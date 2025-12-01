/*
 * 消费管理 API
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025/11/17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { SmartRequest } from '@/lib/smart-request';

const baseUrl = '/api/consume';

export const consumeApi = {
  /**
   * 消费支付
   */
  pay(data) {
    return SmartRequest.post(`${baseUrl}/pay`, data);
  },

  /**
   * 执行消费（支持多种消费模式）
   */
  consume(data) {
    return SmartRequest.post(`${baseUrl}/consume`, data);
  },

  /**
   * 分页查询消费记录
   */
  getRecords(params) {
    return SmartRequest.get(`${baseUrl}/records`, { params });
  },

  /**
   * 获取消费详情
   */
  getDetail(id) {
    return SmartRequest.get(`${baseUrl}/detail/${id}`);
  },

  /**
   * 获取消费统计
   */
  getStatistics(params) {
    return SmartRequest.get(`${baseUrl}/statistics`, { params });
  },

  /**
   * 消费退款
   */
  refund(id, reason) {
    return SmartRequest.post(`${baseUrl}/refund/${id}`, { reason });
  },

  /**
   * 获取可用消费模式
   */
  getModes() {
    return SmartRequest.get(`${baseUrl}/modes`);
  },

  /**
   * 查询账户余额
   */
  getAccountBalance(userId) {
    return SmartRequest.get(`${baseUrl}/account/${userId}/balance`);
  },

  /**
   * 冻结账户
   */
  freezeAccount(userId, reason) {
    return SmartRequest.post(`${baseUrl}/account/${userId}/freeze`, { reason });
  },

  /**
   * 解冻账户
   */
  unfreezeAccount(userId, reason) {
    return SmartRequest.post(`${baseUrl}/account/${userId}/unfreeze`, { reason });
  },

  /**
   * 消费验证
   */
  validateConsume(params) {
    return SmartRequest.get(`${baseUrl}/validate`, { params });
  },

  /**
   * 批量消费
   */
  batchConsume(data) {
    return SmartRequest.post(`${baseUrl}/batch`, data);
  },

  /**
   * 导出消费记录
   */
  exportRecords(params) {
    return SmartRequest.download(`${baseUrl}/export`, { params });
  },

  /**
   * 获取消费趋势
   */
  getConsumeTrend(params) {
    return SmartRequest.get(`${baseUrl}/trend`, { params });
  },

  /**
   * 取消消费
   */
  cancelConsume(id, reason) {
    return SmartRequest.post(`${baseUrl}/cancel/${id}`, { reason });
  },

  /**
   * 获取消费日志
   */
  getConsumeLogs(id) {
    return SmartRequest.get(`${baseUrl}/logs/${id}`);
  },

  /**
   * 同步消费数据
   */
  syncConsumeData(params) {
    return SmartRequest.post(`${baseUrl}/sync`, params);
  }
};

export default consumeApi;