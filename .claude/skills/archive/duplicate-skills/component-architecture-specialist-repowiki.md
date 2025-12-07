# 组件架构专家 (Repowiki标准版)

## 🎯 技能定位
**核心职责**: 基于 `.qoder/repowiki` 中的《组件架构详解》规范，确保IOE-DREAM项目前端组件严格遵循原子设计理论，构建高复用、可维护的组件系统

**⚡ 技能等级**: ★★★★★ (组件架构专家)
**🎯 适用场景**: 组件设计规范、原子设计实施、组件库建设、组件性能优化、组件文档管理
**📊 技能覆盖**: 原子设计理论 | 组件分层架构 | 组件复用策略 | 组件性能优化 | 组件文档体系

---

## 📋 技能概述 (基于Repowiki规范)

### **核心专长 (基于.qoder/repowiki/zh/content/前端架构/组件架构/)**
- **原子设计守护**: 严格确保Atoms→Molecules→Organisms→Templates→Pages设计原则
- **组件分层架构**: 基于功能域的组件分层和依赖管理
- **组件复用优化**: 最大化组件复用性，减少重复代码
- **组件性能保障**: 组件懒加载、渲染优化、内存管理
- **组件文档完善**: 自动化组件文档生成和维护

### **解决能力**
- **组件设计合规性**: 100%符合repowiki原子设计规范
- **组件架构优化**: 科学的组件分层和依赖关系
- **组件性能问题**: 组件渲染性能和内存使用优化
- **组件复用率提升**: 提高组件复用性，减少开发成本
- **组件维护成本**: 降低组件维护难度和成本

---

## 🏗️ Repowiki组件架构规范

### **原子设计五层架构**

#### **第一层：Atoms (原子组件)**
- **定义**: 最基础的UI元素，无法再分割
- **示例**: Button、Input、Icon、Label、Avatar
- **特点**: 单一功能、高度复用、无业务逻辑
- **位置**: `src/components/base/atoms/`

#### **第二层：Molecules (分子组件)**
- **定义**: 由多个原子组件组合而成
- **示例**: SearchBox(Input+Button)、FormItem(Label+Input+Error)
- **特点**: 简单交互、基础功能、组合原子
- **位置**: `src/components/base/molecules/`

#### **第三层：Organisms (有机体组件)**
- **定义**: 由分子和原子组成的复杂UI结构
- **示例**: Header、Sidebar、DataTable、Form
- **特点**: 复杂功能、业务逻辑、多组件协作
- **位置**: `src/components/base/organisms/`

#### **第四层：Templates (模板组件)**
- **定义**: 页面布局框架，定义内容结构
- **示例**: ArticleLayout、DashboardLayout、ModalLayout
- **特点**: 布局结构、插槽设计、内容占位
- **位置**: `src/components/layout/templates/`

#### **第五层：Pages (页面组件)**
- **定义**: 具体的页面实例，填充模板内容
- **示例**: UserListPage、DetailPage、SettingsPage
- **特点**: 业务数据、路由集成、完整功能
- **位置**: `src/views/pages/`

### **组件目录结构规范**
```
src/components/
├── base/                   # 基础组件库
│   ├── atoms/             # 原子组件
│   │   ├── AButton/
│   │   ├── AInput/
│   │   ├── AIcon/
│   │   └── index.ts       # 统一导出
│   ├── molecules/         # 分子组件
│   │   ├── MSearchBox/
│   │   ├── MFormItem/
│   │   └── index.ts
│   └── organisms/         # 有机体组件
│       ├── OHeader/
│       ├── ODataTable/
│       └── index.ts
├── business/              # 业务组件
│   ├── UserCard/          # 用户卡片组件
│   ├── ProductList/       # 产品列表组件
│   └── index.ts
└── layout/                # 布局组件
    ├── AppLayout/         # 应用布局
    ├── PageLayout/        # 页面布局
    └── index.ts
```

---

## 🛠️ 核心工作流程 (基于Repowiki)

### **Phase 1: 组件架构合规性诊断**
```bash
# 检查原子设计规范合规性
./scripts/check-atomic-design-compliance.sh

# 检测组件依赖关系
./scripts/analyze-component-dependencies.sh

# 验证组件命名规范
./scripts/validate-component-naming.sh

# 检查组件复用率
./scripts/calculate-component-reusability.sh
```

### **Phase 2: 组件架构优化**
```bash
# 组件分层优化
./scripts/optimize-component-layering.sh

# 组件依赖解耦
./scripts/decouple-component-dependencies.sh

# 组件性能优化
./scripts/optimize-component-performance.sh

# 组件文档生成
./scripts/generate-component-documentation.sh
```

### **Phase 3: 组件质量保障**
```bash
# 组件单元测试
npm run test:components

# 组件集成测试
npm run test:component-integration

# 组件性能测试
npm run test:component-performance

# 组件兼容性测试
npm run test:component-compatibility
```

---

## 🔍 组件架构合规性检查清单 (基于Repowiki)

### **✅ 强制性规范 (必须100%遵循)**

#### **原子设计原则**
- [ ] 严格遵循Atoms→Molecules→Organisms分层
- [ ] 原子组件无业务逻辑，纯UI展示
- [ ] 分子组件组合原子，实现基础功能
- [ ] 有机体组件处理复杂业务逻辑
- [ ] 组件依赖方向：Pages→Templates→Organisms→Molecules→Atoms

#### **组件设计规范**
- [ ] 单一职责原则，组件功能明确单一
- [ ] Props类型定义完整，禁止any类型
- [ ] Emits事件定义规范，命名清晰
- [ ] 组件命名使用PascalCase，语义明确
- [ ] 文件命名使用kebab-case.vue

#### **组件接口规范**
- [ ] Props定义使用TypeScript interface
- [ ] 提供合理的默认值和验证规则
- [ ] Emits事件命名使用动词或动名词
- [ ] Slots插槽定义清晰，提供默认内容
- [ ] 组件状态管理使用响应式API

### **⚠️ 推荐性规范**

#### **组件性能规范**
- [ ] 大型组件使用异步加载 (defineAsyncComponent)
- [ ] 列表组件使用虚拟滚动优化
- [ ] 图片组件使用懒加载
- [ ] 避免不必要的组件重新渲染
- [ ] 合理使用computed和watch

#### **组件开发规范**
- [ ] 组件样式使用scoped CSS Modules
- [ ] 提供完整的组件文档和示例
- [ ] 编写组件单元测试
- [ ] 使用Storybook进行组件展示
- [ ] 遵循无障碍设计(a11y)规范

---

## 🚀 组件设计最佳实践

### **原子组件设计示例**
```vue
<!-- components/base/atoms/AButton/AButton.vue -->
<template>
  <button
    :class="buttonClasses"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <AIcon v-if="loading" name="loading" class="btn-loading" />
    <AIcon v-if="icon && !loading" :name="icon" :class="iconClasses" />
    <span v-if="$slots.default" class="btn-text">
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AIcon from '../AIcon/AIcon.vue'

interface Props {
  type?: 'primary' | 'secondary' | 'danger' | 'ghost'
  size?: 'small' | 'medium' | 'large'
  disabled?: boolean
  loading?: boolean
  icon?: string
  block?: boolean
}

interface Emits {
  click: [event: MouseEvent]
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'medium',
  disabled: false,
  loading: false,
  block: false
})

const emit = defineEmits<Emits>()

const buttonClasses = computed(() => [
  'a-button',
  `a-button--${props.type}`,
  `a-button--${props.size}`,
  {
    'a-button--disabled': props.disabled,
    'a-button--loading': props.loading,
    'a-button--block': props.block
  }
])

const iconClasses = computed(() => [
  'btn-icon',
  {
    'btn-icon--left': props.icon && props.$slots.default
  }
])

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped lang="scss">
.a-button {
  // 基础样式
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;

  // 变体样式
  &--primary {
    background-color: #1890ff;
    color: white;
    &:hover {
      background-color: #40a9ff;
    }
  }

  &--secondary {
    background-color: white;
    color: #1890ff;
    border: 1px solid #1890ff;
    &:hover {
      background-color: #f0f9ff;
    }
  }

  // 尺寸样式
  &--small {
    padding: 4px 8px;
    font-size: 12px;
  }

  &--medium {
    padding: 8px 16px;
    font-size: 14px;
  }

  &--large {
    padding: 12px 24px;
    font-size: 16px;
  }

  // 状态样式
  &--disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &--loading {
    cursor: wait;
  }

  &--block {
    width: 100%;
  }
}
</style>
```

### **分子组件设计示例**
```vue
<!-- components/base/molecules/MSearchBox/MSearchBox.vue -->
<template>
  <div class="m-search-box">
    <AInput
      v-model="searchQuery"
      :placeholder="placeholder"
      :size="size"
      @keyup.enter="handleSearch"
    >
      <template #suffix>
        <AButton
          type="ghost"
          :size="size"
          :loading="loading"
          @click="handleSearch"
        >
          <AIcon name="search" />
        </AButton>
      </template>
    </AInput>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import AInput from '../atoms/AInput/AInput.vue'
import AButton from '../atoms/AButton/AButton.vue'
import AIcon from '../atoms/AIcon/AIcon.vue'

interface Props {
  placeholder?: string
  size?: 'small' | 'medium' | 'large'
  loading?: boolean
  modelValue?: string
}

interface Emits {
  'update:modelValue': [value: string]
  search: [query: string]
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请输入搜索内容',
  size: 'medium',
  loading: false,
  modelValue: ''
})

const emit = defineEmits<Emits>()

const searchQuery = ref(props.modelValue)

// 双向绑定
watch(searchQuery, (newValue) => {
  emit('update:modelValue', newValue)
})

watch(() => props.modelValue, (newValue) => {
  searchQuery.value = newValue
})

const handleSearch = () => {
  emit('search', searchQuery.value)
}
</script>

<style scoped lang="scss">
.m-search-box {
  display: flex;
  align-items: center;

  .a-input {
    flex: 1;
  }
}
</style>
```

### **有机体组件设计示例**
```vue
<!-- components/base/organisms/ODataTable/ODataTable.vue -->
<template>
  <div class="o-data-table">
    <!-- 表格工具栏 -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <slot name="toolbar-left">
          <AButton type="primary" @click="handleAdd">
            <AIcon name="plus" />
            新增
          </AButton>
        </slot>
      </div>
      <div class="toolbar-right">
        <slot name="toolbar-right">
          <MSearchBox
            v-model="searchQuery"
            placeholder="搜索表格内容"
            @search="handleSearch"
          />
        </slot>
      </div>
    </div>

    <!-- 数据表格 -->
    <ATable
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="pagination"
      :row-selection="rowSelection"
      @change="handleTableChange"
    >
      <!-- 动态插槽 -->
      <template
        v-for="column in columns"
        :key="column.dataIndex"
        #[column.dataIndex]="{ record }"
      >
        <slot :name="column.dataIndex" :record="record">
          {{ record[column.dataIndex] }}
        </slot>
      </template>
    </ATable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TableProps } from 'ant-design-vue'
import AButton from '../atoms/AButton/AButton.vue'
import AIcon from '../atoms/AIcon/AIcon.vue'
import MSearchBox from '../molecules/MSearchBox/MSearchBox.vue'
import ATable from '../atoms/ATable/ATable.vue'

interface ColumnType extends TableProps['columns'][0] {
  dataIndex: string
  title: string
}

interface Props {
  columns: ColumnType[]
  dataSource: any[]
  loading?: boolean
  pagination?: any
  rowSelection?: any
}

interface Emits {
  add: []
  search: [query: string]
  'table-change': [pagination: any, filters: any, sorter: any]
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  pagination: {},
  rowSelection: null
})

const emit = defineEmits<Emits>()

const searchQuery = ref('')

const handleAdd = () => {
  emit('add')
}

const handleSearch = (query: string) => {
  searchQuery.value = query
  emit('search', query)
}

const handleTableChange = (pagination: any, filters: any, sorter: any) => {
  emit('table-change', pagination, filters, sorter)
}
</script>

<style scoped lang="scss">
.o-data-table {
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;

    .toolbar-left {
      display: flex;
      gap: 8px;
    }

    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }
}
</style>
```

---

## 📊 组件质量评估标准

### **组件架构评分**
| 维度 | 权重 | 评分标准 |
|------|------|----------|
| 原子设计合规性 | 30% | 分层架构清晰，依赖关系正确 |
| 组件复用性 | 25% | 组件复用率高，接口设计合理 |
| 组件性能 | 20% | 渲染性能优秀，内存使用合理 |
| 代码质量 | 15% | TypeScript类型安全，代码规范 |
| 文档完整性 | 10% | 组件文档完善，示例清晰 |

### **质量等级**
- **A级 (90-100分)**: 完全符合repowiki组件架构规范
- **B级 (80-89分)**: 基本合规，存在轻微优化空间
- **C级 (70-79分)**: 部分合规，需要重点改进
- **D级 (60-69分)**: 组件设计混乱，需要重构
- **E级 (0-59分)**: 严重违反组件架构规范

---

## 🎯 使用指南

### **何时调用**
- 组件架构设计和技术选型时
- 组件系统重构和优化时
- 组件性能问题诊断和优化时
- 组件库建设和标准化时
- 新组件开发规范制定时

### **调用方式**
```bash
# 基于repowiki的组件架构专家
Skill("component-architecture-specialist-repowiki")

# 将立即执行：
# 1. 基于.qoder/repowiki组件架构规范检查
# 2. 原子设计原则合规性验证
# 3. 组件依赖关系分析和优化
# 4. 组件性能优化建议和实施
```

### **预期结果**
- 100%符合`.qoder/repowiki`原子设计规范
- 科学的组件分层和依赖关系
- 高复用、高性能的组件系统
- 完善的组件文档和开发规范

---

**🏆 技能等级**: 组件架构专家 (★★★★★)
**⏰ 预期效果**: 基于249个repowiki权威文档，确保IOE-DREAM组件系统100%符合原子设计标准
**🎯 核心价值**: 原子设计守护，组件系统优化，开发效率提升