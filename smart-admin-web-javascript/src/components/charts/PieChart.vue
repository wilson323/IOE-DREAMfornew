<!--
 * 饼图组件
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
import { ChartConfigGenerator, COLOR_ARRAYS } from '/@/utils/chart-config';

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
    default: '400px'
  },
  // 饼图半径
  radius: {
    type: Array,
    default: () => ['40%', '70%']
  },
  // 饼图位置
  center: {
    type: Array,
    default: () => ['50%', '60%']
  },
  // 是否显示图例
  showLegend: {
    type: Boolean,
    default: true
  },
  // 是否显示标签
  showLabel: {
    type: Boolean,
    default: true
  },
  // 是否为环形图
  donut: {
    type: Boolean,
    default: false
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 颜色方案
  colorScheme: {
    type: String,
    default: 'default'
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

  const colors = COLOR_ARRAYS[props.colorScheme] || COLOR_ARRAYS.default;

  // 格式化数据
  const formattedData = props.data.map((item, index) => ({
    name: item.name,
    value: item.value,
    itemStyle: {
      color: item.color || colors[index % colors.length]
    }
  }));

  return ChartConfigGenerator.createPieConfig(
    {
      title: {
        text: props.title
      },
      series: [{
        name: props.title || '数据分布',
        data: formattedData,
        radius: props.donut ? props.radius : '70%',
        center: props.center,
        label: {
          show: props.showLabel,
          formatter: '{b}: {d}%'
        }
      }],
      legend: {
        show: props.showLegend,
        data: props.data.map(item => item.name),
        orient: 'vertical',
        left: 'left'
      }
    },
    {
      radius: props.radius,
      center: props.center,
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