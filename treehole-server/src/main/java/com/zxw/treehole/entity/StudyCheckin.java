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
 * 学习打卡（归属某备考目标）
 */
@Data
@TableName("study_checkin")
public class StudyCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long goalId;

    private LocalDate checkDate;
    private String content;
    private Integer durationMinutes;
    /** 0 否 1 是 */
    private Integer pomodoroDone;
    /** 今日心情（自由文案或前端枚举值） */
    private String mood;
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
