package net.lab1024.sa.attendance.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 打卡验证结果
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PunchValidationResult {

    /**
     * 验证是否通过
     */
    private Boolean valid;

    /**
     * 验证错误代码
     */
    private String errorCode;

    /**
     * 验证错误信息
     */
    private String errorMessage;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工编号
     */
    private String employeeCode;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 打卡时间
     */
    private LocalDateTime punchTime;

    /**
     * 打卡类型：IN-上班，OUT-下班，BREAK-休息
     */
    private String punchType;

    /**
     * 验证类型：NORMAL-正常，LATE-迟到，EARLY-早退，ABSENCE-缺勤，OVERTIME-加班
     */
    private String validationType;

    /**
     * 是否在允许的时间范围内
     */
    private Boolean inTimeRange;

    /**
     * 是否在允许的地理范围内
     */
    private Boolean inLocationRange;

    /**
     * 验证规则ID
     */
    private Long ruleId;

    /**
     * 验证规则名称
     */
    private String ruleName;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties;

    /**
     * 创建验证成功结果
     *
     * @param employeeId 员工ID
     * @param punchTime 打卡时间
     * @param punchType 打卡类型
     * @return 成功结果
     */
    public static PunchValidationResult success(Long employeeId, LocalDateTime punchTime, String punchType) {
        return PunchValidationResult.builder()
                .valid(true)
                .employeeId(employeeId)
                .punchTime(punchTime)
                .punchType(punchType)
                .validationType("NORMAL")
                .inTimeRange(true)
                .build();
    }

    /**
     * 创建验证失败结果
     *
     * @param employeeId 员工ID
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static PunchValidationResult failure(Long employeeId, String errorCode, String errorMessage) {
        return PunchValidationResult.builder()
                .valid(false)
                .employeeId(employeeId)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建迟到结果
     *
     * @param employeeId 员工ID
     * @param punchTime 打卡时间
     * @param lateMinutes 迟到分钟数
     * @return 迟到结果
     */
    public static PunchValidationResult late(Long employeeId, LocalDateTime punchTime, Integer lateMinutes) {
        return PunchValidationResult.builder()
                .valid(true)
                .employeeId(employeeId)
                .punchTime(punchTime)
                .punchType("IN")
                .validationType("LATE")
                .inTimeRange(false)
                .errorMessage("迟到 " + lateMinutes + " 分钟")
                .build();
    }

    /**
     * 创建早退结果
     *
     * @param employeeId 员工ID
     * @param punchTime 打卡时间
     * @param earlyMinutes 早退分钟数
     * @return 早退结果
     */
    public static PunchValidationResult earlyLeave(Long employeeId, LocalDateTime punchTime, Integer earlyMinutes) {
        return PunchValidationResult.builder()
                .valid(true)
                .employeeId(employeeId)
                .punchTime(punchTime)
                .punchType("OUT")
                .validationType("EARLY")
                .inTimeRange(false)
                .errorMessage("早退 " + earlyMinutes + " 分钟")
                .build();
    }

    /**
     * 是否为迟到
     *
     * @return 是否为迟到
     */
    public boolean isLate() {
        return "LATE".equals(this.validationType);
    }

    /**
     * 是否为早退
     *
     * @return 是否为早退
     */
    public boolean isEarlyLeave() {
        return "EARLY".equals(this.validationType);
    }

    /**
     * 是否为正常打卡
     *
     * @return 是否为正常打卡
     */
    public boolean isNormal() {
        return "NORMAL".equals(this.validationType);
    }

    /**
     * 是否为异常打卡
     *
     * @return 是否为异常打卡
     */
    public boolean isAbnormal() {
        return isLate() || isEarlyLeave() || !Boolean.TRUE.equals(valid);
    }
}