/**
 * 规则性能测试 API
 * 提供规则性能测试管理相关接口
 */
import { request } from '/@/utils/request';

/**
 * 执行性能测试
 */
export const executePerformanceTest = (data) => {
  return request.post('/api/v1/attendance/rule-performance-test/execute', data);
};

/**
 * 查询测试结果
 */
export const getTestResult = (testId) => {
  return request.get(`/api/v1/attendance/rule-performance-test/${testId}`);
};

/**
 * 查询测试详情
 */
export const getTestDetail = (testId) => {
  return request.get(`/api/v1/attendance/rule-performance-test/${testId}/detail`);
};

/**
 * 查询最近的测试列表
 */
export const getRecentTests = (limit = 10) => {
  return request.get('/api/v1/attendance/rule-performance-test/recent', {
    params: { limit }
  });
};

/**
 * 取消正在运行的测试
 */
export const cancelTest = (testId) => {
  return request.post(`/api/v1/attendance/rule-performance-test/${testId}/cancel`);
};

/**
 * 删除测试记录
 */
export const deleteTest = (testId) => {
  return request.delete(`/api/v1/attendance/rule-performance-test/${testId}`);
};

/**
 * 批量删除测试记录
 */
export const batchDeleteTests = (testIds) => {
  return request.delete('/api/v1/attendance/rule-performance-test/batch', {
    data: testIds
  });
};

// 默认导出
export default {
  executePerformanceTest,
  getTestResult,
  getTestDetail,
  getRecentTests,
  cancelTest,
  deleteTest,
  batchDeleteTests
};
