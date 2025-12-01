package net.lab1024.sa.access.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 生物识别设备实体类
 * <p>
 * 用于存储生物识别设备的信息和状态
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class BiometricDeviceEntity {

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
     * 设备类型
     */
    private String deviceType;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 设备状态 0-离线 1-在线 2-故障
     */
    private Integer deviceStatus;

    /**
     * 设备IP地址
     */
    private String deviceIp;

    /**
     * 设备端口
     */
    private Integer devicePort;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 备注信息
     */
    private String remark;
}
