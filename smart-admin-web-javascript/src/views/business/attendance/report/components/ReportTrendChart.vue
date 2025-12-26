<!--
  @fileoverview 报表趋势图表组件
  @author IOE-DREAM Team
  @description 考勤数据趋势分析可视化
-->
<template>
  <div ref="chartRef" class="report-chart" :style="{ height: height }" />
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

/**
 * 组件属性
 */
interface Props {
  chartData?: Record<string, any>;
  loading?: boolean;
  height?: string;
}

const props = withDefaults(defineProps<Props>(), {
  chartData: () => ({}),
  loading: false,
  height: '350px',
});

/**
 * 图表引用
 */
const chartRef = ref<HTMLDivElement>();

/**
 * 图表实例
 */
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
  if (!chartInstance || !props.chartData || Object.keys(props.chartData).length === 0) {
    return;
  }

  const dates = props.chartData.dates || [];
  const attendanceData = props.chartData.attendanceData || [];
  const lateData = props.chartData.lateData || [];
  const earlyLeaveData = props.chartData.earlyLeaveData || [];
  const overtimeData = props.chartData.overtimeData || [];

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999',
        },
      },
    },
    legend: {
      data: ['出勤人数', '迟到次数', '早退次数', '加班时长'],
      top: 10,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisPointer: {
        type: 'shadow',
      },
    },
    yAxis: [
      {
        type: 'value',
        name: '人数/次数',
        position: 'left',
        axisLabel: {
          formatter: '{value}',
        },
      },
      {
        type: 'value',
        name: '加班时长(小时)',
        position: 'right',
        axisLabel: {
          formatter: '{value} h',
        },
      },
    ],
    series: [
      {
        name: '出勤人数',
        type: 'line',
        smooth: true,
        data: attendanceData,
        itemStyle: {
          color: '#52c41a',
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' },
          ]),
        },
      },
      {
        name: '迟到次数',
        type: 'line',
        smooth: true,
        data: lateData,
        itemStyle: {
          color: '#faad14',
        },
      },
      {
        name: '早退次数',
        type: 'line',
        smooth: true,
        data: earlyLeaveData,
        itemStyle: {
          color: '#f5222d',
        },
      },
      {
        name: '加班时长',
        type: 'bar',
        yAxisIndex: 1,
        data: overtimeData,
        itemStyle: {
          color: '#722ed1',
        },
        barWidth: '40%',
      },
    ],
  };

  chartInstance.setOption(option);
};

/**
 * 监听数据变化
 */
watch(
  () => props.chartData,
  () => {
    updateChart();
  },
  { deep: true }
);

/**
 * 监听loading状态
 */
watch(
  () => props.loading,
  (newVal) => {
    if (newVal && chartInstance) {
      chartInstance.showLoading({
        text: '加载中...',
        color: '#1890ff',
        textColor: '#1890ff',
        maskColor: 'rgba(255, 255, 255, 0.8)',
      });
    } else if (chartInstance) {
      chartInstance.hideLoading();
    }
  }
);

/**
 * 响应式调整
 */
const handleResize = () => {
  chartInstance?.resize();
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});
</script>

<style scoped lang="less">
.report-chart {
  width: 100%;
}
</style>
