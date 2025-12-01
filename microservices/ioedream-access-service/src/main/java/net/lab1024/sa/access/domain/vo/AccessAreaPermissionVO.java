package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁区域访问权限VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁区域访问权限VO")
public class AccessAreaPermissionVO {

    /**
     * 权限ID
     */
    @Schema(description = "权限ID", example = "1001")
    private Long permissionId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "12345")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 用户类型
     * EMPLOYEE-员工
     * CONTRACTOR-承包商
     * VISITOR-访客
     * TEMPORARY-临时人员
     * SECURITY-安保人员
     */
    @Schema(description = "用户类型", example = "EMPLOYEE")
    private String userType;

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
     * 权限级别
     * 1-最低级别
     * 2-普通级别
     * 3-中等级别
     * 4-高级级别
     * 5-最高级别
     */
    @Schema(description = "权限级别", example = "3")
    private Integer permissionLevel;

    /**
     * 权限状态
     * ACTIVE-激活
     * INACTIVE-停用
     * SUSPENDED-暂停
     * EXPIRED-已过期
     * REVOKED-已撤销
     */
    @Schema(description = "权限状态", example = "ACTIVE")
    private String permissionStatus;

    /**
     * 访问模式
     * PERMANENT-永久权限
     * TEMPORARY-临时权限
     * SCHEDULED-定时权限
     * CONDITIONAL-条件权限
     */
    @Schema(description = "访问模式", example = "PERMANENT")
    private String accessMode;

    /**
     * 访问时段限制
     */
    @Schema(description = "访问时段限制")
    private List<TimeSlotVO> allowedTimeSlots;

    /**
     * 特殊日期权限
     */
    @Schema(description = "特殊日期权限")
    private Map<String, SpecialDateVO> specialDatePermissions;

    /**
     * 权限开始时间
     */
    @Schema(description = "权限开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime effectiveStartTime;

    /**
     * 权限结束时间
     */
    @Schema(description = "权限结束时间", example = "2025-12-31T23:59:59")
    private LocalDateTime effectiveEndTime;

    /**
     * 允许访问的设备列表
     */
    @Schema(description = "允许访问的设备列表")
    private List<Long> allowedDeviceIds;

    /**
     * 访问方式限制
     */
    @Schema(description = "访问方式限制")
    private List<String> accessMethods;

    /**
     * 是否允许远程开门
     */
    @Schema(description = "是否允许远程开门", example = "false")
    private Boolean remoteOpenAllowed;

    /**
     * 是否允许带访客
     */
    @Schema(description = "是否允许带访客", example = "true")
    private Boolean visitorAllowed;

    /**
     * 最大访客人数
     */
    @Schema(description = "最大访客人数", example = "2")
    private Integer maxVisitorCount;

    /**
     * 访客陪同要求
     */
    @Schema(description = "访客陪同要求", example = "true")
    private Boolean escortRequired;

    /**
     * 是否需要审批
     */
    @Schema(description = "是否需要审批", example = "false")
    private Boolean approvalRequired;

    /**
     * 审批人列表
     */
    @Schema(description = "审批人列表")
    private List<Long> approverIds;

    /**
     * 最后访问时间
     */
    @Schema(description = "最后访问时间", example = "2025-11-25T08:30:00")
    private LocalDateTime lastAccessTime;

    /**
     * 访问次数统计
     */
    @Schema(description = "访问次数统计", example = "150")
    private Integer accessCount;

    /**
     * 本月访问次数
     */
    @Schema(description = "本月访问次数", example = "25")
    private Integer monthlyAccessCount;

    /**
     * 异常访问次数
     */
    @Schema(description = "异常访问次数", example = "2")
    private Integer abnormalAccessCount;

    /**
     * 权限创建时间
     */
    @Schema(description = "权限创建时间", example = "2025-01-01T10:00:00")
    private LocalDateTime createTime;

    /**
     * 权限更新时间
     */
    @Schema(description = "权限更新时间", example = "2025-11-20T15:30:00")
    private LocalDateTime updateTime;

    /**
     * 权限创建人
     */
    @Schema(description = "权限创建人", example = "admin")
    private String createBy;

    /**
     * 权限更新人
     */
    @Schema(description = "权限更新人", example = "manager")
    private String updateBy;

    /**
     * 权限备注
     */
    @Schema(description = "权限备注", example = "办公区域标准访问权限")
    private String remarks;

    /**
     * 权限标签
     */
    @Schema(description = "权限标签", example = "正式员工")
    private List<String> permissionTags;

    /**
     * 关联的业务部门
     */
    @Schema(description = "关联的业务部门", example = "技术部")
    private String department;

    /**
     * 关联的职位
     */
    @Schema(description = "关联的职位", example = "软件工程师")
    private String position;

    /**
     * 权限来源
     * MANUAL-手动分配
     * AUTO-自动分配
     * IMPORT-批量导入
     * INHERIT-继承获得
     */
    @Schema(description = "权限来源", example = "AUTO")
    private String permissionSource;

    /**
     * 权限变更历史
     */
    @Schema(description = "权限变更历史")
    private List<PermissionHistoryVO> changeHistory;

    // 内部类：访问时段
    @Schema(description = "访问时段")
    public static class TimeSlotVO {
        @Schema(description = "开始时间", example = "08:00")
        private String startTime;

        @Schema(description = "结束时间", example = "18:00")
        private String endTime;

        @Schema(description = "星期几", example = "1,2,3,4,5")
        private String weekdays;

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;

        // Getters and Setters
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }

        public String getWeekdays() { return weekdays; }
        public void setWeekdays(String weekdays) { this.weekdays = weekdays; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }

    // 内部类：特殊日期权限
    @Schema(description = "特殊日期权限")
    public static class SpecialDateVO {
        @Schema(description = "是否允许访问", example = "true")
        private Boolean allowed;

        @Schema(description = "特殊时段", example = "")
        private List<TimeSlotVO> specialTimeSlots;

        @Schema(description = "备注", example = "节假日正常访问")
        private String remark;

        // Getters and Setters
        public Boolean getAllowed() { return allowed; }
        public void setAllowed(Boolean allowed) { this.allowed = allowed; }

        public List<TimeSlotVO> getSpecialTimeSlots() { return specialTimeSlots; }
        public void setSpecialTimeSlots(List<TimeSlotVO> specialTimeSlots) { this.specialTimeSlots = specialTimeSlots; }

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    // 内部类：权限变更历史
    @Schema(description = "权限变更历史")
    public static class PermissionHistoryVO {
        @Schema(description = "变更时间", example = "2025-11-20T15:30:00")
        private LocalDateTime changeTime;

        @Schema(description = "变更类型", example = "MODIFY")
        private String changeType;

        @Schema(description = "变更前值", example = "2")
        private String oldValue;

        @Schema(description = "变更后值", example = "3")
        private String newValue;

        @Schema(description = "变更原因", example = "职位晋升")
        private String changeReason;

        @Schema(description = "操作人", example = "manager")
        private String operator;

        // Getters and Setters
        public LocalDateTime getChangeTime() { return changeTime; }
        public void setChangeTime(LocalDateTime changeTime) { this.changeTime = changeTime; }

        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }

        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }

        public String getNewValue() { return newValue; }
        public void setNewValue(String newValue) { this.newValue = newValue; }

        public String getChangeReason() { return changeReason; }
        public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
    }

    // 主要类的Getters and Setters
    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public Integer getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(Integer permissionLevel) { this.permissionLevel = permissionLevel; }

    public String getPermissionStatus() { return permissionStatus; }
    public void setPermissionStatus(String permissionStatus) { this.permissionStatus = permissionStatus; }

    public String getAccessMode() { return accessMode; }
    public void setAccessMode(String accessMode) { this.accessMode = accessMode; }

    public List<TimeSlotVO> getAllowedTimeSlots() { return allowedTimeSlots; }
    public void setAllowedTimeSlots(List<TimeSlotVO> allowedTimeSlots) { this.allowedTimeSlots = allowedTimeSlots; }

    public Map<String, SpecialDateVO> getSpecialDatePermissions() { return specialDatePermissions; }
    public void setSpecialDatePermissions(Map<String, SpecialDateVO> specialDatePermissions) { this.specialDatePermissions = specialDatePermissions; }

    public LocalDateTime getEffectiveStartTime() { return effectiveStartTime; }
    public void setEffectiveStartTime(LocalDateTime effectiveStartTime) { this.effectiveStartTime = effectiveStartTime; }

    public LocalDateTime getEffectiveEndTime() { return effectiveEndTime; }
    public void setEffectiveEndTime(LocalDateTime effectiveEndTime) { this.effectiveEndTime = effectiveEndTime; }

    public List<Long> getAllowedDeviceIds() { return allowedDeviceIds; }
    public void setAllowedDeviceIds(List<Long> allowedDeviceIds) { this.allowedDeviceIds = allowedDeviceIds; }

    public List<String> getAccessMethods() { return accessMethods; }
    public void setAccessMethods(List<String> accessMethods) { this.accessMethods = accessMethods; }

    public Boolean getRemoteOpenAllowed() { return remoteOpenAllowed; }
    public void setRemoteOpenAllowed(Boolean remoteOpenAllowed) { this.remoteOpenAllowed = remoteOpenAllowed; }

    public Boolean getVisitorAllowed() { return visitorAllowed; }
    public void setVisitorAllowed(Boolean visitorAllowed) { this.visitorAllowed = visitorAllowed; }

    public Integer getMaxVisitorCount() { return maxVisitorCount; }
    public void setMaxVisitorCount(Integer maxVisitorCount) { this.maxVisitorCount = maxVisitorCount; }

    public Boolean getEscortRequired() { return escortRequired; }
    public void setEscortRequired(Boolean escortRequired) { this.escortRequired = escortRequired; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public List<Long> getApproverIds() { return approverIds; }
    public void setApproverIds(List<Long> approverIds) { this.approverIds = approverIds; }

    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }

    public Integer getAccessCount() { return accessCount; }
    public void setAccessCount(Integer accessCount) { this.accessCount = accessCount; }

    public Integer getMonthlyAccessCount() { return monthlyAccessCount; }
    public void setMonthlyAccessCount(Integer monthlyAccessCount) { this.monthlyAccessCount = monthlyAccessCount; }

    public Integer getAbnormalAccessCount() { return abnormalAccessCount; }
    public void setAbnormalAccessCount(Integer abnormalAccessCount) { this.abnormalAccessCount = abnormalAccessCount; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public List<String> getPermissionTags() { return permissionTags; }
    public void setPermissionTags(List<String> permissionTags) { this.permissionTags = permissionTags; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getPermissionSource() { return permissionSource; }
    public void setPermissionSource(String permissionSource) { this.permissionSource = permissionSource; }

    public List<PermissionHistoryVO> getChangeHistory() { return changeHistory; }
    public void setChangeHistory(List<PermissionHistoryVO> changeHistory) { this.changeHistory = changeHistory; }
}