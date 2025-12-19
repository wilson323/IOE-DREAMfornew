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
 * 视频设备实体类
 * <p>
 * 视频监控设备管理实体，支持多种视频设备和智能分析功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_device")
public class VideoDeviceEntity extends BaseEntity {

    /**
     * 设备ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备类型：1-摄像头 2-NVR 3-DVR 4-视频服务器 5-智能分析设备
     */
    @TableField("device_type")
    private Integer deviceType;

    /**
     * 设备子类型：1-枪机 2-球机 3-半球机 4-全景相机 5-热成像相机
     */
    @TableField("device_sub_type")
    private Integer deviceSubType;

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
     * RTSP地址
     */
    @TableField("rtsp_url")
    private String rtspUrl;

    /**
     * HTTP地址
     */
    @TableField("http_url")
    private String httpUrl;

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
     * 分辨率
     */
    @TableField("resolution")
    private String resolution;

    /**
     * 帧率
     */
    @TableField("frame_rate")
    private Integer frameRate;

    /**
     * 码率（Kbps）
     */
    @TableField("bitrate")
    private Integer bitrate;

    /**
     * 是否支持音频：0-不支持 1-支持
     */
    @TableField("audio_enabled")
    private Integer audioEnabled;

    /**
     * 是否支持夜视：0-不支持 1-支持
     */
    @TableField("night_vision")
    private Integer nightVision;

    /**
     * 是否支持智能分析：0-不支持 1-支持
     */
    @TableField("ai_enabled")
    private Integer aiEnabled;

    /**
     * 智能分析类型（JSON数组）：1-人脸识别 2-行为分析 3-车牌识别 4-物体检测
     */
    @TableField("ai_types")
    private String aiTypes;

    /**
     * 设备状态：1-在线 2-离线 3-故障 4-维护
     */
    @TableField("device_status")
    private Integer deviceStatus;

    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 录像存储天数
     */
    @TableField("storage_days")
    private Integer storageDays;

    /**
     * 是否启用移动侦测：0-禁用 1-启用
     */
    @TableField("motion_detection")
    private Integer motionDetection;

    /**
     * 移动侦测灵敏度
     */
    @TableField("motion_sensitivity")
    private Integer motionSensitivity;

    /**
     * 报警联动：1-门禁 2-广播 3-灯光 4-报警器
     */
    @TableField("alarm_linkage")
    private String alarmLinkage;

    /**
     * 预览地址
     */
    @TableField("preview_url")
    private String previewUrl;

    /**
     * 回放地址
     */
    @TableField("playback_url")
    private String playbackUrl;

    /**
     * 设备分组
     */
    @TableField("device_group")
    private String deviceGroup;

    /**
     * 负责人
     */
    @TableField("responsible_person")
    private String responsiblePerson;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

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
     * 设备描述
     */
    @TableField("description")
    private String description;

    /**
     * 扩展配置（JSON格式）
     */
    @TableField("extended_config")
    private String extendedConfig;
}
