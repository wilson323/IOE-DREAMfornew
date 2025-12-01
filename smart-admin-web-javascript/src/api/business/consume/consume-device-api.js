/*
 * 消费设备管理 API
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025/11/17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const consumeDeviceApi = {
  /**
   * 查询设备列表
   */
  queryDeviceList: (params) => {
    return postRequest('/api/consume/device/list', params);
  },

  /**
   * 查询设备详情
   */
  getDeviceDetail: (deviceId) => {
    return getRequest(`/api/consume/device/detail/${deviceId}`);
  },

  /**
   * 添加设备
   */
  addDevice: (data) => {
    return postRequest('/api/consume/device/add', data);
  },

  /**
   * 编辑设备
   */
  updateDevice: (data) => {
    return putRequest('/api/consume/device/update', data);
  },

  /**
   * 删除设备
   */
  deleteDevice: (deviceId) => {
    return deleteRequest(`/api/consume/device/delete/${deviceId}`);
  },

  /**
   * 批量删除设备
   */
  batchDeleteDevices: (deviceIds) => {
    return postRequest('/api/consume/device/batch-delete', { deviceIds });
  },

  /**
   * 批量启用设备
   */
  batchEnableDevices: (deviceIds) => {
    return postRequest('/api/consume/device/batch-enable', { deviceIds });
  },

  /**
   * 批量禁用设备
   */
  batchDisableDevices: (deviceIds) => {
    return postRequest('/api/consume/device/batch-disable', { deviceIds });
  },

  /**
   * 远程控制设备
   */
  controlDevice: (deviceId, command) => {
    return postRequest(`/api/consume/device/control/${deviceId}`, { command });
  },

  /**
   * 获取设备状态
   */
  getDeviceStatus: (deviceId) => {
    return getRequest(`/api/consume/device/status/${deviceId}`);
  },

  /**
   * 批量导入设备
   */
  batchImportDevices: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return postRequest('/api/consume/device/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * 导出设备数据
   */
  exportDevices: (params) => {
    return postRequest('/api/consume/device/export', params, {
      responseType: 'blob',
    });
  },

  /**
   * 获取设备统计数据
   */
  getDeviceStats: () => {
    return getRequest('/api/consume/device/stats');
  },

  /**
   * 获取设备实时状态
   */
  getDeviceRealtimeStatus: (deviceIds) => {
    return postRequest('/api/consume/device/realtime-status', { deviceIds });
  },

  /**
   * 重启设备
   */
  restartDevice: (deviceId) => {
    return postRequest(`/api/consume/device/restart/${deviceId}`);
  },

  /**
   * 同步设备时间
   */
  syncDeviceTime: (deviceId) => {
    return postRequest(`/api/consume/device/sync-time/${deviceId}`);
  },

  /**
   * 获取设备日志
   */
  getDeviceLogs: (params) => {
    return postRequest('/api/consume/device/logs', params);
  },

  /**
   * 清除设备数据
   */
  clearDeviceData: (deviceId) => {
    return postRequest(`/api/consume/device/clear-data/${deviceId}`);
  },

  /**
   * 获取设备配置信息
   */
  getDeviceConfig: (deviceId) => {
    return getRequest(`/api/consume/device/config/${deviceId}`);
  },

  /**
   * 更新设备配置
   */
  updateDeviceConfig: (deviceId, config) => {
    return putRequest(`/api/consume/device/config/${deviceId}`, config);
  },

  /**
   * 测试设备连接
   */
  testDeviceConnection: (deviceId) => {
    return postRequest(`/api/consume/device/test-connection/${deviceId}`);
  },

  /**
   * 获取设备版本信息
   */
  getDeviceVersion: (deviceId) => {
    return getRequest(`/api/consume/device/version/${deviceId}`);
  },

  /**
   * 升级设备固件
   */
  upgradeDeviceFirmware: (deviceId, firmwareFile) => {
    const formData = new FormData();
    formData.append('firmware', firmwareFile);
    return postRequest(`/api/consume/device/upgrade/${deviceId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};