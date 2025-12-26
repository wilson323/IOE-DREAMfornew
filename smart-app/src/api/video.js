/**
 * 智能视频模块 API封装
 * 移动端专用
 */

import { request } from '@/utils/request';

// ==================== 基础URL ====================

const BASE_URL = '/ivs/v1/mobile';

// ==================== 设备管理API ====================

/**
 * 获取设备列表（移动端）
 */
export function getDeviceList(params) {
  return request({
    url: `${BASE_URL}/devices`,
    method: 'GET',
    params
  });
}

/**
 * 获取设备详情
 */
export function getDeviceDetail(deviceId) {
  return request({
    url: `${BASE_URL}/device/${deviceId}`,
    method: 'GET'
  });
}

/**
 * 获取设备统计
 */
export function getDeviceStatistics() {
  return request({
    url: `${BASE_URL}/statistics`,
    method: 'GET'
  });
}

// ==================== 实时监控API ====================

/**
 * 获取监控画面（单路）
 */
export function getMonitorStream(deviceId, quality = '480p') {
  return request({
    url: `${BASE_URL}/monitor/${deviceId}`,
    method: 'GET',
    params: { quality }
  });
}

/**
 * 获取多画面（4路）
 */
export function getMultiMonitor(deviceIds, quality = '480p') {
  return request({
    url: `${BASE_URL}/multi-monitor`,
    method: 'POST',
    data: { deviceIds, quality }
  });
}

/**
 * 获取预览地址
 */
export function getPreviewUrl(deviceId, quality = '480p', protocol = 'HLS') {
  return `${BASE_URL}/preview/${deviceId}?quality=${quality}&protocol=${protocol}`;
}

// ==================== 云台控制API ====================

/**
 * 云台控制
 */
export function ptzControl(params) {
  return request({
    url: `${BASE_URL}/ptz/control`,
    method: 'POST',
    data: params
  });
}

/**
 * 获取预置位列表
 */
export function getPresetList(deviceId) {
  return request({
    url: `${BASE_URL}/preset/${deviceId}`,
    method: 'GET'
  });
}

/**
 * 调用预置位
 */
export function gotoPreset(deviceId, presetId) {
  return request({
    url: `${BASE_URL}/preset/goto`,
    method: 'POST',
    data: { deviceId, presetId }
  });
}

// ==================== 告警管理API ====================

/**
 * 获取告警概览
 */
export function getAlarmOverview() {
  return request({
    url: `${BASE_URL}/alarm/overview`,
    method: 'GET'
  });
}

/**
 * 获取活跃告警
 */
export function getActiveAlarms(params) {
  return request({
    url: `${BASE_URL}/alarm/active`,
    method: 'GET',
    params
  });
}

/**
 * 处理告警
 */
export function processAlarm(alarmId, action, feedback = '') {
  return request({
    url: `${BASE_URL}/alarm/process`,
    method: 'POST',
    data: { alarmId, action, feedback }
  });
}

/**
 * 批量处理告警
 */
export function batchProcessAlarms(alarmIds, action) {
  return request({
    url: `${BASE_URL}/alarm/batch-process`,
    method: 'POST',
    data: { alarmIds, action }
  });
}

// ==================== 视频回放API ====================

/**
 * 查询录像
 */
export function queryRecords(params) {
  return request({
    url: `${BASE_URL}/playback/query`,
    method: 'GET',
    params
  });
}

/**
 * 获取回放地址
 */
export function getPlaybackUrl(recordId) {
  return `${BASE_URL}/playback/${recordId}`;
}

/**
 * 下载录像
 */
export function downloadRecord(recordId) {
  return request({
    url: `${BASE_URL}/playback/download/${recordId}`,
    method: 'GET',
    responseType: 'arraybuffer'
  });
}

// ==================== 快捷操作API ====================

/**
 * 截图
 */
export function captureSnapshot(deviceId) {
  return request({
    url: `${BASE_URL}/action/capture`,
    method: 'POST',
    data: { deviceId }
  });
}

/**
 * 开始录像
 */
export function startRecording(deviceId) {
  return request({
    url: `${BASE_URL}/action/record/start`,
    method: 'POST',
    data: { deviceId }
  });
}

/**
 * 停止录像
 */
export function stopRecording(deviceId) {
  return request({
    url: `${BASE_URL}/action/record/stop`,
    method: 'POST',
    data: { deviceId }
  });
}

/**
 * 获取录制状态
 */
export function getRecordingStatus(deviceId) {
  return request({
    url: `${BASE_URL}/action/record/status/${deviceId}`,
    method: 'GET'
  });
}

// ==================== WebSocket管理 ====================

/**
 * WebSocket连接管理器
 */
export class WebSocketManager {
  constructor() {
    this.ws = null;
    this.url = '';
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000;
    this.listeners = {};
    this.isConnected = false;
  }

  /**
   * 连接WebSocket
   */
  connect(url) {
    this.url = url;

    this.ws = uni.connectSocket({
      url: url,
      success: () => {
        console.log('[WebSocket] 连接成功');
        this.isConnected = true;
        this.reconnectAttempts = 0;
        this.onOpen();
      },
      fail: (error) => {
        console.error('[WebSocket] 连接失败:', error);
        this.isConnected = false;
        this.onError(error);
        this.reconnect();
      }
    });

    // 监听消息
    uni.onSocketMessage((res) => {
      try {
        const data = JSON.parse(res.data);
        this.onMessage(data);
      } catch (error) {
        console.error('[WebSocket] 消息解析失败:', error);
      }
    });

    // 监听关闭
    uni.onSocketClose(() => {
      console.log('[WebSocket] 连接关闭');
      this.isConnected = false;
      this.onClose();
      this.reconnect();
    });
  }

  /**
   * 重连
   */
  reconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`[WebSocket] 重连中... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

      setTimeout(() => {
        this.connect(this.url);
      }, this.reconnectInterval);
    } else {
      console.error('[WebSocket] 达到最大重连次数，停止重连');
    }
  }

  /**
   * 订阅主题
   */
  subscribe(topic, callback) {
    if (!this.listeners[topic]) {
      this.listeners[topic] = [];
    }
    this.listeners[topic].push(callback);

    console.log('[WebSocket] 订阅主题:', topic);
  }

  /**
   * 取消订阅
   */
  unsubscribe(topic, callback) {
    if (this.listeners[topic]) {
      const index = this.listeners[topic].indexOf(callback);
      if (index > -1) {
        this.listeners[topic].splice(index, 1);
      }
    }
  }

  /**
   * 发送消息
   */
  send(data) {
    if (this.isConnected) {
      uni.sendSocketMessage({
        data: JSON.stringify(data),
        success: () => {
          console.log('[WebSocket] 消息发送成功:', data);
        },
        fail: (error) => {
          console.error('[WebSocket] 消息发送失败:', error);
        }
      });
    } else {
      console.warn('[WebSocket] 未连接，无法发送消息');
    }
  }

  /**
   * 关闭连接
   */
  close() {
    if (this.ws) {
      uni.closeSocket();
      this.ws = null;
      this.isConnected = false;
    }
  }

  /**
   * 消息处理
   */
  onOpen() {
    this.emit('connected');
  }

  onClose() {
    this.emit('disconnected');
  }

  onError(error) {
    this.emit('error', error);
  }

  onMessage(data) {
    // 根据消息类型分发到对应监听器
    if (data.type && this.listeners[data.type]) {
      this.listeners[data.type].forEach(callback => {
        callback(data);
      });
    }

    // 通用消息监听器
    if (this.listeners['message']) {
      this.listeners['message'].forEach(callback => {
        callback(data);
      });
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        callback(data);
      });
    }
  }
}

// 创建WebSocket实例
export const wsManager = new WebSocketManager();

export default {
  // 设备
  getDeviceList,
  getDeviceDetail,
  getDeviceStatistics,

  // 监控
  getMonitorStream,
  getMultiMonitor,
  getPreviewUrl,

  // 云台
  ptzControl,
  getPresetList,
  gotoPreset,

  // 告警
  getAlarmOverview,
  getActiveAlarms,
  processAlarm,
  batchProcessAlarms,

  // 回放
  queryRecords,
  getPlaybackUrl,
  downloadRecord,

  // 快捷操作
  captureSnapshot,
  startRecording,
  stopRecording,
  getRecordingStatus,

  // WebSocket
  WebSocketManager,
  wsManager
};
