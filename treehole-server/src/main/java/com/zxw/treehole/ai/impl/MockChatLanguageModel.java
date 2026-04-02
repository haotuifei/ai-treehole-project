package com.zxw.treehole.ai.impl;

import com.zxw.treehole.ai.client.ChatLanguageModel;
import com.zxw.treehole.ai.client.LlmMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 无 API Key 时的本地演示：把用户最后一条 user 消息回声并分段输出，便于联调 SSE
 */
@Slf4j
@Component("mockChatLanguageModelBean")
public class MockChatLanguageModel implements ChatLanguageModel {

    private static final String PREFIX = "（演示模式）学长/学姐听到啦～ ";

    @Override
    public void streamChat(List<LlmMessage> messages, LlmChunkConsumer onChunk) throws Exception {
        String lastUser = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            LlmMessage m = messages.get(i);
            if ("user".equalsIgnoreCase(m.getRole()) && m.getContent() != null) {
                lastUser = m.getContent();
                break;
            }
        }
        String reply = PREFIX + "你说的是：「" + lastUser + "」。先抱抱你，备考真的不容易。我们可以一点点聊，你现在最想被理解的是哪一部分呢？";
        for (int i = 0; i < reply.length(); i++) {
            onChunk.accept(String.valueOf(reply.charAt(i)));
            Thread.sleep(12);
        }
        log.debug("mock stream done, len={}", reply.length());
    }
}
