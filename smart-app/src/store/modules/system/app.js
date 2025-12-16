/**
 * 应用全局状态管理
 * 统一管理应用的基础配置、主题、语言等状态
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { performanceMonitor } from '@/utils/performance.js'

const normalizeBaseUrl = (url) => {
  return url ? url.replace(/\/+$/, '') : ''
}

export const useAppStore = defineStore('app', () => {
  // ========== 基础状态 ==========
  const systemInfo = ref({})
  const networkStatus = ref('unknown')
  const appVersion = ref('1.0.0')
  const buildNumber = ref('100')

  // ========== 主题配置 ==========
  const theme = ref({
    mode: 'light', // light, dark, auto
    primaryColor: '#1890ff',
    fontSize: 'medium', // small, medium, large
    animationEnabled: true
  })

  // ========== 语言配置 ==========
  const locale = ref('zh-CN')

  // ========== 应用配置 ==========
  const config = ref({
    // API配置
    apiBaseUrl: normalizeBaseUrl(import.meta.env.VITE_APP_API_URL),
    apiTimeout: 10000,

    // 缓存配置
    cacheEnabled: true,
    cacheExpireTime: 3600, // 秒

    // 性能配置
    performanceMonitoring: true,
    imageLazyLoad: true,
    dataCompressEnabled: true,

    // 安全配置
    biometricEnabled: false,
    autoLockTime: 300, // 秒
    screenCaptureProtection: false,

    // 功能开关
    debugMode: false,
    logLevel: 'info', // debug, info, warn, error
    crashReportEnabled: true
  })

  // ========== 用户偏好 ==========
  const preferences = ref({
    // 界面偏好
    homePageTabs: ['dashboard', 'quick-actions'],
    sidebarCollapsed: false,
    showNotifications: true,

    // 操作偏好
    autoRefreshInterval: 30, // 秒
    itemsPerPage: 20,
    dateFormat: 'YYYY-MM-DD',
    timeFormat: '24h',

    // 数据偏好
    dataSyncEnabled: true,
    offlineModeEnabled: false,
    backgroundRefreshEnabled: true
  })

  // ========== 应用状态 ==========
  const appStatus = ref({
    isLoading: false,
    isOnline: true,
    isInitialized: false,
    hasUpdate: false,
    maintenanceMode: false
  })

  // ========== 性能数据 ==========
  const performanceData = ref({
    pageLoadTime: 0,
    apiResponseTime: 0,
    memoryUsage: 0,
    errorCount: 0
  })

  // ========== 通知配置 ==========
  const notifications = ref({
    pushEnabled: true,
    soundEnabled: true,
    vibrationEnabled: true,
    badgeEnabled: true,
    channels: {
      system: { enabled: true, sound: true, vibration: true },
      business: { enabled: true, sound: true, vibration: false },
      alert: { enabled: true, sound: true, vibration: true }
    }
  })

  // ========== 计算属性 ==========
  const isDarkMode = computed(() => {
    return theme.value.mode === 'dark' || (theme.value.mode === 'auto' && isDarkTime())
  })

  const isPerformanceGood = computed(() => {
    return performanceData.value.pageLoadTime < 3000 &&
           performanceData.value.apiResponseTime < 2000
  })

  const needsOptimization = computed(() => {
    return performanceData.value.pageLoadTime > 5000 ||
           performanceData.value.memoryUsage > 0.8
  })

  // ========== 方法 ==========

  /**
   * 初始化应用状态
   */
  const initializeApp = async () => {
    try {
      appStatus.value.isLoading = true

      // 获取系统信息
      const systemInfoData = uni.getSystemInfoSync()
      systemInfo.value = systemInfoData

      // 获取网络状态
      const networkInfo = await getNetworkStatus()
      networkStatus.value = networkInfo

      // 初始化性能监控
      if (config.value.performanceMonitoring) {
        performanceMonitor.recordPageLoadTime()
      }

      // 加载本地配置
      await loadLocalConfig()

      // 检查更新
      await checkForUpdate()

      appStatus.value.isInitialized = true
      appStatus.value.isLoading = false

      console.log('应用初始化完成')
    } catch (error) {
      console.error('应用初始化失败:', error)
      appStatus.value.isLoading = false
      throw error
    }
  }

  /**
   * 获取网络状态
   */
  const getNetworkStatus = async () => {
    return new Promise((resolve) => {
      uni.getNetworkType({
        success: (res) => {
          resolve(res.networkType)
        },
        fail: () => {
          resolve('unknown')
        }
      })
    })
  }

  /**
   * 加载本地配置
   */
  const loadLocalConfig = async () => {
    try {
      const storedTheme = uni.getStorageSync('app_theme')
      if (storedTheme) {
        theme.value = { ...theme.value, ...storedTheme }
      }

      const storedConfig = uni.getStorageSync('app_config')
      if (storedConfig) {
        config.value = { ...config.value, ...storedConfig }
      }

      const storedPreferences = uni.getStorageSync('app_preferences')
      if (storedPreferences) {
        preferences.value = { ...preferences.value, ...storedPreferences }
      }

      const storedNotifications = uni.getStorageSync('app_notifications')
      if (storedNotifications) {
        notifications.value = { ...notifications.value, ...storedNotifications }
      }
    } catch (error) {
      console.warn('加载本地配置失败:', error)
    }
  }

  /**
   * 保存配置到本地
   */
  const saveConfig = () => {
    try {
      uni.setStorageSync('app_theme', theme.value)
      uni.setStorageSync('app_config', config.value)
      uni.setStorageSync('app_preferences', preferences.value)
      uni.setStorageSync('app_notifications', notifications.value)
    } catch (error) {
      console.error('保存配置失败:', error)
    }
  }

  /**
   * 更新主题
   */
  const updateTheme = (newTheme) => {
    theme.value = { ...theme.value, ...newTheme }
    saveConfig()
    applyTheme()
  }

  /**
   * 应用主题
   */
  const applyTheme = () => {
    // 应用主题变量到CSS
    const root = document.documentElement
    if (root) {
      root.style.setProperty('--primary-color', theme.value.primaryColor)

      if (isDarkMode.value) {
        root.classList.add('dark-mode')
      } else {
        root.classList.remove('dark-mode')
      }
    }

    // 更新状态栏样式
    if (isDarkMode.value) {
      uni.setStatusBarStyle('light')
    } else {
      uni.setStatusBarStyle('dark')
    }
  }

  /**
   * 更新配置
   */
  const updateConfig = (newConfig) => {
    config.value = { ...config.value, ...newConfig }
    saveConfig()
  }

  /**
   * 更新偏好设置
   */
  const updatePreferences = (newPreferences) => {
    preferences.value = { ...preferences.value, ...newPreferences }
    saveConfig()
  }

  /**
   * 更新通知设置
   */
  const updateNotifications = (newNotifications) => {
    notifications.value = { ...notifications.value, ...newNotifications }
    saveConfig()
  }

  /**
   * 检查是否为暗色时间
   */
  const isDarkTime = () => {
    const hour = new Date().getHours()
    return hour >= 18 || hour < 6
  }

  /**
   * 检查更新
   */
  const checkForUpdate = async () => {
    // #ifdef APP-PLUS
    try {
      const updateManager = uni.getUpdateManager()

      updateManager.onCheckForUpdate((res) => {
        if (res.hasUpdate) {
          appStatus.value.hasUpdate = true
        }
      })

      updateManager.onUpdateReady(() => {
        uni.showModal({
          title: '更新提示',
          content: '新版本已准备好，是否立即重启应用？',
          success: (res) => {
            if (res.confirm) {
              updateManager.applyUpdate()
            }
          }
        })
      })

      updateManager.onUpdateFailed(() => {
        console.error('更新失败')
      })
    } catch (error) {
      console.warn('检查更新失败:', error)
    }
    // #endif
  }

  /**
   * 更新性能数据
   */
  const updatePerformanceData = (type, value) => {
    performanceData.value[type] = value

    // 如果性能较差，显示警告
    if (needsOptimization.value && config.value.debugMode) {
      console.warn('应用性能需要优化:', performanceData.value)
    }
  }

  /**
   * 记录错误
   */
  const recordError = (error, context = {}) => {
    performanceData.value.errorCount++

    if (config.value.crashReportEnabled) {
      // 上报错误信息
      reportError(error, context)
    }

    if (config.value.debugMode) {
      console.error('应用错误:', error, context)
    }
  }

  /**
   * 上报错误信息
   */
  const reportError = (error, context) => {
    // 实现错误上报逻辑
    console.log('上报错误:', error, context)
  }

  /**
   * 清理缓存
   */
  const clearCache = async () => {
    try {
      // 清理存储缓存
      const keys = await uni.getStorageInfo().then(res => res.keys)
      const appKeys = keys.filter(key => key.startsWith('app_'))

      for (const key of appKeys) {
        await uni.removeStorageSync(key)
      }

      // 清理内存缓存
      if (typeof gc === 'function') {
        gc()
      }

      console.log('缓存清理完成')
    } catch (error) {
      console.error('清理缓存失败:', error)
    }
  }

  /**
   * 重置应用状态
   */
  const resetApp = async () => {
    try {
      // 清除所有状态
      systemInfo.value = {}
      networkStatus.value = 'unknown'
      appStatus.value.isInitialized = false

      // 重置为默认配置
      theme.value = {
        mode: 'light',
        primaryColor: '#1890ff',
        fontSize: 'medium',
        animationEnabled: true
      }

      config.value = {
        apiBaseUrl: normalizeBaseUrl(import.meta.env.VITE_APP_API_URL),
        apiTimeout: 10000,
        cacheEnabled: true,
        cacheExpireTime: 3600,
        performanceMonitoring: true,
        imageLazyLoad: true,
        dataCompressEnabled: true,
        biometricEnabled: false,
        autoLockTime: 300,
        screenCaptureProtection: false,
        debugMode: false,
        logLevel: 'info',
        crashReportEnabled: true
      }

      // 清理本地存储
      await clearCache()

      // 重新初始化
      await initializeApp()

      console.log('应用重置完成')
    } catch (error) {
      console.error('应用重置失败:', error)
      throw error
    }
  }

  /**
   * 获取应用信息
   */
  const getAppInfo = () => {
    return {
      version: appVersion.value,
      buildNumber: buildNumber.value,
      systemInfo: systemInfo.value,
      networkStatus: networkStatus.value,
      performanceData: performanceData.value
    }
  }

  // ========== 监听器 ==========

  // 监听网络状态变化
  uni.onNetworkStatusChange((res) => {
    networkStatus.value = res.networkType
    appStatus.value.isOnline = res.isConnected

    if (!res.isConnected) {
      console.log('网络连接断开')
    }
  })

  // 监听应用前后台切换
  uni.onAppShow(() => {
    console.log('应用进入前台')
    // 检查网络状态
    getNetworkStatus().then(status => {
      networkStatus.value = status
    })
  })

  uni.onAppHide(() => {
    console.log('应用进入后台')
    // 保存当前状态
    saveConfig()
  })

  // 返回状态和方法
  return {
    // 状态
    systemInfo,
    networkStatus,
    appVersion,
    buildNumber,
    theme,
    locale,
    config,
    preferences,
    appStatus,
    performanceData,
    notifications,

    // 计算属性
    isDarkMode,
    isPerformanceGood,
    needsOptimization,

    // 方法
    initializeApp,
    updateTheme,
    updateConfig,
    updatePreferences,
    updateNotifications,
    updatePerformanceData,
    recordError,
    clearCache,
    resetApp,
    getAppInfo,
    saveConfig
  }
})
