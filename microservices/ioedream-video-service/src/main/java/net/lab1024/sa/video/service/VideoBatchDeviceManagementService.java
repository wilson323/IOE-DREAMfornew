package net.lab1024.sa.video.service;

import java.util.List;

/**
 * 视频批量设备管理服务
 * <p>
 * 核心功能：
 * 1. 批量配置（批量参数设置）
 * 2. 批量升级（固件远程升级）
 * 3. 批量重启（设备远程重启）
 * 4. 批量巡检（设备状态检查）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface VideoBatchDeviceManagementService {

    /**
     * 批量配置设备
     * <p>
     * 支持批量设置设备参数：
     * - 分辨率、帧率、码率
     * - AI检测开关
     * - 录像计划
     * - ROI区域
     * </p>
     *
     * @param deviceIds 设备ID列表
     * @param configType 配置类型（resolution/frameRate/bitrate/aiDetection/recordingPlan/roi）
     * @param configValue 配置值
     * @return 批量配置结果
     */
    BatchConfigurationResult batchConfigureDevices(List<String> deviceIds, String configType, Object configValue);

    /**
     * 批量升级设备固件
     * <p>
     * 支持远程固件升级：
     * - 固件版本检查
     * - 固件下载
     * - 固件安装
     * - 升级进度跟踪
     * - 失败回滚
     * </p>
     *
     * @param deviceIds 设备ID列表
     * @param firmwareUrl 固件URL
     * @param firmwareVersion 固件版本
     * @return 批量升级结果
     */
    BatchFirmwareUpgradeResult batchUpgradeFirmware(List<String> deviceIds, String firmwareUrl, String firmwareVersion);

    /**
     * 批量重启设备
     * <p>
     * 支持远程设备重启：
     * - 优雅重启（等待当前录像完成）
     * - 强制重启
     * - 重启进度跟踪
     * - 重启后状态验证
     * </p>
     *
     * @param deviceIds 设备ID列表
     * @param force 是否强制重启
     * @return 批量重启结果
     */
    BatchRebootResult batchRebootDevices(List<String> deviceIds, boolean force);

    /**
     * 批量巡检设备状态
     * <p>
     * 支持批量设备健康检查：
     * - 在线状态
     * - 录像状态
     * - 存储状态
     * - 网络状态
     * - AI检测状态
     * - 固件版本
     * </p>
     *
     * @param deviceIds 设备ID列表
     * @param checkItems 检查项列表（online/recording/storage/network/aiDetection/firmwareVersion）
     * @return 批量巡检结果
     */
    BatchInspectionResult batchInspectDevices(List<String> deviceIds, List<String> checkItems);

    /**
     * 批量配置结果
     */
    class BatchConfigurationResult {
        private int totalDevices;
        private int successCount;
        private int failureCount;
        private List<String> successDeviceIds;
        private List<DeviceOperationFailure> failures;
        private long durationMs;

        // Getters and Setters
        public int getTotalDevices() { return totalDevices; }
        public void setTotalDevices(int totalDevices) { this.totalDevices = totalDevices; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        public List<String> getSuccessDeviceIds() { return successDeviceIds; }
        public void setSuccessDeviceIds(List<String> successDeviceIds) { this.successDeviceIds = successDeviceIds; }
        public List<DeviceOperationFailure> getFailures() { return failures; }
        public void setFailures(List<DeviceOperationFailure> failures) { this.failures = failures; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    }

    /**
     * 批量固件升级结果
     */
    class BatchFirmwareUpgradeResult {
        private int totalDevices;
        private int successCount;
        private int failureCount;
        private int inProgressCount;
        private List<String> successDeviceIds;
        private List<String> inProgressDeviceIds;
        private List<DeviceOperationFailure> failures;
        private String taskId;
        private long durationMs;

        // Getters and Setters
        public int getTotalDevices() { return totalDevices; }
        public void setTotalDevices(int totalDevices) { this.totalDevices = totalDevices; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        public int getInProgressCount() { return inProgressCount; }
        public void setInProgressCount(int inProgressCount) { this.inProgressCount = inProgressCount; }
        public List<String> getSuccessDeviceIds() { return successDeviceIds; }
        public void setSuccessDeviceIds(List<String> successDeviceIds) { this.successDeviceIds = successDeviceIds; }
        public List<String> getInProgressDeviceIds() { return inProgressDeviceIds; }
        public void setInProgressDeviceIds(List<String> inProgressDeviceIds) { this.inProgressDeviceIds = inProgressDeviceIds; }
        public List<DeviceOperationFailure> getFailures() { return failures; }
        public void setFailures(List<DeviceOperationFailure> failures) { this.failures = failures; }
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    }

    /**
     * 批量重启结果
     */
    class BatchRebootResult {
        private int totalDevices;
        private int successCount;
        private int failureCount;
        private List<String> successDeviceIds;
        private List<DeviceOperationFailure> failures;
        private long durationMs;

        // Getters and Setters
        public int getTotalDevices() { return totalDevices; }
        public void setTotalDevices(int totalDevices) { this.totalDevices = totalDevices; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        public List<String> getSuccessDeviceIds() { return successDeviceIds; }
        public void setSuccessDeviceIds(List<String> successDeviceIds) { this.successDeviceIds = successDeviceIds; }
        public List<DeviceOperationFailure> getFailures() { return failures; }
        public void setFailures(List<DeviceOperationFailure> failures) { this.failures = failures; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    }

    /**
     * 批量巡检结果
     */
    class BatchInspectionResult {
        private int totalDevices;
        private int onlineCount;
        private int offlineCount;
        private int healthyCount;
        private int warningCount;
        private int errorCount;
        private List<DeviceInspectionResult> inspectionResults;
        private long durationMs;

        // Getters and Setters
        public int getTotalDevices() { return totalDevices; }
        public void setTotalDevices(int totalDevices) { this.totalDevices = totalDevices; }
        public int getOnlineCount() { return onlineCount; }
        public void setOnlineCount(int onlineCount) { this.onlineCount = onlineCount; }
        public int getOfflineCount() { return offlineCount; }
        public void setOfflineCount(int offlineCount) { this.offlineCount = offlineCount; }
        public int getHealthyCount() { return healthyCount; }
        public void setHealthyCount(int healthyCount) { this.healthyCount = healthyCount; }
        public int getWarningCount() { return warningCount; }
        public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        public List<DeviceInspectionResult> getInspectionResults() { return inspectionResults; }
        public void setInspectionResults(List<DeviceInspectionResult> inspectionResults) { this.inspectionResults = inspectionResults; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    }

    /**
     * 设备操作失败信息
     */
    class DeviceOperationFailure {
        private String deviceId;
        private String operation;
        private String errorCode;
        private String errorMessage;

        // Getters and Setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * 设备巡检结果
     */
    class DeviceInspectionResult {
        private String deviceId;
        private String deviceName;
        private boolean online;
        private boolean recording;
        private boolean storageOk;
        private boolean networkOk;
        private boolean aiDetectionOk;
        private String firmwareVersion;
        private String status; // healthy/warning/error
        private List<String> issues;

        // Getters and Setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
        public boolean isRecording() { return recording; }
        public void setRecording(boolean recording) { this.recording = recording; }
        public boolean isStorageOk() { return storageOk; }
        public void setStorageOk(boolean storageOk) { this.storageOk = storageOk; }
        public boolean isNetworkOk() { return networkOk; }
        public void setNetworkOk(boolean networkOk) { this.networkOk = networkOk; }
        public boolean isAiDetectionOk() { return aiDetectionOk; }
        public void setAiDetectionOk(boolean aiDetectionOk) { this.aiDetectionOk = aiDetectionOk; }
        public String getFirmwareVersion() { return firmwareVersion; }
        public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<String> getIssues() { return issues; }
        public void setIssues(List<String> issues) { this.issues = issues; }
    }
}
