package net.lab1024.sa.consume.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.dao.ReconciliationRecordDao;
import net.lab1024.sa.common.entity.consume.ConsumeTransactionEntity;
import net.lab1024.sa.common.entity.consume.ReconciliationRecordEntity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 消费交易管理器单元测试
 *
 * 测试范围：
 * 1. 系统交易统计
 * 2. 设备交易统计
 * 3. 差异计算
 * 4. 对账记录创建
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("消费交易管理器测试")
class ConsumeTransactionManagerTest {

    @Mock
    private ConsumeTransactionDao consumeTransactionDao;

    @Mock
    private ReconciliationRecordDao reconciliationRecordDao;

    @InjectMocks
    private ConsumeTransactionManager consumeTransactionManager;

    private ConsumeTransactionEntity testTransaction;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2025, 12, 26);

        // 创建测试交易记录
        testTransaction = ConsumeTransactionEntity.builder()
                .transactionId(1L)
                .transactionCode("TXN202512260001")
                .userId(1001L)
                .accountId(5001L)
                .transactionType(1)
                .transactionAmount(new BigDecimal("25.50"))
                .deviceCode("POS001")
                .transactionStatus(2)
                .transactionTime(testDate.atStartOfDay())
                .build();
    }

    @Test
    @DisplayName("测试获取系统交易统计 - 正常情况")
    void testGetSystemTransactionStats_Success() {
        log.info("[单元测试] 测试获取系统交易统计 - 正常情况");

        // Given
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        List<ConsumeTransactionEntity> transactions = Arrays.asList(
                testTransaction,
                ConsumeTransactionEntity.builder()
                        .transactionId(2L)
                        .transactionAmount(new BigDecimal("30.00"))
                        .build()
        );

        when(consumeTransactionDao.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(transactions);

        // When
        Map<String, Object> result = consumeTransactionManager.getSystemTransactionStats(startDate, endDate);

        // Then
        assertNotNull(result, "统计结果不应为null");
        assertEquals(2, result.get("transactionCount"), "交易数量应为2");
        assertEquals(new BigDecimal("55.50"), result.get("totalAmount"), "总金额应为55.50");

        log.info("[单元测试] 测试通过: transactionCount={}, totalAmount={}",
                result.get("transactionCount"), result.get("totalAmount"));

        verify(consumeTransactionDao, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试获取系统交易统计 - 无交易记录")
    void testGetSystemTransactionStats_NoTransactions() {
        log.info("[单元测试] 测试获取系统交易统计 - 无交易记录");

        // Given
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        when(consumeTransactionDao.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList());

        // When
        Map<String, Object> result = consumeTransactionManager.getSystemTransactionStats(startDate, endDate);

        // Then
        assertNotNull(result, "统计结果不应为null");
        assertEquals(0, result.get("transactionCount"), "交易数量应为0");
        assertEquals(BigDecimal.ZERO, result.get("totalAmount"), "总金额应为0");

        log.info("[单元测试] 测试通过: 无交易记录情况处理正确");

        verify(consumeTransactionDao, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试获取设备交易统计 - 正常情况")
    void testGetDeviceTransactionStats_Success() {
        log.info("[单元测试] 测试获取设备交易统计 - 正常情况");

        // Given
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        List<ConsumeTransactionEntity> transactions = Arrays.asList(
                testTransaction,
                ConsumeTransactionEntity.builder()
                        .transactionId(3L)
                        .transactionAmount(new BigDecimal("15.00"))
                        .build()
        );

        when(consumeTransactionDao.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(transactions);

        // When
        Map<String, Object> result = consumeTransactionManager.getDeviceTransactionStats(startDate, endDate);

        // Then
        assertNotNull(result, "统计结果不应为null");
        assertEquals(2, result.get("transactionCount"), "交易数量应为2");
        assertEquals(new BigDecimal("40.50"), result.get("totalAmount"), "总金额应为40.50");

        log.info("[单元测试] 测试通过: 设备交易统计正确");

        verify(consumeTransactionDao, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试计算对账差异 - 正常情况")
    void testCalculateDiscrepancy_Success() {
        log.info("[单元测试] 测试计算对账差异 - 正常情况");

        // Given
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("transactionCount", 100);
        systemStats.put("totalAmount", new BigDecimal("1000.00"));

        Map<String, Object> deviceStats = new HashMap<>();
        deviceStats.put("transactionCount", 98);
        deviceStats.put("totalAmount", new BigDecimal("980.00"));

        // When
        Map<String, Object> discrepancy = consumeTransactionManager.calculateDiscrepancy(
                systemStats, deviceStats
        );

        // Then
        assertNotNull(discrepancy, "差异结果不应为null");
        assertEquals(2, discrepancy.get("countDiff"), "数量差异应为2");
        assertEquals(new BigDecimal("20.00"), discrepancy.get("amountDiff"), "金额差异应为20.00");
        assertTrue((Boolean) discrepancy.get("hasDiscrepancy"), "应存在差异");

        log.info("[单元测试] 测试通过: countDiff={}, amountDiff={}, hasDiscrepancy={}",
                discrepancy.get("countDiff"), discrepancy.get("amountDiff"), discrepancy.get("hasDiscrepancy"));
    }

    @Test
    @DisplayName("测试计算对账差异 - 无差异情况")
    void testCalculateDiscrepancy_NoDiscrepancy() {
        log.info("[单元测试] 测试计算对账差异 - 无差异情况");

        // Given
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("transactionCount", 100);
        systemStats.put("totalAmount", new BigDecimal("1000.00"));

        Map<String, Object> deviceStats = new HashMap<>();
        deviceStats.put("transactionCount", 100);
        deviceStats.put("totalAmount", new BigDecimal("1000.00"));

        // When
        Map<String, Object> discrepancy = consumeTransactionManager.calculateDiscrepancy(
                systemStats, deviceStats
        );

        // Then
        assertNotNull(discrepancy, "差异结果不应为null");
        assertEquals(0, discrepancy.get("countDiff"), "数量差异应为0");
        assertEquals(BigDecimal.ZERO, discrepancy.get("amountDiff"), "金额差异应为0");
        assertFalse((Boolean) discrepancy.get("hasDiscrepancy"), "应无差异");

        log.info("[单元测试] 测试通过: 无差异情况处理正确");
    }

    @Test
    @DisplayName("测试创建对账记录 - 正常情况")
    void testCreateReconciliationRecord_Success() {
        log.info("[单元测试] 测试创建对账记录 - 正常情况");

        // Given
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("transactionCount", 100);
        systemStats.put("totalAmount", new BigDecimal("1000.00"));

        Map<String, Object> deviceStats = new HashMap<>();
        deviceStats.put("transactionCount", 100);
        deviceStats.put("totalAmount", new BigDecimal("1000.00"));

        Map<String, Object> discrepancy = new HashMap<>();
        discrepancy.put("countDiff", 0);
        discrepancy.put("amountDiff", BigDecimal.ZERO);
        discrepancy.put("hasDiscrepancy", false);

        when(reconciliationRecordDao.insert(any(ReconciliationRecordEntity.class)))
                .thenReturn(1);

        // When
        ReconciliationRecordEntity record = consumeTransactionManager.createReconciliationRecord(
                startDate, endDate, systemStats, deviceStats, discrepancy
        );

        // Then
        assertNotNull(record, "对账记录不应为null");
        assertEquals(LocalDate.parse(startDate), record.getStartDate(), "开始日期应匹配");
        assertEquals(LocalDate.parse(endDate), record.getEndDate(), "结束日期应匹配");
        assertEquals(100, record.getSystemTransactionCount(), "系统交易数量应匹配");
        assertEquals(100, record.getDeviceTransactionCount(), "设备交易数量应匹配");
        assertEquals(0, record.getDiscrepancyCount(), "差异数量应为0");
        assertEquals(BigDecimal.ZERO, record.getDiscrepancyAmount(), "差异金额应为0");
        assertEquals(2, record.getReconciliationStatus(), "对账状态应为2(成功)");

        log.info("[单元测试] 测试通过: 对账记录创建成功，reconciliationId={}", record.getReconciliationId());

        verify(reconciliationRecordDao, times(1)).insert(any(ReconciliationRecordEntity.class));
    }

    @Test
    @DisplayName("测试创建对账记录 - 存在差异情况")
    void testCreateReconciliationRecord_WithDiscrepancy() {
        log.info("[单元测试] 测试创建对账记录 - 存在差异情况");

        // Given
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("transactionCount", 100);
        systemStats.put("totalAmount", new BigDecimal("1000.00"));

        Map<String, Object> deviceStats = new HashMap<>();
        deviceStats.put("transactionCount", 98);
        deviceStats.put("totalAmount", new BigDecimal("980.00"));

        Map<String, Object> discrepancy = new HashMap<>();
        discrepancy.put("countDiff", 2);
        discrepancy.put("amountDiff", new BigDecimal("20.00"));
        discrepancy.put("hasDiscrepancy", true);

        when(reconciliationRecordDao.insert(any(ReconciliationRecordEntity.class)))
                .thenReturn(1);

        // When
        ReconciliationRecordEntity record = consumeTransactionManager.createReconciliationRecord(
                startDate, endDate, systemStats, deviceStats, discrepancy
        );

        // Then
        assertNotNull(record, "对账记录不应为null");
        assertEquals(2, record.getDiscrepancyCount(), "差异数量应为2");
        assertEquals(new BigDecimal("20.00"), record.getDiscrepancyAmount(), "差异金额应为20.00");
        assertEquals(3, record.getReconciliationStatus(), "对账状态应为3(存在差异)");

        log.info("[单元测试] 测试通过: 差异对账记录创建成功");

        verify(reconciliationRecordDao, times(1)).insert(any(ReconciliationRecordEntity.class));
    }
}
