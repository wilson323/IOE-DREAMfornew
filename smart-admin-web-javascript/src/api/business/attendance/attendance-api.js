/**
 * 考勤管理API接口
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-17
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { postRequest, getRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const attendanceApi = {
  // ===== 考勤打卡 =====

  /**
   * 上班打卡
   * @param {Object} data 打卡数据
   * @param {Number} data.employeeId 员工ID
   * @param {String} data.punchType 打卡类型:上班/下班
   * @param {String} data.punchTime 打卡时间
   * @param {Number} data.latitude 纬度(可选)
   * @param {Number} data.longitude 经度(可选)
   * @param {String} data.deviceId 设备ID
   * @param {String} data.location 地址描述
   * @param {String} data.photoBase64 打卡照片(可选)
   * @param {String} data.remark 备注
   */
  punchIn: (data) =>
    postRequest('/attendance/punch-in', data),

  /**
   * 下班打卡
   * @param {Object} data 打卡数据
   */
  punchOut: (data) =>
    postRequest('/attendance/punch-out', data),

  /**
   * 获取今日打卡记录
   */
  getTodayPunchRecord: () =>
    getRequest('/attendance/today-punch'),

  /**
   * 获取指定日期的打卡记录
   * @param {String} date 日期(YYYY-MM-DD)
   */
  getPunchRecordByDate: (date) =>
    getRequest(`/attendance/punch-record/${date}`),

  /**
   * 获取打卡记录列表
   * @param {Object} params 查询参数
   */
  getPunchRecords: (params) =>
    getRequest('/attendance/punch-records', params),

  /**
   * 获取考勤状态
   * @param {String} date 日期
   */
  getAttendanceStatus: (date) =>
    getRequest('/attendance/status', { date }),

  // ===== 考勤记录查询 =====

  /**
   * 分页查询考勤记录
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.employeeId 员工ID(可选)
   * @param {String} params.employeeName 员工姓名(可选)
   * @param {String} params.departmentId 部门ID(可选)
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {String} params.attendanceStatus 考勤状态(可选)
   */
  queryAttendanceRecords: (params) =>
    getRequest('/attendance/records', params),

  /**
   * 获取考勤记录详情
   * @param {Number} recordId 记录ID
   */
  getAttendanceRecordDetail: (recordId) =>
    getRequest(`/attendance/record/${recordId}`),

  /**
   * 导出考勤记录
   * @param {Object} params 导出参数
   */
  exportAttendanceRecords: (params) =>
    getRequest('/attendance/records/export', params, { responseType: 'blob' }),

  // ===== 考勤异常管理 =====

  /**
   * 获取异常考勤记录
   * @param {Object} params 查询参数
   */
  getAbnormalRecords: (params) =>
    getRequest('/attendance/abnormal-records', params),

  /**
   * 获取异常统计数据
   * @param {Object} params 统计参数
   */
  getAbnormalStatistics: (params) =>
    getRequest('/attendance/abnormal-statistics', params),

  /**
   * 处理异常记录
   * @param {Object} data 处理数据
   */
  handleAbnormalRecord: (data) =>
    postRequest('/attendance/handle-abnormal', data),

  // ===== 补卡申请 =====

  /**
   * 提交补卡申请
   * @param {Object} data 申请数据
   */
  submitPunchCorrection: (data) =>
    postRequest('/attendance/punch-correction', data),

  /**
   * 获取补卡申请列表
   * @param {Object} params 查询参数
   */
  getPunchCorrectionList: (params) =>
    getRequest('/attendance/punch-corrections', params),

  /**
   * 审批补卡申请
   * @param {Object} data 审批数据
   */
  approvePunchCorrection: (data) =>
    postRequest('/attendance/approve-punch-correction', data),

  /**
   * 撤销补卡申请
   * @param {Number} correctionId 申请ID
   */
  cancelPunchCorrection: (correctionId) =>
    postRequest('/attendance/cancel-punch-correction', { correctionId }),

  // ===== 考勤统计 =====

  /**
   * 获取考勤统计数据
   * @param {Object} params 统计参数
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {String} params.employeeId 员工ID(可选)
   * @param {String} params.departmentId 部门ID(可选)
   * @param {String} params.statisticsType 统计类型:daily/weekly/monthly/yearly
   */
  getAttendanceStatistics: (params) =>
    getRequest('/attendance/statistics', params),

  /**
   * 获取个人考勤统计
   * @param {Object} params 统计参数
   */
  getPersonalStatistics: (params) =>
    getRequest('/attendance/personal-statistics', params),

  /**
   * 获取部门考勤统计
   * @param {Object} params 统计参数
   */
  getDepartmentStatistics: (params) =>
    getRequest('/attendance/department-statistics', params),

  /**
   * 获取考勤趋势数据
   * @param {Object} params 查询参数
   */
  getAttendanceTrends: (params) =>
    getRequest('/attendance/trends', params),

  /**
   * 获取考勤日历数据
   * @param {Object} params 查询参数
   * @param {Number} params.year 年份
   * @param {Number} params.month 月份
   * @param {String} params.employeeId 员工ID(可选)
   */
  getAttendanceCalendar: (params) =>
    getRequest('/attendance/calendar', params),

  // ===== 排班管理 =====

  /**
   * 获取员工排班
   * @param {Object} params 查询参数
   * @param {String} params.employeeId 员工ID
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   */
  getEmployeeSchedule: (params) =>
    getRequest('/attendance/schedule/employee', params),

  /**
   * 获取部门排班
   * @param {Object} params 查询参数
   */
  getDepartmentSchedule: (params) =>
    getRequest('/attendance/schedule/department', params),

  /**
   * 创建或更新排班
   * @param {Object} data 排班数据
   */
  saveOrUpdateSchedule: (data) =>
    postRequest('/attendance/schedule', data),

  /**
   * 删除排班
   * @param {Number} scheduleId 排班ID
   */
  deleteSchedule: (scheduleId) =>
    deleteRequest(`/attendance/schedule/${scheduleId}`),

  /**
   * 批量删除排班
   * @param {Array} scheduleIds 排班ID列表
   */
  batchDeleteSchedule: (scheduleIds) =>
    postRequest('/attendance/schedule/batch-delete', { scheduleIds }),

  /**
   * 检查排班冲突
   * @param {Object} data 检查数据
   */
  checkScheduleConflict: (data) =>
    postRequest('/attendance/schedule/check-conflict', data),

  // ===== 考勤规则管理 =====

  /**
   * 获取考勤规则列表
   * @param {Object} params 查询参数
   */
  getAttendanceRules: (params) =>
    getRequest('/attendance/rules', params),

  /**
   * 获取员工适用的考勤规则
   * @param {Number} employeeId 员工ID
   */
  getEmployeeAttendanceRule: (employeeId) =>
    getRequest(`/attendance/rules/employee/${employeeId}`),

  /**
   * 保存或更新考勤规则
   * @param {Object} data 规则数据
   */
  saveOrUpdateAttendanceRule: (data) =>
    postRequest('/attendance/rules', data),

  /**
   * 删除考勤规则
   * @param {Number} ruleId 规则ID
   */
  deleteAttendanceRule: (ruleId) =>
    deleteRequest(`/attendance/rules/${ruleId}`),

  /**
   * 启用/禁用考勤规则
   * @param {Number} ruleId 规则ID
   * @param {Boolean} enabled 是否启用
   */
  toggleAttendanceRuleStatus: (ruleId, enabled) =>
    putRequest(`/attendance/rules/${ruleId}/status`, { enabled }),

  /**
   * 复制考勤规则
   * @param {Object} data 复制数据
   */
  copyAttendanceRule: (data) =>
    postRequest('/attendance/rules/copy', data),

  // ===== 移动端API =====

  /**
   * 移动端获取今日考勤信息
   */
  getMobileTodayInfo: () =>
    getRequest('/attendance/mobile/today-info'),

  /**
   * 移动端快速打卡
   * @param {Object} data 打卡数据
   */
  mobileQuickPunch: (data) =>
    postRequest('/attendance/mobile/quick-punch', data),

  /**
   * 移动端获取历史记录
   * @param {Object} params 查询参数
   */
  getMobileHistory: (params) =>
    getRequest('/attendance/mobile/history', params),

  /**
   * 获取移动端考勤统计
   * @param {Object} params 统计参数
   */
  getMobileStatistics: (params) =>
    getRequest('/attendance/mobile/statistics', params),

  // ===== 实时数据API =====

  /**
   * 获取实时考勤状态
   */
  getRealtimeStatus: () =>
    getRequest('/attendance/realtime/status'),

  /**
   * 获取实时考勤统计
   */
  getRealtimeStatistics: () =>
    getRequest('/attendance/realtime/statistics'),

  /**
   * 获取正在打卡的员工列表
   */
  getActivePunchEmployees: () =>
    getRequest('/attendance/realtime/active-employees'),

  // ===== 考勤报表 =====

  /**
   * 生成考勤报表
   * @param {Object} params 报表参数
   * @param {String} params.reportType 报表类型
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {Array} params.employeeIds 员工ID列表(可选)
   * @param {Array} params.departmentIds 部门ID列表(可选)
   */
  generateAttendanceReport: (params) =>
    postRequest('/attendance/reports/generate', params),

  /**
   * 导出考勤报表
   * @param {Object} params 导出参数
   */
  exportAttendanceReport: (params) =>
    getRequest('/attendance/reports/export', params, { responseType: 'blob' }),

  /**
   * 获取报表模板列表
   */
  getReportTemplates: () =>
    getRequest('/attendance/reports/templates'),

  /**
   * 保存自定义报表模板
   * @param {Object} data 模板数据
   */
  saveReportTemplate: (data) =>
    postRequest('/attendance/reports/templates', data),

  // ===== 考勤设置 =====

  /**
   * 获取考勤设置
   */
  getAttendanceSettings: () =>
    getRequest('/attendance/settings'),

  /**
   * 更新考勤设置
   * @param {Object} data 设置数据
   */
  updateAttendanceSettings: (data) =>
    putRequest('/attendance/settings', data),

  /**
   * 获取考勤设备配置
   */
  getAttendanceDeviceConfig: () =>
    getRequest('/attendance/settings/device-config'),

  /**
   * 更新考勤设备配置
   * @param {Object} data 设备配置
   */
  updateAttendanceDeviceConfig: (data) =>
    putRequest('/attendance/settings/device-config', data),

  // ===== 考勤通知 =====

  /**
   * 获取考勤通知设置
   */
  getNotificationSettings: () =>
    getRequest('/attendance/notifications/settings'),

  /**
   * 更新考勤通知设置
   * @param {Object} data 通知设置
   */
  updateNotificationSettings: (data) =>
    putRequest('/attendance/notifications/settings', data),

  /**
   * 发送考勤通知
   * @param {Object} data 通知数据
   */
  sendAttendanceNotification: (data) =>
    postRequest('/attendance/notifications/send', data),

  /**
   * 获取通知历史
   * @param {Object} params 查询参数
   */
  getNotificationHistory: (params) =>
    getRequest('/attendance/notifications/history', params),

};