package com.zxw.treehole.controller.counselor;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.CounselorWarningPageQuery;
import com.zxw.treehole.dto.InterventionCreateRequest;
import com.zxw.treehole.dto.WarningStatusUpdateRequest;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.CounselorWarningService;
import com.zxw.treehole.vo.WarningDetailVo;
import com.zxw.treehole.vo.WarningListItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "辅导员端-预警", description = "仅管辖内学生")
@RestController
@RequestMapping("/api/counselor/warnings")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('COUNSELOR')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class CounselorWarningController {

    private final CounselorWarningService counselorWarningService;

    @Operation(summary = "预警分页列表")
    @GetMapping
    public Result<PageResult<WarningListItemVo>> page(
            @AuthenticationPrincipal LoginUser loginUser,
            @ParameterObject @Valid CounselorWarningPageQuery query) {
        return Result.ok(counselorWarningService.pageWarnings(loginUser.getUser().getId(), query));
    }

    @Operation(summary = "预警详情（含情绪分析快照与干预记录）")
    @GetMapping("/{id}")
    public Result<WarningDetailVo> detail(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        return Result.ok(counselorWarningService.getDetail(loginUser.getUser().getId(), id));
    }

    @Operation(summary = "更新处理状态与备注")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @Valid @RequestBody WarningStatusUpdateRequest request) {
        counselorWarningService.updateStatus(loginUser.getUser().getId(), id, request);
        return Result.ok(null);
    }

    @Operation(summary = "填写干预记录")
    @PostMapping("/{id}/interventions")
    public Result<Void> addIntervention(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @Valid @RequestBody InterventionCreateRequest request) {
        counselorWarningService.addIntervention(loginUser.getUser().getId(), id, request);
        return Result.ok(null);
    }
}
