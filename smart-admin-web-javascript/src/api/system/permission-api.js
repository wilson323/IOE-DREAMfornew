/**
 * 权限管理API接口
 * IOE-DREAM智慧园区一卡通管理平台
 */

import { postRequest, getRequest, putRequest, deleteRequest } from '/@/lib/axios';

// ========================================
// 权限基础API
// ========================================

/**
 * 获取权限树结构
 * @param {Object} params - 查询参数
 * @returns {Promise} 权限树数据
 */
export const getPermissionTree = (params) => {
  return postRequest('/system/permission/tree', params);
};

/**
 * 获取权限列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 权限列表数据
 */
export const getPermissionList = (params) => {
  return postRequest('/system/permission/list', params);
};

/**
 * 根据ID获取权限详情
 * @param {Number} id - 权限ID
 * @returns {Promise} 权限详情数据
 */
export const getPermissionById = (id) => {
  return getRequest(`/system/permission/detail/${id}`);
};

/**
 * 创建权限
 * @param {Object} data - 权限数据
 * @returns {Promise} 创建结果
 */
export const createPermission = (data) => {
  return postRequest('/system/permission/create', data);
};

/**
 * 更新权限
 * @param {Object} data - 权限数据
 * @returns {Promise} 更新结果
 */
export const updatePermission = (data) => {
  return putRequest('/system/permission/update', data);
};

/**
 * 删除权限
 * @param {Number} id - 权限ID
 * @returns {Promise} 删除结果
 */
export const deletePermission = (id) => {
  return deleteRequest(`/system/permission/delete/${id}`);
};

/**
 * 批量删除权限
 * @param {Array} ids - 权限ID数组
 * @returns {Promise} 批量删除结果
 */
export const batchDeletePermissions = (ids) => {
  return postRequest('/system/permission/batchDelete', { ids });
};

// ========================================
// 用户权限分配API
// ========================================

/**
 * 获取用户权限列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 用户权限列表数据
 */
export const getUserPermissionList = (params) => {
  return postRequest('/system/userPermission/list', params);
};

/**
 * 获取用户权限详情
 * @param {Number} userId - 用户ID
 * @returns {Promise} 用户权限详情数据
 */
export const getUserPermissionDetail = (userId) => {
  return getRequest(`/system/userPermission/detail/${userId}`);
};

/**
 * 分配用户权限
 * @param {Object} data - 权限分配数据
 * @returns {Promise} 分配结果
 */
export const assignUserPermissions = (data) => {
  return postRequest('/system/userPermission/assign', data);
};

/**
 * 撤销用户权限
 * @param {Object} data - 撤销权限数据
 * @returns {Promise} 撤销结果
 */
export const revokeUserPermissions = (data) => {
  return postRequest('/system/userPermission/revoke', data);
};

/**
 * 批量分配用户权限
 * @param {Object} data - 批量分配数据
 * @returns {Promise} 批量分配结果
 */
export const batchAssignUserPermissions = (data) => {
  return postRequest('/system/userPermission/batchAssign', data);
};

/**
 * 批量撤销用户权限
 * @param {Object} data - 批量撤销数据
 * @returns {Promise} 批量撤销结果
 */
export const batchRevokeUserPermissions = (data) => {
  return postRequest('/system/userPermission/batchRevoke', data);
};

/**
 * 更新用户权限
 * @param {Object} data - 更新数据
 * @returns {Promise} 更新结果
 */
export const updateUserPermission = (data) => {
  return putRequest('/system/userPermission/update', data);
};

/**
 * 获取用户权限统计
 * @param {Number} userId - 用户ID
 * @returns {Promise} 权限统计数据
 */
export const getUserPermissionStatistics = (userId) => {
  return getRequest(`/system/userPermission/statistics/${userId}`);
};

// ========================================
// 权限模板API
// ========================================

/**
 * 获取权限模板列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 权限模板列表数据
 */
export const getPermissionTemplateList = (params) => {
  return postRequest('/system/permissionTemplate/list', params);
};

/**
 * 获取权限模板详情
 * @param {Number} id - 模板ID
 * @returns {Promise} 权限模板详情数据
 */
export const getPermissionTemplateById = (id) => {
  return getRequest(`/system/permissionTemplate/detail/${id}`);
};

/**
 * 创建权限模板
 * @param {Object} data - 模板数据
 * @returns {Promise} 创建结果
 */
export const createPermissionTemplate = (data) => {
  return postRequest('/system/permissionTemplate/create', data);
};

/**
 * 更新权限模板
 * @param {Object} data - 模板数据
 * @returns {Promise} 更新结果
 */
export const updatePermissionTemplate = (data) => {
  return putRequest('/system/permissionTemplate/update', data);
};

/**
 * 删除权限模板
 * @param {Number} id - 模板ID
 * @returns {Promise} 删除结果
 */
export const deletePermissionTemplate = (id) => {
  return deleteRequest(`/system/permissionTemplate/delete/${id}`);
};

/**
 * 应用权限模板
 * @param {Object} data - 应用数据
 * @returns {Promise} 应用结果
 */
export const applyPermissionTemplate = (data) => {
  return postRequest('/system/permissionTemplate/apply', data);
};

/**
 * 获取模板权限列表
 * @param {Number} templateId - 模板ID
 * @returns {Promise} 模板权限列表数据
 */
export const getTemplatePermissions = (templateId) => {
  return getRequest(`/system/permissionTemplate/permissions/${templateId}`);
};

/**
 * 预览权限模板应用效果
 * @param {Object} data - 预览数据
 * @returns {Promise} 预览结果
 */
export const previewPermissionTemplate = (data) => {
  return postRequest('/system/permissionTemplate/preview', data);
};

// ========================================
// 权限继承API
// ========================================

/**
 * 获取权限继承配置
 * @param {Object} params - 查询参数
 * @returns {Promise} 权限继承配置数据
 */
export const getPermissionInheritanceConfig = (params) => {
  return postRequest('/system/permissionInheritance/config', params);
};

/**
 * 设置权限继承配置
 * @param {Object} data - 配置数据
 * @returns {Promise} 设置结果
 */
export const setPermissionInheritanceConfig = (data) => {
  return postRequest('/system/permissionInheritance/setConfig', data);
};

/**
 * 获取继承的权限
 * @param {Number} userId - 用户ID
 * @returns {Promise} 继承权限数据
 */
export const getInheritedPermissions = (userId) => {
  return getRequest(`/system/permissionInheritance/inherited/${userId}`);
};

/**
 * 启用/禁用权限继承
 * @param {Object} data - 继承设置数据
 * @returns {Promise} 设置结果
 */
export const togglePermissionInheritance = (data) => {
  return postRequest('/system/permissionInheritance/toggle', data);
};

/**
 * 重新计算权限继承
 * @param {Number} userId - 用户ID
 * @returns {Promise} 计算结果
 */
export const recalculatePermissionInheritance = (userId) => {
  return postRequest('/system/permissionInheritance/recalculate', { userId });
};

// ========================================
// 时间策略API
// ========================================

/**
 * 获取时间策略列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 时间策略列表数据
 */
export const getTimeStrategyList = (params) => {
  return postRequest('/system/timeStrategy/list', params);
};

/**
 * 获取时间策略详情
 * @param {Number} id - 策略ID
 * @returns {Promise} 时间策略详情数据
 */
export const getTimeStrategyById = (id) => {
  return getRequest(`/system/timeStrategy/detail/${id}`);
};

/**
 * 创建时间策略
 * @param {Object} data - 策略数据
 * @returns {Promise} 创建结果
 */
export const createTimeStrategy = (data) => {
  return postRequest('/system/timeStrategy/create', data);
};

/**
 * 更新时间策略
 * @param {Object} data - 策略数据
 * @returns {Promise} 更新结果
 */
export const updateTimeStrategy = (data) => {
  return putRequest('/system/timeStrategy/update', data);
};

/**
 * 删除时间策略
 * @param {Number} id - 策略ID
 * @returns {Promise} 删除结果
 */
export const deleteTimeStrategy = (id) => {
  return deleteRequest(`/system/timeStrategy/delete/${id}`);
};

/**
 * 验证时间策略
 * @param {Object} data - 策略数据
 * @returns {Promise} 验证结果
 */
export const validateTimeStrategy = (data) => {
  return postRequest('/system/timeStrategy/validate', data);
};

/**
 * 获取时间策略使用统计
 * @param {Number} strategyId - 策略ID
 * @returns {Promise} 使用统计数据
 */
export const getTimeStrategyUsage = (strategyId) => {
  return getRequest(`/system/timeStrategy/usage/${strategyId}`);
};

// ========================================
// 地理围栏权限API
// ========================================

/**
 * 获取地理围栏列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 地理围栏列表数据
 */
export const getGeoFenceList = (params) => {
  return postRequest('/system/geoFence/list', params);
};

/**
 * 获取地理围栏详情
 * @param {Number} id - 围栏ID
 * @returns {Promise} 地理围栏详情数据
 */
export const getGeoFenceById = (id) => {
  return getRequest(`/system/geoFence/detail/${id}`);
};

/**
 * 创建地理围栏
 * @param {Object} data - 围栏数据
 * @returns {Promise} 创建结果
 */
export const createGeoFence = (data) => {
  return postRequest('/system/geoFence/create', data);
};

/**
 * 更新地理围栏
 * @param {Object} data - 围栏数据
 * @returns {Promise} 更新结果
 */
export const updateGeoFence = (data) => {
  return putRequest('/system/geoFence/update', data);
};

/**
 * 删除地理围栏
 * @param {Number} id - 围栏ID
 * @returns {Promise} 删除结果
 */
export const deleteGeoFence = (id) => {
  return deleteRequest(`/system/geoFence/delete/${id}`);
};

/**
 * 验证地理围栏
 * @param {Object} data - 围栏数据
 * @returns {Promise} 验证结果
 */
export const validateGeoFence = (data) => {
  return postRequest('/system/geoFence/validate', data);
};

/**
 * 检查用户是否在地理围栏内
 * @param {Object} data - 检查数据
 * @returns {Promise} 检查结果
 */
export const checkUserInGeoFence = (data) => {
  return postRequest('/system/geoFence/check', data);
};

/**
 * 获取用户位置历史
 * @param {Number} userId - 用户ID
 * @param {Object} params - 查询参数
 * @returns {Promise} 位置历史数据
 */
export const getUserLocationHistory = (userId, params) => {
  return postRequest(`/system/geoFence/locationHistory/${userId}`, params);
};

// ========================================
// 设备权限API
// ========================================

/**
 * 获取设备权限列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 设备权限列表数据
 */
export const getDevicePermissionList = (params) => {
  return postRequest('/system/devicePermission/list', params);
};

/**
 * 获取设备权限详情
 * @param {Number} id - 设备权限ID
 * @returns {Promise} 设备权限详情数据
 */
export const getDevicePermissionById = (id) => {
  return getRequest(`/system/devicePermission/detail/${id}`);
};

/**
 * 分配设备权限
 * @param {Object} data - 权限分配数据
 * @returns {Promise} 分配结果
 */
export const assignDevicePermission = (data) => {
  return postRequest('/system/devicePermission/assign', data);
};

/**
 * 撤销设备权限
 * @param {Object} data - 撤销数据
 * @returns {Promise} 撤销结果
 */
export const revokeDevicePermission = (data) => {
  return postRequest('/system/devicePermission/revoke', data);
};

/**
 * 获取用户设备权限
 * @param {Number} userId - 用户ID
 * @returns {Promise} 用户设备权限数据
 */
export const getUserDevicePermissions = (userId) => {
  return getRequest(`/system/devicePermission/user/${userId}`);
};

/**
 * 获取设备用户权限
 * @param {Number} deviceId - 设备ID
 * @returns {Promise} 设备用户权限数据
 */
export const getDeviceUserPermissions = (deviceId) => {
  return getRequest(`/system/devicePermission/device/${deviceId}`);
};

// ========================================
// 权限验证API
// ========================================

/**
 * 验证用户权限
 * @param {Object} data - 验证数据
 * @returns {Promise} 验证结果
 */
export const validateUserPermission = (data) => {
  return postRequest('/system/permission/validate', data);
};

/**
 * 批量验证用户权限
 * @param {Object} data - 批量验证数据
 * @returns {Promise} 批量验证结果
 */
export const batchValidateUserPermissions = (data) => {
  return postRequest('/system/permission/batchValidate', data);
};

/**
 * 获取用户所有有效权限
 * @param {Number} userId - 用户ID
 * @returns {Promise} 用户权限数据
 */
export const getUserEffectivePermissions = (userId) => {
  return getRequest(`/system/permission/effective/${userId}`);
};

/**
 * 检查权限是否过期
 * @param {Number} userId - 用户ID
 * @param {Array} permissionIds - 权限ID数组
 * @returns {Promise} 检查结果
 */
export const checkPermissionExpired = (userId, permissionIds) => {
  return postRequest('/system/permission/checkExpired', {
    userId,
    permissionIds
  });
};

/**
 * 刷新用户权限缓存
 * @param {Number} userId - 用户ID
 * @returns {Promise} 刷新结果
 */
export const refreshUserPermissionCache = (userId) => {
  return postRequest('/system/permission/refreshCache', { userId });
};

/**
 * 批量刷新权限缓存
 * @param {Array} userIds - 用户ID数组
 * @returns {Promise} 批量刷新结果
 */
export const batchRefreshPermissionCache = (userIds) => {
  return postRequest('/system/permission/batchRefreshCache', { userIds });
};

// ========================================
// 权限审计API
// ========================================

/**
 * 获取权限审计日志列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 审计日志列表数据
 */
export const getPermissionAuditLogList = (params) => {
  return postRequest('/system/permissionAudit/list', params);
};

/**
 * 获取权限审计日志详情
 * @param {Number} id - 日志ID
 * @returns {Promise} 审计日志详情数据
 */
export const getPermissionAuditLogById = (id) => {
  return getRequest(`/system/permissionAudit/detail/${id}`);
};

/**
 * 导出权限审计日志
 * @param {Object} params - 导出参数
 * @returns {Promise} 导出结果
 */
export const exportPermissionAuditLog = (params) => {
  return postRequest('/system/permissionAudit/export', params);
};

/**
 * 获取权限操作统计
 * @param {Object} params - 查询参数
 * @returns {Promise} 操作统计数据
 */
export const getPermissionOperationStatistics = (params) => {
  return postRequest('/system/permissionAudit/statistics', params);
};

/**
 * 获取权限变更趋势
 * @param {Object} params - 查询参数
 * @returns {Promise} 变更趋势数据
 */
export const getPermissionChangeTrend = (params) => {
  return postRequest('/system/permissionAudit/trend', params);
};

/**
 * 获取高风险权限操作
 * @param {Object} params - 查询参数
 * @returns {Promise} 高风险操作数据
 */
export const getHighRiskPermissionOperations = (params) => {
  return postRequest('/system/permissionAudit/highRisk', params);
};

// ========================================
// 权限统计API
// ========================================

/**
 * 获取权限总览统计
 * @returns {Promise} 权限总览数据
 */
export const getPermissionOverview = () => {
  return getRequest('/system/permission/statistics/overview');
};

/**
 * 获取安全级别分布统计
 * @returns {Promise} 安全级别分布数据
 */
export const getSecurityLevelDistribution = () => {
  return getRequest('/system/permission/statistics/securityLevel');
};

/**
 * 获取权限类型分布统计
 * @returns {Promise} 权限类型分布数据
 */
export const getPermissionTypeDistribution = () => {
  return getRequest('/system/permission/statistics/type');
};

/**
 * 获取部门权限统计
 * @returns {Promise} 部门权限统计数据
 */
export const getDepartmentPermissionStatistics = () => {
  return getRequest('/system/permission/statistics/department');
};

/**
 * 获取职位权限统计
 * @returns {Promise} 职位权限统计数据
 */
export const getPositionPermissionStatistics = () => {
  return getRequest('/system/permission/statistics/position');
};

/**
 * 获取权限使用率统计
 * @returns {Promise} 权限使用率数据
 */
export const getPermissionUsageStatistics = () => {
  return getRequest('/system/permission/statistics/usage');
};

/**
 * 获取权限过期预警
 * @param {Number} days - 预警天数
 * @returns {Promise} 过期预警数据
 */
export const getPermissionExpirationWarning = (days = 7) => {
  return getRequest(`/system/permission/statistics/expiration/${days}`);
};

/**
 * 获取权限分配趋势
 * @param {Object} params - 查询参数
 * @returns {Promise} 分配趋势数据
 */
export const getPermissionAssignmentTrend = (params) => {
  return postRequest('/system/permission/statistics/assignmentTrend', params);
};

// ========================================
// 权限配置API
// ========================================

/**
 * 获取权限系统配置
 * @returns {Promise} 系统配置数据
 */
export const getPermissionSystemConfig = () => {
  return getRequest('/system/permission/config/system');
};

/**
 * 更新权限系统配置
 * @param {Object} data - 配置数据
 * @returns {Promise} 更新结果
 */
export const updatePermissionSystemConfig = (data) => {
  return putRequest('/system/permission/config/system', data);
};

/**
 * 获取权限验证规则配置
 * @returns {Promise} 验证规则配置数据
 */
export const getPermissionValidationRules = () => {
  return getRequest('/system/permission/config/validationRules');
};

/**
 * 更新权限验证规则配置
 * @param {Object} data - 验证规则配置数据
 * @returns {Promise} 更新结果
 */
export const updatePermissionValidationRules = (data) => {
  return putRequest('/system/permission/config/validationRules', data);
};

/**
 * 重置权限系统配置
 * @returns {Promise} 重置结果
 */
export const resetPermissionSystemConfig = () => {
  return postRequest('/system/permission/config/reset');
};

/**
 * 获取权限缓存状态
 * @returns {Promise} 缓存状态数据
 */
export const getPermissionCacheStatus = () => {
  return getRequest('/system/permission/config/cacheStatus');
};

/**
 * 清理权限缓存
 * @param {Object} params - 清理参数
 * @returns {Promise} 清理结果
 */
export const clearPermissionCache = (params) => {
  return postRequest('/system/permission/config/clearCache', params);
};

// ========================================
// 导出API
// ========================================

/**
 * 导出权限数据
 * @param {Object} params - 导出参数
 * @returns {Promise} 导出结果
 */
export const exportPermissionData = (params) => {
  return postRequest('/system/permission/export', params);
};

/**
 * 导出用户权限数据
 * @param {Object} params - 导出参数
 * @returns {Promise} 导出结果
 */
export const exportUserPermissionData = (params) => {
  return postRequest('/system/userPermission/export', params);
};

/**
 * 导出权限模板数据
 * @param {Object} params - 导出参数
 * @returns {Promise} 导出结果
 */
export const exportPermissionTemplateData = (params) => {
  return postRequest('/system/permissionTemplate/export', params);
};

// ========================================
// 导入API
// ========================================

/**
 * 导入权限模板
 * @param {FormData} formData - 导入数据
 * @returns {Promise} 导入结果
 */
export const importPermissionTemplate = (formData) => {
  return postRequest('/system/permissionTemplate/import', formData);
};

/**
 * 预览权限模板导入
 * @param {FormData} formData - 预览数据
 * @returns {Promise} 预览结果
 */
export const previewPermissionTemplateImport = (formData) => {
  return postRequest('/system/permissionTemplate/previewImport', formData);
};

/**
 * 导入用户权限
 * @param {FormData} formData - 导入数据
 * @returns {Promise} 导入结果
 */
export const importUserPermissions = (formData) => {
  return postRequest('/system/userPermission/import', formData);
};

/**
 * 预览用户权限导入
 * @param {FormData} formData - 预览数据
 * @returns {Promise} 预览结果
 */
export const previewUserPermissionsImport = (formData) => {
  return postRequest('/system/userPermission/previewImport', formData);
};