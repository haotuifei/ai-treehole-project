package com.zxw.treehole.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxw.treehole.entity.StudyCheckin;
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
}
