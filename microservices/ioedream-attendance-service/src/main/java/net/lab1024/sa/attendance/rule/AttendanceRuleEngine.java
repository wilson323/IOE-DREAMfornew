package net.lab1024.sa.attendance.rule;

import net.lab1024.sa.attendance.rule.model.AttendanceRuleConfig;
import net.lab1024.sa.attendance.rule.model.EnginePerformanceMetrics;
import net.lab1024.sa.attendance.rule.model.EngineStatus;
import net.lab1024.sa.attendance.rule.model.request.BatchRuleCalculationRequest;
import net.lab1024.sa.attendance.rule.model.request.RuleCalculationRequest;
import net.lab1024.sa.attendance.rule.model.request.RuleConflictCheckRequest;
import net.lab1024.sa.attendance.rule.model.request.RuleHistoryRequest;
import net.lab1024.sa.attendance.rule.model.request.RuleOptimizationRequest;
import net.lab1024.sa.attendance.rule.model.request.RuleQueryParam;
import net.lab1024.sa.attendance.rule.model.request.RuleSimulationRequest;
import net.lab1024.sa.attendance.rule.model.request.RuleStatisticsRequest;
import net.lab1024.sa.attendance.rule.model.result.AttendanceRuleListResult;
import net.lab1024.sa.attendance.rule.model.result.BatchRuleCalculationResult;
import net.lab1024.sa.attendance.rule.model.result.EngineShutdownResult;
import net.lab1024.sa.attendance.rule.model.result.EngineStartupResult;
import net.lab1024.sa.attendance.rule.model.result.RuleCalculationResult;
import net.lab1024.sa.attendance.rule.model.result.RuleConflictCheckResult;
import net.lab1024.sa.attendance.rule.model.result.RuleCreationResult;
import net.lab1024.sa.attendance.rule.model.result.RuleDeletionResult;
import net.lab1024.sa.attendance.rule.model.result.RuleExecutionHistory;
import net.lab1024.sa.attendance.rule.model.result.RuleOptimizationSuggestion;
import net.lab1024.sa.attendance.rule.model.result.RuleSimulationResult;
import net.lab1024.sa.attendance.rule.model.result.RuleStatisticsResult;
import net.lab1024.sa.attendance.rule.model.result.RuleStatusChangeResult;
import net.lab1024.sa.attendance.rule.model.result.RuleUpdateResult;
import net.lab1024.sa.attendance.rule.model.result.RuleValidationResult;
import java.util.concurrent.CompletableFuture;

/**
 * 考勤规则引擎接口
 * <p>
 * 定义考勤规则计算和执行的标准接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AttendanceRuleEngine {

    /**
     * 引擎启动
     *
     * @return 启动结果
     */
    CompletableFuture<EngineStartupResult> startup();

    /**
     * 引擎停止
     *
     * @return 停止结果
     */
    CompletableFuture<EngineShutdownResult> shutdown();

    /**
     * 执行考勤规则计算
     *
     * @param request 规则计算请求
     * @return 规则计算结果
     */
    CompletableFuture<RuleCalculationResult> executeRules(RuleCalculationRequest request);

    /**
     * 批量执行考勤规则计算
     *
     * @param batchRequest 批量规则计算请求
     * @return 批量规则计算结果
     */
    CompletableFuture<BatchRuleCalculationResult> executeBatchRules(BatchRuleCalculationRequest batchRequest);

    /**
     * 获取考勤规则列表
     *
     * @param queryParam 查询参数
     * @return 规则列表
     */
    CompletableFuture<AttendanceRuleListResult> getAttendanceRules(RuleQueryParam queryParam);

    /**
     * 创建考勤规则
     *
     * @param ruleConfig 规则配置
     * @return 创建结果
     */
    CompletableFuture<RuleCreationResult> createAttendanceRule(AttendanceRuleConfig ruleConfig);

    /**
     * 更新考勤规则
     *
     * @param ruleId 规则ID
     * @param ruleConfig 规则配置
     * @return 更新结果
     */
    CompletableFuture<RuleUpdateResult> updateAttendanceRule(String ruleId, AttendanceRuleConfig ruleConfig);

    /**
     * 删除考勤规则
     *
     * @param ruleId 规则ID
     * @return 删除结果
     */
    CompletableFuture<RuleDeletionResult> deleteAttendanceRule(String ruleId);

    /**
     * 启用/禁用考勤规则
     *
     * @param ruleId 规则ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    CompletableFuture<RuleStatusChangeResult> toggleRuleStatus(String ruleId, Boolean enabled);

    /**
     * 验证考勤规则
     *
     * @param ruleConfig 规则配置
     * @return 验证结果
     */
    CompletableFuture<RuleValidationResult> validateRule(AttendanceRuleConfig ruleConfig);

    /**
     * 获取规则执行历史
     *
     * @param historyRequest 历史查询请求
     * @return 规则执行历史
     */
    CompletableFuture<RuleExecutionHistory> getRuleExecutionHistory(RuleHistoryRequest historyRequest);

    /**
     * 获取规则统计信息
     *
     * @param statisticsRequest 统计请求
     * @return 统计信息
     */
    CompletableFuture<RuleStatisticsResult> getRuleStatistics(RuleStatisticsRequest statisticsRequest);

    /**
     * 规则冲突检测
     *
     * @param conflictCheckRequest 冲突检测请求
     * @return 冲突检测结果
     */
    CompletableFuture<RuleConflictCheckResult> checkRuleConflicts(RuleConflictCheckRequest conflictCheckRequest);

    /**
     * 规则优化建议
     *
     * @param optimizationRequest 优化请求
     * @return 优化建议
     */
    CompletableFuture<RuleOptimizationSuggestion> getOptimizationSuggestions(RuleOptimizationRequest optimizationRequest);

    /**
     * 规则模拟执行
     *
     * @param simulationRequest 模拟执行请求
     * @return 模拟执行结果
     */
    CompletableFuture<RuleSimulationResult> simulateRuleExecution(RuleSimulationRequest simulationRequest);

    /**
     * 获取引擎状态
     *
     * @return 引擎状态
     */
    EngineStatus getEngineStatus();

    /**
     * 获取引擎性能指标
     *
     * @return 性能指标
     */
    EnginePerformanceMetrics getPerformanceMetrics();
}
