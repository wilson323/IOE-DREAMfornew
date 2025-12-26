/**
 * 充值管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-24
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest } from '@/lib/smart-request'

// 充值管理相关接口
export const rechargeApi = {
  /**
   * 创建充值订单
   * @param {Object} data 充值数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.rechargeAmount 充值金额
   * @param {String} data.paymentMethod 支付方式（WECHAT/ALIPAY）
   * @returns {Promise}
   */
  createOrder: (data) => postRequest('/api/v1/consume/mobile/recharge/create', data),

  /**
   * 查询支付结果
   * @param {String} orderId 订单ID
   * @returns {Promise}
   */
  getPaymentResult: (orderId) => getRequest(`/api/v1/consume/mobile/recharge/result/${orderId}`),

  /**
   * 取消充值订单
   * @param {String} orderId 订单ID
   * @returns {Promise}
   */
  cancelOrder: (orderId) => postRequest(`/api/v1/consume/mobile/recharge/cancel/${orderId}`, {}),

  /**
   * 获取充值订单详情
   * @param {String} orderId 订单ID
   * @returns {Promise}
   */
  getOrderDetail: (orderId) => getRequest(`/api/v1/consume/mobile/recharge/detail/${orderId}`),

  /**
   * 获取充值记录列表
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 每页大小
   * @returns {Promise}
   */
  getRechargeRecords: (userId, params) => getRequest(`/api/v1/consume/mobile/recharge/records/${userId}`, params),

  /**
   * 获取充值统计信息
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getRechargeStatistics: (userId) => getRequest(`/api/v1/consume/mobile/recharge/statistics/${userId}`),

  /**
   * 发起微信支付
   * @param {String} orderId 订单ID
   * @returns {Promise}
   */
  wechatPay: (orderId) => postRequest('/api/v1/consume/mobile/recharge/wechat/pay', { orderId }),

  /**
   * 发起支付宝支付
   * @param {String} orderId 订单ID
   * @returns {Promise}
   */
  alipay: (orderId) => postRequest('/api/v1/consume/mobile/recharge/alipay/pay', { orderId })
}

export default rechargeApi
