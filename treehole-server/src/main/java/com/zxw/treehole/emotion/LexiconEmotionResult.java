package com.zxw.treehole.emotion;

import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 词典法情绪分析结果（可运行版，后续可换模型）
 */
@Data
public class LexiconEmotionResult {

    /** [-1,1]，越低越消极 */
    private BigDecimal sentimentScore;
    /** POSITIVE/NEUTRAL/ANXIOUS/SAD/ANGRY/SUPPRESSED */
    private String emotionLabel;
    /** 各词典命中次数，写入 analysis_detail */
    private Map<String, Integer> hitCounts = new LinkedHashMap<>();
}
