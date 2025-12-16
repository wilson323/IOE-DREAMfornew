package net.lab1024.sa.consume.domain.vo;

import lombok.Data;

/**
 * 消费设备配置VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeDeviceConfigVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 设备配置参数（JSON格式）
     */
    private String configParams;
}



