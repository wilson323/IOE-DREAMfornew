/**
 * WebSocket 实时数据推送服务
 *
 * 提供统一的WebSocket连接管理、消息推送、自动重连等功能
 * 用于访客模块实时数据更新、在线访客监控等场景
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { message, notification } from 'ant-design-vue';

/**
 * WebSocket 连接状态枚举
 */
export const WebSocketStatus = {
  CONNECTING: 0,    // 连接中
  OPEN: 1,         // 已连接
  CLOSING: 2,      // 关闭中
  CLOSED: 3,       // 已关闭
};

/**
 * WebSocket 服务类
 */
class WebSocketService {
  constructor() {
    this.ws = null;
    this.url = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000; // 3秒后重连
    this.heartbeatInterval = 30000; // 30秒心跳
    this.heartbeatTimer = null;
    this.reconnectTimer = null;
    this.listeners = new Map(); // 消息监听器
    this.statusListeners = []; // 状态监听器
    this.manualClose = false; // 是否手动关闭
    this.currentStatus = WebSocketStatus.CLOSED;
  }

  /**
   * 连接 WebSocket
   * @param {string} url - WebSocket服务器地址
   * @param {Object} options - 配置选项
   * @param {number} options.maxReconnectAttempts - 最大重连次数，默认5次
   * @param {number} options.reconnectInterval - 重连间隔（毫秒），默认3000ms
   * @param {number} options.heartbeatInterval - 心跳间隔（毫秒），默认30000ms
   * @param {Function} options.onMessage - 消息回调
   * @param {Function} options.onStatusChange - 状态变化回调
   */
  connect(url, options = {}) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.warn('[WebSocket] 已连接，无需重复连接');
      return;
    }

    this.url = url;
    this.manualClose = false;
    this.maxReconnectAttempts = options.maxReconnectAttempts || 5;
    this.reconnectInterval = options.reconnectInterval || 3000;
    this.heartbeatInterval = options.heartbeatInterval || 30000;

    if (options.onMessage) {
      this.on('message', options.onMessage);
    }

    if (options.onStatusChange) {
      this.onStatusChange(options.onStatusChange);
    }

    this._createConnection();
  }

  /**
   * 创建 WebSocket 连接
   * @private
   */
  _createConnection() {
    try {
      this.updateStatus(WebSocketStatus.CONNECTING);

      this.ws = new WebSocket(this.url);

      this.ws.onopen = () => {
        console.log('[WebSocket] 连接成功');
        this.updateStatus(WebSocketStatus.OPEN);
        this.reconnectAttempts = 0;
        this.startHeartbeat();

        notification.success({
          message: '实时数据已连接',
          description: 'WebSocket连接成功，将实时推送最新数据',
          duration: 2,
        });
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          console.log('[WebSocket] 收到消息:', data);

          // 心跳响应
          if (data.type === 'pong') {
            return;
          }

          // 触发消息监听器
          this.emit('message', data);

          // 触发特定类型的监听器
          if (data.type) {
            this.emit(data.type, data);
          }
        } catch (error) {
          console.error('[WebSocket] 消息解析错误:', error);
        }
      };

      this.ws.onerror = (error) => {
        console.error('[WebSocket] 连接错误:', error);
        message.error('WebSocket连接错误');
      };

      this.ws.onclose = (event) => {
        console.log('[WebSocket] 连接关闭:', event.code, event.reason);
        this.updateStatus(WebSocketStatus.CLOSED);
        this.stopHeartbeat();

        // 非手动关闭则尝试重连
        if (!this.manualClose) {
          this.attemptReconnect();
        }
      };
    } catch (error) {
      console.error('[WebSocket] 创建连接失败:', error);
      this.attemptReconnect();
    }
  }

  /**
   * 尝试重连
   * @private
   */
  attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[WebSocket] 达到最大重连次数，停止重连');
      notification.error({
        message: '实时数据连接失败',
        description: '无法连接到服务器，请刷新页面重试',
        duration: 0,
      });
      return;
    }

    this.reconnectAttempts++;
    console.log(`[WebSocket] 第 ${this.reconnectAttempts} 次尝试重连...`);

    notification.warning({
      message: '实时数据连接中断',
      description: `正在尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`,
      duration: 3,
    });

    this.reconnectTimer = setTimeout(() => {
      this._createConnection();
    }, this.reconnectInterval * this.reconnectAttempts); // 逐渐延长重连间隔
  }

  /**
   * 启动心跳
   * @private
   */
  startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send({ type: 'ping' });
      }
    }, this.heartbeatInterval);
  }

  /**
   * 停止心跳
   * @private
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 发送消息
   * @param {Object} data - 要发送的数据
   */
  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message = JSON.stringify(data);
      this.ws.send(message);
      console.log('[WebSocket] 发送消息:', data);
    } else {
      console.warn('[WebSocket] 未连接，无法发送消息');
      message.warning('WebSocket未连接，无法发送消息');
    }
  }

  /**
   * 订阅特定类型的消息
   * @param {string} type - 消息类型
   * @param {Function} callback - 回调函数
   */
  on(type, callback) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, []);
    }
    this.listeners.get(type).push(callback);
  }

  /**
   * 取消订阅
   * @param {string} type - 消息类型
   * @param {Function} callback - 回调函数
   */
  off(type, callback) {
    if (this.listeners.has(type)) {
      const callbacks = this.listeners.get(type);
      const index = callbacks.indexOf(callback);
      if (index > -1) {
        callbacks.splice(index, 1);
      }
    }
  }

  /**
   * 触发事件
   * @private
   * @param {string} type - 事件类型
   * @param {Object} data - 事件数据
   */
  emit(type, data) {
    if (this.listeners.has(type)) {
      this.listeners.get(type).forEach(callback => {
        try {
          callback(data);
        } catch (error) {
          console.error(`[WebSocket] 监听器执行错误 (${type}):`, error);
        }
      });
    }
  }

  /**
   * 监听状态变化
   * @param {Function} callback - 状态变化回调
   */
  onStatusChange(callback) {
    this.statusListeners.push(callback);
    // 立即回调当前状态
    callback(this.currentStatus);
  }

  /**
   * 更新连接状态
   * @private
   * @param {number} status - 新状态
   */
  updateStatus(status) {
    this.currentStatus = status;
    this.statusListeners.forEach(callback => {
      try {
        callback(status);
      } catch (error) {
        console.error('[WebSocket] 状态监听器执行错误:', error);
      }
    });
  }

  /**
   * 获取当前连接状态
   * @returns {number} WebSocket状态
   */
  getStatus() {
    return this.currentStatus;
  }

  /**
   * 是否已连接
   * @returns {boolean}
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN;
  }

  /**
   * 手动关闭连接
   */
  close() {
    this.manualClose = true;
    this.stopHeartbeat();

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }

    this.listeners.clear();
    this.statusListeners = [];
    this.updateStatus(WebSocketStatus.CLOSED);

    console.log('[WebSocket] 手动关闭连接');
  }

  /**
   * 订阅访客数据推送
   * @param {Function} callback - 数据更新回调
   */
  subscribeVisitorData(callback) {
    this.on('visitor-stats', callback);
    this.send({ type: 'subscribe', topic: 'visitor-stats' });
  }

  /**
   * 取消订阅访客数据
   * @param {Function} callback - 数据更新回调
   */
  unsubscribeVisitorData(callback) {
    this.off('visitor-stats', callback);
    this.send({ type: 'unsubscribe', topic: 'visitor-stats' });
  }

  /**
   * 订阅在线访客列表
   * @param {Function} callback - 在线访客回调
   */
  subscribeCurrentVisitors(callback) {
    this.on('current-visitors', callback);
    this.send({ type: 'subscribe', topic: 'current-visitors' });
  }

  /**
   * 取消订阅在线访客列表
   * @param {Function} callback - 在线访客回调
   */
  unsubscribeCurrentVisitors(callback) {
    this.off('current-visitors', callback);
    this.send({ type: 'unsubscribe', topic: 'current-visitors' });
  }

  /**
   * 订阅新访客通知
   * @param {Function} callback - 新访客回调
   */
  subscribeNewVisitor(callback) {
    this.on('new-visitor', callback);
    this.send({ type: 'subscribe', topic: 'new-visitor' });
  }

  /**
   * 取消订阅新访客通知
   * @param {Function} callback - 新访客回调
   */
  unsubscribeNewVisitor(callback) {
    this.off('new-visitor', callback);
    this.send({ type: 'unsubscribe', topic: 'new-visitor' });
  }
}

// 创建全局单例
const websocketService = new WebSocketService();

export default websocketService;
