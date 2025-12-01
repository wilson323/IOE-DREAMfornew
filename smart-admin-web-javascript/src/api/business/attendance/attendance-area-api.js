/**
 * 考勤区域管理API接口
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-24
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { postRequest, getRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const attendanceAreaApi = {
  // ===== 区域基础管理 =====

  /**
   * 获取区域树形结构
   * @param {Object} params 查询参数
   * @param {Boolean} params.includeDisabled 是否包含禁用区域
   * @param {String} params.areaType 区域类型(可选)
   */
  getAreaTree: (params) =>
    getRequest('/attendance/areas/tree', params),

  /**
   * 获取区域列表
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.areaName 区域名称(可选)
   * @param {String} params.areaCode 区域编码(可选)
   * @param {String} params.areaType 区域类型(可选)
   * @param {Number} params.parentId 父区域ID(可选)
   * @param {Number} params.status 状态(可选)
   */
  getAreaList: (params) =>
    getRequest('/attendance/areas', params),

  /**
   * 获取区域详情
   * @param {Number} areaId 区域ID
   */
  getAreaDetail: (areaId) =>
    getRequest(`/attendance/areas/${areaId}`),

  /**
   * 新增区域
   * @param {Object} data 区域数据
   * @param {String} data.areaName 区域名称
   * @param {String} data.areaCode 区域编码
   * @param {String} data.areaType 区域类型
   * @param {Number} data.parentId 父区域ID(可选)
   * @param {Number} data.sortOrder 排序号
   * @param {Number} data.status 状态
   * @param {String} data.description 描述(可选)
   * @param {Number} data.longitude 经度(可选)
   * @param {Number} data.latitude 纬度(可选)
   * @param {Number} data.areaSize 区域大小(可选)
   */
  createArea: (data) =>
    postRequest('/attendance/areas', data),

  /**
   * 更新区域
   * @param {Object} data 区域数据
   */
  updateArea: (data) =>
    putRequest('/attendance/areas', data),

  /**
   * 删除区域
   * @param {Number} areaId 区域ID
   */
  deleteArea: (areaId) =>
    deleteRequest(`/attendance/areas/${areaId}`),

  /**
   * 批量删除区域
   * @param {Array} areaIds 区域ID列表
   */
  batchDeleteAreas: (areaIds) =>
    postRequest('/attendance/areas/batch-delete', { areaIds }),

  /**
   * 启用/禁用区域
   * @param {Number} areaId 区域ID
   * @param {Boolean} status 状态
   */
  toggleAreaStatus: (areaId, status) =>
    putRequest(`/attendance/areas/${areaId}/status`, { status }),

  // ===== 区域考勤配置 =====

  /**
   * 获取区域考勤配置
   * @param {Number} areaId 区域ID
   */
  getAreaAttendanceConfig: (areaId) =>
    getRequest(`/attendance/areas/${areaId}/attendance-config`),

  /**
   * 保存区域考勤配置
   * @param {Number} areaId 区域ID
   * @param {Object} config 配置数据
   * @param {Boolean} config.gpsRequired 是否需要GPS验证
   * @param {Boolean} config.photoRequired 是否需要照片验证
   * @param {Number} config.gpsRange GPS验证范围(米)
   * @param {Number} config.workdayPunchRequired 工作日打卡次数
   * @param {Number} config.weekendPunchRequired 休息日打卡次数
   */
  saveAreaAttendanceConfig: (areaId, config) =>
    postRequest(`/attendance/areas/${areaId}/attendance-config`, config),

  /**
   * 批量设置区域考勤配置
   * @param {Object} data 批量配置数据
   * @param {Array} data.areaIds 区域ID列表
   * @param {Object} data.config 配置数据
   */
  batchSaveAttendanceConfig: (data) =>
    postRequest('/attendance/areas/batch-attendance-config', data),

  // ===== 地理围栏管理 =====

  /**
   * 获取区域地理围栏
   * @param {Number} areaId 区域ID
   */
  getAreaGeofence: (areaId) =>
    getRequest(`/attendance/areas/${areaId}/geofence`),

  /**
   * 保存区域地理围栏
   * @param {Number} areaId 区域ID
   * @param {Object} geofence 地理围栏数据
   * @param {Boolean} geofence.enabled 是否启用
   * @param {Array} geofence.points 围栏顶点
   */
  saveAreaGeofence: (areaId, geofence) =>
    postRequest(`/attendance/areas/${areaId}/geofence`, geofence),

  /**
   * 删除区域地理围栏
   * @param {Number} areaId 区域ID
   */
  deleteAreaGeofence: (areaId) =>
    deleteRequest(`/attendance/areas/${areaId}/geofence`),

  /**
   * 验证坐标是否在地理围栏内
   * @param {Object} data 验证数据
   * @param {Number} data.areaId 区域ID
   * @param {Number} data.longitude 经度
   * @param {Number} data.latitude 纬度
   */
  validateLocationInGeofence: (data) =>
    postRequest('/attendance/areas/validate-geofence', data),

  // ===== 设备限制管理 =====

  /**
   * 获取区域允许的设备列表
   * @param {Number} areaId 区域ID
   */
  getAreaAllowedDevices: (areaId) =>
    getRequest(`/attendance/areas/${areaId}/allowed-devices`),

  /**
   * 保存区域允许的设备
   * @param {Number} areaId 区域ID
   * @param {Array} deviceIds 设备ID列表
   */
  saveAreaAllowedDevices: (areaId, deviceIds) =>
    postRequest(`/attendance/areas/${areaId}/allowed-devices`, { deviceIds }),

  /**
   * 获取区域可用设备选项
   * @param {Object} params 查询参数
   */
  getAreaDeviceOptions: (params) =>
    getRequest('/attendance/areas/device-options', params),

  // ===== 时间限制管理 =====

  /**
   * 获取区域时间限制
   * @param {Number} areaId 区域ID
   */
  getAreaTimeLimits: (areaId) =>
    getRequest(`/attendance/areas/${areaId}/time-limits`),

  /**
   * 保存区域时间限制
   * @param {Number} areaId 区域ID
   * @param {Object} timeLimits 时间限制数据
   * @param {Boolean} timeLimits.enabled 是否启用
   * @param {Array} timeRanges 有效时间段
   */
  saveAreaTimeLimits: (areaId, timeLimits) =>
    postRequest(`/attendance/areas/${areaId}/time-limits`, timeLimits),

  /**
   * 验证时间是否在允许范围内
   * @param {Object} data 验证数据
   * @param {Number} data.areaId 区域ID
   * @param {String} data.punchTime 打卡时间
   */
  validatePunchTimeInRange: (data) =>
    postRequest('/attendance/areas/validate-time-range', data),

  // ===== 扩展配置管理 =====

  /**
   * 获取区域扩展配置
   * @param {Number} areaId 区域ID
   */
  getAreaExtensionConfig: (areaId) =>
    getRequest(`/attendance/areas/${areaId}/extension-config`),

  /**
   * 保存区域扩展配置
   * @param {Number} areaId 区域ID
   * @param {Object} config 扩展配置数据
   */
  saveAreaExtensionConfig: (areaId, config) =>
    postRequest(`/attendance/areas/${areaId}/extension-config`, config),

  // ===== 区域人员管理 =====

  /**
   * 获取区域人员列表
   * @param {Object} params 查询参数
   * @param {Number} params.areaId 区域ID
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @param {String} params.employeeName 员工姓名(可选)
   */
  getAreaEmployees: (params) =>
    getRequest('/attendance/areas/employees', params),

  /**
   * 分配员工到区域
   * @param {Object} data 分配数据
   * @param {Number} data.areaId 区域ID
   * @param {Array} data.employeeIds 员工ID列表
   */
  assignEmployeesToArea: (data) =>
    postRequest('/attendance/areas/assign-employees', data),

  /**
   * 从区域移除员工
   * @param {Object} data 移除数据
   * @param {Number} data.areaId 区域ID
   * @param {Array} data.employeeIds 员工ID列表
   */
  removeEmployeesFromArea: (data) =>
    postRequest('/attendance/areas/remove-employees', data),

  // ===== 区域统计分析 =====

  /**
   * 获取区域打卡统计
   * @param {Object} params 统计参数
   * @param {Number} params.areaId 区域ID
   * @param {String} params.startDate 开始日期
   * @param {String} params.endDate 结束日期
   * @param {String} params.statisticsType 统计类型:daily/weekly/monthly
   */
  getAreaPunchStatistics: (params) =>
    getRequest('/attendance/areas/punch-statistics', params),

  /**
   * 获取区域异常统计
   * @param {Object} params 统计参数
   */
  getAreaAbnormalStatistics: (params) =>
    getRequest('/attendance/areas/abnormal-statistics', params),

  /**
   * 获取区域设备使用统计
   * @param {Object} params 统计参数
   */
  getAreaDeviceUsageStatistics: (params) =>
    getRequest('/attendance/areas/device-usage-statistics', params),

  // ===== 区域导入导出 =====

  /**
   * 导入区域数据
   * @param {FormData} formData 包含Excel文件的FormData
   */
  importAreas: (formData) =>
    postRequest('/attendance/areas/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),

  /**
   * 导出区域数据
   * @param {Object} params 导出参数
   * @param {Array} params.areaIds 区域ID列表(可选)
   * @param {String} params.areaType 区域类型(可选)
   */
  exportAreas: (params) =>
    getRequest('/attendance/areas/export', params, { responseType: 'blob' }),

  /**
   * 下载区域导入模板
   */
  downloadAreaTemplate: () =>
    getRequest('/attendance/areas/import-template', {}, { responseType: 'blob' }),

  /**
   * 验证导入数据
   * @param {FormData} formData 包含Excel文件的FormData
   */
  validateImportData: (formData) =>
    postRequest('/attendance/areas/validate-import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),

  // ===== 区域复制 =====

  /**
   * 复制区域配置
   * @param {Object} data 复制数据
   * @param {Number} data.sourceAreaId 源区域ID
   * @param {String} data.targetAreaName 目标区域名称
   * @param {String} data.targetAreaCode 目标区域编码
   * @param {Number} data.targetParentId 目标父区域ID(可选)
   * @param {Array} data.copyElements 复制元素: ['attendanceConfig', 'geofence', 'deviceLimits', 'timeLimits']
   */
  copyAreaConfig: (data) =>
    postRequest('/attendance/areas/copy-config', data),

  /**
   * 批量复制区域配置
   * @param {Object} data 批量复制数据
   * @param {Number} data.sourceAreaId 源区域ID
   * @param {Array} data.targetAreas 目标区域列表
   * @param {Array} data.copyElements 复制元素
   */
  batchCopyAreaConfig: (data) =>
    postRequest('/attendance/areas/batch-copy-config', data),

  // ===== 区域验证 =====

  /**
   * 验证区域编码是否唯一
   * @param {Object} data 验证数据
   * @param {String} data.areaCode 区域编码
   * @param {Number} data.excludeAreaId 排除的区域ID(编辑时使用)
   */
  validateAreaCodeUnique: (data) =>
    postRequest('/attendance/areas/validate-code', data),

  /**
   * 验证区域配置完整性
   * @param {Number} areaId 区域ID
   */
  validateAreaConfigIntegrity: (areaId) =>
    getRequest(`/attendance/areas/${areaId}/validate-config`),

  /**
   * 获取区域配置检查报告
   * @param {Number} areaId 区域ID
   */
  getAreaConfigCheckReport: (areaId) =>
    getRequest(`/attendance/areas/${areaId}/config-check-report`),

  // ===== 区域缓存管理 =====

  /**
   * 清除区域缓存
   * @param {Number} areaId 区域ID(可选，不传则清除所有)
   */
  clearAreaCache: (areaId) =>
    postRequest('/attendance/areas/clear-cache', { areaId }),

  /**
   * 预热区域缓存
   * @param {Number} areaId 区域ID(可选，不传则预热所有)
   */
  warmUpAreaCache: (areaId) =>
    postRequest('/attendance/areas/warm-up-cache', { areaId }),

  /**
   * 获取区域缓存状态
   * @param {Number} areaId 区域ID(可选)
   */
  getAreaCacheStatus: (areaId) =>
    getRequest('/attendance/areas/cache-status', { areaId }),
};