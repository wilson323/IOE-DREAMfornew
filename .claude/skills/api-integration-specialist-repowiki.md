# API集成专家 (Repowiki标准版)

## 🎯 技能定位
**核心职责**: 基于 `.qoder/repowiki` 中的《API集成详解》规范，确保IOE-DREAM项目API集成严格遵循现代化标准，构建高效、安全、可维护的API调用体系

**⚡ 技能等级**: ★★★★★ (API集成专家)
**🎯 适用场景**: API架构设计、HTTP客户端配置、接口规范制定、错误处理优化、API性能调优
**📊 技能覆盖**: Axios HTTP客户端 | RESTful API设计 | GraphQL支持 | 错误处理 | 缓存策略 | 安全认证

---

## 📋 技能概述 (基于Repowiki规范)

### **核心专长 (基于.qoder/repowiki/zh/content/前端架构/API集成/)**
- **HTTP客户端守护**: 严格确保Axios最佳实践和现代化配置
- **API架构设计**: RESTful API设计规范和GraphQL支持
- **错误处理系统**: 统一的错误处理和用户友好的错误提示
- **性能优化策略**: 请求缓存、并发控制、数据预加载
- **安全保障机制**: Token认证、请求加密、CSRF防护

### **解决能力**
- **API架构合规性**: 100%符合repowiki API集成规范
- **HTTP客户端优化**: 高效的请求拦截和响应处理
- **错误处理完善**: 统一的错误处理和异常恢复机制
- **性能问题解决**: 请求性能优化和资源使用优化
- **安全漏洞防护**: API调用安全和数据传输安全

---

## 🏗️ Repowiki API集成规范

### **API架构核心原则**

#### **分层API设计**
```
src/api/
├── config/                # API配置
│   ├── index.ts          # 主配置文件
│   ├── interceptors.ts   # 拦截器配置
│   └── adapters.ts       # 适配器配置
├── modules/              # API模块
│   ├── auth/             # 认证API
│   ├── user/             # 用户API
│   ├── consume/          # 消费API
│   ├── access/           # 门禁API
│   └── system/           # 系统API
├── types/                # 类型定义
│   ├── common.ts         # 通用类型
│   ├── response.ts       # 响应类型
│   └── error.ts          # 错误类型
├── utils/                # 工具函数
│   ├── request.ts        # 请求工具
│   ├── cache.ts          # 缓存工具
│   └── error.ts          # 错误处理
└── index.ts              # API导出
```

#### **RESTful API规范**
- **HTTP方法语义**: GET(查询)、POST(创建)、PUT(更新)、DELETE(删除)
- **URL设计规范**: 资源导向、复数名词、层级清晰
- **状态码规范**: 标准HTTP状态码，语义明确
- **版本管理**: 通过URL路径或Header进行版本控制

#### **GraphQL支持**
- **Schema定义**: 类型安全的查询和变更
- **查询优化**: 字段选择、数据预加载、缓存策略
- **订阅支持**: 实时数据更新和事件通知
- **错误处理**: GraphQL错误格式和用户友好提示

### **API分类体系**

#### **1. 核心API (Core APIs)**
- **认证相关**: 登录、注册、token刷新
- **用户管理**: 用户信息、权限、设置
- **系统配置**: 应用配置、字典数据

#### **2. 业务API (Business APIs)**
- **消费管理**: 消费记录、统计、设置
- **门禁系统**: 设备管理、通行记录
- **考勤管理**: 考勤记录、统计报表

#### **3. 工具API (Utility APIs)**
- **文件上传**: 文件管理、图片处理
- **数据导出**: Excel导出、PDF生成
- **消息通知**: 站内消息、邮件通知

---

## 🛠️ 核心工作流程 (基于Repowiki)

### **Phase 1: API集成合规性诊断**
```bash
# 检查Axios配置合规性
./scripts/check-axios-compliance.sh

# 检测API设计规范
./scripts/analyze-api-design.sh

# 验证错误处理机制
./scripts/validate-error-handling.sh

# 检查API安全性
./scripts/check-api-security.sh
```

### **Phase 2: API架构优化**
```bash
# HTTP客户端重构
./scripts/refactor-http-client.sh

# API模块优化
./scripts/optimize-api-modules.sh

# 错误处理优化
./scripts/optimize-error-handling.sh

# 缓存策略配置
./scripts/configure-api-caching.sh
```

### **Phase 3: API质量保障**
```bash
# API功能测试
npm run test:api

# API性能测试
npm run test:api-performance

# API安全测试
npm run test:api-security

# API集成测试
npm run test:api-integration
```

---

## 🔍 API集成合规性检查清单 (基于Repowiki)

### **✅ 强制性规范 (必须100%遵循)**

#### **Axios配置规范**
- [ ] 使用Axios作为HTTP客户端库
- [ ] 配置合理的baseURL和timeout
- [ ] 实现请求和响应拦截器
- [ ] 配置请求重试和错误恢复机制
- [ ] 支持请求取消和并发控制

#### **API设计规范**
- [ ] 严格遵循RESTful API设计原则
- [ ] 使用标准HTTP方法和状态码
- [ ] API路径语义化，使用复数名词
- [ ] 实现统一的响应格式
- [ ] 支持分页、排序、筛选功能

#### **错误处理规范**
- [ ] 实现统一的错误处理机制
- [ ] 网络错误和业务错误分别处理
- [ ] 用户友好的错误提示信息
- [ ] 错误日志记录和监控
- [ ] 自动重试和降级策略

### **⚠️ 推荐性规范**

#### **性能优化规范**
- [ ] 请求缓存和去重机制
- [ ] 并发请求控制和队列管理
- [ ] 数据预加载和懒加载策略
- [ ] 响应数据压缩和优化
- [ ] CDN加速和资源优化

#### **安全保障规范**
- [ ] Token认证和自动刷新
- [ ] 请求签名和数据加密
- [ ] CSRF和XSS攻击防护
- [ ] API调用频率限制
- [ ] 敏感数据脱敏处理

---

## 🚀 API集成最佳实践

### **HTTP客户端主配置示例**
```typescript
// api/config/index.ts
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { setupInterceptors } from './interceptors'
import { setupAdapters } from './adapters'
import type { ApiResponse, ApiError } from '../types'

// API配置
const apiConfig: AxiosRequestConfig = {
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  }
}

// 创建Axios实例
const apiClient: AxiosInstance = axios.create(apiConfig)

// 设置适配器
setupAdapters(apiClient)

// 设置拦截器
setupInterceptors(apiClient)

// 请求方法封装
export class ApiClient {
  private client: AxiosInstance

  constructor(client: AxiosInstance) {
    this.client = client
  }

  // GET请求
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.get<ApiResponse<T>>(url, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // POST请求
  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.post<ApiResponse<T>>(url, data, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // PUT请求
  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.put<ApiResponse<T>>(url, data, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // DELETE请求
  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.delete<ApiResponse<T>>(url, config)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // 文件上传
  async upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.post<ApiResponse<T>>(url, formData, {
        ...config,
        headers: {
          'Content-Type': 'multipart/form-data',
          ...config?.headers
        }
      })
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // 错误处理
  private handleError(error: any): ApiError {
    if (axios.isAxiosError(error)) {
      const { response, request } = error

      if (response) {
        // 服务器响应错误
        return {
          code: response.status,
          message: response.data?.message || '服务器错误',
          data: response.data,
          config: error.config
        }
      } else if (request) {
        // 网络错误
        return {
          code: 0,
          message: '网络连接失败，请检查网络设置',
          config: error.config
        }
      }
    }

    // 其他错误
    return {
      code: -1,
      message: error.message || '未知错误',
      config: error.config
    }
  }
}

// 导出API客户端实例
export const apiClientInstance = new ApiClient(apiClient)
```

### **拦截器配置示例**
```typescript
// api/config/interceptors.ts
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/modules/user'
import { showToast, showLoading, hideLoading } from '@/utils/message'
import { getAuthToken, removeAuthToken } from '@/utils/auth'
import { refreshToken } from '@/api/auth'

export function setupInterceptors(client: AxiosInstance) {
  let isRefreshing = false
  let refreshSubscribers: ((token: string) => void)[] = []

  // 请求拦截器
  client.interceptors.request.use(
    (config: AxiosRequestConfig) => {
      // 显示loading
      if (config.showLoading !== false) {
        showLoading()
      }

      // 添加认证token
      const token = getAuthToken()
      if (token) {
        config.headers = {
          ...config.headers,
          Authorization: `Bearer ${token}`
        }
      }

      // 添加请求ID用于追踪
      config.headers = {
        ...config.headers,
        'X-Request-ID': generateRequestId()
      }

      return config
    },
    (error) => {
      hideLoading()
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  client.interceptors.response.use(
    (response: AxiosResponse) => {
      // 隐藏loading
      hideLoading()

      // 检查业务状态码
      const { data } = response
      if (data.code !== 200 && data.code !== 0) {
        showToast(data.message || '请求失败', 'error')
        return Promise.reject(new Error(data.message))
      }

      return response
    },
    async (error) => {
      hideLoading()

      const { response, config } = error

      if (response) {
        const { status } = response

        switch (status) {
          case 401:
            // Token过期，尝试刷新
            if (!config._retry) {
              return handleTokenRefresh(config)
            } else {
              // 刷新失败，跳转登录
              removeAuthToken()
              window.location.href = '/login'
            }
            break

          case 403:
            showToast('无权限访问', 'error')
            break

          case 404:
            showToast('请求的资源不存在', 'error')
            break

          case 500:
            showToast('服务器内部错误', 'error')
            break

          default:
            showToast(response.data?.message || '请求失败', 'error')
        }
      } else if (error.code === 'ECONNABORTED') {
        showToast('请求超时，请重试', 'error')
      } else {
        showToast('网络连接失败', 'error')
      }

      return Promise.reject(error)
    }
  )

  // Token刷新处理
  async function handleTokenRefresh(config: AxiosRequestConfig) {
    if (isRefreshing) {
      // 如果正在刷新，将请求加入队列
      return new Promise((resolve) => {
        refreshSubscribers.push((token: string) => {
          config.headers.Authorization = `Bearer ${token}`
          resolve(client(config))
        })
      })
    }

    isRefreshing = true

    try {
      const response = await refreshToken()
      const newToken = response.data.token

      // 保存新token
      localStorage.setItem('auth_token', newToken)

      // 通知所有等待的请求
      refreshSubscribers.forEach(callback => callback(newToken))
      refreshSubscribers = []

      // 重新发起原请求
      config.headers.Authorization = `Bearer ${newToken}`
      return client(config)
    } catch (refreshError) {
      // 刷新失败，清除token并跳转登录
      removeAuthToken()
      window.location.href = '/login'
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  }
}

// 生成请求ID
function generateRequestId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}
```

### **API模块示例**
```typescript
// api/modules/consume/index.ts
import { apiClientInstance } from '../../config'
import type { ConsumeRecord, ConsumeFilters, ConsumeStatistics, PaginationParams } from '../../types'

export interface ConsumeApi {
  // 获取消费记录列表
  getRecords(params: ConsumeFilters & PaginationParams): Promise<PageResult<ConsumeRecord>>

  // 获取消费记录详情
  getRecord(id: string): Promise<ConsumeRecord>

  // 创建消费记录
  createRecord(data: Omit<ConsumeRecord, 'id' | 'createTime'>): Promise<ConsumeRecord>

  // 更新消费记录
  updateRecord(id: string, data: Partial<ConsumeRecord>): Promise<ConsumeRecord>

  // 删除消费记录
  deleteRecord(id: string): Promise<void>

  // 获取消费统计
  getStatistics(params?: ConsumeFilters): Promise<ConsumeStatistics>

  // 导出消费记录
  exportRecords(params: ConsumeFilters): Promise<Blob>

  // 批量操作
  batchDelete(ids: string[]): Promise<void>
}

export const consumeApi: ConsumeApi = {
  getRecords(params) {
    return apiClientInstance.get('/consume/records', { params })
  },

  getRecord(id: string) {
    return apiClientInstance.get(`/consume/records/${id}`)
  },

  createRecord(data) {
    return apiClientInstance.post('/consume/records', data)
  },

  updateRecord(id: string, data) {
    return apiClientInstance.put(`/consume/records/${id}`, data)
  },

  deleteRecord(id: string) {
    return apiClientInstance.delete(`/consume/records/${id}`)
  },

  getStatistics(params) {
    return apiClientInstance.get('/consume/statistics', { params })
  },

  exportRecords(params) {
    return apiClientInstance.get('/consume/records/export', {
      params,
      responseType: 'blob'
    })
  },

  batchDelete(ids: string[]) {
    return apiClientInstance.post('/consume/records/batch-delete', { ids })
  }
}
```

### **API缓存工具示例**
```typescript
// api/utils/cache.ts
interface CacheEntry<T = any> {
  data: T
  timestamp: number
  ttl: number
}

class ApiCache {
  private cache = new Map<string, CacheEntry>()

  // 设置缓存
  set<T>(key: string, data: T, ttl: number = 300000): void {
    const entry: CacheEntry<T> = {
      data,
      timestamp: Date.now(),
      ttl
    }
    this.cache.set(key, entry)
  }

  // 获取缓存
  get<T>(key: string): T | null {
    const entry = this.cache.get(key)
    if (!entry) return null

    // 检查是否过期
    if (Date.now() - entry.timestamp > entry.ttl) {
      this.cache.delete(key)
      return null
    }

    return entry.data as T
  }

  // 删除缓存
  delete(key: string): void {
    this.cache.delete(key)
  }

  // 清空缓存
  clear(): void {
    this.cache.clear()
  }

  // 批量删除缓存
  deleteByPattern(pattern: string): void {
    const regex = new RegExp(pattern)
    for (const key of this.cache.keys()) {
      if (regex.test(key)) {
        this.cache.delete(key)
      }
    }
  }
}

export const apiCache = new ApiCache()

// 请求缓存装饰器
export function withCache(ttl: number = 300000) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const originalMethod = descriptor.value

    descriptor.value = async function (...args: any[]) {
      // 生成缓存key
      const cacheKey = `${propertyKey}_${JSON.stringify(args)}`

      // 尝试从缓存获取
      const cachedData = apiCache.get(cacheKey)
      if (cachedData) {
        return cachedData
      }

      // 执行原方法
      const result = await originalMethod.apply(this, args)

      // 设置缓存
      apiCache.set(cacheKey, result, ttl)

      return result
    }

    return descriptor
  }
}
```

### **错误处理工具示例**
```typescript
// api/utils/error.ts
import type { ApiError } from '../types'

export class ApiErrorHandler {
  // 错误类型映射
  private errorMessages: Record<number, string> = {
    400: '请求参数错误',
    401: '未授权，请重新登录',
    403: '拒绝访问',
    404: '请求的资源不存在',
    405: '请求方法不被允许',
    408: '请求超时',
    429: '请求过于频繁，请稍后再试',
    500: '服务器内部错误',
    502: '网关错误',
    503: '服务暂不可用',
    504: '网关超时'
  }

  // 处理API错误
  handle(error: ApiError): string {
    // 网络错误
    if (error.code === 0) {
      return '网络连接失败，请检查网络设置'
    }

    // 业务错误
    if (error.code > 0 && error.message) {
      return error.message
    }

    // HTTP状态码错误
    if (this.errorMessages[error.code]) {
      return this.errorMessages[error.code]
    }

    // 默认错误信息
    return error.message || '未知错误，请稍后重试'
  }

  // 记录错误日志
  log(error: ApiError, context?: string): void {
    const logData = {
      timestamp: new Date().toISOString(),
      error: {
        code: error.code,
        message: error.message,
        data: error.data
      },
      context,
      url: error.config?.url,
      method: error.config?.method
    }

    console.error('API Error:', logData)

    // 发送错误日志到监控系统
    this.sendToMonitoring(logData)
  }

  // 发送到监控系统
  private async sendToMonitoring(logData: any): Promise<void> {
    try {
      // 这里可以集成错误监控服务
      // await errorMonitoringService.captureException(logData)
    } catch (error) {
      console.error('Failed to send error to monitoring:', error)
    }
  }
}

export const apiErrorHandler = new ApiErrorHandler()

// 错误边界组件
export function createErrorBoundary(fallbackComponent: any) {
  return {
    name: 'ErrorBoundary',
    data() {
      return {
        hasError: false,
        error: null as any
      }
    },
    errorCaptured(error: any, instance: any, info: string) {
      this.hasError = true
      this.error = error

      // 记录错误
      apiErrorHandler.log({
        code: -1,
        message: error.message,
        config: null
      }, 'component')

      return false
    },
    render() {
      if (this.hasError) {
        return h(fallbackComponent, {
          error: this.error,
          onReset: () => {
            this.hasError = false
            this.error = null
          }
        })
      }
      return this.$slots.default()
    }
  }
}
```

---

## 📊 API集成质量评估标准

### **API架构评分**
| 维度 | 权重 | 评分标准 |
|------|------|----------|
| HTTP客户端配置 | 30% | Axios配置完善，拦截器合理 |
| API设计规范 | 25% | RESTful设计，语义明确 |
| 错误处理机制 | 20% | 统一错误处理，用户友好 |
| 性能优化程度 | 15% | 缓存策略，并发控制 |
| 安全保障 | 10% | 认证授权，数据安全 |

### **质量等级**
- **A级 (90-100分)**: 完全符合repowiki API集成规范
- **B级 (80-89分)**: 基本合规，存在轻微优化空间
- **C级 (70-79分)**: 部分合规，需要重点改进
- **D级 (60-69分)**: API设计混乱，需要重构
- **E级 (0-59分)**: 严重违反API集成规范

---

## 🎯 使用指南

### **何时调用**
- API架构设计和技术选型时
- HTTP客户端配置和优化时
- API错误处理和用户体验优化时
- API性能问题诊断和优化时
- API安全漏洞检查和修复时

### **调用方式**
```bash
# 基于repowiki的API集成专家
Skill("api-integration-specialist-repowiki")

# 将立即执行：
# 1. 基于.qoder/repowiki API集成规范检查
# 2. HTTP客户端配置和拦截器优化
# 3. API设计规范和错误处理分析
# 4. API性能优化和安全加固
```

### **预期结果**
- 100%符合`.qoder/repowiki` API集成规范
- 高效、稳定的HTTP客户端配置
- 完善的错误处理和用户体验
- 安全、可靠的API调用体系

---

**🏆 技能等级**: API集成专家 (★★★★★)
**⏰ 预期效果**: 基于249个repowiki权威文档，确保IOE-DREAM API集成100%符合现代化标准
**🎯 核心价值**: HTTP客户端守护，API架构优化，开发效率提升