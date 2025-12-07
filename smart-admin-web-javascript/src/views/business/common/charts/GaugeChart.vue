<template>
  <a-card :title="title" :bordered="false">
    <div ref="chartRef" :style="{ width: '100%', height: height + 'px' }"></div>
  </a-card>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: { type: String, default: '仪表盘' },
  height: { type: Number, default: 350 },
  value: { type: Number, default: 0 },
  max: { type: Number, default: 100 },
  unit: { type: String, default: '%' }
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

watch(() => props.value, () => {
  updateChart()
})

const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return
  
  const option = {
    series: [{
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: props.max,
      splitNumber: 8,
      axisLine: {
        lineStyle: {
          width: 6,
          color: [
            [0.3, '#67e0e3'],
            [0.7, '#37a2da'],
            [1, '#fd666d']
          ]
        }
      },
      pointer: {
        icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
        length: '12%',
        width: 20,
        offsetCenter: [0, '-60%'],
        itemStyle: {
          color: 'auto'
        }
      },
      axisTick: {
        length: 12,
        lineStyle: {
          color: 'auto',
          width: 2
        }
      },
      splitLine: {
        length: 20,
        lineStyle: {
          color: 'auto',
          width: 5
        }
      },
      axisLabel: {
        color: '#464646',
        fontSize: 14,
        distance: -50,
        formatter: (value) => {
          if (value === props.max) {
            return value + props.unit
          }
          return value
        }
      },
      title: {
        offsetCenter: [0, '-20%'],
        fontSize: 18
      },
      detail: {
        fontSize: 32,
        offsetCenter: [0, '0%'],
        valueAnimation: true,
        formatter: (value) => {
          return value.toFixed(1) + props.unit
        },
        color: 'auto'
      },
      data: [{
        value: props.value,
        name: props.title
      }]
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

