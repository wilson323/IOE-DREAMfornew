package net.lab1024.sa.access.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import net.lab1024.sa.access.controller.AccessMobileController.BiometricVerifyRequest;
import net.lab1024.sa.access.controller.AccessMobileController.MobileAccessCheckRequest;
import net.lab1024.sa.access.controller.AccessMobileController.NFCVerifyRequest;
import net.lab1024.sa.access.controller.AccessMobileController.QRCodeVerifyRequest;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 移动端门禁集成测试
 * <p>
 * 测试移动端门禁功能的完整业务流程，包括门禁检查、二维码验证、NFC验证、生物识别验证等
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("移动端门禁集成测试")
@SuppressWarnings("null")
class AccessMobileIntegrationTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Long testUserId = 1001L;
    private Long testDeviceId = 2001L;
    private Long testAreaId = 3001L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试完整移动端门禁流程：门禁检查->二维码验证->获取权限->查询记录")
    void testCompleteMobileAccessWorkflow() throws Exception {
        // 1. 移动端门禁检查
        MobileAccessCheckRequest checkRequest = new MobileAccessCheckRequest();
        checkRequest.setUserId(testUserId);
        checkRequest.setDeviceId(testDeviceId);
        checkRequest.setAreaId(testAreaId);
        checkRequest.setVerificationType("QR_CODE");
        checkRequest.setLocation("北京市朝阳区");

        String checkResult = mockMvc.perform(post("/api/v1/mobile/access/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> checkResponse = objectMapper.readValue(checkResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertNotNull(checkResponse, "门禁检查响应不应为空");

        // 2. 二维码验证
        QRCodeVerifyRequest qrRequest = new QRCodeVerifyRequest();
        qrRequest.setQrCode("QR_CODE_123456");
        qrRequest.setDeviceId(testDeviceId);

        String qrResult = mockMvc.perform(post("/api/v1/mobile/access/qr/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qrRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> qrResponse = objectMapper.readValue(qrResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertNotNull(qrResponse, "二维码验证响应不应为空");

        // 3. 获取用户门禁权限
        String permissionsResult = mockMvc.perform(get("/api/v1/mobile/access/permissions/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> permissionsResponse = objectMapper.readValue(permissionsResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(permissionsResponse.isSuccess(), "获取用户权限应该成功");

        // 4. 获取用户访问记录
        String recordsResult = mockMvc.perform(get("/api/v1/mobile/access/records/{userId}", testUserId)
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> recordsResponse = objectMapper.readValue(recordsResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(recordsResponse.isSuccess(), "获取访问记录应该成功");
    }

    @Test
    @DisplayName("测试多种验证方式")
    void testMultipleVerificationMethods() throws Exception {
        // 1. NFC验证
        NFCVerifyRequest nfcRequest = new NFCVerifyRequest();
        nfcRequest.setNfcCardId("NFC_123456");
        nfcRequest.setDeviceId(testDeviceId);

        String nfcResult = mockMvc.perform(post("/api/v1/mobile/access/nfc/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nfcRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> nfcResponse = objectMapper.readValue(nfcResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertNotNull(nfcResponse, "NFC验证响应不应为空");

        // 2. 生物识别验证
        BiometricVerifyRequest biometricRequest = new BiometricVerifyRequest();
        biometricRequest.setUserId(testUserId);
        biometricRequest.setBiometricType("FACE");
        biometricRequest.setBiometricData("biometric_data_base64");
        biometricRequest.setDeviceId(testDeviceId);

        String biometricResult = mockMvc.perform(post("/api/v1/mobile/access/biometric/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(biometricRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> biometricResponse = objectMapper.readValue(biometricResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertNotNull(biometricResponse, "生物识别验证响应不应为空");
    }

    @Test
    @DisplayName("测试附近设备查询")
    void testNearbyDevices() throws Exception {
        String devicesResult = mockMvc.perform(get("/api/v1/mobile/access/devices/nearby")
                .param("userId", testUserId.toString())
                .param("latitude", "39.9042")
                .param("longitude", "116.4074")
                .param("radius", "500")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> devicesResponse = objectMapper.readValue(devicesResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(devicesResponse.isSuccess(), "获取附近设备应该成功");
    }

    @Test
    @DisplayName("测试实时状态查询")
    void testRealTimeStatus() throws Exception {
        String statusResult = mockMvc.perform(get("/api/v1/mobile/access/status/realtime")
                .param("deviceId", testDeviceId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> statusResponse = objectMapper.readValue(statusResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(statusResponse.isSuccess(), "获取实时状态应该成功");
    }
}

