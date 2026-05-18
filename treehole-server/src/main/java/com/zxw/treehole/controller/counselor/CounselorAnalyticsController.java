package com.zxw.treehole.controller.counselor;

import com.zxw.treehole.common.Result;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.CounselorAnalyticsService;
import com.zxw.treehole.vo.ClassAnalyticsVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "辅导员端-班级数据分析", description = "管辖学生综合统计，需辅导员角色")
@RestController
@RequestMapping("/api/counselor/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COUNSELOR')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class CounselorAnalyticsController {

    private final CounselorAnalyticsService counselorAnalyticsService;

    @Operation(summary = "班级数据概览")
    @GetMapping
    public Result<ClassAnalyticsVo> analytics(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(counselorAnalyticsService.getClassAnalytics(loginUser.getUser().getId()));
    }
}
