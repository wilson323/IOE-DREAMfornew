package net.lab1024.sa.attendance.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 考勤记录创建 DTO (Data Transfer Object)
 *
 * 严格遵循repowiki规范:
 * - 用于创建考勤记录的数据传输对象
 * - 完整的参数验证注解
 * - 支持批量创建功能
 * - 包含审计字段
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "考勤记录创建DTO")
public class AttendanceRecordCreateDTO {

    @NotNull(message = "员工ID不能为空")
    @Schema(description = "员工ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long employeeId;

    @Schema(description = "部门ID")
    private Long departmentId;

    @NotNull(message = "考勤日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "考勤日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate attendanceDate;

    @Schema(description = "上班打卡时间")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime punchInTime;

    @Schema(description = "下班打卡时间")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime punchOutTime;

    @Schema(description = "上班打卡位置")
    private String punchInLocation;

    @Schema(description = "下班打卡位置")
    private String punchOutLocation;

    @Schema(description = "上班打卡设备")
    private String punchInDevice;

    @Schema(description = "下班打卡设备")
    private String punchOutDevice;

    @Schema(description = "上班打卡照片")
    private String punchInPhoto;

    @Schema(description = "下班打卡照片")
    private String punchOutPhoto;

    @Schema(description = "考勤状态：NORMAL-正常, LATE-迟到, EARLY_LEAVE-早退, ABSENT-旷工, ABNORMAL-异常")
    private String attendanceStatus;

    @Schema(description = "异常类型：LATE-迟到, EARLY_LEAVE-早退, ABSENTEEISM-旷工, FORGET_PUNCH-忘记打卡, INCOMPLETE_RECORD-记录不完整")
    private String exceptionType;

    @Schema(description = "工作时长（小时）")
    private BigDecimal workHours;

    @Schema(description = "加班时长（小时）")
    private BigDecimal overtimeHours;

    @Schema(description = "迟到时长（分钟）")
    private Integer lateMinutes;

    @Schema(description = "早退时长（分钟）")
    private Integer earlyLeaveMinutes;

    @Schema(description = "是否需要审批：0-否，1-是")
    private Integer needApproval;

    @Schema(description = "是否外勤打卡：0-否，1-是")
    private Integer isOutdoor;

    @Schema(description = "外勤事由")
    private String outdoorReason;

    @Schema(description = "是否补卡：0-否，1-是")
    private Integer isMakeUp;

    @Schema(description = "补卡事由")
    private String makeUpReason;

    @Schema(description = "补卡审批人ID")
    private Long makeUpApprovalUserId;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "数据来源：SYSTEM-系统生成，MANUAL-手动录入，DEVICE-设备导入，API-接口导入")
    private String dataSource;

    @Schema(description = "同步来源ID")
    private String syncSourceId;

    @Schema(description = "同步时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime syncTime;
}
