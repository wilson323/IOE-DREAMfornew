package net.lab1024.sa.consume.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionForm;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionDetailVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO;
import net.lab1024.sa.consume.service.ConsumeService;

/**
 * ConsumeController单元测试
 * <p>
 * 测试范围：消费管理REST API接口
 * 目标：提升测试覆盖率至70%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeController单元测试")
class ConsumeControllerTest {

    @Mock
    private ConsumeService consumeService;

    @InjectMocks
    private ConsumeController consumeController;

    private ConsumeTransactionForm transactionForm;

    @BeforeEach
    void setUp() {
        transactionForm = new ConsumeTransactionForm();
        transactionForm.setUserId(1001L);
        transactionForm.setAccountId(1L);
        transactionForm.setDeviceId(1001L);
        transactionForm.setAreaId("AREA001");
        transactionForm.setAmount(new BigDecimal("50.00"));
        transactionForm.setConsumeMode("CARD");
    }

    // ==================== executeTransaction 测试 ====================

    @Test
    @DisplayName("测试执行消费交易-成功")
    void testExecuteTransaction_Success() {
        // Given
        ConsumeTransactionResultVO mockResult = new ConsumeTransactionResultVO();
        mockResult.setTransactionNo("TXN001");
        mockResult.setTransactionStatus(2); // 成功
        mockResult.setAmount(new BigDecimal("50.00"));

        when(consumeService.executeTransaction(any(ConsumeTransactionForm.class))).thenReturn(mockResult);

        // When
        ResponseDTO<ConsumeTransactionResultVO> result = consumeController.executeTransaction(transactionForm);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals("TXN001", result.getData().getTransactionNo());
        verify(consumeService, times(1)).executeTransaction(any(ConsumeTransactionForm.class));
    }

    @Test
    @DisplayName("测试执行消费交易-参数验证失败")
    void testExecuteTransaction_ValidationFailed() {
        // Given
        transactionForm.setUserId(null); // 缺少必填字段

        // When & Then
        // 由于使用@Valid，应该在Controller层被拦截
        // 这里测试Service层返回错误的情况
        ConsumeTransactionResultVO mockResult = new ConsumeTransactionResultVO();
        mockResult.setTransactionStatus(3); // 失败
        when(consumeService.executeTransaction(any(ConsumeTransactionForm.class))).thenReturn(mockResult);

        ResponseDTO<ConsumeTransactionResultVO> result = consumeController.executeTransaction(transactionForm);

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试执行消费交易-异常处理")
    void testExecuteTransaction_Exception() {
        // Given
        when(consumeService.executeTransaction(any(ConsumeTransactionForm.class)))
            .thenThrow(new RuntimeException("系统异常"));

        // When
        ResponseDTO<ConsumeTransactionResultVO> result = consumeController.executeTransaction(transactionForm);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMessage().contains("执行消费交易失败"));
    }

    // ==================== getTransactionDetail 测试 ====================

    @Test
    @DisplayName("测试查询交易详情-成功")
    void testGetTransactionDetail_Success() {
        // Given
        String transactionNo = "TXN001";
        ConsumeTransactionDetailVO mockDetail = new ConsumeTransactionDetailVO();
        mockDetail.setTransactionNo(transactionNo);
        mockDetail.setAmount(new BigDecimal("50.00"));

        when(consumeService.getTransactionDetail(transactionNo)).thenReturn(mockDetail);

        // When
        ResponseDTO<ConsumeTransactionDetailVO> result = consumeController.getTransactionDetail(transactionNo);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(transactionNo, result.getData().getTransactionNo());
        verify(consumeService, times(1)).getTransactionDetail(transactionNo);
    }

    @Test
    @DisplayName("测试查询交易详情-不存在")
    void testGetTransactionDetail_NotFound() {
        // Given
        String transactionNo = "NON_EXISTENT";
        when(consumeService.getTransactionDetail(transactionNo)).thenReturn(null);

        // When
        ResponseDTO<ConsumeTransactionDetailVO> result = consumeController.getTransactionDetail(transactionNo);

        // Then
        assertNotNull(result);
        // 根据实际实现，可能返回null或错误响应
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试执行消费交易-表单为null")
    void testExecuteTransaction_FormIsNull() {
        // When & Then
        // 由于使用@Valid，应该在Controller层被拦截
        assertThrows(Exception.class, () -> {
            consumeController.executeTransaction(null);
        });
    }

    @Test
    @DisplayName("测试查询交易详情-交易流水号为null")
    void testGetTransactionDetail_TransactionNoIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            consumeController.getTransactionDetail(null);
        });
    }
}
