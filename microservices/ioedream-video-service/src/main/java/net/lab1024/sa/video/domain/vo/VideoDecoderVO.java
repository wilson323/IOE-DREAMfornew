package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 解码器视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含完整的业务字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Accessors(chain = true)
public class VideoDecoderVO {

    /**
     * 解码器ID
     */
    private Long decoderId;

    /**
     * 解码器编码
     */
    private String decoderCode;

    /**
     * 解码器名称
     */
    private String decoderName;

    /**
     * 设备类型：1-硬解码器 2-软解码器 3-混合解码器
     */
    private Integer decoderType;

    /**
     * 设备类型描述
     */
    private String decoderTypeDesc;

    /**
     * 品牌厂商
     */
    private String brand;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 序列号
     */
    private String serialNumber;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 最大通道数
     */
    private Integer maxChannels;

    /**
     * 已使用通道数
     */
    private Integer usedChannels;

    /**
     * 可用通道数
     */
    private Integer availableChannels;

    /**
     * 最大分辨率
     */
    private String maxResolution;

    /**
     * 支持的格式
     */
    private String supportedFormats;

    /**
     * 设备状态：1-在线，2-离线，3-故障
     */
    private Integer deviceStatus;

    /**
     * 设备状态描述
     */
    private String deviceStatusDesc;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
