package com.zxw.treehole.controller.counselor;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.CounselorInterventionPageQuery;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.CounselorInterventionService;
import com.zxw.treehole.vo.InterventionListItemVo;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "辅导员端-干预记录", description = "查看管辖学生的干预记录，需辅导员角色")
@RestController
@RequestMapping("/api/counselor/interventions")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('COUNSELOR')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class CounselorInterventionController {

    private final CounselorInterventionService counselorInterventionService;

    @Operation(summary = "干预记录分页列表")
    @GetMapping
    public Result<PageResult<InterventionListItemVo>> page(
            @AuthenticationPrincipal LoginUser loginUser,
            @ParameterObject @Valid CounselorInterventionPageQuery query) {
        return Result.ok(counselorInterventionService.pageInterventions(loginUser.getUser().getId(), query));
    }
}
