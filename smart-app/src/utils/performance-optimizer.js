/**
 * 移动端性能优化工具集
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

/**
 * 图片懒加载管理器
 */
export class ImageLazyLoader {
  constructor() {
    this.loadedImages = new Set()
    this.observer = null
    this.initObserver()
  }

  initObserver() {
    // #ifdef H5
    if ('IntersectionObserver' in window) {
      this.observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.loadImage(entry.target)
          }
        })
      }, {
        rootMargin: '50px'
      })
    }
    // #endif
  }

  loadImage(imgElement) {
    const src = imgElement.dataset.src
    if (src && !this.loadedImages.has(src)) {
      imgElement.src = src
      this.loadedImages.add(src)
      this.observer?.unobserve(imgElement)
    }
  }

  observe(imgElement) {
    if (this.observer) {
      this.observer.observe(imgElement)
    }
  }
}

/**
 * 请求缓存管理器
 */
export class RequestCacheManager {
  constructor(options = {}) {
    this.cache = new Map()
    this.maxSize = options.maxSize || 100
    this.defaultTTL = options.defaultTTL || 5 * 60 * 1000  // 5分钟
  }

  /**
   * 生成缓存key
   */
  generateKey(url, params) {
    return `${url}:${JSON.stringify(params || {})}`
  }

  /**
   * 获取缓存
   */
  get(url, params) {
    const key = this.generateKey(url, params)
    const item = this.cache.get(key)
    
    if (!item) return null
    
    // 检查是否过期
    if (Date.now() > item.expireTime) {
      this.cache.delete(key)
      return null
    }
    
    return item.data
  }

  /**
   * 设置缓存
   */
  set(url, params, data, ttl) {
    const key = this.generateKey(url, params)
    
    // 检查缓存大小
    if (this.cache.size >= this.maxSize) {
      // 删除最早的缓存
      const firstKey = this.cache.keys().next().value
      this.cache.delete(firstKey)
    }
    
    this.cache.set(key, {
      data,
      expireTime: Date.now() + (ttl || this.defaultTTL)
    })
  }

  /**
   * 清除缓存
   */
  clear(url, params) {
    if (url) {
      const key = this.generateKey(url, params)
      this.cache.delete(key)
    } else {
      this.cache.clear()
    }
  }

  /**
   * 清除所有缓存
   */
  clearAll() {
    this.cache.clear()
  }
}

/**
 * 请求去重管理器
 */
export class RequestDeduplicator {
  constructor() {
    this.pendingRequests = new Map()
  }

  /**
   * 执行请求（自动去重）
   */
  async request(key, requestFn) {
    // 如果已有相同请求在进行中，返回相同的Promise
    if (this.pendingRequests.has(key)) {
      return this.pendingRequests.get(key)
    }

    // 创建新请求
    const promise = requestFn()
      .finally(() => {
        // 请求完成后删除
        this.pendingRequests.delete(key)
      })

    this.pendingRequests.set(key, promise)
    return promise
  }
}

/**
 * 图片压缩工具
 */
export const compressImage = (filePath, options = {}) => {
  return new Promise((resolve, reject) => {
    const quality = options.quality || 80
    const maxWidth = options.maxWidth || 1200
    const maxHeight = options.maxHeight || 1200

    uni.compressImage({
      src: filePath,
      quality,
      width: maxWidth,
      height: maxHeight,
      success: (res) => {
        resolve(res.tempFilePath)
      },
      fail: (error) => {
        reject(error)
      }
    })
  })
}

/**
 * 批量图片压缩
 */
export const batchCompressImages = async (filePaths, options = {}) => {
  const results = []
  
  for (const filePath of filePaths) {
    try {
      const compressed = await compressImage(filePath, options)
      results.push(compressed)
    } catch (error) {
      console.error('图片压缩失败:', filePath, error)
      results.push(filePath)  // 失败时使用原图
    }
  }
  
  return results
}

/**
 * 节流函数
 */
export const throttle = (fn, delay = 300) => {
  let timer = null
  let lastTime = 0

  return function (...args) {
    const now = Date.now()

    if (now - lastTime < delay) {
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => {
        lastTime = now
        fn.apply(this, args)
      }, delay - (now - lastTime))
    } else {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

/**
 * 防抖函数
 */
export const debounce = (fn, delay = 300) => {
  let timer = null

  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 代码分割 - 动态导入组件
 */
export const loadComponent = async (componentPath) => {
  try {
    const component = await import(componentPath)
    return component.default || component
  } catch (error) {
    console.error('组件加载失败:', componentPath, error)
    return null
  }
}

/**
 * 预加载关键资源
 */
export const preloadResources = (resources = []) => {
  resources.forEach(resource => {
    if (resource.type === 'image') {
      const img = new Image()
      img.src = resource.url
    } else if (resource.type === 'api') {
      // 预加载API数据
      uni.request({
        url: resource.url,
        method: resource.method || 'GET',
        success: () => {
          console.log('资源预加载成功:', resource.url)
        }
      })
    }
  })
}

/**
 * 内存监控
 */
export const monitorMemory = () => {
  // #ifdef APP-PLUS
  const memory = plus.runtime.getProperty(plus.runtime.appid, (info) => {
    console.log('应用内存使用情况:', {
      usedMemory: info.usedMemory,
      availableMemory: info.availableMemory,
      totalMemory: info.totalMemory
    })
  })
  // #endif
}

/**
 * 清理内存
 */
export const clearMemory = () => {
  // 清理图片缓存
  // #ifdef APP-PLUS
  plus.cache.clear(() => {
    console.log('缓存清理完成')
  })
  // #endif
  
  // 触发垃圾回收（仅H5）
  // #ifdef H5
  if (window.gc) {
    window.gc()
  }
  // #endif
}

/**
 * 性能监控
 */
export class PerformanceMonitor {
  constructor() {
    this.metrics = {
      pageLoadTime: 0,
      apiCallCount: 0,
      apiTotalTime: 0,
      imageLoadCount: 0,
      imageLoadTime: 0
    }
  }

  /**
   * 记录页面加载时间
   */
  recordPageLoad(startTime) {
    this.metrics.pageLoadTime = Date.now() - startTime
    console.log('页面加载时间:', this.metrics.pageLoadTime, 'ms')
  }

  /**
   * 记录API调用
   */
  recordApiCall(duration) {
    this.metrics.apiCallCount++
    this.metrics.apiTotalTime += duration
    
    const avgTime = this.metrics.apiTotalTime / this.metrics.apiCallCount
    console.log('API平均响应时间:', avgTime.toFixed(2), 'ms')
  }

  /**
   * 记录图片加载
   */
  recordImageLoad(duration) {
    this.metrics.imageLoadCount++
    this.metrics.imageLoadTime += duration
    
    const avgTime = this.metrics.imageLoadTime / this.metrics.imageLoadCount
    console.log('图片平均加载时间:', avgTime.toFixed(2), 'ms')
  }

  /**
   * 获取性能报告
   */
  getReport() {
    return {
      ...this.metrics,
      apiAvgTime: this.metrics.apiCallCount > 0 
        ? this.metrics.apiTotalTime / this.metrics.apiCallCount 
        : 0,
      imageAvgTime: this.metrics.imageLoadCount > 0
        ? this.metrics.imageLoadTime / this.metrics.imageLoadCount
        : 0
    }
  }
}

/**
 * 全局性能监控实例
 */
export const performanceMonitor = new PerformanceMonitor()

/**
 * 全局请求缓存实例
 */
export const requestCache = new RequestCacheManager()

/**
 * 全局请求去重实例
 */
export const requestDeduplicator = new RequestDeduplicator()

export default {
  ImageLazyLoader,
  RequestCacheManager,
  RequestDeduplicator,
  compressImage,
  batchCompressImages,
  throttle,
  debounce,
  loadComponent,
  preloadResources,
  monitorMemory,
  clearMemory,
  PerformanceMonitor,
  performanceMonitor,
  requestCache,
  requestDeduplicator
}

