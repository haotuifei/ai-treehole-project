package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "备考目标")
public class StudyGoalVo {

    private Long id;
    private String goalName;
    private String goalType;
    private String goalTypeLabel;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Integer status;
    private String statusLabel;
    private String remark;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
