package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "情绪统计数据")
public class EmotionStatsVo {

    @Schema(description = "情绪均分（0-100）")
    private BigDecimal avgScore;

    @Schema(description = "对话次数")
    private Integer chatCount;

    @Schema(description = "高风险次数")
    private Integer highRiskCount;

    @Schema(description = "情绪标签分布 {\"积极\": 5, \"消极\": 2}")
    private Map<String, Integer> emotionLabelDistribution;

    @Schema(description = "风险等级分布 {\"LOW\": 10, \"MEDIUM\": 3, \"HIGH\": 1}")
    private Map<String, Integer> riskLevelDistribution;

    @Schema(description = "每日情绪分值趋势 [{date: \"2026-04-10\", score: 72}, ...]")
    private List<DailyScore> dailyTrend;

    @Schema(description = "每日对话次数 [{date: \"2026-04-10\", count: 3}, ...]")
    private List<DailyCount> dailyChatCount;

    @Schema(description = "改善建议列表")
    private List<String> suggestions;

    @Data
    public static class DailyScore {
        private String date;
        private BigDecimal score;
    }

    @Data
    public static class DailyCount {
        private String date;
        private Integer count;
    }
}
