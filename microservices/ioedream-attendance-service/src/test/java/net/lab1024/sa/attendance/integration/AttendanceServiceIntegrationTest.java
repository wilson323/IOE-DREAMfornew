package net.lab1024.sa.attendance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.attendance.mobile.model.*;
import net.lab1024.sa.attendance.service.AttendanceMobileService;
import net.lab1024.sa.attendance.service.AttendanceRealtimeService;
import net.lab1024.sa.attendance.service.AttendanceRuleEngineService;
import net.lab1024.sa.attendance.service.AttendanceSchedulingService;
import net.lab1024.sa.attendance.service.AttendanceReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import jakarta.annotation.Resource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 考勤管理服务系统集成测试
 *
 * 完整测试考勤管理微服务的所有核心功能模块
 * 包括移动端API、实时计算、规则引擎、智能排班、报表统计等
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.redis.host=localhost",
    "spring.redis.port=6370",
    "spring.rabbitmq.host=localhost",
    "spring.rabbitmq.port=5673"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
@DisplayName("考勤管理服务系统集成测试")
public class AttendanceServiceIntegrationTest {

    @Resource
    private AttendanceMobileService attendanceMobileService;

    @Resource
    private AttendanceRealtimeService attendanceRealtimeService;

    @Resource
    private AttendanceRuleEngineService attendanceRuleEngineService;

    @Resource
    private AttendanceSchedulingService attendanceSchedulingService;

    @Resource
    private AttendanceReportService attendanceReportService;

    @Resource
    private ObjectMapper objectMapper;

    // 测试数据
    private static final Long TEST_USER_ID = 1001L;
    private static final String TEST_DEVICE_ID = "MOBILE_TEST_001";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        cleanupTestData();
    }

    /**
     * 测试移动端登录功能
     */
    @Test
    @Order(1)
    @DisplayName("测试移动端登录")
    void testMobileLogin() {
        // 准备登录请求
        MobileLoginRequest loginRequest = MobileLoginRequest.builder()
            .username(TEST_USERNAME)
            .password(TEST_PASSWORD)
            .deviceInfo(createTestDeviceInfo())
            .build();

        // 执行登录
        var response = attendanceMobileService.login(loginRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getToken());
        assertNotNull(response.getUserInfo());
        assertEquals(TEST_USER_ID, response.getUserInfo().getUserId());

        // 验证用户信息完整性
        MobileUserInfo userInfo = response.getUserInfo();
        assertNotNull(userInfo.getUsername());
        assertNotNull(userInfo.getRealName());
        assertNotNull(userInfo.getDepartmentName());
    }

    /**
     * 测试移动端打卡功能
     */
    @Test
    @Order(2)
    @DisplayName("测试移动端打卡")
    void testMobileClockIn() {
        // 先登录获取token
        String token = loginAndGetToken();

        // 准备打卡请求
        MobileClockInRequest clockInRequest = createTestClockInRequest();

        // 执行打卡
        var response = attendanceMobileService.clockIn(clockInRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getRecordId());
        assertNotNull(response.getClockInTime());
        assertEquals(0, response.getLateMinutes());

        // 验证打卡状态
        assertEquals("NORMAL", response.getAttendanceStatus());
    }

    /**
     * 测试生物识别验证
     */
    @Test
    @Order(3)
    @DisplayName("测试生物识别验证")
    void testBiometricVerification() {
        // 准备生物识别验证请求
        MobileBiometricVerifyRequest verifyRequest = createTestBiometricVerifyRequest();

        // 执行验证
        var response = attendanceMobileService.verifyBiometric(verifyRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getVerificationScore());
        assertEquals("FACE", response.getBiometricType());

        // 验证详细结果
        assertNotNull(response.getDetailedResult());
        assertTrue(response.getDetailedResult().getFeatureMatchScore() > 0.9);
    }

    /**
     * 测试考勤状态查询
     */
    @Test
    @Order(4)
    @DisplayName("测试考勤状态查询")
    void testAttendanceStatusQuery() {
        // 先执行打卡
        performTestClockIn();

        // 查询考勤状态
        MobileAttendanceStatusRequest statusRequest = MobileAttendanceStatusRequest.builder()
            .userId(TEST_USER_ID)
            .queryDate(LocalDate.now())
            .includeDetails(true)
            .includeStatistics(true)
            .build();

        var response = attendanceMobileService.getAttendanceStatus(statusRequest);

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getClockInTime());
        assertNotNull(response.getAttendanceStatus());
        assertNotNull(response.getShiftInfo());

        // 验证统计信息
        if (response.getStatistics() != null) {
            assertNotNull(response.getStatistics().getMonthlyAttendanceDays());
        }
    }

    /**
     * 测试实时计算引擎
     */
    @Test
    @Order(5)
    @DisplayName("测试实时计算引擎")
    void testRealtimeCalculationEngine() {
        // 创建实时计算事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("userId", TEST_USER_ID);
        eventData.put("eventType", "CLOCK_IN");
        eventData.put("timestamp", System.currentTimeMillis());
        eventData.put("deviceId", TEST_DEVICE_ID);

        // 执行实时计算
        var calculationResult = attendanceRealtimeService.processAttendanceEvent(eventData);

        // 验证计算结果
        assertNotNull(calculationResult);
        assertTrue(calculationResult.isSuccess());
        assertNotNull(calculationResult.getCalculatedData());

        // 验证计算数据
        Map<String, Object> data = calculationResult.getCalculatedData();
        assertNotNull(data.get("attendanceStatus"));
        assertNotNull(data.get("workHours"));
    }

    /**
     * 测试规则引擎
     */
    @Test
    @Order(6)
    @DisplayName("测试规则引擎")
    void testRuleEngine() {
        // 创建规则执行上下文
        Map<String, Object> context = new HashMap<>();
        context.put("userId", TEST_USER_ID);
        context.put("clockInTime", LocalDateTime.now());
        context.put("workShiftId", 1L);
        context.put("location", "OFFICE");

        // 执行规则引擎
        var ruleResult = attendanceRuleEngineService.executeRules(context);

        // 验证规则执行结果
        assertNotNull(ruleResult);
        assertTrue(ruleResult.isSuccess());

        // 验证规则结果
        Map<String, Object> ruleData = ruleResult.getResultData();
        assertNotNull(ruleData.get("isLate"));
        assertNotNull(ruleData.get("isValidLocation"));
    }

    /**
     * 测试智能排班
     */
    @Test
    @Order(7)
    @DisplayName("测试智能排班")
    void testIntelligentScheduling() {
        // 创建排班请求
        Map<String, Object> schedulingRequest = new HashMap<>();
        schedulingRequest.put("departmentId", 1L);
        schedulingRequest.put("startDate", LocalDate.now());
        schedulingRequest.put("endDate", LocalDate.now().plusDays(7));
        schedulingRequest.put("algorithm", "GENETIC");

        // 执行智能排班
        var scheduleResult = attendanceSchedulingService.generateSchedule(schedulingRequest);

        // 验证排班结果
        assertNotNull(scheduleResult);
        assertTrue(scheduleResult.isSuccess());
        assertNotNull(scheduleResult.getScheduleData());

        // 验证排班数据
        Map<String, Object> scheduleData = scheduleResult.getScheduleData();
        assertNotNull(scheduleData.get("schedules"));
        assertTrue(((List<?>) scheduleData.get("schedules")).size() > 0);
    }

    /**
     * 测试报表统计
     */
    @Test
    @Order(8)
    @DisplayName("测试报表统计")
    void testReportStatistics() {
        // 准备报表查询参数
        Map<String, Object> reportParams = new HashMap<>();
        reportParams.put("userId", TEST_USER_ID);
        reportParams.put("startDate", LocalDate.now().minusDays(30));
        reportParams.put("endDate", LocalDate.now());
        reportParams.put("reportType", "ATTENDANCE_SUMMARY");

        // 生成报表
        var reportResult = attendanceReportService.generateReport(reportParams);

        // 验证报表结果
        assertNotNull(reportResult);
        assertTrue(reportResult.isSuccess());
        assertNotNull(reportResult.getReportData());

        // 验证报表数据
        Map<String, Object> reportData = reportResult.getReportData();
        assertNotNull(reportData.get("totalDays"));
        assertNotNull(reportData.get("attendanceRate"));
        assertNotNull(reportData.get("lateCount"));
    }

    /**
     * 测试并发场景
     */
    @Test
    @Order(9)
    @DisplayName("测试并发打卡场景")
    void testConcurrentClockIn() {
        int threadCount = 10;
        List<Thread> threads = new ArrayList<>();
        List<Boolean> results = new ArrayList<>();

        // 创建并发打卡线程
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    MobileClockInRequest request = createTestClockInRequest();
                    request.setDeviceId(TEST_DEVICE_ID + "_" + threadId);

                    var response = attendanceMobileService.clockIn(request);
                    results.add(response.isSuccess());
                } catch (Exception e) {
                    results.add(false);
                }
            });
            threads.add(thread);
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join(5000); // 最多等待5秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 验证并发结果
        assertEquals(threadCount, results.size());
        assertTrue(results.stream().anyMatch(Boolean::booleanValue));
    }

    /**
     * 测试数据一致性
     */
    @Test
    @Order(10)
    @DisplayName("测试数据一致性")
    void testDataConsistency() {
        // 执行打卡
        var clockInResponse = performTestClockIn();
        assertTrue(clockInResponse.isSuccess());

        // 验证数据在不同服务中的一致性
        var statusResponse = attendanceMobileService.getAttendanceStatus(
            MobileAttendanceStatusRequest.builder()
                .userId(TEST_USER_ID)
                .queryDate(LocalDate.now())
                .build()
        );

        // 验证打卡记录ID一致性
        assertEquals(clockInResponse.getRecordId(), statusResponse.getAttendanceRecords()
            .stream()
            .filter(r -> "IN".equals(r.getClockType()))
            .findFirst()
            .map(AttendanceRecord::getRecordId)
            .orElse(null));

        // 验证时间一致性
        assertEquals(clockInResponse.getClockInTime(), statusResponse.getClockInTime());
    }

    /**
     * 测试错误处理
     */
    @Test
    @Order(11)
    @DisplayName("测试错误处理")
    void testErrorHandling() {
        // 测试无效用户登录
        MobileLoginRequest invalidLoginRequest = MobileLoginRequest.builder()
            .username("invaliduser")
            .password("invalidpass")
            .build();

        var loginResponse = attendanceMobileService.login(invalidLoginRequest);
        assertFalse(loginResponse.isSuccess());
        assertNotNull(loginResponse.getErrorCode());

        // 测试重复打卡
        performTestClockIn(); // 第一次打卡
        var secondClockInResponse = performTestClockIn(); // 第二次打卡
        // 根据业务规则，可能允许或拒绝重复打卡

        // 测试无效生物识别数据
        MobileBiometricVerifyRequest invalidBiometricRequest = MobileBiometricVerifyRequest.builder()
            .userId(TEST_USER_ID)
            .biometricType("FACE")
            .build(); // 缺少必要的生物识别数据

        var biometricResponse = attendanceMobileService.verifyBiometric(invalidBiometricRequest);
        assertFalse(biometricResponse.isSuccess());
        assertNotNull(biometricResponse.getErrorCode());
    }

    /**
     * 测试性能指标
     */
    @Test
    @Order(12)
    @DisplayName("测试性能指标")
    void testPerformanceMetrics() {
        int iterationCount = 100;
        long startTime = System.currentTimeMillis();

        // 执行多次打卡操作
        for (int i = 0; i < iterationCount; i++) {
            MobileClockInRequest request = createTestClockInRequest();
            request.setDeviceId(TEST_DEVICE_ID + "_" + i);

            var response = attendanceMobileService.clockIn(request);
            assertNotNull(response);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / iterationCount;

        // 性能断言：平均响应时间应小于500ms
        assertTrue(avgTime < 500, "平均响应时间过长: " + avgTime + "ms");

        // 总时间应在合理范围内
        assertTrue(totalTime < 30000, "总执行时间过长: " + totalTime + "ms");
    }

    // ========== 辅助方法 ==========

    /**
     * 设置测试数据
     */
    private void setupTestData() {
        // 初始化测试用户、部门、班次等基础数据
        // 这里可以使用@MockBean或者实际的数据库初始化
    }

    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        // 清理测试过程中产生的数据
    }

    /**
     * 创建测试设备信息
     */
    private MobileLoginRequest.DeviceInfo createTestDeviceInfo() {
        MobileLoginRequest.DeviceInfo.Location location = MobileLoginRequest.DeviceInfo.Location.builder()
            .latitude(39.9042)
            .longitude(116.4074)
            .address("北京市朝阳区")
            .accuracy(10.5)
            .build();

        return MobileLoginRequest.DeviceInfo.builder()
            .deviceId(TEST_DEVICE_ID)
            .deviceType("ANDROID")
            .deviceModel("Test Device")
            .osVersion("Android 11")
            .appVersion("1.0.0")
            .pushToken("test_push_token")
            .ipAddress("127.0.0.1")
            .location(location)
            .build();
    }

    /**
     * 创建测试打卡请求
     */
    private MobileClockInRequest createTestClockInRequest() {
        MobileClockInRequest.LocationInfo location = MobileClockInRequest.LocationInfo.builder()
            .latitude(new BigDecimal("39.9042"))
            .longitude(new BigDecimal("116.4074"))
            .accuracy(10.5)
            .altitude(50.0)
            .address("北京市朝阳区建国门外大街")
            .locationSource("GPS")
            .geofenceId("GEOFENCE_001")
            .geofenceName("办公区域")
            .withinGeofence(true)
            .build();

        MobileClockInRequest.BiometricData.FaceData faceData = MobileClockInRequest.BiometricData.FaceData.builder()
            .featureVector("test_face_features")
            .faceImage("test_face_image_base64")
            .matchScore(0.98)
            .livenessScore(0.99)
            .qualityScore(0.95)
            .build();

        MobileClockInRequest.BiometricData biometricData = MobileClockInRequest.BiometricData.builder()
            .faceData(faceData)
            .biometricType("FACE")
            .confidence(0.98)
            .livenessDetected(true)
            .build();

        return MobileClockInRequest.builder()
            .userId(TEST_USER_ID)
            .deviceId(TEST_DEVICE_ID)
            .deviceType("MOBILE")
            .attendanceType("IN")
            .location(location)
            .biometricData(biometricData)
            .clockTime(LocalDateTime.now())
            .remark("测试打卡")
            .extendedAttributes(new HashMap<>())
            .build();
    }

    /**
     * 创建测试生物识别验证请求
     */
    private MobileBiometricVerifyRequest createTestBiometricVerifyRequest() {
        MobileBiometricVerifyRequest.FaceData faceData = MobileBiometricVerifyRequest.FaceData.builder()
            .faceImage("test_face_image_base64")
            .faceFeatures("test_face_features")
            .faceBoundingBox("100,100,200,200")
            .faceLandmarks("test_landmarks")
            .faceAngle(0.0)
            .faceSize("200x200")
            .lightingCondition("GOOD")
            .isFrontal(true)
            .eyeState("OPEN")
            .mouthState("CLOSED")
            .build();

        MobileBiometricVerifyRequest.BiometricData biometricData = MobileBiometricVerifyRequest.BiometricData.builder()
            .faceData(faceData)
            .featureVector("test_feature_vector")
            .rawData("test_raw_data_base64")
            .qualityScore(0.95)
            .captureTimestamp(System.currentTimeMillis())
            .build();

        return MobileBiometricVerifyRequest.builder()
            .userId(TEST_USER_ID)
            .biometricType("FACE")
            .biometricData(biometricData)
            .verificationScenario("CLOCK_IN")
            .deviceId(TEST_DEVICE_ID)
            .verificationThreshold(0.85)
            .enableLivenessCheck(true)
            .enableAntiSpoofing(true)
            .extendedAttributes(new HashMap<>())
            .build();
    }

    /**
     * 登录并获取token
     */
    private String loginAndGetToken() {
        MobileLoginRequest loginRequest = MobileLoginRequest.builder()
            .username(TEST_USERNAME)
            .password(TEST_PASSWORD)
            .deviceInfo(createTestDeviceInfo())
            .build();

        var response = attendanceMobileService.login(loginRequest);
        return response.getToken();
    }

    /**
     * 执行测试打卡
     */
    private MobileClockInResult performTestClockIn() {
        MobileClockInRequest clockInRequest = createTestClockInRequest();
        return attendanceMobileService.clockIn(clockInRequest);
    }
}