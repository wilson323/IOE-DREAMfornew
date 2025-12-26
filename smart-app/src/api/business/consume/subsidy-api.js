/**
 * 补贴管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-24
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest } from '@/lib/smart-request'

// 补贴管理相关接口
export const subsidyApi = {
  /**
   * 获取补贴余额汇总
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getSubsidyBalance: (userId) => getRequest(`/api/v1/consume/mobile/subsidy/balance/${userId}`),

  /**
   * 获取补贴发放记录
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getSubsidyRecords: (userId, params) => getRequest(`/api/v1/consume/mobile/subsidy/records/${userId}`, params),

  /**
   * 获取可用补贴列表
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getAvailableSubsidies: (userId) => getRequest(`/api/v1/consume/mobile/subsidy/available/${userId}`),

  /**
   * 获取即将过期的补贴
   * @param {Number} userId 用户ID
   * @param {Number} days 预警天数（默认7天）
   * @returns {Promise}
   */
  getExpiringSubsidies: (userId, days = 7) => getRequest(`/api/v1/consume/mobile/subsidy/expiring/${userId}`, { days }),

  /**
   * 获取补贴使用明细
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getSubsidyUsage: (userId, params) => getRequest(`/api/v1/consume/mobile/subsidy/usage/${userId}`, params),

  /**
   * 获取补贴统计信息
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getSubsidyStatistics: (userId) => getRequest(`/api/v1/consume/mobile/subsidy/statistics/${userId}`),

  /**
   * 获取即将用完的补贴
   * @param {Number} userId 用户ID
   * @param {Number} threshold 使用率阈值（默认80%）
   * @returns {Promise}
   */
  getNearlyDepletedSubsidies: (userId, threshold = 80) => getRequest(`/api/v1/consume/mobile/subsidy/nearly-depleted/${userId}`, { threshold })
}

export default subsidyApi
