/**
 * 消费模块 - 枚举常量定义
 *
 * 统一管理消费模块的所有枚举类型，避免在各组件中重复定义
 */

// ==================== 消费模式枚举 ====================
/**
 * 6种消费模式
 */
export const CONSUME_MODE = {
  FIXED_AMOUNT: 'FIXED_AMOUNT', // 固定金额
  FREE_AMOUNT: 'FREE_AMOUNT', // 自由金额
  METERED: 'METERED', // 计量消费
  PRODUCT: 'PRODUCT', // 商品消费
  ORDER: 'ORDER', // 订餐消费
  INTELLIGENCE: 'INTELLIGENCE', // 智能消费
};

/**
 * 消费模式映射表（中文显示）
 */
export const CONSUME_MODE_MAP = {
  [CONSUME_MODE.FIXED_AMOUNT]: '固定金额',
  [CONSUME_MODE.FREE_AMOUNT]: '自由金额',
  [CONSUME_MODE.METERED]: '计量消费',
  [CONSUME_MODE.PRODUCT]: '商品消费',
  [CONSUME_MODE.ORDER]: '订餐消费',
  [CONSUME_MODE.INTELLIGENCE]: '智能消费',
};

/**
 * 获取消费模式名称
 * @param {string} mode - 消费模式代码
 * @returns {string} 消费模式中文名称
 */
export const getConsumeModeName = (mode) => {
  return CONSUME_MODE_MAP[mode] || mode || '-';
};

// ==================== 管理模式枚举 ====================
/**
 * 管理模式（3种）
 */
export const MANAGEMENT_MODE = {
  MEAL_BASED: 1, // 餐别制
  SUPERMARKET: 2, // 超市制
  HYBRID: 3, // 混合模式
};

/**
 * 管理模式映射表（中文显示）
 */
export const MANAGEMENT_MODE_MAP = {
  [MANAGEMENT_MODE.MEAL_BASED]: '餐别制',
  [MANAGEMENT_MODE.SUPERMARKET]: '超市制',
  [MANAGEMENT_MODE.HYBRID]: '混合模式',
};

/**
 * 获取管理模式名称
 * @param {number} mode - 管理模式代码
 * @returns {string} 管理模式中文名称
 */
export const getManagementModeName = (mode) => {
  return MANAGEMENT_MODE_MAP[mode] || '-';
};

// ==================== 设备类型枚举 ====================
/**
 * 消费设备类型（4种）
 */
export const DEVICE_TYPE = {
  POS: 'POS', // POS机
  CONSUME_MACHINE: 'CONSUME_MACHINE', // 消费机
  CARD_READER: 'CARD_READER', // 读卡器
  BIOMETRIC: 'BIOMETRIC', // 生物识别设备
};

/**
 * 设备类型映射表（中文显示）
 */
export const DEVICE_TYPE_MAP = {
  [DEVICE_TYPE.POS]: 'POS机',
  [DEVICE_TYPE.CONSUME_MACHINE]: '消费机',
  [DEVICE_TYPE.CARD_READER]: '读卡器',
  [DEVICE_TYPE.BIOMETRIC]: '生物识别设备',
};

/**
 * 获取设备类型名称
 * @param {string} type - 设备类型代码
 * @returns {string} 设备类型中文名称
 */
export const getDeviceTypeName = (type) => {
  return DEVICE_TYPE_MAP[type] || type || '-';
};

// ==================== 设备状态枚举 ====================
/**
 * 设备状态（3种）
 */
export const DEVICE_STATUS = {
  ONLINE: 'ONLINE', // 在线
  OFFLINE: 'OFFLINE', // 离线
  FAULT: 'FAULT', // 故障
};

/**
 * 设备状态映射表（中文显示和颜色）
 */
export const DEVICE_STATUS_MAP = {
  [DEVICE_STATUS.ONLINE]: { text: '在线', color: 'success' },
  [DEVICE_STATUS.OFFLINE]: { text: '离线', color: 'default' },
  [DEVICE_STATUS.FAULT]: { text: '故障', color: 'error' },
};

/**
 * 获取设备状态配置
 * @param {string} status - 设备状态代码
 * @returns {object} { text: string, color: string }
 */
export const getDeviceStatusConfig = (status) => {
  return DEVICE_STATUS_MAP[status] || { text: status || '-', color: 'default' };
};

// ==================== 补贴类型枚举 ====================
/**
 * 补贴类型（3种）
 */
export const SUBSIDY_TYPE = {
  MONTHLY: 'MONTHLY', // 月度补贴
  ONE_TIME: 'ONE_TIME', // 一次性补贴
  CONDITIONAL: 'CONDITIONAL', // 条件补贴
};

/**
 * 补贴类型映射表（中文显示）
 */
export const SUBSIDY_TYPE_MAP = {
  [SUBSIDY_TYPE.MONTHLY]: '月度补贴 - 按月自动发放',
  [SUBSIDY_TYPE.ONE_TIME]: '一次性补贴 - 单次发放',
  [SUBSIDY_TYPE.CONDITIONAL]: '条件补贴 - 满足条件发放',
};

/**
 * 获取补贴类型名称
 * @param {string} type - 补贴类型代码
 * @returns {string} 补贴类型中文名称
 */
export const getSubsidyTypeName = (type) => {
  return SUBSIDY_TYPE_MAP[type] || type || '-';
};

// ==================== 补贴条件枚举 ====================
/**
 * 条件补贴条件（5种）
 */
export const SUBSIDY_CONDITION = {
  FULL_ATTENDANCE: 'FULL_ATTENDANCE', // 满勤奖励
  OVERTIME: 'OVERTIME', // 加班补贴
  NIGHT_SHIFT: 'NIGHT_SHIFT', // 夜班补贴
  SPECIAL_POST: 'SPECIAL_POST', // 特殊岗位
  EXCEPTIONAL: 'EXCEPTIONAL', // 特殊贡献
};

/**
 * 补贴条件映射表（中文显示）
 */
export const SUBSIDY_CONDITION_MAP = {
  [SUBSIDY_CONDITION.FULL_ATTENDANCE]: '满勤奖励（月度全勤）',
  [SUBSIDY_CONDITION.OVERTIME]: '加班补贴（累计加班超过阈值）',
  [SUBSIDY_CONDITION.NIGHT_SHIFT]: '夜班补贴（夜班次数达标）',
  [SUBSIDY_CONDITION.SPECIAL_POST]: '特殊岗位（特定岗位人员）',
  [SUBSIDY_CONDITION.EXCEPTIONAL]: '特殊贡献（需审批）',
};

/**
 * 获取补贴条件名称列表
 * @param {string[]} conditions - 条件代码数组
 * @returns {string} 条件中文名称数组
 */
export const getSubsidyConditionNames = (conditions) => {
  if (!conditions || !Array.isArray(conditions)) return [];
  return conditions.map((c) => SUBSIDY_CONDITION_MAP[c] || c);
};

// ==================== 补贴发放状态枚举 ====================
/**
 * 补贴发放状态（4种）
 */
export const SUBSIDY_DISTRIBUTION_STATUS = {
  PENDING: 'PENDING', // 待发放
  DISTRIBUTING: 'DISTRIBUTING', // 发放中
  COMPLETED: 'COMPLETED', // 已完成
  FAILED: 'FAILED', // 发放失败
};

/**
 * 补贴发放状态映射表（中文显示和颜色）
 */
export const SUBSIDY_DISTRIBUTION_STATUS_MAP = {
  [SUBSIDY_DISTRIBUTION_STATUS.PENDING]: { text: '待发放', color: 'default' },
  [SUBSIDY_DISTRIBUTION_STATUS.DISTRIBUTING]: { text: '发放中', color: 'processing' },
  [SUBSIDY_DISTRIBUTION_STATUS.COMPLETED]: { text: '已完成', color: 'success' },
  [SUBSIDY_DISTRIBUTION_STATUS.FAILED]: { text: '发放失败', color: 'error' },
};

/**
 * 获取补贴发放状态配置
 * @param {string} status - 发放状态代码
 * @returns {object} { text: string, color: string }
 */
export const getSubsidyDistributionStatusConfig = (status) => {
  return SUBSIDY_DISTRIBUTION_STATUS_MAP[status] || { text: status || '-', color: 'default' };
};

// ==================== 交易状态枚举 ====================
/**
 * 交易状态（5种）
 */
export const TRANSACTION_STATUS = {
  SUCCESS: 'SUCCESS', // 成功
  FAILED: 'FAILED', // 失败
  PENDING: 'PENDING', // 处理中
  CANCELLED: 'CANCELLED', // 已取消
  REFUNDED: 'REFUNDED', // 已退款
};

/**
 * 交易状态映射表（中文显示和颜色）
 */
export const TRANSACTION_STATUS_MAP = {
  [TRANSACTION_STATUS.SUCCESS]: { text: '成功', color: 'success' },
  [TRANSACTION_STATUS.FAILED]: { text: '失败', color: 'error' },
  [TRANSACTION_STATUS.PENDING]: { text: '处理中', color: 'processing' },
  [TRANSACTION_STATUS.CANCELLED]: { text: '已取消', color: 'default' },
  [TRANSACTION_STATUS.REFUNDED]: { text: '已退款', color: 'warning' },
};

/**
 * 获取交易状态配置
 * @param {string} status - 交易状态代码
 * @returns {object} { text: string, color: string }
 */
export const getTransactionStatusConfig = (status) => {
  return TRANSACTION_STATUS_MAP[status] || { text: status || '-', color: 'default' };
};

// ==================== 报表类型枚举 ====================
/**
 * 报表类型（5种）
 */
export const REPORT_TYPE = {
  SALES: 'SALES', // 销售报表
  AREA: 'AREA', // 区域报表
  ACCOUNT: 'ACCOUNT', // 账户报表
  DEVICE: 'DEVICE', // 设备报表
  TIME: 'TIME', // 时段报表
};

/**
 * 报表类型映射表（中文显示）
 */
export const REPORT_TYPE_MAP = {
  [REPORT_TYPE.SALES]: '销售报表',
  [REPORT_TYPE.AREA]: '区域报表',
  [REPORT_TYPE.ACCOUNT]: '账户报表',
  [REPORT_TYPE.DEVICE]: '设备报表',
  [REPORT_TYPE.TIME]: '时段报表',
};

/**
 * 获取报表类型名称
 * @param {string} type - 报表类型代码
 * @returns {string} 报表类型中文名称
 */
export const getReportTypeName = (type) => {
  return REPORT_TYPE_MAP[type] || type || '-';
};

// ==================== 数据导出 ====================
/**
 * 统一导出所有枚举（用于组件导入）
 */
export const consumeEnums = {
  CONSUME_MODE,
  CONSUME_MODE_MAP,
  MANAGEMENT_MODE,
  MANAGEMENT_MODE_MAP,
  DEVICE_TYPE,
  DEVICE_TYPE_MAP,
  DEVICE_STATUS,
  DEVICE_STATUS_MAP,
  SUBSIDY_TYPE,
  SUBSIDY_TYPE_MAP,
  SUBSIDY_CONDITION,
  SUBSIDY_CONDITION_MAP,
  SUBSIDY_DISTRIBUTION_STATUS,
  SUBSIDY_DISTRIBUTION_STATUS_MAP,
  TRANSACTION_STATUS,
  TRANSACTION_STATUS_MAP,
  REPORT_TYPE,
  REPORT_TYPE_MAP,
};

/**
 * 统一导出所有工具函数（用于组件导入）
 */
export const consumeEnumUtils = {
  getConsumeModeName,
  getManagementModeName,
  getDeviceTypeName,
  getDeviceStatusConfig,
  getSubsidyTypeName,
  getSubsidyConditionNames,
  getSubsidyDistributionStatusConfig,
  getTransactionStatusConfig,
  getReportTypeName,
};

export default {
  ...consumeEnums,
  ...consumeEnumUtils,
};
