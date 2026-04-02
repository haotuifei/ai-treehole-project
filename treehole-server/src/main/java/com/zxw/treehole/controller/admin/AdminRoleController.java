package com.zxw.treehole.controller.admin;

import com.zxw.treehole.common.Result;
import com.zxw.treehole.service.SysUserService;
import com.zxw.treehole.vo.SysRoleOptionVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

/**
 * 管理员：角色列表（分配用户时使用）
 */
@Tag(name = "管理端-角色", description = "角色枚举")
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class AdminRoleController {

    private final SysUserService sysUserService;

    @Operation(summary = "全部角色")
    @GetMapping
    public Result<List<SysRoleOptionVo>> listRoles() {
        return Result.ok(sysUserService.listAllRoles());
    }
}
