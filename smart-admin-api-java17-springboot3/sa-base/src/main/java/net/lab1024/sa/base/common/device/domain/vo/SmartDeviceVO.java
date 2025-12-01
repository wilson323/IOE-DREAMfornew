package net.lab1024.sa.base.common.device.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能设备视图对象
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Data
public class SmartDeviceVO {

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
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 设备类型名称
     */
    private String deviceTypeName;

    /**
     * 设备状态
     */
    private Integer deviceStatus;

    /**
     * 设备状态名称
     */
    private String deviceStatusName;

    /**
     * 设备位置
     */
    private String deviceLocation;

    /**
     * 设备描述
     */
    private String deviceDesc;

    /**
     * 设备配置
     */
    private String deviceConfig;

    /**
     * 安装时间
     */
    private LocalDateTime installTime;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 在线状态
     */
    private Boolean onlineStatus;

    /**
     * 电池电量
     */
    private Integer batteryLevel;

    /**
     * 信号强度
     */
    private Integer signalStrength;

    /**
     * 设备温度
     */
    private Double deviceTemperature;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 创建用户名称
     */
    private String createUserName;

    /**
     * 版本号
     */
    private Integer version;
}
