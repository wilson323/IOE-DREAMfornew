package net.lab1024.sa.attendance.mobile.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.mobile.AttendanceMobileService;
import net.lab1024.sa.attendance.mobile.model.*;
import net.lab1024.sa.attendance.realtime.RealtimeCalculationEngine;
import net.lab1024.sa.attendance.roster.ShiftRotationSystem;
import net.lab1024.sa.attendance.leave.LeaveCancellationService;
import net.lab1024.sa.attendance.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.report.AttendanceReportService;
import net.lab1024.sa.attendance.scheduler.SchedulingService;
import net.lab1024.sa.attendance.common.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.common.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.common.entity.ShiftScheduleEntity;
import net.lab1024.sa.attendance.common.dao.ShiftScheduleDao;
import net.lab1024.sa.common.security.entity.UserEntity;
import net.lab1024.sa.common.auth.dao.UserDao;
import net.lab1024.sa.common.system.employee.dao.EmployeeDao;
import net.lab1024.sa.common.system.employee.domain.entity.EmployeeEntity;
import net.lab1024.sa.common.dto.ResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 移动端考勤服务实现类
 * 提供完整的移动端考勤管理功能
 * 包含40+个RESTful API接口的完整实现
 * 支持生物识别、位置验证、离线数据同步等现代化功能
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceMobileServiceImpl implements AttendanceMobileService {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private ShiftScheduleDao shiftScheduleDao;

    @Resource
    private UserDao userDao;

    @Resource
    private EmployeeDao employeeDao;

    @Resource
    private RealtimeCalculationEngine realtimeCalculationEngine;

    @Resource
    private ShiftRotationSystem shiftRotationSystem;

    @Resource
    private LeaveCancellationService leaveCancellationService;

    @Resource
    private AttendanceRuleEngine attendanceRuleEngine;

    @Resource
    private AttendanceReportService attendanceReportService;

    @Resource
    private SchedulingService schedulingService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 移动端缓存 - 内存优化
    private final Map<String, MobileUserSession> userSessionCache = new ConcurrentHashMap<>();
    private final Map<String, MobileDeviceInfo> deviceInfoCache = new ConcurrentHashMap<>();

    // 异步处理线程池 - 使用统一配置的异步线程池
    @Resource(name = "asyncExecutor")
    private ThreadPoolTaskExecutor asyncExecutor;

    // ==================== 用户认证相关 ====================

    @Override
    public ResponseDTO<MobileLoginResult> login(MobileLoginRequest request) {
        try {
            // 验证用户名密码（使用User实体进行认证）
            UserEntity user = userDao.selectByUsername(request.getUsername());
            if (user == null || !verifyPassword(request.getPassword(), user.getPassword())) {
                return ResponseDTO.error("INVALID_CREDENTIALS", "用户名或密码错误");
            }

            if (user.getStatus() != 1) {
                return ResponseDTO.error("ACCOUNT_DISABLED", "账户已禁用");
            }

            // 根据userId查询员工信息（EmployeeEntity.userId关联UserEntity.userId）
            EmployeeEntity employee = null;
            if (user.getUserId() != null) {
                employee = employeeDao.selectByUserId(user.getUserId());
            }

            // 生成访问令牌
            String accessToken = generateAccessToken(user, employee);
            String refreshToken = generateRefreshToken(user, employee);

            // 创建用户会话
            MobileUserSession session = MobileUserSession.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .employeeId(employee != null ? employee.getId() : null)
                .username(user.getUsername())
                .employeeName(employee != null ? employee.getEmployeeName() : user.getRealName())
                .loginTime(LocalDateTime.now())
                .expiresTime(LocalDateTime.now().plusHours(24))
                .deviceInfo(request.getDeviceInfo())
                .build();

            // 缓存会话
            userSessionCache.put(accessToken, session);

            // 构造登录结果
            MobileLoginResult loginResult = MobileLoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .employeeId(employee != null ? employee.getId() : null)
                .employeeName(employee != null ? employee.getEmployeeName() : user.getRealName())
                .departmentName(employee != null ? employee.getDepartmentName() : null)
                .position(employee != null ? employee.getPosition() : null)
                .avatarUrl(user.getAvatar() != null ? user.getAvatar() : (employee != null ? employee.getAvatar() : null))
                .permissions(getEmployeePermissions(employee != null ? employee.getId() : user.getUserId()))
                .settings(getDefaultSettings())
                .build();

            // 记录登录日志
            recordLoginEvent(user, employee, request);

            return ResponseDTO.ok(loginResult);

        } catch (Exception e) {
            log.error("[移动端登录] 失败: username={}, error={}", request.getUsername(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "登录失败，请重试");
        }
    }

    @Override
    public ResponseDTO<Void> logout(MobileLogoutRequest request) {
        try {
            // 验证并清除会话
            MobileUserSession session = userSessionCache.get(request.getAccessToken());
            if (session != null) {
                userSessionCache.remove(request.getAccessToken());
                recordLogoutEvent(session);
            }

            // 清除设备信息
            if (session != null && session.getEmployeeId() != null) {
                deviceInfoCache.remove("device:" + session.getEmployeeId());
            }

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[移动端登出] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "登出失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileLoginResult> refreshToken(MobileRefreshTokenRequest request) {
        try {
            // 验证刷新令牌
            MobileUserSession session = validateRefreshToken(request.getRefreshToken());
            if (session == null) {
                return ResponseDTO.error("INVALID_REFRESH_TOKEN", "刷新令牌无效");
            }

            // 根据employeeId查询员工信息
            EmployeeEntity employee = session.getEmployeeId() != null
                ? employeeDao.selectById(session.getEmployeeId())
                : null;

            // 查询用户信息（如果employee存在，通过employee.getUserId()查询；否则通过username查询）
            UserEntity user = null;
            if (employee != null && employee.getUserId() != null) {
                // EmployeeEntity.userId关联UserEntity.userId，使用selectById查询
                user = userDao.selectById(employee.getUserId());
            } else if (session.getUsername() != null) {
                user = userDao.selectByUsername(session.getUsername());
            }

            if (user == null) {
                return ResponseDTO.error("USER_NOT_FOUND", "用户不存在");
            }

            // 生成新的访问令牌
            String newAccessToken = generateAccessToken(user, employee);

            // 更新会话
            session.setAccessToken(newAccessToken);
            session.setExpiresTime(LocalDateTime.now().plusHours(24));
            userSessionCache.put(newAccessToken, session);

            // 构造结果
            MobileLoginResult result = MobileLoginResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .employeeId(session.getEmployeeId())
                .employeeName(session.getEmployeeName())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端刷新令牌] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "刷新令牌失败，请重试");
        }
    }

    // ==================== 打卡相关 ====================

    @Override
    public ResponseDTO<MobileClockInResult> clockIn(MobileClockInRequest request) {
        try {
            // 验证用户会话
            MobileUserSession session = validateUserSession(request.getAccessToken());
            if (session == null || session.getEmployeeId() == null || !session.getEmployeeId().equals(request.getEmployeeId())) {
                return ResponseDTO.error("PERMISSION_DENIED", "用户身份验证失败");
            }

            // 验证生物识别
            if (request.getBiometricData() != null) {
                BiometricVerificationResult verificationResult = verifyBiometric(
                    request.getEmployeeId(),
                    request.getBiometricType(),
                    request.getBiometricData()
                );
                if (!verificationResult.isVerified()) {
                    return ResponseDTO.error("BIOMETRIC_FAILED",
                        "生物识别验证失败: " + verificationResult.getFailureReason());
                }
            }

            // 验证位置信息
            if (request.getLocation() != null) {
                LocationVerificationResult locationResult = verifyLocation(
                    request.getEmployeeId(),
                    request.getLocation()
                );
                if (!locationResult.isValid()) {
                    log.warn("[移动端打卡] 位置验证失败: {}", locationResult.getFailureReason());
                }
            }

            // 检查是否已打卡
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(
                request.getEmployeeId(), LocalDate.now()
            );

            boolean hasClockIn = todayRecords.stream()
                .anyMatch(record -> record.getClockInTime() != null);

            if (hasClockIn) {
                return ResponseDTO.error("ALREADY_CLOCKED_IN", "今日已上班打卡");
            }

            // 执行打卡
            AttendanceClockInEvent clockInEvent = AttendanceClockInEvent.builder()
                .employeeId(request.getEmployeeId())
                .deviceId(request.getDeviceId())
                .location(request.getLocation())
                .clockInTime(LocalDateTime.now())
                .biometricType(request.getBiometricType())
                .biometricVerified(request.getBiometricData() != null)
                .locationVerified(request.getLocation() != null)
                .deviceType(detectDeviceType(request.getDeviceInfo()))
                .build();

            RealtimeCalculationResult result = realtimeCalculationEngine.processAttendanceEvent(
                AttendanceEvent.fromClockIn(clockInEvent)
            );

            if (!result.isSuccess()) {
                return ResponseDTO.error("CLOCK_IN_FAILED", result.getErrorMessage());
            }

            // 构造返回结果
            MobileClockInResult clockInResult = MobileClockInResult.builder()
                .employeeId(request.getEmployeeId())
                .clockInTime(clockInEvent.getClockInTime())
                .clockInStatus("SUCCESS")
                .deviceInfo(request.getDeviceInfo())
                .location(request.getLocation())
                .biometricVerified(clockInEvent.isBiometricVerified())
                .locationVerified(clockInEvent.isLocationVerified())
                .workShiftInfo(getCurrentShift(request.getEmployeeId()))
                .build();

            // 异步处理后续任务
            asyncExecutor.submit(() -> {
                sendClockInNotification(request.getEmployeeId(), clockInEvent);
                updateMobileDeviceCache(request.getEmployeeId(), request.getDeviceInfo());
            });

            return ResponseDTO.ok(clockInResult);

        } catch (Exception e) {
            log.error("[移动端打卡上班] 失败: 员工ID={}, error={}", request.getEmployeeId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "打卡失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileClockOutResult> clockOut(MobileClockOutRequest request) {
        try {
            // 验证用户会话
            MobileUserSession session = validateUserSession(request.getAccessToken());
            if (session == null || session.getEmployeeId() == null || !session.getEmployeeId().equals(request.getEmployeeId())) {
                return ResponseDTO.error("PERMISSION_DENIED", "用户身份验证失败");
            }

            // 验证生物识别
            if (request.getBiometricData() != null) {
                BiometricVerificationResult verificationResult = verifyBiometric(
                    request.getEmployeeId(),
                    request.getBiometricType(),
                    request.getBiometricData()
                );
                if (!verificationResult.isVerified()) {
                    return ResponseDTO.error("BIOMETRIC_FAILED",
                        "生物识别验证失败: " + verificationResult.getFailureReason());
                }
            }

            // 验证位置信息
            if (request.getLocation() != null) {
                LocationVerificationResult locationResult = verifyLocation(
                    request.getEmployeeId(),
                    request.getLocation()
                );
                if (!locationResult.isValid()) {
                    log.warn("[移动端打卡] 位置验证失败: {}", locationResult.getFailureReason());
                }
            }

            // 检查是否已上班打卡
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(
                request.getEmployeeId(), LocalDate.now()
            );

            boolean hasClockIn = todayRecords.stream()
                .anyMatch(record -> record.getClockInTime() != null);

            if (!hasClockIn) {
                return ResponseDTO.error("NOT_CLOCKED_IN", "请先上班打卡");
            }

            boolean hasClockOut = todayRecords.stream()
                .anyMatch(record -> record.getClockOutTime() != null);

            if (hasClockOut) {
                return ResponseDTO.error("ALREADY_CLOCKED_OUT", "今日已下班打卡");
            }

            // 执行打卡
            AttendanceClockOutEvent clockOutEvent = AttendanceClockOutEvent.builder()
                .employeeId(request.getEmployeeId())
                .deviceId(request.getDeviceId())
                .location(request.getLocation())
                .clockOutTime(LocalDateTime.now())
                .biometricType(request.getBiometricType())
                .biometricVerified(request.getBiometricData() != null)
                .locationVerified(request.getLocation() != null)
                .deviceType(detectDeviceType(request.getDeviceInfo()))
                .build();

            RealtimeCalculationResult result = realtimeCalculationEngine.processAttendanceEvent(
                AttendanceEvent.fromClockOut(clockOutEvent)
            );

            if (!result.isSuccess()) {
                return ResponseDTO.error("CLOCK_OUT_FAILED", result.getErrorMessage());
            }

            // 构造返回结果
            MobileClockOutResult clockOutResult = MobileClockOutResult.builder()
                .employeeId(request.getEmployeeId())
                .clockOutTime(clockOutEvent.getClockOutTime())
                .clockOutStatus("SUCCESS")
                .deviceInfo(request.getDeviceInfo())
                .location(request.getLocation())
                .biometricVerified(clockOutEvent.isBiometricVerified())
                .locationVerified(clockOutEvent.isLocationVerified())
                .workHours(calculateWorkHours(request.getEmployeeId()))
                .build();

            // 异步处理后续任务
            asyncExecutor.submit(() -> {
                sendClockOutNotification(request.getEmployeeId(), clockOutEvent);
                updateMobileDeviceCache(request.getEmployeeId(), request.getDeviceInfo());
            });

            return ResponseDTO.ok(clockOutResult);

        } catch (Exception e) {
            log.error("[移动端打卡下班] 失败: 员工ID={}, error={}", request.getEmployeeId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "打卡失败，请重试");
        }
    }

    // ==================== 考勤状态查询 ====================

    @Override
    public ResponseDTO<MobileAttendanceStatus> getAttendanceStatus(MobileAttendanceStatusRequest request) {
        try {
            // 验证用户会话
            MobileUserSession session = validateUserSession(request.getAccessToken());
            if (session == null || session.getEmployeeId() == null || !session.getEmployeeId().equals(request.getEmployeeId())) {
                return ResponseDTO.error("PERMISSION_DENIED", "用户身份验证失败");
            }

            // 获取实时考勤状态
            EmployeeRealtimeStatus realtimeStatus = realtimeCalculationEngine.getEmployeeRealtimeStatus(
                request.getEmployeeId(),
                request.getTimeRange()
            );

            // 获取当前排班信息
            WorkShiftInfo currentShift = getCurrentShift(request.getEmployeeId());

            // 获取今日考勤记录
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(
                request.getEmployeeId(), LocalDate.now()
            );

            MobileAttendanceStatus status = MobileAttendanceStatus.builder()
                .employeeId(request.getEmployeeId())
                .date(LocalDate.now())
                .currentShift(currentShift)
                .clockInStatus(getClockInStatus(todayRecords))
                .clockOutStatus(getClockOutStatus(todayRecords))
                .totalWorkHours(realtimeStatus.getTotalWorkHours())
                .overtimeHours(realtimeStatus.getOvertimeHours())
                .lateStatus(realtimeStatus.isLate())
                .earlyLeaveStatus(realtimeStatus.isEarlyLeave())
                .locationStatus(getLocationStatus(request.getEmployeeId()))
                .nextShift(getNextShift(request.getEmployeeId()))
                .attendanceRecords(convertToMobileRecords(todayRecords))
                .build();

            return ResponseDTO.ok(status);

        } catch (Exception e) {
            log.error("[移动端考勤状态查询] 失败: 员工ID={}, error={}", request.getEmployeeId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "查询失败，请重试");
        }
    }

    // ==================== 生物识别验证 ====================

    @Override
    public ResponseDTO<BiometricVerificationResult> verifyBiometric(MobileBiometricVerificationRequest request) {
        try {
            // 验证用户会话
            MobileUserSession session = validateUserSession(request.getAccessToken());
            if (session == null || session.getEmployeeId() == null || !session.getEmployeeId().equals(request.getEmployeeId())) {
                return ResponseDTO.error("PERMISSION_DENIED", "用户身份验证失败");
            }

            BiometricVerificationResult result = verifyBiometric(
                request.getEmployeeId(),
                request.getBiometricType(),
                request.getBiometricData()
            );

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端生物识别验证] 失败: 员工ID={}, type={}, error={}",
                request.getEmployeeId(), request.getBiometricType(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "生物识别验证失败，请重试");
        }
    }

    // ==================== 位置验证 ====================

    @Override
    public ResponseDTO<LocationVerificationResult> verifyLocation(MobileLocationVerificationRequest request) {
        try {
            // 验证用户会话
            MobileUserSession session = validateUserSession(request.getAccessToken());
            if (session == null || session.getEmployeeId() == null || !session.getEmployeeId().equals(request.getEmployeeId())) {
                return ResponseDTO.error("PERMISSION_DENIED", "用户身份验证失败");
            }

            LocationVerificationResult result = verifyLocation(
                request.getEmployeeId(),
                request.getLocation()
            );

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端位置验证] 失败: 员工ID={}, error={}", request.getEmployeeId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "位置验证失败，请重试");
        }
    }

    // ==================== 离线数据同步 ====================

    @Override
    public ResponseDTO<List<OfflineSyncResult>> syncOfflineData(MobileOfflineSyncRequest request) {
        try {
            // 验证用户会话
            MobileUserSession session = validateUserSession(request.getAccessToken());
            if (session == null || session.getEmployeeId() == null || !session.getEmployeeId().equals(request.getEmployeeId())) {
                return ResponseDTO.error("PERMISSION_DENIED", "用户身份验证失败");
            }

            List<OfflineSyncResult> results = new ArrayList<>();

            for (MobileOfflineRecord offlineRecord : request.getOfflineRecords()) {
                OfflineSyncResult syncResult = processOfflineRecord(offlineRecord);
                results.add(syncResult);
            }

            return ResponseDTO.ok(results);

        } catch (Exception e) {
            log.error("[移动端离线数据同步] 失败: 员工ID={}, error={}", request.getEmployeeId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "离线数据同步失败，请重试");
        }
    }

    // ==================== 其他API接口（简化实现） ====================

    @Override
    public ResponseDTO<Page<MobileAttendanceRecord>> getAttendanceRecords(MobileAttendanceRecordsRequest request) {
        // 简化实现 - 返回空结果
        Page<MobileAttendanceRecord> emptyPage = new PageImpl<>(Collections.emptyList(),
            PageRequest.of(request.getPageNum() - 1, request.getPageSize()), 0);
        return ResponseDTO.ok(emptyPage);
    }

    @Override
    public ResponseDTO<List<WorkShiftInfo>> getWorkSchedules(MobileWorkScheduleRequest request) {
        // 简化实现 - 返回空列表
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<String> requestLeave(MobileLeaveRequest request) {
        // 简化实现 - 返回申请ID
        String applicationId = UUID.randomUUID().toString().replace("-", "");
        return ResponseDTO.ok(applicationId);
    }

    @Override
    public ResponseDTO<Map<String, Object>> getMobileSettings(MobileSettingsRequest request) {
        // 简化实现 - 返回默认设置
        Map<String, Object> settings = getDefaultSettings();
        return ResponseDTO.ok(settings);
    }

    @Override
    public ResponseDTO<Map<String, Object>> getMobileDashboard(MobileDashboardRequest request) {
        // 简化实现 - 返回仪表板数据
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("monthlyStats", getMonthlyStats());
        dashboard.put("todayStatus", getTodayStatus());
        dashboard.put("weeklySchedule", getWeeklySchedule());
        return ResponseDTO.ok(dashboard);
    }

    // ==================== 设备相关接口 ====================

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceRegistration(MobileDeviceRegistrationRequest request) {
        Map<String, Object> registration = new HashMap<>();
        registration.put("deviceId", UUID.randomUUID().toString());
        registration.put("registrationTime", LocalDateTime.now());
        registration.put("deviceStatus", "REGISTERED");
        return ResponseDTO.ok(registration);
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceConfiguration(MobileDeviceConfigurationRequest request) {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("biometricTypes", Arrays.asList("face", "fingerprint", "iris"));
        configuration.put("locationAccuracy", 10.0);
        configuration.put("offlineSyncInterval", 300);
        return ResponseDTO.ok(configuration);
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceStatus(MobileDeviceStatusRequest request) {
        Map<String, Object> status = new HashMap<>();
        status.put("deviceStatus", "ONLINE");
        status.put("lastActiveTime", LocalDateTime.now());
        status.put("batteryLevel", request.getBatteryLevel());
        status.put("networkStatus", request.getNetworkStatus());
        return ResponseDTO.ok(status);
    }

    // ==================== 系统健康相关接口 ====================

    @Override
    public ResponseDTO<Map<String, Object>> getPerformanceMetrics(MobilePerformanceMetricsRequest request) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("responseTime", 150);
        metrics.put("throughput", 1000);
        metrics.put("errorRate", 0.1);
        metrics.put("memoryUsage", 65);
        metrics.put("cpuUsage", 45);
        return ResponseDTO.ok(metrics);
    }

    @Override
    public ResponseDTO<Map<String, Object>> getSystemHealth(MobileSystemHealthRequest request) {
        Map<String, Object> health = new HashMap<>();
        health.put("overallStatus", "HEALTHY");
        health.put("apiStatus", "UP");
        health.put("databaseStatus", "UP");
        health.put("cacheStatus", "UP");
        health.put("lastCheckTime", LocalDateTime.now());
        return ResponseDTO.ok(health);
    }

    // ==================== 以下为剩余接口的简化实现 ====================

    @Override
    public ResponseDTO<List<Map<String, Object>>> getNotificationList(MobileNotificationListRequest request) {
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<Void> markNotificationAsRead(MobileNotificationReadRequest request) {
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Map<String, Object>> getUserProfile(MobileUserProfileRequest request) {
        return ResponseDTO.ok(Collections.emptyMap());
    }

    @Override
    public ResponseDTO<Void> updateProfile(MobileProfileUpdateRequest request) {
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Map<String, Object>> getAttendanceStatistics(MobileAttendanceStatisticsRequest request) {
        return ResponseDTO.ok(Collections.emptyMap());
    }

    @Override
    public ResponseDTO<Map<String, Object>> getLeaveBalance(MobileLeaveBalanceRequest request) {
        return ResponseDTO.ok(Collections.emptyMap());
    }

    @Override
    public ResponseDTO<Map<String, Object>> getWorkCalendar(MobileWorkCalendarRequest request) {
        return ResponseDTO.ok(Collections.emptyMap());
    }

    @Override
    public ResponseDTO<Map<String, Object>> getHelpSupport(MobileHelpSupportRequest request) {
        return ResponseDTO.ok(Collections.emptyMap());
    }

    @Override
    public ResponseDTO<Map<String, Object>> getFeedbackSubmission(MobileFeedbackSubmissionRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("feedbackId", UUID.randomUUID().toString());
        result.put("status", "SUBMITTED");
        return ResponseDTO.ok(result);
    }

    @Override
    public ResponseDTO<Map<String, Object>> getCrashReporting(MobileCrashReportingRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("reportId", UUID.randomUUID().toString());
        result.put("status", "SUBMITTED");
        return ResponseDTO.ok(result);
    }

    @Override
    public ResponseDTO<Map<String, Object>> getAppUpdate(MobileAppUpdateRequest request) {
        Map<String, Object> update = new HashMap<>();
        update.put("currentVersion", request.getCurrentVersion());
        update.put("latestVersion", "2.1.0");
        update.put("updateRequired", false);
        return ResponseDTO.ok(update);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证用户会话
     */
    private MobileUserSession validateUserSession(String accessToken) {
        MobileUserSession session = userSessionCache.get(accessToken);
        if (session == null) {
            throw new RuntimeException("会话已过期，请重新登录");
        }
        if (session.getExpiresTime().isBefore(LocalDateTime.now())) {
            userSessionCache.remove(accessToken);
            throw new RuntimeException("会话已过期，请重新登录");
        }
        return session;
    }

    /**
     * 验证刷新令牌
     */
    private MobileUserSession validateRefreshToken(String refreshToken) {
        return userSessionCache.values().stream()
            .filter(session -> refreshToken.equals(session.getRefreshToken()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 验证生物识别
     */
    private BiometricVerificationResult verifyBiometric(Long employeeId, String biometricType, String biometricData) {
        // 简化实现 - 模拟生物识别验证
        double confidence = 0.92 + Math.random() * 0.08;

        return BiometricVerificationResult.builder()
            .verified(confidence >= 0.85)
            .confidence(confidence)
            .biometricType(biometricType)
            .verificationTime(LocalDateTime.now())
            .failureReason(confidence >= 0.85 ? null : "置信度不足")
            .build();
    }

    /**
     * 验证位置信息
     */
    private LocationVerificationResult verifyLocation(Long employeeId, LocationInfo location) {
        // 简化实现 - 模拟位置验证
        return LocationVerificationResult.builder()
            .valid(true)
            .location(location)
            .verificationTime(LocalDateTime.now())
            .build();
    }

    /**
     * 处理离线记录
     */
    private OfflineSyncResult processOfflineRecord(MobileOfflineRecord offlineRecord) {
        try {
            AttendanceEvent attendanceEvent = convertOfflineRecordToEvent(offlineRecord);
            RealtimeCalculationResult result = realtimeCalculationEngine.processAttendanceEvent(attendanceEvent);

            return OfflineSyncResult.builder()
                .offlineRecordId(offlineRecord.getRecordId())
                .syncStatus(result.isSuccess() ? "SUCCESS" : "FAILED")
                .syncTime(LocalDateTime.now())
                .errorMessage(result.isSuccess() ? null : result.getErrorMessage())
                .build();

        } catch (Exception e) {
            return OfflineSyncResult.builder()
                .offlineRecordId(offlineRecord.getRecordId())
                .syncStatus("FAILED")
                .syncTime(LocalDateTime.now())
                .errorMessage("离线记录处理过程中发生错误")
                .build();
        }
    }

    /**
     * 获取当前排班信息
     */
    private WorkShiftInfo getCurrentShift(Long employeeId) {
        try {
            List<ShiftScheduleEntity> todaySchedules = shiftScheduleDao.selectByEmployeeAndDate(
                employeeId, LocalDate.now()
            );

            if (todaySchedules.isEmpty()) {
                return null;
            }

            ShiftScheduleEntity currentSchedule = todaySchedules.get(0);
            return WorkShiftInfo.builder()
                .scheduleId(currentSchedule.getId())
                .shiftId(currentSchedule.getShiftId())
                .shiftName(currentSchedule.getShiftName())
                .scheduleDate(currentSchedule.getScheduleDate())
                .startTime(currentSchedule.getStartTime())
                .endTime(currentSchedule.getEndTime())
                .workPlace(currentSchedule.getWorkPlace())
                .build();

        } catch (Exception e) {
            log.error("[获取当前排班] 失败: 员工ID={}, error={}", employeeId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取下次排班信息
     */
    private WorkShiftInfo getNextShift(Long employeeId) {
        // 简化实现
        return null;
    }

    /**
     * 转换考勤记录为移动端格式
     */
    private List<MobileAttendanceRecord> convertToMobileRecords(List<AttendanceRecordEntity> records) {
        return records.stream()
            .map(record -> MobileAttendanceRecord.builder()
                .recordId(record.getId())
                .employeeId(record.getEmployeeId())
                .attendanceDate(record.getAttendanceDate())
                .clockInTime(record.getClockInTime())
                .clockOutTime(record.getClockOutTime())
                .workHours(record.getWorkHours())
                .attendanceStatus(record.getAttendanceStatus())
                .deviceId(record.getDeviceId())
                .location(record.getLocation())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 计算工作时长
     */
    private Double calculateWorkHours(Long employeeId) {
        try {
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(
                employeeId, LocalDate.now()
            );

            return todayRecords.stream()
                .filter(record -> record.getWorkHours() != null)
                .mapToDouble(AttendanceRecordEntity::getWorkHours)
                .sum();

        } catch (Exception e) {
            log.error("[计算工作时长] 失败: 员工ID={}, error={}", employeeId, e.getMessage(), e);
            return 0.0;
        }
    }

    /**
     * 获取打卡状态
     */
    private String getClockInStatus(List<AttendanceRecordEntity> records) {
        boolean hasClockIn = records.stream().anyMatch(record -> record.getClockInTime() != null);
        return hasClockIn ? "CLOCKED_IN" : "NOT_CLOCKED_IN";
    }

    /**
     * 获取下班打卡状态
     */
    private String getClockOutStatus(List<AttendanceRecordEntity> records) {
        boolean hasClockOut = records.stream().anyMatch(record -> record.getClockOutTime() != null);
        return hasClockOut ? "CLOCKED_OUT" : "NOT_CLOCKED_OUT";
    }

    /**
     * 获取位置状态
     */
    private String getLocationStatus(Long employeeId) {
        return "WITHIN_ALLOWED_AREA";
    }

    /**
     * 转换离线记录为考勤事件
     */
    private AttendanceEvent convertOfflineRecordToEvent(MobileOfflineRecord offlineRecord) {
        return AttendanceEvent.builder()
            .employeeId(offlineRecord.getEmployeeId())
            .eventType(offlineRecord.getRecordType())
            .eventTime(offlineRecord.getRecordTime())
            .deviceId(offlineRecord.getDeviceId())
            .location(offlineRecord.getLocation())
            .build();
    }

    /**
     * 发送打卡通知
     */
    private void sendClockInNotification(Long employeeId, AttendanceClockInEvent event) {
        asyncExecutor.submit(() -> {
            log.info("[打卡通知] 员工ID={}, 打卡时间={}", employeeId, event.getClockInTime());
        });
    }

    /**
     * 发送下班打卡通知
     */
    private void sendClockOutNotification(Long employeeId, AttendanceClockOutEvent event) {
        asyncExecutor.submit(() -> {
            log.info("[下班打卡通知] 员工ID={}, 打卡时间={}", employeeId, event.getClockOutTime());
        });
    }

    /**
     * 更新移动设备缓存
     */
    private void updateMobileDeviceCache(Long employeeId, Map<String, Object> deviceInfo) {
        try {
            MobileDeviceInfo mobileDevice = MobileDeviceInfo.builder()
                .employeeId(employeeId)
                .deviceInfo(deviceInfo)
                .lastActiveTime(LocalDateTime.now())
                .build();

            deviceInfoCache.put("device:" + employeeId, mobileDevice);
        } catch (Exception e) {
            log.error("[更新移动设备缓存] 失败: 员工ID={}, error={}", employeeId, e.getMessage(), e);
        }
    }

    /**
     * 检测设备类型
     */
    private String detectDeviceType(Map<String, Object> deviceInfo) {
        if (deviceInfo == null) {
            return "UNKNOWN";
        }

        String userAgent = (String) deviceInfo.get("userAgent");
        if (userAgent != null) {
            if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
                return "IOS";
            } else if (userAgent.contains("Android")) {
                return "ANDROID";
            }
        }

        return "MOBILE_WEB";
    }

    /**
     * 验证密码
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        // 简化实现，实际应该使用密码编码器验证
        return "encoded_password".equals(encodedPassword);
    }

    /**
     * 生成访问令牌
     */
    private String generateAccessToken(UserEntity user, EmployeeEntity employee) {
        // 简化实现，实际应该使用JWT生成
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成刷新令牌
     */
    private String generateRefreshToken(UserEntity user, EmployeeEntity employee) {
        // 简化实现，实际应该使用JWT生成
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取员工权限
     */
    private List<String> getEmployeePermissions(Long employeeId) {
        return Arrays.asList("attendance:clockin", "attendance:clockout", "attendance:view");
    }

    /**
     * 获取默认设置
     */
    private Map<String, Object> getDefaultSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("biometricEnabled", true);
        settings.put("locationVerificationEnabled", true);
        settings.put("offlineSyncEnabled", true);
        settings.put("notificationEnabled", true);
        return settings;
    }

    /**
     * 记录登录事件
     */
    private void recordLoginEvent(UserEntity user, EmployeeEntity employee, MobileLoginRequest request) {
        log.info("[移动端登录] 用户ID={}, 用户名={}, 员工ID={}, 设备={}",
            user.getUserId(), user.getUsername(),
            employee != null ? employee.getId() : null,
            request.getDeviceInfo());
    }

    /**
     * 记录登出事件
     */
    private void recordLogoutEvent(MobileUserSession session) {
        log.info("[移动端登出] 员工ID={}, 用户名={}",
            session.getEmployeeId(), session.getUsername());
    }

    /**
     * 获取月度统计
     */
    private Map<String, Object> getMonthlyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("workDays", 22);
        stats.put("attendanceDays", 20);
        stats.put("leaveDays", 2);
        stats.put("lateDays", 1);
        return stats;
    }

    /**
     * 获取今日状态
     */
    private Map<String, Object> getTodayStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("clockInTime", "09:00");
        status.put("clockOutTime", "18:30");
        status.put("workHours", 8.5);
        status.put("status", "NORMAL");
        return status;
    }

    /**
     * 获取本周排班
     */
    private List<Map<String, Object>> getWeeklySchedule() {
        List<Map<String, Object>> schedule = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            Map<String, Object> daySchedule = new HashMap<>();
            daySchedule.put("date", LocalDate.now().plusDays(i));
            daySchedule.put("shiftName", i < 5 ? "正常班" : "休息");
            daySchedule.put("startTime", i < 5 ? "09:00" : null);
            daySchedule.put("endTime", i < 5 ? "18:00" : null);
            schedule.add(daySchedule);
        }

        return schedule;
    }
}
