/*
 * @fileoverview 考勤仪表中心 API
 * @author IOE-DREAM Team
 * @description 仪表中心数据接口
 * @date 2025-01-30
 */

import { getRequest, postRequest } from '/@/lib/axios';

/**
 * 仪表中心 API
 */
export const dashboardApi = {
  /**
   * 获取实时统计数据
   * @param params 查询参数
   * @returns 统计数据
   */
  getRealtimeStats: (params?: any) => {
    return getRequest('/api/v1/attendance/dashboard/realtime-stats', params);
  },

  /**
   * 获取今日打卡趋势
   * @returns 趋势数据
   */
  getTodayTrend: () => {
    return getRequest('/api/v1/attendance/dashboard/today-trend');
  },

  /**
   * 获取部门出勤率
   * @param params 查询参数
   * @returns 部门出勤率数据
   */
  getDepartmentRate: (params?: any) => {
    return getRequest('/api/v1/attendance/dashboard/department-rate', params);
  },

  /**
   * 获取异常告警列表
   * @param params 查询参数
   * @returns 告警列表
   */
  getAlertList: (params?: any) => {
    return getRequest('/api/v1/attendance/dashboard/alert-list', params);
  },

  /**
   * 获取实时打卡记录
   * @param params 查询参数
   * @returns 打卡记录列表
   */
  getRealtimeCheckins: (params?: any) => {
    return getRequest('/api/v1/attendance/dashboard/realtime-checkins', params);
  },

  /**
   * 获取周趋势数据
   * @returns 周趋势数据
   */
  getWeeklyTrend: () => {
    return getRequest('/api/v1/attendance/dashboard/weekly-trend');
  },

  /**
   * 获取部门排名
   * @returns 部门排名数据
   */
  getDepartmentRanking: () => {
    return getRequest('/api/v1/attendance/dashboard/department-ranking');
  },

  /**
   * 获取请假汇总
   * @param params 查询参数
   * @returns 请假汇总数据
   */
  getLeaveSummary: (params?: any) => {
    return getRequest('/api/v1/attendance/dashboard/leave-summary', params);
  }
};

/**
 * 类型定义
 */

/** 实时统计数据 */
export interface RealtimeStatsVO {
  /** 应出勤人数 */
  totalAttendance: number;
  /** 实际出勤人数 */
  actualAttendance: number;
  /** 迟到人数 */
  lateCount: number;
  /** 早退人数 */
  earlyLeaveCount: number;
  /** 缺勤人数 */
  absentCount: number;
  /** 出勤率 */
  attendanceRate: number;
}

/** 打卡趋势数据点 */
export interface TrendDataPoint {
  /** 时间点 */
  time: string;
  /** 打卡人数 */
  count: number;
  /** 签到人数 */
  checkInCount: number;
  /** 签退人数 */
  checkOutCount: number;
}

/** 部门出勤率数据 */
export interface DepartmentRateVO {
  /** 部门ID */
  departmentId: number;
  /** 部门名称 */
  departmentName: string;
  /** 出勤率 */
  attendanceRate: number;
  /** 应出勤人数 */
  totalEmployees: number;
  /** 实际出勤人数 */
  actualEmployees: number;
}

/** 告警级别 */
export type AlertLevel = 1 | 2 | 3;

/** 异常告警数据 */
export interface AlertVO {
  /** 告警ID */
  alertId: number;
  /** 员工ID */
  employeeId: number;
  /** 员工姓名 */
  employeeName: string;
  /** 告警级别 */
  level: AlertLevel;
  /** 告警标题 */
  title: string;
  /** 告警描述 */
  description: string;
  /** 告警时间 */
  alertTime: string;
}

/** 实时打卡记录 */
export interface RealtimeCheckinVO {
  /** 记录ID */
  recordId: number;
  /** 员工ID */
  employeeId: number;
  /** 员工姓名 */
  employeeName: string;
  /** 头像URL */
  avatar?: string;
  /** 打卡时间 */
  punchTime: string;
  /** 打卡类型 */
  checkType: 'IN' | 'OUT';
  /** 打卡状态 */
  status: 'SUCCESS' | 'FAILED';
  /** 打卡位置 */
  location: string;
}

/** 部门排名数据 */
export interface DepartmentRankingVO {
  /** 部门ID */
  departmentId: number;
  /** 部门名称 */
  departmentName: string;
  /** 出勤率 */
  rate: number;
  /** 排名 */
  rank: number;
}

/** 请假汇总数据 */
export interface LeaveSummaryVO {
  /** 记录ID */
  leaveId: number;
  /** 员工姓名 */
  employeeName: string;
  /** 请假类型 */
  leaveType: string;
  /** 请假时长 */
  duration: string;
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
}
