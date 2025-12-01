<!--
 * 通用基础图表组件
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div
    ref="chartContainer"
    class="base-chart"
    :style="{ width: width, height: height }"
    :class="{ 'is-loading': loading }"
  >
    <div v-if="loading" class="chart-loading">
      <a-spin size="large" tip="加载中..." />
    </div>
    <div v-if="!loading && !hasData" class="chart-no-data">
      <a-empty description="暂无数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick, computed } from 'vue';
import * as echarts from 'echarts';
import { ChartUtils } from '/@/utils/chart-config';

const props = defineProps({
  // 图表配置
  options: {
    type: Object,
    default: () => ({})
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
  // 是否自动调整大小
  autoResize: {
    type: Boolean,
    default: true
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 是否显示数据为空提示
  showNoData: {
    type: Boolean,
    default: true
  },
  // 图表主题
  theme: {
    type: String,
    default: 'default'
  }
});

const emit = defineEmits(['ready', 'click', 'dblclick', 'mouseover', 'mouseout']);

const chartContainer = ref(null);
const chartInstance = ref(null);
const resizeObserver = ref(null);

// 是否有数据
const hasData = computed(() => {
  if (!props.options.series) return false;
  return props.options.series.some(series => {
    return series.data && series.data.length > 0;
  });
});

// 初始化图表
const initChart = async () => {
  if (!chartContainer.value) return;

  await nextTick();

  // 销毁旧实例
  if (chartInstance.value) {
    chartInstance.value.dispose();
  }

  // 创建新实例
  chartInstance.value = echarts.init(chartContainer.value, props.theme);

  // 设置配置项
  if (props.options && hasData.value) {
    chartInstance.value.setOption(props.options, true);
  }

  // 绑定事件
  bindEvents();

  // 发送准备完成事件
  emit('ready', chartInstance.value);
};

// 绑定图表事件
const bindEvents = () => {
  if (!chartInstance.value) return;

  // 点击事件
  chartInstance.value.on('click', (params) => {
    emit('click', params);
  });

  // 双击事件
  chartInstance.value.on('dblclick', (params) => {
    emit('dblclick', params);
  });

  // 鼠标悬停事件
  chartInstance.value.on('mouseover', (params) => {
    emit('mouseover', params);
  });

  // 鼠标离开事件
  chartInstance.value.on('mouseout', (params) => {
    emit('mouseout', params);
  });
};

// 更新图表配置
const updateChart = (options) => {
  if (chartInstance.value && options) {
    chartInstance.value.setOption(options, true);
  }
};

// 调整图表大小
const resizeChart = () => {
  if (chartInstance.value) {
    ChartUtils.resizeChart(chartInstance.value, chartContainer.value);
  }
};

// 获取图表实例
const getChartInstance = () => {
  return chartInstance.value;
};

// 获取图表数据URL（用于导出图片）
const getDataURL = (type = 'png', pixelRatio = 1, backgroundColor = '#fff') => {
  if (chartInstance.value) {
    return chartInstance.value.getDataURL({
      type,
      pixelRatio,
      backgroundColor
    });
  }
  return '';
};

// 显示加载动画
const showLoading = (opts = {}) => {
  if (chartInstance.value) {
    chartInstance.value.showLoading('default', {
      text: '加载中...',
      color: '#1890ff',
      textColor: '#000',
      maskColor: 'rgba(255, 255, 255, 0.8)',
      zlevel: 0,
      ...opts
    });
  }
};

// 隐藏加载动画
const hideLoading = () => {
  if (chartInstance.value) {
    chartInstance.value.hideLoading();
  }
};

// 监听配置变化
watch(
  () => props.options,
  (newOptions) => {
    if (newOptions && chartInstance.value) {
      updateChart(newOptions);
    }
  },
  { deep: true }
);

// 监听主题变化
watch(
  () => props.theme,
  () => {
    initChart();
  }
);

// 监听窗口大小变化
const handleResize = () => {
  if (props.autoResize) {
    resizeChart();
  }
};

// 监听容器大小变化
const setupResizeObserver = () => {
  if (props.autoResize && chartContainer.value && window.ResizeObserver) {
    resizeObserver.value = new ResizeObserver(handleResize);
    resizeObserver.value.observe(chartContainer.value);
  }
};

// 清理资源
const cleanup = () => {
  if (resizeObserver.value) {
    resizeObserver.value.disconnect();
    resizeObserver.value = null;
  }
  if (chartInstance.value) {
    chartInstance.value.dispose();
    chartInstance.value = null;
  }
  window.removeEventListener('resize', handleResize);
};

onMounted(() => {
  initChart();
  setupResizeObserver();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  cleanup();
});

// 暴露方法给父组件
defineExpose({
  updateChart,
  resizeChart,
  getChartInstance,
  getDataURL,
  showLoading,
  hideLoading
});
</script>

<style scoped>
.base-chart {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 6px;
  overflow: hidden;
}

.chart-loading,
.chart-no-data {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 1;
}

.is-loading {
  pointer-events: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .base-chart {
    height: 300px !important;
  }
}
</style>