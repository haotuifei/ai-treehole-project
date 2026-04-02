<template>
  <div class="th-page">
    <h1 class="th-page-title">班级数据分析</h1>
    <p class="th-page-desc">班级情绪与打卡等统计（示例 ECharts）。</p>
    <div ref="chartEl" class="chart-box th-card" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const chartEl = ref(null)
let chart = null

function buildOption() {
  return {
    color: ['#7c9a82', '#c4a77d', '#a8b8c8'],
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: { type: 'value', name: '人次' },
    series: [
      {
        name: '打卡',
        type: 'bar',
        data: [12, 15, 10, 18, 14, 8, 6],
        itemStyle: { borderRadius: [6, 6, 0, 0] }
      }
    ]
  }
}

function onResize() {
  chart?.resize()
}

onMounted(() => {
  if (!chartEl.value) return
  chart = echarts.init(chartEl.value)
  chart.setOption(buildOption())
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.chart-box {
  height: 360px;
}
</style>
