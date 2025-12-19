package net.lab1024.sa.consume.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.consume.domain.form.ConsumeMobileFaceForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileNfcForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileQuickForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileScanForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceConfigVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileMealVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileSummaryVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserInfoVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncDataVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeValidateResultVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;

/**
 * 消费移动端控制器单元测试
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("消费移动端控制器单元测试")
@SuppressWarnings("null")
class ConsumeMobileControllerTest {

        @Mock
        private ConsumeMobileService consumeMobileService;

        @InjectMocks
        private ConsumeMobileController consumeMobileController;

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
                // 使用与序列化一致的 ObjectMapper，避免 LocalDateTime 等类型反序列化失败导致 400
                mockMvc = MockMvcBuilders.standaloneSetup(consumeMobileController)
                                .setMessageConverters(new MappingJackson2HttpMessageConverter(OBJECT_MAPPER))
                                .build();
                // 性能优化：使用JsonUtil统一ObjectMapper实例，避免重复创建
                objectMapper = OBJECT_MAPPER;
        }

        @Test
        @DisplayName("测试快速消费")
        void testQuickConsume() throws Exception {
                ConsumeMobileQuickForm form = new ConsumeMobileQuickForm();
                form.setDeviceId(2001L);
                form.setUserId(1001L);
                form.setAmount(new BigDecimal("10.00"));

                ConsumeMobileResultVO result = new ConsumeMobileResultVO();
                result.setSuccess(true);
                result.setMessage("消费成功");

                when(consumeMobileService.quickConsume(any(ConsumeMobileQuickForm.class)))
                                .thenReturn(result);

                mockMvc.perform(post("/api/v1/consume/mobile/transaction/quick")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(form)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试扫码消费")
        void testScanConsume() throws Exception {
                ConsumeMobileScanForm form = new ConsumeMobileScanForm();
                form.setDeviceId(2001L);
                form.setQrCode("QR_CODE_123456");
                form.setAmount(new BigDecimal("15.00"));

                ConsumeMobileResultVO result = new ConsumeMobileResultVO();
                result.setSuccess(true);

                when(consumeMobileService.scanConsume(any(ConsumeMobileScanForm.class)))
                                .thenReturn(result);

                mockMvc.perform(post("/api/v1/consume/mobile/transaction/scan")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(form)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试NFC消费")
        void testNfcConsume() throws Exception {
                ConsumeMobileNfcForm form = new ConsumeMobileNfcForm();
                form.setDeviceId(2001L);
                form.setCardNumber("NFC_123456");
                form.setAmount(new BigDecimal("20.00"));

                ConsumeMobileResultVO result = new ConsumeMobileResultVO();
                result.setSuccess(true);

                when(consumeMobileService.nfcConsume(any(ConsumeMobileNfcForm.class)))
                                .thenReturn(result);

                mockMvc.perform(post("/api/v1/consume/mobile/transaction/nfc")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(form)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试人脸识别消费")
        void testFaceConsume() throws Exception {
                ConsumeMobileFaceForm form = new ConsumeMobileFaceForm();
                form.setDeviceId(2001L);
                form.setFaceFeatures("face_features_base64");
                form.setAmount(new BigDecimal("25.00"));

                ConsumeMobileResultVO result = new ConsumeMobileResultVO();
                result.setSuccess(true);

                when(consumeMobileService.faceConsume(any(ConsumeMobileFaceForm.class)))
                                .thenReturn(result);

                mockMvc.perform(post("/api/v1/consume/mobile/transaction/face")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(form)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试快速用户查询")
        void testQuickUserInfo() throws Exception {
                ConsumeMobileUserVO userVO = new ConsumeMobileUserVO();
                userVO.setUserId(1001L);
                userVO.setUserName("张三");

                when(consumeMobileService.quickUserInfo(eq("userId"), eq("1001")))
                                .thenReturn(userVO);

                mockMvc.perform(get("/api/v1/consume/mobile/user/quick")
                                .param("queryType", "userId")
                                .param("queryValue", "1001")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试获取用户消费信息")
        void testGetUserConsumeInfo() throws Exception {
                ConsumeMobileUserInfoVO userInfo = new ConsumeMobileUserInfoVO();
                userInfo.setUserId(1001L);
                userInfo.setTotalBalance(new BigDecimal("100.00"));

                when(consumeMobileService.getUserConsumeInfo(eq(1001L)))
                                .thenReturn(userInfo);

                mockMvc.perform(get("/api/v1/consume/mobile/user/consume-info/1001")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试获取有效餐别")
        void testGetAvailableMeals() throws Exception {
                List<ConsumeMobileMealVO> meals = new ArrayList<>();
                when(consumeMobileService.getAvailableMeals())
                                .thenReturn(meals);

                mockMvc.perform(get("/api/v1/consume/mobile/meal/available")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试获取设备配置")
        void testGetDeviceConfig() throws Exception {
                ConsumeDeviceConfigVO config = new ConsumeDeviceConfigVO();
                config.setDeviceId(2001L);

                when(consumeMobileService.getDeviceConfig(eq(2001L)))
                                .thenReturn(config);

                mockMvc.perform(get("/api/v1/consume/mobile/device/config/2001")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试获取设备今日统计")
        void testGetDeviceTodayStats() throws Exception {
                ConsumeMobileStatsVO stats = new ConsumeMobileStatsVO();
                when(consumeMobileService.getDeviceTodayStats(eq(2001L)))
                                .thenReturn(stats);

                mockMvc.perform(get("/api/v1/consume/mobile/device/today-stats/2001")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试获取实时交易汇总")
        void testGetTransactionSummary() throws Exception {
                ConsumeMobileSummaryVO summary = new ConsumeMobileSummaryVO();
                when(consumeMobileService.getTransactionSummary(eq("3001")))
                                .thenReturn(summary);

                mockMvc.perform(get("/api/v1/consume/mobile/transaction/summary")
                                .param("areaId", "3001")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试离线交易同步")
        void testSyncOfflineTransactions() throws Exception {
                net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm form = new net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm();
                form.setDeviceNo("DEV2001");
                form.setDeviceId(2001L);
                form.setSyncTime(java.time.LocalDateTime.now());

                net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm.OfflineConsumeRecord record = new net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm.OfflineConsumeRecord();
                record.setTransactionNo("OFFLINE_TX_001");
                record.setUserId(1001L);
                record.setAccountNo("ACC_1001");
                record.setAmount(new BigDecimal("10.00"));
                record.setConsumeTime(java.time.LocalDateTime.now());

                form.setRecords(java.util.Collections.singletonList(record));

                ConsumeSyncResultVO result = new ConsumeSyncResultVO();
                result.setSuccess(true);

                when(consumeMobileService.syncOfflineTransactions(any()))
                                .thenReturn(result);

                mockMvc.perform(post("/api/v1/consume/mobile/sync/offline")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(form)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试获取同步数据")
        void testGetSyncData() throws Exception {
                ConsumeSyncDataVO syncData = new ConsumeSyncDataVO();
                syncData.setDeviceId(2001L);

                when(consumeMobileService.getSyncData(eq(2001L), any()))
                                .thenReturn(syncData);

                mockMvc.perform(get("/api/v1/consume/mobile/sync/data/2001")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }

        @Test
        @DisplayName("测试权限验证")
        void testValidatePermission() throws Exception {
                net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm form = new net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm();
                form.setUserId(1001L);
                form.setAreaId("3001");
                form.setAmount(new BigDecimal("10.00"));

                ConsumeValidateResultVO result = new ConsumeValidateResultVO();
                result.setValid(true);

                when(consumeMobileService.validateConsumePermission(any()))
                                .thenReturn(result);

                mockMvc.perform(post("/api/v1/consume/mobile/validate/permission")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(form)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").exists());
        }
}
