# 前端测试配置指南

## 📋 概述

本文档指导如何为 `smart-admin-web-javascript` 前端项目配置测试框架。

---

## 🎯 推荐测试框架

### Vitest (推荐)

**优势**:
- 与Vite完美集成
- 快速执行
- 支持Vue 3组件测试
- 内置覆盖率工具

---

## 📦 安装步骤

### 1. 安装依赖

```bash
cd smart-admin-web-javascript
npm install -D vitest @vue/test-utils @vitest/ui @vitest/coverage-v8 jsdom
```

### 2. 更新 package.json

添加测试脚本:

```json
{
  "scripts": {
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage",
    "test:run": "vitest run"
  }
}
```

### 3. 创建 vitest.config.js

```javascript
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/main.js',
        '**/*.config.js',
        '**/*.spec.js',
        '**/*.test.js'
      ]
    }
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, './src')
    }
  }
})
```

---

## 🧪 测试示例

### 组件测试示例

创建 `src/components/__tests__/RadarChart.test.js`:

```javascript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RadarChart from '../business/common/charts/RadarChart.vue'

describe('RadarChart', () => {
  it('应该正确渲染', () => {
    const wrapper = mount(RadarChart, {
      props: {
        title: '测试雷达图',
        height: 400,
        indicator: [
          { name: '指标1', max: 100 },
          { name: '指标2', max: 100 }
        ],
        series: [
          {
            name: '系列1',
            value: [80, 90]
          }
        ]
      }
    })
    
    expect(wrapper.find('.ant-card').exists()).toBe(true)
  })
  
  it('应该响应props变化', async () => {
    const wrapper = mount(RadarChart, {
      props: {
        title: '初始标题',
        indicator: [],
        series: []
      }
    })
    
    await wrapper.setProps({
      title: '新标题'
    })
    
    expect(wrapper.text()).toContain('新标题')
  })
})
```

### API服务测试示例

创建 `src/api/__tests__/consume-api.test.js`:

```javascript
import { describe, it, expect, vi } from 'vitest'
import axios from 'axios'
import * as consumeApi from '../business/consume/consume-api'

vi.mock('axios')

describe('consume-api', () => {
  it('应该正确调用消费API', async () => {
    const mockData = { success: true }
    axios.post.mockResolvedValue({ data: mockData })
    
    const result = await consumeApi.consume({ amount: 10 })
    
    expect(axios.post).toHaveBeenCalled()
    expect(result).toEqual(mockData)
  })
})
```

---

## 📊 覆盖率目标

- **语句覆盖率**: ≥80%
- **分支覆盖率**: ≥75%
- **函数覆盖率**: ≥80%
- **行覆盖率**: ≥80%

---

## 🚀 执行测试

```bash
# 开发模式 (监听文件变化)
npm run test

# UI模式 (可视化界面)
npm run test:ui

# 生成覆盖率报告
npm run test:coverage

# 单次运行 (CI/CD)
npm run test:run
```

---

## 📝 测试最佳实践

1. **测试命名**: 使用描述性名称
2. **测试结构**: Arrange-Act-Assert模式
3. **测试隔离**: 每个测试独立运行
4. **Mock使用**: 合理使用Mock避免外部依赖
5. **覆盖率**: 关注关键业务逻辑覆盖率

---

## ✅ 检查清单

- [ ] 安装测试依赖
- [ ] 配置vitest.config.js
- [ ] 添加测试脚本
- [ ] 创建测试文件
- [ ] 执行测试验证
- [ ] 生成覆盖率报告

---

**配置完成后**: 执行 `npm run test` 验证配置是否正确。
