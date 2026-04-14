package com.zxw.treehole.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxw.treehole.entity.EmotionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface EmotionRecordMapper extends BaseMapper<EmotionRecord> {

    @Select("SELECT AVG(sentiment_score) FROM emotion_record WHERE deleted = 0 AND user_id = #{userId} AND create_time >= #{startTime} AND create_time < #{endTime}")
    BigDecimal selectAvgScoreByUserIdAndTimeRange(@Param("userId") Long userId,
                                                    @Param("startTime") String startTime,
                                                    @Param("endTime") String endTime);

    @Select("SELECT COUNT(*) FROM emotion_record WHERE deleted = 0 AND user_id = #{userId} AND create_time >= #{startTime} AND create_time < #{endTime}")
    Integer selectCountByUserIdAndTimeRange(@Param("userId") Long userId,
                                             @Param("startTime") String startTime,
                                             @Param("endTime") String endTime);

    @Select("SELECT COUNT(*) FROM emotion_record WHERE deleted = 0 AND user_id = #{userId} AND risk_level = 'HIGH' AND create_time >= #{startTime} AND create_time < #{endTime}")
    Integer selectHighRiskCountByUserIdAndTimeRange(@Param("userId") Long userId,
                                                     @Param("startTime") String startTime,
                                                     @Param("endTime") String endTime);

    @Select("SELECT emotion_label, COUNT(*) as cnt FROM emotion_record WHERE deleted = 0 AND user_id = #{userId} AND create_time >= #{startTime} AND create_time < #{endTime} GROUP BY emotion_label")
    List<Map<String, Object>> selectEmotionLabelDistribution(@Param("userId") Long userId,
                                                               @Param("startTime") String startTime,
                                                               @Param("endTime") String endTime);

    @Select("SELECT risk_level, COUNT(*) as cnt FROM emotion_record WHERE deleted = 0 AND user_id = #{userId} AND create_time >= #{startTime} AND create_time < #{endTime} GROUP BY risk_level")
    List<Map<String, Object>> selectRiskLevelDistribution(@Param("userId") Long userId,
                                                            @Param("startTime") String startTime,
                                                            @Param("endTime") String endTime);

    @Select("SELECT DATE(create_time) as date, AVG(sentiment_score) as score FROM emotion_record " +
            "WHERE deleted = 0 AND user_id = #{userId} AND create_time >= #{startTime} AND create_time < #{endTime} " +
            "GROUP BY DATE(create_time) ORDER BY date ASC")
    List<Map<String, Object>> selectDailyAvgScore(@Param("userId") Long userId,
                                                   @Param("startTime") String startTime,
                                                   @Param("endTime") String endTime);

    @Select("SELECT DATE(create_time) as date, COUNT(*) as cnt FROM emotion_record " +
            "WHERE deleted = 0 AND user_id = #{userId} AND create_time >= #{startTime} AND create_time < #{endTime} " +
            "GROUP BY DATE(create_time) ORDER BY date ASC")
    List<Map<String, Object>> selectDailyChatCount(@Param("userId") Long userId,
                                                     @Param("startTime") String startTime,
                                                     @Param("endTime") String endTime);
}
