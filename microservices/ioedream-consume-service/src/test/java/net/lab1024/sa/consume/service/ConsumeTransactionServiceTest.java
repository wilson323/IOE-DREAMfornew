package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.dao.ReconciliationRecordDao;
import net.lab1024.sa.common.entity.consume.ConsumeTransactionEntity;
import net.lab1024.sa.common.entity.consume.ReconciliationRecordEntity;
import net.lab1024.sa.consume.manager.ConsumeTransactionManager;
import net.lab1024.sa.consume.service.impl.ConsumeTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 消费交易服务单元测试
 *
 * 测试范围：
 * 1. 交易对账流程
 * 2. 对账结果查询
 * 3. 对账历史记录
 * 4. 异常情况处理
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("消费交易服务测试")
class ConsumeTransactionServiceTest {

    @Mock
    private ConsumeTransactionDao consumeTransactionDao;

    @Mock
    private ReconciliationRecordDao reconciliationRecordDao;

    @Mock
    private ConsumeTransactionManager consumeTransactionManager;

    @InjectMocks
    private ConsumeTransactionServiceImpl consumeTransactionService;

    private ConsumeTransactionEntity testTransaction;
    private ReconciliationRecordEntity testReconciliation;

    @BeforeEach
    void setUp() {
        testTransaction = ConsumeTransactionEntity.builder()
                .transactionId(1L)
                .transactionCode("TXN202512260001")
                .transactionAmount(new BigDecimal("25.50"))
                .transactionStatus(2)
                .build();

        testReconciliation = ReconciliationRecordEntity.builder()
                .reconciliationId(1L)
                .startDate(LocalDate.of(2025, 12, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .systemTransactionCount(100)
                .systemTotalAmount(new BigDecimal("1000.00"))
                .deviceTransactionCount(98)
                .deviceTotalAmount(new BigDecimal("980.00"))
                .discrepancyCount(2)
                .discrepancyAmount(new BigDecimal("20.00"))
                .reconciliationStatus(3)
                .build();
    }

    @Test
    @DisplayName("测试执行对账 - 成功情况")
    void testReconcileTransactions_Success() {
        log.info("[单元测试] 测试执行对账 - 成功情况");

        // Given
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        Map<String, Object> systemStats = Map.of(
                "transactionCount", 100,
                "totalAmount", new BigDecimal("1000.00")
        );

        Map<String, Object> deviceStats = Map.of(
                "transactionCount", 98,
                "totalAmount", new BigDecimal("980.00")
        );

        Map<String, Object> discrepancy = Map.of(
                "countDiff", 2,
                "amountDiff", new BigDecimal("20.00"),
                "hasDiscrepancy", true
        );

        when(consumeTransactionManager.getSystemTransactionStats(anyString(), anyString()))
                .thenReturn(systemStats);
        when(consumeTransactionManager.getDeviceTransactionStats(anyString(), anyString()))
                .thenReturn(deviceStats);
        when(consumeTransactionManager.calculateDiscrepancy(any(), any()))
                .thenReturn(discrepancy);
        when(consumeTransactionManager.createReconciliationRecord(any(), any(), any(), any(), any()))
                .thenReturn(testReconciliation);

        // When
        Map<String, Object> result = consumeTransactionService.reconcileTransactions(startDate, endDate);

        // Then
        assertNotNull(result, "对账结果不应为null");
        assertEquals(1L, result.get("reconciliationId"), "对账ID应为1");
        assertEquals(3, result.get("reconciliationStatus"), "对账状态应为3(存在差异)");
        assertTrue((Boolean) result.get("hasDiscrepancy"), "应存在差异");

        log.info("[单元测试] 测试通过: 对账执行成功，reconciliationId={}, hasDiscrepancy={}",
                result.get("reconciliationId"), result.get("hasDiscrepancy"));

        verify(consumeTransactionManager, times(1)).getSystemTransactionStats(startDate, endDate);
        verify(consumeTransactionManager, times(1)).getDeviceTransactionStats(startDate, endDate);
        verify(consumeTransactionManager, times(1)).calculateDiscrepancy(systemStats, deviceStats);
        verify(consumeTransactionManager, times(1)).createReconciliationRecord(
                anyString(), anyString(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("测试执行对账 - 无差异情况")
    void testReconcileTransactions_NoDiscrepancy() {
        log.info("[单元测试] 测试执行对账 - 无差异情况");

        // Given
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        Map<String, Object> systemStats = Map.of(
                "transactionCount", 100,
                "totalAmount", new BigDecimal("1000.00")
        );

        Map<String, Object> deviceStats = Map.of(
                "transactionCount", 100,
                "totalAmount", new BigDecimal("1000.00")
        );

        Map<String, Object> discrepancy = Map.of(
                "countDiff", 0,
                "amountDiff", BigDecimal.ZERO,
                "hasDiscrepancy", false
        );

        ReconciliationRecordEntity noDiscrepancyRecord = ReconciliationRecordEntity.builder()
                .reconciliationId(2L)
                .reconciliationStatus(2) // 对账成功
                .build();

        when(consumeTransactionManager.getSystemTransactionStats(anyString(), anyString()))
                .thenReturn(systemStats);
        when(consumeTransactionManager.getDeviceTransactionStats(anyString(), anyString()))
                .thenReturn(deviceStats);
        when(consumeTransactionManager.calculateDiscrepancy(any(), any()))
                .thenReturn(discrepancy);
        when(consumeTransactionManager.createReconciliationRecord(any(), any(), any(), any(), any()))
                .thenReturn(noDiscrepancyRecord);

        // When
        Map<String, Object> result = consumeTransactionService.reconcileTransactions(startDate, endDate);

        // Then
        assertNotNull(result, "对账结果不应为null");
        assertEquals(2L, result.get("reconciliationId"), "对账ID应为2");
        assertEquals(2, result.get("reconciliationStatus"), "对账状态应为2(成功)");
        assertFalse((Boolean) result.get("hasDiscrepancy"), "应无差异");

        log.info("[单元测试] 测试通过: 无差异对账执行成功");

        verify(consumeTransactionManager, times(1)).getSystemTransactionStats(startDate, endDate);
        verify(consumeTransactionManager, times(1)).getDeviceTransactionStats(startDate, endDate);
    }

    @Test
    @DisplayName("测试查询对账记录 - 正常情况")
    void testGetReconciliationRecord_Success() {
        log.info("[单元测试] 测试查询对账记录 - 正常情况");

        // Given
        Long reconciliationId = 1L;
        when(reconciliationRecordDao.selectById(reconciliationId))
                .thenReturn(testReconciliation);

        // When
        ReconciliationRecordEntity result = consumeTransactionService.getReconciliationRecord(reconciliationId);

        // Then
        assertNotNull(result, "对账记录不应为null");
        assertEquals(reconciliationId, result.getReconciliationId(), "对账ID应匹配");
        assertEquals(3, result.getReconciliationStatus(), "对账状态应为3");

        log.info("[单元测试] 测试通过: 对账记录查询成功，reconciliationId={}", result.getReconciliationId());

        verify(reconciliationRecordDao, times(1)).selectById(reconciliationId);
    }

    @Test
    @DisplayName("测试查询对账记录 - 记录不存在")
    void testGetReconciliationRecord_NotFound() {
        log.info("[单元测试] 测试查询对账记录 - 记录不存在");

        // Given
        Long reconciliationId = 999L;
        when(reconciliationRecordDao.selectById(reconciliationId))
                .thenReturn(null);

        // When
        ReconciliationRecordEntity result = consumeTransactionService.getReconciliationRecord(reconciliationId);

        // Then
        assertNull(result, "对账记录应为null");

        log.info("[单元测试] 测试通过: 记录不存在情况处理正确");

        verify(reconciliationRecordDao, times(1)).selectById(reconciliationId);
    }

    @Test
    @DisplayName("测试查询对账历史 - 正常情况")
    void testGetReconciliationHistory_Success() {
        log.info("[单元测试] 测试查询对账历史 - 正常情况");

        // Given
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        List<ReconciliationRecordEntity> history = Arrays.asList(
                testReconciliation,
                ReconciliationRecordEntity.builder()
                        .reconciliationId(2L)
                        .reconciliationStatus(2)
                        .build()
        );

        when(reconciliationRecordDao.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(history);

        // When
        List<ReconciliationRecordEntity> result = consumeTransactionService.getReconciliationHistory(
                startDate, endDate
        );

        // Then
        assertNotNull(result, "对账历史不应为null");
        assertEquals(2, result.size(), "对账记录数量应为2");

        log.info("[单元测试] 测试通过: 对账历史查询成功，记录数={}", result.size());

        verify(reconciliationRecordDao, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试查询对账统计 - 正常情况")
    void testGetReconciliationStatistics_Success() {
        log.info("[单元测试] 测试查询对账统计 - 正常情况");

        // Given
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        List<ReconciliationRecordEntity> records = Arrays.asList(
                testReconciliation,
                ReconciliationRecordEntity.builder()
                        .reconciliationId(2L)
                        .reconciliationStatus(2)
                        .discrepancyCount(0)
                        .discrepancyAmount(BigDecimal.ZERO)
                        .build()
        );

        when(reconciliationRecordDao.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(records);

        // When
        Map<String, Object> stats = consumeTransactionService.getReconciliationStatistics(startDate, endDate);

        // Then
        assertNotNull(stats, "统计数据不应为null");
        assertEquals(2, stats.get("totalCount"), "总对账次数应为2");
        assertEquals(1, stats.get("discrepancyCount"), "存在差异的次数应为1");

        log.info("[单元测试] 测试通过: 对账统计查询成功，totalCount={}, discrepancyCount={}",
                stats.get("totalCount"), stats.get("discrepancyCount"));

        verify(reconciliationRecordDao, times(1)).selectList(any(LambdaQueryWrapper.class));
    }
}
