<template>
  <div class="th-page">
    <p class="th-page-desc">汇总聊天与测评中的情绪标签与趋势</p>

    <!-- 时间筛选 -->
    <div class="period-tabs">
      <el-radio-group v-model="period">
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
        <div class="chart-title">
          情绪与对话趋势
          <span class="chart-title-tip">左轴情绪分值，右轴对话次数</span>
        </div>
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
          <el-table-column prop="emotionLabel" label="情绪" width="120">
            <template #default="{ row }">
              {{ formatEmotionLabel(row.emotionLabel) }}
            </template>
          </el-table-column>
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
  } catch (e) {
    ElMessage.error('获取情绪数据失败')
  } finally {
    loading.value = false
    // 图表容器在 v-else 中，必须先让 loading 结束再等 DOM 挂载
    await nextTick()
    renderCharts()
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
    queueChartResize(trendChart)
  }
  if (emotionChartRef.value) {
    emotionChart = echarts.init(emotionChartRef.value)
    resizeObserver?.observe(emotionChartRef.value)
    renderEmotionChart()
    queueChartResize(emotionChart)
  }
  if (riskChartRef.value) {
    riskChart = echarts.init(riskChartRef.value)
    resizeObserver?.observe(riskChartRef.value)
    renderRiskChart()
    queueChartResize(riskChart)
  }
}

function renderTrendChart() {
  if (!trendChart) return
  const { labels, scores, counts, scoredDays } = buildTrendData()
  const scoreAvg = scoredDays.length
    ? Math.round(scoredDays.reduce((sum, value) => sum + value, 0) / scoredDays.length)
    : 0
  const barMaxWidth = labels.length <= 3 ? 22 : labels.length <= 7 ? 18 : 14

  trendChart.setOption({
    color: ['#7c9a7c', '#c4a77d'],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(42, 47, 53, 0.92)',
      borderWidth: 0,
      textStyle: { color: '#fff' },
      formatter(params) {
        const lines = params.map(item => {
          const suffix = item.seriesName === '情绪分值' ? '分' : '次'
          const value = item.seriesName === '情绪分值' && (item.value == null || item.value === '-')
            ? '暂无情绪记录'
            : `${item.value}${suffix}`
          return `${item.marker}${item.seriesName}：${value}`
        })
        return [`${params[0]?.axisValue || ''}`, ...lines].join('<br/>')
      }
    },
    legend: { data: ['情绪分值', '对话次数'], bottom: 0, icon: 'roundRect', itemHeight: 10, itemGap: 28 },
    grid: { left: '5%', right: '8%', bottom: '18%', top: '12%', containLabel: true },
    xAxis: {
      type: 'category',
      data: labels,
      boundaryGap: true,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.35)' } },
      axisLabel: { color: '#667085' }
    },
    yAxis: [
      {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: { formatter: '{value}', color: '#667085' },
        splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.14)' } }
      },
      {
        type: 'value',
        minInterval: 1,
        axisLabel: { formatter: '{value}', color: '#667085' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '情绪分值',
        type: 'line',
        smooth: true,
        connectNulls: false,
        symbol: 'circle',
        symbolSize: 8,
        data: scores,
        itemStyle: { color: '#7c9a7c' },
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(124, 154, 124, 0.28)' },
            { offset: 1, color: 'rgba(124, 154, 124, 0.05)' }
          ])
        },
        markLine: {
          symbol: 'none',
          label: {
            show: true,
            position: 'start',
            distance: 10,
            formatter: `平均 ${scoreAvg} 分`,
            color: '#6f8d6f',
            fontSize: 11,
            padding: [3, 8],
            backgroundColor: 'rgba(255, 255, 255, 0.88)',
            borderRadius: 10
          },
          lineStyle: { type: 'dashed', color: 'rgba(124, 154, 124, 0.7)' },
          data: scoreAvg ? [{ yAxis: scoreAvg }] : []
        },
        markPoint: {
          symbolSize: 44,
          itemStyle: { color: '#7c9a7c' },
          label: {
            color: '#fff',
            formatter(param) {
              if (param.data?.type === 'max') return '高'
              if (param.data?.type === 'min') return '低'
              return ''
            }
          },
          data: scoredDays.length >= 3 ? [{ type: 'max' }, { type: 'min' }] : []
        }
      },
      {
        name: '对话次数',
        type: 'bar',
        yAxisIndex: 1,
        data: counts,
        barMaxWidth,
        barCategoryGap: '42%',
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(196, 167, 125, 0.9)' },
            { offset: 1, color: 'rgba(196, 167, 125, 0.45)' }
          ])
        },
        label: {
          show: labels.length <= 7,
          position: 'top',
          color: '#8b6f47',
          formatter: ({ value }) => (value ? value : '')
        }
      }
    ]
  }, true)
}

function renderEmotionChart() {
  if (!emotionChart) return
  const { data, total, centerTitle, centerSubtitle, hasData } = buildPieChartData(
    stats.value.emotionLabelDistribution || {},
    {
      emptyTitle: '暂无情绪',
      emptySubtitle: '等待记录',
      summaryTitle: '主导情绪',
      nameFormatter: formatEmotionLabel,
      summaryBuilder: (topItem, totalCount) => `${Math.round((topItem.value / totalCount) * 100)}%`
    }
  )

  emotionChart.setOption({
    color: ['#7c9a7c', '#c4a77d', '#8ea7c3', '#d8b4a0', '#90c2b3', '#d6c37b'],
    tooltip: total ? { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' } : { show: false },
    legend: {
      show: hasData,
      bottom: 0,
      type: 'scroll',
      icon: 'circle',
      textStyle: { color: '#667085' }
    },
    graphic: buildDonutGraphic(centerTitle, centerSubtitle),
    series: [{
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      label: { show: false },
      labelLine: { show: false },
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      emphasis: { scale: true, label: { show: false } },
      data
    }]
  }, true)
}

function renderRiskChart() {
  if (!riskChart) return
  const dist = stats.value.riskLevelDistribution || {}
  const { data, total, centerTitle, centerSubtitle, hasData } = buildPieChartData(
    dist,
    {
      emptyTitle: '暂无风险',
      emptySubtitle: '等待记录',
      summaryTitle: '最高风险',
      nameFormatter: formatRiskLabel,
      sortFn: ([left], [right]) => riskOrder(right) - riskOrder(left),
      colorMap: { LOW: '#27ae60', MEDIUM: '#f39c12', HIGH: '#e74c3c', UNKNOWN: '#95a5a6' },
      summaryBuilder: (topItem, totalCount) => `${Math.round((topItem.value / totalCount) * 100)}%`
    }
  )

  riskChart.setOption({
    tooltip: total ? { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' } : { show: false },
    legend: {
      show: hasData,
      bottom: 0,
      type: 'scroll',
      icon: 'circle',
      textStyle: { color: '#667085' }
    },
    graphic: buildDonutGraphic(centerTitle, centerSubtitle),
    series: [{
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      label: { show: false },
      labelLine: { show: false },
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      emphasis: { scale: true, label: { show: false } },
      data
    }]
  }, true)
}

function buildTrendData() {
  const trend = stats.value.dailyTrend || []
  const chatCount = stats.value.dailyChatCount || []
  const scoreMap = new Map()
  const countMap = new Map()

  trend.forEach((item) => {
    if (!item?.date) return
    const value = Number(item?.score)
    scoreMap.set(item.date, Number.isFinite(value) ? value : null)
  })

  chatCount.forEach((item) => {
    if (!item?.date) return
    countMap.set(item.date, Number(item?.count) || 0)
  })

  const orderedKeys = buildPeriodDateKeys()
  if (!orderedKeys.length) {
    const fallbackKeys = Array.from(new Set([...scoreMap.keys(), ...countMap.keys()])).sort()
    return {
      labels: fallbackKeys.map(formatDateLabel),
      scores: fallbackKeys.map(key => scoreMap.has(key) ? scoreMap.get(key) : null),
      counts: fallbackKeys.map(key => countMap.get(key) || 0),
      scoredDays: fallbackKeys
        .map(key => scoreMap.get(key))
        .filter(value => value != null)
    }
  }

  return {
    labels: orderedKeys.map(formatDateLabel),
    scores: orderedKeys.map(key => scoreMap.has(key) ? scoreMap.get(key) : null),
    counts: orderedKeys.map(key => countMap.get(key) || 0),
    scoredDays: orderedKeys
      .map(key => scoreMap.get(key))
      .filter(value => value != null)
  }
}

function buildPeriodDateKeys() {
  const today = new Date()
  const current = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  let start = new Date(current)

  switch (period.value) {
    case 'day':
      break
    case 'week': {
      const day = current.getDay()
      const offset = day === 0 ? 6 : day - 1
      start.setDate(current.getDate() - offset)
      break
    }
    case 'month':
    default:
      start = new Date(current.getFullYear(), current.getMonth(), 1)
      break
  }

  const dates = []
  const cursor = new Date(start)
  while (cursor <= current) {
    dates.push(formatDateKey(cursor))
    cursor.setDate(cursor.getDate() + 1)
  }
  return dates
}

function queueChartResize(chart) {
  if (!chart) return
  requestAnimationFrame(() => {
    chart.resize()
    requestAnimationFrame(() => chart.resize())
  })
}

function formatDateKey(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function buildPieChartData(distribution, options = {}) {
  const entries = Object.entries(distribution)
    .filter(([, value]) => Number(value) > 0)
    .sort(options.sortFn || (([, left], [, right]) => Number(right) - Number(left)))
  const total = entries.reduce((sum, [, value]) => sum + Number(value || 0), 0)

  if (!total) {
    return {
      hasData: false,
      total: 0,
      centerTitle: options.emptyTitle || '暂无数据',
      centerSubtitle: options.emptySubtitle || '',
      data: [{
        name: '暂无数据',
        value: 1,
        itemStyle: { color: 'rgba(148, 163, 184, 0.18)' },
        emphasis: { disabled: true }
      }]
    }
  }

  const data = entries.map(([name, value]) => ({
    name: options.nameFormatter ? options.nameFormatter(name) : name,
    value: Number(value),
    itemStyle: options.colorMap?.[name] ? { color: options.colorMap[name] } : undefined
  }))
  const topItem = data[0]

  return {
    hasData: true,
    total,
    centerTitle: options.summaryTitle || topItem.name,
    centerSubtitle: options.summaryBuilder ? options.summaryBuilder(topItem, total) : `${topItem.value} 次`,
    data
  }
}

function buildDonutGraphic(title, subtitle) {
  return [
    {
      type: 'text',
      left: 'center',
      top: '34%',
      style: {
        text: title,
        textAlign: 'center',
        fill: '#667085',
        fontSize: 13,
        fontWeight: 500
      }
    },
    {
      type: 'text',
      left: 'center',
      top: '45%',
      style: {
        text: subtitle,
        textAlign: 'center',
        fill: '#1f2937',
        fontSize: 18,
        fontWeight: 700
      }
    }
  ]
}

function formatDateLabel(dateStr) {
  if (!dateStr) return ''
  if (String(dateStr).includes('-')) return String(dateStr).slice(5).replace('-', '/')
  return String(dateStr)
}

function formatRiskLabel(level) {
  switch (level) {
    case 'HIGH': return '高风险'
    case 'MEDIUM': return '中风险'
    case 'LOW': return '低风险'
    default: return '未知'
  }
}

function formatEmotionLabel(label) {
  switch (label) {
    case 'NEUTRAL': return '平静'
    case 'POSITIVE': return '积极'
    case 'NEGATIVE': return '消极'
    case 'ANXIOUS': return '焦虑'
    case 'SAD': return '低落'
    case 'ANGRY': return '愤怒'
    case 'CRISIS_KEYWORD': return '危机关键词'
    case 'CRISIS': return '危机'
    default: return label || '-'
  }
}

function riskOrder(level) {
  switch (level) {
    case 'HIGH': return 3
    case 'MEDIUM': return 2
    case 'LOW': return 1
    default: return 0
  }
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.chart-title-tip {
  font-size: 0.82rem;
  font-weight: 400;
  color: var(--th-text-muted);
}

.chart-container {
  min-height: 280px;
  height: 280px;
}

.chart-container.small {
  height: 240px;
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
