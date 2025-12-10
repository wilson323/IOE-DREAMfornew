/**
 * 考勤管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest } from '@/lib/smart-request'

// GPS打卡相关接口
export const punchApi = {
  /**
   * GPS定位打卡
   * @param {Object} data 打卡数据
   * @param {Number} data.employeeId 员工ID
   * @param {Number} data.latitude 纬度
   * @param {Number} data.longitude 经度
   * @param {String} data.photoUrl 照片URL
   * @param {String} data.address 地址信息
   * @returns {Promise}
   */
  gpsPunch: (data) => postRequest('/api/attendance/mobile/gps-punch', data),

  /**
   * 上传打卡照片
   * @param {FormData} formData 表单数据
   * @returns {Promise}
   */
  uploadPunchPhoto: (formData) => postRequest('/api/attendance/mobile/punch/photo/upload', formData),

  /**
   * 获取今日打卡状态
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  getTodayPunchStatus: (employeeId) =>
    getRequest('/api/attendance/mobile/punch/status/today', { employeeId }),

  /**
   * 获取打卡记录列表
   * @param {Object} params 查询参数
   * @param {Number} params.employeeId 员工ID
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {Number} params.pageSize 页大小
   * @param {Number} params.pageNum 页码
   * @returns {Promise}
   */
  getPunchRecords: (params) =>
    getRequest('/api/attendance/mobile/punch/records', params)
}

// 位置验证相关接口
export const locationApi = {
  /**
   * 验证GPS位置
   * @param {Object} data 位置数据
   * @param {Number} data.employeeId 员工ID
   * @param {Number} data.latitude 纬度
   * @param {Number} data.longitude 经度
   * @returns {Promise}
   */
  validateLocation: (data) => postRequest('/api/attendance/mobile/location/validate', data),

  /**
   * 获取考勤地点列表
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  getAttendanceLocations: (employeeId) =>
    getRequest('/api/attendance/mobile/locations', { employeeId }),

  /**
   * 获取最近考勤地点
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  getNearestLocation: (employeeId) =>
    getRequest('/api/attendance/mobile/location/nearest', { employeeId })
}

// 离线打卡相关接口
export const offlineApi = {
  /**
   * 缓存离线打卡数据
   * @param {Object} data 离线数据
   * @param {Number} data.employeeId 员工ID
   * @param {Array} data.punchDataList 打卡数据列表
   * @returns {Promise}
   */
  cacheOfflinePunch: (data) => postRequest('/api/attendance/mobile/offline/cache', data),

  /**
   * 同步离线打卡数据
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  syncOfflinePunches: (employeeId) =>
    postRequest(`/api/attendance/mobile/offline/sync/${employeeId}`),

  /**
   * 获取离线数据状态
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  getOfflineStatus: (employeeId) =>
    getRequest('/api/attendance/mobile/offline/status', { employeeId }),

  /**
   * 清除离线缓存数据
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  clearOfflineCache: (employeeId) =>
    postRequest(`/api/attendance/mobile/offline/clear/${employeeId}`)
}

// 请假相关接口
export const leaveApi = {
  /**
   * 申请请假
   * @param {Object} data 请假数据
   * @param {Number} data.employeeId 员工ID
   * @param {String} data.leaveType 请假类型
   * @param {String} data.startTime 开始时间
   * @param {String} data.endTime 结束时间
   * @param {String} data.reason 请假原因
   * @returns {Promise}
   */
  applyLeave: (data) => postRequest('/api/attendance/mobile/leave/apply', data),

  /**
   * 获取请假记录
   * @param {Object} params 查询参数
   * @param {Number} params.employeeId 员工ID
   * @param {String} params.status 状态
   * @param {Number} params.pageSize 页大小
   * @param {Number} params.pageNum 页码
   * @returns {Promise}
   */
  getLeaveRecords: (params) =>
    getRequest('/api/attendance/mobile/leave/records', params),

  /**
   * 取消请假申请
   * @param {Number} leaveId 请假ID
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  cancelLeave: (leaveId, employeeId) =>
    postRequest(`/api/attendance/mobile/leave/cancel/${leaveId}`, { employeeId })
}

// 加班相关接口
export const overtimeApi = {
  /**
   * 申请加班
   * @param {Object} data 加班数据
   * @param {Number} data.employeeId 员工ID
   * @param {String} data.overtimeDate 加班日期
   * @param {String} data.startTime 开始时间
   * @param {String} data.endTime 结束时间
   * @param {String} data.reason 加班原因
   * @returns {Promise}
   */
  applyOvertime: (data) => postRequest('/api/attendance/mobile/overtime/apply', data),

  /**
   * 获取加班记录
   * @param {Object} params 查询参数
   * @param {Number} params.employeeId 员工ID
   * @param {String} params.status 状态
   * @param {Number} params.pageSize 页大小
   * @param {Number} params.pageNum 页码
   * @returns {Promise}
   */
  getOvertimeRecords: (params) =>
    getRequest('/api/attendance/mobile/overtime/records', params)
}

// 考勤统计相关接口
export const statisticsApi = {
  /**
   * 获取个人考勤统计
   * @param {Object} params 统计参数
   * @param {Number} params.employeeId 员工ID
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @returns {Promise}
   */
  getPersonalStatistics: (params) =>
    getRequest('/api/attendance/mobile/statistics/personal', params),

  /**
   * 获取月度考勤汇总
   * @param {Object} params 汇总参数
   * @param {Number} params.employeeId 员工ID
   * @param {String} params.month 月份(YYYY-MM)
   * @returns {Promise}
   */
  getMonthlySummary: (params) =>
    getRequest('/api/attendance/mobile/statistics/monthly', params),

  /**
   * 获取考勤异常列表
   * @param {Object} params 查询参数
   * @param {Number} params.employeeId 员工ID
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @returns {Promise}
   */
  getAbnormalRecords: (params) =>
    getRequest('/api/attendance/mobile/statistics/abnormal', params)
}

// 排班相关接口
export const scheduleApi = {
  /**
   * 获取个人排班信息
   * @param {Object} params 查询参数
   * @param {Number} params.employeeId 员工ID
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @returns {Promise}
   */
  getPersonalSchedule: (params) =>
    getRequest('/api/attendance/mobile/schedule/personal', params),

  /**
   * 获取今日排班
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  getTodaySchedule: (employeeId) =>
    getRequest('/api/attendance/mobile/schedule/today', { employeeId }),

  /**
   * 获取本周排班
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  getWeekSchedule: (employeeId) =>
    getRequest('/api/attendance/mobile/schedule/week', { employeeId })
}

// 补卡相关接口
export const supplementApi = {
  /**
   * 申请补卡
   * @param {Object} data 补卡数据
   * @param {Number} data.employeeId 员工ID
   * @param {String} data.supplementType 补卡类型(IN/OUT)
   * @param {String} data.supplementTime 补卡时间
   * @param {String} data.reason 补卡原因
   * @returns {Promise}
   */
  applySupplement: (data) => postRequest('/api/attendance/mobile/supplement/apply', data),

  /**
   * 获取补卡记录
   * @param {Object} params 查询参数
   * @param {Number} params.employeeId 员工ID
   * @param {String} params.status 状态
   * @param {Number} params.pageSize 页大小
   * @param {Number} params.pageNum 页码
   * @returns {Promise}
   */
  getSupplementRecords: (params) =>
    getRequest('/api/attendance/mobile/supplement/records', params)
}

// 出差相关接口
export const travelApi = {
  /**
   * 申请出差
   * @param {Object} data 出差数据
   * @param {Number} data.employeeId 员工ID
   * @param {String} data.travelType 出差类型
   * @param {String} data.startTime 开始时间
   * @param {String} data.endTime 结束时间
   * @param {String} data.destination 目的地
   * @param {String} data.reason 出差原因
   * @returns {Promise}
   */
  applyTravel: (data) => postRequest('/api/attendance/mobile/travel/apply', data),

  /**
   * 获取出差记录
   * @param {Object} params 查询参数
   * @param {Number} params.employeeId 员工ID
   * @param {String} params.status 状态
   * @param {Number} params.pageSize 页大小
   * @param {Number} params.pageNum 页码
   * @returns {Promise}
   */
  getTravelRecords: (params) =>
    getRequest('/api/attendance/mobile/travel/records', params)
}

// 导出所有API
export default {
  ...punchApi,
  ...locationApi,
  ...offlineApi,
  ...leaveApi,
  ...overtimeApi,
  ...statisticsApi,
  ...scheduleApi,
  ...supplementApi,
  ...travelApi
}