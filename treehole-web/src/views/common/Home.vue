<template>
  <div class="page">
    <div class="top-bar">
      <div class="title-group">
        <h2 class="chat-title">AI 树洞</h2>
        <p class="chat-sub">慢慢说，我会认真听。</p>
      </div>
      <div class="top-actions">
        <el-button v-if="!user.isLoggedIn" type="primary" round plain @click="$router.push('/login')">
          去登录
        </el-button>
        <el-button v-else type="primary" round @click="clearSession">新对话</el-button>
      </div>
    </div>

    <div class="chat-area">
      <el-scrollbar ref="scrollRef" class="msg-scroll">
        <div class="msgs">
          <div v-if="loadingHistory" class="empty-state">
            <p class="empty-text">加载历史消息...</p>
          </div>
          <template v-else>
            <div v-for="(m, i) in messages" :key="i" :class="['bubble', m.role]">
              <div class="bubble-content">
                <span class="role-tag">{{ m.role === 'user' ? '我' : '陪伴者' }}</span>
                <p class="text">{{ m.content }}<span v-if="m.streaming" class="cursor">▍</span></p>
              </div>
            </div>
            <div v-if="!messages.length && !sending" class="empty-state">
              <p class="empty-text">在下方输入想说的话</p>
              <p class="empty-sub">支持流式回复，我会认真倾听你的每一句话</p>
            </div>
            <div v-if="sending && !messages.length" class="empty-state">
              <p class="empty-text">正在思考...</p>
            </div>
          </template>
        </div>
      </el-scrollbar>

      <div class="composer-area">
        <div class="composer">
          <el-input
            v-model="draft"
            type="textarea"
            :rows="3"
            placeholder="说说备考压力、情绪或任何想聊的…"
            :disabled="sending || !user.isLoggedIn"
            @keydown.enter.exact.prevent="send"
          />
          <el-button
            class="send-btn"
            type="primary"
            round
            :loading="sending"
            :disabled="!user.isLoggedIn || !draft.trim()"
            @click="send"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { streamChat, getSessionMessages } from '../../api/chat'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const route = useRoute()
const router = useRouter()
const hasToken = computed(() => user.isLoggedIn)

const messages = ref([])
const draft = ref('')
const sending = ref(false)
const scrollRef = ref(null)
const currentSessionId = ref(null)
const loadingHistory = ref(false)

onMounted(() => {
  const sid = route.query.sessionId
  if (sid) {
    currentSessionId.value = Number(sid)
    loadHistory(Number(sid))
  }
})

watch(() => route.query.sessionId, (newId) => {
  if (newId) {
    currentSessionId.value = Number(newId)
    loadHistory(Number(newId))
  } else {
    currentSessionId.value = null
    messages.value = []
  }
})

async function loadHistory(sessionId) {
  loadingHistory.value = true
  try {
    const res = await getSessionMessages(sessionId, { pageNum: 1, pageSize: 100 })
    const records = res.data.data?.records || []
    // 消息是倒序的，需要反转
    messages.value = records.map(m => ({ role: m.role, content: m.content, streaming: false }))
    scrollBottom()
  } catch (e) {
    ElMessage.error('加载历史消息失败')
  } finally {
    loadingHistory.value = false
  }
}

function clearSession() {
  messages.value = []
  currentSessionId.value = null
  router.replace('/home')
}

function scrollBottom() {
  nextTick(() => {
    const wrap = scrollRef.value?.wrapRef
    if (wrap) wrap.scrollTop = wrap.scrollHeight
  })
}

async function send() {
  const text = draft.value.trim()
  if (!text || sending.value) return
  sending.value = true
  messages.value.push({ role: 'user', content: text, streaming: false })
  const assistantIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '', streaming: true })
  draft.value = ''
  scrollBottom()

  try {
    await streamChat({
      sessionId: currentSessionId.value,
      content: text,
      onMeta: (meta) => {
        if (meta?.riskLevel === 'HIGH') {
          ElMessage.warning('检测到高风险表述，已启用安全回复并记录预警')
        }
        if (meta?.sessionId && !currentSessionId.value) {
          currentSessionId.value = meta.sessionId
          router.replace({ query: { sessionId: meta.sessionId } })
        }
      },
      onDelta: (chunk) => {
        const m = messages.value[assistantIndex]
        if (m) m.content += chunk
        scrollBottom()
      },
      onBlocked: () => {},
      onEnd: () => {
        const m = messages.value[assistantIndex]
        if (m) m.streaming = false
        scrollBottom()
      },
      onError: (msg) => {
        ElMessage.error(msg || '流式输出错误')
        const m = messages.value[assistantIndex]
        if (m) {
          m.streaming = false
          if (!m.content) m.content = '（输出中断）'
        }
      }
    })
  } catch (e) {
    ElMessage.error(e.message || '请求失败')
    const m = messages.value[assistantIndex]
    if (m) {
      m.streaming = false
      if (!m.content) m.content = '（请求失败）'
    }
  } finally {
    sending.value = false
    const m = messages.value[assistantIndex]
    if (m) m.streaming = false
    scrollBottom()
  }
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 56px);
  padding: 20px 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  flex-shrink: 0;
}
.title-group {
  margin-left: 24px;
}
.chat-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--th-text);
}
.chat-sub {
  margin: 6px 0 0;
  font-size: 0.95rem;
  color: var(--th-text-muted);
  font-family: 'Georgia', 'Times New Roman', 'Noto Serif SC', serif;
  font-style: italic;
  letter-spacing: 0.08em;
  line-height: 1.6;
  opacity: 0.85;
}
.top-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.msg-scroll {
  flex: 1;
  overflow: hidden;
  min-height: 0;
}
.msg-scroll :deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
  overflow-y: auto;
  padding-bottom: 16px;
}
.msg-scroll :deep(.el-scrollbar__view) {
  height: auto;
}
.msgs {
  max-width: 720px;
  margin: 0 auto;
  padding: 0 8px;
}
.bubble {
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
  display: flex;
}
.bubble.user {
  justify-content: flex-end;
}
.bubble.assistant {
  justify-content: flex-start;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.bubble-content {
  display: inline-block;
  max-width: 80%;
  padding: 14px 18px;
  border-radius: 18px;
  line-height: 1.65;
}
.bubble.user .bubble-content {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(232, 228, 222, 0.9);
  color: var(--th-text);
  border-bottom-right-radius: 4px;
}
.bubble.assistant .bubble-content {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(232, 228, 222, 0.9);
  color: var(--th-text);
  border-bottom-left-radius: 4px;
}
.role-tag {
  display: block;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
  opacity: 0.6;
}
.text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
.cursor {
  display: inline-block;
  animation: blink 1s step-end infinite;
  color: var(--th-primary);
  font-weight: 100;
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--th-text-muted);
}
.empty-text {
  margin: 0 0 8px;
  font-size: 1rem;
  color: var(--th-text-muted);
}
.empty-sub {
  margin: 0;
  font-size: 0.88rem;
  color: var(--th-text-muted);
  opacity: 0.7;
}
.composer-area {
  flex-shrink: 0;
  padding: 12px 0 0;
  background: linear-gradient(to top, var(--th-bg) 80%, transparent);
}
.composer {
  max-width: 720px;
  margin: 0 auto;
  position: relative;
}
.composer :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  border: 1px solid var(--th-border);
  border-radius: 16px;
  padding: 14px 90px 14px 16px;
  resize: none;
  box-shadow: 0 4px 20px rgba(61, 58, 54, 0.04);
  font-size: 15px;
  line-height: 1.6;
  transition: all 0.2s ease;
}
.composer :deep(.el-textarea__inner:focus) {
  background: rgba(255, 255, 255, 0.85);
  border-color: var(--th-primary);
  box-shadow: 0 4px 24px rgba(124, 154, 130, 0.15);
}
.composer :deep(.el-textarea__inner::placeholder) {
  color: var(--th-text-muted);
  opacity: 0.6;
}
.send-btn {
  position: absolute;
  right: 12px;
  bottom: 12px;
  z-index: 2;
}
.actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
@media (max-width: 768px) {
  .page {
    padding: 16px 12px;
    box-sizing: border-box;
    overflow: hidden;
  }
  .bubble-content {
    max-width: 90%;
    padding: 12px 14px;
  }
  .empty-state {
    padding: 40px 16px;
  }
}
</style>
