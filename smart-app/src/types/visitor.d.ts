/**
 * 访客模块 - TypeScript类型定义
 * 移动端专用
 */

// ==================== 访客预约相关类型 ====================

/**
 * 预约类型
 */
export type AppointmentType = 'NORMAL' | 'VIP' | 'SUPPLIER' | 'INTERVIEW' | 'TEMPORARY';

/**
 * 预约状态
 */
export type AppointmentStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'CHECKED_IN' | 'CHECKED_OUT' | 'EXPIRED';

/**
 * 访客预约
 */
export interface VisitorAppointment {
  appointmentId: number;
  visitorId: number;
  visitorName: string;
  visitorPhone: string;
  visitorIdCard?: string;
  visiteeId: number;
  visiteeName: string;
  visiteePhone?: string;
  appointmentType: AppointmentType;
  visitReason: string;
  appointmentTime: string;
  expectedArrivalTime?: string;
  status: AppointmentStatus;
  qrCode?: string;
  qrCodeExpireTime?: string;
  checkInTime?: string;
  checkOutTime?: string;
  actualArrivalTime?: string;
  accessAreas?: string[];
  remark?: string;
  createTime: string;
  updateTime: string;
  approvalUserId?: number;
  approvalUserName?: string;
  approvalTime?: string;
  rejectionReason?: string;
}

// ==================== 访客登记相关类型 ====================

/**
 * 访客登记
 */
export interface VisitorRegistration {
  registrationId: number;
  appointmentId?: number;
  visitorId: number;
  visitorName: string;
  visitorPhone: string;
  visitorIdCard?: string;
  visiteeId: number;
  visiteeName: string;
  registrationType: 'APPOINTMENT' | 'TEMPORARY';
  checkInTime: string;
  checkOutTime?: string;
  accessAreas: string[];
  vehiclePlate?: string;
  companionCount?: number;
  photoUrl?: string;
  signature?: string;
  logisticsItems?: LogisticsItem[];
  createTime: string;
  updateTime: string;
}

/**
 * 访客信息
 */
export interface VisitorInfo {
  visitorId: number;
  visitorName: string;
  visitorPhone: string;
  visitorIdCard?: string;
  visitorType?: 'INDIVIDUAL' | 'COMPANY' | 'GOVERNMENT';
  companyName?: string;
  idCardFrontUrl?: string;
  idCardBackUrl?: string;
  facePhotoUrl?: string;
  blacklistFlag: boolean;
  visitCount: number;
  lastVisitTime?: string;
  createTime: string;
  updateTime: string;
}

// ==================== 身份验证相关类型 ====================

/**
 * 认证方式
 */
export type VerificationMethod = 'FACE' | 'QR_CODE' | 'ID_CARD' | 'SMS' | 'MANUAL';

/**
 * 认证结果
 */
export type VerificationResult = 'SUCCESS' | 'FAILED' | 'PENDING' | 'BLACKLISTED';

/**
 * 身份验证记录
 */
export interface VisitorVerification {
  verificationId: number;
  registrationId: number;
  visitorId: number;
  visitorName: string;
  verificationMethod: VerificationMethod;
  verificationResult: VerificationResult;
  faceSimilarity?: number;
  livenessScore?: number;
  blacklistFlag?: boolean;
  verificationTime: string;
  deviceId?: string;
  deviceLocation?: string;
  failReason?: string;
  photoUrl?: string;
  createTime: string;
}

// ==================== 物流管理相关类型 ====================

/**
 * 物品状态
 */
export type ItemStatus = 'DEPOSITED' | 'PICKED_UP' | 'OVERDUE' | 'TRANSFERRED';

/**
 * 物品类型
 */
export type ItemType = 'GENERAL' | 'VALUABLE' | 'DANGEROUS' | 'ELECTRONIC' | 'DOCUMENT';

/**
 * 物流物品
 */
export interface LogisticsItem {
  itemId: number;
  registrationId: number;
  logisticsNo: string;
  itemName: string;
  itemType: ItemType;
  itemCount: number;
  itemDescription?: string;
  itemPhotoUrl?: string;
  depositTime: string;
  pickupTime?: string;
  itemStatus: ItemStatus;
  depositorName: string;
  depositorPhone: string;
  pickupPersonName?: string;
  pickupPersonPhone?: string;
  remark?: string;
  createTime: string;
  updateTime: string;
}

/**
 * 物品寄存
 */
export interface ItemDeposit {
  registrationId: number;
  itemName: string;
  itemType: ItemType;
  itemCount: number;
  itemDescription?: string;
  itemPhoto?: string;
  depositorName: string;
  depositorPhone: string;
}

/**
 * 物品领取
 */
export interface ItemPickup {
  logisticsNo: string;
  pickupPersonName: string;
  pickupPersonPhone: string;
  pickupPersonIdCard?: string;
  signature?: string;
}

// ==================== 通行记录相关类型 ====================

/**
 * 通行类型
 */
export type AccessType = 'CHECK_IN' | 'CHECK_OUT' | 'ENTRY' | 'EXIT';

/**
 * 通行记录
 */
export interface VisitorAccessRecord {
  recordId: number;
  registrationId: number;
  visitorId: number;
  visitorName: string;
  visiteeId: number;
  visiteeName: string;
  accessType: AccessType;
  accessTime: string;
  deviceId: string;
  deviceName: string;
  areaId: number;
  areaName: string;
  accessMethod: VerificationMethod;
  photoUrl?: string;
  createTime: string;
}

// ==================== 统计分析相关类型 ====================

/**
 * 访客统计
 */
export interface VisitorStatistics {
  totalVisits: number;
  uniqueVisitors: number;
  activeVisitors: number;
  pendingApprovals: number;
  todayCheckIns: number;
  todayCheckOuts: number;
  averageVisitDuration: number;
  visitTrend: number;
  topVisitees: VisiteeStatistics[];
  appointmentTypeStats: AppointmentTypeStats[];
  dailyStats: DailyStatistics[];
}

/**
 * 被访人统计
 */
export interface VisiteeStatistics {
  visiteeId: number;
  visiteeName: string;
  departmentName?: string;
  visitCount: number;
  visitorCount: number;
}

/**
 * 预约类型统计
 */
export interface AppointmentTypeStats {
  appointmentType: AppointmentType;
  count: number;
  percentage: number;
}

/**
 * 日统计
 */
export interface DailyStatistics {
  date: string;
  totalVisits: number;
  uniqueVisitors: number;
  checkIns: number;
  checkOuts: number;
}

/**
 * 访问趋势
 */
export interface VisitTrend {
  date: string;
  count: number;
}

// ==================== 表单类型 ====================

/**
 * 访客预约表单
 */
export interface VisitorAppointmentForm {
  visitorName: string;
  visitorPhone: string;
  visitorIdCard?: string;
  visiteeId: number;
  appointmentType: AppointmentType;
  visitReason: string;
  appointmentTime: string;
  expectedArrivalTime?: string;
  accessAreas?: string[];
  remark?: string;
}

/**
 * 访客预约查询表单
 */
export interface AppointmentQueryForm {
  visitorName?: string;
  visitorPhone?: string;
  visiteeId?: number;
  appointmentType?: AppointmentType;
  status?: AppointmentStatus;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 访客登记查询表单
 */
export interface RegistrationQueryForm {
  visitorName?: string;
  visitorPhone?: string;
  visiteeId?: number;
  registrationType?: string;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 通行记录查询表单
 */
export interface AccessRecordQueryForm {
  visitorName?: string;
  visitorPhone?: string;
  visiteeId?: number;
  accessType?: AccessType;
  areaId?: number;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 物品查询表单
 */
export interface LogisticsQueryForm {
  logisticsNo?: string;
  registrationId?: number;
  itemStatus?: ItemStatus;
  itemType?: ItemType;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 统计查询表单
 */
export interface StatisticsQueryForm {
  startDate: string;
  endDate: string;
  visiteeId?: number;
  departmentId?: number;
  groupBy?: 'DAY' | 'WEEK' | 'MONTH' | 'VISITEE' | 'TYPE';
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

/**
 * 预约详情响应
 */
export interface AppointmentDetailResponse {
  appointment: VisitorAppointment;
  visitor: VisitorInfo;
  visitee: {
    visiteeId: number;
    visiteeName: string;
    departmentName?: string;
    phone?: string;
  };
  accessRecords?: VisitorAccessRecord[];
  logisticsItems?: LogisticsItem[];
}

/**
 * 二维码响应
 */
export interface QRCodeResponse {
  qrCode: string;
  qrCodeUrl: string;
  expireTime: string;
  appointmentId: number;
}

/**
 * 人脸识别响应
 */
export interface FaceVerificationResponse {
  verified: boolean;
  score: number;
  livenessScore?: number;
  blacklistFlag: boolean;
  visitorInfo?: VisitorInfo;
  failReason?: string;
}

/**
 * 物品寄存响应
 */
export interface ItemDepositResponse {
  logisticsNo: string;
  itemId: number;
  depositTime: string;
  qrCode: string;
}

// ==================== WebSocket消息类型 ====================

/**
 * WebSocket消息类型
 */
export type WSMessageType = 'APPOINTMENT_CREATED' | 'APPOINTMENT_APPROVED' | 'APPOINTMENT_REJECTED' | 'VISITOR_ARRIVED' | 'VISITOR_DEPARTED' | 'ITEM_DEPOSITED' | 'ITEM_PICKED_UP';

/**
 * WebSocket消息
 */
export interface VisitorWSMessage {
  type: WSMessageType;
  data: any;
  timestamp: number;
}

/**
 * 预约创建消息
 */
export interface AppointmentCreatedMessage {
  appointmentId: number;
  visitorName: string;
  visiteeName: string;
  appointmentTime: string;
}

/**
 * 预约审批消息
 */
export interface AppointmentApprovedMessage {
  appointmentId: number;
  visitorName: string;
  status: AppointmentStatus;
  approvalUserName: string;
  approvalTime: string;
  rejectionReason?: string;
}

/**
 * 访客到达消息
 */
export interface VisitorArrivedMessage {
  registrationId: number;
  visitorName: string;
  visiteeName: string;
  checkInTime: string;
  photoUrl?: string;
}

export default {
  // 预约
  VisitorAppointment,
  AppointmentType,
  AppointmentStatus,
  VisitorAppointmentForm,
  AppointmentQueryForm,

  // 登记
  VisitorRegistration,
  VisitorInfo,

  // 验证
  VisitorVerification,
  VerificationMethod,
  VerificationResult,
  FaceVerificationResponse,

  // 物流
  LogisticsItem,
  ItemDeposit,
  ItemPickup,
  ItemDepositResponse,
  ItemType,
  ItemStatus,

  // 通行
  VisitorAccessRecord,
  AccessType,
  AccessRecordQueryForm,

  // 统计
  VisitorStatistics,
  VisiteeStatistics,
  AppointmentTypeStats,
  DailyStatistics,
  VisitTrend,
  StatisticsQueryForm,

  // API
  ResponseDTO,
  PageResult,
  AppointmentDetailResponse,
  QRCodeResponse,

  // WebSocket
  VisitorWSMessage,
  WSMessageType,
  AppointmentCreatedMessage,
  AppointmentApprovedMessage,
  VisitorArrivedMessage
};
