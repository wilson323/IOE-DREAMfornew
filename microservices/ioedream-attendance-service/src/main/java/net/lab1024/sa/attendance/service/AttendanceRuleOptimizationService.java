package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.engine.optimization.AttendanceRuleChainOrchestrator;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRuleChainOrchestrator.ChainExecutionResult;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRuleChainOrchestrator.RuleChainConfig;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRulePerformanceMonitor;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRulePerformanceMonitor.PerformanceAlert;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRulePerformanceMonitor.PerformanceMetrics;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRuleVersionManager;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRuleVersionManager.RuleVersion;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRuleVersionManager.VersionPublishResult;
import net.lab1024.sa.attendance.engine.rule.model.RuleExecutionContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考勤规则优化服务接口
 * <p>
 * 提供规则引擎优化功能，包括规则链编排、版本管理、性能监控等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
public interface AttendanceRuleOptimizationService {

    // ==================== 规则链编排相关 ====================

    /**
     * 执行规则链
     *
     * @param chainConfig 规则链配置
     * @param context 规则执行上下文
     * @return 规则链执行结果
     */
    ChainExecutionResult executeRuleChain(RuleChainConfig chainConfig, RuleExecutionContext context);

    /**
     * 注册规则链
     *
     * @param chainName 规则链名称
     * @param config 规则链配置
     */
    void registerRuleChain(String chainName, RuleChainConfig config);

    /**
     * 获取规则链配置
     *
     * @param chainName 规则链名称
     * @return 规则链配置
     */
    RuleChainConfig getRuleChain(String chainName);

    /**
     * 移除规则链
     *
     * @param chainName 规则链名称
     */
    void unregisterRuleChain(String chainName);

    /**
     * 获取所有规则链名称
     *
     * @return 规则链名称列表
     */
    List<String> getAllRuleChainNames();

    // ==================== 规则版本管理相关 ====================

    /**
     * 创建新规则版本
     *
     * @param ruleId 规则ID
     * @param version 版本信息
     * @return 版本ID
     */
    Long createRuleVersion(Long ruleId, RuleVersion version);

    /**
     * 发布规则版本
     *
     * @param ruleId 规则ID
     * @param versionId 版本ID
     * @param publishTime 生效时间
     * @return 发布结果
     */
    VersionPublishResult publishRuleVersion(Long ruleId, Long versionId, LocalDateTime publishTime);

    /**
     * 回滚规则版本
     *
     * @param ruleId 规则ID
     * @param targetVersionId 目标版本ID
     * @return 是否成功
     */
    Boolean rollbackRuleVersion(Long ruleId, Long targetVersionId);

    /**
     * 获取规则的所有版本
     *
     * @param ruleId 规则ID
     * @return 版本列表
     */
    List<RuleVersion> getRuleVersions(Long ruleId);

    /**
     * 获取指定规则版本
     *
     * @param ruleId 规则ID
     * @param versionId 版本ID
     * @return 版本信息
     */
    RuleVersion getRuleVersion(Long ruleId, Long versionId);

    /**
     * 获取当前生效版本
     *
     * @param ruleId 规则ID
     * @return 生效版本
     */
    RuleVersion getActiveRuleVersion(Long ruleId);

    /**
     * 比较两个规则版本的差异
     *
     * @param ruleId 规则ID
     * @param versionId1 版本1
     * @param versionId2 版本2
     * @return 差异描述
     */
    Map<String, Object> compareRuleVersions(Long ruleId, Long versionId1, Long versionId2);

    /**
     * 归档旧版本
     *
     * @param ruleId 规则ID
     * @param beforeTime 在此时间之前的版本
     * @return 归档的版本数量
     */
    Integer archiveOldRuleVersions(Long ruleId, LocalDateTime beforeTime);

    /**
     * 删除规则版本
     *
     * @param ruleId 规则ID
     * @param versionId 版本ID
     * @return 是否成功
     */
    Boolean deleteRuleVersion(Long ruleId, Long versionId);

    // ==================== 规则性能监控相关 ====================

    /**
     * 记录规则执行
     *
     * @param ruleId 规则ID
     * @param ruleName 规则名称
     * @param executionTime 执行时间（毫秒）
     * @param success 是否成功
     */
    void recordRuleExecution(Long ruleId, String ruleName, Long executionTime, Boolean success);

    /**
     * 获取规则性能指标
     *
     * @param ruleId 规则ID
     * @param ruleName 规则名称
     * @return 性能指标
     */
    PerformanceMetrics getRulePerformanceMetrics(Long ruleId, String ruleName);

    /**
     * 获取所有规则性能指标
     *
     * @return 性能指标映射
     */
    Map<Long, PerformanceMetrics> getAllRulePerformanceMetrics();

    /**
     * 获取性能告警列表
     *
     * @param ruleId 规则ID（可选）
     * @param severity 告警级别（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 告警列表
     */
    List<PerformanceAlert> getPerformanceAlerts(Long ruleId, String severity, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清除执行记录
     *
     * @param beforeTime 在此时间之前的记录
     */
    void clearExecutionRecords(LocalDateTime beforeTime);

    /**
     * 重置规则统计
     *
     * @param ruleId 规则ID
     */
    void resetRuleStatistics(Long ruleId);

    /**
     * 获取性能优化建议
     *
     * @param ruleId 规则ID
     * @return 优化建议列表
     */
    List<String> getRuleOptimizationSuggestions(Long ruleId);
}
