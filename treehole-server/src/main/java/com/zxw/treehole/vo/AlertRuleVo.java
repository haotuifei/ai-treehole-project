package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "预警阈值规则")
public class AlertRuleVo {

    private Long id;
    private String ruleCode;
    private BigDecimal mediumSentimentThreshold;
    private BigDecimal highSentimentThreshold;
    private Integer keywordHighEnabled;
    private String remark;
    private LocalDateTime updateTime;
}
