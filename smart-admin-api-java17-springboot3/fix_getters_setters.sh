#!/bin/bash

# 修复 AttendanceRuleEntity
echo "修复 AttendanceRuleEntity..."
cat > sa-base/src/main/java/net/lab1024/sa/base/common/device/domain/entity/AttendanceRuleEntity.java << 'ENTITY_EOF'
package net.lab1024.sa.base.common.device.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 考勤规则实体类
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-09-21 21:03:09
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href="https://1024lab.net">1024创新实验室</a>
 */
@Entity
@Table(name = "t_attendance_rule")
@TableName("t_attendance_rule")
@Schema(description = "考勤规则实体")
public class AttendanceRuleEntity extends BaseEntity {

    @Schema(description = "规则编号")
    @Column(name = "rule_code")
    @TableField("rule_code")
    private String ruleCode;

    @Schema(description = "规则名称")
    @Column(name = "rule_name")
    @TableField("rule_name")
    private String ruleName;

    @Schema(description = "规则类型")
    @Column(name = "rule_type")
    @TableField("rule_type")
    private Integer ruleType;

    @Schema(description = "规则内容")
    @Column(name = "rule_content", columnDefinition = "TEXT")
    @TableField("rule_content")
    private String ruleContent;

    @Schema(description = "是否启用")
    @Column(name = "enabled")
    @TableField("enabled")
    private Boolean enabled;

    @Schema(description = "生效时间")
    @Column(name = "effective_time")
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间")
    @Column(name = "expiry_time")
    @TableField("expiry_time")
    private LocalDateTime expiryTime;

    // Getter and Setter methods
    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Integer getRuleType() {
        return ruleType;
    }

    public void setRuleType(Integer ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleContent() {
        return ruleContent;
    }

    public void setRuleContent(String ruleContent) {
        this.ruleContent = ruleContent;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(LocalDateTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
}
ENTITY_EOF

echo "✅ AttendanceRuleEntity 修复完成"

# 修复 AttendanceRecordEntity
echo "修复 AttendanceRecordEntity..."
cat > sa-base/src/main/java/net/lab1024/sa/base/common/device/domain/entity/AttendanceRecordEntity.java << 'RECORD_EOF'
package net.lab1024.sa.base.common.device.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.base.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 考勤记录实体类
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-09-21 21:03:09
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href="https://1024lab.net">1024创新实验室</a>
 */
@Entity
@Table(name = "t_attendance_record")
@TableName("t_attendance_record")
@Schema(description = "考勤记录实体")
public class AttendanceRecordEntity extends BaseEntity {

    @Schema(description = "用户ID")
    @Column(name = "user_id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "用户姓名")
    @Column(name = "user_name")
    @TableField("user_name")
    private String userName;

    @Schema(description = "考勤日期")
    @Column(name = "attendance_date")
    @TableField("attendance_date")
    private LocalDateTime attendanceDate;

    @Schema(description = "上班时间")
    @Column(name = "clock_in_time")
    @TableField("clock_in_time")
    private LocalDateTime clockInTime;

    @Schema(description = "下班时间")
    @Column(name = "clock_out_time")
    @TableField("clock_out_time")
    private LocalDateTime clockOutTime;

    @Schema(description = "工作时长")
    @Column(name = "work_hours")
    @TableField("work_hours")
    private Double workHours;

    @Schema(description = "迟到时长")
    @Column(name = "late_duration")
    @TableField("late_duration")
    private Integer lateDuration;

    @Schema(description = "早退时长")
    @Column(name = "early_duration")
    @TableField("early_duration")
    private Integer earlyDuration;

    @Schema(description = "缺勤原因")
    @Column(name = "absence_reason")
    @TableField("absence_reason")
    private String absenceReason;

    @Schema(description = "加班时长")
    @Column(name = "overtime_hours")
    @TableField("overtime_hours")
    private Double overtimeHours;

    @Schema(description = "GPS有效性")
    @Column(name = "gps_valid")
    @TableField("gps_valid")
    private Boolean gpsValid;

    @Schema(description = "照片URL")
    @Column(name = "photo_url")
    @TableField("photo_url")
    private String photoUrl;

    @Schema(description = "是否已处理")
    @Column(name = "is_processed")
    @TableField("is_processed")
    private Boolean isProcessed;

    @Schema(description = "处理原因")
    @Column(name = "process_reason")
    @TableField("process_reason")
    private String processReason;

    @Schema(description = "实际工作时长")
    @Column(name = "actual_work_hours")
    @TableField("actual_work_hours")
    private Double actualWorkHours;

    @Schema(description = "异常原因")
    @Column(name = "exception_reason")
    @TableField("exception_reason")
    private String exceptionReason;

    // Getter and Setter methods
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDateTime attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public LocalDateTime getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }

    public LocalDateTime getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(LocalDateTime clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    public Double getWorkHours() {
        return workHours;
    }

    public void setWorkHours(Double workHours) {
        this.workHours = workHours;
    }

    public Integer getLateDuration() {
        return lateDuration;
    }

    public void setLateDuration(Integer lateDuration) {
        this.lateDuration = lateDuration;
    }

    public Integer getEarlyDuration() {
        return earlyDuration;
    }

    public void setEarlyDuration(Integer earlyDuration) {
        this.earlyDuration = earlyDuration;
    }

    public String getAbsenceReason() {
        return absenceReason;
    }

    public void setAbsenceReason(String absenceReason) {
        this.absenceReason = absenceReason;
    }

    public Double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(Double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public Boolean getGpsValid() {
        return gpsValid;
    }

    public void setGpsValid(Boolean gpsValid) {
        this.gpsValid = gpsValid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public String getProcessReason() {
        return processReason;
    }

    public void setProcessReason(String processReason) {
        this.processReason = processReason;
    }

    public Double getActualWorkHours() {
        return actualWorkHours;
    }

    public void setActualWorkHours(Double actualWorkHours) {
        this.actualWorkHours = actualWorkHours;
    }

    public String getExceptionReason() {
        return exceptionReason;
    }

    public void setExceptionReason(String exceptionReason) {
        this.exceptionReason = exceptionReason;
    }
}
RECORD_EOF

echo "✅ AttendanceRecordEntity 修复完成"

# 修复 AttendanceStatisticsEntity
echo "修复 AttendanceStatisticsEntity..."
cat > sa-base/src/main/java/net/lab1024/sa/base/common/device/domain/entity/AttendanceStatisticsEntity.java << 'STATS_EOF'
package net.lab1024.sa.base.common.device.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.base.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 考勤统计实体类
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-09-21 21:03:09
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href="https://1024lab.net">1024创新实验室</a>
 */
@Entity
@Table(name = "t_attendance_statistics")
@TableName("t_attendance_statistics")
@Schema(description = "考勤统计实体")
public class AttendanceStatisticsEntity extends BaseEntity {

    @Schema(description = "用户ID")
    @Column(name = "user_id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "用户姓名")
    @Column(name = "user_name")
    @TableField("user_name")
    private String userName;

    @Schema(description = "统计月份")
    @Column(name = "statistics_month")
    @TableField("statistics_month")
    private String statisticsMonth;

    @Schema(description = "应出勤天数")
    @Column(name = "should_work_days")
    @TableField("should_work_days")
    private Integer shouldWorkDays;

    @Schema(description = "实际出勤天数")
    @Column(name = "actual_work_days")
    @TableField("actual_work_days")
    private Integer actualWorkDays;

    @Schema(description = "出勤天数")
    @Column(name = "present_days")
    @TableField("present_days")
    private Integer presentDays;

    @Schema(description = "迟到次数")
    @Column(name = "late_count")
    @TableField("late_count")
    private Integer lateCount;

    @Schema(description = "早退次数")
    @Column(name = "early_count")
    @TableField("early_count")
    private Integer earlyCount;

    @Schema(description = "缺勤次数")
    @Column(name = "absence_count")
    @TableField("absence_count")
    private Integer absenceCount;

    @Schema(description = "异常次数")
    @Column(name = "exception_count")
    @TableField("exception_count")
    private Integer exceptionCount;

    @Schema(description = "加班总时长")
    @Column(name = "total_overtime_hours")
    @TableField("total_overtime_hours")
    private Double totalOvertimeHours;

    // Getter and Setter methods
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatisticsMonth() {
        return statisticsMonth;
    }

    public void setStatisticsMonth(String statisticsMonth) {
        this.statisticsMonth = statisticsMonth;
    }

    public Integer getShouldWorkDays() {
        return shouldWorkDays;
    }

    public void setShouldWorkDays(Integer shouldWorkDays) {
        this.shouldWorkDays = shouldWorkDays;
    }

    public Integer getActualWorkDays() {
        return actualWorkDays;
    }

    public void setActualWorkDays(Integer actualWorkDays) {
        this.actualWorkDays = actualWorkDays;
    }

    public Integer getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(Integer presentDays) {
        this.presentDays = presentDays;
    }

    public Integer getLateCount() {
        return lateCount;
    }

    public void setLateCount(Integer lateCount) {
        this.lateCount = lateCount;
    }

    public Integer getEarlyCount() {
        return earlyCount;
    }

    public void setEarlyCount(Integer earlyCount) {
        this.earlyCount = earlyCount;
    }

    public Integer getAbsenceCount() {
        return absenceCount;
    }

    public void setAbsenceCount(Integer absenceCount) {
        this.absenceCount = absenceCount;
    }

    public Integer getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(Integer exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    public Double getTotalOvertimeHours() {
        return totalOvertimeHours;
    }

    public void setTotalOvertimeHours(Double totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }
}
STATS_EOF

echo "✅ AttendanceStatisticsEntity 修复完成"

echo "🎉 所有实体类修复完成！"

