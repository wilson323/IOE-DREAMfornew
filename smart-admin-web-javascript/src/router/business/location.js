/**
 * 位置管理路由配置
 */

import { BasicLayout } from '/@/layout/index';

export default {
  path: '/location',
  name: 'Location',
  component: BasicLayout,
  meta: {
    title: '位置管理',
    icon: 'EnvironmentOutlined',
    sort: 200,
    isAffix: false,
    isLink: false,
    isHide: false,
    isKeepAlive: true,
    frameSrc: '',
    frameBlank: false,
    query: {},
    params: {},
    roles: ['ADMIN', 'LOCATION_MANAGER', 'LOCATION_VIEWER']
  },
  children: [
    {
      path: '/location',
      name: 'LocationManage',
      component: () => import('/@/views/location/LocationManage.vue'),
      meta: {
        title: '位置管理',
        icon: 'EnvironmentOutlined',
        sort: 1,
        isAffix: false,
        isLink: false,
        isHide: false,
        isKeepAlive: true,
        frameSrc: '',
        frameBlank: false,
        query: {},
        params: {},
        roles: ['ADMIN', 'LOCATION_MANAGER', 'LOCATION_VIEWER']
      }
    },
    {
      path: '/location/map',
      name: 'LocationMapView',
      component: () => import('/@/views/location/MapView.vue'),
      meta: {
        title: '地图视图',
        icon: 'GlobalOutlined',
        sort: 2,
        isAffix: false,
        isLink: false,
        isHide: false,
        isKeepAlive: true,
        frameSrc: '',
        frameBlank: false,
        query: {},
        params: {},
        roles: ['ADMIN', 'LOCATION_MANAGER', 'LOCATION_VIEWER']
      }
    },
    {
      path: '/location/geofence',
      name: 'GeoFenceManage',
      component: () => import('/@/views/location/geofence/index.vue'),
      meta: {
        title: '地理围栏',
        icon: 'AimOutlined',
        sort: 3,
        isAffix: false,
        isLink: false,
        isHide: false,
        isKeepAlive: true,
        frameSrc: '',
        frameBlank: false,
        query: {},
        params: {},
        roles: ['ADMIN', 'LOCATION_MANAGER']
      }
    },
    {
      path: '/location/tracking',
      name: 'LocationTracking',
      component: () => import('/@/views/location/tracking/index.vue'),
      meta: {
        title: '实时追踪',
        icon: 'WifiOutlined',
        sort: 4,
        isAffix: false,
        isLink: false,
        isHide: false,
        isKeepAlive: true,
        frameSrc: '',
        frameBlank: false,
        query: {},
        params: {},
        roles: ['ADMIN', 'LOCATION_MANAGER']
      }
    },
    {
      path: '/location/analysis',
      name: 'LocationAnalysis',
      component: () => import('/@/views/location/analysis/index.vue'),
      meta: {
        title: '位置分析',
        icon: 'BarChartOutlined',
        sort: 5,
        isAffix: false,
        isLink: false,
        isHide: false,
        isKeepAlive: true,
        frameSrc: '',
        frameBlank: false,
        query: {},
        params: {},
        roles: ['ADMIN', 'LOCATION_MANAGER', 'LOCATION_ANALYST']
      }
    },
    {
      path: '/location/history',
      name: 'LocationHistory',
      component: () => import('/@/views/location/history/index.vue'),
      meta: {
        title: '历史轨迹',
        icon: 'HistoryOutlined',
        sort: 6,
        isAffix: false,
        isLink: false,
        isHide: false,
        isKeepAlive: true,
        frameSrc: '',
        frameBlank: false,
        query: {},
        params: {},
        roles: ['ADMIN', 'LOCATION_MANAGER', 'LOCATION_VIEWER']
      }
    },
    {
      path: '/location/heatmap',
      name: 'LocationHeatmap',
      component: () => import('/@/views/location/heatmap/index.vue'),
      meta: {
        title: '热力图',
        icon: 'FireOutlined',
        sort: 7,
        isAffix: false,
        isLink: false,
        isHide: false,
        isKeepAlive: true,
        frameSrc: '',
        frameBlank: false,
        query: {},
        params: {},
        roles: ['ADMIN', 'LOCATION_MANAGER', 'LOCATION_ANALYST']
      }
    },
    {
      path: '/location/statistics',
      name: 'LocationStatistics',
      component: () => import('/@/views/location/statistics/index.vue'),
      meta: {
        title: '统计分析',
        icon: 'LineChartOutlined',
        sort: 8,
        isAffix: false,
        isLink: false,
        isHide: false,
        isKeepAlive: true,
        frameSrc: '',
        frameBlank: false,
        query: {},
        params: {},
        roles: ['ADMIN', 'LOCATION_MANAGER', 'LOCATION_ANALYST']
      }
    },
    {
      path: '/location/settings',
      name: 'LocationSettings',
      component: () => import('/@/views/location/settings/index.vue'),
      meta: {
        title: '位置设置',
        icon: 'SettingOutlined',
        sort: 9,
        isAffix: false,
        isLink: false,
        isHide: false,
        isKeepAlive: true,
        frameSrc: '',
        frameBlank: false,
        query: {},
        params: {},
        roles: ['ADMIN', 'LOCATION_MANAGER']
      }
    }
  ]
};