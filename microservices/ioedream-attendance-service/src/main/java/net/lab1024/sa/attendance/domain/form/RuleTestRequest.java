package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/**
 * 规则测试请求表单
 * <p>
 * 用于规则测试工具的测试请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "规则测试请求表单")
public class RuleTestRequest {

    /**
     * 规则ID（测试特定规则时使用）
     */
    @Schema(description = "规则ID", example = "1234567890")
    private Long ruleId;

    /**
     * 规则条件（JSON格式）
     * 测试自定义条件时使用
     */
    @Schema(description = "规则条件（JSON）", example = "{\"lateMinutes\": 5}")
    private String ruleCondition;

    /**
     * 规则动作（JSON格式）
     * 测试自定义动作时使用
     */
    @Schema(description = "规则动作（JSON）", example = "{\"deductAmount\": 50}")
    private String ruleAction;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "研发部")
    private String departmentName;

    /**
     * 考勤日期
     */
    @Schema(description = "考勤日期", example = "2024-01-01")
    private LocalDate attendanceDate;

    /**
     * 打卡时间
     */
    @Schema(description = "打卡时间", example = "08:35:00")
    private LocalTime punchTime;

    /**
     * 打卡类型：IN-上班 OUT-下班
     */
    @Schema(description = "打卡类型", example = "IN")
    private String punchType;

    /**
     * 排班开始时间
     */
    @Schema(description = "排班开始时间", example = "08:30:00")
    private LocalTime scheduleStartTime;

    /**
     * 排班结束时间
     */
    @Schema(description = "排班结束时间", example = "17:30:00")
    private LocalTime scheduleEndTime;

    /**
     * 工作地点
     */
    @Schema(description = "工作地点", example = "总部大楼")
    private String workLocation;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "DEV001")
    private String deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "1号门禁")
    private String deviceName;

    /**
     * 用户属性（JSON格式）
     * 示例：{"position": "工程师", "level": 5}
     */
    @Schema(description = "用户属性（JSON）", example = "{\"position\": \"工程师\"}")
    private Map<String, Object> userAttributes;

    /**
     * 考勤属性（JSON格式）
     * 示例：{"isHoliday": false, "isWeekend": false}
     */
    @Schema(description = "考勤属性（JSON）", example = "{\"isHoliday\": false}")
    private Map<String, Object> attendanceAttributes;

    /**
     * 环境参数（JSON格式）
     * 示例：{"temperature": 25, "weather": "晴"}
     */
    @Schema(description = "环境参数（JSON）", example = "{}")
    private Map<String, Object> environmentParams;
}
