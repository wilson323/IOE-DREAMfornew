/**
 * 统一权限常量（前端）
 * 
 * 约定：
 * - 页面/组件权限用 UPPER_SNAKE_CASE
 * - 与后端 Sa-Token 权限码对应的接口权限使用 kebab:code 形式（供调用端显式引用）
 */
export const PERMISSION_CODES = {
  // 区域
  AREA_MANAGE: 'AREA_MANAGE',
  AREA_CONFIG: 'AREA_CONFIG',
  AREA_ACCESS: 'AREA_ACCESS',

  // 设备
  DEVICE_VIEW: 'DEVICE_VIEW',
  DEVICE_CONTROL: 'DEVICE_CONTROL',
  DEVICE_CONFIG: 'DEVICE_CONFIG',

  // 考勤
  ATTENDANCE_VIEW: 'ATTENDANCE_VIEW',
  ATTENDANCE_MANAGE: 'ATTENDANCE_MANAGE',
  ATTENDANCE_EXPORT: 'ATTENDANCE_EXPORT',
  // 对应后端 Sa-Token 权限
  API_ATTENDANCE_CLOCK_IN: 'attendance:clockIn',
  API_ATTENDANCE_QUERY: 'attendance:query',
  API_ATTENDANCE_STATISTICS: 'attendance:statistics',

  // 门禁
  ACCESS_ENTER: 'ACCESS_ENTER',
  ACCESS_MANAGE: 'ACCESS_MANAGE',
  ACCESS_CONFIG: 'ACCESS_CONFIG',
  // 对应后端 Sa-Token 权限
  API_ACCESS_RECORD: 'access:record',
  API_ACCESS_PAGE: 'access:page',
  API_ACCESS_DETAIL: 'access:detail',
  API_ACCESS_VERIFY: 'access:verify',

  // 消费
  CONSUME_VIEW: 'CONSUME_VIEW',
  CONSUME_MANAGE: 'CONSUME_MANAGE',
  // 对应后端 Sa-Token 权限
  API_CONSUME_PAY: 'consume:pay',
  API_CONSUME_RECORDS: 'consume:records'
};

export default PERMISSION_CODES;

