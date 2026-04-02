package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "最近 7 天学习趋势（含今天，共 7 个自然日）")
public class StudyTrend7dVo {

    @Schema(description = "null 表示汇总全部目标")
    private Long goalId;
    @Schema(description = "按日期升序，无打卡日为 0 分钟")
    private List<TrendPointVo> points;
}
