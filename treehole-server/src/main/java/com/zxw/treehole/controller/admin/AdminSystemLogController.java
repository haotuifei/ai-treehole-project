package com.zxw.treehole.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.entity.SystemLog;
import com.zxw.treehole.mapper.SystemLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "管理端-系统日志", description = "系统操作日志查询，需管理员角色")
@RestController
@RequestMapping("/api/admin/system-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class AdminSystemLogController {

    private final SystemLogMapper systemLogMapper;

    @Operation(summary = "分页查询系统日志")
    @GetMapping
    public Result<PageResult<SystemLog>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(module)) {
            wrapper.eq(SystemLog::getModule, module);
        }
        if (status != null) {
            wrapper.eq(SystemLog::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SystemLog::getOperation, keyword)
                    .or().like(SystemLog::getRequestUri, keyword)
                    .or().like(SystemLog::getIp, keyword)
            );
        }

        wrapper.orderByDesc(SystemLog::getCreateTime);

        long total = systemLogMapper.selectCount(wrapper);
        wrapper.last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize);
        List<SystemLog> list = systemLogMapper.selectList(wrapper);

        return Result.ok(new PageResult<>(total, pageNum, pageSize, list));
    }
}
