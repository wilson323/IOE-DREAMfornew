package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁人员追踪VO
 * 基于现有监控系统和区域管理功能的人员轨迹追踪
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁人员追踪VO")
public class PersonTrackingVO {

    /**
     * 追踪ID
     */
    @Schema(description = "追踪ID", example = "1001")
    private Long trackingId;

    /**
     * 追踪编号
     */
    @Schema(description = "追踪编号", example = "PT20251125001")
    private String trackingCode;

    /**
     * 人员ID
     */
    @Schema(description = "人员ID", example = "12345")
    private Long personId;

    /**
     * 人员姓名
     */
    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    /**
     * 人员类型
     * EMPLOYEE-员工
     * CONTRACTOR-承包商
     * VISITOR-访客
     * TEMPORARY-临时人员
     * SECURITY-安保人员
     */
    @Schema(description = "人员类型", example = "EMPLOYEE")
    private String personType;

    /**
     * 工号/证件号
     */
    @Schema(description = "工号/证件号", example = "EMP001")
    private String employeeNumber;

    /**
     * 卡号/凭证号
     */
    @Schema(description = "卡号/凭证号", example = "1234567890")
    private String credentialNumber;

    /**
     * 部门
     */
    @Schema(description = "部门", example = "技术部")
    private String department;

    /**
     * 职位
     */
    @Schema(description = "职位", example = "软件工程师")
    private String position;

    /**
     * 追踪开始时间
     */
    @Schema(description = "追踪开始时间", example = "2025-11-25T08:00:00")
    private LocalDateTime trackingStartTime;

    /**
     * 追踪结束时间
     */
    @Schema(description = "追踪结束时间", example = "2025-11-25T18:00:00")
    private LocalDateTime trackingEndTime;

    /**
     * 追踪总时长（分钟）
     */
    @Schema(description = "追踪总时长", example = "600")
    private Integer totalTrackingMinutes;

    /**
     * 当前位置
     */
    @Schema(description = "当前位置", example = "办公区域A")
    private String currentLocation;

    /**
     * 当前区域ID
     */
    @Schema(description = "当前区域ID", example = "1")
    private Long currentAreaId;

    /**
     * 当前状态
     * IN_AREA-在区域内
     * OUT_AREA-离开区域
     * MOVING-移动中
     */
    @Schema(description = "当前状态", example = "IN_AREA")
    private String currentStatus;

    /**
     * 最后访问时间
     */
    @Schema(description = "最后访问时间", example = "2025-11-25T17:45:00")
    private LocalDateTime lastAccessTime;

    /**
     * 最后访问设备
     */
    @Schema(description = "最后访问设备", example = "东门门禁设备")
    private String lastAccessDevice;

    /**
     * 今日访问次数
     */
    @Schema(description = "今日访问次数", example = "8")
    private Integer todayAccessCount;

    /**
     * 今日访问区域数
     */
    @Schema(description = "今日访问区域数", example = "4")
    private Integer todayAreaCount;

    /**
     * 轨迹点列表
     */
    @Schema(description = "轨迹点列表")
    private List<TrackingPointVO> trackingPoints;

    /**
     * 异常行为记录
     */
    @Schema(description = "异常行为记录")
    private List<AbnormalBehaviorVO> abnormalBehaviors;

    /**
     * 关联人员列表
     */
    @Schema(description = "关联人员列表")
    private List<AssociatedPersonVO> associatedPersons;

    /**
     * 活动热力图数据
     */
    @Schema(description = "活动热力图数据")
    private List<HeatmapPointVO> heatmapData;

    /**
     * 时间分布统计
     */
    @Schema(description = "时间分布统计")
    private List<TimeDistributionVO> timeDistribution;

    /**
     * 追踪配置
     */
    @Schema(description = "追踪配置")
    private TrackingConfigVO trackingConfig;

    /**
     * 附加信息
     */
    @Schema(description = "附加信息")
    private Map<String, Object> additionalInfo;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-11-25T08:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-11-25T17:45:00")
    private LocalDateTime updateTime;

    // 内部类：轨迹点
    @Schema(description = "轨迹点")
    public static class TrackingPointVO {
        @Schema(description = "轨迹点ID", example = "1")
        private Integer pointId;

        @Schema(description = "时间", example = "2025-11-25T08:30:00")
        private LocalDateTime timestamp;

        @Schema(description = "设备ID", example = "1001")
        private Long deviceId;

        @Schema(description = "设备名称", example = "主门禁设备")
        private String deviceName;

        @Schema(description = "区域ID", example = "1")
        private Long areaId;

        @Schema(description = "区域名称", example = "办公区域")
        private String areaName;

        @Schema(description = "访问类型", example = "ENTER")
        private String accessType;

        @Schema(description = "访问结果", example = "SUCCESS")
        private String accessResult;

        @Schema(description = "凭证类型", example = "CARD")
        private String credentialType;

        @Schema(description = "停留时长（分钟）", example = "30")
        private Integer stayDuration;

        @Schema(description = "坐标X", example = "120.5")
        private Double coordinateX;

        @Schema(description = "坐标Y", example = "240.8")
        private Double coordinateY;

        @Schema(description = "楼层", example = "1")
        private Integer floor;

        @Schema(description = "建筑", example = "A栋")
        private String building;

        // Getters and Setters
        public Integer getPointId() { return pointId; }
        public void setPointId(Integer pointId) { this.pointId = pointId; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }

        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }

        public String getAccessType() { return accessType; }
        public void setAccessType(String accessType) { this.accessType = accessType; }

        public String getAccessResult() { return accessResult; }
        public void setAccessResult(String accessResult) { this.accessResult = accessResult; }

        public String getCredentialType() { return credentialType; }
        public void setCredentialType(String credentialType) { this.credentialType = credentialType; }

        public Integer getStayDuration() { return stayDuration; }
        public void setStayDuration(Integer stayDuration) { this.stayDuration = stayDuration; }

        public Double getCoordinateX() { return coordinateX; }
        public void setCoordinateX(Double coordinateX) { this.coordinateX = coordinateX; }

        public Double getCoordinateY() { return coordinateY; }
        public void setCoordinateY(Double coordinateY) { this.coordinateY = coordinateY; }

        public Integer getFloor() { return floor; }
        public void setFloor(Integer floor) { this.floor = floor; }

        public String getBuilding() { return building; }
        public void setBuilding(String building) { this.building = building; }
    }

    // 内部类：异常行为
    @Schema(description = "异常行为")
    public static class AbnormalBehaviorVO {
        @Schema(description = "行为ID", example = "1")
        private Integer behaviorId;

        @Schema(description = "行为时间", example = "2025-11-25T14:30:00")
        private LocalDateTime behaviorTime;

        @Schema(description = "行为类型", example = "FREQUENT_ACCESS")
        private String behaviorType;

        @Schema(description = "行为描述", example = "短时间内频繁进出同一区域")
        private String behaviorDescription;

        @Schema(description = "风险级别", example = "MEDIUM")
        private String riskLevel;

        @Schema(description = "发生位置", example = "办公区域A")
        private String location;

        @Schema(description = "是否已处理", example = "false")
        private Boolean handled;

        // Getters and Setters
        public Integer getBehaviorId() { return behaviorId; }
        public void setBehaviorId(Integer behaviorId) { this.behaviorId = behaviorId; }

        public LocalDateTime getBehaviorTime() { return behaviorTime; }
        public void setBehaviorTime(LocalDateTime behaviorTime) { this.behaviorTime = behaviorTime; }

        public String getBehaviorType() { return behaviorType; }
        public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }

        public String getBehaviorDescription() { return behaviorDescription; }
        public void setBehaviorDescription(String behaviorDescription) { this.behaviorDescription = behaviorDescription; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Boolean getHandled() { return handled; }
        public void setHandled(Boolean handled) { this.handled = handled; }
    }

    // 内部类：关联人员
    @Schema(description = "关联人员")
    public static class AssociatedPersonVO {
        @Schema(description = "关联人员ID", example = "12346")
        private Long associatedPersonId;

        @Schema(description = "关联人员姓名", example = "李四")
        private String associatedPersonName;

        @Schema(description = "关联关系", example = "同事")
        private String relationship;

        @Schema(description = "关联时间", example = "2025-11-25T12:30:00")
        private LocalDateTime associationTime;

        @Schema(description = "关联位置", example = "餐厅")
        private String associationLocation;

        @Schema(description = "关联时长（分钟）", example = "45")
        private Integer associationDuration;

        // Getters and Setters
        public Long getAssociatedPersonId() { return associatedPersonId; }
        public void setAssociatedPersonId(Long associatedPersonId) { this.associatedPersonId = associatedPersonId; }

        public String getAssociatedPersonName() { return associatedPersonName; }
        public void setAssociatedPersonName(String associatedPersonName) { this.associatedPersonName = associatedPersonName; }

        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }

        public LocalDateTime getAssociationTime() { return associationTime; }
        public void setAssociationTime(LocalDateTime associationTime) { this.associationTime = associationTime; }

        public String getAssociationLocation() { return associationLocation; }
        public void setAssociationLocation(String associationLocation) { this.associationLocation = associationLocation; }

        public Integer getAssociationDuration() { return associationDuration; }
        public void setAssociationDuration(Integer associationDuration) { this.associationDuration = associationDuration; }
    }

    // 内部类：热力图点
    @Schema(description = "活动热力图点")
    public static class HeatmapPointVO {
        @Schema(description = "坐标X", example = "120.5")
        private Double coordinateX;

        @Schema(description = "坐标Y", example = "240.8")
        private Double coordinateY;

        @Schema(description = "热力值", example = "0.85")
        private Double heatValue;

        @Schema(description = "访问次数", example = "15")
        private Integer visitCount;

        @Schema(description = "停留总时长", example = "180")
        private Integer totalStayMinutes;

        @Schema(description = "区域名称", example = "办公区域A")
        private String areaName;

        // Getters and Setters
        public Double getCoordinateX() { return coordinateX; }
        public void setCoordinateX(Double coordinateX) { this.coordinateX = coordinateX; }

        public Double getCoordinateY() { return coordinateY; }
        public void setCoordinateY(Double coordinateY) { this.coordinateY = coordinateY; }

        public Double getHeatValue() { return heatValue; }
        public void setHeatValue(Double heatValue) { this.heatValue = heatValue; }

        public Integer getVisitCount() { return visitCount; }
        public void setVisitCount(Integer visitCount) { this.visitCount = visitCount; }

        public Integer getTotalStayMinutes() { return totalStayMinutes; }
        public void setTotalStayMinutes(Integer totalStayMinutes) { this.totalStayMinutes = totalStayMinutes; }

        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
    }

    // 内部类：时间分布
    @Schema(description = "时间分布统计")
    public static class TimeDistributionVO {
        @Schema(description = "时间段", example = "08:00-09:00")
        private String timeSlot;

        @Schema(description = "访问次数", example = "2")
        private Integer accessCount;

        @Schema(description = "停留时长", example = "45")
        private Integer stayMinutes;

        @Schema(description = "访问区域数", example = "1")
        private Integer areaCount;

        @Schema(description = "活跃度", example = "HIGH")
        private String activityLevel;

        // Getters and Setters
        public String getTimeSlot() { return timeSlot; }
        public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

        public Integer getAccessCount() { return accessCount; }
        public void setAccessCount(Integer accessCount) { this.accessCount = accessCount; }

        public Integer getStayMinutes() { return stayMinutes; }
        public void setStayMinutes(Integer stayMinutes) { this.stayMinutes = stayMinutes; }

        public Integer getAreaCount() { return areaCount; }
        public void setAreaCount(Integer areaCount) { this.areaCount = areaCount; }

        public String getActivityLevel() { return activityLevel; }
        public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    }

    // 内部类：追踪配置
    @Schema(description = "追踪配置")
    public static class TrackingConfigVO {
        @Schema(description = "追踪启用", example = "true")
        private Boolean trackingEnabled;

        @Schema(description = "数据保留天数", example = "30")
        private Integer dataRetentionDays;

        @Schema(description = "异常行为检测启用", example = "true")
        private Boolean abnormalBehaviorDetectionEnabled;

        @Schema(description = "关联人员分析启用", example = "true")
        private Boolean associatedPersonAnalysisEnabled;

        @Schema(description = "热力图生成启用", example = "true")
        private Boolean heatmapGenerationEnabled;

        @Schema(description = "实时位置更新间隔", example = "60")
        private Integer realtimeUpdateInterval;

        // Getters and Setters
        public Boolean getTrackingEnabled() { return trackingEnabled; }
        public void setTrackingEnabled(Boolean trackingEnabled) { this.trackingEnabled = trackingEnabled; }

        public Integer getDataRetentionDays() { return dataRetentionDays; }
        public void setDataRetentionDays(Integer dataRetentionDays) { this.dataRetentionDays = dataRetentionDays; }

        public Boolean getAbnormalBehaviorDetectionEnabled() { return abnormalBehaviorDetectionEnabled; }
        public void setAbnormalBehaviorDetectionEnabled(Boolean abnormalBehaviorDetectionEnabled) { this.abnormalBehaviorDetectionEnabled = abnormalBehaviorDetectionEnabled; }

        public Boolean getAssociatedPersonAnalysisEnabled() { return associatedPersonAnalysisEnabled; }
        public void setAssociatedPersonAnalysisEnabled(Boolean associatedPersonAnalysisEnabled) { this.associatedPersonAnalysisEnabled = associatedPersonAnalysisEnabled; }

        public Boolean getHeatmapGenerationEnabled() { return heatmapGenerationEnabled; }
        public void setHeatmapGenerationEnabled(Boolean heatmapGenerationEnabled) { this.heatmapGenerationEnabled = heatmapGenerationEnabled; }

        public Integer getRealtimeUpdateInterval() { return realtimeUpdateInterval; }
        public void setRealtimeUpdateInterval(Integer realtimeUpdateInterval) { this.realtimeUpdateInterval = realtimeUpdateInterval; }
    }

    // 主要类的Getters and Setters
    public Long getTrackingId() { return trackingId; }
    public void setTrackingId(Long trackingId) { this.trackingId = trackingId; }

    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public String getPersonType() { return personType; }
    public void setPersonType(String personType) { this.personType = personType; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public String getCredentialNumber() { return credentialNumber; }
    public void setCredentialNumber(String credentialNumber) { this.credentialNumber = credentialNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public LocalDateTime getTrackingStartTime() { return trackingStartTime; }
    public void setTrackingStartTime(LocalDateTime trackingStartTime) { this.trackingStartTime = trackingStartTime; }

    public LocalDateTime getTrackingEndTime() { return trackingEndTime; }
    public void setTrackingEndTime(LocalDateTime trackingEndTime) { this.trackingEndTime = trackingEndTime; }

    public Integer getTotalTrackingMinutes() { return totalTrackingMinutes; }
    public void setTotalTrackingMinutes(Integer totalTrackingMinutes) { this.totalTrackingMinutes = totalTrackingMinutes; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public Long getCurrentAreaId() { return currentAreaId; }
    public void setCurrentAreaId(Long currentAreaId) { this.currentAreaId = currentAreaId; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }

    public String getLastAccessDevice() { return lastAccessDevice; }
    public void setLastAccessDevice(String lastAccessDevice) { this.lastAccessDevice = lastAccessDevice; }

    public Integer getTodayAccessCount() { return todayAccessCount; }
    public void setTodayAccessCount(Integer todayAccessCount) { this.todayAccessCount = todayAccessCount; }

    public Integer getTodayAreaCount() { return todayAreaCount; }
    public void setTodayAreaCount(Integer todayAreaCount) { this.todayAreaCount = todayAreaCount; }

    public List<TrackingPointVO> getTrackingPoints() { return trackingPoints; }
    public void setTrackingPoints(List<TrackingPointVO> trackingPoints) { this.trackingPoints = trackingPoints; }

    public List<AbnormalBehaviorVO> getAbnormalBehaviors() { return abnormalBehaviors; }
    public void setAbnormalBehaviors(List<AbnormalBehaviorVO> abnormalBehaviors) { this.abnormalBehaviors = abnormalBehaviors; }

    public List<AssociatedPersonVO> getAssociatedPersons() { return associatedPersons; }
    public void setAssociatedPersons(List<AssociatedPersonVO> associatedPersons) { this.associatedPersons = associatedPersons; }

    public List<HeatmapPointVO> getHeatmapData() { return heatmapData; }
    public void setHeatmapData(List<HeatmapPointVO> heatmapData) { this.heatmapData = heatmapData; }

    public List<TimeDistributionVO> getTimeDistribution() { return timeDistribution; }
    public void setTimeDistribution(List<TimeDistributionVO> timeDistribution) { this.timeDistribution = timeDistribution; }

    public TrackingConfigVO getTrackingConfig() { return trackingConfig; }
    public void setTrackingConfig(TrackingConfigVO trackingConfig) { this.trackingConfig = trackingConfig; }

    public Map<String, Object> getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(Map<String, Object> additionalInfo) { this.additionalInfo = additionalInfo; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}