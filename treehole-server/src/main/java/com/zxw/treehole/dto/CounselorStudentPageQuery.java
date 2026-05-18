package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "辅导员-学生分页查询")
public class CounselorStudentPageQuery {

    @Min(value = 1, message = "页码最小为 1")
    @Schema(description = "页码，从 1 开始", example = "1")
    private long pageNum = 1;

    @Min(value = 1, message = "每页至少 1 条")
    @Max(value = 100, message = "每页最多 100 条")
    @Schema(description = "每页条数", example = "10")
    private long pageSize = 10;

    @Schema(description = "关键词：匹配姓名、学号、用户名")
    private String keyword;
}
