/*
 * 消费管理常量
 *
 * @Author:    SmartAdmin
 * @Date:      2025-11-04
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

/**
 * 活动类型枚举
 */
export const ACTIVITY_TYPE_ENUM = {
  USER_REGISTER: {
    value: 'user_register',
    desc: '用户注册',
    icon: 'UserAddOutlined',
    color: 'success',
  },
  SUBSIDY_GRANT: {
    value: 'subsidy_grant',
    desc: '补贴发放',
    icon: 'WalletOutlined',
    color: 'info',
  },
  DEVICE_OFFLINE: {
    value: 'device_offline',
    desc: '设备离线',
    icon: 'ExclamationCircleOutlined',
    color: 'warning',
  },
  DATA_SYNC: {
    value: 'data_sync',
    desc: '数据同步',
    icon: 'SyncOutlined',
    color: 'success',
  },
  ORDER_CREATED: {
    value: 'order_created',
    desc: '订单创建',
    icon: 'ShoppingCartOutlined',
    color: 'info',
  },
};

/**
 * 统计卡片类型枚举
 */
export const STAT_CARD_TYPE_ENUM = {
  TURNOVER: {
    value: 'turnover',
    desc: '今日营业额',
    icon: 'DollarOutlined',
    color: '#1890ff',
  },
  ORDER_COUNT: {
    value: 'orderCount',
    desc: '今日订单数',
    icon: 'ShoppingCartOutlined',
    color: '#52c41a',
  },
  ACTIVE_USERS: {
    value: 'activeUsers',
    desc: '活跃用户',
    icon: 'TeamOutlined',
    color: '#faad14',
  },
  AVERAGE_PRICE: {
    value: 'averagePrice',
    desc: '平均客单价',
    icon: 'FileTextOutlined',
    color: '#2f54eb',
  },
};

/**
 * 快速操作菜单
 */
export const QUICK_ACTION_MENU = [
  {
    title: '账户类别管理',
    desc: '配置账户类别和权限',
    icon: 'TagsOutlined',
    path: '/business/consumption/account-category',
  },
  {
    title: '账户管理',
    desc: '管理用户账户信息',
    icon: 'UserOutlined',
    path: '/business/consumption/account',
  },
  {
    title: '区域管理',
    desc: '配置消费区域信息',
    icon: 'EnvironmentOutlined',
    path: '/business/consumption/region',
  },
  {
    title: '餐别资料',
    desc: '管理餐别和时段设置',
    icon: 'ClockCircleOutlined',
    path: '/business/consumption/meal',
  },
  {
    title: '参数设置',
    desc: '配置系统参数和规则',
    icon: 'SettingOutlined',
    path: '/business/consumption/parameter',
  },
  {
    title: '设备管理',
    desc: '管理终端设备状态',
    icon: 'DesktopOutlined',
    path: '/business/consumption/device',
  },
  {
    title: '商品资料',
    desc: '管理商品信息库存',
    icon: 'AppstoreOutlined',
    path: '/business/consumption/product',
  },
  {
    title: '补贴管理',
    desc: '管理补贴计划发放',
    icon: 'GiftOutlined',
    path: '/business/consumption/subsidy',
  },
  {
    title: '报表中心',
    desc: '查看业务数据报表',
    icon: 'BarChartOutlined',
    path: '/business/consumption/report',
  },
];

export default {
  ACTIVITY_TYPE_ENUM,
  STAT_CARD_TYPE_ENUM,
  QUICK_ACTION_MENU,
};

