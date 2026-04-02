package com.zxw.treehole.service.impl;

import com.zxw.treehole.mapper.WarningRecordMapper;
import com.zxw.treehole.mapper.dto.WarningStatusCountRow;
import com.zxw.treehole.service.AdminWarningStatsService;
import com.zxw.treehole.vo.AdminWarningStatsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminWarningStatsServiceImpl implements AdminWarningStatsService {

    private final WarningRecordMapper warningRecordMapper;

    @Override
    public AdminWarningStatsVo globalStats() {
        AdminWarningStatsVo vo = new AdminWarningStatsVo();
        Long total = warningRecordMapper.countTotalActive();
        vo.setTotal(total != null ? total : 0L);
        Long d7 = warningRecordMapper.countLast7Days();
        vo.setNewLast7Days(d7 != null ? d7 : 0L);
        List<WarningStatusCountRow> rows = warningRecordMapper.countGroupByStatus();
        if (rows != null) {
            for (WarningStatusCountRow r : rows) {
                if (r.getStatus() != null && r.getCnt() != null) {
                    vo.getStatusCounts().put(r.getStatus(), r.getCnt());
                }
            }
        }
        return vo;
    }
}
