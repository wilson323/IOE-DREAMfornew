package net.lab1024.sa.devicecomm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.devicecomm.biometric.BiometricDataManager;
import net.lab1024.sa.devicecomm.protocol.enums.VerifyTypeEnum;
import net.lab1024.sa.devicecomm.service.BiometricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 生物识别控制器测试
 * 生物识别API接口单元测试
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("生物识别控制器测试")
@SuppressWarnings("null")
class BiometricControllerTest {

    @Mock
    private BiometricService biometricService;

    @InjectMocks
    private BiometricController biometricController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(biometricController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("测试注册生物识别 - 成功")
    void testRegisterBiometric_Success() throws Exception {
        // Given
        Long userId = 1L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        Long deviceId = 1001L;
        byte[] featureData = "test_feature_data".getBytes(StandardCharsets.UTF_8);

        BiometricController.BiometricRegisterRequest request = new BiometricController.BiometricRegisterRequest();
        request.setFeatureData(featureData);
        request.setTemplateData(new byte[]{});

        when(biometricService.registerBiometric(eq(userId), eq(verifyType), eq(featureData), eq(new byte[]{}), eq(deviceId)))
                .thenReturn(ResponseDTO.ok());

        // When & Then
        mockMvc.perform(post("/api/v1/biometric/register")
                .param("userId", "1")
                .param("verifyType", "FACE")
                .param("deviceId", "1001")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试验证生物识别 - 成功")
    void testVerifyBiometric_Success() throws Exception {
        // Given
        Long userId = 1L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FINGERPRINT;
        byte[] featureData = "test_fingerprint_data".getBytes(StandardCharsets.UTF_8);

        BiometricDataManager.BiometricMatchResult matchResult = new BiometricDataManager.BiometricMatchResult();
        matchResult.setUserId(userId);
        matchResult.setVerifyType(verifyType);
        matchResult.setSimilarity(0.95);
        matchResult.setThreshold(0.90);
        matchResult.setMatched(true);
        matchResult.setMessage("匹配成功");

        BiometricController.BiometricVerifyRequest request = new BiometricController.BiometricVerifyRequest();
        request.setFeatureData(featureData);

        when(biometricService.verifyBiometric(eq(userId), eq(verifyType), eq(featureData)))
                .thenReturn(ResponseDTO.ok(matchResult));

        // When & Then
        mockMvc.perform(post("/api/v1/biometric/verify")
                .param("userId", "1")
                .param("verifyType", "FINGERPRINT")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.verifyType").value("FINGERPRINT"))
                .andExpect(jsonPath("$.data.similarity").value(0.95))
                .andExpect(jsonPath("$.data.matched").value(true));
    }

    @Test
    @DisplayName("测试查找最佳匹配 - 成功")
    void testFindBestMatch_Success() throws Exception {
        // Given
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        byte[] featureData = "test_face_data".getBytes(StandardCharsets.UTF_8);
        List<Long> candidateIds = Arrays.asList(1L, 2L, 3L);

        BiometricDataManager.BiometricMatchResult matchResult = new BiometricDataManager.BiometricMatchResult();
        matchResult.setUserId(2L);
        matchResult.setVerifyType(verifyType);
        matchResult.setSimilarity(0.92);
        matchResult.setThreshold(0.85);
        matchResult.setMatched(true);
        matchResult.setMessage("匹配成功");

        BiometricController.BiometricMatchRequest request = new BiometricController.BiometricMatchRequest();
        request.setFeatureData(featureData);
        request.setCandidateUserIds(candidateIds);

        when(biometricService.findBestMatch(eq(verifyType), eq(featureData), eq(candidateIds)))
                .thenReturn(ResponseDTO.ok(matchResult));

        // When & Then
        mockMvc.perform(post("/api/v1/biometric/match")
                .param("verifyType", "FACE")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(2))
                .andExpect(jsonPath("$.data.matched").value(true))
                .andExpect(jsonPath("$.data.similarity").value(0.92));
    }

    @Test
    @DisplayName("测试删除生物数据 - 成功")
    void testDeleteBiometricData_Success() throws Exception {
        // Given
        Long userId = 1L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.IRIS;

        when(biometricService.deleteBiometricData(eq(userId), eq(verifyType)))
                .thenReturn(ResponseDTO.ok());

        // When & Then
        mockMvc.perform(delete("/api/v1/biometric/data")
                .param("userId", "1")
                .param("verifyType", "IRIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取验证方式 - 成功")
    void testGetSupportedVerifyTypes_Success() throws Exception {
        // Given
        Long userId = 1L;
        List<VerifyTypeEnum> verifyTypes = Arrays.asList(VerifyTypeEnum.FACE, VerifyTypeEnum.FINGERPRINT);

        when(biometricService.getSupportedVerifyTypes(eq(userId)))
                .thenReturn(ResponseDTO.ok(verifyTypes));

        // When & Then
        mockMvc.perform(get("/api/v1/biometric/verify-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("FACE"))
                .andExpect(jsonPath("$.data[1]").value("FINGERPRINT"));
    }

    @Test
    @DisplayName("测试获取生物数据 - 成功")
    void testGetUserBiometricData_Success() throws Exception {
        // Given
        Long userId = 1L;
        List<BiometricDataManager.BiometricData> dataList = Arrays.asList(
                createMockBiometricData(userId, VerifyTypeEnum.FACE),
                createMockBiometricData(userId, VerifyTypeEnum.FINGERPRINT)
        );

        when(biometricService.getUserBiometricData(eq(userId)))
                .thenReturn(ResponseDTO.ok(dataList));

        // When & Then
        mockMvc.perform(get("/api/v1/biometric/data/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("测试批量注册 - 成功")
    void testBatchRegisterBiometric_Success() throws Exception {
        // Given
        Long userId = 1L;
        List<BiometricService.BiometricRegisterRequest> requestList = Arrays.asList(
                createMockRegisterRequest(VerifyTypeEnum.FACE),
                createMockRegisterRequest(VerifyTypeEnum.FINGERPRINT)
        );

        when(biometricService.batchRegisterBiometric(eq(userId), eq(requestList)))
                .thenReturn(ResponseDTO.ok());

        // When & Then
        mockMvc.perform(post("/api/v1/biometric/batch-register")
                .param("userId", "1")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(requestList))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取统计信息 - 成功")
    void testGetStatistics_Success() throws Exception {
        // Given
        BiometricDataManager.BiometricDataStatistics statistics = new BiometricDataManager.BiometricDataStatistics();
        statistics.setTotalUsers(100);
        statistics.setTotalDataCount(250);
        statistics.setCacheSize(80);

        when(biometricService.getStatistics())
                .thenReturn(ResponseDTO.ok(statistics));

        // When & Then
        mockMvc.perform(get("/api/v1/biometric/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.totalDataCount").value(250))
                .andExpect(jsonPath("$.data.cacheSize").value(80));
    }

    @Test
    @DisplayName("测试获取支持类型 - 成功")
    void testGetSupportedTypes_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/biometric/supported-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(6))
                .andExpect(jsonPath("$.data[0].name").value("人脸识别"))
                .andExpect(jsonPath("$.data[0].verifyType").value("FACE"));
    }

    @Test
    @DisplayName("测试健康检查 - 成功")
    void testHealth_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/biometric/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("生物识别服务运行正常"));
    }

    @Test
    @DisplayName("测试异步注册 - 成功")
    void testRegisterBiometricAsync_Success() throws Exception {
        // Given
        Long userId = 1L;
        VerifyTypeEnum verifyType = VerifyTypeEnum.FACE;
        Long deviceId = 1001L;
        byte[] featureData = "test_async_feature_data".getBytes(StandardCharsets.UTF_8);

        BiometricController.BiometricRegisterRequest request = new BiometricController.BiometricRegisterRequest();
        request.setFeatureData(featureData);
        request.setTemplateData(new byte[]{});

        CompletableFuture<ResponseDTO<Void>> future = CompletableFuture.completedFuture(ResponseDTO.ok());
        when(biometricService.registerBiometricAsync(eq(userId), eq(verifyType), eq(featureData), eq(new byte[]{}), eq(deviceId)))
                .thenReturn(future);

        // When & Then
        mockMvc.perform(post("/api/v1/biometric/register-async")
                .param("userId", "1")
                .param("verifyType", "FACE")
                .param("deviceId", "1001")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试处理设备消息 - 成功")
    void testProcessDeviceBiometricMessage_Success() throws Exception {
        // Given
        Long deviceId = 1001L;
        byte[] rawData = "test_device_message".getBytes(StandardCharsets.UTF_8);
        String response = "SUCCESS_RESPONSE";

        when(biometricService.processDeviceBiometricMessage(eq(deviceId), eq(rawData)))
                .thenReturn(ResponseDTO.ok(response));

        // When & Then
        mockMvc.perform(post("/api/v1/biometric/device-message/1001")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_OCTET_STREAM))
                .content(Objects.requireNonNull(rawData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("SUCCESS_RESPONSE"));
    }

    @Test
    @DisplayName("测试参数验证 - 用户ID为空")
    void testRegisterBiometric_MissingUserId() throws Exception {
        // Given
        BiometricController.BiometricRegisterRequest request = new BiometricController.BiometricRegisterRequest();
        request.setFeatureData(new byte[]{});

        // When & Then
        mockMvc.perform(post("/api/v1/biometric/register")
                .param("verifyType", "FACE")
                .param("deviceId", "1001")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试参数验证 - 验证类型无效")
    void testVerifyBiometric_InvalidVerifyType() throws Exception {
        // Given
        BiometricController.BiometricVerifyRequest request = new BiometricController.BiometricVerifyRequest();
        request.setFeatureData(new byte[]{});

        // When & Then
        mockMvc.perform(post("/api/v1/biometric/verify")
                .param("userId", "1")
                .param("verifyType", "INVALID_TYPE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("集成测试 - 完整的生物识别流程")
    void testCompleteBiometricFlow() throws Exception {
        // 1. 注册人脸特征
        BiometricController.BiometricRegisterRequest registerRequest = new BiometricController.BiometricRegisterRequest();
        registerRequest.setFeatureData("face_feature".getBytes(StandardCharsets.UTF_8));
        registerRequest.setTemplateData(new byte[]{});

        mockMvc.perform(post("/api/v1/biometric/register")
                .param("userId", "1")
                .param("verifyType", "FACE")
                .param("deviceId", "1001")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(registerRequest))))
                .andExpect(status().isOk());

        // 2. 注册指纹特征
        registerRequest.setFeatureData("fingerprint_feature".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(post("/api/v1/biometric/register")
                .param("userId", "1")
                .param("verifyType", "FINGERPRINT")
                .param("deviceId", "1001")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(registerRequest))))
                .andExpect(status().isOk());

        // 3. 获取支持的验证方式
        mockMvc.perform(get("/api/v1/biometric/verify-types/1"))
                .andExpect(status().isOk());

        // 4. 验证人脸特征
        BiometricController.BiometricVerifyRequest verifyRequest = new BiometricController.BiometricVerifyRequest();
        verifyRequest.setFeatureData("face_feature".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/biometric/verify")
                .param("userId", "1")
                .param("verifyType", "FACE")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(verifyRequest))))
                .andExpect(status().isOk());

        // 5. 获取用户生物数据
        mockMvc.perform(get("/api/v1/biometric/data/1"))
                .andExpect(status().isOk());

        // 6. 获取统计信息
        mockMvc.perform(get("/api/v1/biometric/statistics"))
                .andExpect(status().isOk());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建模拟生物识别数据
     */
    private BiometricDataManager.BiometricData createMockBiometricData(Long userId, VerifyTypeEnum verifyType) {
        BiometricDataManager.BiometricData data = new BiometricDataManager.BiometricData();
        data.setUserId(userId);
        data.setVerifyType(verifyType);
        data.setFeatureData(("feature_" + verifyType.name()).getBytes(StandardCharsets.UTF_8));
        data.setTemplateData(new byte[]{});
        data.setDeviceId(1001L);
        data.setCreateTime(System.currentTimeMillis());
        data.setUpdateTime(System.currentTimeMillis());
        data.setActive(true);
        return data;
    }

    /**
     * 创建模拟注册请求
     */
    private BiometricService.BiometricRegisterRequest createMockRegisterRequest(VerifyTypeEnum verifyType) {
        BiometricService.BiometricRegisterRequest request = new BiometricService.BiometricRegisterRequest();
        request.setVerifyType(verifyType);
        request.setFeatureData(("feature_" + verifyType.name()).getBytes(StandardCharsets.UTF_8));
        request.setTemplateData(new byte[]{});
        request.setDeviceId(1001L);
        return request;
    }
}
