/**
 * 考勤管理API接口
 *
 * @Author:    Claude Code
 * @Date:      2025-11-05
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const attendanceApi = {
  // ===== 排班管理 =====
  // 获取排班日历数据
  getShiftCalendar: (params) =>
    getRequest('/attendance/shift/calendar', params),

  // 获取排班记录
  getShiftRecords: (params) =>
    getRequest('/attendance/shift/records', params),

  // 创建排班
  createShift: (data) =>
    postRequest('/attendance/shift/create', data),

  // 更新排班
  updateShift: (data) =>
    postRequest('/attendance/shift/update', data),

  // 删除排班
  deleteShift: (id) =>
    postRequest('/attendance/shift/delete', { id }),

  // 批量排班
  batchCreateShift: (data) =>
    postRequest('/attendance/shift/batch-create', data),

  // 获取排班模板
  getShiftTemplates: () =>
    getRequest('/attendance/shift/templates'),

  // 应用排班模板
  applyShiftTemplate: (data) =>
    postRequest('/attendance/shift/apply-template', data),

  // 导出排班
  exportShift: (params) =>
    getRequest('/attendance/shift/export', params, { responseType: 'blob' }),

  // ===== 异常申请 =====
  // 获取异常申请列表
  getExceptionApplications: (params) =>
    getRequest('/attendance/exception/applications', params),

  // 创建异常申请
  createExceptionApplication: (data) =>
    postRequest('/attendance/exception/create', data),

  // 更新异常申请
  updateExceptionApplication: (data) =>
    postRequest('/attendance/exception/update', data),

  // 撤销异常申请
  cancelExceptionApplication: (id) =>
    postRequest('/attendance/exception/cancel', { id }),

  // 获取申请详情
  getApplicationDetail: (id) =>
    getRequest(`/attendance/exception/detail/${id}`),

  // ===== 审批工作台 =====
  // 获取待审批列表
  getPendingApprovals: (params) =>
    getRequest('/attendance/approval/pending', params),

  // 审批申请
  approveApplication: (data) =>
    postRequest('/attendance/approval/approve', data),

  // 拒绝申请
  rejectApplication: (data) =>
    postRequest('/attendance/approval/reject', data),

  // 批量审批
  batchApproveApplications: (data) =>
    postRequest('/attendance/approval/batch-approve', data),

  // 获取审批历史
  getApprovalHistory: (params) =>
    getRequest('/attendance/approval/history', params),

  // 审批统计
  getApprovalStatistics: () =>
    getRequest('/attendance/approval/statistics'),

  // ===== 假种管理 =====
  // 获取假种列表
  getLeaveTypes: () =>
    getRequest('/attendance/leave/types'),

  // 创建假种
  createLeaveType: (data) =>
    postRequest('/attendance/leave/create-type', data),

  // 更新假种
  updateLeaveType: (data) =>
    postRequest('/attendance/leave/update-type', data),

  // 删除假种
  deleteLeaveType: (id) =>
    postRequest('/attendance/leave/delete-type', { id }),

  // ===== 统计报表 =====
  // 获取考勤统计
  getAttendanceStatistics: (params) =>
    getRequest('/attendance/statistics', params),

  // 获取部门考勤统计
  getDepartmentStatistics: (params) =>
    getRequest('/attendance/statistics/department', params),

  // 获取员工考勤明细
  getEmployeeAttendanceDetail: (params) =>
    getRequest('/attendance/statistics/employee', params),

  // 导出考勤报表
  exportAttendanceReport: (params) =>
    getRequest('/attendance/statistics/export', params, { responseType: 'blob' }),
};
