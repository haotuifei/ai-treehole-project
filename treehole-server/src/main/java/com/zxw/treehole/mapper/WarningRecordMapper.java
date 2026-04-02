package com.zxw.treehole.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxw.treehole.entity.WarningRecord;
import com.zxw.treehole.mapper.dto.WarningStatusCountRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WarningRecordMapper extends BaseMapper<WarningRecord> {

    @Select("SELECT COUNT(*) FROM warning_record WHERE deleted = 0")
    Long countTotalActive();

    @Select("SELECT COUNT(*) FROM warning_record WHERE deleted = 0 AND create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    Long countLast7Days();

    @Select("SELECT `status` AS status, COUNT(*) AS cnt FROM warning_record WHERE deleted = 0 GROUP BY `status`")
    List<WarningStatusCountRow> countGroupByStatus();
}
