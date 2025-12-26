package net.lab1024.sa.attendance.engine.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

/**
 * 规则执行上下文
 * <p>
 * 封装规则执行时需要的所有变量和环境信息：
 * - 员工信息
 * - 班次信息
 * - 日期信息
 * - 部门信息
 * - 自定义变量
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class RuleExecutionContext {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 班次ID
     */
    private Long shiftId;

    /**
     * 班次编码
     */
    private String shiftCode;

    /**
     * 班次类型
     */
    private Integer shiftType;

    /**
     * 当前连续工作天数
     */
    private Integer consecutiveWorkDays;

    /**
     * 当前休息天数
     */
    private Integer restDays;

    /**
     * 当月已工作天数
     */
    private Integer monthlyWorkDays;

    /**
     * 当月已工作时数
     */
    private Double monthlyWorkHours;

    /**
     * 员工技能列表（JSON数组格式）
     */
    private String skills;

    /**
     * 自定义变量（用于扩展）
     */
    private Map<String, Object> customVariables;

    /**
     * 获取所有变量（用于传递给Aviator表达式）
     */
    public Map<String, Object> getVariables() {
        Map<String, Object> vars = new java.util.HashMap<>();

        // 员工信息
        vars.put("employeeId", employeeId);
        vars.put("employeeName", employeeName);
        vars.put("employeeNo", employeeNo);

        // 部门信息
        vars.put("departmentId", departmentId);
        vars.put("departmentName", departmentName);

        // 日期信息
        vars.put("scheduleDate", scheduleDate);
        vars.put("year", scheduleDate != null ? scheduleDate.getYear() : null);
        vars.put("month", scheduleDate != null ? scheduleDate.getMonthValue() : null);
        vars.put("day", scheduleDate != null ? scheduleDate.getDayOfMonth() : null);
        vars.put("dayOfWeek", scheduleDate != null ? scheduleDate.getDayOfWeek().getValue() : null);

        // 班次信息
        vars.put("shiftId", shiftId);
        vars.put("shiftCode", shiftCode);
        vars.put("shiftType", shiftType);

        // 统计信息
        vars.put("consecutiveWorkDays", consecutiveWorkDays);
        vars.put("restDays", restDays);
        vars.put("monthlyWorkDays", monthlyWorkDays);
        vars.put("monthlyWorkHours", monthlyWorkHours);

        // 技能信息
        vars.put("skills", skills);

        // 自定义变量
        if (customVariables != null) {
            vars.putAll(customVariables);
        }

        return vars;
    }

    /**
     * 创建构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建器类
     */
    public static class Builder {
        private final RuleExecutionContext context = new RuleExecutionContext();

        public Builder employeeId(Long employeeId) {
            context.setEmployeeId(employeeId);
            return this;
        }

        public Builder employeeName(String employeeName) {
            context.setEmployeeName(employeeName);
            return this;
        }

        public Builder departmentId(Long departmentId) {
            context.setDepartmentId(departmentId);
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

        public Builder customVariable(String key, Object value) {
            if (context.getCustomVariables() == null) {
                context.setCustomVariables(new java.util.HashMap<>());
            }
            context.getCustomVariables().put(key, value);
            return this;
        }

        public RuleExecutionContext build() {
            return context;
        }
    }
}
