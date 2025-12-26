import request from '@/utils/request';

/**
 * 智能排班 API
 *
 * 提供排班计划管理、优化执行、结果查询、规则管理等功能的 API 接口
 */

export const smartScheduleApi = {
  // ==================== 排班计划管理 ====================

  /**
   * 创建智能排班计划
   * @param {Object} data - 计划表单数据
   * @returns {Promise}
   */
  createPlan(data) {
    return request.post('/api/v1/schedule/plan', data);
  },

  /**
   * 更新排班计划
   * @param {Number} planId - 计划ID
   * @param {Object} data - 更新数据
   * @returns {Promise}
   */
  updatePlan(planId, data) {
    return request.put(`/api/v1/schedule/plan/${planId}`, data);
  },

  /**
   * 删除排班计划
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  deletePlan(planId) {
    return request.delete(`/api/v1/schedule/plan/${planId}`);
  },

  /**
   * 批量删除排班计划
   * @param {Array} planIds - 计划ID列表
   * @returns {Promise}
   */
  batchDeletePlan(planIds) {
    return request.delete('/api/v1/schedule/plan/batch', { data: planIds });
  },

  /**
   * 查询排班计划列表（分页）
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  queryPlanPage(params) {
    return request.get('/api/v1/schedule/plan/page', { params });
  },

  /**
   * 查询排班计划详情
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  getPlanDetail(planId) {
    return request.get(`/api/v1/schedule/plan/${planId}`);
  },

  /**
   * 执行排班优化
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  executeOptimization(planId) {
    return request.post(`/api/v1/schedule/plan/${planId}/execute`);
  },

  /**
   * 确认排班计划
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  confirmPlan(planId) {
    return request.post(`/api/v1/schedule/plan/${planId}/confirm`);
  },

  /**
   * 取消排班计划
   * @param {Number} planId - 计划ID
   * @param {String} reason - 取消原因
   * @returns {Promise}
   */
  cancelPlan(planId, reason) {
    return request.post(`/api/v1/schedule/plan/${planId}/cancel`, null, { params: { reason } });
  },

  /**
   * 导出排班结果
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  exportScheduleResult(planId) {
    return request.get(`/api/v1/schedule/plan/${planId}/export`, { responseType: 'blob' });
  },

  // ==================== 排班结果查询 ====================

  /**
   * 查询排班结果列表（分页）
   * @param {Object} params - 查询参数
   * @param {Number} params.planId - 计划ID
   * @param {Number} params.pageNum - 页码
   * @param {Number} params.pageSize - 页大小
   * @param {Number} params.employeeId - 员工ID（可选）
   * @param {String} params.startDate - 开始日期（可选）
   * @param {String} params.endDate - 结束日期（可选）
   * @returns {Promise}
   */
  queryResultPage(params) {
    return request.get('/api/v1/schedule/result/page', { params });
  },

  /**
   * 查询排班结果列表（不分页）
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  queryResultList(planId) {
    return request.get(`/api/v1/schedule/result/list/${planId}`);
  },

  // ==================== 规则管理 ====================

  /**
   * 创建排班规则
   * @param {Object} data - 规则表单数据
   * @returns {Promise}
   */
  createRule(data) {
    return request.post('/api/v1/schedule/rule', data);
  },

  /**
   * 更新排班规则
   * @param {Number} ruleId - 规则ID
   * @param {Object} data - 更新数据
   * @returns {Promise}
   */
  updateRule(ruleId, data) {
    return request.put(`/api/v1/schedule/rule/${ruleId}`, data);
  },

  /**
   * 删除排班规则
   * @param {Number} ruleId - 规则ID
   * @returns {Promise}
   */
  deleteRule(ruleId) {
    return request.delete(`/api/v1/schedule/rule/${ruleId}`);
  },

  /**
   * 查询规则列表（分页）
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  queryRulePage(params) {
    return request.get('/api/v1/schedule/rule/page', { params });
  },

  /**
   * 查询规则列表（不分页）
   * @returns {Promise}
   */
  queryRuleList() {
    return request.get('/api/v1/schedule/rule/list');
  },

  /**
   * 验证规则表达式
   * @param {String} expression - 规则表达式
   * @returns {Promise}
   */
  validateRuleExpression(expression) {
    return request.post('/api/v1/schedule/rule/validate', null, { params: { expression } });
  },

  /**
   * 测试规则执行
   * @param {String} expression - 规则表达式
   * @param {Object} context - 执行上下文
   * @returns {Promise}
   */
  testRuleExecution(expression, context) {
    return request.post('/api/v1/schedule/rule/test', { expression, context });
  },

  /**
   * 启用规则
   * @param {Number} ruleId - 规则ID
   * @returns {Promise}
   */
  enableRule(ruleId) {
    return request.post(`/api/v1/schedule/rule/${ruleId}/enable`);
  },

  /**
   * 禁用规则
   * @param {Number} ruleId - 规则ID
   * @returns {Promise}
   */
  disableRule(ruleId) {
    return request.post(`/api/v1/schedule/rule/${ruleId}/disable`);
  },

  /**
   * 获取规则优先级列表
   * @returns {Promise}
   */
  getRulePriority() {
    return request.get('/api/v1/schedule/rule/priority');
  },

  // ==================== 优化算法 ====================

  /**
   * 执行遗传算法优化
   * @param {Object} config - 优化配置
   * @returns {Promise}
   */
  optimizeWithGeneticAlgorithm(config) {
    return request.post('/api/v1/schedule/optimize/genetic', config);
  },

  /**
   * 执行模拟退火优化
   * @param {Object} config - 优化配置
   * @returns {Promise}
   */
  optimizeWithSimulatedAnnealing(config) {
    return request.post('/api/v1/schedule/optimize/annealing', config);
  },

  /**
   * 执行混合算法优化
   * @param {Object} config - 优化配置
   * @returns {Promise}
   */
  optimizeWithHybridAlgorithm(config) {
    return request.post('/api/v1/schedule/optimize/hybrid', config);
  },

  /**
   * 自动选择最优算法并执行优化
   * @param {Object} config - 优化配置
   * @returns {Promise}
   */
  optimizeAuto(config) {
    return request.post('/api/v1/schedule/optimize/auto', config);
  },

  /**
   * 评估优化结果
   * @param {Object} result - 优化结果
   * @returns {Promise}
   */
  evaluateResult(result) {
    return request.post('/api/v1/schedule/optimize/evaluate', result);
  },

  /**
   * 对比不同算法的优化结果
   * @param {Object} config - 优化配置
   * @returns {Promise}
   */
  compareAlgorithms(config) {
    return request.post('/api/v1/schedule/optimize/compare', config);
  },

  /**
   * 验证优化配置
   * @param {Object} config - 优化配置
   * @returns {Promise}
   */
  validateConfig(config) {
    return request.post('/api/v1/schedule/optimize/validate', config);
  },

  /**
   * 获取算法性能统计
   * @param {Object} config - 优化配置
   * @param {Number} runCount - 运行次数
   * @returns {Promise}
   */
  getAlgorithmPerformance(config, runCount) {
    return request.post('/api/v1/schedule/optimize/performance', { config, runCount });
  },

  // ==================== 冲突检测 ====================

  /**
   * 检测排班冲突
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  detectConflicts(planId) {
    return request.post(`/api/v1/schedule/conflict/detect/${planId}`);
  },

  /**
   * 生成冲突报告
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  generateConflictReport(planId) {
    return request.get(`/api/v1/schedule/conflict/report/${planId}`);
  },

  /**
   * 获取冲突统计信息
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  getConflictStatistics(planId) {
    return request.get(`/api/v1/schedule/conflict/statistics/${planId}`);
  },

  /**
   * 自动解决冲突
   * @param {Number} planId - 计划ID
   * @returns {Promise}
   */
  autoResolveConflicts(planId) {
    return request.post(`/api/v1/schedule/conflict/resolve/${planId}`);
  }
};

/**
 * 排班计划状态枚举
 */
export const PlanStatus = {
  DRAFT: 0,        // 草稿
  PENDING: 1,      // 待优化
  OPTIMIZING: 2,   // 优化中
  COMPLETED: 3,    // 已完成
  CONFIRMED: 4,    // 已确认
  CANCELLED: 5,    // 已取消
  FAILED: 6        // 失败
};

/**
 * 优化目标枚举
 */
export const OptimizationGoal = {
  FAIRNESS: 1,       // 公平性优先
  COST: 2,           // 成本优先
  EFFICIENCY: 3,     // 效率优先
  SATISFACTION: 4,   // 满意度优先
  COMPREHENSIVE: 5   // 综合最优
};

/**
 * 算法类型枚举
 */
export const AlgorithmType = {
  GENETIC: 1,           // 遗传算法
  SIMULATED_ANNEALING: 2, // 模拟退火
  HYBRID: 3,            // 混合算法
  AUTO: 4               // 自动选择
};

/**
 * 规则类型枚举
 */
export const RuleType = {
  EMPLOYEE_CONSTRAINT: 1,  // 员工约束
  SHIFT_CONSTRAINT: 2,     // 班次约束
  DATE_CONSTRAINT: 3,      // 日期约束
  CUSTOM: 4               // 自定义规则
};

/**
 * 冲突类型枚举
 */
export const ConflictType = {
  EMPLOYEE_CONSECUTIVE_WORK: 1,    // 员工连续工作天数超标
  EMPLOYEE_INSUFFICIENT_REST: 2,   // 员工休息天数不足
  EMPLOYEE_SHIFT_OVERLOAD: 3,      // 员工班次工作量超标
  SHIFT_COVERAGE_ISSUE: 4,         // 班次人员配置不足
  SHIFT_DUAL_ASSIGNMENT: 5,        // 班次双重分配
  DATE_UNSTAFFED: 6,               // 日期无人排班
  DATE_WEEKEND_OVERTIME: 7,        // 周末排班过多
  DATE_HOLIDAY_OVERTIME: 8,        // 节假日排班过多
  DATE_SHORT_STAFF: 9              // 日期人员短缺
};

/**
 * 冲突严重程度枚举
 */
export const ConflictSeverity = {
  LOW: 1,      // 轻微
  MEDIUM: 2,   // 中等
  HIGH: 3,     // 严重
  CRITICAL: 4  // 紧急
};

export default {
  smartScheduleApi,
  PlanStatus,
  OptimizationGoal,
  AlgorithmType,
  RuleType,
  ConflictType,
  ConflictSeverity
};
