# UniApp开发规范（权威文档）

> **📋 文档版本**: v4.0.0 (整合版)
> **📋 文档职责**: SmartAdmin项目的唯一移动端开发规范权威来源，基于UniApp 3.0.0和uView Plus 3.2.7，贴合项目实际情况。

## ⚠️ UniApp开发铁律（不可违反）

### 🚫 绝对禁止
```markdown
❌ 禁止使用非H5的uni API（保持跨平台兼容性）
❌ 禁止直接操作DOM（使用Vue数据驱动）
❌ 禁止使用window、document等浏览器API
❌ 禁止缺少错误边界和异常处理
❌ 禁止使用setTimeout替代uni.showToast等uni API
❌ 禁止在页面中直接发起网络请求（使用统一封装）
❌ 禁止缺少加载状态和空状态处理
❌ 禁止不进行页面性能优化
```

### ✅ 必须执行
```markdown
✅ 必须使用Vue 3 Composition API
✅ 必须使用TypeScript进行类型检查
✅ 必须使用uView Plus UI组件库
✅ 必须使用统一封装的网络请求库
✅ 必须实现页面加载状态管理
✅ 必须处理网络错误和异常情况
✅ 必须进行页面性能优化
✅ 必须适配暗黑模式和多端样式
```

## 🛠️ 项目结构规范

### 标准目录结构
```
src/
├── api/                    # API接口
│   ├── index.ts           # API统一入口
│   ├── auth.ts            # 认证相关API
│   ├── user.ts            # 用户相关API
│   └── common.ts          # 公共API
├── assets/                # 静态资源
│   ├── images/            # 图片资源
│   ├── icons/             # 图标资源
│   └── fonts/             # 字体资源
├── components/            # 公共组件
│   ├── common/            # 通用组件
│   ├── business/          # 业务组件
│   └── form/              # 表单组件
├── composables/           # 组合式函数
│   ├── useAuth.ts         # 认证相关
│   ├── useRequest.ts      # 网络请求
│   ├── useStorage.ts      # 本地存储
│   └── usePermission.ts   # 权限控制
├── pages/                 # 页面
│   ├── index/             # 首页
│   ├── login/             # 登录页
│   ├── profile/           # 个人中心
│   └── business/          # 业务页面
├── router/                # 路由配置
│   ├── index.ts           # 路由入口
│   └── modules/           # 路由模块
├── store/                 # 状态管理
│   ├── index.ts           # Store入口
│   ├── modules/           # Store模块
│   └── types.ts           # Store类型定义
├── styles/                # 样式文件
│   ├── index.scss         # 全局样式
│   ├── variables.scss     # 样式变量
│   └── mixins.scss        # 样式混入
├── types/                 # 类型定义
│   ├── api.ts             # API类型
│   ├── common.ts          # 公共类型
│   └── user.ts            # 用户类型
├── utils/                 # 工具函数
│   ├── index.ts           # 工具入口
│   ├── request.ts         # 网络请求
│   ├── storage.ts         # 存储工具
│   └── validate.ts        # 验证工具
├── App.vue                # 应用入口
├── main.ts                # 主入口文件
└── manifest.json          # 应用配置
```

## 🎨 UI组件规范

### uView Plus配置
```typescript
// main.ts
import { createSSRApp } from 'vue'
import App from './App.vue'
import uViewPlus from 'uview-plus'

export function createApp() {
  const app = createSSRApp(App)

  // 注册uView Plus
  app.use(uViewPlus)

  // 注册全局组件
  app.use(registerComponents)

  // 注册全局属性
  app.use(registerGlobalProperties)

  return {
    app
  }
}
```

### 页面模板规范
```vue
<template>
  <view class="user-profile-page">
    <!-- 状态栏占位 -->
    <u-status-bar />

    <!-- 导航栏 -->
    <u-navbar
      :title="pageTitle"
      :border="false"
      :placeholder="true"
      bgColor="#ffffff"
      leftIcon="arrow-left"
      @leftClick="handleBack"
    />

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 加载状态 -->
      <template v-if="loading">
        <u-loading-page
          :loading="true"
          loading-text="加载中..."
        />
      </template>

      <!-- 空状态 -->
      <template v-else-if="!userInfo">
        <u-empty
          mode="data"
          icon="/static/images/empty.png"
          text="暂无数据"
        />
      </template>

      <!-- 正常内容 -->
      <template v-else>
        <view class="profile-header">
          <u-avatar
            :src="userInfo.avatarUrl"
            size="80"
            @click="handleAvatarClick"
          />
          <view class="user-info">
            <text class="user-name">{{ userInfo.userName }}</text>
            <text class="user-email">{{ userInfo.email }}</text>
          </view>
        </view>

        <view class="profile-menu">
          <u-cell-group>
            <u-cell
              v-for="item in menuItems"
              :key="item.id"
              :title="item.title"
              :icon="item.icon"
              :isLink="true"
              @click="handleMenuClick(item)"
            />
          </u-cell-group>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { useRequest } from '@/composables/useRequest'
import type { UserVO, MenuItem } from '@/types'

// 组合式函数
const { userInfo, logout } = useAuth()
const { loading, execute } = useRequest()

// 页面配置
definePage({
  navigationBarTitleText: '个人中心',
  enablePullDownRefresh: true
})

// 响应式数据
const pageTitle = ref('个人中心')
const menuItems = ref<MenuItem[]>([
  {
    id: 'profile',
    title: '个人信息',
    icon: 'account',
    url: '/pages/profile/edit'
  },
  {
    id: 'security',
    title: '账号安全',
    icon: 'lock',
    url: '/pages/profile/security'
  },
  {
    id: 'settings',
    title: '系统设置',
    icon: 'setting',
    url: '/pages/profile/settings'
  }
])

// 计算属性
const hasAvatar = computed(() => {
  return userInfo.value?.avatarUrl && userInfo.value.avatarUrl !== '/static/images/default-avatar.png'
})

// 生命周期
onMounted(() => {
  loadUserData()
})

// 方法
const loadUserData = async () => {
  try {
    await execute('/api/user/current', 'GET')
  } catch (error) {
    uni.showToast({
      title: '加载用户信息失败',
      icon: 'error'
    })
  }
}

const handleBack = () => {
  uni.navigateBack()
}

const handleAvatarClick = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      uploadAvatar(res.tempFilePaths[0])
    }
  })
}

const uploadAvatar = async (filePath: string) => {
  try {
    uni.showLoading({ title: '上传中...' })

    const result = await uni.uploadFile({
      url: '/api/upload/avatar',
      filePath,
      name: 'file',
      header: {
        'Authorization': `Bearer ${getToken()}`
      }
    })

    const data = JSON.parse(result.data)
    if (data.code === 200) {
      userInfo.value!.avatarUrl = data.data.url
      uni.showToast({
        title: '头像更新成功',
        icon: 'success'
      })
    } else {
      throw new Error(data.message)
    }
  } catch (error) {
    uni.showToast({
      title: '头像上传失败',
      icon: 'error'
    })
  } finally {
    uni.hideLoading()
  }
}

const handleMenuClick = (item: MenuItem) => {
  uni.navigateTo({
    url: item.url
  })
}

// 下拉刷新
onPullDownRefresh(() => {
  loadUserData().finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.user-profile-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-content {
  padding: 20rpx;
}

.profile-header {
  display: flex;
  align-items: center;
  padding: 40rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);

  .user-info {
    margin-left: 30rpx;
    flex: 1;

    .user-name {
      display: block;
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 10rpx;
    }

    .user-email {
      font-size: 28rpx;
      color: #999;
    }
  }
}

.profile-menu {
  background-color: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}
</style>
```

## 🔄 组合式函数规范

### 认证组合式函数
```typescript
// composables/useAuth.ts
import { ref, computed } from 'vue'
import { useStorage } from './useStorage'
import { useRequest } from './useRequest'
import type { UserVO, LoginForm, RegisterForm } from '@/types'

const tokenStorage = useStorage('token')
const userInfoStorage = useStorage('userInfo')

export function useAuth() {
  // 响应式状态
  const token = ref<string>(tokenStorage.get() || '')
  const userInfo = ref<UserVO | null>(userInfoStorage.get() || null)
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  // 网络请求
  const { loading, execute } = useRequest()

  // 登录
  const login = async (form: LoginForm): Promise<boolean> => {
    try {
      const response = await execute('/api/auth/login', 'POST', form)

      if (response.code === 200) {
        const { token: newToken, userInfo: newUserInfo } = response.data

        // 保存到本地存储
        tokenStorage.set(newToken)
        userInfoStorage.set(newUserInfo)

        // 更新响应式状态
        token.value = newToken
        userInfo.value = newUserInfo

        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })

        return true
      } else {
        throw new Error(response.message)
      }
    } catch (error) {
      uni.showToast({
        title: error.message || '登录失败',
        icon: 'error'
      })
      return false
    }
  }

  // 注册
  const register = async (form: RegisterForm): Promise<boolean> => {
    try {
      const response = await execute('/api/auth/register', 'POST', form)

      if (response.code === 200) {
        uni.showToast({
          title: '注册成功',
          icon: 'success'
        })

        // 注册成功后自动登录
        return await login({
          userName: form.userName,
          password: form.password
        })
      } else {
        throw new Error(response.message)
      }
    } catch (error) {
      uni.showToast({
        title: error.message || '注册失败',
        icon: 'error'
      })
      return false
    }
  }

  // 登出
  const logout = () => {
    // 清除本地存储
    tokenStorage.remove()
    userInfoStorage.remove()

    // 清除响应式状态
    token.value = ''
    userInfo.value = null

    // 跳转到登录页
    uni.reLaunch({
      url: '/pages/login/index'
    })
  }

  // 获取用户信息
  const getUserInfo = async (): Promise<boolean> => {
    try {
      const response = await execute('/api/user/current', 'GET')

      if (response.code === 200) {
        userInfo.value = response.data
        userInfoStorage.set(response.data)
        return true
      } else {
        throw new Error(response.message)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return false
    }
  }

  // 更新用户信息
  const updateUserInfo = (newUserInfo: Partial<UserVO>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...newUserInfo }
      userInfoStorage.set(userInfo.value)
    }
  }

  // 检查登录状态
  const checkLoginStatus = (): boolean => {
    if (!token.value) {
      return false
    }

    // 验证token是否有效
    getUserInfo().catch(() => {
      logout()
    })

    return true
  }

  return {
    // 状态
    token,
    userInfo,
    isLoggedIn,
    loading,

    // 方法
    login,
    register,
    logout,
    getUserInfo,
    updateUserInfo,
    checkLoginStatus
  }
}

// 便捷获取token
export function getToken(): string {
  return uni.getStorageSync('token') || ''
}
```

### 网络请求组合式函数
```typescript
// composables/useRequest.ts
import { ref } from 'vue'
import type { ResponseDTO } from '@/types'

interface RequestConfig {
  baseURL?: string
  timeout?: number
  header?: Record<string, string>
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  showLoading?: boolean
  loadingText?: string
  showError?: boolean
}

export function useRequest(config: RequestConfig = {}) {
  // 响应式状态
  const loading = ref(false)
  const error = ref<Error | null>(null)

  // 默认配置
  const defaultConfig: RequestConfig = {
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 30000,
    header: {
      'Content-Type': 'application/json'
    }
  }

  // 合并配置
  const finalConfig = { ...defaultConfig, ...config }

  // 请求拦截器
  const requestInterceptor = (options: RequestOptions) => {
    // 添加token
    const token = getToken()
    if (token) {
      options.header = {
        ...options.header,
        'Authorization': `Bearer ${token}`
      }
    }

    // 添加设备信息
    options.header = {
      ...options.header,
      'X-Client-Platform': uni.getSystemInfoSync().platform,
      'X-Client-Version': import.meta.env.VITE_APP_VERSION
    }

    return options
  }

  // 响应拦截器
  const responseInterceptor = (response: any) => {
    const { statusCode, data } = response

    // HTTP状态码检查
    if (statusCode !== 200) {
      throw new Error(`请求失败: ${statusCode}`)
    }

    // 业务状态码检查
    if (data.code === 401) {
      // token过期，跳转登录
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.reLaunch({
        url: '/pages/login/index'
      })
      throw new Error('登录已过期')
    }

    if (data.code !== 200) {
      throw new Error(data.message || '请求失败')
    }

    return data
  }

  // 统一请求方法
  const request = async <T = any>(
    url: string,
    options: RequestOptions = {}
  ): Promise<ResponseDTO<T>> => {
    try {
      loading.value = true
      error.value = null

      // 应用请求拦截器
      const finalOptions = requestInterceptor(options)

      // 显示加载状态
      if (finalOptions.showLoading !== false) {
        uni.showLoading({
          title: finalOptions.loadingText || '加载中...',
          mask: true
        })
      }

      // 发起请求
      const response = await uni.request({
        url: finalConfig.baseURL + url,
        method: finalOptions.method || 'GET',
        data: finalOptions.data,
        header: {
          ...finalConfig.header,
          ...finalOptions.header
        },
        timeout: finalConfig.timeout
      })

      // 应用响应拦截器
      const result = responseInterceptor(response)

      return result

    } catch (err) {
      error.value = err as Error

      // 显示错误信息
      if (options.showError !== false) {
        uni.showToast({
          title: err.message || '网络错误',
          icon: 'error'
        })
      }

      throw err
    } finally {
      loading.value = false
      uni.hideLoading()
    }
  }

  // 便捷方法
  const get = <T = any>(url: string, data?: any, options?: RequestOptions) => {
    return request<T>(url, { ...options, method: 'GET', data })
  }

  const post = <T = any>(url: string, data?: any, options?: RequestOptions) => {
    return request<T>(url, { ...options, method: 'POST', data })
  }

  const put = <T = any>(url: string, data?: any, options?: RequestOptions) => {
    return request<T>(url, { ...options, method: 'PUT', data })
  }

  const del = <T = any>(url: string, data?: any, options?: RequestOptions) => {
    return request<T>(url, { ...options, method: 'DELETE', data })
  }

  // 文件上传
  const upload = async <T = any>(
    url: string,
    filePath: string,
    options: {
      name?: string
      formData?: Record<string, any>
      header?: Record<string, string>
      showLoading?: boolean
      loadingText?: string
    } = {}
  ): Promise<ResponseDTO<T>> => {
    try {
      loading.value = true
      error.value = null

      if (options.showLoading !== false) {
        uni.showLoading({
          title: options.loadingText || '上传中...',
          mask: true
        })
      }

      const response = await uni.uploadFile({
        url: finalConfig.baseURL + url,
        filePath,
        name: options.name || 'file',
        formData: options.formData,
        header: {
          ...finalConfig.header,
          ...options.header,
          'Authorization': `Bearer ${getToken()}`
        }
      })

      const data = JSON.parse(response.data)
      return responseInterceptor({ statusCode: response.statusCode, data })

    } catch (err) {
      error.value = err as Error

      uni.showToast({
        title: err.message || '上传失败',
        icon: 'error'
      })

      throw err
    } finally {
      loading.value = false
      uni.hideLoading()
    }
  }

  return {
    loading,
    error,
    request,
    get,
    post,
    put,
    del,
    upload,
    execute: request // 别名，用于向后兼容
  }
}
```

### 本地存储组合式函数
```typescript
// composables/useStorage.ts
export function useStorage<T = any>(key: string) {

  // 获取数据
  const get = (): T | null => {
    try {
      const value = uni.getStorageSync(key)
      return value ? JSON.parse(value) : null
    } catch (error) {
      console.error('获取存储数据失败:', error)
      return null
    }
  }

  // 设置数据
  const set = (value: T): boolean => {
    try {
      uni.setStorageSync(key, JSON.stringify(value))
      return true
    } catch (error) {
      console.error('设置存储数据失败:', error)
      return false
    }
  }

  // 移除数据
  const remove = (): boolean => {
    try {
      uni.removeStorageSync(key)
      return true
    } catch (error) {
      console.error('移除存储数据失败:', error)
      return false
    }
  }

  // 清空所有存储
  const clear = (): boolean => {
    try {
      uni.clearStorageSync()
      return true
    } catch (error) {
      console.error('清空存储数据失败:', error)
      return false
    }
  }

  return {
    get,
    set,
    remove,
    clear
  }
}
```

## 🎯 页面开发规范

### 列表页面模板
```vue
<template>
  <view class="list-page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <u-search
        v-model="searchKeyword"
        placeholder="请输入搜索关键词"
        :show-action="false"
        @search="handleSearch"
        @clear="handleClear"
      />
    </view>

    <!-- 筛选器 -->
    <view class="filter-bar">
      <u-tabs
        :list="tabList"
        v-model="currentTab"
        @change="handleTabChange"
      />
    </view>

    <!-- 列表内容 -->
    <view class="list-content">
      <!-- 加载状态 -->
      <template v-if="loading && dataList.length === 0">
        <u-skeleton
          :loading="true"
          avatar
          :rows="3"
        />
      </template>

      <!-- 空状态 -->
      <template v-else-if="dataList.length === 0 && !loading">
        <u-empty
          mode="list"
          icon="/static/images/empty-list.png"
          text="暂无数据"
        />
      </template>

      <!-- 数据列表 -->
      <template v-else>
        <view
          v-for="item in dataList"
          :key="item.id"
          class="list-item"
          @click="handleItemClick(item)"
        >
          <view class="item-content">
            <view class="item-title">{{ item.title }}</view>
            <view class="item-desc">{{ item.description }}</view>
            <view class="item-time">{{ formatTime(item.createTime) }}</view>
          </view>
          <u-icon name="arrow-right" color="#c8c9cc" />
        </view>

        <!-- 加载更多 -->
        <u-loadmore
          v-if="dataList.length > 0"
          :status="loadMoreStatus"
          @loadmore="loadMore"
        />
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRequest } from '@/composables/useRequest'
import { usePagination } from '@/composables/usePagination'
import type { ListItem } from '@/types'

// 页面配置
definePage({
  navigationBarTitleText: '列表页面',
  enablePullDownRefresh: true,
  onReachBottomDistance: 100
})

// 响应式数据
const searchKeyword = ref('')
const currentTab = ref(0)
const tabList = ref([
  { name: '全部' },
  { name: '进行中' },
  { name: '已完成' }
])

// 分页管理
const {
  dataList,
  loading,
  finished,
  loadMoreStatus,
  refresh,
  loadMore,
  search
} = usePagination<ListItem>('/api/list', {
  defaultParams: {
    pageSize: 20
  },
  transform: (data) => {
    return data.records || []
  }
})

// 计算属性
const currentStatus = computed(() => {
  const statusMap = ['all', 'processing', 'completed']
  return statusMap[currentTab.value]
})

// 生命周期
onMounted(() => {
  loadData()
})

// 方法
const loadData = () => {
  search({
    keyword: searchKeyword.value,
    status: currentStatus.value
  })
}

const handleSearch = () => {
  refresh({
    keyword: searchKeyword.value,
    status: currentStatus.value
  })
}

const handleClear = () => {
  searchKeyword.value = ''
  handleSearch()
}

const handleTabChange = () => {
  refresh({
    keyword: searchKeyword.value,
    status: currentStatus.value
  })
}

const handleItemClick = (item: ListItem) => {
  uni.navigateTo({
    url: `/pages/detail/index?id=${item.id}`
  })
}

const formatTime = (time: string) => {
  // 格式化时间显示
  return time.split(' ')[0]
}

// 下拉刷新
onPullDownRefresh(() => {
  refresh().finally(() => {
    uni.stopPullDownRefresh()
  })
})

// 触底加载
onReachBottom(() => {
  if (!finished.value && !loading.value) {
    loadMore()
  }
})
</script>

<style lang="scss" scoped>
.list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.search-bar {
  padding: 20rpx;
  background-color: #ffffff;
}

.filter-bar {
  background-color: #ffffff;
  border-bottom: 1rpx solid #eee;
}

.list-content {
  padding: 20rpx;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background-color: #ffffff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);

  .item-content {
    flex: 1;
    margin-right: 20rpx;

    .item-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 10rpx;
    }

    .item-desc {
      font-size: 28rpx;
      color: #666;
      margin-bottom: 10rpx;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .item-time {
      font-size: 24rpx;
      color: #999;
    }
  }
}
</style>
```

## 📱 多端适配规范

### 条件编译
```typescript
// utils/platform.ts
export const platform = {
  isH5: process.env.UNI_PLATFORM === 'h5',
  isApp: process.env.UNI_PLATFORM === 'app',
  isMP: process.env.UNI_PLATFORM.startsWith('mp-'),
  isWeChat: process.env.UNI_PLATFORM === 'mp-weixin',
  isAlipay: process.env.UNI_PLATFORM === 'mp-alipay',
  isiOS: uni.getSystemInfoSync().platform === 'ios',
  isAndroid: uni.getSystemInfoSync().platform === 'android'
}

// 平台相关工具
export const platformUtils = {
  // 复制到剪贴板
  copyToClipboard(text: string) {
    // #ifdef H5
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = text
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    // #endif

    // #ifndef H5
    uni.setClipboardData({
      data: text,
      success: () => {
        uni.showToast({
          title: '复制成功',
          icon: 'success'
        })
      }
    })
    // #endif
  },

  // 拨打电话
  makePhoneCall(phoneNumber: string) {
    // #ifdef H5
    window.location.href = `tel:${phoneNumber}`
    // #endif

    // #ifndef H5
    uni.makePhoneCall({
      phoneNumber
    })
    // #endif
  },

  // 打开地图
  openLocation(latitude: number, longitude: number, name: string) {
    // #ifdef H5
    window.open(`https://uri.amap.com/navigation?to=${name},${latitude},${longitude}`)
    // #endif

    // #ifndef H5
    uni.openLocation({
      latitude,
      longitude,
      name,
      success: () => {
        console.log('打开地图成功')
      },
      fail: (error) => {
        console.error('打开地图失败:', error)
      }
    })
    // #endif
  }
}
```

### 响应式样式
```scss
// styles/responsive.scss
// 基准尺寸
$base-width: 750; // 设计稿基准宽度

// 响应式px转换函数
@function rpx($px) {
  @return ($px / $base-width) * 1rpx;
}

// 媒体查询断点
$breakpoints: (
  xs: 0,
  sm: 576,
  md: 768,
  lg: 992,
  xl: 1200
);

// 响应式混入
@mixin respond-to($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media screen and (min-width: map-get($breakpoints, $breakpoint) / $base-width * 100vw) {
      @content;
    }
  }
}

// 使用示例
.container {
  width: rpx(750);
  padding: rpx(20);

  @include respond-to(sm) {
    padding: rpx(30);
  }

  @include respond-to(md) {
    max-width: rpx(600);
    margin: 0 auto;
  }
}

// 暗黑模式适配
@media (prefers-color-scheme: dark) {
  .page {
    background-color: #1a1a1a;
    color: #ffffff;
  }

  .card {
    background-color: #2a2a2a;
    border-color: #3a3a3a;
  }
}
```

## 🚀 性能优化规范

### 图片优化
```vue
<template>
  <view class="image-container">
    <!-- 懒加载图片 -->
    <u-lazy-load
      :src="imageSrc"
      :threshold="200"
      loading-img="/static/images/loading.png"
      error-img="/static/images/error.png"
      @load="handleImageLoad"
      @error="handleImageError"
    />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  src: string
  width?: number
  height?: number
  mode?: 'scaleToFill' | 'aspectFit' | 'aspectFill'
}

const props = withDefaults(defineProps<Props>(), {
  width: 200,
  height: 200,
  mode: 'aspectFill'
})

// 图片压缩处理
const imageSrc = computed(() => {
  if (!props.src) return '/static/images/placeholder.png'

  // 根据平台和设备像素比选择合适的图片质量
  const dpr = uni.getSystemInfoSync().pixelRatio || 1
  const quality = dpr > 2 ? '80' : '90'

  // 如果是CDN图片，添加质量参数
  if (props.src.includes('cdn.')) {
    return `${props.src}?x-oss-process=image/quality,q_${quality}`
  }

  return props.src
})

const handleImageLoad = () => {
  console.log('图片加载成功')
}

const handleImageError = () => {
  console.log('图片加载失败')
}
</script>

<style lang="scss" scoped>
.image-container {
  overflow: hidden;
  border-radius: 8rpx;

  :deep(.u-lazy-load) {
    width: 100%;
    height: 100%;
    transition: opacity 0.3s ease;

    &.loaded {
      opacity: 1;
    }

    &.loading {
      opacity: 0.5;
    }
  }
}
</style>
```

### 长列表优化
```vue
<template>
  <view class="virtual-list">
    <u-list
      @scrolltolower="loadMore"
      :show-scrollbar="false"
    >
      <u-list-item
        v-for="item in visibleItems"
        :key="item.id"
      >
        <slot name="item" :item="item" :index="item.index" />
      </u-list-item>
    </u-list>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'

interface Props {
  items: any[]
  itemHeight: number
  containerHeight: number
  bufferSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  bufferSize: 5
})

const scrollTop = ref(0)
const startIndex = ref(0)
const endIndex = ref(0)

// 计算可见项目
const visibleItems = computed(() => {
  const start = Math.max(0, startIndex.value - props.bufferSize)
  const end = Math.min(
    props.items.length - 1,
    endIndex.value + props.bufferSize
  )

  return props.items.slice(start, end + 1).map((item, index) => ({
    ...item,
    index: start + index
  }))
})

// 更新可见范围
const updateVisibleRange = () => {
  const start = Math.floor(scrollTop.value / props.itemHeight)
  const visibleCount = Math.ceil(props.containerHeight / props.itemHeight)

  startIndex.value = start
  endIndex.value = start + visibleCount
}

// 滚动事件
const handleScroll = (e: any) => {
  scrollTop.value = e.detail.scrollTop
  updateVisibleRange()
}

// 加载更多
const loadMore = () => {
  // 触发加载更多事件
}

// 监听滚动位置变化
watch(scrollTop, updateVisibleRange)

onMounted(() => {
  updateVisibleRange()
})
</script>

<style lang="scss" scoped>
.virtual-list {
  height: v-bind('props.containerHeight + "rpx"');
  overflow: hidden;
}

:deep(.u-list) {
  height: 100%;
}
</style>
```

---

**🎯 核心原则**：
1. **跨平台优先** - 保持H5、App、小程序的兼容性
2. **性能优化** - 懒加载、虚拟列表、图片优化
3. **用户体验** - 加载状态、错误处理、交互反馈
4. **组件化开发** - 可复用的组件和组合式函数
5. **类型安全** - TypeScript类型检查和规范

**📖 相关文档**：
- [API规范](./API规范.md) - 后端API接口规范
- [安全规范](./安全规范.md) - 移动端安全规范
- [编码规范](./编码规范.md) - 前端编码规范