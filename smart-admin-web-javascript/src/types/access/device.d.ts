/*
 * 门禁设备相关类型定义
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

export interface AccessDevice {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  deviceType: string;
  deviceTypeName?: string;
  areaId?: number;
  areaName?: string;
  location: string;
  ip: string;
  port: number;
  status: DeviceStatus;
  onlineStatus: OnlineStatus;
  lastHeartbeatTime?: string;
  manufacturer?: string;
  model?: string;
  firmwareVersion?: string;
  installDate?: string;
  description?: string;
  createTime: string;
  updateTime: string;
  createUserId?: number;
  deletedFlag: boolean;
}

export enum DeviceStatus {
  NORMAL = 'normal',
  MAINTENANCE = 'maintenance',
  FAULT = 'fault',
  OFFLINE = 'offline'
}

export enum OnlineStatus {
  ONLINE = 'online',
  OFFLINE = 'offline',
  UNKNOWN = 'unknown'
}

export interface DeviceQueryForm {
  pageNum?: number;
  pageSize?: number;
  deviceCode?: string;
  deviceName?: string;
  deviceType?: string;
  areaId?: number;
  status?: DeviceStatus;
  onlineStatus?: OnlineStatus;
  manufacturer?: string;
  location?: string;
}

export interface DeviceAddForm {
  deviceCode: string;
  deviceName: string;
  deviceType: string;
  areaId?: number;
  location: string;
  ip: string;
  port: number;
  manufacturer?: string;
  model?: string;
  firmwareVersion?: string;
  description?: string;
}

export interface DeviceUpdateForm {
  deviceId: number;
  deviceName?: string;
  deviceType?: string;
  areaId?: number;
  location?: string;
  ip?: string;
  port?: number;
  manufacturer?: string;
  model?: string;
  firmwareVersion?: string;
  description?: string;
}

export interface DeviceStats {
  totalCount: number;
  onlineCount: number;
  offlineCount: number;
  faultCount: number;
  maintenanceCount: number;
  onlineRate: number;
}

export interface DeviceGroup {
  groupId: number;
  groupName: string;
  groupCode: string;
  parentId?: number;
  level: number;
  children?: DeviceGroup[];
  deviceCount?: number;
}

export interface DeviceRealTimeStatus {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  status: DeviceStatus;
  onlineStatus: OnlineStatus;
  lastHeartbeatTime: string;
  cpuUsage?: number;
  memoryUsage?: number;
  temperature?: number;
  powerStatus?: string;
}

export interface DeviceType {
  typeCode: string;
  typeName: string;
  description?: string;
  features: string[];
  defaultPort: number;
  supportedCommands: string[];
}