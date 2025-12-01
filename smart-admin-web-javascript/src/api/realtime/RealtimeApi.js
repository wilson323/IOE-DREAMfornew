import SmartRequest from '/@/utils/SmartRequest';

export default class RealtimeApi {

  /**
   * 获取统计数据
   */
  static getStatistics() {
    return SmartRequest.request({
      url: '/api/realtime/statistics',
      method: 'get'
    });
  }

  /**
   * 获取队列统计
   */
  static getQueueStatistics() {
    return SmartRequest.request({
      url: '/api/realtime/queue/statistics',
      method: 'get'
    });
  }

  /**
   * 获取告警列表
   */
  static getAlerts(params) {
    return SmartRequest.request({
      url: '/api/realtime/alerts',
      method: 'get',
      params: {
        severity: params.severity,
        startTime: params.startTime,
        endTime: params.endTime,
        page: params.page,
        size: params.size
      }
    });
  }

  /**
   * 获取性能指标
   */
  static getPerformanceMetrics(params) {
    return SmartRequest.request({
      url: '/api/realtime/performance/metrics',
      method: 'get',
      params: {
        metricType: params.metricType,
        timeRange: params.timeRange,
        limit: params.limit || 100
      }
    });
  }

  /**
   * 获取在线用户列表
   */
  static getOnlineUsers() {
    return SmartRequest.request({
      url: '/api/realtime/users/online',
      method: 'get'
    });
  }

  /**
   * 获取系统信息
   */
  static getSystemInfo() {
    return SmartRequest.request({
      url: '/api/realtime/system/info',
      method: 'get'
    });
  }

  /**
   * 发送实时消息
   */
  static sendMessage(params) {
    return SmartRequest.request({
      url: '/api/realtime/message/send',
      method: 'post',
      data: {
        type: params.type,
        content: params.content,
        data: params.data,
        targetId: params.targetId,
        priority: params.priority
      }
    });
  }

  /**
   * 广播消息
   */
  static broadcastMessage(params) {
    return SmartRequest.request({
      url: '/api/realtime/message/broadcast',
      method: 'post',
      data: {
        type: params.type,
        content: params.content,
        data: params.data,
        priority: params.priority
      }
    });
  }

  /**
   * 获取历史消息
   */
  static getMessageHistory(params) {
    return SmartRequest.request({
      url: '/api/realtime/messages/history',
      method: 'get',
      params: {
        messageType: params.messageType,
        startTime: params.startTime,
        endTime: params.endTime,
        page: params.page,
        size: params.size
      }
    });
  }

  /**
   * 手动触发数据同步
   */
  static triggerSync(params) {
    return SmartRequest.request({
      url: '/api/realtime/sync/trigger',
      method: 'post',
      data: {
        syncType: params.syncType,
        entityId: params.entityId,
        data: params.data
      }
    });
  }

  /**
   * 获取同步状态
   */
  static getSyncStatus() {
    return SmartRequest.request({
      url: '/api/realtime/sync/status',
      method: 'get'
    });
  }

  /**
   * 获取缓存统计
   */
  static getCacheStatistics() {
    return SmartRequest.request({
      url: '/api/realtime/cache/statistics',
      method: 'get'
    });
  }

  /**
   * 清理缓存
   */
  static clearCache(params) {
    return SmartRequest.request({
      url: '/api/realtime/cache/clear',
      method: 'post',
      data: {
        cacheType: params.cacheType,
        key: params.key
      }
    });
  }

  /**
   * 测试通知渠道
   */
  static testNotificationChannels() {
    return SmartRequest.request({
      url: '/api/realtime/notification/test',
      method: 'post'
    });
  }

  /**
   * 获取告警统计
   */
  static getAlertStatistics() {
    return SmartRequest.request({
      url: '/api/realtime/alert/statistics',
      method: 'get'
    });
  }

  /**
   * 重置告警计数器
   */
  static resetAlertCounters() {
    return SmartRequest.request({
      url: '/api/realtime/alert/reset-counters',
      method: 'post'
    });
  }

  /**
   * 获取WebSocket连接状态
   */
  static getWebSocketStatus() {
    return SmartRequest.request({
      url: '/api/realtime/websocket/status',
      method: 'get'
    });
  }

  /**
   * 强制断开用户连接
   */
  static disconnectUser(userId) {
    return SmartRequest.request({
      url: `/api/realtime/websocket/disconnect/${userId}`,
      method: 'post'
    });
  }

  /**
   * 发送测试消息
   */
  static sendTestMessage(params) {
    return SmartRequest.request({
      url: '/api/realtime/test/message',
      method: 'post',
      data: {
        messageType: params.messageType,
        targetUserId: params.targetUserId,
        content: params.content
      }
    });
  }

  /**
   * 获取实时位置数据
   */
  static getRealtimeLocations(params) {
    return SmartRequest.request({
      url: '/api/realtime/locations',
      method: 'get',
      params: {
        userIds: params.userIds?.join(','),
        includeInactive: params.includeInactive || false,
        limit: params.limit || 50
      }
    });
  }

  /**
   * 获取实时设备状态
   */
  static getRealtimeDeviceStatus(params) {
    return SmartRequest.request({
      url: '/api/realtime/devices/status',
      method: 'get',
      params: {
        deviceIds: params.deviceIds?.join(','),
        status: params.status,
        limit: params.limit || 50
      }
    });
  }

  /**
   * 订阅实时事件
   */
  static subscribeToEvents(params) {
    return SmartRequest.request({
      url: '/api/realtime/events/subscribe',
      method: 'post',
      data: {
        eventTypes: params.eventTypes,
        filters: params.filters,
        webhookUrl: params.webhookUrl
      }
    });
  }

  /**
   * 取消订阅事件
   */
  static unsubscribeFromEvents(subscriptionId) {
    return SmartRequest.request({
      url: `/api/realtime/events/unsubscribe/${subscriptionId}`,
      method: 'delete'
    });
  }

  /**
   * 获取事件订阅列表
   */
  static getEventSubscriptions() {
    return SmartRequest.request({
      url: '/api/realtime/events/subscriptions',
      method: 'get'
    });
  }

  /**
   * 导出实时数据
   */
  static exportRealtimeData(params) {
    return SmartRequest.request({
      url: '/api/realtime/export',
      method: 'post',
      data: {
        dataType: params.dataType,
        startTime: params.startTime,
        endTime: params.endTime,
        format: params.format || 'json'
      },
      responseType: 'blob'
    });
  }

  /**
   * 获取系统健康状态
   */
  static getHealthStatus() {
    return SmartRequest.request({
      url: '/api/realtime/health',
      method: 'get'
    });
  }

  /**
   * 获取配置信息
   */
  static getConfiguration() {
    return SmartRequest.request({
      url: '/api/realtime/config',
      method: 'get'
    });
  }

  /**
   * 更新配置
   */
  static updateConfiguration(params) {
    return SmartRequest.request({
      url: '/api/realtime/config',
      method: 'put',
      data: params
    });
  }
}