<!--
  @fileoverview 考勤趋势图组件
  @author IOE-DREAM Team
  @description 展示考勤数据趋势的折线图
-->
<template>
  <div class="trend-chart">
    <div ref="chartRef" style="width: 100%; height: 400px;"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';
import type { AttendanceTrendVO } from '@/api/business/attendance/result';

/**
 * 组件属性
 */
interface Props {
  trendData: AttendanceTrendVO[];
}

const props = defineProps<Props>();

// 图表引用
const chartRef = ref<HTMLDivElement>();
let chartInstance: echarts.ECharts | null = null;

/**
 * 初始化图表
 */
const initChart = () => {
  if (!chartRef.value) return;

  chartInstance = echarts.init(chartRef.value);
  updateChart();
};

/**
 * 更新图表
 */
const updateChart = () => {
  if (!chartInstance || !props.trendData || props.trendData.length === 0) return;

  const dates = props.trendData.map(item => item.date);
  const normalCounts = props.trendData.map(item => item.normalCount);
  const lateCounts = props.trendData.map(item => item.lateCount);
  const earlyCounts = props.trendData.map(item => item.earlyCount);
  const absentCounts = props.trendData.map(item => item.absentCount);
  const normalRates = props.trendData.map(item => item.normalRate);

  const option: EChartsOption = {
    title: {
      text: '考勤趋势分析',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 600
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['正常', '迟到', '早退', '缺勤', '正常率'],
      top: 30
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
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '次数',
        position: 'left',
        axisLine: {
          show: true,
          lineStyle: {
            color: '#5470C6'
          }
        },
        axisLabel: {
          formatter: '{value}'
        }
      },
      {
        type: 'value',
        name: '正常率 (%)',
        position: 'right',
        axisLine: {
          show: true,
          lineStyle: {
            color: '#57C3B6'
          }
        },
        axisLabel: {
          formatter: '{value}%'
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: '正常',
        type: 'line',
        data: normalCounts,
        smooth: true,
        itemStyle: {
          color: '#52c41a'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.1)' }
          ])
        }
      },
      {
        name: '迟到',
        type: 'line',
        data: lateCounts,
        smooth: true,
        itemStyle: {
          color: '#faad14'
        }
      },
      {
        name: '早退',
        type: 'line',
        data: earlyCounts,
        smooth: true,
        itemStyle: {
          color: '#fa8c16'
        }
      },
      {
        name: '缺勤',
        type: 'line',
        data: absentCounts,
        smooth: true,
        itemStyle: {
          color: '#f5222d'
        }
      },
      {
        name: '正常率',
        type: 'line',
        yAxisIndex: 1,
        data: normalRates,
        smooth: true,
        itemStyle: {
          color: '#1890ff'
        },
        lineStyle: {
          type: 'dashed'
        }
      }
    ]
  };

  chartInstance.setOption(option);
};

/**
 * 监听数据变化
 */
watch(() => props.trendData, () => {
  updateChart();
}, { deep: true });

/**
 * 响应式调整
 */
const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

onMounted(() => {
  initChart();

  // 监听窗口大小变化
  window.addEventListener('resize', resizeChart);
});
</script>

<style scoped lang="less">
.trend-chart {
  width: 100%;
  height: 400px;
}
</style>
