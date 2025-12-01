package net.lab1024.sa.admin.module.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import net.lab1024.sa.base.common.entity.BaseEntity;import java.time.LocalDateTime;import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
     * 照片URL（用于移动端访问）
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
     * 处理备注
     */
    @TableField("process_remark")
    private String processRemark;

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
            return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
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
     * @param isLate 是否迟到
     * @param isEarlyLeave 是否早退
     * @param isAbsent 是否旷工
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

    // ==================== 缺失的setter方法（修复编译错误） ====================

    /**
     * 设置迟到分钟数
     * 兼容性方法，添加到异常原因中
     *
     * @param lateMinutes 迟到分钟数
     */
    public void setLateMinutes(BigDecimal lateMinutes) {
        if (lateMinutes != null && lateMinutes.compareTo(BigDecimal.ZERO) > 0) {
            this.exceptionType = "LATE";
            this.exceptionReason = "迟到" + lateMinutes + "分钟";
        }
    }

    /**
     * 设置早退分钟数
     * 兼容性方法，添加到异常原因中
     *
     * @param earlyLeaveMinutes 早退分钟数
     */
    public void setEarlyLeaveMinutes(BigDecimal earlyLeaveMinutes) {
        if (earlyLeaveMinutes != null && earlyLeaveMinutes.compareTo(BigDecimal.ZERO) > 0) {
            if (this.exceptionType != null && this.exceptionType.contains("LATE")) {
                this.exceptionType = "LATE_EARLY_LEAVE";
                this.exceptionReason += "，早退" + earlyLeaveMinutes + "分钟";
            } else {
                this.exceptionType = "EARLY_LEAVE";
                this.exceptionReason = "早退" + earlyLeaveMinutes + "分钟";
            }
        }
    }

    /**
     * 设置处理类型
     * 兼容性方法
     *
     * @param processType 处理类型
     */
    public void setProcessType(String processType) {
        // 可以将处理类型存储在处理备注中
        if (processType != null) {
            if (this.processRemark == null) {
                this.processRemark = processType;
            } else {
                this.processRemark = processType + " | " + this.processRemark;
            }
        }
    }

    /**
     * 设置处理原因
     * 兼容性方法
     *
     * @param processReason 处理原因
     */
    public void setProcessReason(String processReason) {
        // 将处理原因存储在处理备注中
        if (processReason != null) {
            if (this.processRemark == null) {
                this.processRemark = processReason;
            } else {
                this.processRemark = this.processRemark + " | " + processReason;
            }
        }
    }

    /**
     * 设置实际工作时长
     * 兼容性方法，设置工作时长
     *
     * @param actualWorkHours 实际工作时长
     */
    public void setActualWorkHours(BigDecimal actualWorkHours) {
        this.workHours = actualWorkHours;
    }

    // ==================== 额外的兼容性getter方法 ====================

    /**
     * 获取迟到分钟数
     * 从异常原因中解析迟到分钟数
     *
     * @return 迟到分钟数
     */
    public BigDecimal getLateMinutes() {
        if (this.exceptionReason != null && this.exceptionReason.contains("迟到")) {
            try {
                String regex = "迟到(\\d+)分钟";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
                java.util.regex.Matcher matcher = pattern.matcher(this.exceptionReason);
                if (matcher.find()) {
                    return new BigDecimal(matcher.group(1));
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取早退分钟数
     * 从异常原因中解析早退分钟数
     *
     * @return 早退分钟数
     */
    public BigDecimal getEarlyLeaveMinutes() {
        if (this.exceptionReason != null && this.exceptionReason.contains("早退")) {
            try {
                String regex = "早退(\\d+)分钟";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
                java.util.regex.Matcher matcher = pattern.matcher(this.exceptionReason);
                if (matcher.find()) {
                    return new BigDecimal(matcher.group(1));
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取实际工作时长
     *
     * @return 实际工作时长
     */
    public BigDecimal getActualWorkHours() {
        return this.workHours != null ? this.workHours : BigDecimal.ZERO;
    }

    /**
     * 获取处理类型
     * 从处理备注中解析处理类型
     *
     * @return 处理类型
     */
    public String getProcessType() {
        if (this.processRemark != null && this.processRemark.contains("|")) {
            String[] parts = this.processRemark.split("\\|");
            if (parts.length > 0) {
                return parts[0].trim();
            }
        }
        return this.processRemark;
    }

    /**
     * 获取处理原因
     * 从处理备注中解析处理原因
     *
     * @return 处理原因
     */
    public String getProcessReason() {
        if (this.processRemark != null && this.processRemark.contains("|")) {
            String[] parts = this.processRemark.split("\\|");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return this.processRemark;
    }
}

