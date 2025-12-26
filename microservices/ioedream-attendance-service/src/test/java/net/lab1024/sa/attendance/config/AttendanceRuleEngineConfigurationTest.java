package net.lab1024.sa.attendance.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.RuleEvaluatorFactory;
import net.lab1024.sa.attendance.engine.RuleExecutor;
import net.lab1024.sa.attendance.engine.RuleLoader;
import net.lab1024.sa.attendance.engine.RuleValidator;
import net.lab1024.sa.attendance.engine.RuleEvaluator;
import net.lab1024.sa.attendance.engine.cache.RuleCacheManager;
import net.lab1024.sa.attendance.engine.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManagementService;
import net.lab1024.sa.attendance.engine.rule.compilation.RuleCompilationService;
import net.lab1024.sa.attendance.engine.rule.execution.RuleExecutionService;
import net.lab1024.sa.attendance.engine.rule.impl.AttendanceRuleEngineImpl;
import net.lab1024.sa.attendance.engine.rule.statistics.RuleStatisticsService;
import net.lab1024.sa.attendance.engine.rule.validation.RuleValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 考勤规则引擎配置测试类
 * <p>
 * P2-Batch4重构: 测试AttendanceRuleEngineConfiguration的6个Bean注册
 * 验证Spring Boot配置类正确性
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@SpringBootTest
@DisplayName("考勤规则引擎配置测试")
@Slf4j
class AttendanceRuleEngineConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        log.info("[规则引擎配置测试] 初始化测试环境");
    }

    @Test
    @DisplayName("测试规则执行服务Bean注册")
    void testRuleExecutionServiceBeanRegistered() {
        log.info("[规则引擎配置测试] 测试场景: 规则执行服务Bean注册");

        // When: 从Spring容器获取Bean
        RuleExecutionService ruleExecutionService = applicationContext.getBean(RuleExecutionService.class);

        // Then: 验证Bean不为空
        assertNotNull(ruleExecutionService, "规则执行服务Bean应该已注册");

        log.info("[规则引擎配置测试] 测试通过: 规则执行服务Bean已正确注册");
    }

    @Test
    @DisplayName("测试规则编译服务Bean注册")
    void testRuleCompilationServiceBeanRegistered() {
        log.info("[规则引擎配置测试] 测试场景: 规则编译服务Bean注册");

        // When: 从Spring容器获取Bean
        RuleCompilationService ruleCompilationService = applicationContext.getBean(RuleCompilationService.class);

        // Then: 验证Bean不为空
        assertNotNull(ruleCompilationService, "规则编译服务Bean应该已注册");

        log.info("[规则引擎配置测试] 测试通过: 规则编译服务Bean已正确注册");
    }

    @Test
    @DisplayName("测试规则验证服务Bean注册")
    void testRuleValidationServiceBeanRegistered() {
        log.info("[规则引擎配置测试] 测试场景: 规则验证服务Bean注册");

        // When: 从Spring容器获取Bean
        RuleValidationService ruleValidationService = applicationContext.getBean(RuleValidationService.class);

        // Then: 验证Bean不为空
        assertNotNull(ruleValidationService, "规则验证服务Bean应该已注册");

        log.info("[规则引擎配置测试] 测试通过: 规则验证服务Bean已正确注册");
    }

    @Test
    @DisplayName("测试规则缓存管理服务Bean注册")
    void testRuleCacheManagementServiceBeanRegistered() {
        log.info("[规则引擎配置测试] 测试场景: 规则缓存管理服务Bean注册");

        // When: 从Spring容器获取Bean
        RuleCacheManagementService ruleCacheManagementService = applicationContext.getBean(RuleCacheManagementService.class);

        // Then: 验证Bean不为空
        assertNotNull(ruleCacheManagementService, "规则缓存管理服务Bean应该已注册");

        log.info("[规则引擎配置测试] 测试通过: 规则缓存管理服务Bean已正确注册");
    }

    @Test
    @DisplayName("测试规则统计服务Bean注册")
    void testRuleStatisticsServiceBeanRegistered() {
        log.info("[规则引擎配置测试] 测试场景: 规则统计服务Bean注册");

        // When: 从Spring容器获取Bean
        RuleStatisticsService ruleStatisticsService = applicationContext.getBean(RuleStatisticsService.class);

        // Then: 验证Bean不为空
        assertNotNull(ruleStatisticsService, "规则统计服务Bean应该已注册");

        log.info("[规则引擎配置测试] 测试通过: 规则统计服务Bean已正确注册");
    }

    @Test
    @DisplayName("测试考勤规则引擎Facade Bean注册")
    void testAttendanceRuleEngineFacadeBeanRegistered() {
        log.info("[规则引擎配置测试] 测试场景: 考勤规则引擎Facade Bean注册");

        // When: 从Spring容器获取Bean
        AttendanceRuleEngine attendanceRuleEngine = applicationContext.getBean(AttendanceRuleEngine.class);

        // Then: 验证Bean不为空且类型正确
        assertNotNull(attendanceRuleEngine, "考勤规则引擎Facade Bean应该已注册");
        assertTrue(attendanceRuleEngine instanceof AttendanceRuleEngineImpl,
                "Facade Bean应该是AttendanceRuleEngineImpl类型");

        log.info("[规则引擎配置测试] 测试通过: 考勤规则引擎Facade Bean已正确注册, type={}",
                attendanceRuleEngine.getClass().getSimpleName());
    }
}
