/**
 * 门禁权限管理 API
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */

import request from '@/utils/request'

/**
 * 获取用户权限列表
 */
export const getPermissionList = (params) => {
  return request.get('/api/v1/mobile/access/permission/list', { params })
}

/**
 * 获取权限统计
 */
export const getPermissionStatistics = () => {
  return request.get('/api/v1/mobile/access/permission/statistics')
}

/**
 * 获取权限详情
 */
export const getPermissionDetail = (permissionId) => {
  return request.get(`/api/v1/mobile/access/permission/${permissionId}`)
}

/**
 * 获取权限二维码
 */
export const getPermissionQRCode = (permissionId) => {
  return request.get(`/api/v1/mobile/access/permission/${permissionId}/qrcode`)
}

/**
 * 获取通行记录
 */
export const getPermissionRecords = (permissionId, params) => {
  return request.get(`/api/v1/mobile/access/permission/${permissionId}/records`, { params })
}

/**
 * 获取权限历史
 */
export const getPermissionHistory = (permissionId) => {
  return request.get(`/api/v1/mobile/access/permission/${permissionId}/history`)
}

/**
 * 续期权限
 */
export const renewPermission = (permissionId, data) => {
  return request.post(`/api/v1/mobile/access/permission/${permissionId}/renew`, data)
}

/**
 * 转移权限
 */
export const transferPermission = (permissionId, data) => {
  return request.post(`/api/v1/mobile/access/permission/${permissionId}/transfer`, data)
}

/**
 * 冻结权限
 */
export const freezePermission = (permissionId) => {
  return request.post(`/api/v1/mobile/access/permission/${permissionId}/freeze`)
}

/**
 * 解冻权限
 */
export const unfreezePermission = (permissionId) => {
  return request.post(`/api/v1/mobile/access/permission/${permissionId}/unfreeze`)
}

/**
 * 获取过期统计
 */
export const getExpiringStatistics = () => {
  return request.get('/api/v1/mobile/access/permission/expiring/statistics')
}

/**
 * 获取即将过期列表
 */
export const getExpiringPermissions = (params) => {
  return request.get('/api/v1/mobile/access/permission/expiring/list', { params })
}

/**
 * 批量续期
 */
export const batchRenewPermissions = (data) => {
  return request.post('/api/v1/mobile/access/permission/batch-renew', data)
}

/**
 * 离线同步 - 获取同步数据
 */
export const getOfflineSyncData = (params) => {
  return request.get('/api/v1/mobile/access/offline/sync-data', { params })
}

/**
 * 离线同步 - 上传离线记录
 */
export const uploadOfflineRecords = (data) => {
  return request.post('/api/v1/mobile/access/offline/upload-records', data)
}

/**
 * 离线同步 - 获取同步状态
 */
export const getSyncStatus = () => {
  return request.get('/api/v1/mobile/access/offline/sync-status')
}

/**
 * 离线同步 - 立即同步
 */
export const syncNow = (data) => {
  return request.post('/api/v1/mobile/access/offline/sync-now', data)
}

export default {
  getPermissionList,
  getPermissionStatistics,
  getPermissionDetail,
  getPermissionQRCode,
  getPermissionRecords,
  getPermissionHistory,
  renewPermission,
  transferPermission,
  freezePermission,
  unfreezePermission,
  getExpiringStatistics,
  getExpiringPermissions,
  batchRenewPermissions,
  getOfflineSyncData,
  uploadOfflineRecords,
  getSyncStatus,
  syncNow
}
