package com.zxw.treehole.controller.admin;

import com.zxw.treehole.common.Result;
import com.zxw.treehole.service.AdminWarningStatsService;
import com.zxw.treehole.vo.AdminWarningStatsVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "管理端-预警统计", description = "全局统计")
@RestController
@RequestMapping("/api/admin/warnings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class AdminWarningStatsController {

    private final AdminWarningStatsService adminWarningStatsService;

    @Operation(summary = "全局预警统计")
    @GetMapping("/stats")
    public Result<AdminWarningStatsVo> stats() {
        return Result.ok(adminWarningStatsService.globalStats());
    }
}
