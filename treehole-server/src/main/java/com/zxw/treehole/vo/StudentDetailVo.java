package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "辅导员端-学生详情")
public class StudentDetailVo {

    // === 基本信息 ===
    private Long id;
    private String username;
    private String realName;
    private String studentNo;
    private String className;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime createTime;

    // === 情绪统计 ===
    @Schema(description = "情绪均分")
    private BigDecimal avgEmotionScore;

    @Schema(description = "情绪记录总数")
    private Long emotionTotalCount;

    @Schema(description = "高风险次数")
    private Long highRiskCount;

    @Schema(description = "风险等级分布 {LOW: n, MEDIUM: n, HIGH: n}")
    private Map<String, Long> riskLevelDistribution;

    @Schema(description = "最近情绪记录")
    private List<EmotionRecordBriefVo> recentEmotions;

    // === 预警统计 ===
    @Schema(description = "预警总数")
    private Long warningTotalCount;

    @Schema(description = "预警状态分布 {PENDING: n, PROCESSING: n, RESOLVED: n, CLOSED: n}")
    private Map<String, Long> warningStatusDistribution;

    // === 学习统计 ===
    @Schema(description = "目标总数")
    private Long goalTotalCount;

    @Schema(description = "进行中目标数")
    private Long goalActiveCount;

    @Schema(description = "累计打卡天数")
    private Long checkinTotalDays;

    @Schema(description = "最近7天打卡天数")
    private Long checkinLast7Days;
}
