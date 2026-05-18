package com.zxw.treehole.config;

import com.zxw.treehole.entity.ModelConfig;
import com.zxw.treehole.service.ModelConfigService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 树洞：模型接入与人设 Prompt
 * 所有模型配置从数据库读取
 */
@Slf4j
@Data
@Component
public class AiChatProperties {

    private final ModelConfigService modelConfigService;

    /** API 端点路径 */
    private String apiEndpoint = "/v1/messages";

    private int connectTimeoutMs = 30_000;

    private int readTimeoutMs = 300_000;

    /** 参与上下文的历史消息条数 */
    private int maxContextMessages = 24;

    private Persona persona = new Persona();

    private Risk risk = new Risk();

    public AiChatProperties(ModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    /**
     * 获取 API Base，从数据库读取
     */
    public String getEffectiveApiBase() {
        return getDbConfig().getApiBase();
    }

    /**
     * 获取 API Key，从数据库读取
     */
    public String getEffectiveApiKey() {
        return getDbConfig().getApiKeyCipher();
    }

    /**
     * 获取模型名称，从数据库读取
     */
    public String getEffectiveModel() {
        return getDbConfig().getModelName();
    }

    /**
     * 获取 Provider，从数据库读取
     */
    public String getEffectiveProvider() {
        return "openai-compatible";
    }

    /**
     * 检查是否有启用的模型配置
     */
    public boolean hasActiveConfig() {
        try {
            boolean result = modelConfigService.getActiveConfig().isPresent();
            log.info("检查模型配置: hasActiveConfig={}", result);
            return result;
        } catch (Exception e) {
            log.error("检查模型配置异常", e);
            return false;
        }
    }

    private ModelConfig getDbConfig() {
        return modelConfigService.getActiveConfig()
                .orElseThrow(() -> new IllegalStateException("没有启用的模型配置，请在管理后台配置模型"));
    }

    @Data
    public static class Persona {
        private String systemPromptFile = "classpath:prompts/treehole-persona.txt";
        private String systemPrompt = "";
    }

    @Data
    public static class Risk {
        private boolean enabled = true;
        private List<String> highKeywords = defaultHighKeywords();
        private String safeReplyText = "";
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
