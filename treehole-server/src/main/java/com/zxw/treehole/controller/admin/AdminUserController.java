package com.zxw.treehole.controller.admin;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.UserCreateRequest;
import com.zxw.treehole.dto.UserPageQuery;
import com.zxw.treehole.dto.UserUpdateRequest;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.SysUserService;
import com.zxw.treehole.vo.SysUserDetailVo;
import com.zxw.treehole.vo.SysUserPageVo;
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

/**
 * 管理员：用户管理 CRUD（RBAC：仅 ADMIN）
 */
@Tag(name = "管理端-用户管理", description = "用户增删改查，需管理员角色")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class AdminUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<PageResult<SysUserPageVo>> page(@ParameterObject @Valid UserPageQuery query) {
        return Result.ok(sysUserService.pageUsers(query));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<SysUserDetailVo> detail(@PathVariable Long id) {
        return Result.ok(sysUserService.getDetail(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.ok(sysUserService.createUser(request));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        sysUserService.updateUser(id, request);
        return Result.ok(null);
    }

    @Operation(summary = "逻辑删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        Long opId = loginUser != null ? loginUser.getUser().getId() : null;
        sysUserService.deleteUser(id, opId);
        return Result.ok(null);
    }
}
