package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.ResultCode;
import com.zxw.treehole.dto.CheckinCreateRequest;
import com.zxw.treehole.dto.CheckinPageQuery;
import com.zxw.treehole.dto.CheckinUpdateRequest;
import com.zxw.treehole.entity.StudyCheckin;
import com.zxw.treehole.entity.StudyGoal;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.StudyCheckinMapper;
import com.zxw.treehole.mapper.StudyGoalMapper;
import com.zxw.treehole.mapper.dto.CalendarDayRow;
import com.zxw.treehole.service.StudyCheckinService;
import com.zxw.treehole.vo.CheckinCalendarVo;
import com.zxw.treehole.vo.CheckinStreakVo;
import com.zxw.treehole.vo.StudyCheckinVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudyCheckinServiceImpl implements StudyCheckinService {

    private final StudyCheckinMapper studyCheckinMapper;
    private final StudyGoalMapper studyGoalMapper;

    @Override
    public PageResult<StudyCheckinVo> pageMyCheckins(Long userId, CheckinPageQuery query) {
        requireOwnGoal(userId, query.getGoalId());
        LambdaQueryWrapper<StudyCheckin> w = new LambdaQueryWrapper<StudyCheckin>()
                .eq(StudyCheckin::getUserId, userId)
                .eq(StudyCheckin::getGoalId, query.getGoalId())
                .eq(StudyCheckin::getDeleted, 0)
                .orderByDesc(StudyCheckin::getCheckDate)
                .orderByDesc(StudyCheckin::getId);
        if (query.getDateFrom() != null) {
            w.ge(StudyCheckin::getCheckDate, query.getDateFrom());
        }
        if (query.getDateTo() != null) {
            w.le(StudyCheckin::getCheckDate, query.getDateTo());
        }
        if (query.getDateFrom() != null && query.getDateTo() != null && query.getDateFrom().isAfter(query.getDateTo())) {
            throw new BusinessException("日期范围不合法");
        }
        Page<StudyCheckin> page = studyCheckinMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), w);
        Page<StudyCheckinVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVo).toList());
        return PageResult.of(voPage);
    }

    @Override
    public StudyCheckinVo getMyCheckin(Long userId, Long checkinId) {
        return toVo(requireOwnCheckin(userId, checkinId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCheckin(Long userId, CheckinCreateRequest request) {
        requireOwnGoal(userId, request.getGoalId());
        assertNoDuplicateOnDate(userId, request.getGoalId(), request.getCheckDate(), null);
        StudyCheckin c = new StudyCheckin();
        c.setUserId(userId);
        c.setGoalId(request.getGoalId());
        c.setCheckDate(request.getCheckDate());
        c.setDurationMinutes(request.getDurationMinutes());
        c.setContent(request.getContent());
        c.setPomodoroDone(request.getPomodoroDone());
        c.setMood(request.getMood());
        c.setRemark(request.getRemark());
        studyCheckinMapper.insert(c);
        return c.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheckin(Long userId, Long checkinId, CheckinUpdateRequest request) {
        StudyCheckin c = requireOwnCheckin(userId, checkinId);
        requireOwnGoal(userId, c.getGoalId());
        if (request.getCheckDate() != null && !request.getCheckDate().equals(c.getCheckDate())) {
            assertNoDuplicateOnDate(userId, c.getGoalId(), request.getCheckDate(), checkinId);
            c.setCheckDate(request.getCheckDate());
        }
        if (request.getDurationMinutes() != null) {
            c.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getContent() != null) {
            c.setContent(request.getContent());
        }
        if (request.getPomodoroDone() != null) {
            c.setPomodoroDone(request.getPomodoroDone());
        }
        if (request.getMood() != null) {
            c.setMood(request.getMood());
        }
        if (request.getRemark() != null) {
            c.setRemark(request.getRemark());
        }
        studyCheckinMapper.updateById(c);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCheckin(Long userId, Long checkinId) {
        requireOwnCheckin(userId, checkinId);
        studyCheckinMapper.deleteById(checkinId);
    }

    private StudyGoal requireOwnGoal(Long userId, Long goalId) {
        StudyGoal g = studyGoalMapper.selectById(goalId);
        if (g == null || (g.getDeleted() != null && g.getDeleted() == 1)) {
            throw new BusinessException("目标不存在");
        }
        if (!userId.equals(g.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该备考目标");
        }
        return g;
    }

    private StudyCheckin requireOwnCheckin(Long userId, Long checkinId) {
        StudyCheckin c = studyCheckinMapper.selectById(checkinId);
        if (c == null || (c.getDeleted() != null && c.getDeleted() == 1)) {
            throw new BusinessException("打卡记录不存在");
        }
        if (!userId.equals(c.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该打卡记录");
        }
        return c;
    }

    private void assertNoDuplicateOnDate(Long userId, Long goalId, java.time.LocalDate date, Long excludeId) {
        LambdaQueryWrapper<StudyCheckin> w = new LambdaQueryWrapper<StudyCheckin>()
                .eq(StudyCheckin::getUserId, userId)
                .eq(StudyCheckin::getGoalId, goalId)
                .eq(StudyCheckin::getCheckDate, date)
                .eq(StudyCheckin::getDeleted, 0);
        if (excludeId != null) {
            w.ne(StudyCheckin::getId, excludeId);
        }
        Long cnt = studyCheckinMapper.selectCount(w);
        if (cnt != null && cnt > 0) {
            throw new BusinessException("该目标在当日已有打卡记录，请修改原记录或更换日期");
        }
    }

    private StudyCheckinVo toVo(StudyCheckin c) {
        StudyCheckinVo v = new StudyCheckinVo();
        v.setId(c.getId());
        v.setGoalId(c.getGoalId());
        v.setCheckDate(c.getCheckDate());
        v.setDurationMinutes(c.getDurationMinutes());
        v.setContent(c.getContent());
        v.setPomodoroDone(c.getPomodoroDone());
        v.setMood(c.getMood());
        v.setRemark(c.getRemark());
        v.setCreateTime(c.getCreateTime());
        v.setUpdateTime(c.getUpdateTime());
        return v;
    }

    @Override
    public CheckinStreakVo getStreakStats(Long userId) {
        CheckinStreakVo vo = new CheckinStreakVo();
        List<LocalDate> allDates = studyCheckinMapper.listAllCheckDates(userId);
        if (allDates.isEmpty()) {
            return vo;
        }

        LocalDate today = LocalDate.now();
        boolean checkedToday = allDates.contains(today);
        vo.setCheckedToday(checkedToday);

        // 计算当前连续天数：从今天（或昨天，如果今天没打卡）往前数
        LocalDate cursor = checkedToday ? today : today.minusDays(1);
        int currentStreak = 0;
        for (LocalDate d : allDates) {
            if (d.equals(cursor)) {
                currentStreak++;
                cursor = cursor.minusDays(1);
            } else if (d.isBefore(cursor)) {
                break;
            }
        }
        vo.setCurrentStreak(currentStreak);

        // 计算历史最长连续天数
        int longest = 0;
        int streak = 1;
        for (int i = 1; i < allDates.size(); i++) {
            if (allDates.get(i - 1).minusDays(1).equals(allDates.get(i))) {
                streak++;
            } else {
                longest = Math.max(longest, streak);
                streak = 1;
            }
        }
        longest = Math.max(longest, streak);
        vo.setLongestStreak(longest);

        // 本月打卡天数
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Long monthDays = studyCheckinMapper.countDistinctCheckDatesForMonth(userId, yearMonth, null);
        vo.setMonthDays(monthDays != null ? monthDays.intValue() : 0);

        return vo;
    }

    @Override
    public CheckinCalendarVo getMonthCalendar(Long userId, String yearMonth) {
        CheckinCalendarVo vo = new CheckinCalendarVo();

        // 月历打卡数据
        List<CalendarDayRow> rows = studyCheckinMapper.listMonthCalendar(userId, yearMonth);
        Map<String, CheckinCalendarVo.DayInfo> days = new LinkedHashMap<>();
        for (CalendarDayRow row : rows) {
            String key = row.getCheckDate().toString();
            days.put(key, new CheckinCalendarVo.DayInfo(
                    true,
                    row.getTotalMinutes() != null ? row.getTotalMinutes().intValue() : 0,
                    row.getMood(),
                    row.getCheckinId()
            ));
        }
        vo.setDays(days);

        // 进行中的目标列表
        List<StudyGoal> goals = studyGoalMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StudyGoal>()
                        .eq(StudyGoal::getUserId, userId)
                        .eq(StudyGoal::getStatus, 0)
                        .eq(StudyGoal::getDeleted, 0));
        List<CheckinCalendarVo.GoalInfo> goalInfos = new ArrayList<>();
        for (StudyGoal g : goals) {
            CheckinCalendarVo.GoalInfo gi = new CheckinCalendarVo.GoalInfo();
            gi.setGoalId(g.getId());
            gi.setGoalName(g.getGoalName());
            gi.setGoalType(g.getGoalType());
            gi.setStartDate(g.getStartDate());
            gi.setEndDate(g.getEndDate());
            Long totalCheckins = studyCheckinMapper.countCheckinDaysByGoal(g.getId(), userId);
            gi.setTotalCheckins(totalCheckins != null ? totalCheckins : 0);
            Long totalMinutes = studyCheckinMapper.sumDurationMinutesByGoalAndUser(g.getId(), userId);
            gi.setTotalMinutes(totalMinutes != null ? totalMinutes : 0);
            goalInfos.add(gi);
        }
        vo.setGoals(goalInfos);

        return vo;
    }
}
