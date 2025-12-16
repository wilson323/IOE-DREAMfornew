/*
 * 消费管理 API
 * 
 * 根据API契约文档和已创建的Controller实现
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest, putRequest, deleteRequest, request } from '/@/lib/axios';

const postWithParams = (url, params, data) => {
  return request({ url, method: 'post', params, data });
};

export const consumeApi = {
  // ==================== 账户管理 ====================
  
  /**
   * 创建账户
   */
  createAccount: (data) => {
    return postRequest('/api/consume/account/create', data);
  },

  /**
   * 更新账户信息
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
   * 根据ID查询账户
   */
  getAccountById: (accountId) => {
    return getRequest(`/api/consume/account/getById/${accountId}`);
  },

  /**
   * 根据用户ID查询账户
   */
  getAccountByUserId: (userId) => {
    return getRequest(`/api/consume/account/getByUserId/${userId}`);
  },

  /**
   * 分页查询账户列表
   */
  pageAccounts: (params) => {
    return getRequest('/api/consume/account/list', params);
  },

  /**
   * 查询账户列表（不分页）
   */
  listAccounts: (params) => {
    return getRequest('/api/consume/account/list', params);
  },

  /**
   * 分页查询账户列表（别名，Smart-Admin 页面调用习惯）
   */
  getAccountList: (params) => {
    return getRequest('/api/consume/account/list', params);
  },

  /**
   * 获取账户详情
   */
  getAccountDetail: (accountId) => {
    return getRequest(`/api/consume/account/${accountId}/detail`);
  },

  /**
   * 增加账户余额
   */
  addBalance: (params) => {
    return postWithParams('/api/consume/account/balance/add', params);
  },

  /**
   * 扣减账户余额
   */
  deductBalance: (params) => {
    return postWithParams('/api/consume/account/balance/deduct', params);
  },

  /**
   * 冻结账户金额
   */
  freezeAmount: (params) => {
    return postWithParams('/api/consume/account/balance/freezeAmount', params);
  },

  /**
   * 解冻账户金额
   */
  unfreezeAmount: (params) => {
    return postWithParams('/api/consume/account/balance/unfreezeAmount', params);
  },

  /**
   * 验证账户余额
   */
  validateBalance: (params) => {
    return getRequest('/api/consume/account/balance/validate', params);
  },

  /**
   * 启用账户
   */
  enableAccount: (accountId) => {
    return postRequest(`/api/consume/account/status/enable/${accountId}`);
  },

  /**
   * 禁用账户
   */
  disableAccount: (accountId) => {
    return postRequest(`/api/consume/account/status/disable/${accountId}`);
  },

  /**
   * 冻结账户状态
   */
  freezeAccountStatus: (accountId, reason) => {
    return postWithParams(`/api/consume/account/status/freeze/${accountId}`, { reason });
  },

  /**
   * 解冻账户状态
   */
  unfreezeAccountStatus: (accountId) => {
    return postRequest(`/api/consume/account/status/unfreeze/${accountId}`);
  },

  /**
   * 关闭账户
   */
  closeAccount: (accountId, reason) => {
    return postWithParams(`/api/consume/account/status/close/${accountId}`, { reason });
  },

  /**
   * 账户充值（运营端）
   */
  rechargeAccount: (data) => {
    return postWithParams(`/api/consume/account/${data.accountId}/recharge`, {
      amount: data.rechargeAmount,
      rechargeType: data.rechargeType,
      remark: data.remark || '',
    });
  },

  /**
   * 获取账户余额
   */
  getAccountBalance: (accountId) => {
    return getRequest(`/api/consume/account/balance/${accountId}`);
  },

  /**
   * 批量查询账户
   */
  batchGetAccountsByIds: (accountIds) => {
    return postRequest('/api/consume/account/batchGetByIds', accountIds);
  },

  /**
   * 获取账户统计
   */
  getAccountStatistics: (accountKindId) => {
    return getRequest('/api/consume/account/statistics', { accountKindId });
  },

  // ==================== 消费交易 ====================

  /**
   * 执行消费交易
   */
  executeTransaction: (data) => {
    return postRequest('/api/v1/consume/transaction/execute', data);
  },

  /**
   * 执行消费请求（兼容旧接口）
   */
  executeConsume: (data) => {
    return postRequest('/api/v1/consume/transaction/execute', data);
  },

  /**
   * 获取设备详情
   */
  getDeviceDetail: (deviceId) => {
    return getRequest(`/api/v1/consume/transaction/device/${deviceId}`);
  },

  /**
   * 获取设备状态统计
   */
  getDeviceStatistics: (areaId) => {
    return getRequest('/api/v1/consume/transaction/device/statistics', { areaId });
  },

  /**
   * 获取实时统计
   */
  getRealtimeStatistics: (areaId) => {
    return getRequest('/api/v1/consume/transaction/realtime-statistics', { areaId });
  },

  /**
   * 分页查询交易记录
   */
  queryTransactions: (params) => {
    return getRequest('/api/v1/consume/transaction/query', params);
  },

  /**
   * 获取交易详情
   */
  getTransactionDetail: (transactionNo) => {
    return getRequest(`/api/v1/consume/transaction/detail/${transactionNo}`);
  },

  // ==================== 设备管理（通用设备服务） ====================

  /**
   * 分页查询设备列表
   */
  pageDevices: (params) => {
    return getRequest('/api/v1/device/list', params);
  },

  /**
   * 删除设备
   */
  deleteDevice: (deviceId) => {
    return deleteRequest(`/api/v1/device/${deviceId}`);
  },

  // ==================== 报表管理 ====================

  /**
   * 生成消费报表
   */
  generateReport: (templateId, params) => {
    return postWithParams('/api/v1/consume/report/generate', { templateId }, params);
  },

  /**
   * 获取报表模板列表
   */
  getReportTemplates: (templateType) => {
    return getRequest('/api/v1/consume/report/templates', { templateType });
  },

  /**
   * 获取报表统计数据
   */
  getReportStatistics: (params) => {
    return getRequest('/api/v1/consume/report/statistics', params);
  },

  /**
   * 导出报表
   */
  exportReport: (templateId, params, exportFormat) => {
    return postWithParams('/api/v1/consume/report/export', { templateId, exportFormat }, params);
  },

  // ==================== 商品管理 ====================

  /**
   * 分页查询商品列表
   */
  pageProducts: (params) => {
    return getRequest('/api/v1/consume/product/query', params);
  },

  /**
   * 上下架商品
   */
  setProductAvailable: (productId, available) => {
    return postWithParams(`/api/v1/consume/product/${productId}/available`, { available });
  },

  /**
   * 删除商品
   */
  deleteProduct: (productId) => {
    return deleteRequest(`/api/v1/consume/product/${productId}`);
  },

  // ==================== 支付管理 ====================

  /**
   * 创建微信支付订单
   */
  createWechatPayOrder: (params) => {
    return postWithParams('/api/v1/consume/payment/wechat/createOrder', params);
  },

  /**
   * 处理微信支付回调
   */
  handleWechatPayNotify: (notifyData) => {
    return postRequest('/api/v1/consume/payment/wechat/notify', notifyData);
  },

  /**
   * 创建支付宝支付订单
   */
  createAlipayOrder: (params) => {
    return postWithParams('/api/v1/consume/payment/alipay/createOrder', params);
  },

  /**
   * 处理支付宝支付回调
   */
  handleAlipayNotify: (params) => {
    return postWithParams('/api/v1/consume/payment/alipay/notify', params);
  },

  /**
   * 微信支付退款
   */
  wechatRefund: (params) => {
    return postWithParams('/api/v1/consume/payment/wechat/refund', params);
  },

  /**
   * 支付宝退款
   */
  alipayRefund: (params) => {
    return postWithParams('/api/v1/consume/payment/alipay/refund', params);
  },
};

