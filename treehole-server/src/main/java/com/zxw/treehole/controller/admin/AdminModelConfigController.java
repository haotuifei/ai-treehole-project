package com.zxw.treehole.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.entity.ModelConfig;
import com.zxw.treehole.mapper.ModelConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "管理端-模型配置", description = "AI 模型接入配置，需管理员角色")
@RestController
@RequestMapping("/api/admin/model-configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class AdminModelConfigController {

    private final ModelConfigMapper modelConfigMapper;

    @Operation(summary = "查询所有模型配置")
    @GetMapping
    public Result<List<ModelConfig>> list() {
        List<ModelConfig> list = modelConfigMapper.selectList(
                new LambdaQueryWrapper<ModelConfig>().orderByAsc(ModelConfig::getPriority)
        );
        return Result.ok(list);
    }

    @Operation(summary = "新增模型配置")
    @PostMapping
    public Result<Long> create(@RequestBody ModelConfig config) {
        config.setId(null);
        config.setDeleted(0);
        modelConfigMapper.insert(config);
        return Result.ok(config.getId());
    }

    @Operation(summary = "更新模型配置")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ModelConfig config) {
        config.setId(id);
        modelConfigMapper.updateById(config);
        return Result.ok(null);
    }

    @Operation(summary = "删除模型配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        modelConfigMapper.deleteById(id);
        return Result.ok(null);
    }
}
