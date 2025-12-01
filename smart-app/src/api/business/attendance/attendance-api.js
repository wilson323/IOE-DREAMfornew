// 考勤模块API接口
import { getRequest, postRequest, putRequest, deleteRequest } from '@/lib/smart-request'

// 考勤打卡相关接口
export const attendanceApi = {
  // 上班打卡
  punchIn: (data) => postRequest('/attendance/punch-in', data),

  // 下班打卡
  punchOut: (data) => postRequest('/attendance/punch-out', data),

  // 获取今日打卡记录
  getTodayPunchRecord: () => getRequest('/attendance/today-punch'),

  // 查询考勤记录
  queryAttendanceRecords: (params) => getRequest('/attendance/records', params),

  // 获取考勤记录详情
  getAttendanceRecordDetail: (recordId) => getRequest(`/attendance/record/${recordId}`),

  // 申请补卡
  applyPunchCorrection: (data) => postRequest('/attendance/correction/apply', data),

  // 查询补卡申请记录
  queryPunchCorrections: (params) => getRequest('/attendance/correction/list', params),

  // 获取补卡申请详情
  getPunchCorrectionDetail: (correctionId) => getRequest(`/attendance/correction/${correctionId}`),

  // 撤销补卡申请
  cancelPunchCorrection: (correctionId) => deleteRequest(`/attendance/correction/${correctionId}`)
}

// 排班管理相关接口
export const scheduleApi = {
  // 获取员工排班信息
  getEmployeeSchedule: (params) => getRequest('/attendance/schedule', params),

  // 获取月度排班信息
  getMonthSchedule: (params) => getRequest('/attendance/schedule/month', params),

  // 获取排班详情
  getScheduleDetail: (scheduleId) => getRequest(`/attendance/schedule/${scheduleId}`),

  // 批量设置排班
  batchSetSchedule: (data) => postRequest('/attendance/schedule/batch', data),

  // 复制排班
  copySchedule: (data) => postRequest('/attendance/schedule/copy', data)
}

// 考勤统计相关接口
export const statisticsApi = {
  // 个人考勤统计
  getPersonalStatistics: (params) => getRequest('/attendance/statistics/personal', params),

  // 部门考勤统计
  getDepartmentStatistics: (params) => getRequest('/attendance/statistics/department', params),

  // 公司考勤统计
  getCompanyStatistics: (params) => getRequest('/attendance/statistics/company', params),

  // 导出考勤统计
  exportStatistics: (params) => getRequest('/attendance/statistics/export', params, { responseType: 'blob' }),

  // 获取统计图表数据
  getStatisticsChart: (params) => getRequest('/attendance/statistics/chart', params)
}

// 考勤规则相关接口
export const ruleApi = {
  // 获取员工考勤规则
  getEmployeeRules: (employeeId) => getRequest(`/attendance/rule/employee/${employeeId}`),

  // 获取部门考勤规则
  getDepartmentRules: (departmentId) => getRequest(`/attendance/rule/department/${departmentId}`),

  // 获取考勤规则详情
  getRuleDetail: (ruleId) => getRequest(`/attendance/rule/${ruleId}`),

  // 更新考勤规则
  updateRule: (ruleId, data) => putRequest(`/attendance/rule/${ruleId}`, data)
}