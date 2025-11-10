/*
 * 智能视频-设备管理API接口
 *
 * @Author:    Claude Code
 * @Date:      2024-11-05
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { postRequest, getRequest } from '/@/lib/axios';

/**
 * 设备管理API
 */
export const deviceApi = {
  /**
   * 查询设备列表
   */
  queryDeviceList: (params) => {
    return postRequest('/device/query', params);
  },

  /**
   * 添加设备
   */
  addDevice: (params) => {
    return postRequest('/device/add', params);
  },

  /**
   * 更新设备
   */
  updateDevice: (params) => {
    return postRequest('/device/update', params);
  },

  /**
   * 删除设备
   */
  deleteDevice: (id) => {
    return getRequest(`/device/delete/${id}`);
  },

  /**
   * 批量删除设备
   */
  batchDeleteDevice: (ids) => {
    return postRequest('/device/batch/delete', ids);
  },

  /**
   * 搜索在线设备（从网络搜索发现设备）
   */
  searchOnlineDevices: (params) => {
    return postRequest('/device/search', params);
  },

  /**
   * 同步设备信息（从设备同步最新信息）
   */
  syncDevice: (id) => {
    return postRequest(`/device/sync/${id}`);
  },

  /**
   * 批量同步设备
   */
  batchSyncDevices: (ids) => {
    return postRequest('/device/batch/sync', ids);
  },

  /**
   * 订阅设备事件
   */
  subscribeDevice: (params) => {
    return postRequest('/device/subscribe', params);
  },

  /**
   * 取消订阅设备事件
   */
  unsubscribeDevice: (params) => {
    return postRequest('/device/unsubscribe', params);
  },

  /**
   * 同步设备时间
   */
  syncDeviceTime: (id) => {
    return postRequest(`/device/sync-time/${id}`);
  },

  /**
   * 获取设备详细信息
   */
  getDeviceInfo: (id) => {
    return getRequest(`/device/info/${id}`);
  },

  /**
   * 获取设备配置
   */
  getDeviceConfig: (id) => {
    return getRequest(`/device/config/${id}`);
  },

  /**
   * 更新设备配置
   */
  updateDeviceConfig: (params) => {
    return postRequest('/device/config/update', params);
  },

  /**
   * 设备维护管理
   */
  deviceMaintenance: (params) => {
    return postRequest('/device/maintenance', params);
  },

  /**
   * 获取设备分组树
   */
  getDeviceGroupTree: () => {
    return getRequest('/device/group/tree');
  },

  /**
   * 导入设备
   */
  importDevices: (file) => {
    return postRequest('/device/import', file);
  },

  /**
   * 导出设备
   */
  exportDevices: (params) => {
    return postRequest('/device/export', params);
  },

  /**
   * 测试设备连接
   */
  testConnection: (params) => {
    return postRequest('/device/test', params);
  },
};
