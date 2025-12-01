package net.lab1024.sa.base.module.support.rbac.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.module.area.dao.AreaDao;
import net.lab1024.sa.base.module.support.rbac.cache.AreaPermissionCacheManager;
import net.lab1024.sa.base.module.support.rbac.dao.AreaPersonDao;
import net.lab1024.sa.base.module.support.rbac.domain.entity.AreaPersonEntity;
import net.lab1024.sa.base.module.support.rbac.service.AreaPermissionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 区域权限验证服务实现
 * <p>
 * 提供集中的区域权限验证和管理功能，支持区域层次结构、时间范围、数据域等高级权限控制
 * 严格遵循四层架构规范，作为Service层处理业务逻辑
 *
 * @author SmartAdmin Team
 * @date 2025/11/25
 */
@Slf4j
@Service
public class AreaPermissionServiceImpl implements AreaPermissionService {

    @Resource
    private AreaPersonDao areaPersonDao;

    @Resource
    private AreaDao areaDao;

    @Resource
    private AreaPermissionCacheManager cacheManager;

    // ==================== 权限验证核心方法 ====================

    /**
     * 检查用户是否具有指定区域权限
     * 使用自定义缓存管理器进行缓存控制，实现更精细的缓存策略
     */
    @Override
    public boolean hasAreaPermission(Long userId, Long areaId) {
        log.debug("检查用户 {} 区域 {} 权限", userId, areaId);

        try {
            // 1. 先尝试从缓存获取
            Boolean cachedResult = cacheManager.getUserAreaPermission(userId, areaId);
            if (cachedResult != null) {
                log.debug("从缓存命中用户区域权限: userId={}, areaId={}, hasPermission={}",
                         userId, areaId, cachedResult);
                return cachedResult;
            }

            // 2. 缓存未命中，查询数据库
            boolean hasPermission = areaPersonDao.hasAreaPermission(userId, areaId);

            // 3. 将结果缓存（true值才缓存，false值不缓存以避免缓存污染）
            if (hasPermission) {
                cacheManager.cacheUserAreaPermission(userId, areaId, true);
            }

            log.debug("数据库查询用户区域权限: userId={}, areaId={}, hasPermission={}",
                     userId, areaId, hasPermission);
            return hasPermission;

        } catch (Exception e) {
            log.error("检查用户区域权限失败: userId={}, areaId={}", userId, areaId, e);
            // 异常情况下保守处理，返回false拒绝访问
            return false;
        }
    }

    /**
     * 检查用户是否具有指定区域路径权限
     */
    @Override
    @Cacheable(value = "area:permission:path", key = "#userId + ':' + #areaPath", unless = "#result == false")
    public boolean hasAreaPathPermission(Long userId, String areaPath) {
        log.debug("检查用户 {} 区域路径 {} 权限", userId, areaPath);

        try {
            return areaPersonDao.hasAreaPathPermission(userId, areaPath);
        } catch (Exception e) {
            log.error("检查用户区域路径权限失败: userId={}, areaPath={}", userId, areaPath, e);
            return false;
        }
    }

    /**
     * 获取用户所有授权的区域ID列表
     */
    @Override
    @Cacheable(value = "area:user:areas", key = "#userId")
    public List<Long> getUserAuthorizedAreaIds(Long userId) {
        log.debug("获取用户 {} 所有授权区域ID", userId);

        try {
            return areaPersonDao.getAreaIdsByPersonId(userId);
        } catch (Exception e) {
            log.error("获取用户授权区域ID失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取用户在指定数据域范围内的区域权限
     */
    @Override
    @Cacheable(value = "area:user:datarange", key = "#userId + ':' + #dataScope")
    public List<Long> getUserAreaIdsByDataScope(Long userId, String dataScope) {
        log.debug("获取用户 {} 数据域 {} 的区域权限", userId, dataScope);

        try {
            return areaPersonDao.getAreaIdsByDataScope(userId, dataScope);
        } catch (Exception e) {
            log.error("获取用户数据域区域权限失败: userId={}, dataScope={}", userId, dataScope, e);
            return new ArrayList<>();
        }
    }

    // ==================== 批量权限管理方法 ====================

    /**
     * 批量授权用户区域权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"area:permission", "area:user:*", "area:permission:*"}, allEntries = true)
    public boolean batchGrantAreaPermissions(Long userId, List<Long> areaIds, String dataScope,
                                           LocalDateTime effectiveTime, LocalDateTime expireTime) {
        log.info("批量授权用户 {} 区域权限: areaIds={}, dataScope={}", userId, areaIds, dataScope);

        if (areaIds == null || areaIds.isEmpty()) {
            log.warn("区域ID列表为空，无法授权");
            return false;
        }

        try {
            List<AreaPersonEntity> areaPersonList = new ArrayList<>();
            for (Long areaId : areaIds) {
                AreaPersonEntity entity = new AreaPersonEntity();
                entity.setAreaId(areaId);
                entity.setPersonId(userId);
                entity.setDataScope(dataScope);
                entity.setEffectiveTime(effectiveTime);
                entity.setExpireTime(expireTime);
                entity.setStatus(1); // 启用状态
                entity.setCreateUserId(userId);
                areaPersonList.add(entity);
            }

            int result = areaPersonDao.batchInsert(areaPersonList);
            boolean success = result == areaIds.size();

            if (success) {
                log.info("批量授权成功: userId={}, 授权数量={}", userId, result);
            } else {
                log.warn("批量授权部分成功: userId={}, 期望数量={}, 实际数量={}", userId, areaIds.size(), result);
            }

            return success;
        } catch (Exception e) {
            log.error("批量授权用户区域权限失败: userId={}, areaIds={}", userId, areaIds, e);
            throw new RuntimeException("批量授权失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量撤销用户区域权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"area:permission", "area:user:*", "area:permission:*"}, allEntries = true)
    public boolean batchRevokeAreaPermissions(Long userId, List<Long> areaIds) {
        log.info("批量撤销用户 {} 区域权限: areaIds={}", userId, areaIds);

        if (areaIds == null || areaIds.isEmpty()) {
            log.warn("区域ID列表为空，无法撤销");
            return false;
        }

        try {
            int result = areaPersonDao.batchDeleteByAreaIds(areaIds, userId);
            boolean success = result > 0;

            if (success) {
                log.info("批量撤销成功: userId={}, 撤销数量={}", userId, result);
            } else {
                log.warn("批量撤销无变化: userId={}", userId);
            }

            return success;
        } catch (Exception e) {
            log.error("批量撤销用户区域权限失败: userId={}, areaIds={}", userId, areaIds, e);
            throw new RuntimeException("批量撤销失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量更新用户区域权限状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"area:permission", "area:user:*", "area:permission:*"}, allEntries = true)
    public boolean batchUpdatePermissionStatus(Long userId, List<Long> areaIds, Integer status) {
        log.info("批量更新用户 {} 区域权限状态: areaIds={}, status={}", userId, areaIds, status);

        if (areaIds == null || areaIds.isEmpty()) {
            log.warn("区域ID列表为空，无法更新状态");
            return false;
        }

        try {
            int result = areaPersonDao.batchUpdateStatusByAreaIds(areaIds, userId, status);
            boolean success = result > 0;

            if (success) {
                log.info("批量更新状态成功: userId={}, 更新数量={}", userId, result);
            } else {
                log.warn("批量更新状态无变化: userId={}", userId);
            }

            return success;
        } catch (Exception e) {
            log.error("批量更新用户区域权限状态失败: userId={}, areaIds={}", userId, areaIds, e);
            throw new RuntimeException("批量更新状态失败: " + e.getMessage(), e);
        }
    }

    // ==================== 区域层次结构方法 ====================

    @Override
    @Cacheable(value = "area:user:all", key = "#userId")
    public List<Long> getAllAuthorizedAreaIds(Long userId) {
        log.debug("获取用户 {} 所有授权区域（包括子区域）", userId);

        try {
            return areaPersonDao.getAllAuthorizedAreaIds(userId);
        } catch (Exception e) {
            log.error("获取用户所有授权区域失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "area:user:direct", key = "#userId")
    public List<Long> getDirectAuthorizedAreaIds(Long userId) {
        log.debug("获取用户 {} 直接授权区域", userId);

        try {
            return areaPersonDao.getDirectAuthorizedAreaIds(userId);
        } catch (Exception e) {
            log.error("获取用户直接授权区域失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "area:user:path:prefix", key = "#userId + ':' + #areaPathPrefix")
    public List<Long> getAreaIdsByPathPrefix(Long userId, String areaPathPrefix) {
        log.debug("获取用户 {} 路径前缀 {} 的区域权限", userId, areaPathPrefix);

        try {
            return areaPersonDao.getAreaIdsByPathPrefix(userId, areaPathPrefix);
        } catch (Exception e) {
            log.error("获取用户路径前缀区域权限失败: userId={}, areaPathPrefix={}", userId, areaPathPrefix, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "area:user:paths", key = "#userId")
    public List<String> getUserAuthorizedAreaPaths(Long userId) {
        log.debug("获取用户 {} 授权区域路径列表", userId);

        try {
            return areaPersonDao.getAreaPathsByPersonId(userId);
        } catch (Exception e) {
            log.error("获取用户授权区域路径失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    // ==================== 时间范围权限管理 ====================

    @Override
    @Cacheable(value = "area:user:time:range", key = "#userId + ':' + #startTime + ':' + #endTime")
    public List<AreaPersonEntity> getEffectivePermissionsByTimeRange(Long userId,
                                                                    LocalDateTime startTime,
                                                                    LocalDateTime endTime) {
        log.debug("获取用户 {} 时间范围 {} - {} 的有效权限", userId, startTime, endTime);

        try {
            return areaPersonDao.getEffectivePermissionsByTimeRange(userId, startTime, endTime);
        } catch (Exception e) {
            log.error("获取用户时间范围有效权限失败: userId={}, startTime={}, endTime={}", userId, startTime, endTime, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "area:permission:time", key = "#userId + ':' + #areaId + ':' + #checkTime")
    public boolean hasAreaPermissionAtTime(Long userId, Long areaId, LocalDateTime checkTime) {
        log.debug("检查用户 {} 在 {} 时是否具有区域 {} 权限", userId, checkTime, areaId);

        try {
            // 检查基本权限
            if (!hasAreaPermission(userId, areaId)) {
                return false;
            }

            // 检查时间范围（如果有设置）
            List<AreaPersonEntity> permissions = areaPersonDao.getEffectivePermissionsByTimeRange(
                userId, checkTime, checkTime);

            return permissions.stream()
                .anyMatch(p -> p.getAreaId().equals(areaId));
        } catch (Exception e) {
            log.error("检查用户时间点区域权限失败: userId={}, areaId={}, checkTime={}", userId, areaId, checkTime, e);
            return false;
        }
    }

    @Override
    @Cacheable(value = "area:expiring", key = "#days")
    public List<AreaPersonEntity> getExpiringPermissions(Integer days) {
        log.debug("获取即将过期的区域授权（{}天内）", days);

        try {
            return areaPersonDao.getExpiringPermissions(days);
        } catch (Exception e) {
            log.error("获取即将过期的区域授权失败: days={}", days, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"area:permission", "area:user:*", "area:expiring"}, allEntries = true)
    public int cleanExpiredPermissions() {
        log.info("开始清理过期的区域授权");

        try {
            int result = areaPersonDao.cleanExpiredPermissions(LocalDateTime.now());
            log.info("清理过期授权完成: 清理数量={}", result);
            return result;
        } catch (Exception e) {
            log.error("清理过期授权失败", e);
            throw new RuntimeException("清理过期授权失败: " + e.getMessage(), e);
        }
    }

    // ==================== 统计和分析方法 ====================

    @Override
    @Cacheable(value = "area:stats:user", key = "#userId")
    public int countUserAreaPermissions(Long userId) {
        log.debug("统计用户 {} 的区域权限数量", userId);

        try {
            return areaPersonDao.countAreaPermissions(userId).intValue();
        } catch (Exception e) {
            log.error("统计用户区域权限数量失败: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    @Cacheable(value = "area:stats:area", key = "#areaId")
    public int countUserPermissionsByArea(Long areaId) {
        log.debug("统计区域 {} 的授权用户数量", areaId);

        try {
            return areaPersonDao.countPersonPermissionsByArea(areaId).intValue();
        } catch (Exception e) {
            log.error("统计区域授权用户数量失败: areaId={}", areaId, e);
            return 0;
        }
    }

    @Override
    @Cacheable(value = "area:stats:all", key = "'global'")
    public List<AreaPersonEntity> getAreaPermissionStatistics() {
        log.debug("获取区域权限统计信息");

        try {
            return areaPersonDao.getAreaPermissionStatistics();
        } catch (Exception e) {
            log.error("获取区域权限统计信息失败", e);
            return new ArrayList<>();
        }
    }

    // ==================== 权限继承和传播方法 ====================

    @Override
    @Cacheable(value = "area:permission:parent", key = "#userId + ':' + #areaId")
    public boolean hasParentAreaPermission(Long userId, Long areaId) {
        log.debug("检查用户 {} 是否具有区域 {} 的父区域权限", userId, areaId);

        try {
            // TODO: 实现父区域权限检查逻辑
            // 需要查询区域表获取父区域信息，然后递归检查
            log.warn("父区域权限检查功能待实现");
            return false;
        } catch (Exception e) {
            log.error("检查父区域权限失败: userId={}, areaId={}", userId, areaId, e);
            return false;
        }
    }

    @Override
    @Cacheable(value = "area:permission:parents", key = "#userId + ':' + #areaId")
    public List<Long> getParentAreaPermissions(Long userId, Long areaId) {
        log.debug("获取用户 {} 区域 {} 的所有父区域权限", userId, areaId);

        try {
            // TODO: 实现父区域权限查询逻辑
            log.warn("父区域权限查询功能待实现");
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取父区域权限失败: userId={}, areaId={}", userId, areaId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"area:permission", "area:user:*"}, allEntries = true)
    public boolean propagatePermissionToChildren(Long userId, Long parentAreaId, boolean includeChildren) {
        log.info("传播用户 {} 区域 {} 权限到子区域: includeChildren={}", userId, parentAreaId, includeChildren);

        try {
            // TODO: 实现权限传播到子区域的逻辑
            // 需要查询区域表获取子区域信息，然后批量创建权限记录
            log.warn("权限传播功能待实现");
            return false;
        } catch (Exception e) {
            log.error("权限传播失败: userId={}, parentAreaId={}", userId, parentAreaId, e);
            throw new RuntimeException("权限传播失败: " + e.getMessage(), e);
        }
    }

    // ==================== 缓存管理方法 ====================

    @Override
    @CacheEvict(value = {"area:permission", "area:user:*", "area:permission:*"}, key = "#userId")
    public void clearUserPermissionCache(Long userId) {
        log.info("清除用户 {} 的权限缓存", userId);
    }

    @Override
    @CacheEvict(value = {"area:permission", "area:user:*", "area:permission:*"}, key = "#areaId")
    public void clearAreaPermissionCache(Long areaId) {
        log.info("清除区域 {} 的权限缓存", areaId);
    }

    @Override
    @CacheEvict(value = {"area:permission", "area:user:*", "area:permission:*", "area:stats:*", "area:expiring"}, allEntries = true)
    public void clearAllPermissionCache() {
        log.info("清除所有权限缓存");
    }

    @Override
    public void warmupUserPermissionCache(Long userId) {
        log.info("预热用户 {} 的权限缓存", userId);

        try {
            // 预热各种权限缓存
            getUserAuthorizedAreaIds(userId);
            getAllAuthorizedAreaIds(userId);
            getDirectAuthorizedAreaIds(userId);
            getUserAuthorizedAreaPaths(userId);
            countUserAreaPermissions(userId);

            log.info("用户 {} 权限缓存预热完成", userId);
        } catch (Exception e) {
            log.error("用户权限缓存预热失败: userId={}", userId, e);
        }
    }

    @Override
    public boolean checkCircularReference(Long areaId, Long newParentId) {
        log.debug("检查区域层次结构循环引用: areaId={}, newParentId={}", areaId, newParentId);

        try {
            if (areaId == null || newParentId == null || areaId.equals(newParentId)) {
                return true; // 直接循环引用
            }

            // 获取新父区域的所有父级区域路径
            List<Long> parentHierarchy = getAreaHierarchyPath(newParentId);

            // 检查当前区域是否在父区域的层次结构中
            return parentHierarchy.contains(areaId);
        } catch (Exception e) {
            log.error("检查循环引用失败: areaId={}, newParentId={}", areaId, newParentId, e);
            return true; // 安全起见，如果检查失败则认为有循环引用
        }
    }

    @Override
    @Cacheable(value = "area:hierarchy:path", key = "#areaId")
    public List<Long> getAreaHierarchyPath(Long areaId) {
        log.debug("获取区域层次路径: areaId={}", areaId);

        try {
            if (areaId == null) {
                return new ArrayList<>();
            }

            // 从AreaDao获取区域的完整父级路径
            String hierarchyPathStr = areaDao.getAreaHierarchyPath(areaId);
            if (hierarchyPathStr == null || hierarchyPathStr.trim().isEmpty()) {
                return new ArrayList<>();
            }

            // 解析逗号分隔的路径字符串为List<Long>
            List<Long> hierarchyPath = new ArrayList<>();
            for (String idStr : hierarchyPathStr.split(",")) {
                try {
                    hierarchyPath.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException e) {
                    log.warn("无效的区域ID: {}", idStr);
                }
            }
            return hierarchyPath;
        } catch (Exception e) {
            log.error("获取区域层次路径失败: areaId={}", areaId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable(value = "area:hierarchy:children", key = "#parentAreaId")
    public List<Long> getAllChildAreaIds(Long parentAreaId) {
        log.debug("获取区域所有子区域: parentAreaId={}", parentAreaId);

        try {
            if (parentAreaId == null) {
                return new ArrayList<>();
            }

            // 递归获取所有子区域ID
            Set<Long> childAreaIds = areaDao.getAllChildAreaIds(parentAreaId);
            return childAreaIds != null ? new ArrayList<>(childAreaIds) : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取所有子区域失败: parentAreaId={}", parentAreaId, e);
            return new ArrayList<>();
        }
    }

    // ==================== 批量查询优化方法 ====================

    @Override
    public Map<Long, Map<Long, Boolean>> batchCheckAreaPermissions(Map<Long, List<Long>> userAreaIds) {
        log.debug("批量检查区域权限: 用户数量={}", userAreaIds.size());

        Map<Long, Map<Long, Boolean>> results = new HashMap<>();
        LocalDateTime currentTime = LocalDateTime.now();

        try {
            for (Map.Entry<Long, List<Long>> entry : userAreaIds.entrySet()) {
                Long userId = entry.getKey();
                List<Long> areaIds = entry.getValue();
                Map<Long, Boolean> userPermissions = new HashMap<>();

                // 获取用户所有有效权限
                List<AreaPersonEntity> userEffectivePermissions = areaPersonDao.selectByPersonIds(Arrays.asList(userId));
                Set<Long> authorizedAreaIds = userEffectivePermissions.stream()
                        .filter(this::isPermissionEffective)
                        .map(AreaPersonEntity::getAreaId)
                        .collect(Collectors.toSet());

                // 检查每个区域权限
                for (Long areaId : areaIds) {
                    boolean hasPermission = authorizedAreaIds.contains(areaId);
                    userPermissions.put(areaId, hasPermission);
                }

                results.put(userId, userPermissions);
            }

            log.debug("批量权限检查完成: 检查结果数量={}", results.size());
            return results;
        } catch (Exception e) {
            log.error("批量检查区域权限失败", e);
            return new HashMap<>();
        }
    }

    @Override
    @Cacheable(value = "area:permission:batch:user-areas", key = "#userIds.hashCode()")
    public Map<Long, List<Long>> batchGetUserAuthorizedAreaIds(List<Long> userIds) {
        log.debug("批量获取用户授权区域: 用户数量={}", userIds.size());

        try {
            // 批量查询用户权限
            List<AreaPersonEntity> userPermissions = areaPersonDao.selectByPersonIds(userIds);
            LocalDateTime currentTime = LocalDateTime.now();

            // 按用户ID分组并过滤有效权限
            return userPermissions.stream()
                    .filter(this::isPermissionEffective)
                    .collect(Collectors.groupingBy(
                            AreaPersonEntity::getPersonId,
                            Collectors.mapping(AreaPersonEntity::getAreaId, Collectors.toList())
                    ));
        } catch (Exception e) {
            log.error("批量获取用户授权区域失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public List<AreaPersonEntity> batchGetPermissionStatus(List<Long> userIds, List<Long> areaIds) {
        log.debug("批量获取权限状态: 用户数量={}, 区域数量={}", userIds.size(), areaIds.size());

        try {
            // 使用批量检查方法
            Map<Long, List<Long>> userAreasMap = new HashMap<>();
            for (Long userId : userIds) {
                userAreasMap.put(userId, areaIds);
            }

            return areaPersonDao.batchCheckAreaPermission(userAreasMap);
        } catch (Exception e) {
            log.error("批量获取权限状态失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<Long, Integer> batchCountUserAreaPermissions(List<Long> userIds) {
        log.debug("批量统计用户权限数量: 用户数量={}", userIds.size());

        try {
            // 批量获取用户权限
            Map<Long, List<Long>> userAreas = batchGetUserAuthorizedAreaIds(userIds);

            // 统计每个用户的权限数量
            return userAreas.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().size()
                    ));
        } catch (Exception e) {
            log.error("批量统计用户权限数量失败", e);
            return new HashMap<>();
        }
    }

    @Override
    @CacheEvict(value = {"area:permission:user", "area:permission:batch:user-areas"}, allEntries = true)
    public boolean batchRefreshUserPermissionCache(List<Long> userIds) {
        log.debug("批量刷新用户权限缓存: 用户数量={}", userIds.size());

        try {
            for (Long userId : userIds) {
                clearUserPermissionCache(userId);
                warmupUserPermissionCache(userId);
            }
            return true;
        } catch (Exception e) {
            log.error("批量刷新用户权限缓存失败", e);
            return false;
        }
    }

    @Override
    @Cacheable(value = "area:hierarchy:batch:children", key = "#parentAreaIds.hashCode()")
    public Map<Long, List<Long>> batchGetAllChildAreaIds(List<Long> parentAreaIds) {
        log.debug("批量获取子区域: 父区域数量={}", parentAreaIds.size());

        Map<Long, List<Long>> results = new HashMap<>();

        try {
            for (Long parentAreaId : parentAreaIds) {
                List<Long> childAreaIds = getAllChildAreaIds(parentAreaId);
                results.put(parentAreaId, childAreaIds);
            }
            return results;
        } catch (Exception e) {
            log.error("批量获取子区域失败", e);
            return new HashMap<>();
        }
    }

    @Override
    @Cacheable(value = "area:hierarchy:batch:paths", key = "#areaIds.hashCode()")
    public Map<Long, List<Long>> batchGetAreaHierarchyPaths(List<Long> areaIds) {
        log.debug("批量获取区域层次路径: 区域数量={}", areaIds.size());

        Map<Long, List<Long>> results = new HashMap<>();

        try {
            for (Long areaId : areaIds) {
                List<Long> hierarchyPath = getAreaHierarchyPath(areaId);
                results.put(areaId, hierarchyPath);
            }
            return results;
        } catch (Exception e) {
            log.error("批量获取区域层次路径失败", e);
            return new HashMap<>();
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查权限是否有效
     */
    private boolean isPermissionEffective(AreaPersonEntity permission) {
        LocalDateTime now = LocalDateTime.now();
        return permission.getStatus() == 1 &&
                (permission.getEffectiveTime() == null || !permission.getEffectiveTime().isAfter(now)) &&
                (permission.getExpireTime() == null || !permission.getExpireTime().isBefore(now));
    }
}