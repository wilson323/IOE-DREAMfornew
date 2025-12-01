import { request } from '@/utils/request'

/**
 * 消费记录统一错误处理
 * @param {Error} error 错误对象
 * @param {string} operation 操作描述
 * @returns {Object} 格式化的错误信息
 */
const handleConsumeError = (error, operation) => {
  console.error(`消费${operation}失败:`, error)

  // 根据不同的错误码返回用户友好的错误信息
  if (error.response) {
    const { status, data } = error.response
    switch (status) {
      case 400:
        return { success: false, message: data.message || '请求参数错误', code: 'BAD_REQUEST' }
      case 401:
        return { success: false, message: '请先登录', code: 'UNAUTHORIZED' }
      case 403:
        return { success: false, message: '权限不足', code: 'FORBIDDEN' }
      case 404:
        return { success: false, message: '请求的资源不存在', code: 'NOT_FOUND' }
      case 500:
        return { success: false, message: '服务器内部错误，请稍后重试', code: 'SERVER_ERROR' }
      default:
        return { success: false, message: data.message || `消费${operation}失败`, code: 'UNKNOWN_ERROR' }
    }
  } else if (error.code === 'ECONNABORTED') {
    return { success: false, message: '请求超时，请检查网络连接', code: 'TIMEOUT' }
  } else {
    return { success: false, message: `消费${operation}失败：${error.message}`, code: 'NETWORK_ERROR' }
  }
}

/**
 * 消费记录API接口
 */
export const consumeRecordApi = {
  /**
   * 消费支付 - 核心支付API
   * @param {Object} payParams 支付参数
   * @returns {Promise} 请求结果
   */
  async consumePay(payParams) {
    try {
      const response = await request({
        url: '/api/consume/pay',
        method: 'post',
        data: payParams,
        timeout: 30000 // 支付操作超时时间30秒
      })
      return { success: true, data: response, message: '支付成功' }
    } catch (error) {
      return handleConsumeError(error, '支付')
    }
  },

  /**
   * 获取消费记录列表
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getRecordList(params) {
    return request({
      url: '/api/consume/records',
      method: 'get',
      params
    })
  },

  /**
   * 获取消费记录详情
   * @param {Number} recordId 记录ID
   * @returns {Promise} 请求结果
   */
  getRecordDetail(recordId) {
    return request({
      url: `/api/consume/detail/${recordId}`,
      method: 'get'
    })
  },

  /**
   * 获取消费统计信息
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getStatistics(params = {}) {
    return request({
      url: '/api/consume/statistics',
      method: 'get',
      params
    })
  },

  /**
   * 导出消费记录
   * @param {Object} params 导出参数
   * @returns {Promise} 请求结果
   */
  exportRecords(params) {
    return request({
      url: '/api/consume/export',
      method: 'post',
      data: params,
      responseType: 'blob'
    })
  },

  /**
   * 导出单条消费记录
   * @param {Number} recordId 记录ID
   * @returns {Promise} 请求结果
   */
  exportSingleRecord(recordId) {
    return request({
      url: `/api/consume/record/${recordId}/export`,
      method: 'post',
      responseType: 'blob'
    })
  },

  /**
   * 申请退款
   * @param {Number} recordId 记录ID
   * @param {String} reason 退款原因
   * @returns {Promise} 请求结果
   */
  applyRefund(recordId, reason = '') {
    return request({
      url: `/api/consume/refund/${recordId}`,
      method: 'post',
      params: { reason }
    })
  },

  /**
   * 获取退款申请记录
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getRefundList(params) {
    return request({
      url: '/api/consume/record/refund/list',
      method: 'get',
      params
    })
  },

  /**
   * 获取操作日志
   * @param {Number} recordId 记录ID
   * @returns {Promise} 请求结果
   */
  getOperationLogs(recordId) {
    return request({
      url: `/api/consume/record/${recordId}/logs`,
      method: 'get'
    })
  },

  /**
   * 打印小票
   * @param {Number} recordId 记录ID
   * @returns {Promise} 请求结果
   */
  printReceipt(recordId) {
    return request({
      url: `/api/consume/record/${recordId}/print`,
      method: 'post'
    })
  },

  /**
   * 取消消费记录
   * @param {Number} recordId 记录ID
   * @param {Object} params 取消原因
   * @returns {Promise} 请求结果
   */
  cancelRecord(recordId, params) {
    return request({
      url: `/api/consume/record/${recordId}/cancel`,
      method: 'post',
      data: params
    })
  },

  /**
   * 修改消费记录备注
   * @param {Number} recordId 记录ID
   * @param {String} remark 备注内容
   * @returns {Promise} 请求结果
   */
  updateRemark(recordId, remark) {
    return request({
      url: `/api/consume/record/${recordId}/remark`,
      method: 'put',
      data: { remark }
    })
  },

  /**
   * 获取消费趋势数据
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getConsumeTrend(params) {
    return request({
      url: '/api/consume/trend',
      method: 'get',
      params
    })
  },

  /**
   * 获取消费模式统计
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getConsumeModeStats(params) {
    return request({
      url: '/api/consume/record/mode-stats',
      method: 'get',
      params
    })
  },

  /**
   * 获取设备消费统计
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getDeviceStats(params) {
    return request({
      url: '/api/consume/record/device-stats',
      method: 'get',
      params
    })
  },

  /**
   * 获取用户消费排行
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getUserRanking(params) {
    return request({
      url: '/api/consume/record/user-ranking',
      method: 'get',
      params
    })
  },

  /**
   * 批量导入消费记录
   * @param {FormData} formData 表单数据
   * @returns {Promise} 请求结果
   */
  batchImport(formData) {
    return request({
      url: '/api/consume/record/batch-import',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 获取导入模板
   * @returns {Promise} 请求结果
   */
  getImportTemplate() {
    return request({
      url: '/api/consume/record/import-template',
      method: 'get',
      responseType: 'blob'
    })
  },

  /**
   * 验证消费记录
   * @param {Object} params 验证参数
   * @returns {Promise} 请求结果
   */
  validateRecord(params) {
    return request({
      url: '/api/consume/record/validate',
      method: 'post',
      data: params
    })
  },

  /**
   * 同步消费记录
   * @param {Object} params 同步参数
   * @returns {Promise} 请求结果
   */
  syncRecords(params) {
    return request({
      url: '/api/consume/record/sync',
      method: 'post',
      data: params
    })
  }
}