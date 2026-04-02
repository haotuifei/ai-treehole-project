package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录返回")
public class LoginVo {

    @Schema(description = "JWT Token")
    private String token;
    @Schema(description = "Token 类型")
    private String tokenType = "Bearer";
    @Schema(description = "过期毫秒数")
    private Long expiresIn;
    @Schema(description = "用户简要信息")
    private UserBriefVo user;
    @Schema(description = "角色编码列表")
    private List<String> roles;
}
