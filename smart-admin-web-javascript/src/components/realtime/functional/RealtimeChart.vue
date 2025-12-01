<template>
  <div class="realtime-chart">
    <div ref="chartContainer" class="chart-container" />

    <!-- 图表控制面板 -->
    <div v-if="showControls" class="chart-controls">
      <a-space>
        <a-button
          size="small"
          :type="isPaused ? 'primary' : 'default'"
          @click="togglePause"
        >
          {{ isPaused ? '继续' : '暂停' }}
        </a-button>
        <a-button size="small" @click="clearData">清空数据</a-button>
        <a-button size="small" @click="exportData">导出数据</a-button>
        <a-dropdown>
          <template #overlay>
            <a-menu @click="handleTimeRangeChange">
              <a-menu-item key="1m">1分钟</a-menu-item>
              <a-menu-item key="5m">5分钟</a-menu-item>
              <a-menu-item key="15m">15分钟</a-menu-item>
              <a-menu-item key="1h">1小时</a-menu-item>
              <a-menu-item key="all">全部</a-menu-item>
            </a-menu>
          </template>
          <a-button size="small">
            时间范围: {{ currentTimeRangeLabel }}
            <DownOutlined />
          </a-button>
        </a-dropdown>
        <a-button size="small" @click="showConfigModal = true">
          配置
        </a-button>
      </a-space>
    </div>

    <!-- 图表图例 -->
    <div v-if="showLegend && series.length > 1" class="chart-legend">
      <a-space>
        <div
          v-for="(s, index) in series"
          :key="s.name"
          class="legend-item"
          @click="toggleSeriesVisibility(index)"
        >
          <div
            class="legend-color"
            :style="{ backgroundColor: s.color }"
          />
          <span :class="{ 'legend-disabled': !s.visible }">
            {{ s.name }}
          </span>
          <span class="legend-value">
            {{ formatValue(s.currentValue) }}
          </span>
        </div>
      </a-space>
    </div>

    <!-- 配置弹窗 -->
    <a-modal
      v-model:open="showConfigModal"
      title="图表配置"
      :width="600"
      @ok="handleSaveConfig"
    >
      <ChartConfig
        :config="chartConfig"
        @update="handleConfigUpdate"
      />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import * as echarts from 'echarts';
import { useEventBus } from '@/utils/event-bus';
import { formatDateTime } from '@/utils/format';
import ChartConfig from './modals/ChartConfig.vue';

// Icons
import { DownOutlined } from '@ant-design/icons-vue';

// Props
const props = defineProps({
  // 图表主题
  theme: {
    type: String,
    default: 'light'
  },
  // 图表类型
  chartType: {
    type: String,
    default: 'line',
    validator: (value) => ['line', 'bar', 'area', 'scatter'].includes(value)
  },
  // 图表尺寸
  width: {
    type: [String, Number],
    default: '100%'
  },
  height: {
    type: [String, Number],
    default: 400
  },
  // 数据源
  dataSource: {
    type: String,
    required: true
  },
  // 数据系列配置
  series: {
    type: Array,
    default: () => []
  },
  // 最大数据点数量
  maxDataPoints: {
    type: Number,
    default: 100
  },
  // 刷新间隔(毫秒)
  refreshInterval: {
    type: Number,
    default: 1000
  },
  // 是否显示控制面板
  showControls: {
    type: Boolean,
    default: true
  },
  // 是否显示图例
  showLegend: {
    type: Boolean,
    default: true
  },
  // 是否自动缩放
  autoScale: {
    type: Boolean,
    default: true
  },
  // 平滑曲线
  smooth: {
    type: Boolean,
    default: true
  }
});

// Emits
const emit = defineEmits([
  'data-updated',
  'series-clicked',
  'zoom-changed',
  'config-changed'
]);

// 响应式数据
const chartContainer = ref(null);
const chartInstance = ref(null);
const isPaused = ref(false);
const showConfigModal = ref(false);
const currentTimeRange = ref('5m');
const dataBuffer = ref(new Map());

// 事件总线
const eventBus = useEventBus('realtime-chart');

// 图表配置
const chartConfig = ref({
  animation: true,
  grid: {
    top: 40,
    right: 40,
    bottom: 60,
    left: 60
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    },
    formatter: (params) => {
      return formatTooltip(params);
    }
  },
  xAxis: {
    type: 'time',
    splitLine: {
      show: false
    }
  },
  yAxis: {
    type: 'value',
    splitLine: {
      show: true
    }
  },
  dataZoom: [
    {
      type: 'inside',
      start: 0,
      end: 100
    },
    {
      start: 0,
      end: 100,
      handleStyle: {
        color: '#1890ff'
      }
    }
  ]
});

// 更新后的数据系列
const updatedSeries = computed(() => {
  return props.series.map(s => ({
    ...s,
    visible: s.visible !== false,
    data: getDataForSeries(s.name),
    currentValue: getCurrentValue(s.name)
  }));
});

// 时间范围标签
const currentTimeRangeLabel = computed(() => {
  const rangeMap = {
    '1m': '1分钟',
    '5m': '5分钟',
    '15m': '15分钟',
    '1h': '1小时',
    'all': '全部'
  };
  return rangeMap[currentTimeRange.value] || '5分钟';
});

// 方法
/**
 * 初始化图表
 */
function initChart() {
  if (!chartContainer.value) return;

  // 销毁现有实例
  if (chartInstance.value) {
    chartInstance.value.dispose();
  }

  // 创建新实例
  chartInstance.value = echarts.init(chartContainer.value, props.theme);

  // 设置选项
  updateChartOption();

  // 监听事件
  setupChartEvents();

  // 启动数据更新
  startDataUpdate();
}

/**
 * 更新图表选项
 */
function updateChartOption() {
  if (!chartInstance.value) return;

  const option = {
    ...chartConfig.value,
    series: updatedSeries.value.map(s => createSeriesOption(s))
  };

  chartInstance.value.setOption(option, true);
}

/**
 * 创建系列选项
 */
function createSeriesOption(series) {
  const baseOption = {
    name: series.name,
    type: props.chartType,
    data: series.data || [],
    smooth: props.smooth,
    showSymbol: false,
    emphasis: {
      focus: 'series'
    }
  };

  if (props.chartType === 'area') {
    baseOption.type = 'line';
    baseOption.areaStyle = {
      opacity: 0.3
    };
  }

  if (series.color) {
    baseOption.itemStyle = {
      color: series.color
    };
    baseOption.lineStyle = {
      color: series.color
    };
  }

  return baseOption;
}

/**
 * 设置图表事件
 */
function setupChartEvents() {
  if (!chartInstance.value) return;

  // 点击事件
  chartInstance.value.on('click', (params) => {
    emit('series-clicked', params);
  });

  // 缩放事件
  chartInstance.value.on('dataZoom', (params) => {
    emit('zoom-changed', params);
  });
}

/**
 * 获取系列数据
 */
function getDataForSeries(seriesName) {
  const buffer = dataBuffer.value.get(seriesName);
  if (!buffer) return [];

  // 根据时间范围过滤数据
  const now = Date.now();
  let startTime = 0;

  switch (currentTimeRange.value) {
    case '1m':
      startTime = now - 60 * 1000;
      break;
    case '5m':
      startTime = now - 5 * 60 * 1000;
      break;
    case '15m':
      startTime = now - 15 * 60 * 1000;
      break;
    case '1h':
      startTime = now - 60 * 60 * 1000;
      break;
  }

  const filteredData = buffer.filter(([timestamp]) => timestamp >= startTime);

  // 限制数据点数量
  if (filteredData.length > props.maxDataPoints) {
    return filteredData.slice(-props.maxDataPoints);
  }

  return filteredData;
}

/**
 * 获取当前值
 */
function getCurrentValue(seriesName) {
  const data = getDataForSeries(seriesName);
  return data.length > 0 ? data[data.length - 1][1] : 0;
}

/**
 * 格式化数值
 */
function formatValue(value) {
  if (typeof value === 'number') {
    if (value >= 1000000) {
      return (value / 1000000).toFixed(2) + 'M';
    } else if (value >= 1000) {
      return (value / 1000).toFixed(2) + 'K';
    }
    return value.toFixed(2);
  }
  return value;
}

/**
 * 格式化提示框
 */
function formatTooltip(params) {
  let result = formatDateTime(params[0].axisValue) + '<br/>';

  params.forEach((param, index) => {
    if (updatedSeries.value[index]?.visible) {
      result += `
        <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${param.color};"></span>
        ${param.seriesName}: ${formatValue(param.value[1])}<br/>
      `;
    }
  });

  return result;
}

/**
 * 添加数据点
 */
function addDataPoint(seriesName, value, timestamp = Date.now()) {
  if (!dataBuffer.value.has(seriesName)) {
    dataBuffer.value.set(seriesName, []);
  }

  const buffer = dataBuffer.value.get(seriesName);
  buffer.push([timestamp, value]);

  // 限制数据点数量
  if (buffer.length > props.maxDataPoints * 2) {
    buffer.splice(0, buffer.length - props.maxDataPoints * 2);
  }

  // 更新图表
  if (!isPaused.value) {
    updateChartOption();
  }
}

/**
 * 批量添加数据
 */
function addBatchData(data) {
  Object.entries(data).forEach(([seriesName, value]) => {
    addDataPoint(seriesName, value);
  });
}

/**
 * 开始数据更新
 */
function startDataUpdate() {
  // 监听数据源事件
  eventBus.on(`data:${props.dataSource}`, handleDataUpdate);

  // 定时更新
  const timer = setInterval(() => {
    if (!isPaused.value) {
      requestUpdate();
    }
  }, props.refreshInterval);

  onUnmounted(() => {
    clearInterval(timer);
    eventBus.off(`data:${props.dataSource}`, handleDataUpdate);
  });
}

/**
 * 处理数据更新
 */
function handleDataUpdate(data) {
  if (isPaused.value) return;

  if (Array.isArray(data)) {
    addBatchData(data);
  } else if (typeof data === 'object') {
    addBatchData(data);
  }
}

/**
 * 请求更新
 */
function requestUpdate() {
  // 可以在这里添加数据拉取逻辑
  updateChartOption();
}

/**
 * 切换暂停状态
 */
function togglePause() {
  isPaused.value = !isPaused.value;
}

/**
 * 清空数据
 */
function clearData() {
  dataBuffer.value.clear();
  updateChartOption();
  message.success('数据已清空');
}

/**
 * 导出数据
 */
function exportData() {
  const exportData = {};
  dataBuffer.value.forEach((data, seriesName) => {
    exportData[seriesName] = data;
  });

  const blob = new Blob([JSON.stringify(exportData, null, 2)], {
    type: 'application/json'
  });

  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `chart-data-${props.dataSource}-${Date.now()}.json`;
  link.click();
  URL.revokeObjectURL(url);

  message.success('数据导出成功');
}

/**
 * 处理时间范围变化
 */
function handleTimeRangeChange({ key }) {
  currentTimeRange.value = key;
  updateChartOption();
}

/**
 * 切换系列可见性
 */
function toggleSeriesVisibility(index) {
  const series = updatedSeries.value[index];
  series.visible = !series.visible;
  updateChartOption();
}

/**
 * 处理保存配置
 */
function handleSaveConfig() {
  showConfigModal.value = false;
  message.success('配置已保存');
  emit('config-changed', chartConfig.value);
}

/**
 * 处理配置更新
 */
function handleConfigUpdate(newConfig) {
  Object.assign(chartConfig.value, newConfig);
  updateChartOption();
}

/**
 * 调整图表大小
 */
function resizeChart() {
  if (chartInstance.value) {
    chartInstance.value.resize();
  }
}

// 监听器
watch(() => props.series, () => {
  updateChartOption();
}, { deep: true });

watch(() => props.theme, () => {
  nextTick(() => {
    initChart();
  });
});

watch(() => [props.width, props.height], () => {
  nextTick(() => {
    resizeChart();
  });
});

// 生命周期
onMounted(() => {
  nextTick(() => {
    initChart();
  });

  // 监听窗口大小变化
  window.addEventListener('resize', resizeChart);
});

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose();
  }
  window.removeEventListener('resize', resizeChart);
});

// 暴露方法
defineExpose({
  addDataPoint,
  addBatchData,
  clearData,
  exportData,
  resizeChart,
  getChartData: () => Object.fromEntries(dataBuffer.value),
  pause: () => { isPaused.value = true; },
  resume: () => { isPaused.value = false; }
});
</script>

<style lang="less" scoped>
.realtime-chart {
  .chart-container {
    width: v-bind('typeof props.width === "number" ? props.width + "px" : props.width');
    height: v-bind('typeof props.height === "number" ? props.height + "px" : props.height');
  }

  .chart-controls {
    margin-top: 16px;
    text-align: center;
  }

  .chart-legend {
    margin-top: 16px;
    padding: 12px;
    background: #fafafa;
    border-radius: 4px;

    .legend-item {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background-color 0.2s;

      &:hover {
        background: #e6f7ff;
      }

      .legend-color {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        margin-right: 8px;
      }

      .legend-disabled {
        color: #ccc;
      }

      .legend-value {
        margin-left: 8px;
        font-weight: bold;
        color: #1890ff;
      }
    }
  }
}
</style>