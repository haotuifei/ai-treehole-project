package com.zxw.treehole.service.impl;

import com.zxw.treehole.dto.EmotionStatsQuery;
import com.zxw.treehole.mapper.EmotionRecordMapper;
import com.zxw.treehole.service.EmotionStatsService;
import com.zxw.treehole.vo.EmotionStatsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmotionStatsServiceImpl implements EmotionStatsService {

    private final EmotionRecordMapper emotionRecordMapper;

    @Override
    public EmotionStatsVo getStats(Long userId, EmotionStatsQuery query) {
        String period = query.getPeriod();
        String[] range = calculateDateRange(period);
        String startTime = range[0];
        String endTime = range[1];

        EmotionStatsVo vo = new EmotionStatsVo();

        // 情绪均分
        BigDecimal avgScore = emotionRecordMapper.selectAvgScoreByUserIdAndTimeRange(userId, startTime, endTime);
        if (avgScore != null) {
            // 换算成 0-100 分制（假设原始是 -1~1）
            avgScore = avgScore.multiply(BigDecimal.valueOf(50)).add(BigDecimal.valueOf(50));
            avgScore = avgScore.setScale(1, RoundingMode.HALF_UP);
        } else {
            avgScore = BigDecimal.ZERO;
        }
        vo.setAvgScore(avgScore);

        // 对话次数
        Integer chatCount = emotionRecordMapper.selectCountByUserIdAndTimeRange(userId, startTime, endTime);
        vo.setChatCount(chatCount != null ? chatCount : 0);

        // 高风险次数
        Integer highRiskCount = emotionRecordMapper.selectHighRiskCountByUserIdAndTimeRange(userId, startTime, endTime);
        vo.setHighRiskCount(highRiskCount != null ? highRiskCount : 0);

        // 情绪标签分布
        Map<String, Integer> emotionDist = new LinkedHashMap<>();
        List<Map<String, Object>> emotionData = emotionRecordMapper.selectEmotionLabelDistribution(userId, startTime, endTime);
        for (Map<String, Object> row : emotionData) {
            String label = row.get("emotion_label") != null ? row.get("emotion_label").toString() : "未知";
            int cnt = ((Number) row.get("cnt")).intValue();
            emotionDist.put(label, cnt);
        }
        vo.setEmotionLabelDistribution(emotionDist);

        // 风险等级分布
        Map<String, Integer> riskDist = new LinkedHashMap<>();
        List<Map<String, Object>> riskData = emotionRecordMapper.selectRiskLevelDistribution(userId, startTime, endTime);
        for (Map<String, Object> row : riskData) {
            String level = row.get("risk_level") != null ? row.get("risk_level").toString() : "UNKNOWN";
            int cnt = ((Number) row.get("cnt")).intValue();
            riskDist.put(level, cnt);
        }
        vo.setRiskLevelDistribution(riskDist);

        // 每日情绪分值趋势
        List<EmotionStatsVo.DailyScore> dailyTrend = new ArrayList<>();
        List<Map<String, Object>> trendData = emotionRecordMapper.selectDailyAvgScore(userId, startTime, endTime);
        for (Map<String, Object> row : trendData) {
            EmotionStatsVo.DailyScore ds = new EmotionStatsVo.DailyScore();
            Object dateObj = row.get("date");
            if (dateObj instanceof java.sql.Date) {
                ds.setDate(dateObj.toString());
            } else {
                ds.setDate(dateObj != null ? dateObj.toString() : "");
            }
            Object scoreObj = row.get("score");
            if (scoreObj != null) {
                BigDecimal score = new BigDecimal(scoreObj.toString());
                score = score.multiply(BigDecimal.valueOf(50)).add(BigDecimal.valueOf(50));
                ds.setScore(score.setScale(1, RoundingMode.HALF_UP));
            } else {
                ds.setScore(BigDecimal.ZERO);
            }
            dailyTrend.add(ds);
        }
        vo.setDailyTrend(dailyTrend);

        // 每日对话次数
        List<EmotionStatsVo.DailyCount> dailyCount = new ArrayList<>();
        List<Map<String, Object>> countData = emotionRecordMapper.selectDailyChatCount(userId, startTime, endTime);
        for (Map<String, Object> row : countData) {
            EmotionStatsVo.DailyCount dc = new EmotionStatsVo.DailyCount();
            Object dateObj = row.get("date");
            if (dateObj instanceof java.sql.Date) {
                dc.setDate(dateObj.toString());
            } else {
                dc.setDate(dateObj != null ? dateObj.toString() : "");
            }
            Object cntObj = row.get("cnt");
            dc.setCount(cntObj != null ? ((Number) cntObj).intValue() : 0);
            dailyCount.add(dc);
        }
        vo.setDailyChatCount(dailyCount);

        // 生成改善建议
        vo.setSuggestions(generateSuggestions(vo, period));

        return vo;
    }

    private String[] calculateDateRange(String period) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String startTime;
        String endTime;

        switch (period) {
            case "day":
                startTime = today.atStartOfDay().format(fmt);
                endTime = today.plusDays(1).atStartOfDay().format(fmt);
                break;
            case "week":
                LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                startTime = weekStart.atStartOfDay().format(fmt);
                endTime = today.plusDays(1).atStartOfDay().format(fmt);
                break;
            case "month":
            default:
                LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
                startTime = monthStart.atStartOfDay().format(fmt);
                endTime = today.plusDays(1).atStartOfDay().format(fmt);
                break;
        }

        return new String[]{startTime, endTime};
    }

    private List<String> generateSuggestions(EmotionStatsVo vo, String period) {
        List<String> suggestions = new ArrayList<>();

        int chatCount = vo.getChatCount();
        int highRiskCount = vo.getHighRiskCount();
        BigDecimal avgScore = vo.getAvgScore();

        if (chatCount == 0) {
            suggestions.add("还没有和树洞聊天记录，今天想说点什么吗？");
            return suggestions;
        }

        // 情绪均分建议（假设 0-100 分制，<40 为低，>70 为高）
        if (avgScore.compareTo(BigDecimal.valueOf(40)) < 0) {
            suggestions.add("最近情绪得分偏低，建议多和朋友、家人交流，或者考虑找辅导员聊聊。");
        } else if (avgScore.compareTo(BigDecimal.valueOf(70)) > 0) {
            suggestions.add("情绪状态良好！继续保持规律作息和适度运动。");
        }

        // 高风险次数建议
        if ("week".equals(period) && highRiskCount >= 3) {
            suggestions.add("本周高风险表达较多，建议关注自身身心健康，可预约心理咨询。");
        } else if ("month".equals(period) && highRiskCount >= 5) {
            suggestions.add("本月高风险表达次数较多，建议和辅导员或心理咨询师沟通。");
        }

        // 情绪波动建议
        List<EmotionStatsVo.DailyScore> trend = vo.getDailyTrend();
        if (trend != null && trend.size() >= 3) {
            boolean rising = true;
            boolean falling = true;
            for (int i = 1; i < trend.size(); i++) {
                BigDecimal prev = trend.get(i - 1).getScore();
                BigDecimal curr = trend.get(i).getScore();
                if (curr.compareTo(prev) <= 0) rising = false;
                if (curr.compareTo(prev) >= 0) falling = false;
            }
            if (rising) {
                suggestions.add("情绪分值呈上升趋势，但注意不要给自己太大压力，适当放松很重要。");
            } else if (falling) {
                suggestions.add("情绪分值持续下降，建议安排一些让自己开心的活动，必要时寻求支持。");
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("继续保持，现在的状态很不错！有空可以和树洞多聊聊。");
        }

        return suggestions;
    }
}
