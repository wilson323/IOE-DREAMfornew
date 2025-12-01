package net.lab1024.sa.base.common.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 视频设备实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class VideoDeviceEntity extends BaseEntity {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备IP地址
     */
    private String deviceIp;

    /**
     * 设备端口
     */
    private Integer devicePort;

    /**
     * 流媒体端口
     */
    private Integer streamPort;

    /**
     * 设备类型 (CAMERA-摄像头, NVR-录像机)
     */
    private String deviceType;

    /**
     * 设备状态 (ONLINE-在线, OFFLINE-离线, FAULT-故障)
     */
    private String deviceStatus;

    /**
     * 设备位置
     */
    private String deviceLocation;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * RTSP流地址
     */
    private String rtspUrl;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(加密存储)
     */
    private String password;

    /**
     * 是否启用云台控制 (0-否, 1-是)
     */
    private Integer ptzEnabled;

    /**
     * 录像开关 (0-关闭, 1-开启)
     */
    private Integer recordEnabled;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 帧率
     */
    private Integer frameRate;

    /**
     * 制式 (PAL, NTSC)
     */
    private String videoFormat;

    /**
     * 设备描述
     */
    private String deviceDescription;

    /**
     * 安装日期
     */
    private LocalDateTime installDate;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 制造商
     */
    private String manufacturer;

    /**
     * 型号
     */
    private String modelNumber;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 备注
     */
    private String remark;
}