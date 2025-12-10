package net.lab1024.sa.common.preference.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.preference.dao.UserPreferenceDao;
import net.lab1024.sa.common.preference.entity.UserPreferenceEntity;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 用户偏好设置管理器
 * <p>
 * 提供用户偏好设置的完整管理功能，包括：
 * - 偏好设置的增删改查
 * - 多级缓存管理
 * - 默认值处理
 * - 偏好设置同步
 * - 统计分析
 * </p>
 * <p>
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入所有依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除@Component注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class UserPreferenceManager {

    private final UserPreferenceDao userPreferenceDao;
    private final RedisTemplate<String, Object> redisTemplate;

    // 本地缓存
    private final Map<String, Object> localCache = new ConcurrentHashMap<>();
    private static final Duration CACHE_DURATION = Duration.ofMinutes(15);

    // 缓存键前缀
    private static final String CACHE_PREFIX = "user:preference:";
    private static final String USER_LIST_CACHE_PREFIX = "user:preference:list:";
    @SuppressWarnings("unused") // 预留字段，用于未来系统默认偏好设置缓存
    private static final String SYSTEM_DEFAULTS_CACHE_KEY = "system:preference:defaults";

    // 系统默认偏好设置缓存
    private volatile List<UserPreferenceEntity> systemDefaultsCache;

    /**
     * 构造函数注入所有依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param userPreferenceDao 用户偏好设置DAO
     * @param redisTemplate Redis模板
     */
    public UserPreferenceManager(UserPreferenceDao userPreferenceDao, RedisTemplate<String, Object> redisTemplate) {
        this.userPreferenceDao = Objects.requireNonNull(userPreferenceDao, "userPreferenceDao不能为null");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate不能为null");
        initSystemDefaultsCache();
    }

    /**
     * 初始化系统默认偏好设置缓存
     */
    private void initSystemDefaultsCache() {
        try {
            this.systemDefaultsCache = userPreferenceDao.selectSystemDefaults();
            if (this.systemDefaultsCache == null) {
                this.systemDefaultsCache = createSystemDefaults();
                log.info("[偏好管理] 已创建系统默认偏好设置");
            }
            log.info("[偏好管理] 系统默认偏好设置加载完成，共{}项", this.systemDefaultsCache.size());
        } catch (Exception e) {
            log.error("[偏好管理] 初始化系统默认偏好设置失败", e);
            this.systemDefaultsCache = createSystemDefaults();
        }
    }

    /**
     * 创建系统默认偏好设置
     */
    private List<UserPreferenceEntity> createSystemDefaults() {
        List<UserPreferenceEntity> defaults = new ArrayList<>();

        // 界面偏好
        defaults.add(createDefaultPreference("interface", "language", "zh_CN", "string", "界面语言"));
        defaults.add(createDefaultPreference("interface", "timezone", "Asia/Shanghai", "string", "时区设置"));
        defaults.add(createDefaultPreference("interface", "dateFormat", "yyyy-MM-dd", "string", "日期格式"));
        defaults.add(createDefaultPreference("interface", "timeFormat", "HH:mm:ss", "string", "时间格式"));
        defaults.add(createDefaultPreference("interface", "pageSize", "20", "number", "分页大小"));
        defaults.add(createDefaultPreference("interface", "autoRefreshInterval", "30", "number", "自动刷新间隔(秒)"));

        // 行为偏好
        defaults.add(createDefaultPreference("behavior", "doubleClickAction", "edit", "string", "双击操作"));
        defaults.add(createDefaultPreference("behavior", "confirmBeforeDelete", "true", "boolean", "删除前确认"));
        defaults.add(createDefaultPreference("behavior", "rememberLastPage", "true", "boolean", "记住最后访问页面"));
        defaults.add(createDefaultPreference("behavior", "autoSaveDraft", "true", "boolean", "自动保存草稿"));

        // 通知偏好
        defaults.add(createDefaultPreference("notification", "enableEmail", "true", "boolean", "启用邮件通知"));
        defaults.add(createDefaultPreference("notification", "enableSms", "false", "boolean", "启用短信通知"));
        defaults.add(createDefaultPreference("notification", "enablePush", "true", "boolean", "启用推送通知"));
        defaults.add(createDefaultPreference("notification", "quietHours", "22:00-08:00", "string", "免打扰时段"));

        // 仪表盘偏好
        defaults.add(createDefaultPreference("dashboard", "layout", "grid", "string", "仪表盘布局"));
        defaults.add(createDefaultPreference("dashboard", "widgets", "[]", "json", "组件配置"));
        defaults.add(createDefaultPreference("dashboard", "refreshInterval", "60", "number", "刷新间隔(秒)"));

        // 保存系统默认偏好设置
        for (UserPreferenceEntity preference : defaults) {
            preference.setUserId(null); // 系统偏好
            preference.setIsSystem(1);
            preference.setStatus(1);
            try {
                userPreferenceDao.insert(preference);
            } catch (Exception e) {
                log.warn("[偏好管理] 保存系统默认偏好失败: key={}", preference.getPreferenceKey(), e);
            }
        }

        return defaults;
    }

    /**
     * 创建默认偏好实体
     */
    private UserPreferenceEntity createDefaultPreference(String category, String key, String value, String type, String desc) {
        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setPreferenceCategory(category);
        preference.setPreferenceKey(key);
        preference.setPreferenceValue(value);
        preference.setDefaultValue(value);
        preference.setPreferenceType(type);
        preference.setPreferenceDesc(desc);
        preference.setIsVisible(1);
        preference.setIsEditable(1);
        preference.setSortOrder(0);
        preference.setDeviceType("all");
        preference.setPreferenceWeight(0);
        return preference;
    }

    /**
     * 获取用户偏好值
     *
     * @param userId 用户ID
     * @param category 类别
     * @param key 键
     * @param deviceType 设备类型
     * @param defaultValue 默认值
     * @return 偏好值
     */
    public String getUserPreference(Long userId, String category, String key, String deviceType, String defaultValue) {
        String cacheKey = CACHE_PREFIX + userId + ":" + category + ":" + key + ":" + deviceType;

        // L1本地缓存
        Object cached = localCache.get(cacheKey);
        if (cached != null) {
            log.debug("[偏好管理] L1缓存命中: userId={}, category={}, key={}", userId, category, key);
            return cached.toString();
        }

        // L2 Redis缓存
        try {
            cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                localCache.put(cacheKey, cached);
                log.debug("[偏好管理] L2缓存命中: userId={}, category={}, key={}", userId, category, key);
                return cached.toString();
            }
        } catch (Exception e) {
            log.warn("[偏好管理] Redis缓存查询失败: userId={}, category={}, key={}", userId, category, key, e);
        }

        // L3数据库查询
        String preferenceValue = loadPreferenceFromDatabase(userId, category, key, deviceType, defaultValue);
        if (preferenceValue != null) {
            // 更新缓存
            updateCache(cacheKey, preferenceValue);
            log.debug("[偏好管理] 数据库加载成功: userId={}, category={}, key={}, value={}",
                    userId, category, key, preferenceValue);
        }

        return preferenceValue;
    }

    /**
     * 从数据库加载偏好值
     */
    private String loadPreferenceFromDatabase(Long userId, String category, String key, String deviceType, String defaultValue) {
        // 查询用户偏好
        UserPreferenceEntity preference = userPreferenceDao.selectByUserIdAndCategoryAndKeyAndDeviceType(
                userId, category, key, deviceType);

        if (preference == null) {
            // 查询系统默认偏好
            UserPreferenceEntity systemDefault = findSystemDefault(key);
            if (systemDefault != null) {
                preference = systemDefault;
                // 为用户创建偏好记录
                createUserPreference(userId, systemDefault);
            }
        }

        return preference != null ? preference.getPreferenceValue() : defaultValue;
    }

    /**
     * 查找系统默认偏好
     */
    private UserPreferenceEntity findSystemDefault(String key) {
        return systemDefaultsCache.stream()
                .filter(pref -> key.equals(pref.getPreferenceKey()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 为用户创建偏好记录
     */
    private void createUserPreference(Long userId, UserPreferenceEntity systemDefault) {
        try {
            UserPreferenceEntity userPreference = new UserPreferenceEntity();
            userPreference.setUserId(userId);
            userPreference.setPreferenceCategory(systemDefault.getPreferenceCategory());
            userPreference.setPreferenceKey(systemDefault.getPreferenceKey());
            userPreference.setPreferenceValue(systemDefault.getDefaultValue());
            userPreference.setPreferenceType(systemDefault.getPreferenceType());
            userPreference.setDefaultValue(systemDefault.getDefaultValue());
            userPreference.setPreferenceDesc(systemDefault.getPreferenceDesc());
            userPreference.setIsSystem(0);
            userPreference.setIsVisible(systemDefault.getIsVisible());
            userPreference.setIsEditable(systemDefault.getIsEditable());
            userPreference.setSortOrder(systemDefault.getSortOrder());
            userPreference.setValidationRule(systemDefault.getValidationRule());
            userPreference.setOptions(systemDefault.getOptions());
            userPreference.setPreferenceGroup(systemDefault.getPreferenceGroup());
            userPreference.setDeviceType(systemDefault.getDeviceType());
            userPreference.setStatus(1);

            userPreferenceDao.insert(userPreference);
            log.debug("[偏好管理] 为用户创建偏好记录: userId={}, key={}", userId, systemDefault.getPreferenceKey());
        } catch (Exception e) {
            log.warn("[偏好管理] 创建用户偏好记录失败: userId={}, key={}", userId, systemDefault.getPreferenceKey(), e);
        }
    }

    /**
     * 设置用户偏好值
     *
     * @param userId 用户ID
     * @param category 类别
     * @param key 键
     * @param value 值
     * @param deviceType 设备类型
     * @return 是否设置成功
     */
    public boolean setUserPreference(Long userId, String category, String key, String value, String deviceType) {
        try {
            // 查询现有偏好
            UserPreferenceEntity existing = userPreferenceDao.selectByUserIdAndCategoryAndKeyAndDeviceType(
                    userId, category, key, deviceType);

            if (existing != null) {
                // 验证值
                if (!existing.validateValue(value)) {
                    log.warn("[偏好管理] 偏好值验证失败: userId={}, key={}, value={}", userId, key, value);
                    return false;
                }

                // 更新偏好值
                int result = userPreferenceDao.updatePreferenceValue(existing.getId(), value, userId);
                if (result > 0) {
                    // 清除缓存
                    clearPreferenceCache(userId, category, key, deviceType);
                    log.info("[偏好管理] 更新偏好成功: userId={}, category={}, key={}, value={}", userId, category, key, value);
                    return true;
                }
            } else {
                // 创建新偏好
                UserPreferenceEntity systemDefault = findSystemDefault(key);
                if (systemDefault != null) {
                    createUserPreference(userId, systemDefault);
                    // 然后更新值
                    return setUserPreference(userId, category, key, value, deviceType);
                }
            }

            return false;

        } catch (Exception e) {
            log.error("[偏好管理] 设置偏好失败: userId={}, category={}, key={}, value={}", userId, category, key, value, e);
            return false;
        }
    }

    /**
     * 批量获取用户偏好
     *
     * @param userId 用户ID
     * @param category 类别
     * @param deviceType 设备类型
     * @return 偏好值映射
     */
    public Map<String, String> getUserPreferences(Long userId, String category, String deviceType) {
        String cacheKey = USER_LIST_CACHE_PREFIX + userId + ":" + category + ":" + deviceType;

        // 尝试从缓存获取
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> cached = (Map<String, String>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("[偏好管理] 偏好列表缓存命中: userId={}, category={}", userId, category);
                return cached;
            }
        } catch (Exception e) {
            log.warn("[偏好管理] 偏好列表缓存查询失败: userId={}, category={}", userId, category, e);
        }

        // 从数据库查询
        List<UserPreferenceEntity> preferences = userPreferenceDao.selectByUserIdAndCategory(userId, category);
        Map<String, String> result = preferences.stream()
                .filter(pref -> deviceType.equals(pref.getDeviceType()) || "all".equals(pref.getDeviceType()))
                .collect(Collectors.toMap(
                        UserPreferenceEntity::getPreferenceKey,
                        UserPreferenceEntity::getPreferenceValue,
                        (existing, replacement) -> replacement // 如果有重复键，使用设备类型特定的值
                ));

        // 补充系统默认值
        systemDefaultsCache.stream()
                .filter(pref -> category.equals(pref.getPreferenceCategory()))
                .forEach(systemDefault -> {
                    result.putIfAbsent(systemDefault.getPreferenceKey(), systemDefault.getDefaultValue());
                });

        // 更新缓存
        try {
            if (cacheKey != null && result != null) {
                Duration duration = Objects.requireNonNull(CACHE_DURATION, "CACHE_DURATION不能为null");
                redisTemplate.opsForValue().set(cacheKey, result, duration);
            }
        } catch (Exception e) {
            log.warn("[偏好管理] 偏好列表缓存更新失败: userId={}, category={}", userId, category, e);
        }

        return result;
    }

    /**
     * 批量设置用户偏好
     *
     * @param userId 用户ID
     * @param preferences 偏好映射
     * @param category 类别
     * @param deviceType 设备类型
     * @return 设置成功的数量
     */
    public int batchSetUserPreferences(Long userId, Map<String, String> preferences, String category, String deviceType) {
        int successCount = 0;
        List<String> clearedKeys = new ArrayList<>();

        try {
            for (Map.Entry<String, String> entry : preferences.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (setUserPreference(userId, category, key, value, deviceType)) {
                    successCount++;
                    clearedKeys.add(key);
                }
            }

            // 批量清除缓存
            if (!clearedKeys.isEmpty()) {
                clearPreferenceCacheList(userId, category, deviceType);
            }

            log.info("[偏好管理] 批量设置偏好完成: userId={}, category={}, successCount={}", userId, category, successCount);
            return successCount;

        } catch (Exception e) {
            log.error("[偏好管理] 批量设置偏好失败: userId={}, category={}", userId, category, e);
            return 0;
        }
    }

    /**
     * 重置用户偏好到默认值
     *
     * @param userId 用户ID
     * @param category 类别（可选）
     * @return 重置的数量
     */
    public int resetUserPreferencesToDefaults(Long userId, String category) {
        try {
            int resetCount = userPreferenceDao.resetToDefaults(userId, category, userId);

            // 清除相关缓存
            if (category != null) {
                clearPreferenceCacheList(userId, category, null);
            } else {
                clearAllUserPreferenceCache(userId);
            }

            log.info("[偏好管理] 重置偏好完成: userId={}, category={}, resetCount={}", userId, category, resetCount);
            return resetCount;

        } catch (Exception e) {
            log.error("[偏好管理] 重置偏好失败: userId={}, category={}", userId, category, e);
            return 0;
        }
    }

    /**
     * 获取用户偏好统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    public Map<String, Object> getUserPreferenceStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 总偏好数量
            int totalCount = userPreferenceDao.countUserPreferences(userId, null);
            statistics.put("totalCount", totalCount);

            // 各类别偏好数量
            List<String> categories = userPreferenceDao.selectPreferenceCategories();
            Map<String, Integer> categoryCounts = new HashMap<>();
            for (String category : categories) {
                int count = userPreferenceDao.countUserPreferences(userId, category);
                categoryCounts.put(category, count);
            }
            statistics.put("categoryCounts", categoryCounts);

            // 最常用偏好
            List<UserPreferenceEntity> mostUsed = userPreferenceDao.selectMostUsedPreferences(userId, 5);
            statistics.put("mostUsed", mostUsed.stream()
                    .map(pref -> Map.of(
                            "key", pref.getPreferenceKey(),
                            "category", pref.getPreferenceCategory(),
                            "updateCount", pref.getUpdateCount() != null ? pref.getUpdateCount() : 0
                    ))
                    .toList());

            // 偏好分组
            List<String> groups = userPreferenceDao.selectUserPreferenceGroups(userId);
            statistics.put("groups", groups);

            log.debug("[偏好管理] 偏好统计获取成功: userId={}", userId);
            return statistics;

        } catch (Exception e) {
            log.error("[偏好管理] 获取偏好统计失败: userId={}", userId, e);
            return statistics;
        }
    }

    /**
     * 同步系统默认偏好到用户
     *
     * @param userId 用户ID
     * @return 同步数量
     */
    public int syncSystemDefaultsToUser(Long userId) {
        try {
            int syncCount = userPreferenceDao.syncSystemDefaultsToUser(userId, userId);

            // 清除用户偏好缓存
            clearAllUserPreferenceCache(userId);

            log.info("[偏好管理] 同步系统默认偏好完成: userId={}, syncCount={}", userId, syncCount);
            return syncCount;

        } catch (Exception e) {
            log.error("[偏好管理] 同步系统默认偏好失败: userId={}", userId, e);
            return 0;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 更新缓存
     */
    private void updateCache(String cacheKey, String value) {
        // 更新本地缓存
        localCache.put(cacheKey, value);

        // 更新Redis缓存
        try {
            if (cacheKey != null && value != null) {
                Duration duration = Objects.requireNonNull(CACHE_DURATION, "CACHE_DURATION不能为null");
                redisTemplate.opsForValue().set(cacheKey, value, duration);
            }
        } catch (Exception e) {
            log.warn("[偏好管理] Redis缓存更新失败: cacheKey={}", cacheKey, e);
        }
    }

    /**
     * 清除单个偏好缓存
     */
    private void clearPreferenceCache(Long userId, String category, String key, String deviceType) {
        String cacheKey = CACHE_PREFIX + userId + ":" + category + ":" + key + ":" + deviceType;

        // 清除本地缓存
        localCache.remove(cacheKey);

        // 清除Redis缓存
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("[偏好管理] 清除缓存失败: cacheKey={}", cacheKey, e);
        }
    }

    /**
     * 清除偏好列表缓存
     */
    private void clearPreferenceCacheList(Long userId, String category, String deviceType) {
        String cacheKey = USER_LIST_CACHE_PREFIX + userId + ":" + category + (deviceType != null ? ":" + deviceType : "");

        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("[偏好管理] 清除列表缓存失败: cacheKey={}", cacheKey, e);
        }
    }

    /**
     * 清除用户所有偏好缓存
     */
    private void clearAllUserPreferenceCache(Long userId) {
        // 清除本地缓存
        localCache.keySet().removeIf(key -> key.startsWith(CACHE_PREFIX + userId + ":"));

        // 清除Redis缓存
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + userId + ":*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            keys = redisTemplate.keys(USER_LIST_CACHE_PREFIX + userId + ":*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("[偏好管理] 清除用户所有缓存失败: userId={}", userId, e);
        }
    }
}
