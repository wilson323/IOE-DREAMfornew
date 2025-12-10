/**
 * 审批配置API - Web前端版本
 * 企业级审批配置前端接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright: IOE-DREAM (https://ioe-dream.net), Since 2025
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

// 审批配置管理
export const approvalConfigApi = {
  /**
   * 分页查询审批配置
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.businessType 业务类型
   * @param {String} params.module 模块
   * @param {String} params.status 状态
   * @returns {Promise}
   */
  pageConfigs: (params) => {
    return getRequest('/api/v1/workflow/approval-config/page', params);
  },

  /**
   * 根据ID查询审批配置
   * @param {Number} id 配置ID
   * @returns {Promise}
   */
  getConfig: (id) => {
    return getRequest(`/api/v1/workflow/approval-config/${id}`);
  },

  /**
   * 根据业务类型查询审批配置
   * @param {String} businessType 业务类型
   * @returns {Promise}
   */
  getConfigByBusinessType: (businessType) => {
    return getRequest(`/api/v1/workflow/approval-config/business-type/${businessType}`);
  },

  /**
   * 创建审批配置
   * @param {Object} data 配置数据
   * @returns {Promise}
   */
  createConfig: (data) => {
    return postRequest('/api/v1/workflow/approval-config', data);
  },

  /**
   * 更新审批配置
   * @param {Number} id 配置ID
   * @param {Object} data 配置数据
   * @returns {Promise}
   */
  updateConfig: (id, data) => {
    return putRequest(`/api/v1/workflow/approval-config/${id}`, data);
  },

  /**
   * 删除审批配置
   * @param {Number} id 配置ID
   * @returns {Promise}
   */
  deleteConfig: (id) => {
    return deleteRequest(`/api/v1/workflow/approval-config/${id}`);
  },

  /**
   * 启用审批配置
   * @param {Number} id 配置ID
   * @returns {Promise}
   */
  enableConfig: (id) => {
    return putRequest(`/api/v1/workflow/approval-config/${id}/enable`);
  },

  /**
   * 禁用审批配置
   * @param {Number} id 配置ID
   * @returns {Promise}
   */
  disableConfig: (id) => {
    return putRequest(`/api/v1/workflow/approval-config/${id}/disable`);
  }
};

// 审批模板管理
export const approvalTemplateApi = {
  /**
   * 分页查询审批模板
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  pageTemplates: (params) => {
    return getRequest('/api/v1/workflow/approval-template/page', params);
  },

  /**
   * 获取审批模板详情
   * @param {Number} templateId 模板ID
   * @returns {Promise}
   */
  getTemplate: (templateId) => {
    return getRequest(`/api/v1/workflow/approval-template/${templateId}`);
  },

  /**
   * 创建审批模板
   * @param {Object} data 模板数据
   * @returns {Promise}
   */
  createTemplate: (data) => {
    return postRequest('/api/v1/workflow/approval-template', data);
  },

  /**
   * 更新审批模板
   * @param {Number} templateId 模板ID
   * @param {Object} data 模板数据
   * @returns {Promise}
   */
  updateTemplate: (templateId, data) => {
    return putRequest(`/api/v1/workflow/approval-template/${templateId}`, data);
  },

  /**
   * 删除审批模板
   * @param {Number} templateId 模板ID
   * @returns {Promise}
   */
  deleteTemplate: (templateId) => {
    return deleteRequest(`/api/v1/workflow/approval-template/${templateId}`);
  }
};

// 审批流程管理
export const approvalProcessApi = {
  /**
   * 分页查询审批流程
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  pageProcesses: (params) => {
    return getRequest('/api/v1/workflow/approval-process/page', params);
  },

  /**
   * 获取审批流程详情
   * @param {Number} processId 流程ID
   * @returns {Promise}
   */
  getProcess: (processId) => {
    return getRequest(`/api/v1/workflow/approval-process/${processId}`);
  },

  /**
   * 启动审批流程
   * @param {Object} data 流程数据
   * @returns {Promise}
   */
  startProcess: (data) => {
    return postRequest('/api/v1/workflow/approval-process/start', data);
  },

  /**
   * 处理审批
   * @param {Number} processId 流程ID
   * @param {Object} data 处理数据
   * @returns {Promise}
   */
  processApproval: (processId, data) => {
    return postRequest(`/api/v1/workflow/approval-process/${processId}/process`, data);
  },

  /**
   * 终止审批流程
   * @param {Number} processId 流程ID
   * @param {Object} data 终止数据
   * @returns {Promise}
   */
  terminateProcess: (processId, data) => {
    return postRequest(`/api/v1/workflow/approval-process/${processId}/terminate`, data);
  }
};

// 审批统计
export const approvalStatisticsApi = {
  /**
   * 获取审批统计信息
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getStatistics: (params) => {
    return getRequest('/api/v1/workflow/approval-statistics', params);
  },

  /**
   * 获取用户审批统计
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getUserStatistics: (userId, params) => {
    return getRequest(`/api/v1/workflow/approval-statistics/user/${userId}`, params);
  },

  /**
   * 获取部门审批统计
   * @param {Number} departmentId 部门ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getDepartmentStatistics: (departmentId, params) => {
    return getRequest(`/api/v1/workflow/approval-statistics/department/${departmentId}`, params);
  }
};

// 导出默认对象
export const approvalConfigDefault = {
  // 审批配置管理
  ...approvalConfigApi,
  // 审批模板管理
  ...approvalTemplateApi,
  // 审批流程管理
  ...approvalProcessApi,
  // 审批统计
  ...approvalStatisticsApi
};

export default approvalConfigDefault;

// 单独导出各个模块
export { approvalConfigApi, approvalTemplateApi, approvalProcessApi, approvalStatisticsApi };