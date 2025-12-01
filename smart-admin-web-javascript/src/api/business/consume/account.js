import { SmartRequest } from '@/lib/smart-request'

const baseUrl = '/api/consume/account'

export const accountApi = {
  /**
   * 获取账户列表
   */
  getAccountList(params) {
    return SmartRequest.get(`${baseUrl}/list`, { params })
  },

  /**
   * 获取账户详情
   */
  getAccountDetail(accountId) {
    return SmartRequest.get(`${baseUrl}/detail/${accountId}`)
  },

  /**
   * 创建账户
   */
  createAccount(data) {
    return SmartRequest.post(baseUrl, data)
  },

  /**
   * 更新账户
   */
  updateAccount(accountId, data) {
    return SmartRequest.put(`${baseUrl}/${accountId}`, data)
  },

  /**
   * 删除账户
   */
  deleteAccount(accountId) {
    return SmartRequest.delete(`${baseUrl}/${accountId}`)
  },

  /**
   * 账户充值
   */
  recharge(accountId, data) {
    return SmartRequest.post(`${baseUrl}/${accountId}/recharge`, data)
  },

  /**
   * 批量充值
   */
  batchRecharge(data) {
    return SmartRequest.post(`${baseUrl}/batch-recharge`, data)
  },

  /**
   * 冻结账户
   */
  freezeAccount(accountId) {
    return SmartRequest.post(`${baseUrl}/${accountId}/freeze`)
  },

  /**
   * 解冻账户
   */
  unfreezeAccount(accountId) {
    return SmartRequest.post(`${accountId}/unfreeze`)
  },

  /**
   * 重置密码
   */
  resetPassword(accountId) {
    return SmartRequest.post(`${accountId}/reset-password`)
  },

  /**
   * 设置账户限额
   */
  setAccountLimit(accountId, data) {
    return SmartRequest.post(`${accountId}/limit`, data)
  },

  /**
   * 获取账户限额
   */
  getAccountLimit(accountId) {
    return SmartRequest.get(`${accountId}/limit`)
  },

  /**
   * 获取账户余额
   */
  getAccountBalance(accountId) {
    return SmartRequest.get(`${accountId}/balance`)
  },

  /**
   * 获取账户统计
   */
  getAccountStatistics(params) {
    return SmartRequest.get(`${baseUrl}/statistics`, { params })
  },

  /**
   * 导出账户信息
   */
  exportAccountData(params) {
    return SmartRequest.download(`${baseUrl}/export`, { params })
  },

  /**
   * 关闭账户
   */
  closeAccount(accountId) {
    return SmartRequest.post(`${accountId}/close`)
  },

  /**
   * 获取账户交易记录
   */
  getAccountTransactions(accountId, params) {
    return SmartRequest.get(`${accountId}/transactions`, { params })
  },

  /**
   * 获取设备列表（用于充值时选择）
   */
  getDeviceList() {
    return SmartRequest.get('/api/consume/device/list')
  }
}

export default accountApi