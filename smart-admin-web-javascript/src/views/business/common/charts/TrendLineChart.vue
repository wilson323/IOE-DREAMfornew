<template>
  <a-card :title="title" :bordered="false">
    <div ref="chartRef" :style="{ width: '100%', height: height + 'px' }"></div>
  </a-card>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: { type: String, default: '时间趋势分析' },
  height: { type: Number, default: 400 },
  data: { type: Array, default: () => [] },
  xAxisData: { type: Array, default: () => [] },
  series: { type: Array, default: () => [] }
})

const chartRef = ref(null)
let chartInstance = null

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
  }
})

watch(() => [props.data, props.series], () => {
  updateChart()
}, { deep: true })

const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: props.series.map(s => s.name),
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.xAxisData
    },
    yAxis: { type: 'value' },
    series: props.series.map(s => ({
      name: s.name,
      type: 'line',
      smooth: true,
      data: s.data,
      areaStyle: s.showArea ? {} : undefined,
      itemStyle: { color: s.color }
    }))
  }

  chartInstance.setOption(option, true)
}

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}
</script>

