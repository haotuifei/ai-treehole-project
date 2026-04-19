package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "树洞流式对话请求")
public class ChatStreamRequest {

    @Schema(description = "会话 ID，不传则创建新会话")
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 8000, message = "单条消息不超过 8000 字")
    private String content;

    @Schema(description = "个性化系统提示词")
    @Size(max = 4000, message = "个性化设置过长")
    private String customSystemPrompt;
}
