/*
 * 工作流管理Store - 移动端版本
 * 提供工作流流程定义、实例、任务管理的状态管理
 * 严格遵循CLAUDE.md架构规范
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { workflowApi } from '@/api/workflow';

export const useWorkflowStore = defineStore({
  id: 'workflowStore',
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

    // ==================== 当前任务详情 ====================
    currentTask: null,
    taskLoading: false,

    // ==================== 当前流程实例详情 ====================
    currentInstance: null,
    instanceDetailLoading: false,

    // ==================== 流程历史记录 ====================
    processHistory: [],
    historyLoading: false,

    // ==================== 选中的任务ID列表 ====================
    selectedTaskIds: [],
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
  },

  actions: {
    /**
     * 获取待办任务列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 分页结果
     */
    async fetchPendingTaskList(params) {
      try {
        this.pendingTaskLoading = true;
        const queryParams = { ...this.pendingTaskQueryParams, ...params };
        const response = await workflowApi.pageMyTasks(queryParams);
        
        if (response.code === 1 || response.code === 200) {
          this.pendingTaskList = response.data?.list || [];
          this.pendingTaskTotal = response.data?.total || 0;
          this.pendingTaskQueryParams = { ...queryParams };
        } else {
          uni.showToast({
            title: response.message || '查询待办任务失败',
            icon: 'none',
          });
        }
        return response;
      } catch (error) {
        console.error('获取待办任务列表失败:', error);
        uni.showToast({
          title: '查询待办任务失败',
          icon: 'none',
        });
        throw error;
      } finally {
        this.pendingTaskLoading = false;
      }
    },

    /**
     * 获取已办任务列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 分页结果
     */
    async fetchCompletedTaskList(params) {
      try {
        this.completedTaskLoading = true;
        const queryParams = { ...this.completedTaskQueryParams, ...params };
        const response = await workflowApi.pageMyCompletedTasks(queryParams);
        
        if (response.code === 1 || response.code === 200) {
          this.completedTaskList = response.data?.list || [];
          this.completedTaskTotal = response.data?.total || 0;
          this.completedTaskQueryParams = { ...queryParams };
        } else {
          uni.showToast({
            title: response.message || '查询已办任务失败',
            icon: 'none',
          });
        }
        return response;
      } catch (error) {
        console.error('获取已办任务列表失败:', error);
        uni.showToast({
          title: '查询已办任务失败',
          icon: 'none',
        });
        throw error;
      } finally {
        this.completedTaskLoading = false;
      }
    },

    /**
     * 获取流程实例列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 分页结果
     */
    async fetchInstanceList(params) {
      try {
        this.instanceLoading = true;
        const queryParams = { ...this.instanceQueryParams, ...params };
        const response = await workflowApi.pageInstances(queryParams);
        
        if (response.code === 1 || response.code === 200) {
          this.instanceList = response.data?.list || [];
          this.instanceTotal = response.data?.total || 0;
          this.instanceQueryParams = { ...queryParams };
        } else {
          uni.showToast({
            title: response.message || '查询流程实例失败',
            icon: 'none',
          });
        }
        return response;
      } catch (error) {
        console.error('获取流程实例列表失败:', error);
        uni.showToast({
          title: '查询流程实例失败',
          icon: 'none',
        });
        throw error;
      } finally {
        this.instanceLoading = false;
      }
    },

    /**
     * 获取我发起的流程列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 分页结果
     */
    async fetchMyProcessList(params) {
      try {
        this.myProcessLoading = true;
        const queryParams = { ...this.myProcessQueryParams, ...params };
        const response = await workflowApi.pageMyProcesses(queryParams);
        
        if (response.code === 1 || response.code === 200) {
          this.myProcessList = response.data?.list || [];
          this.myProcessTotal = response.data?.total || 0;
          this.myProcessQueryParams = { ...queryParams };
        } else {
          uni.showToast({
            title: response.message || '查询我发起的流程失败',
            icon: 'none',
          });
        }
        return response;
      } catch (error) {
        console.error('获取我发起的流程列表失败:', error);
        uni.showToast({
          title: '查询我发起的流程失败',
          icon: 'none',
        });
        throw error;
      } finally {
        this.myProcessLoading = false;
      }
    },

    /**
     * 获取任务详情
     * @param {Number} taskId - 任务ID
     * @returns {Promise} 任务详情
     */
    async fetchTaskDetail(taskId) {
      try {
        this.taskLoading = true;
        const response = await workflowApi.getTask(taskId);
        
        if (response.code === 1 || response.code === 200) {
          this.currentTask = response.data;
        } else {
          uni.showToast({
            title: response.message || '获取任务详情失败',
            icon: 'none',
          });
        }
        return response;
      } catch (error) {
        console.error('获取任务详情失败:', error);
        uni.showToast({
          title: '获取任务详情失败',
          icon: 'none',
        });
        throw error;
      } finally {
        this.taskLoading = false;
      }
    },

    /**
     * 获取流程实例详情
     * @param {Number} instanceId - 流程实例ID
     * @returns {Promise} 流程实例详情
     */
    async fetchInstanceDetail(instanceId) {
      try {
        this.instanceDetailLoading = true;
        const response = await workflowApi.getInstance(instanceId);
        
        if (response.code === 1 || response.code === 200) {
          this.currentInstance = response.data;
        } else {
          uni.showToast({
            title: response.message || '获取流程实例详情失败',
            icon: 'none',
          });
        }
        return response;
      } catch (error) {
        console.error('获取流程实例详情失败:', error);
        uni.showToast({
          title: '获取流程实例详情失败',
          icon: 'none',
        });
        throw error;
      } finally {
        this.instanceDetailLoading = false;
      }
    },

    /**
     * 获取流程历史记录
     * @param {Number} instanceId - 流程实例ID
     * @returns {Promise} 历史记录列表
     */
    async fetchProcessHistory(instanceId) {
      try {
        this.historyLoading = true;
        const response = await workflowApi.getProcessHistory(instanceId);
        
        if (response.code === 1 || response.code === 200) {
          this.processHistory = response.data || [];
        } else {
          uni.showToast({
            title: response.message || '获取流程历史失败',
            icon: 'none',
          });
        }
        return response;
      } catch (error) {
        console.error('获取流程历史失败:', error);
        uni.showToast({
          title: '获取流程历史失败',
          icon: 'none',
        });
        throw error;
      } finally {
        this.historyLoading = false;
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
          uni.showToast({
            title: '任务受理成功',
            icon: 'success',
          });
          await this.fetchPendingTaskList(this.pendingTaskQueryParams);
          return true;
        } else {
          uni.showToast({
            title: response.message || '任务受理失败',
            icon: 'none',
          });
          return false;
        }
      } catch (error) {
        console.error('任务受理失败:', error);
        uni.showToast({
          title: '任务受理失败',
          icon: 'none',
        });
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
          uni.showToast({
            title: '任务完成成功',
            icon: 'success',
          });
          await this.fetchPendingTaskList(this.pendingTaskQueryParams);
          return true;
        } else {
          uni.showToast({
            title: response.message || '任务完成失败',
            icon: 'none',
          });
          return false;
        }
      } catch (error) {
        console.error('任务完成失败:', error);
        uni.showToast({
          title: '任务完成失败',
          icon: 'none',
        });
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
          uni.showToast({
            title: '任务驳回成功',
            icon: 'success',
          });
          await this.fetchPendingTaskList(this.pendingTaskQueryParams);
          return true;
        } else {
          uni.showToast({
            title: response.message || '任务驳回失败',
            icon: 'none',
          });
          return false;
        }
      } catch (error) {
        console.error('任务驳回失败:', error);
        uni.showToast({
          title: '任务驳回失败',
          icon: 'none',
        });
        return false;
      }
    },

    /**
     * 转办任务
     * @param {Number} taskId - 任务ID
     * @param {Number} targetUserId - 目标用户ID
     * @returns {Promise} 操作结果
     */
    async transferTask(taskId, targetUserId) {
      try {
        const response = await workflowApi.transferTask(taskId, targetUserId);
        if (response.code === 1 || response.code === 200) {
          uni.showToast({
            title: '任务转办成功',
            icon: 'success',
          });
          await this.fetchPendingTaskList(this.pendingTaskQueryParams);
          return true;
        } else {
          uni.showToast({
            title: response.message || '任务转办失败',
            icon: 'none',
          });
          return false;
        }
      } catch (error) {
        console.error('任务转办失败:', error);
        uni.showToast({
          title: '任务转办失败',
          icon: 'none',
        });
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
          uni.showToast({
            title: '任务委派成功',
            icon: 'success',
          });
          await this.fetchPendingTaskList(this.pendingTaskQueryParams);
          return true;
        } else {
          uni.showToast({
            title: response.message || '任务委派失败',
            icon: 'none',
          });
          return false;
        }
      } catch (error) {
        console.error('任务委派失败:', error);
        uni.showToast({
          title: '任务委派失败',
          icon: 'none',
        });
        return false;
      }
    },
  },
});

