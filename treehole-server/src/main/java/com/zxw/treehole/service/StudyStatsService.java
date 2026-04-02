package com.zxw.treehole.service;

import com.zxw.treehole.vo.GoalTotalMinutesVo;
import com.zxw.treehole.vo.MonthCheckinDaysVo;
import com.zxw.treehole.vo.StudyTrend7dVo;

public interface StudyStatsService {

    GoalTotalMinutesVo totalMinutesForGoal(Long userId, Long goalId);

    MonthCheckinDaysVo monthCheckinDays(Long userId, String yearMonth, Long goalId);

    StudyTrend7dVo last7DaysTrend(Long userId, Long goalId);
}
