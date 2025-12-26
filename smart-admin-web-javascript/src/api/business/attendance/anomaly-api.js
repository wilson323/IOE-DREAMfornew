/**
 * 考勤异常管理API
 * 提供考勤异常记录、申请、规则配置的所有后端接口调用
 */
import { request } from '@/utils/request';

export const anomalyApi = {
  // ========== 异常记录管理 ==========

  /**
   * 分页查询异常记录
   */
  getAnomalyPage(params) {
    return request.get('/api/v1/attendance/anomaly/page', { params });
  },

  /**
   * 查询异常详情
   */
  getAnomalyDetail(anomalyId) {
    return request.get(`/api/v1/attendance/anomaly/${anomalyId}`);
  },

  /**
   * 手动触发异常检测
   */
  triggerDetection(attendanceDate) {
    return request.post(`/api/v1/attendance/anomaly/detect/${attendanceDate}`);
  },

  /**
   * 忽略异常
   */
  ignoreAnomaly(anomalyId, handlerId, handlerName, comment) {
    return request.put(`/api/v1/attendance/anomaly/${anomalyId}/ignore`, null, {
      params: { handlerId, handlerName, comment }
    });
  },

  /**
   * 修正异常
   */
  correctAnomaly(anomalyId, handlerId, handlerName, comment) {
    return request.put(`/api/v1/attendance/anomaly/${anomalyId}/correct`, null, {
      params: { handlerId, handlerName, comment }
    });
  },

  // ========== 异常申请管理 ==========

  /**
   * 提交补卡申请
   */
  submitSupplementCardApply(data) {
    return request.post('/api/v1/attendance/anomaly-apply/supplement-card', data);
  },

  /**
   * 提交迟到说明申请
   */
  submitLateExplanationApply(data) {
    return request.post('/api/v1/attendance/anomaly-apply/late-explanation', data);
  },

  /**
   * 提交早退说明申请
   */
  submitEarlyExplanationApply(data) {
    return request.post('/api/v1/attendance/anomaly-apply/early-explanation', data);
  },

  /**
   * 提交旷工申诉申请
   */
  submitAbsentAppealApply(data) {
    return request.post('/api/v1/attendance/anomaly-apply/absent-appeal', data);
  },

  /**
   * 撤销申请
   */
  cancelApply(applyId, userId) {
    return request.put(`/api/v1/attendance/anomaly-apply/${applyId}/cancel`, null, {
      params: { userId }
    });
  },

  /**
   * 查询申请详情
   */
  getApplyDetail(applyId) {
    return request.get(`/api/v1/attendance/anomaly-apply/${applyId}`);
  },

  /**
   * 查询我的申请列表
   */
  getMyApplies(userId) {
    return request.get('/api/v1/attendance/anomaly-apply/my-applies', {
      params: { userId }
    });
  },

  /**
   * 查询待审批的申请列表
   */
  getPendingApplies() {
    return request.get('/api/v1/attendance/anomaly-apply/pending');
  },

  /**
   * 批准申请
   */
  approveApply(applyId, approverId, approverName, comment) {
    return request.put(`/api/v1/attendance/anomaly-apply/${applyId}/approve`, null, {
      params: { approverId, approverName, comment }
    });
  },

  /**
   * 驳回申请
   */
  rejectApply(applyId, approverId, approverName, comment) {
    return request.put(`/api/v1/attendance/anomaly-apply/${applyId}/reject`, null, {
      params: { approverId, approverName, comment }
    });
  },

  /**
   * 批量审批
   */
  batchApprove(data) {
    return request.put('/api/v1/attendance/anomaly-apply/batch-approve', data);
  },

  /**
   * 检查是否允许补卡
   */
  checkSupplementAllowed(userId, date) {
    return request.get('/api/v1/attendance/anomaly-apply/check-supplement-allowed', {
      params: { userId, date }
    });
  },

  // ========== 规则配置管理 ==========

  /**
   * 分页查询规则配置
   */
  getRuleConfigPage(params) {
    return request.get('/api/v1/attendance/rule-config/page', { params });
  },

  /**
   * 查询所有启用的规则
   */
  getEnabledRules() {
    return request.get('/api/v1/attendance/rule-config/enabled');
  },

  /**
   * 查询全局规则
   */
  getGlobalRule() {
    return request.get('/api/v1/attendance/rule-config/global');
  },

  /**
   * 查询规则详情
   */
  getRuleDetail(configId) {
    return request.get(`/api/v1/attendance/rule-config/${configId}`);
  },

  /**
   * 创建规则配置
   */
  createRule(data) {
    return request.post('/api/v1/attendance/rule-config', data);
  },

  /**
   * 更新规则配置
   */
  updateRule(configId, data) {
    return request.put(`/api/v1/attendance/rule-config/${configId}`, data);
  },

  /**
   * 启用/禁用规则
   */
  updateRuleStatus(configId, status) {
    return request.put(`/api/v1/attendance/rule-config/${configId}/status`, null, {
      params: { status }
    });
  },

  /**
   * 删除规则配置
   */
  deleteRule(configId) {
    return request.delete(`/api/v1/attendance/rule-config/${configId}`);
  }
};

export default anomalyApi;
