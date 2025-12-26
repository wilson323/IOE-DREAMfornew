/**
 * 视频管理API - 前端版本
 * 基于Vue 3.4 + Ant Design Vue 4的企业级视频管理前端接口
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-30
 * @Copyright: IOE-DREAM (https://ioe-dream.net), Since 2025
 */

import { postRequest, getRequest, putRequest, deleteRequest } from '/@/utils/http';

// ==================== 视频设备管理 ====================

/**
 * 分页查询视频设备
 * @param {Object} params - 查询参数
 * @returns {Promise} 分页结果
 */
export const queryVideoDevices = (params) => {
  return postRequest('/api/v1/video/device/query', params);
};

/**
 * 查询视频设备详情
 * @param {String|Number} deviceId - 设备ID
 * @returns {Promise} 设备详情
 */
export const getVideoDeviceDetail = (deviceId) => {
  return getRequest(`/api/v1/video/device/${deviceId}`);
};

/**
 * 添加视频设备
 * @param {Object} params - 设备信息
 * @returns {Promise} 操作结果
 */
export const addVideoDevice = (params) => {
  return postRequest('/api/v1/video/device', params);
};

/**
 * 更新视频设备
 * @param {Object} params - 设备信息
 * @returns {Promise} 操作结果
 */
export const updateVideoDevice = (params) => {
  return putRequest('/api/v1/video/device', params);
};

/**
 * 删除视频设备
 * @param {String|Number} deviceId - 设备ID
 * @returns {Promise} 操作结果
 */
export const deleteVideoDevice = (deviceId) => {
  return deleteRequest(`/api/v1/video/device/${deviceId}`);
};

/**
 * 更新设备状态
 * @param {String|Number} deviceId - 设备ID
 * @param {Number} enabled - 启用状态
 * @returns {Promise} 操作结果
 */
export const updateVideoDeviceStatus = (deviceId, enabled) => {
  return putRequest(`/api/v1/video/device/${deviceId}/status`, null, {
    params: { enabled }
  });
};

// ==================== 视频流管理 ====================

/**
 * 获取视频流地址
 * @param {String|Number} deviceId - 设备ID
 * @returns {Promise} 流地址
 */
export const getVideoStream = (deviceId) => {
  return getRequest(`/api/v1/video/stream/${deviceId}`);
};

/**
 * 开始录制
 * @param {String|Number} deviceId - 设备ID
 * @param {Object} params - 录制参数
 * @returns {Promise} 操作结果
 */
export const startRecording = (deviceId, params) => {
  return postRequest(`/api/v1/video/recording/start/${deviceId}`, params);
};

/**
 * 停止录制
 * @param {String|Number} deviceId - 设备ID
 * @returns {Promise} 操作结果
 */
export const stopRecording = (deviceId) => {
  return postRequest(`/api/v1/video/recording/stop/${deviceId}`);
};

/**
 * 获取录像列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 录像列表
 */
export const queryVideoRecordings = (params) => {
  return postRequest('/api/v1/video/recording/query', params);
};

// ==================== AI分析功能 ====================

/**
 * 执行人脸识别
 * @param {String|Number} deviceId - 设备ID
 * @param {Object} params - 识别参数
 * @returns {Promise} 识别结果
 */
export const performFaceRecognition = (deviceId, params) => {
  return postRequest(`/api/v1/video/ai/face/${deviceId}`, params);
};

/**
 * 获取AI事件列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 事件列表
 */
export const queryAIEvents = (params) => {
  return postRequest('/api/v1/video/ai/events/query', params);
};

/**
 * 配置AI规则
 * @param {Object} params - 规则配置
 * @returns {Promise} 配置结果
 */
export const configureAIRules = (params) => {
  return postRequest('/api/v1/video/ai/rules/configure', params);
};

// ==================== 视频监控 ====================

/**
 * 获取监控画面
 * @param {String|Number} deviceId - 设备ID
 * @returns {Promise} 监控画面
 */
export const getMonitoringView = (deviceId) => {
  return getRequest(`/api/v1/video/monitor/${deviceId}`);
};

/**
 * 控制云台
 * @param {String|Number} deviceId - 设备ID
 * @param {Object} params - 控制参数
 * @returns {Promise} 控制结果
 */
export const controlPTZ = (deviceId, params) => {
  return postRequest(`/api/v1/video/monitor/ptz/${deviceId}`, params);
};

// ==================== 设备统计 ====================

/**
 * 获取设备统计
 * @returns {Promise} 统计数据
 */
export const getVideoDeviceStatistics = () => {
  return getRequest('/api/v1/video/statistics/devices');
};

/**
 * 获取在线设备数量
 * @returns {Promise} 在线数量
 */
export const getOnlineDeviceCount = () => {
  return getRequest('/api/v1/video/statistics/online');
};

// ==================== 导出功能 ====================

/**
 * 导出设备列表
 * @param {Object} params - 导出参数
 * @returns {Promise} 导出文件
 */
export const exportVideoDevices = (params) => {
  return postRequest('/api/v1/video/device/export', params);
};

/**
 * 导出录像记录
 * @param {Object} params - 导出参数
 * @returns {Promise} 导出文件
 */
export const exportVideoRecordings = (params) => {
  return postRequest('/api/v1/video/recording/export', params);
};

// ==================== 默认导出 ====================

export default {
  // 设备管理
  queryVideoDevices,
  getVideoDeviceDetail,
  addVideoDevice,
  updateVideoDevice,
  deleteVideoDevice,
  updateVideoDeviceStatus,

  // 视频流管理
  getVideoStream,
  startRecording,
  stopRecording,
  queryVideoRecordings,

  // AI分析
  performFaceRecognition,
  queryAIEvents,
  configureAIRules,

  // 视频监控
  getMonitoringView,
  controlPTZ,

  // 设备统计
  getVideoDeviceStatistics,
  getOnlineDeviceCount,

  // 导出功能
  exportVideoDevices,
  exportVideoRecordings
};