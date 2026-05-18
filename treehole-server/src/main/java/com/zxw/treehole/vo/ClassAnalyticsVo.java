package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "辅导员端-班级数据分析")
public class ClassAnalyticsVo {

    // === 概览 ===
    @Schema(description = "管辖学生总数")
    private Long totalStudents;

    @Schema(description = "近7天有情绪记录的学生数")
    private Long activeStudents;

    @Schema(description = "学生平均情绪分值")
    private BigDecimal avgEmotionScore;

    @Schema(description = "预警总数")
    private Long totalWarnings;

    @Schema(description = "待处理预警数")
    private Long pendingWarnings;

    @Schema(description = "累计打卡天次")
    private Long totalCheckins;

    // === 图表数据 ===
    @Schema(description = "风险等级分布 {LOW: n, MEDIUM: n, HIGH: n}")
    private Map<String, Long> riskDistribution;

    @Schema(description = "近7天每日预警数 [{date, count}]")
    private List<DailyCount> warningTrend;

    @Schema(description = "近7天每日打卡数 [{date, count}]")
    private List<DailyCount> checkinTrend;

    @Schema(description = "学生情绪排行（按情绪分值升序，高风险优先）")
    private List<StudentRankItem> studentRanking;

    @Data
    public static class DailyCount {
        private String date;
        private Long count;

        public DailyCount(String date, Long count) {
            this.date = date;
            this.count = count;
        }
    }

    @Data
    public static class StudentRankItem {
        private Long userId;
        private String realName;
        private String className;
        private BigDecimal avgScore;
        private Long warningCount;
        private Long highRiskCount;
    }
}
