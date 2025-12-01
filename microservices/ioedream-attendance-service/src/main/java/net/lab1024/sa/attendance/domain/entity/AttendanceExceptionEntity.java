package net.lab1024.sa.attendance.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 考勤异常实体
 *
 * 严格遵循repowiki规范:
 * - 继承BaseEntity，包含审计字段
 * - 使用jakarta包，避免javax包
 * - 使用Lombok简化代码
 * - 字段命名规范：下划线分隔
 * - 完整的异常处理逻辑
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_attendance_exception")
public class AttendanceExceptionEntity extends BaseEntity {

    /**
     * 异常ID
     */
    @TableId(value = "exception_id", type = IdType.AUTO)
    private Long exceptionId;

    /**
     * 关联考勤记录ID
     */
    @TableField("record_id")
    private Long recordId;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 异常日期
     */
    @TableField("exception_date")
    private LocalDate exceptionDate;

    /**
     * 异常类型
     * LATE-迟到, EARLY_LEAVE-早退, ABSENTEEISM-旷工, FORGET_PUNCH-忘打卡,
     * OUTSIDE_LOCATION-位置异常
     */
    @TableField("exception_type")
    private String exceptionType;

    /**
     * 异常级别
     * LOW-低, NORMAL-一般, HIGH-高, CRITICAL-严重
     */
    @TableField("exception_level")
    private String exceptionLevel;

    /**
     * 异常描述
     */
    @TableField("exception_description")
    private String exceptionDescription;

    /**
     * 原始打卡时间
     */
    @TableField("original_time")
    private LocalDateTime originalTime;

    /**
     * 预期打卡时间
     */
    @TableField("expected_time")
    private LocalDateTime expectedTime;

    /**
     * 迟到分钟数
     */
    @TableField("late_minutes")
    private Integer lateMinutes;

    /**
     * 早退分钟数
     */
    @TableField("early_minutes")
    private Integer earlyMinutes;

    /**
     * 异常原因
     */
    @TableField("exception_reason")
    private String exceptionReason;

    /**
     * 员工备注
     */
    @TableField("employee_remark")
    private String employeeRemark;

    /**
     * 位置信息(JSON格式)
     * 格式: {latitude: 39.9042, longitude: 116.4074, address: "详细地址"}
     */
    @TableField("location_info")
    private String locationInfo;

    /**
     * 设备信息(JSON格式)
     * 格式: {deviceId: "xxx", deviceType: "mobile", appVersion: "1.0.0"}
     */
    @TableField("device_info")
    private String deviceInfo;

    /**
     * 证明文件(JSON格式)
     * 格式: [{type: "image", url: "/files/evidence1.jpg"}]
     */
    @TableField("evidence_files")
    private String evidenceFiles;

    /**
     * 是否自动检测
     * 0-手动, 1-自动
     */
    @TableField("auto_detected")
    private Integer autoDetected;

    /**
     * 检测规则
     */
    @TableField("detection_rule")
    private String detectionRule;

    /**
     * 处理状态
     * PENDING-待处理, APPROVED-已批准, REJECTED-已拒绝, PROCESSING-处理中
     */
    @TableField("status")
    private String status;

    /**
     * 处理方式
     * AUTO_APPROVAL-自动批准, MANUAL-人工处理, SYSTEM-系统处理
     */
    @TableField("process_type")
    private String processType;

    /**
     * 申请人ID
     */
    @TableField("applied_by")
    private Long appliedBy;

    /**
     * 申请人姓名
     */
    @TableField("applied_by_name")
    private String appliedByName;

    /**
     * 申请时间
     */
    @TableField("applied_time")
    private LocalDateTime appliedTime;

    /**
     * 处理人ID
     */
    @TableField("processed_by")
    private Long processedBy;

    /**
     * 处理人姓名
     */
    @TableField("processed_by_name")
    private String processedByName;

    /**
     * 处理时间
     */
    @TableField("processed_time")
    private LocalDateTime processedTime;

    /**
     * 处理结果
     */
    @TableField("process_result")
    private String processResult;

    /**
     * 处理备注
     */
    @TableField("process_remark")
    private String processRemark;

    /**
     * 是否有效异常
     * 0-无效, 1-有效
     */
    @TableField("is_valid")
    private Integer isValid;

    /**
     * 是否需要跟进
     * 0-否, 1-是
     */
    @TableField("need_follow_up")
    private Integer needFollowUp;

    /**
     * 跟进时间
     */
    @TableField("follow_up_time")
    private LocalDateTime followUpTime;

    /**
     * 是否已发送通知
     * 0-否, 1-是
     */
    @TableField("notification_sent")
    private Integer notificationSent;

    /**
     * 检查是否为待处理状态
     *
     * @return 是否待处理
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * 检查是否已处理
     *
     * @return 是否已处理
     */
    public boolean isProcessed() {
        return "APPROVED".equals(status) || "REJECTED".equals(status);
    }

    /**
     * 检查是否已批准
     *
     * @return 是否已批准
     */
    public boolean isApproved() {
        return "APPROVED".equals(status);
    }

    /**
     * 检查是否已拒绝
     *
     * @return 是否已拒绝
     */
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }

    /**
     * 检查是否为自动检测
     *
     * @return 是否自动检测
     */
    public boolean isAutoDetected() {
        return autoDetected != null && autoDetected == 1;
    }

    /**
     * 检查是否为有效异常
     *
     * @return 是否有效
     */
    public boolean isValidException() {
        return isValid != null && isValid == 1;
    }

    /**
     * 检查是否需要跟进
     *
     * @return 是否需要跟进
     */
    public boolean needFollowUp() {
        return needFollowUp != null && needFollowUp == 1;
    }

    /**
     * 检查是否已发送通知
     *
     * @return 是否已发送通知
     */
    public boolean isNotificationSent() {
        return notificationSent != null && notificationSent == 1;
    }

    /**
     * 获取异常类型描述
     *
     * @return 异常类型描述
     */
    public String getExceptionTypeDescription() {
        if (exceptionType == null) {
            return "未知异常";
        }

        switch (exceptionType) {
            case "LATE":
                return "迟到";
            case "EARLY_LEAVE":
                return "早退";
            case "ABSENTEEISM":
                return "旷工";
            case "FORGET_PUNCH":
                return "忘打卡";
            case "OUTSIDE_LOCATION":
                return "位置异常";
            default:
                return exceptionType;
        }
    }

    /**
     * 获取异常级别描述
     *
     * @return 异常级别描述
     */
    public String getExceptionLevelDescription() {
        if (exceptionLevel == null) {
            return "未知级别";
        }

        switch (exceptionLevel) {
            case "LOW":
                return "低";
            case "NORMAL":
                return "一般";
            case "HIGH":
                return "高";
            case "CRITICAL":
                return "严重";
            default:
                return exceptionLevel;
        }
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getStatusDescription() {
        if (status == null) {
            return "未知状态";
        }

        switch (status) {
            case "PENDING":
                return "待处理";
            case "PROCESSING":
                return "处理中";
            case "APPROVED":
                return "已批准";
            case "REJECTED":
                return "已拒绝";
            default:
                return status;
        }
    }

    /**
     * 检查异常是否需要人工处理
     *
     * @return 是否需要人工处理
     */
    public boolean needsManualProcess() {
        return "HIGH".equals(exceptionLevel) || "CRITICAL".equals(exceptionLevel) ||
                "LATE".equals(exceptionType) && lateMinutes != null && lateMinutes > 30;
    }

    /**
     * 检查异常是否可自动批准
     *
     * @return 是否可自动批准
     */
    public boolean canAutoApprove() {
        return "LOW".equals(exceptionLevel) && "FORGET_PUNCH".equals(exceptionType);
    }

    /**
     * 获取异常严重程度分数
     *
     * @return 严重程度分数 (1-10)
     */
    public int getSeverityScore() {
        int score = 1; // 基础分数

        // 根据异常类型增加分数
        switch (exceptionType) {
            case "FORGET_PUNCH":
                score += 1;
                break;
            case "LATE":
                score += lateMinutes != null ? Math.min(lateMinutes / 10, 5) : 3;
                break;
            case "EARLY_LEAVE":
                score += earlyMinutes != null ? Math.min(earlyMinutes / 10, 5) : 3;
                break;
            case "ABSENTEEISM":
                score += 8;
                break;
            case "OUTSIDE_LOCATION":
                score += 4;
                break;
        }

        // 根据异常级别调整分数
        switch (exceptionLevel) {
            case "LOW":
                // 保持原分数
                break;
            case "NORMAL":
                score += 2;
                break;
            case "HIGH":
                score += 4;
                break;
            case "CRITICAL":
                score += 6;
                break;
        }

        return Math.min(score, 10);
    }
}
