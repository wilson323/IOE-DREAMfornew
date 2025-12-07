<template>
  <a-card :title="title" :bordered="false">
    <div ref="chartRef" :style="{ width: '100%', height: height + 'px' }"></div>
  </a-card>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: { type: String, default: '层级数据展示' },
  height: { type: Number, default: 500 },
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

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance || !props.data || props.data.length === 0) return
  
  const option = {
    tooltip: {
      formatter: (info) => {
        const value = info.value
        const treePathInfo = info.treePathInfo
        const treePath = []
        
        for (let i = 1; i < treePathInfo.length; i++) {
          treePath.push(treePathInfo[i].name)
        }
        
        return [
          '<div class="tooltip-title">' + echarts.format.encodeHTML(treePath.join('/')) + '</div>',
          '数值: ' + echarts.format.addCommas(value)
        ].join('')
      }
    },
    series: [{
      type: 'treemap',
      width: '100%',
      height: '90%',
      roam: false,
      nodeClick: 'zoomToNode',
      breadcrumb: {
        show: true,
        height: 30,
        bottom: 0
      },
      label: {
        show: true,
        formatter: '{b}\n{c}'
      },
      upperLabel: {
        show: true,
        height: 30,
        color: '#fff'
      },
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 2,
        gapWidth: 2
      },
      levels: [
        {
          itemStyle: {
            borderColor: '#777',
            borderWidth: 0,
            gapWidth: 1
          },
          upperLabel: {
            show: false
          }
        },
        {
          itemStyle: {
            borderColor: '#555',
            borderWidth: 5,
            gapWidth: 1
          },
          emphasis: {
            itemStyle: {
              borderColor: '#ddd'
            }
          }
        },
        {
          colorSaturation: [0.35, 0.5],
          itemStyle: {
            borderWidth: 5,
            gapWidth: 1,
            borderColorSaturation: 0.6
          }
        }
      ],
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

