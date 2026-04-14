<template>
  <div class="th-page">
    <h1 class="th-page-title">历史记录</h1>
    <p class="th-page-desc">与 AI 树洞的每一次对话都在这里。</p>

    <!-- 加载状态 -->
    <div v-if="loading" class="th-card">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="!sessions.length" class="th-card">
      <el-empty description="还没有任何对话记录" />
    </div>

    <!-- 会话列表 -->
    <div v-else class="th-card">
      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
        >
          <div class="session-main" @click="goToSession(session.id)">
            <div class="session-info">
              <span class="session-title">{{ session.title || '新对话' }}</span>
              <span class="session-time">{{ formatTime(session.lastMessageAt || session.createTime) }}</span>
            </div>
            <div class="session-actions" @click.stop>
              <el-button
                type="danger"
                size="small"
                text
                :loading="deletingId === session.id"
                @click="handleDelete(session.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSessions, deleteSession } from '../../api/chat'

const router = useRouter()
const loading = ref(false)
const sessions = ref([])
const deletingId = ref(null)

onMounted(() => {
  fetchSessions()
})

async function fetchSessions() {
  loading.value = true
  try {
    const res = await listSessions()
    sessions.value = res.data.data || []
  } catch (e) {
    ElMessage.error('获取会话列表失败')
  } finally {
    loading.value = false
  }
}

function goToSession(sessionId) {
  router.push({ path: '/home', query: { sessionId } })
}

async function handleDelete(sessionId) {
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？删除后无法恢复。', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  deletingId.value = sessionId
  try {
    await deleteSession(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    ElMessage.success('已删除')
  } catch (e) {
    ElMessage.error('删除失败')
  } finally {
    deletingId.value = null
  }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now - d

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`

  return `${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<style scoped>
.session-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.session-item {
  border: 1px solid var(--th-border);
  border-radius: var(--th-radius-sm);
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.session-item:hover {
  box-shadow: 0 2px 12px rgba(61, 58, 54, 0.08);
}

.session-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  cursor: pointer;
  background: var(--th-surface);
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.session-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--th-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}

.session-time {
  font-size: 12px;
  color: var(--th-text-muted);
}

.session-actions {
  flex-shrink: 0;
  margin-left: 12px;
}
</style>
