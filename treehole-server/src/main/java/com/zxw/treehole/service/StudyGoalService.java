package com.zxw.treehole.service;

import com.zxw.treehole.dto.GoalCreateRequest;
import com.zxw.treehole.dto.GoalUpdateRequest;
import com.zxw.treehole.vo.StudyGoalVo;

import java.util.List;

public interface StudyGoalService {

    List<StudyGoalVo> listMyGoals(Long userId, Integer status);

    StudyGoalVo getMyGoal(Long userId, Long goalId);

    Long createGoal(Long userId, GoalCreateRequest request);

    void updateGoal(Long userId, Long goalId, GoalUpdateRequest request);

    void deleteGoal(Long userId, Long goalId);
}
