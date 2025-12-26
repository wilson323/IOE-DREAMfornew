package net.lab1024.sa.common.organization.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.entity.AreaEntity;

/**
 * 空间容量管理器
 * <p>
 * 职责：
 * - 区域容量管理
 * - 容量统计和监控
 * - 容量预警
 * </p>
 * <p>
 * 注意：Manager类是纯Java类，不直接依赖Spring注解，通过构造函数注入依赖。
 * 在微服务中通过配置类将Manager注册为Spring Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class SpaceCapacityManager {

    private final AreaDao areaDao;

    /**
     * 构造函数注入依赖
     *
     * @param areaDao 区域数据访问对象
     */
    public SpaceCapacityManager(AreaDao areaDao) {
        this.areaDao = areaDao;
    }

    /**
     * 分析区域空间容量
     *
     * @param areaId 区域ID
     * @return 空间容量分析结果
     */
    public SpaceCapacityAnalysis analyzeSpaceCapacity(Long areaId) {
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null) {
            return new SpaceCapacityAnalysis(areaId, null, 0, 0, 0.0, "NOT_FOUND");
        }

        Integer currentCapacity = area.getCapacity() != null ? area.getCapacity() : 0;
        Integer maxCapacity = currentCapacity > 0 ? currentCapacity : 100; // 默认容量
        Double usageRate = maxCapacity > 0 ? (double) currentCapacity / maxCapacity : 0.0;
        String status = usageRate > 0.9 ? "FULL" : usageRate > 0.7 ? "HIGH" : "NORMAL";

        SpaceCapacityAnalysis analysis = new SpaceCapacityAnalysis(areaId, area.getAreaName(), currentCapacity,
                maxCapacity, usageRate, status);
        analysis.setDeviceCount(0); // 需要从设备服务获取
        analysis.setUtilizationRate(usageRate);
        return analysis;
    }

    /**
     * 批量分析多个区域的空间容量
     *
     * @param areaIds 区域ID列表
     * @return 空间容量分析结果列表
     */
    public List<SpaceCapacityAnalysis> batchAnalyzeSpaceCapacity(List<Long> areaIds) {
        if (areaIds == null || areaIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SpaceCapacityAnalysis> results = new ArrayList<>();
        for (Long areaId : areaIds) {
            results.add(analyzeSpaceCapacity(areaId));
        }
        return results;
    }

    /**
     * 获取区域空间容量对比分析
     *
     * @param parentAreaId 父区域ID
     * @return 容量对比分析
     */
    public SpaceCapacityComparison getSpaceCapacityComparison(Long parentAreaId) {
        AreaEntity parentArea = areaDao.selectById(parentAreaId);
        if (parentArea == null) {
            return new SpaceCapacityComparison(parentAreaId, null, Collections.emptyList());
        }

        List<AreaEntity> childAreas = areaDao.selectByParentId(parentAreaId);
        List<AreaCapacityInfo> childCapacityInfos = new ArrayList<>();
        for (AreaEntity child : childAreas) {
            SpaceCapacityAnalysis analysis = analyzeSpaceCapacity(child.getAreaId());
            childCapacityInfos.add(new AreaCapacityInfo(
                    child.getAreaId(),
                    child.getAreaName(),
                    analysis.getCurrentCapacity(),
                    analysis.getMaxCapacity(),
                    analysis.getUsageRate()));
        }

        return new SpaceCapacityComparison(parentAreaId, parentArea.getAreaName(), childCapacityInfos);
    }

    /**
     * 生成空间容量报告
     *
     * @param areaId 区域ID
     * @return 空间容量报告
     */
    public SpaceCapacityReport generateSpaceCapacityReport(Long areaId) {
        SpaceCapacityAnalysis analysis = analyzeSpaceCapacity(areaId);
        CapacityStatistics statistics = new CapacityStatistics(1L, (long) analysis.getMaxCapacity(),
                (long) analysis.getCurrentCapacity(), analysis.getUsageRate());
        List<AreaCapacityInfo> details = Collections.singletonList(new AreaCapacityInfo(
                analysis.getAreaId(),
                analysis.getAreaName(),
                analysis.getCurrentCapacity(),
                analysis.getMaxCapacity(),
                analysis.getUsageRate()));
        List<String> recommendations = analysis.getRecommendations();

        return new SpaceCapacityReport(areaId, analysis.getAreaName(), statistics, details, recommendations);
    }

    /**
     * 区域容量信息
     */
    public static class AreaCapacityInfo {
        private Long areaId;
        private String areaName;
        private Integer currentCapacity;
        private Integer maxCapacity;
        private Double usageRate;

        public AreaCapacityInfo(Long areaId, String areaName, Integer currentCapacity, Integer maxCapacity,
                Double usageRate) {
            this.areaId = areaId;
            this.areaName = areaName;
            this.currentCapacity = currentCapacity;
            this.maxCapacity = maxCapacity;
            this.usageRate = usageRate;
        }

        public Long getAreaId() {
            return areaId;
        }

        public String getAreaName() {
            return areaName;
        }

        public Integer getCurrentCapacity() {
            return currentCapacity;
        }

        public Integer getMaxCapacity() {
            return maxCapacity;
        }

        public Double getUsageRate() {
            return usageRate;
        }
    }

    /**
     * 容量统计信息
     */
    public static class CapacityStatistics {
        private Long totalAreas;
        private Long totalCapacity;
        private Long usedCapacity;
        private Double averageUsageRate;

        public CapacityStatistics(Long totalAreas, Long totalCapacity, Long usedCapacity, Double averageUsageRate) {
            this.totalAreas = totalAreas;
            this.totalCapacity = totalCapacity;
            this.usedCapacity = usedCapacity;
            this.averageUsageRate = averageUsageRate;
        }

        public Long getTotalAreas() {
            return totalAreas;
        }

        public Long getTotalCapacity() {
            return totalCapacity;
        }

        public Long getUsedCapacity() {
            return usedCapacity;
        }

        public Double getAverageUsageRate() {
            return averageUsageRate;
        }
    }

    /**
     * 空间容量分析结果
     */
    public static class SpaceCapacityAnalysis {
        private Long areaId;
        private String areaName;
        private Integer currentCapacity;
        private Integer maxCapacity;
        private Double usageRate;
        private String status;
        private List<String> recommendations;
        private List<CapacityTrend> trends;
        private Integer deviceCount;
        private Double utilizationRate;

        public SpaceCapacityAnalysis(Long areaId, String areaName, Integer currentCapacity, Integer maxCapacity,
                Double usageRate, String status) {
            this.areaId = areaId;
            this.areaName = areaName;
            this.currentCapacity = currentCapacity;
            this.maxCapacity = maxCapacity;
            this.usageRate = usageRate;
            this.status = status;
            this.recommendations = new java.util.ArrayList<>();
            this.trends = new java.util.ArrayList<>();
        }

        public Long getAreaId() {
            return areaId;
        }

        public String getAreaName() {
            return areaName;
        }

        public Integer getCurrentCapacity() {
            return currentCapacity;
        }

        public Integer getMaxCapacity() {
            return maxCapacity;
        }

        public Double getUsageRate() {
            return usageRate;
        }

        public String getStatus() {
            return status;
        }

        public List<String> getRecommendations() {
            return recommendations != null ? recommendations : new java.util.ArrayList<>();
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }

        public List<CapacityTrend> getTrends() {
            return trends != null ? trends : new java.util.ArrayList<>();
        }

        public void setTrends(List<CapacityTrend> trends) {
            this.trends = trends;
        }

        public Integer getDeviceCount() {
            return deviceCount != null ? deviceCount : 0;
        }

        public void setDeviceCount(Integer deviceCount) {
            this.deviceCount = deviceCount;
        }

        public Double getUtilizationRate() {
            return utilizationRate != null ? utilizationRate : usageRate != null ? usageRate : 0.0;
        }

        public void setUtilizationRate(Double utilizationRate) {
            this.utilizationRate = utilizationRate;
        }
    }

    /**
     * 空间容量对比分析
     */
    public static class SpaceCapacityComparison {
        private Long parentAreaId;
        private String parentAreaName;
        private List<AreaCapacityInfo> childAreas;

        public SpaceCapacityComparison(Long parentAreaId, String parentAreaName, List<AreaCapacityInfo> childAreas) {
            this.parentAreaId = parentAreaId;
            this.parentAreaName = parentAreaName;
            this.childAreas = childAreas;
        }

        public Long getParentAreaId() {
            return parentAreaId;
        }

        public String getParentAreaName() {
            return parentAreaName;
        }

        public List<AreaCapacityInfo> getChildAreas() {
            return childAreas;
        }
    }

    /**
     * 空间容量报告
     */
    public static class SpaceCapacityReport {
        private Long areaId;
        private String areaName;
        private CapacityStatistics statistics;
        private List<AreaCapacityInfo> details;
        private List<String> recommendations;

        public SpaceCapacityReport(Long areaId, String areaName, CapacityStatistics statistics,
                List<AreaCapacityInfo> details, List<String> recommendations) {
            this.areaId = areaId;
            this.areaName = areaName;
            this.statistics = statistics;
            this.details = details;
            this.recommendations = recommendations;
        }

        public Long getAreaId() {
            return areaId;
        }

        public String getAreaName() {
            return areaName;
        }

        public CapacityStatistics getStatistics() {
            return statistics;
        }

        public List<AreaCapacityInfo> getDetails() {
            return details;
        }

        public List<String> getRecommendations() {
            return recommendations;
        }
    }

    /**
     * 容量趋势数据
     */
    public static class CapacityTrend {
        private String period;
        private Integer capacity;
        private Double usageRate;

        public CapacityTrend(String period, Integer capacity, Double usageRate) {
            this.period = period;
            this.capacity = capacity;
            this.usageRate = usageRate;
        }

        public String getPeriod() {
            return period;
        }

        public Integer getCapacity() {
            return capacity;
        }

        public Double getUsageRate() {
            return usageRate;
        }
    }
}
