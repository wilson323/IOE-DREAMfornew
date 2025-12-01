/**
 * SmartPermission 智能权限管理 API
 * 基于现有 SecurityLevelPermissionService 的前端API封装
 */

import { request } from '@/utils/request'

// API 基础路径
const BASE_PATH = '/api/smart-permission'

/**
 * 权限管理 API
 */
export const permissionApi = {
  /**
   * 获取用户权限概览
   * @param {number} userId 用户ID
   * @returns {Promise} 用户权限概览数据
   */
  getUserPermissionOverview(userId) {
    return request({
      url: `${BASE_PATH}/user/${userId}/overview`,
      method: 'GET'
    })
  },

  /**
   * 检查用户权限
   * @param {Object} data 权限检查数据
   * @returns {Promise} 权限检查结果
   */
  checkPermission(data) {
    return request({
      url: `${BASE_PATH}/check`,
      method: 'POST',
      data
    })
  },

  /**
   * 检查数据权限
   * @param {Object} data 数据权限检查数据
   * @returns {Promise} 数据权限检查结果
   */
  checkDataPermission(data) {
    return request({
      url: `${BASE_PATH}/data/check`,
      method: 'POST',
      data
    })
  },

  /**
   * 授予区域权限
   * @param {Object} data 区域权限数据
   * @returns {Promise} 授予结果
   */
  grantAreaPermission(data) {
    return request({
      url: `${BASE_PATH}/area/grant`,
      method: 'POST',
      data
    })
  },

  /**
   * 授予设备权限
   * @param {Object} data 设备权限数据
   * @returns {Promise} 授予结果
   */
  grantDevicePermission(data) {
    return request({
      url: `${BASE_PATH}/device/grant`,
      method: 'POST',
      data
    })
  },

  /**
   * 授予考勤权限
   * @param {Object} data 考勤权限数据
   * @returns {Promise} 授予结果
   */
  grantAttendancePermission(data) {
    return request({
      url: `${BASE_PATH}/attendance/grant`,
      method: 'POST',
      data
    })
  },

  /**
   * 授予门禁权限
   * @param {Object} data 门禁权限数据
   * @returns {Promise} 授予结果
   */
  grantAccessPermission(data) {
    return request({
      url: `${BASE_PATH}/access/grant`,
      method: 'POST',
      data
    })
  },

  /**
   * 授予用户安全级别
   * @param {Object} data 安全级别数据
   * @returns {Promise} 授予结果
   */
  grantUserSecurityLevel(data) {
    return request({
      url: `${BASE_PATH}/security-level/grant`,
      method: 'POST',
      data
    })
  },

  /**
   * 撤销用户权限
   * @param {Object} data 撤销权限数据
   * @returns {Promise} 撤销结果
   */
  revokeUserPermission(data) {
    return request({
      url: `${BASE_PATH}/revoke`,
      method: 'POST',
      data
    })
  },

  /**
   * 清除用户权限缓存
   * @param {number} userId 用户ID
   * @returns {Promise} 清除结果
   */
  clearUserPermissionCache(userId) {
    return request({
      url: `${BASE_PATH}/cache/clear/${userId}`,
      method: 'POST'
    })
  },

  /**
   * 清除所有权限缓存
   * @returns {Promise} 清除结果
   */
  clearAllPermissionCache() {
    return request({
      url: `${BASE_PATH}/cache/clear-all`,
      method: 'POST'
    })
  },

  /**
   * 获取统计数据
   * @returns {Promise} 统计数据
   */
  getStatistics() {
    return request({
      url: `${BASE_PATH}/statistics`,
      method: 'GET'
    })
  }
}

/**
 * 用户权限管理 API
 */
export const userPermissionApi = {
  /**
   * 获取用户列表
   * @param {Object} params 查询参数
   * @returns {Promise} 用户列表
   */
  getUserList(params) {
    return request({
      url: `${BASE_PATH}/users`,
      method: 'GET',
      params
    })
  },

  /**
   * 获取用户详情
   * @param {number} userId 用户ID
   * @returns {Promise} 用户详情
   */
  getUserDetail(userId) {
    return request({
      url: `${BASE_PATH}/users/${userId}`,
      method: 'GET'
    })
  },

  /**
   * 获取用户权限列表
   * @param {number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise} 用户权限列表
   */
  getUserPermissions(userId, params) {
    return request({
      url: `${BASE_PATH}/users/${userId}/permissions`,
      method: 'GET',
      params
    })
  },

  /**
   * 批量分配权限
   * @param {Object} data 批量分配数据
   * @returns {Promise} 分配结果
   */
  batchGrantPermissions(data) {
    return request({
      url: `${BASE_PATH}/users/batch-grant`,
      method: 'POST',
      data
    })
  },

  /**
   * 批量撤销权限
   * @param {Object} data 批量撤销数据
   * @returns {Promise} 撤销结果
   */
  batchRevokePermissions(data) {
    return request({
      url: `${BASE_PATH}/users/batch-revoke`,
      method: 'POST',
      data
    })
  }
}

/**
 * 安全级别管理 API
 */
export const securityLevelApi = {
  /**
   * 获取安全级别列表
   * @returns {Promise} 安全级别列表
   */
  getSecurityLevels() {
    return request({
      url: `${BASE_PATH}/security-levels`,
      method: 'GET'
    })
  },

  /**
   * 获取用户安全级别
   * @param {number} userId 用户ID
   * @returns {Promise} 用户安全级别
   */
  getUserSecurityLevel(userId) {
    return request({
      url: `${BASE_PATH}/users/${userId}/security-level`,
      method: 'GET'
    })
  },

  /**
   * 更新用户安全级别
   * @param {number} userId 用户ID
   * @param {Object} data 安全级别数据
   * @returns {Promise} 更新结果
   */
  updateUserSecurityLevel(userId, data) {
    return request({
      url: `${BASE_PATH}/users/${userId}/security-level`,
      method: 'PUT',
      data
    })
  }
}

/**
 * 权限审计 API
 */
export const permissionAuditApi = {
  /**
   * 获取权限审计日志
   * @param {Object} params 查询参数
   * @returns {Promise} 审计日志列表
   */
  getAuditLogs(params) {
    return request({
      url: `${BASE_PATH}/audit/logs`,
      method: 'GET',
      params
    })
  },

  /**
   * 获取权限操作统计
   * @param {Object} params 查询参数
   * @returns {Promise} 操作统计数据
   */
  getOperationStatistics(params) {
    return request({
      url: `${BASE_PATH}/audit/statistics`,
      method: 'GET',
      params
    })
  },

  /**
   * 导出审计日志
   * @param {Object} params 查询参数
   * @returns {Promise} 导出文件
   */
  exportAuditLogs(params) {
    return request({
      url: `${BASE_PATH}/audit/export`,
      method: 'GET',
      params,
      responseType: 'blob'
    })
  }
}

/**
 * 业务权限配置 API
 */
export const businessPermissionApi = {
  /**
   * 获取区域权限配置
   * @param {Object} params 查询参数
   * @returns {Promise} 区域权限配置
   */
  getAreaPermissionConfig(params) {
    return request({
      url: `${BASE_PATH}/config/area`,
      method: 'GET',
      params
    })
  },

  /**
   * 获取设备权限配置
   * @param {Object} params 查询参数
   * @returns {Promise} 设备权限配置
   */
  getDevicePermissionConfig(params) {
    return request({
      url: `${BASE_PATH}/config/device`,
      method: 'GET',
      params
    })
  },

  /**
   * 获取考勤权限配置
   * @param {Object} params 查询参数
   * @returns {Promise} 考勤权限配置
   */
  getAttendancePermissionConfig(params) {
    return request({
      url: `${BASE_PATH}/config/attendance`,
      method: 'GET',
      params
    })
  },

  /**
   * 获取门禁权限配置
   * @param {Object} params 查询参数
   * @returns {Promise} 门禁权限配置
   */
  getAccessPermissionConfig(params) {
    return request({
      url: `${BASE_PATH}/config/access`,
      method: 'GET',
      params
    })
  }
}

// 导出默认对象
export default {
  permission: permissionApi,
  userPermission: userPermissionApi,
  securityLevel: securityLevelApi,
  audit: permissionAuditApi,
  config: businessPermissionApi
}