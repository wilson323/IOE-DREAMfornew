# 路由系统专家 (Repowiki标准版)

## 🎯 技能定位
**核心职责**: 基于 `.qoder/repowiki` 中的《路由系统详解》规范，确保IOE-DREAM项目路由系统严格遵循Vue Router 4最佳实践，构建高效、安全的路由管理体系

**⚡ 技能等级**: ★★★★★ (路由系统专家)
**🎯 适用场景**: 路由架构设计、路由权限控制、路由性能优化、路由守卫配置、SEO优化
**📊 技能覆盖**: Vue Router 4架构 | 路由权限系统 | 懒加载优化 | 路由守卫 | 导航控制 | SEO支持

---

## 📋 技能概述 (基于Repowiki规范)

### **核心专长 (基于.qoder/repowiki/zh/content/前端架构/路由系统/)**
- **Vue Router 4守护**: 严格确保Vue Router 4最佳实践和现代化特性
- **路由分层架构**: 基于业务域和功能模块的路由分层设计
- **权限控制系统**: 细粒度的路由权限控制和动态路由
- **性能优化**: 路由懒加载、预加载、代码分割优化
- **SEO友好**: 支持SSR和SSG的路由配置优化

### **解决能力**
- **路由架构合规性**: 100%符合repowiki路由系统规范
- **路由权限管理**: 完善的权限控制和动态路由生成
- **路由性能优化**: 懒加载、预加载、缓存策略优化
- **路由安全问题**: 路由劫持防护、权限验证、参数安全
- **开发体验**: 完善的路由开发工具和调试支持

---

## 🏗️ Repowiki路由系统规范

### **路由架构核心原则**

#### **分层路由原则**
```
routes/
├── index.ts              # 路由主入口
├── core/                 # 核心路由
│   ├── auth.ts          # 认证相关路由
│   ├── layout.ts        # 布局路由
│   └── error.ts         # 错误页面路由
├── business/            # 业务路由
│   ├── consume/         # 消费模块路由
│   ├── access/          # 门禁模块路由
│   ├── attendance/      # 考勤模块路由
│   └── system/          # 系统管理路由
├── async.ts             # 动态路由加载
└── guards.ts            # 路由守卫配置
```

#### **权限控制原则**
- **基于角色的路由控制**: RBAC权限模型
- **动态路由生成**: 根据权限动态加载路由
- **路由级权限**: 细粒度的页面访问控制
- **按钮级权限**: 基于路由的操作权限控制

#### **性能优化原则**
- **路由懒加载**: 按需加载页面组件
- **预加载策略**: 智能预测用户行为，提前加载
- **代码分割**: 合理的代码分割和chunk管理
- **缓存策略**: 路由组件缓存和状态保持

### **路由分类体系**

#### **1. 核心路由 (Core Routes)**
- **登录注册**: `/login`, `/register`
- **首页仪表**: `/`, `/dashboard`
- **404错误**: `/404`, `/500`
- **无权限页**: `/403`

#### **2. 业务路由 (Business Routes)**
- **消费管理**: `/consume/*`
- **门禁系统**: `/access/*`
- **考勤管理**: `/attendance/*`
- **系统管理**: `/system/*`

#### **3. 动态路由 (Dynamic Routes)**
- **用户中心**: `/user/:id`
- **详情页面**: `/detail/:type/:id`
- **编辑页面**: `/edit/:category/:id`

---

## 🛠️ 核心工作流程 (基于Repowiki)

### **Phase 1: 路由系统合规性诊断**
```bash
# 检查Vue Router 4规范合规性
./scripts/check-router-compliance.sh

# 检测路由权限配置
./scripts/analyze-route-permissions.sh

# 验证路由性能问题
./scripts/analyze-route-performance.sh

# 检查路由安全性
./scripts/check-route-security.sh
```

### **Phase 2: 路由架构优化**
```bash
# 路由模块重构
./scripts/refactor-route-modules.sh

# 路由权限优化
./scripts/optimize-route-permissions.sh

# 路由懒加载配置
./scripts/configure-route-lazy-loading.sh

# 路由缓存策略
./scripts/configure-route-caching.sh
```

### **Phase 3: 路由质量保障**
```bash
# 路由功能测试
npm run test:routes

# 路由权限测试
npm run test:route-permissions

# 路由性能测试
npm run test:route-performance

# 路由安全测试
npm run test:route-security
```

---

## 🔍 路由系统合规性检查清单 (基于Repowiki)

### **✅ 强制性规范 (必须100%遵循)**

#### **Vue Router 4规范**
- [ ] 严格使用Vue Router 4.x版本，支持Composition API
- [ ] 路由配置使用TypeScript类型定义
- [ ] 使用createRouter创建路由实例
- [ ] 使用createWebHistory或createWebHashHistory
- [ ] 路由组件使用懒加载defineAsyncComponent

#### **路由设计规范**
- [ ] 路由路径语义化，使用kebab-case命名
- [ ] 路由参数类型明确，支持可选参数
- [ ] 嵌套路由结构清晰，层级合理
- [ ] 路由元信息(meta)配置完整
- [ ] 路由名称唯一，支持编程式导航

#### **权限控制规范**
- [ ] 实现路由级权限控制
- [ ] 使用路由守卫进行权限验证
- [ ] 动态路由根据权限生成
- [ ] 支持角色和权限双重验证
- [ ] 权限变更时路由自动更新

### **⚠️ 推荐性规范**

#### **性能优化规范**
- [ ] 页面组件实现懒加载
- [ ] 路由组件合理缓存keep-alive
- [ ] 路由切换动画性能优化
- [ ] 路由预加载策略配置
- [ ] 构建产物分析优化

#### **开发体验规范**
- [ ] 配置路由开发者工具
- [ ] 提供路由调试功能
- [ ] 路由变更日志记录
- [ ] 路由热更新支持
- [ ] 路由文档自动生成

---

## 🚀 路由系统最佳实践

### **路由主配置示例**
```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuards } from './guards'
import { coreRoutes } from './core'
import { businessRoutes } from './business'

// 路由配置
const routes: RouteRecordRaw[] = [
  // 核心路由
  ...coreRoutes,

  // 业务路由
  ...businessRoutes,

  // 404页面 (必须放在最后)
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else if (to.hash) {
      return { el: to.hash }
    } else {
      return { top: 0 }
    }
  }
})

// 设置路由守卫
setupRouterGuards(router)

export default router
```

### **核心路由配置示例**
```typescript
// router/core/index.ts
import type { RouteRecordRaw } from 'vue-router'

export const coreRoutes: RouteRecordRaw[] = [
  // 根路由重定向
  {
    path: '/',
    redirect: '/dashboard',
    meta: {
      title: '首页'
    }
  },

  // 仪表板
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: {
      title: '仪表板',
      icon: 'dashboard',
      requiresAuth: true,
      permissions: ['dashboard:view']
    }
  },

  // 登录页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
      layout: 'auth'
    }
  },

  // 错误页面
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '无权限',
      requiresAuth: false
    }
  },

  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false
    }
  },

  {
    path: '/500',
    name: 'ServerError',
    component: () => import('@/views/error/500.vue'),
    meta: {
      title: '服务器错误',
      requiresAuth: false
    }
  }
]
```

### **业务路由配置示例**
```typescript
// router/business/consume.ts
import type { RouteRecordRaw } from 'vue-router'

export const consumeRoutes: RouteRecordRaw[] = [
  {
    path: '/consume',
    name: 'Consume',
    redirect: '/consume/record',
    component: () => import('@/layouts/BusinessLayout.vue'),
    meta: {
      title: '消费管理',
      icon: 'consume',
      requiresAuth: true,
      permissions: ['consume:view']
    },
    children: [
      // 消费记录
      {
        path: 'record',
        name: 'ConsumeRecord',
        component: () => import('@/views/consume/record/index.vue'),
        meta: {
          title: '消费记录',
          icon: 'record',
          permissions: ['consume:record:view'],
          keepAlive: true
        }
      },

      // 消费统计
      {
        path: 'statistics',
        name: 'ConsumeStatistics',
        component: () => import('@/views/consume/statistics/index.vue'),
        meta: {
          title: '消费统计',
          icon: 'chart',
          permissions: ['consume:statistics:view']
        }
      },

      // 消费设置
      {
        path: 'settings',
        name: 'ConsumeSettings',
        component: () => import('@/views/consume/settings/index.vue'),
        meta: {
          title: '消费设置',
          icon: 'settings',
          permissions: ['consume:settings:edit']
        }
      },

      // 消费详情 (动态路由)
      {
        path: 'detail/:id',
        name: 'ConsumeDetail',
        component: () => import('@/views/consume/detail/index.vue'),
        meta: {
          title: '消费详情',
          hidden: true, // 在菜单中隐藏
          permissions: ['consume:record:view']
        },
        props: (route) => ({
          id: route.params.id
        })
      },

      // 消费编辑 (动态路由)
      {
        path: 'edit/:id?',
        name: 'ConsumeEdit',
        component: () => import('@/views/consume/edit/index.vue'),
        meta: {
          title: '编辑消费记录',
          hidden: true,
          permissions: ['consume:record:edit']
        },
        props: (route) => ({
          id: route.params.id || null
        })
      }
    ]
  }
]
```

### **路由守卫配置示例**
```typescript
// router/guards.ts
import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import { usePermissionStore } from '@/stores/modules/permission'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 进度条配置
NProgress.configure({
  showSpinner: false,
  minimum: 0.2,
  easing: 'ease',
  speed: 500
})

export function setupRouterGuards(router: Router) {
  // 前置守卫
  router.beforeEach(async (to, from, next) => {
    NProgress.start()

    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - IOE-DREAM` : 'IOE-DREAM'

    // 检查是否需要认证
    if (to.meta.requiresAuth !== false) {
      // 检查用户是否登录
      if (!userStore.isLoggedIn) {
        // 未登录，跳转到登录页
        next({
          name: 'Login',
          query: { redirect: to.fullPath }
        })
        return
      }

      // 检查用户信息是否完整
      if (!userStore.state.userInfo) {
        try {
          await userStore.fetchUserInfo()
          await userStore.fetchUserPermissions()
        } catch (error) {
          console.error('获取用户信息失败:', error)
          next({
            name: 'Login',
            query: { redirect: to.fullPath }
          })
          return
        }
      }

      // 检查权限
      if (to.meta.permissions) {
        const hasPermission = to.meta.permissions.every(
          permission => userStore.hasPermission(permission)
        )

        if (!hasPermission) {
          next({ name: 'Forbidden' })
          return
        }
      }

      // 检查路由是否存在 (动态路由)
      if (!to.matched.length) {
        next({ name: 'NotFound' })
        return
      }
    }

    next()
  })

  // 后置守卫
  router.afterEach((to, from) => {
    NProgress.done()

    // 记录路由访问日志
    console.log(`Route changed: ${from.path} -> ${to.path}`)
  })

  // 错误处理
  router.onError((error) => {
    console.error('Router error:', error)
    NProgress.done()
  })
}
```

### **动态路由生成示例**
```typescript
// router/async.ts
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'

export function generateAsyncRoutes(): RouteRecordRaw[] {
  const userStore = useUserStore()
  const permissions = userStore.state.permissions
  const routes: RouteRecordRaw[] = []

  // 根据权限动态生成路由
  if (hasPermission(['system:view'])) {
    routes.push({
      path: '/system',
      name: 'System',
      redirect: '/system/user',
      component: () => import('@/layouts/BusinessLayout.vue'),
      meta: {
        title: '系统管理',
        icon: 'system',
        requiresAuth: true
      },
      children: [
        {
          path: 'user',
          name: 'SystemUser',
          component: () => import('@/views/system/user/index.vue'),
          meta: {
            title: '用户管理',
            permissions: ['system:user:view']
          }
        },
        {
          path: 'role',
          name: 'SystemRole',
          component: () => import('@/views/system/role/index.vue'),
          meta: {
            title: '角色管理',
            permissions: ['system:role:view']
          }
        }
      ]
    })
  }

  if (hasPermission(['access:view'])) {
    routes.push({
      path: '/access',
      name: 'Access',
      redirect: '/access/device',
      component: () => import('@/layouts/BusinessLayout.vue'),
      meta: {
        title: '门禁系统',
        icon: 'access',
        requiresAuth: true
      },
      children: [
        {
          path: 'device',
          name: 'AccessDevice',
          component: () => import('@/views/access/device/index.vue'),
          meta: {
            title: '设备管理',
            permissions: ['access:device:view']
          }
        },
        {
          path: 'record',
          name: 'AccessRecord',
          component: () => import('@/views/access/record/index.vue'),
          meta: {
            title: '通行记录',
            permissions: ['access:record:view']
          }
        }
      ]
    })
  }

  return routes
}

// 权限检查辅助函数
function hasPermission(permissions: string[]): boolean {
  const userStore = useUserStore()
  return permissions.every(permission => userStore.hasPermission(permission))
}
```

### **路由组件缓存配置示例**
```vue
<!-- layouts/AppLayout.vue -->
<template>
  <div class="app-layout">
    <AppHeader />

    <main class="app-main">
      <router-view v-slot="{ Component, route }">
        <keep-alive :include="cachedViews">
          <component
            :is="Component"
            :key="route.name"
            v-if="route.meta.keepAlive"
          />
        </keep-alive>
        <component
          :is="Component"
          :key="route.name"
          v-if="!route.meta.keepAlive"
        />
      </router-view>
    </main>

    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()

// 需要缓存的视图组件
const cachedViews = computed(() => {
  return router.getRoutes()
    .filter(route => route.meta.keepAlive)
    .map(route => route.name as string)
})
</script>
```

---

## 📊 路由系统质量评估标准

### **路由架构评分**
| 维度 | 权重 | 评分标准 |
|------|------|----------|
| Vue Router 4规范 | 30% | 完全符合Vue Router 4最佳实践 |
| 路由设计质量 | 25% | 路由结构清晰，命名规范 |
| 权限控制系统 | 20% | 权限控制完善，动态路由支持 |
| 性能优化程度 | 15% | 懒加载、缓存策略优化 |
| 开发体验 | 10% | 调试工具、开发效率 |

### **质量等级**
- **A级 (90-100分)**: 完全符合repowiki路由系统规范
- **B级 (80-89分)**: 基本合规，存在轻微优化空间
- **C级 (70-79分)**: 部分合规，需要重点改进
- **D级 (60-69分)**: 路由设计混乱，需要重构
- **E级 (0-59分)**: 严重违反路由系统规范

---

## 🎯 使用指南

### **何时调用**
- 路由系统架构设计和技术选型时
- 路由权限控制和安全配置时
- 路由性能问题诊断和优化时
- 路由重构和模块化改造时
- SEO优化和路由缓存策略制定时

### **调用方式**
```bash
# 基于repowiki的路由系统专家
Skill("routing-system-specialist-repowiki")

# 将立即执行：
# 1. 基于.qoder/repowiki路由系统规范检查
# 2. Vue Router 4架构合规性验证
# 3. 路由权限控制和动态路由分析
# 4. 路由性能优化建议和实施
```

### **预期结果**
- 100%符合`.qoder/repowiki`路由系统规范
- 科学的路由分层和权限控制系统
- 高性能、安全的路由管理体系
- 完善的路由开发工具和调试支持

---

**🏆 技能等级**: 路由系统专家 (★★★★★)
**⏰ 预期效果**: 基于249个repowiki权威文档，确保IOE-DREAM路由系统100%符合Vue Router 4最佳实践
**🎯 核心价值**: Vue Router架构守护，路由权限优化，系统安全提升