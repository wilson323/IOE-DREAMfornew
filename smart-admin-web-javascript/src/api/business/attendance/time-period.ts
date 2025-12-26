/*
 * @fileoverview 打卡时间段管理 API
 * @author IOE-DREAM Team
 * @description 打卡时间段配置管理接口
 * @date 2025-01-30
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

/**
 * 打卡时间段管理 API
 */
export const timePeriodApi = {
  /**
   * 分页查询时间段列表
   * @param params 查询参数
   * @returns 分页结果
   */
  queryPage: (params: TimePeriodQueryForm) => {
    return postRequest('/api/v1/attendance/time-period/query', params);
  },

  /**
   * 获取时间段详情
   * @param periodId 时间段ID
   * @returns 时间段详情
   */
  getById: (periodId: number) => {
    return getRequest(`/api/v1/attendance/time-period/${periodId}`);
  },

  /**
   * 新增时间段
   * @param data 时间段数据
   * @returns 新增的时间段ID
   */
  add: (data: TimePeriodForm) => {
    return postRequest('/api/v1/attendance/time-period/add', data);
  },

  /**
   * 更新时间段
   * @param periodId 时间段ID
   * @param data 时间段数据
   * @returns 更新结果
   */
  update: (periodId: number, data: TimePeriodForm) => {
    return putRequest(`/api/v1/attendance/time-period/${periodId}`, data);
  },

  /**
   * 删除时间段
   * @param periodId 时间段ID
   * @returns 删除结果
   */
  delete: (periodId: number) => {
    return deleteRequest(`/api/v1/attendance/time-period/${periodId}`);
  },

  /**
   * 批量删除时间段
   * @param periodIds 时间段ID列表
   * @returns 删除结果
   */
  batchDelete: (periodIds: number[]) => {
    return postRequest('/api/v1/attendance/time-period/batch-delete', { periodIds });
  },

  /**
   * 获取所有时间段
   * @returns 时间段列表
   */
  getAll: () => {
    return getRequest('/api/v1/attendance/time-period/all');
  },

  /**
   * 启用/禁用时间段
   * @param periodId 时间段ID
   * @param enabled 是否启用
   * @returns 更新结果
   */
  updateEnabled: (periodId: number, enabled: boolean) => {
    return putRequest(`/api/v1/attendance/time-period/${periodId}/enabled`, { enabled });
  }
};

/**
 * 类型定义
 */

/** 时间段类型枚举 */
export enum TimePeriodType {
  MORNING_CHECK_IN = 1,    // 上午上班打卡
  MORNING_CHECK_OUT = 2,   // 上午下班打卡
  AFTERNOON_CHECK_IN = 3,  // 下午上班打卡
  AFTERNOON_CHECK_OUT = 4,  // 下午下班打卡
  OVERTIME_CHECK_IN = 5,   // 加班打卡
  OVERTIME_CHECK_OUT = 6,   // 加班结束打卡
  NIGHT_SHIFT_CHECK_IN = 7, // 夜班上班打卡
  NIGHT_SHIFT_CHECK_OUT = 8 // 夜班下班打卡
}

/** 时间段类型显示名称映射 */
export const TimePeriodTypeNames: Record<TimePeriodType, string> = {
  [TimePeriodType.MORNING_CHECK_IN]: '上午上班打卡',
  [TimePeriodType.MORNING_CHECK_OUT]: '上午下班打卡',
  [TimePeriodType.AFTERNOON_CHECK_IN]: '下午上班打卡',
  [TimePeriodType.AFTERNOON_CHECK_OUT]: '下午下班打卡',
  [TimePeriodType.OVERTIME_CHECK_IN]: '加班打卡',
  [TimePeriodType.OVERTIME_CHECK_OUT]: '加班结束打卡',
  [TimePeriodType.NIGHT_SHIFT_CHECK_IN]: '夜班上班打卡',
  [TimePeriodType.NIGHT_SHIFT_CHECK_OUT]: '夜班下班打卡'
};

/** 时间段查询表单 */
export interface TimePeriodQueryForm {
  /** 时间段名称（模糊查询） */
  periodName?: string;
  /** 时间段类型 */
  periodType?: TimePeriodType;
  /** 是否启用 */
  enabled?: boolean;
  /** 页码 */
  pageNum: number;
  /** 每页大小 */
  pageSize: number;
}

/** 时间段表单 */
export interface TimePeriodForm {
  /** 时间段ID（更新时必填） */
  periodId?: number;
  /** 时间段名称 */
  periodName: string;
  /** 时间段类型 */
  periodType: TimePeriodType;
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
  /** 允许提前分钟数 */
  allowEarlyMinutes?: number;
  /** 允许延后分钟数 */
  allowLateMinutes?: number;
  /** 是否启用弹性 */
  isFlexible?: boolean;
  /** 最小工作时长（分钟） */
  minWorkMinutes?: number;
  /** 关联班次ID（可选） */
  shiftId?: number;
  /** 备注 */
  remarks?: string;
  /** 排序号 */
  sortOrder?: number;
}

/** 时间段VO */
export interface TimePeriodVO {
  /** 时间段ID */
  periodId: number;
  /** 时间段名称 */
  periodName: string;
  /** 时间段类型 */
  periodType: TimePeriodType;
  /** 时间段类型名称 */
  periodTypeName: string;
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
  /** 允许提前分钟数 */
  allowEarlyMinutes: number;
  /** 允许延后分钟数 */
  allowLateMinutes: number;
  /** 是否启用弹性 */
  isFlexible: boolean;
  /** 最小工作时长（分钟） */
  minWorkMinutes: number;
  /** 关联班次ID */
  shiftId: number;
  /** 关联班次名称 */
  shiftName: string;
  /** 备注 */
  remarks: string;
  /** 排序号 */
  sortOrder: number;
  /** 是否启用 */
  enabled: boolean;
  /** 创建时间 */
  createTime: string;
  /** 更新时间 */
  updateTime: string;
}

/** 时间段列表项（用于下拉选择） */
export interface TimePeriodOption {
  /** 时间段ID */
  periodId: number;
  /** 时间段名称 */
  periodName: string;
  /** 时间段类型 */
  periodType: TimePeriodType;
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
}
