# Vue3 Composition API 最佳实践

## 📋 文档说明

本文档规定 SmartAdmin 项目中 Vue 3 Composition API 的开发规范和最佳实践，确保代码质量和可维护性。

**强制执行**：所有 Vue 3 组件开发必须严格遵守本规范。

---

## 🎯 Composition API 代码组织顺序

### 标准结构

```vue
<script setup>
// 1. 导入语句
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { xxxApi } from '/@/api/xxx-api';

// 2. Props 和 Emits 定义
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  data: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['update:visible', 'success']);

// 3. 响应式数据定义
const loading = ref(false);
const formRef = ref();
const formData = reactive({
  name: '',
  status: 1,
});

// 4. 计算属性
const modalTitle = computed(() => {
  return formData.id ? '编辑' : '新增';
});

const isDisabled = computed(() => {
  return props.data && props.data.viewMode;
});

// 5. 普通函数定义
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    status: 1,
  });
  formRef.value?.clearValidate();
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    if (formData.id) {
      await xxxApi.update(formData);
      message.success('更新成功');
    } else {
      await xxxApi.add(formData);
      message.success('新增成功');
    }

    emit('success');
    emit('update:visible', false);
  } catch (error) {
    if (error.errorFields) return;
    message.error(error.msg || '操作失败');
  } finally {
    loading.value = false;
  }
};

// 6. Watch 监听
watch(
  () => props.data,
  (newVal) => {
    if (newVal) {
      Object.assign(formData, newVal);
    } else {
      resetForm();
    }
  },
  { immediate: true, deep: true }
);

// 7. 生命周期钩子
onMounted(() => {
  console.log('Component mounted');
});
</script>
```

---

## ⚠️ 关键问题：声明顺序错误

### ❌ 错误示例（导致运行时错误）

```vue
<script setup>
import { ref, reactive, watch } from 'vue';

const props = defineProps({
  data: Object,
});

// ❌ 错误：watch 在 resetForm 声明之前
watch(
  () => props.data,
  (newVal) => {
    if (newVal) {
      // ...
    } else {
      resetForm();  // ❌ 错误：Cannot access 'resetForm' before initialization
    }
  },
  { immediate: true }  // ❌ 立即执行，此时 resetForm 还未声明
);

// 声明在后面
const resetForm = () => {
  // ...
};
</script>
```

**错误原因**：
- JavaScript 的 **Temporal Dead Zone（暂时性死区）**
- `watch` 的 `immediate: true` 会在组件初始化时立即执行
- 此时 `resetForm` 函数还未声明，导致 `ReferenceError`

### ✅ 正确示例

```vue
<script setup>
import { ref, reactive, watch } from 'vue';

const props = defineProps({
  data: Object,
});

// ✅ 正确：先声明函数
const resetForm = () => {
  console.log('resetForm called');
};

// ✅ 正确：后使用 watch
watch(
  () => props.data,
  (newVal) => {
    if (newVal) {
      // ...
    } else {
      resetForm();  // ✅ 正确：resetForm 已声明
    }
  },
  { immediate: true }  // ✅ 安全：resetForm 已存在
);
</script>
```

---

## 🔄 Watch 监听最佳实践

### 基本用法

```javascript
// 监听单个 ref
watch(count, (newVal, oldVal) => {
  console.log(`count changed from ${oldVal} to ${newVal}`);
});

// 监听多个源
watch([count, name], ([newCount, newName], [oldCount, oldName]) => {
  console.log('count or name changed');
});

// 监听 reactive 对象
watch(
  () => state.someProperty,
  (newVal, oldVal) => {
    console.log('state.someProperty changed');
  }
);

// 监听整个 reactive 对象
watch(
  state,
  (newVal, oldVal) => {
    console.log('state changed');
  },
  { deep: true }  // 深度监听
);
```

### Watch 选项

```javascript
watch(
  source,
  callback,
  {
    immediate: true,   // 立即执行（组件创建时）
    deep: true,        // 深度监听（对象/数组内部变化）
    flush: 'post',     // 回调触发时机：'pre' | 'post' | 'sync'
  }
);
```

### 常见场景

#### 场景1：监听 Props 并初始化表单

```javascript
// ✅ 推荐：先声明所有被调用的函数
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    status: 1,
  });
};

const loadFormData = (data) => {
  Object.assign(formData, data);
};

// 后使用 watch
watch(
  () => props.data,
  (newVal) => {
    if (newVal) {
      loadFormData(newVal);  // 编辑模式
    } else {
      resetForm();           // 新增模式
    }
  },
  { immediate: true, deep: true }
);
```

#### 场景2：监听多个依赖并执行查询

```javascript
// ✅ 推荐：先声明查询函数
const fetchData = async () => {
  loading.value = true;
  try {
    const result = await xxxApi.query({
      keyword: searchForm.keyword,
      status: searchForm.status,
    });
    dataList.value = result.data;
  } finally {
    loading.value = false;
  }
};

// 监听多个条件变化
watch(
  [() => searchForm.keyword, () => searchForm.status],
  () => {
    fetchData();
  }
);
```

#### 场景3：防抖处理

```javascript
import { debounce } from 'lodash-es';

// 创建防抖函数
const debouncedSearch = debounce(() => {
  fetchData();
}, 300);

// 监听输入变化
watch(
  () => searchForm.keyword,
  () => {
    debouncedSearch();
  }
);
```

---

## 📊 响应式数据管理

### ref vs reactive

```javascript
// ✅ ref：适用于基本类型和单个对象引用
const count = ref(0);
const name = ref('张三');
const user = ref(null);

// 访问/修改需要 .value
console.log(count.value);
count.value++;

// ✅ reactive：适用于复杂对象
const formData = reactive({
  name: '',
  age: 0,
  hobbies: [],
});

// 直接访问属性，无需 .value
console.log(formData.name);
formData.name = '李四';

// ❌ 不推荐：reactive 包裹数组（使用 ref 代替）
const list = reactive([]);  // 不推荐
const list = ref([]);       // 推荐
```

### 响应式数据修改

```javascript
// ✅ 正确：使用 Object.assign 修改 reactive 对象
Object.assign(formData, {
  name: '张三',
  age: 25,
});

// ✅ 正确：直接修改属性
formData.name = '张三';
formData.age = 25;

// ❌ 错误：直接赋值会失去响应性
formData = { name: '张三', age: 25 };  // ❌ 错误

// ✅ 正确：修改 ref 数组
list.value = [1, 2, 3];
list.value.push(4);

// ✅ 正确：修改 ref 对象
user.value = { name: '张三' };
```

### 计算属性

```javascript
// ✅ 推荐：只读计算属性
const fullName = computed(() => {
  return `${formData.firstName} ${formData.lastName}`;
});

// ✅ 可写计算属性（较少使用）
const fullName = computed({
  get() {
    return `${formData.firstName} ${formData.lastName}`;
  },
  set(value) {
    const names = value.split(' ');
    formData.firstName = names[0];
    formData.lastName = names[1];
  },
});

// ❌ 不推荐：在计算属性中修改其他状态
const badComputed = computed(() => {
  count.value++;  // ❌ 副作用，不推荐
  return someValue;
});
```

---

## 🔄 生命周期钩子

### 生命周期顺序

```javascript
import {
  onBeforeMount,
  onMounted,
  onBeforeUpdate,
  onUpdated,
  onBeforeUnmount,
  onUnmounted,
} from 'vue';

// 挂载前
onBeforeMount(() => {
  console.log('Component is about to mount');
});

// 挂载后（最常用）
onMounted(() => {
  console.log('Component mounted');
  // ✅ 推荐：在这里执行 DOM 操作、发起请求
  fetchData();
  initChart();
});

// 更新前
onBeforeUpdate(() => {
  console.log('Component is about to update');
});

// 更新后
onUpdated(() => {
  console.log('Component updated');
  // ⚠️ 注意：频繁触发，避免在此执行耗时操作
});

// 卸载前
onBeforeUnmount(() => {
  console.log('Component is about to unmount');
  // ✅ 推荐：清理定时器、事件监听
  clearInterval(timer);
  window.removeEventListener('resize', handleResize);
});

// 卸载后
onUnmounted(() => {
  console.log('Component unmounted');
});
```

### 常见使用场景

#### 场景1：初始化数据

```javascript
onMounted(async () => {
  loading.value = true;
  try {
    const result = await xxxApi.query();
    dataList.value = result.data;
  } catch (error) {
    message.error('加载失败');
  } finally {
    loading.value = false;
  }
});
```

#### 场景2：事件监听与清理

```javascript
const handleResize = () => {
  // 处理窗口大小变化
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});
```

#### 场景3：定时器清理

```javascript
let timer = null;

onMounted(() => {
  timer = setInterval(() => {
    // 定时任务
  }, 1000);
});

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});
```

---

## 🎨 组件通信

### Props 和 Emits

```vue
<script setup>
// ✅ Props 定义（带类型和默认值）
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  data: {
    type: Object,
    default: null,
  },
  options: {
    type: Array,
    default: () => [],  // ✅ 对象/数组默认值使用函数
  },
});

// ✅ Emits 定义
const emit = defineEmits(['update:visible', 'success', 'change']);

// 触发事件
const handleClose = () => {
  emit('update:visible', false);
};

const handleSuccess = (data) => {
  emit('success', data);
};
</script>

<template>
  <!-- 父组件使用 -->
  <!-- <MyModal
    v-model:visible="modalVisible"
    :data="currentData"
    @success="handleSuccess"
  /> -->
</template>
```

### Provide / Inject

```javascript
// 父组件 provide
import { provide, ref } from 'vue';

const theme = ref('dark');
const updateTheme = (newTheme) => {
  theme.value = newTheme;
};

provide('theme', theme);
provide('updateTheme', updateTheme);

// 子孙组件 inject
import { inject } from 'vue';

const theme = inject('theme');
const updateTheme = inject('updateTheme');

// 使用
console.log(theme.value);
updateTheme('light');
```

---

## 🔧 常见模式和技巧

### 模式1：表单提交

```javascript
const formRef = ref();
const formData = reactive({
  name: '',
  age: 0,
});

const formRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
  ],
};

const handleSubmit = async () => {
  try {
    // 1. 表单验证
    await formRef.value.validate();

    // 2. 显示加载
    loading.value = true;

    // 3. 提交数据
    if (formData.id) {
      await xxxApi.update(formData);
      message.success('更新成功');
    } else {
      await xxxApi.add(formData);
      message.success('新增成功');
    }

    // 4. 触发成功回调
    emit('success');

    // 5. 关闭弹窗
    emit('update:visible', false);
  } catch (error) {
    // 表单验证失败
    if (error.errorFields) return;

    // 接口错误
    message.error(error.msg || '操作失败');
  } finally {
    loading.value = false;
  }
};
```

### 模式2：列表查询

```javascript
const searchForm = reactive({
  keyword: '',
  status: undefined,
  pageNum: 1,
  pageSize: 10,
});

const tableData = ref([]);
const total = ref(0);
const loading = ref(false);

const fetchData = async () => {
  loading.value = true;
  try {
    const result = await xxxApi.queryPage(searchForm);
    tableData.value = result.data.list;
    total.value = result.data.total;
  } catch (error) {
    message.error('查询失败');
  } finally {
    loading.value = false;
  }
};

// 重置查询
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: undefined,
    pageNum: 1,
    pageSize: 10,
  });
  fetchData();
};

// 页码变化
const handlePageChange = (page, pageSize) => {
  searchForm.pageNum = page;
  searchForm.pageSize = pageSize;
  fetchData();
};

// 初始加载
onMounted(() => {
  fetchData();
});
```

### 模式3：弹窗控制

```javascript
const modalVisible = ref(false);
const modalData = ref(null);

// 新增
const handleAdd = () => {
  modalData.value = null;
  modalVisible.value = true;
};

// 编辑
const handleEdit = (record) => {
  modalData.value = { ...record };
  modalVisible.value = true;
};

// 查看
const handleView = (record) => {
  modalData.value = { ...record, viewMode: true };
  modalVisible.value = true;
};

// 成功回调
const handleModalSuccess = () => {
  modalVisible.value = false;
  fetchData();  // 刷新列表
};
```

---

## ⚠️ 常见错误和解决方案

### 错误1：在 watch immediate 中调用未声明的函数

```javascript
// ❌ 错误
watch(() => props.data, () => {
  resetForm();  // 错误：resetForm 未声明
}, { immediate: true });

const resetForm = () => {
  // ...
};

// ✅ 正确：先声明后使用
const resetForm = () => {
  // ...
};

watch(() => props.data, () => {
  resetForm();  // 正确：resetForm 已声明
}, { immediate: true });
```

### 错误2：修改 reactive 对象导致响应性丢失

```javascript
// ❌ 错误
let formData = reactive({ name: '' });
formData = { name: '张三' };  // 失去响应性

// ✅ 正确
const formData = reactive({ name: '' });
Object.assign(formData, { name: '张三' });
// 或
formData.name = '张三';
```

### 错误3：在 computed 中产生副作用

```javascript
// ❌ 错误
const badComputed = computed(() => {
  count.value++;  // 副作用
  return count.value;
});

// ✅ 正确
const goodComputed = computed(() => {
  return count.value * 2;  // 纯计算
});
```

### 错误4：忘记清理副作用

```javascript
// ❌ 错误：未清理定时器
onMounted(() => {
  setInterval(() => {
    // ...
  }, 1000);
});

// ✅ 正确
let timer = null;

onMounted(() => {
  timer = setInterval(() => {
    // ...
  }, 1000);
});

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
  }
});
```

### 错误5：直接修改 Props

```javascript
const props = defineProps({
  value: String,
});

// ❌ 错误
const handleChange = () => {
  props.value = 'new value';  // 错误：Props 只读
};

// ✅ 正确：使用 v-model
const emit = defineEmits(['update:value']);

const handleChange = () => {
  emit('update:value', 'new value');
};
```

---

## 📝 完整示例

### 示例：编辑弹窗组件

```vue
<template>
  <a-modal
    :visible="visible"
    :title="modalTitle"
    :width="800"
    :confirm-loading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :maskClosable="false"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="名称" name="name">
        <a-input
          v-model:value="formData.name"
          placeholder="请输入名称"
          :disabled="isViewMode"
        />
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status" :disabled="isViewMode">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import { xxxApi } from '/@/api/xxx-api';

// 1. Props 和 Emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  data: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['update:visible', 'success']);

// 2. 响应式数据
const formRef = ref();
const confirmLoading = ref(false);

const formData = reactive({
  id: undefined,
  name: '',
  status: 1,
});

// 3. 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: 100, message: '名称长度不能超过100个字符', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' },
  ],
};

// 4. 计算属性
const modalTitle = computed(() => {
  if (isViewMode.value) {
    return '查看';
  }
  return formData.id ? '编辑' : '新增';
});

const isViewMode = computed(() => {
  return props.data && props.data.viewMode;
});

// 5. 方法 - 必须在 watch 之前声明（如果 watch 使用 immediate）
const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    name: '',
    status: 1,
  });
  formRef.value?.clearValidate();
};

const loadFormData = (data) => {
  Object.assign(formData, {
    id: data.id,
    name: data.name,
    status: data.status,
  });
};

const handleSubmit = async () => {
  if (isViewMode.value) {
    handleCancel();
    return;
  }

  try {
    await formRef.value.validate();
    confirmLoading.value = true;

    if (formData.id) {
      await xxxApi.update(formData);
      message.success('更新成功');
    } else {
      await xxxApi.add(formData);
      message.success('新增成功');
    }

    emit('success');
    handleCancel();
  } catch (error) {
    if (error.errorFields) {
      return;
    }
    message.error(error.msg || '操作失败');
  } finally {
    confirmLoading.value = false;
  }
};

const handleCancel = () => {
  emit('update:visible', false);
  resetForm();
};

// 6. Watch 监听 - 使用 immediate 时，必须确保调用的函数已声明
watch(
  () => props.data,
  (newVal) => {
    if (newVal) {
      loadFormData(newVal);
    } else {
      resetForm();
    }
  },
  { immediate: true, deep: true }
);
</script>

<style lang="less" scoped>
// 样式
</style>
```

---

## 🎓 最佳实践总结

### 强制规范
1. **声明顺序**：严格遵守"导入 → Props/Emits → 数据 → 计算属性 → 函数 → Watch → 生命周期"顺序
2. **immediate watch**：使用 `immediate: true` 时，确保调用的函数已声明
3. **响应式修改**：使用 `Object.assign()` 修改 reactive 对象，使用 `.value` 修改 ref
4. **副作用清理**：在 `onBeforeUnmount` 中清理定时器、事件监听等副作用
5. **Props 只读**：永远不要直接修改 Props，使用 emit 通知父组件

### 推荐实践
1. **命名规范**：使用语义化的变量名，Boolean 变量以 is/has/can 开头
2. **计算属性**：优先使用 computed 而不是 watch 来派生数据
3. **函数拆分**：将复杂逻辑拆分为多个小函数，提高可读性
4. **错误处理**：统一使用 try-catch 处理异步错误
5. **类型安全**：在 TypeScript 项目中，为 Props 和数据定义类型

### 性能优化
1. **按需引入**：只导入需要的 Composition API 函数
2. **避免深度监听**：非必要不使用 `deep: true`
3. **防抖节流**：对高频触发的操作使用 debounce/throttle
4. **计算属性缓存**：利用 computed 的缓存特性避免重复计算
5. **异步组件**：使用 defineAsyncComponent 实现组件懒加载

---

## 📖 相关文档

- [前端组件开发规范](./前端组件开发规范.md)
- [权限命名规范](./权限命名规范.md)
- [API路径规范](./API路径规范.md)
- [AI辅助开发检查清单](../AI_DEV_CHECKLIST.md)

---

**版本**：v1.0
**更新时间**：2025-11-11
**维护人**：开发团队
