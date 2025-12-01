<!--
 * 仪表盘组件
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
    default: '300px'
  },
  // 最小值
  min: {
    type: Number,
    default: 0
  },
  // 最大值
  max: {
    type: Number,
    default: 100
  },
  // 仪表盘位置
  center: {
    type: Array,
    default: () => ['50%', '60%']
  },
  // 仪表盘半径
  radius: {
    type: String,
    default: '90%'
  },
  // 单位
  unit: {
    type: String,
    default: '%'
  },
  // 分割段数
  splitNumber: {
    type: Number,
    default: 10
  },
  // 起始角度
  startAngle: {
    type: Number,
    default: 225
  },
  // 结束角度
  endAngle: {
    type: Number,
    default: -45
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

  return ChartConfigGenerator.createGaugeConfig(
    {
      title: {
        text: props.title
      },
      series: props.data.map(item => ({
        name: item.name,
        data: [{ value: item.value, name: item.name }]
      }))
    },
    {
      min: props.min,
      max: props.max,
      center: props.center,
      radius: props.radius,
      unit: props.unit,
      splitNumber: props.splitNumber,
      startAngle: props.startAngle,
      endAngle: props.endAngle,
      detailFontSize: 24,
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