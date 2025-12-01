/**
 * uni-app WebSocket 客户端
 *
 * 提供移动端优化的 WebSocket 连接管理
 * 支持网络切换、后台运行、电池优化等特性
 *
 * @author SmartAdmin
 * @since 2025-11-14
 */

import { ref, reactive, onUnmounted, nextTick } from 'vue'

/**
 * WebSocket 连接状态枚举
 */
export const WebSocketStatus = {
  DISCONNECTED: 'disconnected',
  CONNECTING: 'connecting',
  CONNECTED: 'connected',
  RECONNECTING: 'reconnecting',
  ERROR: 'error'
}

/**
 * 消息类型枚举
 */
export const MessageType = {
  HEARTBEAT: 'heartbeat',
  NOTIFICATION: 'notification',
  ALERT: 'alert',
  DEVICE_STATUS: 'device_status',
  PERMISSION_UPDATE: 'permission_update',
  ACCESS_EVENT: 'access_event',
  AREA_EVENT: 'area_event'
}

/**
 * uni-app WebSocket 客户端类
 */
export class UniAppWebSocketClient {
  constructor(options = {}) {
    // 默认配置
    this.config = {
      url: '',
      protocols: [],
      timeout: 10000,
      heartbeatInterval: 30000,
      reconnectInterval: 3000,
      maxReconnectAttempts: 10,
      enableBackgroundMode: true,
      enableNetworkListener: true,
      enableBatteryOptimization: true,
      messageQueueSize: 100,
      ...options
    }

    // 连接状态
    this.status = ref(WebSocketStatus.DISCONNECTED)
    this.ws = null
    this.reconnectCount = 0
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.networkListener = null

    // 连接信息
    this.connectionInfo = reactive({
      url: '',
      connectedAt: null,
      lastActiveAt: null,
      reconnectCount: 0,
      error: null,
      networkType: 'unknown'
    })

    // 统计信息
    this.statistics = reactive({
      totalMessages: 0,
      sentMessages: 0,
      receivedMessages: 0,
      failedMessages: 0,
      totalBytes: 0,
      sentBytes: 0,
      receivedBytes: 0,
      averageLatency: 0,
      connectionUptime: 0
    })

    // 消息队列
    this.messageQueue = []
    this.messageCallbacks = new Map()

    // 事件监听器
    this.eventListeners = new Map()

    // 后台模式管理
    this.isInBackground = false
    this.backgroundMessageBuffer = []

    // 网络状态
    this.networkStatus = reactive({
      isConnected: false,
      networkType: 'unknown',
      effectiveType: 'unknown'
    })

    // 电池优化
    this.batteryLevel = 100
    this.isCharging = false

    // 初始化
    this.init()
  }

  /**
   * 初始化
   */
  init() {
    this.setupNetworkListener()
    this.setupBatteryListener()
    this.setupAppLifecycleListener()
  }

  /**
   * 连接 WebSocket
   */
  async connect(url = null) {
    if (this.status.value === WebSocketStatus.CONNECTING ||
        this.status.value === WebSocketStatus.CONNECTED) {
      return
    }

    try {
      this.status.value = WebSocketStatus.CONNECTING
      this.connectionInfo.url = url || this.config.url

      // 检查网络状态
      if (!this.networkStatus.isConnected) {
        throw new Error('网络不可用，无法建立连接')
      }

      // 创建 WebSocket 连接
      this.ws = uni.connectSocket({
        url: this.connectionInfo.url,
        protocols: this.config.protocols,
        timeout: this.config.timeout
      })

      // 设置事件监听
      this.setupWebSocketEvents()

      console.log('WebSocket 连接已创建:', this.connectionInfo.url)

    } catch (error) {
      console.error('WebSocket 连接失败:', error)
      this.handleConnectionError(error)
    }
  }

  /**
   * 断开连接
   */
  async disconnect() {
    try {
      // 清理定时器
      this.clearTimers()

      // 更新状态
      this.status.value = WebSocketStatus.DISCONNECTED
      this.connectionInfo.error = '主动断开连接'

      // 关闭连接
      if (this.ws) {
        this.ws.close({
          code: 1000,
          reason: 'Normal closure'
        })
        this.ws = null
      }

      console.log('WebSocket 连接已断开')

    } catch (error) {
      console.error('断开连接时发生错误:', error)
    }
  }

  /**
   * 发送消息
   */
  async sendMessage(message, options = {}) {
    try {
      if (this.status.value !== WebSocketStatus.CONNECTED) {
        throw new Error('WebSocket 未连接')
      }

      const messageData = {
        id: this.generateMessageId(),
        type: message.type || MessageType.NOTIFICATION,
        data: message,
        timestamp: Date.now(),
        priority: options.priority || 'normal',
        retryCount: 0,
        ...options
      }

      // 序列化消息
      const serializedMessage = JSON.stringify(messageData)
      const messageSize = new Blob([serializedMessage]).size

      // 发送消息
      await new Promise((resolve, reject) => {
        this.ws.send({
          data: serializedMessage,
          success: resolve,
          fail: reject
        })
      })

      // 更新统计
      this.statistics.sentMessages++
      this.statistics.sentBytes += messageSize
      this.statistics.totalMessages++
      this.statistics.totalBytes += messageSize
      this.connectionInfo.lastActiveAt = Date.now()

      console.log('消息发送成功:', messageData.type, messageData.id)

      return messageData.id

    } catch (error) {
      this.statistics.failedMessages++
      console.error('发送消息失败:', error)

      // 如果启用重试且连接正常，加入重试队列
      if (options.retry !== false && this.status.value === WebSocketStatus.CONNECTED) {
        this.addToRetryQueue(message, options)
      }

      throw error
    }
  }

  /**
   * 发送心跳
   */
  async sendHeartbeat() {
    try {
      await this.sendMessage({
        type: MessageType.HEARTBEAT,
        data: {
          timestamp: Date.now(),
          batteryLevel: this.batteryLevel,
          isCharging: this.isCharging,
          networkType: this.networkStatus.networkType,
          isInBackground: this.isInBackground
        }
      }, { priority: 'low', retry: false })

      console.log('心跳发送成功')

    } catch (error) {
      console.error('心跳发送失败:', error)
      this.handleConnectionError(error)
    }
  }

  /**
   * 订阅主题
   */
  async subscribe(topic, callback) {
    try {
      await this.sendMessage({
        type: 'subscribe',
        data: { topic }
      })

      // 存储回调
      if (!this.messageCallbacks.has(topic)) {
        this.messageCallbacks.set(topic, new Set())
      }
      this.messageCallbacks.get(topic).add(callback)

      console.log('订阅主题成功:', topic)

    } catch (error) {
      console.error('订阅主题失败:', error)
      throw error
    }
  }

  /**
   * 取消订阅
   */
  async unsubscribe(topic, callback = null) {
    try {
      await this.sendMessage({
        type: 'unsubscribe',
        data: { topic }
      })

      if (callback) {
        this.messageCallbacks.get(topic)?.delete(callback)
      } else {
        this.messageCallbacks.delete(topic)
      }

      console.log('取消订阅成功:', topic)

    } catch (error) {
      console.error('取消订阅失败:', error)
      throw error
    }
  }

  /**
   * 设置 WebSocket 事件监听
   */
  setupWebSocketEvents() {
    if (!this.ws) return

    // 连接打开
    this.ws.onOpen(() => {
      console.log('WebSocket 连接已建立')
      this.handleConnectionSuccess()
    })

    // 接收消息
    this.ws.onMessage((event) => {
      this.handleMessageReceived(event)
    })

    // 连接错误
    this.ws.onError((error) => {
      console.error('WebSocket 连接错误:', error)
      this.handleConnectionError(error)
    })

    // 连接关闭
    this.ws.onClose((event) => {
      console.log('WebSocket 连接已关闭:', event.code, event.reason)
      this.handleConnectionClose(event.code, event.reason)
    })
  }

  /**
   * 处理连接成功
   */
  handleConnectionSuccess() {
    this.status.value = WebSocketStatus.CONNECTED
    this.reconnectCount = 0
    this.connectionInfo.connectedAt = Date.now()
    this.connectionInfo.lastActiveAt = Date.now()
    this.connectionInfo.error = null

    // 启动心跳
    this.startHeartbeat()

    // 发送缓冲的消息
    this.flushMessageQueue()

    // 触发事件
    this.emit('connected', this.getConnectionInfo())

    console.log('WebSocket 连接成功')
  }

  /**
   * 处理消息接收
   */
  handleMessageReceived(event) {
    try {
      let messageData

      // 解析消息
      if (typeof event.data === 'string') {
        messageData = JSON.parse(event.data)
      } else {
        // 处理二进制数据
        const reader = new FileReader()
        reader.onload = () => {
          const text = reader.result
          messageData = JSON.parse(text)
          this.processMessage(messageData)
        }
        reader.readAsText(event.data)
        return
      }

      this.processMessage(messageData)

    } catch (error) {
      console.error('处理接收消息失败:', error)
      this.statistics.failedMessages++
    }
  }

  /**
   * 处理消息
   */
  processMessage(message) {
    // 更新统计
    this.statistics.receivedMessages++
    this.statistics.totalMessages++
    this.connectionInfo.lastActiveAt = Date.now()

    // 心跳响应
    if (message.type === MessageType.HEARTBEAT) {
      this.handleHeartbeatResponse(message)
      return
    }

    // 后台模式下的消息处理
    if (this.isInBackground) {
      this.backgroundMessageBuffer.push(message)
      if (this.config.enableBackgroundMode) {
        this.handleBackgroundMessage(message)
      }
      return
    }

    // 调用主题回调
    const topic = message.topic || message.type
    const callbacks = this.messageCallbacks.get(topic)
    if (callbacks) {
      callbacks.forEach(callback => {
        try {
          callback(message)
        } catch (error) {
          console.error('执行消息回调失败:', error)
        }
      })
    }

    // 触发通用消息事件
    this.emit('message', message)
    this.emit(`message:${message.type}`, message)

    console.log('消息处理完成:', message.type, message.id)
  }

  /**
   * 处理心跳响应
   */
  handleHeartbeatResponse(message) {
    // 计算延迟
    if (message.data && message.data.timestamp) {
      const latency = Date.now() - message.data.timestamp
      this.statistics.averageLatency =
        (this.statistics.averageLatency + latency) / 2
    }

    console.log('收到心跳响应')
  }

  /**
   * 处理后台消息
   */
  handleBackgroundMessage(message) {
    // 重要消息的处理逻辑
    if (message.priority === 'high' || message.type === MessageType.ALERT) {
      // 可以在这里实现本地通知
      this.showLocalNotification(message)
    }
  }

  /**
   * 显示本地通知
   */
  showLocalNotification(message) {
    // #ifdef APP-PLUS
    try {
      const main = plus.android.runtimeMainActivity();
      const NotificationManager = plus.android.importClass("android.app.NotificationManager");
      const Context = plus.android.importClass("android.content.Context");
      const NotificationCompat = plus.android.importClass("androidx.core.app.NotificationCompat");

      const notificationManager = main.getSystemService(Context.NOTIFICATION_SERVICE);

      const notification = new NotificationCompat.Builder(main, "default_channel")
        .setContentTitle(message.title || "新消息")
        .setContentText(message.content || "您有新的消息")
        .setSmallIcon(plus.android.importClass("android.R.drawable").ic_dialog_info)
        .build();

      notificationManager.notify(1, notification);

    } catch (error) {
      console.error('显示本地通知失败:', error)
    }
    // #endif
  }

  /**
   * 处理连接错误
   */
  handleConnectionError(error) {
    this.status.value = WebSocketStatus.ERROR
    this.connectionInfo.error = error.message || error.toString()

    // 触发错误事件
    this.emit('error', error)

    // 自动重连
    if (this.reconnectCount < this.config.maxReconnectAttempts) {
      this.startReconnect()
    }

    console.error('WebSocket 连接错误:', error)
  }

  /**
   * 处理连接关闭
   */
  handleConnectionClose(code, reason) {
    this.status.value = WebSocketStatus.DISCONNECTED
    this.connectionInfo.error = `连接关闭: ${code} - ${reason}`

    // 清理定时器
    this.clearTimers()

    // 触发关闭事件
    this.emit('closed', { code, reason })

    // 自动重连
    if (this.reconnectCount < this.config.maxReconnectAttempts) {
      this.startReconnect()
    }

    console.log('WebSocket 连接关闭:', code, reason)
  }

  /**
   * 开始重连
   */
  startReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    this.status.value = WebSocketStatus.RECONNECTING
    this.reconnectCount++

    // 指数退避策略
    const delay = Math.min(
      this.config.reconnectInterval * Math.pow(2, this.reconnectCount - 1),
      30000
    )

    console.log(`开始第 ${this.reconnectCount} 次重连，延迟 ${delay}ms`)

    this.reconnectTimer = setTimeout(async () => {
      try {
        await this.connect()
      } catch (error) {
        console.error('重连失败:', error)
      }
    }, delay)

    this.connectionInfo.reconnectCount = this.reconnectCount
  }

  /**
   * 开始心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()

    this.heartbeatTimer = setInterval(() => {
      this.sendHeartbeat()
    }, this.config.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 设置网络监听
   */
  setupNetworkListener() {
    if (!this.config.enableNetworkListener) return

    // 获取当前网络状态
    uni.getNetworkType({
      success: (res) => {
        this.updateNetworkStatus(res.networkType, true)
      }
    })

    // 监听网络状态变化
    uni.onNetworkStatusChange((res) => {
      this.updateNetworkStatus(res.networkType, res.isConnected)

      // 网络恢复时自动重连
      if (res.isConnected &&
          this.status.value === WebSocketStatus.DISCONNECTED &&
          !this.connectionInfo.error?.includes('主动断开')) {
        console.log('网络恢复，尝试重新连接')
        this.reconnectCount = 0
        this.connect()
      }
    })
  }

  /**
   * 更新网络状态
   */
  updateNetworkStatus(networkType, isConnected) {
    this.networkStatus.networkType = networkType
    this.networkStatus.isConnected = isConnected
    this.connectionInfo.networkType = networkType
  }

  /**
   * 设置电池监听
   */
  setupBatteryListener() {
    if (!this.config.enableBatteryOptimization) return

    // #ifdef APP-PLUS
    try {
      const main = plus.android.runtimeMainActivity()
      const batteryManager = main.getSystemService(plus.android.importClass("android.content.Context").BATTERY_SERVICE)

      const updateBatteryInfo = () => {
        this.batteryLevel = batteryManager.getIntProperty(plus.android.importClass("android.os.BatteryManager").BATTERY_PROPERTY_CAPACITY)
        this.isCharging = batteryManager.getIntProperty(plus.android.importClass("android.os.BatteryManager").BATTERY_PROPERTY_STATUS) ===
                         plus.android.importClass("android.os.BatteryManager").BATTERY_STATUS_CHARGING
      }

      // 定期更新电池信息
      setInterval(updateBatteryInfo, 30000)
      updateBatteryInfo()

    } catch (error) {
      console.warn('电池监听设置失败:', error)
    }
    // #endif
  }

  /**
   * 设置应用生命周期监听
   */
  setupAppLifecycleListener() {
    // 监听应用前后台切换
    uni.onAppShow(() => {
      this.handleAppShow()
    })

    uni.onAppHide(() => {
      this.handleAppHide()
    })
  }

  /**
   * 处理应用显示
   */
  handleAppShow() {
    this.isInBackground = false
    console.log('应用进入前台')

    // 处理后台消息缓冲
    if (this.backgroundMessageBuffer.length > 0) {
      console.log(`处理 ${this.backgroundMessageBuffer.length} 条后台消息`)
      this.backgroundMessageBuffer.forEach(message => {
        this.processMessage(message)
      })
      this.backgroundMessageBuffer = []
    }

    // 触发事件
    this.emit('appShow')
  }

  /**
   * 处理应用隐藏
   */
  handleAppHide() {
    this.isInBackground = true
    console.log('应用进入后台')

    // 触发事件
    this.emit('appHide')
  }

  /**
   * 添加到重试队列
   */
  addToRetryQueue(message, options) {
    if (this.messageQueue.length >= this.config.messageQueueSize) {
      this.messageQueue.shift() // 移除最旧的消息
    }

    this.messageQueue.push({
      message,
      options,
      timestamp: Date.now()
    })
  }

  /**
   * 清空消息队列
   */
  flushMessageQueue() {
    while (this.messageQueue.length > 0) {
      const item = this.messageQueue.shift()
      this.sendMessage(item.message, item.options).catch(error => {
        console.error('重试发送消息失败:', error)
      })
    }
  }

  /**
   * 清理定时器
   */
  clearTimers() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 生成消息ID
   */
  generateMessageId() {
    return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * 事件监听
   */
  on(event, callback) {
    if (!this.eventListeners.has(event)) {
      this.eventListeners.set(event, new Set())
    }
    this.eventListeners.get(event).add(callback)
  }

  /**
   * 取消事件监听
   */
  off(event, callback = null) {
    if (callback) {
      this.eventListeners.get(event)?.delete(callback)
    } else {
      this.eventListeners.delete(event)
    }
  }

  /**
   * 触发事件
   */
  emit(event, data = null) {
    const listeners = this.eventListeners.get(event)
    if (listeners) {
      listeners.forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error('执行事件回调失败:', error)
        }
      })
    }
  }

  /**
   * 获取连接信息
   */
  getConnectionInfo() {
    return {
      ...this.connectionInfo,
      status: this.status.value,
      statistics: { ...this.statistics },
      networkStatus: { ...this.networkStatus },
      batteryInfo: {
        level: this.batteryLevel,
        isCharging: this.isCharging
      }
    }
  }

  /**
   * 销毁客户端
   */
  destroy() {
    this.disconnect()
    this.clearTimers()
    this.eventListeners.clear()
    this.messageCallbacks.clear()
    this.messageQueue = []
    this.backgroundMessageBuffer = []
  }
}

/**
 * 创建 WebSocket 客户端实例
 */
export function createWebSocketClient(options = {}) {
  return new UniAppWebSocketClient(options)
}

/**
 * 使用 WebSocket 的 Composable
 */
export function useWebSocket(options = {}) {
  const client = createWebSocketClient(options)

  onUnmounted(() => {
    client.destroy()
  })

  return {
    client,
    status: client.status,
    connectionInfo: client.connectionInfo,
    statistics: client.statistics,
    networkStatus: client.networkStatus,
    connect: client.connect.bind(client),
    disconnect: client.disconnect.bind(client),
    sendMessage: client.sendMessage.bind(client),
    subscribe: client.subscribe.bind(client),
    unsubscribe: client.unsubscribe.bind(client),
    on: client.on.bind(client),
    off: client.off.bind(client)
  }
}

export default UniAppWebSocketClient