<!--
  * 统计分析弹窗组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    title="通行记录统计分析"
    :width="1200"
    :footer="null"
  >
    <div class="statistics-content">
      <!-- 统计时间范围选择 -->
      <div class="time-range-selector">
        <a-radio-group v-model:value="timeRange" button-style="solid" @change="handleTimeRangeChange">
          <a-radio-button value="today">今日</a-radio-button>
          <a-radio-button value="week">本周</a-radio-button>
          <a-radio-button value="month">本月</a-radio-button>
          <a-radio-button value="custom">自定义</a-radio-button>
        </a-radio-group>

        <a-range-picker
          v-if="timeRange === 'custom'"
          v-model:value="customDateRange"
          style="margin-left: 16px"
          @change="handleCustomDateChange"
        />
      </div>

      <!-- 统计概览卡片 -->
      <a-row :gutter="16" class="stats-overview">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="总通行次数"
              :value="statistics.totalCount || 0"
              :loading="loading"
            >
              <template #prefix><LoginOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card success">
            <a-statistic
              title="成功率"
              :value="statistics.successRate || 0"
              suffix="%"
              :loading="loading"
            >
              <template #prefix><CheckCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card warning">
            <a-statistic
              title="异常次数"
              :value="statistics.abnormalCount || 0"
              :loading="loading"
            >
              <template #prefix><ExclamationCircleOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card info">
            <a-statistic
              title="活跃用户"
              :value="statistics.activeUsers || 0"
              :loading="loading"
            >
              <template #prefix><UserOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <!-- 图表区域 -->
      <div class="charts-section">
        <a-row :gutter="16">
          <!-- 通行趋势图 -->
          <a-col :span="12">
            <a-card title="通行趋势" class="chart-card">
              <div ref="trendChartRef" style="height: 300px;"></div>
            </a-card>
          </a-col>

          <!-- 时段分布图 -->
          <a-col :span="12">
            <a-card title="时段分布" class="chart-card">
              <div ref="hourChartRef" style="height: 300px;"></div>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="16" style="margin-top: 16px;">
          <!-- 设备分布图 -->
          <a-col :span="12">
            <a-card title="设备分布" class="chart-card">
              <div ref="deviceChartRef" style="height: 300px;"></div>
            </a-card>
          </a-col>

          <!-- 验证方式分布图 -->
          <a-col :span="12">
            <a-card title="验证方式分布" class="chart-card">
              <div ref="verificationChartRef" style="height: 300px;"></div>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 详细统计表格 -->
      <a-card title="详细统计" class="detail-stats">
        <a-table
          :columns="detailColumns"
          :data-source="detailData"
          :loading="loading"
          :pagination="false"
          size="small"
        >
          <template #passRate="{ record }">
            <a-progress
              :percent="record.passRate"
              size="small"
              :stroke-color="getProgressColor(record.passRate)"
            />
          </template>
        </a-table>
      </a-card>

      <!-- 导出按钮 -->
      <div class="export-section">
        <a-space>
          <a-button @click="exportChart('trend')">
            <template #icon><ExportOutlined /></template>
            导出趋势图
          </a-button>
          <a-button @click="exportChart('distribution')">
            <template #icon><ExportOutlined /></template>
            导出分布图
          </a-button>
          <a-button type="primary" @click="exportStatistics">
            <template #icon><FileExcelOutlined /></template>
            导出统计报表
          </a-button>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import * as echarts from 'echarts';
  import dayjs from 'dayjs';
  import {
    LoginOutlined,
    CheckCircleOutlined,
    ExclamationCircleOutlined,
    UserOutlined,
    ExportOutlined,
    FileExcelOutlined,
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
  const timeRange = ref('week');
  const customDateRange = ref(null);
  const statistics = ref({});
  const detailData = ref([]);

  // 图表引用
  const trendChartRef = ref();
  const hourChartRef = ref();
  const deviceChartRef = ref();
  const verificationChartRef = ref();

  // 图表实例
  let trendChart = null;
  let hourChart = null;
  let deviceChart = null;
  let verificationChart = null;

  // 计算属性
  const visible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
  });

  // 详细统计表格列
  const detailColumns = [
    {
      title: '统计维度',
      dataIndex: 'dimension',
      key: 'dimension',
      width: 120,
    },
    {
      title: '分类名称',
      dataIndex: 'name',
      key: 'name',
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
      title: '成功次数',
      dataIndex: 'successCount',
      key: 'successCount',
      width: 100,
    },
    {
      title: '失败次数',
      dataIndex: 'failedCount',
      key: 'failedCount',
      width: 100,
    },
    {
      title: '成功率',
      key: 'passRate',
      width: 150,
      slots: { customRender: 'passRate' },
      sorter: (a, b) => a.passRate - b.passRate,
    },
    {
      title: '占比',
      dataIndex: 'percentage',
      key: 'percentage',
      width: 100,
      customRender: ({ text }) => `${text}%`,
    },
  ];

  // 监听弹窗显示
  watch(() => props.visible, (visible) => {
    if (visible) {
      loadStatistics();
      nextTick(() => {
        initCharts();
      });
    }
  });

  // 方法
  const handleTimeRangeChange = () => {
    if (timeRange.value !== 'custom') {
      customDateRange.value = null;
      loadStatistics();
    }
  };

  const handleCustomDateChange = () => {
    if (customDateRange.value && customDateRange.value.length === 2) {
      loadStatistics();
    }
  };

  const loadStatistics = async () => {
    loading.value = true;
    try {
      const params = getTimeRangeParams();

      // 获取统计数据
      const response = await accessRecordApi.getAccessStats(params);
      if (response.code === 1) {
        statistics.value = response.data;

        // 处理详细数据
        processDetailData(response.data);

        // 更新图表
        updateCharts(response.data);
      }
    } catch (error) {
      console.error('获取统计数据失败:', error);
      message.error('获取统计数据失败');
    } finally {
      loading.value = false;
    }
  };

  const getTimeRangeParams = () => {
    const params = {};

    if (timeRange.value === 'today') {
      params.timeRange = 'today';
    } else if (timeRange.value === 'week') {
      params.timeRange = 'week';
    } else if (timeRange.value === 'month') {
      params.timeRange = 'month';
    } else if (timeRange.value === 'custom' && customDateRange.value) {
      params.startTime = customDateRange.value[0].format('YYYY-MM-DD HH:mm:ss');
      params.endTime = customDateRange.value[1].format('YYYY-MM-DD HH:mm:ss');
    }

    return params;
  };

  const processDetailData = (data) => {
    const processed = [];

    // 设备统计数据
    if (data.deviceStats) {
      data.deviceStats.forEach(device => {
        processed.push({
          dimension: '设备',
          name: device.deviceName,
          count: device.totalCount || 0,
          successCount: device.successCount || 0,
          failedCount: device.failedCount || 0,
          passRate: device.totalCount > 0 ? Math.round((device.successCount / device.totalCount) * 100) : 0,
          percentage: Math.round((device.totalCount / data.totalCount) * 100),
        });
      });
    }

    // 用户统计数据
    if (data.userStats) {
      data.userStats.slice(0, 10).forEach(user => {
        processed.push({
          dimension: '用户',
          name: user.userName,
          count: user.totalCount || 0,
          successCount: user.successCount || 0,
          failedCount: user.failedCount || 0,
          passRate: user.totalCount > 0 ? Math.round((user.successCount / user.totalCount) * 100) : 0,
          percentage: Math.round((user.totalCount / data.totalCount) * 100),
        });
      });
    }

    detailData.value = processed;
  };

  const initCharts = () => {
    // 延迟初始化确保DOM已渲染
    setTimeout(() => {
      if (trendChartRef.value) {
        trendChart = echarts.init(trendChartRef.value);
      }
      if (hourChartRef.value) {
        hourChart = echarts.init(hourChartRef.value);
      }
      if (deviceChartRef.value) {
        deviceChart = echarts.init(deviceChartRef.value);
      }
      if (verificationChartRef.value) {
        verificationChart = echarts.init(verificationChartRef.value);
      }
    }, 100);
  };

  const updateCharts = (data) => {
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
            name: '成功',
            type: 'line',
            data: data.trendData.map(item => item.successCount),
            itemStyle: { color: '#52c41a' },
          },
          {
            name: '失败',
            type: 'line',
            data: data.trendData.map(item => item.failedCount),
            itemStyle: { color: '#ff4d4f' },
          },
        ],
      };
      trendChart.setOption(trendOption);
    }

    // 更新时段分布图
    if (hourChart && data.hourlyData) {
      const hourOption = {
        title: { text: '时段分布', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: data.hourlyData.map(item => item.hour),
        },
        yAxis: { type: 'value' },
        series: [
          {
            name: '通行次数',
            type: 'bar',
            data: data.hourlyData.map(item => item.count),
            itemStyle: { color: '#1890ff' },
          },
        ],
      };
      hourChart.setOption(hourOption);
    }

    // 更新设备分布图
    if (deviceChart && data.deviceStats) {
      const deviceOption = {
        title: { text: '设备分布', left: 'center' },
        tooltip: { trigger: 'item' },
        series: [
          {
            name: '设备通行',
            type: 'pie',
            radius: '60%',
            data: data.deviceStats.map(item => ({
              name: item.deviceName,
              value: item.totalCount || 0,
            })),
          },
        ],
      };
      deviceChart.setOption(deviceOption);
    }

    // 更新验证方式分布图
    if (verificationChart && data.verificationData) {
      const verificationOption = {
        title: { text: '验证方式分布', left: 'center' },
        tooltip: { trigger: 'item' },
        series: [
          {
            name: '验证方式',
            type: 'pie',
            radius: ['40%', '70%'],
            data: data.verificationData.map(item => ({
              name: getVerificationMethodText(item.method),
              value: item.count,
            })),
          },
        ],
      };
      verificationChart.setOption(verificationOption);
    }
  };

  const exportChart = (type) => {
    message.info(`${type === 'trend' ? '趋势图' : '分布图'}导出功能开发中...`);
  };

  const exportStatistics = () => {
    const params = getTimeRangeParams();
    params.format = 'excel';
    params.type = 'statistics';

    accessRecordApi.exportRecords(params)
      .then(response => {
        if (response.code === 1) {
          message.success('统计报表导出成功');
          // 处理文件下载
          const link = document.createElement('a');
          link.href = response.data.downloadUrl;
          link.download = response.data.fileName;
          link.click();
        }
      })
      .catch(error => {
        console.error('导出统计报表失败:', error);
        message.error('导出失败');
      });
  };

  const getProgressColor = (percent) => {
    if (percent >= 95) return '#52c41a';
    if (percent >= 80) return '#faad14';
    return '#ff4d4f';
  };

  const getVerificationMethodText = (method) => {
    const textMap = {
      card_only: '刷卡',
      card_password: '刷卡+密码',
      card_face: '刷卡+人脸',
      face_only: '人脸',
      fingerprint_only: '指纹',
      password_only: '密码',
      qr_code_only: '二维码',
    };
    return textMap[method] || method;
  };

  // 生命周期
  onUnmounted(() => {
    // 销毁图表实例
    if (trendChart) {
      trendChart.dispose();
    }
    if (hourChart) {
      hourChart.dispose();
    }
    if (deviceChart) {
      deviceChart.dispose();
    }
    if (verificationChart) {
      verificationChart.dispose();
    }
  });
</script>

<style lang="less" scoped>
  .statistics-content {
    .time-range-selector {
      display: flex;
      align-items: center;
      margin-bottom: 24px;
      padding: 16px;
      background: #fafafa;
      border-radius: 6px;
    }

    .stats-overview {
      margin-bottom: 24px;

      .stat-card {
        text-align: center;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        &.success {
          border-left: 4px solid #52c41a;
        }

        &.warning {
          border-left: 4px solid #faad14;
        }

        &.info {
          border-left: 4px solid #1890ff;
        }
      }
    }

    .charts-section {
      margin-bottom: 24px;

      .chart-card {
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }

    .detail-stats {
      margin-bottom: 24px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .export-section {
      text-align: center;
    }
  }
</style>