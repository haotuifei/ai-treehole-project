package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.ResultCode;
import com.zxw.treehole.dto.CounselorStudentPageQuery;
import com.zxw.treehole.entity.*;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.*;
import com.zxw.treehole.service.CounselorStudentService;
import com.zxw.treehole.vo.EmotionRecordBriefVo;
import com.zxw.treehole.vo.StudentDetailVo;
import com.zxw.treehole.vo.StudentProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorStudentServiceImpl implements CounselorStudentService {

    private final SysUserMapper sysUserMapper;
    private final EmotionRecordMapper emotionRecordMapper;
    private final WarningRecordMapper warningRecordMapper;
    private final StudyGoalMapper studyGoalMapper;
    private final StudyCheckinMapper studyCheckinMapper;

    private String getCounselorClassName(Long counselorUserId) {
        SysUser counselor = sysUserMapper.selectById(counselorUserId);
        return counselor != null ? counselor.getClassName() : null;
    }

    @Override
    public PageResult<StudentProfileVo> pageStudents(Long counselorUserId, CounselorStudentPageQuery query) {
        String className = getCounselorClassName(counselorUserId);
        if (className == null || className.isBlank()) {
            Page<SysUser> empty = new Page<>(query.getPageNum(), query.getPageSize(), 0);
            empty.setRecords(List.of());
            Page<StudentProfileVo> voEmpty = new Page<>(empty.getCurrent(), empty.getSize(), empty.getTotal());
            voEmpty.setRecords(List.of());
            return PageResult.of(voEmpty);
        }

        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getClassName, className)
                .eq(SysUser::getDeleted, 0)
                .inSql(SysUser::getId,
                        "SELECT user_id FROM sys_user_role WHERE role_id = (SELECT id FROM sys_role WHERE role_code = 'STUDENT')");
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            qw.and(w -> w.like(SysUser::getRealName, kw)
                    .or().like(SysUser::getUsername, kw)
                    .or().like(SysUser::getStudentNo, kw));
        }
        qw.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = sysUserMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), qw);

        Page<StudentProfileVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toProfileVo).toList());
        return PageResult.of(voPage);
    }

    @Override
    public StudentDetailVo getStudentDetail(Long counselorUserId, Long studentId) {
        SysUser student = sysUserMapper.selectById(studentId);
        if (student == null || (student.getDeleted() != null && student.getDeleted() == 1)) {
            throw new BusinessException("学生不存在");
        }
        String className = getCounselorClassName(counselorUserId);
        if (student.getClassName() == null || !student.getClassName().equals(className)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看该学生档案");
        }

        StudentDetailVo vo = new StudentDetailVo();
        // 基本信息
        vo.setId(student.getId());
        vo.setUsername(student.getUsername());
        vo.setRealName(student.getRealName());
        vo.setStudentNo(student.getStudentNo());
        vo.setClassName(student.getClassName());
        vo.setPhone(student.getPhone());
        vo.setEmail(student.getEmail());
        vo.setStatus(student.getStatus());
        vo.setCreateTime(student.getCreateTime());

        Long sid = student.getId();

        // 情绪统计
        fillEmotionStats(vo, sid);

        // 预警统计
        fillWarningStats(vo, sid);

        // 学习统计
        fillStudyStats(vo, sid);

        return vo;
    }

    private StudentProfileVo toProfileVo(SysUser u) {
        StudentProfileVo vo = new StudentProfileVo();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setRealName(u.getRealName());
        vo.setStudentNo(u.getStudentNo());
        vo.setClassName(u.getClassName());
        vo.setPhone(u.getPhone());
        vo.setEmail(u.getEmail());
        vo.setStatus(u.getStatus());
        vo.setCreateTime(u.getCreateTime());

        Long sid = u.getId();

        // 预警数
        vo.setWarningCount(warningRecordMapper.selectCount(
                new LambdaQueryWrapper<WarningRecord>()
                        .eq(WarningRecord::getUserId, sid)
                        .eq(WarningRecord::getDeleted, 0)));

        // 情绪记录数
        vo.setEmotionCount(emotionRecordMapper.selectCount(
                new LambdaQueryWrapper<EmotionRecord>()
                        .eq(EmotionRecord::getUserId, sid)
                        .eq(EmotionRecord::getDeleted, 0)));

        // 最近一条情绪记录
        EmotionRecord last = emotionRecordMapper.selectOne(
                new LambdaQueryWrapper<EmotionRecord>()
                        .eq(EmotionRecord::getUserId, sid)
                        .eq(EmotionRecord::getDeleted, 0)
                        .orderByDesc(EmotionRecord::getCreateTime)
                        .last("LIMIT 1"));
        if (last != null) {
            vo.setLastEmotionScore(last.getSentimentScore());
            vo.setLastEmotionTime(last.getCreateTime());
        }

        return vo;
    }

    private void fillEmotionStats(StudentDetailVo vo, Long userId) {
        // 总数
        Long total = emotionRecordMapper.selectCount(
                new LambdaQueryWrapper<EmotionRecord>()
                        .eq(EmotionRecord::getUserId, userId)
                        .eq(EmotionRecord::getDeleted, 0));
        vo.setEmotionTotalCount(total);

        if (total == 0) {
            vo.setAvgEmotionScore(null);
            vo.setHighRiskCount(0L);
            vo.setRiskLevelDistribution(Map.of());
            vo.setRecentEmotions(List.of());
            return;
        }

        // 均分
        BigDecimal avg = emotionRecordMapper.selectAvgScoreByUserIdAndTimeRange(
                userId, "2000-01-01", "2099-12-31");
        vo.setAvgEmotionScore(avg);

        // 高风险数
        Integer highCount = emotionRecordMapper.selectHighRiskCountByUserIdAndTimeRange(
                userId, "2000-01-01", "2099-12-31");
        vo.setHighRiskCount(highCount != null ? highCount.longValue() : 0L);

        // 风险分布
        List<Map<String, Object>> riskRows = emotionRecordMapper.selectRiskLevelDistribution(
                userId, "2000-01-01", "2099-12-31");
        Map<String, Long> riskDist = new LinkedHashMap<>();
        riskDist.put("LOW", 0L);
        riskDist.put("MEDIUM", 0L);
        riskDist.put("HIGH", 0L);
        for (Map<String, Object> row : riskRows) {
            String level = (String) row.get("risk_level");
            Long cnt = ((Number) row.get("cnt")).longValue();
            riskDist.put(level, cnt);
        }
        vo.setRiskLevelDistribution(riskDist);

        // 最近10条情绪记录
        List<EmotionRecord> recent = emotionRecordMapper.selectList(
                new LambdaQueryWrapper<EmotionRecord>()
                        .eq(EmotionRecord::getUserId, userId)
                        .eq(EmotionRecord::getDeleted, 0)
                        .orderByDesc(EmotionRecord::getCreateTime)
                        .last("LIMIT 10"));
        vo.setRecentEmotions(recent.stream().map(e -> {
            EmotionRecordBriefVo eb = new EmotionRecordBriefVo();
            eb.setId(e.getId());
            eb.setSentimentScore(e.getSentimentScore());
            eb.setEmotionLabel(e.getEmotionLabel());
            eb.setRiskLevel(e.getRiskLevel());
            eb.setAnalysisDetail(e.getAnalysisDetail());
            eb.setCreateTime(e.getCreateTime());
            return eb;
        }).toList());
    }

    private void fillWarningStats(StudentDetailVo vo, Long userId) {
        Long total = warningRecordMapper.selectCount(
                new LambdaQueryWrapper<WarningRecord>()
                        .eq(WarningRecord::getUserId, userId)
                        .eq(WarningRecord::getDeleted, 0));
        vo.setWarningTotalCount(total);

        // 状态分布
        String[] statuses = {"PENDING", "PROCESSING", "RESOLVED", "CLOSED"};
        Map<String, Long> statusDist = new LinkedHashMap<>();
        for (String st : statuses) {
            Long cnt = warningRecordMapper.selectCount(
                    new LambdaQueryWrapper<WarningRecord>()
                            .eq(WarningRecord::getUserId, userId)
                            .eq(WarningRecord::getDeleted, 0)
                            .eq(WarningRecord::getStatus, st));
            statusDist.put(st, cnt);
        }
        vo.setWarningStatusDistribution(statusDist);
    }

    private void fillStudyStats(StudentDetailVo vo, Long userId) {
        // 目标统计
        Long goalTotal = studyGoalMapper.selectCount(
                new LambdaQueryWrapper<StudyGoal>()
                        .eq(StudyGoal::getUserId, userId)
                        .eq(StudyGoal::getDeleted, 0));
        vo.setGoalTotalCount(goalTotal);

        Long goalActive = studyGoalMapper.selectCount(
                new LambdaQueryWrapper<StudyGoal>()
                        .eq(StudyGoal::getUserId, userId)
                        .eq(StudyGoal::getDeleted, 0)
                        .eq(StudyGoal::getStatus, 0));
        vo.setGoalActiveCount(goalActive);

        // 累计打卡天数（去重日期）
        List<LocalDate> allDates = studyCheckinMapper.listAllCheckDates(userId);
        vo.setCheckinTotalDays((long) allDates.size());

        // 最近7天打卡天数
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(6);
        long last7 = allDates.stream().filter(d -> !d.isBefore(sevenDaysAgo)).count();
        vo.setCheckinLast7Days(last7);
    }
}
