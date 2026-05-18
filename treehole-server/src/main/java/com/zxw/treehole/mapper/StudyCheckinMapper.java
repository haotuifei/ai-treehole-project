package com.zxw.treehole.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxw.treehole.entity.StudyCheckin;
import com.zxw.treehole.mapper.dto.CalendarDayRow;
import com.zxw.treehole.mapper.dto.DailyMinutesRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StudyCheckinMapper extends BaseMapper<StudyCheckin> {

    Long sumDurationMinutesByGoalAndUser(@Param("goalId") Long goalId, @Param("userId") Long userId);

    Long countDistinctCheckDatesForMonth(@Param("userId") Long userId,
                                         @Param("yearMonth") String yearMonth,
                                         @Param("goalId") Long goalId);

    List<DailyMinutesRow> listDailyMinutesBetween(@Param("userId") Long userId,
                                                  @Param("goalId") Long goalId,
                                                  @Param("start") LocalDate start,
                                                  @Param("end") LocalDate end);

    /** 查询用户从指定日期起连续打卡的所有日期（降序） */
    List<LocalDate> listCheckDatesFrom(@Param("userId") Long userId, @Param("from") LocalDate from);

    /** 查询用户在指定日期之前的所有打卡日期（降序，用于计算最长连续） */
    List<LocalDate> listAllCheckDates(@Param("userId") Long userId);

    /** 查询月历数据：某月内每天的打卡详情 */
    List<CalendarDayRow> listMonthCalendar(@Param("userId") Long userId,
                                           @Param("yearMonth") String yearMonth);

    /** 统计某目标下累计打卡天数 */
    Long countCheckinDaysByGoal(@Param("goalId") Long goalId, @Param("userId") Long userId);
}
