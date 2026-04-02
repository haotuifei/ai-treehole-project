package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端用户详情（无密码）
 */
@Data
@Schema(description = "用户详情")
public class SysUserDetailVo {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String studentNo;
    private String className;
    private Long counselorId;
    private String avatarUrl;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @Schema(description = "角色明细")
    private List<UserRoleVo> roles;
}
