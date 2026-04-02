package com.zxw.treehole.controller.student;

import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.GoalCreateRequest;
import com.zxw.treehole.dto.GoalUpdateRequest;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.StudyGoalService;
import com.zxw.treehole.vo.StudyGoalVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "学生端-备考目标", description = "仅本人数据，需 STUDENT 角色")
@RestController
@RequestMapping("/api/student/goals")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('STUDENT')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class StudyGoalController {

    private final StudyGoalService studyGoalService;

    @Operation(summary = "我的目标列表")
    @GetMapping
    public Result<List<StudyGoalVo>> list(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false) Integer status) {
        return Result.ok(studyGoalService.listMyGoals(loginUser.getUser().getId(), status));
    }

    @Operation(summary = "目标详情")
    @GetMapping("/{id}")
    public Result<StudyGoalVo> detail(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        return Result.ok(studyGoalService.getMyGoal(loginUser.getUser().getId(), id));
    }

    @Operation(summary = "新增目标")
    @PostMapping
    public Result<Long> create(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody GoalCreateRequest request) {
        return Result.ok(studyGoalService.createGoal(loginUser.getUser().getId(), request));
    }

    @Operation(summary = "更新目标")
    @PutMapping("/{id}")
    public Result<Void> update(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @Valid @RequestBody GoalUpdateRequest request) {
        studyGoalService.updateGoal(loginUser.getUser().getId(), id, request);
        return Result.ok(null);
    }

    @Operation(summary = "删除目标（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        studyGoalService.deleteGoal(loginUser.getUser().getId(), id);
        return Result.ok(null);
    }
}
