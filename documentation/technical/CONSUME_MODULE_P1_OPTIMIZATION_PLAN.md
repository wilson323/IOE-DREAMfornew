# 消费模块前端P1优化实施计划

**创建时间**: 2025-01-30
**优先级**: P1 (建议优化)
**预计工作量**: 3.5小时
**预期收益**: 提高可维护性，减少未来修改成本，增强类型安全

---

## 📋 优化项清单

### 1️⃣ 提取公共枚举和工具函数 (1小时)

#### 1.1 创建枚举常量文件

**目标**: 统一管理所有消费模块相关的枚举定义

**文件**: `smart-admin-web-javascript/src/constants/consume-enum.js`

```javascript
/**
 * 消费模块公共枚举定义
 * 统一管理所有消费模块相关的常量和映射关系
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-30
 */

// ==================== 消费模式枚举 ====================

/**
 * 消费模式枚举
 * 包含6种消费模式的标签、值、颜色定义
 */
export const CONSUME_MODES = {
  FIXED_AMOUNT: {
    label: '固定金额',
    value: 'FIXED_AMOUNT',
    color: 'blue',
    description: '固定金额消费模式',
  },
  FREE_AMOUNT: {
    label: '自由金额',
    value: 'FREE_AMOUNT',
    color: 'green',
    description: '自由金额消费模式',
  },
  METERED: {
    label: '计量计费',
    value: 'METERED',
    color: 'orange',
    description: '按计量计费模式',
  },
  PRODUCT: {
    label: '商品模式',
    value: 'PRODUCT',
    color: 'purple',
    description: '商品模式消费',
  },
  ORDER: {
    label: '订餐模式',
    value: 'ORDER',
    color: 'cyan',
    description: '订餐模式消费',
  },
  INTELLIGENCE: {
    label: '智能模式',
    value: 'INTELLIGENCE',
    color: 'red',
    description: '智能模式消费',
  },
};

/**
 * 获取消费模式标签
 * @param {string} mode - 消费模式值
 * @returns {string} 消费模式标签
 */
export const getConsumeModeLabel = (mode) => {
  return CONSUME_MODES[mode]?.label || mode;
};

/**
 * 获取消费模式颜色
 * @param {string} mode - 消费模式值
 * @returns {string} Ant Design颜色值
 */
export const getConsumeModeColor = (mode) => {
  return CONSUME_MODES[mode]?.color || 'default';
};

// ==================== 设备类型枚举 ====================

/**
 * 设备类型枚举
 */
export const DEVICE_TYPES = {
  POS: {
    label: 'POS机',
    value: 'POS',
    color: 'blue',
    description: 'POS机，支持多种支付方式',
  },
  CONSUME_MACHINE: {
    label: '消费机',
    value: 'CONSUME_MACHINE',
    color: 'green',
    description: '简易消费终端',
  },
  CARD_READER: {
    label: '读卡器',
    value: 'CARD_READER',
    color: 'orange',
    description: '仅读取卡片信息',
  },
  BIOMETRIC: {
    label: '生物识别设备',
    value: 'BIOMETRIC',
    color: 'purple',
    description: '人脸/指纹识别设备',
  },
};

/**
 * 获取设备类型标签
 * @param {string} type - 设备类型值
 * @returns {string} 设备类型标签
 */
export const getDeviceTypeLabel = (type) => {
  return DEVICE_TYPES[type]?.label || type;
};

/**
 * 获取设备类型颜色
 * @param {string} type - 设备类型值
 * @returns {string} Ant Design颜色值
 */
export const getDeviceTypeColor = (type) => {
  return DEVICE_TYPES[type]?.color || 'default';
};

// ==================== 设备状态枚举 ====================

/**
 * 设备状态枚举
 */
export const DEVICE_STATUS = {
  ONLINE: {
    label: '在线',
    value: 'ONLINE',
    badge: 'success',
    description: '设备在线正常运行',
  },
  OFFLINE: {
    label: '离线',
    value: 'OFFLINE',
    badge: 'default',
    description: '设备离线',
  },
  FAULT: {
    label: '故障',
    value: 'FAULT',
    badge: 'error',
    description: '设备故障',
  },
};

/**
 * 获取设备状态标签
 * @param {string} status - 设备状态值
 * @returns {string} 设备状态标签
 */
export const getDeviceStatusLabel = (status) => {
  return DEVICE_STATUS[status]?.label || status;
};

/**
 * 获取设备状态徽标
 * @param {string} status - 设备状态值
 * @returns {string} Ant Design徽标状态
 */
export const getDeviceStatusBadge = (status) => {
  return DEVICE_STATUS[status]?.badge || 'default';
};

// ==================== 补贴类型枚举 ====================

/**
 * 补贴类型枚举
 */
export const SUBSIDY_TYPES = {
  MONTHLY: {
    label: '月度补贴',
    value: 'MONTHLY',
    color: 'blue',
    description: '按月自动发放',
  },
  ONE_TIME: {
    label: '一次性补贴',
    value: 'ONE_TIME',
    color: 'green',
    description: '单次发放',
  },
  CONDITIONAL: {
    label: '条件补贴',
    value: 'CONDITIONAL',
    color: 'orange',
    description: '满足条件发放',
  },
};

/**
 * 获取补贴类型标签
 * @param {string} type - 补贴类型值
 * @returns {string} 补贴类型标签
 */
export const getSubsidyTypeLabel = (type) => {
  return SUBSIDY_TYPES[type]?.label || type;
};

/**
 * 获取补贴类型颜色
 * @param {string} type - 补贴类型值
 * @returns {string} Ant Design颜色值
 */
export const getSubsidyTypeColor = (type) => {
  return SUBSIDY_TYPES[type]?.color || 'default';
};

// ==================== 条件补贴枚举 ====================

/**
 * 条件补贴条件枚举
 */
export const SUBSIDY_CONDITIONS = {
  FULL_ATTENDANCE: {
    label: '满勤奖励',
    value: 'FULL_ATTENDANCE',
    description: '月度全勤',
  },
  OVERTIME: {
    label: '加班补贴',
    value: 'OVERTIME',
    description: '累计加班超过阈值',
  },
  NIGHT_SHIFT: {
    label: '夜班补贴',
    value: 'NIGHT_SHIFT',
    description: '夜班次数达标',
  },
  SPECIAL_POST: {
    label: '特殊岗位',
    value: 'SPECIAL_POST',
    description: '特定岗位人员',
  },
  EXCEPTIONAL: {
    label: '特殊贡献',
    value: 'EXCEPTIONAL',
    description: '需审批',
  },
};

/**
 * 获取补贴条件标签
 * @param {string} condition - 条件值
 * @returns {string} 条件标签
 */
export const getSubsidyConditionLabel = (condition) => {
  return SUBSIDY_CONDITIONS[condition]?.label || condition;
};

// ==================== 餐别枚举 ====================

/**
 * 餐别枚举
 */
export const MEAL_TYPES = {
  BREAKFAST: {
    label: '早餐',
    value: 'BREAKFAST',
    color: 'green',
  },
  LUNCH: {
    label: '午餐',
    value: 'LUNCH',
    color: 'orange',
  },
  DINNER: {
    label: '晚餐',
    value: 'DINNER',
    color: 'purple',
  },
  SNACK: {
    label: '零食',
    value: 'SNACK',
    color: 'cyan',
  },
};

/**
 * 获取餐别标签
 * @param {string} type - 餐别值
 * @returns {string} 餐别标签
 */
export const getMealTypeLabel = (type) => {
  return MEAL_TYPES[type]?.label || type;
};

// ==================== 定额模式枚举 ====================

/**
 * 定额模式枚举
 */
export const FIXED_MODE_TYPES = {
  MEAL_BASED: {
    label: '基于餐别',
    value: 'MEAL_BASED',
    description: '按餐别设置定额',
  },
  TIME_BASED: {
    label: '基于时间段',
    value: 'TIME_BASED',
    description: '按时间段设置定额',
  },
  HYBRID: {
    label: '混合模式',
    value: 'HYBRID',
    description: '餐别+时间段混合',
  },
};

/**
 * 获取定额模式标签
 * @param {string} mode - 定额模式值
 * @returns {string} 定额模式标签
 */
export const getFixedModeLabel = (mode) => {
  return FIXED_MODE_TYPES[mode]?.label || mode;
};

// ==================== 目标群体类型枚举 ====================

/**
 * 目标群体类型枚举
 */
export const TARGET_TYPES = {
  ACCOUNT_KIND: {
    label: '账户类别',
    value: 'ACCOUNT_KIND',
  },
  DEPARTMENT: {
    label: '部门',
    value: 'DEPARTMENT',
  },
  EMPLOYEE: {
    label: '员工',
    value: 'EMPLOYEE',
  },
};

/**
 * 获取目标群体类型标签
 * @param {string} type - 目标类型值
 * @returns {string} 目标类型标签
 */
export const getTargetTypeLabel = (type) => {
  return TARGET_TYPES[type]?.label || type;
};

// ==================== 导出汇总对象 ====================

/**
 * 消费模块枚举汇总
 * 方便批量导入使用
 */
export const CONSUME_ENUMS = {
  // 消费模式
  ...CONSUME_MODES,

  // 设备相关
  ...DEVICE_TYPES,
  ...DEVICE_STATUS,

  // 补贴相关
  ...SUBSIDY_TYPES,
  ...SUBSIDY_CONDITIONS,

  // 餐别
  ...MEAL_TYPES,

  // 定额模式
  ...FIXED_MODE_TYPES,

  // 目标群体
  ...TARGET_TYPES,
};
```

#### 1.2 创建格式化工具函数文件

**目标**: 统一管理所有格式化相关的工具函数

**文件**: `smart-admin-web-javascript/src/utils/format.js`

```javascript
/**
 * 格式化工具函数
 * 提供统一的格式化方法
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-30
 */

/**
 * 格式化金额
 * @param {number|string} amount - 金额值
 * @param {number} precision - 小数位数，默认2位
 * @returns {string} 格式化后的金额字符串
 * @example
 * formatAmount(1234.5) // "1234.50"
 * formatAmount(0) // "0.00"
 * formatAmount(null) // "0.00"
 */
export const formatAmount = (amount, precision = 2) => {
  if (amount === null || amount === undefined || amount === '') {
    return `0.${'0'.repeat(precision)}`;
  }
  const num = Number(amount);
  if (isNaN(num)) {
    return `0.${'0'.repeat(precision)}`;
  }
  return num.toFixed(precision);
};

/**
 * 格式化百分比
 * @param {number} value - 数值
 * @param {number} total - 总数
 * @param {number} precision - 小数位数，默认1位
 * @returns {string} 百分比字符串
 * @example
 * formatPercentage(15, 100) // "15.0%"
 */
export const formatPercentage = (value, total, precision = 1) => {
  if (!total || total === 0) {
    return `0.${'0'.repeat(precision)}%`;
  }
  const percentage = (Number(value) / Number(total)) * 100;
  return `${percentage.toFixed(precision)}%`;
};

/**
 * 格式化日期时间
 * @param {string|Date|number} datetime - 日期时间
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期时间字符串
 * @example
 * formatDateTime(new Date(), 'YYYY-MM-DD') // "2025-01-30"
 */
export const formatDateTime = (datetime, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!datetime) return '-';
  // 需要引入dayjs
  // import dayjs from 'dayjs';
  // return dayjs(datetime).format(format);
  return datetime; // 临时实现
};

/**
 * 格式化数字（千分位分隔）
 * @param {number} num - 数字
 * @returns {string} 格式化后的数字字符串
 * @example
 * formatNumber(1234567) // "1,234,567"
 */
export const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  return Number(num).toLocaleString('zh-CN');
};

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小字符串
 * @example
 * formatFileSize(1024) // "1.00 KB"
 * formatFileSize(1048576) // "1.00 MB"
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`;
};

/**
 * 格式化持续时间
 * @param {number} seconds - 秒数
 * @returns {string} 格式化后的持续时间字符串
 * @example
 * formatDuration(3661) // "1小时1分1秒"
 */
export const formatDuration = (seconds) => {
  if (!seconds || seconds < 0) return '0秒';

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  const parts = [];
  if (hours > 0) parts.push(`${hours}小时`);
  if (minutes > 0) parts.push(`${minutes}分`);
  if (secs > 0 || parts.length === 0) parts.push(`${secs}秒`);

  return parts.join('');
};

/**
 * 截断文本
 * @param {string} text - 原文本
 * @param {number} maxLength - 最大长度
 * @param {string} suffix - 后缀，默认 '...'
 * @returns {string} 截断后的文本
 * @example
 * truncateText('这是一段很长的文本', 5) // "这是一段..."
 */
export const truncateText = (text, maxLength, suffix = '...') => {
  if (!text || text.length <= maxLength) return text || '';
  return text.substring(0, maxLength) + suffix;
};

/**
 * 高亮关键词
 * @param {string} text - 原文本
 * @param {string} keyword - 关键词
 * @param {string} color - 高亮颜色，默认 '#f50'
 * @returns {string} 包含高亮标签的HTML字符串
 * @example
 * highlightKeyword('搜索关键词匹配', '关键词') // "搜索<span style="color:#f50">关键词</span>匹配"
 */
export const highlightKeyword = (text, keyword, color = '#f50') => {
  if (!text || !keyword) return text || '';
  const regex = new RegExp(`(${keyword})`, 'gi');
  return text.replace(regex, `<span style="color:${color}">$1</span>`);
};
```

#### 1.3 更新现有模块使用新的枚举和工具函数

**需要更新的文件**:
- `account-kind-list.vue`
- `subsidy-list.vue`
- `device-list.vue`
- `report/index.vue`
- `transaction/index.vue`

**更新示例** (device-list.vue):

```javascript
// 替换前
const getDeviceTypeColor = (type) => {
  const colorMap = {
    POS: 'blue',
    CONSUME_MACHINE: 'green',
    CARD_READER: 'orange',
    BIOMETRIC: 'purple',
  };
  return colorMap[type] || 'default';
};

const formatAmount = (amount) => {
  if (!amount) return '0.00';
  return Number(amount).toFixed(2);
};

// 替换后
import { getDeviceTypeColor, formatAmount } from '/@/utils/format';
import { getDeviceTypeLabel } from '/@/constants/consume-enum';

// 直接使用导入的函数，无需重复定义
```

**预计工作量**: 30分钟

---

### 2️⃣ 提取公共Mock数据 (30分钟)

#### 2.1 创建Mock数据文件

**目标**: 统一管理所有模拟数据，便于测试和调试

**文件**: `smart-admin-web-javascript/src/mock/consume-data.js`

```javascript
/**
 * 消费模块公共Mock数据
 * 统一管理所有模拟数据，便于开发和测试
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-30
 */

// ==================== 区域树数据 ====================

/**
 * 区域树形结构数据
 * 3层结构：食堂/超市 → 楼层 → 区域
 */
export const AREA_TREE = [
  {
    title: '第一食堂',
    value: '1',
    children: [
      { title: '一楼主食区', value: '1-1' },
      { title: '一楼副食区', value: '1-2' },
      { title: '二楼风味区', value: '1-3' },
    ],
  },
  {
    title: '第二食堂',
    value: '2',
    children: [
      { title: '清真餐厅', value: '2-1' },
      { title: '自助餐厅', value: '2-2' },
    ],
  },
  {
    title: '超市',
    value: '3',
    children: [
      { title: '一楼超市', value: '3-1' },
      { title: '二楼便利店', value: '3-2' },
    ],
  },
];

// ==================== 账户类别数据 ====================

/**
 * 账户类别列表
 */
export const ACCOUNT_KIND_LIST = [
  { accountKindId: 1, kindName: '员工账户' },
  { accountKindId: 2, kindName: '学生账户' },
  { accountKindId: 3, kindName: '临时账户' },
];

// ==================== 部门树数据 ====================

/**
 * 部门树形结构数据
 */
export const DEPARTMENT_TREE = [
  {
    title: '技术部',
    value: '1',
    children: [
      { title: '前端开发组', value: '1-1' },
      { title: '后端开发组', value: '1-2' },
    ],
  },
  {
    title: '市场部',
    value: '2',
    children: [
      { title: '销售组', value: '2-1' },
      { title: '推广组', value: '2-2' },
    ],
  },
  {
    title: '行政部',
    value: '3',
    children: [
      { title: '人力资源', value: '3-1' },
      { title: '财务管理', value: '3-2' },
    ],
  },
];

// ==================== 员工列表数据 ====================

/**
 * 员工选项列表（用于搜索选择）
 */
export const EMPLOYEE_OPTIONS = [
  { value: '001', label: '张三' },
  { value: '002', label: '李四' },
  { value: '003', label: '王五' },
  { value: '004', label: '赵六' },
  { value: '005', label: '孙七' },
];

// ==================== 餐别列表数据 ====================

/**
 * 餐别选项列表
 */
export const MEAL_CATEGORY_OPTIONS = [
  { categoryId: 1, categoryName: '早餐' },
  { categoryId: 2, categoryName: '午餐' },
  { categoryId: 3, categoryName: '晚餐' },
  { categoryId: 4, categoryName: '零食' },
];

// ==================== 设备Mock数据 ====================

/**
 * 设备列表Mock数据
 */
export const DEVICE_LIST = [
  {
    deviceId: 1,
    deviceCode: 'POS-001',
    deviceName: '一楼主食区POS机',
    deviceType: 'POS',
    deviceModel: 'SmartPOS-2000',
    areaId: '1-1',
    areaName: '第一食堂-一楼主食区',
    consumeMode: 'FIXED_AMOUNT',
    fixedAmount: 15,
    ipAddress: '192.168.1.101',
    port: 8080,
    location: '一楼主食区收银台',
    offlineEnabled: true,
    offlineWhitelist: [1, 2],
    offlineFixedAmount: 15,
    maxOfflineCount: 100,
    printReceipt: true,
    voicePrompt: true,
    status: 'ONLINE',
    isDefault: true,
    remark: '一楼主食区主设备',
    createTime: '2025-01-01 10:00:00',
    updateTime: '2025-01-20 15:30:00',
    todayTransactions: 156,
    todayAmount: 2340,
    offlineRecordCount: 0,
  },
  // ... 更多设备数据
];

// ==================== 账户类别Mock数据 ====================

/**
 * 账户类别列表Mock数据
 */
export const ACCOUNT_KIND_DATA = [
  {
    id: 1,
    kindName: '员工账户类别',
    kindCode: 'STAFF',
    sort: 1,
    status: 1,
    isDefault: true,
    accountCount: 1250,
    description: '默认员工账户类别',
    modeConfig: {
      FIXED_AMOUNT: { enabled: true, subType: 'SECTION' },
      FREE_AMOUNT: { enabled: false },
    },
    areaConfig: {
      allAreasAllowed: false,
      areas: [
        { areaId: 1, areaName: '第一食堂', includeSubAreas: true },
        { areaId: 2, areaName: '第二食堂', includeSubAreas: true },
      ]
    },
    accountLevelFixed: {
      enabled: true,
      mode: 'MEAL_BASED',
      mealValues: {
        BREAKFAST: 1500,
        LUNCH: 2500,
        DINNER: 2000,
        SNACK: 800
      }
    },
    mealCategoryIds: [1, 2, 3],
  },
  // ... 更多账户类别数据
];

// ==================== 补贴规则Mock数据 ====================

/**
 * 补贴规则列表Mock数据
 */
export const SUBSIDY_LIST = [
  {
    subsidyId: 1,
    subsidyName: '员工餐补',
    subsidyType: 'MONTHLY',
    amount: 300,
    distributeDay: 5,
    distributeTime: '09:00',
    targetTypes: ['ACCOUNT_KIND'],
    accountKinds: [{ kindId: 1, kindName: '员工账户' }],
    conditions: [],
    needApproval: false,
    status: 1,
    isDefault: true,
    sort: 1,
    description: '员工每月餐费补贴',
    createTime: '2025-01-01 10:00:00',
    updateTime: '2025-01-01 10:00:00',
    totalDistributed: 90000,
    distributeCount: 300,
    beneficiaryCount: 100,
  },
  // ... 更多补贴规则数据
];

// ==================== 报表统计Mock数据 ====================

/**
 * 报表统计数据
 */
export const REPORT_STATISTICS = {
  totalTransactions: 15234,
  totalAmount: 456780.50,
  activeUsers: 892,
  avgAmount: 29.98,
};

/**
 * 报表详情数据
 */
export const REPORT_DETAIL_DATA = {
  AREA: [
    { id: 1, name: '第一食堂-一楼主食区', transactions: 3456, amount: 51840, percentage: 35 },
    { id: 2, name: '第一食堂-二楼风味区', transactions: 2345, amount: 35175, percentage: 25 },
    { id: 3, name: '第二食堂-清真餐厅', transactions: 2123, amount: 31845, percentage: 20 },
    { id: 4, name: '超市-一楼超市', transactions: 1876, amount: 28140, percentage: 15 },
    { id: 5, name: '其他区域', transactions: 5434, amount: 54340, percentage: 5 },
  ],
  DEVICE: [
    { id: 1, name: '一楼主食区POS机', type: 'POS机', transactions: 3456, amount: 51840, percentage: 35 },
    { id: 2, name: '二楼消费机01', type: '消费机', transactions: 2345, amount: 35175, percentage: 25 },
    { id: 3, name: '人脸识别消费机', type: '生物识别', transactions: 2123, amount: 31845, percentage: 20 },
    { id: 4, name: '一楼超市POS', type: 'POS机', transactions: 1876, amount: 28140, percentage: 15 },
    { id: 5, name: '其他设备', type: '-', transactions: 5434, amount: 54340, percentage: 5 },
  ],
  ACCOUNT: [
    { id: 1, name: '员工账户', users: 456, transactions: 8765, amount: 131475, avgAmount: 15 },
    { id: 2, name: '学生账户', users: 321, transactions: 5432, amount: 108640, avgAmount: 20 },
    { id: 3, name: '临时账户', users: 115, transactions: 1037, amount: 20665.5, avgAmount: 19.93 },
  ],
};
```

#### 2.2 更新模块使用统一的Mock数据

**更新示例** (device-list.vue):

```javascript
// 替换前
const areaTree = ref([
  { title: '第一食堂', value: '1', children: [...] },
  // ...
]);

// 替换后
import { AREA_TREE, ACCOUNT_KIND_LIST, DEVICE_LIST } from '/@/mock/consume-data';

const areaTree = ref(AREA_TREE);
const accountKindList = ref(ACCOUNT_KIND_LIST);
const tableData = ref(DEVICE_LIST);
```

**预计工作量**: 15分钟

---

### 3️⃣ 添加TypeScript类型定义 (2小时)

#### 3.1 创建类型定义文件

**目标**: 增强类型安全，减少运行时错误

**文件**: `smart-admin-web-javascript/src/types/consume.d.ts`

```typescript
/**
 * 消费模块TypeScript类型定义
 * 提供完整的类型支持
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-01-30
 */

// ==================== 基础类型 ====================

/**
 * 分页参数
 */
export interface PageParams {
  pageNum: number;
  pageSize: number;
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

/**
 * API响应
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// ==================== 消费模式类型 ====================

/**
 * 消费模式枚举
 */
export type ConsumeMode =
  | 'FIXED_AMOUNT'    // 固定金额
  | 'FREE_AMOUNT'     // 自由金额
  | 'METERED'          // 计量计费
  | 'PRODUCT'          // 商品模式
  | 'ORDER'            // 订餐模式
  | 'INTELLIGENCE';    // 智能模式

/**
 * 消费模式配置
 */
export interface ConsumeModeConfig {
  enabled: boolean;
  [key: string]: any;
}

/**
 * 固定金额配置
 */
export interface FixedAmountConfig extends ConsumeModeConfig {
  enabled: boolean;
  subType: 'SECTION' | 'UNIFORM';
  amount: number;
  keyValues?: Array<{ key: string; value: number }>;
  values?: number[];
}

/**
 * 自由金额配置
 */
export interface FreeAmountConfig extends ConsumeModeConfig {
  enabled: boolean;
  maxAmount: number;
  dailyLimit: number;
}

/**
 * 计量计费配置
 */
export interface MeteredConfig extends ConsumeModeConfig {
  enabled: boolean;
  subType: 'TIMING' | 'QUANTITY';
  unitPrice: number;
  precision: number;
}

/**
 * 商品模式配置
 */
export interface ProductConfig extends ConsumeModeConfig {
  enabled: boolean;
  allowOverdraw: boolean;
  overdrawLimit: number;
  requireQuantity: boolean;
}

/**
 * 订餐模式配置
 */
export interface OrderConfig extends ConsumeModeConfig {
  enabled: boolean;
  orderDeadline: number;
  allowCancel: boolean;
  cancelDeadline: number;
}

/**
 * 智能模式配置
 */
export interface IntelligenceConfig extends ConsumeModeConfig {
  enabled: boolean;
  ruleType: 'TIME_BASED' | 'LOCATION_BASED' | 'USER_BEHAVIOR';
  ruleConfig: string;
}

/**
 * 完整消费模式配置
 */
export interface ModeConfig {
  FIXED_AMOUNT?: FixedAmountConfig;
  FREE_AMOUNT?: FreeAmountConfig;
  METERED?: MeteredConfig;
  PRODUCT?: ProductConfig;
  ORDER?: OrderConfig;
  INTELLIGENCE?: IntelligenceConfig;
}

// ==================== 区域相关类型 ====================

/**
 * 区域节点
 */
export interface AreaNode {
  title: string;
  value: string;
  children?: AreaNode[];
}

/**
 * 区域配置
 */
export interface AreaConfig {
  allAreasAllowed: boolean;
  areas: Array<{
    areaId: number;
    areaName: string;
    includeSubAreas: boolean;
  }>;
}

// ==================== 账户类别类型 ====================

/**
 * 账户类别
 */
export interface AccountKind {
  accountKindId: number;
  kindName: string;
  kindCode: string;
  sort: number;
  description?: string;
  status: number;
  isDefault: boolean;
  accountCount?: number;
  modeConfig?: ModeConfig;
  areaConfig?: AreaConfig;
  accountLevelFixed?: AccountLevelFixed;
  mealCategoryIds?: number[];
  createTime?: string;
  updateTime?: string;
}

/**
 * 定额模式
 */
export type FixedModeType = 'MEAL_BASED' | 'TIME_BASED' | 'HYBRID';

/**
 * 账户级别定额配置
 */
export interface AccountLevelFixed {
  enabled: boolean;
  mode: FixedModeType;
  mealValues?: {
    BREAKFAST: number;
    LUNCH: number;
    DINNER: number;
    SNACK: number;
  };
  timeSlots?: Array<{
    startTime: string;
    endTime: string;
    value: number;
  }>;
  overMode: 'FORBID' | 'ALLOW' | 'PROMPT';
  overdrawLimit: number;
  showHint: boolean;
}

// ==================== 设备相关类型 ====================

/**
 * 设备类型
 */
export type DeviceType = 'POS' | 'CONSUME_MACHINE' | 'CARD_READER' | 'BIOMETRIC';

/**
 * 设备状态
 */
export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'FAULT';

/**
 * 设备实体
 */
export interface Device {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  deviceType: DeviceType;
  deviceModel?: string;
  areaId?: string;
  areaName?: string;
  consumeMode?: ConsumeMode;
  fixedAmount?: number;
  ipAddress?: string;
  port?: number;
  location?: string;
  offlineEnabled?: boolean;
  offlineWhitelist?: number[];
  offlineFixedAmount?: number;
  maxOfflineCount?: number;
  printReceipt?: boolean;
  voicePrompt?: boolean;
  status: DeviceStatus;
  isDefault?: boolean;
  remark?: string;
  createTime?: string;
  updateTime?: string;
  todayTransactions?: number;
  todayAmount?: number;
  offlineRecordCount?: number;
}

// ==================== 补贴相关类型 ====================

/**
 * 补贴类型
 */
export type SubsidyType = 'MONTHLY' | 'ONE_TIME' | 'CONDITIONAL';

/**
 * 补贴条件
 */
export type SubsidyCondition =
  | 'FULL_ATTENDANCE'
  | 'OVERTIME'
  | 'NIGHT_SHIFT'
  | 'SPECIAL_POST'
  | 'EXCEPTIONAL';

/**
 * 目标群体类型
 */
export type TargetType = 'ACCOUNT_KIND' | 'DEPARTMENT' | 'EMPLOYEE';

/**
 * 补贴规则
 */
export interface Subsidy {
  subsidyId: number;
  subsidyName: string;
  subsidyType: SubsidyType;
  amount: number;
  distributeDay?: number;
  distributeTime?: string;
  firstDistributeDate?: string;
  targetTypes: TargetType[];
  accountKindIds?: number[];
  accountKinds?: Array<{ kindId: number; kindName: string }>;
  departmentIds?: string[];
  departmentCount?: number;
  employeeIds?: string[];
  employeeCount?: number;
  conditions?: SubsidyCondition[];
  overtimeThreshold?: number;
  nightShiftThreshold?: number;
  needApproval: boolean;
  approvalFlowId?: string;
  approvalRemark?: string;
  status: number;
  isDefault: boolean;
  sort: number;
  description?: string;
  createTime?: string;
  updateTime?: string;
  totalDistributed?: number;
  distributeCount?: number;
  beneficiaryCount?: number;
  estimatedCount?: number;
}

// ==================== 交易记录类型 ====================

/**
 * 交易状态
 */
export type TransactionStatus = 'SUCCESS' | 'FAILED' | 'PENDING' | 'REFUND';

/**
 * 交易记录
 */
export interface Transaction {
  transactionNo: string;
  userId?: number;
  userName?: string;
  amount: number;
  consumeMode: ConsumeMode;
  status: TransactionStatus;
  transactionTime: string;
  deviceId?: number;
  deviceName?: string;
  deviceType?: DeviceType;
  areaName?: string;
  areaManageMode?: number;
  mealType?: string;
  productName?: string;
  quantity?: number;
  remark?: string;
  subsidyBalance?: number;
  cashBalance?: number;
  subsidyDeducted?: number;
  cashDeducted?: number;
  sagaStatus?: string;
  sagaSteps?: SagaStep[];
}

/**
 * SAGA步骤
 */
export interface SagaStep {
  step: string;
  status: 'COMPLETED' | 'FAILED' | 'COMPENSATION';
  message: string;
  timestamp: string;
}

// ==================== 报表统计类型 ====================

/**
 * 报表类型
 */
export type ReportType = 'SALES' | 'AREA' | 'ACCOUNT' | 'DEVICE' | 'TIME';

/**
 * 统计概览
 */
export interface Statistics {
  totalTransactions: number;
  totalAmount: number;
  activeUsers: number;
  avgAmount: number;
}

/**
 * 详情数据项
 */
export interface DetailItem {
  id: number;
  name: string;
  transactions?: number;
  amount?: number;
  percentage?: number;
  type?: string;
  users?: number;
  avgAmount?: number;
}

// ==================== 表单相关类型 ====================

/**
 * 查询表单
 */
export interface QueryForm {
  pageNum?: number;
  pageSize?: number;
  [key: string]: any;
}

/**
 * 表单验证规则
 */
export type FormRule = {
  required?: boolean;
  message?: string;
  trigger?: 'blur' | 'change';
  validator?: (rule: any, value: any) => Promise<void> | void;
  [key: string]: any;
}

/**
 * 表单规则集合
 */
export type FormRules = {
  [fieldName: string]: FormRule[];
}
```

#### 3.2 在组件中使用类型定义

**更新示例** (添加到script setup):

```vue
<script setup lang="ts">
import type { Device, DeviceType, DeviceStatus } from '/@/types/consume';

// 使用类型注解
const tableData = ref<Device[]>([]);
const selectedDeviceType = ref<DeviceType | null>(null);
const selectedDeviceStatus = ref<DeviceStatus | null>(null);

// 函数返回类型
const getDeviceTypeLabel = (type: DeviceType): string => {
  return DEVICE_TYPES[type]?.label || type;
};
</script>
```

**预计工作量**: 2小时

---

## 📊 优化效果预估

### 优化前后对比

| 指标 | 优化前 | 优化后 | 改进 |
|------|--------|--------|------|
| 代码重复行数 | ~200行 | 0行 | -100% |
| 枚举定义文件 | 0个 | 1个 | +1个 |
| 工具函数文件 | 0个 | 1个 | +1个 |
| Mock数据文件 | 0个 | 1个 | +1个 |
| TypeScript类型 | 无 | 完整 | +类型安全 |
| 可维护性评分 | 85/100 | 95/100 | +12% |
| 新人上手难度 | 中等 | 简单 | -40% |

### 后续维护成本降低

1. **添加新的消费模式**: 只需在一个文件中更新 (`consume-enum.js`)
2. **修改格式化逻辑**: 只需修改 `format.js` 中的函数
3. **更新Mock数据**: 统一在 `consume-data.js` 中管理
4. **类型检查**: TypeScript会在编译时发现类型错误

---

## ✅ 实施步骤

### 第一步: 创建枚举和工具函数文件 (1小时)

1. 创建 `/src/constants/consume-enum.js`
2. 创建 `/src/utils/format.js`
3. 验证函数正确性

### 第二步: 创建Mock数据文件 (30分钟)

1. 创建 `/src/mock/consume-data.js`
2. 整理所有模拟数据
3. 验证数据结构正确性

### 第三步: 更新现有模块 (30分钟)

1. 更新 `account-kind-list.vue`
2. 更新 `subsidy-list.vue`
3. 更新 `device-list.vue`
4. 更新 `report/index.vue`
5. 更新 `transaction/index.vue`

### 第四步: 添加TypeScript类型定义 (2小时)

1. 创建 `/src/types/consume.d.ts`
2. 逐步为组件添加类型注解
3. 配置tsconfig.json（如果需要）
4. 验证类型检查正确性

### 第五步: 测试验证 (30分钟)

1. 功能测试 - 确保所有功能正常
2. 样式测试 - 确保UI没有变化
3. 类型测试 - 验证TypeScript类型检查

---

## 📝 注意事项

### 1. 向后兼容

- 所有提取的函数和枚举都使用export导出
- 现有模块可以逐步迁移，不必一次性全部更新
- Mock数据使用统一的接口，便于后续替换为真实API

### 2. 渐进式迁移

建议按以下顺序逐步迁移：

1. **第一阶段**: 创建新文件，不影响现有代码
2. **第二阶段**: 新功能和修改优先使用新的工具函数
3. **第三阶段**: 逐步更新现有代码（低优先级，可做可不做）

### 3. 团队协作

- 将本计划分享给团队成员
- 在代码审查时检查是否使用了新的工具函数
- 在开发文档中更新相关说明

---

**文档版本**: v1.0
**创建人**: IOE-DREAM Team
**审核状态**: 待审核
**预计完成时间**: 3.5小时
