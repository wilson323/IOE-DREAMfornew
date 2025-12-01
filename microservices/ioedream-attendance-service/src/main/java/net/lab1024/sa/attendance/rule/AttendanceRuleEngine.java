package net.lab1024.sa.attendance.rule;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.domain.entity.AttendanceRuleEntity;

/**
 * 考勤规则引擎
 * 处理考勤相关的业务规则，包括迟到、早退、加班等判定逻辑
 * 严格遵循repowiki规范，使用jakarta包和@Resource注入
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-28
 */
@Slf4j
@Data
public class AttendanceRuleEngine {

    /**
     * 考勤规则配置
     */
    private Map<String, AttendanceRule> attendanceRules = new HashMap<>();

    /**
     * 异常类型定义
     */
    public enum ExceptionType {
        LATE("LATE", "迟到"),
        EARLY_LEAVE("EARLY_LEAVE", "早退"),
        ABSENTEEISM("ABSENTEEISM", "旷工"),
        FORGET_PUNCH("FORGET_PUNCH", "忘打卡"),
        OVERWORK("OVERWORK", "加班"),
        WORK_OVERTIME("WORK_OVERTIME", "工作超时");

        private final String code;
        private final String description;

        ExceptionType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 考勤规则类
     */
    @Data
    public static class AttendanceRule {
        private Long ruleId;
        private String ruleName;
        private String ruleType; // LATE_RULE, EARLY_LEAVE_RULE, OVERTIME_RULE
        private LocalTime standardWorkStart; // 标准上班时间
        private LocalTime standardWorkEnd; // 标准下班时间
        private LocalTime gracePeriodStart; // 宽限开始时间
        private LocalTime gracePeriodEnd; // 宽限结束时间
        private Integer lateThresholdMinutes; // 迟到阈值(分钟)
        private Integer earlyLeaveThresholdMinutes; // 早退阈值(分钟)
        private BigDecimal overtimeThresholdHours; // 加班阈值(小时)
        private Boolean enableFlexTime; // 是否启用弹性时间
        private LocalTime flexWorkStart; // 弹性上班最早时间
        private LocalTime flexWorkEnd; // 弹性下班最晚时间
        private List<String> ignoreExceptionDates; // 忽略异常的日期
        private Map<String, Object> customRules; // 自定义规则
    }

    /**
     * 考勤分析结果
     */
    public static class AttendanceAnalysisResult {
        private String attendanceStatus; // 考勤状态：NORMAL, LATE, EARLY_LEAVE, ABSENT, etc.
        private String exceptionType; // 异常类型
        private String exceptionReason; // 异常原因
        private BigDecimal workHours; // 工作时长
        private BigDecimal overtimeHours; // 加班时长
        private BigDecimal lateMinutes; // 迟到分钟数
        private BigDecimal earlyLeaveMinutes; // 早退分钟数
        private Boolean isValidRecord; // 是否有效记录
        private List<String> warnings; // 警告信息
        private Map<String, Object> analysisDetails; // 分析详情

        // Getters and Setters
        public String getAttendanceStatus() {
            return attendanceStatus;
        }

        public void setAttendanceStatus(String attendanceStatus) {
            this.attendanceStatus = attendanceStatus;
        }

        public String getExceptionType() {
            return exceptionType;
        }

        public void setExceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
        }

        public String getExceptionReason() {
            return exceptionReason;
        }

        public void setExceptionReason(String exceptionReason) {
            this.exceptionReason = exceptionReason;
        }

        public BigDecimal getWorkHours() {
            return workHours;
        }

        public void setWorkHours(BigDecimal workHours) {
            this.workHours = workHours;
        }

        public BigDecimal getOvertimeHours() {
            return overtimeHours;
        }

        public void setOvertimeHours(BigDecimal overtimeHours) {
            this.overtimeHours = overtimeHours;
        }

        public BigDecimal getLateMinutes() {
            return lateMinutes;
        }

        public void setLateMinutes(BigDecimal lateMinutes) {
            this.lateMinutes = lateMinutes;
        }

        public BigDecimal getEarlyLeaveMinutes() {
            return earlyLeaveMinutes;
        }

        public void setEarlyLeaveMinutes(BigDecimal earlyLeaveMinutes) {
            this.earlyLeaveMinutes = earlyLeaveMinutes;
        }

        public Boolean getValidRecord() {
            return isValidRecord;
        }

        public void setValidRecord(Boolean validRecord) {
            this.isValidRecord = validRecord;
        }

        public void setValidRecord(boolean validRecord) {
            this.isValidRecord = validRecord;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }

        public Map<String, Object> getAnalysisDetails() {
            return analysisDetails;
        }

        public void setAnalysisDetails(Map<String, Object> analysisDetails) {
            this.analysisDetails = analysisDetails;
        }
    }

    /**
     * 初始化考勤规则引擎
     */
    public void initializeEngine() {
        log.info("考勤规则引擎初始化开始");

        // 加载默认考勤规则
        loadDefaultAttendanceRules();

        log.info("考勤规则引擎初始化完成，加载了{}个考勤规则", attendanceRules.size());
    }

    /**
     * 加载默认考勤规则
     */
    private void loadDefaultAttendanceRules() {
        // 标准迟到规则
        AttendanceRule lateRule = new AttendanceRule();
        lateRule.setRuleId(1L);
        lateRule.setRuleName("标准迟到规则");
        lateRule.setRuleType("LATE_RULE");
        lateRule.setStandardWorkStart(LocalTime.of(9, 0));
        lateRule.setGracePeriodStart(LocalTime.of(9, 5));
        lateRule.setLateThresholdMinutes(5);
        lateRule.setEnableFlexTime(false);
        attendanceRules.put("STANDARD_LATE", lateRule);

        // 标准早退规则
        AttendanceRule earlyLeaveRule = new AttendanceRule();
        earlyLeaveRule.setRuleId(2L);
        earlyLeaveRule.setRuleName("标准早退规则");
        earlyLeaveRule.setRuleType("EARLY_LEAVE_RULE");
        earlyLeaveRule.setStandardWorkEnd(LocalTime.of(18, 0));
        earlyLeaveRule.setGracePeriodEnd(LocalTime.of(17, 55));
        earlyLeaveRule.setEarlyLeaveThresholdMinutes(5);
        earlyLeaveRule.setEnableFlexTime(false);
        attendanceRules.put("STANDARD_EARLY_LEAVE", earlyLeaveRule);

        // 弹性工作时间规则
        AttendanceRule flexRule = new AttendanceRule();
        flexRule.setRuleId(3L);
        flexRule.setRuleName("弹性工作时间规则");
        flexRule.setRuleType("FLEX_RULE");
        flexRule.setStandardWorkStart(LocalTime.of(9, 0));
        flexRule.setStandardWorkEnd(LocalTime.of(18, 0));
        flexRule.setFlexWorkStart(LocalTime.of(8, 0));
        flexRule.setFlexWorkEnd(LocalTime.of(20, 0));
        flexRule.setEnableFlexTime(true);
        attendanceRules.put("FLEXIBLE_TIME", flexRule);

        // 加班规则
        AttendanceRule overtimeRule = new AttendanceRule();
        overtimeRule.setRuleId(4L);
        overtimeRule.setRuleName("加班规则");
        overtimeRule.setRuleType("OVERTIME_RULE");
        overtimeRule.setStandardWorkEnd(LocalTime.of(18, 0));
        overtimeRule.setOvertimeThresholdHours(BigDecimal.valueOf(1.0));
        attendanceRules.put("OVERTIME_RULE", overtimeRule);

        log.debug("加载了{}个默认考勤规则", attendanceRules.size());
    }

    /**
     * 分析考勤记录
     *
     * @param punchInTime       上班打卡时间
     * @param punchOutTime      下班打卡时间
     * @param workStartRequired 标准上班时间
     * @param workEndRequired   标准下班时间
     * @param ruleNames         适用的规则名称列表
     * @return 考勤分析结果
     */
    public AttendanceAnalysisResult analyzeAttendance(LocalTime punchInTime, LocalTime punchOutTime,
            LocalTime workStartRequired, LocalTime workEndRequired,
            List<String> ruleNames) {
        log.debug("开始分析考勤记录：上班{}，下班{}，标准时间{}-{}",
                punchInTime, punchOutTime, workStartRequired, workEndRequired);

        AttendanceAnalysisResult result = new AttendanceAnalysisResult();
        result.setValidRecord(true);
        result.setWarnings(new ArrayList<>());
        result.setAnalysisDetails(new HashMap<>());

        // 检查打卡完整性
        if (punchInTime == null && punchOutTime == null) {
            result.setAttendanceStatus("ABSENT");
            result.setExceptionType(ExceptionType.ABSENTEEISM.getCode());
            result.setExceptionReason("未打卡");
            result.setValidRecord(false);
            return result;
        }

        // 分析迟到情况
        analyzeLateStatus(punchInTime, workStartRequired, ruleNames, result);

        // 分析早退情况
        analyzeEarlyLeaveStatus(punchOutTime, workEndRequired, ruleNames, result);

        // 计算工作时长
        calculateWorkHours(punchInTime, punchOutTime, result);

        // 分析加班情况
        analyzeOvertimeStatus(punchOutTime, workEndRequired, ruleNames, result);

        // 确定最终考勤状态
        determineFinalAttendanceStatus(result);

        log.debug("考勤分析完成：状态{}，异常{}，工作时长{}小时",
                result.getAttendanceStatus(), result.getExceptionType(), result.getWorkHours());

        return result;
    }

    /**
     * 分析迟到情况
     */
    private void analyzeLateStatus(LocalTime punchInTime, LocalTime workStartRequired,
            List<String> ruleNames, AttendanceAnalysisResult result) {
        if (punchInTime == null) {
            result.getWarnings().add("缺少上班打卡记录");
            return;
        }

        for (String ruleName : ruleNames) {
            AttendanceRule rule = attendanceRules.get(ruleName);
            if (rule == null || !"LATE_RULE".equals(rule.getRuleType()) && !"FLEX_RULE".equals(rule.getRuleType())) {
                continue;
            }

            LocalTime thresholdTime = rule.getStandardWorkStart();
            if (rule.getGracePeriodStart() != null) {
                thresholdTime = rule.getGracePeriodStart();
            }

            // 弹性时间处理
            if (Boolean.TRUE.equals(rule.getEnableFlexTime()) && rule.getFlexWorkStart() != null) {
                if (punchInTime.isBefore(rule.getFlexWorkStart()) || punchInTime.isAfter(rule.getFlexWorkEnd())) {
                    result.setAttendanceStatus("INVALID_TIME");
                    result.setExceptionReason("打卡时间超出弹性工作时间范围");
                    result.setValidRecord(false);
                    return;
                }
                thresholdTime = rule.getStandardWorkStart(); // 弹性时间以标准工作时间为基准
            }

            if (punchInTime.isAfter(thresholdTime)) {
                long lateMinutes = Duration.between(thresholdTime, punchInTime).toMinutes();
                result.setLateMinutes(BigDecimal.valueOf(lateMinutes));
                result.setAttendanceStatus("LATE");
                result.setExceptionType(ExceptionType.LATE.getCode());
                result.setExceptionReason(String.format("迟到%d分钟", lateMinutes));

                result.getAnalysisDetails().put("lateThreshold", thresholdTime);
                result.getAnalysisDetails().put("actualPunchIn", punchInTime);
                break;
            }
        }
    }

    /**
     * 分析早退情况
     */
    private void analyzeEarlyLeaveStatus(LocalTime punchOutTime, LocalTime workEndRequired,
            List<String> ruleNames, AttendanceAnalysisResult result) {
        if (punchOutTime == null) {
            result.getWarnings().add("缺少下班打卡记录");
            return;
        }

        for (String ruleName : ruleNames) {
            AttendanceRule rule = attendanceRules.get(ruleName);
            if (rule == null
                    || !"EARLY_LEAVE_RULE".equals(rule.getRuleType()) && !"FLEX_RULE".equals(rule.getRuleType())) {
                continue;
            }

            LocalTime thresholdTime = rule.getStandardWorkEnd();
            if (rule.getGracePeriodEnd() != null) {
                thresholdTime = rule.getGracePeriodEnd();
            }

            // 弹性时间处理
            if (Boolean.TRUE.equals(rule.getEnableFlexTime()) && rule.getFlexWorkEnd() != null) {
                if (punchOutTime.isBefore(rule.getFlexWorkStart()) || punchOutTime.isAfter(rule.getFlexWorkEnd())) {
                    result.setAttendanceStatus("INVALID_TIME");
                    result.setExceptionReason("打卡时间超出弹性工作时间范围");
                    result.setValidRecord(false);
                    return;
                }
                thresholdTime = rule.getStandardWorkEnd(); // 弹性时间以标准工作时间为基准
            }

            if (punchOutTime.isBefore(thresholdTime)) {
                long earlyLeaveMinutes = Duration.between(punchOutTime, thresholdTime).toMinutes();
                result.setEarlyLeaveMinutes(BigDecimal.valueOf(earlyLeaveMinutes));

                // 如果已经有迟到，合并为异常
                if ("LATE".equals(result.getAttendanceStatus())) {
                    result.setAttendanceStatus("ABNORMAL");
                    result.setExceptionType("LATE_EARLY_LEAVE");
                    result.setExceptionReason(String.format("迟到%d分钟，早退%d分钟",
                            result.getLateMinutes().intValue(), earlyLeaveMinutes));
                } else {
                    result.setAttendanceStatus("EARLY_LEAVE");
                    result.setExceptionType(ExceptionType.EARLY_LEAVE.getCode());
                    result.setExceptionReason(String.format("早退%d分钟", earlyLeaveMinutes));
                }

                result.getAnalysisDetails().put("earlyLeaveThreshold", thresholdTime);
                result.getAnalysisDetails().put("actualPunchOut", punchOutTime);
                break;
            }
        }
    }

    /**
     * 计算工作时长
     */
    private void calculateWorkHours(LocalTime punchInTime, LocalTime punchOutTime, AttendanceAnalysisResult result) {
        if (punchInTime == null || punchOutTime == null) {
            result.setWorkHours(BigDecimal.ZERO);
            return;
        }

        // 处理跨天情况
        long workMinutes;
        if (punchOutTime.isBefore(punchInTime)) {
            // 跨天工作
            workMinutes = Duration.between(punchInTime, LocalTime.MAX).toMinutes() +
                    Duration.between(LocalTime.MIN, punchOutTime).toMinutes() + 1;
        } else {
            workMinutes = Duration.between(punchInTime, punchOutTime).toMinutes();
        }

        // 减去休息时间（默认1小时）
        long restMinutes = 60;
        if (workMinutes > restMinutes) {
            workMinutes -= restMinutes;
        }

        BigDecimal workHours = BigDecimal.valueOf(workMinutes).divide(BigDecimal.valueOf(60), 2,
                java.math.RoundingMode.HALF_UP);
        result.setWorkHours(workHours);

        result.getAnalysisDetails().put("totalWorkMinutes", workMinutes + restMinutes);
        result.getAnalysisDetails().put("actualWorkMinutes", workMinutes);
    }

    /**
     * 分析加班情况
     */
    private void analyzeOvertimeStatus(LocalTime punchOutTime, LocalTime workEndRequired,
            List<String> ruleNames, AttendanceAnalysisResult result) {
        if (punchOutTime == null) {
            return;
        }

        for (String ruleName : ruleNames) {
            AttendanceRule rule = attendanceRules.get(ruleName);
            if (rule == null || !"OVERTIME_RULE".equals(rule.getRuleType())) {
                continue;
            }

            BigDecimal overtimeThreshold = rule.getOvertimeThresholdHours();
            if (overtimeThreshold == null) {
                overtimeThreshold = BigDecimal.ONE; // 默认1小时
            }

            // 计算加班时长
            long overtimeMinutes = 0;
            if (punchOutTime.isAfter(workEndRequired)) {
                overtimeMinutes = Duration.between(workEndRequired, punchOutTime).toMinutes();
            }

            BigDecimal overtimeHours = BigDecimal.valueOf(overtimeMinutes).divide(BigDecimal.valueOf(60), 2,
                    java.math.RoundingMode.HALF_UP);

            if (overtimeHours.compareTo(overtimeThreshold) >= 0) {
                result.setOvertimeHours(overtimeHours);
                result.getAnalysisDetails().put("overtimeThreshold", overtimeThreshold);
                result.getAnalysisDetails().put("actualOvertimeHours", overtimeHours);

                // 如果不是异常状态，设置为加班
                if ("NORMAL".equals(result.getAttendanceStatus())) {
                    result.setAttendanceStatus("OVERTIME");
                    result.setExceptionType(ExceptionType.OVERWORK.getCode());
                    result.setExceptionReason(String.format("加班%.2f小时", overtimeHours));
                }
            }
            break;
        }
    }

    /**
     * 确定最终考勤状态
     */
    private void determineFinalAttendanceStatus(AttendanceAnalysisResult result) {
        if (result.getAttendanceStatus() == null) {
            result.setAttendanceStatus("NORMAL");
            result.setExceptionType(null);
            result.setExceptionReason(null);
        }

        // 优先级：ABSENT > ABNORMAL > LATE/EARLY_LEAVE > OVERTIME > NORMAL
        String status = result.getAttendanceStatus();
        if (status == null || "NORMAL".equals(status)) {
            result.setAttendanceStatus("NORMAL");
        }
    }

    /**
     * 批量分析考勤记录
     *
     * @param attendanceRecords 考勤记录列表
     * @param ruleNames         适用的规则名称
     * @return 分析结果列表
     */
    public List<AttendanceAnalysisResult> batchAnalyzeAttendance(List<AttendanceRecord> attendanceRecords,
            List<String> ruleNames) {
        log.info("开始批量分析考勤记录，数量：{}", attendanceRecords.size());

        List<AttendanceAnalysisResult> results = new ArrayList<>();

        for (AttendanceRecord record : attendanceRecords) {
            AttendanceAnalysisResult result = analyzeAttendance(
                    record.getPunchInTime(),
                    record.getPunchOutTime(),
                    record.getWorkStartTime(),
                    record.getWorkEndTime(),
                    ruleNames);

            // 设置员工ID和日期
            result.getAnalysisDetails().put("employeeId", record.getEmployeeId());
            result.getAnalysisDetails().put("attendanceDate", record.getAttendanceDate());

            results.add(result);
        }

        log.info("批量考勤分析完成");
        return results;
    }

    /**
     * 验证考勤规则的有效性
     *
     * @param rule 考勤规则
     * @return 验证结果
     */
    public RuleValidationResult validateRule(AttendanceRule rule) {
        log.debug("开始验证考勤规则：{}", rule.getRuleName());

        RuleValidationResult result = new RuleValidationResult();
        result.setValid(true);
        result.setErrors(new ArrayList<>());

        // 检查必要字段
        if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
            result.addError("规则名称不能为空");
            result.setValid(false);
        }

        if (rule.getRuleType() == null || rule.getRuleType().trim().isEmpty()) {
            result.addError("规则类型不能为空");
            result.setValid(false);
        }

        // 检查时间合理性
        if (rule.getStandardWorkStart() != null && rule.getStandardWorkEnd() != null) {
            if (rule.getStandardWorkStart().isAfter(rule.getStandardWorkEnd())) {
                // 检查是否为夜班
                if (!"NIGHT_SHIFT".equals(rule.getRuleType())) {
                    result.addError("标准上班时间不能晚于下班时间");
                    result.setValid(false);
                }
            }
        }

        // 检查弹性时间配置
        if (Boolean.TRUE.equals(rule.getEnableFlexTime())) {
            if (rule.getFlexWorkStart() == null || rule.getFlexWorkEnd() == null) {
                result.addError("启用弹性时间时必须设置弹性工作起止时间");
                result.setValid(false);
            }

            if (rule.getFlexWorkStart() != null && rule.getFlexWorkEnd() != null &&
                    rule.getFlexWorkStart().isAfter(rule.getFlexWorkEnd())) {
                result.addError("弹性开始时间不能晚于弹性结束时间");
                result.setValid(false);
            }
        }

        log.info("考勤规则验证完成：{} - {}", rule.getRuleName(), result.isValid() ? "通过" : "失败");
        return result;
    }

    /**
     * 考勤记录类（简化版）
     */
    @Data
    public static class AttendanceRecord {
        private Long employeeId;
        private LocalDate attendanceDate;
        private LocalTime punchInTime;
        private LocalTime punchOutTime;
        private LocalTime workStartTime;
        private LocalTime workEndTime;
    }

    /**
     * 规则验证结果类
     */
    @Data
    public static class RuleValidationResult {
        private Boolean valid;
        private List<String> errors;

        public void addError(String error) {
            if (errors == null) {
                errors = new ArrayList<>();
            }
            errors.add(error);
        }

        public boolean isValid() {
            return valid != null && valid;
        }
    }

    /**
     * 考勤规则处理结果类
     */
    @Data
    public static class AttendanceRuleProcessResult {
        private boolean success;
        private String message;
        private String errorMessage;
        private String attendanceStatus;
        private String exceptionType;
        private String exceptionDescription;
        private AttendanceAnalysisResult analysisResult;

        public static AttendanceRuleProcessResult success(String message, AttendanceAnalysisResult result) {
            AttendanceRuleProcessResult processResult = new AttendanceRuleProcessResult();
            processResult.setSuccess(true);
            processResult.setMessage(message);
            processResult.setAnalysisResult(result);
            return processResult;
        }

        public static AttendanceRuleProcessResult failure(String message) {
            AttendanceRuleProcessResult processResult = new AttendanceRuleProcessResult();
            processResult.setSuccess(false);
            processResult.setMessage(message);
            processResult.setErrorMessage(message);
            return processResult;
        }

        public String getAttendanceStatus() {
            return attendanceStatus;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getExceptionType() {
            return exceptionType;
        }

        public String getExceptionDescription() {
            return exceptionDescription;
        }
    }

    /**
     * 验证GPS位置
     *
     * @param record    考勤记录
     * @param rule      考勤规则
     * @param latitude  纬度
     * @param longitude 经度
     * @param range     范围（米）
     * @return 是否验证通过
     */
    public boolean validateGpsLocation(AttendanceRecordEntity record, AttendanceRuleEntity rule, Double latitude,
            Double longitude, Double range) {
        try {
            if (rule == null || latitude == null || longitude == null || range == null) {
                return false;
            }

            // 简单的距离计算（实际应用中应该使用更精确的算法）
            if (rule.getLatitude() != null && rule.getLongitude() != null) {
                double distance = calculateDistance(latitude, longitude, rule.getLatitude(), rule.getLongitude());
                return distance <= range;
            }

            return true;
        } catch (Exception e) {
            log.error("GPS位置验证异常", e);
            return false;
        }
    }

    /**
     * 验证打卡位置
     *
     * 根据员工ID获取考勤规则，验证打卡位置是否在允许范围内。
     * 支持单个GPS坐标点验证和多个GPS位置点验证（从JSON配置中解析）。
     *
     * @param employeeId 员工ID
     * @param latitude   打卡纬度
     * @param longitude  打卡经度
     * @param rule       考勤规则实体（如果为null，需要从外部获取）
     * @return 是否验证通过，true表示位置在允许范围内，false表示不在范围内或验证失败
     *
     * @example
     *
     *          <pre>
     *          Long employeeId = 123L;
     *          Double latitude = 39.9042;
     *          Double longitude = 116.4074;
     *          AttendanceRuleEntity rule = getRuleByEmployeeId(employeeId);
     *          boolean isValid = attendanceRuleEngine.validateLocation(employeeId, latitude, longitude, rule);
     *          </pre>
     */
    public boolean validateLocation(Long employeeId, Double latitude, Double longitude, AttendanceRuleEntity rule) {
        try {
            // 如果规则为空，直接返回true（不强制位置验证）
            if (rule == null) {
                log.debug("考勤规则为空，跳过位置验证: 员工ID={}", employeeId);
                return true;
            }

            // 如果规则未启用位置验证，直接返回true
            if (!Boolean.TRUE.equals(rule.getLocationRequired()) && !rule.isGpsValidationEnabled()) {
                log.debug("位置验证未启用，跳过验证: 员工ID={}, 规则ID={}", employeeId, rule.getRuleId());
                return true;
            }

            // 如果打卡位置为空，返回false
            if (latitude == null || longitude == null) {
                log.warn("打卡位置为空: 员工ID={}", employeeId);
                return false;
            }

            // 获取验证范围（默认100米）
            double range = rule.getGpsRange() != null ? rule.getGpsRange() : 100.0;
            if (rule.getMaxDistance() != null) {
                range = rule.getMaxDistance();
            }

            // 1. 优先验证单个GPS坐标点（latitude和longitude字段）
            if (rule.getLatitude() != null && rule.getLongitude() != null) {
                double distance = calculateDistance(latitude, longitude, rule.getLatitude(), rule.getLongitude());
                if (distance <= range) {
                    log.debug("位置验证通过（单个坐标点）: 员工ID={}, 距离={}米", employeeId, distance);
                    return true;
                }
            }

            // 2. 验证多个GPS位置点（从gpsLocations JSON配置中解析）
            if (rule.getGpsLocations() != null && !rule.getGpsLocations().trim().isEmpty()) {
                try {
                    // 解析JSON格式的GPS位置配置
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<AttendanceRuleEntity.GpsLocationPoint> locations = objectMapper.readValue(
                            rule.getGpsLocations(),
                            objectMapper.getTypeFactory().constructCollectionType(java.util.List.class,
                                    AttendanceRuleEntity.GpsLocationPoint.class));

                    if (locations != null && !locations.isEmpty()) {
                        for (AttendanceRuleEntity.GpsLocationPoint location : locations) {
                            if (location.getLatitude() != null && location.getLongitude() != null) {
                                double locationRange = location.getRadius() != null ? location.getRadius() : range;
                                double distance = calculateDistance(latitude, longitude, location.getLatitude(),
                                        location.getLongitude());
                                if (distance <= locationRange) {
                                    log.debug("位置验证通过（多个坐标点）: 员工ID={}, 位置名称={}, 距离={}米",
                                            employeeId, location.getName(), distance);
                                    return true;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析GPS位置配置失败: 员工ID={}, 规则ID={}, 错误={}", employeeId, rule.getRuleId(),
                            e.getMessage());
                }
            }

            log.warn("位置验证失败: 员工ID={}, 打卡位置=({}, {}), 规则ID={}", employeeId, latitude, longitude,
                    rule.getRuleId());
            return false;
        } catch (Exception e) {
            log.error("位置验证异常: 员工ID={}", employeeId, e);
            return false;
        }
    }

    /**
     * 验证打卡设备
     *
     * 根据员工ID获取考勤规则，验证打卡设备是否在允许的设备列表中。
     * 从deviceRestrictions JSON配置中解析允许的设备列表。
     *
     * @param employeeId 员工ID
     * @param deviceId   打卡设备ID
     * @param rule       考勤规则实体（如果为null，需要从外部获取）
     * @return 是否验证通过，true表示设备在允许列表中，false表示不在列表中或验证失败
     *
     * @example
     *
     *          <pre>
     *          Long employeeId = 123L;
     *          String deviceId = "DEVICE_001";
     *          AttendanceRuleEntity rule = getRuleByEmployeeId(employeeId);
     *          boolean isValid = attendanceRuleEngine.validateDevice(employeeId, deviceId, rule);
     *          </pre>
     */
    public boolean validateDevice(Long employeeId, String deviceId, AttendanceRuleEntity rule) {
        try {
            // 如果规则为空，直接返回true（不强制设备验证）
            if (rule == null) {
                log.debug("考勤规则为空，跳过设备验证: 员工ID={}", employeeId);
                return true;
            }

            // 如果规则未启用设备验证，直接返回true
            if (!Boolean.TRUE.equals(rule.getDeviceRequired())) {
                log.debug("设备验证未启用，跳过验证: 员工ID={}, 规则ID={}", employeeId, rule.getRuleId());
                return true;
            }

            // 如果设备ID为空，返回false
            if (deviceId == null || deviceId.trim().isEmpty()) {
                log.warn("打卡设备ID为空: 员工ID={}", employeeId);
                return false;
            }

            // 如果设备限制配置为空，返回true（允许所有设备）
            if (rule.getDeviceRestrictions() == null || rule.getDeviceRestrictions().trim().isEmpty()) {
                log.debug("设备限制配置为空，允许所有设备: 员工ID={}", employeeId);
                return true;
            }

            // 解析JSON格式的设备限制配置
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> deviceConfig = (java.util.Map<String, Object>) objectMapper.readValue(
                        rule.getDeviceRestrictions(),
                        objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class,
                                Object.class));

                if (deviceConfig != null && deviceConfig.containsKey("allowedDevices")) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> allowedDevices = (java.util.List<String>) deviceConfig.get("allowedDevices");

                    if (allowedDevices != null && !allowedDevices.isEmpty()) {
                        boolean isValid = allowedDevices.contains(deviceId.trim());
                        if (isValid) {
                            log.debug("设备验证通过: 员工ID={}, 设备ID={}", employeeId, deviceId);
                        } else {
                            log.warn("设备验证失败: 员工ID={}, 设备ID={}, 允许的设备列表={}", employeeId, deviceId,
                                    allowedDevices);
                        }
                        return isValid;
                    }
                }
            } catch (Exception e) {
                log.warn("解析设备限制配置失败: 员工ID={}, 规则ID={}, 错误={}", employeeId, rule.getRuleId(),
                        e.getMessage());
            }

            // 如果解析失败，默认允许（避免影响正常打卡）
            log.debug("设备限制配置解析失败，默认允许: 员工ID={}", employeeId);
            return true;
        } catch (Exception e) {
            log.error("设备验证异常: 员工ID={}, 设备ID={}", employeeId, deviceId, e);
            return false;
        }
    }

    /**
     * 计算两点间距离（单位：米）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
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
                return AttendanceRuleProcessResult.failure("考勤记录不能为空");
            }

            // 获取员工考勤规则
            AttendanceRuleEntity rule = getEmployeeRule(record.getEmployeeId());
            if (rule == null) {
                return AttendanceRuleProcessResult.failure("未找到员工考勤规则");
            }

            // 分析考勤记录
            AttendanceAnalysisResult analysisResult = analyzeAttendance(
                    record.getPunchInTime(),
                    record.getPunchOutTime(),
                    rule.getWorkStartTime(),
                    rule.getWorkEndTime(),
                    List.of(rule.getRuleName()));

            // 更新考勤记录状态
            record.setAttendanceStatus(analysisResult.getAttendanceStatus());
            record.setExceptionType(analysisResult.getExceptionType());
            record.setExceptionReason(analysisResult.getExceptionReason());
            record.setWorkHours(analysisResult.getWorkHours());
            record.setOvertimeHours(analysisResult.getOvertimeHours());
            record.setLateMinutes(analysisResult.getLateMinutes());
            record.setEarlyLeaveMinutes(analysisResult.getEarlyLeaveMinutes());

            return AttendanceRuleProcessResult.success("考勤记录处理成功", analysisResult);
        } catch (Exception e) {
            log.error("处理考勤记录失败", e);
            return AttendanceRuleProcessResult.failure("处理异常：" + e.getMessage());
        }
    }

    /**
     * 获取员工考勤规则
     *
     * @param employeeId 员工ID
     * @return 考勤规则
     */
    private AttendanceRuleEntity getEmployeeRule(Long employeeId) {
        // 简化实现，返回默认规则
        AttendanceRuleEntity defaultRule = new AttendanceRuleEntity();
        defaultRule.setRuleName("默认规则");
        defaultRule.setWorkStartTime(LocalTime.of(9, 0));
        defaultRule.setWorkEndTime(LocalTime.of(18, 0));
        return defaultRule;
    }

    /**
     * 获取标准上班开始时间
     *
     * @param employeeId 员工ID
     * @param date       日期
     * @return 上班开始时间
     */
    public LocalTime getStandardWorkStartTime(Long employeeId, LocalDate date) {
        AttendanceRuleEntity rule = getEmployeeRule(employeeId);
        return rule != null ? rule.getWorkStartTime() : LocalTime.of(9, 0);
    }

    /**
     * 获取标准上班结束时间
     *
     * @param employeeId 员工ID
     * @param date       日期
     * @return 上班结束时间
     */
    public LocalTime getStandardWorkEndTime(Long employeeId, LocalDate date) {
        AttendanceRuleEntity rule = getEmployeeRule(employeeId);
        return rule != null ? rule.getWorkEndTime() : LocalTime.of(18, 0);
    }

    /**
     * 添加考勤规则
     *
     * @param rule 考勤规则
     */
    public void addAttendanceRule(AttendanceRule rule) {
        attendanceRules.put(rule.getRuleName(), rule);
        log.info("添加考勤规则：{}", rule.getRuleName());
    }

    /**
     * 获取所有考勤规则
     *
     * @return 考勤规则列表
     */
    public List<AttendanceRule> getAllAttendanceRules() {
        return new ArrayList<>(attendanceRules.values());
    }

    /**
     * 获取异常类型列表
     *
     * @return 异常类型列表
     */
    public List<ExceptionType> getAllExceptionTypes() {
        return Arrays.asList(ExceptionType.values());
    }
}
