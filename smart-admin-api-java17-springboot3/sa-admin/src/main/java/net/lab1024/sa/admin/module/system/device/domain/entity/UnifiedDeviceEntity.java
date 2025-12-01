package net.lab1024.sa.admin.module.system.device.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 统一设备实体类
 * <p>
 * 严格遵循repowiki业务架构规范：
 * - 整合SmartDeviceEntity、AccessDeviceEntity、VideoDeviceEntity的重复字段
 * - 支持多种设备类型：门禁、视频、消费、考勤等
 * - 统一的数据模型，便于维护和扩展
 * - 使用设备类型字段区分不同的设备业务逻辑
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_unified_device")
@Schema(description = "统一设备信息")
public class UnifiedDeviceEntity extends BaseEntity {

    /**
     * 设备ID
     */
    @TableId
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 设备编号（设备唯一标识）
     */
    @TableField("device_code")
    @Schema(description = "设备编号")
    private String deviceCode;

    /**
     * 设备名称
     */
    @TableField("device_name")
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 设备类型
     * ACCESS-门禁设备, VIDEO-视频设备, CONSUME-消费设备, ATTENDANCE-考勤设备, SMART-智能设备
     */
    @TableField("device_type")
    @Schema(description = "设备类型")
    private String deviceType;

    /**
     * 设备型号
     */
    @TableField("device_model")
    @Schema(description = "设备型号")
    private String deviceModel;

    /**
     * 设备厂商
     */
    @TableField("manufacturer")
    @Schema(description = "设备厂商")
    private String manufacturer;

    /**
     * 设备序列号
     */
    @TableField("serial_number")
    @Schema(description = "设备序列号")
    private String serialNumber;

    /**
     * 设备IP地址
     */
    @TableField("ip_address")
    @Schema(description = "设备IP地址")
    private String ipAddress;

    /**
     * 设备端口号
     */
    @TableField("port")
    @Schema(description = "设备端口号")
    private Integer port;

    /**
     * 设备MAC地址
     */
    @TableField("mac_address")
    @Schema(description = "设备MAC地址")
    private String macAddress;

    /**
     * 所属区域ID（主要用于门禁设备）
     */
    @TableField("area_id")
    @Schema(description = "所属区域ID")
    private Long areaId;

    /**
     * 所属区域名称
     */
    @TableField("area_name")
    @Schema(description = "所属区域名称")
    private String areaName;

    /**
     * 设备位置描述
     */
    @TableField("location")
    @Schema(description = "设备位置描述")
    private String location;

    /**
     * 在线状态：0-离线，1-在线
     */
    @TableField("online_status")
    @Schema(description = "在线状态")
    private Integer onlineStatus;

    /**
     * 启用状态：0-禁用，1-启用
     */
    @TableField("enabled")
    @Schema(description = "启用状态")
    private Integer enabled;

    /**
     * 工作模式（主要用于门禁设备）
     * 0-普通模式，1-刷卡模式，2-人脸模式，3-指纹模式，4-混合模式
     */
    @TableField("work_mode")
    @Schema(description = "工作模式")
    private Integer workMode;

    /**
     * 设备状态：NORMAL-正常，FAULT-故障，MAINTENANCE-维护中
     */
    @TableField("device_status")
    @Schema(description = "设备状态")
    private String deviceStatus;

    /**
     * 最后心跳时间
     */
    @TableField("last_heartbeat_time")
    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 心跳间隔（秒）
     */
    @TableField("heartbeat_interval")
    @Schema(description = "心跳间隔")
    private Integer heartbeatInterval;

    // ========== 视频设备专用字段 ==========

    /**
     * 视频流地址（视频设备专用）
     */
    @TableField("stream_url")
    @Schema(description = "视频流地址")
    private String streamUrl;

    /**
     * 视频格式（视频设备专用）
     */
    @TableField("video_format")
    @Schema(description = "视频格式")
    private String videoFormat;

    /**
     * 分辨率（视频设备专用）
     */
    @TableField("resolution")
    @Schema(description = "分辨率")
    private String resolution;

    /**
     * 帧率（视频设备专用）
     */
    @TableField("frame_rate")
    @Schema(description = "帧率")
    private Integer frameRate;

    /**
     * 码率(kbps)（视频设备专用）
     */
    @TableField("bitrate")
    @Schema(description = "码率(kbps)")
    private Integer bitrate;

    /**
     * 是否支持云台控制（视频设备专用）
     */
    @TableField("support_ptz")
    @Schema(description = "是否支持云台控制")
    private Boolean supportPtz;

    /**
     * 是否支持录像（视频设备专用）
     */
    @TableField("support_recording")
    @Schema(description = "是否支持录像")
    private Boolean supportRecording;

    // ========== 门禁设备专用字段 ==========

    /**
     * 是否支持远程开门（门禁设备专用）
     */
    @TableField("support_remote_open")
    @Schema(description = "是否支持远程开门")
    private Boolean supportRemoteOpen;

    /**
     * 是否支持人脸识别（门禁设备专用）
     */
    @TableField("support_face_recognition")
    @Schema(description = "是否支持人脸识别")
    private Boolean supportFaceRecognition;

    /**
     * 是否支持指纹识别（门禁设备专用）
     */
    @TableField("support_fingerprint")
    @Schema(description = "是否支持指纹识别")
    private Boolean supportFingerprint;

    /**
     * 是否支持刷卡（门禁设备专用）
     */
    @TableField("support_card")
    @Schema(description = "是否支持刷卡")
    private Boolean supportCard;

    // ========== 设备配置信息（JSON格式存储） ==========

    /**
     * 设备配置信息（JSON格式）
     */
    @TableField("device_config")
    @Schema(description = "设备配置信息")
    private String deviceConfig;

    /**
     * 设备扩展属性（JSON格式）
     */
    @TableField("extend_properties")
    @Schema(description = "设备扩展属性")
    private String extendProperties;

    // ========== 维护信息 ==========

    /**
     * 安装时间
     */
    @TableField("install_time")
    @Schema(description = "安装时间")
    private LocalDateTime installTime;

    /**
     * 最后维护时间
     */
    @TableField("last_maintenance_time")
    @Schema(description = "最后维护时间")
    private LocalDateTime lastMaintenanceTime;

    /**
     * 维护周期（天）
     */
    @TableField("maintenance_cycle")
    @Schema(description = "维护周期")
    private Integer maintenanceCycle;

    /**
     * 设备备注
     */
    @TableField("remark")
    @Schema(description = "设备备注")
    private String remark;

    // ========== 虚拟字段（不映射到数据库） ==========

    /**
     * 在线状态描述（虚拟字段，根据onlineStatus计算）
     */
    @TableField(exist = false)
    @Schema(description = "在线状态描述")
    private String onlineStatusDesc;

    /**
     * 启用状态描述（虚拟字段，根据enabled计算）
     */
    @TableField(exist = false)
    @Schema(description = "启用状态描述")
    private String enabledDesc;

    /**
     * 设备类型描述（虚拟字段，根据deviceType计算）
     */
    @TableField(exist = false)
    @Schema(description = "设备类型描述")
    private String deviceTypeDesc;

    /**
     * 设备状态描述（虚拟字段，根据deviceStatus计算）
     */
    @TableField(exist = false)
    @Schema(description = "设备状态描述")
    private String deviceStatusDesc;

    /**
     * 工作模式描述（虚拟字段，根据workMode计算）
     */
    @TableField(exist = false)
    @Schema(description = "工作模式描述")
    private String workModeDesc;

    /**
     * 是否需要维护（虚拟字段，根据维护周期和最后维护时间计算）
     */
    @TableField(exist = false)
    @Schema(description = "是否需要维护")
    private Boolean needMaintenance;

    /**
     * 是否心跳超时（虚拟字段，根据心跳间隔和最后心跳时间计算）
     */
    @TableField(exist = false)
    @Schema(description = "是否心跳超时")
    private Boolean heartbeatTimeout;
}