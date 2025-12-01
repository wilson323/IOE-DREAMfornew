/*
 * 门禁管理-区域API接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-11-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, postEncryptRequest } from '/@/lib/axios';

export const accessAreaApi = {
  /**
   * 获取区域树形结构
   */
  getAreaTree: () => {
    return getRequest('/api/smart/access/area/tree');
  },

  /**
   * 获取区域详情
   */
  getAreaDetail: (areaId) => {
    return getRequest(`/api/smart/access/area/detail/${areaId}`);
  },

  /**
   * 分页查询区域列表
   */
  getAreaPage: (params) => {
    return postRequest('/api/smart/access/area/page', params);
  },

  /**
   * 添加区域
   */
  addArea: (params) => {
    return postRequest('/api/smart/access/area/add', params);
  },

  /**
   * 更新区域
   */
  updateArea: (params) => {
    return postRequest('/api/smart/access/area/update', params);
  },

  /**
   * 删除区域
   */
  deleteArea: (areaId) => {
    return getRequest(`/api/smart/access/area/delete/${areaId}`);
  },

  /**
   * 批量删除区域
   */
  batchDeleteAreas: (areaIds) => {
    return postRequest('/api/smart/access/area/batch/delete', areaIds);
  },

  /**
   * 获取区域关联的设备
   */
  getAreaDevices: (areaId) => {
    return getRequest(`/api/smart/access/area/devices/${areaId}`);
  },

  /**
   * 为区域分配设备
   */
  assignDevices: (areaId, deviceIds) => {
    return postRequest('/api/smart/access/area/assign/devices', { areaId, deviceIds });
  },

  /**
   * 移动区域到指定父级
   */
  moveArea: (areaId, parentAreaId) => {
    return postRequest('/api/smart/access/area/move', { areaId, parentAreaId });
  },

  /**
   * 更新区域状态
   */
  updateAreaStatus: (areaId, status) => {
    return postRequest('/api/smart/access/area/status', { areaId, status });
  },

  /**
   * 验证区域编码唯一性
   */
  validateAreaCode: (areaCode, excludeId = null) => {
    return postRequest('/api/smart/access/area/validate/code', { areaCode, excludeId });
  },

  /**
   * 获取区域统计数据
   */
  getAreaStatistics: (areaId = null) => {
    const url = areaId ? `/api/smart/access/area/stats/${areaId}` : '/api/smart/access/area/stats';
    return getRequest(url);
  },

  /**
   * 获取区域权限配置
   */
  getAreaPermissionConfig: (areaId) => {
    return getRequest(`/api/smart/access/area/permission/config/${areaId}`);
  },

  /**
   * 更新区域权限配置
   */
  updateAreaPermissionConfig: (areaId, config) => {
    return postRequest('/api/smart/access/area/permission/config', { areaId, config });
  },

  /**
   * 获取区域访问记录统计
   */
  getAreaAccessStats: (areaId, params) => {
    return postRequest(`/api/smart/access/area/access/stats/${areaId}`, params);
  },

  /**
   * 导出区域数据
   */
  exportAreaData: (params) => {
    return postRequest('/api/smart/access/area/export', params);
  },

  /**
   * 导入区域数据
   */
  importAreaData: (formData) => {
    return postRequest('/api/smart/access/area/import', formData);
  },

  /**
   * 获取区域层级路径
   */
  getAreaPath: (areaId) => {
    return getRequest(`/api/smart/access/area/path/${areaId}`);
  },

  /**
   * 复制区域配置
   */
  copyAreaConfig: (sourceAreaId, targetAreaId) => {
    return postRequest('/api/smart/access/area/copy/config', { sourceAreaId, targetAreaId });
  }
};