/*
 * 视频模块微服务化集成测试套件
 * 基于现有项目代码改造的视频模块完整功能测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-11-27
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.smart.video.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
 * 视频模块微服务化集成测试套件
 * 基于现有视频模块代码的完整功能验证测试
 *
 * 测试范围：
 * 1. 视频设备管理功能
 * 2. 实时视频监控功能
 * 3. 历史录像回放功能
 * 4. AI分析引擎功能
 * 5. 数据一致性和性能验证
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("视频模块微服务化集成测试")
class VideoModuleSystemIntegrationTest extends BaseTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        logTestStart("视频模块微服务化集成测试");
    }

    @Test
    @Order(1)
    @DisplayName("测试视频设备管理完整流程")
    void testVideoDeviceManagementFlow() throws Exception {
        logTestStep("开始测试视频设备管理流程");

        // 1. 添加IP摄像头设备
        VideoDeviceAddForm ipCamera = VideoDeviceAddForm.builder()
                .deviceName("主楼入口IP摄像机")
                .deviceCode("MAIN_ENTRANCE_IP_001")
                .deviceType("IP_CAMERA")
                .manufacturer("Hikvision")
                .model("DS-2CD2143G2-I")
                .serialNumber("HK20250127001")
                .ipAddress("192.168.1.100")
                .port(554)
                .username("admin")
                .password("admin123")
                .rtspUrl("rtsp://192.168.1.100:554/Streaming/Channels/101")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/video/device/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ipCamera)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        logTestStep("IP摄像头设备创建完成");

        // 2. 添加NVR设备
        VideoDeviceAddForm nvr = VideoDeviceAddForm.builder()
                .deviceName("主楼NVR录像机")
                .deviceCode("MAIN_BUILDING_NVR_001")
                .deviceType("NVR")
                .manufacturer("Hikvision")
                .model("DS-7816N-K2/16P")
                .serialNumber("NVR20250127001")
                .ipAddress("192.168.1.200")
                .port(8000)
                .username("admin")
                .password("nvr123456")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/video/device/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nvr)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        logTestStep("NVR设备创建完成");

        // 3. 验证设备列表查询
        mockMvc.perform(post("/api/smart/video/device/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        PageParam.builder()
                                .pageNum(1)
                                .pageSize(20)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2));

        logTestStep("设备列表查询验证完成");
    }

    @Test
    @Order(2)
    @DisplayName("测试实时视频监控功能")
    void testRealTimeVideoSurveillanceFlow() throws Exception {
        logTestStep("开始测试实时视频监控功能");

        // 1. 获取设备实时流地址
        mockMvc.perform(get("/api/smart/video/surveillance/realtime/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("实时视频流地址获取完成");

        // 2. 启动实时监控
        mockMvc.perform(post("/api/smart/video/surveillance/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", 1L,
                        "streamType", "MAIN"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        logTestStep("实时监控启动完成");

        // 3. 获取监控状态
        mockMvc.perform(get("/api/smart/video/surveillance/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.status").exists());

        logTestStep("监控状态查询完成");

        // 4. 停止监控
        mockMvc.perform(post("/api/smart/video/surveillance/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", 1L
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        logTestStep("监控停止完成");
    }

    @Test
    @Order(3)
    @DisplayName("测试历史录像回放功能")
    void testVideoPlaybackFlow() throws Exception {
        logTestStep("开始测试历史录像回放功能");

        // 1. 查询录像列表
        mockMvc.perform(post("/api/smart/video/playback/record/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", 1L,
                        "startTime", "2025-11-27 00:00:00",
                        "endTime", "2025-11-27 23:59:59",
                        "pageNum", 1,
                        "pageSize", 20
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("录像列表查询完成");

        // 2. 获取回放地址
        mockMvc.perform(get("/api/smart/video/playback/url/1")
                .param("startTime", "2025-11-27 10:00:00")
                .param("endTime", "2025-11-27 10:10:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("回放地址获取完成");

        // 3. 开始回放
        mockMvc.perform(post("/api/smart/video/playback/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "recordId", 1L,
                        "startTime", "2025-11-27 10:00:00"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        logTestStep("回放启动完成");

        // 4. 录制回放日志
        mockMvc.perform(post("/api/smart/video/playback/log")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", 1L,
                        "userId", 1L,
                        "playTime", "2025-11-27 15:30:00",
                        "duration", 600
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        logTestStep("回放日志记录完成");
    }

    @Test
    @Order(4)
    @DisplayName("测试AI分析引擎功能")
    void testAIAnalysisEngineFlow() throws Exception {
        logTestStep("开始测试AI分析引擎功能");

        // 1. 人脸检测分析
        mockMvc.perform(post("/api/smart/video/ai/face-detection")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", 1L,
                        "imageUrl", "http://example.com/face_image.jpg",
                        "confidence", 0.8
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("人脸检测分析完成");

        // 2. 人脸搜索
        FaceSearchForm faceSearch = FaceSearchForm.builder()
                .faceImage("http://example.com/search_face.jpg")
                .deviceId(1L)
                .confidence(0.75)
                .maxResults(10)
                .build();

        mockMvc.perform(post("/api/smart/video/ai/face-search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(faceSearch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("人脸搜索完成");

        // 3. 行为分析
        mockMvc.perform(post("/api/smart/video/ai/behavior-analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", 1L,
                        "videoUrl", "http://example.com/behavior_video.mp4",
                        "analysisTypes", Arrays.asList("WALKING", "RUNNING", "FALLING"),
                        "startTime", "2025-11-27 10:00:00",
                        "endTime", "2025-11-27 10:05:00"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("行为分析完成");

        // 4. 轨迹分析
        mockMvc.perform(post("/api/smart/video/ai/trajectory-analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "targetId", "PERSON_001",
                        "deviceIds", Arrays.asList(1L, 2L),
                        "startTime", "2025-11-27 09:00:00",
                        "endTime", "2025-11-27 18:00:00"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("轨迹分析完成");
    }

    @Test
    @Order(5)
    @DisplayName("测试数据一致性和完整性")
    void testDataConsistencyAndIntegrity() throws Exception {
        logTestStep("开始测试数据一致性和完整性");

        // 1. 验证设备状态同步
        mockMvc.perform(get("/api/smart/video/device/status/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        logTestStep("设备状态同步验证完成");

        // 2. 验证录像数据完整性
        mockMvc.perform(post("/api/smart/video/playback/record/integrity-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", 1L,
                        "checkDate", "2025-11-27"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("录像数据完整性检查完成");

        // 3. 验证AI分析结果统计
        mockMvc.perform(get("/api/smart/video/ai/statistics")
                .param("deviceId", "1")
                .param("startDate", "2025-11-27")
                .param("endDate", "2025-11-27"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("AI分析结果统计验证完成");
    }

    @Test
    @Order(6)
    @DisplayName("测试并发操作和数据隔离")
    void testConcurrentOperationsAndDataIsolation() throws Exception {
        logTestStep("开始测试并发操作和数据隔离");

        // 模拟多个用户同时进行视频分析
        CompletableFuture<Void> faceAnalysisTask = CompletableFuture.runAsync(() -> {
            try {
                mockMvc.perform(post("/api/smart/video/ai/face-detection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", 1L,
                                "imageUrl", "http://example.com/concurrent_face_1.jpg",
                                "confidence", 0.8
                        ))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<Void> behaviorAnalysisTask = CompletableFuture.runAsync(() -> {
            try {
                mockMvc.perform(post("/api/smart/video/ai/behavior-analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", 2L,
                                "videoUrl", "http://example.com/concurrent_behavior_1.mp4",
                                "analysisTypes", Arrays.asList("WALKING", "RUNNING")
                        ))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<Void> playbackTask = CompletableFuture.runAsync(() -> {
            try {
                mockMvc.perform(post("/api/smart/video/playback/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "recordId", 1L,
                                "startTime", "2025-11-27 12:00:00"
                        ))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // 等待所有并发操作完成
        CompletableFuture.allOf(faceAnalysisTask, behaviorAnalysisTask, playbackTask)
                .get(30, TimeUnit.SECONDS);

        logTestStep("并发操作测试完成");

        // 验证数据隔离
        mockMvc.perform(get("/api/smart/video/ai/results")
                .param("deviceId", "1")
                .param("pageNum", "1")
                .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("数据隔离验证完成");
    }

    @Test
    @Order(7)
    @DisplayName("测试系统性能和响应时间")
    void testSystemPerformanceAndResponseTime() throws Exception {
        logTestStep("开始测试系统性能和响应时间");

        // 1. 测试设备查询性能
        long startTime = System.currentTimeMillis();
        mockMvc.perform(post("/api/smart/video/device/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        PageParam.builder()
                                .pageNum(1)
                                .pageSize(20)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        long deviceQueryTime = System.currentTimeMillis() - startTime;

        logTestStep("设备查询性能测试完成，耗时: " + deviceQueryTime + "ms");

        // 2. 测试人脸搜索性能
        startTime = System.currentTimeMillis();
        mockMvc.perform(post("/api/smart/video/ai/face-search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(FaceSearchForm.builder()
                        .faceImage("http://example.com/perf_test_face.jpg")
                        .confidence(0.75)
                        .maxResults(10)
                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        long faceSearchTime = System.currentTimeMillis() - startTime;

        logTestStep("人脸搜索性能测试完成，耗时: " + faceSearchTime + "ms");

        // 3. 测试录像查询性能
        startTime = System.currentTimeMillis();
        mockMvc.perform(post("/api/smart/video/playback/record/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", 1L,
                        "startTime", "2025-11-27 00:00:00",
                        "endTime", "2025-11-27 23:59:59",
                        "pageNum", 1,
                        "pageSize", 20
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        long recordQueryTime = System.currentTimeMillis() - startTime;

        logTestStep("录像查询性能测试完成，耗时: " + recordQueryTime + "ms");

        // 验证性能要求（根据实际需求调整阈值）
        assertTrue("设备查询时间过长: " + deviceQueryTime + "ms", deviceQueryTime < 3000);
        assertTrue("人脸搜索时间过长: " + faceSearchTime + "ms", faceSearchTime < 5000);
        assertTrue("录像查询时间过长: " + recordQueryTime + "ms", recordQueryTime < 2000);

        logTestStep("性能验证完成");
    }

    @Test
    @Order(8)
    @DisplayName("测试错误处理和异常恢复")
    void testErrorHandlingAndExceptionRecovery() throws Exception {
        logTestStep("开始测试错误处理和异常恢复");

        // 1. 测试无效设备ID的错误处理
        mockMvc.perform(get("/api/smart/video/device/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").exists());

        logTestStep("无效设备ID错误处理验证完成");

        // 2. 测试重复设备编码的错误处理
        VideoDeviceAddForm duplicateDevice = VideoDeviceAddForm.builder()
                .deviceName("重复编码设备")
                .deviceCode("MAIN_ENTRANCE_IP_001") // 使用已存在的编码
                .deviceType("IP_CAMERA")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/video/device/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateDevice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").exists());

        logTestStep("重复设备编码错误处理验证完成");

        // 3. 测试无效RTSP地址的错误处理
        VideoDeviceAddForm invalidRtspDevice = VideoDeviceAddForm.builder()
                .deviceName("无效RTSP设备")
                .deviceCode("INVALID_RTSP_001")
                .deviceType("IP_CAMERA")
                .ipAddress("192.168.1.999") // 无效IP
                .port(554)
                .rtspUrl("invalid_rtsp_url") // 无效URL
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/video/device/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRtspDevice)))
                .andExpect(status().isOk());

        logTestStep("无效RTSP地址处理验证完成");

        // 4. 测试AI分析参数验证
        mockMvc.perform(post("/api/smart/video/ai/face-detection")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "deviceId", null, // 缺少必需参数
                        "imageUrl", "http://example.com/test.jpg"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").exists());

        logTestStep("AI分析参数验证完成");
    }

    @Test
    @Order(9)
    @DisplayName("测试系统完整性和集成验证")
    void testSystemIntegrityAndIntegrationValidation() throws Exception {
        logTestStep("开始测试系统完整性和集成验证");

        // 1. 验证所有API端点都正常响应
        String[] endpoints = {
                "/api/smart/video/device/query",
                "/api/smart/video/surveillance/status/1",
                "/api/smart/video/playback/record/query",
                "/api/smart/video/ai/statistics"
        };

        for (String endpoint : endpoints) {
            if (endpoint.contains("query")) {
                mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PageParam.builder().build())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            } else if (endpoint.contains("statistics")) {
                mockMvc.perform(get(endpoint))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            } else {
                mockMvc.perform(get(endpoint))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            }
        }

        logTestStep("API端点完整性验证完成");

        // 2. 验证微服务架构一致性
        mockMvc.perform(get("/api/smart/video/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"));

        logTestStep("微服务健康检查完成");

        // 3. 验证数据流转完整性
        mockMvc.perform(post("/api/smart/video/integration/data-flow-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "checkType", "FULL_FLOW",
                        "deviceId", 1L
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        logTestStep("数据流转完整性验证完成");
    }
}