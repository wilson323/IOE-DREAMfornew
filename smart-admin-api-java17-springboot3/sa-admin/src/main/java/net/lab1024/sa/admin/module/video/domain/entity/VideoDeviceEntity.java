package net.lab1024.sa.admin.module.video.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;

/**
 * 视频监控设备实体类
 *
 * 继承SmartDeviceEntity，包含视频监控特有字段
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_device")
public class VideoDeviceEntity extends SmartDeviceEntity {

    /**
     * 摄像机类型 (DOME-半球机, BULLET-枪机, PTZ-球机, BOX-盒式机)
     */
    private String cameraType;

    /**
     * 视频编码格式 (H264, H265, MJPEG)
     */
    private String videoFormat;

    /**
     * 音频编码格式 (AAC, G711A, G711U)
     */
    private String audioFormat;

    /**
     * 分辨率 (720P, 1080P, 4K)
     */
    private String resolution;

    /**
     * 帧率(fps)
     */
    private Integer frameRate;

    /**
     * 码率(kbps)
     */
    private Integer bitRate;

    /**
     * 是否支持云台控制 (0-不支持, 1-支持)
     */
    private Integer ptzEnabled;

    /**
     * 云台类型 (FIXED-固定, HORIZONTAL-水平, VERTICAL-垂直, FULL-全方位)
     */
    private String ptzType;

    /**
     * 水平旋转角度范围
     */
    private String horizontalRange;

    /**
     * 垂直旋转角度范围
     */
    private String verticalRange;

    /**
     * 变焦倍数
     */
    private Integer zoomRatio;

    /**
     * 是否支持红外夜视 (0-不支持, 1-支持)
     */
    private Integer infraredEnabled;

    /**
     * 红外距离(米)
     */
    private Integer infraredDistance;

    /**
     * 是否支持智能分析 (0-不支持, 1-支持)
     */
    private Integer intelligentAnalysisEnabled;

    /**
     * 智能分析功能(JSON格式，如：人脸检测、行为分析、车牌识别等)
     */
    private String intelligentFunctions;

    /**
     * 是否支持音频录制 (0-不支持, 1-支持)
     */
    private Integer audioRecordingEnabled;

    /**
     * 是否支持双向对讲 (0-不支持, 1-支持)
     */
    private Integer twoWayAudioEnabled;

    /**
     * 报警输入数量
     */
    private Integer alarmInputCount;

    /**
     * 报警输出数量
     */
    private Integer alarmOutputCount;

    /**
     * 存储方式 (LOCAL-本地存储, NETWORK-网络存储, HYBRID-混合存储)
     */
    private String storageType;

    /**
     * 存储天数
     */
    private Integer storageDays;

    /**
     * 是否支持移动侦测 (0-不支持, 1-支持)
     */
    private Integer motionDetectionEnabled;

    /**
     * 移动侦测灵敏度 (1-10)
     */
    private Integer motionSensitivity;

    /**
     * 是否支持遮挡报警 (0-不支持, 1-支持)
     */
    private Integer occlusionAlarmEnabled;

    /**
     * 监控区域ID
     */
    private Long monitorAreaId;

    /**
     * 安装高度(米)
     */
    private Double installHeight;

    /**
     * 镜头焦距(mm)
     */
    private Integer focalLength;

    /**
     * 视野角度(度)
     */
    private Integer viewAngle;
}