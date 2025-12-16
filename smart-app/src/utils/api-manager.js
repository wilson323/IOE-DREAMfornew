/**
 * API请求管理工具
 * 统一管理网络请求、错误处理、重试机制等
 */

import { performanceMonitor } from './performance.js'
import { errorHandler } from './ux-enhancement.js'

const normalizeBaseUrl = (url) => {
  return url ? url.replace(/\/+$/, '') : ''
}

// API配置
const API_CONFIG = {
  baseURL: normalizeBaseUrl(import.meta.env.VITE_APP_API_URL),
  timeout: 10000,
  retryCount: 3,
  retryDelay: 1000,
  enableCache: true,
  cacheExpireTime: 300000, // 5分钟
  enableCompression: true
}

// 请求拦截器配置
const REQUEST_INTERCEPTORS = []
const RESPONSE_INTERCEPTORS = []

// 响应缓存
const responseCache = new Map()

// 请求队列管理
const requestQueue = new Map()
let requestId = 0

/**
 * 生成唯一请求ID
 */
const generateRequestId = () => {
  return `req_${Date.now()}_${++requestId}`
}

/**
 * 生成缓存键
 */
const getCacheKey = (url, method = 'GET', data = null) => {
  const keyData = data ? JSON.stringify(data) : ''
  return `${method}_${url}_${btoa(keyData).slice(0, 16)}`
}

/**
 * 获取缓存响应
 */
const getCachedResponse = (url, method, data) => {
  if (method.toUpperCase() !== 'GET' || !API_CONFIG.enableCache) {
    return null
  }

  const cacheKey = getCacheKey(url, method, data)
  const cached = responseCache.get(cacheKey)

  if (cached && Date.now() - cached.timestamp < API_CONFIG.cacheExpireTime) {
    return cached.data
  }

  // 清除过期缓存
  if (cached) {
    responseCache.delete(cacheKey)
  }

  return null
}

/**
 * 缓存响应数据
 */
const cacheResponse = (url, method, data, responseData) => {
  if (method.toUpperCase() !== 'GET' || !API_CONFIG.enableCache) {
    return
  }

  const cacheKey = getCacheKey(url, method, data)
  responseCache.set(cacheKey, {
    data: responseData,
    timestamp: Date.now()
  })

  // 限制缓存大小
  if (responseCache.size > 100) {
    const firstKey = responseCache.keys().next().value
    responseCache.delete(firstKey)
  }
}

/**
 * 清理过期缓存
 */
const clearExpiredCache = () => {
  const now = Date.now()
  for (const [key, value] of responseCache.entries()) {
    if (now - value.timestamp > API_CONFIG.cacheExpireTime) {
      responseCache.delete(key)
    }
  }
}

/**
 * 定期清理缓存
 */
setInterval(clearExpiredCache, 60000) // 每分钟清理一次

/**
 * 处理请求错误
 */
const handleRequestError = (error, config) => {
  const errorInfo = {
    type: 'request',
    error,
    config,
    timestamp: Date.now()
  }

  performanceMonitor.recordError(error)

  // 根据错误类型进行不同处理
  if (error.name === 'NetworkError' || !navigator.onLine) {
    throw new Error('网络连接失败，请检查网络设置')
  } else if (error.name === 'TimeoutError') {
    throw new Error('请求超时，请稍后重试')
  } else if (error.status === 401) {
    // 处理认证失败
    handleAuthError()
    throw new Error('登录已过期，请重新登录')
  } else if (error.status === 403) {
    throw new Error('没有权限进行此操作')
  } else if (error.status === 404) {
    throw new Error('请求的资源不存在')
  } else if (error.status >= 500) {
    throw new Error('服务器内部错误，请联系管理员')
  } else {
    throw error
  }
}

/**
 * 处理认证错误
 */
const handleAuthError = () => {
  // 清除用户认证信息
  uni.removeStorageSync('auth_token')
  uni.removeStorageSync('user_info')

  // 跳转到登录页面
  uni.reLaunch({
    url: '/pages/login/index'
  })
}

/**
 * 执行请求重试
 */
const executeRequestWithRetry = async (requestFn, config) => {
  let lastError = null

  for (let attempt = 0; attempt <= config.retryCount; attempt++) {
    try {
      const response = await requestFn()
      return response
    } catch (error) {
      lastError = error

      // 如果是最后一次尝试，直接抛出错误
      if (attempt === config.retryCount) {
        break
      }

      // 某些错误不需要重试
      if (error.status === 401 || error.status === 403 || error.status === 404) {
        break
      }

      // 等待后重试
      await new Promise(resolve => setTimeout(resolve, config.retryDelay * Math.pow(2, attempt)))
    }
  }

  throw lastError
}

/**
 * 压缩请求数据
 */
const compressRequestData = (data) => {
  if (!API_CONFIG.enableCompression || !data) {
    return data
  }

  try {
    // 实现压缩逻辑（这里简化处理）
    return JSON.stringify(data)
  } catch (error) {
    console.warn('数据压缩失败:', error)
    return data
  }
}

/**
 * 解压缩响应数据
 */
const decompressResponseData = (data) => {
  if (!API_CONFIG.enableCompression || !data) {
    return data
  }

  try {
    // 实现解压缩逻辑（这里简化处理）
    return typeof data === 'string' ? JSON.parse(data) : data
  } catch (error) {
    console.warn('数据解压缩失败:', error)
    return data
  }
}

/**
 * 发送HTTP请求
 */
const request = async (config) => {
  const requestId = generateRequestId()
  const startTime = Date.now()

  try {
    // 合并默认配置
    const finalConfig = {
      baseURL: API_CONFIG.baseURL,
      timeout: API_CONFIG.timeout,
      retryCount: API_CONFIG.retryCount,
      retryDelay: API_CONFIG.retryDelay,
      ...config
    }

    // 完整URL
    const fullUrl = finalConfig.url.startsWith('http')
      ? finalConfig.url
      : `${finalConfig.baseURL}${finalConfig.url}`

    // 检查缓存
    const cachedResponse = getCachedResponse(fullUrl, finalConfig.method, finalConfig.data)
    if (cachedResponse) {
      console.log(`使用缓存响应: ${fullUrl}`)
      return cachedResponse
    }

    // 检查是否已有相同请求在进行
    const existingRequest = requestQueue.get(fullUrl)
    if (existingRequest && finalConfig.method === 'GET') {
      console.log(`等待现有请求: ${fullUrl}`)
      return existingRequest
    }

    // 获取认证token
    const authToken = uni.getStorageSync('auth_token')

    // 请求头配置
    const headers = {
      'Content-Type': 'application/json',
      'X-Request-ID': requestId,
      'X-Client-Version': '1.0.0',
      'X-Platform': uni.getSystemInfoSync().platform,
      ...finalConfig.headers
    }

    if (authToken) {
      headers['Authorization'] = `Bearer ${authToken}`
    }

    // 准备请求配置
    const requestConfig = {
      url: fullUrl,
      method: finalConfig.method || 'GET',
      data: finalConfig.data,
      header: headers,
      timeout: finalConfig.timeout,
      dataType: 'json',
      responseType: finalConfig.responseType || 'text',
      enableCache: false, // 使用自定义缓存
      success: (res) => {
        console.log(`请求成功 [${requestId}]: ${finalConfig.method} ${fullUrl}`, res)
      },
      fail: (error) => {
        console.error(`请求失败 [${requestId}]: ${finalConfig.method} ${fullUrl}`, error)
      }
    }

    // 创建请求Promise
    const requestPromise = executeRequestWithRetry(async () => {
      return new Promise((resolve, reject) => {
        uni.request({
          ...requestConfig,
          success: (res) => {
            // 移除请求队列
            requestQueue.delete(fullUrl)

            // 记录响应时间
            const responseTime = performanceMonitor.recordApiResponseTime(fullUrl, startTime)

            // 处理响应数据
            let responseData = res.data

            // 检查业务状态码
            if (responseData && responseData.code !== undefined) {
              if (responseData.code === 200 || responseData.code === 0) {
                responseData = responseData.data || responseData
              } else {
                const businessError = new Error(responseData.message || '业务处理失败')
                businessError.code = responseData.code
                businessError.businessError = true
                reject(businessError)
                return
              }
            }

            // 解压缩响应数据
            responseData = decompressResponseData(responseData)

            // 缓存GET请求响应
            if (finalConfig.method === 'GET') {
              cacheResponse(fullUrl, finalConfig.method, finalConfig.data, responseData)
            }

            resolve(responseData)
          },
          fail: (error) => {
            // 移除请求队列
            requestQueue.delete(fullUrl)

            // 记录错误
            const responseTime = performanceMonitor.recordApiResponseTime(fullUrl, startTime)

            // 处理请求错误
            handleRequestError(error, finalConfig)
              .then(reject)
              .catch(reject)
          }
        })
      })
    }, finalConfig)

    // 添加到请求队列
    if (finalConfig.method === 'GET') {
      requestQueue.set(fullUrl, requestPromise)
    }

    return await requestPromise

  } catch (error) {
    // 记录错误
    performanceMonitor.recordError(error, {
      requestId,
      url: config.url,
      method: config.method
    })

    // 统一错误处理
    errorHandler.handleError(error, 'api', {
      requestId,
      url: config.url,
      method: config.method,
      duration: Date.now() - startTime
    })

    throw error
  }
}

/**
 * GET请求
 */
const get = (url, config = {}) => {
  return request({
    url,
    method: 'GET',
    ...config
  })
}

/**
 * POST请求
 */
const post = (url, data = {}, config = {}) => {
  return request({
    url,
    method: 'POST',
    data,
    ...config
  })
}

/**
 * PUT请求
 */
const put = (url, data = {}, config = {}) => {
  return request({
    url,
    method: 'PUT',
    data,
    ...config
  })
}

/**
 * DELETE请求
 */
const del = (url, config = {}) => {
  return request({
    url,
    method: 'DELETE',
    ...config
  })
}

/**
 * PATCH请求
 */
const patch = (url, data = {}, config = {}) => {
  return request({
    url,
    method: 'PATCH',
    data,
    ...config
  })
}

/**
 * 文件上传
 */
const upload = (url, filePath, config = {}) => {
  const requestId = generateRequestId()
  const startTime = Date.now()

  return new Promise((resolve, reject) => {
    const authToken = uni.getStorageSync('auth_token')

    uni.uploadFile({
      url: url.startsWith('http') ? url : `${API_CONFIG.baseURL}${url}`,
      filePath,
      name: config.name || 'file',
      formData: config.formData || {},
      header: {
        'Authorization': authToken ? `Bearer ${authToken}` : '',
        'X-Request-ID': requestId,
        ...config.header
      },
      success: (res) => {
        const responseTime = performanceMonitor.recordApiResponseTime(url, startTime)

        try {
          const data = JSON.parse(res.data)
          resolve(data)
        } catch (error) {
          reject(new Error('响应数据解析失败'))
        }
      },
      fail: (error) => {
        const responseTime = performanceMonitor.recordApiResponseTime(url, startTime)
        handleRequestError(error, config)
          .then(reject)
          .catch(reject)
      }
    })
  })
}

/**
 * 文件下载
 */
const download = (url, config = {}) => {
  const requestId = generateRequestId()
  const startTime = Date.now()

  return new Promise((resolve, reject) => {
    const authToken = uni.getStorageSync('auth_token')

    uni.downloadFile({
      url: url.startsWith('http') ? url : `${API_CONFIG.baseURL}${url}`,
      header: {
        'Authorization': authToken ? `Bearer ${authToken}` : '',
        'X-Request-ID': requestId,
        ...config.header
      },
      success: (res) => {
        const responseTime = performanceMonitor.recordApiResponseTime(url, startTime)
        resolve(res)
      },
      fail: (error) => {
        const responseTime = performanceMonitor.recordApiResponseTime(url, startTime)
        handleRequestError(error, config)
          .then(reject)
          .catch(reject)
      }
    })
  })
}

/**
 * 并发请求
 */
const all = (requests) => {
  return Promise.all(requests.map(requestConfig => request(requestConfig)))
}

/**
 * 批量请求（按顺序执行）
 */
const series = async (requests) => {
  const results = []
  for (const requestConfig of requests) {
    results.push(await request(requestConfig))
  }
  return results
}

/**
 * 取消所有请求
 */
const cancelAllRequests = () => {
  for (const [url, promise] of requestQueue.entries()) {
    // 这里可以实现请求取消逻辑
    console.log(`取消请求: ${url}`)
  }
  requestQueue.clear()
}

/**
 * 清理所有缓存
 */
const clearAllCache = () => {
  responseCache.clear()
  console.log('所有API缓存已清理')
}

/**
 * 获取请求统计信息
 */
const getStats = () => {
  return {
    cacheSize: responseCache.size,
    activeRequests: requestQueue.size,
    performanceReport: performanceMonitor.getPerformanceReport()
  }
}

/**
 * 设置API配置
 */
const setConfig = (newConfig) => {
  Object.assign(API_CONFIG, newConfig)
}

// 添加请求拦截器
const addRequestInterceptor = (interceptor) => {
  REQUEST_INTERCEPTORS.push(interceptor)
}

// 添加响应拦截器
const addResponseInterceptor = (interceptor) => {
  RESPONSE_INTERCEPTORS.push(interceptor)
}

// 导出API管理器
export const apiManager = {
  request,
  get,
  post,
  put,
  delete: del,
  patch,
  upload,
  download,
  all,
  series,
  cancelAllRequests,
  clearAllCache,
  getStats,
  setConfig,
  addRequestInterceptor,
  addResponseInterceptor,
  config: API_CONFIG
}

// 默认导出
export default apiManager
