/*
 * @fileoverview 考勤规则配置 API
 * @author IOE-DREAM Team
 * @description 考勤规则管理接口（迟到、早退、缺勤等规则配置）
 * @date 2025-01-30
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

/**
 * 考勤规则管理 API
 */
export const ruleApi = {
  /**
   * 分页查询规则列表
   */
  queryPage: (params: RuleQueryForm) => {
    return postRequest('/api/v1/attendance/rule/query', params);
  },

  /**
   * 获取规则详情
   */
  getById: (ruleId: number) => {
    return getRequest(`/api/v1/attendance/rule/${ruleId}`);
  },

  /**
   * 新增规则
   */
  add: (data: RuleForm) => {
    return postRequest('/api/v1/attendance/rule/add', data);
  },

  /**
   * 更新规则
   */
  update: (ruleId: number, data: RuleForm) => {
    return putRequest(`/api/v1/attendance/rule/${ruleId}`, data);
  },

  /**
   * 删除规则
   */
  delete: (ruleId: number) => {
    return deleteRequest(`/api/v1/attendance/rule/${ruleId}`);
  },

  /**
   * 启用/禁用规则
   */
  updateEnabled: (ruleId: number, enabled: boolean) => {
    return putRequest(`/api/v1/attendance/rule/${ruleId}/status`, { enabled });
  }
};

/**
 * 类型定义
 */

/** 规则分类枚举 */
export enum RuleCategory {
  TIME = 'TIME',           // 时间规则
  LOCATION = 'LOCATION',   // 地点规则
  ABSENCE = 'ABSENCE',     // 缺勤规则
  OVERTIME = 'OVERTIME'     // 加班规则
}

/** 规则分类显示名称 */
export const RuleCategoryNames: Record<RuleCategory, string> = {
  [RuleCategory.TIME]: '时间规则',
  [RuleCategory.LOCATION]: '地点规则',
  [RuleCategory.ABSENCE]: '缺勤规则',
  [RuleCategory.OVERTIME]: '加班规则'
};

/** 规则查询表单 */
export interface RuleQueryForm {
  ruleName?: string;
  ruleCategory?: RuleCategory;
  pageNum: number;
  pageSize: number;
}

/** 规则表单 */
export interface RuleForm {
  ruleId?: number;
  ruleCode: string;
  ruleName: string;
  ruleCategory: RuleCategory;
  ruleType: string;
  ruleCondition: string;  // JSON字符串
  ruleAction: string;      // JSON字符串
  rulePriority: number;
  effectiveStartTime?: string;
  effectiveEndTime?: string;
  effectiveDays?: string;  // "1,2,3,4,5"
  departmentIds?: string;  // JSON数组
  userIds?: string;        // JSON数组
  ruleScope: 'GLOBAL' | 'DEPARTMENT' | 'USER';
  description?: string;
  sortOrder?: number;
}

/** 规则VO */
export interface RuleVO {
  ruleId: number;
  ruleCode: string;
  ruleName: string;
  ruleCategory: RuleCategory;
  ruleCategoryName: string;
  ruleType: string;
  ruleCondition: string;
  ruleAction: string;
  rulePriority: number;
  effectiveStartTime: string;
  effectiveEndTime: string;
  effectiveDays: string;
  departmentIds: string;
  userIds: string;
  ruleScope: string;
  ruleStatus: number;
  description: string;
  sortOrder: number;
  createTime: string;
  updateTime: string;
}
