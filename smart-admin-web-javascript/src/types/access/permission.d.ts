/*
 * 门禁权限相关类型定义
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

export interface AccessPermission {
  permissionId: number;
  userId: number;
  userName?: string;
  userCardNumber?: string;
  areaId: number;
  areaName?: string;
  permissionType: PermissionType;
  timeStrategyId?: number;
  timeStrategyName?: string;
  validStartTime: string;
  validEndTime: string;
  status: PermissionStatus;
  isTemporary: boolean;
  approveUserId?: number;
  approveUserName?: string;
  approveTime?: string;
  remark?: string;
  createTime: string;
  updateTime: string;
  createUserId?: number;
}

export enum PermissionType {
  AREA = 'area',
  DEVICE = 'device',
  TIME_LIMITED = 'time_limited',
  PERMANENT = 'permanent'
}

export enum PermissionStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  EXPIRED = 'expired',
  REVOKED = 'revoked',
  PENDING = 'pending'
}

export interface PermissionQueryForm {
  pageNum?: number;
  pageSize?: number;
  userId?: number;
  userName?: string;
  userCardNumber?: string;
  areaId?: number;
  permissionType?: PermissionType;
  status?: PermissionStatus;
  isTemporary?: boolean;
  validStartTime?: string;
  validEndTime?: string;
  createTimeStart?: string;
  createTimeEnd?: string;
}

export interface PermissionAssignForm {
  userId: number;
  areaIds: number[];
  permissionType: PermissionType;
  timeStrategyId?: number;
  validStartTime?: string;
  validEndTime?: string;
  isTemporary?: boolean;
  remark?: string;
}

export interface TemporaryPermissionForm {
  userId: number;
  areaIds: number[];
  validStartTime: string;
  validEndTime: string;
  reason: string;
  timeStrategyId?: number;
  remark?: string;
}

export interface PermissionTemplate {
  templateId: number;
  templateName: string;
  templateCode: string;
  description?: string;
  areaIds: number[];
  permissionType: PermissionType;
  timeStrategyId?: number;
  isDefault?: boolean;
  status: TemplateStatus;
  createTime: string;
  updateTime: string;
}

export enum TemplateStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive'
}

export interface PermissionTemplateForm {
  templateName: string;
  templateCode: string;
  description?: string;
  areaIds: number[];
  permissionType: PermissionType;
  timeStrategyId?: number;
  isDefault?: boolean;
}

export interface PermissionStats {
  totalPermissions: number;
  activePermissions: number;
  expiredPermissions: number;
  temporaryPermissions: number;
  expiringSoonCount: number;
  permissionsByType: Record<PermissionType, number>;
}

export interface TimeStrategy {
  strategyId: number;
  strategyName: string;
  strategyCode: string;
  description?: string;
  monday: TimeRange;
  tuesday: TimeRange;
  wednesday: TimeRange;
  thursday: TimeRange;
  friday: TimeRange;
  saturday: TimeRange;
  sunday: TimeRange;
  holidays?: HolidayRule[];
  status: StrategyStatus;
  createTime: string;
  updateTime: string;
}

export interface TimeRange {
  enabled: boolean;
  timeSlots: TimeSlot[];
}

export interface TimeSlot {
  startTime: string;
  endTime: string;
}

export interface HolidayRule {
  date: string;
  enabled: boolean;
  timeRanges: TimeRange[];
}

export enum StrategyStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive'
}

export interface TimeStrategyForm {
  strategyName: string;
  strategyCode: string;
  description?: string;
  monday: TimeRange;
  tuesday: TimeRange;
  wednesday: TimeRange;
  thursday: TimeRange;
  friday: TimeRange;
  saturday: TimeRange;
  sunday: TimeRange;
  holidays?: HolidayRule[];
}