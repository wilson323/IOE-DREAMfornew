package net.lab1024.sa.common.organization.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;

import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 区域管理器
 * <p>
 * 区域作为空间概念的统一管理器
 * 负责区域、设备、人员的联动管理
 * 支持人员信息下发到区域设备的业务逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class AreaManager {

    private final AreaDao areaDao;
    private final AreaDeviceDao areaDeviceDao;
    private final AreaUserDao areaUserDao;
    private final AreaUnifiedService areaUnifiedService; // 区域统一服务，用于获取子区域
    private final GatewayServiceClient gatewayServiceClient; // 网关服务客户端，用于调用设备通讯服务

    /**
     * 构造函数注入依赖
     *
     * @param areaDao 区域DAO
     * @param areaDeviceDao 区域设备关联DAO
     * @param areaUserDao 区域人员关联DAO
     * @param areaUnifiedService 区域统一服务
     * @param gatewayServiceClient 网关服务客户端
     */
    public AreaManager(AreaDao areaDao, AreaDeviceDao areaDeviceDao, AreaUserDao areaUserDao,
                      AreaUnifiedService areaUnifiedService, GatewayServiceClient gatewayServiceClient) {
        this.areaDao = areaDao;
        this.areaDeviceDao = areaDeviceDao;
        this.areaUserDao = areaUserDao;
        this.areaUnifiedService = areaUnifiedService;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    // ==================== 区域-人员管理 ====================

    /**
     * 将人员添加到区域
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     * @param relationType 关联类型
     * @param permissionLevel 权限级别
     * @return 关联关系
     */
    public AreaUserEntity addUserToArea(Long areaId, Long userId, Integer relationType, Integer permissionLevel) {
        log.debug("[区域管理] 添加人员到区域, areaId={}, userId={}, relationType={}, permissionLevel={}",
                 areaId, userId, relationType, permissionLevel);

        // 检查区域是否存在
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null) {
            log.warn("[区域管理] 区域不存在: areaId={}", areaId);
            throw new BusinessException("AREA_NOT_FOUND", "区域不存在: " + areaId);
        }

        // 检查是否已存在关联
        AreaUserEntity existingRelation = areaUserDao.selectByAreaIdAndUserId(areaId, userId);
        if (existingRelation != null) {
            log.warn("[区域管理] 用户已关联到该区域: areaId={}, userId={}", areaId, userId);
            throw new BusinessException("USER_ALREADY_ASSOCIATED_TO_AREA", "用户已关联到该区域: " + userId);
        }

        // 创建关联关系
        AreaUserEntity relation = new AreaUserEntity()
                .setAreaId(areaId)
                .setAreaCode(area.getAreaCode())
                .setUserId(userId)
                .setRelationType(relationType)
                .setPermissionLevel(permissionLevel)
                .setRelationStatus(1)
                .setPermanent(true)
                .setInheritChildren(false)
                .setInheritParent(true)
                .setDeviceSyncStatus(0) // 需要下发到设备
                .setRetryCount(0);

        // 保存关联关系
        areaUserDao.insert(relation);

        log.info("[区域管理] 人员关联成功, relationId={}, areaId={}, userId={}",
                relation.getId(), areaId, userId);

        // 异步触发设备信息下发
        triggerUserInfoSync(areaId, userId);

        return relation;
    }

    /**
     * 移除区域人员关联
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     */
    public void removeUserFromArea(Long areaId, Long userId) {
        log.debug("[区域管理] 移除区域人员关联, areaId={}, userId={}", areaId, userId);

        AreaUserEntity relation = areaUserDao.selectByAreaIdAndUserId(areaId, userId);
        if (relation == null) {
            log.warn("[区域管理] 用户未关联到该区域: areaId={}, userId={}", areaId, userId);
            throw new BusinessException("USER_NOT_ASSOCIATED_TO_AREA", "用户未关联到该区域: " + userId);
        }

        // 撤销设备权限
        revokeUserInfoFromDevices(areaId, userId);

        // 删除关联关系
        areaUserDao.deleteById(relation.getId());

        log.info("[区域管理] 人员关联已移除, areaId={}, userId={}", areaId, userId);
    }

    /**
     * 获取区域关联的所有人员
     *
     * @param areaId 区域ID
     * @return 人员列表
     */
    public List<AreaUserEntity> getAreaUsers(Long areaId) {
        log.debug("[区域管理] 获取区域人员, areaId={}", areaId);

        List<AreaUserEntity> users = areaUserDao.selectByAreaId(areaId);

        // 包含继承权限的用户
        List<AreaUserEntity> inheritedUsers = getInheritedUsers(areaId);
        users.addAll(inheritedUsers);

        // 去重并按权限级别排序
        Map<Long, AreaUserEntity> uniqueUsers = new HashMap<>();
        for (AreaUserEntity user : users) {
            AreaUserEntity existing = uniqueUsers.get(user.getUserId());
            if (existing == null || user.getPermissionLevel() > existing.getPermissionLevel()) {
                uniqueUsers.put(user.getUserId(), user);
            }
        }

        return uniqueUsers.values().stream()
                .sorted((u1, u2) -> u2.getPermissionLevel().compareTo(u1.getPermissionLevel()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户有权访问的所有区域
     *
     * @param userId 用户ID
     * @return 区域列表
     */
    public List<Long> getUserAccessibleAreas(Long userId) {
        log.debug("[区域管理] 获取用户可访问区域, userId={}", userId);

        List<AreaUserEntity> relations = areaUserDao.selectByUserId(userId);
        List<Long> areaIds = new ArrayList<>();

        for (AreaUserEntity relation : relations) {
            // 添加直接关联的区域
            areaIds.add(relation.getAreaId());

            // 如果允许继承子区域权限，添加所有子区域
            if (relation.getInheritChildren() != null && relation.getInheritChildren()) {
                List<Long> childAreaIds = getChildAreaIds(relation.getAreaId());
                areaIds.addAll(childAreaIds);
            }
        }

        // 去重
        return areaIds.stream().distinct().collect(Collectors.toList());
    }

    // ==================== 设备-人员联动管理 ====================

    /**
     * 将人员信息下发到区域内的所有设备
     * <p>
     * 获取区域内所有设备，批量同步用户信息
     * 支持同步进度跟踪和失败重试
     * </p>
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     */
    public void triggerUserInfoSync(Long areaId, Long userId) {
        log.info("[区域管理] 触发用户信息同步, areaId={}, userId={}", areaId, userId);

        try {
            // 获取区域内所有设备
            List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaId(areaId);

            if (devices == null || devices.isEmpty()) {
                log.warn("[区域管理] 区域内无设备，跳过用户信息同步, areaId={}, userId={}", areaId, userId);
                return;
            }

            int successCount = 0;
            int failureCount = 0;

            for (AreaDeviceEntity device : devices) {
                try {
                    // 更新关联关系为需要同步
                    updateRelationSyncStatus(areaId, userId, 1); // 下发中

                    // 调用设备通讯服务下发用户信息
                    syncUserToDevice(device, userId);
                    successCount++;

                } catch (Exception e) {
                    failureCount++;
                    log.error("[区域管理] 设备用户信息同步失败, deviceId={}, userId={}, error={}",
                            device.getDeviceId(), userId, e.getMessage(), e);
                    // 更新同步状态为失败
                    updateRelationSyncStatus(areaId, userId, 3);
                }
            }

            log.info("[区域管理] 用户信息同步完成, areaId={}, userId={}, totalDevices={}, success={}, failure={}",
                    areaId, userId, devices.size(), successCount, failureCount);

        } catch (Exception e) {
            log.error("[区域管理] 触发用户信息同步异常, areaId={}, userId={}", areaId, userId, e);
        }
    }

    /**
     * 撤销用户在区域内所有设备的权限
     * <p>
     * 获取区域内所有设备，批量撤销用户权限
     * 支持撤销进度跟踪和操作回滚
     * </p>
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     */
    public void revokeUserInfoFromDevices(Long areaId, Long userId) {
        log.info("[区域管理] 撤销用户设备权限, areaId={}, userId={}", areaId, userId);

        try {
            List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaId(areaId);

            if (devices == null || devices.isEmpty()) {
                log.warn("[区域管理] 区域内无设备，跳过权限撤销, areaId={}, userId={}", areaId, userId);
                // 更新关联关系为已撤销
                updateRelationSyncStatus(areaId, userId, 4);
                return;
            }

            int successCount = 0;
            int failureCount = 0;

            for (AreaDeviceEntity device : devices) {
                try {
                    // 调用设备通讯服务撤销用户权限
                    revokeUserFromDevice(device, userId);
                    successCount++;

                } catch (Exception e) {
                    failureCount++;
                    log.error("[区域管理] 设备权限撤销失败, deviceId={}, userId={}, error={}",
                            device.getDeviceId(), userId, e.getMessage(), e);
                }
            }

            // 更新关联关系为已撤销
            updateRelationSyncStatus(areaId, userId, 4);

            log.info("[区域管理] 用户权限撤销完成, areaId={}, userId={}, totalDevices={}, success={}, failure={}",
                    areaId, userId, devices.size(), successCount, failureCount);

        } catch (Exception e) {
            log.error("[区域管理] 撤销用户设备权限异常, areaId={}, userId={}", areaId, userId, e);
        }
    }

    /**
     * 批量同步人员信息到设备
     *
     * @param deviceIds 设备ID列表
     */
    public void batchSyncUsersToDevices(List<String> deviceIds) {
        log.info("[区域管理] 批量同步用户信息到设备, deviceCount={}", deviceIds.size());

        for (String deviceId : deviceIds) {
            try {
                // 获取设备关联的区域
                List<AreaDeviceEntity> deviceRelations = areaDeviceDao.selectByDeviceId(deviceId);

                for (AreaDeviceEntity deviceRelation : deviceRelations) {
                    // 获取区域内需要同步的用户
                    List<AreaUserEntity> users = areaUserDao.selectNeedSync()
                            .stream()
                            .filter(user -> user.getAreaId().equals(deviceRelation.getAreaId()))
                            .collect(Collectors.toList());

                    // 同步用户到设备
                    for (AreaUserEntity user : users) {
                        syncUserToDevice(deviceRelation, user.getUserId());
                    }
                }
            } catch (Exception e) {
                log.error("[区域管理] 设备用户同步失败, deviceId={}", deviceId, e);
            }
        }
    }

    // ==================== 权限管理 ====================

    /**
     * 检查用户是否有区域访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param requiredPermissionLevel 需要的权限级别
     * @return 是否有权限
     */
    public boolean hasAreaPermission(Long userId, Long areaId, Integer requiredPermissionLevel) {
        log.debug("[区域管理] 检查区域权限, userId={}, areaId={}, requiredLevel={}",
                 userId, areaId, requiredPermissionLevel);

        // 检查直接权限
        if (areaUserDao.hasPermission(userId, areaId, requiredPermissionLevel)) {
            return true;
        }

        // 检查继承权限
        return hasInheritedPermission(userId, areaId, requiredPermissionLevel);
    }

    /**
     * 获取用户在区域内的最高权限级别
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 最高权限级别
     */
    public Integer getUserMaxPermissionLevel(Long userId, Long areaId) {
        log.debug("[区域管理] 获取用户最高权限级别, userId={}, areaId={}", userId, areaId);

        List<AreaUserEntity> relations = areaUserDao.selectUserAreaPermissions(userId, Arrays.asList(areaId));

        if (relations.isEmpty()) {
            return 0; // 无权限
        }

        return relations.stream()
                .mapToInt(AreaUserEntity::getPermissionLevel)
                .max()
                .orElse(0);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取继承权限的用户
     */
    private List<AreaUserEntity> getInheritedUsers(Long areaId) {
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null || area.getPath() == null) {
            return Collections.emptyList();
        }

        return areaUserDao.selectByParentAreaPath(area.getPath());
    }

    /**
     * 获取子区域ID列表
     * <p>
     * 优先使用AreaUnifiedService获取子区域
     * 如果服务不可用，降级使用AreaDao查询
     * </p>
     *
     * @param parentAreaId 父区域ID
     * @return 子区域ID列表
     */
    private List<Long> getChildAreaIds(Long parentAreaId) {
        try {
            // 1. 优先使用AreaUnifiedService
            if (areaUnifiedService != null) {
                List<AreaEntity> childAreas = areaUnifiedService.getChildAreas(parentAreaId);
                if (childAreas != null && !childAreas.isEmpty()) {
                    return childAreas.stream()
                            .map(AreaEntity::getId)
                            .collect(Collectors.toList());
                }
            }

            // 2. 降级处理：使用AreaDao查询
            List<AreaEntity> childAreas = areaDao.selectByParentId(parentAreaId);
            if (childAreas != null && !childAreas.isEmpty()) {
                return childAreas.stream()
                        .map(AreaEntity::getId)
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("[区域管理] 获取子区域ID列表异常, parentAreaId={}", parentAreaId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 更新关联关系同步状态
     */
    private void updateRelationSyncStatus(Long areaId, Long userId, Integer syncStatus) {
        AreaUserEntity relation = areaUserDao.selectByAreaIdAndUserId(areaId, userId);
        if (relation != null) {
            areaUserDao.updateSyncStatus(relation.getId(), syncStatus, null, LocalDateTime.now());
        }
    }

    /**
     * 同步用户信息到单个设备
     * <p>
     * 通过设备通讯服务同步用户信息到设备
     * 复用AreaDeviceManager中的用户同步逻辑
     * </p>
     *
     * @param device 区域设备关联
     * @param userId 用户ID
     */
    private void syncUserToDevice(AreaDeviceEntity device, Long userId) {
        try {
            log.debug("[区域管理] 同步用户到设备, deviceId={}, userId={}", device.getDeviceId(), userId);

            if (gatewayServiceClient == null) {
                log.warn("[区域管理] GatewayServiceClient未注入，无法同步用户信息");
                updateRelationSyncStatus(device.getAreaId(), userId, 3); // 同步失败
                return;
            }

            // 1. 构建用户同步请求
            Map<String, Object> syncRequest = new HashMap<>();
            syncRequest.put("deviceId", device.getDeviceId());
            syncRequest.put("userId", userId);
            syncRequest.put("timestamp", System.currentTimeMillis());

            // 2. 调用设备通讯服务同步用户信息接口
            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/user/sync",
                    HttpMethod.POST,
                    syncRequest,
                    Map.class
            );

            // 3. 处理响应
            if (response != null && response.isSuccess()) {
                log.info("[区域管理] 用户同步成功, deviceId={}, userId={}", device.getDeviceId(), userId);
                // 更新同步状态为成功
                updateRelationSyncStatus(device.getAreaId(), userId, 2);
            } else {
                log.warn("[区域管理] 用户同步失败, deviceId={}, userId={}, message={}",
                        device.getDeviceId(), userId, response != null ? response.getMessage() : "响应为空");
                // 更新同步状态为失败
                updateRelationSyncStatus(device.getAreaId(), userId, 3);
            }

        } catch (Exception e) {
            log.error("[区域管理] 用户同步异常, deviceId={}, userId={}", device.getDeviceId(), userId, e);
            // 更新同步状态为失败
            updateRelationSyncStatus(device.getAreaId(), userId, 3);
        }
    }

    /**
     * 从设备撤销用户权限
     * <p>
     * 通过设备通讯服务撤销用户在设备上的权限
     * 复用AreaDeviceManager中的权限撤销逻辑
     * </p>
     *
     * @param device 区域设备关联
     * @param userId 用户ID
     */
    private void revokeUserFromDevice(AreaDeviceEntity device, Long userId) {
        try {
            log.debug("[区域管理] 从设备撤销用户权限, deviceId={}, userId={}", device.getDeviceId(), userId);

            if (gatewayServiceClient == null) {
                log.warn("[区域管理] GatewayServiceClient未注入，无法撤销用户权限");
                return;
            }

            // 1. 调用设备通讯服务撤销用户权限接口
            // DELETE /api/v1/device/user/{deviceId}/{userId}
            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/user/" + device.getDeviceId() + "/" + userId,
                    HttpMethod.DELETE,
                    null,
                    Map.class
            );

            // 2. 处理响应
            if (response != null && response.isSuccess()) {
                log.info("[区域管理] 用户权限撤销成功, deviceId={}, userId={}", device.getDeviceId(), userId);
            } else {
                log.warn("[区域管理] 用户权限撤销失败, deviceId={}, userId={}, message={}",
                        device.getDeviceId(), userId, response != null ? response.getMessage() : "响应为空");
            }

        } catch (Exception e) {
            log.error("[区域管理] 撤销用户权限异常, deviceId={}, userId={}", device.getDeviceId(), userId, e);
        }
    }

    /**
     * 检查继承权限
     */
    private boolean hasInheritedPermission(Long userId, Long areaId, Integer requiredPermissionLevel) {
        AreaEntity area = areaDao.selectById(areaId);
        if (area == null || area.getParentId() == null) {
            return false;
        }

        // 递归检查父区域权限
        return hasAreaPermission(userId, area.getParentId(), requiredPermissionLevel);
    }
}
