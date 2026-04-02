package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 当前登录用户完整资料（不含密码）
 */
@Data
@Schema(description = "当前用户资料")
public class UserProfileVo {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String studentNo;
    private String className;
    private Long counselorId;
    private String avatarUrl;
    /** 0 禁用 1 正常 */
    private Integer status;
    @Schema(description = "角色编码列表，如 ADMIN、COUNSELOR、STUDENT")
    private List<String> roleCodes;
    @Schema(description = "角色名称列表")
    private List<String> roleNames;
}
