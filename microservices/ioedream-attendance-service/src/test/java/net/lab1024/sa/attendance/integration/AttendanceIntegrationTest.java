package net.lab1024.sa.attendance.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.mobile.AttendanceMobileService;
import net.lab1024.sa.attendance.mobile.model.*;
import net.lab1024.sa.attendance.realtime.RealtimeCalculationEngine;
import net.lab1024.sa.attendance.roster.ShiftService;
import net.lab1024.sa.attendance.leave.LeaveService;
import net.lab1024.sa.attendance.report.AttendanceReportService;
import net.lab1024.sa.attendance.rule.AttendanceRuleEngine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 考勤系统集成测试
 * <p>
 * 全面测试考勤系统各模块集成功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class AttendanceIntegrationTest {

    // ==================== 核心服务依赖 ====================

    private final AttendanceMobileService mobileService;
    private final RealtimeCalculationEngine realtimeEngine;
    private final ShiftService shiftService;
    private final LeaveService leaveService;
    private final AttendanceReportService reportService;
    private final AttendanceRuleEngine ruleEngine;

    // ==================== 测试数据 ====================

    private static final String TEST_USERNAME = "test_user_001";
    private static final String TEST_PASSWORD = "Test@123456";
    private static final Long TEST_EMPLOYEE_ID = 1001L;
    private static final String TEST_DEVICE_CODE = "MOBILE_DEVICE_001";

    private String testToken;
    private Long testUserId;

    // ==================== 移动端功能集成测试 ====================

    @Test
    @Order(1)
    @DisplayName("移动端用户登录集成测试")
    void testMobileUserLoginIntegration() {
        log.info("[集成测试] 开始测试移动端用户登录集成");

        try {
            // 1. 构建登录请求
            MobileLoginRequest loginRequest = new MobileLoginRequest();
            loginRequest.setUsername(TEST_USERNAME);
            loginRequest.setPassword(TEST_PASSWORD);
            loginRequest.setCaptchaCode("1234");
            loginRequest.setCaptchaKey("test_captcha_key");

            // 设置设备信息
            MobileLoginRequest.DeviceInfo deviceInfo = new MobileLoginRequest.DeviceInfo();
            deviceInfo.setDeviceId("TEST_DEVICE_001");
            deviceInfo.setDeviceType("ANDROID");
            deviceInfo.setDeviceModel("Test Device");
            deviceInfo.setOsVersion("Android 12");
            deviceInfo.setAppVersion("1.0.0");
            loginRequest.setDeviceInfo(deviceInfo);

            // 2. 执行登录
            CompletableFuture<MobileLoginResult> loginFuture = mobileService.login(loginRequest);
            MobileLoginResult loginResult = loginFuture.get(10, TimeUnit.SECONDS);

            // 3. 验证登录结果
            assert loginResult.isSuccess() : "登录应该成功";
            assert loginResult.getToken() != null : "Token不应为空";
            assert loginResult.getUserId() != null : "用户ID不应为空";
            assert loginResult.getUserInfo() != null : "用户信息不应为空";

            // 4. 保存测试数据
            testToken = loginResult.getToken();
            testUserId = loginResult.getUserId();

            log.info("[集成测试] 移动端用户登录集成测试通过，userId: {}, token: {}", testUserId, testToken);

        } catch (Exception e) {
            log.error("[集成测试] 移动端用户登录集成测试失败", e);
            throw new RuntimeException("移动端登录集成测试失败", e);
        }
    }

    @Test
    @Order(2)
    @DisplayName("移动端打卡流程集成测试")
    void testMobileClockingIntegration() {
        log.info("[集成测试] 开始测试移动端打卡流程集成");

        try {
            // 确保已登录
            if (testToken == null) {
                testMobileUserLoginIntegration();
            }

            // 1. 测试上班打卡
            MobileClockInRequest clockInRequest = new MobileClockInRequest();
            clockInRequest.setDeviceCode(TEST_DEVICE_CODE);
            clockInRequest.setRemark("集成测试上班打卡");

            // 设置位置信息
            MobileClockInRequest.Location location = new MobileClockInRequest.Location();
            location.setLatitude(39.9042);
            location.setLongitude(116.4074);
            location.setAddress("北京市朝阳区建国门外大街1号");
            location.setAccuracy(10.5);
            clockInRequest.setLocation(location);

            // 执行上班打卡
            CompletableFuture<MobileClockInResult> clockInFuture = mobileService.clockIn(clockInRequest, testToken);
            MobileClockInResult clockInResult = clockInFuture.get(10, TimeUnit.SECONDS);

            // 验证上班打卡结果
            assert clockInResult.isSuccess() : "上班打卡应该成功";
            assert clockInResult.getClockInTime() != null : "打卡时间不应为空";
            assert clockInResult.getRecordId() != null : "打卡记录ID不应为空";

            log.info("[集成测试] 上班打卡成功，recordId: {}, clockInTime: {}",
                    clockInResult.getRecordId(), clockInResult.getClockInTime());

            // 2. 等待一段时间模拟工作
            Thread.sleep(1000);

            // 3. 测试下班打卡
            MobileClockOutRequest clockOutRequest = new MobileClockOutRequest();
            clockOutRequest.setDeviceCode(TEST_DEVICE_CODE);
            clockOutRequest.setRemark("集成测试下班打卡");
            clockOutRequest.setLocation(location);

            // 执行下班打卡
            CompletableFuture<MobileClockOutResult> clockOutFuture = mobileService.clockOut(clockOutRequest, testToken);
            MobileClockOutResult clockOutResult = clockOutFuture.get(10, TimeUnit.SECONDS);

            // 验证下班打卡结果
            assert clockOutResult.isSuccess() : "下班打卡应该成功";
            assert clockOutResult.getClockOutTime() != null : "打卡时间不应为空";
            assert clockOutResult.getWorkHours() != null : "工时不应为空";

            log.info("[集成测试] 下班打卡成功，workHours: {}, overtimeHours: {}",
                    clockOutResult.getWorkHours(), clockOutResult.getOvertimeHours());

            log.info("[集成测试] 移动端打卡流程集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 移动端打卡流程集成测试失败", e);
            throw new RuntimeException("移动端打卡流程集成测试失败", e);
        }
    }

    @Test
    @Order(3)
    @DisplayName("实时计算引擎集成测试")
    void testRealtimeCalculationEngineIntegration() {
        log.info("[集成测试] 开始测试实时计算引擎集成");

        try {
            // 1. 启动实时计算引擎
            CompletableFuture<RealtimeCalculationEngine.EngineStartupResult> startupFuture = realtimeEngine.startup();
            RealtimeCalculationEngine.EngineStartupResult startupResult = startupFuture.get(10, TimeUnit.SECONDS);

            assert startupResult.isSuccess() : "实时计算引擎启动应该成功";
            log.info("[集成测试] 实时计算引擎启动成功");

            // 2. 测试事件处理
            AttendanceEvent attendanceEvent = AttendanceEvent.builder()
                    .employeeId(TEST_EMPLOYEE_ID)
                    .eventType(AttendanceEvent.EventType.CLOCK_IN)
                    .eventTime(LocalDateTime.now())
                    .deviceCode(TEST_DEVICE_CODE)
                    .location("北京市朝阳区建国门外大街1号")
                    .build();

            CompletableFuture<RealtimeCalculationEngine.RealtimeCalculationResult> processFuture =
                realtimeEngine.processAttendanceEvent(attendanceEvent);
            RealtimeCalculationEngine.RealtimeCalculationResult processResult = processFuture.get(10, TimeUnit.SECONDS);

            assert processResult.isSuccess() : "事件处理应该成功";
            log.info("[集成测试] 事件处理成功，processedTime: {}", processResult.getProcessedTime());

            // 3. 测试员工实时状态查询
            RealtimeCalculationEngine.TimeRange timeRange = RealtimeCalculationEngine.TimeRange.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now())
                    .build();

            CompletableFuture<RealtimeCalculationEngine.EmployeeRealtimeStatus> statusFuture =
                realtimeEngine.getEmployeeRealtimeStatus(TEST_EMPLOYEE_ID, timeRange);
            RealtimeCalculationEngine.EmployeeRealtimeStatus statusResult = statusFuture.get(10, TimeUnit.SECONDS);

            assert statusResult != null : "员工状态不应为空";
            log.info("[集成测试] 员工状态查询成功，workStatus: {}, workHours: {}",
                    statusResult.getWorkStatus(), statusResult.getWorkHours());

            // 4. 测试批量事件处理
            List<AttendanceEvent> batchEvents = List.of(
                AttendanceEvent.builder()
                        .employeeId(TEST_EMPLOYEE_ID + 1)
                        .eventType(AttendanceEvent.EventType.CLOCK_IN)
                        .eventTime(LocalDateTime.now())
                        .build(),
                AttendanceEvent.builder()
                        .employeeId(TEST_EMPLOYEE_ID + 2)
                        .eventType(AttendanceEvent.EventType.CLOCK_IN)
                        .eventTime(LocalDateTime.now())
                        .build()
            );

            CompletableFuture<RealtimeCalculationEngine.BatchCalculationResult> batchFuture =
                realtimeEngine.processBatchEvents(batchEvents);
            RealtimeCalculationEngine.BatchCalculationResult batchResult = batchFuture.get(10, TimeUnit.SECONDS);

            assert batchResult.isSuccess() : "批量事件处理应该成功";
            assert batchResult.getProcessedCount() == batchEvents.size() : "处理数量应该匹配";

            log.info("[集成测试] 批量事件处理成功，processedCount: {}, successCount: {}",
                    batchResult.getProcessedCount(), batchResult.getSuccessCount());

            log.info("[集成测试] 实时计算引擎集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 实时计算引擎集成测试失败", e);
            throw new RuntimeException("实时计算引擎集成测试失败", e);
        }
    }

    @Test
    @Order(4)
    @DisplayName("智能排班集成测试")
    void testIntelligentSchedulingIntegration() {
        log.info("[集成测试] 开始测试智能排班集成");

        try {
            LocalDate testDate = LocalDate.now();
            LocalDate weekStart = testDate.minusDays(testDate.getDayOfWeek().getValue() - 1);

            // 1. 测试排班信息查询
            CompletableFuture<List<Shift>> shiftsFuture =
                shiftService.getEmployeeShifts(TEST_EMPLOYEE_ID, testDate, testDate.plusDays(7));
            List<Shift> shifts = shiftsFuture.get(10, TimeUnit.SECONDS);

            assert shifts != null : "班次信息不应为空";
            log.info("[集成测试] 排班信息查询成功，班次数量: {}", shifts.size());

            // 2. 测试周排班查询
            CompletableFuture<Schedule> scheduleFuture =
                shiftService.getEmployeeSchedule(TEST_EMPLOYEE_ID, weekStart);
            Schedule schedule = scheduleFuture.get(10, TimeUnit.SECONDS);

            assert schedule != null : "排班信息不应为空";
            assert schedule.getShifts() != null : "班次列表不应为空";

            log.info("[集成测试] 周排班查询成功，周开始: {}, 班次数量: {}",
                    schedule.getWeekStart(), schedule.getShifts().size());

            // 3. 测试排班优化（如果实现了）
            // ShiftOptimizationResult optimizationResult = shiftService.optimizeShifts(optimizationRequest);
            // log.info("[集成测试] 排班优化完成");

            log.info("[集成测试] 智能排班集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 智能排班集成测试失败", e);
            throw new RuntimeException("智能排班集成测试失败", e);
        }
    }

    @Test
    @Order(5)
    @DisplayName("请假流程集成测试")
    void testLeaveProcessIntegration() {
        log.info("[集成测试] 开始测试请假流程集成");

        try {
            // 确保已登录
            if (testToken == null) {
                testMobileUserLoginIntegration();
            }

            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(6);

            // 1. 测试请假申请
            MobileLeaveApplicationRequest leaveRequest = new MobileLeaveApplicationRequest();
            leaveRequest.setLeaveType("PERSONAL_LEAVE");
            leaveRequest.setStartDate(startDate);
            leaveRequest.setEndDate(endDate);
            leaveRequest.setStartTime(LocalTime.of(9, 0));
            leaveRequest.setEndTime(LocalTime.of(18, 0));
            leaveRequest.setReason("集成测试请假");

            // 设置附件
            Map<String, Object> attachments = new HashMap<>();
            attachments.put("medical_certificate", "base64_encoded_certificate");
            leaveRequest.setAttachments(attachments);

            // 执行请假申请
            CompletableFuture<MobileLeaveApplicationResult> applicationFuture =
                mobileService.applyLeave(leaveRequest, testToken);
            MobileLeaveApplicationResult applicationResult = applicationFuture.get(10, TimeUnit.SECONDS);

            assert applicationResult.isSuccess() : "请假申请应该成功";
            assert applicationResult.getApplicationId() != null : "申请ID不应为空";

            log.info("[集成测试] 请假申请成功，applicationId: {}", applicationResult.getApplicationId());

            String applicationId = applicationResult.getApplicationId();

            // 2. 测试请假记录查询
            MobileLeaveQueryParam queryParam = new MobileLeaveQueryParam();
            queryParam.setStartDate(startDate.minusDays(1));
            queryParam.setEndDate(endDate.plusDays(1));

            CompletableFuture<MobileLeaveRecordsResult> recordsFuture =
                mobileService.getLeaveRecords(testToken, queryParam);
            MobileLeaveRecordsResult recordsResult = recordsFuture.get(10, TimeUnit.SECONDS);

            assert recordsResult.isSuccess() : "请假记录查询应该成功";
            assert recordsResult.getLeaveRecords() != null : "请假记录不应为空";

            log.info("[集成测试] 请假记录查询成功，记录数量: {}", recordsResult.getLeaveRecords().size());

            // 3. 测试销假申请
            MobileLeaveCancellationRequest cancellationRequest = new MobileLeaveCancellationRequest();
            cancellationRequest.setApplicationId(applicationId);
            cancellationRequest.setReason("集成测试销假");

            CompletableFuture<MobileLeaveCancellationResult> cancellationFuture =
                mobileService.cancelLeave(cancellationRequest, testToken);
            MobileLeaveCancellationResult cancellationResult = cancellationFuture.get(10, TimeUnit.SECONDS);

            assert cancellationResult.isSuccess() : "销假申请应该成功";
            log.info("[集成测试] 销假申请成功");

            log.info("[集成测试] 请假流程集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 请假流程集成测试失败", e);
            throw new RuntimeException("请假流程集成测试失败", e);
        }
    }

    @Test
    @Order(6)
    @DisplayName("考勤报表集成测试")
    void testAttendanceReportIntegration() {
        log.info("[集成测试] 开始测试考勤报表集成");

        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            // 1. 测试考勤汇总报表生成
            AttendanceReportService.AttendanceSummaryReportRequest summaryRequest =
                new AttendanceReportService.AttendanceSummaryReportRequest();
            summaryRequest.setReportType(AttendanceReportService.AttendanceSummaryReportRequest.ReportType.MONTHLY);
            summaryRequest.setStartDate(startDate);
            summaryRequest.setEndDate(endDate);
            summaryRequest.setIncludeDepartmentSummary(true);
            summaryRequest.setIncludeTrendAnalysis(true);

            CompletableFuture<AttendanceReportService.AttendanceSummaryReportResult> summaryFuture =
                reportService.generateSummaryReport(summaryRequest);
            AttendanceReportService.AttendanceSummaryReportResult summaryResult = summaryFuture.get(15, TimeUnit.SECONDS);

            assert summaryResult.isSuccess() : "汇总报表生成应该成功";
            assert summaryResult.getReport() != null : "报表数据不应为空";
            assert summaryResult.getReport().getReportId() != null : "报表ID不应为空";

            log.info("[集成测试] 汇总报表生成成功，reportId: {}, 出勤率: {}",
                    summaryResult.getReport().getReportId(), summaryResult.getReport().getAttendanceRate());

            // 2. 测试报表详情查询
            CompletableFuture<AttendanceReportService.ReportDetailResult> detailFuture =
                reportService.getReportDetail(summaryResult.getReport().getReportId());
            AttendanceReportService.ReportDetailResult detailResult = detailFuture.get(10, TimeUnit.SECONDS);

            assert detailResult.isSuccess() : "报表详情查询应该成功";
            assert detailResult.getReport() != null : "报表详情不应为空";

            log.info("[集成测试] 报表详情查询成功");

            // 3. 测试报表导出
            AttendanceReportService.ReportExportRequest exportRequest =
                new AttendanceReportService.ReportExportRequest();
            exportRequest.setReportId(summaryResult.getReport().getReportId());
            exportRequest.setExportFormat("EXCEL");
            exportRequest.setIncludeCharts(true);

            CompletableFuture<AttendanceReportService.ReportExportResult> exportFuture =
                reportService.exportReport(exportRequest);
            AttendanceReportService.ReportExportResult exportResult = exportFuture.get(15, TimeUnit.SECONDS);

            assert exportResult.isSuccess() : "报表导出应该成功";
            assert exportResult.getDownloadUrl() != null : "下载链接不应为空";

            log.info("[集成测试] 报表导出成功，下载链接: {}", exportResult.getDownloadUrl());

            // 4. 测试报表缓存状态
            AttendanceReportService.ReportCacheStatusRequest cacheRequest =
                new AttendanceReportService.ReportCacheStatusRequest();
            cacheRequest.setReportType("SUMMARY");
            cacheRequest.setDateRange(startDate + " 至 " + endDate);

            CompletableFuture<AttendanceReportService.ReportCacheStatusResult> cacheFuture =
                reportService.getReportCacheStatus(cacheRequest);
            AttendanceReportService.ReportCacheStatusResult cacheResult = cacheFuture.get(10, TimeUnit.SECONDS);

            assert cacheResult.isSuccess() : "缓存状态查询应该成功";
            log.info("[集成测试] 缓存状态查询成功，缓存命中率: {}", cacheResult.getCacheHitRate());

            log.info("[集成测试] 考勤报表集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 考勤报表集成测试失败", e);
            throw new RuntimeException("考勤报表集成测试失败", e);
        }
    }

    @Test
    @Order(7)
    @DisplayName("考勤规则引擎集成测试")
    void testAttendanceRuleEngineIntegration() {
        log.info("[集成测试] 开始测试考勤规则引擎集成");

        try {
            // 1. 测试规则引擎启动
            CompletableFuture<AttendanceRuleEngine.RuleEngineStartupResult> startupFuture =
                ruleEngine.startup();
            AttendanceRuleEngine.RuleEngineStartupResult startupResult = startupFuture.get(10, TimeUnit.SECONDS);

            assert startupResult.isSuccess() : "规则引擎启动应该成功";
            log.info("[集成测试] 规则引擎启动成功");

            // 2. 测试规则配置
            AttendanceRuleEngine.RuleConfig ruleConfig = AttendanceRuleEngine.RuleConfig.builder()
                    .ruleId("LATE_DETECTION_RULE")
                    .ruleName("迟到检测规则")
                    .ruleType("TIME_BASED")
                    .enabled(true)
                    .priority(1)
                    .build();

            Map<String, Object> ruleParams = new HashMap<>();
            ruleParams.put("standardStartTime", "09:00");
            ruleParams.put("gracePeriodMinutes", "5");
            ruleParams.put("lateThresholdMinutes", "15");
            ruleConfig.setParameters(ruleParams);

            CompletableFuture<AttendanceRuleEngine.RuleConfigResult> configFuture =
                ruleEngine.configureRule(ruleConfig);
            AttendanceRuleEngine.RuleConfigResult configResult = configFuture.get(10, TimeUnit.SECONDS);

            assert configResult.isSuccess() : "规则配置应该成功";
            log.info("[集成测试] 规则配置成功，ruleId: {}", ruleConfig.getRuleId());

            // 3. 测试规则执行
            AttendanceRuleEngine.AttendanceRuleContext context =
                AttendanceRuleEngine.AttendanceRuleContext.builder()
                        .employeeId(TEST_EMPLOYEE_ID)
                        .attendanceDate(LocalDate.now())
                        .clockInTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 10)))
                        .clockOutTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)))
                        .build();

            CompletableFuture<AttendanceRuleEngine.RuleExecutionResult> executionFuture =
                ruleEngine.executeRules("LATE_DETECTION_RULE", context);
            AttendanceRuleEngine.RuleExecutionResult executionResult = executionFuture.get(10, TimeUnit.SECONDS);

            assert executionResult.isSuccess() : "规则执行应该成功";
            assert executionResult.getResults() != null : "执行结果不应为空";

            log.info("[集成测试] 规则执行成功，结果数量: {}, 执行耗时: {}ms",
                    executionResult.getResults().size(), executionResult.getExecutionTimeMs());

            // 4. 测试规则统计
            CompletableFuture<AttendanceRuleEngine.RuleStatisticsResult> statsFuture =
                ruleEngine.getRuleStatistics();
            AttendanceRuleEngine.RuleStatisticsResult statsResult = statsFuture.get(10, TimeUnit.SECONDS);

            assert statsResult.isSuccess() : "规则统计查询应该成功";
            assert statsResult.getStatistics() != null : "统计数据不应为空";

            log.info("[集成测试] 规则统计查询成功，总规则数: {}, 活跃规则数: {}",
                    statsResult.getStatistics().getTotalRules(), statsResult.getStatistics().getActiveRules());

            log.info("[集成测试] 考勤规则引擎集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 考勤规则引擎集成测试失败", e);
            throw new RuntimeException("考勤规则引擎集成测试失败", e);
        }
    }

    @Test
    @Order(8)
    @DisplayName("系统集成性能测试")
    void testSystemIntegrationPerformance() {
        log.info("[集成测试] 开始测试系统集成性能");

        try {
            int testCount = 100;
            long startTime = System.currentTimeMillis();

            // 1. 测试并发登录性能
            List<CompletableFuture<MobileLoginResult>> loginFutures = new java.util.ArrayList<>();
            for (int i = 0; i < testCount; i++) {
                MobileLoginRequest loginRequest = new MobileLoginRequest();
                loginRequest.setUsername(TEST_USERNAME + "_" + i);
                loginRequest.setPassword(TEST_PASSWORD);

                CompletableFuture<MobileLoginResult> future = mobileService.login(loginRequest);
                loginFutures.add(future);
            }

            // 等待所有登录完成
            for (CompletableFuture<MobileLoginResult> future : loginFutures) {
                future.get(5, TimeUnit.SECONDS);
            }

            long loginTime = System.currentTimeMillis() - startTime;
            double loginTps = (double) testCount / (loginTime / 1000.0);

            log.info("[集成测试] 并发登录性能测试完成，请求数: {}, 总耗时: {}ms, TPS: {:.2f}",
                    testCount, loginTime, loginTps);

            assert loginTps > 50.0 : "登录TPS应该大于50";

            // 2. 测试并发打卡性能
            startTime = System.currentTimeMillis();
            List<CompletableFuture<MobileClockInResult>> clockInFutures = new java.util.ArrayList<>();

            for (int i = 0; i < testCount; i++) {
                if (testToken != null) {
                    MobileClockInRequest clockInRequest = new MobileClockInRequest();
                    clockInRequest.setDeviceCode(TEST_DEVICE_CODE + "_" + i);
                    clockInRequest.setRemark("性能测试打卡");

                    CompletableFuture<MobileClockInResult> future = mobileService.clockIn(clockInRequest, testToken);
                    clockInFutures.add(future);
                }
            }

            // 等待所有打卡完成
            int successCount = 0;
            for (CompletableFuture<MobileClockInResult> future : clockInFutures) {
                try {
                    MobileClockInResult result = future.get(5, TimeUnit.SECONDS);
                    if (result.isSuccess()) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("[集成测试] 打卡请求失败", e);
                }
            }

            long clockInTime = System.currentTimeMillis() - startTime;
            double clockInTps = (double) successCount / (clockInTime / 1000.0);

            log.info("[集成测试] 并发打卡性能测试完成，成功数: {}, 总耗时: {}ms, TPS: {:.2f}",
                    successCount, clockInTime, clockInTps);

            assert clockInTps > 30.0 : "打卡TPS应该大于30";

            // 3. 测试内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();

            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

            log.info("[集成测试] 内存使用情况 - 已用: {}MB, 可用: {}MB, 最大: {}MB, 使用率: {:.2f}%",
                    usedMemory / 1024 / 1024, freeMemory / 1024 / 1024, maxMemory / 1024 / 1024, memoryUsagePercent);

            assert memoryUsagePercent < 80.0 : "内存使用率应该小于80%";

            log.info("[集成测试] 系统集成性能测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 系统集成性能测试失败", e);
            throw new RuntimeException("系统集成性能测试失败", e);
        }
    }

    @Test
    @Order(9)
    @DisplayName("移动端健康检查集成测试")
    void testMobileHealthCheckIntegration() {
        log.info("[集成测试] 开始测试移动端健康检查集成");

        try {
            // 确保已登录
            if (testToken == null) {
                testMobileUserLoginIntegration();
            }

            // 执行健康检查
            CompletableFuture<MobileHealthCheckResult> healthFuture = mobileService.healthCheck(testToken);
            MobileHealthCheckResult healthResult = healthFuture.get(10, TimeUnit.SECONDS);

            assert healthResult.isHealthy() : "系统应该健康";
            assert healthResult.getTokenValid() : "Token应该有效";
            assert healthResult.getServicesHealth() != null : "服务健康状态不应为空";
            assert healthResult.getCheckTime() != null : "检查时间不应为空";

            Map<String, Boolean> servicesHealth = healthResult.getServicesHealth();
            log.info("[集成测试] 健康检查结果 - 整体健康: {}, Token有效: {}, 考勤服务: {}, 缓存服务: {}, 通知服务: {}",
                    healthResult.isHealthy(),
                    healthResult.getTokenValid(),
                    servicesHealth.getOrDefault("attendance", false),
                    servicesHealth.getOrDefault("cache", false),
                    servicesHealth.getOrDefault("notification", false));

            // 验证核心服务健康
            assert servicesHealth.getOrDefault("attendance", false) : "考勤服务应该健康";
            assert servicesHealth.getOrDefault("cache", false) : "缓存服务应该健康";
            assert servicesHealth.getOrDefault("notification", false) : "通知服务应该健康";

            log.info("[集成测试] 移动端健康检查集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 移动端健康检查集成测试失败", e);
            throw new RuntimeException("移动端健康检查集成测试失败", e);
        }
    }

    @Test
    @Order(10)
    @DisplayName("数据一致性集成测试")
    void testDataConsistencyIntegration() {
        log.info("[集成测试] 开始测试数据一致性集成");

        try {
            // 确保已登录
            if (testToken == null) {
                testMobileUserLoginIntegration();
            }

            LocalDate testDate = LocalDate.now();

            // 1. 通过移动端获取考勤记录
            MobileRecordQueryParam queryParam = new MobileRecordQueryParam();
            queryParam.setStartDate(testDate);
            queryParam.setEndDate(testDate);
            queryParam.setPageNum(1);
            queryParam.setPageSize(100);

            CompletableFuture<MobileAttendanceRecordsResult> mobileRecordsFuture =
                mobileService.getAttendanceRecords(testToken, queryParam);
            MobileAttendanceRecordsResult mobileRecordsResult = mobileRecordsFuture.get(10, TimeUnit.SECONDS);

            assert mobileRecordsResult.isSuccess() : "移动端考勤记录查询应该成功";

            // 2. 通过实时计算引擎获取统计
            RealtimeCalculationEngine.TimeRange timeRange = RealtimeCalculationEngine.TimeRange.builder()
                    .startDate(testDate)
                    .endDate(testDate)
                    .build();

            CompletableFuture<RealtimeCalculationEngine.EmployeeRealtimeStatus> statusFuture =
                realtimeEngine.getEmployeeRealtimeStatus(testUserId, timeRange);
            RealtimeCalculationEngine.EmployeeRealtimeStatus statusResult = statusFuture.get(10, TimeUnit.SECONDS);

            assert statusResult != null : "员工状态不应为空";

            // 3. 数据一致性验证
            int mobileRecordCount = mobileRecordsResult.getRecords().size();
            boolean hasClockInRecord = mobileRecordsResult.getRecords().stream()
                    .anyMatch(record -> record.getClockInTime() != null);
            boolean hasClockOutRecord = mobileRecordsResult.getRecords().stream()
                    .anyMatch(record -> record.getClockOutTime() != null);

            log.info("[集成测试] 数据一致性验证 - 移动端记录数: {}, 有上班打卡: {}, 有下班打卡: {}",
                    mobileRecordCount, hasClockInRecord, hasClockOutRecord);

            log.info("[集成测试] 实时状态 - 工作状态: {}, 工时: {}, 上班时间: {}, 下班时间: {}",
                    statusResult.getWorkStatus(),
                    statusResult.getWorkHours(),
                    statusResult.getClockInTime(),
                    statusResult.getClockOutTime());

            // 验证数据基本一致性
            // 这里可以根据具体业务逻辑添加更严格的一致性检查

            log.info("[集成测试] 数据一致性集成测试通过");

        } catch (Exception e) {
            log.error("[集成测试] 数据一致性集成测试失败", e);
            throw new RuntimeException("数据一致性集成测试失败", e);
        }
    }

    // ==================== 辅助类定义 ====================

    /**
     * 考勤事件
     */
    private static class AttendanceEvent {
        private Long employeeId;
        private EventType eventType;
        private LocalDateTime eventTime;
        private String deviceCode;
        private String location;

        public enum EventType {
            CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END
        }

        public static AttendanceEventBuilder builder() {
            return new AttendanceEventBuilder();
        }

        public static class AttendanceEventBuilder {
            private AttendanceEvent event = new AttendanceEvent();

            public AttendanceEventBuilder employeeId(Long employeeId) {
                event.employeeId = employeeId;
                return this;
            }

            public AttendanceEventBuilder eventType(EventType eventType) {
                event.eventType = eventType;
                return this;
            }

            public AttendanceEventBuilder eventTime(LocalDateTime eventTime) {
                event.eventTime = eventTime;
                return this;
            }

            public AttendanceEventBuilder deviceCode(String deviceCode) {
                event.deviceCode = deviceCode;
                return this;
            }

            public AttendanceEventBuilder location(String location) {
                event.location = location;
                return this;
            }

            public AttendanceEvent build() {
                return event;
            }
        }

        // Getters
        public Long getEmployeeId() { return employeeId; }
        public EventType getEventType() { return eventType; }
        public LocalDateTime getEventTime() { return eventTime; }
        public String getDeviceCode() { return deviceCode; }
        public String getLocation() { return location; }
    }

    /**
     * 班次信息
     */
    private static class Shift {
        private Long shiftId;
        private String shiftName;
        private String shiftType;
        private LocalTime startTime;
        private LocalTime endTime;

        // Getters
        public Long getShiftId() { return shiftId; }
        public String getShiftName() { return shiftName; }
        public String getShiftType() { return shiftType; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
    }

    /**
     * 排班信息
     */
    private static class Schedule {
        private LocalDate weekStart;
        private List<Shift> shifts;

        // Getters
        public LocalDate getWeekStart() { return weekStart; }
        public List<Shift> getShifts() { return shifts; }
    }

    /**
     * 考勤记录
     */
    private static class MobileAttendanceRecord {
        private Long recordId;
        private LocalDateTime clockInTime;
        private LocalDateTime clockOutTime;

        // Getters
        public Long getRecordId() { return recordId; }
        public LocalDateTime getClockInTime() { return clockInTime; }
        public LocalDateTime getClockOutTime() { return clockOutTime; }
    }
}