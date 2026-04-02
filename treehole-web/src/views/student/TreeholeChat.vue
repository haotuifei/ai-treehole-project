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
        <template v-else>
          <el-button round @click="loadSessions">刷新会话</el-button>
          <el-button type="primary" round @click="newSession">新对话</el-button>
        </template>
      </div>
    </div>

    <div class="body">
      <aside class="sessions th-card">
        <el-scrollbar height="calc(100vh - 200px)">
          <div
            v-for="s in sessions"
            :key="s.id"
            :class="['sess-item', { active: s.id === sessionId }]"
            @click="selectSession(s)"
          >
            <div class="sess-title">{{ s.title || '未命名' }}</div>
            <div class="sess-time">{{ formatTime(s.lastMessageAt) }}</div>
          </div>
          <el-empty v-if="!sessions.length && user.isLoggedIn" description="暂无会话" :image-size="72" />
        </el-scrollbar>
      </aside>

      <main class="chat th-card">
        <el-scrollbar ref="scrollRef" height="calc(100vh - 280px)">
          <div class="msgs">
            <div v-for="(m, i) in messages" :key="i" :class="['bubble', m.role]">
              <div class="role-label">{{ m.role === 'user' ? '我' : '陪伴者' }}</div>
              <div class="text">{{ m.content }}<span v-if="m.streaming" class="cursor">▍</span></div>
            </div>
            <el-empty
              v-if="!messages.length"
              description="在下方输入想说的话，支持流式回复"
              :image-size="80"
            />
          </div>
        </el-scrollbar>

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
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'
import { streamChat } from '../../api/chat'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const hasToken = computed(() => user.isLoggedIn)

const sessions = ref([])
const sessionId = ref(null)
const messages = ref([])
const draft = ref('')
const sending = ref(false)
const scrollRef = ref(null)

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

async function loadSessions() {
  if (!hasToken.value) return
  try {
    const { data } = await http.get('/student/chat/sessions')
    if (data.code === 200) {
      sessions.value = data.data || []
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载会话失败')
  }
}

function newSession() {
  sessionId.value = null
  messages.value = []
}

async function selectSession(s) {
  sessionId.value = s.id
  await loadHistory()
}

async function loadHistory() {
  if (!sessionId.value) return
  try {
    const { data } = await http.get(`/student/chat/sessions/${sessionId.value}/messages`, {
      params: { pageNum: 1, pageSize: 100 }
    })
    if (data.code !== 200) return
    const page = data.data
    const rows = page?.records || []
    messages.value = rows.map((r) => ({
      role: r.role,
      content: r.content || '',
      streaming: false
    }))
    scrollBottom()
  } catch {
    ElMessage.error('加载历史失败')
  }
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
      sessionId: sessionId.value,
      content: text,
      onMeta: (meta) => {
        if (meta?.sessionId) sessionId.value = meta.sessionId
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
        loadSessions()
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

onMounted(() => {
  if (hasToken.value) loadSessions()
})

watch(hasToken, (v) => {
  if (v) loadSessions()
  else {
    sessions.value = []
    sessionId.value = null
    messages.value = []
  }
})
</script>

<style scoped>
.page {
  padding: 16px 20px 24px;
  max-width: 1100px;
  margin: 0 auto;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}
.chat-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}
.chat-sub {
  margin: 6px 0 0;
  font-size: 0.9rem;
  color: var(--th-text-muted);
}
.top-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.body {
  display: flex;
  gap: 16px;
  min-height: 0;
}
.sessions {
  width: 260px;
  flex-shrink: 0;
  padding: 12px;
}
.sess-item {
  padding: 10px 12px;
  border-radius: var(--th-radius-sm);
  cursor: pointer;
  margin-bottom: 6px;
  border: 1px solid transparent;
}
.sess-item:hover {
  background: var(--th-primary-soft);
}
.sess-item.active {
  border-color: var(--th-primary);
  background: var(--th-primary-soft);
}
.sess-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sess-time {
  font-size: 12px;
  color: var(--th-text-muted);
  margin-top: 4px;
}
.chat {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 16px;
}
.msgs {
  max-width: 720px;
  margin: 0 auto;
  padding-bottom: 12px;
}
.bubble {
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: var(--th-radius-sm);
  line-height: 1.65;
}
.bubble.user {
  background: var(--th-primary-soft);
  margin-left: 40px;
}
.bubble.assistant {
  background: #f3f1ed;
  border: 1px solid var(--th-border);
  margin-right: 40px;
}
.role-label {
  font-size: 12px;
  color: var(--th-text-muted);
  margin-bottom: 6px;
}
.text {
  white-space: pre-wrap;
  word-break: break-word;
}
.cursor {
  animation: blink 1s step-end infinite;
  color: var(--th-primary);
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}
.composer {
  max-width: 720px;
  margin: 12px auto 0;
  width: 100%;
}
.actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style>
