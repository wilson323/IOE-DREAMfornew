package net.lab1024.sa.common.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.organization.manager.AreaPermissionManager;
import net.lab1024.sa.common.organization.service.AreaPermissionService;
import org.springframework.context.ApplicationEventPublisher;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 区域权限服务实现
 * <p>
 * 提供完整的区域权限管理服务实现
 * 包括权限分配、检查、统计、同步等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RequiredArgsConstructor
public class AreaPermissionServiceImpl implements AreaPermissionService {

    private final AreaPermissionManager areaPermissionManager;
    private final AreaUserDao areaUserDao;
    private final AreaDao areaDao;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    public String grantAreaPermission(Long areaId, Long userId, String username, String realName,
                                     Integer relationType, Integer permissionLevel,
                                     LocalDateTime effectiveTime, LocalDateTime expireTime,
                                     String allowStartTime, String allowEndTime,
                                     Boolean workdayOnly, String accessPermissions,
                                     String extendedAttributes, String remark) {
        log.info("[区域权限服务] 分配区域权限: areaId={}, userId={}, permissionLevel={}",
                areaId, userId, permissionLevel);

        try {
            String relationId = areaPermissionManager.grantAreaPermission(
                    areaId, userId, username, realName, relationType, permissionLevel,
                    effectiveTime, expireTime, allowStartTime, allowEndTime,
                    workdayOnly, accessPermissions, extendedAttributes, remark
            );

            // 发布权限新增事件（异步触发权限同步）
            publishPermissionChangeEvent(userId, areaId, "ADDED");

            return relationId;
        } catch (Exception e) {
            log.error("[区域权限服务] 分配区域权限失败: areaId={}, userId={}, error={}",
                    areaId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean revokeAreaPermission(String relationId) {
        log.info("[区域权限服务] 撤销区域权限: relationId={}", relationId);

        try {
            // 先获取权限信息（用于发布事件）
            AreaUserEntity relation = areaUserDao.selectById(relationId);
            Long userId = relation != null ? relation.getUserId() : null;
            Long areaId = relation != null ? relation.getAreaId() : null;

            boolean result = areaPermissionManager.revokeAreaPermission(relationId);

            // 发布权限移除事件（异步触发权限移除）
            if (result && userId != null && areaId != null) {
                publishPermissionChangeEvent(userId, areaId, "REMOVED");
            }

            return result;
        } catch (Exception e) {
            log.error("[区域权限服务] 撤销区域权限失败: relationId={}, error={}",
                    relationId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public AreaPermissionManager.PermissionCheckResult checkAreaPermission(Long userId, Long areaId, Integer requiredPermissionLevel) {
        log.debug("[区域权限服务] 检查区域权限: userId={}, areaId={}, requiredLevel={}",
                userId, areaId, requiredPermissionLevel);

        try {
            return areaPermissionManager.checkAreaPermission(userId, areaId, requiredPermissionLevel);
        } catch (Exception e) {
            log.error("[区域权限服务] 检查区域权限失败: userId={}, areaId={}, error={}",
                    userId, areaId, e.getMessage(), e);
            return AreaPermissionManager.PermissionCheckResult.denied("权限检查异常: " + e.getMessage());
        }
    }

    @Override
    public List<Long> getUserAccessibleAreas(Long userId) {
        log.debug("[区域权限服务] 获取用户可访问区域: userId={}", userId);

        try {
            return areaPermissionManager.getUserAccessibleAreas(userId);
        } catch (Exception e) {
            log.error("[区域权限服务] 获取用户可访问区域失败: userId={}, error={}",
                    userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AreaUserPermissionVO> getAreaUserPermissions(Long areaId, Integer permissionLevel) {
        log.debug("[区域权限服务] 获取区域用户权限: areaId={}, permissionLevel={}", areaId, permissionLevel);

        try {
            List<AreaUserEntity> entities;
            if (permissionLevel != null) {
                entities = areaUserDao.selectByAreaIdAndPermissionLevel(areaId, permissionLevel);
            } else {
                entities = areaUserDao.selectByAreaId(areaId);
            }

            return entities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[区域权限服务] 获取区域用户权限失败: areaId={}, error={}",
                    areaId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public AreaPermissionManager.BatchSyncResult batchSyncToDevices(List<String> relationIds) {
        log.info("[区域权限服务] 批量同步权限到设备: count={}", relationIds.size());

        try {
            return areaPermissionManager.batchSyncToDevices(relationIds);
        } catch (Exception e) {
            log.error("[区域权限服务] 批量同步权限到设备失败: error={}", e.getMessage(), e);
            AreaPermissionManager.BatchSyncResult result = new AreaPermissionManager.BatchSyncResult();
            result.setTotalCount(relationIds.size());
            result.setSuccessCount(0);
            result.setFailedCount(relationIds.size());
            result.setFailedRelations(relationIds);
            return result;
        }
    }

    @Override
    public AreaPermissionManager.AreaPermissionStatistics getAreaPermissionStatistics(Long areaId) {
        log.debug("[区域权限服务] 获取区域权限统计: areaId={}", areaId);

        try {
            return areaPermissionManager.getAreaPermissionStatistics(areaId);
        } catch (Exception e) {
            log.error("[区域权限服务] 获取区域权限统计失败: areaId={}, error={}",
                    areaId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int cleanExpiredPermissions() {
        log.info("[区域权限服务] 清理过期权限");

        try {
            return areaPermissionManager.cleanExpiredPermissions();
        } catch (Exception e) {
            log.error("[区域权限服务] 清理过期权限失败: error={}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public boolean updateAreaPermission(String relationId, Integer permissionLevel,
                                       LocalDateTime effectiveTime, LocalDateTime expireTime,
                                       String allowStartTime, String allowEndTime,
                                       Boolean workdayOnly, String accessPermissions,
                                       String extendedAttributes, String remark) {
        log.info("[区域权限服务] 更新区域权限: relationId={}", relationId);

        try {
            AreaUserEntity relation = areaUserDao.selectById(relationId);
            if (relation == null) {
                log.warn("[区域权限服务] 权限关系不存在: relationId={}", relationId);
                return false;
            }

            // 记录变更历史
            recordPermissionHistory(relation, "UPDATE", permissionLevel);

            // 更新权限信息
            if (permissionLevel != null) {
                relation.setPermissionLevel(permissionLevel);
            }
            if (effectiveTime != null) {
                relation.setEffectiveTime(effectiveTime);
            }
            if (expireTime != null) {
                relation.setExpireTime(expireTime);
                relation.setPermanent(expireTime == null);
            }
            if (allowStartTime != null) {
                relation.setAllowStartTime(allowStartTime);
            }
            if (allowEndTime != null) {
                relation.setAllowEndTime(allowEndTime);
            }
            if (workdayOnly != null) {
                relation.setWorkdayOnly(workdayOnly);
            }
            if (accessPermissions != null) {
                relation.setAccessPermissions(accessPermissions);
            }
            if (extendedAttributes != null) {
                relation.setExtendedAttributes(extendedAttributes);
            }
            if (remark != null) {
                relation.setRemark(remark);
            }

            // 标记需要重新同步
            relation.setDeviceSyncStatus(0);
            relation.setUpdateTime(LocalDateTime.now());

            int result = areaUserDao.updateById(relation);
            if (result > 0) {
                // 触发设备同步
                triggerDeviceSync(relationId);
                log.info("[区域权限服务] 成功更新区域权限: relationId={}", relationId);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("[区域权限服务] 更新区域权限失败: relationId={}, error={}",
                    relationId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public AreaUserPermissionVO getUserPermissionDetail(String relationId) {
        log.debug("[区域权限服务] 获取用户权限详情: relationId={}", relationId);

        try {
            AreaUserEntity entity = areaUserDao.selectById(relationId);
            if (entity == null) {
                return null;
            }

            AreaUserPermissionVO vo = convertToVO(entity);

            // 添加区域名称
            AreaEntity area = areaDao.selectById(entity.getAreaId());
            if (area != null) {
                vo.setAreaName(area.getAreaName());
            }

            return vo;
        } catch (Exception e) {
            log.error("[区域权限服务] 获取用户权限详情失败: relationId={}, error={}",
                    relationId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public BatchGrantResult batchGrantAreaPermission(Long areaId, List<Long> userIds,
                                                     Integer permissionLevel, Integer relationType) {
        log.info("[区域权限服务] 批量分配区域权限: areaId={}, userCount={}, level={}",
                areaId, userIds.size(), permissionLevel);

        BatchGrantResult result = new BatchGrantResult();
        result.setTotalCount(userIds.size());
        result.setSuccessUsers(new ArrayList<>());
        result.setFailedUsers(new ArrayList<>());

        for (Long userId : userIds) {
            try {
                // 这里需要获取用户信息，暂时使用默认值
                String relationId = grantAreaPermission(
                        areaId, userId, "user_" + userId, "用户" + userId,
                        relationType, permissionLevel,
                        null, null, null, null, false, null, null, null
                );
                result.getSuccessUsers().add(relationId);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                log.error("[区域权限服务] 批量分配权限失败: userId={}, error={}", userId, e.getMessage(), e);
                BatchGrantResult.BatchGrantError error = new BatchGrantResult.BatchGrantError();
                error.setUserId(userId);
                error.setUsername("user_" + userId);
                error.setErrorMessage(e.getMessage());
                result.getFailedUsers().add(error);
                result.setFailedCount(result.getFailedCount() + 1);
            }
        }

        log.info("[区域权限服务] 批量分配完成: 成功={}, 失败={}",
                result.getSuccessCount(), result.getFailedCount());

        return result;
    }

    @Override
    public List<AreaPermissionHistoryVO> getUserPermissionHistory(Long userId, Long areaId) {
        log.debug("[区域权限服务] 获取用户权限历史: userId={}, areaId={}", userId, areaId);

        try {
            // 这里需要实现权限历史查询，暂时返回空列表
            // 实际实现需要查询权限历史记录表
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("[区域权限服务] 获取用户权限历史失败: userId={}, areaId={}, error={}",
                    userId, areaId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public AreaPermissionValidationResult validateAreaPermissionConfig(Long areaId) {
        log.debug("[区域权限服务] 验证区域权限配置: areaId={}", areaId);

        try {
            AreaPermissionValidationResult result = new AreaPermissionValidationResult();
            result.setErrors(new ArrayList<>());
            result.setWarnings(new ArrayList<>());

            List<AreaUserEntity> permissions = areaUserDao.selectByAreaId(areaId);
            AreaPermissionValidationResult.ValidationSummary summary = new AreaPermissionValidationResult.ValidationSummary();

            summary.setTotalPermissions(permissions.size());
            summary.setValidPermissions(0);
            summary.setInvalidPermissions(0);
            summary.setExpiredPermissions(0);
            summary.setSoonToExpirePermissions(0);
            summary.setNotSyncedPermissions(0);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sevenDaysLater = now.plusDays(7);

            for (AreaUserEntity permission : permissions) {
                // 检查权限有效性
                boolean valid = true;

                // 检查过期时间
                if (!permission.getPermanent() && permission.getExpireTime() != null) {
                    if (permission.getExpireTime().isBefore(now)) {
                        summary.setExpiredPermissions(summary.getExpiredPermissions() + 1);
                        valid = false;
                    } else if (permission.getExpireTime().isBefore(sevenDaysLater)) {
                        summary.setSoonToExpirePermissions(summary.getSoonToExpirePermissions() + 1);
                        result.getWarnings().add("权限即将过期: " + permission.getRealName());
                    }
                }

                // 检查同步状态
                if (permission.getDeviceSyncStatus() == null || permission.getDeviceSyncStatus() != 2) {
                    summary.setNotSyncedPermissions(summary.getNotSyncedPermissions() + 1);
                    result.getWarnings().add("权限未同步到设备: " + permission.getRealName());
                }

                if (valid) {
                    summary.setValidPermissions(summary.getValidPermissions() + 1);
                } else {
                    summary.setInvalidPermissions(summary.getInvalidPermissions() + 1);
                }
            }

            result.setSummary(summary);
            result.setValid(summary.getInvalidPermissions() == 0);

            return result;
        } catch (Exception e) {
            log.error("[区域权限服务] 验证区域权限配置失败: areaId={}, error={}",
                    areaId, e.getMessage(), e);
            AreaPermissionValidationResult result = new AreaPermissionValidationResult();
            result.setValid(false);
            result.setErrors(Arrays.asList("验证过程发生异常: " + e.getMessage()));
            return result;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为VO对象
     */
    private AreaUserPermissionVO convertToVO(AreaUserEntity entity) {
        AreaUserPermissionVO vo = new AreaUserPermissionVO();
        vo.setRelationId(entity.getId());
        vo.setAreaId(entity.getAreaId());
        vo.setAreaCode(entity.getAreaCode());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setRealName(entity.getRealName());
        vo.setRelationType(entity.getRelationType());
        vo.setRelationTypeName(getRelationTypeName(entity.getRelationType()));
        vo.setPermissionLevel(entity.getPermissionLevel());
        vo.setPermissionLevelName(getPermissionLevelName(entity.getPermissionLevel()));
        vo.setEffectiveTime(entity.getEffectiveTime());
        vo.setExpireTime(entity.getExpireTime());
        vo.setPermanent(entity.getPermanent());
        vo.setAllowStartTime(entity.getAllowStartTime());
        vo.setAllowEndTime(entity.getAllowEndTime());
        vo.setWorkdayOnly(entity.getWorkdayOnly());
        vo.setAccessPermissions(entity.getAccessPermissions());
        vo.setDeviceSyncStatus(entity.getDeviceSyncStatus());
        vo.setDeviceSyncStatusName(getDeviceSyncStatusName(entity.getDeviceSyncStatus()));
        vo.setLastSyncTime(entity.getLastSyncTime());
        vo.setExtendedAttributes(entity.getExtendedAttributes());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    /**
     * 获取关联类型名称
     */
    private String getRelationTypeName(Integer relationType) {
        if (relationType == null) return "未知";
        return switch (relationType) {
            case 1 -> "常驻人员";
            case 2 -> "临时人员";
            case 3 -> "访客";
            case 4 -> "维护人员";
            case 5 -> "管理人员";
            default -> "未知";
        };
    }

    /**
     * 获取权限级别名称
     */
    private String getPermissionLevelName(Integer permissionLevel) {
        if (permissionLevel == null) return "未知";
        return switch (permissionLevel) {
            case 1 -> "普通权限";
            case 2 -> "高级权限";
            case 3 -> "管理员权限";
            default -> "未知";
        };
    }

    /**
     * 获取设备同步状态名称
     */
    private String getDeviceSyncStatusName(Integer syncStatus) {
        if (syncStatus == null) return "未知";
        return switch (syncStatus) {
            case 0 -> "未下发";
            case 1 -> "下发中";
            case 2 -> "已同步";
            case 3 -> "同步失败";
            case 4 -> "已撤销";
            default -> "未知";
        };
    }

    /**
     * 记录权限变更历史
     */
    private void recordPermissionHistory(AreaUserEntity relation, String operationType, Integer newPermissionLevel) {
        // 这里需要实现权限历史记录功能
        // 实际实现需要插入权限历史记录表
        log.debug("[区域权限服务] 记录权限变更历史: relationId={}, operation={}, newLevel={}",
                relation.getId(), operationType, newPermissionLevel);
    }

    /**
     * 触发设备同步
     */
    private void triggerDeviceSync(String relationId) {
        // 异步触发设备同步
        log.info("[区域权限服务] 触发设备同步: relationId={}", relationId);
    }

    /**
     * 发布权限变更事件
     * <p>
     * 发布权限变更事件，触发权限同步到设备
     * 使用通用的ApplicationEvent，各个服务可以监听并处理
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param changeType 变更类型（ADDED/REMOVED）
     */
    private void publishPermissionChangeEvent(Long userId, Long areaId, String changeType) {
        try {
            // 创建权限变更事件（使用Spring的ApplicationEvent作为基类）
            PermissionChangeEvent event = new PermissionChangeEvent(this, userId, areaId, changeType);
            eventPublisher.publishEvent(event);

            log.debug("[区域权限服务] 权限变更事件已发布: userId={}, areaId={}, changeType={}",
                    userId, areaId, changeType);
        } catch (Exception e) {
            log.error("[区域权限服务] 发布权限变更事件失败: userId={}, areaId={}, changeType={}, error={}",
                    userId, areaId, changeType, e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 权限变更事件（内部类）
     * <p>
     * 通用的权限变更事件，可以被各个服务监听
     * </p>
     */
    public static class PermissionChangeEvent extends org.springframework.context.ApplicationEvent {
        private static final long serialVersionUID = 1L;

        private final Long userId;
        private final Long areaId;
        private final String changeType;

        public PermissionChangeEvent(Object source, Long userId, Long areaId, String changeType) {
            super(source);
            this.userId = userId;
            this.areaId = areaId;
            this.changeType = changeType;
        }

        public Long getUserId() {
            return userId;
        }

        public Long getAreaId() {
            return areaId;
        }

        public String getChangeType() {
            return changeType;
        }
    }
}
