/*
 * @fileoverview 考勤报表 API
 * @author IOE-DREAM Team
 * @description 考勤报表查询、统计、导出等接口
 * @date 2025-01-30
 */

import { getRequest, postRequest } from '/@/lib/axios';

/**
 * 考勤报表 API
 */
export const reportApi = {
  // ==================== 报表查询 ====================

  /**
   * 查询日报表
   */
  getDailyReport: (params: ReportQueryForm) => {
    return getRequest('/api/v1/attendance/report/daily', params);
  },

  /**
   * 查询月报表
   */
  getMonthlyReport: (params: ReportQueryForm) => {
    return getRequest('/api/v1/attendance/report/monthly', params);
  },

  /**
   * 导出报表
   */
  exportReport: (form: ReportQueryForm) => {
    return postRequest('/api/v1/attendance/report/export', form, {
      responseType: 'blob',
    });
  },

  // ==================== 统计数据 ====================

  /**
   * 查询个人统计
   */
  getPersonalStatistics: (employeeId: number, startDate?: string, endDate?: string) => {
    const params: any = { employeeId };
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    return getRequest('/api/v1/attendance/statistics/personal', params);
  },

  /**
   * 查询部门统计
   */
  getDepartmentStatistics: (departmentId: number, startDate?: string, endDate?: string) => {
    const params: any = { departmentId };
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    return getRequest('/api/v1/attendance/statistics/department', params);
  },

  /**
   * 查询公司统计
   */
  getCompanyStatistics: (startDate?: string, endDate?: string) => {
    const params: any = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    return getRequest('/api/v1/attendance/statistics/company', params);
  },

  /**
   * 导出统计数据
   */
  exportStatistics: (form: StatisticsQueryForm) => {
    return postRequest('/api/v1/attendance/statistics/export', form, {
      responseType: 'blob',
    });
  },

  /**
   * 查询图表数据
   */
  getChartData: (form: StatisticsQueryForm) => {
    return postRequest('/api/v1/attendance/statistics/chart', form);
  },
};

/**
 * 类型定义
 */

// ==================== 枚举类型 ====================

/**
 * 报表类型枚举
 */
export enum ReportType {
  DAILY = 'DAILY',       // 日报
  MONTHLY = 'MONTHLY'    // 月报
}

/**
 * 统计类型枚举
 */
export enum StatisticsType {
  PERSONAL = 'PERSONAL',       // 个人
  DEPARTMENT = 'DEPARTMENT',   // 部门
  COMPANY = 'COMPANY'          // 公司
}

// ==================== 表单对象 ====================

/**
 * 报表查询表单
 */
export interface ReportQueryForm {
  startDate: string;
  endDate: string;
  employeeId?: number;
  departmentId?: number;
  reportType?: ReportType;
}

/**
 * 统计查询表单
 */
export interface StatisticsQueryForm {
  employeeId?: number;
  departmentId?: number;
  startDate?: string;
  endDate?: string;
  statisticsType?: StatisticsType;
}

// ==================== 响应数据 ====================

/**
 * 考勤报表VO
 */
export interface AttendanceReportVO {
  employeeId: number;
  employeeName: string;
  departmentId: number;
  departmentName: string;
  reportDate: string;
  attendanceDays: number;
  absenceDays: number;
  lateCount: number;
  earlyLeaveCount: number;
  overtimeHours: number;
  totalWorkHours: number;
}

/**
 * 考勤统计VO
 */
export interface AttendanceStatisticsVO {
  statisticsType: StatisticsType;
  targetId: number;
  targetName: string;
  totalCount: number;
  attendanceCount: number;
  absenceCount: number;
  attendanceRate: number;
  avgWorkHours: number;
  lateCount: number;
  earlyLeaveCount: number;
  totalOvertimeHours: number;
  avgOvertimeHours: number;
  dailyStatistics: Array<Record<string, any>>;
  weeklyStatistics: Array<Record<string, any>>;
  monthlyStatistics: Array<Record<string, any>>;
}
