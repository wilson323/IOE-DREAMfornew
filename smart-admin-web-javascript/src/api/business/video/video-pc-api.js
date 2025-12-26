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
  },

  /**
   * 设置预置位
   */
  setPreset: (deviceId, channelId, presetId, presetName) => {
    return postRequest('/video/ptz/preset/set', {
      deviceId,
      channelId,
      presetId,
      presetName
    });
  },

  /**
   * 删除预置位
   */
  deletePreset: (deviceId, channelId, presetId) => {
    return postRequest('/video/ptz/preset/delete', {
      deviceId,
      channelId,
      presetId
    });
  },

  /**
   * 调用预置位
   */
  gotoPreset: (deviceId, channelId, presetId, speed = 5) => {
    return postRequest('/video/ptz/preset/goto', {
      deviceId,
      channelId,
      presetId,
      speed
    });
  },

  /**
   * 获取预置位列表
   */
  getPresetList: (deviceId, channelId) => {
    return getRequest(`/video/ptz/preset/list/${deviceId}`, { channelId });
  },

  /**
   * 巡航启动
   */
  startCruise: (deviceId, channelId, cruiseId, speed = 5) => {
    return postRequest('/video/ptz/cruise/start', {
      deviceId,
      channelId,
      cruiseId,
      speed
    });
  },

  /**
   * 巡航停止
   */
  stopCruise: (deviceId, channelId) => {
    return postRequest('/video/ptz/cruise/stop', {
      deviceId,
      channelId
    });
  },

  // ==================== 录像回放 ====================

  /**
   * 查询录像列表
   */
  queryRecordings: (params) => {
    return postRequest('/video/playback/recordings/query', params);
  },

  /**
   * 获取录像时间轴
   */
  getRecordingTimeline: (params) => {
    return postRequest('/video/playback/timeline', params);
  },

  /**
   * 获取录像回放流
   */
  getPlaybackStream: (params) => {
    return postRequest('/video/playback/stream', params);
  },

  /**
   * 下载录像
   */
  downloadRecording: (params) => {
    return postRequest('/video/playback/download', params, {
      responseType: 'blob'
    });
  },

  /**
   * 获取录像下载URL
   */
  getDownloadUrl: (params) => {
    return postRequest('/video/playback/download/url', params);
  },

  // ==================== 告警管理 ====================

  /**
   * 查询告警列表
   */
  queryAlarms: (params) => {
    return postRequest('/video/alarm/query', params);
  },

  /**
   * 获取告警统计
   */
  getAlarmStatistics: (params) => {
    return postRequest('/video/alarm/statistics', params);
  },

  /**
   * 处理告警
   */
  processAlarm: (params) => {
    return postRequest('/video/alarm/process', params);
  },

  /**
   * 批量处理告警
   */
  batchProcessAlarms: (params) => {
    return postRequest('/video/alarm/batch-process', params);
  },

  /**
   * 获取告警详情
   */
  getAlarmDetail: (alarmId) => {
    return getRequest(`/video/alarm/detail/${alarmId}`);
  },

  /**
   * 获取告警快照
   */
  getAlarmSnapshot: (alarmId) => {
    return getRequest(`/video/alarm/snapshot/${alarmId}`, {}, {
      responseType: 'blob'
    });
  },

  /**
   * 导出告警数据
   */
  exportAlarms: (params) => {
    return postRequest('/video/alarm/export', params, {
      responseType: 'blob'
    });
  },

  /**
   * 订阅实时告警推送
   */
  subscribeAlarmPush: (deviceId, channelId) => {
    return getRequest('/video/alarm/subscribe', { deviceId, channelId });
  },

  // ==================== 电视墙管理 ====================

  /**
   * 获取电视墙列表
   */
  queryTvWalls: (params) => {
    return postRequest('/video/tvwall/query', params);
  },

  /**
   * 获取电视墙详情
   */
  getTvWallDetail: (wallId) => {
    return getRequest(`/video/tvwall/detail/${wallId}`);
  },

  /**
   * 创建电视墙
   */
  createTvWall: (params) => {
    return postRequest('/video/tvwall/create', params);
  },

  /**
   * 更新电视墙
   */
  updateTvWall: (params) => {
    return postRequest('/video/tvwall/update', params);
  },

  /**
   * 删除电视墙
   */
  deleteTvWall: (wallId) => {
    return postRequest(`/video/tvwall/delete/${wallId}`);
  },

  /**
   * 切换电视墙布局
   */
  switchLayout: (wallId, layout) => {
    return postRequest('/video/tvwall/layout/switch', { wallId, layout });
  },

  /**
   * 交换窗口位置
   */
  swapWindows: (wallId, window1, window2) => {
    return postRequest('/video/tvwindow/swap', { wallId, window1, window2 });
  },

  /**
   * 设置窗口视频源
   */
  setWindowSource: (wallId, windowIndex, deviceId, channelId) => {
    return postRequest('/video/tvwindow/source/set', {
      wallId,
      windowIndex,
      deviceId,
      channelId
    });
  },

  /**
   * 启动轮巡
   */
  startTour: (wallId, tourConfig) => {
    return postRequest('/video/tvwall/tour/start', { wallId, tourConfig });
  },

  /**
   * 停止轮巡
   */
  stopTour: (wallId) => {
    return postRequest(`/video/tvwall/tour/stop/${wallId}`);
  },

  /**
   * 获取轮巡配置
   */
  getTourConfig: (wallId) => {
    return getRequest(`/video/tvwall/tour/config/${wallId}`);
  },

  /**
   * 保存电视墙配置
   */
  saveTvWallConfig: (wallId, config) => {
    return postRequest('/video/tvwall/config/save', { wallId, config });
  },

  /**
   * 加载电视墙配置
   */
  loadTvWallConfig: (wallId) => {
    return getRequest(`/video/tvwall/config/load/${wallId}`);
  },

  // ==================== 智能检索 ====================

  /**
   * 以图搜图 - 人脸检索
   */
  searchFace: (params) => {
    return postRequest('/video/ai/face/search', params);
  },

  /**
   * 以图搜图 - 车辆检索
   */
  searchVehicle: (params) => {
    return postRequest('/video/ai/vehicle/search', params);
  },

  /**
   * 以图搜图 - 人体检索
   */
  searchPerson: (params) => {
    return postRequest('/video/ai/person/search', params);
  },

  /**
   * 上传检索图片
   */
  uploadSearchImage: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return postRequest('/video/ai/image/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  /**
   * 获取检索结果详情
   */
  getSearchResultDetail: (resultId) => {
    return getRequest(`/video/ai/search/result/${resultId}`);
  },

  /**
   * 目标轨迹追踪
   */
  trackTarget: (params) => {
    return postRequest('/video/ai/track/target', params);
  },

  /**
   * 获取目标轨迹
   */
  getTargetTrajectory: (targetId, params) => {
    return postRequest('/video/ai/track/trajectory', { targetId, ...params });
  },

  /**
   * 导出检索结果
   */
  exportSearchResults: (params) => {
    return postRequest('/video/ai/search/export', params, {
      responseType: 'blob'
    });
  },

  /**
   * 获取人脸库列表
   */
  getFaceLibraries: () => {
    return getRequest('/video/ai/face/libraries');
  },

  /**
   * 获取车辆库列表
   */
  getVehicleLibraries: () => {
    return getRequest('/video/ai/vehicle/libraries');
  },

  /**
   * 添加人脸到库
   */
  addFaceToLibrary: (params) => {
    return postRequest('/video/ai/face/add', params);
  },

  /**
   * 添加车辆到库
   */
  addVehicleToLibrary: (params) => {
    return postRequest('/video/ai/vehicle/add', params);
  }
};

