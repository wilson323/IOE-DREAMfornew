<template>
  <a-card :title="title" :bordered="false">
    <div ref="chartRef" :style="{ width: '100%', height: height + 'px' }"></div>
  </a-card>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: { type: String, default: '桑基图（流向分析）' },
  height: { type: Number, default: 500 },
  nodes: { type: Array, default: () => [] },
  links: { type: Array, default: () => [] }
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

watch(() => [props.nodes, props.links], () => {
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
      trigger: 'item',
      triggerOn: 'mousemove',
      formatter: (params) => {
        if (params.dataType === 'edge') {
          return `${params.data.source} → ${params.data.target}<br/>数量: ${params.data.value}`
        }
        return `${params.data.name}<br/>总计: ${params.data.value}`
      }
    },
    series: [{
      type: 'sankey',
      layout: 'none',
      emphasis: {
        focus: 'adjacency'
      },
      data: props.nodes,
      links: props.links,
      lineStyle: {
        color: 'gradient',
        curveness: 0.5
      },
      label: {
        color: '#333',
        fontSize: 12
      },
      itemStyle: {
        borderWidth: 1,
        borderColor: '#aaa'
      }
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

