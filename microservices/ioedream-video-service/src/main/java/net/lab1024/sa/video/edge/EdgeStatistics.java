package net.lab1024.sa.video.edge;

import java.time.LocalDateTime;

/**
 * 边缘统计信息
 * <p>
 * 用于聚合边缘设备与推理指标（最小字段集，满足编译与接口返回需要）。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class EdgeStatistics {

    private Integer totalDevices;
    private Integer readyDevices;
    private Integer totalInferences;
    private Double averageInferenceTime;
    private LocalDateTime lastUpdateTime;

    public Integer getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(Integer totalDevices) {
        this.totalDevices = totalDevices;
    }

    public Integer getReadyDevices() {
        return readyDevices;
    }

    public void setReadyDevices(Integer readyDevices) {
        this.readyDevices = readyDevices;
    }

    public Integer getTotalInferences() {
        return totalInferences;
    }

    public void setTotalInferences(Integer totalInferences) {
        this.totalInferences = totalInferences;
    }

    public Double getAverageInferenceTime() {
        return averageInferenceTime;
    }

    public void setAverageInferenceTime(Double averageInferenceTime) {
        this.averageInferenceTime = averageInferenceTime;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}

