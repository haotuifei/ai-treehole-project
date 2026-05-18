package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.ResultCode;
import com.zxw.treehole.dto.CounselorWarningPageQuery;
import com.zxw.treehole.dto.InterventionCreateRequest;
import com.zxw.treehole.dto.WarningStatusUpdateRequest;
import com.zxw.treehole.entity.EmotionRecord;
import com.zxw.treehole.entity.InterventionRecord;
import com.zxw.treehole.entity.SysUser;
import com.zxw.treehole.entity.WarningRecord;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.EmotionRecordMapper;
import com.zxw.treehole.mapper.InterventionRecordMapper;
import com.zxw.treehole.mapper.SysUserMapper;
import com.zxw.treehole.mapper.WarningRecordMapper;
import com.zxw.treehole.service.CounselorWarningService;
import com.zxw.treehole.vo.EmotionRecordBriefVo;
import com.zxw.treehole.vo.InterventionVo;
import com.zxw.treehole.vo.WarningDetailVo;
import com.zxw.treehole.vo.WarningListItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorWarningServiceImpl implements CounselorWarningService {

    private static final Set<String> STATUSES = Set.of("PENDING", "PROCESSING", "RESOLVED", "CLOSED");

    private final WarningRecordMapper warningRecordMapper;
    private final EmotionRecordMapper emotionRecordMapper;
    private final InterventionRecordMapper interventionRecordMapper;
    private final SysUserMapper sysUserMapper;

    private String getCounselorClassName(Long counselorUserId) {
        SysUser counselor = sysUserMapper.selectById(counselorUserId);
        return counselor != null ? counselor.getClassName() : null;
    }

    @Override
    public PageResult<WarningListItemVo> pageWarnings(Long counselorUserId, CounselorWarningPageQuery query) {
        String className = getCounselorClassName(counselorUserId);
        if (className == null || className.isBlank()) {
            Page<WarningListItemVo> empty = new Page<>(query.getPageNum(), query.getPageSize(), 0);
            empty.setRecords(List.of());
            return PageResult.of(empty);
        }

        LambdaQueryWrapper<SysUser> uw = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getClassName, className)
                .eq(SysUser::getDeleted, 0)
                .inSql(SysUser::getId,
                        "SELECT user_id FROM sys_user_role WHERE role_id = (SELECT id FROM sys_role WHERE role_code = 'STUDENT')");
        if (StringUtils.hasText(query.getStudentKeyword())) {
            String kw = query.getStudentKeyword().trim();
            uw.and(w -> w.like(SysUser::getRealName, kw).or().like(SysUser::getUsername, kw));
        }
        List<Long> studentIds = sysUserMapper.selectList(uw).stream().map(SysUser::getId).toList();
        if (studentIds.isEmpty()) {
            Page<WarningListItemVo> empty = new Page<>(query.getPageNum(), query.getPageSize(), 0);
            empty.setRecords(List.of());
            return PageResult.of(empty);
        }

        LambdaQueryWrapper<WarningRecord> w = new LambdaQueryWrapper<WarningRecord>()
                .eq(WarningRecord::getDeleted, 0)
                .in(WarningRecord::getUserId, studentIds)
                .orderByDesc(WarningRecord::getCreateTime);
        if (StringUtils.hasText(query.getStatus())) {
            w.eq(WarningRecord::getStatus, query.getStatus().trim());
        }
        Page<WarningRecord> page = warningRecordMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), w);
        Map<Long, SysUser> userMap = loadUsers(page.getRecords().stream().map(WarningRecord::getUserId).distinct().toList());

        Page<WarningListItemVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(r -> toListItem(r, userMap)).toList());
        return PageResult.of(voPage);
    }

    @Override
    public WarningDetailVo getDetail(Long counselorUserId, Long warningId) {
        WarningRecord wr = requireWarning(warningId);
        assertCounselorScope(counselorUserId, wr.getUserId());
        Map<Long, SysUser> userMap = loadUsers(List.of(wr.getUserId()));
        SysUser st = userMap.get(wr.getUserId());

        WarningDetailVo vo = new WarningDetailVo();
        vo.setId(wr.getId());
        vo.setUserId(wr.getUserId());
        if (st != null) {
            vo.setStudentName(st.getRealName());
            vo.setStudentUsername(st.getUsername());
            vo.setClassName(st.getClassName());
        }
        vo.setSessionId(wr.getSessionId());
        vo.setEmotionRecordId(wr.getEmotionRecordId());
        vo.setTextSummary(wr.getTextSummary());
        vo.setTriggerSource(wr.getTriggerSource());
        vo.setRiskLevel(wr.getRiskLevel());
        vo.setStatus(wr.getStatus());
        vo.setHandlerUserId(wr.getHandlerUserId());
        vo.setHandleRemark(wr.getHandleRemark());
        vo.setHandledAt(wr.getHandledAt());
        vo.setCreateTime(wr.getCreateTime());

        if (wr.getEmotionRecordId() != null) {
            EmotionRecord er = emotionRecordMapper.selectById(wr.getEmotionRecordId());
            if (er != null && (er.getDeleted() == null || er.getDeleted() == 0)) {
                EmotionRecordBriefVo eb = new EmotionRecordBriefVo();
                eb.setId(er.getId());
                eb.setSentimentScore(er.getSentimentScore());
                eb.setEmotionLabel(er.getEmotionLabel());
                eb.setRiskLevel(er.getRiskLevel());
                eb.setAnalysisDetail(er.getAnalysisDetail());
                eb.setCreateTime(er.getCreateTime());
                vo.setEmotion(eb);
            }
        }

        List<InterventionRecord> irs = interventionRecordMapper.selectList(new LambdaQueryWrapper<InterventionRecord>()
                .eq(InterventionRecord::getWarningRecordId, warningId)
                .eq(InterventionRecord::getDeleted, 0)
                .orderByDesc(InterventionRecord::getInterventionTime));
        Map<Long, SysUser> counselors = loadUsers(irs.stream().map(InterventionRecord::getCounselorUserId).distinct().toList());
        vo.setInterventions(irs.stream().map(ir -> toInterventionVo(ir, counselors)).toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long counselorUserId, Long warningId, WarningStatusUpdateRequest request) {
        WarningRecord wr = requireWarning(warningId);
        assertCounselorScope(counselorUserId, wr.getUserId());
        String st = request.getStatus().trim();
        if (!STATUSES.contains(st)) {
            throw new BusinessException("无效的处理状态");
        }
        wr.setStatus(st);
        if (request.getHandleRemark() != null) {
            wr.setHandleRemark(request.getHandleRemark());
        }
        wr.setHandlerUserId(counselorUserId);
        if ("RESOLVED".equals(st) || "CLOSED".equals(st)) {
            wr.setHandledAt(LocalDateTime.now());
        }
        warningRecordMapper.updateById(wr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIntervention(Long counselorUserId, Long warningId, InterventionCreateRequest request) {
        WarningRecord wr = requireWarning(warningId);
        assertCounselorScope(counselorUserId, wr.getUserId());
        InterventionRecord ir = new InterventionRecord();
        ir.setWarningRecordId(warningId);
        ir.setStudentUserId(wr.getUserId());
        ir.setCounselorUserId(counselorUserId);
        ir.setContent(request.getContent().trim());
        ir.setInterventionTime(LocalDateTime.now());
        interventionRecordMapper.insert(ir);
        if ("PENDING".equals(wr.getStatus())) {
            wr.setStatus("PROCESSING");
            wr.setHandlerUserId(counselorUserId);
            warningRecordMapper.updateById(wr);
        }
    }

    private WarningRecord requireWarning(Long id) {
        WarningRecord wr = warningRecordMapper.selectById(id);
        if (wr == null || (wr.getDeleted() != null && wr.getDeleted() == 1)) {
            throw new BusinessException("预警记录不存在");
        }
        return wr;
    }

    private void assertCounselorScope(Long counselorUserId, Long studentUserId) {
        SysUser st = sysUserMapper.selectById(studentUserId);
        if (st == null || (st.getDeleted() != null && st.getDeleted() == 1)) {
            throw new BusinessException("学生不存在");
        }
        String className = getCounselorClassName(counselorUserId);
        if (st.getClassName() == null || !st.getClassName().equals(className)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看该学生预警");
        }
    }

    private Map<Long, SysUser> loadUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        List<Long> distinct = ids.stream().distinct().toList();
        if (distinct.isEmpty()) {
            return Map.of();
        }
        List<SysUser> list = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, distinct));
        return list.stream().collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a, HashMap::new));
    }

    private WarningListItemVo toListItem(WarningRecord r, Map<Long, SysUser> userMap) {
        WarningListItemVo v = new WarningListItemVo();
        v.setId(r.getId());
        v.setUserId(r.getUserId());
        SysUser u = userMap.get(r.getUserId());
        if (u != null) {
            v.setStudentName(u.getRealName());
            v.setStudentUsername(u.getUsername());
            v.setClassName(u.getClassName());
        }
        v.setSessionId(r.getSessionId());
        v.setTextSummary(r.getTextSummary());
        v.setTriggerSource(r.getTriggerSource());
        v.setRiskLevel(r.getRiskLevel());
        v.setStatus(r.getStatus());
        v.setCreateTime(r.getCreateTime());
        return v;
    }

    private InterventionVo toInterventionVo(InterventionRecord ir, Map<Long, SysUser> counselors) {
        InterventionVo v = new InterventionVo();
        v.setId(ir.getId());
        v.setCounselorUserId(ir.getCounselorUserId());
        SysUser c = counselors.get(ir.getCounselorUserId());
        v.setCounselorName(c != null ? c.getRealName() : null);
        v.setContent(ir.getContent());
        v.setInterventionTime(ir.getInterventionTime());
        return v;
    }
}
