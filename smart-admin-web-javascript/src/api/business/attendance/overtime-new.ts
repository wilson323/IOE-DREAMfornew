/*
 * @fileoverview 加班管理 API（更新版）
 * @author IOE-DREAM Team
 * @description 加班申请、审批、查询等接口（匹配Task 2.6后端Controller）
 * @date 2025-01-30
 */

import { getRequest, postRequest, deleteRequest } from '/@/lib/axios';

/**
 * 加班管理 API（匹配后端Controller）
 */
export const overtimeApi = {
  // ==================== 加班申请管理 ====================

  /**
   * 分页查询加班申请列表
   * POST /api/attendance/overtime/apply/page
   */
  queryOvertimeList: (params: OvertimeQueryForm) => {
    return postRequest('/api/attendance/overtime/apply/page', params);
  },

  /**
   * 获取加班申请详情
   * GET /api/attendance/overtime/apply/{applyId}
   */
  getOvertimeDetail: (applyId: number) => {
    return getRequest(`/api/attendance/overtime/apply/${applyId}`);
  },

  /**
   * 新增加班申请
   * POST /api/attendance/overtime/apply/add
   */
  submitOvertime: (form: OvertimeAddForm) => {
    return postRequest('/api/attendance/overtime/apply/add', form);
  },

  /**
   * 更新加班申请
   * POST /api/attendance/overtime/apply/update/{applyId}
   */
  updateOvertime: (applyId: number, form: OvertimeUpdateForm) => {
    return postRequest(`/api/attendance/overtime/apply/update/${applyId}`, form);
  },

  /**
   * 删除加班申请
   * POST /api/attendance/overtime/apply/delete/{applyId}
   */
  deleteOvertime: (applyId: number) => {
    return postRequest(`/api/attendance/overtime/apply/delete/${applyId}`);
  },

  /**
   * 批量删除加班申请
   * POST /api/attendance/overtime/apply/batchDelete
   */
  batchDeleteOvertime: (applyIds: number[]) => {
    return postRequest('/api/attendance/overtime/apply/batchDelete', applyIds);
  },

  /**
   * 提交加班申请
   * POST /api/attendance/overtime/apply/submit/{applyId}
   */
  submitForApproval: (applyId: number) => {
    return postRequest(`/api/attendance/overtime/apply/submit/${applyId}`);
  },

  /**
   * 取消加班申请
   * POST /api/attendance/overtime/apply/cancel/{applyId}
   */
  cancelOvertime: (applyId: number, cancelReason: string) => {
    return postRequest(`/api/attendance/overtime/apply/cancel/${applyId}`, null, {
      params: { cancelReason }
    });
  },

  // ==================== 加班审批 ====================

  /**
   * 审批通过
   * POST /api/attendance/overtime/apply/approve
   */
  approveOvertime: (params: {
    applyId: number;
    approvalLevel: number;
    approverId: number;
    approvalComment?: string;
  }) => {
    return postRequest('/api/attendance/overtime/apply/approve', null, {
      params
    });
  },

  /**
   * 审批驳回
   * POST /api/attendance/overtime/apply/reject
   */
  rejectOvertime: (params: {
    applyId: number;
    approvalLevel: number;
    approverId: number;
    rejectReason: string;
  }) => {
    return postRequest('/api/attendance/overtime/apply/reject', null, {
      params
    });
  },

  /**
   * 查询我的加班申请
   * GET /api/attendance/overtime/apply/my/{applicantId}
   */
  getMyApplications: (applicantId: number) => {
    return getRequest(`/api/attendance/overtime/apply/my/${applicantId}`);
  },

  /**
   * 查询待我审批的申请
   * GET /api/attendance/overtime/apply/pending/{approverId}
   */
  getPendingApprovals: (approverId: number) => {
    return getRequest(`/api/attendance/overtime/apply/pending/${approverId}`);
  },

  // ==================== 加班统计 ====================

  /**
   * 统计部门加班时长
   * GET /api/attendance/overtime/apply/statistics/department
   */
  getDepartmentOvertimeHours: (params: {
    departmentId: number;
    startDate: string;
    endDate: string;
  }) => {
    return getRequest('/api/attendance/overtime/apply/statistics/department', params);
  },

  /**
   * 部门统计报表
   * GET /api/attendance/overtime/apply/statistics/department/report
   */
  getDepartmentStatistics: (params: {
    startDate: string;
    endDate: string;
  }) => {
    return getRequest('/api/attendance/overtime/apply/statistics/department/report', params);
  },

  /**
   * 员工统计报表
   * GET /api/attendance/overtime/apply/statistics/employee/report
   */
  getEmployeeStatistics: (params: {
    departmentId?: number;
    startDate: string;
    endDate: string;
  }) => {
    return getRequest('/api/attendance/overtime/apply/statistics/employee/report', params);
  },

  /**
   * 类型统计报表
   * GET /api/attendance/overtime/apply/statistics/type/report
   */
  getTypeStatistics: (params: {
    startDate: string;
    endDate: string;
  }) => {
    return getRequest('/api/attendance/overtime/apply/statistics/type/report', params);
  },

  /**
   * 获取加班统计数据（通用统计）
   * 旧的接口，保持兼容性
   */
  getOvertimeStatistics: (params: OvertimeStatisticsForm) => {
    return getRequest('/api/attendance/overtime/apply/statistics/summary', params);
  },

  // ==================== 数据导出 ====================

  /**
   * 导出加班申请数据
   * POST /api/attendance/overtime/apply/export
   */
  exportOvertimeRecords: (params: OvertimeQueryForm) => {
    return postRequest('/api/attendance/overtime/apply/export', params, {
      responseType: 'blob',
    });
  },

  /**
   * 导出加班统计报表（旧接口，保持兼容性）
   */
  exportOvertimeStatistics: (params: OvertimeStatisticsForm) => {
    return postRequest('/api/attendance/overtime/apply/statistics/export', params, {
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
  DRAFT = 'DRAFT',           // 草稿
  PENDING = 'PENDING',       // 待审批
  APPROVED = 'APPROVED',     // 已批准
  REJECTED = 'REJECTED',     // 已驳回
  CANCELLED = 'CANCELLED'    // 已撤销
}

/**
 * 加班类型枚举
 */
export enum OvertimeType {
  WORKDAY = 'WORKDAY',       // 工作日加班
  OVERTIME = 'OVERTIME',     // 休息日加班
  HOLIDAY = 'HOLIDAY'        // 法定节假日加班
}

/**
 * 补偿方式枚举
 */
export enum CompensationType {
  PAY = 'PAY',               // 支付加班费
  LEAVE = 'LEAVE'            // 调休
}

// ==================== 表单对象 ====================

/**
 * 加班查询表单
 */
export interface OvertimeQueryForm {
  applicantId?: number;
  departmentId?: number;
  overtimeType?: OvertimeType;
  applyStatus?: OvertimeStatus;
  startDate?: string;
  endDate?: string;
  keyword?: string;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 加班申请新增表单
 */
export interface OvertimeAddForm {
  applicantId: number;
  departmentId: number;
  positionId?: number;
  overtimeType: OvertimeType;
  overtimeDate: string;
  startTime: string;
  endTime: string;
  plannedHours: number;
  overtimeReason: string;
  overtimeDescription?: string;
  compensationType?: CompensationType;
  leaveDate?: string;
}

/**
 * 加班申请更新表单
 */
export interface OvertimeUpdateForm {
  overtimeType?: OvertimeType;
  overtimeDate?: string;
  startTime?: string;
  endTime?: string;
  plannedHours?: number;
  overtimeReason?: string;
  overtimeDescription?: string;
  compensationType?: CompensationType;
  leaveDate?: string;
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
  applyId: number;
  applyNo: string;
  applicantId: number;
  applicantName: string;
  departmentId: number;
  departmentName: string;
  positionId?: number;
  positionName?: string;
  overtimeType: OvertimeType;
  overtimeDate: string;
  startTime: string;
  endTime: string;
  plannedHours: number;
  actualHours?: number;
  overtimeReason: string;
  overtimeDescription?: string;
  compensationType?: CompensationType;
  leaveDate?: string;
  applyStatus: OvertimeStatus;
  approvalLevel?: number;
  approverId?: number;
  approverName?: string;
  finalApproverId?: number;
  finalApproverName?: string;
  finalApprovalTime?: string;
  finalApprovalComment?: string;
  createTime: string;
  updateTime: string;
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
  applyCount: number;
  totalHours: number;
}

/**
 * 员工加班统计VO
 */
export interface EmployeeOvertimeStatisticsVO {
  employeeId: number;
  employeeName: string;
  departmentName: string;
  applyCount: number;
  totalHours: number;
}

/**
 * 加班类型统计VO
 */
export interface OvertimeTypeStatisticsVO {
  overtimeType: string;
  applyCount: number;
  totalHours: number;
}
