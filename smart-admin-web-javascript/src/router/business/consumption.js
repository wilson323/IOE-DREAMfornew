/*
 * 消费管理模块路由（静态路由 - 可选）
 * 
 * 注意：SmartAdmin 推荐使用动态路由（从后端获取菜单）
 * 此文件仅用于开发阶段快速测试，生产环境请使用后端菜单配置
 *
 * @Author:    SmartAdmin
 * @Date:      2025-11-04
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import SmartLayout from '/@/layout/index.vue';
import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';

export const consumptionRouters = [
  {
    path: '/',
    component: SmartLayout,
    children: [
      {
        path: '/business/consumption/dashboard',
        name: 'ConsumptionDashboard',
        component: () => import('/@/views/business/consumption/dashboard/index.vue'),
        meta: {
          title: '消费管理 - 数据总览',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'DashboardOutlined',
          // 是否在菜单隐藏
          hideInMenu: false,
          // 页面是否keep-alive缓存
          keepAlive: true,
        },
      },
      {
        path: '/business/consumption/region',
        name: 'ConsumptionRegion',
        component: () => import('/@/views/business/consumption/region/index.vue'),
        meta: {
          title: '区域管理',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'ApartmentOutlined',
          hideInMenu: false,
          keepAlive: true,
        },
      },
      // 其他消费管理页面可以在这里继续添加
      // {
      //   path: '/business/consumption/account',
      //   name: 'ConsumptionAccount',
      //   component: () => import('/@/views/business/consumption/account/index.vue'),
      //   meta: {
      //     title: '账户管理',
      //     menuType: MENU_TYPE_ENUM.MENU.value,
      //     icon: 'UserOutlined',
      //     hideInMenu: false,
      //     keepAlive: true,
      //   },
      // },
    ],
  },
];

