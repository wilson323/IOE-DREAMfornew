/**
 * 智能视频模块 - TypeScript类型定义
 * 移动端专用
 */

// ==================== 设备相关类型 ====================

/**
 * 设备状态
 */
export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'FAULT';

/**
 * 设备类型
 */
export type DeviceType = 'CAMERA' | 'NVR' | 'DVR' | 'DECODER';

/**
 * 视频设备
 */
export interface VideoDevice {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  deviceType: DeviceType;
  deviceStatus: DeviceStatus;
  ipAddress: string;
  port: number;
  manufacturer?: string;
  model?: string;
  location?: string;
  areaId?: number;
  areaName?: string;
  streamUrl?: string;
  hasPTZ?: boolean;
  isFavorite?: boolean;
  lastOnlineTime?: string;
}

// ==================== 告警相关类型 ====================

/**
 * 告警级别
 */
export type AlarmLevel = 'HIGH' | 'MEDIUM' | 'LOW';

/**
 * 告警类型
 */
export type AlarmType = 'MOTION' | 'CROSS_LINE' | 'INTRUSION' | 'LEFT_OBJ' | 'REMOVAL';

/**
 * 告警状态
 */
export type AlarmStatus = 'ACTIVE' | 'CONFIRMED' | 'IGNORED' | 'FALSE';

/**
 * 视频告警
 */
export interface VideoAlarm {
  alarmId: number;
  deviceId: number;
  deviceName: string;
  alarmType: AlarmType;
  alarmLevel: AlarmLevel;
  alarmStatus: AlarmStatus;
  message: string;
  snapshotUrl?: string;
  videoUrl?: string;
  alarmTime: string;
  confirmTime?: string;
  confirmUser?: string;
  feedback?: string;
}

// ==================== 录像相关类型 ====================

/**
 * 录像类型
 */
export type RecordType = 'ALL' | 'MOTION' | 'ALARM' | 'MANUAL';

/**
 * 云录像像
 */
export interface CloudRecord {
  recordId: number;
  deviceId: number;
  deviceName: string;
  startTime: string;
  endTime: string;
  recordType: RecordType;
  fileSize: number;
  duration: number;
  downloadUrl?: string;
}

/**
 * 录像查询条件
 */
export interface RecordQueryForm {
  deviceId?: number;
  startTime: string;
  endTime: string;
  recordType?: RecordType;
  pageNum?: number;
  pageSize?: number;
}

// ==================== 云台相关类型 ====================

/**
 * 云台方向
 */
export type PTZDirection = 'UP' | 'DOWN' | 'LEFT' | 'RIGHT' | 'UP_LEFT' | 'UP_RIGHT' | 'DOWN_LEFT' | 'DOWN_RIGHT';

/**
 * 云台动作
 */
export type PTZAction = 'MOVE' | 'ZOOM_IN' | 'ZOOM_OUT' | 'FOCUS_NEAR' | 'FOCUS_FAR' | 'IRIS_OPEN' | 'IRIS_CLOSE';

/**
 * 预置位
 */
export interface Preset {
  presetId: number;
  presetName: string;
  presetToken: string;
}

/**
 * 云台控制参数
 */
export interface PTZControlParams {
  deviceId: number;
  command: PTZAction;
  direction?: PTZDirection;
  speed?: number;
  presetId?: number;
}

// ==================== 网络相关类型 ====================

/**
 * 网络类型
 */
export type NetworkType = 'wifi' | '4g' | '3g' | '2g' | 'none' | 'unknown';

/**
 * 视频质量
 */
export type VideoQuality = '720p' | '480p' | '360p' | '240p';

/**
 * 流协议
 */
export type StreamProtocol = 'HLS' | 'HTTP-FLV' | 'RTC';

/**
 * 流配置
 */
export interface StreamConfig {
  quality: VideoQuality;
  protocol: StreamProtocol;
  bitrate: number;
  fps: number;
  resolution: {
    width: number;
    height: number;
  };
}

// ==================== API响应类型 ====================

/**
 * 统一响应DTO
 */
export interface ResponseDTO<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp?: number;
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

// ==================== 表单类型 ====================

/**
 * 设备查询表单
 */
export interface DeviceQueryForm {
  deviceName?: string;
  deviceType?: DeviceType;
  deviceStatus?: DeviceStatus;
  areaId?: number;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 告警查询表单
 */
export interface AlarmQueryForm {
  deviceId?: number;
  alarmLevel?: AlarmLevel;
  alarmType?: AlarmType;
  alarmStatus?: AlarmStatus;
  startTime?: string;
  endTime?: string;
  pageNum?: number;
  pageSize?: number;
}

export default {
  // 设备
  VideoDevice,
  DeviceStatus,
  DeviceType,

  // 告警
  VideoAlarm,
  AlarmLevel,
  AlarmType,
  AlarmStatus,

  // 录像
  CloudRecord,
  RecordType,
  RecordQueryForm,

  // 云台
  PTZDirection,
  PTZAction,
  Preset,
  PTZControlParams,

  // 网络
  NetworkType,
  VideoQuality,
  StreamProtocol,
  StreamConfig,

  // API
  ResponseDTO,
  PageResult,

  // 表单
  DeviceQueryForm,
  AlarmQueryForm
};
