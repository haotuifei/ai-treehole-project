<template>
  <div class="th-page">
    <h1 class="th-page-title">设置</h1>
    <p class="th-page-desc">管理当前账号、AI 名字、人设和个性化回复风格。</p>

    <div class="settings-layout">
      <div class="settings-sidebar">
        <div class="th-card settings-panel profile-panel">
          <div class="panel-head">
            <div>
              <div class="section-title">账号</div>
              <div class="section-desc">当前登录信息与账号操作</div>
            </div>
            <div class="profile-badge">已登录</div>
          </div>

          <div class="profile-summary">
            <div class="avatar-soft">{{ (user.profile?.realName || user.profile?.username || '树').slice(0, 1) }}</div>
            <div class="profile-meta">
              <div class="profile-name">{{ user.profile?.realName || user.profile?.username || '未登录用户' }}</div>
              <div class="profile-role">{{ roleText }}</div>
            </div>
          </div>

          <div class="account-list">
            <div class="account-item">
              <span class="label">用户名</span>
              <span class="value">{{ user.profile?.username || '—' }}</span>
            </div>
            <div class="account-item">
              <span class="label">姓名</span>
              <span class="value">{{ user.profile?.realName || '—' }}</span>
            </div>
            <div class="account-item">
              <span class="label">角色</span>
              <span class="value">{{ roleText }}</span>
            </div>
          </div>

          <div class="logout-box">
            <div class="logout-title">账号安全</div>
            <div class="logout-desc">退出后将返回登录页，需要重新登录才能继续使用。</div>
            <el-button type="danger" plain round @click="onLogout">退出登录</el-button>
          </div>
        </div>

        <div class="th-card settings-panel preview-panel">
          <div class="panel-head compact">
            <div>
              <div class="section-title">对话预览</div>
              <div class="section-desc">展示当前设置在聊天页的大致效果</div>
            </div>
          </div>

          <div class="preview-chat">
            <div class="preview-chat-item user">
              <div class="preview-chat-role">我</div>
              <div class="preview-chat-bubble user-bubble">{{ previewUserMessage }}</div>
            </div>

            <div class="preview-chat-item assistant">
              <div class="preview-chat-role">{{ form.aiName || '树洞陪伴者' }}</div>
              <div class="preview-chat-bubble assistant-bubble">
                <div class="preview-bubble-title">{{ previewSubtitle }}</div>
                <div class="preview-bubble-text">{{ previewAssistantMessage }}</div>
              </div>
            </div>
          </div>

          <div class="preview-tags">
            <span class="preview-tag">{{ toneLabel }}</span>
            <span class="preview-tag">{{ replyLengthLabel }}</span>
            <span class="preview-tag">{{ form.proactiveCare ? '主动关怀' : '减少追问' }}</span>
            <span class="preview-tag">{{ form.useEmoji ? '可少量表情' : '纯文本表达' }}</span>
          </div>
        </div>
      </div>

      <div class="th-card settings-panel editor-panel">
        <div class="panel-head">
          <div>
            <div class="section-title">AI 个性化</div>
            <div class="section-desc">调整名称、语气、人设和互动方式，让陪伴更贴近你的偏好。</div>
          </div>
        </div>

        <el-form label-position="top" class="settings-form">
          <div class="field-block">
            <div class="field-block-title">基础设定</div>
            <div class="field-grid">
              <el-form-item label="AI 名字">
                <el-input v-model="form.aiName" maxlength="20" show-word-limit placeholder="例如：小树、木木、心语" />
              </el-form-item>

              <el-form-item label="用户称呼">
                <el-input v-model="form.userNickname" maxlength="20" show-word-limit placeholder="例如：小林、同学、阿宁" />
              </el-form-item>
            </div>
          </div>

          <div class="field-block">
            <div class="field-block-title">角色人设</div>
            <el-form-item label="AI 人设">
              <el-input
                v-model="form.persona"
                type="textarea"
                :rows="5"
                maxlength="200"
                show-word-limit
                placeholder="例如：像一个温柔耐心的学长，擅长倾听、安抚情绪，也会给出轻量建议。"
              />
            </el-form-item>
          </div>

          <div class="field-block">
            <div class="field-block-title">回复风格</div>
            <div class="field-grid">
              <el-form-item label="回复语气">
                <el-select v-model="form.tone">
                  <el-option label="温柔陪伴" value="gentle" />
                  <el-option label="理性清晰" value="rational" />
                  <el-option label="轻快鼓励" value="lively" />
                </el-select>
              </el-form-item>

              <el-form-item label="回复长度">
                <el-select v-model="form.replyLength">
                  <el-option label="简短" value="short" />
                  <el-option label="适中" value="balanced" />
                  <el-option label="详细" value="detailed" />
                </el-select>
              </el-form-item>
            </div>
          </div>

          <div class="field-block">
            <div class="field-block-title">互动偏好</div>
            <div class="switch-row">
              <div class="switch-item">
                <div class="switch-copy">
                  <div class="switch-title">允许少量表情</div>
                  <div class="switch-desc">让语气更轻松一点，但仍保持自然克制。</div>
                </div>
                <el-switch v-model="form.useEmoji" />
              </div>

              <div class="switch-item">
                <div class="switch-copy">
                  <div class="switch-title">主动追问关怀</div>
                  <div class="switch-desc">在合适时多问一句，帮助你继续表达心情与想法。</div>
                </div>
                <el-switch v-model="form.proactiveCare" />
              </div>
            </div>
          </div>
        </el-form>

        <div class="editor-actions">
          <div class="actions-note">保存后，新开启的对话会立即使用这些设置。</div>
          <div class="actions-group">
            <el-button round @click="onReset">恢复默认</el-button>
            <el-button type="primary" round @click="onSave">保存设置</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { useAiPreferencesStore } from '../../stores/aiPreferences'

const router = useRouter()
const user = useUserStore()
const preferences = useAiPreferencesStore()

const form = reactive({
  aiName: preferences.aiName,
  userNickname: preferences.userNickname,
  persona: preferences.persona,
  tone: preferences.tone,
  replyLength: preferences.replyLength,
  useEmoji: preferences.useEmoji,
  proactiveCare: preferences.proactiveCare
})

const roleText = computed(() => {
  const names = user.profile?.roleNames || []
  if (names.length) return names.join('、')
  const codes = user.profile?.roleCodes || []
  return codes.length ? codes.join('、') : '—'
})

const previewSubtitle = computed(() => {
  const map = {
    gentle: '慢慢说，我会认真听。',
    rational: '我们可以把感受和问题一起理清。',
    lively: '我会陪你把情绪一点点说开。'
  }
  return map[form.tone] || map.gentle
})

const toneLabel = computed(() => {
  const map = {
    gentle: '温柔陪伴',
    rational: '理性清晰',
    lively: '轻快鼓励'
  }
  return map[form.tone] || '温柔陪伴'
})

const replyLengthLabel = computed(() => {
  const map = {
    short: '简短回复',
    balanced: '适中长度',
    detailed: '详细表达'
  }
  return map[form.replyLength] || '适中长度'
})

const previewUserMessage = computed(() => {
  const name = (form.userNickname || '').trim()
  if (name) return `${name}这两天有点累，想找人聊聊。`
  return '这两天有点累，想找人聊聊。'
})

const previewAssistantMessage = computed(() => {
  const name = (form.userNickname || '').trim()
  const emoji = form.useEmoji ? ' ' + (form.tone === 'lively' ? '✨' : '🙂') : ''
  const greeting = name ? `${name}，` : ''
  const careTail = form.proactiveCare ? ' 如果你愿意，也可以和我说说最近最让你累的是哪一件事。' : ''

  if (form.tone === 'rational') {
    if (form.replyLength === 'short') {
      return `${greeting}先别急，我们可以先把让你疲惫的事情理一理。${emoji}${careTail}`
    }
    if (form.replyLength === 'detailed') {
      return `${greeting}听起来你最近一直在消耗自己。我们可以先分成两部分来看：是事情太多、节奏太快，还是情绪上一直没机会放松？你不用一次讲很多，先说最想解决的一点就好。${emoji}${careTail}`
    }
    return `${greeting}听起来你最近有些透支了。我们可以先一起分辨一下，是压力堆积，还是单纯没休息好。${emoji}${careTail}`
  }

  if (form.tone === 'lively') {
    if (form.replyLength === 'short') {
      return `${greeting}辛苦啦，我在这儿陪你缓一缓。${emoji}${careTail}`
    }
    if (form.replyLength === 'detailed') {
      return `${greeting}最近真的撑了很久吧，先抱抱你一下。你不用急着把状态调整好，能来到这里说一句“我累了”已经很不容易了。我们可以慢一点，把心里最堵的那件事先拿出来说。${emoji}${careTail}`
    }
    return `${greeting}辛苦啦，先在我这里歇一会儿。你可以慢慢说，我会陪你把这些累一点点捋开。${emoji}${careTail}`
  }

  if (form.replyLength === 'short') {
    return `${greeting}辛苦你了，我在听。${emoji}${careTail}`
  }
  if (form.replyLength === 'detailed') {
    return `${greeting}感觉你已经默默扛了不少事情。先不用逼自己马上振作起来，你可以把这里当成一个安全一点的小角落，想到什么就说什么。我会认真听，也会陪你一起把那些压在心里的东西慢慢放下来。${emoji}${careTail}`
  }
  return `${greeting}感觉你最近真的有点累了，我在这里陪你慢慢说。${emoji}${careTail}`
})

function normalizeForm() {
  return {
    aiName: (form.aiName || '').trim() || '树洞陪伴者',
    userNickname: (form.userNickname || '').trim(),
    persona: (form.persona || '').trim() || '温柔、耐心、共情，像一个会认真倾听的朋友。',
    tone: form.tone || 'gentle',
    replyLength: form.replyLength || 'balanced',
    useEmoji: Boolean(form.useEmoji),
    proactiveCare: Boolean(form.proactiveCare)
  }
}

function onSave() {
  preferences.patchSettings(normalizeForm())
  ElMessage.success('设置已保存，新的对话会立即生效')
}

function onReset() {
  preferences.resetSettings()
  Object.assign(form, preferences.settings)
  ElMessage.success('已恢复默认设置')
}

async function onLogout() {
  try {
    await ElMessageBox.confirm('确认退出当前账号吗？', '退出登录', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  user.logout()
  router.push('/login')
}

onMounted(() => {
  if (!user.profile && user.isLoggedIn) {
    user.fetchProfile()
  }
})
</script>

<style scoped>
.settings-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.settings-sidebar {
  display: grid;
  gap: 20px;
}

.settings-panel {
  padding: 24px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
}

.panel-head.compact {
  margin-bottom: 16px;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--th-text);
}

.section-desc {
  margin-top: 6px;
  font-size: 0.88rem;
  line-height: 1.6;
  color: var(--th-text-muted);
}

.profile-badge {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(124, 154, 124, 0.12);
  color: #6f8d6f;
  font-size: 0.8rem;
  white-space: nowrap;
}

.profile-summary {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(124, 154, 124, 0.08), rgba(196, 167, 125, 0.1));
  margin-bottom: 18px;
}

.avatar-soft {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--th-text);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(232, 228, 222, 0.95);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);
}

.profile-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--th-text);
}

.profile-role {
  margin-top: 4px;
  font-size: 0.88rem;
  color: var(--th-text-muted);
}

.account-list {
  display: grid;
  gap: 10px;
}

.account-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(232, 228, 222, 0.85);
}

.label {
  color: var(--th-text-muted);
}

.value {
  color: var(--th-text);
  font-weight: 500;
  text-align: right;
}

.logout-box {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px dashed var(--th-border);
}

.logout-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--th-text);
}

.logout-desc {
  margin: 6px 0 14px;
  font-size: 0.86rem;
  line-height: 1.6;
  color: var(--th-text-muted);
}

.preview-panel {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 252, 248, 0.98));
}

.preview-chat {
  display: grid;
  gap: 14px;
}

.preview-chat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-chat-item.user {
  align-items: flex-end;
}

.preview-chat-item.assistant {
  align-items: flex-start;
}

.preview-chat-role {
  font-size: 0.78rem;
  color: var(--th-text-muted);
}

.preview-chat-bubble {
  max-width: 92%;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(232, 228, 222, 0.9);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.user-bubble {
  background: rgba(255, 255, 255, 0.9);
  border-bottom-right-radius: 6px;
  color: var(--th-text);
}

.assistant-bubble {
  background: linear-gradient(135deg, rgba(124, 154, 124, 0.07), rgba(255, 255, 255, 0.94));
  border-bottom-left-radius: 6px;
}

.preview-bubble-title {
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--th-text);
}

.preview-bubble-text {
  margin-top: 8px;
  font-size: 0.9rem;
  line-height: 1.75;
  color: var(--th-text);
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.preview-tag {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(232, 228, 222, 0.95);
  color: var(--th-text-muted);
  font-size: 0.8rem;
}

.settings-form {
  margin-top: 4px;
}

.field-block + .field-block {
  margin-top: 22px;
}

.field-block-title {
  margin-bottom: 14px;
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--th-text);
}

.field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.editor-panel :deep(.el-form-item) {
  margin-bottom: 0;
}

.editor-panel :deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: var(--th-text-muted);
}

.editor-panel :deep(.el-input__wrapper),
.editor-panel :deep(.el-textarea__inner),
.editor-panel :deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.74);
  border-radius: 14px;
  box-shadow: none;
  border: 1px solid transparent;
}

.editor-panel :deep(.el-input__wrapper.is-focus),
.editor-panel :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px rgba(124, 154, 124, 0.18);
}

.editor-panel :deep(.el-textarea__inner) {
  min-height: 124px !important;
  padding: 14px 16px;
  line-height: 1.7;
}

.switch-row {
  display: grid;
  gap: 12px;
}

.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid rgba(232, 228, 222, 0.9);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.65);
}

.switch-copy {
  min-width: 0;
}

.switch-title {
  color: var(--th-text);
  font-weight: 500;
}

.switch-desc {
  margin-top: 4px;
  color: var(--th-text-muted);
  font-size: 0.85rem;
  line-height: 1.6;
}

.editor-actions {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid rgba(232, 228, 222, 0.9);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.actions-note {
  color: var(--th-text-muted);
  font-size: 0.88rem;
}

.actions-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 960px) {
  .settings-layout {
    grid-template-columns: 1fr;
  }

  .settings-sidebar {
    grid-template-columns: 1fr 1fr;
    align-items: start;
  }
}

@media (max-width: 768px) {
  .settings-panel {
    padding: 20px;
  }

  .settings-sidebar {
    grid-template-columns: 1fr;
  }

  .field-grid {
    grid-template-columns: 1fr;
  }

  .switch-item {
    align-items: flex-start;
  }

  .editor-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .actions-group {
    width: 100%;
  }

  .actions-group :deep(.el-button) {
    flex: 1;
  }
}
</style>
