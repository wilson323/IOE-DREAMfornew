package net.lab1024.sa.admin.module.attendance.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 考勤规则处理结果
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRuleProcessResult {

    /**
     * 处理是否成功
     */
    private Boolean success;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties;

    /**
     * 创建成功结果
     *
     * @return 成功结果
     */
    public static AttendanceRuleProcessResult success() {
        return AttendanceRuleProcessResult.builder()
                .success(true)
                .processTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功结果（带规则信息）
     *
     * @param ruleId 规则ID
     * @param ruleName 规则名称
     * @return 成功结果
     */
    public static AttendanceRuleProcessResult success(Long ruleId, String ruleName) {
        return AttendanceRuleProcessResult.builder()
                .success(true)
                .ruleId(ruleId)
                .ruleName(ruleName)
                .processTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static AttendanceRuleProcessResult failure(String errorCode, String errorMessage) {
        return AttendanceRuleProcessResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .processTime(LocalDateTime.now())
                .build();
    }

    // ==================== 兼容性getter方法 ====================

    /**
     * 获取考勤状态
     * 兼容性方法，从扩展属性中获取考勤状态
     *
     * @return 考勤状态
     */
    public String getAttendanceStatus() {
        if (extendedProperties != null && extendedProperties.containsKey("attendanceStatus")) {
            return (String) extendedProperties.get("attendanceStatus");
        }
        return success ? "NORMAL" : null;
    }

    /**
     * 设置考勤状态到扩展属性
     *
     * @param attendanceStatus 考勤状态
     */
    public void setAttendanceStatus(String attendanceStatus) {
        if (extendedProperties == null) {
            extendedProperties = new java.util.HashMap<>();
        }
        extendedProperties.put("attendanceStatus", attendanceStatus);
    }
}