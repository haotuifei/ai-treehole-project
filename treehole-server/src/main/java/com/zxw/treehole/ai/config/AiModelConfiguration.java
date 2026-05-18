package com.zxw.treehole.ai.config;

import com.zxw.treehole.ai.client.ChatLanguageModel;
import com.zxw.treehole.ai.client.LlmMessage;
import com.zxw.treehole.ai.impl.OpenAiCompatibleChatModel;
import com.zxw.treehole.config.AiChatProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * 根据数据库配置动态注入 ChatLanguageModel
 */
@Slf4j
@Configuration
public class AiModelConfiguration {

    @Bean
    @Primary
    public ChatLanguageModel chatLanguageModel(
            OpenAiCompatibleChatModel openAiModel,
            AiChatProperties aiChatProperties) {
        // 返回代理，每次调用时动态检查配置
        return new DynamicChatLanguageModel(openAiModel, aiChatProperties);
    }

    /**
     * 动态代理：每次调用时检查数据库配置
     */
    @RequiredArgsConstructor
    private static class DynamicChatLanguageModel implements ChatLanguageModel {
        private final OpenAiCompatibleChatModel delegate;
        private final AiChatProperties aiChatProperties;

        @Override
        public void streamChat(List<LlmMessage> messages, LlmChunkConsumer onChunk) throws Exception {
            if (!aiChatProperties.hasActiveConfig()) {
                throw new IllegalStateException("没有启用的模型配置，请在管理后台配置模型");
            }
            delegate.streamChat(messages, onChunk);
        }
    }
}
