/**
 * 缓存管理器
 * 提供统一的缓存操作接口，支持过期时间管理
 */

class CacheManager {
  constructor() {
    this.CACHE_PREFIX = 'ioedream_cache_'
    this.CACHE_VERSION_KEY = 'cache_version'
    this.CACHE_VERSION = '1.0.0'
  }

  /**
   * 生成缓存key
   * @param {string} key - 原始key
   * @returns {string} 带前缀的缓存key
   */
  getCacheKey(key) {
    return `${this.CACHE_PREFIX}${key}`
  }

  /**
   * 设置缓存
   * @param {string} key - 缓存key
   * @param {*} data - 缓存数据
   * @param {number} expireTime - 过期时间（毫秒），默认1小时
   */
  setCache(key, data, expireTime = 3600000) {
    try {
      const cacheData = {
        data,
        timestamp: Date.now(),
        expireTime,
        version: this.CACHE_VERSION
      }
      uni.setStorageSync(this.getCacheKey(key), JSON.stringify(cacheData))
      console.log(`[缓存] 设置成功: ${key}, 过期时间: ${expireTime}ms`)
    } catch (error) {
      console.error(`[缓存] 设置失败: ${key}`, error)
    }
  }

  /**
   * 获取缓存
   * @param {string} key - 缓存key
   * @param {boolean} checkVersion - 是否检查版本，默认true
   * @returns {*} 缓存数据，如果不存在或已过期返回null
   */
  getCache(key, checkVersion = true) {
    try {
      const cacheStr = uni.getStorageSync(this.getCacheKey(key))
      if (!cacheStr) return null

      const cache = JSON.parse(cacheStr)

      // 检查版本
      if (checkVersion && cache.version !== this.CACHE_VERSION) {
        console.log(`[缓存] 版本不匹配，清除缓存: ${key}`)
        this.removeCache(key)
        return null
      }

      // 检查是否过期
      const now = Date.now()
      if (now - cache.timestamp > cache.expireTime) {
        console.log(`[缓存] 已过期，清除缓存: ${key}`)
        this.removeCache(key)
        return null
      }

      console.log(`[缓存] 命中: ${key}, 剩余有效时间: ${cache.expireTime - (now - cache.timestamp)}ms`)
      return cache.data
    } catch (error) {
      console.error(`[缓存] 获取失败: ${key}`, error)
      return null
    }
  }

  /**
   * 移除缓存
   * @param {string} key - 缓存key
   */
  removeCache(key) {
    try {
      uni.removeStorageSync(this.getCacheKey(key))
      console.log(`[缓存] 已移除: ${key}`)
    } catch (error) {
      console.error(`[缓存] 移除失败: ${key}`, error)
    }
  }

  /**
   * 清空所有缓存
   */
  clearAllCache() {
    try {
      const { keys } = uni.getStorageInfoSync()
      keys.forEach(key => {
        if (key.startsWith(this.CACHE_PREFIX)) {
          uni.removeStorageSync(key)
        }
      })
      console.log('[缓存] 已清空所有缓存')
    } catch (error) {
      console.error('[缓存] 清空失败', error)
    }
  }

  /**
   * 获取缓存大小（字节）
   * @returns {number} 缓存大小
   */
  getCacheSize() {
    try {
      const { keys } = uni.getStorageInfoSync()
      let totalSize = 0
      keys.forEach(key => {
        if (key.startsWith(this.CACHE_PREFIX)) {
          const value = uni.getStorageSync(key)
          totalSize += new Blob([value]).size
        }
      })
      return totalSize
    } catch (error) {
      console.error('[缓存] 获取大小失败', error)
      return 0
    }
  }

  /**
   * 检查缓存是否存在且有效
   * @param {string} key - 缓存key
   * @returns {boolean} 是否有效
   */
  isCacheValid(key) {
    return this.getCache(key) !== null
  }

  /**
   * 刷新缓存（更新时间戳，延长有效期）
   * @param {string} key - 缓存key
   * @param {number} expireTime - 新的过期时间（毫秒）
   */
  refreshCache(key, expireTime) {
    const data = this.getCache(key)
    if (data) {
      this.setCache(key, data, expireTime)
    }
  }

  /**
   * 批量设置缓存
   * @param {Object} cacheMap - 缓存键值对 { key: { data, expireTime } }
   */
  setCacheBatch(cacheMap) {
    Object.keys(cacheMap).forEach(key => {
      const { data, expireTime } = cacheMap[key]
      this.setCache(key, data, expireTime)
    })
  }

  /**
   * 批量获取缓存
   * @param {string[]} keys - 缓存key数组
   * @returns {Object} 缓存数据对象 { key: data }
   */
  getCacheBatch(keys) {
    const result = {}
    keys.forEach(key => {
      result[key] = this.getCache(key)
    })
    return result
  }
}

// 导出单例
const cacheManager = new CacheManager()

export default cacheManager
