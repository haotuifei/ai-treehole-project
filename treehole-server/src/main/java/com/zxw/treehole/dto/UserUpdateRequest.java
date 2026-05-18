package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "管理员更新用户（不传字段表示不修改；password 为空则不修改密码）")
public class UserUpdateRequest {

    private String realName;
    private String phone;
    private String email;
    private String studentNo;
    private String className;
    private Long counselorId;
    private String avatarUrl;

    @Schema(description = "0 禁用 1 正常")
    private Integer status;

    @Schema(description = "新密码，可选，留空或不传则不修改")
    private String password;

    @Schema(description = "若传入则全量替换用户角色（至少保留一个角色）")
    private List<Long> roleIds;
}
