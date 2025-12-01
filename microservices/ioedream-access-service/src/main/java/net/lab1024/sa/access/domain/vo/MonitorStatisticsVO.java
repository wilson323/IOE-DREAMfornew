package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁监控统计VO
 * 基于现有监控系统的统计数据增强
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁监控统计VO")
public class MonitorStatisticsVO {

    /**
     * 统计时间范围（小时）
     */
    @Schema(description = "统计时间范围", example = "24")
    private Integer timeRangeHours;

    /**
     * 统计开始时间
     */
    @Schema(description = "统计开始时间", example = "2025-11-24T10:30:00")
    private LocalDateTime statisticsStartTime;

    /**
     * 统计结束时间
     */
    @Schema(description = "统计结束时间", example = "2025-11-25T10:30:00")
    private LocalDateTime statisticsEndTime;

    /**
     * 设备统计
     */
    @Schema(description = "设备统计")
    private DeviceStatisticsVO deviceStatistics;

    /**
     * 访问统计
     */
    @Schema(description = "访问统计")
    private AccessStatisticsVO accessStatistics;

    /**
     * 告警统计
     */
    @Schema(description = "告警统计")
    private AlertStatisticsVO alertStatistics;

    /**
     * 区域统计
     */
    @Schema(description = "区域统计")
    private AreaStatisticsVO areaStatistics;

    /**
     * 人员统计
     */
    @Schema(description = "人员统计")
    private PersonStatisticsVO personStatistics;

    /**
     * 性能统计
     */
    @Schema(description = "性能统计")
    private PerformanceStatisticsVO performanceStatistics;

    /**
     * 视频联动统计
     */
    @Schema(description = "视频联动统计")
    private VideoLinkageStatisticsVO videoLinkageStatistics;

    /**
     * 趋势数据
     */
    @Schema(description = "趋势数据")
    private List<TrendDataVO> trendData;

    /**
     * 生成时间
     */
    @Schema(description = "生成时间", example = "2025-11-25T10:30:00")
    private LocalDateTime generateTime;

    /**
     * 数据更新时间
     */
    @Schema(description = "数据更新时间", example = "2025-11-25T10:30:00")
    private LocalDateTime dataUpdateTime;

    // 内部类：设备统计
    @Schema(description = "设备统计")
    public static class DeviceStatisticsVO {
        @Schema(description = "设备总数", example = "150")
        private Integer totalDeviceCount;

        @Schema(description = "在线设备数", example = "142")
        private Integer onlineDeviceCount;

        @Schema(description = "离线设备数", example = "8")
        private Integer offlineDeviceCount;

        @Schema(description = "故障设备数", example = "3")
        private Integer faultDeviceCount;

        @Schema(description = "维护中设备数", example = "2")
        private Integer maintenanceDeviceCount;

        @Schema(description = "设备在线率", example = "94.7")
        private Double onlineRate;

        @Schema(description = "设备健康度", example = "92.3")
        private Double healthScore;

        @Schema(description = "设备类型分布")
        private Map<String, Integer> deviceTypeDistribution;

        @Schema(description = "设备位置分布")
        private Map<String, Integer> deviceLocationDistribution;

        // Getters and Setters
        public Integer getTotalDeviceCount() { return totalDeviceCount; }
        public void setTotalDeviceCount(Integer totalDeviceCount) { this.totalDeviceCount = totalDeviceCount; }

        public Integer getOnlineDeviceCount() { return onlineDeviceCount; }
        public void setOnlineDeviceCount(Integer onlineDeviceCount) { this.onlineDeviceCount = onlineDeviceCount; }

        public Integer getOfflineDeviceCount() { return offlineDeviceCount; }
        public void setOfflineDeviceCount(Integer offlineDeviceCount) { this.offlineDeviceCount = offlineDeviceCount; }

        public Integer getFaultDeviceCount() { return faultDeviceCount; }
        public void setFaultDeviceCount(Integer faultDeviceCount) { this.faultDeviceCount = faultDeviceCount; }

        public Integer getMaintenanceDeviceCount() { return maintenanceDeviceCount; }
        public void setMaintenanceDeviceCount(Integer maintenanceDeviceCount) { this.maintenanceDeviceCount = maintenanceDeviceCount; }

        public Double getOnlineRate() { return onlineRate; }
        public void setOnlineRate(Double onlineRate) { this.onlineRate = onlineRate; }

        public Double getHealthScore() { return healthScore; }
        public void setHealthScore(Double healthScore) { this.healthScore = healthScore; }

        public Map<String, Integer> getDeviceTypeDistribution() { return deviceTypeDistribution; }
        public void setDeviceTypeDistribution(Map<String, Integer> deviceTypeDistribution) { this.deviceTypeDistribution = deviceTypeDistribution; }

        public Map<String, Integer> getDeviceLocationDistribution() { return deviceLocationDistribution; }
        public void setDeviceLocationDistribution(Map<String, Integer> deviceLocationDistribution) { this.deviceLocationDistribution = deviceLocationDistribution; }
    }

    // 内部类：访问统计
    @Schema(description = "访问统计")
    public static class AccessStatisticsVO {
        @Schema(description = "总访问次数", example = "5234")
        private Integer totalAccessCount;

        @Schema(description = "成功访问次数", example = "5189")
        private Integer successAccessCount;

        @Schema(description = "失败访问次数", example = "45")
        private Integer failureAccessCount;

        @Schema(description = "访问成功率", example = "99.1")
        private Double successRate;

        @Schema(description = "唯一访问人数", example = "856")
        private Integer uniquePersonCount;

        @Schema(description = "平均每日访问次数", example = "2617")
        private Double averageDailyAccess;

        @Schema(description = "高峰时段访问次数", example = "1234")
        private Integer peakHourAccess;

        @Schema(description = "访问类型分布")
        private Map<String, Integer> accessTypeDistribution;

        @Schema(description = "人员类型分布")
        private Map<String, Integer> personTypeDistribution;

        @Schema(description = "区域访问分布")
        private Map<String, Integer> areaAccessDistribution;

        // Getters and Setters
        public Integer getTotalAccessCount() { return totalAccessCount; }
        public void setTotalAccessCount(Integer totalAccessCount) { this.totalAccessCount = totalAccessCount; }

        public Integer getSuccessAccessCount() { return successAccessCount; }
        public void setSuccessAccessCount(Integer successAccessCount) { this.successAccessCount = successAccessCount; }

        public Integer getFailureAccessCount() { return failureAccessCount; }
        public void setFailureAccessCount(Integer failureAccessCount) { this.failureAccessCount = failureAccessCount; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public Integer getUniquePersonCount() { return uniquePersonCount; }
        public void setUniquePersonCount(Integer uniquePersonCount) { this.uniquePersonCount = uniquePersonCount; }

        public Double getAverageDailyAccess() { return averageDailyAccess; }
        public void setAverageDailyAccess(Double averageDailyAccess) { this.averageDailyAccess = averageDailyAccess; }

        public Integer getPeakHourAccess() { return peakHourAccess; }
        public void setPeakHourAccess(Integer peakHourAccess) { this.peakHourAccess = peakHourAccess; }

        public Map<String, Integer> getAccessTypeDistribution() { return accessTypeDistribution; }
        public void setAccessTypeDistribution(Map<String, Integer> accessTypeDistribution) { this.accessTypeDistribution = accessTypeDistribution; }

        public Map<String, Integer> getPersonTypeDistribution() { return personTypeDistribution; }
        public void setPersonTypeDistribution(Map<String, Integer> personTypeDistribution) { this.personTypeDistribution = personTypeDistribution; }

        public Map<String, Integer> getAreaAccessDistribution() { return areaAccessDistribution; }
        public void setAreaAccessDistribution(Map<String, Integer> areaAccessDistribution) { this.areaAccessDistribution = areaAccessDistribution; }
    }

    // 内部类：告警统计
    @Schema(description = "告警统计")
    public static class AlertStatisticsVO {
        @Schema(description = "总告警数", example = "67")
        private Integer totalAlertCount;

        @Schema(description = "已处理告警数", example = "58")
        private Integer handledAlertCount;

        @Schema(description = "未处理告警数", example = "9")
        private Integer unhandledAlertCount;

        @Schema(description = "紧急告警数", example = "3")
        private Integer criticalAlertCount;

        @Schema(description = "严重告警数", example = "8")
        private Integer severeAlertCount;

        @Schema(description = "警告告警数", example = "45")
        private Integer warningAlertCount;

        @Schema(description = "信息告警数", example = "11")
        private Integer infoAlertCount;

        @Schema(description = "告警处理率", example = "86.6")
        private Double handlingRate;

        @Schema(description = "平均处理时间（分钟）", example = "15.5")
        private Double averageHandlingTime;

        @Schema(description = "告警类型分布")
        private Map<String, Integer> alertTypeDistribution;

        @Schema(description = "告警级别分布")
        private Map<String, Integer> alertLevelDistribution;

        // Getters and Setters
        public Integer getTotalAlertCount() { return totalAlertCount; }
        public void setTotalAlertCount(Integer totalAlertCount) { this.totalAlertCount = totalAlertCount; }

        public Integer getHandledAlertCount() { return handledAlertCount; }
        public void setHandledAlertCount(Integer handledAlertCount) { this.handledAlertCount = handledAlertCount; }

        public Integer getUnhandledAlertCount() { return unhandledAlertCount; }
        public void setUnhandledAlertCount(Integer unhandledAlertCount) { this.unhandledAlertCount = unhandledAlertCount; }

        public Integer getCriticalAlertCount() { return criticalAlertCount; }
        public void setCriticalAlertCount(Integer criticalAlertCount) { this.criticalAlertCount = criticalAlertCount; }

        public Integer getSevereAlertCount() { return severeAlertCount; }
        public void setSevereAlertCount(Integer severeAlertCount) { this.severeAlertCount = severeAlertCount; }

        public Integer getWarningAlertCount() { return warningAlertCount; }
        public void setWarningAlertCount(Integer warningAlertCount) { this.warningAlertCount = warningAlertCount; }

        public Integer getInfoAlertCount() { return infoAlertCount; }
        public void setInfoAlertCount(Integer infoAlertCount) { this.infoAlertCount = infoAlertCount; }

        public Double getHandlingRate() { return handlingRate; }
        public void setHandlingRate(Double handlingRate) { this.handlingRate = handlingRate; }

        public Double getAverageHandlingTime() { return averageHandlingTime; }
        public void setAverageHandlingTime(Double averageHandlingTime) { this.averageHandlingTime = averageHandlingTime; }

        public Map<String, Integer> getAlertTypeDistribution() { return alertTypeDistribution; }
        public void setAlertTypeDistribution(Map<String, Integer> alertTypeDistribution) { this.alertTypeDistribution = alertTypeDistribution; }

        public Map<String, Integer> getAlertLevelDistribution() { return alertLevelDistribution; }
        public void setAlertLevelDistribution(Map<String, Integer> alertLevelDistribution) { this.alertLevelDistribution = alertLevelDistribution; }
    }

    // 内部类：区域统计
    @Schema(description = "区域统计")
    public static class AreaStatisticsVO {
        @Schema(description = "监控区域总数", example = "25")
        private Integer totalAreaCount;

        @Schema(description = "有人员区域数", example = "18")
        private Integer occupiedAreaCount;

        @Schema(description = "超容告警区域数", example = "2")
        private Integer overflowAlertAreaCount;

        @Schema(description = "区域平均容量利用率", example = "65.3")
        private Double averageCapacityUtilization;

        @Schema(description = "最大容量利用率", example = "95.0")
        private Double maxCapacityUtilization;

        @Schema(description = "区域活动热力图")
        private Map<String, Double> areaActivityHeatmap;

        @Schema(description = "区域类型分布")
        private Map<String, Integer> areaTypeDistribution;

        // Getters and Setters
        public Integer getTotalAreaCount() { return totalAreaCount; }
        public void setTotalAreaCount(Integer totalAreaCount) { this.totalAreaCount = totalAreaCount; }

        public Integer getOccupiedAreaCount() { return occupiedAreaCount; }
        public void setOccupiedAreaCount(Integer occupiedAreaCount) { this.occupiedAreaCount = occupiedAreaCount; }

        public Integer getOverflowAlertAreaCount() { return overflowAlertAreaCount; }
        public void setOverflowAlertAreaCount(Integer overflowAlertAreaCount) { this.overflowAlertAreaCount = overflowAlertAreaCount; }

        public Double getAverageCapacityUtilization() { return averageCapacityUtilization; }
        public void setAverageCapacityUtilization(Double averageCapacityUtilization) { this.averageCapacityUtilization = averageCapacityUtilization; }

        public Double getMaxCapacityUtilization() { return maxCapacityUtilization; }
        public void setMaxCapacityUtilization(Double maxCapacityUtilization) { this.maxCapacityUtilization = maxCapacityUtilization; }

        public Map<String, Double> getAreaActivityHeatmap() { return areaActivityHeatmap; }
        public void setAreaActivityHeatmap(Map<String, Double> areaActivityHeatmap) { this.areaActivityHeatmap = areaActivityHeatmap; }

        public Map<String, Integer> getAreaTypeDistribution() { return areaTypeDistribution; }
        public void setAreaTypeDistribution(Map<String, Integer> areaTypeDistribution) { this.areaTypeDistribution = areaTypeDistribution; }
    }

    // 内部类：人员统计
    @Schema(description = "人员统计")
    public static class PersonStatisticsVO {
        @Schema(description = "活跃人员总数", example = "856")
        private Integer activePersonCount;

        @Schema(description = "当前在场人员数", example = "342")
        private Integer currentPresentPersonCount;

        @Schema(description = "平均在场时长（小时）", example = "6.5")
        private Double averagePresentHours;

        @Schema(description = "人员类型分布")
        private Map<String, Integer> personTypeDistribution;

        @Schema(description = "部门分布")
        private Map<String, Integer> departmentDistribution;

        @Schema(description = "访问频次分布")
        private Map<String, Integer> accessFrequencyDistribution;

        // Getters and Setters
        public Integer getActivePersonCount() { return activePersonCount; }
        public void setActivePersonCount(Integer activePersonCount) { this.activePersonCount = activePersonCount; }

        public Integer getCurrentPresentPersonCount() { return currentPresentPersonCount; }
        public void setCurrentPresentPersonCount(Integer currentPresentPersonCount) { this.currentPresentPersonCount = currentPresentPersonCount; }

        public Double getAveragePresentHours() { return averagePresentHours; }
        public void setAveragePresentHours(Double averagePresentHours) { this.averagePresentHours = averagePresentHours; }

        public Map<String, Integer> getPersonTypeDistribution() { return personTypeDistribution; }
        public void setPersonTypeDistribution(Map<String, Integer> personTypeDistribution) { this.personTypeDistribution = personTypeDistribution; }

        public Map<String, Integer> getDepartmentDistribution() { return departmentDistribution; }
        public void setDepartmentDistribution(Map<String, Integer> departmentDistribution) { this.departmentDistribution = departmentDistribution; }

        public Map<String, Integer> getAccessFrequencyDistribution() { return accessFrequencyDistribution; }
        public void setAccessFrequencyDistribution(Map<String, Integer> accessFrequencyDistribution) { this.accessFrequencyDistribution = accessFrequencyDistribution; }
    }

    // 内部类：性能统计
    @Schema(description = "性能统计")
    public static class PerformanceStatisticsVO {
        @Schema(description = "平均设备响应时间（毫秒）", example = "125")
        private Double averageDeviceResponseTime;

        @Schema(description = "平均系统响应时间（毫秒）", example = "85")
        private Double averageSystemResponseTime;

        @Schema(description = "设备状态更新延迟（秒）", example = "3.2")
        private Double deviceStatusUpdateDelay;

        @Schema(description = "告警响应时间（秒）", example = "12.5")
        private Double alertResponseTime;

        @Schema(description = "视频联动响应时间（秒）", example = "1.8")
        private Double videoLinkageResponseTime;

        @Schema(description = "系统负载平均值", example = "45.6")
        private Double systemLoadAverage;

        @Schema(description = "内存使用率", example = "68.3")
        private Double memoryUsage;

        @Schema(description = "CPU使用率", example = "35.7")
        private Double cpuUsage;

        @Schema(description = "网络吞吐量（Mbps）", example = "125.6")
        private Double networkThroughput;

        // Getters and Setters
        public Double getAverageDeviceResponseTime() { return averageDeviceResponseTime; }
        public void setAverageDeviceResponseTime(Double averageDeviceResponseTime) { this.averageDeviceResponseTime = averageDeviceResponseTime; }

        public Double getAverageSystemResponseTime() { return averageSystemResponseTime; }
        public void setAverageSystemResponseTime(Double averageSystemResponseTime) { this.averageSystemResponseTime = averageSystemResponseTime; }

        public Double getDeviceStatusUpdateDelay() { return deviceStatusUpdateDelay; }
        public void setDeviceStatusUpdateDelay(Double deviceStatusUpdateDelay) { this.deviceStatusUpdateDelay = deviceStatusUpdateDelay; }

        public Double getAlertResponseTime() { return alertResponseTime; }
        public void setAlertResponseTime(Double alertResponseTime) { this.alertResponseTime = alertResponseTime; }

        public Double getVideoLinkageResponseTime() { return videoLinkageResponseTime; }
        public void setVideoLinkageResponseTime(Double videoLinkageResponseTime) { this.videoLinkageResponseTime = videoLinkageResponseTime; }

        public Double getSystemLoadAverage() { return systemLoadAverage; }
        public void setSystemLoadAverage(Double systemLoadAverage) { this.systemLoadAverage = systemLoadAverage; }

        public Double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }

        public Double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }

        public Double getNetworkThroughput() { return networkThroughput; }
        public void setNetworkThroughput(Double networkThroughput) { this.networkThroughput = networkThroughput; }
    }

    // 内部类：视频联动统计
    @Schema(description = "视频联动统计")
    public static class VideoLinkageStatisticsVO {
        @Schema(description = "总联动次数", example = "234")
        private Integer totalLinkageCount;

        @Schema(description = "成功联动次数", example = "228")
        private Integer successLinkageCount;

        @Schema(description = "失败联动次数", example = "6")
        private Integer failureLinkageCount;

        @Schema(description = "联动成功率", example = "97.4")
        private Double successRate;

        @Schema(description = "平均联动响应时间（秒）", example = "1.5")
        private Double averageLinkageResponseTime;

        @Schema(description = "录制视频总时长（小时）", example = "156.8")
        private Double totalRecordingHours;

        @Schema(description = "录制文件总大小（GB）", example = "45.6")
        private Double totalRecordingSize;

        @Schema(description = "联动类型分布")
        private Map<String, Integer> linkageTypeDistribution;

        // Getters and Setters
        public Integer getTotalLinkageCount() { return totalLinkageCount; }
        public void setTotalLinkageCount(Integer totalLinkageCount) { this.totalLinkageCount = totalLinkageCount; }

        public Integer getSuccessLinkageCount() { return successLinkageCount; }
        public void setSuccessLinkageCount(Integer successLinkageCount) { this.successLinkageCount = successLinkageCount; }

        public Integer getFailureLinkageCount() { return failureLinkageCount; }
        public void setFailureLinkageCount(Integer failureLinkageCount) { this.failureLinkageCount = failureLinkageCount; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public Double getAverageLinkageResponseTime() { return averageLinkageResponseTime; }
        public void setAverageLinkageResponseTime(Double averageLinkageResponseTime) { this.averageLinkageResponseTime = averageLinkageResponseTime; }

        public Double getTotalRecordingHours() { return totalRecordingHours; }
        public void setTotalRecordingHours(Double totalRecordingHours) { this.totalRecordingHours = totalRecordingHours; }

        public Double getTotalRecordingSize() { return totalRecordingSize; }
        public void setTotalRecordingSize(Double totalRecordingSize) { this.totalRecordingSize = totalRecordingSize; }

        public Map<String, Integer> getLinkageTypeDistribution() { return linkageTypeDistribution; }
        public void setLinkageTypeDistribution(Map<String, Integer> linkageTypeDistribution) { this.linkageTypeDistribution = linkageTypeDistribution; }
    }

    // 内部类：趋势数据
    @Schema(description = "趋势数据")
    public static class TrendDataVO {
        @Schema(description = "时间点", example = "2025-11-25T10:00:00")
        private LocalDateTime timestamp;

        @Schema(description = "访问次数", example = "125")
        private Integer accessCount;

        @Schema(description = "在线设备数", example = "142")
        private Integer onlineDeviceCount;

        @Schema(description = "告警数量", example = "5")
        private Integer alertCount;

        @Schema(description = "在场人数", example = "342")
        private Integer presentPersonCount;

        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Integer getAccessCount() { return accessCount; }
        public void setAccessCount(Integer accessCount) { this.accessCount = accessCount; }

        public Integer getOnlineDeviceCount() { return onlineDeviceCount; }
        public void setOnlineDeviceCount(Integer onlineDeviceCount) { this.onlineDeviceCount = onlineDeviceCount; }

        public Integer getAlertCount() { return alertCount; }
        public void setAlertCount(Integer alertCount) { this.alertCount = alertCount; }

        public Integer getPresentPersonCount() { return presentPersonCount; }
        public void setPresentPersonCount(Integer presentPersonCount) { this.presentPersonCount = presentPersonCount; }
    }

    // 主要类的Getters and Setters
    public Integer getTimeRangeHours() { return timeRangeHours; }
    public void setTimeRangeHours(Integer timeRangeHours) { this.timeRangeHours = timeRangeHours; }

    public LocalDateTime getStatisticsStartTime() { return statisticsStartTime; }
    public void setStatisticsStartTime(LocalDateTime statisticsStartTime) { this.statisticsStartTime = statisticsStartTime; }

    public LocalDateTime getStatisticsEndTime() { return statisticsEndTime; }
    public void setStatisticsEndTime(LocalDateTime statisticsEndTime) { this.statisticsEndTime = statisticsEndTime; }

    public DeviceStatisticsVO getDeviceStatistics() { return deviceStatistics; }
    public void setDeviceStatistics(DeviceStatisticsVO deviceStatistics) { this.deviceStatistics = deviceStatistics; }

    public AccessStatisticsVO getAccessStatistics() { return accessStatistics; }
    public void setAccessStatistics(AccessStatisticsVO accessStatistics) { this.accessStatistics = accessStatistics; }

    public AlertStatisticsVO getAlertStatistics() { return alertStatistics; }
    public void setAlertStatistics(AlertStatisticsVO alertStatistics) { this.alertStatistics = alertStatistics; }

    public AreaStatisticsVO getAreaStatistics() { return areaStatistics; }
    public void setAreaStatistics(AreaStatisticsVO areaStatistics) { this.areaStatistics = areaStatistics; }

    public PersonStatisticsVO getPersonStatistics() { return personStatistics; }
    public void setPersonStatistics(PersonStatisticsVO personStatistics) { this.personStatistics = personStatistics; }

    public PerformanceStatisticsVO getPerformanceStatistics() { return performanceStatistics; }
    public void setPerformanceStatistics(PerformanceStatisticsVO performanceStatistics) { this.performanceStatistics = performanceStatistics; }

    public VideoLinkageStatisticsVO getVideoLinkageStatistics() { return videoLinkageStatistics; }
    public void setVideoLinkageStatistics(VideoLinkageStatisticsVO videoLinkageStatistics) { this.videoLinkageStatistics = videoLinkageStatistics; }

    public List<TrendDataVO> getTrendData() { return trendData; }
    public void setTrendData(List<TrendDataVO> trendData) { this.trendData = trendData; }

    public LocalDateTime getGenerateTime() { return generateTime; }
    public void setGenerateTime(LocalDateTime generateTime) { this.generateTime = generateTime; }

    public LocalDateTime getDataUpdateTime() { return dataUpdateTime; }
    public void setDataUpdateTime(LocalDateTime dataUpdateTime) { this.dataUpdateTime = dataUpdateTime; }
}