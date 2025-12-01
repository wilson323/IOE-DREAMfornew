/*
 * 视频模块微服务端到端测试
 * 验证视频微服务的完整业务流程和数据一致性
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-11-27
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.smart.video.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceAddForm;
import net.lab1024.sa.admin.module.smart.video.domain.form.FaceSearchForm;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.BaseTest;

/**
 * 视频模块微服务端到端测试
 * 验证完整的微服务架构下的视频功能流程
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("视频模块微服务端到端测试")
class VideoMicroserviceEndToEndTest extends BaseTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Long testDeviceId;
    private String testRecordId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        logTestStart("视频模块微服务端到端测试");
    }

    @Test
    @Order(1)
    @DisplayName("完整业务流程：设备注册->实时监控->AI分析->录像回放")
    void testCompleteBusinessFlow() throws Exception {
        logTestStep("开始完整业务流程测试");

        // === 第一阶段：设备注册和配置 ===

        // 1. 注册视频设备
        VideoDeviceAddForm device = VideoDeviceAddForm.builder()
                .deviceName("端到端测试摄像机")
                .deviceCode("E2E_TEST_CAMERA_001")
                .deviceType("IP_CAMERA")
                .manufacturer("Hikvision")
                .model("DS-2CD2143G2-I")
                .serialNumber("E2E_TEST_SN_" + UUID.randomUUID().toString().substring(0, 8))
                .ipAddress("192.168.1.150")
                .port(554)
                .username("admin")
                .password("test123456")
                .rtspUrl("rtsp://192.168.1.150:554/Streaming/Channels/101")
                .status(1)
                .build();

        String deviceResponse = mockMvc.perform(post("/api/smart/video/device/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(device)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> deviceResult = objectMapper.readValue(deviceResponse, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> deviceData = (Map<String, Object>) deviceResult.get("data");
        testDeviceId = Long.valueOf(deviceData.get("deviceId").toString());

        assertNotNull(testDeviceId);
        logTestStep("设备注册完成，设备ID: " + testDeviceId);

        // 2. 验证设备状态
        mockMvc.perform(get("/api/smart/video/device/" + testDeviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.deviceName").value("端到端测试摄像机"))
                .andExpect(jsonPath("$.data.status").value(1));

        logTestStep("设备状态验证完成");

        // === 第二阶段：实时视频监控 ===

        // 3. 启动实时监控
        String surveillanceResponse = mockMvc.perform(post("/api/smart/video/surveillance/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", testDeviceId,
                        "streamType", "MAIN",
                        "recordEnabled", true
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> surveillanceResult = objectMapper.readValue(surveillanceResponse, Map.class);
        String streamUrl = (String) surveillanceResult.get("data");

        assertNotNull(streamUrl);
        logTestStep("实时监控启动完成，流地址: " + streamUrl);

        // 4. 获取监控状态
        mockMvc.perform(get("/api/smart/video/surveillance/status/" + testDeviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.status").value("STREAMING"))
                .andExpect(jsonPath("$.data.streamType").value("MAIN"));

        logTestStep("监控状态验证完成");

        // === 第三阶段：AI智能分析 ===

        // 5. 执行人脸检测分析
        mockMvc.perform(post("/api/smart/video/ai/face-detection")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", testDeviceId,
                        "imageUrl", "http://example.com/e2e_face_test.jpg",
                        "confidence", 0.8,
                        "saveResult", true
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray());

        logTestStep("人脸检测分析完成");

        // 6. 执行行为分析
        mockMvc.perform(post("/api/smart/video/ai/behavior-analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", testDeviceId,
                        "videoUrl", streamUrl,
                        "analysisTypes", Arrays.asList("WALKING", "RUNNING", "FALLING"),
                        "startTime", LocalDateTime.now().minusMinutes(5),
                        "endTime", LocalDateTime.now(),
                        "saveResult", true
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("行为分析完成");

        // 7. 创建人脸特征库并搜索
        mockMvc.perform(post("/api/smart/video/ai/face-feature/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", testDeviceId,
                        "personId", "E2E_TEST_PERSON_001",
                        "faceImage", "http://example.com/e2e_person_face.jpg",
                        "featureData", "e2e_face_feature_vector"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        logTestStep("人脸特征创建完成");

        FaceSearchForm faceSearch = FaceSearchForm.builder()
                .faceImage("http://example.com/e2e_search_face.jpg")
                .deviceId(testDeviceId)
                .confidence(0.75)
                .maxResults(5)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/smart/video/ai/face-search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(faceSearch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray());

        logTestStep("人脸搜索完成");

        // === 第四阶段：录像存储和回放 ===

        // 8. 创建测试录像记录
        String recordResponse = mockMvc.perform(post("/api/smart/video/record/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", testDeviceId,
                        "fileName", "e2e_test_recording_" + System.currentTimeMillis() + ".mp4",
                        "filePath", "/data/videos/e2e_test_recording.mp4",
                        "fileSize", 1024000L,
                        "duration", 300,
                        "startTime", LocalDateTime.now().minusMinutes(5),
                        "endTime", LocalDateTime.now(),
                        "recordType", "MANUAL"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andReturn().getResponse().getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, Object> recordResult = objectMapper.readValue(recordResponse, Map.class);
        testRecordId = recordResult.get("data").toString();

        assertNotNull(testRecordId);
        logTestStep("录像记录创建完成，记录ID: " + testRecordId);

        // 9. 查询录像列表
        mockMvc.perform(post("/api/smart/video/playback/record/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", testDeviceId,
                        "startTime", LocalDateTime.now().minusHours(1),
                        "endTime", LocalDateTime.now(),
                        "pageNum", 1,
                        "pageSize", 20
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1));

        logTestStep("录像列表查询完成");

        // 10. 启动录像回放
        mockMvc.perform(post("/api/smart/video/playback/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "recordId", testRecordId,
                        "startTime", LocalDateTime.now().minusMinutes(5),
                        "playbackSpeed", 1.0
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("录像回放启动完成");

        // === 第五阶段：数据一致性验证 ===

        // 11. 验证设备完整信息
        mockMvc.perform(get("/api/smart/video/device/" + testDeviceId + "/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.deviceId").value(testDeviceId))
                .andExpect(jsonPath("$.data.deviceName").value("端到端测试摄像机"))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.surveillanceStatus").value("STREAMING"));

        logTestStep("设备完整信息验证完成");

        // 12. 验证AI分析统计
        mockMvc.perform(get("/api/smart/video/ai/statistics")
                .param("deviceId", testDeviceId.toString())
                .param("startDate", LocalDateTime.now().minusHours(1).toString())
                .param("endDate", LocalDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.deviceId").value(testDeviceId))
                .andExpect(jsonPath("$.data.totalAnalyses").exists())
                .andExpect(jsonPath("$.data.successCount").exists());

        logTestStep("AI分析统计验证完成");

        // 13. 验证录像完整性
        mockMvc.perform(get("/api/smart/video/record/" + testRecordId + "/integrity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.recordId").value(testRecordId))
                .andExpect(jsonPath("$.data.integrityStatus").exists())
                .andExpect(jsonPath("$.data.fileExists").value(true));

        logTestStep("录像完整性验证完成");
    }

    @Test
    @Order(2)
    @DisplayName("微服务架构一致性验证")
    void testMicroserviceArchitectureConsistency() throws Exception {
        logTestStep("开始微服务架构一致性验证");

        // 1. 验证服务发现注册
        mockMvc.perform(get("/api/smart/video/service/discovery/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.registered").value(true))
                .andExpect(jsonPath("$.data.serviceName").value("smart-video-service"))
                .andExpect(jsonPath("$.data.instances").isArray());

        logTestStep("服务发现验证完成");

        // 2. 验证配置中心同步
        mockMvc.perform(get("/api/smart/video/config/sync/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.syncStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.data.lastSyncTime").exists());

        logTestStep("配置中心同步验证完成");

        // 3. 验证数据库连接
        mockMvc.perform(get("/api/smart/video/health/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.connectionPool").exists());

        logTestStep("数据库连接验证完成");

        // 4. 验证缓存服务
        mockMvc.perform(get("/api/smart/video/health/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.redis").exists());

        logTestStep("缓存服务验证完成");

        // 5. 验证消息队列
        mockMvc.perform(get("/api/smart/video/health/messagequeue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.rabbitmq").exists());

        logTestStep("消息队列验证完成");
    }

    @Test
    @Order(3)
    @DisplayName("跨服务数据一致性测试")
    void testCrossServiceDataConsistency() throws Exception {
        logTestStep("开始跨服务数据一致性测试");

        // 1. 创建与人员服务关联的视频事件
        mockMvc.perform(post("/api/smart/video/event/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", testDeviceId,
                        "eventType", "FACE_DETECTED",
                        "personId", "E2E_TEST_PERSON_001",
                        "eventTime", LocalDateTime.now(),
                        "eventData", Map.of(
                                "confidence", 0.95,
                                "faceImage", "http://example.com/detected_face.jpg"
                        )
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        logTestStep("视频事件创建完成");

        // 2. 验证事件与人员数据同步
        mockMvc.perform(get("/api/smart/video/event/person/E2E_TEST_PERSON_001")
                .param("startTime", LocalDateTime.now().minusHours(1).toString())
                .param("endTime", LocalDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray());

        logTestStep("人员数据同步验证完成");

        // 3. 验证区域权限数据同步
        mockMvc.perform(get("/api/smart/video/device/" + testDeviceId + "/permissions")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("区域权限数据同步验证完成");

        // 4. 验证审计日志一致性
        mockMvc.perform(post("/api/smart/video/audit/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "moduleId", "VIDEO",
                        "userId", "1",
                        "startTime", LocalDateTime.now().minusHours(1),
                        "endTime", LocalDateTime.now(),
                        "pageNum", 1,
                        "pageSize", 20
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("审计日志一致性验证完成");
    }

    @Test
    @Order(4)
    @DisplayName("性能和稳定性测试")
    void testPerformanceAndStability() throws Exception {
        logTestStep("开始性能和稳定性测试");

        // 1. 并发设备状态查询
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/smart/video/device/" + testDeviceId + "/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));
        }

        logTestStep("并发设备状态查询完成");

        // 2. 大量AI分析任务提交
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/smart/video/ai/submit-task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                            "deviceId", testDeviceId,
                            "taskType", "FACE_DETECTION",
                            "parameters", Map.of(
                                    "imageUrl", "http://example.com/batch_face_" + i + ".jpg",
                                    "confidence", 0.8
                            )
                    ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));
        }

        logTestStep("批量AI分析任务提交完成");

        // 3. 录像查询性能测试
        long startTime = System.currentTimeMillis();
        mockMvc.perform(post("/api/smart/video/playback/record/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", testDeviceId,
                        "startTime", LocalDateTime.now().minusDays(7),
                        "endTime", LocalDateTime.now(),
                        "pageNum", 1,
                        "pageSize", 100
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        long queryTime = System.currentTimeMillis() - startTime;

        assertTrue("录像查询时间过长: " + queryTime + "ms", queryTime < 5000);
        logTestStep("录像查询性能测试完成，耗时: " + queryTime + "ms");

        // 4. 内存和CPU使用率检查
        mockMvc.perform(get("/api/smart/video/metrics/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.memoryUsage").exists())
                .andExpect(jsonPath("$.data.cpuUsage").exists())
                .andExpect(jsonPath("$.data.threadCount").exists());

        logTestStep("性能指标检查完成");
    }

    @Test
    @Order(5)
    @DisplayName("故障恢复和容错测试")
    void testFaultRecoveryAndTolerance() throws Exception {
        logTestStep("开始故障恢复和容错测试");

        // 1. 模拟网络中断后的重连
        mockMvc.perform(post("/api/smart/video/device/" + testDeviceId + "/reconnect")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.reconnectStatus").exists());

        logTestStep("设备重连测试完成");

        // 2. 模拟AI分析任务失败后的重试
        mockMvc.perform(post("/api/smart/video/ai/task/retry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "taskId", "FAILED_TASK_001",
                        "maxRetries", 3
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.retryStatus").exists());

        logTestStep("AI任务重试测试完成");

        // 3. 数据库连接中断恢复测试
        mockMvc.perform(get("/api/smart/video/health/database/recovery")
                .param("simulateFailure", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.recoveryStatus").exists());

        logTestStep("数据库连接恢复测试完成");

        // 4. 缓存降级测试
        mockMvc.perform(get("/api/smart/video/cache/fallback/test"))
                .param("key", "test_key")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.fallbackActivated").exists());

        logTestStep("缓存降级测试完成");
    }

    @Test
    @Order(6)
    @DisplayName("清理测试数据")
    void cleanupTestData() throws Exception {
        logTestStep("开始清理测试数据");

        // 1. 停止实时监控
        if (testDeviceId != null) {
            mockMvc.perform(post("/api/smart/video/surveillance/stop")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                            "deviceId", testDeviceId
                    ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            logTestStep("实时监控停止完成");
        }

        // 2. 删除测试录像记录
        if (testRecordId != null) {
            mockMvc.perform(post("/api/smart/video/record/" + testRecordId + "/delete")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            logTestStep("测试录像记录删除完成");
        }

        // 3. 删除测试设备
        if (testDeviceId != null) {
            mockMvc.perform(post("/api/smart/video/device/" + testDeviceId + "/delete")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            logTestStep("测试设备删除完成");
        }

        logTestStep("测试数据清理完成");
    }
}