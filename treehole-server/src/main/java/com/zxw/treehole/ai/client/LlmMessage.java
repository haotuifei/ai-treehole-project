package com.zxw.treehole.ai.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 与大模型交互的标准消息（OpenAI 兼容 role）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmMessage {

    /** system / user / assistant */
    private String role;
    private String content;
}
