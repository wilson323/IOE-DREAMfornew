package net.lab1024.sa.devicecomm.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.devicecomm.controller.BiometricIntegrationController.BiometricFeatureRequest;
import net.lab1024.sa.devicecomm.controller.BiometricIntegrationController.BiometricRegisterRequest;
import net.lab1024.sa.devicecomm.controller.BiometricIntegrationController.QuickAccessRequest;
import net.lab1024.sa.devicecomm.controller.BiometricIntegrationController.QuickAttendanceRequest;
import net.lab1024.sa.devicecomm.controller.BiometricIntegrationController.QuickConsumeRequest;
import net.lab1024.sa.devicecomm.controller.BiometricIntegrationController.IntegrationFunction;
import net.lab1024.sa.devicecomm.integration.BiometricIntegrationService;
import net.lab1024.sa.devicecomm.protocol.enums.VerifyTypeEnum;

/**
 * BiometricIntegrationController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：BiometricIntegrationController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BiometricIntegrationController单元测试")
class BiometricIntegrationControllerTest {
    @Mock
    private BiometricIntegrationService biometricIntegrationService;
    @InjectMocks
    private BiometricIntegrationController biometricIntegrationController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    // ==================== verifyAccessBiometric ====================

    @Test
    @DisplayName("verifyAccessBiometric - 成功场景")
    void test_verifyAccessBiometric_Success() {
        // Given
        Long userId = 1L;
        Long deviceId = 100L;
        Long areaId = 200L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        BiometricIntegrationService.BiometricAccessResult result = new BiometricIntegrationService.BiometricAccessResult();
        result.setUserId(userId);
        result.setAccessGranted(true);

        when(biometricIntegrationService.verifyAccessBiometric(
                eq(userId), eq(deviceId), eq(areaId), eq(verifyType), any(byte[].class)))
                .thenReturn(ResponseDTO.ok(result));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricAccessResult> response =
                biometricIntegrationController.verifyAccessBiometric(userId, deviceId, areaId, verifyType, request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().getAccessGranted());
        verify(biometricIntegrationService).verifyAccessBiometric(
                eq(userId), eq(deviceId), eq(areaId), eq(verifyType), any(byte[].class));
    }

    @Test
    @DisplayName("verifyAccessBiometric - 参数错误")
    void test_verifyAccessBiometric_ParamError() {
        // Given
        Long userId = 1L;
        Long deviceId = 100L;
        Long areaId = 200L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        when(biometricIntegrationService.verifyAccessBiometric(
                eq(userId), eq(deviceId), eq(areaId), eq(verifyType), any(byte[].class)))
                .thenReturn(ResponseDTO.error("ACCESS_BIOMETRIC_PARAM_ERROR", "参数错误"));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricAccessResult> response =
                biometricIntegrationController.verifyAccessBiometric(userId, deviceId, areaId, verifyType, request);

        // Then
        assertNotEquals(200, response.getCode());
        assertEquals(ResponseDTO.error("ACCESS_BIOMETRIC_PARAM_ERROR", "x").getCode(), response.getCode());
    }

    @Test
    @DisplayName("verifyAccessBiometric - 业务错误")
    void test_verifyAccessBiometric_BusinessError() {
        // Given
        Long userId = 1L;
        Long deviceId = 100L;
        Long areaId = 200L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        when(biometricIntegrationService.verifyAccessBiometric(
                eq(userId), eq(deviceId), eq(areaId), eq(verifyType), any(byte[].class)))
                .thenReturn(ResponseDTO.error("ACCESS_PERMISSION_DENIED", "门禁权限不足"));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricAccessResult> response =
                biometricIntegrationController.verifyAccessBiometric(userId, deviceId, areaId, verifyType, request);

        // Then
        assertNotEquals(200, response.getCode());
        assertEquals(ResponseDTO.error("ACCESS_PERMISSION_DENIED", "x").getCode(), response.getCode());
    }

    @Test
    @DisplayName("verifyAccessBiometric - 系统错误")
    void test_verifyAccessBiometric_SystemError() {
        // Given
        Long userId = 1L;
        Long deviceId = 100L;
        Long areaId = 200L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        when(biometricIntegrationService.verifyAccessBiometric(
                eq(userId), eq(deviceId), eq(areaId), eq(verifyType), any(byte[].class)))
                .thenReturn(ResponseDTO.error("ACCESS_BIOMETRIC_SYSTEM_ERROR", "系统异常"));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricAccessResult> response =
                biometricIntegrationController.verifyAccessBiometric(userId, deviceId, areaId, verifyType, request);

        // Then
        assertNotEquals(200, response.getCode());
        assertEquals(ResponseDTO.error("ACCESS_BIOMETRIC_SYSTEM_ERROR", "x").getCode(), response.getCode());
    }

    // ==================== punchAttendanceBiometric ====================

    @Test
    @DisplayName("punchAttendanceBiometric - 成功场景")
    void test_punchAttendanceBiometric_Success() {
        // Given
        Long userId = 1L;
        Long deviceId = 100L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        String punchType = "上班";
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        BiometricIntegrationService.BiometricAttendanceResult result =
                new BiometricIntegrationService.BiometricAttendanceResult();
        result.setUserId(userId);
        result.setSuccess(true);

        when(biometricIntegrationService.punchAttendanceBiometric(
                eq(userId), eq(deviceId), eq(verifyType), any(byte[].class), eq(punchType)))
                .thenReturn(ResponseDTO.ok(result));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricAttendanceResult> response =
                biometricIntegrationController.punchAttendanceBiometric(userId, deviceId, verifyType, punchType, request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().getSuccess());
        verify(biometricIntegrationService).punchAttendanceBiometric(
                eq(userId), eq(deviceId), eq(verifyType), any(byte[].class), eq(punchType));
    }

    @Test
    @DisplayName("punchAttendanceBiometric - 参数错误")
    void test_punchAttendanceBiometric_ParamError() {
        // Given
        Long userId = 1L;
        Long deviceId = 100L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        String punchType = "上班";
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        when(biometricIntegrationService.punchAttendanceBiometric(
                eq(userId), eq(deviceId), eq(verifyType), any(byte[].class), eq(punchType)))
                .thenReturn(ResponseDTO.error("ATTENDANCE_BIOMETRIC_PARAM_ERROR", "参数错误"));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricAttendanceResult> response =
                biometricIntegrationController.punchAttendanceBiometric(userId, deviceId, verifyType, punchType, request);

        // Then
        assertNotEquals(200, response.getCode());
        assertEquals(ResponseDTO.error("ATTENDANCE_BIOMETRIC_PARAM_ERROR", "x").getCode(), response.getCode());
    }

    // ==================== verifyVisitorBiometric ====================

    @Test
    @DisplayName("verifyVisitorBiometric - 成功场景")
    void test_verifyVisitorBiometric_Success() {
        // Given
        Long visitorId = 1L;
        Long deviceId = 100L;
        Long areaId = 200L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        BiometricIntegrationService.BiometricVisitorResult result =
                new BiometricIntegrationService.BiometricVisitorResult();
        result.setVisitorId(visitorId);
        result.setAccessGranted(true);

        when(biometricIntegrationService.verifyVisitorBiometric(
                eq(visitorId), eq(deviceId), eq(areaId), eq(verifyType), any(byte[].class)))
                .thenReturn(ResponseDTO.ok(result));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricVisitorResult> response =
                biometricIntegrationController.verifyVisitorBiometric(visitorId, deviceId, areaId, verifyType, request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().getAccessGranted());
        verify(biometricIntegrationService).verifyVisitorBiometric(
                eq(visitorId), eq(deviceId), eq(areaId), eq(verifyType), any(byte[].class));
    }

    @Test
    @DisplayName("verifyVisitorBiometric - 业务错误")
    void test_verifyVisitorBiometric_BusinessError() {
        // Given
        Long visitorId = 1L;
        Long deviceId = 100L;
        Long areaId = 200L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        when(biometricIntegrationService.verifyVisitorBiometric(
                eq(visitorId), eq(deviceId), eq(areaId), eq(verifyType), any(byte[].class)))
                .thenReturn(ResponseDTO.error("VISITOR_PERMISSION_DENIED", "访客权限不足"));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricVisitorResult> response =
                biometricIntegrationController.verifyVisitorBiometric(visitorId, deviceId, areaId, verifyType, request);

        // Then
        assertNotEquals(200, response.getCode());
        assertEquals(ResponseDTO.error("VISITOR_PERMISSION_DENIED", "x").getCode(), response.getCode());
    }

    // ==================== verifyConsumeBiometric ====================

    @Test
    @DisplayName("verifyConsumeBiometric - 成功场景")
    void test_verifyConsumeBiometric_Success() {
        // Given
        Long userId = 1L;
        Long deviceId = 100L;
        Double amount = 50.0;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        BiometricIntegrationService.BiometricConsumeResult result =
                new BiometricIntegrationService.BiometricConsumeResult();
        result.setUserId(userId);
        result.setPaymentSuccess(true);

        when(biometricIntegrationService.verifyConsumeBiometric(
                eq(userId), eq(deviceId), eq(amount), eq(verifyType), any(byte[].class)))
                .thenReturn(ResponseDTO.ok(result));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricConsumeResult> response =
                biometricIntegrationController.verifyConsumeBiometric(userId, deviceId, amount, verifyType, request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().getPaymentSuccess());
        verify(biometricIntegrationService).verifyConsumeBiometric(
                eq(userId), eq(deviceId), eq(amount), eq(verifyType), any(byte[].class));
    }

    @Test
    @DisplayName("verifyConsumeBiometric - 业务错误")
    void test_verifyConsumeBiometric_BusinessError() {
        // Given
        Long userId = 1L;
        Long deviceId = 100L;
        Double amount = 50.0;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        BiometricFeatureRequest request = new BiometricFeatureRequest();
        request.setFeatureData(new byte[]{1, 2, 3});

        when(biometricIntegrationService.verifyConsumeBiometric(
                eq(userId), eq(deviceId), eq(amount), eq(verifyType), any(byte[].class)))
                .thenReturn(ResponseDTO.error("INSUFFICIENT_BALANCE", "余额不足"));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricConsumeResult> response =
                biometricIntegrationController.verifyConsumeBiometric(userId, deviceId, amount, verifyType, request);

        // Then
        assertNotEquals(200, response.getCode());
        assertEquals(ResponseDTO.error("INSUFFICIENT_BALANCE", "x").getCode(), response.getCode());
    }

    // ==================== batchRegisterUserBiometric ====================

    @Test
    @DisplayName("batchRegisterUserBiometric - 成功场景")
    void test_batchRegisterUserBiometric_Success() {
        // Given
        Long userId = 1L;
        List<BiometricRegisterRequest> requestList = new ArrayList<>();
        BiometricRegisterRequest req1 = new BiometricRegisterRequest();
        req1.setVerifyType(VerifyTypeEnum.FACE);
        req1.setFeatureData(new byte[]{1, 2, 3});
        req1.setDeviceId(100L);
        requestList.add(req1);

        BiometricIntegrationService.BiometricBatchRegisterResult result =
                new BiometricIntegrationService.BiometricBatchRegisterResult();
        result.setUserId(userId);
        result.setTotalCount(1);
        result.setSuccessCount(1);
        result.setFailCount(0);

        when(biometricIntegrationService.batchRegisterUserBiometric(
                eq(userId), anyList()))
                .thenReturn(ResponseDTO.ok(result));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricBatchRegisterResult> response =
                biometricIntegrationController.batchRegisterUserBiometric(userId, requestList);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getSuccessCount());
        verify(biometricIntegrationService).batchRegisterUserBiometric(eq(userId), anyList());
    }

    @Test
    @DisplayName("batchRegisterUserBiometric - 部分失败")
    void test_batchRegisterUserBiometric_PartialFailure() {
        // Given
        Long userId = 1L;
        List<BiometricRegisterRequest> requestList = new ArrayList<>();
        BiometricRegisterRequest req1 = new BiometricRegisterRequest();
        req1.setVerifyType(VerifyTypeEnum.FACE);
        req1.setFeatureData(new byte[]{1, 2, 3});
        req1.setDeviceId(100L);
        requestList.add(req1);

        BiometricIntegrationService.BiometricBatchRegisterResult result =
                new BiometricIntegrationService.BiometricBatchRegisterResult();
        result.setUserId(userId);
        result.setTotalCount(1);
        result.setSuccessCount(0);
        result.setFailCount(1);

        when(biometricIntegrationService.batchRegisterUserBiometric(
                eq(userId), anyList()))
                .thenReturn(ResponseDTO.ok(result));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricBatchRegisterResult> response =
                biometricIntegrationController.batchRegisterUserBiometric(userId, requestList);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getFailCount());
    }

    // ==================== batchRegisterUserBiometricAsync ====================

    @Test
    @DisplayName("batchRegisterUserBiometricAsync - 成功场景")
    void test_batchRegisterUserBiometricAsync_Success() throws Exception {
        // Given
        Long userId = 1L;
        List<BiometricRegisterRequest> requestList = new ArrayList<>();
        BiometricRegisterRequest req1 = new BiometricRegisterRequest();
        req1.setVerifyType(VerifyTypeEnum.FACE);
        req1.setFeatureData(new byte[]{1, 2, 3});
        req1.setDeviceId(100L);
        requestList.add(req1);

        BiometricIntegrationService.BiometricBatchRegisterResult result =
                new BiometricIntegrationService.BiometricBatchRegisterResult();
        result.setUserId(userId);
        result.setTotalCount(1);
        result.setSuccessCount(1);

        CompletableFuture<ResponseDTO<BiometricIntegrationService.BiometricBatchRegisterResult>> future =
                CompletableFuture.completedFuture(ResponseDTO.ok(result));

        when(biometricIntegrationService.batchRegisterUserBiometricAsync(
                eq(userId), anyList()))
                .thenReturn(future);

        // When
        CompletableFuture<ResponseDTO<BiometricIntegrationService.BiometricBatchRegisterResult>> responseFuture =
                biometricIntegrationController.batchRegisterUserBiometricAsync(userId, requestList);

        // Then
        ResponseDTO<BiometricIntegrationService.BiometricBatchRegisterResult> response = responseFuture.get();
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getSuccessCount());
        verify(biometricIntegrationService).batchRegisterUserBiometricAsync(eq(userId), anyList());
    }

    // ==================== quickVerifyAccess ====================

    @Test
    @DisplayName("quickVerifyAccess - 成功场景")
    void test_quickVerifyAccess_Success() {
        // Given
        QuickAccessRequest request = new QuickAccessRequest();
        request.setUserId(1L);
        request.setDeviceId(100L);
        request.setAreaId(200L);
        request.setVerifyType(VerifyTypeEnum.FACE);
        request.setFeatureData(new byte[]{1, 2, 3});

        BiometricIntegrationService.BiometricAccessResult result =
                new BiometricIntegrationService.BiometricAccessResult();
        result.setAccessGranted(true);

        when(biometricIntegrationService.verifyAccessBiometric(
                eq(1L), eq(100L), eq(200L), eq(VerifyTypeEnum.FACE), any(byte[].class)))
                .thenReturn(ResponseDTO.ok(result));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricAccessResult> response =
                biometricIntegrationController.quickVerifyAccess(request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().getAccessGranted());
        verify(biometricIntegrationService).verifyAccessBiometric(
                eq(1L), eq(100L), eq(200L), eq(VerifyTypeEnum.FACE), any(byte[].class));
    }

    // ==================== quickPunchAttendance ====================

    @Test
    @DisplayName("quickPunchAttendance - 成功场景")
    void test_quickPunchAttendance_Success() {
        // Given
        QuickAttendanceRequest request = new QuickAttendanceRequest();
        request.setUserId(1L);
        request.setDeviceId(100L);
        request.setVerifyType(VerifyTypeEnum.FACE);
        request.setPunchType("上班");
        request.setFeatureData(new byte[]{1, 2, 3});

        BiometricIntegrationService.BiometricAttendanceResult result =
                new BiometricIntegrationService.BiometricAttendanceResult();
        result.setSuccess(true);

        when(biometricIntegrationService.punchAttendanceBiometric(
                eq(1L), eq(100L), eq(VerifyTypeEnum.FACE), any(byte[].class), eq("上班")))
                .thenReturn(ResponseDTO.ok(result));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricAttendanceResult> response =
                biometricIntegrationController.quickPunchAttendance(request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().getSuccess());
        verify(biometricIntegrationService).punchAttendanceBiometric(
                eq(1L), eq(100L), eq(VerifyTypeEnum.FACE), any(byte[].class), eq("上班"));
    }

    // ==================== quickPayConsume ====================

    @Test
    @DisplayName("quickPayConsume - 成功场景")
    void test_quickPayConsume_Success() {
        // Given
        QuickConsumeRequest request = new QuickConsumeRequest();
        request.setUserId(1L);
        request.setDeviceId(100L);
        request.setAmount(50.0);
        request.setVerifyType(VerifyTypeEnum.FACE);
        request.setFeatureData(new byte[]{1, 2, 3});

        BiometricIntegrationService.BiometricConsumeResult result =
                new BiometricIntegrationService.BiometricConsumeResult();
        result.setPaymentSuccess(true);

        when(biometricIntegrationService.verifyConsumeBiometric(
                eq(1L), eq(100L), eq(50.0), eq(VerifyTypeEnum.FACE), any(byte[].class)))
                .thenReturn(ResponseDTO.ok(result));

        // When
        ResponseDTO<BiometricIntegrationService.BiometricConsumeResult> response =
                biometricIntegrationController.quickPayConsume(request);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().getPaymentSuccess());
        verify(biometricIntegrationService).verifyConsumeBiometric(
                eq(1L), eq(100L), eq(50.0), eq(VerifyTypeEnum.FACE), any(byte[].class));
    }

    // ==================== health ====================

    @Test
    @DisplayName("health - 成功场景")
    void test_health_Success() {
        // When
        ResponseDTO<String> response = biometricIntegrationController.health();

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("生物识别集成服务运行正常", response.getData());
    }

    // ==================== getIntegrationFunctions ====================

    @Test
    @DisplayName("getIntegrationFunctions - 成功场景")
    void test_getIntegrationFunctions_Success() {
        // When
        ResponseDTO<List<IntegrationFunction>> response = biometricIntegrationController.getIntegrationFunctions();

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
        assertEquals(5, response.getData().size());
    }
}
