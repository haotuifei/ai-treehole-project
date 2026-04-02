import { getToken } from './http'
import { readSseStream } from '../utils/sseStream'

/**
 * 流式发送树洞消息（POST + SSE）
 */
export async function streamChat({ sessionId, content, onMeta, onDelta, onBlocked, onEnd, onError }) {
  const token = getToken()
  const res = await fetch('/api/student/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify({
      sessionId: sessionId ?? null,
      content
    })
  })

  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || `HTTP ${res.status}`)
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
