package com.zxw.treehole.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 树洞：模型接入与人设 Prompt（可被 yaml / 环境变量覆盖）
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiChatProperties {

    /** mock：本地演示；openai-compatible：OpenAI 兼容接口（DeepSeek/通义兼容模式等） */
    private String provider = "mock";

    private String apiBase = "https://api.deepseek.com";

    private String apiKey = "";

    private String model = "deepseek-chat";

    /** API 端点路径，默认 /v1/chat/completions，Anthropic 兼容填 /v1/messages */
    private String apiEndpoint = "/v1/chat/completions";

    private int connectTimeoutMs = 30_000;

    private int readTimeoutMs = 300_000;

    /** 参与上下文的历史消息条数（user+assistant 各算一条，不含 system） */
    private int maxContextMessages = 24;

    private Persona persona = new Persona();

    private Risk risk = new Risk();

    @Data
    public static class Persona {
        /** 优先读取该 classpath 文件（UTF-8） */
        private String systemPromptFile = "classpath:prompts/treehole-persona.txt";
        /** 非空时覆盖文件内容（适合部署时注入长文本） */
        private String systemPrompt = "";
    }

    @Data
    public static class Risk {
        private boolean enabled = true;
        /** 命中即视为 HIGH，走安全回复 + 预警 */
        private List<String> highKeywords = defaultHighKeywords();
        /** 非空时覆盖下方文件 */
        private String safeReplyText = "";
        /** 高风险固定安抚话术文件 */
        private String safeReplyFile = "classpath:prompts/safe-reply-high-risk.txt";
    }

    private static List<String> defaultHighKeywords() {
        List<String> list = new ArrayList<>();
        list.add("自杀");
        list.add("自殺");
        list.add("结束生命");
        list.add("不想活了");
        list.add("去死");
        list.add("自残");
        list.add("割腕");
        list.add("跳楼");
        list.add("烧炭");
        list.add("安乐死");
        list.add("杀了");
        list.add("杀人");
        list.add("同归于尽");
        return list;
    }
}
