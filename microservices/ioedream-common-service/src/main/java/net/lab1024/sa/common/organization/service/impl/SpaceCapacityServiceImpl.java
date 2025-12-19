package net.lab1024.sa.common.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import net.lab1024.sa.common.organization.manager.SpaceCapacityManager;
import net.lab1024.sa.common.organization.service.SpaceCapacityService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 空间容量分析服务实现
 * 提供区域空间容量分析和优化建议功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceCapacityServiceImpl implements SpaceCapacityService {

    private final SpaceCapacityManager spaceCapacityManager;

    @Override
    public SpaceCapacityManager.SpaceCapacityAnalysis analyzeSpaceCapacity(Long areaId) {
        log.info("[空间容量服务] 分析区域空间容量: areaId={}", areaId);

        try {
            return spaceCapacityManager.analyzeSpaceCapacity(areaId);
        } catch (Exception e) {
            log.error("分析区域空间容量失败, areaId={}", areaId, e);
            throw e;
        }
    }

    @Override
    public List<SpaceCapacityManager.SpaceCapacityAnalysis> batchAnalyzeSpaceCapacity(List<Long> areaIds) {
        log.info("[空间容量服务] 批量分析区域空间容量: count={}", areaIds.size());

        try {
            return spaceCapacityManager.batchAnalyzeSpaceCapacity(areaIds);
        } catch (Exception e) {
            log.error("批量分析区域空间容量失败, areaIds={}", areaIds, e);
            throw e;
        }
    }

    @Override
    public SpaceCapacityManager.SpaceCapacityComparison getSpaceCapacityComparison(Long parentAreaId) {
        log.info("[空间容量服务] 获取区域空间容量对比: parentAreaId={}", parentAreaId);

        try {
            return spaceCapacityManager.getSpaceCapacityComparison(parentAreaId);
        } catch (Exception e) {
            log.error("获取区域空间容量对比失败, parentAreaId={}", parentAreaId, e);
            throw e;
        }
    }

    @Override
    public SpaceCapacityManager.SpaceCapacityReport generateSpaceCapacityReport(Long areaId) {
        log.info("[空间容量服务] 生成空间容量报告: areaId={}", areaId);

        try {
            return spaceCapacityManager.generateSpaceCapacityReport(areaId);
        } catch (Exception e) {
            log.error("生成空间容量报告失败, areaId={}", areaId, e);
            throw e;
        }
    }

    @Override
    public List<String> getCapacityOptimizationSuggestions(Long areaId) {
        log.debug("[空间容量服务] 获取区域容量优化建议: areaId={}", areaId);

        try {
            SpaceCapacityManager.SpaceCapacityAnalysis analysis = analyzeSpaceCapacity(areaId);
            return analysis.getRecommendations();
        } catch (Exception e) {
            log.error("获取区域容量优化建议失败, areaId={}", areaId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<SpaceCapacityManager.CapacityTrend> getCapacityTrends(Long areaId, Integer months) {
        log.info("[空间容量服务] 获取区域容量历史趋势: areaId={}, months={}", areaId, months);

        try {
            // 获取当前容量分析
            SpaceCapacityManager.SpaceCapacityAnalysis analysis = analyzeSpaceCapacity(areaId);
            List<SpaceCapacityManager.CapacityTrend> allTrends = analysis.getTrends();

            // 根据请求的月份数筛选数据
            if (months == null || months <= 0 || months >= allTrends.size()) {
                return allTrends;
            }

            return allTrends.stream()
                    .limit(months)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取区域容量历史趋势失败, areaId={}, months={}", areaId, months, e);
            return new ArrayList<>();
        }
    }

    @Override
    public CapacityForecastResult forecastCapacityDemand(Long areaId, Integer forecastMonths) {
        log.info("[空间容量服务] 预测区域未来容量需求: areaId={}, forecastMonths={}", areaId, forecastMonths);

        try {
            CapacityForecastResult result = new CapacityForecastResult();
            result.setAreaId(areaId);

            // 获取当前容量分析
            SpaceCapacityManager.SpaceCapacityAnalysis currentAnalysis = analyzeSpaceCapacity(areaId);
            result.setAreaName(currentAnalysis.getAreaName());

            // 获取历史趋势数据
            List<SpaceCapacityManager.CapacityTrend> historicalTrends = getCapacityTrends(areaId, 6);
            List<CapacityForecast> forecasts = new ArrayList<>();

            if (historicalTrends.size() < 3) {
                // 历史数据不足，使用简单预测
                generateSimpleForecast(currentAnalysis, forecastMonths, forecasts);
                result.setAverageGrowthRate(0.05); // 默认5%增长率
            } else {
                // 基于历史数据进行预测
                generateAdvancedForecast(historicalTrends, currentAnalysis, forecastMonths, forecasts);
                result.setAverageGrowthRate(calculateGrowthRate(historicalTrends));
            }

            result.setForecasts(forecasts);

            // 生成预测建议
            List<String> recommendations = generateForecastRecommendations(forecasts);
            result.setRecommendations(recommendations);

            log.info("[空间容量服务] 完成区域未来容量需求预测: areaId={}, forecastCount={}",
                    areaId, forecasts.size());

            return result;
        } catch (Exception e) {
            log.error("预测区域未来容量需求失败, areaId={}, forecastMonths={}", areaId, forecastMonths, e);
            throw e;
        }
    }

    /**
     * 生成简单预测
     */
    private void generateSimpleForecast(SpaceCapacityManager.SpaceCapacityAnalysis currentAnalysis,
                                         Integer forecastMonths,
                                         List<CapacityForecast> forecasts) {
        int currentDeviceCount = currentAnalysis.getDeviceCount();
        double currentUtilization = currentAnalysis.getUtilizationRate();

        for (int i = 1; i <= forecastMonths; i++) {
            CapacityForecast forecast = new CapacityForecast();

            // 计算预测月份
            LocalDateTime forecastDate = LocalDateTime.now().plusMonths(i);
            forecast.setPeriod(forecastDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));

            // 简单线性增长预测
            double growthRate = 0.03 + (Math.random() * 0.04); // 3%-7%的月增长率
            int predictedDeviceCount = (int) (currentDeviceCount * Math.pow(1 + growthRate, i));
            double predictedUtilization = Math.min(currentUtilization * (1 + growthRate * i * 0.5), 1.0);

            forecast.setPredictedDeviceCount(predictedDeviceCount);
            forecast.setPredictedUtilizationRate(predictedUtilization);
            forecast.setRecommendedDeviceCount(calculateRecommendedDeviceCount(predictedUtilization));
            forecast.setRiskLevel(determineRiskLevel(predictedUtilization));

            forecasts.add(forecast);
        }
    }

    /**
     * 生成高级预测
     */
    private void generateAdvancedForecast(List<SpaceCapacityManager.CapacityTrend> historicalTrends,
                                          SpaceCapacityManager.SpaceCapacityAnalysis currentAnalysis,
                                          Integer forecastMonths,
                                          List<CapacityForecast> forecasts) {
        // 计算历史增长率
        double avgGrowthRate = calculateGrowthRate(historicalTrends);

        for (int i = 1; i <= forecastMonths; i++) {
            CapacityForecast forecast = new CapacityForecast();

            // 计算预测月份
            LocalDateTime forecastDate = LocalDateTime.now().plusMonths(i);
            forecast.setPeriod(forecastDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));

            // 基于历史趋势预测
            int currentDeviceCount = currentAnalysis.getDeviceCount();
            double predictedDeviceCount = currentDeviceCount * Math.pow(1 + avgGrowthRate, i);

            // 考虑季节性因素
            double seasonalFactor = calculateSeasonalFactor(forecastDate.getMonthValue());
            predictedDeviceCount *= seasonalFactor;

            double predictedUtilization = Math.min(
                    currentAnalysis.getUtilizationRate() * (predictedDeviceCount / currentDeviceCount),
                    1.0
            );

            forecast.setPredictedDeviceCount((int) predictedDeviceCount);
            forecast.setPredictedUtilizationRate(predictedUtilization);
            forecast.setRecommendedDeviceCount(calculateRecommendedDeviceCount(predictedUtilization));
            forecast.setRiskLevel(determineRiskLevel(predictedUtilization));

            forecasts.add(forecast);
        }
    }

    /**
     * 计算增长率
     */
    private double calculateGrowthRate(List<SpaceCapacityManager.CapacityTrend> trends) {
        if (trends.size() < 2) {
            return 0.05; // 默认5%增长率
        }

        double totalGrowthRate = 0.0;
        int count = 0;

        for (int i = 1; i < trends.size(); i++) {
            double prevValue = trends.get(i - 1).getDeviceCount();
            double currValue = trends.get(i).getDeviceCount();

            if (prevValue > 0) {
                totalGrowthRate += (currValue - prevValue) / prevValue;
                count++;
            }
        }

        return count > 0 ? totalGrowthRate / count : 0.05;
    }

    /**
     * 计算季节性因子
     */
    private double calculateSeasonalFactor(int month) {
        // 简单的季节性调整模型
        return switch (month) {
            case 1, 2 -> 0.9;  // 春节前后，利用率较低
            case 3, 4, 5 -> 1.0; // 春季正常
            case 6, 7, 8 -> 1.1; // 夏季较高
            case 9, 10 -> 1.05; // 秋季略高
            case 11, 12 -> 1.02; // 年末较高
            default -> 1.0;
        };
    }

    /**
     * 计算推荐设备数量
     */
    private Integer calculateRecommendedDeviceCount(double utilizationRate) {
        if (utilizationRate < 0.6) {
            return null; // 不需要调整
        } else if (utilizationRate < 0.8) {
            return null; // 合理范围内
        } else {
            // 超过80%建议增加空间或优化
            return (int) (100 / utilizationRate); // 目标利用率控制在100%
        }
    }

    /**
     * 确定风险等级
     */
    private String determineRiskLevel(double utilizationRate) {
        if (utilizationRate < 0.7) {
            return "低风险";
        } else if (utilizationRate < 0.85) {
            return "中风险";
        } else if (utilizationRate < 0.95) {
            return "高风险";
        } else {
            return "极高风险";
        }
    }

    /**
     * 生成预测建议
     */
    private List<String> generateForecastRecommendations(List<CapacityForecast> forecasts) {
        List<String> recommendations = new ArrayList<>();

        if (forecasts.isEmpty()) {
            return recommendations;
        }

        // 分析预测趋势
        double finalUtilization = forecasts.get(forecasts.size() - 1).getPredictedUtilizationRate();
        String finalRiskLevel = forecasts.get(forecasts.size() - 1).getRiskLevel();

        if ("极高风险".equals(finalRiskLevel)) {
            recommendations.add("预测显示区域将严重超负荷，建议立即制定空间扩容计划");
            recommendations.add("考虑启用远程工作或多班次制度以缓解空间压力");
        } else if ("高风险".equals(finalRiskLevel)) {
            recommendations.add("预测显示区域将面临高负载，建议提前规划空间扩容");
            recommendations.add("优化现有空间布局以提高利用效率");
        } else if ("中风险".equals(finalRiskLevel)) {
            recommendations.add("预测显示区域负载将增加，建议制定应急预案");
            recommendations.add("定期监控空间使用情况，及时调整配置");
        }

        // 分析增长趋势
        double avgGrowthRate = forecasts.stream()
                .mapToDouble(f -> f.getPredictedDeviceCount())
                .average()
                .orElse(0.0);

        if (avgGrowthRate > 100) {
            recommendations.add("设备数量增长较快，建议评估长期空间规划");
        }

        return recommendations;
    }
}
