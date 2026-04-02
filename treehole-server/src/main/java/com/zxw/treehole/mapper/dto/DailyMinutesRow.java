package com.zxw.treehole.mapper.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 按日聚合学习时长（Mapper 查询结果）
 */
@Data
public class DailyMinutesRow {

    private LocalDate checkDate;
    private Long totalMinutes;
}
