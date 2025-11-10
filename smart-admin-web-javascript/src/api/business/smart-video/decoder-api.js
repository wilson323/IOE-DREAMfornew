/*
 * 解码器管理API接口
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Copyright  1024创新实验室
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const decoderApi = {
  /**
   * 查询解码器列表
   * @param {Object} params - 查询参数
   * @param {number} params.pageNum - 页码
   * @param {number} params.pageSize - 每页数量
   * @param {string} params.decoderName - 解码器名称
   * @param {number} params.status - 状态（1:在线，0:离线）
   * @param {string} params.manufacturer - 厂商
   * @returns {Promise}
   */
  queryDecoderList: (params) => {
    return postRequest('/decoder/query', params);
  },

  /**
   * 添加解码器
   * @param {Object} params - 解码器信息
   * @returns {Promise}
   */
  addDecoder: (params) => {
    return postRequest('/decoder/add', params);
  },

  /**
   * 更新解码器
   * @param {Object} params - 解码器信息
   * @returns {Promise}
   */
  updateDecoder: (params) => {
    return postRequest('/decoder/update', params);
  },

  /**
   * 删除解码器
   * @param {number} id - 解码器ID
   * @returns {Promise}
   */
  deleteDecoder: (id) => {
    return getRequest(`/decoder/delete/${id}`);
  },

  /**
   * 批量删除解码器
   * @param {Array<number>} ids - 解码器ID数组
   * @returns {Promise}
   */
  batchDeleteDecoder: (ids) => {
    return postRequest('/decoder/update/batch/delete', ids);
  },

  /**
   * 获取解码器详细信息
   * @param {number} id - 解码器ID
   * @returns {Promise}
   */
  getDecoderDetail: (id) => {
    return getRequest(`/decoder/detail/${id}`);
  },

  /**
   * 获取解码器通道信息
   * @param {number} decoderId - 解码器ID
   * @returns {Promise}
   */
  getDecoderChannels: (decoderId) => {
    return getRequest(`/decoder/channels/${decoderId}`);
  },

  /**
   * 重启解码器
   * @param {number} id - 解码器ID
   * @returns {Promise}
   */
  restartDecoder: (id) => {
    return postRequest(`/decoder/restart/${id}`);
  },

  /**
   * 测试解码器连接
   * @param {Object} params - 连接参数
   * @param {string} params.ipAddress - IP地址
   * @param {number} params.port - 端口
   * @param {string} params.username - 用户名
   * @param {string} params.password - 密码
   * @returns {Promise}
   */
  testConnection: (params) => {
    return postRequest('/decoder/test', params);
  },

  /**
   * 获取解码器统计信息
   * @returns {Promise}
   */
  getDecoderStatistics: () => {
    return getRequest('/decoder/statistics');
  },

  /**
   * 配置通道
   * @param {Object} params - 通道配置参数
   * @param {number} params.decoderId - 解码器ID
   * @param {number} params.channelNumber - 通道编号
   * @param {Object} params.config - 配置信息
   * @returns {Promise}
   */
  configChannel: (params) => {
    return postRequest('/decoder/channel/config', params);
  },

  /**
   * 释放通道
   * @param {Object} params - 通道参数
   * @param {number} params.decoderId - 解码器ID
   * @param {number} params.channelNumber - 通道编号
   * @returns {Promise}
   */
  releaseChannel: (params) => {
    return postRequest('/decoder/channel/release', params);
  },

  /**
   * 获取解码器实时状态
   * @param {number} id - 解码器ID
   * @returns {Promise}
   */
  getDecoderStatus: (id) => {
    return getRequest(`/decoder/status/${id}`);
  },

  /**
   * 获取解码器性能指标
   * @param {number} id - 解码器ID
   * @returns {Promise}
   */
  getDecoderMetrics: (id) => {
    return getRequest(`/decoder/metrics/${id}`);
  },

  /**
   * 导出解码器列表
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  exportDecoderList: (params) => {
    return postRequest('/decoder/export', params, {
      responseType: 'blob',
    });
  },
};