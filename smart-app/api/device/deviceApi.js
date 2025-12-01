import { request } from '@/utils/request'

/**
 * 设备管理API接口
 */
export const deviceApi = {
  /**
   * 分页查询设备列表
   * @param {Object} params - 查询参数
   * @returns {Promise<Object>}
   */
  queryPage: (params) => {
    return request.post('/api/smart/device/page', params)
  },

  /**
   * 获取设备详情
   * @param {Number} deviceId - 设备ID
   * @returns {Promise<Object>}
   */
  getDetail: (deviceId) => {
    return request.get(`/api/smart/device/detail/${deviceId}`)
  },

  /**
   * 新增设备
   * @param {Object} data - 设备数据
   * @returns {Promise<Object>}
   */
  add: (data) => {
    return request.post('/api/smart/device/add', data)
  },

  /**
   * 更新设备
   * @param {Object} data - 设备数据
   * @returns {Promise<Object>}
   */
  update: (data) => {
    return request.post('/api/smart/device/update', data)
  },

  /**
   * 删除设备
   * @param {Number} deviceId - 设备ID
   * @returns {Promise<Object>}
   */
  delete: (deviceId) => {
    return request.post('/api/smart/device/delete', deviceId)
  },

  /**
   * 批量删除设备
   * @param {Array} deviceIds - 设备ID列表
   * @returns {Promise<Object>}
   */
  batchDelete: (deviceIds) => {
    return request.post('/api/smart/device/batchDelete', deviceIds)
  },

  /**
   * 启用设备
   * @param {Number} deviceId - 设备ID
   * @returns {Promise<Object>}
   */
  enable: (deviceId) => {
    return request.post(`/api/smart/device/enable/${deviceId}`)
  },

  /**
   * 禁用设备
   * @param {Number} deviceId - 设备ID
   * @returns {Promise<Object>}
   */
  disable: (deviceId) => {
    return request.post(`/api/smart/device/disable/${deviceId}`)
  },

  /**
   * 设备上线
   * @param {Number} deviceId - 设备ID
   * @returns {Promise<Object>}
   */
  online: (deviceId) => {
    return request.post(`/api/smart/device/online/${deviceId}`)
  },

  /**
   * 设备离线
   * @param {Number} deviceId - 设备ID
   * @returns {Promise<Object>}
   */
  offline: (deviceId) => {
    return request.post(`/api/smart/device/offline/${deviceId}`)
  },

  /**
   * 获取设备统计数据
   * @returns {Promise<Object>}
   */
  getStatistics: () => {
    return request.get('/api/smart/device/statistics')
  },

  /**
   * 刷新设备状态
   * @returns {Promise<Object>}
   */
  refreshStatus: () => {
    return request.post('/api/smart/device/refreshStatus')
  },

  /**
   * 根据类型获取设备列表
   * @param {String} deviceType - 设备类型
   * @returns {Promise<Object>}
   */
  getDevicesByType: (deviceType) => {
    return request.get(`/api/smart/device/type/${deviceType}`)
  },

  /**
   * 根据区域获取设备列表
   * @param {Number} areaId - 区域ID
   * @returns {Promise<Object>}
   */
  getDevicesByArea: (areaId) => {
    return request.get(`/api/smart/device/area/${areaId}`)
  }
}