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
 * 闂ㄧ绉诲姩绔帶鍒跺櫒鍗曞厓娴嬭瘯
 * <p>
 * 娉ㄦ剰锛歁ediaType.APPLICATION_JSON鐨凬ull Type Safety璀﹀憡鏄疘DE妫€鏌ュ櫒鐨勮鎶ワ紝
 * org.springframework.http.MediaType.APPLICATION_JSON鏄父閲忥紝涓嶄細涓簄ull
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("闂ㄧ绉诲姩绔帶鍒跺櫒鍗曞厓娴嬭瘯")
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
     * 浣跨敤JsonUtil缁熶竴ObjectMapper瀹炰緥锛堟€ц兘浼樺寲锛?     * <p>
     * ObjectMapper鏄嚎绋嬪畨鍏ㄧ殑锛岃璁＄敤浜庡鐢?     * JsonUtil宸查厤缃甁avaTimeModule锛屾棤闇€閲嶅閰嶇疆
     * </p>
     */
    private static final ObjectMapper OBJECT_MAPPER = net.lab1024.sa.common.util.JsonUtil.getObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accessMobileController).build();
        // 鎬ц兘浼樺寲锛氫娇鐢↗sonUtil缁熶竴ObjectMapper瀹炰緥锛岄伩鍏嶉噸澶嶅垱寤?        objectMapper = OBJECT_MAPPER;
    }

    @Test
    @DisplayName("娴嬭瘯绉诲姩绔棬绂佹鏌?)
    void testMobileAccessCheck() throws Exception {
        MobileAccessCheckRequest request = new MobileAccessCheckRequest();
        request.setUserId(1001L);
        request.setDeviceId(2001L);
        request.setAreaId(3001L);
        request.setVerificationType("QR_CODE");
        request.setLocation("鍖椾含甯傛湞闃冲尯");

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
    @DisplayName("娴嬭瘯浜岀淮鐮侀獙璇?)
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
    @DisplayName("娴嬭瘯NFC楠岃瘉")
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
    @DisplayName("娴嬭瘯鐢熺墿璇嗗埆楠岃瘉")
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
    @DisplayName("娴嬭瘯鑾峰彇闄勮繎璁惧")
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
    @DisplayName("娴嬭瘯鑾峰彇鐢ㄦ埛闂ㄧ鏉冮檺")
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
    @DisplayName("娴嬭瘯鑾峰彇鐢ㄦ埛璁块棶璁板綍")
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
    @DisplayName("娴嬭瘯涓存椂寮€闂ㄧ敵璇?)
    void testRequestTemporaryAccess() throws Exception {
        AccessMobileController.TemporaryAccessRequest request = new AccessMobileController.TemporaryAccessRequest();
        request.setUserId(1001L);
        request.setDeviceId(2001L);
        request.setReason("涓存椂璁块棶");

        mockMvc.perform(post("/api/v1/mobile/access/temporary-access")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("娴嬭瘯鑾峰彇瀹炴椂闂ㄧ鐘舵€?)
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
    @DisplayName("娴嬭瘯鍙戦€佹帹閫侀€氱煡")
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
