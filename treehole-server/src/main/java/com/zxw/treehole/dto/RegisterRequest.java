package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "学生注册请求")
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度 3-64")
    @Schema(description = "登录用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度 6-64")
    @Schema(description = "密码")
    private String password;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "班级名称")
    private String className;
}
