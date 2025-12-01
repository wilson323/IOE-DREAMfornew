/**
 * 权限管理相关类型定义
 * IOE-DREAM智慧园区一卡通管理平台
 */

// ========================================
// 基础枚举类型
// ========================================

/**
 * 安全级别枚举
 */
export enum SecurityLevel {
  TOP_SECRET = 'TOP_SECRET',    // 绝密级
  SECRET = 'SECRET',            // 机密级
  CONFIDENTIAL = 'CONFIDENTIAL', // 秘密级
  INTERNAL = 'INTERNAL',        // 内部级
  PUBLIC = 'PUBLIC'            // 公开级
}

/**
 * 权限类型枚举
 */
export enum PermissionType {
  MENU = 'MENU',                // 菜单权限
  BUTTON = 'BUTTON',            // 按钮权限
  API = 'API',                  // 接口权限
  DATA = 'DATA',                // 数据权限
  DEVICE = 'DEVICE',            // 设备权限
  AREA = 'AREA',                // 区域权限
  TIME = 'TIME'                 // 时间权限
}

/**
 * 权限状态枚举
 */
export enum PermissionStatus {
  ACTIVE = 'ACTIVE',            // 生效中
  INACTIVE = 'INACTIVE',        // 未生效
  EXPIRED = 'EXPIRED',          // 已过期
  REVOKED = 'REVOKED'           // 已撤销
}

/**
 * 权限分配类型枚举
 */
export enum PermissionAssignType {
  USER = 'USER',                // 用户权限
  ROLE = 'ROLE',                // 角色权限
  DEPARTMENT = 'DEPARTMENT',    // 部门权限
  POSITION = 'POSITION',        // 职位权限
  TEMPLATE = 'TEMPLATE'         // 模板权限
}

/**
 * 时间策略类型枚举
 */
export enum TimeStrategyType {
  ALWAYS = 'ALWAYS',            // 永久有效
  PERIOD = 'PERIOD',            // 时间段
  WORKDAY = 'WORKDAY',          // 工作日
  WEEKEND = 'WEEKEND',          // 周末
  CUSTOM = 'CUSTOM'             // 自定义
}

// ========================================
// 基础接口定义
// ========================================

/**
 * 基础权限接口
 */
export interface BasePermission {
  id: number;
  code: string;                  // 权限编码
  name: string;                  // 权限名称
  description?: string;          // 权限描述
  type: PermissionType;          // 权限类型
  securityLevel: SecurityLevel;  // 安全级别
  status: PermissionStatus;      // 权限状态
  parentId?: number;             // 父权限ID
  sort: number;                  // 排序
  createTime: string;            // 创建时间
  updateTime: string;            // 更新时间
  createUserId: number;          // 创建人ID
  updateUserId: number;          // 更新人ID
  deletedFlag: boolean;          // 删除标记
}

/**
 * 权限树节点接口
 */
export interface PermissionTreeNode extends BasePermission {
  children?: PermissionTreeNode[];
  checkable?: boolean;           // 是否可选中
  selectable?: boolean;          // 是否可选择
  disabled?: boolean;            // 是否禁用
  icon?: string;                 // 图标
  path?: string;                 // 路由路径
  component?: string;            // 组件路径
}

/**
 * 用户权限分配接口
 */
export interface UserPermissionAssign {
  id: number;
  userId: number;                // 用户ID
  userName: string;              // 用户姓名
  userCode: string;              // 用户编号
  departmentId?: number;         // 部门ID
  departmentName?: string;       // 部门名称
  positionId?: number;           // 职位ID
  positionName?: string;         // 职位名称
  permissionId: number;          // 权限ID
  permissionCode: string;        // 权限编码
  permissionName: string;        // 权限名称
  assignType: PermissionAssignType; // 分配类型
  assignTime: string;            // 分配时间
  assignUserId: number;          // 分配人ID
  assignUserName: string;        // 分配人姓名
  expireTime?: string;           // 过期时间
  status: PermissionStatus;      // 状态
  remark?: string;               // 备注
}

/**
 * 权限模板接口
 */
export interface PermissionTemplate {
  id: number;
  name: string;                  // 模板名称
  code: string;                  // 模板编码
  description?: string;          // 模板描述
  securityLevel: SecurityLevel;  // 安全级别
  status: PermissionStatus;      // 状态
  permissions: number[];         // 权限ID列表
  departmentId?: number;         // 适用部门ID
  positionId?: number;           // 适用职位ID
  createTime: string;            // 创建时间
  updateTime: string;            // 更新时间
  createUserId: number;          // 创建人ID
  updateUserId: number;          // 更新人ID
  deletedFlag: boolean;          // 删除标记
}

/**
 * 时间策略接口
 */
export interface TimeStrategy {
  id: number;
  name: string;                  // 策略名称
  type: TimeStrategyType;        // 策略类型
  config: TimeStrategyConfig;    // 策略配置
  description?: string;          // 描述
  createTime: string;            // 创建时间
  updateTime: string;            // 更新时间
  createUserId: number;          // 创建人ID
  updateUserId: number;          // 更新人ID
  deletedFlag: boolean;          // 删除标记
}

/**
 * 时间策略配置接口
 */
export interface TimeStrategyConfig {
  startDate?: string;            // 开始日期
  endDate?: string;              // 结束日期
  startTime?: string;            // 开始时间
  endTime?: string;              // 结束时间
  weekdays?: number[];           // 星期几 (0-6, 0=周日)
  workdays?: string[];           // 工作日日期
  holidays?: string[];           // 节假日日期
  customTimes?: CustomTimeRange[]; // 自定义时间段
}

/**
 * 自定义时间范围接口
 */
export interface CustomTimeRange {
  start: string;                 // 开始时间
  end: string;                   // 结束时间
  days: number[];                // 适用星期几
}

/**
 * 地理围栏权限接口
 */
export interface GeoFencePermission {
  id: number;
  name: string;                  // 围栏名称
  type: 'CIRCLE' | 'POLYGON' | 'RECTANGLE'; // 围栏类型
  coordinates: GeoCoordinate[];  // 坐标点
  center?: GeoCoordinate;        // 中心点（圆形围栏）
  radius?: number;               // 半径（圆形围栏，米）
  description?: string;          // 描述
  createTime: string;            // 创建时间
  updateTime: string;            // 更新时间
  createUserId: number;          // 创建人ID
  updateUserId: number;          // 更新人ID
  deletedFlag: boolean;          // 删除标记
}

/**
 * 地理坐标接口
 */
export interface GeoCoordinate {
  latitude: number;              // 纬度
  longitude: number;             // 经度
  address?: string;              // 地址描述
}

/**
 * 设备权限接口
 */
export interface DevicePermission {
  id: number;
  deviceId: number;              // 设备ID
  deviceName: string;            // 设备名称
  deviceType: string;            // 设备类型
  deviceCode: string;            // 设备编码
  locationId?: number;           // 位置ID
  locationName?: string;         // 位置名称
  permissionType: 'READ' | 'WRITE' | 'CONTROL' | 'ADMIN'; // 权限类型
  securityLevel: SecurityLevel;  // 安全级别
  status: PermissionStatus;      // 状态
  createTime: string;            // 创建时间
  updateTime: string;            // 更新时间
  createUserId: number;          // 创建人ID
  updateUserId: number;          // 更新人ID
  deletedFlag: boolean;          // 删除标记
}

/**
 * 权限审计日志接口
 */
export interface PermissionAuditLog {
  id: number;
  userId: number;                // 操作用户ID
  userName: string;              // 操作用户姓名
  userType: 'USER' | 'ADMIN' | 'SYSTEM'; // 用户类型
  action: 'GRANT' | 'REVOKE' | 'MODIFY' | 'VIEW'; // 操作类型
  targetType: 'USER' | 'ROLE' | 'TEMPLATE'; // 目标类型
  targetId: number;              // 目标ID
  targetName: string;            // 目标名称
  permissionId?: number;         // 权限ID
  permissionName?: string;       // 权限名称
  oldValue?: string;             // 原值
  newValue?: string;             // 新值
  reason?: string;               // 操作原因
  ip: string;                    // IP地址
  userAgent?: string;            // 用户代理
  result: 'SUCCESS' | 'FAILED';  // 操作结果
  errorMessage?: string;         // 错误信息
  createTime: string;            // 创建时间
}

// ========================================
// 请求参数类型
// ========================================

/**
 * 权限查询参数接口
 */
export interface PermissionQueryParams {
  keyword?: string;              // 关键字搜索
  type?: PermissionType;         // 权限类型
  securityLevel?: SecurityLevel; // 安全级别
  status?: PermissionStatus;     // 状态
  parentId?: number;             // 父权限ID
  page?: number;                 // 页码
  pageSize?: number;             // 每页数量
}

/**
 * 用户权限查询参数接口
 */
export interface UserPermissionQueryParams {
  userId?: number;               // 用户ID
  userName?: string;             // 用户姓名
  departmentId?: number;         // 部门ID
  positionId?: number;           // 职位ID
  permissionCode?: string;       // 权限编码
  permissionType?: PermissionType; // 权限类型
  securityLevel?: SecurityLevel; // 安全级别
  status?: PermissionStatus;     // 状态
  assignType?: PermissionAssignType; // 分配类型
  page?: number;                 // 页码
  pageSize?: number;             // 每页数量
}

/**
 * 权限分配参数接口
 */
export interface PermissionAssignParams {
  userId: number;                // 用户ID
  permissionIds: number[];       // 权限ID列表
  assignType: PermissionAssignType; // 分配类型
  expireTime?: string;           // 过期时间
  timeStrategyId?: number;       // 时间策略ID
  geoFenceIds?: number[];        // 地理围栏ID列表
  deviceIds?: number[];          // 设备ID列表
  reason?: string;               // 分配原因
}

/**
 * 权限模板创建参数接口
 */
export interface PermissionTemplateCreateParams {
  name: string;                  // 模板名称
  code: string;                  // 模板编码
  description?: string;          // 模板描述
  securityLevel: SecurityLevel;  // 安全级别
  permissions: number[];         // 权限ID列表
  departmentId?: number;         // 适用部门ID
  positionId?: number;           // 适用职位ID
}

/**
 * 权限模板应用参数接口
 */
export interface PermissionTemplateApplyParams {
  templateId: number;            // 模板ID
  userIds: number[];             // 用户ID列表
  expireTime?: string;           // 过期时间
  reason?: string;               // 应用原因
}

/**
 * 时间策略创建参数接口
 */
export interface TimeStrategyCreateParams {
  name: string;                  // 策略名称
  type: TimeStrategyType;        // 策略类型
  config: TimeStrategyConfig;    // 策略配置
  description?: string;          // 描述
}

/**
 * 地理围栏创建参数接口
 */
export interface GeoFenceCreateParams {
  name: string;                  // 围栏名称
  type: 'CIRCLE' | 'POLYGON' | 'RECTANGLE'; // 围栏类型
  coordinates: GeoCoordinate[];  // 坐标点
  center?: GeoCoordinate;        // 中心点（圆形围栏）
  radius?: number;               // 半径（圆形围栏）
  description?: string;          // 描述
}

/**
 * 权限审计查询参数接口
 */
export interface PermissionAuditQueryParams {
  userId?: number;               // 操作用户ID
  userName?: string;             // 操作用户姓名
  action?: 'GRANT' | 'REVOKE' | 'MODIFY' | 'VIEW'; // 操作类型
  targetType?: 'USER' | 'ROLE' | 'TEMPLATE'; // 目标类型
  targetId?: number;             // 目标ID
  permissionId?: number;         // 权限ID
  result?: 'SUCCESS' | 'FAILED'; // 操作结果
  startTime?: string;            // 开始时间
  endTime?: string;              // 结束时间
  page?: number;                 // 页码
  pageSize?: number;             // 每页数量
}

// ========================================
// 组件Props类型
// ========================================

/**
 * 权限树组件Props接口
 */
export interface PermissionTreeProps {
  value?: number[];              // 选中的权限ID
  checkedKeys?: number[];        // 选中的权限ID
  defaultExpandAll?: boolean;    // 默认展开所有
  checkable?: boolean;           // 显示复选框
  selectable?: boolean;          // 可选择
  showIcon?: boolean;            // 显示图标
  securityLevel?: SecurityLevel; // 限制显示的安全级别
  permissionTypes?: PermissionType[]; // 限制显示的权限类型
  height?: string | number;      // 高度
  disabled?: boolean;            // 是否禁用
  loading?: boolean;             // 加载状态
}

/**
 * 用户权限选择器Props接口
 */
export interface UserPermissionSelectorProps {
  value?: number[];              // 选中的用户ID
  multiple?: boolean;            // 多选
  departmentId?: number;         // 限制部门
  positionId?: number;           // 限制职位
  showPermission?: boolean;      // 显示权限信息
  securityLevel?: SecurityLevel; // 限制安全级别
  disabled?: boolean;            // 是否禁用
  placeholder?: string;          // 占位符
}

/**
 * 权限矩阵表格Props接口
 */
export interface PermissionMatrixProps {
  users: any[];                  // 用户列表
  permissions: PermissionTreeNode[]; // 权限列表
  loading?: boolean;             // 加载状态
  showSecurityLevel?: boolean;   // 显示安全级别
  editable?: boolean;            // 可编辑
  height?: string | number;      // 高度
}

/**
 * 权限可视化Props接口
 */
export interface PermissionVisualizerProps {
  userId: number;                // 用户ID
  type: 'TREE' | 'GRAPH' | 'MATRIX'; // 可视化类型
  showExpired?: boolean;         // 显示已过期
  showInherited?: boolean;       // 显示继承权限
  height?: string | number;      // 高度
  interactive?: boolean;         // 可交互
}

// ========================================
// 状态管理类型
// ========================================

/**
 * 权限管理状态接口
 */
export interface PermissionState {
  // 权限数据
  permissions: PermissionTreeNode[];
  permissionMap: Record<number, PermissionTreeNode>;

  // 用户权限数据
  userPermissions: UserPermissionAssign[];
  userPermissionMap: Record<string, UserPermissionAssign[]>;

  // 权限模板数据
  permissionTemplates: PermissionTemplate[];

  // 时间策略数据
  timeStrategies: TimeStrategy[];

  // 地理围栏数据
  geoFences: GeoFencePermission[];

  // 设备权限数据
  devicePermissions: DevicePermission[];

  // 当前选中的权限
  selectedPermissions: number[];

  // 当前用户
  currentUser: any;

  // 加载状态
  loading: boolean;

  // 权限统计
  statistics: PermissionStatistics;
}

/**
 * 权限统计接口
 */
export interface PermissionStatistics {
  totalPermissions: number;      // 总权限数
  userPermissionCount: number;   // 用户权限数
  rolePermissionCount: number;   // 角色权限数
  templateCount: number;         // 模板数量
  expiredCount: number;          // 过期权限数
  activeCount: number;           // 生效权限数
  securityLevelDistribution: Record<SecurityLevel, number>; // 安全级别分布
  typeDistribution: Record<PermissionType, number>; // 类型分布
  recentAssignments: UserPermissionAssign[]; // 最近分配
  auditLogs: PermissionAuditLog[]; // 审计日志
}

// ========================================
// 事件类型
// ========================================

/**
 * 权限树事件接口
 */
export interface PermissionTreeEvents {
  select?: (selectedKeys: number[], info: any) => void;
  check?: (checkedKeys: number[], info: any) => void;
  expand?: (expandedKeys: number[], info: any) => void;
  nodeClick?: (node: PermissionTreeNode) => void;
}

/**
 * 权限分配事件接口
 */
export interface PermissionAssignEvents {
  assign?: (params: PermissionAssignParams) => void;
  revoke?: (userId: number, permissionIds: number[]) => void;
  modify?: (userId: number, permissionId: number, newData: any) => void;
}

// ========================================
// 工具类型
// ========================================

/**
 * 分页响应接口
 */
export interface PageResponse<T> {
  total: number;                 // 总记录数
  page: number;                  // 当前页码
  pageSize: number;              // 每页数量
  data: T[];                     // 数据列表
}

/**
 * API响应接口
 */
export interface ApiResponse<T = any> {
  code: number;                  // 响应码
  message: string;               // 响应消息
  data: T;                       // 响应数据
  success: boolean;              // 是否成功
  timestamp: number;             // 时间戳
}

/**
 * 树节点数据接口
 */
export interface TreeNodeData {
  key: number | string;          // 节点键值
  title: string;                 // 节点标题
  children?: TreeNodeData[];     // 子节点
  isLeaf?: boolean;              // 是否叶子节点
  icon?: string;                 // 图标
  disabled?: boolean;            // 是否禁用
  selectable?: boolean;          // 是否可选择
  checkable?: boolean;           // 是否可勾选
  checked?: boolean;             // 是否选中
  expanded?: boolean;            // 是否展开
  level?: number;                // 层级
  parentKey?: number | string;   // 父节点键值
}

/**
 * 表格列配置接口
 */
export interface TableColumn {
  title: string;                 // 列标题
  dataIndex: string;             // 数据字段
  key: string;                   // 唯一标识
  width?: number | string;       // 列宽度
  align?: 'left' | 'center' | 'right'; // 对齐方式
  sorter?: boolean;              // 是否可排序
  filterable?: boolean;          // 是否可过滤
  fixed?: 'left' | 'right';      // 固定列
  render?: (text: any, record: any, index: number) => any; // 自定义渲染
}

/**
 * 表单验证规则接口
 */
export interface FormRule {
  required?: boolean;            // 是否必填
  message?: string;              // 错误消息
  pattern?: RegExp;              // 正则表达式
  min?: number;                  // 最小长度
  max?: number;                  // 最大长度
  validator?: (rule: any, value: any) => Promise<void>; // 自定义验证器
}

/**
 * 操作按钮配置接口
 */
export interface ActionButton {
  label: string;                 // 按钮文本
  type?: 'primary' | 'default' | 'danger' | 'ghost'; // 按钮类型
  icon?: string;                 // 图标
  disabled?: boolean;            // 是否禁用
  loading?: boolean;             // 加载状态
  danger?: boolean;              // 危险操作
  onClick: () => void;           // 点击事件
}