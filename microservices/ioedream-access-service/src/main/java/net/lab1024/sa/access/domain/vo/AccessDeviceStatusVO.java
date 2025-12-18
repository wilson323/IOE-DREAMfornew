package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 门禁设备状态视图对象
 * <p>
 * 用于实时监控模块显示设备状态
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessDeviceStatusVO {

    /**
     * 设备ID
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
     * 区域ID
     */
    private Long areaId;

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
     * 4-维护
     * 5-停用
     * </p>
     */
    private Integer deviceStatus;

    /**
     * 设备状态名称
     */
    private String deviceStatusName;

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
     * 最后离线时间
     */
    private LocalDateTime lastOfflineTime;

    /**
     * 在线时长（小时）
     */
    private Long onlineDuration;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 网络质量
     * <p>
     * 可能的值：
     * - EXCELLENT - 优秀
     * - GOOD - 良好
     * - FAIR - 一般
     * - POOR - 差
     * </p>
     */
    private String networkQuality;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
