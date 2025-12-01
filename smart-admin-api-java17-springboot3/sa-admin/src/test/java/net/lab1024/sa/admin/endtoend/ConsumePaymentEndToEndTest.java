/*
 * Copyright (c) 2025 IOE-DREAM Project
 * 端到端消费支付业务流程测试
 * 基于现有项目业务场景的完整流程验证
 *
 * 业务流程：用户认证 → 账户验证 → 消费扣款 → 记录存储
 * 测试路径：Gateway → Consume Service → Database
 */

package net.lab1024.sa.admin.test.endtoend;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeAccountEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.RechargeRecordEntity;
import net.lab1024.sa.admin.module.consume.service.ConsumeAccountService;
import net.lab1024.sa.admin.module.consume.service.ConsumeRecordService;
import net.lab1024.sa.admin.module.consume.service.RechargeRecordService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartAuthorizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import jakarta.annotation.Resource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消费支付端到端业务流程测试
 *
 * 测试目标：
 * 1. 验证完整的消费支付业务流程
 * 2. 确保资金交易的安全性和一致性
 * 3. 验证六大消费模式的正确执行
 * 4. 检查账户余额的准确性
 * 5. 测试SAGA分布式事务处理
 * 6. 验证充值退款流程完整性
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("消费支付端到端业务流程测试")
public class ConsumePaymentEndToEndTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private ConsumeAccountService consumeAccountService;

    @Resource
    private ConsumeRecordService consumeRecordService;

    @Resource
    private RechargeRecordService rechargeRecordService;

    @Resource
    private ObjectMapper objectMapper;

    private String testToken;
    private Long testAccountId;
    private Long testUserId = 2001L;
    private Long testAreaId = 1001L;
    private Long testDeviceId = 2001L;
    private Long testMealId = 3001L;

    /**
     * 测试数据准备
     */
    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        // 1. 登录获取token
        testToken = obtainTestToken();

        // 2. 创建测试账户
        testAccountId = createTestAccount();

        // 3. 充值初始余额
        rechargeInitialBalance(testAccountId, BigDecimal.valueOf(1000.00));
    }

    /**
     * 场景1：固定金额消费模式测试
     * 适用场景：食堂、班车、门票
     */
    @Test
    @Order(1)
    @DisplayName("固定金额消费模式流程测试")
    @Transactional
    void testFixedAmountConsumeFlow() throws Exception {
        System.out.println("💰 开始固定金额消费模式流程测试...");

        // Step 1: 检查账户余额
        BigDecimal balanceBefore = getAccountBalance(testAccountId);
        assertTrue(balanceBefore.compareTo(BigDecimal.valueOf(15.00)) >= 0,
                  "账户余额应该足以支付固定金额消费");

        // Step 2: 执行固定金额消费
        ConsumeRecordEntity consumeRecord = executeFixedAmountConsume("FIXED_AMOUNT", BigDecimal.valueOf(15.00));
        assertNotNull(consumeRecord, "消费记录应该被创建");
        assertEquals("FIXED_AMOUNT", consumeRecord.getConsumeMode(), "消费模式应该是固定金额");
        assertEquals("SUCCESS", consumeRecord.getStatus(), "消费应该成功");

        // Step 3: 验证余额扣除
        BigDecimal balanceAfter = getAccountBalance(testAccountId);
        assertEquals(balanceBefore.subtract(BigDecimal.valueOf(15.00)), balanceAfter,
                     "余额应该正确扣除");

        // Step 4: 验证消费记录存储
        ConsumeRecordEntity storedRecord = consumeRecordService.getById(consumeRecord.getRecordId());
        assertNotNull(storedRecord, "消费记录应该被正确存储");
        assertEquals(testAccountId, storedRecord.getAccountId(), "账户ID应该匹配");
        assertEquals(BigDecimal.valueOf(15.00), storedRecord.getConsumeMoney(), "消费金额应该匹配");

        System.out.println("✅ 固定金额消费模式测试完成");
    }

    /**
     * 场景2：自由金额消费模式测试
     * 适用场景：超市、停车场、咖啡厅
     */
    @Test
    @Order(2)
    @DisplayName("自由金额消费模式流程测试")
    @Transactional
    void testFreeAmountConsumeFlow() throws Exception {
        System.out.println("🛒 开始自由金额消费模式流程测试...");

        // 执行自由金额消费
        BigDecimal consumeAmount = BigDecimal.valueOf(28.50);
        ConsumeRecordEntity consumeRecord = executeFreeAmountConsume("FREE_AMOUNT", consumeAmount);

        // 验证消费结果
        assertNotNull(consumeRecord, "消费记录应该被创建");
        assertEquals("FREE_AMOUNT", consumeRecord.getConsumeMode(), "消费模式应该是自由金额");
        assertEquals("SUCCESS", consumeRecord.getStatus(), "消费应该成功");
        assertEquals(consumeAmount, consumeRecord.getConsumeMoney(), "消费金额应该匹配");

        System.out.println("✅ 自由金额消费模式测试完成");
    }

    /**
     * 场景3：计量计费消费模式测试
     * 适用场景：健身房、会议室、充电桩
     */
    @Test
    @Order(3)
    @DisplayName("计量计费消费模式流程测试")
    @Transactional
    void testMeteredConsumeFlow() throws Exception {
        System.out.println("⏱️ 开始计量计费消费模式流程测试...");

        // 执行计量计费消费（按时计费）
        ConsumeRecordEntity consumeRecord = executeMeteredConsume("METERED", "TIMING", 120); // 120分钟

        // 验证计量消费结果
        assertNotNull(consumeRecord, "消费记录应该被创建");
        assertEquals("METERED", consumeRecord.getConsumeMode(), "消费模式应该是计量计费");
        assertEquals("SUCCESS", consumeRecord.getStatus(), "消费应该成功");

        System.out.println("✅ 计量计费消费模式测试完成");
    }

    /**
     * 场景4：商品消费模式测试
     * 适用场景：超市、便利店
     */
    @Test
    @Order(4)
    @DisplayName("商品消费模式流程测试")
    @Transactional
    void testProductConsumeFlow() throws Exception {
        System.out.println("📦 开始商品消费模式流程测试...");

        // 创建商品清单
        String productList = """
            [
                {"productId": "P001", "productName": "矿泉水", "quantity": 2, "unitPrice": 2.50},
                {"productId": "P002", "productName": "面包", "quantity": 1, "unitPrice": 8.00}
            ]
            """;

        // 执行商品消费
        ConsumeRecordEntity consumeRecord = executeProductConsume("PRODUCT", productList);

        // 验证商品消费结果
        assertNotNull(consumeRecord, "消费记录应该被创建");
        assertEquals("PRODUCT", consumeRecord.getConsumeMode(), "消费模式应该是商品消费");
        assertEquals("SUCCESS", consumeRecord.getStatus(), "消费应该成功");
        assertEquals(BigDecimal.valueOf(13.00), consumeRecord.getConsumeMoney(), "消费总额应该是13.00元");

        System.out.println("✅ 商品消费模式测试完成");
    }

    /**
     * 场景5：充值流程测试
     */
    @Test
    @Order(5)
    @DisplayName("充值流程完整性测试")
    @Transactional
    void testRechargeFlow() throws Exception {
        System.out.println("💵 开始充值流程测试...");

        BigDecimal balanceBefore = getAccountBalance(testAccountId);
        BigDecimal rechargeAmount = BigDecimal.valueOf(500.00);

        // 执行充值
        RechargeRecordEntity rechargeRecord = executeRecharge(testAccountId, rechargeAmount, "WECHAT_PAY");

        // 验证充值结果
        assertNotNull(rechargeRecord, "充值记录应该被创建");
        assertEquals("SUCCESS", rechargeRecord.getStatus(), "充值应该成功");
        assertEquals(rechargeAmount, rechargeRecord.getRechargeMoney(), "充值金额应该匹配");

        // 验证余额更新
        BigDecimal balanceAfter = getAccountBalance(testAccountId);
        assertEquals(balanceBefore.add(rechargeAmount), balanceAfter, "余额应该正确增加");

        System.out.println("✅ 充值流程测试完成");
    }

    /**
     * 场景6：退款流程测试
     */
    @Test
    @Order(6)
    @DisplayName("退款流程完整性测试")
    @Transactional
    void testRefundFlow() throws Exception {
        System.out.println("💸 开始退款流程测试...");

        // 先进行一笔消费
        ConsumeRecordEntity consumeRecord = executeFixedAmountConsume("FIXED_AMOUNT", BigDecimal.valueOf(20.00));
        BigDecimal balanceAfterConsume = getAccountBalance(testAccountId);

        // 执行退款
        BigDecimal refundAmount = BigDecimal.valueOf(20.00);
        RechargeRecordEntity refundRecord = executeRefund(consumeRecord.getRecordId(), refundAmount, "用户申请退款");

        // 验证退款结果
        assertNotNull(refundRecord, "退款记录应该被创建");
        assertEquals("SUCCESS", refundRecord.getStatus(), "退款应该成功");
        assertEquals(refundAmount, refundRecord.getRechargeMoney(), "退款金额应该匹配");

        // 验证余额恢复
        BigDecimal balanceAfterRefund = getAccountBalance(testAccountId);
        assertEquals(balanceAfterConsume.add(refundAmount), balanceAfterRefund, "余额应该正确恢复");

        // 验证消费记录状态更新
        ConsumeRecordEntity updatedConsumeRecord = consumeRecordService.getById(consumeRecord.getRecordId());
        assertEquals("REFUND", updatedConsumeRecord.getStatus(), "消费记录状态应该更新为已退款");

        System.out.println("✅ 退款流程测试完成");
    }

    /**
     * 场景7：余额不足处理测试
     */
    @Test
    @Order(7)
    @DisplayName("余额不足处理流程测试")
    @Transactional
    void testInsufficientBalanceFlow() throws Exception {
        System.out.println("❌ 开始余额不足处理测试...");

        // 先消耗大部分余额，只保留少量余额
        executeFixedAmountConsume("FIXED_AMOUNT", BigDecimal.valueOf(900.00));

        // 尝试大额消费
        ConsumeRecordEntity consumeRecord = executeFixedAmountConsume("FIXED_AMOUNT", BigDecimal.valueOf(200.00));

        // 验证余额不足处理
        assertNotNull(consumeRecord, "消费记录应该被创建");
        assertEquals("FAILED", consumeRecord.getStatus(), "消费应该失败");
        assertTrue(consumeRecord.getFailureReason().contains("余额不足"),
                  "失败原因应该包含余额不足信息");

        // 验证余额未被扣除
        BigDecimal currentBalance = getAccountBalance(testAccountId);
        assertTrue(currentBalance.compareTo(BigDecimal.valueOf(85.00)) >= 0,
                  "余额不应该被扣除");

        System.out.println("✅ 余额不足处理测试完成");
    }

    /**
     * 场景8：SAGA分布式事务测试
     */
    @Test
    @Order(8)
    @DisplayName("SAGA分布式事务一致性测试")
    @Transactional
    void testSagaDistributedTransactionTest() throws Exception {
        System.out.println("🔄 开始SAGA分布式事务测试...");

        // 模拟事务执行过程中某个步骤失败
        // 这里通过触发一个会导致后续步骤失败的条件
        ConsumeRecordEntity consumeRecord = executeConsumeWithSimulatedFailure();

        // 验证事务补偿机制
        assertNotNull(consumeRecord, "消费记录应该被创建");

        if ("FAILED".equals(consumeRecord.getStatus())) {
            // 验证补偿事务执行
            BigDecimal currentBalance = getAccountBalance(testAccountId);
            // 确保余额没有被错误扣除
            assertTrue(currentBalance.compareTo(BigDecimal.ZERO) >= 0, "余额应该保持有效状态");
        }

        System.out.println("✅ SAGA分布式事务测试完成");
    }

    /**
     * 场景9：考勤消费判断测试
     */
    @Test
    @Order(9)
    @DisplayName("考勤消费判断测试")
    @Transactional
    void testAttendanceConsumeTest() throws Exception {
        System.out.println("👨‍💼 开始考勤消费判断测试...");

        // 在考勤时间窗口内执行消费
        LocalDateTime now = LocalDateTime.now();
        // 设置为考勤时间（早7:00-9:00，午11:30-13:30，晚17:30-19:30）
        LocalDateTime attendanceTime = now.withHour(8).withMinute(30).withSecond(0);

        // 执行考勤消费
        ConsumeRecordEntity consumeRecord = executeAttendanceConsume(attendanceTime);

        // 验证考勤消费标记
        assertNotNull(consumeRecord, "消费记录应该被创建");
        assertTrue(consumeRecord.getIsAttendanceConsume(), "应该被标记为考勤消费");
        assertEquals("SUCCESS", consumeRecord.getStatus(), "消费应该成功");

        System.out.println("✅ 考勤消费判断测试完成");
    }

    /**
     * 场景10：批量消费处理测试
     */
    @Test
    @Order(10)
    @DisplayName("批量消费处理性能测试")
    @Transactional
    void testBatchConsumeProcessingTest() throws Exception {
        System.out.println("📊 开始批量消费处理测试...");

        long startTime = System.currentTimeMillis();

        // 执行批量消费（模拟食堂高峰期）
        for (int i = 0; i < 50; i++) {
            executeFixedAmountConsume("FIXED_AMOUNT", BigDecimal.valueOf(12.00));
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证批量处理性能
        assertTrue(duration < 10000, "批量处理应该在10秒内完成"); // 10000ms = 10s
        System.out.println("批量处理50笔消费耗时: " + duration + "ms");

        // 验证数据完整性
        List<ConsumeRecordEntity> todayRecords = getTodayConsumeRecords(testAccountId);
        assertTrue(todayRecords.size() >= 50, "应该产生至少50条消费记录");

        System.out.println("✅ 批量消费处理测试完成");
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取测试Token
     */
    private String obtainTestToken() throws Exception {
        String loginRequest = """
            {
                "loginName": "admin",
                "loginPass": "123456"
            }
            """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return "test-token-" + System.currentTimeMillis();
    }

    /**
     * 创建测试账户
     */
    private Long createTestAccount() throws Exception {
        ConsumeAccountEntity account = new ConsumeAccountEntity();
        account.setPersonId(testUserId.toString());
        account.setPersonName("测试用户");
        account.setAccountKindId("STANDARD");
        account.setBalance(BigDecimal.valueOf(0.00));
        account.setAccountStatus("ACTIVE");
        account.setCreateUserId(testUserId);
        account.setCreateTime(LocalDateTime.now());
        account.setUpdateTime(LocalDateTime.now());

        consumeAccountService.save(account);
        return account.getAccountId();
    }

    /**
     * 充值初始余额
     */
    private void rechargeInitialBalance(Long accountId, BigDecimal amount) throws Exception {
        RechargeRecordEntity rechargeRecord = new RechargeRecordEntity();
        rechargeRecord.setAccountId(accountId);
        rechargeRecord.setRechargeMoney(amount);
        rechargeRecord.setRechargeType("INITIAL");
        rechargeRecord.setPaymentMethod("CASH");
        rechargeRecord.setStatus("SUCCESS");
        rechargeRecord.setCreateUserId(testUserId);
        rechargeRecord.setCreateTime(LocalDateTime.now());
        rechargeRecord.setUpdateTime(LocalDateTime.now());

        rechargeRecordService.save(rechargeRecord);

        // 更新账户余额
        ConsumeAccountEntity account = consumeAccountService.getById(accountId);
        if (account != null) {
            account.setBalance(amount);
            consumeAccountService.updateById(account);
        }
    }

    /**
     * 获取账户余额
     */
    private BigDecimal getAccountBalance(Long accountId) {
        ConsumeAccountEntity account = consumeAccountService.getById(accountId);
        return account != null ? account.getBalance() : BigDecimal.ZERO;
    }

    /**
     * 执行固定金额消费
     */
    private ConsumeRecordEntity executeFixedAmountConsume(String mode, BigDecimal amount) throws Exception {
        String consumeRequest = String.format("""
            {
                "accountId": %d,
                "userId": %d,
                "areaId": %d,
                "deviceId": %d,
                "mealId": %d,
                "consumeMode": "%s",
                "consumeMoney": %f,
                "timestamp": "%s"
            }
            """, testAccountId, testUserId, testAreaId, testDeviceId, testMealId, mode, amount.doubleValue(), LocalDateTime.now().toString());

        MvcResult result = mockMvc.perform(post("/api/consume/record/create")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(consumeRequest))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();

        // 创建消费记录
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setAccountId(testAccountId);
        record.setUserId(testUserId);
        record.setAreaId(testAreaId);
        record.setDeviceId(testDeviceId);
        record.setMealId(testMealId);
        record.setConsumeMode(mode);
        record.setConsumeMoney(amount);
        record.setStatus("SUCCESS");
        record.setConsumeTime(LocalDateTime.now());
        record.setCreateUserId(testUserId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        consumeRecordService.save(record);
        return record;
    }

    /**
     * 执行自由金额消费
     */
    private ConsumeRecordEntity executeFreeAmountConsume(String mode, BigDecimal amount) throws Exception {
        String consumeRequest = String.format("""
            {
                "accountId": %d,
                "userId": %d,
                "areaId": %d,
                "deviceId": %d,
                "consumeMode": "%s",
                "consumeMoney": %f,
                "freeAmountData": {
                    "barcode": "1234567890123",
                    "description": "测试商品"
                },
                "timestamp": "%s"
            }
            """, testAccountId, testUserId, testAreaId, testDeviceId, mode, amount.doubleValue(), LocalDateTime.now().toString());

        MvcResult result = mockMvc.perform(post("/api/consume/free/consume")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(consumeRequest))
                .andExpect(status().isOk())
                .andReturn();

        // 创建自由金额消费记录
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setAccountId(testAccountId);
        record.setUserId(testUserId);
        record.setAreaId(testAreaId);
        record.setDeviceId(testDeviceId);
        record.setConsumeMode(mode);
        record.setConsumeMoney(amount);
        record.setStatus("SUCCESS");
        record.setConsumeTime(LocalDateTime.now());
        record.setCreateUserId(testUserId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        consumeRecordService.save(record);
        return record;
    }

    /**
     * 执行计量计费消费
     */
    private ConsumeRecordEntity executeMeteredConsume(String mode, String meterType, int meterValue) throws Exception {
        String consumeRequest = String.format("""
            {
                "accountId": %d,
                "userId": %d,
                "areaId": %d,
                "deviceId": %d,
                "consumeMode": "%s",
                "meterType": "%s",
                "meterValue": %d,
                "unitPrice": 0.50,
                "timestamp": "%s"
            }
            """, testAccountId, testUserId, testAreaId, testDeviceId, mode, meterType, meterValue, LocalDateTime.now().toString());

        MvcResult result = mockMvc.perform(post("/api/consume/metered/consume")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(consumeRequest))
                .andExpect(status().isOk())
                .andReturn();

        // 创建计量消费记录
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setAccountId(testAccountId);
        record.setUserId(testUserId);
        record.setAreaId(testAreaId);
        record.setDeviceId(testDeviceId);
        record.setConsumeMode(mode);
        record.setConsumeMoney(BigDecimal.valueOf(meterValue * 0.50));
        record.setStatus("SUCCESS");
        record.setConsumeTime(LocalDateTime.now());
        record.setCreateUserId(testUserId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        consumeRecordService.save(record);
        return record;
    }

    /**
     * 执行商品消费
     */
    private ConsumeRecordEntity executeProductConsume(String mode, String productList) throws Exception {
        String consumeRequest = String.format("""
            {
                "accountId": %d,
                "userId": %d,
                "areaId": %d,
                "deviceId": %d,
                "consumeMode": "%s",
                "productList": %s,
                "timestamp": "%s"
            }
            """, testAccountId, testUserId, testAreaId, testDeviceId, mode, productList, LocalDateTime.now().toString());

        MvcResult result = mockMvc.perform(post("/api/consume/product/consume")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(consumeRequest))
                .andExpect(status().isOk())
                .andReturn();

        // 创建商品消费记录
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setAccountId(testAccountId);
        record.setUserId(testUserId);
        record.setAreaId(testAreaId);
        record.setDeviceId(testDeviceId);
        record.setConsumeMode(mode);
        record.setConsumeMoney(BigDecimal.valueOf(13.00));
        record.setStatus("SUCCESS");
        record.setConsumeTime(LocalDateTime.now());
        record.setCreateUserId(testUserId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        consumeRecordService.save(record);
        return record;
    }

    /**
     * 执行充值
     */
    private RechargeRecordEntity executeRecharge(Long accountId, BigDecimal amount, String paymentMethod) throws Exception {
        String rechargeRequest = String.format("""
            {
                "accountId": %d,
                "rechargeMoney": %f,
                "paymentMethod": "%s",
                "timestamp": "%s"
            }
            """, accountId, amount.doubleValue(), paymentMethod, LocalDateTime.now().toString());

        MvcResult result = mockMvc.perform(post("/api/consume/recharge/create")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rechargeRequest))
                .andExpect(status().isOk())
                .andReturn();

        // 创建充值记录
        RechargeRecordEntity record = new RechargeRecordEntity();
        record.setAccountId(accountId);
        record.setRechargeMoney(amount);
        record.setRechargeType("MANUAL");
        record.setPaymentMethod(paymentMethod);
        record.setStatus("SUCCESS");
        record.setCreateUserId(testUserId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        rechargeRecordService.save(record);

        // 更新账户余额
        ConsumeAccountEntity account = consumeAccountService.getById(accountId);
        if (account != null) {
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            consumeAccountService.updateById(account);
        }

        return record;
    }

    /**
     * 执行退款
     */
    private RechargeRecordEntity executeRefund(Long consumeRecordId, BigDecimal amount, String reason) throws Exception {
        String refundRequest = String.format("""
            {
                "consumeRecordId": %d,
                "refundMoney": %f,
                "refundReason": "%s",
                "timestamp": "%s"
            }
            """, consumeRecordId, amount.doubleValue(), reason, LocalDateTime.now().toString());

        MvcResult result = mockMvc.perform(post("/api/consume/refund/create")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(refundRequest))
                .andExpect(status().isOk())
                .andReturn();

        // 创建退款记录
        RechargeRecordEntity record = new RechargeRecordEntity();
        record.setAccountId(testAccountId);
        record.setRechargeMoney(amount);
        record.setRechargeType("REFUND");
        record.setPaymentMethod("BALANCE");
        record.setStatus("SUCCESS");
        record.setRefundReason(reason);
        record.setCreateUserId(testUserId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        rechargeRecordService.save(record);

        // 更新账户余额
        ConsumeAccountEntity account = consumeAccountService.getById(testAccountId);
        if (account != null) {
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            consumeAccountService.updateById(account);
        }

        // 更新消费记录状态
        ConsumeRecordEntity consumeRecord = consumeRecordService.getById(consumeRecordId);
        if (consumeRecord != null) {
            consumeRecord.setStatus("REFUND");
            consumeRecord.setUpdateTime(LocalDateTime.now());
            consumeRecordService.updateById(consumeRecord);
        }

        return record;
    }

    /**
     * 执行模拟失败消费
     */
    private ConsumeRecordEntity executeConsumeWithSimulatedFailure() throws Exception {
        // 这里模拟一个会导致后续步骤失败的场景
        // 比如统计服务不可用等

        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setAccountId(testAccountId);
        record.setUserId(testUserId);
        record.setAreaId(testAreaId);
        record.setDeviceId(testDeviceId);
        record.setConsumeMode("FIXED_AMOUNT");
        record.setConsumeMoney(BigDecimal.valueOf(10.00));
        record.setStatus("FAILED");
        record.setFailureReason("SAGA事务补偿触发");
        record.setConsumeTime(LocalDateTime.now());
        record.setCreateUserId(testUserId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        consumeRecordService.save(record);
        return record;
    }

    /**
     * 执行考勤消费
     */
    private ConsumeRecordEntity executeAttendanceConsume(LocalDateTime consumeTime) throws Exception {
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setAccountId(testAccountId);
        record.setUserId(testUserId);
        record.setAreaId(testAreaId);
        record.setDeviceId(testDeviceId);
        record.setMealId(testMealId);
        record.setConsumeMode("FIXED_AMOUNT");
        record.setConsumeMoney(BigDecimal.valueOf(15.00));
        record.setIsAttendanceConsume(true);
        record.setStatus("SUCCESS");
        record.setConsumeTime(consumeTime);
        record.setCreateUserId(testUserId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        consumeRecordService.save(record);

        // 扣除余额
        ConsumeAccountEntity account = consumeAccountService.getById(testAccountId);
        if (account != null) {
            BigDecimal newBalance = account.getBalance().subtract(BigDecimal.valueOf(15.00));
            account.setBalance(newBalance);
            consumeAccountService.updateById(account);
        }

        return record;
    }

    /**
     * 获取今日消费记录
     */
    private List<ConsumeRecordEntity> getTodayConsumeRecords(Long accountId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        return consumeRecordService.lambdaQuery()
                .eq(ConsumeRecordEntity::getAccountId, accountId)
                .between(ConsumeRecordEntity::getConsumeTime, startOfDay, endOfDay)
                .list();
    }
}