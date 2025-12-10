/*
 * 视频监控 PC端 API
 * 
 * 根据API契约文档和已创建的Controller实现
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest } from '/@/lib/axios';

export const videoPcApi = {
  // ==================== 视频设备管理 ====================

  /**
   * 分页查询设备
   */
  queryDevices: (params) => {
    return postRequest('/video/device/query', params);
  },

  /**
   * 查询设备详情
   */
  getDeviceDetail: (deviceId) => {
    return getRequest(`/video/device/${deviceId}`);
  },

  // ==================== 视频设备管理 ====================

  /**
   * 添加设备
   */
  addDevice: (params) => {
    return postRequest('/video/device/add', params);
  },

  /**
   * 更新设备
   */
  updateDevice: (params) => {
    return postRequest('/video/device/update', params);
  },

  /**
   * 删除设备
   */
  deleteDevice: (deviceId) => {
    return postRequest(`/video/device/delete/${deviceId}`);
  },

  /**
   * 获取设备详细信息
   */
  getDeviceInfo: (deviceId) => {
    return getRequest(`/video/device/info/${deviceId}`);
  },

  /**
   * 获取设备配置
   */
  getDeviceConfig: (deviceId) => {
    return getRequest(`/video/device/config/${deviceId}`);
  },

  /**
   * 更新设备配置
   */
  updateDeviceConfig: (params) => {
    return postRequest('/video/device/config/update', params);
  },

  /**
   * 批量删除设备
   */
  batchDeleteDevice: (deviceIds) => {
    return postRequest('/video/device/batch/delete', deviceIds);
  },

  /**
   * 搜索在线设备
   */
  searchOnlineDevices: (params) => {
    return postRequest('/video/device/search', params);
  },

  /**
   * 同步设备信息
   */
  syncDevice: (deviceId) => {
    return postRequest(`/video/device/sync/${deviceId}`);
  },

  /**
   * 批量同步设备
   */
  batchSyncDevices: (deviceIds) => {
    return postRequest('/video/device/batch/sync', deviceIds);
  },

  /**
   * 同步设备时间
   */
  syncDeviceTime: (deviceId) => {
    return postRequest(`/video/device/sync-time/${deviceId}`);
  },

  /**
   * 测试设备连接
   */
  testDeviceConnection: (params) => {
    return postRequest('/video/device/test', params);
  },

  /**
   * 获取设备分组树
   */
  getDeviceGroupTree: () => {
    return getRequest('/video/device/group/tree');
  },

  // ==================== 视频播放管理 ====================

  /**
   * 获取视频流地址
   */
  getVideoStream: (params) => {
    return postRequest('/video/play/stream', params);
  },

  /**
   * 获取视频截图
   */
  getSnapshot: (deviceId, channelId) => {
    return getRequest(`/video/play/snapshot/${deviceId}`, { channelId });
  },

  /**
   * 开始录像
   */
  startRecording: (params) => {
    return postRequest('/video/play/record/start', params);
  },

  /**
   * 停止录像
   */
  stopRecording: (deviceId, channelId) => {
    return postRequest(`/video/play/record/stop/${deviceId}`, { channelId });
  },

  // ==================== 云台控制 ====================

  /**
   * 云台上移
   */
  ptzUp: (deviceId, channelId, speed = 5) => {
    return postRequest(`/video/ptz/up/${deviceId}`, { channelId, speed });
  },

  /**
   * 云台下移
   */
  ptzDown: (deviceId, channelId, speed = 5) => {
    return postRequest(`/video/ptz/down/${deviceId}`, { channelId, speed });
  },

  /**
   * 云台左移
   */
  ptzLeft: (deviceId, channelId, speed = 5) => {
    return postRequest(`/video/ptz/left/${deviceId}`, { channelId, speed });
  },

  /**
   * 云台右移
   */
  ptzRight: (deviceId, channelId, speed = 5) => {
    return postRequest(`/video/ptz/right/${deviceId}`, { channelId, speed });
  },

  /**
   * 云台放大
   */
  ptzZoomIn: (deviceId, channelId, speed = 5) => {
    return postRequest(`/video/ptz/zoom-in/${deviceId}`, { channelId, speed });
  },

  /**
   * 云台缩小
   */
  ptzZoomOut: (deviceId, channelId, speed = 5) => {
    return postRequest(`/video/ptz/zoom-out/${deviceId}`, { channelId, speed });
  },

  /**
   * 停止云台
   */
  ptzStop: (deviceId, channelId) => {
    return postRequest(`/video/ptz/stop/${deviceId}`, { channelId });
  }
};

