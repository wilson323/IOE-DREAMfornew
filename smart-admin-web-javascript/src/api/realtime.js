import { request } from '@/utils/request';

/**
 * 实时数据相关API接口
 */

// WebSocket连接配置
export function getWebSocketConfig() {
  return request({
    url: '/api/realtime/config',
    method: 'get'
  });
}

// 获取设备监控数据
export function getDeviceMonitorData(params = {}) {
  return request({
    url: '/api/realtime/device/monitor',
    method: 'get',
    params
  });
}

// 获取系统监控数据
export function getSystemMonitorData(params = {}) {
  return request({
    url: '/api/realtime/system/monitor',
    method: 'get',
    params
  });
}

// 获取告警列表
export function getAlertList(params = {}) {
  return request({
    url: '/api/realtime/alert/list',
    method: 'get',
    params
  });
}

// 获取告警详情
export function getAlertDetail(alertId) {
  return request({
    url: `/api/realtime/alert/${alertId}`,
    method: 'get'
  });
}

// 标记告警为已读
export function markAlertAsRead(alertId) {
  return request({
    url: `/api/realtime/alert/${alertId}/read`,
    method: 'put'
  });
}

// 确认告警
export function acknowledgeAlert(alertId, comment) {
  return request({
    url: `/api/realtime/alert/${alertId}/acknowledge`,
    method: 'put',
    data: { comment }
  });
}

// 解决告警
export function resolveAlert(alertId, resolution) {
  return request({
    url: `/api/realtime/alert/${alertId}/resolve`,
    method: 'put',
    data: { resolution }
  });
}

// 获取消息列表
export function getMessageList(params = {}) {
  return request({
    url: '/api/realtime/message/list',
    method: 'get',
    params
  });
}

// 获取消息详情
export function getMessageDetail(messageId) {
  return request({
    url: `/api/realtime/message/${messageId}`,
    method: 'get'
  });
}

// 标记消息为已读
export function markMessageAsRead(messageId) {
  return request({
    url: `/api/realtime/message/${messageId}/read`,
    method: 'put'
  });
}

// 删除消息
export function deleteMessage(messageId) {
  return request({
    url: `/api/realtime/message/${messageId}`,
    method: 'delete'
  });
}

// 批量操作消息
export function batchOperateMessages(operation, messageIds) {
  return request({
    url: '/api/realtime/message/batch',
    method: 'post',
    data: {
      operation, // 'read', 'delete'
      messageIds
    }
  });
}

// 获取订阅列表
export function getSubscriptionList() {
  return request({
    url: '/api/realtime/subscription/list',
    method: 'get'
  });
}

// 创建订阅
export function createSubscription(data) {
  return request({
    url: '/api/realtime/subscription',
    method: 'post',
    data
  });
}

// 更新订阅
export function updateSubscription(subscriptionId, data) {
  return request({
    url: `/api/realtime/subscription/${subscriptionId}`,
    method: 'put',
    data
  });
}

// 删除订阅
export function deleteSubscription(subscriptionId) {
  return request({
    url: `/api/realtime/subscription/${subscriptionId}`,
    method: 'delete'
  });
}

// 获取实时统计数据
export function getRealtimeStatistics(params = {}) {
  return request({
    url: '/api/realtime/statistics',
    method: 'get',
    params
  });
}

// 获取图表数据
export function getChartData(params = {}) {
  return request({
    url: '/api/realtime/chart/data',
    method: 'get',
    params
  });
}

// 获取设备列表
export function getDeviceList(params = {}) {
  return request({
    url: '/api/realtime/device/list',
    method: 'get',
    params
  });
}

// 获取设备详情
export function getDeviceDetail(deviceId) {
  return request({
    url: `/api/realtime/device/${deviceId}`,
    method: 'get'
  });
}

// 控制设备
export function controlDevice(deviceId, command) {
  return request({
    url: `/api/realtime/device/${deviceId}/control`,
    method: 'post',
    data: { command }
  });
}

// 获取设备历史数据
export function getDeviceHistoryData(deviceId, params = {}) {
  return request({
    url: `/api/realtime/device/${deviceId}/history`,
    method: 'get',
    params
  });
}

// 获取消息队列状态
export function getQueueStatus() {
  return request({
    url: '/api/realtime/queue/status',
    method: 'get'
  });
}

// 获取事件日志
export function getEventLog(params = {}) {
  return request({
    url: '/api/realtime/event/log',
    method: 'get',
    params
  });
}

// 发布事件
export function publishEvent(data) {
  return request({
    url: '/api/realtime/event/publish',
    method: 'post',
    data
  });
}

// 获取连接状态
export function getConnectionStatus() {
  return request({
    url: '/api/realtime/connection/status',
    method: 'get'
  });
}

// 获取性能指标
export function getPerformanceMetrics(params = {}) {
  return request({
    url: '/api/realtime/performance/metrics',
    method: 'get',
    params
  });
}

// 获取用户在线状态
export function getUserOnlineStatus(params = {}) {
  return request({
    url: '/api/realtime/user/online',
    method: 'get',
    params
  });
}

// 获取实时通知设置
export function getNotificationSettings() {
  return request({
    url: '/api/realtime/notification/settings',
    method: 'get'
  });
}

// 更新通知设置
export function updateNotificationSettings(settings) {
  return request({
    url: '/api/realtime/notification/settings',
    method: 'put',
    data: settings
  });
}

// 测试WebSocket连接
export function testWebSocketConnection(config) {
  return request({
    url: '/api/realtime/connection/test',
    method: 'post',
    data: config
  });
}

// 获取数据导出
export function exportRealtimeData(params = {}) {
  return request({
    url: '/api/realtime/export',
    method: 'get',
    params,
    responseType: 'blob'
  });
}

// 获取实时配置选项
export function getRealtimeConfigOptions() {
  return request({
    url: '/api/realtime/config/options',
    method: 'get'
  });
}

// 更新实时配置
export function updateRealtimeConfig(config) {
  return request({
    url: '/api/realtime/config',
    method: 'put',
    data: config
  });
}

// 获取告警规则
export function getAlertRules(params = {}) {
  return request({
    url: '/api/realtime/alert/rules',
    method: 'get',
    params
  });
}

// 创建告警规则
export function createAlertRule(rule) {
  return request({
    url: '/api/realtime/alert/rule',
    method: 'post',
    data: rule
  });
}

// 更新告警规则
export function updateAlertRule(ruleId, rule) {
  return request({
    url: `/api/realtime/alert/rule/${ruleId}`,
    method: 'put',
    data: rule
  });
}

// 删除告警规则
export function deleteAlertRule(ruleId) {
  return request({
    url: `/api/realtime/alert/rule/${ruleId}`,
    method: 'delete'
  });
}

// 获取告警统计
export function getAlertStatistics(params = {}) {
  return request({
    url: '/api/realtime/alert/statistics',
    method: 'get',
    params
  });
}

// 获取系统健康状态
export function getSystemHealth() {
  return request({
    url: '/api/realtime/system/health',
    method: 'get'
  });
}

// 获取API使用统计
export function getApiUsageStatistics(params = {}) {
  return request({
    url: '/api/realtime/api/usage',
    method: 'get',
    params
  });
}

// 清理过期数据
export function cleanupExpiredData() {
  return request({
    url: '/api/realtime/cleanup',
    method: 'post'
  });
}

// 获取实时数据统计
export function getRealtimeDataStats() {
  return request({
    url: '/api/realtime/data/stats',
    method: 'get'
  });
}

// 重新加载数据
export function reloadData() {
  return request({
    url: '/api/realtime/reload',
    method: 'post'
  });
}

// 获取数据源状态
export function getDataSourceStatus() {
  return request({
    url: '/api/realtime/datasource/status',
    method: 'get'
  });
}

// 刷新数据源
export function refreshDataSource(sourceId) {
  return request({
    url: `/api/realtime/datasource/${sourceId}/refresh`,
    method: 'post'
  });
}

// 获取缓存状态
export function getCacheStatus() {
  return request({
    url: '/api/realtime/cache/status',
    method: 'get'
  });
}

// 清理缓存
export function clearCache(cacheKey) {
  return request({
    url: `/api/realtime/cache/${cacheKey}`,
    method: 'delete'
  });
}

// 获取日志级别设置
export function getLogLevelSettings() {
  return request({
    url: '/api/realtime/log/level',
    method: 'get'
  });
}

// 更新日志级别
export function updateLogLevel(logger, level) {
  return request({
    url: '/api/realtime/log/level',
    method: 'put',
    data: { logger, level }
  });
}

// 获取实时数据流的配置信息
export function getDataStreamConfig() {
  return request({
    url: '/api/realtimes/stream/config',
    method: 'get'
  });
}

// 配置数据流
export function configureDataStream(config) {
  return request({
    url: '/api/realtimes/stream/config',
    method: 'put',
    data: config
  });
}

// 启动/停止数据流
export function controlDataStream(streamId, action) {
  return request({
    url: `/api/realtimes/stream/${streamId}/${action}`,
    method: 'post'
  });
}

// 获取数据流状态
export function getDataStreamStatus(streamId) {
  return request({
    url: `/api/realtimes/stream/${streamId}/status`,
    method: 'get'
  });
}