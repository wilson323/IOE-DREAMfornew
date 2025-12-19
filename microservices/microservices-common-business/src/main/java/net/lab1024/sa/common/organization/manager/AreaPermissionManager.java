package net.lab1024.sa.common.organization.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 区域权限管理器
 * <p>
 * 负责管理区域的用户权限控制和设备访问权限
 * 支持细粒度权限控制、时间限制、权限继承等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RequiredArgsConstructor
public class AreaPermissionManager {

    private final AreaUserDao areaUserDao;
    private final AreaDeviceDao areaDeviceDao;
    private final AreaDao areaDao;

    /**
     * 为用户分配区域权限
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
    public String grantAreaPermission(Long areaId, Long userId, String username, String realName,
                                     Integer relationType, Integer permissionLevel,
                                     LocalDateTime effectiveTime, LocalDateTime expireTime,
                                     String allowStartTime, String allowEndTime,
                                     Boolean workdayOnly, String accessPermissions,
                                     String extendedAttributes, String remark) {
        log.info("[区域权限管理] 分配区域权限: areaId={}, userId={}, permissionLevel={}",
                areaId, userId, permissionLevel);

        // 验证区域是否存在
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null || area.getDeletedFlag() != 0) {
            throw new RuntimeException("区域不存在或已删除: " + areaId);
        }

        // 检查是否已存在权限关系
        AreaUserEntity existingRelation = areaUserDao.selectByAreaIdAndUserId(areaId, userId);
        if (existingRelation != null) {
            log.warn("[区域权限管理] 用户已在该区域拥有权限: areaId={}, userId={}", areaId, userId);
            throw new RuntimeException("用户已在该区域拥有权限");
        }

        // 创建权限关联
        AreaUserEntity relation = new AreaUserEntity();
        relation.setAreaId(areaId);
        relation.setAreaCode(area.getAreaCode());
        relation.setUserId(userId);
        relation.setUsername(username);
        relation.setRealName(realName);
        relation.setRelationType(relationType);
        relation.setPermissionLevel(permissionLevel);
        relation.setEffectiveTime(effectiveTime);
        relation.setExpireTime(expireTime);
        relation.setPermanent(expireTime == null);
        relation.setAllowStartTime(allowStartTime);
        relation.setAllowEndTime(allowEndTime);
        relation.setWorkdayOnly(workdayOnly != null ? workdayOnly : false);
        relation.setAccessPermissions(accessPermissions);
        relation.setExtendedAttributes(extendedAttributes);
        relation.setRemark(remark);

        // 设置默认值
        relation.setDeviceSyncStatus(0); // 未下发
        relation.setRetryCount(0);

        int result = areaUserDao.insert(relation);
        if (result <= 0) {
            throw new RuntimeException("创建区域权限关联失败");
        }

        log.info("[区域权限管理] 成功分配区域权限: relationId={}, areaId={}, userId={}",
                relation.getId(), areaId, userId);

        // 触发设备同步
        triggerDeviceSync(relation.getId());

        return relation.getId();
    }

    /**
     * 撤销用户区域权限
     *
     * @param relationId 关联ID
     * @return 是否成功
     */
    public boolean revokeAreaPermission(String relationId) {
        log.info("[区域权限管理] 撤销区域权限: relationId={}", relationId);

        AreaUserEntity relation = areaUserDao.selectById(relationId);
        if (relation == null) {
            log.warn("[区域权限管理] 权限关系不存在: relationId={}", relationId);
            return false;
        }

        // 标记为已删除
        relation.setDeletedFlag(1);
        relation.setUpdateTime(LocalDateTime.now());

        int result = areaUserDao.updateById(relation);
        if (result > 0) {
            // 撤销设备权限
            revokeDevicePermissions(relationId);
            log.info("[区域权限管理] 成功撤销区域权限: relationId={}", relationId);
            return true;
        }

        return false;
    }

    /**
     * 检查用户是否具有区域访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param requiredPermissionLevel 所需权限级别
     * @return 权限检查结果
     */
    public PermissionCheckResult checkAreaPermission(Long userId, Long areaId, Integer requiredPermissionLevel) {
        log.debug("[区域权限管理] 检查区域权限: userId={}, areaId={}, requiredLevel={}",
                userId, areaId, requiredPermissionLevel);

        // 查询用户在该区域的权限
        AreaUserEntity permission = areaUserDao.selectByAreaIdAndUserId(areaId, userId);
        if (permission == null) {
            // 检查是否有父区域的继承权限
            permission = checkInheritedPermission(userId, areaId);
            if (permission == null) {
                return PermissionCheckResult.denied("用户在该区域没有权限");
            }
        }

        // 检查权限级别
        if (permission.getPermissionLevel() < requiredPermissionLevel) {
            return PermissionCheckResult.denied("权限级别不足");
        }

        // 检查时间有效性
        if (!isTimeValid(permission)) {
            return PermissionCheckResult.denied("权限不在有效时间内");
        }

        // 检查工作日限制
        if (permission.getWorkdayOnly() != null && permission.getWorkdayOnly() && !isWorkday()) {
            return PermissionCheckResult.denied("权限仅在工作日有效");
        }

        // 检查每日时间限制
        if (!isDailyTimeValid(permission)) {
            return PermissionCheckResult.denied("当前时间不在允许的时间范围内");
        }

        // 检查设备同步状态
        if (permission.getDeviceSyncStatus() != null && permission.getDeviceSyncStatus() != 2) {
            log.warn("[区域权限管理] 权限未同步到设备: relationId={}, syncStatus={}",
                    permission.getId(), permission.getDeviceSyncStatus());
        }

        return PermissionCheckResult.granted(permission);
    }

    /**
     * 获取用户可访问的区域列表
     *
     * @param userId 用户ID
     * @return 可访问区域列表
     */
    public List<Long> getUserAccessibleAreas(Long userId) {
        log.debug("[区域权限管理] 获取用户可访问区域: userId={}", userId);

        List<AreaUserEntity> permissions = areaUserDao.selectByUserId(userId);
        List<Long> accessibleAreas = new ArrayList<>();

        for (AreaUserEntity permission : permissions) {
            // 检查权限是否有效
            if (isPermissionValid(permission)) {
                accessibleAreas.add(permission.getAreaId());
            }
        }

        // 获取有继承权限的子区域
        List<Long> inheritedAreas = getInheritedAreas(userId);
        accessibleAreas.addAll(inheritedAreas);

        // 去重并返回
        return accessibleAreas.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 获取区域用户权限列表
     *
     * @param areaId 区域ID
     * @param permissionLevel 权限级别（可选）
     * @return 用户权限列表
     */
    public List<AreaUserEntity> getAreaUserPermissions(Long areaId, Integer permissionLevel) {
        log.debug("[区域权限管理] 获取区域用户权限: areaId={}, permissionLevel={}", areaId, permissionLevel);

        if (permissionLevel != null) {
            return areaUserDao.selectByAreaIdAndPermissionLevel(areaId, permissionLevel);
        } else {
            return areaUserDao.selectByAreaId(areaId);
        }
    }

    /**
     * 批量同步权限到设备
     *
     * @param relationIds 关联ID列表
     * @return 同步结果
     */
    public BatchSyncResult batchSyncToDevices(List<String> relationIds) {
        log.info("[区域权限管理] 批量同步权限到设备: count={}", relationIds.size());

        BatchSyncResult result = new BatchSyncResult();
        result.setTotalCount(relationIds.size());

        List<String> failedRelations = new ArrayList<>();
        List<String> successRelations = new ArrayList<>();

        for (String relationId : relationIds) {
            try {
                syncPermissionToDevice(relationId);
                successRelations.add(relationId);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                log.error("[区域权限管理] 同步权限到设备失败: relationId={}", relationId, e);
                failedRelations.add(relationId);
                result.setFailedCount(result.getFailedCount() + 1);
            }
        }

        result.setFailedRelations(failedRelations);
        result.setSuccessRelations(successRelations);

        log.info("[区域权限管理] 批量同步完成: 成功={}, 失败={}",
                result.getSuccessCount(), result.getFailedCount());

        return result;
    }

    /**
     * 获取区域权限统计信息
     *
     * @param areaId 区域ID
     * @return 权限统计信息
     */
    public AreaPermissionStatistics getAreaPermissionStatistics(Long areaId) {
        log.debug("[区域权限管理] 获取区域权限统计: areaId={}", areaId);

        AreaPermissionStatistics statistics = new AreaPermissionStatistics();
        statistics.setAreaId(areaId);

        // 总用户数
        List<AreaUserEntity> allUsers = areaUserDao.selectByAreaId(areaId);
        statistics.setTotalUsers(allUsers.size());

        // 按权限级别统计
        Map<Integer, Long> permissionLevelCount = allUsers.stream()
                .collect(Collectors.groupingBy(
                        AreaUserEntity::getPermissionLevel,
                        Collectors.counting()
                ));
        statistics.setPermissionLevelCount(permissionLevelCount);

        // 按关联类型统计
        Map<Integer, Long> relationTypeCount = allUsers.stream()
                .collect(Collectors.groupingBy(
                        AreaUserEntity::getRelationType,
                        Collectors.counting()
                ));
        statistics.setRelationTypeCount(relationTypeCount);

        // 同步状态统计
        Map<Integer, Long> syncStatusCount = allUsers.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getDeviceSyncStatus() != null ? user.getDeviceSyncStatus() : 0,
                        Collectors.counting()
                ));
        statistics.setSyncStatusCount(syncStatusCount);

        // 过期权限统计
        LocalDateTime now = LocalDateTime.now();
        long expiredCount = allUsers.stream()
                .filter(user -> !user.getPermanent() && user.getExpireTime() != null && user.getExpireTime().isBefore(now))
                .count();
        statistics.setExpiredCount(expiredCount);

        // 即将过期权限统计（7天内）
        LocalDateTime sevenDaysLater = now.plusDays(7);
        long soonToExpireCount = allUsers.stream()
                .filter(user -> !user.getPermanent() && user.getExpireTime() != null
                        && user.getExpireTime().isAfter(now) && user.getExpireTime().isBefore(sevenDaysLater))
                .count();
        statistics.setSoonToExpireCount(soonToExpireCount);

        return statistics;
    }

    /**
     * 清理过期权限
     *
     * @return 清理的数量
     */
    public int cleanExpiredPermissions() {
        log.info("[区域权限管理] 开始清理过期权限");

        LocalDateTime now = LocalDateTime.now();
        List<AreaUserEntity> expiredPermissions = areaUserDao.selectExpired(now);

        int cleanedCount = 0;
        for (AreaUserEntity permission : expiredPermissions) {
            try {
                // 标记为已删除
                permission.setDeletedFlag(1);
                permission.setUpdateTime(now);
                areaUserDao.updateById(permission);

                // 撤销设备权限
                revokeDevicePermissions(permission.getId());

                cleanedCount++;
            } catch (Exception e) {
                log.error("[区域权限管理] 清理过期权限失败: relationId={}", permission.getId(), e);
            }
        }

        log.info("[区域权限管理] 过期权限清理完成: count={}", cleanedCount);
        return cleanedCount;
    }

    // ==================== 私有方法 ====================

    /**
     * 检查继承权限
     */
    private AreaUserEntity checkInheritedPermission(Long userId, Long areaId) {
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null || area.getParentAreaId() == null) {
            return null;
        }

        // 递归检查父区域权限
        return checkInheritedPermissionRecursive(userId, area.getParentAreaId());
    }

    /**
     * 递归检查继承权限
     */
    private AreaUserEntity checkInheritedPermissionRecursive(Long userId, Long parentAreaId) {
        AreaUserEntity parentPermission = areaUserDao.selectByAreaIdAndUserId(parentAreaId, userId);
        if (parentPermission != null && isPermissionValid(parentPermission)) {
            return parentPermission;
        }

        AreaEntity parentArea = areaDao.selectById(parentAreaId);
        if (parentArea != null && parentArea.getParentAreaId() != null) {
            return checkInheritedPermissionRecursive(userId, parentArea.getParentAreaId());
        }

        return null;
    }

    /**
     * 检查权限时间有效性
     */
    private boolean isTimeValid(AreaUserEntity permission) {
        LocalDateTime now = LocalDateTime.now();

        // 检查生效时间
        if (permission.getEffectiveTime() != null && now.isBefore(permission.getEffectiveTime())) {
            return false;
        }

        // 检查失效时间
        if (!permission.getPermanent() && permission.getExpireTime() != null && now.isAfter(permission.getExpireTime())) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否为工作日
     */
    private boolean isWorkday() {
        DayOfWeek dayOfWeek = LocalDateTime.now().getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * 检查每日时间有效性
     */
    private boolean isDailyTimeValid(AreaUserEntity permission) {
        if (permission.getAllowStartTime() == null || permission.getAllowEndTime() == null) {
            return true; // 没有时间限制
        }

        LocalTime now = LocalTime.now();
        LocalTime startTime = LocalTime.parse(permission.getAllowStartTime());
        LocalTime endTime = LocalTime.parse(permission.getAllowEndTime());

        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    /**
     * 检查权限是否有效
     */
    private boolean isPermissionValid(AreaUserEntity permission) {
        return isTimeValid(permission)
                && (!permission.getWorkdayOnly() || isWorkday())
                && isDailyTimeValid(permission);
    }

    /**
     * 获取有继承权限的子区域
     */
    private List<Long> getInheritedAreas(Long userId) {
        List<Long> inheritedAreas = new ArrayList<>();

        // 获取用户有权限的区域
        List<AreaUserEntity> userPermissions = areaUserDao.selectByUserId(userId);
        for (AreaUserEntity permission : userPermissions) {
            if (permission.getInheritChildren() != null && permission.getInheritChildren()) {
                // 查询子区域
                List<AreaEntity> childAreas = areaDao.selectByParentId(permission.getAreaId());
                for (AreaEntity childArea : childAreas) {
                    inheritedAreas.add(childArea.getAreaId());
                    // 递归获取子区域的子区域
                    inheritedAreas.addAll(getChildAreasRecursive(childArea.getAreaId()));
                }
            }
        }

        return inheritedAreas;
    }

    /**
     * 递归获取子区域
     */
    private List<Long> getChildAreasRecursive(Long parentAreaId) {
        List<Long> childAreas = new ArrayList<>();
        List<AreaEntity> children = areaDao.selectByParentId(parentAreaId);

        for (AreaEntity child : children) {
            childAreas.add(child.getAreaId());
            childAreas.addAll(getChildAreasRecursive(child.getAreaId()));
        }

        return childAreas;
    }

    /**
     * 触发设备同步
     */
    private void triggerDeviceSync(String relationId) {
        // 异步触发设备同步
        // 这里可以使用消息队列或异步任务
        log.info("[区域权限管理] 触发设备同步: relationId={}", relationId);
    }

    /**
     * 同步权限到设备
     */
    private void syncPermissionToDevice(String relationId) {
        log.info("[区域权限管理] 同步权限到设备: relationId={}", relationId);

        AreaUserEntity relation = areaUserDao.selectById(relationId);
        if (relation == null) {
            return;
        }

        // 获取区域关联的设备
        List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaId(relation.getAreaId());

        // 同步到设备（这里需要调用设备通讯服务）
        for (AreaDeviceEntity device : devices) {
            try {
                // 调用设备通讯服务下发权限
                log.debug("[区域权限管理] 下发权限到设备: deviceId={}, userId={}",
                        device.getDeviceId(), relation.getUserId());
            } catch (Exception e) {
                log.error("[区域权限管理] 下发权限到设备失败: deviceId={}, error={}",
                        device.getDeviceId(), e.getMessage(), e);
            }
        }

        // 更新同步状态
        areaUserDao.updateSyncStatus(relationId, 2, null, LocalDateTime.now());
    }

    /**
     * 撤销设备权限
     */
    private void revokeDevicePermissions(String relationId) {
        log.info("[区域权限管理] 撤销设备权限: relationId={}", relationId);

        AreaUserEntity relation = areaUserDao.selectById(relationId);
        if (relation == null) {
            return;
        }

        // 获取区域关联的设备
        List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaId(relation.getAreaId());

        // 从设备撤销权限
        for (AreaDeviceEntity device : devices) {
            try {
                // 调用设备通讯服务撤销权限
                log.debug("[区域权限管理] 从设备撤销权限: deviceId={}, userId={}",
                        device.getDeviceId(), relation.getUserId());
            } catch (Exception e) {
                log.error("[区域权限管理] 从设备撤销权限失败: deviceId={}, error={}",
                        device.getDeviceId(), e.getMessage(), e);
            }
        }
    }

    // ==================== 内部类 ====================

    /**
     * 权限检查结果
     */
    public static class PermissionCheckResult {
        private boolean permissionGranted;
        private String reason;
        private AreaUserEntity permission;

        private PermissionCheckResult(boolean permissionGranted, String reason, AreaUserEntity permission) {
            this.permissionGranted = permissionGranted;
            this.reason = reason;
            this.permission = permission;
        }

        public static PermissionCheckResult granted(AreaUserEntity permission) {
            return new PermissionCheckResult(true, "权限验证通过", permission);
        }

        public static PermissionCheckResult denied(String reason) {
            return new PermissionCheckResult(false, reason, null);
        }

        // getters
        public boolean isGranted() { return permissionGranted; }
        public String getReason() { return reason; }
        public AreaUserEntity getPermission() { return permission; }
    }

    /**
     * 批量同步结果
     */
    public static class BatchSyncResult {
        private int totalCount;
        private int successCount;
        private int failedCount;
        private List<String> successRelations;
        private List<String> failedRelations;

        // getters and setters
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
        public List<String> getSuccessRelations() { return successRelations; }
        public void setSuccessRelations(List<String> successRelations) { this.successRelations = successRelations; }
        public List<String> getFailedRelations() { return failedRelations; }
        public void setFailedRelations(List<String> failedRelations) { this.failedRelations = failedRelations; }
    }

    /**
     * 区域权限统计信息
     */
    public static class AreaPermissionStatistics {
        private Long areaId;
        private Integer totalUsers;
        private Map<Integer, Long> permissionLevelCount;
        private Map<Integer, Long> relationTypeCount;
        private Map<Integer, Long> syncStatusCount;
        private Long expiredCount;
        private Long soonToExpireCount;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public Integer getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
        public Map<Integer, Long> getPermissionLevelCount() { return permissionLevelCount; }
        public void setPermissionLevelCount(Map<Integer, Long> permissionLevelCount) { this.permissionLevelCount = permissionLevelCount; }
        public Map<Integer, Long> getRelationTypeCount() { return relationTypeCount; }
        public void setRelationTypeCount(Map<Integer, Long> relationTypeCount) { this.relationTypeCount = relationTypeCount; }
        public Map<Integer, Long> getSyncStatusCount() { return syncStatusCount; }
        public void setSyncStatusCount(Map<Integer, Long> syncStatusCount) { this.syncStatusCount = syncStatusCount; }
        public Long getExpiredCount() { return expiredCount; }
        public void setExpiredCount(Long expiredCount) { this.expiredCount = expiredCount; }
        public Long getSoonToExpireCount() { return soonToExpireCount; }
        public void setSoonToExpireCount(Long soonToExpireCount) { this.soonToExpireCount = soonToExpireCount; }
    }
}
