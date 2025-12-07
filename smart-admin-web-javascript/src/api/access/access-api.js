/*
 * 门禁管理API
 *
 * @Author:    老王
 * @Date:      2025-12-01
 * @Wechat:    老王牛逼
 * @Copyright  IOE-DREAM （ https://ioe-dream.net ），Since 2025
 */

import { getRequest, postRequest } from '/@/lib/axios';

// ==================== 设备管理API ====================

/**
 * 获取设备列表
 */
export const getDeviceList = (params) => {
  return getRequest('/access/device/list', params);
};

/**
 * 获取设备详情
 */
export const getDeviceDetail = (deviceId) => {
  return getRequest(`/access/device/${deviceId}`);
};

/**
 * 控制设备
 */
export const controlDevice = (deviceId, data) => {
  return postRequest(`/access/device/${deviceId}/control`, data);
};

/**
 * 获取设备状态
 */
export const getDeviceStatus = (deviceId) => {
  return getRequest(`/access/device/${deviceId}/status`);
};

/**
 * 重启设备
 */
export const restartDevice = (deviceId) => {
  return postRequest(`/access/device/${deviceId}/restart`);
};

/**
 * 获取设备统计信息
 */
export const getDeviceStatistics = (params) => {
  return getRequest('/access/device/statistics', params);
};

// ==================== 区域管理API ====================

/**
 * 获取区域树
 */
export const getAreaTree = () => {
  return getRequest('/access/area/tree');
};

/**
 * 获取区域详情
 */
export const getAreaDetail = (areaId) => {
  return getRequest(`/access/area/${areaId}`);
};

/**
 * 创建区域
 */
export const createArea = (data) => {
  return postRequest('/access/area', data);
};

/**
 * 更新区域
 */
export const updateArea = (areaId, data) => {
  return postRequest(`/access/area/${areaId}`, data);
};

/**
 * 删除区域
 */
export const deleteArea = (areaId) => {
  return postRequest(`/access/area/delete/${areaId}`);
};

// ==================== 通行记录API ====================

/**
 * 获取通行记录列表
 */
export const getAccessRecordList = (params) => {
  return getRequest('/access/record/list', params);
};

/**
 * 获取最近通行记录
 */
export const getRecentRecords = (params) => {
  return getRequest('/access/record/recent', params);
};

/**
 * 获取通行统计
 */
export const getAccessStatistics = (params) => {
  return getRequest('/access/record/statistics', params);
};

// ==================== 实时监控API ====================

/**
 * 获取监控大屏数据
 */
export const getDashboardStats = () => {
  return getRequest('/access/monitor/dashboard');
};

/**
 * 获取实时事件流
 */
export const getRealtimeEvents = (params) => {
  return getRequest('/access/monitor/events', params);
};

/**
 * 获取在线设备
 */
export const getActiveDevices = () => {
  return getRequest('/access/monitor/active-devices');
};

/**
 * 获取紧急告警
 */
export const getEmergencyAlerts = (params) => {
  return getRequest('/access/monitor/emergency-alerts', params);
};

// ==================== 疏散管理API ====================

/**
 * 获取疏散点列表
 */
export const getEvacuationPoints = () => {
  return getRequest('/access/evacuation/points');
};

/**
 * 获取疏散点详情
 */
export const getEvacuationPointDetail = (pointId) => {
  return getRequest(`/access/evacuation/points/${pointId}`);
};

/**
 * 更新疏散点状态
 */
export const updateEvacuationPoint = (pointId, data) => {
  return postRequest(`/access/evacuation/points/${pointId}`, data);
};

// ==================== 高级功能API ====================

/**
 * 获取全局联动规则
 */
export const getGlobalLinkageRules = () => {
  return getRequest('/access/advanced/global-linkage/rules');
};

/**
 * 创建全局联动规则
 */
export const createGlobalLinkageRule = (data) => {
  return postRequest('/access/advanced/global-linkage/rules', data);
};

/**
 * 获取全局互锁规则
 */
export const getGlobalInterlockRules = () => {
  return getRequest('/access/advanced/global-interlock/rules');
};

/**
 * 创建全局互锁规则
 */
export const createGlobalInterlockRule = (data) => {
  return postRequest('/access/advanced/global-interlock/rules', data);
};

/**
 * 触发全局联动
 */
export const triggerGlobalLinkage = (ruleId, data) => {
  return postRequest(`/access/advanced/global-linkage/trigger/${ruleId}`, data);
};

/**
 * 触发全局互锁
 */
export const triggerGlobalInterlock = (ruleId, data) => {
  return postRequest(`/access/advanced/global-interlock/trigger/${ruleId}`, data);
};

// ==================== 生物识别API ====================

/**
 * 获取生物识别模板
 */
export const getBiometricTemplates = (userId) => {
  return getRequest(`/access/biometric/templates/${userId}`);
};

/**
 * 注册生物识别模板
 */
export const registerBiometricTemplate = (data) => {
  return postRequest('/access/biometric/register', data);
};

/**
 * 验证生物识别
 */
export const verifyBiometric = (data) => {
  return postRequest('/access/biometric/verify', data);
};

// ==================== 权限管理API ====================

/**
 * 获取用户权限
 */
export const getUserPermissions = (userId) => {
  return getRequest(`/access/permission/user/${userId}`);
};

/**
 * 分配权限
 */
export const assignPermissions = (data) => {
  return postRequest('/access/permission/assign', data);
};

/**
 * 检查权限
 */
export const checkPermission = (userId, permissionCode) => {
  return getRequest(`/access/permission/check`, { userId, permissionCode });
};

// ==================== 导出默认对象 ====================

export default {
  // 设备管理
  getDeviceList,
  getDeviceDetail,
  controlDevice,
  getDeviceStatus,
  restartDevice,
  getDeviceStatistics,

  // 区域管理
  getAreaTree,
  getAreaDetail,
  createArea,
  updateArea,
  deleteArea,

  // 通行记录
  getAccessRecordList,
  getRecentRecords,
  getAccessStatistics,

  // 实时监控
  getDashboardStats,
  getRealtimeEvents,
  getActiveDevices,
  getEmergencyAlerts,

  // 疏散管理
  getEvacuationPoints,
  getEvacuationPointDetail,
  updateEvacuationPoint,

  // 高级功能
  getGlobalLinkageRules,
  createGlobalLinkageRule,
  getGlobalInterlockRules,
  createGlobalInterlockRule,
  triggerGlobalLinkage,
  triggerGlobalInterlock,

  // 生物识别
  getBiometricTemplates,
  registerBiometricTemplate,
  verifyBiometric,

  // 权限管理
  getUserPermissions,
  assignPermissions,
  checkPermission,
};