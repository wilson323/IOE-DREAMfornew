package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.organization.manager.SpaceCapacityManager;

import java.util.List;

/**
 * 空间容量分析服务接口
 * 提供区域空间容量分析和优化建议功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
public interface SpaceCapacityService {

    /**
     * 分析区域空间容量
     *
     * @param areaId 区域ID
     * @return 空间容量分析结果
     */
    SpaceCapacityManager.SpaceCapacityAnalysis analyzeSpaceCapacity(Long areaId);

    /**
     * 批量分析多个区域的空间容量
     *
     * @param areaIds 区域ID列表
     * @return 空间容量分析结果列表
     */
    List<SpaceCapacityManager.SpaceCapacityAnalysis> batchAnalyzeSpaceCapacity(List<Long> areaIds);

    /**
     * 获取区域空间容量对比分析
     *
     * @param parentAreaId 父区域ID
     * @return 容量对比分析
     */
    SpaceCapacityManager.SpaceCapacityComparison getSpaceCapacityComparison(Long parentAreaId);

    /**
     * 生成空间容量报告
     *
     * @param areaId 区域ID
     * @return 空间容量报告
     */
    SpaceCapacityManager.SpaceCapacityReport generateSpaceCapacityReport(Long areaId);

    /**
     * 获取区域容量优化建议
     *
     * @param areaId 区域ID
     * @return 优化建议列表
     */
    List<String> getCapacityOptimizationSuggestions(Long areaId);

    /**
     * 获取区域容量历史趋势
     *
     * @param areaId 区域ID
     * @param months 历史月份数
     * @return 容量趋势数据
     */
    List<SpaceCapacityManager.CapacityTrend> getCapacityTrends(Long areaId, Integer months);

    /**
     * 预测区域未来容量需求
     *
     * @param areaId 区域ID
     * @param forecastMonths 预测月份数
     * @return 预测结果
     */
    CapacityForecastResult forecastCapacityDemand(Long areaId, Integer forecastMonths);

    /**
     * 容量预测结果
     */
    class CapacityForecastResult {
        private Long areaId;
        private String areaName;
        private List<CapacityForecast> forecasts;
        private double averageGrowthRate;
        private List<String> recommendations;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public List<CapacityForecast> getForecasts() { return forecasts; }
        public void setForecasts(List<CapacityForecast> forecasts) { this.forecasts = forecasts; }
        public double getAverageGrowthRate() { return averageGrowthRate; }
        public void setAverageGrowthRate(double averageGrowthRate) { this.averageGrowthRate = averageGrowthRate; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    /**
     * 容量预测数据
     */
    class CapacityForecast {
        private String period;
        private int predictedDeviceCount;
        private double predictedUtilizationRate;
        private Integer recommendedDeviceCount;
        private String riskLevel;

        // getters and setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public int getPredictedDeviceCount() { return predictedDeviceCount; }
        public void setPredictedDeviceCount(int predictedDeviceCount) { this.predictedDeviceCount = predictedDeviceCount; }
        public double getPredictedUtilizationRate() { return predictedUtilizationRate; }
        public void setPredictedUtilizationRate(double predictedUtilizationRate) { this.predictedUtilizationRate = predictedUtilizationRate; }
        public Integer getRecommendedDeviceCount() { return recommendedDeviceCount; }
        public void setRecommendedDeviceCount(Integer recommendedDeviceCount) { this.recommendedDeviceCount = recommendedDeviceCount; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }
}