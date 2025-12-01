package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁视频联动VO
 * 基于现有监控系统和WebSocket基础设施的视频联动功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁视频联动VO")
public class VideoLinkageVO {

    /**
     * 联动ID
     */
    @Schema(description = "联动ID", example = "1001")
    private Long linkageId;

    /**
     * 联动编号
     */
    @Schema(description = "联动编号", example = "VL20251125001")
    private String linkageCode;

    /**
     * 门禁设备ID
     */
    @Schema(description = "门禁设备ID", example = "1001")
    private Long accessDeviceId;

    /**
     * 门禁设备名称
     */
    @Schema(description = "门禁设备名称", example = "主门禁设备")
    private String accessDeviceName;

    /**
     * 门禁设备位置
     */
    @Schema(description = "门禁设备位置", example = "主入口")
    private String accessDeviceLocation;

    /**
     * 联动类型
     * ACCESS_EVENT-访问事件
     * ALERT_EVENT-告警事件
     * DOOR_OPEN-开门事件
     * DOOR_CLOSE-关门事件
     * FORCED_ENTRY-强制进入
     * EMERGENCY-紧急情况
     * MANUAL_TRIGGER-手动触发
     */
    @Schema(description = "联动类型", example = "ACCESS_EVENT")
    private String linkageType;

    /**
     * 联动事件
     */
    @Schema(description = "联动事件", example = "INVALID_CARD_ACCESS")
    private String linkageEvent;

    /**
     * 联动触发条件
     */
    @Schema(description = "联动触发条件", example = "检测到无效卡访问时触发")
    private String triggerCondition;

    /**
     * 联动状态
     * PENDING-待执行
     * EXECUTING-执行中
     * SUCCESS-执行成功
     * FAILED-执行失败
     * CANCELLED-已取消
     */
    @Schema(description = "联动状态", example = "SUCCESS")
    private String linkageStatus;

    /**
     * 视频设备列表
     */
    @Schema(description = "视频设备列表")
    private List<VideoDeviceVO> videoDevices;

    /**
     * 主视频设备ID
     */
    @Schema(description = "主视频设备ID", example = "2001")
    private Long primaryVideoDeviceId;

    /**
     * 主视频设备名称
     */
    @Schema(description = "主视频设备名称", example = "主入口监控摄像头")
    private String primaryVideoDeviceName;

    /**
     * 录制开始时间
     */
    @Schema(description = "录制开始时间", example = "2025-11-25T10:30:00")
    private LocalDateTime recordStartTime;

    /**
     * 录制结束时间
     */
    @Schema(description = "录制结束时间", example = "2025-11-25T10:35:00")
    private LocalDateTime recordEndTime;

    /**
     * 录制时长（秒）
     */
    @Schema(description = "录制时长", example = "300")
    private Integer recordDuration;

    /**
     * 录制文件路径
     */
    @Schema(description = "录制文件路径", example = "/videos/2025/11/25/access_1001_20251125103000.mp4")
    private String recordFilePath;

    /**
     * 录制文件URL
     */
    @Schema(description = "录制文件URL", example = "http://example.com/videos/access_1001_20251125103000.mp4")
    private String recordFileUrl;

    /**
     * 录制文件大小（字节）
     */
    @Schema(description = "录制文件大小", example = "52428800")
    private Long recordFileSize;

    /**
     * 视频质量
     * LOW-低质量
     * MEDIUM-中等质量
     * HIGH-高质量
     * ULTRA-超高质量
     */
    @Schema(description = "视频质量", example = "HIGH")
    private String videoQuality;

    /**
     * 视频编码格式
     */
    @Schema(description = "视频编码格式", example = "H264")
    private String videoEncoding;

    /**
     * 音频录制启用
     */
    @Schema(description = "音频录制启用", example = "true")
    private Boolean audioRecordingEnabled;

    /**
     * 触发前录制时间（秒）
     */
    @Schema(description = "触发前录制时间", example = "10")
    private Integer preTriggerRecordingSeconds;

    /**
     * 触发后录制时间（秒）
     */
    @Schema(description = "触发后录制时间", example = "60")
    private Integer postTriggerRecordingSeconds;

    /**
     * 相关人员ID
     */
    @Schema(description = "相关人员ID", example = "12345")
    private Long relatedPersonId;

    /**
     * 相关人员姓名
     */
    @Schema(description = "相关人员姓名", example = "张三")
    private String relatedPersonName;

    /**
     * 快照时间点列表
     */
    @Schema(description = "快照时间点列表")
    private List<VideoSnapshotVO> snapshots;

    /**
     * 联动触发时间
     */
    @Schema(description = "联动触发时间", example = "2025-11-25T10:30:00")
    private LocalDateTime triggerTime;

    /**
     * 联动执行开始时间
     */
    @Schema(description = "联动执行开始时间", example = "2025-11-25T10:30:01")
    private LocalDateTime executeStartTime;

    /**
     * 联动执行完成时间
     */
    @Schema(description = "联动执行完成时间", example = "2025-11-25T10:30:05")
    private LocalDateTime executeCompleteTime;

    /**
     * 联动响应时间（毫秒）
     */
    @Schema(description = "联动响应时间", example = "1500")
    private Long responseTimeMs;

    /**
     * 是否启用实时预览
     */
    @Schema(description = "是否启用实时预览", example = "true")
    private Boolean realtimePreviewEnabled;

    /**
     * 实时预览URL
     */
    @Schema(description = "实时预览URL", example = "rtsp://example.com/live/camera_2001")
    private String realtimePreviewUrl;

    /**
     * 云存储同步启用
     */
    @Schema(description = "云存储同步启用", example = "true")
    private Boolean cloudStorageSyncEnabled;

    /**
     * 云存储文件URL
     */
    @Schema(description = "云存储文件URL", example = "https://cloud.example.com/videos/access_1001.mp4")
    private String cloudStorageUrl;

    /**
     * 备份状态
     * BACKUP_SUCCESS-备份成功
     * BACKUP_FAILED-备份失败
     * BACKUP_PENDING-待备份
     */
    @Schema(description = "备份状态", example = "BACKUP_SUCCESS")
    private String backupStatus;

    /**
     * 联动配置
     */
    @Schema(description = "联动配置")
    private LinkageConfigVO linkageConfig;

    /**
     * 处理日志
     */
    @Schema(description = "处理日志")
    private List<LinkageLogVO> processLogs;

    /**
     * 附加数据
     */
    @Schema(description = "附加数据")
    private Map<String, Object> additionalData;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-11-25T10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-11-25T10:35:00")
    private LocalDateTime updateTime;

    // 内部类：视频设备信息
    @Schema(description = "视频设备信息")
    public static class VideoDeviceVO {
        @Schema(description = "视频设备ID", example = "2001")
        private Long videoDeviceId;

        @Schema(description = "视频设备名称", example = "主入口监控摄像头")
        private String videoDeviceName;

        @Schema(description = "视频设备位置", example = "主入口上方")
        private String videoDeviceLocation;

        @Schema(description = "设备IP地址", example = "192.168.1.101")
        private String deviceIp;

        @Schema(description = "设备状态", example = "ONLINE")
        private String deviceStatus;

        @Schema(description = "是否为主设备", example = "true")
        private Boolean isPrimary;

        @Schema(description = "录制启用", example = "true")
        private Boolean recordingEnabled;

        @Schema(description = "实时预览URL", example = "rtsp://192.168.1.101:554/live")
        private String previewUrl;

        // Getters and Setters
        public Long getVideoDeviceId() { return videoDeviceId; }
        public void setVideoDeviceId(Long videoDeviceId) { this.videoDeviceId = videoDeviceId; }

        public String getVideoDeviceName() { return videoDeviceName; }
        public void setVideoDeviceName(String videoDeviceName) { this.videoDeviceName = videoDeviceName; }

        public String getVideoDeviceLocation() { return videoDeviceLocation; }
        public void setVideoDeviceLocation(String videoDeviceLocation) { this.videoDeviceLocation = videoDeviceLocation; }

        public String getDeviceIp() { return deviceIp; }
        public void setDeviceIp(String deviceIp) { this.deviceIp = deviceIp; }

        public String getDeviceStatus() { return deviceStatus; }
        public void setDeviceStatus(String deviceStatus) { this.deviceStatus = deviceStatus; }

        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

        public Boolean getRecordingEnabled() { return recordingEnabled; }
        public void setRecordingEnabled(Boolean recordingEnabled) { this.recordingEnabled = recordingEnabled; }

        public String getPreviewUrl() { return previewUrl; }
        public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
    }

    // 内部类：视频快照
    @Schema(description = "视频快照")
    public static class VideoSnapshotVO {
        @Schema(description = "快照ID", example = "SNAP001")
        private String snapshotId;

        @Schema(description = "快照时间", example = "2025-11-25T10:30:15")
        private LocalDateTime snapshotTime;

        @Schema(description = "快照URL", example = "http://example.com/snapshots/20251125103015.jpg")
        private String snapshotUrl;

        @Schema(description = "快照大小", example = "204800")
        private Long snapshotSize;

        @Schema(description = "快照描述", example = "人员进入画面")
        private String snapshotDescription;

        // Getters and Setters
        public String getSnapshotId() { return snapshotId; }
        public void setSnapshotId(String snapshotId) { this.snapshotId = snapshotId; }

        public LocalDateTime getSnapshotTime() { return snapshotTime; }
        public void setSnapshotTime(LocalDateTime snapshotTime) { this.snapshotTime = snapshotTime; }

        public String getSnapshotUrl() { return snapshotUrl; }
        public void setSnapshotUrl(String snapshotUrl) { this.snapshotUrl = snapshotUrl; }

        public Long getSnapshotSize() { return snapshotSize; }
        public void setSnapshotSize(Long snapshotSize) { this.snapshotSize = snapshotSize; }

        public String getSnapshotDescription() { return snapshotDescription; }
        public void setSnapshotDescription(String snapshotDescription) { this.snapshotDescription = snapshotDescription; }
    }

    // 内部类：联动配置
    @Schema(description = "联动配置")
    public static class LinkageConfigVO {
        @Schema(description = "联动启用", example = "true")
        private Boolean linkageEnabled;

        @Schema(description = "自动录制", example = "true")
        private Boolean autoRecording;

        @Schema(description = "录制质量", example = "HIGH")
        private String recordingQuality;

        @Schema(description = "预触发录制时间", example = "10")
        private Integer preTriggerSeconds;

        @Schema(description = "后触发录制时间", example = "60")
        private Integer postTriggerSeconds;

        @Schema(description = "音频录制", example = "true")
        private Boolean audioEnabled;

        @Schema(description = "云存储同步", example = "true")
        private Boolean cloudSyncEnabled;

        @Schema(description = "实时预览", example = "true")
        private Boolean realtimePreviewEnabled;

        // Getters and Setters
        public Boolean getLinkageEnabled() { return linkageEnabled; }
        public void setLinkageEnabled(Boolean linkageEnabled) { this.linkageEnabled = linkageEnabled; }

        public Boolean getAutoRecording() { return autoRecording; }
        public void setAutoRecording(Boolean autoRecording) { this.autoRecording = autoRecording; }

        public String getRecordingQuality() { return recordingQuality; }
        public void setRecordingQuality(String recordingQuality) { this.recordingQuality = recordingQuality; }

        public Integer getPreTriggerSeconds() { return preTriggerSeconds; }
        public void setPreTriggerSeconds(Integer preTriggerSeconds) { this.preTriggerSeconds = preTriggerSeconds; }

        public Integer getPostTriggerSeconds() { return postTriggerSeconds; }
        public void setPostTriggerSeconds(Integer postTriggerSeconds) { this.postTriggerSeconds = postTriggerSeconds; }

        public Boolean getAudioEnabled() { return audioEnabled; }
        public void setAudioEnabled(Boolean audioEnabled) { this.audioEnabled = audioEnabled; }

        public Boolean getCloudSyncEnabled() { return cloudSyncEnabled; }
        public void setCloudSyncEnabled(Boolean cloudSyncEnabled) { this.cloudSyncEnabled = cloudSyncEnabled; }

        public Boolean getRealtimePreviewEnabled() { return realtimePreviewEnabled; }
        public void setRealtimePreviewEnabled(Boolean realtimePreviewEnabled) { this.realtimePreviewEnabled = realtimePreviewEnabled; }
    }

    // 内部类：联动处理日志
    @Schema(description = "联动处理日志")
    public static class LinkageLogVO {
        @Schema(description = "日志时间", example = "2025-11-25T10:30:01")
        private LocalDateTime logTime;

        @Schema(description = "处理步骤", example = "1")
        private Integer step;

        @Schema(description = "处理动作", example = "启动视频录制")
        private String action;

        @Schema(description = "处理结果", example = "成功")
        private String result;

        @Schema(description = "处理消息", example = "摄像头2001开始录制")
        private String message;

        @Schema(description = "耗时（毫秒）", example = "500")
        private Long durationMs;

        // Getters and Setters
        public LocalDateTime getLogTime() { return logTime; }
        public void setLogTime(LocalDateTime logTime) { this.logTime = logTime; }

        public Integer getStep() { return step; }
        public void setStep(Integer step) { this.step = step; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Long getDurationMs() { return durationMs; }
        public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    }

    // 主要类的Getters and Setters
    public Long getLinkageId() { return linkageId; }
    public void setLinkageId(Long linkageId) { this.linkageId = linkageId; }

    public String getLinkageCode() { return linkageCode; }
    public void setLinkageCode(String linkageCode) { this.linkageCode = linkageCode; }

    public Long getAccessDeviceId() { return accessDeviceId; }
    public void setAccessDeviceId(Long accessDeviceId) { this.accessDeviceId = accessDeviceId; }

    public String getAccessDeviceName() { return accessDeviceName; }
    public void setAccessDeviceName(String accessDeviceName) { this.accessDeviceName = accessDeviceName; }

    public String getAccessDeviceLocation() { return accessDeviceLocation; }
    public void setAccessDeviceLocation(String accessDeviceLocation) { this.accessDeviceLocation = accessDeviceLocation; }

    public String getLinkageType() { return linkageType; }
    public void setLinkageType(String linkageType) { this.linkageType = linkageType; }

    public String getLinkageEvent() { return linkageEvent; }
    public void setLinkageEvent(String linkageEvent) { this.linkageEvent = linkageEvent; }

    public String getTriggerCondition() { return triggerCondition; }
    public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }

    public String getLinkageStatus() { return linkageStatus; }
    public void setLinkageStatus(String linkageStatus) { this.linkageStatus = linkageStatus; }

    public List<VideoDeviceVO> getVideoDevices() { return videoDevices; }
    public void setVideoDevices(List<VideoDeviceVO> videoDevices) { this.videoDevices = videoDevices; }

    public Long getPrimaryVideoDeviceId() { return primaryVideoDeviceId; }
    public void setPrimaryVideoDeviceId(Long primaryVideoDeviceId) { this.primaryVideoDeviceId = primaryVideoDeviceId; }

    public String getPrimaryVideoDeviceName() { return primaryVideoDeviceName; }
    public void setPrimaryVideoDeviceName(String primaryVideoDeviceName) { this.primaryVideoDeviceName = primaryVideoDeviceName; }

    public LocalDateTime getRecordStartTime() { return recordStartTime; }
    public void setRecordStartTime(LocalDateTime recordStartTime) { this.recordStartTime = recordStartTime; }

    public LocalDateTime getRecordEndTime() { return recordEndTime; }
    public void setRecordEndTime(LocalDateTime recordEndTime) { this.recordEndTime = recordEndTime; }

    public Integer getRecordDuration() { return recordDuration; }
    public void setRecordDuration(Integer recordDuration) { this.recordDuration = recordDuration; }

    public String getRecordFilePath() { return recordFilePath; }
    public void setRecordFilePath(String recordFilePath) { this.recordFilePath = recordFilePath; }

    public String getRecordFileUrl() { return recordFileUrl; }
    public void setRecordFileUrl(String recordFileUrl) { this.recordFileUrl = recordFileUrl; }

    public Long getRecordFileSize() { return recordFileSize; }
    public void setRecordFileSize(Long recordFileSize) { this.recordFileSize = recordFileSize; }

    public String getVideoQuality() { return videoQuality; }
    public void setVideoQuality(String videoQuality) { this.videoQuality = videoQuality; }

    public String getVideoEncoding() { return videoEncoding; }
    public void setVideoEncoding(String videoEncoding) { this.videoEncoding = videoEncoding; }

    public Boolean getAudioRecordingEnabled() { return audioRecordingEnabled; }
    public void setAudioRecordingEnabled(Boolean audioRecordingEnabled) { this.audioRecordingEnabled = audioRecordingEnabled; }

    public Integer getPreTriggerRecordingSeconds() { return preTriggerRecordingSeconds; }
    public void setPreTriggerRecordingSeconds(Integer preTriggerRecordingSeconds) { this.preTriggerRecordingSeconds = preTriggerRecordingSeconds; }

    public Integer getPostTriggerRecordingSeconds() { return postTriggerRecordingSeconds; }
    public void setPostTriggerRecordingSeconds(Integer postTriggerRecordingSeconds) { this.postTriggerRecordingSeconds = postTriggerRecordingSeconds; }

    public Long getRelatedPersonId() { return relatedPersonId; }
    public void setRelatedPersonId(Long relatedPersonId) { this.relatedPersonId = relatedPersonId; }

    public String getRelatedPersonName() { return relatedPersonName; }
    public void setRelatedPersonName(String relatedPersonName) { this.relatedPersonName = relatedPersonName; }

    public List<VideoSnapshotVO> getSnapshots() { return snapshots; }
    public void setSnapshots(List<VideoSnapshotVO> snapshots) { this.snapshots = snapshots; }

    public LocalDateTime getTriggerTime() { return triggerTime; }
    public void setTriggerTime(LocalDateTime triggerTime) { this.triggerTime = triggerTime; }

    public LocalDateTime getExecuteStartTime() { return executeStartTime; }
    public void setExecuteStartTime(LocalDateTime executeStartTime) { this.executeStartTime = executeStartTime; }

    public LocalDateTime getExecuteCompleteTime() { return executeCompleteTime; }
    public void setExecuteCompleteTime(LocalDateTime executeCompleteTime) { this.executeCompleteTime = executeCompleteTime; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public Boolean getRealtimePreviewEnabled() { return realtimePreviewEnabled; }
    public void setRealtimePreviewEnabled(Boolean realtimePreviewEnabled) { this.realtimePreviewEnabled = realtimePreviewEnabled; }

    public String getRealtimePreviewUrl() { return realtimePreviewUrl; }
    public void setRealtimePreviewUrl(String realtimePreviewUrl) { this.realtimePreviewUrl = realtimePreviewUrl; }

    public Boolean getCloudStorageSyncEnabled() { return cloudStorageSyncEnabled; }
    public void setCloudStorageSyncEnabled(Boolean cloudStorageSyncEnabled) { this.cloudStorageSyncEnabled = cloudStorageSyncEnabled; }

    public String getCloudStorageUrl() { return cloudStorageUrl; }
    public void setCloudStorageUrl(String cloudStorageUrl) { this.cloudStorageUrl = cloudStorageUrl; }

    public String getBackupStatus() { return backupStatus; }
    public void setBackupStatus(String backupStatus) { this.backupStatus = backupStatus; }

    public LinkageConfigVO getLinkageConfig() { return linkageConfig; }
    public void setLinkageConfig(LinkageConfigVO linkageConfig) { this.linkageConfig = linkageConfig; }

    public List<LinkageLogVO> getProcessLogs() { return processLogs; }
    public void setProcessLogs(List<LinkageLogVO> processLogs) { this.processLogs = processLogs; }

    public Map<String, Object> getAdditionalData() { return additionalData; }
    public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}