package net.lab1024.sa.common.preference.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.preference.dao.UserPreferenceDao;
import net.lab1024.sa.common.preference.entity.UserPreferenceEntity;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 用户偏好设置管理器
 * <p>
 * 提供用户偏好设置的完整管理功能，包括：
 * - 偏好设置的增删改查
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
 * <p>
 * ⚠️ <strong>缓存说明：</strong>缓存逻辑已移除，缓存应在Service层使用@Cacheable注解处理。
 * 如果未来需要使用时，请创建PreferenceService并在Service层使用@Cacheable注解。
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除缓存逻辑，符合CLAUDE.md规范，缓存应在Service层使用@Cacheable注解
 */
@Slf4j
public class UserPreferenceManager {

    private final UserPreferenceDao userPreferenceDao;

    // 系统默认偏好设置缓存（仅用于初始化，不用于运行时缓存）
    private volatile List<UserPreferenceEntity> systemDefaultsCache;

    /**
     * 构造函数注入所有依赖
     * <p>
     * ⚠️ 注意：已移除RedisTemplate依赖，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param userPreferenceDao 用户偏好设置DAO
     */
    public UserPreferenceManager(UserPreferenceDao userPreferenceDao) {
        this.userPreferenceDao = Objects.requireNonNull(userPreferenceDao, "userPreferenceDao不能为null");
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[偏好管理] 初始化系统默认偏好设置参数错误: error={}", e.getMessage());
            this.systemDefaultsCache = createSystemDefaults(); // 降级处理：使用默认值
        } catch (BusinessException e) {
            log.warn("[偏好管理] 初始化系统默认偏好设置业务异常: code={}, message={}", e.getCode(), e.getMessage());
            this.systemDefaultsCache = createSystemDefaults(); // 降级处理：使用默认值
        } catch (SystemException e) {
            log.error("[偏好管理] 初始化系统默认偏好设置系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            this.systemDefaultsCache = createSystemDefaults(); // 降级处理：使用默认值
        } catch (Exception e) {
            log.error("[偏好管理] 初始化系统默认偏好设置未知异常", e);
            this.systemDefaultsCache = createSystemDefaults(); // 降级处理：使用默认值
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
            } catch (IllegalArgumentException | ParamException e) {
                log.warn("[偏好管理] 保存系统默认偏好参数错误: key={}, error={}", preference.getPreferenceKey(), e.getMessage());
                // 单个偏好保存失败不影响其他偏好，继续执行
            } catch (BusinessException e) {
                log.warn("[偏好管理] 保存系统默认偏好业务异常: key={}, code={}, message={}", preference.getPreferenceKey(), e.getCode(), e.getMessage());
                // 单个偏好保存失败不影响其他偏好，继续执行
            } catch (SystemException e) {
                log.error("[偏好管理] 保存系统默认偏好系统异常: key={}, code={}, message={}", preference.getPreferenceKey(), e.getCode(), e.getMessage(), e);
                // 单个偏好保存失败不影响其他偏好，继续执行
            } catch (Exception e) {
                log.warn("[偏好管理] 保存系统默认偏好未知异常: key={}", preference.getPreferenceKey(), e);
                // 单个偏好保存失败不影响其他偏好，继续执行
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
     * 获取用户偏好值（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param userId 用户ID
     * @param category 类别
     * @param key 键
     * @param deviceType 设备类型
     * @param defaultValue 默认值
     * @return 偏好值
     */
    public String getUserPreference(Long userId, String category, String key, String deviceType, String defaultValue) {
        // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
        String preferenceValue = loadPreferenceFromDatabase(userId, category, key, deviceType, defaultValue);
        log.debug("[偏好管理] 数据库加载成功: userId={}, category={}, key={}, value={}",
                userId, category, key, preferenceValue);
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[偏好管理] 创建用户偏好记录参数错误: userId={}, key={}, error={}", userId, systemDefault.getPreferenceKey(), e.getMessage());
            // 创建失败不影响主流程，继续执行
        } catch (BusinessException e) {
            log.warn("[偏好管理] 创建用户偏好记录业务异常: userId={}, key={}, code={}, message={}", userId, systemDefault.getPreferenceKey(), e.getCode(), e.getMessage());
            // 创建失败不影响主流程，继续执行
        } catch (SystemException e) {
            log.error("[偏好管理] 创建用户偏好记录系统异常: userId={}, key={}, code={}, message={}", userId, systemDefault.getPreferenceKey(), e.getCode(), e.getMessage(), e);
            // 创建失败不影响主流程，继续执行
        } catch (Exception e) {
            log.warn("[偏好管理] 创建用户偏好记录未知异常: userId={}, key={}", userId, systemDefault.getPreferenceKey(), e);
            // 创建失败不影响主流程，继续执行
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
                    // ⚠️ 缓存清除应在Service层使用@CacheEvict注解处理
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[偏好管理] 设置偏好参数错误: userId={}, category={}, key={}, value={}, error={}", userId, category, key, value, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[偏好管理] 设置偏好业务异常: userId={}, category={}, key={}, value={}, code={}, message={}", userId, category, key, value, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[偏好管理] 设置偏好系统异常: userId={}, category={}, key={}, value={}, code={}, message={}", userId, category, key, value, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[偏好管理] 设置偏好未知异常: userId={}, category={}, key={}, value={}", userId, category, key, value, e);
            return false; // For boolean return methods, return false on unknown error
        }
    }

    /**
     * 批量获取用户偏好（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param userId 用户ID
     * @param category 类别
     * @param deviceType 设备类型
     * @return 偏好值映射
     */
    public Map<String, String> getUserPreferences(Long userId, String category, String deviceType) {
        // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
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

            // ⚠️ 缓存清除应在Service层使用@CacheEvict注解处理

            log.info("[偏好管理] 批量设置偏好完成: userId={}, category={}, successCount={}", userId, category, successCount);
            return successCount;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[偏好管理] 批量设置偏好参数错误: userId={}, category={}, error={}", userId, category, e.getMessage());
            return 0; // For int return methods, return 0 on parameter error
        } catch (BusinessException e) {
            log.warn("[偏好管理] 批量设置偏好业务异常: userId={}, category={}, code={}, message={}", userId, category, e.getCode(), e.getMessage());
            return 0; // For int return methods, return 0 on business error
        } catch (SystemException e) {
            log.error("[偏好管理] 批量设置偏好系统异常: userId={}, category={}, code={}, message={}", userId, category, e.getCode(), e.getMessage(), e);
            return 0; // For int return methods, return 0 on system error
        } catch (Exception e) {
            log.error("[偏好管理] 批量设置偏好未知异常: userId={}, category={}", userId, category, e);
            return 0; // For int return methods, return 0 on unknown error
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

            // ⚠️ 缓存清除应在Service层使用@CacheEvict注解处理

            log.info("[偏好管理] 重置偏好完成: userId={}, category={}, resetCount={}", userId, category, resetCount);
            return resetCount;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[偏好管理] 重置偏好参数错误: userId={}, category={}, error={}", userId, category, e.getMessage());
            return 0; // For int return methods, return 0 on parameter error
        } catch (BusinessException e) {
            log.warn("[偏好管理] 重置偏好业务异常: userId={}, category={}, code={}, message={}", userId, category, e.getCode(), e.getMessage());
            return 0; // For int return methods, return 0 on business error
        } catch (SystemException e) {
            log.error("[偏好管理] 重置偏好系统异常: userId={}, category={}, code={}, message={}", userId, category, e.getCode(), e.getMessage(), e);
            return 0; // For int return methods, return 0 on system error
        } catch (Exception e) {
            log.error("[偏好管理] 重置偏好未知异常: userId={}, category={}", userId, category, e);
            return 0; // For int return methods, return 0 on unknown error
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[偏好管理] 获取偏好统计参数错误: userId={}, error={}", userId, e.getMessage());
            return statistics; // For Map return methods, return existing statistics on parameter error
        } catch (BusinessException e) {
            log.warn("[偏好管理] 获取偏好统计业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return statistics; // For Map return methods, return existing statistics on business error
        } catch (SystemException e) {
            log.error("[偏好管理] 获取偏好统计系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return statistics; // For Map return methods, return existing statistics on system error
        } catch (Exception e) {
            log.error("[偏好管理] 获取偏好统计未知异常: userId={}", userId, e);
            return statistics; // For Map return methods, return existing statistics on unknown error
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

            // ⚠️ 缓存清除应在Service层使用@CacheEvict注解处理

            log.info("[偏好管理] 同步系统默认偏好完成: userId={}, syncCount={}", userId, syncCount);
            return syncCount;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[偏好管理] 同步系统默认偏好参数错误: userId={}, error={}", userId, e.getMessage());
            return 0; // For int return methods, return 0 on parameter error
        } catch (BusinessException e) {
            log.warn("[偏好管理] 同步系统默认偏好业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return 0; // For int return methods, return 0 on business error
        } catch (SystemException e) {
            log.error("[偏好管理] 同步系统默认偏好系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return 0; // For int return methods, return 0 on system error
        } catch (Exception e) {
            log.error("[偏好管理] 同步系统默认偏好未知异常: userId={}", userId, e);
            return 0; // For int return methods, return 0 on unknown error
        }
    }

    // ==================== 私有方法 ====================
}
