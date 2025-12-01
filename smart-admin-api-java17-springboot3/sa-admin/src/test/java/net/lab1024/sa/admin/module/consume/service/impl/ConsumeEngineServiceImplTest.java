/*
 * 核心消费引擎服务单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.consume.service.ConsumeEngineService;
import net.lab1024.sa.base.common.exception.SmartException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 核心消费引擎服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("核心消费引擎服务单元测试")
class ConsumeEngineServiceImplTest {

    @Mock
    private AccountService accountService;

    @Mock
    private ConsumeRecordDao consumeRecordDao;

    @InjectMocks
    private ConsumeEngineServiceImpl consumeEngineService;

    private AccountEntity testAccount;
    private ConsumeRequest testRequest;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // 创建测试账户
        testAccount = new AccountEntity();
        testAccount.setAccountId(1L);
        testAccount.setPersonId(1001L);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setFrozenAmount(BigDecimal.ZERO);
        testAccount.setCreditLimit(BigDecimal.ZERO);
        testAccount.setSingleLimit(new BigDecimal("500.00"));
        testAccount.setDailyLimit(new BigDecimal("2000.00"));
        testAccount.setMonthlyLimit(new BigDecimal("10000.00"));
        testAccount.setStatus("ACTIVE");

        // 创建测试消费请求
        testRequest = ConsumeRequest.builder()
                .personId(1001L)
                .personName("测试用户")
                .accountId(1L)
                .deviceId(2001L)
                .deviceName("测试设备")
                .deviceNo("DEV001")
                .deviceType("POS机")
                .consumptionMode("STANDARD")
                .amount(new BigDecimal("100.00"))
                .payMethod("BALANCE")
                .regionId("R001")
                .regionName("测试区域")
                .orderNo("TEST_ORDER_001")
                .clientIp("192.168.1.100")
                .source("WEB")
                .remark("测试消费")
                .build();
    }

    @Test
    @DisplayName("测试正常消费流程 - 成功")
    void testProcessConsume_Success() {
        // Arrange
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);
        when(accountService.deductBalance(eq(1L), eq(new BigDecimal("100.00")), eq("TEST_ORDER_001")))
                .thenReturn(true);
        when(accountService.getTodayConsumeAmount(1001L)).thenReturn(BigDecimal.ZERO);
        when(accountService.getMonthlyConsumeAmount(1001L)).thenReturn(BigDecimal.ZERO);
        when(consumeRecordDao.insert(any(ConsumeRecordEntity.class))).thenReturn(1);

        // Act
        ConsumeResult result = consumeEngineService.processConsume(testRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getResultCode());
        assertEquals("消费成功", result.getMessage());
        assertEquals(new BigDecimal("100.00"), result.getActualAmount());
        assertEquals("TEST_ORDER_001", result.getOrderNo());

        // 验证调用
        verify(accountService, times(1)).getByPersonId(1001L);
        verify(accountService, times(1)).deductBalance(1L, new BigDecimal("100.00"), "TEST_ORDER_001");
        verify(consumeRecordDao, times(1)).insert(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试消费流程 - 账户不存在")
    void testProcessConsume_AccountNotFound() {
        // Arrange
        when(accountService.getByPersonId(1001L)).thenReturn(null);

        // Act
        ConsumeResult result = consumeEngineService.processConsume(testRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("账户不存在", result.getMessage());

        // 验证不会调用扣减余额
        verify(accountService, never()).deductBalance(anyLong(), any(BigDecimal.class), anyString());
        verify(consumeRecordDao, never()).insert(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试消费流程 - 账户状态异常")
    void testProcessConsume_AccountStatusInvalid() {
        // Arrange
        testAccount.setStatus("FROZEN");
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);

        // Act
        ConsumeResult result = consumeEngineService.processConsume(testRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("账户状态异常"));

        // 验证不会调用扣减余额
        verify(accountService, never()).deductBalance(anyLong(), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("测试消费流程 - 余额不足")
    void testProcessConsume_InsufficientBalance() {
        // Arrange
        testAccount.setBalance(new BigDecimal("50.00")); // 余额小于消费金额
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);

        // Act
        ConsumeResult result = consumeEngineService.processConsume(testRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("INSUFFICIENT_BALANCE", result.getErrorCode());
        assertTrue(result.getMessage().contains("余额不足"));

        // 验证不会调用扣减余额
        verify(accountService, never()).deductBalance(anyLong(), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("测试消费流程 - 超出单次限额")
    void testProcessConsume_ExceedSingleLimit() {
        // Arrange
        testAccount.setSingleLimit(new BigDecimal("50.00")); // 单次限额小于消费金额
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);

        // Act
        ConsumeResult result = consumeEngineService.processConsume(testRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("LIMIT_EXCEEDED", result.getErrorCode());
        assertEquals("超出消费限额", result.getMessage());

        // 验证不会调用扣减余额
        verify(accountService, never()).deductBalance(anyLong(), any(BigDecimal.class), anyString());
    }

    @Test
    @DisplayName("测试消费流程 - 幂等性检查 - 重复订单")
    void testProcessConsume_IdempotencyCheck() {
        // Arrange
        ConsumeRecordEntity existingRecord = new ConsumeRecordEntity();
        existingRecord.setRecordId(123L);
        existingRecord.setOrderNo("TEST_ORDER_001");
        existingRecord.setStatus("SUCCESS");
        existingRecord.setAmount(new BigDecimal("100.00"));
        existingRecord.setBalanceBefore(new BigDecimal("1000.00"));
        existingRecord.setBalanceAfter(new BigDecimal("900.00"));

        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);
        when(consumeRecordDao.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.class)))
                .thenReturn(existingRecord);

        // Act
        ConsumeResult result = consumeEngineService.processConsume(testRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(Long.valueOf(123L), result.getRecordId());
        assertEquals("TEST_ORDER_001", result.getOrderNo());

        // 验证不会重复扣减余额
        verify(accountService, never()).deductBalance(anyLong(), any(BigDecimal.class), anyString());
        verify(consumeRecordDao, never()).insert(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试消费流程 - 扣减余额失败")
    void testProcessConsume_DeductBalanceFailure() {
        // Arrange
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);
        when(accountService.deductBalance(eq(1L), eq(new BigDecimal("100.00")), eq("TEST_ORDER_001")))
                .thenReturn(false);

        // Act
        ConsumeResult result = consumeEngineService.processConsume(testRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("余额扣减失败", result.getMessage());

        // 验证不会创建消费记录
        verify(consumeRecordDao, never()).insert(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试消费流程 - 记录创建失败，余额补偿")
    void testProcessConsume_RecordInsertFailure() {
        // Arrange
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);
        when(accountService.deductBalance(eq(1L), eq(new BigDecimal("100.00")), eq("TEST_ORDER_001")))
                .thenReturn(true);
        when(accountService.addBalance(eq(1L), eq(new BigDecimal("100.00")), eq("COMPENSATE_TEST_ORDER_001")))
                .thenReturn(true);
        when(consumeRecordDao.insert(any(ConsumeRecordEntity.class))).thenReturn(0); // 插入失败

        // Act
        ConsumeResult result = consumeEngineService.processConsume(testRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("余额扣减失败", result.getMessage());

        // 验证余额补偿被调用
        verify(accountService, times(1)).addBalance(1L, new BigDecimal("100.00"), "COMPENSATE_TEST_ORDER_001");
    }

    @Test
    @DisplayName("测试查询消费结果 - 成功")
    void testQueryConsumeResult_Success() {
        // Arrange
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setRecordId(123L);
        record.setOrderNo("TEST_ORDER_001");
        record.setStatus("SUCCESS");
        record.setAmount(new BigDecimal("100.00"));
        record.setBalanceBefore(new BigDecimal("1000.00"));
        record.setBalanceAfter(new BigDecimal("900.00"));
        record.setConsumeTime(LocalDateTime.now());

        when(consumeRecordDao.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.class)))
                .thenReturn(record);

        // Act
        ConsumeResult result = consumeEngineService.queryConsumeResult("TEST_ORDER_001");

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(Long.valueOf(123L), result.getRecordId());
        assertEquals("TEST_ORDER_001", result.getOrderNo());
        assertEquals(new BigDecimal("100.00"), result.getActualAmount());
    }

    @Test
    @DisplayName("测试查询消费结果 - 订单不存在")
    void testQueryConsumeResult_OrderNotFound() {
        // Arrange
        when(consumeRecordDao.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.class)))
                .thenReturn(null);

        // Act
        ConsumeResult result = consumeEngineService.queryConsumeResult("NONEXISTENT_ORDER");

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("NOT_FOUND", result.getErrorCode());
        assertEquals("订单不存在", result.getMessage());
    }

    @Test
    @DisplayName("测试查询消费结果 - 空订单号")
    void testQueryConsumeResult_EmptyOrderNo() {
        // Act
        ConsumeResult result = consumeEngineService.queryConsumeResult("");

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("INVALID_PARAM", result.getErrorCode());
        assertEquals("订单号不能为空", result.getMessage());
    }

    @Test
    @DisplayName("测试权限检查 - 成功")
    void testCheckConsumePermission_Success() {
        // Arrange
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);

        // Act
        boolean result = consumeEngineService.checkConsumePermission(1001L, 2001L, "R001");

        // Assert
        assertTrue(result);
        verify(accountService, times(1)).getByPersonId(1001L);
    }

    @Test
    @DisplayName("测试权限检查 - 账户不存在")
    void testCheckConsumePermission_AccountNotFound() {
        // Arrange
        when(accountService.getByPersonId(1001L)).thenReturn(null);

        // Act
        boolean result = consumeEngineService.checkConsumePermission(1001L, 2001L, "R001");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试权限检查 - 账户状态异常")
    void testCheckConsumePermission_AccountStatusInvalid() {
        // Arrange
        testAccount.setStatus("FROZEN");
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);

        // Act
        boolean result = consumeEngineService.checkConsumePermission(1001L, 2001L, "R001");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试限额验证 - 成功")
    void testValidateConsumeLimit_Success() {
        // Arrange
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);
        when(accountService.getTodayConsumeAmount(1001L)).thenReturn(BigDecimal.ZERO);
        when(accountService.getMonthlyConsumeAmount(1001L)).thenReturn(BigDecimal.ZERO);

        // Act
        boolean result = consumeEngineService.validateConsumeLimit(1001L, new BigDecimal("100.00"));

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("测试限额验证 - 超出单次限额")
    void testValidateConsumeLimit_ExceedSingleLimit() {
        // Arrange
        testAccount.setSingleLimit(new BigDecimal("50.00"));
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);

        // Act
        boolean result = consumeEngineService.validateConsumeLimit(1001L, new BigDecimal("100.00"));

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试限额验证 - 超出日度限额")
    void testValidateConsumeLimit_ExceedDailyLimit() {
        // Arrange
        testAccount.setDailyLimit(new BigDecimal("500.00"));
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);
        when(accountService.getTodayConsumeAmount(1001L)).thenReturn(new BigDecimal("450.00"));

        // Act
        boolean result = consumeEngineService.validateConsumeLimit(1001L, new BigDecimal("100.00"));

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试限额验证 - 超出月度限额")
    void testValidateConsumeLimit_ExceedMonthlyLimit() {
        // Arrange
        testAccount.setMonthlyLimit(new BigDecimal("5000.00"));
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);
        when(accountService.getTodayConsumeAmount(1001L)).thenReturn(BigDecimal.ZERO);
        when(accountService.getMonthlyConsumeAmount(1001L)).thenReturn(new BigDecimal("4950.00"));

        // Act
        boolean result = consumeEngineService.validateConsumeLimit(1001L, new BigDecimal("100.00"));

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试订单号生成")
    void testGenerateOrderNo() {
        // 使用反射调用私有方法进行测试
        ReflectionTestUtils.invokeMethod(consumeEngineService, "generateOrderNo");
    }

    @Test
    @DisplayName("测试消费请求验证 - 基本信息完整")
    void testConsumeRequest_Valid() {
        // Act
        boolean result = testRequest.isValid();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("测试消费请求验证 - 缺少必要信息")
    void testConsumeRequest_Invalid_MissingPersonId() {
        // Arrange
        testRequest.setPersonId(null);

        // Act
        boolean result = testRequest.isValid();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试消费请求验证 - 缺少消费模式")
    void testConsumeRequest_Invalid_MissingConsumptionMode() {
        // Arrange
        testRequest.setConsumptionMode(null);

        // Act
        boolean result = testRequest.isValid();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试消费请求 - 支付信息完整")
    void testConsumeRequest_HasPaymentInfo() {
        // Act
        boolean result = testRequest.hasPaymentInfo();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("测试消费请求 - 缺少支付信息")
    void testConsumeRequest_NoPaymentInfo() {
        // Arrange
        testRequest.setPayMethod(null);

        // Act
        boolean result = testRequest.hasPaymentInfo();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试消费请求 - 获取客户端信息")
    void testConsumeRequest_GetClientInfo() {
        // Act
        String clientInfo = testRequest.getClientInfo();

        // Assert
        assertNotNull(clientInfo);
        assertTrue(clientInfo.contains("192.168.1.100"));
        assertTrue(clientInfo.contains("WEB"));
        assertTrue(clientInfo.contains("测试设备"));
        assertTrue(clientInfo.contains("DEV001"));
    }

    @Test
    @DisplayName("测试并发安全性 - 多线程消费")
    void testConsume_ConcurrentSafety() throws InterruptedException {
        // Arrange
        when(accountService.getByPersonId(1001L)).thenReturn(testAccount);
        when(accountService.deductBalance(eq(1L), any(BigDecimal.class), anyString()))
                .thenReturn(true);
        when(accountService.getTodayConsumeAmount(1001L)).thenReturn(BigDecimal.ZERO);
        when(accountService.getMonthlyConsumeAmount(1001L)).thenReturn(BigDecimal.ZERO);
        when(consumeRecordDao.insert(any(ConsumeRecordEntity.class))).thenReturn(1);

        // 创建10个并发线程
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    ConsumeRequest request = ConsumeRequest.builder()
                            .personId(1001L)
                            .deviceId(2001L)
                            .consumptionMode("STANDARD")
                            .amount(new BigDecimal("10.00"))
                            .payMethod("BALANCE")
                            .orderNo("CONCURRENT_ORDER_" + index)
                            .build();

                    ConsumeResult result = consumeEngineService.processConsume(request);
                    results[index] = result.isSuccess();
                } catch (Exception e) {
                    results[index] = false;
                }
            });
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        int successCount = 0;
        for (boolean result : results) {
            if (result) successCount++;
        }
        assertTrue("至少应该有一笔消费成功", successCount > 0);
    }
}