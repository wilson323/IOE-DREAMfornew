/*
 * 门禁管理-配置API接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, postEncryptRequest } from '/@/lib/axios';

export const accessConfigApi = {
  /**
   * 获取门禁系统配置
   */
  getSystemConfig: () => {
    return getRequest('/access/config/system');
  },

  /**
   * 更新门禁系统配置
   */
  updateSystemConfig: (params) => {
    return postRequest('/access/config/system/update', params);
  },

  /**
   * 获取时间策略列表
   */
  getTimeStrategies: () => {
    return getRequest('/access/config/time/strategy/list');
  },

  /**
   * 创建时间策略
   */
  createTimeStrategy: (params) => {
    return postRequest('/access/config/time/strategy/create', params);
  },

  /**
   * 更新时间策略
   */
  updateTimeStrategy: (params) => {
    return postRequest('/access/config/time/strategy/update', params);
  },

  /**
   * 删除时间策略
   */
  deleteTimeStrategy: (strategyId) => {
    return getRequest(`/access/config/time/strategy/delete/${strategyId}`);
  },

  /**
   * 获取安防级别配置
   */
  getSecurityLevels: () => {
    return getRequest('/access/config/security/level/list');
  },

  /**
   * 创建安防级别
   */
  createSecurityLevel: (params) => {
    return postRequest('/access/config/security/level/create', params);
  },

  /**
   * 更新安防级别
   */
  updateSecurityLevel: (params) => {
    return postRequest('/access/config/security/level/update', params);
  },

  /**
   * 删除安防级别
   */
  deleteSecurityLevel: (levelId) => {
    return getRequest(`/access/config/security/level/delete/${levelId}`);
  },

  /**
   * 获取设备类型配置
   */
  getDeviceTypes: () => {
    return getRequest('/access/config/device/type/list');
  },

  /**
   * 创建设备类型
   */
  createDeviceType: (params) => {
    return postRequest('/access/config/device/type/create', params);
  },

  /**
   * 更新设备类型
   */
  updateDeviceType: (params) => {
    return postRequest('/access/config/device/type/update', params);
  },

  /**
   * 获取告警配置
   */
  getAlertConfig: () => {
    return getRequest('/access/config/alert');
  },

  /**
   * 更新告警配置
   */
  updateAlertConfig: (params) => {
    return postRequest('/access/config/alert/update', params);
  },

  /**
   * 获取日志配置
   */
  getLogConfig: () => {
    return getRequest('/access/config/log');
  },

  /**
   * 更新日志配置
   */
  updateLogConfig: (params) => {
    return postRequest('/access/config/log/update', params);
  },

  /**
   * 测试时间策略
   */
  testTimeStrategy: (params) => {
    return postRequest('/access/config/time/strategy/test', params);
  }
};