package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "辅导员-干预记录分页查询")
public class CounselorInterventionPageQuery {

    @Min(1)
    private long pageNum = 1;

    @Min(1)
    @Max(100)
    private long pageSize = 10;

    @Schema(description = "学生姓名或用户名模糊")
    private String studentKeyword;
}
