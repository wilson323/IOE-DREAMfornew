/*
 * @fileoverview 补签管理 API
 * @author IOE-DREAM Team
 * @description 补签申请、审批、查询等接口
 * @date 2025-01-30
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

/**
 * 补签管理 API
 */
export const supplementApi = {
  // ==================== 补签申请管理 ====================

  /**
   * 分页查询补签申请列表
   */
  querySupplementList: (params: SupplementQueryForm) => {
    return getRequest('/api/v1/attendance/supplement/list', params);
  },

  /**
   * 获取补签申请详情
   */
  getSupplementDetail: (supplementId: number) => {
    return getRequest(`/api/v1/attendance/supplement/${supplementId}`);
  },

  /**
   * 提交补签申请
   */
  submitSupplement: (form: SupplementAddForm) => {
    return postRequest('/api/v1/attendance/supplement/submit', form);
  },

  /**
   * 更新补签申请
   */
  updateSupplement: (supplementId: number, form: SupplementUpdateForm) => {
    return putRequest(`/api/v1/attendance/supplement/${supplementId}`, form);
  },

  /**
   * 删除补签申请
   */
  deleteSupplement: (supplementId: number) => {
    return deleteRequest(`/api/v1/attendance/supplement/${supplementId}`);
  },

  /**
   * 取消补签申请
   */
  cancelSupplement: (supplementNo: string) => {
    return postRequest(`/api/v1/attendance/supplement/${supplementNo}/cancel`);
  },

  // ==================== 补签审批 ====================

  /**
   * 审批补签申请
   */
  approveSupplement: (supplementNo: string, form: SupplementApprovalForm) => {
    return putRequest(`/api/v1/attendance/supplement/${supplementNo}/status`, {
      status: 'APPROVED',
      approvalComment: form.approvalComment,
    });
  },

  /**
   * 驳回补签申请
   */
  rejectSupplement: (supplementNo: string, form: SupplementRejectionForm) => {
    return putRequest(`/api/v1/attendance/supplement/${supplementNo}/status`, {
      status: 'REJECTED',
      approvalComment: form.rejectionReason,
    });
  },

  /**
   * 获取待审批补签列表
   */
  getPendingApprovalList: (params: SupplementQueryForm) => {
    return getRequest('/api/v1/attendance/supplement/pending', params);
  },

  // ==================== 补签统计 ====================

  /**
   * 获取补签统计数据
   */
  getSupplementStatistics: (params: SupplementStatisticsForm) => {
    return getRequest('/api/v1/attendance/supplement/statistics', params);
  },

  // ==================== 数据导出 ====================

  /**
   * 导出补签记录
   */
  exportSupplementRecords: (params: SupplementQueryForm) => {
    return postRequest('/api/v1/attendance/supplement/export', params, {
      responseType: 'blob',
    });
  },
};

/**
 * 类型定义
 */

// ==================== 枚举类型 ====================

/**
 * 补签状态枚举
 */
export enum SupplementStatus {
  PENDING = 'PENDING',         // 待审批
  APPROVED = 'APPROVED',       // 已通过
  REJECTED = 'REJECTED',       // 已驳回
  CANCELLED = 'CANCELLED'      // 已取消
}

/**
 * 打卡类型枚举
 */
export enum PunchType {
  CHECK_IN = 'CHECK_IN',       // 上班打卡
  CHECK_OUT = 'CHECK_OUT'      // 下班打卡
}

// ==================== 表单对象 ====================

/**
 * 补签查询表单
 */
export interface SupplementQueryForm {
  employeeId?: number;
  employeeName?: string;
  departmentId?: number;
  status?: SupplementStatus;
  punchType?: PunchType;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 补签申请表单
 */
export interface SupplementAddForm {
  employeeId: number;
  supplementDate: string;
  punchTime: string;
  punchType: PunchType;
  reason: string;
  remark?: string;
}

/**
 * 补签更新表单
 */
export interface SupplementUpdateForm {
  supplementDate?: string;
  punchTime?: string;
  punchType?: PunchType;
  reason?: string;
  remark?: string;
}

/**
 * 补签审批表单
 */
export interface SupplementApprovalForm {
  approvalComment: string;
}

/**
 * 补签驳回表单
 */
export interface SupplementRejectionForm {
  rejectionReason: string;
}

/**
 * 补签统计表单
 */
export interface SupplementStatisticsForm {
  startDate: string;
  endDate: string;
  departmentId?: number;
  groupBy?: 'DEPARTMENT' | 'EMPLOYEE' | 'DATE';
}

// ==================== 响应数据 ====================

/**
 * 补签记录VO
 */
export interface SupplementRecordVO {
  id: number;
  supplementNo: string;
  employeeId: number;
  employeeName: string;
  departmentId: number;
  departmentName: string;
  supplementDate: string;
  punchTime: string;
  punchType: PunchType;
  reason: string;
  status: SupplementStatus;
  approvalComment?: string;
  approvalTime?: string;
  createTime: string;
  remark?: string;
}

/**
 * 补签详情VO
 */
export interface SupplementDetailVO extends SupplementRecordVO {
  workflowInstanceId?: number;
  approvalHistory?: ApprovalRecord[];
}

/**
 * 补签统计VO
 */
export interface SupplementStatisticsVO {
  totalCount: number;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
  cancelledCount: number;
  checkInCount: number;
  checkOutCount: number;
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
