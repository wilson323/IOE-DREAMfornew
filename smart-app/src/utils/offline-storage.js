/**
 * 离线存储封装模块
 * 提供 uni.storage 的 Promise 封装和类型安全
 */

// 存储键名常量
export const STORAGE_KEYS = {
  // 权限数据
  PERMISSION_LIST: 'offline:permission:list',
  PERMISSION_DETAIL: 'offline:permission:detail',
  PERMISSION_TIMESTAMP: 'offline:permission:timestamp',

  // 通行记录
  PASS_RECORDS: 'offline:pass:records',
  PENDING_RECORDS: 'offline:pass:pending',

  // 同步状态
  SYNC_STATUS: 'offline:sync:status',
  LAST_SYNC_TIME: 'offline:sync:last_time',
  SYNC_QUEUE: 'offline:sync:queue',

  // 用户信息
  USER_INFO: 'offline:user:info',
  DEVICE_INFO: 'offline:device:info',

  // 设置
  OFFLINE_SETTINGS: 'offline:settings'
}

/**
 * 离线存储管理类
 */
class OfflineStorage {
  constructor() {
    this.cache = new Map() // 内存缓存
  }

  /**
   * 获取数据
   * @param {String} key 存储键
   * @param {*} defaultValue 默认值
   * @param {Boolean} useCache 是否使用缓存
   * @returns {Promise<*>}
   */
  async get(key, defaultValue = null, useCache = false) {
    try {
      // 先检查内存缓存
      if (useCache && this.cache.has(key)) {
        console.log(`[离线存储] 从缓存获取: ${key}`)
        return this.cache.get(key)
      }

      // 从本地存储获取
      const data = await uni.getStorageSync(key)
      const result = data ? JSON.parse(data) : defaultValue

      // 更新内存缓存
      if (useCache && result !== null) {
        this.cache.set(key, result)
      }

      console.log(`[离线存储] 获取数据: ${key}`, result)
      return result
    } catch (error) {
      console.error(`[离线存储] 获取失败: ${key}`, error)
      return defaultValue
    }
  }

  /**
   * 保存数据
   * @param {String} key 存储键
   * @param {*} data 数据
   * @param {Boolean} useCache 是否更新缓存
   * @returns {Promise<Boolean>}
   */
  async set(key, data, useCache = true) {
    try {
      const json = JSON.stringify(data)

      // 保存到本地存储
      await uni.setStorageSync(key, json)

      // 更新内存缓存
      if (useCache) {
        this.cache.set(key, data)
      }

      console.log(`[离线存储] 保存数据: ${key}`, data)
      return true
    } catch (error) {
      console.error(`[离线存储] 保存失败: ${key}`, error)
      return false
    }
  }

  /**
   * 删除数据
   * @param {String} key 存储键
   * @returns {Promise<Boolean>}
   */
  async remove(key) {
    try {
      await uni.removeStorageSync(key)
      this.cache.delete(key)
      console.log(`[离线存储] 删除数据: ${key}`)
      return true
    } catch (error) {
      console.error(`[离线存储] 删除失败: ${key}`, error)
      return false
    }
  }

  /**
   * 清空所有离线数据
   * @returns {Promise<Boolean>}
   */
  async clear() {
    try {
      const keys = Object.values(STORAGE_KEYS)
      for (const key of keys) {
        await uni.removeStorageSync(key)
        this.cache.delete(key)
      }
      console.log('[离线存储] 清空所有数据')
      return true
    } catch (error) {
      console.error('[离线存储] 清空失败', error)
      return false
    }
  }

  /**
   * 检查键是否存在
   * @param {String} key 存储键
   * @returns {Promise<Boolean>}
   */
  async has(key) {
    try {
      const data = await uni.getStorageSync(key)
      return data !== ''
    } catch (error) {
      return false
    }
  }

  /**
   * 获取存储信息
   * @returns {Promise<Object>}
   */
  async getInfo() {
    try {
      const info = await uni.getStorageInfo()
      return {
        keys: info.keys,
        currentSize: info.currentSize,
        limitSize: info.limitSize,
        usagePercent: ((info.currentSize / info.limitSize) * 100).toFixed(2)
      }
    } catch (error) {
      console.error('[离线存储] 获取存储信息失败', error)
      return null
    }
  }

  /**
   * 批量获取
   * @param {Array<String>} keys 键数组
   * @returns {Promise<Object>}
   */
  async batchGet(keys) {
    const result = {}
    for (const key of keys) {
      result[key] = await this.get(key)
    }
    return result
  }

  /**
   * 批量保存
   * @param {Object} data 键值对对象
   * @returns {Promise<Boolean>}
   */
  async batchSet(data) {
    const promises = Object.entries(data).map(([key, value]) =>
      this.set(key, value)
    )
    try {
      await Promise.all(promises)
      return true
    } catch (error) {
      console.error('[离线存储] 批量保存失败', error)
      return false
    }
  }

  /**
   * 清除内存缓存
   */
  clearCache() {
    this.cache.clear()
    console.log('[离线存储] 清除内存缓存')
  }

  /**
   * 获取缓存大小
   * @returns {Number}
   */
  getCacheSize() {
    return this.cache.size
  }
}

// 导出单例
export const offlineStorage = new OfflineStorage()

// 导出工具函数
export const storage = {
  get: (key, defaultValue) => offlineStorage.get(key, defaultValue, true),
  set: (key, data) => offlineStorage.set(key, data, true),
  remove: (key) => offlineStorage.remove(key),
  clear: () => offlineStorage.clear(),
  has: (key) => offlineStorage.has(key),
  getInfo: () => offlineStorage.getInfo(),
  batchGet: (keys) => offlineStorage.batchGet(keys),
  batchSet: (data) => offlineStorage.batchSet(data)
}

export default offlineStorage
