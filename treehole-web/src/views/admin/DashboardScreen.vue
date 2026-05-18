<template>
  <div class="screen">
    <header class="screen-head">
      <h1>全局数据概览</h1>
      <el-button type="primary" @click="fetchData" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </header>

    <!-- 核心指标卡片 -->
    <el-row :gutter="16" class="row">
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-icon user-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="metric-label">用户总数</div>
          <div class="metric-value">{{ data.users?.total ?? '-' }}</div>
          <div class="metric-sub">今日新增 {{ data.users?.todayNew ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-icon chat-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="metric-label">消息总数</div>
          <div class="metric-value">{{ data.chats?.totalMessages ?? '-' }}</div>
          <div class="metric-sub">今日消息 {{ data.chats?.todayMessages ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-icon emotion-icon">
            <el-icon><Sunny /></el-icon>
          </div>
          <div class="metric-label">情绪记录</div>
          <div class="metric-value">{{ data.emotions?.total ?? '-' }}</div>
          <div class="metric-sub">高风险 {{ data.emotions?.riskCounts?.HIGH ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-icon warning-icon">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="metric-label">预警总数</div>
          <div class="metric-value">{{ data.warnings?.total ?? '-' }}</div>
          <div class="metric-sub">近7天 {{ data.warnings?.last7Days ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-icon checkin-icon">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="metric-label">打卡总数</div>
          <div class="metric-value">{{ data.studies?.totalCheckins ?? '-' }}</div>
          <div class="metric-sub">今日打卡 {{ data.studies?.todayCheckins ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-icon system-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="metric-label">今日请求</div>
          <div class="metric-value">{{ data.system?.todayRequests ?? '-' }}</div>
          <div class="metric-sub">失败 {{ data.system?.todayFailures ?? 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="row">
      <el-col :xs="24" :md="12">
        <div class="th-card chart-card">
          <h3>用户角色分布</h3>
          <div ref="rolePieEl" class="chart" />
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="th-card chart-card">
          <h3>情绪风险分布</h3>
          <div ref="riskPieEl" class="chart" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="row">
      <el-col :xs="24">
        <div class="th-card chart-card">
          <h3>数据总览</h3>
          <div ref="barEl" class="chart" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { Refresh, User, ChatDotRound, Sunny, Warning, Calendar, Monitor } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getDashboard } from '../../api/admin'

const loading = ref(false)
const data = ref({})

const rolePieEl = ref(null)
const riskPieEl = ref(null)
const barEl = ref(null)
let rolePieChart = null
let riskPieChart = null
let barChart = null

function onResize() {
  rolePieChart?.resize()
  riskPieChart?.resize()
  barChart?.resize()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDashboard()
    data.value = res.data.data || {}
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  const d = data.value

  // 用户角色分布饼图
  if (rolePieEl.value && d.users?.roleCounts) {
    if (!rolePieChart) rolePieChart = echarts.init(rolePieEl.value)
    const roleData = Object.entries(d.users.roleCounts).map(([name, value]) => ({
      name: name === 'ADMIN' ? '管理员' : name === 'COUNSELOR' ? '辅导员' : '学生',
      value
    }))
    rolePieChart.setOption({
      color: ['#F56C6C', '#E6A23C', '#67C23A'],
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}人' },
        data: roleData
      }]
    })
  }

  // 情绪风险分布饼图
  if (riskPieEl.value && d.emotions?.riskCounts) {
    if (!riskPieChart) riskPieChart = echarts.init(riskPieEl.value)
    const riskData = Object.entries(d.emotions.riskCounts).map(([name, value]) => ({
      name: name === 'LOW' ? '低风险' : name === 'MEDIUM' ? '中风险' : '高风险',
      value
    }))
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
        data: riskData
      }]
    })
  }

  // 数据总览柱状图
  if (barEl.value) {
    if (!barChart) barChart = echarts.init(barEl.value)
    barChart.setOption({
      color: ['#7c9a82'],
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['用户', '会话', '消息', '情绪记录', '预警', '目标', '打卡']
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        barWidth: '40%',
        data: [
          d.users?.total || 0,
          d.chats?.totalSessions || 0,
          d.chats?.totalMessages || 0,
          d.emotions?.total || 0,
          d.warnings?.total || 0,
          d.studies?.totalGoals || 0,
          d.studies?.totalCheckins || 0
        ]
      }]
    })
  }
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  rolePieChart?.dispose()
  riskPieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.screen {
  padding: 20px 24px 32px;
  min-height: calc(100vh - 56px);
}

.screen-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.screen-head h1 {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
}

.row {
  margin-bottom: 16px;
}

.metric {
  padding: 20px;
  margin-bottom: 16px;
  text-align: center;
  position: relative;
}

.metric-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  font-size: 20px;
  color: #fff;
}

.user-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.chat-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }
.emotion-icon { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.warning-icon { background: linear-gradient(135deg, #fa709a, #fee140); }
.checkin-icon { background: linear-gradient(135deg, #a8edea, #fed6e3); }
.system-icon { background: linear-gradient(135deg, #667eea, #764ba2); }

.metric-label {
  font-size: 0.88rem;
  color: var(--th-text-muted);
}

.metric-value {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--th-text);
  margin-top: 8px;
}

.metric-sub {
  font-size: 0.8rem;
  color: var(--th-text-muted);
  margin-top: 4px;
}

.chart-card {
  padding: 20px;
  margin-bottom: 16px;
}

.chart-card h3 {
  margin: 0 0 16px;
  font-size: 1rem;
  font-weight: 600;
  color: var(--th-text);
}

.chart {
  height: 300px;
}
</style>
