package net.lab1024.sa.attendance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 考勤记录实体
 *
 * 严格遵循repowiki规范:
 * - 继承BaseEntity，包含审计字段
 * - 使用jakarta包，避免javax包
 * - 使用Lombok简化代码
 * - 字段命名规范：下划线分隔
 * - 完整的业务逻辑方法
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_attendance_record")
public class AttendanceRecordEntity extends BaseEntity {

    /**
     * 记录ID
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 考勤日期
     */
    @TableField("attendance_date")
    private LocalDate attendanceDate;

    /**
     * 上班打卡时间
     */
    @TableField("punch_in_time")
    private LocalTime punchInTime;

    /**
     * 下班打卡时间
     */
    @TableField("punch_out_time")
    private LocalTime punchOutTime;

    /**
     * 上班打卡位置(JSON格式)
     * 格式: {latitude: 39.9042, longitude: 116.4074, address: "详细地址", accuracy: 10.0}
     */
    @TableField("punch_in_location")
    private String punchInLocation;

    /**
     * 下班打卡位置(JSON格式)
     */
    @TableField("punch_out_location")
    private String punchOutLocation;

    /**
     * 上班打卡设备ID
     */
    @TableField("punch_in_device_id")
    private Long punchInDeviceId;

    /**
     * 下班打卡设备ID
     */
    @TableField("punch_out_device_id")
    private Long punchOutDeviceId;

    /**
     * 上班打卡照片
     */
    @TableField("punch_in_photo")
    private String punchInPhoto;

    /**
     * 下班打卡照片
     */
    @TableField("punch_out_photo")
    private String punchOutPhoto;

    /**
     * 工作时长(小时)
     */
    @TableField("work_hours")
    private BigDecimal workHours;

    /**
     * 加班时长(小时)
     */
    @TableField("overtime_hours")
    private BigDecimal overtimeHours;

    /**
     * 考勤状态
     * NORMAL-正常, LATE-迟到, EARLY_LEAVE-早退, ABSENT-旷工, LEAVE-请假
     */
    @TableField("attendance_status")
    private String attendanceStatus;

    /**
     * 异常类型
     * LATE-迟到, EARLY_LEAVE-早退, ABSENTEEISM-旷工, FORGET_PUNCH-忘打卡
     */
    @TableField("exception_type")
    private String exceptionType;

    /**
     * 异常原因
     */
    @TableField("exception_reason")
    private String exceptionReason;

    /**
     * 是否已处理异常
     * 0-未处理, 1-已处理
     */
    @TableField("is_processed")
    private Integer isProcessed;

    /**
     * 异常处理人ID
     */
    @TableField("processed_by")
    private Long processedBy;

    /**
     * 异常处理时间
     */
    @TableField("processed_time")
    private LocalDateTime processedTime;

    /**
     * 验证方式
     */
    @TableField("verification_method")
    private String verificationMethod;

    /**
     * 设备编码
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 上班打卡时间
     */
    @TableField("punch_in_date_time")
    private LocalDateTime punchInDateTime;

    /**
     * 下班打卡时间
     */
    @TableField("punch_out_datetime")
    private LocalDateTime punchOutDatetime;

    /**
     * 照片URL
     */
    @TableField("photo_url")
    private String photoUrl;

    /**
     * GPS纬度
     */
    @TableField("gps_latitude")
    private Double gpsLatitude;

    /**
     * GPS经度
     */
    @TableField("gps_longitude")
    private Double gpsLongitude;

    /**
     * GPS验证是否通过
     */
    @TableField("gps_validation")
    private Boolean gpsValidation;

    /**
     * 处理备注
     */
    @TableField("process_remark")
    private String processRemark;

    /**
     * 迟到分钟数
     */
    @TableField("late_minutes")
    private BigDecimal lateMinutes;

    /**
     * 早退分钟数
     */
    @TableField("early_leave_minutes")
    private BigDecimal earlyLeaveMinutes;

    /**
     * 实际工作时长(小时)
     */
    @TableField("actual_work_hours")
    private BigDecimal actualWorkHours;

    /**
     * 处理类型
     */
    @TableField("process_type")
    private String processType;

    /**
     * 处理原因
     */
    @TableField("process_reason")
    private String processReason;

    // 兼容性字段，保留原有字段但标记为废弃
    /**
     * @deprecated 使用 punchInTime 和 punchOutTime 替代
     */
    @Deprecated
    private LocalDateTime clockTime;

    /**
     * @deprecated 使用 punchInDeviceId 和 punchOutDeviceId 替代
     */
    @Deprecated
    private Long deviceId;

    /**
     * @deprecated 使用 punchInLocation 和 punchOutLocation 替代
     */
    @Deprecated
    private String clockType; // IN/OUT

    /**
     * 计算工作时长
     * 根据打卡时间计算实际工作时长
     *
     * @return 工作时长(小时)
     */
    public BigDecimal calculateWorkHours() {
        if (punchInTime != null && punchOutTime != null) {
            // 简单计算，后续可考虑休息时间
            LocalTime start = punchInTime;
            LocalTime end = punchOutTime;

            // 处理跨天情况
            if (end.isBefore(start)) {
                // 跨天工作，加上24小时
                end = end.plusHours(24);
            }

            long minutes = java.time.Duration.between(start, end).toMinutes();
            return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 检查是否有打卡记录
     *
     * @return 是否有打卡记录
     */
    public boolean hasPunchIn() {
        return punchInTime != null;
    }

    /**
     * 检查是否有下班记录
     *
     * @return 是否有下班记录
     */
    public boolean hasPunchOut() {
        return punchOutTime != null;
    }

    /**
     * 检查是否完整打卡记录
     *
     * @return 是否有完整的上下班打卡
     */
    public boolean isCompleteRecord() {
        return hasPunchIn() && hasPunchOut();
    }

    /**
     * 检查是否有异常
     *
     * @return 是否有异常
     */
    public boolean hasException() {
        return exceptionType != null && !exceptionType.trim().isEmpty();
    }

    /**
     * 获取打卡状态描述
     *
     * @return 状态描述
     */
    public String getStatusDescription() {
        if (attendanceStatus == null) {
            return "未打卡";
        }

        switch (attendanceStatus) {
            case "NORMAL":
                return "正常";
            case "LATE":
                return "迟到";
            case "EARLY_LEAVE":
                return "早退";
            case "ABSENT":
                return "旷工";
            case "LEAVE":
                return "请假";
            default:
                return attendanceStatus;
        }
    }

    /**
     * 设置打卡状态
     *
     * @param isLate       是否迟到
     * @param isEarlyLeave 是否早退
     * @param isAbsent     是否旷工
     */
    public void setAttendanceStatus(boolean isLate, boolean isEarlyLeave, boolean isAbsent) {
        if (isAbsent) {
            this.attendanceStatus = "ABSENT";
            this.exceptionType = "ABSENTEEISM";
        } else if (isLate && isEarlyLeave) {
            this.attendanceStatus = "ABNORMAL";
            this.exceptionType = "LATE_EARLY_LEAVE";
        } else if (isLate) {
            this.attendanceStatus = "LATE";
            this.exceptionType = "LATE";
        } else if (isEarlyLeave) {
            this.attendanceStatus = "EARLY_LEAVE";
            this.exceptionType = "EARLY_LEAVE";
        } else {
            this.attendanceStatus = "NORMAL";
            this.exceptionType = null;
        }
    }

    // ==================== 手动添加的getter/setter方法（解决Lombok编译问题） ====================

    public Long getRecordId() {
        return recordId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public LocalTime getPunchInTime() {
        return punchInTime;
    }

    public LocalTime getPunchOutTime() {
        return punchOutTime;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }

    public String getPunchInLocation() {
        return punchInLocation;
    }

    public String getPunchOutLocation() {
        return punchOutLocation;
    }

    public Long getPunchInDeviceId() {
        return punchInDeviceId;
    }

    public Long getPunchOutDeviceId() {
        return punchOutDeviceId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setPunchInDevice(String punchInDevice) {
        // 兼容性方法，保留原有逻辑
    }

    public void setPunchOutDevice(String punchOutDevice) {
        // 兼容性方法，保留原有逻辑
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public void setPunchInTime(LocalTime punchInTime) {
        this.punchInTime = punchInTime;
    }

    public void setPunchOutTime(LocalTime punchOutTime) {
        this.punchOutTime = punchOutTime;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public void setWorkHours(BigDecimal workHours) {
        this.workHours = workHours;
    }

    public void setPunchInLocation(String punchInLocation) {
        this.punchInLocation = punchInLocation;
    }

    public void setPunchOutLocation(String punchOutLocation) {
        this.punchOutLocation = punchOutLocation;
    }

    public void setPunchInDeviceId(Long punchInDeviceId) {
        this.punchInDeviceId = punchInDeviceId;
    }

    public void setPunchOutDeviceId(Long punchOutDeviceId) {
        this.punchOutDeviceId = punchOutDeviceId;
    }
}
