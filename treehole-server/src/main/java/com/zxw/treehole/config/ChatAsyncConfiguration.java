package com.zxw.treehole.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 对话 SSE 在独立线程中拉流，避免阻塞 Tomcat 请求线程
 */
@Configuration
@EnableAsync
public class ChatAsyncConfiguration {

    @Bean(name = "chatExecutor")
    public Executor chatExecutor() {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setCorePoolSize(4);
        e.setMaxPoolSize(32);
        e.setQueueCapacity(200);
        e.setThreadNamePrefix("chat-sse-");
        e.initialize();
        return e;
    }
}
