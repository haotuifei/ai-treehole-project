<template>
  <div class="th-page">
    <h1 class="th-page-title">学习打卡</h1>
    <p class="th-page-desc">每日打卡，保持火焰不断。</p>

    <!-- 连续打卡统计 -->
    <div class="streak-section">
      <div class="streak-card main-streak">
        <div class="streak-flame" :class="{ active: streak.currentStreak > 0 }">
          <span class="flame-icon">🔥</span>
          <span class="flame-count">{{ streak.currentStreak }}</span>
        </div>
        <div class="streak-label">连续打卡</div>
      </div>
      <div class="streak-card">
        <div class="streak-value">{{ streak.longestStreak }}</div>
        <div class="streak-label">最长纪录</div>
      </div>
      <div class="streak-card">
        <div class="streak-value">{{ streak.monthDays }}</div>
        <div class="streak-label">本月打卡</div>
      </div>
      <div class="streak-card">
        <div class="streak-value" :class="streak.checkedToday ? 'checked' : 'unchecked'">
          {{ streak.checkedToday ? '✓' : '—' }}
        </div>
        <div class="streak-label">今日状态</div>
      </div>
    </div>

    <!-- 连续打卡提醒 -->
    <div v-if="streak.currentStreak > 0 && !streak.checkedToday" class="streak-warning">
      <span>⚠️ 别让火焰熄灭！你已连续打卡 {{ streak.currentStreak }} 天，今天还没打卡哦</span>
    </div>

    <!-- 进行中的目标 -->
    <div v-if="calendar.goals?.length" class="goals-progress">
      <div class="section-title">进行中的目标</div>
      <div class="goals-row">
        <div v-for="goal in calendar.goals" :key="goal.goalId" class="goal-progress-card">
          <div class="goal-progress-name">{{ goal.goalName }}</div>
          <div class="goal-progress-stats">
            <span>已打卡 {{ goal.totalCheckins }} 天</span>
            <span>{{ formatMinutes(goal.totalMinutes) }}</span>
          </div>
          <div v-if="goal.endDate" class="goal-progress-deadline">
            <span :class="deadlineClass(goal.endDate)">
              {{ deadlineText(goal.endDate) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 日历 -->
    <div class="th-card calendar-card">
      <div class="calendar-header">
        <el-button text @click="prevMonth">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="calendar-title">{{ currentYearMonth.year }}年{{ currentYearMonth.month }}月</span>
        <el-button text @click="nextMonth">
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div class="calendar-weekdays">
        <span v-for="d in weekdays" :key="d">{{ d }}</span>
      </div>

      <div class="calendar-grid">
        <div
          v-for="(cell, i) in calendarCells"
          :key="i"
          class="calendar-cell"
          :class="{
            'other-month': !cell.currentMonth,
            'today': cell.isToday,
            'checked': cell.checked,
            'has-deadline': cell.deadlines?.length
          }"
          @click="handleCellClick(cell)"
        >
          <span class="cell-date">{{ cell.day }}</span>
          <div v-if="cell.checked" class="cell-check">
            <span class="cell-duration">{{ cell.durationMinutes }}m</span>
          </div>
          <div v-if="cell.deadlines?.length" class="cell-deadlines">
            <span v-for="dl in cell.deadlines" :key="dl.goalId" class="cell-deadline-tag">
              {{ dl.goalName }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 打卡弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogDate + ' 打卡'"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="left">
        <el-form-item label="关联目标" prop="goalId">
          <el-select v-model="form.goalId" placeholder="选择目标" style="width: 100%">
            <el-option
              v-for="g in activeGoals"
              :key="g.goalId"
              :label="g.goalName"
              :value="g.goalId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学习时长">
          <el-input-number v-model="form.durationMinutes" :min="0" :max="1440" :step="15" />
          <span class="form-unit">分钟</span>
        </el-form-item>
        <el-form-item label="打卡内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="3"
            placeholder="今天学了什么？"
            maxlength="1024"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="今日心情">
          <div class="mood-selector">
            <span
              v-for="m in moods"
              :key="m.value"
              class="mood-item"
              :class="{ active: form.mood === m.value }"
              @click="form.mood = m.value"
            >
              {{ m.icon }}
            </span>
          </div>
        </el-form-item>
        <el-form-item label="番茄钟">
          <el-switch v-model="form.pomodoroDone" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCheckin">打卡</el-button>
      </template>
    </el-dialog>

    <!-- 查看打卡详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="detailDate + ' 打卡详情'"
      width="420px"
      destroy-on-close
    >
      <div v-if="detailData" class="detail-content">
        <div class="detail-row">
          <span class="detail-label">学习时长</span>
          <span>{{ formatMinutes(detailData.durationMinutes) }}</span>
        </div>
        <div v-if="detailData.content" class="detail-row">
          <span class="detail-label">打卡内容</span>
          <span class="detail-text">{{ detailData.content }}</span>
        </div>
        <div v-if="detailData.mood" class="detail-row">
          <span class="detail-label">今日心情</span>
          <span>{{ detailData.mood }}</span>
        </div>
      </div>
      <el-empty v-else description="加载中..." />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getStreak, getCalendar, createCheckin, listCheckins } from '../../api/checkin'
import { listGoals } from '../../api/goal'

const weekdays = ['一', '二', '三', '四', '五', '六', '日']

const streak = ref({
  currentStreak: 0,
  longestStreak: 0,
  monthDays: 0,
  checkedToday: false
})

const calendar = ref({ days: {}, goals: [] })
const currentMonth = ref(new Date())
const loading = ref(false)

const dialogVisible = ref(false)
const dialogDate = ref('')
const submitting = ref(false)
const formRef = ref(null)
const activeGoals = ref([])

const detailVisible = ref(false)
const detailDate = ref('')
const detailData = ref(null)

const moods = [
  { icon: '😊', value: '😊' },
  { icon: '😐', value: '😐' },
  { icon: '😢', value: '😢' },
  { icon: '😤', value: '😤' },
  { icon: '🔥', value: '🔥' }
]

const form = reactive({
  goalId: null,
  checkDate: null,
  durationMinutes: 60,
  content: '',
  mood: '😊',
  pomodoroDone: 0
})

const rules = {
  goalId: [{ required: true, message: '请选择目标', trigger: 'change' }]
}

const currentYearMonth = computed(() => {
  const d = currentMonth.value
  return {
    year: d.getFullYear(),
    month: d.getMonth() + 1,
    key: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  }
})

const calendarCells = computed(() => {
  const { year, month } = currentYearMonth.value
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)
  const today = new Date()

  // 周一=0, 周日=6
  let startOffset = firstDay.getDay() - 1
  if (startOffset < 0) startOffset = 6

  const cells = []
  // 上月填充
  const prevLast = new Date(year, month - 1, 0)
  for (let i = startOffset - 1; i >= 0; i--) {
    const d = prevLast.getDate() - i
    const dateStr = `${year}-${String(month - 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    cells.push({ day: d, dateStr, currentMonth: false, checked: false })
  }

  // 本月
  for (let d = 1; d <= lastDay.getDate(); d++) {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const dayInfo = calendar.value.days?.[dateStr]
    const isToday = today.getFullYear() === year && today.getMonth() === month - 1 && today.getDate() === d
    const deadlines = getDeadlinesForDate(dateStr)
    cells.push({
      day: d,
      dateStr,
      currentMonth: true,
      isToday,
      checked: dayInfo?.checked || false,
      durationMinutes: dayInfo?.durationMinutes || 0,
      mood: dayInfo?.mood,
      checkinId: dayInfo?.checkinId,
      deadlines
    })
  }

  // 下月填充
  const remaining = 42 - cells.length
  for (let d = 1; d <= remaining; d++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    cells.push({ day: d, dateStr, currentMonth: false, checked: false })
  }

  return cells
})

onMounted(() => {
  fetchData()
})

watch(currentMonth, () => {
  fetchCalendar()
})

function getDeadlinesForDate(dateStr) {
  const goals = calendar.value.goals || []
  const deadlines = []
  for (const g of goals) {
    if (g.endDate === dateStr) {
      deadlines.push({ goalId: g.goalId, goalName: g.goalName, type: 'end' })
    }
    if (g.startDate === dateStr) {
      deadlines.push({ goalId: g.goalId, goalName: g.goalName, type: 'start' })
    }
  }
  return deadlines.length ? deadlines : null
}

async function fetchData() {
  loading.value = true
  try {
    const [streakRes, calendarRes] = await Promise.all([
      getStreak(),
      getCalendar(currentYearMonth.value.key)
    ])
    streak.value = streakRes.data.data || streak.value
    calendar.value = calendarRes.data.data || calendar.value
  } catch {
    ElMessage.error('获取打卡数据失败')
  } finally {
    loading.value = false
  }
}

async function fetchCalendar() {
  try {
    const res = await getCalendar(currentYearMonth.value.key)
    calendar.value = res.data.data || { days: {}, goals: [] }
  } catch {
    ElMessage.error('获取月历数据失败')
  }
}

function prevMonth() {
  const d = currentMonth.value
  currentMonth.value = new Date(d.getFullYear(), d.getMonth() - 1, 1)
}

function nextMonth() {
  const d = currentMonth.value
  currentMonth.value = new Date(d.getFullYear(), d.getMonth() + 1, 1)
}

async function handleCellClick(cell) {
  if (!cell.currentMonth) return
  if (cell.checked) {
    // 查看详情
    detailDate.value = cell.dateStr
    try {
      const res = await listCheckins({ goalId: getFirstGoalId(), pageNum: 1, pageSize: 1, dateFrom: cell.dateStr, dateTo: cell.dateStr })
      detailData.value = res.data.data?.records?.[0] || null
    } catch {
      detailData.value = null
    }
    detailVisible.value = true
  } else {
    // 打卡
    openCheckin(cell.dateStr)
  }
}

function getFirstGoalId() {
  return calendar.value.goals?.[0]?.goalId || 0
}

async function openCheckin(dateStr) {
  dialogDate.value = dateStr
  form.checkDate = dateStr
  form.goalId = null
  form.durationMinutes = 60
  form.content = ''
  form.mood = '😊'
  form.pomodoroDone = 0

  // 加载进行中的目标
  try {
    const res = await listGoals(0)
    activeGoals.value = (res.data.data || []).map(g => ({
      goalId: g.id,
      goalName: g.goalName
    }))
    if (activeGoals.value.length === 1) {
      form.goalId = activeGoals.value[0].goalId
    }
  } catch {
    activeGoals.value = []
  }

  dialogVisible.value = true
}

async function handleCheckin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createCheckin({
      goalId: form.goalId,
      checkDate: form.checkDate,
      durationMinutes: form.durationMinutes,
      content: form.content || null,
      mood: form.mood || null,
      pomodoroDone: form.pomodoroDone
    })
    ElMessage.success('打卡成功！🔥')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '打卡失败')
  } finally {
    submitting.value = false
  }
}

function formatMinutes(min) {
  if (!min) return '0分钟'
  if (min < 60) return `${min}分钟`
  const h = Math.floor(min / 60)
  const m = min % 60
  return m > 0 ? `${h}小时${m}分` : `${h}小时`
}

function deadlineText(dateStr) {
  const target = new Date(dateStr)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const diff = Math.ceil((target - today) / 86400000)
  if (diff < 0) return '已到期'
  if (diff === 0) return '今天截止'
  return `D-${diff}`
}

function deadlineClass(dateStr) {
  const target = new Date(dateStr)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const diff = Math.ceil((target - today) / 86400000)
  if (diff < 0) return 'deadline-passed'
  if (diff <= 3) return 'deadline-urgent'
  if (diff <= 7) return 'deadline-soon'
  return 'deadline-normal'
}
</script>

<style scoped>
.streak-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.streak-card {
  background: var(--th-card);
  border: 1px solid var(--th-border);
  border-radius: var(--th-radius);
  padding: 16px;
  text-align: center;
  box-shadow: var(--th-shadow);
}

.main-streak {
  background: linear-gradient(135deg, rgba(255, 120, 0, 0.06), rgba(255, 80, 0, 0.04));
  border-color: rgba(255, 120, 0, 0.2);
}

.streak-flame {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.flame-icon {
  font-size: 1.8rem;
}

.flame-icon {
  display: inline-block;
  animation: none;
}

.streak-flame.active .flame-icon {
  animation: flicker 1.5s ease-in-out infinite;
}

@keyframes flicker {
  0%, 100% { transform: scale(1) rotate(0deg); }
  25% { transform: scale(1.1) rotate(-3deg); }
  50% { transform: scale(1.05) rotate(2deg); }
  75% { transform: scale(1.08) rotate(-1deg); }
}

.flame-count {
  font-size: 2rem;
  font-weight: 700;
  color: #ff6b00;
}

.streak-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--th-text);
}

.streak-value.checked {
  color: #27ae60;
}

.streak-value.unchecked {
  color: var(--th-text-muted);
}

.streak-label {
  font-size: 0.82rem;
  color: var(--th-text-muted);
  margin-top: 4px;
}

.streak-warning {
  background: rgba(255, 120, 0, 0.08);
  border: 1px solid rgba(255, 120, 0, 0.2);
  border-radius: var(--th-radius-sm);
  padding: 10px 16px;
  margin-bottom: 16px;
  font-size: 0.9rem;
  color: #b35900;
}

.section-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--th-text);
  margin-bottom: 10px;
}

.goals-progress {
  margin-bottom: 16px;
}

.goals-row {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.goal-progress-card {
  flex-shrink: 0;
  min-width: 180px;
  background: var(--th-card);
  border: 1px solid var(--th-border);
  border-radius: var(--th-radius-sm);
  padding: 12px 14px;
}

.goal-progress-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--th-text);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goal-progress-stats {
  display: flex;
  gap: 12px;
  font-size: 0.78rem;
  color: var(--th-text-muted);
  margin-bottom: 4px;
}

.goal-progress-deadline {
  font-size: 0.78rem;
  font-weight: 500;
}

.deadline-passed { color: #999; }
.deadline-urgent { color: #e74c3c; font-weight: 600; }
.deadline-soon { color: #f39c12; }
.deadline-normal { color: var(--th-text-muted); }

/* 日历 */
.calendar-card {
  padding: 20px;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.calendar-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--th-text);
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-size: 0.78rem;
  color: var(--th-text-muted);
  margin-bottom: 8px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: var(--th-radius-sm);
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
  min-height: 56px;
  gap: 2px;
}

.calendar-cell:hover {
  background: var(--th-primary-soft);
}

.calendar-cell.other-month {
  opacity: 0.3;
  cursor: default;
}

.calendar-cell.other-month:hover {
  background: transparent;
}

.calendar-cell.today {
  border: 2px solid var(--th-primary);
  font-weight: 600;
}

.calendar-cell.checked {
  background: rgba(39, 174, 96, 0.08);
}

.cell-date {
  font-size: 0.85rem;
  color: var(--th-text);
}

.cell-check {
  display: flex;
  align-items: center;
  gap: 2px;
}

.cell-duration {
  font-size: 0.65rem;
  color: #27ae60;
  font-weight: 500;
}

.cell-deadlines {
  display: flex;
  flex-direction: column;
  gap: 1px;
  max-width: 100%;
  overflow: hidden;
}

.cell-deadline-tag {
  font-size: 0.55rem;
  background: rgba(255, 120, 0, 0.15);
  color: #b35900;
  padding: 0 3px;
  border-radius: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 心情选择 */
.mood-selector {
  display: flex;
  gap: 12px;
}

.mood-item {
  font-size: 1.6rem;
  cursor: pointer;
  opacity: 0.4;
  transition: all 0.15s;
}

.mood-item:hover {
  opacity: 0.7;
  transform: scale(1.15);
}

.mood-item.active {
  opacity: 1;
  transform: scale(1.2);
}

.form-unit {
  margin-left: 8px;
  font-size: 0.85rem;
  color: var(--th-text-muted);
}

/* 详情弹窗 */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-row {
  display: flex;
  gap: 12px;
}

.detail-label {
  flex-shrink: 0;
  width: 70px;
  font-size: 0.85rem;
  color: var(--th-text-muted);
}

.detail-text {
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 768px) {
  .streak-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .calendar-cell {
    min-height: 44px;
  }

  .cell-deadline-tag {
    display: none;
  }
}
</style>
