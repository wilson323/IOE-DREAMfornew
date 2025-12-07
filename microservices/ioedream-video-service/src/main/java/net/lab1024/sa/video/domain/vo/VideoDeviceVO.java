package net.lab1024.sa.video.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 视频设备视图对象
 * <p>
 * 用于返回视频设备信息
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
public class VideoDeviceVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

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
     * 区域名称
     */
    private String areaName;

    /**
     * 设备IP
     */
    private String deviceIp;

    /**
     * 设备端口
     */
    private Integer devicePort;

    /**
     * 设备状态
     * <p>
     * 1 - 在线
     * 2 - 离线
     * 3 - 故障
     * </p>
     */
    private Integer deviceStatus;

    /**
     * 设备状态描述
     */
    private String deviceStatusDesc;

    /**
     * 启用标志
     * <p>
     * 0 - 禁用
     * 1 - 启用
     * </p>
     */
    private Integer enabledFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

