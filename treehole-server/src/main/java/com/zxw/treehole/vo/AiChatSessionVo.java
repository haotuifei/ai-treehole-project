package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI 会话摘要")
public class AiChatSessionVo {

    private Long id;
    private String title;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createTime;
}
