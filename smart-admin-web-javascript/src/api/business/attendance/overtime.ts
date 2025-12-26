/*
 * @fileoverview 加班管理 API
 * @author IOE-DREAM Team
 * @description 加班申请、审批、查询等接口
 * @date 2025-01-30
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

/**
 * 加班管理 API
 */
export const overtimeApi = {
  // ==================== 加班申请管理 ====================

  /**
   * 分页查询加班申请列表
   */
  queryOvertimeList: (params: OvertimeQueryForm) => {
    return getRequest('/api/v1/attendance/overtime/list', params);
  },

  /**
   * 获取加班申请详情
   */
  getOvertimeDetail: (overtimeId: number) => {
    return getRequest(`/api/v1/attendance/overtime/${overtimeId}`);
  },

  /**
   * 提交加班申请
   */
  submitOvertime: (form: OvertimeAddForm) => {
    return postRequest('/api/v1/attendance/overtime/submit', form);
  },

  /**
   * 更新加班申请
   */
  updateOvertime: (overtimeId: number, form: OvertimeUpdateForm) => {
    return putRequest(`/api/v1/attendance/overtime/${overtimeId}`, form);
  },

  /**
   * 删除加班申请
   */
  deleteOvertime: (overtimeId: number) => {
    return deleteRequest(`/api/v1/attendance/overtime/${overtimeId}`);
  },

  /**
   * 取消加班申请
   */
  cancelOvertime: (overtimeNo: string) => {
    return postRequest(`/api/v1/attendance/overtime/${overtimeNo}/cancel`);
  },

  // ==================== 加班审批 ====================

  /**
   * 审批加班申请
   */
  approveOvertime: (overtimeNo: string, form: OvertimeApprovalForm) => {
    return putRequest(`/api/v1/attendance/overtime/${overtimeNo}/status`, {
      status: 'APPROVED',
      approvalComment: form.approvalComment,
    });
  },

  /**
   * 驳回加班申请
   */
  rejectOvertime: (overtimeNo: string, form: OvertimeRejectionForm) => {
    return putRequest(`/api/v1/attendance/overtime/${overtimeNo}/status`, {
      status: 'REJECTED',
      approvalComment: form.rejectionReason,
    });
  },

  /**
   * 获取待审批加班列表
   */
  getPendingApprovalList: (params: OvertimeQueryForm) => {
    return getRequest('/api/v1/attendance/overtime/pending', params);
  },

  // ==================== 加班统计 ====================

  /**
   * 获取加班统计数据
   */
  getOvertimeStatistics: (params: OvertimeStatisticsForm) => {
    return getRequest('/api/v1/attendance/overtime/statistics', params);
  },

  /**
   * 获取员工加班汇总
   */
  getEmployeeOvertimeSummary: (employeeId: number, year: number, month?: number) => {
    return getRequest('/api/v1/attendance/overtime/summary', {
      employeeId,
      year,
      month,
    });
  },

  // ==================== 数据导出 ====================

  /**
   * 导出加班记录
   */
  exportOvertimeRecords: (params: OvertimeQueryForm) => {
    return postRequest('/api/v1/attendance/overtime/export', params, {
      responseType: 'blob',
    });
  },

  /**
   * 导出加班统计报表
   */
  exportOvertimeStatistics: (params: OvertimeStatisticsForm) => {
    return postRequest('/api/v1/attendance/overtime/statistics/export', params, {
      responseType: 'blob',
    });
  },
};

/**
 * 类型定义
 */

// ==================== 枚举类型 ====================

/**
 * 加班状态枚举
 */
export enum OvertimeStatus {
  PENDING = 'PENDING',         // 待审批
  APPROVED = 'APPROVED',       // 已通过
  REJECTED = 'REJECTED',       // 已驳回
  CANCELLED = 'CANCELLED'      // 已取消
}

/**
 * 加班类型枚举
 */
export enum OvertimeType {
  WORKDAY = 'WORKDAY',         // 工作日加班
  WEEKEND = 'WEEKEND',         // 周末加班
  HOLIDAY = 'HOLIDAY'          // 法定节假日加班
}

// ==================== 表单对象 ====================

/**
 * 加班查询表单
 */
export interface OvertimeQueryForm {
  employeeId?: number;
  employeeName?: string;
  departmentId?: number;
  status?: OvertimeStatus;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 加班申请表单
 */
export interface OvertimeAddForm {
  employeeId: number;
  overtimeDate: string;
  startTime: string;
  endTime: string;
  overtimeHours: number;
  reason: string;
  remark?: string;
}

/**
 * 加班更新表单
 */
export interface OvertimeUpdateForm {
  overtimeDate?: string;
  startTime?: string;
  endTime?: string;
  overtimeHours?: number;
  reason?: string;
  remark?: string;
}

/**
 * 加班审批表单
 */
export interface OvertimeApprovalForm {
  approvalComment: string;
}

/**
 * 加班驳回表单
 */
export interface OvertimeRejectionForm {
  rejectionReason: string;
}

/**
 * 加班统计表单
 */
export interface OvertimeStatisticsForm {
  startDate: string;
  endDate: string;
  departmentId?: number;
  groupBy?: 'DEPARTMENT' | 'EMPLOYEE' | 'DATE';
}

// ==================== 响应数据 ====================

/**
 * 加班记录VO
 */
export interface OvertimeRecordVO {
  id: number;
  overtimeNo: string;
  employeeId: number;
  employeeName: string;
  departmentId: number;
  departmentName: string;
  overtimeDate: string;
  startTime: string;
  endTime: string;
  overtimeHours: number;
  reason: string;
  status: OvertimeStatus;
  approvalComment?: string;
  approvalTime?: string;
  createTime: string;
  remark?: string;
}

/**
 * 加班详情VO
 */
export interface OvertimeDetailVO extends OvertimeRecordVO {
  workflowInstanceId?: number;
  approvalHistory?: ApprovalRecord[];
}

/**
 * 加班统计VO
 */
export interface OvertimeStatisticsVO {
  totalCount: number;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
  cancelledCount: number;
  totalOvertimeHours: number;
  averageOvertimeHours: number;
}

/**
 * 部门加班统计VO
 */
export interface DepartmentOvertimeStatisticsVO {
  departmentId: number;
  departmentName: string;
  totalCount: number;
  totalOvertimeHours: number;
  averageOvertimeHours: number;
  approvalRate: number;
}

/**
 * 员工加班汇总VO
 */
export interface EmployeeOvertimeSummaryVO {
  employeeId: number;
  employeeName: string;
  year: number;
  month?: number;
  totalCount: number;
  totalOvertimeHours: number;
  workdayOvertimeHours: number;
  weekendOvertimeHours: number;
  holidayOvertimeHours: number;
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
