package net.lab1024.sa.access.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.access.controller.AccessMobileController.BiometricVerifyRequest;
import net.lab1024.sa.access.controller.AccessMobileController.MobileAccessCheckRequest;
import net.lab1024.sa.access.controller.AccessMobileController.NFCVerifyRequest;
import net.lab1024.sa.access.controller.AccessMobileController.QRCodeVerifyRequest;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.access.service.AccessEventService;
import net.lab1024.sa.access.service.AdvancedAccessControlService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * 门禁移动端控制器单元测试
 * <p>
 * 注意：MediaType.APPLICATION_JSON的Null Type Safety警告是IDE检查器的误报，
 * org.springframework.http.MediaType.APPLICATION_JSON是常量，不会为null
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁移动端控制器单元测试")
class AccessMobileControllerTest {

    @Mock
    private AccessDeviceService accessDeviceService;

    @Mock
    private AccessEventService accessEventService;

    @Mock
    private AdvancedAccessControlService advancedAccessControlService;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private AccessMobileController accessMobileController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    /**
     * 使用JsonUtil统一ObjectMapper实例（性能优化）
     * <p>
     * ObjectMapper是线程安全的，设计用于复用
     * JsonUtil已配置JavaTimeModule，无需重复配置
     * </p>
     */
    private static final ObjectMapper OBJECT_MAPPER = net.lab1024.sa.common.util.JsonUtil.getObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accessMobileController).build();
        // 性能优化：使用JsonUtil统一ObjectMapper实例，避免重复创建
        objectMapper = OBJECT_MAPPER;
    }

    @Test
    @DisplayName("测试移动端门禁检查")
    void testMobileAccessCheck() throws Exception {
        MobileAccessCheckRequest request = new MobileAccessCheckRequest();
        request.setUserId(1001L);
        request.setDeviceId(2001L);
        request.setAreaId(3001L);
        request.setVerificationType("QR_CODE");
        request.setLocation("北京市朝阳区");

        AdvancedAccessControlService.AccessControlResult controlResult = new AdvancedAccessControlService.AccessControlResult();
        controlResult.setAllowed(true);
        controlResult.setDenyReason(null);

        when(advancedAccessControlService.performAccessControlCheck(
                eq(1001L), eq(2001L), eq(3001L), any(), eq("MOBILE_ACCESS")))
                .thenReturn(controlResult);

        mockMvc.perform(post("/api/v1/mobile/access/check")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试二维码验证")
    void testVerifyQRCode() throws Exception {
        QRCodeVerifyRequest request = new QRCodeVerifyRequest();
        request.setQrCode("QR_CODE_123456");
        request.setDeviceId(2001L);

        AdvancedAccessControlService.AccessControlResult controlResult = new AdvancedAccessControlService.AccessControlResult();
        controlResult.setAllowed(true);

        when(advancedAccessControlService.performAccessControlCheck(
                any(), eq(2001L), any(), any(), eq("QR_CODE_ACCESS")))
                .thenReturn(controlResult);

        mockMvc.perform(post("/api/v1/mobile/access/qr/verify")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试NFC验证")
    void testVerifyNFC() throws Exception {
        NFCVerifyRequest request = new NFCVerifyRequest();
        request.setNfcCardId("NFC_123456");
        request.setDeviceId(2001L);

        AdvancedAccessControlService.AccessControlResult controlResult = new AdvancedAccessControlService.AccessControlResult();
        controlResult.setAllowed(true);

        when(advancedAccessControlService.performAccessControlCheck(
                any(), eq(2001L), any(), any(), eq("NFC_ACCESS")))
                .thenReturn(controlResult);

        mockMvc.perform(post("/api/v1/mobile/access/nfc/verify")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试生物识别验证")
    void testVerifyBiometric() throws Exception {
        BiometricVerifyRequest request = new BiometricVerifyRequest();
        request.setUserId(1001L);
        request.setBiometricType("FACE");
        request.setBiometricData("biometric_data_base64");
        request.setDeviceId(2001L);

        AdvancedAccessControlService.AccessControlResult controlResult = new AdvancedAccessControlService.AccessControlResult();
        controlResult.setAllowed(true);

        when(advancedAccessControlService.performAccessControlCheck(
                eq(1001L), eq(2001L), any(), any(), eq("BIOMETRIC_ACCESS")))
                .thenReturn(controlResult);

        mockMvc.perform(post("/api/v1/mobile/access/biometric/verify")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取附近设备")
    void testGetNearbyDevices() throws Exception {
        List<AccessMobileController.MobileDeviceItem> devices = new ArrayList<>();
        when(accessDeviceService.getNearbyDevices(eq(1001L), eq(39.9042), eq(116.4074), eq(500)))
                .thenReturn(ResponseDTO.ok(devices));

        mockMvc.perform(get("/api/v1/mobile/access/devices/nearby")
                .param("userId", "1001")
                .param("latitude", "39.9042")
                .param("longitude", "116.4074")
                .param("radius", "500")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取用户门禁权限")
    void testGetUserPermissions() throws Exception {
        AccessMobileController.MobileUserPermissions permissions = new AccessMobileController.MobileUserPermissions();
        when(accessDeviceService.getMobileUserPermissions(eq(1001L)))
                .thenReturn(ResponseDTO.ok(permissions));

        mockMvc.perform(get("/api/v1/mobile/access/permissions/1001")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取用户访问记录")
    void testGetUserAccessRecords() throws Exception {
        List<AccessMobileController.MobileAccessRecord> records = new ArrayList<>();
        when(accessEventService.getMobileAccessRecords(eq(1001L), eq(20)))
                .thenReturn(ResponseDTO.ok(records));

        mockMvc.perform(get("/api/v1/mobile/access/records/1001")
                .param("size", "20")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试临时开门申请")
    void testRequestTemporaryAccess() throws Exception {
        AccessMobileController.TemporaryAccessRequest request = new AccessMobileController.TemporaryAccessRequest();
        request.setUserId(1001L);
        request.setDeviceId(2001L);
        request.setReason("临时访问");

        mockMvc.perform(post("/api/v1/mobile/access/temporary-access")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取实时门禁状态")
    void testGetRealTimeStatus() throws Exception {
        AccessMobileController.MobileRealTimeStatus status = new AccessMobileController.MobileRealTimeStatus();
        when(accessDeviceService.getMobileRealTimeStatus(eq(2001L)))
                .thenReturn(ResponseDTO.ok(status));

        mockMvc.perform(get("/api/v1/mobile/access/status/realtime")
                .param("deviceId", "2001")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试发送推送通知")
    void testSendPushNotification() throws Exception {
        AccessMobileController.PushNotificationRequest request = new AccessMobileController.PushNotificationRequest();
        request.setUserId(1001L);
        request.setNotificationType("ACCESS_GRANTED");

        mockMvc.perform(post("/api/v1/mobile/access/notification/push")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }
}
