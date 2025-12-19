package net.lab1024.sa.common.organization.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 空间容量分析管理器
 * 负责分析区域空间容量、利用率和规划建议
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RequiredArgsConstructor
public class SpaceCapacityManager {

    private final AreaDao areaDao;
    private final AreaDeviceDao areaDeviceDao;

    // 区域类型常量
    public static class AreaType {
        public static final int CAMPUS = 1;     // 园区
        public static final int BUILDING = 2;    // 建筑
        public static final int FLOOR = 3;       // 楼层
        public static final int AREA = 4;        // 区域
        public static final int ROOM = 5;        // 房间
    }

    // 容量状态常量
    public static class CapacityStatus {
        public static final int LOW_USAGE = 1;      // 低利用率 (<30%)
        public static final int NORMAL_USAGE = 2;   // 正常利用率 (30%-80%)
        public static final int HIGH_USAGE = 3;     // 高利用率 (>80%)
        public static final int OVERLOAD = 4;       // 超负荷 (>100%)
    }

    /**
     * 分析区域空间容量
     *
     * @param areaId 区域ID
     * @return 空间容量分析结果
     */
    public SpaceCapacityAnalysis analyzeSpaceCapacity(Long areaId) {
        log.info("[空间容量分析] 开始分析区域空间容量: areaId={}", areaId);

        SpaceCapacityAnalysis analysis = new SpaceCapacityAnalysis();

        // 获取区域信息
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null) {
            log.warn("[空间容量分析] 区域不存在: areaId={}", areaId);
            return analysis;
        }

        analysis.setAreaId(areaId);
        analysis.setAreaName(area.getAreaName());
        analysis.setAreaType(area.getAreaType());
        analysis.setAreaLevel(area.getAreaLevel());

        // 获取区域下的所有子区域
        List<AreaEntity> childAreas = areaDao.selectByParentId(areaId);
        analysis.setChildAreaCount(childAreas.size());

        // 获取区域设备统计
        List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaId(areaId);
        analysis.setDeviceCount(devices.size());

        // 按设备类型统计
        Map<Integer, Integer> deviceTypeCount = devices.stream()
                .collect(Collectors.groupingBy(AreaDeviceEntity::getDeviceType, Collectors.collectingAndThen(
                        Collectors.counting(), Math::toIntExact)));

        analysis.setDeviceTypeDistribution(deviceTypeCount);

        // 按业务模块统计
        Map<String, Integer> moduleDeviceCount = devices.stream()
                .collect(Collectors.groupingBy(AreaDeviceEntity::getBusinessModule, Collectors.collectingAndThen(
                        Collectors.counting(), Math::toIntExact)));

        analysis.setBusinessModuleDistribution(moduleDeviceCount);

        // 计算空间利用率
        double utilizationRate = calculateSpaceUtilizationRate(area, devices);
        analysis.setUtilizationRate(utilizationRate);

        // 分析容量状态
        int capacityStatus = determineCapacityStatus(utilizationRate);
        analysis.setCapacityStatus(capacityStatus);

        // 生成容量建议
        List<String> recommendations = generateCapacityRecommendations(area, analysis);
        analysis.setRecommendations(recommendations);

        // 计算容量趋势（模拟）
        List<CapacityTrend> trends = calculateCapacityTrends(areaId, devices);
        analysis.setTrends(trends);

        log.info("[空间容量分析] 完成区域空间容量分析: areaId={}, utilizationRate={}, status={}",
                areaId, utilizationRate, capacityStatus);

        return analysis;
    }

    /**
     * 批量分析多个区域的空间容量
     *
     * @param areaIds 区域ID列表
     * @return 空间容量分析结果列表
     */
    public List<SpaceCapacityAnalysis> batchAnalyzeSpaceCapacity(List<Long> areaIds) {
        log.info("[空间容量分析] 开始批量分析区域空间容量: count={}", areaIds.size());

        List<SpaceCapacityAnalysis> results = new ArrayList<>();
        for (Long areaId : areaIds) {
            try {
                SpaceCapacityAnalysis analysis = analyzeSpaceCapacity(areaId);
                results.add(analysis);
            } catch (Exception e) {
                log.error("[空间容量分析] 分析区域容量失败: areaId={}", areaId, e);
            }
        }

        log.info("[空间容量分析] 完成批量分析区域空间容量: count={}, success={}",
                areaIds.size(), results.size());
        return results;
    }

    /**
     * 获取区域空间容量对比分析
     *
     * @param parentAreaId 父区域ID
     * @return 容量对比分析
     */
    public SpaceCapacityComparison getSpaceCapacityComparison(Long parentAreaId) {
        log.info("[空间容量分析] 开始获取区域空间容量对比: parentAreaId={}", parentAreaId);

        SpaceCapacityComparison comparison = new SpaceCapacityComparison();

        // 获取所有子区域
        List<AreaEntity> childAreas = areaDao.selectByParentId(parentAreaId);
        if (childAreas.isEmpty()) {
            return comparison;
        }

        // 分析每个子区域的容量
        List<SpaceCapacityAnalysis> childAnalyses = new ArrayList<>();
        for (AreaEntity childArea : childAreas) {
            SpaceCapacityAnalysis analysis = analyzeSpaceCapacity(childArea.getAreaId());
            childAnalyses.add(analysis);
        }

        comparison.setParentAreaId(parentAreaId);
        comparison.setChildAreaAnalyses(childAnalyses);

        // 计算总体统计
        comparison.setTotalChildAreas(childAreas.size());
        comparison.setTotalDevices(childAnalyses.stream()
                .mapToInt(SpaceCapacityAnalysis::getDeviceCount)
                .sum());

        // 计算平均利用率
        double avgUtilization = childAnalyses.stream()
                .mapToDouble(SpaceCapacityAnalysis::getUtilizationRate)
                .average()
                .orElse(0.0);
        comparison.setAverageUtilizationRate(avgUtilization);

        // 找出利用率最高和最低的区域
        childAnalyses.stream()
                .max(Comparator.comparing(SpaceCapacityAnalysis::getUtilizationRate))
                .ifPresent(comparison::setHighestUtilizationArea);

        childAnalyses.stream()
                .min(Comparator.comparing(SpaceCapacityAnalysis::getUtilizationRate))
                .ifPresent(comparison::setLowestUtilizationArea);

        log.info("[空间容量分析] 完成区域空间容量对比: parentAreaId={}, avgUtilization={}",
                parentAreaId, avgUtilization);

        return comparison;
    }

    /**
     * 生成空间容量报告
     *
     * @param areaId 区域ID
     * @return 空间容量报告
     */
    public SpaceCapacityReport generateSpaceCapacityReport(Long areaId) {
        log.info("[空间容量分析] 开始生成空间容量报告: areaId={}", areaId);

        SpaceCapacityReport report = new SpaceCapacityReport();

        // 基础分析
        SpaceCapacityAnalysis analysis = analyzeSpaceCapacity(areaId);
        report.setAnalysis(analysis);

        // 区域层级信息
        List<AreaEntity> areaPath = areaDao.selectAreaPath(areaId);
        report.setAreaPath(areaPath);

        // 对比分析（如果有父区域）
        AreaEntity area = areaDao.selectById(areaId);
        if (area != null && area.getParentAreaId() != null) {
            SpaceCapacityComparison comparison = getSpaceCapacityComparison(area.getParentAreaId());
            report.setSiblingComparison(comparison);
        }

        // 生成报告摘要
        report.setSummary(generateReportSummary(analysis));

        // 生成优化建议
        report.setOptimizationSuggestions(generateOptimizationSuggestions(analysis));

        report.setGeneratedTime(LocalDateTime.now());

        log.info("[空间容量分析] 完成空间容量报告生成: areaId={}", areaId);
        return report;
    }

    /**
     * 计算空间利用率
     */
    private double calculateSpaceUtilizationRate(AreaEntity area, List<AreaDeviceEntity> devices) {
        if (devices.isEmpty()) {
            return 0.0;
        }

        // 基础利用率计算（可根据实际需求调整）
        double baseUtilization = devices.size() * 0.1; // 每个设备贡献10%的基础利用率

        // 设备类型权重
        Map<Integer, Double> deviceTypeWeights = Map.of(
                1, 0.15, // 门禁设备
                2, 0.12, // 考勤设备
                3, 0.20, // 消费设备
                4, 0.18, // 视频设备
                5, 0.10  // 访客设备
        );

        double weightedUtilization = devices.stream()
                .mapToDouble(device -> deviceTypeWeights.getOrDefault(device.getDeviceType(), 0.1))
                .sum();

        // 考虑区域大小的调整
        double areaFactor = calculateAreaFactor(area);
        double finalUtilization = (baseUtilization + weightedUtilization) * areaFactor;

        return Math.min(finalUtilization, 1.0); // 最大100%
    }

    /**
     * 计算区域因子
     */
    private double calculateAreaFactor(AreaEntity area) {
        // 根据区域级别调整利用率计算
        return switch (area.getAreaLevel()) {
            case 1 -> 0.8;  // 园区级别，利用率要求相对较低
            case 2 -> 0.9;  // 建筑级别
            case 3 -> 1.0;  // 楼层级别
            case 4 -> 1.1;  // 区域级别，利用率要求较高
            case 5 -> 1.2;  // 房间级别，利用率要求最高
            default -> 1.0;
        };
    }

    /**
     * 确定容量状态
     */
    private int determineCapacityStatus(double utilizationRate) {
        if (utilizationRate < 0.3) {
            return CapacityStatus.LOW_USAGE;
        } else if (utilizationRate < 0.8) {
            return CapacityStatus.NORMAL_USAGE;
        } else if (utilizationRate <= 1.0) {
            return CapacityStatus.HIGH_USAGE;
        } else {
            return CapacityStatus.OVERLOAD;
        }
    }

    /**
     * 生成容量建议
     */
    private List<String> generateCapacityRecommendations(AreaEntity area, SpaceCapacityAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();

        int status = analysis.getCapacityStatus();

        switch (status) {
            case CapacityStatus.LOW_USAGE:
                recommendations.add("当前区域利用率较低，建议增加设备配置以提高空间利用效率");
                recommendations.add("考虑合并相邻的低利用率区域以优化资源配置");
                break;
            case CapacityStatus.NORMAL_USAGE:
                recommendations.add("区域利用率在合理范围内，保持当前配置");
                recommendations.add("建议定期监控设备使用情况，及时调整布局");
                break;
            case CapacityStatus.HIGH_USAGE:
                recommendations.add("区域利用率较高，建议优化设备布局以提高空间效率");
                recommendations.add("考虑扩展现有区域或增加新的区域以分担压力");
                break;
            case CapacityStatus.OVERLOAD:
                recommendations.add("区域已超负荷，建议立即进行空间优化");
                recommendations.add("优先处理设备布局调整，必要时增加新区域");
                recommendations.add("评估当前设备配置的必要性，移除非关键设备");
                break;
            default:
                recommendations.add("容量状态未知，建议检查区域配置并重新计算容量指标");
                break;
        }

        // 基于设备分布的建议
        if (analysis.getDeviceCount() > 50) {
            recommendations.add("设备数量较多，建议实施分区域管理和监控");
        }

        return recommendations;
    }

    /**
     * 计算容量趋势
     */
    private List<CapacityTrend> calculateCapacityTrends(Long areaId, List<AreaDeviceEntity> currentDevices) {
        List<CapacityTrend> trends = new ArrayList<>();

        // 模拟过去6个月的容量趋势
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            CapacityTrend trend = new CapacityTrend();
            trend.setPeriod(now.minusMonths(i));

            // 模拟历史数据（实际应用中应从历史记录中获取）
            double randomFactor = 0.8 + (Math.random() * 0.4); // 0.8-1.2之间的随机因子
            trend.setDeviceCount((int) (currentDevices.size() * randomFactor));
            trend.setUtilizationRate(calculateSpaceUtilizationRate(null, currentDevices) * randomFactor);

            trends.add(trend);
        }

        return trends;
    }

    /**
     * 生成报告摘要
     */
    private String generateReportSummary(SpaceCapacityAnalysis analysis) {
        return String.format(
                "区域 %s 的空间容量分析完成。当前设备总数：%d，空间利用率：%.1f%%，容量状态：%s。",
                analysis.getAreaName(),
                analysis.getDeviceCount(),
                analysis.getUtilizationRate() * 100,
                getCapacityStatusText(analysis.getCapacityStatus())
        );
    }

    /**
     * 生成优化建议
     */
    private List<String> generateOptimizationSuggestions(SpaceCapacityAnalysis analysis) {
        List<String> suggestions = new ArrayList<>();

        // 基于设备分布的优化建议
        Map<String, Integer> moduleDistribution = analysis.getBusinessModuleDistribution();
        moduleDistribution.forEach((module, count) -> {
            if (count > 10) {
                suggestions.add(String.format("%s 模块设备数量较多(%d)，建议进行子区域划分", module, count));
            }
        });

        // 基于容量状态的优化建议
        if (analysis.getCapacityStatus() == CapacityStatus.HIGH_USAGE ||
            analysis.getCapacityStatus() == CapacityStatus.OVERLOAD) {
            suggestions.add("建议采用垂直布局方案，充分利用垂直空间");
            suggestions.add("考虑使用多功能设备以减少设备占用空间");
        }

        return suggestions;
    }

    /**
     * 获取容量状态文本
     */
    private String getCapacityStatusText(int status) {
        return switch (status) {
            case CapacityStatus.LOW_USAGE -> "低利用率";
            case CapacityStatus.NORMAL_USAGE -> "正常";
            case CapacityStatus.HIGH_USAGE -> "高利用率";
            case CapacityStatus.OVERLOAD -> "超负荷";
            default -> "未知";
        };
    }

    /**
     * 空间容量分析结果
     */
    public static class SpaceCapacityAnalysis {
        private Long areaId;
        private String areaName;
        private Integer areaType;
        private Integer areaLevel;
        private Integer childAreaCount;
        private Integer deviceCount;
        private Map<Integer, Integer> deviceTypeDistribution;
        private Map<String, Integer> businessModuleDistribution;
        private double utilizationRate;
        private int capacityStatus;
        private List<String> recommendations;
        private List<CapacityTrend> trends;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public Integer getAreaType() { return areaType; }
        public void setAreaType(Integer areaType) { this.areaType = areaType; }
        public Integer getAreaLevel() { return areaLevel; }
        public void setAreaLevel(Integer areaLevel) { this.areaLevel = areaLevel; }
        public Integer getChildAreaCount() { return childAreaCount; }
        public void setChildAreaCount(Integer childAreaCount) { this.childAreaCount = childAreaCount; }
        public Integer getDeviceCount() { return deviceCount; }
        public void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }
        public Map<Integer, Integer> getDeviceTypeDistribution() { return deviceTypeDistribution; }
        public void setDeviceTypeDistribution(Map<Integer, Integer> deviceTypeDistribution) { this.deviceTypeDistribution = deviceTypeDistribution; }
        public Map<String, Integer> getBusinessModuleDistribution() { return businessModuleDistribution; }
        public void setBusinessModuleDistribution(Map<String, Integer> businessModuleDistribution) { this.businessModuleDistribution = businessModuleDistribution; }
        public double getUtilizationRate() { return utilizationRate; }
        public void setUtilizationRate(double utilizationRate) { this.utilizationRate = utilizationRate; }
        public int getCapacityStatus() { return capacityStatus; }
        public void setCapacityStatus(int capacityStatus) { this.capacityStatus = capacityStatus; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
        public List<CapacityTrend> getTrends() { return trends; }
        public void setTrends(List<CapacityTrend> trends) { this.trends = trends; }
    }

    /**
     * 空间容量对比分析
     */
    public static class SpaceCapacityComparison {
        private Long parentAreaId;
        private List<SpaceCapacityAnalysis> childAreaAnalyses;
        private int totalChildAreas;
        private int totalDevices;
        private double averageUtilizationRate;
        private SpaceCapacityAnalysis highestUtilizationArea;
        private SpaceCapacityAnalysis lowestUtilizationArea;

        // getters and setters
        public Long getParentAreaId() { return parentAreaId; }
        public void setParentAreaId(Long parentAreaId) { this.parentAreaId = parentAreaId; }
        public List<SpaceCapacityAnalysis> getChildAreaAnalyses() { return childAreaAnalyses; }
        public void setChildAreaAnalyses(List<SpaceCapacityAnalysis> childAreaAnalyses) { this.childAreaAnalyses = childAreaAnalyses; }
        public int getTotalChildAreas() { return totalChildAreas; }
        public void setTotalChildAreas(int totalChildAreas) { this.totalChildAreas = totalChildAreas; }
        public int getTotalDevices() { return totalDevices; }
        public void setTotalDevices(int totalDevices) { this.totalDevices = totalDevices; }
        public double getAverageUtilizationRate() { return averageUtilizationRate; }
        public void setAverageUtilizationRate(double averageUtilizationRate) { this.averageUtilizationRate = averageUtilizationRate; }
        public SpaceCapacityAnalysis getHighestUtilizationArea() { return highestUtilizationArea; }
        public void setHighestUtilizationArea(SpaceCapacityAnalysis highestUtilizationArea) { this.highestUtilizationArea = highestUtilizationArea; }
        public SpaceCapacityAnalysis getLowestUtilizationArea() { return lowestUtilizationArea; }
        public void setLowestUtilizationArea(SpaceCapacityAnalysis lowestUtilizationArea) { this.lowestUtilizationArea = lowestUtilizationArea; }
    }

    /**
     * 空间容量报告
     */
    public static class SpaceCapacityReport {
        private SpaceCapacityAnalysis analysis;
        private List<AreaEntity> areaPath;
        private SpaceCapacityComparison siblingComparison;
        private String summary;
        private List<String> optimizationSuggestions;
        private LocalDateTime generatedTime;

        // getters and setters
        public SpaceCapacityAnalysis getAnalysis() { return analysis; }
        public void setAnalysis(SpaceCapacityAnalysis analysis) { this.analysis = analysis; }
        public List<AreaEntity> getAreaPath() { return areaPath; }
        public void setAreaPath(List<AreaEntity> areaPath) { this.areaPath = areaPath; }
        public SpaceCapacityComparison getSiblingComparison() { return siblingComparison; }
        public void setSiblingComparison(SpaceCapacityComparison siblingComparison) { this.siblingComparison = siblingComparison; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public List<String> getOptimizationSuggestions() { return optimizationSuggestions; }
        public void setOptimizationSuggestions(List<String> optimizationSuggestions) { this.optimizationSuggestions = optimizationSuggestions; }
        public LocalDateTime getGeneratedTime() { return generatedTime; }
        public void setGeneratedTime(LocalDateTime generatedTime) { this.generatedTime = generatedTime; }
    }

    /**
     * 容量趋势数据
     */
    public static class CapacityTrend {
        private LocalDateTime period;
        private int deviceCount;
        private double utilizationRate;

        // getters and setters
        public LocalDateTime getPeriod() { return period; }
        public void setPeriod(LocalDateTime period) { this.period = period; }
        public int getDeviceCount() { return deviceCount; }
        public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }
        public double getUtilizationRate() { return utilizationRate; }
        public void setUtilizationRate(double utilizationRate) { this.utilizationRate = utilizationRate; }
    }
}
