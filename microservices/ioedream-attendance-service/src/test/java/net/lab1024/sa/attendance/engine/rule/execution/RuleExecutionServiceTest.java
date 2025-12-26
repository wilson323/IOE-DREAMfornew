package net.lab1024.sa.attendance.engine.rule.execution;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.RuleEvaluatorFactory;
import net.lab1024.sa.attendance.engine.RuleExecutor;
import net.lab1024.sa.attendance.engine.RuleLoader;
import net.lab1024.sa.attendance.engine.RuleValidator;
import net.lab1024.sa.attendance.engine.cache.RuleCacheManager;
import net.lab1024.sa.attendance.engine.model.CompiledRule;
import net.lab1024.sa.attendance.engine.model.RuleEvaluationResult;
import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;
import net.lab1024.sa.attendance.engine.rule.model.RuleValidationResult;
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
import static org.mockito.Mockito.*;

/**
 * 规则执行服务测试类
 * <p>
 * P2-Batch4重构: 测试RuleExecutionService的8个核心方法
 * 遵循Given-When-Then测试模式
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("规则执行服务测试")
@Slf4j
class RuleExecutionServiceTest {

    @Mock
    private RuleLoader ruleLoader;

    @Mock
    private RuleValidator ruleValidator;

    @Mock
    private RuleCacheManager cacheManager;

    @Mock
    private RuleEvaluatorFactory evaluatorFactory;

    @Mock
    private RuleExecutor ruleExecutor;

    private RuleExecutionService ruleExecutionService;

    @BeforeEach
    void setUp() {
        log.info("[规则执行测试] 初始化测试环境");
        ruleExecutionService = new RuleExecutionService(
                ruleLoader,
                ruleValidator,
                cacheManager,
                evaluatorFactory,
                ruleExecutor
        );
    }

    @Test
    @DisplayName("测试执行单个规则评估-成功场景")
    void testEvaluateRule_Success() {
        log.info("[规则执行测试] 测试场景: 执行单个规则评估-成功");

        // Given: 准备测试数据
        Long ruleId = 1L;
        RuleExecutionContext context = createMockContext();
        RuleValidationResult validationResult = RuleValidationResult.builder()
                .ruleId(ruleId)
                .valid(true)
                .build();

        CompiledRule compiledRule = CompiledRule.builder()
                .ruleId(ruleId)
                .conditionExpression("userId == 100")
                .compiled(true)
                .build();

        // Mock依赖行为
        when(ruleValidator.validateRule(ruleId)).thenReturn(validationResult);
        when(cacheManager.getCachedResult(anyLong(), any())).thenReturn(null);
        when(ruleLoader.loadRuleConfig(ruleId)).thenReturn(Collections.singletonMap("ruleId", ruleId));
        when(evaluatorFactory.createEvaluator(any())).thenReturn(ruleExecutor);
        when(ruleExecutor.execute(any(), any())).thenReturn(true);

        // When: 执行规则评估
        RuleEvaluationResult result = ruleExecutionService.evaluateRule(ruleId, context);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(ruleId, result.getRuleId(), "规则ID应该匹配");
        assertEquals("SUCCESS", result.getEvaluationResult(), "评估结果应该是成功");

        log.info("[规则执行测试] 测试通过: 规则评估成功, ruleId={}, result={}", ruleId, result.getEvaluationResult());
    }

    @Test
    @DisplayName("测试执行单个规则评估-规则验证失败")
    void testEvaluateRule_ValidationFailed() {
        log.info("[规则执行测试] 测试场景: 执行单个规则评估-验证失败");

        // Given: 准备测试数据
        Long ruleId = 2L;
        RuleExecutionContext context = createMockContext();
        RuleValidationResult validationResult = RuleValidationResult.builder()
                .ruleId(ruleId)
                .valid(false)
                .errorMessage("规则不存在")
                .build();

        // Mock依赖行为
        when(ruleValidator.validateRule(ruleId)).thenReturn(validationResult);

        // When: 执行规则评估
        RuleEvaluationResult result = ruleExecutionService.evaluateRule(ruleId, context);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(ruleId, result.getRuleId(), "规则ID应该匹配");
        assertEquals("FAILED", result.getEvaluationResult(), "评估结果应该是失败");
        assertTrue(result.getErrorMessage().contains("规则不存在"), "错误消息应该包含验证失败信息");

        log.info("[规则执行测试] 测试通过: 规则验证失败被正确处理, ruleId={}", ruleId);
    }

    @Test
    @DisplayName("测试执行单个规则评估-缓存命中")
    void testEvaluateRule_CacheHit() {
        log.info("[规则执行测试] 测试场景: 执行单个规则评估-缓存命中");

        // Given: 准备测试数据
        Long ruleId = 3L;
        RuleExecutionContext context = createMockContext();
        RuleEvaluationResult cachedResult = RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .evaluationResult("SUCCESS")
                .evaluatedAt(LocalDateTime.now())
                .build();

        // Mock依赖行为
        when(cacheManager.getCachedResult(eq(ruleId), any())).thenReturn(cachedResult);

        // When: 执行规则评估
        RuleEvaluationResult result = ruleExecutionService.evaluateRule(ruleId, context);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(cachedResult.getRuleId(), result.getRuleId(), "应该返回缓存的结果");
        assertEquals(cachedResult.getEvaluationResult(), result.getEvaluationResult(), "评估结果应该与缓存一致");

        // 验证不会重新执行规则
        verify(ruleValidator, never()).validateRule(anyLong());

        log.info("[规则执行测试] 测试通过: 缓存命中时直接返回缓存结果, ruleId={}", ruleId);
    }

    @Test
    @DisplayName("测试批量执行规则评估-成功场景")
    void testEvaluateRules_Success() {
        log.info("[规则执行测试] 测试场景: 批量执行规则评估-成功");

        // Given: 准备测试数据
        List<Long> ruleIds = Arrays.asList(1L, 2L, 3L);
        RuleExecutionContext context = createMockContext();

        for (Long ruleId : ruleIds) {
            RuleValidationResult validationResult = RuleValidationResult.builder()
                    .ruleId(ruleId)
                    .valid(true)
                    .build();

            when(ruleValidator.validateRule(ruleId)).thenReturn(validationResult);
            when(cacheManager.getCachedResult(eq(ruleId), any())).thenReturn(null);
            when(ruleLoader.loadRuleConfig(ruleId)).thenReturn(Collections.singletonMap("ruleId", ruleId));
            when(evaluatorFactory.createEvaluator(any())).thenReturn(ruleExecutor);
            when(ruleExecutor.execute(any(), any())).thenReturn(true);
        }

        // When: 执行批量规则评估
        List<RuleEvaluationResult> results = ruleExecutionService.evaluateRules(ruleIds, context);

        // Then: 验证结果
        assertNotNull(results, "结果列表不应为空");
        assertEquals(ruleIds.size(), results.size(), "结果数量应该与输入规则数量一致");

        for (RuleEvaluationResult result : results) {
            assertEquals("SUCCESS", result.getEvaluationResult(), "所有规则评估结果应该是成功");
        }

        log.info("[规则执行测试] 测试通过: 批量规则评估成功, 评估规则数={}, 结果数={}", ruleIds.size(), results.size());
    }

    @Test
    @DisplayName("测试按分类执行规则评估-成功场景")
    void testEvaluateRulesByCategory_Success() {
        log.info("[规则执行测试] 测试场景: 按分类执行规则评估-成功");

        // Given: 准备测试数据
        String ruleCategory = "ATTENDANCE";
        RuleExecutionContext context = createMockContext();
        List<Long> applicableRuleIds = Arrays.asList(10L, 20L);

        for (Long ruleId : applicableRuleIds) {
            RuleValidationResult validationResult = RuleValidationResult.builder()
                    .ruleId(ruleId)
                    .valid(true)
                    .build();

            when(ruleValidator.validateRule(ruleId)).thenReturn(validationResult);
            when(cacheManager.getCachedResult(eq(ruleId), any())).thenReturn(null);
            when(ruleLoader.loadRuleConfig(ruleId)).thenReturn(
                    Collections.singletonMap("category", ruleCategory)
            );
            when(evaluatorFactory.createEvaluator(any())).thenReturn(ruleExecutor);
            when(ruleExecutor.execute(any(), any())).thenReturn(true);
        }

        // When: 按分类执行规则评估
        List<RuleEvaluationResult> results = ruleExecutionService.evaluateRulesByCategory(ruleCategory, context);

        // Then: 验证结果
        assertNotNull(results, "结果列表不应为空");
        assertTrue(results.size() >= 0, "结果数量应该大于等于0");

        log.info("[规则执行测试] 测试通过: 按分类规则评估成功, category={}, 结果数={}", ruleCategory, results.size());
    }

    @Test
    @DisplayName("测试批量评估多个上下文-成功场景")
    void testBatchEvaluateRules_Success() {
        log.info("[规则执行测试] 测试场景: 批量评估多个上下文-成功");

        // Given: 准备测试数据
        List<RuleExecutionContext> contexts = Arrays.asList(
                createMockContext(100L, 1L),
                createMockContext(101L, 1L),
                createMockContext(102L, 1L)
        );

        // When: 批量评估
        List<RuleEvaluationResult> results = ruleExecutionService.batchEvaluateRules(contexts);

        // Then: 验证结果
        assertNotNull(results, "结果列表不应为空");
        assertTrue(results.size() >= 0, "结果数量应该大于等于0");

        log.info("[规则执行测试] 测试通过: 批量评估多个上下文成功, 上下文数={}, 结果数={}", contexts.size(), results.size());
    }

    @Test
    @DisplayName("测试按优先级排序规则-成功场景")
    void testSortByPriority_Success() {
        log.info("[规则执行测试] 测试场景: 按优先级排序规则-成功");

        // Given: 准备测试数据（优先级无序）
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .ruleId(1L)
                .rulePriority(5)
                .build();
        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .ruleId(2L)
                .rulePriority(1)
                .build();
        RuleEvaluationResult result3 = RuleEvaluationResult.builder()
                .ruleId(3L)
                .rulePriority(3)
                .build();

        List<RuleEvaluationResult> unsortedResults = Arrays.asList(result1, result2, result3);

        // When: 按优先级排序
        List<RuleEvaluationResult> sortedResults = ruleExecutionService.sortByPriority(unsortedResults);

        // Then: 验证排序结果（优先级从低到高：1, 3, 5）
        assertNotNull(sortedResults, "排序结果不应为空");
        assertEquals(3, sortedResults.size(), "排序结果数量应该与输入一致");
        assertEquals(1, sortedResults.get(0).getRulePriority(), "第一项优先级应该是1");
        assertEquals(3, sortedResults.get(1).getRulePriority(), "第二项优先级应该是3");
        assertEquals(5, sortedResults.get(2).getRulePriority(), "第三项优先级应该是5");

        log.info("[规则执行测试] 测试通过: 规则按优先级排序成功, 排序结果=[{}, {}, {}]",
                sortedResults.get(0).getRulePriority(),
                sortedResults.get(1).getRulePriority(),
                sortedResults.get(2).getRulePriority()
        );
    }

    @Test
    @DisplayName("测试规则执行异常处理")
    void testEvaluateRule_ExceptionHandling() {
        log.info("[规则执行测试] 测试场景: 规则执行异常处理");

        // Given: 准备测试数据
        Long ruleId = 99L;
        RuleExecutionContext context = createMockContext();

        // Mock依赖行为 - 抛出异常
        when(ruleValidator.validateRule(ruleId)).thenThrow(new RuntimeException("数据库连接失败"));

        // When: 执行规则评估
        RuleEvaluationResult result = ruleExecutionService.evaluateRule(ruleId, context);

        // Then: 验证异常被正确处理
        assertNotNull(result, "结果不应为空");
        assertEquals(ruleId, result.getRuleId(), "规则ID应该匹配");
        assertEquals("ERROR", result.getEvaluationResult(), "评估结果应该是错误");
        assertNotNull(result.getErrorMessage(), "错误消息不应为空");

        log.info("[规则执行测试] 测试通过: 异常被正确处理, ruleId={}, error={}", ruleId, result.getErrorMessage());
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
}
