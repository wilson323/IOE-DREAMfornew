package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import net.lab1024.sa.consume.dao.ConsumeSubsidyDao;
import net.lab1024.sa.consume.domain.vo.ConsumeSubsidyVO;
import net.lab1024.sa.consume.domain.entity.ConsumeSubsidyEntity;
import net.lab1024.sa.consume.exception.ConsumeSubsidyException;

/**
 * ConsumeSubsidyManager 单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@DisplayName("ConsumeSubsidyManager 单元测试")
class ConsumeSubsidyManagerTest {

    @Mock
    private ConsumeSubsidyDao consumeSubsidyDao;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ConsumeSubsidyManager consumeSubsidyManager;

    private ConsumeSubsidyEntity testSubsidy;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testSubsidy = createTestSubsidy();
        testTime = LocalDateTime.of(2025, 12, 22, 10, 0, 0);
    }

    private ConsumeSubsidyEntity createTestSubsidy() {
        // 确保 testTime 已初始化
        if (testTime == null) {
            testTime = LocalDateTime.of(2025, 12, 22, 10, 0, 0);
        }

        ConsumeSubsidyEntity subsidy = new ConsumeSubsidyEntity();
        subsidy.setSubsidyId(1L);
        subsidy.setSubsidyCode("SUBSIDY_001");
        subsidy.setSubsidyName("测试补贴");
        subsidy.setUserId(100L);
        subsidy.setUserName("测试用户");
        subsidy.setSubsidyType(1); // 餐饮补贴
        subsidy.setSubsidyPeriod(1); // 月度
        subsidy.setSubsidyAmount(new BigDecimal("100.00"));
        subsidy.setUsedAmount(BigDecimal.ZERO);
        subsidy.setSubsidyStatus(1); // 待发放
        subsidy.setEffectiveDate(testTime.minusDays(1));
        subsidy.setExpiryDate(testTime.plusDays(30));
        subsidy.setIssueDate(testTime);
        subsidy.setIssuerId(1L);
        subsidy.setIssuerName("管理员");
        subsidy.setRemark("测试补贴");
        subsidy.setApplicableMerchants("[\"MERCHANT_001\", \"MERCHANT_002\"]");
        subsidy.setUsageTimePeriods("[\"08:00-20:00\"]");
        subsidy.setDailyLimit(new BigDecimal("10.00"));
        subsidy.setDailyUsedAmount(BigDecimal.ZERO);
        subsidy.setDailyUsageDate(testTime.toLocalDate());
        subsidy.setMaxDiscountRate(new BigDecimal("1.0"));
        subsidy.setAutoRenew(0);  // 0表示false，1表示true
        subsidy.setRenewalRule("{}");
        return subsidy;
    }

    @Nested
    @DisplayName("补贴编码唯一性验证测试")
    class SubsidyCodeUniquenessTests {

        @Test
        @DisplayName("测试补贴编码唯一 - 返回true")
        void testIsSubsidyCodeUnique_UniqueCode_ReturnsTrue() {
            // Given
            String subsidyCode = "UNIQUE_001";
            Long excludeId = 1L;
            when(consumeSubsidyDao.countByCode(subsidyCode, excludeId)).thenReturn(0);

            // When
            boolean result = consumeSubsidyManager.isSubsidyCodeUnique(subsidyCode, excludeId);

            // Then
            assertTrue(result);
            verify(consumeSubsidyDao).countByCode(subsidyCode, excludeId);
        }

        @Test
        @DisplayName("测试补贴编码重复 - 返回false")
        void testIsSubsidyCodeUnique_DuplicateCode_ReturnsFalse() {
            // Given
            String subsidyCode = "DUPLICATE_001";
            Long excludeId = 1L;
            when(consumeSubsidyDao.countByCode(subsidyCode, excludeId)).thenReturn(1);

            // When
            boolean result = consumeSubsidyManager.isSubsidyCodeUnique(subsidyCode, excludeId);

            // Then
            assertFalse(result);
            verify(consumeSubsidyDao).countByCode(subsidyCode, excludeId);
        }

        @Test
        @DisplayName("测试补贴编码为null - 返回false")
        void testIsSubsidyCodeUnique_NullCode_ReturnsFalse() {
            // When
            boolean result = consumeSubsidyManager.isSubsidyCodeUnique(null, 1L);

            // Then
            assertFalse(result);
            verify(consumeSubsidyDao, never()).countByCode(anyString(), any());
        }

        @Test
        @DisplayName("测试补贴编码为空字符串 - 返回false")
        void testIsSubsidyCodeUnique_EmptyCode_ReturnsFalse() {
            // When
            boolean result = consumeSubsidyManager.isSubsidyCodeUnique("   ", 1L);

            // Then
            assertFalse(result);
            verify(consumeSubsidyDao, never()).countByCode(anyString(), any());
        }
    }

    @Nested
    @DisplayName("补贴可用性检查测试")
    class SubsidyUsabilityTests {

        @Test
        @DisplayName("测试补贴可使用 - 正常状态")
        void testIsSubsidyUsable_NormalStatus_ReturnsTrue() {
            // Given
            testSubsidy.setSubsidyStatus(2); // 已发放
            LocalDateTime currentTime = testTime;

            // When
            boolean result = consumeSubsidyManager.isSubsidyUsable(testSubsidy, currentTime);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("测试补贴不可使用 - 状态不可用")
        void testIsSubsidyUsable_UnusableStatus_ReturnsFalse() {
            // Given
            testSubsidy.setSubsidyStatus(3); // 已过期
            LocalDateTime currentTime = testTime;

            // When
            boolean result = consumeSubsidyManager.isSubsidyUsable(testSubsidy, currentTime);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试补贴不可使用 - 未到生效时间")
        void testIsSubsidyUsable_NotYetEffective_ReturnsFalse() {
            // Given
            testSubsidy.setSubsidyStatus(2);
            testSubsidy.setEffectiveDate(testTime.plusDays(1));
            LocalDateTime currentTime = testTime;

            // When
            boolean result = consumeSubsidyManager.isSubsidyUsable(testSubsidy, currentTime);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试补贴不可使用 - 已过期")
        void testIsSubsidyUsable_Expired_ReturnsFalse() {
            // Given
            testSubsidy.setSubsidyStatus(2);
            testSubsidy.setExpiryDate(testTime.minusDays(1));
            LocalDateTime currentTime = testTime;

            // When
            boolean result = consumeSubsidyManager.isSubsidyUsable(testSubsidy, currentTime);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试补贴不可使用 - 余额不足")
        void testIsSubsidyUsable_NoRemainingBalance_ReturnsFalse() {
            // Given
            testSubsidy.setSubsidyStatus(2);
            testSubsidy.setSubsidyAmount(new BigDecimal("100.00"));
            testSubsidy.setUsedAmount(new BigDecimal("100.00"));
            LocalDateTime currentTime = testTime;

            // When
            boolean result = consumeSubsidyManager.isSubsidyUsable(testSubsidy, currentTime);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试补贴不可使用 - 每日限额已达到")
        void testIsSubsidyUsable_DailyLimitReached_ReturnsFalse() {
            // Given
            testSubsidy.setSubsidyStatus(2);
            testSubsidy.setDailyLimit(new BigDecimal("10.00"));
            testSubsidy.setDailyUsedAmount(new BigDecimal("10.00"));
            testSubsidy.setDailyUsageDate(testTime.toLocalDate());
            LocalDateTime currentTime = testTime;

            // When
            boolean result = consumeSubsidyManager.isSubsidyUsable(testSubsidy, currentTime);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试参数为null - 返回false")
        void testIsSubsidyUsable_NullParameters_ReturnsFalse() {
            // When & Then
            assertFalse(consumeSubsidyManager.isSubsidyUsable(null, testTime));
            assertFalse(consumeSubsidyManager.isSubsidyUsable(testSubsidy, null));
        }
    }

    @Nested
    @DisplayName("可用金额计算测试")
    class AvailableAmountTests {

        @Test
        @DisplayName("测试计算可用金额 - 正常情况")
        void testCalculateAvailableAmount_NormalCase_ReturnsCorrectAmount() {
            // Given
            testSubsidy.setSubsidyStatus(2); // 已发放
            testSubsidy.setDailyLimit(null); // 清除每日限额
            BigDecimal requestedAmount = new BigDecimal("50.00");

            // When
            BigDecimal result = consumeSubsidyManager.calculateAvailableAmount(testSubsidy, requestedAmount);

            // Then
            assertEquals(new BigDecimal("50.00"), result);
        }

        @Test
        @DisplayName("测试计算可用金额 - 余额不足")
        void testCalculateAvailableAmount_InsufficientBalance_ReturnsRemainingAmount() {
            // Given
            testSubsidy.setSubsidyStatus(2);
            testSubsidy.setSubsidyAmount(new BigDecimal("100.00"));
            testSubsidy.setUsedAmount(new BigDecimal("80.00"));
            testSubsidy.setDailyLimit(null); // 清除每日限额
            BigDecimal requestedAmount = new BigDecimal("30.00");

            // When
            BigDecimal result = consumeSubsidyManager.calculateAvailableAmount(testSubsidy, requestedAmount);

            // Then
            assertEquals(new BigDecimal("20.00"), result); // 剩余余额
        }

        @Test
        @DisplayName("测试计算可用金额 - 每日限额限制")
        void testCalculateAvailableAmount_DailyLimitRestriction_ReturnsDailyRemaining() {
            // Given
            testSubsidy.setSubsidyStatus(2);
            testSubsidy.setDailyLimit(new BigDecimal("10.00"));
            testSubsidy.setDailyUsedAmount(new BigDecimal("8.00"));
            BigDecimal requestedAmount = new BigDecimal("50.00");

            // When
            BigDecimal result = consumeSubsidyManager.calculateAvailableAmount(testSubsidy, requestedAmount);

            // Then
            assertEquals(new BigDecimal("2.00"), result); // 每日剩余限额
        }

        @Test
        @DisplayName("测试计算可用金额 - 参数无效")
        void testCalculateAvailableAmount_InvalidParameters_ReturnsZero() {
            // When & Then
            assertEquals(BigDecimal.ZERO, consumeSubsidyManager.calculateAvailableAmount(null, new BigDecimal("10.00")));
            assertEquals(BigDecimal.ZERO, consumeSubsidyManager.calculateAvailableAmount(testSubsidy, null));
            testSubsidy.setSubsidyStatus(3); // 不可用
            assertEquals(BigDecimal.ZERO, consumeSubsidyManager.calculateAvailableAmount(testSubsidy, new BigDecimal("10.00")));
        }
    }

    @Nested
    @DisplayName("补贴使用测试")
    class UseSubsidyTests {

        @Test
        @DisplayName("测试使用补贴成功")
        void testUseSubsidy_Success() {
            // Given
            Long subsidyId = 1L;
            BigDecimal amount = new BigDecimal("20.00");
            testSubsidy.setSubsidyStatus(2); // 已发放
            testSubsidy.setDailyLimit(null); // 清除每日限额

            ConsumeSubsidyEntity updatedSubsidy = createTestSubsidy();
            updatedSubsidy.setUsedAmount(new BigDecimal("20.00"));
            updatedSubsidy.setRemainingAmount(new BigDecimal("80.00"));
            updatedSubsidy.setDailyLimit(null); // 清除每日限额

            // 第一次调用返回原始补贴，第二次调用返回更新后的补贴
            when(consumeSubsidyDao.selectById(subsidyId))
                .thenReturn(testSubsidy)
                .thenReturn(updatedSubsidy);
            when(consumeSubsidyDao.updateUsedAmount(eq(subsidyId), eq(amount), eq(amount))).thenReturn(1);

            // When
            Map<String, Object> result = consumeSubsidyManager.useSubsidy(subsidyId, amount);

            // Then
            assertTrue((Boolean) result.get("success"));
            assertEquals("使用成功", result.get("message"));
            assertEquals(new BigDecimal("20.00"), result.get("usedAmount"));
            assertEquals(new BigDecimal("80.00"), result.get("remainingAmount"));
        }

        @Test
        @DisplayName("测试使用补贴 - 参数错误")
        void testUseSubsidy_InvalidParameters_Failure() {
            // When
            Map<String, Object> result = consumeSubsidyManager.useSubsidy(null, new BigDecimal("10.00"));

            // Then
            assertFalse((Boolean) result.get("success"));
            assertEquals("参数错误", result.get("message"));
        }

        @Test
        @DisplayName("测试使用补贴 - 补贴不存在")
        void testUseSubsidy_SubsidyNotExists_Failure() {
            // Given
            Long subsidyId = 999L;
            BigDecimal amount = new BigDecimal("10.00");
            when(consumeSubsidyDao.selectById(subsidyId)).thenReturn(null);

            // When
            Map<String, Object> result = consumeSubsidyManager.useSubsidy(subsidyId, amount);

            // Then
            assertFalse((Boolean) result.get("success"));
            assertEquals("补贴不存在", result.get("message"));
        }

        @Test
        @DisplayName("测试使用补贴 - 不可用")
        void testUseSubsidy_NotUsable_Failure() {
            // Given
            Long subsidyId = 1L;
            BigDecimal amount = new BigDecimal("10.00");
            testSubsidy.setSubsidyStatus(3); // 已过期
            when(consumeSubsidyDao.selectById(subsidyId)).thenReturn(testSubsidy);

            // When
            Map<String, Object> result = consumeSubsidyManager.useSubsidy(subsidyId, amount);

            // Then
            assertFalse((Boolean) result.get("success"));
            assertEquals("补贴不可使用", result.get("message"));
        }

        @Test
        @DisplayName("测试使用补贴 - 更新失败")
        void testUseSubsidy_UpdateFailed_Failure() {
            // Given
            Long subsidyId = 1L;
            BigDecimal amount = new BigDecimal("10.00");
            testSubsidy.setSubsidyStatus(2);
            when(consumeSubsidyDao.selectById(subsidyId)).thenReturn(testSubsidy);
            when(consumeSubsidyDao.updateUsedAmount(eq(subsidyId), eq(amount), eq(amount))).thenReturn(0);

            // When
            Map<String, Object> result = consumeSubsidyManager.useSubsidy(subsidyId, amount);

            // Then
            assertFalse((Boolean) result.get("success"));
            assertEquals("更新使用金额失败", result.get("message"));
        }
    }

    @Nested
    @DisplayName("补贴业务规则验证测试")
    class SubsidyValidationTests {

        @Test
        @DisplayName("测试补贴验证成功 - 无异常")
        void testValidateSubsidyRules_ValidSubsidy_NoException() {
            // Given
            when(consumeSubsidyDao.countByCode(anyString(), any())).thenReturn(0);
            when(consumeSubsidyDao.countConflictingSubsidies(anyLong(), anyInt(), anyInt(), any(), anyLong())).thenReturn(0);

            // When & Then
            assertDoesNotThrow(() -> consumeSubsidyManager.validateSubsidyRules(testSubsidy));
            verify(consumeSubsidyDao).countByCode(testSubsidy.getSubsidyCode(), testSubsidy.getSubsidyId());
            verify(consumeSubsidyDao).countConflictingSubsidies(
                testSubsidy.getUserId(),
                testSubsidy.getSubsidyType(),
                testSubsidy.getSubsidyPeriod(),
                testSubsidy.getEffectiveDate(),
                testSubsidy.getSubsidyId()
            );
        }

        @Test
        @DisplayName("测试补贴验证 - 编码重复")
        void testValidateSubsidyRules_DuplicateCode_ThrowsException() {
            // Given
            when(consumeSubsidyDao.countByCode(anyString(), any())).thenReturn(1);

            // When & Then
            ConsumeSubsidyException exception = assertThrows(
                ConsumeSubsidyException.class,
                () -> consumeSubsidyManager.validateSubsidyRules(testSubsidy)
            );
            assertTrue(exception.getMessage().contains("补贴编码已存在"));
        }

        @Test
        @DisplayName("测试补贴验证 - 冲突补贴")
        void testValidateSubsidyRules_ConflictingSubsidy_ThrowsException() {
            // Given
            when(consumeSubsidyDao.countByCode(anyString(), any())).thenReturn(0);
            when(consumeSubsidyDao.countConflictingSubsidies(anyLong(), anyInt(), anyInt(), any(), anyLong())).thenReturn(1);

            // When & Then
            ConsumeSubsidyException exception = assertThrows(
                ConsumeSubsidyException.class,
                () -> consumeSubsidyManager.validateSubsidyRules(testSubsidy)
            );
            assertTrue(exception.getMessage().contains("已存在相同类型和周期的补贴"));
        }
    }

    @Nested
    @DisplayName("补贴删除检查测试")
    class DeleteCheckTests {

        @Test
        @DisplayName("检查补贴可以删除 - 待发放状态")
        void testCheckDeleteSubsidy_PendingStatus_CanDelete() {
            // Given
            Long subsidyId = 1L;
            testSubsidy.setSubsidyStatus(1); // 待发放
            testSubsidy.setEffectiveDate(testTime.plusDays(1)); // 未生效
            when(consumeSubsidyDao.selectById(subsidyId)).thenReturn(testSubsidy);

            Map<String, Long> relatedRecords = new HashMap<>();
            relatedRecords.put("usageRecords", 0L);
            relatedRecords.put("auditRecords", 2L);
            when(consumeSubsidyDao.countRelatedRecords(subsidyId)).thenReturn(relatedRecords);

            // When
            Map<String, Object> result = consumeSubsidyManager.checkDeleteSubsidy(subsidyId);

            // Then
            assertTrue((Boolean) result.get("canDelete"));
            assertEquals(relatedRecords, result.get("relatedRecords"));
        }

        @Test
        @DisplayName("检查补贴不可删除 - 已使用")
        void testCheckDeleteSubsidy_UsedStatus_CannotDelete() {
            // Given
            Long subsidyId = 1L;
            testSubsidy.setSubsidyStatus(4); // 已使用
            when(consumeSubsidyDao.selectById(subsidyId)).thenReturn(testSubsidy);

            // When
            Map<String, Object> result = consumeSubsidyManager.checkDeleteSubsidy(subsidyId);

            // Then
            assertFalse((Boolean) result.get("canDelete"));
            assertEquals("已使用或已生效的补贴不能删除，只能作废", result.get("reason"));
        }

        @Test
        @DisplayName("检查补贴不可删除 - 已生效")
        void testCheckDeleteSubsidy_EffectiveStatus_CannotDelete() {
            // Given
            Long subsidyId = 1L;
            testSubsidy.setSubsidyStatus(2); // 已发放
            testSubsidy.setEffectiveDate(testTime.minusDays(1)); // 已生效
            when(consumeSubsidyDao.selectById(subsidyId)).thenReturn(testSubsidy);

            // When
            Map<String, Object> result = consumeSubsidyManager.checkDeleteSubsidy(subsidyId);

            // Then
            assertFalse((Boolean) result.get("canDelete"));
            assertEquals("已使用或已生效的补贴不能删除，只能作废", result.get("reason"));
        }

        @Test
        @DisplayName("检查补贴不存在 - 不能删除")
        void testCheckDeleteSubsidy_SubsidyNotExists_CannotDelete() {
            // Given
            Long subsidyId = 999L;
            when(consumeSubsidyDao.selectById(subsidyId)).thenReturn(null);

            // When
            Map<String, Object> result = consumeSubsidyManager.checkDeleteSubsidy(subsidyId);

            // Then
            assertFalse((Boolean) result.get("canDelete"));
            assertEquals("补贴不存在", result.get("reason"));
        }
    }

    @Nested
    @DisplayName("定时任务测试")
    class ScheduledTaskTests {

        @Test
        @DisplayName("测试重置每日使用金额")
        void testResetDailyUsage_Success() {
            // Given
            when(consumeSubsidyDao.resetDailyUsedAmount(any(LocalDateTime.class))).thenReturn(5);

            // When
            int result = consumeSubsidyManager.resetDailyUsage();

            // Then
            assertEquals(5, result);
            verify(consumeSubsidyDao).resetDailyUsedAmount(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("测试重置每日使用金额 - 异常情况")
        void testResetDailyUsage_Exception_ReturnsZero() {
            // Given
            when(consumeSubsidyDao.resetDailyUsedAmount(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("数据库异常"));

            // When
            int result = consumeSubsidyManager.resetDailyUsage();

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("测试自动过期补贴处理")
        void testAutoExpireSubsidies_Success() {
            // Given
            when(consumeSubsidyDao.autoExpireSubsidies(any(LocalDateTime.class))).thenReturn(3);

            // When
            int result = consumeSubsidyManager.autoExpireSubsidies();

            // Then
            assertEquals(3, result);
            verify(consumeSubsidyDao).autoExpireSubsidies(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("测试自动过期补贴处理 - 异常情况")
        void testAutoExpireSubsidies_Exception_ReturnsZero() {
            // Given
            when(consumeSubsidyDao.autoExpireSubsidies(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("数据库异常"));

            // When
            int result = consumeSubsidyManager.autoExpireSubsidies();

            // Then
            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("用户补贴汇总测试")
    class UserSubsidySummaryTests {

        @Test
        @DisplayName("测试获取用户补贴汇总")
        void testGetUserSubsidySummary_Success() {
            // Given
            Long userId = 100L;
            Map<String, Object> expectedSummary = new HashMap<>();
            expectedSummary.put("totalSubsidies", 5L);
            expectedSummary.put("totalAmount", new BigDecimal("500.00"));
            expectedSummary.put("usedAmount", new BigDecimal("200.00"));
            expectedSummary.put("remainingAmount", new BigDecimal("300.00"));

            List<ConsumeSubsidyVO> expiringSoon = new ArrayList<>();
            ConsumeSubsidyVO expiringSubsidy = new ConsumeSubsidyVO();
            expiringSubsidy.setSubsidyName("即将过期补贴");
            expiringSoon.add(expiringSubsidy);

            List<ConsumeSubsidyVO> nearlyDepleted = new ArrayList<>();
            ConsumeSubsidyVO depletedSubsidy = new ConsumeSubsidyVO();
            depletedSubsidy.setSubsidyName("即将用完补贴");
            nearlyDepleted.add(depletedSubsidy);

            when(consumeSubsidyDao.getUserSubsidySummary(userId, false)).thenReturn(expectedSummary);
            when(consumeSubsidyDao.selectExpiringSoon(7, userId)).thenReturn(expiringSoon);
            when(consumeSubsidyDao.selectNearlyDepleted(new BigDecimal("0.8"), userId)).thenReturn(nearlyDepleted);

            // When
            Map<String, Object> result = consumeSubsidyManager.getUserSubsidySummary(userId);

            // Then
            assertEquals(5L, result.get("totalSubsidies"));
            assertEquals(new BigDecimal("500.00"), result.get("totalAmount"));
            assertEquals(1, result.get("expiringSoonCount"));
            assertEquals(expiringSoon, result.get("expiringSoon"));
            assertEquals(1, result.get("nearlyDepletedCount"));
            assertEquals(nearlyDepleted, result.get("nearlyDepleted"));
        }

        @Test
        @DisplayName("测试获取用户补贴汇总 - 无数据")
        void testGetUserSubsidySummary_NoData() {
            // Given
            Long userId = 999L;
            when(consumeSubsidyDao.getUserSubsidySummary(userId, false)).thenReturn(null);
            when(consumeSubsidyDao.selectExpiringSoon(7, userId)).thenReturn(new ArrayList<>());
            when(consumeSubsidyDao.selectNearlyDepleted(new BigDecimal("0.8"), userId)).thenReturn(new ArrayList<>());

            // When
            Map<String, Object> result = consumeSubsidyManager.getUserSubsidySummary(userId);

            // Then
            assertNotNull(result);
            assertEquals(0, result.get("expiringSoonCount"));
            assertEquals(0, result.get("nearlyDepletedCount"));
        }
    }

    @Nested
    @DisplayName("批量发放测试")
    class BatchIssueTests {

        @Test
        @DisplayName("测试批量发放成功")
        void testBatchIssueSubsidies_Success() {
            // Given
            List<Long> subsidyIds = List.of(1L, 2L, 3L);
            Long issuerId = 1L;
            String issuerName = "管理员";

            when(consumeSubsidyDao.selectById(1L)).thenReturn(testSubsidy);
            when(consumeSubsidyDao.selectById(2L)).thenReturn(testSubsidy);
            when(consumeSubsidyDao.selectById(3L)).thenReturn(testSubsidy);
            when(consumeSubsidyDao.batchIssueSubsidies(eq(subsidyIds), any(LocalDateTime.class), eq(issuerId), eq(issuerName)))
                .thenReturn(3);

            // When
            Map<String, Object> result = consumeSubsidyManager.batchIssueSubsidies(subsidyIds, issuerId, issuerName);

            // Then
            assertTrue((Boolean) result.get("success"));
            assertEquals("补贴批量发放成功", result.get("message"));
            assertEquals(3, result.get("issuedCount"));
        }

        @Test
        @DisplayName("测试批量发放 - 空列表")
        void testBatchIssueSubsidies_EmptyList_Failure() {
            // When
            Map<String, Object> result = consumeSubsidyManager.batchIssueSubsidies(new ArrayList<>(), 1L, "管理员");

            // Then
            assertFalse((Boolean) result.get("success"));
            assertEquals("补贴ID列表为空", result.get("message"));
        }

        @Test
        @DisplayName("测试批量发放 - 补贴状态错误")
        void testBatchIssueSubsidies_InvalidStatus_Failure() {
            // Given
            List<Long> subsidyIds = List.of(1L);
            testSubsidy.setSubsidyStatus(2); // 已发放
            when(consumeSubsidyDao.selectById(1L)).thenReturn(testSubsidy);

            // When
            Map<String, Object> result = consumeSubsidyManager.batchIssueSubsidies(subsidyIds, 1L, "管理员");

            // Then
            assertFalse((Boolean) result.get("success"));
            assertEquals("补贴发放验证失败", result.get("message"));
            assertTrue(((List<?>) result.get("errors")).get(0).toString().contains("补贴状态不是待发放"));
        }
    }

    @Nested
    @DisplayName("补贴统计测试")
    class SubsidyStatisticsTests {

        @Test
        @DisplayName("测试获取补贴统计")
        void testGetSubsidyStatistics_Success() {
            // Given
            String startDate = "2025-01-01";
            String endDate = "2025-01-31";
            Long userId = 100L;
            Long departmentId = 1L;

            Map<String, Object> basicStats = new HashMap<>();
            basicStats.put("totalSubsidies", 100L);
            basicStats.put("totalAmount", new BigDecimal("10000.00"));

            List<Map<String, Object>> typeStats = new ArrayList<>();
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("subsidyType", 1);
            typeStat.put("count", 50L);
            typeStats.add(typeStat);

            List<Map<String, Object>> deptStats = new ArrayList<>();
            List<Map<String, Object>> periodStats = new ArrayList<>();
            List<Map<String, Object>> usageLimitStats = new ArrayList<>();
            List<Map<String, Object>> balanceStats = new ArrayList<>();

            when(consumeSubsidyDao.getSubsidyStatistics(startDate, endDate, userId, departmentId)).thenReturn(basicStats);
            when(consumeSubsidyDao.countBySubsidyType(startDate, endDate)).thenReturn(typeStats);
            when(consumeSubsidyDao.countByDepartment(startDate, endDate)).thenReturn(deptStats);
            when(consumeSubsidyDao.countByPeriod()).thenReturn(periodStats);
            when(consumeSubsidyDao.countByUsageLimit()).thenReturn(usageLimitStats);
            when(consumeSubsidyDao.getBalanceDistribution(userId)).thenReturn(balanceStats);

            // When
            Map<String, Object> result = consumeSubsidyManager.getSubsidyStatistics(startDate, endDate, userId, departmentId);

            // Then
            assertEquals(100L, result.get("totalSubsidies"));
            assertEquals(new BigDecimal("10000.00"), result.get("totalAmount"));
            assertEquals(typeStats, result.get("typeStatistics"));
            assertEquals(deptStats, result.get("departmentStatistics"));
            assertEquals(periodStats, result.get("periodStatistics"));
            assertEquals(usageLimitStats, result.get("usageLimitStatistics"));
            assertEquals(balanceStats, result.get("balanceDistribution"));
        }
    }

    @Nested
    @DisplayName("JSON解析测试")
    class JsonParsingTests {

        @Test
        @DisplayName("测试解析商户ID列表")
        void testParseApplicableMerchants_Success() throws JsonProcessingException {
            // Given
            String merchantsJson = "[\"MERCHANT_001\", \"MERCHANT_002\", \"MERCHANT_003\"]";

            // When
            List<String> result = consumeSubsidyManager.parseApplicableMerchants(merchantsJson);

            // Then
            assertEquals(3, result.size());
            assertTrue(result.contains("MERCHANT_001"));
            assertTrue(result.contains("MERCHANT_002"));
            assertTrue(result.contains("MERCHANT_003"));
        }

        @Test
        @DisplayName("测试解析商户ID列表 - 空字符串")
        void testParseApplicableMerchants_EmptyString() throws JsonProcessingException {
            // When
            List<String> result = consumeSubsidyManager.parseApplicableMerchants("");

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("测试解析商户ID列表 - null")
        void testParseApplicableMerchants_NullString() throws JsonProcessingException {
            // When
            List<String> result = consumeSubsidyManager.parseApplicableMerchants(null);

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("测试解析使用时间段")
        void testParseUsageTimePeriods_Success() throws JsonProcessingException {
            // Given
            String periodsJson = "[\"08:00-12:00\", \"14:00-18:00\"]";

            // When
            List<String> result = consumeSubsidyManager.parseUsageTimePeriods(periodsJson);

            // Then
            assertEquals(2, result.size());
            assertTrue(result.contains("08:00-12:00"));
            assertTrue(result.contains("14:00-18:00"));
        }

        @Test
        @DisplayName("测试解析续期规则")
        void testParseRenewalRule_Success() throws JsonProcessingException {
            // Given
            String ruleJson = "{\"renewalDays\": 30, \"renewalType\": \"auto\"}";

            // When
            Map<String, Object> result = consumeSubsidyManager.parseRenewalRule(ruleJson);

            // Then
            assertEquals(30, result.get("renewalDays"));
            assertEquals("auto", result.get("renewalType"));
        }
    }

    @Nested
    @DisplayName("业务工具方法测试")
    class BusinessUtilityTests {

        @Test
        @DisplayName("测试计算使用率")
        void testCalculateUsageRate_Success() {
            // When & Then
            assertEquals(0, new BigDecimal("50.0000").compareTo(consumeSubsidyManager.calculateUsageRate(new BigDecimal("100.00"), new BigDecimal("50.00"))));
            assertEquals(0, BigDecimal.ZERO.compareTo(consumeSubsidyManager.calculateUsageRate(new BigDecimal("100.00"), BigDecimal.ZERO)));
            assertEquals(0, BigDecimal.ZERO.compareTo(consumeSubsidyManager.calculateUsageRate(BigDecimal.ZERO, new BigDecimal("50.00"))));
            assertEquals(0, BigDecimal.ZERO.compareTo(consumeSubsidyManager.calculateUsageRate(null, new BigDecimal("50.00"))));
        }

        @Test
        @DisplayName("测试检查即将过期")
        void testIsExpiringSoon_Success() {
            // Given
            ConsumeSubsidyEntity expiringSoon = createTestSubsidy();
            expiringSoon.setExpiryDate(LocalDateTime.now().plusDays(3)); // 3天后过期

            ConsumeSubsidyEntity notExpiringSoon = createTestSubsidy();
            notExpiringSoon.setExpiryDate(LocalDateTime.now().plusDays(10)); // 10天后过期

            ConsumeSubsidyEntity noExpiryDate = createTestSubsidy();
            noExpiryDate.setExpiryDate(null); // 无过期日期

            // When & Then
            assertTrue(consumeSubsidyManager.isExpiringSoon(expiringSoon, 7)); // 7天阈值
            assertFalse(consumeSubsidyManager.isExpiringSoon(notExpiringSoon, 7));
            assertFalse(consumeSubsidyManager.isExpiringSoon(null, 7)); // null补贴
            assertFalse(consumeSubsidyManager.isExpiringSoon(noExpiryDate, 7)); // 无过期日期
        }

        @Test
        @DisplayName("测试检查即将用完")
        void testIsNearlyDepleted_Success() {
            // Given
            ConsumeSubsidyEntity nearlyDepleted = createTestSubsidy();
            nearlyDepleted.setSubsidyAmount(new BigDecimal("100.00"));
            nearlyDepleted.setUsedAmount(new BigDecimal("85.00")); // 85%使用率

            ConsumeSubsidyEntity notNearlyDepleted = createTestSubsidy();
            notNearlyDepleted.setSubsidyAmount(new BigDecimal("100.00"));
            notNearlyDepleted.setUsedAmount(new BigDecimal("30.00")); // 30%使用率

            // When & Then
            assertTrue(consumeSubsidyManager.isNearlyDepleted(nearlyDepleted, new BigDecimal("0.8"))); // 80%阈值
            assertFalse(consumeSubsidyManager.isNearlyDepleted(notNearlyDepleted, new BigDecimal("0.8")));
            assertFalse(consumeSubsidyManager.isNearlyDepleted(null, new BigDecimal("0.8"))); // null补贴
        }
    }
}