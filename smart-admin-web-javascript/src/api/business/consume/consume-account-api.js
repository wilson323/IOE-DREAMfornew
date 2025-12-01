/*
 * 消费账户管理 API
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025/11/17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const consumeAccountApi = {
  /**
   * 查询账户列表
   */
  queryAccountList: (params) => {
    return postRequest('/api/consume/account/list', params);
  },

  /**
   * 查询账户详情
   */
  getAccountDetail: (accountId) => {
    return getRequest(`/api/consume/account/detail/${accountId}`);
  },

  /**
   * 添加账户
   */
  addAccount: (data) => {
    return postRequest('/api/consume/account/add', data);
  },

  /**
   * 编辑账户
   */
  updateAccount: (data) => {
    return putRequest('/api/consume/account/update', data);
  },

  /**
   * 删除账户
   */
  deleteAccount: (accountId) => {
    return deleteRequest(`/api/consume/account/delete/${accountId}`);
  },

  /**
   * 账户充值
   */
  rechargeAccount: (data) => {
    return postRequest('/api/consume/account/recharge', data);
  },

  /**
   * 账户退款
   */
  refundAccount: (data) => {
    return postRequest('/api/consume/account/refund', data);
  },

  /**
   * 冻结账户
   */
  freezeAccount: (accountId, reason) => {
    return postRequest(`/api/consume/account/freeze/${accountId}`, { reason });
  },

  /**
   * 解冻账户
   */
  unfreezeAccount: (accountId, reason) => {
    return postRequest(`/api/consume/account/unfreeze/${accountId}`, { reason });
  },

  /**
   * 重置账户密码
   */
  resetPassword: (accountId) => {
    return postRequest(`/api/consume/account/reset-password/${accountId}`);
  },

  /**
   * 批量操作账户
   */
  batchOperateAccounts: (data) => {
    return postRequest('/api/consume/account/batch-operate', data);
  },

  /**
   * 批量充值
   */
  batchRecharge: (data) => {
    return postRequest('/api/consume/account/batch-recharge', data);
  },

  /**
   * 获取账户统计数据
   */
  getAccountStats: () => {
    return getRequest('/api/consume/account/stats');
  },

  /**
   * 获取账户余额
   */
  getAccountBalance: (accountId) => {
    return getRequest(`/api/consume/account/balance/${accountId}`);
  },

  /**
   * 查询消费记录
   */
  queryConsumeRecords: (params) => {
    return postRequest('/api/consume/account/consume-records', params);
  },

  /**
   * 查询充值记录
   */
  queryRechargeRecords: (params) => {
    return postRequest('/api/consume/account/recharge-records', params);
  },

  /**
   * 查询退款记录
   */
  queryRefundRecords: (params) => {
    return postRequest('/api/consume/account/refund-records', params);
  },

  /**
   * 导出账户数据
   */
  exportAccounts: (params) => {
    return postRequest('/api/consume/account/export', params, {
      responseType: 'blob',
    });
  },

  /**
   * 导出账户记录
   */
  exportAccountRecords: (accountId, recordType, params) => {
    return postRequest(`/api/consume/account/export-records/${accountId}/${recordType}`, params, {
      responseType: 'blob',
    });
  },

  /**
   * 设置账户限额
   */
  setAccountLimit: (data) => {
    return putRequest('/api/consume/account/set-limit', data);
  },

  /**
   * 获取账户限额信息
   */
  getAccountLimit: (accountId) => {
    return getRequest(`/api/consume/account/limit/${accountId}`);
  },

  /**
   * 账户转账
   */
  transferBalance: (data) => {
    return postRequest('/api/consume/account/transfer', data);
  },

  /**
   * 获取账户操作日志
   */
  getAccountLogs: (params) => {
    return postRequest('/api/consume/account/logs', params);
  },

  /**
   * 验证账户状态
   */
  validateAccountStatus: (accountId) => {
    return getRequest(`/api/consume/account/validate/${accountId}`);
  },

  /**
   * 同步账户数据
   */
  syncAccountData: (accountId) => {
    return postRequest(`/api/consume/account/sync/${accountId}`);
  },

  /**
   * 批量导入账户
   */
  batchImportAccounts: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return postRequest('/api/consume/account/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * 获取账户分类信息
   */
  getAccountCategories: () => {
    return getRequest('/api/consume/account/categories');
  },

  /**
   * 设置账户分类
   */
  setAccountCategory: (accountId, categoryId) => {
    return putRequest(`/api/consume/account/category/${accountId}`, { categoryId });
  },

  /**
   * 获取账户统计报表
   */
  getAccountReport: (params) => {
    return postRequest('/api/consume/account/report', params);
  },

  /**
   * 获取账户月度统计
   */
  getAccountMonthlyStats: (accountId, year) => {
    return getRequest(`/api/consume/account/monthly-stats/${accountId}/${year}`);
  },

  /**
   * 启用/禁用账户
   */
  toggleAccountStatus: (accountId, status) => {
    return putRequest(`/api/consume/account/status/${accountId}`, { status });
  },

  /**
   * 设置账户密码
   */
  setAccountPassword: (data) => {
    return putRequest('/api/consume/account/password', data);
  },

  /**
   * 验证账户密码
   */
  validatePassword: (data) => {
    return postRequest('/api/consume/account/validate-password', data);
  },

  /**
   * 获取账户安全设置
   */
  getAccountSecurity: (accountId) => {
    return getRequest(`/api/consume/account/security/${accountId}`);
  },

  /**
   * 更新账户安全设置
   */
  updateAccountSecurity: (data) => {
    return putRequest('/api/consume/account/security', data);
  },
};