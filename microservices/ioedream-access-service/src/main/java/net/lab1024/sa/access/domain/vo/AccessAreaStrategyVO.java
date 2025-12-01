package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁区域策略配置VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁区域策略配置VO")
public class AccessAreaStrategyVO {

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 策略名称
     */
    @Schema(description = "策略名称", example = "办公区域门禁策略")
    private String strategyName;

    /**
     * 策略类型
     * NORMAL-普通策略
     * HIGH_SECURITY-高安全策略
     * EMERGENCY-紧急策略
     * MAINTENANCE-维护策略
     */
    @Schema(description = "策略类型", example = "NORMAL")
    private String strategyType;

    /**
     * 访问控制模式
     * WHITELIST-白名单模式
     * BLACKLIST-黑名单模式
     * TIME_BASED-时段控制模式
     * HYBRID-混合模式
     */
    @Schema(description = "访问控制模式", example = "WHITELIST")
    private String accessMode;

    /**
     * 是否启用身份验证
     */
    @Schema(description = "是否启用身份验证", example = "true")
    private Boolean authenticationEnabled;

    /**
     * 验证方式
     * CARD-刷卡
     * PASSWORD-密码
     * BIOMETRIC-生物特征
     * FACE-人脸识别
     * FINGERPRINT-指纹
     * MULTI_FACTOR-多因子
     */
    @Schema(description = "验证方式", example = "CARD_BIOMETRIC")
    private List<String> verificationMethods;

    /**
     * 是否启用反潜回
     */
    @Schema(description = "是否启用反潜回", example = "true")
    private Boolean antiPassbackEnabled;

    /**
     * 反潜回模式
     * HARD-硬反潜回
     * SOFT-软反潜回
     * TIMED-时限反潜回
     */
    @Schema(description = "反潜回模式", example = "HARD")
    private String antiPassbackMode;

    /**
     * 是否启用胁迫开门
     */
    @Schema(description = "是否启用胁迫开门", example = "true")
    private Boolean duressOpenEnabled;

    /**
     * 胁迫密码/指纹
     */
    @Schema(description = "胁迫密码/指纹", example = "123456")
    private String duressCode;

    /**
     * 是否启用多次验证
     */
    @Schema(description = "是否启用多次验证", example = "false")
    private Boolean multiVerificationEnabled;

    /**
     * 多次验证人数
     */
    @Schema(description = "多次验证人数", example = "2")
    private Integer multiVerificationCount;

    /**
     * 验证时间间隔（秒）
     */
    @Schema(description = "验证时间间隔（秒）", example = "30")
    private Integer verificationInterval;

    /**
     * 是否启用时段控制
     */
    @Schema(description = "是否启用时段控制", example = "true")
    private Boolean timeSlotEnabled;

    /**
     * 允许访问的时间段
     */
    @Schema(description = "允许访问的时间段")
    private List<TimeSlotVO> allowedTimeSlots;

    /**
     * 特殊日期访问控制
     */
    @Schema(description = "特殊日期访问控制")
    private Map<String, SpecialDateVO> specialDateControls;

    /**
     * 最大同时进入人数
     */
    @Schema(description = "最大同时进入人数", example = "50")
    private Integer maxSimultaneousEntry;

    /**
     * 区域容量限制
     */
    @Schema(description = "区域容量限制", example = "100")
    private Integer areaCapacityLimit;

    /**
     * 是否启用容量告警
     */
    @Schema(description = "是否启用容量告警", example = "true")
    private Boolean capacityAlertEnabled;

    /**
     * 容量告警阈值（百分比）
     */
    @Schema(description = "容量告警阈值（百分比）", example = "80")
    private Integer capacityAlertThreshold;

    /**
     * 开门延时时间（秒）
     */
    @Schema(description = "开门延时时间（秒）", example = "3")
    private Integer openDelayTime;

    /**
     * 开门保持时间（秒）
     */
    @Schema(description = "开门保持时间（秒）", example = "5")
    private Integer openHoldTime;

    /**
     * 是否启用远程开门
     */
    @Schema(description = "是否启用远程开门", example = "true")
    private Boolean remoteOpenEnabled;

    /**
     * 远程开门权限等级
     */
    @Schema(description = "远程开门权限等级", example = "3")
    private Integer remoteOpenPermissionLevel;

    /**
     * 是否启用联动控制
     */
    @Schema(description = "是否启用联动控制", example = "true")
    private Boolean linkageControlEnabled;

    /**
     * 联动设备列表
     */
    @Schema(description = "联动设备列表")
    private List<Long> linkageDeviceIds;

    /**
     * 是否启用访客管理
     */
    @Schema(description = "是否启用访客管理", example = "true")
    private Boolean visitorManagementEnabled;

    /**
     * 访客访问时段
     */
    @Schema(description = "访客访问时段")
    private List<TimeSlotVO> visitorTimeSlots;

    /**
     * 访客需要审批
     */
    @Schema(description = "访客需要审批", example = "true")
    private Boolean visitorApprovalRequired;

    /**
     * 访客单次有效期（小时）
     */
    @Schema(description = "访客单次有效期（小时）", example = "8")
    private Integer visitorValidityHours;

    /**
     * 策略优先级
     */
    @Schema(description = "策略优先级", example = "5")
    private Integer priority;

    /**
     * 策略状态
     * ACTIVE-激活
     * INACTIVE-停用
     * PENDING-待审核
     * EXPIRED-已过期
     */
    @Schema(description = "策略状态", example = "ACTIVE")
    private String status;

    /**
     * 生效时间
     */
    @Schema(description = "生效时间", example = "2025-01-01T00:00:00")
    private String effectiveTime;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private String expiryTime;

    /**
     * 策略描述
     */
    @Schema(description = "策略描述", example = "办公区域标准门禁策略")
    private String description;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-11-25T10:30:00")
    private String createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-11-25T10:30:00")
    private String updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "admin")
    private String createBy;

    /**
     * 更新人
     */
    @Schema(description = "更新人", example = "admin")
    private String updateBy;

    // 内部类：时间段配置
    @Schema(description = "时间段配置")
    public static class TimeSlotVO {
        @Schema(description = "开始时间", example = "08:00")
        private LocalTime startTime;

        @Schema(description = "结束时间", example = "18:00")
        private LocalTime endTime;

        @Schema(description = "星期几", example = "1,2,3,4,5")
        private String weekdays;

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;

        // Getters and Setters
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public String getWeekdays() { return weekdays; }
        public void setWeekdays(String weekdays) { this.weekdays = weekdays; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }

    // 内部类：特殊日期配置
    @Schema(description = "特殊日期配置")
    public static class SpecialDateVO {
        @Schema(description = "是否允许访问", example = "false")
        private Boolean allowed;

        @Schema(description = "特殊时段", example = "")
        private List<TimeSlotVO> specialTimeSlots;

        @Schema(description = "备注", example = "节假日")
        private String remark;

        // Getters and Setters
        public Boolean getAllowed() { return allowed; }
        public void setAllowed(Boolean allowed) { this.allowed = allowed; }

        public List<TimeSlotVO> getSpecialTimeSlots() { return specialTimeSlots; }
        public void setSpecialTimeSlots(List<TimeSlotVO> specialTimeSlots) { this.specialTimeSlots = specialTimeSlots; }

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    // Getters and Setters
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }

    public String getStrategyName() { return strategyName; }
    public void setStrategyName(String strategyName) { this.strategyName = strategyName; }

    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }

    public String getAccessMode() { return accessMode; }
    public void setAccessMode(String accessMode) { this.accessMode = accessMode; }

    public Boolean getAuthenticationEnabled() { return authenticationEnabled; }
    public void setAuthenticationEnabled(Boolean authenticationEnabled) { this.authenticationEnabled = authenticationEnabled; }

    public List<String> getVerificationMethods() { return verificationMethods; }
    public void setVerificationMethods(List<String> verificationMethods) { this.verificationMethods = verificationMethods; }

    public Boolean getAntiPassbackEnabled() { return antiPassbackEnabled; }
    public void setAntiPassbackEnabled(Boolean antiPassbackEnabled) { this.antiPassbackEnabled = antiPassbackEnabled; }

    public String getAntiPassbackMode() { return antiPassbackMode; }
    public void setAntiPassbackMode(String antiPassbackMode) { this.antiPassbackMode = antiPassbackMode; }

    public Boolean getDuressOpenEnabled() { return duressOpenEnabled; }
    public void setDuressOpenEnabled(Boolean duressOpenEnabled) { this.duressOpenEnabled = duressOpenEnabled; }

    public String getDuressCode() { return duressCode; }
    public void setDuressCode(String duressCode) { this.duressCode = duressCode; }

    public Boolean getMultiVerificationEnabled() { return multiVerificationEnabled; }
    public void setMultiVerificationEnabled(Boolean multiVerificationEnabled) { this.multiVerificationEnabled = multiVerificationEnabled; }

    public Integer getMultiVerificationCount() { return multiVerificationCount; }
    public void setMultiVerificationCount(Integer multiVerificationCount) { this.multiVerificationCount = multiVerificationCount; }

    public Integer getVerificationInterval() { return verificationInterval; }
    public void setVerificationInterval(Integer verificationInterval) { this.verificationInterval = verificationInterval; }

    public Boolean getTimeSlotEnabled() { return timeSlotEnabled; }
    public void setTimeSlotEnabled(Boolean timeSlotEnabled) { this.timeSlotEnabled = timeSlotEnabled; }

    public List<TimeSlotVO> getAllowedTimeSlots() { return allowedTimeSlots; }
    public void setAllowedTimeSlots(List<TimeSlotVO> allowedTimeSlots) { this.allowedTimeSlots = allowedTimeSlots; }

    public Map<String, SpecialDateVO> getSpecialDateControls() { return specialDateControls; }
    public void setSpecialDateControls(Map<String, SpecialDateVO> specialDateControls) { this.specialDateControls = specialDateControls; }

    public Integer getMaxSimultaneousEntry() { return maxSimultaneousEntry; }
    public void setMaxSimultaneousEntry(Integer maxSimultaneousEntry) { this.maxSimultaneousEntry = maxSimultaneousEntry; }

    public Integer getAreaCapacityLimit() { return areaCapacityLimit; }
    public void setAreaCapacityLimit(Integer areaCapacityLimit) { this.areaCapacityLimit = areaCapacityLimit; }

    public Boolean getCapacityAlertEnabled() { return capacityAlertEnabled; }
    public void setCapacityAlertEnabled(Boolean capacityAlertEnabled) { this.capacityAlertEnabled = capacityAlertEnabled; }

    public Integer getCapacityAlertThreshold() { return capacityAlertThreshold; }
    public void setCapacityAlertThreshold(Integer capacityAlertThreshold) { this.capacityAlertThreshold = capacityAlertThreshold; }

    public Integer getOpenDelayTime() { return openDelayTime; }
    public void setOpenDelayTime(Integer openDelayTime) { this.openDelayTime = openDelayTime; }

    public Integer getOpenHoldTime() { return openHoldTime; }
    public void setOpenHoldTime(Integer openHoldTime) { this.openHoldTime = openHoldTime; }

    public Boolean getRemoteOpenEnabled() { return remoteOpenEnabled; }
    public void setRemoteOpenEnabled(Boolean remoteOpenEnabled) { this.remoteOpenEnabled = remoteOpenEnabled; }

    public Integer getRemoteOpenPermissionLevel() { return remoteOpenPermissionLevel; }
    public void setRemoteOpenPermissionLevel(Integer remoteOpenPermissionLevel) { this.remoteOpenPermissionLevel = remoteOpenPermissionLevel; }

    public Boolean getLinkageControlEnabled() { return linkageControlEnabled; }
    public void setLinkageControlEnabled(Boolean linkageControlEnabled) { this.linkageControlEnabled = linkageControlEnabled; }

    public List<Long> getLinkageDeviceIds() { return linkageDeviceIds; }
    public void setLinkageDeviceIds(List<Long> linkageDeviceIds) { this.linkageDeviceIds = linkageDeviceIds; }

    public Boolean getVisitorManagementEnabled() { return visitorManagementEnabled; }
    public void setVisitorManagementEnabled(Boolean visitorManagementEnabled) { this.visitorManagementEnabled = visitorManagementEnabled; }

    public List<TimeSlotVO> getVisitorTimeSlots() { return visitorTimeSlots; }
    public void setVisitorTimeSlots(List<TimeSlotVO> visitorTimeSlots) { this.visitorTimeSlots = visitorTimeSlots; }

    public Boolean getVisitorApprovalRequired() { return visitorApprovalRequired; }
    public void setVisitorApprovalRequired(Boolean visitorApprovalRequired) { this.visitorApprovalRequired = visitorApprovalRequired; }

    public Integer getVisitorValidityHours() { return visitorValidityHours; }
    public void setVisitorValidityHours(Integer visitorValidityHours) { this.visitorValidityHours = visitorValidityHours; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(String effectiveTime) { this.effectiveTime = effectiveTime; }

    public String getExpiryTime() { return expiryTime; }
    public void setExpiryTime(String expiryTime) { this.expiryTime = expiryTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
}