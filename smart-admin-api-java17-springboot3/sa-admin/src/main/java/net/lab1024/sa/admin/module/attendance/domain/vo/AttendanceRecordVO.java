package net.lab1024.sa.admin.module.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考勤记录 VO (View Object)
 *
 * 严格遵循repowiki规范:
 * - 用于前端展示的数据传输对象
 * - 只包含展示需要的字段
 * - 使用@JsonInclude忽略null值
 * - 完整的Swagger注解
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "考勤记录VO")
public class AttendanceRecordVO {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "考勤日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
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

    @Schema(description = "考勤状态描述")
    private String attendanceStatusDesc;

    @Schema(description = "异常类型：LATE-迟到, EARLY_LEAVE-早退, ABSENTEEISM-旷工, FORGET_PUNCH-忘记打卡, INCOMPLETE_RECORD-记录不完整")
    private String exceptionType;

    @Schema(description = "异常类型描述")
    private String exceptionTypeDesc;

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

    @Schema(description = "审批人姓名")
    private String approvalUserName;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime updateTime;
}