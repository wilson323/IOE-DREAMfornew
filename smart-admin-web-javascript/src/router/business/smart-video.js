/*
 * 智能视频模块路由（静态路由 - 可选）
 *
 * 注意：SmartAdmin 推荐使用动态路由（从后端获取菜单）
 * 此文件仅用于开发阶段快速测试，生产环境请使用后端菜单配置
 *
 * 重要：路由的 name 必须与 user.js 中菜单的 menuId 保持一致
 * 这样才能正确高亮菜单项
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import SmartLayout from '/@/layout/index.vue';
import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';

// 重要：路由的 name 必须与 user.js 中的 menuId 完全一致
export const smartVideoRouters = [
  {
    path: '/',
    component: SmartLayout,
    children: [
      // 系统概览 - menuId: 9001
      {
        path: '/business/smart-video/system-overview',
        name: 9001,
        component: () => import('/@/views/business/smart-video/system-overview.vue'),
        meta: {
          title: '智能视频 - 系统概览',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'DashboardOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoSystemOverview',
          renameComponentFlag: false,
        },
      },
      // 设备管理 - 设备 - menuId: 9101
      {
        path: '/business/smart-video/device-list',
        name: 9101,
        component: () => import('/@/views/business/smart-video/device-list.vue'),
        meta: {
          title: '智能视频 - 设备',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'VideoCameraOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoDeviceList',
          renameComponentFlag: false,
        },
      },
      // 设备管理 - 分组 - menuId: 9102
      {
        path: '/business/smart-video/device-group',
        name: 9102,
        component: () => import('/@/views/business/smart-video/device-group.vue'),
        meta: {
          title: '智能视频 - 分组',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'ApartmentOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoDeviceGroup',
          renameComponentFlag: false,
        },
      },
      // 视频监控 - 监控预览 - menuId: 9201
      {
        path: '/business/smart-video/monitor-preview',
        name: 9201,
        component: () => import('/@/views/business/smart-video/monitor-preview.vue'),
        meta: {
          title: '智能视频 - 监控预览',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'EyeOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoMonitorPreview',
          renameComponentFlag: false,
        },
      },
      // 视频监控 - 录像回放 - menuId: 9202
      {
        path: '/business/smart-video/video-playback',
        name: 9202,
        component: () => import('/@/views/business/smart-video/video-playback.vue'),
        meta: {
          title: '智能视频 - 录像回放',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'PlaySquareOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoVideoPlayback',
          renameComponentFlag: false,
        },
      },
      // 解码上墙 - 解码器 - menuId: 9301
      {
        path: '/business/smart-video/decoder-management',
        name: 9301,
        component: () => import('/@/views/business/smart-video/decoder-management.vue'),
        meta: {
          title: '智能视频 - 解码器',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'ApiOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoDecoderManagement',
          renameComponentFlag: false,
        },
      },
      // 解码上墙 - 电视墙 - menuId: 9302
      {
        path: '/business/smart-video/tv-wall',
        name: 9302,
        component: () => import('/@/views/business/smart-video/tv-wall.vue'),
        meta: {
          title: '智能视频 - 电视墙',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'DesktopOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoTvWall',
          renameComponentFlag: false,
        },
      },
      // 解码上墙 - 大屏管控 - menuId: 9303
      {
        path: '/business/smart-video/screen-control',
        name: 9303,
        component: () => import('/@/views/business/smart-video/screen-control.vue'),
        meta: {
          title: '智能视频 - 大屏管控',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'MonitorOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoScreenControl',
          renameComponentFlag: false,
        },
      },
      // 智能分析 - 算法模式 - menuId: 9401
      {
        path: '/business/smart-video/algorithm-mode',
        name: 9401,
        component: () => import('/@/views/business/smart-video/algorithm-mode.vue'),
        meta: {
          title: '智能视频 - 算法模式',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'FunctionOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoAlgorithmMode',
          renameComponentFlag: false,
        },
      },
      // 智能分析 - 目标智能 - menuId: 9402
      {
        path: '/business/smart-video/target-intelligence',
        name: 9402,
        component: () => import('/@/views/business/smart-video/target-intelligence.vue'),
        meta: {
          title: '智能视频 - 目标智能',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'AimOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoTargetIntelligence',
          renameComponentFlag: false,
        },
      },
      // 智能分析 - 行为分析 - menuId: 9403
      {
        path: '/business/smart-video/behavior-analysis',
        name: 9403,
        component: () => import('/@/views/business/smart-video/behavior-analysis.vue'),
        meta: {
          title: '智能视频 - 行为分析',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'BranchesOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoBehaviorAnalysis',
          renameComponentFlag: false,
        },
      },
      // 智能分析 - 人群态势 - menuId: 9404
      {
        path: '/business/smart-video/crowd-situation',
        name: 9404,
        component: () => import('/@/views/business/smart-video/crowd-situation.vue'),
        meta: {
          title: '智能视频 - 人群态势',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'TeamOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoCrowdSituation',
          renameComponentFlag: false,
        },
      },
      // 检索功能 - 目标检索 - menuId: 9501
      {
        path: '/business/smart-video/target-search',
        name: 9501,
        component: () => import('/@/views/business/smart-video/target-search.vue'),
        meta: {
          title: '智能视频 - 目标检索',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'ScanOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoTargetSearch',
          renameComponentFlag: false,
        },
      },
      // 检索功能 - 以图搜图 - menuId: 9502
      {
        path: '/business/smart-video/image-search',
        name: 9502,
        component: () => import('/@/views/business/smart-video/image-search.vue'),
        meta: {
          title: '智能视频 - 以图搜图',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'PictureOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoImageSearch',
          renameComponentFlag: false,
        },
      },
      // 联动管理 - 联动设置 - menuId: 9601
      {
        path: '/business/smart-video/linkage-settings',
        name: 9601,
        component: () => import('/@/views/business/smart-video/linkage-settings.vue'),
        meta: {
          title: '智能视频 - 联动设置',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'SettingOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoLinkageSettings',
          renameComponentFlag: false,
        },
      },
      // 联动管理 - 联动记录 - menuId: 9602
      {
        path: '/business/smart-video/linkage-records',
        name: 9602,
        component: () => import('/@/views/business/smart-video/linkage-records.vue'),
        meta: {
          title: '智能视频 - 联动记录',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'HistoryOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoLinkageRecords',
          renameComponentFlag: false,
        },
      },
      // 系统分析 - 过热图 - menuId: 9701
      {
        path: '/business/smart-video/heatmap',
        name: 9701,
        component: () => import('/@/views/business/smart-video/heatmap.vue'),
        meta: {
          title: '智能视频 - 过热图',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'HeatMapOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoHeatmap',
          renameComponentFlag: false,
        },
      },
      // 系统分析 - 过热统计 - menuId: 9702
      {
        path: '/business/smart-video/heat-statistics',
        name: 9702,
        component: () => import('/@/views/business/smart-video/heat-statistics.vue'),
        meta: {
          title: '智能视频 - 过热统计',
          menuType: MENU_TYPE_ENUM.MENU.value,
          icon: 'DotChartOutlined',
          hideInMenu: false,
          keepAlive: true,
          componentName: 'SmartVideoHeatStatistics',
          renameComponentFlag: false,
        },
      },
    ],
  },
];