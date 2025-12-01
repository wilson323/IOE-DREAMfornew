/**
 * 权限辅助工具库
 * 提供权限相关的辅助函数、验证工具和便捷方法
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */

import { permissionManager } from './permission'
import { Message } from 'ant-design-vue'

/**
 * 权限辅助工具类
 */
export class PermissionHelper {
  constructor() {
    this.cache = new Map()
    this.cacheTimeout = 5 * 60 * 1000 // 5分钟缓存
  }

  /**
   * 批量权限检查
   * @param {Array} permissions - 权限检查列表
   * @param {Object} options - 配置选项
   * @returns {Promise<Object>} 检查结果
   */
  async batchCheckPermissions(permissions, options = {}) {
    const { mode = 'any', cache = true } = options
    const cacheKey = `batch:${JSON.stringify(permissions)}:${mode}`

    if (cache && this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)
    }

    try {
      // 构建批量检查请求
      const requests = permissions.map(perm => {
        if (typeof perm === 'string') {
          const [resource, action = 'READ'] = perm.split(':')
          return { resource, action }
        } else if (typeof perm === 'object') {
          return {
            resource: perm.resource,
            action: perm.action || 'READ'
          }
        }
        return null
      }).filter(Boolean)

      // 执行批量检查
      const results = await permissionManager.batchCheckPermissions(requests)

      // 根据模式计算最终结果
      let finalResult
      if (mode === 'all') {
        finalResult = results.every(result => result.hasPermission)
      } else {
        // any mode (default)
        finalResult = results.some(result => result.hasPermission)
      }

      const result = {
        hasPermission: finalResult,
        details: results,
        mode,
        checkedAt: new Date().toISOString()
      }

      if (cache) {
        this.cache.set(cacheKey, result)
        setTimeout(() => this.cache.delete(cacheKey), this.cacheTimeout)
      }

      return result

    } catch (error) {
      console.error('批量权限检查失败:', error)
      return {
        hasPermission: false,
        error: error.message,
        details: [],
        mode
      }
    }
  }

  /**
   * 获取用户权限概览
   * @returns {Promise<Object>} 权限概览信息
   */
  async getPermissionOverview() {
    try {
      const [
        userPermissions,
        permissionStats,
        accessibleAreas,
        accessibleDepts
      ] = await Promise.all([
        permissionManager.getUserPermissions(),
        permissionManager.getPermissionStats(),
        Promise.resolve(permissionManager.getAccessibleAreas()),
        Promise.resolve(permissionManager.getAccessibleDepts())
      ])

      return {
        user: {
          id: userPermissions?.userId,
          roles: userPermissions?.roles || [],
          isSuperAdmin: userPermissions?.isSuperAdmin || false
        },
        permissions: {
          totalRoles: permissionStats.totalRoles || 0,
          accessibleAreas: accessibleAreas.length,
          accessibleDepts: accessibleDepts.length
        },
        dataScope: {
          areas: accessibleAreas,
          departments: accessibleDepts
        },
        lastUpdated: new Date().toISOString()
      }

    } catch (error) {
      console.error('获取权限概览失败:', error)
      return {
        error: error.message,
        lastUpdated: new Date().toISOString()
      }
    }
  }

  /**
   * 检查操作权限并显示提示
   * @param {string} resource - 资源编码
   * @param {string} action - 动作
   * @param {string} message - 提示消息
   * @returns {Promise<boolean>} 是否有权限
   */
  async checkPermissionWithMessage(resource, action = 'READ', message) {
    const hasPermission = await permissionManager.checkPermissionWithMessage(
      resource,
      action,
      message || '权限不足，无法执行此操作'
    )

    return hasPermission
  }

  /**
   * 解析权限字符串
   * @param {string} permissionString - 权限字符串
   * @returns {Object} 解析结果
   */
  parsePermissionString(permissionString) {
    if (!permissionString || typeof permissionString !== 'string') {
      return null
    }

    const parts = permissionString.split(':')

    if (parts.length === 1) {
      return {
        resource: parts[0].trim(),
        action: 'READ'
      }
    } else if (parts.length === 2) {
      return {
        resource: parts[0].trim(),
        action: parts[1].trim()
      }
    } else if (parts.length === 3) {
      return {
        resource: parts[0].trim(),
        action: parts[1].trim(),
        dataScope: parts[2].trim()
      }
    }

    return null
  }

  /**
   * 构建权限字符串
   * @param {Object} config - 权限配置
   * @returns {string} 权限字符串
   */
  buildPermissionString(config) {
    const { resource, action = 'READ', dataScope } = config

    if (!resource) {
      return ''
    }

    let permissionString = `${resource}:${action}`

    if (dataScope) {
      permissionString += `:${dataScope}`
    }

    return permissionString
  }

  /**
   * 检查数据权限范围
   * @param {string} dataScope - 数据域
   * @param {Object} context - 上下文数据
   * @returns {Promise<boolean>} 是否有权限
   */
  async checkDataScopePermission(dataScope, context = {}) {
    const { areaId, deptId, userId, resource } = context

    switch (dataScope.toUpperCase()) {
      case 'ALL':
        return true

      case 'AREA':
        if (areaId) {
          return await permissionManager.hasAreaPermission(areaId)
        }
        // 检查是否有任意区域权限
        return permissionManager.getAccessibleAreas().length > 0

      case 'DEPT':
        if (deptId) {
          return await permissionManager.hasDeptPermission(deptId)
        }
        // 检查是否有任意部门权限
        return permissionManager.getAccessibleDepts().length > 0

      case 'SELF':
        if (userId) {
          return await permissionManager.hasPersonalDataPermission(userId)
        }
        // 自身数据权限，检查是否为当前用户
        return this.isCurrentUser(userId)

      case 'CUSTOM':
        // 自定义权限检查，可能需要调用特定的API
        if (resource && context.customCheck) {
          return await context.customCheck(resource, context)
        }
        return false

      default:
        console.warn('未知的数据域类型:', dataScope)
        return false
    }
  }

  /**
   * 检查是否为当前用户
   * @param {number} userId - 用户ID
   * @returns {boolean} 是否为当前用户
   */
  isCurrentUser(userId) {
    // TODO: 从用户Store中获取当前用户ID进行比较
    // const currentUser = useUserStore().userInfo
    // return currentUser?.userId === userId
    return false // 暂时返回false
  }

  /**
   * 获取资源权限配置
   * @param {string} resource - 资源编码
   * @returns {Promise<Object>} 权限配置
   */
  async getResourcePermissionConfig(resource) {
    try {
      const buttons = await permissionManager.getButtonPermissions(resource)
      const userPermissions = permissionManager.getUserPermissions()

      return {
        resource,
        buttons: buttons || [],
        userRoles: userPermissions?.roles || [],
        isSuperAdmin: userPermissions?.isSuperAdmin || false,
        accessibleAreas: userPermissions?.accessibleAreas || [],
        accessibleDepts: userPermissions?.accessibleDepts || []
      }

    } catch (error) {
      console.error('获取资源权限配置失败:', error)
      return {
        resource,
        buttons: [],
        userRoles: [],
        isSuperAdmin: false,
        accessibleAreas: [],
        accessibleDepts: [],
        error: error.message
      }
    }
  }

  /**
   * 过滤有权限的菜单项
   * @param {Array} menus - 菜单列表
   * @param {Object} options - 过滤选项
   * @returns {Promise<Array>} 过滤后的菜单列表
   */
  async filterMenusByPermission(menus, options = {}) {
    const { strict = true } = options

    if (!Array.isArray(menus)) {
      return []
    }

    const filteredMenus = []

    for (const menu of menus) {
      try {
        let hasPermission = true

        // 检查菜单权限
        if (menu.resourceCode) {
          hasPermission = await permissionManager.hasPermission(menu.resourceCode, 'READ')
        }

        // 检查数据域权限
        if (hasPermission && menu.dataScope) {
          hasPermission = await this.checkDataScopePermission(menu.dataScope, menu)
        }

        // 检查角色权限
        if (hasPermission && menu.requiredRoles) {
          const requiredRoles = Array.isArray(menu.requiredRoles)
            ? menu.requiredRoles
            : [menu.requiredRoles]

          const userRoles = permissionManager.getUserRoles()
          hasPermission = requiredRoles.some(role => userRoles.includes(role))
        }

        if (hasPermission || !strict) {
          // 递归处理子菜单
          if (menu.children && menu.children.length > 0) {
            menu.children = await this.filterMenusByPermission(menu.children, options)
          }

          // 在严格模式下，如果没有子菜单且有权限则保留，否则在非严格模式下也保留
          if (!strict || hasPermission || menu.children.length > 0) {
            filteredMenus.push({
              ...menu,
              hasPermission // 添加权限标识
            })
          }
        }

      } catch (error) {
        console.error('菜单权限检查失败:', error)
        // 严格模式下出错时跳过，非严格模式下保留
        if (!strict) {
          filteredMenus.push({
            ...menu,
            hasPermission: false,
            error: error.message
          })
        }
      }
    }

    return filteredMenus
  }

  /**
   * 权限验证装饰器
   * @param {Object} permissionConfig - 权限配置
   * @param {Function} targetFunction - 目标函数
   * @returns {Function} 装饰后的函数
   */
  withPermissionCheck(permissionConfig, targetFunction) {
    return async (...args) => {
      try {
        let hasPermission = false

        if (typeof permissionConfig === 'function') {
          // 自定义权限检查函数
          hasPermission = await permissionConfig(...args)
        } else {
          // 标准权限检查
          hasPermission = await this.checkObjectPermission(permissionConfig)
        }

        if (hasPermission) {
          return await targetFunction(...args)
        } else {
          const message = permissionConfig.message || '权限不足，无法执行此操作'
          Message.warning(message)
          throw new Error(message)
        }

      } catch (error) {
        console.error('权限验证失败:', error)
        throw error
      }
    }
  }

  /**
   * 检查对象格式权限（内部方法）
   */
  async checkObjectPermission(obj) {
    const {
      resource,
      action = 'READ',
      dataScope,
      roles,
      requireSuperAdmin
    } = obj

    // 检查超级管理员权限
    if (requireSuperAdmin && !permissionManager.isSuperAdmin()) {
      return false
    }

    // 检查角色权限
    if (roles) {
      const requiredRoles = Array.isArray(roles) ? roles : [roles]
      const userRoles = permissionManager.getUserRoles()
      const hasRole = requiredRoles.some(role => userRoles.includes(role))

      if (!hasRole) {
        return false
      }
    }

    // 检查基础权限
    if (resource) {
      const hasBasicPermission = await permissionManager.hasPermission(resource, action)
      if (!hasBasicPermission) {
        return false
      }
    }

    // 检查数据域权限
    if (dataScope) {
      const hasDataScopePermission = await this.checkDataScopePermission(dataScope, obj)
      if (!hasDataScopePermission) {
        return false
      }
    }

    return true
  }

  /**
   * 清除缓存
   */
  clearCache() {
    this.cache.clear()
    permissionManager.clearCache()
  }

  /**
   * 刷新权限缓存
   */
  async refreshCache() {
    this.clearCache()
    await permissionManager.refreshPermissionCache()
  }
}

// 创建全局实例
export const permissionHelper = new PermissionHelper()

/**
 * 便捷方法导出
 */
export const {
  batchCheckPermissions,
  getPermissionOverview,
  checkPermissionWithMessage,
  parsePermissionString,
  buildPermissionString,
  checkDataScopePermission,
  getResourcePermissionConfig,
  filterMenusByPermission,
  withPermissionCheck,
  clearCache,
  refreshCache
} = {
  batchCheckPermissions: (permissions, options) => permissionHelper.batchCheckPermissions(permissions, options),
  getPermissionOverview: () => permissionHelper.getPermissionOverview(),
  checkPermissionWithMessage: (resource, action, message) => permissionHelper.checkPermissionWithMessage(resource, action, message),
  parsePermissionString: (permissionString) => permissionHelper.parsePermissionString(permissionString),
  buildPermissionString: (config) => permissionHelper.buildPermissionString(config),
  checkDataScopePermission: (dataScope, context) => permissionHelper.checkDataScopePermission(dataScope, context),
  getResourcePermissionConfig: (resource) => permissionHelper.getResourcePermissionConfig(resource),
  filterMenusByPermission: (menus, options) => permissionHelper.filterMenusByPermission(menus, options),
  withPermissionCheck: (permissionConfig, targetFunction) => permissionHelper.withPermissionCheck(permissionConfig, targetFunction),
  clearCache: () => permissionHelper.clearCache(),
  refreshCache: () => permissionHelper.refreshCache()
}

export default permissionHelper