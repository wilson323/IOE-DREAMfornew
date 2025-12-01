/*
 * 门禁管理-设备API接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, postEncryptRequest } from '/@/lib/axios';

export const accessDeviceApi = {
  /**
   * 分页查询门禁设备列表
   */
  queryDeviceList: (params) => {
    return postRequest('/access/device/query', params);
  },

  /**
   * 获取门禁设备详情
   */
  getDeviceDetail: (deviceId) => {
    return getRequest(`/access/device/detail/${deviceId}`);
  },

  /**
   * 添加门禁设备
   */
  addDevice: (params) => {
    return postRequest('/access/device/add', params);
  },

  /**
   * 更新门禁设备信息
   */
  updateDevice: (params) => {
    return postRequest('/access/device/update', params);
  },

  /**
   * 删除门禁设备
   */
  deleteDevice: (deviceId) => {
    return getRequest(`/access/device/delete/${deviceId}`);
  },

  /**
   * 批量删除门禁设备
   */
  batchDeleteDevices: (deviceIds) => {
    return postRequest('/access/device/batch/delete', deviceIds);
  },

  /**
   * 更新设备状态
   */
  updateDeviceStatus: (deviceId, status) => {
    return postRequest('/access/device/update/status', { deviceId, status });
  },

  /**
   * 远程开门
   */
  remoteOpenDoor: (deviceId) => {
    return postRequest('/access/device/remote/open', { deviceId });
  },

  /**
   * 重启设备
   */
  restartDevice: (deviceId) => {
    return postRequest('/access/device/restart', { deviceId });
  },

  /**
   * 同步设备时间
   */
  syncDeviceTime: (deviceId) => {
    return postRequest(`/api/smart/access/device/syncTime/${deviceId}`);
  },

  /**
   * 获取设备状态统计
   */
  getDeviceStats: () => {
    return getRequest('/access/device/stats');
  },

  /**
   * 获取设备分组树
   */
  getDeviceGroupTree: () => {
    return getRequest('/access/device/group/tree');
  },

  /**
   * 设备配置同步
   */
  syncDeviceConfig: (deviceId) => {
    return getRequest(`/access/device/sync/config/${deviceId}`);
  },

  /**
   * 获取设备实时状态
   */
  getDeviceRealTimeStatus: (deviceIds) => {
    return postRequest('/access/device/realtime/status', { deviceIds });
  },

  /**
   * 设备测试连接
   */
  testDeviceConnection: (deviceId) => {
    return getRequest(`/access/device/test/connection/${deviceId}`);
  }
};
