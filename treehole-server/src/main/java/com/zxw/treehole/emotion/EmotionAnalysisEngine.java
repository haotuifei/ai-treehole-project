package com.zxw.treehole.emotion;

/**
 * 情绪分析引擎接口（当前实现：{@link LexiconEmotionAnalyzer}；后续可替换为深度学习/API）
 */
public interface EmotionAnalysisEngine {

    /**
     * 对单段文本做情绪特征提取（不含落库与预警）
     */
    LexiconEmotionResult analyze(String text);
}
