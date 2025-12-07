# 状态管理专家 (Repowiki标准版)

## 🎯 技能定位
**核心职责**: 基于 `.qoder/repowiki` 中的《状态管理详解》规范，确保IOE-DREAM项目状态管理严格遵循Pinia最佳实践，构建高效、可维护的状态管理体系

**⚡ 技能等级**: ★★★★★ (状态管理专家)
**🎯 适用场景**: 状态架构设计、Pinia状态管理、数据流优化、状态持久化、状态性能调优
**📊 技能覆盖**: Pinia架构设计 | 状态分层管理 | 数据流控制 | 状态持久化 | 状态性能优化 | 开发体验优化

---

## 📋 技能概述 (基于Repowiki规范)

### **核心专长 (基于.qoder/repowiki/zh/content/前端架构/状态管理/)**
- **Pinia架构守护**: 严格确保Pinia状态管理最佳实践
- **状态分层设计**: 按业务域进行状态模块化分层
- **数据流规范**: 单向数据流和状态变更规范化
- **状态持久化**: 智能状态持久化和恢复机制
- **性能优化**: 状态管理性能优化和内存管理

### **解决能力**
- **状态架构合规性**: 100%符合repowiki状态管理规范
- **状态管理优化**: 科学的状态分层和数据流设计
- **状态性能问题**: 状态更新性能和内存使用优化
- **状态持久化**: 智能的状态持久化和恢复策略
- **开发体验**: 完善的状态管理开发工具和调试

---

## 🏗️ Repowiki状态管理规范

### **状态管理核心原则**

#### **单一数据源原则**
- **全局状态**: 使用Pinia作为应用唯一的状态管理中心
- **模块化设计**: 按业务域将状态分割为独立的store模块
- **状态隔离**: 各模块状态相互独立，避免交叉依赖
- **类型安全**: 使用TypeScript确保状态类型安全

#### **单向数据流原则**
```
User Action → Component → Action → Mutation → State → Component Update
     ↑                                                              ↓
     ←─────────────── UI Update ←─────────────── State Change ←────────
```

#### **状态分层架构**
```
src/stores/
├── modules/               # 业务模块状态
│   ├── user/             # 用户状态模块
│   │   ├── index.ts      # store主文件
│   │   ├── types.ts      # 类型定义
│   │   ├── actions.ts    # 异步操作
│   │   └── getters.ts    # 计算属性
│   ├── auth/             # 认证状态模块
│   ├── app/              # 应用状态模块
│   └── business/         # 业务状态模块
├── plugins/              # Pinia插件
│   ├── persistence.ts    # 状态持久化插件
│   ├── logger.ts         # 状态变更日志插件
│   └── devtools.ts       # 开发工具插件
└── index.ts              # store配置和导出
```

### **状态模块分类**

#### **1. 核心状态模块 (Core State)**
- **用户状态**: 登录信息、权限、个人设置
- **认证状态**: 登录状态、token、session管理
- **应用状态**: 主题、语言、布局配置

#### **2. 业务状态模块 (Business State)**
- **页面状态**: 表单数据、列表状态、筛选条件
- **UI状态**: 加载状态、错误状态、弹窗状态
- **缓存状态**: API数据缓存、计算结果缓存

#### **3. 临时状态模块 (Temporary State)**
- **组件状态**: 组件内部状态，不存储在store中
- **会话状态**: 当前会话相关的临时数据
- **导航状态**: 路由参数、页面状态

---

## 🛠️ 核心工作流程 (基于Repowiki)

### **Phase 1: 状态管理合规性诊断**
```bash
# 检查Pinia架构合规性
./scripts/check-pinia-architecture.sh

# 检测状态模块设计
./scripts/analyze-state-modules.sh

# 验证状态类型安全
./scripts/validate-state-types.sh

# 检查状态性能问题
./scripts/analyze-state-performance.sh
```

### **Phase 2: 状态架构优化**
```bash
# 状态模块重构
./scripts/refactor-state-modules.sh

# 状态依赖优化
./scripts/optimize-state-dependencies.sh

# 状态持久化配置
./scripts/configure-state-persistence.sh

# 状态开发工具配置
./scripts/setup-state-devtools.sh
```

### **Phase 3: 状态质量保障**
```bash
# 状态管理测试
npm run test:state

# 状态变更测试
npm run test:state-mutations

# 状态性能测试
npm run test:state-performance

# 状态一致性测试
npm run test:state-consistency
```

---

## 🔍 状态管理合规性检查清单 (基于Repowiki)

### **✅ 强制性规范 (必须100%遵循)**

#### **Pinia使用规范**
- [ ] 严格使用Pinia进行状态管理，禁用Vuex
- [ ] 使用Composition API风格定义store (`defineStore`)
- [ ] 状态模块按业务域分层，避免单一巨型store
- [ ] 状态变更必须通过actions，禁止直接修改状态
- [ ] 使用TypeScript定义状态类型，禁止any类型

#### **状态设计规范**
- [ ] 状态命名语义化，使用camelCase命名
- [ ] 状态结构扁平化，避免深层嵌套
- [ ] 计算属性使用getter，避免在组件中重复计算
- [ ] 异步操作使用async/await，错误处理完善
- [ ] 状态变更可预测，支持时间旅行调试

#### **类型安全规范**
- [ ] 定义完整的State、Getters、Actions接口
- [ ] 使用TypeScript严格模式，禁止隐式any
- [ ] Action参数和返回值类型明确
- [ ] 计算属性返回值类型定义完整
- [ ] 持久化数据类型验证

### **⚠️ 推荐性规范**

#### **性能优化规范**
- [ ] 大型状态对象使用shallowRef优化
- [ ] 频繁变更的状态使用computed缓存
- [ ] 状态订阅使用store.$subscribe避免重复监听
- [ ] 按需加载状态模块，减少初始加载时间
- [ ] 状态变更批量处理，避免频繁更新

#### **开发体验规范**
- [ ] 配置Vue DevTools状态管理插件
- [ ] 添加状态变更日志和错误追踪
- [ ] 提供状态管理的单元测试
- [ ] 使用状态管理中间件增强功能
- [ ] 提供状态重置和调试工具

---

## 🚀 状态管理最佳实践

### **用户状态管理示例**
```typescript
// stores/modules/user/index.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserState, UserInfo, UserPermissions } from './types'
import { userApi } from '@/api/user'
import { useRouter } from 'vue-router'

export const useUserStore = defineStore('user', () => {
  // 状态定义
  const state = ref<UserState>({
    userInfo: null,
    permissions: [],
    settings: {
      theme: 'light',
      language: 'zh-CN',
      sidebarCollapsed: false
    },
    lastLoginTime: null
  })

  // 计算属性
  const isLoggedIn = computed(() => !!state.value.userInfo)
  const userName = computed(() => state.value.userInfo?.name || 'Guest')
  const userAvatar = computed(() => state.value.userInfo?.avatar || '')
  const hasPermission = computed(() =>
    (permission: string) => state.value.permissions.some(p => p.code === permission)
  )
  const isAdmin = computed(() =>
    state.value.permissions.some(p => p.role === 'admin')
  )

  // 异步操作
  const fetchUserInfo = async () => {
    try {
      const response = await userApi.getUserInfo()
      state.value.userInfo = response.data
      state.value.lastLoginTime = new Date().toISOString()
      return response.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  const fetchUserPermissions = async () => {
    try {
      const response = await userApi.getUserPermissions()
      state.value.permissions = response.data
      return response.data
    } catch (error) {
      console.error('获取用户权限失败:', error)
      throw error
    }
  }

  const login = async (credentials: LoginCredentials) => {
    try {
      const response = await userApi.login(credentials)
      const { token, user, permissions } = response.data

      // 更新状态
      state.value.userInfo = user
      state.value.permissions = permissions
      state.value.lastLoginTime = new Date().toISOString()

      // 存储token
      localStorage.setItem('auth_token', token)

      return response.data
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  const logout = async () => {
    try {
      await userApi.logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      // 清除状态
      state.value.userInfo = null
      state.value.permissions = []
      state.value.lastLoginTime = null

      // 清除token
      localStorage.removeItem('auth_token')
    }
  }

  const updateUserInfo = async (userInfo: Partial<UserInfo>) => {
    try {
      const response = await userApi.updateUserInfo(userInfo)
      state.value.userInfo = { ...state.value.userInfo!, ...response.data }
      return response.data
    } catch (error) {
      console.error('更新用户信息失败:', error)
      throw error
    }
  }

  const updateSettings = (settings: Partial<UserState['settings']>) => {
    state.value.settings = { ...state.value.settings, ...settings }
    // 持久化设置
    localStorage.setItem('user_settings', JSON.stringify(state.value.settings))
  }

  // 重置状态
  const resetState = () => {
    state.value = {
      userInfo: null,
      permissions: [],
      settings: {
        theme: 'light',
        language: 'zh-CN',
        sidebarCollapsed: false
      },
      lastLoginTime: null
    }
  }

  return {
    // 状态
    state: readonly(state),

    // 计算属性
    isLoggedIn,
    userName,
    userAvatar,
    hasPermission,
    isAdmin,

    // 操作
    fetchUserInfo,
    fetchUserPermissions,
    login,
    logout,
    updateUserInfo,
    updateSettings,
    resetState
  }
})
```

### **业务状态管理示例**
```typescript
// stores/modules/business/consume.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ConsumeState, ConsumeRecord, ConsumeFilters } from './types'
import { consumeApi } from '@/api/consume'

export const useConsumeStore = defineStore('consume', () => {
  // 状态定义
  const state = ref<ConsumeState>({
    records: [],
    filters: {
      keyword: '',
      dateRange: [],
      status: '',
      category: ''
    },
    pagination: {
      current: 1,
      pageSize: 20,
      total: 0
    },
    loading: false,
    error: null
  })

  // 计算属性
  const filteredRecords = computed(() => {
    let records = state.value.records

    // 关键词过滤
    if (state.value.filters.keyword) {
      records = records.filter(record =>
        record.description.includes(state.value.filters.keyword)
      )
    }

    // 状态过滤
    if (state.value.filters.status) {
      records = records.filter(record => record.status === state.value.filters.status)
    }

    // 分类过滤
    if (state.value.filters.category) {
      records = records.filter(record => record.category === state.value.filters.category)
    }

    // 日期范围过滤
    if (state.value.filters.dateRange.length === 2) {
      const [startDate, endDate] = state.value.filters.dateRange
      records = records.filter(record => {
        const recordDate = new Date(record.createTime)
        return recordDate >= startDate && recordDate <= endDate
      })
    }

    return records
  })

  const totalAmount = computed(() => {
    return filteredRecords.value.reduce((sum, record) => sum + record.amount, 0)
  })

  const statistics = computed(() => {
    const records = filteredRecords.value
    return {
      totalCount: records.length,
      totalAmount: totalAmount.value,
      averageAmount: records.length > 0 ? totalAmount.value / records.length : 0,
      categories: [...new Set(records.map(r => r.category))]
    }
  })

  // 异步操作
  const fetchRecords = async (params?: any) => {
    state.value.loading = true
    state.value.error = null

    try {
      const response = await consumeApi.getRecords({
        ...state.value.filters,
        ...state.value.pagination,
        ...params
      })

      state.value.records = response.data.records
      state.value.pagination.total = response.data.total

      return response.data
    } catch (error) {
      state.value.error = error as Error
      console.error('获取消费记录失败:', error)
      throw error
    } finally {
      state.value.loading = false
    }
  }

  const createRecord = async (record: Omit<ConsumeRecord, 'id' | 'createTime'>) => {
    try {
      const response = await consumeApi.createRecord(record)

      // 添加到本地状态
      state.value.records.unshift(response.data)

      // 更新分页
      state.value.pagination.total += 1

      return response.data
    } catch (error) {
      console.error('创建消费记录失败:', error)
      throw error
    }
  }

  const updateRecord = async (id: string, updates: Partial<ConsumeRecord>) => {
    try {
      const response = await consumeApi.updateRecord(id, updates)

      // 更新本地状态
      const index = state.value.records.findIndex(r => r.id === id)
      if (index !== -1) {
        state.value.records[index] = response.data
      }

      return response.data
    } catch (error) {
      console.error('更新消费记录失败:', error)
      throw error
    }
  }

  const deleteRecord = async (id: string) => {
    try {
      await consumeApi.deleteRecord(id)

      // 从本地状态移除
      state.value.records = state.value.records.filter(r => r.id !== id)

      // 更新分页
      state.value.pagination.total -= 1
    } catch (error) {
      console.error('删除消费记录失败:', error)
      throw error
    }
  }

  const updateFilters = (filters: Partial<ConsumeFilters>) => {
    state.value.filters = { ...state.value.filters, ...filters }
    state.value.pagination.current = 1 // 重置到第一页
  }

  const resetFilters = () => {
    state.value.filters = {
      keyword: '',
      dateRange: [],
      status: '',
      category: ''
    }
    state.value.pagination.current = 1
  }

  return {
    // 状态
    state: readonly(state),

    // 计算属性
    filteredRecords,
    totalAmount,
    statistics,

    // 操作
    fetchRecords,
    createRecord,
    updateRecord,
    deleteRecord,
    updateFilters,
    resetFilters
  }
})
```

### **状态持久化插件示例**
```typescript
// stores/plugins/persistence.ts
import type { PiniaPluginContext } from 'pinia'

interface PersistenceOptions {
  key?: string
  storage?: Storage
  paths?: string[]
  serializer?: {
    serialize: (state: any) => string
    deserialize: (value: string) => any
  }
}

export const createPersistencePlugin = (options: PersistenceOptions = {}) => {
  const {
    key = 'pinia',
    storage = localStorage,
    paths = [],
    serializer = {
      serialize: JSON.stringify,
      deserialize: JSON.parse
    }
  } = options

  return ({ store }: PiniaPluginContext) => {
    const storeKey = `${key}_${store.$id}`

    // 恢复状态
    const storedState = storage.getItem(storeKey)
    if (storedState) {
      try {
        const parsedState = serializer.deserialize(storedState)
        if (paths.length > 0) {
          // 只恢复指定路径的状态
          paths.forEach(path => {
            if (parsedState[path] !== undefined) {
              store.$state[path] = parsedState[path]
            }
          })
        } else {
          // 恢复全部状态
          store.$patch(parsedState)
        }
      } catch (error) {
        console.warn(`Failed to restore state for store ${store.$id}:`, error)
      }
    }

    // 持久化状态
    store.$subscribe((mutation, state) => {
      try {
        let stateToPersist = state

        if (paths.length > 0) {
          // 只持久化指定路径的状态
          stateToPersist = {}
          paths.forEach(path => {
            if (state[path] !== undefined) {
              stateToPersist[path] = state[path]
            }
          })
        }

        storage.setItem(storeKey, serializer.serialize(stateToPersist))
      } catch (error) {
        console.warn(`Failed to persist state for store ${store.$id}:`, error)
      }
    })
  }
}
```

---

## 📊 状态管理质量评估标准

### **状态架构评分**
| 维度 | 权重 | 评分标准 |
|------|------|----------|
| Pinia规范遵循度 | 30% | 完全符合Pinia最佳实践 |
| 状态模块设计 | 25% | 模块化程度和依赖关系合理 |
| 类型安全保障 | 20% | TypeScript类型定义完整准确 |
| 性能优化程度 | 15% | 状态更新性能和内存使用 |
| 开发体验 | 10% | 调试工具和开发效率 |

### **质量等级**
- **A级 (90-100分)**: 完全符合repowiki状态管理规范
- **B级 (80-89分)**: 基本合规，存在轻微优化空间
- **C级 (70-79分)**: 部分合规，需要重点改进
- **D级 (60-69分)**: 状态管理混乱，需要重构
- **E级 (0-59分)**: 严重违反状态管理规范

---

## 🎯 使用指南

### **何时调用**
- 状态管理架构设计和技术选型时
- 状态模块重构和优化时
- 状态性能问题诊断和优化时
- 状态持久化和恢复策略制定时
- 状态管理开发规范制定时

### **调用方式**
```bash
# 基于repowiki的状态管理专家
Skill("state-management-specialist-repowiki")

# 将立即执行：
# 1. 基于.qoder/repowiki状态管理规范检查
# 2. Pinia架构合规性验证
# 3. 状态模块设计和依赖分析
# 4. 状态性能优化建议和实施
```

### **预期结果**
- 100%符合`.qoder/repowiki`状态管理规范
- 科学的状态分层和数据流设计
- 高性能、可维护的状态管理系统
- 完善的状态持久化和调试工具

---

**🏆 技能等级**: 状态管理专家 (★★★★★)
**⏰ 预期效果**: 基于249个repowiki权威文档，确保IOE-DREAM状态管理100%符合Pinia最佳实践
**🎯 核心价值**: Pinia架构守护，状态管理优化，开发效率提升