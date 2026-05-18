package com.zxw.treehole.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.entity.*;
import com.zxw.treehole.mapper.*;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "管理端-数据大屏", description = "全局统计数据，需管理员角色")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class AdminDashboardController {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final AiChatSessionMapper chatSessionMapper;
    private final AiChatMessageMapper chatMessageMapper;
    private final EmotionRecordMapper emotionRecordMapper;
    private final WarningRecordMapper warningRecordMapper;
    private final StudyCheckinMapper checkinMapper;
    private final StudyGoalMapper goalMapper;
    private final SystemLogMapper systemLogMapper;

    @Operation(summary = "数据大屏统计")
    @GetMapping
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();

        // 用户统计
        data.put("users", getUserStats());

        // 聊天统计
        data.put("chats", getChatStats());

        // 情绪统计
        data.put("emotions", getEmotionStats());

        // 预警统计
        data.put("warnings", getWarningStats());

        // 学习统计
        data.put("studies", getStudyStats());

        // 系统统计
        data.put("system", getSystemStats());

        return Result.ok(data);
    }

    private Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0)));

        // 各角色用户数
        Map<String, Long> roleCounts = new LinkedHashMap<>();
        roleMapper.selectList(null).forEach(role -> {
            Long count = userRoleMapper.selectCount(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, role.getId()));
            roleCounts.put(role.getRoleCode(), count);
        });
        stats.put("roleCounts", roleCounts);

        // 今日新增用户
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.put("todayNew", userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .ge(SysUser::getCreateTime, todayStart)));

        return stats;
    }

    private Map<String, Object> getChatStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", chatSessionMapper.selectCount(
                new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getDeleted, 0)));
        stats.put("totalMessages", chatMessageMapper.selectCount(
                new LambdaQueryWrapper<AiChatMessage>().eq(AiChatMessage::getDeleted, 0)));

        // 今日聊天数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.put("todayMessages", chatMessageMapper.selectCount(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getDeleted, 0)
                        .ge(AiChatMessage::getCreateTime, todayStart)));

        return stats;
    }

    private Map<String, Object> getEmotionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", emotionRecordMapper.selectCount(
                new LambdaQueryWrapper<EmotionRecord>().eq(EmotionRecord::getDeleted, 0)));

        // 风险等级分布
        Map<String, Long> riskCounts = new LinkedHashMap<>();
        riskCounts.put("LOW", emotionRecordMapper.selectCount(
                new LambdaQueryWrapper<EmotionRecord>()
                        .eq(EmotionRecord::getDeleted, 0)
                        .eq(EmotionRecord::getRiskLevel, "LOW")));
        riskCounts.put("MEDIUM", emotionRecordMapper.selectCount(
                new LambdaQueryWrapper<EmotionRecord>()
                        .eq(EmotionRecord::getDeleted, 0)
                        .eq(EmotionRecord::getRiskLevel, "MEDIUM")));
        riskCounts.put("HIGH", emotionRecordMapper.selectCount(
                new LambdaQueryWrapper<EmotionRecord>()
                        .eq(EmotionRecord::getDeleted, 0)
                        .eq(EmotionRecord::getRiskLevel, "HIGH")));
        stats.put("riskCounts", riskCounts);

        return stats;
    }

    private Map<String, Object> getWarningStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", warningRecordMapper.selectCount(
                new LambdaQueryWrapper<WarningRecord>().eq(WarningRecord::getDeleted, 0)));

        // 近7天预警数
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        stats.put("last7Days", warningRecordMapper.selectCount(
                new LambdaQueryWrapper<WarningRecord>()
                        .eq(WarningRecord::getDeleted, 0)
                        .ge(WarningRecord::getCreateTime, sevenDaysAgo)));

        return stats;
    }

    private Map<String, Object> getStudyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGoals", goalMapper.selectCount(
                new LambdaQueryWrapper<StudyGoal>().eq(StudyGoal::getDeleted, 0)));
        stats.put("totalCheckins", checkinMapper.selectCount(
                new LambdaQueryWrapper<StudyCheckin>().eq(StudyCheckin::getDeleted, 0)));

        // 今日打卡数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.put("todayCheckins", checkinMapper.selectCount(
                new LambdaQueryWrapper<StudyCheckin>()
                        .eq(StudyCheckin::getDeleted, 0)
                        .ge(StudyCheckin::getCreateTime, todayStart)));

        return stats;
    }

    private Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 今日请求数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.put("todayRequests", systemLogMapper.selectCount(
                new LambdaQueryWrapper<SystemLog>()
                        .ge(SystemLog::getCreateTime, todayStart)));

        // 今日失败数
        stats.put("todayFailures", systemLogMapper.selectCount(
                new LambdaQueryWrapper<SystemLog>()
                        .ge(SystemLog::getCreateTime, todayStart)
                        .eq(SystemLog::getStatus, 0)));

        return stats;
    }
}
