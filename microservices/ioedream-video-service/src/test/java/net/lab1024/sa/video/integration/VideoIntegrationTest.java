package net.lab1024.sa.video.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.video.dao.VideoDeviceDao;
import net.lab1024.sa.video.dao.VideoRecordDao;
import net.lab1024.sa.video.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.video.domain.entity.VideoRecordEntity;
import net.lab1024.sa.video.service.VideoSurveillanceService;

/**
 * 视频服务集成测试
 * 测试完整的视频监控业务流程，包括设备管理、录像、监控等
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("视频服务集成测试")
class VideoIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Resource
    private VideoSurveillanceService videoSurveillanceService;

    @Resource
    private VideoDeviceDao videoDeviceDao;

    @Resource
    private VideoRecordDao videoRecordDao;

    private MockMvc mockMvc;
    private Long testDeviceId = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试视频设备管理完整流程")
    void testVideoDeviceManagementWorkflow() throws Exception {
        // 1. 分页查询设备
        mockMvc.perform(get("/api/video/devices")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.list").isArray());

        // 2. 获取设备详情
        mockMvc.perform(get("/api/video/devices/{deviceId}", testDeviceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 3. 获取直播流地址
        mockMvc.perform(get("/api/video/devices/{deviceId}/stream", testDeviceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试录像管理完整流程")
    void testVideoRecordingWorkflow() throws Exception {
        // 1. 开始录像
        String startResult = mockMvc.perform(post("/api/video/recording/start")
                .param("deviceId", String.valueOf(testDeviceId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<Long> startResponse = objectMapper.readValue(startResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Long.class));
        Long recordId = startResponse.getData();
        assertNotNull(recordId);

        // 2. 查询录像记录
        mockMvc.perform(get("/api/video/records/{recordId}", recordId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.recordId").value(recordId));

        // 3. 停止录像
        mockMvc.perform(post("/api/video/recording/stop/{recordId}", recordId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 4. 获取录像统计
        mockMvc.perform(get("/api/video/recording/stats")
                .param("deviceId", String.valueOf(testDeviceId))
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试PTZ控制流程")
    void testPtzControlWorkflow() throws Exception {
        // 1. PTZ控制
        mockMvc.perform(post("/api/video/devices/{deviceId}/ptz", testDeviceId)
                .param("action", "UP")
                .param("speed", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 2. 获取设备截图
        mockMvc.perform(get("/api/video/devices/{deviceId}/snapshot", testDeviceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试数据库事务一致性")
    void testDatabaseTransactionConsistency() throws Exception {
        // 1. 创建设备
        VideoDeviceEntity device = new VideoDeviceEntity();
        device.setDeviceName("事务测试设备");
        device.setDeviceCode("TX001");
        device.setDeviceType("CAMERA");
        device.setDeviceStatus("ONLINE");
        device.setDeviceIp("192.168.1.100");
        device.setDevicePort(8000);
        videoDeviceDao.insert(device);
        Long deviceId = device.getDeviceId();

        // 2. 创建录像记录
        VideoRecordEntity record = new VideoRecordEntity();
        record.setDeviceId(deviceId);
        record.setRecordType("MANUAL");
        record.setRecordStatus("RECORDING");
        record.setRecordStartTime(java.time.LocalDateTime.now());
        videoRecordDao.insert(record);

        // 3. 验证关联关系
        VideoRecordEntity savedRecord = videoRecordDao.selectById(record.getRecordId());
        assertNotNull(savedRecord);
        assertEquals(deviceId, savedRecord.getDeviceId());

        // 4. 通过设备查询记录
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VideoRecordEntity> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(VideoRecordEntity::getDeviceId, deviceId);
        Long count = videoRecordDao.selectCount(wrapper);
        assertTrue(count > 0);
    }

    @Test
    @DisplayName("测试录像统计功能")
    void testRecordingStatisticsWorkflow() throws Exception {
        // 1. 通过服务层获取录像统计
        Object stats = videoSurveillanceService.getRecordingStats(
                testDeviceId, "2025-01-01", "2025-01-31");
        assertNotNull(stats);

        // 2. 验证统计数据结构
        if (stats instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> statsMap = (java.util.Map<String, Object>) stats;
            assertTrue(statsMap.containsKey("totalCount"));
            assertTrue(statsMap.containsKey("totalDuration"));
            assertTrue(statsMap.containsKey("totalFileSize"));
        }
    }
}
