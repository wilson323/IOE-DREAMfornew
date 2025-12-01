package net.lab1024.sa.admin.module.system.device.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一设备视图对象
 * <p>
 * 企业级设备管理 - 设备展示对象
 * 严格遵循repowiki规范：完整的字段映射和格式化
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
public class UnifiedDeviceVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 设备厂商
     */
    private String manufacturer;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 设备版本
     */
    private String deviceVersion;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备端口
     */
    private Integer port;

    /**
     * 安装位置
     */
    private String installLocation;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 所属区域名称
     */
    private String areaName;

    /**
     * 设备状态
     */
    private Integer deviceStatus;

    /**
     * 设备状态名称
     */
    private String deviceStatusName;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 设备描述
     */
    private String deviceDescription;

    /**
     * 最后维护时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMaintenanceTime;

    /**
     * 维护周期（天）
     */
    private Integer maintenanceCycle;

    /**
     * 下次维护时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextMaintenanceTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 更新人ID
     */
    private Long updateUserId;
}