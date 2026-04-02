package com.zxw.treehole.ai.config;

import com.zxw.treehole.ai.client.ChatLanguageModel;
import com.zxw.treehole.ai.impl.MockChatLanguageModel;
import com.zxw.treehole.ai.impl.OpenAiCompatibleChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 按 app.ai.provider 注入统一的 {@link ChatLanguageModel}
 */
@Configuration
public class AiModelConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai-compatible")
    public ChatLanguageModel chatLanguageModel(
            @Qualifier("openAiCompatibleChatModelBean") OpenAiCompatibleChatModel impl) {
        return impl;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
    public ChatLanguageModel chatLanguageModelMock(
            @Qualifier("mockChatLanguageModelBean") MockChatLanguageModel impl) {
        return impl;
    }
}
