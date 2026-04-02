package com.zxw.treehole.service;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单条用户消息的情绪分析 + 预警落库结果（供 SSE meta 与业务使用）
 */
@Data
public class EmotionWarningOutcome {

    private String riskLevel;
    private String emotionLabel;
    private BigDecimal sentimentScore;
    private Long emotionRecordId;
    private Long warningRecordId;
    /** 分析完成时间 */
    private LocalDateTime analyzedAt;
}
