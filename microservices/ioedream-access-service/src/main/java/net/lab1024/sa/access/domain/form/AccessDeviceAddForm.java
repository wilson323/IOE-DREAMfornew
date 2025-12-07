package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 门禁设备添加表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * - 完整的参数验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessDeviceAddForm {

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100字符")
    private String deviceName;

    /**
     * 设备编号
     */
    @NotBlank(message = "设备编号不能为空")
    @Size(max = 50, message = "设备编号长度不能超过50字符")
    private String deviceCode;

    /**
     * 设备类型（固定为ACCESS）
     */
    @NotBlank(message = "设备类型不能为空")
    private String deviceType = "ACCESS";

    /**
     * 区域ID（外键）
     */
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    /**
     * IP地址
     */
    @NotBlank(message = "IP地址不能为空")
    @Size(max = 50, message = "IP地址长度不能超过50字符")
    private String ipAddress;

    /**
     * 端口号
     */
    @NotNull(message = "端口号不能为空")
    private Integer port;

    /**
     * 启用标志
     * <p>
     * 0-禁用
     * 1-启用
     * </p>
     */
    private Integer enabledFlag = 1;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}

