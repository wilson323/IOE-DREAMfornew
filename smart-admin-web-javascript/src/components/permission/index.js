/**
 * 权限管理组件入口文件
 * 统一导出所有权限相关的组件和工具
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */

// 权限组件
import PermissionWrapper from './PermissionWrapper.vue'
import PermissionButton from './PermissionButton.vue'

// 权限工具
import { permissionManager } from '@/utils/permission'
import { permissionHelper,
  batchCheckPermissions,
  getPermissionOverview,
  checkPermissionWithMessage,
  parsePermissionString,
  buildPermissionString,
  checkDataScopePermission,
  getResourcePermissionConfig,
  filterMenusByPermission,
  withPermissionCheck } from '@/utils/permission-helper'

// 路由守卫
import {
  checkRoutePermission,
  filterRoutesByPermission,
  checkMenuPermission,
  getAccessibleMenus,
  createPermissionGuard } from '@/router/permission-guard'

// 权限指令
import permissionDirective from '@/directives/permission'

/**
 * 权限管理插件
 */
const PermissionPlugin = {
  install(app, options = {}) {
    // 注册全局组件
    app.component('PermissionWrapper', PermissionWrapper)
    app.component('PermissionButton', PermissionButton)

    // 注册全局指令
    app.directive('permission', permissionDirective.permission)
    app.directive('permission-show', permissionDirective['permission-show'])
    app.directive('permission-disabled', permissionDirective['permission-disabled'])

    // 提供全局属性
    app.config.globalProperties.$permission = permissionManager
    app.config.globalProperties.$permissionHelper = permissionHelper

    // 提供全局权限检查方法
    app.config.globalProperties.$hasPermission = (resource, action) =>
      permissionManager.hasPermission(resource, action)
    app.config.globalProperties.$canRead = (resource) =>
      permissionManager.canRead(resource)
    app.config.globalProperties.$canWrite = (resource) =>
      permissionManager.canWrite(resource)
    app.config.globalProperties.$canDelete = (resource) =>
      permissionManager.canDelete(resource)
    app.config.globalProperties.$isSuperAdmin = () =>
      permissionManager.isSuperAdmin()

    console.log('权限管理插件已安装')
  }
}

/**
 * 权限管理类
 * 提供统一的权限管理API
 */
export class PermissionManager {
  constructor() {
    this.permissionManager = permissionManager
    this.permissionHelper = permissionHelper
  }

  // 权限检查方法
  async hasPermission(resource, action = 'READ') {
    return await this.permissionManager.hasPermission(resource, action)
  }

  async canRead(resource) {
    return await this.permissionManager.canRead(resource)
  }

  async canWrite(resource) {
    return await this.permissionManager.canWrite(resource)
  }

  async canDelete(resource) {
    return await this.permissionManager.canDelete(resource)
  }

  async canApprove(resource) {
    return await this.permissionManager.canApprove(resource)
  }

  // 批量权限检查
  async batchCheck(permissions, options = {}) {
    return await this.permissionHelper.batchCheckPermissions(permissions, options)
  }

  // 数据域权限检查
  async hasDataScope(dataScope, context = {}) {
    return await this.permissionHelper.checkDataScopePermission(dataScope, context)
  }

  async hasAreaPermission(areaId) {
    return await this.permissionManager.hasAreaPermission(areaId)
  }

  async hasDeptPermission(deptId) {
    return await this.permissionManager.hasDeptPermission(deptId)
  }

  async hasPersonalDataPermission(userId) {
    return await this.permissionManager.hasPersonalDataPermission(userId)
  }

  // 用户信息
  getUserPermissions() {
    return this.permissionManager.getUserPermissions()
  }

  getUserRoles() {
    return this.permissionManager.getUserRoles()
  }

  getAccessibleAreas() {
    return this.permissionManager.getAccessibleAreas()
  }

  getAccessibleDepts() {
    return this.permissionManager.getAccessibleDepts()
  }

  isSuperAdmin() {
    return this.permissionManager.isSuperAdmin()
  }

  // 菜单和路由权限
  async filterMenus(menus, options = {}) {
    return await this.permissionHelper.filterMenusByPermission(menus, options)
  }

  async getResourceConfig(resource) {
    return await this.permissionHelper.getResourcePermissionConfig(resource)
  }

  async getOverview() {
    return await this.permissionHelper.getPermissionOverview()
  }

  // 权限检查与提示
  async checkWithMessage(resource, action, message) {
    return await this.permissionHelper.checkPermissionWithMessage(resource, action, message)
  }

  // 工具方法
  parsePermission(permissionString) {
    return this.permissionHelper.parsePermissionString(permissionString)
  }

  buildPermission(config) {
    return this.permissionHelper.buildPermissionString(config)
  }

  // 缓存管理
  clearCache() {
    this.permissionHelper.clearCache()
  }

  async refreshCache() {
    await this.permissionHelper.refreshCache()
  }

  // 函数装饰器
  withPermission(permissionConfig, targetFunction) {
    return this.permissionHelper.withPermissionCheck(permissionConfig, targetFunction)
  }
}

// 创建全局权限管理实例
export const permissionManagerInstance = new PermissionManager()

/**
 * 响应式权限组合式API
 */
export function usePermission() {
  return {
    // 权限管理器
    permission: permissionManagerInstance,

    // 便捷方法
    hasPermission: (resource, action) => permissionManagerInstance.hasPermission(resource, action),
    canRead: (resource) => permissionManagerInstance.canRead(resource),
    canWrite: (resource) => permissionManagerInstance.canWrite(resource),
    canDelete: (resource) => permissionManagerInstance.canDelete(resource),
    isSuperAdmin: () => permissionManagerInstance.isSuperAdmin(),

    // 用户信息
    getUserInfo: () => permissionManagerInstance.getUserPermissions(),
    getUserRoles: () => permissionManagerInstance.getUserRoles(),
    getAccessibleAreas: () => permissionManagerInstance.getAccessibleAreas(),
    getAccessibleDepts: () => permissionManagerInstance.getAccessibleDepts(),

    // 批量检查
    batchCheck: (permissions, options) => permissionManagerInstance.batchCheck(permissions, options),

    // 权限概览
    getOverview: () => permissionManagerInstance.getOverview(),

    // 缓存管理
    clearCache: () => permissionManagerInstance.clearCache(),
    refreshCache: () => permissionManagerInstance.refreshCache()
  }
}

/**
 * 数据域权限组合式API
 */
export function useDataScope() {
  return {
    hasDataScope: (dataScope, context) => permissionManagerInstance.hasDataScope(dataScope, context),
    hasAreaPermission: (areaId) => permissionManagerInstance.hasAreaPermission(areaId),
    hasDeptPermission: (deptId) => permissionManagerInstance.hasDeptPermission(deptId),
    hasPersonalDataPermission: (userId) => permissionManagerInstance.hasPersonalDataPermission(userId),

    // 获取可访问的数据范围
    getAccessibleAreas: () => permissionManagerInstance.getAccessibleAreas(),
    getAccessibleDepts: () => permissionManagerInstance.getAccessibleDepts()
  }
}

/**
 * 路由权限组合式API
 */
export function useRoutePermission() {
  return {
    checkRoutePermission,
    filterRoutes: filterRoutesByPermission,
    checkMenu: checkMenuPermission,
    getAccessibleMenus,
    createGuard: createPermissionGuard
  }
}

// 导出组件
export {
  PermissionWrapper,
  PermissionButton
}

// 导出工具
export {
  permissionManager,
  permissionHelper,
  batchCheckPermissions,
  getPermissionOverview,
  checkPermissionWithMessage,
  parsePermissionString,
  buildPermissionString,
  checkDataScopePermission,
  getResourcePermissionConfig,
  filterMenusByPermission,
  withPermissionCheck
}

// 导出路由守卫
export {
  checkRoutePermission,
  filterRoutesByPermission,
  checkMenuPermission,
  getAccessibleMenus,
  createPermissionGuard
}

// 导出指令
export {
  permissionDirective
}

// 导出插件和管理器
export {
  PermissionPlugin,
  PermissionManager,
  permissionManagerInstance
}

// 默认导出插件
export default PermissionPlugin