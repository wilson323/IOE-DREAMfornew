/**
 * 在线订餐API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-24
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest } from '@/lib/smart-request'

// 在线订餐相关接口
export const orderingApi = {
  /**
   * 获取菜品列表
   * @param {Number} userId 用户ID（可选）
   * @param {String} orderDate 订餐日期（可选）
   * @param {String} mealType 餐别类型（可选）
   * @returns {Promise}
   */
  getDishes: (params) => getRequest('/api/v1/consume/mobile/ordering/dishes', params),

  /**
   * 获取菜品详情
   * @param {Number} dishId 菜品ID
   * @returns {Promise}
   */
  getDishDetail: (dishId) => getRequest(`/api/v1/consume/mobile/ordering/dish/${dishId}`),

  /**
   * 创建订餐订单
   * @param {Object} data 订餐数据
   * @param {Number} data.userId 用户ID
   * @param {Array} data.dishIds 菜品ID数组
   * @param {String} data.orderDate 订餐日期
   * @param {String} data.mealType 餐别类型
   * @param {String} data.remark 备注
   * @returns {Promise}
   */
  createOrder: (data) => postRequest('/api/v1/consume/mobile/ordering/create', data),

  /**
   * 获取订餐记录列表
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 每页大小
   * @param {Number} params.orderStatus 订单状态（可选）
   * @returns {Promise}
   */
  getOrderingOrders: (userId, params) => getRequest(`/api/v1/consume/mobile/ordering/orders/${userId}`, params),

  /**
   * 取消订餐
   * @param {Number} orderId 订单ID
   * @returns {Promise}
   */
  cancelOrder: (orderId) => postRequest(`/api/v1/consume/mobile/ordering/cancel/${orderId}`, {}),

  /**
   * 核销订餐
   * @param {Number} orderId 订单ID
   * @returns {Promise}
   */
  verifyOrder: (orderId) => postRequest(`/api/v1/consume/mobile/ordering/verify/${orderId}`, {}),

  /**
   * 获取订餐统计信息
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getOrderingStatistics: (userId) => getRequest(`/api/v1/consume/mobile/ordering/statistics/${userId}`),

  /**
   * 获取可订餐时段
   * @returns {Promise}
   */
  getAvailableOrderingTimes: () => getRequest('/api/v1/consume/mobile/ordering/available-times'),

  /**
   * 获取菜品分类
   * @returns {Promise}
   */
  getDishCategories: () => getRequest('/api/v1/consume/mobile/ordering/categories')
}

export default orderingApi
