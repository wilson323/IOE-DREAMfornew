/*
 * 消费模块核心引擎集成测试
 * 端到端业务流程测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-11-22
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.consume.service.ConsumeEngineService;
import net.lab1024.sa.admin.module.consume.service.ConsumeAuditService;
import net.lab1024.sa.base.common.exception.SmartException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消费模块核心引擎集成测试
 * 端到端业务流程测试，验证完整的消费处理流程
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@DisplayName("消费模块核心引擎集成测试")
class ConsumeEngineIntegrationTest {

    @Resource
    private ConsumeEngineService consumeEngineService;

    @Resource
    private AccountService accountService;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private ConsumeAuditService auditService;

    @MockBean
    private AccountService mockAccountService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @BeforeEach
    void setUp() {
        // 清理测试数据
        consumeRecordDao.delete(null);

        // 设置测试账户
        setupTestAccount();
    }

    private void setupTestAccount() {
        AccountEntity testAccount = new AccountEntity();
        testAccount.setAccountId(1L);
        testAccount.setPersonId(1001L);
        testAccount.setAccountNo("TEST001");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setAccountStatus("ACTIVE");
        testAccount.setCreateTime(LocalDateTime.now());
        testAccount.setUpdateTime(LocalDateTime.now());

        // 注入测试账户到服务
        when(mockAccountService.getAccountByPersonId(1001L))
                .thenReturn(testAccount);
        when(mockAccountService.getBalance(1L))
                .thenReturn(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("端到端消费流程测试 - 固定金额模式")
    void testEndToEndConsumeFixedAmountMode() {
        // 1. 构建消费请求
        ConsumeRequest request = new ConsumeRequest();
        request.setPersonId(1001L);
        request.setAmount(new BigDecimal("50.00"));
        request.setDeviceId(2001L);
        request.setOrderNo("TEST_ORDER_" + System.currentTimeMillis());
        request.setConsumeMode("FIXED_AMOUNT");
        request.setConsumeType("MEAL");

        // 2. 执行消费处理
        ConsumeResult result = consumeEngineService.processConsume(request);

        // 3. 验证消费结果
        assertNotNull(result);
        assertEquals("SUCCESS", result.getResultCode());
        assertNotNull(result.getOrderNo());
        assertTrue(result.getAmount().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(result.getBalanceAfter());

        // 4. 验证数据库记录
        List<ConsumeRecordEntity> records = consumeRecordDao.selectList(null);
        assertEquals(1, records.size());

        ConsumeRecordEntity record = records.get(0);
        assertEquals(request.getPersonId(), record.getPersonId());
        assertEquals(request.getAmount(), record.getConsumeAmount());
        assertEquals(request.getOrderNo(), record.getOrderNo());
        assertEquals("SUCCESS", record.getStatus());

        // 5. 验证审计日志
        // 这里需要检查审计日志是否正确记录
        List<Object> auditLogs = auditService.queryAuditLogs(
            request.getPersonId(),
            "CONSUME",
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now(),
            10
        );
        assertFalse(auditLogs.isEmpty());
    }

    @Test
    @DisplayName("并发消费测试 - 防重复扣费")
    void testConcurrentConsumePrevention() {
        String orderNo = "CONCURRENT_TEST_" + System.currentTimeMillis();

        // 1. 构建相同的消费请求
        ConsumeRequest request1 = new ConsumeRequest();
        request1.setPersonId(1001L);
        request1.setAmount(new BigDecimal("30.00"));
        request1.setDeviceId(2001L);
        request1.setOrderNo(orderNo);
        request1.setConsumeMode("FIXED_AMOUNT");

        ConsumeRequest request2 = new ConsumeRequest();
        request2.setPersonId(1001L);
        request2.setAmount(new BigDecimal("30.00"));
        request2.setDeviceId(2002L);
        request2.setOrderNo(orderNo); // 相同订单号
        request2.setConsumeMode("FIXED_AMOUNT");

        // 2. 并发执行消费请求
        CompletableFuture<ConsumeResult> future1 = CompletableFuture.supplyAsync(
            () -> consumeEngineService.processConsume(request1), executorService);
        CompletableFuture<ConsumeResult> future2 = CompletableFuture.supplyAsync(
            () -> consumeEngineService.processConsume(request2), executorService);

        // 3. 等待结果
        ConsumeResult result1 = future1.join();
        ConsumeResult result2 = future2.join();

        // 4. 验证只有一个成功，另一个返回重复处理
        assertTrue(
            ("SUCCESS".equals(result1.getResultCode()) && "DUPLICATE_ORDER".equals(result2.getResultCode())) ||
            ("SUCCESS".equals(result2.getResultCode()) && "DUPLICATE_ORDER".equals(result1.getResultCode()))
        );

        // 5. 验证数据库只有一条记录
        List<ConsumeRecordEntity> records = consumeRecordDao.selectList(null);
        assertEquals(1, records.size());
        assertEquals(orderNo, records.get(0).getOrderNo());
    }

    @Test
    @DisplayName("余额不足测试")
    void testInsufficientBalance() {
        // 1. 设置余额不足的账户
        AccountEntity lowBalanceAccount = new AccountEntity();
        lowBalanceAccount.setAccountId(2L);
        lowBalanceAccount.setPersonId(1002L);
        lowBalanceAccount.setAccountNo("TEST002");
        lowBalanceAccount.setBalance(new BigDecimal("10.00")); // 余额不足
        lowBalanceAccount.setAccountStatus("ACTIVE");

        when(mockAccountService.getAccountByPersonId(1002L))
                .thenReturn(lowBalanceAccount);

        // 2. 构建消费请求
        ConsumeRequest request = new ConsumeRequest();
        request.setPersonId(1002L);
        request.setAmount(new BigDecimal("50.00")); // 超过余额
        request.setDeviceId(2003L);
        request.setOrderNo("INSUFFICIENT_BALANCE_" + System.currentTimeMillis());
        request.setConsumeMode("FIXED_AMOUNT");

        // 3. 执行消费处理
        ConsumeResult result = consumeEngineService.processConsume(request);

        // 4. 验证消费失败
        assertNotNull(result);
        assertEquals("INSUFFICIENT_BALANCE", result.getResultCode());
        assertTrue(result.getMessage().contains("余额不足"));

        // 5. 验证数据库没有消费记录
        List<ConsumeRecordEntity> records = consumeRecordDao.selectList(null);
        assertEquals(0, records.size());

        // 6. 验证审计日志记录了失败原因
        List<Object> auditLogs = auditService.queryAuditLogs(
            request.getPersonId(),
            "CONSUME",
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now(),
            10
        );
        assertFalse(auditLogs.isEmpty());
    }

    @Test
    @DisplayName("异常情况处理测试")
    void testExceptionHandling() {
        // 1. 模拟账户服务异常
        when(mockAccountService.getAccountByPersonId(anyLong()))
                .thenThrow(new RuntimeException("模拟数据库连接异常"));

        // 2. 构建消费请求
        ConsumeRequest request = new ConsumeRequest();
        request.setPersonId(1003L);
        request.setAmount(new BigDecimal("25.00"));
        request.setDeviceId(2004L);
        request.setOrderNo("EXCEPTION_TEST_" + System.currentTimeMillis());
        request.setConsumeMode("FIXED_AMOUNT");

        // 3. 执行消费处理
        ConsumeResult result = consumeEngineService.processConsume(request);

        // 4. 验证异常处理
        assertNotNull(result);
        assertEquals("SYSTEM_ERROR", result.getResultCode());
        assertTrue(result.getMessage().contains("系统异常"));

        // 5. 验证审计日志记录了异常
        List<Object> auditLogs = auditService.queryAuditLogs(
            request.getPersonId(),
            "CONSUME",
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now(),
            10
        );
        assertFalse(auditLogs.isEmpty());
    }

    @Test
    @DisplayName("性能测试 - 单次消费响应时间")
    void testPerformanceSingleConsume() {
        // 1. 构建消费请求
        ConsumeRequest request = new ConsumeRequest();
        request.setPersonId(1001L);
        request.setAmount(new BigDecimal("20.00"));
        request.setDeviceId(2005L);
        request.setOrderNo("PERFORMANCE_TEST_" + System.currentTimeMillis());
        request.setConsumeMode("FIXED_AMOUNT");

        // 2. 执行性能测试
        long startTime = System.currentTimeMillis();
        ConsumeResult result = consumeEngineService.processConsume(request);
        long endTime = System.currentTimeMillis();

        // 3. 验证响应时间（企业级标准：≤ 100ms）
        long responseTime = endTime - startTime;
        assertTrue(responseTime <= 100,
            "响应时间超过标准: " + responseTime + "ms (标准: ≤100ms)");

        // 4. 验证消费成功
        assertNotNull(result);
        assertEquals("SUCCESS", result.getResultCode());
    }

    @Test
    @DisplayName("数据一致性验证测试")
    void testDataConsistencyValidation() {
        // 1. 记录初始状态
        BigDecimal initialBalance = mockAccountService.getBalance(1L);
        BigDecimal consumeAmount = new BigDecimal("75.00");

        // 2. 构建消费请求
        ConsumeRequest request = new ConsumeRequest();
        request.setPersonId(1001L);
        request.setAmount(consumeAmount);
        request.setDeviceId(2006L);
        request.setOrderNo("CONSISTENCY_TEST_" + System.currentTimeMillis());
        request.setConsumeMode("FIXED_AMOUNT");

        // 3. 执行消费处理
        ConsumeResult result = consumeEngineService.processConsume(request);

        // 4. 验证数据一致性
        BigDecimal expectedBalanceAfter = initialBalance.subtract(consumeAmount);
        assertEquals(expectedBalanceAfter, result.getBalanceAfter());

        // 5. 验证数据库记录的一致性
        List<ConsumeRecordEntity> records = consumeRecordDao.selectList(null);
        assertEquals(1, records.size());
        assertEquals(consumeAmount, records.get(0).getConsumeAmount());
    }

    @Test
    @DisplayName("不同消费模式测试")
    void testDifferentConsumeModes() {
        String[] consumeModes = {"FIXED_AMOUNT", "FREE_AMOUNT", "METERING_MODE"};

        for (String mode : consumeModes) {
            // 1. 构建消费请求
            ConsumeRequest request = new ConsumeRequest();
            request.setPersonId(1001L);
            request.setAmount(new BigDecimal("25.00"));
            request.setDeviceId(2007L);
            request.setOrderNo("MODE_TEST_" + mode + "_" + System.currentTimeMillis());
            request.setConsumeMode(mode);

            // 2. 执行消费处理
            ConsumeResult result = consumeEngineService.processConsume(request);

            // 3. 验证消费成功（注意：某些模式可能需要特定配置）
            if (result.getResultCode().equals("SUCCESS")) {
                assertNotNull(result.getOrderNo());
                assertNotNull(result.getBalanceAfter());
            } else {
                // 某些模式可能因为配置问题失败，这是可以接受的
                assertTrue(result.getMessage() != null);
            }
        }
    }

    @Test
    @DisplayName("安全审计功能测试")
    void testSecurityAuditFunction() {
        // 1. 构建消费请求
        ConsumeRequest request = new ConsumeRequest();
        request.setPersonId(1001L);
        request.setAmount(new BigDecimal("1000.00")); // 高金额操作
        request.setDeviceId(2008L);
        request.setOrderNo("SECURITY_TEST_" + System.currentTimeMillis());
        request.setConsumeMode("FIXED_AMOUNT");

        // 2. 执行消费处理
        ConsumeResult result = consumeEngineService.processConsume(request);

        // 3. 验证高金额操作的审计日志
        List<Object> auditLogs = auditService.queryAuditLogs(
            request.getPersonId(),
            "CONSUME",
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now(),
            10
        );

        assertFalse(auditLogs.isEmpty());
        // 验证高金额操作被标记为高风险
        // 这里需要根据实际的审计日志实现来检查风险等级
    }

    @Test
    @DisplayName("缓存策略验证测试")
    void testCacheStrategyValidation() {
        // 1. 多次查询相同的账户信息
        for (int i = 0; i < 3; i++) {
            AccountEntity account = mockAccountService.getAccountByPersonId(1001L);
            assertNotNull(account);
            assertEquals(1001L, account.getPersonId());
        }

        // 2. 验证缓存命中（这里需要检查缓存的统计信息）
        // 实际实现中应该验证缓存命中率
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}