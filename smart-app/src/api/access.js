/**
 * 门禁管理API接口 - 移动端（兼容文件）
 * 重导出 business/access/access-api.js 中的API
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import accessApi from '@/api/business/access/access-api'

// 兼容旧版API调用
export default {
  // 设备相关
  getDeviceInfo: () => accessApi.getNearbyDevices(0, 0, 0, 10000),
  getNearbyDevices: accessApi.getNearbyDevices,
  getRealTimeStatus: accessApi.getRealTimeStatus,
  
  // 访问检查
  mobileAccessCheck: accessApi.mobileAccessCheck,
  verifyQRCode: accessApi.verifyQRCode,
  verifyNFC: accessApi.verifyNFC,
  verifyBiometric: accessApi.verifyBiometric,
  
  // 权限相关
  getUserPermissions: accessApi.getUserPermissions,
  
  // 记录相关
  getUserAccessRecords: accessApi.getUserAccessRecords,
  
  // 临时访问
  requestTemporaryAccess: accessApi.requestTemporaryAccess,
  
  // 通知
  sendPushNotification: accessApi.sendPushNotification,
  
  // 统计相关（需要从后端获取）
  getAccessStatistics: async () => {
    // TODO: 实现统计接口调用
    return { success: true, data: { onlineDevices: 0, todayAccess: 0, activeAlerts: 0 } }
  },
  
  // 事件相关（需要从后端获取）
  getRecentEvents: async (params) => {
    // TODO: 实现事件列表接口调用
    return { success: true, data: [] }
  }
}

