package net.lab1024.sa.attendance.engine.rule.validation;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.RuleLoader;
import net.lab1024.sa.attendance.engine.RuleValidator;
import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;
import net.lab1024.sa.attendance.engine.rule.model.RuleValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 规则验证服务测试类
 * <p>
 * P2-Batch4重构: 测试RuleValidationService的9个核心方法
 * 遵循Given-When-Then测试模式
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("规则验证服务测试")
@Slf4j
class RuleValidationServiceTest {

    @Mock
    private RuleLoader ruleLoader;

    @Mock
    private RuleValidator ruleValidator;

    private RuleValidationService ruleValidationService;

    @BeforeEach
    void setUp() {
        log.info("[规则验证测试] 初始化测试环境");
        ruleValidationService = new RuleValidationService(
                ruleLoader,
                ruleValidator
        );
    }

    @Test
    @DisplayName("测试验证规则-成功场景")
    void testValidateRule_Success() {
        log.info("[规则验证测试] 测试场景: 验证规则-成功");

        // Given: 准备测试数据
        Long ruleId = 1L;
        RuleValidationResult mockResult = RuleValidationResult.builder()
                .ruleId(ruleId)
                .valid(true)
                .build();

        // Mock依赖行为
        when(ruleValidator.validateRule(ruleId)).thenReturn(mockResult);

        // When: 验证规则
        RuleValidationResult result = ruleValidationService.validateRule(ruleId);

        // Then: 验证结果
        assertNotNull(result, "验证结果不应为空");
        assertEquals(ruleId, result.getRuleId(), "规则ID应该匹配");
        assertTrue(result.isValid(), "规则应该验证通过");

        log.info("[规则验证测试] 测试通过: 规则验证成功, ruleId={}", ruleId);
    }

    @Test
    @DisplayName("测试验证规则-失败场景（规则不存在）")
    void testValidateRule_RuleNotExists() {
        log.info("[规则验证测试] 测试场景: 验证规则-规则不存在");

        // Given: 准备测试数据
        Long ruleId = 999L;
        RuleValidationResult mockResult = RuleValidationResult.builder()
                .ruleId(ruleId)
                .valid(false)
                .errorMessage("规则不存在")
                .build();

        // Mock依赖行为
        when(ruleValidator.validateRule(ruleId)).thenReturn(mockResult);

        // When: 验证规则
        RuleValidationResult result = ruleValidationService.validateRule(ruleId);

        // Then: 验证结果
        assertNotNull(result, "验证结果不应为空");
        assertEquals(ruleId, result.getRuleId(), "规则ID应该匹配");
        assertFalse(result.isValid(), "规则应该验证失败");
        assertNotNull(result.getErrorMessage(), "错误消息不应为空");

        log.info("[规则验证测试] 测试通过: 规则不存在被正确处理, ruleId={}", ruleId);
    }

    @Test
    @DisplayName("测试检查规则适用性-全部检查通过")
    void testIsRuleApplicable_AllChecksPass() {
        log.info("[规则验证测试] 测试场景: 检查规则适用性-全部检查通过");

        // Given: 准备测试数据
        Long ruleId = 1L;
        RuleExecutionContext context = createMockContext(100L, 1L);

        Map<String, Object> ruleConfig = new HashMap<>();
        ruleConfig.put("ruleId", ruleId);
        ruleConfig.put("departmentScope", Arrays.asList(1L, 2L, 3L));
        ruleConfig.put("userAttributes", Map.of("level", "senior"));
        ruleConfig.put("timeScope", Map.of("startTime", "08:00", "endTime", "18:00"));

        // Mock依赖行为
        when(ruleLoader.loadRuleConfig(ruleId)).thenReturn(ruleConfig);

        // When: 检查规则适用性
        boolean isApplicable = ruleValidationService.isRuleApplicable(ruleId, context);

        // Then: 验证结果
        assertTrue(isApplicable, "规则应该适用");

        log.info("[规则验证测试] 测试通过: 规则适用性检查全部通过, ruleId={}", ruleId);
    }

    @Test
    @DisplayName("测试检查规则适用性-部门范围检查失败")
    void testIsRuleApplicable_DepartmentScopeFailed() {
        log.info("[规则验证测试] 测试场景: 检查规则适用性-部门范围检查失败");

        // Given: 准备测试数据
        Long ruleId = 2L;
        RuleExecutionContext context = createMockContext(100L, 99L); // 部门ID不在范围内

        Map<String, Object> ruleConfig = new HashMap<>();
        ruleConfig.put("ruleId", ruleId);
        ruleConfig.put("departmentScope", Arrays.asList(1L, 2L, 3L)); // 部门99不在范围内

        // Mock依赖行为
        when(ruleLoader.loadRuleConfig(ruleId)).thenReturn(ruleConfig);

        // When: 检查规则适用性
        boolean isApplicable = ruleValidationService.isRuleApplicable(ruleId, context);

        // Then: 验证结果
        assertFalse(isApplicable, "规则不应该适用（部门范围不匹配）");

        log.info("[规则验证测试] 测试通过: 部门范围检查失败被正确处理, ruleId={}, deptId={}",
                ruleId, context.getDepartmentId());
    }

    @Test
    @DisplayName("测试检查规则适用性-用户属性检查失败")
    void testIsRuleApplicable_UserAttributesFailed() {
        log.info("[规则验证测试] 测试场景: 检查规则适用性-用户属性检查失败");

        // Given: 准备测试数据
        Long ruleId = 3L;
        RuleExecutionContext context = createMockContext(100L, 1L);
        context.setUserAttributes(Map.of("level", "junior")); // 用户等级不匹配

        Map<String, Object> ruleConfig = new HashMap<>();
        ruleConfig.put("ruleId", ruleId);
        ruleConfig.put("userAttributes", Map.of("level", "senior")); // 要求senior等级

        // Mock依赖行为
        when(ruleLoader.loadRuleConfig(ruleId)).thenReturn(ruleConfig);

        // When: 检查规则适用性
        boolean isApplicable = ruleValidationService.isRuleApplicable(ruleId, context);

        // Then: 验证结果
        assertFalse(isApplicable, "规则不应该适用（用户属性不匹配）");

        log.info("[规则验证测试] 测试通过: 用户属性检查失败被正确处理, ruleId={}", ruleId);
    }

    @Test
    @DisplayName("测试检查规则适用性-时间范围检查失败")
    void testIsRuleApplicable_TimeScopeFailed() {
        log.info("[规则验证测试] 测试场景: 检查规则适用性-时间范围检查失败");

        // Given: 准备测试数据
        Long ruleId = 4L;
        RuleExecutionContext context = createMockContext(100L, 1L);
        context.setEvaluationTime(LocalDateTime.of(2025, 12, 26, 20, 0)); // 晚上20点

        Map<String, Object> ruleConfig = new HashMap<>();
        ruleConfig.put("ruleId", ruleId);
        ruleConfig.put("timeScope", Map.of(
                "startTime", "08:00",
                "endTime", "18:00"  // 规则只在8:00-18:00有效
        ));

        // Mock依赖行为
        when(ruleLoader.loadRuleConfig(ruleId)).thenReturn(ruleConfig);

        // When: 检查规则适用性
        boolean isApplicable = ruleValidationService.isRuleApplicable(ruleId, context);

        // Then: 验证结果
        assertFalse(isApplicable, "规则不应该适用（时间范围不匹配）");

        log.info("[规则验证测试] 测试通过: 时间范围检查失败被正确处理, ruleId={}, time={}",
                ruleId, context.getEvaluationTime());
    }

    @Test
    @DisplayName("测试检查规则适用性-规则过滤器检查失败")
    void testIsRuleApplicable_RuleFiltersFailed() {
        log.info("[规则验证测试] 测试场景: 检查规则适用性-规则过滤器检查失败");

        // Given: 准备测试数据
        Long ruleId = 5L;
        RuleExecutionContext context = createMockContext(100L, 1L);

        Map<String, Object> ruleConfig = new HashMap<>();
        ruleConfig.put("ruleId", ruleId);
        ruleConfig.put("ruleFilters", Map.of(
                "minAge", 18,
                "maxAge", 60
        ));
        context.setUserAttributes(Map.of("age", 17)); // 年龄小于最小值

        // Mock依赖行为
        when(ruleLoader.loadRuleConfig(ruleId)).thenReturn(ruleConfig);

        // When: 检查规则适用性
        boolean isApplicable = ruleValidationService.isRuleApplicable(ruleId, context);

        // Then: 验证结果
        assertFalse(isApplicable, "规则不应该适用（规则过滤器不匹配）");

        log.info("[规则验证测试] 测试通过: 规则过滤器检查失败被正确处理, ruleId={}", ruleId);
    }

    @Test
    @DisplayName("测试检查部门范围-部门在范围内")
    void testCheckDepartmentScope_DepartmentInRange() {
        log.info("[规则验证测试] 测试场景: 检查部门范围-部门在范围内");

        // Given: 准备测试数据
        RuleExecutionContext context = createMockContext(100L, 2L);
        Map<String, Object> ruleConfig = new HashMap<>();
        ruleConfig.put("departmentScope", Arrays.asList(1L, 2L, 3L));

        // When: 检查部门范围
        boolean inScope = ruleValidationService.checkDepartmentScope(ruleConfig, context);

        // Then: 验证结果
        assertTrue(inScope, "部门应该在范围内");

        log.info("[规则验证测试] 测试通过: 部门在范围内检查成功, deptId={}", context.getDepartmentId());
    }

    @Test
    @DisplayName("测试检查用户属性-用户属性匹配")
    void testCheckUserAttributes_AttributesMatch() {
        log.info("[规则验证测试] 测试场景: 检查用户属性-用户属性匹配");

        // Given: 准备测试数据
        RuleExecutionContext context = createMockContext(100L, 1L);
        context.setUserAttributes(Map.of(
                "level", "senior",
                "department", "IT"
        ));

        Map<String, Object> ruleConfig = new HashMap<>();
        ruleConfig.put("userAttributes", Map.of(
                "level", "senior",
                "department", "IT"
        ));

        // When: 检查用户属性
        boolean attributesMatch = ruleValidationService.checkUserAttributes(ruleConfig, context);

        // Then: 验证结果
        assertTrue(attributesMatch, "用户属性应该匹配");

        log.info("[规则验证测试] 测试通过: 用户属性匹配检查成功, userAttributes={}",
                context.getUserAttributes());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建模拟的执行上下文（带参数）
     */
    private RuleExecutionContext createMockContext(Long userId, Long departmentId) {
        return RuleExecutionContext.builder()
                .userId(userId)
                .departmentId(departmentId)
                .attendanceDate(LocalDateTime.now())
                .evaluationTime(LocalDateTime.now())
                .build();
    }
}
