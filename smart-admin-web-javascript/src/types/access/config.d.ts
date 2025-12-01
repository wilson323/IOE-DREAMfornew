/*
 * 门禁配置相关类型定义
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

export interface AccessSystemConfig {
  systemName: string;
  systemVersion: string;
  enableRealTimeMonitor: boolean;
  heartbeatInterval: number;
  alertRetentionDays: number;
  recordRetentionDays: number;
  enableAutoBackup: boolean;
  backupInterval: number;
  maxConcurrentConnections: number;
  enableEncryption: boolean;
  encryptionKey?: string;
  enableDataMasking: boolean;
  enableAuditLog: boolean;
  defaultLanguage: string;
  timezone: string;
  maintenanceMode: boolean;
  maintenanceStartTime?: string;
  maintenanceEndTime?: string;
}

export interface SecurityLevel {
  levelId: number;
  levelName: string;
  levelCode: string;
  level: number;
  description?: string;
  requiredVerifications: VerificationMethod[];
  maxAccessAttempts: number;
  lockoutDuration: number;
  requireTwoFactor: boolean;
  allowedTimeStrategies?: number[];
  alertOnFailure: boolean;
  status: SecurityLevelStatus;
  createTime: string;
  updateTime: string;
}

export enum SecurityLevelStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive'
}

export interface SecurityLevelForm {
  levelName: string;
  levelCode: string;
  level: number;
  description?: string;
  requiredVerifications: VerificationMethod[];
  maxAccessAttempts: number;
  lockoutDuration: number;
  requireTwoFactor: boolean;
  allowedTimeStrategies?: number[];
  alertOnFailure: boolean;
}

export enum VerificationMethod {
  CARD_ONLY = 'card_only',
  CARD_PASSWORD = 'card_password',
  CARD_FACE = 'card_face',
  FACE_ONLY = 'face_only',
  FINGERPRINT_ONLY = 'fingerprint_only',
  PASSWORD_ONLY = 'password_only',
  QR_CODE_ONLY = 'qr_code_only',
  CARD_FINGERPRINT = 'card_fingerprint',
  FACE_FINGERPRINT = 'face_fingerprint',
  TWO_FACTOR = 'two_factor'
}

export interface AlertConfig {
  enableAlert: boolean;
  alertTypes: AlertType[];
  notificationMethods: NotificationMethod[];
  emailRecipients: string[];
  smsRecipients: string[];
  webhookUrls: string[];
  alertLevels: AlertLevel[];
  alertCooldownMinutes: number;
  enableEscalation: boolean;
  escalationRules: EscalationRule[];
}

export interface AlertType {
  type: string;
  enabled: boolean;
  name: string;
  description: string;
}

export enum NotificationMethod {
  EMAIL = 'email',
  SMS = 'sms',
  WEBHOOK = 'webhook',
  SYSTEM = 'system',
  WECHAT = 'wechat'
}

export enum AlertLevel {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
  CRITICAL = 'critical'
}

export interface EscalationRule {
  condition: string;
  delayMinutes: number;
  escalateTo: NotificationMethod[];
  escalateLevel: AlertLevel;
}

export interface LogConfig {
  enableAccessLog: boolean;
  enableOperationLog: boolean;
  enableErrorLog: boolean;
  enablePerformanceLog: boolean;
  logLevel: LogLevel;
  maxLogFileSize: number;
  maxLogFiles: number;
  logRetentionDays: number;
  enableLogCompression: boolean;
  enableLogEncryption: boolean;
  logFormat: string;
}

export enum LogLevel {
  DEBUG = 'debug',
  INFO = 'info',
  WARN = 'warn',
  ERROR = 'error',
  FATAL = 'fatal'
}

export interface DeviceTypeConfig {
  typeCode: string;
  typeName: string;
  description?: string;
  category: DeviceCategory;
  protocol: string;
  defaultPort: number;
  supportedCommands: string[];
  features: DeviceFeature[];
  connectionTimeout: number;
  heartbeatInterval: number;
  maxRetries: number;
  parameters: DeviceParameter[];
  icon?: string;
}

export enum DeviceCategory {
  DOOR_CONTROLLER = 'door_controller',
  TURNSTILE = 'turnstile',
  GATE = 'gate',
  BARRIER = 'barrier',
  ELEVATOR = 'elevator',
  PARKING = 'parking'
}

export interface DeviceFeature {
  code: string;
  name: string;
  description: string;
  enabled: boolean;
}

export interface DeviceParameter {
  key: string;
  name: string;
  type: ParameterType;
  required: boolean;
  defaultValue?: any;
  description?: string;
  validation?: ValidationRule;
}

export enum ParameterType {
  STRING = 'string',
  NUMBER = 'number',
  BOOLEAN = 'boolean',
  ARRAY = 'array',
  OBJECT = 'object'
}

export interface ValidationRule {
  min?: number;
  max?: number;
  pattern?: string;
  enum?: any[];
  custom?: string;
}

export interface DeviceTypeForm {
  typeCode: string;
  typeName: string;
  description?: string;
  category: DeviceCategory;
  protocol: string;
  defaultPort: number;
  supportedCommands: string[];
  features: DeviceFeature[];
  connectionTimeout: number;
  heartbeatInterval: number;
  maxRetries: number;
  parameters: DeviceParameter[];
  icon?: string;
}

export interface ConfigUpdateForm {
  configType: 'system' | 'alert' | 'log';
  configData: Partial<AccessSystemConfig | AlertConfig | LogConfig>;
}