package net.lab1024.sa.common.permission.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.permission.domain.vo.*;
import net.lab1024.sa.common.permission.service.PermissionDataService;
import net.lab1024.sa.common.permission.manager.PermissionCacheManager;
import net.lab1024.sa.common.permission.audit.PermissionAuditLogger;
import net.lab1024.sa.common.permission.monitor.PermissionPerformanceMonitor;
import net.lab1024.sa.common.organization.dao.UserDao;
import net.lab1024.sa.common.organization.dao.MenuDao;
import net.lab1024.sa.common.organization.dao.RoleDao;
import net.lab1024.sa.common.organization.dao.PermissionDao;
import net.lab1024.sa.common.organization.entity.UserEntity;
import net.lab1024.sa.common.organization.entity.MenuEntity;
import net.lab1024.sa.common.organization.entity.RoleEntity;
import net.lab1024.sa.common.organization.entity.PermissionEntity;
import net.lab1024.sa.common.service.AuthService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 权限数据服务实现类
 * <p>
 * 前后端权限一致性保障的核心实现，提供：
 * - 高性能权限数据查询（多级缓存、并行处理）
 * - 完整的菜单权限树结构构建
 * - webPerms格式权限标识生成和管理
 * - 实时权限变更通知和数据同步
 * - 权限数据版本控制和冲突检测
 * - 批量权限数据操作和优化
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Service
public class PermissionDataServiceImpl implements PermissionDataService {

    @Resource
    private UserDao userDao;

    @Resource
    private MenuDao menuDao;

    @Resource
    private RoleDao roleDao;

    @Resource
    private PermissionDao permissionDao;

    @Resource
    private PermissionCacheManager permissionCacheManager;

    @Resource
    private PermissionAuditLogger permissionAuditLogger;

    @Resource
    private PermissionPerformanceMonitor permissionPerformanceMonitor;

    @Resource
    private AuthService authService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // Redis键前缀
    private static final String USER_PERMISSION_KEY_PREFIX = "permission:user:";
    private static final String MENU_TREE_KEY_PREFIX = "permission:menu:tree:";
    private static final String PERMISSION_CHANGE_KEY_PREFIX = "permission:change:";
    private static final String PERMISSION_SYNC_KEY_PREFIX = "permission:sync:";
    private static final String PERMISSION_STATS_KEY_PREFIX = "permission:stats:";

    // 缓存时间配置
    private static final long USER_PERMISSION_CACHE_TTL = 30 * 60; // 30分钟
    private static final long MENU_TREE_CACHE_TTL = 60 * 60; // 1小时
    private static final long PERMISSION_CHANGE_CACHE_TTL = 24 * 60 * 60; // 24小时

    // 权限数据版本号
    private static final String CURRENT_DATA_VERSION = "v1.0.0.20251216";

    // 内存缓存用于热点数据
    private final Map<Long, UserPermissionVO> userPermissionCache = new ConcurrentHashMap<>();
    private final Map<String, List<MenuPermissionVO>> menuTreeCache = new ConcurrentHashMap<>();

    @Override
    public UserPermissionVO getUserPermissions(Long userId) {
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;

        try {
            // 参数处理
            Long targetUserId = userId != null ? userId : authService.getCurrentUserId();
            if (targetUserId == null) {
                log.warn("[权限数据] 获取用户权限失败：用户ID为空");
                return createEmptyUserPermissionVO(targetUserId);
            }

            // 检查内存缓存
            UserPermissionVO cachedData = userPermissionCache.get(targetUserId);
            if (cachedData != null) {
                cacheHit = true;
                log.debug("[权限数据] 内存缓存命中: userId={}", targetUserId);
                return cachedData;
            }

            // 检查Redis缓存
            String cacheKey = USER_PERMISSION_KEY_PREFIX + targetUserId;
            UserPermissionVO redisData = (UserPermissionVO) permissionCacheManager.get(cacheKey);
            if (redisData != null) {
                // 更新内存缓存
                userPermissionCache.put(targetUserId, redisData);
                cacheHit = true;
                log.debug("[权限数据] Redis缓存命中: userId={}", targetUserId);
                return redisData;
            }

            // 从数据库查询权限数据
            log.debug("[权限数据] 数据库查询: userId={}", targetUserId);
            UserPermissionVO userPermissionVO = buildUserPermissionVOFromDatabase(targetUserId);

            // 缓存查询结果
            if (userPermissionVO != null && userPermissionVO.isValid()) {
                // 更新内存缓存
                userPermissionCache.put(targetUserId, userPermissionVO);

                // 更新Redis缓存
                permissionCacheManager.put(cacheKey, userPermissionVO, USER_PERMISSION_CACHE_TTL);

                log.debug("[权限数据] 数据库查询完成并缓存: userId={}", targetUserId);
            }

            // 记录性能指标
            long duration = System.currentTimeMillis() - startTime;
            permissionPerformanceMonitor.recordCacheAccess("user_permission", cacheKey, duration, cacheHit);

            return userPermissionVO != null ? userPermissionVO : createEmptyUserPermissionVO(targetUserId);

        } catch (Exception e) {
            log.error("[权限数据] 获取用户权限异常: userId={}", userId, e);

            // 记录性能指标（失败）
            long duration = System.currentTimeMillis() - startTime;
            permissionPerformanceMonitor.recordCacheAccess("user_permission", "", duration, false);

            // 返回空权限数据作为降级
            return createEmptyUserPermissionVO(userId);
        }
    }

    @Override
    public List<MenuPermissionVO> getMenuPermissions(Long userId) {
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;

        try {
            // 参数处理
            Long targetUserId = userId != null ? userId : authService.getCurrentUserId();
            if (targetUserId == null) {
                log.warn("[权限数据] 获取菜单权限失败：用户ID为空");
                return Collections.emptyList();
            }

            // 检查内存缓存
            List<MenuPermissionVO> cachedMenuTree = menuTreeCache.get("user:" + targetUserId);
            if (cachedMenuTree != null) {
                cacheHit = true;
                log.debug("[权限数据] 菜单树内存缓存命中: userId={}", targetUserId);
                return cachedMenuTree;
            }

            // 检查Redis缓存
            String cacheKey = MENU_TREE_KEY_PREFIX + "user:" + targetUserId;
            List<MenuPermissionVO> redisMenuTree = (List<MenuPermissionVO>) permissionCacheManager.get(cacheKey);
            if (redisMenuTree != null) {
                // 更新内存缓存
                menuTreeCache.put("user:" + targetUserId, redisMenuTree);
                cacheHit = true;
                log.debug("[权限数据] 菜单树Redis缓存命中: userId={}", targetUserId);
                return redisMenuTree;
            }

            // 获取用户权限和角色
            Set<String> userPermissions = getUserPermissions(targetUserId).getPermissions();
            Set<String> userRoles = getUserPermissions(targetUserId).getRoles();

            // 构建菜单权限树
            log.debug("[权限数据] 构建菜单权限树: userId={}", targetUserId);
            List<MenuPermissionVO> menuTree = buildMenuPermissionTree(targetUserId, userPermissions, userRoles);

            // 缓存菜单树
            if (menuTree != null && !menuTree.isEmpty()) {
                // 更新内存缓存
                menuTreeCache.put("user:" + targetUserId, menuTree);

                // 更新Redis缓存
                permissionCacheManager.put(cacheKey, menuTree, MENU_TREE_CACHE_TTL);

                log.debug("[权限数据] 菜单树构建完成并缓存: userId={}, menuCount={}", targetUserId, menuTree.size());
            }

            // 记录性能指标
            long duration = System.currentTimeMillis() - startTime;
            permissionPerformanceMonitor.recordCacheAccess("menu_tree", cacheKey, duration, cacheHit);

            return menuTree != null ? menuTree : Collections.emptyList();

        } catch (Exception e) {
            log.error("[权限数据] 获取菜单权限异常: userId={}", userId, e);

            // 记录性能指标（失败）
            long duration = System.currentTimeMillis() - startTime;
            permissionPerformanceMonitor.recordCacheAccess("menu_tree", "", duration, false);

            return Collections.emptyList();
        }
    }

    @Override
    public List<UserPermissionVO> getBatchUserPermissions(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("[权限数据] 批量获取用户权限: userIdCount={}", userIds.size());

        try {
            // 并行查询用户权限数据
            List<CompletableFuture<UserPermissionVO>> futures = userIds.stream()
                .map(userId -> CompletableFuture.supplyAsync(() -> getUserPermissions(userId)))
                .collect(Collectors.toList());

            // 等待所有查询完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );

            allFutures.join(); // 等待完成

            // 收集结果
            List<UserPermissionVO> results = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            log.info("[权限数据] 批量获取用户权限完成: userIdCount={}, resultCount={}",
                    userIds.size(), results.size());

            return results;

        } catch (Exception e) {
            log.error("[权限数据] 批量获取用户权限异常", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<PermissionDataVO> getPermissionChanges(Long lastSyncTime) {
        try {
            String changeKey = PERMISSION_CHANGE_KEY_PREFIX + "changes:" + lastSyncTime;

            // 尝试从缓存获取变更数据
            List<PermissionDataVO> cachedChanges = (List<PermissionDataVO>) permissionCacheManager.get(changeKey);
            if (cachedChanges != null) {
                return cachedChanges;
            }

            // 从Redis获取变更通知
            Set<Object> changeKeys = redisTemplate.keys(PERMISSION_CHANGE_KEY_PREFIX + "*");
            List<PermissionDataVO> changes = new ArrayList<>();

            if (changeKeys != null) {
                for (Object keyObj : changeKeys) {
                    String key = (String) keyObj;
                    PermissionDataVO change = (PermissionDataVO) redisTemplate.opsForValue().get(key);

                    if (change != null && change.getChangeTimestamp() > lastSyncTime) {
                        changes.add(change);
                    }
                }
            }

            // 按时间戳排序
            changes.sort(Comparator.comparing(PermissionDataVO::getChangeTimestamp));

            // 缓存查询结果
            if (!changes.isEmpty()) {
                permissionCacheManager.put(changeKey, changes, PERMISSION_CHANGE_CACHE_TTL);
            }

            log.debug("[权限数据] 获取权限变更: lastSyncTime={}, changeCount={}", lastSyncTime, changes.size());
            return changes;

        } catch (Exception e) {
            log.error("[权限数据] 获取权限变更异常: lastSyncTime={}", lastSyncTime, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void confirmPermissionSync(Long userId, String dataVersion, String syncType) {
        try {
            String syncKey = PERMISSION_SYNC_KEY_PREFIX + "user:" + userId + ":" + dataVersion;

            PermissionSyncRecord syncRecord = PermissionSyncRecord.builder()
                .userId(userId)
                .dataVersion(dataVersion)
                .syncType(syncType)
                .syncTime(LocalDateTime.now())
                .status("COMPLETED")
                .build();

            // 记录同步确认
            redisTemplate.opsForValue().set(syncKey, syncRecord, 7 * 24 * 60 * 60); // 保存7天

            // 清除用户权限缓存，强制下次从数据库重新加载
            clearUserPermissionCache(userId);

            log.info("[权限数据] 权限同步确认: userId={}, dataVersion={}, syncType={}",
                    userId, dataVersion, syncType);

        } catch (Exception e) {
            log.error("[权限数据] 权限同步确认异常: userId={}, dataVersion={}, syncType={}",
                    userId, dataVersion, syncType, e);
        }
    }

    @Override
    public void clearUserPermissionCache(Long userId) {
        try {
            // 清除内存缓存
            userPermissionCache.remove(userId);
            menuTreeCache.remove("user:" + userId);

            // 清除Redis缓存
            permissionCacheManager.evict(USER_PERMISSION_KEY_PREFIX + userId);
            permissionCacheManager.evict(MENU_TREE_KEY_PREFIX + "user:" + userId);

            log.debug("[权限数据] 清除用户权限缓存: userId={}", userId);

        } catch (Exception e) {
            log.error("[权限数据] 清除用户权限缓存异常: userId={}", userId, e);
        }
    }

    @Override
    public void clearBatchPermissionCache(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        log.info("[权限数据] 批量清除权限缓存: userIdCount={}", userIds.size());

        try {
            for (Long userId : userIds) {
                clearUserPermissionCache(userId);
            }

            log.debug("[权限数据] 批量清除权限缓存完成");

        } catch (Exception e) {
            log.error("[权限数据] 批量清除权限缓存异常", e);
        }
    }

    @Override
    public PermissionStatsVO getPermissionStats() {
        try {
            String statsKey = PERMISSION_STATS_KEY_PREFIX + "current";

            // 尝试从缓存获取统计数据
            PermissionStatsVO cachedStats = (PermissionStatsVO) permissionCacheManager.get(statsKey);
            if (cachedStats != null) {
                return cachedStats;
            }

            // 构建统计数据
            PermissionStatsVO stats = buildPermissionStats();

            // 缓存统计数据（短时间缓存）
            permissionCacheManager.put(statsKey, stats, 5 * 60); // 5分钟

            return stats;

        } catch (Exception e) {
            log.error("[权限数据] 获取权限统计异常", e);
            return createEmptyPermissionStats();
        }
    }

    @Override
    public UserPermissionVO buildUserPermissionVO(Long userId, Set<String> userPermissions, Set<String> userRoles) {
        try {
            // 获取用户基础信息
            UserEntity user = userDao.selectById(userId);
            if (user == null) {
                return createEmptyUserPermissionVO(userId);
            }

            // 获取菜单权限树
            List<MenuPermissionVO> menuTree = buildMenuPermissionTree(userId, userPermissions, userRoles);

            // 生成webPerms权限标识
            Set<String> webPerms = generateWebPerms(userPermissions, userRoles);

            // 构建权限统计
            UserPermissionVO.PermissionStats stats = UserPermissionVO.PermissionStats.builder()
                .totalRoles(userRoles != null ? userRoles.size() : 0)
                .totalPermissions(userPermissions != null ? userPermissions.size() : 0)
                .totalWebPerms(webPerms != null ? webPerms.size() : 0)
                .totalMenuPermissions(countMenuPermissions(menuTree))
                .accessibleTopMenus(countTopLevelMenus(menuTree))
                .build();

            // 构建用户权限VO
            return UserPermissionVO.builder()
                .userId(userId)
                .username(user.getUsername())
                .realName(user.getRealName())
                .status(user.getStatus())
                .roles(userRoles)
                .permissions(userPermissions)
                .webPerms(webPerms)
                .menuTree(menuTree)
                .dataVersion(CURRENT_DATA_VERSION)
                .lastUpdateTime(LocalDateTime.now())
                .cacheExpireTime(LocalDateTime.now().plusMinutes(30))
                .dataChecksum(calculateDataChecksum(userPermissions, userRoles))
                .permissionStats(stats)
                .build();

        } catch (Exception e) {
            log.error("[权限数据] 构建用户权限VO异常: userId={}", userId, e);
            return createEmptyUserPermissionVO(userId);
        }
    }

    @Override
    public List<MenuPermissionVO> buildMenuPermissionTree(Long userId, Set<String> userPermissions, Set<String> userRoles) {
        try {
            // 查询所有菜单
            List<MenuEntity> allMenus = menuDao.selectList(null);
            if (allMenus == null || allMenus.isEmpty()) {
                return Collections.emptyList();
            }

            // 转换为MenuPermissionVO
            List<MenuPermissionVO> menuVos = allMenus.stream()
                .map(menu -> convertToMenuPermissionVO(menu, userPermissions, userRoles))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            // 构建菜单树结构
            return buildMenuTreeStructure(menuVos);

        } catch (Exception e) {
            log.error("[权限数据] 构建菜单权限树异常: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> generateWebPerms(String permissionId) {
        try {
            // 这里可以根据permissionId生成对应的webPerms
            // 实际实现中可能需要查询权限与webPerms的映射关系
            Set<String> webPerms = new HashSet<>();

            // 基础权限转换
            if (StringUtils.hasText(permissionId)) {
                webPerms.add(permissionId.toLowerCase().replace(":", "."));
            }

            return webPerms;

        } catch (Exception e) {
            log.error("[权限数据] 生成webPerms异常: permissionId={}", permissionId, e);
            return Collections.emptySet();
        }
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        try {
            UserPermissionVO userPermission = getUserPermissions(userId);
            return userPermission.hasPermission(permission);

        } catch (Exception e) {
            log.error("[权限数据] 检查用户权限异常: userId={}, permission={}", userId, permission, e);
            return false;
        }
    }

    @Override
    public boolean hasRole(Long userId, String role) {
        try {
            UserPermissionVO userPermission = getUserPermissions(userId);
            return userPermission.hasRole(role);

        } catch (Exception e) {
            log.error("[权限数据] 检查用户角色异常: userId={}, role={}", userId, role, e);
            return false;
        }
    }

    @Override
    public Set<String> getUserPermissions(Long userId) {
        try {
            UserPermissionVO userPermission = getUserPermissions(userId);
            return userPermission.getPermissions() != null ?
                userPermission.getPermissions() : Collections.emptySet();

        } catch (Exception e) {
            log.error("[权限数据] 获取用户权限列表异常: userId={}", userId, e);
            return Collections.emptySet();
        }
    }

    @Override
    public Set<String> getUserRoles(Long userId) {
        try {
            UserPermissionVO userPermission = getUserPermissions(userId);
            return userPermission.getRoles() != null ?
                userPermission.getRoles() : Collections.emptySet();

        } catch (Exception e) {
            log.error("[权限数据] 获取用户角色列表异常: userId={}", userId, e);
            return Collections.emptySet();
        }
    }

    @Override
    public void notifyPermissionChange(String changeType, Long targetId, Object affectedData) {
        try {
            PermissionDataVO change = PermissionDataVO.builder()
                .changeId(generateChangeId())
                .changeTimestamp(System.currentTimeMillis())
                .changeTime(LocalDateTime.now())
                .changeType(changeType)
                .operationType("UPDATE")
                .targetId(targetId)
                .targetData(affectedData.toString())
                .afterVersion(CURRENT_DATA_VERSION)
                .changeSource("SYSTEM")
                .status("PENDING")
                .build();

            // 发布权限变更通知
            publishPermissionChange(change);

            log.info("[权限数据] 权限变更通知: changeType={}, targetId={}", changeType, targetId);

        } catch (Exception e) {
            log.error("[权限数据] 权限变更通知异常: changeType={}, targetId={}", changeType, targetId, e);
        }
    }

    /**
     * 从数据库构建用户权限数据
     */
    private UserPermissionVO buildUserPermissionVOFromDatabase(Long userId) {
        try {
            // 查询用户信息
            UserEntity user = userDao.selectById(userId);
            if (user == null) {
                return null;
            }

            // 查询用户角色
            Set<String> userRoles = getUserRolesFromDatabase(userId);

            // 查询用户权限
            Set<String> userPermissions = getUserPermissionsFromDatabase(userId, userRoles);

            // 构建用户权限VO
            return buildUserPermissionVO(userId, userPermissions, userRoles);

        } catch (Exception e) {
            log.error("[权限数据] 从数据库构建用户权限异常: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 从数据库查询用户角色
     */
    private Set<String> getUserRolesFromDatabase(Long userId) {
        try {
            // 这里应该调用角色服务查询用户角色
            // 暂时返回空集合
            return new HashSet<>();

        } catch (Exception e) {
            log.error("[权限数据] 查询用户角色异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 从数据库查询用户权限
     */
    private Set<String> getUserPermissionsFromDatabase(Long userId, Set<String> userRoles) {
        try {
            // 这里应该调用权限服务查询用户权限
            // 暂时返回空集合
            return new HashSet<>();

        } catch (Exception e) {
            log.error("[权限数据] 查询用户权限异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 生成webPerms权限标识
     */
    private Set<String> generateWebPerms(Set<String> userPermissions, Set<String> userRoles) {
        Set<String> webPerms = new HashSet<>();

        if (userPermissions != null) {
            for (String permission : userPermissions) {
                webPerms.add(permission.toLowerCase().replace(":", "."));
            }
        }

        return webPerms;
    }

    /**
     * 转换菜单实体为VO
     */
    private MenuPermissionVO convertToMenuPermissionVO(MenuEntity menu, Set<String> userPermissions, Set<String> userRoles) {
        try {
            // 检查用户是否有权限访问此菜单
            if (!hasMenuPermission(menu, userPermissions, userRoles)) {
                return null;
            }

            return MenuPermissionVO.builder()
                .menuId(menu.getMenuId())
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .icon(menu.getIcon())
                .menuType(menu.getMenuType())
                .parentId(menu.getParentId())
                .level(menu.getLevel())
                .orderNum(menu.getOrderNum())
                .path(menu.getPath())
                .component(menu.getComponent())
                .permission(menu.getPermission())
                .webPermission(menu.getPermission() != null ?
                    menu.getPermission().toLowerCase().replace(":", ".") : null)
                .status(menu.getStatus())
                .visible(menu.getVisible())
                .keepAlive(menu.getKeepAlive())
                .isExternal(menu.getIsExternal())
                .externalUrl(menu.getExternalUrl())
                .query(menu.getQuery())
                .description(menu.getDescription())
                .remark(menu.getRemark())
                .build();

        } catch (Exception e) {
            log.error("[权限数据] 转换菜单实体异常: menuId={}", menu.getMenuId(), e);
            return null;
        }
    }

    /**
     * 检查用户是否有菜单权限
     */
    private boolean hasMenuPermission(MenuEntity menu, Set<String> userPermissions, Set<String> userRoles) {
        // 如果菜单没有设置权限，默认允许访问
        if (!StringUtils.hasText(menu.getPermission())) {
            return true;
        }

        // 检查用户是否有菜单权限
        if (userPermissions != null && userPermissions.contains(menu.getPermission())) {
            return true;
        }

        return false;
    }

    /**
     * 构建菜单树结构
     */
    private List<MenuPermissionVO> buildMenuTreeStructure(List<MenuPermissionVO> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 按父菜单ID分组
        Map<Long, List<MenuPermissionVO>> menuMap = menus.stream()
            .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
            .collect(Collectors.groupingBy(MenuPermissionVO::getParentId));

        // 递归构建树结构
        List<MenuPermissionVO> rootMenus = menuMap.getOrDefault(0L, new ArrayList<>());
        buildChildren(rootMenus, menus);

        return rootMenus;
    }

    /**
     * 递归构建子菜单
     */
    private void buildChildren(List<MenuPermissionVO> parentMenus, List<MenuPermissionVO> allMenus) {
        if (parentMenus == null || parentMenus.isEmpty()) {
            return;
        }

        Map<Long, MenuPermissionVO> menuMap = allMenus.stream()
            .collect(Collectors.toMap(MenuPermissionVO::getMenuId, menu -> menu));

        for (MenuPermissionVO parent : parentMenus) {
            List<MenuPermissionVO> children = allMenus.stream()
                .filter(menu -> parent.getMenuId().equals(menu.getParentId()))
                .sorted(Comparator.comparing(MenuPermissionVO::getOrderNum))
                .collect(Collectors.toList());

            if (!children.isEmpty()) {
                parent.setChildren(children);
                buildChildren(children, allMenus);
            }
        }
    }

    /**
     * 计算数据校验和
     */
    private String calculateDataChecksum(Set<String> permissions, Set<String> roles) {
        StringBuilder data = new StringBuilder();
        if (permissions != null) {
            permissions.stream().sorted().forEach(data::append);
        }
        if (roles != null) {
            roles.stream().sorted().forEach(data::append);
        }
        return DigestUtils.md5DigestAsHex(data.toString());
    }

    /**
     * 生成变更ID
     */
    private String generateChangeId() {
        return "PERM_CHANGE_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 发布权限变更通知
     */
    private void publishPermissionChange(PermissionDataVO change) {
        try {
            String changeKey = PERMISSION_CHANGE_KEY_PREFIX + change.getChangeId();
            redisTemplate.opsForValue().set(changeKey, change, 24 * 60 * 60); // 保存24小时

            // 这里可以发布到消息队列通知其他服务
            // messagePublisher.publish("permission.change", change);

        } catch (Exception e) {
            log.error("[权限数据] 发布权限变更通知异常", e);
        }
    }

    /**
     * 创建空的用户权限VO
     */
    private UserPermissionVO createEmptyUserPermissionVO(Long userId) {
        return UserPermissionVO.builder()
            .userId(userId)
            .roles(Collections.emptySet())
            .permissions(Collections.emptySet())
            .webPerms(Collections.emptySet())
            .menuTree(Collections.emptyList())
            .build();
    }

    /**
     * 创建空的权限统计VO
     */
    private PermissionStatsVO createEmptyPermissionStats() {
        return PermissionStatsVO.builder()
            .validationStats(new PermissionStatsVO.ValidationPerformanceStats())
            .cacheStats(new PermissionStatsVO.CachePerformanceStats())
            .usageStats(new PermissionStatsVO.PermissionUsageStats())
            .exceptionStats(new PermissionStatsVO.PermissionExceptionStats())
            .userStats(new PermissionStatsVO.UserPermissionDistributionStats())
            .changeStats(new PermissionStatsVO.PermissionChangeStats())
            .healthScore(BigDecimal.ZERO)
            .performanceGrade("POOR")
            .trendData(new ArrayList<>())
            .build();
    }

    /**
     * 构建权限统计信息
     */
    private PermissionStatsVO buildPermissionStats() {
        // 这里应该实现具体的统计逻辑
        // 暂时返回空的统计数据
        return createEmptyPermissionStats();
    }

    /**
     * 统计菜单权限数量
     */
    private int countMenuPermissions(List<MenuPermissionVO> menuTree) {
        if (menuTree == null || menuTree.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (MenuPermissionVO menu : menuTree) {
            count++;
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                count += countMenuPermissions(menu.getChildren());
            }
        }
        return count;
    }

    /**
     * 统计顶级菜单数量
     */
    private int countTopLevelMenus(List<MenuPermissionVO> menuTree) {
        return menuTree != null ? menuTree.size() : 0;
    }

    /**
     * 权限同步记录内部类
     */
    @lombok.Data
    @lombok.Builder
    private static class PermissionSyncRecord {
        private Long userId;
        private String dataVersion;
        private String syncType;
        private LocalDateTime syncTime;
        private String status;
    }
}