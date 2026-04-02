package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.dto.AlertRuleSnapshot;
import com.zxw.treehole.dto.AlertRuleUpdateRequest;
import com.zxw.treehole.entity.AlertRule;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.AlertRuleMapper;
import com.zxw.treehole.service.AlertRuleService;
import com.zxw.treehole.vo.AlertRuleVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AlertRuleServiceImpl implements AlertRuleService {

    public static final String CODE_DEFAULT = "DEFAULT";

    private final AlertRuleMapper alertRuleMapper;

    @Override
    public AlertRuleSnapshot activeThresholds() {
        AlertRule r = loadOrBuiltIn();
        boolean kw = r.getKeywordHighEnabled() == null || r.getKeywordHighEnabled() == 1;
        return new AlertRuleSnapshot(r.getMediumSentimentThreshold(), r.getHighSentimentThreshold(), kw);
    }

    @Override
    public AlertRuleVo getDefaultRule() {
        AlertRule r = loadOrBuiltIn();
        return toVo(r);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefaultRule(AlertRuleUpdateRequest request) {
        BigDecimal high = request.getHighSentimentThreshold();
        BigDecimal medium = request.getMediumSentimentThreshold();
        if (high.compareTo(medium) > 0) {
            throw new BusinessException("高风险阈值应小于等于中风险阈值（数值越负越严重，如高=-0.55 中=-0.25）");
        }
        AlertRule r = alertRuleMapper.selectOne(new LambdaQueryWrapper<AlertRule>()
                .eq(AlertRule::getRuleCode, CODE_DEFAULT)
                .eq(AlertRule::getDeleted, 0));
        if (r == null) {
            r = new AlertRule();
            r.setRuleCode(CODE_DEFAULT);
            r.setRemark("系统默认");
            r.setKeywordHighEnabled(request.getKeywordHighEnabled() != null && request.getKeywordHighEnabled() == 1 ? 1 : 0);
            r.setHighSentimentThreshold(high);
            r.setMediumSentimentThreshold(medium);
            alertRuleMapper.insert(r);
            return;
        }
        r.setHighSentimentThreshold(high);
        r.setMediumSentimentThreshold(medium);
        r.setKeywordHighEnabled(request.getKeywordHighEnabled() != null && request.getKeywordHighEnabled() == 1 ? 1 : 0);
        if (request.getRemark() != null) {
            r.setRemark(request.getRemark());
        }
        alertRuleMapper.updateById(r);
    }

    public AlertRule loadOrBuiltIn() {
        AlertRule r = alertRuleMapper.selectOne(new LambdaQueryWrapper<AlertRule>()
                .eq(AlertRule::getRuleCode, CODE_DEFAULT)
                .eq(AlertRule::getDeleted, 0));
        if (r != null) {
            return r;
        }
        AlertRule builtIn = new AlertRule();
        builtIn.setId(0L);
        builtIn.setRuleCode(CODE_DEFAULT);
        builtIn.setMediumSentimentThreshold(new BigDecimal("-0.250"));
        builtIn.setHighSentimentThreshold(new BigDecimal("-0.550"));
        builtIn.setKeywordHighEnabled(1);
        return builtIn;
    }

    private static AlertRuleVo toVo(AlertRule r) {
        AlertRuleVo v = new AlertRuleVo();
        v.setId(r.getId() != null && r.getId() > 0 ? r.getId() : null);
        v.setRuleCode(r.getRuleCode());
        v.setMediumSentimentThreshold(r.getMediumSentimentThreshold());
        v.setHighSentimentThreshold(r.getHighSentimentThreshold());
        v.setKeywordHighEnabled(r.getKeywordHighEnabled());
        v.setRemark(r.getRemark());
        v.setUpdateTime(r.getUpdateTime());
        return v;
    }
}
