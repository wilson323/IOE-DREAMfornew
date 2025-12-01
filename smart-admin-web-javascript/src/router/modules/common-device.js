import { BasicLayout } from '/@/layouts'

export default {
  name: 'CommonDevice',
  path: '/common/device',
  component: BasicLayout,
  meta: {
    title: '设备管理',
    icon: 'DesktopOutlined',
    permission: ['smart:device:list']
  },
  children: [
    {
      name: 'DeviceList',
      path: '',
      component: () => import('/@/views/common/device/index.vue'),
      meta: {
        title: '设备列表',
        permission: ['smart:device:list']
      }
    },
    {
      name: 'DeviceDetail',
      path: 'detail/:deviceId',
      component: () => import('/@/views/common/device/detail.vue'),
      meta: {
        title: '设备详情',
        permission: ['smart:device:view'],
        hidden: true
      }
    }
  ]
}