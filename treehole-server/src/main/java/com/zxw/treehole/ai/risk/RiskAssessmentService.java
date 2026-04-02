package com.zxw.treehole.ai.risk;

import com.zxw.treehole.config.AiChatProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 关键词维度高风险识别（落库由 {@link com.zxw.treehole.service.EmotionWarningService} 统一处理）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAssessmentService {

    private static final String LEVEL_LOW = "LOW";
    private static final String LEVEL_HIGH = "HIGH";

    private final AiChatProperties props;

    public RiskAssessmentResult assess(String userText) {
        RiskAssessmentResult r = new RiskAssessmentResult();
        r.setLevel(LEVEL_LOW);
        if (!props.getRisk().isEnabled() || !StringUtils.hasText(userText)) {
            return r;
        }
        String t = userText.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (String kw : props.getRisk().getHighKeywords()) {
            if (!StringUtils.hasText(kw)) {
                continue;
            }
            if (userText.contains(kw) || t.contains(kw.toLowerCase())) {
                matched.add(kw);
            }
        }
        if (!matched.isEmpty()) {
            r.setLevel(LEVEL_HIGH);
            r.setMatchedKeywords(matched);
        }
        return r;
    }
}
