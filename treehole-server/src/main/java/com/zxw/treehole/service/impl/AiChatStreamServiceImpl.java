package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.ai.PersonaPromptLoader;
import com.zxw.treehole.ai.SafetyCopyLoader;
import com.zxw.treehole.ai.client.ChatLanguageModel;
import com.zxw.treehole.ai.client.LlmMessage;
import com.zxw.treehole.service.EmotionWarningOutcome;
import com.zxw.treehole.service.EmotionWarningService;
import com.zxw.treehole.common.ResultCode;
import com.zxw.treehole.config.AiChatProperties;
import com.zxw.treehole.dto.ChatStreamRequest;
import com.zxw.treehole.entity.AiChatMessage;
import com.zxw.treehole.entity.AiChatSession;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.AiChatMessageMapper;
import com.zxw.treehole.mapper.AiChatSessionMapper;
import com.zxw.treehole.service.AiChatStreamService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatStreamServiceImpl implements AiChatStreamService {

    private static final String RISK_HIGH = "HIGH";
    private static final Pattern GOAL_SUGGEST_PATTERN = Pattern.compile("\\[GOAL_SUGGEST:(.+?):(.+?)\\]");

    /** 用户消息中的目标关键词 → (目标名称, 目标类型) */
    private static final List<Map.Entry<String, String[]>> GOAL_KEYWORDS = List.of(
            Map.entry("考研", new String[]{"考研备考", "POSTGRAD"}),
            Map.entry("研究生", new String[]{"考研备考", "POSTGRAD"}),
            Map.entry("考公", new String[]{"考公备考", "CIVIL_SERVICE"}),
            Map.entry("公务员", new String[]{"考公备考", "CIVIL_SERVICE"}),
            Map.entry("国考", new String[]{"国考备考", "CIVIL_SERVICE"}),
            Map.entry("省考", new String[]{"省考备考", "CIVIL_SERVICE"}),
            Map.entry("考编", new String[]{"考编备考", "CIVIL_SERVICE"}),
            Map.entry("四六级", new String[]{"英语四六级", "COURSE"}),
            Map.entry("四级", new String[]{"英语四级", "COURSE"}),
            Map.entry("六级", new String[]{"英语六级", "COURSE"}),
            Map.entry("雅思", new String[]{"雅思备考", "COURSE"}),
            Map.entry("托福", new String[]{"托福备考", "COURSE"}),
            Map.entry("驾照", new String[]{"考驾照", "CUSTOM"}),
            Map.entry("学车", new String[]{"考驾照", "CUSTOM"}),
            Map.entry("教资", new String[]{"教师资格证", "COURSE"}),
            Map.entry("教师资格证", new String[]{"教师资格证", "COURSE"}),
            Map.entry("计算机二级", new String[]{"计算机二级", "COURSE"}),
            Map.entry("注会", new String[]{"注册会计师", "COURSE"}),
            Map.entry("法考", new String[]{"法律职业资格考试", "COURSE"}),
            Map.entry("考研政治", new String[]{"考研政治", "POSTGRAD"}),
            Map.entry("考研英语", new String[]{"考研英语", "POSTGRAD"}),
            Map.entry("考研数学", new String[]{"考研数学", "POSTGRAD"})
    );

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final ChatLanguageModel chatLanguageModel;
    private final PersonaPromptLoader personaPromptLoader;
    private final EmotionWarningService emotionWarningService;
    private final SafetyCopyLoader safetyCopyLoader;
    private final AiChatProperties aiChatProperties;

    @Resource(name = "chatExecutor")
    private Executor chatExecutor;

    @Override
    public void startStreamAsync(Long userId, ChatStreamRequest request, SseEmitter emitter) {
        chatExecutor.execute(() -> {
            try {
                runStream(userId, request, emitter);
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE 流异常 userId={}", userId, e);
                try {
                    String msg = e.getMessage() != null ? e.getMessage() : "对话失败";
                    emitter.send(SseEmitter.event().name("error").data(msg, MediaType.TEXT_PLAIN));
                } catch (Exception ignored) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });
    }

    private void runStream(Long userId, ChatStreamRequest request, SseEmitter emitter) throws Exception {
        AiChatSession session = resolveSession(userId, request.getSessionId());
        String content = request.getContent().trim();

        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setMessageRole("user");
        userMsg.setContent(content);
        messageMapper.insert(userMsg);

        EmotionWarningOutcome outcome = emotionWarningService.analyzeAndPersist(userId, session.getId(), userMsg.getId(), content);
        sendMeta(emitter, session.getId(), userMsg.getId(), outcome);

        if (RISK_HIGH.equals(outcome.getRiskLevel())) {
            String blockSource = "CRISIS_KEYWORD".equals(outcome.getEmotionLabel()) ? "keyword" : "emotion";
            emitter.send(SseEmitter.event().name("blocked").data(Map.of(
                    "riskLevel", RISK_HIGH,
                    "source", blockSource
            )));
            String safe = safetyCopyLoader.loadHighRiskSafeReply();
            streamPlainText(emitter, safe);
            Long assistantId = saveAssistantMessage(session.getId(), safe);
            touchSession(session, content);
            sendEnd(emitter, assistantId);
            return;
        }

        String systemPrompt = personaPromptLoader.loadSystemPrompt();
        if (!StringUtils.hasText(systemPrompt)) {
            systemPrompt = "你是温和、有共情的树洞陪伴者，不做医疗诊断。";
        }
        if (StringUtils.hasText(request.getCustomSystemPrompt())) {
            systemPrompt = systemPrompt + "\n\n【当前用户的个性化偏好】\n" + request.getCustomSystemPrompt().trim();
        }

        List<LlmMessage> messages = buildLlmMessages(session.getId(), systemPrompt);
        StringBuilder full = new StringBuilder();
        chatLanguageModel.streamChat(messages, chunk -> {
            if (chunk == null || chunk.isEmpty()) {
                return;
            }
            full.append(chunk);
            emitter.send(SseEmitter.event().name("delta").data(chunk, MediaType.TEXT_PLAIN));
        });

        String reply = full.toString();
        if (!StringUtils.hasText(reply)) {
            reply = "我在呢，刚才没有组织好语言，可以再说一点点你现在的心情吗？";
        }

        // 解析目标建议：先尝试 LLM 标记，再用关键词检测
        String goalName = null;
        String goalType = null;
        Matcher goalMatcher = GOAL_SUGGEST_PATTERN.matcher(reply);
        if (goalMatcher.find()) {
            goalName = goalMatcher.group(1).trim();
            goalType = goalMatcher.group(2).trim();
            reply = goalMatcher.replaceAll("").trim();
            log.debug("从 AI 回复中检测到目标标记: name={}, type={}", goalName, goalType);
        } else {
            // LLM 未输出标记时，用关键词检测用户消息
            String[] detected = detectGoalFromContent(content);
            if (detected != null) {
                goalName = detected[0];
                goalType = detected[1];
                log.debug("从用户消息中检测到目标关键词: name={}, type={}", goalName, goalType);
            }
        }

        Long assistantId = saveAssistantMessage(session.getId(), reply);
        touchSession(session, content);

        // 发送目标建议事件
        if (goalName != null) {
            Map<String, String> goalSuggest = new LinkedHashMap<>();
            goalSuggest.put("goalName", goalName);
            goalSuggest.put("goalType", goalType);
            emitter.send(SseEmitter.event().name("goal_suggest")
                    .data(goalSuggest, MediaType.APPLICATION_JSON));
        }

        sendEnd(emitter, assistantId);
    }

    private AiChatSession resolveSession(Long userId, Long sessionId) {
        if (sessionId == null) {
            AiChatSession s = new AiChatSession();
            s.setUserId(userId);
            s.setTitle("新对话");
            sessionMapper.insert(s);
            return s;
        }
        AiChatSession s = sessionMapper.selectById(sessionId);
        if (s == null || (s.getDeleted() != null && s.getDeleted() == 1)) {
            throw new BusinessException("会话不存在");
        }
        if (!userId.equals(s.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权在该会话中发言");
        }
        return s;
    }

    private List<LlmMessage> buildLlmMessages(Long sessionId, String systemPrompt) {
        int limit = Math.max(4, aiChatProperties.getMaxContextMessages());
        List<AiChatMessage> rows = messageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .eq(AiChatMessage::getDeleted, 0)
                        .in(AiChatMessage::getMessageRole, "user", "assistant")
                        .orderByDesc(AiChatMessage::getId)
                        .last("LIMIT " + limit));
        Collections.reverse(rows);
        List<LlmMessage> out = new ArrayList<>();
        out.add(new LlmMessage("system", systemPrompt));
        for (AiChatMessage m : rows) {
            out.add(new LlmMessage(m.getMessageRole(), m.getContent() == null ? "" : m.getContent()));
        }
        return out;
    }

    private void touchSession(AiChatSession session, String lastUserContent) {
        session.setLastMessageAt(LocalDateTime.now());
        if (!StringUtils.hasText(session.getTitle()) || "新对话".equals(session.getTitle())) {
            String t = lastUserContent.length() > 36 ? lastUserContent.substring(0, 36) + "…" : lastUserContent;
            session.setTitle(t);
        }
        sessionMapper.updateById(session);
    }

    private Long saveAssistantMessage(Long sessionId, String content) {
        AiChatMessage m = new AiChatMessage();
        m.setSessionId(sessionId);
        m.setMessageRole("assistant");
        m.setContent(content);
        messageMapper.insert(m);
        return m.getId();
    }

    private void streamPlainText(SseEmitter emitter, String text) throws Exception {
        for (int i = 0; i < text.length(); i++) {
            String c = String.valueOf(text.charAt(i));
            emitter.send(SseEmitter.event().name("delta").data(c, MediaType.TEXT_PLAIN));
            if (i % 40 == 0) {
                Thread.sleep(5);
            }
        }
    }

    private void sendMeta(SseEmitter emitter, Long sessionId, Long userMessageId, EmotionWarningOutcome o) throws IOException {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("sessionId", sessionId);
        meta.put("userMessageId", userMessageId);
        meta.put("riskLevel", o.getRiskLevel());
        meta.put("emotionLabel", o.getEmotionLabel());
        meta.put("sentimentScore", o.getSentimentScore());
        meta.put("emotionRecordId", o.getEmotionRecordId());
        meta.put("warningRecordId", o.getWarningRecordId());
        meta.put("analyzedAt", o.getAnalyzedAt());
        emitter.send(SseEmitter.event().name("meta").data(meta, MediaType.APPLICATION_JSON));
    }

    private void sendEnd(SseEmitter emitter, Long assistantMessageId) throws IOException {
        Map<String, Object> end = new LinkedHashMap<>();
        end.put("assistantMessageId", assistantMessageId);
        emitter.send(SseEmitter.event().name("end").data(end, MediaType.APPLICATION_JSON));
    }

    /**
     * 从用户消息中检测目标关键词，返回 [目标名称, 目标类型]，未检测到返回 null
     */
    private String[] detectGoalFromContent(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String lower = content.toLowerCase();
        for (Map.Entry<String, String[]> entry : GOAL_KEYWORDS) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
