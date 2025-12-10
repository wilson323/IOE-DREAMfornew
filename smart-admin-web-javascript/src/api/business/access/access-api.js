/*
 * 门禁管理 API
 * 
 * 根据API契约文档和已创建的Controller实现
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const accessApi = {
  // ==================== 门禁记录管理 ====================

  /**
   * 分页查询门禁记录
   */
  queryAccessRecords: (params) => {
    return postRequest('/api/v1/access/record/query', params);
  },

  /**
   * 获取门禁记录统计
   */
  getAccessRecordStatistics: (params) => {
    return getRequest('/api/v1/access/record/statistics', params);
  },

  // ==================== 设备管理 ====================

  /**
   * 分页查询设备
   */
  queryDevices: (params) => {
    return postRequest('/api/v1/access/device/query', params);
  },

  /**
   * 查询设备详情
   */
  getDeviceDetail: (deviceId) => {
    return getRequest(`/api/v1/access/device/${deviceId}`);
  },

  /**
   * 添加设备
   */
  addDevice: (data) => {
    return postRequest('/api/v1/access/device/add', data);
  },

  /**
   * 更新设备
   */
  updateDevice: (data) => {
    return putRequest('/api/v1/access/device/update', data);
  },

  /**
   * 删除设备
   */
  deleteDevice: (deviceId) => {
    return deleteRequest(`/api/v1/access/device/${deviceId}`);
  },

  /**
   * 更新设备状态
   */
  updateDeviceStatus: (params) => {
    return postRequest('/api/v1/access/device/status/update', null, params);
  },

  // ==================== 移动端验证功能 ====================

  /**
   * 二维码验证
   * @param {Object} data 验证数据
   * @param {String} data.qrCode 二维码
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyQRCode: (data) => {
    return postRequest('/api/v1/mobile/access/qr/verify', data);
  },

  /**
   * NFC验证
   * @param {Object} data 验证数据
   * @param {String} data.nfcCardId NFC卡ID
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyNFC: (data) => {
    return postRequest('/api/v1/mobile/access/nfc/verify', data);
  },

  /**
   * 生物识别验证
   * @param {Object} data 验证数据
   * @param {Number} data.userId 用户ID
   * @param {String} data.biometricType 生物识别类型
   * @param {String} data.biometricData 生物识别数据
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyBiometric: (data) => {
    return postRequest('/api/v1/mobile/access/biometric/verify', data);
  },

  /**
   * 获取附近设备
   * @param {Number} userId 用户ID
   * @param {Number} latitude 纬度
   * @param {Number} longitude 经度
   * @param {Number} radius 半径（米，默认500）
   * @returns {Promise}
   */
  getNearbyDevices: (userId, latitude, longitude, radius = 500) => {
    return getRequest('/api/v1/mobile/access/devices/nearby', {
      userId, latitude, longitude, radius
    });
  },

  /**
   * 获取实时门禁状态
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getRealTimeStatus: (deviceId) => {
    return getRequest('/api/v1/mobile/access/status/realtime', { deviceId });
  },

  /**
   * 获取用户门禁权限
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserPermissions: (userId) => {
    return getRequest(`/api/v1/mobile/access/permissions/${userId}`);
  },

  /**
   * 获取用户访问记录
   * @param {Number} userId 用户ID
   * @param {Number} size 记录数量（默认20）
   * @returns {Promise}
   */
  getUserAccessRecords: (userId, size = 20) => {
    return getRequest(`/api/v1/mobile/access/records/${userId}`, { size });
  },

  /**
   * 临时开门申请
   * @param {Object} data 申请数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.deviceId 设备ID
   * @param {String} data.reason 申请原因
   * @returns {Promise}
   */
  requestTemporaryAccess: (data) => {
    return postRequest('/api/v1/mobile/access/temporary-access', data);
  },

  /**
   * 发送推送通知
   * @param {Object} data 通知数据
   * @param {Number} data.userId 用户ID
   * @param {String} data.notificationType 通知类型
   * @returns {Promise}
   */
  sendPushNotification: (data) => {
    return postRequest('/api/v1/mobile/access/notification/push', data);
  }
};

