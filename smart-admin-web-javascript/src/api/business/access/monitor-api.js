/*
 * 门禁管理-实时监控API接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, postEncryptRequest } from '/@/lib/axios';

export const accessMonitorApi = {
  /**
   * 获取实时设备状态
   */
  getRealTimeDeviceStatus: () => {
    return getRequest('/access/monitor/device/status');
  },

  /**
   * 获取实时通行数据
   */
  getRealTimeAccessData: (params = {}) => {
    return postRequest('/access/monitor/access/data', params);
  },

  /**
   * 获取告警信息
   */
  getAlerts: (params = {}) => {
    return postRequest('/access/monitor/alerts', params);
  },

  /**
   * 获取未处理告警数量
   */
  getUnprocessedAlertCount: () => {
    return getRequest('/access/monitor/alerts/unprocessed/count');
  },

  /**
   * 处理告警
   */
  handleAlert: (alertId, handleResult) => {
    return postRequest('/access/monitor/alert/handle', { alertId, handleResult });
  },

  /**
   * 批量处理告警
   */
  batchHandleAlerts: (params) => {
    return postRequest('/access/monitor/alerts/batch/handle', params);
  },

  /**
   * 获取实时统计数据
   */
  getRealTimeStats: () => {
    return getRequest('/access/monitor/stats');
  },

  /**
   * 获取设备在线率统计
   */
  getDeviceOnlineRate: (params = {}) => {
    return postRequest('/access/monitor/device/online/rate', params);
  },

  /**
   * 获取通行趋势数据
   */
  getAccessTrend: (params) => {
    return postRequest('/access/monitor/access/trend', params);
  },

  /**
   * 获取区域通行热力图数据
   */
  getAreaHeatmap: (params) => {
    return postRequest('/access/monitor/area/heatmap', params);
  },

  /**
   * WebSocket连接地址
   */
  getWebSocketUrl: () => {
    return '/ws/access/monitor';
  },

  /**
   * 获取设备详细监控数据
   */
  getDeviceMonitorDetail: (deviceId) => {
    return getRequest(`/access/monitor/device/detail/${deviceId}`);
  },

  /**
   * 获取实时告警配置
   */
  getRealTimeAlertConfig: () => {
    return getRequest('/access/monitor/alert/config');
  },

  /**
   * 更新实时告警配置
   */
  updateRealTimeAlertConfig: (params) => {
    return postRequest('/access/monitor/alert/config/update', params);
  },

  /**
   * 获取监控日志
   */
  getMonitorLogs: (params) => {
    return postRequest('/access/monitor/logs', params);
  }
};