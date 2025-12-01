<template>
  <div class="device-status-chart">
    <a-card title="设备状态统计" :bordered="false">
      <div ref="chartContainer" style="width: 100%; height: 300px;"></div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const chartContainer = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartContainer.value) return

  chartInstance = echarts.init(chartContainer.value)

  const option = {
    title: {
      text: '设备状态分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '设备状态',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 1048, name: '在线设备', itemStyle: { color: '#52c41a' } },
          { value: 735, name: '离线设备', itemStyle: { color: '#ff4d4f' } },
          { value: 580, name: '维护设备', itemStyle: { color: '#faad14' } },
          { value: 484, name: '故障设备', itemStyle: { color: '#f5222d' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', resizeChart)
})
</script>

<style scoped>
.device-status-chart {
  width: 100%;
}
</style>