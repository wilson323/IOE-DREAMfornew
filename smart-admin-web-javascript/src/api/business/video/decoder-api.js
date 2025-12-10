/*
 * 视频解码器 API
 * 智慧园区视频监控解码器管理接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-10
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const decoderApi = {
  // ==================== 解码器管理 ====================

  /**
   * 获取解码器列表
   */
  getDecoderList: (params) => {
    return getRequest('/api/video/decoder/list', params);
  },

  /**
   * 获取解码器详情
   */
  getDecoderDetail: (decoderId) => {
    return getRequest(`/api/video/decoder/detail/${decoderId}`);
  },

  /**
   * 创建解码器
   */
  createDecoder: (data) => {
    return postRequest('/api/video/decoder/create', data);
  },

  /**
   * 更新解码器信息
   */
  updateDecoder: (data) => {
    return putRequest('/api/video/decoder/update', data);
  },

  /**
   * 删除解码器
   */
  deleteDecoder: (decoderId) => {
    return deleteRequest(`/api/video/decoder/delete/${decoderId}`);
  },

  // ==================== 解码器配置 ====================

  /**
   * 获取解码器配置
   */
  getDecoderConfig: (decoderId) => {
    return getRequest(`/api/video/decoder/config/${decoderId}`);
  },

  /**
   * 更新解码器配置
   */
  updateDecoderConfig: (decoderId, config) => {
    return putRequest(`/api/video/decoder/config/${decoderId}`, config);
  },

  // ==================== 解码器状态 ====================

  /**
   * 获取解码器状态
   */
  getDecoderStatus: (decoderId) => {
    return getRequest(`/api/video/decoder/status/${decoderId}`);
  },

  /**
   * 启动解码器
   */
  startDecoder: (decoderId) => {
    return postRequest(`/api/video/decoder/start/${decoderId}`);
  },

  /**
   * 停止解码器
   */
  stopDecoder: (decoderId) => {
    return postRequest(`/api/video/decoder/stop/${decoderId}`);
  },

  /**
   * 重启解码器
   */
  restartDecoder: (decoderId) => {
    return postRequest(`/api/video/decoder/restart/${decoderId}`);
  },

  // ==================== 解码器通道管理 ====================

  /**
   * 获取解码器通道列表
   */
  getDecoderChannels: (decoderId) => {
    return getRequest(`/api/video/decoder/channels/${decoderId}`);
  },

  /**
   * 更新解码器通道配置
   */
  updateDecoderChannel: (decoderId, channelId, config) => {
    return putRequest(`/api/video/decoder/channel/${decoderId}/${channelId}`, config);
  },

  // ==================== 解码器流管理 ====================

  /**
   * 获取解码器流信息
   */
  getDecoderStreams: (decoderId) => {
    return getRequest(`/api/video/decoder/streams/${decoderId}`);
  },

  /**
   * 配置解码器流
   */
  configureDecoderStream: (decoderId, streamConfig) => {
    return postRequest(`/api/video/decoder/stream/config/${decoderId}`, streamConfig);
  }
};