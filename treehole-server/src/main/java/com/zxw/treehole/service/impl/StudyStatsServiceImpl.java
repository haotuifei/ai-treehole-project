package com.zxw.treehole.service.impl;

import com.zxw.treehole.common.ResultCode;
import com.zxw.treehole.entity.StudyGoal;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.StudyCheckinMapper;
import com.zxw.treehole.mapper.StudyGoalMapper;
import com.zxw.treehole.mapper.dto.DailyMinutesRow;
import com.zxw.treehole.service.StudyStatsService;
import com.zxw.treehole.vo.GoalTotalMinutesVo;
import com.zxw.treehole.vo.MonthCheckinDaysVo;
import com.zxw.treehole.vo.StudyTrend7dVo;
import com.zxw.treehole.vo.TrendPointVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudyStatsServiceImpl implements StudyStatsService {

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    private final StudyCheckinMapper studyCheckinMapper;
    private final StudyGoalMapper studyGoalMapper;

    @Override
    public GoalTotalMinutesVo totalMinutesForGoal(Long userId, Long goalId) {
        requireOwnGoal(userId, goalId);
        Long sum = studyCheckinMapper.sumDurationMinutesByGoalAndUser(goalId, userId);
        if (sum == null) {
            sum = 0L;
        }
        return new GoalTotalMinutesVo(goalId, sum);
    }

    @Override
    public MonthCheckinDaysVo monthCheckinDays(Long userId, String yearMonth, Long goalId) {
        if (goalId != null) {
            requireOwnGoal(userId, goalId);
        }
        String ym = parseYearMonth(yearMonth);
        Long days = studyCheckinMapper.countDistinctCheckDatesForMonth(userId, ym, goalId);
        if (days == null) {
            days = 0L;
        }
        return new MonthCheckinDaysVo(ym, goalId, days);
    }

    @Override
    public StudyTrend7dVo last7DaysTrend(Long userId, Long goalId) {
        if (goalId != null) {
            requireOwnGoal(userId, goalId);
        }
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        List<DailyMinutesRow> rows = studyCheckinMapper.listDailyMinutesBetween(userId, goalId, start, end);
        Map<LocalDate, Long> map = new HashMap<>();
        for (DailyMinutesRow r : rows) {
            if (r.getCheckDate() != null && r.getTotalMinutes() != null) {
                map.put(r.getCheckDate(), r.getTotalMinutes());
            }
        }
        List<TrendPointVo> points = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            points.add(new TrendPointVo(d, map.getOrDefault(d, 0L)));
        }
        StudyTrend7dVo vo = new StudyTrend7dVo();
        vo.setGoalId(goalId);
        vo.setPoints(points);
        return vo;
    }

    private StudyGoal requireOwnGoal(Long userId, Long goalId) {
        StudyGoal g = studyGoalMapper.selectById(goalId);
        if (g == null || (g.getDeleted() != null && g.getDeleted() == 1)) {
            throw new BusinessException("目标不存在");
        }
        if (!userId.equals(g.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看该备考目标统计");
        }
        return g;
    }

    private static String parseYearMonth(String yearMonth) {
        try {
            YearMonth.parse(yearMonth, YM);
            return yearMonth;
        } catch (DateTimeParseException e) {
            throw new BusinessException("yearMonth 格式须为 yyyy-MM，例如 2026-04");
        }
    }
}
