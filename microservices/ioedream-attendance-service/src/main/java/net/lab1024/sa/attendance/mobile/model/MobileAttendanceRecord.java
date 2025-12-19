package net.lab1024.sa.attendance.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 移动端考勤记录
 * <p>
 * 封装移动端考勤记录信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileAttendanceRecord {

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 考勤日期
     */
    private LocalDate attendanceDate;

    /**
     * 上班打卡时间
     */
    private LocalDateTime clockInTime;

    /**
     * 下班打卡时间
     */
    private LocalDateTime clockOutTime;

    /**
     * 工作时长（小时）
     */
    private Double workHours;

    /**
     * 考勤状态
     */
    private String attendanceStatus;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 位置
     */
    private String location;
}
