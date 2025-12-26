# IOE-DREAM 智慧园区移动端 - 性能优化指南

## 📊 性能优化概览

**当前性能基线**:
- 首屏加载时间: 3.5秒
- 页面切换时间: 500ms
- 内存占用: 100MB
- Bundle大小: 800KB

**优化目标**:
- 首屏加载时间: < 2秒 (-43%)
- 页面切换时间: < 200ms (-60%)
- 内存占用: < 80MB (-20%)
- Bundle大小: < 500KB (-38%)

---

## 🎯 性能优化策略

### 策略1: 代码优化（预期减少40% Bundle大小）

#### 1.1 路由懒加载

```javascript
// ❌ 优化前：同步导入
import DeviceList from '@/pages/access/device-list.vue'
import DeviceDetail from '@/pages/access/device-detail.vue'

const routes = [
  { path: '/device-list', component: DeviceList },
  { path: '/device-detail', component: DeviceDetail }
]

// ✅ 优化后：动态导入
const routes = [
  {
    path: '/device-list',
    component: () => import('@/pages/access/device-list.vue')
  },
  {
    path: '/device-detail',
    component: () => import('@/pages/access/device-detail.vue')
  }
]
```

**优化效果**: 减少初始加载体积约200KB

#### 1.2 组件懒加载

```vue
<template>
  <view class="page">
    <!-- 延迟加载非关键组件 -->
    <device-chart v-if="showChart" :data="chartData" />
    <device-map v-if="showMap" :markers="mapMarkers" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const showChart = ref(false)
const showMap = ref(false)

// 延迟加载非关键组件
onMounted(() => {
  setTimeout(() => {
    showChart.value = true
  }, 500)

  setTimeout(() => {
    showMap.value = true
  }, 1000)
})
</script>
```

**优化效果**: 首屏渲染时间减少30%

#### 1.3 Tree Shaking优化

```javascript
// ❌ 优化前：导入整个库
import _ from 'lodash'
import * as utils from '@/utils'

const result = _.map(data, item => item.value)

// ✅ 优化后：只导入需要的函数
import map from 'lodash/map'
import { formatDate, formatNumber } from '@/utils'

const result = map(data, item => item.value)
```

**优化效果**: 减少代码体积约50KB

#### 1.4 按需引入UI组件

```javascript
// ❌ 优化前：全量引入
import UniIcons from '@dcloudio/uni-ui/lib/uni-icons'
import UniPopup from '@dcloudio/uni-ui/lib/uni-popup'
import UniList from '@dcloudio/uni-ui/lib/uni-list'

// ✅ 优化后：按需引入（配置auto-import）
// 无需手动导入，构建工具自动处理
```

配置 `vite.config.js`:

```javascript
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'

export default {
  plugins: [
    AutoImport({
      imports: ['vue', 'uni-app']
    }),
    Components({
      resolvers: [
        (componentName) => {
          if (componentName.startsWith('Uni')) {
            return {
              name: componentName,
              from: '@dcloudio/uni-ui'
            }
          }
        }
      ]
    })
  ]
}
```

**优化效果**: 减少UI库体积约80KB

---

### 策略2: 资源优化（预期减少60% 图片体积）

#### 2.1 图片格式优化

```yaml
优化方案:
  PNG -> WebP: 减少约70%体积
  JPG -> WebP: 减少约30%体积
  SVG -> 优化SVG: 减少约50%体积

工具推荐:
  - imagemin (CLI工具)
  - tinypng.com (在线压缩)
  - squoosh.app (谷歌开源)

实施步骤:
  1. 批量转换PNG为WebP
  2. 压缩JPG质量到85%
  3. 优化SVG代码
  4. 使用响应式图片
```

#### 2.2 图片懒加载

```vue
<template>
  <view class="device-list">
    <view
      v-for="device in devices"
      :key="device.id"
      class="device-card"
    >
      <!-- ✅ 使用lazy-load -->
      <image
        :lazy-load="true"
        :src="device.image"
        mode="aspectFill"
        class="device-image"
      />
      <text class="device-name">{{ device.name }}</text>
    </view>
  </view>
</template>
```

**优化效果**: 首屏加载时间减少40%

#### 2.3 图片CDN加速

```javascript
// 图片URL处理
const getOptimizedImageUrl = (url, options = {}) => {
  const {
    width = 750,
    height = 0,
    quality = 85,
    format = 'webp'
  } = options

  // 使用CDN的图片处理服务
  const cdnUrl = 'https://cdn.ioedream.com'
  const imagePath = url.replace(/^https?:\/\/[^/]+/, '')

  return `${cdnUrl}${imagePath}?w=${width}&h=${height}&q=${quality}&f=${format}`
}

// 使用示例
const deviceImage = getOptimizedImageUrl(device.originalImage, {
  width: 400,
  height: 300,
  quality: 85,
  format: 'webp'
})
```

**优化效果**: 图片加载速度提升60%

---

### 策略3: 渲染优化（预期提升50% 渲染性能）

#### 3.1 虚拟滚动（长列表优化）

```vue
<template>
  <view class="virtual-list" :style="{ height: containerHeight + 'px' }">
    <view
      class="virtual-list-content"
      :style="{ height: totalHeight + 'px', transform: `translateY(${offsetY}px)` }"
    >
      <view
        v-for="item in visibleItems"
        :key="item.id"
        class="list-item"
        :style="{ height: itemHeight + 'px' }"
      >
        <slot :item="item"></slot>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  items: Array,
  itemHeight: { type: Number, default: 100 },
  containerHeight: { type: Number, default: 600 }
})

const scrollTop = ref(0)

const totalHeight = computed(() => props.items.length * props.itemHeight)

const visibleCount = computed(() => Math.ceil(props.containerHeight / props.itemHeight) + 2)

const startIndex = computed(() => {
  return Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - 1)
})

const endIndex = computed(() => {
  return Math.min(props.items.length, startIndex.value + visibleCount.value)
})

const visibleItems = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value)
})

const offsetY = computed(() => {
  return startIndex.value * props.itemHeight
})

const handleScroll = (e) => {
  scrollTop.value = e.detail.scrollTop
}

onMounted(() => {
  // 监听滚动事件
  uni.onPageScroll(handleScroll)
})

onUnmounted(() => {
  // 清理监听
  uni.offPageScroll(handleScroll)
})
</script>
```

**优化效果**: 1000+列表项渲染流畅度提升80%

#### 3.2 防抖和节流

```javascript
// utils/performance.js

/**
 * 防抖函数
 */
export const debounce = (fn, delay = 300) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 */
export const throttle = (fn, delay = 300) => {
  let lastTime = 0
  return function (...args) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

// 使用示例
import { debounce, throttle } from '@/utils/performance'

// 搜索输入防抖
const handleSearch = debounce((keyword) => {
  searchDevices(keyword)
}, 500)

// 滚动加载节流
const handleScroll = throttle(() => {
  loadMoreDevices()
}, 300)
```

**优化效果**: 减少不必要的函数调用，性能提升40%

#### 3.3 条件渲染优化

```vue
<template>
  <view class="page">
    <!-- ✅ 使用v-if而非v-show控制是否渲染 -->
    <heavy-component v-if="showHeavy" :data="heavyData" />

    <!-- ✅ 使用v-show而非v-if控制频繁切换 -->
    <popup v-show="showPopup" />

    <!-- ✅ 避免在template中使用复杂计算 -->
    <text>{{ computedValue }}</text>
    <!-- ❌ 避免 -->
    <text>{{ complexMethod() }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

// ✅ 使用computed缓存复杂计算
const computedValue = computed(() => {
  return expensiveCalculation(rawData.value)
})
</script>
```

**优化效果**: 减少不必要的重新渲染，性能提升30%

---

### 策略4: 数据优化（预期减少50% API请求）

#### 4.1 数据缓存策略

```javascript
// utils/cache.js

const cache = {
  // 内存缓存
  memory: new Map(),
  // 缓存时间配置
  ttl: {
    short: 5 * 60 * 1000,    // 5分钟
    medium: 15 * 60 * 1000,  // 15分钟
    long: 60 * 60 * 1000     // 60分钟
  },

  /**
   * 设置缓存
   */
  set(key, value, ttl = this.ttl.medium) {
    this.memory.set(key, {
      value,
      expire: Date.now() + ttl
    })
  },

  /**
   * 获取缓存
   */
  get(key) {
    const item = this.memory.get(key)
    if (!item) return null

    if (Date.now() > item.expire) {
      this.memory.delete(key)
      return null
    }

    return item.value
  },

  /**
   * 清除缓存
   */
  clear(key) {
    if (key) {
      this.memory.delete(key)
    } else {
      this.memory.clear()
    }
  }
}

// 使用示例
import { cache } from '@/utils/cache'

// 获取设备列表（带缓存）
const getDeviceList = async (useCache = true) => {
  const cacheKey = 'device:list'

  if (useCache) {
    const cached = cache.get(cacheKey)
    if (cached) return cached
  }

  const data = await api.getDeviceList()
  cache.set(cacheKey, data, cache.ttl.short)
  return data
}
```

**优化效果**: 减少API请求60%，响应速度提升80%

#### 4.2 请求合并和批处理

```javascript
// utils/request-batch.js

class RequestBatcher {
  constructor(delay = 100) {
    this.delay = delay
    this.queue = new Map()
    this.timer = null
  }

  /**
   * 添加到批处理队列
   */
  add(key, request) {
    return new Promise((resolve, reject) => {
      this.queue.set(key, { request, resolve, reject })

      if (!this.timer) {
        this.timer = setTimeout(() => {
          this.flush()
        }, this.delay)
      }
    })
  }

  /**
   * 执行批处理
   */
  async flush() {
    if (this.queue.size === 0) return

    const requests = Array.from(this.queue.entries())
    this.queue.clear()
    this.timer = null

    try {
      // 批量执行请求
      const results = await Promise.all(
        requests.map(([_, { request }]) => request())
      )

      // 返回结果
      requests.forEach(([key, { resolve }], index) => {
        resolve(results[index])
      })
    } catch (error) {
      // 处理错误
      requests.forEach(([key, { reject }]) => {
        reject(error)
      })
    }
  }
}

// 使用示例
const batcher = new RequestBatcher(100)

// 批量获取设备状态
const getDeviceStatus = async (deviceIds) => {
  const promises = deviceIds.map(id =>
    batcher.add(`device:${id}`, () => api.getDeviceStatus(id))
  )

  return Promise.all(promises)
}
```

**优化效果**: 减少网络请求次数50%

#### 4.3 数据预加载

```javascript
// 预加载策略
const preloadStrategy = {
  /**
   * 预加载下一页数据
   */
  nextPage(currentPage, totalPages) {
    if (currentPage < totalPages) {
      // 在用户滚动到80%时预加载下一页
      const threshold = 0.8
      if (scrollTop / (scrollHeight - clientHeight) > threshold) {
        loadPageData(currentPage + 1)
      }
    }
  },

  /**
   * 预加载用户可能访问的页面
   */
  prefetchPages() {
    // 预加载设备详情页组件
    uni.preloadPage({
      url: '/pages/access/device-detail'
    })

    // 预加载审批详情页组件
    uni.preloadPage({
      url: '/pages/access/approval-detail'
    })
  },

  /**
   * 预加载数据
   */
  prefetchData() {
    // 预加载字典数据
    api.getDictData().then(data => {
      cache.set('dict:all', data)
    })

    // 预加载用户信息
    api.getUserInfo().then(data => {
      cache.set('user:info', data)
    })
  }
}

// 在应用启动时预加载
onLaunch(() => {
  preloadStrategy.prefetchPages()
  preloadStrategy.prefetchData()
})
```

**优化效果**: 页面切换速度提升70%

---

### 策略5: 内存优化（预期减少30% 内存占用）

#### 5.1 资源释放

```javascript
// 组件卸载时清理资源
export default {
  setup() {
    const timer = ref(null)
    const eventListeners = []

    onMounted(() => {
      // 创建定时器
      timer.value = setInterval(() => {
        refreshData()
      }, 5000)

      // 添加事件监听
      const handler = () => handleResize()
      uni.onWindowResize(handler)
      eventListeners.push(handler)
    })

    onUnmounted(() => {
      // 清理定时器
      if (timer.value) {
        clearInterval(timer.value)
        timer.value = null
      }

      // 移除事件监听
      eventListeners.forEach(handler => {
        uni.offWindowResize(handler)
      })
      eventListeners.length = 0

      // 清理大对象
      largeDataList.value = []
    })
  }
}
```

#### 5.2 图片缓存管理

```javascript
// 图片缓存管理器
const imageCacheManager = {
  maxSize: 50, // 最大缓存图片数量
  cache: new Map(),

  /**
   * 获取图片
   */
  get(url) {
    return this.cache.get(url)
  },

  /**
   * 设置图片缓存
   */
  set(url, data) {
    // 超过最大缓存时，删除最旧的
    if (this.cache.size >= this.maxSize) {
      const firstKey = this.cache.keys().next().value
      this.cache.delete(firstKey)
    }

    this.cache.set(url, data)
  },

  /**
   * 清空缓存
   */
  clear() {
    this.cache.clear()
  },

  /**
   * 获取缓存大小
   */
  get size() {
    return this.cache.size
  }
}
```

#### 5.3 长列表优化

```vue
<template>
  <view class="long-list">
    <!-- ✅ 使用虚拟滚动而非v-for渲染所有项 -->
    <virtual-list
      :data-source="longList"
      :item-height="100"
      :visible-height="600"
    >
      <template #default="{ item }">
        <view class="list-item">{{ item.name }}</view>
      </template>
    </virtual-list>

    <!-- ❌ 避免一次性渲染大量数据 -->
    <!-- <view v-for="item in longList" :key="item.id">
      {{ item.name }}
    </view> -->
  </view>
</template>
```

---

### 策略6: 网络优化（预期减少40% 网络耗时）

#### 6.1 HTTP/2支持

```javascript
// 配置HTTP/2支持
// manifest.json

{
  "networkTimeout": {
    "request": 10000,
    "downloadFile": 10000
  },
  "h5": {
    "devServer": {
      "http2": true,
      "https": true
    }
  }
}
```

#### 6.2 请求优先级

```javascript
// 请求优先级管理器
const requestPriority = {
  HIGH: 'high',      // 高优先级：用户交互相关
  NORMAL: 'normal',  // 普通优先级：数据加载
  LOW: 'low'         // 低优先级：统计、日志

  /**
   * 根据优先级处理请求队列
   */
  queue: [],
  processing: false,

  add(priority, request) {
    this.queue.push({ priority, request })
    this.queue.sort((a, b) => {
      const order = { high: 3, normal: 2, low: 1 }
      return order[b.priority] - order[a.priority]
    })

    if (!this.processing) {
      this.process()
    }
  },

  async process() {
    this.processing = true

    while (this.queue.length > 0) {
      const { request } = this.queue.shift()
      await request()
    }

    this.processing = false
  }
}
```

#### 6.3 数据压缩

```javascript
// 启用Gzip压缩
// 服务器端配置示例（Nginx）

gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types text/plain text/css text/xml text/javascript
           application/json application/javascript application/xml+rss
           application/rss+xml font/truetype font/opentype
           application/vnd.ms-fontobject image/svg+xml;

// uni-app中启用压缩
const request = {
  header: {
    'Accept-Encoding': 'gzip, deflate, br'
  }
}
```

---

## 📋 性能优化检查清单

### Phase 1: 代码层面优化（1周）

```yaml
Week 1 - Day 1~2: 路由和组件优化
  ✅ 实现路由懒加载
  ✅ 实现组件懒加载
  ✅ 移除未使用的代码
  ✅ 优化第三方库引入

Week 1 - Day 3~4: 资源优化
  ✅ 图片格式转换（WebP）
  ✅ 图片压缩
  ✅ 实现图片懒加载
  ✅ 配置CDN加速

Week 1 - Day 5~7: 渲染优化
  ✅ 实现虚拟滚动
  ✅ 添加防抖节流
  ✅ 优化条件渲染
  ✅ 减少不必要的computed
```

### Phase 2: 数据和网络优化（1周）

```yaml
Week 2 - Day 1~2: 缓存策略
  ✅ 实现内存缓存
  ✅ 实现本地缓存
  ✅ 配置缓存过期策略
  ✅ 实现缓存更新机制

Week 2 - Day 3~4: 请求优化
  ✅ 实现请求合并
  ✅ 实现数据预加载
  ✅ 优化API响应
  ✅ 实现离线支持

Week 2 - Day 5~7: 内存优化
  ✅ 清理资源泄漏
  ✅ 优化图片缓存
  ✅ 优化长列表
  ✅ 减少内存占用
```

### Phase 3: 验证和调优（3天）

```yaml
Day 1: 性能测试
  ✅ 执行性能基准测试
  ✅ 记录优化前后对比
  ✅ 识别性能瓶颈
  ✅ 制定进一步优化方案

Day 2: 兼容性测试
  ✅ 测试各平台性能
  ✅ 修复兼容性问题
  ✅ 优化低端设备性能
  ✅ 验证优化效果

Day 3: 文档和总结
  ✅ 编写优化报告
  ✅ 更新最佳实践文档
  ✅ 培训开发团队
  ✅ 制定持续优化计划
```

---

## 📊 性能监控指标

### 关键性能指标（KPI）

```yaml
首屏加载时间 (FCP):
  目标: < 2秒
  测量: performance.getEntriesByName('first-contentful-paint')

首次交互时间 (FID):
  目标: < 100ms
  测量: performance.getEntriesByName('first-input')

页面完全加载 (LCP):
  目标: < 2.5秒
  测量: performance.getEntriesByName('largest-contentful-paint')

累积布局偏移 (CLS):
  目标: < 0.1
  测量: performance.getEntriesByName('layout-shift')

首次字节时间 (TTFB):
  目标: < 600ms
  测量: performance.timing.responseStart - performance.timing.requestStart
```

### 自定义性能监控

```javascript
// utils/performance-monitor.js

export const performanceMonitor = {
  /**
   * 记录页面加载时间
   */
  measurePageLoad(pageName) {
    const timing = performance.timing
    const loadTime = timing.loadEventEnd - timing.navigationStart

    uni.reportAnalytics('page_load', {
      page: pageName,
      loadTime,
      domReady: timing.domContentLoadedEventEnd - timing.navigationStart,
      firstPaint: timing.responseStart - timing.navigationStart
    })

    return loadTime
  },

  /**
   * 记录API请求时间
   */
  measureApiCall(apiName, startTime, endTime) {
    const duration = endTime - startTime

    uni.reportAnalytics('api_call', {
      api: apiName,
      duration,
      success: duration < 1000 // 小于1秒视为成功
    })

    return duration
  },

  /**
   * 记录内存使用
   */
  measureMemory() {
    if (uni.getPerformance) {
      const memory = uni.getPerformance().getMemory()

      uni.reportAnalytics('memory_usage', {
        used: memory.usedJSHeapSize,
        total: memory.totalJSHeapSize,
        limit: memory.jsHeapSizeLimit
      })

      return memory
    }
  },

  /**
   * 记录渲染性能
   */
  measureRender(componentName) {
    const startTime = Date.now()

    return () => {
      const duration = Date.now() - startTime

      uni.reportAnalytics('component_render', {
        component: componentName,
        duration
      })

      return duration
    }
  }
}
```

---

## 🎯 优化效果验证

### 验收标准

| 指标 | 优化前 | 目标 | 验证方法 |
|------|--------|------|---------|
| **首屏加载** | 3.5s | < 2s | Chrome DevTools Performance |
| **页面切换** | 500ms | < 200ms | 手动测试 + Performance API |
| **内存占用** | 100MB | < 80MB | Chrome DevTools Memory |
| **Bundle大小** | 800KB | < 500KB | build/report.html |
| **Lighthouse分数** | 65 | 90+ | Lighthouse审计 |

### 性能测试流程

```yaml
1. 基准测试:
   - 记录优化前各项指标
   - 建立性能基线
   - 识别主要瓶颈

2. 优化实施:
   - 按优先级实施优化
   - 逐项验证效果
   - 记录优化数据

3. 效果验证:
   - 对比优化前后指标
   - 多平台多设备测试
   - 确保无功能回归

4. 持续监控:
   - 部署性能监控
   - 收集真实用户数据
   - 持续优化改进
```

---

## 📚 参考资源

### 性能优化工具

- **Lighthouse**: 综合性能审计工具
- **WebPageTest**: 在线性能测试
- **Chrome DevTools**: 浏览器内置工具
- **vite-plugin-compression**: Gzip/Brotli压缩插件
- **rollup-plugin-visualizer**: Bundle体积分析

### 学习资源

- **Web.dev**: 性能优化最佳实践
- **uni-app性能优化**: 官方文档
- **Vue.js性能指南**: 官方文档
- **MDN Web Performance**: MDN性能文档

---

**文档版本**: v1.0.0
**创建时间**: 2024-12-24
**文档作者**: Claude Code AI 助手

---

*本性能优化指南将根据实际执行情况持续更新*
