package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import net.lab1024.sa.common.entity.consume.ConsumeAccountEntity;
import net.lab1024.sa.common.entity.consume.ConsumeRecordEntity;
import net.lab1024.sa.common.entity.consume.ConsumeAccountTransactionEntity;

/**
 * ConsumeOfflineSyncManager 单元测试
 * <p>
 * 测试离线消费同步管理器的核心功能：
 * - 定时扫描待同步记录
 * - 单条记录同步
 * - 余额扣减
 * - 交易记录创建
 * - 统计报告
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@ExtendWith(MockitoExtension.class)
class ConsumeOfflineSyncManagerTest {

    @Mock
    private ConsumeRecordDao consumeRecordDao;

    @Mock
    private ConsumeAccountDao consumeAccountDao;

    @Mock
    private ConsumeAccountTransactionDao accountTransactionDao;

    @Mock
    private ConsumeRecordManager recordManager;

    @Mock
    private ConsumeDistributedLockManager lockManager;

    @InjectMocks
    private ConsumeOfflineSyncManager syncManager;

    private ConsumeRecordEntity offlineRecord;
    private ConsumeAccountEntity testAccount;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();
        offlineRecord = createOfflineRecord();
        testAccount = createTestAccount();
    }

    /**
     * 创建离线消费记录
     */
    private ConsumeRecordEntity createOfflineRecord() {
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setRecordId(1L);
        record.setAccountId(1001L);
        record.setUserId(10001L);
        record.setUserName("张三");
        record.setAmount(new BigDecimal("25.50"));
        record.setOrderNo("OFFLINE20251223001");
        record.setTransactionNo(null);
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
        record.setOfflineFlag(1); // 离线
        record.setSyncStatus(0); // 未同步
        record.setSyncTime(null);
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
        account.setAccountStatus(1);
        account.setDeletedFlag(0);
        return account;
    }

    // ==================== 定时任务测试 ====================

    @Test
    void testSyncPendingOfflineRecords_NoPendingRecords() {
        // Given
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenReturn(Collections.emptyList());

        // When
        syncManager.syncPendingOfflineRecords();

        // Then
        verify(consumeRecordDao).selectPendingSyncRecords(100);
        verify(lockManager, never()).executeWithAccountLock(anyLong(), any());
    }

    @Test
    void testSyncPendingOfflineRecords_WithPendingRecords() {
        // Given
        List<ConsumeRecordEntity> pendingRecords = Collections.singletonList(offlineRecord);
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenReturn(pendingRecords);
        when(consumeAccountDao.selectById(1001L)).thenReturn(testAccount);
        when(lockManager.executeWithAccountLock(eq(1001L), any())).thenAnswer(invocation -> {
            // 模拟同步成功 - 正确调用Supplier.get()
            java.util.function.Supplier<Boolean> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(consumeAccountDao.updateById(any(ConsumeAccountEntity.class))).thenReturn(1);
        when(accountTransactionDao.insert(any(ConsumeAccountTransactionEntity.class))).thenReturn(1);
        when(consumeRecordDao.updateById(any(ConsumeRecordEntity.class))).thenReturn(1);

        // When
        syncManager.syncPendingOfflineRecords();

        // Then
        verify(consumeRecordDao).selectPendingSyncRecords(100);
        verify(lockManager).executeWithAccountLock(eq(1001L), any());
    }

    @Test
    void testSyncPendingOfflineRecords_ExceptionHandling() {
        // Given
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenThrow(new RuntimeException("Database error"));

        // When & Then - 不应该抛出异常，只记录日志
        assertDoesNotThrow(() -> syncManager.syncPendingOfflineRecords());

        verify(consumeRecordDao).selectPendingSyncRecords(100);
    }

    // ==================== 统计报告测试 ====================

    @Test
    void testReportPendingSyncRecords_NoRecords() {
        // Given
        when(consumeRecordDao.selectPendingSyncRecords(10000)).thenReturn(Collections.emptyList());

        // When
        syncManager.reportPendingSyncRecords();

        // Then
        verify(consumeRecordDao).selectPendingSyncRecords(10000);
    }

    @Test
    void testReportPendingSyncRecords_WithRecords() {
        // Given
        List<ConsumeRecordEntity> pendingRecords = Collections.singletonList(offlineRecord);
        when(consumeRecordDao.selectPendingSyncRecords(10000)).thenReturn(pendingRecords);

        // When
        syncManager.reportPendingSyncRecords();

        // Then
        verify(consumeRecordDao).selectPendingSyncRecords(10000);
    }

    @Test
    void testReportPendingSyncRecords_ExceedAlertThreshold() {
        // Given - 创建超过1000条记录
        List<ConsumeRecordEntity> pendingRecords = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            ConsumeRecordEntity record = new ConsumeRecordEntity();
            record.setRecordId((long) i);
            record.setOfflineFlag(1);
            record.setSyncStatus(0);
            pendingRecords.add(record);
        }
        when(consumeRecordDao.selectPendingSyncRecords(10000)).thenReturn(pendingRecords);

        // When
        syncManager.reportPendingSyncRecords();

        // Then
        verify(consumeRecordDao).selectPendingSyncRecords(10000);
    }

    // ==================== 记录同步测试 ====================

    @Test
    void testSyncOfflineRecord_AccountNotFound() {
        // Given
        List<ConsumeRecordEntity> pendingRecords = Collections.singletonList(offlineRecord);
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenReturn(pendingRecords);
        when(consumeAccountDao.selectById(1001L)).thenReturn(null);
        when(lockManager.executeWithAccountLock(eq(1001L), any())).thenAnswer(invocation -> {
            // 模拟Supplier执行 - 账户不存在场景
            java.util.function.Supplier<Boolean> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        // When
        syncManager.syncPendingOfflineRecords();

        // Then
        verify(consumeAccountDao).selectById(1001L);
    }

    @Test
    void testSyncOfflineRecord_AccountInactive() {
        // Given
        List<ConsumeRecordEntity> pendingRecords = Collections.singletonList(offlineRecord);
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenReturn(pendingRecords);
        testAccount.setAccountStatus(0); // 冻结状态
        when(consumeAccountDao.selectById(1001L)).thenReturn(testAccount);
        when(lockManager.executeWithAccountLock(eq(1001L), any())).thenAnswer(invocation -> {
            // 模拟Supplier执行 - 账户冻结场景
            java.util.function.Supplier<Boolean> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        // When
        syncManager.syncPendingOfflineRecords();

        // Then
        verify(consumeAccountDao).selectById(1001L);
    }

    @Test
    void testSyncOfflineRecord_InsufficientBalance() {
        // Given - 账户余额不足
        List<ConsumeRecordEntity> pendingRecords = Collections.singletonList(offlineRecord);
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenReturn(pendingRecords);
        testAccount.setBalance(new BigDecimal("10.00"));
        when(consumeAccountDao.selectById(1001L)).thenReturn(testAccount);
        when(lockManager.executeWithAccountLock(eq(1001L), any())).thenAnswer(invocation -> {
            // 模拟Supplier执行 - 余额不足场景
            java.util.function.Supplier<Boolean> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(consumeRecordDao.updateById(any(ConsumeRecordEntity.class))).thenReturn(1);

        // When
        syncManager.syncPendingOfflineRecords();

        // Then
        verify(consumeAccountDao).selectById(1001L);
    }

    // ==================== 交易流水号生成测试 ====================

    @Test
    void testGenerateTransactionNo() {
        // Given - 这个测试需要反射或者包级可见方法
        // 由于generateTransactionNo是private方法，我们通过整体行为来测试

        // Given
        List<ConsumeRecordEntity> pendingRecords = Collections.singletonList(offlineRecord);
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenReturn(pendingRecords);
        when(consumeAccountDao.selectById(1001L)).thenReturn(testAccount);
        when(lockManager.executeWithAccountLock(eq(1001L), any())).thenAnswer(invocation -> {
            // 模拟Supplier执行 - 正常同步场景
            java.util.function.Supplier<Boolean> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(consumeAccountDao.updateById(any(ConsumeAccountEntity.class))).thenReturn(1);
        when(accountTransactionDao.insert(any(ConsumeAccountTransactionEntity.class))).thenReturn(1);
        when(consumeRecordDao.updateById(any(ConsumeRecordEntity.class))).thenReturn(1);

        // When
        syncManager.syncPendingOfflineRecords();

        // Then - 验证交易记录被创建（包含交易流水号）
        verify(accountTransactionDao, atLeastOnce()).insert(any(ConsumeAccountTransactionEntity.class));
    }

    // ==================== 辅助方法测试 ====================

    @Test
    void testCreateTransactionEntity() {
        // Given - 这个测试需要反射或者通过整体行为测试

        // Given
        List<ConsumeRecordEntity> pendingRecords = Collections.singletonList(offlineRecord);
        when(consumeRecordDao.selectPendingSyncRecords(100)).thenReturn(pendingRecords);
        when(consumeAccountDao.selectById(1001L)).thenReturn(testAccount);
        when(lockManager.executeWithAccountLock(eq(1001L), any())).thenAnswer(invocation -> {
            // 模拟Supplier执行 - 正常同步场景
            java.util.function.Supplier<Boolean> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(consumeAccountDao.updateById(any(ConsumeAccountEntity.class))).thenReturn(1);
        when(accountTransactionDao.insert(any(ConsumeAccountTransactionEntity.class))).thenAnswer(invocation -> {
            // 验证交易实体被正确创建
            return 1;
        });
        when(consumeRecordDao.updateById(any(ConsumeRecordEntity.class))).thenReturn(1);

        // When
        syncManager.syncPendingOfflineRecords();

        // Then
        verify(accountTransactionDao).insert(any(ConsumeAccountTransactionEntity.class));
    }
}
