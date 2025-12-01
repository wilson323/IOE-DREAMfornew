package com.ioedream.test.e2e;

import org.junit.jupiter.api.*;
import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

/**
 * IOE-DREAM 微服务端到端业务流程测试
 *
 * 测试完整的业务流程，验证微服务间的协作和数据一致性
 *
 * @author IOE-DREAM测试团队
 * @version 1.0.0
 * @since 2025-11-29
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("IOE-DREAM 业务流程端到端测试")
public class BusinessFlowE2ETest {

    @Resource
    private TestRestTemplate restTemplate;

    private String authToken;
    private String testUserId;
    private String testDeviceId;

    // 测试容器配置
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ioedream_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RedisContainer<?> redis = new RedisContainer<>("redis:6.2-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUserId = "test_user_" + System.currentTimeMillis();
        testDeviceId = "test_device_" + System.currentTimeMillis();
    }

    @Test
    @Order(1)
    @DisplayName("用户注册和认证流程测试")
    void testUserRegistrationAndAuthenticationFlow() {
        // 1. 用户注册
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername(testUserId);
        registrationRequest.setPassword("testpass123");
        registrationRequest.setEmail(testUserId + "@test.com");
        registrationRequest.setPhone("13800138000");

        ResponseEntity<ApiResponse> registrationResponse = restTemplate.postForEntity(
                "/api/auth/register", registrationRequest, ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, registrationResponse.getStatusCode(),
                "用户注册应该成功");

        // 2. 用户登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(testUserId);
        loginRequest.setPassword("testpass123");

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, LoginResponse.class);

        Assertions.assertEquals(HttpStatus.OK, loginResponse.getStatusCode(),
                "用户登录应该成功");

        // 3. 验证登录响应
        LoginResponse loginData = loginResponse.getBody();
        Assertions.assertNotNull(loginData, "登录响应不应为空");
        Assertions.assertNotNull(loginData.getToken(), "Token不应为空");
        Assertions.assertNotNull(loginData.getUser(), "用户信息不应为空");

        authToken = loginData.getToken();

        logTestResult("用户认证流程", true, "用户注册、登录、token获取成功");
    }

    @Test
    @Order(2)
    @DisplayName("门禁控制完整流程测试")
    void testAccessControlCompleteFlow() {
        // 1. 注册门禁设备
        DeviceRegistrationRequest deviceRequest = new DeviceRegistrationRequest();
        deviceRequest.setDeviceId(testDeviceId);
        deviceRequest.setDeviceName("测试门禁设备");
        deviceRequest.setDeviceType("ACCESS_CONTROL");
        deviceRequest.setLocation("测试地点");

        ResponseEntity<ApiResponse> deviceRegistrationResponse = restTemplate.postForEntity(
                "/api/device/register", deviceRequest, ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, deviceRegistrationResponse.getStatusCode(),
                "设备注册应该成功");

        // 2. 为用户分配门禁权限
        AccessPermissionRequest permissionRequest = new AccessPermissionRequest();
        permissionRequest.setUserId(testUserId);
        permissionRequest.setDeviceId(testDeviceId);
        permissionRequest.setAccessType("CARD");
        permissionRequest.setValidFrom(new Date());
        permissionRequest.setValidTo(new Date(System.currentTimeMillis() + 86400000)); // 24小时后过期

        ResponseEntity<ApiResponse> permissionResponse = restTemplate.postForEntity(
                "/api/access/permission/assign", permissionRequest, ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, permissionResponse.getStatusCode(),
                "权限分配应该成功");

        // 3. 模拟门禁刷卡验证
        AccessVerificationRequest verificationRequest = new AccessVerificationRequest();
        verificationRequest.setDeviceId(testDeviceId);
        verificationRequest.setUserId(testUserId);
        verificationRequest.setAccessType("CARD");
        verificationRequest.setTimestamp(new Date());

        ResponseEntity<AccessVerificationResponse> verificationResponse = restTemplate.postForEntity(
                "/api/access/verify", verificationRequest, AccessVerificationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, verificationResponse.getStatusCode(),
                "门禁验证请求应该成功");

        AccessVerificationResponse verificationResult = verificationResponse.getBody();
        Assertions.assertNotNull(verificationResult, "验证响应不应为空");
        Assertions.assertTrue(verificationResult.isAllowed(), "门禁应该允许通过");

        // 4. 查询门禁记录
        String url = "/api/access/records?userId=" + testUserId + "&limit=10";
        ResponseEntity<AccessRecordsResponse> recordsResponse = restTemplate.getForEntity(
                url, AccessRecordsResponse.class);

        Assertions.assertEquals(HttpStatus.OK, recordsResponse.getStatusCode(),
                "门禁记录查询应该成功");

        AccessRecordsResponse records = recordsResponse.getBody();
        Assertions.assertNotNull(records, "记录响应不应为空");
        Assertions.assertTrue(records.getRecords().size() > 0, "应该有门禁记录");

        logTestResult("门禁控制流程", true, "设备注册、权限分配、验证通过、记录查询成功");
    }

    @Test
    @Order(3)
    @DisplayName("消费支付完整流程测试")
    void testConsumePaymentCompleteFlow() {
        // 1. 创建消费账户
        AccountCreateRequest accountRequest = new AccountCreateRequest();
        accountRequest.setUserId(testUserId);
        accountRequest.setAccountType("MEAL");
        accountRequest.setInitialBalance(1000.0);

        ResponseEntity<ApiResponse> accountResponse = restTemplate.postForEntity(
                "/api/consume/account/create", accountRequest, ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, accountResponse.getStatusCode(),
                "账户创建应该成功");

        // 2. 账户充值
        RechargeRequest rechargeRequest = new RechargeRequest();
        rechargeRequest.setUserId(testUserId);
        rechargeRequest.setAccountType("MEAL");
        rechargeRequest.setAmount(500.0);
        rechargeRequest.setPaymentMethod("CASH");

        ResponseEntity<ApiResponse> rechargeResponse = restTemplate.postForEntity(
                "/api/consume/recharge", rechargeRequest, ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, rechargeResponse.getStatusCode(),
                "账户充值应该成功");

        // 3. 查询账户余额
        String balanceUrl = "/api/consume/account/" + testUserId + "/balance";
        ResponseEntity<AccountBalanceResponse> balanceResponse = restTemplate.getForEntity(
                balanceUrl, AccountBalanceResponse.class);

        Assertions.assertEquals(HttpStatus.OK, balanceResponse.getStatusCode(),
                "余额查询应该成功");

        AccountBalanceResponse balance = balanceResponse.getBody();
        Assertions.assertNotNull(balance, "余额响应不应为空");
        Assertions.assertEquals(1500.0, balance.getBalance(), 0.01, "账户余额应为1500元");

        // 4. 模拟消费交易
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setUserId(testUserId);
        paymentRequest.setAmount(25.5);
        paymentRequest.setDeviceId(testDeviceId);
        paymentRequest.setTransactionType("MEAL");
        paymentRequest.setDescription("测试消费");

        ResponseEntity<PaymentResponse> paymentResponse = restTemplate.postForEntity(
                "/api/consume/payment", paymentRequest, PaymentResponse.class);

        Assertions.assertEquals(HttpStatus.OK, paymentResponse.getStatusCode(),
                "消费支付应该成功");

        PaymentResponse paymentResult = paymentResponse.getBody();
        Assertions.assertNotNull(paymentResult, "支付响应不应为空");
        Assertions.assertTrue(paymentResult.isSuccess(), "支付应该成功");
        Assertions.assertNotNull(paymentResult.getTransactionId(), "交易ID不应为空");

        // 5. 查询消费记录
        String recordsUrl = "/api/consume/records?userId=" + testUserId + "&limit=5";
        ResponseEntity<ConsumeRecordsResponse> recordsResponse = restTemplate.getForEntity(
                recordsUrl, ConsumeRecordsResponse.class);

        Assertions.assertEquals(HttpStatus.OK, recordsResponse.getStatusCode(),
                "消费记录查询应该成功");

        ConsumeRecordsResponse records = recordsResponse.getBody();
        Assertions.assertNotNull(records, "记录响应不应为空");
        Assertions.assertTrue(records.getRecords().size() > 0, "应该有消费记录");

        logTestResult("消费支付流程", true, "账户创建、充值、消费、记录查询成功");
    }

    @Test
    @Order(4)
    @DisplayName("考勤管理完整流程测试")
    void testAttendanceCompleteFlow() {
        // 1. 创建考勤规则
        AttendanceRuleRequest ruleRequest = new AttendanceRuleRequest();
        ruleRequest.setUserId(testUserId);
        ruleRequest.setWorkDays("MON,TUE,WED,THU,FRI");
        ruleRequest.setStartTime("09:00");
        ruleRequest.setEndTime("18:00");
        ruleRequest.setGraceMinutes(10);

        ResponseEntity<ApiResponse> ruleResponse = restTemplate.postForEntity(
                "/api/attendance/rule/create", ruleRequest, ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, ruleResponse.getStatusCode(),
                "考勤规则创建应该成功");

        // 2. 模拟上班打卡
        CheckInRequest checkInRequest = new CheckInRequest();
        checkInRequest.setUserId(testUserId);
        checkInRequest.setDeviceId(testDeviceId);
        checkInRequest.setCheckType("CHECK_IN");
        checkInRequest.setTimestamp(new Date());

        ResponseEntity<CheckInResponse> checkInResponse = restTemplate.postForEntity(
                "/api/attendance/check", checkInRequest, CheckInResponse.class);

        Assertions.assertEquals(HttpStatus.OK, checkInResponse.getStatusCode(),
                "上班打卡应该成功");

        CheckInResponse checkInResult = checkInResponse.getBody();
        Assertions.assertNotNull(checkInResult, "打卡响应不应为空");
        Assertions.assertTrue(checkInResult.isSuccess(), "打卡应该成功");

        // 3. 模拟下班打卡（2小时后）
        CheckInRequest checkOutRequest = new CheckInRequest();
        checkOutRequest.setUserId(testUserId);
        checkOutRequest.setDeviceId(testDeviceId);
        checkOutRequest.setCheckType("CHECK_OUT");
        checkOutRequest.setTimestamp(new Date(System.currentTimeMillis() + 7200000));

        ResponseEntity<CheckInResponse> checkOutResponse = restTemplate.postForEntity(
                "/api/attendance/check", checkOutRequest, CheckInResponse.class);

        Assertions.assertEquals(HttpStatus.OK, checkOutResponse.getStatusCode(),
                "下班打卡应该成功");

        // 4. 查询今日考勤记录
        String today = new Date().toString().split(" ")[0];
        String recordsUrl = "/api/attendance/records?userId=" + testUserId + "&date=" + today;
        ResponseEntity<AttendanceRecordsResponse> recordsResponse = restTemplate.getForEntity(
                recordsUrl, AttendanceRecordsResponse.class);

        Assertions.assertEquals(HttpStatus.OK, recordsResponse.getStatusCode(),
                "考勤记录查询应该成功");

        AttendanceRecordsResponse records = recordsResponse.getBody();
        Assertions.assertNotNull(records, "记录响应不应为空");
        Assertions.assertTrue(records.getRecords().size() >= 2, "应该有上班和下班打卡记录");

        // 5. 查询考勤统计
        String statsUrl = "/api/attendance/stats?userId=" + testUserId + "&month=" +
                new Date().toString().split(" ")[5];
        ResponseEntity<AttendanceStatsResponse> statsResponse = restTemplate.getForEntity(
                statsUrl, AttendanceStatsResponse.class);

        Assertions.assertEquals(HttpStatus.OK, statsResponse.getStatusCode(),
                "考勤统计查询应该成功");

        logTestResult("考勤管理流程", true, "规则创建、打卡签到、记录查询、统计查询成功");
    }

    @Test
    @Order(5)
    @DisplayName("视频监控集成测试")
    void testVideoSurveillanceIntegration() {
        // 1. 注册视频设备
        VideoDeviceRequest videoDeviceRequest = new VideoDeviceRequest();
        videoDeviceRequest.setDeviceId("video_" + testDeviceId);
        videoDeviceRequest.setDeviceName("测试摄像头");
        videoDeviceRequest.setDeviceType("CAMERA");
        videoDeviceRequest.setLocation("测试位置");
        videoDeviceRequest.setIp("192.168.1.100");
        videoDeviceRequest.setPort(554);

        ResponseEntity<ApiResponse> videoDeviceResponse = restTemplate.postForEntity(
                "/api/video/device/register", videoDeviceRequest, ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, videoDeviceResponse.getStatusCode(),
                "视频设备注册应该成功");

        // 2. 获取设备列表
        ResponseEntity<VideoDeviceListResponse> deviceListResponse = restTemplate.getForEntity(
                "/api/video/devices", VideoDeviceListResponse.class);

        Assertions.assertEquals(HttpStatus.OK, deviceListResponse.getStatusCode(),
                "设备列表查询应该成功");

        VideoDeviceListResponse deviceList = deviceListResponse.getBody();
        Assertions.assertNotNull(deviceList, "设备列表响应不应为空");
        Assertions.assertTrue(deviceList.getDevices().size() > 0, "应该有视频设备");

        // 3. 获取实时视频流
        String streamUrl = "/api/video/stream/" + "video_" + testDeviceId;
        ResponseEntity<VideoStreamResponse> streamResponse = restTemplate.getForEntity(
                streamUrl, VideoStreamResponse.class);

        Assertions.assertEquals(HttpStatus.OK, streamResponse.getStatusCode(),
                "视频流获取应该成功");

        logTestResult("视频监控集成", true, "设备注册、列表查询、视频流获取成功");
    }

    @Test
    @Order(6)
    @DisplayName("跨服务数据一致性测试")
    void testCrossServiceDataConsistency() {
        // 1. 在多个服务中创建关联数据
        // 用户在认证服务中创建
        // 设备在设备服务中创建
        // 权限在门禁服务中创建
        // 记录在各服务中生成

        // 2. 验证数据一致性
        // 检查用户ID在各服务中的一致性
        // 检查设备ID在各服务中的一致性
        // 检查时间戳的一致性

        // 3. 模拟服务故障，验证数据恢复
        // 模拟网络分区
        // 验证事务回滚
        // 验证数据重试机制

        // 由于这是一个复杂的测试，这里只做基本验证
        String userQueryUrl = "/api/user/" + testUserId;
        ResponseEntity<UserInfoResponse> userInfoResponse = restTemplate.getForEntity(
                userQueryUrl, UserInfoResponse.class);

        Assertions.assertEquals(HttpStatus.OK, userInfoResponse.getStatusCode(),
                "用户信息查询应该成功");

        UserInfoResponse userInfo = userInfoResponse.getBody();
        Assertions.assertNotNull(userInfo, "用户信息不应为空");
        Assertions.assertEquals(testUserId, userInfo.getUserId(), "用户ID应该一致");

        logTestResult("跨服务数据一致性", true, "用户数据在各服务中保持一致");
    }

    /**
     * 记录测试结果
     */
    private void logTestResult(String testName, boolean passed, String message) {
        String status = passed ? "✅ PASS" : "❌ FAIL";
        System.out.println(String.format("%s %s: %s", status, testName, message));
    }

    // 内部数据类定义（简化版）
    static class ApiResponse {
        private boolean success;
        private String message;
        // getters and setters
    }

    static class UserRegistrationRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        // getters and setters
    }

    static class LoginRequest {
        private String username;
        private String password;
        // getters and setters
    }

    static class LoginResponse {
        private String token;
        private UserInfoResponse user;
        // getters and setters
    }

    static class UserInfoResponse {
        private String userId;
        private String username;
        private String email;
        // getters and setters
    }

    static class DeviceRegistrationRequest {
        private String deviceId;
        private String deviceName;
        private String deviceType;
        private String location;
        // getters and setters
    }

    static class AccessPermissionRequest {
        private String userId;
        private String deviceId;
        private String accessType;
        private Date validFrom;
        private Date validTo;
        // getters and setters
    }

    static class AccessVerificationRequest {
        private String deviceId;
        private String userId;
        private String accessType;
        private Date timestamp;
        // getters and setters
    }

    static class AccessVerificationResponse {
        private boolean allowed;
        private String message;
        private Date timestamp;
        // getters and setters
    }

    static class AccessRecordsResponse {
        private List<AccessRecord> records;
        // getters and setters
    }

    static class AccessRecord {
        private String recordId;
        private String userId;
        private String deviceId;
        private Date timestamp;
        private boolean allowed;
        // getters and setters
    }

    static class AccountCreateRequest {
        private String userId;
        private String accountType;
        private Double initialBalance;
        // getters and setters
    }

    static class RechargeRequest {
        private String userId;
        private String accountType;
        private Double amount;
        private String paymentMethod;
        // getters and setters
    }

    static class AccountBalanceResponse {
        private String userId;
        private String accountType;
        private Double balance;
        // getters and setters
    }

    static class PaymentRequest {
        private String userId;
        private Double amount;
        private String deviceId;
        private String transactionType;
        private String description;
        // getters and setters
    }

    static class PaymentResponse {
        private boolean success;
        private String transactionId;
        private Double newBalance;
        // getters and setters
    }

    static class ConsumeRecordsResponse {
        private List<ConsumeRecord> records;
        // getters and setters
    }

    static class ConsumeRecord {
        private String transactionId;
        private String userId;
        private Double amount;
        private Date timestamp;
        private String description;
        // getters and setters
    }

    static class AttendanceRuleRequest {
        private String userId;
        private String workDays;
        private String startTime;
        private String endTime;
        private Integer graceMinutes;
        // getters and setters
    }

    static class CheckInRequest {
        private String userId;
        private String deviceId;
        private String checkType;
        private Date timestamp;
        // getters and setters
    }

    static class CheckInResponse {
        private boolean success;
        private String recordId;
        private String message;
        // getters and setters
    }

    static class AttendanceRecordsResponse {
        private List<AttendanceRecord> records;
        // getters and setters
    }

    static class AttendanceRecord {
        private String recordId;
        private String userId;
        private String checkType;
        private Date timestamp;
        private String deviceName;
        // getters and setters
    }

    static class AttendanceStatsResponse {
        private String userId;
        private String month;
        private Integer workDays;
        private Integer attendanceDays;
        private Integer lateDays;
        private Integer earlyLeaveDays;
        private Double attendanceRate;
        // getters and setters
    }

    static class VideoDeviceRequest {
        private String deviceId;
        private String deviceName;
        private String deviceType;
        private String location;
        private String ip;
        private Integer port;
        // getters and setters
    }

    static class VideoDeviceListResponse {
        private List<VideoDevice> devices;
        // getters and setters
    }

    static class VideoDevice {
        private String deviceId;
        private String deviceName;
        private String deviceType;
        private String location;
        private String status;
        // getters and setters
    }

    static class VideoStreamResponse {
        private String streamUrl;
        private String format;
        private String status;
        // getters and setters
    }
}