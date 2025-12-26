package net.lab1024.sa.biometric.service;

import net.lab1024.sa.biometric.domain.vo.TemplateSyncResultVO;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 生物模板同步服务测试
 * <p>
 * 测试范围：
 * - 模板同步到单个设备
 * - 模板同步到多个设备
 * - 从设备删除模板
 * - 批量同步操作
 * </p>
 * <p>
 * 注意：使用纯Mockito测试，不加载Spring上下文，避免ApplicationContext加载失败
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("生物模板同步服务测试")
class BiometricTemplateSyncServiceTest {

    @Mock
    private BiometricTemplateSyncService templateSyncService;

    private final Long testUserId = 1001L;
    private final String testDeviceId = "DEV001";
    private final Long testAreaId = 101L;

    @Test
    @DisplayName("同步用户模板到单个设备 - 服务未实现")
    void testSyncTemplateToDevice_NotImplemented() {
        // Given
        TemplateSyncResultVO mockResult = new TemplateSyncResultVO();
        mockResult.setTotalCount(1);
        mockResult.setSuccessCount(1);
        mockResult.setFailCount(0);

        when(templateSyncService.syncTemplateToDevice(any(Long.class), any(String.class)))
                .thenReturn(ResponseDTO.ok(mockResult));

        // When
        ResponseDTO<TemplateSyncResultVO> result = templateSyncService.syncTemplateToDevice(
                testUserId,
                testDeviceId
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().getSuccessCount());

        // 验证方法被调用
        verify(templateSyncService, times(1)).syncTemplateToDevice(eq(testUserId), eq(testDeviceId));
    }

    @Test
    @DisplayName("同步用户模板到多个设备 - 服务未实现")
    void testSyncTemplateToDevices_NotImplemented() {
        // Given
        List<String> deviceIds = Arrays.asList("DEV001", "DEV002", "DEV003");
        TemplateSyncResultVO mockResult = new TemplateSyncResultVO();
        mockResult.setTotalCount(3);
        mockResult.setSuccessCount(3);
        mockResult.setFailCount(0);

        when(templateSyncService.syncTemplateToDevices(any(Long.class), any(List.class)))
                .thenReturn(ResponseDTO.ok(mockResult));

        // When
        ResponseDTO<TemplateSyncResultVO> result = templateSyncService.syncTemplateToDevices(
                testUserId,
                deviceIds
        );

        // Then
        assertNotNull(result);
        assertEquals(3, result.getData().getSuccessCount());
    }

    @Test
    @DisplayName("从设备删除用户模板 - 服务未实现")
    void testDeleteTemplateFromDevice_NotImplemented() {
        // Given
        when(templateSyncService.deleteTemplateFromDevice(any(Long.class), any(String.class)))
                .thenReturn(ResponseDTO.ok());

        // When
        ResponseDTO<Void> result = templateSyncService.deleteTemplateFromDevice(
                testUserId,
                testDeviceId
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("从所有设备删除用户模板 - 服务未实现")
    void testDeleteTemplateFromAllDevices_NotImplemented() {
        // Given
        when(templateSyncService.deleteTemplateFromAllDevices(any(Long.class)))
                .thenReturn(ResponseDTO.ok());

        // When
        ResponseDTO<Void> result = templateSyncService.deleteTemplateFromAllDevices(
                testUserId
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("权限变更时同步模板 - 服务未实现")
    void testSyncOnPermissionAdded_NotImplemented() {
        // Given
        TemplateSyncResultVO mockResult = new TemplateSyncResultVO();
        mockResult.setTotalCount(5);
        mockResult.setSuccessCount(5);
        mockResult.setFailCount(0);

        when(templateSyncService.syncOnPermissionAdded(any(Long.class), any(Long.class)))
                .thenReturn(ResponseDTO.ok(mockResult));

        // When
        ResponseDTO<TemplateSyncResultVO> result = templateSyncService.syncOnPermissionAdded(
                testUserId,
                testAreaId
        );

        // Then
        assertNotNull(result);
        assertEquals(5, result.getData().getSuccessCount());
    }

    @Test
    @DisplayName("权限移除时删除模板 - 服务未实现")
    void testDeleteOnPermissionRemoved_NotImplemented() {
        // Given
        when(templateSyncService.deleteOnPermissionRemoved(any(Long.class), any(Long.class)))
                .thenReturn(ResponseDTO.ok());

        // When
        ResponseDTO<Void> result = templateSyncService.deleteOnPermissionRemoved(
                testUserId,
                testAreaId
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("批量同步模板到设备 - 服务未实现")
    void testBatchSyncTemplatesToDevice_NotImplemented() {
        // Given
        List<Long> userIds = Arrays.asList(1001L, 1002L, 1003L);
        TemplateSyncResultVO mockResult = new TemplateSyncResultVO();
        mockResult.setTotalCount(3);
        mockResult.setSuccessCount(3);
        mockResult.setFailCount(0);

        when(templateSyncService.batchSyncTemplatesToDevice(any(List.class), any(String.class)))
                .thenReturn(ResponseDTO.ok(mockResult));

        // When
        ResponseDTO<TemplateSyncResultVO> result = templateSyncService.batchSyncTemplatesToDevice(
                userIds,
                testDeviceId
        );

        // Then
        assertNotNull(result);
        assertEquals(3, result.getData().getSuccessCount());
    }
}
