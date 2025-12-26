/*
 * 门禁管理 API
 * 
 * 根据API契约文档和已创建的Controller实现
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest, putRequest, deleteRequest, request } from '/@/lib/axios';

const postWithParams = (url, params, data) => {
  return request({ url, method: 'post', params, data });
};

export const accessApi = {
  // ==================== 门禁记录管理 ====================

  /**
   * 分页查询门禁记录
   */
  queryAccessRecords: (params) => {
    return postRequest('/api/v1/access/record/query', params);
  },

  /**
   * 获取门禁记录统计
   */
  getAccessRecordStatistics: (params) => {
    return getRequest('/api/v1/access/record/statistics', params);
  },

  // ==================== 设备管理 ====================

  /**
   * 分页查询设备
   */
  queryDevices: (params) => {
    return postRequest('/api/v1/access/device/query', params);
  },

  /**
   * 查询设备详情
   */
  getDeviceDetail: (deviceId) => {
    return getRequest(`/api/v1/access/device/${deviceId}`);
  },

  /**
   * 添加设备
   */
  addDevice: (data) => {
    return postRequest('/api/v1/access/device/add', data);
  },

  /**
   * 更新设备
   */
  updateDevice: (data) => {
    return putRequest('/api/v1/access/device/update', data);
  },

  /**
   * 删除设备
   */
  deleteDevice: (deviceId) => {
    return deleteRequest(`/api/v1/access/device/${deviceId}`);
  },

  /**
   * 更新设备状态
   */
  updateDeviceStatus: (params) => {
    return postWithParams('/api/v1/access/device/status/update', params);
  },

  // ==================== 区域设备关联（统一能力） ====================

  /**
   * 获取区域设备列表
   */
  getAreaDevices: (areaId) => {
    return getRequest(`/api/v1/area-device/area/${areaId}/devices`);
  },

  /**
   * 从区域移除设备
   */
  removeAreaDevice: (areaId, deviceId) => {
    return deleteRequest('/api/v1/area-device/remove', { areaId, deviceId });
  },

  // ==================== 区域权限（通用能力） ====================

  /**
   * 获取区域权限列表（当前支持：user）
   */
  getAreaPermissions: (areaId, type) => {
    return getRequest(`/api/v1/area-permission/area/${areaId}`, { type });
  },

  /**
   * 删除单条区域权限
   */
  deleteAreaPermission: (permissionId) => {
    return deleteRequest(`/api/v1/area-permission/${permissionId}`);
  },

  /**
   * 批量删除区域权限
   */
  batchDeleteAreaPermissions: (permissionIds) => {
    return postRequest('/api/v1/area-permission/batch-delete', permissionIds);
  },

  // ==================== 移动端验证功能 ====================

  /**
   * 二维码验证
   * @param {Object} data 验证数据
   * @param {String} data.qrCode 二维码
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyQRCode: (data) => {
    return postRequest('/api/v1/mobile/access/qr/verify', data);
  },

  /**
   * NFC验证
   * @param {Object} data 验证数据
   * @param {String} data.nfcCardId NFC卡ID
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyNFC: (data) => {
    return postRequest('/api/v1/mobile/access/nfc/verify', data);
  },

  /**
   * 生物识别验证
   * @param {Object} data 验证数据
   * @param {Number} data.userId 用户ID
   * @param {String} data.biometricType 生物识别类型
   * @param {String} data.biometricData 生物识别数据
   * @param {Number} data.deviceId 设备ID
   * @returns {Promise}
   */
  verifyBiometric: (data) => {
    return postRequest('/api/v1/mobile/access/biometric/verify', data);
  },

  /**
   * 获取附近设备
   * @param {Number} userId 用户ID
   * @param {Number} latitude 纬度
   * @param {Number} longitude 经度
   * @param {Number} radius 半径（米，默认500）
   * @returns {Promise}
   */
  getNearbyDevices: (userId, latitude, longitude, radius = 500) => {
    return getRequest('/api/v1/mobile/access/devices/nearby', {
      userId, latitude, longitude, radius
    });
  },

  /**
   * 获取实时门禁状态
   * @param {Number} deviceId 设备ID
   * @returns {Promise}
   */
  getRealTimeStatus: (deviceId) => {
    return getRequest('/api/v1/mobile/access/status/realtime', { deviceId });
  },

  /**
   * 获取用户门禁权限
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserPermissions: (userId) => {
    return getRequest(`/api/v1/mobile/access/permissions/${userId}`);
  },

  /**
   * 获取用户访问记录
   * @param {Number} userId 用户ID
   * @param {Number} size 记录数量（默认20）
   * @returns {Promise}
   */
  getUserAccessRecords: (userId, size = 20) => {
    return getRequest(`/api/v1/mobile/access/records/${userId}`, { size });
  },

  /**
   * 临时开门申请
   * @param {Object} data 申请数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.deviceId 设备ID
   * @param {String} data.reason 申请原因
   * @returns {Promise}
   */
  requestTemporaryAccess: (data) => {
    return postRequest('/api/v1/mobile/access/temporary-access', data);
  },

  /**
   * 发送推送通知
   * @param {Object} data 通知数据
   * @param {Number} data.userId 用户ID
   * @param {String} data.notificationType 通知类型
   * @returns {Promise}
   */
  sendPushNotification: (data) => {
    return postRequest('/api/v1/mobile/access/notification/push', data);
  },

  // ==================== 设备自动发现 ====================

  /**
   * 启动设备发现
   * @param {Object} data 发现参数
   * @param {String} data.subnet 子网地址（如 192.168.1.0/24）
   * @param {Array<String>} data.protocols 协议列表（SSDP, ONVIF, SNMP, PRIVATE）
   * @param {Number} data.timeout 超时时间（秒）
   * @returns {Promise<{scanId: String, status: String, progress: Number}>}
   */
  startDeviceDiscovery: (data) => {
    return postRequest('/api/v1/access/device-discovery/start', data);
  },

  /**
   * 获取发现进度
   * @param {String} scanId 扫描ID
   * @returns {Promise<{scanId, status, progress, totalDevices, discoveredDevices}>}
   */
  getDiscoveryProgress: (scanId) => {
    return getRequest(`/api/v1/access/device-discovery/progress/${scanId}`);
  },

  /**
   * 批量添加发现的设备
   * @param {Array<Object>} devices 设备列表
   * @returns {Promise>
   */
  batchAddDiscoveredDevices: (devices) => {
    return postRequest('/api/v1/access/device-discovery/batch-add', devices);
  },

  /**
   * 导出发现结果
   * @param {String} scanId 扫描ID
   * @returns {Promise<{fileName: String, fileData: String}>}
   */
  exportDiscoveryResult: (scanId) => {
    return getRequest(`/api/v1/access/device-discovery/export/${scanId}`);
  },

  /**
   * 取消发现任务
   * @param {String} scanId 扫描ID
   * @returns {Promise}
   */
  cancelDiscovery: (scanId) => {
    return postRequest(`/api/v1/access/device-discovery/cancel/${scanId}`);
  },

  // ==================== 设备批量导入 ====================

  /**
   * 上传Excel文件并解析
   * @param {FormData} formData 包含file和batchName
   * @returns {Promise<Number>} 返回批次ID
   */
  uploadDeviceImportFile: (formData) => {
    return postRequest('/api/v1/access/device-import/upload', formData);
  },

  /**
   * 执行导入操作
   * @param {Number} batchId 批次ID
   * @returns {Promise>
   */
  executeDeviceImport: (batchId) => {
    return postRequest(`/api/v1/access/device-import/${batchId}/execute`);
  },

  /**
   * 分页查询导入批次
   * @param {Object} params 查询参数
   * @returns {Promise<PageResult<DeviceImportBatchVO>>}
   */
  queryDeviceImportBatches: (params) => {
    return postRequest('/api/v1/access/device-import/page', params);
  },

  /**
   * 查询导入批次详情
   * @param {Number} batchId 批次ID
   * @returns {Promise<DeviceImportBatchVO>}
   */
  getDeviceImportBatchDetail: (batchId) => {
    return getRequest(`/api/v1/access/device-import/${batchId}`);
  },

  /**
   * 查询批次的错误列表
   * @param {Number} batchId 批次ID
   * @returns {Promise<Array<DeviceImportErrorVO>>}
   */
  queryDeviceImportErrors: (batchId) => {
    return getRequest(`/api/v1/access/device-import/${batchId}/errors`);
  },

  /**
   * 获取导入统计信息
   * @returns {Promise<DeviceImportStatisticsVO>}
   */
  getDeviceImportStatistics: () => {
    return getRequest('/api/v1/access/device-import/statistics');
  },

  /**
   * 删除导入批次
   * @param {Number} batchId 批次ID
   * @returns {Promise}
   */
  deleteDeviceImportBatch: (batchId) => {
    return deleteRequest(`/api/v1/access/device-import/${batchId}`);
  },

  /**
   * 下载导入模板
   * @returns {Promise<Blob>} Excel文件
   */
  downloadDeviceImportTemplate: () => {
    return getRequest('/api/v1/access/device-import/template', {}, { responseType: 'blob' });
  },

  /**
   * 导出错误记录
   * @param {Number} batchId 批次ID
   * @returns {Promise<Blob>} Excel文件
   */
  exportDeviceImportErrors: (batchId) => {
    return getRequest(`/api/v1/access/device-import/${batchId}/export-errors`, {}, { responseType: 'blob' });
  },

  // ==================== 固件管理 ====================

  /**
   * 上传固件
   * @param {FormData} formData 包含file和固件信息
   * @returns {Promise<Number>} 返回固件ID
   */
  uploadFirmware: (formData) => {
    return postRequest('/api/v1/access/firmware/upload', formData);
  },

  /**
   * 分页查询固件列表
   * @param {Object} params 查询参数
   * @returns {Promise<PageResult<FirmwareVO>>}
   */
  queryFirmwarePage: (params) => {
    return postRequest('/api/v1/access/firmware/page', params);
  },

  /**
   * 查询固件详情
   * @param {Number} firmwareId 固件ID
   * @returns {Promise<FirmwareDetailVO>}
   */
  getFirmwareDetail: (firmwareId) => {
    return getRequest(`/api/v1/access/firmware/${firmwareId}`);
  },

  /**
   * 查询可用固件列表
   * @param {Number} deviceType 设备类型（可选）
   * @param {String} deviceModel 设备型号（可选）
   * @param {String} currentVersion 当前版本（可选）
   * @returns {Promise<Array<FirmwareVO>>}
   */
  queryAvailableFirmware: (deviceType, deviceModel, currentVersion) => {
    return getRequest('/api/v1/access/firmware/available', { deviceType, deviceModel, currentVersion });
  },

  /**
   * 下载固件
   * @param {Number} firmwareId 固件ID
   * @returns {Promise<FirmwareDetailVO>}
   */
  downloadFirmware: (firmwareId) => {
    return getRequest(`/api/v1/access/firmware/${firmwareId}/download`);
  },

  /**
   * 更新固件状态
   * @param {Number} firmwareId 固件ID
   * @param {Number} firmwareStatus 状态（1-测试中 2-已发布 3-已废弃）
   * @returns {Promise}
   */
  updateFirmwareStatus: (firmwareId, firmwareStatus) => {
    return putRequest('/api/v1/access/firmware/status', { firmwareId, firmwareStatus });
  },

  /**
   * 更新固件启用状态
   * @param {Number} firmwareId 固件ID
   * @param {Number} isEnabled 是否启用（0-禁用 1-启用）
   * @returns {Promise}
   */
  updateFirmwareEnabled: (firmwareId, isEnabled) => {
    return putRequest('/api/v1/access/firmware/enabled', { firmwareId, isEnabled });
  },

  /**
   * 删除固件
   * @param {Number} firmwareId 固件ID
   * @returns {Promise}
   */
  deleteFirmware: (firmwareId) => {
    return deleteRequest(`/api/v1/access/firmware/${firmwareId}`);
  },

  /**
   * 增加固件下载次数
   * @param {Number} firmwareId 固件ID
   * @returns {Promise}
   */
  incrementFirmwareDownloadCount: (firmwareId) => {
    return postRequest(`/api/v1/access/firmware/${firmwareId}/download/increment`);
  },

  // ==================== 固件升级任务管理 ====================

  /**
   * 创建升级任务
   * @param {Object} data 任务数据
   * @returns {Promise<Number>} 返回任务ID
   */
  createUpgradeTask: (data) => {
    return postRequest('/api/v1/access/firmware-upgrade/task/create', data);
  },

  /**
   * 分页查询升级任务
   * @param {Object} params 查询参数
   * @returns {Promise<PageResult<FirmwareUpgradeTaskVO>>}
   */
  queryUpgradeTasksPage: (params) => {
    return postRequest('/api/v1/access/firmware-upgrade/task/page', params);
  },

  /**
   * 查询升级任务详情
   * @param {Number} taskId 任务ID
   * @returns {Promise<FirmwareUpgradeTaskVO>}
   */
  getUpgradeTaskDetail: (taskId) => {
    return getRequest(`/api/v1/access/firmware-upgrade/task/${taskId}`);
  },

  /**
   * 获取任务设备列表
   * @param {Number} taskId 任务ID
   * @returns {Promise<Array<FirmwareUpgradeDeviceVO>>}
   */
  getTaskDevices: (taskId) => {
    return getRequest(`/api/v1/access/firmware-upgrade/task/${taskId}/devices`);
  },

  /**
   * 获取任务进度统计
   * @param {Number} taskId 任务ID
   * @returns {Promise<Object>} 统计信息
   */
  getTaskProgress: (taskId) => {
    return getRequest(`/api/v1/access/firmware-upgrade/task/${taskId}/progress`);
  },

  /**
   * 启动升级任务
   * @param {Number} taskId 任务ID
   * @returns {Promise}
   */
  startUpgradeTask: (taskId) => {
    return postRequest(`/api/v1/access/firmware-upgrade/task/${taskId}/start`);
  },

  /**
   * 暂停升级任务
   * @param {Number} taskId 任务ID
   * @returns {Promise}
   */
  pauseUpgradeTask: (taskId) => {
    return postRequest(`/api/v1/access/firmware-upgrade/task/${taskId}/pause`);
  },

  /**
   * 恢复升级任务
   * @param {Number} taskId 任务ID
   * @returns {Promise}
   */
  resumeUpgradeTask: (taskId) => {
    return postRequest(`/api/v1/access/firmware-upgrade/task/${taskId}/resume`);
  },

  /**
   * 停止升级任务
   * @param {Number} taskId 任务ID
   * @returns {Promise}
   */
  stopUpgradeTask: (taskId) => {
    return postRequest(`/api/v1/access/firmware-upgrade/task/${taskId}/stop`);
  },

  /**
   * 删除升级任务
   * @param {Number} taskId 任务ID
   * @returns {Promise}
   */
  deleteUpgradeTask: (taskId) => {
    return deleteRequest(`/api/v1/access/firmware-upgrade/task/${taskId}`);
  },

  /**
   * 重试失败的设备
   * @param {Number} taskId 任务ID
   * @returns {Promise<Number>} 返回重试的设备数量
   */
  retryFailedDevices: (taskId) => {
    return postRequest(`/api/v1/access/firmware-upgrade/task/${taskId}/retry`);
  },

  /**
   * 回滚升级任务
   * @param {Number} taskId 任务ID
   * @returns {Promise<Number>} 返回回滚任务ID
   */
  rollbackUpgradeTask: (taskId) => {
    return postRequest(`/api/v1/access/firmware-upgrade/task/${taskId}/rollback`);
  },

  /**
   * 检查任务是否支持回滚
   * @param {Number} taskId 任务ID
   * @returns {Promise<Boolean>}
   */
  isRollbackSupported: (taskId) => {
    return getRequest(`/api/v1/access/firmware-upgrade/task/${taskId}/rollback-supported`);
  },

  // ==================== 设备查询（固件管理用）====================

  /**
   * 分页查询设备（固件管理使用）
   * @param {Object} params 查询参数
   * @returns {Promise<PageResult<DeviceVO>>}
   */
  queryDevicePage: (params) => {
    return postRequest('/api/v1/access/device/page', params);
  },

  // ==================== 反潜回管理 ====================

  /**
   * 反潜回检测
   * @param {Object} detectForm 检测请求
   * @returns {Promise<Object>} 检测结果
   */
  antiPassbackDetect: (detectForm) => {
    return postRequest('/api/v1/access/anti-passback/detect', detectForm);
  },

  /**
   * 批量反潜回检测
   * @param {Array<Object>} detectForms 检测请求列表
   * @returns {Promise<Array<Object>>} 检测结果列表
   */
  antiPassbackBatchDetect: (detectForms) => {
    return postRequest('/api/v1/access/anti-passback/batch-detect', detectForms);
  },

  /**
   * 创建反潜回规则配置
   * @param {Object} configForm 配置表单
   * @returns {Promise<Number>} 规则ID
   */
  createAntiPassbackConfig: (configForm) => {
    return postRequest('/api/v1/access/anti-passback-rules', configForm);
  },

  /**
   * 更新反潜回规则配置
   * @param {Number} ruleId 规则ID
   * @param {Object} configForm 配置表单
   * @returns {Promise}
   */
  updateAntiPassbackConfig: (ruleId, configForm) => {
    return putRequest(`/api/v1/access/anti-passback-rules/${ruleId}`, configForm);
  },

  /**
   * 删除反潜回规则配置
   * @param {Number} ruleId 规则ID
   * @returns {Promise}
   */
  deleteAntiPassbackConfig: (ruleId) => {
    return deleteRequest(`/api/v1/access/anti-passback-rules/${ruleId}`);
  },

  /**
   * 查询反潜回规则配置详情
   * @param {Number} ruleId 规则ID
   * @returns {Promise<Object>} 配置详情
   */
  getAntiPassbackConfig: (ruleId) => {
    return getRequest(`/api/v1/access/anti-passback-rules/${ruleId}`);
  },

  /**
   * 分页查询反潜回规则配置列表
   * @param {Object} params 查询参数
   * @returns {Promise<PageResult<Object>>}
   */
  queryAntiPassbackConfigPage: (params) => {
    return getRequest('/api/v1/access/anti-passback-rules', params);
  },

  /**
   * 查询反潜回规则配置列表（不分页）
   * @param {Number} areaId 区域ID（可选）
   * @returns {Promise<Array<Object>>}
   */
  queryAntiPassbackConfigList: (areaId) => {
    return getRequest('/api/v1/access/anti-passback-rules', { areaId });
  },

  /**
   * 切换反潜回规则启用状态
   * @param {Number} ruleId 规则ID
   * @param {Boolean} enabled 是否启用
   * @returns {Promise}
   */
  toggleAntiPassbackRule: (ruleId, enabled) => {
    return putRequest(`/api/v1/access/anti-passback-rules/${ruleId}/toggle`, null, {
      params: { enabled }
    });
  },

  /**
   * 启用反潜回规则配置
   * @param {Number} ruleId 规则ID
   * @returns {Promise}
   */
  enableAntiPassbackConfig: (ruleId) => {
    return putRequest(`/api/v1/access/anti-passback-rules/${ruleId}/toggle`, null, {
      params: { enabled: true }
    });
  },

  /**
   * 禁用反潜回规则配置
   * @param {Number} ruleId 规则ID
   * @returns {Promise}
   */
  disableAntiPassbackConfig: (ruleId) => {
    return putRequest(`/api/v1/access/anti-passback-rules/${ruleId}/toggle`, null, {
      params: { enabled: false }
    });
  },

  /**
   * 测试反潜回规则
   * @param {Number} ruleId 规则ID
   * @param {String} testScenario 测试场景
   * @returns {Promise<Object>} 测试结果
   */
  testAntiPassbackRule: (ruleId, testScenario) => {
    return postRequest(`/api/v1/access/anti-passback-rules/${ruleId}/test`, null, {
      params: { testScenario }
    });
  },

  /**
   * 分页查询反潜回记录
   * @param {Object} params 查询参数
   * @returns {Promise<PageResult<Object>>}
   */
  queryAntiPassbackRecordPage: (params) => {
    return postRequest('/api/v1/access/anti-passback/record/page', params);
  },

  /**
   * 处理反潜回记录
   * @param {Number} recordId 记录ID
   * @param {String} handleResult 处理结果（ALLOW/DENY）
   * @param {String} handleReason 处理原因
   * @returns {Promise}
   */
  handleAntiPassbackRecord: (recordId, handleResult, handleReason) => {
    return postRequest('/api/v1/access/anti-passback/record/handle', {
      recordId,
      handleResult,
      handleReason
    });
  },

  /**
   * 批量处理反潜回记录
   * @param {Array<Number>} recordIds 记录ID列表
   * @param {String} handleResult 处理结果
   * @param {String} handleReason 处理原因
   * @returns {Promise<Number>} 处理的记录数量
   */
  batchHandleAntiPassbackRecords: (recordIds, handleResult, handleReason) => {
    return postRequest('/api/v1/access/anti-passback/record/batch-handle', {
      recordIds,
      handleResult,
      handleReason
    });
  },

  /**
   * 获取反潜回统计信息
   * @param {Object} params 统计参数
   * @returns {Promise<Object>} 统计信息
   */
  getAntiPassbackStatistics: (params) => {
    return getRequest('/api/v1/access/anti-passback/statistics', params);
  },

  /**
   * 清空反潜回缓存
   * @param {Number} configId 配置ID（可选）
   * @returns {Promise}
   */
  clearAntiPassbackCache: (configId) => {
    return postRequest('/api/v1/access/anti-passback/cache/clear', { configId });
  },

  /**
   * 验证反潜回配置
   * @param {Object} configForm 配置表单
   * @returns {Promise<Object>} 验证结果
   */
  validateAntiPassbackConfig: (configForm) => {
    return postRequest('/api/v1/access/anti-passback/config/validate', configForm);
  }
};

