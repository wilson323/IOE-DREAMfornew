package net.lab1024.sa.common.business.video.manager;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 设备模型同步Manager
 * <p>
 * 提供设备模型同步的业务逻辑：
 * 1. 同步任务ID生成
 * 2. 同步状态管理
 * 3. 同步进度计算
 * </p>
 * <p>
 * 注意：这是纯Java类，提供业务逻辑辅助方法。
 * 数据访问操作由Service层通过DAO完成。
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @since 2025-01-30
 */
@Slf4j
public class DeviceModelSyncManager {

    /**
     * 生成同步任务ID
     *
     * @return 任务ID
     */
    public String generateSyncTaskId() {
        String taskId = "sync_" + UUID.randomUUID().toString().substring(0, 8);
        log.debug("[设备模型同步Manager] 生成同步任务ID: taskId={}", taskId);
        return taskId;
    }

    /**
     * 验证同步状态
     *
     * @param syncStatus 同步状态
     * @return 是否有效
     */
    public boolean validateSyncStatus(Integer syncStatus) {
        if (syncStatus == null) {
            return false;
        }

        // 状态定义：0-待同步 1-同步中 2-成功 3-失败
        boolean valid = syncStatus >= 0 && syncStatus <= 3;

        if (!valid) {
            log.warn("[设备模型同步Manager] 同步状态无效: status={}", syncStatus);
        }

        return valid;
    }

    /**
     * 获取同步状态名称
     *
     * @param syncStatus 同步状态
     * @return 状态名称
     */
    public String getSyncStatusName(Integer syncStatus) {
        if (syncStatus == null) {
            return "未知";
        }

        return switch (syncStatus) {
            case 0 -> "待同步";
            case 1 -> "同步中";
            case 2 -> "成功";
            case 3 -> "失败";
            default -> "未知";
        };
    }

    /**
     * 计算同步进度百分比
     *
     * @param totalDevices    总设备数
     * @param completedDevices 已完成设备数
     * @return 进度百分比（0-100）
     */
    public Integer calculateProgress(Integer totalDevices, Integer completedDevices) {
        if (totalDevices == null || totalDevices == 0) {
            return 0;
        }

        Integer actualCompleted = completedDevices != null ? completedDevices : 0;
        int progress = actualCompleted * 100 / totalDevices;
        log.debug("[设备模型同步Manager] 计算同步进度: total={}, completed={}, progress={}%",
                totalDevices, actualCompleted, progress);

        return progress;
    }

    /**
     * 同步进度数据结构
     */
    public static class SyncProgress {
        private Integer totalDevices;
        private Integer pendingDevices;
        private Integer syncingDevices;
        private Integer successDevices;
        private Integer failedDevices;
        private Integer progress;

        public Integer getTotalDevices() {
            return totalDevices;
        }

        public void setTotalDevices(Integer totalDevices) {
            this.totalDevices = totalDevices;
        }

        public Integer getPendingDevices() {
            return pendingDevices;
        }

        public void setPendingDevices(Integer pendingDevices) {
            this.pendingDevices = pendingDevices;
        }

        public Integer getSyncingDevices() {
            return syncingDevices;
        }

        public void setSyncingDevices(Integer syncingDevices) {
            this.syncingDevices = syncingDevices;
        }

        public Integer getSuccessDevices() {
            return successDevices;
        }

        public void setSuccessDevices(Integer successDevices) {
            this.successDevices = successDevices;
        }

        public Integer getFailedDevices() {
            return failedDevices;
        }

        public void setFailedDevices(Integer failedDevices) {
            this.failedDevices = failedDevices;
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }
    }
}
