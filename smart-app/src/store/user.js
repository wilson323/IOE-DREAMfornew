import { defineStore } from 'pinia'

/**
 * 用户信息状态管理
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref({
    userId: null,
    username: '',
    realName: '',
    avatar: '',
    phone: '',
    email: '',
    departmentName: '',
    roleName: '',
    permissions: []
  })

  const token = ref('')
  const isLoggedIn = ref(false)

  // 计算属性
  const hasPermission = computed(() => (permission) => {
    if (!userInfo.value.permissions || userInfo.value.permissions.length === 0) {
      return false
    }
    return userInfo.value.permissions.includes(permission)
  })

  const displayName = computed(() => {
    return userInfo.value.realName || userInfo.value.username || '未知用户'
  })

  // 方法
  const setUserInfo = (info) => {
    userInfo.value = { ...userInfo.value, ...info }
  }

  const setToken = (newToken) => {
    token.value = newToken
    isLoggedIn.value = !!newToken

    // 持久化存储
    if (newToken) {
      uni.setStorageSync('token', newToken)
    } else {
      uni.removeStorageSync('token')
    }
  }

  const login = async (loginForm) => {
    try {
      // 这里应该调用登录API
      // const response = await loginApi(loginForm)

      // 模拟登录成功
      const mockResponse = {
        code: 1,
        data: {
          token: 'mock_token_' + Date.now(),
          userInfo: {
            userId: 1,
            username: loginForm.username,
            realName: '测试用户',
            avatar: '/static/images/avatar/default.png',
            phone: '138****8888',
            email: 'test@example.com',
            departmentName: '技术部',
            roleName: '管理员',
            permissions: ['dashboard:view', 'user:manage', 'system:config']
          }
        }
      }

      if (mockResponse.code === 1) {
        setToken(mockResponse.data.token)
        setUserInfo(mockResponse.data.userInfo)

        // 持久化用户信息
        uni.setStorageSync('userInfo', mockResponse.data.userInfo)

        return { success: true, data: mockResponse.data }
      } else {
        throw new Error(mockResponse.message || '登录失败')
      }
    } catch (error) {
      console.error('登录失败:', error)
      return { success: false, message: error.message || '登录失败' }
    }
  }

  const logout = async () => {
    try {
      // 这里可以调用注销API
      // await logoutApi()

      // 清除本地状态
      token.value = ''
      userInfo.value = {
        userId: null,
        username: '',
        realName: '',
        avatar: '',
        phone: '',
        email: '',
        departmentName: '',
        roleName: '',
        permissions: []
      }
      isLoggedIn.value = false

      // 清除持久化存储
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')

      return { success: true }
    } catch (error) {
      console.error('注销失败:', error)
      return { success: false, message: error.message || '注销失败' }
    }
  }

  const refreshToken = async () => {
    try {
      // 这里应该调用刷新token的API
      // const response = await refreshTokenApi()

      // 模拟刷新成功
      const newToken = 'refreshed_token_' + Date.now()
      setToken(newToken)

      return { success: true, token: newToken }
    } catch (error) {
      console.error('刷新token失败:', error)
      // 刷新失败，需要重新登录
      await logout()
      return { success: false, message: error.message || 'Token刷新失败' }
    }
  }

  const updateUserInfo = async (updates) => {
    try {
      // 这里应该调用更新用户信息的API
      // const response = await updateUserInfoApi(updates)

      // 模拟更新成功
      const updatedUserInfo = { ...userInfo.value, ...updates }
      setUserInfo(updatedUserInfo)

      // 持久化更新
      uni.setStorageSync('userInfo', updatedUserInfo)

      return { success: true, data: updatedUserInfo }
    } catch (error) {
      console.error('更新用户信息失败:', error)
      return { success: false, message: error.message || '更新失败' }
    }
  }

  const initUserFromStorage = () => {
    try {
      const storedToken = uni.getStorageSync('token')
      const storedUserInfo = uni.getStorageSync('userInfo')

      if (storedToken) {
        setToken(storedToken)
      }

      if (storedUserInfo) {
        setUserInfo(storedUserInfo)
      }
    } catch (error) {
      console.error('从本地存储初始化用户信息失败:', error)
    }
  }

  const checkLoginStatus = () => {
    return isLoggedIn.value && !!token.value
  }

  // 初始化用户信息（从本地存储）
  initUserFromStorage()

  return {
    // 状态
    userInfo: readonly(userInfo),
    token: readonly(token),
    isLoggedIn: readonly(isLoggedIn),

    // 计算属性
    hasPermission,
    displayName,

    // 方法
    setUserInfo,
    setToken,
    login,
    logout,
    refreshToken,
    updateUserInfo,
    initUserFromStorage,
    checkLoginStatus
  }
})