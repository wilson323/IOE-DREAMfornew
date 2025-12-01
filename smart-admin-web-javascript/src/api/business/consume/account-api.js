import { request } from '@/utils/request'

/**
 * 账户管理API接口
 */
export const consumeAccountApi = {
  /**
   * 创建账户
   * @param {Object} data 账户数据
   * @returns {Promise} 请求结果
   */
  createAccount(data) {
    return request({
      url: '/api/consume/account/create',
      method: 'post',
      data
    })
  },

  /**
   * 获取账户列表
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getAccountList(params) {
    return request({
      url: '/api/consume/account/list',
      method: 'get',
      params
    })
  },

  /**
   * 获取账户详情
   * @param {Number} accountId 账户ID
   * @returns {Promise} 请求结果
   */
  getAccountDetail(accountId) {
    return request({
      url: `/api/consume/account/detail/${accountId}`,
      method: 'get'
    })
  },

  /**
   * 更新账户信息
   * @param {Object} data 更新数据
   * @returns {Promise} 请求结果
   */
  updateAccount(data) {
    return request({
      url: '/api/consume/account/update',
      method: 'put',
      data
    })
  },

  /**
   * 账户充值
   * @param {Object} data 充值数据
   * @returns {Promise} 请求结果
   */
  rechargeAccount(data) {
    return request({
      url: '/api/consume/account/recharge',
      method: 'post',
      data
    })
  },

  /**
   * 查询账户余额
   * @param {Number} accountId 账户ID
   * @returns {Promise} 请求结果
   */
  getAccountBalance(accountId) {
    return request({
      url: `/api/consume/account/balance/${accountId}`,
      method: 'get'
    })
  },

  /**
   * 冻结账户
   * @param {Number} accountId 账户ID
   * @param {String} reason 冻结原因
   * @returns {Promise} 请求结果
   */
  freezeAccount(accountId, reason) {
    return request({
      url: `/api/consume/account/freeze/${accountId}`,
      method: 'post',
      params: { reason }
    })
  },

  /**
   * 解冻账户
   * @param {Number} accountId 账户ID
   * @param {String} reason 解冻原因
   * @returns {Promise} 请求结果
   */
  unfreezeAccount(accountId, reason) {
    return request({
      url: `/api/consume/account/unfreeze/${accountId}`,
      method: 'post',
      params: { reason }
    })
  },

  /**
   * 关闭账户
   * @param {Number} accountId 账户ID
   * @param {String} reason 关闭原因
   * @returns {Promise} 请求结果
   */
  closeAccount(accountId, reason) {
    return request({
      url: `/api/consume/account/close/${accountId}`,
      method: 'post',
      params: { reason }
    })
  },

  /**
   * 获取账户交易记录
   * @param {Number} accountId 账户ID
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getAccountTransactions(accountId, params) {
    return request({
      url: `/api/consume/account/transactions/${accountId}`,
      method: 'get',
      params
    })
  },

  /**
   * 获取账户统计
   * @param {Object} params 查询参数
   * @returns {Promise} 请求结果
   */
  getAccountStatistics(params = {}) {
    return request({
      url: '/api/consume/account/statistics',
      method: 'get',
      params
    })
  },

  /**
   * 导出账户数据
   * @param {Object} params 导出参数
   * @returns {Promise} 请求结果
   */
  exportAccounts(params) {
    return request({
      url: '/api/consume/account/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  /**
   * 获取账户类型
   * @returns {Promise} 请求结果
   */
  getAccountTypes() {
    return request({
      url: '/api/consume/account/types',
      method: 'get'
    })
  },

  /**
   * 批量修改账户状态
   * @param {Array} accountIds 账户ID列表
   * @param {Number} status 状态
   * @param {String} reason 操作原因
   * @returns {Promise} 请求结果
   */
  batchUpdateStatus(accountIds, status, reason = '') {
    return request({
      url: '/api/consume/account/batch/status',
      method: 'post',
      data: { accountIds, status, reason }
    })
  }
}