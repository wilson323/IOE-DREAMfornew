package net.lab1024.sa.attendance.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 上班打卡事件
 * <p>
 * 封装上班打卡事件信息
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
public class AttendanceClockInEvent {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 位置信息
     */
    private LocationInfo location;

    /**
     * 上班打卡时间
     */
    private LocalDateTime clockInTime;

    /**
     * 生物识别类型
     */
    private String biometricType;

    /**
     * 是否生物识别验证通过
     */
    private Boolean biometricVerified;

    /**
     * 是否位置验证通过
     */
    private Boolean locationVerified;

    /**
     * 设备类型
     */
    private String deviceType;
}
