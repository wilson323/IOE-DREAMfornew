package net.lab1024.sa.access.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccessBusinessException 单元测试
 * <p>
 * 测试门禁业务异常的各种构造函数和方法
 * 验证异常信息的正确性和完整性
 * 测试多模态认证和安全相关异常
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁业务异常测试")
class AccessBusinessExceptionTest {

    @Test
    @DisplayName("使用错误码构造异常")
    void testConstructorWithErrorCode() {
        // Given
        AccessBusinessException.ErrorCode errorCode = AccessBusinessException.ErrorCode.ACCESS_DENIED;

        // When
        AccessBusinessException exception = new AccessBusinessException(errorCode);

        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getDefaultMessage(), exception.getMessage());
        assertNull(exception.getBusinessId());
    }

    @Test
    @DisplayName("使用错误码和自定义消息构造异常")
    void testConstructorWithErrorCodeAndMessage() {
        // Given
        AccessBusinessException.ErrorCode errorCode = AccessBusinessException.ErrorCode.AUTHENTICATION_FAILED;
        String customMessage = "身份验证失败，请重试";

        // When
        AccessBusinessException exception = new AccessBusinessException(errorCode, customMessage);

        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(customMessage, exception.getMessage());
        assertNull(exception.getBusinessId());
    }

    @Test
    @DisplayName("使用错误码、消息和业务ID构造异常")
    void testConstructorWithErrorCodeAndMessageAndBusinessId() {
        // Given
        AccessBusinessException.ErrorCode errorCode = AccessBusinessException.ErrorCode.PERMISSION_REVOKED;
        String message = "权限已被撤销";
        String businessId = "USER001";

        // When
        AccessBusinessException exception = new AccessBusinessException(errorCode, message, businessId);

        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(businessId, exception.getBusinessId());
    }

    @Test
    @DisplayName("使用错误码、消息、业务ID和原因异常构造异常")
    void testConstructorWithAllParameters() {
        // Given
        AccessBusinessException.ErrorCode errorCode = AccessBusinessException.ErrorCode.BIOMETRIC_MATCH_FAILED;
        String message = "生物特征匹配失败";
        String businessId = "USER002";
        Throwable cause = new RuntimeException("人脸特征提取失败");

        // When
        AccessBusinessException exception = new AccessBusinessException(errorCode, message, businessId, cause);

        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(businessId, exception.getBusinessId());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("验证getCode()方法兼容性")
    void testGetCodeCompatibility() {
        // Given
        AccessBusinessException.ErrorCode errorCode = AccessBusinessException.ErrorCode.DEVICE_OFFLINE;
        AccessBusinessException exception = new AccessBusinessException(errorCode);

        // When
        String code = exception.getCode();

        // Then
        assertEquals(errorCode.getCode(), code);
    }

    @Test
    @DisplayName("验证getDetails()方法兼容性")
    void testGetDetailsCompatibility() {
        // Given
        String businessId = "USER003";
        AccessBusinessException exception = AccessBusinessException.accessDenied(businessId, "AREA001");

        // When
        Object details = exception.getDetails();

        // Then
        assertEquals(businessId + "|" + "AREA001", details);
    }

    @Test
    @DisplayName("测试访问被拒绝工厂方法")
    void testAccessDeniedFactoryMethod() {
        // Given
        Object userId = "USER001";
        Object areaId = "AREA001";

        // When
        AccessBusinessException exception = AccessBusinessException.accessDenied(userId, areaId);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        assertEquals("访问被拒绝", exception.getMessage());
        assertEquals(userId + "|" + areaId, exception.getBusinessId());
        assertEquals("ACCESS_DENIED", exception.getCode());
    }

    @Test
    @DisplayName("测试身份验证失败工厂方法")
    void testAuthenticationFailedFactoryMethod() {
        // Given
        Object userId = "USER002";

        // When
        AccessBusinessException exception = AccessBusinessException.authenticationFailed(userId);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.AUTHENTICATION_FAILED, exception.getErrorCode());
        assertEquals("身份验证失败", exception.getMessage());
        assertEquals(userId, exception.getBusinessId());
        assertEquals("AUTHENTICATION_FAILED", exception.getCode());
    }

    @Test
    @DisplayName("测试生物特征匹配失败工厂方法")
    void testBiometricMatchFailedFactoryMethod() {
        // Given
        Object userId = "USER003";
        String biometricType = "人脸识别";

        // When
        AccessBusinessException exception = AccessBusinessException.biometricMatchFailed(userId, biometricType);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.BIOMETRIC_MATCH_FAILED, exception.getErrorCode());
        assertEquals("生物特征匹配失败: " + biometricType, exception.getMessage());
        assertEquals(userId, exception.getBusinessId());
        assertEquals("BIOMETRIC_MATCH_FAILED", exception.getCode());
    }

    @Test
    @DisplayName("测试卡片不存在工厂方法")
    void testCardNotFoundFactoryMethod() {
        // Given
        String cardNumber = "1234567890";

        // When
        AccessBusinessException exception = AccessBusinessException.cardNotFound(cardNumber);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.CARD_NOT_FOUND, exception.getErrorCode());
        assertEquals("卡片不存在", exception.getMessage());
        assertEquals(cardNumber, exception.getBusinessId());
        assertEquals("CARD_NOT_FOUND", exception.getCode());
    }

    @Test
    @DisplayName("测试门禁设备离线工厂方法")
    void testDeviceOfflineFactoryMethod() {
        // Given
        Object deviceId = "DEV001";

        // When
        AccessBusinessException exception = AccessBusinessException.deviceOffline(deviceId);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.DEVICE_OFFLINE, exception.getErrorCode());
        assertEquals("门禁设备离线", exception.getMessage());
        assertEquals(deviceId, exception.getBusinessId());
        assertEquals("DEVICE_OFFLINE", exception.getCode());
    }

    @Test
    @DisplayName("测试违反反潜回规则工厂方法")
    void testAntiPassbackViolationFactoryMethod() {
        // Given
        Object userId = "USER004";
        Object deviceId = "DEV002";

        // When
        AccessBusinessException exception = AccessBusinessException.antiPassbackViolation(userId, deviceId);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.ANTI_PASSBACK_VIOLATION, exception.getErrorCode());
        assertEquals("违反反潜回规则", exception.getMessage());
        assertEquals(userId + "|" + deviceId, exception.getBusinessId());
        assertEquals("ANTI_PASSBACK_VIOLATION", exception.getCode());
    }

    @Test
    @DisplayName("测试区域访问被拒绝工厂方法")
    void testAreaAccessDeniedFactoryMethod() {
        // Given
        Object userId = "USER005";
        Object areaId = "AREA003";

        // When
        AccessBusinessException exception = AccessBusinessException.areaAccessDenied(userId, areaId);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.AREA_ACCESS_DENIED, exception.getErrorCode());
        assertEquals("区域访问被拒绝", exception.getMessage());
        assertEquals(userId + "|" + areaId, exception.getBusinessId());
        assertEquals("AREA_ACCESS_DENIED", exception.getCode());
    }

    @Test
    @DisplayName("测试多模态认证失败工厂方法")
    void testMultimodalAuthFailedFactoryMethod() {
        // Given
        Object userId = "USER006";
        String reason = "认证因子不匹配";

        // When
        AccessBusinessException exception = AccessBusinessException.multimodalAuthFailed(userId, reason);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.MULTIMODAL_AUTH_FAILED, exception.getErrorCode());
        assertEquals("多模态认证失败: " + reason, exception.getMessage());
        assertEquals(userId, exception.getBusinessId());
        assertEquals("MULTIMODAL_AUTH_FAILED", exception.getCode());
    }

    @Test
    @DisplayName("测试安全违规检测工厂方法")
    void testSecurityBreachDetectedFactoryMethod() {
        // Given
        Object userId = "USER007";
        String violation = "暴力破解尝试";

        // When
        AccessBusinessException exception = AccessBusinessException.securityBreachDetected(userId, violation);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.SECURITY_BREACH_DETECTED, exception.getErrorCode());
        assertEquals("检测到安全违规: " + violation, exception.getMessage());
        assertEquals(userId, exception.getBusinessId());
        assertEquals("SECURITY_BREACH_DETECTED", exception.getCode());
    }

    @Test
    @DisplayName("测试验证失败的工厂方法")
    void testValidationFailedFactoryMethod() {
        // Given
        java.util.List<String> errors = java.util.Arrays.asList(
                "用户ID不能为空",
                "区域ID不能为空",
                "访问时间格式错误"
        );

        // When
        AccessBusinessException exception = AccessBusinessException.validationFailed(errors);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.INVALID_PARAMETER, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("验证失败:"));
        assertTrue(exception.getMessage().contains("用户ID不能为空"));
        assertTrue(exception.getMessage().contains("区域ID不能为空"));
        assertTrue(exception.getMessage().contains("访问时间格式错误"));
        assertEquals("INVALID_PARAMETER", exception.getCode());
    }

    @Test
    @DisplayName("测试数据库操作失败工厂方法")
    void testDatabaseErrorFactoryMethod() {
        // Given
        String operation = "更新访问记录";
        String details = "数据库连接超时";

        // When
        AccessBusinessException exception = AccessBusinessException.databaseError(operation, details);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.OPERATION_NOT_SUPPORTED, exception.getErrorCode());
        assertEquals("数据库操作失败: " + operation + ", 详情: " + details, exception.getMessage());
        assertEquals("OPERATION_NOT_SUPPORTED", exception.getCode());
    }

    @Test
    @DisplayName("验证兼容性构造函数")
    void testCompatibilityConstructors() {
        // 测试只有消息的构造函数
        AccessBusinessException exception1 = new AccessBusinessException("测试消息");
        assertEquals(AccessBusinessException.ErrorCode.OPERATION_NOT_SUPPORTED, exception1.getErrorCode());
        assertEquals("测试消息", exception1.getMessage());
        assertEquals("UNKNOWN_ERROR", exception1.getBusinessId());

        // 测试代码和消息的构造函数
        AccessBusinessException exception2 = new AccessBusinessException("TEST_ERROR", "测试错误");
        assertEquals(AccessBusinessException.ErrorCode.OPERATION_NOT_SUPPORTED, exception2.getErrorCode());
        assertEquals("测试错误", exception2.getMessage());
        assertEquals("TEST_ERROR", exception2.getBusinessId());
    }

    @Test
    @DisplayName("验证错误码枚举的完整性")
    void testErrorCodeEnumCompleteness() {
        // 验证访问权限异常
        assertNotNull(AccessBusinessException.ErrorCode.ACCESS_DENIED);
        assertNotNull(AccessBusinessException.ErrorCode.PERMISSION_NOT_FOUND);
        assertNotNull(AccessBusinessException.ErrorCode.PERMISSION_EXPIRED);
        assertNotNull(AccessBusinessException.ErrorCode.PERMISSION_REVOKED);
        assertNotNull(AccessBusinessException.ErrorCode.INSUFFICIENT_PRIVILEGES);
        assertNotNull(AccessBusinessException.ErrorCode.AREA_ACCESS_DENIED);
        assertNotNull(AccessBusinessException.ErrorCode.TIME_ACCESS_DENIED);
        assertNotNull(AccessBusinessException.ErrorCode.INVALID_CREDENTIAL);

        // 验证身份验证异常
        assertNotNull(AccessBusinessException.ErrorCode.AUTHENTICATION_FAILED);
        assertNotNull(AccessBusinessException.ErrorCode.USER_NOT_FOUND);
        assertNotNull(AccessBusinessException.ErrorCode.USER_DISABLED);
        assertNotNull(AccessBusinessException.ErrorCode.USER_LOCKED);
        assertNotNull(AccessBusinessException.ErrorCode.BIOMETRIC_NOT_FOUND);
        assertNotNull(AccessBusinessException.ErrorCode.BIOMETRIC_EXPIRED);
        assertNotNull(AccessBusinessException.ErrorCode.BIOMETRIC_MATCH_FAILED);
        assertNotNull(AccessBusinessException.ErrorCode.CARD_NOT_FOUND);
        assertNotNull(AccessBusinessException.ErrorCode.CARD_EXPIRED);
        assertNotNull(AccessBusinessException.ErrorCode.CARD_DISABLED);
        assertNotNull(AccessBusinessException.ErrorCode.CARD_LOST);

        // 验证门禁设备异常
        assertNotNull(AccessBusinessException.ErrorCode.DEVICE_NOT_FOUND);
        assertNotNull(AccessBusinessException.ErrorCode.DEVICE_OFFLINE);
        assertNotNull(AccessBusinessException.ErrorCode.DEVICE_MALFUNCTION);
        assertNotNull(AccessBusinessException.ErrorCode.DEVICE_BUSY);
        assertNotNull(AccessBusinessException.ErrorCode.DEVICE_MAINTENANCE);
        assertNotNull(AccessBusinessException.ErrorCode.DOOR_CONTROL_FAILED);
        assertNotNull(AccessBusinessException.ErrorCode.DOOR_OPEN_TIMEOUT);
        assertNotNull(AccessBusinessException.ErrorCode.DOOR_FORCE_OPENED);
        assertNotNull(AccessBusinessException.ErrorCode.DOOR_NOT_CLOSED);
        assertNotNull(AccessBusinessException.ErrorCode.DOOR_SENSOR_ERROR);

        // 验证反潜回异常
        assertNotNull(AccessBusinessException.ErrorCode.ANTI_PASSBACK_VIOLATION);
        assertNotNull(AccessBusinessException.ErrorCode.DUPLICATE_ACCESS);
        assertNotNull(AccessBusinessException.ErrorCode.CONCURRENT_ACCESS);
        assertNotNull(AccessBusinessException.ErrorCode.PASSBACK_TIMEOUT);
        assertNotNull(AccessBusinessException.ErrorCode.DIRECTION_VIOLATION);
        assertNotNull(AccessBusinessException.ErrorCode.AREA_SEQUENCE_VIOLATION);

        // 验证多模态认证异常
        assertNotNull(AccessBusinessException.ErrorCode.MULTIMODAL_AUTH_FAILED);
        assertNotNull(AccessBusinessException.ErrorCode.AUTH_FACTOR_INSUFFICIENT);
        assertNotNull(AccessBusinessException.ErrorCode.AUTH_FACTOR_MISMATCH);
        assertNotNull(AccessBusinessException.ErrorCode.AUTH_SEQUENCE_INVALID);
        assertNotNull(AccessBusinessException.ErrorCode.BIOMETRIC_QUALITY_POOR);
        assertNotNull(AccessBusinessException.ErrorCode.FACE_RECOGNITION_FAILED);
        assertNotNull(AccessBusinessException.ErrorCode.FINGERPRINT_RECOGNITION_FAILED);
        assertNotNull(AccessBusinessException.ErrorCode.IRIS_RECOGNITION_FAILED);
        assertNotNull(AccessBusinessException.ErrorCode.NFC_READ_FAILED);
        assertNotNull(AccessBusinessException.ErrorCode.VERIFICATION_TIMEOUT);
    }

    @Test
    @DisplayName("验证错误码枚举的getter方法")
    void testErrorCodeEnumGetters() {
        // Given
        AccessBusinessException.ErrorCode errorCode = AccessBusinessException.ErrorCode.ACCESS_DENIED;

        // When
        String code = errorCode.getCode();
        String defaultMessage = errorCode.getDefaultMessage();

        // Then
        assertEquals("ACCESS_DENIED", code);
        assertEquals("访问被拒绝", defaultMessage);
    }

    @Test
    @DisplayName("测试异常消息的多语言支持")
    void testExceptionMessages() {
        // Given & When
        AccessBusinessException exception = AccessBusinessException.accessDenied("USER001", "AREA001");

        // Then
        assertTrue(exception.getMessage().contains("访问被拒绝"));
    }

    @Test
    @DisplayName("测试异常链传递")
    void testExceptionChaining() {
        // Given
        RuntimeException rootCause = new RuntimeException("数据库连接失败");
        AccessBusinessException exception = new AccessBusinessException(
                AccessBusinessException.ErrorCode.DEVICE_MALFUNCTION,
                "设备故障",
                "DEV001",
                rootCause
        );

        // When & Then
        assertEquals(rootCause, exception.getCause());
        assertEquals("数据库连接失败", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("测试批量操作失败工厂方法")
    void testBatchOperationFailedFactoryMethod() {
        // Given
        String operation = "批量同步用户权限";
        String reason = "网络中断";

        // When
        AccessBusinessException exception = AccessBusinessException.batchOperationFailed(operation, reason);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.BATCH_OPERATION_FAILED, exception.getErrorCode());
        assertEquals("批量" + operation + "失败: " + reason, exception.getMessage());
        assertEquals("BATCH_OPERATION_FAILED", exception.getCode());
    }

    @Test
    @DisplayName("测试系统维护中工厂方法")
    void testSystemMaintenanceFactoryMethod() {
        // When
        AccessBusinessException exception = AccessBusinessException.systemMaintenance();

        // Then
        assertEquals(AccessBusinessException.ErrorCode.SYSTEM_MAINTENANCE, exception.getErrorCode());
        assertEquals("系统维护中，请稍后重试", exception.getMessage());
        assertEquals("SYSTEM_MAINTENANCE", exception.getCode());
    }

    @Test
    @DisplayName("测试请求频率超限工厂方法")
    void testRateLimitExceededFactoryMethod() {
        // Given
        Object deviceId = "DEV001";

        // When
        AccessBusinessException exception = AccessBusinessException.rateLimitExceeded(deviceId);

        // Then
        assertEquals(AccessBusinessException.ErrorCode.RATE_LIMIT_EXCEEDED, exception.getErrorCode());
        assertEquals("请求频率超限", exception.getMessage());
        assertEquals(deviceId, exception.getBusinessId());
        assertEquals("RATE_LIMIT_EXCEEDED", exception.getCode());
    }
}