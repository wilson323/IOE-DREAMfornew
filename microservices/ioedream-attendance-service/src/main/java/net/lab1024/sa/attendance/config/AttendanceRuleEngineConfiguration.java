package net.lab1024.sa.attendance.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.rule.evaluator.RuleEvaluatorFactory;
import net.lab1024.sa.attendance.engine.rule.executor.RuleExecutor;
import net.lab1024.sa.attendance.engine.rule.loader.RuleLoader;
import net.lab1024.sa.attendance.engine.rule.validator.RuleValidator;
import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManager;
import net.lab1024.sa.attendance.engine.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManagementService;
import net.lab1024.sa.attendance.engine.rule.compilation.RuleCompilationService;
import net.lab1024.sa.attendance.engine.rule.execution.RuleExecutionService;
import net.lab1024.sa.attendance.engine.rule.impl.AttendanceRuleEngineImpl;
import net.lab1024.sa.attendance.engine.rule.statistics.RuleStatisticsService;
import net.lab1024.sa.attendance.engine.rule.validation.RuleValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 考勤规则引擎配置类
 * <p>
 * P2-Batch4重构: 注册5个专业服务 + 1个Facade
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Configuration
public class AttendanceRuleEngineConfiguration {

    /**
     * 注册规则执行服务
     */
    @Bean
    public RuleExecutionService ruleExecutionService(
            RuleLoader ruleLoader,
            RuleValidator ruleValidator,
            RuleCacheManager cacheManager,
            RuleEvaluatorFactory evaluatorFactory,
            RuleExecutor ruleExecutor) {
        log.info("[规则引擎配置] 注册规则执行服务为Spring Bean");
        return new RuleExecutionService(
                ruleLoader, ruleValidator, cacheManager,
                evaluatorFactory, ruleExecutor
        );
    }

    /**
     * 注册规则编译服务
     */
    @Bean
    public RuleCompilationService ruleCompilationService() {
        log.info("[规则引擎配置] 注册规则编译服务为Spring Bean");
        return new RuleCompilationService();
    }

    /**
     * 注册规则验证服务
     */
    @Bean
    public RuleValidationService ruleValidationService(
            RuleLoader ruleLoader,
            RuleValidator ruleValidator) {
        log.info("[规则引擎配置] 注册规则验证服务为Spring Bean");
        return new RuleValidationService(ruleLoader, ruleValidator);
    }

    /**
     * 注册规则缓存管理服务
     */
    @Bean
    public RuleCacheManagementService ruleCacheManagementService(RuleCacheManager cacheManager) {
        log.info("[规则引擎配置] 注册规则缓存管理服务为Spring Bean");
        return new RuleCacheManagementService(cacheManager);
    }

    /**
     * 注册规则统计服务
     */
    @Bean
    public RuleStatisticsService ruleStatisticsService() {
        log.info("[规则引擎配置] 注册规则统计服务为Spring Bean");
        return new RuleStatisticsService();
    }

    /**
     * 注册考勤规则引擎Facade
     */
    @Bean
    public AttendanceRuleEngine attendanceRuleEngine(
            RuleExecutionService ruleExecutionService,
            RuleCompilationService ruleCompilationService,
            RuleValidationService ruleValidationService,
            RuleCacheManagementService ruleCacheManagementService,
            RuleStatisticsService ruleStatisticsService) {
        log.info("[规则引擎配置] 注册考勤规则引擎Facade为Spring Bean");
        log.info("[规则引擎配置] 5个专业服务已注入到Facade");

        return new AttendanceRuleEngineImpl(
                ruleExecutionService,
                ruleCompilationService,
                ruleValidationService,
                ruleCacheManagementService,
                ruleStatisticsService
        );
    }
}
