package net.lab1024.sa.admin.module.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁区域容量监控VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁区域容量监控VO")
public class AccessAreaCapacityVO {

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "办公区域")
    private String areaName;

    /**
     * 区域类型
     */
    @Schema(description = "区域类型", example = "OFFICE")
    private String areaType;

    /**
     * 设计容量
     */
    @Schema(description = "设计容量", example = "100")
    private Integer designCapacity;

    /**
     * 当前人数
     */
    @Schema(description = "当前人数", example = "75")
    private Integer currentOccupancy;

    /**
     * 最大同时进入人数
     */
    @Schema(description = "最大同时进入人数", example = "50")
    private Integer maxSimultaneousEntry;

    /**
     * 当前进入人数
     */
    @Schema(description = "当前进入人数", example = "25")
    private Integer currentEntryCount;

    /**
     * 容量利用率（百分比）
     */
    @Schema(description = "容量利用率（百分比）", example = "75.0")
    private Double capacityUtilizationRate;

    /**
     * 进入人数利用率（百分比）
     */
    @Schema(description = "进入人数利用率（百分比）", example = "50.0")
    private Double entryUtilizationRate;

    /**
     * 是否超容告警
     */
    @Schema(description = "是否超容告警", example = "false")
    private Boolean capacityAlert;

    /**
     * 告警级别
     * NORMAL-正常
     * WARNING-警告
     * CRITICAL-严重
     * EMERGENCY-紧急
     */
    @Schema(description = "告警级别", example = "NORMAL")
    private String alertLevel;

    /**
     * 告警阈值设置
     */
    @Schema(description = "告警阈值设置")
    private AlertThresholdVO alertThresholds;

    /**
     * 容量历史数据
     */
    @Schema(description = "容量历史数据")
    private List<CapacityHistoryVO> capacityHistory;

    /**
     * 实时人员列表
     */
    @Schema(description = "实时人员列表")
    private List<PersonInAreaVO> currentPersons;

    /**
     * 预测容量趋势
     */
    @Schema(description = "预测容量趋势")
    private List<CapacityForecastVO> capacityForecast;

    /**
     * 高峰时段统计
     */
    @Schema(description = "高峰时段统计")
    private List<PeakHourVO> peakHourStatistics;

    /**
     * 区域限制配置
     */
    @Schema(description = "区域限制配置")
    private AreaLimitVO areaLimits;

    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间", example = "2025-11-25T10:30:00")
    private LocalDateTime lastUpdateTime;

    /**
     * 统计时间范围
     */
    @Schema(description = "统计时间范围", example = "24小时")
    private String statisticsTimeRange;

    /**
     * 告警信息
     */
    @Schema(description = "告警信息")
    private List<CapacityAlertVO> alerts;

    // 内部类：告警阈值配置
    @Schema(description = "告警阈值配置")
    public static class AlertThresholdVO {
        @Schema(description = "容量警告阈值（百分比）", example = "80")
        private Double capacityWarningThreshold;

        @Schema(description = "容量严重阈值（百分比）", example = "90")
        private Double capacityCriticalThreshold;

        @Schema(description = "容量紧急阈值（百分比）", example = "95")
        private Double capacityEmergencyThreshold;

        @Schema(description = "进入人数警告阈值（百分比）", example = "70")
        private Double entryWarningThreshold;

        @Schema(description = "进入人数严重阈值（百分比）", example = "85")
        private Double entryCriticalThreshold;

        @Schema(description = "进入人数紧急阈值（百分比）", example = "95")
        private Double entryEmergencyThreshold;

        // Getters and Setters
        public Double getCapacityWarningThreshold() { return capacityWarningThreshold; }
        public void setCapacityWarningThreshold(Double capacityWarningThreshold) { this.capacityWarningThreshold = capacityWarningThreshold; }

        public Double getCapacityCriticalThreshold() { return capacityCriticalThreshold; }
        public void setCapacityCriticalThreshold(Double capacityCriticalThreshold) { this.capacityCriticalThreshold = capacityCriticalThreshold; }

        public Double getCapacityEmergencyThreshold() { return capacityEmergencyThreshold; }
        public void setCapacityEmergencyThreshold(Double capacityEmergencyThreshold) { this.capacityEmergencyThreshold = capacityEmergencyThreshold; }

        public Double getEntryWarningThreshold() { return entryWarningThreshold; }
        public void setEntryWarningThreshold(Double entryWarningThreshold) { this.entryWarningThreshold = entryWarningThreshold; }

        public Double getEntryCriticalThreshold() { return entryCriticalThreshold; }
        public void setEntryCriticalThreshold(Double entryCriticalThreshold) { this.entryCriticalThreshold = entryCriticalThreshold; }

        public Double getEntryEmergencyThreshold() { return entryEmergencyThreshold; }
        public void setEntryEmergencyThreshold(Double entryEmergencyThreshold) { this.entryEmergencyThreshold = entryEmergencyThreshold; }
    }

    // 内部类：容量历史数据
    @Schema(description = "容量历史数据")
    public static class CapacityHistoryVO {
        @Schema(description = "时间点", example = "2025-11-25T10:00:00")
        private LocalDateTime timestamp;

        @Schema(description = "人数", example = "80")
        private Integer occupancy;

        @Schema(description = "进入人数", example = "30")
        private Integer entryCount;

        @Schema(description = "离开人数", example = "25")
        private Integer exitCount;

        @Schema(description = "容量利用率", example = "80.0")
        private Double utilizationRate;

        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Integer getOccupancy() { return occupancy; }
        public void setOccupancy(Integer occupancy) { this.occupancy = occupancy; }

        public Integer getEntryCount() { return entryCount; }
        public void setEntryCount(Integer entryCount) { this.entryCount = entryCount; }

        public Integer getExitCount() { return exitCount; }
        public void setExitCount(Integer exitCount) { this.exitCount = exitCount; }

        public Double getUtilizationRate() { return utilizationRate; }
        public void setUtilizationRate(Double utilizationRate) { this.utilizationRate = utilizationRate; }
    }

    // 内部类：区域内人员信息
    @Schema(description = "区域内人员信息")
    public static class PersonInAreaVO {
        @Schema(description = "人员ID", example = "12345")
        private Long personId;

        @Schema(description = "人员姓名", example = "张三")
        private String personName;

        @Schema(description = "人员类型", example = "EMPLOYEE")
        private String personType;

        @Schema(description = "进入时间", example = "2025-11-25T08:30:00")
        private LocalDateTime entryTime;

        @Schema(description = "停留时长（分钟）", example = "120")
        private Integer stayDuration;

        @Schema(description = "访问权限级别", example = "3")
        private Integer accessLevel;

        @Schema(description = "是否访客", example = "false")
        private Boolean isVisitor;

        @Schema(description = "访问事由", example = "工作")
        private String visitPurpose;

        // Getters and Setters
        public Long getPersonId() { return personId; }
        public void setPersonId(Long personId) { this.personId = personId; }

        public String getPersonName() { return personName; }
        public void setPersonName(String personName) { this.personName = personName; }

        public String getPersonType() { return personType; }
        public void setPersonType(String personType) { this.personType = personType; }

        public LocalDateTime getEntryTime() { return entryTime; }
        public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

        public Integer getStayDuration() { return stayDuration; }
        public void setStayDuration(Integer stayDuration) { this.stayDuration = stayDuration; }

        public Integer getAccessLevel() { return accessLevel; }
        public void setAccessLevel(Integer accessLevel) { this.accessLevel = accessLevel; }

        public Boolean getIsVisitor() { return isVisitor; }
        public void setIsVisitor(Boolean isVisitor) { this.isVisitor = isVisitor; }

        public String getVisitPurpose() { return visitPurpose; }
        public void setVisitPurpose(String visitPurpose) { this.visitPurpose = visitPurpose; }
    }

    // 内部类：容量预测
    @Schema(description = "容量预测")
    public static class CapacityForecastVO {
        @Schema(description = "预测时间", example = "2025-11-25T11:00:00")
        private LocalDateTime forecastTime;

        @Schema(description = "预测人数", example = "85")
        private Integer forecastOccupancy;

        @Schema(description = "预测容量利用率", example = "85.0")
        private Double forecastUtilization;

        @Schema(description = "置信度", example = "0.85")
        private Double confidence;

        @Schema(description = "预测模型", example = "LINEAR_REGRESSION")
        private String forecastModel;

        // Getters and Setters
        public LocalDateTime getForecastTime() { return forecastTime; }
        public void setForecastTime(LocalDateTime forecastTime) { this.forecastTime = forecastTime; }

        public Integer getForecastOccupancy() { return forecastOccupancy; }
        public void setForecastOccupancy(Integer forecastOccupancy) { this.forecastOccupancy = forecastOccupancy; }

        public Double getForecastUtilization() { return forecastUtilization; }
        public void setForecastUtilization(Double forecastUtilization) { this.forecastUtilization = forecastUtilization; }

        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }

        public String getForecastModel() { return forecastModel; }
        public void setForecastModel(String forecastModel) { this.forecastModel = forecastModel; }
    }

    // 内部类：高峰时段统计
    @Schema(description = "高峰时段统计")
    public static class PeakHourVO {
        @Schema(description = "时段开始", example = "09:00")
        private String timeSlot;

        @Schema(description = "平均人数", example = "90")
        private Integer averageOccupancy;

        @Schema(description = "最大人数", example = "100")
        private Integer maxOccupancy;

        @Schema(description = "最小人数", example = "70")
        private Integer minOccupancy;

        @Schema(description = "出现频率", example = "0.8")
        private Double frequency;

        @Schema(description = "高峰类型", example = "MORNING_PEAK")
        private String peakType;

        // Getters and Setters
        public String getTimeSlot() { return timeSlot; }
        public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

        public Integer getAverageOccupancy() { return averageOccupancy; }
        public void setAverageOccupancy(Integer averageOccupancy) { this.averageOccupancy = averageOccupancy; }

        public Integer getMaxOccupancy() { return maxOccupancy; }
        public void setMaxOccupancy(Integer maxOccupancy) { this.maxOccupancy = maxOccupancy; }

        public Integer getMinOccupancy() { return minOccupancy; }
        public void setMinOccupancy(Integer minOccupancy) { this.minOccupancy = minOccupancy; }

        public Double getFrequency() { return frequency; }
        public void setFrequency(Double frequency) { this.frequency = frequency; }

        public String getPeakType() { return peakType; }
        public void setPeakType(String peakType) { this.peakType = peakType; }
    }

    // 内部类：区域限制配置
    @Schema(description = "区域限制配置")
    public static class AreaLimitVO {
        @Schema(description = "是否启用人数限制", example = "true")
        private Boolean occupancyLimitEnabled;

        @Schema(description = "是否启用时段限制", example = "true")
        private Boolean timeSlotLimitEnabled;

        @Schema(description = "是否启用权限等级限制", example = "false")
        private Boolean accessLevelLimitEnabled;

        @Schema(description = "是否启用访客限制", example = "true")
        private Boolean visitorLimitEnabled;

        @Schema(description = "访客最大人数", example = "20")
        private Integer maxVisitorCount;

        @Schema(description = "最小访问权限等级", example = "2")
        private Integer minAccessLevel;

        @Schema(description = "受限时段", example = "22:00-06:00")
        private List<String> restrictedTimeSlots;

        // Getters and Setters
        public Boolean getOccupancyLimitEnabled() { return occupancyLimitEnabled; }
        public void setOccupancyLimitEnabled(Boolean occupancyLimitEnabled) { this.occupancyLimitEnabled = occupancyLimitEnabled; }

        public Boolean getTimeSlotLimitEnabled() { return timeSlotLimitEnabled; }
        public void setTimeSlotLimitEnabled(Boolean timeSlotLimitEnabled) { this.timeSlotLimitEnabled = timeSlotLimitEnabled; }

        public Boolean getAccessLevelLimitEnabled() { return accessLevelLimitEnabled; }
        public void setAccessLevelLimitEnabled(Boolean accessLevelLimitEnabled) { this.accessLevelLimitEnabled = accessLevelLimitEnabled; }

        public Boolean getVisitorLimitEnabled() { return visitorLimitEnabled; }
        public void setVisitorLimitEnabled(Boolean visitorLimitEnabled) { this.visitorLimitEnabled = visitorLimitEnabled; }

        public Integer getMaxVisitorCount() { return maxVisitorCount; }
        public void setMaxVisitorCount(Integer maxVisitorCount) { this.maxVisitorCount = maxVisitorCount; }

        public Integer getMinAccessLevel() { return minAccessLevel; }
        public void setMinAccessLevel(Integer minAccessLevel) { this.minAccessLevel = minAccessLevel; }

        public List<String> getRestrictedTimeSlots() { return restrictedTimeSlots; }
        public void setRestrictedTimeSlots(List<String> restrictedTimeSlots) { this.restrictedTimeSlots = restrictedTimeSlots; }
    }

    // 内部类：告警信息
    @Schema(description = "告警信息")
    public static class CapacityAlertVO {
        @Schema(description = "告警ID", example = "1001")
        private Long alertId;

        @Schema(description = "告警类型", example = "CAPACITY_OVERFLOW")
        private String alertType;

        @Schema(description = "告警级别", example = "WARNING")
        private String alertLevel;

        @Schema(description = "告警消息", example = "区域人数已超过警告阈值")
        private String alertMessage;

        @Schema(description = "告警时间", example = "2025-11-25T10:15:00")
        private LocalDateTime alertTime;

        @Schema(description = "是否已处理", example = "false")
        private Boolean handled;

        @Schema(description = "处理时间", example = "2025-11-25T10:20:00")
        private LocalDateTime handledTime;

        @Schema(description = "处理人", example = "admin")
        private String handledBy;

        // Getters and Setters
        public Long getAlertId() { return alertId; }
        public void setAlertId(Long alertId) { this.alertId = alertId; }

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }

        public String getAlertLevel() { return alertLevel; }
        public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

        public String getAlertMessage() { return alertMessage; }
        public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

        public LocalDateTime getAlertTime() { return alertTime; }
        public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }

        public Boolean getHandled() { return handled; }
        public void setHandled(Boolean handled) { this.handled = handled; }

        public LocalDateTime getHandledTime() { return handledTime; }
        public void setHandledTime(LocalDateTime handledTime) { this.handledTime = handledTime; }

        public String getHandledBy() { return handledBy; }
        public void setHandledBy(String handledBy) { this.handledBy = handledBy; }
    }

    // 主要类的Getters and Setters
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }

    public Integer getDesignCapacity() { return designCapacity; }
    public void setDesignCapacity(Integer designCapacity) { this.designCapacity = designCapacity; }

    public Integer getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(Integer currentOccupancy) { this.currentOccupancy = currentOccupancy; }

    public Integer getMaxSimultaneousEntry() { return maxSimultaneousEntry; }
    public void setMaxSimultaneousEntry(Integer maxSimultaneousEntry) { this.maxSimultaneousEntry = maxSimultaneousEntry; }

    public Integer getCurrentEntryCount() { return currentEntryCount; }
    public void setCurrentEntryCount(Integer currentEntryCount) { this.currentEntryCount = currentEntryCount; }

    public Double getCapacityUtilizationRate() { return capacityUtilizationRate; }
    public void setCapacityUtilizationRate(Double capacityUtilizationRate) { this.capacityUtilizationRate = capacityUtilizationRate; }

    public Double getEntryUtilizationRate() { return entryUtilizationRate; }
    public void setEntryUtilizationRate(Double entryUtilizationRate) { this.entryUtilizationRate = entryUtilizationRate; }

    public Boolean getCapacityAlert() { return capacityAlert; }
    public void setCapacityAlert(Boolean capacityAlert) { this.capacityAlert = capacityAlert; }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public AlertThresholdVO getAlertThresholds() { return alertThresholds; }
    public void setAlertThresholds(AlertThresholdVO alertThresholds) { this.alertThresholds = alertThresholds; }

    public List<CapacityHistoryVO> getCapacityHistory() { return capacityHistory; }
    public void setCapacityHistory(List<CapacityHistoryVO> capacityHistory) { this.capacityHistory = capacityHistory; }

    public List<PersonInAreaVO> getCurrentPersons() { return currentPersons; }
    public void setCurrentPersons(List<PersonInAreaVO> currentPersons) { this.currentPersons = currentPersons; }

    public List<CapacityForecastVO> getCapacityForecast() { return capacityForecast; }
    public void setCapacityForecast(List<CapacityForecastVO> capacityForecast) { this.capacityForecast = capacityForecast; }

    public List<PeakHourVO> getPeakHourStatistics() { return peakHourStatistics; }
    public void setPeakHourStatistics(List<PeakHourVO> peakHourStatistics) { this.peakHourStatistics = peakHourStatistics; }

    public AreaLimitVO getAreaLimits() { return areaLimits; }
    public void setAreaLimits(AreaLimitVO areaLimits) { this.areaLimits = areaLimits; }

    public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }

    public String getStatisticsTimeRange() { return statisticsTimeRange; }
    public void setStatisticsTimeRange(String statisticsTimeRange) { this.statisticsTimeRange = statisticsTimeRange; }

    public List<CapacityAlertVO> getAlerts() { return alerts; }
    public void setAlerts(List<CapacityAlertVO> alerts) { this.alerts = alerts; }
}