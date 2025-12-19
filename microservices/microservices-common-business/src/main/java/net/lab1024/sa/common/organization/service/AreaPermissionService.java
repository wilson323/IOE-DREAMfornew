package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.organization.manager.AreaPermissionManager;
import net.lab1024.sa.common.organization.domain.vo.AreaUserPermissionVO;
import net.lab1024.sa.common.organization.domain.vo.AreaPermissionHistoryVO;
import net.lab1024.sa.common.organization.domain.vo.BatchGrantResult;
import net.lab1024.sa.common.organization.domain.vo.AreaPermissionValidationResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 区域权限服务接口
 * <p>
 * 提供区域权限管理的业务服务接口
 * 包括权限分配、撤销、检查、统计等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
public interface AreaPermissionService {

    /**
     * 分配区域权限
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     * @param username 用户名
     * @param realName 真实姓名
     * @param relationType 关联类型
     * @param permissionLevel 权限级别
     * @param effectiveTime 生效时间
     * @param expireTime 失效时间
     * @param allowStartTime 允许开始时间
     * @param allowEndTime 允许结束时间
     * @param workdayOnly 是否仅工作日有效
     * @param accessPermissions 访问权限配置
     * @param extendedAttributes 扩展属性
     * @param remark 备注
     * @return 关联ID
     */
    String grantAreaPermission(Long areaId, Long userId, String username, String realName,
                              Integer relationType, Integer permissionLevel,
                              LocalDateTime effectiveTime, LocalDateTime expireTime,
                              String allowStartTime, String allowEndTime,
                              Boolean workdayOnly, String accessPermissions,
                              String extendedAttributes, String remark);

    /**
     * 撤销区域权限
     *
     * @param relationId 关联ID
     * @return 是否成功
     */
    boolean revokeAreaPermission(String relationId);

    /**
     * 检查区域访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param requiredPermissionLevel 所需权限级别
     * @return 权限检查结果
     */
    AreaPermissionManager.PermissionCheckResult checkAreaPermission(Long userId, Long areaId, Integer requiredPermissionLevel);

    /**
     * 获取用户可访问的区域列表
     *
     * @param userId 用户ID
     * @return 可访问区域ID列表
     */
    List<Long> getUserAccessibleAreas(Long userId);

    /**
     * 获取区域用户权限列表
     *
     * @param areaId 区域ID
     * @param permissionLevel 权限级别（可选）
     * @return 用户权限列表
     */
    List<AreaUserPermissionVO> getAreaUserPermissions(Long areaId, Integer permissionLevel);

    /**
     * 批量同步权限到设备
     *
     * @param relationIds 关联ID列表
     * @return 同步结果
     */
    AreaPermissionManager.BatchSyncResult batchSyncToDevices(List<String> relationIds);

    /**
     * 获取区域权限统计信息
     *
     * @param areaId 区域ID
     * @return 权限统计信息
     */
    AreaPermissionManager.AreaPermissionStatistics getAreaPermissionStatistics(Long areaId);

    /**
     * 清理过期权限
     *
     * @return 清理的数量
     */
    int cleanExpiredPermissions();

    /**
     * 更新区域权限
     *
     * @param relationId 关联ID
     * @param permissionLevel 权限级别
     * @param effectiveTime 生效时间
     * @param expireTime 失效时间
     * @param allowStartTime 允许开始时间
     * @param allowEndTime 允许结束时间
     * @param workdayOnly 是否仅工作日有效
     * @param accessPermissions 访问权限配置
     * @param extendedAttributes 扩展属性
     * @param remark 备注
     * @return 是否成功
     */
    boolean updateAreaPermission(String relationId, Integer permissionLevel,
                                 LocalDateTime effectiveTime, LocalDateTime expireTime,
                                 String allowStartTime, String allowEndTime,
                                 Boolean workdayOnly, String accessPermissions,
                                 String extendedAttributes, String remark);

    /**
     * 获取用户权限详情
     *
     * @param relationId 关联ID
     * @return 权限详情
     */
    AreaUserPermissionVO getUserPermissionDetail(String relationId);

    /**
     * 批量分配区域权限
     *
     * @param areaId 区域ID
     * @param userIds 用户ID列表
     * @param permissionLevel 权限级别
     * @param relationType 关联类型
     * @return 分配结果
     */
    BatchGrantResult batchGrantAreaPermission(Long areaId, List<Long> userIds,
                                             Integer permissionLevel, Integer relationType);

    /**
     * 获取用户区域权限历史
     *
     * @param userId 用户ID
     * @param areaId 区域ID（可选）
     * @return 权限历史列表
     */
    List<AreaPermissionHistoryVO> getUserPermissionHistory(Long userId, Long areaId);

    /**
     * 验证区域权限配置
     *
     * @param areaId 区域ID
     * @return 验证结果
     */
    AreaPermissionValidationResult validateAreaPermissionConfig(Long areaId);

    // ==================== 内部类 ====================

    /**
     * 区域用户权限VO
     */
    class AreaUserPermissionVO {
        private String relationId;
        private Long areaId;
        private String areaCode;
        private String areaName;
        private Long userId;
        private String username;
        private String realName;
        private Integer relationType;
        private String relationTypeName;
        private Integer permissionLevel;
        private String permissionLevelName;
        private LocalDateTime effectiveTime;
        private LocalDateTime expireTime;
        private Boolean permanent;
        private String allowStartTime;
        private String allowEndTime;
        private Boolean workdayOnly;
        private String accessPermissions;
        private Integer deviceSyncStatus;
        private String deviceSyncStatusName;
        private LocalDateTime lastSyncTime;
        private String extendedAttributes;
        private String remark;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        // getters and setters
        public String getRelationId() { return relationId; }
        public void setRelationId(String relationId) { this.relationId = relationId; }
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaCode() { return areaCode; }
        public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public Integer getRelationType() { return relationType; }
        public void setRelationType(Integer relationType) { this.relationType = relationType; }
        public String getRelationTypeName() { return relationTypeName; }
        public void setRelationTypeName(String relationTypeName) { this.relationTypeName = relationTypeName; }
        public Integer getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(Integer permissionLevel) { this.permissionLevel = permissionLevel; }
        public String getPermissionLevelName() { return permissionLevelName; }
        public void setPermissionLevelName(String permissionLevelName) { this.permissionLevelName = permissionLevelName; }
        public LocalDateTime getEffectiveTime() { return effectiveTime; }
        public void setEffectiveTime(LocalDateTime effectiveTime) { this.effectiveTime = effectiveTime; }
        public LocalDateTime getExpireTime() { return expireTime; }
        public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
        public Boolean getPermanent() { return permanent; }
        public void setPermanent(Boolean permanent) { this.permanent = permanent; }
        public String getAllowStartTime() { return allowStartTime; }
        public void setAllowStartTime(String allowStartTime) { this.allowStartTime = allowStartTime; }
        public String getAllowEndTime() { return allowEndTime; }
        public void setAllowEndTime(String allowEndTime) { this.allowEndTime = allowEndTime; }
        public Boolean getWorkdayOnly() { return workdayOnly; }
        public void setWorkdayOnly(Boolean workdayOnly) { this.workdayOnly = workdayOnly; }
        public String getAccessPermissions() { return accessPermissions; }
        public void setAccessPermissions(String accessPermissions) { this.accessPermissions = accessPermissions; }
        public Integer getDeviceSyncStatus() { return deviceSyncStatus; }
        public void setDeviceSyncStatus(Integer deviceSyncStatus) { this.deviceSyncStatus = deviceSyncStatus; }
        public String getDeviceSyncStatusName() { return deviceSyncStatusName; }
        public void setDeviceSyncStatusName(String deviceSyncStatusName) { this.deviceSyncStatusName = deviceSyncStatusName; }
        public LocalDateTime getLastSyncTime() { return lastSyncTime; }
        public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
        public String getExtendedAttributes() { return extendedAttributes; }
        public void setExtendedAttributes(String extendedAttributes) { this.extendedAttributes = extendedAttributes; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        public LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    }

    /**
     * 批量分配结果
     */
    class BatchGrantResult {
        private int totalCount;
        private int successCount;
        private int failedCount;
        private List<String> successUsers;
        private List<BatchGrantError> failedUsers;

        // getters and setters
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
        public List<String> getSuccessUsers() { return successUsers; }
        public void setSuccessUsers(List<String> successUsers) { this.successUsers = successUsers; }
        public List<BatchGrantError> getFailedUsers() { return failedUsers; }
        public void setFailedUsers(List<BatchGrantError> failedUsers) { this.failedUsers = failedUsers; }

        /**
         * 批量分配错误
         */
        public static class BatchGrantError {
            private Long userId;
            private String username;
            private String errorMessage;

            // getters and setters
            public Long getUserId() { return userId; }
            public void setUserId(Long userId) { this.userId = userId; }
            public String getUsername() { return username; }
            public void setUsername(String username) { this.username = username; }
            public String getErrorMessage() { return errorMessage; }
            public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        }
    }

    /**
     * 区域权限历史VO
     */
    class AreaPermissionHistoryVO {
        private String historyId;
        private String relationId;
        private Long areaId;
        private String areaName;
        private Long userId;
        private String username;
        private String realName;
        private String operationType;
        private String operationTypeName;
        private Integer oldPermissionLevel;
        private Integer newPermissionLevel;
        private String changeReason;
        private Long operatorId;
        private String operatorName;
        private LocalDateTime operationTime;

        // getters and setters
        public String getHistoryId() { return historyId; }
        public void setHistoryId(String historyId) { this.historyId = historyId; }
        public String getRelationId() { return relationId; }
        public void setRelationId(String relationId) { this.relationId = relationId; }
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        public String getOperationTypeName() { return operationTypeName; }
        public void setOperationTypeName(String operationTypeName) { this.operationTypeName = operationTypeName; }
        public Integer getOldPermissionLevel() { return oldPermissionLevel; }
        public void setOldPermissionLevel(Integer oldPermissionLevel) { this.oldPermissionLevel = oldPermissionLevel; }
        public Integer getNewPermissionLevel() { return newPermissionLevel; }
        public void setNewPermissionLevel(Integer newPermissionLevel) { this.newPermissionLevel = newPermissionLevel; }
        public String getChangeReason() { return changeReason; }
        public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public LocalDateTime getOperationTime() { return operationTime; }
        public void setOperationTime(LocalDateTime operationTime) { this.operationTime = operationTime; }
    }

    /**
     * 区域权限配置验证结果
     */
    class AreaPermissionValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        private ValidationSummary summary;

        // getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        public ValidationSummary getSummary() { return summary; }
        public void setSummary(ValidationSummary summary) { this.summary = summary; }

        /**
         * 验证摘要
         */
        public static class ValidationSummary {
            private int totalPermissions;
            private int validPermissions;
            private int invalidPermissions;
            private int expiredPermissions;
            private int soonToExpirePermissions;
            private int notSyncedPermissions;

            // getters and setters
            public int getTotalPermissions() { return totalPermissions; }
            public void setTotalPermissions(int totalPermissions) { this.totalPermissions = totalPermissions; }
            public int getValidPermissions() { return validPermissions; }
            public void setValidPermissions(int validPermissions) { this.validPermissions = validPermissions; }
            public int getInvalidPermissions() { return invalidPermissions; }
            public void setInvalidPermissions(int invalidPermissions) { this.invalidPermissions = invalidPermissions; }
            public int getExpiredPermissions() { return expiredPermissions; }
            public void setExpiredPermissions(int expiredPermissions) { this.expiredPermissions = expiredPermissions; }
            public int getSoonToExpirePermissions() { return soonToExpirePermissions; }
            public void setSoonToExpirePermissions(int soonToExpirePermissions) { this.soonToExpirePermissions = soonToExpirePermissions; }
            public int getNotSyncedPermissions() { return notSyncedPermissions; }
            public void setNotSyncedPermissions(int notSyncedPermissions) { this.notSyncedPermissions = notSyncedPermissions; }
        }
    }
}