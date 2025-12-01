package net.lab1024.sa.admin.module.attendance.manager;

import lombok.Data;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;

import java.time.LocalDateTime;

/**
 * 考勤设备详情VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-30
 */
@Data
public class AttendanceDeviceDetailVO {

    /**
     * 基础设备信息
     */
    private SmartDeviceEntity device;

    /**
     * 考勤设备配置
     */
    private AttendanceDeviceConfig attendanceConfig;

    /**
     * 区域配置信息
     */
    private AttendanceAreaConfigEntity areaConfig;

    /**
     * 设备状态文本
     */
    private String deviceStatusText;

    /**
     * 最后打卡时间
     */
    private LocalDateTime lastPunchTime;

    /**
     * 今日打卡次数
     */
    private Integer todayPunchCount;

    /**
     * 设备在线时长（分钟）
     */
    private Long onlineDuration;

    /**
     * 设备使用率（%）
     */
    private Double usageRate;

    /**
     * 设备健康状态
     */
    private String healthStatus;

    /**
     * 故障描述
     */
    private String faultDescription;

    /**
     * 维护信息
     */
    private MaintenanceInfo maintenanceInfo;

    @Data
    public static class MaintenanceInfo {
        private LocalDateTime lastMaintenanceTime;
        private String lastMaintenanceContent;
        private LocalDateTime nextMaintenanceTime;
        private String maintenanceCycle;
    }
}