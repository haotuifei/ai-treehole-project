package com.zxw.treehole.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话消息（user / assistant / system）
 */
@Data
@TableName("ai_chat_message")
public class AiChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    /** user / assistant / system */
    @TableField("message_role")
    private String messageRole;
    private String content;
    private Integer tokenEstimate;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
