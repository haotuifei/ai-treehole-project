package com.zxw.treehole.controller.admin;

import com.zxw.treehole.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

/**
 * RBAC 示例：仅管理员可访问（需使用 admin 账号登录获取 JWT）
 */
@Tag(name = "管理端-示例", description = "权限校验示例")
@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class AdminPingController {

    @Operation(summary = "管理员心跳检测")
    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> ping() {
        return Result.ok("ADMIN OK");
    }
}
