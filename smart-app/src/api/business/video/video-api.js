/**
 * 智能视频API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, putRequest } from '@/lib/smart-request'

// 实时监控相关接口
export const monitorApi = {
  /**
   * 获取移动端实时监控画面
   * @param {Number} deviceId 设备ID
   * @param {String} streamType 流类型：MAIN-主码流，SUB-子码流
   * @param {String} resolution 分辨率：720p,480p
   * @returns {Promise}
   */
  getMobileMonitor: (deviceId, streamType = 'SUB', resolution = '720p') =>
    getRequest(`/api/mobile/v1/video/monitor/${deviceId}`, { streamType, resolution }),

  /**
   * 移动端多画面监控
   * @param {Object} data 监控请求数据
   * @param {Array} data.deviceIds 设备ID列表（最多4个）
   * @param {String} data.layout 布局模式：2x2
   * @returns {Promise}
   */
  getMultiMonitor: (data) => postRequest('/api/mobile/v1/video/monitor/multi', data),

  /**
   * 启动实时监控
   * @param {Number} deviceId 设备ID
   * @param {String} streamType 流类型
   * @returns {Promise}
   */
  startMonitor: (deviceId, streamType = 'SUB') =>
    postRequest(`/api/v1/video/monitor/${deviceId}/start`, null, { streamType }),

  /**
   * 停止实时监控
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  stopMonitor: (deviceId) =>
    postRequest(`/api/v1/video/monitor/${deviceId}/stop`),

  /**
   * 批量启动监控
   * @param {Array} deviceIds 设备ID列表
   * @param {String} streamType 流类型
   * @returns {Promise}
   */
  batchStartMonitor: (deviceIds, streamType = 'SUB') =>
    postRequest('/api/v1/video/monitor/batch/start', { deviceIds, streamType }),

  /**
   * 批量停止监控
   * @param {Array} deviceIds 设备ID列表
   * @returns {Promise}
   */
  batchStopMonitor: (deviceIds) =>
    postRequest('/api/v1/video/monitor/batch/stop', deviceIds)
}

// 云台控制相关接口
export const ptzApi = {
  /**
   * 移动端云台控制
   * @param {Number} deviceId 设备ID
   * @param {Object} controlData 控制数据
   * @param {String} controlData.action 操作：UP/DOWN/LEFT/RIGHT/ZOOM_IN/ZOOM_OUT
   * @param {Number} controlData.speed 速度：1-100
   * @returns {Promise}
   */
  mobilePTZControl: (deviceId, controlData) =>
    postRequest(`/api/mobile/v1/video/ptz/${deviceId}`, controlData),

  /**
   * 调用预置位
   * @param {Number} deviceId 设备ID
   * @param {Number} presetNum 预置位编号
   * @returns {Promise}
   */
  gotoPreset: (deviceId, presetNum) =>
    postRequest(`/api/v1/video/monitor/${deviceId}/preset/${presetNum}`),

  /**
   * 获取预置位列表
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getPresetList: (deviceId) =>
    getRequest(`/api/v1/video/monitor/${deviceId}/preset/list`)
}

// 快捷操作相关接口
export const quickActionApi = {
  /**
   * 移动端快捷操作
   * @param {Number} deviceId 设备ID
   * @param {String} action 操作类型：SNAPSHOT/START_RECORD/STOP_RECORD/PRESET_1等
   * @returns {Promise}
   */
  quickAction: (deviceId, action) =>
    postRequest(`/api/mobile/v1/video/quick-action/${deviceId}`, null, { action }),

  /**
   * 截图
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  captureSnapshot: (deviceId) =>
    postRequest(`/api/v1/video/monitor/${deviceId}/snapshot`),

  /**
   * 开始录像
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  startRecord: (deviceId) =>
    postRequest(`/api/v1/video/monitor/${deviceId}/record/start`),

  /**
   * 停止录像
   * @param {Number} deviceId 设备ID
   * @param {String} recordId 录像ID
   * @returns {Promise}
   */
  stopRecord: (deviceId, recordId) =>
    postRequest(`/api/v1/video/monitor/${deviceId}/record/stop`, { recordId })
}

// 设备管理相关接口
export const deviceApi = {
  /**
   * 移动端设备列表
   * @param {Boolean} onlineOnly 是否只显示在线设备
   * @param {String} deviceType 设备类型过滤
   * @returns {Promise}
   */
  getMobileDevices: (onlineOnly = true, deviceType) =>
    getRequest('/api/mobile/v1/video/devices', { onlineOnly, deviceType }),

  /**
   * 移动端设备详情
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getMobileDeviceDetail: (deviceId) =>
    getRequest(`/api/mobile/v1/video/devices/${deviceId}/detail`),

  /**
   * 获取设备统计信息
   * @returns {Promise}
   */
  getDeviceStatistics: () =>
    getRequest('/api/v1/video/devices/statistics')
}

// 告警管理相关接口
export const alarmApi = {
  /**
   * 移动端告警概览
   * @returns {Promise}
   */
  getAlarmOverview: () =>
    getRequest('/api/mobile/v1/video/alarms/overview'),

  /**
   * 移动端告警处理
   * @param {String} alarmId 告警ID
   * @param {String} action 处理动作：CONFIRM/IGNORE/FORWARD
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  processMobileAlarm: (alarmId, action, userId) =>
    postRequest(`/api/mobile/v1/video/alarms/${alarmId}/process`, null, { action, userId }),

  /**
   * 获取活跃告警列表
   * @param {Number} limit 数量限制
   * @returns {Promise}
   */
  getActiveAlarms: (limit = 10) =>
    getRequest('/api/v1/video/alarms/active', { limit }),

  /**
   * 获取告警统计
   * @param {String} timeRange 时间范围：DAY/WEEK/MONTH
   * @returns {Promise}
   */
  getAlarmStatistics: (timeRange = 'DAY') =>
    getRequest('/api/v1/video/alarms/statistics', { timeRange })
}

// 录像回放相关接口
export const playbackApi = {
  /**
   * 查询历史录像
   * @param {Object} params 查询参数
   * @param {Number} params.deviceId 设备ID
   * @param {String} params.startTime 开始时间
   * @param {String} params.endTime 结束时间
   * @returns {Promise}
   */
  queryPlayback: (params) =>
    getRequest('/api/v1/video/playback/query', params),

  /**
   * 获取回放流地址
   * @param {Number} deviceId 设备ID
   * @param {String} recordId 录像ID
   * @returns {Promise}
   */
  getPlaybackUrl: (deviceId, recordId) =>
    getRequest(`/api/v1/video/playback/${deviceId}/${recordId}/url`),

  /**
   * 下载录像
   * @param {String} recordId 录像ID
   * @returns {Promise}
   */
  downloadRecord: (recordId) =>
    getRequest(`/api/v1/video/playback/download/${recordId}`)
}

// 流媒体优化相关接口
export const streamApi = {
  /**
   * 获取优化的流媒体配置
   * @param {Number} deviceId 设备ID
   * @param {String} networkType 网络类型：WIFI/4G/3G
   * @returns {Promise}
   */
  getOptimizedStreamConfig: (deviceId, networkType = 'WIFI') =>
    getRequest(`/api/mobile/v1/video/stream/optimized/${deviceId}`, { networkType }),

  /**
   * 检测网络质量
   * @returns {Promise}
   */
  detectNetworkQuality: () =>
    getRequest('/api/mobile/v1/video/stream/network-quality')
}

// 导出所有API
export default {
  ...monitorApi,
  ...ptzApi,
  ...quickActionApi,
  ...deviceApi,
  ...alarmApi,
  ...playbackApi,
  ...streamApi
}

