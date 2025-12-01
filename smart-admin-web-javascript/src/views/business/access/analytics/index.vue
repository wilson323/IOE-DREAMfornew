<!--
 * 数据分析报表主入口页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="analytics-index-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">数据分析报表</h1>
        <p class="page-description">智能分析门禁数据，提供决策支持</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button
            type="primary"
            :loading="overviewLoading"
            @click="refreshOverview"
          >
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新概览
          </a-button>
          <a-button @click="showExportAllModal = true">
            <template #icon>
              <ExportOutlined />
            </template>
            导出全部报表
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 系统概览卡片 -->
    <div class="overview-cards">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="6" v-for="(card, index) in overviewCards" :key="index">
          <a-card
            class="overview-card"
            :hoverable="true"
            @click="navigateToAnalytics(card.type)"
          >
            <div class="card-icon">
              <component :is="card.icon" :style="{ color: card.color }" />
            </div>
            <div class="card-content">
              <h3 class="card-title">{{ card.title }}</h3>
              <p class="card-description">{{ card.description }}</p>
              <div class="card-stats">
                <span class="stat-item">
                  <span class="stat-value">{{ card.totalCount }}</span>
                  <span class="stat-label">总数据量</span>
                </span>
                <span class="stat-item" v-if="card.trend">
                  <span class="stat-value" :class="getTrendClass(card.trend)">
                    {{ getTrendIcon(card.trend) }}{{ Math.abs(card.trend) }}%
                  </span>
                  <span class="stat-label">变化率</span>
                </span>
              </div>
            </div>
            <div class="card-arrow">
              <ArrowRightOutlined />
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 综合仪表盘 -->
    <a-card title="综合仪表盘" class="dashboard-card">
      <template #extra>
        <a-space>
          <a-switch
            v-model:checked="realTimeEnabled"
            checked-children="实时更新"
            un-checked-children="手动更新"
            @change="handleRealTimeToggle"
          />
          <a-button type="link" size="small" @click="refreshDashboard">
            <ReloadOutlined />
          </a-button>
        </a-space>
      </template>

      <!-- KPI指标行 -->
      <div class="kpi-row">
        <a-row :gutter="16">
          <a-col :xs="24" :sm="12" :md="6" v-for="(kpi, index) in dashboardKPIs" :key="index">
            <div class="kpi-item">
              <div class="kpi-icon">
                <component :is="kpi.icon" :style="{ color: kpi.color }" />
              </div>
              <div class="kpi-content">
                <div class="kpi-title">{{ kpi.title }}</div>
                <div class="kpi-value" :style="{ color: kpi.color }">
                  {{ formatKPIValue(kpi.value, kpi.format) }}
                </div>
                <div class="kpi-trend" v-if="kpi.trend !== undefined">
                  <span :class="getTrendClass(kpi.trend)">
                    {{ getTrendIcon(kpi.trend) }} {{ Math.abs(kpi.trend) }}%
                  </span>
                  <span class="trend-text">较昨日</span>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 主要图表区域 -->
      <div class="main-charts">
        <a-row :gutter="16">
          <!-- 通行流量趋势 -->
          <a-col :xs="24" :lg="16">
            <div class="chart-section">
              <h4>通行流量趋势</h4>
              <LineChart
                :data="trafficTrendData"
                :x-axis="trafficTrendXAxis"
                height="300px"
                :loading="trafficLoading"
                :show-area="true"
                color-scheme="business"
              />
            </div>
          </a-col>

          <!-- 设备状态分布 -->
          <a-col :xs="24" :lg="8">
            <div class="chart-section">
              <h4>设备状态分布</h4>
              <PieChart
                :data="deviceStatusData"
                height="300px"
                :loading="deviceLoading"
                :donut="true"
              />
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 次要图表区域 -->
      <div class="secondary-charts">
        <a-row :gutter="16">
          <!-- 权限使用趋势 -->
          <a-col :xs="24" :md="8">
            <div class="chart-section">
              <h4>权限使用趋势</h4>
              <BarChart
                :data="permissionTrendData"
                :x-axis="permissionTrendXAxis"
                height="250px"
                :loading="permissionLoading"
                color-scheme="traffic"
              />
            </div>
          </a-col>

          <!-- 安防事件统计 -->
          <a-col :xs="24" :md="8">
            <div class="chart-section">
              <h4>安防事件统计</h4>
              <BarChart
                :data="securityEventData"
                :x-axis="securityEventXAxis"
                height="250px"
                :loading="securityLoading"
                color-scheme="security"
              />
            </div>
          </a-col>

          <!-- 系统健康度 -->
          <a-col :xs="24" :md="8">
            <div class="chart-section">
              <h4>系统健康度</h4>
              <GaugeChart
                :data="systemHealthData"
                height="250px"
                :loading="healthLoading"
                :min="0"
                :max="100"
                unit="%"
              />
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 实时动态 -->
      <div class="real-time-section" v-if="realTimeUpdates.length > 0">
        <h4>实时动态</h4>
        <a-timeline>
          <a-timeline-item
            v-for="(update, index) in realTimeUpdates.slice(0, 5)"
            :key="index"
            :color="getUpdateColor(update.severity)"
          >
            <div class="update-item">
              <div class="update-time">{{ formatTime(update.timestamp) }}</div>
              <div class="update-content">{{ update.content }}</div>
              <div class="update-type" :class="`update-type-${update.type.toLowerCase()}`">
                {{ update.type }}
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </div>
    </a-card>

    <!-- 快速操作区域 -->
    <div class="quick-actions">
      <a-card title="快速操作" class="actions-card">
        <a-row :gutter="16">
          <a-col :xs="24" :sm="12" :md="6" v-for="(action, index) in quickActions" :key="index">
            <a-button
              type="dashed"
              size="large"
              class="action-button"
              @click="handleQuickAction(action)"
            >
              <template #icon>
                <component :is="action.icon" />
              </template>
              {{ action.title }}
            </a-button>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 导出全部报表模态框 -->
    <ExportModal
      v-model:visible="showExportAllModal"
      export-type="DASHBOARD"
      @confirm="handleExportAll"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
  ReloadOutlined,
  ExportOutlined,
  ArrowRightOutlined,
  BarChartOutlined,
  UserOutlined,
  SafetyOutlined,
  KeyOutlined,
  DashboardOutlined,
  FileTextOutlined,
  SettingOutlined,
  NotificationOutlined,
  TeamOutlined,
  DatabaseOutlined
} from '@ant-design/icons-vue';
import { useAccessAnalyticsStore } from '/@/store/modules/business/access-analytics';
import {
  LineChart,
  BarChart,
  PieChart,
  GaugeChart
} from '/@/components/charts';
import ExportModal from './components/ExportModal.vue';
import dayjs from 'dayjs';

const router = useRouter();
const store = useAccessAnalyticsStore();

// 响应式数据
const overviewLoading = ref(false);
const trafficLoading = ref(false);
const deviceLoading = ref(false);
const permissionLoading = ref(false);
const securityLoading = ref(false);
const healthLoading = ref(false);
const realTimeEnabled = ref(false);
const showExportAllModal = ref(false);

// 概览卡片数据
const overviewCards = ref([
  {
    type: 'TRAFFIC',
    title: '通行数据分析',
    description: '分析园区人流动态和通行模式',
    icon: UserOutlined,
    color: '#1890ff',
    totalCount: '15.2万',
    trend: 12.5
  },
  {
    type: 'DEVICE',
    title: '设备运行分析',
    description: '监控设备状态和性能指标',
    icon: DatabaseOutlined,
    color: '#52c41a',
    totalCount: '1,245台',
    trend: 8.2
  },
  {
    type: 'PERMISSION',
    title: '权限使用分析',
    description: '分析权限分配和使用效率',
    icon: KeyOutlined,
    color: '#faad14',
    totalCount: '5,680项',
    trend: -2.1
  },
  {
    type: 'SECURITY',
    title: '安防态势分析',
    description: '评估园区安全状况和风险',
    icon: SafetyOutlined,
    color: '#f5222d',
    totalCount: '128次',
    trend: -15.3
  }
]);

// 仪表盘KPI数据
const dashboardKPIs = ref([
  {
    title: '今日通行量',
    value: 5680,
    format: 'number',
    icon: UserOutlined,
    color: '#1890ff',
    trend: 12.5
  },
  {
    title: '设备在线率',
    value: 98.7,
    format: 'percent',
    icon: DatabaseOutlined,
    color: '#52c41a',
    trend: 1.2
  },
  {
    title: '权限使用率',
    value: 85.3,
    format: 'percent',
    icon: KeyOutlined,
    color: '#faad14',
    trend: 3.8
  },
  {
    title: '安全指数',
    value: 92.1,
    format: 'score',
    icon: SafetyOutlined,
    color: '#f5222d',
    trend: -0.5
  }
]);

// 图表数据
const trafficTrendData = computed(() => [
  {
    name: '进入人数',
    data: [320, 302, 301, 334, 390, 330, 320]
  },
  {
    name: '离开人数',
    data: [280, 288, 290, 320, 350, 310, 300]
  }
]);

const trafficTrendXAxis = computed(() => {
  const days = [];
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('MM-DD'));
  }
  return days;
});

const deviceStatusData = computed(() => [
  { name: '在线', value: 1228, color: '#52c41a' },
  { name: '离线', value: 15, color: '#f5222d' },
  { name: '维护中', value: 2, color: '#faad14' }
]);

const permissionTrendData = computed(() => [
  {
    name: '申请数量',
    data: [45, 52, 38, 65, 42, 78, 55]
  }
]);

const permissionTrendXAxis = computed(() => {
  const days = [];
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('MM-DD'));
  }
  return days;
});

const securityEventData = computed(() => [
  {
    name: '事件数量',
    data: [8, 12, 6, 15, 9, 11, 7]
  }
]);

const securityEventXAxis = computed(() => ['周一', '周二', '周三', '周四', '周五', '周六', '周日']);

const systemHealthData = computed(() => [
  {
    name: '系统健康度',
    value: 92.1
  }
]);

const realTimeUpdates = computed(() => store.state.dashboardData.realTimeUpdates || []);

// 快速操作
const quickActions = ref([
  {
    title: '生成周报',
    icon: FileTextOutlined,
    action: 'generate-weekly-report'
  },
  {
    title: '系统设置',
    icon: SettingOutlined,
    action: 'system-settings'
  },
  {
    title: '告警管理',
    icon: NotificationOutlined,
    action: 'alarm-management'
  },
  {
    title: '用户管理',
    icon: TeamOutlined,
    action: 'user-management'
  }
]);

// 方法
const navigateToAnalytics = (type) => {
  const routes = {
    TRAFFIC: '/business/access/analytics/traffic',
    DEVICE: '/business/access/analytics/device',
    PERMISSION: '/business/access/analytics/permission',
    SECURITY: '/business/access/analytics/security'
  };

  const route = routes[type];
  if (route) {
    router.push(route);
  }
};

const refreshOverview = async () => {
  overviewLoading.value = true;
  try {
    // 模拟刷新概览数据
    await new Promise(resolve => setTimeout(resolve, 1000));
    message.success('概览数据已刷新');
  } catch (error) {
    console.error('刷新概览数据失败:', error);
    message.error('刷新概览数据失败');
  } finally {
    overviewLoading.value = false;
  }
};

const refreshDashboard = async () => {
  await Promise.all([
    loadTrafficData(),
    loadDeviceData(),
    loadPermissionData(),
    loadSecurityData(),
    loadHealthData()
  ]);
  message.success('仪表盘数据已刷新');
};

const loadTrafficData = async () => {
  trafficLoading.value = true;
  try {
    // 加载通行数据
  } catch (error) {
    console.error('加载通行数据失败:', error);
  } finally {
    trafficLoading.value = false;
  }
};

const loadDeviceData = async () => {
  deviceLoading.value = true;
  try {
    // 加载设备数据
  } catch (error) {
    console.error('加载设备数据失败:', error);
  } finally {
    deviceLoading.value = false;
  }
};

const loadPermissionData = async () => {
  permissionLoading.value = true;
  try {
    // 加载权限数据
  } catch (error) {
    console.error('加载权限数据失败:', error);
  } finally {
    permissionLoading.value = false;
  }
};

const loadSecurityData = async () => {
  securityLoading.value = true;
  try {
    // 加载安防数据
  } catch (error) {
    console.error('加载安防数据失败:', error);
  } finally {
    securityLoading.value = false;
  }
};

const loadHealthData = async () => {
  healthLoading.value = true;
  try {
    // 加载健康度数据
  } catch (error) {
    console.error('加载健康度数据失败:', error);
  } finally {
    healthLoading.value = false;
  }
};

const handleRealTimeToggle = (enabled) => {
  if (enabled) {
    store.startRealTimeUpdates();
    message.info('实时数据更新已启动');
  } else {
    store.stopRealTimeUpdates();
    message.info('实时数据更新已停止');
  }
};

const handleQuickAction = (action) => {
  switch (action.action) {
    case 'generate-weekly-report':
      showExportAllModal.value = true;
      break;
    case 'system-settings':
      message.info('系统设置功能开发中');
      break;
    case 'alarm-management':
      message.info('告警管理功能开发中');
      break;
    case 'user-management':
      router.push('/system/employee');
      break;
    default:
      message.info('功能开发中');
  }
};

const handleExportAll = async (exportParams) => {
  try {
    await store.generateCustomReport({
      ...exportParams,
      includeAllTypes: true
    });
    showExportAllModal.value = false;
    message.success('综合报表导出任务已创建');
  } catch (error) {
    console.error('导出失败:', error);
  }
};

// 工具方法
const formatKPIValue = (value, format) => {
  switch (format) {
    case 'percent':
      return `${value}%`;
    case 'score':
      return value.toFixed(1);
    case 'number':
    default:
      return value.toLocaleString();
  }
};

const getTrendIcon = (trend) => {
  if (trend > 0) return '↑';
  if (trend < 0) return '↓';
  return '→';
};

const getTrendClass = (trend) => {
  if (trend > 0) return 'trend-up';
  if (trend < 0) return 'trend-down';
  return 'trend-stable';
};

const getUpdateColor = (severity) => {
  switch (severity) {
    case 'CRITICAL':
      return 'red';
    case 'HIGH':
      return 'orange';
    case 'MEDIUM':
      return 'yellow';
    default:
      return 'blue';
  }
};

const formatTime = (timestamp) => {
  return dayjs(timestamp).format('HH:mm:ss');
};

// 生命周期
onMounted(async () => {
  await refreshDashboard();
});

onBeforeUnmount(() => {
  // 清理实时更新
  if (realTimeEnabled.value) {
    store.stopRealTimeUpdates();
  }
});
</script>

<style scoped>
.analytics-index-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left .page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.header-left .page-description {
  margin: 8px 0 0 0;
  color: #8c8c8c;
  font-size: 14px;
}

.overview-cards {
  margin-bottom: 24px;
}

.overview-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.overview-card :deep(.ant-card-body) {
  display: flex;
  align-items: center;
  padding: 20px;
}

.card-icon {
  font-size: 32px;
  margin-right: 16px;
}

.card-content {
  flex: 1;
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.card-description {
  margin: 0 0 12px 0;
  color: #8c8c8c;
  font-size: 14px;
}

.card-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.stat-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 2px;
}

.card-arrow {
  font-size: 16px;
  color: #8c8c8c;
  transition: all 0.3s;
}

.overview-card:hover .card-arrow {
  color: #1890ff;
  transform: translateX(4px);
}

.dashboard-card,
.actions-card {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.kpi-row {
  margin-bottom: 32px;
}

.kpi-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.kpi-icon {
  font-size: 24px;
  margin-right: 12px;
}

.kpi-content {
  flex: 1;
}

.kpi-title {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.kpi-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.kpi-trend {
  font-size: 12px;
  color: #8c8c8c;
}

.trend-up {
  color: #52c41a;
}

.trend-down {
  color: #f5222d;
}

.trend-stable {
  color: #8c8c8c;
}

.trend-text {
  margin-left: 4px;
}

.main-charts {
  margin-bottom: 32px;
}

.secondary-charts {
  margin-bottom: 32px;
}

.chart-section {
  h4 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #262626;
  }
}

.real-time-section {
  h4 {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #262626;
  }
}

.update-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.update-time {
  font-size: 12px;
  color: #8c8c8c;
  min-width: 80px;
}

.update-content {
  flex: 1;
  font-size: 14px;
  color: #262626;
}

.update-type {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.update-type-device_status {
  background-color: #e6f7ff;
  color: #1890ff;
}

.update-type-access_data {
  background-color: #f6ffed;
  color: #52c41a;
}

.update-type-security_event {
  background-color: #fff2e8;
  color: #fa8c16;
}

.update-type-system_alert {
  background-color: #fff1f0;
  color: #f5222d;
}

.action-button {
  width: 100%;
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
  border-radius: 8px;
}

.action-button :deep(.anticon) {
  font-size: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .analytics-index-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-right {
    width: 100%;
  }

  .overview-card :deep(.ant-card-body) {
    flex-direction: column;
    text-align: center;
  }

  .card-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .kpi-item {
    flex-direction: column;
    text-align: center;
  }

  .kpi-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }

  .update-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>