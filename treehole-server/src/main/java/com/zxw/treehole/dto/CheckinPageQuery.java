package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "打卡分页查询")
public class CheckinPageQuery {

    @Min(1)
    private long pageNum = 1;

    @Min(1)
    @Max(100)
    private long pageSize = 10;

    @NotNull(message = "必须指定所属目标")
    private Long goalId;

    @Schema(description = "起始日期（含）")
    private LocalDate dateFrom;

    @Schema(description = "结束日期（含）")
    private LocalDate dateTo;
}
