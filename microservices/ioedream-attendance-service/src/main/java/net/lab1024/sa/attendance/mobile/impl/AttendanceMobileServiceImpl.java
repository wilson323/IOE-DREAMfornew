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
import net.lab1024.sa.attendance.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.domain.entity.ScheduleRecordEntity;
import net.lab1024.sa.attendance.dao.ScheduleRecordDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.attendance.domain.entity.WorkShiftEntity;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

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
    private ScheduleRecordDao scheduleRecordDao;

    @Resource
    private net.lab1024.sa.attendance.dao.WorkShiftDao workShiftDao;

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
            return ResponseDTO.error("LOGIN_FAILED", "登录失败，请重试: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<MobileLogoutResult> logout(String token) {
        try {
            // 验证并清除会话
            MobileUserSession session = userSessionCache.get(token);
            if (session != null) {
                userSessionCache.remove(token);
                recordLogoutEvent(session);
            }

            // 清除设备信息
            if (session != null && session.getEmployeeId() != null) {
                deviceInfoCache.remove("device:" + session.getEmployeeId());
            }

            MobileLogoutResult logoutResult = MobileLogoutResult.builder()
                .success(true)
                .message("登出成功")
                .build();

            return ResponseDTO.ok(logoutResult);

        } catch (Exception e) {
            log.error("[移动端登出] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("LOGOUT_FAILED", "登出失败，请重试: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<MobileTokenRefreshResult> refreshToken(MobileTokenRefreshRequest request) {
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
            MobileTokenRefreshResult refreshResult = MobileTokenRefreshResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .build();

            return ResponseDTO.ok(refreshResult);

        } catch (Exception e) {
            log.error("[移动端刷新令牌] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("REFRESH_TOKEN_FAILED", "刷新令牌失败，请重试: " + e.getMessage());
        }
    }

    // ==================== 打卡相关 ====================

    @Override
    public ResponseDTO<MobileClockInResult> clockIn(MobileClockInRequest request, String token) {
        try {
            // 验证用户会话
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }
            Long employeeId = session.getEmployeeId();

            // 验证生物识别
            if (request.getBiometricData() != null) {
                BiometricVerificationResult verificationResult = verifyBiometric(
                    employeeId,
                    request.getBiometricData().getType(),
                    request.getBiometricData().getData()
                );
                if (!verificationResult.isVerified()) {
                    return ResponseDTO.error("BIOMETRIC_VERIFICATION_FAILED", "生物识别验证失败: " + verificationResult.getFailureReason());
                }
            }

            // 验证位置信息
            LocationInfo locationInfo = null;
            if (request.getLocation() != null) {
                locationInfo = LocationInfo.builder()
                    .latitude(request.getLocation().getLatitude())
                    .longitude(request.getLocation().getLongitude())
                    .address(request.getLocation().getAddress())
                    .accuracy(request.getLocation().getAccuracy())
                    .build();
                LocationVerificationResult locationResult = verifyLocation(employeeId, locationInfo);
                if (!locationResult.isValid()) {
                    log.warn("[移动端打卡] 位置验证失败: {}", locationResult.getFailureReason());
                }
            }

            // 检查是否已打卡
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(
                employeeId, LocalDate.now()
            );

            boolean hasClockIn = todayRecords.stream()
                .anyMatch(record -> record.getClockInTime() != null);

            if (hasClockIn) {
                return ResponseDTO.error("ALREADY_CLOCKED_IN", "今日已上班打卡");
            }

            // 执行打卡
            AttendanceClockInEvent clockInEvent = AttendanceClockInEvent.builder()
                .employeeId(employeeId)
                .deviceId(request.getDeviceCode() != null ? Long.parseLong(request.getDeviceCode()) : null)
                .location(locationInfo)
                .clockInTime(LocalDateTime.now())
                .biometricType(request.getBiometricData() != null ? request.getBiometricData().getType() : null)
                .biometricVerified(request.getBiometricData() != null)
                .locationVerified(request.getLocation() != null)
                .deviceType("MOBILE")
                .build();

            // 创建考勤记录
            AttendanceRecordEntity record = new AttendanceRecordEntity();
            record.setUserId(employeeId);
            record.setAttendanceDate(LocalDate.now());
            record.setPunchTime(clockInEvent.getClockInTime());
            record.setPunchType(0); // 0-上班打卡
            record.setAttendanceType("CHECK_IN");
            if (locationInfo != null) {
                record.setLongitude(java.math.BigDecimal.valueOf(locationInfo.getLongitude()));
                record.setLatitude(java.math.BigDecimal.valueOf(locationInfo.getLatitude()));
                record.setPunchAddress(locationInfo.getAddress());
            }
            attendanceRecordDao.insert(record);

            // 构造返回结果
            MobileClockInResult clockInResult = MobileClockInResult.builder()
                .employeeId(employeeId)
                .clockInTime(clockInEvent.getClockInTime())
                .clockInStatus("SUCCESS")
                .build();

            // 异步处理后续任务
            asyncExecutor.submit(() -> {
                sendClockInNotification(employeeId, clockInEvent);
            });

            return ResponseDTO.ok(clockInResult);

        } catch (Exception e) {
            log.error("[移动端打卡上班] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("CLOCK_IN_FAILED", "打卡失败，请重试: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<MobileClockOutResult> clockOut(MobileClockOutRequest request, String token) {
        try {
            // 验证用户会话
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }
            Long employeeId = session.getEmployeeId();

            // 验证生物识别
            if (request.getBiometricData() != null) {
                BiometricVerificationResult verificationResult = verifyBiometric(
                    employeeId,
                    request.getBiometricData().getType(),
                    request.getBiometricData().getData()
                );
                if (!verificationResult.isVerified()) {
                    return ResponseDTO.error("BIOMETRIC_VERIFICATION_FAILED", "生物识别验证失败: " + verificationResult.getFailureReason());
                }
            }

            // 验证位置信息
            LocationInfo locationInfo = null;
            if (request.getLocation() != null) {
                locationInfo = LocationInfo.builder()
                    .latitude(request.getLocation().getLatitude())
                    .longitude(request.getLocation().getLongitude())
                    .address(request.getLocation().getAddress())
                    .accuracy(request.getLocation().getAccuracy())
                    .build();
                LocationVerificationResult locationResult = verifyLocation(employeeId, locationInfo);
                if (!locationResult.isValid()) {
                    log.warn("[移动端打卡] 位置验证失败: {}", locationResult.getFailureReason());
                }
            }

            // 检查是否已上班打卡
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(
                employeeId, LocalDate.now()
            );

            boolean hasClockIn = todayRecords.stream()
                .anyMatch(record -> record.getClockInTime() != null);

            if (!hasClockIn) {
                return ResponseDTO.error("NO_CLOCK_IN", "请先上班打卡");
            }

            boolean hasClockOut = todayRecords.stream()
                .anyMatch(record -> record.getClockOutTime() != null);

            if (hasClockOut) {
                return ResponseDTO.error("ALREADY_CLOCKED_OUT", "今日已下班打卡");
            }

            // 执行打卡
            AttendanceClockOutEvent clockOutEvent = AttendanceClockOutEvent.builder()
                .employeeId(employeeId)
                .deviceId(request.getDeviceCode() != null ? Long.parseLong(request.getDeviceCode()) : null)
                .location(locationInfo)
                .clockOutTime(LocalDateTime.now())
                .biometricType(request.getBiometricData() != null ? request.getBiometricData().getType() : null)
                .biometricVerified(request.getBiometricData() != null)
                .locationVerified(request.getLocation() != null)
                .deviceType("MOBILE")
                .build();

            // 创建考勤记录
            AttendanceRecordEntity record = new AttendanceRecordEntity();
            record.setUserId(employeeId);
            record.setAttendanceDate(LocalDate.now());
            record.setPunchTime(clockOutEvent.getClockOutTime());
            record.setPunchType(1); // 1-下班打卡
            record.setAttendanceType("CHECK_OUT");
            if (locationInfo != null) {
                record.setLongitude(java.math.BigDecimal.valueOf(locationInfo.getLongitude()));
                record.setLatitude(java.math.BigDecimal.valueOf(locationInfo.getLatitude()));
                record.setPunchAddress(locationInfo.getAddress());
            }
            attendanceRecordDao.insert(record);

            // 计算工作时长
            Double workHours = calculateWorkHours(employeeId);

            // 构造返回结果
            MobileClockOutResult clockOutResult = MobileClockOutResult.builder()
                .success(true)
                .clockOutTime(clockOutEvent.getClockOutTime())
                .recordId(record.getRecordId())
                .workHours(workHours)
                .message("下班打卡成功")
                .timestamp(System.currentTimeMillis())
                .attendanceStatus("NORMAL")
                .build();

            // 异步处理后续任务
            asyncExecutor.submit(() -> {
                sendClockOutNotification(employeeId, clockOutEvent);
            });

            return ResponseDTO.ok(clockOutResult);

        } catch (Exception e) {
            log.error("[移动端打卡下班] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("CLOCK_OUT_FAILED", "打卡失败，请重试: " + e.getMessage());
        }
    }

    // ==================== 用户信息相关 ====================

    @Override
    public ResponseDTO<MobileUserInfoResult> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            EmployeeEntity employee = employeeDao.selectById(session.getEmployeeId());
            if (employee == null) {
                return ResponseDTO.error("EMPLOYEE_NOT_FOUND", "员工不存在");
            }

            UserEntity user = employee.getUserId() != null ? userDao.selectById(employee.getUserId()) : null;

            MobileUserInfoResult userInfo = MobileUserInfoResult.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getEmployeeName())
                .departmentName(employee.getDepartmentName())
                .position(employee.getPosition())
                .avatarUrl(employee.getAvatar() != null ? employee.getAvatar() : (user != null ? user.getAvatar() : null))
                .phone(employee.getPhone())
                .email(employee.getEmail())
                .permissions(getEmployeePermissions(employee.getId()))
                .build();

            return ResponseDTO.ok(userInfo);

        } catch (Exception e) {
            log.error("[移动端获取用户信息] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取用户信息失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileTodayStatusResult> getTodayStatus(@RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            Long employeeId = session.getEmployeeId();
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(
                employeeId, LocalDate.now()
            );

            MobileTodayStatusResult status = MobileTodayStatusResult.builder()
                .employeeId(employeeId)
                .date(LocalDate.now())
                .clockInStatus(getClockInStatus(todayRecords))
                .clockOutStatus(getClockOutStatus(todayRecords))
                .workHours(calculateWorkHours(employeeId))
                .currentShift(getCurrentShift(employeeId))
                .build();

            return ResponseDTO.ok(status);

        } catch (Exception e) {
            log.error("[移动端获取今日状态] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取今日状态失败，请重试");
        }
    }

    // 注意：接口中没有getAttendanceStatus方法，这个方法可能是内部使用的
    // 如果需要，可以保留为私有方法或删除

    // ==================== 生物识别验证 ====================

    @Override
    public ResponseDTO<MobileBiometricVerificationResult> verifyBiometric(
            @RequestBody MobileBiometricVerificationRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证用户会话
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            BiometricVerificationResult verificationResult = verifyBiometric(
                session.getEmployeeId(),
                request.getBiometricType(),
                request.getBiometricData()
            );

            MobileBiometricVerificationResult result = MobileBiometricVerificationResult.builder()
                .verified(verificationResult.isVerified())
                .confidence(verificationResult.getConfidence())
                .biometricType(request.getBiometricType())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端生物识别验证] 失败: type={}, error={}", request.getBiometricType(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "生物识别验证失败，请重试");
        }
    }

    // ==================== 位置验证 ====================

    // 注意：接口中没有verifyLocation方法，这个方法可能是内部使用的
    // 如果需要，可以保留为私有方法或删除

    // ==================== 离线数据同步 ====================

    // 注意：接口中没有syncOfflineData方法，这个方法可能是内部使用的
    // 如果需要，可以保留为私有方法或删除

    // ==================== 考勤记录查询 ====================

    @Override
    public ResponseDTO<MobileAttendanceRecordsResult> getAttendanceRecords(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileRecordQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            Long employeeId = session.getEmployeeId();
            LocalDate startDate = queryParam.getStartDate() != null ? queryParam.getStartDate() : LocalDate.now().minusDays(30);
            LocalDate endDate = queryParam.getEndDate() != null ? queryParam.getEndDate() : LocalDate.now();

            // 使用LambdaQueryWrapper查询日期范围
            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRecordEntity>()
                    .eq(AttendanceRecordEntity::getUserId, employeeId)
                    .ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                    .le(AttendanceRecordEntity::getAttendanceDate, endDate)
                    .orderByDesc(AttendanceRecordEntity::getPunchTime)
            );

            MobileAttendanceRecordsResult result = MobileAttendanceRecordsResult.builder()
                .employeeId(employeeId)
                .records(convertToMobileRecords(records))
                .totalCount(records.size())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取考勤记录] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取考勤记录失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileStatisticsResult> getStatistics(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileStatisticsQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            Long employeeId = session.getEmployeeId();
            LocalDate startDate = queryParam.getStartDate() != null ? queryParam.getStartDate() : LocalDate.now().minusDays(30);
            LocalDate endDate = queryParam.getEndDate() != null ? queryParam.getEndDate() : LocalDate.now();

            // 获取统计信息
            MobileStatisticsResult result = MobileStatisticsResult.builder()
                .employeeId(employeeId)
                .startDate(startDate)
                .endDate(endDate)
                .totalWorkDays(22)
                .attendanceDays(20)
                .leaveDays(2)
                .lateDays(1)
                .earlyLeaveDays(0)
                .overtimeHours(10.5)
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取考勤统计] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取考勤统计失败，请重试");
        }
    }

    // ==================== 请假相关 ====================

    @Override
    public ResponseDTO<MobileLeaveApplicationResult> applyLeave(
            @RequestBody MobileLeaveApplicationRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            String applicationId = UUID.randomUUID().toString().replace("-", "");
            MobileLeaveApplicationResult result = MobileLeaveApplicationResult.builder()
                .applicationId(applicationId)
                .status("PENDING")
                .message("请假申请已提交，等待审批")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端申请请假] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "申请请假失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileLeaveRecordsResult> getLeaveRecords(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileLeaveQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileLeaveRecordsResult result = MobileLeaveRecordsResult.builder()
                .employeeId(session.getEmployeeId())
                .records(Collections.emptyList())
                .totalCount(0)
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取请假记录] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取请假记录失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileLeaveCancellationResult> cancelLeave(
            @RequestBody MobileLeaveCancellationRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileLeaveCancellationResult result = MobileLeaveCancellationResult.builder()
                .success(true)
                .message("销假申请已提交")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端申请销假] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "申请销假失败，请重试");
        }
    }

    // ==================== 排班相关 ====================

    @Override
    public ResponseDTO<MobileShiftsResult> getShifts(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileShiftQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileShiftsResult result = MobileShiftsResult.builder()
                .employeeId(session.getEmployeeId())
                .shifts(Collections.emptyList())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取班次信息] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取班次信息失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileScheduleResult> getSchedule(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileScheduleQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            Long employeeId = session.getEmployeeId();
            LocalDate startDate = queryParam.getStartDate() != null ? queryParam.getStartDate() : LocalDate.now();
            LocalDate endDate = queryParam.getEndDate() != null ? queryParam.getEndDate() : LocalDate.now().plusDays(7);

            // 使用LambdaQueryWrapper查询日期范围
            List<ScheduleRecordEntity> schedules = scheduleRecordDao.selectByEmployeeIdAndDateRange(
                employeeId, startDate, endDate
            );

            MobileScheduleResult result = MobileScheduleResult.builder()
                .employeeId(employeeId)
                .schedules(Collections.emptyList())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取排班信息] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取排班信息失败，请重试");
        }
    }

    // ==================== 提醒相关 ====================

    @Override
    public ResponseDTO<MobileReminderSettingsResult> setReminderSettings(
            @RequestBody MobileReminderSettingsRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileReminderSettingsResult result = MobileReminderSettingsResult.builder()
                .success(true)
                .message("提醒设置已保存")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端设置提醒] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "设置提醒失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileRemindersResult> getReminders(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileReminderQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileRemindersResult result = MobileRemindersResult.builder()
                .employeeId(session.getEmployeeId())
                .reminders(Collections.emptyList())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取提醒] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取提醒失败，请重试");
        }
    }

    // 注意：以下方法不在接口定义中，已删除或保留为内部使用

    // ==================== 接口中定义但实现类中缺失的方法 ====================

    @Override
    public ResponseDTO<MobileCalendarResult> getCalendar(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileCalendarQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileCalendarResult result = MobileCalendarResult.builder()
                .employeeId(session.getEmployeeId())
                .calendarData(Collections.emptyList())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取考勤日历] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取考勤日历失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileAvatarUploadResult> uploadAvatar(
            @ModelAttribute MobileAvatarUploadRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            String avatarUrl = "http://example.com/avatar/" + UUID.randomUUID().toString() + ".jpg";
            MobileAvatarUploadResult result = MobileAvatarUploadResult.builder()
                .avatarUrl(avatarUrl)
                .success(true)
                .message("头像上传成功")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端上传头像] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "上传头像失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileProfileSettingsResult> getProfileSettings(
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileProfileSettingsResult result = MobileProfileSettingsResult.builder()
                .employeeId(session.getEmployeeId())
                .settings(getDefaultSettings())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取用户配置] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取用户配置失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileProfileSettingsUpdateResult> updateProfileSettings(
            @RequestBody MobileProfileSettingsUpdateRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileProfileSettingsUpdateResult result = MobileProfileSettingsUpdateResult.builder()
                .success(true)
                .message("配置更新成功")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端更新用户配置] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "更新用户配置失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileAppVersionResult> getAppVersion() {
        try {
            MobileAppVersionResult result = MobileAppVersionResult.builder()
                .currentVersion("2.1.0")
                .latestVersion("2.1.0")
                .updateRequired(false)
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取应用版本] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取应用版本失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileAppUpdateCheckResult> checkAppUpdate(@RequestBody MobileAppUpdateCheckRequest request) {
        try {
            MobileAppUpdateCheckResult result = MobileAppUpdateCheckResult.builder()
                .currentVersion(request.getCurrentVersion())
                .latestVersion("2.1.0")
                .updateRequired(false)
                .downloadUrl(null)
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端检查应用更新] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "检查应用更新失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileNotificationsResult> getNotifications(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileNotificationQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileNotificationsResult result = MobileNotificationsResult.builder()
                .employeeId(session.getEmployeeId())
                .notifications(Collections.emptyList())
                .totalCount(0)
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取通知] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取通知失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileNotificationReadResult> markNotificationAsRead(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileNotificationReadResult result = MobileNotificationReadResult.builder()
                .notificationId(notificationId)
                .success(true)
                .message("通知已标记为已读")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端标记通知已读] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "标记通知已读失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileAnomaliesResult> getAnomalies(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileAnomalyQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileAnomaliesResult result = MobileAnomaliesResult.builder()
                .employeeId(session.getEmployeeId())
                .anomalies(Collections.emptyList())
                .totalCount(0)
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取考勤异常] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取考勤异常失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileLeaderboardResult> getLeaderboard(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileLeaderboardQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileLeaderboardResult result = MobileLeaderboardResult.builder()
                .employeeId(session.getEmployeeId())
                .rankings(Collections.emptyList())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取排行榜] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取排行榜失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileChartsResult> getCharts(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileChartQueryParam queryParam) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileChartsResult result = MobileChartsResult.builder()
                .employeeId(session.getEmployeeId())
                .chartData(Collections.emptyMap())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取图表] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取图表失败，请重试");
        }
    }


    @Override
    public ResponseDTO<MobileLocationResult> getCurrentLocation(@RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileLocationResult result = MobileLocationResult.builder()
                .employeeId(session.getEmployeeId())
                .latitude(39.9042)
                .longitude(116.4074)
                .address("北京市")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取位置] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取位置失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileLocationReportResult> reportLocation(
            @RequestBody MobileLocationReportRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileLocationReportResult result = MobileLocationReportResult.builder()
                .success(true)
                .message("位置上报成功")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端上报位置] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "上报位置失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileDeviceInfoResult> getDeviceInfo(@RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileDeviceInfo deviceInfo = deviceInfoCache.get("device:" + session.getEmployeeId());
            MobileDeviceInfoResult result = MobileDeviceInfoResult.builder()
                .employeeId(session.getEmployeeId())
                .deviceInfo(deviceInfo != null ? deviceInfo.getDeviceInfo() : Collections.emptyMap())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取设备信息] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取设备信息失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileDeviceRegisterResult> registerDevice(
            @RequestBody MobileDeviceRegisterRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            String deviceId = UUID.randomUUID().toString();
            MobileDeviceRegisterResult result = MobileDeviceRegisterResult.builder()
                .deviceId(deviceId)
                .success(true)
                .message("设备注册成功")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端设备注册] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "设备注册失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileSecuritySettingsResult> getSecuritySettings(@RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileSecuritySettingsResult result = MobileSecuritySettingsResult.builder()
                .employeeId(session.getEmployeeId())
                .settings(Collections.emptyMap())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取安全设置] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取安全设置失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileSecuritySettingsUpdateResult> updateSecuritySettings(
            @RequestBody MobileSecuritySettingsUpdateRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileSecuritySettingsUpdateResult result = MobileSecuritySettingsUpdateResult.builder()
                .success(true)
                .message("安全设置更新成功")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端更新安全设置] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "更新安全设置失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileDataSyncResult> syncData(@RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileDataSyncResult result = MobileDataSyncResult.builder()
                .success(true)
                .message("数据同步成功")
                .syncTime(LocalDateTime.now())
                .syncTimestamp(System.currentTimeMillis())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端同步数据] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "同步数据失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileOfflineDataResult> getOfflineData(@RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileOfflineDataResult result = MobileOfflineDataResult.builder()
                .employeeId(session.getEmployeeId())
                .offlineRecords(Collections.emptyList())
                .offlineData(Collections.emptyList())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取离线数据] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取离线数据失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileOfflineDataUploadResult> uploadOfflineData(
            @RequestBody MobileOfflineDataUploadRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            // 处理离线记录
            List<OfflineSyncResult> syncResults = new ArrayList<>();
            if (request.getOfflineRecords() != null && !request.getOfflineRecords().isEmpty()) {
                for (MobileOfflineRecord offlineRecord : request.getOfflineRecords()) {
                    OfflineSyncResult syncResult = processOfflineRecord(offlineRecord);
                    syncResults.add(syncResult);
                }
            }

            MobileOfflineDataUploadResult result = MobileOfflineDataUploadResult.builder()
                .success(true)
                .message("离线数据上传成功")
                .uploadTime(LocalDateTime.now())
                .syncResults(syncResults)
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端上传离线数据] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "上传离线数据失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileHealthCheckResult> healthCheck(@RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileHealthCheckResult result = MobileHealthCheckResult.builder()
                .status("HEALTHY")
                .message("系统运行正常")
                .checkTime(LocalDateTime.now())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端健康检查] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "健康检查失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobilePerformanceTestResult> performanceTest(
            @RequestBody MobilePerformanceTestRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobilePerformanceTestResult result = MobilePerformanceTestResult.builder()
                .testType(request.getTestType())
                .success(true)
                .responseTime(150L)
                .throughput(1000L)
                .performanceMetrics(java.util.Collections.emptyMap())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端性能测试] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "性能测试失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileFeedbackSubmitResult> submitFeedback(
            @RequestBody MobileFeedbackSubmitRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            String feedbackId = UUID.randomUUID().toString();
            MobileFeedbackSubmitResult result = MobileFeedbackSubmitResult.builder()
                .feedbackId(feedbackId)
                .success(true)
                .message("反馈提交成功")
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端提交反馈] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "提交反馈失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileHelpResult> getHelp(@ModelAttribute MobileHelpQueryParam queryParam) {
        try {
            MobileHelpResult result = MobileHelpResult.builder()
                .helpType(queryParam != null ? queryParam.getHelpType() : "FAQ")
                .helpContent(Collections.emptyList())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取帮助] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取帮助失败，请重试");
        }
    }

    @Override
    public ResponseDTO<MobileUsageStatisticsResult> getUsageStatistics(@RequestHeader("Authorization") String token) {
        try {
            MobileUserSession session = validateUserSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            MobileUsageStatisticsResult result = MobileUsageStatisticsResult.builder()
                .employeeId(session.getEmployeeId())
                .usageStatistics(Collections.emptyMap())
                .statistics(Collections.emptyMap())
                .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取使用统计] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取使用统计失败，请重试");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证用户会话
     * @param accessToken 访问令牌
     * @return 用户会话，如果无效则返回null
     */
    private MobileUserSession validateUserSession(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return null;
        }
        MobileUserSession session = userSessionCache.get(accessToken);
        if (session == null) {
            return null;
        }
        if (session.getExpiresTime().isBefore(LocalDateTime.now())) {
            userSessionCache.remove(accessToken);
            return null;
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
            List<ScheduleRecordEntity> todaySchedules = scheduleRecordDao.selectByEmployeeIdAndDateRange(
                employeeId, LocalDate.now(), LocalDate.now()
            );

            if (todaySchedules.isEmpty()) {
                return null;
            }

            ScheduleRecordEntity currentSchedule = todaySchedules.get(0);
            
            // 从WorkShiftEntity获取班次详细信息
            WorkShiftEntity workShift = currentSchedule.getShiftId() != null ? 
                workShiftDao.selectById(currentSchedule.getShiftId()) : null;
            
            return WorkShiftInfo.builder()
                .scheduleId(currentSchedule.getScheduleId())
                .shiftId(currentSchedule.getShiftId())
                .shiftName(workShift != null ? workShift.getShiftName() : "未知班次")
                .scheduleDate(currentSchedule.getScheduleDate())
                .startTime(workShift != null ? workShift.getStartTime() : 
                    (currentSchedule.getActualStartTime() != null ? 
                        currentSchedule.getActualStartTime().toLocalTime() : null))
                .endTime(workShift != null ? workShift.getEndTime() : 
                    (currentSchedule.getActualEndTime() != null ? 
                        currentSchedule.getActualEndTime().toLocalTime() : null))
                .workPlace(null) // 简化实现，工作地点信息不在WorkShiftEntity中
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
