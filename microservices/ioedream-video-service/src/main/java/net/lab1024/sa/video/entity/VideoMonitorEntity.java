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
 * 视频监控会话实体类
 * <p>
 * 实时监控会话管理实体，支持多屏监控和云台控制
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_monitor")
public class VideoMonitorEntity extends BaseEntity {

    /**
     * 监控会话ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long monitorId;

    /**
     * 会话名称
     */
    @TableField("session_name")
    private String sessionName;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 客户端IP
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 客户端类型：1-WEB 2-PC 3-MOBILE 4-PAD
     */
    @TableField("client_type")
    private Integer clientType;

    /**
     * 浏览器信息
     */
    @TableField("browser_info")
    private String browserInfo;

    /**
     * 屏幕布局：1-单屏 2-四分屏 3-九分屏 4-十六分屏 5-二十五分屏
     */
    @TableField("screen_layout")
    private Integer screenLayout;

    /**
     * 分屏数量
     */
    @TableField("screen_count")
    private Integer screenCount;

    /**
     * 设备ID列表（JSON数组）
     */
    @TableField("device_ids")
    private String deviceIds;

    /**
     * 通道号列表（JSON数组）
     */
    @TableField("channel_numbers")
    private String channelNumbers;

    /**
     * 流ID列表（JSON数组）
     */
    @TableField("stream_ids")
    private String streamIds;

    /**
     * 会话状态：1-活跃 2-暂停 3-断开 4-异常
     */
    @TableField("session_status")
    private Integer sessionStatus;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 持续时间（秒）
     */
    @TableField("duration")
    private Long duration;

    /**
     * 最后活跃时间
     */
    @TableField("last_active_time")
    private LocalDateTime lastActiveTime;

    /**
     * 是否音频：0-关闭 1-开启
     */
    @TableField("audio_enabled")
    private Integer audioEnabled;

    /**
     * 是否录制：0-不录制 1-录制
     */
    @TableField("record_enabled")
    private Integer recordEnabled;

    /**
     * 录制质量：1-流畅 2-标准 3-高清 4-超清
     */
    @TableField("record_quality")
    private Integer recordQuality;

    /**
     * 是否云台控制：0-禁用 1-启用
     */
    @TableField("ptz_enabled")
    private Integer ptzEnabled;

    /**
     * 是否数字变倍：0-禁用 1-启用
     */
    @TableField("digital_zoom_enabled")
    private Integer digitalZoomEnabled;

    /**
     * 是否全屏：0-窗口 1-全屏
     */
    @TableField("fullscreen_enabled")
    private Integer fullscreenEnabled;

    /**
     * 布局配置（JSON格式）
     */
    @TableField("layout_config")
    private String layoutConfig;

    /**
     * 显示参数（JSON格式）
     */
    @TableField("display_params")
    private String displayParams;

    /**
     * 网络状态：1-优秀 2-良好 3-一般 4-差
     */
    @TableField("network_status")
    private Integer networkStatus;

    /**
     * 网络延迟（毫秒）
     */
    @TableField("network_latency")
    private Integer networkLatency;

    /**
     * 丢包率（百分比）
     */
    @TableField("packet_loss_rate")
    private Double packetLossRate;

    /**
     * 带宽占用（Kbps）
     */
    @TableField("bandwidth_usage")
    private Integer bandwidthUsage;

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
     * 异常次数
     */
    @TableField("exception_count")
    private Integer exceptionCount;

    /**
     * 最后异常时间
     */
    @TableField("last_exception_time")
    private LocalDateTime lastExceptionTime;

    /**
     * 异常信息
     */
    @TableField("exception_message")
    private String exceptionMessage;

    /**
     * 重连次数
     */
    @TableField("reconnect_count")
    private Integer reconnectCount;

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
     * 权限级别：1-查看 2-控制 3-管理
     */
    @TableField("permission_level")
    private Integer permissionLevel;

    /**
     * 会话标签（JSON数组）
     */
    @TableField("tags")
    private String tags;

    /**
     * 会话备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
