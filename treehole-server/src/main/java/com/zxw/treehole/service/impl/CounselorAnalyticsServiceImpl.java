package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.entity.*;
import com.zxw.treehole.mapper.*;
import com.zxw.treehole.service.CounselorAnalyticsService;
import com.zxw.treehole.vo.ClassAnalyticsVo;
import com.zxw.treehole.vo.ClassAnalyticsVo.DailyCount;
import com.zxw.treehole.vo.ClassAnalyticsVo.StudentRankItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorAnalyticsServiceImpl implements CounselorAnalyticsService {

    private final SysUserMapper sysUserMapper;
    private final EmotionRecordMapper emotionRecordMapper;
    private final WarningRecordMapper warningRecordMapper;
    private final StudyCheckinMapper studyCheckinMapper;

    @Override
    public ClassAnalyticsVo getClassAnalytics(Long counselorUserId) {
        ClassAnalyticsVo vo = new ClassAnalyticsVo();

        // 查询辅导员的班级
        SysUser counselor = sysUserMapper.selectById(counselorUserId);
        String className = counselor != null ? counselor.getClassName() : null;

        // 查询管辖学生（仅学生角色）
        List<SysUser> students = (className == null || className.isBlank()) ? List.of()
                : sysUserMapper.selectList(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getClassName, className)
                                .eq(SysUser::getDeleted, 0)
                                .inSql(SysUser::getId,
                                        "SELECT user_id FROM sys_user_role WHERE role_id = (SELECT id FROM sys_role WHERE role_code = 'STUDENT')"));

        vo.setTotalStudents((long) students.size());

        if (students.isEmpty()) {
            fillEmpty(vo);
            return vo;
        }

        List<Long> studentIds = students.stream().map(SysUser::getId).toList();
        Map<Long, SysUser> studentMap = students.stream()
                .collect(Collectors.toMap(SysUser::getId, s -> s, (a, b) -> a));

        // === 情绪统计 ===
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // 近7天有情绪记录的学生数
        List<EmotionRecord> recentEmotions = emotionRecordMapper.selectList(
                new LambdaQueryWrapper<EmotionRecord>()
                        .eq(EmotionRecord::getDeleted, 0)
                        .in(EmotionRecord::getUserId, studentIds)
                        .ge(EmotionRecord::getCreateTime, sevenDaysAgo));
        long activeStudents = recentEmotions.stream()
                .map(EmotionRecord::getUserId).distinct().count();
        vo.setActiveStudents(activeStudents);

        // 全部情绪记录（用于统计）
        List<EmotionRecord> allEmotions = emotionRecordMapper.selectList(
                new LambdaQueryWrapper<EmotionRecord>()
                        .eq(EmotionRecord::getDeleted, 0)
                        .in(EmotionRecord::getUserId, studentIds));

        // 平均情绪分值
        BigDecimal avg = allEmotions.stream()
                .filter(e -> e.getSentimentScore() != null)
                .map(EmotionRecord::getSentimentScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!allEmotions.isEmpty()) {
            vo.setAvgEmotionScore(avg.divide(BigDecimal.valueOf(allEmotions.size()), 4, RoundingMode.HALF_UP));
        } else {
            vo.setAvgEmotionScore(null);
        }

        // 风险分布
        Map<String, Long> riskDist = allEmotions.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getRiskLevel() != null ? e.getRiskLevel() : "LOW",
                        LinkedHashMap::new, Collectors.counting()));
        riskDist.putIfAbsent("LOW", 0L);
        riskDist.putIfAbsent("MEDIUM", 0L);
        riskDist.putIfAbsent("HIGH", 0L);
        vo.setRiskDistribution(riskDist);

        // === 预警统计 ===
        List<WarningRecord> allWarnings = warningRecordMapper.selectList(
                new LambdaQueryWrapper<WarningRecord>()
                        .eq(WarningRecord::getDeleted, 0)
                        .in(WarningRecord::getUserId, studentIds));
        vo.setTotalWarnings((long) allWarnings.size());
        vo.setPendingWarnings(allWarnings.stream()
                .filter(w -> "PENDING".equals(w.getStatus())).count());

        // 近7天每日预警数
        vo.setWarningTrend(buildDailyCountForWarnings(allWarnings, 7));

        // === 打卡统计 ===
        // 累计打卡天次（去重学生+日期）
        long totalCheckins = 0;
        for (Long sid : studentIds) {
            List<LocalDate> dates = studyCheckinMapper.listAllCheckDates(sid);
            totalCheckins += dates.size();
        }
        vo.setTotalCheckins(totalCheckins);

        // 近7天每日打卡数
        vo.setCheckinTrend(buildDailyCheckinTrend(studentIds, 7));

        // === 学生情绪排行 ===
        vo.setStudentRanking(buildStudentRanking(students, allEmotions, allWarnings));

        return vo;
    }

    private void fillEmpty(ClassAnalyticsVo vo) {
        vo.setActiveStudents(0L);
        vo.setAvgEmotionScore(null);
        vo.setRiskDistribution(Map.of("LOW", 0L, "MEDIUM", 0L, "HIGH", 0L));
        vo.setTotalWarnings(0L);
        vo.setPendingWarnings(0L);
        vo.setTotalCheckins(0L);
        vo.setWarningTrend(buildEmptyTrend(7));
        vo.setCheckinTrend(buildEmptyTrend(7));
        vo.setStudentRanking(List.of());
    }

    private List<DailyCount> buildDailyCountForWarnings(List<WarningRecord> warnings, int days) {
        LocalDate today = LocalDate.now();
        Map<String, Long> countMap = warnings.stream()
                .filter(w -> w.getCreateTime() != null && w.getCreateTime().isAfter(today.minusDays(days).atStartOfDay()))
                .collect(Collectors.groupingBy(
                        w -> w.getCreateTime().toLocalDate().toString(),
                        Collectors.counting()));

        List<DailyCount> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            result.add(new DailyCount(date, countMap.getOrDefault(date, 0L)));
        }
        return result;
    }

    private List<DailyCount> buildDailyCheckinTrend(List<Long> studentIds, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        // 查询所有学生的近N天打卡
        Map<String, Long> countMap = new HashMap<>();
        for (Long sid : studentIds) {
            List<LocalDate> dates = studyCheckinMapper.listCheckDatesFrom(sid, startDate);
            for (LocalDate d : dates) {
                countMap.merge(d.toString(), 1L, Long::sum);
            }
        }

        List<DailyCount> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            result.add(new DailyCount(date, countMap.getOrDefault(date, 0L)));
        }
        return result;
    }

    private List<DailyCount> buildEmptyTrend(int days) {
        LocalDate today = LocalDate.now();
        List<DailyCount> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            result.add(new DailyCount(today.minusDays(i).toString(), 0L));
        }
        return result;
    }

    private List<StudentRankItem> buildStudentRanking(List<SysUser> students,
                                                       List<EmotionRecord> allEmotions,
                                                       List<WarningRecord> allWarnings) {
        // 按学生分组情绪记录
        Map<Long, List<EmotionRecord>> emotionByUser = allEmotions.stream()
                .collect(Collectors.groupingBy(EmotionRecord::getUserId));

        // 按学生分组预警记录
        Map<Long, List<WarningRecord>> warningByUser = allWarnings.stream()
                .collect(Collectors.groupingBy(WarningRecord::getUserId));

        List<StudentRankItem> ranking = new ArrayList<>();
        for (SysUser st : students) {
            StudentRankItem item = new StudentRankItem();
            item.setUserId(st.getId());
            item.setRealName(st.getRealName());
            item.setClassName(st.getClassName());

            List<EmotionRecord> userEmotions = emotionByUser.getOrDefault(st.getId(), List.of());
            if (!userEmotions.isEmpty()) {
                BigDecimal sum = userEmotions.stream()
                        .filter(e -> e.getSentimentScore() != null)
                        .map(EmotionRecord::getSentimentScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                long count = userEmotions.stream()
                        .filter(e -> e.getSentimentScore() != null).count();
                if (count > 0) {
                    item.setAvgScore(sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP));
                }
                item.setHighRiskCount(userEmotions.stream()
                        .filter(e -> "HIGH".equals(e.getRiskLevel())).count());
            } else {
                item.setAvgScore(null);
                item.setHighRiskCount(0L);
            }

            List<WarningRecord> userWarnings = warningByUser.getOrDefault(st.getId(), List.of());
            item.setWarningCount((long) userWarnings.size());

            ranking.add(item);
        }

        // 排序：有高风险的优先，然后按均分升序（分值越低情绪越差）
        ranking.sort((a, b) -> {
            if (!Objects.equals(a.getHighRiskCount(), b.getHighRiskCount())) {
                return Long.compare(b.getHighRiskCount(), a.getHighRiskCount());
            }
            BigDecimal sa = a.getAvgScore() != null ? a.getAvgScore() : BigDecimal.ZERO;
            BigDecimal sb = b.getAvgScore() != null ? b.getAvgScore() : BigDecimal.ZERO;
            return sa.compareTo(sb);
        });

        return ranking;
    }
}
