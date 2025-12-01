/*
 * 支付密码验证服务单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service.impl;

import net.lab1024.sa.admin.module.consume.dao.PaymentPasswordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.PaymentPasswordEntity;
import net.lab1024.sa.admin.module.consume.domain.vo.PaymentPasswordResult;
import net.lab1024.sa.admin.module.consume.domain.vo.PasswordStrengthResult;
import net.lab1024.sa.admin.module.consume.domain.vo.PasswordVerificationHistory;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.consume.service.PaymentPasswordService;
import net.lab1024.sa.base.common.cache.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 支付密码验证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("支付密码验证服务单元测试")
class PaymentPasswordServiceImplTest {

    @Mock
    private PaymentPasswordDao paymentPasswordDao;

    @Mock
    private AccountService accountService;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private PaymentPasswordServiceImpl paymentPasswordService;

    private PaymentPasswordEntity testPasswordEntity;
    private static final Long TEST_PERSON_ID = 1001L;
    private static final String TEST_PASSWORD = "Test123456";
    private static final String WRONG_PASSWORD = "Wrong123";
    private static final String SALT = "testSalt123";

    @BeforeEach
    void setUp() {
        // 设置测试配置
        ReflectionTestUtils.setField(paymentPasswordService, "maxAttempts", 5);
        ReflectionTestUtils.setField(paymentPasswordService, "lockDurationMinutes", 30);
        ReflectionTestUtils.setField(paymentPasswordService, "minPasswordLength", 6);
        ReflectionTestUtils.setField(paymentPasswordService, "requireComplexPassword", true);

        // 创建测试用的支付密码实体
        testPasswordEntity = new PaymentPasswordEntity();
        testPasswordEntity.setPasswordId(1L);
        testPasswordEntity.setPersonId(TEST_PERSON_ID);
        testPasswordEntity.setPasswordHash(hashPassword(TEST_PASSWORD, SALT));
        testPasswordEntity.setSalt(SALT);
        testPasswordEntity.setStatus("ACTIVE");
        testPasswordEntity.setExpiredTime(LocalDateTime.now().plusDays(90));
        testPasswordEntity.setCreateTime(LocalDateTime.now());
        testPasswordEntity.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试密码验证 - 成功")
    void testVerifyPassword_Success() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange
            when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);
            mockedRedis.when(() -> RedisUtil.get(anyString(), any())).thenReturn(null);
            mockedRedis.when(() -> RedisUtil.delete(anyString())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.lPush(anyString(), anyString())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.lTrim(anyString(), anyInt(), anyInt())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.hIncrBy(anyString(), anyString(), anyInt())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            // Act
            PaymentPasswordResult result = paymentPasswordService.verifyPassword(TEST_PERSON_ID, TEST_PASSWORD, 2001L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("SUCCESS", result.getResultCode());
            assertEquals("支付密码验证成功", result.getMessage());
            assertEquals("PASSWORD", result.getVerifyMethod());
            assertEquals("HIGH", result.getSecurityLevel());

            // 验证调用
            verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
        }
    }

    @Test
    @DisplayName("测试密码验证 - 密码错误")
    void testVerifyPassword_WrongPassword() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange
            when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);
            mockedRedis.when(() -> RedisUtil.get(anyString(), any())).thenReturn(null);
            mockedRedis.when(() -> RedisUtil.set(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.lPush(anyString(), anyString())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.lTrim(anyString(), anyInt(), anyInt())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.hIncrBy(anyString(), anyString(), anyInt())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            // Act
            PaymentPasswordResult result = paymentPasswordService.verifyPassword(TEST_PERSON_ID, WRONG_PASSWORD, 2001L);

            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("PASSWORD_INCORRECT", result.getErrorCode());
            assertEquals("支付密码错误", result.getMessage());
            assertEquals(Integer.valueOf(4), result.getRemainingAttempts());
            assertEquals("MEDIUM", result.getSecurityLevel());

            // 验证调用
            verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
        }
    }

    @Test
    @DisplayName("测试密码验证 - 未设置密码")
    void testVerifyPassword_PasswordNotSet() {
        // Arrange
        when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(null);

        // Act
        PaymentPasswordResult result = paymentPasswordService.verifyPassword(TEST_PERSON_ID, TEST_PASSWORD, 2001L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("PASSWORD_NOT_SET", result.getErrorCode());
        assertEquals("未设置支付密码", result.getMessage());

        verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
    }

    @Test
    @DisplayName("测试密码验证 - 密码为空")
    void testVerifyPassword_EmptyPassword() {
        // Act
        PaymentPasswordResult result = paymentPasswordService.verifyPassword(TEST_PERSON_ID, "", 2001L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("INVALID_PARAM", result.getErrorCode());
        assertEquals("密码不能为空", result.getMessage());

        // 验证不会查询数据库
        verify(paymentPasswordDao, never()).selectByPersonId(any());
    }

    @Test
    @DisplayName("测试设置支付密码 - 首次设置成功")
    void testSetPassword_FirstTimeSuccess() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange
            when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(null);
            when(paymentPasswordDao.insert(any(PaymentPasswordEntity.class))).thenReturn(1);
            mockedRedis.when(() -> RedisUtil.lPush(anyString(), anyString())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.lTrim(anyString(), anyInt(), anyInt())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.hIncrBy(anyString(), anyString(), anyInt())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            // Act
            PaymentPasswordResult result = paymentPasswordService.setPassword(TEST_PERSON_ID, null, "NewPass123", "NewPass123", 2001L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("SUCCESS", result.getResultCode());
            assertEquals("支付密码设置成功", result.getMessage());
            assertEquals("PASSWORD", result.getVerifyMethod());

            verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
            verify(paymentPasswordDao, times(1)).insert(any(PaymentPasswordEntity.class));
        }
    }

    @Test
    @DisplayName("测试设置支付密码 - 密码不一致")
    void testSetPassword_PasswordMismatch() {
        // Act
        PaymentPasswordResult result = paymentPasswordService.setPassword(TEST_PERSON_ID, null, "NewPass123", "DifferentPass", 2001L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("PASSWORD_MISMATCH", result.getErrorCode());
        assertEquals("两次输入的密码不一致", result.getMessage());

        // 验证不会查询数据库
        verify(paymentPasswordDao, never()).selectByPersonId(any());
    }

    @Test
    @DisplayName("测试修改支付密码 - 成功")
    void testChangePassword_Success() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange
            when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);
            when(paymentPasswordDao.updateById(any(PaymentPasswordEntity.class))).thenReturn(1);
            mockedRedis.when(() -> RedisUtil.delete(anyString())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.lPush(anyString(), anyString())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.lTrim(anyString(), anyInt(), anyInt())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.hIncrBy(anyString(), anyString(), anyInt())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            // Act
            PaymentPasswordResult result = paymentPasswordService.changePassword(TEST_PERSON_ID, TEST_PASSWORD, "NewPass456", "NewPass456", 2001L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("SUCCESS", result.getResultCode());
            assertEquals("支付密码更新成功", result.getMessage());

            verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
            verify(paymentPasswordDao, times(1)).updateById(any(PaymentPasswordEntity.class));
        }
    }

    @Test
    @DisplayName("测试修改支付密码 - 旧密码错误")
    void testChangePassword_WrongOldPassword() {
        // Arrange
        when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);

        // Act
            PaymentPasswordResult result = paymentPasswordService.changePassword(TEST_PERSON_ID, WRONG_PASSWORD, "NewPass456", "NewPass456", 2001L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("OLD_PASSWORD_INCORRECT", result.getErrorCode());
        assertEquals("旧密码错误", result.getMessage());

        verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
        verify(paymentPasswordDao, never()).updateById(any());
    }

    @Test
    @DisplayName("测试检查是否已设置支付密码 - 已设置")
    void testHasPaymentPassword_HasPassword() {
        // Arrange
        when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);

        // Act
        boolean result = paymentPasswordService.hasPaymentPassword(TEST_PERSON_ID);

        // Assert
        assertTrue(result);

        verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
    }

    @Test
    @DisplayName("测试检查是否已设置支付密码 - 未设置")
    void testHasPaymentPassword_NoPassword() {
        // Arrange
        when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(null);

        // Act
        boolean result = paymentPasswordService.hasPaymentPassword(TEST_PERSON_ID);

        // Assert
        assertFalse(result);

        verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
    }

    @Test
    @DisplayName("测试密码强度检查 - 强密码")
    void testCheckPasswordStrength_StrongPassword() {
        // Act
        PaymentPasswordResult result = paymentPasswordService.checkPasswordStrength("StrongPass123");

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getResultCode());
        assertEquals("密码强度检查通过", result.getMessage());
    }

    @Test
    @DisplayName("测试密码强度检查 - 密码太短")
    void testCheckPassword_PasswordTooShort() {
        // Act
        PaymentPasswordResult result = paymentPasswordService.checkPasswordStrength("123");

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("PASSWORD_TOO_SHORT", result.getErrorCode());
        assertTrue(result.getMessage().contains("密码长度不能少于6位"));
    }

    @Test
    @DisplayName("测试密码强度检查 - 密码过于简单")
    void testCheckPassword_PasswordTooSimple() {
        // Act
        PaymentPasswordResult result = paymentPasswordService.checkPasswordStrength("123456");

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("PASSWORD_TOO_SIMPLE", result.getErrorCode());
        assertEquals("密码必须包含字母和数字", result.getMessage());
    }

    @Test
    @DisplayName("测试锁定密码 - 成功")
    void testLockPassword_Success() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange
            when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);
            when(paymentPasswordDao.updateById(any(PaymentPasswordEntity.class))).thenReturn(1);
            mockedRedis.when(() -> RedisUtil.set(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            // Act
            PaymentPasswordResult result = paymentPasswordService.lockPassword(TEST_PERSON_ID, "测试锁定", 60);

            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess()); // 锁定结果通常为失败状态
            assertEquals("PASSWORD_LOCKED", result.getResultCode());
            assertTrue(result.getMessage().contains("支付密码已被锁定"));
            assertNotNull(result.getLockUntil());

            verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
            verify(paymentPasswordDao, times(1)).updateById(any(PaymentPasswordEntity.class));
        }
    }

    @Test
    @DisplayName("测试解锁密码 - 成功")
    void testUnlockPassword_Success() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange - 设置锁定状态
            testPasswordEntity.setStatus("LOCKED");
            testPasswordEntity.setLockReason("测试锁定");
            testPasswordEntity.setLockUntil(LocalDateTime.now().plusMinutes(30));
            when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);
            when(paymentPasswordDao.updateById(any(PaymentPasswordEntity.class))).thenReturn(1);
            mockedRedis.when(() -> RedisUtil.delete(anyString())).thenReturn(true);

            // Act
            PaymentPasswordResult result = paymentPasswordService.unlockPassword(TEST_PERSON_ID, "测试解锁");

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("SUCCESS", result.getResultCode());
            assertEquals("支付密码解锁成功", result.getMessage());

            verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
            verify(paymentPasswordDao, times(1)).updateById(any(PaymentPasswordEntity.class));
        }
    }

    @Test
    @DisplayName("测试检查密码是否锁定 - 未锁定")
    void testIsPasswordLocked_NotLocked() {
        // Arrange
        when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);

        // Act
        boolean result = paymentPasswordService.isPasswordLocked(TEST_PERSON_ID);

        // Assert
        assertFalse(result);

        verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
    }

    @Test
    @DisplayName("测试检查密码是否过期 - 未过期")
    void testIsPasswordExpired_NotExpired() {
        // Arrange
        testPasswordEntity.setExpiredTime(LocalDateTime.now().plusDays(30));
        when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);

        // Act
        boolean result = paymentPasswordService.isPasswordExpired(TEST_PERSON_ID);

        // Assert
        assertFalse(result);

        verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
    }

    @Test
    @DisplayName("测试更新密码过期时间 - 成功")
    void testUpdatePasswordExpiry_Success() {
        // Arrange
        when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);
        when(paymentPasswordDao.updateById(any(PaymentPasswordEntity.class))).thenReturn(1);

        // Act
        boolean result = paymentPasswordService.updatePasswordExpiry(TEST_PERSON_ID, 60);

        // Assert
        assertTrue(result);

        verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
        verify(paymentPasswordDao, times(1)).updateById(any(PaymentPasswordEntity.class));
    }

    @Test
    @DisplayName("测试生物特征验证 - 成功")
    void testVerifyBiometric_Success() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange
            String biometricData = "testBiometricData123456";
            String biometricType = "FINGERPRINT";
            mockedRedis.when(() -> RedisUtil.lPush(anyString(), anyString())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.lTrim(anyString(), anyInt(), anyInt())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any())).thenReturn(true);
            mockedRedis.when(() -> RedisUtil.hIncrBy(anyString(), anyString(), anyInt())).thenReturn(1L);
            mockedRedis.when(() -> RedisUtil.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            // Act
            PaymentPasswordResult result = paymentPasswordService.verifyBiometric(TEST_PERSON_ID, biometricData, biometricType, 2001L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("SUCCESS", result.getResultCode());
            assertEquals("生物特征验证成功", result.getMessage());
            assertEquals("BIOMETRIC_FINGERPRINT", result.getVerifyMethod());
            assertEquals("MEDIUM", result.getSecurityLevel());
        }
    }

    @Test
    @DisplayName("测试获取密码验证历史 - 有记录")
    void testGetVerificationHistory_HasRecords() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange
            String historyEntry = "2025-01-17T10:00:00|2001|192.168.1.100|SUCCESS|验证成功";
            when(RedisUtil.lRange(anyString(), eq(0), eq(19))).thenReturn(List.of(historyEntry));

            // Act
            List<PasswordVerificationHistory> result = paymentPasswordService.getVerificationHistory(TEST_PERSON_ID, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            PasswordVerificationHistory history = result.get(0);
            assertEquals("SUCCESS", history.getResult());
            assertEquals("验证成功", history.getMessage());
            assertEquals("192.168.1.100", history.getClientIp());
            assertEquals(Long.valueOf(2001), history.getDeviceId());
        }
    }

    @Test
    @DisplayName("测试检测异常尝试行为 - 无异常")
    void testDetectAbnormalAttempts_NoAbnormal() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange - 设置少量失败记录
            String historyEntry1 = "2025-01-17T10:00:00|2001|192.168.1.100|SUCCESS|验证成功";
            String historyEntry2 = "2025-01-17T10:01:00|2001|192.168.1.100|FAILED|密码错误";
            when(RedisUtil.lRange(anyString(), eq(0), eq(-1))).thenReturn(List.of(historyEntry1, historyEntry2));

            // Act
            boolean result = paymentPasswordService.detectAbnormalAttempts(TEST_PERSON_ID, 2001L, 30);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("测试检测异常尝试行为 - 有异常")
    void testDetectAbnormalAttempts_HasAbnormal() {
        try (MockedStatic<RedisUtil> mockedRedis = mockStatic(RedisUtil.class)) {
            // Arrange - 设置多次失败记录（超过最大尝试次数）
            when(RedisUtil.lRange(anyString(), eq(0), eq(-1))).thenReturn(
                List.of("2025-01-17T10:00:00|2001|192.168.1.100|FAILED|密码错误",
                       "2025-01-17T10:01:00|2001|192.168.1.100|FAILED|密码错误",
                       "2025-01-17T10:02:00|2001|192.168.1.100|FAILED|密码错误",
                       "2025-01-17T10:03:00|2001|192.168.1.100|FAILED|密码错误",
                       "2025-01-17T10:04:00|2001|192.168.1.100|FAILED|密码错误",
                       "2025-01-17T10:05:00|2001|192.168.1.100|FAILED|密码错误")
            );

            // Act
            boolean result = paymentPasswordService.detectAbnormalAttempts(TEST_PERSON_ID, 2001L, 30);

            // Assert
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("测试获取密码安全策略")
    void testGetPasswordSecurityPolicy() {
        // Act
        var policy = paymentPasswordService.getPasswordSecurityPolicy();

        // Assert
        assertNotNull(policy);
        assertEquals(Integer.valueOf(6), policy.getMinLength());
        assertEquals(Boolean.TRUE, policy.getRequireComplex());
        assertEquals(Integer.valueOf(5), policy.getMaxAttempts());
        assertEquals(Integer.valueOf(30), policy.getLockDurationMinutes());
    }

    @Test
    @DisplayName("测试设置支付密码启用状态 - 成功")
    void testSetPaymentPasswordEnabled_Success() {
        // Arrange
        when(paymentPasswordDao.selectByPersonId(TEST_PERSON_ID)).thenReturn(testPasswordEntity);
        when(paymentPasswordDao.updateById(any(PaymentPasswordEntity.class))).thenReturn(1);

        // Act
        boolean result = paymentPasswordService.setPaymentPasswordEnabled(TEST_PERSON_ID, false, "测试禁用");

        // Assert
        assertTrue(result);

        verify(paymentPasswordDao, times(1)).selectByPersonId(TEST_PERSON_ID);
        verify(paymentPasswordDao, times(1)).updateById(any(PaymentPasswordEntity.class));
    }

    /**
     * 辅助方法：哈希密码
     */
    private String hashPassword(String password, String salt) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
}