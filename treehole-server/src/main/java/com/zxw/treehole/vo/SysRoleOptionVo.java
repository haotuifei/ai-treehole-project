package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色下拉选项")
public class SysRoleOptionVo {

    private Long id;
    private String roleCode;
    private String roleName;
}
