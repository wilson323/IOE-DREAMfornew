package net.lab1024.sa.consume.domain.vo;

import lombok.Data;

/**
 * 消费设备VO
 * <p>
 * 用于返回消费设备信息
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含完整的业务字段
 * - 符合企业级VO设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeDeviceVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 设备状态
     * <p>
     * 1-在线
     * 2-离线
     * 3-故障
     * </p>
     */
    private Integer deviceStatus;

    /**
     * 支持的消费模式（JSON数组）
     * <p>
     * 示例：["FIXED", "AMOUNT", "PRODUCT"]
     * </p>
     */
    private String supportedModes;
}



