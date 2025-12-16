import SmartRequest from '/@/utils/http/axios';

export default {
  /**
   * 启动发现任务
   */
  startDiscovery(data) {
    return SmartRequest.post('/api/v1/discovery/start', data);
  },

  /**
   * 停止发现任务
   */
  stopDiscovery(taskId) {
    return SmartRequest.post(`/api/v1/discovery/stop/${taskId}`);
  },

  /**
   * 获取发现任务状态
   */
  getTaskStatus(taskId) {
    return SmartRequest.get(`/api/v1/discovery/status/${taskId}`);
  },

  /**
   * 获取发现结果
   */
  getResult(taskId) {
    return SmartRequest.get(`/api/v1/discovery/result/${taskId}`);
  },

  /**
   * 获取所有发现任务
   */
  getAllTasks() {
    return SmartRequest.get('/api/v1/discovery/tasks');
  },

  /**
   * 扫描单个设备
   */
  scanSingleDevice(ipAddress, timeout = 5000) {
    return SmartRequest.post(`/api/v1/discovery/scan/single?ipAddress=${ipAddress}&timeout=${timeout}`);
  },

  /**
   * 批量扫描设备
   */
  batchScanDevices(ipAddresses, timeout = 5000) {
    return SmartRequest.post(`/api/v1/discovery/scan/batch?timeout=${timeout}`, { ipAddresses });
  },

  /**
   * 自动注册设备
   */
  registerDevice(data, autoRegister = true) {
    return SmartRequest.post(`/api/v1/discovery/register?autoRegister=${autoRegister}`, data);
  },

  /**
   * 获取发现统计
   */
  getStatistics() {
    return SmartRequest.get('/api/v1/discovery/statistics');
  },

  /**
   * 更新协议指纹
   */
  updateProtocolFingerprints(fingerprints) {
    return SmartRequest.post('/api/v1/discovery/fingerprints', fingerprints);
  },

  /**
   * 获取支持的协议
   */
  getSupportedProtocols() {
    return SmartRequest.get('/api/v1/discovery/protocols');
  },

  /**
   * 检测设备协议
   */
  detectDeviceProtocol(ipAddress, openPorts, timeout = 5000) {
    return SmartRequest.post(`/api/v1/discovery/detect?ipAddress=${ipAddress}&timeout=${timeout}`, openPorts);
  },

  /**
   * 验证设备连接
   */
  validateDeviceConnection(deviceId) {
    return SmartRequest.get(`/api/v1/discovery/validate/${deviceId}`);
  },

  /**
   * 获取发现历史
   */
  getHistory(limit = 10) {
    return SmartRequest.get(`/api/v1/discovery/history?limit=${limit}`);
  },

  /**
   * 清理过期任务
   */
  cleanupExpiredTasks(maxAgeHours = 168) {
    return SmartRequest.post(`/api/v1/discovery/cleanup?maxAgeHours=${maxAgeHours}`);
  },

  /**
   * 导出发现结果
   */
  exportResult(taskId, format = 'json') {
    return SmartRequest.get(`/api/v1/discovery/export/${taskId}?format=${format}`);
  },

  /**
   * 快速发现设备
   */
  quickDiscover(networkRange) {
    return SmartRequest.post('/api/v1/discovery/quick-discover', { networkRange });
  },

  /**
   * 测试连接
   */
  testConnection(ipAddress) {
    return SmartRequest.get(`/api/v1/discovery/test-connection?ipAddress=${ipAddress}`);
  },

  /**
   * 获取系统状态
   */
  getSystemStatus() {
    return SmartRequest.get('/api/v1/discovery/system/status');
  },

  /**
   * 重置服务
   */
  resetService() {
    return SmartRequest.post('/api/v1/discovery/reset');
  },

  /**
   * 获取网络设备列表
   */
  getNetworkDevices() {
    return SmartRequest.get('/api/v1/discovery/network-devices');
  },

  /**
   * 添加设备到监控列表
   */
  addToMonitoring(deviceInfo) {
    return SmartRequest.post('/api/v1/discovery/monitoring/add', deviceInfo);
  },

  /**
   * 从监控列表移除设备
   */
  removeFromMonitoring(deviceId) {
    return SmartRequest.delete(`/api/v1/discovery/monitoring/${deviceId}`);
  },

  /**
   * 获取监控设备列表
   */
  getMonitoringDevices() {
    return SmartRequest.get('/api/v1/discovery/monitoring/devices');
  },

  /**
   * 设置监控设备状态
   */
  setMonitoringDeviceStatus(deviceId, status) {
    return SmartRequest.put(`/api/v1/discovery/monitoring/${deviceId}/status`, { status });
  },

  /**
   * 获取设备详细信息
   */
  getDeviceDetail(deviceId) {
    return SmartRequest.get(`/api/v1/discovery/device/${deviceId}/detail`);
  },

  /**
   * 更新设备信息
   */
  updateDeviceInfo(deviceId, deviceInfo) {
    return SmartRequest.put(`/api/v1/discovery/device/${deviceId}`, deviceInfo);
  },

  /**
   * 删除设备
   */
  deleteDevice(deviceId) {
    return SmartRequest.delete(`/api/v1/discovery/device/${deviceId}`);
  },

  /**
   * 批量导入设备
   */
  batchImportDevices(deviceList) {
    return SmartRequest.post('/api/v1/discovery/device/batch-import', { deviceList });
  },

  /**
   * 导出设备列表
   */
  exportDevices(format = 'excel') {
    return SmartRequest.get(`/api/v1/discovery/device/export?format=${format}`);
  },

  /**
   * 获取协议检测日志
   */
  getProtocolDetectionLogs(deviceId, limit = 100) {
    return SmartRequest.get(`/api/v1/discovery/device/${deviceId}/logs?limit=${limit}`);
  },

  /**
   * 重新检测设备协议
   */
  redetectDeviceProtocol(deviceId) {
    return SmartRequest.post(`/api/v1/discovery/device/${deviceId}/redetect`);
  },

  /**
   * 设置设备标签
   */
  setDeviceTags(deviceId, tags) {
    return SmartRequest.put(`/api/v1/discovery/device/${deviceId}/tags`, { tags });
  },

  /**
   * 获取设备标签
   */
  getDeviceTags(deviceId) {
    return SmartRequest.get(`/api/v1/discovery/device/${deviceId}/tags`);
  },

  /**
   * 搜索设备
   */
  searchDevices(keyword, filters = {}) {
    const params = new URLSearchParams();
    if (keyword) {
      params.append('keyword', keyword);
    }
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        params.append(key, value);
      }
    });
    return SmartRequest.get(`/api/v1/discovery/device/search?${params}`);
  },

  /**
   * 获取设备统计图表数据
   */
  getDeviceStatistics(type = 'protocol') {
    return SmartRequest.get(`/api/v1/discovery/statistics/charts?type=${type}`);
  },

  /**
   * 获取扫描进度
   */
  getScanProgress(taskId) {
    return SmartRequest.get(`/api/v1/discovery/progress/${taskId}`);
  },

  /**
   * 配置发现参数
   */
  configureDiscovery(config) {
    return SmartRequest.post('/api/v1/discovery/configure', config);
  },

  /**
   * 获取发现配置
   */
  getDiscoveryConfig() {
    return SmartRequest.get('/api/v1/discovery/configure');
  }
}