package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.consume.dao.ConsumeDeviceDao;
import net.lab1024.sa.consume.domain.entity.ConsumeDeviceEntity;
import net.lab1024.sa.consume.exception.ConsumeDeviceException;

/**
 * ConsumeDeviceManager 单元测试
 * <p>
 * 完整的企业级单元测试，包含：
 * - 业务逻辑正确性验证
 * - 异常情况处理测试
 * - 边界条件测试
 * - 数据验证测试
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@ExtendWith(MockitoExtension.class)
class ConsumeDeviceManagerTest {

    @Mock
    private ConsumeDeviceDao consumeDeviceDao;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ConsumeDeviceManager consumeDeviceManager;

    private ConsumeDeviceEntity testDevice;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();
        testDevice = createTestDevice();
    }

    /**
     * 创建测试设备
     */
    private ConsumeDeviceEntity createTestDevice() {
        ConsumeDeviceEntity device = new ConsumeDeviceEntity();
        device.setDeviceId(1L);
        device.setDeviceCode("TEST_001");
        device.setDeviceName("测试设备");
        device.setDeviceType(1);
        device.setDeviceStatus(1);
        device.setDeviceLocation("测试位置");
        device.setIpAddress("192.168.1.100");
        device.setMacAddress("00:11:22:33:44:55");
        device.setDeviceModel("TEST-1000");
        device.setDeviceManufacturer("测试厂商");
        device.setFirmwareVersion("v2.1.0");  // 使用最新固件版本，避免健康检查返回"需要更新"
        device.setSupportOffline(1);
        device.setBusinessAttributes("{\"test\": \"value\"}");
        device.setLastCommunicationTime(testTime);
        device.setHealthStatus("正常");
        setFinalField(device, "createTime", testTime);
        setFinalField(device, "updateTime", testTime);
        return device;
    }

    /**
     * 使用反射设置final字段
     */
    private void setFinalField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            // 忽略反射异常
        }
    }

    // ==================== 设备编码唯一性测试 ====================

    @Test
    void testIsDeviceCodeUnique_Success() {
        // Given
        String deviceCode = "UNIQUE_001";
        when(consumeDeviceDao.countByDeviceCode(deviceCode, null)).thenReturn(0);

        // When
        boolean result = consumeDeviceManager.isDeviceCodeUnique(deviceCode, null);

        // Then
        assertTrue(result);
        verify(consumeDeviceDao).countByDeviceCode(deviceCode, null);
    }

    @Test
    void testIsDeviceCodeUnique_Duplicate() {
        // Given
        String deviceCode = "DUPLICATE_001";
        when(consumeDeviceDao.countByDeviceCode(deviceCode, null)).thenReturn(1);

        // When
        boolean result = consumeDeviceManager.isDeviceCodeUnique(deviceCode, null);

        // Then
        assertFalse(result);
        verify(consumeDeviceDao).countByDeviceCode(deviceCode, null);
    }

    @Test
    void testIsDeviceCodeUnique_EmptyCode() {
        // When
        boolean result1 = consumeDeviceManager.isDeviceCodeUnique(null, null);
        boolean result2 = consumeDeviceManager.isDeviceCodeUnique("", null);
        boolean result3 = consumeDeviceManager.isDeviceCodeUnique("   ", null);

        // Then
        assertFalse(result1);
        assertFalse(result2);
        assertFalse(result3);
    }

    @Test
    void testIsDeviceCodeUnique_ExcludeId() {
        // Given
        String deviceCode = "TEST_001";
        Long excludeId = 1L;
        when(consumeDeviceDao.countByDeviceCode(deviceCode, excludeId)).thenReturn(0);

        // When
        boolean result = consumeDeviceManager.isDeviceCodeUnique(deviceCode, excludeId);

        // Then
        assertTrue(result);
        verify(consumeDeviceDao).countByDeviceCode(deviceCode, excludeId);
    }

    // ==================== IP地址唯一性测试 ====================

    @Test
    void testIsIpAddressUnique_Success() {
        // Given
        String ipAddress = "192.168.1.100";
        when(consumeDeviceDao.countByIpAddress(ipAddress, null)).thenReturn(0);

        // When
        boolean result = consumeDeviceManager.isIpAddressUnique(ipAddress, null);

        // Then
        assertTrue(result);
        verify(consumeDeviceDao).countByIpAddress(ipAddress, null);
    }

    @Test
    void testIsIpAddressUnique_EmptyIp() {
        // When
        boolean result = consumeDeviceManager.isIpAddressUnique(null, null);

        // Then
        assertTrue(result); // IP地址可以为空
        verify(consumeDeviceDao, never()).countByIpAddress(anyString(), any());
    }

    @Test
    void testIsIpAddressUnique_Duplicate() {
        // Given
        String ipAddress = "192.168.1.100";
        when(consumeDeviceDao.countByIpAddress(ipAddress, null)).thenReturn(1);

        // When
        boolean result = consumeDeviceManager.isIpAddressUnique(ipAddress, null);

        // Then
        assertFalse(result);
        verify(consumeDeviceDao).countByIpAddress(ipAddress, null);
    }

    // ==================== MAC地址唯一性测试 ====================

    @Test
    void testIsMacAddressUnique_Success() {
        // Given
        String macAddress = "00:11:22:33:44:55";
        when(consumeDeviceDao.countByMacAddress(macAddress, null)).thenReturn(0);

        // When
        boolean result = consumeDeviceManager.isMacAddressUnique(macAddress, null);

        // Then
        assertTrue(result);
        verify(consumeDeviceDao).countByMacAddress(macAddress, null);
    }

    @Test
    void testIsMacAddressUnique_EmptyMac() {
        // When
        boolean result = consumeDeviceManager.isMacAddressUnique(null, null);

        // Then
        assertTrue(result); // MAC地址可以为空
        verify(consumeDeviceDao, never()).countByMacAddress(anyString(), any());
    }

    // ==================== 设备可操作性测试 ====================

    @Test
    void testIsDeviceOperable_OnlineDevice() {
        // Given
        testDevice.setDeviceStatus(1); // 在线
        String operation = "test";

        // When
        boolean result = consumeDeviceManager.isDeviceOperable(testDevice, operation);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsDeviceOperable_OfflineDevice() {
        // Given
        testDevice.setDeviceStatus(2); // 离线
        String operation = "restart";

        // When
        boolean result = consumeDeviceManager.isDeviceOperable(testDevice, operation);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsDeviceOperable_MaintenanceDevice() {
        // Given
        testDevice.setDeviceStatus(4); // 维护中
        String operation = "test";

        // When
        boolean result = consumeDeviceManager.isDeviceOperable(testDevice, operation);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsDeviceOperable_DisabledDevice() {
        // Given
        testDevice.setDeviceStatus(5); // 停用
        String operation = "test";

        // When
        boolean result = consumeDeviceManager.isDeviceOperable(testDevice, operation);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsDeviceOperable_NullDevice() {
        // When
        boolean result = consumeDeviceManager.isDeviceOperable(null, "test");

        // Then
        assertFalse(result);
    }

    // ==================== 设备健康检查测试 ====================

    @Test
    void testCheckDeviceHealth_HealthyDevice() {
        // Given
        testDevice.setLastCommunicationTime(testTime.minusMinutes(10));
        testDevice.setDeviceStatus(1);

        // When
        Map<String, Object> result = consumeDeviceManager.checkDeviceHealth(testDevice, testTime);

        // Then
        assertNotNull(result);
        assertEquals("正常", result.get("healthStatus"));
        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) result.get("issues");
        assertTrue(issues.isEmpty());
        assertFalse((Boolean) result.get("needsAttention"));
    }

    @Test
    void testCheckDeviceHealth_LowBatteryWarning() {
        // Given
        testDevice.setLastCommunicationTime(testTime.minusMinutes(45));
        testDevice.setDeviceStatus(1);

        // When
        Map<String, Object> result = consumeDeviceManager.checkDeviceHealth(testDevice, testTime);

        // Then
        assertNotNull(result);
        assertEquals("警告", result.get("healthStatus"));
        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) result.get("issues");
        assertTrue(issues.size() == 1);
        assertTrue(issues.get(0).contains("超过30分钟未通信"));
        assertTrue((Boolean) result.get("needsAttention"));
    }

    @Test
    void testCheckDeviceHealth_OfflineForTooLong() {
        // Given
        testDevice.setLastCommunicationTime(testTime.minusHours(2));
        testDevice.setDeviceStatus(1);

        // When
        Map<String, Object> result = consumeDeviceManager.checkDeviceHealth(testDevice, testTime);

        // Then
        assertNotNull(result);
        assertEquals("异常", result.get("healthStatus"));
        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) result.get("issues");
        assertTrue(issues.size() == 1);
        assertTrue(issues.get(0).contains("超过60分钟未通信"));
        assertTrue((Boolean) result.get("needsAttention"));
    }

    @Test
    void testCheckDeviceHealth_NeverCommunicated() {
        // Given
        testDevice.setLastCommunicationTime(null);
        testDevice.setDeviceStatus(1);

        // When
        Map<String, Object> result = consumeDeviceManager.checkDeviceHealth(testDevice, testTime);

        // Then
        assertNotNull(result);
        assertEquals("未知", result.get("healthStatus"));
        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) result.get("issues");
        assertTrue(issues.size() == 1);
        assertEquals("从未通信记录", issues.get(0));
        assertTrue((Boolean) result.get("needsAttention"));
    }

    @Test
    void testCheckDeviceHealth_FaultyDevice() {
        // Given
        testDevice.setDeviceStatus(3); // 故障

        // When
        Map<String, Object> result = consumeDeviceManager.checkDeviceHealth(testDevice, testTime);

        // Then
        assertNotNull(result);
        assertEquals("故障", result.get("healthStatus"));
        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) result.get("issues");
        assertTrue(issues.contains("设备故障"));
        assertTrue((Boolean) result.get("needsAttention"));
    }

    @Test
    void testCheckDeviceHealth_NeedsFirmwareUpdate() {
        // Given
        testDevice.setFirmwareVersion("v1.0.0");
        testDevice.setDeviceType(1);

        // When
        Map<String, Object> result = consumeDeviceManager.checkDeviceHealth(testDevice, testTime);

        // Then
        assertNotNull(result);
        assertEquals("需要更新", result.get("healthStatus"));
        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) result.get("issues");
        boolean hasFirmwareIssue = issues.stream().anyMatch(issue -> issue.contains("固件版本过时"));
        assertTrue(hasFirmwareIssue);
        assertTrue((Boolean) result.get("needsAttention"));
    }

    // ==================== 设备配置验证测试 ====================

    @Test
    void testValidateDeviceConfig_ValidConfig() throws JsonProcessingException {
        // Given
        Map<String, Object> configParams = new HashMap<>();
        configParams.put("serverUrl", "http://192.168.1.100:8080");
        configParams.put("timeout", "30000");
        configParams.put("port", "8080");
        configParams.put("retries", "3");

        testDevice.setBusinessAttributes("{\"offlineConfig\": {\"enabled\": true}}");
        testDevice.setSupportOffline(1);

        // Mock parseBusinessAttributes
        Map<String, Object> businessAttrs = new HashMap<>();
        businessAttrs.put("offlineConfig", Map.of("enabled", true));
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(businessAttrs);

        // When
        Map<String, Object> result = consumeDeviceManager.validateDeviceConfig(testDevice, configParams);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("valid"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get("errors");
        assertTrue(errors.isEmpty());
        assertNotNull(result.get("warnings"));
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void testValidateDeviceConfig_MissingRequiredParam() {
        // Given
        Map<String, Object> configParams = new HashMap<>();
        // 缺少必需的serverUrl
        testDevice.setBusinessAttributes("");

        // When
        Map<String, Object> result = consumeDeviceManager.validateDeviceConfig(testDevice, configParams);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("valid"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get("errors");
        assertTrue(errors.contains("缺少服务器URL配置"));
    }

    @Test
    void testValidateDeviceConfig_InvalidPort() {
        // Given
        Map<String, Object> configParams = new HashMap<>();
        configParams.put("serverUrl", "http://192.168.1.100:8080");
        configParams.put("port", "70000"); // 无效端口号
        testDevice.setBusinessAttributes("");

        // When
        Map<String, Object> result = consumeDeviceManager.validateDeviceConfig(testDevice, configParams);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("valid"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get("errors");
        assertTrue(errors.contains("端口号必须在1-65535范围内"));
    }

    @Test
    void testValidateDeviceConfig_InvalidBusinessAttributes() throws JsonProcessingException {
        // Given
        Map<String, Object> configParams = new HashMap<>();
        configParams.put("serverUrl", "http://192.168.1.100:8080");

        testDevice.setBusinessAttributes("{invalid json");
        // 使用 JsonMappingException（JsonProcessingException 的子类）而不是 RuntimeException
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
            .thenThrow(new com.fasterxml.jackson.databind.JsonMappingException("Invalid JSON"));

        // When
        Map<String, Object> result = consumeDeviceManager.validateDeviceConfig(testDevice, configParams);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("valid"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get("errors");
        assertTrue(errors.stream().anyMatch(error -> error.contains("业务属性JSON格式不正确")));
    }

    // ==================== 设备配置生成测试 ====================

    @Test
    void testGenerateDeviceConfig() throws JsonProcessingException {
        // Given
        testDevice.setIpAddress("192.168.1.100");
        testDevice.setSupportOffline(1);
        testDevice.setBusinessAttributes("{\"maxAmount\": 1000}");

        // Mock parseBusinessAttributes
        Map<String, Object> businessAttrs = new HashMap<>();
        businessAttrs.put("maxAmount", 1000);
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(businessAttrs);

        // When
        Map<String, Object> result = consumeDeviceManager.generateDeviceConfig(testDevice);

        // Then
        assertNotNull(result);
        assertEquals(testDevice.getDeviceId(), result.get("deviceId"));
        assertEquals(testDevice.getDeviceCode(), result.get("deviceCode"));
        assertEquals(testDevice.getDeviceName(), result.get("deviceName"));
        assertEquals(testDevice.getDeviceType(), result.get("deviceType"));
        assertEquals("192.168.1.100", result.get("ipAddress"));
        assertEquals(8080, result.get("port"));
        assertEquals(30000, result.get("timeout"));
        assertEquals(3, result.get("retries"));
        assertTrue((Boolean) result.get("encryptionEnabled"));
        assertEquals(60, result.get("heartbeatInterval"));
        assertEquals(7200, result.get("maxOfflineDuration"));
        assertTrue((Boolean) result.get("supportOffline"));
        assertTrue((Boolean) result.get("allowPartialOffline"));
        assertFalse((Boolean) result.get("autoSync"));
        assertEquals("INFO", result.get("logLevel"));
    }

    // ==================== 设备冲突检查测试 ====================

    @Test
    void testCheckDeviceConflicts_NoConflicts() {
        // Given
        when(consumeDeviceDao.countByDeviceCode(anyString(), any())).thenReturn(0);
        when(consumeDeviceDao.countByIpAddress(anyString(), any())).thenReturn(0);
        when(consumeDeviceDao.countByMacAddress(anyString(), any())).thenReturn(0);

        // When
        Map<String, Object> result = consumeDeviceManager.checkDeviceConflicts(
            "UNIQUE_001", "192.168.1.100", "00:11:22:33:44:55", null);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("hasConflict"));
        @SuppressWarnings("unchecked")
        List<String> conflictFields = (List<String>) result.get("conflictFields");
        assertTrue(conflictFields.isEmpty());
    }

    @Test
    void testCheckDeviceConflicts_DeviceCodeConflict() {
        // Given
        when(consumeDeviceDao.countByDeviceCode(anyString(), any())).thenReturn(1);
        when(consumeDeviceDao.countByIpAddress(anyString(), any())).thenReturn(0);
        when(consumeDeviceDao.countByMacAddress(anyString(), any())).thenReturn(0);

        // When
        Map<String, Object> result = consumeDeviceManager.checkDeviceConflicts(
            "DUPLICATE_001", "192.168.1.100", "00:11:22:33:44:55", null);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("hasConflict"));
        @SuppressWarnings("unchecked")
        List<String> conflictFields = (List<String>) result.get("conflictFields");
        assertTrue(conflictFields.size() == 1);
        assertTrue(conflictFields.get(0).contains("设备编码已存在"));
    }

    @Test
    void testCheckDeviceConflicts_MultipleConflicts() {
        // Given
        when(consumeDeviceDao.countByDeviceCode(anyString(), any())).thenReturn(1);
        when(consumeDeviceDao.countByIpAddress(anyString(), any())).thenReturn(1);
        when(consumeDeviceDao.countByMacAddress(anyString(), any())).thenReturn(1);

        // When
        Map<String, Object> result = consumeDeviceManager.checkDeviceConflicts(
            "ALL_DUPLICATE", "192.168.1.100", "00:11:22:33:44:55", null);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("hasConflict"));
        @SuppressWarnings("unchecked")
        List<String> conflictFields = (List<String>) result.get("conflictFields");
        assertTrue(conflictFields.size() == 3);
    }

    // ==================== 通信时间更新测试 ====================

    @Test
    void testUpdateLastCommunicationTime_Success() {
        // Given
        Long deviceId = 1L;
        LocalDateTime communicationTime = testTime;
        when(consumeDeviceDao.updateLastCommunicationTime(deviceId, communicationTime)).thenReturn(1);

        // When
        boolean result = consumeDeviceManager.updateLastCommunicationTime(deviceId, communicationTime);

        // Then
        assertTrue(result);
        verify(consumeDeviceDao).updateLastCommunicationTime(deviceId, communicationTime);
    }

    @Test
    void testUpdateLastCommunicationTime_Failure() {
        // Given
        Long deviceId = 1L;
        LocalDateTime communicationTime = testTime;
        when(consumeDeviceDao.updateLastCommunicationTime(deviceId, communicationTime)).thenReturn(0);

        // When
        boolean result = consumeDeviceManager.updateLastCommunicationTime(deviceId, communicationTime);

        // Then
        assertFalse(result);
        verify(consumeDeviceDao).updateLastCommunicationTime(deviceId, communicationTime);
    }

    @Test
    void testUpdateLastCommunicationTime_Exception() {
        // Given
        Long deviceId = 1L;
        LocalDateTime communicationTime = testTime;
        when(consumeDeviceDao.updateLastCommunicationTime(deviceId, communicationTime))
            .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = consumeDeviceManager.updateLastCommunicationTime(deviceId, communicationTime);

        // Then
        assertFalse(result);
        verify(consumeDeviceDao).updateLastCommunicationTime(deviceId, communicationTime);
    }

    // ==================== 批量状态更新测试 ====================

    @Test
    void testBatchUpdateDeviceStatus_Success() {
        // Given
        List<Long> deviceIds = List.of(1L, 2L, 3L);
        Integer targetStatus = 2; // 离线
        String reason = "维护升级";

        // Mock设备查询和状态验证
        ConsumeDeviceEntity device1 = createTestDevice();
        device1.setDeviceId(1L);
        device1.setDeviceStatus(1);
        ConsumeDeviceEntity device2 = createTestDevice();
        device2.setDeviceId(2L);
        device2.setDeviceStatus(1);
        ConsumeDeviceEntity device3 = createTestDevice();
        device3.setDeviceId(3L);
        device3.setDeviceStatus(1);

        when(consumeDeviceDao.selectById(1L)).thenReturn(device1);
        when(consumeDeviceDao.selectById(2L)).thenReturn(device2);
        when(consumeDeviceDao.selectById(3L)).thenReturn(device3);
        when(consumeDeviceDao.updateById(any(ConsumeDeviceEntity.class))).thenReturn(1);

        // When
        Map<String, Object> result = consumeDeviceManager.batchUpdateDeviceStatus(deviceIds, targetStatus, reason);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("批量更新成功", result.get("message"));
        assertEquals(3, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
        @SuppressWarnings("unchecked")
        List<String> failedDevices = (List<String>) result.get("failedDevices");
        assertTrue(failedDevices.isEmpty());

        // 验证调用次数
        verify(consumeDeviceDao, times(3)).selectById(anyLong());
        verify(consumeDeviceDao, times(3)).updateById(any(ConsumeDeviceEntity.class));
    }

    @Test
    void testBatchUpdateDeviceStatus_EmptyList() {
        // Given
        List<Long> deviceIds = new ArrayList<>();
        Integer targetStatus = 2;
        String reason = "测试";

        // When
        Map<String, Object> result = consumeDeviceManager.batchUpdateDeviceStatus(deviceIds, targetStatus, reason);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("设备ID列表为空", result.get("message"));
        assertEquals(0, result.get("failedCount"));
        verify(consumeDeviceDao, never()).selectById(anyLong());
    }

    @Test
    void testBatchUpdateDeviceStatus_DeviceNotFound() {
        // Given
        List<Long> deviceIds = List.of(1L, 999L);
        Integer targetStatus = 2;
        String reason = "测试";

        when(consumeDeviceDao.selectById(1L)).thenReturn(createTestDevice());
        when(consumeDeviceDao.selectById(999L)).thenReturn(null);

        // When
        Map<String, Object> result = consumeDeviceManager.batchUpdateDeviceStatus(deviceIds, targetStatus, reason);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success")); // 有设备失败，整体失败
        assertEquals("部分更新失败", result.get("message"));
        assertEquals(1, result.get("successCount"));
        assertEquals(1, result.get("failedCount"));
        @SuppressWarnings("unchecked")
        List<String> failedDevices = (List<String>) result.get("failedDevices");
        assertTrue(failedDevices.size() == 1);
        assertTrue(failedDevices.get(0).contains("设备不存在: 999"));
    }

    @Test
    void testBatchUpdateDeviceStatus_InvalidTransition() {
        // Given
        List<Long> deviceIds = List.of(1L);
        Integer targetStatus = 1; // 从在线到在线无效转换
        String reason = "测试";

        ConsumeDeviceEntity device = createTestDevice();
        device.setDeviceId(1L);
        device.setDeviceStatus(1); // 已经是在线状态

        when(consumeDeviceDao.selectById(1L)).thenReturn(device);

        // When
        Map<String, Object> result = consumeDeviceManager.batchUpdateDeviceStatus(deviceIds, targetStatus, reason);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success")); // 状态转换失败
        assertEquals("部分更新失败", result.get("message"));
        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("failedCount"));
        @SuppressWarnings("unchecked")
        List<String> failedDevices = (List<String>) result.get("failedDevices");
        assertTrue(failedDevices.size() == 1);
        assertTrue(failedDevices.get(0).contains("状态转换不合法"));
    }

    // ==================== 业务属性解析测试 ====================

    @Test
    void testParseBusinessAttributes_ValidJson() throws JsonProcessingException {
        // Given
        String businessAttributesJson = "{\"key1\": \"value1\", \"key2\": 123}";
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("key1", "value1");
        expectedResult.put("key2", 123);

        when(objectMapper.readValue(eq(businessAttributesJson), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(expectedResult);

        // When
        Map<String, Object> result = consumeDeviceManager.parseBusinessAttributes(businessAttributesJson);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(objectMapper).readValue(eq(businessAttributesJson), any(com.fasterxml.jackson.core.type.TypeReference.class));
    }

    @Test
    void testParseBusinessAttributes_EmptyJson() throws JsonProcessingException {
        // Given
        String businessAttributesJson = "";

        // When
        Map<String, Object> result = consumeDeviceManager.parseBusinessAttributes(businessAttributesJson);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseBusinessAttributes_NullJson() throws JsonProcessingException {
        // Given - no mock needed, parseBusinessAttributes handles null internally

        // When
        Map<String, Object> result = consumeDeviceManager.parseBusinessAttributes(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseBusinessAttributes_InvalidJson() throws JsonProcessingException {
        // Given
        String businessAttributesJson = "{invalid json}";
        when(objectMapper.readValue(eq(businessAttributesJson), any(com.fasterxml.jackson.core.type.TypeReference.class)))
            .thenThrow(new com.fasterxml.jackson.databind.JsonMappingException("Invalid JSON"));

        // When & Then
        assertThrows(JsonProcessingException.class, () -> {
            consumeDeviceManager.parseBusinessAttributes(businessAttributesJson);
        });
    }

    // ==================== 设备统计测试 ====================

    @Test
    void testGetDeviceStatistics() {
        // Given
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        Map<String, Object> overallStats = new HashMap<>();
        overallStats.put("totalCount", 10L);
        overallStats.put("totalAmount", new BigDecimal("1000.00"));

        List<Map<String, Object>> typeStats = new ArrayList<>();
        Map<String, Object> typeStat = new HashMap<>();
        typeStat.put("deviceType", 1);
        typeStat.put("count", 5L);
        typeStats.add(typeStat);

        List<Map<String, Object>> statusStats = new ArrayList<>();
        Map<String, Object> statusStat = new HashMap<>();
        statusStat.put("deviceStatus", 1);
        statusStat.put("count", 8L);
        statusStats.add(statusStat);

        when(consumeDeviceDao.getOverallStatistics(startDate, endDate)).thenReturn(overallStats);
        when(consumeDeviceDao.countByDeviceType()).thenReturn(typeStats);
        when(consumeDeviceDao.countByDeviceStatus()).thenReturn(statusStats);
        when(consumeDeviceDao.countByHealthStatus()).thenReturn(new HashMap<>());
        when(consumeDeviceDao.countByOfflineDuration()).thenReturn(new ArrayList<>());
        when(consumeDeviceDao.countByCommunicationPattern()).thenReturn(new ArrayList<>());

        // When
        Map<String, Object> result = consumeDeviceManager.getDeviceStatistics(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.get("totalCount"));
        assertEquals(new BigDecimal("1000.00"), result.get("totalAmount"));
        assertEquals(typeStats, result.get("typeStatistics"));
        assertEquals(statusStats, result.get("statusStatistics"));
        assertNotNull(result.get("healthStatistics"));
        assertNotNull(result.get("offlineDurationStatistics"));
        assertNotNull(result.get("communicationStatistics"));
        assertNotNull(result.get("timestamp"));

        verify(consumeDeviceDao).getOverallStatistics(startDate, endDate);
        verify(consumeDeviceDao).countByDeviceType();
        verify(consumeDeviceDao).countByDeviceStatus();
    }

    // ==================== 设备分组统计测试 ====================

    @Test
    void testGetDeviceGroupStatistics_ByType() {
        // Given
        List<Map<String, Object>> expectedStats = new ArrayList<>();
        Map<String, Object> stat = new HashMap<>();
        stat.put("deviceType", 1);
        stat.put("count", 5L);
        expectedStats.add(stat);

        when(consumeDeviceDao.countByDeviceType()).thenReturn(expectedStats);

        // When
        List<Map<String, Object>> result = consumeDeviceManager.getDeviceGroupStatistics("byType");

        // Then
        assertNotNull(result);
        assertEquals(expectedStats, result);
        verify(consumeDeviceDao).countByDeviceType();
    }

    @Test
    void testGetDeviceGroupStatistics_ByStatus() {
        // Given
        List<Map<String, Object>> expectedStats = new ArrayList<>();
        Map<String, Object> stat = new HashMap<>();
        stat.put("deviceStatus", 1);
        stat.put("count", 8L);
        expectedStats.add(stat);

        when(consumeDeviceDao.countByDeviceStatus()).thenReturn(expectedStats);

        // When
        List<Map<String, Object>> result = consumeDeviceManager.getDeviceGroupStatistics("byStatus");

        // Then
        assertNotNull(result);
        assertEquals(expectedStats, result);
        verify(consumeDeviceDao).countByDeviceStatus();
    }

    @Test
    void testGetDeviceGroupStatistics_InvalidGroupType() {
        // Given
        String invalidGroupType = "invalid";

        // When
        List<Map<String, Object>> result = consumeDeviceManager.getDeviceGroupStatistics(invalidGroupType);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(consumeDeviceDao, never()).countByDeviceType();
        verify(consumeDeviceDao, never()).countByDeviceStatus();
    }

    @Test
    void testGetDeviceGroupStatistics_Exception() {
        // Given
        when(consumeDeviceDao.countByDeviceType()).thenThrow(new RuntimeException("Database error"));

        // When
        List<Map<String, Object>> result = consumeDeviceManager.getDeviceGroupStatistics("byType");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(consumeDeviceDao).countByDeviceType();
    }

    // ==================== 设备报告生成测试 ====================

    @Test
    void testGenerateDeviceReport_SummaryReport() {
        // Given
        List<Long> deviceIds = List.of(1L);
        ConsumeDeviceEntity device = createTestDevice();

        when(consumeDeviceDao.selectById(1L)).thenReturn(device);

        // When
        Map<String, Object> result = consumeDeviceManager.generateDeviceReport(deviceIds, "summary");

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(1, result.get("deviceCount"));
        assertNotNull(result.get("generatedAt"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> devices = (List<Map<String, Object>>) result.get("devices");
        assertEquals(1, devices.size());

        Map<String, Object> deviceReport = devices.get(0);
        assertEquals(device.getDeviceId(), deviceReport.get("deviceId"));
        assertEquals(device.getDeviceCode(), deviceReport.get("deviceCode"));
        assertEquals(device.getDeviceName(), deviceReport.get("deviceName"));
        assertEquals("summary", deviceReport.get("reportType"));
        assertNotNull(deviceReport.get("reportTime"));
    }

    @Test
    void testGenerateDeviceReport_DetailedReport() {
        // Given
        List<Long> deviceIds = List.of(1L);
        ConsumeDeviceEntity device = createTestDevice();

        when(consumeDeviceDao.selectById(1L)).thenReturn(device);

        // When
        Map<String, Object> result = consumeDeviceManager.generateDeviceReport(deviceIds, "detailed");

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(1, result.get("deviceCount"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> devices = (List<Map<String, Object>>) result.get("devices");
        assertEquals(1, devices.size());

        Map<String, Object> deviceReport = devices.get(0);
        assertEquals("detailed", deviceReport.get("reportType"));
        assertEquals(device.getIpAddress(), deviceReport.get("ipAddress"));
        assertEquals(device.getMacAddress(), deviceReport.get("macAddress"));
        assertEquals(device.getDeviceModel(), deviceReport.get("deviceModel"));
        assertEquals(device.getDeviceManufacturer(), deviceReport.get("deviceManufacturer"));
        assertEquals(device.getFirmwareVersion(), deviceReport.get("firmwareVersion"));
        assertEquals(device.supportsOffline(), deviceReport.get("supportsOffline"));
        assertEquals(device.getBusinessAttributes(), deviceReport.get("businessAttributes"));
        assertEquals(device.getDeviceDescription(), deviceReport.get("deviceDescription"));
        assertEquals(device.getCreateTime(), deviceReport.get("createTime"));
        assertEquals(device.getUpdateTime(), deviceReport.get("updateTime"));
    }

    @Test
    void testGenerateDeviceReport_EmptyList() {
        // Given
        List<Long> deviceIds = new ArrayList<>();

        // When
        Map<String, Object> result = consumeDeviceManager.generateDeviceReport(deviceIds, "summary");

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("设备ID列表为空", result.get("message"));
        verify(consumeDeviceDao, never()).selectById(anyLong());
    }

    @Test
    void testGenerateDeviceReport_DeviceNotFound() {
        // Given
        List<Long> deviceIds = List.of(1L, 999L);
        when(consumeDeviceDao.selectById(1L)).thenReturn(createTestDevice());
        when(consumeDeviceDao.selectById(999L)).thenReturn(null);

        // When
        Map<String, Object> result = consumeDeviceManager.generateDeviceReport(deviceIds, "summary");

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(1, result.get("deviceCount")); // 只有找到的设备
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> devices = (List<Map<String, Object>>) result.get("devices");
        assertEquals(1, devices.size()); // 只有找到的设备
    }
}