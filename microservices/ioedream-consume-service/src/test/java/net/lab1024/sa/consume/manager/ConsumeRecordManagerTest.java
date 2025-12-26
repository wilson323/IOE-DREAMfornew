package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.consume.dao.ConsumeAccountDao;
import net.lab1024.sa.consume.dao.ConsumeAccountTransactionDao;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.entity.ConsumeAccountEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeAccountTransactionEntity;

/**
 * ConsumeRecordManager 单元测试
 * <p>
 * 测试消费记录管理器的核心功能：
 * - 在线消费记录创建
 * - 离线消费记录创建
 * - 退款处理
 * - 离线记录同步
 * - 账户变动记录
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@ExtendWith(MockitoExtension.class)
class ConsumeRecordManagerTest {

    @Mock
    private ConsumeRecordDao consumeRecordDao;

    @Mock
    private ConsumeAccountDao consumeAccountDao;

    @Mock
    private ConsumeAccountTransactionDao accountTransactionDao;

    @Mock
    private ConsumeDistributedLockManager lockManager;

    @InjectMocks
    private ConsumeRecordManager recordManager;

    private ConsumeRecordEntity testRecord;
    private ConsumeAccountEntity testAccount;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();
        testRecord = createTestRecord();
        testAccount = createTestAccount();
    }

    /**
     * 创建测试消费记录
     */
    private ConsumeRecordEntity createTestRecord() {
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setRecordId(1L);
        record.setAccountId(1001L);
        record.setUserId(10001L);
        record.setUserName("张三");
        record.setAmount(new BigDecimal("25.50"));
        record.setOrderNo("ORDER20251223001");
        record.setTransactionNo("TXN20251223001");
        record.setTransactionStatus(1);
        record.setConsumeStatus(1);
        record.setConsumeTime(testTime);
        record.setDeviceId("POS001");
        record.setDeviceName("餐厅POS机");
        record.setMerchantId(2001L);
        record.setMerchantName("中餐厅");
        record.setConsumeType("MEAL");
        record.setConsumeTypeName("午餐");
        record.setPaymentMethod("BALANCE");
        record.setRefundStatus(0);
        record.setRefundAmount(BigDecimal.ZERO);
        record.setOfflineFlag(0);
        record.setSyncStatus(1);
        return record;
    }

    /**
     * 创建测试账户
     */
    private ConsumeAccountEntity createTestAccount() {
        ConsumeAccountEntity account = new ConsumeAccountEntity();
        account.setAccountId(1001L);
        account.setUserId(10001L);
        account.setAccountCode("ACC20251223001");
        account.setAccountName("张三账户");
        account.setBalance(new BigDecimal("1000.00"));
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setCreditLimit(BigDecimal.ZERO);
        account.setAccountStatus(1);  // 修复：使用setAccountStatus
        return account;
    }

    // ==================== 在线消费记录测试 ====================

    @Test
    void testCreateOnlineRecord_Success() {
        // Given
        when(consumeRecordDao.insert(any(ConsumeRecordEntity.class))).thenAnswer(invocation -> {
            ConsumeRecordEntity record = invocation.getArgument(0);
            record.setRecordId(1L);
            return 1;
        });

        // When
        Long recordId = recordManager.createOnlineRecord(
                1001L, 10001L, "张三",
                "POS001", "餐厅POS机",
                2001L, "中餐厅",
                new BigDecimal("25.50"),
                new BigDecimal("25.50"),
                BigDecimal.ZERO,
                "MEAL", "午餐",
                "BALANCE",
                "ORDER20251223001",
                "TXN20251223001",
                "中餐厅"
        );

        // Then
        assertNotNull(recordId);
        assertEquals(1L, recordId);
        verify(consumeRecordDao).insert(any(ConsumeRecordEntity.class));
    }

    // ==================== 离线消费记录测试 ====================

    @Test
    void testCreateOfflineRecord_Success() {
        // Given
        when(consumeRecordDao.insert(any(ConsumeRecordEntity.class))).thenAnswer(invocation -> {
            ConsumeRecordEntity record = invocation.getArgument(0);
            record.setRecordId(2L);
            return 1;
        });

        // When
        Long recordId = recordManager.createOfflineRecord(
                1001L, 10001L, "张三",
                "POS001", "餐厅POS机",
                2001L, "中餐厅",
                new BigDecimal("25.50"),
                "MEAL", "午餐",
                "BALANCE",
                "OFFLINE20251223001",
                testTime,
                "中餐厅"
        );

        // Then
        assertNotNull(recordId);
        assertEquals(2L, recordId);
        verify(consumeRecordDao).insert(any(ConsumeRecordEntity.class));
    }

    // ==================== 退款处理测试 ====================

    @Test
    void testProcessRefund_Success() {
        // Given
        when(consumeRecordDao.selectById(1L)).thenReturn(testRecord);
        when(consumeRecordDao.updateById(any(ConsumeRecordEntity.class))).thenReturn(1);
        when(lockManager.executeWithAccountLock(eq(1L), any())).thenAnswer(invocation -> {
            // 模拟Supplier执行 - 退款成功场景
            java.util.function.Supplier<Boolean> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        // When
        boolean success = recordManager.processRefund(1L, new BigDecimal("25.50"), "菜品质量问题");

        // Then
        assertTrue(success);
        verify(consumeRecordDao).selectById(1L);
        verify(consumeRecordDao).updateById(any(ConsumeRecordEntity.class));
    }

    @Test
    void testProcessRefund_RecordNotFound() {
        // Given
        when(consumeRecordDao.selectById(999L)).thenReturn(null);
        when(lockManager.executeWithAccountLock(eq(999L), any())).thenAnswer(invocation -> {
            // 模拟Supplier执行 - 记录不存在场景
            java.util.function.Supplier<Boolean> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        // When
        boolean success = recordManager.processRefund(999L, new BigDecimal("25.50"), "菜品质量问题");

        // Then
        assertFalse(success);
        verify(consumeRecordDao).selectById(999L);
        verify(consumeRecordDao, never()).updateById(any(ConsumeRecordEntity.class));
    }

    @Test
    void testProcessRefund_ExceedAmount() {
        // Given
        when(consumeRecordDao.selectById(1L)).thenReturn(testRecord);
        when(lockManager.executeWithAccountLock(eq(1L), any())).thenAnswer(invocation -> {
            // 模拟Supplier执行 - 退款金额超额场景
            java.util.function.Supplier<Boolean> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        // When
        boolean success = recordManager.processRefund(1L, new BigDecimal("30.00"), "菜品质量问题");

        // Then
        assertFalse(success);
        verify(consumeRecordDao).selectById(1L);
        verify(consumeRecordDao, never()).updateById(any(ConsumeRecordEntity.class));
    }

    // ==================== 离线记录同步测试 ====================

    @Test
    void testSyncOfflineRecord_Success() {
        // Given
        testRecord.setOfflineFlag(1);
        testRecord.setSyncStatus(0);
        when(consumeRecordDao.selectById(1L)).thenReturn(testRecord);
        when(consumeRecordDao.updateById(any(ConsumeRecordEntity.class))).thenReturn(1);

        // When
        boolean success = recordManager.syncOfflineRecord(1L);

        // Then
        assertTrue(success);
        verify(consumeRecordDao).selectById(1L);
        verify(consumeRecordDao).updateById(any(ConsumeRecordEntity.class));
    }

    @Test
    void testSyncOfflineRecord_NotOffline() {
        // Given
        testRecord.setOfflineFlag(0); // 在线记录
        when(consumeRecordDao.selectById(1L)).thenReturn(testRecord);

        // When
        boolean success = recordManager.syncOfflineRecord(1L);

        // Then
        assertFalse(success);
        verify(consumeRecordDao).selectById(1L);
        verify(consumeRecordDao, never()).updateById(any(ConsumeRecordEntity.class));
    }

    // ==================== 批量同步测试 ====================

    @Test
    void testBatchSyncOfflineRecords_Success() {
        // Given
        testRecord.setOfflineFlag(1);
        testRecord.setSyncStatus(0);
        List<ConsumeRecordEntity> pendingRecords = Collections.singletonList(testRecord);
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenReturn(pendingRecords);
        when(consumeRecordDao.selectById(1L)).thenReturn(testRecord);
        when(consumeRecordDao.updateById(any(ConsumeRecordEntity.class))).thenReturn(1);

        // When
        int successCount = recordManager.batchSyncOfflineRecords(100);

        // Then
        assertEquals(1, successCount);
        verify(consumeRecordDao).selectPendingSyncRecords(100);
    }

    @Test
    void testBatchSyncOfflineRecords_NoPendingRecords() {
        // Given
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenReturn(Collections.emptyList());

        // When
        int successCount = recordManager.batchSyncOfflineRecords(100);

        // Then
        assertEquals(0, successCount);
        verify(consumeRecordDao).selectPendingSyncRecords(100);
        verify(consumeRecordDao, never()).updateById(any(ConsumeRecordEntity.class));
    }

    // ==================== 账户变动记录测试 ====================

    @Test
    void testRecordTransaction_Success() {
        // Given
        when(accountTransactionDao.insert(any(ConsumeAccountTransactionEntity.class))).thenAnswer(invocation -> {
            // 模拟MyBatis-Plus的insert行为 - 设置生成的ID
            ConsumeAccountTransactionEntity transaction = invocation.getArgument(0);
            transaction.setTransactionId(12345L); // 模拟数据库生成的ID
            return 1;
        });

        // When
        Long transactionId = recordManager.recordTransaction(
                1001L,
                10001L,
                "CONSUME",
                "TXN20251223001",
                "ORDER20251223001",
                new BigDecimal("-25.50"),
                new BigDecimal("1000.00"),
                new BigDecimal("974.50"),
                1L,
                "ORDER20251223001"
        );

        // Then
        assertNotNull(transactionId);
        assertEquals(12345L, transactionId);
        verify(accountTransactionDao).insert(any(ConsumeAccountTransactionEntity.class));
    }
}
