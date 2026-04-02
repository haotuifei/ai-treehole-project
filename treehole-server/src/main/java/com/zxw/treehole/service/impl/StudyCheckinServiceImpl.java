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
import com.zxw.treehole.service.StudyCheckinService;
import com.zxw.treehole.vo.StudyCheckinVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
