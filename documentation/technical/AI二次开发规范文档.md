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
1. 功能描述：...
2. 字段列表：...
3. 操作权限：...
```

AI将自动生成：
- ✅ API 接口文件
- ✅ 常量定义文件
- ✅ 列表页面组件
- ✅ 表单弹窗组件
- ✅ 所有代码符合项目规范

---

## 📋 目录

1. [快速开始 - AI开发模板](#1-快速开始---ai开发模板)
2. [标准开发流程](#2-标准开发流程)
3. [完整代码模板](#3-完整代码模板)
4. [项目核心约定](#4-项目核心约定)
5. [常用功能模板](#5-常用功能模板)
6. [技术栈说明](#6-技术栈说明)

---

## 1. 项目概述

### 1.1 项目简介
SmartAdmin 是一个基于 Vue 3 + Ant Design Vue 的现代化企业级后台管理系统，采用组合式 API (Composition API) 开发模式，支持动态路由、权限控制、数据加密等企业级功能。

### 1.2 核心特性
- ✅ **动态路由**: 从后端获取菜单数据，动态构建路由
- ✅ **权限控制**: 基于功能点的细粒度权限控制
- ✅ **数据加密**: 支持敏感数据加密传输
- ✅ **国际化**: 内置中英文支持
- ✅ **主题配置**: 支持多主题、暗黑模式
- ✅ **KeepAlive**: 智能页面缓存管理
- ✅ **标签页导航**: 多标签页管理

### 1.3 浏览器支持
- Node.js >= 18
- 现代浏览器（Chrome、Firefox、Safari、Edge）

---

## 2. 技术栈与核心依赖

### 2.1 核心框架
```json
{
  "vue": "3.4.27",
  "ant-design-vue": "4.2.5",
  "vite": "5.2.12",
  "pinia": "2.1.7",
  "vue-router": "4.3.2",
  "axios": "1.6.8"
}
```

### 2.2 重要工具库
```json
{
  "lodash": "4.17.21",           // 工具函数库
  "dayjs": "1.11.13",            // 日期处理
  "crypto-js": "4.1.1",          // 加密解密
  "sm-crypto": "0.3.13",         // 国密加密
  "echarts": "5.4.3",            // 图表库
  "@wangeditor-next/editor": "5.6.34"  // 富文本编辑器
}
```

### 2.3 构建工具
- **Vite 5**: 快速的开发服务器和构建工具
- **Less**: CSS 预处理器
- **ESLint + Prettier**: 代码规范和格式化

---

## 3. 项目结构规范

### 3.1 目录结构
```
smart-admin-web-javascript/
├── public/                     # 静态资源
│   └── favicon.ico
├── src/
│   ├── api/                    # API 接口定义
│   │   ├── business/           # 业务模块 API
│   │   ├── support/            # 支撑模块 API
│   │   └── system/             # 系统模块 API
│   ├── assets/                 # 资源文件
│   │   └── images/             # 图片资源
│   ├── components/             # 公共组件
│   │   ├── business/           # 业务组件
│   │   ├── framework/          # 框架组件
│   │   ├── support/            # 支撑组件
│   │   └── system/             # 系统组件
│   ├── config/                 # 配置文件
│   │   └── app-config.js       # 应用默认配置
│   ├── constants/              # 常量定义
│   │   ├── business/           # 业务常量
│   │   ├── support/            # 支撑常量
│   │   ├── system/             # 系统常量
│   │   └── common-const.js     # 通用常量
│   ├── directives/             # 自定义指令
│   │   └── privilege.js        # 权限指令
│   ├── i18n/                   # 国际化
│   │   └── lang/               # 语言包
│   ├── layout/                 # 布局组件
│   │   ├── components/         # 布局子组件
│   │   └── index.vue           # 主布局
│   ├── lib/                    # 工具库封装
│   │   ├── axios.js            # axios 封装
│   │   ├── encrypt.js          # 加密工具
│   │   └── smart-watermark.js  # 水印工具
│   ├── plugins/                # Vue 插件
│   │   ├── dict-plugin.js      # 字典插件
│   │   ├── privilege-plugin.js # 权限插件
│   │   └── smart-enums-plugin.js # 枚举插件
│   ├── router/                 # 路由配置
│   │   ├── index.js            # 路由主文件
│   │   ├── routers.js          # 静态路由
│   │   ├── support/            # 支撑路由
│   │   └── system/             # 系统路由
│   ├── store/                  # 状态管理
│   │   ├── index.js            # Store 主文件
│   │   └── modules/            # Store 模块
│   ├── theme/                  # 主题配置
│   │   ├── color.js            # 颜色配置
│   │   └── index.less          # 样式入口
│   ├── utils/                  # 工具函数
│   │   ├── local-util.js       # 本地存储工具
│   │   └── str-util.js         # 字符串工具
│   ├── views/                  # 页面视图
│   │   ├── business/           # 业务页面
│   │   ├── support/            # 支撑页面
│   │   └── system/             # 系统页面
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
├── index.html                  # HTML 模板
├── vite.config.js              # Vite 配置
├── package.json                # 项目配置
└── jsconfig.json               # JS 配置
```

### 3.2 模块分类原则
- **system/**: 系统核心模块（用户、角色、权限、菜单等）
- **support/**: 支撑模块（日志、字典、配置、文件等）
- **business/**: 业务模块（ERP、OA 等具体业务）

### 3.3 组件存放规则
- 公共业务组件 → `components/business/`
- 框架级组件 → `components/framework/`
- 系统组件 → `components/system/`
- 页面级组件 → `views/{module}/components/`

---

## 4. 代码规范

### 4.1 文件头注释
**所有文件必须包含统一的文件头注释**：

```javascript
/*
 * 文件功能描述
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2022-09-03 21:59:15
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
  * @Date:      2022-09-12 15:09:02
  * @Wechat:    zhuda1024
  * @Email:     lab1024@163.com
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
```

### 4.2 代码风格
- 使用 2 空格缩进
- 使用单引号
- 语句结尾使用分号
- 函数名使用小驼峰命名（camelCase）
- 类名使用大驼峰命名（PascalCase）
- 常量使用全大写下划线命名（SNAKE_CASE）

### 4.3 导入顺序
```javascript
// 1. Vue 核心库
import { ref, reactive, onMounted } from 'vue';

// 2. 第三方库
import { message, Modal } from 'ant-design-vue';
import _ from 'lodash';

// 3. API 接口
import { employeeApi } from '/@/api/system/employee-api';

// 4. 组件
import EmployeeFormModal from './employee-form-modal/index.vue';

// 5. 工具函数
import { smartSentry } from '/@/lib/smart-sentry';

// 6. 常量
import { PAGE_SIZE } from '/@/constants/common-const';
```

### 4.4 路径别名
使用 `/@/` 作为 `src/` 目录的别名：
```javascript
import { employeeApi } from '/@/api/system/employee-api';
import { useUserStore } from '/@/store/modules/system/user';
```

---

## 5. API 接口规范

### 5.1 API 文件结构
每个模块的 API 放在对应的 `-api.js` 文件中：

```javascript
/*
 * 员工 API
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2022-09-03 21:59:15
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { getRequest, postRequest, postEncryptRequest } from '/@/lib/axios';

export const employeeApi = {
  /**
   * 查询所有员工 @author 卓大
   */
  queryAll: (params) => {
    return getRequest('/employee/queryAll', params);
  },

  /**
   * 员工管理查询
   */
  queryEmployee: (params) => {
    return postRequest('/employee/query', params);
  },

  /**
   * 添加员工
   */
  addEmployee: (params) => {
    return postRequest('/employee/add', params);
  },

  /**
   * 更新员工信息
   */
  updateEmployee: (params) => {
    return postRequest('/employee/update', params);
  },

  /**
   * 删除员工
   */
  deleteEmployee: (employeeId) => {
    return getRequest(`/employee/delete/${employeeId}`);
  },

  /**
   * 修改密码（加密传输）
   */
  updateEmployeePassword: (param) => {
    return postEncryptRequest('/employee/update/password', param);
  },
};
```

### 5.2 请求方法
```javascript
// GET 请求
getRequest(url, params)

// POST 请求
postRequest(url, data)

// 加密 POST 请求（敏感数据）
postEncryptRequest(url, data)

// GET 下载
getDownload(url, params)

// POST 下载
postDownload(url, data)

// 通用请求
request(config)
```

### 5.3 API 命名规范
- 查询列表: `query{Module}` 或 `query{Module}List`
- 查询所有: `queryAll`
- 查询详情: `get{Module}Detail`
- 添加: `add{Module}`
- 更新: `update{Module}`
- 删除: `delete{Module}`
- 批量删除: `batchDelete{Module}`
- 导出: `export{Module}`
- 导入: `import{Module}`

### 5.4 响应处理
API 自动处理响应：
- 成功：`res.code === 1`，返回 `Promise.resolve(res)`
- 失败：自动显示错误信息，返回 `Promise.reject(response)`
- Token 过期：自动退出登录

### 5.5 错误处理
```javascript
// 在组件中使用 try-catch
import { smartSentry } from '/@/lib/smart-sentry';

async function query() {
  try {
    let res = await employeeApi.queryAll();
    employeeList.value = res.data;
  } catch (e) {
    smartSentry.captureError(e);
  }
}
```

---

## 6. Vue 组件开发规范

### 6.1 组件结构（推荐 setup script）
```vue
<!--
  * 组件描述
  *
  * @Author:    1024创新实验室
  * @Date:      2022-09-12 15:09:02
  * @Copyright  1024创新实验室
-->
<template>
  <div class="component-wrapper">
    <!-- 模板内容 -->
  </div>
</template>

<script setup>
  // =========== 导入依赖 =============
  import { ref, reactive, computed, watch, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import _ from 'lodash';
  import { employeeApi } from '/@/api/system/employee-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  // =========== 属性定义 和 事件方法暴露 =============
  const props = defineProps({
    value: [Number, Array],
    placeholder: {
      type: String,
      default: '请选择',
    },
    width: {
      type: String,
      default: '100%',
    },
  });

  const emit = defineEmits(['update:value', 'change']);

  // =========== 响应式数据 =============
  const dataList = ref([]);
  const loading = ref(false);
  const selectValue = ref(props.value);

  // =========== 计算属性 =============
  const filteredList = computed(() => {
    return dataList.value.filter(item => item.status);
  });

  // =========== 监听器 =============
  watch(
    () => props.value,
    (newValue) => {
      selectValue.value = newValue;
    }
  );

  // =========== 方法定义 =============
  async function queryData() {
    try {
      loading.value = true;
      let res = await employeeApi.queryAll();
      dataList.value = res.data;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  }

  function onChange(value) {
    emit('update:value', value);
    emit('change', value);
  }

  // =========== 暴露方法（供父组件调用） =============
  defineExpose({
    queryData,
  });

  // =========== 生命周期 =============
  onMounted(() => {
    queryData();
  });
</script>

<style scoped lang="less">
  .component-wrapper {
    // 样式
  }
</style>
```

### 6.2 组件代码组织顺序
1. **导入依赖**: Vue API → 第三方库 → 本地模块
2. **Props 定义**: 使用 `defineProps`
3. **Emits 定义**: 使用 `defineEmits`
4. **响应式数据**: `ref`、`reactive`
5. **计算属性**: `computed`
6. **监听器**: `watch`、`watchEffect`
7. **方法定义**: 业务逻辑函数
8. **暴露方法**: `defineExpose`（如需要）
9. **生命周期**: `onMounted`、`onUnmounted` 等

### 6.3 Props 定义规范
```javascript
const props = defineProps({
  // 单一类型
  value: Number,
  
  // 多类型
  modelValue: [Number, String, Array],
  
  // 带默认值和验证
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  },
  
  // 对象类型需要工厂函数
  options: {
    type: Object,
    default: () => ({})
  },
  
  // 数组类型需要工厂函数
  list: {
    type: Array,
    default: () => []
  },
  
  // 必填项
  employeeId: {
    type: Number,
    required: true
  }
});
```

### 6.4 双向绑定规范
```javascript
// 使用 v-model
const props = defineProps({
  value: [Number, String]
});

const emit = defineEmits(['update:value']);

const selectValue = ref(props.value);

watch(
  () => props.value,
  (newValue) => {
    selectValue.value = newValue;
  }
);

function onChange(value) {
  emit('update:value', value);
}
```

### 6.5 组件通信方式
- **父→子**: Props
- **子→父**: Emits
- **跨组件**: Pinia Store 或 Provide/Inject
- **事件总线**: 使用 `mitt`

---

## 7. 路由规范

### 7.1 路由配置方式
本项目采用 **动态路由** 方式：
- 登录后从后端获取菜单数据
- 前端根据菜单数据动态构建路由
- 路由信息存储在 `menuList` 中

### 7.2 静态路由配置
`src/router/routers.js`:
```javascript
/*
 * 所有路由入口
 *
 * @Author:    1024创新实验室
 */
import { homeRouters } from './system/home';
import { loginRouters } from './system/login';
import NotFound from '/@/views/system/40X/404.vue';
import NoPrivilege from '/@/views/system/40X/403.vue';

export const routerArray = [
  ...loginRouters,
  ...homeRouters,
  { path: '/:pathMatch(.*)*', name: '404', component: NotFound },
  { path: '/403', name: '403', component: NoPrivilege }
];
```

### 7.3 动态路由构建
`src/router/index.js` 中的 `buildRoutes` 方法：
```javascript
export function buildRoutes(menuRouterList) {
  const routerList = [];
  const modules = import.meta.glob('../views/**/**.vue');

  for (const e of menuRouterList) {
    let route = {
      path: e.path.startsWith('/') ? e.path : `/${e.path}`,
      name: e.menuId.toString(), // 使用 menuId 作为唯一标识
      meta: {
        id: e.menuId.toString(),
        componentName: e.menuId.toString(),
        title: e.menuName,
        icon: e.icon,
        hideInMenu: !e.visibleFlag,
        keepAlive: e.cacheFlag,
        frameFlag: e.frameFlag,
        frameUrl: e.frameUrl,
        renameComponentFlag: false,
      },
      component: modules[`../views${e.component}`]
    };
    routerList.push(route);
  }

  router.addRoute({
    path: '/',
    component: SmartLayout,
    children: routerList,
  });
}
```

### 7.4 路由跳转
```javascript
import { useRouter } from 'vue-router';

const router = useRouter();

// 路径跳转
router.push({ path: '/system/employee' });

// 命名路由跳转
router.push({ name: 'employeeList' });

// 带参数跳转
router.push({ 
  path: '/system/employee/detail', 
  query: { id: 123 }
});

// 返回上一页
router.back();
```

### 7.5 路由守卫
```javascript
// 全局前置守卫（已在 router/index.js 中实现）
router.beforeEach(async (to, from, next) => {
  // 1. 进度条开启
  nProgress.start();
  
  // 2. 验证登录
  const token = localRead(LocalStorageKeyConst.USER_TOKEN);
  if (!token && to.path !== PAGE_PATH_LOGIN) {
    next({ path: PAGE_PATH_LOGIN });
    return;
  }
  
  // 3. 设置 TagNav
  useUserStore().setTagNav(to, from);
  
  // 4. 设置 KeepAlive
  if (to.meta.keepAlive) {
    useUserStore().pushKeepAliveIncludes(to.meta.componentName);
  }
  
  next();
});

// 全局后置守卫
router.afterEach(() => {
  nProgress.done();
});
```

---

## 8. 状态管理规范

### 8.1 Store 结构
使用 Pinia 进行状态管理，按模块划分：
```
store/
├── index.js
└── modules/
    └── system/
        ├── user.js          # 用户信息
        ├── menu.js          # 菜单信息
        ├── app-config.js    # 应用配置
        ├── dict.js          # 数据字典
        └── tag-nav.js       # 标签页导航
```

### 8.2 Store 定义规范
```javascript
/*
 * 用户状态管理
 *
 * @Author:    1024创新实验室
 */
import { defineStore } from 'pinia';
import { localRead, localSave, localRemove } from '/@/utils/local-util';
import localKey from '/@/constants/local-storage-key-const';

export const useUserStore = defineStore({
  id: 'userStore',
  
  // =========== State =============
  state: () => ({
    token: '',
    employeeId: '',
    loginName: '',
    actualName: '',
    avatar: '',
    phone: '',
    departmentId: '',
    departmentName: '',
    menuTree: [],
    menuRouterList: [],
    pointsList: [],
    administratorFlag: false,
    unreadMessageCount: 0,
  }),
  
  // =========== Getters =============
  getters: {
    getToken(state) {
      if (state.token) {
        return state.token;
      }
      return localRead(localKey.USER_TOKEN);
    },
    
    getMenuTree(state) {
      return state.menuTree;
    },
    
    getPointList(state) {
      if (_.isEmpty(state.pointsList)) {
        let localUserPoints = localRead(localKey.USER_POINTS) || '';
        state.pointsList = localUserPoints ? JSON.parse(localUserPoints) : [];
      }
      return state.pointsList;
    },
  },
  
  // =========== Actions =============
  actions: {
    // 设置登录信息
    setUserLoginInfo(data) {
      this.token = data.token;
      this.employeeId = data.employeeId;
      this.loginName = data.loginName;
      this.actualName = data.actualName;
      this.menuTree = buildMenuTree(data.menuList);
      this.menuRouterList = data.menuList.filter((e) => e.path || e.frameUrl);
      this.pointsList = data.menuList.filter(
        (menu) => menu.menuType === MENU_TYPE_ENUM.POINTS.value
      );
      
      // 持久化存储
      localSave(localKey.USER_TOKEN, data.token);
      localSave(localKey.USER_POINTS, JSON.stringify(this.pointsList));
    },
    
    // 退出登录
    logout() {
      this.token = '';
      this.menuList = [];
      this.unreadMessageCount = 0;
      localRemove(localKey.USER_TOKEN);
      localRemove(localKey.USER_POINTS);
      localRemove(localKey.USER_TAG_NAV);
    },
    
    // 查询未读消息数量
    async queryUnreadMessageCount() {
      try {
        let result = await messageApi.queryUnreadCount();
        this.unreadMessageCount = result.data;
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
  },
});
```

### 8.3 使用 Store
```javascript
// 在组件中使用
import { useUserStore } from '/@/store/modules/system/user';

const userStore = useUserStore();

// 访问 state
const token = userStore.token;

// 访问 getters
const menuTree = userStore.getMenuTree;

// 调用 actions
userStore.setUserLoginInfo(data);
userStore.logout();
```

### 8.4 持久化策略
- 使用 `localStorage` 存储需要持久化的数据
- 封装 `local-util.js` 统一管理本地存储
- 所有 key 定义在 `local-storage-key-const.js` 中

```javascript
// 本地存储工具
import { localRead, localSave, localRemove, localClear } from '/@/utils/local-util';
import LocalStorageKeyConst from '/@/constants/local-storage-key-const';

// 保存
localSave(LocalStorageKeyConst.USER_TOKEN, token);

// 读取
const token = localRead(LocalStorageKeyConst.USER_TOKEN);

// 删除
localRemove(LocalStorageKeyConst.USER_TOKEN);

// 清空
localClear();
```

---

## 9. 常量与枚举规范

### 9.1 常量文件结构
```
constants/
├── business/              # 业务常量
│   ├── erp/
│   ├── oa/
│   └── message/
├── support/              # 支撑常量
│   ├── dict-const.js
│   ├── file-const.js
│   └── job-const.js
├── system/               # 系统常量
│   ├── employee-const.js
│   ├── menu-const.js
│   └── home-const.js
├── common-const.js       # 通用常量
├── layout-const.js       # 布局常量
├── local-storage-key-const.js  # 本地存储 Key
└── index.js              # 常量入口
```

### 9.2 枚举定义规范
```javascript
/*
 * 员工常量
 *
 * @Author:    1024创新实验室
 */

// 性别枚举
export const GENDER_ENUM = {
  UNKNOWN: {
    value: 0,
    desc: '未知',
  },
  MAN: {
    value: 1,
    desc: '男',
  },
  WOMAN: {
    value: 2,
    desc: '女',
  },
};

// 布尔枚举
export const FLAG_NUMBER_ENUM = {
  TRUE: {
    value: 1,
    desc: '是',
  },
  FALSE: {
    value: 0,
    desc: '否',
  },
};

// 用户类型枚举
export const USER_TYPE_ENUM = {
  ADMIN_EMPLOYEE: {
    value: 1,
    desc: '员工',
  },
};

export default {
  GENDER_ENUM,
  FLAG_NUMBER_ENUM,
  USER_TYPE_ENUM,
};
```

### 9.3 通用常量
`src/constants/common-const.js`:
```javascript
/*
 * 通用常量
 */

// 分页大小
export const PAGE_SIZE = 10;

// 分页大小选项
export const PAGE_SIZE_OPTIONS = [
  '5', '10', '15', '20', '30', '40', '50', 
  '75', '100', '150', '200', '300', '500'
];

// 路径常量
export const PAGE_PATH_LOGIN = '/login';
export const HOME_PAGE_PATH = '/home';
export const PAGE_PATH_404 = '/404';

// 分页总数显示
export const showTableTotal = function (total) {
  return `共${total}条`;
};
```

### 9.4 本地存储 Key 常量
`src/constants/local-storage-key-const.js`:
```javascript
export default {
  // 用户相关
  USER_TOKEN: 'userToken',
  USER_POINTS: 'userPoints',
  USER_TAG_NAV: 'userTagNav',
  
  // 应用配置
  APP_CONFIG: 'appConfig',
  
  // 业务相关
  HOME_QUICK_ENTRY: 'homeQuickEntry',
  NOTICE_READ: 'noticeRead',
  TO_BE_DONE: 'toBeDone',
};
```

### 9.5 枚举插件使用
项目提供了 `smartEnumPlugin` 插件用于枚举值转换：

```javascript
// 在组件中使用
<template>
  <span>{{ $smartEnumPlugin.getDescByValue('GENDER_ENUM', record.gender) }}</span>
</template>

// 在 script 中使用
import { getCurrentInstance } from 'vue';

const { proxy } = getCurrentInstance();
const genderDesc = proxy.$smartEnumPlugin.getDescByValue('GENDER_ENUM', 1);
// 返回: '男'
```

---

## 10. 权限控制规范

### 10.1 权限体系
- **菜单权限**: 控制页面访问
- **功能点权限**: 控制按钮、操作权限
- **超级管理员**: 拥有所有权限

### 10.2 权限指令
`v-privilege` 指令用于控制元素显示：

```vue
<template>
  <!-- 按钮权限控制 -->
  <a-button 
    v-privilege="'system:employee:add'" 
    type="primary" 
    @click="showDrawer"
  >
    添加成员
  </a-button>

  <a-button 
    v-privilege="'system:employee:update'" 
    type="link" 
    @click="showDrawer(record)"
  >
    编辑
  </a-button>

  <a-button 
    v-privilege="'system:employee:delete'" 
    type="link" 
    @click="deleteEmployee(record.employeeId)"
  >
    删除
  </a-button>
</template>
```

### 10.3 权限指令实现
`src/directives/privilege.js`:
```javascript
/*
 * 权限指令
 */
import { useUserStore } from '/@/store/modules/system/user';
import _ from 'lodash';

export function privilegeDirective(el, binding) {
  // 超级管理员拥有所有权限
  if (useUserStore().administratorFlag) {
    return true;
  }
  
  // 获取功能点权限列表
  let userPointsList = useUserStore().getPointList;
  if (!userPointsList) {
    return false;
  }
  
  // 如果没有权限，删除节点
  if (!_.some(userPointsList, ['webPerms', binding.value])) {
    el.parentNode.removeChild(el);
  }
  
  return true;
}
```

### 10.4 权限插件
`src/plugins/privilege-plugin.js` 提供编程式权限检查：

```javascript
// 在组件中使用
import { getCurrentInstance } from 'vue';

const { proxy } = getCurrentInstance();

// 检查是否有权限
const hasPrivilege = proxy.$hasPrivilege('system:employee:add');

if (hasPrivilege) {
  // 执行操作
}
```

### 10.5 权限命名规范
权限点命名格式：`模块:功能:操作`

```
system:employee:add          # 系统-员工-添加
system:employee:update       # 系统-员工-更新
system:employee:delete       # 系统-员工-删除
system:employee:query        # 系统-员工-查询
system:role:menu:update      # 系统-角色-菜单-更新
support:file:upload          # 支撑-文件-上传
business:goods:import        # 业务-商品-导入
```

---

## 11. 样式开发规范

### 11.1 样式组织方式
- 全局样式: `src/theme/index.less`
- 主题变量: `src/theme/custom-variables.js`
- 组件样式: 使用 `<style scoped lang="less">`

### 11.2 Less 变量
`src/theme/custom-variables.js`:
```javascript
export default {
  '@primary-color': '#1677ff',           // 主题色
  '@link-color': '#1677ff',              // 链接色
  '@success-color': '#52c41a',           // 成功色
  '@warning-color': '#faad14',           // 警告色
  '@error-color': '#f5222d',             // 错误色
  '@font-size-base': '14px',             // 基础字号
  '@heading-color': 'rgba(0, 0, 0, 0.85)', // 标题色
  '@text-color': 'rgba(0, 0, 0, 0.65)',  // 主文本色
  '@text-color-secondary': 'rgba(0, 0, 0, 0.45)', // 次文本色
  '@disabled-color': 'rgba(0, 0, 0, 0.25)', // 禁用色
  '@border-radius-base': '6px',          // 组件圆角
  '@border-color-base': '#d9d9d9',       // 边框色
};
```

### 11.3 通用样式类
项目提供了通用工具类：

```html
<!-- 间距类 -->
<div class="smart-margin-left10">左边距 10px</div>
<div class="smart-margin-right10">右边距 10px</div>
<div class="smart-padding10">内边距 10px</div>

<!-- 表格操作栏 -->
<div class="smart-table-operate">
  <a-button type="link">操作1</a-button>
  <a-button type="link">操作2</a-button>
</div>

<!-- 查询表格分页 -->
<div class="smart-query-table-page">
  <a-pagination />
</div>

<!-- 表格列操作 -->
<span class="smart-table-column-operate">
  <TableOperator />
</span>
```

### 11.4 组件样式规范
```vue
<style scoped lang="less">
  // 使用类名包裹，避免样式冲突
  .employee-container {
    // 使用相对单位
    padding: 16px;
    
    // 嵌套层级不超过 3 层
    .header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 16px;
      
      .query-operate {
        display: flex;
        gap: 8px;
      }
    }
    
    // 使用 Less 变量
    .btn-group {
      margin-bottom: @margin-md;
    }
    
    // 响应式设计
    @media (max-width: 768px) {
      .header {
        flex-direction: column;
      }
    }
  }
</style>
```

### 11.5 深度选择器
修改第三方组件样式时使用深度选择器：

```vue
<style scoped lang="less">
  .employee-container {
    // Vue 3 推荐使用 :deep()
    :deep(.ant-table-thead > tr > th) {
      background-color: #fafafa;
    }
    
    // 或使用 ::v-deep
    ::v-deep .ant-btn-primary {
      background-color: #1890ff;
    }
  }
</style>
```

---

## 12. 工具函数规范

### 12.1 本地存储工具
`src/utils/local-util.js`:
```javascript
/*
 * 本地存储工具
 */

// 保存
export function localSave(key, value) {
  localStorage.setItem(key, value);
}

// 读取
export function localRead(key) {
  return localStorage.getItem(key);
}

// 删除
export function localRemove(key) {
  localStorage.removeItem(key);
}

// 清空
export function localClear() {
  localStorage.clear();
}
```

### 12.2 字符串工具
`src/utils/str-util.js`:
```javascript
/*
 * 字符串工具
 */

// 去除空格
export function trim(str) {
  return str.replace(/\s+/g, '');
}

// 首字母大写
export function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

// 下划线转驼峰
export function toCamelCase(str) {
  return str.replace(/_([a-z])/g, (match, letter) => letter.toUpperCase());
}
```

### 12.3 Loading 工具
`src/components/framework/smart-loading/index.js`:
```javascript
/*
 * Loading 工具
 */
import { message } from 'ant-design-vue';

export const SmartLoading = {
  show(text = '加载中...') {
    return message.loading(text, 0);
  },
  
  hide(messageInstance) {
    if (messageInstance) {
      messageInstance();
    }
  },
};

// 使用方式
const loading = SmartLoading.show('数据加载中...');
// 执行异步操作
SmartLoading.hide(loading);
```

### 12.4 错误监控
`src/lib/smart-sentry.js`:
```javascript
/*
 * 错误监控工具
 */
export const smartSentry = {
  // 捕获错误
  captureError(error) {
    console.error('Error captured:', error);
    // 这里可以集成 Sentry 等错误监控服务
  },
  
  // 捕获消息
  captureMessage(message) {
    console.log('Message captured:', message);
  },
};

// 使用方式
try {
  // 业务逻辑
} catch (e) {
  smartSentry.captureError(e);
}
```

### 12.5 加密工具
`src/lib/encrypt.js`:
```javascript
/*
 * 加密解密工具
 */
import CryptoJS from 'crypto-js';
import { sm2, sm4 } from 'sm-crypto';

// AES 加密
export function encryptData(data) {
  const key = CryptoJS.enc.Utf8.parse('your-secret-key');
  const encrypted = CryptoJS.AES.encrypt(
    JSON.stringify(data), 
    key, 
    { mode: CryptoJS.mode.ECB }
  );
  return encrypted.toString();
}

// AES 解密
export function decryptData(encryptedData) {
  const key = CryptoJS.enc.Utf8.parse('your-secret-key');
  const decrypted = CryptoJS.AES.decrypt(
    encryptedData, 
    key, 
    { mode: CryptoJS.mode.ECB }
  );
  return decrypted.toString(CryptoJS.enc.Utf8);
}

// 国密 SM2 加密
export function sm2Encrypt(data, publicKey) {
  return sm2.doEncrypt(data, publicKey);
}

// 国密 SM2 解密
export function sm2Decrypt(encryptedData, privateKey) {
  return sm2.doDecrypt(encryptedData, privateKey);
}
```

---

## 13. 命名规范

### 13.1 文件命名
- **组件文件**: 使用 kebab-case（短横线分隔）
  ```
  employee-list.vue
  employee-form-modal.vue
  department-tree-select.vue
  ```

- **工具文件**: 使用 kebab-case + `-util` 或 `-api` 后缀
  ```
  local-util.js
  str-util.js
  employee-api.js
  login-api.js
  ```

- **常量文件**: 使用 kebab-case + `-const` 后缀
  ```
  common-const.js
  employee-const.js
  local-storage-key-const.js
  ```

- **组件目录**: 使用 kebab-case，index.vue 作为入口
  ```
  components/
  └── employee-select/
      └── index.vue
  ```

### 13.2 变量命名
```javascript
// 普通变量：小驼峰
const employeeList = ref([]);
const currentUser = reactive({});

// 布尔变量：is/has/can/should 开头
const isLoading = ref(false);
const hasPermission = ref(true);
const canEdit = computed(() => true);

// 常量：全大写下划线
const PAGE_SIZE = 10;
const API_BASE_URL = 'https://api.example.com';

// 私有变量：下划线开头（不推荐，ES6 模块已有作用域）
const _privateVar = 'private';

// 枚举：全大写下划线 + _ENUM 后缀
const GENDER_ENUM = { ... };
const USER_TYPE_ENUM = { ... };
```

### 13.3 函数命名
```javascript
// 查询：query/get/fetch
function queryEmployeeList() {}
function getUserInfo() {}
function fetchData() {}

// 添加：add/create
function addEmployee() {}
function createOrder() {}

// 更新：update/modify/edit
function updateEmployee() {}
function modifyStatus() {}

// 删除：delete/remove
function deleteEmployee() {}
function removeItem() {}

// 事件处理：on/handle 开头
function onSubmit() {}
function handleClick() {}
function onChange() {}

// 显示/隐藏：show/hide/toggle
function showModal() {}
function hideDialog() {}
function toggleMenu() {}

// 验证：validate/check
function validateForm() {}
function checkPermission() {}

// 初始化：init/initialize
function initData() {}
function initialize() {}

// 格式化：format
function formatDate() {}
function formatCurrency() {}

// 计算：calculate/compute
function calculateTotal() {}
function computeScore() {}
```

### 13.4 组件命名
```javascript
// 组件名：大驼峰（PascalCase）
import EmployeeList from './employee-list.vue';
import DepartmentTreeSelect from './department-tree-select.vue';

// 注册组件：使用大驼峰或 kebab-case
export default {
  components: {
    EmployeeList,
    DepartmentTreeSelect,
  }
}

// 使用组件：推荐 kebab-case
<template>
  <employee-list />
  <department-tree-select />
</template>
```

### 13.5 CSS 类命名
使用 BEM 命名规范（Block-Element-Modifier）：

```html
<!-- Block -->
<div class="employee-container">
  <!-- Element -->
  <div class="employee-container__header">
    <h2 class="employee-container__title">员工列表</h2>
  </div>
  
  <!-- Element with Modifier -->
  <div class="employee-container__content employee-container__content--loading">
    内容区域
  </div>
</div>

<style scoped lang="less">
  .employee-container {
    // Block 样式
    
    &__header {
      // Element 样式
    }
    
    &__title {
      // Element 样式
    }
    
    &__content {
      // Element 样式
      
      &--loading {
        // Modifier 样式
      }
    }
  }
</style>
```

---

## 14. 注释规范

### 14.1 文件头注释
所有文件必须包含文件头注释（见第 4.1 节）。

### 14.2 函数注释
```javascript
/**
 * 查询员工列表
 * 
 * @param {Object} params - 查询参数
 * @param {Number} params.pageNum - 页码
 * @param {Number} params.pageSize - 每页条数
 * @param {String} params.keyword - 关键字
 * @returns {Promise} 返回员工列表数据
 * @author 卓大
 */
async function queryEmployeeList(params) {
  try {
    let res = await employeeApi.queryEmployee(params);
    return res.data;
  } catch (e) {
    smartSentry.captureError(e);
    return [];
  }
}

/**
 * 重置密码
 * 
 * @param {Number} employeeId - 员工ID
 * @param {String} loginName - 登录名
 */
function resetPassword(employeeId, loginName) {
  Modal.confirm({
    title: '重置密码',
    content: `确定要重置【${loginName}】的密码吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await employeeApi.resetPassword(employeeId);
        message.success('密码重置成功');
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
  });
}
```

### 14.3 块注释
```javascript
// =========== 属性定义 和 事件方法暴露 =============
const props = defineProps({ ... });
const emit = defineEmits(['update:value', 'change']);

// =========== 响应式数据 =============
const dataList = ref([]);
const loading = ref(false);

// =========== 方法定义 =============
async function queryData() { ... }
function onChange() { ... }

// =========== 生命周期 =============
onMounted(() => { ... });
```

### 14.4 行内注释
```javascript
// 超级管理员拥有所有权限
if (useUserStore().administratorFlag) {
  return true;
}

// TODO: 待优化性能
function heavyComputation() {
  // ...
}

// FIXME: 修复已知 bug
function buggyFunction() {
  // ...
}

// NOTE: 重要说明
const IMPORTANT_VALUE = 100; // 单位：元
```

### 14.5 复杂逻辑注释
```javascript
/**
 * 构建菜单树
 * 
 * 处理流程：
 * 1. 过滤出有效的目录和菜单（排除功能点、隐藏菜单、禁用菜单）
 * 2. 获取顶级目录（parentId === 0）
 * 3. 递归构建子菜单
 * 
 * @param {Array} menuList - 菜单列表
 * @returns {Array} 菜单树
 */
function buildMenuTree(menuList) {
  // 1. 获取所有有效的目录和菜单
  let catalogAndMenuList = menuList.filter(
    (menu) => menu.menuType !== MENU_TYPE_ENUM.POINTS.value 
      && menu.visibleFlag 
      && !menu.disabledFlag
  );

  // 2. 获取顶级目录
  let topCatalogList = catalogAndMenuList.filter(
    (menu) => menu.parentId === 0
  );
  
  // 3. 递归构建子菜单
  for (const topCatalog of topCatalogList) {
    buildMenuChildren(topCatalog, catalogAndMenuList);
  }
  
  return topCatalogList;
}
```

---

## 15. 最佳实践

### 15.1 组件设计原则
1. **单一职责**: 每个组件只负责一个功能
2. **可复用性**: 提取通用逻辑为独立组件
3. **Props 验证**: 严格定义 Props 类型和默认值
4. **事件命名**: 使用 `update:xxx` 支持 v-model
5. **插槽使用**: 提供插槽增强灵活性

```vue
<!-- 好的例子：组件职责单一 -->
<template>
  <a-select
    v-model:value="selectValue"
    :placeholder="placeholder"
    :options="options"
    @change="onChange"
  />
</template>

<!-- 不好的例子：组件职责过多 -->
<template>
  <div>
    <a-select /> <!-- 选择器 -->
    <a-table />  <!-- 表格 -->
    <a-modal />  <!-- 弹窗 -->
  </div>
</template>
```

### 15.2 性能优化
```javascript
// 1. 使用 computed 缓存计算结果
const filteredList = computed(() => {
  return dataList.value.filter(item => item.status === 1);
});

// 2. 使用 v-show 代替 v-if（频繁切换的场景）
<template>
  <div v-show="isVisible">内容</div>
</template>

// 3. 大列表使用虚拟滚动
import { VirtualList } from 'ant-design-vue';

// 4. 图片懒加载
<img v-lazy="imageSrc" />

// 5. 组件懒加载
const EmployeeFormModal = defineAsyncComponent(() => 
  import('./employee-form-modal/index.vue')
);

// 6. 防抖和节流
import { debounce, throttle } from 'lodash';

const debouncedSearch = debounce((keyword) => {
  queryData(keyword);
}, 300);

// 7. KeepAlive 缓存页面
<router-view v-slot="{ Component }">
  <keep-alive :include="keepAliveIncludes">
    <component :is="Component" />
  </keep-alive>
</router-view>
```

### 15.3 错误处理
```javascript
// 1. 统一错误处理
async function queryData() {
  try {
    const res = await employeeApi.queryAll();
    dataList.value = res.data;
  } catch (e) {
    // 记录错误日志
    smartSentry.captureError(e);
    // 不需要手动显示错误信息，axios 拦截器已处理
  } finally {
    loading.value = false;
  }
}

// 2. 表单验证错误
async function onSubmit() {
  try {
    await formRef.value.validate();
    // 提交表单
  } catch (errorInfo) {
    console.log('验证失败:', errorInfo);
  }
}

// 3. 业务逻辑错误
function deleteEmployee(employeeId) {
  if (!employeeId) {
    message.error('员工ID不能为空');
    return;
  }
  
  Modal.confirm({
    title: '确认删除',
    content: '删除后数据不可恢复，确定要删除吗？',
    onOk: async () => {
      try {
        await employeeApi.deleteEmployee(employeeId);
        message.success('删除成功');
        queryData();
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
  });
}
```

### 15.4 代码复用
```javascript
// 1. 使用 Composables（组合式函数）
// src/composables/useTable.js
export function useTable(apiFunction) {
  const dataList = ref([]);
  const loading = ref(false);
  const total = ref(0);
  const params = reactive({
    pageNum: 1,
    pageSize: PAGE_SIZE,
  });

  async function queryData() {
    try {
      loading.value = true;
      const res = await apiFunction(params);
      dataList.value = res.data.list;
      total.value = res.data.total;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  }

  function onPageChange(pageNum, pageSize) {
    params.pageNum = pageNum;
    params.pageSize = pageSize;
    queryData();
  }

  return {
    dataList,
    loading,
    total,
    params,
    queryData,
    onPageChange,
  };
}

// 在组件中使用
import { useTable } from '/@/composables/useTable';

const {
  dataList,
  loading,
  total,
  params,
  queryData,
  onPageChange,
} = useTable(employeeApi.queryEmployee);

onMounted(() => {
  queryData();
});

// 2. 提取公共逻辑为 mixin 或 composable
// src/composables/useModal.js
export function useModal() {
  const visible = ref(false);

  function showModal() {
    visible.value = true;
  }

  function hideModal() {
    visible.value = false;
  }

  return {
    visible,
    showModal,
    hideModal,
  };
}
```

### 15.5 表单开发最佳实践
```vue
<template>
  <a-modal
    v-model:open="visible"
    :title="formModel.employeeId ? '编辑员工' : '添加员工'"
    :width="800"
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
      <a-form-item label="姓名" name="actualName">
        <a-input v-model:value="formModel.actualName" placeholder="请输入姓名" />
      </a-form-item>

      <a-form-item label="性别" name="gender">
        <a-radio-group v-model:value="formModel.gender">
          <a-radio :value="1">男</a-radio>
          <a-radio :value="2">女</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="手机号" name="phone">
        <a-input v-model:value="formModel.phone" placeholder="请输入手机号" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { ref, reactive } from 'vue';
  import { message } from 'ant-design-vue';
  import { employeeApi } from '/@/api/system/employee-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { REGULAR_CONST } from '/@/constants/regular-const';

  const emit = defineEmits(['refresh']);

  const visible = ref(false);
  const formRef = ref();
  
  // 表单数据
  const formModel = reactive({
    employeeId: null,
    actualName: '',
    gender: 1,
    phone: '',
  });

  // 验证规则
  const rules = {
    actualName: [
      { required: true, message: '请输入姓名', trigger: 'blur' },
      { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' },
    ],
    gender: [
      { required: true, message: '请选择性别', trigger: 'change' },
    ],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: REGULAR_CONST.PHONE, message: '手机号格式不正确', trigger: 'blur' },
    ],
  };

  // 显示弹窗
  function showModal(record) {
    visible.value = true;
    if (record) {
      // 编辑
      Object.assign(formModel, record);
    } else {
      // 新增
      resetForm();
    }
  }

  // 重置表单
  function resetForm() {
    formRef.value?.resetFields();
    Object.assign(formModel, {
      employeeId: null,
      actualName: '',
      gender: 1,
      phone: '',
    });
  }

  // 提交表单
  async function onSubmit() {
    try {
      // 验证表单
      await formRef.value.validate();
      
      // 提交数据
      if (formModel.employeeId) {
        await employeeApi.updateEmployee(formModel);
        message.success('更新成功');
      } else {
        await employeeApi.addEmployee(formModel);
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
    }
  }

  // 取消
  function onCancel() {
    visible.value = false;
    resetForm();
  }

  // 暴露方法
  defineExpose({
    showModal,
  });
</script>
```

### 15.6 列表页开发最佳实践
```vue
<template>
  <a-card>
    <!-- 查询表单 -->
    <div class="query-form">
      <a-form layout="inline" :model="queryParams">
        <a-form-item label="姓名">
          <a-input 
            v-model:value="queryParams.keyword" 
            placeholder="姓名/手机号/登录账号" 
            @press-enter="onSearch"
          />
        </a-form-item>
        
        <a-form-item label="状态">
          <a-select 
            v-model:value="queryParams.disabledFlag" 
            placeholder="请选择"
            style="width: 120px"
          >
            <a-select-option :value="undefined">全部</a-select-option>
            <a-select-option :value="false">启用</a-select-option>
            <a-select-option :value="true">禁用</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item>
          <a-button type="primary" @click="onSearch">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
          <a-button @click="onReset" style="margin-left: 8px">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 操作按钮 -->
    <div class="btn-group">
      <a-button 
        type="primary" 
        @click="showFormModal()" 
        v-privilege="'system:employee:add'"
      >
        <template #icon><PlusOutlined /></template>
        添加员工
      </a-button>
      
      <a-button 
        @click="batchDelete" 
        v-privilege="'system:employee:delete'"
        :disabled="selectedRowKeys.length === 0"
      >
        <template #icon><DeleteOutlined /></template>
        批量删除
      </a-button>
    </div>

    <!-- 数据表格 -->
    <a-table
      :row-selection="{
        selectedRowKeys: selectedRowKeys,
        onChange: onSelectChange,
      }"
      :columns="columns"
      :data-source="dataList"
      :loading="loading"
      :pagination="false"
      :scroll="{ x: 1500 }"
      row-key="employeeId"
      size="small"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'disabledFlag'">
          <a-tag :color="record.disabledFlag ? 'error' : 'processing'">
            {{ record.disabledFlag ? '禁用' : '启用' }}
          </a-tag>
        </template>
        
        <template v-else-if="column.dataIndex === 'gender'">
          <span>{{ $smartEnumPlugin.getDescByValue('GENDER_ENUM', record.gender) }}</span>
        </template>
        
        <template v-else-if="column.dataIndex === 'operate'">
          <div class="smart-table-operate">
            <a-button 
              type="link" 
              size="small" 
              @click="showFormModal(record)"
              v-privilege="'system:employee:update'"
            >
              编辑
            </a-button>
            
            <a-button 
              type="link" 
              size="small" 
              @click="deleteRecord(record)"
              v-privilege="'system:employee:delete'"
            >
              删除
            </a-button>
          </div>
        </template>
      </template>
    </a-table>

    <!-- 分页 -->
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

    <!-- 表单弹窗 -->
    <EmployeeFormModal 
      ref="formModalRef" 
      @refresh="queryData" 
    />
  </a-card>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { 
    SearchOutlined, 
    ReloadOutlined, 
    PlusOutlined, 
    DeleteOutlined 
  } from '@ant-design/icons-vue';
  import { employeeApi } from '/@/api/system/employee-api';
  import { 
    PAGE_SIZE, 
    PAGE_SIZE_OPTIONS, 
    showTableTotal 
  } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import EmployeeFormModal from './components/employee-form-modal/index.vue';

  // =========== 查询参数 =============
  const queryParams = reactive({
    pageNum: 1,
    pageSize: PAGE_SIZE,
    keyword: '',
    disabledFlag: undefined,
  });

  // =========== 表格数据 =============
  const dataList = ref([]);
  const loading = ref(false);
  const total = ref(0);
  const selectedRowKeys = ref([]);

  // =========== 表格列 =============
  const columns = ref([
    {
      title: '姓名',
      dataIndex: 'actualName',
      width: 100,
    },
    {
      title: '性别',
      dataIndex: 'gender',
      width: 80,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'disabledFlag',
      width: 80,
    },
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
      const res = await employeeApi.queryEmployee(queryParams);
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
      disabledFlag: undefined,
    });
    queryData();
  }

  // =========== 分页 =============
  function onPageChange(pageNum, pageSize) {
    queryParams.pageNum = pageNum;
    queryParams.pageSize = pageSize;
    queryData();
  }

  // =========== 选择 =============
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
      content: `确定要删除【${record.actualName}】吗？`,
      onOk: async () => {
        try {
          await employeeApi.deleteEmployee(record.employeeId);
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
      onOk: async () => {
        try {
          await employeeApi.batchDeleteEmployee(selectedRowKeys.value);
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
  .query-form {
    margin-bottom: 16px;
  }

  .btn-group {
    margin-bottom: 16px;
    
    .ant-btn {
      margin-right: 8px;
    }
  }
</style>
```

### 15.7 代码审查检查清单
- [ ] 所有文件都包含文件头注释
- [ ] 函数都有清晰的注释说明
- [ ] 变量命名符合规范
- [ ] 没有 console.log 等调试代码
- [ ] 错误都使用 smartSentry.captureError 捕获
- [ ] API 调用都使用 try-catch
- [ ] 表单都有验证规则
- [ ] 删除操作都有二次确认
- [ ] 权限控制使用 v-privilege 指令
- [ ] 组件都使用 setup script 语法
- [ ] Props 都定义了类型和默认值
- [ ] 样式使用 scoped 避免污染
- [ ] 没有硬编码的数字和字符串（应使用常量）
- [ ] 图片等资源都放在 assets 目录
- [ ] 路径都使用 `/@/` 别名

---

## 附录

### A. 常用命令

```bash
# 安装依赖
npm install

# 本地开发
npm run localhost  # 使用 localhost 模式
npm run dev        # 使用 dev 模式

# 构建
npm run build:test  # 测试环境构建
npm run build:pre   # 预发布环境构建
npm run build:prod  # 生产环境构建
```

### B. 环境变量

创建 `.env.localhost`、`.env.development`、`.env.test`、`.env.production` 文件：

```bash
# API 地址
VITE_APP_API_URL=http://127.0.0.1:1024

# 应用名称
VITE_APP_NAME=SmartAdmin

# 是否开启加密
VITE_APP_ENCRYPT_ENABLED=true
```

### C. VSCode 推荐插件

```json
{
  "recommendations": [
    "vue.volar",                    // Vue 语言支持
    "dbaeumer.vscode-eslint",       // ESLint
    "esbenp.prettier-vscode",       // Prettier
    "stylelint.vscode-stylelint",   // Stylelint
    "bradlc.vscode-tailwindcss"     // Tailwind CSS（可选）
  ]
}
```

### D. 常见问题

**Q: 如何添加新的业务模块？**

A: 
1. 在 `src/api/business/` 创建 API 文件
2. 在 `src/views/business/` 创建视图文件
3. 在后端添加菜单数据（会自动构建路由）
4. 在 `src/constants/business/` 创建常量文件（如需要）

**Q: 如何修改主题颜色？**

A: 修改 `src/theme/custom-variables.js` 中的 `@primary-color`

**Q: 如何禁用某个页面的 KeepAlive？**

A: 在后端菜单数据中将 `cacheFlag` 设置为 `false`

**Q: 如何添加全局组件？**

A: 在 `src/main.js` 中注册：
```javascript
app.component('GlobalComponent', GlobalComponent);
```

---

## 结语

本规范文档旨在帮助 AI 和开发者更好地理解 SmartAdmin 项目的架构和编码规范，确保代码质量和一致性。

在进行二次开发时，请务必遵循本规范，保持代码风格的统一。如有任何疑问或建议，欢迎与团队沟通。

**记住**: 
- ✅ 保持代码简洁、可读
- ✅ 遵循单一职责原则
- ✅ 重视错误处理和用户体验
- ✅ 编写清晰的注释
- ✅ 做好代码复用

祝开发顺利！🚀

---

**文档版权**: © 2012-2025 1024创新实验室  
**官网**: https://smartadmin.1024lab.net  
**GitHub**: https://github.com/1024-lab/smart-admin

