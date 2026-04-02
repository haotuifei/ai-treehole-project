package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新默认预警规则")
public class AlertRuleUpdateRequest {

    @NotNull
    @Schema(description = "中风险阈值，情绪分值<=此且>高风险线为中等（区间[-1,1]）")
    private BigDecimal mediumSentimentThreshold;

    @NotNull
    @Schema(description = "高风险阈值（情绪维），分值<=此视为情绪高风险")
    private BigDecimal highSentimentThreshold;

    @NotNull
    @Schema(description = "1 启用关键词直判高风险，0 关闭")
    private Integer keywordHighEnabled;

    private String remark;
}
