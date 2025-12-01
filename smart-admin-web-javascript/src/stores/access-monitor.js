/*
 * 门禁监控状态管理
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { accessMonitorApi } from '@/api/business/access/monitor-api';

export const useAccessMonitorStore = defineStore('accessMonitor', {
  state: () => ({
    // WebSocket连接
    websocket: null,
    wsConnected: false,
    wsReconnectAttempts: 0,
    wsMaxReconnectAttempts: 5,
    wsReconnectInterval: 3000,

    // 实时设备状态
    realTimeDeviceStatus: [],
    deviceStatusLoading: false,

    // 实时通行数据
    realTimeAccessData: [],
    accessDataLoading: false,

    // 告警信息
    alerts: [],
    alertsLoading: false,
    unreadAlertCount: 0,

    // 实时统计
    realTimeStats: null,
    statsLoading: false,

    // 监控配置
    monitorConfig: {
      autoRefresh: true,
      refreshInterval: 5000,
      showAlerts: true,
      soundAlerts: false,
    },

    // 数据更新时间戳
    lastUpdateTime: null,

    // 缓存的数据
    statusCache: new Map(),
    accessDataCache: new Map(),
  }),

  getters: {
    // 获取在线设备数量
    onlineDeviceCount: (state) => {
      return state.realTimeDeviceStatus.filter(device => device.status === 'ONLINE').length;
    },

    // 获取离线设备数量
    offlineDeviceCount: (state) => {
      return state.realTimeDeviceStatus.filter(device => device.status === 'OFFLINE').length;
    },

    // 获取故障设备数量
    faultDeviceCount: (state) => {
      return state.realTimeDeviceStatus.filter(device => device.status === 'FAULT').length;
    },

    // 获取紧急告警数量
    criticalAlertCount: (state) => {
      return state.alerts.filter(alert => alert.level === 'CRITICAL').length;
    },

    // 获取高级告警数量
    highAlertCount: (state) => {
      return state.alerts.filter(alert => alert.level === 'HIGH').length;
    },

    // 获取今日通行统计
    todayAccessStats: (state) => {
      const today = new Date().toDateString();
      return state.realTimeAccessData.filter(access =>
        new Date(access.accessTime).toDateString() === today
      );
    },

    // 获取异常通行记录
    abnormalAccessCount: (state) => {
      return state.realTimeAccessData.filter(access =>
        access.status === 'ABNORMAL' || access.result === 'FAILED'
      ).length;
    },

    // 检查设备是否在线
    isDeviceOnline: (state) => (deviceId) => {
      const device = state.realTimeDeviceStatus.find(d => d.deviceId === deviceId);
      return device && device.status === 'ONLINE';
    },

    // 获取设备最新状态
    getDeviceLatestStatus: (state) => (deviceId) => {
      return state.realTimeDeviceStatus.find(device => device.deviceId === deviceId);
    },

    // 检查连接状态
    connectionStatus: (state) => {
      if (state.wsConnected) return 'connected';
      if (state.wsReconnectAttempts > 0) return 'reconnecting';
      return 'disconnected';
    },
  },

  actions: {
    /**
     * 初始化WebSocket连接
     */
    initWebSocket() {
      try {
        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
          return;
        }

        const wsUrl = accessMonitorApi.getWebSocketUrl();
        this.websocket = new WebSocket(wsUrl);

        this.websocket.onopen = () => {
          console.log('监控WebSocket连接成功');
          this.wsConnected = true;
          this.wsReconnectAttempts = 0;
          message.success('实时监控连接已建立');
        };

        this.websocket.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            this.handleWebSocketMessage(data);
          } catch (error) {
            console.error('WebSocket消息解析失败:', error);
          }
        };

        this.websocket.onclose = () => {
          console.log('监控WebSocket连接关闭');
          this.wsConnected = false;
          this.handleWebSocketReconnect();
        };

        this.websocket.onerror = (error) => {
          console.error('监控WebSocket连接错误:', error);
          this.wsConnected = false;
        };

      } catch (error) {
        console.error('初始化WebSocket连接失败:', error);
        message.error('实时监控连接失败');
      }
    },

    /**
     * 处理WebSocket重连
     */
    handleWebSocketReconnect() {
      if (this.wsReconnectAttempts < this.wsMaxReconnectAttempts) {
        this.wsReconnectAttempts++;
        console.log(`WebSocket重连尝试 ${this.wsReconnectAttempts}/${this.wsMaxReconnectAttempts}`);

        setTimeout(() => {
          this.initWebSocket();
        }, this.wsReconnectInterval);
      } else {
        message.error('实时监控连接失败，请刷新页面重试');
      }
    },

    /**
     * 处理WebSocket消息
     */
    handleWebSocketMessage(data) {
      const { type, payload } = data;

      switch (type) {
        case 'DEVICE_STATUS_UPDATE':
          this.handleDeviceStatusUpdate(payload);
          break;
        case 'ACCESS_DATA_UPDATE':
          this.handleAccessDataUpdate(payload);
          break;
        case 'ALERT_NOTIFICATION':
          this.handleAlertNotification(payload);
          break;
        case 'STATS_UPDATE':
          this.handleStatsUpdate(payload);
          break;
        case 'HEARTBEAT':
          this.handleHeartbeat(payload);
          break;
        default:
          console.warn('未知的WebSocket消息类型:', type);
      }

      this.lastUpdateTime = new Date();
    },

    /**
     * 处理设备状态更新
     */
    handleDeviceStatusUpdate(statusData) {
      const { deviceId, status, metrics } = statusData;

      // 更新实时设备状态
      const existingDevice = this.realTimeDeviceStatus.find(d => d.deviceId === deviceId);
      if (existingDevice) {
        Object.assign(existingDevice, statusData);
      } else {
        this.realTimeDeviceStatus.push(statusData);
      }

      // 更新缓存
      this.statusCache.set(deviceId, statusData);
    },

    /**
     * 处理通行数据更新
     */
    handleAccessDataUpdate(accessData) {
      // 将新数据添加到开头
      this.realTimeAccessData.unshift(accessData);

      // 限制数据量，保留最近1000条
      if (this.realTimeAccessData.length > 1000) {
        this.realTimeAccessData = this.realTimeAccessData.slice(0, 1000);
      }

      // 检查是否需要发送通知
      if (accessData.status === 'ABNORMAL' && this.monitorConfig.showAlerts) {
        this.handleAbnormalAccess(accessData);
      }
    },

    /**
     * 处理告警通知
     */
    handleAlertNotification(alertData) {
      this.alerts.unshift(alertData);
      this.unreadAlertCount++;

      // 限制告警数量
      if (this.alerts.length > 500) {
        this.alerts = this.alerts.slice(0, 500);
      }

      // 发送通知
      if (this.monitorConfig.showAlerts) {
        this.showAlertNotification(alertData);
      }

      // 播放声音提醒
      if (this.monitorConfig.soundAlerts && alertData.level === 'CRITICAL') {
        this.playAlertSound();
      }
    },

    /**
     * 处理统计数据更新
     */
    handleStatsUpdate(statsData) {
      this.realTimeStats = { ...this.realTimeStats, ...statsData };
    },

    /**
     * 处理心跳消息
     */
    handleHeartbeat(payload) {
      // 发送心跳响应
      if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
        this.websocket.send(JSON.stringify({
          type: 'HEARTBEAT_RESPONSE',
          timestamp: new Date().toISOString(),
        }));
      }
    },

    /**
     * 处理异常通行
     */
    handleAbnormalAccess(accessData) {
      const alertData = {
        id: `abnormal-${Date.now()}`,
        type: 'ABNORMAL_ACCESS',
        level: 'HIGH',
        title: '异常通行检测',
        message: `用户 ${accessData.userName} 在 ${accessData.deviceName} 发生异常通行`,
        deviceId: accessData.deviceId,
        userId: accessData.userId,
        timestamp: new Date().toISOString(),
      };

      this.handleAlertNotification(alertData);
    },

    /**
     * 显示告警通知
     */
    showAlertNotification(alertData) {
      const notificationConfig = {
        message: alertData.title,
        description: alertData.message,
        duration: alertData.level === 'CRITICAL' ? 0 : 4.5,
        type: this.getAlertNotificationType(alertData.level),
      };

      if (alertData.level === 'CRITICAL') {
        notificationConfig.key = alertData.id;
        message.error(notificationConfig);
      } else {
        message.warning(notificationConfig);
      }
    },

    /**
     * 播放告警声音
     */
    playAlertSound() {
      try {
        const audio = new Audio('/sounds/alert.mp3');
        audio.play().catch(error => {
          console.error('播放告警声音失败:', error);
        });
      } catch (error) {
        console.error('创建告警声音对象失败:', error);
      }
    },

    /**
     * 获取告警通知类型
     */
    getAlertNotificationType(level) {
      const typeMap = {
        CRITICAL: 'error',
        HIGH: 'warning',
        MEDIUM: 'info',
        LOW: 'info',
      };
      return typeMap[level] || 'info';
    },

    /**
     * 获取实时设备状态
     */
    async fetchRealTimeDeviceStatus() {
      try {
        this.deviceStatusLoading = true;
        const response = await accessMonitorApi.getRealTimeDeviceStatus();

        if (response.code === 200) {
          this.realTimeDeviceStatus = response.data || [];
        } else {
          message.error(response.message || '获取设备状态失败');
        }
      } catch (error) {
        console.error('获取设备状态失败:', error);
        message.error('获取设备状态失败');
      } finally {
        this.deviceStatusLoading = false;
      }
    },

    /**
     * 获取实时通行数据
     */
    async fetchRealTimeAccessData(params = {}) {
      try {
        this.accessDataLoading = true;
        const response = await accessMonitorApi.getRealTimeAccessData(params);

        if (response.code === 200) {
          this.realTimeAccessData = response.data || [];
        } else {
          message.error(response.message || '获取通行数据失败');
        }
      } catch (error) {
        console.error('获取通行数据失败:', error);
        message.error('获取通行数据失败');
      } finally {
        this.accessDataLoading = false;
      }
    },

    /**
     * 获取告警信息
     */
    async fetchAlerts(params = {}) {
      try {
        this.alertsLoading = true;
        const response = await accessMonitorApi.getAlerts(params);

        if (response.code === 200) {
          this.alerts = response.data || [];
          this.unreadAlertCount = this.alerts.filter(alert => !alert.read).length;
        } else {
          message.error(response.message || '获取告警信息失败');
        }
      } catch (error) {
        console.error('获取告警信息失败:', error);
        message.error('获取告警信息失败');
      } finally {
        this.alertsLoading = false;
      }
    },

    /**
     * 获取实时统计
     */
    async fetchRealTimeStats() {
      try {
        this.statsLoading = true;
        const response = await accessMonitorApi.getRealTimeStats();

        if (response.code === 200) {
          this.realTimeStats = response.data;
        } else {
          message.error(response.message || '获取统计数据失败');
        }
      } catch (error) {
        console.error('获取统计数据失败:', error);
        message.error('获取统计数据失败');
      } finally {
        this.statsLoading = false;
      }
    },

    /**
     * 标记告警为已读
     */
    markAlertAsRead(alertId) {
      const alert = this.alerts.find(a => a.id === alertId);
      if (alert) {
        alert.read = true;
        alert.readTime = new Date().toISOString();
        this.unreadAlertCount = Math.max(0, this.unreadAlertCount - 1);
      }
    },

    /**
     * 批量标记告警为已读
     */
    markAllAlertsAsRead() {
      this.alerts.forEach(alert => {
        alert.read = true;
        alert.readTime = new Date().toISOString();
      });
      this.unreadAlertCount = 0;
    },

    /**
     * 处理告警
     */
    async handleAlert(alertId, action, remark = '') {
      try {
        const response = await accessMonitorApi.handleAlert({
          alertId,
          action,
          remark,
        });

        if (response.code === 200) {
          message.success('告警处理成功');
          // 更新告警状态
          const alert = this.alerts.find(a => a.id === alertId);
          if (alert) {
            alert.status = 'HANDLED';
            alert.action = action;
            alert.remark = remark;
            alert.handleTime = new Date().toISOString();
          }
          return true;
        } else {
          message.error(response.message || '告警处理失败');
          return false;
        }
      } catch (error) {
        console.error('告警处理失败:', error);
        message.error('告警处理失败');
        return false;
      }
    },

    /**
     * 更新监控配置
     */
    updateMonitorConfig(config) {
      this.monitorConfig = { ...this.monitorConfig, ...config };

      // 如果关闭了WebSocket，断开连接
      if (!config.autoRefresh && this.websocket) {
        this.closeWebSocket();
      } else if (config.autoRefresh && !this.wsConnected) {
        this.initWebSocket();
      }
    },

    /**
     * 关闭WebSocket连接
     */
    closeWebSocket() {
      if (this.websocket) {
        this.websocket.close();
        this.websocket = null;
      }
      this.wsConnected = false;
      this.wsReconnectAttempts = 0;
    },

    /**
     * 清除状态数据
     */
    clearState() {
      this.closeWebSocket();
      this.realTimeDeviceStatus = [];
      this.realTimeAccessData = [];
      this.alerts = [];
      this.realTimeStats = null;
      this.unreadAlertCount = 0;
      this.statusCache.clear();
      this.accessDataCache.clear();
      this.lastUpdateTime = null;
    },
  },
});