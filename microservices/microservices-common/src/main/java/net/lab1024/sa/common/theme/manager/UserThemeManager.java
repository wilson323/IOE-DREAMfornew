package net.lab1024.sa.common.theme.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.theme.dao.UserThemeConfigDao;
import net.lab1024.sa.common.theme.entity.UserThemeConfigEntity;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户主题配置管理器
 * <p>
 * 提供用户主题配置的完整管理功能，包括：
 * - 主题配置的增删改查
 * - 多级缓存管理
 * - 默认主题处理
 * - 主题配置同步
 * </p>
 * <p>
 * <strong>重要说明：</strong>Manager类为纯Java类，不使用Spring注解。
 * 通过构造函数注入依赖，在微服务中通过@Configuration类注册为Spring Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Slf4j
public class UserThemeManager {

    private final UserThemeConfigDao userThemeConfigDao;
    private final RedisTemplate<String, Object> redisTemplate;

    // 本地缓存
    private final Map<String, UserThemeConfigEntity> localCache = new ConcurrentHashMap<>();
    private static final Duration CACHE_DURATION = Duration.ofMinutes(30);

    // 缓存键前缀
    private static final String CACHE_PREFIX = "user:theme:";
    private static final String USER_CACHE_PREFIX = "user:theme:list:";

    // 系统默认主题配置
    private UserThemeConfigEntity systemDefaultTheme;

    public UserThemeManager(UserThemeConfigDao userThemeConfigDao, RedisTemplate<String, Object> redisTemplate) {
        this.userThemeConfigDao = userThemeConfigDao;
        this.redisTemplate = redisTemplate;
        initSystemDefaultTheme();
    }

    /**
     * 初始化系统默认主题配置
     */
    private void initSystemDefaultTheme() {
        try {
            this.systemDefaultTheme = userThemeConfigDao.selectSystemDefaultTheme();
            if (this.systemDefaultTheme == null) {
                // 创建默认主题配置
                this.systemDefaultTheme = createDefaultSystemTheme();
                log.info("[主题管理] 已创建系统默认主题配置");
            }
        } catch (Exception e) {
            log.error("[主题管理] 初始化系统默认主题失败", e);
            this.systemDefaultTheme = createDefaultSystemTheme();
        }
    }

    /**
     * 创建默认系统主题配置
     */
    private UserThemeConfigEntity createDefaultSystemTheme() {
        UserThemeConfigEntity defaultTheme = new UserThemeConfigEntity();
        defaultTheme.setId(0L); // 系统配置使用0作为ID
        defaultTheme.setUserId(null); // 系统配置无用户ID
        defaultTheme.setColorIndex(0); // 蓝色主题
        defaultTheme.setLayoutMode("side"); // 侧边栏布局
        defaultTheme.setSideMenuTheme("dark"); // 暗色侧边栏
        defaultTheme.setDarkModeFlag(0); // 关闭暗黑模式
        defaultTheme.setSideMenuWidth(200); // 默认宽度
        defaultTheme.setPageAnimateFlag(1); // 开启动效
        defaultTheme.setThemeName("系统默认");
        defaultTheme.setIsDefault(1);
        defaultTheme.setStatus(1);
        defaultTheme.setSortOrder(0);
        defaultTheme.setDeviceType("web"); // 默认Web端
        defaultTheme.setGroupFlag("system");
        return defaultTheme;
    }

    /**
     * 获取用户主题配置（多级缓存）
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return 主题配置
     */
    public UserThemeConfigEntity getUserTheme(Long userId, String deviceType) {
        String cacheKey = CACHE_PREFIX + userId + ":" + deviceType;

        // L1本地缓存
        UserThemeConfigEntity cached = localCache.get(cacheKey);
        if (cached != null) {
            log.debug("[主题管理] L1缓存命中: userId={}, deviceType={}", userId, deviceType);
            return cached;
        }

        // L2 Redis缓存
        try {
            cached = (UserThemeConfigEntity) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                localCache.put(cacheKey, cached);
                log.debug("[主题管理] L2缓存命中: userId={}, deviceType={}", userId, deviceType);
                return cached;
            }
        } catch (Exception e) {
            log.warn("[主题管理] Redis缓存查询失败: userId={}, deviceType={}", userId, deviceType, e);
        }

        // L3数据库查询
        cached = loadUserThemeFromDatabase(userId, deviceType);
        if (cached != null) {
            // 更新缓存
            updateCache(cacheKey, cached);
            log.info("[主题管理] 数据库加载成功: userId={}, deviceType={}", userId, deviceType);
        } else {
            // 使用系统默认主题
            cached = getSystemDefaultTheme(deviceType);
            log.info("[主题管理] 使用系统默认主题: userId={}, deviceType={}", userId, deviceType);
        }

        return cached;
    }

    /**
     * 从数据库加载用户主题配置
     */
    private UserThemeConfigEntity loadUserThemeFromDatabase(Long userId, String deviceType) {
        // 查询用户默认主题
        UserThemeConfigEntity userTheme = userThemeConfigDao.selectDefaultByUserIdAndDeviceType(userId, deviceType);

        if (userTheme == null) {
            // 如果没有默认主题，查询第一个可用主题
            List<UserThemeConfigEntity> themes = userThemeConfigDao.selectByUserIdAndDeviceType(userId, deviceType);
            if (!themes.isEmpty()) {
                userTheme = themes.get(0);
                // 设为默认主题
                userTheme.setIsDefault(1);
                userThemeConfigDao.updateDefaultTheme(userId, userTheme.getId(), deviceType);
            }
        }

        return userTheme;
    }

    /**
     * 获取系统默认主题配置
     *
     * @param deviceType 设备类型
     * @return 默认主题配置
     */
    public UserThemeConfigEntity getSystemDefaultTheme(String deviceType) {
        UserThemeConfigEntity defaultTheme = new UserThemeConfigEntity();
        // 从系统默认主题复制配置
        if (systemDefaultTheme != null) {
            defaultTheme.setColorIndex(systemDefaultTheme.getColorIndex());
            defaultTheme.setThemeColor(systemDefaultTheme.getThemeColor());
            defaultTheme.setLayoutMode(systemDefaultTheme.getLayoutMode());
            defaultTheme.setSideMenuTheme(systemDefaultTheme.getSideMenuTheme());
            defaultTheme.setDarkModeFlag(systemDefaultTheme.getDarkModeFlag());
            defaultTheme.setSideMenuWidth(systemDefaultTheme.getSideMenuWidth());
            defaultTheme.setPageAnimateFlag(systemDefaultTheme.getPageAnimateFlag());
            defaultTheme.setThemeName(systemDefaultTheme.getThemeName());
            defaultTheme.setExtendedConfig(systemDefaultTheme.getExtendedConfig());
        }
        defaultTheme.setDeviceType(deviceType);
        defaultTheme.setUserId(null); // 系统主题
        return defaultTheme;
    }

    /**
     * 保存或更新用户主题配置
     *
     * @param themeConfig 主题配置
     * @return 保存后的主题配置
     */
    public UserThemeConfigEntity saveUserTheme(UserThemeConfigEntity themeConfig) {
        try {
            if (themeConfig.getId() == null || themeConfig.getId() == 0) {
                // 新增主题配置
                themeConfig.setId(null); // 让数据库生成ID
                userThemeConfigDao.insert(themeConfig);
                log.info("[主题管理] 新增主题配置: userId={}, configId={}", themeConfig.getUserId(), themeConfig.getId());
            } else {
                // 更新主题配置
                userThemeConfigDao.updateById(themeConfig);
                log.info("[主题管理] 更新主题配置: userId={}, configId={}", themeConfig.getUserId(), themeConfig.getId());
            }

            // 如果设置为默认主题，更新其他主题的默认状态
            if (themeConfig.getIsDefault() != null && themeConfig.getIsDefault() == 1) {
                userThemeConfigDao.updateDefaultTheme(themeConfig.getUserId(), themeConfig.getId(), themeConfig.getDeviceType());
            }

            // 清除相关缓存
            clearUserThemeCache(themeConfig.getUserId(), themeConfig.getDeviceType());

            return themeConfig;

        } catch (Exception e) {
            log.error("[主题管理] 保存主题配置失败: userId={}, error={}", themeConfig.getUserId(), e.getMessage(), e);
            throw new RuntimeException("保存主题配置失败", e);
        }
    }

    /**
     * 删除用户主题配置
     *
     * @param configId 配置ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    public boolean deleteUserTheme(Long configId, Long userId) {
        try {
            // 查询主题配置
            UserThemeConfigEntity theme = userThemeConfigDao.selectByConfigId(configId);
            if (theme == null || !theme.getUserId().equals(userId)) {
                log.warn("[主题管理] 主题配置不存在或无权限: configId={}, userId={}", configId, userId);
                return false;
            }

            // 检查是否为默认主题
            boolean isDefault = theme.getIsDefault() != null && theme.getIsDefault() == 1;

            // 删除主题配置
            int result = userThemeConfigDao.deleteByConfigId(configId, userId);
            if (result > 0) {
                // 清除缓存
                clearUserThemeCache(userId, theme.getDeviceType());

                // 如果删除的是默认主题，需要设置新的默认主题
                if (isDefault) {
                    setNewDefaultTheme(userId, theme.getDeviceType());
                }

                log.info("[主题管理] 删除主题配置成功: configId={}, userId={}", configId, userId);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("[主题管理] 删除主题配置失败: configId={}, userId={}, error={}", configId, userId, e.getMessage(), e);
            throw new RuntimeException("删除主题配置失败", e);
        }
    }

    /**
     * 设置新的默认主题
     */
    private void setNewDefaultTheme(Long userId, String deviceType) {
        List<UserThemeConfigEntity> remainingThemes = userThemeConfigDao.selectByUserIdAndDeviceType(userId, deviceType);
        if (!remainingThemes.isEmpty()) {
            UserThemeConfigEntity newDefault = remainingThemes.get(0);
            newDefault.setIsDefault(1);
            userThemeConfigDao.updateById(newDefault);
            log.info("[主题管理] 设置新默认主题: userId={}, configId={}", userId, newDefault.getId());
        }
    }

    /**
     * 获取用户所有主题配置
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return 主题配置列表
     */
    public List<UserThemeConfigEntity> getUserThemes(Long userId, String deviceType) {
        String cacheKey = USER_CACHE_PREFIX + userId + ":" + deviceType;

        // 尝试从缓存获取
        try {
            @SuppressWarnings("unchecked")
            List<UserThemeConfigEntity> cached = (List<UserThemeConfigEntity>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("[主题管理] 主题列表缓存命中: userId={}, deviceType={}", userId, deviceType);
                return cached;
            }
        } catch (Exception e) {
            log.warn("[主题管理] 主题列表缓存查询失败: userId={}, deviceType={}", userId, deviceType, e);
        }

        // 从数据库查询
        List<UserThemeConfigEntity> themes = userThemeConfigDao.selectByUserIdAndDeviceType(userId, deviceType);

        // 更新缓存
        try {
            if (cacheKey != null && themes != null) {
                Duration duration = Objects.requireNonNull(CACHE_DURATION, "CACHE_DURATION不能为null");
                redisTemplate.opsForValue().set(cacheKey, themes, duration);
            }
        } catch (Exception e) {
            log.warn("[主题管理] 主题列表缓存更新失败: userId={}, deviceType={}", userId, deviceType, e);
        }

        return themes;
    }

    /**
     * 更新缓存
     */
    private void updateCache(String cacheKey, UserThemeConfigEntity themeConfig) {
        // 更新本地缓存
        localCache.put(cacheKey, themeConfig);

        // 更新Redis缓存
        try {
            if (cacheKey != null && themeConfig != null) {
                Duration duration = Objects.requireNonNull(CACHE_DURATION, "CACHE_DURATION不能为null");
                redisTemplate.opsForValue().set(cacheKey, themeConfig, duration);
            }
        } catch (Exception e) {
            log.warn("[主题管理] Redis缓存更新失败: cacheKey={}", cacheKey, e);
        }
    }

    /**
     * 清除用户主题缓存
     */
    private void clearUserThemeCache(Long userId, String deviceType) {
        // 清除本地缓存
        String cacheKey = CACHE_PREFIX + userId + ":" + deviceType;
        localCache.remove(cacheKey);

        // 清除Redis缓存
        try {
            redisTemplate.delete(cacheKey);
            redisTemplate.delete(USER_CACHE_PREFIX + userId + ":" + deviceType);
        } catch (Exception e) {
            log.warn("[主题管理] 清除缓存失败: userId={}, deviceType={}", userId, deviceType, e);
        }
    }

    /**
     * 预热主题缓存
     *
     * @param userId 用户ID
     */
    public void warmupThemeCache(Long userId) {
        log.info("[主题管理] 开始预热主题缓存: userId={}", userId);

        try {
            // 预热Web端主题
            getUserTheme(userId, "web");
            // 预热移动端主题
            getUserTheme(userId, "mobile");

            log.info("[主题管理] 主题缓存预热完成: userId={}", userId);
        } catch (Exception e) {
            log.error("[主题管理] 主题缓存预热失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 获取主题使用统计
     *
     * @param limit 限制数量
     * @return 热门主题列表
     */
    public List<UserThemeConfigEntity> getPopularThemes(Integer limit) {
        try {
            return userThemeConfigDao.selectPopularThemes(limit != null ? limit : 10);
        } catch (Exception e) {
            log.error("[主题管理] 查询热门主题失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 复制主题配置
     *
     * @param sourceConfigId 源配置ID
     * @param targetUserId 目标用户ID
     * @param themeName 新主题名称
     * @return 复制后的主题配置
     */
    public UserThemeConfigEntity copyThemeConfig(Long sourceConfigId, Long targetUserId, String themeName) {
        try {
            UserThemeConfigEntity sourceTheme = userThemeConfigDao.selectByConfigId(sourceConfigId);
            if (sourceTheme == null) {
                throw new RuntimeException("源主题配置不存在");
            }

            UserThemeConfigEntity newTheme = new UserThemeConfigEntity();
            // 复制所有配置项
            newTheme.setUserId(targetUserId);
            newTheme.setColorIndex(sourceTheme.getColorIndex());
            newTheme.setThemeColor(sourceTheme.getThemeColor());
            newTheme.setLayoutMode(sourceTheme.getLayoutMode());
            newTheme.setSideMenuTheme(sourceTheme.getSideMenuTheme());
            newTheme.setDarkModeFlag(sourceTheme.getDarkModeFlag());
            newTheme.setSideMenuWidth(sourceTheme.getSideMenuWidth());
            newTheme.setPageAnimateFlag(sourceTheme.getPageAnimateFlag());
            newTheme.setThemeName(themeName);
            newTheme.setIsDefault(0); // 复制的主题默认不是默认主题
            newTheme.setStatus(1);
            newTheme.setSortOrder(0);
            newTheme.setExtendedConfig(sourceTheme.getExtendedConfig());
            newTheme.setDeviceType(sourceTheme.getDeviceType());

            return saveUserTheme(newTheme);

        } catch (Exception e) {
            log.error("[主题管理] 复制主题配置失败: sourceConfigId={}, targetUserId={}, error={}",
                    sourceConfigId, targetUserId, e.getMessage(), e);
            throw new RuntimeException("复制主题配置失败", e);
        }
    }
}
