package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI 消息")
public class AiChatMessageVo {

    private Long id;
    /** user / assistant */
    private String role;
    private String content;
    private LocalDateTime createTime;
}
