/*
 * 智能排班模块路由（静态路由 - 可选）
 *
 * 注意：SmartAdmin 推荐使用动态路由（从后端获取菜单）
 * 此文件仅用于开发阶段快速测试，生产环境请使用后端菜单配置
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM Project
 */
import SmartLayout from '/@/layout/index.vue';
import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';

export const smartScheduleRouters = [
  {
    path: '/',
    component: SmartLayout,
    children: [
      {
        path: '/business/attendance/smart-schedule-config',
        name: 'SmartScheduleConfig',
        component: () => import('/@/views/business/attendance/smart-schedule-config.vue'),
        meta: {
          title: '智能排班配置',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'ScheduleOutlined',
          hideInMenu: false,
          keepAlive: true,
        },
      },
      {
        path: '/business/attendance/smart-schedule-result',
        name: 'SmartScheduleResult',
        component: () => import('/@/views/business/attendance/smart-schedule-result.vue'),
        meta: {
          title: '排班结果展示',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'FileTextOutlined',
          hideInMenu: false,
          keepAlive: false,
        },
      },
      {
        path: '/business/attendance/schedule-rule-manage',
        name: 'ScheduleRuleManage',
        component: () => import('/@/views/business/attendance/schedule-rule-manage.vue'),
        meta: {
          title: '排班规则管理',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'RuleOutlined',
          hideInMenu: false,
          keepAlive: true,
        },
      },
    ],
  },
];
