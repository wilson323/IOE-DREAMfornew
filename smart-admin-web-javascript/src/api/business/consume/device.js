import { SmartRequest } from '@/lib/smart-request'

const baseUrl = '/api/consume/device'

export const deviceApi = {
  /**
   * 获取设备列表
   */
  getDeviceList(params) {
    return SmartRequest.get(`${baseUrl}/list`, { params })
  },

  /**
   * 获取设备详情
   */
  getDeviceDetail(deviceId) {
    return SmartRequest.get(`${baseUrl}/detail/${deviceId}`)
  },

  /**
   * 添加设备
   */
  addDevice(data) {
    return SmartRequest.post(baseUrl, data)
  },

  /**
   * 更新设备
   */
  updateDevice(deviceId, data) {
    return SmartRequest.put(`${baseUrl}/${deviceId}`, data)
  },

  /**
   * 删除设备
   */
  deleteDevice(deviceId) {
    return SmartRequest.delete(`${baseUrl}/${deviceId}`)
  },

  /**
   * 获取区域树
   */
  getRegionTree() {
    return SmartRequest.get('/api/consume/region/tree')
  },

  /**
   * 重启设备
   */
  restartDevice(deviceId) {
    return SmartRequest.post(`${deviceId}/restart`)
  },

  /**
   * 设置维护模式
   */
  setMaintenanceMode(deviceId) {
    return SmartRequest.post(`${deviceId}/maintenance`)
  },

  /**
   * 启用设备
   */
  enableDevice(deviceId) {
    return SmartRequest.post(`${deviceId}/enable`)
  },

  /**
   * 禁用设备
   */
  disableDevice(deviceId) {
    return SmartRequest.post(`${deviceId}/disable`)
  },

  /**
   * 获取设备状态
   */
  getDeviceStatus(deviceId) {
    return SmartRequest.get(`${deviceId}/status`)
  },

  /**
   * 获取设备配置
   */
  getDeviceConfig(deviceId) {
    return SmartRequest.get(`${deviceId}/config`)
  },

  /**
   * 更新设备配置
   */
  updateDeviceConfig(deviceId, config) {
    return SmartRequest.put(`${deviceId}/config`, config)
  },

  /**
   * 远程控制设备
   */
  remoteControl(deviceId, command) {
    return SmartRequest.post(`${deviceId}/remote-control`, { command })
  },

  /**
   * 获取设备统计
   */
  getDeviceStatistics(params) {
    return SmartRequest.get(`${baseUrl}/statistics`, { params })
  },

  /**
   * 获取设备日志
   */
  getDeviceLogs(deviceId, params) {
    return SmartRequest.get(`${deviceId}/logs`, { params })
  },

  /**
   * 导出设备数据
   */
  exportDeviceData(params) {
    return SmartRequest.download(`${baseUrl}/export`, { params })
  },

  /**
   * 批量操作设备
   */
  batchOperateDevices(data) {
    return SmartRequest.post(`${baseUrl}/batch-operate`, data)
  },

  /**
   * 获取设备类型列表
   */
  getDeviceTypes() {
    return SmartRequest.get(`${baseUrl}/types`)
  },

  /**
   * 获取设备位置列表
   */
  getDeviceLocations() {
    return SmartRequest.get(`${baseUrl}/locations`)
  },

  /**
   * 同步设备时间
   */
  syncDeviceTime(deviceId) {
    return SmartRequest.post(`${deviceId}/sync-time`)
  },

  /**
   * 获取设备交易记录
   */
  getDeviceTransactions(deviceId, params) {
    return SmartRequest.get(`${deviceId}/transactions`, { params })
  },

  /**
   * 上传设备固件
   */
  uploadDeviceFirmware(deviceId, formData) {
    return SmartRequest.upload(`${deviceId}/firmware`, formData)
  },

  /**
   * 获取设备版本信息
   */
  getDeviceVersion(deviceId) {
    return SmartRequest.get(`${deviceId}/version`)
  },

  /**
   * 获取在线设备列表
   */
  getOnlineDevices() {
    return SmartRequest.get(`${baseUrl}/online`)
  }
}

export default deviceApi