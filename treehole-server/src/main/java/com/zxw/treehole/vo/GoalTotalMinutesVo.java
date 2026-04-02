package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "某目标累计学习时长（分钟）")
public class GoalTotalMinutesVo {

    private Long goalId;
    private Long totalMinutes;
}
