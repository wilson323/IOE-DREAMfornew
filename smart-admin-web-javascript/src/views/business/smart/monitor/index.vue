<template>
  <div class="realtime-index">
    <!-- 页面标题和导航 -->
    <div class="page-header">
      <div class="header-content">
        <h1>实时数据管理中心</h1>
        <p class="header-description">
          IOE-DREAM智慧园区一卡通管理平台实时监控系统
        </p>
      </div>
      <div class="header-navigation">
        <a-menu
          v-model:selectedKeys="selectedMenuKeys"
          mode="horizontal"
          @click="handleMenuClick"
        >
          <a-menu-item key="dashboard">
            <template #icon>
              <DashboardOutlined />
            </template>
            监控仪表盘
          </a-menu-item>
          <a-menu-item key="alerts">
            <template #icon>
              <AlertOutlined />
            </template>
            告警中心
          </a-menu-item>
          <a-menu-item key="messages">
            <template #icon>
              <MessageOutlined />
            </template>
            消息中心
          </a-menu-item>
          <a-menu-item key="monitoring">
            <template #icon>
              <MonitorOutlined />
            </template>
            系统监控
          </a-menu-item>
          <a-menu-item key="devices">
            <template #icon>
              <DesktopOutlined />
            </template>
            设备管理
          </a-menu-item>
          <a-menu-item key="settings">
            <template #icon>
              <SettingOutlined />
            </template>
            系统设置
          </a-menu-item>
        </a-menu>
      </div>
    </div>

    <!-- 全局连接状态栏 -->
    <div class="global-status-bar">
      <a-row :gutter="16" align="middle">
        <a-col :span="6">
          <div class="status-item">
            <span class="status-label">连接状态:</span>
            <ConnectionStatus mode="compact" />
          </div>
        </a-col>
        <a-col :span="6">
          <div class="status-item">
            <span class="status-label">在线设备:</span>
            <a-statistic
              :value="deviceStatistics.online"
              :value-style="{ fontSize: '16px' }"
              suffix="/"
            >
              <template #suffix>
                <span>{{ deviceStatistics.total }}</span>
              </template>
            </a-statistic>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="status-item">
            <span class="status-label">活跃告警:</span>
            <a-badge
              :count="criticalAlerts.length"
              :number-style="{ backgroundColor: '#ff4d4f' }"
            >
              <a-button type="text" size="small">
                告警中心
              </a-button>
            </a-badge>
          </div>
        </a-col>
        <a-col :span="6">
          <div class="status-item">
            <span class="status-label">系统负载:</span>
            <a-progress
              :percent="systemMonitorData.systemLoad"
              :size="['small', 'small']"
              :stroke-color="getLoadColor(systemMonitorData.systemLoad)"
            />
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 监控仪表盘 -->
      <div v-show="currentView === 'dashboard'" class="view-content">
        <Dashboard />
      </div>

      <!-- 告警中心 -->
      <div v-show="currentView === 'alerts'" class="view-content">
        <AlertCenter />
      </div>

      <!-- 消息中心 -->
      <div v-show="currentView === 'messages'" class="view-content">
        <MessageCenter />
      </div>

      <!-- 系统监控 -->
      <div v-show="currentView === 'monitoring'" class="view-content">
        <SystemMonitoring />
      </div>

      <!-- 设备管理 -->
      <div v-show="currentView === 'devices'" class="view-content">
        <DeviceManagement />
      </div>

      <!-- 系统设置 -->
      <div v-show="currentView === 'settings'" class="view-content">
        <RealtimeSettings />
      </div>
    </div>

    <!-- 实时通知抽屉 -->
    <a-drawer
      v-model:open="showNotificationDrawer"
      title="实时通知"
      placement="right"
      :width="400"
    >
      <NotificationPanel
        :notifications="notifications"
        @clear="handleClearNotifications"
        @mark-read="handleMarkNotificationRead"
      />
    </a-drawer>

    <!-- 全局WebSocket客户端控制台 -->
    <a-drawer
      v-model:open="showConsoleDrawer"
      title="WebSocket控制台"
      placement="right"
      :width="600"
    >
      <WebSocketConsole />
    </a-drawer>

    <!-- 快速操作浮动按钮组 -->
    <div class="floating-actions">
      <a-space direction="vertical">
        <a-button
          type="primary"
          shape="circle"
          size="large"
          :title="`${showNotificationDrawer ? '关闭' : '打开'}通知面板`"
          @click="toggleNotificationDrawer"
        >
          <template #icon>
            <BellOutlined />
          </template>
          <a-badge
            v-if="unreadNotifications > 0"
            :count="unreadNotifications"
            :offset="[-5, 5]"
          />
        </a-button>

        <a-button
          type="default"
          shape="circle"
          size="large"
          :title="`${showConsoleDrawer ? '关闭' : '打开'}控制台`"
          @click="toggleConsoleDrawer"
        >
          <template #icon>
            <CodeOutlined />
          </template>
        </a-button>

        <a-button
          type="default"
          shape="circle"
          size="large"
          title="快速刷新"
          :loading="refreshing"
          @click="handleQuickRefresh"
        >
          <template #icon>
            <ReloadOutlined />
          </template>
        </a-button>

        <a-button
          type="default"
          shape="circle"
          size="large"
          title="全屏模式"
          @click="toggleFullscreen"
        >
          <template #icon>
            <FullscreenOutlined />
          </template>
        </a-button>
      </a-space>
    </div>

    <!-- 系统状态指示器 -->
    <div class="system-indicator">
      <a-tooltip :title="systemStatusTooltip">
        <div
          :class="['indicator-dot', systemStatusClass]"
          @click="showSystemStatusModal = true"
        />
      </a-tooltip>
    </div>

    <!-- 系统状态弹窗 -->
    <a-modal
      v-model:open="showSystemStatusModal"
      title="系统状态详情"
      :width="800"
      :footer="null"
    >
      <SystemStatusModal :status="systemStatus" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRealtimeStore } from '@/store/modules/realtime';
import { useEventBus } from '@/utils/event-bus';

// Components
import ConnectionStatus from '@/components/realtime/core/ConnectionStatus.vue';
import Dashboard from './dashboard.vue';
import AlertCenter from './AlertCenter.vue';
import MessageCenter from './MessageCenter.vue';
import SystemMonitoring from './SystemMonitoring.vue';
import DeviceManagement from './DeviceManagement.vue';
import RealtimeSettings from './RealtimeSettings.vue';
import NotificationPanel from '@/components/realtime/functional/NotificationPanel.vue';
import WebSocketConsole from '@/components/realtime/functional/WebSocketConsole.vue';
import SystemStatusModal from './modals/SystemStatusModal.vue';

// Icons
import {
  DashboardOutlined,
  AlertOutlined,
  MessageOutlined,
  MonitorOutlined,
  DesktopOutlined,
  SettingOutlined,
  BellOutlined,
  CodeOutlined,
  ReloadOutlined,
  FullscreenOutlined
} from '@ant-design/icons-vue';

// Store
const realtimeStore = useRealtimeStore();
const eventBus = useEventBus('realtime-index');

// 响应式数据
const selectedMenuKeys = ref(['dashboard']);
const currentView = ref('dashboard');
const showNotificationDrawer = ref(false);
const showConsoleDrawer = ref(false);
const showSystemStatusModal = ref(false);
const refreshing = ref(false);

const notifications = ref([]);
const systemStatus = ref({
  overall: 'healthy',
  services: {
    websocket: 'connected',
    database: 'connected',
    redis: 'connected',
    messageQueue: 'connected'
  },
  performance: {
    cpu: 45,
    memory: 62,
    disk: 38,
    network: 'normal'
  },
  uptime: 0,
  lastUpdate: Date.now()
});

// 计算属性
const deviceStatistics = computed(() => realtimeStore.deviceStatistics);
const systemMonitorData = computed(() => realtimeStore.systemMonitorData);
const criticalAlerts = computed(() =>
  realtimeStore.alerts.filter(alert => alert.level === 'critical' && alert.status === 'active')
);

const unreadNotifications = computed(() =>
  notifications.value.filter(n => !n.read).length
);

const systemStatusClass = computed(() => {
  const status = systemStatus.value.overall;
  switch (status) {
    case 'healthy':
      return 'status-healthy';
    case 'warning':
      return 'status-warning';
    case 'error':
      return 'status-error';
    default:
      return 'status-unknown';
  }
});

const systemStatusTooltip = computed(() => {
  const status = systemStatus.value.overall;
  const statusMap = {
    healthy: '系统运行正常',
    warning: '系统存在警告',
    error: '系统存在错误',
    unknown: '系统状态未知'
  };
  return statusMap[status] || status;
});

// 方法
/**
 * 获取负载颜色
 */
function getLoadColor(load) {
  if (load >= 80) return '#ff4d4f';
  if (load >= 60) return '#faad14';
  return '#52c41a';
}

/**
 * 处理菜单点击
 */
function handleMenuClick({ key }) {
  currentView.value = key;
  selectedMenuKeys.value = [key];

  // 添加路由历史记录（如果使用Vue Router）
  // router.push({ name: `realtime-${key}` });
}

/**
 * 切换通知抽屉
 */
function toggleNotificationDrawer() {
  showNotificationDrawer.value = !showNotificationDrawer.value;
}

/**
 * 切换控制台抽屉
 */
function toggleConsoleDrawer() {
  showConsoleDrawer.value = !showConsoleDrawer.value;
}

/**
 * 处理快速刷新
 */
async function handleQuickRefresh() {
  if (refreshing.value) return;

  refreshing.value = true;
  try {
    await realtimeStore.refreshAllData();
    updateSystemStatus();
    message.success('数据刷新成功');
  } catch (error) {
    message.error('数据刷新失败: ' + error.message);
  } finally {
    refreshing.value = false;
  }
}

/**
 * 切换全屏模式
 */
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen();
  } else {
    document.exitFullscreen();
  }
}

/**
 * 处理清空通知
 */
function handleClearNotifications() {
  notifications.value = [];
  message.success('通知已清空');
}

/**
 * 处理标记通知为已读
 */
function handleMarkNotificationRead(notificationId) {
  const notification = notifications.value.find(n => n.id === notificationId);
  if (notification) {
    notification.read = true;
  }
}

/**
 * 添加通知
 */
function addNotification(notification) {
  notifications.value.unshift({
    id: Date.now().toString(),
    timestamp: Date.now(),
    read: false,
    ...notification
  });

  // 限制通知数量
  if (notifications.value.length > 100) {
    notifications.value = notifications.value.slice(0, 100);
  }

  // 自动标记为已读（5秒后）
  setTimeout(() => {
    handleMarkNotificationRead(notification.id);
  }, 5000);
}

/**
 * 更新系统状态
 */
function updateSystemStatus() {
  const status = systemStatus.value;

  // 更新运行时间
  status.uptime = Date.now() - (realtimeStore.statistics.uptime || 0);
  status.lastUpdate = Date.now();

  // 根据各种指标判断整体状态
  const alerts = realtimeStore.alerts;
  const criticalAlerts = alerts.filter(a => a.level === 'critical' && a.status === 'active');
  const errorAlerts = alerts.filter(a => a.level === 'error' && a.status === 'active');

  if (criticalAlerts.length > 0) {
    status.overall = 'error';
  } else if (errorAlerts.length > 0 || systemMonitorData.value.systemLoad > 80) {
    status.overall = 'warning';
  } else {
    status.overall = 'healthy';
  }

  // 更新服务状态
  status.services.websocket = realtimeStore.isConnected ? 'connected' : 'disconnected';

  // 更新性能指标
  status.performance.cpu = systemMonitorData.value.cpuUsage;
  status.performance.memory = systemMonitorData.value.memoryUsage;
  status.performance.disk = systemMonitorData.value.diskUsage;
}

/**
 * 设置事件监听
 */
function setupEventListeners() {
  // 监听新告警
  eventBus.on('alert-received', (alert) => {
    if (alert.level === 'critical' || alert.level === 'error') {
      addNotification({
        type: 'alert',
        title: alert.level === 'critical' ? '严重告警' : '错误告警',
        content: alert.title,
        level: alert.level,
        data: alert
      });
    }
    updateSystemStatus();
  });

  // 监听连接状态变化
  eventBus.on('connection-changed', () => {
    updateSystemStatus();
  });

  // 监听系统指标更新
  eventBus.on('system-metrics-updated', () => {
    updateSystemStatus();
  });

  // 监听设备状态变化
  eventBus.on('device-status-changed', (device) => {
    if (device.status === 'error') {
      addNotification({
        type: 'device',
        title: '设备异常',
        content: `设备 ${device.deviceName} 状态异常`,
        level: 'warning',
        data: device
      });
    }
  });

  // 监听WebSocket消息
  eventBus.on('websocket-message', (message) => {
    addNotification({
      type: 'websocket',
      title: 'WebSocket消息',
      content: `收到消息: ${message.topic}`,
      level: 'info',
      data: message
    });
  });
}

/**
 * 初始化实时数据模块
 */
async function initializeRealtimeModule() {
  try {
    await realtimeStore.initialize();
    addNotification({
      type: 'system',
      title: '系统初始化',
      content: '实时数据模块初始化成功',
      level: 'success'
    });
  } catch (error) {
    console.error('实时数据模块初始化失败:', error);
    addNotification({
      type: 'system',
      title: '初始化失败',
      content: '实时数据模块初始化失败: ' + error.message,
      level: 'error'
    });
  }
}

/**
 * 启动定时更新
 */
function startPeriodicUpdate() {
  // 每30秒更新系统状态
  const statusTimer = setInterval(() => {
    updateSystemStatus();
  }, 30000);

  onUnmounted(() => {
    clearInterval(statusTimer);
  });
}

// 监听器
watch(currentView, (newView) => {
  // 视图切换时的处理逻辑
  console.log('切换到视图:', newView);
});

// 生命周期
onMounted(async () => {
  // 设置事件监听
  setupEventListeners();

  // 初始化实时数据模块
  await initializeRealtimeModule();

  // 更新系统状态
  updateSystemStatus();

  // 启动定时更新
  startPeriodicUpdate();

  // 添加键盘快捷键
  setupKeyboardShortcuts();
});

onUnmounted(() => {
  // 清理事件监听器
  eventBus.off('alert-received');
  eventBus.off('connection-changed');
  eventBus.off('system-metrics-updated');
  eventBus.off('device-status-changed');
  eventBus.off('websocket-message');
});

/**
 * 设置键盘快捷键
 */
function setupKeyboardShortcuts() {
  const handleKeyDown = (event) => {
    // Ctrl/Cmd + R: 快速刷新
    if ((event.ctrlKey || event.metaKey) && event.key === 'r') {
      event.preventDefault();
      handleQuickRefresh();
    }

    // Ctrl/Cmd + N: 打开通知面板
    if ((event.ctrlKey || event.metaKey) && event.key === 'n') {
      event.preventDefault();
      toggleNotificationDrawer();
    }

    // F11: 全屏切换
    if (event.key === 'F11') {
      event.preventDefault();
      toggleFullscreen();
    }

    // ESC: 关闭抽屉
    if (event.key === 'Escape') {
      showNotificationDrawer.value = false;
      showConsoleDrawer.value = false;
    }
  };

  document.addEventListener('keydown', handleKeyDown);

  onUnmounted(() => {
    document.removeEventListener('keydown', handleKeyDown);
  });
}

// 暴露方法
defineExpose({
  refresh: handleQuickRefresh,
  getCurrentView: () => currentView.value,
  addNotification,
  getSystemStatus: () => systemStatus.value
});
</script>

<style lang="less" scoped>
.realtime-index {
  min-height: 100vh;
  background: #f0f2f5;

  .page-header {
    background: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    position: sticky;
    top: 0;
    z-index: 100;

    .header-content {
      padding: 24px;
      border-bottom: 1px solid #f0f0f0;

      h1 {
        margin: 0 0 8px 0;
        font-size: 28px;
        font-weight: 600;
        color: #262626;
      }

      .header-description {
        margin: 0;
        color: #8c8c8c;
        font-size: 14px;
      }
    }

    .header-navigation {
      padding: 0 24px;
    }
  }

  .global-status-bar {
    background: white;
    padding: 12px 24px;
    border-bottom: 1px solid #f0f0f0;

    .status-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .status-label {
        font-size: 14px;
        color: #666;
        min-width: 80px;
      }
    }
  }

  .main-content {
    padding: 24px;

    .view-content {
      min-height: calc(100vh - 280px);
    }
  }

  .floating-actions {
    position: fixed;
    bottom: 30px;
    right: 30px;
    z-index: 1000;
  }

  .system-indicator {
    position: fixed;
    bottom: 30px;
    left: 30px;
    z-index: 1000;

    .indicator-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      cursor: pointer;
      transition: all 0.3s;

      &.status-healthy {
        background: #52c41a;
        box-shadow: 0 0 10px rgba(82, 196, 26, 0.5);
      }

      &.status-warning {
        background: #faad14;
        box-shadow: 0 0 10px rgba(250, 173, 20, 0.5);
      }

      &.status-error {
        background: #ff4d4f;
        box-shadow: 0 0 10px rgba(255, 77, 79, 0.5);
        animation: pulse 2s infinite;
      }

      &.status-unknown {
        background: #d9d9d9;
      }

      &:hover {
        transform: scale(1.2);
      }
    }
  }
}

// 脉冲动画
@keyframes pulse {
  0% {
    box-shadow: 0 0 10px rgba(255, 77, 79, 0.5);
  }
  50% {
    box-shadow: 0 0 20px rgba(255, 77, 79, 0.8);
  }
  100% {
    box-shadow: 0 0 10px rgba(255, 77, 79, 0.5);
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .realtime-index {
    .global-status-bar {
      :deep(.ant-col) {
        margin-bottom: 8px;
      }
    }
  }
}

@media (max-width: 768px) {
  .realtime-index {
    .page-header {
      .header-content {
        padding: 16px;

        h1 {
          font-size: 24px;
        }
      }

      .header-navigation {
        padding: 0 16px;

        :deep(.ant-menu-horizontal) {
          border-bottom: none;
        }
      }
    }

    .global-status-bar {
      padding: 8px 16px;

      .status-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
        margin-bottom: 8px;
      }
    }

    .main-content {
      padding: 16px;
    }

    .floating-actions {
      bottom: 20px;
      right: 20px;
    }

    .system-indicator {
      bottom: 20px;
      left: 20px;
    }
  }
}
</style>