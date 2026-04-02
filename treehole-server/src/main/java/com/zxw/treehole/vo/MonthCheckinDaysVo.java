package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "某月有打卡记录的天数（同一天多次打卡只计 1 天）")
public class MonthCheckinDaysVo {

    @Schema(description = "yyyy-MM")
    private String yearMonth;
    @Schema(description = "null 表示统计全部目标")
    private Long goalId;
    @Schema(description = " distinct 打卡天数")
    private Long distinctDays;
}
