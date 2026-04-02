package com.zxw.treehole.ai.client;

import java.util.List;

/**
 * 第三方大模型统一适配：流式输出 token 片段（UTF-8 文本）
 */
public interface ChatLanguageModel {

    /**
     * 流式对话：按到达顺序多次调用 onChunk，结束后正常返回；异常向上抛出
     */
    void streamChat(List<LlmMessage> messages, LlmChunkConsumer onChunk) throws Exception;

    @FunctionalInterface
    interface LlmChunkConsumer {
        void accept(String textChunk) throws Exception;
    }
}
