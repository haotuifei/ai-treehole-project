package com.zxw.treehole.dto;

import java.math.BigDecimal;

/**
 * 当前生效的预警阈值快照（无表数据时使用内置默认）
 */
public record AlertRuleSnapshot(
        BigDecimal mediumSentimentThreshold,
        BigDecimal highSentimentThreshold,
        boolean keywordHighEnabled
) {
}
