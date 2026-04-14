<template>
  <div class="th-page">
    <p class="th-page-desc">汇总聊天与测评中的情绪标签与趋势</p>

    <!-- 时间筛选 -->
    <div class="period-tabs">
      <el-radio-group v-model="period" @change="changePeriod">
        <el-radio-button value="day">今日</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="th-card">
      <el-skeleton :rows="8" animated />
    </div>

    <template v-else>
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-value" :style="{ color: scoreColor }">{{ stats.avgScore || 0 }}</div>
          <div class="stat-label">情绪均分</div>
          <div class="stat-sub">满分 100</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.chatCount || 0 }}</div>
          <div class="stat-label">对话次数</div>
          <div class="stat-sub">树洞交流</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" :class="stats.highRiskCount > 0 ? 'risk-active' : ''">{{ stats.highRiskCount || 0 }}</div>
          <div class="stat-label">高风险次数</div>
          <div class="stat-sub">需要关注</div>
        </div>
      </div>

      <!-- 情绪趋势曲线 -->
      <div class="th-card chart-card">
        <div class="chart-title">情绪与对话趋势</div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>

      <!-- 分布图表 -->
      <div class="charts-row">
        <div class="th-card chart-card">
          <div class="chart-title">情绪分布</div>
          <div ref="emotionChartRef" class="chart-container small"></div>
        </div>
        <div class="th-card chart-card">
          <div class="chart-title">风险等级分布</div>
          <div ref="riskChartRef" class="chart-container small"></div>
        </div>
      </div>

      <!-- 改善建议 -->
      <div class="th-card suggestion-card" v-if="stats.suggestions?.length">
        <div class="suggestion-title">💡 改善建议</div>
        <ul class="suggestion-list">
          <li v-for="(s, i) in stats.suggestions" :key="i">{{ s }}</li>
        </ul>
      </div>

      <!-- 情绪记录明细 -->
      <div class="th-card">
        <div class="chart-title">情绪记录明细</div>
        <el-table :data="records" stripe style="width: 100%" v-if="records.length">
          <el-table-column prop="createTime" label="时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="emotionLabel" label="情绪" width="100" />
          <el-table-column prop="sentimentScore" label="分值" width="80">
            <template #default="{ row }">
              {{ row.sentimentScore != null ? Math.round(Number(row.sentimentScore) * 100) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="riskLevel" label="风险">
            <template #default="{ row }">
              <el-tag :type="riskTagType(row.riskLevel)" size="small">
                {{ row.riskLevel || '-' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无记录" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getEmotionStats, getEmotionRecords } from '../../api/emotion'

const period = ref('week')
const loading = ref(false)
const stats = ref({})
const records = ref([])
const trendChartRef = ref(null)
const emotionChartRef = ref(null)
const riskChartRef = ref(null)
let trendChart = null
let emotionChart = null
let riskChart = null
let resizeObserver = null

const scoreColor = computed(() => {
  const score = Number(stats.value.avgScore) || 0
  if (score < 40) return '#e74c3c'
  if (score < 60) return '#f39c12'
  return '#27ae60'
})

onMounted(() => {
  fetchData()
  initResizeObserver()
})

onUnmounted(() => {
  disposeCharts()
  resizeObserver?.disconnect()
})

watch(period, () => {
  fetchData()
})

function changePeriod() {
  fetchData()
}

async function fetchData() {
  loading.value = true
  // 先销毁旧图表，确保容器尺寸正确
  disposeCharts()
  try {
    const [statsRes, recordsRes] = await Promise.all([
      getEmotionStats(period.value),
      getEmotionRecords({ period: period.value, pageNum: 1, pageSize: 50 })
    ])
    stats.value = statsRes.data.data || {}
    records.value = recordsRes.data.data?.records || []
    // 等 DOM 更新完再渲染图表
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('获取情绪数据失败')
  } finally {
    loading.value = false
  }
}

function disposeCharts() {
  trendChart?.dispose()
  emotionChart?.dispose()
  riskChart?.dispose()
  trendChart = null
  emotionChart = null
  riskChart = null
}

function initResizeObserver() {
  resizeObserver = new ResizeObserver(() => {
    trendChart?.resize()
    emotionChart?.resize()
    riskChart?.resize()
  })
}

function renderCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    resizeObserver?.observe(trendChartRef.value)
    renderTrendChart()
  }
  if (emotionChartRef.value) {
    emotionChart = echarts.init(emotionChartRef.value)
    resizeObserver?.observe(emotionChartRef.value)
    renderEmotionChart()
  }
  if (riskChartRef.value) {
    riskChart = echarts.init(riskChartRef.value)
    resizeObserver?.observe(riskChartRef.value)
    renderRiskChart()
  }
}

function renderTrendChart() {
  if (!trendChart) return
  const trend = stats.value.dailyTrend || []
  const dates = trend.map(d => d.date?.slice(5) || '')
  const scores = trend.map(d => Number(d.score) || 0)
  const chatCount = stats.value.dailyChatCount || []
  const counts = chatCount.map(d => d.count || 0)

  trendChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['情绪分值', '对话次数'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: [
      { type: 'value', name: '情绪分值', min: 0, max: 100, axisLabel: { formatter: '{value}' } },
      { type: 'value', name: '对话次数', min: 0, axisLabel: { formatter: '{value}' } }
    ],
    series: [
      {
        name: '情绪分值',
        type: 'line',
        smooth: true,
        data: scores,
        itemStyle: { color: '#7c9a7c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(124, 154, 124, 0.3)' },
            { offset: 1, color: 'rgba(124, 154, 124, 0.05)' }
          ])
        }
      },
      {
        name: '对话次数',
        type: 'bar',
        yAxisIndex: 1,
        data: counts,
        itemStyle: { color: 'rgba(196, 167, 125, 0.5)' }
      }
    ]
  }, true)
}

function renderEmotionChart() {
  if (!emotionChart) return
  const dist = stats.value.emotionLabelDistribution || {}
  const data = Object.entries(dist).map(([name, value]) => ({ name, value }))

  emotionChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data
    }]
  }, true)
}

function renderRiskChart() {
  if (!riskChart) return
  const dist = stats.value.riskLevelDistribution || {}
  const colorMap = { LOW: '#27ae60', MEDIUM: '#f39c12', HIGH: '#e74c3c', UNKNOWN: '#95a5a6' }
  const data = Object.entries(dist).map(([name, value]) => ({
    name,
    value,
    itemStyle: { color: colorMap[name] || colorMap.UNKNOWN }
  }))

  riskChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data
    }]
  }, true)
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

function riskTagType(level) {
  switch (level) {
    case 'HIGH': return 'danger'
    case 'MEDIUM': return 'warning'
    case 'LOW': return 'success'
    default: return 'info'
  }
}
</script>

<style scoped>
.period-tabs {
  margin-bottom: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--th-card);
  border: 1px solid var(--th-border);
  border-radius: var(--th-radius);
  padding: 20px;
  text-align: center;
  box-shadow: var(--th-shadow);
}

.stat-value {
  font-size: 2.2rem;
  font-weight: 700;
  color: var(--th-text);
  line-height: 1.2;
}

.stat-value.risk-active {
  color: #e74c3c;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--th-text-muted);
  margin-top: 4px;
}

.stat-sub {
  font-size: 0.8rem;
  color: var(--th-text-muted);
  opacity: 0.7;
  margin-top: 2px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--th-text);
  margin-bottom: 16px;
}

.chart-container {
  height: 280px;
}

.chart-container.small {
  height: 220px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.suggestion-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, rgba(124, 154, 124, 0.08), rgba(196, 167, 125, 0.08));
}

.suggestion-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--th-text);
  margin-bottom: 12px;
}

.suggestion-list {
  margin: 0;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestion-list li {
  color: var(--th-text);
  font-size: 0.9rem;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
