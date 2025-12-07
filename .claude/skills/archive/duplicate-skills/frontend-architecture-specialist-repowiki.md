# 前端架构专家 (Repowiki标准版)

## 🎯 技能定位
**核心职责**: 基于 `.qoder/repowiki` 中的《前端架构详解》规范，确保IOE-DREAM项目前端严格遵循Vue3 + TypeScript + Vite5现代化架构标准

**⚡ 技能等级**: ★★★★★ (架构守护专家)
**🎯 适用场景**: 前端架构设计、组件系统优化、状态管理规范、性能优化、前端工程化
**📊 技能覆盖**: Vue3架构设计 | TypeScript类型系统 | 组件化开发 | 状态管理 | 路由系统 | API集成 | 工程化优化

---

## 📋 技能概述 (基于Repowiki规范)

### **核心专长 (基于.qoder/repowiki/zh/content/前端架构/)**
- **前端架构守护**: 严格确保Vue3 + TypeScript + Vite5架构标准
- **组件系统设计**: 基于原子设计理论的组件化架构
- **状态管理优化**: Pinia状态管理和数据流规范化
- **性能优化**: 构建优化、运行时优化、资源优化
- **工程化建设**: 自动化工具链和开发体验优化

### **解决能力**
- **架构合规性检查**: 100%符合repowiki前端架构规范
- **组件系统优化**: 可复用、可维护的组件架构设计
- **状态管理规范**: Pinia状态管理最佳实践
- **性能问题诊断**: 构建性能、运行时性能系统性优化
- **类型安全保障**: TypeScript类型系统完善

---

## 🏗️ Repowiki前端架构规范

### **核心架构栈**
- **基础框架**: Vue 3.4+ + TypeScript 5.0+
- **构建工具**: Vite 5.0+ + ESBuild
- **UI组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia 2.x
- **路由系统**: Vue Router 4.x
- **CSS方案**: Sass/SCSS + CSS Modules
- **代码规范**: ESLint + Prettier + Husky

### **目录结构规范**
```
src/
├── api/                     # API接口定义
├── assets/                  # 静态资源
├── components/              # 通用组件
│   ├── base/               # 基础组件
│   ├── business/           # 业务组件
│   └── layout/             # 布局组件
├── composables/            # 组合式函数
├── hooks/                  # 自定义Hooks
├── layouts/                # 页面布局
├── pages/                  # 页面组件
├── router/                 # 路由配置
├── stores/                 # Pinia状态管理
├── styles/                 # 样式文件
├── types/                  # TypeScript类型定义
├── utils/                  # 工具函数
└── views/                  # 视图组件
```

---

## 🛠️ 核心工作流程 (基于Repowiki)

### **Phase 1: 前端架构合规性诊断**
```bash
# 检查Vue3+TypeScript架构合规性
./scripts/check-frontend-architecture.sh

# 检测组件设计规范
grep -r "defineComponent" --include="*.vue" . | wc -l
grep -r "export.*defineComponent" --include="*.ts" . | wc -l

# 验证状态管理规范
./scripts/validate-pinia-structure.sh

# 检查类型定义完整性
./scripts/check-typescript-coverage.sh
```

### **Phase 2: 组件系统优化**
```bash
# 检查原子设计规范
./scripts/validate-atomic-design.sh

# 组件性能优化
./scripts/optimize-components-performance.sh

# 组件文档生成
./scripts/generate-component-docs.sh
```

### **Phase 3: 性能优化实施**
```bash
# 构建性能分析
npm run build -- --analyze

# 运行时性能检测
./scripts/runtime-performance-check.sh

# 资源优化
./scripts/asset-optimization.sh
```

---

## 🔍 前端架构合规性检查清单 (基于Repowiki)

### **✅ 强制性规范 (必须100%遵循)**

#### **Vue3+TypeScript规范**
- [ ] 所有组件使用Vue 3 Composition API (`<script setup>`)
- [ ] 严格的TypeScript类型定义 (no any)
- [ ] 使用Pinia进行状态管理 (禁用Vuex)
- [ ] 使用Vue Router 4进行路由管理
- [ ] ESLint + Prettier代码规范检查

#### **组件设计规范**
- [ ] 遵循原子设计原则 (Atoms → Molecules → Organisms)
- [ ] 单一职责原则，组件功能明确
- [ ] Props和Emits完整类型定义
- [ ] 组件命名规范 (PascalCase)
- [ ] 文件命名规范 (kebab-case.vue)

#### **状态管理规范**
- [ ] 使用Pinia Store进行状态管理
- [ ] Store模块化，按业务域划分
- [ ] 状态变更通过actions，禁止直接mutation
- [ ] 类型安全的状态定义 (interface/type)

### **⚠️ 推荐性规范**

#### **性能优化规范**
- [ ] 组件懒加载 (defineAsyncComponent)
- [ ] 路由懒加载
- [ ] 图片懒加载和优化
- [ ] 第三方库按需引入
- [ ] 构建产物分析和优化

#### **工程化规范**
- [ ] 统一的代码格式化配置
- [ ] 提交前代码检查 (pre-commit hooks)
- [ ] 组件单元测试覆盖
- [ ] API接口类型定义完整
- [ ] 错误边界处理完善

---

## 🚀 前端性能优化策略

### **构建性能优化**
```bash
# Vite配置优化
// vite.config.ts
export default defineConfig({
  plugins: [
    vue(),
    // 开启gzip压缩
    compression(),
    // 代码分割
    splitVendorChunkPlugin()
  ],
  build: {
    // 构建优化
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          antd: ['ant-design-vue']
        }
      }
    }
  }
})
```

### **运行时性能优化**
```typescript
// 组件懒加载
const HeavyComponent = defineAsyncComponent(() => import('./HeavyComponent.vue'))

// 图片懒加载指令
const vLazyload = {
  mounted(el: HTMLImageElement, binding: DirectiveBinding) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          el.src = binding.value
          observer.unobserve(el)
        }
      })
    })
    observer.observe(el)
  }
}
```

### **状态管理性能优化**
```typescript
// store/user.ts
export const useUserStore = defineStore('user', () => {
  // 响应式状态
  const state = reactive({
    userInfo: null as UserInfo | null,
    permissions: [] as string[]
  })

  // 计算属性
  const hasPermission = computed(() => (permission: string) => {
    return state.permissions.includes(permission)
  })

  // actions
  const fetchUserInfo = async () => {
    try {
      const response = await userApi.getUserInfo()
      state.userInfo = response.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  return {
    ...toRefs(state),
    hasPermission,
    fetchUserInfo
  }
})
```

---

## 📊 前端架构质量评估标准

### **架构合规性评分**
| 维度 | 权重 | 评分标准 |
|------|------|----------|
| 技术栈合规性 | 25% | Vue3+TS+Vite5技术栈完整度 |
| 组件设计质量 | 20% | 组件化程度和复用性 |
| 状态管理规范 | 20% | Pinia使用规范性 |
| 性能优化程度 | 15% | 构建和运行时性能 |
| 工程化完善度 | 10% | 开发工具链完善度 |
| 代码质量 | 10% | TypeScript类型覆盖度 |

### **质量等级**
- **A级 (90-100分)**: 完全符合repowiki前端架构规范
- **B级 (80-89分)**: 基本合规，存在轻微优化空间
- **C级 (70-79分)**: 部分合规，需要重点改进
- **D级 (60-69分)**: 架构设计混乱，需要重构
- **E级 (0-59分)**: 严重违反前端架构规范

---

## 🔧 前端开发最佳实践

### **组件设计最佳实践**
```vue
<!-- 组件模板示例 -->
<template>
  <div class="component-wrapper">
    <slot name="header" />
    <main class="component-content">
      <!-- 组件内容 -->
    </main>
    <slot name="footer" />
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string
  disabled?: boolean
  theme?: 'light' | 'dark'
}

interface Emits {
  click: [event: MouseEvent]
  change: [value: string]
}

// Props定义
const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  theme: 'light'
})

// Emits定义
const emit = defineEmits<Emits>()

// 组合式函数
const { isLoading, handleClick } = useComponentLogic(props)

// 监听器
watch(() => props.title, (newTitle) => {
  console.log('标题变更:', newTitle)
})
</script>

<style scoped lang="scss">
.component-wrapper {
  // 组件样式
}
</style>
```

### **状态管理最佳实践**
```typescript
// store/modules/user.ts
export interface UserState {
  userInfo: UserInfo | null
  permissions: Permission[]
  settings: UserSettings
}

export const useUserStore = defineStore('user', () => {
  // 状态定义
  const state = reactive<UserState>({
    userInfo: null,
    permissions: [],
    settings: defaultSettings
  })

  // 计算属性
  const isLoggedIn = computed(() => !!state.userInfo)
  const isAdmin = computed(() =>
    state.permissions.some(p => p.code === 'admin')
  )

  // Actions
  const login = async (credentials: LoginCredentials) => {
    try {
      const response = await authApi.login(credentials)
      state.userInfo = response.user
      state.permissions = response.permissions
      return response
    } catch (error) {
      throw new Error('登录失败')
    }
  }

  const logout = async () => {
    await authApi.logout()
    state.userInfo = null
    state.permissions = []
  }

  return {
    ...toRefs(state),
    isLoggedIn,
    isAdmin,
    login,
    logout
  }
})
```

---

## 🎯 使用指南

### **何时调用**
- 前端架构设计和技术选型时
- 组件系统设计和优化时
- 状态管理架构规划时
- 性能优化和工程化建设时
- 代码质量审查和重构时

### **调用方式**
```bash
# 基于repowiki的前端架构专家
Skill("frontend-architecture-specialist-repowiki")

# 将立即执行：
# 1. 基于.qoder/repowiki前端架构规范检查
# 2. 组件系统设计验证和优化
# 3. 状态管理架构合规检查
# 4. 性能优化建议和实施方案
```

### **预期结果**
- 100%符合`.qoder/repowiki`前端架构规范
- 现代化的Vue3+TypeScript组件系统
- 高性能的前端应用架构
- 完善的工程化工具链

---

**🏆 技能等级**: 前端架构守护专家 (★★★★★)
**⏰ 预期效果**: 基于249个repowiki权威文档，确保IOE-DREAM前端架构100%符合现代化标准
**🎯 核心价值**: 现代化前端架构守护，组件系统优化，性能质量保障