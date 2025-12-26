/**
 * 权限数据离线同步模块
 * 负责权限数据的本地缓存、增量更新、离线验证
 */

import { storage, STORAGE_KEYS } from './offline-storage.js'
import { offlineSync } from './offline-sync.js'

/**
 * 权限状态枚举
 */
export const PERMISSION_STATUS = {
  VALID: 1,          // 有效
  EXPIRING: 2,       // 即将过期
  EXPIRED: 3,        // 已过期
  FROZEN: 4          // 已冻结
}

/**
 * 权限类型枚举
 */
export const PERMISSION_TYPE = {
  PERMANENT: 1,      // 永久权限
  TEMPORARY: 2,      // 临时权限
  TIME_BASED: 3      // 时段权限
}

/**
 * 权限数据管理类
 */
class OfflinePermission {
  constructor() {
    this.cache = new Map()  // 内存缓存
    this.cacheTimeout = 5 * 60 * 1000  // 缓存5分钟
  }

  /**
   * 初始化权限数据
   */
  async init() {
    console.log('[权限离线] 初始化权限数据')

    // 从本地存储加载权限列表
    await this.loadFromStorage()

    // 监听同步完成事件
    offlineSync.on('syncComplete', (data) => {
      console.log('[权限离线] 同步完成，更新权限数据')
      this.handleSyncComplete(data)
    })
  }

  /**
   * 从本地存储加载权限列表
   */
  async loadFromStorage() {
    try {
      const permissions = await storage.get(STORAGE_KEYS.PERMISSION_LIST, [])
      console.log(`[权限离线] 加载 ${permissions.length} 条权限数据`)

      this.cache.clear()
      permissions.forEach(p => {
        this.cache.set(p.permissionId, p)
      })

      return permissions
    } catch (error) {
      console.error('[权限离线] 加载失败:', error)
      return []
    }
  }

  /**
   * 保存权限列表到本地
   * @param {Array} permissions 权限列表
   */
  async saveToStorage(permissions) {
    try {
      await storage.set(STORAGE_KEYS.PERMISSION_LIST, permissions)
      await storage.set(STORAGE_KEYS.PERMISSION_TIMESTAMP, Date.now())

      // 更新内存缓存
      this.cache.clear()
      permissions.forEach(p => {
        this.cache.set(p.permissionId, p)
      })

      console.log(`[权限离线] 保存 ${permissions.length} 条权限数据`)
      return true
    } catch (error) {
      console.error('[权限离线] 保存失败:', error)
      return false
    }
  }

  /**
   * 获取权限列表
   * @param {Object} filters 过滤条件
   * @returns {Promise<Array>}
   */
  async getPermissionList(filters = {}) {
    try {
      // 先从本地缓存获取
      let permissions = await storage.get(STORAGE_KEYS.PERMISSION_LIST, [])

      // 应用过滤条件
      if (filters.status) {
        permissions = permissions.filter(p => p.status === filters.status)
      }
      if (filters.type) {
        permissions = permissions.filter(p => p.permissionType === filters.type)
      }
      if (filters.areaId) {
        permissions = permissions.filter(p =>
          p.areaIds && p.areaIds.includes(filters.areaId)
        )
      }

      console.log(`[权限离线] 获取权限列表: ${permissions.length} 条`)
      return permissions
    } catch (error) {
      console.error('[权限离线] 获取权限列表失败:', error)
      return []
    }
  }

  /**
   * 获取权限详情
   * @param {Number} permissionId 权限ID
   * @returns {Promise<Object|null>}
   */
  async getPermissionDetail(permissionId) {
    try {
      // 先从内存缓存获取
      if (this.cache.has(permissionId)) {
        console.log(`[权限离线] 从缓存获取权限: ${permissionId}`)
        return this.cache.get(permissionId)
      }

      // 从本地存储获取
      const permissions = await storage.get(STORAGE_KEYS.PERMISSION_LIST, [])
      const permission = permissions.find(p => p.permissionId === permissionId)

      if (permission) {
        // 更新缓存
        this.cache.set(permissionId, permission)
        console.log(`[权限离线] 获取权限详情: ${permissionId}`)
      }

      return permission || null
    } catch (error) {
      console.error(`[权限离线] 获取权限详情失败: ${permissionId}`, error)
      return null
    }
  }

  /**
   * 验证权限是否有效
   * @param {Number} permissionId 权限ID
   * @param {Number} areaId 区域ID
   * @returns {Promise<Object>}
   */
  async validatePermission(permissionId, areaId = null) {
    try {
      const permission = await this.getPermissionDetail(permissionId)

      if (!permission) {
        return {
          valid: false,
          reason: '权限不存在'
        }
      }

      // 检查状态
      if (permission.status === PERMISSION_STATUS.EXPIRED) {
        return {
          valid: false,
          reason: '权限已过期'
        }
      }

      if (permission.status === PERMISSION_STATUS.FROZEN) {
        return {
          valid: false,
          reason: '权限已冻结'
        }
      }

      // 检查有效期
      if (permission.expireTime) {
        const now = Date.now()
        const expireTime = new Date(permission.expireTime).getTime()
        if (now > expireTime) {
          return {
            valid: false,
            reason: '权限已过期'
          }
        }
      }

      // 检查区域权限
      if (areaId && permission.areaIds) {
        if (!permission.areaIds.includes(areaId)) {
          return {
            valid: false,
            reason: '无该区域通行权限'
          }
        }
      }

      // 检查时段权限（如果有）
      if (permission.permissionType === PERMISSION_TYPE.TIME_BASED) {
        const now = new Date()
        const currentHour = now.getHours()
        const currentMinute = now.getMinutes()
        const currentTime = currentHour * 60 + currentMinute

        // 检查是否在允许的时间段内
        // TODO: 根据permission的timeSlots验证
      }

      return {
        valid: true,
        permission
      }
    } catch (error) {
      console.error(`[权限离线] 验证权限失败: ${permissionId}`, error)
      return {
        valid: false,
        reason: '验证失败'
      }
    }
  }

  /**
   * 生成离线二维码
   * @param {Number} permissionId 权限ID
   * @returns {Promise<Object>}
   */
  async generateOfflineQRCode(permissionId) {
    try {
      const permission = await this.getPermissionDetail(permissionId)

      if (!permission) {
        throw new Error('权限不存在')
      }

      // 验证权限有效性
      const validation = await this.validatePermission(permissionId)
      if (!validation.valid) {
        throw new Error(validation.reason)
      }

      // 生成离线二维码数据
      const qrCodeData = {
        permissionId: permission.permissionId,
        userId: permission.userId,
        areaIds: permission.areaIds,
        expireTime: permission.expireTime,
        timestamp: Date.now(),
        signature: this.generateSignature(permission)
      }

      // TODO: 调用二维码生成库
      // const qrcode = await QRCode.toDataURL(JSON.stringify(qrCodeData))

      console.log(`[权限离线] 生成离线二维码: ${permissionId}`)
      return {
        success: true,
        data: qrCodeData,
        // qrcode: qrcode
      }
    } catch (error) {
      console.error(`[权限离线] 生成二维码失败: ${permissionId}`, error)
      return {
        success: false,
        message: error.message
      }
    }
  }

  /**
   * 生成签名（用于离线验证）
   * @param {Object} permission 权限对象
   * @returns {String}
   */
  generateSignature(permission) {
    // TODO: 使用实际签名算法（如HMAC-SHA256）
    const data = `${permission.permissionId}:${permission.userId}:${permission.expireTime}`
    return btoa(data)  // 简单Base64编码，实际应使用加密算法
  }

  /**
   * 更新权限数据（增量更新）
   * @param {Array} updates 更新的权限列表
   */
  async updatePermissions(updates) {
    try {
      console.log(`[权限离线] 更新 ${updates.length} 条权限数据`)

      let permissions = await storage.get(STORAGE_KEYS.PERMISSION_LIST, [])

      // 更新或添加权限
      updates.forEach(update => {
        const index = permissions.findIndex(p => p.permissionId === update.permissionId)
        if (index > -1) {
          permissions[index] = update
        } else {
          permissions.push(update)
        }

        // 更新缓存
        this.cache.set(update.permissionId, update)
      })

      // 保存到本地
      await this.saveToStorage(permissions)

      return { success: true, updated: updates.length }
    } catch (error) {
      console.error('[权限离线] 更新权限失败:', error)
      return { success: false, message: error.message }
    }
  }

  /**
   * 删除权限
   * @param {Number} permissionId 权限ID
   */
  async deletePermission(permissionId) {
    try {
      let permissions = await storage.get(STORAGE_KEYS.PERMISSION_LIST, [])

      permissions = permissions.filter(p => p.permissionId !== permissionId)

      await this.saveToStorage(permissions)

      // 从缓存中删除
      this.cache.delete(permissionId)

      console.log(`[权限离线] 删除权限: ${permissionId}`)
      return { success: true }
    } catch (error) {
      console.error(`[权限离线] 删除权限失败: ${permissionId}`, error)
      return { success: false, message: error.message }
    }
  }

  /**
   * 获取权限统计
   * @returns {Promise<Object>}
   */
  async getStatistics() {
    try {
      const permissions = await storage.get(STORAGE_KEYS.PERMISSION_LIST, [])

      const stats = {
        total: permissions.length,
        valid: 0,
        expiring: 0,
        expired: 0,
        frozen: 0,
        byType: {}
      }

      const now = Date.now()

      permissions.forEach(p => {
        // 状态统计
        if (p.status === PERMISSION_STATUS.VALID) {
          stats.valid++
        } else if (p.status === PERMISSION_STATUS.EXPIRING) {
          stats.expiring++
        } else if (p.status === PERMISSION_STATUS.EXPIRED) {
          stats.expired++
        } else if (p.status === PERMISSION_STATUS.FROZEN) {
          stats.frozen++
        }

        // 类型统计
        const type = p.permissionType
        if (!stats.byType[type]) {
          stats.byType[type] = 0
        }
        stats.byType[type]++
      })

      console.log('[权限离线] 权限统计:', stats)
      return stats
    } catch (error) {
      console.error('[权限离线] 统计失败:', error)
      return null
    }
  }

  /**
   * 处理同步完成事件
   * @param {Object} data 同步数据
   */
  async handleSyncComplete(data) {
    console.log('[权限离线] 处理同步完成:', data)

    // 更新权限数据
    if (data.download && data.download.permission > 0) {
      // TODO: 从同步结果中提取权限数据并更新
    }
  }

  /**
   * 清除过期权限
   */
  async clearExpired() {
    try {
      let permissions = await storage.get(STORAGE_KEYS.PERMISSION_LIST, [])
      const now = Date.now()

      const before = permissions.length
      permissions = permissions.filter(p => {
        if (p.expireTime) {
          const expireTime = new Date(p.expireTime).getTime()
          return expireTime > now
        }
        return true
      })

      await this.saveToStorage(permissions)

      console.log(`[权限离线] 清除过期权限: ${before} -> ${permissions.length}`)
      return { success: true, cleared: before - permissions.length }
    } catch (error) {
      console.error('[权限离线] 清除过期权限失败:', error)
      return { success: false, message: error.message }
    }
  }
}

// 导出单例
export const offlinePermission = new OfflinePermission()

// 导出工具函数
export const permission = {
  init: () => offlinePermission.init(),
  getList: (filters) => offlinePermission.getPermissionList(filters),
  getDetail: (id) => offlinePermission.getPermissionDetail(id),
  validate: (id, areaId) => offlinePermission.validatePermission(id, areaId),
  generateQRCode: (id) => offlinePermission.generateOfflineQRCode(id),
  update: (updates) => offlinePermission.updatePermissions(updates),
  delete: (id) => offlinePermission.deletePermission(id),
  getStatistics: () => offlinePermission.getStatistics(),
  clearExpired: () => offlinePermission.clearExpired()
}

export default offlinePermission
