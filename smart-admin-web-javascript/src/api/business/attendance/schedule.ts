/*
 * @fileoverview 排班管理 API
 * @author IOE-DREAM Team
 * @description 智能排班、日历视图、冲突检测、模板管理等接口
 * @date 2025-01-30
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

/**
 * 排班管理 API
 */
export const scheduleApi = {
  // ==================== 智能排班 ====================

  /**
   * 执行智能排班
   */
  executeIntelligentSchedule: (request: ScheduleRequest) => {
    return postRequest('/api/v1/attendance/schedule/intelligent', request);
  },

  /**
   * 快速生成排班
   */
  quickGenerate: (request: QuickGenerateRequest) => {
    return postRequest('/api/v1/attendance/smart-scheduling/generate', request);
  },

  // ==================== 排班计划 ====================

  /**
   * 生成排班计划
   */
  generateSchedulePlan: (planId: number, startDate: string, endDate: string) => {
    return postRequest('/api/v1/attendance/schedule/plan', null, {
      planId,
      startDate,
      endDate
    });
  },

  // ==================== 冲突检测与解决 ====================

  /**
   * 验证排班冲突
   */
  validateConflicts: (scheduleData: ScheduleData) => {
    return postRequest('/api/v1/attendance/schedule/conflicts/validate', scheduleData);
  },

  /**
   * 解决排班冲突
   */
  resolveConflicts: (conflicts: ScheduleConflict[], strategy: string) => {
    return postRequest('/api/v1/attendance/schedule/conflicts/resolve', {
      conflicts,
      resolutionStrategy: strategy
    });
  },

  /**
   * 检测排班冲突（SmartScheduling）
   */
  detectConflicts: (request: ConflictDetectionRequest) => {
    return postRequest('/api/v1/attendance/smart-scheduling/conflicts/detect', request);
  },

  // ==================== 排班优化 ====================

  /**
   * 优化排班
   */
  optimizeSchedule: (request: OptimizeScheduleRequest) => {
    return postRequest('/api/v1/attendance/schedule/optimize', request);
  },

  /**
   * 预测排班效果
   */
  predictEffect: (scheduleData: ScheduleData) => {
    return postRequest('/api/v1/attendance/schedule/predict', scheduleData);
  },

  // ==================== 排班统计 ====================

  /**
   * 获取排班统计
   */
  getStatistics: (planId: number) => {
    return getRequest('/api/v1/attendance/schedule/statistics', { planId });
  },

  /**
   * 获取排班统计（SmartScheduling）
   */
  getSmartStatistics: (requestId: number) => {
    return getRequest('/api/v1/attendance/smart-scheduling/statistics', { requestId });
  },

  // ==================== 模板管理 ====================

  /**
   * 获取排班模板列表
   */
  getTemplates: (params?: TemplateQueryForm) => {
    return getRequest('/api/v1/attendance/smart-scheduling/templates', params);
  },

  /**
   * 获取模板详情
   */
  getTemplateById: (templateId: number) => {
    return getRequest(`/api/v1/attendance/schedule/template/${templateId}`);
  },

  /**
   * 新增排班模板
   */
  addTemplate: (data: TemplateForm) => {
    return postRequest('/api/v1/attendance/schedule/template', data);
  },

  /**
   * 更新排班模板
   */
  updateTemplate: (templateId: number, data: TemplateForm) => {
    return putRequest(`/api/v1/attendance/schedule/template/${templateId}`, data);
  },

  /**
   * 删除排班模板
   */
  deleteTemplate: (templateId: number) => {
    return deleteRequest(`/api/v1/attendance/schedule/template/${templateId}`);
  },

  /**
   * 批量删除模板
   */
  batchDeleteTemplates: (templateIds: number[]) => {
    return postRequest('/api/v1/attendance/schedule/template/batch-delete', templateIds);
  },

  /**
   * 复制模板
   */
  copyTemplate: (templateId: number, newTemplateName: string) => {
    return postRequest(`/api/v1/attendance/schedule/template/${templateId}/copy`, {
      newTemplateName
    });
  },

  /**
   * 应用排班模板
   */
  applyTemplate: (templateId: number, request: ApplyTemplateRequest) => {
    return postRequest(`/api/v1/attendance/smart-scheduling/template/${templateId}/apply`, request);
  },

  /**
   * 启用/禁用模板
   */
  updateTemplateStatus: (templateId: number, status: number) => {
    return putRequest(`/api/v1/attendance/schedule/template/${templateId}/status`, { status });
  },

  // ==================== 日历视图 ====================

  /**
   * 获取日历视图数据
   */
  getCalendarView: (params: CalendarViewParams) => {
    return getRequest('/api/v1/attendance/schedule/calendar', params);
  },

  // ==================== 排班记录管理 ====================

  /**
   * 保存排班记录
   */
  saveScheduleRecord: (record: ScheduleRecordForm) => {
    return postRequest('/api/v1/attendance/schedule/record', record);
  },

  /**
   * 批量保存排班记录
   */
  batchSaveRecords: (records: ScheduleRecordForm[]) => {
    return postRequest('/api/v1/attendance/schedule/records/batch', records);
  },

  /**
   * 删除排班记录
   */
  deleteRecord: (recordId: number) => {
    return deleteRequest(`/api/v1/attendance/schedule/record/${recordId}`);
  },

  /**
   * 批量删除记录
   */
  batchDeleteRecords: (recordIds: number[]) => {
    return postRequest('/api/v1/attendance/schedule/records/batch-delete', recordIds);
  },

  /**
   * 获取排班详情
   */
  getRecordDetail: (recordId: number) => {
    return getRequest(`/api/v1/attendance/schedule/record/${recordId}`);
  }
};

/**
 * 类型定义
 */

// ==================== 枚举类型 ====================

/** 优化目标枚举 */
export enum OptimizationTarget {
  BALANCE_WORKLOAD = 'BALANCE_WORKLOAD',   // 均衡工作负载
  MINIMIZE_OVERTIME = 'MINIMIZE_OVERTIME', // 最小化加班
  MAXIMIZE_COVERAGE = 'MAXIMIZE_COVERAGE', // 最大化覆盖
  REDUCE_COST = 'REDUCE_COST'              // 降低成本
}

/** 冲突类型枚举 */
export enum ConflictType {
  EMPLOYEE_CONFLICT = 'EMPLOYEE_CONFLICT',     // 人员冲突（同一人员多个班次）
  SHIFT_CONFLICT = 'SHIFT_CONFLICT',           // 班次冲突（班次时间重叠）
  SKILL_MISMATCH = 'SKILL_MISMATCH',           // 技能不匹配
  AVAILABILITY_CONFLICT = 'AVAILABILITY_CONFLICT', // 可用性冲突
  REST_PERIOD_VIOLATION = 'REST_PERIOD_VIOLATION' // 休息时间违规
}

/** 模板类型枚举 */
export enum TemplateType {
  DEPARTMENT = 'DEPARTMENT',     // 部门模板
  POSITION = 'POSITION',         // 岗位模板
  PERSONAL = 'PERSONAL',         // 个人模板
  GLOBAL = 'GLOBAL'              // 全局模板
}

/** 周期类型枚举 */
export enum CycleType {
  WEEKLY = 'WEEKLY',             // 每周
  MONTHLY = 'MONTHLY',           // 每月
  CUSTOM = 'CUSTOM'              // 自定义
}

// ==================== 模板相关类型 ====================

/** 模板查询表单 */
export interface TemplateQueryForm {
  templateName?: string;
  templateType?: TemplateType;
  departmentId?: number;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

/** 模板表单 */
export interface TemplateForm {
  templateId?: number;
  templateName: string;
  templateType: TemplateType;
  departmentId?: number;
  templateConfig: TemplateConfig;
  status: number;
  applicableDepartments?: number[];
  description?: string;
}

/** 模板配置 */
export interface TemplateConfig {
  cycleType: CycleType;
  shiftPattern: ShiftPattern[];
  constraints?: TemplateConstraint[];
  effectiveDays?: number[];  // [1,2,3,4,5] 工作日
}

/** 班次模式 */
export interface ShiftPattern {
  dayOfWeek: number;            // 1-7 (周一到周日)
  shiftId: number;
  shiftName: string;
  requiredEmployees?: number;
  optionalEmployees?: number[];
}

/** 模板约束 */
export interface TemplateConstraint {
  type: 'MIN_EMPLOYEES' | 'MAX_EMPLOYEES' | 'REQUIRED_SKILLS' | 'FORBID_OVERLAP';
  value: any;
}

/** 模板VO */
export interface TemplateVO {
  templateId: number;
  templateName: string;
  templateType: TemplateType;
  templateTypeDesc: string;
  departmentId?: number;
  departmentName?: string;
  templateConfig: TemplateConfig;
  status: number;
  statusDesc: string;
  applicableEmployees: number;
  applyCount: number;
  lastAppliedTime?: string;
  templateVersion: string;
  description?: string;
  createTime: string;
  updateTime: string;
  createUserName?: string;
  updateUserName?: string;
}

/** 应用模板请求 */
export interface ApplyTemplateRequest {
  startDate: string;
  endDate: string;
  departmentIds?: number[];
  employeeIds?: number[];
  enableOptimization?: boolean;
  autoResolveConflicts?: boolean;
}

/** 应用模板结果 */
export interface ApplyTemplateResult {
  success: boolean;
  createdRecords: number;
  updatedRecords: number;
  failedRecords: number;
  records: ScheduleRecord[];
  conflicts?: ScheduleConflict[];
}

// ==================== 智能排班相关类型 ====================

/** 排班请求 */
export interface ScheduleRequest {
  planId: number;
  startDate: string;
  endDate: string;
  employeeIds?: number[];
  departmentIds?: number[];
  shiftIds?: number[];
  constraints?: ScheduleConstraint[];
  optimizationTarget?: OptimizationTarget;
}

/** 排班约束 */
export interface ScheduleConstraint {
  type: 'MAX_WORK_HOURS' | 'MIN_REST_HOURS' | 'MAX_CONSECUTIVE_DAYS' | 'SKILL_REQUIRED';
  value: number | string;
  employeeIds?: number[];
  departmentIds?: number[];
}

/** 快速生成请求 */
export interface QuickGenerateRequest {
  planName: string;
  startDate: string;
  endDate: string;
  departmentIds?: number[];
  employeeIds?: number[];
  useTemplate?: boolean;
  templateId?: number;
  autoResolveConflicts?: boolean;
}

/** 优化请求 */
export interface OptimizeScheduleRequest {
  scheduleData: ScheduleData;
  optimizationTarget: OptimizationTarget;
}

// ==================== 冲突检测相关类型 ====================

/** 冲突检测请求 */
export interface ConflictDetectionRequest {
  records: ScheduleRecord[];
  checkRules?: string[];
  startDate: string;
  endDate: string;
}

/** 排班数据 */
export interface ScheduleData {
  planId: number;
  records: ScheduleRecord[];
  startDate: string;
  endDate: string;
}

/** 排班记录 */
export interface ScheduleRecord {
  recordId?: number;
  planId: number;
  employeeId: number;
  employeeName: string;
  departmentId: number;
  departmentName: string;
  shiftId: number;
  shiftName: string;
  shiftType: number;
  workDate: string;
  startTime: string;
  endTime: string;
  status: number;
  hasConflict?: boolean;
}

/** 冲突信息 */
export interface ScheduleConflict {
  conflictId: string;
  type: ConflictType;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
  affectedRecords: ScheduleRecord[];
  suggestedResolution?: string;
}

/** 冲突检测结果 */
export interface ConflictDetectionResult {
  hasConflicts: boolean;
  conflicts: ScheduleConflict[];
  conflictCount: number;
  highSeverityCount: number;
  mediumSeverityCount: number;
  lowSeverityCount: number;
}

/** 冲突解决方案 */
export interface ConflictResolution {
  resolved: boolean;
  resolvedConflicts: number;
  resolutions: ConflictResolutionItem[];
  updatedRecords?: ScheduleRecord[];
}

/** 冲突解决项 */
export interface ConflictResolutionItem {
  conflictId: string;
  resolution: string;
  action: 'MODIFY' | 'REMOVE' | 'SWAP';
  affectedRecords: ScheduleRecord[];
}

// ==================== 排班优化相关类型 ====================

/** 优化结果 */
export interface OptimizedSchedule {
  improved: boolean;
  improvementPercentage: number;
  optimizationDetails: OptimizationDetails;
  optimizedRecords: ScheduleRecord[];
}

/** 优化详情 */
export interface OptimizationDetails {
  target: OptimizationTarget;
  beforeScore: number;
  afterScore: number;
  improvements: string[];
}

/** 排班预测 */
export interface SchedulePrediction {
  predictedCoverage: number;
  predictedOvertime: number;
  predictedCost: number;
  riskFactors: RiskFactor[];
  recommendations: string[];
}

/** 风险因素 */
export interface RiskFactor {
  type: string;
  level: 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
  mitigation?: string;
}

// ==================== 排班统计相关类型 ====================

/** 排班统计 */
export interface ScheduleStatistics {
  totalShifts: number;
  totalEmployees: number;
  totalHours: number;
  averageHoursPerEmployee: number;
  overtimeHours: number;
  coverageRate: number;
  departmentStats: DepartmentScheduleStats[];
  shiftDistribution: ShiftDistribution[];
}

/** 部门排班统计 */
export interface DepartmentScheduleStats {
  departmentId: number;
  departmentName: string;
  totalShifts: number;
  totalEmployees: number;
  totalHours: number;
  coverageRate: number;
}

/** 班次分布 */
export interface ShiftDistribution {
  shiftId: number;
  shiftName: string;
  count: number;
  percentage: number;
}

// ==================== 日历视图相关类型 ====================

/** 日历视图参数 */
export interface CalendarViewParams {
  viewMode: 'MONTH' | 'WEEK' | 'DAY';
  startDate: string;
  endDate: string;
  departmentIds?: number[];
  employeeIds?: number[];
  shiftIds?: number[];
}

/** 日历视图数据 */
export interface CalendarViewData {
  dates: CalendarDate[];
  records: ScheduleRecord[];
  statistics: CalendarStatistics;
}

/** 日历日期 */
export interface CalendarDate {
  date: string;
  isWorkingDay: boolean;
  isHoliday: boolean;
  holidayName?: string;
  dayOfWeek: number;
}

/** 日历统计 */
export interface CalendarStatistics {
  totalShifts: number;
  totalEmployees: number;
  coverageRate: number;
  conflictCount: number;
}

// ==================== 排班记录表单 ====================

/** 排班记录表单 */
export interface ScheduleRecordForm {
  recordId?: number;
  planId: number;
  employeeId: number;
  shiftId: number;
  workDate: string;
  startTime?: string;
  endTime?: string;
  status?: number;
  notes?: string;
}

/** 排班请求 */
export interface ScheduleRequest {
  planId: number;
  startDate: string;
  endDate: string;
  employeeIds?: number[];
  departmentIds?: number[];
  shiftIds?: number[];
  constraints?: ScheduleConstraint[];
  optimizationTarget?: OptimizationTarget;
}

/** 排班约束 */
export interface ScheduleConstraint {
  type: 'MAX_WORK_HOURS' | 'MIN_REST_HOURS' | 'MAX_CONSECUTIVE_DAYS' | 'SKILL_REQUIRED';
  value: number | string;
  employeeIds?: number[];
  departmentIds?: number[];
}

/** 排班数据 */
export interface ScheduleData {
  planId: number;
  records: ScheduleRecord[];
  startDate: string;
  endDate: string;
}

/** 排班记录 */
export interface ScheduleRecord {
  recordId?: number;
  planId: number;
  employeeId: number;
  employeeName: string;
  departmentId: number;
  departmentName: string;
  shiftId: number;
  shiftName: string;
  shiftType: number;
  workDate: string;
  startTime: string;
  endTime: string;
  status: number;
}

/** 冲突信息 */
export interface ScheduleConflict {
  conflictId: string;
  type: ConflictType;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
  affectedRecords: ScheduleRecord[];
  suggestedResolution?: string;
}

/** 冲突检测结果 */
export interface ConflictDetectionResult {
  hasConflicts: boolean;
  conflicts: ScheduleConflict[];
  conflictCount: number;
  highSeverityCount: number;
  mediumSeverityCount: number;
  lowSeverityCount: number;
}

/** 冲突解决方案 */
export interface ConflictResolution {
  resolved: boolean;
  resolvedConflicts: number;
  resolutions: ConflictResolutionItem[];
  updatedRecords?: ScheduleRecord[];
}

/** 冲突解决项 */
export interface ConflictResolutionItem {
  conflictId: string;
  resolution: string;
  action: 'MODIFY' | 'REMOVE' | 'SWAP';
  affectedRecords: ScheduleRecord[];
}

/** 优化结果 */
export interface OptimizedSchedule {
  improved: boolean;
  improvementPercentage: number;
  optimizationDetails: OptimizationDetails;
  optimizedRecords: ScheduleRecord[];
}

/** 优化详情 */
export interface OptimizationDetails {
  target: OptimizationTarget;
  beforeScore: number;
  afterScore: number;
  improvements: string[];
}

/** 排班预测 */
export interface SchedulePrediction {
  predictedCoverage: number;
  predictedOvertime: number;
  predictedCost: number;
  riskFactors: RiskFactor[];
  recommendations: string[];
}

/** 风险因素 */
export interface RiskFactor {
  type: string;
  level: 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
  mitigation?: string;
}

/** 排班统计 */
export interface ScheduleStatistics {
  totalShifts: number;
  totalEmployees: number;
  totalHours: number;
  averageHoursPerEmployee: number;
  overtimeHours: number;
  coverageRate: number;
  departmentStats: DepartmentScheduleStats[];
  shiftDistribution: ShiftDistribution[];
}

/** 部门排班统计 */
export interface DepartmentScheduleStats {
  departmentId: number;
  departmentName: string;
  totalShifts: number;
  totalEmployees: number;
  totalHours: number;
  coverageRate: number;
}

/** 班次分布 */
export interface ShiftDistribution {
  shiftId: number;
  shiftName: string;
  count: number;
  percentage: number;
}

/** 快速生成请求 */
export interface QuickGenerateRequest {
  planName: string;
  startDate: string;
  endDate: string;
  departmentIds?: number[];
  employeeIds?: number[];
  useTemplate?: boolean;
  templateId?: number;
  autoResolveConflicts?: boolean;
}

/** 冲突检测请求 */
export interface ConflictDetectionRequest {
  records: ScheduleRecord[];
  checkRules?: string[];
  startDate: string;
  endDate: string;
}

/** 日历视图参数 */
export interface CalendarViewParams {
  viewMode: 'MONTH' | 'WEEK' | 'DAY';
  startDate: string;
  endDate: string;
  departmentIds?: number[];
  employeeIds?: number[];
  shiftIds?: number[];
}

/** 日历视图数据 */
export interface CalendarViewData {
  dates: CalendarDate[];
  records: ScheduleRecord[];
  statistics: CalendarStatistics;
}

/** 日历日期 */
export interface CalendarDate {
  date: string;
  isWorkingDay: boolean;
  isHoliday: boolean;
  holidayName?: string;
  dayOfWeek: number;
}

/** 日历统计 */
export interface CalendarStatistics {
  totalShifts: number;
  totalEmployees: number;
  coverageRate: number;
  conflictCount: number;
}

/** 排班记录表单 */
export interface ScheduleRecordForm {
  recordId?: number;
  planId: number;
  employeeId: number;
  shiftId: number;
  workDate: string;
  status?: number;
  notes?: string;
}

/** 排班模板 */
export interface ScheduleTemplate {
  templateId: number;
  templateName: string;
  templateCode: string;
  description: string;
  cycleType: 'WEEKLY' | 'MONTHLY' | 'CUSTOM';
  shiftPattern: ShiftPattern[];
  applicableDepartments: number[];
  isDefault: boolean;
  status: number;
}

/** 班次模式 */
export interface ShiftPattern {
  dayOfWeek: number;
  shiftId: number;
  shiftName: string;
  isRequired: boolean;
}
