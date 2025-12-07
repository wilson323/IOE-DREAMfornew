/**
 * 访客管理API接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { postRequest, getRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const visitorApi = {
  // ===== 访客预约管理 =====

  /**
   * 创建访客预约
   * @param {Object} data 预约数据
   * @param {String} data.visitorName 访客姓名
   * @param {String} data.phone 手机号
   * @param {String} data.idCard 身份证号
   * @param {String} data.company 来访公司
   * @param {String} data.purpose 来访事由
   * @param {Number} data.visiteeId 被访人ID
   * @param {String} data.appointmentTime 预约时间
   * @param {String} data.appointmentType 预约类型
   * @returns {Promise<ResponseDTO<Long>>}
   */
  createAppointment: (data) =>
    postRequest('/api/v1/mobile/visitor/appointment', data),

  /**
   * 取消访客预约
   * @param {Number} appointmentId 预约ID
   * @param {Number} userId 用户ID
   * @returns {Promise<ResponseDTO<Void>>}
   */
  cancelAppointment: (appointmentId, userId) =>
    putRequest(`/api/v1/mobile/visitor/appointment/${appointmentId}/cancel`, { userId }),

  /**
   * 获取我的访客预约列表
   * @param {Number} userId 用户ID
   * @param {Number} status 预约状态（可选）
   * @returns {Promise<ResponseDTO<List<VisitorAppointmentVO>>>}
   */
  getMyAppointments: (userId, status) =>
    getRequest('/api/v1/mobile/visitor/my-appointments', { userId, status }),

  /**
   * 获取预约详情
   * @param {Number} appointmentId 预约ID
   * @returns {Promise<ResponseDTO<VisitorAppointmentDetailVO>>}
   */
  getAppointmentDetail: (appointmentId) =>
    getRequest(`/api/v1/mobile/visitor/appointment/${appointmentId}`),

  /**
   * 分页查询访客列表
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.visitorName 访客姓名（可选）
   * @param {String} params.phone 手机号（可选）
   * @param {String} params.status 状态（可选）
   * @returns {Promise<ResponseDTO<PageResult<VisitorVO>>>}
   */
  queryVisitors: (params) =>
    getRequest('/api/visitor/page', params),

  /**
   * 更新访客信息
   * @param {Object} data 访客数据
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  updateVisitor: (data) =>
    putRequest('/api/visitor/', data),

  /**
   * 删除访客
   * @param {Number} id 访客ID
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  deleteVisitor: (id) =>
    deleteRequest(`/api/visitor/${id}`),

  // ===== 签到签退管理 =====

  /**
   * 二维码签到
   * @param {String} qrCode 二维码内容
   * @returns {Promise<ResponseDTO<MobileCheckInResultVO>>}
   */
  checkInByQRCode: (qrCode) =>
    postRequest('/api/v1/mobile/visitor/checkin/qrcode', { qrCode }),

  /**
   * 访客签退
   * @param {Number} appointmentId 预约ID
   * @returns {Promise<ResponseDTO<Void>>}
   */
  checkout: (appointmentId) =>
    postRequest(`/api/v1/mobile/visitor/checkout/${appointmentId}`),

  /**
   * 获取签到状态
   * @param {Number} appointmentId 预约ID
   * @returns {Promise<ResponseDTO<Object>>}
   */
  getCheckInStatus: (appointmentId) =>
    getRequest(`/api/v1/mobile/visitor/checkin/status/${appointmentId}`),

  // ===== 位置和通行管理 =====

  /**
   * 获取访客位置
   * @param {Number} appointmentId 预约ID
   * @returns {Promise<ResponseDTO<VisitorLocationVO>>}
   */
  getVisitorLocation: (appointmentId) =>
    getRequest(`/api/v1/mobile/visitor/location/${appointmentId}`),

  /**
   * 更新访客位置
   * @param {Number} appointmentId 预约ID
   * @param {Number} areaId 区域ID
   * @param {Number} deviceId 设备ID
   * @returns {Promise<ResponseDTO<Void>>}
   */
  updateVisitorLocation: (appointmentId, areaId, deviceId) =>
    putRequest(`/api/v1/mobile/visitor/location/${appointmentId}`, { areaId, deviceId }),

  /**
   * 获取车证信息
   * @param {Number} appointmentId 预约ID
   * @returns {Promise<ResponseDTO<VisitorVehiclePermitVO>>}
   */
  getVehiclePermit: (appointmentId) =>
    getRequest(`/api/v1/mobile/visitor/vehicle-permit/${appointmentId}`),

  /**
   * 生成车证
   * @param {Number} appointmentId 预约ID
   * @param {String} vehicleNumber 车牌号
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  generateVehiclePermit: (appointmentId, vehicleNumber) =>
    postRequest(`/api/v1/mobile/visitor/vehicle-permit/${appointmentId}`, { vehicleNumber }),

  // ===== 记录查询管理 =====

  /**
   * 获取通行记录
   * @param {Number} appointmentId 预约ID
   * @returns {Promise<ResponseDTO<List<VisitRecordVO>>>}
   */
  getAccessRecords: (appointmentId) =>
    getRequest(`/api/v1/mobile/visitor/access-records/${appointmentId}`),

  /**
   * 获取访客历史记录
   * @param {Number} visitorId 访客ID
   * @param {Number} limit 记录数量限制
   * @returns {Promise<ResponseDTO<List<VisitHistoryVO>>>}
   */
  getVisitorHistory: (visitorId, limit = 10) =>
    getRequest(`/api/v1/mobile/visitor/history/${visitorId}`, { limit }),

  // ===== 通知管理 =====

  /**
   * 发送访客通知
   * @param {Number} appointmentId 预约ID
   * @param {String} notificationType 通知类型
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  sendNotification: (appointmentId, notificationType) =>
    postRequest(`/api/v1/mobile/visitor/notification/${appointmentId}`, { notificationType }),

  // ===== 异常处理 =====

  /**
   * 报告异常情况
   * @param {Number} recordId 记录ID
   * @param {String} exceptionType 异常类型
   * @param {String} description 异常描述
   * @returns {Promise<ResponseDTO<Void>>}
   */
  reportException: (recordId, exceptionType, description) =>
    postRequest('/api/v1/mobile/visitor/exception', { recordId, exceptionType, description }),

  // ===== 统计和报告 =====

  /**
   * 获取个人访问统计
   * @param {Number} userId 用户ID
   * @returns {Promise<ResponseDTO<Object>>}
   */
  getPersonalStatistics: (userId) =>
    getRequest(`/api/v1/mobile/visitor/statistics/${userId}`),

  /**
   * 导出访问记录
   * @param {String} startDate 开始日期
   * @param {String} endDate 结束日期
   * @returns {Promise<ResponseDTO<String>>}
   */
  exportRecords: (startDate, endDate) =>
    getRequest('/api/v1/mobile/visitor/export', { startDate, endDate }),

  // ===== 实用工具接口 =====

  /**
   * 验证访客信息
   * @param {String} idCardNumber 身份证号
   * @param {String} phoneNumber 手机号
   * @returns {Promise<ResponseDTO<Object>>}
   */
  validateVisitorInfo: (idCardNumber, phoneNumber) =>
    postRequest('/api/v1/mobile/visitor/validate', { idCardNumber, phoneNumber }),

  /**
   * 获取被访人信息
   * @param {Number} userId 被访人ID
   * @returns {Promise<ResponseDTO<Object>>}
   */
  getVisiteeInfo: (userId) =>
    getRequest(`/api/v1/mobile/visitor/visitee/${userId}`),

  /**
   * 获取访问区域列表
   * @returns {Promise<ResponseDTO<List<Object>>>}
   */
  getVisitAreas: () =>
    getRequest('/api/v1/mobile/visitor/areas'),

  /**
   * 获取预约类型列表
   * @returns {Promise<ResponseDTO<List<Object>>>}
   */
  getAppointmentTypes: () =>
    getRequest('/api/v1/mobile/visitor/appointment-types'),

  /**
   * 获取帮助信息
   * @param {String} helpType 帮助类型（可选）
   * @returns {Promise<ResponseDTO<Object>>}
   */
  getHelpInfo: (helpType) =>
    getRequest('/api/v1/mobile/visitor/help', { helpType }),

  // ===== PC端访客管理接口 =====

  /**
   * 分页查询访客预约（PC端）
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.visitorName 访客姓名（可选）
   * @param {Number} params.hostUserId 接待人ID（可选）
   * @param {String} params.startDate 开始日期（可选，格式：yyyy-MM-dd）
   * @param {String} params.endDate 结束日期（可选，格式：yyyy-MM-dd）
   * @param {String} params.status 预约状态（可选）
   * @returns {Promise<ResponseDTO<PageResult<VisitorAppointmentVO>>>}
   */
  queryAppointments: (params) =>
    postRequest('/api/v1/visitor/appointment/query', params),

  /**
   * 获取访客统计（PC端）
   * @param {String} startDate 开始日期（格式：yyyy-MM-dd）
   * @param {String} endDate 结束日期（格式：yyyy-MM-dd）
   * @returns {Promise<ResponseDTO<Object>>}
   */
  getVisitorStatistics: (startDate, endDate) =>
    getRequest('/api/v1/visitor/statistics', { startDate, endDate }),

  // ===== 黑名单管理接口 =====

  /**
   * 分页查询黑名单
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.visitorName 访客姓名（可选）
   * @param {String} params.phone 手机号（可选）
   * @param {String} params.idCard 身份证号（可选）
   * @param {String} params.reason 加入原因（可选）
   * @returns {Promise<ResponseDTO<PageResult<BlacklistVO>>>}
   */
  queryBlacklist: (params) =>
    postRequest('/api/v1/visitor/blacklist/query', params),

  /**
   * 加入黑名单
   * @param {Object} data 黑名单数据
   * @param {String} data.visitorName 访客姓名
   * @param {String} data.phone 手机号
   * @param {String} data.idCard 身份证号（可选）
   * @param {String} data.reason 加入原因（SECURITY/VIOLATION/OTHER）
   * @param {String} data.description 详细说明（可选）
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  addToBlacklist: (data) =>
    postRequest('/api/v1/visitor/blacklist/add', data),

  /**
   * 移出黑名单
   * @param {Array<Number>} visitorIds 访客ID列表
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  removeFromBlacklist: (visitorIds) =>
    postRequest('/api/v1/visitor/blacklist/remove', { visitorIds }),

  /**
   * 导出黑名单
   * @param {Object} params 查询参数
   * @returns {Promise<ResponseDTO<Blob>>}
   */
  exportBlacklist: (params) =>
    getRequest('/api/v1/visitor/blacklist/export', params, {
      responseType: 'blob',
    }),

  // ===== 车辆管理接口 =====

  /**
   * 分页查询车辆
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.vehicleNumber 车牌号（可选）
   * @param {String} params.vehicleType 车辆类型（可选）
   * @returns {Promise<ResponseDTO<PageResult<VehicleVO>>>}
   */
  queryVehicles: (params) =>
    postRequest('/api/v1/visitor/vehicle/query', params),

  /**
   * 添加车辆
   * @param {Object} data 车辆数据
   * @param {String} data.vehicleNumber 车牌号
   * @param {String} data.vehicleType 车辆类型
   * @param {String} data.brand 车辆品牌（可选）
   * @param {String} data.model 车辆型号（可选）
   * @param {String} data.color 车辆颜色（可选）
   * @param {String} data.remark 备注（可选）
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  addVehicle: (data) => postRequest('/api/v1/visitor/vehicle/add', data),

  /**
   * 更新车辆
   * @param {Object} data 车辆数据
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  updateVehicle: (data) => putRequest('/api/v1/visitor/vehicle/update', data),

  /**
   * 删除车辆
   * @param {Number} vehicleId 车辆ID
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  deleteVehicle: (vehicleId) =>
    deleteRequest(`/api/v1/visitor/vehicle/${vehicleId}`),

  // ===== 司机管理接口 =====

  /**
   * 分页查询司机
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.driverName 司机姓名（可选）
   * @param {String} params.phone 手机号（可选）
   * @param {String} params.licenseNumber 驾照号（可选）
   * @returns {Promise<ResponseDTO<PageResult<DriverVO>>>}
   */
  queryDrivers: (params) =>
    postRequest('/api/v1/visitor/driver/query', params),

  /**
   * 添加司机
   * @param {Object} data 司机数据
   * @param {String} data.driverName 司机姓名
   * @param {String} data.phone 手机号
   * @param {String} data.idCard 身份证号（可选）
   * @param {String} data.licenseNumber 驾照号
   * @param {String} data.licenseType 驾照类型（可选）
   * @param {String} data.remark 备注（可选）
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  addDriver: (data) => postRequest('/api/v1/visitor/driver/add', data),

  /**
   * 更新司机
   * @param {Object} data 司机数据
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  updateDriver: (data) => putRequest('/api/v1/visitor/driver/update', data),

  /**
   * 删除司机
   * @param {Number} driverId 司机ID
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  deleteDriver: (driverId) =>
    deleteRequest(`/api/v1/visitor/driver/${driverId}`),

  // ===== 电子出门单管理接口 =====

  /**
   * 分页查询电子出门单
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.passNo 出门单号（可选）
   * @param {String} params.vehicleNumber 车牌号（可选）
   * @param {String} params.status 状态（可选）
   * @returns {Promise<ResponseDTO<PageResult<ElectronicPassVO>>>}
   */
  queryElectronicPasses: (params) =>
    postRequest('/api/v1/visitor/pass/query', params),

  /**
   * 创建电子出门单
   * @param {Object} data 出门单数据
   * @param {String} data.vehicleNumber 车牌号
   * @param {String} data.driverName 司机姓名
   * @param {String} data.itemList 物品清单
   * @param {String} data.remark 备注（可选）
   * @returns {Promise<ResponseDTO<Long>>}
   */
  createElectronicPass: (data) =>
    postRequest('/api/v1/visitor/pass/create', data),

  /**
   * 更新电子出门单
   * @param {Object} data 出门单数据
   * @returns {Promise<ResponseDTO<Boolean>>}
   */
  updateElectronicPass: (data) =>
    putRequest('/api/v1/visitor/pass/update', data),

  /**
   * 打印电子出门单
   * @param {Number} passId 出门单ID
   * @returns {Promise<ResponseDTO<Blob>>}
   */
  printElectronicPass: (passId) =>
    getRequest(`/api/v1/visitor/pass/${passId}/print`, null, {
      responseType: 'blob',
    }),
};

export default visitorApi;

