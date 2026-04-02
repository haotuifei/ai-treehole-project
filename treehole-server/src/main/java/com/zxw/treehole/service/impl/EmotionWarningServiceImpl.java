package com.zxw.treehole.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zxw.treehole.ai.risk.RiskAssessmentResult;
import com.zxw.treehole.ai.risk.RiskAssessmentService;
import com.zxw.treehole.emotion.LexiconEmotionAnalyzer;
import com.zxw.treehole.emotion.LexiconEmotionResult;
import com.zxw.treehole.dto.AlertRuleSnapshot;
import com.zxw.treehole.entity.EmotionRecord;
import com.zxw.treehole.entity.WarningRecord;
import com.zxw.treehole.mapper.EmotionRecordMapper;
import com.zxw.treehole.mapper.WarningRecordMapper;
import com.zxw.treehole.service.AlertRuleService;
import com.zxw.treehole.service.EmotionWarningOutcome;
import com.zxw.treehole.service.EmotionWarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionWarningServiceImpl implements EmotionWarningService {

    private static final String LOW = "LOW";
    private static final String MEDIUM = "MEDIUM";
    private static final String HIGH = "HIGH";
    private static final String LABEL_CRISIS = "CRISIS_KEYWORD";

    private final LexiconEmotionAnalyzer lexiconEmotionAnalyzer;
    private final RiskAssessmentService riskAssessmentService;
    private final AlertRuleService alertRuleService;
    private final EmotionRecordMapper emotionRecordMapper;
    private final WarningRecordMapper warningRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public EmotionWarningOutcome analyzeAndPersist(Long userId, Long sessionId, Long messageId, String text) {
        LocalDateTime analyzedAt = LocalDateTime.now();
        AlertRuleSnapshot rule = alertRuleService.activeThresholds();
        LexiconEmotionResult lex = lexiconEmotionAnalyzer.analyze(text);
        RiskAssessmentResult kw = riskAssessmentService.assess(text);
        boolean keywordHigh = HIGH.equalsIgnoreCase(kw.getLevel());
        boolean kwEnabled = rule.keywordHighEnabled();

        BigDecimal score = lex.getSentimentScore();
        BigDecimal highLine = rule.highSentimentThreshold();
        BigDecimal mediumLine = rule.mediumSentimentThreshold();

        boolean emotionHigh = score.compareTo(highLine) <= 0;
        boolean emotionMedium = !emotionHigh && score.compareTo(mediumLine) <= 0;

        String riskFromEmotion = emotionHigh ? HIGH : (emotionMedium ? MEDIUM : LOW);
        String finalRisk;
        if (kwEnabled && keywordHigh) {
            finalRisk = HIGH;
        } else if (HIGH.equals(riskFromEmotion)) {
            finalRisk = HIGH;
        } else if (MEDIUM.equals(riskFromEmotion)) {
            finalRisk = MEDIUM;
        } else {
            finalRisk = LOW;
        }

        String trigger;
        if (kwEnabled && keywordHigh && emotionHigh) {
            trigger = "BOTH";
        } else if (kwEnabled && keywordHigh) {
            trigger = "KEYWORD";
        } else if (HIGH.equals(finalRisk)) {
            trigger = "EMOTION";
        } else {
            trigger = null;
        }

        String emotionLabel = (kwEnabled && keywordHigh) ? LABEL_CRISIS : lex.getEmotionLabel();

        String detailJson;
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("finalRisk", finalRisk);
            root.put("riskFromEmotion", riskFromEmotion);
            root.put("keywordHigh", keywordHigh);
            root.set("lexiconHits", objectMapper.valueToTree(lex.getHitCounts()));
            root.set("matchedKeywords", objectMapper.valueToTree(kw.getMatchedKeywords()));
            root.put("mediumThreshold", mediumLine);
            root.put("highThreshold", highLine);
            if (trigger != null) {
                root.put("triggerSource", trigger);
            }
            detailJson = objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            detailJson = "{}";
        }

        EmotionRecord er = new EmotionRecord();
        er.setUserId(userId);
        er.setSessionId(sessionId);
        er.setMessageId(messageId);
        er.setSentimentScore(score);
        er.setEmotionLabel(emotionLabel);
        er.setRiskLevel(finalRisk);
        er.setAnalysisDetail(detailJson);
        emotionRecordMapper.insert(er);

        Long warningId = null;
        if (HIGH.equals(finalRisk)) {
            WarningRecord wr = new WarningRecord();
            wr.setUserId(userId);
            wr.setSessionId(sessionId);
            wr.setEmotionRecordId(er.getId());
            wr.setTextSummary(summarize(text));
            wr.setTriggerSource(trigger != null ? trigger : "EMOTION");
            wr.setRiskLevel(HIGH);
            wr.setStatus("PENDING");
            warningRecordMapper.insert(wr);
            warningId = wr.getId();
            log.warn("已生成高风险预警 warningId={} userId={} sessionId={} trigger={}", warningId, userId, sessionId, wr.getTriggerSource());
        }

        EmotionWarningOutcome out = new EmotionWarningOutcome();
        out.setRiskLevel(finalRisk);
        out.setEmotionLabel(emotionLabel);
        out.setSentimentScore(score);
        out.setEmotionRecordId(er.getId());
        out.setWarningRecordId(warningId);
        out.setAnalyzedAt(analyzedAt);
        return out;
    }

    private static String summarize(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String t = text.replaceAll("\\s+", " ").trim();
        return t.length() > 200 ? t.substring(0, 200) + "…" : t;
    }
}
