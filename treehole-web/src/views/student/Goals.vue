<template>
  <div class="th-page">
    <div class="goals-header">
      <div>
        <h1 class="th-page-title">目标管理</h1>
        <p class="th-page-desc">设定备考目标，追踪学习进度。</p>
      </div>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新建目标
      </el-button>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-tabs">
      <el-radio-group v-model="filterStatus" @change="fetchGoals">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">进行中</el-radio-button>
        <el-radio-button :value="1">已完成</el-radio-button>
        <el-radio-button :value="2">已暂停</el-radio-button>
        <el-radio-button :value="3">已放弃</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="th-card">
      <el-skeleton :rows="4" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="!goals.length" class="th-card">
      <el-empty description="还没有任何目标，点击上方按钮新建一个吧" />
    </div>

    <!-- 目标列表 -->
    <div v-else class="goals-grid">
      <div v-for="goal in goals" :key="goal.id" class="goal-card th-card">
        <div class="goal-card-header">
          <div class="goal-title-row">
            <span class="goal-name">{{ goal.goalName }}</span>
            <el-tag :type="statusTagType(goal.status)" size="small" effect="plain">
              {{ goal.statusLabel }}
            </el-tag>
          </div>
          <el-tag :type="typeTagType(goal.goalType)" size="small" effect="dark">
            {{ goal.goalTypeLabel }}
          </el-tag>
        </div>

        <div v-if="goal.startDate || goal.endDate" class="goal-dates">
          <el-icon><Calendar /></el-icon>
          <span>{{ goal.startDate || '—' }} ~ {{ goal.endDate || '—' }}</span>
        </div>

        <div v-if="goal.description" class="goal-desc">{{ goal.description }}</div>

        <div v-if="goal.remark" class="goal-remark">
          <el-icon><Document /></el-icon>
          {{ goal.remark }}
        </div>

        <div v-if="goal._stats" class="goal-stats">
          <span class="goal-stat">📅 已打卡 {{ goal._stats.days }} 天</span>
          <span class="goal-stat">⏱ {{ formatMinutes(goal._stats.minutes) }}</span>
          <span v-if="goal.endDate" class="goal-stat" :class="deadlineClass(goal.endDate)">
            {{ deadlineText(goal.endDate) }}
          </span>
        </div>

        <div class="goal-actions">
          <div class="goal-status-actions">
            <el-button
              v-if="goal.status === 2"
              size="small"
              text
              type="success"
              @click="changeStatus(goal, 0)"
            >
              继续
            </el-button>
            <el-button
              v-if="goal.status === 0"
              size="small"
              text
              type="warning"
              @click="changeStatus(goal, 2)"
            >
              暂停
            </el-button>
            <el-button
              v-if="goal.status === 0 || goal.status === 2"
              size="small"
              text
              type="primary"
              @click="changeStatus(goal, 1)"
            >
              完成
            </el-button>
            <el-button
              v-if="goal.status !== 3"
              size="small"
              text
              type="info"
              @click="changeStatus(goal, 3)"
            >
              放弃
            </el-button>
          </div>
          <div class="goal-edit-actions">
            <el-button size="small" text @click="openEdit(goal)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(goal)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑目标' : '新建目标'"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        label-position="left"
      >
        <el-form-item label="目标名称" prop="goalName">
          <el-input v-model="form.goalName" placeholder="如：2026考研政治" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="目标类型" prop="goalType">
          <el-select v-model="form.goalType" placeholder="选择类型" style="width: 100%">
            <el-option label="考研" value="POSTGRAD" />
            <el-option label="考公" value="CIVIL_SERVICE" />
            <el-option label="课程" value="COURSE" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="截止日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="目标描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="描述你的目标计划"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="补充说明"
            maxlength="512"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Calendar, Document } from '@element-plus/icons-vue'
import { listGoals, createGoal, updateGoal, deleteGoal } from '../../api/goal'
import { listCheckins } from '../../api/checkin'

const loading = ref(false)
const goals = ref([])
const filterStatus = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({
  goalName: '',
  goalType: 'CUSTOM',
  dateRange: null,
  description: '',
  remark: ''
})

const rules = {
  goalName: [{ required: true, message: '请输入目标名称', trigger: 'blur' }],
  goalType: [{ required: true, message: '请选择目标类型', trigger: 'change' }]
}

onMounted(() => {
  fetchGoals()
})

async function fetchGoals() {
  loading.value = true
  try {
    const res = await listGoals(filterStatus.value)
    goals.value = res.data.data || []
    // 加载每个目标的打卡统计
    for (const goal of goals.value) {
      try {
        const cr = await listCheckins({ goalId: goal.id, pageNum: 1, pageSize: 1 })
        const total = cr.data.data?.total || 0
        goal._stats = { days: total, minutes: 0 }
        // 如果有记录，获取总时长（通过分页查询不到，用简单方式）
      } catch {
        goal._stats = { days: 0, minutes: 0 }
      }
    }
  } catch {
    ElMessage.error('获取目标列表失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(goal) {
  isEdit.value = true
  editingId.value = goal.id
  form.goalName = goal.goalName
  form.goalType = goal.goalType
  form.dateRange = (goal.startDate || goal.endDate) ? [goal.startDate, goal.endDate] : null
  form.description = goal.description || ''
  form.remark = goal.remark || ''
  dialogVisible.value = true
}

function resetForm() {
  form.goalName = ''
  form.goalType = 'CUSTOM'
  form.dateRange = null
  form.description = ''
  form.remark = ''
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      goalName: form.goalName.trim(),
      goalType: form.goalType,
      startDate: form.dateRange?.[0] || null,
      endDate: form.dateRange?.[1] || null,
      description: form.description || null,
      remark: form.remark || null,
      status: 0
    }

    if (isEdit.value) {
      await updateGoal(editingId.value, payload)
      ElMessage.success('目标已更新')
    } else {
      await createGoal(payload)
      ElMessage.success('目标已创建')
    }
    dialogVisible.value = false
    fetchGoals()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

async function handleDelete(goal) {
  try {
    await ElMessageBox.confirm(
      `确定要删除目标「${goal.goalName}」吗？删除后无法恢复。`,
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  try {
    await deleteGoal(goal.id)
    ElMessage.success('已删除')
    fetchGoals()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function changeStatus(goal, newStatus) {
  try {
    await updateGoal(goal.id, { status: newStatus })
    ElMessage.success('状态已更新')
    fetchGoals()
  } catch {
    ElMessage.error('状态更新失败')
  }
}

function statusTagType(status) {
  switch (status) {
    case 0: return 'primary'
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'info'
    default: return ''
  }
}

function typeTagType(type) {
  switch (type) {
    case 'POSTGRAD': return 'danger'
    case 'CIVIL_SERVICE': return 'warning'
    case 'COURSE': return 'success'
    default: return 'info'
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
.goals-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.filter-tabs {
  margin-bottom: 20px;
}

.goals-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.goal-card {
  padding: 20px;
}

.goal-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.goal-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.goal-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--th-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goal-dates {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--th-text-muted);
  margin-bottom: 8px;
}

.goal-desc {
  font-size: 14px;
  color: var(--th-text);
  line-height: 1.6;
  margin-bottom: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

.goal-remark {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 13px;
  color: var(--th-text-muted);
  margin-bottom: 12px;
}

.goal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid var(--th-border);
}

.goal-status-actions,
.goal-edit-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.goal-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--th-border);
}

.goal-stat {
  font-size: 0.82rem;
  color: var(--th-text-muted);
}

.goal-stat.deadline-passed { color: #999; }
.goal-stat.deadline-urgent { color: #e74c3c; font-weight: 600; }
.goal-stat.deadline-soon { color: #f39c12; }
.goal-stat.deadline-normal { color: var(--th-text-muted); }

@media (max-width: 768px) {
  .goals-header {
    flex-direction: column;
  }

  .goal-card-header {
    flex-direction: column;
    gap: 8px;
  }

  .goal-actions {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style>
