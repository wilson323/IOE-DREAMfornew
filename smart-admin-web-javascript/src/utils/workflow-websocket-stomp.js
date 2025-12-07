/**
 * 工作流WebSocket实时通信工具（STOMP协议版本）
 * 使用STOMP协议提供新任务通知、任务状态变更、流程实例状态变更等实时通信功能
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 * 
 * 注意：此文件使用STOMP协议，需要安装以下依赖：
 * npm install @stomp/stompjs sockjs-client
 */

// import { Client } from '@stomp/stompjs';
// import SockJS from 'sockjs-client';
import { message } from 'ant-design-vue';

/**
 * WebSocket连接类（STOMP协议）
 */
class WorkflowWebSocketStomp {
  constructor() {
    this.client = null;
    this.url = null;
    this.token = null;
    this.reconnectTimer = null;
    this.heartbeatTimer = null;
    this.reconnectCount = 0;
    this.maxReconnectCount = 3;
    this.reconnectInterval = 3000; // 3秒
    this.heartbeatInterval = 30000; // 30秒
    this.listeners = new Map();
    this.isManualClose = false;
    this.subscriptions = new Map(); // STOMP订阅列表
  }

  /**
   * 初始化WebSocket连接（STOMP协议）
   * @param {String} url - WebSocket服务器地址
   * @param {String} token - 认证令牌
   */
  connect(url, token) {
    if (this.client && this.client.connected) {
      console.log('WebSocket已连接');
      return;
    }

    this.url = url;
    this.token = token;
    this.isManualClose = false;

    try {
      // 动态导入STOMP客户端（如果已安装）
      this.initStompClient(url, token);
    } catch (err) {
      console.error('WebSocket连接失败:', err);
      console.warn('请先安装STOMP客户端依赖: npm install @stomp/stompjs sockjs-client');
      this.triggerEvent('error', { error: err.message });
      this.scheduleReconnect();
    }
  }

  /**
   * 初始化STOMP客户端
   * @param {String} url - WebSocket服务器地址
   * @param {String} token - 认证令牌
   */
  async initStompClient(url, token) {
    try {
      // 动态导入STOMP客户端
      const { Client } = await import('@stomp/stompjs');
      const SockJS = (await import('sockjs-client')).default;

      this.client = new Client({
        // 使用SockJS连接（兼容性更好）
        webSocketFactory: () => new SockJS(url),
        // 连接头（认证信息）
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        // 心跳间隔
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        // 连接成功回调
        onConnect: (frame) => {
          console.log('WebSocket连接已建立', frame);
          this.reconnectCount = 0;
          this.startHeartbeat();
          this.subscribeTopics();
          this.triggerEvent('connected', {});
        },
        // 连接错误回调
        onStompError: (frame) => {
          console.error('STOMP错误:', frame);
          this.triggerEvent('error', { error: frame.headers['message'] });
        },
        // WebSocket错误回调
        onWebSocketError: (event) => {
          console.error('WebSocket错误:', event);
          this.triggerEvent('error', { error: event });
        },
        // 连接关闭回调
        onDisconnect: () => {
          console.log('WebSocket连接已关闭');
          this.clearTimers();
          this.clearSubscriptions();
          this.triggerEvent('disconnected', {});

          // 如果不是手动关闭，则尝试重连
          if (!this.isManualClose) {
            this.scheduleReconnect();
          }
        },
      });

      // 激活连接
      this.client.activate();
    } catch (importError) {
      // 如果STOMP客户端未安装，使用原生WebSocket降级方案
      console.warn('STOMP客户端未安装，使用原生WebSocket降级方案:', importError);
      this.initNativeWebSocket(url, token);
    }
  }

  /**
   * 初始化原生WebSocket（降级方案）
   * @param {String} url - WebSocket服务器地址
   * @param {String} token - 认证令牌
   */
  initNativeWebSocket(url, token) {
    try {
      const wsUrl = `${url}?token=${token}`;
      const ws = new WebSocket(wsUrl.replace('http', 'ws').replace('https', 'wss'));

      ws.onopen = () => {
        console.log('原生WebSocket连接已建立');
        this.reconnectCount = 0;
        this.startHeartbeat();
        this.triggerEvent('connected', {});
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.type === 'PONG') {
            return;
          }
          this.triggerEvent(data.type, data.data);
        } catch (err) {
          console.error('解析WebSocket消息失败:', err);
        }
      };

      ws.onerror = (event) => {
        console.error('WebSocket错误:', event);
        this.triggerEvent('error', { error: event });
      };

      ws.onclose = () => {
        console.log('WebSocket连接已关闭');
        this.clearTimers();
        this.triggerEvent('disconnected', {});

        if (!this.isManualClose) {
          this.scheduleReconnect();
        }
      };

      this.ws = ws;
    } catch (err) {
      console.error('初始化原生WebSocket失败:', err);
      this.triggerEvent('error', { error: err.message });
    }
  }

  /**
   * 订阅主题
   */
  subscribeTopics() {
    if (!this.client || !this.client.connected) {
      return;
    }

    try {
      // 订阅新任务通知（用户特定）
      const newTaskSubscription = this.client.subscribe(
        `/user/queue/workflow/tasks`,
        (message) => {
          const data = JSON.parse(message.body);
          if (data.type === 'NEW_TASK') {
            this.handleNewTask(data.data);
          } else if (data.type === 'TASK_STATUS_CHANGED') {
            this.handleTaskStatusChanged(data.data);
          }
        }
      );
      this.subscriptions.set('tasks', newTaskSubscription);

      // 订阅流程实例状态变更（用户特定）
      const instanceSubscription = this.client.subscribe(
        `/user/queue/workflow/instances`,
        (message) => {
          const data = JSON.parse(message.body);
          if (data.type === 'INSTANCE_STATUS_CHANGED') {
            this.handleInstanceStatusChanged(data.data);
          }
        }
      );
      this.subscriptions.set('instances', instanceSubscription);

      // 订阅广播消息（可选）
      const broadcastSubscription = this.client.subscribe(
        `/topic/workflow/broadcast`,
        (message) => {
          const data = JSON.parse(message.body);
          this.triggerEvent(data.type, data.data);
        }
      );
      this.subscriptions.set('broadcast', broadcastSubscription);
    } catch (err) {
      console.error('订阅主题失败:', err);
    }
  }

  /**
   * 关闭WebSocket连接
   */
  disconnect() {
    this.isManualClose = true;
    this.clearTimers();
    this.clearSubscriptions();

    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }

    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  /**
   * 发送消息
   * @param {String} destination - 目标地址
   * @param {Object} body - 消息体
   */
  send(destination, body) {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
      });
      return true;
    } else if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({ destination, ...body }));
      return true;
    } else {
      console.warn('WebSocket未连接，无法发送消息');
      return false;
    }
  }

  /**
   * 订阅事件
   * @param {String} eventType - 事件类型
   * @param {Function} callback - 回调函数
   */
  on(eventType, callback) {
    if (!this.listeners.has(eventType)) {
      this.listeners.set(eventType, []);
    }
    this.listeners.get(eventType).push(callback);
  }

  /**
   * 取消订阅事件
   * @param {String} eventType - 事件类型
   * @param {Function} callback - 回调函数（可选）
   */
  off(eventType, callback) {
    if (!this.listeners.has(eventType)) {
      return;
    }

    if (callback) {
      const callbacks = this.listeners.get(eventType);
      const index = callbacks.indexOf(callback);
      if (index > -1) {
        callbacks.splice(index, 1);
      }
    } else {
      this.listeners.delete(eventType);
    }
  }

  /**
   * 触发事件
   * @param {String} eventType - 事件类型
   * @param {Object} data - 事件数据
   */
  triggerEvent(eventType, data) {
    const callbacks = this.listeners.get(eventType);
    if (callbacks && callbacks.length > 0) {
      callbacks.forEach((callback) => {
        try {
          callback(data);
        } catch (err) {
          console.error(`执行事件回调失败 [${eventType}]:`, err);
        }
      });
    }
  }

  /**
   * 处理新任务通知
   * @param {Object} data - 任务数据
   */
  handleNewTask(data) {
    message.info(`您有新的待办任务：${data.taskName}`);
    this.triggerEvent('new-task', data);
  }

  /**
   * 处理任务状态变更
   * @param {Object} data - 任务数据
   */
  handleTaskStatusChanged(data) {
    this.triggerEvent('task-status-changed', data);
  }

  /**
   * 处理流程实例状态变更
   * @param {Object} data - 流程实例数据
   */
  handleInstanceStatusChanged(data) {
    this.triggerEvent('instance-status-changed', data);
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.clearHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      if (this.client && this.client.connected) {
        // STOMP客户端会自动处理心跳
        // 如果需要手动发送，可以调用：
        // this.send('/app/heartbeat', {});
      } else if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'PING' }));
      }
    }, this.heartbeatInterval);
  }

  /**
   * 清除心跳定时器
   */
  clearHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 清除所有订阅
   */
  clearSubscriptions() {
    this.subscriptions.forEach((subscription) => {
      try {
        subscription.unsubscribe();
      } catch (err) {
        console.error('取消订阅失败:', err);
      }
    });
    this.subscriptions.clear();
  }

  /**
   * 安排重连
   */
  scheduleReconnect() {
    if (this.isManualClose) {
      return;
    }

    if (this.reconnectCount >= this.maxReconnectCount) {
      console.error('WebSocket重连次数已达上限');
      this.triggerEvent('reconnect-failed', {});
      return;
    }

    this.reconnectCount++;
    console.log(`将在 ${this.reconnectInterval}ms 后尝试第 ${this.reconnectCount} 次重连`);

    this.reconnectTimer = setTimeout(() => {
      if (!this.isManualClose && this.url && this.token) {
        console.log(`开始第 ${this.reconnectCount} 次重连...`);
        this.connect(this.url, this.token);
      }
    }, this.reconnectInterval);
  }

  /**
   * 清除所有定时器
   */
  clearTimers() {
    this.clearHeartbeat();
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  /**
   * 获取连接状态
   * @returns {String} 连接状态
   */
  getState() {
    if (this.client) {
      return this.client.connected ? 'OPEN' : 'CLOSED';
    }
    if (this.ws) {
      switch (this.ws.readyState) {
        case WebSocket.CONNECTING:
          return 'CONNECTING';
        case WebSocket.OPEN:
          return 'OPEN';
        case WebSocket.CLOSING:
          return 'CLOSING';
        case WebSocket.CLOSED:
          return 'CLOSED';
        default:
          return 'UNKNOWN';
      }
    }
    return 'CLOSED';
  }

  /**
   * 是否已连接
   * @returns {Boolean}
   */
  isConnected() {
    if (this.client) {
      return this.client.connected;
    }
    return this.ws && this.ws.readyState === WebSocket.OPEN;
  }
}

// 创建单例实例
const workflowWebSocketStomp = new WorkflowWebSocketStomp();

/**
 * 初始化工作流WebSocket连接（STOMP协议）
 * @param {String} url - WebSocket服务器地址
 * @param {String} token - 认证令牌
 */
export function initWorkflowWebSocketStomp(url, token) {
  workflowWebSocketStomp.connect(url, token);
  return workflowWebSocketStomp;
}

/**
 * 获取WebSocket实例（STOMP协议）
 * @returns {WorkflowWebSocketStomp}
 */
export function getWorkflowWebSocketStomp() {
  return workflowWebSocketStomp;
}

/**
 * 关闭WebSocket连接
 */
export function closeWorkflowWebSocketStomp() {
  workflowWebSocketStomp.disconnect();
}

export default workflowWebSocketStomp;

