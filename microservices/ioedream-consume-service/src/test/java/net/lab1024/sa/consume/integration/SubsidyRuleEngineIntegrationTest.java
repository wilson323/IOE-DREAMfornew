package net.lab1024.sa.consume.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.consume.dao.*;
import net.lab1024.sa.consume.domain.dto.SubsidyCalculationDTO;
import net.lab1024.sa.consume.domain.dto.SubsidyResultDTO;
import net.lab1024.sa.common.entity.consume.*;
import net.lab1024.sa.consume.service.SubsidyRuleEngineService;
import net.lab1024.sa.consume.service.impl.SubsidyRuleEngineServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 补贴规则引擎集成测试
 *
 * 测试范围：
 * 1. 完整业务流程端到端测试
 * 2. 数据库事务一致性
 * 3. 规则执行日志记录
 * 4. 补贴记录生成
 * 5. 规则管理操作
 * 6. 缓存一致性
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("补贴规则引擎集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubsidyRuleEngineIntegrationTest {

    @Autowired(required = false)
    private SubsidyRuleEngineService ruleEngine;

    @Autowired(required = false)
    private SubsidyRuleDao subsidyRuleDao;

    @Autowired(required = false)
    private SubsidyRuleConditionDao ruleConditionDao;

    @Autowired(required = false)
    private SubsidyRuleLogDao ruleLogDao;

    @Autowired(required = false)
    private UserSubsidyRecordDao subsidyRecordDao;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private static Long testRuleId;
    private static final String TEST_RULE_CODE = "INTEGRATION_TEST_RULE";

    // ==================== 测试准备和清理 ====================

    @BeforeAll
    static void beforeAll() {
        System.out.println("========================================");
        System.out.println("补贴规则引擎集成测试开始");
        System.out.println("========================================");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("========================================");
        System.out.println("补贴规则引擎集成测试完成");
        System.out.println("========================================");
    }

    @BeforeEach
    @Transactional
    void setUp() {
        // 清理测试数据
        if (ruleConditionDao != null) {
            ruleConditionDao.deleteByRuleId(testRuleId);
        }
    }

    // ==================== 1. 端到端业务流程测试 ====================

    @Test
    @Order(1)
    @DisplayName("E2E-001: 创建规则并计算补贴")
    @Transactional
    void testE2E_CreateRuleAndCalculate() {
        // 假设：检查依赖是否注入
        assumeTrue(ruleEngine != null, "规则引擎服务未注入");
        assumeTrue(subsidyRuleDao != null, "规则DAO未注入");

        // Step 1: 创建测试规则
        SubsidyRuleEntity rule = createTestRule();
        int insertResult = subsidyRuleDao.insert(rule);
        assertTrue(insertResult > 0, "规则创建失败");
        testRuleId = rule.getId();
        assertNotNull(testRuleId, "规则ID不应为null");

        // Step 2: 创建计算请求
        SubsidyCalculationDTO calculationDTO = new SubsidyCalculationDTO();
        calculationDTO.setUserId(1L);
        calculationDTO.setConsumeId(100L);
        calculationDTO.setDeviceId(1001L);
        calculationDTO.setConsumeAmount(new BigDecimal("25.00"));
        calculationDTO.setConsumeTime(LocalDateTime.of(2025, 12, 26, 12, 30));
        calculationDTO.setMealType(2); // 午餐
        calculationDTO.setSubsidyType(1); // 餐补

        // Step 3: 计算补贴
        SubsidyResultDTO result = ruleEngine.calculateSubsidy(calculationDTO);

        // Step 4: 验证结果
        assertAll("补贴计算结果验证",
                () -> assertTrue(result.getMatched(), "应匹配到规则"),
                () -> assertTrue(result.getSuccess(), "计算应成功"),
                () -> assertNotNull(result.getSubsidyAmount(), "补贴金额不应为null"),
                () -> assertEquals(TEST_RULE_CODE, result.getRuleCode(), "规则代码应匹配")
        );

        System.out.println("✅ E2E-001: 创建规则并计算补贴 - 通过");
    }

    @Test
    @Order(2)
    @DisplayName("E2E-002: 执行规则并验证日志记录")
    @Transactional
    void testE2E_ExecuteRuleAndVerifyLog() {
        assumeTrue(ruleEngine != null, "规则引擎服务未注入");
        assumeTrue(testRuleId != null, "测试规则不存在");

        // Step 1: 执行规则
        SubsidyCalculationDTO calculationDTO = new SubsidyCalculationDTO();
        calculationDTO.setUserId(2L);
        calculationDTO.setConsumeId(101L);
        calculationDTO.setDeviceId(1002L);
        calculationDTO.setConsumeAmount(new BigDecimal("30.00"));
        calculationDTO.setConsumeTime(LocalDateTime.now());
        calculationDTO.setMealType(2);
        calculationDTO.setSubsidyType(1);

        SubsidyResultDTO result = ruleEngine.executeRule(calculationDTO);

        // Step 2: 验证执行结果
        assertTrue(result.getSuccess(), "执行应成功");

        // Step 3: 查询执行日志
        if (ruleLogDao != null) {
            List<SubsidyRuleLogEntity> logs = ruleLogDao.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubsidyRuleLogEntity>()
                            .eq(SubsidyRuleLogEntity::getConsumeId, calculationDTO.getConsumeId())
            );

            assertFalse(logs.isEmpty(), "应生成执行日志");

            SubsidyRuleLogEntity log = logs.get(0);
            assertAll("执行日志验证",
                    () -> assertEquals(TEST_RULE_CODE, log.getRuleCode(), "日志规则代码应匹配"),
                    () -> assertEquals(calculationDTO.getUserId(), log.getUserId(), "日志用户ID应匹配"),
                    () -> assertEquals(result.getSubsidyAmount(), log.getSubsidyAmount(), "日志补贴金额应匹配"),
                    () -> assertEquals(1, log.getExecutionStatus(), "日志执行状态应为成功")
            );
        }

        System.out.println("✅ E2E-002: 执行规则并验证日志记录 - 通过");
    }

    @Test
    @Order(3)
    @DisplayName("E2E-003: 验证补贴记录生成")
    @Transactional
    void testE2E_VerifySubsidyRecord() {
        assumeTrue(ruleEngine != null, "规则引擎服务未注入");
        assumeTrue(subsidyRecordDao != null, "补贴记录DAO未注入");

        // Step 1: 执行规则
        SubsidyCalculationDTO calculationDTO = new SubsidyCalculationDTO();
        calculationDTO.setUserId(3L);
        calculationDTO.setConsumeId(102L);
        calculationDTO.setDeviceId(1003L);
        calculationDTO.setConsumeAmount(new BigDecimal("20.00"));
        calculationDTO.setConsumeTime(LocalDateTime.now());
        calculationDTO.setMealType(2);
        calculationDTO.setSubsidyType(1);

        SubsidyResultDTO result = ruleEngine.executeRule(calculationDTO);

        // Step 2: 查询补贴记录
        List<UserSubsidyRecordEntity> records = subsidyRecordDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserSubsidyRecordEntity>()
                        .eq(UserSubsidyRecordEntity::getConsumeId, calculationDTO.getConsumeId())
        );

        assertFalse(records.isEmpty(), "应生成补贴记录");

        UserSubsidyRecordEntity record = records.get(0);
        assertAll("补贴记录验证",
                () -> assertEquals(calculationDTO.getUserId(), record.getUserId(), "记录用户ID应匹配"),
                () -> assertEquals(result.getSubsidyAmount(), record.getSubsidyAmount(), "记录补贴金额应匹配"),
                () -> assertEquals(TEST_RULE_CODE, record.getRuleCode(), "记录规则代码应匹配"),
                () -> assertEquals(1, record.getStatus(), "记录状态应为有效")
        );

        System.out.println("✅ E2E-003: 验证补贴记录生成 - 通过");
    }

    // ==================== 2. 事务一致性测试 ====================

    @Test
    @Order(4)
    @DisplayName("E2E-004: 事务回滚测试")
    @Transactional
    void testE2E_TransactionRollback() {
        assumeTrue(ruleEngine != null, "规则引擎服务未注入");
        assumeTrue(ruleLogDao != null, "规则日志DAO未注入");
        assumeTrue(subsidyRecordDao != null, "补贴记录DAO未注入");

        // Step 1: 记录执行前的日志数量
        long logCountBefore = ruleLogDao.selectCount(null);
        long recordCountBefore = subsidyRecordDao.selectCount(null);

        // Step 2: 创建一个会失败的规则（消费金额为负）
        SubsidyCalculationDTO calculationDTO = new SubsidyCalculationDTO();
        calculationDTO.setUserId(4L);
        calculationDTO.setConsumeId(103L);
        calculationDTO.setDeviceId(1004L);
        calculationDTO.setConsumeAmount(new BigDecimal("-10.00")); // 无效金额
        calculationDTO.setConsumeTime(LocalDateTime.now());
        calculationDTO.setMealType(2);
        calculationDTO.setSubsidyType(1);

        // Step 3: 执行规则（应失败）
        SubsidyResultDTO result = ruleEngine.executeRule(calculationDTO);

        // Step 4: 验证执行失败
        assertFalse(result.getSuccess(), "执行应失败");

        // Step 5: 验证未生成日志和记录（事务回滚）
        long logCountAfter = ruleLogDao.selectCount(null);
        long recordCountAfter = subsidyRecordDao.selectCount(null);

        assertEquals(logCountBefore, logCountAfter, "失败执行不应生成日志");
        assertEquals(recordCountBefore, recordCountAfter, "失败执行不应生成补贴记录");

        System.out.println("✅ E2E-004: 事务回滚测试 - 通过");
    }

    // ==================== 3. 规则管理操作测试 ====================

    @Test
    @Order(5)
    @DisplayName("E2E-005: 规则启用/禁用操作")
    @Transactional
    void testE2E_RuleEnableDisable() {
        assumeTrue(ruleEngine != null, "规则引擎服务未注入");
        assumeTrue(subsidyRuleDao != null, "规则DAO未注入");
        assumeTrue(testRuleId != null, "测试规则不存在");

        // Step 1: 禁用规则
        ruleEngine.disableRule(testRuleId);

        // Step 2: 验证规则已禁用
        SubsidyRuleEntity disabledRule = subsidyRuleDao.selectById(testRuleId);
        assertEquals(0, disabledRule.getStatus(), "规则状态应为禁用");

        // Step 3: 尝试使用禁用的规则计算
        SubsidyCalculationDTO calculationDTO = new SubsidyCalculationDTO();
        calculationDTO.setUserId(5L);
        calculationDTO.setConsumeId(104L);
        calculationDTO.setDeviceId(1005L);
        calculationDTO.setConsumeAmount(new BigDecimal("25.00"));
        calculationDTO.setConsumeTime(LocalDateTime.now());
        calculationDTO.setMealType(2);
        calculationDTO.setSubsidyType(1);

        SubsidyResultDTO result = ruleEngine.calculateSubsidy(calculationDTO);
        assertFalse(result.getMatched(), "禁用的规则不应匹配");

        // Step 4: 重新启用规则
        ruleEngine.enableRule(testRuleId);

        // Step 5: 验证规则已启用
        SubsidyRuleEntity enabledRule = subsidyRuleDao.selectById(testRuleId);
        assertEquals(1, enabledRule.getStatus(), "规则状态应为启用");

        System.out.println("✅ E2E-005: 规则启用/禁用操作 - 通过");
    }

    @Test
    @Order(6)
    @DisplayName("E2E-006: 规则优先级调整")
    @Transactional
    void testE2E_RulePriorityAdjustment() {
        assumeTrue(ruleEngine != null, "规则引擎服务未注入");
        assumeTrue(subsidyRuleDao != null, "规则DAO未注入");
        assumeTrue(testRuleId != null, "测试规则不存在");

        // Step 1: 调整优先级
        Integer newPriority = 80;
        ruleEngine.adjustPriority(testRuleId, newPriority);

        // Step 2: 验证优先级已更新
        SubsidyRuleEntity rule = subsidyRuleDao.selectById(testRuleId);
        assertEquals(newPriority, rule.getPriority(), "优先级应已更新");

        System.out.println("✅ E2E-006: 规则优先级调整 - 通过");
    }

    // ==================== 4. 复杂场景测试 ====================

    @Test
    @Order(7)
    @DisplayName("E2E-007: 多规则竞争场景")
    @Transactional
    void testE2E_MultipleRulesCompetition() {
        assumeTrue(ruleEngine != null, "规则引擎服务未注入");
        assumeTrue(subsidyRuleDao != null, "规则DAO未注入");

        // Step 1: 创建多个规则（不同优先级）
        SubsidyRuleEntity highPriorityRule = createTestRule();
        highPriorityRule.setRuleCode("HIGH_PRIORITY_RULE");
        highPriorityRule.setPriority(90);
        highPriorityRule.setSubsidyAmount(new BigDecimal("20.00"));
        subsidyRuleDao.insert(highPriorityRule);

        SubsidyRuleEntity lowPriorityRule = createTestRule();
        lowPriorityRule.setRuleCode("LOW_PRIORITY_RULE");
        lowPriorityRule.setPriority(30);
        lowPriorityRule.setSubsidyAmount(new BigDecimal("10.00"));
        subsidyRuleDao.insert(lowPriorityRule);

        // Step 2: 计算补贴
        SubsidyCalculationDTO calculationDTO = new SubsidyCalculationDTO();
        calculationDTO.setUserId(6L);
        calculationDTO.setConsumeId(105L);
        calculationDTO.setDeviceId(1006L);
        calculationDTO.setConsumeAmount(new BigDecimal("25.00"));
        calculationDTO.setConsumeTime(LocalDateTime.now());
        calculationDTO.setMealType(2);
        calculationDTO.setSubsidyType(1);

        SubsidyResultDTO result = ruleEngine.calculateSubsidy(calculationDTO);

        // Step 3: 验证高优先级规则被应用
        assertAll("多规则竞争验证",
                () -> assertTrue(result.getMatched(), "应匹配到规则"),
                () -> assertEquals("HIGH_PRIORITY_RULE", result.getRuleCode(), "应应用高优先级规则"),
                () -> assertEquals(new BigDecimal("20.00"), result.getSubsidyAmount(), "补贴金额应为高优先级规则金额")
        );

        System.out.println("✅ E2E-007: 多规则竞争场景 - 通过");
    }

    @Test
    @Order(8)
    @DisplayName("E2E-008: 无匹配规则场景")
    @Transactional
    void testE2E_NoMatchedRules() {
        assumeTrue(ruleEngine != null, "规则引擎服务未注入");
        assumeTrue(subsidyRuleDao != null, "规则DAO未注入");

        // Step 1: 创建一个无法匹配的计算请求（错误的餐别）
        SubsidyCalculationDTO calculationDTO = new SubsidyCalculationDTO();
        calculationDTO.setUserId(7L);
        calculationDTO.setConsumeId(106L);
        calculationDTO.setDeviceId(1007L);
        calculationDTO.setConsumeAmount(new BigDecimal("25.00"));
        calculationDTO.setConsumeTime(LocalDateTime.now());
        calculationDTO.setMealType(99); // 不存在的餐别
        calculationDTO.setSubsidyType(1);

        // Step 2: 计算补贴
        SubsidyResultDTO result = ruleEngine.calculateSubsidy(calculationDTO);

        // Step 3: 验证未匹配
        assertAll("无匹配规则验证",
                () -> assertFalse(result.getMatched(), "不应匹配到规则"),
                () -> assertNotNull(result, "结果不应为null")
        );

        System.out.println("✅ E2E-008: 无匹配规则场景 - 通过");
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试规则
     */
    private SubsidyRuleEntity createTestRule() {
        SubsidyRuleEntity rule = new SubsidyRuleEntity();
        rule.setRuleCode(TEST_RULE_CODE);
        rule.setRuleName("集成测试规则");
        rule.setRuleType(1); // 固定金额
        rule.setSubsidyType(1); // 餐补
        rule.setSubsidyAmount(new BigDecimal("15.00"));
        rule.setApplyTimeType(1); // 全部
        rule.setApplyMealTypes("1,2,3"); // 所有餐别
        rule.setPriority(50);
        rule.setStatus(1); // 启用
        rule.setEffectiveDate(LocalDateTime.now().minusDays(1));
        rule.setExpireDate(LocalDateTime.now().plusDays(30));
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        rule.setDeletedFlag(0);
        rule.setVersion(0);

        return rule;
    }

    /**
     * 假设工具方法
     */
    private void assumeTrue(boolean condition, String message) {
        if (!condition) {
            System.out.println("⚠️ " + message + " - 跳过测试");
            Assumptions.assumeTrue(condition, message);
        }
    }
}
