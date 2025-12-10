/**
 * 工作流引擎API - 移动端版本
 * 企业级工作流移动端接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright: IOE-DREAM (https://ioe-dream.net), Since 2025
 */

import { getRequest, postRequest, putRequest } from '@/lib/smart-request'

// 工作流定义相关接口
export const definitionApi = {
  /**
   * 部署流程定义
   * @param {Object} data 部署数据
   * @param {String} data.bpmnXml BPMN XML定义
   * @param {String} data.processName 流程名称
   * @param {String} data.processKey 流程Key
   * @param {String} data.description 流程描述
   * @param {String} data.category 流程分类
   * @returns {Promise}
   */
  deployProcess: (data) => postRequest('/api/v1/mobile/workflow/engine/definition/deploy', data),

  /**
   * 获取流程定义列表
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getDefinitionList: (params) =>
    getRequest('/api/v1/mobile/workflow/engine/definition/list', params),

  /**
   * 获取流程定义详情
   * @param {Number} definitionId 定义ID
   * @returns {Promise}
   */
  getDefinitionDetail: (definitionId) =>
    getRequest(`/api/v1/mobile/workflow/engine/definition/${definitionId}`),

  /**
   * 获取流程分类列表
   * @returns {Promise}
   */
  getDefinitionCategories: () =>
    getRequest('/api/v1/mobile/workflow/engine/definition/categories')
}

// 工作流实例相关接口
export const instanceApi = {
  /**
   * 启动流程实例
   * @param {Object} data 启动数据
   * @param {Number} data.definitionId 定义ID
   * @param {String} data.businessKey 业务Key
   * @param {String} data.instanceName 实例名称
   * @param {Object} data.variables 流程变量
   * @param {Object} data.formData 表单数据
   * @returns {Promise}
   */
  startProcess: (data) => postRequest('/api/v1/mobile/workflow/engine/instance/start', data),

  /**
   * 获取我的流程实例
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMyInstances: (userId, params) =>
    getRequest('/api/v1/mobile/workflow/engine/instance/my', { userId, ...params }),

  /**
   * 获取流程实例详情
   * @param {Number} instanceId 实例ID
   * @returns {Promise}
   */
  getInstanceDetail: (instanceId) =>
    getRequest(`/api/v1/mobile/workflow/engine/instance/${instanceId}`),

  /**
   * 获取流程实例图
   * @param {Number} instanceId 实例ID
   * @returns {Promise}
   */
  getInstanceDiagram: (instanceId) =>
    getRequest(`/api/v1/mobile/workflow/engine/instance/${instanceId}/diagram`),

  /**
   * 撤销流程实例
   * @param {Number} instanceId 实例ID
   * @param {String} reason 撤销原因
   * @returns {Promise}
   */
  revokeInstance: (instanceId, reason) =>
    putRequest(`/api/v1/mobile/workflow/engine/instance/${instanceId}/revoke`, { reason })
}

// 工作流任务相关接口
export const taskApi = {
  /**
   * 获取我的待办任务
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMyTasks: (userId, params) =>
    getRequest('/api/v1/mobile/workflow/engine/task/my/pending', { userId, ...params }),

  /**
   * 获取我的已办任务
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMyCompletedTasks: (userId, params) =>
    getRequest('/api/v1/mobile/workflow/engine/task/my/completed', { userId, ...params }),

  /**
   * 获取任务详情
   * @param {Number} taskId 任务ID
   * @returns {Promise}
   */
  getTaskDetail: (taskId) =>
    getRequest(`/api/v1/mobile/workflow/engine/task/${taskId}`),

  /**
   * 受理任务
   * @param {Number} taskId 任务ID
   * @returns {Promise}
   */
  claimTask: (taskId) =>
    putRequest(`/api/v1/mobile/workflow/engine/task/${taskId}/claim`),

  /**
   * 完成任务
   * @param {Number} taskId 任务ID
   * @param {Object} data 完成数据
   * @param {String} data.outcome 处理结果
   * @param {String} data.comment 处理意见
   * @param {Object} data.variables 流程变量
   * @returns {Promise}
   */
  completeTask: (taskId, data) =>
    postRequest(`/api/v1/mobile/workflow/engine/task/${taskId}/complete`, data),

  /**
   * 驳回任务
   * @param {Number} taskId 任务ID
   * @param {Object} data 驳回数据
   * @param {String} data.comment 驳回意见
   * @param {Object} data.variables 流程变量
   * @returns {Promise}
   */
  rejectTask: (taskId, data) =>
    postRequest(`/api/v1/mobile/workflow/engine/task/${taskId}/reject`, data),

  /**
   * 转交任务
   * @param {Number} taskId 任务ID
   * @param {Number} targetUserId 目标用户ID
   * @returns {Promise}
   */
  transferTask: (taskId, targetUserId) =>
    putRequest(`/api/v1/mobile/workflow/engine/task/${taskId}/transfer`, { targetUserId }),

  /**
   * 委派任务
   * @param {Number} taskId 任务ID
   * @param {Number} targetUserId 目标用户ID
   * @returns {Promise}
   */
  delegateTask: (taskId, targetUserId) =>
    putRequest(`/api/v1/mobile/workflow/engine/task/${taskId}/delegate`, { targetUserId })
}

// 工作流统计相关接口
export const statisticsApi = {
  /**
   * 获取我的工作统计
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMyStatistics: (userId, params) =>
    getRequest('/api/v1/mobile/workflow/engine/statistics/my', { userId, ...params }),

  /**
   * 获取待办任务统计
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getTaskStatistics: (userId) =>
    getRequest(`/api/v1/mobile/workflow/engine/statistics/tasks/${userId}`),

  /**
   * 获取流程实例统计
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getInstanceStatistics: (userId) =>
    getRequest(`/api/v1/mobile/workflow/engine/statistics/instances/${userId}`),

  /**
   * 获取工作量统计
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getWorkloadStatistics: (userId, params) =>
    getRequest(`/api/v1/mobile/workflow/engine/statistics/workload/${userId}`, params)
}

// 工作流历史相关接口
export const historyApi = {
  /**
   * 获取流程历史记录
   * @param {Number} instanceId 实例ID
   * @returns {Promise}
   */
  getProcessHistory: (instanceId) =>
    getRequest(`/api/v1/mobile/workflow/engine/instance/${instanceId}/history`),

  /**
   * 获取任务历史记录
   * @param {Number} taskId 任务ID
   * @returns {Promise}
   */
  getTaskHistory: (taskId) =>
    getRequest(`/api/v1/mobile/workflow/engine/task/${taskId}/history`),

  /**
   * 获取我的处理历史
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMyHistory: (userId, params) =>
    getRequest('/api/v1/mobile/workflow/engine/history/my', { userId, ...params })
}

// 实用工具相关接口
export const utilApi = {
  /**
   * 获取可转交的用户列表
   * @param {Number} taskId 任务ID
   * @returns {Promise}
   */
  getTransferableUsers: (taskId) =>
    getRequest(`/api/v1/mobile/workflow/engine/task/${taskId}/transferable-users`),

  /**
   * 获取流程表单结构
   * @param {Number} definitionId 定义ID
   * @returns {Promise}
   */
  getFormStructure: (definitionId) =>
    getRequest(`/api/v1/mobile/workflow/engine/definition/${definitionId}/form`),

  /**
   * 验证表单数据
   * @param {Number} definitionId 定义ID
   * @param {Object} formData 表单数据
   * @returns {Promise}
   */
  validateFormData: (definitionId, formData) =>
    postRequest(`/api/v1/mobile/workflow/engine/definition/${definitionId}/validate`, formData),

  /**
   * 获取下一步节点信息
   * @param {Number} taskId 任务ID
   * @param {Object} variables 流程变量
   * @returns {Promise}
   */
  getNextSteps: (taskId, variables) =>
    postRequest(`/api/v1/mobile/workflow/engine/task/${taskId}/next-steps`, variables)
}

// 导出所有API
export default {
  ...definitionApi,
  ...instanceApi,
  ...taskApi,
  ...statisticsApi,
  ...historyApi,
  ...utilApi
}

// 单独导出各个模块
export { definitionApi, instanceApi, taskApi, statisticsApi, historyApi, utilApi }