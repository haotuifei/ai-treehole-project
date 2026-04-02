package com.zxw.treehole.controller.student;

import com.zxw.treehole.common.Result;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.StudyStatsService;
import com.zxw.treehole.vo.GoalTotalMinutesVo;
import com.zxw.treehole.vo.MonthCheckinDaysVo;
import com.zxw.treehole.vo.StudyTrend7dVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "学生端-学习统计", description = "仅本人数据，需 STUDENT 角色")
@RestController
@RequestMapping("/api/student/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class StudyStatsController {

    private final StudyStatsService studyStatsService;

    @Operation(summary = "某目标累计学习时长（分钟）")
    @GetMapping("/goals/{goalId}/total-minutes")
    public Result<GoalTotalMinutesVo> totalMinutes(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long goalId) {
        return Result.ok(studyStatsService.totalMinutesForGoal(loginUser.getUser().getId(), goalId));
    }

    @Operation(summary = "某月打卡天数（distinct 日期）；goalId 为空则统计全部目标")
    @GetMapping("/checkins/month-days")
    public Result<MonthCheckinDaysVo> monthDays(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam String yearMonth,
            @RequestParam(required = false) Long goalId) {
        return Result.ok(studyStatsService.monthCheckinDays(loginUser.getUser().getId(), yearMonth, goalId));
    }

    @Operation(summary = "最近 7 天学习时长趋势；goalId 为空则汇总全部目标")
    @GetMapping("/checkins/trend-7d")
    public Result<StudyTrend7dVo> trend7d(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false) Long goalId) {
        return Result.ok(studyStatsService.last7DaysTrend(loginUser.getUser().getId(), goalId));
    }
}
