/**
 * 规则覆盖率报告 API
 * 提供规则覆盖率统计和分析相关接口
 */
import { request } from '/@/utils/request';

/**
 * 生成覆盖率报告
 */
export const generateCoverageReport = (data) => {
  return request.post('/api/v1/attendance/rule-coverage-report/generate', data);
};

/**
 * 查询报告结果
 */
export const getReport = (reportId) => {
  return request.get(`/api/v1/attendance/rule-coverage-report/${reportId}`);
};

/**
 * 查询指定日期的报告
 */
export const getReportByDate = (reportDate) => {
  return request.get(`/api/v1/attendance/rule-coverage-report/date/${reportDate}`);
};

/**
 * 查询最近的报告列表
 */
export const getRecentReports = (limit = 10) => {
  return request.get('/api/v1/attendance/rule-coverage-report/recent', {
    params: { limit }
  });
};

/**
 * 查询覆盖率趋势数据
 */
export const getCoverageTrend = (startDate, endDate) => {
  return request.get('/api/v1/attendance/rule-coverage-report/trend', {
    params: { startDate, endDate }
  });
};

/**
 * 查询规则覆盖详情
 */
export const getRuleCoverageDetails = (reportId) => {
  return request.get(`/api/v1/attendance/rule-coverage-report/${reportId}/details`);
};

/**
 * 删除报告
 */
export const deleteReport = (reportId) => {
  return request.delete(`/api/v1/attendance/rule-coverage-report/${reportId}`);
};

/**
 * 批量删除报告
 */
export const batchDeleteReports = (reportIds) => {
  return request.delete('/api/v1/attendance/rule-coverage-report/batch', {
    data: reportIds
  });
};

// 默认导出
export default {
  generateCoverageReport,
  getReport,
  getReportByDate,
  getRecentReports,
  getCoverageTrend,
  getRuleCoverageDetails,
  deleteReport,
  batchDeleteReports
};
