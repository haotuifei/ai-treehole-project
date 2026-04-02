package com.zxw.treehole.vo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AdminWarningStatsVo {

    private Long total;
    private Long newLast7Days;
    /** 各状态数量 */
    private Map<String, Long> statusCounts = new LinkedHashMap<>();
}
