package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.ResultCode;
import com.zxw.treehole.dto.ChatSessionCreateRequest;
import com.zxw.treehole.entity.AiChatMessage;
import com.zxw.treehole.entity.AiChatSession;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.AiChatMessageMapper;
import com.zxw.treehole.mapper.AiChatSessionMapper;
import com.zxw.treehole.service.AiChatSessionService;
import com.zxw.treehole.vo.AiChatMessageVo;
import com.zxw.treehole.vo.AiChatSessionVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatSessionServiceImpl implements AiChatSessionService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;

    @Override
    public List<AiChatSessionVo> listMySessions(Long userId) {
        List<AiChatSession> list = sessionMapper.selectList(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getUserId, userId)
                .eq(AiChatSession::getDeleted, 0)
                .orderByDesc(AiChatSession::getLastMessageAt)
                .orderByDesc(AiChatSession::getId));
        return list.stream().map(this::toSessionVo).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSession(Long userId, ChatSessionCreateRequest request) {
        AiChatSession s = new AiChatSession();
        s.setUserId(userId);
        s.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : "新对话");
        sessionMapper.insert(s);
        return s.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long userId, Long sessionId) {
        requireOwnSession(userId, sessionId);
        sessionMapper.deleteById(sessionId);
    }

    @Override
    public PageResult<AiChatMessageVo> pageMessages(Long userId, Long sessionId, long pageNum, long pageSize) {
        requireOwnSession(userId, sessionId);
        Page<AiChatMessage> page = messageMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .eq(AiChatMessage::getDeleted, 0)
                        .in(AiChatMessage::getMessageRole, "user", "assistant")
                        .orderByAsc(AiChatMessage::getCreateTime)
                        .orderByAsc(AiChatMessage::getId));
        Page<AiChatMessageVo> vo = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        vo.setRecords(page.getRecords().stream().map(this::toMessageVo).toList());
        return PageResult.of(vo);
    }

    private AiChatSession requireOwnSession(Long userId, Long sessionId) {
        AiChatSession s = sessionMapper.selectById(sessionId);
        if (s == null || (s.getDeleted() != null && s.getDeleted() == 1)) {
            throw new BusinessException("会话不存在");
        }
        if (!userId.equals(s.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权访问该会话");
        }
        return s;
    }

    private AiChatSessionVo toSessionVo(AiChatSession s) {
        AiChatSessionVo v = new AiChatSessionVo();
        v.setId(s.getId());
        v.setTitle(s.getTitle());
        v.setLastMessageAt(s.getLastMessageAt());
        v.setCreateTime(s.getCreateTime());
        return v;
    }

    private AiChatMessageVo toMessageVo(AiChatMessage m) {
        AiChatMessageVo v = new AiChatMessageVo();
        v.setId(m.getId());
        v.setRole(m.getMessageRole());
        v.setContent(m.getContent());
        v.setCreateTime(m.getCreateTime());
        return v;
    }
}
