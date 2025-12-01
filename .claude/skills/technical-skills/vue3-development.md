# 🖥️ Vue3 前端开发技能

**技能名称**: Vue3 + TypeScript 企业级前端开发
**技能等级**: 高级
**适用角色**: 前端开发工程师、全栈工程师、UI/UX开发者
**前置技能**: JavaScript基础、HTML/CSS基础、前端框架基础、Node.js基础
**预计学时**: 32小时

---

## 📚 知识要求

### 📖 理论知识
- **Vue3核心原理**: 深入理解Vue3的响应式系统、Composition API、虚拟DOM等核心机制
- **TypeScript高级特性**: 掌握泛型、装饰器、高级类型、模块系统等TypeScript特性
- **前端工程化**: 理解Webpack/Vite构建原理、模块化开发、代码分割等工程化概念
- **组件设计模式**: 掌握高阶组件、渲染函数、自定义指令等高级组件设计模式

### 💼 业务理解
- **用户交互设计**: 理解用户体验设计和交互设计的基本原理
- **响应式设计**: 掌握移动端适配和响应式布局的设计方法
- **性能优化**: 了解前端性能优化的策略和技术手段
- **可访问性**: 理解Web可访问性标准和实现方法

### 🔧 技术背景
- **Vue3生态系统**: 熟练使用Vue Router 4、Pinia、VueUse等Vue3生态工具
- **UI组件库**: 深入掌握Ant Design Vue 4.x、Element Plus等UI组件库
- **构建工具**: 熟练使用Vite 5、PostCSS、ESLint等前端构建工具
- **调试工具**: 掌握Vue DevTools、浏览器开发者工具等调试手段

---

## 🛠️ 操作步骤

### 步骤1: Vue3 项目环境搭建 (4小时)

#### 1.1 开发环境配置
```bash
# 1. 安装Node.js 18+
node --version  # 确保版本 >= 18.0.0

# 2. 安装pnpm包管理器
npm install -g pnpm

# 3. 创建Vue3项目
pnpm create vue@latest smart-admin-frontend

# 4. 项目配置选择
✔ Project name: › smart-admin-frontend
✔ Add TypeScript? › Yes
✔ Add JSX Support? › No
✔ Add Vue Router for Single Page Application development? › Yes
✔ Add Pinia for state management? › Yes
✔ Add Vitest for Unit Testing? › Yes
✔ Add an End-to-End Testing Solution? › Playwright
✔ Add ESLint for code quality? › Yes
✔ Add Prettier for code formatting? › Yes

# 5. 进入项目并安装依赖
cd smart-admin-frontend
pnpm install

# 6. 启动开发服务器
pnpm dev
```

#### 1.2 项目配置文件
```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '/@/': resolve(__dirname, 'src')
    }
  },
  css: {
    preprocessorOptions: {
      less: {
        modifyVars: {
          // Ant Design Vue主题定制
          'primary-color': '#1890ff',
          'border-radius-base': '6px'
        },
        javascriptEnabled: true
      }
    }
  },
  server: {
    port: 8081,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:1024',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          antd: ['ant-design-vue'],
          utils: ['lodash-es', 'dayjs', 'axios']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  }
})
```

#### 1.3 TypeScript 配置
```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"],
      "/@/*": ["src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

### 步骤2: Composition API 核心开发 (6小时)

#### 2.1 响应式数据管理
```typescript
// 使用 Composition API 的响应式数据
import { ref, reactive, computed, watch, watchEffect } from 'vue'

// user-management.ts
export interface User {
  id: number
  name: string
  email: string
  department: string
  status: 'active' | 'inactive'
  createTime: string
}

export const useUserManagement = () => {
  // 响应式状态
  const users = ref<User[]>([])
  const loading = ref(false)
  const searchQuery = ref('')
  const selectedUsers = ref<number[]>([])

  // 响应式对象
  const filters = reactive({
    department: '',
    status: 'all' as 'all' | 'active' | 'inactive',
    dateRange: [] as string[]
  })

  // 计算属性
  const filteredUsers = computed(() => {
    return users.value.filter(user => {
      const matchesSearch = user.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                           user.email.toLowerCase().includes(searchQuery.value.toLowerCase())
      const matchesDepartment = !filters.department || user.department === filters.department
      const matchesStatus = filters.status === 'all' || user.status === filters.status

      return matchesSearch && matchesDepartment && matchesStatus
    })
  })

  const selectedUsersCount = computed(() => selectedUsers.value.length)

  // 监听器
  watch(searchQuery, (newQuery) => {
    console.log('搜索查询变化:', newQuery)
    // 可以在这里触发搜索API调用
  })

  watchEffect(() => {
    // 自动追踪依赖的响应式数据
    if (filters.department || filters.status !== 'all') {
      console.log('应用过滤器:', filters)
    }
  })

  // 方法
  const fetchUsers = async () => {
    loading.value = true
    try {
      const response = await userApi.getUsers(filters)
      users.value = response.data
    } catch (error) {
      console.error('获取用户列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const toggleUserSelection = (userId: number) => {
    const index = selectedUsers.value.indexOf(userId)
    if (index > -1) {
      selectedUsers.value.splice(index, 1)
    } else {
      selectedUsers.value.push(userId)
    }
  }

  const selectAllUsers = () => {
    selectedUsers.value = filteredUsers.value.map(user => user.id)
  }

  const clearSelection = () => {
    selectedUsers.value = []
  }

  // 生命周期
  onMounted(() => {
    fetchUsers()
  })

  return {
    // 状态
    users: readonly(users),
    loading: readonly(loading),
    searchQuery,
    filters,
    selectedUsers,

    // 计算属性
    filteredUsers,
    selectedUsersCount,

    // 方法
    fetchUsers,
    toggleUserSelection,
    selectAllUsers,
    clearSelection
  }
}
```

#### 2.2 自定义组合式函数 (Composables)
```typescript
// composables/useTable.ts
import { ref, computed, watch } from 'vue'

export interface TableColumn {
  title: string
  dataIndex: string
  key: string
  width?: number
  align?: 'left' | 'center' | 'right'
  sorter?: boolean
  filters?: Array<{ text: string; value: any }>
  scopedSlots?: boolean
}

export interface Pagination {
  current: number
  pageSize: number
  total: number
  showSizeChanger: boolean
  showQuickJumper: boolean
  showTotal: (total: number, range: [number, number]) => string
}

export const useTable = <T = any>(fetchData: (params: any) => Promise<{ data: T[], total: number }>) => {
  // 数据状态
  const data = ref<T[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 分页状态
  const pagination = ref<Pagination>({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
  })

  // 搜索和筛选
  const searchForm = ref<Record<string, any>>({})
  const sorter = ref<Record<string, any>>({})

  // 计算属性
  const hasData = computed(() => data.value.length > 0)
  const isEmpty = computed(() => !loading.value && data.value.length === 0)

  // 方法
  const loadData = async (params: Record<string, any> = {}) => {
    loading.value = true
    error.value = null

    try {
      const requestParams = {
        ...searchForm.value,
        ...sorter.value,
        pageNum: pagination.value.current,
        pageSize: pagination.value.pageSize,
        ...params
      }

      const response = await fetchData(requestParams)

      data.value = response.data
      pagination.value.total = response.total

    } catch (err: any) {
      error.value = err.message || '数据加载失败'
      console.error('表格数据加载失败:', err)
    } finally {
      loading.value = false
    }
  }

  const handleTableChange = (paginationInfo: any, filters: any, sorterInfo: any) => {
    pagination.value.current = paginationInfo.current
    pagination.value.pageSize = paginationInfo.pageSize

    if (sorterInfo.field) {
      sorter.value = {
        field: sorterInfo.field,
        order: sorterInfo.order
      }
    }

    loadData()
  }

  const handleSearch = (searchParams: Record<string, any>) => {
    searchForm.value = { ...searchParams }
    pagination.value.current = 1 // 重置到第一页
    loadData()
  }

  const handleReset = () => {
    searchForm.value = {}
    sorter.value = {}
    pagination.value.current = 1
    loadData()
  }

  const refresh = () => {
    loadData()
  }

  // 监听分页变化
  watch(() => pagination.value.current, () => {
    if (data.value.length > 0) {
      loadData()
    }
  })

  watch(() => pagination.value.pageSize, () => {
    if (data.value.length > 0) {
      loadData()
    }
  })

  return {
    // 状态
    data,
    loading,
    error,
    pagination,
    searchForm,

    // 计算属性
    hasData,
    isEmpty,

    // 方法
    loadData,
    handleTableChange,
    handleSearch,
    handleReset,
    refresh
  }
}
```

### 步骤3: 组件开发最佳实践 (6小时)

#### 3.1 通用组件设计
```vue
<!-- components/BaseTable.vue -->
<template>
  <div class="base-table">
    <!-- 搜索区域 -->
    <div v-if="showSearch" class="table-search">
      <a-form
        :model="searchForm"
        layout="inline"
        @finish="handleSearch"
      >
        <slot name="search-form" :form="searchForm" />
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">
              搜索
            </a-button>
            <a-button @click="handleReset">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 工具栏 -->
    <div v-if="showToolbar" class="table-toolbar">
      <div class="toolbar-left">
        <slot name="toolbar-left" />
      </div>
      <div class="toolbar-right">
        <slot name="toolbar-right" />
        <a-button v-if="showRefresh" @click="refresh" :loading="loading">
          刷新
        </a-button>
      </div>
    </div>

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      :loading="loading"
      :row-selection="rowSelection"
      :row-key="rowKey"
      :size="size"
      :scroll="scroll"
      @change="handleTableChange"
      v-bind="$attrs"
    >
      <!-- 动态插槽 -->
      <template
        v-for="column in columns"
        v-slot:[getSlotName(column.dataIndex)]="{ record, text, index }"
        :key="column.dataIndex"
      >
        <slot
          :name="column.dataIndex"
          :record="record"
          :text="text"
          :index="index"
        />
      </template>

      <!-- 空状态 -->
      <template #emptyText>
        <a-empty
          :description="emptyDescription"
          :image="emptyImage"
        >
          <template #description>
            <slot name="empty-description">
              {{ emptyDescription }}
            </slot>
          </template>
          <slot name="empty-action">
            <a-button type="primary" @click="handleEmptyAction" v-if="showEmptyAction">
              {{ emptyActionText }}
            </a-button>
          </slot>
        </a-empty>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'
import type { TableProps, ColumnType } from 'ant-design-vue'
import { useTable } from '@/composables/useTable'

// 组件属性接口
interface Props {
  columns: ColumnType[]
  fetchData: (params: any) => Promise<{ data: any[], total: number }>
  rowKey?: string | ((record: any) => string)
  showSearch?: boolean
  showToolbar?: boolean
  showRefresh?: boolean
  size?: 'small' | 'middle' | 'large'
  scroll?: { x?: number; y?: number }
  emptyDescription?: string
  emptyImage?: string
  emptyActionText?: string
  showEmptyAction?: boolean
  rowSelection?: TableProps['rowSelection']
  // 其他 a-table 属性
}

// 组件属性默认值
const props = withDefaults(defineProps<Props>(), {
  rowKey: 'id',
  showSearch: true,
  showToolbar: true,
  showRefresh: true,
  size: 'middle',
  emptyDescription: '暂无数据',
  emptyActionText: '重新加载',
  showEmptyAction: true
})

// 组件事件
interface Emits {
  (e: 'search', params: Record<string, any>): void
  (e: 'reset'): void
  (e: 'refresh'): void
  (e: 'selection-change', selectedRowKeys: any[], selectedRows: any[]): void
}

const emit = defineEmits<Emits>()

// 使用表格 composable
const {
  data,
  loading,
  pagination,
  searchForm,
  loadData,
  handleTableChange,
  handleSearch: handleSearchInternal,
  handleReset: handleResetInternal,
  refresh
} = useTable(props.fetchData)

// 插槽处理
const slots = useSlots()

const getSlotName = (dataIndex: string) => `bodyCell[${dataIndex}]`

// 事件处理
const handleSearch = (params: Record<string, any>) => {
  handleSearchInternal(params)
  emit('search', params)
}

const handleReset = () => {
  handleResetInternal()
  emit('reset')
}

const handleEmptyAction = () => {
  refresh()
  emit('refresh')
}

// 暴露给父组件的方法
defineExpose({
  refresh,
  loadData,
  getSelectedRows: () => data.value.filter(row =>
    props.rowSelection?.selectedRowKeys?.includes(row[props.rowKey as string])
  )
})
</script>

<style scoped lang="less">
.base-table {
  .table-search {
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 6px;
  }

  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}
</style>
```

#### 3.2 表单组件开发
```vue
<!-- components/BaseForm.vue -->
<template>
  <a-form
    ref="formRef"
    :model="formModel"
    :rules="rules"
    :layout="layout"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
    v-bind="$attrs"
    @finish="handleFinish"
    @finishFailed="handleFinishFailed"
  >
    <a-row :gutter="gutter">
      <a-col
        v-for="field in fields"
        :key="field.name"
        :span="field.span || 24"
      >
        <a-form-item
          :label="field.label"
          :name="field.name"
          :required="field.required"
          :colon="field.colon"
        >
          <!-- 输入框 -->
          <a-input
            v-if="field.type === 'input'"
            v-model:value="formModel[field.name]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :maxlength="field.maxlength"
            :show-count="field.showCount"
            v-bind="field.props"
          />

          <!-- 文本域 -->
          <a-textarea
            v-else-if="field.type === 'textarea'"
            v-model:value="formModel[field.name]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :rows="field.rows"
            :maxlength="field.maxlength"
            :show-count="field.showCount"
            v-bind="field.props"
          />

          <!-- 选择器 -->
          <a-select
            v-else-if="field.type === 'select'"
            v-model:value="formModel[field.name]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :allowClear="field.allowClear"
            :mode="field.mode"
            :options="field.options"
            v-bind="field.props"
          />

          <!-- 日期选择器 -->
          <a-date-picker
            v-else-if="field.type === 'date'"
            v-model:value="formModel[field.name]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :format="field.format"
            v-bind="field.props"
          />

          <!-- 日期范围选择器 -->
          <a-range-picker
            v-else-if="field.type === 'dateRange'"
            v-model:value="formModel[field.name]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :format="field.format"
            v-bind="field.props"
          />

          <!-- 数字输入框 -->
          <a-input-number
            v-else-if="field.type === 'number'"
            v-model:value="formModel[field.name]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :min="field.min"
            :max="field.max"
            :step="field.step"
            :precision="field.precision"
            v-bind="field.props"
          />

          <!-- 开关 -->
          <a-switch
            v-else-if="field.type === 'switch'"
            v-model:checked="formModel[field.name]"
            :disabled="field.disabled"
            v-bind="field.props"
          />

          <!-- 单选框组 -->
          <a-radio-group
            v-else-if="field.type === 'radio'"
            v-model:value="formModel[field.name]"
            :disabled="field.disabled"
            v-bind="field.props"
          >
            <a-radio
              v-for="option in field.options"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-radio>
          </a-radio-group>

          <!-- 复选框组 -->
          <a-checkbox-group
            v-else-if="field.type === 'checkbox'"
            v-model:value="formModel[field.name]"
            :disabled="field.disabled"
            v-bind="field.props"
          >
            <a-checkbox
              v-for="option in field.options"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-checkbox>
          </a-checkbox-group>

          <!-- 上传组件 -->
          <a-upload
            v-else-if="field.type === 'upload'"
            v-model:file-list="formModel[field.name]"
            :disabled="field.disabled"
            v-bind="field.props"
          >
            <a-button>
              <upload-outlined />
              点击上传
            </a-button>
          </a-upload>

          <!-- 自定义插槽 -->
          <slot
            v-else-if="field.type === 'slot'"
            :name="field.name"
            :field="field"
            :model="formModel"
          />

          <!-- 默认输入框 -->
          <a-input
            v-else
            v-model:value="formModel[field.name]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
          />
        </a-form-item>
      </a-col>
    </a-row>

    <!-- 表单操作按钮 -->
    <div v-if="showActions" class="form-actions">
      <slot name="actions" :form="formModel" :loading="loading">
        <a-space>
          <a-button @click="handleReset">
            重置
          </a-button>
          <a-button type="primary" html-type="submit" :loading="loading">
            {{ submitText }}
          </a-button>
        </a-space>
      </slot>
    </div>
  </a-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import type { FormInstance, Rule } from 'ant-design-vue'

// 表单字段接口
interface FormField {
  name: string
  label: string
  type: 'input' | 'textarea' | 'select' | 'date' | 'dateRange' | 'number' | 'switch' | 'radio' | 'checkbox' | 'upload' | 'slot'
  placeholder?: string
  required?: boolean
  disabled?: boolean
  span?: number
  rules?: Rule[]
  options?: Array<{ label: string; value: any }>
  props?: Record<string, any>
  // 特定字段属性
  rows?: number
  maxlength?: number
  showCount?: boolean
  allowClear?: boolean
  mode?: 'multiple' | 'tags'
  format?: string
  min?: number
  max?: number
  step?: number
  precision?: number
  colon?: boolean
}

// 组件属性接口
interface Props {
  fields: FormField[]
  modelValue: Record<string, any>
  rules?: Record<string, Rule[]>
  layout?: 'horizontal' | 'vertical' | 'inline'
  labelCol?: { span: number }
  wrapperCol?: { span: number }
  gutter?: number
  showActions?: boolean
  submitText?: string
  loading?: boolean
  colon?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  layout: 'horizontal',
  labelCol: () => ({ span: 6 }),
  wrapperCol: () => ({ span: 18 }),
  gutter: 16,
  showActions: true,
  submitText: '提交',
  loading: false,
  colon: true
})

// 组件事件
interface Emits {
  (e: 'update:modelValue', value: Record<string, any>): void
  (e: 'finish', values: Record<string, any>): void
  (e: 'finishFailed', errors: any): void
  (e: 'reset'): void
}

const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据模型
const formModel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 验证规则
const computedRules = computed(() => {
  const rules: Record<string, Rule[]> = {}

  props.fields.forEach(field => {
    if (field.rules) {
      rules[field.name] = field.rules
    } else if (field.required) {
      rules[field.name] = [{ required: true, message: `请输入${field.label}` }]
    }
  })

  return { ...rules, ...props.rules }
})

// 方法
const validate = async () => {
  try {
    await formRef.value?.validate()
    return true
  } catch (error) {
    return false
  }
}

const validateFields = async (fields?: string[]) => {
  try {
    await formRef.value?.validateFields(fields)
    return true
  } catch (error) {
    return false
  }
}

const resetFields = () => {
  formRef.value?.resetFields()
  emit('reset')
}

const clearValidate = () => {
  formRef.value?.clearValidate()
}

const setFieldsValue = (values: Record<string, any>) => {
  formRef.value?.setFieldsValue(values)
}

const getFieldsValue = () => {
  return formRef.value?.getFieldsValue()
}

// 事件处理
const handleFinish = (values: Record<string, any>) => {
  emit('finish', values)
}

const handleFinishFailed = (errors: any) => {
  emit('finishFailed', errors)
}

// 暴露给父组件的方法
defineExpose({
  formRef,
  validate,
  validateFields,
  resetFields,
  clearValidate,
  setFieldsValue,
  getFieldsValue
})
</script>

<style scoped lang="less">
.form-actions {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}
</style>
```

### 步骤4: 状态管理与Pinia使用 (4小时)

#### 4.1 Pinia Store 设计
```typescript
// stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginForm } from '@/types/user'
import { userApi } from '@/api/user'
import { message } from 'ant-design-vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const currentUser = ref<User | null>(null)
  const token = ref<string>('')
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const hasPermission = computed(() => (permission: string) =>
    permissions.value.includes(permission) || permissions.value.includes('*')
  )
  const hasRole = computed(() => (role: string) =>
    roles.value.includes(role)
  )
  const isAdmin = computed(() => hasRole.value('admin') || hasRole.value('super_admin'))

  // 方法
  const login = async (loginForm: LoginForm): Promise<boolean> => {
    loading.value = true
    try {
      const response = await userApi.login(loginForm)

      if (response.success) {
        const { user, token: accessToken, permissions: userPermissions, roles: userRoles } = response.data

        currentUser.value = user
        token.value = accessToken
        permissions.value = userPermissions
        roles.value = userRoles

        // 保存到本地存储
        localStorage.setItem('token', accessToken)
        localStorage.setItem('user', JSON.stringify(user))
        localStorage.setItem('permissions', JSON.stringify(userPermissions))
        localStorage.setItem('roles', JSON.stringify(userRoles))

        message.success('登录成功')
        return true
      } else {
        message.error(response.message || '登录失败')
        return false
      }
    } catch (error: any) {
      message.error(error.message || '登录失败')
      return false
    } finally {
      loading.value = false
    }
  }

  const logout = async () => {
    try {
      await userApi.logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      // 清除状态
      currentUser.value = null
      token.value = ''
      permissions.value = []
      roles.value = []

      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('permissions')
      localStorage.removeItem('roles')

      message.success('退出成功')
    }
  }

  const getUserInfo = async () => {
    if (!token.value) return false

    loading.value = true
    try {
      const response = await userApi.getUserInfo()
      if (response.success) {
        const { user, permissions: userPermissions, roles: userRoles } = response.data

        currentUser.value = user
        permissions.value = userPermissions
        roles.value = userRoles

        // 更新本地存储
        localStorage.setItem('user', JSON.stringify(user))
        localStorage.setItem('permissions', JSON.stringify(userPermissions))
        localStorage.setItem('roles', JSON.stringify(userRoles))

        return true
      }
      return false
    } catch (error: any) {
      message.error('获取用户信息失败')
      return false
    } finally {
      loading.value = false
    }
  }

  const updateUserInfo = (userInfo: Partial<User>) => {
    if (currentUser.value) {
      currentUser.value = { ...currentUser.value, ...userInfo }
      localStorage.setItem('user', JSON.stringify(currentUser.value))
    }
  }

  const initializeAuth = () => {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    const savedPermissions = localStorage.getItem('permissions')
    const savedRoles = localStorage.getItem('roles')

    if (savedToken && savedUser) {
      token.value = savedToken
      currentUser.value = JSON.parse(savedUser)
      permissions.value = savedPermissions ? JSON.parse(savedPermissions) : []
      roles.value = savedRoles ? JSON.parse(savedRoles) : []
    }
  }

  return {
    // 状态
    currentUser: readonly(currentUser),
    token: readonly(token),
    permissions: readonly(permissions),
    roles: readonly(roles),
    loading: readonly(loading),

    // 计算属性
    isLoggedIn,
    hasPermission,
    hasRole,
    isAdmin,

    // 方法
    login,
    logout,
    getUserInfo,
    updateUserInfo,
    initializeAuth
  }
})
```

#### 4.2 持久化插件
```typescript
// plugins/persistedState.ts
import { PiniaPlugin } from 'pinia'

type Storage = 'localStorage' | 'sessionStorage'

interface PersistedStateOptions {
  key?: string
  storage?: Storage
  paths?: string[]
  serializer?: {
    serialize: (state: any) => string
    deserialize: (value: string) => any
  }
}

export const createPersistedState = (options: PersistedStateOptions = {}): PiniaPlugin => {
  const {
    key = 'pinia',
    storage = 'localStorage',
    paths = [],
    serializer = {
      serialize: JSON.stringify,
      deserialize: JSON.parse
    }
  } = options

  return (context) => {
    const { store, options: { id } } = context

    // 存储键名
    const storageKey = `${key}-${id}`

    // 恢复状态
    const fromStorage = serializer.deserialize(
      (storage === 'localStorage' ? localStorage : sessionStorage).getItem(storageKey) || '{}'
    )

    if (paths.length === 0) {
      // 恢复整个状态
      store.$patch(fromStorage)
    } else {
      // 只恢复指定路径的状态
      paths.forEach(path => {
        if (fromStorage[path] !== undefined) {
          store.$state[path] = fromStorage[path]
        }
      })
    }

    // 订阅状态变化
    store.$subscribe((mutation, state) => {
      try {
        const toStore: any = paths.length === 0 ? state : {}

        if (paths.length === 0) {
          // 存储整个状态
          Object.assign(toStore, state)
        } else {
          // 只存储指定路径的状态
          paths.forEach(path => {
            toStore[path] = state[path]
          })
        }

        (storage === 'localStorage' ? localStorage : sessionStorage).setItem(
          storageKey,
          serializer.serialize(toStore)
        )
      } catch (error) {
        console.error(`Failed to persist state for store ${id}:`, error)
      }
    })
  }
}
```

### 步骤5: 路由和权限管理 (4小时)

#### 5.1 路由配置
```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { NProgress } from '@/utils/nprogress'

// 基础路由（不需要权限）
const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
      hidden: true
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false,
      hidden: true
    }
  },
  {
    path: '/',
    redirect: '/dashboard'
  }
]

// 异步路由（需要权限）
const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '仪表盘',
      icon: 'dashboard',
      requiresAuth: true
    },
    children: [
      {
        path: '',
        name: 'DashboardHome',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '首页',
          icon: 'home'
        }
      }
    ]
  },
  {
    path: '/system',
    name: 'System',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '系统管理',
      icon: 'setting',
      requiresAuth: true,
      roles: ['admin', 'super_admin']
    },
    children: [
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'user',
          permission: 'system:user:view'
        }
      },
      {
        path: 'role',
        name: 'RoleManagement',
        component: () => import('@/views/system/role/index.vue'),
        meta: {
          title: '角色管理',
          icon: 'team',
          permission: 'system:role:view'
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()

  const userStore = useUserStore()

  // 检查是否需要登录
  if (to.meta.requiresAuth !== false) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
  }

  // 检查角色权限
  if (to.meta.roles && Array.isArray(to.meta.roles)) {
    const hasRole = to.meta.roles.some(role => userStore.hasRole(role))
    if (!hasRole) {
      next('/403')
      return
    }
  }

  // 检查操作权限
  if (to.meta.permission) {
    if (!userStore.hasPermission(to.meta.permission)) {
      next('/403')
      return
    }
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
```

#### 5.2 动态路由生成
```typescript
// utils/permission.ts
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 根据用户权限过滤路由
 */
export const filterAsyncRoutes = (routes: RouteRecordRaw[], permissions: string[], roles: string[]): RouteRecordRaw[] => {
  const filteredRoutes: RouteRecordRaw[] = []

  routes.forEach(route => {
    const routeCopy = { ...route }

    // 检查路由权限
    if (hasRoutePermission(routeCopy, permissions, roles)) {
      // 递归过滤子路由
      if (routeCopy.children) {
        routeCopy.children = filterAsyncRoutes(routeCopy.children, permissions, roles)
      }

      filteredRoutes.push(routeCopy)
    }
  })

  return filteredRoutes
}

/**
 * 检查路由权限
 */
const hasRoutePermission = (route: RouteRecordRaw, permissions: string[], roles: string[]): boolean => {
  const meta = route.meta as any

  // 不需要权限的路由
  if (meta?.requiresAuth === false) {
    return true
  }

  // 检查角色权限
  if (meta?.roles && Array.isArray(meta.roles)) {
    const hasRole = meta.roles.some(role => roles.includes(role))
    if (!hasRole) {
      return false
    }
  }

  // 检查操作权限
  if (meta?.permission) {
    if (!permissions.includes(meta.permission) && !permissions.includes('*')) {
      return false
    }
  }

  return true
}

/**
 * 生成用户菜单
 */
export const generateMenus = (routes: RouteRecordRaw[]): MenuItem[] => {
  const menus: MenuItem[] = []

  routes.forEach(route => {
    const meta = route.meta as any

    // 跳过隐藏菜单
    if (meta?.hidden) {
      return
    }

    const menu: MenuItem = {
      path: route.path,
      name: route.name as string,
      title: meta?.title || route.name,
      icon: meta?.icon,
      children: []
    }

    // 处理子路由
    if (route.children && route.children.length > 0) {
      menu.children = generateMenus(route.children)
    }

    menus.push(menu)
  })

  return menus
}

interface MenuItem {
  path: string
  name: string
  title: string
  icon?: string
  children: MenuItem[]
}
```

### 步骤6: 性能优化和部署 (4小时)

#### 6.1 代码分割和懒加载
```typescript
// 路由懒加载
const routes = [
  {
    path: '/heavy-component',
    component: () => import('@/views/heavy-component/index.vue')
  }
]

// 组件懒加载
import { defineAsyncComponent } from 'vue'

const HeavyComponent = defineAsyncComponent({
  loader: () => import('@/components/HeavyComponent.vue'),
  loadingComponent: LoadingComponent,
  errorComponent: ErrorComponent,
  delay: 200,
  timeout: 10000
})

// 动态导入
const loadComponent = (componentName: string) => {
  return defineAsyncComponent(() =>
    import(`@/components/${componentName}.vue`)
  )
}
```

#### 6.2 性能监控
```typescript
// composables/usePerformance.ts
import { ref, onMounted, onUnmounted } from 'vue'

export const usePerformance = () => {
  const metrics = ref({
    loadTime: 0,
    renderTime: 0,
    apiTime: 0,
    errorCount: 0
  })

  const startMeasure = (name: string) => {
    performance.mark(`${name}-start`)
  }

  const endMeasure = (name: string) => {
    performance.mark(`${name}-end`)
    performance.measure(name, `${name}-start`, `${name}-end`)

    const measures = performance.getEntriesByName(name)
    if (measures.length > 0) {
      const duration = measures[measures.length - 1].duration

      // 更新指标
      switch (name) {
        case 'page-load':
          metrics.value.loadTime = duration
          break
        case 'component-render':
          metrics.value.renderTime = duration
          break
        case 'api-call':
          metrics.value.apiTime = duration
          break
      }

      // 上报到监控系统
      reportMetric(name, duration)
    }
  }

  const reportMetric = (name: string, value: number) => {
    // 发送到监控服务
    if (import.meta.env.PROD) {
      // 生产环境上报
      fetch('/api/metrics', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name,
          value,
          timestamp: Date.now(),
          url: window.location.href,
          userAgent: navigator.userAgent
        })
      })
    }
  }

  // 错误监控
  const handleError = (error: Error, context?: string) => {
    metrics.value.errorCount++

    console.error('Performance error:', error, context)

    // 错误上报
    if (import.meta.env.PROD) {
      fetch('/api/errors', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          message: error.message,
          stack: error.stack,
          context,
          timestamp: Date.now(),
          url: window.location.href,
          userAgent: navigator.userAgent
        })
      })
    }
  }

  // 页面性能监控
  const observePagePerformance = () => {
    // 页面加载时间
    window.addEventListener('load', () => {
      const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
      const loadTime = navigation.loadEventEnd - navigation.navigationStart
      metrics.value.loadTime = loadTime
      reportMetric('page-load-complete', loadTime)
    })

    // 长任务监控
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
          if (entry.duration > 100) {
            reportMetric('long-task', entry.duration)
          }
        })
      })

      observer.observe({ entryTypes: ['longtask'] })
    }
  }

  onMounted(() => {
    observePagePerformance()
  })

  return {
    metrics: readonly(metrics),
    startMeasure,
    endMeasure,
    handleError
  }
}
```

---

## ⚠️ 注意事项

### 🔒 安全提醒
- **XSS防护**: 使用Vue的模板语法避免XSS攻击
- **CSRF防护**: 配置CSRF令牌保护
- **输入验证**: 客户端和服务端双重验证
- **敏感信息**: 避免在前端存储敏感信息

### 📊 性能要求
- **首屏加载**: 首屏加载时间不超过3秒
- **路由切换**: 路由切换响应时间不超过500ms
- **包大小**: 生产环境包大小不超过1MB
- **内存使用**: 页面内存使用不超过100MB

### 🎯 最佳实践
- **组件设计**: 遵循单一职责原则，保持组件简洁
- **状态管理**: 合理使用Pinia，避免过度复杂化
- **代码分割**: 合理使用代码分割和懒加载
- **TypeScript**: 充分利用TypeScript的类型系统

---

## 📊 评估标准

### ⏱️ 开发效率要求
- **组件开发**: 单个通用组件4小时内完成
- **页面开发**: 单个业务页面8小时内完成
- **路由配置**: 模块路由配置2小时内完成
- **Bug修复**: 一般Bug1小时内修复

### 🎯 代码质量要求
- **TypeScript覆盖**: 100%TypeScript覆盖
- **ESLint检查**: 0ESLint错误和警告
- **组件复用**: 组件复用率达到80%
- **代码规范**: 100%符合团队代码规范

### 🔍 用户体验标准
- **响应速度**: 页面交互响应时间<200ms
- **加载体验**: 优雅的加载状态和错误处理
- **可访问性**: 符合WCAG 2.1 AA标准
- **兼容性**: 支持Chrome、Firefox、Safari、Edge最新版本

---

## 🔗 相关技能

### 📚 相关技能
- **[React开发技能](./react-development.skill)** - React框架开发
- **[Node.js开发技能](./nodejs-development.skill)** - Node.js后端开发
- **[移动端开发技能](./mobile-development.skill)** - 移动端应用开发
- **[UI设计技能](./ui-design.skill)** - 用户界面设计

### 🚀 进阶路径
1. **前端架构师**: 负责前端技术架构和选型
2. **全栈工程师**: 掌握前后端全栈开发
3. **前端性能专家**: 专注于前端性能优化
4. **开源贡献者**: 参与Vue生态开源项目

### 📖 参考资料
- **[Vue3官方文档](https://vuejs.org/)**
- **[TypeScript手册](https://www.typescriptlang.org/docs/)**
- **[Ant Design Vue](https://antdv.com/)**
- **[Vite文档](https://vitejs.dev/)**
- **[前端架构设计规范](../../../docs/repowiki/zh/content/前端架构/)**

---

**✅ 技能认证完成标准**:
- 能够独立设计和开发复杂的Vue3企业级应用
- 熟练掌握TypeScript和现代前端工程化工具
- 能够进行前端性能优化和架构设计
- 具备良好的代码规范和团队协作能力
- 通过技能评估测试（理论+实操+项目实战）