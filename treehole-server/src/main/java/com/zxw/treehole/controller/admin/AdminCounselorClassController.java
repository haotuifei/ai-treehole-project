package com.zxw.treehole.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.CounselorClassRequest;
import com.zxw.treehole.entity.CounselorClass;
import com.zxw.treehole.entity.SysUser;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.CounselorClassMapper;
import com.zxw.treehole.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "管理端-辅导员班级管理", description = "分配辅导员管辖班级，需管理员角色")
@RestController
@RequestMapping("/api/admin/counselor-classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class AdminCounselorClassController {

    private final CounselorClassMapper counselorClassMapper;
    private final SysUserMapper sysUserMapper;

    @Operation(summary = "查询所有辅导员及其管辖班级")
    @GetMapping
    public Result<List<Map<String, Object>>> listAll() {
        // 查询所有辅导员
        List<SysUser> counselors = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .inSql(SysUser::getId,
                                "SELECT user_id FROM sys_user_role WHERE role_id = (SELECT id FROM sys_role WHERE role_code = 'COUNSELOR')"));

        // 查询所有关联关系
        List<CounselorClass> allRelations = counselorClassMapper.selectList(
                new LambdaQueryWrapper<CounselorClass>().eq(CounselorClass::getDeleted, 0));

        // 按辅导员分组
        Map<Long, List<String>> classMap = new LinkedHashMap<>();
        for (CounselorClass cc : allRelations) {
            classMap.computeIfAbsent(cc.getCounselorUserId(), k -> new java.util.ArrayList<>())
                    .add(cc.getClassName());
        }

        List<Map<String, Object>> result = counselors.stream().map(u -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("counselorUserId", u.getId());
            item.put("realName", u.getRealName());
            item.put("username", u.getUsername());
            item.put("classes", classMap.getOrDefault(u.getId(), List.of()));
            return item;
        }).toList();

        return Result.ok(result);
    }

    @Operation(summary = "给辅导员分配班级")
    @PostMapping
    public Result<Void> assign(@Valid @RequestBody CounselorClassRequest request) {
        // 检查辅导员是否存在
        SysUser counselor = sysUserMapper.selectById(request.getCounselorUserId());
        if (counselor == null || (counselor.getDeleted() != null && counselor.getDeleted() == 1)) {
            throw new BusinessException("辅导员不存在");
        }

        // 检查是否已存在
        Long exists = counselorClassMapper.selectCount(
                new LambdaQueryWrapper<CounselorClass>()
                        .eq(CounselorClass::getCounselorUserId, request.getCounselorUserId())
                        .eq(CounselorClass::getClassName, request.getClassName().trim())
                        .eq(CounselorClass::getDeleted, 0));
        if (exists != null && exists > 0) {
            throw new BusinessException("该班级已分配给此辅导员");
        }

        CounselorClass cc = new CounselorClass();
        cc.setCounselorUserId(request.getCounselorUserId());
        cc.setClassName(request.getClassName().trim());
        counselorClassMapper.insert(cc);
        return Result.ok(null);
    }

    @Operation(summary = "查询指定辅导员的管辖班级")
    @GetMapping("/{counselorUserId}")
    public Result<List<String>> listByCounselor(@PathVariable Long counselorUserId) {
        List<CounselorClass> list = counselorClassMapper.selectList(
                new LambdaQueryWrapper<CounselorClass>()
                        .eq(CounselorClass::getCounselorUserId, counselorUserId)
                        .eq(CounselorClass::getDeleted, 0));
        List<String> classes = list.stream().map(CounselorClass::getClassName).toList();
        return Result.ok(classes);
    }

    @Operation(summary = "批量同步辅导员的管辖班级")
    @PutMapping("/{counselorUserId}")
    public Result<Void> syncClasses(@PathVariable Long counselorUserId, @RequestBody List<String> classNames) {
        // 物理删除原有班级（避免软删除导致唯一约束冲突）
        counselorClassMapper.physicalDeleteByCounselorId(counselorUserId);
        // 插入新班级
        for (String name : classNames) {
            if (name != null && !name.isBlank()) {
                CounselorClass cc = new CounselorClass();
                cc.setCounselorUserId(counselorUserId);
                cc.setClassName(name.trim());
                counselorClassMapper.insert(cc);
            }
        }
        return Result.ok(null);
    }

    @Operation(summary = "移除辅导员的班级")
    @DeleteMapping
    public Result<Void> remove(@Valid @RequestBody CounselorClassRequest request) {
        Long count = counselorClassMapper.selectCount(
                new LambdaQueryWrapper<CounselorClass>()
                        .eq(CounselorClass::getCounselorUserId, request.getCounselorUserId())
                        .eq(CounselorClass::getClassName, request.getClassName().trim())
                        .eq(CounselorClass::getDeleted, 0));
        if (count == null || count == 0) {
            throw new BusinessException("该班级分配记录不存在");
        }

        counselorClassMapper.delete(
                new LambdaQueryWrapper<CounselorClass>()
                        .eq(CounselorClass::getCounselorUserId, request.getCounselorUserId())
                        .eq(CounselorClass::getClassName, request.getClassName().trim()));
        return Result.ok(null);
    }
}
