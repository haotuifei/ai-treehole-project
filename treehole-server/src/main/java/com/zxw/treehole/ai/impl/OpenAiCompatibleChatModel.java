package com.zxw.treehole.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zxw.treehole.ai.client.ChatLanguageModel;
import com.zxw.treehole.ai.client.LlmMessage;
import com.zxw.treehole.config.AiChatProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * OpenAI 兼容 Chat Completions 流式接口（DeepSeek / 多数国产兼容模式可用）
 */
@Slf4j
@Component("openAiCompatibleChatModelBean")
@RequiredArgsConstructor
public class OpenAiCompatibleChatModel implements ChatLanguageModel {

    private final AiChatProperties props;
    private final ObjectMapper objectMapper;

    @Override
    public void streamChat(List<LlmMessage> messages, LlmChunkConsumer onChunk) throws Exception {
        if (!StringUtils.hasText(props.getApiKey())) {
            throw new IllegalStateException("未配置 app.ai.api-key，无法调用大模型");
        }
        String base = props.getApiBase().replaceAll("/+$", "");
        String url = base + "/v1/chat/completions";

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", props.getModel());
        root.put("stream", true);
        ArrayNode arr = root.putArray("messages");
        for (LlmMessage m : messages) {
            ObjectNode o = objectMapper.createObjectNode();
            o.put("role", m.getRole());
            o.put("content", m.getContent() == null ? "" : m.getContent());
            arr.add(o);
        }

        String body = objectMapper.writeValueAsString(root);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getConnectTimeoutMs()))
                .build();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(props.getReadTimeoutMs()))
                .header("Authorization", "Bearer " + props.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<java.io.InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            String err = new String(resp.body().readAllBytes(), StandardCharsets.UTF_8);
            log.warn("LLM HTTP {} : {}", resp.statusCode(), err);
            throw new IllegalStateException("大模型请求失败 HTTP " + resp.statusCode());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resp.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (!line.startsWith("data:")) {
                    continue;
                }
                String payload = line.substring(5).trim();
                if ("[DONE]".equals(payload)) {
                    break;
                }
                JsonNode node = objectMapper.readTree(payload);
                JsonNode choices = node.path("choices");
                if (!choices.isArray() || choices.isEmpty()) {
                    continue;
                }
                JsonNode delta = choices.get(0).path("delta");
                if (delta.has("content") && !delta.get("content").isNull()) {
                    String piece = delta.get("content").asText("");
                    if (!piece.isEmpty()) {
                        onChunk.accept(piece);
                    }
                }
            }
        }
    }
}
