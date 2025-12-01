/**
 * 权限管理工具
 * 基于RAC权限模型的前端权限控制工具
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */

import { request } from '@/utils/request'
import { Message } from 'ant-design-vue'

/**
 * 权限管理工具类
 */
class PermissionManager {
  constructor() {
    this.userPermissions = null
    this.permissionCache = new Map()
    this.init()
  }

  /**
   * 初始化权限管理器
   */
  async init() {
    try {
      await this.loadUserPermissions()
      console.log('权限管理器初始化完成')
    } catch (error) {
      console.error('权限管理器初始化失败:', error)
      Message.error('权限管理器初始化失败')
    }
  }

  /**
   * 加载用户权限信息
   */
  async loadUserPermissions() {
    try {
      const response = await request.get('/api/auth/permission/user-permissions')
      if (response.data) {
        this.userPermissions = response.data
        this.clearCache()
        console.log('用户权限加载成功:', this.userPermissions)
      }
    } catch (error) {
      console.error('加载用户权限失败:', error)
      throw error
    }
  }

  /**
   * 检查是否有指定资源权限
   * @param {string} resource - 资源编码
   * @param {string} action - 动作，默认为 READ
   * @returns {boolean} 是否有权限
   */
  async hasPermission(resource, action = 'READ') {
    // 检查缓存
    const cacheKey = `${resource}:${action}`
    if (this.permissionCache.has(cacheKey)) {
      return this.permissionCache.get(cacheKey)
    }

    try {
      const response = await request.get('/api/auth/permission/check', {
        params: { resource, action }
      })

      const hasPermission = response.data || false
      this.permissionCache.set(cacheKey, hasPermission)
      return hasPermission
    } catch (error) {
      console.error('权限检查失败:', error)
      return false
    }
  }

  /**
   * 批量检查权限
   * @param {Array} permissions - 权限检查请求数组
   * @returns {Array} 权限检查结果数组
   */
  async batchCheckPermissions(permissions) {
    try {
      const response = await request.post('/api/auth/permission/batch-check', permissions)
      const results = response.data || []

      // 更新缓存
      results.forEach(result => {
        const cacheKey = `${result.resource}:${result.action}`
        this.permissionCache.set(cacheKey, result.hasPermission)
      })

      return results
    } catch (error) {
      console.error('批量权限检查失败:', error)
      return permissions.map(p => ({
        resource: p.resource,
        action: p.action,
        hasPermission: false
      }))
    }
  }

  /**
   * 检查读权限
   * @param {string} resource - 资源编码
   * @returns {boolean} 是否有读权限
   */
  async canRead(resource) {
    return await this.hasPermission(resource, 'READ')
  }

  /**
   * 检查写权限
   * @param {string} resource - 资源编码
   * @returns {boolean} 是否有写权限
   */
  async canWrite(resource) {
    return await this.hasPermission(resource, 'WRITE')
  }

  /**
   * 检查删除权限
   * @param {string} resource - 资源编码
   * @returns {boolean} 是否有删除权限
   */
  async canDelete(resource) {
    return await this.hasPermission(resource, 'DELETE')
  }

  /**
   * 检查审批权限
   * @param {string} resource - 资源编码
   * @returns {boolean} 是否有审批权限
   */
  async canApprove(resource) {
    return await this.hasPermission(resource, 'APPROVE')
  }

  /**
   * 检查是否有指定区域的数据权限
   * @param {number} areaId - 区域ID
   * @returns {boolean} 是否有区域权限
   */
  async hasAreaPermission(areaId) {
    try {
      const response = await request.get('/api/auth/permission/data-scope/check', {
        params: { type: 'area', dataId: areaId }
      })
      return response.data || false
    } catch (error) {
      console.error('区域权限检查失败:', error)
      return false
    }
  }

  /**
   * 检查是否有指定部门的数据权限
   * @param {number} deptId - 部门ID
   * @returns {boolean} 是否有部门权限
   */
  async hasDeptPermission(deptId) {
    try {
      const response = await request.get('/api/auth/permission/data-scope/check', {
        params: { type: 'dept', dataId: deptId }
      })
      return response.data || false
    } catch (error) {
      console.error('部门权限检查失败:', error)
      return false
    }
  }

  /**
   * 检查是否有指定个人数据的访问权限
   * @param {number} dataUserId - 数据所属用户ID
   * @returns {boolean} 是否有个人数据权限
   */
  async hasPersonalDataPermission(dataUserId) {
    try {
      const response = await request.get('/api/auth/permission/data-scope/check', {
        params: { type: 'self', dataId: dataUserId }
      })
      return response.data || false
    } catch (error) {
      console.error('个人数据权限检查失败:', error)
      return false
    }
  }

  /**
   * 过滤菜单权限
   * @param {Array} menus - 完整菜单列表
   * @returns {Array} 过滤后的菜单列表
   */
  async filterMenus(menus) {
    try {
      const response = await request.post('/api/auth/permission/filter-menus', menus)
      return response.data || []
    } catch (error) {
      console.error('菜单权限过滤失败:', error)
      return []
    }
  }

  /**
   * 获取按钮权限
   * @param {string} resource - 资源编码
   * @returns {Array} 按钮权限列表
   */
  async getButtonPermissions(resource) {
    try {
      const response = await request.get('/api/auth/permission/buttons', {
        params: { resource }
      })
      return response.data || []
    } catch (error) {
      console.error('获取按钮权限失败:', error)
      return []
    }
  }

  /**
   * 获取数据权限SQL条件
   * @param {Object} options - 配置选项
   * @returns {string} SQL WHERE条件
   */
  async getDataScopeSql(options) {
    const {
      tableName,
      userIdField = 'user_id',
      areaIdField = 'area_id',
      deptIdField = 'dept_id'
    } = options

    try {
      const response = await request.get('/api/auth/permission/data-scope/sql', {
        params: { tableName, userIdField, areaIdField, deptIdField }
      })
      return response.data?.condition || ''
    } catch (error) {
      console.error('获取数据权限SQL失败:', error)
      return ''
    }
  }

  /**
   * 刷新权限缓存
   * @param {number} userId - 用户ID（可选）
   */
  async refreshPermissionCache(userId) {
    try {
      await request.post('/api/auth/permission/refresh-cache', null, {
        params: userId ? { userId } : {}
      })
      await this.loadUserPermissions()
      this.clearCache()
      Message.success('权限缓存刷新成功')
    } catch (error) {
      console.error('权限缓存刷新失败:', error)
      Message.error('权限缓存刷新失败')
    }
  }

  /**
   * 获取权限统计信息
   * @returns {Object} 权限统计信息
   */
  async getPermissionStats() {
    try {
      const response = await request.get('/api/auth/permission/stats')
      return response.data || {}
    } catch (error) {
      console.error('获取权限统计失败:', error)
      return {}
    }
  }

  /**
   * 验证权限，如果没有权限则显示错误消息
   * @param {string} resource - 资源编码
   * @param {string} action - 动作
   * @param {string} message - 错误消息
   * @returns {boolean} 是否有权限
   */
  async checkPermissionWithMessage(resource, action = 'READ', message = '权限不足，无法执行此操作') {
    const hasPermission = await this.hasPermission(resource, action)
    if (!hasPermission) {
      Message.error(message)
    }
    return hasPermission
  }

  /**
   * 清除权限缓存
   */
  clearCache() {
    this.permissionCache.clear()
  }

  /**
   * 获取用户权限信息
   * @returns {Object} 用户权限信息
   */
  getUserPermissions() {
    return this.userPermissions
  }

  /**
   * 获取用户角色
   * @returns {Array} 用户角色列表
   */
  getUserRoles() {
    return this.userPermissions?.roles || []
  }

  /**
   * 获取可访问的区域
   * @returns {Array} 可访问的区域ID列表
   */
  getAccessibleAreas() {
    return Array.from(this.userPermissions?.accessibleAreas || [])
  }

  /**
   * 获取可访问的部门
   * @returns {Array} 可访问的部门ID列表
   */
  getAccessibleDepts() {
    return Array.from(this.userPermissions?.accessibleDepts || [])
  }

  /**
   * 检查是否为超级管理员
   * @returns {boolean} 是否为超级管理员
   */
  isSuperAdmin() {
    return this.userPermissions?.isSuperAdmin || false
  }
}

// 创建全局权限管理器实例
export const permissionManager = new PermissionManager()

// 导出便捷方法
export const {
  hasPermission,
  canRead,
  canWrite,
  canDelete,
  canApprove,
  hasAreaPermission,
  hasDeptPermission,
  hasPersonalDataPermission,
  filterMenus,
  getButtonPermissions,
  getDataScopeSql,
  checkPermissionWithMessage
} = {
  hasPermission: (resource, action) => permissionManager.hasPermission(resource, action),
  canRead: (resource) => permissionManager.canRead(resource),
  canWrite: (resource) => permissionManager.canWrite(resource),
  canDelete: (resource) => permissionManager.canDelete(resource),
  canApprove: (resource) => permissionManager.canApprove(resource),
  hasAreaPermission: (areaId) => permissionManager.hasAreaPermission(areaId),
  hasDeptPermission: (deptId) => permissionManager.hasDeptPermission(deptId),
  hasPersonalDataPermission: (dataUserId) => permissionManager.hasPersonalDataPermission(dataUserId),
  filterMenus: (menus) => permissionManager.filterMenus(menus),
  getButtonPermissions: (resource) => permissionManager.getButtonPermissions(resource),
  getDataScopeSql: (options) => permissionManager.getDataScopeSql(options),
  checkPermissionWithMessage: (resource, action, message) => permissionManager.checkPermissionWithMessage(resource, action, message)
}

export default permissionManager