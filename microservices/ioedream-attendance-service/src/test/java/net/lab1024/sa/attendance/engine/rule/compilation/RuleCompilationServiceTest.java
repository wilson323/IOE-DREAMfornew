package net.lab1024.sa.attendance.engine.rule.compilation;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.CompiledRule;
import net.lab1024.sa.attendance.engine.rule.model.CompiledAction;
import net.lab1024.sa.attendance.engine.rule.model.CompiledActionObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 规则编译服务测试类
 * <p>
 * P2-Batch4重构: 测试RuleCompilationService的6个核心方法
 * 遵循Given-When-Then测试模式
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@DisplayName("规则编译服务测试")
@Slf4j
class RuleCompilationServiceTest {

    private RuleCompilationService ruleCompilationService;

    @BeforeEach
    void setUp() {
        log.info("[规则编译测试] 初始化测试环境");
        ruleCompilationService = new RuleCompilationService();
    }

    @Test
    @DisplayName("测试编译规则条件-简单表达式（==操作符）")
    void testCompileRuleCondition_SimpleExpression() {
        log.info("[规则编译测试] 测试场景: 编译简单条件表达式");

        // Given: 准备测试数据
        String ruleCondition = "userId == 100";

        // When: 编译规则条件
        CompiledRule compiledRule = ruleCompilationService.compileRuleCondition(ruleCondition);

        // Then: 验证编译结果
        assertNotNull(compiledRule, "编译结果不应为空");
        assertTrue(compiledRule.isCompiled(), "规则应该编译成功");
        assertEquals(ruleCondition, compiledRule.getConditionExpression(), "条件表达式应该匹配");
        assertNotNull(compiledRule.getCompiledCondition(), "编译后的条件对象不应为空");

        // 验证解析的操作符和操作数
        RuleCompilationService.CompiledCondition condition = compiledRule.getCompiledCondition();
        assertEquals("==", condition.getOperator(), "操作符应该是==");
        assertEquals("userId", condition.getLeftOperand(), "左操作数应该是userId");
        assertEquals("100", condition.getRightOperand(), "右操作数应该是100");

        log.info("[规则编译测试] 测试通过: 简单条件表达式编译成功, expression={}, operator={}",
                ruleCondition, condition.getOperator());
    }

    @Test
    @DisplayName("测试编译规则条件-复杂表达式（&&和||操作符）")
    void testCompileRuleCondition_ComplexExpression() {
        log.info("[规则编译测试] 测试场景: 编译复杂条件表达式");

        // Given: 准备测试数据
        String ruleCondition = "userId == 100 && departmentId == 1";

        // When: 编译规则条件
        CompiledRule compiledRule = ruleCompilationService.compileRuleCondition(ruleCondition);

        // Then: 验证编译结果
        assertNotNull(compiledRule, "编译结果不应为空");
        assertTrue(compiledRule.isCompiled(), "规则应该编译成功");
        assertEquals(ruleCondition, compiledRule.getConditionExpression(), "条件表达式应该匹配");

        RuleCompilationService.CompiledCondition condition = compiledRule.getCompiledCondition();
        assertNotNull(condition, "编译后的条件对象不应为空");

        log.info("[规则编译测试] 测试通过: 复杂条件表达式编译成功, expression={}", ruleCondition);
    }

    @Test
    @DisplayName("测试编译规则条件-失败场景（空表达式）")
    void testCompileRuleCondition_EmptyExpression() {
        log.info("[规则编译测试] 测试场景: 编译空条件表达式");

        // Given: 准备测试数据
        String ruleCondition = "";

        // When: 编译规则条件
        CompiledRule compiledRule = ruleCompilationService.compileRuleCondition(ruleCondition);

        // Then: 验证编译失败
        assertNotNull(compiledRule, "编译结果不应为空");
        assertFalse(compiledRule.isCompiled(), "规则应该编译失败");
        assertNotNull(compiledRule.getCompilationError(), "错误消息不应为空");
        assertTrue(compiledRule.getCompilationError().contains("规则条件表达式不能为空"),
                "错误消息应该提示表达式为空");

        log.info("[规则编译测试] 测试通过: 空表达式编译失败被正确处理");
    }

    @Test
    @DisplayName("测试编译规则动作-成功场景")
    void testCompileRuleAction_Success() {
        log.info("[规则编译测试] 测试场景: 编译规则动作-成功");

        // Given: 准备测试数据
        String ruleAction = "APPROVE(userId, leaveType)";

        // When: 编译规则动作
        CompiledAction compiledAction = ruleCompilationService.compileRuleAction(ruleAction);

        // Then: 验证编译结果
        assertNotNull(compiledAction, "编译结果不应为空");
        assertTrue(compiledAction.isCompiled(), "动作应该编译成功");
        assertEquals(ruleAction, compiledAction.getActionExpression(), "动作表达式应该匹配");
        assertNotNull(compiledAction.getCompiledAction(), "编译后的动作对象不应为空");

        // 验证解析的方法名和参数
        CompiledActionObject actionObject = compiledAction.getCompiledAction();
        assertEquals("APPROVE", actionObject.getMethodName(), "方法名应该是APPROVE");
        assertEquals(2, actionObject.getParameters().size(), "参数数量应该是2");
        assertTrue(actionObject.getParameters().contains("userId"), "参数应该包含userId");
        assertTrue(actionObject.getParameters().contains("leaveType"), "参数应该包含leaveType");

        log.info("[规则编译测试] 测试通过: 规则动作编译成功, action={}, method={}, params={}",
                ruleAction, actionObject.getMethodName(), actionObject.getParameters());
    }

    @Test
    @DisplayName("测试编译规则动作-失败场景（空表达式）")
    void testCompileRuleAction_EmptyExpression() {
        log.info("[规则编译测试] 测试场景: 编译空动作表达式");

        // Given: 准备测试数据
        String ruleAction = "";

        // When: 编译规则动作
        CompiledAction compiledAction = ruleCompilationService.compileRuleAction(ruleAction);

        // Then: 验证编译失败
        assertNotNull(compiledAction, "编译结果不应为空");
        assertFalse(compiledAction.isCompiled(), "动作应该编译失败");
        assertNotNull(compiledAction.getCompilationError(), "错误消息不应为空");
        assertTrue(compiledAction.getCompilationError().contains("规则动作表达式不能为空"),
                "错误消息应该提示表达式为空");

        log.info("[规则编译测试] 测试通过: 空动作表达式编译失败被正确处理");
    }

    @Test
    @DisplayName("测试解析条件表达式-多种操作符")
    void testParseCondition_VariousOperators() {
        log.info("[规则编译测试] 测试场景: 解析包含多种操作符的条件表达式");

        // Given: 准备测试数据 - 测试所有支持的操作符
        String[] expressions = {
                "userId == 100",   // 等于
                "userId != 100",   // 不等于
                "age > 18",        // 大于
                "age < 60",        // 小于
                "score >= 80",     // 大于等于
                "score <= 100",    // 小于等于
                "flag1 && flag2",  // 逻辑与
                "flag1 || flag2"   // 逻辑或
        };

        String[] expectedOperators = {"==", "!=", ">", "<", ">=", "<=", "&&", "||"};

        // When: 编译每个表达式
        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            String expectedOperator = expectedOperators[i];

            CompiledRule compiledRule = ruleCompilationService.compileRuleCondition(expression);

            // Then: 验证每个操作符都被正确解析
            assertNotNull(compiledRule, "编译结果不应为空: " + expression);
            assertTrue(compiledRule.isCompiled(), "规则应该编译成功: " + expression);

            RuleCompilationService.CompiledCondition condition = compiledRule.getCompiledCondition();
            assertNotNull(condition, "编译后的条件对象不应为空: " + expression);
            assertEquals(expectedOperator, condition.getOperator(),
                    "操作符应该匹配: " + expression + ", expected: " + expectedOperator);

            log.debug("[规则编译测试] 操作符解析成功: expression={}, operator={}",
                    expression, condition.getOperator());
        }

        log.info("[规则编译测试] 测试通过: 所有8种操作符解析成功");
    }
}
