/**
 * 性能监控模块
 * 用于监控页面性能、资源使用、网络请求等
 */

/**
 * 性能指标类
 */
class PerformanceMetrics {
  constructor() {
    this.metrics = new Map()
    this.startTime = Date.now()
  }

  /**
   * 记录指标
   */
  set(name, value) {
    this.metrics.set(name, {
      value,
      timestamp: Date.now()
    })
  }

  /**
   * 获取指标
   */
  get(name) {
    const metric = this.metrics.get(name)
    return metric ? metric.value : null
  }

  /**
   * 获取所有指标
   */
  getAll() {
    const result = {}
    for (const [name, metric] of this.metrics.entries()) {
      result[name] = metric.value
    }
    return result
  }

  /**
   * 清除所有指标
   */
  clear() {
    this.metrics.clear()
  }

  /**
   * 计算运行时间
   */
  getRuntime() {
    return Date.now() - this.startTime
  }
}

/**
 * 性能监控类
 */
class PerformanceMonitor {
  constructor() {
    this.metrics = new PerformanceMetrics()
    this.observers = new Map()
    this.isMonitoring = false
  }

  /**
   * 开始监控
   */
  start() {
    if (this.isMonitoring) {
      console.warn('[性能监控] 已经在监控中')
      return
    }

    this.isMonitoring = true
    this.metrics.startTime = Date.now()

    // 监控页面性能
    this.monitorPagePerformance()

    // 监控内存使用
    this.monitorMemory()

    // 监控网络请求
    this.monitorNetwork()

    console.log('[性能监控] 开始监控')
  }

  /**
   * 停止监控
   */
  stop() {
    if (!this.isMonitoring) {
      console.warn('[性能监控] 未在监控中')
      return
    }

    this.isMonitoring = false

    // 停止所有观察器
    for (const observer of this.observers.values()) {
      if (observer.disconnect) {
        observer.disconnect()
      }
    }
    this.observers.clear()

    console.log('[性能监控] 停止监控')
  }

  /**
   * 监控页面性能
   */
  monitorPagePerformance() {
    // 使用Performance API
    if (typeof performance !== 'undefined') {
      // 页面加载时间
      window.addEventListener('load', () => {
        setTimeout(() => {
          const perfData = performance.getEntriesByType('navigation')[0]

          if (perfData) {
            this.metrics.set('pageLoadTime', perfData.loadEventEnd - perfData.fetchStart)
            this.metrics.set('domReadyTime', perfData.domContentLoadedEventEnd - perfData.fetchStart)
            this.metrics.set('firstPaint', perfData.responseStart - perfData.fetchStart)

            console.log('[性能监控] 页面加载指标:', {
              页面加载时间: this.metrics.get('pageLoadTime') + 'ms',
              DOM就绪时间: this.metrics.get('domReadyTime') + 'ms',
              首次绘制: this.metrics.get('firstPaint') + 'ms'
            })
          }
        }, 0)
      })

      // 监控长任务
      if (PerformanceObserver) {
        try {
          const longTaskObserver = new PerformanceObserver((list) => {
            for (const entry of list.getEntries()) {
              if (entry.duration > 50) {
                console.warn('[性能监控] 检测到长任务:', {
                  name: entry.name,
                  duration: entry.duration + 'ms',
                  startTime: entry.startTime
                })

                this.metrics.set('longTask', {
                  name: entry.name,
                  duration: entry.duration,
                  startTime: entry.startTime
                })
              }
            }
          })

          longTaskObserver.observe({ entryTypes: ['longtask'] })
          this.observers.set('longTask', longTaskObserver)
        } catch (e) {
          console.log('[性能监控] 长任务监控不支持')
        }
      }
    }

    // uni-app 页面生命周期监控
    const pages = getCurrentPages()
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1]
      const pageName = currentPage.route || 'unknown'

      this.metrics.set('currentPage', pageName)
      console.log('[性能监控] 当前页面:', pageName)
    }
  }

  /**
   * 监控内存使用
   */
  monitorMemory() {
    if (typeof performance !== 'undefined' && performance.memory) {
      setInterval(() => {
        if (!this.isMonitoring) return

        const memory = performance.memory
        const usedMB = (memory.usedJSHeapSize / 1048576).toFixed(2)
        const totalMB = (memory.totalJSHeapSize / 1048576).toFixed(2)
        const limitMB = (memory.jsHeapSizeLimit / 1048576).toFixed(2)

        this.metrics.set('memoryUsed', usedMB)
        this.metrics.set('memoryTotal', totalMB)
        this.metrics.set('memoryLimit', limitMB)

        // 内存使用率超过80%时警告
        const usagePercent = (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100
        if (usagePercent > 80) {
          console.warn('[性能监控] 内存使用率过高:', {
            已使用: usedMB + 'MB',
            总计: totalMB + 'MB',
            限制: limitMB + 'MB',
            使用率: usagePercent.toFixed(2) + '%'
          })
        }
      }, 10000)  // 每10秒检查一次
    }
  }

  /**
   * 监控网络请求
   */
  monitorNetwork() {
    // 拦截uni.request
    const originalRequest = uni.request
    let requestCount = 0
    let totalRequestTime = 0

    uni.request = (options) => {
      const startTime = Date.now()
      requestCount++

      const requestSuccess = options.success
      const requestFail = options.fail
      const requestComplete = options.complete

      options.success = (res) => {
        const endTime = Date.now()
        const requestTime = endTime - startTime
        totalRequestTime += requestTime

        this.metrics.set('lastRequestTime', requestTime)
        this.metrics.set('avgRequestTime', totalRequestTime / requestCount)
        this.metrics.set('requestCount', requestCount)

        console.log('[性能监控] 网络请求:', {
          url: options.url,
          耗时: requestTime + 'ms',
          状态: res.statusCode
        })

        if (requestSuccess) {
          requestSuccess(res)
        }
      }

      options.fail = (err) => {
        console.error('[性能监控] 请求失败:', {
          url: options.url,
          错误: err
        })

        if (requestFail) {
          requestFail(err)
        }
      }

      options.complete = (res) => {
        if (requestComplete) {
          requestComplete(res)
        }
      }

      return originalRequest.call(this, options)
    }
  }

  /**
   * 监控组件渲染时间
   */
  measureRender(componentName, fn) {
    const start = Date.now()
    const result = fn()

    if (result && typeof result.then === 'function') {
      // 异步组件
      return result.then(() => {
        const renderTime = Date.now() - start
        this.metrics.set(`${componentName}_render`, renderTime)
        console.log(`[性能监控] ${componentName} 渲染时间:`, renderTime + 'ms')
      })
    } else {
      // 同步组件
      const renderTime = Date.now() - start
      this.metrics.set(`${componentName}_render`, renderTime)
      console.log(`[性能监控] ${componentName} 渲染时间:`, renderTime + 'ms')
      return result
    }
  }

  /**
   * 标记性能点
   */
  mark(name) {
    this.metrics.set(name, Date.now() - this.metrics.startTime)
    console.log(`[性能监控] 标记: ${name}`, this.metrics.get(name) + 'ms')
  }

  /**
   * 测量两个标记之间的时间
   */
  measure(name, startMark, endMark) {
    const startTime = this.metrics.get(startMark)
    const endTime = this.metrics.get(endMark)

    if (startTime !== null && endTime !== null) {
      const duration = endTime - startTime
      this.metrics.set(name, duration)
      console.log(`[性能监控] 测量: ${name}`, duration + 'ms')
      return duration
    }

    return null
  }

  /**
   * 获取性能报告
   */
  getReport() {
    const runtime = this.metrics.getRuntime()
    const allMetrics = this.metrics.getAll()

    return {
      运行时间: runtime + 'ms',
      ...allMetrics
    }
  }

  /**
   * 导出性能报告
   */
  exportReport() {
    const report = this.getReport()
    const json = JSON.stringify(report, null, 2)
    console.log('[性能监控] 性能报告:', json)

    return report
  }

  /**
   * 清除性能数据
   */
  clear() {
    this.metrics.clear()
    this.metrics.startTime = Date.now()
    console.log('[性能监控] 清除性能数据')
  }
}

// 导出单例
export const performanceMonitor = new PerformanceMonitor()

/**
 * Vue 3 组合式函数 - 使用性能监控
 * @param {String} componentName 组件名称
 * @returns {Object}
 */
export function usePerformanceMonitor(componentName = 'Component') {
  const start = () => {
    performanceMonitor.start()
  }

  const stop = () => {
    performanceMonitor.stop()
  }

  const measureRender = (fn) => {
    return performanceMonitor.measureRender(componentName, fn)
  }

  const mark = (name) => {
    performanceMonitor.mark(`${componentName}_${name}`)
  }

  const measure = (name, startMark, endMark) => {
    return performanceMonitor.measure(
      `${componentName}_${name}`,
      `${componentName}_${startMark}`,
      `${componentName}_${endMark}`
    )
  }

  const getReport = () => {
    return performanceMonitor.getReport()
  }

  return {
    start,
    stop,
    measureRender,
    mark,
    measure,
    getReport
  }
}

/**
 * 自动性能监控装饰器
 * @param {String} componentName 组件名称
 * @returns {Function}
 */
export function autoMonitor(componentName = 'Component') {
  return function (target, propertyKey, descriptor) {
    const originalMethod = descriptor.value

    descriptor.value = async function (...args) {
      const start = Date.now()

      try {
        const result = await originalMethod.apply(this, args)

        const duration = Date.now() - start
        console.log(`[性能监控] ${componentName}.${propertyKey}:`, duration + 'ms')

        if (duration > 1000) {
          console.warn(`[性能监控] ${componentName}.${propertyKey} 执行时间过长`)
        }

        return result
      } catch (error) {
        const duration = Date.now() - start
        console.error(`[性能监控] ${componentName}.${propertyKey} 错误:`, duration + 'ms', error)
        throw error
      }
    }

    return descriptor
  }
}

export default {
  performanceMonitor,
  usePerformanceMonitor,
  autoMonitor
}
