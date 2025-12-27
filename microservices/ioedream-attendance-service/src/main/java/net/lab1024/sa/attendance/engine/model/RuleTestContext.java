package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 规则测试上下文
 * <p>
 * 继承自RuleExecutionContext，添加测试专用字段，提供类型安全的测试数据访问
 * 使用手动Builder避免Lombok @Builder的继承冲突
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@NoArgsConstructor
public class RuleTestContext extends RuleExecutionContext {

    /**
     * 执行ID（用于唯一标识测试执行实例）
     */
    private String executionId;

    /**
     * 执行时间戳
     */
    private LocalDateTime executionTimestamp;

    /**
     * 执行模式（TEST/PRODUCTION）
     */
    private String executionMode;

    /**
     * 打卡时间
     */
    private LocalTime punchTime;

    /**
     * 打卡类型（IN-上班 OUT-下班）
     */
    private String punchType;

    /**
     * 排班开始时间
     */
    private LocalTime scheduleStartTime;

    /**
     * 排班结束时间
     */
    private LocalTime scheduleEndTime;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 用户属性（JSON格式）
     * <p>
     * 示例：{"position": "工程师", "level": 5}
     * </p>
     */
    private Map<String, Object> userAttributes = new HashMap<>();

    /**
     * 考勤属性（JSON格式）
     * <p>
     * 示例：{"isHoliday": false, "isWeekend": false}
     * </p>
     */
    private Map<String, Object> attendanceAttributes = new HashMap<>();

    /**
     * 环境参数（JSON格式）
     * <p>
     * 示例：{"temperature": 25, "weather": "晴"}
     * </p>
     */
    private Map<String, Object> environmentParams = new HashMap<>();

    /**
     * 转换为RuleExecutionContext
     * <p>
     * 将测试专用字段合并到customVariables中，用于规则引擎执行
     * </p>
     *
     * @return 包含所有测试字段的RuleExecutionContext
     */
    public RuleExecutionContext toRuleExecutionContext() {
        // 使用父类基础字段构建基础上下文
        RuleExecutionContext context = RuleExecutionContext.builder()
                .employeeId(this.getEmployeeId())
                .employeeName(this.getEmployeeName())
                .employeeNo(this.getEmployeeNo())
                .departmentId(this.getDepartmentId())
                .departmentName(this.getDepartmentName())
                .scheduleDate(this.getScheduleDate())
                .shiftId(this.getShiftId())
                .shiftCode(this.getShiftCode())
                .shiftType(this.getShiftType())
                .consecutiveWorkDays(this.getConsecutiveWorkDays())
                .restDays(this.getRestDays())
                .monthlyWorkDays(this.getMonthlyWorkDays())
                .monthlyWorkHours(this.getMonthlyWorkHours())
                .skills(this.getSkills())
                .executionId(this.getExecutionId())
                .sessionId(this.getSessionId())
                .build();

        // 准备合并customVariables
        Map<String, Object> customVars = new HashMap<>();

        // 添加父类的customVariables（如果有的话）
        if (this.getCustomVariables() != null && !this.getCustomVariables().isEmpty()) {
            customVars.putAll(this.getCustomVariables());
        }

        // 添加测试专用字段
        if (this.executionId != null) {
            customVars.put("executionId", this.executionId);
        }
        if (this.executionTimestamp != null) {
            customVars.put("executionTimestamp", this.executionTimestamp);
        }
        if (this.executionMode != null) {
            customVars.put("executionMode", this.executionMode);
        }
        if (this.punchTime != null) {
            customVars.put("punchTime", this.punchTime);
        }
        if (this.punchType != null) {
            customVars.put("punchType", this.punchType);
        }
        if (this.scheduleStartTime != null) {
            customVars.put("scheduleStartTime", this.scheduleStartTime);
        }
        if (this.scheduleEndTime != null) {
            customVars.put("scheduleEndTime", this.scheduleEndTime);
        }
        if (this.workLocation != null) {
            customVars.put("workLocation", this.workLocation);
        }
        if (this.deviceId != null) {
            customVars.put("deviceId", this.deviceId);
        }
        if (this.deviceName != null) {
            customVars.put("deviceName", this.deviceName);
        }
        if (this.userAttributes != null && !this.userAttributes.isEmpty()) {
            customVars.put("userAttributes", this.userAttributes);
        }
        if (this.attendanceAttributes != null && !this.attendanceAttributes.isEmpty()) {
            customVars.put("attendanceAttributes", this.attendanceAttributes);
        }
        if (this.environmentParams != null && !this.environmentParams.isEmpty()) {
            customVars.put("environmentParams", this.environmentParams);
        }

        // 设置合并后的customVariables
        context.setCustomVariables(customVars);

        return context;
    }

    /**
     * 创建测试构建器
     */
    public static Builder testBuilder() {
        return new Builder();
    }

    /**
     * 手动Builder类
     * <p>
     * 避免Lombok @Builder的继承冲突
     * </p>
     */
    public static class Builder {
        private final RuleTestContext context = new RuleTestContext();

        public Builder employeeId(Long employeeId) {
            context.setEmployeeId(employeeId);
            return this;
        }

        public Builder employeeName(String employeeName) {
            context.setEmployeeName(employeeName);
            return this;
        }

        public Builder employeeNo(String employeeNo) {
            context.setEmployeeNo(employeeNo);
            return this;
        }

        public Builder departmentId(Long departmentId) {
            context.setDepartmentId(departmentId);
            return this;
        }

        public Builder departmentName(String departmentName) {
            context.setDepartmentName(departmentName);
            return this;
        }

        public Builder scheduleDate(LocalDate scheduleDate) {
            context.setScheduleDate(scheduleDate);
            return this;
        }

        public Builder shiftId(Long shiftId) {
            context.setShiftId(shiftId);
            return this;
        }

        public Builder shiftCode(String shiftCode) {
            context.setShiftCode(shiftCode);
            return this;
        }

        public Builder shiftType(Integer shiftType) {
            context.setShiftType(shiftType);
            return this;
        }

        public Builder consecutiveWorkDays(Integer days) {
            context.setConsecutiveWorkDays(days);
            return this;
        }

        public Builder restDays(Integer days) {
            context.setRestDays(days);
            return this;
        }

        public Builder monthlyWorkDays(Integer days) {
            context.setMonthlyWorkDays(days);
            return this;
        }

        public Builder monthlyWorkHours(Double hours) {
            context.setMonthlyWorkHours(hours);
            return this;
        }

        public Builder skills(String skills) {
            context.setSkills(skills);
            return this;
        }

        public Builder customVariables(Map<String, Object> customVariables) {
            context.setCustomVariables(customVariables);
            return this;
        }

        public Builder executionId(String executionId) {
            context.setExecutionId(executionId);
            return this;
        }

        public Builder executionTimestamp(LocalDateTime executionTimestamp) {
            context.setExecutionTimestamp(executionTimestamp);
            return this;
        }

        public Builder executionMode(String executionMode) {
            context.setExecutionMode(executionMode);
            return this;
        }

        public Builder punchTime(LocalTime punchTime) {
            context.setPunchTime(punchTime);
            return this;
        }

        public Builder punchType(String punchType) {
            context.setPunchType(punchType);
            return this;
        }

        public Builder scheduleStartTime(LocalTime scheduleStartTime) {
            context.setScheduleStartTime(scheduleStartTime);
            return this;
        }

        public Builder scheduleEndTime(LocalTime scheduleEndTime) {
            context.setScheduleEndTime(scheduleEndTime);
            return this;
        }

        public Builder workLocation(String workLocation) {
            context.setWorkLocation(workLocation);
            return this;
        }

        public Builder deviceId(String deviceId) {
            context.setDeviceId(deviceId);
            return this;
        }

        public Builder deviceName(String deviceName) {
            context.setDeviceName(deviceName);
            return this;
        }

        public Builder userAttributes(Map<String, Object> userAttributes) {
            context.setUserAttributes(userAttributes);
            return this;
        }

        public Builder attendanceAttributes(Map<String, Object> attendanceAttributes) {
            context.setAttendanceAttributes(attendanceAttributes);
            return this;
        }

        public Builder environmentParams(Map<String, Object> environmentParams) {
            context.setEnvironmentParams(environmentParams);
            return this;
        }

        public RuleTestContext build() {
            return context;
        }
    }
}
