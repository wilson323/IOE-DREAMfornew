/**
 * 审批配置API - 移动端版本
 * 企业级审批配置移动端接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright: IOE-DREAM (https://ioe-dream.net), Since 2025
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '@/lib/smart-request'

// 审批配置管理相关接口
export const approvalConfigApi = {
  /**
   * 获取审批配置列表
   * @param {Object} params 查询参数
   * @param {String} params.businessType 业务类型
   * @param {String} params.module 模块
   * @param {String} params.status 状态
   * @returns {Promise}
   */
  getConfigList: (params) =>
    getRequest('/api/v1/mobile/workflow/approval-config/list', params),

  /**
   * 获取审批配置详情
   * @param {Number} configId 配置ID
   * @returns {Promise}
   */
  getConfigDetail: (configId) =>
    getRequest(`/api/v1/mobile/workflow/approval-config/${configId}`),

  /**
   * 根据业务类型获取审批配置
   * @param {String} businessType 业务类型
   * @returns {Promise}
   */
  getConfigByBusinessType: (businessType) =>
    getRequest(`/api/v1/mobile/workflow/approval-config/business-type/${businessType}`),

  /**
   * 获取可用的审批配置
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getActiveConfigs: (params) =>
    getRequest('/api/v1/mobile/workflow/approval-config/active', params)
}

// 审批模板相关接口
export const approvalTemplateApi = {
  /**
   * 获取审批模板列表
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getTemplateList: (params) =>
    getRequest('/api/v1/mobile/workflow/approval-template/list', params),

  /**
   * 获取审批模板详情
   * @param {Number} templateId 模板ID
   * @returns {Promise}
   */
  getTemplateDetail: (templateId) =>
    getRequest(`/api/v1/mobile/workflow/approval-template/${templateId}`),

  /**
   * 使用审批模板
   * @param {Number} templateId 模板ID
   * @param {Object} data 业务数据
   * @returns {Promise}
   */
  useTemplate: (templateId, data) =>
    postRequest(`/api/v1/mobile/workflow/approval-template/${templateId}/use`, data)
}

// 审批流程相关接口
export const approvalProcessApi = {
  /**
   * 获取我的审批列表
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMyApprovals: (userId, params) =>
    getRequest('/api/v1/mobile/workflow/approval/my', { userId, ...params }),

  /**
   * 获取我发起的审批列表
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMyRequests: (userId, params) =>
    getRequest('/api/v1/mobile/workflow/approval/requests/my', { userId, ...params }),

  /**
   * 获取审批详情
   * @param {Number} approvalId 审批ID
   * @returns {Promise}
   */
  getApprovalDetail: (approvalId) =>
    getRequest(`/api/v1/mobile/workflow/approval/${approvalId}`),

  /**
   * 处理审批
   * @param {Number} approvalId 审批ID
   * @param {Object} data 审批数据
   * @param {String} data.action 审批动作（agree/reject/return）
   * @param {String} data.comment 审批意见
   * @returns {Promise}
   */
  processApproval: (approvalId, data) =>
    postRequest(`/api/v1/mobile/workflow/approval/${approvalId}/process`, data)
}

// 审批设置相关接口
export const approvalSettingApi = {
  /**
   * 获取审批设置
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getApprovalSettings: (userId) =>
    getRequest(`/api/v1/mobile/workflow/approval/settings/${userId}`),

  /**
   * 更新审批设置
   * @param {Number} userId 用户ID
   * @param {Object} data 设置数据
   * @returns {Promise}
   */
  updateApprovalSettings: (userId, data) =>
    putRequest(`/api/v1/mobile/workflow/approval/settings/${userId}`, data),

  /**
   * 获取代理设置
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getDelegationSettings: (userId) =>
    getRequest(`/api/v1/mobile/workflow/approval/delegation/${userId}`),

  /**
   * 设置代理审批
   * @param {Number} userId 用户ID
   * @param {Object} data 代理数据
   * @returns {Promise}
   */
  setDelegation: (userId, data) =>
    postRequest(`/api/v1/mobile/workflow/approval/delegation/${userId}`, data)
}

// 导出所有API
export default {
  ...approvalConfigApi,
  ...approvalTemplateApi,
  ...approvalProcessApi,
  ...approvalSettingApi
}

// 单独导出各个模块
export {
  approvalConfigApi,
  approvalTemplateApi,
  approvalProcessApi,
  approvalSettingApi
}