/*
 * 全局联动管理Store
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-01
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { globalLinkageApi } from '/@/api/business/access/global-linkage-api';

export const useGlobalLinkageStore = defineStore('globalLinkage', {
  state: () => ({
    // 联动规则列表数据
    ruleList: [],
    ruleTotal: 0,
    ruleLoading: false,

    // 联动历史数据
    linkageHistory: [],
    historyLoading: false,

    // 可联动设备列表
    linkageDevices: [],
    devicesLoading: false,

    // 触发条件类型
    triggerConditionTypes: [],

    // 联动动作类型
    linkageActionTypes: [],

    // 当前选中的规则
    currentRule: null,

    // 全局联动状态
    globalLinkageEnabled: true,

    // 查询参数
    queryParams: {
      pageNum: 1,
      pageSize: 10,
    },

    // 选中的规则ID列表
    selectedRuleIds: [],

    // 搜索参数
    searchParams: {
      ruleName: '',
      ruleType: '',
      status: '',
    },

    // 规则执行统计
    ruleStatsMap: new Map(),

    // WebSocket连接状态
    wsConnected: false,

    // 测试结果
    testResult: null,
  }),

  getters: {
    /**
     * 启用的规则数量
     */
    activeRuleCount(state) {
      return state.ruleList.filter(rule => rule.status === 'ACTIVE').length;
    },

    /**
     * 禁用的规则数量
     */
    inactiveRuleCount(state) {
      return state.ruleList.filter(rule => rule.status === 'INACTIVE').length;
    },

    /**
     * 今日执行次数
     */
    todayExecutionCount(state) {
      return state.ruleList.reduce((total, rule) => {
        return total + (rule.todayExecutionCount || 0);
      }, 0);
    },

    /**
     * 根据规则ID获取规则信息
     */
    getRuleById: (state) => (ruleId) => {
      return state.ruleList.find(rule => rule.ruleId === ruleId);
    },

    /**
     * 获取规则执行统计
     */
    getRuleStats: (state) => (ruleId) => {
      return state.ruleStatsMap.get(ruleId);
    },

    /**
     * 设备状态统计
     */
    deviceStats(state) {
      const stats = {
        online: 0,
        offline: 0,
        fault: 0,
        maintenance: 0,
        total: state.linkageDevices.length
      };

      state.linkageDevices.forEach(device => {
        const status = device.status || 'OFFLINE';
        if (stats.hasOwnProperty(status.toLowerCase())) {
          stats[status.toLowerCase()]++;
        }
      });

      return stats;
    },

    /**
     * 规则类型统计
     */
    ruleTypeStats(state) {
      const stats = {};
      state.ruleList.forEach(rule => {
        const type = rule.ruleType || 'UNKNOWN';
        stats[type] = (stats[type] || 0) + 1;
      });
      return stats;
    },
  },

  actions: {
    /**
     * 获取联动规则列表
     */
    async fetchRuleList(params = {}) {
      this.ruleLoading = true;
      try {
        const queryParams = { ...this.queryParams, ...this.searchParams, ...params };
        const response = await globalLinkageApi.queryLinkageRules(queryParams);

        if (response.code === 1) {
          this.ruleList = response.data.records || [];
          this.ruleTotal = response.data.total || 0;
          this.queryParams = queryParams;
          return response.data;
        } else {
          message.error(response.msg || '获取联动规则失败');
          return null;
        }
      } catch (error) {
        console.error('获取联动规则失败:', error);
        message.error('获取联动规则失败');
        return null;
      } finally {
        this.ruleLoading = false;
      }
    },

    /**
     * 获取联动规则详情
     */
    async fetchRuleDetail(ruleId) {
      try {
        const response = await globalLinkageApi.getLinkageRuleDetail(ruleId);
        if (response.code === 1) {
          this.currentRule = response.data;
          return response.data;
        } else {
          message.error(response.msg || '获取规则详情失败');
          return null;
        }
      } catch (error) {
        console.error('获取规则详情失败:', error);
        message.error('获取规则详情失败');
        return null;
      }
    },

    /**
     * 添加联动规则
     */
    async addRule(ruleData) {
      try {
        const response = await globalLinkageApi.addLinkageRule(ruleData);
        if (response.code === 1) {
          message.success('添加联动规则成功');
          await this.fetchRuleList();
          return true;
        } else {
          message.error(response.msg || '添加联动规则失败');
          return false;
        }
      } catch (error) {
        console.error('添加联动规则失败:', error);
        message.error('添加联动规则失败');
        return false;
      }
    },

    /**
     * 更新联动规则
     */
    async updateRule(ruleData) {
      try {
        const response = await globalLinkageApi.updateLinkageRule(ruleData);
        if (response.code === 1) {
          message.success('更新联动规则成功');
          await this.fetchRuleList();
          return true;
        } else {
          message.error(response.msg || '更新联动规则失败');
          return false;
        }
      } catch (error) {
        console.error('更新联动规则失败:', error);
        message.error('更新联动规则失败');
        return false;
      }
    },

    /**
     * 删除联动规则
     */
    async deleteRule(ruleId) {
      try {
        const response = await globalLinkageApi.deleteLinkageRule(ruleId);
        if (response.code === 1) {
          message.success('删除联动规则成功');
          await this.fetchRuleList();
          return true;
        } else {
          message.error(response.msg || '删除联动规则失败');
          return false;
        }
      } catch (error) {
        console.error('删除联动规则失败:', error);
        message.error('删除联动规则失败');
        return false;
      }
    },

    /**
     * 批量删除联动规则
     */
    async batchDeleteRules(ruleIds) {
      try {
        const response = await globalLinkageApi.batchDeleteLinkageRules(ruleIds);
        if (response.code === 1) {
          message.success(response.msg || '批量删除成功');
          this.selectedRuleIds = [];
          await this.fetchRuleList();
          return true;
        } else {
          message.error(response.msg || '批量删除失败');
          return false;
        }
      } catch (error) {
        console.error('批量删除失败:', error);
        message.error('批量删除失败');
        return false;
      }
    },

    /**
     * 更新规则状态
     */
    async updateRuleStatus(ruleId, status) {
      try {
        const response = await globalLinkageApi.updateRuleStatus(ruleId, status);
        if (response.code === 1) {
          message.success(`${status === 'ACTIVE' ? '启用' : '禁用'}规则成功`);
          // 更新本地状态
          const rule = this.ruleList.find(r => r.ruleId === ruleId);
          if (rule) {
            rule.status = status;
          }
          return true;
        } else {
          message.error(response.msg || '更新规则状态失败');
          return false;
        }
      } catch (error) {
        console.error('更新规则状态失败:', error);
        message.error('更新规则状态失败');
        return false;
      }
    },

    /**
     * 获取联动历史
     */
    async fetchLinkageHistory(params = {}) {
      this.historyLoading = true;
      try {
        const response = await globalLinkageApi.getLinkageHistory(params);
        if (response.code === 1) {
          this.linkageHistory = response.data.records || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取联动历史失败:', error);
        return [];
      } finally {
        this.historyLoading = false;
      }
    },

    /**
     * 测试联动规则
     */
    async testRule(ruleId, testParams = {}) {
      try {
        const response = await globalLinkageApi.testLinkageRule(ruleId, testParams);
        if (response.code === 1) {
          this.testResult = {
            success: true,
            data: response.data,
            message: '测试执行成功'
          };
          return this.testResult;
        } else {
          this.testResult = {
            success: false,
            data: null,
            message: response.msg || '测试执行失败'
          };
          return this.testResult;
        }
      } catch (error) {
        console.error('测试联动规则失败:', error);
        this.testResult = {
          success: false,
          data: null,
          message: '测试执行失败'
        };
        return this.testResult;
      }
    },

    /**
     * 获取可联动设备列表
     */
    async fetchLinkageDevices(params = {}) {
      this.devicesLoading = true;
      try {
        const response = await globalLinkageApi.getLinkageDevices(params);
        if (response.code === 1) {
          this.linkageDevices = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取联动设备失败:', error);
        return [];
      } finally {
        this.devicesLoading = false;
      }
    },

    /**
     * 获取触发条件类型
     */
    async fetchTriggerConditionTypes() {
      try {
        const response = await globalLinkageApi.getTriggerConditionTypes();
        if (response.code === 1) {
          this.triggerConditionTypes = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取触发条件类型失败:', error);
        return [];
      }
    },

    /**
     * 获取联动动作类型
     */
    async fetchLinkageActionTypes() {
      try {
        const response = await globalLinkageApi.getLinkageActionTypes();
        if (response.code === 1) {
          this.linkageActionTypes = response.data || [];
          return response.data;
        }
      } catch (error) {
        console.error('获取联动动作类型失败:', error);
        return [];
      }
    },

    /**
     * 批量操作规则
     */
    async batchOperateRules(operation, ruleIds) {
      try {
        const response = await globalLinkageApi.batchOperateRules({
          operation,
          ruleIds
        });
        if (response.code === 1) {
          message.success('批量操作成功');
          this.selectedRuleIds = [];
          await this.fetchRuleList();
          return true;
        } else {
          message.error(response.msg || '批量操作失败');
          return false;
        }
      } catch (error) {
        console.error('批量操作失败:', error);
        message.error('批量操作失败');
        return false;
      }
    },

    /**
     * 复制规则
     */
    async copyRule(ruleId, newRuleName) {
      try {
        const response = await globalLinkageApi.copyLinkageRule(ruleId, newRuleName);
        if (response.code === 1) {
          message.success('复制规则成功');
          await this.fetchRuleList();
          return true;
        } else {
          message.error(response.msg || '复制规则失败');
          return false;
        }
      } catch (error) {
        console.error('复制规则失败:', error);
        message.error('复制规则失败');
        return false;
      }
    },

    /**
     * 获取全局联动状态
     */
    async fetchGlobalLinkageStatus() {
      try {
        const response = await globalLinkageApi.getGlobalLinkageStatus();
        if (response.code === 1) {
          this.globalLinkageEnabled = response.data.enabled;
          return response.data;
        }
      } catch (error) {
        console.error('获取全局联动状态失败:', error);
        return null;
      }
    },

    /**
     * 切换全局联动状态
     */
    async toggleGlobalLinkage(enabled) {
      try {
        const response = await globalLinkageApi.toggleGlobalLinkage(enabled);
        if (response.code === 1) {
          this.globalLinkageEnabled = enabled;
          message.success(`${enabled ? '启用' : '禁用'}全局联动成功`);
          return true;
        } else {
          message.error(response.msg || '操作失败');
          return false;
        }
      } catch (error) {
        console.error('切换全局联动状态失败:', error);
        message.error('操作失败');
        return false;
      }
    },

    /**
     * 设置当前规则
     */
    setCurrentRule(rule) {
      this.currentRule = rule;
    },

    /**
     * 清空规则列表
     */
    clearRuleList() {
      this.ruleList = [];
      this.ruleTotal = 0;
    },

    /**
     * 设置搜索参数
     */
    setSearchParams(params) {
      this.searchParams = { ...this.searchParams, ...params };
    },

    /**
     * 重置搜索参数
     */
    resetSearchParams() {
      this.searchParams = {
        ruleName: '',
        ruleType: '',
        status: '',
      };
    },

    /**
     * 设置选中的规则ID列表
     */
    setSelectedRuleIds(ruleIds) {
      this.selectedRuleIds = ruleIds;
    },

    /**
     * 清空测试结果
     */
    clearTestResult() {
      this.testResult = null;
    },

    /**
     * 设置WebSocket连接状态
     */
    setWsConnected(connected) {
      this.wsConnected = connected;
    },

    /**
     * 添加新的联动历史记录
     */
    addLinkageHistory(record) {
      this.linkageHistory.unshift(record);
      // 保持最新的100条记录
      if (this.linkageHistory.length > 100) {
        this.linkageHistory = this.linkageHistory.slice(0, 100);
      }
    },

    /**
     * 更新规则执行统计
     */
    updateRuleStats(ruleId, stats) {
      this.ruleStatsMap.set(ruleId, stats);
    },
  },
});