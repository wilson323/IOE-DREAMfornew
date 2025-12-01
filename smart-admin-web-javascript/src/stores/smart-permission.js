/**
 * SmartPermission 权限管理状态管理
 * 基于 Pinia 的权限管理状态
 */

import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import { permissionApi, userPermissionApi, securityLevelApi } from '@/api/smart-permission'
import PERMISSION_CODES from '@/constants/permission-codes'

export const usePermissionStore = defineStore('smartPermission', () => {
  // 状态定义
  const currentUser = ref(null)
  const userPermissions = ref([])
  const permissionStatistics = reactive({
    totalUsers: 0,
    activePermissions: 0,
    todayOperations: 0,
    securityEvents: 0
  })
  const securityLevels = ref([])
  const loading = reactive({
    permissions: false,
    statistics: false,
    securityLevels: false
  })
  const permissionCache = new Map()

  // 计算属性
  const hasPermission = computed(() => (permissionCode) => {
    if (!currentUser.value || !userPermissions.value.length) {
      return false
    }
    return userPermissions.value.some(permission =>
      permission.operationCode === permissionCode &&
      permission.approveStatus === 'ACTIVE'
    )
  })

  const isLoggedIn = computed(() => {
    return currentUser.value !== null
  })

  const currentUserRoles = computed(() => {
    return currentUser.value?.roles || []
  })

  const hasRole = computed(() => (roleCode) => {
    if (!currentUser.value || !currentUserRoles.value.length) {
      return false
    }
    return currentUserRoles.value.some(role => role.roleCode === roleCode)
  })

  const hasDataScope = computed(() => (dataScope) => {
    if (!currentUser.value) {
      return false
    }
    // 检查用户是否具有指定数据域权限
    const userPermissionsData = currentUser.value?.permissions || []
    return userPermissionsData.some(permission =>
      permission.dataScope === dataScope &&
      permission.approveStatus === 'ACTIVE'
    )
  })

  const userSecurityLevel = computed(() => {
    return currentUser.value?.securityLevelValue || 1
  })

  const canAccessResource = (resourceType, resourceId, requiredLevel = 1) => {
    return userSecurityLevel.value >= requiredLevel &&
           hasPermission.value(`${resourceType.toUpperCase()}_ACCESS`)
  }

  // 方法定义
  /**
   * 设置当前用户
   * @param {Object} user 用户信息
   */
  const setCurrentUser = (user) => {
    currentUser.value = user
    // 清除用户权限缓存
    permissionCache.delete(`user_${user?.userId}_permissions`)
  }

  /**
   * 获取用户权限概览
   * @param {number} userId 用户ID
   * @returns {Promise} 用户权限概览
   */
  const fetchUserPermissionOverview = async (userId) => {
    const cacheKey = `user_${userId}_overview`

    if (permissionCache.has(cacheKey)) {
      return permissionCache.get(cacheKey)
    }

    try {
      loading.permissions = true
      const response = await permissionApi.getUserPermissionOverview(userId)

      if (response.data) {
        permissionCache.set(cacheKey, response.data)
        return response.data
      }
    } catch (error) {
      message.error('获取用户权限概览失败')
      throw error
    } finally {
      loading.permissions = false
    }
  }

  /**
   * 获取用户权限列表
   * @param {number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise} 用户权限列表
   */
  const fetchUserPermissions = async (userId, params = {}) => {
    const cacheKey = `user_${userId}_permissions_${JSON.stringify(params)}`

    if (permissionCache.has(cacheKey)) {
      return permissionCache.get(cacheKey)
    }

    try {
      loading.permissions = true
      const response = await userPermissionApi.getUserPermissions(userId, params)

      const permissions = response.data || []
      userPermissions.value = permissions
      permissionCache.set(cacheKey, permissions)

      return permissions
    } catch (error) {
      message.error('获取用户权限列表失败')
      throw error
    } finally {
      loading.permissions = false
    }
  }

  /**
   * 检查权限
   * @param {string} operationCode 操作代码
   * @param {Object} context 权限检查上下文
   * @returns {Promise} 权限检查结果
   */
  const checkPermission = async (operationCode, context = {}) => {
    try {
      const data = {
        userId: currentUser.value?.userId,
        operationCode,
        ...context
      }

      const response = await permissionApi.checkPermission(data)
      return response.data
    } catch (error) {
      console.error('权限检查失败:', error)
      return { granted: false, errorMessage: error.message }
    }
  }

  /**
   * 授予权限
   * @param {string} permissionType 权限类型 (area/device/attendance/access)
   * @param {Object} permissionData 权限数据
   * @returns {Promise} 授予结果
   */
  const grantPermission = async (permissionType, permissionData) => {
    try {
      let response

      switch (permissionType) {
        case 'area':
          response = await permissionApi.grantAreaPermission(permissionData)
          break
        case 'device':
          response = await permissionApi.grantDevicePermission(permissionData)
          break
        case 'attendance':
          response = await permissionApi.grantAttendancePermission(permissionData)
          break
        case 'access':
          response = await permissionApi.grantAccessPermission(permissionData)
          break
        default:
          throw new Error('不支持的权限类型')
      }

      message.success('权限授予成功')

      // 清除相关缓存
      clearUserCache()

      return response.data
    } catch (error) {
      message.error('权限授予失败')
      throw error
    }
  }

  /**
   * 撤销权限
   * @param {Object} revokeData 撤销数据
   * @returns {Promise} 撤销结果
   */
  const revokePermission = async (revokeData) => {
    try {
      const response = await permissionApi.revokeUserPermission(revokeData)
      message.success('权限撤销成功')

      // 清除相关缓存
      clearUserCache()

      return response.data
    } catch (error) {
      message.error('权限撤销失败')
      throw error
    }
  }

  /**
   * 获取统计数据
   */
  const fetchStatistics = async () => {
    try {
      loading.statistics = true
      const response = await permissionApi.getStatistics()

      if (response.data) {
        Object.assign(permissionStatistics, response.data)
      }
    } catch (error) {
      console.error('获取统计数据失败:', error)
    } finally {
      loading.statistics = false
    }
  }

  /**
   * 获取安全级别列表
   */
  const fetchSecurityLevels = async () => {
    try {
      loading.securityLevels = true
      const response = await securityLevelApi.getSecurityLevels()
      securityLevels.value = response.data || []
    } catch (error) {
      message.error('获取安全级别列表失败')
    } finally {
      loading.securityLevels = false
    }
  }

  /**
   * 授予用户安全级别
   * @param {Object} securityLevelData 安全级别数据
   * @returns {Promise} 授予结果
   */
  const grantSecurityLevel = async (securityLevelData) => {
    try {
      const response = await permissionApi.grantUserSecurityLevel(securityLevelData)
      message.success('安全级别授予成功')

      // 清除相关缓存
      clearUserCache()

      return response.data
    } catch (error) {
      message.error('安全级别授予失败')
      throw error
    }
  }

  /**
   * 清除用户缓存
   */
  const clearUserCache = () => {
    if (currentUser.value) {
      permissionCache.delete(`user_${currentUser.value.userId}_permissions`)
      permissionCache.delete(`user_${currentUser.value.userId}_overview`)
    }
  }

  /**
   * 清除所有缓存
   */
  const clearAllCache = () => {
    permissionCache.clear()
    userPermissions.value = []
  }

  /**
   * 刷新权限数据
   */
  const refreshPermissions = async () => {
    if (currentUser.value) {
      await fetchUserPermissions(currentUser.value.userId)
      await fetchStatistics()
    }
  }

  /**
   * 重置状态
   */
  const reset = () => {
    currentUser.value = null
    userPermissions.value = []
    permissionCache.clear()
  }

  // 权限检查便捷方法
  const permissions = {
    // 区域权限
    canAccessArea: (areaLevel = 1) => canAccessResource.value('area', null, areaLevel),
    canManageArea: () => hasPermission.value(PERMISSION_CODES.AREA_MANAGE),
    canConfigArea: () => hasPermission.value(PERMISSION_CODES.AREA_CONFIG),

    // 设备权限
    canViewDevice: () => hasPermission.value(PERMISSION_CODES.DEVICE_VIEW),
    canControlDevice: () => hasPermission.value(PERMISSION_CODES.DEVICE_CONTROL),
    canConfigDevice: () => hasPermission.value(PERMISSION_CODES.DEVICE_CONFIG),

    // 考勤权限
    canViewAttendance: () => hasPermission.value(PERMISSION_CODES.ATTENDANCE_VIEW),
    canManageAttendance: () => hasPermission.value(PERMISSION_CODES.ATTENDANCE_MANAGE),
    canExportAttendance: () => hasPermission.value(PERMISSION_CODES.ATTENDANCE_EXPORT),

    // 门禁权限
    canEnterAccess: () => hasPermission.value(PERMISSION_CODES.ACCESS_ENTER),
    canManageAccess: () => hasPermission.value(PERMISSION_CODES.ACCESS_MANAGE),
    canConfigAccess: () => hasPermission.value(PERMISSION_CODES.ACCESS_CONFIG)
  }

  return {
    // 状态
    currentUser,
    userPermissions,
    permissionStatistics,
    securityLevels,
    loading,

    // 计算属性
    hasPermission,
    isLoggedIn,
    currentUserRoles,
    hasRole,
    hasDataScope,
    userSecurityLevel,
    canAccessResource,
    permissions,

    // 方法
    setCurrentUser,
    fetchUserPermissionOverview,
    fetchUserPermissions,
    checkPermission,
    grantPermission,
    revokePermission,
    fetchStatistics,
    fetchSecurityLevels,
    grantSecurityLevel,
    clearUserCache,
    clearAllCache,
    refreshPermissions,
    reset
  }
})