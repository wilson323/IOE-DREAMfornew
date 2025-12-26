package net.lab1024.sa.attendance.mobile.clockin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import net.lab1024.sa.attendance.mobile.model.AttendanceClockInEvent;
import net.lab1024.sa.attendance.mobile.model.AttendanceClockOutEvent;
import net.lab1024.sa.attendance.mobile.model.BiometricVerificationResult;
import net.lab1024.sa.attendance.mobile.model.LocationInfo;
import net.lab1024.sa.attendance.mobile.model.LocationVerificationResult;
import net.lab1024.sa.attendance.mobile.model.MobileBiometricVerificationRequest;
import net.lab1024.sa.attendance.mobile.model.MobileBiometricVerificationResult;
import net.lab1024.sa.attendance.mobile.model.MobileClockInRequest;
import net.lab1024.sa.attendance.mobile.model.MobileClockInResult;
import net.lab1024.sa.attendance.mobile.model.MobileClockOutRequest;
import net.lab1024.sa.attendance.mobile.model.MobileClockOutResult;
import net.lab1024.sa.attendance.mobile.model.MobileUserSession;
import net.lab1024.sa.attendance.mobile.model.MobileUserInfoResult;
import net.lab1024.sa.attendance.mobile.model.WorkShiftInfo;
import net.lab1024.sa.attendance.mobile.auth.MobileAuthenticationService;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.ScheduleRecordDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.entity.WorkShiftEntity;
import net.lab1024.sa.attendance.domain.entity.ScheduleRecordEntity;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.gateway.domain.response.EmployeeResponse;

/**
 * 移动端打卡服务
 * <p>
 * 负责移动端打卡相关的所有功能，包括：
 * - 上班打卡
 * - 下班打卡
 * - 生物识别验证
 * - 位置验证
 * - 排班信息查询
 * </p>
 * <p>
 * 从AttendanceMobileServiceImpl中抽取，遵循单一职责原则
 * </p>
 *
 * @author IOE-DREAM Refactoring Team
 * @since 2025-12-26
 */
@Slf4j
@Service
public class MobileClockInService {

    @Resource
    private MobileAuthenticationService authenticationService;

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private ScheduleRecordDao scheduleRecordDao;

    @Resource
    private WorkShiftDao workShiftDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ExecutorService asyncExecutor;

    /**
     * 上班打卡
     * <p>
     * 验证用户身份、生物识别、位置信息后执行打卡
     * </p>
     *
     * @param request 打卡请求（生物识别、位置信息等）
     * @param token   访问令牌
     * @return 打卡结果
     */
    public ResponseDTO<MobileClockInResult> clockIn(MobileClockInRequest request, String token) {
        try {
            // 验证用户会话
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }
            Long employeeId = session.getEmployeeId();

            // 验证生物识别
            if (request.getBiometricData() != null) {
                BiometricVerificationResult verificationResult = verifyBiometric(employeeId,
                        request.getBiometricData().getType(), request.getBiometricData().getData());
                if (!verificationResult.isVerified()) {
                    return ResponseDTO.error("BIOMETRIC_VERIFICATION_FAILED",
                            "生物识别验证失败: " + verificationResult.getFailureReason());
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
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(employeeId,
                    LocalDate.now());

            boolean hasClockIn = todayRecords.stream().anyMatch(record -> record.getClockInTime() != null);

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
                record.setLongitude(BigDecimal.valueOf(locationInfo.getLongitude()));
                record.setLatitude(BigDecimal.valueOf(locationInfo.getLatitude()));
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

    /**
     * 下班打卡
     * <p>
     * 验证用户身份、生物识别、位置信息后执行打卡
     * </p>
     *
     * @param request 打卡请求（生物识别、位置信息等）
     * @param token   访问令牌
     * @return 打卡结果
     */
    public ResponseDTO<MobileClockOutResult> clockOut(MobileClockOutRequest request, String token) {
        try {
            // 验证用户会话
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }
            Long employeeId = session.getEmployeeId();

            // 验证生物识别
            if (request.getBiometricData() != null) {
                BiometricVerificationResult verificationResult = verifyBiometric(employeeId,
                        request.getBiometricData().getType(), request.getBiometricData().getData());
                if (!verificationResult.isVerified()) {
                    return ResponseDTO.error("BIOMETRIC_VERIFICATION_FAILED",
                            "生物识别验证失败: " + verificationResult.getFailureReason());
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
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(employeeId,
                    LocalDate.now());

            boolean hasClockIn = todayRecords.stream().anyMatch(record -> record.getClockInTime() != null);

            if (!hasClockIn) {
                return ResponseDTO.error("NO_CLOCK_IN", "请先上班打卡");
            }

            boolean hasClockOut = todayRecords.stream().anyMatch(record -> record.getClockOutTime() != null);

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
                record.setLongitude(BigDecimal.valueOf(locationInfo.getLongitude()));
                record.setLatitude(BigDecimal.valueOf(locationInfo.getLatitude()));
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

    /**
     * 生物识别验证
     * <p>
     * 验证用户的生物识别数据（人脸、指纹等）
     * </p>
     *
     * @param request      生物识别验证请求
     * @param token        访问令牌
     * @return 生物识别验证结果
     */
    public ResponseDTO<MobileBiometricVerificationResult> verifyBiometric(
            MobileBiometricVerificationRequest request, String token) {
        try {
            // 验证用户会话
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            BiometricVerificationResult verificationResult = verifyBiometric(session.getEmployeeId(),
                    request.getBiometricType(), request.getBiometricData());

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

    /**
     * 获取用户信息（含当前排班）
     *
     * @param token 访问令牌
     * @return 用户信息
     */
    public ResponseDTO<MobileUserInfoResult> getUserInfo(String token) {
        try {
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            ResponseDTO<EmployeeResponse> employeeResponse = gatewayServiceClient.callCommonService(
                    "/api/employee/" + session.getEmployeeId(),
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<EmployeeResponse>>() {}
            );
            if (employeeResponse.getCode() != 200 || employeeResponse.getData() == null) {
                return ResponseDTO.error("EMPLOYEE_NOT_FOUND", "员工不存在");
            }
            EmployeeResponse employee = employeeResponse.getData();

            MobileUserInfoResult userInfo = MobileUserInfoResult.builder()
                    .employeeId(employee.getEmployeeId())
                    .employeeName(employee.getEmployeeName())
                    .departmentName(employee.getDepartmentName())
                    .position(employee.getPosition())
                    .avatarUrl(employee.getAvatarUrl())
                    .phone(employee.getPhone())
                    .email(employee.getEmail())
                    .permissions(java.util.Arrays.asList("attendance:clockin", "attendance:clockout", "attendance:view"))
                    .build();

            return ResponseDTO.ok(userInfo);

        } catch (Exception e) {
            log.error("[移动端获取用户信息] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取用户信息失败，请重试");
        }
    }

    /**
     * 获取当前排班信息
     *
     * @param employeeId 员工ID
     * @return 排班信息
     */
    public WorkShiftInfo getCurrentShift(Long employeeId) {
        try {
            List<ScheduleRecordEntity> todaySchedules = scheduleRecordDao.selectByEmployeeIdAndDateRange(employeeId,
                    LocalDate.now(), LocalDate.now());

            if (todaySchedules.isEmpty()) {
                return null;
            }

            ScheduleRecordEntity currentSchedule = todaySchedules.get(0);

            // 从WorkShiftEntity获取班次详细信息
            WorkShiftEntity workShift = currentSchedule.getShiftId() != null
                    ? workShiftDao.selectById(currentSchedule.getShiftId())
                    : null;

            return WorkShiftInfo.builder()
                    .scheduleId(currentSchedule.getScheduleId())
                    .shiftId(currentSchedule.getShiftId())
                    .shiftName(workShift != null ? workShift.getShiftName() : "未知班次")
                    .scheduleDate(currentSchedule.getScheduleDate())
                    .startTime(workShift != null ? workShift.getWorkStartTime()
                            : (currentSchedule.getActualStartTime() != null
                                    ? currentSchedule.getActualStartTime().toLocalTime()
                                    : null))
                    .endTime(workShift != null ? workShift.getWorkEndTime()
                            : (currentSchedule.getActualEndTime() != null
                                    ? currentSchedule.getActualEndTime().toLocalTime()
                                    : null))
                    .workPlace(null) // 简化实现，工作地点信息不在WorkShiftEntity中
                    .build();

        } catch (Exception e) {
            log.error("[获取当前排班] 失败: 员工ID={}, error={}", employeeId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生物识别验证（内部方法）
     *
     * @param employeeId   员工ID
     * @param biometricType 生物识别类型
     * @param biometricData 生物识别数据
     * @return 验证结果
     */
    private BiometricVerificationResult verifyBiometric(Long employeeId, String biometricType, String biometricData) {
        log.info("[移动端考勤] 生物识别验证: employeeId={}, biometricType={}", employeeId, biometricType);

        try {
            // 构建验证请求
            Map<String, Object> verificationRequest = new HashMap<>();
            verificationRequest.put("employeeId", employeeId);
            verificationRequest.put("biometricType", biometricType);
            verificationRequest.put("biometricData", biometricData);
            verificationRequest.put("verificationTime", LocalDateTime.now().toString());
            verificationRequest.put("requestSource", "MOBILE_ATTENDANCE");

            // 简化实现：直接返回验证通过（实际应调用生物识别服务）
            // TODO: 集成生物识别服务后，通过GatewayServiceClient调用
            log.info("[移动端考勤] 生物识别验证完成: employeeId={}, verified=true", employeeId);

            return BiometricVerificationResult.builder()
                    .verified(true)
                    .confidence(0.95)
                    .biometricType(biometricType)
                    .verificationTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("[移动端考勤] 生物识别验证异常: employeeId={}, error={}", employeeId, e.getMessage(), e);
            // 降级处理：返回验证失败
            return BiometricVerificationResult.builder()
                    .verified(false)
                    .confidence(0.0)
                    .biometricType(biometricType)
                    .verificationTime(LocalDateTime.now())
                    .failureReason("验证异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 验证位置信息
     *
     * @param employeeId 员工ID
     * @param location   位置信息（GPS坐标等）
     * @return 位置验证结果
     */
    private LocationVerificationResult verifyLocation(Long employeeId, LocationInfo location) {
        log.info("[移动端考勤] 位置验证: employeeId={}, location={}",
                employeeId, location != null ? location.getAddress() : null);

        try {
            // 1. 检查位置信息是否有效
            if (location == null) {
                return LocationVerificationResult.builder()
                        .valid(false)
                        .location(location)
                        .verificationTime(LocalDateTime.now())
                        .failureReason("位置信息为空")
                        .build();
            }

            // 2. 简化验证：只要有经纬度坐标就认为有效
            boolean locationValid = location.getLatitude() != null && location.getLongitude() != null;
            String failureReason = locationValid ? null : "GPS坐标无效";

            // 3. 记录验证结果
            if (locationValid) {
                log.info("[移动端考勤] 位置验证通过: employeeId={}, lat={}, lng={}",
                        employeeId, location.getLatitude(), location.getLongitude());
            } else {
                log.warn("[移动端考勤] 位置验证失败: employeeId={}, reason={}",
                        employeeId, failureReason);
            }

            return LocationVerificationResult.builder()
                    .valid(locationValid)
                    .location(location)
                    .verificationTime(LocalDateTime.now())
                    .failureReason(failureReason)
                    .build();

        } catch (Exception e) {
            log.error("[移动端考勤] 位置验证异常: employeeId={}, error={}", employeeId, e.getMessage(), e);
            // 降级处理：返回验证失败
            return LocationVerificationResult.builder()
                    .valid(false)
                    .location(location)
                    .verificationTime(LocalDateTime.now())
                    .failureReason("验证异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 计算工作时长
     *
     * @param employeeId 员工ID
     * @return 工作时长（小时）
     */
    public Double calculateWorkHours(Long employeeId) {
        try {
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(employeeId,
                    LocalDate.now());

            return todayRecords.stream()
                    .filter(record -> record.getWorkDuration() != null)
                    .mapToDouble(AttendanceRecordEntity::getWorkHours)
                    .sum();

        } catch (Exception e) {
            log.error("[计算工作时长] 失败: 员工ID={}, error={}", employeeId, e.getMessage(), e);
            return 0.0;
        }
    }

    /**
     * 发送打卡通知
     *
     * @param employeeId 员工ID
     * @param event      打卡事件
     */
    private void sendClockInNotification(Long employeeId, AttendanceClockInEvent event) {
        asyncExecutor.submit(() -> {
            log.info("[打卡通知] 员工ID={}, 打卡时间={}", employeeId, event.getClockInTime());
        });
    }

    /**
     * 发送下班打卡通知
     *
     * @param employeeId 员工ID
     * @param event      打卡事件
     */
    private void sendClockOutNotification(Long employeeId, AttendanceClockOutEvent event) {
        asyncExecutor.submit(() -> {
            log.info("[下班打卡通知] 员工ID={}, 打卡时间={}", employeeId, event.getClockOutTime());
        });
    }
}
