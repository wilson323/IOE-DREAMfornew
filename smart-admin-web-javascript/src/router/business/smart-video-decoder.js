/*
 * 智能视频-解码器路由配置（静态路由 - 可选）
 *
 * 注意：SmartAdmin 推荐使用动态路由（从后端获取菜单）
 * 此文件仅用于开发阶段快速测试，生产环境请使用后端菜单配置
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Copyright  1024创新实验室
 */
import SmartLayout from '/@/layout/index.vue';
import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';

export const smartVideoDecoderRouters = [
  {
    path: '/',
    component: SmartLayout,
    children: [
      {
        path: '/business/smart-video/decoder-management',
        name: 'SmartVideoDecoderManagement',
        component: () => import('/@/views/business/smart-video/decoder-management.vue'),
        meta: {
          title: '智能视频 - 解码器管理',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'AppstoreOutlined',
          hideInMenu: false,
          keepAlive: true,
        },
      },
    ],
  },
];