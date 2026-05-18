package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.dto.CounselorInterventionPageQuery;
import com.zxw.treehole.entity.InterventionRecord;
import com.zxw.treehole.entity.SysUser;
import com.zxw.treehole.entity.WarningRecord;
import com.zxw.treehole.mapper.InterventionRecordMapper;
import com.zxw.treehole.mapper.SysUserMapper;
import com.zxw.treehole.mapper.WarningRecordMapper;
import com.zxw.treehole.service.CounselorInterventionService;
import com.zxw.treehole.vo.InterventionListItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorInterventionServiceImpl implements CounselorInterventionService {

    private final InterventionRecordMapper interventionRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final WarningRecordMapper warningRecordMapper;

    @Override
    public PageResult<InterventionListItemVo> pageInterventions(Long counselorUserId, CounselorInterventionPageQuery query) {
        // 查询辅导员的班级
        SysUser counselor = sysUserMapper.selectById(counselorUserId);
        String className = counselor != null ? counselor.getClassName() : null;

        if (className == null || className.isBlank()) {
            Page<InterventionRecord> empty = new Page<>(query.getPageNum(), query.getPageSize(), 0);
            empty.setRecords(List.of());
            Page<InterventionListItemVo> voEmpty = new Page<>(empty.getCurrent(), empty.getSize(), empty.getTotal());
            voEmpty.setRecords(List.of());
            return PageResult.of(voEmpty);
        }

        LambdaQueryWrapper<SysUser> sw = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getClassName, className)
                .eq(SysUser::getDeleted, 0)
                .inSql(SysUser::getId,
                        "SELECT user_id FROM sys_user_role WHERE role_id = (SELECT id FROM sys_role WHERE role_code = 'STUDENT')");
        if (StringUtils.hasText(query.getStudentKeyword())) {
            String kw = query.getStudentKeyword().trim();
            sw.and(w -> w.like(SysUser::getRealName, kw)
                    .or().like(SysUser::getUsername, kw));
        }
        List<Long> studentIds = sysUserMapper.selectList(sw).stream().map(SysUser::getId).toList();

        if (studentIds.isEmpty()) {
            Page<InterventionRecord> empty = new Page<>(query.getPageNum(), query.getPageSize(), 0);
            empty.setRecords(List.of());
            Page<InterventionListItemVo> voEmpty = new Page<>(empty.getCurrent(), empty.getSize(), empty.getTotal());
            voEmpty.setRecords(List.of());
            return PageResult.of(voEmpty);
        }

        // 查询这些学生的干预记录
        LambdaQueryWrapper<InterventionRecord> qw = new LambdaQueryWrapper<InterventionRecord>()
                .eq(InterventionRecord::getDeleted, 0)
                .in(InterventionRecord::getStudentUserId, studentIds)
                .orderByDesc(InterventionRecord::getInterventionTime);

        Page<InterventionRecord> page = interventionRecordMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), qw);

        // 批量加载关联数据
        List<Long> userIds = page.getRecords().stream()
                .map(InterventionRecord::getStudentUserId).distinct().toList();
        Map<Long, SysUser> studentMap = loadUsers(userIds);

        List<Long> counselorIds = page.getRecords().stream()
                .map(InterventionRecord::getCounselorUserId).distinct().toList();
        Map<Long, SysUser> counselorMap = loadUsers(counselorIds);

        List<Long> warningIds = page.getRecords().stream()
                .map(InterventionRecord::getWarningRecordId).distinct().toList();
        Map<Long, WarningRecord> warningMap = loadWarnings(warningIds);

        Page<InterventionListItemVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(ir -> toListItem(ir, studentMap, counselorMap, warningMap))
                .toList());
        return PageResult.of(voPage);
    }

    private InterventionListItemVo toListItem(InterventionRecord ir,
                                               Map<Long, SysUser> studentMap,
                                               Map<Long, SysUser> counselorMap,
                                               Map<Long, WarningRecord> warningMap) {
        InterventionListItemVo vo = new InterventionListItemVo();
        vo.setId(ir.getId());
        vo.setWarningRecordId(ir.getWarningRecordId());
        vo.setStudentUserId(ir.getStudentUserId());
        vo.setCounselorUserId(ir.getCounselorUserId());
        vo.setContent(ir.getContent());
        vo.setInterventionTime(ir.getInterventionTime());

        SysUser st = studentMap.get(ir.getStudentUserId());
        if (st != null) {
            vo.setStudentName(st.getRealName());
            vo.setStudentUsername(st.getUsername());
            vo.setClassName(st.getClassName());
        }

        SysUser co = counselorMap.get(ir.getCounselorUserId());
        if (co != null) {
            vo.setCounselorName(co.getRealName());
        }

        WarningRecord wr = warningMap.get(ir.getWarningRecordId());
        if (wr != null) {
            vo.setWarningRiskLevel(wr.getRiskLevel());
            vo.setWarningTextSummary(wr.getTextSummary());
        }

        return vo;
    }

    private Map<Long, SysUser> loadUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        List<Long> distinct = ids.stream().distinct().toList();
        if (distinct.isEmpty()) return Map.of();
        List<SysUser> list = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().in(SysUser::getId, distinct));
        return list.stream().collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a, HashMap::new));
    }

    private Map<Long, WarningRecord> loadWarnings(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        List<Long> distinct = ids.stream().distinct().toList();
        if (distinct.isEmpty()) return Map.of();
        List<WarningRecord> list = warningRecordMapper.selectList(
                new LambdaQueryWrapper<WarningRecord>().in(WarningRecord::getId, distinct));
        return list.stream().collect(Collectors.toMap(WarningRecord::getId, w -> w, (a, b) -> a, HashMap::new));
    }
}
