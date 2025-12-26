/**
 * 消费模块 - TypeScript 类型定义
 *
 * 为消费模块提供完整的TypeScript类型定义，增强类型安全和IDE支持
 */

// ==================== 基础类型定义 ====================

/**
 * 消费模式枚举
 */
export type ConsumeMode =
  | 'FIXED_AMOUNT' // 固定金额
  | 'FREE_AMOUNT' // 自由金额
  | 'METERED' // 计量消费
  | 'PRODUCT' // 商品消费
  | 'ORDER' // 订餐消费
  | 'INTELLIGENCE'; // 智能消费

/**
 * 管理模式枚举
 */
export type ManagementMode = 1 | 2 | 3; // 1=餐别制 2=超市制 3=混合模式

/**
 * 设备类型枚举
 */
export type DeviceType = 'POS' | 'CONSUME_MACHINE' | 'CARD_READER' | 'BIOMETRIC';

/**
 * 设备状态枚举
 */
export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'FAULT';

/**
 * 账户类型枚举
 */
export type AccountType = 'CASH' | 'SUBSIDY' | 'CREDIT';

/**
 * 补贴类型枚举
 */
export type SubsidyType = 'MONTHLY' | 'ONE_TIME' | 'CONDITIONAL';

/**
 * 补贴条件枚举
 */
export type SubsidyCondition =
  | 'FULL_ATTENDANCE' // 满勤奖励
  | 'OVERTIME' // 加班补贴
  | 'NIGHT_SHIFT' // 夜班补贴
  | 'SPECIAL_POST' // 特殊岗位
  | 'EXCEPTIONAL'; // 特殊贡献

/**
 * 补贴发放状态枚举
 */
export type SubsidyDistributionStatus = 'PENDING' | 'DISTRIBUTING' | 'COMPLETED' | 'FAILED';

/**
 * 交易状态枚举
 */
export type TransactionStatus = 'SUCCESS' | 'FAILED' | 'PENDING' | 'CANCELLED' | 'REFUNDED';

/**
 * 报表类型枚举
 */
export type ReportType = 'SALES' | 'AREA' | 'ACCOUNT' | 'DEVICE' | 'TIME';

/**
 * 区域类型枚举
 */
export type AreaType = 'ROOT' | 'BUILDING' | 'FLOOR' | 'CANTEEN' | 'FACILITY';

// ==================== 区域相关类型 ====================

/**
 * 区域树节点
 */
export interface AreaTreeNode {
  areaId: number;
  areaName: string;
  areaType: AreaType;
  parentId: number | null;
  path: string;
  level: number;
  children?: AreaTreeNode[];
}

/**
 * 区域配置
 */
export interface AreaConfig {
  areaId: number;
  fixedValueConfig?: {
    fixedAmount: number;
    maxAmount?: number;
    requirePassword?: boolean;
  };
}

// ==================== 账户类别相关类型 ====================

/**
 * 消费模式配置
 */
export interface ModeConfig {
  [key: string]: {
    enabled: boolean;
    minAmount?: number;
    maxAmount?: number;
    requirePassword?: boolean;
    allowedCategories?: string[];
    [key: string]: any;
  };
}

/**
 * 账户类别实体
 */
export interface AccountKind {
  accountKindId: number;
  kindName: string;
  kindCode: string;
  accountType: AccountType;
  managementMode: ManagementMode;
  description?: string;
  modeConfig: ModeConfig;
  areaConfig: AreaConfig[];
  accountLevelFixed: boolean;
  status: number;
  createTime: string;
  updateTime: string;
}

/**
 * 账户类别表单
 */
export interface AccountKindForm {
  accountKindId?: number;
  kindName: string;
  kindCode: string;
  accountType: AccountType;
  managementMode: ManagementMode;
  description?: string;
  modeConfig: ModeConfig;
  areaConfig: AreaConfig[];
  accountLevelFixed: boolean;
  status?: number;
}

// ==================== 设备相关类型 ====================

/**
 * 设备状态配置
 */
export interface DeviceStatusConfig {
  text: string;
  color: 'success' | 'error' | 'default' | 'processing' | 'warning';
}

/**
 * 消费设备实体
 */
export interface ConsumeDevice {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  deviceType: DeviceType;
  areaId: number;
  areaName: string;
  ipAddress: string;
  macAddress: string;
  location: string;
  status: DeviceStatus;
  consumeModes: ConsumeMode[];
  offlineEnabled: boolean;
  offlineWhitelist: number[];
  maxOfflineCount: number;
  description?: string;
  manufacturer?: string;
  model?: string;
  serialNumber?: string;
  purchaseDate?: string;
  warrantyDate?: string;
  createTime: string;
  updateTime: string;
}

/**
 * 设备表单
 */
export interface DeviceForm {
  deviceId?: number;
  deviceCode: string;
  deviceName: string;
  deviceType: DeviceType;
  areaId: number;
  ipAddress: string;
  macAddress: string;
  location: string;
  status?: DeviceStatus;
  consumeModes: ConsumeMode[];
  offlineEnabled: boolean;
  offlineWhitelist: number[];
  maxOfflineCount: number;
  description?: string;
  manufacturer?: string;
  model?: string;
  serialNumber?: string;
  purchaseDate?: string;
  warrantyDate?: string;
}

// ==================== 补贴相关类型 ====================

/**
 * 补贴规则实体
 */
export interface Subsidy {
  subsidyId: number;
  subsidyName: string;
  subsidyType: SubsidyType;
  amount: number;
  targetGroups: string[];
  departments: number[];
  positions: number[];
  accountKinds: number[];
  distributionDay: number | null;
  autoDistribute: boolean;
  effectiveDate: string;
  expiryDate: string;
  conditions: SubsidyCondition[];
  approvalRequired: boolean;
  description?: string;
  status: number;
  createTime: string;
  updateTime: string;
}

/**
 * 补贴表单
 */
export interface SubsidyForm {
  subsidyId?: number;
  subsidyName: string;
  subsidyType: SubsidyType;
  amount: number;
  targetGroups: string[];
  departments: number[];
  positions: number[];
  accountKinds: number[];
  distributionDay?: number;
  autoDistribute: boolean;
  effectiveDate: string;
  expiryDate: string;
  conditions?: SubsidyCondition[];
  approvalRequired: boolean;
  description?: string;
  status?: number;
}

/**
 * 补贴发放记录
 */
export interface SubsidyDistribution {
  distributionId: number;
  subsidyId: number;
  subsidyName: string;
  distributionStatus: SubsidyDistributionStatus;
  targetCount: number;
  successCount: number;
  failedCount: number;
  totalAmount: number;
  distributionTime: string;
  operator: string;
  failureReason?: string;
}

// ==================== 交易相关类型 ====================

/**
 * 交易明细项
 */
export interface TransactionItem {
  name: string;
  price: number;
  quantity: number;
}

/**
 * 交易记录实体
 */
export interface Transaction {
  transactionId: number;
  transactionNo: string;
  userId: number;
  username: string;
  employeeId: string;
  accountKindId: number;
  accountKindName: string;
  deviceId: number;
  deviceName: string;
  areaId: number;
  areaName: string;
  consumeMode: ConsumeMode;
  amount: number;
  subsidyAmount: number;
  cashAmount: number;
  transactionTime: string;
  status: TransactionStatus;
  failureReason?: string;
  description?: string;
  items: TransactionItem[];
  createTime: string;
}

/**
 * 交易查询表单
 */
export interface TransactionQueryForm {
  transactionNo?: string;
  userId?: number;
  username?: string;
  employeeId?: string;
  deviceId?: number;
  areaId?: number;
  consumeMode?: ConsumeMode;
  status?: TransactionStatus;
  startTime?: string;
  endTime?: string;
  pageNum: number;
  pageSize: number;
}

// ==================== 报表相关类型 ====================

/**
 * 报表查询表单
 */
export interface ReportQueryForm {
  reportType: ReportType;
  dateRange: string[];
  areaId?: number;
  accountKindId?: number;
  deviceId?: number;
  consumeMode?: ConsumeMode;
}

/**
 * 统计汇总
 */
export interface ReportSummary {
  totalAmount: number;
  totalCount: number;
  avgAmount: number;
  subsidyAmount: number;
  cashAmount: number;
}

/**
 * 趋势数据
 */
export interface TrendData {
  dates: string[];
  counts: number[];
  amounts: number[];
}

/**
 * 区域数据
 */
export interface AreaData {
  areaName: string;
  amount: number;
  count: number;
  percent: number;
}

/**
 * 账户数据
 */
export interface AccountData {
  accountKindName: string;
  amount: number;
  count: number;
  percent: number;
}

/**
 * 设备数据
 */
export interface DeviceData {
  deviceName: string;
  amount: number;
  count: number;
  percent: number;
}

/**
 * 模式数据
 */
export interface ModeData {
  mode: ConsumeMode;
  modeName: string;
  amount: number;
  count: number;
  percent: number;
}

/**
 * 时段数据
 */
export interface TimeData {
  timeSlot: string;
  amount: number;
  count: number;
  percent: number;
}

/**
 * 完整报表数据
 */
export interface ReportData {
  summary: ReportSummary;
  trendData?: TrendData;
  areaData?: AreaData[];
  accountData?: AccountData[];
  deviceData?: DeviceData[];
  modeData?: ModeData[];
  timeData?: TimeData[];
}

// ==================== 分页相关类型 ====================

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages?: number;
}

/**
 * 分页查询表单
 */
export interface PageQueryForm {
  pageNum: number;
  pageSize: number;
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
 * 分页响应DTO
 */
export interface PageResponseDTO<T = any> extends ResponseDTO<PageResult<T>> {}

// ==================== 员工和部门类型 ====================

/**
 * 员工选项
 */
export interface EmployeeOption {
  userId: number;
  label: string;
  value: number;
  employeeId: string;
  departmentId: number;
}

/**
 * 部门树节点
 */
export interface DepartmentTreeNode {
  departmentId: number;
  departmentName: string;
  parentId: number | null;
  children?: DepartmentTreeNode[];
}

// ==================== 餐别类型 ====================

/**
 * 餐别实体
 */
export interface MealType {
  mealTypeId: number;
  mealTypeName: string;
  startTime: string;
  endTime: string;
  status: number;
}

// ==================== 工具函数类型 ====================

/**
 * 状态映射配置
 */
export interface StatusMap {
  [key: string]: {
    text: string;
    color: string;
  };
}

/**
 * 枚举映射配置
 */
export interface EnumMap {
  [key: string]: string;
}

// ==================== 全局类型声明 ====================

/**
 * Vue组件Ref类型
 */
export type ComponentRef<T> = T | null;

/**
 * ECharts实例类型
 */
export interface EChartsInstance {
  setOption: (option: any) => void;
  resize: () => void;
  dispose: () => void;
}

/**
 * 表单列配置
 */
export interface ColumnConfig {
  title: string;
  dataIndex: string;
  key?: string;
  width?: number;
  align?: 'left' | 'center' | 'right';
  fixed?: 'left' | 'right';
  customRender?: (params: { text: any; record: any; index: number }) => any;
}

/**
 * 表单规则
 */
export interface FormRule {
  required?: boolean;
  message?: string;
  trigger?: 'blur' | 'change';
  validator?: (rule: any, value: any) => Promise<void> | void;
}

/**
 * 表单规则集
 */
export interface FormRules {
  [key: string]: FormRule | FormRule[];
}

// ==================== 默认导出 ====================

export default {
  // 枚举类型
  ConsumeMode,
  ManagementMode,
  DeviceType,
  DeviceStatus,
  AccountType,
  SubsidyType,
  SubsidyCondition,
  SubsidyDistributionStatus,
  TransactionStatus,
  ReportType,
  AreaType,
};
