/**
 * 退款管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-24
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest } from '@/lib/smart-request'

// 退款管理相关接口
export const refundApi = {
  /**
   * 申请退款
   * @param {Object} data 退款申请数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.consumeId 消费记录ID
   * @param {Number} data.refundAmount 退款金额
   * @param {String} data.refundReason 退款原因
   * @returns {Promise}
   */
  applyRefund: (data) => postRequest('/api/v1/consume/mobile/refund/apply', data),

  /**
   * 获取退款记录列表
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 每页大小
   * @param {Number} params.refundStatus 退款状态（可选）
   * @returns {Promise}
   */
  getRefundRecords: (userId, params) => getRequest(`/api/v1/consume/mobile/refund/records/${userId}`, params),

  /**
   * 查询退款状态
   * @param {Number} refundId 退款ID
   * @returns {Promise}
   */
  getRefundStatus: (refundId) => getRequest(`/api/v1/consume/mobile/refund/status/${refundId}`),

  /**
   * 取消退款申请
   * @param {Number} refundId 退款ID
   * @returns {Promise}
   */
  cancelRefund: (refundId) => postRequest(`/api/v1/consume/mobile/refund/cancel/${refundId}`, {}),

  /**
   * 获取可退款消费记录
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getAvailableRefundRecords: (userId) => getRequest(`/api/v1/consume/mobile/refund/available/${userId}`),

  /**
   * 获取退款统计信息
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getRefundStatistics: (userId) => getRequest(`/api/v1/consume/mobile/refund/statistics/${userId}`),

  /**
   * 获取退款详情
   * @param {Number} refundId 退款ID
   * @returns {Promise}
   */
  getRefundDetail: (refundId) => getRequest(`/api/v1/consume/mobile/refund/detail/${refundId}`)
}

export default refundApi
