package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 视频设备视图对象
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
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
    @Schema(description = "所属区域名称", example = "办公区")
    private String areaName;

    /**
     * RTSP地址
     */
    @Schema(description = "RTSP地址", example = "rtsp://192.168.1.100:554/stream")
    private String rtspUrl;

    /**
     * 登录用户名
     */
    @Schema(description = "登录用户名", example = "admin")
    private String username;

    /**
     * 登录密码（已加密）
     */
    @Schema(description = "登录密码（已加密）", example = "****")
    private String password;

    /**
     * 是否启用云台控制
     */
    @Schema(description = "是否启用云台控制", example = "1")
    private Integer ptzEnabled;

    /**
     * 是否启用云台控制描述
     */
    @Schema(description = "是否启用云台控制描述", example = "启用")
    private String ptzEnabledDesc;

    /**
     * 录像开关
     */
    @Schema(description = "录像开关", example = "1")
    private Integer recordEnabled;

    /**
     * 录像开关描述
     */
    @Schema(description = "录像开关描述", example = "启用")
    private String recordEnabledDesc;

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
    @Schema(description = "型号", example = "DS-2CD3T46EWD-L")
    private String model;

    /**
     * 固件版本
     */
    @Schema(description = "固件版本", example = "V5.5.0")
    private String firmwareVersion;

    /**
     * 设备描述
     */
    @Schema(description = "设备描述", example = "主通道高清摄像机，支持夜视")
    private String deviceDescription;

    /**
     * 最后在线时间
     */
    @Schema(description = "最后在线时间", example = "2025-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-01 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    /**
     * 创建人名称
     */
    @Schema(description = "创建人名称", example = "管理员")
    private String createUserName;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    /**
     * 更新人名称
     */
    @Schema(description = "更新人名称", example = "管理员")
    private String updateUserName;
}