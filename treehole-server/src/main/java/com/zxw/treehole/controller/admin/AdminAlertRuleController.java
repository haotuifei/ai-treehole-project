package com.zxw.treehole.controller.admin;

import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.AlertRuleUpdateRequest;
import com.zxw.treehole.service.AlertRuleService;
import com.zxw.treehole.vo.AlertRuleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "管理端-预警规则", description = "阈值配置")
@RestController
@RequestMapping("/api/admin/alert-rule")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class AdminAlertRuleController {

    private final AlertRuleService alertRuleService;

    @Operation(summary = "获取默认预警规则")
    @GetMapping
    public Result<AlertRuleVo> get() {
        return Result.ok(alertRuleService.getDefaultRule());
    }

    @Operation(summary = "更新默认预警规则")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody AlertRuleUpdateRequest request) {
        alertRuleService.updateDefaultRule(request);
        return Result.ok(null);
    }
}
