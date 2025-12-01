package net.lab1024.sa.admin.module.system.device.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一设备添加表单
 * <p>
 * 企业级设备管理 - 设备新增表单
 * 严格遵循repowiki规范：完整的参数验证和业务规则
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
public class UnifiedDeviceAddForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备编码
     */
    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    /**
     * 设备类型
     */
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    /**
     * 设备厂商
     */
    private String manufacturer;

    /**
     * 设备型号
     */
    private String model;

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
     * 设备状态
     */
    private Integer deviceStatus;

    /**
     * 设备描述
     */
    private String deviceDescription;
}