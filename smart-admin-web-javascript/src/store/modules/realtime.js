import { defineStore } from 'pinia';
import { ref, computed, reactive, watch } from 'vue';
import { useWebSocket } from '@/utils/websocket';
import { useEventBus } from '@/utils/event-bus';
import { createEnhancedEventEmitter } from '@/utils/event-bus';
import * as realtimeApi from '@/api/realtime';

export const useRealtimeStore = defineStore('realtime', () => {
  // ==================== 状态定义 ====================

  // WebSocket相关状态
  const wsClient = ref(null);
  const connectionStatus = ref({
    status: 'disconnected',
    reconnectCount: 0,
    lastActiveAt: null,
    connectedAt: null,
    error: null
  });

  // 配置状态
  const config = reactive({
    websocket: {
      url: '',
      protocol: 'ws',
      timeout: 10000,
      heartbeatInterval: 30000,
      maxReconnectAttempts: 5,
      reconnectInterval: 3000,
      autoReconnect: true,
      debug: false
    },
    notification: {
      enabled: true,
      sound: true,
      desktop: false,
      alertLevels: ['error', 'critical']
    },
    display: {
      maxDataPoints: 100,
      refreshInterval: 5000,
      animationEnabled: true,
      theme: 'light'
    }
  });

  // 订阅管理
  const subscriptions = ref(new Map());
  const activeSubscriptions = ref([]);

  // 消息相关
  const messages = ref([]);
  const unreadMessages = ref(0);
  const messageCache = ref(new Map());

  // 告警相关
  const alerts = ref([]);
  const unreadAlerts = ref(0);
  const activeAlerts = ref([]);
  const alertStatistics = ref({
    total: 0,
    critical: 0,
    error: 0,
    warning: 0,
    info: 0
  });

  // 设备监控数据
  const deviceData = ref(new Map());
  const onlineDevices = ref(0);
  const offlineDevices = ref(0);
  const errorDevices = ref(0);

  // 系统监控数据
  const systemMonitorData = ref({
    cpuUsage: 0,
    memoryUsage: 0,
    diskUsage: 0,
    networkTraffic: { inbound: 0, outbound: 0 },
    onlineUsers: 0,
    activeConnections: 0,
    messageQueueLength: 0,
    systemLoad: 0,
    timestamp: Date.now()
  });

  // 图表数据
  const chartData = ref(new Map());
  const chartCache = ref(new Map());

  // 事件总线
  const eventBus = createEnhancedEventEmitter({
    debug: process.env.NODE_ENV === 'development',
    maxListeners: 50,
    cacheSize: 500
  });

  // 统计数据
  const statistics = reactive({
    totalMessages: 0,
    totalAlerts: 0,
    totalConnections: 0,
    averageResponseTime: 0,
    uptime: 0,
    lastUpdate: Date.now()
  });

  // 加载状态
  const loading = ref({
    connecting: false,
    subscribing: false,
    loadingData: false
  });

  // 错误状态
  const errors = ref([]);

  // ==================== 计算属性 ====================

  const isConnected = computed(() =>
    connectionStatus.value.status === 'connected'
  );

  const connectionStatusText = computed(() => {
    const status = connectionStatus.value.status;
    const statusMap = {
      connected: '已连接',
      connecting: '连接中',
      disconnecting: '断开中',
      disconnected: '未连接',
      reconnecting: '重连中',
      error: '连接错误'
    };
    return statusMap[status] || status;
  });

  const connectionStatusColor = computed(() => {
    const status = connectionStatus.value.status;
    const colorMap = {
      connected: 'success',
      connecting: 'processing',
      disconnecting: 'warning',
      disconnected: 'default',
      reconnecting: 'warning',
      error: 'error'
    };
    return colorMap[status] || 'default';
  });

  const criticalAlerts = computed(() =>
    alerts.value.filter(alert => alert.level === 'critical' && !alert.acknowledged)
  );

  const recentMessages = computed(() =>
    messages.value.slice(-10).reverse()
  );

  const deviceStatistics = computed(() => ({
    total: deviceData.value.size,
    online: onlineDevices.value,
    offline: offlineDevices.value,
    error: errorDevices.value,
    onlineRate: deviceData.value.size > 0
      ? (onlineDevices.value / deviceData.value.size * 100).toFixed(1)
      : 0
  }));

  // ==================== 初始化方法 ====================

  /**
   * 初始化实时数据模块
   */
  async function initialize() {
    try {
      loading.value.loadingData = true;

      // 获取配置
      await loadConfig();

      // 初始化WebSocket
      await initializeWebSocket();

      // 设置事件监听
      setupEventListeners();

      // 加载初始数据
      await loadInitialData();

      // 启动定时更新
      startPeriodicUpdate();

    } catch (error) {
      console.error('初始化实时数据模块失败:', error);
      addError(error);
    } finally {
      loading.value.loadingData = false;
    }
  }

  /**
   * 加载配置
   */
  async function loadConfig() {
    try {
      const response = await realtimeApi.getWebSocketConfig();
      if (response.data) {
        Object.assign(config.websocket, response.data);
      }
    } catch (error) {
      console.warn('加载WebSocket配置失败，使用默认配置:', error);
    }
  }

  /**
   * 初始化WebSocket连接
   */
  async function initializeWebSocket() {
    if (!config.websocket.url) {
      throw new Error('WebSocket URL未配置');
    }

    const { client, status, error: wsError } = useWebSocket(config.websocket);
    wsClient.value = client;

    // 监听连接状态变化
    watch(status, (newStatus) => {
      connectionStatus.value = { ...newStatus };
      eventBus.emit('connection-changed', newStatus);
    });

    // 监听连接错误
    watch(wsError, (error) => {
      if (error) {
        addError(error);
        eventBus.emit('connection-error', error);
      }
    });

    // 尝试连接
    await connect();
  }

  /**
   * 设置事件监听
   */
  function setupEventListeners() {
    if (!wsClient.value) return;

    // 监听消息
    wsClient.value.on('message', handleWebSocketMessage);

    // 监听连接事件
    eventBus.on('connection-changed', handleConnectionChanged);
    eventBus.on('data-update', handleDataUpdate);
    eventBus.on('alert-received', handleAlertReceived);
    eventBus.on('device-status-changed', handleDeviceStatusChanged);
    eventBus.on('system-metrics-updated', handleSystemMetricsUpdated);
  }

  /**
   * 处理WebSocket消息
   */
  function handleWebSocketMessage(message) {
    try {
      statistics.totalMessages++;
      statistics.lastUpdate = Date.now();

      switch (message.type) {
        case 'data':
          handleDataMessage(message);
          break;
        case 'alert':
          handleAlertMessage(message);
          break;
        case 'event':
          handleEventMessage(message);
          break;
        case 'status':
          handleStatusMessage(message);
          break;
        default:
          console.warn('未知消息类型:', message.type);
      }

      // 缓存消息
      cacheMessage(message);

    } catch (error) {
      console.error('处理WebSocket消息失败:', error);
      addError(error);
    }
  }

  /**
   * 处理数据消息
   */
  function handleDataMessage(message) {
    const { topic, payload } = message;

    // 根据主题分发数据
    if (topic.startsWith('device.')) {
      updateDeviceData(topic, payload);
    } else if (topic.startsWith('system.')) {
      updateSystemData(topic, payload);
    } else if (topic.startsWith('chart.')) {
      updateChartData(topic, payload);
    }

    eventBus.emit('data-update', { topic, payload, timestamp: message.timestamp });
  }

  /**
   * 处理告警消息
   */
  function handleAlertMessage(message) {
    const alert = message.payload;

    // 添加到告警列表
    alerts.value.unshift(alert);

    // 更新统计
    updateAlertStatistics(alert, 1);

    // 检查是否未读
    if (!alert.read) {
      unreadAlerts.value++;
    }

    // 如果是活跃告警
    if (alert.status === 'active') {
      activeAlerts.value.push(alert);
    }

    // 发送通知
    if (shouldNotify(alert)) {
      sendNotification(alert);
    }

    eventBus.emit('alert-received', alert);
  }

  /**
   * 处理事件消息
   */
  function handleEventMessage(message) {
    const event = message.payload;
    eventBus.emit('event-received', event);
  }

  /**
   * 处理状态消息
   */
  function handleStatusMessage(message) {
    const status = message.payload;
    if (status.system) {
      Object.assign(systemMonitorData.value, status.system);
    }
  }

  /**
   * 处理连接状态变化
   */
  function handleConnectionChanged(status) {
    if (status.status === 'connected') {
      // 重新订阅所有主题
      resubscribeAll();
      statistics.totalConnections++;
    }
  }

  /**
   * 处理数据更新
   */
  function handleDataUpdate(data) {
    eventBus.emit('ui-refresh', data);
  }

  /**
   * 处理告警接收
   */
  function handleAlertReceived(alert) {
    // 触发UI更新
    eventBus.emit('ui-alert', alert);
  }

  /**
   * 处理设备状态变化
   */
  function handleDeviceStatusChanged(device) {
    // 更新设备状态统计
    updateDeviceStatistics();

    // 触发UI更新
    eventBus.emit('ui-device-status', device);
  }

  /**
   * 处理系统指标更新
   */
  function handleSystemMetricsUpdated(metrics) {
    Object.assign(systemMonitorData.value, metrics);

    // 触发UI更新
    eventBus.emit('ui-system-metrics', metrics);
  }

  // ==================== WebSocket操作方法 ====================

  /**
   * 连接WebSocket
   */
  async function connect() {
    if (isConnected.value) {
      return;
    }

    try {
      loading.value.connecting = true;
      await wsClient.value?.connect();
    } catch (error) {
      console.error('WebSocket连接失败:', error);
      addError(error);
      throw error;
    } finally {
      loading.value.connecting = false;
    }
  }

  /**
   * 断开WebSocket连接
   */
  function disconnect() {
    wsClient.value?.close();
    connectionStatus.value.status = 'disconnected';
  }

  /**
   * 订阅主题
   */
  async function subscribe(topic, filters = []) {
    if (!wsClient.value || !isConnected.value) {
      throw new Error('WebSocket未连接');
    }

    try {
      loading.value.subscribing = true;
      const subscriptionId = await wsClient.value.subscribe(topic, filters);

      const subscription = {
        id: subscriptionId,
        topic,
        filters,
        active: true,
        subscribedAt: Date.now()
      };

      subscriptions.value.set(subscriptionId, subscription);
      activeSubscriptions.value.push(subscription);

      return subscriptionId;
    } catch (error) {
      console.error('订阅主题失败:', error);
      addError(error);
      throw error;
    } finally {
      loading.value.subscribing = false;
    }
  }

  /**
   * 取消订阅主题
   */
  function unsubscribe(subscriptionId) {
    if (!wsClient.value) return false;

    try {
      const success = wsClient.value.unsubscribe(subscriptionId);
      if (success) {
        const subscription = subscriptions.value.get(subscriptionId);
        if (subscription) {
          subscription.active = false;
          const index = activeSubscriptions.value.findIndex(s => s.id === subscriptionId);
          if (index > -1) {
            activeSubscriptions.value.splice(index, 1);
          }
        }
      }
      return success;
    } catch (error) {
      console.error('取消订阅失败:', error);
      addError(error);
      return false;
    }
  }

  /**
   * 重新订阅所有主题
   */
  async function resubscribeAll() {
    const subs = Array.from(subscriptions.value.values());
    for (const subscription of subs) {
      if (subscription.active) {
        try {
          await subscribe(subscription.topic, subscription.filters);
        } catch (error) {
          console.error('重新订阅失败:', subscription.topic, error);
        }
      }
    }
  }

  /**
   * 发送消息
   */
  async function sendMessage(message) {
    if (!wsClient.value || !isConnected.value) {
      throw new Error('WebSocket未连接');
    }

    try {
      await wsClient.value.send(message);
    } catch (error) {
      console.error('发送消息失败:', error);
      addError(error);
      throw error;
    }
  }

  // ==================== 数据管理方法 ====================

  /**
   * 更新设备数据
   */
  function updateDeviceData(topic, data) {
    const deviceId = topic.replace('device.', '');
    deviceData.value.set(deviceId, data);

    // 更新设备统计
    updateDeviceStatistics();

    // 触发设备状态变化事件
    eventBus.emit('device-status-changed', data);
  }

  /**
   * 更新系统数据
   */
  function updateSystemData(topic, data) {
    if (topic === 'system.monitor') {
      Object.assign(systemMonitorData.value, data);
      eventBus.emit('system-metrics-updated', data);
    }
  }

  /**
   * 更新图表数据
   */
  function updateChartData(topic, data) {
    const chartId = topic.replace('chart.', '');
    chartData.value.set(chartId, data);
  }

  /**
   * 缓存消息
   */
  function cacheMessage(message) {
    const key = `${message.topic}_${message.timestamp}`;
    messageCache.value.set(key, message);

    // 限制缓存大小
    if (messageCache.value.size > 1000) {
      const oldestKey = messageCache.value.keys().next().value;
      messageCache.value.delete(oldestKey);
    }
  }

  /**
   * 更新告警统计
   */
  function updateAlertStatistics(alert, delta = 1) {
    alertStatistics.value.total += delta;
    if (alert.level) {
      alertStatistics.value[alert.level] += delta;
    }
  }

  /**
   * 更新设备统计
   */
  function updateDeviceStatistics() {
    let online = 0, offline = 0, error = 0;

    deviceData.value.forEach(device => {
      switch (device.status) {
        case 'online':
          online++;
          break;
        case 'offline':
          offline++;
          break;
        case 'error':
          error++;
          break;
      }
    });

    onlineDevices.value = online;
    offlineDevices.value = offline;
    errorDevices.value = error;
  }

  /**
   * 发送通知
   */
  function sendNotification(alert) {
    if (!config.notification.enabled) return;

    const title = `告警: ${alert.title}`;
    const message = alert.content;

    // 浏览器通知
    if (config.notification.desktop && 'Notification' in window) {
      if (Notification.permission === 'granted') {
        new Notification(title, {
          body: message,
          icon: '/favicon.ico',
          tag: alert.id
        });
      }
    }

    // 声音通知
    if (config.notification.sound) {
      // 播放提示音
      const audio = new Audio('/notification.mp3');
      audio.play().catch(() => {});
    }
  }

  /**
   * 判断是否应该发送通知
   */
  function shouldNotify(alert) {
    return config.notification.alertLevels.includes(alert.level);
  }

  /**
   * 加载初始数据
   */
  async function loadInitialData() {
    try {
      // 加载告警列表
      const alertsResponse = await realtimeApi.getAlertList({ limit: 50 });
      alerts.value = alertsResponse.data || [];

      // 加载设备列表
      const devicesResponse = await realtimeApi.getDeviceList();
      devicesResponse.data?.forEach(device => {
        deviceData.value.set(device.deviceId, device);
      });

      // 加载系统监控数据
      const systemResponse = await realtimeApi.getSystemMonitorData();
      if (systemResponse.data) {
        Object.assign(systemMonitorData.value, systemResponse.data);
      }

      // 计算初始统计
      updateDeviceStatistics();
      alerts.value.forEach(alert => {
        updateAlertStatistics(alert, 0);
        if (!alert.read) unreadAlerts.value++;
        if (alert.status === 'active') activeAlerts.value.push(alert);
      });

    } catch (error) {
      console.error('加载初始数据失败:', error);
      addError(error);
    }
  }

  /**
   * 启动定时更新
   */
  function startPeriodicUpdate() {
    // 每30秒更新系统监控数据
    setInterval(async () => {
      if (isConnected.value) return; // 如果连接正常，通过WebSocket更新

      try {
        const response = await realtimeApi.getSystemMonitorData();
        if (response.data) {
          Object.assign(systemMonitorData.value, response.data);
        }
      } catch (error) {
        console.warn('定时更新系统数据失败:', error);
      }
    }, 30000);

    // 每5分钟更新设备状态
    setInterval(async () => {
      if (isConnected.value) return;

      try {
        const response = await realtimeApi.getDeviceList();
        if (response.data) {
          response.data.forEach(device => {
            deviceData.value.set(device.deviceId, device);
          });
          updateDeviceStatistics();
        }
      } catch (error) {
        console.warn('定时更新设备状态失败:', error);
      }
    }, 300000);
  }

  /**
   * 标记告警为已读
   */
  async function markAlertAsRead(alertId) {
    try {
      await realtimeApi.markAlertAsRead(alertId);

      const alert = alerts.value.find(a => a.id === alertId);
      if (alert && !alert.read) {
        alert.read = true;
        unreadAlerts.value = Math.max(0, unreadAlerts.value - 1);
      }
    } catch (error) {
      console.error('标记告警已读失败:', error);
      addError(error);
      throw error;
    }
  }

  /**
   * 确认告警
   */
  async function acknowledgeAlert(alertId, comment) {
    try {
      await realtimeApi.acknowledgeAlert(alertId, comment);

      const alert = alerts.value.find(a => a.id === alertId);
      if (alert) {
        alert.acknowledged = true;
        alert.acknowledgedAt = Date.now();
        alert.acknowledgmentComment = comment;
      }
    } catch (error) {
      console.error('确认告警失败:', error);
      addError(error);
      throw error;
    }
  }

  /**
   * 解决告警
   */
  async function resolveAlert(alertId, resolution) {
    try {
      await realtimeApi.resolveAlert(alertId, resolution);

      const alert = alerts.value.find(a => a.id === alertId);
      if (alert) {
        alert.status = 'resolved';
        alert.resolvedAt = Date.now();
        alert.resolution = resolution;

        // 从活跃告警中移除
        const index = activeAlerts.value.findIndex(a => a.id === alertId);
        if (index > -1) {
          activeAlerts.value.splice(index, 1);
        }
      }
    } catch (error) {
      console.error('解决告警失败:', error);
      addError(error);
      throw error;
    }
  }

  /**
   * 添加错误
   */
  function addError(error) {
    const errorInfo = {
      id: Date.now().toString(),
      message: error.message || error,
      timestamp: Date.now(),
      type: error.name || 'Error'
    };
    errors.value.unshift(errorInfo);

    // 限制错误记录数量
    if (errors.value.length > 100) {
      errors.value = errors.value.slice(0, 100);
    }
  }

  /**
   * 清除错误
   */
  function clearErrors() {
    errors.value = [];
  }

  /**
   * 清理数据
   */
  function cleanup() {
    disconnect();
    eventBus.destroy();
    subscriptions.value.clear();
    activeSubscriptions.value = [];
    messages.value = [];
    alerts.value = [];
    deviceData.value.clear();
    chartData.value.clear();
    messageCache.value.clear();
    chartCache.value.clear();
  }

  // ==================== 导出 ====================

  return {
    // 状态
    connectionStatus,
    config,
    subscriptions,
    activeSubscriptions,
    messages,
    unreadMessages,
    alerts,
    unreadAlerts,
    activeAlerts,
    alertStatistics,
    deviceData,
    onlineDevices,
    offlineDevices,
    errorDevices,
    systemMonitorData,
    chartData,
    statistics,
    loading,
    errors,
    eventBus,

    // 计算属性
    isConnected,
    connectionStatusText,
    connectionStatusColor,
    criticalAlerts,
    recentMessages,
    deviceStatistics,

    // 方法
    initialize,
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    resubscribeAll,
    sendMessage,
    markAlertAsRead,
    acknowledgeAlert,
    resolveAlert,
    clearErrors,
    cleanup
  };
});