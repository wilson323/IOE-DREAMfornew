package net.lab1024.sa.attendance.engine.rule.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.CompiledAction;
import net.lab1024.sa.attendance.engine.model.CompiledRule;
import net.lab1024.sa.attendance.engine.model.RuleEvaluationResult;
import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;
import net.lab1024.sa.attendance.engine.model.RuleExecutionStatistics;
import net.lab1024.sa.attendance.engine.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManagementService;
import net.lab1024.sa.attendance.engine.rule.compilation.RuleCompilationService;
import net.lab1024.sa.attendance.engine.rule.execution.RuleExecutionService;
import net.lab1024.sa.attendance.engine.rule.model.RuleValidationResult;
import net.lab1024.sa.attendance.engine.rule.statistics.RuleStatisticsService;
import net.lab1024.sa.attendance.engine.rule.validation.RuleValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 考勤规则引擎实现测试类（Facade）
 * <p>
 * P2-Batch4重构: 测试AttendanceRuleEngineImpl Facade的8个核心方法
 * 验证Facade委托调用正确性
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("考勤规则引擎Facade测试")
@Slf4j
class AttendanceRuleEngineImplTest {

    @Mock
    private RuleExecutionService executionService;

    @Mock
    private RuleCompilationService compilationService;

    @Mock
    private RuleValidationService validationService;

    @Mock
    private RuleCacheManagementService cacheService;

    @Mock
    private RuleStatisticsService statisticsService;

    private AttendanceRuleEngine attendanceRuleEngine;

    @BeforeEach
    void setUp() {
        log.info("[规则引擎Facade测试] 初始化测试环境");
        attendanceRuleEngine = new AttendanceRuleEngineImpl(
                executionService,
                compilationService,
                validationService,
                cacheService,
                statisticsService
        );
    }

    @Test
    @DisplayName("测试评估多个规则-成功场景")
    void testEvaluateRules_Success() {
        log.info("[规则引擎Facade测试] 测试场景: 评估多个规则-成功");

        // Given: 准备测试数据
        RuleExecutionContext context = createMockContext();
        List<Long> applicableRules = Arrays.asList(1L, 2L, 3L);

        RuleEvaluationResult result1 = createMockResult(1L, "SUCCESS", 1);
        RuleEvaluationResult result2 = createMockResult(2L, "SUCCESS", 2);
        RuleEvaluationResult result3 = createMockResult(3L, "SUCCESS", 3);
        List<RuleEvaluationResult> mockResults = Arrays.asList(result1, result2, result3);

        // Mock依赖行为
        when(executionService.evaluateRules(anyList(), any())).thenReturn(mockResults);

        // When: 评估多个规则
        List<RuleEvaluationResult> results = attendanceRuleEngine.evaluateRules(context);

        // Then: 验证结果
        assertNotNull(results, "结果列表不应为空");
        assertEquals(3, results.size(), "结果数量应该匹配");

        log.info("[规则引擎Facade测试] 测试通过: 评估多个规则成功, 结果数={}", results.size());
    }

    @Test
    @DisplayName("测试按分类评估规则-成功场景")
    void testEvaluateRulesByCategory_Success() {
        log.info("[规则引擎Facade测试] 测试场景: 按分类评估规则-成功");

        // Given: 准备测试数据
        String ruleCategory = "ATTENDANCE";
        RuleExecutionContext context = createMockContext();

        RuleEvaluationResult result1 = createMockResult(1L, "SUCCESS", 1);
        RuleEvaluationResult result2 = createMockResult(2L, "SUCCESS", 2);
        List<RuleEvaluationResult> mockResults = Arrays.asList(result1, result2);

        // Mock依赖行为
        when(executionService.evaluateRulesByCategory(eq(ruleCategory), any())).thenReturn(mockResults);

        // When: 按分类评估规则
        List<RuleEvaluationResult> results = attendanceRuleEngine.evaluateRulesByCategory(ruleCategory, context);

        // Then: 验证结果
        assertNotNull(results, "结果列表不应为空");
        assertEquals(2, results.size(), "结果数量应该匹配");

        log.info("[规则引擎Facade测试] 测试通过: 按分类评估规则成功, category={}, 结果数={}",
                ruleCategory, results.size());
    }

    @Test
    @DisplayName("测试评估单个规则-成功场景")
    void testEvaluateRule_Success() {
        log.info("[规则引擎Facade测试] 测试场景: 评估单个规则-成功");

        // Given: 准备测试数据
        Long ruleId = 1L;
        RuleExecutionContext context = createMockContext();

        RuleEvaluationResult mockResult = createMockResult(ruleId, "SUCCESS", 1);

        // Mock依赖行为
        when(executionService.evaluateRule(eq(ruleId), any())).thenReturn(mockResult);

        // When: 评估单个规则
        RuleEvaluationResult result = attendanceRuleEngine.evaluateRule(ruleId, context);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(ruleId, result.getRuleId(), "规则ID应该匹配");
        assertEquals("SUCCESS", result.getEvaluationResult(), "评估结果应该匹配");

        log.info("[规则引擎Facade测试] 测试通过: 评估单个规则成功, ruleId={}, result={}",
                ruleId, result.getEvaluationResult());
    }

    @Test
    @DisplayName("测试验证规则-成功场景")
    void testValidateRule_Success() {
        log.info("[规则引擎Facade测试] 测试场景: 验证规则-成功");

        // Given: 准备测试数据
        Long ruleId = 1L;
        RuleValidationResult mockResult = RuleValidationResult.builder()
                .ruleId(ruleId)
                .valid(true)
                .build();

        // Mock依赖行为
        when(validationService.validateRule(ruleId)).thenReturn(mockResult);

        // When: 验证规则
        RuleValidationResult result = attendanceRuleEngine.validateRule(ruleId);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(ruleId, result.getRuleId(), "规则ID应该匹配");
        assertTrue(result.isValid(), "规则应该验证通过");

        log.info("[规则引擎Facade测试] 测试通过: 验证规则成功, ruleId={}", ruleId);
    }

    @Test
    @DisplayName("测试编译规则条件-成功场景")
    void testCompileRuleCondition_Success() {
        log.info("[规则引擎Facade测试] 测试场景: 编译规则条件-成功");

        // Given: 准备测试数据
        String ruleCondition = "userId == 100";
        CompiledRule mockCompiledRule = CompiledRule.builder()
                .ruleId(1L)
                .conditionExpression(ruleCondition)
                .compiled(true)
                .build();

        // Mock依赖行为
        when(compilationService.compileRuleCondition(ruleCondition)).thenReturn(mockCompiledRule);

        // When: 编译规则条件
        CompiledRule compiledRule = attendanceRuleEngine.compileRuleCondition(ruleCondition);

        // Then: 验证结果
        assertNotNull(compiledRule, "编译结果不应为空");
        assertTrue(compiledRule.isCompiled(), "规则应该编译成功");
        assertEquals(ruleCondition, compiledRule.getConditionExpression(), "条件表达式应该匹配");

        log.info("[规则引擎Facade测试] 测试通过: 编译规则条件成功, expression={}", ruleCondition);
    }

    @Test
    @DisplayName("测试编译规则动作-成功场景")
    void testCompileRuleAction_Success() {
        log.info("[规则引擎Facade测试] 测试场景: 编译规则动作-成功");

        // Given: 准备测试数据
        String ruleAction = "APPROVE(userId)";
        CompiledAction mockCompiledAction = CompiledAction.builder()
                .actionExpression(ruleAction)
                .compiled(true)
                .build();

        // Mock依赖行为
        when(compilationService.compileRuleAction(ruleAction)).thenReturn(mockCompiledAction);

        // When: 编译规则动作
        CompiledAction compiledAction = attendanceRuleEngine.compileRuleAction(ruleAction);

        // Then: 验证结果
        assertNotNull(compiledAction, "编译结果不应为空");
        assertTrue(compiledAction.isCompiled(), "动作应该编译成功");
        assertEquals(ruleAction, compiledAction.getActionExpression(), "动作表达式应该匹配");

        log.info("[规则引擎Facade测试] 测试通过: 编译规则动作成功, action={}", ruleAction);
    }

    @Test
    @DisplayName("测试批量评估规则-成功场景")
    void testBatchEvaluateRules_Success() {
        log.info("[规则引擎Facade测试] 测试场景: 批量评估规则-成功");

        // Given: 准备测试数据
        List<RuleExecutionContext> contexts = Arrays.asList(
                createMockContext(100L, 1L),
                createMockContext(101L, 1L),
                createMockContext(102L, 1L)
        );

        RuleEvaluationResult result1 = createMockResult(1L, "SUCCESS", 1);
        RuleEvaluationResult result2 = createMockResult(2L, "SUCCESS", 2);
        RuleEvaluationResult result3 = createMockResult(3L, "SUCCESS", 3);
        List<RuleEvaluationResult> mockResults = Arrays.asList(result1, result2, result3);

        // Mock依赖行为
        when(executionService.batchEvaluateRules(anyList())).thenReturn(mockResults);

        // When: 批量评估规则
        List<RuleEvaluationResult> results = attendanceRuleEngine.batchEvaluateRules(contexts);

        // Then: 验证结果
        assertNotNull(results, "结果列表不应为空");
        assertEquals(3, results.size(), "结果数量应该与输入上下文数量一致");

        log.info("[规则引擎Facade测试] 测试通过: 批量评估规则成功, 上下文数={}, 结果数={}",
                contexts.size(), results.size());
    }

    @Test
    @DisplayName("测试获取执行统计-成功场景")
    void testGetExecutionStatistics_Success() {
        log.info("[规则引擎Facade测试] 测试场景: 获取执行统计-成功");

        // Given: 准备测试数据
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();

        RuleExecutionStatistics mockStatistics = RuleExecutionStatistics.builder()
                .startTime(startTime)
                .endTime(endTime)
                .totalExecutions(100L)
                .successfulExecutions(85L)
                .failedExecutions(15L)
                .statisticsTimestamp(LocalDateTime.now())
                .build();

        // Mock依赖行为
        when(statisticsService.getExecutionStatistics(eq(startTime), eq(endTime))).thenReturn(mockStatistics);

        // When: 获取执行统计
        RuleExecutionStatistics statistics = attendanceRuleEngine.getExecutionStatistics(startTime, endTime);

        // Then: 验证结果
        assertNotNull(statistics, "统计结果不应为空");
        assertEquals(startTime, statistics.getStartTime(), "开始时间应该匹配");
        assertEquals(endTime, statistics.getEndTime(), "结束时间应该匹配");
        assertEquals(100L, statistics.getTotalExecutions(), "总执行次数应该匹配");

        log.info("[规则引擎Facade测试] 测试通过: 获取执行统计成功, totalExecutions={}",
                statistics.getTotalExecutions());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建模拟的执行上下文
     */
    private RuleExecutionContext createMockContext() {
        return createMockContext(100L, 1L);
    }

    /**
     * 创建模拟的执行上下文（带参数）
     */
    private RuleExecutionContext createMockContext(Long userId, Long departmentId) {
        return RuleExecutionContext.builder()
                .userId(userId)
                .departmentId(departmentId)
                .attendanceDate(LocalDateTime.now())
                .build();
    }

    /**
     * 创建模拟的评估结果
     */
    private RuleEvaluationResult createMockResult(Long ruleId, String result, int priority) {
        return RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .evaluationResult(result)
                .rulePriority(priority)
                .evaluatedAt(LocalDateTime.now())
                .build();
    }
}
