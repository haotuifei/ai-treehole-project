package com.zxw.treehole.service;

import com.zxw.treehole.dto.ChatStreamRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiChatStreamService {

    /**
     * 异步启动 SSE：立即返回，在后台线程中写入事件流
     */
    void startStreamAsync(Long userId, ChatStreamRequest request, SseEmitter emitter);
}
