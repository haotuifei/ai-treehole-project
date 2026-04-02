package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建空会话（可选标题）")
public class ChatSessionCreateRequest {

    @Size(max = 255)
    private String title;
}
