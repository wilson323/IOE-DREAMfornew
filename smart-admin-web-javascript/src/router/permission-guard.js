/**
 * 统一权限路由守卫
 * <p>
 * 基于RAC模型的统一权限控制，支持：
 * - 资源操作权限验证
 * - 数据域权限过滤
 * - 角色权限检查
 * - 动态路由权限控制
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */

import { usePermissionStore } from '@/stores/smart-permission'
import ResourceCode from '@/constants/resource-code'

/**
 * 检查路由权限
 * @param {Object} to 目标路由
 * @param {Object} from 来源路由
 * @param {Function} next next函数
 * @returns {boolean} 是否有权限访问
 */
export function checkRoutePermission(to, from, next) {
  const permissionStore = usePermissionStore()

  // 1. 检查是否已登录
  if (!permissionStore.isLoggedIn) {
    // 未登录跳转到登录页
    next({ path: '/login', query: { redirect: to.fullPath } })
    return false
  }

  // 2. 检查路由是否需要权限验证
  if (!to.meta || !to.meta.requireAuth) {
    // 不需要权限验证的路由直接通过
    next()
    return true
  }

  // 3. 检查资源操作权限
  if (to.meta.resourceCode) {
    if (!permissionStore.hasPermission(to.meta.resourceCode)) {
      // 没有资源操作权限
      handlePermissionDenied(to, 'RESOURCE', to.meta.resourceCode)
      return false
    }
  }

  // 4. 检查数据域权限
  if (to.meta.dataScope) {
    if (!permissionStore.hasDataScope(to.meta.dataScope)) {
      // 没有数据域权限
      handlePermissionDenied(to, 'DATA_SCOPE', to.meta.dataScope)
      return false
    }
  }

  // 5. 检查角色权限
  if (to.meta.requiredRoles) {
    const requiredRoles = Array.isArray(to.meta.requiredRoles)
      ? to.meta.requiredRoles
      : [to.meta.requiredRoles]

    const hasRole = to.meta.roleMatchMode === 'all'
      ? requiredRoles.every(role => permissionStore.hasRole(role))
      : requiredRoles.some(role => permissionStore.hasRole(role))

    if (!hasRole) {
      // 没有必需的角色权限
      handlePermissionDenied(to, 'ROLE', requiredRoles.join(','))
      return false
    }
  }

  // 6. 所有权限检查通过
  next()
  return true
}

/**
 * 处理权限拒绝
 * @param {Object} route 路由对象
 * @param {string} denyType 权限类型
 * @param {string} denyDetail 权限详情
 */
function handlePermissionDenied(route, denyType, denyDetail) {
  console.warn(`权限拒绝: ${denyType} - ${denyDetail}`, route)

  // 根据路由配置决定处理方式
  if (route.meta && route.meta.onPermissionDenied) {
    // 使用路由自定义的权限拒绝处理函数
    if (typeof route.meta.onPermissionDenied === 'function') {
      route.meta.onPermissionDenied(denyType, denyDetail)
    }
  } else {
    // 默认处理方式：跳转到无权限页面
    window.location.href = '/403'
  }
}

/**
 * 动态路由权限过滤器
 * @param {Array} routes 路由列表
 * @returns {Array} 过滤后的路由列表
 */
export function filterRoutesByPermission(routes) {
  const permissionStore = usePermissionStore()

  return routes.filter(route => {
    // 检查路由权限
    if (route.meta && route.meta.resourceCode) {
      if (!permissionStore.hasPermission(route.meta.resourceCode)) {
        return false
      }
    }

    // 检查数据域权限
    if (route.meta && route.meta.dataScope) {
      if (!permissionStore.hasDataScope(route.meta.dataScope)) {
        return false
      }
    }

    // 检查角色权限
    if (route.meta && route.meta.requiredRoles) {
      const requiredRoles = Array.isArray(route.meta.requiredRoles)
        ? route.meta.requiredRoles
        : [route.meta.requiredRoles]

      const hasRole = route.meta.roleMatchMode === 'all'
        ? requiredRoles.every(role => permissionStore.hasRole(role))
        : requiredRoles.some(role => permissionStore.hasRole(route))

      if (!hasRole) {
        return false
      }
    }

    // 递归检查子路由
    if (route.children && route.children.length > 0) {
      route.children = filterRoutesByPermission(route.children)
      // 如果子路由全部被过滤掉，且父路由没有redirect，则过滤掉父路由
      if (route.children.length === 0 && !route.redirect) {
        return false
      }
    }

    return true
  })
}

/**
 * 检查菜单权限
 * @param {Object} menuItem 菜单项
 * @returns {boolean} 是否有权限
 */
export function checkMenuPermission(menuItem) {
  const permissionStore = usePermissionStore()

  // 检查资源操作权限
  if (menuItem.resourceCode) {
    if (!permissionStore.hasPermission(menuItem.resourceCode)) {
      return false
    }
  }

  // 检查数据域权限
  if (menuItem.dataScope) {
    if (!permissionStore.hasDataScope(menuItem.dataScope)) {
      return false
    }
  }

  // 检查角色权限
  if (menuItem.requiredRoles) {
    const requiredRoles = Array.isArray(menuItem.requiredRoles)
      ? menuItem.requiredRoles
      : [menuItem.requiredRoles]

    const hasRole = menuItem.roleMatchMode === 'all'
      ? requiredRoles.every(role => permissionStore.hasRole(role))
      : requiredRoles.some(role => permissionStore.hasRole(role))

    if (!hasRole) {
      return false
    }
  }

  return true
}

/**
 * 获取用户可访问的菜单列表
 * @param {Array} allMenus 所有菜单列表
 * @returns {Array} 过滤后的菜单列表
 */
export function getAccessibleMenus(allMenus) {
  return allMenus.filter(menu => {
    // 检查当前菜单权限
    if (!checkMenuPermission(menu)) {
      return false
    }

    // 递归检查子菜单
    if (menu.children && menu.children.length > 0) {
      menu.children = getAccessibleMenus(menu.children)
      // 如果子菜单全部被过滤掉，则过滤掉父菜单
      return menu.children.length > 0
    }

    return true
  })
}

/**
 * 权限守卫工厂函数
 * @param {Object} router Vue Router实例
 * @returns {Function} 路由守卫函数
 */
export function createPermissionGuard(router) {
  return (to, from, next) => {
    return checkRoutePermission(to, from, next)
  }
}

export default {
  checkRoutePermission,
  filterRoutesByPermission,
  checkMenuPermission,
  getAccessibleMenus,
  createPermissionGuard,
  ResourceCode
}