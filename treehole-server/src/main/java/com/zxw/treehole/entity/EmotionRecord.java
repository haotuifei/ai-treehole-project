package com.zxw.treehole.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 情绪分析记录
 */
@Data
@TableName("emotion_record")
public class EmotionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long sessionId;
    private Long messageId;
    /** 情绪分值，如 -1 ~ 1 或 0~100，按算法定义 */
    private BigDecimal sentimentScore;
    private String emotionLabel;
    /** LOW / MEDIUM / HIGH */
    private String riskLevel;
    /** 原始分析 JSON 或说明 */
    private String analysisDetail;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
