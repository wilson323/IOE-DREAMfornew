<template>
  <a-card :title="title" :bordered="false">
    <div ref="chartRef" :style="{ width: '100%', height: height + 'px' }"></div>
  </a-card>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: { type: String, default: '日历热力图' },
  height: { type: Number, default: 300 },
  year: { type: Number, default: new Date().getFullYear() },
  data: { type: Array, default: () => [] }
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

watch(() => [props.year, props.data], () => {
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
      formatter: (params) => {
        return `${params.data[0]}<br/>数量: ${params.data[1]}`
      }
    },
    visualMap: {
      min: 0,
      max: Math.max(...props.data.map(d => d[1]), 10),
      type: 'piecewise',
      orient: 'horizontal',
      left: 'center',
      top: 20,
      pieces: [
        { min: 0, max: 0, color: '#ebedf0' },
        { min: 1, max: 5, color: '#c6e48b' },
        { min: 6, max: 10, color: '#7bc96f' },
        { min: 11, max: 20, color: '#239a3b' },
        { min: 21, color: '#196127' }
      ]
    },
    calendar: {
      top: 80,
      left: 30,
      right: 30,
      cellSize: ['auto', 15],
      range: props.year,
      itemStyle: {
        borderWidth: 0.5,
        borderColor: '#fff'
      },
      yearLabel: { show: true },
      dayLabel: {
        firstDay: 1,
        nameMap: ['日', '一', '二', '三', '四', '五', '六']
      },
      monthLabel: {
        nameMap: 'cn'
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#fff',
          width: 2,
          type: 'solid'
        }
      }
    },
    series: [{
      type: 'heatmap',
      coordinateSystem: 'calendar',
      data: props.data
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

