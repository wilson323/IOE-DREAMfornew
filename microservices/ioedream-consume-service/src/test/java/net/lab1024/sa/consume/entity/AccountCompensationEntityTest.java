package net.lab1024.sa.consume.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * AccountCompensationEntity 单元测试
 * <p>
 * 测试补偿记录实体的各项功能：
 * - 静态工厂方法
 * - 业务逻辑方法
 * - 指数退避算法
 * - 状态转换
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@DisplayName("AccountCompensationEntity 单元测试")
class AccountCompensationEntityTest {

    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2025, 12, 23, 10, 0, 0);
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryMethodTests {

        @Test
        @DisplayName("forIncrease - 创建余额增加补偿记录")
        void testForIncrease() {
            // Given
            Long userId = 10001L;
            BigDecimal amount = new BigDecimal("100.00");
            String businessType = "SUBSIDY_GRANT";
            String businessNo = "SUB-20251223-0001";
            String errorMessage = "账户服务暂时不可用";

            // When
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                userId, amount, businessType, businessNo, errorMessage
            );

            // Then
            assertNotNull(entity);
            assertEquals(userId, entity.getUserId());
            assertEquals(amount, entity.getAmount());
            assertEquals(businessType, entity.getBusinessType());
            assertEquals(businessNo, entity.getBusinessNo());
            assertEquals("INCREASE", entity.getOperation());
            assertEquals("PENDING", entity.getStatus());
            assertEquals(0, entity.getRetryCount());
            assertEquals(3, entity.getMaxRetryCount());
            assertEquals(errorMessage, entity.getErrorMessage());
            assertNotNull(entity.getCreateTime());
            assertNotNull(entity.getUpdateTime());
            assertNotNull(entity.getNextRetryTime());
        }

        @Test
        @DisplayName("forDecrease - 创建余额扣减补偿记录")
        void testForDecrease() {
            // Given
            Long userId = 10002L;
            BigDecimal amount = new BigDecimal("50.00");
            String businessType = "CONSUME_PAYMENT";
            String businessNo = "CON-20251223-0001";
            String errorMessage = "连接超时";

            // When
            AccountCompensationEntity entity = AccountCompensationEntity.forDecrease(
                userId, amount, businessType, businessNo, errorMessage
            );

            // Then
            assertNotNull(entity);
            assertEquals(userId, entity.getUserId());
            assertEquals(amount, entity.getAmount());
            assertEquals(businessType, entity.getBusinessType());
            assertEquals(businessNo, entity.getBusinessNo());
            assertEquals("DECREASE", entity.getOperation());
            assertEquals("PENDING", entity.getStatus());
            assertEquals(0, entity.getRetryCount());
            assertEquals(3, entity.getMaxRetryCount());
            assertEquals(errorMessage, entity.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("业务逻辑方法测试")
    class BusinessLogicMethodTests {

        @Test
        @DisplayName("canRetry - 判断是否可以重试（条件满足）")
        void testCanRetry_Success() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            entity.setNextRetryTime(LocalDateTime.now().minusMinutes(1)); // 已过重试时间

            // When
            boolean canRetry = entity.canRetry();

            // Then
            assertTrue(canRetry);
        }

        @Test
        @DisplayName("canRetry - 状态不是PENDING时不能重试")
        void testCanRetry_StatusNotPending() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            entity.setStatus("SUCCESS"); // 已成功

            // When
            boolean canRetry = entity.canRetry();

            // Then
            assertFalse(canRetry);
        }

        @Test
        @DisplayName("canRetry - 达到最大重试次数时不能重试")
        void testCanRetry_MaxRetryReached() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            entity.setRetryCount(3); // 已达到最大重试次数

            // When
            boolean canRetry = entity.canRetry();

            // Then
            assertFalse(canRetry);
        }

        @Test
        @DisplayName("canRetry - 未到重试时间时不能重试")
        void testCanRetry_NotYetRetryTime() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            entity.setNextRetryTime(LocalDateTime.now().plusMinutes(1)); // 1分钟后才可重试

            // When
            boolean canRetry = entity.canRetry();

            // Then
            assertFalse(canRetry);
        }

        @Test
        @DisplayName("isMaxRetryReached - 判断是否达到最大重试次数")
        void testIsMaxRetryReached() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );

            // When & Then
            assertFalse(entity.isMaxRetryReached()); // retryCount=0

            entity.setRetryCount(2);
            assertFalse(entity.isMaxRetryReached()); // retryCount=2

            entity.setRetryCount(3);
            assertTrue(entity.isMaxRetryReached()); // retryCount=3

            entity.setRetryCount(4);
            assertTrue(entity.isMaxRetryReached()); // retryCount=4
        }

        @Test
        @DisplayName("incrementRetry - 增加重试次数并更新重试时间")
        void testIncrementRetry() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            LocalDateTime oldNextRetryTime = entity.getNextRetryTime();

            // When
            entity.incrementRetry();

            // Then
            assertEquals(1, entity.getRetryCount());
            assertNotNull(entity.getLastRetryTime());
            assertNotEquals(oldNextRetryTime, entity.getNextRetryTime());
            assertNotNull(entity.getUpdateTime());

            // 验证指数退避：第1次重试应该在2分钟后（2^1）
            LocalDateTime expectedNextRetryTime = LocalDateTime.now().plusMinutes(2);
            assertTrue(Math.abs(entity.getNextRetryTime().getMinute() - expectedNextRetryTime.getMinute()) <= 1);
        }

        @Test
        @DisplayName("markAsSuccess - 标记为成功")
        void testMarkAsSuccess() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );

            // When
            entity.markAsSuccess();

            // Then
            assertEquals("SUCCESS", entity.getStatus());
            assertNotNull(entity.getSuccessTime());
            assertNotNull(entity.getUpdateTime());
        }

        @Test
        @DisplayName("markAsFailed - 标记为失败")
        void testMarkAsFailed() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );

            // When
            entity.markAsFailed("MAX_RETRY_REACHED", "已达到最大重试次数");

            // Then
            assertEquals("FAILED", entity.getStatus());
            assertEquals("MAX_RETRY_REACHED", entity.getErrorCode());
            assertEquals("已达到最大重试次数", entity.getErrorMessage());
            assertNotNull(entity.getUpdateTime());
        }

        @Test
        @DisplayName("cancel - 取消补偿")
        void testCancel() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );

            // When
            entity.cancel();

            // Then
            assertEquals("CANCELLED", entity.getStatus());
            assertNotNull(entity.getUpdateTime());
        }
    }

    @Nested
    @DisplayName("指数退避算法测试")
    class ExponentialBackoffTests {

        @Test
        @DisplayName("第0次重试 - 1分钟后")
        void testBackoff_RetryCount0() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );

            // When
            LocalDateTime nextRetryTime = entity.getNextRetryTime();

            // Then - 2^0 = 1分钟
            LocalDateTime expected = LocalDateTime.now().plusMinutes(1);
            assertTrue(Math.abs(nextRetryTime.getMinute() - expected.getMinute()) <= 1);
        }

        @Test
        @DisplayName("第1次重试 - 2分钟后")
        void testBackoff_RetryCount1() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            entity.incrementRetry(); // retryCount = 1

            // When
            LocalDateTime nextRetryTime = entity.getNextRetryTime();

            // Then - 2^1 = 2分钟
            LocalDateTime expected = LocalDateTime.now().plusMinutes(2);
            assertTrue(Math.abs(nextRetryTime.getMinute() - expected.getMinute()) <= 1);
        }

        @Test
        @DisplayName("第2次重试 - 4分钟后")
        void testBackoff_RetryCount2() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            entity.incrementRetry(); // retryCount = 1
            entity.incrementRetry(); // retryCount = 2

            // When
            LocalDateTime nextRetryTime = entity.getNextRetryTime();

            // Then - 2^2 = 4分钟
            LocalDateTime expected = LocalDateTime.now().plusMinutes(4);
            assertTrue(Math.abs(nextRetryTime.getMinute() - expected.getMinute()) <= 1);
        }

        @Test
        @DisplayName("第3次重试 - 8分钟后")
        void testBackoff_RetryCount3() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            entity.incrementRetry(); // retryCount = 1
            entity.incrementRetry(); // retryCount = 2
            entity.incrementRetry(); // retryCount = 3

            // When
            LocalDateTime nextRetryTime = entity.getNextRetryTime();

            // Then - 2^3 = 8分钟
            LocalDateTime expected = LocalDateTime.now().plusMinutes(8);
            assertTrue(Math.abs(nextRetryTime.getMinute() - expected.getMinute()) <= 1);
        }
    }

    @Nested
    @DisplayName("状态转换测试")
    class StateTransitionTests {

        @Test
        @DisplayName("PENDING -> SUCCESS - 正常流程")
        void testStateTransition_PendingToSuccess() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            assertEquals("PENDING", entity.getStatus());

            // When
            entity.markAsSuccess();

            // Then
            assertEquals("SUCCESS", entity.getStatus());
            assertNotNull(entity.getSuccessTime());
        }

        @Test
        @DisplayName("PENDING -> FAILED - 达到最大重试次数")
        void testStateTransition_PendingToFailed() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );
            entity.setRetryCount(3);

            // When
            entity.markAsFailed("MAX_RETRY_REACHED", "已达到最大重试次数");

            // Then
            assertEquals("FAILED", entity.getStatus());
            assertEquals("MAX_RETRY_REACHED", entity.getErrorCode());
        }

        @Test
        @DisplayName("PENDING -> CANCELLED - 用户取消")
        void testStateTransition_PendingToCancelled() {
            // Given
            AccountCompensationEntity entity = AccountCompensationEntity.forIncrease(
                10001L, new BigDecimal("100.00"), "SUBSIDY_GRANT",
                "SUB-20251223-0001", "服务不可用"
            );

            // When
            entity.cancel();

            // Then
            assertEquals("CANCELLED", entity.getStatus());
        }
    }
}
