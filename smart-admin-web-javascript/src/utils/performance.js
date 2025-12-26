/**
 * 性能优化工具集
 *
 * 提供防抖、节流、虚拟滚动等性能优化工具
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

/**
 * 防抖函数 - 延迟执行
 * @param {Function} func - 要执行的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @param {boolean} immediate - 是否立即执行
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, delay = 300, immediate = false) {
  let timer = null;

  return function (...args) {
    const context = this;

    if (timer) {
      clearTimeout(timer);
    }

    if (immediate) {
      const callNow = !timer;
      timer = setTimeout(() => {
        timer = null;
      }, delay);
      if (callNow) {
        func.apply(context, args);
      }
    } else {
      timer = setTimeout(() => {
        func.apply(context, args);
        timer = null;
      }, delay);
    }
  };
}

/**
 * 节流函数 - 限制执行频率
 * @param {Function} func - 要执行的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(func, delay = 300) {
  let timer = null;
  let lastTime = 0;

  return function (...args) {
    const context = this;
    const now = Date.now();

    if (now - lastTime >= delay) {
      if (timer) {
        clearTimeout(timer);
        timer = null;
      }
      func.apply(context, args);
      lastTime = now;
    } else if (!timer) {
      timer = setTimeout(() => {
        func.apply(context, args);
        lastTime = Date.now();
        timer = null;
      }, delay - (now - lastTime));
    }
  };
}

/**
 * 请求缓存管理器
 */
export class CacheManager {
  constructor(defaultCacheTime = 5 * 60 * 1000) {
    this.cache = new Map();
    this.defaultCacheTime = defaultCacheTime;
  }

  /**
   * 生成缓存键
   * @param {string} key - 缓存键前缀
   * @param {Object} params - 参数对象
   * @returns {string} 缓存键
   */
  generateKey(key, params = {}) {
    const sortedParams = Object.keys(params)
      .sort()
      .map(k => `${k}=${JSON.stringify(params[k])}`)
      .join('&');
    return `${key}?${sortedParams}`;
  }

  /**
   * 设置缓存
   * @param {string} key - 缓存键
   * @param {any} value - 缓存值
   * @param {number} cacheTime - 缓存时间（毫秒）
   */
  set(key, value, cacheTime = this.defaultCacheTime) {
    this.cache.set(key, {
      value,
      expireTime: Date.now() + cacheTime,
    });
  }

  /**
   * 获取缓存
   * @param {string} key - 缓存键
   * @returns {any|null} 缓存值或null
   */
  get(key) {
    const item = this.cache.get(key);

    if (!item) {
      return null;
    }

    if (Date.now() > item.expireTime) {
      this.cache.delete(key);
      return null;
    }

    return item.value;
  }

  /**
   * 清除缓存
   * @param {string} key - 缓存键
   */
  clear(key) {
    if (key) {
      this.cache.delete(key);
    } else {
      this.cache.clear();
    }
  }

  /**
   * 清除过期缓存
   */
  clearExpired() {
    const now = Date.now();
    for (const [key, item] of this.cache.entries()) {
      if (now > item.expireTime) {
        this.cache.delete(key);
      }
    }
  }
}

/**
 * 创建带缓存的API请求函数
 * @param {Function} apiFunc - API请求函数
 * @param {CacheManager} cacheManager - 缓存管理器
 * @param {number} cacheTime - 缓存时间
 * @returns {Function} 带缓存的请求函数
 */
export function withCache(apiFunc, cacheManager, cacheTime) {
  return async function (key, params, useCache = true) {
    const cacheKey = cacheManager.generateKey(key, params);

    // 尝试从缓存获取
    if (useCache) {
      const cached = cacheManager.get(cacheKey);
      if (cached) {
        console.log(`[Cache] 命中缓存: ${cacheKey}`);
        return cached;
      }
    }

    // 执行API请求
    const result = await apiFunc(params);

    // 存入缓存
    if (result && (result.code === 200 || result.success)) {
      cacheManager.set(cacheKey, result, cacheTime);
      console.log(`[Cache] 存入缓存: ${cacheKey}`);
    }

    return result;
  };
}

/**
 * 虚拟滚动计算辅助函数
 * 计算可见区域的数据索引
 */
export class VirtualScrollHelper {
  constructor(options = {}) {
    this.itemHeight = options.itemHeight || 50;
    this.containerHeight = options.containerHeight || 600;
    this.bufferSize = options.bufferSize || 5;
  }

  /**
   * 计算可见范围
   * @param {number} scrollTop - 滚动位置
   * @param {number} totalCount - 总数据量
   * @returns {Object} 可见范围信息
   */
  getVisibleRange(scrollTop, totalCount) {
    const startIndex = Math.max(0, Math.floor(scrollTop / this.itemHeight) - this.bufferSize);
    const endIndex = Math.min(
      totalCount,
      Math.ceil((scrollTop + this.containerHeight) / this.itemHeight) + this.bufferSize
    );

    return {
      startIndex,
      endIndex,
      visibleCount: endIndex - startIndex,
      offsetY: startIndex * this.itemHeight,
    };
  }

  /**
   * 计算总高度
   * @param {number} totalCount - 总数据量
   * @returns {number} 总高度
   */
  getTotalHeight(totalCount) {
    return totalCount * this.itemHeight;
  }
}

/**
 * RAF节流 - 使用requestAnimationFrame优化高频事件
 * @param {Function} func - 要执行的函数
 * @returns {Function} 节流后的函数
 */
export function rafThrottle(func) {
  let rafId = null;

  return function (...args) {
    if (rafId) {
      return;
    }

    rafId = requestAnimationFrame(() => {
      func.apply(this, args);
      rafId = null;
    });
  };
}

/**
 * 空闲时执行 - 使用requestIdleCallback优化低优先级任务
 * @param {Function} func - 要执行的函数
 * @param {number} timeout - 超时时间
 * @returns {Function}
 */
export function runWhenIdle(func, timeout = 2000) {
  return function (...args) {
    if ('requestIdleCallback' in window) {
      requestIdleCallback(() => {
        func.apply(this, args);
      }, { timeout });
    } else {
      // 降级方案
      setTimeout(() => {
        func.apply(this, args);
      }, 1);
    }
  };
}

/**
 * 批量处理 - 批量执行任务避免频繁操作
 * @param {Function} func - 要执行的函数
 * @param {number} delay - 延迟时间
 * @returns {Function} 批量处理后的函数
 */
export function batchProcess(func, delay = 100) {
  let timer = null;
  let queue = [];

  return function (...args) {
    queue.push(args);

    if (timer) {
      clearTimeout(timer);
    }

    timer = setTimeout(() => {
      if (queue.length > 0) {
        func.call(this, queue);
        queue = [];
      }
      timer = null;
    }, delay);
  };
}

/**
 * 图片懒加载观察器
 * @param {string} selector - 图片选择器
 * @param {Function} callback - 加载回调
 * @returns {IntersectionObserver}
 */
export function createLazyLoader(selector, callback) {
  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target;
          callback(img);
          observer.unobserve(img);
        }
      });
    }, {
      rootMargin: '50px',
    });

    document.querySelectorAll(selector).forEach(img => {
      observer.observe(img);
    });

    return observer;
  } else {
    // 降级方案
    document.querySelectorAll(selector).forEach(img => {
      callback(img);
    });
    return null;
  }
}

// 导出默认缓存管理器实例
export const defaultCacheManager = new CacheManager();
