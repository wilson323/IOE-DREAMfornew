/**
 * 请求缓存模块
 * 用于缓存API请求结果，减少重复请求
 */

/**
 * 缓存配置
 */
const CACHE_CONFIG = {
  defaultTTL: 5 * 60 * 1000,      // 默认缓存5分钟
  maxSize: 100,                   // 最大缓存数量
  enableDebug: false              // 调试模式
}

/**
 * 缓存项类
 */
class CacheItem {
  constructor(data, ttl = CACHE_CONFIG.defaultTTL) {
    this.data = data
    this.createTime = Date.now()
    this.expireTime = this.createTime + ttl
    this.hits = 0                  // 命中次数
  }

  /**
   * 检查是否过期
   */
  isExpired() {
    return Date.now() > this.expireTime
  }

  /**
   * 获取缓存数据
   */
  get() {
    this.hits++
    return this.data
  }
}

/**
 * 请求缓存管理类
 */
class RequestCache {
  constructor() {
    this.cache = new Map()
    this.maxSize = CACHE_CONFIG.maxSize
  }

  /**
   * 生成缓存键
   * @param {String} url 请求URL
   * @param {Object} params 请求参数
   * @returns {String}
   */
  generateKey(url, params = {}) {
    const paramStr = JSON.stringify(params)
    return `${url}:${paramStr}`
  }

  /**
   * 设置缓存
   * @param {String} url 请求URL
   * @param {Object} params 请求参数
   * @param {*} data 缓存数据
   * @param {Number} ttl 过期时间(ms)
   */
  set(url, params, data, ttl) {
    const key = this.generateKey(url, params)

    // 检查缓存大小
    if (this.cache.size >= this.maxSize) {
      this.evictOldest()
    }

    const cacheItem = new CacheItem(data, ttl)
    this.cache.set(key, cacheItem)

    if (CACHE_CONFIG.enableDebug) {
      console.log(`[请求缓存] 设置缓存: ${key}`)
    }
  }

  /**
   * 获取缓存
   * @param {String} url 请求URL
   * @param {Object} params 请求参数
   * @returns {*} 缓存数据或null
   */
  get(url, params) {
    const key = this.generateKey(url, params)
    const cacheItem = this.cache.get(key)

    if (!cacheItem) {
      if (CACHE_CONFIG.enableDebug) {
        console.log(`[请求缓存] 未命中: ${key}`)
      }
      return null
    }

    if (cacheItem.isExpired()) {
      this.cache.delete(key)
      if (CACHE_CONFIG.enableDebug) {
        console.log(`[请求缓存] 已过期: ${key}`)
      }
      return null
    }

    if (CACHE_CONFIG.enableDebug) {
      console.log(`[请求缓存] 命中: ${key}, 命中次数: ${cacheItem.hits}`)
    }

    return cacheItem.get()
  }

  /**
   * 删除缓存
   * @param {String} url 请求URL
   * @param {Object} params 请求参数
   */
  delete(url, params) {
    const key = this.generateKey(url, params)
    this.cache.delete(key)
  }

  /**
   * 清空所有缓存
   */
  clear() {
    this.cache.clear()
    console.log('[请求缓存] 清空所有缓存')
  }

  /**
   * 淘汰最旧的缓存项（LRU策略）
   */
  evictOldest() {
    let oldestKey = null
    let oldestTime = Date.now()

    for (const [key, item] of this.cache.entries()) {
      if (item.createTime < oldestTime) {
        oldestTime = item.createTime
        oldestKey = key
      }
    }

    if (oldestKey) {
      this.cache.delete(oldestKey)
      if (CACHE_CONFIG.enableDebug) {
        console.log(`[请求缓存] 淘汰缓存: ${oldestKey}`)
      }
    }
  }

  /**
   * 清理过期缓存
   */
  cleanExpired() {
    let cleaned = 0
    for (const [key, item] of this.cache.entries()) {
      if (item.isExpired()) {
        this.cache.delete(key)
        cleaned++
      }
    }

    if (cleaned > 0) {
      console.log(`[请求缓存] 清理过期缓存: ${cleaned}条`)
    }

    return cleaned
  }

  /**
   * 获取缓存统计
   */
  getStats() {
    let totalHits = 0
    let expired = 0

    for (const item of this.cache.values()) {
      totalHits += item.hits
      if (item.isExpired()) {
        expired++
      }
    }

    return {
      size: this.cache.size,
      maxSize: this.maxSize,
      totalHits,
      expired,
      usagePercent: ((this.cache.size / this.maxSize) * 100).toFixed(2)
    }
  }

  /**
   * 根据URL模式批量删除缓存
   * @param {String} pattern URL模式
   */
  deleteByPattern(pattern) {
    let deleted = 0

    for (const key of this.cache.keys()) {
      if (key.includes(pattern)) {
        this.cache.delete(key)
        deleted++
      }
    }

    console.log(`[请求缓存] 批量删除: ${pattern}, ${deleted}条`)
    return deleted
  }
}

// 导出单例
export const requestCache = new RequestCache()

/**
 * 缓存装饰器
 * @param {Number} ttl 缓存时间(ms)
 * @returns {Function}
 */
export function cached(ttl = CACHE_CONFIG.defaultTTL) {
  return function (target, propertyKey, descriptor) {
    const originalMethod = descriptor.value

    descriptor.value = async function (...args) {
      const url = args[0]
      const params = args[1] || {}

      // 尝试从缓存获取
      const cachedData = requestCache.get(url, params)
      if (cachedData !== null) {
        return cachedData
      }

      // 执行原方法
      const result = await originalMethod.apply(this, args)

      // 缓存结果
      if (result && result.success) {
        requestCache.set(url, params, result, ttl)
      }

      return result
    }

    return descriptor
  }
}

/**
 * 创建带缓存的请求函数
 * @param {Function} requestFn 原始请求函数
 * @param {Number} ttl 缓存时间
 * @returns {Function}
 */
export function createCachedRequest(requestFn, ttl = CACHE_CONFIG.defaultTTL) {
  return async function (url, params, options = {}) {
    const { skipCache = false } = options

    // 跳过缓存
    if (skipCache) {
      return requestFn(url, params)
    }

    // 尝试从缓存获取
    const cachedData = requestCache.get(url, params)
    if (cachedData !== null) {
      return cachedData
    }

    // 执行请求
    const result = await requestFn(url, params)

    // 缓存结果
    if (result && result.success) {
      requestCache.set(url, params, result, ttl)
    }

    return result
  }
}

/**
 * Vue 3 组合式函数 - 使用请求缓存
 * @param {Function} requestFn 请求函数
 * @param {Number} ttl 缓存时间
 * @returns {Object}
 */
export function useRequestCache(requestFn, ttl = CACHE_CONFIG.defaultTTL) {
  const cachedRequest = createCachedRequest(requestFn, ttl)

  const stats = computed(() => requestCache.getStats())

  const clear = () => requestCache.clear()

  const cleanExpired = () => requestCache.cleanExpired()

  return {
    request: cachedRequest,
    stats,
    clear,
    cleanExpired
  }
}

/**
 * 定时清理过期缓存
 */
setInterval(() => {
  requestCache.cleanExpired()
}, 60000)  // 每分钟清理一次

export default {
  requestCache,
  cached,
  createCachedRequest,
  useRequestCache
}
