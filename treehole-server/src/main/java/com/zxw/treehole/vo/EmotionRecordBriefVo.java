package com.zxw.treehole.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmotionRecordBriefVo {

    private Long id;
    private BigDecimal sentimentScore;
    private String emotionLabel;
    private String riskLevel;
    private String analysisDetail;
    private LocalDateTime createTime;
}
