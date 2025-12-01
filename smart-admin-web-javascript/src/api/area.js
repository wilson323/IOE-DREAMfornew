import { request } from '@/utils/request'

// 区域管理API
export const areaApi = {
  // 获取区域列表
  getAreaList(params) {
    return request({
      url: '/area/list',
      method: 'get',
      params
    })
  },

  // 获取区域详情
  getAreaDetail(areaId) {
    return request({
      url: `/area/${areaId}`,
      method: 'get'
    })
  },

  // 创建区域
  createArea(data) {
    return request({
      url: '/area',
      method: 'post',
      data
    })
  },

  // 更新区域
  updateArea(areaId, data) {
    return request({
      url: `/area/${areaId}`,
      method: 'put',
      data
    })
  },

  // 删除区域
  deleteArea(areaId) {
    return request({
      url: `/area/${areaId}`,
      method: 'delete'
    })
  },

  // 批量删除区域
  batchDelete(areaIds) {
    return request({
      url: '/area/batch/delete',
      method: 'delete',
      data: { areaIds }
    })
  },

  // 批量更新状态
  batchUpdateStatus(areaIds, status) {
    return request({
      url: '/area/batch/status',
      method: 'put',
      data: { areaIds, status }
    })
  },

  // 获取区域树
  getAreaTree() {
    return request({
      url: '/area/tree',
      method: 'get'
    })
  },

  // 获取子区域
  getChildAreas(parentAreaId) {
    return request({
      url: `/area/${parentAreaId}/children`,
      method: 'get'
    })
  },

  // 获取区域路径
  getAreaPath(areaId) {
    return request({
      url: `/area/${areaId}/path`,
      method: 'get'
    })
  },

  // 移动区域
  moveArea(areaId, parentAreaId) {
    return request({
      url: `/area/${areaId}/move`,
      method: 'put',
      data: { parentAreaId }
    })
  },

  // 复制区域结构
  copyAreaStructure(sourceAreaId, targetParentAreaId, areaName) {
    return request({
      url: '/area/copy',
      method: 'post',
      data: {
        sourceAreaId,
        targetParentAreaId,
        areaName
      }
    })
  },

  // 获取区域统计信息
  getAreaStatistics(areaId) {
    return request({
      url: `/area/${areaId}/statistics`,
      method: 'get'
    })
  },

  // 获取总体统计
  getOverallStatistics() {
    return request({
      url: '/area/statistics/overall',
      method: 'get'
    })
  },

  // 获取层级分布统计
  getLevelDistribution() {
    return request({
      url: '/area/statistics/level',
      method: 'get'
    })
  },

  // 获取类型分布统计
  getTypeDistribution() {
    return request({
      url: '/area/statistics/type',
      method: 'get'
    })
  },

  // 获取设备分布统计
  getDeviceDistribution() {
    return request({
      url: '/area/statistics/device',
      method: 'get'
    })
  },

  // 获取用户分布统计
  getUserDistribution() {
    return request({
      url: '/area/statistics/user',
      method: 'get'
    })
  },

  // 获取访问统计
  getAccessStatistics() {
    return request({
      url: '/area/statistics/access',
      method: 'get'
    })
  },

  // 获取地理分布统计
  getGeographicDistribution() {
    return request({
      url: '/area/statistics/geographic',
      method: 'get'
    })
  },

  // 获取状态统计
  getStatusStatistics() {
    return request({
      url: '/area/statistics/status',
      method: 'get'
    })
  },

  // 获取增长趋势
  getGrowthTrend() {
    return request({
      url: '/area/statistics/growth',
      method: 'get'
    })
  },

  // 获取活动热力图数据
  getActivityHeatmap() {
    return request({
      url: '/area/statistics/heatmap',
      method: 'get'
    })
  },

  // 获取地图区域数据
  getMapAreas(params) {
    return request({
      url: '/area/map/areas',
      method: 'get',
      params
    })
  },

  // 根据坐标搜索区域
  searchByCoordinates(latitude, longitude, radius) {
    return request({
      url: '/area/map/search',
      method: 'get',
      params: { latitude, longitude, radius }
    })
  },

  // 获取区域地理信息
  getAreaGeographicInfo(areaId) {
    return request({
      url: `/area/${areaId}/geographic`,
      method: 'get'
    })
  },

  // 获取热力图数据
  getHeatmapData(params) {
    return request({
      url: '/area/map/heatmap',
      method: 'get',
      params
    })
  },

  // 获取聚类数据
  getClusterData(minLat, maxLat, minLng, maxLng, zoomLevel) {
    return request({
      url: '/area/map/cluster',
      method: 'get',
      params: {
        minLatitude: minLat,
        maxLatitude: maxLat,
        minLongitude: minLng,
        maxLongitude: maxLng,
        zoomLevel
      }
    })
  },

  // 计算两点间距离
  calculateDistance(lat1, lng1, lat2, lng2) {
    return request({
      url: '/area/map/distance',
      method: 'get',
      params: { lat1, lng1, lat2, lng2 }
    })
  },

  // 检查点是否在多边形内
  checkPointInPolygon(pointLat, pointLng, areaBounds) {
    return request({
      url: '/area/map/point-in-polygon',
      method: 'post',
      data: { pointLat, pointLng, areaBounds }
    })
  },

  // 检查点是否在圆形区域内
  checkPointInCircle(pointLat, pointLng, centerLat, centerLng, radius) {
    return request({
      url: '/area/map/point-in-circle',
      method: 'post',
      data: { pointLat, pointLng, centerLat, centerLng, radius }
    })
  }
}

// 区域权限API
export const areaPermissionApi = {
  // 检查区域权限
  checkPermission(userId, areaId, permissionCode) {
    return request({
      url: '/area/permission/check',
      method: 'get',
      params: { userId, areaId, permissionCode }
    })
  },

  // 获取用户可访问区域
  getUserAccessibleAreas(userId) {
    return request({
      url: `/area/permission/user/${userId}/areas`,
      method: 'get'
    })
  },

  // 授予区域权限
  grantPermission(data) {
    return request({
      url: '/area/permission/grant',
      method: 'post',
      data
    })
  },

  // 撤销区域权限
  revokePermission(userId, areaId, permissionCode) {
    return request({
      url: '/area/permission/revoke',
      method: 'delete',
      data: { userId, areaId, permissionCode }
    })
  },

  // 拒绝区域权限
  denyPermission(data) {
    return request({
      url: '/area/permission/deny',
      method: 'post',
      data
    })
  },

  // 批量授予权限
  batchGrantPermission(data) {
    return request({
      url: '/area/permission/batch/grant',
      method: 'post',
      data
    })
  },

  // 批量撤销权限
  batchRevokePermission(data) {
    return request({
      url: '/area/permission/batch/revoke',
      method: 'delete',
      data
    })
  },

  // 授予区域树权限
  grantTreePermission(data) {
    return request({
      url: '/area/permission/tree/grant',
      method: 'post',
      data
    })
  },

  // 撤销区域树权限
  revokeTreePermission(data) {
    return request({
      url: '/area/permission/tree/revoke',
      method: 'delete',
      data
    })
  },

  // 复制权限
  copyPermissions(data) {
    return request({
      url: '/area/permission/copy',
      method: 'post',
      data
    })
  },

  // 获取用户权限统计
  getUserPermissionStatistics(userId) {
    return request({
      url: `/area/permission/user/${userId}/statistics`,
      method: 'get'
    })
  },

  // 获取权限矩阵
  getPermissionMatrix(areaIds, userIds) {
    return request({
      url: '/area/permission/matrix',
      method: 'post',
      data: { areaIds, userIds }
    })
  },

  // 检测权限冲突
  detectPermissionConflicts() {
    return request({
      url: '/area/permission/conflicts',
      method: 'get'
    })
  },

  // 清理过期权限
  cleanupExpiredPermissions() {
    return request({
      url: '/area/permission/cleanup',
      method: 'delete'
    })
  }
}

// 区域设备关联API
export const areaDeviceApi = {
  // 获取区域设备列表
  getAreaDevices(areaId, params) {
    return request({
      url: `/area/${areaId}/devices`,
      method: 'get',
      params
    })
  },

  // 添加设备到区域
  addDeviceToArea(areaId, deviceId) {
    return request({
      url: `/area/${areaId}/device/${deviceId}`,
      method: 'post'
    })
  },

  // 从区域移除设备
  removeDeviceFromArea(areaId, deviceId) {
    return request({
      url: `/area/${areaId}/device/${deviceId}`,
      method: 'delete'
    })
  },

  // 批量添加设备到区域
  batchAddDevices(areaId, deviceIds) {
    return request({
      url: `/area/${areaId}/devices/batch`,
      method: 'post',
      data: { deviceIds }
    })
  },

  // 批量移除设备
  batchRemoveDevices(areaId, deviceIds) {
    return request({
      url: `/area/${areaId}/devices/batch`,
      method: 'delete',
      data: { deviceIds }
    })
  },

  // 移动设备到其他区域
  moveDevice(deviceId, fromAreaId, toAreaId) {
    return request({
      url: '/area/device/move',
      method: 'put',
      data: { deviceId, fromAreaId, toAreaId }
    })
  }
}

// 区域用户关联API
export const areaUserApi = {
  // 获取区域用户列表
  getAreaUsers(areaId, params) {
    return request({
      url: `/area/${areaId}/users`,
      method: 'get',
      params
    })
  },

  // 添加用户到区域
  addUserToArea(areaId, userId) {
    return request({
      url: `/area/${areaId}/user/${userId}`,
      method: 'post'
    })
  },

  // 从区域移除用户
  removeUserFromArea(areaId, userId) {
    return request({
      url: `/area/${areaId}/user/${userId}`,
      method: 'delete'
    })
  },

  // 批量添加用户到区域
  batchAddUsers(areaId, userIds) {
    return request({
      url: `/area/${areaId}/users/batch`,
      method: 'post',
      data: { userIds }
    })
  },

  // 批量移除用户
  batchRemoveUsers(areaId, userIds) {
    return request({
      url: `/area/${areaId}/users/batch`,
      method: 'delete',
      data: { userIds }
    })
  },

  // 移动用户到其他区域
  moveUser(userId, fromAreaId, toAreaId) {
    return request({
      url: '/area/user/move',
      method: 'put',
      data: { userId, fromAreaId, toAreaId }
    })
  }
}