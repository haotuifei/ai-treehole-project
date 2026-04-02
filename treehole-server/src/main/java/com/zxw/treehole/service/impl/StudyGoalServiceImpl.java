package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.common.ResultCode;
import com.zxw.treehole.common.enums.GoalStatus;
import com.zxw.treehole.common.enums.GoalType;
import com.zxw.treehole.dto.GoalCreateRequest;
import com.zxw.treehole.dto.GoalUpdateRequest;
import com.zxw.treehole.entity.StudyGoal;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.StudyGoalMapper;
import com.zxw.treehole.service.StudyGoalService;
import com.zxw.treehole.vo.StudyGoalVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyGoalServiceImpl implements StudyGoalService {

    private final StudyGoalMapper studyGoalMapper;

    @Override
    public List<StudyGoalVo> listMyGoals(Long userId, Integer status) {
        LambdaQueryWrapper<StudyGoal> w = new LambdaQueryWrapper<StudyGoal>()
                .eq(StudyGoal::getUserId, userId)
                .eq(StudyGoal::getDeleted, 0)
                .orderByAsc(StudyGoal::getSortOrder)
                .orderByDesc(StudyGoal::getId);
        if (status != null) {
            w.eq(StudyGoal::getStatus, status);
        }
        return studyGoalMapper.selectList(w).stream().map(this::toVo).toList();
    }

    @Override
    public StudyGoalVo getMyGoal(Long userId, Long goalId) {
        return toVo(requireOwnGoal(userId, goalId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGoal(Long userId, GoalCreateRequest request) {
        String type = GoalType.normalize(request.getGoalType());
        if (type == null) {
            throw new BusinessException("无效的目标类型，可选：" + Arrays.toString(Arrays.stream(GoalType.values()).map(GoalType::getCode).toArray()));
        }
        if (!GoalStatus.isValid(request.getStatus())) {
            throw new BusinessException("无效的目标状态");
        }
        validateGoalDates(request.getStartDate(), request.getEndDate());
        StudyGoal g = new StudyGoal();
        g.setUserId(userId);
        g.setGoalName(request.getGoalName().trim());
        g.setGoalType(type);
        g.setStartDate(request.getStartDate());
        g.setEndDate(request.getEndDate());
        g.setDescription(request.getDescription());
        g.setStatus(request.getStatus());
        g.setRemark(request.getRemark());
        g.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        studyGoalMapper.insert(g);
        return g.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoal(Long userId, Long goalId, GoalUpdateRequest request) {
        StudyGoal g = requireOwnGoal(userId, goalId);
        if (StringUtils.hasText(request.getGoalName())) {
            g.setGoalName(request.getGoalName().trim());
        }
        if (StringUtils.hasText(request.getGoalType())) {
            String type = GoalType.normalize(request.getGoalType());
            if (type == null) {
                throw new BusinessException("无效的目标类型");
            }
            g.setGoalType(type);
        }
        if (request.getStartDate() != null) {
            g.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            g.setEndDate(request.getEndDate());
        }
        if (request.getDescription() != null) {
            g.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            if (!GoalStatus.isValid(request.getStatus())) {
                throw new BusinessException("无效的目标状态");
            }
            g.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            g.setRemark(request.getRemark());
        }
        if (request.getSortOrder() != null) {
            g.setSortOrder(request.getSortOrder());
        }
        validateGoalDates(g.getStartDate(), g.getEndDate());
        studyGoalMapper.updateById(g);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoal(Long userId, Long goalId) {
        requireOwnGoal(userId, goalId);
        studyGoalMapper.deleteById(goalId);
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

    private static void validateGoalDates(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException("开始日期不能晚于截止日期");
        }
    }

    private StudyGoalVo toVo(StudyGoal g) {
        StudyGoalVo v = new StudyGoalVo();
        v.setId(g.getId());
        v.setGoalName(g.getGoalName());
        v.setGoalType(g.getGoalType());
        v.setGoalTypeLabel(resolveTypeLabel(g.getGoalType()));
        v.setStartDate(g.getStartDate());
        v.setEndDate(g.getEndDate());
        v.setDescription(g.getDescription());
        v.setStatus(g.getStatus());
        v.setStatusLabel(resolveStatusLabel(g.getStatus()));
        v.setRemark(g.getRemark());
        v.setSortOrder(g.getSortOrder());
        v.setCreateTime(g.getCreateTime());
        v.setUpdateTime(g.getUpdateTime());
        return v;
    }

    private static String resolveTypeLabel(String code) {
        if (code == null) {
            return "";
        }
        return Arrays.stream(GoalType.values())
                .filter(t -> t.getCode().equalsIgnoreCase(code))
                .map(GoalType::getLabel)
                .findFirst()
                .orElse(code);
    }

    private static String resolveStatusLabel(Integer status) {
        if (status == null) {
            return "";
        }
        return Arrays.stream(GoalStatus.values())
                .filter(s -> s.getCode() == status)
                .map(GoalStatus::getLabel)
                .findFirst()
                .orElse(String.valueOf(status));
    }
}
