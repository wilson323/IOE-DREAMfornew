/*
 * @fileoverview 请假管理 API
 * @author IOE-DREAM Team
 * @description 请假申请、审批、查询、销假等接口
 * @date 2025-01-30
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

/**
 * 请假管理 API
 */
export const leaveApi = {
  // ==================== 请假申请管理 ====================

  /**
   * 分页查询请假申请列表
   */
  queryLeaveList: (params: LeaveQueryForm) => {
    return getRequest('/api/v1/attendance/leave/list', params);
  },

  /**
   * 获取请假申请详情
   */
  getLeaveDetail: (leaveId: number) => {
    return getRequest(`/api/v1/attendance/leave/${leaveId}`);
  },

  /**
   * 创建请假申请
   */
  createLeave: (form: LeaveAddForm) => {
    return postRequest('/api/v1/attendance/leave', form);
  },

  /**
   * 更新请假申请
   */
  updateLeave: (leaveId: number, form: LeaveUpdateForm) => {
    return putRequest(`/api/v1/attendance/leave/${leaveId}`, form);
  },

  /**
   * 删除请假申请
   */
  deleteLeave: (leaveId: number) => {
    return deleteRequest(`/api/v1/attendance/leave/${leaveId}`);
  },

  /**
   * 取消请假申请
   */
  cancelLeave: (leaveId: number) => {
    return postRequest(`/api/v1/attendance/leave/${leaveId}/cancel`);
  },

  // ==================== 请假审批 ====================

  /**
   * 审批请假申请
   */
  approveLeave: (leaveId: number, form: LeaveApprovalForm) => {
    return postRequest(`/api/v1/attendance/leave/${leaveId}/approve`, form);
  },

  /**
   * 驳回请假申请
   */
  rejectLeave: (leaveId: number, form: LeaveRejectionForm) => {
    return postRequest(`/api/v1/attendance/leave/${leaveId}/reject`, form);
  },

  /**
   * 获取待审批请假列表
   */
  getPendingApprovalList: (params: LeaveQueryForm) => {
    return getRequest('/api/v1/attendance/leave/pending', params);
  },

  // ==================== 销假管理 ====================

  /**
   * 分页查询销假申请列表
   */
  queryCancellationList: (params: CancellationQueryForm) => {
    return getRequest('/api/v1/attendance/leave/cancellation/list', params);
  },

  /**
   * 获取销假申请详情
   */
  getCancellationDetail: (cancellationId: string) => {
    return getRequest(`/api/v1/attendance/leave/cancellation/${cancellationId}`);
  },

  /**
   * 创建销假申请
   */
  createCancellation: (form: CancellationAddForm) => {
    return postRequest('/api/v1/attendance/leave/cancellation', form);
  },

  /**
   * 审批销假申请
   */
  approveCancellation: (cancellationId: string, form: CancellationApprovalForm) => {
    return postRequest(`/api/v1/attendance/leave/cancellation/${cancellationId}/approve`, form);
  },

  /**
   * 驳回销假申请
   */
  rejectCancellation: (cancellationId: string, form: CancellationRejectionForm) => {
    return postRequest(`/api/v1/attendance/leave/cancellation/${cancellationId}/reject`, form);
  },

  // ==================== 请假统计 ====================

  /**
   * 获取请假统计数据
   */
  getLeaveStatistics: (params: LeaveStatisticsForm) => {
    return getRequest('/api/v1/attendance/leave/statistics', params);
  },

  /**
   * 获取员工请假汇总
   */
  getEmployeeLeaveSummary: (employeeId: number, year: number) => {
    return getRequest('/api/v1/attendance/leave/summary', { employeeId, year });
  },

  // ==================== 数据导出 ====================

  /**
   * 导出请假记录
   */
  exportLeaveRecords: (params: LeaveQueryForm) => {
    return postRequest('/api/v1/attendance/leave/export', params, {
      responseType: 'blob'
    });
  },

  /**
   * 导出请假统计报表
   */
  exportLeaveStatistics: (params: LeaveStatisticsForm) => {
    return postRequest('/api/v1/attendance/leave/statistics/export', params, {
      responseType: 'blob'
    });
  }
};

/**
 * 类型定义
 */

// ==================== 枚举类型 ====================

/**
 * 请假类型枚举
 */
export enum LeaveType {
  ANNUAL = 'ANNUAL',               // 年假
  SICK = 'SICK',                   // 病假
  PERSONAL = 'PERSONAL',           // 事假
  MARRIAGE = 'MARRIAGE',           // 婚假
  MATERNITY = 'MATERNITY',         // 产假
  PATERNITY = 'PATERNITY',         // 陪产假
  OTHER = 'OTHER'                  // 其他
}

/**
 * 请假状态枚举
 */
export enum LeaveStatus {
  PENDING = 'PENDING',             // 待审批
  APPROVED = 'APPROVED',           // 已通过
  REJECTED = 'REJECTED',           // 已驳回
  CANCELLED = 'CANCELLED'          // 已取消
}

/**
 * 销假类型枚举
 */
export enum CancellationType {
  FULL_CANCELLATION = 'FULL_CANCELLATION',       // 全部销假
  PARTIAL_CANCELLATION = 'PARTIAL_CANCELLATION', // 部分销假
  EARLY_RETURN = 'EARLY_RETURN',                 // 提前销假
  POSTPONED_RETURN = 'POSTPONED_RETURN',         // 延后销假
  CANCELLED_LEAVE = 'CANCELLED_LEAVE'            // 取消请假
}

/**
 * 销假申请状态枚举
 */
export enum CancellationStatus {
  DRAFT = 'DRAFT',                   // 草稿
  SUBMITTED = 'SUBMITTED',           // 已提交
  UNDER_REVIEW = 'UNDER_REVIEW',     // 审核中
  APPROVED = 'APPROVED',             // 已通过
  REJECTED = 'REJECTED',             // 已驳回
  WITHDRAWN = 'WITHDRAWN',           // 已撤销
  EFFECTIVE = 'EFFECTIVE',           // 已生效
  INVALID = 'INVALID'                // 已失效
}

/**
 * 紧急程度枚举
 */
export enum UrgencyLevel {
  NORMAL = 'NORMAL',                 // 普通
  URGENT = 'URGENT',                 // 紧急
  VERY_URGENT = 'VERY_URGENT'        // 非常紧急
}

// ==================== 表单对象 ====================

/**
 * 请假查询表单
 */
export interface LeaveQueryForm {
  employeeId?: number;
  employeeName?: string;
  departmentId?: number;
  leaveType?: LeaveType;
  status?: LeaveStatus;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 请假申请表单
 */
export interface LeaveAddForm {
  employeeId: number;
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  leaveDays: number;
  reason: string;
  remark?: string;
}

/**
 * 请假更新表单
 */
export interface LeaveUpdateForm {
  leaveType?: LeaveType;
  startDate?: string;
  endDate?: string;
  leaveDays?: number;
  reason?: string;
  remark?: string;
}

/**
 * 请假审批表单
 */
export interface LeaveApprovalForm {
  approvalComment: string;
}

/**
 * 请假驳回表单
 */
export interface LeaveRejectionForm {
  rejectionReason: string;
}

/**
 * 销假查询表单
 */
export interface CancellationQueryForm {
  status?: CancellationStatus;
  cancellationType?: CancellationType;
  urgencyLevel?: UrgencyLevel;
  employeeId?: number;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 销假申请表单
 */
export interface CancellationAddForm {
  originalLeaveId: string;
  employeeId: number;
  cancellationType: CancellationType;
  cancellationStartDate: string;
  cancellationEndDate: string;
  cancellationReason: string;
  cancellationReasonDetail?: string;
  urgencyLevel?: UrgencyLevel;
}

/**
 * 销假审批表单
 */
export interface CancellationApprovalForm {
  approvalComment: string;
}

/**
 * 销假驳回表单
 */
export interface CancellationRejectionForm {
  rejectionReason: string;
}

/**
 * 请假统计表单
 */
export interface LeaveStatisticsForm {
  startDate: string;
  endDate: string;
  departmentId?: number;
  groupBy?: 'DEPARTMENT' | 'LEAVE_TYPE' | 'EMPLOYEE';
}

// ==================== 响应数据 ====================

/**
 * 请假记录VO
 */
export interface LeaveRecordVO {
  id: number;
  leaveNo: string;
  employeeId: number;
  employeeName: string;
  departmentId: number;
  departmentName: string;
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  leaveDays: number;
  reason: string;
  status: LeaveStatus;
  approvalComment?: string;
  approvalTime?: string;
  createTime: string;
  remark?: string;
}

/**
 * 请假详情VO
 */
export interface LeaveDetailVO extends LeaveRecordVO {
  workflowInstanceId?: number;
  approvalHistory?: ApprovalRecord[];
  attachments?: AttachmentInfo[];
}

/**
 * 销假申请VO
 */
export interface CancellationVO {
  cancellationId: string;
  originalLeaveId: string;
  employeeId: number;
  employeeName: string;
  departmentName: string;
  originalLeaveType: LeaveType;
  originalLeaveStartDate: string;
  originalLeaveEndDate: string;
  originalLeaveDays: number;
  cancellationType: CancellationType;
  cancellationStartDate: string;
  cancellationEndDate: string;
  cancellationDays: number;
  cancellationReason: string;
  status: CancellationStatus;
  urgencyLevel?: UrgencyLevel;
  applicationTime: string;
  approvalCompletedTime?: string;
  effectiveTime?: string;
}

/**
 * 请假统计VO
 */
export interface LeaveStatisticsVO {
  totalCount: number;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
  cancelledCount: number;
  annualLeaveCount: number;
  sickLeaveCount: number;
  personalLeaveCount: number;
  totalLeaveDays: number;
}

/**
 * 部门请假统计VO
 */
export interface DepartmentLeaveStatisticsVO {
  departmentId: number;
  departmentName: string;
  totalCount: number;
  annualLeaveCount: number;
  sickLeaveCount: number;
  personalLeaveCount: number;
  totalLeaveDays: number;
  approvalRate: number;
}

/**
 * 员工请假汇总VO
 */
export interface EmployeeLeaveSummaryVO {
  employeeId: number;
  employeeName: string;
  year: number;
  annualLeaveUsed: number;
  annualLeaveRemaining: number;
  sickLeaveUsed: number;
  personalLeaveUsed: number;
  totalLeaveDays: number;
}

/**
 * 审批记录
 */
export interface ApprovalRecord {
  approvalId: string;
  approvalStep: string;
  approverId: number;
  approverName: string;
  approvalResult: 'APPROVED' | 'REJECTED' | 'RETURNED' | 'TRANSFERRED';
  approvalComment: string;
  approvalTime: string;
  isProxyApproval?: boolean;
  proxyApproverName?: string;
}

/**
 * 附件信息
 */
export interface AttachmentInfo {
  attachmentId: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  fileUrl: string;
  uploadTime: string;
}
