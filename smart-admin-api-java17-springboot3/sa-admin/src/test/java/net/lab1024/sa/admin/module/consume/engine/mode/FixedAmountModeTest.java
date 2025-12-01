package net.lab1024.sa.admin.module.consume.engine.mode;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeResult;
import net.lab1024.sa.admin.module.consume.engine.mode.FixedAmountMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 固定金额消费模式单元测试
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig
@ExtendWith(SpringJUnitConfig.class)
class FixedAmountModeTest {

    private FixedAmountMode fixedAmountMode;
    private ConsumeModeRequest testRequest;

    @BeforeEach
    void setUp() {
        fixedAmountMode = new FixedAmountMode();

        // 初始化测试请求
        testRequest = new ConsumeModeRequest();
        testRequest.setUserId(1001L);
        testRequest.setAmount(new BigDecimal("10.00")); // 固定金额10元
        testRequest.setDeviceId(2001L);
        testRequest.setConsumeMode("FIXED_AMOUNT");
        testRequest.setOrderNo("ORDER_001");
    }

    @Test
    @DisplayName("测试标准固定金额消费")
    void testProcessFixedAmount_Success() {
        // 执行测试
        ConsumeModeResult result = fixedAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("固定金额消费成功", result.getMessage());

        // 固定金额模式，无折扣
        assertEquals(testRequest.getAmount(), result.getOriginalAmount());
        assertEquals(testRequest.getAmount(), result.getFinalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals("FIXED_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试不同固定金额消费")
    void testProcessFixedAmount_DifferentAmounts() {
        BigDecimal[] testAmounts = {
            new BigDecimal("1.00"),
            new BigDecimal("5.00"),
            new BigDecimal("10.00"),
            new BigDecimal("20.00"),
            new BigDecimal("50.00"),
            new BigDecimal("100.00")
        };

        for (BigDecimal amount : testAmounts) {
            testRequest.setAmount(amount);

            ConsumeModeResult result = fixedAmountMode.processMode(testRequest);

            assertNotNull(result, "金额 " + amount + " 处理失败");
            assertTrue(result.isSuccess(), "金额 " + amount + " 应该成功");
            assertEquals("FIXED_AMOUNT", result.getModeCode());

            // 固定金额模式，无折扣
            assertEquals(amount, result.getOriginalAmount());
            assertEquals(amount, result.getFinalAmount());
            assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        }
    }

    @Test
    @DisplayName("测试零金额固定消费")
    void testProcessFixedAmount_ZeroAmount() {
        testRequest.setAmount(BigDecimal.ZERO);

        // 执行测试
        ConsumeModeResult result = fixedAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("固定金额消费成功", result.getMessage());

        // 零金额
        assertEquals(BigDecimal.ZERO, result.getOriginalAmount());
        assertEquals(BigDecimal.ZERO, result.getFinalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals("FIXED_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试小数精度固定消费")
    void testProcessFixedAmount_DecimalPrecision() {
        // 设置带小数的金额
        testRequest.setAmount(new BigDecimal("15.99"));

        // 执行测试
        ConsumeModeResult result = fixedAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("固定金额消费成功", result.getMessage());

        // 精确金额，无折扣
        assertEquals(new BigDecimal("15.99"), result.getOriginalAmount());
        assertEquals(new BigDecimal("15.99"), result.getFinalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals("FIXED_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试大金额固定消费")
    void testProcessFixedAmount_LargeAmount() {
        // 设置大金额
        testRequest.setAmount(new BigDecimal("1000.00"));

        // 执行测试
        ConsumeModeResult result = fixedAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("固定金额消费成功", result.getMessage());

        // 大金额，无折扣
        assertEquals(new BigDecimal("1000.00"), result.getOriginalAmount());
        assertEquals(new BigDecimal("1000.00"), result.getFinalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals("FIXED_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试模式代码")
    void testGetModeCode() {
        // 执行测试
        String modeCode = fixedAmountMode.getModeCode();

        // 验证结果
        assertEquals("FIXED_AMOUNT", modeCode);
    }

    @Test
    @DisplayName("测试模式名称")
    void testGetModeName() {
        // 执行测试
        String modeName = fixedAmountMode.getModeName();

        // 验证结果
        assertEquals("固定金额消费", modeName);
    }

    @Test
    @DisplayName("测试模式描述")
    void testGetModeDescription() {
        // 执行测试
        String description = fixedAmountMode.getModeDescription();

        // 验证结果
        assertNotNull(description);
        assertTrue(description.contains("固定金额"));
    }

    @Test
    @DisplayName("测试是否支持金额参数")
    void testSupportsAmountParam() {
        // 执行测试
        boolean supportsAmount = fixedAmountMode.supportsAmountParam();

        // 验证结果
        assertTrue(supportsAmount);
    }

    @Test
    @DisplayName("测试验证请求参数 - 有效请求")
    void testValidateRequest_ValidRequest() {
        // 执行测试
        boolean isValid = fixedAmountMode.validateRequest(testRequest);

        // 验证结果
        assertTrue(isValid);
    }

    @Test
    @DisplayName("测试验证请求参数 - 无效金额")
    void testValidateRequest_InvalidAmount() {
        // 设置负金额
        testRequest.setAmount(new BigDecimal("-100.00"));

        // 执行测试
        boolean isValid = fixedAmountMode.validateRequest(testRequest);

        // 验证结果
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试验证请求参数 - 空请求")
    void testValidateRequest_NullRequest() {
        // 执行测试
        boolean isValid = fixedAmountMode.validateRequest(null);

        // 验证结果
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试边界值金额处理")
    void testBoundaryAmounts() {
        BigDecimal[] boundaryAmounts = {
            new BigDecimal("0.01"),   // 最小金额
            new BigDecimal("0.10"),   // 10分
            new BigDecimal("1.00"),   // 1元
            new BigDecimal("99.99"),  // 接近100元
            new BigDecimal("100.00"), // 100元
            new BigDecimal("999.99")  // 接近1000元
        };

        for (BigDecimal amount : boundaryAmounts) {
            testRequest.setAmount(amount);

            ConsumeModeResult result = fixedAmountMode.processMode(testRequest);

            assertNotNull(result, "金额 " + amount + " 处理失败");
            assertTrue(result.isSuccess(), "金额 " + amount + " 应该成功");
            assertEquals("FIXED_AMOUNT", result.getModeCode());

            // 固定金额模式，无折扣
            assertEquals(amount, result.getOriginalAmount(),
                "金额 " + amount + " 原始金额不正确");
            assertEquals(amount, result.getFinalAmount(),
                "金额 " + amount + " 最终金额不正确");
            assertEquals(BigDecimal.ZERO, result.getDiscountAmount(),
                "金额 " + amount + " 折扣金额应该为0");
        }
    }

    @Test
    @DisplayName("测试不同用户等级处理")
    void testDifferentUserLevels() {
        String[] userLevels = {"BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", null};

        for (String userLevel : userLevels) {
            testRequest.setUserLevel(userLevel);
            testRequest.setAmount(new BigDecimal("20.00"));

            ConsumeModeResult result = fixedAmountMode.processMode(testRequest);

            assertNotNull(result, "用户等级 " + userLevel + " 处理失败");
            assertTrue(result.isSuccess(), "用户等级 " + userLevel + " 应该成功");
            assertEquals("FIXED_AMOUNT", result.getModeCode());

            // 固定金额模式对所有用户等级都无折扣
            assertEquals(new BigDecimal("20.00"), result.getOriginalAmount(),
                "用户等级 " + userLevel + " 原始金额不正确");
            assertEquals(new BigDecimal("20.00"), result.getFinalAmount(),
                "用户等级 " + userLevel + " 最终金额不正确");
            assertEquals(BigDecimal.ZERO, result.getDiscountAmount(),
                "用户等级 " + userLevel + " 折扣金额应该为0");
        }
    }

    @Test
    @DisplayName("测试请求参数不变性")
    void testRequestImmutability() {
        // 保存原始请求参数
        BigDecimal originalAmount = testRequest.getAmount();
        String originalUserLevel = testRequest.getUserLevel();

        // 执行测试
        fixedAmountMode.processMode(testRequest);

        // 验证请求参数未被修改
        assertEquals(originalAmount, testRequest.getAmount(),
            "请求金额不应被修改");
        assertEquals(originalUserLevel, testRequest.getUserLevel(),
            "用户等级不应被修改");
        assertEquals("FIXED_AMOUNT", testRequest.getConsumeMode(),
            "消费模式不应被修改");
    }

    @Test
    @DisplayName("测试结果对象完整性")
    void testResultObjectCompleteness() {
        // 执行测试
        ConsumeModeResult result = fixedAmountMode.processMode(testRequest);

        // 验证结果对象完整性
        assertNotNull(result);
        assertNotNull(result.getCode());
        assertNotNull(result.getMessage());
        assertNotNull(result.getOriginalAmount());
        assertNotNull(result.getFinalAmount());
        assertNotNull(result.getDiscountAmount());
        assertNotNull(result.getModeCode());

        // 验证金额逻辑一致性
        assertEquals(result.getOriginalAmount().subtract(result.getDiscountAmount()),
            result.getFinalAmount(),
            "原始金额减去折扣金额应等于最终金额");
    }

    @Test
    @DisplayName("测试多次调用一致性")
    void testMultipleCallConsistency() {
        // 多次调用相同请求
        ConsumeModeResult result1 = fixedAmountMode.processMode(testRequest);
        ConsumeModeResult result2 = fixedAmountMode.processMode(testRequest);
        ConsumeModeResult result3 = fixedAmountMode.processMode(testRequest);

        // 验证结果一致性
        assertEquals(result1.getCode(), result2.getCode());
        assertEquals(result2.getCode(), result3.getCode());

        assertEquals(result1.getOriginalAmount(), result2.getOriginalAmount());
        assertEquals(result2.getOriginalAmount(), result3.getOriginalAmount());

        assertEquals(result1.getFinalAmount(), result2.getFinalAmount());
        assertEquals(result2.getFinalAmount(), result3.getFinalAmount());

        assertEquals(result1.getDiscountAmount(), result2.getDiscountAmount());
        assertEquals(result2.getDiscountAmount(), result3.getDiscountAmount());
    }
}