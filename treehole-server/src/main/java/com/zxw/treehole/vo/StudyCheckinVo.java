package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "学习打卡")
public class StudyCheckinVo {

    private Long id;
    private Long goalId;
    private LocalDate checkDate;
    private Integer durationMinutes;
    private String content;
    private Integer pomodoroDone;
    private String mood;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
