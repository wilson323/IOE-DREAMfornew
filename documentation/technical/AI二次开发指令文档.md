# SmartAdmin AI 二次开发指令文档

> **文档目的**: 让AI根据需求描述自动生成符合项目风格的完整功能模块  
> **项目名称**: SmartAdmin 3.X  
> **技术栈**: Vue 3 + Ant Design Vue 4 + Vite 5 + Pinia  
> **开发者**: 1024创新实验室  
> **文档版本**: 2.0.0  
> **更新日期**: 2025-11-03

---

## 🎯 使用说明

**给AI的开发指令格式：**

```
请基于 SmartAdmin 项目为我开发一个【模块名称】功能，需求如下：
1. 功能描述：管理XXX信息，包括增删改查
2. 字段列表：
   - 字段1：类型、是否必填、说明
   - 字段2：类型、是否必填、说明
3. 操作权限：添加、编辑、删除、查询、导出
```

**AI将自动生成：**
- ✅ API 接口文件（`src/api/业务模块/xxx-api.js`）
- ✅ 常量定义文件（`src/constants/业务模块/xxx-const.js`）
- ✅ 列表页面组件（`src/views/业务模块/xxx/index.vue`）
- ✅ 表单弹窗组件（`src/views/业务模块/xxx/components/xxx-form-modal.vue`）
- ✅ 所有代码完全符合项目规范，可直接使用

---

## 📋 目录

1. [快速开始 - 开发示例](#1-快速开发示例)
2. [标准文件模板](#2-标准文件模板)
3. [项目核心约定](#3-项目核心约定)
4. [常用功能模式](#4-常用功能模式)
5. [代码生成检查清单](#5-代码生成检查清单)
6. [复杂页面开发指南](#6-复杂页面开发指南)

---

## 1. 快速开发示例

### 示例需求

```
请基于 SmartAdmin 项目为我开发一个【产品管理】功能，需求如下：

1. 功能描述：
   - 管理公司产品信息
   - 支持产品的增删改查
   - 支持按产品名称、分类搜索
   - 支持产品上下架

2. 字段列表：
   - 产品名称：字符串，必填，2-50字符
   - 产品分类：下拉选择，必填
   - 产品价格：数字，必填，大于0
   - 产品库存：整数，必填，大于等于0
   - 产品状态：枚举（上架/下架），必填
   - 产品描述：富文本，非必填
   - 产品图片：图片上传，必填

3. 操作权限：
   - business:product:add（添加产品）
   - business:product:update（编辑产品）
   - business:product:delete（删除产品）
   - business:product:query（查询产品）
   - business:product:updateStatus（更新状态）
```

### AI应生成的文件结构

```
src/
├── api/
│   └── business/
│       └── product/
│           └── product-api.js          # API接口定义
├── constants/
│   └── business/
│       └── product/
│           └── product-const.js        # 常量枚举定义
└── views/
    └── business/
        └── product/
            ├── index.vue                # 产品列表页
            └── components/
                └── product-form-modal/
                    └── index.vue        # 产品表单弹窗
```

---

## 2. 标准文件模板

### 2.1 API 接口文件模板

**文件位置**: `src/api/{模块分类}/{模块名}/{模块名}-api.js`

**模板代码**:

```javascript
/*
 * {模块中文名} API
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      {当前日期}
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { getRequest, postRequest } from '/@/lib/axios';

export const {模块名}Api = {
  /**
   * 分页查询 @author {作者}
   */
  query{模块名}: (params) => {
    return postRequest('/{模块路径}/query', params);
  },

  /**
   * 查询所有 @author {作者}
   */
  queryAll: () => {
    return getRequest('/{模块路径}/queryAll');
  },

  /**
   * 添加 @author {作者}
   */
  add{模块名}: (params) => {
    return postRequest('/{模块路径}/add', params);
  },

  /**
   * 更新 @author {作者}
   */
  update{模块名}: (params) => {
    return postRequest('/{模块路径}/update', params);
  },

  /**
   * 删除 @author {作者}
   */
  delete{模块名}: (id) => {
    return getRequest(`/{模块路径}/delete/${id}`);
  },

  /**
   * 批量删除 @author {作者}
   */
  batchDelete{模块名}: (idList) => {
    return postRequest('/{模块路径}/batchDelete', idList);
  },
};
```

**实际示例** (产品管理):

```javascript
/*
 * 产品管理 API
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2025-11-03 10:00:00
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { getRequest, postRequest } from '/@/lib/axios';

export const productApi = {
  /**
   * 分页查询产品 @author 卓大
   */
  queryProduct: (params) => {
    return postRequest('/product/query', params);
  },

  /**
   * 查询所有产品 @author 卓大
   */
  queryAll: () => {
    return getRequest('/product/queryAll');
  },

  /**
   * 添加产品 @author 卓大
   */
  addProduct: (params) => {
    return postRequest('/product/add', params);
  },

  /**
   * 更新产品 @author 卓大
   */
  updateProduct: (params) => {
    return postRequest('/product/update', params);
  },

  /**
   * 删除产品 @author 卓大
   */
  deleteProduct: (productId) => {
    return getRequest(`/product/delete/${productId}`);
  },

  /**
   * 批量删除产品 @author 卓大
   */
  batchDeleteProduct: (productIdList) => {
    return postRequest('/product/batchDelete', productIdList);
  },

  /**
   * 更新产品状态 @author 卓大
   */
  updateProductStatus: (productId, status) => {
    return postRequest('/product/updateStatus', { productId, status });
  },
};
```

---

### 2.2 常量定义文件模板

**文件位置**: `src/constants/{模块分类}/{模块名}-const.js`

**模板代码**:

```javascript
/*
 * {模块中文名}常量
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      {当前日期}
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

// 状态枚举
export const {模块名}_STATUS_ENUM = {
  ENUM_KEY_1: {
    value: 1,
    desc: '描述1',
  },
  ENUM_KEY_2: {
    value: 2,
    desc: '描述2',
  },
};

// 其他枚举...

export default {
  {模块名}_STATUS_ENUM,
};
```

**实际示例** (产品管理):

```javascript
/*
 * 产品管理常量
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2025-11-03 10:00:00
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

// 产品状态枚举
export const PRODUCT_STATUS_ENUM = {
  ON_SALE: {
    value: 1,
    desc: '上架',
    color: 'success',
  },
  OFF_SALE: {
    value: 0,
    desc: '下架',
    color: 'default',
  },
};

// 产品分类枚举
export const PRODUCT_CATEGORY_ENUM = {
  ELECTRONICS: {
    value: 1,
    desc: '电子产品',
  },
  CLOTHING: {
    value: 2,
    desc: '服装',
  },
  FOOD: {
    value: 3,
    desc: '食品',
  },
};

export default {
  PRODUCT_STATUS_ENUM,
  PRODUCT_CATEGORY_ENUM,
};
```

---

### 2.3 列表页面组件模板

**文件位置**: `src/views/{模块分类}/{模块名}/index.vue`

**完整模板代码**:

```vue
<!--
  * {模块中文名}列表
  *
  * @Author:    1024创新实验室-主任：卓大
  * @Date:      {当前日期}
  * @Wechat:    zhuda1024
  * @Email:     lab1024@163.com
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <a-card class="{模块名}-container">
    <!-- ========== 查询表单 ========== -->
    <a-form class="smart-query-form" layout="inline">
      <a-form-item label="关键字">
        <a-input
          v-model:value.trim="queryParams.keyword"
          placeholder="请输入关键字"
          style="width: 200px"
          @press-enter="onSearch"
        />
      </a-form-item>

      <!-- 更多查询条件... -->

      <a-form-item>
        <a-button type="primary" @click="onSearch">
          <template #icon><SearchOutlined /></template>
          查询
        </a-button>
        <a-button @click="onReset" class="smart-margin-left10">
          <template #icon><ReloadOutlined /></template>
          重置
        </a-button>
      </a-form-item>
    </a-form>

    <!-- ========== 操作按钮 ========== -->
    <div class="smart-query-table-btn-group">
      <a-button type="primary" @click="showFormModal()" v-privilege="'{模块}:add'">
        <template #icon><PlusOutlined /></template>
        新建
      </a-button>
      <a-button @click="batchDelete" v-privilege="'{模块}:delete'" :disabled="selectedRowKeys.length === 0">
        <template #icon><DeleteOutlined /></template>
        批量删除
      </a-button>
    </div>

    <!-- ========== 数据表格 ========== -->
    <a-table
      :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      :columns="columns"
      :data-source="dataList"
      :loading="loading"
      :pagination="false"
      :scroll="{ x: 1200 }"
      row-key="{主键字段}"
      size="small"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <!-- 状态列 -->
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="$smartEnumPlugin.getEnumByValue('STATUS_ENUM', record.status)?.color">
            {{ $smartEnumPlugin.getDescByValue('STATUS_ENUM', record.status) }}
          </a-tag>
        </template>

        <!-- 操作列 -->
        <template v-else-if="column.dataIndex === 'operate'">
          <div class="smart-table-operate">
            <a-button type="link" size="small" @click="showFormModal(record)" v-privilege="'{模块}:update'">
              编辑
            </a-button>
            <a-button type="link" size="small" @click="deleteRecord(record)" v-privilege="'{模块}:delete'">
              删除
            </a-button>
          </div>
        </template>
      </template>
    </a-table>

    <!-- ========== 分页 ========== -->
    <div class="smart-query-table-page">
      <a-pagination
        v-model:current="queryParams.pageNum"
        v-model:pageSize="queryParams.pageSize"
        :total="total"
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        :showTotal="showTableTotal"
        showSizeChanger
        showQuickJumper
        @change="onPageChange"
      />
    </div>

    <!-- ========== 表单弹窗 ========== -->
    <{模块名}FormModal ref="formModalRef" @refresh="queryData" />
  </a-card>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { SearchOutlined, ReloadOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import { {模块名}Api } from '/@/api/{模块分类}/{模块名}-api';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS, showTableTotal } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import {模块名}FormModal from './components/{模块名}-form-modal/index.vue';

  // =========== 查询参数 =============
  const queryParams = reactive({
    pageNum: 1,
    pageSize: PAGE_SIZE,
    keyword: '',
    // 其他查询字段...
  });

  // =========== 表格数据 =============
  const dataList = ref([]);
  const loading = ref(false);
  const total = ref(0);
  const selectedRowKeys = ref([]);

  // =========== 表格列配置 =============
  const columns = ref([
    {
      title: '列名',
      dataIndex: '字段名',
      width: 150,
    },
    // 更多列...
    {
      title: '操作',
      dataIndex: 'operate',
      width: 150,
      fixed: 'right',
    },
  ]);

  // =========== 查询数据 =============
  async function queryData() {
    try {
      loading.value = true;
      const res = await {模块名}Api.query{模块名}(queryParams);
      dataList.value = res.data.list;
      total.value = res.data.total;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  }

  // =========== 搜索 =============
  function onSearch() {
    queryParams.pageNum = 1;
    queryData();
  }

  // =========== 重置 =============
  function onReset() {
    Object.assign(queryParams, {
      pageNum: 1,
      pageSize: PAGE_SIZE,
      keyword: '',
      // 重置其他查询字段...
    });
    queryData();
  }

  // =========== 分页 =============
  function onPageChange(pageNum, pageSize) {
    queryParams.pageNum = pageNum;
    queryParams.pageSize = pageSize;
    queryData();
  }

  // =========== 表格选择 =============
  function onSelectChange(keys) {
    selectedRowKeys.value = keys;
  }

  // =========== 表单弹窗 =============
  const formModalRef = ref();

  function showFormModal(record) {
    formModalRef.value.showModal(record);
  }

  // =========== 删除 =============
  function deleteRecord(record) {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除【${record.name}】吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await {模块名}Api.delete{模块名}(record.{主键字段});
          message.success('删除成功');
          queryData();
        } catch (e) {
          smartSentry.captureError(e);
        }
      },
    });
  }

  // =========== 批量删除 =============
  function batchDelete() {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请选择要删除的数据');
      return;
    }

    Modal.confirm({
      title: '确认删除',
      content: `确定要删除选中的 ${selectedRowKeys.value.length} 条数据吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await {模块名}Api.batchDelete{模块名}(selectedRowKeys.value);
          message.success('删除成功');
          selectedRowKeys.value = [];
          queryData();
        } catch (e) {
          smartSentry.captureError(e);
        }
      },
    });
  }

  // =========== 初始化 =============
  onMounted(() => {
    queryData();
  });
</script>

<style scoped lang="less">
  .{模块名}-container {
    .smart-query-form {
      margin-bottom: 16px;
    }

    .smart-query-table-btn-group {
      margin-bottom: 16px;
    }
  }
</style>
```

---

### 2.4 表单弹窗组件模板

**文件位置**: `src/views/{模块分类}/{模块名}/components/{模块名}-form-modal/index.vue`

**完整模板代码**:

```vue
<!--
  * {模块中文名}表单弹窗
  *
  * @Author:    1024创新实验室-主任：卓大
  * @Date:      {当前日期}
  * @Wechat:    zhuda1024
  * @Email:     lab1024@163.com
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <a-modal
    v-model:open="visible"
    :title="formModel.{主键字段} ? '编辑{模块中文名}' : '添加{模块中文名}'"
    :width="800"
    :confirmLoading="confirmLoading"
    @ok="onSubmit"
    @cancel="onCancel"
  >
    <a-form
      ref="formRef"
      :model="formModel"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <!-- 表单字段 -->
      <a-form-item label="字段名" name="字段">
        <a-input v-model:value="formModel.字段" placeholder="请输入" />
      </a-form-item>

      <!-- 下拉选择 -->
      <a-form-item label="选择字段" name="selectField">
        <a-select v-model:value="formModel.selectField" placeholder="请选择">
          <a-select-option :value="1">选项1</a-select-option>
          <a-select-option :value="2">选项2</a-select-option>
        </a-select>
      </a-form-item>

      <!-- 枚举选择 -->
      <a-form-item label="状态" name="status">
        <SmartEnumSelect v-model:value="formModel.status" enumName="STATUS_ENUM" placeholder="请选择状态" />
      </a-form-item>

      <!-- 数字输入 -->
      <a-form-item label="数字字段" name="numberField">
        <a-input-number v-model:value="formModel.numberField" :min="0" :precision="2" style="width: 100%" placeholder="请输入" />
      </a-form-item>

      <!-- 日期选择 -->
      <a-form-item label="日期" name="date">
        <a-date-picker v-model:value="formModel.date" style="width: 100%" placeholder="请选择日期" />
      </a-form-item>

      <!-- 富文本编辑器 -->
      <a-form-item label="描述" name="description">
        <WangEditor v-model:value="formModel.description" :height="300" />
      </a-form-item>

      <!-- 文件上传 -->
      <a-form-item label="图片" name="imageUrl">
        <FileUpload v-model:value="formModel.imageUrl" :maxCount="1" :fileTypes="['image']" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import { {模块名}Api } from '/@/api/{模块分类}/{模块名}-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { REGULAR_CONST } from '/@/constants/regular-const';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import WangEditor from '/@/components/framework/wangeditor/index.vue';
  import FileUpload from '/@/components/support/file-upload/index.vue';

  const emit = defineEmits(['refresh']);

  // =========== 弹窗显示 =============
  const visible = ref(false);
  const confirmLoading = ref(false);
  const formRef = ref();

  // =========== 表单数据 =============
  const formModel = reactive({
    {主键字段}: null,
    // 其他字段...
  });

  // =========== 验证规则 =============
  const rules = {
    字段名: [
      { required: true, message: '请输入字段名', trigger: 'blur' },
      { min: 2, max: 50, message: '长度为2-50个字符', trigger: 'blur' },
    ],
    selectField: [
      { required: true, message: '请选择', trigger: 'change' },
    ],
    numberField: [
      { required: true, message: '请输入数字', trigger: 'blur' },
      { type: 'number', min: 0, message: '必须大于等于0', trigger: 'blur' },
    ],
    // 手机号验证
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: REGULAR_CONST.PHONE, message: '手机号格式不正确', trigger: 'blur' },
    ],
    // 邮箱验证
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
    ],
    // 更多验证规则...
  };

  // =========== 显示弹窗 =============
  function showModal(record) {
    visible.value = true;
    nextTick(() => {
      if (record) {
        // 编辑：回显数据
        Object.assign(formModel, record);
      } else {
        // 新增：重置表单
        resetForm();
      }
    });
  }

  // =========== 重置表单 =============
  function resetForm() {
    formRef.value?.resetFields();
    Object.assign(formModel, {
      {主键字段}: null,
      // 重置其他字段...
    });
  }

  // =========== 提交表单 =============
  async function onSubmit() {
    try {
      // 验证表单
      await formRef.value.validate();

      confirmLoading.value = true;

      // 提交数据
      if (formModel.{主键字段}) {
        await {模块名}Api.update{模块名}(formModel);
        message.success('更新成功');
      } else {
        await {模块名}Api.add{模块名}(formModel);
        message.success('添加成功');
      }

      // 关闭弹窗
      visible.value = false;

      // 刷新列表
      emit('refresh');
    } catch (e) {
      if (e.errorFields) {
        // 验证失败
        console.log('验证失败:', e);
      } else {
        // 请求失败
        smartSentry.captureError(e);
      }
    } finally {
      confirmLoading.value = false;
    }
  }

  // =========== 取消 =============
  function onCancel() {
    visible.value = false;
    resetForm();
  }

  // =========== 暴露方法 =============
  defineExpose({
    showModal,
  });
</script>
```

---

## 3. 项目核心约定

### 3.1 目录结构约定

```
必须遵循的目录结构：

src/
├── api/                        # API 接口
│   ├── business/               # 业务模块
│   │   └── {模块名}/
│   │       └── {模块名}-api.js
│   ├── support/                # 支撑模块
│   └── system/                 # 系统模块
│
├── constants/                  # 常量定义
│   ├── business/
│   │   └── {模块名}-const.js
│   ├── support/
│   └── system/
│
└── views/                      # 页面视图
    ├── business/
    │   └── {模块名}/
    │       ├── index.vue       # 列表页
    │       └── components/     # 页面级组件
    │           └── {模块名}-form-modal/
    │               └── index.vue
    ├── support/
    └── system/
```

### 3.2 命名约定

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 文件名 | kebab-case（短横线） | `product-api.js`、`product-form-modal.vue` |
| 组件名 | PascalCase（大驼峰） | `ProductFormModal` |
| API对象名 | camelCase + Api | `productApi` |
| 常量名 | UPPER_SNAKE_CASE | `PRODUCT_STATUS_ENUM` |
| 函数名 | camelCase（小驼峰） | `queryProduct`、`deleteProduct` |
| 变量名 | camelCase（小驼峰） | `dataList`、`loading` |
| CSS类名 | kebab-case | `product-container` |

### 3.3 文件头注释约定

**所有文件必须包含以下注释**：

```javascript
/*
 * 文件功能描述
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      YYYY-MM-DD HH:mm:ss
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
```

Vue 组件使用 HTML 注释格式：

```html
<!--
  * 组件功能描述
  *
  * @Author:    1024创新实验室-主任：卓大
  * @Date:      YYYY-MM-DD HH:mm:ss
  * @Wechat:    zhuda1024
  * @Email:     lab1024@163.com
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
```

### 3.4 导入路径约定

**必须使用 `/@/` 作为 `src/` 目录的别名**：

```javascript
// ✅ 正确
import { employeeApi } from '/@/api/system/employee-api';
import { useUserStore } from '/@/store/modules/system/user';

// ❌ 错误
import { employeeApi } from '@/api/system/employee-api';
import { employeeApi } from '../../../api/system/employee-api';
```

### 3.5 权限指令约定

**权限命名格式**: `{模块分类}:{模块名}:{操作}`

```javascript
// 系统模块
v-privilege="'system:employee:add'"
v-privilege="'system:employee:update'"
v-privilege="'system:employee:delete'"

// 业务模块
v-privilege="'business:product:add'"
v-privilege="'business:product:update'"

// 支撑模块
v-privilege="'support:file:upload'"
```

### 3.6 API 接口约定

```javascript
// 标准接口命名和请求方式
export const xxxApi = {
  // 分页查询 - POST
  queryXxx: (params) => postRequest('/xxx/query', params),
  
  // 查询所有 - GET
  queryAll: () => getRequest('/xxx/queryAll'),
  
  // 查询详情 - GET
  getXxxDetail: (id) => getRequest(`/xxx/detail/${id}`),
  
  // 添加 - POST
  addXxx: (params) => postRequest('/xxx/add', params),
  
  // 更新 - POST
  updateXxx: (params) => postRequest('/xxx/update', params),
  
  // 删除 - GET
  deleteXxx: (id) => getRequest(`/xxx/delete/${id}`),
  
  // 批量删除 - POST
  batchDeleteXxx: (idList) => postRequest('/xxx/batchDelete', idList),
  
  // 导出 - POST Download
  exportXxx: (params) => postDownload('/xxx/export', params),
};
```

### 3.7 枚举常量约定

```javascript
// 枚举格式
export const {模块名}_{字段名}_ENUM = {
  ENUM_KEY: {
    value: 1,           // 枚举值
    desc: '描述',       // 枚举描述
    color: 'success',   // 标签颜色（可选）
  },
};

// 实际示例
export const PRODUCT_STATUS_ENUM = {
  ON_SALE: {
    value: 1,
    desc: '上架',
    color: 'success',
  },
  OFF_SALE: {
    value: 0,
    desc: '下架',
    color: 'default',
  },
};
```

### 3.8 组件代码组织顺序

```vue
<script setup>
  // 1. 导入依赖（Vue API → 第三方库 → 本地模块）
  import { ref, reactive, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { productApi } from '/@/api/business/product-api';

  // 2. Emits 定义
  const emit = defineEmits(['refresh']);

  // 3. Props 定义（如有）
  const props = defineProps({ ... });

  // 4. 响应式数据
  const dataList = ref([]);
  const loading = ref(false);
  const queryParams = reactive({ ... });

  // 5. 计算属性
  const filteredList = computed(() => { ... });

  // 6. 监听器
  watch(() => props.value, () => { ... });

  // 7. 方法定义
  async function queryData() { ... }
  function onSubmit() { ... }

  // 8. 暴露方法（如需要）
  defineExpose({ queryData });

  // 9. 生命周期
  onMounted(() => { ... });
</script>
```

---

## 4. 常用功能模式

### 4.1 表格列常见类型

```javascript
const columns = ref([
  // 基础文本列
  {
    title: '名称',
    dataIndex: 'name',
    width: 150,
  },

  // 枚举状态列（需要在 bodyCell 中转换）
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
  },

  // 日期时间列
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },

  // 金额列（右对齐）
  {
    title: '金额',
    dataIndex: 'amount',
    width: 120,
    align: 'right',
  },

  // 操作列（固定右侧）
  {
    title: '操作',
    dataIndex: 'operate',
    width: 200,
    fixed: 'right',
  },
]);
```

**bodyCell 插槽处理**：

```vue
<template #bodyCell="{ column, record }">
  <!-- 枚举状态 -->
  <template v-if="column.dataIndex === 'status'">
    <a-tag :color="$smartEnumPlugin.getEnumByValue('STATUS_ENUM', record.status)?.color">
      {{ $smartEnumPlugin.getDescByValue('STATUS_ENUM', record.status) }}
    </a-tag>
  </template>

  <!-- 布尔值 -->
  <template v-else-if="column.dataIndex === 'enableFlag'">
    <a-tag :color="record.enableFlag ? 'success' : 'default'">
      {{ record.enableFlag ? '启用' : '禁用' }}
    </a-tag>
  </template>

  <!-- 金额格式化 -->
  <template v-else-if="column.dataIndex === 'amount'">
    ¥{{ record.amount.toFixed(2) }}
  </template>

  <!-- 图片预览 -->
  <template v-else-if="column.dataIndex === 'imageUrl'">
    <FilePreview :url="record.imageUrl" :width="60" :height="60" />
  </template>

  <!-- 操作按钮 -->
  <template v-else-if="column.dataIndex === 'operate'">
    <div class="smart-table-operate">
      <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
      <a-button type="link" size="small" @click="handleDelete(record)">删除</a-button>
    </div>
  </template>
</template>
```

### 4.2 表单字段常见类型

```vue
<template>
  <a-form ref="formRef" :model="formModel" :rules="rules">
    <!-- 文本输入 -->
    <a-form-item label="名称" name="name">
      <a-input v-model:value="formModel.name" placeholder="请输入名称" />
    </a-form-item>

    <!-- 数字输入 -->
    <a-form-item label="价格" name="price">
      <a-input-number
        v-model:value="formModel.price"
        :min="0"
        :precision="2"
        :step="0.01"
        style="width: 100%"
        placeholder="请输入价格"
      />
    </a-form-item>

    <!-- 下拉选择 -->
    <a-form-item label="分类" name="categoryId">
      <a-select v-model:value="formModel.categoryId" placeholder="请选择分类">
        <a-select-option v-for="item in categoryList" :key="item.id" :value="item.id">
          {{ item.name }}
        </a-select-option>
      </a-select>
    </a-form-item>

    <!-- 枚举选择（使用项目组件） -->
    <a-form-item label="状态" name="status">
      <SmartEnumSelect
        v-model:value="formModel.status"
        enumName="PRODUCT_STATUS_ENUM"
        placeholder="请选择状态"
      />
    </a-form-item>

    <!-- 单选框组 -->
    <a-form-item label="性别" name="gender">
      <a-radio-group v-model:value="formModel.gender">
        <a-radio :value="1">男</a-radio>
        <a-radio :value="2">女</a-radio>
      </a-radio-group>
    </a-form-item>

    <!-- 多选框组 -->
    <a-form-item label="标签" name="tags">
      <a-checkbox-group v-model:value="formModel.tags">
        <a-checkbox value="hot">热门</a-checkbox>
        <a-checkbox value="new">新品</a-checkbox>
        <a-checkbox value="sale">促销</a-checkbox>
      </a-checkbox-group>
    </a-form-item>

    <!-- 日期选择 -->
    <a-form-item label="日期" name="date">
      <a-date-picker
        v-model:value="formModel.date"
        style="width: 100%"
        placeholder="请选择日期"
      />
    </a-form-item>

    <!-- 日期范围选择 -->
    <a-form-item label="有效期" name="dateRange">
      <a-range-picker
        v-model:value="formModel.dateRange"
        style="width: 100%"
        :placeholder="['开始日期', '结束日期']"
      />
    </a-form-item>

    <!-- 时间选择 -->
    <a-form-item label="时间" name="time">
      <a-time-picker
        v-model:value="formModel.time"
        style="width: 100%"
        placeholder="请选择时间"
      />
    </a-form-item>

    <!-- 文本域 -->
    <a-form-item label="备注" name="remark">
      <a-textarea
        v-model:value="formModel.remark"
        :rows="4"
        placeholder="请输入备注"
      />
    </a-form-item>

    <!-- 富文本编辑器 -->
    <a-form-item label="详情" name="content">
      <WangEditor v-model:value="formModel.content" :height="300" />
    </a-form-item>

    <!-- 文件上传（单个） -->
    <a-form-item label="封面图" name="coverImage">
      <FileUpload
        v-model:value="formModel.coverImage"
        :maxCount="1"
        :fileTypes="['image']"
      />
    </a-form-item>

    <!-- 文件上传（多个） -->
    <a-form-item label="附件" name="attachments">
      <FileUpload
        v-model:value="formModel.attachments"
        :maxCount="5"
        :fileTypes="['image', 'pdf', 'doc']"
      />
    </a-form-item>

    <!-- 部门选择（项目组件） -->
    <a-form-item label="部门" name="departmentId">
      <DepartmentTreeSelect
        v-model:value="formModel.departmentId"
        placeholder="请选择部门"
      />
    </a-form-item>

    <!-- 员工选择（项目组件） -->
    <a-form-item label="负责人" name="employeeId">
      <EmployeeSelect
        v-model:value="formModel.employeeId"
        placeholder="请选择员工"
      />
    </a-form-item>
  </a-form>
</template>
```

### 4.3 表单验证规则

```javascript
const rules = {
  // 必填验证
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
  ],

  // 长度验证
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度为2-50个字符', trigger: 'blur' },
  ],

  // 手机号验证
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: REGULAR_CONST.PHONE, message: '手机号格式不正确', trigger: 'blur' },
  ],

  // 邮箱验证
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],

  // 数字验证
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于0', trigger: 'blur' },
  ],

  // 选择验证
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' },
  ],

  // 数组验证
  tags: [
    { required: true, type: 'array', min: 1, message: '请至少选择一个标签', trigger: 'change' },
  ],

  // 自定义验证
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      validator: (rule, value) => {
        if (value.length < 6) {
          return Promise.reject('密码长度不能少于6位');
        }
        return Promise.resolve();
      },
      trigger: 'blur',
    },
  ],
};
```

### 4.4 常见业务操作

```javascript
// ========== 删除确认 ==========
function deleteRecord(record) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除【${record.name}】吗？删除后数据不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        await xxxApi.deleteXxx(record.id);
        message.success('删除成功');
        queryData(); // 刷新列表
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
  });
}

// ========== 批量删除 ==========
function batchDelete() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的数据');
    return;
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条数据吗？`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        await xxxApi.batchDeleteXxx(selectedRowKeys.value);
        message.success('删除成功');
        selectedRowKeys.value = []; // 清空选择
        queryData(); // 刷新列表
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
  });
}

// ========== 状态切换 ==========
function toggleStatus(record) {
  const statusText = record.status === 1 ? '禁用' : '启用';
  Modal.confirm({
    title: `确认${statusText}`,
    content: `确定要${statusText}【${record.name}】吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await xxxApi.updateStatus(record.id, record.status === 1 ? 0 : 1);
        message.success(`${statusText}成功`);
        queryData(); // 刷新列表
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
  });
}

// ========== 导出数据 ==========
function exportData() {
  Modal.confirm({
    title: '确认导出',
    content: '确定要导出当前查询条件的数据吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        message.loading('正在导出，请稍候...', 0);
        await xxxApi.exportXxx(queryParams);
        message.destroy();
        message.success('导出成功');
      } catch (e) {
        message.destroy();
        smartSentry.captureError(e);
      }
    },
  });
}
```

---

## 5. 代码生成检查清单

### ✅ AI 生成代码时必须检查的事项

**文件结构**
- [ ] API 文件放在 `src/api/{模块分类}/{模块名}/{模块名}-api.js`
- [ ] 常量文件放在 `src/constants/{模块分类}/{模块名}-const.js`
- [ ] 列表页放在 `src/views/{模块分类}/{模块名}/index.vue`
- [ ] 表单弹窗放在 `src/views/{模块分类}/{模块名}/components/{模块名}-form-modal/index.vue`

**文件头注释**
- [ ] 所有文件都包含标准的文件头注释
- [ ] 注释中包含作者、日期、联系方式等信息

**命名规范**
- [ ] 文件名使用 kebab-case
- [ ] 组件名使用 PascalCase
- [ ] API 对象名使用 camelCase + Api 后缀
- [ ] 常量名使用 UPPER_SNAKE_CASE
- [ ] 函数名使用 camelCase

**导入路径**
- [ ] 所有导入路径使用 `/@/` 别名
- [ ] 导入顺序：Vue API → 第三方库 → 本地模块

**API 接口**
- [ ] 使用 `getRequest`、`postRequest` 等封装方法
- [ ] 接口方法名符合约定（query、add、update、delete）
- [ ] 每个接口方法都有注释说明

**常量枚举**
- [ ] 枚举名称格式：`{模块名}_{字段名}_ENUM`
- [ ] 枚举包含 value、desc 字段
- [ ] 需要显示标签的枚举包含 color 字段

**列表页面**
- [ ] 包含查询表单
- [ ] 包含操作按钮组
- [ ] 包含数据表格
- [ ] 包含分页组件
- [ ] 使用权限指令 `v-privilege`
- [ ] 错误处理使用 `smartSentry.captureError`

**表单弹窗**
- [ ] 表单验证规则完整
- [ ] 支持新增和编辑两种模式
- [ ] 提交时有 loading 状态
- [ ] 验证失败有提示
- [ ] 提交成功后触发 refresh 事件

**代码组织**
- [ ] script 代码按标准顺序组织
- [ ] 使用注释分隔不同功能区域
- [ ] 使用 `setup` 语法
- [ ] 响应式数据使用 ref 或 reactive

**样式**
- [ ] 使用 scoped 避免样式污染
- [ ] 类名使用 kebab-case
- [ ] 优先使用项目提供的工具类（smart-xxx）

**错误处理**
- [ ] 所有 API 调用都使用 try-catch
- [ ] 捕获的错误使用 smartSentry.captureError 记录
- [ ] 删除操作有二次确认

---

## 6. 技术栈说明

### 6.1 核心依赖

```json
{
  "vue": "3.4.27",                      // Vue 3 框架
  "ant-design-vue": "4.2.5",            // Ant Design Vue UI 组件库
  "vite": "5.2.12",                     // 构建工具
  "pinia": "2.1.7",                     // 状态管理
  "vue-router": "4.3.2",                // 路由管理
  "axios": "1.6.8",                     // HTTP 请求
  "lodash": "4.17.21",                  // 工具函数库
  "dayjs": "1.11.13",                   // 日期处理
  "@wangeditor-next/editor": "5.6.34"   // 富文本编辑器
}
```

### 6.2 项目组件

| 组件名 | 路径 | 说明 |
|--------|------|------|
| SmartEnumSelect | `/@/components/framework/smart-enum-select` | 枚举选择器 |
| SmartEnumRadio | `/@/components/framework/smart-enum-radio` | 枚举单选框 |
| SmartEnumCheckbox | `/@/components/framework/smart-enum-checkbox` | 枚举多选框 |
| WangEditor | `/@/components/framework/wangeditor` | 富文本编辑器 |
| FileUpload | `/@/components/support/file-upload` | 文件上传 |
| FilePreview | `/@/components/support/file-preview` | 文件预览 |
| DepartmentTreeSelect | `/@/components/system/department-tree-select` | 部门树选择 |
| EmployeeSelect | `/@/components/system/employee-select` | 员工选择 |
| TableOperator | `/@/components/support/table-operator` | 表格列配置 |

### 6.3 全局插件

```javascript
// 枚举插件
$smartEnumPlugin.getDescByValue('ENUM_NAME', value)  // 根据值获取描述
$smartEnumPlugin.getEnumByValue('ENUM_NAME', value)  // 根据值获取枚举对象

// 权限插件
$hasPrivilege('permission:key')  // 检查是否有权限

// 字典插件
$dictPlugin.getLabel('DICT_KEY', value)  // 获取字典标签
```

### 6.4 常用工具

```javascript
// 本地存储
import { localSave, localRead, localRemove } from '/@/utils/local-util';

// 错误监控
import { smartSentry } from '/@/lib/smart-sentry';

// 加载提示
import { SmartLoading } from '/@/components/framework/smart-loading';

// 加密工具
import { encryptData, decryptData } from '/@/lib/encrypt';
```

---

## 附录：完整开发示例

### 示例：开发一个"供应商管理"模块

**需求描述**：

```
请基于 SmartAdmin 项目开发一个【供应商管理】功能，需求如下：

1. 功能描述：
   - 管理公司供应商信息
   - 支持供应商的增删改查
   - 支持按供应商名称、联系人搜索
   - 支持供应商启用/禁用

2. 字段列表：
   - 供应商名称：字符串，必填，2-100字符
   - 供应商编码：字符串，必填，唯一
   - 联系人：字符串，必填，2-50字符
   - 联系电话：字符串，必填，手机号格式
   - 联系邮箱：字符串，非必填，邮箱格式
   - 地址：字符串，非必填
   - 状态：枚举（启用/禁用），必填
   - 备注：文本域，非必填

3. 操作权限：
   - business:supplier:add（添加供应商）
   - business:supplier:update（编辑供应商）
   - business:supplier:delete（删除供应商）
   - business:supplier:query（查询供应商）
```

**生成的文件**：

1. `src/api/business/supplier/supplier-api.js`
2. `src/constants/business/supplier-const.js`
3. `src/views/business/supplier/index.vue`
4. `src/views/business/supplier/components/supplier-form-modal/index.vue`

这些文件的具体代码请参考前面的模板进行生成。

---

## 6. 复杂页面开发指南

前面的模板主要适用于**标准的增删改查列表页**，但实际开发中经常会遇到更复杂的页面场景。本章节提供复杂页面的开发指南，确保即使是复杂页面也能遵循项目规范。

### 6.1 复杂页面类型

| 页面类型 | 说明 | 典型场景 |
|---------|------|---------|
| 数据可视化 Dashboard | 包含多个图表、统计卡片的仪表盘 | 首页、数据分析、经营看板 |
| 多步骤表单 | 分步骤填写的复杂表单 | 订单提交、注册流程、审批流程 |
| 主从表页面 | 主表+明细表的关系页面 | 订单详情、采购单、出入库单 |
| 树形+详情页面 | 左侧树形结构+右侧详情 | 组织架构、商品分类、菜单管理 |
| 拖拽排序页面 | 支持拖拽排序的页面 | 菜单排序、广告位管理 |
| 流程图页面 | 流程设计、审批流程 | 工作流设计、审批流程配置 |
| 自定义布局页面 | 灵活布局的复杂页面 | 个性化配置、自定义报表 |

### 6.2 核心原则

**无论多复杂的页面，都必须遵循以下原则：**

1. **文件结构规范**
   - 所有文件必须放在规定的目录下
   - 使用统一的文件命名规范
   - 复杂组件拆分到 `components/` 目录

2. **命名规范不变**
   - API、常量、变量、函数命名规则不变
   - 使用 `/@/` 路径别名
   - 文件头注释必须包含

3. **代码组织顺序**
   - script setup 代码组织顺序不变
   - 导入顺序保持一致
   - 注释分隔符使用统一格式

4. **技术栈一致**
   - 使用项目已有的组件库
   - 图表使用 ECharts
   - 状态管理使用 Pinia
   - 路由使用 vue-router

### 6.3 场景一：数据可视化 Dashboard

**示例需求**：

```
请基于 SmartAdmin 项目开发一个【销售数据看板】页面，需求如下：

1. 页面布局：
   - 顶部：4个统计卡片（今日销售额、订单数、客户数、转化率）
   - 中部左侧：销售趋势折线图（近30天）
   - 中部右侧：销售分类饼图（按产品分类）
   - 底部：销售排行榜表格（Top 10 产品）

2. 数据接口：
   - 获取统计数据
   - 获取趋势数据
   - 获取分类数据
   - 获取排行数据

3. 交互功能：
   - 支持日期范围筛选
   - 图表支持鼠标悬浮显示详情
   - 表格支持排序
```

**文件结构**：

```
src/
├── api/business/
│   └── sales-dashboard-api.js          # Dashboard API
├── views/business/
│   └── sales-dashboard/
│       ├── index.vue                    # 主页面
│       └── components/
│           ├── statistics-card.vue      # 统计卡片组件
│           ├── sales-trend-chart.vue    # 趋势图表组件
│           ├── category-chart.vue       # 分类图表组件
│           └── ranking-table.vue        # 排行榜表格组件
```

**主页面模板**：

```vue
<!--
  * 销售数据看板
  *
  * @Author:    1024创新实验室-主任：卓大
  * @Date:      2025-11-03
  * @Copyright  1024创新实验室
-->
<template>
  <div class="sales-dashboard">
    <!-- ========== 日期筛选 ========== -->
    <a-card class="filter-card" :bordered="false">
      <a-form layout="inline">
        <a-form-item label="日期范围">
          <a-range-picker
            v-model:value="dateRange"
            :default-value="defaultTimeRanges.近30天"
            @change="onDateChange"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="refreshData">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- ========== 统计卡片 ========== -->
    <a-row :gutter="16" class="statistics-row">
      <a-col :xs="24" :sm="12" :lg="6">
        <StatisticsCard
          title="今日销售额"
          :value="statistics.todaySales"
          prefix="¥"
          :trend="statistics.salesTrend"
          :loading="statisticsLoading"
        />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatisticsCard
          title="订单数"
          :value="statistics.orderCount"
          suffix="单"
          :trend="statistics.orderTrend"
          :loading="statisticsLoading"
        />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatisticsCard
          title="客户数"
          :value="statistics.customerCount"
          suffix="人"
          :trend="statistics.customerTrend"
          :loading="statisticsLoading"
        />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatisticsCard
          title="转化率"
          :value="statistics.conversionRate"
          suffix="%"
          :trend="statistics.conversionTrend"
          :loading="statisticsLoading"
        />
      </a-col>
    </a-row>

    <!-- ========== 图表区域 ========== -->
    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :lg="16">
        <a-card title="销售趋势" :bordered="false" :loading="trendLoading">
          <SalesTrendChart :data="trendData" :height="400" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card title="销售分类" :bordered="false" :loading="categoryLoading">
          <CategoryChart :data="categoryData" :height="400" />
        </a-card>
      </a-col>
    </a-row>

    <!-- ========== 排行榜 ========== -->
    <a-card title="产品销售排行" :bordered="false" class="ranking-card">
      <RankingTable :data="rankingData" :loading="rankingLoading" />
    </a-card>
  </div>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue';
  import { ReloadOutlined } from '@ant-design/icons-vue';
  import dayjs from 'dayjs';
  import { salesDashboardApi } from '/@/api/business/sales-dashboard-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { defaultTimeRanges } from '/@/lib/default-time-ranges';
  import StatisticsCard from './components/statistics-card.vue';
  import SalesTrendChart from './components/sales-trend-chart.vue';
  import CategoryChart from './components/category-chart.vue';
  import RankingTable from './components/ranking-table.vue';

  // =========== 日期筛选 =============
  const dateRange = ref([
    dayjs().subtract(30, 'day'),
    dayjs()
  ]);

  function onDateChange() {
    refreshData();
  }

  // =========== 统计数据 =============
  const statisticsLoading = ref(false);
  const statistics = reactive({
    todaySales: 0,
    salesTrend: 0,
    orderCount: 0,
    orderTrend: 0,
    customerCount: 0,
    customerTrend: 0,
    conversionRate: 0,
    conversionTrend: 0,
  });

  async function queryStatistics() {
    try {
      statisticsLoading.value = true;
      const res = await salesDashboardApi.getStatistics({
        startDate: dateRange.value[0].format('YYYY-MM-DD'),
        endDate: dateRange.value[1].format('YYYY-MM-DD'),
      });
      Object.assign(statistics, res.data);
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      statisticsLoading.value = false;
    }
  }

  // =========== 趋势数据 =============
  const trendLoading = ref(false);
  const trendData = ref([]);

  async function queryTrend() {
    try {
      trendLoading.value = true;
      const res = await salesDashboardApi.getTrend({
        startDate: dateRange.value[0].format('YYYY-MM-DD'),
        endDate: dateRange.value[1].format('YYYY-MM-DD'),
      });
      trendData.value = res.data;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      trendLoading.value = false;
    }
  }

  // =========== 分类数据 =============
  const categoryLoading = ref(false);
  const categoryData = ref([]);

  async function queryCategory() {
    try {
      categoryLoading.value = true;
      const res = await salesDashboardApi.getCategory({
        startDate: dateRange.value[0].format('YYYY-MM-DD'),
        endDate: dateRange.value[1].format('YYYY-MM-DD'),
      });
      categoryData.value = res.data;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      categoryLoading.value = false;
    }
  }

  // =========== 排行数据 =============
  const rankingLoading = ref(false);
  const rankingData = ref([]);

  async function queryRanking() {
    try {
      rankingLoading.value = true;
      const res = await salesDashboardApi.getRanking({
        startDate: dateRange.value[0].format('YYYY-MM-DD'),
        endDate: dateRange.value[1].format('YYYY-MM-DD'),
      });
      rankingData.value = res.data;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      rankingLoading.value = false;
    }
  }

  // =========== 刷新数据 =============
  function refreshData() {
    queryStatistics();
    queryTrend();
    queryCategory();
    queryRanking();
  }

  // =========== 初始化 =============
  onMounted(() => {
    refreshData();
  });
</script>

<style scoped lang="less">
  .sales-dashboard {
    .filter-card {
      margin-bottom: 16px;
    }

    .statistics-row {
      margin-bottom: 16px;
    }

    .chart-row {
      margin-bottom: 16px;
    }

    .ranking-card {
      margin-bottom: 16px;
    }
  }
</style>
```

**图表组件示例**（销售趋势图）：

```vue
<!--
  * 销售趋势图表组件
  *
  * @Author:    1024创新实验室-主任：卓大
  * @Date:      2025-11-03
  * @Copyright  1024创新实验室
-->
<template>
  <div ref="chartRef" :style="{ height: height + 'px' }"></div>
</template>

<script setup>
  import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
  import * as echarts from 'echarts';

  const props = defineProps({
    data: {
      type: Array,
      default: () => []
    },
    height: {
      type: Number,
      default: 400
    }
  });

  const chartRef = ref();
  let chartInstance = null;

  // =========== 初始化图表 =============
  function initChart() {
    if (!chartRef.value) return;

    chartInstance = echarts.init(chartRef.value);

    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        }
      },
      legend: {
        data: ['销售额', '订单量']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: props.data.map(item => item.date)
      },
      yAxis: [
        {
          type: 'value',
          name: '销售额（元）',
          position: 'left',
        },
        {
          type: 'value',
          name: '订单量（单）',
          position: 'right',
        }
      ],
      series: [
        {
          name: '销售额',
          type: 'line',
          smooth: true,
          data: props.data.map(item => item.sales),
          itemStyle: {
            color: '#1890ff'
          }
        },
        {
          name: '订单量',
          type: 'line',
          smooth: true,
          yAxisIndex: 1,
          data: props.data.map(item => item.orderCount),
          itemStyle: {
            color: '#52c41a'
          }
        }
      ]
    };

    chartInstance.setOption(option);
  }

  // =========== 更新图表 =============
  function updateChart() {
    if (!chartInstance) return;

    chartInstance.setOption({
      xAxis: {
        data: props.data.map(item => item.date)
      },
      series: [
        {
          data: props.data.map(item => item.sales)
        },
        {
          data: props.data.map(item => item.orderCount)
        }
      ]
    });
  }

  // =========== 监听数据变化 =============
  watch(() => props.data, () => {
    updateChart();
  }, { deep: true });

  // =========== 窗口大小变化 =============
  function handleResize() {
    chartInstance?.resize();
  }

  // =========== 生命周期 =============
  onMounted(() => {
    initChart();
    window.addEventListener('resize', handleResize);
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize);
    chartInstance?.dispose();
  });
</script>
```

### 6.4 场景二：多步骤表单

**示例需求**：

```
请基于 SmartAdmin 项目开发一个【订单提交】多步骤表单，需求如下：

1. 步骤流程：
   - 步骤1：选择商品（商品列表选择）
   - 步骤2：填写订单信息（收货地址、联系方式、备注）
   - 步骤3：确认订单（预览订单信息）
   - 步骤4：提交成功（显示订单号）

2. 交互要求：
   - 支持上一步、下一步
   - 每步验证通过才能进入下一步
   - 最后一步提交订单
   - 支持保存草稿
```

**文件结构**：

```
src/
├── api/business/
│   └── order-api.js
└── views/business/
    └── order-submit/
        ├── index.vue                    # 主页面
        └── components/
            ├── step1-select-product.vue  # 步骤1
            ├── step2-order-info.vue      # 步骤2
            ├── step3-confirm.vue         # 步骤3
            └── step4-success.vue         # 步骤4
```

**主页面模板**：

```vue
<!--
  * 订单提交 - 多步骤表单
  *
  * @Author:    1024创新实验室-主任：卓大
  * @Date:      2025-11-03
  * @Copyright  1024创新实验室
-->
<template>
  <a-card class="order-submit-container">
    <!-- ========== 步骤条 ========== -->
    <a-steps :current="currentStep" class="steps">
      <a-step title="选择商品" />
      <a-step title="填写信息" />
      <a-step title="确认订单" />
      <a-step title="提交成功" />
    </a-steps>

    <!-- ========== 步骤内容 ========== -->
    <div class="step-content">
      <!-- 步骤1 -->
      <Step1SelectProduct
        v-if="currentStep === 0"
        v-model:selectedProducts="formData.products"
        ref="step1Ref"
      />

      <!-- 步骤2 -->
      <Step2OrderInfo
        v-if="currentStep === 1"
        v-model:orderInfo="formData.orderInfo"
        ref="step2Ref"
      />

      <!-- 步骤3 -->
      <Step3Confirm
        v-if="currentStep === 2"
        :formData="formData"
      />

      <!-- 步骤4 -->
      <Step4Success
        v-if="currentStep === 3"
        :orderNo="orderNo"
      />
    </div>

    <!-- ========== 操作按钮 ========== -->
    <div class="step-actions" v-if="currentStep < 3">
      <a-space>
        <a-button v-if="currentStep > 0" @click="prevStep">
          上一步
        </a-button>
        <a-button v-if="currentStep < 2" type="primary" @click="nextStep">
          下一步
        </a-button>
        <a-button v-if="currentStep === 2" type="primary" :loading="submitting" @click="submitOrder">
          提交订单
        </a-button>
        <a-button @click="saveDraft">
          保存草稿
        </a-button>
      </a-space>
    </div>
  </a-card>
</template>

<script setup>
  import { ref, reactive } from 'vue';
  import { message } from 'ant-design-vue';
  import { useRouter } from 'vue-router';
  import { orderApi } from '/@/api/business/order-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import Step1SelectProduct from './components/step1-select-product.vue';
  import Step2OrderInfo from './components/step2-order-info.vue';
  import Step3Confirm from './components/step3-confirm.vue';
  import Step4Success from './components/step4-success.vue';

  const router = useRouter();

  // =========== 当前步骤 =============
  const currentStep = ref(0);

  // =========== 表单数据 =============
  const formData = reactive({
    products: [],      // 选中的商品
    orderInfo: {       // 订单信息
      receiverName: '',
      receiverPhone: '',
      receiverAddress: '',
      remark: '',
    },
  });

  // =========== 步骤引用 =============
  const step1Ref = ref();
  const step2Ref = ref();

  // =========== 上一步 =============
  function prevStep() {
    currentStep.value--;
  }

  // =========== 下一步 =============
  async function nextStep() {
    // 验证当前步骤
    let valid = true;
    if (currentStep.value === 0) {
      valid = await step1Ref.value?.validate();
    } else if (currentStep.value === 1) {
      valid = await step2Ref.value?.validate();
    }

    if (!valid) {
      return;
    }

    currentStep.value++;
  }

  // =========== 提交订单 =============
  const submitting = ref(false);
  const orderNo = ref('');

  async function submitOrder() {
    try {
      submitting.value = true;
      const res = await orderApi.submitOrder(formData);
      orderNo.value = res.data.orderNo;
      message.success('订单提交成功');
      currentStep.value = 3;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      submitting.value = false;
    }
  }

  // =========== 保存草稿 =============
  async function saveDraft() {
    try {
      await orderApi.saveDraft(formData);
      message.success('草稿保存成功');
    } catch (e) {
      smartSentry.captureError(e);
    }
  }
</script>

<style scoped lang="less">
  .order-submit-container {
    .steps {
      margin-bottom: 32px;
    }

    .step-content {
      min-height: 400px;
      margin-bottom: 24px;
    }

    .step-actions {
      text-align: center;
      padding-top: 24px;
      border-top: 1px solid #f0f0f0;
    }
  }
</style>
```

### 6.5 场景三：主从表页面

**示例需求**：

```
请基于 SmartAdmin 项目开发一个【采购单管理】主从表页面，需求如下：

1. 页面布局：
   - 上半部分：采购单主表信息（单号、供应商、日期等）
   - 下半部分：采购单明细表（商品列表、数量、单价等）

2. 功能要求：
   - 可以新增、编辑、删除明细
   - 自动计算总金额
   - 支持保存草稿
   - 支持提交审核
```

**主页面模板**：

```vue
<!--
  * 采购单详情 - 主从表页面
  *
  * @Author:    1024创新实验室-主任：卓大
  * @Date:      2025-11-03
  * @Copyright  1024创新实验室
-->
<template>
  <a-card class="purchase-order-detail">
    <!-- ========== 主表信息 ========== -->
    <a-card title="采购单信息" :bordered="false" class="main-info">
      <a-form
        ref="mainFormRef"
        :model="mainForm"
        :rules="mainRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-row>
          <a-col :span="12">
            <a-form-item label="采购单号" name="orderNo">
              <a-input v-model:value="mainForm.orderNo" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="采购日期" name="purchaseDate">
              <a-date-picker
                v-model:value="mainForm.purchaseDate"
                style="width: 100%"
                placeholder="请选择日期"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="供应商" name="supplierId">
              <a-select
                v-model:value="mainForm.supplierId"
                placeholder="请选择供应商"
              >
                <a-select-option
                  v-for="item in supplierList"
                  :key="item.id"
                  :value="item.id"
                >
                  {{ item.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-tag :color="statusColor">{{ statusText }}</a-tag>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="备注" name="remark" :label-col="{ span: 3 }">
              <a-textarea
                v-model:value="mainForm.remark"
                :rows="3"
                placeholder="请输入备注"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- ========== 明细表信息 ========== -->
    <a-card title="采购明细" :bordered="false" class="detail-info">
      <template #extra>
        <a-button type="primary" @click="addDetail">
          <template #icon><PlusOutlined /></template>
          添加明细
        </a-button>
      </template>

      <a-table
        :columns="detailColumns"
        :data-source="detailList"
        :pagination="false"
        row-key="id"
        bordered
      >
        <template #bodyCell="{ column, record, index }">
          <!-- 商品 -->
          <template v-if="column.dataIndex === 'productId'">
            <a-select
              v-model:value="record.productId"
              style="width: 100%"
              placeholder="请选择商品"
              @change="onProductChange(record)"
            >
              <a-select-option
                v-for="item in productList"
                :key="item.id"
                :value="item.id"
              >
                {{ item.name }}
              </a-select-option>
            </a-select>
          </template>

          <!-- 数量 -->
          <template v-else-if="column.dataIndex === 'quantity'">
            <a-input-number
              v-model:value="record.quantity"
              :min="1"
              style="width: 100%"
              @change="calculateAmount(record)"
            />
          </template>

          <!-- 单价 -->
          <template v-else-if="column.dataIndex === 'price'">
            <a-input-number
              v-model:value="record.price"
              :min="0"
              :precision="2"
              style="width: 100%"
              @change="calculateAmount(record)"
            />
          </template>

          <!-- 金额 -->
          <template v-else-if="column.dataIndex === 'amount'">
            ¥{{ record.amount?.toFixed(2) || '0.00' }}
          </template>

          <!-- 操作 -->
          <template v-else-if="column.dataIndex === 'operate'">
            <a-button
              type="link"
              danger
              size="small"
              @click="deleteDetail(index)"
            >
              删除
            </a-button>
          </template>
        </template>
      </a-table>

      <!-- 合计 -->
      <div class="total-amount">
        <span>合计金额：</span>
        <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
    </a-card>

    <!-- ========== 操作按钮 ========== -->
    <div class="actions">
      <a-space>
        <a-button type="primary" @click="save">保存</a-button>
        <a-button @click="saveDraft">保存草稿</a-button>
        <a-button @click="submit">提交审核</a-button>
        <a-button @click="goBack">返回</a-button>
      </a-space>
    </div>
  </a-card>
</template>

<script setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { useRouter, useRoute } from 'vue-router';
  import { purchaseOrderApi } from '/@/api/business/purchase-order-api';
  import { supplierApi } from '/@/api/business/supplier-api';
  import { productApi } from '/@/api/business/product-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const router = useRouter();
  const route = useRoute();

  // =========== 主表数据 =============
  const mainFormRef = ref();
  const mainForm = reactive({
    orderId: null,
    orderNo: '系统自动生成',
    supplierId: null,
    purchaseDate: null,
    remark: '',
    status: 0,
  });

  const mainRules = {
    supplierId: [
      { required: true, message: '请选择供应商', trigger: 'change' }
    ],
    purchaseDate: [
      { required: true, message: '请选择采购日期', trigger: 'change' }
    ],
  };

  // =========== 明细数据 =============
  const detailList = ref([]);
  
  const detailColumns = [
    {
      title: '序号',
      width: 60,
      customRender: ({ index }) => index + 1,
    },
    {
      title: '商品',
      dataIndex: 'productId',
      width: 200,
    },
    {
      title: '数量',
      dataIndex: 'quantity',
      width: 120,
    },
    {
      title: '单价',
      dataIndex: 'price',
      width: 120,
    },
    {
      title: '金额',
      dataIndex: 'amount',
      width: 120,
    },
    {
      title: '操作',
      dataIndex: 'operate',
      width: 80,
      fixed: 'right',
    },
  ];

  // =========== 下拉数据 =============
  const supplierList = ref([]);
  const productList = ref([]);

  async function querySupplierList() {
    try {
      const res = await supplierApi.queryAll();
      supplierList.value = res.data;
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  async function queryProductList() {
    try {
      const res = await productApi.queryAll();
      productList.value = res.data;
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  // =========== 明细操作 =============
  function addDetail() {
    detailList.value.push({
      id: Date.now(),
      productId: null,
      quantity: 1,
      price: 0,
      amount: 0,
    });
  }

  function deleteDetail(index) {
    detailList.value.splice(index, 1);
  }

  function onProductChange(record) {
    const product = productList.value.find(p => p.id === record.productId);
    if (product) {
      record.price = product.price;
      calculateAmount(record);
    }
  }

  function calculateAmount(record) {
    record.amount = (record.quantity || 0) * (record.price || 0);
  }

  // =========== 合计金额 =============
  const totalAmount = computed(() => {
    return detailList.value.reduce((sum, item) => sum + (item.amount || 0), 0);
  });

  // =========== 状态显示 =============
  const statusColor = computed(() => {
    const colors = { 0: 'default', 1: 'processing', 2: 'success' };
    return colors[mainForm.status] || 'default';
  });

  const statusText = computed(() => {
    const texts = { 0: '草稿', 1: '待审核', 2: '已审核' };
    return texts[mainForm.status] || '未知';
  });

  // =========== 保存/提交 =============
  async function save() {
    try {
      await mainFormRef.value.validate();
      
      if (detailList.value.length === 0) {
        message.warning('请至少添加一条明细');
        return;
      }

      const data = {
        ...mainForm,
        details: detailList.value,
        totalAmount: totalAmount.value,
      };

      if (mainForm.orderId) {
        await purchaseOrderApi.update(data);
        message.success('保存成功');
      } else {
        await purchaseOrderApi.add(data);
        message.success('添加成功');
      }

      goBack();
    } catch (e) {
      if (e.errorFields) {
        console.log('验证失败:', e);
      } else {
        smartSentry.captureError(e);
      }
    }
  }

  async function saveDraft() {
    mainForm.status = 0;
    await save();
  }

  async function submit() {
    mainForm.status = 1;
    await save();
  }

  function goBack() {
    router.back();
  }

  // =========== 初始化 =============
  onMounted(() => {
    querySupplierList();
    queryProductList();

    // 如果有ID，加载数据
    const orderId = route.query.id;
    if (orderId) {
      // 加载采购单数据
      // ...
    }
  });
</script>

<style scoped lang="less">
  .purchase-order-detail {
    .main-info {
      margin-bottom: 16px;
    }

    .detail-info {
      margin-bottom: 16px;

      .total-amount {
        text-align: right;
        padding: 16px;
        font-size: 16px;
        font-weight: bold;

        .amount {
          color: #f5222d;
          font-size: 20px;
        }
      }
    }

    .actions {
      text-align: center;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
    }
  }
</style>
```

### 6.6 通用规范总结

**无论多复杂的页面，必须遵循：**

✅ **文件组织**
```
views/{模块}/
├── index.vue                 # 主页面
└── components/               # 拆分的子组件
    ├── xxx-component.vue
    └── xxx-component.vue
```

✅ **命名规范**
- 文件名：kebab-case
- 组件名：PascalCase
- API/变量：camelCase
- 常量：UPPER_SNAKE_CASE

✅ **代码组织**
```vue
<script setup>
  // 1. 导入
  // 2. Props/Emits
  // 3. 响应式数据
  // 4. 计算属性
  // 5. 方法
  // 6. 生命周期
</script>
```

✅ **注释要求**
- 文件头注释
- 复杂逻辑添加注释
- 使用分隔符标注功能区域

✅ **错误处理**
```javascript
try {
  // 业务逻辑
} catch (e) {
  smartSentry.captureError(e);
}
```

✅ **图表组件**
- 使用 ECharts
- 响应式处理
- 资源清理

---

## 结语

本文档为 AI 提供了完整的开发指令和代码模板，确保生成的代码：
- ✅ 符合项目规范
- ✅ 保持代码风格一致
- ✅ 包含完整的功能实现
- ✅ 可以直接使用，无需大量修改

**使用建议**：
1. 将本文档提供给 AI 作为开发依据
2. 明确描述需求（功能、字段、权限）
3. AI 将自动生成所有必需文件
4. 生成后按检查清单进行验证

---

**文档版权**: © 2012-2025 1024创新实验室  
**官网**: https://smartadmin.1024lab.net  
**GitHub**: https://github.com/1024-lab/smart-admin

