package com.zxw.treehole.service;

import com.zxw.treehole.dto.AlertRuleSnapshot;
import com.zxw.treehole.dto.AlertRuleUpdateRequest;
import com.zxw.treehole.vo.AlertRuleVo;

public interface AlertRuleService {

    AlertRuleVo getDefaultRule();

    void updateDefaultRule(AlertRuleUpdateRequest request);

    /** 情绪流水线使用：库中无记录时返回内置默认 */
    AlertRuleSnapshot activeThresholds();
}
