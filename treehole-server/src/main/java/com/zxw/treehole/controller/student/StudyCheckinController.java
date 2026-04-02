package com.zxw.treehole.controller.student;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.CheckinCreateRequest;
import com.zxw.treehole.dto.CheckinPageQuery;
import com.zxw.treehole.dto.CheckinUpdateRequest;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.StudyCheckinService;
import com.zxw.treehole.vo.StudyCheckinVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "学生端-学习打卡", description = "仅本人数据，需 STUDENT 角色")
@RestController
@RequestMapping("/api/student/checkins")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('STUDENT')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class StudyCheckinController {

    private final StudyCheckinService studyCheckinService;

    @Operation(summary = "分页查询某目标下打卡记录")
    @GetMapping
    public Result<PageResult<StudyCheckinVo>> page(
            @AuthenticationPrincipal LoginUser loginUser,
            @ParameterObject @Valid CheckinPageQuery query) {
        return Result.ok(studyCheckinService.pageMyCheckins(loginUser.getUser().getId(), query));
    }

    @Operation(summary = "打卡详情")
    @GetMapping("/{id}")
    public Result<StudyCheckinVo> detail(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        return Result.ok(studyCheckinService.getMyCheckin(loginUser.getUser().getId(), id));
    }

    @Operation(summary = "新增打卡")
    @PostMapping
    public Result<Long> create(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody CheckinCreateRequest request) {
        return Result.ok(studyCheckinService.createCheckin(loginUser.getUser().getId(), request));
    }

    @Operation(summary = "更新打卡")
    @PutMapping("/{id}")
    public Result<Void> update(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @Valid @RequestBody CheckinUpdateRequest request) {
        studyCheckinService.updateCheckin(loginUser.getUser().getId(), id, request);
        return Result.ok(null);
    }

    @Operation(summary = "删除打卡（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        studyCheckinService.deleteCheckin(loginUser.getUser().getId(), id);
        return Result.ok(null);
    }
}
