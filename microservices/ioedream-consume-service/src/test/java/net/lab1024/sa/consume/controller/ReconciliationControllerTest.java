package net.lab1024.sa.consume.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.service.consistency.ReconciliationService;

/**
 * ReconciliationController单元测试
 * <p>
 * 测试范围：对账管理REST API接口
 * 目标：提升测试覆盖率至70%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReconciliationController单元测试")
class ReconciliationControllerTest {

    @Mock
    private ReconciliationService reconciliationService;

    @InjectMocks
    private ReconciliationController reconciliationController;

    // ==================== performDailyReconciliation 测试 ====================

    @Test
    @DisplayName("测试执行日终对账-成功")
    void testPerformDailyReconciliation_Success() {
        // Given
        LocalDate reconcileDate = LocalDate.now().minusDays(1);
        ReconciliationService.ReconciliationResult mockResult = new ReconciliationService.ReconciliationResult();
        mockResult.setStatus("SUCCESS");
        mockResult.setAccountCount(100);

        when(reconciliationService.performDailyReconciliation(any(LocalDate.class))).thenReturn(mockResult);

        // When
        ResponseDTO<ReconciliationService.ReconciliationResult> result =
            reconciliationController.performDailyReconciliation(reconcileDate);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals("SUCCESS", result.getData().getStatus());
        verify(reconciliationService, times(1)).performDailyReconciliation(any(LocalDate.class));
    }

    @Test
    @DisplayName("测试执行日终对账-日期为null（使用默认日期）")
    void testPerformDailyReconciliation_DateIsNull() {
        // Given
        ReconciliationService.ReconciliationResult mockResult = new ReconciliationService.ReconciliationResult();
        mockResult.setStatus("SUCCESS");

        when(reconciliationService.performDailyReconciliation(any(LocalDate.class))).thenReturn(mockResult);

        // When
        ResponseDTO<ReconciliationService.ReconciliationResult> result =
            reconciliationController.performDailyReconciliation(null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        // 应该使用昨天的日期
        verify(reconciliationService, times(1)).performDailyReconciliation(any(LocalDate.class));
    }

    @Test
    @DisplayName("测试执行日终对账-异常处理")
    void testPerformDailyReconciliation_Exception() {
        // Given
        LocalDate reconcileDate = LocalDate.now().minusDays(1);
        when(reconciliationService.performDailyReconciliation(any(LocalDate.class)))
            .thenThrow(new RuntimeException("对账失败"));

        // When
        ResponseDTO<ReconciliationService.ReconciliationResult> result =
            reconciliationController.performDailyReconciliation(reconcileDate);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMessage().contains("执行日终对账失败"));
    }

    // ==================== performRealtimeReconciliation 测试 ====================

    @Test
    @DisplayName("测试执行实时对账-指定账户")
    void testPerformRealtimeReconciliation_WithAccountId() {
        // Given
        Long accountId = 1L;
        ReconciliationService.ReconciliationResult mockResult = new ReconciliationService.ReconciliationResult();
        mockResult.setStatus("SUCCESS");

        when(reconciliationService.performRealTimeReconciliation(accountId)).thenReturn(mockResult);

        // When
        ResponseDTO<ReconciliationService.ReconciliationResult> result =
            reconciliationController.performRealtimeReconciliation(accountId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(reconciliationService, times(1)).performRealTimeReconciliation(accountId);
    }

    @Test
    @DisplayName("测试执行实时对账-所有账户")
    void testPerformRealtimeReconciliation_AllAccounts() {
        // Given
        ReconciliationService.ReconciliationResult mockResult = new ReconciliationService.ReconciliationResult();
        mockResult.setStatus("SUCCESS");

        when(reconciliationService.performRealTimeReconciliation(null)).thenReturn(mockResult);

        // When
        ResponseDTO<ReconciliationService.ReconciliationResult> result =
            reconciliationController.performRealtimeReconciliation(null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(reconciliationService, times(1)).performRealTimeReconciliation(null);
    }

    // ==================== getReconciliationHistory 测试 ====================

    @Test
    @DisplayName("测试查询对账历史-成功")
    void testQueryReconciliationHistory_Success() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 20;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        ReconciliationService.ReconciliationHistoryResult mockHistoryResult =
            new ReconciliationService.ReconciliationHistoryResult();
        mockHistoryResult.setRecords(java.util.Collections.emptyList());
        mockHistoryResult.setTotal(0L);
        mockHistoryResult.setPageNum(pageNum);
        mockHistoryResult.setPageSize(pageSize);

        when(reconciliationService.queryReconciliationHistory(
            any(LocalDate.class), any(LocalDate.class), eq(pageNum), eq(pageSize)))
            .thenReturn(mockHistoryResult);

        // When
        ResponseDTO<PageResult<ReconciliationService.ReconciliationResult>> result =
            reconciliationController.queryReconciliationHistory(startDate, endDate, pageNum, pageSize);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(reconciliationService, times(1)).queryReconciliationHistory(
            any(LocalDate.class), any(LocalDate.class), eq(pageNum), eq(pageSize));
    }

    @Test
    @DisplayName("测试查询对账历史-使用默认参数")
    void testQueryReconciliationHistory_DefaultParams() {
        // Given
        ReconciliationService.ReconciliationHistoryResult mockHistoryResult =
            new ReconciliationService.ReconciliationHistoryResult();
        mockHistoryResult.setRecords(java.util.Collections.emptyList());
        mockHistoryResult.setTotal(0L);

        when(reconciliationService.queryReconciliationHistory(
            any(), any(), anyInt(), anyInt()))
            .thenReturn(mockHistoryResult);

        // When
        ResponseDTO<PageResult<ReconciliationService.ReconciliationResult>> result =
            reconciliationController.queryReconciliationHistory(null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(reconciliationService, times(1)).queryReconciliationHistory(
            any(), any(), anyInt(), anyInt());
    }
}
