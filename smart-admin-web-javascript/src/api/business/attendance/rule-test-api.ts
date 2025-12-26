/**
 * 考勤规则测试 API
 * 提供规则测试工具相关接口
 */
import { request } from '/@/utils/request';

/**
 * 测试单个规则
 */
export const testRule = (ruleId, data) => {
  return request.post(`/api/v1/attendance/rule-test/test/${ruleId}`, data);
};

/**
 * 测试自定义规则
 */
export const testCustomRule = (data) => {
  return request.post('/api/v1/attendance/rule-test/test-custom', data);
};

/**
 * 批量测试规则
 */
export const batchTestRules = (ruleIds, data) => {
  return request.post('/api/v1/attendance/rule-test/batch', data, {
    params: { ruleIds }
  });
};

/**
 * 快速测试（使用默认数据）
 */
export const quickTest = (ruleCondition, ruleAction) => {
  return request.post('/api/v1/attendance/rule-test/quick', null, {
    params: { ruleCondition, ruleAction }
  });
};

/**
 * 生成测试数据
 */
export const generateTestData = (scenario) => {
  return request.get(`/api/v1/attendance/rule-test/generate/${scenario}`);
};

/**
 * 验证规则语法
 */
export const validateRuleSyntax = (ruleCondition, ruleAction) => {
  return request.post('/api/v1/attendance/rule-test/validate', null, {
    params: { ruleCondition, ruleAction }
  });
};

/**
 * 获取测试场景列表
 */
export const getTestScenarios = () => {
  return request.get('/api/v1/attendance/rule-test/scenarios');
};

// 默认导出
export default {
  testRule,
  testCustomRule,
  batchTestRules,
  quickTest,
  generateTestData,
  validateRuleSyntax,
  getTestScenarios
};
