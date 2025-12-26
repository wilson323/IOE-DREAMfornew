/**
 * 门禁管理API - 移动端版本
 * 基于uni-app框架的企业级门禁移动端接口
 *
 * @Author: 老王
 * @Date: 2025-12-01
 * @Wechat: 老王牛逼
 * @Copyright: IOE-DREAM (https://ioe-dream.net), Since 2025
 */

import request from '@/utils/request'

// ==================== 移动端认证管理 ====================

/**
 * 初始化移动端认证
 */
export const initializeAuth = (params) => {
  return request.post('/api/mobile/v1/access/auth/initialize', params)
}

/**
 * 刷新移动端认证令牌
 */
export const refreshToken = (params) => {
  return request.post('/api/mobile/v1/access/auth/refresh', params)
}

/**
 * 注销移动端认证
 */
export const logout = (params) => {
  return request.post('/api/mobile/v1/access/auth/logout', params)
}

// ==================== 访问控制接口 ====================

/**
 * 生成临时访问二维码
 */
export const generateQRCode = (params) => {
  return request.post('/api/mobile/v1/access/qrcode/generate', params)
}

/**
 * 验证二维码访问
 */
export const verifyQRCode = (params) => {
  return request.post('/api/mobile/v1/access/qrcode/verify', params)
}

/**
 * 请求门禁访问
 */
export const requestAccess = (params) => {
  return request.post('/api/mobile/v1/access/access/request', params)
}

/**
 * 生物识别验证
 */
export const verifyBiometric = (params) => {
  return request.post('/api/mobile/v1/access/biometric/verify', params)
}

// ==================== 位置服务接口 ====================

/**
 * 获取当前位置
 */
export const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    if (!('geolocation' in navigator)) {
      reject(new Error('浏览器不支持地理定位'))
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          success: true,
          data: {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracy: position.coords.accuracy,
            altitude: position.coords.altitude,
            heading: position.coords.heading,
            speed: position.coords.speed,
            timestamp: new Date().toISOString()
          },
          message: '获取位置成功'
        })
      },
      (error) => {
        reject({
          success: false,
          data: null,
          message: `获取位置失败: ${error.message}`
        })
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 60000
      }
    )
  })
}

/**
 * 更新设备位置
 */
export const updateDeviceLocation = (params) => {
  return request.post('/api/mobile/v1/access/location/update', params)
}

// ==================== 事件管理接口 ====================

/**
 * 发送移动端事件
 */
export const sendEvent = (params) => {
  return request.post('/api/mobile/v1/access/events/send', params)
}

/**
 * 获取最近的事件记录
 */
export const getRecentEvents = (params) => {
  return request.get('/api/mobile/v1/access/events/recent', params)
}

/**
 * 获取设备日志
 */
export const getDeviceLogs = (params) => {
  return request.get('/api/mobile/v1/access/logs', params)
}

// ==================== 通行记录接口 ====================

/**
 * 获取最近的通行记录
 */
export const getRecentRecords = (params) => {
  return request.get('/api/mobile/v1/access/records/recent', params)
}

/**
 * 获取通行统计
 */
export const getAccessStatistics = (params) => {
  return request.get('/api/mobile/v1/access/statistics', params)
}

// ==================== 紧急告警接口 ====================

/**
 * 获取紧急告警
 */
export const getEmergencyAlerts = (params) => {
  return request.get('/api/mobile/v1/access/alerts/emergency', params)
}

/**
 * 发送紧急求助
 */
export const sendEmergencyRequest = (params) => {
  return request.post('/api/mobile/v1/access/emergency/request', params)
}

/**
 * 确认收到告警
 */
export const acknowledgeAlert = (params) => {
  return request.post('/api/mobile/v1/access/alerts/acknowledge', params)
}

// ==================== 设备管理接口 ====================

/**
 * 发送设备心跳
 */
export const sendHeartbeat = (params) => {
  return request.post('/api/mobile/v1/access/heartbeat', params)
}

/**
 * 获取设备信息
 */
export const getDeviceInfo = (deviceId) => {
  return request.get(`/api/mobile/v1/access/device/${deviceId}`)
}

/**
 * 更新设备配置
 */
export const updateDeviceConfig = (params) => {
  return request.put('/api/mobile/v1/access/device/config', params)
}

// ==================== 用户配置接口 ====================

/**
 * 获取用户配置
 */
export const getUserSettings = (userId) => {
  return request.get(`/api/mobile/v1/access/user/${userId}/settings`)
}

/**
 * 更新用户配置
 */
export const updateUserSettings = (params) => {
  return request.put('/api/mobile/v1/access/user/settings', params)
}

// ==================== 缓存和同步接口 ====================

/**
 * 同步离线数据
 */
export const syncOfflineData = (params) => {
  return request.post('/api/mobile/v1/access/sync/offline', params)
}

/**
 * 获取离线配置
 */
export const getOfflineConfig = (deviceId) => {
  return request.get(`/api/mobile/v1/access/offline/config/${deviceId}`)
}

// ==================== 安全接口 ====================

/**
 * 检查设备安全状态
 */
export const checkSecurityStatus = (deviceId) => {
  return request.get(`/api/mobile/v1/access/security/check/${deviceId}`)
}

/**
 * 报告设备丢失
 */
export const reportDeviceLost = (params) => {
  return request.post('/api/mobile/v1/access/security/report-lost', params)
}

// ==================== 移动端特色功能 ====================

/**
 * 扫码功能
 */
export const scanQRCode = () => {
  return new Promise((resolve, reject) => {
    uni.scanCode({
      success: (res) => {
        resolve({
          success: true,
          data: {
            result: res.result,
            scanType: res.scanType,
            charSet: res.charSet,
            path: res.path
          },
          message: '扫码成功'
        })
      },
      fail: (error) => {
        reject({
          success: false,
          data: null,
          message: `扫码失败: ${error.errMsg}`
        })
      }
    })
  })
}

/**
 * 获取设备信息
 */
export const getSystemInfo = () => {
  return new Promise((resolve) => {
    uni.getSystemInfo({
      success: (res) => {
        resolve({
          success: true,
          data: {
            platform: res.platform,
            version: res.version,
            system: res.system,
            model: res.model,
            deviceId: res.deviceId,
            SDKVersion: res.SDKVersion,
            screenWidth: res.screenWidth,
            screenHeight: res.screenHeight,
            windowWidth: res.windowWidth,
            windowHeight: res.windowHeight,
            statusBarHeight: res.statusBarHeight,
            safeArea: res.safeArea
          },
          message: '获取设备信息成功'
        })
      },
      fail: () => {
        resolve({
          success: false,
          data: null,
          message: '获取设备信息失败'
        })
      }
    })
  })
}

/**
 * 获取网络状态
 */
export const getNetworkType = () => {
  return new Promise((resolve) => {
    uni.getNetworkType({
      success: (res) => {
        resolve({
          success: true,
          data: {
            networkType: res.networkType,
            isConnected: res.networkType !== 'none'
          },
          message: '获取网络状态成功'
        })
      },
      fail: () => {
        resolve({
          success: false,
          data: {
            networkType: 'unknown',
            isConnected: false
          },
          message: '获取网络状态失败'
        })
      }
    })
  })
}

/**
 * 震动反馈
 */
export const vibrate = (type = 'short') => {
  return new Promise((resolve) => {
    uni.vibrate({
      type: type === 'long' ? 'long' : 'short',
      success: () => {
        resolve({
          success: true,
          message: '震动反馈成功'
        })
      },
      fail: () => {
        resolve({
          success: false,
          message: '震动反馈失败'
        })
      }
    })
  })
}

/**
 * 播放提示音
 */
export const playBeep = () => {
  return new Promise((resolve) => {
    // uni-app播放声音的实现
    const audioContext = uni.createInnerAudioContext()
    audioContext.src = '/static/sounds/beep.mp3'
    audioContext.play()

    audioContext.onPlay(() => {
      resolve({
        success: true,
        message: '播放提示音成功'
      })
    })

    audioContext.onError(() => {
      resolve({
        success: false,
        message: '播放提示音失败'
      })
    })
  })
}

// ==================== 全局联动接口 ====================

/**
 * 获取全局联动状态
 */
export const getGlobalLinkageStatus = (params) => {
  return request.get('/api/mobile/v1/access/global-linkage/status', params)
}

/**
 * 触发移动端联动
 */
export const triggerMobileLinkage = (params) => {
  return request.post('/api/mobile/v1/access/global-linkage/trigger', params)
}

/**
 * 获取移动端联动规则
 */
export const getMobileLinkageRules = (params) => {
  return request.get('/api/mobile/v1/access/global-linkage/rules', params)
}

// ==================== 导出默认对象 ====================

export default {
  // 认证管理
  initializeAuth,
  refreshToken,
  logout,

  // 访问控制
  generateQRCode,
  verifyQRCode,
  requestAccess,
  verifyBiometric,

  // 位置服务
  getCurrentLocation,
  updateDeviceLocation,

  // 事件管理
  sendEvent,
  getRecentEvents,
  getDeviceLogs,

  // 通行记录
  getRecentRecords,
  getAccessStatistics,

  // 紧急告警
  getEmergencyAlerts,
  sendEmergencyRequest,
  acknowledgeAlert,

  // 设备管理
  sendHeartbeat,
  getDeviceInfo,
  updateDeviceConfig,

  // 用户配置
  getUserSettings,
  updateUserSettings,

  // 缓存同步
  syncOfflineData,
  getOfflineConfig,

  // 安全接口
  checkSecurityStatus,
  reportDeviceLost,

  // 全局联动
  getGlobalLinkageStatus,
  triggerMobileLinkage,
  getMobileLinkageRules,

  // 移动端特色
  scanQRCode,
  getSystemInfo,
  getNetworkType,
  vibrate,
  playBeep
}