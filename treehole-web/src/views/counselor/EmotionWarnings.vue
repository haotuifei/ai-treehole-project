<template>
  <div class="th-page">
    <h1 class="th-page-title">情绪预警</h1>

    <!-- 筛选栏 -->
    <div class="th-card filter-bar">
      <el-radio-group v-model="statusFilter" @change="handleFilter">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="PENDING">待处理</el-radio-button>
        <el-radio-button label="PROCESSING">处理中</el-radio-button>
        <el-radio-button label="RESOLVED">已解决</el-radio-button>
        <el-radio-button label="CLOSED">已关闭</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="studentKeyword"
        placeholder="搜索学生姓名/用户名"
        clearable
        style="width: 220px; margin-left: 16px"
        @keyup.enter="handleFilter"
      />
      <el-button type="primary" @click="handleFilter" style="margin-left: 8px">搜索</el-button>
    </div>

    <!-- 预警表格 -->
    <div class="th-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="学生" width="120">
          <template #default="{ row }">
            <div>{{ row.studentName || '-' }}</div>
            <div class="sub-text">{{ row.studentUsername }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column label="风险等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.riskLevel)" size="small">
              {{ riskLabel(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="textSummary" label="触发内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="触发来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ triggerLabel(row.triggerSource) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > pageSize"
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="预警详情" size="600px" destroy-on-close>
      <div v-loading="detailLoading" class="drawer-body">
        <template v-if="detail">
          <!-- 预警信息 -->
          <div class="section">
            <h3 class="section-title">预警信息</h3>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="学生">
                {{ detail.studentName }}（{{ detail.studentUsername }}）
              </el-descriptions-item>
              <el-descriptions-item label="班级">{{ detail.className || '-' }}</el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="riskTagType(detail.riskLevel)" size="small">
                  {{ riskLabel(detail.riskLevel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="触发来源">
                {{ triggerLabel(detail.triggerSource) }}
              </el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <el-tag :type="statusTagType(detail.status)" size="small">
                  {{ statusLabel(detail.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="触发时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="触发内容" :span="2">
                {{ detail.textSummary || '-' }}
              </el-descriptions-item>
              <el-descriptions-item v-if="detail.handleRemark" label="处理备注" :span="2">
                {{ detail.handleRemark }}
              </el-descriptions-item>
              <el-descriptions-item v-if="detail.handledAt" label="处理时间">
                {{ formatTime(detail.handledAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 情绪分析快照 -->
          <div v-if="detail.emotion" class="section">
            <h3 class="section-title">情绪分析快照</h3>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="情绪分值">
                {{ detail.emotion.sentimentScore }}
              </el-descriptions-item>
              <el-descriptions-item label="情绪标签">
                {{ detail.emotion.emotionLabel || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="riskTagType(detail.emotion.riskLevel)" size="small">
                  {{ riskLabel(detail.emotion.riskLevel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="分析时间">
                {{ formatTime(detail.emotion.createTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 干预记录 -->
          <div class="section">
            <h3 class="section-title">干预记录（{{ detail.interventions?.length || 0 }}）</h3>
            <div v-if="detail.interventions?.length" class="intervention-list">
              <div v-for="item in detail.interventions" :key="item.id" class="intervention-item">
                <div class="intervention-head">
                  <span class="intervention-author">{{ item.counselorName || '辅导员' }}</span>
                  <span class="intervention-time">{{ formatTime(item.interventionTime) }}</span>
                </div>
                <div class="intervention-content">{{ item.content }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无干预记录" :image-size="60" />
          </div>

          <!-- 操作区 -->
          <div class="section">
            <h3 class="section-title">处理操作</h3>

            <!-- 添加干预记录 -->
            <div class="action-block">
              <h4>添加干预记录</h4>
              <el-input
                v-model="interventionContent"
                type="textarea"
                :rows="3"
                placeholder="请输入干预内容..."
                maxlength="2000"
                show-word-limit
              />
              <el-button
                type="primary"
                :loading="interventionLoading"
                :disabled="!interventionContent.trim()"
                @click="submitIntervention"
                style="margin-top: 8px"
              >
                提交干预
              </el-button>
            </div>

            <!-- 更新状态 -->
            <div class="action-block">
              <h4>更新处理状态</h4>
              <el-select v-model="newStatus" style="width: 200px">
                <el-option label="待处理" value="PENDING" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="已解决" value="RESOLVED" />
                <el-option label="已关闭" value="CLOSED" />
              </el-select>
              <el-input
                v-model="handleRemark"
                placeholder="处理备注（可选）"
                style="margin-top: 8px"
                maxlength="512"
              />
              <el-button
                type="success"
                :loading="statusLoading"
                :disabled="newStatus === detail.status"
                @click="submitStatus"
                style="margin-top: 8px"
              >
                更新状态
              </el-button>
            </div>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarnings, getWarningDetail, updateWarningStatus, addIntervention } from '../../api/counselor'

// === 列表 ===
const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')
const studentKeyword = ref('')

async function fetchList() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (statusFilter.value) params.status = statusFilter.value
    if (studentKeyword.value.trim()) params.studentKeyword = studentKeyword.value.trim()
    const res = await getWarnings(params)
    const d = res.data.data
    list.value = d.records || []
    total.value = d.total || 0
  } catch {
    ElMessage.error('加载预警列表失败')
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  pageNum.value = 1
  fetchList()
}

function handlePageChange(page) {
  pageNum.value = page
  fetchList()
}

// === 详情抽屉 ===
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const currentWarningId = ref(null)

async function openDetail(id) {
  drawerVisible.value = true
  detailLoading.value = true
  detail.value = null
  currentWarningId.value = id
  interventionContent.value = ''
  handleRemark.value = ''
  try {
    const res = await getWarningDetail(id)
    detail.value = res.data.data
    newStatus.value = detail.value.status
  } catch {
    ElMessage.error('加载预警详情失败')
  } finally {
    detailLoading.value = false
  }
}

// === 干预记录 ===
const interventionContent = ref('')
const interventionLoading = ref(false)

async function submitIntervention() {
  if (!interventionContent.value.trim()) return
  interventionLoading.value = true
  try {
    await addIntervention(currentWarningId.value, {
      content: interventionContent.value.trim()
    })
    ElMessage.success('干预记录已添加')
    interventionContent.value = ''
    // 刷新详情
    const res = await getWarningDetail(currentWarningId.value)
    detail.value = res.data.data
    newStatus.value = detail.value.status
    fetchList()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    interventionLoading.value = false
  }
}

// === 状态更新 ===
const newStatus = ref('')
const handleRemark = ref('')
const statusLoading = ref(false)

async function submitStatus() {
  if (newStatus.value === detail.value.status) return
  statusLoading.value = true
  try {
    await updateWarningStatus(currentWarningId.value, {
      status: newStatus.value,
      handleRemark: handleRemark.value.trim() || undefined
    })
    ElMessage.success('状态已更新')
    const res = await getWarningDetail(currentWarningId.value)
    detail.value = res.data.data
    newStatus.value = detail.value.status
    handleRemark.value = ''
    fetchList()
  } catch {
    ElMessage.error('更新失败')
  } finally {
    statusLoading.value = false
  }
}

// === 工具方法 ===
function riskTagType(level) {
  if (level === 'HIGH') return 'danger'
  if (level === 'MEDIUM') return 'warning'
  return 'success'
}

function riskLabel(level) {
  if (level === 'HIGH') return '高风险'
  if (level === 'MEDIUM') return '中风险'
  return '低风险'
}

function triggerLabel(source) {
  if (source === 'KEYWORD') return '关键词'
  if (source === 'EMOTION') return '情绪分值'
  if (source === 'BOTH') return '综合'
  return source || '-'
}

function statusTagType(st) {
  if (st === 'PENDING') return 'danger'
  if (st === 'PROCESSING') return 'warning'
  if (st === 'RESOLVED') return 'success'
  if (st === 'CLOSED') return 'info'
  return ''
}

function statusLabel(st) {
  const map = { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }
  return map[st] || st
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  padding: 16px 20px;
}

.sub-text {
  font-size: 12px;
  color: var(--th-text-muted);
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 抽屉 */
.drawer-body {
  padding: 0 4px;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--th-text);
  padding-bottom: 8px;
  border-bottom: 1px solid var(--th-border);
}

/* 干预记录 */
.intervention-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.intervention-item {
  padding: 12px;
  background: var(--th-bg);
  border-radius: 8px;
}

.intervention-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}

.intervention-author {
  font-weight: 600;
  color: var(--th-text);
}

.intervention-time {
  color: var(--th-text-muted);
}

.intervention-content {
  font-size: 14px;
  color: var(--th-text);
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 操作区 */
.action-block {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--th-bg);
  border-radius: 8px;
}

.action-block h4 {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--th-text);
}
</style>
