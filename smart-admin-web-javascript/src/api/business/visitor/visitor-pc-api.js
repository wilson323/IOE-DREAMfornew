/*
 * 访客管理 PC端 API
 *
 * 完整实现访客管理系统的所有前端API接口
 * 包含7个子模块：访客信息、预约管理、登记管理、身份验证、物流管理、通行记录、统计分析
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest } from '/@/lib/axios';

export const visitorPcApi = {
  // ==================== 1. 访客信息管理 ====================

  /**
   * 分页查询访客列表
   */
  queryVisitors: (params) => {
    return postRequest('/visitor/info/query', params);
  },

  /**
   * 获取访客详情
   */
  getVisitorDetail: (visitorId) => {
    return getRequest(`/visitor/info/detail/${visitorId}`);
  },

  /**
   * 添加访客
   */
  addVisitor: (params) => {
    return postRequest('/visitor/info/add', params);
  },

  /**
   * 更新访客信息
   */
  updateVisitor: (params) => {
    return postRequest('/visitor/info/update', params);
  },

  /**
   * 删除访客
   */
  deleteVisitor: (visitorId) => {
    return postRequest(`/visitor/info/delete/${visitorId}`);
  },

  /**
   * 批量删除访客
   */
  batchDeleteVisitors: (visitorIds) => {
    return postRequest('/visitor/info/batch/delete', visitorIds);
  },

  /**
   * 导出访客数据
   */
  exportVisitors: (params) => {
    return postRequest('/visitor/info/export', params, {
      responseType: 'blob'
    });
  },

  /**
   * 查询访客等级列表
   */
  queryVisitorLevels: () => {
    return getRequest('/visitor/info/levels');
  },

  /**
   * 更新访客等级
   */
  updateVisitorLevel: (visitorId, level) => {
    return postRequest('/visitor/info/level/update', {
      visitorId,
      level
    });
  },

  // ==================== 2. 黑名单管理 ====================

  /**
   * 分页查询黑名单
   */
  queryBlacklist: (params) => {
    return postRequest('/visitor/blacklist/query', params);
  },

  /**
   * 获取黑名单详情
   */
  getBlacklistDetail: (blacklistId) => {
    return getRequest(`/visitor/blacklist/detail/${blacklistId}`);
  },

  /**
   * 添加到黑名单
   */
  addToBlacklist: (params) => {
    return postRequest('/visitor/blacklist/add', params);
  },

  /**
   * 从黑名单移除
   */
  removeFromBlacklist: (blacklistId) => {
    return postRequest(`/visitor/blacklist/remove/${blacklistId}`);
  },

  /**
   * 更新黑名单信息
   */
  updateBlacklist: (params) => {
    return postRequest('/visitor/blacklist/update', params);
  },

  /**
   * 批量移除黑名单
   */
  batchRemoveBlacklist: (blacklistIds) => {
    return postRequest('/visitor/blacklist/batch/remove', blacklistIds);
  },

  /**
   * 导出黑名单
   */
  exportBlacklist: (params) => {
    return postRequest('/visitor/blacklist/export', params, {
      responseType: 'blob'
    });
  },

  // ==================== 3. 预约管理 ====================

  /**
   * 分页查询预约列表
   */
  queryAppointments: (params) => {
    return postRequest('/visitor/appointment/query', params);
  },

  /**
   * 获取预约详情
   */
  getAppointmentDetail: (appointmentId) => {
    return getRequest(`/visitor/appointment/detail/${appointmentId}`);
  },

  /**
   * 创建访客预约
   */
  applyAppointment: (params) => {
    return postRequest('/visitor/appointment/apply', params);
  },

  /**
   * 更新预约信息
   */
  updateAppointment: (params) => {
    return postRequest('/visitor/appointment/update', params);
  },

  /**
   * 取消预约
   */
  cancelAppointment: (appointmentId, reason) => {
    return postRequest(`/visitor/appointment/cancel/${appointmentId}`, {
      reason
    });
  },

  /**
   * 删除预约
   */
  deleteAppointment: (appointmentId) => {
    return postRequest(`/visitor/appointment/delete/${appointmentId}`);
  },

  /**
   * 审批预约
   */
  approveAppointment: (appointmentId, approvalComment) => {
    return postRequest(`/visitor/appointment/approve/${appointmentId}`, {
      approvalComment
    });
  },

  /**
   * 拒绝预约
   */
  rejectAppointment: (appointmentId, rejectReason) => {
    return postRequest(`/visitor/appointment/reject/${appointmentId}`, {
      rejectReason
    });
  },

  /**
   * 批量审批预约
   */
  batchApproveAppointments: (appointmentIds, approvalComment) => {
    return postRequest('/visitor/appointment/batch/approve', {
      appointmentIds,
      approvalComment
    });
  },

  /**
   * 批量拒绝预约
   */
  batchRejectAppointments: (appointmentIds, rejectReason) => {
    return postRequest('/visitor/appointment/batch/reject', {
      appointmentIds,
      rejectReason
    });
  },

  /**
   * 生成访客码（二维码）
   */
  generateQRCode: (appointmentId) => {
    return getRequest(`/visitor/appointment/qrcode/generate/${appointmentId}`);
  },

  /**
   * 获取访客码
   */
  getQRCode: (appointmentId) => {
    return getRequest(`/visitor/appointment/qrcode/${appointmentId}`, {}, {
      responseType: 'blob'
    });
  },

  /**
   * 验证访客码
   */
  validateQRCode: (qrcode) => {
    return postRequest('/visitor/appointment/qrcode/validate', {
      qrcode
    });
  },

  /**
   * 导出预约数据
   */
  exportAppointments: (params) => {
    return postRequest('/visitor/appointment/export', params, {
      responseType: 'blob'
    });
  },

  /**
   * 获取被访人列表
   */
  getHostList: (params) => {
    return getRequest('/visitor/appointment/host/list', params);
  },

  /**
   * 获取可预约时间段
   */
  getAvailableTimeSlots: (hostId, date) => {
    return getRequest('/visitor/appointment/timeslots', {
      hostId,
      date
    });
  },

  /**
   * 检查预约冲突
   */
  checkAppointmentConflict: (params) => {
    return postRequest('/visitor/appointment/check/conflict', params);
  },

  // ==================== 4. 登记管理 ====================

  /**
   * 访客签到
   */
  checkInVisitor: (params) => {
    return postRequest('/visitor/register/checkin', params);
  },

  /**
   * 访客签退
   */
  checkOutVisitor: (registerId, satisfactionSurvey) => {
    return postRequest(`/visitor/register/checkout/${registerId}`, {
      satisfactionSurvey
    });
  },

  /**
   * 查询当前在访列表
   */
  queryCurrentVisitors: (params) => {
    return postRequest('/visitor/register/current/query', params);
  },

  /**
   * 查询登记记录
   */
  queryRegisterRecords: (params) => {
    return postRequest('/visitor/register/records/query', params);
  },

  /**
   * 获取登记详情
   */
  getRegisterDetail: (registerId) => {
    return getRequest(`/visitor/register/detail/${registerId}`);
  },

  /**
   * 补充登记信息
   */
  supplementRegisterInfo: (registerId, params) => {
    return postRequest(`/visitor/register/supplement/${registerId}`, params);
  },

  /**
   * 打印访客证
   */
  printVisitorPass: (registerId) => {
    return getRequest(`/visitor/register/pass/print/${registerId}`, {}, {
      responseType: 'blob'
    });
  },

  /**
   * 获取在访实时统计
   */
  getCurrentVisitorStats: () => {
    return getRequest('/visitor/register/current/stats');
  },

  /**
   * 滞留预警列表
   */
  getOvertimeAlertList: (params) => {
    return postRequest('/visitor/register/overtime/alerts', params);
  },

  /**
   * 导出登记记录
   */
  exportRegisterRecords: (params) => {
    return postRequest('/visitor/register/records/export', params, {
      responseType: 'blob'
    });
  },

  // ==================== 5. 身份验证 ====================

  /**
   * 人脸识别验证
   */
  faceRecognition: (params) => {
    return postRequest('/visitor/auth/face', params);
  },

  /**
   * 证件识别验证
   */
  idCardRecognition: (params) => {
    return postRequest('/visitor/auth/idcard', params);
  },

  /**
   * 二维码验证
   */
  qrcodeRecognition: (qrcode) => {
    return postRequest('/visitor/auth/qrcode', {
      qrcode
    });
  },

  /**
   * 手机号验证
   */
  phoneVerification: (phone, verifyCode) => {
    return postRequest('/visitor/auth/phone', {
      phone,
      verifyCode
    });
  },

  /**
   * 发送验证码
   */
  sendVerifyCode: (phone) => {
    return postRequest('/visitor/auth/verifycode/send', {
      phone
    });
  },

  /**
   * 上传人脸照片
   */
  uploadFacePhoto: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return postRequest('/visitor/auth/face/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },

  /**
   * 采集生物特征
   */
  captureBiometric: (registerId, biometricType) => {
    return postRequest('/visitor/auth/biometric/capture', {
      registerId,
      biometricType
    });
  },

  /**
   * 验证结果记录
   */
  getAuthRecords: (visitorId) => {
    return getRequest(`/visitor/auth/records/${visitorId}`);
  },

  // ==================== 6. 物流管理 ====================

  /**
   * 创建物流预约
   */
  createLogisticsReservation: (params) => {
    return postRequest('/visitor/logistics/reservation/create', params);
  },

  /**
   * 查询物流预约列表
   */
  queryLogisticsReservations: (params) => {
    return postRequest('/visitor/logistics/reservation/query', params);
  },

  /**
   * 获取物流预约详情
   */
  getLogisticsDetail: (reservationId) => {
    return getRequest(`/visitor/logistics/reservation/detail/${reservationId}`);
  },

  /**
   * 更新物流预约
   */
  updateLogisticsReservation: (params) => {
    return postRequest('/visitor/logistics/reservation/update', params);
  },

  /**
   * 取消物流预约
   */
  cancelLogisticsReservation: (reservationId, reason) => {
    return postRequest(`/visitor/logistics/reservation/cancel/${reservationId}`, {
      reason
    });
  },

  /**
   * 物流签到
   */
  logisticsCheckIn: (reservationId, params) => {
    return postRequest(`/visitor/logistics/checkin/${reservationId}`, params);
  },

  /**
   * 物流签退
   */
  logisticsCheckOut: (checkinId) => {
    return postRequest(`/visitor/logistics/checkout/${checkinId}`);
  },

  /**
   * 申请放行条
   */
  applyExitPass: (checkinId, params) => {
    return postRequest(`/visitor/logistics/exitpass/apply/${checkinId}`, params);
  },

  /**
   * 审批放行条
   */
  approveExitPass: (passId, approvalResult, approvalComment) => {
    return postRequest(`/visitor/logistics/exitpass/approve/${passId}`, {
      approvalResult,
      approvalComment
    });
  },

  /**
   * 获取放行条
   */
  getExitPass: (passId) => {
    return getRequest(`/visitor/logistics/exitpass/${passId}`, {}, {
      responseType: 'blob'
    });
  },

  /**
   * 查询物流记录
   */
  queryLogisticsRecords: (params) => {
    return postRequest('/visitor/logistics/records/query', params);
  },

  /**
   * 获取物流记录详情
   */
  getLogisticsRecordDetail: (recordId) => {
    return getRequest(`/visitor/logistics/records/detail/${recordId}`);
  },

  /**
   * 导出物流记录
   */
  exportLogisticsRecords: (params) => {
    return postRequest('/visitor/logistics/records/export', params, {
      responseType: 'blob'
    });
  },

  /**
   * 获取车辆信息
   */
  getVehicleInfo: (plateNumber) => {
    return getRequest('/visitor/logistics/vehicle/info', {
      plateNumber
    });
  },

  /**
   * 保存车辆信息
   */
  saveVehicleInfo: (params) => {
    return postRequest('/visitor/logistics/vehicle/save', params);
  },

  /**
   * 获取司机信息
   */
  getDriverInfo: (phone) => {
    return getRequest('/visitor/logistics/driver/info', {
      phone
    });
  },

  /**
   * 保存司机信息
   */
  saveDriverInfo: (params) => {
    return postRequest('/visitor/logistics/driver/save', params);
  },

  /**
   * 拍照上传货物照片
   */
  uploadGoodsPhoto: (checkinId, file) => {
    const formData = new FormData();
    formData.append('checkinId', checkinId);
    formData.append('file', file);
    return postRequest('/visitor/logistics/goods/photo/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },

  // ==================== 7. 通行记录 ====================

  /**
   * 查询访客通行记录
   */
  queryAccessRecords: (params) => {
    return postRequest('/visitor/access/records/query', params);
  },

  /**
   * 获取通行记录详情
   */
  getAccessRecordDetail: (recordId) => {
    return getRequest(`/visitor/access/records/detail/${recordId}`);
  },

  /**
   * 获取访客轨迹
   */
  getVisitorTrajectory: (visitorId, params) => {
    return postRequest('/visitor/access/trajectory', {
      visitorId,
      ...params
    });
  },

  /**
   * 查询异常记录
   */
  queryExceptionRecords: (params) => {
    return postRequest('/visitor/access/exceptions/query', params);
  },

  /**
   * 处理异常记录
   */
  handleExceptionRecord: (exceptionId, handleResult, handleComment) => {
    return postRequest(`/visitor/access/exceptions/handle/${exceptionId}`, {
      handleResult,
      handleComment
    });
  },

  /**
   * 导出通行记录
   */
  exportAccessRecords: (params) => {
    return postRequest('/visitor/access/records/export', params, {
      responseType: 'blob'
    });
  },

  /**
   * 实时通行监控数据
   */
  getRealtimeAccessData: () => {
    return getRequest('/visitor/access/realtime/data');
  },

  /**
   * 区域通行统计
   */
  getAreaAccessStats: (params) => {
    return getRequest('/visitor/access/area/stats', params);
  },

  // ==================== 8. 统计分析 ====================

  /**
   * 获取访客统计概览
   */
  getVisitorStatistics: (params) => {
    return getRequest('/visitor/statistics/overview', params);
  },

  /**
   * 预约统计分析
   */
  getAppointmentStatistics: (params) => {
    return getRequest('/visitor/statistics/appointment', params);
  },

  /**
   * 登记统计分析
   */
  getRegisterStatistics: (params) => {
    return getRequest('/visitor/statistics/register', params);
  },

  /**
   * 物流统计分析
   */
  getLogisticsStatistics: (params) => {
    return getRequest('/visitor/statistics/logistics', params);
  },

  /**
   * 访客流量趋势
   */
  getVisitorTrend: (params) => {
    return getRequest('/visitor/statistics/trend', params);
  },

  /**
   * 访客时段分布
   */
  getTimeDistribution: (params) => {
    return getRequest('/visitor/statistics/timedistribution', params);
  },

  /**
   * 访客区域分布
   */
  getAreaDistribution: (params) => {
    return getRequest('/visitor/statistics/areadistribution', params);
  },

  /**
   * 访客类型统计
   */
  getVisitorTypeStats: (params) => {
    return getRequest('/visitor/statistics/visitortype', params);
  },

  /**
   * 访问目的统计
   */
  getVisitPurposeStats: (params) => {
    return getRequest('/visitor/statistics/visitpurpose', params);
  },

  /**
   * 滞留时长统计
   */
  getStayDurationStats: (params) => {
    return getRequest('/visitor/statistics/stayduration', params);
  },

  /**
   * 预约完成率统计
   */
  getAppointmentCompletionRate: (params) => {
    return getRequest('/visitor/statistics/completionrate', params);
  },

  /**
   * 黑名单统计
   */
  getBlacklistStats: (params) => {
    return getRequest('/visitor/statistics/blacklist', params);
  },

  /**
   * 访客满意度统计
   */
  getSatisfactionStats: (params) => {
    return getRequest('/visitor/statistics/satisfaction', params);
  },

  /**
   * 生成统计报表
   */
  generateStatisticsReport: (params) => {
    return postRequest('/visitor/statistics/report/generate', params, {
      responseType: 'blob'
    });
  },

  /**
   * 获取报表列表
   */
  getReportList: (params) => {
    return getRequest('/visitor/statistics/report/list', params);
  },

  /**
   * 下载报表
   */
  downloadReport: (reportId) => {
    return getRequest(`/visitor/statistics/report/download/${reportId}`, {}, {
      responseType: 'blob'
    });
  },
};
