package com.zxw.treehole.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("counselor_class")
public class CounselorClass {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long counselorUserId;
    private String className;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
