<template>
  <div class="realtime-dashboard">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <div class="header-title">
        <h1>实时监控仪表盘</h1>
        <div class="header-status">
          <ConnectionStatus mode="compact" :show-reconnect-button="true" />
        </div>
      </div>
      <div class="header-actions">
        <a-space>
          <a-button @click="handleRefresh">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新数据
          </a-button>
          <a-button @click="showConfigModal = true">
            <template #icon>
              <SettingOutlined />
            </template>
            仪表盘配置
          </a-button>
          <a-dropdown>
            <template #overlay>
              <a-menu @click="handleExportMenuClick">
                <a-menu-item key="image">导出为图片</a-menu-item>
                <a-menu-item key="pdf">导出为PDF</a-menu-item>
                <a-menu-item key="data">导出数据</a-menu-item>
              </a-menu>
            </template>
            <a-button>
              <template #icon>
                <ExportOutlined />
              </template>
              导出
              <DownOutlined />
            </a-button>
          </a-dropdown>
        </a-space>
      </div>
    </div>

    <!-- 关键指标卡片 -->
    <div class="metrics-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <MetricCard
            title="在线设备"
            :value="deviceStatistics.online"
            :total="deviceStatistics.total"
            suffix="台"
            :trend="deviceTrend"
            color="#52c41a"
            icon="desktop"
          />
        </a-col>
        <a-col :span="6">
          <MetricCard
            title="活跃用户"
            :value="systemMonitorData.onlineUsers"
            suffix="人"
            :trend="userTrend"
            color="#1890ff"
            icon="user"
          />
        </a-col>
        <a-col :span="6">
          <MetricCard
            title="活跃告警"
            :value="criticalAlerts.length"
            suffix="个"
            :trend="alertTrend"
            color="#ff4d4f"
            icon="alert"
            :critical="true"
          />
        </a-col>
        <a-col :span="6">
          <MetricCard
            title="系统负载"
            :value="systemMonitorData.systemLoad"
            suffix="%"
            :trend="loadTrend"
            color="#faad14"
            icon="dashboard"
          />
        </a-col>
      </a-row>
    </div>

    <!-- 主要监控区域 -->
    <div class="monitoring-area">
      <a-row :gutter="16">
        <!-- 系统性能监控 -->
        <a-col :span="12">
          <a-card title="系统性能监控" class="monitoring-card">
            <template #extra>
              <a-space>
                <a-select
                  v-model:value="performanceTimeRange"
                  size="small"
                  style="width: 100px"
                  @change="handlePerformanceTimeRangeChange"
                >
                  <a-select-option value="1h">1小时</a-select-option>
                  <a-select-option value="6h">6小时</a-select-option>
                  <a-select-option value="24h">24小时</a-select-option>
                </a-select>
                <a-button size="small" @click="togglePerformancePause">
                  {{ performancePaused ? '继续' : '暂停' }}
                </a-button>
              </a-space>
            </template>

            <RealtimeChart
              :data-source="'system-performance'"
              :series="performanceSeries"
              :height="300"
              :max-data-points="200"
              :refresh-interval="2000"
              :smooth="true"
              chart-type="line"
              @data-updated="handlePerformanceDataUpdate"
            />
          </a-card>
        </a-col>

        <!-- 设备状态监控 -->
        <a-col :span="12">
          <a-card title="设备状态监控" class="monitoring-card">
            <template #extra>
              <a-space>
                <a-button size="small" @click="showDeviceListModal = true">
                  设备列表
                </a-button>
                <a-button size="small" @click="refreshDeviceData">
                  刷新
                </a-button>
              </a-space>
            </template>

            <DeviceStatusChart
              :device-data="Array.from(deviceData.values())"
              :height="300"
              @device-click="handleDeviceClick"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 告警和消息区域 -->
    <div class="alerts-messages-area">
      <a-row :gutter="16">
        <!-- 实时告警面板 -->
        <a-col :span="14">
          <a-card title="实时告警" class="alerts-card">
            <template #extra>
              <a-space>
                <a-badge :count="unreadAlerts" :offset="[10, 0]">
                  <a-button size="small" @click="showAlertCenter = true">
                    告警中心
                  </a-button>
                </a-badge>
                <a-button size="small" @click="showAlertPanel = !showAlertPanel">
                  {{ showAlertPanel ? '收起' : '展开' }}
                </a-button>
              </a-space>
            </template>

            <AlertPanel
              v-show="showAlertPanel"
              :refresh-interval="30000"
              :show-notifications="true"
              @alert-clicked="handleAlertClicked"
            />
          </a-card>
        </a-col>

        <!-- 实时消息 -->
        <a-col :span="10">
          <a-card title="实时消息" class="messages-card">
            <template #extra>
              <a-space>
                <a-badge :count="unreadMessages" :offset="[10, 0]">
                  <a-button size="small" @click="showMessageCenter = true">
                    消息中心
                  </a-button>
                </a-badge>
                <a-button size="small" @click="clearMessages">
                  清空
                </a-button>
              </a-space>
            </template>

            <MessageList
              :messages="recentMessages"
              :max-items="10"
              :show-avatars="true"
              @message-click="handleMessageClick"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 数据流监控 -->
    <div class="dataflow-area">
      <a-row :gutter="16">
        <!-- 消息队列状态 -->
        <a-col :span="8">
          <a-card title="消息队列状态" class="queue-card">
            <QueueStatusPanel
              :queue-status="queueStatus"
              @refresh="handleQueueRefresh"
            />
          </a-card>
        </a-col>

        <!-- 数据流量监控 -->
        <a-col :span="8">
          <a-card title="数据流量" class="traffic-card">
            <RealtimeChart
              :data-source="'network-traffic'"
              :series="trafficSeries"
              :height="200"
              chart-type="area"
              :smooth="true"
            />
          </a-card>
        </a-col>

        <!-- 事件统计 -->
        <a-col :span="8">
          <a-card title="事件统计" class="events-card">
            <EventStatisticsPanel
              :statistics="eventStatistics"
              :time-range="eventTimeRange"
              @time-range-change="handleEventTimeRangeChange"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 配置弹窗 -->
    <a-modal
      v-model:open="showConfigModal"
      title="仪表盘配置"
      :width="800"
      @ok="handleSaveConfig"
    >
      <DashboardConfig
        :config="dashboardConfig"
        @update="handleConfigUpdate"
      />
    </a-modal>

    <!-- 设备列表弹窗 -->
    <a-modal
      v-model:open="showDeviceListModal"
      title="设备列表"
      :width="1000"
      :footer="null"
    >
      <DeviceList
        :devices="Array.from(deviceData.values())"
        @device-click="handleDeviceClick"
        @device-control="handleDeviceControl"
      />
    </a-modal>

    <!-- 告警中心弹窗 -->
    <a-modal
      v-model:open="showAlertCenter"
      title="告警中心"
      :width="1200"
      :footer="null"
      fullscreen
    >
      <AlertCenter />
    </a-modal>

    <!-- 消息中心弹窗 -->
    <a-modal
      v-model:open="showMessageCenter"
      title="消息中心"
      :width="1000"
      :footer="null"
    >
      <MessageCenter />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import { useRealtimeStore } from '@/store/modules/realtime';
import { useEventBus } from '@/utils/event-bus';
import { formatDateTime } from '@/utils/format';

// Components
import ConnectionStatus from '@/components/realtime/core/ConnectionStatus.vue';
import MetricCard from '@/components/realtime/functional/MetricCard.vue';
import RealtimeChart from '@/components/realtime/functional/RealtimeChart.vue';
import DeviceStatusChart from '@/components/realtime/functional/DeviceStatusChart.vue';
import AlertPanel from '@/components/realtime/functional/AlertPanel.vue';
import MessageList from '@/components/realtime/functional/MessageList.vue';
import QueueStatusPanel from '@/components/realtime/functional/QueueStatusPanel.vue';
import EventStatisticsPanel from '@/components/realtime/functional/EventStatisticsPanel.vue';
import DashboardConfig from './modals/DashboardConfig.vue';
import DeviceList from './modals/DeviceList.vue';
import AlertCenter from './AlertCenter.vue';
import MessageCenter from './MessageCenter.vue';

// Icons
import {
  ReloadOutlined,
  SettingOutlined,
  ExportOutlined,
  DownOutlined
} from '@ant-design/icons-vue';

// Store
const realtimeStore = useRealtimeStore();
const eventBus = useEventBus('dashboard');

// 响应式数据
const showConfigModal = ref(false);
const showDeviceListModal = ref(false);
const showAlertCenter = ref(false);
const showMessageCenter = ref(false);
const showAlertPanel = ref(true);

const performanceTimeRange = ref('1h');
const performancePaused = ref(false);
const eventTimeRange = ref('1h');

// 配置
const dashboardConfig = ref({
  refreshInterval: 5000,
  autoRefresh: true,
  showNotifications: true,
  theme: 'light',
  layout: 'default'
});

// 临时数据
const deviceTrend = ref({ direction: 'up', value: 5.2 });
const userTrend = ref({ direction: 'up', value: 12.8 });
const alertTrend = ref({ direction: 'down', value: 8.5 });
const loadTrend = ref({ direction: 'stable', value: 0.3 });

const performanceSeries = ref([
  {
    name: 'CPU使用率',
    color: '#1890ff',
    unit: '%'
  },
  {
    name: '内存使用率',
    color: '#52c41a',
    unit: '%'
  },
  {
    name: '磁盘使用率',
    color: '#faad14',
    unit: '%'
  }
]);

const trafficSeries = ref([
  {
    name: '入站流量',
    color: '#52c41a',
    unit: 'KB/s'
  },
  {
    name: '出站流量',
    color: '#ff4d4f',
    unit: 'KB/s'
  }
]);

const queueStatus = ref({
  name: 'default',
  size: 245,
  consumers: 3,
  productionRate: 15.2,
  consumptionRate: 14.8,
  averageLatency: 120,
  errorRate: 0.5
});

const eventStatistics = ref({
  total: 12580,
  types: {
    'user.action': 5230,
    'system.event': 3120,
    'device.status': 2890,
    'alert.triggered': 1340
  }
});

// 计算属性
const deviceData = computed(() => realtimeStore.deviceData);
const deviceStatistics = computed(() => realtimeStore.deviceStatistics);
const systemMonitorData = computed(() => realtimeStore.systemMonitorData);
const alerts = computed(() => realtimeStore.alerts);
const unreadAlerts = computed(() => realtimeStore.unreadAlerts);
const messages = computed(() => realtimeStore.messages);
const unreadMessages = computed(() => realtimeStore.unreadMessages);

const criticalAlerts = computed(() =>
  alerts.value.filter(alert => alert.level === 'critical' && alert.status === 'active')
);

const recentMessages = computed(() =>
  messages.value.slice(-20).reverse()
);

// 方法
/**
 * 处理刷新
 */
async function handleRefresh() {
  try {
    // 刷新所有数据
    await Promise.all([
      refreshDeviceData(),
      refreshSystemData(),
      refreshAlertData(),
      refreshMessageData()
    ]);

    message.success('数据刷新成功');
  } catch (error) {
    message.error('数据刷新失败: ' + error.message);
  }
}

/**
 * 刷新设备数据
 */
async function refreshDeviceData() {
  // 这里应该调用API获取设备数据
  console.log('刷新设备数据');
}

/**
 * 刷新系统数据
 */
async function refreshSystemData() {
  // 这里应该调用API获取系统监控数据
  console.log('刷新系统数据');
}

/**
 * 刷新告警数据
 */
async function refreshAlertData() {
  // 这里应该调用API获取告警数据
  console.log('刷新告警数据');
}

/**
 * 刷新消息数据
 */
async function refreshMessageData() {
  // 这里应该调用API获取消息数据
  console.log('刷新消息数据');
}

/**
 * 处理性能时间范围变化
 */
function handlePerformanceTimeRangeChange(timeRange) {
  performanceTimeRange.value = timeRange;
  // 这里可以重新加载性能数据
}

/**
 * 切换性能监控暂停状态
 */
function togglePerformancePause() {
  performancePaused.value = !performancePaused.value;
}

/**
 * 处理性能数据更新
 */
function handlePerformanceDataUpdate(data) {
  // 更新趋势数据
  updateTrends(data);
}

/**
 * 更新趋势数据
 */
function updateTrends(data) {
  // 这里可以根据实际数据更新趋势
  // 暂时使用模拟数据
}

/**
 * 处理设备点击
 */
function handleDeviceClick(device) {
  // 显示设备详情
  console.log('设备点击:', device);
}

/**
 * 处理设备控制
 */
async function handleDeviceControl(device, command) {
  try {
    // 发送设备控制命令
    await realtimeStore.sendMessage({
      type: 'device-control',
      deviceId: device.deviceId,
      command
    });

    message.success('设备控制命令发送成功');
  } catch (error) {
    message.error('设备控制失败: ' + error.message);
  }
}

/**
 * 处理告警点击
 */
function handleAlertClicked(alert) {
  // 显示告警详情
  console.log('告警点击:', alert);
}

/**
 * 处理消息点击
 */
function handleMessageClick(message) {
  // 标记消息为已读
  if (!message.read) {
    realtimeStore.markMessageAsRead(message.id);
  }
}

/**
 * 处理队列刷新
 */
function handleQueueRefresh() {
  // 刷新队列状态
  console.log('刷新队列状态');
}

/**
 * 处理事件时间范围变化
 */
function handleEventTimeRangeChange(timeRange) {
  eventTimeRange.value = timeRange;
}

/**
 * 处理导出菜单点击
 */
function handleExportMenuClick({ key }) {
  switch (key) {
    case 'image':
      exportAsImage();
      break;
    case 'pdf':
      exportAsPDF();
      break;
    case 'data':
      exportData();
      break;
  }
}

/**
 * 导出为图片
 */
function exportAsImage() {
  // 使用html2canvas等库导出为图片
  message.info('正在导出为图片...');
}

/**
 * 导出为PDF
 */
function exportAsPDF() {
  // 使用jsPDF等库导出为PDF
  message.info('正在导出为PDF...');
}

/**
 * 导出数据
 */
function exportData() {
  // 导出仪表盘数据为JSON或CSV
  const exportData = {
    timestamp: Date.now(),
    deviceStatistics: deviceStatistics.value,
    systemMonitorData: systemMonitorData.value,
    alertStatistics: realtimeStore.alertStatistics
  };

  const blob = new Blob([JSON.stringify(exportData, null, 2)], {
    type: 'application/json'
  });

  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `dashboard-data-${formatDateTime(Date.now())}.json`;
  link.click();
  URL.revokeObjectURL(url);

  message.success('数据导出成功');
}

/**
 * 清空消息
 */
function clearMessages() {
  realtimeStore.clearMessages();
  message.success('消息已清空');
}

/**
 * 处理保存配置
 */
function handleSaveConfig() {
  showConfigModal.value = false;
  message.success('仪表盘配置已保存');

  // 应用配置
  applyConfig();
}

/**
 * 处理配置更新
 */
function handleConfigUpdate(newConfig) {
  Object.assign(dashboardConfig.value, newConfig);
}

/**
 * 应用配置
 */
function applyConfig() {
  // 根据配置调整仪表盘行为
  if (dashboardConfig.value.autoRefresh) {
    startAutoRefresh();
  } else {
    stopAutoRefresh();
  }
}

/**
 * 开始自动刷新
 */
function startAutoRefresh() {
  // 实现自动刷新逻辑
}

/**
 * 停止自动刷新
 */
function stopAutoRefresh() {
  // 停止自动刷新
}

/**
 * 设置事件监听
 */
function setupEventListeners() {
  // 监听实时数据更新
  eventBus.on('data-update', handleRealtimeDataUpdate);
  eventBus.on('alert-received', handleNewAlert);
  eventBus.on('message-received', handleNewMessage);
  eventBus.on('device-status-changed', handleDeviceStatusChange);
}

/**
 * 处理实时数据更新
 */
function handleRealtimeDataUpdate(data) {
  // 根据数据类型更新相应的图表和指标
  if (data.topic?.startsWith('system.')) {
    // 更新系统监控数据
  } else if (data.topic?.startsWith('device.')) {
    // 更新设备数据
  }
}

/**
 * 处理新告警
 */
function handleNewAlert(alert) {
  // 显示告警通知
  if (alert.level === 'critical') {
    message.error(`严重告警: ${alert.title}`, 5);
  }
}

/**
 * 处理新消息
 */
function handleNewMessage(messageData) {
  // 显示消息通知
  message.info(`新消息: ${messageData.title}`);
}

/**
 * 处理设备状态变化
 */
function handleDeviceStatusChange(device) {
  // 更新设备状态显示
  console.log('设备状态变化:', device);
}

/**
 * 初始化仪表盘
 */
async function initializeDashboard() {
  try {
    // 初始化实时数据模块
    await realtimeStore.initialize();

    // 设置事件监听
    setupEventListeners();

    // 加载初始数据
    await handleRefresh();

    console.log('仪表盘初始化完成');
  } catch (error) {
    console.error('仪表盘初始化失败:', error);
    message.error('仪表盘初始化失败: ' + error.message);
  }
}

// 生命周期
onMounted(async () => {
  await initializeDashboard();
});

onUnmounted(() => {
  // 清理资源
  stopAutoRefresh();
  eventBus.off('data-update');
  eventBus.off('alert-received');
  eventBus.off('message-received');
  eventBus.off('device-status-changed');
});

// 暴露方法
defineExpose({
  refresh: handleRefresh,
  exportData,
  getDashboardData: () => ({
    deviceStatistics: deviceStatistics.value,
    systemMonitorData: systemMonitorData.value,
    alertStatistics: realtimeStore.alertStatistics
  })
});
</script>

<style lang="less" scoped>
.realtime-dashboard {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;

  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 16px 24px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .header-title {
      display: flex;
      align-items: center;
      gap: 16px;

      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #262626;
      }
    }
  }

  .metrics-cards {
    margin-bottom: 24px;
  }

  .monitoring-area {
    margin-bottom: 24px;

    .monitoring-card {
      height: 400px;

      :deep(.ant-card-body) {
        height: calc(100% - 57px);
        padding: 16px;
      }
    }
  }

  .alerts-messages-area {
    margin-bottom: 24px;

    .alerts-card,
    .messages-card {
      height: 500px;

      :deep(.ant-card-body) {
        height: calc(100% - 57px);
        overflow-y: auto;
      }
    }
  }

  .dataflow-area {
    .queue-card,
    .traffic-card,
    .events-card {
      height: 300px;

      :deep(.ant-card-body) {
        height: calc(100% - 57px);
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .realtime-dashboard {
    .metrics-cards {
      :deep(.ant-col) {
        margin-bottom: 16px;
      }
    }

    .monitoring-area,
    .alerts-messages-area,
    .dataflow-area {
      :deep(.ant-col) {
        margin-bottom: 16px;
      }
    }
  }
}

@media (max-width: 768px) {
  .realtime-dashboard {
    padding: 16px;

    .dashboard-header {
      flex-direction: column;
      gap: 16px;

      .header-title,
      .header-actions {
        width: 100%;
        justify-content: center;
      }
    }
  }
}
</style>