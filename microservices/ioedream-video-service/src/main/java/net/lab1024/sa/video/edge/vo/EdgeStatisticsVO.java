package net.lab1024.sa.video.edge.vo;

import java.time.LocalDateTime;

/**
 * 边缘统计VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class EdgeStatisticsVO {

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


