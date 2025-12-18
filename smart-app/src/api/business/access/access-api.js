/**
 * 门禁管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest } from '@/lib/smart-request'

// 门禁检查相关接口
export const accessCheckApi = {
  /**
   * 移动端门禁检查
   * @param {Object} data 检查请求数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.deviceId 设备ID
   * @param {Number} data.areaId 区域ID
   * @param {String} data.verificationType 验证类型
   * @param {String} data.location 位置信息
   * @returns {Promise}
   */
  mobileAccessCheck: (data) => postRequest('/api/v1/mobile/access/check', data),

  /**
   * 二维码验证
   * @param {Object} data 验证数据
   * @param {String} data.qrCode 二维码
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyQRCode: (data) => postRequest('/api/v1/mobile/access/qr/verify', data),

  /**
   * NFC验证
   * @param {Object} data 验证数据
   * @param {String} data.nfcCardId NFC卡ID
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyNFC: (data) => postRequest('/api/v1/mobile/access/nfc/verify', data),

  /**
   * 生物识别验证
   * @param {Object} data 验证数据
   * @param {Number} data.userId 用户ID
   * @param {String} data.biometricType 生物识别类型
   * @param {String} data.biometricData 生物识别数据
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyBiometric: (data) => postRequest('/api/v1/mobile/access/biometric/verify', data)
}

// 设备管理相关接口
export const deviceApi = {
  /**
   * 获取附近设备
   * @param {Number} userId 用户ID
   * @param {Number} latitude 纬度
   * @param {Number} longitude 经度
   * @param {Number} radius 半径（米，默认500）
   * @returns {Promise}
   */
  getNearbyDevices: (userId, latitude, longitude, radius = 500) =>
    getRequest('/api/v1/mobile/access/devices/nearby', {
      userId,
      latitude,
      longitude,
      radius
    }),

  /**
   * 获取实时门禁状态
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getRealTimeStatus: (deviceId) =>
    getRequest('/api/v1/mobile/access/status/realtime', { deviceId })
}

// 权限管理相关接口
export const permissionApi = {
  /**
   * 获取用户门禁权限
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserPermissions: (userId) =>
    getRequest(`/api/v1/mobile/access/permissions/${userId}`)
}

// 访问记录相关接口
export const recordApi = {
  /**
   * 获取用户访问记录
   * @param {Number} userId 用户ID
   * @param {Number} size 记录数量（默认20）
   * @returns {Promise}
   */
  getUserAccessRecords: (userId, size = 20) =>
    getRequest(`/api/v1/mobile/access/records/${userId}`, { size }),

  /**
   * 分页查询访问记录
   * @param {Object} params 查询参数
   * @param {Number} params.userId 用户ID
   * @param {Number} params.pageNum 页码（默认1）
   * @param {Number} params.pageSize 每页数量（默认20）
   * @param {String} params.startDate 开始日期（可选）
   * @param {String} params.endDate 结束日期（可选）
   * @param {String} params.status 状态筛选（可选：success/fail）
   * @returns {Promise}
   */
  queryAccessRecords: (params) =>
    getRequest('/api/v1/access/record/query', params),

  /**
   * 获取访问记录统计
   * @param {Object} params 统计参数
   * @param {String} params.startDate 开始日期（可选）
   * @param {String} params.endDate 结束日期（可选）
   * @param {Number} params.areaId 区域ID（可选）
   * @param {Number} params.userId 用户ID（可选）
   * @returns {Promise}
   */
  getAccessRecordStatistics: (params) =>
    getRequest('/api/v1/access/record/statistics', params)
}

// 临时访问相关接口
export const temporaryApi = {
  /**
   * 临时开门申请
   * @param {Object} data 申请数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.deviceId 设备ID
   * @param {String} data.reason 申请原因
   * @returns {Promise}
   */
  requestTemporaryAccess: (data) =>
    postRequest('/api/v1/mobile/access/temporary-access', data)
}

// 通知相关接口
export const notificationApi = {
  /**
   * 发送推送通知
   * @param {Object} data 通知数据
   * @param {Number} data.userId 用户ID
   * @param {String} data.notificationType 通知类型
   * @returns {Promise}
   */
  sendPushNotification: (data) =>
    postRequest('/api/v1/mobile/access/notification/push', data)
}

// 导出所有API
export default {
  ...accessCheckApi,
  ...deviceApi,
  ...permissionApi,
  ...recordApi,
  ...temporaryApi,
  ...notificationApi
}

