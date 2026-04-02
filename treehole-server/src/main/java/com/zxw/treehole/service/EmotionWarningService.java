package com.zxw.treehole.service;

/**
 * 情绪分析 + 写 emotion_record；高风险时写 warning_record（独立事务）
 */
public interface EmotionWarningService {

    /**
     * 分析用户消息并落库，返回结果供对话流继续分支
     */
    EmotionWarningOutcome analyzeAndPersist(Long userId, Long sessionId, Long messageId, String text);
}
