package com.zxw.treehole.controller.student;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.ChatSessionCreateRequest;
import com.zxw.treehole.dto.ChatStreamRequest;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.AiChatSessionService;
import com.zxw.treehole.service.AiChatStreamService;
import com.zxw.treehole.vo.AiChatMessageVo;
import com.zxw.treehole.vo.AiChatSessionVo;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "学生端-AI树洞", description = "多轮对话 + SSE 流式输出")
@RestController
@RequestMapping("/api/student/chat")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('STUDENT')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class StudentAiChatController {

    private final AiChatSessionService aiChatSessionService;
    private final AiChatStreamService aiChatStreamService;

    @Operation(summary = "我的会话列表")
    @GetMapping("/sessions")
    public Result<List<AiChatSessionVo>> listSessions(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(aiChatSessionService.listMySessions(loginUser.getUser().getId()));
    }

    @Operation(summary = "创建空会话")
    @PostMapping("/sessions")
    public Result<Long> createSession(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody(required = false) ChatSessionCreateRequest request) {
        if (request == null) {
            request = new ChatSessionCreateRequest();
        }
        return Result.ok(aiChatSessionService.createSession(loginUser.getUser().getId(), request));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long sessionId) {
        aiChatSessionService.deleteSession(loginUser.getUser().getId(), sessionId);
        return Result.ok(null);
    }

    @Operation(summary = "分页查询历史消息（user/assistant）")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<PageResult<AiChatMessageVo>> pageMessages(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") @Min(1) long pageNum,
            @RequestParam(defaultValue = "30") @Min(1) @Max(100) long pageSize) {
        return Result.ok(aiChatSessionService.pageMessages(loginUser.getUser().getId(), sessionId, pageNum, pageSize));
    }

    @Hidden
    @Operation(summary = "发送消息并以 SSE 流式返回助手回复（EventStream）")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody ChatStreamRequest request) {
        SseEmitter emitter = new SseEmitter(300_000L);
        aiChatStreamService.startStreamAsync(loginUser.getUser().getId(), request, emitter);
        return emitter;
    }
}
