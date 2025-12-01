package net.lab1024.sa.admin.module.system.device.domain.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一设备查询表单
 * <p>
 * 企业级设备管理 - 设备查询表单
 * 严格遵循repowiki规范：完整的参数验证和业务规则
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
public class UnifiedDeviceQueryForm implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 设备状态
     */
    private Integer deviceStatus;

    /**
     * 设备厂商
     */
    private String manufacturer;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 是否在线
     */
    private Boolean isOnline;
}