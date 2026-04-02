package com.zxw.treehole.controller;

import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.LoginRequest;
import com.zxw.treehole.dto.RegisterRequest;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.AuthService;
import com.zxw.treehole.vo.LoginVo;
import com.zxw.treehole.vo.UserProfileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证", description = "登录、注册、当前用户")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录", description = "返回 JWT，请在 Swagger 中 Authorize 填入：Bearer {token}")
    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @Operation(summary = "学生注册", description = "默认仅绑定 STUDENT 角色")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok(null);
    }

    @Operation(summary = "当前登录用户资料（含角色）")
    @GetMapping("/profile")
    public Result<UserProfileVo> profile(@AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            return Result.fail(401, "未登录");
        }
        return Result.ok(authService.currentProfile(loginUser.getUser().getId()));
    }
}
