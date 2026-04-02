package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户简要信息（无敏感字段）")
public class UserBriefVo {

    private Long id;
    private String username;
    private String realName;
    private String studentNo;
    private String className;
    private String avatarUrl;
}
