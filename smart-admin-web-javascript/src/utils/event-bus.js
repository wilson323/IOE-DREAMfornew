import { ref, reactive, onUnmounted, nextTick } from 'vue';

/**
 * 事件总线工具类
 * 提供发布订阅模式的事件系统，支持事件过滤、优先级、异步处理等
 */
export class EventEmitter {
  constructor(options = {}) {
    this.events = new Map();
    this.onceEvents = new Map();
    this.middlewares = [];
    this.maxListeners = options.maxListeners || 100;
    this.debug = options.debug || false;
    this.statistics = reactive({
      totalEvents: 0,
      totalListeners: 0,
      eventCounts: new Map()
    });
  }

  /**
   * 添加事件监听器
   * @param {string} event 事件名称
   * @param {Function} listener 监听器函数
   * @param {Object} options 选项
   */
  on(event, listener, options = {}) {
    if (typeof listener !== 'function') {
      throw new TypeError('监听器必须是函数');
    }

    if (!this.events.has(event)) {
      this.events.set(event, new Set());
    }

    const listeners = this.events.get(event);

    // 检查监听器数量限制
    if (listeners.size >= this.maxListeners) {
      console.warn(`事件 "${event}" 的监听器数量已达到最大限制 ${this.maxListeners}`);
    }

    const listenerInfo = {
      listener,
      priority: options.priority || 0,
      context: options.context || null,
      id: this.generateListenerId(),
      addedAt: Date.now()
    };

    listeners.add(listenerInfo);
    this.statistics.totalListeners++;

    this.log(`添加事件监听器: ${event}`, listenerInfo);

    // 返回取消监听函数
    return () => this.off(event, listener);
  }

  /**
   * 添加一次性事件监听器
   * @param {string} event 事件名称
   * @param {Function} listener 监听器函数
   * @param {Object} options 选项
   */
  once(event, listener, options = {}) {
    const onceWrapper = (data) => {
      this.off(event, onceWrapper);
      listener.call(options.context || null, data);
    };

    // 标记为一次性监听器
    onceWrapper._isOnce = true;
    onceWrapper._originalListener = listener;

    return this.on(event, onceWrapper, options);
  }

  /**
   * 移除事件监听器
   * @param {string} event 事件名称
   * @param {Function} listener 监听器函数
   */
  off(event, listener) {
    const listeners = this.events.get(event);
    if (!listeners) {
      return false;
    }

    let removed = false;
    listeners.forEach(listenerInfo => {
      if (listenerInfo.listener === listener ||
          listenerInfo.listener._originalListener === listener) {
        listeners.delete(listenerInfo);
        removed = true;
        this.statistics.totalListeners--;
      }
    });

    // 如果没有监听器了，删除事件
    if (listeners.size === 0) {
      this.events.delete(event);
    }

    this.log(`移除事件监听器: ${event}`, removed);
    return removed;
  }

  /**
   * 移除事件的所有监听器
   * @param {string} event 事件名称
   */
  removeAllListeners(event) {
    if (event) {
      const listeners = this.events.get(event);
      if (listeners) {
        this.statistics.totalListeners -= listeners.size;
        this.events.delete(event);
        this.log(`移除事件 "${event}" 的所有监听器`);
      }
    } else {
      // 移除所有事件
      this.events.clear();
      this.statistics.totalListeners = 0;
      this.log('移除所有事件监听器');
    }
  }

  /**
   * 触发事件
   * @param {string} event 事件名称
   * @param {*} data 事件数据
   * @param {Object} options 选项
   */
  async emit(event, data, options = {}) {
    this.statistics.totalEvents++;

    const eventCount = this.statistics.eventCounts.get(event) || 0;
    this.statistics.eventCounts.set(event, eventCount + 1);

    this.log(`触发事件: ${event}`, data);

    // 执行中间件
    if (this.middlewares.length > 0) {
      let middlewareIndex = 0;

      const executeMiddleware = async (index) => {
        if (index >= this.middlewares.length) {
          return this.executeListeners(event, data, options);
        }

        const middleware = this.middlewares[index];
        try {
          await middleware(data, event, () => executeMiddleware(index + 1));
        } catch (error) {
          console.error(`事件中间件执行失败 [${index}]:`, error);
          return this.executeListeners(event, data, options);
        }
      };

      return executeMiddleware(0);
    }

    return this.executeListeners(event, data, options);
  }

  /**
   * 执行事件监听器
   */
  async executeListeners(event, data, options) {
    const listeners = this.events.get(event);
    if (!listeners || listeners.size === 0) {
      return;
    }

    // 按优先级排序
    const sortedListeners = Array.from(listeners).sort((a, b) => b.priority - a.priority);

    const promises = [];
    const errors = [];

    for (const listenerInfo of sortedListeners) {
      try {
        const result = options.async
          ? this.executeAsyncListener(listenerInfo, data, options)
          : this.executeSyncListener(listenerInfo, data, options);

        if (options.async) {
          promises.push(result);
        }
      } catch (error) {
        errors.push({
          error,
          listener: listenerInfo.listener
        });
        console.error(`事件监听器执行失败 [${event}]:`, error);
      }
    }

    // 处理异步结果
    if (options.async && promises.length > 0) {
      try {
        const results = await Promise.allSettled(promises);
        return {
          success: true,
          results,
          errors
        };
      } catch (error) {
        return {
          success: false,
          error,
          errors
        };
      }
    }

    return {
      success: errors.length === 0,
      errors,
      listenersCount: sortedListeners.length
    };
  }

  /**
   * 同步执行监听器
   */
  executeSyncListener(listenerInfo, data, options) {
    const { listener, context } = listenerInfo;
    return listener.call(context, data, options);
  }

  /**
   * 异步执行监听器
   */
  async executeAsyncListener(listenerInfo, data, options) {
    const { listener, context } = listenerInfo;
    const result = listener.call(context, data, options);

    // 如果返回Promise，等待其完成
    if (result && typeof result.then === 'function') {
      return await result;
    }

    return result;
  }

  /**
   * 添加中间件
   * @param {Function} middleware 中间件函数
   */
  use(middleware) {
    if (typeof middleware !== 'function') {
      throw new TypeError('中间件必须是函数');
    }

    this.middlewares.push(middleware);
    this.log('添加中间件:', middleware.name || 'anonymous');
  }

  /**
   * 获取事件监听器数量
   * @param {string} event 事件名称
   */
  listenerCount(event) {
    const listeners = this.events.get(event);
    return listeners ? listeners.size : 0;
  }

  /**
   * 获取所有事件名称
   */
  eventNames() {
    return Array.from(this.events.keys());
  }

  /**
   * 检查是否有监听器
   * @param {string} event 事件名称
   */
  hasListeners(event) {
    return this.listenerCount(event) > 0;
  }

  /**
   * 获取统计信息
   */
  getStatistics() {
    return {
      ...this.statistics,
      eventCounts: Object.fromEntries(this.statistics.eventCounts),
      eventsCount: this.events.size,
      middlewaresCount: this.middlewares.length
    };
  }

  /**
   * 等待事件触发
   * @param {string} event 事件名称
   * @param {number} timeout 超时时间（毫秒）
   */
  waitFor(event, timeout = 30000) {
    return new Promise((resolve, reject) => {
      let timeoutId;

      const cleanup = () => {
        if (timeoutId) {
          clearTimeout(timeoutId);
        }
      };

      const handler = (data) => {
        cleanup();
        resolve(data);
      };

      this.once(event, handler);

      if (timeout > 0) {
        timeoutId = setTimeout(() => {
          this.off(event, handler);
          reject(new Error(`等待事件 "${event}" 超时`));
        }, timeout);
      }
    });
  }

  /**
   * 批量触发事件
   * @param {Array} events 事件数组
   * @param {Object} options 选项
   */
  async emitBatch(events, options = {}) {
    const promises = events.map(({ event, data }) =>
      this.emit(event, data, options)
    );

    try {
      const results = await Promise.allSettled(promises);
      return {
        success: true,
        results,
        total: events.length
      };
    } catch (error) {
      return {
        success: false,
        error,
        total: events.length
      };
    }
  }

  /**
   * 创建命名空间
   * @param {string} namespace 命名空间名称
   */
  namespace(namespace) {
    const emitter = this;

    return {
      on(event, listener, options) {
        return emitter.on(`${namespace}:${event}`, listener, options);
      },
      once(event, listener, options) {
        return emitter.once(`${namespace}:${event}`, listener, options);
      },
      off(event, listener) {
        return emitter.off(`${namespace}:${event}`, listener);
      },
      emit(event, data, options) {
        return emitter.emit(`${namespace}:${event}`, data, options);
      },
      removeAllListeners(event) {
        if (event) {
          return emitter.removeAllListeners(`${namespace}:${event}`);
        }
        return emitter.removeAllListeners();
      },
      listenerCount(event) {
        return emitter.listenerCount(`${namespace}:${event}`);
      },
      waitFor(event, timeout) {
        return emitter.waitFor(`${namespace}:${event}`, timeout);
      }
    };
  }

  /**
   * 生成监听器ID
   */
  generateListenerId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  /**
   * 日志输出
   */
  log(...args) {
    if (this.debug) {
      console.log('[EventEmitter]', ...args);
    }
  }

  /**
   * 销毁事件发射器
   */
  destroy() {
    this.removeAllListeners();
    this.middlewares = [];
    this.statistics.totalEvents = 0;
    this.statistics.totalListeners = 0;
    this.statistics.eventCounts.clear();
  }
}

/**
 * 全局事件发射器实例
 */
export const globalEventEmitter = new EventEmitter({
  debug: process.env.NODE_ENV === 'development',
  maxListeners: 200
});

/**
 * Vue3 Composable: useEventBus
 */
export function useEventBus(namespace = null) {
  const emitter = namespace ? globalEventEmitter.namespace(namespace) : globalEventEmitter;
  const listeners = [];

  // 组件卸载时清理监听器
  onUnmounted(() => {
    listeners.forEach(({ event, listener }) => {
      emitter.off(event, listener);
    });
    listeners.length = 0;
  });

  return {
    on: (event, listener, options) => {
      const off = emitter.on(event, listener, options);
      listeners.push({ event, listener });
      return off;
    },
    once: (event, listener, options) => {
      const off = emitter.once(event, listener, options);
      listeners.push({ event, listener });
      return off;
    },
    off: (event, listener) => emitter.off(event, listener),
    emit: (event, data, options) => emitter.emit(event, data, options),
    emitBatch: (events, options) => emitter.emitBatch(events, options),
    removeAllListeners: (event) => emitter.removeAllListeners(event),
    listenerCount: (event) => emitter.listenerCount(event),
    hasListeners: (event) => emitter.hasListeners(event),
    eventNames: () => emitter.eventNames(),
    waitFor: (event, timeout) => emitter.waitFor(event, timeout),
    getStatistics: () => emitter.getStatistics()
  };
}

/**
 * 事件过滤器工具
 */
export class EventFilter {
  constructor() {
    this.filters = new Map();
  }

  /**
   * 添加过滤器
   * @param {string} event 事件名称
   * @param {Function} filterFn 过滤函数
   */
  addFilter(event, filterFn) {
    if (!this.filters.has(event)) {
      this.filters.set(event, new Set());
    }
    this.filters.get(event).add(filterFn);
  }

  /**
   * 移除过滤器
   * @param {string} event 事件名称
   * @param {Function} filterFn 过滤函数
   */
  removeFilter(event, filterFn) {
    const filters = this.filters.get(event);
    if (filters) {
      filters.delete(filterFn);
      if (filters.size === 0) {
        this.filters.delete(event);
      }
    }
  }

  /**
   * 应用过滤器
   * @param {string} event 事件名称
   * @param {*} data 事件数据
   */
  applyFilters(event, data) {
    const filters = this.filters.get(event);
    if (!filters || filters.size === 0) {
      return true;
    }

    for (const filterFn of filters) {
      try {
        if (!filterFn(data)) {
          return false;
        }
      } catch (error) {
        console.error('事件过滤器执行失败:', error);
        return false;
      }
    }

    return true;
  }

  /**
   * 清空所有过滤器
   */
  clearFilters() {
    this.filters.clear();
  }
}

/**
 * 事件缓存工具
 */
export class EventCache {
  constructor(maxSize = 1000) {
    this.cache = new Map();
    this.maxSize = maxSize;
    this.accessOrder = [];
  }

  /**
   * 缓存事件
   * @param {string} event 事件名称
   * @param {*} data 事件数据
   * @param {number} ttl 过期时间（毫秒）
   */
  set(event, data, ttl = 60000) {
    // 如果缓存已满，删除最旧的条目
    if (this.cache.size >= this.maxSize) {
      const oldestKey = this.accessOrder.shift();
      this.cache.delete(oldestKey);
    }

    const key = `${event}_${Date.now()}`;
    const item = {
      event,
      data,
      timestamp: Date.now(),
      ttl,
      expiresAt: Date.now() + ttl
    };

    this.cache.set(key, item);
    this.accessOrder.push(key);

    // 清理过期条目
    this.cleanup();

    return key;
  }

  /**
   * 获取缓存的事件
   * @param {string} event 事件名称
   * @param {number} limit 限制数量
   */
  get(event, limit = 10) {
    const now = Date.now();
    const results = [];

    for (const [key, item] of this.cache) {
      if (item.event === event && item.expiresAt > now) {
        results.push(item.data);
        if (results.length >= limit) {
          break;
        }
      }
    }

    return results;
  }

  /**
   * 获取最新的事件
   * @param {string} event 事件名称
   */
  getLatest(event) {
    const events = this.get(event, 1);
    return events.length > 0 ? events[0] : null;
  }

  /**
   * 清理过期条目
   */
  cleanup() {
    const now = Date.now();
    const expiredKeys = [];

    for (const [key, item] of this.cache) {
      if (item.expiresAt <= now) {
        expiredKeys.push(key);
      }
    }

    expiredKeys.forEach(key => {
      this.cache.delete(key);
      const index = this.accessOrder.indexOf(key);
      if (index > -1) {
        this.accessOrder.splice(index, 1);
      }
    });
  }

  /**
   * 清空缓存
   */
  clear() {
    this.cache.clear();
    this.accessOrder = [];
  }

  /**
   * 获取缓存统计
   */
  getStats() {
    return {
      size: this.cache.size,
      maxSize: this.maxSize,
      memoryUsage: JSON.stringify([...this.cache]).length
    };
  }
}

/**
 * 创建带有事件过滤和缓存的事件发射器
 */
export function createEnhancedEventEmitter(options = {}) {
  const emitter = new EventEmitter(options);
  const filter = new EventFilter();
  const cache = new EventCache(options.cacheSize || 1000);

  const originalEmit = emitter.emit.bind(emitter);

  emitter.emit = async function(event, data, options = {}) {
    // 应用过滤器
    if (!filter.applyFilters(event, data)) {
      return { success: false, reason: 'Event filtered' };
    }

    // 缓存事件
    if (options.cache !== false) {
      cache.set(event, data, options.cacheTtl || 60000);
    }

    // 触发原始事件
    return originalEmit(event, data, options);
  };

  // 添加过滤器方法
  emitter.addFilter = (event, filterFn) => filter.addFilter(event, filterFn);
  emitter.removeFilter = (event, filterFn) => filter.removeFilter(event, filterFn);

  // 添加缓存方法
  emitter.getCachedEvents = (event, limit) => cache.get(event, limit);
  emitter.getLatestCachedEvent = (event) => cache.getLatest(event);
  emitter.clearCache = () => cache.clear();
  emitter.getCacheStats = () => cache.getStats();

  return emitter;
}