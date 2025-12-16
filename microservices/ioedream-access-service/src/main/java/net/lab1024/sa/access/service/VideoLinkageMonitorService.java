package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 视频联动监控服务接口
 * <p>
 * 提供门禁与视频监控系统的智能联动功能：
 * - 门禁事件触发实时视频监控
 * - 人脸识别验证与身份确认
 * - 异常行为检测与告警推送
 * - 视频片段自动录制与存储
 * - 多画面监控与云台控制
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 清晰的方法注释
 * - 统一的数据传输对象
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VideoLinkageMonitorService {

    /**
     * 门禁事件触发视频联动
     * <p>
     * 当门禁事件发生时，自动触发视频监控系统：
     * - 启动相关摄像头录像
     * - 执行人脸识别验证
     * - 记录事件视频片段
     * - 实时推送给监控中心
     * </p>
     *
     * @param request 联动请求
     * @return 联动结果
     */
    ResponseDTO<VideoLinkageResult> triggerVideoLinkage(VideoLinkageRequest request);

    /**
     * 实时视频流获取
     * <p>
     * 获取指定摄像头的实时视频流：
     * - 多协议支持（RTSP/RTMP/HLS/WebRTC）
     * - 多码流自适应切换
     * - 实时性能监控
     * - 断线自动重连
     * </p>
     *
     * @param request 视频流请求
     * @return 视频流信息
     */
    ResponseDTO<VideoStreamResult> getRealTimeStream(VideoStreamRequest request);

    /**
     * 人脸识别验证
     * <p>
     * 通过视频流进行实时人脸识别验证：
     * - 活体检测防止欺骗
     * - 多角度人脸识别
     * - 特征提取与比对
     * - 置信度评估
     * </p>
     *
     * @param request 人脸识别请求
     * @return 识别结果
     */
    ResponseDTO<FaceRecognitionResult> performFaceRecognition(FaceRecognitionRequest request);

    /**
     * 异常行为检测
     * <p>
     * 基于视频流的智能异常行为检测：
     * - 尾随检测
     * - 可疑徘徊识别
     * - 强行闯入检测
     * - 异常物品携带识别
     * </p>
     *
     * @param request 行为检测请求
     * @return 检测结果
     */
    ResponseDTO<AbnormalBehaviorResult> detectAbnormalBehavior(AbnormalBehaviorRequest request);

    /**
     * 视频片段录制管理
     * <p>
     * 管理事件相关的视频片段录制：
     * - 事件触发录制
     * - 预录制缓冲
     * - 智能剪辑优化
     * - 云端同步存储
     * </p>
     *
     * @param request 录制请求
     * @return 录制结果
     */
    ResponseDTO<VideoRecordingResult> manageVideoRecording(VideoRecordingRequest request);

    /**
     * 多画面监控展示
     * <p>
     * 提供多画面实时监控展示功能：
     * - 动态布局调整
     * - 画面轮播设置
     * - 重点画面突出
     * - 告警画面自动切换
     * </p>
     *
     * @param request 多画面请求
     * @return 监控画面信息
     */
    ResponseDTO<MultiScreenResult> getMultiScreenView(MultiScreenRequest request);

    /**
     * 云台控制操作
     * <p>
     * 远程控制摄像头的云台操作：
     * - 上下左右移动
     * - 变倍对焦控制
     * - 预设位置调用
     * - 巡航路径设置
     * </p>
     *
     * @param request 云台控制请求
     * @return 控制结果
     */
    ResponseDTO<PTZControlResult> controlPTZCamera(PTZControlRequest request);

    /**
     * 历史视频回放
     * <p>
     * 提供历史视频的回放功能：
     * - 时间精确定位
     * - 倍速播放控制
     * - 事件片段跳转
     * - 下载分享功能
     * </p>
     *
     * @param request 回放请求
     * @return 回放信息
     */
    ResponseDTO<VideoPlaybackResult> playbackHistoricalVideo(VideoPlaybackRequest request);

    /**
     * 获取联动事件列表
     * <p>
     * 查询视频联动事件的历史记录：
     * - 事件类型筛选
     * - 时间范围查询
     * - 处理状态跟踪
     * - 统计分析报表
     * </p>
     *
     * @param request 查询请求
     * @return 事件列表
     */
    ResponseDTO<List<VideoLinkageEventVO>> getLinkageEvents(VideoLinkageEventQueryRequest request);

    /**
     * 获取监控统计报告
     * <p>
     * 生成视频监控系统的统计分析报告：
     * - 设备运行状态统计
     * - 事件处理效率分析
     * - 存储空间使用情况
     * - 系统性能指标报告
     * </p>
     *
     * @param request 报告请求
     * @return 统计报告
     */
    ResponseDTO<MonitorStatisticsVO> getMonitorStatistics(MonitorStatisticsRequest request);

    // ==================== 内部数据传输对象 ====================

    /**
     * 视频联动请求
     */
    class VideoLinkageRequest {
        private String accessEventId;      // 门禁事件ID
        private Long deviceId;              // 门禁设备ID
        private String userId;               // 用户ID
        private String userName;             // 用户姓名
        private Integer accessType;          // 访问类型（1-进入 2-离开）
        private String areaId;               // 区域ID
        private String accessTime;           // 访问时间
        private Integer verificationResult;   // 验证结果（1-成功 2-失败）
        private String eventType;            // 事件类型
        private List<Long> cameraIds;        // 关联摄像头ID列表
        private Boolean enableRecording;     // 是否启用录制
        private Boolean enableFaceRecognition; // 是否启用人脸识别
        private Integer recordDuration;      // 录制时长（秒）
        private String description;          // 事件描述
    }

    /**
     * 视频联动结果
     */
    class VideoLinkageResult {
        private String linkageId;            // 联动ID
        private String accessEventId;        // 门禁事件ID
        private LocalDateTime triggerTime;    // 触发时间
        private List<CameraLinkageInfo> cameraLinkages; // 摄像头联动信息
        private List<String> recordingIds;   // 录制文件ID列表
        private FaceVerificationResult faceVerification; // 人脸验证结果
        private List<AbnormalEvent> detectedEvents; // 检测到的异常事件
        private Integer linkageStatus;       // 联动状态（1-成功 2-部分成功 3-失败）
        private String statusDescription;    // 状态描述
        private Integer processingDuration;  // 处理时长（毫秒）
    }

    /**
     * 摄像头联动信息
     */
    class CameraLinkageInfo {
        private Long cameraId;               // 摄像头ID
        private String cameraName;           // 摄像头名称
        private String cameraLocation;       // 摄像头位置
        private String streamUrl;            // 视频流地址
        private Boolean recordingEnabled;    // 是否启用录制
        private String recordingId;          // 录制文件ID
        private LocalDateTime startTime;      // 开始时间
        private Integer linkagePriority;     // 联动优先级
        private String cameraStatus;         // 摄像头状态
    }

    /**
     * 人脸验证结果
     */
    class FaceVerificationResult {
        private Boolean verificationSuccess; // 验证是否成功
        private Double confidenceScore;      // 置信度评分
        private String matchedUserId;        // 匹配的用户ID
        private String matchedUserName;      // 匹配的用户姓名
        private String faceImageUrl;         // 抓拍的人脸图片URL
        private LocalDateTime verifyTime;     // 验证时间
        private String verifyMethod;         // 验证方法
        private List<String> failureReasons; // 失败原因
    }

    /**
     * 异常事件
     */
    class AbnormalEvent {
        private String eventId;              // 事件ID
        private String eventType;            // 事件类型
        private String description;          // 事件描述
        private LocalDateTime eventTime;     // 事件时间
        private Integer severityLevel;       // 严重等级
        private String imageUrl;             // 事件截图URL
        private String videoClipUrl;         // 视频片段URL
        private Boolean needAlert;           // 是否需要告警
    }

    /**
     * 视频流请求
     */
    class VideoStreamRequest {
        private Long cameraId;               // 摄像头ID
        private Integer channelId;           // 通道ID
        private String streamType;           // 流类型（MAIN/SUB/MOBILE）
        private String protocol;             // 协议类型（RTSP/RTMP/HLS/WebRTC）
        private Integer quality;             // 画质质量（1-高清 2-标清 3-流畅）
        private Boolean enableAudio;         // 是否启用音频
        private Integer maxConnections;      // 最大连接数
        private String clientType;           // 客户端类型（WEB/MOBILE/DESKTOP）
    }

    /**
     * 视频流结果
     */
    class VideoStreamResult {
        private String streamId;             // 流ID
        private String streamUrl;            // 流地址
        private String streamType;           // 流类型
        private String protocol;             // 协议类型
        private String quality;              // 画质质量
        private Integer width;               // 视频宽度
        private Integer height;              // 视频高度
        private Integer fps;                 // 帧率
        private Long bitrate;                // 码率
        private String expireTime;           // 过期时间
        private String accessToken;          // 访问令牌
        private Boolean audioEnabled;        // 音频是否启用
    }

    /**
     * 人脸识别请求
     */
    class FaceRecognitionRequest {
        private String streamId;             // 视频流ID
        private Long userId;                 // 用户ID
        private String userPhotoUrl;         // 用户照片URL
        private Integer recognitionTimeout;   // 识别超时时间（秒）
        private Double confidenceThreshold;  // 置信度阈值
        private Boolean enableLivenessCheck; // 是否启用人脸检测
        private Integer maxAttempts;         // 最大尝试次数
    }

    /**
     * 人脸识别结果
     */
    class FaceRecognitionResult {
        private Boolean recognitionSuccess;  // 识别是否成功
        private Double confidenceScore;      // 置信度评分
        private String matchedUserId;        // 匹配的用户ID
        private String matchedUserName;      // 匹配的用户姓名
        private String capturedFaceUrl;      // 抓拍的人脸图片URL
        private LocalDateTime recognitionTime; // 识别时间
        private Integer faceCount;           // 检测到的人脸数量
        private String faceQuality;          // 人脸质量评估
        private LivenessCheckResult livenessCheck; // 活体检测结果
        private List<String> failureReasons; // 识别失败原因
    }

    /**
     * 活体检测结果
     */
    class LivenessCheckResult {
        private Boolean isLive;              // 是否为活体
        private Double livenessScore;        // 活体评分
        private String detectionMethod;      // 检测方法
        private List<String> spoofingIndicators; // 欺骗指标
    }

    /**
     * 异常行为检测请求
     */
    class AbnormalBehaviorRequest {
        private String streamId;             // 视频流ID
        private String areaId;               // 区域ID
        private List<String> behaviorTypes;  // 检测的行为类型
        private Integer detectionDuration;   // 检测时长（秒）
        private Double sensitivityLevel;     // 灵敏度级别
        private Boolean enableAlert;         // 是否启用告警
    }

    /**
     * 异常行为检测结果
     */
    class AbnormalBehaviorResult {
        private String detectionId;          // 检测ID
        private Boolean hasAbnormalBehavior;  // 是否有异常行为
        private List<DetectedBehavior> detectedBehaviors; // 检测到的行为
        private LocalDateTime detectionTime;  // 检测时间
        private Integer behaviorCount;       // 异常行为数量
        private Double riskScore;            // 风险评分
        private String alertLevel;            // 告警级别
        private List<String> recommendedActions; // 建议措施
    }

    /**
     * 检测到的行为
     */
    class DetectedBehavior {
        private String behaviorType;        // 行为类型
        private String description;          // 行为描述
        private Double confidence;           // 置信度
        private LocalDateTime occurTime;     // 发生时间
        private String imageUrl;             // 行为截图URL
        private String videoClipUrl;         // 相关视频片段
        private Integer severityLevel;       // 严重等级
    }

    /**
     * 视频录制请求
     */
    class VideoRecordingRequest {
        private String linkageId;            // 联动ID
        private List<Long> cameraIds;        // 摄像头ID列表
        private Integer preRecordSeconds;    // 预录制时长（秒）
        private Integer postRecordSeconds;   // 后录制时长（秒）
        private String recordingQuality;     // 录制质量
        private Boolean enableAudio;         // 是否录制音频
        private String storageType;          // 存储类型（LOCAL/CLOUD/HYBRID）
        private Integer retentionDays;       // 保留天数
    }

    /**
     * 视频录制结果
     */
    class VideoRecordingResult {
        private String recordingTaskId;      // 录制任务ID
        private List<RecordingFileInfo> recordingFiles; // 录制文件信息
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private Long totalDuration;          // 总时长（秒）
        private Long totalSize;              // 总大小（字节）
        private Integer recordingStatus;     // 录制状态
        private List<String> tags;           // 标签列表
    }

    /**
     * 录制文件信息
     */
    class RecordingFileInfo {
        private String recordingId;          // 录制文件ID
        private Long cameraId;               // 摄像头ID
        private String fileName;             // 文件名
        private String filePath;             // 文件路径
        private Long fileSize;               // 文件大小（字节）
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private Integer duration;            // 时长（秒）
        private String quality;              // 画质质量
        private String format;               // 文件格式
        private String downloadUrl;          // 下载地址
    }

    /**
     * 多画面监控请求
     */
    class MultiScreenRequest {
        private List<Long> cameraIds;        // 摄像头ID列表
        private String layoutType;           // 布局类型（1x1/2x2/3x3/4x4/CUSTOM）
        private Integer displayCols;         // 显示列数
        private Integer displayRows;         // 显示行数
        private String quality;              // 画质质量
        private Boolean enableAudio;         // 是否启用音频
        private Integer refreshInterval;     // 刷新间隔（秒）
        private Boolean autoSwitchAlert;     // 是否自动切换告警画面
    }

    /**
     * 多画面监控结果
     */
    class MultiScreenResult {
        private String sessionId;            // 监控会话ID
        private String layoutType;           // 布局类型
        private List<CameraStreamInfo> cameraStreams; // 摄像头视频流信息
        private LocalDateTime createTime;     // 创建时间
        private Integer totalCameras;        // 总摄像头数
        private Integer activeCameras;       // 活跃摄像头数
        private String monitorUrl;           // 监控地址
        private List<String> availableQualities; // 可用画质选项
    }

    /**
     * 摄像头视频流信息
     */
    class CameraStreamInfo {
        private Long cameraId;               // 摄像头ID
        private String cameraName;           // 摄像头名称
        private String streamUrl;            // 视频流地址
        private String position;             // 在多画面中的位置
        private Integer width;               // 视频宽度
        private Integer height;              // 视频高度
        private String status;               // 流状态
        private LocalDateTime lastUpdateTime; // 最后更新时间
    }

    /**
     * 云台控制请求
     */
    class PTZControlRequest {
        private Long cameraId;               // 摄像头ID
        private String action;               // 控制动作
        private Integer speed;               // 控制速度（1-10）
        private Integer duration;            // 持续时间（毫秒）
        private Double pan;                  // 水平转动角度
        private Double tilt;                 // 垂直转动角度
        private Double zoom;                 // 变倍倍数
        private Integer presetId;            // 预设位ID
        private String patrolPath;           // 巡航路径
    }

    /**
     * 云台控制结果
     */
    class PTZControlResult {
        private Boolean controlSuccess;      // 控制是否成功
        private String commandId;            // 命令ID
        private LocalDateTime executeTime;    // 执行时间
        private Integer responseTime;        // 响应时间（毫秒）
        private String currentPosition;       // 当前位置
        private Integer currentZoom;         // 当前变倍
        private List<PTZPresetInfo> availablePresets; // 可用预设位
    }

    /**
     * PTZ预设位信息
     */
    class PTZPresetInfo {
        private Integer presetId;            // 预设位ID
        private String presetName;           // 预设位名称
        private String description;          // 描述
        private String position;             // 位置信息
    }

    /**
     * 视频回放请求
     */
    class VideoPlaybackRequest {
        private Long cameraId;               // 摄像头ID
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private String playbackQuality;      // 回放质量
        private Double playbackSpeed;        // 播放速度
        private Boolean enableAudio;         // 是否启用音频
        private String eventType;            // 事件类型筛选
    }

    /**
     * 视频回放结果
     */
    class VideoPlaybackResult {
        private String playbackId;           // 回放ID
        private String playbackUrl;          // 回放地址
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private Integer totalDuration;       // 总时长（秒）
        private List<VideoEventMarker> eventMarkers; // 事件标记点
        private String quality;              // 画质质量
        private Boolean audioAvailable;      // 音频是否可用
    }

    /**
     * 视频事件标记点
     */
    class VideoEventMarker {
        private String eventId;              // 事件ID
        private LocalDateTime eventTime;     // 事件时间
        private String eventType;            // 事件类型
        private String description;          // 事件描述
        private Integer offsetSeconds;       // 偏移秒数
        private String thumbnailUrl;         // 缩略图URL
    }

    /**
     * 视频联动事件查询请求
     */
    class VideoLinkageEventQueryRequest {
        private String userId;               // 用户ID
        private Long deviceId;               // 设备ID
        private String areaId;               // 区域ID
        private String eventType;            // 事件类型
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private Integer linkageStatus;       // 联动状态
        private Integer pageNum;             // 页码
        private Integer pageSize;            // 每页大小
    }

    /**
     * 视频联动事件视图对象
     */
    class VideoLinkageEventVO {
        private String linkageId;            // 联动ID
        private String accessEventId;        // 门禁事件ID
        private String userId;               // 用户ID
        private String userName;             // 用户姓名
        private Long deviceId;               // 设备ID
        private String deviceName;           // 设备名称
        private String areaId;               // 区域ID
        private String areaName;             // 区域名称
        private String eventType;            // 事件类型
        private String accessTime;           // 访问时间
        private Integer linkageStatus;       // 联动状态
        private LocalDateTime triggerTime;    // 触发时间
        private Integer cameraCount;         // 关联摄像头数量
        private Boolean faceVerificationSuccess; // 人脸验证是否成功
        private Integer abnormalEventCount;  // 异常事件数量
        private String description;          // 描述
    }

    /**
     * 监控统计请求
     */
    class MonitorStatisticsRequest {
        private List<Long> cameraIds;        // 摄像头ID列表
        private String statisticsType;       // 统计类型（DAILY/WEEKLY/MONTHLY）
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private List<String> metrics;        // 统计指标
        private String groupBy;              // 分组方式
    }

    /**
     * 监控统计结果
     */
    class MonitorStatisticsVO {
        private String statisticsPeriod;     // 统计周期
        private Long totalLinkageEvents;     // 总联动事件数
        private Long successfulLinkages;     // 成功联动数
        private Long faceVerificationAttempts; // 人脸验证尝试次数
        private Long faceVerificationSuccesses; // 人脸验证成功次数
        private Double faceVerificationSuccessRate; // 人脸验证成功率
        private Long abnormalBehaviorDetections; // 异常行为检测次数
        private Long totalRecordingDuration; // 总录制时长（小时）
        private Long totalStorageUsed;       // 总存储使用量（GB）
        private Double averageResponseTime;  // 平均响应时间（毫秒）
        private Integer onlineCameraCount;   // 在线摄像头数量
        private Integer totalCameraCount;    // 总摄像头数量
        private List<CameraStatistics> cameraStatistics; // 摄像头统计
    }

    /**
     * 摄像头统计信息
     */
    class CameraStatistics {
        private Long cameraId;               // 摄像头ID
        private String cameraName;           // 摄像头名称
        private Long linkageEventCount;      // 联动事件数量
        private Long recordingDuration;      // 录制时长（小时）
        private Long storageUsed;            // 存储使用量（GB）
        private String status;               // 摄像头状态
        private Double onlineRate;           // 在线率
        private List<String> topEventTypes;  // 主要事件类型
    }
}