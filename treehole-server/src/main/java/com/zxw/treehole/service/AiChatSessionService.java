package com.zxw.treehole.service;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.dto.ChatSessionCreateRequest;
import com.zxw.treehole.vo.AiChatMessageVo;
import com.zxw.treehole.vo.AiChatSessionVo;

import java.util.List;

public interface AiChatSessionService {

    List<AiChatSessionVo> listMySessions(Long userId);

    Long createSession(Long userId, ChatSessionCreateRequest request);

    void deleteSession(Long userId, Long sessionId);

    PageResult<AiChatMessageVo> pageMessages(Long userId, Long sessionId, long pageNum, long pageSize);
}
