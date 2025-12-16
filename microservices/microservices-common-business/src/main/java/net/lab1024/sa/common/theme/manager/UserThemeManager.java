package net.lab1024.sa.common.theme.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.theme.dao.UserThemeConfigDao;
import net.lab1024.sa.common.theme.entity.UserThemeConfigEntity;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;

import java.util.List;

/**
 * 用户主题配置管理器
 * <p>
 * 提供用户主题配置的完整管理功能，包括：
 * - 主题配置的增删改查
 * - 默认主题处理
 * - 主题配置同步
 * </p>
 * <p>
 * <strong>重要说明：</strong>Manager类为纯Java类，不使用Spring注解。
 * 通过构造函数注入依赖，在微服务中通过@Configuration类注册为Spring Bean。
 * </p>
 * <p>
 * ⚠️ <strong>缓存说明：</strong>缓存逻辑已移除，缓存应在Service层使用@Cacheable注解处理。
 * 如果未来需要使用时，请创建ThemeService并在Service层使用@Cacheable注解。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 * @updated 2025-01-30 移除缓存逻辑，符合CLAUDE.md规范，缓存应在Service层使用@Cacheable注解
 */
@Slf4j
public class UserThemeManager {

    private final UserThemeConfigDao userThemeConfigDao;

    // 系统默认主题配置
    private UserThemeConfigEntity systemDefaultTheme;

    /**
     * 构造函数注入依赖
     * <p>
     * ⚠️ 注意：已移除RedisTemplate依赖，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param userThemeConfigDao 用户主题配置DAO
     */
    public UserThemeManager(UserThemeConfigDao userThemeConfigDao) {
        this.userThemeConfigDao = userThemeConfigDao;
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
     * 获取用户主题配置（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return 主题配置
     */
    public UserThemeConfigEntity getUserTheme(Long userId, String deviceType) {
        // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
        UserThemeConfigEntity cached = loadUserThemeFromDatabase(userId, deviceType);
        if (cached != null) {
            log.debug("[主题管理] 数据库加载成功: userId={}, deviceType={}", userId, deviceType);
            return cached;
        } else {
            // 使用系统默认主题
            cached = getSystemDefaultTheme(deviceType);
            log.info("[主题管理] 使用系统默认主题: userId={}, deviceType={}", userId, deviceType);
            return cached;
        }
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

            // ⚠️ 缓存清除应在Service层使用@CacheEvict注解处理

            return themeConfig;

        } catch (Exception e) {
            log.error("[主题管理] 保存主题配置失败: userId={}, error={}", themeConfig.getUserId(), e.getMessage(), e);
            throw new SystemException("THEME_SAVE_ERROR", "保存主题配置失败", e);
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
                // ⚠️ 缓存清除应在Service层使用@CacheEvict注解处理

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
            throw new SystemException("THEME_DELETE_ERROR", "删除主题配置失败", e);
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
     * 获取用户所有主题配置（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return 主题配置列表
     */
    public List<UserThemeConfigEntity> getUserThemes(Long userId, String deviceType) {
        // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
        return userThemeConfigDao.selectByUserIdAndDeviceType(userId, deviceType);
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
                log.warn("[主题管理] 源主题配置不存在, sourceConfigId={}", sourceConfigId);
                throw new BusinessException("SOURCE_THEME_NOT_FOUND", "源主题配置不存在");
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
            throw new SystemException("THEME_COPY_ERROR", "复制主题配置失败", e);
        }
    }
}
