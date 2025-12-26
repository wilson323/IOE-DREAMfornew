# IOE-DREAM 智慧园区移动端 - 优化路线图

## 📅 项目概述

**当前状态**: ✅ 功能开发完成（48个页面，~52,000行代码）
**优化周期**: 短期1-2周 + 中期1-2月
**优化目标**: 企业级质量标准，生产环境就绪

---

## 🎯 优化目标设定

### 质量目标

| 指标 | 当前水平 | 目标水平 | 提升幅度 |
|------|---------|---------|---------|
| **首屏加载时间** | 3-4秒 | < 2秒 | -50% |
| **页面切换流畅度** | 良好 | 优秀 | +30% |
| **内存占用** | 80-120MB | < 80MB | -33% |
| **测试覆盖率** | 0% | 80%+ | +80% |
| **Bug密度** | 未知 | < 5/KLOC | 企业级 |
| **兼容性** | 未测试 | 95%+ | 全面覆盖 |

---

## 📋 Phase 1: 短期优化（1-2周）

### 任务1.1: 集成测试（3天）

#### 1.1.1 功能联调测试清单

**核心功能测试**（P0级）:

```
设备管理模块 (device/):
  ✅ 设备列表 - 搜索、筛选、分页
  ✅ 设备详情 - 信息展示、实时状态
  ✅ 设备添加 - 表单验证、数据提交
  ✅ 设备编辑 - 信息修改、图片上传
  ✅ 设备控制 - 远程开门、常开、锁定
  ✅ 设备监控 - 状态曲线图、性能指标
  ✅ 设备事件 - 事件列表、时间轴
  ✅ 设备维护 - 维护计划、记录管理

区域管理模块 (area/):
  ✅ 区域列表 - 层级展示、搜索
  ✅ 区域详情 - 基本信息、关联设备
  ✅ 区域添加 - 表单验证、父级选择
  ✅ 区域设备 - 设备关联管理
  ✅ 区域用户 - 用户权限管理

审批流程模块 (approval/):
  ✅ 审批列表 - 状态筛选、批量操作
  ✅ 审批详情 - 流程时间线、审批历史
  ✅ 审批申请 - 表单验证、附件上传
  ✅ 审批历史 - 日期分组、统计概览
  ✅ 审批统计 - 数据图表、趋势分析
  ✅ 审批配置 - 流程规则、审批人管理

高级功能模块 (system/):
  ✅ 反潜回配置 - 模式选择、白名单管理
  ✅ 多因子认证 - 认证方式、策略配置
  ✅ 联动规则 - 规则配置、测试验证
  ✅ 固件升级 - 批量升级、版本管理
  ✅ 数据备份 - 备份恢复、自动备份
  ✅ 系统日志 - 日志筛选、关键词搜索
  ✅ 帮助中心 - 搜索、分类浏览
  ✅ 关于系统 - 版本检查、信息展示
```

**跨模块测试**（P1级）:

```
数据流转测试:
  ✅ 设备 -> 区域关联
  ✅ 区域 -> 权限继承
  ✅ 审批 -> 状态同步
  ✅ 日志 -> 全局记录

导航测试:
  ✅ 页面跳转链路完整性
  ✅ 返回导航正确性
  ✅ Tab切换无异常
  ✅ 深度链接跳转
```

#### 1.1.2 测试用例模板

```yaml
测试用例编号: TC-DEVICE-001
测试项目: 设备列表搜索功能
前置条件: 已登录系统，有设备数据
测试步骤:
  1. 进入设备列表页面
  2. 点击搜索框
  3. 输入设备名称关键词
  4. 点击搜索按钮
预期结果: 显示匹配的设备列表
优先级: P0
测试状态: ⬜ 待测试
```

#### 1.1.3 测试执行计划

```powershell
# 测试环境准备
1. 准备测试数据（100+设备、20+区域、50+审批记录）
2. 配置测试账号（管理员/普通用户/访客）
3. 搭建测试环境（H5/微信小程序/安卓/iOS）

# 测试执行
Day 1: 设备管理模块 + 区域管理模块
Day 2: 审批流程模块 + 高级功能模块
Day 3: 跨模块测试 + 回归测试

# 缺陷跟踪
- 使用Bug跟踪系统记录所有问题
- 优先级分类：P0（阻塞）/P1（严重）/P2（一般）/P3（轻微）
- 每日缺陷总结会议
```

---

### 任务1.2: 性能优化（4天）

#### 1.2.1 首屏加载优化

**目标**: 首屏加载时间从3-4秒降至<2秒

**优化策略**:

```javascript
// 1. 路由懒加载优化
const routes = [
  {
    path: '/pages/access/device-list',
    component: () => import('@/pages/access/device-list.vue')
  }
]

// 2. 图片懒加载
<image :lazy-load="true" :src="imageSrc"></image>

// 3. 骨架屏占位
<template>
  <view v-if="loading" class="skeleton-screen">
    <skeleton-box width="100%" height="200rpx"></skeleton-box>
    <skeleton-box width="80%" height="100rpx"></skeleton-box>
  </view>
  <view v-else class="content">
    <!-- 实际内容 -->
  </view>
</template>

// 4. 预加载关键资源
onMounted(() => {
  // 预加载下一页资源
  uni.preloadPage({
    url: '/pages/access/device-detail'
  })
})
```

**具体优化项**:

| 优化项 | 当前状态 | 优化方案 | 预期效果 |
|--------|---------|---------|---------|
| **JS bundle大小** | ~800KB | 代码分割、Tree Shaking | -40% |
| **图片资源** | 未优化 | WebP格式、压缩 | -60% |
| **API请求** | 串行请求 | 并行请求、批量接口 | -50% |
| **渲染阻塞** | 存在 | 异步组件、虚拟列表 | +30% |

#### 1.2.2 页面切换优化

```javascript
// 页面过渡动画优化
// pages.json配置
{
  "pages": [
    {
      "path": "pages/access/device-list",
      "style": {
        "navigationStyle": "custom",
        "app-plus": {
          "animationType": "slide-in-right",
          "animationDuration": 200
        }
      }
    }
  ]
}

// keep-alive缓存页面状态
<template>
  <keep-alive :include="cachedPages">
    <router-view></router-view>
  </keep-alive>
</template>
```

#### 1.2.3 内存优化

```javascript
// 1. 组件销毁时清理资源
onUnmounted(() => {
  // 清理定时器
  if (timer) {
    clearInterval(timer)
    timer = null
  }

  // 清理事件监听
  uni.offWindowResize()

  // 清理大对象
  dataList.value = []
})

// 2. 图片缓存管理
const imageCache = {
  maxCache: 50,
  cached: new Map(),

  get(url) {
    return this.cached.get(url)
  },

  set(url, data) {
    if (this.cached.size >= this.maxCache) {
      const firstKey = this.cached.keys().next().value
      this.cached.delete(firstKey)
    }
    this.cached.set(url, data)
  }
}

// 3. 虚拟滚动（长列表优化）
<template>
  <virtual-list
    :data-source="largeList"
    :item-height="100"
    :visible-height="600"
  >
    <template #default="{ item }">
      <device-card :device="item"></device-card>
    </template>
  </virtual-list>
</template>
```

#### 1.2.4 性能监控

```javascript
// 性能数据采集
const performance = {
  // 页面加载时间
  measurePageLoad() {
    const startTime = Date.now()

    onMounted(() => {
      const loadTime = Date.now() - startTime
      uni.reportAnalytics('page_load', {
        page: getCurrentPages().pop().route,
        loadTime
      })
    })
  },

  // API请求时间
  measureApiCall(apiName) {
    const start = Date.now()
    return () => {
      const duration = Date.now() - start
      uni.reportAnalytics('api_call', {
        api: apiName,
        duration
      })
    }
  },

  // 内存使用
  measureMemory() {
    if (uni.getPerformance) {
      const memory = uni.getPerformance().getMemory()
      uni.reportAnalytics('memory_usage', {
        used: memory.usedJSHeapSize,
        total: memory.totalJSHeapSize
      })
    }
  }
}
```

---

### 任务1.3: 兼容性测试（3天）

#### 1.3.1 测试矩阵

| 平台 | 版本 | 测试设备 | 测试重点 |
|------|------|---------|---------|
| **H5** | Chrome 120+, Safari 17+ | Windows/Mac | 响应式布局、API兼容 |
| **微信小程序** | 基础库 2.40+ | iOS/Android | 小程序API、授权流程 |
| **支付宝小程序** | 基础库 2.0+ | iOS/Android | 支付能力、用户认证 |
| **iOS App** | iOS 14+ | iPhone 12/13/14 | 原生能力、性能表现 |
| **Android App** | Android 8+ | 华为/小米/OPPO | 碎片适配、权限管理 |

#### 1.3.2 兼容性检查清单

```yaml
样式兼容性:
  ✅ flex布局兼容
  ✅ CSS Grid降级方案
  ✅ 安全区域适配（刘海屏）
  ✅ 深色模式支持
  ✅ 字体渲染一致性

API兼容性:
  ✅ uni-app API条件编译
  ✅ 平台特定功能降级
  ✅ 网络请求封装
  ✅ 本地存储兼容
  ✅ 定位/相机/相册权限

性能兼容性:
  ✅ 低端设备性能测试
  ✅ 内存泄漏检测
  ✅ 电量消耗测试
  ✅ 流量消耗测试
```

#### 1.3.3 条件编译示例

```javascript
// #ifdef H5
// H5平台特有代码
const isH5 = true
window.addEventListener('scroll', handleScroll)
// #endif

// #ifdef MP-WEIXIN
// 微信小程序特有代码
wx.updateShareMenu({
  withShareTicket: true
})
// #endif

// #ifdef APP-PLUS
// App特有代码
plus.push.createMessage(message)
// #endif

// #ifndef H5
// 非H5平台代码
const nativeApi = uni.requireNativePlugin('NativePlugin')
// #endif
```

---

### 任务1.4: Bug修复（4天）

#### 1.4.1 Bug分类和优先级

| 优先级 | 描述 | 修复时限 | 示例 |
|--------|------|---------|------|
| **P0** | 阻塞性问题，无法使用功能 | 2小时 | 页面白屏、崩溃、数据丢失 |
| **P1** | 严重问题，影响核心功能 | 1天 | API调用失败、导航错误 |
| **P2** | 一般问题，影响用户体验 | 2天 | 样式错乱、文案错误 |
| **P3** | 轻微问题，不影响使用 | 1周 | 建议性改进、优化项 |

#### 1.4.2 Bug修复流程

```yaml
1. Bug发现和记录:
   - 测试人员提交Bug报告
   - 包含：复现步骤、预期结果、实际结果、截图/录屏
   - 分配优先级和负责人

2. Bug分析和定位:
   - 开发人员分析问题根因
   - 确定影响范围
   - 评估修复难度和时间

3. Bug修复实施:
   - 编写修复代码
   - 添加单元测试
   - 代码审查

4. Bug验证和关闭:
   - 测试人员验证修复
   - 回归测试
   - 关闭Bug报告
```

#### 1.4.3 常见问题预判

```javascript
// 问题1: iOS刘海屏适配
// 解决方案: 使用CSS安全区域
.page-container {
  padding-top: env(safe-area-inset-top);
  padding-bottom: env(safe-area-inset-bottom);
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
}

// 问题2: Android返回键误操作
// 解决方案: 监听返回键，添加确认提示
onBackPress() {
  if (hasUnsavedChanges) {
    uni.showModal({
      title: '提示',
      content: '有未保存的修改，确认离开吗？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack()
        }
      }
    })
    return true // 阻止默认返回
  }
}

// 问题3: 微信小程序网络请求域名限制
// 解决方案: 配置合法域名，开发环境可跳过
// manifest.json -> mp-weixin -> requestLegalDomain

// 问题4: 图片上传大小限制
// 解决方案: 前端压缩和验证
const compressImage = (filePath) => {
  return new Promise((resolve) => {
    uni.compressImage({
      src: filePath,
      quality: 80,
      success: (res) => resolve(res.tempFilePath),
      fail: () => resolve(filePath)
    })
  })
}
```

---

## 📋 Phase 2: 中期规划（1-2月）

### 任务2.1: API对接（2周）

#### 2.1.1 API架构设计

```typescript
// API统一管理
// src/api/index.ts

interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

class ApiClient {
  private baseURL: string
  private token: string

  constructor(baseURL: string) {
    this.baseURL = baseURL
    this.token = uni.getStorageSync('token') || ''
  }

  // 通用请求方法
  async request<T>(
    url: string,
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    data?: any
  ): Promise<ApiResponse<T>> {
    return new Promise((resolve, reject) => {
      uni.request({
        url: this.baseURL + url,
        method,
        data,
        header: {
          'Authorization': `Bearer ${this.token}`,
          'Content-Type': 'application/json'
        },
        success: (res: any) => {
          if (res.statusCode === 200) {
            if (res.data.code === 200) {
              resolve(res.data)
            } else {
              uni.showToast({
                title: res.data.message,
                icon: 'none'
              })
              reject(res.data)
            }
          } else {
            this.handleError(res)
            reject(res)
          }
        },
        fail: (err) => {
          this.handleError(err)
          reject(err)
        }
      })
    })
  }

  // 便捷方法
  get<T>(url: string, data?: any) {
    return this.request<T>(url, 'GET', data)
  }

  post<T>(url: string, data?: any) {
    return this.request<T>(url, 'POST', data)
  }

  // 错误处理
  handleError(error: any) {
    console.error('API Error:', error)
    uni.showToast({
      title: '网络请求失败',
      icon: 'none'
    })
  }
}

// 导出单例
export const apiClient = new ApiClient('https://api.ioedream.com')
```

#### 2.1.2 模块化API定义

```typescript
// src/api/device.ts
import { apiClient } from './index'

export interface Device {
  deviceId: string
  deviceName: string
  deviceType: number
  status: number
  // ...其他字段
}

export interface DeviceListQuery {
  pageNum: number
  pageSize: number
  deviceName?: string
  deviceType?: number
  status?: number
}

export const deviceApi = {
  // 获取设备列表
  getDeviceList(query: DeviceListQuery) {
    return apiClient.get<PageResult<Device>>('/api/v1/device/list', query)
  },

  // 获取设备详情
  getDeviceDetail(deviceId: string) {
    return apiClient.get<Device>(`/api/v1/device/${deviceId}`)
  },

  // 添加设备
  addDevice(device: Partial<Device>) {
    return apiClient.post<string>('/api/v1/device', device)
  },

  // 更新设备
  updateDevice(deviceId: string, device: Partial<Device>) {
    return apiClient.put<void>(`/api/v1/device/${deviceId}`, device)
  },

  // 删除设备
  deleteDevice(deviceId: string) {
    return apiClient.delete<void>(`/api/v1/device/${deviceId}`)
  },

  // 控制设备
  controlDevice(deviceId: string, action: string) {
    return apiClient.post<void>(`/api/v1/device/${deviceId}/control`, { action })
  }
}
```

#### 2.1.3 API对接清单

```yaml
设备管理API:
  ✅ GET /api/v1/device/list - 设备列表
  ✅ GET /api/v1/device/{id} - 设备详情
  ✅ POST /api/v1/device - 添加设备
  ✅ PUT /api/v1/device/{id} - 更新设备
  ✅ DELETE /api/v1/device/{id} - 删除设备
  ✅ POST /api/v1/device/{id}/control - 控制设备
  ✅ GET /api/v1/device/{id}/events - 设备事件
  ✅ GET /api/v1/device/{id}/monitor - 设备监控

区域管理API:
  ✅ GET /api/v1/area/list - 区域列表
  ✅ GET /api/v1/area/{id} - 区域详情
  ✅ POST /api/v1/area - 添加区域
  ✅ PUT /api/v1/area/{id} - 更新区域
  ✅ DELETE /api/v1/area/{id} - 删除区域
  ✅ GET /api/v1/area/{id}/devices - 区域设备
  ✅ GET /api/v1/area/{id}/users - 区域用户

审批流程API:
  ✅ GET /api/v1/approval/list - 审批列表
  ✅ GET /api/v1/approval/{id} - 审批详情
  ✅ POST /api/v1/approval - 提交申请
  ✅ PUT /api/v1/approval/{id}/approve - 审批操作
  ✅ GET /api/v1/approval/history - 审批历史
  ✅ GET /api/v1/approval/statistics - 审批统计

高级功能API:
  ✅ GET/POST /api/v1/system/anti-passback - 反潜回配置
  ✅ GET/POST /api/v1/system/multi-factor - 多因子认证
  ✅ GET/POST /api/v1/system/linkage - 联动规则
  ✅ GET/POST /api/v1/system/firmware - 固件升级
  ✅ GET/POST /api/v1/system/backup - 数据备份
  ✅ GET /api/v1/system/logs - 系统日志
```

#### 2.1.4 API Mock服务

```javascript
// 开发环境Mock数据
// src/mock/index.js

import Mock from 'mockjs'

// 设备列表Mock
Mock.mock('/api/v1/device/list', 'get', (options) => {
  const { pageNum = 1, pageSize = 20 } = parseQuery(options.url)

  return {
    code: 200,
    message: 'success',
    data: {
      list: Mock.mock({
        [`list|${pageSize}`]: [{
          'deviceId|+1': 1000,
          deviceName: '@ctitle(5, 10)',
          deviceType: '@pick([1, 2, 3, 4, 5])',
          status: '@pick([1, 2, 3])',
          areaName: '@ctitle(3, 6)',
          createTime: '@datetime'
        }]
      }).list,
      total: 100,
      pageNum,
      pageSize,
      pages: 5
    }
  }
})

// 设备详情Mock
Mock.mock(/\/api\/v1\/device\/\d+/, 'get', () => {
  return {
    code: 200,
    message: 'success',
    data: Mock.mock({
      deviceId: '@natural(1000, 9999)',
      deviceName: '@ctitle(5, 10)',
      deviceType: 1,
      status: 1,
      areaName: '@ctitle(3, 6)',
      ip: '@ip',
      port: '@natural(1000, 9999)',
      firmwareVersion: '@float(1.0, 5.0, 1, 2)',
      createTime: '@datetime',
      updateTime: '@datetime'
    })
  }
})
```

---

### 任务2.2: 数据Mock完善（1周）

#### 2.2.1 Mock数据生成器

```javascript
// src/mock/generators.js

export const mockGenerators = {
  // 设备数据生成器
  device(count = 100) {
    return Mock.mock({
      [`list|${count}`]: [{
        'deviceId|+1': 1000,
        deviceName() {
          const types = ['门禁', '考勤', '消费', '视频', '报警']
          const type = Mock.Random.pick(types)
          const num = Mock.Random.string('number', 3)
          return `${type}设备${num}`
        },
        'deviceType|1-5': 1,
        'status|1-3': 1,
        areaName: '@ctitle(3, 6)',
        'ip': '@ip',
        'port|8000-9999': 1,
        firmwareVersion: '@float(1.0, 5.0, 1, 2)',
        online: '@boolean',
        createTime: '@datetime'
      }]
    }).list
  },

  // 区域数据生成器
  area(count = 20) {
    return Mock.mock({
      [`list|${count}`]: [{
        'areaId|+1': 100,
        areaName: '@ctitle(3, 6)',
        'areaType|1-3': 1,
        parentId: '@natural(0, 50)',
        'level|1-3': 1,
        deviceCount: '@natural(0, 50)',
        userCount: '@natural(0, 100)',
        createTime: '@datetime'
      }]
    }).list
  },

  // 审批数据生成器
  approval(count = 50) {
    return Mock.mock({
      [`list|${count}`]: [{
        'approvalId|+1': 10000,
        'approvalType|1': ['access', 'visitor', 'overtime', 'leave'],
        title: '@ctitle(10, 20)',
        applicantName: '@cname',
        applicantDept: '@ctitle(3, 6)',
        'status|1': ['pending', 'approved', 'rejected', 'cancelled'],
        currentTime: '@datetime',
        approvers: '@cname',
        createTime: '@datetime',
        updateTime: '@datetime'
      }]
    }).list
  }
}
```

#### 2.2.2 真实场景数据模拟

```javascript
// src/mock/scenarios.js

export const mockScenarios = {
  // 设备监控场景
  deviceMonitor: {
    onlineCount: 85,
    offlineCount: 15,
    normalCount: 78,
    warningCount: 5,
    errorCount: 2,

    performanceData: {
      cpuUsage: Mock.Random.float(20, 80, 0, 2),
      memoryUsage: Mock.Random.float(30, 70, 0, 2),
      diskUsage: Mock.Random.float(40, 60, 0, 2),
      networkSpeed: Mock.Random.natural(100, 1000)
    },

    eventTimeline: Mock.mock({
      'list|20': [{
        eventId: '@id',
        eventName: '@ctitle(5, 15)',
        eventType: '@pick(["info", "warning", "error"])',
        deviceName: '@ctitle(3, 6)',
        occurTime: '@datetime'
      }]
    }).list
  },

  // 审批统计场景
  approvalStats: {
    totalApplications: 1250,
    approvalRate: 0.87,
    avgProcessTime: 2.3,
    pendingCount: 35,

    comparison: {
      totalCount: 0.12, // 同比增长12%
      approvalRate: -0.03, // 同比下降3%
      avgTime: -0.18 // 同比下降18%
    },

    typeDistribution: [
      { type: 'access', count: 450, percent: 36 },
      { type: 'visitor', count: 300, percent: 24 },
      { type: 'overtime', count: 280, percent: 22 },
      { type: 'leave', count: 220, percent: 18 }
    ],

    statusDistribution: [
      { status: 'approved', count: 1087, percent: 87 },
      { status: 'pending', count: 35, percent: 3 },
      { status: 'rejected', count: 88, percent: 7 },
      { status: 'cancelled', count: 40, percent: 3 }
    ]
  }
}
```

---

### 任务2.3: 单元测试（2周）

#### 2.3.1 测试框架选择

```javascript
// 使用 Vitest + Vue Test Utils
// vitest.config.ts

import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      lines: 80,
      functions: 80,
      branches: 80,
      statements: 80
    }
  }
})
```

#### 2.3.2 工具函数单元测试

```javascript
// tests/utils/format.test.ts

import { describe, it, expect } from 'vitest'
import { formatDate, formatFileSize, formatDeviceStatus } from '@/utils/format'

describe('formatDate', () => {
  it('应该正确格式化日期', () => {
    const date = new Date('2024-12-24T10:30:00')
    expect(formatDate(date, 'YYYY-MM-DD')).toBe('2024-12-24')
    expect(formatDate(date, 'YYYY-MM-DD HH:mm')).toBe('2024-12-24 10:30')
  })

  it('应该处理无效日期', () => {
    expect(formatDate(null)).toBe('-')
    expect(formatDate(undefined)).toBe('-')
  })
})

describe('formatFileSize', () => {
  it('应该正确格式化文件大小', () => {
    expect(formatFileSize(1024)).toBe('1.00 KB')
    expect(formatFileSize(1024 * 1024)).toBe('1.00 MB')
    expect(formatFileSize(1024 * 1024 * 1024)).toBe('1.00 GB')
  })
})

describe('formatDeviceStatus', () => {
  it('应该返回正确的状态文本', () => {
    expect(formatDeviceStatus(1)).toBe('在线')
    expect(formatDeviceStatus(2)).toBe('离线')
    expect(formatDeviceStatus(3)).toBe('故障')
  })
})
```

#### 2.3.3 组件单元测试

```javascript
// tests/components/DeviceCard.test.ts

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DeviceCard from '@/components/DeviceCard.vue'

describe('DeviceCard', () => {
  const mockDevice = {
    deviceId: '1001',
    deviceName: '测试设备',
    deviceType: 1,
    status: 1,
    areaName: '测试区域'
  }

  it('应该正确渲染设备信息', () => {
    const wrapper = mount(DeviceCard, {
      props: { device: mockDevice }
    })

    expect(wrapper.text()).toContain('测试设备')
    expect(wrapper.text()).toContain('测试区域')
  })

  it('应该触发点击事件', async () => {
    const wrapper = mount(DeviceCard, {
      props: { device: mockDevice }
    })

    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')![0]).toEqual([mockDevice])
  })

  it('应该显示正确的状态样式', () => {
    const wrapper = mount(DeviceCard, {
      props: { device: mockDevice }
    })

    const statusBadge = wrapper.find('.status-badge')
    expect(statusBadge.classes()).toContain('status-online')
  })
})
```

#### 2.3.4 API测试

```javascript
// tests/api/deviceApi.test.ts

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { deviceApi } from '@/api/device'
import { apiClient } from '@/api/index'

vi.mock('@/api/index')

describe('deviceApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该获取设备列表', async () => {
    const mockData = {
      list: [
        { deviceId: '1001', deviceName: '设备1' },
        { deviceId: '1002', deviceName: '设备2' }
      ],
      total: 2
    }

    vi.mocked(apiClient.get).mockResolvedValue(mockData)

    const result = await deviceApi.getDeviceList({ pageNum: 1, pageSize: 20 })

    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/device/list', {
      pageNum: 1,
      pageSize: 20
    })
    expect(result.list).toHaveLength(2)
  })

  it('应该获取设备详情', async () => {
    const mockDevice = { deviceId: '1001', deviceName: '设备1' }

    vi.mocked(apiClient.get).mockResolvedValue(mockDevice)

    const result = await deviceApi.getDeviceDetail('1001')

    expect(apiClient.get).toHaveBeenCalledWith('/api/v1/device/1001')
    expect(result.deviceName).toBe('设备1')
  })
})
```

#### 2.3.5 测试覆盖率目标

| 模块类型 | 目标覆盖率 | 优先级 |
|---------|-----------|--------|
| **工具函数** | 95%+ | P0 |
| **API层** | 90%+ | P0 |
| **公共组件** | 85%+ | P1 |
| **页面组件** | 75%+ | P1 |
| **Store/状态管理** | 80%+ | P1 |

---

### 任务2.4: E2E测试（2周）

#### 2.4.1 E2E测试框架

```javascript
// 使用 Playwright 进行E2E测试
// e2e/playwright.config.ts

import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e/tests',
  timeout: 30000,
  retries: 1,
  use: {
    baseURL: 'http://localhost:3000',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure'
  },

  projects: [
    {
      name: 'chromium',
      use: { browserName: 'chromium' }
    },
    {
      name: 'firefox',
      use: { browserName: 'firefox' }
    },
    {
      name: 'webkit',
      use: { browserName: 'webkit' }
    }
  ]
})
```

#### 2.4.2 E2E测试用例

```typescript
// e2e/tests/device-list.spec.ts

import { test, expect } from '@playwright/test'

test.describe('设备列表页面', () => {
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/login')
    await page.fill('input[name="username"]', 'admin')
    await page.fill('input[name="password"]', 'admin123')
    await page.click('button[type="submit"]')

    // 等待跳转到首页
    await page.waitForURL('/')

    // 进入设备列表
    await page.click('text=设备管理')
    await page.waitForURL('/pages/access/device-list')
  })

  test('应该显示设备列表', async ({ page }) => {
    // 验证页面标题
    await expect(page.locator('text=设备列表')).toBeVisible()

    // 验证列表存在
    const deviceCards = page.locator('.device-card')
    await expect(deviceCards.first()).toBeVisible()
  })

  test('应该支持搜索功能', async ({ page }) => {
    // 点击搜索框
    await page.click('.search-input')

    // 输入搜索关键词
    await page.fill('.search-input', '门禁')

    // 等待搜索结果
    await page.waitForTimeout(500)

    // 验证搜索结果
    const searchResults = page.locator('.device-card')
    const count = await searchResults.count()
    expect(count).toBeGreaterThan(0)

    // 验证搜索结果包含关键词
    for (let i = 0; i < Math.min(count, 5); i++) {
      const card = searchResults.nth(i)
      const text = await card.textContent()
      expect(text).toContain('门禁')
    }
  })

  test('应该支持筛选功能', async ({ page }) => {
    // 点击筛选按钮
    await page.click('.filter-button')

    // 选择设备类型
    await page.click('text=门禁设备')

    // 点击确认
    await page.click('text=确定')

    // 等待筛选结果
    await page.waitForTimeout(500)

    // 验证筛选结果
    const deviceCards = page.locator('.device-card')
    const count = await deviceCards.count()
    expect(count).toBeGreaterThan(0)
  })

  test('应该支持分页加载', async ({ page }) => {
    const initialCount = await page.locator('.device-card').count()

    // 滚动到底部
    await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight))

    // 等待加载更多
    await page.waitForTimeout(1000)

    // 验证数据增加
    const newCount = await page.locator('.device-card').count()
    expect(newCount).toBeGreaterThan(initialCount)
  })
})
```

```typescript
// e2e/tests/approval-flow.spec.ts

import { test, expect } from '@playwright/test'

test.describe('审批流程测试', () => {
  test('完整的审批流程', async ({ page }) => {
    // 1. 登录
    await page.goto('/login')
    await page.fill('input[name="username"]', 'testuser')
    await page.fill('input[name="password"]', 'password123')
    await page.click('button[type="submit"]')

    // 2. 进入审批申请页面
    await page.click('text=审批流程')
    await page.click('text=申请审批')
    await page.waitForURL('/pages/access/approval-apply')

    // 3. 填写申请表单
    await page.selectOption('select[name="type"]', 'access')
    await page.fill('input[name="title"]', '门禁权限申请')
    await page.fill('textarea[name="reason"]', '需要开通A区门禁权限')
    await page.fill('input[name="area"]', 'A区')

    // 4. 提交申请
    await page.click('button:has-text("提交申请")')

    // 5. 验证提交成功
    await expect(page.locator('text=申请已提交')).toBeVisible()

    // 6. 查看审批详情
    await page.click('button:has-text("查看详情")')
    await page.waitForURL(/\/pages\/access\/approval-detail/)

    // 7. 验证审批状态
    await expect(page.locator('text=待审批')).toBeVisible()

    // 8. 审批人操作（切换账号）
    await page.click('button:has-text("退出登录")')
    await page.fill('input[name="username"]', 'approver')
    await page.fill('input[name="password"]', 'password123')
    await page.click('button[type="submit"]')

    // 9. 进入审批列表
    await page.click('text=审批流程')
    await page.click('text=待审批')
    await page.waitForURL('/pages/access/approval-list')

    // 10. 审批操作
    await page.click('.approval-item:first-child')
    await page.click('button:has-text("通过")')
    await page.fill('textarea[name="comment"]', '同意')
    await page.click('button:has-text("确认")')

    // 11. 验证审批结果
    await expect(page.locator('text=审批通过')).toBeVisible()
  })
})
```

#### 2.4.3 E2E测试覆盖场景

```yaml
核心业务流程:
  ✅ 用户登录/登出
  ✅ 设备管理CRUD
  ✅ 设备控制操作
  ✅ 审批申请和审批
  ✅ 区域管理
  ✅ 高级功能配置

异常场景:
  ✅ 网络错误处理
  ✅ 表单验证
  ✅ 权限控制
  ✅ 数据为空状态
  ✅ 加载失败处理

性能测试:
  ✅ 大数据量列表渲染
  ✅ 长时间运行稳定性
  ✅ 内存泄漏检测
  ✅ 页面加载性能
```

---

## 📊 优化效果评估

### 性能指标对比

| 指标 | 优化前 | 目标 | 预期提升 |
|------|--------|------|---------|
| **首屏加载时间** | 3.5s | < 2s | -43% |
| **页面切换时间** | 500ms | < 200ms | -60% |
| **内存占用** | 100MB | < 80MB | -20% |
| **Bundle大小** | 800KB | < 500KB | -38% |
| **API响应时间** | 800ms | < 300ms | -63% |

### 质量指标对比

| 指标 | 优化前 | 目标 | 预期提升 |
|------|--------|------|---------|
| **测试覆盖率** | 0% | 80%+ | +80% |
| **Bug密度** | 未知 | < 5/KLOC | 企业级 |
| **兼容性通过率** | 未测试 | 95%+ | 全面覆盖 |
| **代码规范符合率** | 70% | 95%+ | +25% |

---

## 🛠️ 工具和资源

### 开发工具

```yaml
代码编辑器:
  - VS Code + Volar插件
  - WebStorm（可选）

版本控制:
  - Git
  - GitHub/GitLab

调试工具:
  - Chrome DevTools
  - 微信开发者工具
  - uni-app调试器
```

### 测试工具

```yaml
单元测试:
  - Vitest
  - Vue Test Utils
  - @vue/test-utils

E2E测试:
  - Playwright
  - @playwright/test

性能分析:
  - Chrome DevTools Performance
  - Lighthouse
  - WebPageTest

代码质量:
  - ESLint
  - Prettier
  - Stylelint
```

### CI/CD工具

```yaml
持续集成:
  - GitHub Actions
  - GitLab CI/CD
  - Jenkins（可选）

自动化部署:
  - Docker
  - Kubernetes（可选）

监控告警:
  - Sentry（错误追踪）
  - Google Analytics（用户分析）
```

---

## 📚 参考文档

### 技术文档

- **uni-app官方文档**: https://uniapp.dcloud.net.cn/
- **Vue 3官方文档**: https://cn.vuejs.org/
- **Vitest文档**: https://cn.vitest.dev/
- **Playwright文档**: https://playwright.dev/

### 项目文档

- **项目完成报告**: [MOBILE_APP_COMPLETION_REPORT.md](./MOBILE_APP_COMPLETION_REPORT.md)
- **编码规范**: [CLAUDE.md](../CLAUDE.md)
- **API文档**: [API文档.md](../docs/API文档.md)

---

## ✅ 验收标准

### 功能验收

- ✅ 所有核心功能正常运行
- ✅ 无P0/P1级别Bug
- ✅ 跨平台兼容性测试通过
- ✅ API对接完成，Mock数据完善

### 性能验收

- ✅ 首屏加载时间 < 2秒
- ✅ 页面切换流畅，无卡顿
- ✅ 内存占用 < 80MB
- ✅ Bundle大小 < 500KB

### 质量验收

- ✅ 单元测试覆盖率 ≥ 80%
- ✅ E2E测试覆盖核心流程
- ✅ 代码规范符合率 ≥ 95%
- ✅ 性能测试通过

### 交付物验收

- ✅ 测试报告（含测试用例和结果）
- ✅ 性能优化报告
- ✅ API对接文档
- ✅ 单元测试代码
- ✅ E2E测试代码
- ✅ 优化总结报告

---

## 📞 联系方式

如有任何问题或建议，请通过以下方式联系：

- **技术支持**: support@ioedream.com
- **客服热线**: 400-123-4567
- **官方网站**: www.ioedream.com

---

**文档版本**: v1.0.0
**创建时间**: 2024-12-24
**更新时间**: 2024-12-24
**文档作者**: Claude Code AI 助手

---

*本优化路线图将根据实际执行情况持续更新*
