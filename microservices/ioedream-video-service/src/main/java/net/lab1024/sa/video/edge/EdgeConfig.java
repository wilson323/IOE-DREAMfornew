package net.lab1024.sa.video.edge;

/**
 * 边缘计算配置
 * <p>
 * 说明：
 * - 该配置用于视频边缘计算模块的最小运行参数承载
 * - 为保证编译与后续可演进性，提供显式 Getter/Setter（避免 Lombok 未生效）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class EdgeConfig {

    /**
     * 最大并发任务数
     */
    private Integer maxConcurrentTasks = 8;

    /**
     * 推理最大耗时（毫秒）
     */
    private Long maxInferenceTime = 3000L;

    /**
     * 边缘推理超时时间（毫秒）
     */
    private Long edgeInferenceTimeout = 5000L;

    /**
     * 云边协同阈值（例如：置信度/复杂度阈值）
     */
    private Double cloudCollaborationThreshold = 0.85;

    public Integer getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

    public void setMaxConcurrentTasks(Integer maxConcurrentTasks) {
        this.maxConcurrentTasks = maxConcurrentTasks;
    }

    public Long getMaxInferenceTime() {
        return maxInferenceTime;
    }

    public void setMaxInferenceTime(Long maxInferenceTime) {
        this.maxInferenceTime = maxInferenceTime;
    }

    public Long getEdgeInferenceTimeout() {
        return edgeInferenceTimeout;
    }

    public void setEdgeInferenceTimeout(Long edgeInferenceTimeout) {
        this.edgeInferenceTimeout = edgeInferenceTimeout;
    }

    public Double getCloudCollaborationThreshold() {
        return cloudCollaborationThreshold;
    }

    public void setCloudCollaborationThreshold(Double cloudCollaborationThreshold) {
        this.cloudCollaborationThreshold = cloudCollaborationThreshold;
    }

    /**
     * 模型同步是否启用
     */
    private Boolean modelSyncEnabled = true;

    /**
     * 模型同步超时时间（毫秒）
     */
    private Long modelSyncTimeout = 30000L;

    /**
     * 心跳检测间隔（毫秒）
     */
    private Long heartbeatInterval = 10000L;

    /**
     * 连接重试次数
     */
    private Integer connectionRetryCount = 3;

    /**
     * 连接重试间隔（毫秒）
     */
    private Long connectionRetryInterval = 5000L;

    public Boolean getModelSyncEnabled() {
        return modelSyncEnabled;
    }

    public void setModelSyncEnabled(Boolean modelSyncEnabled) {
        this.modelSyncEnabled = modelSyncEnabled;
    }

    public Long getModelSyncTimeout() {
        return modelSyncTimeout;
    }

    public void setModelSyncTimeout(Long modelSyncTimeout) {
        this.modelSyncTimeout = modelSyncTimeout;
    }

    public Long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public Integer getConnectionRetryCount() {
        return connectionRetryCount;
    }

    public void setConnectionRetryCount(Integer connectionRetryCount) {
        this.connectionRetryCount = connectionRetryCount;
    }

    public Long getConnectionRetryInterval() {
        return connectionRetryInterval;
    }

    public void setConnectionRetryInterval(Long connectionRetryInterval) {
        this.connectionRetryInterval = connectionRetryInterval;
    }
}
