/**
 * 规则测试历史 API
 * 提供规则测试历史管理相关接口
 */
import { request } from '/@/utils/request';

/**
 * 分页查询测试历史
 */
export const queryHistoryPage = (data) => {
  return request.post('/api/v1/attendance/rule-test-history/page', data);
};

/**
 * 查询历史详情
 */
export const getHistoryDetail = (historyId) => {
  return request.get(`/api/v1/attendance/rule-test-history/${historyId}`);
};

/**
 * 查询规则的所有测试历史
 */
export const getRuleTestHistory = (ruleId) => {
  return request.get(`/api/v1/attendance/rule-test-history/rule/${ruleId}`);
};

/**
 * 查询最近的测试历史
 */
export const getRecentHistory = (limit = 10) => {
  return request.get('/api/v1/attendance/rule-test-history/recent', {
    params: { limit }
  });
};

/**
 * 统计规则测试次数
 */
export const countRuleTests = (ruleId) => {
  return request.get(`/api/v1/attendance/rule-test-history/count/${ruleId}`);
};

/**
 * 删除测试历史
 */
export const deleteHistory = (historyId) => {
  return request.delete(`/api/v1/attendance/rule-test-history/${historyId}`);
};

/**
 * 批量删除测试历史
 */
export const batchDeleteHistory = (historyIds) => {
  return request.delete('/api/v1/attendance/rule-test-history/batch', {
    data: historyIds
  });
};

/**
 * 清理过期历史
 */
export const cleanExpiredHistory = (days) => {
  return request.delete('/api/v1/attendance/rule-test-history/cleanup', {
    params: { days }
  });
};

/**
 * 导出测试历史为JSON
 */
export const exportHistoryToJson = (historyIds) => {
  return request.post('/api/v1/attendance/rule-test-history/export', historyIds);
};

/**
 * 从JSON导入测试历史
 */
export const importHistoryFromJson = (jsonData) => {
  return request.post('/api/v1/attendance/rule-test-history/import', jsonData);
};

/**
 * 导出测试配置模板
 */
export const exportTestTemplate = () => {
  return request.get('/api/v1/attendance/rule-test-history/export-template');
};

// 默认导出
export default {
  queryHistoryPage,
  getHistoryDetail,
  getRuleTestHistory,
  getRecentHistory,
  countRuleTests,
  deleteHistory,
  batchDeleteHistory,
  cleanExpiredHistory,
  exportHistoryToJson,
  importHistoryFromJson,
  exportTestTemplate
};
