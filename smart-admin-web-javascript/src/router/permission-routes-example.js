/**
 * 权限路由配置示例
 * <p>
 * 展示如何在路由配置中使用统一权限守卫和资源码常量
 * 支持资源操作权限、数据域权限和角色权限的完整控制
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */

import { createPermissionGuard } from './permission-guard'
import ResourceCode from '@/constants/resource-code'

/**
 * 权限路由配置示例
 * 展示如何在路由meta中配置权限控制
 */
export const permissionRoutes = [
  {
    path: '/smart/access',
    name: 'AccessControl',
    component: () => import('@/views/smart/access/index.vue'),
    meta: {
      title: '门禁管理',
      requireAuth: true, // 需要登录
      resourceCode: ResourceCode.AccessDevice.VIEW, // 需要设备查看权限
      dataScope: ResourceCode.DataScope.AREA, // 需要区域数据域权限
      icon: 'LockOutlined',
      keepAlive: true
    },
    children: [
      {
        path: 'device',
        name: 'AccessDeviceList',
        component: () => import('@/views/smart/access/device/list.vue'),
        meta: {
          title: '门禁设备',
          resourceCode: ResourceCode.AccessDevice.VIEW,
          dataScope: ResourceCode.DataScope.AREA
        }
      },
      {
        path: 'device/add',
        name: 'AccessDeviceAdd',
        component: () => import('@/views/smart/access/device/add.vue'),
        meta: {
          title: '添加设备',
          resourceCode: ResourceCode.AccessDevice.ADD,
          dataScope: ResourceCode.DataScope.AREA,
          hidden: true // 隐藏菜单，但权限检查仍然生效
        }
      },
      {
        path: 'device/control/:id',
        name: 'AccessDeviceControl',
        component: () => import('@/views/smart/access/device/control.vue'),
        meta: {
          title: '设备控制',
          resourceCode: ResourceCode.AccessDevice.CONTROL,
          dataScope: ResourceCode.DataScope.AREA,
          requiredRoles: ['ACCESS_ADMIN', 'SYSTEM_ADMIN'], // 需要管理员角色
          roleMatchMode: 'any' // 匹配模式: 'any' 任一角色, 'all' 所有角色
        }
      }
    ]
  },
  {
    path: '/attendance',
    name: 'Attendance',
    component: () => import('@/views/attendance/index.vue'),
    meta: {
      title: '考勤管理',
      requireAuth: true,
      resourceCode: ResourceCode.Attendance.RECORD_VIEW,
      dataScope: ResourceCode.DataScope.DEPT, // 部门数据域权限
      icon: 'CalendarOutlined',
      keepAlive: true
    },
    children: [
      {
        path: 'record',
        name: 'AttendanceRecord',
        component: () => import('@/views/attendance/record/index.vue'),
        meta: {
          title: '考勤记录',
          resourceCode: ResourceCode.Attendance.RECORD_VIEW,
          dataScope: ResourceCode.DataScope.DEPT
        }
      },
      {
        path: 'export',
        name: 'AttendanceExport',
        component: () => import('@/views/attendance/export/index.vue'),
        meta: {
          title: '考勤导出',
          resourceCode: ResourceCode.Attendance.RECORD_EXPORT,
          dataScope: ResourceCode.DataScope.DEPT,
          requiredRoles: ['HR_MANAGER', 'DEPT_MANAGER']
        }
      },
      {
        path: 'schedule',
        name: 'AttendanceSchedule',
        component: () => import('@/views/attendance/schedule/index.vue'),
        meta: {
          title: '排班管理',
          resourceCode: ResourceCode.Attendance.SCHEDULE_MANAGE,
          requiredRoles: ['HR_MANAGER', 'SCHEDULE_ADMIN']
        }
      }
    ]
  },
  {
    path: '/consume',
    name: 'Consume',
    component: () => import('@/views/consume/index.vue'),
    meta: {
      title: '消费管理',
      requireAuth: true,
      resourceCode: ResourceCode.Consume.RECORD_VIEW,
      dataScope: ResourceCode.DataScope.SELF, // 个人数据域权限
      icon: 'WalletOutlined'
    },
    children: [
      {
        path: 'account',
        name: 'ConsumeAccount',
        component: () => import('@/views/consume/account/index.vue'),
        meta: {
          title: '消费账户',
          resourceCode: ResourceCode.Consume.ACCOUNT_MANAGE,
          dataScope: ResourceCode.DataScope.SELF
        }
      },
      {
        path: 'settlement',
        name: 'ConsumeSettlement',
        component: () => import('@/views/consume/settlement/index.vue'),
        meta: {
          title: '消费结算',
          resourceCode: ResourceCode.Consume.SETTLEMENT_MANAGE,
          requiredRoles: ['FINANCE_MANAGER', 'CASHIER']
        }
      }
    ]
  },
  {
    path: '/system/permission',
    name: 'SystemPermission',
    component: () => import('@/views/system/permission/index.vue'),
    meta: {
      title: '权限管理',
      requireAuth: true,
      resourceCode: ResourceCode.Permission.ROLE_MANAGE,
      requiredRoles: ['SYSTEM_ADMIN', 'PERMISSION_ADMIN'],
      icon: 'SafetyCertificateOutlined',
      onPermissionDenied: (denyType, denyDetail) => {
        // 自定义权限拒绝处理
        console.warn(`权限管理访问被拒绝: ${denyType} - ${denyDetail}`)
        // 可以显示自定义提示或执行其他逻辑
      }
    },
    children: [
      {
        path: 'role',
        name: 'RoleManagement',
        component: () => import('@/views/system/permission/role/index.vue'),
        meta: {
          title: '角色管理',
          resourceCode: ResourceCode.Permission.ROLE_MANAGE,
          requiredRoles: ['SYSTEM_ADMIN', 'PERMISSION_ADMIN']
        }
      },
      {
        path: 'data-scope',
        name: 'DataScopeConfig',
        component: () => import('@/views/system/permission/data-scope/index.vue'),
        meta: {
          title: '数据域配置',
          resourceCode: ResourceCode.Permission.DATA_SCOPE_CONFIG,
          requiredRoles: ['SYSTEM_ADMIN']
        }
      }
    ]
  }
]

/**
 * 路由守卫集成示例
 * @param {Object} router Vue Router实例
 */
export function setupPermissionGuard(router) {
  // 创建权限守卫
  const permissionGuard = createPermissionGuard(router)

  // 全局前置守卫
  router.beforeEach((to, from, next) => {
    // 权限检查
    if (!permissionGuard(to, from, next)) {
      // 权限检查失败，guard会处理next调用
      return
    }

    // 权限检查通过，继续其他逻辑
    next()
  })

  // 全局后置守卫（可选，用于记录访问日志等）
  router.afterEach((to, from) => {
    // 记录路由访问日志
    console.log(`路由访问: ${from.path} -> ${to.path}`)
  })
}

/**
 * 动态菜单权限过滤示例
 * @param {Array} allMenus 所有菜单配置
 * @returns {Array} 过滤后的菜单
 */
export function generatePermissionMenus(allMenus) {
  const { getAccessibleMenus } = require('./permission-guard')
  return getAccessibleMenus(allMenus)
}

export default {
  permissionRoutes,
  setupPermissionGuard,
  generatePermissionMenus
}