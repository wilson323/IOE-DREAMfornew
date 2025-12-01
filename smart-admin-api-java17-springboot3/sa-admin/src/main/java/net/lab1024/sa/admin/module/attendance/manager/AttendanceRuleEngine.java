package net.lab1024.sa.admin.module.attendance.manager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceRecordRepository;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceRuleRepository;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceScheduleRepository;
import net.lab1024.sa.admin.module.attendance.domain.result.AttendanceRuleProcessResult;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.exception.SmartException;

/**
 * 考勤规则引擎
 *
 * 严格遵循repowiki规范:
 * - 规则驱动的考勤计算引擎
 * - 支持多种规则类型和优先级
 * - 异常检测和自动处理
 * - 统计计算和分析
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Component
public class AttendanceRuleEngine {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttendanceRuleEngine.class);

    @Resource
    private AttendanceRuleRepository attendanceRuleRepository;

    @Resource
    private AttendanceScheduleRepository attendanceScheduleRepository;

    @Resource
    private AttendanceRecordRepository attendanceRecordRepository;

    @Resource
    private AttendanceCacheManager attendanceCacheManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 计算员工考勤记录
     *
     * @param employeeId     员工ID
     * @param departmentId   部门ID
     * @param employeeType   员工类型
     * @param attendanceDate 考勤日期
     * @param punchInTime    上班打卡时间
     * @param punchOutTime   下班打卡时间
     * @return 考勤记录
     */
    public AttendanceRecordEntity calculateAttendanceRecord(Long employeeId,
            Long departmentId,
            String employeeType,
            LocalDate attendanceDate,
            LocalTime punchInTime,
            LocalTime punchOutTime) {
        try {
            log.debug("开始计算员工考勤记录，employeeId: {}, attendanceDate: {}", employeeId, attendanceDate);

            // 获取适用的规则
            Optional<AttendanceRuleEntity> ruleOptional = attendanceCacheManager.getTodayRule(employeeId, departmentId,
                    employeeType);
            if (ruleOptional.isEmpty()) {
                log.warn("未找到员工{}的考勤规则", employeeId);
                return createDefaultRecord(employeeId, attendanceDate, punchInTime, punchOutTime);
            }

            AttendanceRuleEntity rule = ruleOptional.get();
            AttendanceRecordEntity record = new AttendanceRecordEntity();

            // 设置基本信息
            record.setEmployeeId(employeeId);
            record.setAttendanceDate(attendanceDate);
            record.setPunchInTime(punchInTime);
            record.setPunchOutTime(punchOutTime);

            // 获取排班信息
            Optional<AttendanceScheduleEntity> scheduleOptional = attendanceCacheManager.getSchedule(employeeId,
                    attendanceDate);

            // 计算考勤状态和异常
            calculateAttendanceStatus(record, rule, scheduleOptional.orElse(null));

            // 计算工作时长
            calculateWorkHours(record, rule, scheduleOptional.orElse(null));

            return record;

        } catch (Exception e) {
            log.error("计算考勤记录失败", e);
            throw new SmartException(UserErrorCode.DATA_ERROR);
        }
    }

    /**
     * 计算考勤状态和异常
     *
     * @param record   考勤记录
     * @param rule     考勤规则
     * @param schedule 排班信息
     */
    private void calculateAttendanceStatus(AttendanceRecordEntity record,
            AttendanceRuleEntity rule,
            AttendanceScheduleEntity schedule) {
        LocalTime punchInTime = record.getPunchInTime();
        LocalTime punchOutTime = record.getPunchOutTime();

        // 默认状态为旷工
        String attendanceStatus = "ABSENT";
        String exceptionType = null;

        // 检查是否有排班
        boolean hasSchedule = schedule != null && schedule.isWorkDay();
        if (!hasSchedule) {
            // 无排班，根据打卡情况判断
            if (punchInTime != null || punchOutTime != null) {
                attendanceStatus = "NORMAL";
            }
        } else {
            // 有排班，根据规则判断
            if (schedule == null) {
                // 防御性检查：虽然理论上不应该为null，但为了安全起见
                if (punchInTime != null || punchOutTime != null) {
                    attendanceStatus = "NORMAL";
                }
                record.setAttendanceStatus(attendanceStatus);
                record.setExceptionType(exceptionType);
                return;
            }

            LocalTime workStartTime = schedule.getWorkStartTime();
            LocalTime workEndTime = schedule.getWorkEndTime();

            if (punchInTime == null && punchOutTime == null) {
                attendanceStatus = "ABSENT";
                exceptionType = "ABSENTEEISM";
            } else if (punchInTime == null) {
                attendanceStatus = "ABNORMAL";
                exceptionType = "FORGET_PUNCH";
            } else if (punchOutTime == null) {
                attendanceStatus = "ABNORMAL";
                exceptionType = "FORGET_PUNCH";
            } else {
                // 有完整打卡记录，判断是否迟到早退
                boolean isLate = isLate(punchInTime, workStartTime, rule.getLateTolerance());
                boolean isEarlyLeave = isEarlyLeave(punchOutTime, workEndTime, rule.getEarlyTolerance());

                if (isLate && isEarlyLeave) {
                    attendanceStatus = "ABNORMAL";
                    exceptionType = "LATE_EARLY_LEAVE";
                } else if (isLate) {
                    attendanceStatus = "LATE";
                    exceptionType = "LATE";
                } else if (isEarlyLeave) {
                    attendanceStatus = "EARLY_LEAVE";
                    exceptionType = "EARLY_LEAVE";
                } else {
                    attendanceStatus = "NORMAL";
                    exceptionType = null;
                }
            }
        }

        record.setAttendanceStatus(attendanceStatus);
        record.setExceptionType(exceptionType);
    }

    /**
     * 计算工作时长
     *
     * @param record   考勤记录
     * @param rule     考勤规则
     * @param schedule 排班信息
     */
    private void calculateWorkHours(AttendanceRecordEntity record,
            AttendanceRuleEntity rule,
            AttendanceScheduleEntity schedule) {
        LocalTime punchInTime = record.getPunchInTime();
        LocalTime punchOutTime = record.getPunchOutTime();

        if (punchInTime == null || punchOutTime == null) {
            record.setWorkHours(BigDecimal.ZERO);
            record.setOvertimeHours(BigDecimal.ZERO);
            return;
        }

        // 计算基本工作时长
        BigDecimal workHours = calculateBasicWorkHours(punchInTime, punchOutTime, schedule);
        record.setWorkHours(workHours);

        // 计算加班时长
        BigDecimal overtimeHours = calculateOvertimeHours(punchInTime, punchOutTime, schedule, rule);
        record.setOvertimeHours(overtimeHours);
    }

    /**
     * 计算基本工作时长
     */
    private BigDecimal calculateBasicWorkHours(LocalTime punchInTime, LocalTime punchOutTime,
            AttendanceScheduleEntity schedule) {
        if (schedule == null) {
            // 无排班，按实际打卡时间计算
            Duration duration = Duration.between(punchInTime, punchOutTime);
            long minutes = duration.toMinutes();
            return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }

        LocalTime workStartTime = schedule.getWorkStartTime();
        LocalTime breakStartTime = schedule.getBreakStartTime();
        LocalTime breakEndTime = schedule.getBreakEndTime();

        // 处理跨天工作
        LocalTime actualEndTime = punchOutTime;
        if (actualEndTime.isBefore(workStartTime)) {
            actualEndTime = actualEndTime.plusHours(24);
        }

        // 计算总工作时长
        Duration totalDuration = Duration.between(punchInTime, actualEndTime);
        BigDecimal totalHours = BigDecimal.valueOf(totalDuration.toMinutes()).divide(BigDecimal.valueOf(60), 2,
                RoundingMode.HALF_UP);

        // 扣除休息时间
        if (breakStartTime != null && breakEndTime != null) {
            Duration breakDuration = Duration.between(breakStartTime, breakEndTime);
            BigDecimal breakHours = BigDecimal.valueOf(breakDuration.toMinutes()).divide(BigDecimal.valueOf(60), 2,
                    RoundingMode.HALF_UP);
            totalHours = totalHours.subtract(breakHours);
        }

        return totalHours;
    }

    /**
     * 计算加班时长
     */
    private BigDecimal calculateOvertimeHours(LocalTime punchInTime, LocalTime punchOutTime,
            AttendanceScheduleEntity schedule, AttendanceRuleEntity rule) {
        if (schedule == null || schedule.getWorkStartTime() == null || schedule.getWorkEndTime() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal standardWorkHours = schedule.getWorkHours();

        if (standardWorkHours == null || standardWorkHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 计算实际工作时长
        BigDecimal actualWorkHours = calculateBasicWorkHours(punchInTime, punchOutTime, schedule);

        // 计算加班时长
        BigDecimal overtimeHours = actualWorkHours.subtract(standardWorkHours);
        if (overtimeHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return overtimeHours;
    }

    /**
     * 判断是否迟到
     */
    private boolean isLate(LocalTime punchInTime, LocalTime workStartTime, Integer lateTolerance) {
        if (punchInTime == null || workStartTime == null) {
            return false;
        }

        Duration tolerance = Duration.ofMinutes(lateTolerance != null ? lateTolerance : 0);
        Duration lateness = Duration.between(workStartTime, punchInTime);

        return lateness.compareTo(tolerance) > 0;
    }

    /**
     * 判断是否早退
     */
    private boolean isEarlyLeave(LocalTime punchOutTime, LocalTime workEndTime, Integer earlyTolerance) {
        if (punchOutTime == null || workEndTime == null) {
            return false;
        }

        Duration tolerance = Duration.ofMinutes(earlyTolerance != null ? earlyTolerance : 0);
        Duration earliness = Duration.between(punchOutTime, workEndTime);

        return earliness.compareTo(tolerance) > 0;
    }

    /**
     * 创建默认考勤记录
     */
    private AttendanceRecordEntity createDefaultRecord(Long employeeId, LocalDate attendanceDate,
            LocalTime punchInTime, LocalTime punchOutTime) {
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setEmployeeId(employeeId);
        record.setAttendanceDate(attendanceDate);
        record.setPunchInTime(punchInTime);
        record.setPunchOutTime(punchOutTime);

        // 简单状态计算
        if (punchInTime != null && punchOutTime != null) {
            record.setAttendanceStatus("NORMAL");
        } else {
            record.setAttendanceStatus("ABNORMAL");
            record.setExceptionType("INCOMPLETE_RECORD");
        }

        return record;
    }

    /**
     * 验证打卡位置
     *
     * @param employeeId 员工ID
     * @param latitude   纬度
     * @param longitude  经度
     * @return 是否在允许范围内
     */
    public boolean validateLocation(Long employeeId, Double latitude, Double longitude) {
        try {
            // 获取员工适用的规则
            Optional<AttendanceRuleEntity> ruleOptional = attendanceCacheManager.getTodayRule(employeeId, null, null);
            if (ruleOptional.isEmpty()) {
                return true; // 无规则，不验证
            }

            AttendanceRuleEntity rule = ruleOptional.get();
            if (rule.getGpsValidation() == null || rule.getGpsValidation() != 1) {
                return true; // 未启用GPS验证
            }

            // 解析GPS位置配置
            String gpsLocations = rule.getGpsLocations();
            if (gpsLocations == null || gpsLocations.trim().isEmpty()) {
                return true;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> locations = (List<Map<String, Object>>) objectMapper.readValue(
                    gpsLocations,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            Integer gpsRange = rule.getGpsRange() != null ? rule.getGpsRange() : 100;

            // 检查是否在任何允许的位置范围内
            for (Map<String, Object> location : locations) {
                Double locationLat = ((Number) location.get("latitude")).doubleValue();
                Double locationLng = ((Number) location.get("longitude")).doubleValue();

                double distance = calculateDistance(latitude, longitude, locationLat, locationLng);
                if (distance <= gpsRange) {
                    return true;
                }
            }

            return false;

        } catch (JsonProcessingException e) {
            log.error("解析GPS位置配置失败", e);
            return true; // 解析失败，不验证
        } catch (Exception e) {
            log.error("验证打卡位置失败", e);
            return false;
        }
    }

    /**
     * 计算两点间距离（米）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // 地球半径（米）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    /**
     * 检查是否允许的打卡设备
     *
     * @param employeeId 员工ID
     * @param deviceId   设备ID
     * @return 是否允许
     */
    public boolean validateDevice(Long employeeId, String deviceId) {
        try {
            // 获取员工适用的规则
            Optional<AttendanceRuleEntity> ruleOptional = attendanceCacheManager.getTodayRule(employeeId, null, null);
            if (ruleOptional.isEmpty()) {
                return true; // 无规则，不验证
            }

            AttendanceRuleEntity rule = ruleOptional.get();
            String deviceRestrictions = rule.getDeviceRestrictions();
            if (deviceRestrictions == null || deviceRestrictions.trim().isEmpty()) {
                return true; // 无设备限制
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> restrictions = (Map<String, Object>) objectMapper.readValue(
                    deviceRestrictions,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

            @SuppressWarnings("unchecked")
            List<String> allowedDevices = (List<String>) restrictions.get("allowedDevices");

            if (allowedDevices == null || allowedDevices.isEmpty()) {
                return true; // 无限制设备
            }

            return allowedDevices.contains(deviceId);

        } catch (JsonProcessingException e) {
            log.error("解析设备限制配置失败", e);
            return true; // 解析失败，不验证
        } catch (Exception e) {
            log.error("验证打卡设备失败", e);
            return false;
        }
    }

    /**
     * 批量计算考勤记录
     *
     * @param employeeId   员工ID
     * @param departmentId 部门ID
     * @param employeeType 员工类型
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 计算结果列表
     */
    public List<AttendanceRecordEntity> batchCalculateAttendanceRecords(Long employeeId,
            Long departmentId,
            String employeeType,
            LocalDate startDate,
            LocalDate endDate) {
        List<AttendanceRecordEntity> records = new ArrayList<>();

        try {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                // 获取当天的打卡记录
                Optional<AttendanceRecordEntity> existingRecord = attendanceRecordRepository
                        .findByEmployeeAndDate(employeeId, date);

                if (existingRecord.isPresent()) {
                    AttendanceRecordEntity record = existingRecord.get();

                    // 重新计算状态和时长
                    Optional<AttendanceRuleEntity> ruleOptional = attendanceCacheManager.getTodayRule(employeeId,
                            departmentId, employeeType);
                    Optional<AttendanceScheduleEntity> scheduleOptional = attendanceCacheManager.getSchedule(employeeId,
                            date);

                    calculateAttendanceStatus(record, ruleOptional.orElse(null), scheduleOptional.orElse(null));
                    calculateWorkHours(record, ruleOptional.orElse(null), scheduleOptional.orElse(null));

                    records.add(record);
                }
            }
        } catch (Exception e) {
            log.error("批量计算考勤记录失败", e);
            throw new SmartException(UserErrorCode.DATA_ERROR);
        }

        return records;
    }

    /**
     * 获取规则配置
     *
     * @param ruleId 规则ID
     * @return 规则配置Map
     */
    public Map<String, Object> getRuleConfiguration(Long ruleId) {
        try {
            Optional<AttendanceRuleEntity> ruleOptional = attendanceRuleRepository
                    .selectApplicableRules(null, null, null, LocalDate.now())
                    .stream()
                    .filter(rule -> rule.getRuleId().equals(ruleId))
                    .findFirst();

            if (ruleOptional.isEmpty()) {
                return new HashMap<>();
            }

            AttendanceRuleEntity rule = ruleOptional.get();
            Map<String, Object> config = new HashMap<>();

            // 基本配置
            config.put("ruleId", rule.getRuleId());
            config.put("ruleName", rule.getRuleName());
            config.put("ruleType", rule.getRuleType());
            config.put("lateTolerance", rule.getLateTolerance());
            config.put("earlyTolerance", rule.getEarlyTolerance());
            config.put("gpsValidation", rule.getGpsValidation());
            config.put("photoRequired", rule.getPhotoRequired());
            config.put("faceRecognition", rule.getFaceRecognition());
            config.put("autoApproval", rule.getAutoApproval());

            // JSON配置
            if (rule.getWorkSchedule() != null && !rule.getWorkSchedule().trim().isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> workSchedule = (Map<String, Object>) objectMapper.readValue(
                        rule.getWorkSchedule(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                config.put("workSchedule", workSchedule);
            }

            if (rule.getOvertimeRules() != null && !rule.getOvertimeRules().trim().isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> overtimeRules = (Map<String, Object>) objectMapper.readValue(
                        rule.getOvertimeRules(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                config.put("overtimeRules", overtimeRules);
            }

            if (rule.getGpsLocations() != null && !rule.getGpsLocations().trim().isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> gpsLocations = (List<Map<String, Object>>) objectMapper.readValue(
                        rule.getGpsLocations(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                config.put("gpsLocations", gpsLocations);
            }

            if (rule.getNotificationSettings() != null && !rule.getNotificationSettings().trim().isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> notificationSettings = (Map<String, Object>) objectMapper.readValue(
                        rule.getNotificationSettings(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                config.put("notificationSettings", notificationSettings);
            }

            return config;

        } catch (Exception e) {
            log.error("获取规则配置失败，ruleId: {}", ruleId, e);
            return new HashMap<>();
        }
    }

    /**
     * 处理考勤记录
     *
     * @param record 考勤记录
     * @return 处理结果
     */
    public AttendanceRuleProcessResult processAttendanceRecord(AttendanceRecordEntity record) {
        try {
            if (record == null) {
                return AttendanceRuleProcessResult.failure("RECORD_NULL", "考勤记录不能为空");
            }

            // 基本验证
            if (record.getEmployeeId() == null || record.getAttendanceDate() == null) {
                return AttendanceRuleProcessResult.failure("INVALID_RECORD", "员工ID或考勤日期不能为空");
            }

            // 检查是否有适用的规则
            Optional<AttendanceRuleEntity> ruleOpt = attendanceRuleRepository.findByEmployeeId(
                record.getEmployeeId());

            if (!ruleOpt.isPresent()) {
                log.warn("未找到适用的考勤规则: employeeId={}", record.getEmployeeId());
                return AttendanceRuleProcessResult.success();
            }

            AttendanceRuleEntity rule = ruleOpt.get();

            // 应用考勤规则逻辑
            applyAttendanceRules(record, rule);

            return AttendanceRuleProcessResult.success(rule.getRuleId(), rule.getRuleName());

        } catch (Exception e) {
            log.error("处理考勤记录失败: employeeId={}, attendanceDate={}",
                     record.getEmployeeId(), record.getAttendanceDate(), e);
            return AttendanceRuleProcessResult.failure("PROCESS_ERROR", "处理考勤记录时发生错误: " + e.getMessage());
        }
    }

    /**
     * 应用考勤规则
     *
     * @param record 考勤记录
     * @param rule 考勤规则
     */
    private void applyAttendanceRules(AttendanceRecordEntity record, AttendanceRuleEntity rule) {
        // 简单的规则应用逻辑
        // 可以根据具体需求扩展

        // 计算迟到时间（如果有上班打卡时间）
        if (record.getPunchInTime() != null && rule.getWorkStartTime() != null) {
            LocalTime workStartTime = LocalTime.parse(rule.getWorkStartTime());
            if (record.getPunchInTime().isAfter(workStartTime)) {
                Duration lateDuration = Duration.between(workStartTime, record.getPunchInTime());
                long lateMinutes = lateDuration.toMinutes();

                if (lateMinutes > (rule.getLateTolerance() != null ? rule.getLateTolerance() : 0)) {
                    record.setAttendanceStatus("LATE");
                    record.setExceptionType("LATE");
                    // 这里可以设置迟到分钟字段，如果实体有的话
                }
            }
        }

        // 计算早退时间（如果有下班打卡时间）
        if (record.getPunchOutTime() != null && rule.getWorkEndTime() != null) {
            LocalTime workEndTime = LocalTime.parse(rule.getWorkEndTime());
            if (record.getPunchOutTime().isBefore(workEndTime)) {
                Duration earlyDuration = Duration.between(record.getPunchOutTime(), workEndTime);
                long earlyMinutes = earlyDuration.toMinutes();

                if (earlyMinutes > (rule.getEarlyTolerance() != null ? rule.getEarlyTolerance() : 0)) {
                    if ("LATE".equals(record.getAttendanceStatus())) {
                        record.setAttendanceStatus("ABNORMAL");
                        record.setExceptionType("LATE_EARLY_LEAVE");
                    } else {
                        record.setAttendanceStatus("EARLY_LEAVE");
                        record.setExceptionType("EARLY_LEAVE");
                    }
                }
            }
        }

        // 如果没有异常，设置为正常
        if (record.getExceptionType() == null || record.getExceptionType().isEmpty()) {
            record.setAttendanceStatus("NORMAL");
        }
    }
}
