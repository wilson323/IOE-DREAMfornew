package net.lab1024.sa.admin.module.system.device.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 统一设备更新表单
 * <p>
 * 企业级设备管理 - 设备更新表单
 * 严格遵循repowiki规范：完整的参数验证和业务规则
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnifiedDeviceUpdateForm extends UnifiedDeviceAddForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 设备版本
     */
    private String deviceVersion;

    /**
     * 最后维护时间
     */
    private String lastMaintenanceTime;

    /**
     * 维护周期（天）
     */
    private Integer maintenanceCycle;
}