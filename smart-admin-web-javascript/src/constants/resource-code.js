/**
 * 统一资源码常量定义
 * <p>
 * 与后端ResourceCodeConst保持一致，用于前端权限控制和路由守卫
 * 支持数据域权限校验和角色权限检查
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */

/**
 * 门禁设备管理资源码
 */
export const AccessDevice = {
  VIEW: 'ACCESS_DEVICE_VIEW',
  ADD: 'ACCESS_DEVICE_ADD',
  UPDATE: 'ACCESS_DEVICE_UPDATE',
  DELETE: 'ACCESS_DEVICE_DELETE',
  CONTROL: 'ACCESS_DEVICE_CONTROL',
  CONFIG: 'ACCESS_DEVICE_CONFIG',
  MONITOR: 'ACCESS_DEVICE_MONITOR'
}

/**
 * 考勤管理资源码
 */
export const Attendance = {
  PUNCH_IN: 'ATTENDANCE_PUNCH_IN',
  PUNCH_OUT: 'ATTENDANCE_PUNCH_OUT',
  RECORD_VIEW: 'ATTENDANCE_RECORD_VIEW',
  RECORD_EXPORT: 'ATTENDANCE_RECORD_EXPORT',
  SCHEDULE_MANAGE: 'ATTENDANCE_SCHEDULE_MANAGE',
  RULE_CONFIG: 'ATTENDANCE_RULE_CONFIG',
  STATISTICS_VIEW: 'ATTENDANCE_STATISTICS_VIEW'
}

/**
 * 消费管理资源码
 */
export const Consume = {
  ACCOUNT_MANAGE: 'CONSUME_ACCOUNT_MANAGE',
  RECORD_VIEW: 'CONSUME_RECORD_VIEW',
  RECORD_EXPORT: 'CONSUME_RECORD_EXPORT',
  TERMINAL_MANAGE: 'CONSUME_TERMINAL_MANAGE',
  SETTLEMENT_MANAGE: 'CONSUME_SETTLEMENT_MANAGE',
  RECHARGE_MANAGE: 'CONSUME_RECHARGE_MANAGE'
}

/**
 * 区域管理资源码
 */
export const Area = {
  VIEW: 'AREA_VIEW',
  ADD: 'AREA_ADD',
  UPDATE: 'AREA_UPDATE',
  DELETE: 'AREA_DELETE',
  MANAGE: 'AREA_MANAGE',
  CONFIG: 'AREA_CONFIG'
}

/**
 * 人员管理资源码
 */
export const Person = {
  VIEW: 'PERSON_VIEW',
  ADD: 'PERSON_ADD',
  UPDATE: 'PERSON_UPDATE',
  DELETE: 'PERSON_DELETE',
  IMPORT: 'PERSON_IMPORT',
  EXPORT: 'PERSON_EXPORT',
  BIOMETRIC_MANAGE: 'PERSON_BIOMETRIC_MANAGE'
}

/**
 * 权限管理资源码
 */
export const Permission = {
  ROLE_MANAGE: 'PERMISSION_ROLE_MANAGE',
  USER_ROLE_ASSIGN: 'PERMISSION_USER_ROLE_ASSIGN',
  DATA_SCOPE_CONFIG: 'PERMISSION_DATA_SCOPE_CONFIG',
  RESOURCE_CONFIG: 'PERMISSION_RESOURCE_CONFIG'
}

/**
 * 系统管理资源码
 */
export const System = {
  USER_MANAGE: 'SYSTEM_USER_MANAGE',
  DEPT_MANAGE: 'SYSTEM_DEPT_MANAGE',
  DICTIONARY_MANAGE: 'SYSTEM_DICTIONARY_MANAGE',
  CONFIG_MANAGE: 'SYSTEM_CONFIG_MANAGE',
  LOG_VIEW: 'SYSTEM_LOG_VIEW',
  MONITOR_VIEW: 'SYSTEM_MONITOR_VIEW'
}

/**
 * 数据域常量
 */
export const DataScope = {
  AREA: 'AREA',    // 区域数据域
  DEPT: 'DEPT',    // 部门数据域
  SELF: 'SELF',    // 个人数据域
  CUSTOM: 'CUSTOM' // 自定义数据域
}

/**
 * 权限动作常量
 */
export const Action = {
  READ: 'READ',      // 读取权限
  WRITE: 'WRITE',    // 写入权限
  DELETE: 'DELETE',  // 删除权限
  MANAGE: 'MANAGE',  // 管理权限
  CONFIG: 'CONFIG',  // 配置权限
  EXPORT: 'EXPORT'   // 导出权限
}

/**
 * 统一导出所有资源码
 */
export const ResourceCode = {
  AccessDevice,
  Attendance,
  Consume,
  Area,
  Person,
  Permission,
  System,
  DataScope,
  Action
}

export default ResourceCode