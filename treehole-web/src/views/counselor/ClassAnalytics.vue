<template>
  <div class="th-page">
    <h1 class="th-page-title">班级数据分析</h1>

    <!-- 概览卡片 -->
    <el-row :gutter="16" class="row">
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-value">{{ data.totalStudents ?? '-' }}</div>
          <div class="metric-label">学生总数</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-value">{{ data.activeStudents ?? '-' }}</div>
          <div class="metric-label">近7天活跃</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-value">{{ data.avgEmotionScore ?? '-' }}</div>
          <div class="metric-label">情绪均分</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-value">{{ data.totalWarnings ?? '-' }}</div>
          <div class="metric-label">预警总数</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-value warning">{{ data.pendingWarnings ?? '-' }}</div>
          <div class="metric-label">待处理预警</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="metric th-card">
          <div class="metric-value">{{ data.totalCheckins ?? '-' }}</div>
          <div class="metric-label">累计打卡天次</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="row">
      <el-col :xs="24" :md="12">
        <div class="th-card chart-card">
          <h3>情绪风险分布</h3>
          <div ref="riskPieEl" class="chart" />
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="th-card chart-card">
          <h3>近7天预警趋势</h3>
          <div ref="warningLineEl" class="chart" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="row">
      <el-col :xs="24">
        <div class="th-card chart-card">
          <h3>近7天打卡趋势</h3>
          <div ref="checkinLineEl" class="chart" />
        </div>
      </el-col>
    </el-row>

    <!-- 学生情绪排行 -->
    <div class="th-card rank-card">
      <h3>学生情绪排行（高风险优先）</h3>
      <el-table :data="data.studentRanking" stripe size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column label="情绪均分" width="100" align="center">
          <template #default="{ row }">
            <span :class="scoreClass(row.avgScore)">{{ row.avgScore ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningCount" label="预警次数" width="90" align="center" />
        <el-table-column prop="highRiskCount" label="高风险次数" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.highRiskCount > 0 ? 'danger' : ''">{{ row.highRiskCount }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getClassAnalytics } from '../../api/counselor'

const data = ref({})

const riskPieEl = ref(null)
const warningLineEl = ref(null)
const checkinLineEl = ref(null)
let riskPie = null
let warningLine = null
let checkinLine = null

function onResize() {
  riskPie?.resize()
  warningLine?.resize()
  checkinLine?.resize()
}

async function fetchData() {
  try {
    const res = await getClassAnalytics()
    data.value = res.data.data || {}
    await nextTick()
    renderCharts()
  } catch {
    ElMessage.error('加载班级数据失败')
  }
}

function renderCharts() {
  const d = data.value

  // 情绪风险分布饼图
  if (riskPieEl.value && d.riskDistribution) {
    if (riskPie) riskPie.dispose()
    riskPie = echarts.init(riskPieEl.value)
    riskPie.setOption({
      color: ['#67C23A', '#E6A23C', '#F56C6C'],
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}条' },
        data: [
          { name: '低风险', value: d.riskDistribution.LOW || 0 },
          { name: '中风险', value: d.riskDistribution.MEDIUM || 0 },
          { name: '高风险', value: d.riskDistribution.HIGH || 0 }
        ]
      }]
    })
  }

  // 近7天预警趋势
  if (warningLineEl.value && d.warningTrend?.length) {
    if (warningLine) warningLine.dispose()
    warningLine = echarts.init(warningLineEl.value)
    warningLine.setOption(buildLineOption(d.warningTrend, '#F56C6C', '预警数'))
  }

  // 近7天打卡趋势
  if (checkinLineEl.value && d.checkinTrend?.length) {
    if (checkinLine) checkinLine.dispose()
    checkinLine = echarts.init(checkinLineEl.value)
    checkinLine.setOption(buildLineOption(d.checkinTrend, '#7c9a82', '打卡数'))
  }
}

function buildLineOption(trendData, color, name) {
  return {
    color: [color],
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: trendData.map(d => d.date.substring(5)),
      boundaryGap: false
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name,
      type: 'line',
      data: trendData.map(d => d.count),
      smooth: true,
      areaStyle: { opacity: 0.15 },
      itemStyle: { borderRadius: 4 }
    }]
  }
}

function scoreClass(score) {
  if (score == null) return ''
  if (score < -0.25) return 'score-low'
  if (score < 0) return 'score-mid'
  return 'score-high'
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  riskPie?.dispose()
  warningLine?.dispose()
  checkinLine?.dispose()
})
</script>

<style scoped>
.row {
  margin-bottom: 16px;
}

.metric {
  padding: 20px;
  margin-bottom: 16px;
  text-align: center;
}

.metric-value {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--th-text);
}

.metric-value.warning {
  color: #F56C6C;
}

.metric-label {
  font-size: 0.85rem;
  color: var(--th-text-muted);
  margin-top: 6px;
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

.rank-card {
  padding: 20px;
  margin-bottom: 16px;
}

.rank-card h3 {
  margin: 0 0 16px;
  font-size: 1rem;
  font-weight: 600;
  color: var(--th-text);
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

.danger {
  color: #F56C6C;
  font-weight: 600;
}
</style>
