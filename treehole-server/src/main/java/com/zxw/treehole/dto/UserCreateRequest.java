package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "管理员创建用户")
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度 3-64")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度 6-64")
    private String password;

    private String realName;
    private String phone;
    private String email;
    private String studentNo;
    private String className;
    private Long counselorId;
    private String avatarUrl;

    @Schema(description = "0 禁用 1 正常，默认 1")
    private Integer status = 1;

    @NotEmpty(message = "至少选择一个角色")
    @Schema(description = "角色 ID 列表，须为已存在的 sys_role.id")
    private List<Long> roleIds;
}
