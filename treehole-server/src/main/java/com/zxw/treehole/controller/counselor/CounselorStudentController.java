package com.zxw.treehole.controller.counselor;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.CounselorStudentPageQuery;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.CounselorStudentService;
import com.zxw.treehole.vo.StudentDetailVo;
import com.zxw.treehole.vo.StudentProfileVo;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "辅导员端-学生档案", description = "查看管辖学生信息，需辅导员角色")
@RestController
@RequestMapping("/api/counselor/students")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('COUNSELOR')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class CounselorStudentController {

    private final CounselorStudentService counselorStudentService;

    @Operation(summary = "学生分页列表")
    @GetMapping
    public Result<PageResult<StudentProfileVo>> page(
            @AuthenticationPrincipal LoginUser loginUser,
            @ParameterObject @Valid CounselorStudentPageQuery query) {
        return Result.ok(counselorStudentService.pageStudents(loginUser.getUser().getId(), query));
    }

    @Operation(summary = "学生详情（情绪/预警/学习统计）")
    @GetMapping("/{id}")
    public Result<StudentDetailVo> detail(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        return Result.ok(counselorStudentService.getStudentDetail(loginUser.getUser().getId(), id));
    }
}
