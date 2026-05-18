package com.zxw.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.entity.ModelConfig;
import com.zxw.treehole.mapper.ModelConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 模型配置服务：从数据库读取启用的模型配置
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigService {

    private final ModelConfigMapper modelConfigMapper;

    /**
     * 获取优先级最高且启用的模型配置（priority 越大优先级越高）
     */
    public Optional<ModelConfig> getActiveConfig() {
        List<ModelConfig> list = modelConfigMapper.selectList(
                new LambdaQueryWrapper<ModelConfig>()
                        .eq(ModelConfig::getEnabled, 1)
                        .orderByDesc(ModelConfig::getPriority)
                        .last("LIMIT 1")
        );
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * 获取所有配置
     */
    public List<ModelConfig> getAllConfigs() {
        return modelConfigMapper.selectList(
                new LambdaQueryWrapper<ModelConfig>().orderByDesc(ModelConfig::getPriority)
        );
    }
}
