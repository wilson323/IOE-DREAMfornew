/*
 * @fileoverview 考勤结果查询 API
 * @author IOE-DREAM Team
 * @description 考勤记录查询、统计、导出等接口
 * @date 2025-01-30
 */

import { getRequest, postRequest } from '/@/lib/axios';

/**
 * 考勤结果查询 API
 */
export const resultApi = {
  // ==================== 考勤记录查询 ====================

  /**
   * 分页查询考勤记录
   */
  queryRecords: (params: ResultQueryForm) => {
    return getRequest('/api/v1/attendance/records/query', params);
  },

  /**
   * 获取考勤记录详情
   */
  getRecordDetail: (recordId: number) => {
    return getRequest(`/api/v1/attendance/record/${recordId}`);
  },

  // ==================== 考勤统计 ====================

  /**
   * 获取考勤统计数据
   */
  getStatistics: (params: StatisticsQueryForm) => {
    return getRequest('/api/v1/attendance/statistics', params);
  },

  /**
   * 获取个人考勤统计
   */
  getPersonalStatistics: (userId: number, startDate: string, endDate: string) => {
    return getRequest('/api/v1/attendance/statistics/personal', {
      userId,
      startDate,
      endDate
    });
  },

  /**
   * 获取部门考勤统计
   */
  getDepartmentStatistics: (departmentId: number, startDate: string, endDate: string) => {
    return getRequest('/api/v1/attendance/statistics/department', {
      departmentId,
      startDate,
      endDate
    });
  },

  /**
   * 获取考勤趋势数据
   */
  getTrendData: (params: TrendQueryForm) => {
    return getRequest('/api/v1/attendance/statistics/trend', params);
  },

  // ==================== 数据导出 ====================

  /**
   * 导出考勤记录
   */
  exportRecords: (params: ResultQueryForm) => {
    return postRequest('/api/v1/attendance/records/export', params, {
      responseType: 'blob'
    });
  },

  /**
   * 导出考勤统计报表
   */
  exportStatistics: (params: StatisticsQueryForm) => {
    return postRequest('/api/v1/attendance/statistics/export', params, {
      responseType: 'blob'
    });
  },

  /**
   * 导出个人考勤报表
   */
  exportPersonalReport: (userId: number, startDate: string, endDate: string) => {
    return postRequest('/api/v1/attendance/statistics/export/personal', {
      userId,
      startDate,
      endDate
    }, {
      responseType: 'blob'
    });
  }
};

/**
 * 类型定义
 */

// ==================== 枚举类型 ====================

/** 考勤状态枚举 */
export enum AttendanceStatus {
  NORMAL = 'NORMAL',           // 正常
  LATE = 'LATE',               // 迟到
  EARLY_LEAVE = 'EARLY_LEAVE', // 早退
  ABSENT = 'ABSENT',           // 缺勤
  OVERTIME = 'OVERTIME',       // 加班
  LEAVE = 'LEAVE'              // 请假
}

/** 考勤类型枚举 */
export enum AttendanceType {
  CHECK_IN = 'CHECK_IN',       // 上班打卡
  CHECK_OUT = 'CHECK_OUT'      // 下班打卡
}

// ==================== 查询表单 ====================

/** 考勤结果查询表单 */
export interface ResultQueryForm {
  userId?: number;
  userName?: string;
  departmentId?: number;
  departmentName?: string;
  startDate: string;
  endDate: string;
  attendanceStatus?: AttendanceStatus;
  attendanceType?: AttendanceType;
  shiftId?: number;
  pageNum?: number;
  pageSize?: number;
}

/** 统计查询表单 */
export interface StatisticsQueryForm {
  startDate: string;
  endDate: string;
  departmentId?: number;
  departmentIds?: number[];
  userId?: number;
  userIds?: number[];
  groupBy?: 'DEPARTMENT' | 'USER' | 'DATE';
}

/** 趋势查询表单 */
export interface TrendQueryForm {
  startDate: string;
  endDate: string;
  dimension: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  departmentId?: number;
  userId?: number;
}

// ==================== 响应数据 ====================

/** 考勤记录VO */
export interface AttendanceRecordVO {
  recordId: number;
  userId: number;
  userName: string;
  departmentId: number;
  departmentName: string;
  shiftId: number;
  shiftName: string;
  attendanceDate: string;
  punchTime: string;
  attendanceStatus: string;
  attendanceType: string;
  punchAddress?: string;
  deviceName?: string;
}

/** 考勤结果VO */
export interface AttendanceResultVO {
  userId: number;
  date: string;
  status: string;
  lateDuration?: number;
  earlyDuration?: number;
  overtimeDuration?: number;
  workingMinutes?: number;
  remark?: string;
}

/** 考勤统计VO */
export interface AttendanceStatisticsVO {
  totalCount: number;
  normalCount: number;
  lateCount: number;
  earlyCount: number;
  absentCount: number;
  overtimeCount: number;
  normalRate: number;
  lateRate: number;
  earlyRate: number;
  absentRate: number;
}

/** 个人考勤统计 */
export interface PersonalStatisticsVO {
  userId: number;
  userName: string;
  departmentName: string;
  totalCount: number;
  normalCount: number;
  lateCount: number;
  earlyCount: number;
  absentCount: number;
  overtimeHours: number;
  workingHours: number;
  normalRate: number;
}

/** 部门考勤统计 */
export interface DepartmentStatisticsVO {
  departmentId: number;
  departmentName: string;
  employeeCount: number;
  totalCount: number;
  normalCount: number;
  lateCount: number;
  earlyCount: number;
  absentCount: number;
  normalRate: number;
}

/** 考勤趋势数据 */
export interface AttendanceTrendVO {
  date: string;
  totalCount: number;
  normalCount: number;
  lateCount: number;
  earlyCount: number;
  absentCount: number;
  normalRate: number;
}

/** 部门考勤汇总 */
export interface DepartmentSummaryVO {
  departmentId: number;
  departmentName: string;
  totalEmployees: number;
  presentEmployees: number;
  absentEmployees: number;
  lateEmployees: number;
  earlyLeaveEmployees: number;
  attendanceRate: number;
}

/** 个人考勤汇总 */
export interface PersonalSummaryVO {
  userId: number;
  userName: string;
  departmentName: string;
  totalDays: number;
  presentDays: number;
  lateDays: number;
  earlyDays: number;
  absentDays: number;
  overtimeHours: number;
  workingHours: number;
  attendanceRate: number;
}
