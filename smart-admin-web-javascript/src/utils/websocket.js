import { ref, reactive, onUnmounted, nextTick } from 'vue';
import { message } from 'ant-design-vue';

/**
 * WebSocket客户端工具类
 * 提供连接管理、消息收发、自动重连等功能
 */
export class WebSocketClient {
  constructor(config = {}) {
    this.config = this.mergeConfig(config);
    this.ws = null;
    this.status = reactive({
      status: 'disconnected',
      reconnectCount: 0,
      lastActiveAt: null,
      connectedAt: null,
      statistics: {
        totalConnections: 0,
        totalDisconnections: 0,
        totalReconnections: 0,
        totalMessagesSent: 0,
        totalMessagesReceived: 0,
        averageConnectionDuration: 0,
        currentConnectionDuration: 0
      }
    });
    this.subscriptions = new Map();
    this.messageHandlers = new Map();
    this.eventHandlers = new Map();
    this.messageQueue = [];
    this.heartbeatTimer = null;
    this.reconnectTimer = null;
    this.connectionStartTime = null;
    this.isManualClose = false;
  }

  // 默认配置
  defaultConfig = {
    url: '',
    protocol: 'ws',
    timeout: 10000,
    heartbeatInterval: 30000,
    maxReconnectAttempts: 5,
    reconnectInterval: 3000,
    enableCompression: false,
    enableEncryption: false,
    debug: false
  };

  /**
   * 合并配置
   */
  mergeConfig(userConfig) {
    return {
      ...this.defaultConfig,
      ...userConfig
    };
  }

  /**
   * 连接WebSocket
   */
  connect() {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.log('WebSocket已连接');
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      try {
        const wsUrl = this.buildWebSocketUrl();
        this.log(`连接WebSocket: ${wsUrl}`);

        this.ws = new WebSocket(wsUrl);
        this.connectionStartTime = Date.now();
        this.isManualClose = false;

        this.ws.onopen = (event) => {
          this.handleOpen(event);
          resolve(event);
        };

        this.ws.onmessage = (event) => {
          this.handleMessage(event);
        };

        this.ws.onclose = (event) => {
          this.handleClose(event);
          if (!this.isManualClose) {
            reject(new Error('WebSocket连接关闭'));
          }
        };

        this.ws.onerror = (event) => {
          this.handleError(event);
          reject(new Error('WebSocket连接错误'));
        };

        // 设置连接超时
        setTimeout(() => {
          if (this.ws && this.ws.readyState === WebSocket.CONNECTING) {
            this.ws.close();
            reject(new Error('WebSocket连接超时'));
          }
        }, this.config.timeout);

      } catch (error) {
        reject(error);
      }
    });
  }

  /**
   * 构建WebSocket URL
   */
  buildWebSocketUrl() {
    const { url, protocol, token } = this.config;
    let wsUrl = url;

    // 替换协议
    if (protocol === 'wss' && !wsUrl.startsWith('wss://')) {
      wsUrl = wsUrl.replace(/^https?:/, 'wss:');
    } else if (protocol === 'ws' && !wsUrl.startsWith('ws://')) {
      wsUrl = wsUrl.replace(/^https?:/, 'ws:');
    }

    // 添加token参数
    if (token) {
      const separator = wsUrl.includes('?') ? '&' : '?';
      wsUrl += `${separator}token=${encodeURIComponent(token)}`;
    }

    return wsUrl;
  }

  /**
   * 处理连接打开
   */
  handleOpen(event) {
    this.log('WebSocket连接成功');
    this.status.status = 'connected';
    this.status.connectedAt = Date.now();
    this.status.lastActiveAt = Date.now();
    this.status.statistics.totalConnections++;
    this.status.reconnectCount = 0;

    // 启动心跳
    this.startHeartbeat();

    // 重新订阅之前的主题
    this.resubscribeAll();

    // 发送队列中的消息
    this.flushMessageQueue();

    // 触发事件
    this.emit('open', event);
  }

  /**
   * 处理消息接收
   */
  handleMessage(event) {
    try {
      const message = this.parseMessage(event.data);
      this.status.lastActiveAt = Date.now();
      this.status.statistics.totalMessagesReceived++;

      this.log('收到消息:', message);

      // 处理心跳响应
      if (message.type === 'heartbeat') {
        this.handleHeartbeatResponse(message);
        return;
      }

      // 触发消息事件
      this.emit('message', message);

      // 触发主题特定事件
      if (message.topic) {
        this.emit(`topic:${message.topic}`, message);
      }

      // 调用消息处理器
      this.callMessageHandlers(message);

    } catch (error) {
      console.error('处理WebSocket消息失败:', error);
      this.emit('error', error);
    }
  }

  /**
   * 解析消息
   */
  parseMessage(data) {
    if (typeof data === 'string') {
      try {
        return JSON.parse(data);
      } catch {
        return { type: 'text', payload: data };
      }
    }
    return data;
  }

  /**
   * 处理连接关闭
   */
  handleClose(event) {
    this.log('WebSocket连接关闭:', event.code, event.reason);

    // 更新统计信息
    if (this.connectionStartTime) {
      const duration = Date.now() - this.connectionStartTime;
      this.status.statistics.totalDisconnections++;
      this.updateAverageConnectionDuration(duration);
    }

    this.status.status = 'disconnected';
    this.stopHeartbeat();

    // 清理重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    // 自动重连
    if (!this.isManualClose && this.config.autoReconnect !== false) {
      this.scheduleReconnect();
    }

    this.emit('close', event);
  }

  /**
   * 处理连接错误
   */
  handleError(event) {
    this.log('WebSocket连接错误:', event);
    this.status.status = 'error';
    this.emit('error', event);
  }

  /**
   * 发送消息
   */
  send(message) {
    return new Promise((resolve, reject) => {
      if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
        // 如果未连接，将消息加入队列
        this.messageQueue.push(message);
        reject(new Error('WebSocket未连接'));
        return;
      }

      try {
        const data = typeof message === 'string' ? message : JSON.stringify(message);
        this.ws.send(data);
        this.status.statistics.totalMessagesSent++;
        this.log('发送消息:', message);
        resolve();
      } catch (error) {
        this.log('发送消息失败:', error);
        reject(error);
      }
    });
  }

  /**
   * 订阅主题
   */
  subscribe(topic, filters = [], options = {}) {
    const subscriptionId = this.generateId();
    const subscription = {
      id: subscriptionId,
      topic,
      type: 'subscribe',
      filters,
      active: true,
      subscribedAt: Date.now(),
      statistics: {
        messagesReceived: 0,
        lastReceivedAt: null,
        averageMessageSize: 0,
        subscriptionDuration: 0
      }
    };

    this.subscriptions.set(subscriptionId, subscription);

    const message = {
      type: 'subscribe',
      topic,
      subscriptionId,
      filters,
      ...options
    };

    this.send(message);
    this.emit('subscription', subscription);

    return subscriptionId;
  }

  /**
   * 取消订阅主题
   */
  unsubscribe(subscriptionId) {
    const subscription = this.subscriptions.get(subscriptionId);
    if (!subscription) {
      return false;
    }

    subscription.active = false;

    const message = {
      type: 'unsubscribe',
      topic: subscription.topic,
      subscriptionId
    };

    this.send(message);
    this.subscriptions.delete(subscriptionId);
    this.emit('unsubscription', subscriptionId);

    return true;
  }

  /**
   * 取消所有订阅
   */
  unsubscribeAll() {
    const subscriptionIds = Array.from(this.subscriptions.keys());
    subscriptionIds.forEach(id => this.unsubscribe(id));
  }

  /**
   * 重新订阅所有主题
   */
  resubscribeAll() {
    const subscriptions = Array.from(this.subscriptions.values());
    subscriptions.forEach(subscription => {
      if (subscription.active) {
        const message = {
          type: 'subscribe',
          topic: subscription.topic,
          subscriptionId: subscription.id,
          filters: subscription.filters
        };
        this.send(message);
      }
    });
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
    }

    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        const heartbeatMessage = {
          type: 'heartbeat',
          timestamp: Date.now()
        };
        this.send(heartbeatMessage);
      }
    }, this.config.heartbeatInterval);
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 处理心跳响应
   */
  handleHeartbeatResponse(message) {
    // 计算延迟
    const latency = Date.now() - message.timestamp;
    this.log('心跳延迟:', latency, 'ms');
    this.emit('heartbeat', { latency, timestamp: Date.now() });
  }

  /**
   * 安排重连
   */
  scheduleReconnect() {
    if (this.status.reconnectCount >= this.config.maxReconnectAttempts) {
      this.log('达到最大重连次数，停止重连');
      return;
    }

    this.status.reconnectCount++;
    this.status.status = 'reconnecting';

    const delay = this.config.reconnectInterval * Math.pow(2, this.status.reconnectCount - 1);

    this.log(`${delay}ms后尝试第${this.status.reconnectCount}次重连`);

    this.reconnectTimer = setTimeout(() => {
      this.connect().catch(error => {
        this.log('重连失败:', error);
      });
    }, delay);
  }

  /**
   * 手动关闭连接
   */
  close() {
    this.isManualClose = true;
    this.stopHeartbeat();

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  /**
   * 获取连接状态
   */
  getConnectionStatus() {
    return { ...this.status };
  }

  /**
   * 获取订阅列表
   */
  getSubscriptions() {
    return Array.from(this.subscriptions.values());
  }

  /**
   * 清空消息队列
   */
  flushMessageQueue() {
    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift();
      this.send(message).catch(error => {
        this.log('发送队列消息失败:', error);
      });
    }
  }

  /**
   * 添加消息处理器
   */
  addMessageHandler(topic, handler) {
    if (!this.messageHandlers.has(topic)) {
      this.messageHandlers.set(topic, new Set());
    }
    this.messageHandlers.get(topic).add(handler);
  }

  /**
   * 移除消息处理器
   */
  removeMessageHandler(topic, handler) {
    const handlers = this.messageHandlers.get(topic);
    if (handlers) {
      handlers.delete(handler);
      if (handlers.size === 0) {
        this.messageHandlers.delete(topic);
      }
    }
  }

  /**
   * 调用消息处理器
   */
  callMessageHandlers(message) {
    const handlers = this.messageHandlers.get(message.topic);
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(message);
        } catch (error) {
          console.error('消息处理器执行失败:', error);
        }
      });
    }
  }

  /**
   * 添加事件监听器
   */
  on(event, handler) {
    if (!this.eventHandlers.has(event)) {
      this.eventHandlers.set(event, new Set());
    }
    this.eventHandlers.get(event).add(handler);
  }

  /**
   * 移除事件监听器
   */
  off(event, handler) {
    const handlers = this.eventHandlers.get(event);
    if (handlers) {
      handlers.delete(handler);
      if (handlers.size === 0) {
        this.eventHandlers.delete(event);
      }
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    const handlers = this.eventHandlers.get(event);
    if (handlers) {
      nextTick(() => {
        handlers.forEach(handler => {
          try {
            handler(data);
          } catch (error) {
            console.error('事件处理器执行失败:', error);
          }
        });
      });
    }
  }

  /**
   * 更新平均连接时长
   */
  updateAverageConnectionDuration(duration) {
    const stats = this.status.statistics;
    const totalConnections = stats.totalConnections;

    if (totalConnections === 1) {
      stats.averageConnectionDuration = duration;
    } else {
      stats.averageConnectionDuration =
        ((stats.averageConnectionDuration * (totalConnections - 1)) + duration) / totalConnections;
    }
  }

  /**
   * 生成唯一ID
   */
  generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  /**
   * 日志输出
   */
  log(...args) {
    if (this.config.debug) {
      console.log('[WebSocketClient]', ...args);
    }
  }
}

/**
 * 创建WebSocket客户端实例
 */
export function createWebSocketClient(config) {
  return new WebSocketClient(config);
}

/**
 * WebSocket连接管理器（单例模式）
 */
class WebSocketManager {
  constructor() {
    this.clients = new Map();
    this.defaultClient = null;
  }

  /**
   * 创建客户端
   */
  createClient(name = 'default', config) {
    const client = new WebSocketClient(config);
    this.clients.set(name, client);

    if (!this.defaultClient) {
      this.defaultClient = client;
    }

    return client;
  }

  /**
   * 获取客户端
   */
  getClient(name = 'default') {
    return this.clients.get(name);
  }

  /**
   * 获取默认客户端
   */
  getDefaultClient() {
    return this.defaultClient;
  }

  /**
   * 关闭客户端
   */
  closeClient(name) {
    const client = this.clients.get(name);
    if (client) {
      client.close();
      this.clients.delete(name);

      if (this.defaultClient === client) {
        this.defaultClient = null;
      }
    }
  }

  /**
   * 关闭所有客户端
   */
  closeAllClients() {
    this.clients.forEach(client => client.close());
    this.clients.clear();
    this.defaultClient = null;
  }

  /**
   * 获取所有客户端状态
   */
  getAllStatus() {
    const status = {};
    this.clients.forEach((client, name) => {
      status[name] = client.getConnectionStatus();
    });
    return status;
  }
}

export const wsManager = new WebSocketManager();

/**
 * Vue3 Composable: useWebSocket
 */
export function useWebSocket(config = {}) {
  const client = ref(null);
  const status = ref({ status: 'disconnected' });
  const error = ref(null);

  // 创建客户端
  client.value = wsManager.createClient('default', config);

  // 监听状态变化
  const statusHandler = (newStatus) => {
    status.value = newStatus;
  };

  const errorHandler = (err) => {
    error.value = err;
    message.error(`WebSocket连接错误: ${err.message}`);
  };

  client.value.on('open', statusHandler);
  client.value.on('close', statusHandler);
  client.value.on('error', errorHandler);

  // 组件卸载时清理
  onUnmounted(() => {
    if (client.value) {
      client.value.off('open', statusHandler);
      client.value.off('close', statusHandler);
      client.value.off('error', errorHandler);
    }
  });

  return {
    client,
    status,
    error,
    connect: () => client.value.connect(),
    close: () => client.value.close(),
    send: (message) => client.value.send(message),
    subscribe: (topic, filters) => client.value.subscribe(topic, filters),
    unsubscribe: (id) => client.value.unsubscribe(id)
  };
}