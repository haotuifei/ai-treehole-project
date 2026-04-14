package com.zxw.treehole.service;

import com.zxw.treehole.dto.EmotionStatsQuery;
import com.zxw.treehole.vo.EmotionStatsVo;

public interface EmotionStatsService {

    /**
     * 获取情绪统计数据
     * @param userId 用户ID
     * @param query 查询条件（period: day/week/month）
     * @return 统计数据
     */
    EmotionStatsVo getStats(Long userId, EmotionStatsQuery query);
}
