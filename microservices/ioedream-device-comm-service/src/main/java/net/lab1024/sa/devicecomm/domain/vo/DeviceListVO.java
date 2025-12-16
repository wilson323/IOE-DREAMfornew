package net.lab1024.sa.devicecomm.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 设备列表VO（管理端）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
public class DeviceListVO {

    private Long deviceId;

    private String deviceCode;

    private String deviceName;

    private String deviceType;

    private String location;

    /**
     * 1-在线；0-离线（前端页面约定）
     */
    private Integer status;

    private LocalDateTime lastOnlineTime;
}

