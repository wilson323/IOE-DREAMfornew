/**
 * 访客管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '@/lib/smart-request'

// 访客预约相关接口
export const appointmentApi = {
  /**
   * 创建访客预约
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  createAppointment: (data) => postRequest('/api/v1/mobile/visitor/appointment', data),

  /**
   * 取消访客预约
   * @param {Number} appointmentId 预约ID
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  cancelAppointment: (appointmentId, userId) => 
    putRequest(`/api/v1/mobile/visitor/appointment/${appointmentId}/cancel?userId=${userId}`),

  /**
   * 获取我的预约列表
   * @param {Number} userId 用户ID
   * @param {Number} status 预约状态
   * @returns {Promise}
   */
  getMyAppointments: (userId, status) => 
    getRequest('/api/v1/mobile/visitor/my-appointments', { userId, status }),

  /**
   * 获取预约详情
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  getAppointmentDetail: (appointmentId) => 
    getRequest(`/api/v1/mobile/visitor/appointment/${appointmentId}`)
}

// 访客签到签退相关接口
export const checkInApi = {
  /**
   * 二维码签到
   * @param {String} qrCode 二维码内容
   * @returns {Promise}
   */
  checkInByQRCode: (qrCode) => 
    postRequest(`/api/v1/mobile/visitor/checkin/qrcode?qrCode=${qrCode}`),

  /**
   * 访客签退
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  checkout: (appointmentId) => 
    postRequest(`/api/v1/mobile/visitor/checkout/${appointmentId}`),

  /**
   * 获取签到状态
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  getCheckInStatus: (appointmentId) => 
    getRequest(`/api/v1/mobile/visitor/checkin/status/${appointmentId}`)
}

// 位置和通行相关接口
export const locationApi = {
  /**
   * 获取访客位置
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  getVisitorLocation: (appointmentId) => 
    getRequest(`/api/v1/mobile/visitor/location/${appointmentId}`),

  /**
   * 更新访客位置
   * @param {Number} appointmentId 预约ID
   * @param {Number} areaId 区域ID
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  updateVisitorLocation: (appointmentId, areaId, deviceId) => 
    putRequest(`/api/v1/mobile/visitor/location/${appointmentId}`, { areaId, deviceId })
}

// 车证管理相关接口
export const vehicleApi = {
  /**
   * 获取车证信息
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  getVehiclePermit: (appointmentId) => 
    getRequest(`/api/v1/mobile/visitor/vehicle-permit/${appointmentId}`),

  /**
   * 生成车证
   * @param {Number} appointmentId 预约ID
   * @param {String} vehicleNumber 车牌号
   * @returns {Promise}
   */
  generateVehiclePermit: (appointmentId, vehicleNumber) => 
    postRequest(`/api/v1/mobile/visitor/vehicle-permit/${appointmentId}`, { vehicleNumber })
}

// 记录查询相关接口
export const recordApi = {
  /**
   * 获取通行记录
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  getAccessRecords: (appointmentId) => 
    getRequest(`/api/v1/mobile/visitor/access-records/${appointmentId}`),

  /**
   * 获取访客历史记录
   * @param {Number} visitorId 访客ID
   * @param {Number} limit 记录数量限制
   * @returns {Promise}
   */
  getVisitorHistory: (visitorId, limit = 10) => 
    getRequest(`/api/v1/mobile/visitor/history/${visitorId}`, { limit })
}

// 通知和提醒相关接口
export const notificationApi = {
  /**
   * 发送访客通知
   * @param {Number} appointmentId 预约ID
   * @param {String} notificationType 通知类型
   * @returns {Promise}
   */
  sendNotification: (appointmentId, notificationType) => 
    postRequest(`/api/v1/mobile/visitor/notification/${appointmentId}`, { notificationType })
}

// 异常处理相关接口
export const exceptionApi = {
  /**
   * 报告异常情况
   * @param {Number} recordId 记录ID
   * @param {String} exceptionType 异常类型
   * @param {String} description 异常描述
   * @returns {Promise}
   */
  reportException: (recordId, exceptionType, description) => 
    postRequest('/api/v1/mobile/visitor/exception', { recordId, exceptionType, description })
}

// 统计和报告相关接口
export const statisticsApi = {
  /**
   * 获取个人访问统计
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getPersonalStatistics: (userId) => 
    getRequest(`/api/v1/mobile/visitor/statistics/${userId}`),

  /**
   * 导出访问记录
   * @param {String} startDate 开始日期
   * @param {String} endDate 结束日期
   * @returns {Promise}
   */
  exportRecords: (startDate, endDate) => 
    getRequest('/api/v1/mobile/visitor/export', { startDate, endDate })
}

// 实用工具相关接口
export const utilApi = {
  /**
   * 验证访客信息
   * @param {String} idCardNumber 身份证号
   * @param {String} phoneNumber 手机号
   * @returns {Promise}
   */
  validateVisitorInfo: (idCardNumber, phoneNumber) => 
    postRequest('/api/v1/mobile/visitor/validate', { idCardNumber, phoneNumber }),

  /**
   * 获取被访人信息
   * @param {Number} userId 被访人ID
   * @returns {Promise}
   */
  getVisiteeInfo: (userId) => 
    getRequest(`/api/v1/mobile/visitor/visitee/${userId}`),

  /**
   * 获取访问区域列表
   * @returns {Promise}
   */
  getVisitAreas: () => 
    getRequest('/api/v1/mobile/visitor/areas'),

  /**
   * 获取预约类型列表
   * @returns {Promise}
   */
  getAppointmentTypes: () => 
    getRequest('/api/v1/mobile/visitor/appointment-types'),

  /**
   * 获取帮助信息
   * @param {String} helpType 帮助类型
   * @returns {Promise}
   */
  getHelpInfo: (helpType) => 
    getRequest('/api/v1/mobile/visitor/help', { helpType })
}

// OCR识别相关接口
export const ocrApi = {
  /**
   * OCR识别身份证
   * @param {String} imagePath 图片路径（本地临时路径）
   * @param {String} cardSide 身份证面（FRONT-正面，BACK-背面）
   * @returns {Promise}
   */
  recognizeIdCard: (imagePath, cardSide = 'FRONT') => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: `${import.meta.env.VITE_APP_API_URL || ''}/api/v1/mobile/visitor/ocr/idcard?cardSide=${cardSide}`,
        filePath: imagePath,
        name: 'file',
        header: {
          'Authorization': `Bearer ${uni.getStorageSync('USER_TOKEN') || ''}`
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 200 || data.success) {
              resolve({ success: true, data: data.data || data })
            } else {
              reject(new Error(data.message || '识别失败'))
            }
          } catch (e) {
            reject(new Error('解析响应失败'))
          }
        },
        fail: (error) => {
          reject(error)
        }
      })
    })
  }
}

// 导出所有API
export default {
  ...appointmentApi,
  ...checkInApi,
  ...locationApi,
  ...vehicleApi,
  ...recordApi,
  ...notificationApi,
  ...exceptionApi,
  ...statisticsApi,
  ...utilApi,
  ...ocrApi
}

// 单独导出OCR API
export { ocrApi }

