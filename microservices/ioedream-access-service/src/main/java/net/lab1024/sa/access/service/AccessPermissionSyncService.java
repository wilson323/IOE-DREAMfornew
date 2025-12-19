package net.lab1024.sa.access.service;

import net.lab1024.sa.common.dto.ResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁权限同步服务接口
 * <p>
 * 负责权限数据同步到设备，包括：
 * 1. 权限新增时同步到设备
 * 2. 权限删除时从设备移除
 * 3. 权限更新时同步更新设备
 * 4. 同步状态跟踪和重试机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessPermissionSyncService {

    /**
     * 同步权限到设备
     * <p>
     * 当用户新增区域权限时，将权限数据同步到该区域的所有门禁设备
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 同步结果
     */
    ResponseDTO<PermissionSyncResult> syncPermissionToDevices(Long userId, Long areaId);

    /**
     * 从设备移除权限
     * <p>
     * 当用户移除区域权限时，从该区域的所有门禁设备删除权限数据
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 同步结果
     */
    ResponseDTO<PermissionSyncResult> removePermissionFromDevices(Long userId, Long areaId);

    /**
     * 批量同步权限到设备
     * <p>
     * 批量将多个用户的权限同步到设备
     * </p>
     *
     * @param userIds 用户ID列表
     * @param areaId 区域ID
     * @return 同步结果
     */
    ResponseDTO<PermissionSyncResult> batchSyncPermissionToDevices(List<Long> userIds, Long areaId);

    /**
     * 查询权限同步状态
     * <p>
     * 查询指定用户和区域的权限同步状态
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 同步状态信息
     */
    ResponseDTO<PermissionSyncStatus> getSyncStatus(Long userId, Long areaId);

    /**
     * 重试失败的同步任务
     * <p>
     * 对于同步失败的权限，进行重试
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 同步结果
     */
    ResponseDTO<PermissionSyncResult> retrySync(Long userId, Long areaId);

    /**
     * 权限同步结果
     */
    class PermissionSyncResult {
        private Integer totalCount;
        private Integer successCount;
        private Integer failCount;
        private List<DeviceSyncRecord> syncRecords;

        // Getters and Setters
        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Integer successCount) {
            this.successCount = successCount;
        }

        public Integer getFailCount() {
            return failCount;
        }

        public void setFailCount(Integer failCount) {
            this.failCount = failCount;
        }

        public List<DeviceSyncRecord> getSyncRecords() {
            return syncRecords;
        }

        public void setSyncRecords(List<DeviceSyncRecord> syncRecords) {
            this.syncRecords = syncRecords;
        }
    }

    /**
     * 设备同步记录
     */
    class DeviceSyncRecord {
        private String deviceId;
        private String deviceName;
        private Boolean success;
        private String errorMessage;
        private Long syncTime;

        // Getters and Setters
        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Long getSyncTime() {
            return syncTime;
        }

        public void setSyncTime(Long syncTime) {
            this.syncTime = syncTime;
        }
    }

    /**
     * 权限同步状态
     */
    class PermissionSyncStatus {
        private Long userId;
        private Long areaId;
        private Integer syncStatus; // 0-未同步 1-同步中 2-同步成功 3-同步失败
        private LocalDateTime lastSyncTime;
        private Integer retryCount;
        private String lastErrorMessage;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getAreaId() {
            return areaId;
        }

        public void setAreaId(Long areaId) {
            this.areaId = areaId;
        }

        public Integer getSyncStatus() {
            return syncStatus;
        }

        public void setSyncStatus(Integer syncStatus) {
            this.syncStatus = syncStatus;
        }

        public LocalDateTime getLastSyncTime() {
            return lastSyncTime;
        }

        public void setLastSyncTime(LocalDateTime lastSyncTime) {
            this.lastSyncTime = lastSyncTime;
        }

        public Integer getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(Integer retryCount) {
            this.retryCount = retryCount;
        }

        public String getLastErrorMessage() {
            return lastErrorMessage;
        }

        public void setLastErrorMessage(String lastErrorMessage) {
            this.lastErrorMessage = lastErrorMessage;
        }
    }
}
