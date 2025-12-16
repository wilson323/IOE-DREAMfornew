package net.lab1024.sa.video.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

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
@Accessors(chain = true)
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
     * 设备子类型
     * <p>
     * 1 - 枪机
     * 2 - 球机
     * 3 - 半球机
     * 4 - 一体机
     * </p>
     */
    private Integer deviceSubType;

    /**
     * 设备子类型描述
     */
    private String deviceSubTypeDesc;

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
     * 设备协议
     * <p>
     * 1 - RTSP
     * 2 - RTMP
     * 3 - HTTP
     * 4 - TCP
     * 5 - UDP
     * </p>
     */
    private Integer protocol;

    /**
     * 设备协议描述
     */
    private String protocolDesc;

    /**
     * 视频流地址
     */
    private String streamUrl;

    /**
     * 设备厂商
     */
    private String manufacturer;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 设备序列号
     */
    private String serialNumber;

    /**
     * 安装位置
     */
    private String installLocation;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 海拔高度（米）
     */
    private Double altitude;

    /**
     * 是否支持PTZ控制
     * <p>
     * 0 - 不支持
     * 1 - 支持
     * </p>
     */
    private Integer ptzSupported;

    /**
     * 是否支持音频
     * <p>
     * 0 - 不支持
     * 1 - 支持
     * </p>
     */
    private Integer audioSupported;

    /**
     * 是否支持夜视
     * <p>
     * 0 - 不支持
     * 1 - 支持
     * </p>
     */
    private Integer nightVisionSupported;

    /**
     * 是否支持AI分析
     * <p>
     * 0 - 不支持
     * 1 - 支持
     * </p>
     */
    private Integer aiSupported;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 帧率
     */
    private Integer frameRate;

    /**
     * 启用标志
     * <p>
     * 0 - 禁用
     * 1 - 启用
     * </p>
     */
    private Integer enabledFlag;

    /**
     * 最后上线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 最后离线时间
     */
    private LocalDateTime lastOfflineTime;

    /**
     * 在线时长（分钟）
     */
    private Long onlineDuration;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;
}

