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

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatStreamServiceImpl implements AiChatStreamService {

    private static final String RISK_HIGH = "HIGH";

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
        Long assistantId = saveAssistantMessage(session.getId(), reply);
        touchSession(session, content);
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
}
