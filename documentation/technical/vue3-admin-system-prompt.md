# Vue3 B端管理系统标准化提示词

## 项目概述

请创建一个基于Vue 3 + JavaScript + Vite + Ant Design Vue的现代化B端管理系统，具备完整的用户认证、系统管理和数据展示功能。

## 技术栈要求

### 核心技术栈
- **前端框架**: Vue 3.4.27 + JavaScript (非TypeScript)
- **构建工具**: Vite 5.2.12
- **UI组件库**: Ant Design Vue 4.2.5
- **图标库**: @ant-design/icons-vue 7.0.1
- **路由管理**: Vue Router 4.3.2
- **状态管理**: Pinia 2.1.7
- **样式预处理**: Less 4.2.0
- **HTTP请求**: Axios 1.6.8
- **图表库**: ECharts 5.4.3
- **富文本编辑器**: @wangeditor-next/editor 5.6.34
- **国际化**: Vue I18n 9.13.1
- **工具库**: Lodash 4.17.21 + Day.js 1.11.13

### 开发工具配置
- **代码检查**: ESLint 8.16.0 + Prettier 3.0.2
- **样式检查**: Stylelint 14.8.5
- **进度条**: NProgress 0.2.0
- **加密工具**: CryptoJS 4.1.1 + sm-crypto 0.3.13
- **其他工具**: clipboard 2.0.11, uuid 11.1.0, sortablejs 1.15.0

## 项目结构要求

```
src/
├── api/                    # API接口
│   ├── business/          # 业务接口
│   ├── support/           # 支撑系统接口
│   └── system/            # 系统管理接口
├── assets/                # 静态资源
│   └── images/           # 图片资源
├── components/            # 通用组件
│   ├── business/         # 业务组件
│   ├── framework/        # 框架组件
│   ├── support/          # 支撑组件
│   └── system/           # 系统组件
├── config/               # 配置文件
│   └── app-config.js     # 应用默认配置
├── constants/            # 常量定义
│   ├── business/         # 业务常量
│   ├── support/          # 支撑常量
│   ├── system/           # 系统常量
│   ├── common-const.js   # 公共常量
│   ├── layout-const.js   # 布局常量
│   └── index.js          # 常量入口
├── directives/           # 自定义指令
│   └── privilege.js      # 权限指令
├── i18n/                 # 国际化
│   ├── index.js          # i18n入口
│   └── lang/             # 语言包
├── layout/               # 布局组件
│   ├── components/       # 布局子组件
│   ├── index.vue         # 布局入口
│   ├── side-layout.vue   # 侧边栏布局
│   ├── top-layout.vue    # 顶部菜单布局
│   ├── side-expand-layout.vue  # 侧边展开布局
│   └── top-expand-layout.vue   # 顶部展开布局
├── lib/                  # 工具库
│   ├── axios.js          # Axios封装
│   ├── encrypt.js        # 加密工具
│   ├── smart-watermark.js  # 水印工具
│   └── table-auto-height.js # 表格自适应
├── plugins/              # 插件
│   ├── dict-plugin.js    # 字典插件
│   ├── privilege-plugin.js  # 权限插件
│   └── smart-enums-plugin.js # 枚举插件
├── router/               # 路由
│   ├── index.js          # 路由入口
│   ├── routers.js        # 静态路由
│   ├── support/          # 支撑系统路由
│   └── system/           # 系统管理路由
├── store/                # 状态管理
│   ├── index.js          # Store入口
│   └── modules/system/   # 系统模块
├── theme/                # 主题样式
│   ├── index.less        # 样式入口
│   ├── color.js          # 颜色配置
│   └── custom-variables.js  # 自定义变量
├── utils/                # 工具函数
│   ├── local-util.js     # 本地存储工具
│   └── str-util.js       # 字符串工具
├── views/                # 页面组件
│   ├── business/         # 业务页面
│   ├── support/          # 支撑系统页面
│   └── system/           # 系统管理页面
├── App.vue              # 根组件
└── main.js              # 应用入口
```

## 核心配置文件

### package.json 脚本配置
```json
{
  "scripts": {
    "localhost": "vite --mode localhost",
    "dev": "vite",
    "build:test": "vite build --base=/admin/ --mode test",
    "build:pre": "vite build --mode pre",
    "build:prod": "vite build --mode production"
  },
  "type": "module"
}
```

### vite.config.js 配置
```javascript
import { resolve } from 'path';
import vue from '@vitejs/plugin-vue';
import customVariables from '/@/theme/custom-variables.js';

const pathResolve = (dir) => {
  return resolve(__dirname, '.', dir);
};

export default {
  base: process.env.NODE_ENV === 'production' ? '/' : '/',
  root: process.cwd(),
  resolve: {
    alias: [
      {
        find: 'vue-i18n',
        replacement: 'vue-i18n/dist/vue-i18n.cjs.js',
      },
      {
        find: /\/@\//,
        replacement: pathResolve('src') + '/',
      },
      {
        find: /^~/,
        replacement: '',
      },
    ],
  },
  server: {
    host: '0.0.0.0',
    port: 8081,
  },
  plugins: [vue()],
  optimizeDeps: {
    include: ['ant-design-vue/es/locale/zh_CN', 'dayjs/locale/zh-cn'],
    exclude: ['vue-demi'],
  },
  build: {
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
    rollupOptions: {
      output: {
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]',
        manualChunks(id) {
          if (id.includes('node_modules')) {
            return id.toString().split('node_modules/')[1].split('/')[0].toString();
          }
        },
      },
    },
    target: 'esnext',
    outDir: 'dist',
    assetsDir: 'assets',
    minify: 'terser',
  },
  css: {
    preprocessorOptions: {
      less: {
        modifyVars: customVariables,
        javascriptEnabled: true,
      },
    },
  },
  define: {
    __INTLIFY_PROD_DEVTOOLS__: false,
    'process.env': process.env,
  },
};
```

### jsconfig.json 路径别名配置
```json
{
  "compilerOptions": {
    "target": "ES6",
    "jsx": "preserve",
    "module": "commonjs",
    "allowSyntheticDefaultImports": true,
    "baseUrl": "./",
    "paths": {
      "/@/*": ["src/*"]
    }
  },
  "exclude": ["node_modules"]
}
```

## 应用架构设计

### 主应用入口 (main.js)

**初始化逻辑**:
```javascript
// 1. 优先获取Token
let token = localRead(LocalStorageKeyConst.USER_TOKEN);

// 2. 根据Token状态决定初始化流程
if (!token) {
  // 无Token: 直接初始化Vue，仅渲染登录页
  await initVue();
} else {
  // 有Token: 先获取用户信息和菜单，再初始化Vue
  await getLoginInfo();
}
```

**关键特性**:
- 动态路由构建（根据后端返回菜单）
- 用户信息状态管理（Pinia）
- 数据字典预加载
- 全局组件和插件注册
- 权限指令注入

### 应用根组件 (App.vue)

**核心功能**:
```vue
<template>
  <a-config-provider
    :locale="antdLocale"
    :theme="{
      algorithm: themeAlgorithm,
      token: {
        colorPrimary: themeColors[colorIndex].primaryColor,
        borderRadius: borderRadius,
      }
    }"
  >
    <a-spin :spinning="spinning" tip="稍等片刻，我在拼命加载中...">
      <RouterView />
    </a-spin>
  </a-config-provider>
</template>
```

**主题配置**:
- 支持暗黑模式切换
- 支持紧凑模式
- 支持多主题色切换（10种预设颜色）
- 支持圆角自定义
- 全局loading状态

### 布局系统架构

**布局模式** (4种):
1. **side**: 左侧菜单布局
2. **side-expand**: 左侧展开菜单布局
3. **top**: 顶部菜单布局
4. **top-expand**: 顶部展开菜单布局

**布局切换**:
```vue
<template>
  <SideLayout v-if="layout === 'side'" />
  <SideExpandLayout v-if="layout === 'side-expand'" />
  <TopLayout v-if="layout === 'top'" />
  <TopExpandLayout v-if="layout === 'top-expand'" />
</template>
```

### 侧边栏布局 (side-layout.vue)

**布局结构**:
```vue
<a-layout class="admin-layout" style="min-height: 100%">
  <!-- 1. 侧边菜单 -->
  <a-layout-sider
    :width="sideMenuWidth"
    v-model:collapsed="collapsed"
    :theme="theme"
  >
    <SideMenu :collapsed="collapsed" />
  </a-layout-sider>

  <!-- 2. 主体区域 -->
  <a-layout class="admin-layout-main">
    <!-- 2.1 顶部头部 -->
    <a-layout-header class="layout-header">
      <a-row class="layout-header-user">
        <!-- 左侧：折叠按钮 + 首页按钮 + 面包屑/标签页 -->
        <a-col class="layout-header-left">
          <span class="collapsed-button">
            <menu-unfold-outlined v-if="collapsed" @click="() => (collapsed = !collapsed)" />
            <menu-fold-outlined v-else @click="() => (collapsed = !collapsed)" />
          </span>
          <span class="home-button" @click="goHome">
            <home-outlined />
          </span>
          <span class="location-breadcrumb">
            <PageTag v-if="pageTagLocation === 'top'" />
            <MenuLocationBreadcrumb />
          </span>
        </a-col>
        <!-- 右侧：用户操作区 -->
        <a-col class="layout-header-right">
          <HeaderUserSpace />
        </a-col>
      </a-row>
      <PageTag v-if="pageTagLocation === 'center'" />
    </a-layout-header>

    <!-- 2.2 内容区域 -->
    <a-layout-content class="admin-layout-content">
      <!-- iframe页面 -->
      <IframeIndex v-if="iframeNotKeepAlivePageFlag" />
      
      <!-- 常规页面 -->
      <div class="admin-content">
        <router-view v-slot="{ Component }">
          <keep-alive :include="keepAliveIncludes">
            <component :is="Component" :key="route.name" />
          </keep-alive>
        </router-view>
      </div>
    </a-layout-content>

    <!-- 2.3 页脚 -->
    <a-layout-footer class="layout-footer" v-show="footerFlag">
      <smart-footer />
    </a-layout-footer>
  </a-layout>

  <!-- 3. 右侧帮助文档（可选） -->
  <a-layout-sider v-if="helpDocFlag" v-show="helpDocExpandFlag">
    <SideHelpDoc />
  </a-layout-sider>
</a-layout>
```

**样式规范**:
```less
// 顶部头部高度
@header-user-height: 40px;

// 侧边菜单样式
.side-menu {
  height: 100vh;
  overflow-x: hidden;
  overflow-y: scroll;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
}

// 内容区域样式
.admin-layout-content {
  background-color: inherit;
  padding: 5px 10px 0px 10px;
  height: calc(100% - 计算高度);
}
```

### 侧边菜单组件

**Logo区域**:
```vue
<template>
  <!-- 展开状态 -->
  <div class="logo" @click="onGoHome" v-if="!collapsed">
    <img class="logo-img" :src="logoImg" />
    <div class="title" :class="sideMenuTheme === 'light' ? 'title-light' : 'title-dark'">
      {{ websiteName }}
    </div>
  </div>
  
  <!-- 折叠状态 -->
  <div class="min-logo" @click="onGoHome" v-if="collapsed">
    <img class="logo-img" :src="logoImg" />
  </div>

  <!-- 菜单区域：递归菜单 -->
  <div class="menu">
    <RecursionMenu :collapsed="collapsed" ref="menuRef" />
  </div>
</template>
```

**样式配置**:
```less
.logo {
  height: @header-user-height;
  line-height: @header-user-height;
  padding: 0px 15px;
  position: fixed;
  z-index: 21;
  display: flex;
  cursor: pointer;
  justify-content: center;
  align-items: center;
  
  .logo-img {
    width: 30px;
    height: 30px;
  }
  
  .title {
    font-size: 16px;
    font-weight: 600;
    margin-left: 8px;
  }
  
  .title-light {
    color: #001529;
  }
  
  .title-dark {
    color: #ffffff;
  }
}
```

### 标签页系统 (PageTag)

**支持3种风格**:
```javascript
// layout-const.js
export const PAGE_TAG_ENUM = {
  DEFAULT: { value: 'default', label: '默认风格' },
  ANTD: { value: 'antd', label: 'Ant Design风格' },
  CHROME: { value: 'chrome', label: 'Chrome风格' },
};
```

**标签页功能**:
- 标签拖拽排序
- 标签右键菜单（关闭当前、关闭其他、关闭全部）
- 标签双击关闭
- 标签页缓存
- 刷新当前标签页
- 固定标签页

**标签页位置**:
- `top`: 标题栏内
- `center`: 标题栏下方独立一行

## 路由系统设计

### 动态路由构建

**核心逻辑**:
```javascript
// router/index.js
export function buildRoutes(menuRouterList) {
  const routerList = [];
  const modules = import.meta.glob('../views/**/**.vue');
  
  for (const e of menuList) {
    let route = {
      path: e.path.startsWith('/') ? e.path : `/${e.path}`,
      name: e.menuId.toString(),
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
    };
    
    // iframe路由
    if (e.frameFlag) {
      route.component = () => import('../components/framework/iframe/iframe-index.vue');
    } else {
      // 普通路由
      let componentPath = e.component.startsWith('/') ? e.component : '/' + e.component;
      let relativePath = `../views${componentPath}`;
      route.component = modules[relativePath];
    }
    
    routerList.push(route);
  }
  
  // 添加到路由
  router.addRoute({
    path: '/',
    component: SmartLayout,
    children: routerList,
  });
}
```

### 路由守卫

**前置守卫**:
```javascript
router.beforeEach(async (to, from, next) => {
  nProgress.start();
  
  // 1. 公共页面直接放行
  if (to.path === '/404') {
    next();
    return;
  }
  
  // 2. 验证登录
  const token = localRead(LocalStorageKeyConst.USER_TOKEN);
  if (!token) {
    if (to.path === '/login') {
      next();
    } else {
      next({ path: '/login' });
    }
    return;
  }
  
  // 3. 已登录访问登录页，跳转首页
  if (to.path === '/login') {
    next({ path: '/home' });
    return;
  }
  
  // 4. 设置页面缓存和标签页
  if (to.meta.keepAlive) {
    useUserStore().pushKeepAliveIncludes(to.meta.componentName);
  }
  useUserStore().setTagNav(to, from);
  
  next();
});
```

## 页面设计规范

### 登录页面 (login.vue)

**页面布局**:
```vue
<template>
  <div class="login-container">
    <!-- 左侧：介绍区域 -->
    <div class="box-item desc">
      <div class="welcome">
        <p>欢迎登录 SmartAdmin V3</p>
        <p class="desc">产品介绍文案</p>
      </div>
      <div class="app-qr-box">
        <!-- 二维码区域 -->
      </div>
    </div>
    
    <!-- 右侧：登录表单 -->
    <div class="box-item login">
      <img class="login-qr" :src="loginQR" />
      <div class="login-title">账号登录</div>
      
      <a-form ref="formRef" :model="loginForm" :rules="rules">
        <a-form-item name="loginName">
          <a-input v-model:value.trim="loginForm.loginName" placeholder="请输入用户名" />
        </a-form-item>
        
        <a-form-item name="password">
          <a-input-password v-model:value="loginForm.password" placeholder="请输入密码" />
        </a-form-item>
        
        <a-form-item name="captchaCode">
          <a-input v-model:value.trim="loginForm.captchaCode" placeholder="请输入验证码" />
          <img class="captcha-img" :src="captchaBase64Image" @click="getCaptcha" />
        </a-form-item>
        
        <a-form-item>
          <a-checkbox v-model:checked="rememberPwd">记住密码</a-checkbox>
          <span>(账号：admin, 密码：123456)</span>
        </a-form-item>
        
        <a-form-item>
          <div class="btn" @click="onLogin">登录</div>
        </a-form-item>
      </a-form>
      
      <!-- 其他登录方式 -->
      <div class="more">
        <div class="title-box">
          <p class="line"></p>
          <p class="title">其他方式登录</p>
          <p class="line"></p>
        </div>
        <div class="login-type">
          <img :src="wechatIcon" />
          <img :src="aliIcon" />
          <!-- 更多图标... -->
        </div>
      </div>
    </div>
  </div>
</template>
```

**登录逻辑**:
```javascript
async function onLogin() {
  formRef.value.validate().then(async () => {
    SmartLoading.show();
    try {
      // 1. 密码加密
      let encryptPasswordForm = Object.assign({}, loginForm, {
        password: encryptData(loginForm.password),
      });
      
      // 2. 登录请求
      const res = await loginApi.login(encryptPasswordForm);
      
      // 3. 保存Token
      localSave(LocalStorageKeyConst.USER_TOKEN, res.data.token);
      
      // 4. 更新用户信息
      useUserStore().setUserLoginInfo(res.data);
      
      // 5. 初始化数据字典
      const dictRes = await dictApi.getAllDictData();
      useDictStore().initData(dictRes.data);
      
      // 6. 构建动态路由
      buildRoutes();
      
      // 7. 跳转首页
      router.push('/home');
      message.success('登录成功');
    } catch (e) {
      if (e.data && e.data.code !== 0) {
        loginForm.captchaCode = '';
        getCaptcha();
      }
    } finally {
      SmartLoading.hide();
    }
  });
}
```

### 首页 (home/index.vue)

**页面布局**:
```vue
<template>
  <!-- 顶部用户信息 -->
  <a-row>
    <HomeHeader />
  </a-row>
  
  <!-- 主体内容区域 -->
  <a-row :gutter="[10, 10]">
    <!-- 左侧：公告、图表 -->
    <a-col :span="16">
      <a-row :gutter="[10, 10]">
        <a-col :span="12">
          <HomeNotice title="公告" :noticeTypeId="1" />
        </a-col>
        <a-col :span="12">
          <HomeNotice title="通知" :noticeTypeId="2" />
        </a-col>
        <a-col :span="12">
          <Pie />
        </a-col>
        <a-col :span="12">
          <Category />
        </a-col>
        <a-col :span="24">
          <Gradient />
        </a-col>
      </a-row>
    </a-col>
    
    <!-- 右侧：快捷入口、更新日志、待办 -->
    <a-col :span="8">
      <a-row :gutter="[10, 10]">
        <a-col :span="24">
          <OfficialAccountCard />
        </a-col>
        <a-col :span="24">
          <ChangelogCard />
        </a-col>
        <a-col :span="24">
          <ToBeDoneCard />
        </a-col>
      </a-row>
    </a-col>
  </a-row>
</template>
```

**布局特点**:
- 左右分栏布局 (16:8)
- 内部使用嵌套栅格
- 统一间距 `gutter="[10, 10]"`
- 组件化设计，模块独立

### 员工管理页面 (employee/index.vue)

**页面结构**:
```vue
<template>
  <div class="height100">
    <a-row :gutter="16" class="height100">
      <!-- 左侧：部门树 -->
      <a-col :span="6">
        <DepartmentTree ref="departmentTree" />
      </a-col>
      
      <!-- 右侧：员工列表 -->
      <a-col :span="18" class="height100">
        <div class="employee-box height100">
          <EmployeeList :departmentId="selectedDepartmentId" />
        </div>
      </a-col>
    </a-row>
  </div>
</template>
```

### 员工列表组件 (employee-list/index.vue)

**列表布局**:
```vue
<template>
  <a-card class="employee-container">
    <!-- 1. 顶部筛选区 -->
    <div class="header">
      <a-typography-title :level="5">部门人员</a-typography-title>
      <div class="query-operate">
        <a-radio-group v-model:value="params.disabledFlag" @change="queryEmployee">
          <a-radio-button :value="undefined">全部</a-radio-button>
          <a-radio-button :value="false">启用</a-radio-button>
          <a-radio-button :value="true">禁用</a-radio-button>
        </a-radio-group>
        <a-input-search
          v-model:value.trim="params.keyword"
          placeholder="姓名/手机号/登录账号"
          @search="queryEmployee"
        />
        <a-button @click="reset">
          <template #icon><ReloadOutlined /></template>
          重置
        </a-button>
      </div>
    </div>
    
    <!-- 2. 操作按钮区 -->
    <div class="btn-group">
      <a-button type="primary" @click="showDrawer" v-privilege="'system:employee:add'">
        添加成员
      </a-button>
      <a-button @click="updateEmployeeDepartment" v-privilege="'system:employee:department:update'">
        调整部门
      </a-button>
      <a-button @click="batchDelete" v-privilege="'system:employee:delete'">
        批量删除
      </a-button>
      
      <!-- 表格操作栏 -->
      <span class="smart-table-column-operate">
        <TableOperator v-model="columns" :tableId="TABLE_ID" :refresh="queryEmployee" />
      </span>
    </div>
    
    <!-- 3. 数据表格 -->
    <a-table
      :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
      size="small"
      :columns="columns"
      :data-source="tableData"
      :loading="tableLoading"
      :scroll="{ x: 1500 }"
      :pagination="false"
      row-key="employeeId"
      bordered
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'disabledFlag'">
          <a-tag :color="text ? 'error' : 'processing'">
            {{ text ? '禁用' : '启用' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'operate'">
          <div class="smart-table-operate">
            <a-button type="link" size="small" @click="showDrawer(record)">编辑</a-button>
            <a-button type="link" size="small" @click="resetPassword(record)">重置密码</a-button>
            <a-button type="link" @click="updateDisabled(record)">
              {{ record.disabledFlag ? '启用' : '禁用' }}
            </a-button>
          </div>
        </template>
      </template>
    </a-table>
    
    <!-- 4. 分页组件 -->
    <div class="smart-query-table-page">
      <a-pagination
        showSizeChanger
        showQuickJumper
        show-less-items
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        v-model:current="params.pageNum"
        v-model:pageSize="params.pageSize"
        :total="total"
        @change="queryEmployee"
        :show-total="showTableTotal"
      />
    </div>
  </a-card>
</template>
```

**表格列配置**:
```javascript
const columns = ref([
  {
    title: '姓名',
    dataIndex: 'actualName',
    width: 85,
  },
  {
    title: '性别',
    dataIndex: 'gender',
    width: 70,
  },
  {
    title: '登录账号',
    dataIndex: 'loginName',
    width: 100,
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    width: 85,
  },
  {
    title: '状态',
    dataIndex: 'disabledFlag',
    width: 60,
  },
  {
    title: '操作',
    dataIndex: 'operate',
    width: 140,
  },
]);
```

### 角色管理页面 (role/index.vue)

**左右分栏布局**:
```vue
<template>
  <div class="height100">
    <a-row :gutter="10" type="flex" class="height100">
      <!-- 左侧：角色列表 -->
      <a-col flex="200px">
        <RoleList ref="roleList" />
      </a-col>
      
      <!-- 右侧：角色配置 -->
      <a-col flex="1" class="role-setting">
        <RoleSetting />
      </a-col>
    </a-row>
  </div>
</template>
```

## 核心组件封装

### 表格操作栏 (TableOperator)

**功能特性**:
- 全屏/取消全屏
- 刷新表格
- 列设置（显示/隐藏、拖拽排序、固定列）
- 列配置保存到后端

**使用示例**:
```vue
<TableOperator 
  v-model="columns" 
  :tableId="TABLE_ID_CONST.SYSTEM.EMPLOYEE" 
  :refresh="queryEmployee" 
/>
```

### 文件上传组件 (FileUpload)

**组件特性**:
```vue
<FileUpload
  :multiple="true"
  :maxUploadSize="10"
  :maxSize="10"
  :accept="'.jpg,.jpeg,.png,.gif'"
  :folder="FILE_FOLDER_TYPE_ENUM.COMMON.value"
  :listType="'picture-card'"
  :defaultFileList="fileList"
  @change="handleFileChange"
/>
```

**功能特性**:
- 支持单文件/多文件上传
- 文件类型限制
- 文件大小限制
- 文件数量限制
- 图片预览
- 文件下载
- 三种展示风格：`text`、`picture`、`picture-card`

### 字典选择组件 (DictSelect)

**使用示例**:
```vue
<DictSelect
  v-model:value="formData.status"
  dictCode="USER_STATUS"
  placeholder="请选择状态"
  width="200px"
  size="default"
  :disabled="false"
  :disabledOption="['DELETED']"
  :hiddenOption="['ARCHIVED']"
  @change="handleChange"
/>
```

**功能特性**:
- 自动从字典系统获取数据
- 支持单选/多选模式
- 支持禁用选项
- 支持隐藏选项
- 支持自定义宽度和尺寸

### 权限指令 (v-privilege)

**使用方式**:
```vue
<a-button v-privilege="'system:employee:add'">添加</a-button>
<a-button v-privilege="'system:employee:delete'">删除</a-button>
```

**实现原理**:
```javascript
// directives/privilege.js
export function privilegeDirective(el, binding) {
  const { value } = binding;
  const pointsList = useUserStore().getPointList;
  
  if (value && value instanceof Array && value.length > 0) {
    const hasPrivilege = pointsList.some((point) => value.includes(point.webPerms));
    if (!hasPrivilege) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  } else {
    throw new Error(`need roles! Like v-privilege="['admin','editor']"`);
  }
}
```

## 状态管理规范

### 用户状态 (user.js)

**状态定义**:
```javascript
export const useUserStore = defineStore({
  id: 'userStore',
  state: () => ({
    token: '',
    employeeId: '',
    avatar: '',
    loginName: '',
    actualName: '',
    phone: '',
    departmentId: '',
    departmentName: '',
    administratorFlag: false,
    menuTree: [],
    menuRouterList: [],
    pointsList: [],
    tagNav: null,
    keepAliveIncludes: [],
    unreadMessageCount: 0,
  }),
  getters: {
    getToken(state) {
      return state.token || localRead(localKey.USER_TOKEN);
    },
    getMenuTree(state) {
      return state.menuTree;
    },
    getPointList(state) {
      return state.pointsList;
    },
  },
  actions: {
    setUserLoginInfo(data) {
      // 设置用户基本信息
      this.token = data.token;
      this.employeeId = data.employeeId;
      // 构建菜单树
      this.menuTree = buildMenuTree(data.menuList);
      // 功能点列表
      this.pointsList = data.menuList.filter(menu => menu.menuType === MENU_TYPE_ENUM.POINTS.value);
    },
    logout() {
      this.token = '';
      this.menuList = [];
      this.tagNav = [];
      localRemove(localKey.USER_TOKEN);
    },
    setTagNav(route, from) {
      // 标签页逻辑
    },
  },
});
```

### 应用配置状态 (app-config.js)

**默认配置**:
```javascript
export const appDefaultConfig = {
  language: 'zh_CN',                    // 语言
  layout: 'side',                       // 布局模式
  sideMenuWidth: 200,                   // 侧边菜单宽度
  sideMenuTheme: 'dark',                // 菜单主题: dark/light
  pageTagLocation: 'center',            // 标签页位置: top/center
  pageTagStyle: 'chrome',               // 标签页风格: default/antd/chrome
  darkModeFlag: false,                  // 暗黑模式
  colorIndex: 0,                        // 主题色索引
  borderRadius: 6,                      // 圆角
  menuSingleExpandFlag: true,           // 菜单单一展开
  pageTagFlag: true,                    // 显示标签页
  breadCrumbFlag: true,                 // 显示面包屑
  footerFlag: true,                     // 显示页脚
  helpDocFlag: true,                    // 显示帮助文档
  watermarkFlag: true,                  // 显示水印
  websiteName: 'SmartAdmin 3.X',        // 网站名称
  compactFlag: false,                   // 紧凑模式
};
```

### 字典状态 (dict.js)

**字典管理**:
```javascript
export const useDictStore = defineStore({
  id: 'dictStore',
  state: () => ({
    dictDataMap: new Map(),
  }),
  actions: {
    initData(dictData) {
      this.dictDataMap = new Map();
      dictData.forEach(item => {
        if (!this.dictDataMap.has(item.keyCode)) {
          this.dictDataMap.set(item.keyCode, []);
        }
        this.dictDataMap.get(item.keyCode).push(item);
      });
    },
    getDictData(dictCode) {
      return this.dictDataMap.get(dictCode) || [];
    },
  },
});
```

## HTTP请求封装

### Axios配置 (lib/axios.js)

**基础配置**:
```javascript
const smartAxios = axios.create({
  baseURL: import.meta.env.VITE_APP_API_URL,
});
```

**请求拦截器**:
```javascript
smartAxios.interceptors.request.use(
  (config) => {
    const token = localRead(LocalStorageKeyConst.USER_TOKEN);
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
```

**响应拦截器**:
```javascript
smartAxios.interceptors.response.use(
  (response) => {
    const res = response.data;
    
    // 统一错误处理
    if (res.code && res.code !== 1) {
      // Token过期
      if (res.code === 30007 || res.code === 30008) {
        message.error('您没有登录，请重新登录');
        setTimeout(logout, 300);
        return Promise.reject(response);
      }
      
      message.error(res.msg);
      return Promise.reject(response);
    }
    
    return Promise.resolve(res);
  },
  (error) => {
    if (error.message.indexOf('timeout') !== -1) {
      message.error('网络超时');
    } else if (error.message === 'Network Error') {
      message.error('网络连接错误');
    }
    return Promise.reject(error);
  }
);
```

**请求方法封装**:
```javascript
// GET请求
export const getRequest = (url, params) => {
  return request({ url, method: 'get', params });
};

// POST请求
export const postRequest = (url, data) => {
  return request({ url, method: 'post', data });
};

// 加密POST请求
export const postEncryptRequest = (url, data) => {
  return request({
    url,
    method: 'post',
    data: { encryptData: encryptData(data) },
  });
};

// 文件下载
export const getDownload = function (url, params) {
  request({
    method: 'get',
    url,
    params,
    responseType: 'blob',
  }).then((data) => {
    handleDownloadData(data);
  });
};
```

## 样式和主题规范

### 颜色系统

**主题色配置** (theme/color.js):
```javascript
export const themeColors = [
  { primaryColor: '#1677ff', activeColor: '#0958d9', hoverColor: '#4096ff' }, // 默认蓝
  { primaryColor: '#00b96b', activeColor: '#389e0d', hoverColor: '#52c41a' }, // 绿色
  { primaryColor: '#722ed1', activeColor: '#531dab', hoverColor: '#9254de' }, // 紫色
  { primaryColor: '#eb2f96', activeColor: '#c41d7f', hoverColor: '#f759ab' }, // 粉色
  { primaryColor: '#fa8c16', activeColor: '#d46b08', hoverColor: '#ffa940' }, // 橙色
  { primaryColor: '#13c2c2', activeColor: '#08979c', hoverColor: '#36cfc9' }, // 青色
  { primaryColor: '#faad14', activeColor: '#d48806', hoverColor: '#ffc53d' }, // 黄色
  { primaryColor: '#f5222d', activeColor: '#cf1322', hoverColor: '#ff4d4f' }, // 红色
  { primaryColor: '#52c41a', activeColor: '#389e0d', hoverColor: '#73d13d' }, // 浅绿
  { primaryColor: '#2f54eb', activeColor: '#1d39c4', hoverColor: '#597ef7' }, // 深蓝
];
```

### 自定义变量 (custom-variables.js)

```javascript
export default {
  '@primary-color': token['primary-color'],
  '@base-bg-color': '#fff',
  '@hover-bg-color': 'rgba(0, 0, 0, 0.025)',
  '@header-height': '80px',
  '@header-user-height': '40px',
  '@page-tag-height': '40px',
};
```

### 全局样式 (theme/index.less)

```less
// 基础样式
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  outline: none !important;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  background-color: #f8f8f8;
  font-size: 14px;
}

#app {
  width: 100%;
  height: 100%;
}

// 表格斑马纹
.ant-table-tbody :deep(.smart-table-striped) {
  background-color: #fafafa;
}

// 富文本内容样式
.html-content {
  table {
    border: 1px solid #ccc;
    td, th {
      border: 1px solid #ccc;
      padding: 3px 5px;
    }
  }
  
  blockquote {
    border-left: 8px solid #d0e5f2;
    padding: 5px 10px;
    background-color: #f1f1f1;
  }
  
  code {
    background-color: #f1f1f1;
    border-radius: 3px;
    padding: 3px 5px;
  }
}
```

## 工具函数封装

### 本地存储工具 (utils/local-util.js)

```javascript
// 保存
export function localSave(key, value) {
  localStorage.setItem(key, value);
}

// 读取
export function localRead(key) {
  return localStorage.getItem(key) || '';
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

### 加密工具 (lib/encrypt.js)

```javascript
import CryptoJS from 'crypto-js';
import { sm2 } from 'sm-crypto';

// AES加密
export function encryptData(data) {
  const key = CryptoJS.enc.Utf8.parse('your-secret-key');
  const encrypted = CryptoJS.AES.encrypt(JSON.stringify(data), key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
  });
  return encrypted.toString();
}

// AES解密
export function decryptData(data) {
  const key = CryptoJS.enc.Utf8.parse('your-secret-key');
  const decrypted = CryptoJS.AES.decrypt(data, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
  });
  return decrypted.toString(CryptoJS.enc.Utf8);
}
```

### 水印工具 (lib/smart-watermark.js)

```javascript
export default {
  set(elementId, text) {
    const element = document.getElementById(elementId);
    // 创建水印canvas
    // 设置样式和内容
  },
  clear() {
    // 清除水印
  },
};
```

## 常量定义规范

### 公共常量 (constants/common-const.js)

```javascript
// 分页
export const PAGE_SIZE = 10;
export const PAGE_SIZE_OPTIONS = ['10', '20', '50', '100'];

// 显示分页总数
export function showTableTotal(total, range) {
  return `第 ${range[0]}-${range[1]} 条/共 ${total} 条`;
}

// 标识枚举
export const FLAG_NUMBER_ENUM = {
  TRUE: { value: 1, label: '是' },
  FALSE: { value: 0, label: '否' },
};

// 性别枚举
export const GENDER_ENUM = {
  MAN: { value: 1, label: '男' },
  WOMAN: { value: 2, label: '女' },
};

// 数据类型枚举
export const DATA_TYPE_ENUM = {
  NORMAL: { value: 1, label: '普通' },
  ENCRYPT: { value: 2, label: '加密' },
};
```

### 布局常量 (constants/layout-const.js)

```javascript
// 布局模式
export const LAYOUT_ENUM = {
  SIDE: { value: 'side', label: '左侧菜单' },
  SIDE_EXPAND: { value: 'side-expand', label: '左侧展开菜单' },
  TOP: { value: 'top', label: '顶部菜单' },
  TOP_EXPAND: { value: 'top-expand', label: '顶部展开菜单' },
};

// 标签页风格
export const PAGE_TAG_ENUM = {
  DEFAULT: { value: 'default', label: '默认风格' },
  ANTD: { value: 'antd', label: 'Ant Design风格' },
  CHROME: { value: 'chrome', label: 'Chrome风格' },
};

// 标签页位置
export const PAGE_TAG_LOCATION_ENUM = {
  TOP: { value: 'top', label: '标题栏内' },
  CENTER: { value: 'center', label: '独立一行' },
};
```

### 菜单常量 (constants/system/menu-const.js)

```javascript
// 菜单类型
export const MENU_TYPE_ENUM = {
  CATALOG: { value: 1, label: '目录' },
  MENU: { value: 2, label: '菜单' },
  POINTS: { value: 3, label: '功能点' },
};
```

## 插件系统

### 枚举插件 (plugins/smart-enums-plugin.js)

**安装**:
```javascript
// main.js
import smartEnumPlugin from '/@/plugins/smart-enums-plugin';
app.use(smartEnumPlugin, constantsInfo);
```

**使用**:
```vue
<template>
  <!-- 获取枚举描述 -->
  <span>{{ $smartEnumPlugin.getDescByValue('GENDER_ENUM', 1) }}</span>
  
  <!-- 获取枚举对象 -->
  <a-select v-model:value="gender">
    <a-select-option 
      v-for="item in $smartEnumPlugin.getEnum('GENDER_ENUM')" 
      :key="item.value" 
      :value="item.value"
    >
      {{ item.label }}
    </a-select-option>
  </a-select>
</template>
```

### 字典插件 (plugins/dict-plugin.js)

**使用**:
```vue
<template>
  <!-- 获取字典值 -->
  <span>{{ $dictPlugin.getLabel('USER_STATUS', formData.status) }}</span>
</template>
```

## 国际化配置

### i18n配置 (i18n/index.js)

```javascript
import { createI18n } from 'vue-i18n';
import zh_CN from './lang/zh-CN';
import en_US from './lang/en-US';

export const messages = {
  zh_CN: zh_CN,
  en_US: en_US,
};

const i18n = createI18n({
  locale: 'zh_CN',
  fallbackLocale: 'zh_CN',
  messages,
  legacy: false,
  globalInjection: true,
});

export default i18n;
```

## 开发规范

### 组件命名规范
- 组件文件：PascalCase（如：`UserManagement.vue`）
- 组件注册：PascalCase（如：`<UserManagement />`）
- 组件目录：kebab-case（如：`user-management/`）

### API接口规范
```javascript
// api/system/employee-api.js
export const employeeApi = {
  // 查询员工列表
  queryEmployee: (params) => {
    return postRequest('/system/employee/query', params);
  },
  
  // 添加员工
  addEmployee: (data) => {
    return postRequest('/system/employee/add', data);
  },
  
  // 更新员工
  updateEmployee: (data) => {
    return postRequest('/system/employee/update', data);
  },
  
  // 删除员工
  deleteEmployee: (employeeId) => {
    return getRequest(`/system/employee/delete/${employeeId}`);
  },
  
  // 批量删除
  batchDeleteEmployee: (employeeIdList) => {
    return postRequest('/system/employee/batchDelete', employeeIdList);
  },
};
```

### 页面查询规范
```javascript
// 查询参数
const params = reactive({
  keyword: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: PAGE_SIZE,
  searchCount: true,
});

// 查询方法
const tableLoading = ref(false);
async function queryData() {
  tableLoading.value = true;
  try {
    let res = await someApi.query(params);
    tableData.value = res.data.list;
    total.value = res.data.total;
  } catch (error) {
    smartSentry.captureError(error);
  } finally {
    tableLoading.value = false;
  }
}

// 重置方法
function reset() {
  Object.assign(params, defaultParams);
  queryData();
}
```

### 表单操作规范
```javascript
// 表单引用
const formRef = ref();
const formData = reactive({
  name: '',
  status: undefined,
});

// 表单规则
const rules = {
  name: [
    { required: true, message: '请输入名称' },
    { max: 50, message: '名称长度不能超过50个字符' },
  ],
  status: [
    { required: true, message: '请选择状态' },
  ],
};

// 提交方法
async function handleSubmit() {
  formRef.value.validate().then(async () => {
    SmartLoading.show();
    try {
      await someApi.save(formData);
      message.success('保存成功');
      handleClose();
      emit('refresh');
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      SmartLoading.hide();
    }
  });
}
```

## 性能优化建议

### 路由懒加载
```javascript
// 已在 buildRoutes 中实现
const modules = import.meta.glob('../views/**/**.vue');
route.component = modules[relativePath];
```

### 组件缓存
```vue
<router-view v-slot="{ Component }">
  <keep-alive :include="keepAliveIncludes">
    <component :is="Component" :key="route.name" />
  </keep-alive>
</router-view>
```

### 图片懒加载
```vue
<img v-lazy="imageUrl" />
```

### 表格虚拟滚动
- 大数据量表格使用虚拟滚动
- 分页优先于一次性加载

## 构建和部署

### 环境变量配置

**.env.development**:
```
VITE_APP_API_URL=http://localhost:1024
VITE_APP_TITLE=SmartAdmin开发环境
```

**.env.production**:
```
VITE_APP_API_URL=https://api.example.com
VITE_APP_TITLE=SmartAdmin生产环境
```

### 构建命令
```bash
# 开发环境
npm run dev

# 测试环境构建
npm run build:test

# 预发布环境构建
npm run build:pre

# 生产环境构建
npm run build:prod
```

### 构建优化
- 代码分割（已配置）
- Tree Shaking（Vite自动）
- 静态资源压缩
- Console日志清除（生产环境）

## 常见问题

### 1. 路径别名问题
确保 `vite.config.js` 和 `jsconfig.json` 中的路径别名配置一致：
```javascript
// vite.config.js
{
  find: /\/@\//,
  replacement: pathResolve('src') + '/',
}

// jsconfig.json
{
  "paths": {
    "/@/*": ["src/*"]
  }
}
```

### 2. Less变量注入
在 `vite.config.js` 中配置：
```javascript
css: {
  preprocessorOptions: {
    less: {
      modifyVars: customVariables,
      javascriptEnabled: true,
    },
  },
}
```

### 3. 动态路由刷新404
确保服务器配置了路由回退到 `index.html`

### 4. Keep-Alive不生效
- 确保组件有唯一的 `name` 属性
- 确保 `keepAliveIncludes` 数组中包含组件名
- 确保路由 `meta.keepAlive` 为 `true`

## 执行指令

请严格按照以上规范创建一个完整的Vue 3 B端管理系统，确保:

1. ✅ 所有技术栈版本严格匹配
2. ✅ 项目结构完全一致
3. ✅ 使用JavaScript而非TypeScript
4. ✅ 路径别名使用 `/@/` 格式
5. ✅ 所有功能模块完整实现
6. ✅ 动态路由和权限系统完整
7. ✅ 状态管理使用Pinia
8. ✅ 样式使用Less预处理器
9. ✅ 代码规范和注释完整
10. ✅ 性能优化和构建配置到位

**重要提醒**:
- 项目使用 JavaScript，不使用 TypeScript
- 路径别名统一使用 `/@/` 格式
- 菜单和路由从后端动态获取
- 所有接口调用需要Token认证
- 表格列配置支持保存到后端
- 支持多种布局模式和主题切换

