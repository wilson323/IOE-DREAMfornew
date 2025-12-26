package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 录像详情视图对象
 * <p>
 * 用于返回录像详细信息
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含完整的详细信息
 * - 支持扩展属性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
public class VideoRecordingDetailVO extends VideoRecordingVO {

    /**
     * 完整文件路径
     */
    private String fullFilePath;

    /**
     * 相对路径
     */
    private String relativePath;

    /**
     * 文件哈希值（MD5）
     */
    private String fileHash;

    /**
     * 文件创建时间
     */
    private LocalDateTime fileCreateTime;

    /**
     * 文件修改时间
     */
    private LocalDateTime fileModifyTime;

    /**
     * 文件访问时间
     */
    private LocalDateTime fileAccessTime;

    /**
     * 视频时长（毫秒）
     */
    private Long videoDurationMs;

    /**
     * 视频宽度
     */
    private Integer videoWidth;

    /**
     * 视频高度
     */
    private Integer videoHeight;

    /**
     * 视频宽高比
     */
    private String aspectRatio;

    /**
     * 视频帧总数
     */
    private Long totalFrames;

    /**
     * 音频采样率
     */
    private Integer audioSampleRate;

    /**
     * 音频声道数
     */
    private Integer audioChannels;

    /**
     * 音频比特率
     */
    private Integer audioBitrate;

    /**
     * 容器格式
     */
    private String containerFormat;

    /**
     * 录制触发方式
     */
    private String triggerMethod;

    /**
     * 录制触发原因
     */
    private String triggerReason;

    /**
     * 录制任务ID
     */
    private String recordingTaskId;

    /**
     * 录制用户ID
     */
    private Long recordingUserId;

    /**
     * 录制用户姓名
     */
    private String recordingUserName;

    /**
     * 录制客户端IP
     */
    private String recordingClientIp;

    /**
     * 录制用户代理
     */
    private String recordingUserAgent;

    /**
     * 关联的事件列表
     */
    private List<Map<String, Object>> relatedEvents;

    /**
     * 关联的告警列表
     */
    private List<Map<String, Object>> relatedAlarms;

    /**
     * 关联的快照列表
     */
    private List<Map<String, Object>> relatedSnapshots;

    /**
     * 关联的日志列表
     */
    private List<Map<String, Object>> relatedLogs;

    /**
     * 视频分析结果
     */
    private Map<String, Object> videoAnalysisResult;

    /**
     * 关键帧时间点
     */
    private List<Long> keyFrameTimestamps;

    /**
     * 场景变化时间点
     */
    private List<Long> sceneChangeTimestamps;

    /**
     * 运动检测时间点
     */
    private List<Long> motionDetectionTimestamps;

    /**
     * 音频检测时间点
     */
    private List<Long> audioDetectionTimestamps;

    /**
     * 人脸检测时间点
     */
    private List<Long> faceDetectionTimestamps;

    /**
     * 车辆检测时间点
     */
    private List<Long> vehicleDetectionTimestamps;

    /**
     * 物体检测时间点
     */
    private List<Long> objectDetectionTimestamps;

    /**
     * 录像标签
     */
    private List<String> tags;

    /**
     * 录像分类
     */
    private List<String> categories;

    /**
     * 访问权限级别
     */
    private Integer accessLevel;

    /**
     * 访问权限级别描述
     */
    private String accessLevelDesc;

    /**
     * 加密状态
     */
    private Integer encrypted;

    /**
     * 加密状态描述
     */
    private String encryptedDesc;

    /**
     * 加密算法
     */
    private String encryptionAlgorithm;

    /**
     * 水印信息
     */
    private String watermarkInfo;

    /**
     * 数字签名
     */
    private String digitalSignature;

    /**
     * 审计日志
     */
    private List<Map<String, Object>> auditLogs;

    /**
     * 存储成本（元）
     */
    private Double storageCost;

    /**
     * 存储期限（天）
     */
    private Integer storageRetentionDays;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 自动删除时间
     */
    private LocalDateTime autoDeleteTime;

    /**
     * 压缩状态
     */
    private Integer compressed;

    /**
     * 压缩状态描述
     */
    private String compressedDesc;

    /**
     * 压缩算法
     */
    private String compressionAlgorithm;

    /**
     * 压缩前大小（字节）
     */
    private Long originalSize;

    /**
     * 压缩率（百分比）
     */
    private Double compressionRatio;

    /**
     * 索引状态
     */
    private Integer indexed;

    /**
     * 索引状态描述
     */
    private String indexedDesc;

    /**
     * 索引内容
     */
    private String indexContent;

    /**
     * 检索关键词
     */
    private List<String> searchKeywords;

    /**
     * 数据版本
     */
    private Integer dataVersion;

    /**
     * 同步状态
     */
    private Integer syncStatus;

    /**
     * 同步状态描述
     */
    private String syncStatusDesc;

    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

    /**
     * 冗余备份位置
     */
    private List<String> redundantBackupLocations;

    /**
     * 灾难恢复状态
     */
    private Integer disasterRecoveryStatus;

    /**
     * 灾难恢复状态描述
     */
    private String disasterRecoveryStatusDesc;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;
}
