/**
 * 消费模块 - 公共Mock数据
 *
 * 统一管理消费模块的所有Mock测试数据，避免在各组件中重复定义
 */

// ==================== 区域树数据 ====================
/**
 * 区域树结构（完整层级）
 */
export const AREA_TREE = [
  {
    areaId: 1,
    areaName: '总部园区',
    areaType: 'ROOT',
    parentId: null,
    path: '/1',
    level: 1,
    children: [
      {
        areaId: 11,
        areaName: 'A栋办公楼',
        areaType: 'BUILDING',
        parentId: 1,
        path: '/1/11',
        level: 2,
        children: [
          {
            areaId: 111,
            areaName: '1楼大厅',
            areaType: 'FLOOR',
            parentId: 11,
            path: '/1/11/111',
            level: 3,
            children: []
          },
          {
            areaId: 112,
            areaName: '2楼办公区',
            areaType: 'FLOOR',
            parentId: 11,
            path: '/1/11/112',
            level: 3,
            children: []
          },
          {
            areaId: 113,
            areaName: '3楼会议室',
            areaType: 'FLOOR',
            parentId: 11,
            path: '/1/11/113',
            level: 3,
            children: []
          }
        ]
      },
      {
        areaId: 12,
        areaName: 'B栋生产楼',
        areaType: 'BUILDING',
        parentId: 1,
        path: '/1/12',
        level: 2,
        children: [
          {
            areaId: 121,
            areaName: '1楼车间',
            areaType: 'FLOOR',
            parentId: 12,
            path: '/1/12/121',
            level: 3,
            children: []
          },
          {
            areaId: 122,
            areaName: '2楼仓库',
            areaType: 'FLOOR',
            parentId: 12,
            path: '/1/12/122',
            level: 3,
            children: []
          }
        ]
      },
      {
        areaId: 13,
        areaName: 'C栋综合楼',
        areaType: 'BUILDING',
        parentId: 1,
        path: '/1/13',
        level: 2,
        children: [
          {
            areaId: 131,
            areaName: '餐厅',
            areaType: 'CANTEEN',
            parentId: 13,
            path: '/1/13/131',
            level: 3,
            children: []
          },
          {
            areaId: 132,
            areaName: '健身房',
            areaType: 'FACILITY',
            parentId: 13,
            path: '/1/13/132',
            level: 3,
            children: []
          }
        ]
      }
    ]
  }
];

// ==================== 账户类别数据 ====================
/**
 * 账户类别列表
 */
export const ACCOUNT_KIND_LIST = [
  {
    accountKindId: 1,
    kindName: '员工账户',
    kindCode: 'EMPLOYEE',
    accountType: 'CASH',
    managementMode: 1,
    description: '正式员工消费账户',
    modeConfig: {
      FIXED_AMOUNT: { enabled: true, minAmount: 1, maxAmount: 100 },
      FREE_AMOUNT: { enabled: true, maxAmount: 200, requirePassword: false },
      METERED: { enabled: false },
      PRODUCT: { enabled: true },
      ORDER: { enabled: false },
      INTELLIGENCE: { enabled: false }
    },
    areaConfig: [
      { areaId: 131, fixedValueConfig: { fixedAmount: 15 } }
    ],
    accountLevelFixed: false,
    status: 1,
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  },
  {
    accountKindId: 2,
    kindName: '访客账户',
    kindCode: 'VISITOR',
    accountType: 'CASH',
    managementMode: 2,
    description: '访客临时消费账户',
    modeConfig: {
      FIXED_AMOUNT: { enabled: true, minAmount: 5, maxAmount: 50 },
      FREE_AMOUNT: { enabled: false },
      METERED: { enabled: false },
      PRODUCT: { enabled: false },
      ORDER: { enabled: false },
      INTELLIGENCE: { enabled: false }
    },
    areaConfig: [],
    accountLevelFixed: false,
    status: 1,
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  },
  {
    accountKindId: 3,
    kindName: '补贴账户',
    kindCode: 'SUBSIDY',
    accountType: 'SUBSIDY',
    managementMode: 1,
    description: '员工补贴账户，仅限指定消费',
    modeConfig: {
      FIXED_AMOUNT: { enabled: true, minAmount: 1, maxAmount: 30 },
      FREE_AMOUNT: { enabled: false },
      METERED: { enabled: false },
      PRODUCT: { enabled: true, allowedCategories: ['餐品', '饮料'] },
      ORDER: { enabled: true },
      INTELLIGENCE: { enabled: false }
    },
    areaConfig: [
      { areaId: 131, fixedValueConfig: { fixedAmount: 20 } }
    ],
    accountLevelFixed: true,
    status: 1,
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  },
  {
    accountKindId: 4,
    kindName: '管理员账户',
    kindCode: 'ADMIN',
    accountType: 'CASH',
    managementMode: 3,
    description: '管理员账户，支持所有消费模式',
    modeConfig: {
      FIXED_AMOUNT: { enabled: true, minAmount: 0, maxAmount: 1000 },
      FREE_AMOUNT: { enabled: true, maxAmount: 1000, requirePassword: true },
      METERED: { enabled: true },
      PRODUCT: { enabled: true },
      ORDER: { enabled: true },
      INTELLIGENCE: { enabled: true }
    },
    areaConfig: [],
    accountLevelFixed: false,
    status: 1,
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  }
];

// ==================== 设备数据 ====================
/**
 * 消费设备列表
 */
export const DEVICE_LIST = [
  {
    deviceId: 1,
    deviceCode: 'POS001',
    deviceName: '餐厅POS机1号',
    deviceType: 'POS',
    areaId: 131,
    areaName: '餐厅',
    ipAddress: '192.168.1.101',
    macAddress: '00:1A:2B:3C:4D:5E',
    location: '餐厅入口',
    status: 'ONLINE',
    consumeModes: ['FIXED_AMOUNT', 'FREE_AMOUNT', 'PRODUCT'],
    offlineEnabled: true,
    offlineWhitelist: [1, 3],
    maxOfflineCount: 100,
    description: '餐厅主POS机',
    manufacturer: '某厂商',
    model: 'POS-2000',
    serialNumber: 'SN20240101001',
    purchaseDate: '2024-01-01',
    warrantyDate: '2025-01-01',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  },
  {
    deviceId: 2,
    deviceCode: 'POS002',
    deviceName: '餐厅POS机2号',
    deviceType: 'POS',
    areaId: 131,
    areaName: '餐厅',
    ipAddress: '192.168.1.102',
    macAddress: '00:1A:2B:3C:4D:5F',
    location: '餐厅出口',
    status: 'ONLINE',
    consumeModes: ['FIXED_AMOUNT', 'PRODUCT', 'ORDER'],
    offlineEnabled: false,
    offlineWhitelist: [],
    maxOfflineCount: 0,
    description: '餐厅副POS机',
    manufacturer: '某厂商',
    model: 'POS-2000',
    serialNumber: 'SN20240101002',
    purchaseDate: '2024-01-01',
    warrantyDate: '2025-01-01',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  },
  {
    deviceId: 3,
    deviceCode: 'CM001',
    deviceName: '1楼消费机',
    deviceType: 'CONSUME_MACHINE',
    areaId: 111,
    areaName: '1楼大厅',
    ipAddress: '192.168.1.103',
    macAddress: '00:1A:2B:3C:4D:60',
    location: 'A栋1楼电梯口',
    status: 'ONLINE',
    consumeModes: ['FIXED_AMOUNT'],
    offlineEnabled: true,
    offlineWhitelist: [1],
    maxOfflineCount: 50,
    description: '1楼自助消费机',
    manufacturer: '某厂商',
    model: 'CM-100',
    serialNumber: 'SN20240101003',
    purchaseDate: '2024-01-01',
    warrantyDate: '2025-01-01',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  },
  {
    deviceId: 4,
    deviceCode: 'CR001',
    deviceName: '生物识别读卡器',
    deviceType: 'BIOMETRIC',
    areaId: 112,
    areaName: '2楼办公区',
    ipAddress: '192.168.1.104',
    macAddress: '00:1A:2B:3C:4D:61',
    location: '2楼办公区入口',
    status: 'OFFLINE',
    consumeModes: ['FIXED_AMOUNT', 'FREE_AMOUNT'],
    offlineEnabled: false,
    offlineWhitelist: [],
    maxOfflineCount: 0,
    description: '人脸识别消费终端',
    manufacturer: '某厂商',
    model: 'FACE-300',
    serialNumber: 'SN20240101004',
    purchaseDate: '2024-01-01',
    warrantyDate: '2025-01-01',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  }
];

// ==================== 补贴数据 ====================
/**
 * 补贴规则列表
 */
export const SUBSIDY_LIST = [
  {
    subsidyId: 1,
    subsidyName: '月度工作餐补贴',
    subsidyType: 'MONTHLY',
    amount: 300,
    targetGroups: ['ALL_EMPLOYEES'],
    departments: [],
    positions: [],
    accountKinds: [3],
    distributionDay: 5,
    autoDistribute: true,
    effectiveDate: '2024-01-01',
    expiryDate: '2024-12-31',
    conditions: [],
    approvalRequired: false,
    description: '每月5号自动发放工作餐补贴',
    status: 1,
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  },
  {
    subsidyId: 2,
    subsidyName: '夜班补贴',
    subsidyType: 'CONDITIONAL',
    amount: 150,
    targetGroups: [],
    departments: [2],
    positions: [],
    accountKinds: [3],
    distributionDay: null,
    autoDistribute: false,
    effectiveDate: '2024-01-01',
    expiryDate: '2024-12-31',
    conditions: ['NIGHT_SHIFT'],
    approvalRequired: true,
    description: '夜班次数超过10次/月自动发放',
    status: 1,
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  },
  {
    subsidyId: 3,
    subsidyName: '满勤奖励',
    subsidyType: 'CONDITIONAL',
    amount: 200,
    targetGroups: [],
    departments: [],
    positions: [],
    accountKinds: [3],
    distributionDay: null,
    autoDistribute: false,
    effectiveDate: '2024-01-01',
    expiryDate: '2024-12-31',
    conditions: ['FULL_ATTENDANCE'],
    approvalRequired: false,
    description: '月度全勤自动发放奖励',
    status: 1,
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  },
  {
    subsidyId: 4,
    subsidyName: '特殊贡献奖励',
    subsidyType: 'ONE_TIME',
    amount: 500,
    targetGroups: ['SPECIFIC_EMPLOYEES'],
    departments: [],
    positions: [],
    accountKinds: [3],
    distributionDay: null,
    autoDistribute: false,
    effectiveDate: '2024-01-01',
    expiryDate: '2024-12-31',
    conditions: ['EXCEPTIONAL'],
    approvalRequired: true,
    description: '特殊贡献一次性奖励，需审批',
    status: 1,
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00'
  }
];

// ==================== 交易数据 ====================
/**
 * 交易记录列表
 */
export const TRANSACTION_LIST = [
  {
    transactionId: 1,
    transactionNo: 'TXN202401150001',
    userId: 1001,
    username: '张三',
    employeeId: 'EMP001',
    accountKindId: 1,
    accountKindName: '员工账户',
    deviceId: 1,
    deviceName: '餐厅POS机1号',
    areaId: 131,
    areaName: '餐厅',
    consumeMode: 'PRODUCT',
    amount: 25.50,
    subsidyAmount: 10.00,
    cashAmount: 15.50,
    transactionTime: '2024-01-15 12:30:25',
    status: 'SUCCESS',
    failureReason: null,
    description: '午餐消费',
    items: [
      { name: '宫保鸡丁', price: 12.00, quantity: 1 },
      { name: '米饭', price: 2.00, quantity: 1 },
      { name: '紫菜蛋花汤', price: 5.00, quantity: 1 },
      { name: '餐盒费', price: 1.50, quantity: 1 }
    ],
    createTime: '2024-01-15 12:30:25'
  },
  {
    transactionId: 2,
    transactionNo: 'TXN202401150002',
    userId: 1002,
    username: '李四',
    employeeId: 'EMP002',
    accountKindId: 1,
    accountKindName: '员工账户',
    deviceId: 1,
    deviceName: '餐厅POS机1号',
    areaId: 131,
    areaName: '餐厅',
    consumeMode: 'FIXED_AMOUNT',
    amount: 15.00,
    subsidyAmount: 5.00,
    cashAmount: 10.00,
    transactionTime: '2024-01-15 12:28:10',
    status: 'SUCCESS',
    failureReason: null,
    description: '固定金额消费',
    items: [],
    createTime: '2024-01-15 12:28:10'
  },
  {
    transactionId: 3,
    transactionNo: 'TXN202401150003',
    userId: 1003,
    username: '王五',
    employeeId: 'EMP003',
    accountKindId: 1,
    accountKindName: '员工账户',
    deviceId: 3,
    deviceName: '1楼消费机',
    areaId: 111,
    areaName: '1楼大厅',
    consumeMode: 'FIXED_AMOUNT',
    amount: 20.00,
    subsidyAmount: 0,
    cashAmount: 20.00,
    transactionTime: '2024-01-15 09:15:30',
    status: 'SUCCESS',
    failureReason: null,
    description: '早餐消费',
    items: [],
    createTime: '2024-01-15 09:15:30'
  },
  {
    transactionId: 4,
    transactionNo: 'TXN202401150004',
    userId: 1004,
    username: '赵六',
    employeeId: 'EMP004',
    accountKindId: 1,
    accountKindName: '员工账户',
    deviceId: 4,
    deviceName: '生物识别读卡器',
    areaId: 112,
    areaName: '2楼办公区',
    consumeMode: 'FREE_AMOUNT',
    amount: 35.80,
    subsidyAmount: 15.00,
    cashAmount: 20.80,
    transactionTime: '2024-01-15 14:45:20',
    status: 'SUCCESS',
    failureReason: null,
    description: '自由金额消费',
    items: [],
    createTime: '2024-01-15 14:45:20'
  }
];

// ==================== 报表数据 ====================
/**
 * 销售报表统计数据
 */
export const SALES_REPORT_DATA = {
  summary: {
    totalAmount: 70280,
    totalCount: 2334,
    avgAmount: 30.12,
    subsidyAmount: 28112,
    cashAmount: 42168
  },
  trendData: {
    dates: ['01-15', '01-16', '01-17', '01-18', '01-19', '01-20', '01-21'],
    counts: [2100, 2300, 1950, 2450, 2200, 2600, 2334],
    amounts: [63000, 69000, 58500, 73500, 66000, 78000, 70280]
  },
  areaData: [
    { areaName: '餐厅', amount: 45000, count: 1500, percent: 64.1 },
    { areaName: '1楼大厅', amount: 15280, count: 534, percent: 21.7 },
    { areaName: '2楼办公区', amount: 10000, count: 300, percent: 14.2 }
  ],
  accountData: [
    { accountKindName: '员工账户', amount: 55000, count: 1834, percent: 78.3 },
    { accountKindName: '补贴账户', amount: 15280, count: 500, percent: 21.7 }
  ],
  deviceData: [
    { deviceName: '餐厅POS机1号', amount: 28000, count: 934, percent: 39.8 },
    { deviceName: '餐厅POS机2号', amount: 17000, count: 566, percent: 24.2 },
    { deviceName: '1楼消费机', amount: 15280, count: 534, percent: 21.7 },
    { deviceName: '生物识别读卡器', amount: 10000, count: 300, percent: 14.2 }
  ],
  modeData: [
    { mode: 'PRODUCT', modeName: '商品消费', amount: 35000, count: 1200, percent: 49.8 },
    { mode: 'FIXED_AMOUNT', modeName: '固定金额', amount: 25280, count: 934, percent: 36.0 },
    { mode: 'FREE_AMOUNT', modeName: '自由金额', amount: 10000, count: 200, percent: 14.2 }
  ],
  timeData: [
    { timeSlot: '早餐', amount: 12000, count: 450, percent: 17.1 },
    { timeSlot: '午餐', amount: 38000, count: 1384, percent: 54.1 },
    { timeSlot: '晚餐', amount: 18280, count: 450, percent: 26.0 },
    { timeSlot: '夜宵', amount: 2000, count: 50, percent: 2.8 }
  ]
};

// ==================== 部门树数据 ====================
/**
 * 部门树结构
 */
export const DEPARTMENT_TREE = [
  {
    departmentId: 1,
    departmentName: 'IOE-DREAM公司',
    parentId: null,
    children: [
      {
        departmentId: 11,
        departmentName: '研发部',
        parentId: 1,
        children: [
          { departmentId: 111, departmentName: '前端组', parentId: 11 },
          { departmentId: 112, departmentName: '后端组', parentId: 11 },
          { departmentId: 113, departmentName: '测试组', parentId: 11 }
        ]
      },
      {
        departmentId: 12,
        departmentName: '市场部',
        parentId: 1,
        children: [
          { departmentId: 121, departmentName: '销售组', parentId: 12 },
          { departmentId: 122, departmentName: '推广组', parentId: 12 }
        ]
      },
      {
        departmentId: 13,
        departmentName: '行政部',
        parentId: 1,
        children: []
      },
      {
        departmentId: 14,
        departmentName: '人事部',
        parentId: 1,
        children: []
      }
    ]
  }
];

// ==================== 员工数据 ====================
/**
 * 员工选项列表（用于下拉选择）
 */
export const EMPLOYEE_OPTIONS = [
  { userId: 1001, label: '张三 (EMP001)', value: 1001, employeeId: 'EMP001', departmentId: 111 },
  { userId: 1002, label: '李四 (EMP002)', value: 1002, employeeId: 'EMP002', departmentId: 112 },
  { userId: 1003, label: '王五 (EMP003)', value: 1003, employeeId: 'EMP003', departmentId: 111 },
  { userId: 1004, label: '赵六 (EMP004)', value: 1004, employeeId: 'EMP004', departmentId: 113 },
  { userId: 1005, label: '孙七 (EMP005)', value: 1005, employeeId: 'EMP005', departmentId: 121 }
];

// ==================== 餐别数据 ====================
/**
 * 餐别列表
 */
export const MEAL_TYPE_LIST = [
  { mealTypeId: 1, mealTypeName: '早餐', startTime: '06:00', endTime: '09:00', status: 1 },
  { mealTypeId: 2, mealTypeName: '午餐', startTime: '11:00', endTime: '14:00', status: 1 },
  { mealTypeId: 3, mealTypeName: '晚餐', startTime: '17:00', endTime: '20:00', status: 1 },
  { mealTypeId: 4, mealTypeName: '夜宵', startTime: '22:00', endTime: '23:59', status: 1 }
];

// ==================== 数据导出 ====================
/**
 * 统一导出所有Mock数据
 */
export const consumeMockData = {
  // 基础数据
  AREA_TREE,
  ACCOUNT_KIND_LIST,
  DEVICE_LIST,
  DEPARTMENT_TREE,
  EMPLOYEE_OPTIONS,
  MEAL_TYPE_LIST,
  // 业务数据
  SUBSIDY_LIST,
  TRANSACTION_LIST,
  // 报表数据
  SALES_REPORT_DATA,
};

/**
 * 默认导出
 */
export default {
  ...consumeMockData,
};
