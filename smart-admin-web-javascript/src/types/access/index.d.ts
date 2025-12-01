/*
 * 门禁管理类型定义统一导出
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

export * from './device';
export * from './permission';
export * from './record';
export * from './config';

// 通用类型定义
export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

export interface ResponseDTO<T = any> {
  code: number;
  msg: string;
  data: T;
  success: boolean;
}

export interface WebSocketMessage<T = any> {
  type: string;
  data: T;
  timestamp: number;
  messageId?: string;
}

export interface AlertMessage {
  alertId: string;
  alertType: string;
  level: 'low' | 'medium' | 'high' | 'critical';
  title: string;
  content: string;
  sourceId?: number;
  sourceType?: string;
  sourceName?: string;
  timestamp: number;
  status: 'new' | 'processing' | 'resolved' | 'ignored';
}

export interface DashboardStats {
  totalDevices: number;
  onlineDevices: number;
  totalUsers: number;
  activePermissions: number;
  todayAccessCount: number;
  abnormalAlertCount: number;
  systemHealth: 'good' | 'warning' | 'error';
  lastUpdateTime: string;
}

export interface AreaTreeNode {
  areaId: number;
  areaName: string;
  areaCode: string;
  parentId?: number;
  level: number;
  areaType: string;
  status: 'active' | 'inactive';
  children?: AreaTreeNode[];
  deviceCount?: number;
  permissionCount?: number;
}

export interface UserSelectInfo {
  userId: number;
  userName: string;
  realName?: string;
  cardNumber?: string;
  departmentName?: string;
  phone?: string;
  email?: string;
  status: 'active' | 'inactive' | 'locked';
}

export interface DeviceSelectInfo {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  deviceType: string;
  location: string;
  status: string;
  onlineStatus: 'online' | 'offline';
}

export interface ExportParams {
  format: 'excel' | 'csv' | 'pdf';
  columns?: string[];
  dateRange?: [string, string];
  filters?: Record<string, any>;
}

export interface ImportResult {
  successCount: number;
  failureCount: number;
  totalCount: number;
  errors: Array<{
    row: number;
    field: string;
    message: string;
    value?: any;
  }>;
}