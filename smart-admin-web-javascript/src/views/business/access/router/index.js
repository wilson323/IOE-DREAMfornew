/*
 * 门禁管理路由配置
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import AccessManagement from '../index.vue';
import DeviceManagement from '../device/index.vue';
import PermissionManagement from '../permission/index.vue';
import AccessRecord from '../record/index.vue';
import AccessConfig from '../config/index.vue';
import AccessMonitor from '../monitor/index.vue';

export const accessRoutes = [
  {
    path: '/business/access',
    name: 'AccessManagement',
    component: AccessManagement,
    meta: {
      title: '门禁管理',
      icon: 'SafetyCertificateOutlined',
      keepAlive: true,
    },
  },
  {
    path: '/business/access/device',
    name: 'AccessDevice',
    component: DeviceManagement,
    meta: {
      title: '门禁设备',
      icon: 'DesktopOutlined',
      keepAlive: true,
      permission: ['access:device:view'],
    },
  },
  {
    path: '/business/access/permission',
    name: 'AccessPermission',
    component: PermissionManagement,
    meta: {
      title: '门禁权限',
      icon: 'SafetyCertificateOutlined',
      keepAlive: true,
      permission: ['access:permission:view'],
    },
  },
  {
    path: '/business/access/record',
    name: 'AccessRecord',
    component: AccessRecord,
    meta: {
      title: '通行记录',
      icon: 'HistoryOutlined',
      keepAlive: true,
      permission: ['access:record:view'],
    },
  },
  {
    path: '/business/access/config',
    name: 'AccessConfig',
    component: AccessConfig,
    meta: {
      title: '门禁配置',
      icon: 'SettingOutlined',
      keepAlive: true,
      permission: ['access:config:view'],
    },
  },
  {
    path: '/business/access/monitor',
    name: 'AccessMonitor',
    component: AccessMonitor,
    meta: {
      title: '实时监控',
      icon: 'DesktopOutlined',
      keepAlive: false, // 监控页面不需要缓存
      permission: ['access:monitor:view'],
    },
  },
];

export default accessRoutes;