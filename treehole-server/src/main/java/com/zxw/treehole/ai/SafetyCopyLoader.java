package com.zxw.treehole.ai;

import com.zxw.treehole.config.AiChatProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 高风险时的固定安全回复文案（可 yaml 覆盖，否则读 classpath 文件）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SafetyCopyLoader {

    private final AiChatProperties props;
    private final ResourceLoader resourceLoader;

    public String loadHighRiskSafeReply() {
        if (StringUtils.hasText(props.getRisk().getSafeReplyText())) {
            return props.getRisk().getSafeReplyText().trim();
        }
        String loc = props.getRisk().getSafeReplyFile();
        if (!StringUtils.hasText(loc)) {
            loc = "classpath:prompts/safe-reply-high-risk.txt";
        }
        try {
            Resource r = resourceLoader.getResource(loc);
            if (!r.exists()) {
                return defaultFallback();
            }
            return StreamUtils.copyToString(r.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            log.error("加载安全回复文案失败", e);
            return defaultFallback();
        }
    }

    private static String defaultFallback() {
        return "谢谢你愿意说出来。你现在可能非常难受，但我不能讨论任何伤害自己的方式。"
                + "请尽快联系你信任的老师、辅导员或家人，或拨打当地心理援助热线寻求专业帮助。我会继续倾听你愿意分享的情绪与压力。";
    }
}
