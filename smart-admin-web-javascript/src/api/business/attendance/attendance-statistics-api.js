/**
 * 考勤统计报表API接口
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-24
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { postRequest, getRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const attendanceStatisticsApi = {
  // ===== 统计概览 =====

  /**
   * 获取考勤统计概览
   * @param {Object} params 查询参数
   * @param {String} params.statisticsType 统计类型: personal/department/company
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {Number} params.employeeId 员工ID(仅个人统计时使用)
   * @param {Number} params.departmentId 部门ID(仅部门统计时使用)
   */
  getAttendanceOverview: (params) =>
    getRequest('/attendance/statistics/overview', params),

  /**
   * 获取个人考勤统计
   * @param {Object} params 查询参数
   */
  getPersonalStatistics: (params) =>
    getRequest('/attendance/statistics/personal', params),

  /**
   * 获取部门考勤统计
   * @param {Object} params 查询参数
   */
  getDepartmentStatistics: (params) =>
    getRequest('/attendance/statistics/department', params),

  /**
   * 获取公司考勤统计
   * @param {Object} params 查询参数
   */
  getCompanyStatistics: (params) =>
    getRequest('/attendance/statistics/company', params),

  /**
   * 获取考勤统计数据
   * @param {Object} params 查询参数
   * @param {String} params.statisticsType 统计类型:daily/weekly/monthly/yearly
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {Array} params.departmentIds 部门ID列表(可选)
   * @param {Array} params.employeeIds 员工ID列表(可选)
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   */
  getAttendanceStatistics: (params) =>
    getRequest('/attendance/statistics', params),

  // ===== 趋势数据 =====

  /**
   * 获取考勤趋势数据
   * @param {Object} params 查询参数
   */
  getAttendanceTrends: (params) =>
    getRequest('/attendance/statistics/trends', params),

  /**
   * 获取出勤率趋势
   * @param {Object} params 查询参数
   */
  getAttendanceRateTrends: (params) =>
    getRequest('/attendance/statistics/attendance-rate-trends', params),

  /**
   * 获取工作时长趋势
   * @param {Object} params 查询参数
   */
  getWorkHoursTrends: (params) =>
    getRequest('/attendance/statistics/work-hours-trends', params),

  /**
   * 获取异常趋势
   * @param {Object} params 查询参数
   */
  getAbnormalTrends: (params) =>
    getRequest('/attendance/statistics/abnormal-trends', params),

  // ===== 详细数据 =====

  /**
   * 获取详细统计数据列表
   * @param {Object} params 查询参数
   * @param {String} params.statisticsType 统计类型
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {Array} params.departmentIds 部门ID列表(可选)
   * @param {Array} params.employeeIds 员工ID列表(可选)
   * @param {String} params.groupBy 分组字段:department/employee
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   */
  getStatisticsDetails: (params) =>
    getRequest('/attendance/statistics/details', params),

  /**
   * 获取员工考勤统计列表
   * @param {Object} params 查询参数
   */
  getEmployeeStatisticsList: (params) =>
    getRequest('/attendance/statistics/employee-list', params),

  /**
   * 获取部门考勤统计列表
   * @param {Object} params 查询参数
   */
  getDepartmentStatisticsList: (params) =>
    getRequest('/attendance/statistics/department-list', params),

  /**
   * 获取异常统计列表
   * @param {Object} params 查询参数
   */
  getAbnormalStatisticsList: (params) =>
    getRequest('/attendance/statistics/abnormal-list', params),

  // ===== 图表数据 =====

  /**
   * 获取趋势图表数据
   * @param {Object} params 查询参数
   */
  getTrendChartData: (params) =>
    getRequest('/attendance/statistics/trend-chart-data', params),

  /**
   * 获取状态分布图表数据
   * @param {Object} params 查询参数
   */
  getStatusDistributionChartData: (params) =>
    getRequest('/attendance/statistics/status-distribution-chart-data', params),

  /**
   * 获取部门对比图表数据
   * @param {Object} params 查询参数
   */
  getDepartmentComparisonChartData: (params) =>
   getRequest('/attendance/statistics/department-comparison-chart-data', params),

  /**
   * 获取个人图表数据
   * @param {Object} params 查询参数
   */
  getPersonalChartData: (params) =>
    getRequest('/attendance/statistics/personal-chart-data', params),

  /**
   * 获取异常统计图表数据
   * @param {Object} params 查询参数
   */
  getAbnormalChartData: (params) =>
    getRequest('/attendance/statistics/abnormal-chart-data', params),

  /**
   * 获取热力图表数据
   @param {Object} params 查询参数
   */
  getHeatmapChartData: (params) =>
    getRequest('/attendance/statistics/heatmap-chart-data', params),

  // ===== 报表生成 =====

  /**
   * 生成统计报表
   * @param {Object} data 报表配置
   * @param {String} data.reportType 报表类型
   * @param {String} data.statisticsType 统计类型
   * @param {String} data.startDate 开始日期
   * @param {String} data.endDate 结束日期
   * @param {Array} data.departmentIds 部门ID列表(可选)
   * @param {Array} data.employeeIds 员工ID列表(可选)
   * @param {Object} data.chartConfig 图表配置
   @param {Object} data.exportConfig 导出配置
   */
  generateStatisticsReport: (data) =>
    postRequest('/attendance/statistics/generate-report', data),

  /**
   * 获取报表模板列表
   */
  getReportTemplates: () =>
    getRequest('/attendance/statistics/report-templates'),

  /**
   * 保存自定义报表模板
   * @param {Object} data 模板数据
   */
  saveReportTemplate: (data) =>
    postRequest('/attendance/statistics/report-templates', data),

  /**
   * 获取报表生成进度
   * @param {String} reportId 报表ID
   */
  getReportGenerationProgress: (reportId) =>
    getRequest(`/attendance/statistics/report-progress/${reportId}`),

  /**
   * 取消报表生成
   * @param {String} reportId 报表ID
   */
  cancelReportGeneration: (reportId) =>
    postRequest(`/attendance/statistics/cancel-report/${reportId}`),

  /**
   * 下载生成的报表
   * @param {String} reportId 报表ID
   */
  downloadReport: (reportId) =>
    getRequest(`/attendance/statistics/download-report/${reportId}`, {}, { responseType: 'blob' }),

  /**
   * 获取报表历史记录
   * @param {Object} params 查询参数
   */
  getReportHistory: (params) =>
    getRequest('/attendance/statistics/report-history', params),

  /**
   * 删除报表记录
   * @param {String} reportId 报表ID
   */
  deleteReport: (reportId) =>
    deleteRequest(`/attendance/statistics/reports/${reportId}`),

  // ===== 数据导出 =====

  /**
   * 导出考勤统计报表
   * @param {Object} params 导出参数
   * @param {String} params.exportType 导出类型:excel/pdf/csv
   * @param {String} params.statisticsType 统计类型
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {Array} params.departmentIds 部门ID列表(可选)
   * @param {Array} params.employeeIds 员工ID列表(可选)
   * @param {Object} params.exportConfig 导出配置
   */
  exportStatisticsReport: (params) =>
    getRequest('/attendance/statistics/export', params, { responseType: 'blob' }),

  /**
   * 导出部门对比报表
   * @param {Object} params 导出参数
   */
  exportDepartmentComparisonReport: (params) =>
    getRequest('/attendance/statistics/export-department-comparison', params, { responseType: 'blob' }),

  /**
   * 导出个人考勤明细
   * @param {Object} params 导出参数
   */
  exportPersonalAttendanceReport: (params) =>
    getRequest('/attendance/statistics/export-personal-attendance', params, { responseType: 'blob' }),

  /**
   * 导出异常统计报表
   * @param {Object} params 导出参数
   */
  exportAbnormalReport: (params) =>
    getRequest('/attendance/statistics/export-abnormal-report', params, { responseType: 'blob' }),

  // ===== 数据分析 =====

  /**
   * 获取考勤模式分析
   * @param {Object} params 分析参数
   */
  getAttendancePatternAnalysis: (params) =>
    getRequest('/attendance/statistics/pattern-analysis', params),

  /**
   * 获取工作效率分析
   * @param {Object} params 分析参数
   */
  getWorkEfficiencyAnalysis: (params) =>
    getRequest('/attendance/statistics/work-efficiency-analysis', params),

  /**
   * 获取出勤质量分析
   * @param {Object} params 分析参数
   */
  getAttendanceQualityAnalysis: (params) =>
    getRequest('/attendance/statistics/quality-analysis', params),

  /**
   * 获取员工考勤画像
   * @param {Number} employeeId 员工ID
   * @param {Object} params 查询参数
   */
  getEmployeeProfileAnalysis: (employeeId, params) =>
    getRequest(`/attendance/statistics/employee-profile/${employeeId}`, params),

  /**
   * 获取部门考勤画像
   * @param {Number} departmentId 部门ID
   * @param {Object} params 查询参数
   */
  getDepartmentProfileAnalysis: (departmentId, params) =>
    getRequest(`/attendance/statistics/department-profile/${departmentId}`, params),

  /**
   * 获取考勤预测分析
   * @param {Object} params 预测参数
   */
  getAttendancePrediction: (params) =>
    getRequest('/attendance/statistics/attendance-prediction', params),

  // ===== 对比分析 =====

  /**
   * 获取期间对比分析
   * @param {Object} params 对比参数
   */
  getPeriodComparison: (params) =>
    getRequest('/attendance/statistics/period-comparison', params),

  /**
   * 获取部门对比分析
   * @param {Object} params 对比参数
   */
  getDepartmentComparison: (params) =>
    getRequest('/attendance/statistics/department-comparison', params),

  /**
   * 获取员工对比分析
   * @param {Object} params 对比参数
   */
  getEmployeeComparison: (params) =>
    getRequest('/attendance/statistics/employee-comparison', params),

  /**
   * 获取同期对比分析
   * @param {Object} params 对比参数
   */
  getYoYComparison: (params) =>
    getRequest('/attendance/statistics/yoy-comparison', params),

  // ===== 实时监控数据 =====

  /**
   * 获取实时考勤状态
   * @param {Object} params 查询参数
   */
  getRealTimeAttendanceStatus: (params) =>
    getRequest('/attendance/statistics/realtime-status', params),

  /**
   * 获取今日实时统计
   */
  getTodayRealtimeStatistics: () =>
    getRequest('/attendance/statistics/today-realtime-statistics'),

  /**
   * 获取实时出勤率
   * @param {Object} params 查询参数
   */
  getRealtimeAttendanceRate: (params) =>
    getRequest('/attendance/statistics/realtime-attendance-rate', params),

  /**
   * 获取实时异常监控
   */
  getRealtimeAbnormalMonitoring: () =>
    getRequest('/attendance/statistics/realtime-abnormal-monitoring'),

  // ===== 预警设置 =====

  /**
   * 获取统计预警规则
   */
  getStatisticsAlertRules: () =>
    getRequest('/attendance/statistics/alert-rules'),

  /**
   * 保存统计预警规则
   * @param {Object} data 预警规则
   */
  saveStatisticsAlertRules: (data) =>
    postRequest('/attendance/statistics/alert-rules', data),

  /**
   * 测试预警规则
   * @param {Object} data 测试数据
   */
  testStatisticsAlertRule: (data) =>
    postRequest('/attendance/statistics/test-alert-rule', data),

  /**
   * 获取预警历史记录
   * @param {Object} params 查询参数
   */
  getAlertHistory: (params) =>
    getRequest('/attendance/statistics/alert-history', params),

  // ===== 数据缓存管理 =====

  /**
   * 清除统计缓存
   * @param {Object} params 清除参数
   */
  clearStatisticsCache: (params) =>
    postRequest('/attendance/statistics/clear-cache', params),

  /**
   * 预热统计缓存
   * @param {Object} params 预热参数
   */
  warmupStatisticsCache: (params) =>
    postRequest('/attendance/statistics/warmup-cache', params),

  /**
   * 获取缓存状态
   * @param {Object} params 查询参数
   */
  getCacheStatus: (params) =>
    getRequest('/attendance/statistics/cache-status', params),

  // ===== 辅助功能 =====

  /**
   * 获取统计类型选项
   */
  getStatisticsTypeOptions: () =>
    getRequest('/attendance/statistics/statistics-type-options'),

  /**
   * 获取时间范围选项
   */
  getTimeRangeOptions: () =>
    getRequest('/attendance/statistics/time-range-options'),

  /**
   * 获取分析维度选项
   */
  getAnalysisDimensionOptions: () =>
    getRequest('/attendance/statistics/analysis-dimension-options'),

  /**
   * 验证查询参数
   * @param {Object} params 验证参数
   */
  validateQueryParams: (params) =>
    postRequest('/attendance/statistics/validate-query-params', params),

  /**
   * 获取统计数据元信息
   * @param {Object} params 元信息查询参数
   */
  getStatisticsMetadata: (params) =>
    getRequest('/attendance/statistics/metadata', params),

  /**
   * 获取推荐报表
   * @param {Object} params 推荐参数
   */
  getRecommendedReports: (params) =>
    getRequest('/attendance/statistics/recommended-reports', params),

  /**
   * 获取统计配置
   */
  getStatisticsConfig: () =>
    getRequest('/attendance/statistics/config'),

  /**
   * 保存统计配置
   * @param {Object} config 配置数据
   */
  saveStatisticsConfig: (config) =>
    putRequest('/attendance/statistics/config', config),

  /**
   * 重置统计配置
   */
  resetStatisticsConfig: () =>
    postRequest('/attendance/statistics/reset-config'),

  /**
   * 获取数据权限范围
   * @param {Object} params 权限查询参数
   */
  getDataScope: (params) =>
    getRequest('/attendance/statistics/data-scope', params),

  /**
   * 检查数据权限
   * @param {Object} params 权限检查参数
   */
  checkDataPermission: (params) =>
    postRequest('/attendance/statistics/check-data-permission', params),

  /**
   * 获取用户统计权限
   */
  getUserStatisticsPermissions: () =>
    getRequest('/attendance/statistics/user-statistics-permissions'),
};