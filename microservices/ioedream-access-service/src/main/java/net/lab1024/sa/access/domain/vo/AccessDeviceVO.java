package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 门禁设备视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 用于Controller返回给前端的数据
 * - 不包含业务敏感信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessDeviceVO {

    /**
     * 设备ID（String类型，与DeviceEntity保持一致）
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备类型（固定为ACCESS）
     */
    private String deviceType;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * MAC地址
     */
    private String macAddress;

    /**
     * 设备序列号
     */
    private String serialNumber;

    /**
     * 品牌厂商
     */
    private String brand;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 设备状态
     * <p>
     * 1-在线
     * 2-离线
     * 3-故障
     * 4-维护
     * 5-停用
     * </p>
     */
    private Integer deviceStatus;

    /**
     * 启用标志
     * <p>
     * 0-禁用
     * 1-启用
     * </p>
     */
    private Integer enabled;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
