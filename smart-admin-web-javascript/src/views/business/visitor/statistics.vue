<!--
 * 访客统计分析页面（增强版 + WebSocket实时推送）
 *
 * 功能：
 * - 统计概览卡片
 * - WebSocket实时数据推送
 * - 实时数据刷新（5/10/30/60秒）
 * - 7个ECharts图表
 * - 详细数据表格
 * - 导出功能
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-statistics-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h1>访客统计分析</h1>
        <p>实时监控访客数据，多维度分析统计</p>
      </div>
      <div class="header-actions">
        <a-space>
          <!-- WebSocket连接状态 -->
          <a-badge
            :count="wsStatusText"
            :number-style="{
              backgroundColor: wsStatusColor,
              color: '#fff',
              boxShadow: '0 0 0 1px ' + wsStatusColor
            }"
          >
            <a-button
              size="small"
              :icon="wsStatus === 1 ? h(WifiOutlined) : h(DisconnectOutlined)"
              :style="{ color: wsStatusColor }"
            >
              {{ wsStatus === 1 ? '实时连接' : '连接断开' }}
            </a-button>
          </a-badge>

          <a-divider type="vertical" />

          <!-- 自动刷新控制 -->
          <a-space>
            <span class="refresh-info">
              上次更新: {{ lastUpdateTime }}
            </span>
            <a-button
              size="small"
              :loading="refreshing"
              @click="manualRefresh"
            >
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
            <a-switch
              v-model:checked="autoRefresh"
              size="small"
              checked-children="自动"
              un-checked-children="手动"
            />
            <a-select
              v-model:value="refreshInterval"
              size="small"
              style="width: 100px"
              :disabled="!autoRefresh"
            >
              <a-select-option :value="5">5秒</a-select-option>
              <a-select-option :value="10">10秒</a-select-option>
              <a-select-option :value="30">30秒</a-select-option>
              <a-select-option :value="60">1分钟</a-select-option>
            </a-select>
          </a-space>

          <a-divider type="vertical" />

          <a-button @click="handleExportReport">
            <template #icon><DownloadOutlined /></template>
            导出报表
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- KPI统计卡片 -->
    <div class="kpi-cards">
      <!-- 骨架屏 -->
      <SkeletonLoader v-if="pageLoading" type="statistic" :statisticCount="4" />

      <!-- 实际内容 -->
      <a-row v-else :gutter="16">
        <a-col :span="6">
          <a-card :bordered="false" class="kpi-card primary">
            <a-statistic
              title="今日预约"
              :value="overviewStats.todayAppointments || 0"
              :value-style="{ color: '#1890ff', fontSize: '32px' }"
            >
              <template #prefix>
                <CalendarOutlined class="icon" />
              </template>
              <template #suffix>
                <a-tag :color="getTrendColor(overviewStats.appointmentTrend)">
                  {{ formatTrend(overviewStats.appointmentTrend) }}
                </a-tag>
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="kpi-card success">
            <a-statistic
              title="在访访客"
              :value="overviewStats.activeVisitors || 0"
              :value-style="{ color: '#52c41a', fontSize: '32px' }"
            >
              <template #prefix>
                <UserOutlined class="icon" />
              </template>
              <template #suffix>
                <a-tag :color="getTrendColor(overviewStats.visitorTrend)">
                  {{ formatTrend(overviewStats.visitorTrend) }}
                </a-tag>
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="kpi-card warning">
            <a-statistic
              title="待审批"
              :value="overviewStats.pendingApprovals || 0"
              :value-style="{ color: '#faad14', fontSize: '32px' }"
            >
              <template #prefix>
                <ClockCircleOutlined class="icon" />
              </template>
              <template #suffix>
                <a-tag color="orange">需处理</a-tag>
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="kpi-card info">
            <a-statistic
              title="本月访问"
              :value="overviewStats.monthlyVisitors || 0"
              :value-style="{ color: '#13c2c2', fontSize: '32px' }"
            >
              <template #prefix>
                <TeamOutlined class="icon" />
              </template>
              <template #suffix>
                <a-tag :color="getTrendColor(overviewStats.monthlyTrend)">
                  {{ formatTrend(overviewStats.monthlyTrend) }}
                </a-tag>
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 骨架屏 -->
      <SkeletonLoader v-if="chartsLoading" type="chart" :chartCount="4" />

      <!-- 实际内容 -->
      <template v-else>
        <a-row :gutter="16">
          <!-- 访客趋势图 -->
          <a-col :span="12">
            <a-card :bordered="false" title="访客流量趋势" class="chart-card">
              <div ref="trendChartRef" class="chart-container"></div>
            </a-card>
          </a-col>

          <!-- 访客类型分布 -->
          <a-col :span="12">
            <a-card :bordered="false" title="访客类型分布" class="chart-card">
              <div ref="typeChartRef" class="chart-container"></div>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="16" style="margin-top: 16px;">
          <!-- 时段分布 -->
          <a-col :span="12">
            <a-card :bordered="false" title="访问时段分布" class="chart-card">
              <div ref="timeDistChartRef" class="chart-container"></div>
            </a-card>
          </a-col>

          <!-- 区域分布 -->
          <a-col :span="12">
            <a-card :bordered="false" title="访问区域分布" class="chart-card">
              <div ref="areaDistChartRef" class="chart-container"></div>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="16" style="margin-top: 16px;">
          <!-- 访问目的统计 -->
          <a-col :span="8">
            <a-card :bordered="false" title="访问目的统计" class="chart-card">
              <div ref="purposeChartRef" class="chart-container"></div>
            </a-card>
          </a-col>

          <!-- 滞留时长分布 -->
          <a-col :span="8">
            <a-card :bordered="false" title="平均停留时长" class="chart-card">
              <div ref="durationChartRef" class="chart-container"></div>
            </a-card>
          </a-col>

          <!-- 满意度统计 -->
          <a-col :span="8">
            <a-card :bordered="false" title="访客满意度" class="chart-card">
              <div ref="satisfactionChartRef" class="chart-container"></div>
            </a-card>
          </a-col>
        </a-row>
      </template>
    </div>

    <!-- 详细统计表 -->
    <div class="detail-section">
      <a-card :bordered="false" title="详细统计数据" class="detail-card">
        <template #extra>
          <a-space>
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              @change="handleDateChange"
            />
            <a-button type="primary" @click="loadDetailData">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
          </a-space>
        </template>

        <!-- 骨架屏 -->
        <SkeletonLoader v-if="loadingDetail && detailData.length === 0" type="table" :rowCount="5" />

        <!-- 实际内容 -->
        <a-table
          v-else
          :columns="detailColumns"
          :data-source="detailData"
          :loading="loadingDetail && detailData.length > 0"
          :pagination="detailPagination"
          @change="handleDetailTableChange"
          size="small"
          :scroll="{ x: 1200 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'date'">
              <span>{{ record.date }}</span>
            </template>
            <template v-else-if="column.key === 'appointments'">
              <a-statistic
                :value="record.appointments"
                :value-style="{ fontSize: '14px', color: record.appointments > 0 ? '#1890ff' : '#999' }"
              />
            </template>
            <template v-else-if="column.key === 'checkInRate'">
              <a-progress
                :percent="record.checkInRate"
                :stroke-color="getRateColor(record.checkInRate)"
                size="small"
              />
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, computed, h } from 'vue';
import { message, notification } from 'ant-design-vue';
import {
  CalendarOutlined,
  UserOutlined,
  ClockCircleOutlined,
  TeamOutlined,
  ReloadOutlined,
  DownloadOutlined,
  SearchOutlined,
  WifiOutlined,
  DisconnectOutlined,
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import * as echarts from 'echarts';
import { visitorApi } from '/@/api/business/visitor/visitor-api';
import websocketService, { WebSocketStatus } from '/@/utils/websocket';
import SkeletonLoader from '/@/components/SkeletonLoader.vue';

// 自动刷新控制
const autoRefresh = ref(true);
const refreshInterval = ref(10);
const lastUpdateTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'));
const refreshing = ref(false);

// 页面加载状态
const pageLoading = ref(true);
const chartsLoading = ref(true);

// WebSocket连接状态
const wsStatus = ref(WebSocketStatus.CLOSED);
const wsStatusText = computed(() => {
  switch (wsStatus.value) {
    case WebSocketStatus.CONNECTING:
      return '连接中';
    case WebSocketStatus.OPEN:
      return '已连接';
    case WebSocketStatus.CLOSING:
      return '断开中';
    case WebSocketStatus.CLOSED:
      return '未连接';
    default:
      return '未知';
  }
});

const wsStatusColor = computed(() => {
  switch (wsStatus.value) {
    case WebSocketStatus.CONNECTING:
      return '#faad14';
    case WebSocketStatus.OPEN:
      return '#52c41a';
    case WebSocketStatus.CLOSING:
      return '#faad14';
    case WebSocketStatus.CLOSED:
      return '#ff4d4f';
    default:
      return '#999';
  }
});

// KPI概览数据
const overviewStats = reactive({
  todayAppointments: 0,
  activeVisitors: 0,
  pendingApprovals: 0,
  monthlyVisitors: 0,
  appointmentTrend: 0,
  visitorTrend: 0,
  monthlyTrend: 0,
});

// 详细数据
const detailData = ref([]);
const loadingDetail = ref(false);
const dateRange = ref([dayjs().subtract(7, 'days'), dayjs()]);

// 详细数据分页
const detailPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 详细表格列
const detailColumns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120, fixed: 'left' },
  { title: '预约数', dataIndex: 'appointments', key: 'appointments', width: 100, align: 'center' },
  { title: '签到数', dataIndex: 'checkIns', key: 'checkIns', width: 100, align: 'center' },
  { title: '签退数', dataIndex: 'checkOuts', key: 'checkOuts', width: 100, align: 'center' },
  { title: '签到率', dataIndex: 'checkInRate', key: 'checkInRate', width: 150, align: 'center' },
  { title: '平均停留(分钟)', dataIndex: 'avgDuration', key: 'avgDuration', width: 140, align: 'center' },
  { title: '新增访客', dataIndex: 'newVisitors', key: 'newVisitors', width: 100, align: 'center' },
  { title: '回头客', dataIndex: 'returnVisitors', key: 'returnVisitors', width: 100, align: 'center' },
];

// 图表引用
const trendChartRef = ref(null);
const typeChartRef = ref(null);
const timeDistChartRef = ref(null);
const areaDistChartRef = ref(null);
const purposeChartRef = ref(null);
const durationChartRef = ref(null);
const satisfactionChartRef = ref(null);

// 图表实例
let trendChart = null;
let typeChart = null;
let timeDistChart = null;
let areaDistChart = null;
let purposeChart = null;
let durationChart = null;
let satisfactionChart = null;

// 自动刷新定时器
let refreshTimer = null;

// 加载概览统计
const loadOverviewStats = async () => {
  try {
    const result = await visitorApi.getVisitorStatistics({
      startDate: dayjs().format('YYYY-MM-DD'),
      endDate: dayjs().format('YYYY-MM-DD'),
    });

    if (result.code === 200 || result.success) {
      Object.assign(overviewStats, result.data || {});
    }
  } catch (error) {
    console.error('加载概览统计失败:', error);
    message.error('加载概览统计失败');
  } finally {
    pageLoading.value = false;
  }
};

// 加载详细数据
const loadDetailData = async () => {
  loadingDetail.value = true;
  try {
    const params = {
      pageNum: detailPagination.current,
      pageSize: detailPagination.pageSize,
      startDate: dateRange.value[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value[1]?.format('YYYY-MM-DD'),
    };

    const result = await visitorApi.getAppointmentStatistics(params);

    if (result.code === 200 || result.success) {
      detailData.value = result.data?.list || [];
      detailPagination.total = result.data?.total || 0;
    }
  } catch (error) {
    message.error('加载详细数据失败');
    console.error('加载详细数据失败:', error);
  } finally {
    loadingDetail.value = false;
  }
};

// 初始化访客趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return;

  trendChart = echarts.init(trendChartRef.value);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985',
        },
      },
    },
    legend: {
      data: ['预约数', '签到数', '签退数'],
      top: 0,
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
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '预约数',
        type: 'line',
        data: [120, 132, 101, 134, 90, 230, 210],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0)' },
          ]),
        },
      },
      {
        name: '签到数',
        type: 'line',
        data: [110, 121, 91, 121, 85, 210, 195],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0)' },
          ]),
        },
      },
      {
        name: '签退数',
        type: 'line',
        data: [105, 115, 85, 115, 80, 200, 185],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(250, 173, 20, 0.3)' },
            { offset: 1, color: 'rgba(250, 173, 20, 0)' },
          ]),
        },
      },
    ],
  };

  trendChart.setOption(option);
};

// 初始化访客类型分布图
const initTypeChart = () => {
  if (!typeChartRef.value) return;

  typeChart = echarts.init(typeChartRef.value);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['普通访客', 'VIP访客', '常客', '临时访客'],
    },
    series: [
      {
        name: '访客类型',
        type: 'pie',
        radius: '60%',
        data: [
          { value: 548, name: '普通访客' },
          { value: 233, name: 'VIP访客' },
          { value: 310, name: '常客' },
          { value: 135, name: '临时访客' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
        label: {
          formatter: '{b}\n{d}%',
        },
      },
    ],
  };

  typeChart.setOption(option);
};

// 初始化时段分布图
const initTimeDistChart = () => {
  if (!timeDistChartRef.value) return;

  timeDistChart = echarts.init(timeDistChartRef.value);

  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: hours,
      axisLabel: {
        interval: 2,
        rotate: 30,
      },
    },
    yAxis: {
      type: 'value',
      name: '人数',
    },
    series: [
      {
        name: '访客数',
        type: 'bar',
        data: [
          5, 3, 2, 1, 2, 8, 25, 45, 78, 95, 120, 135,
          128, 145, 138, 125, 115, 98, 85, 65, 45, 28, 15, 8
        ],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' },
          ]),
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' },
            ]),
          },
        },
      },
    ],
  };

  timeDistChart.setOption(option);
};

// 初始化区域分布图
const initAreaDistChart = () => {
  if (!areaDistChartRef.value) return;

  areaDistChart = echarts.init(areaDistChartRef.value);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: ['A栋', 'B栋', 'C栋', 'D栋', 'E栋', '会议室', '餐厅', '其他'],
    },
    yAxis: {
      type: 'value',
      name: '访问次数',
    },
    series: [
      {
        name: '访问次数',
        type: 'bar',
        data: [235, 189, 156, 123, 98, 145, 267, 89],
        itemStyle: {
          color: (params) => {
            const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4'];
            return colors[params.dataIndex % colors.length];
          },
        },
      },
    ],
  };

  areaDistChart.setOption(option);
};

// 初始化访问目的统计图
const initPurposeChart = () => {
  if (!purposeChartRef.value) return;

  purposeChart = echarts.init(purposeChartRef.value);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    series: [
      {
        name: '访问目的',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          formatter: '{b}\n{d}%',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: [
          { value: 1048, name: '商务拜访' },
          { value: 735, name: '面试' },
          { value: 580, name: '个人拜访' },
          { value: 484, name: '其他' },
        ],
      },
    ],
  };

  purposeChart.setOption(option);
};

// 初始化停留时长统计图
const initDurationChart = () => {
  if (!durationChartRef.value) return;

  durationChart = echarts.init(durationChartRef.value);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: ['0-30分钟', '30-60分钟', '60-120分钟', '120-180分钟', '180分钟以上'],
    },
    yAxis: {
      type: 'value',
      name: '人数',
    },
    series: [
      {
        name: '人数',
        type: 'bar',
        data: [245, 389, 267, 156, 89],
        itemStyle: {
          color: (params) => {
            const colors = ['#91cc75', '#fac858', '#ee6666', '#73c0de', '#9a60b4'];
            return colors[params.dataIndex % colors.length];
          },
        },
      },
    ],
  };

  durationChart.setOption(option);
};

// 初始化满意度统计图
const initSatisfactionChart = () => {
  if (!satisfactionChartRef.value) return;

  satisfactionChart = echarts.init(satisfactionChartRef.value);

  const option = {
    tooltip: {
      formatter: '{b}: {c} ({d}%)',
    },
    series: [
      {
        name: '满意度',
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 5,
        itemStyle: {
          color: (params) => {
            const colors = ['#ee6666', '#fac858', '#91cc75', '#5470c6', '#73c0de'];
            return colors[params.dataIndex];
          },
        },
        progress: {
          show: true,
          overlap: false,
          roundCap: true,
          clip: false,
          itemStyle: {
            borderWidth: 1,
            borderColor: '#464646',
          },
        },
        axisLabel: {
          distance: 10,
          color: '#999',
          fontSize: 10,
        },
        axisLine: {
          lineStyle: {
            width: 10,
            color: [[1, '#464646']],
          },
        },
        splitLine: {
          lineStyle: {
            width: 10,
            color: '#464646',
          },
        },
        axisTick: {
          distance: -10,
          length: 8,
          lineStyle: {
            color: '#464646',
            width: 2,
          },
        },
        pointer: {
          itemStyle: {
            color: 'auto',
          },
        },
        title: {
          offsetCenter: [0, '-10%'],
          fontSize: 14,
          color: '#999',
        },
        detail: {
          fontSize: 24,
          fontWeight: 'bold',
          formatter: '{value}%',
          color: 'auto',
          offsetCenter: [0, '0%'],
        },
        data: [
          {
            value: 85,
            name: '满意度',
            title: {
              offsetCenter: [0, '30%'],
              fontSize: 14,
            },
          },
        ],
      },
    ],
  };

  satisfactionChart.setOption(option);
};

// 启动自动刷新
const startAutoRefresh = () => {
  stopAutoRefresh();

  if (!autoRefresh.value) {
    return;
  }

  refreshTimer = setInterval(async () => {
    await refreshData();
    lastUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
  }, refreshInterval.value * 1000);
};

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
};

// 刷新数据
const refreshData = async () => {
  refreshing.value = true;
  try {
    await Promise.all([
      loadOverviewStats(),
      // 可以添加更多数据加载
    ]);
  } finally {
    refreshing.value = false;
  }
};

// 手动刷新
const manualRefresh = async () => {
  await refreshData();
  message.success('刷新成功');
};

// 监听自动刷新配置变化
watch([autoRefresh, refreshInterval], () => {
  startAutoRefresh();
});

// 导出报表
const handleExportReport = async () => {
  try {
    const blob = await visitorApi.generateStatisticsReport({
      startDate: dateRange.value[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value[1]?.format('YYYY-MM-DD'),
      reportType: 'COMPREHENSIVE',
    });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `访客统计报表_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);

    message.success('报表导出成功');
  } catch (error) {
    console.error('导出报表失败:', error);
    message.error('导出报表失败');
  }
};

// 日期变化
const handleDateChange = () => {
  detailPagination.current = 1;
  loadDetailData();
};

// 详细表格变化
const handleDetailTableChange = (pag) => {
  detailPagination.current = pag.current;
  detailPagination.pageSize = pag.pageSize;
  loadDetailData();
};

// 获取趋势颜色
const getTrendColor = (trend) => {
  if (trend > 0) return 'green';
  if (trend < 0) return 'red';
  return 'default';
};

// 格式化趋势
const formatTrend = (trend) => {
  if (trend > 0) return `↑ ${trend}%`;
  if (trend < 0) return `↓ ${Math.abs(trend)}%`;
  return '无变化';
};

// 获取比率颜色
const getRateColor = (rate) => {
  if (rate >= 80) return '#52c41a';
  if (rate >= 60) return '#faad14';
  return '#ff4d4f';
};

// 处理窗口大小变化
const handleResize = () => {
  if (trendChart) trendChart.resize();
  if (typeChart) typeChart.resize();
  if (timeDistChart) timeDistChart.resize();
  if (areaDistChart) areaDistChart.resize();
  if (purposeChart) purposeChart.resize();
  if (durationChart) durationChart.resize();
  if (satisfactionChart) satisfactionChart.resize();
};

// ==================== WebSocket 实时数据推送 ====================

/**
 * 连接WebSocket
 */
const connectWebSocket = () => {
  // TODO: 从配置或环境变量获取WebSocket地址
  // const wsUrl = `ws://${window.location.host}/websocket/visitor`;
  const wsUrl = 'ws://localhost:8095/websocket/visitor'; // 示例地址

  websocketService.connect(wsUrl, {
    maxReconnectAttempts: 5,
    reconnectInterval: 3000,
    heartbeatInterval: 30000,

    // 监听状态变化
    onStatusChange: (status) => {
      wsStatus.value = status;
      console.log('[WebSocket] 状态变化:', wsStatusText.value);
    },

    // 监听消息
    onMessage: (data) => {
      console.log('[WebSocket] 收到消息:', data);
      handleWebSocketMessage(data);
    },
  });

  // 订阅访客统计数据推送
  websocketService.subscribeVisitorData((data) => {
    console.log('[WebSocket] 访客统计数据更新:', data);
    updateOverviewStats(data);
  });

  // 订阅新访客通知
  websocketService.subscribeNewVisitor((data) => {
    console.log('[WebSocket] 新访客通知:', data);
    showNewVisitorNotification(data);
  });
};

/**
 * 处理WebSocket消息
 */
const handleWebSocketMessage = (data) => {
  switch (data.type) {
    case 'visitor-stats':
      updateOverviewStats(data.payload);
      break;

    case 'new-visitor':
      showNewVisitorNotification(data.payload);
      break;

    case 'current-visitors':
      updateCurrentVisitors(data.payload);
      break;

    case 'chart-update':
      updateChartData(data.payload);
      break;

    default:
      console.log('[WebSocket] 未知消息类型:', data.type);
  }
};

/**
 * 更新概览统计数据
 */
const updateOverviewStats = (data) => {
  if (!data) return;

  // 更新KPI数据
  if (data.todayAppointments !== undefined) {
    overviewStats.todayAppointments = data.todayAppointments;
  }

  if (data.activeVisitors !== undefined) {
    overviewStats.activeVisitors = data.activeVisitors;
  }

  if (data.pendingApprovals !== undefined) {
    overviewStats.pendingApprovals = data.pendingApprovals;
  }

  if (data.monthlyVisitors !== undefined) {
    overviewStats.monthlyVisitors = data.monthlyVisitors;
  }

  // 更新趋势数据
  if (data.appointmentTrend !== undefined) {
    overviewStats.appointmentTrend = data.appointmentTrend;
  }

  if (data.visitorTrend !== undefined) {
    overviewStats.visitorTrend = data.visitorTrend;
  }

  if (data.monthlyTrend !== undefined) {
    overviewStats.monthlyTrend = data.monthlyTrend;
  }

  // 更新时间戳
  lastUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
};

/**
 * 显示新访客通知
 */
const showNewVisitorNotification = (data) => {
  if (!data || !data.visitorName) return;

  notification.info({
    message: '新访客到访',
    description: `${data.visitorName} 刚刚到达，被访人: ${data.hostName || '未知'}`,
    duration: 5,
    placement: 'topRight',
  });
};

/**
 * 更新在访访客数据
 */
const updateCurrentVisitors = (data) => {
  if (!data) return;

  overviewStats.activeVisitors = data.count || 0;
  lastUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
};

/**
 * 更新图表数据
 */
const updateChartData = (data) => {
  if (!data) return;

  // 根据图表类型更新对应图表
  switch (data.chartType) {
    case 'trend':
      updateTrendChart(data.chartData);
      break;

    case 'type':
      updateTypeChart(data.chartData);
      break;

    case 'timeDist':
      updateTimeDistChart(data.chartData);
      break;

    case 'areaDist':
      updateAreaDistChart(data.chartData);
      break;

    case 'purpose':
      updatePurposeChart(data.chartData);
      break;

    case 'duration':
      updateDurationChart(data.chartData);
      break;

    case 'satisfaction':
      updateSatisfactionChart(data.chartData);
      break;

    default:
      console.log('[WebSocket] 未知图表类型:', data.chartType);
  }
};

/**
 * 更新访客趋势图
 */
const updateTrendChart = (data) => {
  if (!trendChart || !data) return;

  const option = trendChart.getOption();
  if (data.series && data.series.length > 0) {
    option.series = data.series;
    if (data.xAxis) {
      option.xAxis = data.xAxis;
    }
    trendChart.setOption(option);
  }
};

/**
 * 更新访客类型分布图
 */
const updateTypeChart = (data) => {
  if (!typeChart || !data) return;

  const option = typeChart.getOption();
  if (data.series && data.series.length > 0) {
    option.series[0].data = data.series[0].data;
    typeChart.setOption(option);
  }
};

/**
 * 更新时段分布图
 */
const updateTimeDistChart = (data) => {
  if (!timeDistChart || !data) return;

  const option = timeDistChart.getOption();
  if (data.series && data.series.length > 0) {
    option.series[0].data = data.series[0].data;
    timeDistChart.setOption(option);
  }
};

/**
 * 更新区域分布图
 */
const updateAreaDistChart = (data) => {
  if (!areaDistChart || !data) return;

  const option = areaDistChart.getOption();
  if (data.series && data.series.length > 0) {
    option.series[0].data = data.series[0].data;
    if (data.xAxis) {
      option.xAxis.data = data.xAxis.data;
    }
    areaDistChart.setOption(option);
  }
};

/**
 * 更新访问目的统计图
 */
const updatePurposeChart = (data) => {
  if (!purposeChart || !data) return;

  const option = purposeChart.getOption();
  if (data.series && data.series.length > 0) {
    option.series[0].data = data.series[0].data;
    purposeChart.setOption(option);
  }
};

/**
 * 更新停留时长统计图
 */
const updateDurationChart = (data) => {
  if (!durationChart || !data) return;

  const option = durationChart.getOption();
  if (data.series && data.series.length > 0) {
    option.series[0].data = data.series[0].data;
    durationChart.setOption(option);
  }
};

/**
 * 更新满意度统计图
 */
const updateSatisfactionChart = (data) => {
  if (!satisfactionChart || !data) return;

  const option = satisfactionChart.getOption();
  if (data.series && data.series.length > 0) {
    option.series[0].data[0].value = data.series[0].data[0].value;
    satisfactionChart.setOption(option);
  }
};

/**
 * 断开WebSocket连接
 */
const disconnectWebSocket = () => {
  websocketService.close();
  wsStatus.value = WebSocketStatus.CLOSED;
};

// 初始化
onMounted(async () => {
  // 初始化所有图表
  initTrendChart();
  initTypeChart();
  initTimeDistChart();
  initAreaDistChart();
  initPurposeChart();
  initDurationChart();
  initSatisfactionChart();

  // 等待图表初始化完成
  await new Promise(resolve => setTimeout(resolve, 300));
  chartsLoading.value = false;

  // 加载数据
  loadOverviewStats();
  loadDetailData();

  // 启动自动刷新
  startAutoRefresh();

  // 连接WebSocket实时数据推送
  connectWebSocket();

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize);
});

// 清理
onUnmounted(() => {
  stopAutoRefresh();

  // 断开WebSocket连接
  disconnectWebSocket();

  // 销毁所有图表
  if (trendChart) {
    trendChart.dispose();
    trendChart = null;
  }
  if (typeChart) {
    typeChart.dispose();
    typeChart = null;
  }
  if (timeDistChart) {
    timeDistChart.dispose();
    timeDistChart = null;
  }
  if (areaDistChart) {
    areaDistChart.dispose();
    areaDistChart = null;
  }
  if (purposeChart) {
    purposeChart.dispose();
    purposeChart = null;
  }
  if (durationChart) {
    durationChart.dispose();
    durationChart = null;
  }
  if (satisfactionChart) {
    satisfactionChart.dispose();
    satisfactionChart = null;
  }

  window.removeEventListener('resize', handleResize);
});
</script>

<style lang="scss" scoped>
.visitor-statistics-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .page-title {
    h1 {
      font-size: 24px;
      font-weight: 600;
      margin: 0 0 8px 0;
      color: #1890ff;
    }

    p {
      margin: 0;
      color: #666;
      font-size: 14px;
    }
  }

  .refresh-info {
    color: #999;
    font-size: 12px;
  }
}

.kpi-cards {
  margin-bottom: 24px;

  .kpi-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    &.primary {
      border-top: 4px solid #1890ff;
    }

    &.success {
      border-top: 4px solid #52c41a;
    }

    &.warning {
      border-top: 4px solid #faad14;
    }

    &.info {
      border-top: 4px solid #13c2c2;
    }

    :deep(.ant-statistic) {
      .icon {
        font-size: 24px;
        margin-right: 8px;
      }
    }
  }
}

.charts-section {
  margin-bottom: 24px;

  .chart-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .chart-container {
      height: 320px;
    }
  }
}

.detail-section {
  .detail-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

// ==================== 响应式设计 ====================

// 平板设备 (768px - 1024px)
@media (max-width: 1024px) {
  .visitor-statistics-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 16px;
    padding: 16px;

    .header-actions {
      width: 100%;

      :deep(.ant-space) {
        flex-wrap: wrap;
      }
    }
  }

  .kpi-cards {
    :deep(.ant-col) {
      margin-bottom: 16px;
    }
  }

  .charts-section {
    .chart-card {
      margin-bottom: 16px;
    }
  }
}

// 移动设备 (<768px)
@media (max-width: 767px) {
  .visitor-statistics-page {
    padding: 12px;
  }

  .page-header {
    padding: 12px;
    margin-bottom: 12px;

    .page-title {
      h1 {
        font-size: 18px;
      }

      p {
        font-size: 12px;
      }
    }

    .header-actions {
      :deep(.ant-space) {
        gap: 8px !important;
      }

      :deep(.ant-btn) {
        font-size: 13px;
        padding: 4px 12px;
        height: auto;
      }
    }

    .refresh-info {
      display: none; // 移动端隐藏刷新时间
    }
  }

  // KPI卡片移动端优化
  .kpi-cards {
    :deep(.ant-row) {
      flex-direction: column;
    }

    .kpi-card {
      margin-bottom: 12px;

      :deep(.ant-statistic) {
        .ant-statistic-title {
          font-size: 13px;
        }

        .ant-statistic-content {
          font-size: 24px !important;
        }

        .icon {
          font-size: 20px;
        }
      }
    }
  }

  // 图表区域移动端优化
  .charts-section {
    margin-bottom: 12px;

    :deep(.ant-row) {
      flex-direction: column;
    }

    .chart-card {
      margin-bottom: 12px;

      .chart-container {
        height: 260px !important; // 移动端减小图表高度
      }
    }
  }

  // 详细统计表移动端优化
  .detail-section {
    .detail-card {
      :deep(.ant-table) {
        font-size: 12px;

        .ant-table-thead > tr > th {
          padding: 8px 4px;
          font-size: 12px;
        }

        .ant-table-tbody > tr > td {
          padding: 8px 4px;
        }

        // 隐藏次要列
        .ant-table-cell {
          &:nth-child(6),
          &:nth-child(7),
          &:nth-child(8) {
            display: none;
          }
        }
      }
    }
  }

  // 分页器移动端优化
  :deep(.ant-pagination) {
    .ant-pagination-total-text {
      display: none;
    }

    .ant-pagination-item {
      min-width: 28px;
      height: 28px;
      line-height: 26px;
      font-size: 12px;
    }

    .ant-pagination-prev,
    .ant-pagination-next {
      min-width: 28px;
      height: 28px;
      line-height: 26px;
    }

    .ant-select {
      font-size: 12px;
    }
  }
}

// 小屏手机 (<576px)
@media (max-width: 575px) {
  .visitor-statistics-page {
    padding: 8px;
  }

  .page-header {
    padding: 12px;

    .page-title {
      h1 {
        font-size: 16px;
      }

      p {
        display: none; // 小屏隐藏副标题
      }
    }

    .header-actions {
      :deep(.ant-btn) {
        font-size: 12px;
        padding: 4px 8px;

        span:not(.anticon) {
          display: none; // 只显示图标
        }
      }
    }
  }

  // KPI卡片小屏优化
  .kpi-cards {
    .kpi-card {
      :deep(.ant-statistic) {
        .ant-statistic-content {
          font-size: 20px !important;
        }

        .icon {
          font-size: 18px;
        }
      }
    }
  }

  // 图表小屏优化
  .charts-section {
    .chart-card {
      :deep(.ant-card-head) {
        padding: 8px 12px;

        .ant-card-head-title {
          font-size: 13px;
        }
      }

      .chart-container {
        height: 220px !important;
      }
    }
  }

  // 详细统计表小屏优化
  .detail-section {
    .detail-card {
      :deep(.ant-card-head) {
        padding: 8px 12px;

        .ant-card-head-title {
          font-size: 13px;
        }

        .ant-card-extra {
          .ant-space {
            gap: 4px !important;
          }

          .ant-btn {
            font-size: 12px;
            padding: 4px 8px;
          }
        }
      }
    }
  }
}
</style>
