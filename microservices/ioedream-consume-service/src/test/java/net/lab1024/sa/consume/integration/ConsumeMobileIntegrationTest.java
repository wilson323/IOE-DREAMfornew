package net.lab1024.sa.consume.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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
import net.lab1024.sa.consume.domain.form.ConsumeMobileFaceForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileNfcForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileQuickForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileScanForm;
import net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 移动端消费集成测试
 * <p>
 * 测试移动端消费功能的完整业务流程，包括快速消费、扫码消费、NFC消费、人脸识别消费等
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("移动端消费集成测试")
@SuppressWarnings("null")
class ConsumeMobileIntegrationTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Long testUserId = 1001L;
    private Long testDeviceId = 2001L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试完整移动端消费流程：快速消费->查询余额->查询记录->统计")
    void testCompleteMobileConsumeWorkflow() throws Exception {
        // 1. 快速消费
        ConsumeMobileQuickForm quickForm = new ConsumeMobileQuickForm();
        quickForm.setDeviceId(testDeviceId);
        quickForm.setUserId(testUserId);
        quickForm.setAmount(new BigDecimal("10.00"));

        String quickResult = mockMvc.perform(post("/api/v1/consume/mobile/transaction/quick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(quickForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> quickResponse = objectMapper.readValue(quickResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertNotNull(quickResponse, "快速消费响应不应为空");

        // 2. 获取用户消费信息
        String userInfoResult = mockMvc.perform(get("/api/v1/consume/mobile/user/consume-info/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> userInfoResponse = objectMapper.readValue(userInfoResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(userInfoResponse.isSuccess(), "获取用户消费信息应该成功");

        // 3. 获取最近消费记录
        String recentResult = mockMvc.perform(get("/api/v1/consume/mobile/history/recent")
                .param("userId", testUserId.toString())
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> recentResponse = objectMapper.readValue(recentResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(recentResponse.isSuccess(), "获取最近消费记录应该成功");

        // 4. 获取交易汇总
        String summaryResult = mockMvc.perform(get("/api/v1/consume/mobile/transaction/summary")
                .param("areaId", "3001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> summaryResponse = objectMapper.readValue(summaryResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(summaryResponse.isSuccess(), "获取交易汇总应该成功");
    }

    @Test
    @DisplayName("测试多种消费方式")
    void testMultipleConsumeMethods() throws Exception {
        // 1. 扫码消费
        ConsumeMobileScanForm scanForm = new ConsumeMobileScanForm();
        scanForm.setDeviceId(testDeviceId);
        scanForm.setQrCode("QR_CODE_123456");
        scanForm.setAmount(new BigDecimal("15.00"));

        String scanResult = mockMvc.perform(post("/api/v1/consume/mobile/transaction/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(scanForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> scanResponse = objectMapper.readValue(scanResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertNotNull(scanResponse, "扫码消费响应不应为空");

        // 2. NFC消费
        ConsumeMobileNfcForm nfcForm = new ConsumeMobileNfcForm();
        nfcForm.setDeviceId(testDeviceId);
        nfcForm.setCardNumber("NFC_123456");
        nfcForm.setAmount(new BigDecimal("20.00"));

        String nfcResult = mockMvc.perform(post("/api/v1/consume/mobile/transaction/nfc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nfcForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> nfcResponse = objectMapper.readValue(nfcResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertNotNull(nfcResponse, "NFC消费响应不应为空");

        // 3. 人脸识别消费
        ConsumeMobileFaceForm faceForm = new ConsumeMobileFaceForm();
        faceForm.setDeviceId(testDeviceId);
        faceForm.setFaceFeatures("face_features_base64");
        faceForm.setAmount(new BigDecimal("25.00"));

        String faceResult = mockMvc.perform(post("/api/v1/consume/mobile/transaction/face")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(faceForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> faceResponse = objectMapper.readValue(faceResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertNotNull(faceResponse, "人脸识别消费响应不应为空");
    }

    @Test
    @DisplayName("测试离线交易同步")
    void testOfflineTransactionSync() throws Exception {
        // 1. 离线交易同步
        ConsumeOfflineSyncForm syncForm = new ConsumeOfflineSyncForm();
        syncForm.setDeviceId(testDeviceId);

        String syncResult = mockMvc.perform(post("/api/v1/consume/mobile/sync/offline")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(syncForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> syncResponse = objectMapper.readValue(syncResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(syncResponse.isSuccess(), "离线交易同步应该成功");

        // 2. 获取同步数据
        String syncDataResult = mockMvc.perform(get("/api/v1/consume/mobile/sync/data/{deviceId}", testDeviceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> syncDataResponse = objectMapper.readValue(syncDataResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(syncDataResponse.isSuccess(), "获取同步数据应该成功");
    }

    @Test
    @DisplayName("测试设备配置和统计")
    void testDeviceConfigAndStats() throws Exception {
        // 1. 获取设备配置
        String configResult = mockMvc.perform(get("/api/v1/consume/mobile/device/config/{deviceId}", testDeviceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> configResponse = objectMapper.readValue(configResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(configResponse.isSuccess(), "获取设备配置应该成功");

        // 2. 获取设备今日统计
        String statsResult = mockMvc.perform(get("/api/v1/consume/mobile/device/today-stats/{deviceId}", testDeviceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> statsResponse = objectMapper.readValue(statsResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(statsResponse.isSuccess(), "获取设备统计应该成功");
    }
}



