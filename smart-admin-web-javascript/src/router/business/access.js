/*
 * 门禁管理路由配置
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

export default {
  path: '/business/access',
  name: 'AccessManagement',
  redirect: '/business/access/index',
  component: () => import('@/views/business/access/index.vue'),
  meta: {
    title: '门禁管理',
    icon: 'SafetyOutlined',
    permission: ['access:view'],
    keepAlive: true,
  },
  children: [
    // 设备管理
    {
      path: 'device',
      name: 'AccessDevice',
      component: () => import('@/views/business/access/device/index.vue'),
      meta: {
        title: '设备管理',
        icon: 'DesktopOutlined',
        permission: ['access:device:view'],
        keepAlive: true,
        activeMenu: '/business/access',
      },
    },
    // 权限管理
    {
      path: 'permission',
      name: 'AccessPermission',
      component: () => import('@/views/business/access/permission/index.vue'),
      meta: {
        title: '权限管理',
        icon: 'KeyOutlined',
        permission: ['access:permission:view'],
        keepAlive: true,
        activeMenu: '/business/access',
      },
    },
    // 通行记录
    {
      path: 'record',
      name: 'AccessRecord',
      component: () => import('@/views/business/access/record/index.vue'),
      meta: {
        title: '通行记录',
        icon: 'FileTextOutlined',
        permission: ['access:record:view'],
        keepAlive: true,
        activeMenu: '/business/access',
      },
    },
    // 实时监控
    {
      path: 'monitor',
      name: 'AccessMonitor',
      component: () => import('@/views/business/access/monitor/index.vue'),
      meta: {
        title: '实时监控',
        icon: 'EyeOutlined',
        permission: ['access:monitor:view'],
        keepAlive: false, // 实时监控页面不需要缓存
        activeMenu: '/business/access',
      },
    },
    // 数据分析
    {
      path: 'analytics',
      name: 'AccessAnalytics',
      component: () => import('@/views/business/access/analytics/index.vue'),
      meta: {
        title: '数据分析',
        icon: 'BarChartOutlined',
        permission: ['access:analytics:view'],
        keepAlive: true,
        activeMenu: '/business/access',
      },
    },
    // 系统配置
    {
      path: 'config',
      name: 'AccessConfig',
      component: () => import('@/views/business/access/config/index.vue'),
      meta: {
        title: '系统配置',
        icon: 'SettingOutlined',
        permission: ['access:config:view'],
        keepAlive: true,
        activeMenu: '/business/access',
      },
    },
  ],
};