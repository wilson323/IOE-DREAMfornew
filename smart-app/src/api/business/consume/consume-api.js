/**
 * 消费管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, putRequest } from '@/lib/smart-request'

// 消费交易相关接口
export const transactionApi = {
  /**
   * 快速消费
   * @param {Object} data 消费数据
   * @returns {Promise}
   */
  quickConsume: (data) => postRequest('/api/v1/consume/mobile/transaction/quick', data),

  /**
   * 扫码消费
   * @param {Object} data 扫码数据
   * @returns {Promise}
   */
  scanConsume: (data) => postRequest('/api/v1/consume/mobile/transaction/scan', data),

  /**
   * NFC刷卡消费
   * @param {Object} data NFC数据
   * @returns {Promise}
   */
  nfcConsume: (data) => postRequest('/api/v1/consume/mobile/transaction/nfc', data),

  /**
   * 人脸识别消费
   * @param {Object} data 人脸数据
   * @returns {Promise}
   */
  faceConsume: (data) => postRequest('/api/v1/consume/mobile/transaction/face', data)
}

// 账户管理相关接口
export const accountApi = {
  /**
   * 快速用户查询
   * @param {String} queryType 查询方式
   * @param {String} queryValue 查询值
   * @returns {Promise}
   */
  quickUserInfo: (queryType, queryValue) => getRequest('/api/v1/consume/mobile/user/quick', {
    queryType,
    queryValue
  }),

  /**
   * 获取用户信息
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserInfo: (userId) => getRequest(`/api/v1/consume/mobile/user/${userId}`),

  /**
   * 获取用户消费信息
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserConsumeInfo: (userId) => getRequest(`/api/v1/consume/mobile/user/consume-info/${userId}`),

  /**
   * 获取账户余额
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getAccountBalance: (userId) => getRequest(`/api/v1/consume/mobile/account/balance/${userId}`),

  /**
   * 获取用户摘要信息
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserSummary: (userId) => getRequest(`/api/v1/consume/mobile/user/${userId}/summary`)
}

// 交易记录相关接口
export const historyApi = {
  /**
   * 获取最近交易记录
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getRecentHistory: (params) => getRequest('/api/v1/consume/mobile/history/recent', params),

  /**
   * 获取交易历史
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getTransactionHistory: (params) => getRequest('/api/v1/consume/mobile/history', params),

  /**
   * 获取交易详情
   * @param {Number} transactionId 交易ID
   * @returns {Promise}
   */
  getTransactionDetail: (transactionId) => getRequest(`/api/v1/consume/mobile/history/${transactionId}`)
}

// 餐别管理相关接口
export const mealApi = {
  /**
   * 获取有效餐别（移动端专用）
   * @returns {Promise}
   */
  getAvailableMeals: () => getRequest('/api/v1/consume/mobile/meal/available'),

  /**
   * 获取可用餐别（按区域）
   * @param {Number} areaId 区域ID
   * @returns {Promise}
   */
  getAvailableMealsByArea: (areaId) => getRequest(`/api/v1/consume/mobile/meals/available/${areaId}`),

  /**
   * 获取当前餐别
   * @returns {Promise}
   */
  getCurrentMeal: () => getRequest('/api/v1/consume/mobile/meals/current')
}

// 统计相关接口
export const statsApi = {
  /**
   * 获取用户统计
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserStats: (userId) => getRequest(`/api/v1/consume/mobile/stats/${userId}`),

  /**
   * 获取消费统计
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getConsumeStats: (params) => getRequest('/api/v1/consume/mobile/stats', params),

  /**
   * 获取设备今日统计
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getDeviceTodayStats: (deviceId) => getRequest(`/api/v1/consume/mobile/device/today-stats/${deviceId}`),

  /**
   * 获取实时交易汇总
   * @param {String} areaId 区域ID（可选）
   * @returns {Promise}
   */
  getTransactionSummary: (areaId) => getRequest('/api/v1/consume/mobile/transaction/summary', {
    areaId
  })
}

// 离线同步相关接口
export const syncApi = {
  /**
   * 离线数据同步
   * @param {Object} data 同步数据
   * @returns {Promise}
   */
  offlineSync: (data) => postRequest('/api/v1/consume/mobile/sync/offline', data),

  /**
   * 获取离线数据
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getOfflineData: (deviceId) => getRequest(`/api/v1/consume/mobile/sync/offline/${deviceId}`),

  /**
   * 批量下载数据
   * @param {Object} data 下载请求
   * @returns {Promise}
   */
  batchDownload: (data) => postRequest('/api/v1/consume/mobile/sync/batch-download', data)
}

// 设备管理相关接口
export const deviceApi = {
  /**
   * 设备认证
   * @param {Object} data 认证数据
   * @returns {Promise}
   */
  deviceAuth: (data) => postRequest('/api/v1/consume/mobile/device/auth', data),

  /**
   * 设备注册
   * @param {Object} data 注册数据
   * @returns {Promise}
   */
  deviceRegister: (data) => postRequest('/api/v1/consume/mobile/device/register', data),

  /**
   * 设备心跳
   * @param {Object} data 心跳数据
   * @returns {Promise}
   */
  deviceHeartbeat: (data) => postRequest('/api/v1/consume/mobile/device/heartbeat', data),

  /**
   * 获取设备配置
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getDeviceConfig: (deviceId) => getRequest(`/api/v1/consume/mobile/device/config/${deviceId}`),

  /**
   * 更新设备配置
   * @param {Object} data 配置数据
   * @returns {Promise}
   */
  updateDeviceConfig: (data) => putRequest('/api/v1/consume/mobile/device/config', data),

  /**
   * 获取设备令牌
   * @param {Object} data 令牌请求
   * @returns {Promise}
   */
  getDeviceToken: (data) => postRequest('/api/v1/consume/mobile/device/token', data)
}

// 权限验证相关接口
export const permissionApi = {
  /**
   * 验证消费权限（移动端专用）
   * @param {Object} data 验证数据
   * @returns {Promise}
   */
  validateConsumePermission: (data) => postRequest('/api/v1/consume/mobile/validate/permission', data),

  /**
   * 验证消费权限（通用）
   * @param {Object} data 验证数据
   * @returns {Promise}
   */
  validatePermission: (data) => postRequest('/api/v1/consume/mobile/permission/validate', data)
}

// 异常处理相关接口
export const exceptionApi = {
  /**
   * 上报设备异常（移动端专用）
   * @param {Object} data 异常数据
   * @returns {Promise}
   */
  reportDeviceException: (data) => postRequest('/api/v1/consume/mobile/device/exception', data),

  /**
   * 处理交易异常（移动端专用）
   * @param {Object} data 处理数据
   * @returns {Promise}
   */
  handleTransactionException: (data) => postRequest('/api/v1/consume/mobile/transaction/handle-exception', data),

  /**
   * 上报交易异常（通用）
   * @param {Object} data 异常数据
   * @returns {Promise}
   */
  reportException: (data) => postRequest('/api/v1/consume/mobile/exception/report', data),

  /**
   * 处理交易异常（通用）
   * @param {Object} data 处理数据
   * @returns {Promise}
   */
  handleException: (data) => postRequest('/api/v1/consume/mobile/exception/handle', data)
}

// 导出所有API
export default {
  ...transactionApi,
  ...accountApi,
  ...historyApi,
  ...mealApi,
  ...statsApi,
  ...syncApi,
  ...deviceApi,
  ...permissionApi,
  ...exceptionApi
}

