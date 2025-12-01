# IOE-DREAM 技术规范体系 v2.0

## 📋 核心原则

### 1. 统一性原则
- 所有代码必须遵循统一的技术栈和编码标准
- 严禁混用不同技术栈的特性（如在JavaScript项目中使用TypeScript语法）
- 保持项目架构的一致性

### 2. 质量优先原则
- 代码质量优先于开发速度
- 所有代码变更必须通过自动化检查
- 建立强制性的质量门禁

### 3. 可维护性原则
- 代码必须易于理解和维护
- 建立清晰的模块边界和依赖关系
- 确保技术债务的可控管理

## 🎨 Vue 3 开发规范

### Composition API 使用规范

#### ✅ 正确示例
```vue
<script setup>
import { ref, computed } from 'vue'

// Props定义 - 必须使用withDefaults
interface Props {
  visible: boolean
  title: string
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  title: ''
})

// Emits定义 - 必须显式声明
interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'confirm'): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const formData = ref({
  name: '',
  age: 0
})

// 计算属性
const isValid = computed(() => {
  return formData.value.name.length > 0
})

// 方法
const handleClose = () => {
  emit('update:visible', false)
}
</script>

<template>
  <a-modal
    :open="props.visible"
    :title="props.title"
    @update:open="val => emit('update:visible', val)"
    @cancel="handleClose"
  >
    <!-- 模态框内容 -->
  </a-modal>
</template>
```

#### ❌ 禁止使用
```vue
<!-- 禁止：在JavaScript中使用TypeScript类型注解 -->
<script setup>
const data: MyType = {}  // ❌ 禁止
</script>

<!-- 禁止：对prop直接使用v-model -->
<template>
  <a-modal v-model:open="visible" />  <!-- ❌ 禁止，visible是prop -->
</template>

<!-- 禁止：重复声明变量 -->
<script setup>
const handleSomething = () => {}
const handleSomething = () => {}  <!-- ❌ 禁止重复声明 -->
</script>
```

## 🏗️ 架构设计规范

### 模块化设计原则
1. **单一职责**：每个模块只负责一个明确的功能域
2. **低耦合高内聚**：模块间依赖关系清晰，内部逻辑紧密
3. **依赖倒置**：高层模块不依赖低层模块，都依赖抽象
4. **开闭原则**：对扩展开放，对修改关闭

### 文件组织规范
```
src/
├── api/                    # API接口层
│   ├── modules/           # 按业务模块分组
│   │   ├── user.ts        # 用户相关API
│   │   └── device.ts      # 设备相关API
│   └── index.ts           # API统一导出
├── components/            # 公共组件
│   ├── base/             # 基础组件
│   ├── business/         # 业务组件
│   └── layout/           # 布局组件
├── views/                 # 页面组件
│   └── modules/          # 按业务模块分组
├── stores/               # 状态管理
├── utils/                # 工具函数
├── types/                # TypeScript类型定义
└── assets/               # 静态资源
```

## 🔧 代码质量标准

### 命名规范
```javascript
// ✅ 组件命名：PascalCase
const UserListComponent = {}

// ✅ 变量命名：camelCase
const userName = ''
const isVaild = true

// ✅ 常量命名：UPPER_SNAKE_CASE
const API_BASE_URL = ''

// ✅ 文件命名：kebab-case
// user-list-component.vue
// device-management.service.ts

// ✅ CSS类名：BEM规范
.user-list__item--active
```

### 函数设计规范
```javascript
// ✅ 函数命名清晰表达意图
const validateUserInput = (userData) => {}
const calculateTotalPrice = (items) => {}

// ✅ 函数职责单一
const processUserData = (user) => {
  const validatedData = validateUser(user)
  const transformedData = transformUser(validatedData)
  return transformedData
}

// ❌ 避免函数过长或职责不清晰
const processData = (data) => {
  // 验证、转换、存储、通知...（职责过多）
}
```

## 📝 注释规范

### JSDoc注释标准
```javascript
/**
 * 用户管理服务
 *
 * @description 负责用户相关的业务逻辑处理
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-14
 */
class UserService {
  /**
   * 创建新用户
   *
   * @param {Object} userData - 用户数据
   * @param {string} userData.name - 用户姓名
   * @param {number} userData.age - 用户年龄
   * @returns {Promise<User>} 创建的用户对象
   * @throws {ValidationError} 当用户数据无效时
   * @example
   * const user = await userService.createUser({
   *   name: 'John Doe',
   *   age: 30
   * })
   */
  async createUser(userData) {
    // 实现逻辑
  }
}
```

## 🔄 Git工作流规范

### 分支命名策略
- `feature/功能名称` - 新功能开发
- `fix/问题描述` - 问题修复
- `hotfix/紧急修复` - 生产紧急修复
- `refactor/重构内容` - 代码重构
- `docs/文档更新` - 文档更新

### 提交信息规范
```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型说明：**
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档变更
- `style`: 代码格式变更
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建或辅助工具变动

**示例：**
```
feat(auth): 添加JWT令牌刷新机制

- 实现自动令牌刷新逻辑
- 添加令牌过期检测
- 优化用户认证体验

Closes #123
```

## 🚀 性能优化标准

### 前端性能要求
1. **首屏加载时间**：< 2秒
2. **路由切换时间**：< 300ms
3. **API响应时间**：< 500ms
4. **Bundle大小**：< 2MB (gzipped)

### 性能优化策略
- 路由懒加载
- 组件按需加载
- 图片懒加载和压缩
- API接口缓存
- 代码分割和tree-shaking

## 🔒 安全规范

### 前端安全要求
- 用户输入验证和转义
- XSS攻击防护
- CSRF攻击防护
- 敏感信息加密存储
- 安全的API调用

### 代码安全检查
```javascript
// ✅ 安全的API调用
const apiCall = async () => {
  try {
    const response = await fetch('/api/data', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-Token': getCsrfToken()
      },
      body: JSON.stringify(sanitizeData(userData))
    })
    return await response.json()
  } catch (error) {
    handleError(error)
  }
}

// ❌ 危险的操作
const dangerousCode = () => {
  eval(userInput) // ❌ 禁止使用eval
  document.write(userContent) // ❌ 禁止直接写入DOM
}
```

---

**本规范文档版本：v2.0**
**最后更新时间：2025-11-14**
**负责人：技术架构团队**

所有开发人员必须严格遵守此规范，违规代码将无法通过代码审查和自动化检查。