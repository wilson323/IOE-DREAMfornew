<template>
  <a-card :title="title" :bordered="false">
    <div ref="chartRef" :style="{ width: '100%', height: height + 'px' }"></div>
  </a-card>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: { type: String, default: '雷达图分析' },
  height: { type: Number, default: 450 },
  indicator: { type: Array, default: () => [] },
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

watch(() => [props.indicator, props.series], () => {
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
      trigger: 'item'
    },
    legend: {
      data: props.series.map(s => s.name),
      bottom: 10
    },
    radar: {
      indicator: props.indicator.map(item => ({
        name: item.name,
        max: item.max || 100
      })),
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#428BD4'
      },
      splitLine: {
        lineStyle: {
          color: [
            'rgba(66, 139, 212, 0.1)',
            'rgba(66, 139, 212, 0.2)',
            'rgba(66, 139, 212, 0.3)',
            'rgba(66, 139, 212, 0.4)',
            'rgba(66, 139, 212, 0.5)'
          ].reverse()
        }
      },
      splitArea: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(66, 139, 212, 0.5)'
        }
      }
    },
    series: [{
      type: 'radar',
      data: props.series.map((s, index) => ({
        value: s.value,
        name: s.name,
        areaStyle: {
          color: new echarts.graphic.RadialGradient(0.1, 0.6, 1, [
            { color: ['#5470c6', '#91cc75', '#fac858', '#ee6666'][index % 4], offset: 0 },
            { color: ['#5470c6', '#91cc75', '#fac858', '#ee6666'][index % 4], offset: 1, opacity: 0.2 }
          ])
        }
      }))
    }]
  }

  chartInstance.setOption(option, true)
}

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}
</script>

