package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户分页查询参数
 */
@Data
@Schema(description = "用户分页查询")
public class UserPageQuery {

    @Min(value = 1, message = "页码最小为 1")
    @Schema(description = "页码，从 1 开始", example = "1")
    private long pageNum = 1;

    @Min(value = 1, message = "每页至少 1 条")
    @Max(value = 100, message = "每页最多 100 条")
    @Schema(description = "每页条数", example = "10")
    private long pageSize = 10;

    @Schema(description = "关键词：匹配用户名、真实姓名")
    private String keyword;

    @Schema(description = "按角色编码筛选：ADMIN / COUNSELOR / STUDENT")
    private String roleCode;

    @Schema(description = "账号状态：0 禁用 1 正常，空表示不限")
    private Integer status;
}
