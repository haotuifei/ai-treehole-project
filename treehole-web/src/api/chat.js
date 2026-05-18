import { getToken } from './http'
import { readSseStream } from '../utils/sseStream'
import { http } from './http'

/**
 * 流式发送树洞消息（POST + SSE）
 */
export async function streamChat({ sessionId, content, customSystemPrompt, onMeta, onDelta, onBlocked, onGoalSuggest, onEnd, onError }) {
  const token = getToken()
  const res = await fetch('/api/student/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify({
      sessionId: sessionId ?? null,
      content,
      customSystemPrompt: customSystemPrompt || ''
    })
  })

  if (!res.ok) {
    let errorMsg = `HTTP ${res.status}`
    try {
      const text = await res.text()
      const json = JSON.parse(text)
      errorMsg = json.message || errorMsg
    } catch {
      // ignore parse error
    }
    throw new Error(errorMsg)
  }

  await readSseStream(res, {
    meta: (data) => {
      try {
        onMeta?.(JSON.parse(data))
      } catch {
        onMeta?.(null)
      }
    },
    delta: (data) => onDelta?.(data),
    blocked: (data) => {
      try {
        onBlocked?.(JSON.parse(data))
      } catch {
        onBlocked?.(null)
      }
    },
    goal_suggest: (data) => {
      try {
        onGoalSuggest?.(JSON.parse(data))
      } catch {
        onGoalSuggest?.(null)
      }
    },
    end: (data) => {
      try {
        onEnd?.(JSON.parse(data))
      } catch {
        onEnd?.(null)
      }
    },
    error: (data) => onError?.(data)
  })
}

/**
 * 获取会话列表
 */
export function listSessions() {
  return http.get('/student/chat/sessions')
}

/**
 * 删除会话
 */
export function deleteSession(sessionId) {
  return http.delete(`/student/chat/sessions/${sessionId}`)
}

/**
 * 分页获取会话消息
 */
export function getSessionMessages(sessionId, { pageNum = 1, pageSize = 30 } = {}) {
  return http.get(`/student/chat/sessions/${sessionId}/messages`, {
    params: { pageNum, pageSize }
  })
}
