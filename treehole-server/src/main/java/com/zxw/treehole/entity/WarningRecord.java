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
 * 预警记录
 */
@Data
@TableName("warning_record")
public class WarningRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long sessionId;
    private Long emotionRecordId;
    /** 触发文本摘要 */
    private String textSummary;
    /** KEYWORD / EMOTION / BOTH */
    private String triggerSource;
    private String riskLevel;
    /** PENDING / PROCESSING / RESOLVED / CLOSED */
    private String status;
    private Long handlerUserId;
    private String handleRemark;
    private LocalDateTime handledAt;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
