/**
 * 卡片管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-24
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest } from '@/lib/smart-request'

// 卡片管理相关接口
export const cardApi = {
  /**
   * 获取用户卡片状态列表
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getCardStatus: (userId) => getRequest(`/api/v1/consume/mobile/card/status/${userId}`),

  /**
   * 申请卡片挂失
   * @param {Object} data 挂失申请数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.cardId 卡片ID
   * @param {String} data.lossReason 挂失原因
   * @returns {Promise}
   */
  reportLoss: (data) => postRequest('/api/v1/consume/mobile/card/loss', data),

  /**
   * 申请卡片解挂
   * @param {Object} data 解挂申请数据
   * @param {Number} data.userId 用户ID
   * @param {Number} data.cardId 卡片ID
   * @param {String} data.unlockReason 解挂原因
   * @returns {Promise}
   */
  reportUnlock: (data) => postRequest('/api/v1/consume/mobile/card/unlock', data),

  /**
   * 获取卡片操作历史
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 每页大小
   * @returns {Promise}
   */
  getOperationHistory: (userId, params) => getRequest(`/api/v1/consume/mobile/card/history/${userId}`, params),

  /**
   * 获取卡片详情
   * @param {Number} cardId 卡片ID
   * @returns {Promise}
   */
  getCardDetail: (cardId) => getRequest(`/api/v1/consume/mobile/card/detail/${cardId}`),

  /**
   * 验证卡片密码
   * @param {Number} userId 用户ID
   * @param {String} cardPassword 卡片密码
   * @returns {Promise}
   */
  verifyPassword: (userId, cardPassword) => getRequest('/api/v1/consume/mobile/card/verify-password', {
    userId,
    cardPassword
  }),

  /**
   * 获取卡片统计信息
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getCardStatistics: (userId) => getRequest(`/api/v1/consume/mobile/card/statistics/${userId}`)
}

export default cardApi
