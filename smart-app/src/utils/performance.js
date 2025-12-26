/**
 * 性能优化工具类
 * 用于图片懒加载、缓存管理、性能监控等
 */

// 图片缓存管理
class ImageCacheManager {
  constructor() {
    this.cache = new Map()
    this.maxCacheSize = 100 // 最大缓存数量
    this.loadingImages = new Set()
  }

  /**
   * 预加载图片
   * @param {string} url 图片URL
   * @returns {Promise}
   */
  preloadImage(url) {
    return new Promise((resolve, reject) => {
      // 如果已在缓存中，直接返回
      if (this.cache.has(url)) {
        resolve(this.cache.get(url))
        return
      }

      // 如果正在加载，返回加载中的Promise
      if (this.loadingImages.has(url)) {
        setTimeout(() => this.preloadImage(url).then(resolve).catch(reject), 100)
        return
      }

      this.loadingImages.add(url)

      const img = new Image()
      img.onload = () => {
        this.cache.set(url, url)
        this.loadingImages.delete(url)
        this.checkCacheSize()
        resolve(url)
      }
      img.onerror = () => {
        this.loadingImages.delete(url)
        reject(new Error(`图片加载失败: ${url}`))
      }
      img.src = url
    })
  }

  /**
   * 检查缓存大小，清理最老的缓存
   */
  checkCacheSize() {
    if (this.cache.size > this.maxCacheSize) {
      const firstKey = this.cache.keys().next().value
      this.cache.delete(firstKey)
    }
  }

  /**
   * 清理缓存
   */
  clearCache() {
    this.cache.clear()
    this.loadingImages.clear()
  }
}

// 性能监控
class PerformanceMonitor {
  constructor() {
    this.metrics = {
      pageLoadTime: 0,
      apiResponseTimes: [],
      imageLoadTimes: [],
      errorCount: 0
    }
    this.startTime = Date.now()
  }

  /**
   * 记录页面加载时间
   */
  recordPageLoadTime() {
    this.metrics.pageLoadTime = Date.now() - this.startTime
    console.log(`页面加载时间: ${this.metrics.pageLoadTime}ms`)
    return this.metrics.pageLoadTime
  }

  /**
   * 记录API响应时间
   * @param {string} api API名称
   * @param {number} startTime 开始时间
   */
  recordApiResponseTime(api, startTime) {
    const duration = Date.now() - startTime
    this.metrics.apiResponseTimes.push({
      api,
      duration,
      timestamp: Date.now()
    })

    // 如果响应时间过长，记录警告
    if (duration > 3000) {
      console.warn(`API响应时间过长: ${api} - ${duration}ms`)
    }

    return duration
  }

  /**
   * 记录图片加载时间
   * @param {string} url 图片URL
   * @param {number} startTime 开始时间
   */
  recordImageLoadTime(url, startTime) {
    const duration = Date.now() - startTime
    this.metrics.imageLoadTimes.push({
      url,
      duration,
      timestamp: Date.now()
    })
    return duration
  }

  /**
   * 记录错误
   * @param {Error} error 错误对象
   */
  recordError(error) {
    this.metrics.errorCount++
    console.error(`性能监控错误 #${this.metrics.errorCount}:`, error)
  }

  /**
   * 获取性能报告
   */
  getPerformanceReport() {
    const avgApiResponseTime = this.metrics.apiResponseTimes.length > 0
      ? this.metrics.apiResponseTimes.reduce((sum, item) => sum + item.duration, 0) / this.metrics.apiResponseTimes.length
      : 0

    const avgImageLoadTime = this.metrics.imageLoadTimes.length > 0
      ? this.metrics.imageLoadTimes.reduce((sum, item) => sum + item.duration, 0) / this.metrics.imageLoadTimes.length
      : 0

    return {
      pageLoadTime: this.metrics.pageLoadTime,
      avgApiResponseTime: Math.round(avgApiResponseTime),
      avgImageLoadTime: Math.round(avgImageLoadTime),
      totalApiCalls: this.metrics.apiResponseTimes.length,
      totalImageLoads: this.metrics.imageLoadTimes.length,
      errorCount: this.metrics.errorCount
    }
  }
}

// 防抖函数
function debounce(func, wait) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

// 节流函数
function throttle(func, limit) {
  let inThrottle
  return function() {
    const args = arguments
    const context = this
    if (!inThrottle) {
      func.apply(context, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 懒加载指令
const lazyLoad = {
  mounted(el, binding) {
    const imageUrl = binding.value

    // 创建 Intersection Observer
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = new Image()
          img.onload = () => {
            el.src = imageUrl
            el.classList.add('loaded')
          }
          img.onerror = () => {
            el.src = '/static/images/placeholder.png'
            el.classList.add('error')
          }
          img.src = imageUrl
          observer.unobserve(el)
        }
      })
    }, {
      rootMargin: '50px' // 提前50px开始加载
    })

    observer.observe(el)
  }
}

// 创建全局实例
const imageCacheManager = new ImageCacheManager()
const performanceMonitor = new PerformanceMonitor()

export {
  ImageCacheManager,
  PerformanceMonitor,
  imageCacheManager,
  performanceMonitor,
  debounce,
  throttle,
  lazyLoad
}

// ==================== 新增性能优化工具导出 ====================

// 防抖节流（增强版）
export {
  debounce as debounceEnhanced,
  throttle as throttleEnhanced,
  debounceDirective,
  throttleDirective,
  useDebounce,
  useThrottle
} from './performance-debounce.js'

// 请求缓存
export {
  requestCache,
  cached,
  createCachedRequest,
  useRequestCache
} from './performance-cache.js'

// 性能监控（增强版）
export {
  performanceMonitor as performanceMonitorEnhanced,
  usePerformanceMonitor,
  autoMonitor
} from './performance-monitor.js'