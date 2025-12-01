package net.lab1024.sa.access.domain.entity;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 生物识别引擎状态实体
 *
 * 生物识别引擎的状态监控和配置信息
 * 用于管理和监控生物识别系统的运行状态
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BiometricEngineStatusEntity {

    /**
     * 引擎ID
     */
    private Long engineId;

    /**
     * 引擎名称
     */
    private String engineName;

    /**
     * 引擎类型
     */
    private String engineType;

    /**
     * 运行状态
     */
    private String status;

    /**
     * 版本号
     */
    private String version;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 引擎配置
     */
    private String engineConfig;

    /**
     * 性能指标
     */
    private String performanceMetrics;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 启用状态
     */
    private Boolean enabled;

    /**
     * 运行时长（秒）
     */
    private Long uptime;

    /**
     * 内存使用率
     */
    private Double memoryUsageRate;

    /**
     * CPU使用率
     */
    private Double cpuUsageRate;

    /**
     * 连接数
     */
    private Integer connectionCount;

    /**
     * 最大连接数
     */
    private Integer maxConnections;

    /**
     * 最后启动时间
     */
    private LocalDateTime lastStartTime;

    /**
     * 引擎状态枚举
     */
    public enum EngineStatus {
        RUNNING("running", "运行中"),
        STOPPED("stopped", "已停止"),
        ERROR("error", "错误"),
        MAINTENANCE("maintenance", "维护中"),
        STARTING("starting", "启动中"),
        STOPPING("stopping", "停止中");

        private final String value;
        private final String description;

        EngineStatus(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static EngineStatus fromValue(String value) {
            for (EngineStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown engine status: " + value);
        }
    }

    /**
     * 引擎类型枚举
     */
    public enum EngineType {
        FACE_RECOGNITION("face_recognition", "人脸识别"),
        FINGERPRINT("fingerprint", "指纹识别"),
        IRIS("iris", "虹膜识别"),
        VOICE("voice", "语音识别"),
        PALM("palm", "掌纹识别"),
        MULTI_MODAL("multi_modal", "多模态识别");

        private final String value;
        private final String description;

        EngineType(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static EngineType fromValue(String value) {
            for (EngineType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown engine type: " + value);
        }
    }

    /**
     * 检查引擎是否运行中
     */
    public boolean isRunning() {
        return EngineStatus.RUNNING.getValue().equals(status);
    }

    /**
     * 检查引擎是否停止
     */
    public boolean isStopped() {
        return EngineStatus.STOPPED.getValue().equals(status);
    }

    /**
     * 检查引擎是否有错误
     */
    public boolean hasError() {
        return EngineStatus.ERROR.getValue().equals(status);
    }

    /**
     * 检查引擎是否在维护中
     */
    public boolean isMaintenance() {
        return EngineStatus.MAINTENANCE.getValue().equals(status);
    }

    /**
     * 检查引擎是否已启用
     */
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    /**
     * 获取连接使用率
     */
    public Double getConnectionUsageRate() {
        if (connectionCount == null || maxConnections == null || maxConnections == 0) {
            return 0.0;
        }
        return (double) connectionCount / maxConnections * 100;
    }

    /**
     * 检查是否需要扩容
     */
    public boolean needsScalingUp() {
        return getConnectionUsageRate() > 80.0;
    }

    /**
     * 检查是否健康运行
     */
    public boolean isHealthy() {
        return isRunning() && isEnabled() && !hasError() && getConnectionUsageRate() < 90.0;
    }

    /**
     * 更新运行状态
     */
    public void updateRunningStatus() {
        this.status = EngineStatus.RUNNING.getValue();
        this.lastCheckTime = LocalDateTime.now();
    }

    /**
     * 更新错误状态
     */
    public void updateErrorStatus(String error) {
        this.status = EngineStatus.ERROR.getValue();
        this.errorMessage = error;
        this.lastCheckTime = LocalDateTime.now();
    }

    /**
     * 更新停止状态
     */
    public void updateStoppedStatus() {
        this.status = EngineStatus.STOPPED.getValue();
        this.lastCheckTime = LocalDateTime.now();
    }

    /**
     * 更新维护状态
     */
    public void updateMaintenanceStatus() {
        this.status = EngineStatus.MAINTENANCE.getValue();
        this.lastCheckTime = LocalDateTime.now();
    }

    /**
     * 重置性能指标
     */
    public void resetPerformanceMetrics() {
        this.uptime = 0L;
        this.memoryUsageRate = 0.0;
        this.cpuUsageRate = 0.0;
        this.connectionCount = 0;
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        try {
            EngineStatus engineStatus = EngineStatus.fromValue(status);
            return engineStatus.getDescription();
        } catch (Exception e) {
            return "未知状态";
        }
    }

    /**
     * 获取类型描述
     */
    public String getTypeDescription() {
        try {
            EngineType type = EngineType.fromValue(this.engineType);
            return type.getDescription();
        } catch (Exception e) {
            return "未知类型";
        }
    }
}