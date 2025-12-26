/**
 * 门禁管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest } from '@/lib/smart-request'

// 门禁检查相关接口
export const accessCheckApi = {
  /**
   * 移动端门禁检查
   * @param {Object} data 检查请求数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.deviceId 设备ID
   * @param {Number} data.areaId 区域ID
   * @param {String} data.verificationType 验证类型
   * @param {String} data.location 位置信息
   * @returns {Promise}
   */
  mobileAccessCheck: (data) => postRequest('/api/v1/mobile/access/check', data),

  /**
   * 二维码验证
   * @param {Object} data 验证数据
   * @param {String} data.qrCode 二维码
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyQRCode: (data) => postRequest('/api/v1/mobile/access/qr/verify', data),

  /**
   * NFC验证
   * @param {Object} data 验证数据
   * @param {String} data.nfcCardId NFC卡ID
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyNFC: (data) => postRequest('/api/v1/mobile/access/nfc/verify', data),

  /**
   * 生物识别验证
   * @param {Object} data 验证数据
   * @param {Number} data.userId 用户ID
   * @param {String} data.biometricType 生物识别类型
   * @param {String} data.biometricData 生物识别数据
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyBiometric: (data) => postRequest('/api/v1/mobile/access/biometric/verify', data)
}

// 设备管理相关接口
export const deviceApi = {
  /**
   * 获取设备列表
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码（默认1）
   * @param {Number} params.pageSize 每页数量（默认20）
   * @param {Number} params.areaId 区域ID（可选）
   * @param {Number} params.status 设备状态（可选：1-在线 0-离线）
   * @param {String} params.deviceName 设备名称（可选，模糊搜索）
   * @returns {Promise}
   */
  getDeviceList: (params) =>
    getRequest('/api/v1/access/device/list', params),

  /**
   * 获取附近设备
   * @param {Number} userId 用户ID
   * @param {Number} latitude 纬度
   * @param {Number} longitude 经度
   * @param {Number} radius 半径（米，默认500）
   * @returns {Promise}
   */
  getNearbyDevices: (userId, latitude, longitude, radius = 500) =>
    getRequest('/api/v1/mobile/access/devices/nearby', {
      userId,
      latitude,
      longitude,
      radius
    }),

  /**
   * 获取设备详情
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getDeviceDetail: (deviceId) =>
    getRequest(`/api/v1/access/device/${deviceId}`),

  /**
   * 新增设备
   * @param {Object} data 设备数据
   * @param {String} data.deviceName 设备名称
   * @param {Number} data.deviceType 设备类型（1-门禁控制器 2-门禁一体机）
   * @param {String} data.deviceModel 设备型号
   * @param {Number} data.commType 通讯方式（1-TCP/IP 2-RS485）
   * @param {String} data.ipAddress IP地址
   * @param {Number} data.port 端口号
   * @param {Number} data.areaId 关联区域ID
   * @param {String} data.locationDesc 位置描述
   * @returns {Promise}
   */
  addDevice: (data) =>
    postRequest('/api/v1/access/device/add', data),

  /**
   * 编辑设备
   * @param {Number} deviceId 设备ID
   * @param {Object} data 设备数据
   * @returns {Promise}
   */
  updateDevice: (deviceId, data) =>
    postRequest(`/api/v1/access/device/${deviceId}/update`, data),

  /**
   * 删除设备
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  deleteDevice: (deviceId) =>
    postRequest(`/api/v1/access/device/${deviceId}/delete`),

  /**
   * 搜索设备（网络搜索）
   * @param {String} ipAddress IP地址段（可选）
   * @param {Number} startPort 起始端口（可选）
   * @param {Number} endPort 结束端口（可选）
   * @returns {Promise}
   */
  searchDevices: (ipAddress, startPort, endPort) =>
    getRequest('/api/v1/access/device/search', { ipAddress, startPort, endPort }),

  /**
   * 设备远程控制
   * @param {Number} deviceId 设备ID
   * @param {String} action 控制动作（reboot-重启 enable-启用 disable-禁用）
   * @returns {Promise}
   */
  deviceControl: (deviceId, action) =>
    postRequest(`/api/v1/access/device/${deviceId}/control`, { action }),

  /**
   * 获取实时门禁状态
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getRealTimeStatus: (deviceId) =>
    getRequest('/api/v1/mobile/access/status/realtime', { deviceId }),

  /**
   * 批量删除设备
   * @param {Array<Number>} deviceIds 设备ID列表
   * @returns {Promise}
   */
  batchDeleteDevices: (deviceIds) =>
    postRequest('/api/v1/access/device/batch-delete', { deviceIds }),

  /**
   * 批量启用/禁用设备
   * @param {Array<Number>} deviceIds 设备ID列表
   * @param {Number} status 状态（1-启用 0-禁用）
   * @returns {Promise}
   */
  batchUpdateDeviceStatus: (deviceIds, status) =>
    postRequest('/api/v1/access/device/batch-status', { deviceIds, status }),

  /**
   * 远程开门
   * @param {Number} doorId 门ID
   * @param {String} reason 操作原因
   * @returns {Promise}
   */
  remoteOpenDoor: (doorId, reason) =>
    postRequest('/api/v1/access/door/remote-open', { doorId, reason }),

  /**
   * 远程常开
   * @param {Number} doorId 门ID
   * @param {String} reason 操作原因
   * @returns {Promise}
   */
  remoteKeepOpen: (doorId, reason) =>
    postRequest('/api/v1/access/door/keep-open', { doorId, reason }),

  /**
   * 远程常闭
   * @param {Number} doorId 门ID
   * @param {String} reason 操作原因
   * @returns {Promise}
   */
  remoteKeepClosed: (doorId, reason) =>
    postRequest('/api/v1/access/door/keep-closed', { doorId, reason }),

  /**
   * 更新设备验证参数
   * @param {Number} deviceId 设备ID
   * @param {Object} params 验证参数
   * @returns {Promise}
   */
  updateDeviceVerifyParams: (deviceId, params) =>
    postRequest(`/api/v1/access/device/${deviceId}/verify-params`, params),

  /**
   * 更新设备通讯密码
   * @param {Number} deviceId 设备ID
   * @param {Object} data 密码数据
   * @returns {Promise}
   */
  updateDevicePassword: (deviceId, data) =>
    postRequest(`/api/v1/access/device/${deviceId}/password`, data),

  /**
   * 同步设备时间
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  syncDeviceTime: (deviceId) =>
    postRequest(`/api/v1/access/device/${deviceId}/sync-time`, {}),

  /**
   * 清除设备命令缓存
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  clearDeviceCommands: (deviceId) =>
    postRequest(`/api/v1/access/device/${deviceId}/clear-commands`, {}),

  /**
   * 获取设备固件版本
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getFirmwareVersion: (deviceId) =>
    getRequest(`/api/v1/access/device/${deviceId}/firmware-version`, {}),

  /**
   * 升级设备固件
   * @param {Number} deviceId 设备ID
   * @param {String} firmwareUrl 固件下载地址
   * @returns {Promise}
   */
  upgradeFirmware: (deviceId, firmwareUrl) =>
    postRequest(`/api/v1/access/device/${deviceId}/upgrade-firmware`, { firmwareUrl }),

  /**
   * 获取设备运行状态
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getDeviceRuntimeStatus: (deviceId) =>
    getRequest(`/api/v1/access/device/${deviceId}/runtime-status`, {}),

  /**
   * 获取设备统计信息
   * @param {Number} deviceId 设备ID
   * @param {Object} params 统计参数
   * @returns {Promise}
   */
  getDeviceStatistics: (deviceId, params) =>
    getRequest(`/api/v1/access/device/${deviceId}/statistics`, params),

  // ============ 新增设备管理API（移动端完整实现）============

  /**
   * 获取设备人员列表
   * @param {Number} deviceId 设备ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getDevicePersonnel: (deviceId, params) =>
    getRequest(`/api/v1/mobile/access/device/${deviceId}/personnel`, params),

  /**
   * 获取设备通行记录
   * @param {Number} deviceId 设备ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getDeviceRecords: (deviceId, params) =>
    getRequest(`/api/v1/mobile/access/device/${deviceId}/records`, params),

  /**
   * 获取设备访问规则
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getDeviceRules: (deviceId) =>
    getRequest(`/api/v1/mobile/access/device/${deviceId}/rules`),

  /**
   * 获取设备容量统计
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getDeviceCapacity: (deviceId) =>
    getRequest(`/api/v1/mobile/access/device/${deviceId}/capacity`),

  /**
   * 更新设备高级设置
   * @param {Number} deviceId 设备ID
   * @param {Object} settings 设置数据
   * @returns {Promise}
   */
  updateDeviceSettings: (deviceId, settings) =>
    postRequest(`/api/v1/mobile/access/device/${deviceId}/settings`, settings),

  /**
   * 更新设备通信设置
   * @param {Number} deviceId 设备ID
   * @param {Object} commConfig 通信配置
   * @returns {Promise}
   */
  updateDeviceCommunication: (deviceId, commConfig) =>
    postRequest(`/api/v1/mobile/access/device/${deviceId}/communication`, commConfig),

  /**
   * 搜索设备
   * @param {Object} params 搜索参数
   * @param {String} params.keyword 关键词
   * @returns {Promise}
   */
  searchDevices: (params) =>
    getRequest('/api/v1/mobile/access/device/search', params),

  /**
   * 导出设备列表
   * @param {Object} params 导出参数
   * @returns {Promise}
   */
  exportDevices: (params) =>
    getRequest('/api/v1/mobile/access/device/export', params)
}

// 区域管理相关接口
export const areaApi = {
  /**
   * 获取区域列表
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码（默认1）
   * @param {Number} params.pageSize 每页数量（默认20）
   * @param {String} params.areaName 区域名称（可选，模糊搜索）
   * @param {String} params.areaType 区域类型（可选）
   * @param {Number} params.enabled 状态（可选：1-启用 0-禁用）
   * @returns {Promise}
   */
  getAreaList: (params) =>
    getRequest('/api/v1/access/area/list', params),

  /**
   * 获取区域树
   * @returns {Promise}
   */
  getAreaTree: () =>
    getRequest('/api/v1/access/area/tree'),

  /**
   * 获取区域详情
   * @param {Number} areaId 区域ID
   * @returns {Promise}
   */
  getAreaDetail: (areaId) =>
    getRequest(`/api/v1/access/area/${areaId}`),

  /**
   * 新增区域
   * @param {Object} data 区域数据
   * @param {String} data.areaName 区域名称
   * @param {String} data.areaType 区域类型
   * @param {Number} data.parentId 父区域ID
   * @param {String} data.location 位置描述
   * @param {String} data.remark 备注
   * @returns {Promise}
   */
  addArea: (data) =>
    postRequest('/api/v1/access/area/add', data),

  /**
   * 编辑区域
   * @param {Number} areaId 区域ID
   * @param {Object} data 区域数据
   * @returns {Promise}
   */
  updateArea: (areaId, data) =>
    postRequest(`/api/v1/access/area/${areaId}/update`, data),

  /**
   * 删除区域
   * @param {Number} areaId 区域ID
   * @returns {Promise}
   */
  deleteArea: (areaId) =>
    postRequest(`/api/v1/access/area/${areaId}/delete`),

  /**
   * 获取区域权限用户
   * @param {Number} areaId 区域ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getAreaPermissions: (areaId, params) =>
    getRequest(`/api/v1/access/area/${areaId}/permissions`, params),

  /**
   * 设置区域权限
   * @param {Number} areaId 区域ID
   * @param {Object} data 权限数据
   * @param {Array<Number>} data.userIds 用户ID列表
   * @param {String} data.permissionType 权限类型
   * @returns {Promise}
   */
  setAreaPermissions: (areaId, data) =>
    postRequest(`/api/v1/access/area/${areaId}/permissions`, data),

  /**
   * 获取区域设备
   * @param {Number} areaId 区域ID
   * @returns {Promise}
   */
  getAreaDevices: (areaId) =>
    getRequest(`/api/v1/access/area/${areaId}/devices`),

  /**
   * 批量删除区域
   * @param {Array<Number>} areaIds 区域ID列表
   * @returns {Promise}
   */
  batchDeleteAreas: (areaIds) =>
    postRequest('/api/v1/access/area/batch-delete', { areaIds }),

  /**
   * 获取区域统计
   * @param {Number} areaId 区域ID
   * @returns {Promise}
   */
  getAreaStatistics: (areaId) =>
    getRequest(`/api/v1/access/area/${areaId}/statistics`)
}

// 权限管理相关接口
export const permissionApi = {
  /**
   * 获取用户门禁权限
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserPermissions: (userId) =>
    getRequest(`/api/v1/mobile/access/permissions/${userId}`)
}

// 访问记录相关接口
export const recordApi = {
  /**
   * 获取用户访问记录
   * @param {Number} userId 用户ID
   * @param {Number} size 记录数量（默认20）
   * @returns {Promise}
   */
  getUserAccessRecords: (userId, size = 20) =>
    getRequest(`/api/v1/mobile/access/records/${userId}`, { size }),

  /**
   * 分页查询访问记录
   * @param {Object} params 查询参数
   * @param {Number} params.userId 用户ID
   * @param {Number} params.pageNum 页码（默认1）
   * @param {Number} params.pageSize 每页数量（默认20）
   * @param {String} params.startDate 开始日期（可选）
   * @param {String} params.endDate 结束日期（可选）
   * @param {String} params.status 状态筛选（可选：success/fail）
   * @returns {Promise}
   */
  queryAccessRecords: (params) =>
    getRequest('/api/v1/access/record/query', params),

  /**
   * 获取访问记录统计
   * @param {Object} params 统计参数
   * @param {String} params.startDate 开始日期（可选）
   * @param {String} params.endDate 结束日期（可选）
   * @param {Number} params.areaId 区域ID（可选）
   * @param {Number} params.userId 用户ID（可选）
   * @returns {Promise}
   */
  getAccessRecordStatistics: (params) =>
    getRequest('/api/v1/access/record/statistics', params)
}

// 临时访问相关接口
export const temporaryApi = {
  /**
   * 临时开门申请
   * @param {Object} data 申请数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.deviceId 设备ID
   * @param {String} data.reason 申请原因
   * @returns {Promise}
   */
  requestTemporaryAccess: (data) =>
    postRequest('/api/v1/mobile/access/temporary-access', data)
}

// 通知相关接口
export const notificationApi = {
  /**
   * 发送推送通知
   * @param {Object} data 通知数据
   * @param {Number} data.userId 用户ID
   * @param {String} data.notificationType 通知类型
   * @returns {Promise}
   */
  sendPushNotification: (data) =>
    postRequest('/api/v1/mobile/access/notification/push', data)
}

// 审批流程相关接口
export const approvalApi = {
  /**
   * 获取审批列表
   * @param {Object} params 查询参数
   * @param {String} params.tabType Tab类型（myApply-我的申请 pending-待我审批 approved-已审批）
   * @param {Number} params.pageNum 页码（默认1）
   * @param {Number} params.pageSize 每页数量（默认20）
   * @param {Number} params.approvalStatus 审批状态（可选：1-待审批 2-已通过 3-已拒绝）
   * @param {Number} params.approvalType 申请类型（可选：1-权限申请 2-访客预约 3-紧急权限）
   * @param {String} params.timeRange 时间范围（可选：today-今天 week-本周 month-本月）
   * @returns {Promise}
   */
  getApprovalList: (params) =>
    getRequest('/api/v1/mobile/access/approval/list', params),

  /**
   * 获取审批统计
   * @param {Object} params 统计参数
   * @param {String} params.tabType Tab类型
   * @returns {Promise}
   */
  getApprovalStatistics: (params) =>
    getRequest('/api/v1/mobile/access/approval/statistics', params),

  /**
   * 获取审批详情
   * @param {Number} approvalId 审批ID
   * @returns {Promise}
   */
  getApprovalDetail: (approvalId) =>
    getRequest(`/api/v1/mobile/access/approval/${approvalId}`),

  /**
   * 审批操作（通过/拒绝）
   * @param {Number} approvalId 审批ID
   * @param {Object} data 审批数据
   * @param {String} data.action 操作类型（approve-通过 reject-拒绝）
   * @param {String} data.comment 审批意见
   * @returns {Promise}
   */
  approveRequest: (approvalId, data) =>
    postRequest(`/api/v1/mobile/access/approval/${approvalId}/approve`, data),

  /**
   * 撤回申请
   * @param {Number} approvalId 审批ID
   * @returns {Promise}
   */
  withdrawApproval: (approvalId) =>
    postRequest(`/api/v1/mobile/access/approval/${approvalId}/withdraw`, {}),

  /**
   * 创建权限申请
   * @param {Object} data 申请数据
   * @returns {Promise}
   */
  createPermissionApply: (data) =>
    postRequest('/api/v1/mobile/access/approval/permission-apply', data)
}

// 访客预约相关接口
export const visitorApi = {
  /**
   * 获取员工列表（用于选择被访人）
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码（默认1）
   * @param {Number} params.pageSize 每页数量（默认1000）
   * @param {Number} params.enabled 状态（可选：1-启用 0-禁用）
   * @returns {Promise}
   */
  getEmployeeList: (params) =>
    getRequest('/api/v1/mobile/access/employee/list', params),

  /**
   * 创建访客预约
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  createVisitorAppointment: (data) =>
    postRequest('/api/v1/mobile/access/visitor/appointment', data),

  /**
   * 获取访客预约列表
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码（默认1）
   * @param {Number} params.pageSize 每页数量（默认20）
   * @param {Number} params.appointmentStatus 预约状态（可选）
   * @returns {Promise}
   */
  getVisitorAppointmentList: (params) =>
    getRequest('/api/v1/mobile/access/visitor/appointments', params),

  /**
   * 生成访客二维码
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  generateVisitorQRCode: (appointmentId) =>
    getRequest(`/api/v1/mobile/access/visitor/${appointmentId}/qrcode`),

  /**
   * 取消访客预约
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  cancelVisitorAppointment: (appointmentId) =>
    postRequest(`/api/v1/mobile/access/visitor/${appointmentId}/cancel`, {}),

  /**
   * 访客签到
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  visitorCheckIn: (appointmentId) =>
    postRequest(`/api/v1/mobile/access/visitor/${appointmentId}/check-in`, {}),

  /**
   * 访客签退
   * @param {Number} appointmentId 预约ID
   * @returns {Promise}
   */
  visitorCheckOut: (appointmentId) =>
    postRequest(`/api/v1/mobile/access/visitor/${appointmentId}/check-out`, {})
}

// 紧急权限相关接口
export const emergencyApi = {
  /**
   * 创建紧急权限申请
   * @param {Object} data 申请数据
   * @returns {Promise}
   */
  createEmergencyPermission: (data) =>
    postRequest('/api/v1/mobile/access/emergency/apply', data),

  /**
   * 获取紧急权限申请列表
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码（默认1）
   * @param {Number} params.pageSize 每页数量（默认20）
   * @param {Number} params.urgencyLevel 紧急程度（可选：1-一般 2-紧急 3-非常紧急）
   * @param {Number} params.approvalStatus 审批状态（可选）
   * @returns {Promise}
   */
  getEmergencyPermissionList: (params) =>
    getRequest('/api/v1/mobile/access/emergency/list', params)
}

// 远程控制相关接口
export const remoteControlApi = {
  /**
   * 远程开门
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  remoteOpenDoor: (deviceId) =>
    postRequest(`/api/v1/mobile/access/door/${deviceId}/open`, {}),

  /**
   * 远程锁定
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  remoteLockDoor: (deviceId) =>
    postRequest(`/api/v1/mobile/access/door/${deviceId}/lock`, {}),

  /**
   * 远程解锁
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  unlockDoor: (deviceId) =>
    postRequest(`/api/v1/mobile/access/door/${deviceId}/unlock`, {}),

  /**
   * 复位门禁
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  resetDoor: (deviceId) =>
    postRequest(`/api/v1/mobile/access/door/${deviceId}/reset`, {}),

  /**
   * 批量开门
   * @param {Array<Number>} deviceIds 设备ID列表
   * @returns {Promise}
   */
  batchOpenDoors: (deviceIds) =>
    postRequest('/api/v1/mobile/access/door/batch-open', { deviceIds }),

  /**
   * 批量锁定
   * @param {Array<Number>} deviceIds 设备ID列表
   * @returns {Promise}
   */
  batchLockDoors: (deviceIds) =>
    postRequest('/api/v1/mobile/access/door/batch-lock', { deviceIds }),

  /**
   * 批量复位
   * @param {Array<Number>} deviceIds 设备ID列表
   * @returns {Promise}
   */
  batchResetDoors: (deviceIds) =>
    postRequest('/api/v1/mobile/access/door/batch-reset', { deviceIds }),

  /**
   * 获取门禁设置
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getDoorSettings: (deviceId) =>
    getRequest(`/api/v1/mobile/access/door/${deviceId}/settings`),

  /**
   * 更新门禁设置
   * @param {Number} deviceId 设备ID
   * @param {Object} settings 设置数据
   * @param {Number} settings.openDuration 开门时长（秒）
   * @param {Boolean} settings.autoLock 自动锁门
   * @param {Boolean} settings.keepOpen 常开模式
   * @param {Boolean} settings.antiPassback 反潜回
   * @param {Boolean} settings.duressCode 胁迫码报警
   * @param {Boolean} settings.abnormalAlarm 异常开门报警
   * @returns {Promise}
   */
  updateDoorSettings: (deviceId, settings) =>
    postRequest(`/api/v1/mobile/access/door/${deviceId}/settings`, settings),

  /**
   * 获取通行记录
   * @param {Object} params 查询参数
   * @param {Number} params.deviceId 设备ID
   * @param {Number} params.startTime 开始时间戳
   * @param {Number} params.endTime 结束时间戳
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 每页数量
   * @returns {Promise}
   */
  getPassRecords: (params) =>
    getRequest('/api/v1/mobile/access/door/records', params),

  /**
   * 获取门禁事件日志
   * @param {Object} params 查询参数
   * @param {Number} params.deviceId 设备ID
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 每页数量
   * @returns {Promise}
   */
  getDoorEvents: (params) =>
    getRequest('/api/v1/mobile/access/door/events', params),

  /**
   * 实时刷新门禁状态
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  refreshDoorStatus: (deviceId) =>
    getRequest(`/api/v1/mobile/access/door/${deviceId}/status`),

  /**
   * 获取门禁统计信息
   * @param {Number} deviceId 设备ID
   * @param {Object} params 统计参数
   * @returns {Promise}
   */
  getDoorStatistics: (deviceId, params) =>
    getRequest(`/api/v1/mobile/access/door/${deviceId}/statistics`, params),

  /**
   * 远程常开
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  remoteKeepOpen: (deviceId) =>
    postRequest(`/api/v1/mobile/access/door/${deviceId}/keep-open`, {}),

  /**
   * 取消常开
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  cancelKeepOpen: (deviceId) =>
    postRequest(`/api/v1/mobile/access/door/${deviceId}/cancel-keep-open`, {}),

  /**
   * 清除门禁告警
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  clearDoorAlarm: (deviceId) =>
    postRequest(`/api/v1/mobile/access/door/${deviceId}/clear-alarm`, {}),

  /**
   * 获取区域门禁列表
   * @param {Number} areaId 区域ID
   * @returns {Promise}
   */
  getAreaDoors: (areaId) =>
    getRequest(`/api/v1/mobile/access/area/${areaId}/doors`),

  /**
   * 获取在线门禁列表
   * @param {Number} areaId 区域ID（可选）
   * @returns {Promise}
   */
  getOnlineDoors: (areaId) =>
    getRequest('/api/v1/mobile/access/door/online-list', { areaId })
}

// 权限管理相关接口
export const permissionManageApi = {
  /**
   * 获取用户权限列表
   * @param {Object} params 查询参数
   * @param {Number} params.permissionStatus 权限状态（1-有效 2-即将过期 3-已过期）
   * @param {Number} params.pageNum 页码（默认1）
   * @param {Number} params.pageSize 每页数量（默认20）
   * @param {String} params.keyword 搜索关键词（区域名称）
   * @returns {Promise}
   */
  getUserPermissions: (params) =>
    getRequest('/api/v1/mobile/access/permission/list', params),

  /**
   * 获取用户权限统计
   * @returns {Promise}
   */
  getUserPermissionStatistics: () =>
    getRequest('/api/v1/mobile/access/permission/statistics'),

  /**
   * 获取权限详情
   * @param {Number} permissionId 权限ID
   * @returns {Promise}
   */
  getPermissionDetail: (permissionId) =>
    getRequest(`/api/v1/mobile/access/permission/${permissionId}`),

  /**
   * 获取权限二维码
   * @param {Number} permissionId 权限ID
   * @returns {Promise}
   */
  getPermissionQRCode: (permissionId) =>
    getRequest(`/api/v1/mobile/access/permission/${permissionId}/qrcode`),

  /**
   * 获取权限通行记录
   * @param {Number} permissionId 权限ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getPermissionRecords: (permissionId, params) =>
    getRequest(`/api/v1/mobile/access/permission/${permissionId}/records`, params),

  /**
   * 获取权限历史
   * @param {Number} permissionId 权限ID
   * @returns {Promise}
   */
  getPermissionHistory: (permissionId) =>
    getRequest(`/api/v1/mobile/access/permission/${permissionId}/history`),

  /**
   * 续期权限
   * @param {Number} permissionId 权限ID
   * @param {Object} data 续期数据
   * @param {Number} data.duration 续期时长（天）
   * @param {String} data.reason 续期原因
   * @returns {Promise}
   */
  renewPermission: (permissionId, data) =>
    postRequest(`/api/v1/mobile/access/permission/${permissionId}/renew`, data),

  /**
   * 转让权限
   * @param {Number} permissionId 权限ID
   * @param {Object} data 转让数据
   * @param {String} data.targetUserId 目标用户ID
   * @param {String} data.reason 转让原因
   * @returns {Promise}
   */
  transferPermission: (permissionId, data) =>
    postRequest(`/api/v1/mobile/access/permission/${permissionId}/transfer`, data),

  /**
   * 冻结权限
   * @param {Number} permissionId 权限ID
   * @returns {Promise}
   */
  freezePermission: (permissionId) =>
    postRequest(`/api/v1/mobile/access/permission/${permissionId}/freeze`, {}),

  /**
   * 解冻权限
   * @param {Number} permissionId 权限ID
   * @returns {Promise}
   */
  unfreezePermission: (permissionId) =>
    postRequest(`/api/v1/mobile/access/permission/${permissionId}/unfreeze`, {}),

  /**
   * 导出权限凭证
   * @param {Number} permissionId 权限ID
   * @returns {Promise}
   */
  exportPermissionCertificate: (permissionId) =>
    getRequest(`/api/v1/mobile/access/permission/${permissionId}/export`),

  /**
   * 分享权限
   * @param {Number} permissionId 权限ID
   * @param {Object} data 分享数据
   * @returns {Promise}
   */
  sharePermission: (permissionId, data) =>
    postRequest(`/api/v1/mobile/access/permission/${permissionId}/share`, data)
}

// 即将过期权限相关接口
export const expireApi = {
  /**
   * 获取即将过期权限统计
   * @returns {Promise}
   */
  getExpiringStatistics: () =>
    getRequest('/api/v1/mobile/access/permission/expiring/statistics'),

  /**
   * 获取即将过期权限列表
   * @param {Object} params 查询参数
   * @param {Number} params.advanceDays 提前天数
   * @param {String} params.urgentLevel 紧急等级（urgent-紧急 warning-提醒 normal-一般）
   * @returns {Promise}
   */
  getExpiringPermissions: (params) =>
    getRequest('/api/v1/mobile/access/permission/expiring/list', params),

  /**
   * 批量续期
   * @param {Object} data 续期数据
   * @param {Array<Number>} data.permissionIds 权限ID列表
   * @param {Number} data.duration 统一续期时长（天）
   * @param {String} data.reason 续期原因
   * @returns {Promise}
   */
  batchRenewPermissions: (data) =>
    postRequest('/api/v1/mobile/access/permission/batch-renew', data),

  /**
   * 获取过期提醒设置
   * @returns {Promise}
   */
  getExpireReminderSettings: () =>
    getRequest('/api/v1/mobile/access/permission/expire-settings'),

  /**
   * 更新过期提醒设置
   * @param {Object} data 设置数据
   * @param {Number} data.advanceDays 提前提醒天数
   * @param {Object} data.settings 提醒方式设置
   * @returns {Promise}
   */
  updateExpireReminderSettings: (data) =>
    postRequest('/api/v1/mobile/access/permission/expire-settings', data),

  /**
   * 关闭过期提醒
   * @param {Number} permissionId 权限ID
   * @returns {Promise}
   */
  dismissExpireReminder: (permissionId) =>
    postRequest(`/api/v1/mobile/access/permission/${permissionId}/dismiss-reminder`, {})
}

// 导出所有API
export default {
  ...accessCheckApi,
  ...deviceApi,
  ...areaApi,
  ...permissionApi,
  ...recordApi,
  ...temporaryApi,
  ...notificationApi,
  ...approvalApi,
  ...visitorApi,
  ...emergencyApi,
  ...remoteControlApi,
  ...permissionManageApi,
  ...expireApi,
  ...offlineSyncApi
}

// 离线同步相关接口
export const offlineSyncApi = {
  /**
   * 获取离线同步数据
   * @param {Object} params 同步参数
   * @param {Number} params.lastSyncTime 上次同步时间戳（毫秒）
   * @param {String} params.dataType 数据类型（permissions-权限数据 all-全部数据）
   * @param {String} params.clientVersion 客户端版本号
   * @param {String} params.deviceId 设备唯一标识
   * @returns {Promise}
   */
  getOfflineSyncData: (params) =>
    getRequest('/api/v1/mobile/access/offline/sync-data', params),

  /**
   * 上传离线通行记录
   * @param {Object} data 上传数据
   * @param {Array} data.records 离线记录列表
   * @param {String} data.deviceId 设备ID
   * @param {String} data.deviceUuid 设备唯一标识
   * @returns {Promise}
   */
  uploadOfflineRecords: (data) =>
    postRequest('/api/v1/mobile/access/offline/upload-records', data),

  /**
   * 获取同步状态
   * @returns {Promise}
   */
  getSyncStatus: () =>
    getRequest('/api/v1/mobile/access/offline/sync-status'),

  /**
   * 立即同步
   * @param {Object} data 同步参数
   * @returns {Promise}
   */
  syncNow: (data) =>
    postRequest('/api/v1/mobile/access/offline/sync-now', data)
}

