/*
 * 门禁实时监控Store
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { accessMonitorApi } from '/@/api/business/access/monitor-api';

export const useAccessMonitorStore = defineStore('accessMonitor', {
  state: () => ({
    // 实时设备状态
    realTimeDeviceStatus: [],

    // 实时通行数据
    realTimeAccessData: [],

    // 告警信息
    alerts: [],
    unprocessedAlertCount: 0,
    alertLoading: false,

    // 实时统计数据
    realTimeStats: null,

    // WebSocket连接
    websocket: null,
    wsConnected: false,
    wsReconnectTimer: null,
    wsHeartbeatTimer: null,

    // 通行趋势数据
    accessTrend: [],

    // 区域热力图数据
    areaHeatmap: [],

    // 设备在线率统计
    deviceOnlineRate: [],

    // 监控配置
    monitorConfig: {
      enableRealTimeMonitor: true,
      refreshInterval: 30000, // 30秒
      alertAutoRefresh: true,
      maxDataPoints: 100, // 最大数据点数
    },

    // 监控日志
    monitorLogs: [],

    // 监控状态
    monitorStatus: {
      isConnected: false,
      lastUpdateTime: null,
      dataUpdateTime: null,
      errorCount: 0,
    },
  }),

  getters: {
    /**
     * 最新告警数量
     */
    latestAlertCount(state) {
      return state.alerts.filter(alert => alert.status === 'new').length;
    },

    /**
     * 高危告警数量
     */
    criticalAlertCount(state) {
      return state.alerts.filter(alert =>
        alert.level === 'critical' && alert.status !== 'resolved'
      ).length;
    },

    /**
     * 系统健康状态
     */
    systemHealth(state) {
      if (state.criticalAlertCount > 0) return 'error';
      if (state.latestAlertCount > 5) return 'warning';
      return 'good';
    },

    /**
     * 今日通行统计
     */
    todayAccessStats(state) {
      if (!state.realTimeStats) return null;
      return {
        totalCount: state.realTimeStats.todayAccessCount || 0,
        successCount: state.realTimeStats.todaySuccessCount || 0,
        failedCount: state.realTimeStats.todayFailedCount || 0,
        abnormalCount: state.realTimeStats.todayAbnormalCount || 0,
      };
    },

    /**
     * 获取告警统计
     */
    alertStats(state) {
      const stats = {
        total: state.alerts.length,
        new: 0,
        processing: 0,
        resolved: 0,
        ignored: 0,
      };

      state.alerts.forEach(alert => {
        stats[alert.status] = (stats[alert.status] || 0) + 1;
      });

      return stats;
    },

    /**
     * 获取告警级别统计
     */
    alertLevelStats(state) {
      const stats = { low: 0, medium: 0, high: 0, critical: 0 };
      state.alerts.forEach(alert => {
        stats[alert.level] = (stats[alert.level] || 0) + 1;
      });
      return stats;
    },
  },

  actions: {
    /**
     * 初始化WebSocket连接
     */
    initWebSocket() {
      if (this.websocket) {
        this.closeWebSocket();
      }

      try {
        const wsUrl = accessMonitorApi.getWebSocketUrl();
        this.websocket = new WebSocket(wsUrl);

        this.websocket.onopen = () => {
          console.log('门禁监控WebSocket连接成功');
          this.wsConnected = true;
          this.monitorStatus.isConnected = true;
          this.monitorStatus.lastUpdateTime = new Date().toISOString();
          this.startHeartbeat();
        };

        this.websocket.onmessage = (event) => {
          this.handleWebSocketMessage(event);
        };

        this.websocket.onclose = () => {
          console.log('门禁监控WebSocket连接关闭');
          this.wsConnected = false;
          this.monitorStatus.isConnected = false;
          this.stopHeartbeat();
          this.startReconnect();
        };

        this.websocket.onerror = (error) => {
          console.error('门禁监控WebSocket连接错误:', error);
          this.monitorStatus.errorCount++;
        };

      } catch (error) {
        console.error('初始化WebSocket连接失败:', error);
        this.startReconnect();
      }
    },

    /**
     * 处理WebSocket消息
     */
    handleWebSocketMessage(event) {
      try {
        const data = JSON.parse(event.data);
        this.monitorStatus.dataUpdateTime = new Date().toISOString();

        switch (data.type) {
          case 'DEVICE_STATUS':
            this.updateDeviceStatus(data.data);
            break;
          case 'ACCESS_DATA':
            this.addRealTimeAccessData(data.data);
            break;
          case 'ALERT':
            this.addAlert(data.data);
            break;
          case 'STATS':
            this.updateRealTimeStats(data.data);
            break;
          case 'HEARTBEAT':
            this.handleHeartbeat(data.data);
            break;
          default:
            console.log('未知的WebSocket消息类型:', data.type);
        }
      } catch (error) {
        console.error('处理WebSocket消息失败:', error);
      }
    },

    /**
     * 更新设备状态
     */
    updateDeviceStatus(deviceStatus) {
      const index = this.realTimeDeviceStatus.findIndex(
        d => d.deviceId === deviceStatus.deviceId
      );
      if (index >= 0) {
        this.realTimeDeviceStatus[index] = deviceStatus;
      } else {
        this.realTimeDeviceStatus.push(deviceStatus);
      }

      // 限制最大数据量
      if (this.realTimeDeviceStatus.length > 1000) {
        this.realTimeDeviceStatus = this.realTimeDeviceStatus.slice(-500);
      }
    },

    /**
     * 添加实时通行数据
     */
    addRealTimeAccessData(accessData) {
      this.realTimeAccessData.unshift(accessData);

      // 限制最大数据量
      if (this.realTimeAccessData.length > this.monitorConfig.maxDataPoints) {
        this.realTimeAccessData = this.realTimeAccessData.slice(
          0,
          this.monitorConfig.maxDataPoints
        );
      }
    },

    /**
     * 添加告警
     */
    addAlert(alertData) {
      this.alerts.unshift(alertData);
      if (alertData.status === 'new') {
        this.unprocessedAlertCount++;
      }

      // 限制最大数据量
      if (this.alerts.length > 1000) {
        this.alerts = this.alerts.slice(0, 500);
      }
    },

    /**
     * 更新实时统计
     */
    updateRealTimeStats(stats) {
      this.realTimeStats = stats;
    },

    /**
     * 处理心跳
     */
    handleHeartbeat(data) {
      // 可以在这里处理心跳响应
      console.log('收到WebSocket心跳:', data);
    },

    /**
     * 开始心跳
     */
    startHeartbeat() {
      this.stopHeartbeat();
      this.wsHeartbeatTimer = setInterval(() => {
        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
          this.websocket.send(JSON.stringify({
            type: 'HEARTBEAT',
            timestamp: Date.now(),
          }));
        }
      }, 30000); // 30秒心跳
    },

    /**
     * 停止心跳
     */
    stopHeartbeat() {
      if (this.wsHeartbeatTimer) {
        clearInterval(this.wsHeartbeatTimer);
        this.wsHeartbeatTimer = null;
      }
    },

    /**
     * 开始重连
     */
    startReconnect() {
      this.stopReconnect();
      this.wsReconnectTimer = setTimeout(() => {
        console.log('尝试重新连接WebSocket...');
        this.initWebSocket();
      }, 5000); // 5秒后重连
    },

    /**
     * 停止重连
     */
    stopReconnect() {
      if (this.wsReconnectTimer) {
        clearTimeout(this.wsReconnectTimer);
        this.wsReconnectTimer = null;
      }
    },

    /**
     * 关闭WebSocket连接
     */
    closeWebSocket() {
      this.stopHeartbeat();
      this.stopReconnect();

      if (this.websocket) {
        this.websocket.close();
        this.websocket = null;
      }

      this.wsConnected = false;
      this.monitorStatus.isConnected = false;
    },

    /**
     * 获取实时设备状态
     */
    async fetchRealTimeDeviceStatus() {
      try {
        const response = await accessMonitorApi.getRealTimeDeviceStatus();
        if (response.code === 1) {
          this.realTimeDeviceStatus = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取实时设备状态失败:', error);
        return [];
      }
    },

    /**
     * 获取实时通行数据
     */
    async fetchRealTimeAccessData(params = {}) {
      try {
        const response = await accessMonitorApi.getRealTimeAccessData(params);
        if (response.code === 1) {
          this.realTimeAccessData = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取实时通行数据失败:', error);
        return [];
      }
    },

    /**
     * 获取告警信息
     */
    async fetchAlerts(params = {}) {
      this.alertLoading = true;
      try {
        const response = await accessMonitorApi.getAlerts(params);
        if (response.code === 1) {
          this.alerts = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取告警信息失败:', error);
        return [];
      } finally {
        this.alertLoading = false;
      }
    },

    /**
     * 获取未处理告警数量
     */
    async fetchUnprocessedAlertCount() {
      try {
        const response = await accessMonitorApi.getUnprocessedAlertCount();
        if (response.code === 1) {
          this.unprocessedAlertCount = response.data || 0;
          return response.data;
        }
      } catch (error) {
        console.error('获取未处理告警数量失败:', error);
        return 0;
      }
    },

    /**
     * 处理告警
     */
    async handleAlert(alertId, handleResult) {
      try {
        const response = await accessMonitorApi.handleAlert(alertId, handleResult);
        if (response.code === 1) {
          message.success('告警处理成功');
          await this.fetchAlerts();
          await this.fetchUnprocessedAlertCount();
          return true;
        } else {
          message.error(response.msg || '告警处理失败');
          return false;
        }
      } catch (error) {
        console.error('告警处理失败:', error);
        message.error('告警处理失败');
        return false;
      }
    },

    /**
     * 批量处理告警
     */
    async batchHandleAlerts(params) {
      try {
        const response = await accessMonitorApi.batchHandleAlerts(params);
        if (response.code === 1) {
          message.success('批量处理告警成功');
          await this.fetchAlerts();
          await this.fetchUnprocessedAlertCount();
          return true;
        } else {
          message.error(response.msg || '批量处理告警失败');
          return false;
        }
      } catch (error) {
        console.error('批量处理告警失败:', error);
        message.error('批量处理告警失败');
        return false;
      }
    },

    /**
     * 获取实时统计数据
     */
    async fetchRealTimeStats() {
      try {
        const response = await accessMonitorApi.getRealTimeStats();
        if (response.code === 1) {
          this.realTimeStats = response.data;
          return response.data;
        }
      } catch (error) {
        console.error('获取实时统计失败:', error);
        return null;
      }
    },

    /**
     * 获取通行趋势数据
     */
    async fetchAccessTrend(params) {
      try {
        const response = await accessMonitorApi.getAccessTrend(params);
        if (response.code === 1) {
          this.accessTrend = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取通行趋势失败:', error);
        return [];
      }
    },

    /**
     * 获取区域热力图数据
     */
    async fetchAreaHeatmap(params) {
      try {
        const response = await accessMonitorApi.getAreaHeatmap(params);
        if (response.code === 1) {
          this.areaHeatmap = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取区域热力图失败:', error);
        return [];
      }
    },

    /**
     * 更新监控配置
     */
    updateMonitorConfig(config) {
      this.monitorConfig = { ...this.monitorConfig, ...config };
    },

    /**
     * 清空监控数据
     */
    clearMonitorData() {
      this.realTimeDeviceStatus = [];
      this.realTimeAccessData = [];
      this.alerts = [];
      this.accessTrend = [];
      this.areaHeatmap = [];
      this.deviceOnlineRate = [];
      this.monitorLogs = [];
    },

    /**
     * 重置监控状态
     */
    resetMonitorStatus() {
      this.monitorStatus = {
        isConnected: false,
        lastUpdateTime: null,
        dataUpdateTime: null,
        errorCount: 0,
      };
    },
  },
});