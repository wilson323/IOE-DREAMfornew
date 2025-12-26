package net.lab1024.sa.biometric.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.biometric.dao.BiometricTemplateDao;
import net.lab1024.sa.common.entity.biometric.BiometricTemplateEntity;
import net.lab1024.sa.common.entity.biometric.BiometricType;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;

import java.util.concurrent.TimeUnit;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 生物模板管理器测试
 * <p>
 * 测试范围：
 * - 模板注册成功/失败场景
 * - 模板删除场景
 * - 模板状态更新
 * - 缓存管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("生物模板管理器测试")
class BiometricTemplateManagerTest {

    @Mock
    private BiometricTemplateDao biometricTemplateDao;

    @Mock
    private DeviceDao deviceDao;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private BiometricTemplateManager biometricTemplateManager;

    private final Long testUserId = 1001L;
    private final Integer testBiometricType = 1; // 人脸
    private final String testFeatureData = "base64encodedfeaturedata";
    private final Double testQualityScore = 0.95;

    @BeforeEach
    void setUp() {
        // lenient()宽松模式，允许不必要的stubbing
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("注册新模板 - 成功场景")
    void testRegisterTemplate_Success() {
        // Given
        when(biometricTemplateDao.selectByUserIdAndType(testUserId, testBiometricType))
                .thenReturn(Collections.emptyList());

        // When
        BiometricTemplateEntity result = biometricTemplateManager.registerTemplate(
                testUserId,
                testBiometricType,
                testFeatureData,
                testQualityScore
        );

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(testBiometricType, result.getBiometricType());
        assertEquals(BiometricTemplateEntity.TemplateStatus.ACTIVE.getCode(), result.getTemplateStatus());
        assertEquals(testFeatureData, result.getFeatureData());
        assertEquals(testQualityScore, result.getQualityScore());
        assertEquals(0.85, result.getMatchThreshold(), 0.01);
        assertEquals("1.0", result.getAlgorithmVersion());
        assertEquals("1.0", result.getTemplateVersion());
        assertEquals(0, result.getUseCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailCount());

        // 验证DAO插入调用
        verify(biometricTemplateDao, times(1)).insert(any(BiometricTemplateEntity.class));

        // 验证缓存清除
        verify(redisTemplate, times(1)).delete(contains("biometric:template:user:"));
    }

    @Test
    @DisplayName("注册新模板 - 用户已存在相同类型模板")
    void testRegisterTemplate_TemplateAlreadyExists() {
        // Given
        BiometricTemplateEntity existingTemplate = new BiometricTemplateEntity();
        existingTemplate.setUserId(testUserId);
        existingTemplate.setBiometricType(testBiometricType);

        when(biometricTemplateDao.selectByUserIdAndType(testUserId, testBiometricType))
                .thenReturn(List.of(existingTemplate));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            biometricTemplateManager.registerTemplate(
                    testUserId,
                    testBiometricType,
                    testFeatureData,
                    testQualityScore
            );
        });

        assertTrue(exception.getMessage().contains("用户已存在该类型的生物模板"));

        // 验证未执行插入
        verify(biometricTemplateDao, never()).insert(any(BiometricTemplateEntity.class));
    }

    @Test
    @DisplayName("删除模板 - 成功场景")
    void testDeleteTemplate_Success() {
        // Given
        when(biometricTemplateDao.deleteByUserIdAndType(testUserId, testBiometricType))
                .thenReturn(1);

        // When
        biometricTemplateManager.deleteTemplate(testUserId, testBiometricType);

        // Then
        verify(biometricTemplateDao, times(1))
                .deleteByUserIdAndType(testUserId, testBiometricType);
        verify(redisTemplate, times(1))
                .delete(contains("biometric:template:user:"));
    }

    @Test
    @DisplayName("更新模板状态 - 成功场景")
    void testUpdateTemplateStatus_Success() {
        // Given
        Long templateId = 1001L;
        Integer newStatus = BiometricTemplateEntity.TemplateStatus.INACTIVE.getCode();
        BiometricTemplateEntity template = new BiometricTemplateEntity();
        template.setTemplateId(templateId);
        template.setUserId(testUserId);

        when(biometricTemplateDao.selectById(templateId)).thenReturn(template);
        when(biometricTemplateDao.updateTemplateStatus(templateId, newStatus)).thenReturn(1);

        // When
        biometricTemplateManager.updateTemplateStatus(templateId, newStatus);

        // Then
        verify(biometricTemplateDao, times(1))
                .updateTemplateStatus(templateId, newStatus);
        verify(redisTemplate, times(1))
                .delete(contains("biometric:template:user:"));
    }

    @Test
    @DisplayName("更新模板状态 - 模板不存在")
    void testUpdateTemplateStatus_TemplateNotFound() {
        // Given
        Long templateId = 9999L;
        Integer newStatus = BiometricTemplateEntity.TemplateStatus.INACTIVE.getCode();

        when(biometricTemplateDao.selectById(templateId)).thenReturn(null);

        // When
        biometricTemplateManager.updateTemplateStatus(templateId, newStatus);

        // Then - 应该不抛异常，只是不清除缓存
        verify(biometricTemplateDao, times(1))
                .updateTemplateStatus(templateId, newStatus);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("生成模板名称")
    void testGenerateTemplateName() {
        // Given
        when(biometricTemplateDao.selectByUserIdAndType(testUserId, testBiometricType))
                .thenReturn(Collections.emptyList());

        // When
        BiometricTemplateEntity result = biometricTemplateManager.registerTemplate(
                testUserId,
                testBiometricType,
                testFeatureData,
                testQualityScore
        );

        // Then
        BiometricType type = BiometricType.fromCode(testBiometricType);
        assertTrue(result.getTemplateName().contains(type.getName()));
        assertTrue(result.getTemplateName().contains(testUserId.toString()));
    }

    @Test
    @DisplayName("分布式锁保护 - 防止重复注册")
    void testRegisterTemplate_WithLockProtection() {
        // Given
        when(biometricTemplateDao.selectByUserIdAndType(testUserId, testBiometricType))
                .thenReturn(Collections.emptyList());

        // 第一次获取锁成功
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(true);

        // When
        biometricTemplateManager.registerTemplate(
                testUserId,
                testBiometricType,
                testFeatureData,
                testQualityScore
        );

        // Then - 验证使用了锁
        ArgumentCaptor<String> lockKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations, times(1)).setIfAbsent(
                lockKeyCaptor.capture(),
                eq("1"),
                eq(30L),
                any(TimeUnit.class)
        );

        assertTrue(lockKeyCaptor.getValue().contains("biometric:lock:register:"));
        assertTrue(lockKeyCaptor.getValue().contains(testUserId.toString()));
        assertTrue(lockKeyCaptor.getValue().contains(testBiometricType.toString()));

        // 验证锁被释放
        verify(redisTemplate, times(1)).delete(lockKeyCaptor.getValue());
    }

    @Test
    @DisplayName("模板默认值验证")
    void testRegisterTemplate_DefaultValues() {
        // Given
        when(biometricTemplateDao.selectByUserIdAndType(testUserId, testBiometricType))
                .thenReturn(Collections.emptyList());

        // When
        BiometricTemplateEntity result = biometricTemplateManager.registerTemplate(
                testUserId,
                testBiometricType,
                testFeatureData,
                testQualityScore
        );

        // Then - 验证默认值
        assertNotNull(result.getCaptureTime());
        assertNotNull(result.getExpireTime());
        assertTrue(result.getExpireTime().isAfter(result.getCaptureTime()));
        assertEquals(0, result.getUseCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
    }

    @Test
    @DisplayName("清除缓存 - 使用正确的缓存键")
    void testClearCache_WithCorrectKey() {
        // Given
        when(biometricTemplateDao.deleteByUserIdAndType(testUserId, testBiometricType))
                .thenReturn(1);

        // When
        biometricTemplateManager.deleteTemplate(testUserId, testBiometricType);

        // Then
        ArgumentCaptor<String> cacheKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate, times(1)).delete(cacheKeyCaptor.capture());

        assertEquals("biometric:template:user:" + testUserId, cacheKeyCaptor.getValue());
    }

    @Test
    @DisplayName("并发注册 - 分布式锁冲突")
    void testRegisterTemplate_LockConflict() {
        // Given
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(false); // 锁已被占用

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            biometricTemplateManager.registerTemplate(
                    testUserId,
                    testBiometricType,
                    testFeatureData,
                    testQualityScore
            );
        });

        assertTrue(exception.getMessage().contains("模板注册中，请稍后重试"));

        // 验证未执行插入
        verify(biometricTemplateDao, never()).insert(any(BiometricTemplateEntity.class));
    }
}
