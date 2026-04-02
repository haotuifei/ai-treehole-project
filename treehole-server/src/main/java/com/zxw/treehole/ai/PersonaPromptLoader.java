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
 * 加载可配置的系统人设：yaml 直配优先，否则读 classpath 文件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonaPromptLoader {

    private final AiChatProperties props;
    private final ResourceLoader resourceLoader;

    public String loadSystemPrompt() {
        if (StringUtils.hasText(props.getPersona().getSystemPrompt())) {
            return props.getPersona().getSystemPrompt().trim();
        }
        String location = props.getPersona().getSystemPromptFile();
        if (!StringUtils.hasText(location)) {
            return "";
        }
        try {
            Resource r = resourceLoader.getResource(location);
            if (!r.exists()) {
                log.warn("人设文件不存在: {}", location);
                return "";
            }
            return StreamUtils.copyToString(r.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            log.error("加载人设文件失败: {}", location, e);
            return "";
        }
    }
}
