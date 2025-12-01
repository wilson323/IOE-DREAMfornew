package net.lab1024.sa.admin.module.attendance.manager;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考勤设备VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-30
 */
@Data
public class AttendanceDeviceVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备状态
     */
    private String deviceStatus;

    /**
     * 设备状态名称
     */
    private String deviceStatusName;

    /**
     * 在线状态
     */
    private Integer onlineStatus;

    /**
     * 在线状态名称
     */
    private String onlineStatusName;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 连接地址
     */
    private String connectionAddress;

    /**
     * 今日打卡人数
     */
    private Integer todayPunchCount;

    /**
     * 本月打卡人数
     */
    private Integer monthlyPunchCount;

    /**
     * 设备使用率
     */
    private Double usageRate;

    /**
     * 设备健康状态
     */
    private String healthStatus;

    /**
     * 是否需要维护
     */
    private Boolean needMaintenance;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}