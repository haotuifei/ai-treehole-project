package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户关联角色")
public class UserRoleVo {

    private Long roleId;
    private String roleCode;
    private String roleName;
}
