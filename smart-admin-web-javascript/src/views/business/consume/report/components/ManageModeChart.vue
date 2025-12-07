<template>
  <a-card title="经营模式统计" :bordered="false">
    <div ref="chartRef" style="width: 100%; height: 400px"></div>
  </a-card>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  dateRange: {
    type: Array,
    default: () => []
  }
})

const chartRef = ref(null)
let chartInstance = null

onMounted(() => {
  initChart()
})

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance || !props.data) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}笔 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: props.data.map(item => item.modeName)
    },
    series: [
      {
        name: '经营模式',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: props.data.map(item => ({
          value: item.count,
          name: item.modeName
        }))
      }
    ],
    color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de']
  }
  
  chartInstance.setOption(option)
}
</script>

