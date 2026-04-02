/**
 * 解析 Spring SseEmitter 返回的 text/event-stream（支持 event: 与多行 data:）
 * @param {Response} response fetch 响应
 * @param {Record<string, (data: string) => void>} handlers 按事件名分发
 */
export async function readSseStream(response, handlers) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })

    let sep
    while ((sep = buffer.indexOf('\n\n')) >= 0) {
      const raw = buffer.slice(0, sep)
      buffer = buffer.slice(sep + 2)
      if (!raw.trim()) continue

      let eventName = 'message'
      const dataLines = []
      for (const line of raw.split('\n')) {
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
    }
  }
}
