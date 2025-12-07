/*
 * 工作流管理Store
 * 提供工作流流程定义、实例、任务管理的状态管理
 * 严格遵循CLAUDE.md架构规范
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { workflowApi } from '/@/api/business/oa/workflow-api';

export const useWorkflowStore = defineStore('workflow', {
  state: () => ({
    // ==================== 待办任务列表 ====================
    pendingTaskList: [],
    pendingTaskTotal: 0,
    pendingTaskLoading: false,
    pendingTaskQueryParams: {
      pageNum: 1,
      pageSize: 20,
      category: null,
      priority: null,
      dueStatus: null,
    },

    // ==================== 已办任务列表 ====================
    completedTaskList: [],
    completedTaskTotal: 0,
    completedTaskLoading: false,
    completedTaskQueryParams: {
      pageNum: 1,
      pageSize: 20,
      category: null,
      outcome: null,
      startDate: null,
      endDate: null,
    },

    // ==================== 流程实例列表 ====================
    instanceList: [],
    instanceTotal: 0,
    instanceLoading: false,
    instanceQueryParams: {
      pageNum: 1,
      pageSize: 20,
      definitionId: null,
      status: null,
      startUserId: null,
      startDate: null,
      endDate: null,
    },

    // ==================== 我发起的流程列表 ====================
    myProcessList: [],
    myProcessTotal: 0,
    myProcessLoading: false,
    myProcessQueryParams: {
      pageNum: 1,
      pageSize: 20,
      category: null,
      status: null,
    },

    // ==================== 流程定义列表 ====================
    definitionList: [],
    definitionTotal: 0,
    definitionLoading: false,
    definitionQueryParams: {
      pageNum: 1,
      pageSize: 20,
      category: null,
      status: null,
      keyword: null,
    },

    // ==================== 当前任务详情 ====================
    currentTask: null,
    taskLoading: false,

    // ==================== 当前流程实例详情 ====================
    currentInstance: null,
    instanceLoading: false,

    // ==================== 当前流程定义详情 ====================
    currentDefinition: null,
    definitionDetailLoading: false,

    // ==================== 流程历史记录 ====================
    processHistory: [],
    historyLoading: false,

    // ==================== 流程图数据 ====================
    processDiagram: null,
    diagramLoading: false,

    // ==================== 流程统计信息 ====================
    processStatistics: null,
    statisticsLoading: false,

    // ==================== 用户工作量统计 ====================
    userWorkloadStatistics: null,
    workloadLoading: false,

    // ==================== WebSocket连接状态 ====================
    wsConnected: false,
    webSocketConnected: false, // 别名，兼容两种命名
    wsReconnecting: false,
    wsError: null,

    // ==================== 选中的任务ID列表 ====================
    selectedTaskIds: [],

    // ==================== 选中的流程实例ID列表 ====================
    selectedInstanceIds: [],
  }),

  getters: {
    /**
     * 待办任务统计
     */
    pendingTaskStats(state) {
      const urgent = state.pendingTaskList.filter(t => t.priority === 4).length;
      const high = state.pendingTaskList.filter(t => t.priority === 3).length;
      const overdue = state.pendingTaskList.filter(t => t.isOverdue).length;
      const total = state.pendingTaskTotal;
      return { urgent, high, overdue, total };
    },

    /**
     * 根据任务ID获取任务信息
     */
    getTaskById: (state) => (taskId) => {
      return state.pendingTaskList.find(t => t.taskId === taskId) ||
             state.completedTaskList.find(t => t.taskId === taskId);
    },

    /**
     * 根据实例ID获取流程实例信息
     */
    getInstanceById: (state) => (instanceId) => {
      return state.instanceList.find(i => i.instanceId === instanceId) ||
             state.myProcessList.find(i => i.instanceId === instanceId);
    },

    /**
     * 根据定义ID获取流程定义信息
     */
    getDefinitionById: (state) => (definitionId) => {
      return state.definitionList.find(d => d.id === definitionId);
    },

    /**
     * 是否有选中的任务
     */
    hasSelectedTasks(state) {
      return state.selectedTaskIds.length > 0;
    },

    /**
     * 是否有选中的流程实例
     */
    hasSelectedInstances(state) {
      return state.selectedInstanceIds.length > 0;
    },
  },

  actions: {
    // ==================== 待办任务管理 ====================

    /**
     * 获取待办任务列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 分页结果
     */
    async fetchPendingTaskList(params = {}) {
      this.pendingTaskLoading = true;
      try {
        const queryParams = { ...this.pendingTaskQueryParams, ...params };
        const response = await workflowApi.pageMyTasks(queryParams);

        if (response.code === 1 || response.code === 200) {
          this.pendingTaskList = response.data?.list || [];
          this.pendingTaskTotal = response.data?.total || 0;
          this.pendingTaskQueryParams = queryParams;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取待办任务列表失败');
          return null;
        }
      } catch (error) {
        console.error('获取待办任务列表失败:', error);
        message.error('获取待办任务列表失败');
        return null;
      } finally {
        this.pendingTaskLoading = false;
      }
    },

    /**
     * 受理任务
     * @param {Number} taskId - 任务ID
     * @returns {Promise} 操作结果
     */
    async claimTask(taskId) {
      try {
        const response = await workflowApi.claimTask(taskId);
        if (response.code === 1 || response.code === 200) {
          message.success('任务受理成功');
          await this.fetchPendingTaskList();
          return true;
        } else {
          message.error(response.message || response.msg || '任务受理失败');
          return false;
        }
      } catch (error) {
        console.error('任务受理失败:', error);
        message.error('任务受理失败');
        return false;
      }
    },

    /**
     * 取消受理任务
     * @param {Number} taskId - 任务ID
     * @returns {Promise} 操作结果
     */
    async unclaimTask(taskId) {
      try {
        const response = await workflowApi.unclaimTask(taskId);
        if (response.code === 1 || response.code === 200) {
          message.success('取消受理成功');
          await this.fetchPendingTaskList();
          return true;
        } else {
          message.error(response.message || response.msg || '取消受理失败');
          return false;
        }
      } catch (error) {
        console.error('取消受理失败:', error);
        message.error('取消受理失败');
        return false;
      }
    },

    /**
     * 委派任务
     * @param {Number} taskId - 任务ID
     * @param {Number} targetUserId - 目标用户ID
     * @returns {Promise} 操作结果
     */
    async delegateTask(taskId, targetUserId) {
      try {
        const response = await workflowApi.delegateTask(taskId, targetUserId);
        if (response.code === 1 || response.code === 200) {
          message.success('任务委派成功');
          await this.fetchPendingTaskList();
          return true;
        } else {
          message.error(response.message || response.msg || '任务委派失败');
          return false;
        }
      } catch (error) {
        console.error('任务委派失败:', error);
        message.error('任务委派失败');
        return false;
      }
    },

    /**
     * 转交任务
     * @param {Number} taskId - 任务ID
     * @param {Number} targetUserId - 目标用户ID
     * @returns {Promise} 操作结果
     */
    async transferTask(taskId, targetUserId) {
      try {
        const response = await workflowApi.transferTask(taskId, targetUserId);
        if (response.code === 1 || response.code === 200) {
          message.success('任务转交成功');
          await this.fetchPendingTaskList();
          return true;
        } else {
          message.error(response.message || response.msg || '任务转交失败');
          return false;
        }
      } catch (error) {
        console.error('任务转交失败:', error);
        message.error('任务转交失败');
        return false;
      }
    },

    /**
     * 完成任务
     * @param {Number} taskId - 任务ID
     * @param {Object} params - 完成参数
     * @returns {Promise} 操作结果
     */
    async completeTask(taskId, params) {
      try {
        const response = await workflowApi.completeTask(taskId, params);
        if (response.code === 1 || response.code === 200) {
          message.success('任务完成成功');
          await this.fetchPendingTaskList();
          return true;
        } else {
          message.error(response.message || response.msg || '任务完成失败');
          return false;
        }
      } catch (error) {
        console.error('任务完成失败:', error);
        message.error('任务完成失败');
        return false;
      }
    },

    /**
     * 驳回任务
     * @param {Number} taskId - 任务ID
     * @param {Object} params - 驳回参数
     * @returns {Promise} 操作结果
     */
    async rejectTask(taskId, params) {
      try {
        const response = await workflowApi.rejectTask(taskId, params);
        if (response.code === 1 || response.code === 200) {
          message.success('任务驳回成功');
          await this.fetchPendingTaskList();
          return true;
        } else {
          message.error(response.message || response.msg || '任务驳回失败');
          return false;
        }
      } catch (error) {
        console.error('任务驳回失败:', error);
        message.error('任务驳回失败');
        return false;
      }
    },

    // ==================== 已办任务管理 ====================

    /**
     * 获取已办任务列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 分页结果
     */
    async fetchCompletedTaskList(params = {}) {
      this.completedTaskLoading = true;
      try {
        const queryParams = { ...this.completedTaskQueryParams, ...params };
        const response = await workflowApi.pageMyCompletedTasks(queryParams);

        if (response.code === 1 || response.code === 200) {
          this.completedTaskList = response.data?.list || [];
          this.completedTaskTotal = response.data?.total || 0;
          this.completedTaskQueryParams = queryParams;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取已办任务列表失败');
          return null;
        }
      } catch (error) {
        console.error('获取已办任务列表失败:', error);
        message.error('获取已办任务列表失败');
        return null;
      } finally {
        this.completedTaskLoading = false;
      }
    },

    // ==================== 流程实例管理 ====================

    /**
     * 获取流程实例列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 分页结果
     */
    async fetchInstanceList(params = {}) {
      this.instanceLoading = true;
      try {
        const queryParams = { ...this.instanceQueryParams, ...params };
        const response = await workflowApi.pageInstances(queryParams);

        if (response.code === 1 || response.code === 200) {
          this.instanceList = response.data?.list || [];
          this.instanceTotal = response.data?.total || 0;
          this.instanceQueryParams = queryParams;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取流程实例列表失败');
          return null;
        }
      } catch (error) {
        console.error('获取流程实例列表失败:', error);
        message.error('获取流程实例列表失败');
        return null;
      } finally {
        this.instanceLoading = false;
      }
    },

    /**
     * 获取流程实例详情
     * @param {Number} instanceId - 实例ID
     * @returns {Promise} 流程实例详情
     */
    async fetchInstanceDetail(instanceId) {
      this.instanceLoading = true;
      try {
        const response = await workflowApi.getInstance(instanceId);
        if (response.code === 1 || response.code === 200) {
          this.currentInstance = response.data;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取流程实例详情失败');
          return null;
        }
      } catch (error) {
        console.error('获取流程实例详情失败:', error);
        message.error('获取流程实例详情失败');
        return null;
      } finally {
        this.instanceLoading = false;
      }
    },

    /**
     * 启动流程实例
     * @param {Object} params - 启动参数
     * @returns {Promise} 流程实例ID
     */
    async startProcess(params) {
      try {
        const response = await workflowApi.startProcess(params);
        if (response.code === 1 || response.code === 200) {
          message.success('流程启动成功');
          await this.fetchMyProcessList();
          return response.data;
        } else {
          message.error(response.message || response.msg || '流程启动失败');
          return null;
        }
      } catch (error) {
        console.error('流程启动失败:', error);
        message.error('流程启动失败');
        return null;
      }
    },

    /**
     * 挂起流程实例
     * @param {Number} instanceId - 实例ID
     * @param {String} reason - 原因（可选）
     * @returns {Promise} 操作结果
     */
    async suspendInstance(instanceId, reason) {
      try {
        const response = await workflowApi.suspendInstance(instanceId, reason);
        if (response.code === 1 || response.code === 200) {
          message.success('流程挂起成功');
          await this.fetchInstanceList();
          await this.fetchMyProcessList();
          return true;
        } else {
          message.error(response.message || response.msg || '流程挂起失败');
          return false;
        }
      } catch (error) {
        console.error('流程挂起失败:', error);
        message.error('流程挂起失败');
        return false;
      }
    },

    /**
     * 激活流程实例
     * @param {Number} instanceId - 实例ID
     * @returns {Promise} 操作结果
     */
    async activateInstance(instanceId) {
      try {
        const response = await workflowApi.activateInstance(instanceId);
        if (response.code === 1 || response.code === 200) {
          message.success('流程激活成功');
          await this.fetchInstanceList();
          await this.fetchMyProcessList();
          return true;
        } else {
          message.error(response.message || response.msg || '流程激活失败');
          return false;
        }
      } catch (error) {
        console.error('流程激活失败:', error);
        message.error('流程激活失败');
        return false;
      }
    },

    /**
     * 终止流程实例
     * @param {Number} instanceId - 实例ID
     * @param {String} reason - 原因（可选）
     * @returns {Promise} 操作结果
     */
    async terminateInstance(instanceId, reason) {
      try {
        const response = await workflowApi.terminateInstance(instanceId, reason);
        if (response.code === 1 || response.code === 200) {
          message.success('流程终止成功');
          await this.fetchInstanceList();
          await this.fetchMyProcessList();
          return true;
        } else {
          message.error(response.message || response.msg || '流程终止失败');
          return false;
        }
      } catch (error) {
        console.error('流程终止失败:', error);
        message.error('流程终止失败');
        return false;
      }
    },

    /**
     * 撤销流程实例
     * @param {Number} instanceId - 实例ID
     * @param {String} reason - 原因（可选）
     * @returns {Promise} 操作结果
     */
    async revokeInstance(instanceId, reason) {
      try {
        const response = await workflowApi.revokeInstance(instanceId, reason);
        if (response.code === 1 || response.code === 200) {
          message.success('流程撤销成功');
          await this.fetchInstanceList();
          await this.fetchMyProcessList();
          return true;
        } else {
          message.error(response.message || response.msg || '流程撤销失败');
          return false;
        }
      } catch (error) {
        console.error('流程撤销失败:', error);
        message.error('流程撤销失败');
        return false;
      }
    },

    // ==================== 我发起的流程管理 ====================

    /**
     * 获取我发起的流程列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 分页结果
     */
    async fetchMyProcessList(params = {}) {
      this.myProcessLoading = true;
      try {
        const queryParams = { ...this.myProcessQueryParams, ...params };
        const response = await workflowApi.pageMyProcesses(queryParams);

        if (response.code === 1 || response.code === 200) {
          this.myProcessList = response.data?.list || [];
          this.myProcessTotal = response.data?.total || 0;
          this.myProcessQueryParams = queryParams;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取我发起的流程列表失败');
          return null;
        }
      } catch (error) {
        console.error('获取我发起的流程列表失败:', error);
        message.error('获取我发起的流程列表失败');
        return null;
      } finally {
        this.myProcessLoading = false;
      }
    },

    // ==================== 流程定义管理 ====================

    /**
     * 获取流程定义列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 分页结果
     */
    async fetchDefinitionList(params = {}) {
      this.definitionLoading = true;
      try {
        const queryParams = { ...this.definitionQueryParams, ...params };
        const response = await workflowApi.pageDefinitions(queryParams);

        if (response.code === 1 || response.code === 200) {
          this.definitionList = response.data?.list || [];
          this.definitionTotal = response.data?.total || 0;
          this.definitionQueryParams = queryParams;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取流程定义列表失败');
          return null;
        }
      } catch (error) {
        console.error('获取流程定义列表失败:', error);
        message.error('获取流程定义列表失败');
        return null;
      } finally {
        this.definitionLoading = false;
      }
    },

    /**
     * 获取流程定义详情
     * @param {Number} definitionId - 定义ID
     * @returns {Promise} 流程定义详情
     */
    async fetchDefinitionDetail(definitionId) {
      this.definitionDetailLoading = true;
      try {
        const response = await workflowApi.getDefinition(definitionId);
        if (response.code === 1 || response.code === 200) {
          this.currentDefinition = response.data;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取流程定义详情失败');
          return null;
        }
      } catch (error) {
        console.error('获取流程定义详情失败:', error);
        message.error('获取流程定义详情失败');
        return null;
      } finally {
        this.definitionDetailLoading = false;
      }
    },

    /**
     * 部署流程定义
     * @param {Object} params - 部署参数
     * @returns {Promise} 操作结果
     */
    async deployProcess(params) {
      try {
        const response = await workflowApi.deployProcess(params);
        if (response.code === 1 || response.code === 200) {
          message.success('流程部署成功');
          await this.fetchDefinitionList();
          return true;
        } else {
          message.error(response.message || response.msg || '流程部署失败');
          return false;
        }
      } catch (error) {
        console.error('流程部署失败:', error);
        message.error('流程部署失败');
        return false;
      }
    },

    /**
     * 激活流程定义
     * @param {Number} definitionId - 定义ID
     * @returns {Promise} 操作结果
     */
    async activateDefinition(definitionId) {
      try {
        const response = await workflowApi.activateDefinition(definitionId);
        if (response.code === 1 || response.code === 200) {
          message.success('流程激活成功');
          await this.fetchDefinitionList();
          return true;
        } else {
          message.error(response.message || response.msg || '流程激活失败');
          return false;
        }
      } catch (error) {
        console.error('流程激活失败:', error);
        message.error('流程激活失败');
        return false;
      }
    },

    /**
     * 禁用流程定义
     * @param {Number} definitionId - 定义ID
     * @returns {Promise} 操作结果
     */
    async disableDefinition(definitionId) {
      try {
        const response = await workflowApi.disableDefinition(definitionId);
        if (response.code === 1 || response.code === 200) {
          message.success('流程禁用成功');
          await this.fetchDefinitionList();
          return true;
        } else {
          message.error(response.message || response.msg || '流程禁用失败');
          return false;
        }
      } catch (error) {
        console.error('流程禁用失败:', error);
        message.error('流程禁用失败');
        return false;
      }
    },

    /**
     * 删除流程定义
     * @param {Number} definitionId - 定义ID
     * @param {Boolean} cascade - 是否级联删除
     * @returns {Promise} 操作结果
     */
    async deleteDefinition(definitionId, cascade = false) {
      try {
        const response = await workflowApi.deleteDefinition(definitionId, cascade);
        if (response.code === 1 || response.code === 200) {
          message.success('流程删除成功');
          await this.fetchDefinitionList();
          return true;
        } else {
          message.error(response.message || response.msg || '流程删除失败');
          return false;
        }
      } catch (error) {
        console.error('流程删除失败:', error);
        message.error('流程删除失败');
        return false;
      }
    },

    // ==================== 任务详情管理 ====================

    /**
     * 获取任务详情
     * @param {Number} taskId - 任务ID
     * @returns {Promise} 任务详情
     */
    async fetchTaskDetail(taskId) {
      this.taskLoading = true;
      try {
        const response = await workflowApi.getTask(taskId);
        if (response.code === 1 || response.code === 200) {
          this.currentTask = response.data;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取任务详情失败');
          return null;
        }
      } catch (error) {
        console.error('获取任务详情失败:', error);
        message.error('获取任务详情失败');
        return null;
      } finally {
        this.taskLoading = false;
      }
    },

    // ==================== 流程监控 ====================

    /**
     * 获取流程实例图
     * @param {Number} instanceId - 实例ID
     * @returns {Promise} 流程图数据
     */
    async fetchProcessDiagram(instanceId) {
      this.diagramLoading = true;
      try {
        const response = await workflowApi.getProcessDiagram(instanceId);
        if (response.code === 1 || response.code === 200) {
          this.processDiagram = response.data;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取流程图失败');
          return null;
        }
      } catch (error) {
        console.error('获取流程图失败:', error);
        message.error('获取流程图失败');
        return null;
      } finally {
        this.diagramLoading = false;
      }
    },

    /**
     * 获取流程历史记录
     * @param {Number} instanceId - 实例ID
     * @returns {Promise} 历史记录列表
     */
    async fetchProcessHistory(instanceId) {
      this.historyLoading = true;
      try {
        const response = await workflowApi.getProcessHistory(instanceId);
        if (response.code === 1 || response.code === 200) {
          this.processHistory = response.data || [];
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取流程历史失败');
          return null;
        }
      } catch (error) {
        console.error('获取流程历史失败:', error);
        message.error('获取流程历史失败');
        return null;
      } finally {
        this.historyLoading = false;
      }
    },

    /**
     * 获取流程统计信息
     * @param {Object} params - 查询参数
     * @returns {Promise} 统计信息
     */
    async fetchProcessStatistics(params = {}) {
      this.statisticsLoading = true;
      try {
        const response = await workflowApi.getProcessStatistics(params);
        if (response.code === 1 || response.code === 200) {
          this.processStatistics = response.data;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取流程统计失败');
          return null;
        }
      } catch (error) {
        console.error('获取流程统计失败:', error);
        message.error('获取流程统计失败');
        return null;
      } finally {
        this.statisticsLoading = false;
      }
    },

    /**
     * 获取用户工作量统计
     * @param {Number} userId - 用户ID
     * @param {Object} params - 查询参数
     * @returns {Promise} 工作量统计
     */
    async fetchUserWorkloadStatistics(userId, params = {}) {
      this.workloadLoading = true;
      try {
        const response = await workflowApi.getUserWorkloadStatistics(userId, params);
        if (response.code === 1 || response.code === 200) {
          this.userWorkloadStatistics = response.data;
          return response.data;
        } else {
          message.error(response.message || response.msg || '获取工作量统计失败');
          return null;
        }
      } catch (error) {
        console.error('获取工作量统计失败:', error);
        message.error('获取工作量统计失败');
        return null;
      } finally {
        this.workloadLoading = false;
      }
    },

    // ==================== WebSocket管理 ====================

    /**
     * 设置WebSocket连接状态
     * @param {Boolean} connected - 连接状态
     */
    setWsConnected(connected) {
      this.wsConnected = connected;
    },

    /**
     * 设置WebSocket重连状态
     * @param {Boolean} reconnecting - 重连状态
     */
    setWsReconnecting(reconnecting) {
      this.wsReconnecting = reconnecting;
    },

    /**
     * 设置WebSocket错误
     * @param {String} error - 错误信息
     */
    setWsError(error) {
      this.wsError = error;
    },

    // ==================== 选中项管理 ====================

    /**
     * 设置选中的任务ID列表
     * @param {Array} taskIds - 任务ID列表
     */
    setSelectedTaskIds(taskIds) {
      this.selectedTaskIds = taskIds;
    },

    /**
     * 设置选中的流程实例ID列表
     * @param {Array} instanceIds - 实例ID列表
     */
    setSelectedInstanceIds(instanceIds) {
      this.selectedInstanceIds = instanceIds;
    },

    // ==================== 数据清理 ====================

    /**
     * 清空待办任务列表
     */
    clearPendingTaskList() {
      this.pendingTaskList = [];
      this.pendingTaskTotal = 0;
    },

    /**
     * 清空已办任务列表
     */
    clearCompletedTaskList() {
      this.completedTaskList = [];
      this.completedTaskTotal = 0;
    },

    /**
     * 清空流程实例列表
     */
    clearInstanceList() {
      this.instanceList = [];
      this.instanceTotal = 0;
    },

    /**
     * 清空我发起的流程列表
     */
    clearMyProcessList() {
      this.myProcessList = [];
      this.myProcessTotal = 0;
    },

    /**
     * 清空流程定义列表
     */
    clearDefinitionList() {
      this.definitionList = [];
      this.definitionTotal = 0;
    },

    /**
     * 清空当前任务详情
     */
    clearCurrentTask() {
      this.currentTask = null;
    },

    /**
     * 清空当前流程实例详情
     */
    clearCurrentInstance() {
      this.currentInstance = null;
    },

    /**
     * 清空当前流程定义详情
     */
    clearCurrentDefinition() {
      this.currentDefinition = null;
    },

    /**
     * 设置WebSocket连接状态
     * @param {Boolean} connected - 连接状态
     */
    setWebSocketConnected(connected) {
      this.wsConnected = connected;
      this.webSocketConnected = connected;
    },

    /**
     * 重置所有查询参数
     */
    resetAllQueryParams() {
      this.pendingTaskQueryParams = {
        pageNum: 1,
        pageSize: 20,
        category: null,
        priority: null,
        dueStatus: null,
      };
      this.completedTaskQueryParams = {
        pageNum: 1,
        pageSize: 20,
        category: null,
        outcome: null,
        startDate: null,
        endDate: null,
      };
      this.instanceQueryParams = {
        pageNum: 1,
        pageSize: 20,
        definitionId: null,
        status: null,
        startUserId: null,
        startDate: null,
        endDate: null,
      };
      this.myProcessQueryParams = {
        pageNum: 1,
        pageSize: 20,
        category: null,
        status: null,
      };
      this.definitionQueryParams = {
        pageNum: 1,
        pageSize: 20,
        category: null,
        status: null,
        keyword: null,
      };
    },
  },
});
