package net.lab1024.sa.attendance.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRuleChainOrchestrator;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRulePerformanceMonitor;
import net.lab1024.sa.attendance.engine.optimization.AttendanceRuleVersionManager;
import net.lab1024.sa.attendance.service.AttendanceRuleOptimizationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考勤规则优化服务实现类
 * <p>
 * 整合规则链编排、版本管理、性能监控等优化功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class AttendanceRuleOptimizationServiceImpl implements AttendanceRuleOptimizationService {

    private final AttendanceRuleChainOrchestrator chainOrchestrator;
    private final AttendanceRuleVersionManager versionManager;
    private final AttendanceRulePerformanceMonitor performanceMonitor;

    /**
     * 构造函数注入依赖
     */
    public AttendanceRuleOptimizationServiceImpl(
            AttendanceRuleChainOrchestrator chainOrchestrator,
            AttendanceRuleVersionManager versionManager,
            AttendanceRulePerformanceMonitor performanceMonitor) {
        this.chainOrchestrator = chainOrchestrator;
        this.versionManager = versionManager;
        this.performanceMonitor = performanceMonitor;
    }

    // ==================== 规则链编排相关 ====================

    @Override
    public AttendanceRuleChainOrchestrator.ChainExecutionResult executeRuleChain(
            AttendanceRuleChainOrchestrator.RuleChainConfig chainConfig,
            net.lab1024.sa.attendance.engine.rule.model.RuleExecutionContext context) {
        log.info("[规则优化服务] 执行规则链: chainName={}", chainConfig.getChainName());
        return chainOrchestrator.executeChain(chainConfig, context);
    }

    @Override
    public void registerRuleChain(String chainName, AttendanceRuleChainOrchestrator.RuleChainConfig config) {
        log.info("[规则优化服务] 注册规则链: chainName={}", chainName);
        chainOrchestrator.registerRuleChain(chainName, config);
    }

    @Override
    public AttendanceRuleChainOrchestrator.RuleChainConfig getRuleChain(String chainName) {
        return chainOrchestrator.getRuleChain(chainName);
    }

    @Override
    public void unregisterRuleChain(String chainName) {
        log.info("[规则优化服务] 移除规则链: chainName={}", chainName);
        chainOrchestrator.unregisterRuleChain(chainName);
    }

    @Override
    public List<String> getAllRuleChainNames() {
        return chainOrchestrator.getAllRuleChainNames();
    }

    // ==================== 规则版本管理相关 ====================

    @Override
    public Long createRuleVersion(Long ruleId, AttendanceRuleVersionManager.RuleVersion version) {
        log.info("[规则优化服务] 创建规则版本: ruleId={}", ruleId);
        return versionManager.createVersion(ruleId, version);
    }

    @Override
    public AttendanceRuleVersionManager.VersionPublishResult publishRuleVersion(
            Long ruleId, Long versionId, LocalDateTime publishTime) {
        log.info("[规则优化服务] 发布规则版本: ruleId={}, versionId={}", ruleId, versionId);
        return versionManager.publishVersion(ruleId, versionId, publishTime);
    }

    @Override
    public Boolean rollbackRuleVersion(Long ruleId, Long targetVersionId) {
        log.info("[规则优化服务] 回滚规则版本: ruleId={}, targetVersionId={}", ruleId, targetVersionId);
        return versionManager.rollbackToVersion(ruleId, targetVersionId);
    }

    @Override
    public List<AttendanceRuleVersionManager.RuleVersion> getRuleVersions(Long ruleId) {
        return versionManager.getVersions(ruleId);
    }

    @Override
    public AttendanceRuleVersionManager.RuleVersion getRuleVersion(Long ruleId, Long versionId) {
        return versionManager.getVersion(ruleId, versionId);
    }

    @Override
    public AttendanceRuleVersionManager.RuleVersion getActiveRuleVersion(Long ruleId) {
        return versionManager.getActiveVersion(ruleId);
    }

    @Override
    public Map<String, Object> compareRuleVersions(Long ruleId, Long versionId1, Long versionId2) {
        log.info("[规则优化服务] 比较规则版本: ruleId={}, versionId1={}, versionId2={}",
                ruleId, versionId1, versionId2);
        return versionManager.compareVersions(ruleId, versionId1, versionId2);
    }

    @Override
    public Integer archiveOldRuleVersions(Long ruleId, LocalDateTime beforeTime) {
        log.info("[规则优化服务] 归档旧版本: ruleId={}, beforeTime={}", ruleId, beforeTime);
        return versionManager.archiveOldVersions(ruleId, beforeTime);
    }

    @Override
    public Boolean deleteRuleVersion(Long ruleId, Long versionId) {
        log.info("[规则优化服务] 删除规则版本: ruleId={}, versionId={}", ruleId, versionId);
        return versionManager.deleteVersion(ruleId, versionId);
    }

    // ==================== 规则性能监控相关 ====================

    @Override
    public void recordRuleExecution(Long ruleId, String ruleName, Long executionTime, Boolean success) {
        log.debug("[规则优化服务] 记录规则执行: ruleId={}, executionTime={}ms", ruleId, executionTime);
        performanceMonitor.recordExecution(ruleId, ruleName, executionTime, success);
    }

    @Override
    public AttendanceRulePerformanceMonitor.PerformanceMetrics getRulePerformanceMetrics(Long ruleId, String ruleName) {
        return performanceMonitor.getPerformanceMetrics(ruleId, ruleName);
    }

    @Override
    public Map<Long, AttendanceRulePerformanceMonitor.PerformanceMetrics> getAllRulePerformanceMetrics() {
        return performanceMonitor.getAllPerformanceMetrics();
    }

    @Override
    public List<AttendanceRulePerformanceMonitor.PerformanceAlert> getPerformanceAlerts(
            Long ruleId, String severity, LocalDateTime startTime, LocalDateTime endTime) {
        return performanceMonitor.getAlerts(ruleId, severity, startTime, endTime);
    }

    @Override
    public void clearExecutionRecords(LocalDateTime beforeTime) {
        log.info("[规则优化服务] 清除执行记录: beforeTime={}", beforeTime);
        performanceMonitor.clearExecutionRecords(beforeTime);
    }

    @Override
    public void resetRuleStatistics(Long ruleId) {
        log.info("[规则优化服务] 重置规则统计: ruleId={}", ruleId);
        performanceMonitor.resetRuleStatistics(ruleId);
    }

    @Override
    public List<String> getRuleOptimizationSuggestions(Long ruleId) {
        log.info("[规则优化服务] 获取优化建议: ruleId={}", ruleId);
        return performanceMonitor.getOptimizationSuggestions(ruleId);
    }
}
