package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "月历打卡数据")
public class CheckinCalendarVo {

    @Schema(description = "日期 → 每日打卡信息")
    private Map<String, DayInfo> days;

    @Schema(description = "进行中的目标列表（含起止日期）")
    private List<GoalInfo> goals;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "单日打卡信息")
    public static class DayInfo {
        private boolean checked;
        private Integer durationMinutes;
        private String mood;
        private Long checkinId;
    }

    @Data
    @Schema(description = "目标概要（用于日历标记）")
    public static class GoalInfo {
        private Long goalId;
        private String goalName;
        private String goalType;
        private LocalDate startDate;
        private LocalDate endDate;
        private long totalCheckins;
        private long totalMinutes;
    }
}
