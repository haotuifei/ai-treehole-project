<template>
  <div class="screen">
    <header class="screen-head">
      <h1>全局数据概览</h1>
      <span class="hint">示例图表 · 可对接 `/api/admin/warnings/stats` 等</span>
    </header>
    <el-row :gutter="16" class="row">
      <el-col :xs="24" :md="8">
        <div class="metric th-card">
          <div class="metric-label">今日预警</div>
          <div class="metric-value">—</div>
        </div>
      </el-col>
      <el-col :xs="24" :md="8">
        <div class="metric th-card">
          <div class="metric-label">活跃会话</div>
          <div class="metric-value">—</div>
        </div>
      </el-col>
      <el-col :xs="24" :md="8">
        <div class="metric th-card">
          <div class="metric-label">打卡人次</div>
          <div class="metric-value">—</div>
        </div>
      </el-col>
    </el-row>
    <div class="charts">
      <div ref="lineEl" class="chart th-card" />
      <div ref="pieEl" class="chart th-card" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const lineEl = ref(null)
const pieEl = ref(null)
let lineChart = null
let pieChart = null

function onResize() {
  lineChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  if (lineEl.value) {
    lineChart = echarts.init(lineEl.value)
    lineChart.setOption({
      color: ['#7c9a82'],
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: ['1日', '5日', '10日', '15日', '20日', '25日', '30日'] },
      yAxis: { type: 'value' },
      series: [
        {
          name: '咨询量',
          type: 'line',
          smooth: true,
          areaStyle: { color: 'rgba(124, 154, 130, 0.15)' },
          data: [30, 42, 38, 55, 48, 62, 58]
        }
      ]
    })
  }
  if (pieEl.value) {
    pieChart = echarts.init(pieEl.value)
    pieChart.setOption({
      color: ['#7c9a82', '#c4a77d', '#a8b8c8', '#d4c4b0'],
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          name: '情绪分布',
          type: 'pie',
          radius: ['42%', '68%'],
          avoidLabelOverlap: true,
          itemStyle: { borderRadius: 8, borderColor: '#fffcf8', borderWidth: 2 },
          data: [
            { value: 48, name: '平稳' },
            { value: 22, name: '焦虑' },
            { value: 18, name: '低落' },
            { value: 12, name: '其他' }
          ]
        }
      ]
    })
  }
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  lineChart?.dispose()
  pieChart?.dispose()
  lineChart = null
  pieChart = null
})
</script>

<style scoped>
.screen {
  padding: 20px 24px 32px;
  min-height: calc(100vh - 56px);
  background: linear-gradient(180deg, #eef3ef 0%, var(--th-bg) 40%);
}
.screen-head {
  margin-bottom: 20px;
}
.screen-head h1 {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
}
.hint {
  font-size: 0.85rem;
  color: var(--th-text-muted);
}
.row {
  margin-bottom: 16px;
}
.metric {
  padding: 20px;
  margin-bottom: 16px;
  text-align: center;
}
.metric-label {
  font-size: 0.88rem;
  color: var(--th-text-muted);
}
.metric-value {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--th-primary);
  margin-top: 8px;
}
.charts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}
.chart {
  height: 320px;
}
</style>
