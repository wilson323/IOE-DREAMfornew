/*
 * @fileoverview 班次管理 API
 * @author IOE-DREAM Team
 * @description 班次基础信息管理接口
 * @date 2025-01-30
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

/**
 * 班次管理 API
 */
export const shiftApi = {
  /**
   * 分页查询班次列表
   * @param params 查询参数
   * @returns 分页结果
   */
  queryPage: (params: ShiftQueryForm) => {
    return postRequest('/api/v1/attendance/shift/query', params);
  },

  /**
   * 获取班次详情
   * @param shiftId 班次ID
   * @returns 班次详情
   */
  getById: (shiftId: number) => {
    return getRequest(`/api/v1/attendance/shift/${shiftId}`);
  },

  /**
   * 新增班次
   * @param data 班次数据
   * @returns 新增的班次ID
   */
  add: (data: ShiftForm) => {
    return postRequest('/api/v1/attendance/shift/add', data);
  },

  /**
   * 更新班次
   * @param shiftId 班次ID
   * @param data 班次数据
   * @returns 更新结果
   */
  update: (shiftId: number, data: ShiftForm) => {
    return putRequest(`/api/v1/attendance/shift/${shiftId}`, data);
  },

  /**
   * 删除班次
   * @param shiftId 班次ID
   * @returns 删除结果
   */
  delete: (shiftId: number) => {
    return deleteRequest(`/api/v1/attendance/shift/${shiftId}`);
  },

  /**
   * 批量删除班次
   * @param shiftIds 班次ID列表
   * @returns 删除结果
   */
  batchDelete: (shiftIds: number[]) => {
    return postRequest('/api/v1/attendance/shift/batch-delete', { shiftIds });
  },

  /**
   * 获取所有班次（用于下拉选择）
   * @returns 班次列表
   */
  getAll: () => {
    return getRequest('/api/v1/attendance/shift/all');
  },

  /**
   * 启用/禁用班次
   * @param shiftId 班次ID
   * @param enabled 是否启用
   * @returns 更新结果
   */
  updateEnabled: (shiftId: number, enabled: boolean) => {
    return putRequest(`/api/v1/attendance/shift/${shiftId}/enabled`, { enabled });
  }
};

/**
 * 类型定义
 */

/** 班次类型枚举 */
export enum ShiftType {
  DAY_SHIFT = 1,        // 白班
  NIGHT_SHIFT = 2,      // 夜班
  ROTATING_SHIFT = 3,   // 轮班
  FLEXIBLE_SHIFT = 4,   // 弹性班
  PART_TIME_SHIFT = 5,  // 兼职班
  SPECIAL_SHIFT = 6     // 特殊班
}

/** 班次类型显示名称映射 */
export const ShiftTypeNames: Record<ShiftType, string> = {
  [ShiftType.DAY_SHIFT]: '白班',
  [ShiftType.NIGHT_SHIFT]: '夜班',
  [ShiftType.ROTATING_SHIFT]: '轮班',
  [ShiftType.FLEXIBLE_SHIFT]: '弹性班',
  [ShiftType.PART_TIME_SHIFT]: '兼职班',
  [ShiftType.SPECIAL_SHIFT]: '特殊班'
};

/** 班次查询表单 */
export interface ShiftQueryForm {
  /** 班次名称（模糊查询） */
  shiftName?: string;
  /** 班次类型 */
  shiftType?: ShiftType;
  /** 是否启用 */
  enabled?: boolean;
  /** 页码 */
  pageNum: number;
  /** 每页大小 */
  pageSize: number;
}

/** 班次表单 */
export interface ShiftForm {
  /** 班次ID（更新时必填） */
  shiftId?: number;
  /** 班次名称 */
  shiftName: string;
  /** 班次类型 */
  shiftType: ShiftType;
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
  /** 工作时长(小时) */
  workHours?: number;
  /** 休息时长(分钟) */
  breakMinutes?: number;
  /** 休息开始时间 */
  breakStartTime?: string;
  /** 休息结束时间 */
  breakEndTime?: string;
  /** 是否跨天 */
  isOvernight?: boolean;
  /** 是否弹性时间 */
  isFlexible?: boolean;
  /** 弹性开始时间(分钟) */
  flexibleStartTime?: number;
  /** 弹性结束时间(分钟) */
  flexibleEndTime?: number;
  /** 加班计算开始时间 */
  overtimeStartTime?: string;
  /** 最小加班时长(分钟) */
  minOvertimeMinutes?: number;
  /** 颜色标识 */
  colorCode?: string;
  /** 备注 */
  remarks?: string;
  /** 排序号 */
  sortOrder?: number;
}

/** 班次VO */
export interface ShiftVO {
  /** 班次ID */
  shiftId: number;
  /** 班次名称 */
  shiftName: string;
  /** 班次类型 */
  shiftType: ShiftType;
  /** 班次类型名称 */
  shiftTypeName: string;
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
  /** 工作时长(小时) */
  workHours: number;
  /** 休息时长(分钟) */
  breakMinutes: number;
  /** 休息开始时间 */
  breakStartTime: string;
  /** 休息结束时间 */
  breakEndTime: string;
  /** 是否跨天 */
  isOvernight: boolean;
  /** 是否弹性时间 */
  isFlexible: boolean;
  /** 弹性开始时间(分钟) */
  flexibleStartTime: number;
  /** 弹性结束时间(分钟) */
  flexibleEndTime: number;
  /** 加班计算开始时间 */
  overtimeStartTime: string;
  /** 最小加班时长(分钟) */
  minOvertimeMinutes: number;
  /** 颜色标识 */
  colorCode: string;
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

/** 班次列表项（用于下拉选择） */
export interface ShiftOption {
  /** 班次ID */
  shiftId: number;
  /** 班次名称 */
  shiftName: string;
  /** 班次类型 */
  shiftType: ShiftType;
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
  /** 颜色标识 */
  colorCode: string;
}
