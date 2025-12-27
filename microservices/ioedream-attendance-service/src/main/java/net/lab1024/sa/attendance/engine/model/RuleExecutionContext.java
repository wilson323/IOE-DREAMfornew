package net.lab1024.sa.attendance.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private Map<String, Object> customVariables = new HashMap<>();

    /**
     * 执行ID（用于唯一标识规则执行实例）
     */
    private String executionId;

    /**
     * 会话ID（用于缓存关联）
     */
    private String sessionId;

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
     * 获取用户ID（别名方法，兼容不同命名）
     * <p>
     * 返回employeeId字段值
     * </p>
     *
     * @return 用户ID
     */
    public Long getUserId() {
        return employeeId;
    }

    /**
     * 设置用户ID（别名方法，同时设置employeeId）
     *
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.employeeId = userId;
    }

    /**
     * 获取考勤日期（别名方法，兼容不同命名）
     * <p>
     * 返回scheduleDate字段值
     * </p>
     *
     * @return 考勤日期
     */
    public LocalDate getAttendanceDate() {
        return scheduleDate;
    }

    /**
     * 设置考勤日期（别名方法，同时设置scheduleDate）
     *
     * @param attendanceDate 考勤日期
     */
    public void setAttendanceDate(LocalDate attendanceDate) {
        this.scheduleDate = attendanceDate;
    }
}
