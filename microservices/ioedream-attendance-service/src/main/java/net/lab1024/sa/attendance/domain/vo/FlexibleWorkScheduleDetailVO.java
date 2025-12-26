package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 弹性工作制配置详情VO
 * <p>
 * 包含完整的配置信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "弹性工作制配置详情VO")
public class FlexibleWorkScheduleDetailVO {

    // ==================== 基础信息 ====================

    @Schema(description = "配置ID", example = "1")
    private Long scheduleId;

    @Schema(description = "配置名称", example = "研发部弹性工作制")
    private String scheduleName;

    @Schema(description = "配置编码", example = "FLEX_RD_001")
    private String scheduleCode;

    @Schema(description = "弹性模式：STANDARD-标准弹性 FLEXIBLE-完全弹性 HYBRID-混合弹性", example = "STANDARD")
    private String flexMode;

    @Schema(description = "弹性模式描述", example = "标准弹性")
    private String flexModeDesc;

    @Schema(description = "适用部门ID列表", example = "[1, 2, 3]")
    private List<Long> departmentIds;

    @Schema(description = "适用部门名称列表", example = "[\"研发部\", \"技术部\"]")
    private List<String> departmentNames;

    @Schema(description = "描述", example = "研发部门标准弹性工作制，核心时间10:00-16:00")
    private String description;

    // ==================== 弹性上下班时间 ====================

    @Schema(description = "弹性上班时间（最早）", example = "07:00")
    private LocalTime flexStartEarliest;

    @Schema(description = "弹性上班时间（最晚）", example = "10:00")
    private LocalTime flexStartLatest;

    @Schema(description = "弹性下班时间（最早）", example = "16:00")
    private LocalTime flexEndEarliest;

    @Schema(description = "弹性下班时间（最晚）", example = "20:00")
    private LocalTime flexEndLatest;

    @Schema(description = "弹性上班时间范围", example = "07:00-10:00")
    private String flexStartRange;

    @Schema(description = "弹性下班时间范围", example = "16:00-20:00")
    private String flexEndRange;

    // ==================== 核心工作时间（标准弹性模式） ====================

    @Schema(description = "核心工作开始时间", example = "10:00")
    private LocalTime coreStartTime;

    @Schema(description = "核心工作结束时间", example = "16:00")
    private LocalTime coreEndTime;

    @Schema(description = "核心工作时间", example = "10:00-16:00")
    private String coreTimeRange;

    @Schema(description = "核心工作时长（分钟）", example = "360")
    private Integer coreWorkDuration;

    // ==================== 工作时长要求 ====================

    @Schema(description = "最少工作时长（分钟）", example = "480")
    private Integer minWorkDuration;

    @Schema(description = "标准工作时长（分钟）", example = "480")
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
    private Integer requireCoreTimeCheck;

    @Schema(description = "允许弹性加班：0-否 1-是", example = "1")
    private Integer allowFlexibleOvertime;

    @Schema(description = "跨天弹性：0-否 1-是", example = "0")
    private Integer crossDayFlex;

    // ==================== 午休时间 ====================

    @Schema(description = "午休开始时间", example = "12:00")
    private LocalTime lunchStartTime;

    @Schema(description = "午休结束时间", example = "13:00")
    private LocalTime lunchEndTime;

    @Schema(description = "午休时长（分钟）", example = "60")
    private Integer lunchDuration;

    @Schema(description = "午休是否计入工作时长：0-否 1-是", example = "0")
    private Integer lunchCountAsWork;

    // ==================== 高级规则 ====================

    @Schema(description = "弹性周最小工作天数", example = "4")
    private Integer minWorkDaysPerWeek;

    @Schema(description = "弹性周最大工作天数", example = "6")
    private Integer maxWorkDaysPerWeek;

    @Schema(description = "允许远程工作：0-否 1-是", example = "1")
    private Integer allowRemoteWork;

    @Schema(description = "远程工作需要审批：0-否 1-是", example = "1")
    private Integer remoteWorkRequiresApproval;

    // ==================== 状态信息 ====================

    @Schema(description = "配置状态：1-启用 0-禁用", example = "1")
    private Integer status;

    @Schema(description = "配置状态描述", example = "启用")
    private String statusDesc;

    @Schema(description = "生效时间", example = "2025-01-01T00:00:00")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间", example = "2025-01-01T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-15T14:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    @Schema(description = "创建人姓名", example = "张三")
    private String createUserName;

    @Schema(description = "更新人ID", example = "2")
    private Long updateUserId;

    @Schema(description = "更新人姓名", example = "李四")
    private String updateUserName;

    // ==================== 统计信息 ====================

    @Schema(description = "已分配员工数", example = "120")
    private Integer assignedEmployeeCount;

    @Schema(description = "本月使用次数", example = "2500")
    private Integer monthlyUsageCount;

    @Schema(description = "平均迟到率（%）", example = "5.2")
    private Double averageLateRate;

    @Schema(description = "平均早退率（%）", example = "2.8")
    private Double averageEarlyLeaveRate;

    // ==================== 扩展字段 ====================

    @Schema(description = "扩展属性（JSON格式）", example = "{\"customField1\":\"value1\"}")
    private Map<String, Object> extendedAttributes;

    @Schema(description = "规则描述摘要", example = "核心时间10:00-16:00，弹性上下班，标准工作8小时")
    private String ruleSummary;
}
