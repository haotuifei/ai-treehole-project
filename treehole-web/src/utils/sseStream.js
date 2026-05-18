/**
 * 解析 Spring SseEmitter 返回的 text/event-stream（支持 event: 与多行 data:）
 * @param {Response} response fetch 响应
 * @param {Record<string, (data: string) => void>} handlers 按事件名分发
 * @param {number} timeoutMs 超时毫秒，默认 120000（2分钟）
 */
export async function readSseStream(response, handlers, timeoutMs = 120000) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let resolved = false

  let timeoutId
  const timeoutPromise = new Promise((_, reject) => {
    timeoutId = setTimeout(() => reject(new Error('SSE 超时')), timeoutMs)
  })

  try {
    while (true) {
      let sep = -1
      let skip = 2
      // Spring 可能输出 \n\n 或 \r\n\r\n
      while (true) {
        const n = buffer.indexOf('\n\n')
        const r = buffer.indexOf('\r\n\r\n')
        if (n === -1 && r === -1) break
        if (r === -1 || (n !== -1 && n < r)) {
          sep = n
          skip = 2
        } else {
          sep = r
          skip = 4
        }
        const raw = buffer.slice(0, sep)
        buffer = buffer.slice(sep + skip)
        if (!raw.trim()) continue

        let eventName = 'message'
        const dataLines = []
        for (const line of raw.split(/\r?\n/)) {
          if (line.startsWith('event:')) {
            eventName = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            dataLines.push(line.slice(5).replace(/^\u0020/, ''))
          }
        }
        const data = dataLines.join('\n')
        const fn = handlers[eventName]
        if (typeof fn === 'function') {
          fn(data)
        }
        // 收到 end 或 error 后立刻结束
        if (eventName === 'end' || eventName === 'error') {
          resolved = true
          break
        }
      }
      if (resolved) break

      const { done, value } = await Promise.race([reader.read(), timeoutPromise])
      if (done) break
      buffer += decoder.decode(value, { stream: true })
    }
  } finally {
    if (timeoutId) clearTimeout(timeoutId)
    try {
      reader.cancel()
    } catch (_) {
      /* ignore */
    }
  }
}
