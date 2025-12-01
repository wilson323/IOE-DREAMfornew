package net.lab1024.sa.admin.module.consume;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeEngine;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeResult;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.consume.service.ConsumeRecordService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消费引擎服务单元测试
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig
@ExtendWith(SpringJUnitConfig.class)
class ConsumeEngineServiceTest {

    @InjectMocks
    private ConsumeEngineService consumeEngineService;

    @Mock
    private AccountService accountService;

    @Mock
    private ConsumeRecordService consumeRecordService;

    @Mock
    private ConsumeModeEngine consumeModeEngine;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ConsumeRecordEntity testAccount;
    private ConsumeModeRequest testRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试账户
        testAccount = new ConsumeRecordEntity();
        testAccount.setAccountId(1L);
        testAccount.setUserId(1001L);
        testAccount.setBalanceAmount(new BigDecimal("1000.00"));
        testAccount.setStatus("ACTIVE");

        // 初始化测试请求
        testRequest = new ConsumeModeRequest();
        testRequest.setUserId(1001L);
        testRequest.setAmount(new BigDecimal("50.00"));
        testRequest.setDeviceId(2001L);
        testRequest.setConsumeMode("FIXED_AMOUNT");
        testRequest.setOrderNo("ORDER_" + System.currentTimeMillis());
    }

    @Test
    @DisplayName("测试正常消费流程")
    void testProcessConsume_Success() throws Exception {
        // 模拟依赖方法调用
        when(accountService.getAccountByUserId(testRequest.getUserId()))
            .thenReturn(testAccount);

        when(accountService.checkBalanceSufficient(eq(testAccount.getAccountId()), any(BigDecimal.class)))
            .thenReturn(true);

        when(accountService.hasFrozenAmount(eq(testAccount.getAccountId()), any(BigDecimal.class)))
            .thenReturn(false);

        ConsumeModeResult modeResult = ConsumeModeResult.success("SUCCESS", "消费成功")
            .setOriginalAmount(testRequest.getAmount())
            .setFinalAmount(testRequest.getAmount())
            .setModeCode("FIXED_AMOUNT");

        when(consumeModeEngine.processMode(any(ConsumeModeRequest.class)))
            .thenReturn(modeResult);

        ConsumeRecordEntity savedRecord = new ConsumeRecordEntity();
        savedRecord.setRecordId(12345L);
        when(consumeRecordService.createRecord(any(ConsumeRecordEntity.class)))
            .thenReturn(savedRecord);

        // 执行测试
        ResponseDTO<ConsumeRecordEntity> result = consumeEngineService.processConsume(testRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(12345L, result.getData().getRecordId());
        assertEquals("消费成功", result.getMsg());
    }

    @Test
        @DisplayName("测试余额不足")
        void testProcessConsume_InsufficientBalance() throws Exception {
            // 模拟余额不足
            when(accountService.getAccountByUserId(testRequest.getUserId()))
                .thenReturn(testAccount);

            when(accountService.checkBalanceSufficient(eq(testAccount.getAccountId()), any(BigDecimal.class)))
                .thenReturn(false);

            // 执行测试
            ResponseDTO<ConsumeRecordEntity> result = consumeEngineService.processConsume(testRequest);

            // 验证结果
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("账户余额不足", result.getMsg());
            assertNull(result.getData());
        }

    @Test
        @DisplayName("测试消费模式处理失败")
        void testProcessConsume_ModeProcessingFailed() throws Exception {
            // 模拟正常账户检查
            when(accountService.getAccountByUserId(testRequest.getUserId()))
                .thenReturn(testAccount);

            when(accountService.checkBalanceSufficient(eq(testAccount.getAccountId()), any(BigDecimal.class)))
                .thenReturn(true);

            when(accountService.hasFrozenAmount(eq(testAccount.getAccountId()), any(BigDecimal.class)))
                .thenReturn(false);

            // 模拟消费模式处理失败
            ConsumeModeResult modeResult = ConsumeModeResult.failure("MODE_ERROR", "消费模式处理失败");
            when(consumeModeEngine.processMode(any(ConsumeModeRequest.class)))
                .thenReturn(modeResult);

            // 执行测试
            ResponseDTO<ConsumeRecordEntity> result = consumeEngineService.processConsume(testRequest);

            // 验证结果
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("消费模式处理失败", result.getMsg());
            assertNull(result.getData());
        }

    @Test
        @DisplayName("测试重复订单处理")
        void testProcessConsume_DuplicateOrder() throws Exception {
            // 模拟重复订单检查
            when(consumeRecordService.existsByOrderNo(testRequest.getOrderNo()))
                .thenReturn(true);

            // 执行测试
            ResponseDTO<ConsumeRecordEntity> result = consumeEngineService.processConsume(testRequest);

            // 验证结果
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("订单号重复，请勿重复提交", result.getMsg());
            assertNull(result.getData());
        }

    @Test
        @DisplayName("测试账户不存在")
        void testProcessConsume_AccountNotFound() throws Exception {
            // 模拟账户不存在
            when(accountService.getAccountByUserId(testRequest.getUserId()))
                .thenReturn(null);

            // 执行测试
            ResponseDTO<ConsumeRecordEntity> result = consumeEngineService.processConsume(testRequest);

            // 验证结果
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("账户不存在或已冻结", result.getMsg());
            assertNull(result.getData());
        }

    @Test
        @DisplayName("测试账户已冻结")
        void testProcessConsume_AccountFrozen() throws Exception {
            // 模拟账户已冻结
            ConsumeRecordEntity frozenAccount = new ConsumeRecordEntity();
            frozenAccount.setStatus("FROZEN");
            when(accountService.getAccountByUserId(testRequest.getUserId()))
                .thenReturn(frozenAccount);

            // 执行测试
            ResponseDTO<ConsumeRecordEntity> result = consumeEngineService.processConsume(testRequest);

            // 验证结果
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("账户不存在或已冻结", result.getMsg());
            assertNull(result.getData());
        }

    @Test
        @DisplayName("测试参数验证")
    void testProcessConsume_InvalidParameter() {
        // 测试空请求
        ResponseDTO<ConsumeRecordEntity> result1 = consumeEngineService.processConsume(null);
        assertFalse(result1.isSuccess());
        assertEquals("请求参数不能为空", result1.getMsg());

        // 测试用户ID为空
        ConsumeModeRequest request2 = new ConsumeModeRequest();
        request2.setAmount(new BigDecimal("100.00"));
        ResponseDTO<ConsumeRecordEntity> result2 = consumeEngineService.processConsume(request2);
        assertFalse(result2.isSuccess());
        assertEquals("用户ID不能为空", result2.getMsg());

        // 测试金额为空
        ConsumeModeRequest request3 = new ConsumeModeRequest();
        request3.setUserId(1001L);
        ResponseDTO<ConsumeRecordEntity> result3 = consumeEngineService.processConsume(request3);
        assertFalse(result3.isSuccess());
        assertEquals("消费金额不能为空", result3.getMsg());

        // 测试金额小于等于0
        ConsumeModeRequest request4 = new ConsumeModeRequest();
        request4.setUserId(1001L);
        request4.setAmount(BigDecimal.ZERO);
        ResponseDTO<ConsumeRecordEntity> result4 = consumeEngineService.processConsume(request4);
        assertFalse(result4.isSuccess());
        assertEquals("消费金额必须大于0", result4.getMsg());
    }

    @Test
    @DisplayName("测试异常处理")
    void testProcessConsume_Exception() throws Exception {
        // 模拟异常
        when(accountService.getAccountByUserId(testRequest.getUserId()))
                .thenThrow(new RuntimeException("数据库连接异常"));

        // 执行测试
        ResponseDTO<ConsumeRecordEntity> result = consumeEngineService.processConsume(testRequest);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("消费处理异常"));
        assertNull(result.getData());
    }

    @Test
        @DisplayName("测试消费记录保存失败")
    void testProcessConsume_SaveRecordFailed() throws Exception {
        // 模拟正常的前置检查
        when(accountService.getAccountByUserId(testRequest.getUserId()))
                .thenReturn(testAccount);

        when(accountService.checkBalanceSufficient(eq(testAccount.getAccountId()), any(BigDecimal.class)))
                .thenReturn(true);

        when(accountService.hasFrozenAmount(eq(testAccount.getAccountId()), any(BigDecimal.class)))
                .thenReturn(false);

        ConsumeModeResult modeResult = ConsumeModeResult.success("SUCCESS", "消费成功");
        when(consumeModeEngine.processMode(any(ConsumeModeRequest.class)))
                .thenReturn(modeResult);

        // 模拟记录保存失败
        when(consumeRecordService.createRecord(any(ConsumeRecordEntity.class)))
                .thenThrow(new RuntimeException("数据库保存失败"));

        // 执行测试
        ResponseDTO<ConsumeRecordEntity> result = consumeEngineService.processConsume(testRequest);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("消费记录保存失败"));
        assertNull(result.getData());
    }

    @Test
        @DisplayName("测试余额扣减失败")
    void testProcessConsume_DeductBalanceFailed() throws Exception {
        // 模拟正常的前置检查
        when(accountService.getAccountByUserId(testRequest.getUserId()))
                .thenReturn(testAccount);

        when(accountService.checkBalanceSufficient(eq(testAccount.getAccountId()), any(BigDecimal.class)))
                .thenReturn(true);

        when(accountService.hasFrozenAmount(eq(testAccount.getAccountId()), any(BigDecimal.class)))
                .thenReturn(false);

        ConsumeModeResult modeResult = ConsumeModeResult.success("SUCCESS", "消费成功")
                .setOriginalAmount(testRequest.getAmount())
                .setFinalAmount(testRequest.getAmount())
                .setModeCode("FIXED_AMOUNT");

        when(consumeModeEngine.processMode(any(ConsumeModeRequest.class)))
                .thenReturn(modeResult);

        ConsumeRecordEntity savedRecord = new ConsumeRecordEntity();
        savedRecord.setRecordId(12345L);
        when(consumeRecordService.createRecord(any(ConsumeRecordEntity.class)))
                .thenReturn(savedRecord);

        // 模拟余额扣减失败
        when(accountService.deductBalance(eq(testAccount.getAccountId()), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("余额扣减失败"));

        // 执行测试
        ResponseDTO<ConsumeRecordEntity> result = consumeEngineService.processConsume(testRequest);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("余额扣减失败"));
        assertNull(result.getData());
    }
}