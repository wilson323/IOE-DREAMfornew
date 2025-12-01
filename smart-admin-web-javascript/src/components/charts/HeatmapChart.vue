<!--
 * 热力图组件
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <BaseChart
    ref="baseChart"
    :options="chartOptions"
    :width="width"
    :height="height"
    :loading="loading"
    :theme="theme"
    @ready="$emit('ready', $event)"
    @click="$emit('click', $event)"
  />
</template>

<script setup>
import { computed } from 'vue';
import BaseChart from './BaseChart.vue';
import { ChartConfigGenerator } from '/@/utils/chart-config';

const props = defineProps({
  // 数据
  data: {
    type: Array,
    default: () => []
  },
  // X轴数据
  xAxis: {
    type: Array,
    default: () => []
  },
  // Y轴数据
  yAxis: {
    type: Array,
    default: () => []
  },
  // 图表标题
  title: {
    type: String,
    default: ''
  },
  // 图表宽度
  width: {
    type: String,
    default: '100%'
  },
  // 图表高度
  height: {
    type: String,
    default: '400px'
  },
  // 最小值
  visualMin: {
    type: Number,
    default: 0
  },
  // 最大值
  visualMax: {
    type: Number,
    default: 100
  },
  // 是否显示标签
  showLabel: {
    type: Boolean,
    default: false
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 图表主题
  theme: {
    type: String,
    default: 'default'
  },
  // 自定义配置
  customOptions: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['ready', 'click']);

const baseChart = ref(null);

// 图表配置
const chartOptions = computed(() => {
  if (!props.data || props.data.length === 0) {
    return {};
  }

  return ChartConfigGenerator.createHeatmapConfig(
    {
      title: {
        text: props.title
      },
      series: [{
        data: props.data,
        name: props.title || '热力图'
      }],
      xAxis: props.xAxis,
      yAxis: props.yAxis
    },
    {
      visualMin: props.visualMin,
      visualMax: props.visualMax,
      showLabel: props.showLabel,
      ...props.customOptions
    }
  );
});

// 暴露方法
defineExpose({
  getChartInstance: () => baseChart.value?.getChartInstance(),
  updateChart: (options) => baseChart.value?.updateChart(options),
  resizeChart: () => baseChart.value?.resizeChart(),
  getDataURL: (type, pixelRatio, backgroundColor) =>
    baseChart.value?.getDataURL(type, pixelRatio, backgroundColor)
});
</script>