/**
 * 考勤设备管理API接口
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-24
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { postRequest, getRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const attendanceDeviceApi = {
  // ===== 设备基础管理 =====

  /**
   * 获取设备列表
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.deviceName 设备名称(可选)
   * @param {String} params.deviceCode 设备编码(可选)
   * @param {String} params.deviceType 设备类型(可选)
   * @param {Number} params.areaId 区域ID(可选)
   * @param {Number} params.status 状态(可选)
   */
  getDeviceList: (params) =>
    getRequest('/attendance/devices', params),

  /**
   * 获取设备详情
   * @param {Number} deviceId 设备ID
   */
  getDeviceDetail: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}`),

  /**
   * 新增设备
   * @param {Object} data 设备数据
   * @param {String} data.deviceName 设备名称
   * @param {String} data.deviceCode 设备编码
   * @param {String} data.deviceType 设备类型
   * @param {String} data.manufacturer 厂商(可选)
   * @param {String} data.model 型号(可选)
   * @param {String} data.ipAddress IP地址
   * @param {Number} data.port 端口号
   * @param {String} data.location 安装位置
   * @param {Number} data.areaId 所属区域ID(可选)
   */
  createDevice: (data) =>
    postRequest('/attendance/devices', data),

  /**
   * 更新设备
   * @param {Object} data 设备数据
   */
  updateDevice: (data) =>
    putRequest('/attendance/devices', data),

  /**
   * 删除设备
   * @param {Number} deviceId 设备ID
   */
  deleteDevice: (deviceId) =>
    deleteRequest(`/attendance/devices/${deviceId}`),

  /**
   * 批量删除设备
   * @param {Array} deviceIds 设备ID列表
   */
  batchDeleteDevices: (deviceIds) =>
    postRequest('/attendance/devices/batch-delete', { deviceIds }),

  /**
   * 启用/禁用设备
   * @param {Number} deviceId 设备ID
   * @param {Boolean} status 状态
   */
  toggleDeviceStatus: (deviceId, status) =>
    putRequest(`/attendance/devices/${deviceId}/status`, { status }),

  // ===== 设备配置管理 =====

  /**
   * 获取设备配置
   * @param {Number} deviceId 设备ID
   */
  getDeviceConfig: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}/config`),

  /**
   * 保存设备配置
   * @param {Number} deviceId 设备ID
   * @param {Object} config 配置数据
   * @param {Array} config.punchModes 支持的打卡方式
   * @param {Boolean} config.gpsEnabled 是否启用GPS验证
   * @param {Boolean} config.photoEnabled 是否启用拍照验证
   * @param {Boolean} config.faceRecognitionEnabled 是否启用人脸识别
   * @param {Boolean} config.cardEnabled 是否启用IC卡验证
   * @param {Boolean} config.passwordEnabled 是否启用密码验证
   * @param {Object} config.workTimeConfig 工作时间配置
   */
  saveDeviceConfig: (deviceId, config) =>
    postRequest(`/attendance/devices/${deviceId}/config`, config),

  /**
   * 批量配置设备
   * @param {Object} data 批量配置数据
   * @param {Array} data.deviceIds 设备ID列表
   * @param {Object} data.config 配置数据
   */
  batchConfigureDevices: (data) =>
    postRequest('/attendance/devices/batch-config', data),

  /**
   * 重置设备配置
   * @param {Number} deviceId 设备ID
   */
  resetDeviceConfig: (deviceId) =>
    postRequest(`/attendance/devices/${deviceId}/reset-config`),

  // ===== 设备连接管理 =====

  /**
   * 测试设备连接
   * @param {Object} data 测试数据
   * @param {Number} data.deviceId 设备ID
   */
  testDeviceConnection: (data) =>
    postRequest('/attendance/devices/test-connection', data),

  /**
   * 获取设备连接状态
   * @param {Number} deviceId 设备ID
   */
  getDeviceConnectionStatus: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}/connection-status`),

  /**
   * 重启设备
   * @param {Object} data 重启数据
   * @param {Number} data.deviceId 设备ID
   */
  restartDevice: (data) =>
    postRequest('/attendance/devices/restart', data),

  /**
   * 批量重启设备
   * @param {Array} deviceIds 设备ID列表
   */
  batchRestartDevices: (deviceIds) =>
    postRequest('/attendance/devices/batch-restart', { deviceIds }),

  // ===== 设备状态监控 =====

  /**
   * 获取设备实时状态
   * @param {Number} deviceId 设备ID
   */
  getDeviceRealtimeStatus: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}/realtime-status`),

  /**
   * 获取设备运行统计
   * @param {Object} params 统计参数
   * @param {Number} params.deviceId 设备ID(可选)
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {String} params.statisticsType 统计类型:daily/weekly/monthly
   */
  getDeviceStatistics: (params) =>
    getRequest('/attendance/devices/statistics', params),

  /**
   * 获取设备健康状态
   * @param {Number} deviceId 设备ID
   */
  getDeviceHealthStatus: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}/health-status`),

  /**
   * 获取设备使用率统计
   * @param {Object} params 统计参数
   */
  getDeviceUsageStatistics: (params) =>
    getRequest('/attendance/devices/usage-statistics', params),

  /**
   * 获取设备异常记录
   * @param {Object} params 查询参数
   */
  getDeviceErrorRecords: (params) =>
    getRequest('/attendance/devices/error-records', params),

  // ===== 设备生物特征管理 =====

  /**
   * 获取设备支持的生物特征类型
   * @param {Number} deviceId 设备ID
   */
  getDeviceSupportedBiometrics: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}/supported-biometrics`),

  /**
   * 同步生物特征到设备
   * @param {Object} data 同步数据
   * @param {Number} data.deviceId 设备ID
   * @param {Array} data.employeeIds 员工ID列表(可选，不传则同步所有)
   * @param {Array} data.biometricTypes 生物特征类型列表
   */
  syncBiometricsToDevice: (data) =>
    postRequest('/attendance/devices/sync-biometrics', data),

  /**
   * 从设备删除生物特征
   * @param {Object} data 删除数据
   * @param {Number} data.deviceId 设备ID
   * @param {Array} data.employeeIds 员工ID列表
   */
  removeBiometricsFromDevice: (data) =>
    postRequest('/attendance/devices/remove-biometrics', data),

  /**
   * 获取设备生物特征同步状态
   * @param {Number} deviceId 设备ID
   */
  getDeviceBiometricSyncStatus: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}/biometric-sync-status`),

  // ===== 设备固件管理 =====

  /**
   * 获取设备固件版本
   * @param {Number} deviceId 设备ID
   */
  getDeviceFirmwareVersion: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}/firmware-version`),

  /**
   * 检查固件更新
   * @param {Number} deviceId 设备ID
   */
  checkFirmwareUpdate: (deviceId) =>
    postRequest(`/attendance/devices/${deviceId}/check-firmware-update`),

  /**
   * 更新设备固件
   * @param {Object} data 更新数据
   * @param {Number} data.deviceId 设备ID
   * @param {String} data.firmwareUrl 固件文件URL
   */
  updateDeviceFirmware: (data) =>
    postRequest('/attendance/devices/update-firmware', data),

  /**
   * 获取固件更新历史
   * @param {Number} deviceId 设备ID
   */
  getFirmwareUpdateHistory: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}/firmware-update-history`),

  // ===== 设备日志管理 =====

  /**
   * 获取设备操作日志
   * @param {Object} params 查询参数
   * @param {Number} params.deviceId 设备ID(可选)
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {String} params.logType 日志类型
   */
  getDeviceOperationLogs: (params) =>
    getRequest('/attendance/devices/operation-logs', params),

  /**
   * 获取设备异常日志
   * @param {Object} params 查询参数
   */
  getDeviceErrorLogs: (params) =>
    getRequest('/attendance/devices/error-logs', params),

  /**
   * 导出设备日志
   * @param {Object} params 导出参数
   */
  exportDeviceLogs: (params) =>
    getRequest('/attendance/devices/export-logs', params, { responseType: 'blob' }),

  // ===== 设备分组管理 =====

  /**
   * 获取设备分组列表
   * @param {Object} params 查询参数
   */
  getDeviceGroups: (params) =>
    getRequest('/attendance/devices/groups', params),

  /**
   * 创建设备分组
   * @param {Object} data 分组数据
   */
  createDeviceGroup: (data) =>
    postRequest('/attendance/devices/groups', data),

  /**
   * 更新设备分组
   * @param {Object} data 分组数据
   */
  updateDeviceGroup: (data) =>
    putRequest('/attendance/devices/groups', data),

  /**
   * 删除设备分组
   * @param {Number} groupId 分组ID
   */
  deleteDeviceGroup: (groupId) =>
    deleteRequest(`/attendance/devices/groups/${groupId}`),

  /**
   * 分配设备到分组
   * @param {Object} data 分配数据
   */
  assignDevicesToGroup: (data) =>
    postRequest('/attendance/devices/groups/assign-devices', data),

  // ===== 设备权限管理 =====

  /**
   * 获取设备权限配置
   * @param {Number} deviceId 设备ID
   */
  getDevicePermissions: (deviceId) =>
    getRequest(`/attendance/devices/${deviceId}/permissions`),

  /**
   * 保存设备权限配置
   * @param {Number} deviceId 设备ID
   * @param {Object} permissions 权限配置
   */
  saveDevicePermissions: (deviceId, permissions) =>
    postRequest(`/attendance/devices/${deviceId}/permissions`, permissions),

  /**
   * 获取员工设备访问权限
   * @param {Object} params 查询参数
   */
  getEmployeeDevicePermissions: (params) =>
    getRequest('/attendance/devices/employee-permissions', params),

  /**
   * 授予员工设备访问权限
   * @param {Object} data 授权数据
   */
  grantEmployeeDevicePermission: (data) =>
    postRequest('/attendance/devices/grant-employee-permission', data),

  /**
   * 撤销员工设备访问权限
   * @param {Object} data 撤销数据
   */
  revokeEmployeeDevicePermission: (data) =>
    postRequest('/attendance/devices/revoke-employee-permission', data),

  // ===== 设备导入导出 =====

  /**
   * 导入设备数据
   * @param {FormData} formData 包含Excel文件的FormData
   */
  importDevices: (formData) =>
    postRequest('/attendance/devices/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),

  /**
   * 导出设备数据
   * @param {Object} params 导出参数
   * @param {Array} params.deviceIds 设备ID列表(可选)
   * @param {String} params.deviceType 设备类型(可选)
   * @param {Number} params.areaId 区域ID(可选)
   */
  exportDevices: (params) =>
    getRequest('/attendance/devices/export', params, { responseType: 'blob' }),

  /**
   * 下载设备导入模板
   */
  downloadDeviceTemplate: () =>
    getRequest('/attendance/devices/import-template', {}, { responseType: 'blob' }),

  /**
   * 验证导入数据
   * @param {FormData} formData 包含Excel文件的FormData
   */
  validateDeviceImportData: (formData) =>
    postRequest('/attendance/devices/validate-import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),

  // ===== 设备配置模板 =====

  /**
   * 获取设备配置模板列表
   */
  getDeviceConfigTemplates: () =>
    getRequest('/attendance/devices/config-templates'),

  /**
   * 创建设备配置模板
   * @param {Object} data 模板数据
   */
  createDeviceConfigTemplate: (data) =>
    postRequest('/attendance/devices/config-templates', data),

  /**
   * 应用配置模板到设备
   * @param {Object} data 应用数据
   * @param {Number} data.templateId 模板ID
   * @param {Array} data.deviceIds 设备ID列表
   */
  applyConfigTemplateToDevices: (data) =>
    postRequest('/attendance/devices/apply-config-template', data),

  /**
   * 从设备创建配置模板
   * @param {Object} data 创建数据
   * @param {Number} data.deviceId 设备ID
   * @param {String} data.templateName 模板名称
   * @param {String} data.templateDescription 模板描述
   */
  createConfigTemplateFromDevice: (data) =>
    postRequest('/attendance/devices/create-config-template', data),

  // ===== 设备维护管理 =====

  /**
   * 获取设备维护记录
   * @param {Object} params 查询参数
   */
  getDeviceMaintenanceRecords: (params) =>
    getRequest('/attendance/devices/maintenance-records', params),

  /**
   * 创建设备维护记录
   * @param {Object} data 维护数据
   */
  createDeviceMaintenanceRecord: (data) =>
    postRequest('/attendance/devices/maintenance-records', data),

  /**
   * 获取设备维护计划
   * @param {Object} params 查询参数
   */
  getDeviceMaintenancePlans: (params) =>
    getRequest('/attendance/devices/maintenance-plans', params),

  /**
   * 创建设备维护计划
   * @param {Object} data 计划数据
   */
  createDeviceMaintenancePlan: (data) =>
    postRequest('/attendance/devices/maintenance-plans', data),

  // ===== 辅助功能 =====

  /**
   * 获取区域选项（用于设备分配）
   */
  getAreaTreeOptions: () =>
    getRequest('/attendance/devices/area-tree-options'),

  /**
   * 获取设备类型选项
   */
  getDeviceTypeOptions: () =>
    getRequest('/attendance/devices/device-type-options'),

  /**
   * 验证设备编码唯一性
   * @param {Object} data 验证数据
   * @param {String} data.deviceCode 设备编码
   * @param {Number} data.excludeDeviceId 排除的设备ID(编辑时使用)
   */
  validateDeviceCodeUnique: (data) =>
    postRequest('/attendance/devices/validate-code', data),

  /**
   * 获取设备在线统计
   */
  getDeviceOnlineStatistics: () =>
    getRequest('/attendance/devices/online-statistics'),

  /**
   * 获取设备分布统计
   * @param {Object} params 统计参数
   */
  getDeviceDistributionStatistics: (params) =>
    getRequest('/attendance/devices/distribution-statistics', params),
};