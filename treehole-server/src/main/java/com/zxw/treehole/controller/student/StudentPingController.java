package com.zxw.treehole.controller.student;

import com.zxw.treehole.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

/** 用于验证学生角色 JWT 是否生效 */
@Tag(name = "学生端-示例", description = "权限校验示例")
@RestController
@RequestMapping("/api/student")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class StudentPingController {

    @Operation(summary = "学生心跳")
    @GetMapping("/ping")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<String> ping() {
        return Result.ok("STUDENT OK");
    }
}
