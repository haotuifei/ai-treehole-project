package com.zxw.treehole.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 备考目标
 */
@Data
@TableName("study_goal")
public class StudyGoal {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;

    @TableField("goal_name")
    private String goalName;

    /** POSTGRAD / CIVIL_SERVICE / COURSE / CUSTOM */
    private String goalType;

    private LocalDate startDate;
    private LocalDate endDate;
    /** 总目标描述 */
    private String description;

    /** 0进行中 1已完成 2已暂停 3已放弃 */
    private Integer status;

    private String remark;
    private Integer sortOrder;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
