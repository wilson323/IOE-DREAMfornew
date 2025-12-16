package net.lab1024.sa.consume.manager;

import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.OfflineConsumeRecordDao;
import net.lab1024.sa.common.consume.entity.AccountEntity;
import net.lab1024.sa.common.consume.entity.OfflineConsumeRecordEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

/**
 * 离线同步管理器单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("离线同步管理器测试")
class OfflineSyncManagerTest {

    @Mock
    private OfflineConsumeRecordDao offlineConsumeRecordDao;

    @Mock
    private AccountDao accountDao;

    private OfflineSyncManager offlineSyncManager;

    @BeforeEach
    void setUp() {
        offlineSyncManager = new OfflineSyncManager(offlineConsumeRecordDao, accountDao);
    }

    @Test
    @DisplayName("保存离线记录 - 新记录")
    void saveOfflineRecord_newRecord_shouldSucceed() {
        OfflineConsumeRecordEntity record = new OfflineConsumeRecordEntity();
        record.setOfflineTransNo("OFF202512140001");
        record.setDeviceId(1L);
        record.setAmount(new BigDecimal("10.00"));

        when(offlineConsumeRecordDao.selectByOfflineTransNo("OFF202512140001")).thenReturn(null);
        doAnswer(invocation -> {
            OfflineConsumeRecordEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        }).when(offlineConsumeRecordDao).insert(any(OfflineConsumeRecordEntity.class));

        Long id = offlineSyncManager.saveOfflineRecord(record);

        assertNotNull(id);
        assertEquals(1L, id);
        assertEquals("PENDING", record.getSyncStatus());
        assertEquals(0, record.getRetryCount());
        verify(offlineConsumeRecordDao, times(1)).insert(any(OfflineConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("保存离线记录 - 重复记录")
    void saveOfflineRecord_duplicateRecord_shouldReturnExistingId() {
        OfflineConsumeRecordEntity existing = new OfflineConsumeRecordEntity();
        existing.setId(99L);
        existing.setOfflineTransNo("OFF202512140001");

        OfflineConsumeRecordEntity record = new OfflineConsumeRecordEntity();
        record.setOfflineTransNo("OFF202512140001");

        when(offlineConsumeRecordDao.selectByOfflineTransNo("OFF202512140001")).thenReturn(existing);

        Long id = offlineSyncManager.saveOfflineRecord(record);

        assertEquals(99L, id);
        verify(offlineConsumeRecordDao, never()).insert(any(OfflineConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("批量同步 - 无待同步记录")
    void syncPendingRecords_noPendingRecords_shouldReturnZero() {
        when(offlineConsumeRecordDao.selectPendingRecords(anyInt(), anyInt())).thenReturn(List.of());

        OfflineSyncManager.SyncResult result = offlineSyncManager.syncPendingRecords();

        assertEquals(0, result.total());
        assertEquals(0, result.successCount());
    }

    @Test
    @DisplayName("批量同步 - 成功同步")
    void syncPendingRecords_shouldSyncSuccessfully() {
        OfflineConsumeRecordEntity record = new OfflineConsumeRecordEntity();
        record.setId(1L);
        record.setAccountId(1L);
        record.setAmount(new BigDecimal("10.00"));
        record.setBalanceBeforeConsume(new BigDecimal("100.00"));

        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setBalance(new BigDecimal("100.00"));

        when(offlineConsumeRecordDao.selectPendingRecords(anyInt(), anyInt())).thenReturn(List.of(record));
        when(accountDao.selectById(1L)).thenReturn(account);
        when(accountDao.updateById(any(AccountEntity.class))).thenReturn(1);
        when(offlineConsumeRecordDao.updateSyncStatus(anyLong(), anyString(), any(), anyString(), any())).thenReturn(1);

        OfflineSyncManager.SyncResult result = offlineSyncManager.syncPendingRecords();

        assertEquals(1, result.successCount());
        assertEquals(0, result.conflictCount());
        assertEquals(0, result.failCount());
    }

    @Test
    @DisplayName("批量同步 - 余额不足冲突")
    void syncPendingRecords_insufficientBalance_shouldMarkConflict() {
        OfflineConsumeRecordEntity record = new OfflineConsumeRecordEntity();
        record.setId(1L);
        record.setAccountId(1L);
        record.setAmount(new BigDecimal("100.00"));

        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setBalance(new BigDecimal("50.00")); // 余额不足

        when(offlineConsumeRecordDao.selectPendingRecords(anyInt(), anyInt())).thenReturn(List.of(record));
        when(accountDao.selectById(1L)).thenReturn(account);
        when(offlineConsumeRecordDao.markConflict(anyLong(), anyString(), anyString())).thenReturn(1);

        OfflineSyncManager.SyncResult result = offlineSyncManager.syncPendingRecords();

        assertEquals(0, result.successCount());
        assertEquals(1, result.conflictCount());
        verify(offlineConsumeRecordDao, times(1)).markConflict(eq(1L), eq("BALANCE"), anyString());
    }
}
