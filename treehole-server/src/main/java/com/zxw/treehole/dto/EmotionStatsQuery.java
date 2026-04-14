package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmotionStatsQuery {

    @Parameter(description = "统计周期：day(今日) / week(本周) / month(本月)")
    @NotBlank(message = "period 不能为空")
    private String period;
}
