package net.lab1024.sa.attendance.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 考勤规则验证工具类
 *
 * <p>
 * 专门用于考勤规则的数据验证、业务逻辑验证和格式检查
 * 严格遵循repowiki编码规范：使用jakarta包名、SLF4J日志
 * </p>
 *
 * <p>
 * 主要功能：
 * - 考勤规则数据格式验证
 * - 时间格式和范围验证
 * - 班次配置冲突检查
 * - 规则逻辑一致性验证
 * - 数据完整性检查
 * </p>
 *
 * <p>
 * 使用示例：
 *
 * <pre>
 * &#64;Resource
 * private AttendanceRuleValidator ruleValidator;
 *
 * // 验证考勤规则
 * ValidationResult result = ruleValidator.validateAttendanceRule(ruleData);
 *
 * // 检查时间格式
 * boolean isValidTime = ruleValidator.isValidTimeFormat("09:00");
 * </pre>
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
@Component
public class AttendanceRuleValidator {

    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 正则表达式模式
     */
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    private static final Pattern TIME_RANGE_PATTERN = Pattern
            .compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$");

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }

        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    /**
     * 验证考勤规则完整数据
     *
     * @param ruleData 规则数据
     * @return 验证结果
     */
    public ValidationResult validateAttendanceRule(Map<String, Object> ruleData) {
        ValidationResult result = new ValidationResult();

        try {
            log.debug("[AttendanceRuleValidator] 开始验证考勤规则数据");

            // 1. 基础字段验证
            validateBasicFields(ruleData, result);

            // 2. 时间配置验证
            validateTimeConfiguration(ruleData, result);

            // 3. 班次配置验证
            validateShiftConfiguration(ruleData, result);

            // 4. 规则逻辑验证
            validateRuleLogic(ruleData, result);

            // 5. 数据完整性验证
            validateDataIntegrity(ruleData, result);

            log.debug("[AttendanceRuleValidator] 考勤规则验证完成: 有效={}, 错误={}, 警告={}",
                    result.isValid(), result.getErrors().size(), result.getWarnings().size());

        } catch (Exception e) {
            log.error("[AttendanceRuleValidator] 验证考勤规则时发生异常", e);
            result.addError("验证过程发生异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 验证基础字段
     */
    private void validateBasicFields(Map<String, Object> ruleData, ValidationResult result) {
        // 验证规则名称
        String ruleName = (String) ruleData.get("ruleName");
        if (!StringUtils.hasText(ruleName)) {
            result.addError("规则名称不能为空");
        } else if (ruleName.length() > 50) {
            result.addError("规则名称长度不能超过50个字符");
        }

        // 验证规则类型
        String ruleType = (String) ruleData.get("ruleType");
        if (!StringUtils.hasText(ruleType)) {
            result.addError("规则类型不能为空");
        } else {
            List<String> validTypes = Arrays.asList("NORMAL", "FLEXIBLE", "SHIFT", "OVERTIME");
            if (!validTypes.contains(ruleType)) {
                result.addError("无效的规则类型: " + ruleType);
            }
        }

        // 验证状态
        String status = (String) ruleData.get("status");
        if (StringUtils.hasText(status)) {
            List<String> validStatuses = Arrays.asList("ACTIVE", "INACTIVE", "DRAFT");
            if (!validStatuses.contains(status)) {
                result.addError("无效的规则状态: " + status);
            }
        }
    }

    /**
     * 验证时间配置
     */
    private void validateTimeConfiguration(Map<String, Object> ruleData, ValidationResult result) {
        // 验证上班时间
        String startTime = (String) ruleData.get("startTime");
        if (StringUtils.hasText(startTime)) {
            if (!isValidTimeFormat(startTime)) {
                result.addError("无效的上班时间格式: " + startTime);
            } else {
                validateTimeRange(startTime, null, "上班时间", result);
            }
        }

        // 验证下班时间
        String endTime = (String) ruleData.get("endTime");
        if (StringUtils.hasText(endTime)) {
            if (!isValidTimeFormat(endTime)) {
                result.addError("无效的下班时间格式: " + endTime);
            } else {
                validateTimeRange(null, endTime, "下班时间", result);
            }
        }

        // 验证时间范围一致性
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            validateTimeConsistency(startTime, endTime, result);
        }

        // 验证休息时间
        String restStartTime = (String) ruleData.get("restStartTime");
        String restEndTime = (String) ruleData.get("restEndTime");
        if (StringUtils.hasText(restStartTime) && StringUtils.hasText(restEndTime)) {
            if (!isValidTimeFormat(restStartTime) || !isValidTimeFormat(restEndTime)) {
                result.addError("休息时间格式无效");
            } else {
                validateTimeConsistency(restStartTime, restEndTime, result);
            }
        }
    }

    /**
     * 验证时间格式
     *
     * @param timeString 时间字符串
     * @return 是否有效
     */
    public boolean isValidTimeFormat(String timeString) {
        if (!StringUtils.hasText(timeString)) {
            return false;
        }

        try {
            // 使用正则表达式快速检查
            if (!TIME_PATTERN.matcher(timeString).matches()) {
                return false;
            }

            // 使用时间解析器精确验证
            LocalTime.parse(timeString, TIME_FORMATTER);
            return true;

        } catch (DateTimeParseException e) {
            log.debug("[AttendanceRuleValidator] 时间格式验证失败: {}", timeString);
            return false;
        }
    }

    /**
     * 验证时间范围格式
     *
     * @param timeRange 时间范围字符串（如：09:00-18:00）
     * @return 是否有效
     */
    public boolean isValidTimeRangeFormat(String timeRange) {
        if (!StringUtils.hasText(timeRange)) {
            return false;
        }

        try {
            // 使用正则表达式快速检查
            if (!TIME_RANGE_PATTERN.matcher(timeRange).matches()) {
                return false;
            }

            String[] times = timeRange.split("-");
            String startTime = times[0];
            String endTime = times[1];

            return isValidTimeFormat(startTime) && isValidTimeFormat(endTime);

        } catch (Exception e) {
            log.debug("[AttendanceRuleValidator] 时间范围格式验证失败: {}", timeRange);
            return false;
        }
    }

    /**
     * 验证时间逻辑一致性
     */
    private void validateTimeConsistency(String startTime, String endTime, ValidationResult result) {
        try {
            LocalTime start = LocalTime.parse(startTime, TIME_FORMATTER);
            LocalTime end = LocalTime.parse(endTime, TIME_FORMATTER);

            // 如果结束时间小于开始时间，可能是跨天班次
            if (end.isBefore(start)) {
                result.getWarnings().add("结束时间早于开始时间，可能是跨天班次配置");
            }

            // 检查最小工作时间
            Duration workDuration = Duration.between(start, end);
            if (workDuration.isNegative()) {
                workDuration = workDuration.plusHours(24); // 处理跨天
            }

            if (workDuration.toMinutes() < 60) {
                result.getWarnings().add("工作时间少于1小时，请确认配置是否正确");
            }

            if (workDuration.toHours() > 12) {
                result.getWarnings().add("工作时间超过12小时，请确认配置是否合理");
            }

        } catch (Exception e) {
            result.addError("时间一致性验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证时间范围
     */
    private void validateTimeRange(String startTime, String endTime, String fieldName, ValidationResult result) {
        try {
            LocalTime time = LocalTime.parse(startTime != null ? startTime : endTime, TIME_FORMATTER);

            // 检查是否在合理的时间范围内（00:00-23:59）
            if (time.isBefore(LocalTime.MIN) || time.isAfter(LocalTime.MAX.minusSeconds(1))) {
                result.addError(fieldName + "超出有效时间范围");
            }

        } catch (Exception e) {
            result.addError(fieldName + "解析失败: " + e.getMessage());
        }
    }

    /**
     * 验证班次配置
     */
    private void validateShiftConfiguration(Map<String, Object> ruleData, ValidationResult result) {
        // 验证弹性工作时间配置
        Boolean flexibleEnabled = (Boolean) ruleData.get("flexibleEnabled");
        if (Boolean.TRUE.equals(flexibleEnabled)) {
            validateFlexibleWorkConfig(ruleData, result);
        }

        // 验证迟到早退规则
        validateLateEarlyRules(ruleData, result);

        // 验证加班规则
        validateOvertimeRules(ruleData, result);
    }

    /**
     * 验证弹性工作配置
     */
    private void validateFlexibleWorkConfig(Map<String, Object> ruleData, ValidationResult result) {
        // 验证弹性开始时间
        String flexibleStartTime = (String) ruleData.get("flexibleStartTime");
        if (StringUtils.hasText(flexibleStartTime) && !isValidTimeFormat(flexibleStartTime)) {
            result.addError("弹性开始时间格式无效");
        }

        // 验证弹性结束时间
        String flexibleEndTime = (String) ruleData.get("flexibleEndTime");
        if (StringUtils.hasText(flexibleEndTime) && !isValidTimeFormat(flexibleEndTime)) {
            result.addError("弹性结束时间格式无效");
        }

        // 验证核心工作时间
        String coreStartTime = (String) ruleData.get("coreStartTime");
        String coreEndTime = (String) ruleData.get("coreEndTime");
        if (StringUtils.hasText(coreStartTime) && StringUtils.hasText(coreEndTime)) {
            if (!isValidTimeFormat(coreStartTime) || !isValidTimeFormat(coreEndTime)) {
                result.addError("核心工作时间格式无效");
            } else {
                validateTimeConsistency(coreStartTime, coreEndTime, result);
            }
        }

        // 验证最短工作时长
        Integer minWorkHours = (Integer) ruleData.get("minWorkHours");
        if (minWorkHours != null && (minWorkHours < 1 || minWorkHours > 12)) {
            result.addError("最短工作时长应在1-12小时之间");
        }
    }

    /**
     * 验证迟到早退规则
     */
    private void validateLateEarlyRules(Map<String, Object> ruleData, ValidationResult result) {
        // 验证迟到宽限时间
        Integer lateGracePeriod = (Integer) ruleData.get("lateGracePeriod");
        if (lateGracePeriod != null && (lateGracePeriod < 0 || lateGracePeriod > 60)) {
            result.addError("迟到宽限时间应在0-60分钟之间");
        }

        // 验证早退宽限时间
        Integer earlyGracePeriod = (Integer) ruleData.get("earlyGracePeriod");
        if (earlyGracePeriod != null && (earlyGracePeriod < 0 || earlyGracePeriod > 60)) {
            result.addError("早退宽限时间应在0-60分钟之间");
        }

        // 验证迟到处罚规则
        @SuppressWarnings("unchecked")
        Map<String, Object> latePenalty = (Map<String, Object>) ruleData.get("latePenalty");
        if (latePenalty != null) {
            validatePenaltyRules(latePenalty, "迟到", result);
        }
    }

    /**
     * 验证加班规则
     */
    private void validateOvertimeRules(Map<String, Object> ruleData, ValidationResult result) {
        // 验证加班最小时间
        Integer minOvertimeMinutes = (Integer) ruleData.get("minOvertimeMinutes");
        if (minOvertimeMinutes != null && (minOvertimeMinutes < 0 || minOvertimeMinutes > 480)) {
            result.addError("加班最小时间应在0-480分钟之间");
        }

        // 验证加班计算方式
        String overtimeCalcType = (String) ruleData.get("overtimeCalcType");
        if (StringUtils.hasText(overtimeCalcType)) {
            List<String> validTypes = Arrays.asList("DAILY", "WEEKLY", "MONTHLY");
            if (!validTypes.contains(overtimeCalcType)) {
                result.addError("无效的加班计算方式: " + overtimeCalcType);
            }
        }

        // 验证加班限制
        Integer maxOvertimeHours = (Integer) ruleData.get("maxOvertimeHours");
        if (maxOvertimeHours != null && (maxOvertimeHours < 0 || maxOvertimeHours > 100)) {
            result.addError("每月加班上限应在0-100小时之间");
        }
    }

    /**
     * 验证处罚规则
     */
    private void validatePenaltyRules(Map<String, Object> penalty, String penaltyType, ValidationResult result) {
        String penaltyTypeCode = (String) penalty.get("type");
        if (StringUtils.hasText(penaltyTypeCode)) {
            List<String> validTypes = Arrays.asList("WARNING", "DEDUCTION", "POINTS");
            if (!validTypes.contains(penaltyTypeCode)) {
                result.addError("无效的" + penaltyType + "处罚类型: " + penaltyTypeCode);
            }
        }

        Double penaltyAmount = (Double) penalty.get("amount");
        if (penaltyAmount != null && penaltyAmount < 0) {
            result.addError(penaltyType + "处罚金额不能为负数");
        }

        String description = (String) penalty.get("description");
        if (StringUtils.hasText(description) && description.length() > 200) {
            result.addError(penaltyType + "处罚描述过长");
        }
    }

    /**
     * 验证规则逻辑
     */
    private void validateRuleLogic(Map<String, Object> ruleData, ValidationResult result) {
        // 检查规则冲突
        checkRuleConflicts(ruleData, result);

        // 检查逻辑一致性
        checkLogicConsistency(ruleData, result);
    }

    /**
     * 检查规则冲突
     */
    private void checkRuleConflicts(Map<String, Object> ruleData, ValidationResult result) {
        // 检查时间冲突
        String startTime = (String) ruleData.get("startTime");
        String endTime = (String) ruleData.get("endTime");
        String restStartTime = (String) ruleData.get("restStartTime");
        String restEndTime = (String) ruleData.get("restEndTime");

        if (StringUtils.hasText(startTime) && StringUtils.hasText(restStartTime)) {
            try {
                LocalTime workStart = LocalTime.parse(startTime, TIME_FORMATTER);
                LocalTime restStart = LocalTime.parse(restStartTime, TIME_FORMATTER);

                if (restStart.isBefore(workStart)) {
                    result.getWarnings().add("休息开始时间早于上班开始时间");
                }
            } catch (Exception e) {
                // 时间解析错误已在前面处理
            }
        }

        if (StringUtils.hasText(endTime) && StringUtils.hasText(restEndTime)) {
            try {
                LocalTime workEnd = LocalTime.parse(endTime, TIME_FORMATTER);
                LocalTime restEnd = LocalTime.parse(restEndTime, TIME_FORMATTER);

                if (restEnd.isAfter(workEnd)) {
                    result.getWarnings().add("休息结束时间晚于下班结束时间");
                }
            } catch (Exception e) {
                // 时间解析错误已在前面处理
            }
        }
    }

    /**
     * 检查逻辑一致性
     */
    private void checkLogicConsistency(Map<String, Object> ruleData, ValidationResult result) {
        String ruleType = (String) ruleData.get("ruleType");
        Boolean flexibleEnabled = (Boolean) ruleData.get("flexibleEnabled");

        // 检查规则类型与配置的一致性
        if ("FLEXIBLE".equals(ruleType) && !Boolean.TRUE.equals(flexibleEnabled)) {
            result.getWarnings().add("弹性工作规则类型未启用弹性配置");
        }

        if ("NORMAL".equals(ruleType) && Boolean.TRUE.equals(flexibleEnabled)) {
            result.getWarnings().add("标准规则类型启用了弹性配置");
        }

        // 检查必要字段的完整性
        if ("SHIFT".equals(ruleType)) {
            String shiftName = (String) ruleData.get("shiftName");
            if (!StringUtils.hasText(shiftName)) {
                result.addError("班次规则必须指定班次名称");
            }
        }
    }

    /**
     * 验证数据完整性
     */
    private void validateDataIntegrity(Map<String, Object> ruleData, ValidationResult result) {
        // 检查关键字段是否存在
        List<String> requiredFields = Arrays.asList("ruleName", "ruleType");
        for (String field : requiredFields) {
            if (!ruleData.containsKey(field)) {
                result.addError("缺少必需字段: " + field);
            }
        }

        // 检查日期字段格式
        validateDateFields(ruleData, result);

        // 检查数值字段范围
        validateNumericFields(ruleData, result);
    }

    /**
     * 验证日期字段
     */
    private void validateDateFields(Map<String, Object> ruleData, ValidationResult result) {
        String[] dateFields = { "effectiveDate", "expiryDate" };

        for (String field : dateFields) {
            String dateValue = (String) ruleData.get(field);
            if (StringUtils.hasText(dateValue)) {
                if (!isValidDateFormat(dateValue)) {
                    result.addError(field + " 日期格式无效，应为 yyyy-MM-dd");
                } else {
                    // 验证日期合理性
                    try {
                        LocalDate date = LocalDate.parse(dateValue, DATE_FORMATTER);
                        LocalDate today = LocalDate.now();
                        if (date.isBefore(today.minusYears(1))) {
                            result.getWarnings().add(field + " 日期过早，请确认是否正确");
                        }
                        if (date.isAfter(today.plusYears(5))) {
                            result.getWarnings().add(field + " 日期过远，请确认是否正确");
                        }
                    } catch (Exception e) {
                        result.addError(field + " 日期解析失败: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 验证日期格式
     */
    public boolean isValidDateFormat(String dateString) {
        if (!StringUtils.hasText(dateString)) {
            return false;
        }

        try {
            LocalDate.parse(dateString, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 验证数值字段
     */
    private void validateNumericFields(Map<String, Object> ruleData, ValidationResult result) {
        Map<String, String> numericFields = Map.of(
                "workHours", "工作时长",
                "breakMinutes", "休息时长",
                "toleranceMinutes", "容差时间");

        for (Map.Entry<String, String> entry : numericFields.entrySet()) {
            Object value = ruleData.get(entry.getKey());
            if (value != null) {
                try {
                    if (value instanceof String) {
                        Double.parseDouble((String) value);
                    } else if (value instanceof Number) {
                        // 已经是数字类型
                    } else {
                        result.addError(entry.getValue() + " 必须是数字");
                    }
                } catch (NumberFormatException e) {
                    result.addError(entry.getValue() + " 数字格式无效");
                }
            }
        }
    }

    // ==================== 公共验证方法 ====================

    /**
     * 验证员工ID格式
     */
    public boolean isValidEmployeeId(String employeeId) {
        if (!StringUtils.hasText(employeeId)) {
            return false;
        }
        return employeeId.matches("^[A-Za-z0-9_-]{1,20}$");
    }

    /**
     * 验证打卡地点信息
     */
    public boolean isValidLocation(String location) {
        if (!StringUtils.hasText(location)) {
            return true; // 位置信息可以为空
        }
        return location.length() <= 200;
    }

    /**
     * 验证备注信息
     */
    public boolean isValidRemark(String remark) {
        if (!StringUtils.hasText(remark)) {
            return true; // 备注可以为空
        }
        return remark.length() <= 500;
    }

    /**
     * 批量验证打卡记录
     */
    public List<ValidationResult> validatePunchRecords(List<Map<String, Object>> records) {
        List<ValidationResult> results = new ArrayList<>();

        if (records == null || records.isEmpty()) {
            return results;
        }

        for (int i = 0; i < records.size(); i++) {
            Map<String, Object> record = records.get(i);
            ValidationResult result = new ValidationResult();

            // 验证打卡时间
            String punchTime = (String) record.get("punchTime");
            if (StringUtils.hasText(punchTime)) {
                try {
                    LocalDateTime.parse(punchTime, DATE_TIME_FORMATTER);
                } catch (Exception e) {
                    result.addError("打卡时间格式无效");
                }
            } else {
                result.addError("打卡时间不能为空");
            }

            // 验证员工ID
            String employeeId = (String) record.get("employeeId");
            if (!isValidEmployeeId(employeeId)) {
                result.addError("员工ID格式无效");
            }

            // 验证打卡类型
            String punchType = (String) record.get("punchType");
            if (StringUtils.hasText(punchType)) {
                List<String> validTypes = Arrays.asList("IN", "OUT", "BREAK");
                if (!validTypes.contains(punchType)) {
                    result.addError("无效的打卡类型: " + punchType);
                }
            }

            results.add(result);
        }

        return results;
    }
}
