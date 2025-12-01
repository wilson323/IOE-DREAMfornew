package net.lab1024.sa.admin.module.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 视频设备视图对象
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */

@Schema(description = "视频设备视图对象")
public class VideoDeviceVO {

    /**
     * 视频设备ID
     */
    @Schema(description = "视频设备ID", example = "1")
    private Long videoDeviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "CAM001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主入口摄像头")
    private String deviceName;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型", example = "CAMERA")
    private String deviceType;

    /**
     * 设备类型描述
     */
    @Schema(description = "设备类型描述", example = "网络摄像头")
    private String deviceTypeDesc;

    /**
     * 设备IP地址
     */
    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String deviceIp;

    /**
     * 设备端口
     */
    @Schema(description = "设备端口", example = "554")
    private Integer devicePort;

    /**
     * 流媒体端口
     */
    @Schema(description = "流媒体端口", example = "554")
    private Integer streamPort;

    /**
     * 设备状态
     */
    @Schema(description = "设备状态", example = "ONLINE")
    private String deviceStatus;

    /**
     * 设备状态描述
     */
    @Schema(description = "设备状态描述", example = "在线")
    private String deviceStatusDesc;

    /**
     * 设备位置
     */
    @Schema(description = "设备位置", example = "一楼大厅")
    private String deviceLocation;

    /**
     * 所属区域ID
     */
    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    /**
     * 所属区域名称
     */
    @Schema(description = "所属区域名称", example = "主楼区域")
    private String areaName;

    /**
     * RTSP流地址
     */
    @Schema(description = "RTSP流地址", example = "rtsp://192.168.1.100:554/stream")
    private String rtspUrl;

    /**
     * 是否启用云台控制
     */
    @Schema(description = "是否启用云台控制", example = "1")
    private Integer ptzEnabled;

    /**
     * 录像开关
     */
    @Schema(description = "录像开关", example = "1")
    private Integer recordEnabled;

    /**
     * 分辨率
     */
    @Schema(description = "分辨率", example = "1080P")
    private String resolution;

    /**
     * 帧率
     */
    @Schema(description = "帧率", example = "25")
    private Integer frameRate;

    /**
     * 视频制式
     */
    @Schema(description = "视频制式", example = "PAL")
    private String videoFormat;

    /**
     * 制造商
     */
    @Schema(description = "制造商", example = "海康威视")
    private String manufacturer;

    /**
     * 型号
     */
    @Schema(description = "型号", example = "DS-2CD2142FWD-I")
    private String modelNumber;

    /**
     * 固件版本
     */
    @Schema(description = "固件版本", example = "V5.5.0")
    private String firmwareVersion;

    /**
     * 安装日期
     */
    @Schema(description = "安装日期", example = "2025-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime installDate;

    /**
     * 最后在线时间
     */
    @Schema(description = "最后在线时间", example = "2025-01-16 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;

    /**
     * 设备描述
     */
    @Schema(description = "设备描述", example = "主入口监控摄像头，支持夜视功能")
    private String deviceDescription;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-16 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名", example = "管理员")
    private String createUserName;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID", example = "2")
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    @Schema(description = "更新人姓名", example = "操作员")
    private String updateUserName;

    /**
     * 版本号
     */
    @Schema(description = "版本号", example = "1")
    private Integer version;

    /**
     * 在线时长（分钟）
     */
    @Schema(description = "在线时长（分钟）", example = "1440")
    private Long onlineMinutes;

    /**
     * 今日录像时长（分钟）
     */
    @Schema(description = "今日录像时长（分钟）", example = "480")
    private Integer todayRecordMinutes;

    /**
     * 存储空间使用率（%）
     */
    @Schema(description = "存储空间使用率（%）", example = "65.5")
    private Double storageUsageRate;

    /**
     * CPU使用率（%）
     */
    @Schema(description = "CPU使用率（%）", example = "25.8")
    private Double cpuUsage;

    /**
     * 内存使用率（%）
     */
    @Schema(description = "内存使用率（%）", example = "42.3")
    private Double memoryUsage;

    /**
     * 网络状态
     */
    @Schema(description = "网络状态", example = "NORMAL")
    private String networkStatus;

    /**
     * 网络状态描述
     */
    @Schema(description = "网络状态描述", example = "正常")
    private String networkStatusDesc;
}