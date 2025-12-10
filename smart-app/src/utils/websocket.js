import { ref, reactive, readonly } from 'vue'

/**
 * WebSocket 工具类
 * 提供WebSocket连接管理和通信功能
 */

/**
 * 创建WebSocket客户端
 * @param {Object} options 配置选项
 * @param {string} options.url WebSocket地址
 * @param {number} options.heartbeatInterval 心跳间隔时间（毫秒）
 * @param {number} options.reconnectInterval 重连间隔时间（毫秒）
 * @param {number} options.maxReconnectAttempts 最大重连次数
 * @param {Function} options.onOpen 连接成功回调
 * @param {Function} options.onMessage 消息接收回调
 * @param {Function} options.onClose 连接关闭回调
 * @param {Function} options.onError 错误回调
 * @returns {Object} WebSocket客户端实例
 */
export function useWebSocket(options = {}) {
  const {
    url,
    heartbeatInterval = 30000,
    reconnectInterval = 3000,
    maxReconnectAttempts = 5,
    onOpen,
    onMessage,
    onClose,
    onError
  } = options

  let ws = null
  let heartbeatTimer = null
  let reconnectTimer = null
  let reconnectAttempts = 0
  let isManualClose = false

  // 连接状态
  const connectionStatus = ref({
    connected: false,
    connecting: false,
    error: null
  })

  // 消息队列
  const messageQueue = reactive([])
  const subscribers = new Map()

  // 创建WebSocket连接
  const connect = () => {
    if (ws && (ws.readyState === WebSocket.CONNECTING || ws.readyState === WebSocket.OPEN)) {
      return
    }

    isManualClose = false
    connectionStatus.value.connecting = true
    connectionStatus.value.error = null

    try {
      // uni-app环境使用uni.connectSocket
      if (typeof uni !== 'undefined' && uni.connectSocket) {
        ws = uni.connectSocket({
          url,
          protocols: []
        })

        ws.onOpen(() => {
          console.log('[WebSocket] 连接成功:', url)
          connectionStatus.value.connected = true
          connectionStatus.value.connecting = false
          reconnectAttempts = 0

          startHeartbeat()
          processMessageQueue()

          onOpen?.()
        })

        ws.onMessage((event) => {
          try {
            const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data
            handleMessage(data)
            onMessage?.(data)
          } catch (error) {
            console.error('[WebSocket] 消息解析错误:', error)
          }
        })

        ws.onClose(() => {
          console.log('[WebSocket] 连接关闭:', url)
          connectionStatus.value.connected = false
          connectionStatus.value.connecting = false

          stopHeartbeat()

          if (!isManualClose && reconnectAttempts < maxReconnectAttempts) {
            reconnect()
          }

          onClose?.()
        })

        ws.onError((error) => {
          console.error('[WebSocket] 连接错误:', error)
          connectionStatus.value.error = error
          connectionStatus.value.connecting = false

          onError?.(error)
        })
      } else {
        // 标准WebSocket
        ws = new WebSocket(url)

        ws.onopen = () => {
          console.log('[WebSocket] 连接成功:', url)
          connectionStatus.value.connected = true
          connectionStatus.value.connecting = false
          reconnectAttempts = 0

          startHeartbeat()
          processMessageQueue()

          onOpen?.()
        }

        ws.onmessage = (event) => {
          try {
            const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data
            handleMessage(data)
            onMessage?.(data)
          } catch (error) {
            console.error('[WebSocket] 消息解析错误:', error)
          }
        }

        ws.onclose = () => {
          console.log('[WebSocket] 连接关闭:', url)
          connectionStatus.value.connected = false
          connectionStatus.value.connecting = false

          stopHeartbeat()

          if (!isManualClose && reconnectAttempts < maxReconnectAttempts) {
            reconnect()
          }

          onClose?.()
        }

        ws.onerror = (error) => {
          console.error('[WebSocket] 连接错误:', error)
          connectionStatus.value.error = error
          connectionStatus.value.connecting = false

          onError?.(error)
        }
      }
    } catch (error) {
      console.error('[WebSocket] 创建连接失败:', error)
      connectionStatus.value.error = error
      connectionStatus.value.connecting = false
    }
  }

  // 重连
  const reconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
    }

    reconnectTimer = setTimeout(() => {
      reconnectAttempts++
      console.log(`[WebSocket] 尝试重连 (${reconnectAttempts}/${maxReconnectAttempts}):`, url)
      connect()
    }, reconnectInterval)
  }

  // 发送消息
  const send = (data) => {
    if (!ws || !connectionStatus.value.connected) {
      console.warn('[WebSocket] 连接未建立，消息加入队列:', data)
      messageQueue.push({
        data,
        timestamp: Date.now()
      })
      return false
    }

    try {
      const message = typeof data === 'string' ? data : JSON.stringify(data)

      if (typeof uni !== 'undefined' && ws.send) {
        ws.send({
          data: message
        })
      } else if (ws.readyState === WebSocket.OPEN) {
        ws.send(message)
      }

      return true
    } catch (error) {
      console.error('[WebSocket] 发送消息失败:', error)
      return false
    }
  }

  // 处理消息队列
  const processMessageQueue = () => {
    while (messageQueue.length > 0) {
      const { data } = messageQueue.shift()
      send(data)
    }
  }

  // 处理接收到的消息
  const handleMessage = (data) => {
    if (data.type && subscribers.has(data.type)) {
      const handlers = subscribers.get(data.type)
      handlers.forEach(handler => {
        try {
          handler(data)
        } catch (error) {
          console.error('[WebSocket] 消息处理错误:', error)
        }
      })
    }
  }

  // 订阅消息
  const subscribe = (type, handler) => {
    if (!subscribers.has(type)) {
      subscribers.set(type, new Set())
    }
    subscribers.get(type).add(handler)

    // 返回取消订阅函数
    return () => {
      const handlers = subscribers.get(type)
      if (handlers) {
        handlers.delete(handler)
        if (handlers.size === 0) {
          subscribers.delete(type)
        }
      }
    }
  }

  // 开始心跳
  const startHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
    }

    heartbeatTimer = setInterval(() => {
      send({
        type: 'ping',
        timestamp: Date.now()
      })
    }, heartbeatInterval)
  }

  // 停止心跳
  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  // 关闭连接
  const close = () => {
    isManualClose = true

    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }

    stopHeartbeat()

    if (ws) {
      if (typeof uni !== 'undefined' && ws.close) {
        ws.close()
      } else {
        ws.close()
      }
      ws = null
    }

    connectionStatus.value.connected = false
    connectionStatus.value.connecting = false
    messageQueue.length = 0
    subscribers.clear()
  }

  // 获取连接状态
  const getStatus = () => {
    return {
      connected: connectionStatus.value.connected,
      connecting: connectionStatus.value.connecting,
      error: connectionStatus.value.error,
      reconnectAttempts
    }
  }

  // 自动连接
  if (url) {
    connect()
  }

  return {
    // 状态
    connected: readonly(connectionStatus).value.connected,
    connecting: readonly(connectionStatus).value.connecting,
    error: readonly(connectionStatus).value.error,

    // 方法
    connect,
    send,
    subscribe,
    close,
    getStatus,

    // 工具方法
    ping: () => send({ type: 'ping', timestamp: Date.now() })
  }
}

/**
 * WebSocket 管理器
 * 管理多个WebSocket连接
 */
export class WebSocketManager {
  constructor() {
    this.connections = new Map()
  }

  /**
   * 创建连接
   * @param {string} id 连接ID
   * @param {Object} options 配置选项
   * @returns {Object} WebSocket客户端
   */
  create(id, options) {
    if (this.connections.has(id)) {
      this.close(id)
    }

    const client = useWebSocket(options)
    this.connections.set(id, client)
    return client
  }

  /**
   * 获取连接
   * @param {string} id 连接ID
   * @returns {Object|null} WebSocket客户端
   */
  get(id) {
    return this.connections.get(id) || null
  }

  /**
   * 关闭连接
   * @param {string} id 连接ID
   */
  close(id) {
    const client = this.connections.get(id)
    if (client) {
      client.close()
      this.connections.delete(id)
    }
  }

  /**
   * 关闭所有连接
   */
  closeAll() {
    this.connections.forEach((client, id) => {
      client.close()
    })
    this.connections.clear()
  }

  /**
   * 获取所有连接状态
   * @returns {Object} 连接状态映射
   */
  getAllStatus() {
    const status = {}
    this.connections.forEach((client, id) => {
      status[id] = client.getStatus()
    })
    return status
  }
}

// 全局WebSocket管理器实例
export const wsManager = new WebSocketManager()

// 兼容性处理
if (typeof window !== 'undefined') {
  window.wsManager = wsManager
}