<template>
  <div class="th-page">
    <h1 class="th-page-title">学生档案</h1>

    <!-- 搜索栏 -->
    <div class="th-card search-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索姓名、学号、用户名"
        clearable
        style="width: 280px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 学生表格 -->
    <div class="th-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="情绪评分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.lastEmotionScore != null" :class="scoreClass(row.lastEmotionScore)">
              {{ row.lastEmotionScore }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningCount" label="预警" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.warningCount > 0" type="danger" size="small">{{ row.warningCount }}</el-tag>
            <span v-else class="text-muted">0</span>
          </template>
        </el-table-column>
        <el-table-column prop="emotionCount" label="情绪记录" width="90" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > query.pageSize"
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="学生档案详情" size="560px" destroy-on-close>
      <div v-loading="detailLoading" class="drawer-body">
        <template v-if="detail">
          <!-- 基本信息 -->
          <div class="section">
            <h3 class="section-title">基本信息</h3>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="姓名">{{ detail.realName }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
              <el-descriptions-item label="学号">{{ detail.studentNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="班级">{{ detail.className || '-' }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ detail.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ detail.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="detail.status === 1 ? 'success' : 'info'" size="small">
                  {{ detail.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 情绪统计 -->
          <div class="section">
            <h3 class="section-title">情绪概况</h3>
            <div class="stat-row">
              <div class="stat-item">
                <div class="stat-value">{{ detail.avgEmotionScore ?? '-' }}</div>
                <div class="stat-label">情绪均分</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ detail.emotionTotalCount ?? 0 }}</div>
                <div class="stat-label">情绪记录</div>
              </div>
              <div class="stat-item">
                <div class="stat-value danger">{{ detail.highRiskCount ?? 0 }}</div>
                <div class="stat-label">高风险次数</div>
              </div>
            </div>
            <div ref="riskPieEl" class="chart-box" />
            <div v-if="detail.recentEmotions?.length" class="recent-list">
              <h4>最近情绪记录</h4>
              <div v-for="e in detail.recentEmotions" :key="e.id" class="emotion-item">
                <el-tag :type="riskTagType(e.riskLevel)" size="small">{{ riskLabel(e.riskLevel) }}</el-tag>
                <span class="emotion-label">{{ e.emotionLabel || '-' }}</span>
                <span class="emotion-score">分值 {{ e.sentimentScore }}</span>
                <span class="emotion-time">{{ formatTime(e.createTime) }}</span>
              </div>
            </div>
          </div>

          <!-- 预警统计 -->
          <div class="section">
            <h3 class="section-title">预警概况</h3>
            <div class="stat-row">
              <div class="stat-item">
                <div class="stat-value danger">{{ detail.warningTotalCount ?? 0 }}</div>
                <div class="stat-label">预警总数</div>
              </div>
              <div class="stat-item" v-for="(cnt, st) in detail.warningStatusDistribution" :key="st">
                <div class="stat-value">{{ cnt }}</div>
                <div class="stat-label">{{ warningStatusLabel(st) }}</div>
              </div>
            </div>
          </div>

          <!-- 学习统计 -->
          <div class="section">
            <h3 class="section-title">学习概况</h3>
            <div class="stat-row">
              <div class="stat-item">
                <div class="stat-value">{{ detail.goalTotalCount ?? 0 }}</div>
                <div class="stat-label">目标总数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ detail.goalActiveCount ?? 0 }}</div>
                <div class="stat-label">进行中</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ detail.checkinTotalDays ?? 0 }}</div>
                <div class="stat-label">累计打卡天</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ detail.checkinLast7Days ?? 0 }}</div>
                <div class="stat-label">近7天打卡</div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getStudents, getStudentDetail } from '../../api/counselor'

// === 列表 ===
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await getStudents(query)
    const d = res.data.data
    list.value = d.records || []
    total.value = d.total || 0
  } catch {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchList()
}

function handlePageChange(page) {
  query.pageNum = page
  fetchList()
}

// === 详情抽屉 ===
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const riskPieEl = ref(null)
let riskPieChart = null

async function openDetail(id) {
  drawerVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await getStudentDetail(id)
    detail.value = res.data.data
    await nextTick()
    renderRiskPie()
  } catch {
    ElMessage.error('加载学生详情失败')
  } finally {
    detailLoading.value = false
  }
}

function renderRiskPie() {
  const d = detail.value
  if (!riskPieEl.value || !d?.riskLevelDistribution) return
  if (riskPieChart) riskPieChart.dispose()
  riskPieChart = echarts.init(riskPieEl.value)
  const data = [
    { name: '低风险', value: d.riskLevelDistribution.LOW || 0 },
    { name: '中风险', value: d.riskLevelDistribution.MEDIUM || 0 },
    { name: '高风险', value: d.riskLevelDistribution.HIGH || 0 }
  ]
  riskPieChart.setOption({
    color: ['#67C23A', '#E6A23C', '#F56C6C'],
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}条' },
      data
    }]
  })
}

function onResize() {
  riskPieChart?.resize()
}

// === 工具方法 ===
function scoreClass(score) {
  if (score == null) return ''
  if (score < 40) return 'score-low'
  if (score < 70) return 'score-mid'
  return 'score-high'
}

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

function warningStatusLabel(st) {
  const map = { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }
  return map[st] || st
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchList()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  riskPieChart?.dispose()
})
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px 20px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.text-muted {
  color: var(--th-text-muted);
}

.score-low {
  color: #F56C6C;
  font-weight: 600;
}

.score-mid {
  color: #E6A23C;
  font-weight: 600;
}

.score-high {
  color: #67C23A;
  font-weight: 600;
}

/* 抽屉内样式 */
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

.stat-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.stat-item {
  flex: 1;
  min-width: 80px;
  text-align: center;
  padding: 12px 8px;
  background: var(--th-bg);
  border-radius: 8px;
}

.stat-value {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--th-text);
}

.stat-value.danger {
  color: #F56C6C;
}

.stat-label {
  font-size: 0.78rem;
  color: var(--th-text-muted);
  margin-top: 4px;
}

.chart-box {
  height: 240px;
  margin-bottom: 12px;
}

.recent-list h4 {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--th-text-muted);
}

.emotion-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  font-size: 13px;
  border-bottom: 1px solid var(--th-border);
}

.emotion-item:last-child {
  border-bottom: none;
}

.emotion-label {
  color: var(--th-text);
}

.emotion-score {
  color: var(--th-text-muted);
  margin-left: auto;
}

.emotion-time {
  color: var(--th-text-muted);
  font-size: 12px;
}
</style>
