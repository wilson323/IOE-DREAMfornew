# 前端性能优化实施指南

> **任务编号**: P1-7.6
> **任务名称**: 前端性能优化（Bundle优化、首屏<2s）
> **预计工时**: 5人天
> **优先级**: P1（高优先级）
> **创建日期**: 2025-12-26

---

## 📋 任务概述

### 问题描述

当前前端应用存在以下性能瓶颈：

- **首屏加载慢**: 首屏平均加载时间3.5秒，超过用户容忍度（2秒）
- **Bundle体积大**: 打包后体积5.2MB，加载时间长
- **资源未压缩**: 静态资源未启用Gzip压缩
- **未优化缓存策略**: 浏览器缓存命中率低，重复加载资源
- **未代码分割**: 所有代码打包成单个bundle，无法按需加载
- **图片未优化**: 大图未压缩，未使用WebP格式
- **第三方库未优化**: 未使用CDN，未按需引入

### 优化目标

- ✅ **首屏加载时间**: 从3.5秒→<2秒（**43%↑**）
- ✅ **Bundle体积**: 从5.2MB→<2MB（**62%↓**）
- ✅ **启用Gzip压缩**: 静态资源压缩率>70%
- ✅ **优化缓存策略**: 缓存命中率>80%
- ✅ **代码分割**: 按路由懒加载，首屏加载减少50%
- ✅ **图片优化**: WebP格式，压缩率>50%
- ✅ **CDN加速**: 第三方库使用CDN，加载速度提升200%

### 预期效果

| 指标 | 优化前 | 优化后 | 提升幅度 |
|------|--------|--------|----------|
| **首屏加载时间** | 3.5s | <2s | **43%↑** |
| **Bundle总体积** | 5.2MB | <2MB | **62%↓** |
| **Gzip压缩后体积** | 5.2MB | 1.5MB | **71%↓** |
| **缓存命中率** | 30% | >80% | **167%↑** |
| **首屏加载体积** | 3.8MB | <1MB | **74%↓** |
| **Lighthouse性能评分** | 65分 | >90分 | **38%↑** |

---

## 🎯 优化策略

### 1. Bundle优化策略

#### 1.1 代码分割（Code Splitting）

**原理**: 将代码分割成多个小的bundle，按需加载

**优化前**:
```javascript
// 所有代码打包成一个bundle（5.2MB）
import Home from './views/Home';
import User from './views/User';
import Access from './views/Access';
// ... 100+ 页面组件
```

**优化后**:
```javascript
// 路由懒加载，每个页面单独打包
const Home = () => import(/* webpackChunkName: "home" */ './views/Home');
const User = () => import(/* webpackChunkName: "user" */ './views/User');
const Access = () => import(/* webpackChunkName: "access" */ './views/Access');

// 首屏只加载Home组件（约200KB）
```

**预期效果**: 首屏加载体积从3.8MB→500KB（**87%↓**）

#### 1.2 第三方库分离（Vendor Splitting）

**原理**: 将第三方库打包成单独的vendor bundle，利用长期缓存

**优化前**:
```javascript
// 第三方库和业务代码混在一起
// 每次业务代码变更，vendor缓存失效
```

**优化后**:
```javascript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          // 第三方库单独打包
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'ui-vendor': ['ant-design-vue', '@ant-design/icons-vue'],
          'utils-vendor': ['axios', 'dayjs', 'lodash-es'],
        }
      }
    }
  }
});
```

**预期效果**: vendor bundle从3.2MB→1.8MB，缓存利用率提升90%

#### 1.3 Tree Shaking

**原理**: 移除未使用的代码，减少bundle体积

**优化前**:
```javascript
// 导入整个lodash库（约70KB）
import _ from 'lodash';
_.map(array, fn);
```

**优化后**:
```javascript
// 只导入需要的函数（约2KB）
import { map } from 'lodash-es';
map(array, fn);

// 或者使用ES6原生方法
array.map(fn);
```

**预期效果**: 第三方库体积减少40%

### 2. 资源优化策略

#### 2.1 Gzip压缩

**原理**: 使用Gzip算法压缩文本资源（JS、CSS、HTML）

**配置**: `vite.config.ts`

```typescript
import viteCompression from 'vite-plugin-compression';

export default defineConfig({
  plugins: [
    // Gzip压缩
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240, // 只压缩>10KB的文件
      algorithm: 'gzip',
      ext: '.gz',
      deleteOriginFile: false, // 删除原文件
    }),
    // Brotli压缩（比Gzip更高效）
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,
      algorithm: 'brotliCompress',
      ext: '.br',
      deleteOriginFile: false,
    }),
  ],
});
```

**预期效果**: JS/CSS文件压缩率70-80%

#### 2.2 图片优化

**1. 图片压缩**: 使用vite-plugin-imagemin

```typescript
import viteImagemin from 'vite-plugin-imagemin';

export default defineConfig({
  plugins: [
    viteImagemin({
      gifsicle: { optimizationLevel: 7 },
      optipng: { optimizationLevel: 7 },
      mozjpeg: { quality: 80 },
      pngquant: { quality: [0.8, 0.9] },
      svgo: {
        plugins: [
          { name: 'removeViewBox', active: false },
          { name: 'removeEmptyAttrs', active: false },
        ],
      },
    }),
  ],
});
```

**2. 转换为WebP格式**: 更高压缩比

```typescript
// vite.config.ts
import imagewebp from 'vite-plugin-webp';

export default defineConfig({
  plugins: [
    imagewebp({
      gif: true,  // 转换GIF
      svg: true,  // 转换SVG
      webp: {
        quality: 80,  // WebP质量
        preset: 'default',
      },
    }),
  ],
});
```

**3. 响应式图片**: 使用<picture>元素

```html
<picture>
  <source srcset="@/assets/hero-image.webp" type="image/webp">
  <source srcset="@/assets/hero-image.jpg" type="image/jpeg">
  <img src="@/assets/hero-image.jpg" alt="Hero Image" loading="lazy">
</picture>
```

**预期效果**: 图片体积减少50-70%

#### 2.3 字体优化

**1. 使用font-display: swap**: 避免FOIT（Flash of Invisible Text）

```css
@font-face {
  font-family: 'Custom Font';
  src: url('./custom-font.woff2') format('woff2');
  font-display: swap;  /* 立即显示备用字体，加载完成后切换 */
  font-weight: 400;
}
```

**2. 字体子集化**: 只包含需要的字符

```bash
# 使用pyftsubset提取中文字符集
pyftsubset font.ttf \
  --unicodes=U+0020-007E,U+4E00-9FA5 \
  --output-file=font-subset.ttf
```

**预期效果**: 字体文件减少60%

### 3. 加载优化策略

#### 3.1 路由懒加载

**配置**: `src/router/index.ts`

```typescript
import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    name: 'Home',
    // 懒加载：访问时才加载
    component: () => import('@/views/Home/index.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/User/index.vue'),
    meta: { title: '用户管理' }
  },
  {
    path: '/access',
    name: 'Access',
    component: () => import('@/views/Access/index.vue'),
    meta: { title: '门禁管理' }
  },
  // ... 其他路由
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 预加载关键路由（提升性能）
router.beforeResolve(async (to) => {
  if (to.meta?.preload) {
    // 预加载组件
    await import(/* @vite-ignore */ `@/views/${to.name}/index.vue`);
  }
});

export default router;
```

**预加载关键资源**: `index.html`

```html
<!-- 预加载关键资源 -->
<link rel="modulepreload" href="/src/views/Home/index.vue">
<link rel="preload" href="/src/assets/css/main.css" as="style">
<link rel="prefetch" href="/src/views/User/index.vue">  /* 预取下一个可能访问的页面 */
```

#### 3.2 组件懒加载

**使用defineAsyncComponent**:

```vue
<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

// 同步加载（首屏关键组件）
import Header from '@/components/Header.vue';

// 异步加载（非首屏组件）
const Modal = defineAsyncComponent(() => import('@/components/Modal.vue'));
const Chart = defineAsyncComponent(() => import('@/components/Chart.vue'));

// 带加载状态的异步组件
const AsyncComponent = defineAsyncComponent({
  loader: () => import('@/components/HeavyComponent.vue'),
  loadingComponent: LoadingSpinner,  // 加载中显示
  errorComponent: ErrorComponent,     // 错误时显示
  delay: 200,                         // 延迟显示加载组件
  timeout: 3000,                      // 超时时间
});
</script>
```

#### 3.3 图片懒加载

**使用loading="lazy"属性**:

```html
<!-- 图片进入视口时才加载 -->
<img
  src="@/assets/image.jpg"
  alt="Description"
  loading="lazy"
  decoding="async"
>
```

**Intersection Observer API**:

```typescript
// 自定义图片懒加载指令
const lazyLoadDirective = {
  mounted(el: HTMLImageElement) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target as HTMLImageElement;
          img.src = img.dataset.src || '';
          observer.unobserve(img);
        }
      });
    });

    observer.observe(el);
  },
};

// 注册指令
app.directive('lazy', lazyLoadDirective);
```

**使用**:

```html
<img v-lazy src="@/assets/image.jpg" data-src="@/assets/image.jpg" alt="Description">
```

### 4. 缓存优化策略

#### 4.1 HTTP缓存策略

**Nginx配置**: `/etc/nginx/conf.d/static.conf`

```nginx
# 静态资源缓存配置
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
  expires 1y;  # 1年缓存
  add_header Cache-Control "public, immutable";
  add_header X-Content-Type-Options nosniff;
}

# HTML文件不缓存（确保获取最新版本）
location ~* \.html$ {
  expires -1;
  add_header Cache-Control "no-cache, no-store, must-revalidate";
}

# Service Worker文件不缓存
location /sw.js {
  expires -1;
  add_header Cache-Control "no-cache, no-store, must-revalidate";
}
```

#### 4.2 Vite缓存配置

**配置**: `vite.config.ts`

```typescript
export default defineConfig({
  build: {
    // 文件名包含hash，利用缓存
    rollupOptions: {
      output: {
        // 文件名格式: [name]-[hash].js
        entryFileNames: 'assets/[name]-[hash].js',
        chunkFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]',
      },
    },
    // chunk大小警告阈值（KB）
    chunkSizeWarningLimit: 1000,
  },
});
```

**效果**:
- `main-abc123.js` → 内容变化后hash变为`def456`
- 浏览器自动下载新文件，旧文件从缓存加载

#### 4.3 Service Worker缓存

**注册Service Worker**: `src/main.ts`

```typescript
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then(registration => {
        console.log('SW registered: ', registration);
      })
      .catch(registrationError => {
        console.log('SW registration failed: ', registrationError);
      });
  });
}
```

**Service Worker实现**: `public/sw.js`

```javascript
const CACHE_NAME = 'ioe-dream-v1.0.0';
const urlsToCache = [
  '/',
  '/assets/main-abc123.js',
  '/assets/main-def456.css',
  // 其他关键资源
];

// 安装事件：缓存关键资源
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

// 激活事件：清理旧缓存
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== CACHE_NAME) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

// 拦截请求：缓存优先策略
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // 缓存命中，返回缓存
        if (response) {
          return response;
        }

        // 缓存未命中，请求网络
        return fetch(event.request).then(response => {
          // 检查是否是有效响应
          if (!response || response.status !== 200 || response.type !== 'basic') {
            return response;
          }

          // 克隆响应（响应只能使用一次）
          const responseToCache = response.clone();

          // 缓存新资源
          caches.open(CACHE_NAME).then(cache => {
            cache.put(event.request, responseToCache);
          });

          return response;
        });
      })
  );
});
```

**预期效果**: 二次访问速度提升90%+

### 5. CDN优化策略

#### 5.1 第三方库使用CDN

**index.html**: 引入CDN资源

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <link rel="icon" href="/favicon.ico">
  <!-- 使用CDN加载第三方库 -->
  <script src="https://cdn.jsdelivr.net/npm/vue@3.4.21/dist/vue.global.prod.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/vue-router@4.3.0/dist/vue-router.global.prod.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/pinia@2.1.7/dist/pinia.iife.prod.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/axios@1.6.5/dist/axios.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/dayjs@1.11.10/dayjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js"></script>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.ts"></script>
</body>
</html>
```

**Vite配置**: 排除CDN库

```typescript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      external: ['vue', 'vue-router', 'pinia', 'axios', 'dayjs', 'echarts'],
      output: {
        globals: {
          vue: 'Vue',
          'vue-router': 'VueRouter',
          pinia: 'Pinia',
          axios: 'axios',
          dayjs: 'dayjs',
          echarts: 'echarts',
        },
      },
    },
  },
});
```

**预期效果**: 第三方库加载时间从800ms→200ms（**75%↓**）

#### 5.2 静态资源CDN

**Vite配置**: 指定CDN基础路径

```typescript
// vite.config.ts
export default defineConfig({
  base: process.env.NODE_ENV === 'production'
    ? 'https://cdn.example.com/ioe-dream/'
    : '/',
  build: {
    rollupOptions: {
      output: {
        // 资源文件名
        assetFileNames: 'assets/[name]-[hash].[ext]',
      },
    },
  },
});
```

**预期效果**: 静态资源下载速度提升200%+

---

## 📝 实施步骤

### 步骤1: 环境准备

**1. 安装依赖**:

```bash
cd smart-admin-web-javascript

# 安装Vite插件
npm install --save-dev \
  vite-plugin-compression \
  vite-plugin-imagemin \
  @vitejs/plugin-vue \
  rollup-plugin-visualizer

# 安装性能分析工具
npm install --save-dev \
  webpack-bundle-analyzer \
  rollup-plugin-visualizer
```

**2. 配置package.json**:

```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "build:analyze": "vite build --mode analyze",
    "preview": "vite preview",
    "lighthouse": "lighthouse http://localhost:3000 --output=html --output-path=./lighthouse-report"
  }
}
```

### 步骤2: Vite配置优化

**修改文件**: `smart-admin-web-javascript/vite.config.ts`

```typescript
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import viteCompression from 'vite-plugin-compression';
import viteImagemin from 'vite-plugin-imagemin';
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig({
  plugins: [
    vue(),

    // Gzip压缩
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,
      algorithm: 'gzip',
      ext: '.gz',
    }),

    // Brotli压缩
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,
      algorithm: 'brotliCompress',
      ext: '.br',
    }),

    // 图片优化
    viteImagemin({
      gifsicle: { optimizationLevel: 7 },
      optipng: { optimizationLevel: 7 },
      mozjpeg: { quality: 80 },
      pngquant: { quality: [0.8, 0.9] },
      svgo: {
        plugins: [
          { name: 'removeViewBox', active: false },
        ],
      },
    }),

    // Bundle分析
    visualizer({
      open: true,
      filename: './dist/stats.html',
      gzipSize: true,
      brotliSize: true,
    }),
  ],

  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },

  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,  // 生产环境不生成sourcemap
    minify: 'terser',  // 使用terser压缩

    // 代码分割
    rollupOptions: {
      output: {
        // 文件名包含hash
        entryFileNames: 'assets/[name]-[hash].js',
        chunkFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]',

        // 手动分包
        manualChunks: {
          // Vue核心库
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // UI组件库
          'ui-vendor': ['ant-design-vue', '@ant-design/icons-vue'],
          // 工具库
          'utils-vendor': ['axios', 'dayjs', 'lodash-es'],
          // 图表库
          'chart-vendor': ['echarts'],
        },
      },
    },

    // chunk大小警告阈值
    chunkSizeWarningLimit: 1000,

    // Terser压缩配置
    terserOptions: {
      compress: {
        drop_console: true,  // 删除console
        drop_debugger: true, // 删除debugger
      },
    },
  },

  server: {
    port: 3000,
    open: true,
    cors: true,
  },
});
```

### 步骤3: 路由懒加载配置

**修改文件**: `smart-admin-web-javascript/src/router/index.ts`

```typescript
import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

// 路由懒加载
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home/index.vue'),
    meta: { title: '首页', preload: true }  // 预加载
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/User/index.vue'),
    meta: { title: '用户管理' }
  },
  {
    path: '/access',
    name: 'Access',
    component: () => import('@/views/Access/index.vue'),
    meta: { title: '门禁管理' }
  },
  {
    path: '/attendance',
    name: 'Attendance',
    component: () => import('@/views/Attendance/index.vue'),
    meta: { title: '考勤管理' }
  },
  {
    path: '/consume',
    name: 'Consume',
    component: () => import('@/views/Consume/index.vue'),
    meta: { title: '消费管理' }
  },
  {
    path: '/visitor',
    name: 'Visitor',
    component: () => import('@/views/Visitor/index.vue'),
    meta: { title: '访客管理' }
  },
  {
    path: '/video',
    name: 'Video',
    component: () => import('@/views/Video/index.vue'),
    meta: { title: '视频监控' }
  },
  // ... 其他路由
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 预加载关键路由
router.beforeResolve(async (to) => {
  if (to.meta?.preload) {
    try {
      await import(/* @vite-ignore */ `@/views/${to.name}/index.vue`);
    } catch (error) {
      console.warn('预加载失败:', error);
    }
  }
});

export default router;
```

### 步骤4: 组件懒加载优化

**修改文件**: `smart-admin-web-javascript/src/App.vue`

```vue
<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

// 首屏关键组件：同步加载
import AppHeader from '@/components/AppHeader.vue';
import AppSidebar from '@/components/AppSidebar.vue';

// 非首屏组件：异步加载
const AppFooter = defineAsyncComponent(() => import('@/components/AppFooter.vue'));
const BackToTop = defineAsyncComponent(() => import('@/components/BackToTop.vue'));

// 带加载状态的异步组件
const HeavyChart = defineAsyncComponent({
  loader: () => import('@/components/HeavyChart.vue'),
  loadingComponent: () => import('@/components/LoadingSpinner.vue'),
  errorComponent: () => import('@/components/ErrorComponent.vue'),
  delay: 200,
  timeout: 3000,
});
</script>

<template>
  <div id="app">
    <AppHeader />
    <AppSidebar />
    <main>
      <router-view />
    </main>
    <AppFooter />
    <BackToTop />
    <HeavyChart v-if="showChart" />
  </div>
</template>
```

### 步骤5: 图片懒加载指令

**新建文件**: `smart-admin-web-javascript/src/directives/lazyLoad.ts`

```typescript
import type { Directive } from 'vue';

/**
 * 图片懒加载指令
 *
 * 使用方法:
 * <img v-lazy="src" data-src="actual-src" alt="...">
 */
const lazyLoadDirective: Directive = {
  mounted(el: HTMLImageElement, binding) {
    // 设置占位图
    el.src = binding.value || 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target as HTMLImageElement;
          const actualSrc = img.dataset.src;

          if (actualSrc) {
            // 加载实际图片
            const tempImg = new Image();
            tempImg.onload = () => {
              img.src = actualSrc;
              img.classList.add('loaded');  // 添加加载完成类
            };
            tempImg.onerror = () => {
              img.src = binding.value || '/placeholder-error.jpg';  // 加载失败占位图
            };
            tempImg.src = actualSrc;
          }

          // 停止观察
          observer.unobserve(img);
        }
      });
    }, {
      rootMargin: '50px',  // 提前50px开始加载
    });

    observer.observe(el);
  },
};

export default lazyLoadDirective;
```

**注册指令**: `smart-admin-web-javascript/src/main.ts`

```typescript
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { createPinia } from 'pinia';
import lazyLoadDirective from './directives/lazyLoad';

const app = createApp(App);

// 注册懒加载指令
app.directive('lazy', lazyLoadDirective);

app.use(router);
app.use(createPinia());
app.mount('#app');
```

**使用示例**:

```vue
<template>
  <div>
    <!-- 图片懒加载 -->
    <img
      v-lazy="'/placeholder-loading.jpg'"
      data-src="/actual-image.jpg"
      alt="Description"
      class="lazy-image"
    >
  </div>
</template>

<style>
.lazy-image {
  opacity: 0;
  transition: opacity 0.3s;
}

.lazy-image.loaded {
  opacity: 1;
}
</style>
```

### 步骤6: Service Worker缓存

**新建文件**: `smart-admin-web-javascript/public/sw.js`

```javascript
const CACHE_NAME = 'ioe-dream-v1.0.0';
const RUNTIME_CACHE = 'ioe-dream-runtime';

// 需要预缓存的静态资源
const PRECACHE_URLS = [
  '/',
  '/index.html',
  '/favicon.ico',
  // 其他关键资源（构建后会自动添加）
];

// 安装事件：预缓存关键资源
self.addEventListener('install', (event) => {
  console.log('[SW] Install event');

  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('[SW] Precaching app shell');
      return cache.addAll(PRECACHE_URLS);
    })
  );

  // 立即激活
  self.skipWaiting();
});

// 激活事件：清理旧缓存
self.addEventListener('activate', (event) => {
  console.log('[SW] Activate event');

  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME && cacheName !== RUNTIME_CACHE) {
            console.log('[SW] Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );

  // 立即控制所有页面
  return self.clients.claim();
});

// 拦截请求：缓存策略
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // 只缓存同源资源
  if (url.origin !== location.origin) {
    return;
  }

  // HTML文件：网络优先策略（确保获取最新）
  if (request.destination === 'document') {
    event.respondWith(
      fetch(request)
        .then((response) => {
          // 克隆响应并缓存
          const responseClone = response.clone();
          caches.open(RUNTIME_CACHE).then((cache) => {
            cache.put(request, responseClone);
          });
          return response;
        })
        .catch(() => {
          // 网络失败，回退到缓存
          return caches.match(request);
        })
    );
    return;
  }

  // 静态资源（JS/CSS/图片）：缓存优先策略
  if (
    request.destination === 'script' ||
    request.destination === 'style' ||
    request.destination === 'image'
  ) {
    event.respondWith(
      caches.match(request).then((cached) => {
        // 缓存命中
        if (cached) {
          // 后台更新（Stale-While-Revalidate）
          fetch(request).then((response) => {
            caches.open(RUNTIME_CACHE).then((cache) => {
              cache.put(request, response);
            });
          });
          return cached;
        }

        // 缓存未命中，请求网络
        return fetch(request).then((response) => {
          // 检查响应有效性
          if (!response || response.status !== 200 || response.type !== 'basic') {
            return response;
          }

          // 缓存响应
          const responseClone = response.clone();
          caches.open(RUNTIME_CACHE).then((cache) => {
            cache.put(request, responseClone);
          });

          return response;
        });
      })
    );
    return;
  }

  // 其他请求：直接请求网络
  event.respondWith(fetch(request));
});

// 消息事件：处理来自客户端的消息
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
```

**注册Service Worker**: `smart-admin-web-javascript/src/main.ts`

```typescript
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then((registration) => {
        console.log('SW registered: ', registration);

        // 监听更新
        registration.addEventListener('updatefound', () => {
          const newWorker = registration.installing;
          if (newWorker) {
            newWorker.addEventListener('statechange', () => {
              if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                // 有新版本可用
                if (confirm('发现新版本，是否立即更新？')) {
                  newWorker.postMessage({ type: 'SKIP_WAITING' });
                  window.location.reload();
                }
              }
            });
          }
        });
      })
      .catch((registrationError) => {
        console.log('SW registration failed: ', registrationError);
      });
  });
}
```

### 步骤7: 构建和验证

**1. 开发环境验证**:

```bash
cd smart-admin-web-javascript

# 启动开发服务器
npm run dev

# 访问 http://localhost:3000
# 验证功能正常
```

**2. 生产构建**:

```bash
# 构建生产版本
npm run build

# 查看构建产物
ls -lh dist/

# 预期输出:
# - assets/main-abc123.js (约200KB)
# - assets/main-def456.css (约50KB)
# - assets/vue-vendor-ghi789.js (约150KB)
# - assets/ui-vendor-jkl012.js (约500KB)
# ...
```

**3. Bundle分析**:

```bash
# 生成Bundle分析报告
npm run build:analyze

# 自动打开dist/stats.html
# 查看各模块大小
```

**4. 性能测试**:

```bash
# 启动预览服务器
npm run preview

# 运行Lighthouse测试
npm run lighthouse

# 查看报告
open lighthouse-report.html
```

**预期Lighthouse评分**:

| 指标 | 优化前 | 优化后 | 目标 |
|------|--------|--------|------|
| **Performance** | 65 | >90 | >90 |
| **Accessibility** | 85 | >90 | >90 |
| **Best Practices** | 90 | >95 | >95 |
| **SEO** | 80 | >90 | >90 |

---

## 📊 监控与测量

### 1. Web Vitals监控

**关键指标**:

| 指标 | 全称 | 说明 | 良好值 |
|------|------|------|--------|
| **LCP** | Largest Contentful Paint | 最大内容绘制 | <2.5s |
| **FID** | First Input Delay | 首次输入延迟 | <100ms |
| **CLS** | Cumulative Layout Shift | 累积布局偏移 | <0.1 |
| **FCP** | First Contentful Paint | 首次内容绘制 | <1.8s |
| **TTI** | Time to Interactive | 可交互时间 | <3.8s |

**监控代码**: `src/utils/webVitals.ts`

```typescript
import { getCLS, getFID, getFCP, getLCP, getTTI } from 'web-vitals';

// 发送性能数据到监控平台
function sendToAnalytics(metric: any) {
  const { name, value, id } = metric;

  // 发送到后端监控
  fetch('/api/v1/analytics/performance', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      metric: name,
      value: Math.round(value),
      id: id,
      url: window.location.href,
      userAgent: navigator.userAgent,
      timestamp: Date.now(),
    }),
  }).catch(console.error);
}

// 监控Web Vitals
getCLS(sendToAnalytics);
getFID(sendToAnalytics);
getFCP(sendToAnalytics);
getLCP(sendToAnalytics);
getTTI(sendToAnalytics);
```

### 2. 性能监控面板

**后端API**: `PerformanceMonitorController.java`

```java
@RestController
@RequestMapping("/api/v1/analytics")
public class PerformanceMonitorController {

    @PostMapping("/performance")
    public ResponseDTO<Void> recordPerformance(@RequestBody PerformanceMetricDTO metric) {
        // 存储性能数据
        performanceMonitorService.recordMetric(metric);
        return ResponseDTO.ok();
    }

    @GetMapping("/performance/report")
    public ResponseDTO<PerformanceReportVO> getPerformanceReport(
        @RequestParam String startDate,
        @RequestParam String endDate
    ) {
        PerformanceReportVO report = performanceMonitorService.generateReport(startDate, endDate);
        return ResponseDTO.ok(report);
    }
}
```

### 3. 实时性能告警

**告警规则**:

```yaml
# prometheus-alerts.yml
groups:
  - name: frontend_performance
    interval: 1m
    rules:
      # LCP告警
      - alert: HighLCP
        expr: web_vitals_lcp_p95 > 2500
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "LCP过高"
          description: "页面 {{ $labels.page }} LCP超过2.5秒"

      # FID告警
      - alert: HighFID
        expr: web_vitals_fid_p95 > 100
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "FID过高"
          description: "页面 {{ $labels.page }} FID超过100ms"

      # CLS告警
      - alert: HighCLS
        expr: web_vitals_cls_p95 > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "CLS过高"
          description: "页面 {{ $labels.page }} CLS超过0.1"
```

---

## 📋 实施检查清单

### 阶段1: 代码优化

- [ ] **Vite配置优化**
  - [ ] 配置代码分割（manualChunks）
  - [ ] 启用Gzip和Brotli压缩
  - [ ] 配置图片优化（vite-plugin-imagemin）
  - [ ] 配置Bundle分析（rollup-plugin-visualizer）

- [ ] **路由懒加载**
  - [ ] 修改路由配置使用懒加载
  - [ ] 配置关键路由预加载
  - [ ] 验证路由切换正常

- [ ] **组件懒加载**
  - [ ] 非首屏组件使用defineAsyncComponent
  - [ ] 配置加载状态和错误处理
  - [ ] 验证组件加载正常

- [ ] **图片优化**
  - [ ] 实现图片懒加载指令
  - [ ] 转换为WebP格式
  - [ ] 配置响应式图片（<picture>）
  - [ ] 验证图片加载正常

### 阶段2: 缓存策略

- [ ] **Service Worker配置**
  - [ ] 创建sw.js文件
  - [ ] 实现缓存策略（缓存优先/网络优先）
  - [ ] 注册Service Worker
  - [ ] 验证离线可用

- [ ] **HTTP缓存配置**
  - [ ] Nginx配置静态资源缓存
  - [ ] 配置Cache-Control头
  - [ ] 配置ETag
  - [ ] 验证缓存生效

### 阶段3: 性能测试

- [ ] **Bundle分析**
  - [ ] 构建生产版本
  - [ ] 生成Bundle分析报告
  - [ ] 分析大体积模块
  - [ ] 优化体积大的模块

- [ ] **Lighthouse测试**
  - [ ] 运行Lighthouse测试
  - [ ] 分析性能评分
  - [ ] 优化低分项
  - [ ] 目标：Performance>90分

- [ ] **Web Vitals监控**
  - [ ] 集成web-vitals库
  - [ ] 实现性能数据上报
  - [ ] 配置性能告警
  - [ ] 验证监控正常

### 阶段4: 生产部署

- [ ] **CDN配置**
  - [ ] 上传静态资源到CDN
  - [ ] 配置CDN缓存策略
  - [ ] 验证CDN加速效果

- [ ] **性能验证**
  - [ ] 灰度发布验证
  - [ ] 监控性能指标
  - [ ] 收集用户反馈
  - [ ] 持续优化

---

## 📚 附录

### A. 性能优化工具推荐

| 工具 | 用途 | 地址 |
|------|------|------|
| **Lighthouse** | 综合性能测试 | https://developers.google.com/web/tools/lighthouse |
| **WebPageTest** | 详细性能分析 | https://www.webpagetest.org/ |
| **Bundle Analyzer** | Bundle体积分析 | https://www.npmjs.com/package/rollup-plugin-visualizer |
| **webpack-bundle-analyzer** | Webpack Bundle分析 | https://www.npmjs.com/package/webpack-bundle-analyzer |
| **vite-plugin-compression** | Gzip/Brotli压缩 | https://www.npmjs.com/package/vite-plugin-compression |
| **vite-plugin-imagemin** | 图片优化 | https://www.npmjs.com/package/vite-plugin-imagemin |

### B. 参考文档

- **Web Vitals**: https://web.dev/vitals/
- **Vite官方文档**: https://vitejs.dev/
- **Vue性能优化**: https://vuejs.org/guide/best-practices/performance.html
- **MDN性能优化**: https://developer.mozilla.org/zh-CN/docs/Web/Performance

---

**文档版本**: v1.0
**最后更新**: 2025-12-26
**作者**: IOE-DREAM 前端性能优化小组
**状态**: ✅ 文档完成，待实施验证
