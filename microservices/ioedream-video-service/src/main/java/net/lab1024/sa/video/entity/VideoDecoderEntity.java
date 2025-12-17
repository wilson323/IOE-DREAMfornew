package net.lab1024.sa.video.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 视频解码器实体类
 * <p>
 * 视频解码器管理实体，支持解码上墙和多路视频输出
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_decoder")
public class VideoDecoderEntity extends BaseEntity {

    /**
     * 解码器ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long decoderId;

    /**
     * 解码器编码
     */
    @TableField("decoder_code")
    private String decoderCode;

    /**
     * 解码器名称
     */
    @TableField("decoder_name")
    private String decoderName;

    /**
     * 设备类型：1-硬解码器 2-软解码器 3-混合解码器
     */
    @TableField("decoder_type")
    private Integer decoderType;

    /**
     * 品牌厂商
     */
    @TableField("brand")
    private String brand;

    /**
     * 设备型号
     */
    @TableField("model")
    private String model;

    /**
     * 序列号
     */
    @TableField("serial_number")
    private String serialNumber;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 端口号
     */
    @TableField("port")
    private Integer port;

    /**
     * MAC地址
     */
    @TableField("mac_address")
    private String macAddress;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码（加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 最大解码路数
     */
    @TableField("max_channels")
    private Integer maxChannels;

    /**
     * 当前使用路数
     */
    @TableField("used_channels")
    private Integer usedChannels;

    /**
     * 可用路数
     */
    @TableField("available_channels")
    private Integer availableChannels;

    /**
     * 最大分辨率：1-4CIF 2-D1 3-720P 4-1080P 5-4K
     */
    @TableField("max_resolution")
    private Integer maxResolution;

    /**
     * 支持解码格式（JSON数组：H264,H265,MJPEG等）
     */
    @TableField("supported_formats")
    private String supportedFormats;

    /**
     * 输出接口类型：1-HDMI 2-VGA 3-DVI 4-SDI 5-网络
     */
    @TableField("output_interface")
    private Integer outputInterface;

    /**
     * 输出数量
     */
    @TableField("output_count")
    private Integer outputCount;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 区域名称
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 安装位置
     */
    @TableField("location")
    private String location;

    /**
     * 经度
     */
    @TableField("longitude")
    private Double longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private Double latitude;

    /**
     * 设备状态：1-在线 2-离线 3-故障 4-维护 5-停用
     */
    @TableField("device_status")
    private Integer deviceStatus;

    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 心跳间隔（秒）
     */
    @TableField("heartbeat_interval")
    private Integer heartbeatInterval;

    /**
     * 最后心跳时间
     */
    @TableField("last_heartbeat_time")
    private LocalDateTime lastHeartbeatTime;

    /**
     * CPU使用率（百分比）
     */
    @TableField("cpu_usage")
    private Double cpuUsage;

    /**
     * 内存使用率（百分比）
     */
    @TableField("memory_usage")
    private Double memoryUsage;

    /**
     * 硬盘使用率（百分比）
     */
    @TableField("disk_usage")
    private Double diskUsage;

    /**
     * 温度（摄氏度）
     */
    @TableField("temperature")
    private Double temperature;

    /**
     * 运行时长（小时）
     */
    @TableField("running_hours")
    private Long runningHours;

    /**
     * 安装时间
     */
    @TableField("install_time")
    private LocalDateTime installTime;

    /**
     * 维护周期（天）
     */
    @TableField("maintenance_cycle")
    private Integer maintenanceCycle;

    /**
     * 上次维护时间
     */
    @TableField("last_maintenance_time")
    private LocalDateTime lastMaintenanceTime;

    /**
     * 下次维护时间
     */
    @TableField("next_maintenance_time")
    private LocalDateTime nextMaintenanceTime;

    /**
     * 保修期至
     */
    @TableField("warranty_expire_time")
    private LocalDateTime warrantyExpireTime;

    /**
     * 管理员
     */
    @TableField("administrator")
    private String administrator;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 解码器配置（JSON格式）
     */
    @TableField("decoder_config")
    private String decoderConfig;

    /**
     * 网络配置（JSON格式）
     */
    @TableField("network_config")
    private String networkConfig;

    /**
     * 输出配置（JSON格式）
     */
    @TableField("output_config")
    private String outputConfig;

    /**
     * 告警配置（JSON格式）
     */
    @TableField("alarm_config")
    private String alarmConfig;

    /**
     * 解码器分组
     */
    @TableField("decoder_group")
    private String decoderGroup;

    /**
     * 标签（JSON数组）
     */
    @TableField("tags")
    private String tags;

    /**
     * 设备描述
     */
    @TableField("description")
    private String description;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
