package com.zxw.treehole.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志（审计）
 */
@Data
@TableName("system_log")
public class SystemLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String module;
    private String operation;
    private String method;
    private String requestUri;
    private String requestMethod;
    private String ip;
    private String userAgent;
    /** 0 失败 1 成功 */
    private Integer status;
    private String errorMsg;
    private Integer durationMs;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
