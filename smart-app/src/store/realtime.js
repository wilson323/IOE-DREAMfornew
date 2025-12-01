/**
 * 移动端实时通信状态管理
 *
 * 提供移动端优化的实时通信状态管理
 * 支持网络状态、电池状态、后台运行等移动端特性
 *
 * @author SmartAdmin
 * @since 2025-11-14
 */

import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'
import { useWebSocket, WebSocketStatus, MessageType } from '@/utils/websocket'

/**
 * 实时通信 Store
 */
export const useRealtimeStore = defineStore('realtime', () => {
  // WebSocket 客户端
  const { client, status, connectionInfo, statistics, networkStatus } = useWebSocket({
    url: 'ws://localhost:1024/ws',
    heartbeatInterval: 30000,
    enableBackgroundMode: true,
    enableNetworkListener: true,
    enableBatteryOptimization: true
  })

  // 基础状态
  const isConnected = computed(() => status.value === WebSocketStatus.CONNECTED)
  const isConnecting = computed(() => status.value === WebSocketStatus.CONNECTING)
  const isReconnecting = computed(() => status.value === WebSocketStatus.RECONNECTING)
  const hasError = computed(() => status.value === WebSocketStatus.ERROR)

  // 消息状态
  const messages = ref([])
  const alerts = ref([])
  const notifications = ref([])
  const deviceStatuses = ref(new Map())
  const unreadCount = ref(0)

  // 移动端特定状态
  const appState = reactive({
    isInBackground: false,
    networkType: 'unknown',
    batteryLevel: 100,
    isCharging: false,
    lastActiveAt: Date.now(),
    backgroundSyncEnabled: true
  })

  // 连接配置
  const connectionConfig = reactive({
    autoConnect: true,
    reconnectOnNetworkRestore: true,
    enableNotifications: true,
    enableVibration: true,
    maxBackgroundMessages: 50,
    heartbeatInterval: 30000,
    messageRetentionDays: 7
  })

  // 统计信息
  const realtimeStats = reactive({
    totalMessages: 0,
    unreadMessages: 0,
    alertsToday: 0,
    notificationsToday: 0,
    dataUsage: {
      sent: 0,
      received: 0
    },
    connectionQuality: 'good',
    lastSyncAt: null
  })

  // 事件处理
  const setupEventListeners = () => {
    // 连接事件
    client.on('connected', handleConnectionSuccess)
    client.on('error', handleConnectionError)
    client.on('closed', handleConnectionClosed)

    // 消息事件
    client.on('message', handleNewMessage)
    client.on(`message:${MessageType.NOTIFICATION}`, handleNotification)
    client.on(`message:${MessageType.ALERT}`, handleAlert)
    client.on(`message:${MessageType.DEVICE_STATUS}`, handleDeviceStatus)
    client.on(`message:${MessageType.PERMISSION_UPDATE}`, handlePermissionUpdate)
    client.on(`message:${MessageType.ACCESS_EVENT}`, handleAccessEvent)
    client.on(`message:${MessageType.AREA_EVENT}`, handleAreaEvent)

    // 应用生命周期事件
    client.on('appShow', handleAppShow)
    client.on('appHide', handleAppHide)
  }

  /**
   * 初始化实时通信
   */
  const initialize = async () => {
    try {
      console.log('初始化移动端实时通信')

      setupEventListeners()

      // 恢复状态
      await restoreState()

      // 自动连接
      if (connectionConfig.autoConnect) {
        await connect()
      }

      console.log('移动端实时通信初始化完成')

    } catch (error) {
      console.error('实时通信初始化失败:', error)
      throw error
    }
  }

  /**
   * 连接
   */
  const connect = async () => {
    try {
      await client.connect()
    } catch (error) {
      console.error('连接失败:', error)
      throw error
    }
  }

  /**
   * 断开连接
   */
  const disconnect = async () => {
    try {
      await client.disconnect()
    } catch (error) {
      console.error('断开连接失败:', error)
      throw error
    }
  }

  /**
   * 发送消息
   */
  const sendMessage = async (message, options = {}) => {
    try {
      const messageId = await client.sendMessage(message, options)

      // 更新统计
      realtimeStats.totalMessages++
      realtimeStats.dataUsage.sent += JSON.stringify(message).length

      // 保存到本地
      if (options.save !== false) {
        await saveMessage({
          id: messageId,
          type: 'sent',
          message,
          timestamp: Date.now(),
          status: 'sent'
        })
      }

      return messageId

    } catch (error) {
      console.error('发送消息失败:', error)
      throw error
    }
  }

  /**
   * 订阅主题
   */
  const subscribe = async (topic, callback) => {
    try {
      await client.subscribe(topic, callback)
    } catch (error) {
      console.error('订阅主题失败:', error)
      throw error
    }
  }

  /**
   * 取消订阅
   */
  const unsubscribe = async (topic, callback = null) => {
    try {
      await client.unsubscribe(topic, callback)
    } catch (error) {
      console.error('取消订阅失败:', error)
      throw error
    }
  }

  /**
   * 处理连接成功
   */
  const handleConnectionSuccess = () => {
    console.log('WebSocket 连接成功')

    // 同步离线消息
    syncOfflineMessages()

    // 更新最后活跃时间
    appState.lastActiveAt = Date.now()
  }

  /**
   * 处理连接错误
   */
  const handleConnectionError = (error) => {
    console.error('WebSocket 连接错误:', error)

    // 可以在这里显示错误提示
    uni.showToast({
      title: '连接失败',
      icon: 'none',
      duration: 2000
    })
  }

  /**
   * 处理连接关闭
   */
  const handleConnectionClosed = ({ code, reason }) => {
    console.log('WebSocket 连接关闭:', code, reason)
  }

  /**
   * 处理新消息
   */
  const handleNewMessage = (message) => {
    // 添加到消息列表
    messages.value.unshift({
      id: message.id,
      type: message.type,
      content: message,
      timestamp: message.timestamp,
      read: false
    })

    // 限制消息数量
    if (messages.value.length > 100) {
      messages.value = messages.value.slice(0, 100)
    }

    // 更新未读数
    updateUnreadCount()

    // 保存到本地
    saveMessage({
      id: message.id,
      type: 'received',
      message,
      timestamp: message.timestamp,
      read: false
    })
  }

  /**
   * 处理通知
   */
  const handleNotification = (message) => {
    const notification = {
      id: message.id,
      title: message.title || '新通知',
      content: message.content,
      type: message.notificationType || 'info',
      timestamp: message.timestamp,
      read: false
    }

    notifications.value.unshift(notification)

    // 限制通知数量
    if (notifications.value.length > 50) {
      notifications.value = notifications.value.slice(0, 50)
    }

    // 显示通知
    if (connectionConfig.enableNotifications) {
      showNotification(notification)
    }

    // 更新统计
    realtimeStats.notificationsToday++

    // 振动提醒
    if (connectionConfig.enableVibration) {
      vibrate()
    }
  }

  /**
   * 处理告警
   */
  const handleAlert = (message) => {
    const alert = {
      id: message.id,
      title: message.title || '系统告警',
      content: message.content,
      level: message.level || 'warning',
      source: message.source || 'system',
      timestamp: message.timestamp,
      read: false,
      acknowledged: false,
      resolved: false
    }

    alerts.value.unshift(alert)

    // 限制告警数量
    if (alerts.value.length > 100) {
      alerts.value = alerts.value.slice(0, 100)
    }

    // 高优先级告警特殊处理
    if (message.level === 'critical' || message.level === 'error') {
      showUrgentNotification(alert)
      vibrate([200, 100, 200])
    }

    // 更新统计
    realtimeStats.alertsToday++
  }

  /**
   * 处理设备状态更新
   */
  const handleDeviceStatus = (message) => {
    const deviceId = message.deviceId
    const status = message.status

    deviceStatuses.value.set(deviceId, {
      deviceId,
      status,
      lastUpdate: message.timestamp,
      ...message.data
    })

    // 触发设备状态变化事件
    uni.$emit('device-status-changed', { deviceId, status })
  }

  /**
   * 处理权限变更
   */
  const handlePermissionUpdate = (message) => {
    console.log('权限变更通知:', message)

    // 刷新权限缓存
    uni.$emit('permission-updated', {
      userId: message.userId,
      permissions: message.permissions,
      timestamp: message.timestamp
    })
  }

  /**
   * 处理门禁事件
   */
  const handleAccessEvent = (message) => {
    console.log('门禁事件通知:', message)

    // 可以在这里实现门禁事件的处理逻辑
    uni.$emit('access-event', message)
  }

  /**
   * 处理区域事件
   */
  const handleAreaEvent = (message) => {
    console.log('区域事件通知:', message)

    uni.$emit('area-event', message)
  }

  /**
   * 处理应用显示
   */
  const handleAppShow = () => {
    appState.isInBackground = false

    // 检查连接状态
    if (!isConnected.value && connectionConfig.autoConnect) {
      connect()
    }

    // 同步后台消息
    syncBackgroundMessages()
  }

  /**
   * 处理应用隐藏
   */
  const handleAppHide = () => {
    appState.isInBackground = true

    // 保存当前状态
    saveState()
  }

  /**
   * 显示通知
   */
  const showNotification = (notification) => {
    uni.showToast({
      title: notification.title,
      icon: 'none',
      duration: 3000
    })
  }

  /**
   * 显示紧急通知
   */
  const showUrgentNotification = (alert) => {
    uni.showModal({
      title: alert.title,
      content: alert.content,
      showCancel: false,
      confirmText: '知道了',
      success: () => {
        markAlertAsRead(alert.id)
      }
    })
  }

  /**
   * 振动提醒
   */
  const vibrate = (pattern = [100]) => {
    // #ifdef APP-PLUS
    try {
      plus.device.vibrate(pattern)
    } catch (error) {
      console.warn('振动失败:', error)
    }
    // #endif

    // #ifdef H5
    if ('vibrate' in navigator) {
      navigator.vibrate(pattern)
    }
    // #endif
  }

  /**
   * 同步离线消息
   */
  const syncOfflineMessages = async () => {
    try {
      // 从本地存储读取离线消息
      const offlineMessages = uni.getStorageSync('realtime_offline_messages') || []

      // 发送离线消息
      for (const message of offlineMessages) {
        if (message.type === 'sent' && message.status === 'pending') {
          try {
            await client.sendMessage(message.message, { retry: true })
            message.status = 'sent'
          } catch (error) {
            console.error('同步离线消息失败:', error)
          }
        }
      }

      // 保存同步结果
      uni.setStorageSync('realtime_offline_messages', offlineMessages)

      realtimeStats.lastSyncAt = Date.now()

    } catch (error) {
      console.error('同步离线消息失败:', error)
    }
  }

  /**
   * 同步后台消息
   */
  const syncBackgroundMessages = async () => {
    if (!appState.backgroundSyncEnabled) return

    // 处理后台期间的消息
    const backgroundMessages = client.backgroundMessageBuffer || []
    backgroundMessages.forEach(message => {
      handleNewMessage(message)
    })

    // 清空后台消息缓冲
    client.backgroundMessageBuffer = []
  }

  /**
   * 更新未读数
   */
  const updateUnreadCount = () => {
    let count = 0

    // 统计未读消息
    messages.value.forEach(msg => {
      if (!msg.read) count++
    })

    // 统计未读通知
    notifications.value.forEach(notif => {
      if (!notif.read) count++
    })

    // 统计未读告警
    alerts.value.forEach(alert => {
      if (!alert.read) count++
    })

    unreadCount.value = count
    realtimeStats.unreadMessages = count
  }

  /**
   * 标记消息为已读
   */
  const markMessageAsRead = (messageId) => {
    const message = messages.value.find(msg => msg.id === messageId)
    if (message) {
      message.read = true
      updateUnreadCount()
      saveMessage(message)
    }
  }

  /**
   * 标记通知为已读
   */
  const markNotificationAsRead = (notificationId) => {
    const notification = notifications.value.find(notif => notif.id === notificationId)
    if (notification) {
      notification.read = true
      updateUnreadCount()
    }
  }

  /**
   * 标记告警为已读
   */
  const markAlertAsRead = (alertId) => {
    const alert = alerts.value.find(a => a.id === alertId)
    if (alert) {
      alert.read = true
      alert.acknowledged = true
      updateUnreadCount()
    }
  }

  /**
   * 保存消息到本地
   */
  const saveMessage = async (message) => {
    try {
      const savedMessages = uni.getStorageSync('realtime_messages') || []
      savedMessages.unshift(message)

      // 限制存储数量
      if (savedMessages.length > 1000) {
        savedMessages.splice(1000)
      }

      uni.setStorageSync('realtime_messages', savedMessages)
    } catch (error) {
      console.error('保存消息失败:', error)
    }
  }

  /**
   * 保存状态
   */
  const saveState = () => {
    try {
      const state = {
        connectionConfig,
        appState,
        realtimeStats,
        lastSaved: Date.now()
      }
      uni.setStorageSync('realtime_state', state)
    } catch (error) {
      console.error('保存状态失败:', error)
    }
  }

  /**
   * 恢复状态
   */
  const restoreState = async () => {
    try {
      const savedState = uni.getStorageSync('realtime_state')
      if (savedState) {
        Object.assign(connectionConfig, savedState.connectionConfig || {})
        Object.assign(appState, savedState.appState || {})
        Object.assign(realtimeStats, savedState.realtimeStats || {})
      }

      // 恢复消息历史
      const savedMessages = uni.getStorageSync('realtime_messages') || []
      messages.value = savedMessages.slice(0, 50) // 只恢复最近的50条

      // 恢复设备状态
      const savedDeviceStatuses = uni.getStorageSync('realtime_device_statuses') || {}
      deviceStatuses.value = new Map(Object.entries(savedDeviceStatuses))

      updateUnreadCount()

    } catch (error) {
      console.error('恢复状态失败:', error)
    }
  }

  /**
   * 清理过期数据
   */
  const cleanupExpiredData = () => {
    const retentionMs = connectionConfig.messageRetentionDays * 24 * 60 * 60 * 1000
    const cutoffTime = Date.now() - retentionMs

    // 清理过期消息
    messages.value = messages.value.filter(msg => msg.timestamp > cutoffTime)
    notifications.value = notifications.value.filter(notif => notif.timestamp > cutoffTime)
    alerts.value = alerts.value.filter(alert => alert.timestamp > cutoffTime)

    // 保存清理结果
    saveState()
  }

  /**
   * 获取连接质量
   */
  const getConnectionQuality = computed(() => {
    const latency = statistics.value.averageLatency
    const reconnectCount = connectionInfo.value.reconnectCount

    if (latency < 100 && reconnectCount === 0) return 'excellent'
    if (latency < 300 && reconnectCount <= 2) return 'good'
    if (latency < 1000 && reconnectCount <= 5) return 'fair'
    return 'poor'
  })

  /**
   * 获取今日统计
   */
  const getTodayStats = computed(() => {
    const today = new Date().toDateString()

    const todayMessages = messages.value.filter(msg =>
      new Date(msg.timestamp).toDateString() === today
    )

    const todayAlerts = alerts.value.filter(alert =>
      new Date(alert.timestamp).toDateString() === today
    )

    const todayNotifications = notifications.value.filter(notif =>
      new Date(notif.timestamp).toDateString() === today
    )

    return {
      messages: todayMessages.length,
      alerts: todayAlerts.length,
      notifications: todayNotifications.length,
      unread: todayMessages.filter(msg => !msg.read).length +
             todayAlerts.filter(alert => !alert.read).length +
             todayNotifications.filter(notif => !notif.read).length
    }
  })

  return {
    // 状态
    status,
    connectionInfo,
    statistics,
    networkStatus,
    isConnected,
    isConnecting,
    isReconnecting,
    hasError,
    messages,
    alerts,
    notifications,
    deviceStatuses,
    unreadCount,
    appState,
    connectionConfig,
    realtimeStats,

    // 计算属性
    getConnectionQuality,
    getTodayStats,

    // 方法
    initialize,
    connect,
    disconnect,
    sendMessage,
    subscribe,
    unsubscribe,
    markMessageAsRead,
    markNotificationAsRead,
    markAlertAsRead,
    cleanupExpiredData,
    saveState,
    restoreState
  }
})

export default useRealtimeStore