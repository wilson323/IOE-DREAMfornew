/*
 * 门禁管理-全局联动API接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-01
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest } from '/@/lib/axios';

export const globalLinkageApi = {
  /**
   * 分页查询联动规则列表
   */
  queryLinkageRules: (params) => {
    return postRequest('/access/linkage/rule/query', params);
  },

  /**
   * 获取联动规则详情
   */
  getLinkageRuleDetail: (ruleId) => {
    return getRequest(`/access/linkage/rule/detail/${ruleId}`);
  },

  /**
   * 添加联动规则
   */
  addLinkageRule: (params) => {
    return postRequest('/access/linkage/rule/add', params);
  },

  /**
   * 更新联动规则
   */
  updateLinkageRule: (params) => {
    return postRequest('/access/linkage/rule/update', params);
  },

  /**
   * 删除联动规则
   */
  deleteLinkageRule: (ruleId) => {
    return getRequest(`/access/linkage/rule/delete/${ruleId}`);
  },

  /**
   * 批量删除联动规则
   */
  batchDeleteLinkageRules: (ruleIds) => {
    return postRequest('/access/linkage/rule/batch/delete', ruleIds);
  },

  /**
   * 更新规则状态
   */
  updateRuleStatus: (ruleId, status) => {
    return postRequest('/access/linkage/rule/update/status', { ruleId, status });
  },

  /**
   * 获取联动规则执行历史
   */
  getLinkageHistory: (params) => {
    return postRequest('/access/linkage/history/query', params);
  },

  /**
   * 触发联动规则测试
   */
  testLinkageRule: (ruleId, testParams) => {
    return postRequest(`/access/linkage/rule/test/${ruleId}`, testParams);
  },

  /**
   * 获取可联动的设备列表
   */
  getLinkageDevices: (params) => {
    return postRequest('/access/linkage/device/list', params);
  },

  /**
   * 获取触发条件类型
   */
  getTriggerConditionTypes: () => {
    return getRequest('/access/linkage/condition/types');
  },

  /**
   * 获取联动动作类型
   */
  getLinkageActionTypes: () => {
    return getRequest('/access/linkage/action/types');
  },

  /**
   * 批量操作联动规则
   */
  batchOperateRules: (params) => {
    return postRequest('/access/linkage/rule/batch/operate', params);
  },

  /**
   * 复制联动规则
   */
  copyLinkageRule: (ruleId, newRuleName) => {
    return postRequest('/access/linkage/rule/copy', { ruleId, newRuleName });
  },

  /**
   * 导出联动规则
   */
  exportLinkageRules: (params) => {
    return postRequest('/access/linkage/rule/export', params);
  },

  /**
   * 导入联动规则
   */
  importLinkageRules: (file) => {
    return postRequest('/access/linkage/rule/import', file);
  },

  /**
   * 获取联动规则执行统计
   */
  getLinkageRuleStats: (ruleId) => {
    return getRequest(`/access/linkage/rule/stats/${ruleId}`);
  },

  /**
   * 清空执行历史
   */
  clearLinkageHistory: (params) => {
    return postRequest('/access/linkage/history/clear', params);
  },

  /**
   * 获取全局联动状态
   */
  getGlobalLinkageStatus: () => {
    return getRequest('/access/linkage/global/status');
  },

  /**
   * 启用/禁用全局联动
   */
  toggleGlobalLinkage: (enabled) => {
    return postRequest('/access/linkage/global/toggle', { enabled });
  }
};