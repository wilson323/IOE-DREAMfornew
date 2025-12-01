package net.lab1024.sa.admin.module.consume.engine.mode;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeResult;
import net.lab1024.sa.admin.module.consume.engine.mode.FreeAmountMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自由金额消费模式单元测试
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig
@ExtendWith(SpringJUnitConfig.class)
class FreeAmountModeTest {

    private FreeAmountMode freeAmountMode;
    private ConsumeModeRequest testRequest;

    @BeforeEach
    void setUp() {
        freeAmountMode = new FreeAmountMode();

        // 初始化测试请求
        testRequest = new ConsumeModeRequest();
        testRequest.setUserId(1001L);
        testRequest.setAmount(new BigDecimal("100.00"));
        testRequest.setDeviceId(2001L);
        testRequest.setConsumeMode("FREE_AMOUNT");
        testRequest.setOrderNo("ORDER_001");
        testRequest.setUserLevel("BRONZE"); // 默认青铜会员
    }

    @Test
    @DisplayName("测试青铜会员自由金额消费")
    void testProcessFreeAmount_BronzeMember() {
        // 设置青铜会员
        testRequest.setUserLevel("BRONZE");

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 青铜会员无折扣
        assertEquals(testRequest.getAmount(), result.getOriginalAmount());
        assertEquals(testRequest.getAmount(), result.getFinalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试白银会员自由金额消费")
    void testProcessFreeAmount_SilverMember() {
        // 设置白银会员
        testRequest.setUserLevel("SILVER");

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 白银会员5%折扣
        assertEquals(testRequest.getAmount(), result.getOriginalAmount());
        assertEquals(new BigDecimal("95.00"), result.getFinalAmount());
        assertEquals(new BigDecimal("5.00"), result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试黄金会员自由金额消费")
    void testProcessFreeAmount_GoldMember() {
        // 设置黄金会员
        testRequest.setUserLevel("GOLD");

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 黄金会员10%折扣
        assertEquals(testRequest.getAmount(), result.getOriginalAmount());
        assertEquals(new BigDecimal("90.00"), result.getFinalAmount());
        assertEquals(new BigDecimal("10.00"), result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试铂金会员自由金额消费")
    void testProcessFreeAmount_PlatinumMember() {
        // 设置铂金会员
        testRequest.setUserLevel("PLATINUM");

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 铂金会员20%折扣（修复后的正确值）
        assertEquals(testRequest.getAmount(), result.getOriginalAmount());
        assertEquals(new BigDecimal("80.00"), result.getFinalAmount());
        assertEquals(new BigDecimal("20.00"), result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试钻石会员自由金额消费")
    void testProcessFreeAmount_DiamondMember() {
        // 设置钻石会员
        testRequest.setUserLevel("DIAMOND");

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 钻石会员30%折扣
        assertEquals(testRequest.getAmount(), result.getOriginalAmount());
        assertEquals(new BigDecimal("70.00"), result.getFinalAmount());
        assertEquals(new BigDecimal("30.00"), result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试未知会员等级自由金额消费")
    void testProcessFreeAmount_UnknownMemberLevel() {
        // 设置未知会员等级
        testRequest.setUserLevel("UNKNOWN");

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 未知等级无折扣
        assertEquals(testRequest.getAmount(), result.getOriginalAmount());
        assertEquals(testRequest.getAmount(), result.getFinalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试空会员等级自由金额消费")
    void testProcessFreeAmount_NullMemberLevel() {
        // 设置空会员等级
        testRequest.setUserLevel(null);

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 空等级无折扣
        assertEquals(testRequest.getAmount(), result.getOriginalAmount());
        assertEquals(testRequest.getAmount(), result.getFinalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试零金额自由金额消费")
    void testProcessFreeAmount_ZeroAmount() {
        // 设置零金额
        testRequest.setAmount(BigDecimal.ZERO);
        testRequest.setUserLevel("GOLD");

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 零金额无折扣
        assertEquals(BigDecimal.ZERO, result.getOriginalAmount());
        assertEquals(BigDecimal.ZERO, result.getFinalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试大金额自由金额消费")
    void testProcessFreeAmount_LargeAmount() {
        // 设置大金额
        testRequest.setAmount(new BigDecimal("99999.99"));
        testRequest.setUserLevel("DIAMOND");

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 钻石会员30%折扣
        assertEquals(new BigDecimal("99999.99"), result.getOriginalAmount());
        assertEquals(new BigDecimal("69999.99"), result.getFinalAmount());
        assertEquals(new BigDecimal("30000.00"), result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试小数精度自由金额消费")
    void testProcessFreeAmount_DecimalPrecision() {
        // 设置带小数的金额
        testRequest.setAmount(new BigDecimal("99.99"));
        testRequest.setUserLevel("SILVER");

        // 执行测试
        ConsumeModeResult result = freeAmountMode.processMode(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("自由金额消费成功", result.getMessage());

        // 白银会员5%折扣：99.99 * 0.95 = 94.9905
        assertEquals(new BigDecimal("99.99"), result.getOriginalAmount());
        assertEquals(new BigDecimal("94.99"), result.getFinalAmount()); // 四舍五入
        assertEquals(new BigDecimal("5.00"), result.getDiscountAmount());
        assertEquals("FREE_AMOUNT", result.getModeCode());
    }

    @Test
    @DisplayName("测试模式代码")
    void testGetModeCode() {
        // 执行测试
        String modeCode = freeAmountMode.getModeCode();

        // 验证结果
        assertEquals("FREE_AMOUNT", modeCode);
    }

    @Test
    @DisplayName("测试模式名称")
    void testGetModeName() {
        // 执行测试
        String modeName = freeAmountMode.getModeName();

        // 验证结果
        assertEquals("自由金额消费", modeName);
    }

    @Test
    @DisplayName("测试模式描述")
    void testGetModeDescription() {
        // 执行测试
        String description = freeAmountMode.getModeDescription();

        // 验证结果
        assertNotNull(description);
        assertTrue(description.contains("自由金额"));
        assertTrue(description.contains("会员等级"));
    }

    @Test
    @DisplayName("测试是否支持金额参数")
    void testSupportsAmountParam() {
        // 执行测试
        boolean supportsAmount = freeAmountMode.supportsAmountParam();

        // 验证结果
        assertTrue(supportsAmount);
    }

    @Test
    @DisplayName("测试验证请求参数 - 有效请求")
    void testValidateRequest_ValidRequest() {
        // 执行测试
        boolean isValid = freeAmountMode.validateRequest(testRequest);

        // 验证结果
        assertTrue(isValid);
    }

    @Test
    @DisplayName("测试验证请求参数 - 无效金额")
    void testValidateRequest_InvalidAmount() {
        // 设置负金额
        testRequest.setAmount(new BigDecimal("-100.00"));

        // 执行测试
        boolean isValid = freeAmountMode.validateRequest(testRequest);

        // 验证结果
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试验证请求参数 - 空请求")
    void testValidateRequest_NullRequest() {
        // 执行测试
        boolean isValid = freeAmountMode.validateRequest(null);

        // 验证结果
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试不同会员等级折扣率")
    void testDiscountRatesForDifferentLevels() {
        // 测试不同会员等级的折扣率
        String[] memberLevels = {"BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND"};
        BigDecimal[] expectedDiscountRates = {
            BigDecimal.ZERO,        // BRONZE: 0%
            new BigDecimal("0.05"),  // SILVER: 5%
            new BigDecimal("0.10"),  // GOLD: 10%
            new BigDecimal("0.20"),  // PLATINUM: 20% (修复后)
            new BigDecimal("0.30")   // DIAMOND: 30%
        };

        for (int i = 0; i < memberLevels.length; i++) {
            testRequest.setUserLevel(memberLevels[i]);
            testRequest.setAmount(new BigDecimal("100.00"));

            ConsumeModeResult result = freeAmountMode.processMode(testRequest);

            assertNotNull(result);
            assertTrue(result.isSuccess());

            // 验证折扣金额
            BigDecimal expectedDiscount = testRequest.getAmount().multiply(expectedDiscountRates[i]);
            assertEquals(expectedDiscount, result.getDiscountAmount(),
                "会员等级 " + memberLevels[i] + " 折扣率不正确");
        }
    }

    @Test
    @DisplayName("测试边界值金额处理")
    void testBoundaryAmounts() {
        BigDecimal[] boundaryAmounts = {
            new BigDecimal("0.01"),   // 最小金额
            new BigDecimal("0.99"),   // 接近1元
            new BigDecimal("1.00"),   // 1元
            new BigDecimal("999.99"), // 接近1000元
            new BigDecimal("1000.00") // 1000元
        };

        for (BigDecimal amount : boundaryAmounts) {
            testRequest.setAmount(amount);
            testRequest.setUserLevel("GOLD");

            ConsumeModeResult result = freeAmountMode.processMode(testRequest);

            assertNotNull(result, "金额 " + amount + " 处理失败");
            assertTrue(result.isSuccess(), "金额 " + amount + " 应该成功");

            // 验证黄金会员10%折扣
            BigDecimal expectedFinalAmount = amount.multiply(new BigDecimal("0.90"));
            assertEquals(expectedFinalAmount, result.getFinalAmount(),
                "金额 " + amount + " 最终金额计算错误");
        }
    }
}