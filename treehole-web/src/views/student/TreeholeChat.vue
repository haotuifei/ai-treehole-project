<template>
  <div class="page">
    <div class="top-bar">
      <div>
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
          <div class="actions">
            <el-button
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
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { streamChat } from '../../api/chat'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const hasToken = computed(() => user.isLoggedIn)

const messages = ref([])
const draft = ref('')
const sending = ref(false)
const scrollRef = ref(null)

function clearSession() {
  messages.value = []
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
      content: text,
      onMeta: (meta) => {
        if (meta?.riskLevel === 'HIGH') {
          ElMessage.warning('检测到高风险表述，已启用安全回复并记录预警')
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
  padding: 20px 24px 0;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  flex-shrink: 0;
}
.chat-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--th-text);
}
.chat-sub {
  margin: 4px 0 0;
  font-size: 0.9rem;
  color: var(--th-text-muted);
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
}
.msg-scroll :deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
  padding-bottom: 16px;
}
.msg-scroll :deep(.el-scrollbar__view) {
  height: 100%;
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
  padding: 16px 8px 24px;
  background: linear-gradient(to top, var(--th-bg) 80%, transparent);
}
.composer {
  max-width: 720px;
  margin: 0 auto;
}
.composer :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  border: 1px solid var(--th-border);
  border-radius: 16px;
  padding: 14px 16px;
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
.actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
@media (max-width: 768px) {
  .page {
    padding: 16px 12px 0;
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
