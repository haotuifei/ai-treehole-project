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
        String apiKey = props.getEffectiveApiKey();
        String apiBase = props.getEffectiveApiBase();
        String model = props.getEffectiveModel();

        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("未配置 API Key，无法调用大模型");
        }
        String base = apiBase.replaceAll("/+$", "");
        String endpoint = props.getApiEndpoint();
        String url = base + endpoint;

        boolean isAnthropic = base.contains("anthropic");

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
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
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(props.getReadTimeoutMs()))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        if (isAnthropic) {
            reqBuilder.header("anthropic-version", "2023-06-01");
        }
        HttpResponse<java.io.InputStream> resp = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            String err = new String(resp.body().readAllBytes(), StandardCharsets.UTF_8);
            log.warn("LLM HTTP {} : {}", resp.statusCode(), err);
            throw new IllegalStateException("大模型请求失败 HTTP " + resp.statusCode());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resp.body(), StandardCharsets.UTF_8))) {
            if (isAnthropic) {
                parseAnthropicStream(reader, onChunk);
            } else {
                parseOpenAiStream(reader, onChunk);
            }
        }
    }

    private void parseOpenAiStream(BufferedReader reader, LlmChunkConsumer onChunk) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) continue;
            if (!line.startsWith("data:")) continue;
            String payload = line.substring(5).trim();
            if ("[DONE]".equals(payload)) break;
            JsonNode node = objectMapper.readTree(payload);
            JsonNode choices = node.path("choices");
            if (!choices.isArray() || choices.isEmpty()) continue;
            JsonNode delta = choices.get(0).path("delta");
            if (delta.has("content") && !delta.get("content").isNull()) {
                String piece = delta.get("content").asText("");
                if (!piece.isEmpty()) onChunk.accept(piece);
            }
        }
    }

    private void parseAnthropicStream(BufferedReader reader, LlmChunkConsumer onChunk) throws Exception {
        String eventName = "";
        StringBuilder dataBuf = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                if (dataBuf.length() > 0) {
                    String data = dataBuf.toString();
                    if ("[DONE]".equals(data)) break;
                    if ("event".equals(eventName)) {
                        // event: 行的数据部分
                    } else if (dataBuf.length() > 0) {
                        try {
                            JsonNode node = objectMapper.readTree(data);
                            if ("content_block_delta".equals(node.path("type").asText())) {
                                JsonNode delta = node.path("delta");
                                if (delta.has("text")) {
                                    String text = delta.get("text").asText();
                                    if (!text.isEmpty()) onChunk.accept(text);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                    dataBuf.setLength(0);
                }
                eventName = "";
                continue;
            }
            if (line.startsWith("event:")) {
                eventName = line.substring(6).trim();
            } else if (line.startsWith("data:")) {
                dataBuf.append(line.substring(5).trim());
            }
        }
    }
}
