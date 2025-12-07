/**
 * 工作流引擎API - 移动端版本
 * 基于uni-app框架的企业级工作流移动端接口
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-30
 * @Copyright: IOE-DREAM (https://ioe-dream.net), Since 2025
 */

import request from '@/utils/request'

// ==================== 流程定义管理 ====================

/**
 * 部署流程定义
 * @param {Object} params - 部署参数
 * @param {String} params.bpmnXml - BPMN XML定义
 * @param {String} params.processName - 流程名称
 * @param {String} params.processKey - 流程Key（唯一标识）
 * @param {String} params.description - 流程描述（可选）
 * @param {String} params.category - 流程分类（可选）
 * @returns {Promise} 部署结果
 */
export const deployProcess = (params) => {
  return request.post('/api/v1/workflow/engine/definition/deploy', params)
}

/**
 * 分页查询流程定义
 * @param {Object} params - 查询参数
 * @param {Number} params.pageNum - 页码（默认1）
 * @param {Number} params.pageSize - 每页数量（默认20）
 * @param {String} params.category - 流程分类（可选）
 * @param {String} params.status - 状态（DRAFT/PUBLISHED/DISABLED，可选）
 * @param {String} params.keyword - 关键词（搜索流程名称和Key，可选）
 * @returns {Promise} 分页结果
 */
export const pageDefinitions = (params) => {
  return request.get('/api/v1/workflow/engine/definition/page', params)
}

/**
 * 获取流程定义详情
 * @param {Number} definitionId - 定义ID
 * @returns {Promise} 流程定义详情
 */
export const getDefinition = (definitionId) => {
  return request.get(`/api/v1/workflow/engine/definition/${definitionId}`)
}

/**
 * 激活流程定义
 * @param {Number} definitionId - 定义ID
 * @returns {Promise} 操作结果
 */
export const activateDefinition = (definitionId) => {
  return request.put(`/api/v1/workflow/engine/definition/${definitionId}/activate`)
}

/**
 * 禁用流程定义
 * @param {Number} definitionId - 定义ID
 * @returns {Promise} 操作结果
 */
export const disableDefinition = (definitionId) => {
  return request.put(`/api/v1/workflow/engine/definition/${definitionId}/disable`)
}

/**
 * 删除流程定义
 * @param {Number} definitionId - 定义ID
 * @param {Boolean} cascade - 是否级联删除（默认false）
 * @returns {Promise} 操作结果
 */
export const deleteDefinition = (definitionId, cascade = false) => {
  return request.post(`/api/v1/workflow/engine/definition/${definitionId}/delete`, null, {
    params: { cascade }
  })
}

// ==================== 流程实例管理 ====================

/**
 * 启动流程实例
 * @param {Object} params - 启动参数
 * @param {Number} params.definitionId - 定义ID
 * @param {String} params.businessKey - 业务Key（可选）
 * @param {String} params.instanceName - 实例名称（可选）
 * @param {Object} params.variables - 流程变量（可选）
 * @param {Object} params.formData - 表单数据（可选）
 * @returns {Promise} 流程实例ID
 */
export const startProcess = (params) => {
  return request.post('/api/v1/workflow/engine/instance/start', params)
}

/**
 * 分页查询流程实例
 * @param {Object} params - 查询参数
 * @param {Number} params.pageNum - 页码（默认1）
 * @param {Number} params.pageSize - 每页数量（默认20）
 * @param {Number} params.definitionId - 流程定义ID（可选）
 * @param {String} params.status - 状态（1-进行中，2-已完成，3-已终止，4-已挂起，可选）
 * @param {Number} params.startUserId - 发起人ID（可选）
 * @param {String} params.startDate - 开始日期（格式：yyyy-MM-dd，可选）
 * @param {String} params.endDate - 结束日期（格式：yyyy-MM-dd，可选）
 * @returns {Promise} 分页结果
 */
export const pageInstances = (params) => {
  return request.get('/api/v1/workflow/engine/instance/page', params)
}

/**
 * 获取流程实例详情
 * @param {Number} instanceId - 实例ID
 * @returns {Promise} 流程实例详情
 */
export const getInstance = (instanceId) => {
  return request.get(`/api/v1/workflow/engine/instance/${instanceId}`)
}

/**
 * 挂起流程实例
 * @param {Number} instanceId - 实例ID
 * @param {String} reason - 原因（可选）
 * @returns {Promise} 操作结果
 */
export const suspendInstance = (instanceId, reason) => {
  const params = reason ? { reason } : {}
  return request.put(`/api/v1/workflow/engine/instance/${instanceId}/suspend`, null, { params })
}

/**
 * 激活流程实例
 * @param {Number} instanceId - 实例ID
 * @returns {Promise} 操作结果
 */
export const activateInstance = (instanceId) => {
  return request.put(`/api/v1/workflow/engine/instance/${instanceId}/activate`)
}

/**
 * 终止流程实例
 * @param {Number} instanceId - 实例ID
 * @param {String} reason - 原因（可选）
 * @returns {Promise} 操作结果
 */
export const terminateInstance = (instanceId, reason) => {
  const params = reason ? { reason } : {}
  return request.put(`/api/v1/workflow/engine/instance/${instanceId}/terminate`, null, { params })
}

/**
 * 撤销流程实例
 * @param {Number} instanceId - 实例ID
 * @param {String} reason - 原因（可选）
 * @returns {Promise} 操作结果
 */
export const revokeInstance = (instanceId, reason) => {
  const params = reason ? { reason } : {}
  return request.put(`/api/v1/workflow/engine/instance/${instanceId}/revoke`, null, { params })
}

// ==================== 任务管理 ====================

/**
 * 分页查询我的待办任务
 * @param {Object} params - 查询参数
 * @param {Number} params.pageNum - 页码（默认1）
 * @param {Number} params.pageSize - 每页数量（默认20）
 * @param {String} params.category - 流程分类（可选）
 * @param {Number} params.priority - 优先级（1-低，2-普通，3-高，4-紧急，可选）
 * @param {String} params.dueStatus - 到期状态（OVERDUE-已过期，DUE_SOON-即将到期，NORMAL-正常，可选）
 * @returns {Promise} 分页结果
 */
export const pageMyTasks = (params) => {
  return request.get('/api/v1/workflow/engine/task/my/pending', params)
}

/**
 * 分页查询我的已办任务
 * @param {Object} params - 查询参数
 * @param {Number} params.pageNum - 页码（默认1）
 * @param {Number} params.pageSize - 每页数量（默认20）
 * @param {String} params.category - 流程分类（可选）
 * @param {String} params.outcome - 处理结果（1-同意，2-驳回，3-转办，4-委派，可选）
 * @param {String} params.startDate - 开始日期（格式：yyyy-MM-dd，可选）
 * @param {String} params.endDate - 结束日期（格式：yyyy-MM-dd，可选）
 * @returns {Promise} 分页结果
 */
export const pageMyCompletedTasks = (params) => {
  return request.get('/api/v1/workflow/engine/task/my/completed', params)
}

/**
 * 分页查询我发起的流程
 * @param {Object} params - 查询参数
 * @param {Number} params.pageNum - 页码（默认1）
 * @param {Number} params.pageSize - 每页数量（默认20）
 * @param {String} params.category - 流程分类（可选）
 * @param {String} params.status - 状态（1-进行中，2-已完成，3-已终止，4-已挂起，可选）
 * @returns {Promise} 分页结果
 */
export const pageMyProcesses = (params) => {
  return request.get('/api/v1/workflow/engine/instance/my', params)
}

/**
 * 获取任务详情
 * @param {Number} taskId - 任务ID
 * @returns {Promise} 任务详情
 */
export const getTask = (taskId) => {
  return request.get(`/api/v1/workflow/engine/task/${taskId}`)
}

/**
 * 受理任务
 * @param {Number} taskId - 任务ID
 * @returns {Promise} 操作结果
 */
export const claimTask = (taskId) => {
  return request.put(`/api/v1/workflow/engine/task/${taskId}/claim`)
}

/**
 * 取消受理任务
 * @param {Number} taskId - 任务ID
 * @returns {Promise} 操作结果
 */
export const unclaimTask = (taskId) => {
  return request.put(`/api/v1/workflow/engine/task/${taskId}/unclaim`)
}

/**
 * 委派任务
 * @param {Number} taskId - 任务ID
 * @param {Number} targetUserId - 目标用户ID
 * @returns {Promise} 操作结果
 */
export const delegateTask = (taskId, targetUserId) => {
  return request.put(`/api/v1/workflow/engine/task/${taskId}/delegate`, null, {
    params: { targetUserId }
  })
}

/**
 * 转交任务
 * @param {Number} taskId - 任务ID
 * @param {Number} targetUserId - 目标用户ID
 * @returns {Promise} 操作结果
 */
export const transferTask = (taskId, targetUserId) => {
  return request.put(`/api/v1/workflow/engine/task/${taskId}/transfer`, null, {
    params: { targetUserId }
  })
}

/**
 * 完成任务
 * @param {Number} taskId - 任务ID
 * @param {Object} params - 完成参数
 * @param {String} params.outcome - 处理结果（默认"同意"）
 * @param {String} params.comment - 处理意见（可选）
 * @param {Object} params.variables - 流程变量（可选）
 * @param {Object} params.formData - 表单数据（可选）
 * @returns {Promise} 操作结果
 */
export const completeTask = (taskId, params) => {
  return request.post(`/api/v1/workflow/engine/task/${taskId}/complete`, params)
}

/**
 * 驳回任务
 * @param {Number} taskId - 任务ID
 * @param {Object} params - 驳回参数
 * @param {String} params.comment - 驳回意见（必填）
 * @param {Object} params.variables - 流程变量（可选）
 * @returns {Promise} 操作结果
 */
export const rejectTask = (taskId, params) => {
  return request.post(`/api/v1/workflow/engine/task/${taskId}/reject`, params)
}

// ==================== 流程监控 ====================

/**
 * 获取流程实例图
 * @param {Number} instanceId - 实例ID
 * @returns {Promise} 流程图数据
 */
export const getProcessDiagram = (instanceId) => {
  return request.get(`/api/v1/workflow/engine/instance/${instanceId}/diagram`)
}

/**
 * 获取流程历史记录
 * @param {Number} instanceId - 实例ID
 * @returns {Promise} 历史记录列表
 */
export const getProcessHistory = (instanceId) => {
  return request.get(`/api/v1/workflow/engine/instance/${instanceId}/history`)
}

/**
 * 获取流程统计信息
 * @param {Object} params - 查询参数
 * @param {String} params.startDate - 开始日期（格式：yyyy-MM-dd，可选）
 * @param {String} params.endDate - 结束日期（格式：yyyy-MM-dd，可选）
 * @returns {Promise} 统计信息
 */
export const getProcessStatistics = (params) => {
  return request.get('/api/v1/workflow/engine/statistics', params)
}

/**
 * 获取用户工作量统计
 * @param {Number} userId - 用户ID
 * @param {Object} params - 查询参数
 * @param {String} params.startDate - 开始日期（格式：yyyy-MM-dd，可选）
 * @param {String} params.endDate - 结束日期（格式：yyyy-MM-dd，可选）
 * @returns {Promise} 工作量统计
 */
export const getUserWorkloadStatistics = (userId, params) => {
  return request.get(`/api/v1/workflow/engine/statistics/user/${userId}`, params)
}

// ==================== 导出默认对象 ====================

export default {
  // 流程定义管理
  deployProcess,
  pageDefinitions,
  getDefinition,
  activateDefinition,
  disableDefinition,
  deleteDefinition,

  // 流程实例管理
  startProcess,
  pageInstances,
  getInstance,
  suspendInstance,
  activateInstance,
  terminateInstance,
  revokeInstance,

  // 任务管理
  pageMyTasks,
  pageMyCompletedTasks,
  pageMyProcesses,
  getTask,
  claimTask,
  unclaimTask,
  delegateTask,
  transferTask,
  completeTask,
  rejectTask,

  // 流程监控
  getProcessDiagram,
  getProcessHistory,
  getProcessStatistics,
  getUserWorkloadStatistics
}
