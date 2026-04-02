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
 * 大模型接入配置
 */
@Data
@TableName("model_config")
public class ModelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 厂商标识：deepseek / qwen / kimi 等 */
    private String provider;
    private String apiBase;
    private String modelName;
    /** 密文存储的 API Key */
    private String apiKeyCipher;
    private Integer enabled;
    private Integer priority;
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
