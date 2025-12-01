import { request } from '@/utils/request'

/**
 * 报表API统一错误处理
 * @param {Error} error 错误对象
 * @param {string} operation 操作描述
 * @returns {Object} 格式化的错误信息
 */
const handleReportError = (error, operation) => {
  console.error(`报表${operation}失败:`, error)

  if (error.response) {
    const { status, data } = error.response
    switch (status) {
      case 400:
        return { success: false, message: data.message || '请求参数错误', code: 'BAD_REQUEST' }
      case 401:
        return { success: false, message: '请先登录', code: 'UNAUTHORIZED' }
      case 403:
        return { success: false, message: '权限不足', code: 'FORBIDDEN' }
      case 500:
        return { success: false, message: '服务器内部错误，请稍后重试', code: 'SERVER_ERROR' }
      default:
        return { success: false, message: data.message || `报表${operation}失败`, code: 'UNKNOWN_ERROR' }
    }
  } else if (error.code === 'ECONNABORTED') {
    return { success: false, message: '请求超时，数据量可能过大', code: 'TIMEOUT' }
  } else {
    return { success: false, message: `报表${operation}失败：${error.message}`, code: 'NETWORK_ERROR' }
  }
}

/**
 * 报表管理API接口
 */
export const consumeReportApi = {
  /**
   * 获取消费汇总
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getConsumeSummary(params = {}) {
    return request({
      url: '/api/consume/report/summary',
      method: 'get',
      params
    })
  },

  /**
   * 获取消费趋势
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getConsumeTrend(params = {}) {
    return request({
      url: '/api/consume/report/trend',
      method: 'get',
      params
    })
  },

  /**
   * 获取消费模式分布
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getConsumeModeDistribution(params = {}) {
    return request({
      url: '/api/consume/report/mode-distribution',
      method: 'get',
      params
    })
  },

  /**
   * 获取设备消费排行
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getDeviceRanking(params = {}) {
    return request({
      url: '/api/consume/report/device-ranking',
      method: 'get',
      params
    })
  },

  /**
   * 获取用户消费排行
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getUserRanking(params = {}) {
    return request({
      url: '/api/consume/report/user-ranking',
      method: 'get',
      params
    })
  },

  /**
   * 获取时段分布
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getHourDistribution(params = {}) {
    return request({
      url: '/api/consume/report/hour-distribution',
      method: 'get',
      params
    })
  },

  /**
   * 获取地区分布
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getRegionDistribution(params = {}) {
    return request({
      url: '/api/consume/report/region-distribution',
      method: 'get',
      params
    })
  },

  /**
   * 获取同比环比数据
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getComparisonData(params) {
    return request({
      url: '/api/consume/report/comparison',
      method: 'get',
      params
    })
  },

  /**
   * 导出报表
   * @param {Object} params 导出参数
   * @returns {Promise} 请求结果
   */
  exportReport(params) {
    return request({
      url: '/api/consume/report/export',
      method: 'post',
      data: params,
      responseType: 'blob'
    })
  },

  /**
   * 获取仪表盘数据
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getDashboardData(params = {}) {
    return request({
      url: '/api/consume/report/dashboard',
      method: 'get',
      params
    })
  },

  /**
   * 获取实时统计
   * @returns {Promise} 请求结果
   */
  getRealTimeStatistics() {
    return request({
      url: '/api/consume/report/real-time',
      method: 'get'
    })
  },

  /**
   * 获取异常检测结果
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getAnomalyDetection(params = {}) {
    return request({
      url: '/api/consume/report/anomaly-detection',
      method: 'get',
      params
    })
  },

  /**
   * 获取预测分析
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getForecastAnalysis(params) {
    return request({
      url: '/api/consume/report/forecast',
      method: 'get',
      params
    })
  }
}