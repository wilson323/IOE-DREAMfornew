package net.lab1024.sa.attendance.domain.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 考勤记录更新 DTO (Data Transfer Object)
 *
 * 严格遵循repowiki规范:
 * - 用于更新考勤记录的数据传输对象
 * - 只包含可更新的字段
 * - 完整的参数验证注解
 * - 支持部分更新功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "考勤记录更新DTO")
public class AttendanceRecordUpdateDTO {

    @NotNull(message = "记录ID不能为空")
    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recordId;

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

    @Schema(description = "审批状态：PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝")
    private String approvalStatus;

    @Schema(description = "审批时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime approvalTime;

    @Schema(description = "审批人ID")
    private Long approvalUserId;

    @Schema(description = "审批意见")
    private String approvalRemark;

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

    @Schema(description = "补卡审批状态：PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝")
    private String makeUpApprovalStatus;

    @Schema(description = "补卡审批时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime makeUpApprovalTime;

    @Schema(description = "补卡审批意见")
    private String makeUpApprovalRemark;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "版本号（用于乐观锁）")
    private Integer version;
}
