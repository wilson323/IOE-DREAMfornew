<!--
  智能视频-系统概览页面

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="system-overview">
    <!-- 顶部KPI指标卡片 -->
    <a-row :gutter="16" class="kpi-row">
      <a-col :span="6">
        <a-card :bordered="false" class="kpi-card">
          <a-statistic
            title="设备总数"
            :value="statistics.totalDevices"
            suffix="台"
          >
            <template #prefix>
              <VideoCameraOutlined style="color: #1890ff" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :span="6">
        <a-card :bordered="false" class="kpi-card">
          <a-statistic
            title="在线设备"
            :value="statistics.onlineDevices"
            suffix="台"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
          <div class="kpi-extra">
            在线率: {{ onlineRate }}%
          </div>
        </a-card>
      </a-col>

      <a-col :span="6">
        <a-card :bordered="false" class="kpi-card">
          <a-statistic
            title="今日告警"
            :value="statistics.todayAlarms"
            suffix="条"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <WarningOutlined />
            </template>
          </a-statistic>
          <div class="kpi-extra">
            较昨日
            <a-tag :color="alarmChange >= 0 ? 'red' : 'green'" size="small">
              {{ alarmChange >= 0 ? '+' : '' }}{{ alarmChange }}%
            </a-tag>
          </div>
        </a-card>
      </a-col>

      <a-col :span="6">
        <a-card :bordered="false" class="kpi-card">
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
          <div class="kpi-extra">
            {{ statistics.storageUsed }}/{{ statistics.storageTotal }} TB
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 第二行：图表和列表 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <!-- 设备状态趋势 -->
      <a-col :span="12">
        <a-card title="设备状态趋势" :bordered="false">
          <template #extra>
            <a-radio-group v-model:value="trendPeriod" button-style="solid" size="small">
              <a-radio-button value="day">今日</a-radio-button>
              <a-radio-button value="week">近7天</a-radio-button>
              <a-radio-button value="month">近30天</a-radio-button>
            </a-radio-group>
          </template>
          <div ref="deviceTrendChartRef" style="height: 300px;"></div>
        </a-card>
      </a-col>

      <!-- 设备类型分布 -->
      <a-col :span="12">
        <a-card title="设备类型分布" :bordered="false">
          <div ref="deviceTypeChartRef" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 第三行：最新告警和任务队列 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <!-- 最新告警 -->
      <a-col :span="12">
        <a-card title="最新告警" :bordered="false">
          <template #extra>
            <a-button type="link" @click="goToAlarmPage">
              查看更多
              <RightOutlined />
            </a-button>
          </template>
          <a-list
            :data-source="latestAlarms"
            :loading="loading"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-badge :status="getAlarmStatus(item.level)" />
                  </template>
                  <template #title>
                    <span>{{ item.deviceName }}</span>
                    <a-tag :color="getAlarmLevelColor(item.level)" size="small" style="margin-left: 8px;">
                      {{ item.level }}
                    </a-tag>
                  </template>
                  <template #description>
                    {{ item.content }}
                  </template>
                </a-list-item-meta>
                <div>{{ item.time }}</div>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 系统状态 -->
      <a-col :span="12">
        <a-card title="系统状态" :bordered="false">
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="CPU使用率">
              <a-progress
                :percent="systemStatus.cpuUsage"
                :stroke-color="getProgressColor(systemStatus.cpuUsage)"
                size="small"
              />
            </a-descriptions-item>
            <a-descriptions-item label="内存使用率">
              <a-progress
                :percent="systemStatus.memoryUsage"
                :stroke-color="getProgressColor(systemStatus.memoryUsage)"
                size="small"
              />
            </a-descriptions-item>
            <a-descriptions-item label="网络流量">
              <span>上行: {{ systemStatus.networkUpload }} Mbps / 下行: {{ systemStatus.networkDownload }} Mbps</span>
            </a-descriptions-item>
            <a-descriptions-item label="在线用户数">
              <a-tag color="blue">{{ systemStatus.onlineUsers }} 人</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="运行时长">
              <span>{{ systemStatus.uptime }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="系统版本">
              <a-tag color="green">{{ systemStatus.version }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  VideoCameraOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  DatabaseOutlined,
  RightOutlined,
} from '@ant-design/icons-vue';
import * as echarts from 'echarts';

const router = useRouter();

// 统计数据
const statistics = reactive({
  totalDevices: 156,
  onlineDevices: 142,
  todayAlarms: 23,
  storageUsage: 68,
  storageUsed: 3.4,
  storageTotal: 5.0,
});

// 计算在线率
const onlineRate = computed(() => {
  return ((statistics.onlineDevices / statistics.totalDevices) * 100).toFixed(1);
});

// 告警变化率
const alarmChange = ref(-12);

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
    time: '2024-11-05 15:32:10',
  },
  {
    id: 2,
    deviceName: '停车场摄像头-01',
    level: '中危',
    content: '车辆违停告警',
    time: '2024-11-05 15:28:35',
  },
  {
    id: 3,
    deviceName: 'NVR存储设备-01',
    level: '低危',
    content: '存储空间不足80%',
    time: '2024-11-05 15:15:22',
  },
  {
    id: 4,
    deviceName: '走廊摄像头-003',
    level: '中危',
    content: '设备离线',
    time: '2024-11-05 14:58:10',
  },
  {
    id: 5,
    deviceName: '会议室摄像头-01',
    level: '低危',
    content: '画面模糊告警',
    time: '2024-11-05 14:45:18',
  },
]);

// 系统状态
const systemStatus = reactive({
  cpuUsage: 42,
  memoryUsage: 68,
  networkUpload: 125.6,
  networkDownload: 543.2,
  onlineUsers: 12,
  uptime: '15天 8小时 32分钟',
  version: 'v3.0.0',
});

// 图表引用
const deviceTrendChartRef = ref(null);
const deviceTypeChartRef = ref(null);
let deviceTrendChart = null;
let deviceTypeChart = null;

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

// 获取进度条颜色
const getProgressColor = (percent) => {
  if (percent >= 80) return '#ff4d4f';
  if (percent >= 60) return '#faad14';
  return '#52c41a';
};

// 跳转到告警页面
const goToAlarmPage = () => {
  router.push('/business/smart-video/history-alarm');
};

// 初始化设备趋势图表
const initDeviceTrendChart = () => {
  if (!deviceTrendChartRef.value) return;

  deviceTrendChart = echarts.init(deviceTrendChartRef.value);

  const option = {
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['在线设备', '离线设备'],
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
        name: '离线设备',
        type: 'line',
        data: [18, 14, 11, 13, 16, 14, 12],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 77, 79, 0.3)' },
            { offset: 1, color: 'rgba(255, 77, 79, 0)' },
          ]),
        },
      },
    ],
  };

  deviceTrendChart.setOption(option);
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
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: [
          { value: 95, name: '网络摄像机' },
          { value: 42, name: 'NVR' },
          { value: 12, name: 'DVR' },
          { value: 7, name: '解码器' },
        ],
      },
    ],
  };

  deviceTypeChart.setOption(option);
};

// 窗口resize处理
const handleResize = () => {
  deviceTrendChart?.resize();
  deviceTypeChart?.resize();
};

// 定时更新数据
let updateTimer = null;
const startDataUpdate = () => {
  updateTimer = setInterval(() => {
    // 模拟数据更新
    statistics.onlineDevices = 140 + Math.floor(Math.random() * 6);
    systemStatus.cpuUsage = 35 + Math.floor(Math.random() * 20);
    systemStatus.memoryUsage = 60 + Math.floor(Math.random() * 15);
  }, 5000);
};

// 生命周期
onMounted(() => {
  initDeviceTrendChart();
  initDeviceTypeChart();
  window.addEventListener('resize', handleResize);
  startDataUpdate();
});

onUnmounted(() => {
  if (deviceTrendChart) {
    deviceTrendChart.dispose();
    deviceTrendChart = null;
  }
  if (deviceTypeChart) {
    deviceTypeChart.dispose();
    deviceTypeChart = null;
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

  .kpi-row {
    margin-bottom: 0;
  }

  .kpi-card {
    :deep(.ant-card-body) {
      padding: 20px 24px;
    }

    .kpi-extra {
      margin-top: 8px;
      font-size: 12px;
      color: #666;
    }
  }

  :deep(.ant-card) {
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);

    .ant-card-head {
      border-bottom: 1px solid #f0f0f0;
    }

    .ant-card-body {
      padding: 20px;
    }
  }

  :deep(.ant-list-item) {
    padding: 12px 0;
  }

  :deep(.ant-statistic-title) {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.45);
  }

  :deep(.ant-statistic-content) {
    font-size: 24px;
    font-weight: 600;
  }
}
</style>
