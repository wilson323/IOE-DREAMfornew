<!--
  * 通行热力图弹窗组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    title="通行热力图"
    :width="1400"
    :footer="null"
  >
    <div class="heatmap-content">
      <!-- 控制面板 -->
      <div class="control-panel">
        <a-row :gutter="16" align="middle">
          <a-col :span="6">
            <a-select
              v-model:value="heatmapType"
              placeholder="选择热力图类型"
              style="width: 100%"
              @change="handleTypeChange"
            >
              <a-select-option value="time_location">时间-位置热力图</a-select-option>
              <a-select-option value="device_usage">设备使用热力图</a-select-option>
              <a-select-option value="user_activity">用户活动热力图</a-select-option>
              <a-select-option value="access_pattern">通行模式热力图</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-range-picker
              v-model:value="dateRange"
              style="width: 100%"
              @change="handleDateChange"
            />
          </a-col>
          <a-col :span="6">
            <a-select
              v-model:value="timeGranularity"
              placeholder="选择时间粒度"
              style="width: 100%"
              @change="handleGranularityChange"
            >
              <a-select-option value="hour">按小时</a-select-option>
              <a-select-option value="day">按天</a-select-option>
              <a-select-option value="week">按周</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-space>
              <a-button @click="refreshHeatmap" :loading="loading">
                <template #icon><ReloadOutlined /></template>
                刷新
              </a-button>
              <a-button @click="exportHeatmap">
                <template #icon><ExportOutlined /></template>
                导出
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <!-- 热力图统计信息 -->
      <div class="heatmap-stats">
        <a-row :gutter="16">
          <a-col :span="4">
            <a-statistic
              title="总通行次数"
              :value="heatmapStats.totalCount || 0"
              :loading="loading"
            />
          </a-col>
          <a-col :span="4">
            <a-statistic
              title="峰值时间"
              :value="heatmapStats.peakTime || '-'"
              :loading="loading"
            />
          </a-col>
          <a-col :span="4">
            <a-statistic
              title="最活跃位置"
              :value="heatmapStats.activeLocation || '-'"
              :loading="loading"
            />
          </a-col>
          <a-col :span="4">
            <a-statistic
              title="平均密度"
              :value="heatmapStats.avgDensity || 0"
              suffix="次/小时"
              :loading="loading"
            />
          </a-col>
          <a-col :span="4">
            <a-statistic
              title="覆盖范围"
              :value="heatmapStats.coverageRate || 0"
              suffix="%"
              :loading="loading"
            />
          </a-col>
          <a-col :span="4">
            <a-statistic
              title="活跃天数"
              :value="heatmapStats.activeDays || 0"
              :loading="loading"
            />
          </a-col>
        </a-row>
      </div>

      <!-- 热力图主体 -->
      <div class="heatmap-main">
        <div class="heatmap-container">
          <div ref="heatmapRef" class="heatmap-chart"></div>
        </div>

        <!-- 颜色图例 -->
        <div class="heatmap-legend">
          <div class="legend-title">通行密度</div>
          <div class="legend-gradient"></div>
          <div class="legend-labels">
            <span>低</span>
            <span>中</span>
            <span>高</span>
          </div>
        </div>
      </div>

      <!-- 详细数据表格 -->
      <div class="detail-data">
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="hotspot" tab="热点区域">
            <a-table
              :columns="hotspotColumns"
              :data-source="hotspotData"
              :loading="loading"
              :pagination="{ pageSize: 10 }"
              size="small"
            >
              <template #density="{ record }">
                <a-progress
                  :percent="record.density"
                  size="small"
                  :stroke-color="getDensityColor(record.density)"
                />
              </template>
            </a-table>
          </a-tab-pane>
          <a-tab-pane key="trend" tab="趋势分析">
            <div ref="trendChartRef" style="height: 300px;"></div>
          </a-tab-pane>
          <a-tab-pane key="comparison" tab="对比分析">
            <a-row :gutter="16">
              <a-col :span="12">
                <div ref="comparisonChartRef" style="height: 300px;"></div>
              </a-col>
              <a-col :span="12">
                <div ref="distributionChartRef" style="height: 300px;"></div>
              </a-col>
            </a-row>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import * as echarts from 'echarts';
  import {
    ReloadOutlined,
    ExportOutlined,
  } from '@ant-design/icons-vue';
  import { accessRecordApi } from '/@/api/business/access/record-api';

  // Props
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
  });

  // Emits
  const emit = defineEmits(['update:visible']);

  // 响应式数据
  const loading = ref(false);
  const heatmapType = ref('time_location');
  const dateRange = ref(null);
  const timeGranularity = ref('hour');
  const activeTab = ref('hotspot');
  const heatmapStats = ref({});
  const hotspotData = ref([]);

  // 图表引用
  const heatmapRef = ref();
  const trendChartRef = ref();
  const comparisonChartRef = ref();
  const distributionChartRef = ref();

  // 图表实例
  let heatmapChart = null;
  let trendChart = null;
  let comparisonChart = null;
  let distributionChart = null;

  // 计算属性
  const visible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
  });

  // 热点区域表格列
  const hotspotColumns = [
    {
      title: '排名',
      dataIndex: 'rank',
      key: 'rank',
      width: 60,
    },
    {
      title: '位置',
      dataIndex: 'location',
      key: 'location',
      width: 150,
    },
    {
      title: '通行次数',
      dataIndex: 'count',
      key: 'count',
      width: 100,
      sorter: (a, b) => a.count - b.count,
    },
    {
      title: '密度指数',
      key: 'density',
      width: 150,
      slots: { customRender: 'density' },
      sorter: (a, b) => a.density - b.density,
    },
    {
      title: '峰值时段',
      dataIndex: 'peakHour',
      key: 'peakHour',
      width: 100,
    },
    {
      title: '主要用户',
      dataIndex: 'mainUsers',
      key: 'mainUsers',
      width: 120,
      ellipsis: true,
    },
  ];

  // 监听弹窗显示
  watch(() => props.visible, (visible) => {
    if (visible) {
      loadHeatmapData();
      nextTick(() => {
        initCharts();
      });
    }
  });

  // 监听热力图类型变化
  watch(heatmapType, () => {
    loadHeatmapData();
  });

  // 方法
  const handleTypeChange = () => {
    loadHeatmapData();
  };

  const handleDateChange = () => {
    if (dateRange.value) {
      loadHeatmapData();
    }
  };

  const handleGranularityChange = () => {
    loadHeatmapData();
  };

  const loadHeatmapData = async () => {
    loading.value = true;
    try {
      const params = {
        type: heatmapType.value,
        granularity: timeGranularity.value,
      };

      if (dateRange.value && dateRange.value.length === 2) {
        params.startDate = dateRange.value[0].format('YYYY-MM-DD');
        params.endDate = dateRange.value[1].format('YYYY-MM-DD');
      }

      const response = await accessRecordApi.getHeatmapData(params);
      if (response.code === 1) {
        const data = response.data;

        // 更新统计信息
        heatmapStats.value = data.statistics || {};

        // 更新热力图数据
        updateHeatmapChart(data.heatmapData);

        // 更新热点数据
        hotspotData.value = data.hotspotData || [];

        // 更新其他图表
        updateOtherCharts(data);
      }
    } catch (error) {
      console.error('获取热力图数据失败:', error);
      message.error('获取热力图数据失败');
    } finally {
      loading.value = false;
    }
  };

  const updateHeatmapChart = (data) => {
    if (!heatmapChart || !data) return;

    const option = {
      title: {
        text: getHeatmapTitle(),
        left: 'center',
      },
      tooltip: {
        position: 'top',
        trigger: 'item',
        formatter: function (params) {
          return `${params.name}<br/>通行次数: ${params.data[2] || 0}`;
        },
      },
      grid: {
        height: '70%',
        top: '10%',
      },
      xAxis: {
        type: 'category',
        data: data.xAxis || [],
        splitArea: {
          show: true,
        },
      },
      yAxis: {
        type: 'category',
        data: data.yAxis || [],
        splitArea: {
          show: true,
        },
      },
      visualMap: {
        min: 0,
        max: data.maxValue || 100,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '5%',
        inRange: {
          color: ['#e0f3ff', '#1890ff', '#ff7a45', '#ff4d4f'],
        },
      },
      series: [
        {
          name: '通行次数',
          type: 'heatmap',
          data: data.data || [],
          label: {
            show: true,
            formatter: function (params) {
              return params.data[2] || '';
            },
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
        },
      ],
    };

    heatmapChart.setOption(option);
  };

  const updateOtherCharts = (data) => {
    // 更新趋势图
    if (trendChart && data.trendData) {
      const trendOption = {
        title: { text: '通行趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: data.trendData.map(item => item.time),
        },
        yAxis: { type: 'value' },
        series: [
          {
            name: '通行次数',
            type: 'line',
            data: data.trendData.map(item => item.count),
            smooth: true,
            itemStyle: { color: '#1890ff' },
          },
        ],
      };
      trendChart.setOption(trendOption);
    }

    // 更新对比图
    if (comparisonChart && data.comparisonData) {
      const comparisonOption = {
        title: { text: '设备对比', left: 'center' },
        tooltip: { trigger: 'axis' },
        legend: { top: 'bottom' },
        xAxis: {
          type: 'category',
          data: data.comparisonData.categories || [],
        },
        yAxis: { type: 'value' },
        series: data.comparisonData.series || [],
      };
      comparisonChart.setOption(comparisonOption);
    }

    // 更新分布图
    if (distributionChart && data.distributionData) {
      const distributionOption = {
        title: { text: '时段分布', left: 'center' },
        tooltip: { trigger: 'item' },
        series: [
          {
            name: '通行分布',
            type: 'pie',
            radius: '60%',
            data: data.distributionData || [],
          },
        ],
      };
      distributionChart.setOption(distributionOption);
    }
  };

  const getHeatmapTitle = () => {
    const titleMap = {
      time_location: '时间-位置通行热力图',
      device_usage: '设备使用热力图',
      user_activity: '用户活动热力图',
      access_pattern: '通行模式热力图',
    };
    return titleMap[heatmapType.value] || '通行热力图';
  };

  const initCharts = () => {
    // 延迟初始化确保DOM已渲染
    setTimeout(() => {
      if (heatmapRef.value) {
        heatmapChart = echarts.init(heatmapRef.value);
      }
      if (trendChartRef.value) {
        trendChart = echarts.init(trendChartRef.value);
      }
      if (comparisonChartRef.value) {
        comparisonChart = echarts.init(comparisonChartRef.value);
      }
      if (distributionChartRef.value) {
        distributionChart = echarts.init(distributionChartRef.value);
      }
    }, 100);
  };

  const refreshHeatmap = () => {
    loadHeatmapData();
  };

  const exportHeatmap = () => {
    message.info('热力图导出功能开发中...');
  };

  const getDensityColor = (density) => {
    if (density >= 80) return '#ff4d4f';
    if (density >= 60) return '#fa8c16';
    if (density >= 40) return '#fadb14';
    if (density >= 20) return '#a0d911';
    return '#52c41a';
  };

  // 生命周期
  onUnmounted(() => {
    // 销毁图表实例
    if (heatmapChart) {
      heatmapChart.dispose();
    }
    if (trendChart) {
      trendChart.dispose();
    }
    if (comparisonChart) {
      comparisonChart.dispose();
    }
    if (distributionChart) {
      distributionChart.dispose();
    }
  });
</script>

<style lang="less" scoped>
  .heatmap-content {
    .control-panel {
      margin-bottom: 24px;
      padding: 16px;
      background: #fafafa;
      border-radius: 6px;
    }

    .heatmap-stats {
      margin-bottom: 24px;
      padding: 16px;
      background: #f0f9ff;
      border: 1px solid #91d5ff;
      border-radius: 6px;
    }

    .heatmap-main {
      display: flex;
      margin-bottom: 24px;
      gap: 16px;

      .heatmap-container {
        flex: 1;
        background: white;
        border-radius: 6px;
        padding: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        .heatmap-chart {
          width: 100%;
          height: 500px;
        }
      }

      .heatmap-legend {
        width: 100px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: white;
        border-radius: 6px;
        padding: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        .legend-title {
          font-weight: 600;
          margin-bottom: 12px;
          color: #262626;
        }

        .legend-gradient {
          width: 20px;
          height: 200px;
          background: linear-gradient(to bottom, #e0f3ff, #1890ff, #ff7a45, #ff4d4f);
          border-radius: 4px;
          margin-bottom: 8px;
        }

        .legend-labels {
          display: flex;
          flex-direction: column;
          gap: 60px;
          font-size: 12px;
          color: #666;
        }
      }
    }

    .detail-data {
      background: white;
      border-radius: 6px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  }
</style>