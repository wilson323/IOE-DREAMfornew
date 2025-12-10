/**
 * 企业级轻量级前端优化管理器
 * <p>
 * 提供核心的Vue3组件优化、性能监控、错误处理、缓存管理等企业级前端功能
 * 避免过度工程化，保持轻量级且功能完善
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-09
 */

class FrontendOptimizationManager {
    constructor() {
        this.performanceMetrics = new Map();
        this.cacheManager = new Map();
        this.loadingStates = new Map();
        this.errorCount = 0;
        this.apiPerformance = [];

        this.init();
    }

    /**
     * 初始化前端优化管理器
     */
    init() {
        this.initPerformanceMonitoring();
        this.initErrorHandling();
        this.initCacheManagement();

        console.log('[前端优化] 轻量级企业级前端优化管理器初始化完成');
    }

    /**
     * 性能监控
     */
    initPerformanceMonitoring() {
        // 页面加载性能监控
        if ('performance' in window) {
            window.addEventListener('load', () => {
                const perfData = performance.getEntriesByType('navigation')[0];
                this.recordPerformanceMetric('pageLoad', {
                    domContentLoaded: perfData.domContentLoadedEventEnd - perfData.domContentLoadedEventStart,
                    loadComplete: perfData.loadEventEnd - perfData.loadEventStart
                });
            });
        }
    }

    /**
     * 错误处理
     */
    initErrorHandling() {
        // 全局JavaScript错误处理
        window.addEventListener('error', (event) => {
            this.errorCount++;
            this.recordError('javascript', {
                message: event.message,
                filename: event.filename,
                lineno: event.lineno
            });
        });

        // Promise错误处理
        window.addEventListener('unhandledrejection', (event) => {
            this.errorCount++;
            this.recordError('promise', {
                reason: event.reason
            });
        });
    }

    /**
     * 缓存管理
     */
    initCacheManagement() {
        // 设置缓存策略
        this.setCacheStrategy('api', {
            ttl: 5 * 60 * 1000, // 5分钟
            maxSize: 50
        });

        this.setCacheStrategy('static', {
            ttl: 24 * 60 * 60 * 1000, // 24小时
            maxSize: 100
        });
    }

    /**
     * Vue组件轻量级优化装饰器
     */
    static createOptimizedComponent(options) {
        return {
            ...options,
            // 添加性能监控
            mounted() {
                if (this._startTime) {
                    const mountTime = Date.now() - this._startTime;
                    window.frontendOptimization?.recordPerformanceMetric('componentMount', {
                        name: this.$options.name || 'anonymous',
                        mountTime: mountTime
                    });
                }
                if (options.mounted) {
                    options.mounted.call(this);
                }
            },

            created() {
                this._startTime = Date.now();
                if (options.created) {
                    options.created.call(this);
                }
            },

            methods: {
                ...options.methods,

                // 轻量级防抖方法
                $debounce(method, delay = 300) {
                    let timeoutId;
                    return (...args) => {
                        clearTimeout(timeoutId);
                        timeoutId = setTimeout(() => {
                            this[method].apply(this, args);
                        }, delay);
                    };
                },

                // 轻量级节流方法
                $throttle(method, delay = 300) {
                    let lastCall = 0;
                    return (...args) => {
                        const now = Date.now();
                        if (now - lastCall >= delay) {
                            lastCall = now;
                            this[method].apply(this, args);
                        }
                    };
                }
            }
        };
    }

    /**
     * API请求轻量级优化
     */
    static optimizeApiRequest(axiosInstance) {
        // 请求拦截器
        axiosInstance.interceptors.request.use(
            (config) => {
                config.metadata = { startTime: Date.now() };

                // 简单缓存检查
                if (config.cache !== false) {
                    const cacheKey = `${config.method}_${config.url}`;
                    const cachedResponse = window.frontendOptimization?.getFromCache('api', cacheKey);
                    if (cachedResponse) {
                        return Promise.resolve({ data: cachedResponse, fromCache: true });
                    }
                }

                return config;
            },
            (error) => {
                window.frontendOptimization?.recordError('api_request', error);
                return Promise.reject(error);
            }
        );

        // 响应拦截器
        axiosInstance.interceptors.response.use(
            (response) => {
                // 记录API性能
                if (response.config.metadata) {
                    const duration = Date.now() - response.config.metadata.startTime;
                    window.frontendOptimization?.recordApiPerformance(response.config.url, duration, true);
                }

                // 缓存响应
                if (response.config.cache !== false && response.status === 200) {
                    const cacheKey = `${response.config.method}_${response.config.url}`;
                    window.frontendOptimization?.setCache('api', cacheKey, response.data);
                }

                return response;
            },
            (error) => {
                // 记录API性能和错误
                if (error.config?.metadata) {
                    const duration = Date.now() - error.config.metadata.startTime;
                    window.frontendOptimization?.recordApiPerformance(error.config.url, duration, false);
                }
                window.frontendOptimization?.recordError('api_response', error);

                return Promise.reject(error);
            }
        );

        return axiosInstance;
    }

    /**
     * 记录性能指标
     */
    recordPerformanceMetric(name, data) {
        this.performanceMetrics.set(name, {
            ...data,
            timestamp: Date.now()
        });

        console.log('[性能监控]', name, data);
    }

    /**
     * 记录错误
     */
    recordError(type, error) {
        const errorRecord = {
            type,
            message: error.message || String(error),
            url: window.location.href,
            timestamp: Date.now()
        };

        console.error('[错误处理]', errorRecord);

        // 简单错误上报（可选）
        this.reportError(errorRecord);
    }

    /**
     * 记录API性能
     */
    recordApiPerformance(url, duration, success) {
        this.apiPerformance.push({
            url,
            duration,
            success,
            timestamp: Date.now()
        });

        // 保持数组大小合理
        if (this.apiPerformance.length > 100) {
            this.apiPerformance.shift();
        }
    }

    /**
     * 缓存操作
     */
    setCache(type, key, value) {
        const strategy = this.cacheManager.get(type);
        if (!strategy) {
            return;
        }

        try {
            const cacheKey = `${type}:${key}`;
            const cacheItem = {
                value,
                timestamp: Date.now(),
                ttl: strategy.ttl
            };

            localStorage.setItem(cacheKey, JSON.stringify(cacheItem));

        } catch (error) {
            console.warn('[缓存管理] 设置缓存失败:', error);
        }
    }

    getFromCache(type, key) {
        const strategy = this.cacheManager.get(type);
        if (!strategy) {
            return null;
        }

        try {
            const cacheKey = `${type}:${key}`;
            const cacheItem = localStorage.getItem(cacheKey);

            if (!cacheItem) {
                return null;
            }

            const parsed = JSON.parse(cacheItem);

            // 检查是否过期
            if (Date.now() - parsed.timestamp > parsed.ttl) {
                localStorage.removeItem(cacheKey);
                return null;
            }

            return parsed.value;

        } catch (error) {
            console.warn('[缓存管理] 获取缓存失败:', error);
            return null;
        }
    }

    /**
     * 设置加载状态
     */
    setLoading(key, loading) {
        this.loadingStates.set(key, loading);
    }

    /**
     * 获取加载状态
     */
    isLoading(key) {
        return this.loadingStates.get(key) || false;
    }

    /**
     * 设置缓存策略
     */
    setCacheStrategy(type, strategy) {
        this.cacheManager.set(type, strategy);
    }

    /**
     * 简单错误上报
     */
    reportError(errorRecord) {
        // 简单的错误上报逻辑，可以扩展到具体的监控系统
        try {
            // 这里可以发送到后端错误监控接口
            // fetch('/api/error/report', { method: 'POST', body: JSON.stringify(errorRecord) });
        } catch (e) {
            console.warn('[错误上报] 上报失败:', e);
        }
    }

    /**
     * 获取性能统计
     */
    getPerformanceStats() {
        return {
            metrics: Object.fromEntries(this.performanceMetrics),
            apiPerformance: this.apiPerformance.slice(-10), // 最近10条API性能
            errorCount: this.errorCount
        };
    }
}

// 创建全局实例
window.frontendOptimization = new FrontendOptimizationManager();

// 导出工厂函数
export const createOptimizedComponent = FrontendOptimizationManager.createOptimizedComponent;
export const optimizeApiRequest = FrontendOptimizationManager.optimizeApiRequest;

export default FrontendOptimizationManager;