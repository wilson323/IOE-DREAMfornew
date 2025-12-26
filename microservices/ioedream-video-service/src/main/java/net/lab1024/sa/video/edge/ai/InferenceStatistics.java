package net.lab1024.sa.video.edge.ai;

/**
 * 推理统计信息
 * <p>
 * 用于对外暴露边缘推理运行态指标（最小字段集，满足编译）。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class InferenceStatistics {

    private int totalInferences;
    private double averageInferenceTime;
    private int maxConcurrency;
    private int currentConcurrency;
    private int loadedModels;

    public int getTotalInferences() {
        return totalInferences;
    }

    public void setTotalInferences(int totalInferences) {
        this.totalInferences = totalInferences;
    }

    public double getAverageInferenceTime() {
        return averageInferenceTime;
    }

    public void setAverageInferenceTime(double averageInferenceTime) {
        this.averageInferenceTime = averageInferenceTime;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }

    public int getCurrentConcurrency() {
        return currentConcurrency;
    }

    public void setCurrentConcurrency(int currentConcurrency) {
        this.currentConcurrency = currentConcurrency;
    }

    public int getLoadedModels() {
        return loadedModels;
    }

    public void setLoadedModels(int loadedModels) {
        this.loadedModels = loadedModels;
    }
}

