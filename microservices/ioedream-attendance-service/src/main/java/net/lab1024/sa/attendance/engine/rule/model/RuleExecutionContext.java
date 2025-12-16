package net.lab1024.sa.attendance.engine.rule.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * 规则执行上下文模型
 * <p>
 * 规则引擎执行时的上下文信息，包含用户数据、考勤数据、环境参数等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionContext {

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 考勤记录ID
     */
    private Long attendanceRecordId;

    /**
     * 考勤日期
     */
    private LocalDate attendanceDate;

    /**
     * 打卡时间
     */
    private LocalTime punchTime;

    /**
     * 打卡类型：IN-上班 OUT-下班
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
     * 打卡位置信息
     */
    private Map<String, Object> locationInfo;

    /**
     * 用户相关属性
     */
    private Map<String, Object> userAttributes;

    /**
     * 考勤相关属性
     */
    private Map<String, Object> attendanceAttributes;

    /**
     * 环境参数
     */
    private Map<String, Object> environmentParams;

    /**
     * 执行时间戳
     */
    private LocalDateTime executionTimestamp;

    /**
     * 会话ID（用于跟踪同一个执行会话中的多次规则执行）
     */
    private String sessionId;

    /**
     * 触发事件类型
     */
    private String triggerEventType;

    /**
     * 执行模式：NORMAL-正常 DEBUG-调试 TEST-测试
     */
    private String executionMode;

    /**
     * 规则过滤器（只执行匹配的规则）
     */
    private Map<String, Object> ruleFilters;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 设置用户属性
     */
    public void setUserAttribute(String key, Object value) {
        if (userAttributes == null) {
            userAttributes = new java.util.HashMap<>();
        }
        userAttributes.put(key, value);
    }

    /**
     * 获取用户属性
     */
    public Object getUserAttribute(String key) {
        return userAttributes != null ? userAttributes.get(key) : null;
    }

    /**
     * 设置考勤属性
     */
    public void setAttendanceAttribute(String key, Object value) {
        if (attendanceAttributes == null) {
            attendanceAttributes = new java.util.HashMap<>();
        }
        attendanceAttributes.put(key, value);
    }

    /**
     * 获取考勤属性
     */
    public Object getAttendanceAttribute(String key) {
        return attendanceAttributes != null ? attendanceAttributes.get(key) : null;
    }

    /**
     * 设置环境参数
     */
    public void setEnvironmentParam(String key, Object value) {
        if (environmentParams == null) {
            environmentParams = new java.util.HashMap<>();
        }
        environmentParams.put(key, value);
    }

    /**
     * 获取环境参数
     */
    public Object getEnvironmentParam(String key) {
        return environmentParams != null ? environmentParams.get(key) : null;
    }
}