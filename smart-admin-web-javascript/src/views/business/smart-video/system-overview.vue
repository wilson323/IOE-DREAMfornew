<!--
  智能视频-系统概览页面 (增强版)

  @Author:    IOE-DREAM Team
  @Date:      2025-01-30
  @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="system-overview">
    <!-- 自动刷新控制 -->
    <div class="refresh-control">
      <a-space>
        <span class="refresh-info">上次更新: {{ lastUpdateTime }}</span>
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
    </div>

    <!-- 第一行：KPI指标卡片 -->
    <a-row :gutter="16" class="kpi-row">
      <a-col :span="4">
        <a-card :bordered="false" class="kpi-card kpi-card-blue">
          <a-statistic
            title="设备总数"
            :value="statistics.totalDevices"
            suffix="台"
          >
            <template #prefix>
              <VideoCameraOutlined />
            </template>
          </a-statistic>
          <div class="kpi-trend">
            <a-tag color="green">+5</a-tag>
            <span class="trend-text">较上月</span>
          </div>
        </a-card>
      </a-col>

      <a-col :span="4">
        <a-card :bordered="false" class="kpi-card kpi-card-green">
          <a-statistic
            title="在线设备"
            :value="statistics.onlineDevices"
            suffix="台"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
          <div class="kpi-trend">
            <a-progress
              :percent="onlineRate"
              :show-info="true"
              size="small"
              :stroke-color="onlineRate > 90 ? '#52c41a' : onlineRate > 80 ? '#faad14' : '#ff4d4f'"
            />
          </div>
        </a-card>
      </a-col>

      <a-col :span="4">
        <a-card :bordered="false" class="kpi-card kpi-card-orange">
          <a-statistic
            title="实时视频流"
            :value="statistics.activeStreams"
            suffix="路"
          >
            <template #prefix>
              <PlayCircleOutlined />
            </template>
          </a-statistic>
          <div class="kpi-trend">
            <span class="trend-text">带宽: {{ statistics.totalBandwidth }} Mbps</span>
          </div>
        </a-card>
      </a-col>

      <a-col :span="4">
        <a-card :bordered="false" class="kpi-card kpi-card-red">
          <a-statistic
            title="今日告警"
            :value="statistics.todayAlarms"
            suffix="条"
          >
            <template #prefix>
              <WarningOutlined />
            </template>
          </a-statistic>
          <div class="kpi-trend">
            <a-tag :color="alarmChange >= 0 ? 'red' : 'green'">
              {{ alarmChange >= 0 ? '↑' : '↓' }} {{ Math.abs(alarmChange) }}%
            </a-tag>
            <span class="trend-text">较昨日</span>
          </div>
        </a-card>
      </a-col>

      <a-col :span="4">
        <a-card :bordered="false" class="kpi-card kpi-card-purple">
          <a-statistic
            title="录像任务"
            :value="statistics.recordingTasks"
            suffix="个"
          >
            <template #prefix>
              <VideoCameraAddOutlined />
            </template>
          </a-statistic>
          <div class="kpi-trend">
            <span class="trend-text">进行中: {{ statistics.recordingActive }} 个</span>
          </div>
        </a-card>
      </a-col>

      <a-col :span="4">
        <a-card :bordered="false" class="kpi-card kpi-card-cyan">
          <a-statistic
            title="存储使用"
            :value="statistics.storageUsage"
            suffix="%"
            :value-style="{ color: statistics.storageUsage > 80 ? '#ff4d4f' : '#1890ff' }"
          >
            <template #prefix>
              <DatabaseOutlined />
            </template>
          </a-statistic>
          <div class="kpi-trend">
            <span class="trend-text">{{ statistics.storageUsed }} / {{ statistics.storageTotal }} TB</span>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 第二行：核心图表 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <!-- 设备状态趋势 -->
      <a-col :span="16">
        <a-card title="设备状态趋势" :bordered="false" class="chart-card">
          <template #extra>
            <a-radio-group v-model:value="trendPeriod" button-style="solid" size="small">
              <a-radio-button value="day">今日</a-radio-button>
              <a-radio-button value="week">近7天</a-radio-button>
              <a-radio-button value="month">近30天</a-radio-button>
            </a-radio-group>
          </template>
          <div ref="deviceTrendChartRef" style="height: 280px;"></div>
        </a-card>
      </a-col>

      <!-- 告警级别分布 -->
      <a-col :span="8">
        <a-card title="告警级别分布" :bordered="false" class="chart-card">
          <div ref="alarmLevelChartRef" style="height: 280px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 第三行：详细监控 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <!-- 存储容量监控 -->
      <a-col :span="8">
        <a-card title="存储容量监控" :bordered="false" class="chart-card">
          <div ref="storageChartRef" style="height: 260px;"></div>
        </a-card>
      </a-col>

      <!-- 网络流量趋势 -->
      <a-col :span="8">
        <a-card title="网络流量趋势" :bordered="false" class="chart-card">
          <div ref="networkChartRef" style="height: 260px;"></div>
        </a-card>
      </a-col>

      <!-- 设备类型分布 -->
      <a-col :span="8">
        <a-card title="设备类型分布" :bordered="false" class="chart-card">
          <div ref="deviceTypeChartRef" style="height: 260px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 第四行：实时监控和操作 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <!-- 最新告警 -->
      <a-col :span="12">
        <a-card title="最新告警" :bordered="false" class="list-card">
          <template #extra>
            <a-space>
              <a-tag color="red">高危: {{ alarmStats.high }}</a-tag>
              <a-tag color="orange">中危: {{ alarmStats.medium }}</a-tag>
              <a-tag color="blue">低危: {{ alarmStats.low }}</a-tag>
              <a-button type="link" size="small" @click="goToAlarmPage">
                查看更多
                <RightOutlined />
              </a-button>
            </a-space>
          </template>
          <a-list
            :data-source="latestAlarms"
            :loading="loading"
            size="small"
            class="alarm-list"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-badge :status="getAlarmStatus(item.level)" />
                  </template>
                  <template #title>
                    <a-space>
                      <span class="alarm-device">{{ item.deviceName }}</span>
                      <a-tag :color="getAlarmLevelColor(item.level)" size="small">
                        {{ item.level }}
                      </a-tag>
                    </a-space>
                  </template>
                  <template #description>
                    {{ item.content }}
                  </template>
                </a-list-item-meta>
                <div class="alarm-time">{{ item.time }}</div>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 系统性能监控 -->
      <a-col :span="12">
        <a-card title="系统性能监控" :bordered="false" class="performance-card">
          <a-row :gutter="16">
            <a-col :span="12">
              <div class="performance-item">
                <div class="performance-label">
                  <CloudServerOutlined />
                  CPU使用率
                </div>
                <a-progress
                  :percent="systemStatus.cpuUsage"
                  :stroke-color="getPerformanceColor(systemStatus.cpuUsage)"
                  :stroke-width="12"
                />
                <div class="performance-detail">
                  {{ systemStatus.cpuCores }} 核 / {{ systemStatus.cpuFrequency }} GHz
                </div>
              </div>

              <div class="performance-item">
                <div class="performance-label">
                  <MemoryOutlined />
                  内存使用率
                </div>
                <a-progress
                  :percent="systemStatus.memoryUsage"
                  :stroke-color="getPerformanceColor(systemStatus.memoryUsage)"
                  :stroke-width="12"
                />
                <div class="performance-detail">
                  {{ systemStatus.memoryUsed }} GB / {{ systemStatus.memoryTotal }} GB
                </div>
              </div>

              <div class="performance-item">
                <div class="performance-label">
                  <DatabaseOutlined />
                  磁盘使用率
                </div>
                <a-progress
                  :percent="systemStatus.diskUsage"
                  :stroke-color="getPerformanceColor(systemStatus.diskUsage)"
                  :stroke-width="12"
                />
                <div class="performance-detail">
                  {{ systemStatus.diskUsed }} TB / {{ systemStatus.diskTotal }} TB
                </div>
              </div>
            </a-col>

            <a-col :span="12">
              <div class="performance-item">
                <div class="performance-label">
                  <ApiOutlined />
                  网络流量
                </div>
                <div class="network-stats">
                  <div class="network-item">
                    <ArrowUpOutlined class="network-icon upload" />
                    <span class="network-label">上行:</span>
                    <span class="network-value">{{ systemStatus.networkUpload }} Mbps</span>
                  </div>
                  <div class="network-item">
                    <ArrowDownOutlined class="network-icon download" />
                    <span class="network-label">下行:</span>
                    <span class="network-value">{{ systemStatus.networkDownload }} Mbps</span>
                  </div>
                </div>
              </div>

              <div class="performance-item">
                <div class="performance-label">
                  <TeamOutlined />
                  在线用户
                </div>
                <div class="user-stats">
                  <a-statistic
                    :value="systemStatus.onlineUsers"
                    suffix="人"
                    :value-style="{ fontSize: '28px', fontWeight: 'bold' }"
                  />
                </div>
              </div>

              <div class="performance-item">
                <div class="performance-label">
                  <ClockCircleOutlined />
                  系统信息
                </div>
                <div class="system-info">
                  <div>版本: {{ systemStatus.version }}</div>
                  <div>运行时长: {{ systemStatus.uptime }}</div>
                  <div>启动时间: {{ systemStatus.startTime }}</div>
                </div>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!-- 第五行：设备健康状况和区域分布 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <!-- 设备健康状况雷达图 -->
      <a-col :span="8">
        <a-card title="设备健康状况" :bordered="false" class="chart-card">
          <div ref="healthRadarChartRef" style="height: 300px;"></div>
        </a-card>
      </a-col>

      <!-- 区域设备分布 -->
      <a-col :span="8">
        <a-card title="区域设备分布" :bordered="false" class="chart-card">
          <div ref="areaChartRef" style="height: 300px;"></div>
        </a-card>
      </a-col>

      <!-- 快捷操作 -->
      <a-col :span="8">
        <a-card title="快捷操作" :bordered="false" class="action-card">
          <a-space direction="vertical" style="width: 100%;" size="middle">
            <a-button type="primary" block @click="quickAction('monitor')">
              <template #icon><VideoCameraOutlined /></template>
              打开实时监控
            </a-button>
            <a-button block @click="quickAction('playback')">
              <template #icon><HistoryOutlined /></template>
              查看录像回放
            </a-button>
            <a-button block @click="quickAction('alarm')">
              <template #icon><AlertOutlined /></template>
              处理告警信息
              <a-badge :count="statistics.todayAlarms" :offset="[10, 0]" />
            </a-button>
            <a-button block @click="quickAction('device')">
              <template #icon><SettingOutlined /></template>
              设备管理配置
            </a-button>
            <a-button block @click="quickAction('report')">
              <template #icon><FileTextOutlined /></template>
              生成统计报表
            </a-button>
            <a-button block @click="quickAction('system')">
              <template #icon><ControlOutlined /></template>
              系统设置
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
  VideoCameraOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  DatabaseOutlined,
  RightOutlined,
  ReloadOutlined,
  PlayCircleOutlined,
  VideoCameraAddOutlined,
  CloudServerOutlined,
  MemoryOutlined,
  ApiOutlined,
  TeamOutlined,
  ClockCircleOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  HistoryOutlined,
  AlertOutlined,
  SettingOutlined,
  FileTextOutlined,
  ControlOutlined,
} from '@ant-design/icons-vue';
import * as echarts from 'echarts';
import dayjs from 'dayjs';

const router = useRouter();

// 刷新控制
const autoRefresh = ref(true);
const refreshInterval = ref(10);
const refreshing = ref(false);
const lastUpdateTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'));

// 统计数据
const statistics = reactive({
  totalDevices: 156,
  onlineDevices: 142,
  activeStreams: 38,
  totalBandwidth: 856,
  todayAlarms: 23,
  recordingTasks: 45,
  recordingActive: 38,
  storageUsage: 68,
  storageUsed: 3.4,
  storageTotal: 5.0,
});

// 计算在线率
const onlineRate = computed(() => {
  return Math.round((statistics.onlineDevices / statistics.totalDevices) * 100);
});

// 告警变化率
const alarmChange = ref(-12);

// 告警统计
const alarmStats = reactive({
  high: 3,
  medium: 8,
  low: 12,
});

// 趋势周期
const trendPeriod = ref('day');

// 加载状态
const loading = ref(false);

// 最新告警
const latestAlarms = ref([
  {
    id: 1,
    deviceName: '前门摄像头-001',
    level: '高危',
    content: '检测到可疑人员徘徊',
    time: '15:32:10',
  },
  {
    id: 2,
    deviceName: '停车场摄像头-01',
    level: '中危',
    content: '车辆违停告警',
    time: '15:28:35',
  },
  {
    id: 3,
    deviceName: 'NVR存储设备-01',
    level: '低危',
    content: '存储空间不足80%',
    time: '15:15:22',
  },
  {
    id: 4,
    deviceName: '走廊摄像头-003',
    level: '中危',
    content: '设备离线',
    time: '14:58:10',
  },
  {
    id: 5,
    deviceName: '会议室摄像头-01',
    level: '低危',
    content: '画面模糊告警',
    time: '14:45:18',
  },
]);

// 系统状态
const systemStatus = reactive({
  cpuUsage: 42,
  cpuCores: 8,
  cpuFrequency: 2.4,
  memoryUsage: 68,
  memoryUsed: 10.9,
  memoryTotal: 16,
  diskUsage: 55,
  diskUsed: 5.5,
  diskTotal: 10,
  networkUpload: 125.6,
  networkDownload: 543.2,
  onlineUsers: 12,
  uptime: '15天 8小时 32分钟',
  version: 'v3.0.0',
  startTime: '2024-11-20 08:00:00',
});

// 图表引用
const deviceTrendChartRef = ref(null);
const alarmLevelChartRef = ref(null);
const storageChartRef = ref(null);
const networkChartRef = ref(null);
const deviceTypeChartRef = ref(null);
const healthRadarChartRef = ref(null);
const areaChartRef = ref(null);

let deviceTrendChart = null;
let alarmLevelChart = null;
let storageChart = null;
let networkChart = null;
let deviceTypeChart = null;
let healthRadarChart = null;
let areaChart = null;

// 获取告警状态
const getAlarmStatus = (level) => {
  const statusMap = {
    '高危': 'error',
    '中危': 'warning',
    '低危': 'default',
  };
  return statusMap[level] || 'default';
};

// 获取告警级别颜色
const getAlarmLevelColor = (level) => {
  const colorMap = {
    '高危': 'red',
    '中危': 'orange',
    '低危': 'blue',
  };
  return colorMap[level] || 'default';
};

// 获取性能颜色
const getPerformanceColor = (percent) => {
  if (percent >= 80) return '#ff4d4f';
  if (percent >= 60) return '#faad14';
  return '#52c41a';
};

// 跳转到告警页面
const goToAlarmPage = () => {
  router.push('/business/smart-video/history-alarm');
};

// 快捷操作
const quickAction = (action) => {
  const routes = {
    monitor: '/business/smart-video/monitor-preview',
    playback: '/business/smart-video/video-playback',
    alarm: '/business/smart-video/history-alarm',
    device: '/business/smart-video/device-list',
    report: '/business/smart-video/heat-statistics',
    system: '/system/config',
  };

  if (routes[action]) {
    router.push(routes[action]);
  } else {
    message.info('功能开发中');
  }
};

// 手动刷新
const manualRefresh = async () => {
  refreshing.value = true;
  try {
    await refreshData();
    message.success('数据刷新成功');
    lastUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
  } catch (error) {
    message.error('数据刷新失败');
  } finally {
    refreshing.value = false;
  }
};

// 刷新数据
const refreshData = async () => {
  // 模拟数据刷新
  statistics.onlineDevices = 138 + Math.floor(Math.random() * 10);
  statistics.activeStreams = 35 + Math.floor(Math.random() * 8);
  statistics.todayAlarms = 20 + Math.floor(Math.random() * 10);

  systemStatus.cpuUsage = 35 + Math.floor(Math.random() * 20);
  systemStatus.memoryUsage = 60 + Math.floor(Math.random() * 15);
  systemStatus.networkUpload = 100 + Math.floor(Math.random() * 50);
  systemStatus.networkDownload = 500 + Math.floor(Math.random() * 100);
};

// 初始化设备趋势图表
const initDeviceTrendChart = () => {
  if (!deviceTrendChartRef.value) return;

  deviceTrendChart = echarts.init(deviceTrendChartRef.value);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['在线设备', '播放视频', '录像任务'],
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
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '在线设备',
        type: 'line',
        data: [138, 142, 145, 143, 140, 142, 144],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0)' },
          ]),
        },
      },
      {
        name: '播放视频',
        type: 'line',
        data: [25, 18, 35, 42, 38, 35, 32],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0)' },
          ]),
        },
      },
      {
        name: '录像任务',
        type: 'line',
        data: [40, 42, 43, 45, 44, 43, 42],
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

  deviceTrendChart.setOption(option);
};

// 初始化告警级别图表
const initAlarmLevelChart = () => {
  if (!alarmLevelChartRef.value) return;

  alarmLevelChart = echarts.init(alarmLevelChartRef.value);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
    },
    series: [
      {
        name: '告警级别',
        type: 'pie',
        radius: ['45%', '75%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}: {c}',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
          },
        },
        data: [
          { value: alarmStats.high, name: '高危', itemStyle: { color: '#ff4d4f' } },
          { value: alarmStats.medium, name: '中危', itemStyle: { color: '#faad14' } },
          { value: alarmStats.low, name: '低危', itemStyle: { color: '#1890ff' } },
        ],
      },
    ],
  };

  alarmLevelChart.setOption(option);
};

// 初始化存储图表
const initStorageChart = () => {
  if (!storageChartRef.value) return;

  storageChart = echarts.init(storageChartRef.value);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: ['NVR-01', 'NVR-02', 'NVR-03', 'NVR-04', 'NVR-05'],
    },
    yAxis: {
      type: 'value',
      name: '使用率 (%)',
      max: 100,
    },
    series: [
      {
        name: '存储使用率',
        type: 'bar',
        data: [68, 72, 45, 85, 58],
        itemStyle: {
          color: (params) => {
            const colorList = ['#52c41a', '#52c41a', '#52c41a', '#ff4d4f', '#faad14'];
            return colorList[params.dataIndex];
          },
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%'
        }
      },
    ],
  };

  storageChart.setOption(option);
};

// 初始化网络流量图表
const initNetworkChart = () => {
  if (!networkChartRef.value) return;

  networkChart = echarts.init(networkChartRef.value);

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['上行流量', '下行流量'],
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
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
    },
    yAxis: {
      type: 'value',
      name: 'Mbps',
    },
    series: [
      {
        name: '上行流量',
        type: 'line',
        data: [85, 92, 125, 142, 138, 132, 128],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0)' },
          ]),
        },
      },
      {
        name: '下行流量',
        type: 'line',
        data: [420, 485, 543, 568, 552, 538, 525],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0)' },
          ]),
        },
      },
    ],
  };

  networkChart.setOption(option);
};

// 初始化设备类型图表
const initDeviceTypeChart = () => {
  if (!deviceTypeChartRef.value) return;

  deviceTypeChart = echarts.init(deviceTypeChartRef.value);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
    },
    series: [
      {
        name: '设备类型',
        type: 'pie',
        radius: '60%',
        data: [
          { value: 95, name: '网络摄像机' },
          { value: 42, name: 'NVR' },
          { value: 12, name: 'DVR' },
          { value: 7, name: '解码器' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      },
    ],
  };

  deviceTypeChart.setOption(option);
};

// 初始化设备健康雷达图
const initHealthRadarChart = () => {
  if (!healthRadarChartRef.value) return;

  healthRadarChart = echarts.init(healthRadarChartRef.value);

  const option = {
    tooltip: {},
    radar: {
      indicator: [
        { name: '在线率', max: 100 },
        { name: '画质', max: 100 },
        { name: '稳定性', max: 100 },
        { name: '响应速度', max: 100 },
        { name: '存储健康', max: 100 },
      ],
      radius: 65,
    },
    series: [
      {
        name: '设备健康度',
        type: 'radar',
        data: [
          {
            value: [91, 85, 88, 82, 75],
            name: '当前状态',
            areaStyle: {
              color: 'rgba(24, 144, 255, 0.3)'
            }
          },
        ],
      },
    ],
  };

  healthRadarChart.setOption(option);
};

// 初始化区域分布图表
const initAreaChart = () => {
  if (!areaChartRef.value) return;

  areaChart = echarts.init(areaChartRef.value);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: ['主出入口', '停车场', '办公区', '会议区', '餐厅', '宿舍区'],
      axisLabel: {
        rotate: 30,
      },
    },
    yAxis: {
      type: 'value',
      name: '设备数量',
    },
    series: [
      {
        name: '设备数量',
        type: 'bar',
        data: [28, 35, 42, 18, 22, 11],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' }
          ])
        },
        label: {
          show: true,
          position: 'top'
        }
      },
    ],
  };

  areaChart.setOption(option);
};

// 窗口resize处理
const handleResize = () => {
  deviceTrendChart?.resize();
  alarmLevelChart?.resize();
  storageChart?.resize();
  networkChart?.resize();
  deviceTypeChart?.resize();
  healthRadarChart?.resize();
  areaChart?.resize();
};

// 定时更新数据
let updateTimer = null;

const startDataUpdate = () => {
  if (updateTimer) {
    clearInterval(updateTimer);
  }

  if (!autoRefresh.value) {
    return;
  }

  updateTimer = setInterval(async () => {
    await refreshData();
    lastUpdateTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
  }, refreshInterval.value * 1000);
};

// 监听自动刷新配置变化
watch([autoRefresh, refreshInterval], () => {
  startDataUpdate();
});

// 生命周期
onMounted(() => {
  initDeviceTrendChart();
  initAlarmLevelChart();
  initStorageChart();
  initNetworkChart();
  initDeviceTypeChart();
  initHealthRadarChart();
  initAreaChart();

  window.addEventListener('resize', handleResize);
  startDataUpdate();
});

onUnmounted(() => {
  if (deviceTrendChart) {
    deviceTrendChart.dispose();
    deviceTrendChart = null;
  }
  if (alarmLevelChart) {
    alarmLevelChart.dispose();
    alarmLevelChart = null;
  }
  if (storageChart) {
    storageChart.dispose();
    storageChart = null;
  }
  if (networkChart) {
    networkChart.dispose();
    networkChart = null;
  }
  if (deviceTypeChart) {
    deviceTypeChart.dispose();
    deviceTypeChart = null;
  }
  if (healthRadarChart) {
    healthRadarChart.dispose();
    healthRadarChart = null;
  }
  if (areaChart) {
    areaChart.dispose();
    areaChart = null;
  }

  window.removeEventListener('resize', handleResize);
  if (updateTimer) {
    clearInterval(updateTimer);
    updateTimer = null;
  }
});
</script>

<style scoped lang="less">
.system-overview {
  padding: 24px;
  background-color: #f0f2f5;

  .refresh-control {
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #fff;
    border-radius: 4px;
    display: flex;
    justify-content: flex-end;
    align-items: center;

    .refresh-info {
      font-size: 12px;
      color: #666;
      margin-right: 8px;
    }
  }

  .kpi-row {
    margin-bottom: 0;
  }

  .kpi-card {
    position: relative;
    overflow: hidden;

    :deep(.ant-card-body) {
      padding: 20px 24px;
    }

    &.kpi-card-blue {
      border-top: 3px solid #1890ff;
    }

    &.kpi-card-green {
      border-top: 3px solid #52c41a;
    }

    &.kpi-card-orange {
      border-top: 3px solid #faad14;
    }

    &.kpi-card-red {
      border-top: 3px solid #ff4d4f;
    }

    &.kpi-card-purple {
      border-top: 3px solid #722ed1;
    }

    &.kpi-card-cyan {
      border-top: 3px solid #13c2c2;
    }

    .kpi-trend {
      margin-top: 12px;
      display: flex;
      align-items: center;
      gap: 8px;

      .trend-text {
        font-size: 12px;
        color: #666;
      }
    }
  }

  .chart-card {
    :deep(.ant-card-body) {
      padding: 20px;
    }
  }

  .list-card {
    :deep(.ant-card-body) {
      padding: 16px 20px;
    }

    .alarm-list {
      max-height: 280px;
      overflow-y: auto;

      .alarm-device {
        font-weight: 500;
      }

      .alarm-time {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .performance-card {
    :deep(.ant-card-body) {
      padding: 20px;
    }

    .performance-item {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .performance-label {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        font-weight: 500;
        font-size: 14px;
      }

      .performance-detail {
        margin-top: 8px;
        font-size: 12px;
        color: #666;
      }

      .network-stats {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .network-item {
          display: flex;
          align-items: center;
          gap: 8px;

          .network-icon {
            font-size: 16px;

            &.upload {
              color: #52c41a;
            }

            &.download {
              color: #1890ff;
            }
          }

          .network-label {
            font-size: 13px;
            color: #666;
          }

          .network-value {
            font-weight: 600;
            font-size: 14px;
          }
        }
      }

      .user-stats {
        text-align: center;
        padding: 12px 0;
      }

      .system-info {
        font-size: 12px;
        color: #666;
        line-height: 1.8;
      }
    }
  }

  .action-card {
    :deep(.ant-card-body) {
      padding: 20px;
    }
  }

  :deep(.ant-statistic-title) {
    font-size: 13px;
    color: rgba(0, 0, 0, 0.65);
    font-weight: 500;
  }

  :deep(.ant-statistic-content) {
    font-size: 28px;
    font-weight: 600;
  }
}
</style>
