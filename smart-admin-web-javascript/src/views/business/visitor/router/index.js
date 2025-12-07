/*
 * 访客管理路由配置
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import VisitorManagement from '../index.vue';
import VisitorAppointment from '../appointment.vue';
import VisitorRegistration from '../registration.vue';
import VisitorVerification from '../verification.vue';
import VisitorRecord from '../record.vue';
import VisitorStatistics from '../statistics.vue';
import VisitorBlacklist from '../blacklist.vue';
import VisitorLogistics from '../logistics.vue';

export const visitorRoutes = [
  {
    path: '/business/visitor',
    name: 'VisitorManagement',
    component: VisitorManagement,
    meta: {
      title: '访客管理',
      icon: 'TeamOutlined',
      keepAlive: true,
    },
  },
  {
    path: '/business/visitor/appointment',
    name: 'VisitorAppointment',
    component: VisitorAppointment,
    meta: {
      title: '预约管理',
      icon: 'CalendarOutlined',
      keepAlive: true,
      permission: ['visitor:appointment:view'],
    },
  },
  {
    path: '/business/visitor/registration',
    name: 'VisitorRegistration',
    component: VisitorRegistration,
    meta: {
      title: '访客登记',
      icon: 'UserAddOutlined',
      keepAlive: false,
      permission: ['visitor:registration:add'],
    },
  },
  {
    path: '/business/visitor/verification',
    name: 'VisitorVerification',
    component: VisitorVerification,
    meta: {
      title: '访客验证',
      icon: 'SafetyCertificateOutlined',
      keepAlive: false,
      permission: ['visitor:verification:check'],
    },
  },
  {
    path: '/business/visitor/record',
    name: 'VisitorRecord',
    component: VisitorRecord,
    meta: {
      title: '访客记录',
      icon: 'HistoryOutlined',
      keepAlive: true,
      permission: ['visitor:record:view'],
    },
  },
  {
    path: '/business/visitor/statistics',
    name: 'VisitorStatistics',
    component: VisitorStatistics,
    meta: {
      title: '访客统计',
      icon: 'BarChartOutlined',
      keepAlive: true,
      permission: ['visitor:statistics:view'],
    },
  },
  {
    path: '/business/visitor/blacklist',
    name: 'VisitorBlacklist',
    component: VisitorBlacklist,
    meta: {
      title: '黑名单管理',
      icon: 'StopOutlined',
      keepAlive: true,
      permission: ['visitor:blacklist:view'],
    },
  },
  {
    path: '/business/visitor/logistics',
    name: 'VisitorLogistics',
    component: VisitorLogistics,
    meta: {
      title: '物流管理',
      icon: 'CarOutlined',
      keepAlive: true,
      permission: ['visitor:logistics:view'],
    },
  },
];

export default visitorRoutes;

