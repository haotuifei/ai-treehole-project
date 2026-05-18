package com.zxw.treehole.mapper.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 月历单日打卡数据（Mapper 查询结果）
 */
@Data
public class CalendarDayRow {

    private LocalDate checkDate;
    private Long totalMinutes;
    private String mood;
    private Long checkinId;
}
