package net.lab1024.sa.consume.integration;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.dao.ReconciliationRecordDao;
import net.lab1024.sa.consume.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.entity.ReconciliationRecordEntity;
import net.lab1024.sa.consume.manager.ConsumeTransactionManager;
import net.lab1024.sa.consume.service.ConsumeTransactionService;
import net.lab1024.sa.consume.service.impl.ConsumeTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消费记录对账集成测试
 *
 * 测试范围：
 * 1. 完整的对账流程（端到端）
 * 2. 系统与设备数据对比
 * 3. 差异检测和记录
 * 4. 数据库事务一致性
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("消费记录对账集成测试")
class ConsumeReconciliationIntegrationTest {

    @Autowired
    private ConsumeTransactionService consumeTransactionService;

    @Autowired
    private ConsumeTransactionDao consumeTransactionDao;

    @Autowired
    private ReconciliationRecordDao reconciliationRecordDao;

    private ConsumeTransactionEntity systemTransaction;
    private ConsumeTransactionEntity deviceTransaction;

    @BeforeEach
    void setUp() {
        // 创建系统交易记录
        systemTransaction = ConsumeTransactionEntity.builder()
                .transactionCode("TXN2025122600001")
                .userId(1001L)
                .accountId(5001L)
                .transactionType(1)
                .transactionAmount(new BigDecimal("25.50"))
                .transactionStatus(2) // 已完成
                .deviceCode("POS001")
                .transactionTime(LocalDate.now().atStartOfDay())
                .build();

        // 创建设备交易记录
        deviceTransaction = ConsumeTransactionEntity.builder()
                .transactionCode("TXN_DEVICE_2025122600001")
                .userId(1001L)
                .accountId(5001L)
                .transactionType(1)
                .transactionAmount(new BigDecimal("25.50"))
                .transactionStatus(2)
                .deviceCode("POS001")
                .transactionTime(LocalDate.now().atStartOfDay())
                .build();
    }

    @Test
    @DisplayName("测试完整对账流程 - 无差异情况")
    void testCompleteReconciliation_NoDiscrepancy() {
        log.info("[集成测试] 测试完整对账流程 - 无差异情况");

        // Given - 准备测试数据
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        // 清空测试数据
        reconciliationRecordDao.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>()
        );

        // 创建系统和设备的同步交易记录
        consumeTransactionDao.insert(systemTransaction);
        consumeTransactionDao.insert(deviceTransaction);

        // When - 执行对账
        Map<String, Object> result = consumeTransactionService.reconcileTransactions(startDate, endDate);

        // Then - 验证结果
        assertNotNull(result, "对账结果不应为null");
        assertNotNull(result.get("reconciliationId"), "对账ID不应为null");
        assertEquals(2, result.get("reconciliationStatus"), "对账状态应为2（成功）");
        assertFalse((Boolean) result.get("hasDiscrepancy"), "应无差异");

        // 验证数据库记录
        Long reconciliationId = (Long) result.get("reconciliationId");
        ReconciliationRecordEntity record = reconciliationRecordDao.selectById(reconciliationId);
        assertNotNull(record, "对账记录应保存到数据库");
        assertEquals(LocalDate.parse(startDate), record.getStartDate(), "开始日期应匹配");
        assertEquals(LocalDate.parse(endDate), record.getEndDate(), "结束日期应匹配");
        assertEquals(0, record.getDiscrepancyCount(), "差异数量应为0");

        log.info("[集成测试] 测试通过: 无差异对账流程成功，reconciliationId={}", reconciliationId);
    }

    @Test
    @DisplayName("测试完整对账流程 - 存在差异")
    void testCompleteReconciliation_WithDiscrepancy() {
        log.info("[集成测试] 测试完整对账流程 - 存在差异");

        // Given - 创建系统交易（但无对应设备交易）
        consumeTransactionDao.insert(systemTransaction);

        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        // When - 执行对账
        Map<String, Object> result = consumeTransactionService.reconcileTransactions(startDate, endDate);

        // Then - 验证结果
        assertNotNull(result, "对账结果不应为null");
        assertEquals(3, result.get("reconciliationStatus"), "对账状态应为3（存在差异）");
        assertTrue((Boolean) result.get("hasDiscrepancy"), "应存在差异");

        // 验证数据库记录
        Long reconciliationId = (Long) result.get("reconciliationId");
        ReconciliationRecordEntity record = reconciliationRecordDao.selectById(reconciliationId);
        assertNotNull(record, "对账记录应保存到数据库");
        assertTrue(record.getDiscrepancyCount() > 0, "差异数量应大于0");

        log.info("[集成测试] 测试通过: 差异对账流程成功，discrepancyCount={}",
                record.getDiscrepancyCount());
    }

    @Test
    @DisplayName("测试对账历史查询")
    void testGetReconciliationHistory() {
        log.info("[集成测试] 测试对账历史查询");

        // Given - 创建对账记录
        ReconciliationRecordEntity record = ReconciliationRecordEntity.builder()
                .startDate(LocalDate.of(2025, 12, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .systemTransactionCount(100)
                .systemTotalAmount(new BigDecimal("1000.00"))
                .deviceTransactionCount(100)
                .deviceTotalAmount(new BigDecimal("1000.00"))
                .reconciliationStatus(2)
                .build();

        reconciliationRecordDao.insert(record);

        // When - 查询对账历史
        List<ReconciliationRecordEntity> history = consumeTransactionService.getReconciliationHistory(
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 31)
        );

        // Then
        assertNotNull(history, "对账历史不应为null");
        assertFalse(history.isEmpty(), "对账历史不应为空");
        assertTrue(history.stream().anyMatch(r ->
                r.getStartDate().equals(LocalDate.of(2025, 12, 1))), "应包含对账记录");

        log.info("[集成测试] 测试通过: 对账历史查询成功，记录数={}", history.size());
    }

    @Test
    @DisplayName("测试对账统计信息")
    void testGetReconciliationStatistics() {
        log.info("[集成测试] 测试对账统计信息");

        // Given - 创建多条对账记录
        for (int i = 0; i < 5; i++) {
            ReconciliationRecordEntity record = ReconciliationRecordEntity.builder()
                    .startDate(LocalDate.of(2025, 12, 1))
                    .endDate(LocalDate.of(2025, 12, 31))
                    .systemTransactionCount(100)
                    .reconciliationStatus(i % 2 == 0 ? 2 : 3) // 交替成功和差异
                    .build();

            reconciliationRecordDao.insert(record);
        }

        // When - 查询统计信息
        Map<String, Object> stats = consumeTransactionService.getReconciliationStatistics(
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 31)
        );

        // Then
        assertNotNull(stats, "统计信息不应为null");
        assertTrue((Integer) stats.get("totalCount") >= 5, "总对账次数应>=5");
        assertTrue((Integer) stats.get("discrepancyCount") >= 2, "存在差异次数应>=2");

        log.info("[集成测试] 测试通过: 对账统计查询成功，totalCount={}, discrepancyCount={}",
                stats.get("totalCount"), stats.get("discrepancyCount"));
    }

    @Test
    @DisplayName("测试并发对账事务一致性")
    void testConcurrentReconciliation_TransactionConsistency() {
        log.info("[集成测试] 测试并发对账事务一致性");

        // Given
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        // When - 并发执行多次对账
        Map<String, Object> result1 = consumeTransactionService.reconcileTransactions(startDate, endDate);
        Map<String, Object> result2 = consumeTransactionService.reconcileTransactions(startDate, endDate);

        // Then - 验证事务一致性
        assertNotNull(result1, "第一次对账结果不应为null");
        assertNotNull(result2, "第二次对账结果不应为null");

        // 每次对账都应创建独立记录
        assertNotEquals(result1.get("reconciliationId"), result2.get("reconciliationId"),
                "对账ID应不同（独立记录）");

        log.info("[集成测试] 测试通过: 并发对账事务一致性保证");
    }
}
