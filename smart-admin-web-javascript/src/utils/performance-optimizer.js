/**
 * 考勤模块前端性能优化工具
 * 提供虚拟滚动、懒加载、防抖节流、缓存等性能优化功能
 */

import { ref, reactive, nextTick, onMounted, onUnmounted } from 'vue';

// ========================================
// 防抖和节流工具
// ========================================

/**
 * 防抖函数
 * @param {Function} func - 要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @param {boolean} immediate - 是否立即执行
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, delay, immediate = false) {
  let timeout = null;

  return function(...args) {
    const context = this;
    const later = () => {
      timeout = null;
      if (!immediate) func.apply(context, args);
    };

    const callNow = immediate && !timeout;

    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(later, delay);

    if (callNow) func.apply(context, args);
  };
}

/**
 * 节流函数
 * @param {Function} func - 要节流的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @param {boolean} immediate - 是否立即执行
 * @returns {Function} 节流后的函数
 */
export function throttle(func, delay, immediate = false) {
  let timeout = null;
  let lastCall = 0;

  return function(...args) {
    const context = this;
    const now = Date.now();

    if (lastCall > now - delay) {
      lastCall = now;
      if (immediate) func.apply(context, args);
      return;
    }

    if (timeout) return;

    timeout = setTimeout(() => {
      lastCall = now;
      if (!immediate) func.apply(context, args);
    }, delay);
  };
}

// ========================================
// 内存缓存管理
// ========================================

class MemoryCache {
  constructor(options = {}) {
    this.cache = new Map();
    this.maxSize = options.maxSize || 100;
    this.defaultTTL = options.defaultTTL || 5 * 60 * 1000; // 5分钟
    this.cleanup();
  }

  set(key, value, ttl = null) {
    // 检查缓存大小限制
    if (this.cache.size >= this.maxSize) {
      this.evictLRU();
    }

    const expireTime = Date.now() + (ttl || this.defaultTTL);
    this.cache.set(key, {
      value,
      expireTime,
      accessTime: Date.now()
    });
  }

  get(key) {
    const item = this.cache.get(key);

    if (!item) {
      return null;
    }

    // 检查是否过期
    if (Date.now() > item.expireTime) {
      this.cache.delete(key);
      return null;
    }

    // 更新访问时间
    item.accessTime = Date.now();
    return item.value;
  }

  has(key) {
    return this.get(key) !== null;
  }

  delete(key) {
    return this.cache.delete(key);
  }

  clear() {
    this.cache.clear();
  }

  size() {
    return this.cache.size;
  }

  // LRU淘汰
  evictLRU() {
    let oldestKey = null;
    let oldestTime = Infinity;

    for (const [key, item] of this.cache.entries()) {
      if (item.accessTime < oldestTime) {
        oldestTime = item.accessTime;
        oldestKey = key;
      }
    }

    if (oldestKey) {
      this.cache.delete(oldestKey);
    }
  }

  // 清理过期项
  cleanup() {
    const now = Date.now();
    const keysToDelete = [];

    for (const [key, item] of this.cache.entries()) {
      if (now > item.expireTime) {
        keysToDelete.push(key);
      }
    }

    keysToDelete.forEach(key => this.cache.delete(key));
  }
}

// ========================================
// 虚拟滚动实现
// ========================================

export class VirtualScrollManager {
  constructor(options = {}) {
    this.itemHeight = options.itemHeight || 50;
    this.bufferSize = options.bufferSize || 10;
    this.visibleRange = reactive({
      startIndex: 0,
      endIndex: 0
    });
    this.data = reactive([]);
    this.scrollTop = ref(0);
    this.containerHeight = ref(0);
    this.totalHeight = ref(0);

    this.renderCallback = options.renderCallback;
    this.initScrollListener();
  }

  setData(data) {
    this.data = [...data];
    this.totalHeight.value = data.length * this.itemHeight;
    this.updateVisibleRange();
  }

  initScrollListener() {
    const handleScroll = throttle(() => {
      this.updateVisibleRange();
    }, 16); // 60fps

    // 监听滚动事件
    window.addEventListener('scroll', handleScroll);

    // 清理函数
    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll);
    });
  }

  updateVisibleRange() {
    if (this.containerHeight.value === 0) return;

    const startIndex = Math.floor(this.scrollTop.value / this.itemHeight);
    const endIndex = Math.min(
      startIndex + Math.ceil(this.containerHeight.value / this.itemHeight),
      this.data.length - 1
    );

    // 添加缓冲区
    const bufferedStartIndex = Math.max(0, startIndex - this.bufferSize);
    const bufferedEndIndex = Math.min(
      endIndex + this.bufferSize,
      this.data.length - 1
    );

    this.visibleRange.startIndex = bufferedStartIndex;
    this.visibleRange.endIndex = bufferedEndIndex;

    if (this.renderCallback) {
      this.renderCallback(
        this.data.slice(bufferedStartIndex, bufferedEndIndex + 1),
        bufferedStartIndex,
        bufferedEndIndex
      );
    }
  }

  scrollToIndex(index) {
    const targetScrollTop = index * this.itemHeight;
    window.scrollTo({
      top: targetScrollTop,
      behavior: 'smooth'
    });
  }

  updateContainerHeight(height) {
    this.containerHeight.value = height;
    this.updateVisibleRange();
  }

  updateScrollTop(scrollTop) {
    this.scrollTop.value = scrollTop;
    this.updateVisibleRange();
  }
}

// ========================================
// 图片懒加载
// ========================================

export class LazyLoadManager {
  constructor(options = {}) {
    this.observer = null;
    this.loadedImages = new Set();
    this.options = {
      rootMargin: options.rootMargin || '50px',
      threshold: options.threshold || 0.1,
      ...options
    };

    this.init();
  }

  init() {
    if ('IntersectionObserver' in window) {
      this.observer = new IntersectionObserver(this.handleIntersection.bind(this), {
        rootMargin: this.options.rootMargin,
        threshold: this.options.threshold
      });
    }
  }

  observe(element) {
    if (this.observer && element) {
      this.observer.observe(element);
    }
  }

  unobserve(element) {
    if (this.observer && element) {
      this.observer.unobserve(element);
    }
  }

  loadImage(imageElement) {
    const src = imageElement.dataset.src;
    if (!src || this.loadedImages.has(src)) {
      return;
    }

    const img = new Image();
    img.onload = () => {
      this.loadedImages.add(src);
      imageElement.src = src;
      imageElement.classList.remove('lazy-loading');
      imageElement.classList.add('lazy-loaded');
    };

    img.onerror = () => {
      imageElement.classList.remove('lazy-loading');
      imageElement.classList.add('lazy-error');
    };

    img.src = src;
  }

  handleIntersection(entries) {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const element = entry.target;
        if (element.dataset.src) {
          this.loadImage(element);
          this.unobserve(element);
        }
      }
    });
  }

  disconnect() {
    if (this.observer) {
      this.observer.disconnect();
    }
  }
}

// ========================================
// 性能监控
// ========================================

export class PerformanceMonitor {
  constructor() {
    this.metrics = reactive({
      pageLoadTime: 0,
      firstContentfulPaint: 0,
      largestContentfulPaint: 0,
      firstInputDelay: 0,
      cumulativeLayoutShift: 0,
      renderTime: 0
    });

    this.init();
  }

  init() {
    // 监听页面加载
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => {
        this.metrics.pageLoadTime = performance.now();
      });
    }

    // 监听FCP
    if ('PerformanceObserver' in window) {
      try {
        // 监听导航和LCP
        new PerformanceObserver((entryList) => {
          const entries = entryList.getEntries();
          entries.forEach(entry => {
            if (entry.entryType === 'navigation') {
              this.metrics.pageLoadTime = entry.loadEventEnd - entry.fetchStart;
            } else if (entry.entryType === 'paint') {
              if (entry.name === 'first-contentful-paint') {
                this.metrics.firstContentfulPaint = entry.startTime;
              } else if (entry.name === 'largest-contentful-paint') {
                this.metrics.largestContentfulPaint = entry.startTime;
              }
            }
          });
        }).observe({ entryTypes: ['navigation', 'paint'] });

        // 监听FID
        new PerformanceObserver((entryList) => {
          const entries = entryList.getEntries();
          entries.forEach(entry => {
            if (entry.entryType === 'first-input') {
              this.metrics.firstInputDelay = entry.processingStart - entry.startTime;
            }
          });
        }).observe({ entryTypes: ['first-input'] });

        // 监听CLS
        let clsScore = 0;
        new PerformanceObserver((entryList) => {
          entries.forEach(entry => {
            if (!entry.hadRecentInput) {
              clsScore += entry.value;
              this.metrics.cumulativeLayoutShift = clsScore;
            }
          });
        }).observe({ entryTypes: ['layout-shift'] });

      } catch (error) {
        console.warn('Performance Observer not fully supported:', error);
      }
    }
  }

  measureRenderTime(componentName, renderFn) {
    return (...args) => {
      const startTime = performance.now();
      const result = renderFn.apply(this, args);
      const endTime = performance.now();

      const renderTime = endTime - startTime;
      console.log(`📊 ${componentName} 渲染时间: ${renderTime.toFixed(2)}ms`);

      this.metrics.renderTime = Math.max(this.metrics.renderTime, renderTime);
      return result;
    };
  }

  startTiming(label) {
    performance.mark(`${label}-start`);
  }

  endTiming(label) {
    performance.mark(`${label}-end`);
    performance.measure(label, `${label}-start`, `${label}-end`);

    const measure = performance.getEntriesByName(label).pop();
    if (measure) {
      console.log(`⏱️ ${label}: ${measure.duration.toFixed(2)}ms`);
    }
  }

  getMetrics() {
    return {
      ...this.metrics,
      memoryUsage: this.getMemoryUsage(),
      networkInfo: this.getNetworkInfo()
    };
  }

  getMemoryUsage() {
    if (performance.memory) {
      return {
        used: (performance.memory.usedJSHeapSize / 1024 / 1024).toFixed(2) + ' MB',
        total: (performance.memory.totalJSHeapSize / 1024 / 1024).toFixed(2) + ' MB',
        limit: (performance.memory.jsHeapSizeLimit / 1024 / 1024).toFixed(2) + ' MB'
      };
    }
    return null;
  }

  getNetworkInfo() {
    if (navigator.connection) {
      return {
        effectiveType: navigator.connection.effectiveType,
        downlink: navigator.connection.downlink + ' Mbps',
        rtt: navigator.connection.rtt + ' ms'
      };
    }
    return null;
  }
}

// ========================================
// Vue 组合式API 钩子
// ========================================

/**
 * 性能优化组合式函数
 * @param {Object} options 配置选项
 */
export function usePerformanceOptimization(options = {}) {
  const config = reactive({
    enableVirtualScroll: options.enableVirtualScroll || false,
    enableLazyLoad: options.enableLazyLoad || false,
    enableDebounce: options.enableDebounce || true,
    enableThrottle: options.enableThrottle || true,
    enableCache: options.enableCache || true,
    ...options
  });

  const cache = new MemoryCache();
  const performanceMonitor = new PerformanceMonitor();

  let virtualScrollManager = null;
  let lazyLoadManager = null;

  // 初始化虚拟滚动
  const initVirtualScroll = (scrollOptions) => {
    if (config.enableVirtualScroll) {
      virtualScrollManager = new VirtualScrollManager(scrollOptions);
    }
  };

  // 初始化懒加载
  const initLazyLoad = (lazyOptions) => {
    if (config.enableLazyLoad) {
      lazyLoadManager = new LazyLoadManager(lazyOptions);
    }
  };

  // 防抖函数
  const createDebounce = (fn, delay) => {
    if (config.enableDebounce) {
      return debounce(fn, delay);
    }
    return fn;
  };

  // 节流函数
  const createThrottle = (fn, delay) => {
    if (config.enableThrottle) {
      return throttle(fn, delay);
    }
    return fn;
  };

  // 缓存函数
  const createCache = (key, ttl) => {
    if (config.enableCache) {
      return {
        get: () => cache.get(key),
        set: (value) => cache.set(key, value, ttl),
        has: () => cache.has(key),
        delete: () => cache.delete(key),
        clear: () => cache.clear()
      };
    }
    return {
      get: () => null,
      set: () => {},
      has: () => false,
      delete: () => {},
      clear: () => {}
    };
  };

  // 性能监控函数
  const measurePerformance = performanceMonitor.measureRenderTime;

  onMounted(() => {
    // 初始化组件
    if (options.virtualScrollOptions) {
      initVirtualScroll(options.virtualScrollOptions);
    }
    if (options.lazyLoadOptions) {
      initLazyLoad(options.lazyLoadOptions);
    }
  });

  onUnmounted(() => {
    // 清理资源
    if (lazyLoadManager) {
      lazyLoadManager.disconnect();
    }
  });

  return {
    config,
    cache,
    virtualScrollManager,
    lazyLoadManager,
    performanceMonitor,
    initVirtualScroll,
    initLazyLoad,
    createDebounce,
    createThrottle,
    createCache,
    measurePerformance,
    getMetrics: performanceMonitor.getMetrics.bind(performanceMonitor)
  };
}

// ========================================
// 导出默认实例
// ========================================

export const defaultCache = new MemoryCache({ maxSize: 200 });
export const defaultLazyLoader = new LazyLoadManager();

// 常用的性能优化工具函数
export const optimizedUtils = {
  // 分页数据缓存
  cachePageData: (cacheKey, data, ttl = 300000) => {
    defaultCache.set(`page:${cacheKey}`, data, ttl);
  },

  getCachedPageData: (cacheKey) => {
    return defaultCache.get(`page:${cacheKey}`);
  },

  // 表格数据分片处理
  processTableData: (data, chunkSize = 100) => {
    const chunks = [];
    for (let i = 0; i < data.length; i += chunkSize) {
      chunks.push(data.slice(i, i + chunkSize));
    }
    return chunks;
  },

  // 大数组搜索优化
  binarySearch: (array, target, key) => {
    let left = 0;
    let right = array.length - 1;

    while (left <= right) {
      const mid = Math.floor((left + right) / 2);
      const midValue = array[mid][key];

      if (midValue === target) {
        return mid;
      } else if (midValue < target) {
        left = mid + 1;
      } else {
        right = mid - 1;
      }
    }

    return -1;
  }
};