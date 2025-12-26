package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.consume.dao.ConsumeRechargeDao;
import net.lab1024.sa.consume.domain.entity.ConsumeRechargeEntity;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeStatisticsVO;
import net.lab1024.sa.consume.exception.ConsumeRechargeException;

/**
 * ConsumeRechargeManager 单元测试
 * <p>
 * 完整的企业级单元测试，包含： - 充值业务逻辑正确性验证 - 交易唯一性检查测试 - 充值规则验证测试 - 统计数据计算测试 - 批量操作测试 - 冲正业务逻辑测试
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@ExtendWith(MockitoExtension.class)
class ConsumeRechargeManagerTest {

    @Mock
    private ConsumeRechargeDao consumeRechargeDao;

    @InjectMocks
    private ConsumeRechargeManager consumeRechargeManager;

    private ConsumeRechargeEntity testRechargeRecord;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp () {
        testTime = LocalDateTime.now ();
        testRechargeRecord = createTestRechargeRecord ();
    }

    /**
     * 创建测试充值记录
     */
    private ConsumeRechargeEntity createTestRechargeRecord () {
        ConsumeRechargeEntity record = new ConsumeRechargeEntity ();
        record.setRecordId (1L);
        record.setUserId (123L);
        record.setUserName ("测试用户");
        record.setRechargeAmount (new BigDecimal ("100.00"));
        record.setBeforeBalance (new BigDecimal ("50.00"));
        record.setAfterBalance (new BigDecimal ("150.00"));
        record.setRechargeWay (2); // 微信
        record.setRechargeChannel (1); // 线上
        record.setTransactionNo ("TEST_TXN_001");
        record.setThirdPartyNo ("WX_001");
        record.setRechargeStatus (1); // 成功
        record.setRechargeTime (testTime);
        record.setArrivalTime (testTime.plusSeconds (5));
        record.setDeviceId (1001L);
        record.setDeviceCode ("POS_001");
        record.setOperatorId (456L);
        record.setOperatorName ("操作员");
        record.setRechargeDescription ("测试充值");
        record.setBatchNo (null);
        record.setRemark ("测试备注");
        return record;
    }

    // ==================== 交易唯一性检查测试 ====================

    @Test
    void testIsTransactionUnique_Success () {
        // Given
        String transactionNo = "UNIQUE_TXN_001";
        String thirdPartyNo = "UNIQUE_WX_001";
        when (consumeRechargeDao.countByTransactionNo (transactionNo, null)).thenReturn (0);
        when (consumeRechargeDao.countByThirdPartyNo (thirdPartyNo, null)).thenReturn (0);

        // When
        boolean result = consumeRechargeManager.isTransactionUnique (transactionNo, thirdPartyNo, null);

        // Then
        assertTrue (result);
        verify (consumeRechargeDao).countByTransactionNo (transactionNo, null);
        verify (consumeRechargeDao).countByThirdPartyNo (thirdPartyNo, null);
    }

    @Test
    void testIsTransactionUnique_TransactionNoDuplicate () {
        // Given
        String transactionNo = "DUPLICATE_TXN_001";
        String thirdPartyNo = "UNIQUE_WX_001";
        when (consumeRechargeDao.countByTransactionNo (transactionNo, null)).thenReturn (1);
        // countByThirdPartyNo 不会被调用（short-circuit）

        // When
        boolean result = consumeRechargeManager.isTransactionUnique (transactionNo, thirdPartyNo, null);

        // Then
        assertFalse (result);
        verify (consumeRechargeDao).countByTransactionNo (transactionNo, null);
        verify (consumeRechargeDao, never ()).countByThirdPartyNo (anyString (), any ());
    }

    @Test
    void testIsTransactionUnique_ThirdPartyNoDuplicate () {
        // Given
        String transactionNo = "UNIQUE_TXN_001";
        String thirdPartyNo = "DUPLICATE_WX_001";
        when (consumeRechargeDao.countByTransactionNo (transactionNo, null)).thenReturn (0);
        when (consumeRechargeDao.countByThirdPartyNo (thirdPartyNo, null)).thenReturn (1);

        // When
        boolean result = consumeRechargeManager.isTransactionUnique (transactionNo, thirdPartyNo, null);

        // Then
        assertFalse (result);
        verify (consumeRechargeDao).countByTransactionNo (transactionNo, null);
        verify (consumeRechargeDao).countByThirdPartyNo (thirdPartyNo, null);
    }

    @Test
    void testIsTransactionUnique_ExcludeId () {
        // Given
        String transactionNo = "TEST_TXN_001";
        String thirdPartyNo = "TEST_WX_001";
        Long excludeId = 1L;
        when (consumeRechargeDao.countByTransactionNo (transactionNo, excludeId)).thenReturn (0);
        when (consumeRechargeDao.countByThirdPartyNo (thirdPartyNo, excludeId)).thenReturn (0);

        // When
        boolean result = consumeRechargeManager.isTransactionUnique (transactionNo, thirdPartyNo, excludeId);

        // Then
        assertTrue (result);
        verify (consumeRechargeDao).countByTransactionNo (transactionNo, excludeId);
        verify (consumeRechargeDao).countByThirdPartyNo (thirdPartyNo, excludeId);
    }

    // ==================== 交易流水号生成测试 ====================

    @Test
    void testGenerateTransactionNo () {
        // Given
        Long userId = 123L;
        Integer rechargeWay = 2;

        // When
        String result = consumeRechargeManager.generateTransactionNo (userId, rechargeWay);

        // Then
        assertNotNull (result);
        assertTrue (result.startsWith ("RCG"));
        assertTrue (result.contains ("2")); // 充值方式
        assertTrue (result.contains ("123")); // 用户ID
        assertTrue (result.length () > 20); // 包含时间戳和随机数
    }

    @Test
    void testGenerateTransactionNo_DifferentParameters () {
        // When & Then
        String result1 = consumeRechargeManager.generateTransactionNo (123L, 1);
        String result2 = consumeRechargeManager.generateTransactionNo (456L, 3);
        String result3 = consumeRechargeManager.generateTransactionNo (789L, 5);

        // Then
        assertNotEquals (result1, result2);
        assertNotEquals (result2, result3);
        assertNotEquals (result1, result3);
        assertTrue (result1.contains ("1"));
        assertTrue (result2.contains ("3"));
        assertTrue (result3.contains ("5"));
    }

    // ==================== 批次号生成测试 ====================

    @Test
    void testGenerateBatchNo () {
        // When
        String result = consumeRechargeManager.generateBatchNo ();

        // Then
        assertNotNull (result);
        assertTrue (result.startsWith ("BATCH"));
        assertTrue (result.length () > 15); // 包含时间戳和随机数
    }

    @Test
    void testGenerateBatchNo_Uniqueness () {
        // When
        String result1 = consumeRechargeManager.generateBatchNo ();
        String result2 = consumeRechargeManager.generateBatchNo ();

        // Then
        assertNotEquals (result1, result2);
    }

    // ==================== 充值金额验证测试 ====================

    @Test
    void testValidateRechargeAmount_ValidAmount () {
        // Given
        BigDecimal validAmount = new BigDecimal ("100.00");

        // When
        Map<String, Object> result = consumeRechargeManager.validateRechargeAmount (validAmount);

        // Then
        assertNotNull (result);
        assertTrue ((Boolean) result.get ("valid"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get ("errors");
        assertTrue (errors.isEmpty ());
        assertEquals (new BigDecimal ("100.00"), result.get ("validatedAmount"));
    }

    @Test
    void testValidateRechargeAmount_NullAmount () {
        // When
        Map<String, Object> result = consumeRechargeManager.validateRechargeAmount (null);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("valid"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get ("errors");
        assertTrue (errors.contains ("充值金额不能为空"));
    }

    @Test
    void testValidateRechargeAmount_ZeroAmount () {
        // Given
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // When
        Map<String, Object> result = consumeRechargeManager.validateRechargeAmount (zeroAmount);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("valid"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get ("errors");
        assertTrue (errors.contains ("充值金额必须大于0"));
        assertTrue (errors.contains ("充值金额不能小于0.01元"));
    }

    @Test
    void testValidateRechargeAmount_NegativeAmount () {
        // Given
        BigDecimal negativeAmount = new BigDecimal ("-100.00");

        // When
        Map<String, Object> result = consumeRechargeManager.validateRechargeAmount (negativeAmount);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("valid"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get ("errors");
        assertTrue (errors.contains ("充值金额必须大于0"));
    }

    @Test
    void testValidateRechargeAmount_TooLarge () {
        // Given
        BigDecimal largeAmount = new BigDecimal ("60000.00");

        // When
        Map<String, Object> result = consumeRechargeManager.validateRechargeAmount (largeAmount);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("valid"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get ("errors");
        assertTrue (errors.contains ("单次充值金额不能超过50000元"));
    }

    @Test
    void testValidateRechargeAmount_LargeAmountWarning () {
        // Given
        BigDecimal largeAmount = new BigDecimal ("20000.00");

        // When
        Map<String, Object> result = consumeRechargeManager.validateRechargeAmount (largeAmount);

        // Then
        assertNotNull (result);
        assertTrue ((Boolean) result.get ("valid"));
        @SuppressWarnings("unchecked")
        List<String> warnings = (List<String>) result.get ("warnings");
        assertTrue (warnings.contains ("充值金额较大，建议分批充值"));
    }

    @Test
    void testValidateRechargeAmount_InvalidDecimalPlaces () {
        // Given
        BigDecimal invalidAmount = new BigDecimal ("100.123");

        // When
        Map<String, Object> result = consumeRechargeManager.validateRechargeAmount (invalidAmount);

        // Then
        assertNotNull (result);
        assertTrue ((Boolean) result.get ("valid"));
        @SuppressWarnings("unchecked")
        List<String> warnings = (List<String>) result.get ("warnings");
        assertTrue (warnings.contains ("充值金额小数位数超过2位，将四舍五入处理"));
        assertEquals (new BigDecimal ("100.12"), result.get ("validatedAmount"));
    }

    // ==================== 充值规则验证测试 ====================

    @Test
    void testValidateRechargeRules_ValidRecord () {
        // Given
        ConsumeRechargeEntity validRecord = createTestRechargeRecord ();
        when (consumeRechargeDao.countByTransactionNo (anyString (), any ())).thenReturn (0);
        when (consumeRechargeDao.countByThirdPartyNo (anyString (), any ())).thenReturn (0);

        // When & Then
        assertDoesNotThrow ( () -> {
            consumeRechargeManager.validateRechargeRules (validRecord);
        });
    }

    @Test
    void testValidateRechargeRules_BusinessRuleViolation () {
        // Given
        ConsumeRechargeEntity invalidRecord = createTestRechargeRecord ();
        // 设置无效的余额关系
        invalidRecord.setBeforeBalance (new BigDecimal ("100.00"));
        invalidRecord.setAfterBalance (new BigDecimal ("150.00"));
        invalidRecord.setRechargeAmount (new BigDecimal ("30.00")); // 100 + 30 ≠ 150

        // validateBusinessRules 会先失败，不会检查交易唯一性
        // when (consumeRechargeDao.countByTransactionNo (anyString (), any ())).thenReturn (0);
        // when (consumeRechargeDao.countByThirdPartyNo (anyString (), any ())).thenReturn (0);

        // When & Then
        ConsumeRechargeException exception = assertThrows (ConsumeRechargeException.class, () -> {
            consumeRechargeManager.validateRechargeRules (invalidRecord);
        });
        // validateBusinessRules 失败抛出的是 VALIDATION_FAILED
        assertEquals (ConsumeRechargeException.ErrorCode.VALIDATION_FAILED, exception.getErrorCode ());
    }

    @Test
    void testValidateRechargeRules_DuplicateTransaction () {
        // Given
        ConsumeRechargeEntity duplicateRecord = createTestRechargeRecord ();
        when (consumeRechargeDao.countByTransactionNo (anyString (), any ())).thenReturn (1);

        // When & Then
        ConsumeRechargeException exception = assertThrows (ConsumeRechargeException.class, () -> {
            consumeRechargeManager.validateRechargeRules (duplicateRecord);
        });
        assertEquals (ConsumeRechargeException.ErrorCode.TRANSACTION_DUPLICATE, exception.getErrorCode ());
    }

    @Test
    void testValidateRechargeRules_InvalidAmount () {
        // Given
        ConsumeRechargeEntity invalidAmountRecord = createTestRechargeRecord ();
        invalidAmountRecord.setRechargeAmount (BigDecimal.ZERO);

        // validateBusinessRules 会先失败，不会检查交易唯一性
        // when (consumeRechargeDao.countByTransactionNo (anyString (), any ())).thenReturn (0);
        // when (consumeRechargeDao.countByThirdPartyNo (anyString (), any ())).thenReturn (0);

        // When & Then
        ConsumeRechargeException exception = assertThrows (ConsumeRechargeException.class, () -> {
            consumeRechargeManager.validateRechargeRules (invalidAmountRecord);
        });
        assertEquals (ConsumeRechargeException.ErrorCode.VALIDATION_FAILED, exception.getErrorCode ());
    }

    @Test
    void testValidateRechargeRules_NullRecord () {
        // When & Then - 实现直接调用方法而不检查null，会抛出NullPointerException
        assertThrows (NullPointerException.class, () -> {
            consumeRechargeManager.validateRechargeRules (null);
        });
    }

    // ==================== 异常充值检测测试 ====================

    @Test
    void testIsAbnormalRecharge_NormalRecharge () {
        // Given
        ConsumeRechargeEntity normalRecord = createTestRechargeRecord ();
        normalRecord.setRechargeAmount (new BigDecimal ("100.00"));
        normalRecord.setRechargeTime (LocalDateTime.of (2025, 12, 21, 10, 30, 0));

        // When
        boolean result = consumeRechargeManager.isAbnormalRecharge (normalRecord);

        // Then
        assertFalse (result);
    }

    @Test
    void testIsAbnormalRecharge_LargeAmount () {
        // Given
        ConsumeRechargeEntity largeAmountRecord = createTestRechargeRecord ();
        largeAmountRecord.setRechargeAmount (new BigDecimal ("15000.00"));

        // When
        boolean result = consumeRechargeManager.isAbnormalRecharge (largeAmountRecord);

        // Then
        assertTrue (result);
    }

    @Test
    void testIsAbnormalRecharge_OddHours () {
        // Given
        ConsumeRechargeEntity oddHoursRecord = createTestRechargeRecord ();
        oddHoursRecord.setRechargeTime (LocalDateTime.of (2025, 12, 21, 4, 30, 0));

        // When
        boolean result = consumeRechargeManager.isAbnormalRecharge (oddHoursRecord);

        // Then
        assertTrue (result);
    }

    @Test
    void testIsAbnormalRecharge_LargeCashAmount () {
        // Given
        ConsumeRechargeEntity largeCashRecord = createTestRechargeRecord ();
        largeCashRecord.setRechargeWay (1); // 现金
        largeCashRecord.setRechargeAmount (new BigDecimal ("6000.00"));

        // When
        boolean result = consumeRechargeManager.isAbnormalRecharge (largeCashRecord);

        // Then
        assertTrue (result);
    }

    // ==================== 统计数据计算测试 ====================

    @Test
    void testCalculateStatistics () {
        // Given
        Long userId = 123L;
        LocalDateTime startDate = LocalDateTime.of (2025, 12, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of (2025, 12, 31, 23, 59, 59);

        Map<String, Object> basicStats = new HashMap<> ();
        basicStats.put ("totalCount", 10L);
        basicStats.put ("totalAmount", new BigDecimal ("1000.00"));
        basicStats.put ("successCount", 9L);
        basicStats.put ("successAmount", new BigDecimal ("950.00"));
        basicStats.put ("failureCount", 1L);
        basicStats.put ("processingCount", 0L);

        Map<String, Object> avgStats = new HashMap<> ();
        avgStats.put ("averageAmount", new BigDecimal ("100.00"));
        avgStats.put ("maxAmount", new BigDecimal ("500.00"));
        avgStats.put ("minAmount", new BigDecimal ("10.00"));

        Map<String, Object> todayStats = new HashMap<> ();
        todayStats.put ("todayCount", 2L);
        todayStats.put ("todayAmount", new BigDecimal ("200.00"));

        Map<String, Object> monthStats = new HashMap<> ();
        monthStats.put ("monthCount", 15L);
        monthStats.put ("monthAmount", new BigDecimal ("1500.00"));

        when (consumeRechargeDao.getBasicStatistics (userId, startDate, endDate)).thenReturn (basicStats);
        when (consumeRechargeDao.getAverageStatistics (userId, startDate, endDate)).thenReturn (avgStats);
        when (consumeRechargeDao.getTodayStatistics (userId)).thenReturn (todayStats);
        when (consumeRechargeDao.getMonthStatistics (userId)).thenReturn (monthStats);

        // When
        ConsumeRechargeStatisticsVO result = consumeRechargeManager.calculateStatistics (userId, startDate, endDate);

        // Then
        assertNotNull (result);
        assertEquals (10, result.getTotalCount ());
        assertEquals (new BigDecimal ("1000.00"), result.getTotalAmount ());
        assertEquals (9L, result.getSuccessCount ());
        assertEquals (new BigDecimal ("950.00"), result.getSuccessAmount ());
        assertEquals (1L, result.getFailureCount ());
        assertEquals (0L, result.getProcessingCount ());
        // 使用 compareTo 比较 BigDecimal，避免精度问题
        assertEquals (0, new BigDecimal ("90.00").compareTo (result.getSuccessRate ())); // 9/10 * 100
        assertEquals (new BigDecimal ("100.00"), result.getAverageAmount ());
        assertEquals (new BigDecimal ("500.00"), result.getMaxAmount ());
        assertEquals (new BigDecimal ("10.00"), result.getMinAmount ());
        assertEquals (2, result.getTodayCount ());
        assertEquals (new BigDecimal ("200.00"), result.getTodayAmount ());
        assertEquals (15, result.getMonthCount ());
        assertEquals (new BigDecimal ("1500.00"), result.getMonthAmount ());
        assertNotNull (result.getStatisticsTime ());
    }

    @Test
    void testCalculateStatistics_ZeroTotalCount () {
        // Given
        Long userId = 123L;
        LocalDateTime startDate = LocalDateTime.of (2025, 12, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of (2025, 12, 31, 23, 59, 59);

        Map<String, Object> basicStats = new HashMap<> ();
        basicStats.put ("totalCount", 0L);
        basicStats.put ("totalAmount", BigDecimal.ZERO);
        basicStats.put ("successCount", 0L);
        basicStats.put ("successAmount", BigDecimal.ZERO);
        basicStats.put ("failureCount", 0L);
        basicStats.put ("processingCount", 0L);

        // 提供完整的mock数据，避免NullPointerException
        Map<String, Object> avgStats = new HashMap<> ();
        avgStats.put ("averageAmount", BigDecimal.ZERO);
        avgStats.put ("maxAmount", BigDecimal.ZERO);
        avgStats.put ("minAmount", BigDecimal.ZERO);

        Map<String, Object> todayStats = new HashMap<> ();
        todayStats.put ("todayCount", 0L);
        todayStats.put ("todayAmount", BigDecimal.ZERO);

        Map<String, Object> monthStats = new HashMap<> ();
        monthStats.put ("monthCount", 0L);
        monthStats.put ("monthAmount", BigDecimal.ZERO);

        when (consumeRechargeDao.getBasicStatistics (userId, startDate, endDate)).thenReturn (basicStats);
        when (consumeRechargeDao.getAverageStatistics (userId, startDate, endDate)).thenReturn (avgStats);
        when (consumeRechargeDao.getTodayStatistics (userId)).thenReturn (todayStats);
        when (consumeRechargeDao.getMonthStatistics (userId)).thenReturn (monthStats);

        // When
        ConsumeRechargeStatisticsVO result = consumeRechargeManager.calculateStatistics (userId, startDate, endDate);

        // Then
        assertNotNull (result);
        assertEquals (0, result.getTotalCount ());
        assertEquals (BigDecimal.ZERO, result.getTotalAmount ());
        assertEquals (0L, result.getSuccessCount ());
        assertEquals (BigDecimal.ZERO, result.getSuccessAmount ());
        // 当 totalCount 为 0 时，successRate 不会被设置，保持为 null
        assertNull (result.getSuccessRate ());
    }

    @Test
    void testCalculateStatistics_DatabaseError () {
        // Given
        Long userId = 123L;
        LocalDateTime startDate = LocalDateTime.of (2025, 12, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of (2025, 12, 31, 23, 59, 59);

        when (consumeRechargeDao.getBasicStatistics (userId, startDate, endDate))
                .thenThrow (new RuntimeException ("Database error"));

        // When & Then
        ConsumeRechargeException exception = assertThrows (ConsumeRechargeException.class, () -> {
            consumeRechargeManager.calculateStatistics (userId, startDate, endDate);
        });
        assertEquals (ConsumeRechargeException.ErrorCode.DATABASE_ERROR, exception.getErrorCode ());
    }

    // ==================== 充值方式统计测试 ====================

    @Test
    void testGetRechargeWayStatistics () {
        // Given
        LocalDateTime startDate = LocalDateTime.of (2025, 12, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of (2025, 12, 31, 23, 59, 59);

        List<Map<String, Object>> expectedStats = new ArrayList<> ();
        Map<String, Object> stat1 = new HashMap<> ();
        stat1.put ("rechargeWay", 1);
        stat1.put ("count", 5L);
        stat1.put ("amount", new BigDecimal ("500.00"));
        expectedStats.add (stat1);
        Map<String, Object> stat2 = new HashMap<> ();
        stat2.put ("rechargeWay", 2);
        stat2.put ("count", 8L);
        stat2.put ("amount", new BigDecimal ("800.00"));
        expectedStats.add (stat2);

        when (consumeRechargeDao.getRechargeWayStatistics (startDate, endDate)).thenReturn (expectedStats);

        // When
        List<Map<String, Object>> result = consumeRechargeManager.getRechargeWayStatistics (startDate, endDate);

        // Then
        assertNotNull (result);
        assertEquals (2, result.size ());
        assertEquals (expectedStats, result);
        verify (consumeRechargeDao).getRechargeWayStatistics (startDate, endDate);
    }

    @Test
    void testGetRechargeWayStatistics_DatabaseError () {
        // Given
        LocalDateTime startDate = LocalDateTime.of (2025, 12, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of (2025, 12, 31, 23, 59, 59);

        when (consumeRechargeDao.getRechargeWayStatistics (startDate, endDate))
                .thenThrow (new RuntimeException ("Database error"));

        // When & Then
        ConsumeRechargeException exception = assertThrows (ConsumeRechargeException.class, () -> {
            consumeRechargeManager.getRechargeWayStatistics (startDate, endDate);
        });
        assertEquals (ConsumeRechargeException.ErrorCode.DATABASE_ERROR, exception.getErrorCode ());
    }

    // ==================== 充值趋势数据测试 ====================

    @Test
    void testGetRechargeTrend () {
        // Given
        Integer days = 7;

        List<Map<String, Object>> expectedTrend = new ArrayList<> ();
        for (int i = 0; i < days; i++) {
            Map<String, Object> dayData = new HashMap<> ();
            dayData.put ("date", "2025-12-0" + (i + 1));
            dayData.put ("count", i + 1L);
            dayData.put ("amount", new BigDecimal ( (i + 1) * 100));
            expectedTrend.add (dayData);
        }

        when (consumeRechargeDao.getRechargeTrend (any (LocalDateTime.class), any (LocalDateTime.class)))
                .thenReturn (expectedTrend);

        // When
        List<Map<String, Object>> result = consumeRechargeManager.getRechargeTrend (days);

        // Then
        assertNotNull (result);
        assertEquals (7, result.size ());
        assertEquals (expectedTrend, result);
        verify (consumeRechargeDao).getRechargeTrend (any (LocalDateTime.class), any (LocalDateTime.class));
    }

    @Test
    void testGetRechargeTrend_DefaultDays () {
        // Given
        List<Map<String, Object>> expectedTrend = new ArrayList<> ();
        for (int i = 0; i < 7; i++) {
            expectedTrend.add (new HashMap<> ());
        }
        when (consumeRechargeDao.getRechargeTrend (any (LocalDateTime.class), any (LocalDateTime.class)))
                .thenReturn (expectedTrend);

        // When
        List<Map<String, Object>> result = consumeRechargeManager.getRechargeTrend (null);

        // Then
        assertNotNull (result);
        assertEquals (7, result.size ()); // 默认7天
        verify (consumeRechargeDao).getRechargeTrend (any (LocalDateTime.class), any (LocalDateTime.class));
    }

    @Test
    void testGetRechargeTrend_InvalidDays () {
        // Given
        Integer invalidDays = -1;
        List<Map<String, Object>> expectedTrend = new ArrayList<> ();
        for (int i = 0; i < 7; i++) {
            expectedTrend.add (new HashMap<> ());
        }
        when (consumeRechargeDao.getRechargeTrend (any (LocalDateTime.class), any (LocalDateTime.class)))
                .thenReturn (expectedTrend);

        // When
        List<Map<String, Object>> result = consumeRechargeManager.getRechargeTrend (invalidDays);

        // Then
        assertNotNull (result);
        assertEquals (7, result.size ()); // 仍然返回默认7天
        verify (consumeRechargeDao).getRechargeTrend (any (LocalDateTime.class), any (LocalDateTime.class));
    }

    // ==================== 批量充值预处理测试 ====================

    @Test
    void testPrepareBatchRecharge_Success () {
        // Given
        List<Long> userIds = List.of (123L, 456L, 789L);
        BigDecimal amount = new BigDecimal ("100.00");
        Integer rechargeWay = 2;
        Long operatorId = 1000L;

        // When
        Map<String, Object> result = consumeRechargeManager.prepareBatchRecharge (userIds, amount, rechargeWay,
                operatorId);

        // Then
        assertNotNull (result);
        assertTrue ((Boolean) result.get ("success"));
        assertEquals ("批量充值预处理成功", result.get ("message"));
        assertNotNull (result.get ("batchNo"));
        assertEquals (3, result.get ("userCount"));
        assertEquals (new BigDecimal ("300.00"), result.get ("totalAmount"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> userRecharges = (List<Map<String, Object>>) result.get ("userRecharges");
        assertEquals (3, userRecharges.size ());
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get ("errors");
        assertTrue (errors.isEmpty ());
    }

    @Test
    void testPrepareBatchRecharge_EmptyUserList () {
        // Given
        List<Long> userIds = new ArrayList<> ();
        BigDecimal amount = new BigDecimal ("100.00");

        // When
        Map<String, Object> result = consumeRechargeManager.prepareBatchRecharge (userIds, amount, 1, null);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("success"));
        assertEquals ("用户ID列表为空", result.get ("message"));
    }

    @Test
    void testPrepareBatchRecharge_InvalidAmount () {
        // Given
        List<Long> userIds = List.of (123L);
        BigDecimal invalidAmount = BigDecimal.ZERO;

        // When
        Map<String, Object> result = consumeRechargeManager.prepareBatchRecharge (userIds, invalidAmount, 1, null);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("success"));
        assertTrue (result.get ("message").toString ().contains ("充值金额验证失败"));
    }

    // ==================== 充值冲正检查测试 ====================

    @Test
    void testCheckRechargeReversibility_CanReverse () {
        // Given
        Long recordId = 1L;
        ConsumeRechargeEntity record = createTestRechargeRecord ();
        record.setRechargeStatus (1); // 成功
        record.setRechargeTime (testTime.minusDays (1)); // 1天前

        when (consumeRechargeDao.selectById (recordId)).thenReturn (record);
        when (consumeRechargeDao.countSubsequentTransactions (record.getUserId (), record.getRechargeTime ()))
                .thenReturn (0);

        // When
        Map<String, Object> result = consumeRechargeManager.checkRechargeReversibility (recordId);

        // Then
        assertNotNull (result);
        assertTrue ((Boolean) result.get ("canReverse"));
        assertEquals (record, result.get ("record"));
        @SuppressWarnings("unchecked")
        List<String> restrictions = (List<String>) result.get ("restrictions");
        assertTrue (restrictions.isEmpty ());
        assertEquals (0, result.get ("subsequentTransactions"));
    }

    @Test
    void testCheckRechargeReversibility_AlreadyReversed () {
        // Given
        Long recordId = 1L;
        ConsumeRechargeEntity record = createTestRechargeRecord ();
        record.setRechargeStatus (4); // 已冲正

        when (consumeRechargeDao.selectById (recordId)).thenReturn (record);

        // When
        Map<String, Object> result = consumeRechargeManager.checkRechargeReversibility (recordId);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("canReverse"));
        @SuppressWarnings("unchecked")
        List<String> restrictions = (List<String>) result.get ("restrictions");
        assertTrue (restrictions.contains ("只能冲正成功的充值记录"));
        assertTrue (restrictions.contains ("该充值记录已冲正"));
    }

    @Test
    void testCheckRechargeReversibility_TooOld () {
        // Given
        Long recordId = 1L;
        ConsumeRechargeEntity record = createTestRechargeRecord ();
        record.setRechargeStatus (1); // 成功
        record.setRechargeTime (testTime.minusDays (40)); // 40天前

        when (consumeRechargeDao.selectById (recordId)).thenReturn (record);

        // When
        Map<String, Object> result = consumeRechargeManager.checkRechargeReversibility (recordId);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("canReverse"));
        @SuppressWarnings("unchecked")
        List<String> restrictions = (List<String>) result.get ("restrictions");
        assertTrue (restrictions.contains ("充值记录超过30天，无法冲正"));
    }

    @Test
    void testCheckRechargeReversibility_InsufficientBalance () {
        // Given
        Long recordId = 1L;
        ConsumeRechargeEntity record = createTestRechargeRecord ();
        record.setRechargeStatus (1); // 成功
        record.setAfterBalance (new BigDecimal ("50.00"));
        record.setRechargeAmount (new BigDecimal ("100.00")); // 充值金额大于当前余额

        when (consumeRechargeDao.selectById (recordId)).thenReturn (record);

        // When
        Map<String, Object> result = consumeRechargeManager.checkRechargeReversibility (recordId);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("canReverse"));
        @SuppressWarnings("unchecked")
        List<String> restrictions = (List<String>) result.get ("restrictions");
        assertTrue (restrictions.contains ("当前账户余额不足，无法冲正"));
    }

    @Test
    void testCheckRechargeRechargeReversibility_RecordNotFound () {
        // Given
        Long recordId = 999L;
        when (consumeRechargeDao.selectById (recordId)).thenReturn (null);

        // When
        Map<String, Object> result = consumeRechargeManager.checkRechargeReversibility (recordId);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("canReverse"));
        assertEquals ("充值记录不存在", result.get ("reason"));
    }

    // ==================== 充值冲正执行测试 ====================

    @Test
    void testExecuteRechargeReversal_Success () {
        // Given
        Long recordId = 1L;
        String reason = "测试冲正";

        // 配置DAO mock让checkRechargeReversibility正常工作
        when (consumeRechargeDao.selectById (recordId)).thenReturn (createTestRechargeRecord ());
        when (consumeRechargeDao.countSubsequentTransactions (eq (123L), any (LocalDateTime.class))).thenReturn (0);
        when (consumeRechargeDao.updateById (any (ConsumeRechargeEntity.class))).thenReturn (1);
        // 使用 Answer 模拟 MyBatis-Plus 的 insert 行为（自动生成 ID）
        when (consumeRechargeDao.insert (any (ConsumeRechargeEntity.class))).thenAnswer (invocation -> {
            ConsumeRechargeEntity entity = invocation.getArgument (0);
            if (entity.getRecordId () == null) {
                entity.setRecordId (999L); // 模拟生成的 ID
            }
            return 1;
        });

        // When
        Map<String, Object> result = consumeRechargeManager.executeRechargeReversal (recordId, reason, null);

        // Then
        assertNotNull (result);
        assertTrue ((Boolean) result.get ("success"));
        assertEquals ("充值冲正成功", result.get ("message"));
        assertNotNull (result.get ("originalRecordId"));
        assertNotNull (result.get ("reversalRecordId"));
        assertEquals (testRechargeRecord.getRechargeAmount (), result.get ("reversalAmount"));
        assertNotNull (result.get ("reversalTime"));
    }

    @Test
    void testExecuteRechargeReversal_CannotReverse () {
        // Given
        Long recordId = 1L;
        String reason = "测试冲正";

        // 配置DAO mock让checkRechargeReversibility返回有后续交易的情况（无法冲正）
        when (consumeRechargeDao.selectById (recordId)).thenReturn (createTestRechargeRecord ());
        when (consumeRechargeDao.countSubsequentTransactions (eq (123L), any (LocalDateTime.class))).thenReturn (5); // 有5笔后续交易

        // When
        Map<String, Object> result = consumeRechargeManager.executeRechargeReversal (recordId, reason, null);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("success"));
        assertEquals ("充值记录无法冲正", result.get ("message"));
        @SuppressWarnings("unchecked")
        List<String> restrictions = (List<String>) result.get ("restrictions");
        // 验证确实有限制
        assertNotNull (restrictions);
        assertFalse (restrictions.isEmpty ());
    }

    @Test
    void testExecuteRechargeReversal_UpdateOriginalFailed () {
        // Given
        Long recordId = 1L;
        String reason = "测试冲正";

        // 配置DAO mock：可以冲正，但更新失败
        when (consumeRechargeDao.selectById (recordId)).thenReturn (createTestRechargeRecord ());
        when (consumeRechargeDao.countSubsequentTransactions (eq (123L), any (LocalDateTime.class))).thenReturn (0);
        when (consumeRechargeDao.updateById (any (ConsumeRechargeEntity.class))).thenReturn (0); // 更新失败

        // When
        Map<String, Object> result = consumeRechargeManager.executeRechargeReversal (recordId, reason, null);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("success"));
        assertEquals ("更新原充值记录状态失败", result.get ("message"));
    }

    @Test
    void testExecuteRechargeReversal_CreateReversalFailed () {
        // Given
        Long recordId = 1L;
        String reason = "测试冲正";

        // 配置DAO mock：可以冲正，更新成功，但插入冲正记录失败
        when (consumeRechargeDao.selectById (recordId)).thenReturn (createTestRechargeRecord ());
        when (consumeRechargeDao.countSubsequentTransactions (eq (123L), any (LocalDateTime.class))).thenReturn (0);
        when (consumeRechargeDao.updateById (any (ConsumeRechargeEntity.class))).thenReturn (1); // 更新成功
        when (consumeRechargeDao.insert (any (ConsumeRechargeEntity.class))).thenReturn (0); // 插入失败

        // When
        Map<String, Object> result = consumeRechargeManager.executeRechargeReversal (recordId, reason, null);

        // Then
        assertNotNull (result);
        assertFalse ((Boolean) result.get ("success"));
        assertEquals ("创建冲正记录失败", result.get ("message"));
    }

    // ==================== 用户充值排行测试 ====================

    @Test
    void testGetUserRechargeRanking () {
        // Given
        Integer limit = 10;
        LocalDateTime startDate = LocalDateTime.of (2025, 12, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of (2025, 12, 31, 23, 59, 59);

        List<Map<String, Object>> expectedRanking = new ArrayList<> ();
        Map<String, Object> user1 = new HashMap<> ();
        user1.put ("userId", 123L);
        user1.put ("userName", "用户A");
        user1.put ("totalAmount", new BigDecimal ("1000.00"));
        user1.put ("rechargeCount", 5L);
        expectedRanking.add (user1);

        Map<String, Object> user2 = new HashMap<> ();
        user2.put ("userId", 456L);
        user2.put ("userName", "用户B");
        user2.put ("totalAmount", new BigDecimal ("800.00"));
        user2.put ("rechargeCount", 3L);
        expectedRanking.add (user2);

        when (consumeRechargeDao.getUserRechargeRanking (startDate, endDate, limit)).thenReturn (expectedRanking);

        // When
        List<Map<String, Object>> result = consumeRechargeManager.getUserRechargeRanking (startDate, endDate, limit);

        // Then
        assertNotNull (result);
        assertEquals (2, result.size ());
        assertEquals (expectedRanking, result);
        verify (consumeRechargeDao).getUserRechargeRanking (startDate, endDate, limit);
    }

    @Test
    void testGetUserRechargeRanking_DatabaseError () {
        // Given
        Integer limit = 10;
        LocalDateTime startDate = LocalDateTime.of (2025, 12, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of (2025, 12, 31, 23, 59, 59);

        when (consumeRechargeDao.getUserRechargeRanking (startDate, endDate, limit))
                .thenThrow (new RuntimeException ("Database error"));

        // When & Then
        ConsumeRechargeException exception = assertThrows (ConsumeRechargeException.class, () -> {
            consumeRechargeManager.getUserRechargeRanking (startDate, endDate, limit);
        });
        assertEquals (ConsumeRechargeException.ErrorCode.DATABASE_ERROR, exception.getErrorCode ());
    }
}
