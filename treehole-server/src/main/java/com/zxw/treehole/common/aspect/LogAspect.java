package com.zxw.treehole.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxw.treehole.entity.SystemLog;
import com.zxw.treehole.mapper.SystemLogMapper;
import com.zxw.treehole.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面：自动记录控制器方法调用
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SystemLogMapper systemLogMapper;
    private final ObjectMapper objectMapper;

    @Pointcut("execution(* com.zxw.treehole.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        SystemLog sysLog = new SystemLog();

        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                sysLog.setRequestUri(request.getRequestURI());
                sysLog.setRequestMethod(request.getMethod());
                sysLog.setIp(getClientIp(request));
                sysLog.setUserAgent(request.getHeader("User-Agent"));
            }

            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                sysLog.setUserId(loginUser.getUser().getId());
            }

            // 方法信息
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            sysLog.setMethod(className + "." + methodName);
            sysLog.setModule(inferModule(className));
            sysLog.setOperation(inferOperation(className, methodName));

            // 执行方法
            Object result = joinPoint.proceed();

            sysLog.setStatus(1);
            sysLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
            saveLog(sysLog);

            return result;
        } catch (Throwable e) {
            sysLog.setStatus(0);
            sysLog.setErrorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : null);
            sysLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
            saveLog(sysLog);
            throw e;
        }
    }

    private void saveLog(SystemLog sysLog) {
        try {
            systemLogMapper.insert(sysLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String inferModule(String className) {
        if (className.contains("Auth")) return "AUTH";
        if (className.contains("User") || className.contains("Role")) return "USER";
        if (className.contains("Chat")) return "CHAT";
        if (className.contains("Emotion") || className.contains("Warning") || className.contains("Alert")) return "EMOTION";
        if (className.contains("Admin")) return "SYSTEM";
        return "OTHER";
    }

    private String inferOperation(String className, String methodName) {
        String action = switch (methodName) {
            case "login" -> "用户登录";
            case "register" -> "用户注册";
            case "profile" -> "查看个人资料";
            case "page", "list" -> "查询列表";
            case "detail", "get" -> "查看详情";
            case "create", "add" -> "新增";
            case "update", "put" -> "更新";
            case "delete", "remove" -> "删除";
            case "stream", "streamChat" -> "AI对话";
            default -> methodName;
        };
        return action;
    }
}
