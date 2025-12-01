/**
 * 权限相关常量定义
 * 基于RAC权限模型的权限常量和资源编码
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */

/**
 * 权限动作类型
 */
export const PermissionActions = {
  READ: 'READ',
  WRITE: 'WRITE',
  DELETE: 'DELETE',
  APPROVE: 'APPROVE',
  MANAGE: 'MANAGE',
  CONFIG: 'CONFIG',
  EXPORT: 'EXPORT',
  IMPORT: 'IMPORT'
}

/**
 * 数据域类型
 */
export const DataScopeTypes = {
  ALL: 'ALL',         // 全部数据
  AREA: 'AREA',       // 区域数据
  DEPT: 'DEPT',       // 部门数据
  SELF: 'SELF',       // 个人数据
  CUSTOM: 'CUSTOM'    // 自定义数据
}

/**
 * 角色编码
 */
export const RoleCodes = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  OPERATOR: 'OPERATOR',
  USER: 'USER',
  GUEST: 'GUEST'
}

/**
 * 资源编码映射
 */
export const ResourceCodes = {
  // 系统管理
  SYSTEM: {
    USER: 'system:user',
    ROLE: 'system:role',
    PERMISSION: 'system:permission',
    MENU: 'system:menu',
    DICTIONARY: 'system:dictionary',
    LOG: 'system:log',
    CONFIG: 'system:config'
  },

  // 智能设备管理
  SMART: {
    DEVICE: 'smart:device',
    DEVICE_TYPE: 'smart:device:type',
    DEVICE_STATUS: 'smart:device:status',
    DEVICE_CONFIG: 'smart:device:config',
    DEVICE_MONITOR: 'smart:device:monitor'
  },

  // 门禁管理
  ACCESS: {
    DEVICE: 'smart:access:device',
    RECORD: 'smart:access:record',
    PERMISSION: 'smart:access:permission',
    AREA: 'smart:access:area',
    SCHEDULE: 'smart:access:schedule',
    PERSON: 'smart:access:person',
    CARD: 'smart:access:card',
    BIOMETRIC: 'smart:access:biometric',
    MONITOR: 'smart:access:monitor',
    REPORT: 'smart:access:report'
  },

  // 考勤管理
  ATTENDANCE: {
    SCHEDULE: 'smart:attendance:schedule',
    RULE: 'smart:attendance:rule',
    RECORD: 'smart:attendance:record',
    EXCEPTION: 'smart:attendance:exception',
    STATISTICS: 'smart:attendance:statistics',
    REPORT: 'smart:attendance:report',
    LEAVE: 'smart:attendance:leave',
    OVERTIME: 'smart:attendance:overtime'
  },

  // 消费管理
  CONSUME: {
    TERMINAL: 'smart:consume:terminal',
    RECORD: 'smart:consume:record',
    RECHARGE: 'smart:consume:recharge',
    REFUND: 'smart:consume:refund',
    SETTLEMENT: 'smart:consume:settlement',
    REPORT: 'smart:consume:report',
    MERCHANT: 'smart:consume:merchant',
    CARD: 'smart:consume:card'
  },

  // 人员管理
  PERSON: {
    BASIC_INFO: 'smart:person:basic',
    CREDENTIAL: 'smart:person:credential',
    BIOLOGICAL: 'smart:person:biological',
    PHOTO: 'smart:person:photo',
    RELATION: 'smart:person:relation',
    IMPORT_EXPORT: 'smart:person:import_export'
  },

  // 区域管理
  AREA: {
    INFO: 'smart:area:info',
    DEVICE: 'smart:area:device',
    PERSON: 'smart:area:person',
    ACCESS: 'smart:area:access',
    HIERARCHY: 'smart:area:hierarchy'
  },

  // 权限管理
  AUTH: {
    PERMISSION: 'auth:permission',
    CHECK: 'auth:permission:check',
    BATCH_CHECK: 'auth:permission:batch-check',
    USER_PERMISSIONS: 'auth:permission:user-permissions',
    FILTER_MENUS: 'auth:permission:filter-menus',
    BUTTONS: 'auth:permission:buttons',
    DATA_SCOPE_CHECK: 'auth:permission:data-scope-check',
    DATA_SCOPE_SQL: 'auth:permission:data-scope-sql',
    REFRESH_CACHE: 'auth:permission:refresh-cache',
    STATS: 'auth:permission:stats'
  }
}

/**
 * 常用权限组合
 */
export const CommonPermissions = {
  // 系统管理员权限
  SYSTEM_ADMIN: [
    ResourceCodes.SYSTEM.USER,
    ResourceCodes.SYSTEM.ROLE,
    ResourceCodes.SYSTEM.PERMISSION,
    ResourceCodes.SYSTEM.MENU
  ],

  // 设备管理员权限
  DEVICE_ADMIN: [
    ResourceCodes.SMART.DEVICE,
    ResourceCodes.SMART.DEVICE_CONFIG,
    ResourceCodes.SMART.DEVICE_MONITOR
  ],

  // 门禁管理员权限
  ACCESS_ADMIN: [
    ResourceCodes.ACCESS.DEVICE,
    ResourceCodes.ACCESS.PERMISSION,
    ResourceCodes.ACCESS.AREA,
    ResourceCodes.ACCESS.SCHEDULE,
    ResourceCodes.ACCESS.PERSON
  ],

  // 考勤管理员权限
  ATTENDANCE_ADMIN: [
    ResourceCodes.ATTENDANCE.SCHEDULE,
    ResourceCodes.ATTENDANCE.RULE,
    ResourceCodes.ATTENDANCE.RECORD,
    ResourceCodes.ATTENDANCE.EXCEPTION,
    ResourceCodes.ATTENDANCE.STATISTICS
  ],

  // 消费管理员权限
  CONSUME_ADMIN: [
    ResourceCodes.CONSUME.TERMINAL,
    ResourceCodes.CONSUME.RECORD,
    ResourceCodes.CONSUME.RECHARGE,
    ResourceCodes.CONSUME.SETTLEMENT,
    ResourceCodes.CONSUME.REPORT
  ]
}

/**
 * 权限检查模式
 */
export const PermissionCheckModes = {
  STRICT: 'strict',    // 严格模式，无权限配置时默认拒绝
  LOOSE: 'loose',      // 宽松模式，无权限配置时默认允许
  CUSTOM: 'custom'     // 自定义模式
}

/**
 * 权限显示模式
 */
export const PermissionDisplayModes = {
  HIDE: 'hide',        // 隐藏无权限元素
  DISABLE: 'disable',  // 禁用无权限元素
  SHOW: 'show'         // 显示无权限元素
}

/**
 * 数据域级别
 */
export const DataScopeLevels = {
  LEVEL_0: 0,  // 仅个人数据
  LEVEL_1: 1,  // 本部门数据
  LEVEL_2: 2,  // 本级及下级部门数据
  LEVEL_3: 3,  // 本区域数据
  LEVEL_4: 4,  // 本区域及下级区域数据
  LEVEL_5: 5   // 全部数据
}

/**
 * 权限状态
 */
export const PermissionStatus = {
  GRANTED: 'granted',       // 已授权
  DENIED: 'denied',         // 拒绝
  PENDING: 'pending',       // 待审核
  EXPIRED: 'expired',       // 已过期
  REVOKED: 'revoked'        // 已撤销
}

/**
 * 权限错误类型
 */
export const PermissionErrorTypes = {
  NOT_FOUND: 'NOT_FOUND',           // 权限不存在
  EXPIRED: 'EXPIRED',               // 权限已过期
  INSUFFICIENT: 'INSUFFICIENT',     // 权限不足
  INVALID_SCOPE: 'INVALID_SCOPE',   // 数据域无效
  DENIED: 'DENIED',                 // 权限被拒绝
  ERROR: 'ERROR'                    // 系统错误
}

/**
 * 权限消息模板
 */
export const PermissionMessages = {
  // 通用权限消息
  LOGIN_REQUIRED: '请先登录',
  PERMISSION_DENIED: '权限不足，无法访问',
  RESOURCE_NOT_FOUND: '资源不存在或已被删除',
  OPERATION_NOT_ALLOWED: '当前操作不被允许',

  // 数据域权限消息
  DATA_SCOPE_DENIED: '您没有权限访问此数据范围',
  AREA_ACCESS_DENIED: '您没有权限访问该区域',
  DEPT_ACCESS_DENIED: '您没有权限访问该部门',
  PERSONAL_DATA_DENIED: '您没有权限访问此个人数据',

  // 角色权限消息
  ROLE_REQUIRED: '需要指定角色权限',
  ROLE_NOT_MATCH: '您的角色权限不足',

  // 功能权限消息
  READ_DENIED: '您没有查看权限',
  WRITE_DENIED: '您没有编辑权限',
  DELETE_DENIED: '您没有删除权限',
  MANAGE_DENIED: '您没有管理权限',
  CONFIG_DENIED: '您没有配置权限',
  EXPORT_DENIED: '您没有导出权限',
  IMPORT_DENIED: '您没有导入权限',

  // 会话相关消息
  SESSION_EXPIRED: '登录已过期，请重新登录',
  TOKEN_INVALID: '访问令牌无效',
  MULTI_LOGIN: '检测到多地登录，请重新确认身份'
}

/**
 * 权限配置默认值
 */
export const PermissionDefaults = {
  // 默认权限检查模式
  CHECK_MODE: PermissionCheckModes.LOOSE,

  // 默认显示模式
  DISPLAY_MODE: PermissionDisplayModes.HIDE,

  // 默认数据域
  DATA_SCOPE: DataScopeTypes.SELF,

  // 缓存超时时间（毫秒）
  CACHE_TIMEOUT: 5 * 60 * 1000,

  // 批量检查大小限制
  BATCH_CHECK_LIMIT: 50,

  // 权限检查超时时间（毫秒）
  CHECK_TIMEOUT: 10 * 1000
}

/**
 * 权限相关正则表达式
 */
export const PermissionPatterns = {
  // 权限字符串格式:resource:action[:dataScope]
  PERMISSION_STRING: /^[a-zA-Z][a-zA-Z0-9:_-]*$/,

  // 资源编码格式
  RESOURCE_CODE: /^[a-zA-Z][a-zA-Z0-9:_-]*$/,

  // 角色编码格式
  ROLE_CODE: /^[A-Z][A-Z0-9_]*$/,

  // 数据域编码格式
  DATA_SCOPE_CODE: /^[A-Z][A-Z]*$/
}

/**
 * 权限事件类型
 */
export const PermissionEvents = {
  // 权限检查事件
  PERMISSION_CHECK: 'permission:check',
  PERMISSION_GRANTED: 'permission:granted',
  PERMISSION_DENIED: 'permission:denied',

  // 缓存事件
  CACHE_CLEARED: 'permission:cache:cleared',
  CACHE_REFRESHED: 'permission:cache:refreshed',

  // 用户权限事件
  USER_PERMISSIONS_LOADED: 'user:permissions:loaded',
  USER_PERMISSIONS_UPDATED: 'user:permissions:updated',

  // 路由权限事件
  ROUTE_PERMISSION_CHECK: 'route:permission:check',
  MENU_PERMISSION_FILTER: 'menu:permission:filter'
}

/**
 * 统一资源码常量（与后端 @RequireResource 保持一致）
 * @deprecated 建议使用 ResourceCodes 替代
 */
export const PERMISSION_CODES = {
  ACCESS_VERIFY: 'access:verify',
  BIOMETRIC_VERIFY: 'biometric:verify',
  DEVICE_CONTROL: 'device:control'
};

export default {
  PermissionActions,
  DataScopeTypes,
  RoleCodes,
  ResourceCodes,
  CommonPermissions,
  PermissionCheckModes,
  PermissionDisplayModes,
  DataScopeLevels,
  PermissionStatus,
  PermissionErrorTypes,
  PermissionMessages,
  PermissionDefaults,
  PermissionPatterns,
  PermissionEvents,
  PERMISSION_CODES
}

