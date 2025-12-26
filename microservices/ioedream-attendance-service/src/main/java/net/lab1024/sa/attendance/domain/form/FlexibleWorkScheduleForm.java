package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * 弹性工作制配置表单
 * <p>
 * 用于创建和更新弹性工作制配置
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "弹性工作制配置表单")
public class FlexibleWorkScheduleForm {

    // ==================== 基础信息 ====================

    @Schema(description = "配置ID（更新时必填）", example = "1")
    private Long scheduleId;

    @Schema(description = "配置名称", required = true, example = "研发部弹性工作制")
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100个字符")
    private String scheduleName;

    @Schema(description = "配置编码", required = true, example = "FLEX_RD_001")
    @NotBlank(message = "配置编码不能为空")
    @Size(max = 50, message = "配置编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "配置编码只能包含大写字母、数字和下划线")
    private String scheduleCode;

    @Schema(description = "弹性模式：STANDARD-标准弹性 FLEXIBLE-完全弹性 HYBRID-混合弹性", required = true, example = "STANDARD")
    @NotBlank(message = "弹性模式不能为空")
    @Pattern(regexp = "^(STANDARD|FLEXIBLE|HYBRID)$", message = "弹性模式必须是STANDARD、FLEXIBLE或HYBRID")
    private String flexMode;

    @Schema(description = "适用部门ID列表", example = "[1, 2, 3]")
    private List<Long> departmentIds;

    @Schema(description = "描述", example = "研发部门标准弹性工作制，核心时间10:00-16:00")
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    // ==================== 弹性上下班时间 ====================

    @Schema(description = "弹性上班时间（最早）", required = true, example = "07:00")
    @NotNull(message = "弹性上班最早时间不能为空")
    private LocalTime flexStartEarliest;

    @Schema(description = "弹性上班时间（最晚）", required = true, example = "10:00")
    @NotNull(message = "弹性上班最晚时间不能为空")
    private LocalTime flexStartLatest;

    @Schema(description = "弹性下班时间（最早）", required = true, example = "16:00")
    @NotNull(message = "弹性下班最早时间不能为空")
    private LocalTime flexEndEarliest;

    @Schema(description = "弹性下班时间（最晚）", required = true, example = "20:00")
    @NotNull(message = "弹性下班最晚时间不能为空")
    private LocalTime flexEndLatest;

    // ==================== 核心工作时间（标准弹性模式） ====================

    @Schema(description = "核心工作开始时间", example = "10:00")
    private LocalTime coreStartTime;

    @Schema(description = "核心工作结束时间", example = "16:00")
    private LocalTime coreEndTime;

    // ==================== 工作时长要求 ====================

    @Schema(description = "最少工作时长（分钟）", required = true, example = "480")
    @NotNull(message = "最少工作时长不能为空")
    private Integer minWorkDuration;

    @Schema(description = "标准工作时长（分钟）", required = true, example = "480")
    @NotNull(message = "标准工作时长不能为空")
    private Integer standardWorkDuration;

    @Schema(description = "最多工作时长（分钟）", example = "600")
    private Integer maxWorkDuration;

    // ==================== 宽限时间 ====================

    @Schema(description = "迟到宽限时间（分钟）", example = "10")
    private Integer lateTolerance;

    @Schema(description = "早退宽限时间（分钟）", example = "10")
    private Integer earlyTolerance;

    // ==================== 考勤规则 ====================

    @Schema(description = "必须打卡核心时间：0-否 1-是", example = "1")
    @NotNull(message = "必须打卡核心时间标识不能为空")
    private Integer requireCoreTimeCheck;

    @Schema(description = "允许弹性加班：0-否 1-是", example = "1")
    @NotNull(message = "允许弹性加班标识不能为空")
    private Integer allowFlexibleOvertime;

    @Schema(description = "跨天弹性：0-否 1-是", example = "0")
    @NotNull(message = "跨天弹性标识不能为空")
    private Integer crossDayFlex;

    // ==================== 午休时间 ====================

    @Schema(description = "午休开始时间", example = "12:00")
    private LocalTime lunchStartTime;

    @Schema(description = "午休结束时间", example = "13:00")
    private LocalTime lunchEndTime;

    @Schema(description = "午休是否计入工作时长：0-否 1-是", example = "0")
    @NotNull(message = "午休计入标识不能为空")
    private Integer lunchCountAsWork;

    // ==================== 高级规则 ====================

    @Schema(description = "弹性周最小工作天数", example = "4")
    private Integer minWorkDaysPerWeek;

    @Schema(description = "弹性周最大工作天数", example = "6")
    private Integer maxWorkDaysPerWeek;

    @Schema(description = "允许远程工作：0-否 1-是", example = "1")
    @NotNull(message = "允许远程工作标识不能为空")
    private Integer allowRemoteWork;

    @Schema(description = "远程工作需要审批：0-否 1-是", example = "1")
    private Integer remoteWorkRequiresApproval;

    // ==================== 状态信息 ====================

    @Schema(description = "配置状态：1-启用 0-禁用", example = "1")
    @NotNull(message = "配置状态不能为空")
    private Integer status;

    @Schema(description = "生效时间", example = "2025-01-01T00:00:00")
    private java.time.LocalDateTime effectiveTime;

    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private java.time.LocalDateTime expireTime;

    // ==================== 扩展字段 ====================

    @Schema(description = "扩展属性（JSON格式）", example = "{\"customField1\":\"value1\"}")
    private String extendedAttributes;
}
