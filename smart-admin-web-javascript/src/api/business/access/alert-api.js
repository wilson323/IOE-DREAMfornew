/**
 * 门禁告警管理 API
 */
import { request } from '/@/utils/request';

/**
 * 分页查询告警列表
 */
export const queryAlerts = (params) => {
  return request({
    url: '/api/v1/access/alerts',
    method: 'get',
    params,
  });
};

/**
 * 查询告警详情
 */
export const getAlertDetail = (alertId) => {
  return request({
    url: `/api/v1/access/alerts/${alertId}`,
    method: 'get',
  });
};

/**
 * 处理告警（确认/处理/忽略）
 */
export const handleAlert = (data) => {
  return request({
    url: '/api/v1/access/alerts/handle',
    method: 'put',
    data,
  });
};

/**
 * 批量确认告警
 */
export const batchConfirm = (data) => {
  return request({
    url: '/api/v1/access/alerts/batch-confirm',
    method: 'post',
    data,
  });
};

/**
 * 批量处理告警
 */
export const batchHandle = (data) => {
  return request({
    url: '/api/v1/access/alerts/batch-handle',
    method: 'post',
    params: data,
  });
};

/**
 * 获取告警统计数据
 */
export const getStatistics = () => {
  return request({
    url: '/api/v1/access/alerts/statistics',
    method: 'get',
  });
};

/**
 * 查询紧急告警
 */
export const getCriticalAlerts = (limit = 10) => {
  return request({
    url: '/api/v1/access/alerts/critical',
    method: 'get',
    params: { limit },
  });
};

/**
 * 获取告警趋势
 */
export const getAlertTrend = () => {
  return request({
    url: '/api/v1/access/alerts/trend',
    method: 'get',
  });
};

/**
 * 手动创建告警
 */
export const createAlert = (data) => {
  return request({
    url: '/api/v1/access/alerts',
    method: 'post',
    data,
  });
};

/**
 * 检测并创建告警
 */
export const detectAndCreateAlert = (data) => {
  return request({
    url: '/api/v1/access/alerts/detect',
    method: 'post',
    params: data,
  });
};

/**
 * 清理过期告警
 */
export const cleanupExpiredAlerts = (daysToKeep) => {
  return request({
    url: '/api/v1/access/alerts/cleanup',
    method: 'delete',
    params: { daysToKeep },
  });
};

/**
 * 获取设备未确认告警数量
 */
export const getUnconfirmedCount = (deviceId) => {
  return request({
    url: '/api/v1/access/alerts/unconfirmed-count',
    method: 'get',
    params: { deviceId },
  });
};

/**
 * 获取今日告警数量
 */
export const getTodayAlertCount = () => {
  return request({
    url: '/api/v1/access/alerts/today-count',
    method: 'get',
  });
};

/**
 * WebSocket健康检查
 */
export const healthCheck = () => {
  return request({
    url: '/api/v1/access/alerts/health',
    method: 'get',
  });
};

// 默认导出所有API
export default {
  queryAlerts,
  getAlertDetail,
  handleAlert,
  batchConfirm,
  batchHandle,
  getStatistics,
  getCriticalAlerts,
  getAlertTrend,
  createAlert,
  detectAndCreateAlert,
  cleanupExpiredAlerts,
  getUnconfirmedCount,
  getTodayAlertCount,
  healthCheck,
};
