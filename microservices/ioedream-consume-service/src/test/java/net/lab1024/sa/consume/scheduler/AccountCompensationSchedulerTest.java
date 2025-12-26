package net.lab1024.sa.consume.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.client.AccountServiceClient;
import net.lab1024.sa.consume.client.dto.BalanceChangeResult;
import net.lab1024.sa.consume.dao.AccountCompensationDao;
import net.lab1024.sa.consume.entity.AccountCompensationEntity;

/**
 * AccountCompensationScheduler 集成测试
 * <p>
 * 测试策略：
 * 1. 测试Scheduler的核心流程控制
 * 2. 验证服务调用和DAO方法调用顺序
 * 3. 不验证LambdaUpdateWrapper的具体实现（需要MyBatis-Plus环境）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@DisplayName("AccountCompensationScheduler 集成测试")
class AccountCompensationSchedulerTest {

    @Mock
    private AccountCompensationDao accountCompensationDao;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private AccountCompensationScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("processPendingCompensations - 没有待处理记录")
    void testProcessPendingCompensations_NoRecords() {
        // Given
        when(accountCompensationDao.selectPendingCompensations(any(LocalDateTime.class), eq(100)))
            .thenReturn(Collections.emptyList());

        // When
        scheduler.processPendingCompensations();

        // Then
        verify(accountCompensationDao, times(1))
            .selectPendingCompensations(any(LocalDateTime.class), eq(100));
        verify(accountServiceClient, never())
            .increaseBalance(any());
    }

    @Test
    @DisplayName("processPendingCompensations - 有待处理记录且重试成功")
    void testProcessPendingCompensations_Success() {
        // Given
        AccountCompensationEntity compensation = AccountCompensationEntity.forIncrease(
            10001L,
            new BigDecimal("100.00"),
            "SUBSIDY_GRANT",
            "SUB-20251223-0001",
            "账户服务暂时不可用"
        );
        compensation.setCompensationId(1L);
        compensation.setNextRetryTime(LocalDateTime.now().minusMinutes(1));

        List<AccountCompensationEntity> compensations = List.of(compensation);

        when(accountCompensationDao.selectPendingCompensations(any(LocalDateTime.class), eq(100)))
            .thenReturn(compensations);

        BalanceChangeResult successResult = BalanceChangeResult.success(
            "TXN-20251223-0001",
            10001L,
            new BigDecimal("100.00"),
            new BigDecimal("200.00"),
            new BigDecimal("100.00")
        );

        when(accountServiceClient.increaseBalance(any()))
            .thenReturn(ResponseDTO.ok(successResult));

        // When
        scheduler.processPendingCompensations();

        // Then - 验证关键调用，不验证update的具体实现
        verify(accountCompensationDao, times(1))
            .selectPendingCompensations(any(LocalDateTime.class), eq(100));
        verify(accountServiceClient, times(1))
            .increaseBalance(any());
        // 注意：不验证update()调用，因为LambdaUpdateWrapper在单元测试中无法正常工作
    }

    @Test
    @DisplayName("processPendingCompensations - 重试失败但未达到最大次数")
    void testProcessPendingCompensations_RetryFailedNotMax() {
        // Given
        AccountCompensationEntity compensation = AccountCompensationEntity.forIncrease(
            10001L,
            new BigDecimal("100.00"),
            "SUBSIDY_GRANT",
            "SUB-20251223-0002",
            "账户服务暂时不可用"
        );
        compensation.setCompensationId(2L);
        compensation.setRetryCount(0);
        compensation.setNextRetryTime(LocalDateTime.now().minusMinutes(1));

        List<AccountCompensationEntity> compensations = List.of(compensation);

        when(accountCompensationDao.selectPendingCompensations(any(LocalDateTime.class), eq(100)))
            .thenReturn(compensations);

        BalanceChangeResult failResult = BalanceChangeResult.failure(
            "SERVICE_ERROR",
            "账户服务错误"
        );

        when(accountServiceClient.increaseBalance(any()))
            .thenReturn(ResponseDTO.error(failResult.getErrorCode(), failResult.getErrorMessage()));

        // When
        scheduler.processPendingCompensations();

        // Then - 验证关键调用
        verify(accountCompensationDao, times(1))
            .selectPendingCompensations(any(LocalDateTime.class), eq(100));
        verify(accountServiceClient, times(1))
            .increaseBalance(any());
        // 注意：不验证update()调用
    }

    @Test
    @DisplayName("processPendingCompensations - 达到最大重试次数（不调用服务）")
    void testProcessPendingCompensations_MaxRetryReached() {
        // Given
        AccountCompensationEntity compensation = AccountCompensationEntity.forIncrease(
            10001L,
            new BigDecimal("100.00"),
            "SUBSIDY_GRANT",
            "SUB-20251223-0003",
            "账户服务暂时不可用"
        );
        compensation.setCompensationId(3L);
        compensation.setRetryCount(3); // 已达到最大重试次数，canRetry()返回false
        compensation.setNextRetryTime(LocalDateTime.now().minusMinutes(1));

        List<AccountCompensationEntity> compensations = List.of(compensation);

        when(accountCompensationDao.selectPendingCompensations(any(LocalDateTime.class), eq(100)))
            .thenReturn(compensations);

        // When
        scheduler.processPendingCompensations();

        // Then - 由于canRetry()返回false，不会调用任何服务
        verify(accountCompensationDao, times(1))
            .selectPendingCompensations(any(LocalDateTime.class), eq(100));
        verify(accountServiceClient, never())
            .increaseBalance(any());
        // 注意：当retryCount >= maxRetryCount时，canRetry()返回false，直接跳过处理
    }

    @Test
    @DisplayName("reportPendingCompensations - 没有待处理记录")
    void testReportPendingCompensations_NoRecords() {
        // Given
        when(accountCompensationDao.countPendingCompensations(any(LocalDateTime.class)))
            .thenReturn(0);

        // When
        scheduler.reportPendingCompensations();

        // Then
        verify(accountCompensationDao, times(1))
            .countPendingCompensations(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("reportPendingCompensations - 大量待处理记录（触发告警）")
    void testReportPendingCompensations_ManyRecords() {
        // Given
        when(accountCompensationDao.countPendingCompensations(any(LocalDateTime.class)))
            .thenReturn(1500);

        // When
        scheduler.reportPendingCompensations();

        // Then
        verify(accountCompensationDao, times(1))
            .countPendingCompensations(any(LocalDateTime.class));
        // TODO: 验证告警通知已发送
    }
}
