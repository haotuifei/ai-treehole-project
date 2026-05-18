package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "连续打卡统计")
public class CheckinStreakVo {

    @Schema(description = "当前连续打卡天数")
    private int currentStreak;

    @Schema(description = "历史最长连续天数")
    private int longestStreak;

    @Schema(description = "本月已打卡天数")
    private int monthDays;

    @Schema(description = "今日是否已打卡")
    private boolean checkedToday;
}
