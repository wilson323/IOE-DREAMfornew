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

  // ==================== 视频播放管理 ====================

  /**
   * 获取视频流地址
   */
  getVideoStream: (params) => {
    return postRequest('/video/play/stream', null, params);
  },

  /**
   * 获取视频截图
   */
  getSnapshot: (deviceId, channelId) => {
    return getRequest(`/video/play/snapshot/${deviceId}`, { channelId });
  },
};

