package net.lab1024.sa.admin.module.attendance.manager;

import lombok.Data;

/**
 * 考勤设备配置类
 * <p>
 * 用于存储考勤设备的详细配置参数
 * 包括工作模式、验证方式、功能设置等
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-30
 */
@Data
public class AttendanceDeviceConfig {

    /**
     * 工作模式
     * NORMAL-正常模式，DEBUG-调试模式，MAINTAIN-维护模式
     */
    private String workMode;

    /**
     * 打卡方式
     * FINGERPRINT-指纹，FACE-人脸，CARD-刷卡，PASSWORD-密码
     */
    private String punchMode;

    /**
     * 是否启用GPS验证
     */
    private Boolean gpsEnabled;

    /**
     * GPS验证范围（米）
     */
    private Integer gpsRange;

    /**
     * 是否启用拍照验证
     */
    private Boolean photoEnabled;

    /**
     * 拍照质量
     * LOW-低，MEDIUM-中，HIGH-高
     */
    private String photoQuality;

    /**
     * 是否启用人脸识别
     */
    private Boolean faceRecognitionEnabled;

    /**
     * 人脸识别阈值（0-100）
     */
    private Integer faceThreshold;

    /**
     * 是否启用语音提示
     */
    private Boolean voicePromptEnabled;

    /**
     * 语音音量（0-100）
     */
    private Integer voiceVolume;

    /**
     * 是否启用LED指示灯
     */
    private Boolean ledIndicatorEnabled;

    /**
     * LED指示灯颜色
     * RED-红色，GREEN-绿色，BLUE-蓝色
     */
    private String ledColor;

    /**
     * 是否自动同步时间
     */
    private Boolean autoSyncTime;

    /**
     * 时间同步间隔（秒）
     */
    private Integer syncInterval;

    /**
     * 是否启用离线存储
     */
    private Boolean offlineStorageEnabled;

    /**
     * 最大离线记录数
     */
    private Integer maxOfflineRecords;

    /**
     * 是否启用体温检测
     */
    private Boolean temperatureDetectionEnabled;

    /**
     * 体温阈值（摄氏度）
     */
    private Double temperatureThreshold;

    /**
     * 是否启用口罩检测
     */
    private Boolean maskDetectionEnabled;

    /**
     * 口罩检测阈值（0-100）
     */
    private Integer maskThreshold;

    /**
     * 是否启用胁迫报警
     */
    private Boolean duressAlarmEnabled;

    /**
     * 胁迫报警触发方式
     * FINGERPRINT-特定指纹，PASSWORD-特定密码，BUTTON-胁迫按钮
     */
    private String duressTriggerType;

    /**
     * 是否启用活体检测
     */
    private Boolean livenessDetectionEnabled;

    /**
     * 活体检测级别
     * LOW-低，MEDIUM-中，HIGH-高
     */
    private String livenessLevel;

    /**
     * 设备超时时间（秒）
     */
    private Integer deviceTimeout;

    /**
     * 连续打卡间隔（秒）
     */
    private Integer punchInterval;

    /**
     * 是否启用防代打卡
     */
    private Boolean antiProxyEnabled;

    /**
     * 设备语言设置
     */
    private String deviceLanguage;

    /**
     * 设备显示亮度（0-100）
     */
    private Integer displayBrightness;

    /**
     * 是否启用屏幕保护
     */
    private Boolean screenSaverEnabled;

    /**
     * 屏幕保护时间（秒）
     */
    private Integer screenSaverTime;

    /**
     * 数据上传方式
     * REALTIME-实时，BATCH-批量，MANUAL-手动
     */
    private String dataUploadMode;

    /**
     * 批量上传间隔（分钟）
     */
    private Integer batchUploadInterval;

    /**
     * 是否启用数据加密
     */
    private Boolean dataEncryptionEnabled;

    /**
     * 加密密钥
     */
    private String encryptionKey;

    /**
     * 是否启用日志记录
     */
    private Boolean loggingEnabled;

    /**
     * 日志保留天数
     */
    private Integer logRetentionDays;

    /**
     * 是否启用远程控制
     */
    private Boolean remoteControlEnabled;

    /**
     * 远程控制端口
     */
    private Integer remoteControlPort;

    /**
     * 设备固件版本
     */
    private String firmwareVersion;

    /**
     * 是否自动升级固件
     */
    private Boolean autoUpgradeEnabled;

    /**
     * 设备序列号
     */
    private String deviceSerialNumber;

    /**
     * 设备MAC地址
     */
    private String deviceMacAddress;

    /**
     * 设备IP地址
     */
    private String deviceIpAddress;

    /**
     * 网络端口
     */
    private Integer networkPort;

    /**
     * 心跳间隔（秒）
     */
    private Integer heartbeatInterval;

    /**
     * 重连次数
     */
    private Integer reconnectAttempts;

    /**
     * 重连间隔（秒）
     */
    private Integer reconnectInterval;

    /**
     * 扩展配置（JSON格式）
     */
    private String extensionConfig;
}