/*
 * 门禁通行记录相关类型定义
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

export interface AccessRecord {
  recordId: number;
  userId?: number;
  userName?: string;
  userCardNumber?: string;
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  areaId?: number;
  areaName?: string;
  accessType: AccessType;
  accessResult: AccessResult;
  accessTime: string;
  cardNumber?: string;
  cardType?: CardType;
  photoUrl?: string;
  verificationMethod?: VerificationMethod;
  abnormalReason?: string;
  isAbnormal: boolean;
  processedStatus?: ProcessedStatus;
  processUserId?: number;
  processUserName?: string;
  processTime?: string;
  processRemark?: string;
  deviceId?: number;
  deviceName?: string;
  location?: string;
  createTime: string;
}

export enum AccessType {
  IN = 'in',
  OUT = 'out',
  PASS = 'pass'
}

export enum AccessResult {
  SUCCESS = 'success',
  FAILED = 'failed',
  DENIED = 'denied',
  TIMEOUT = 'timeout'
}

export enum CardType {
  ID_CARD = 'id_card',
  IC_CARD = 'ic_card',
  FACE = 'face',
  FINGERPRINT = 'fingerprint',
  PASSWORD = 'password',
  QR_CODE = 'qr_code'
}

export enum VerificationMethod {
  CARD_ONLY = 'card_only',
  CARD_PASSWORD = 'card_password',
  CARD_FACE = 'card_face',
  FACE_ONLY = 'face_only',
  FINGERPRINT_ONLY = 'fingerprint_only',
  PASSWORD_ONLY = 'password_only',
  QR_CODE_ONLY = 'qr_code_only'
}

export enum ProcessedStatus {
  UNPROCESSED = 'unprocessed',
  PROCESSED = 'processed',
  IGNORED = 'ignored'
}

export interface RecordQueryForm {
  pageNum?: number;
  pageSize?: number;
  userId?: number;
  userName?: string;
  userCardNumber?: string;
  deviceId?: number;
  deviceName?: string;
  areaId?: number;
  accessType?: AccessType;
  accessResult?: AccessResult;
  accessTimeStart?: string;
  accessTimeEnd?: string;
  isAbnormal?: boolean;
  processedStatus?: ProcessedStatus;
  verificationMethod?: VerificationMethod;
}

export interface AccessRecordDetail extends AccessRecord {
  deviceInfo?: {
    deviceCode: string;
    deviceName: string;
    location: string;
    ip: string;
  };
  userInfo?: {
    userId: number;
    userName: string;
    cardNumber: string;
    departmentName?: string;
    phone?: string;
  };
  permissionInfo?: {
    permissionId: number;
    permissionType: string;
    validStartTime: string;
    validEndTime: string;
  };
}

export interface AccessStats {
  totalAccessCount: number;
  successCount: number;
  failedCount: number;
  abnormalCount: number;
  uniqueUserCount: number;
  peakHour?: {
    hour: number;
    count: number;
  };
  accessByType: Record<AccessType, number>;
  accessByResult: Record<AccessResult, number>;
  accessByMethod: Record<VerificationMethod, number>;
}

export interface TimeRangeStats {
  timeRange: string;
  accessCount: number;
  successCount: number;
  failedCount: number;
  uniqueUserCount: number;
}

export interface UserAccessStats {
  userId: number;
  userName: string;
  cardNumber: string;
  totalAccess: number;
  successAccess: number;
  failedAccess: number;
  firstAccessTime?: string;
  lastAccessTime?: string;
  frequentDevices: Array<{
    deviceId: number;
    deviceName: string;
    accessCount: number;
  }>;
}

export interface DeviceAccessStats {
  deviceId: number;
  deviceName: string;
  deviceCode: string;
  location: string;
  totalAccess: number;
  successAccess: number;
  failedAccess: number;
  uniqueUserCount: number;
  peakHour?: {
    hour: number;
    count: number;
  };
}

export interface AbnormalRecordHandleForm {
  recordId: number;
  processedStatus: ProcessedStatus;
  processRemark?: string;
}

export interface RealTimeRecord {
  recordId: number;
  userName?: string;
  deviceName: string;
  areaName?: string;
  accessType: AccessType;
  accessResult: AccessResult;
  accessTime: string;
  photoUrl?: string;
  isAbnormal: boolean;
  abnormalReason?: string;
}