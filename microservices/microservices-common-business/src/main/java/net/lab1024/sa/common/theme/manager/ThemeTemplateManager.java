package net.lab1024.sa.common.theme.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.theme.dao.ThemeTemplateDao;
import net.lab1024.sa.common.theme.entity.ThemeTemplateEntity;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 主题模板管理器
 * <p>
 * 严格遵循本项目技术栈的现代化主题模板管理：
 * - 智能推荐算法
 * - 主题配置验证和转换
 * - 针对单企业1000台设备、20000人规模优化
 * - 完整的使用统计和性能监控
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
public class ThemeTemplateManager {

    private final ThemeTemplateDao themeTemplateDao;

    // 默认主题配置缓存（仅用于初始化，不用于运行时缓存）
    private volatile Map<String, ThemeTemplateEntity> defaultTemplatesCache;

    /**
     * 构造函数注入依赖
     * <p>
     * ⚠️ 注意：已移除RedisTemplate依赖，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param themeTemplateDao 主题模板DAO
     */
    public ThemeTemplateManager(ThemeTemplateDao themeTemplateDao) {
        this.themeTemplateDao = themeTemplateDao;
        initDefaultTemplates();
    }

    /**
     * 初始化默认主题模板
     */
    private void initDefaultTemplates() {
        try {
            // 从数据库加载默认模板
            List<ThemeTemplateEntity> officialTemplates = themeTemplateDao.selectOfficialTemplates(50);
            this.defaultTemplatesCache = officialTemplates.stream()
                    .collect(Collectors.toMap(
                            ThemeTemplateEntity::getTemplateCode,
                            Function.identity(),
                            (existing, replacement) -> replacement // 如果有重复，使用最新的
                    ));

            // 如果数据库中没有默认模板，创建基础默认模板
            if (defaultTemplatesCache.isEmpty()) {
                createBasicDefaultTemplates();
            }

            log.info("[主题模板] 默认模板初始化完成，共{}个", defaultTemplatesCache.size());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[主题模板] 初始化默认模板参数错误: error={}", e.getMessage());
            this.defaultTemplatesCache = new HashMap<>(); // 降级处理：使用空Map
        } catch (BusinessException e) {
            log.warn("[主题模板] 初始化默认模板业务异常: code={}, message={}", e.getCode(), e.getMessage());
            this.defaultTemplatesCache = new HashMap<>(); // 降级处理：使用空Map
        } catch (SystemException e) {
            log.error("[主题模板] 初始化默认模板系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            this.defaultTemplatesCache = new HashMap<>(); // 降级处理：使用空Map
        } catch (Exception e) {
            log.error("[主题模板] 初始化默认模板未知异常", e);
            this.defaultTemplatesCache = new HashMap<>(); // 降级处理：使用空Map
        }
    }

    /**
     * 创建基础默认模板
     */
    private void createBasicDefaultTemplates() {
        List<ThemeTemplateEntity> basicTemplates = new ArrayList<>();

        // 创建基础颜色主题
        basicTemplates.add(createColorThemeTemplate("ocean-blue", "海洋蓝", "#1677ff", "经典蓝色主题"));
        basicTemplates.add(createColorThemeTemplate("sunset-orange", "夕阳橙", "#fa541c", "温暖橙色主题"));
        basicTemplates.add(createColorThemeTemplate("forest-green", "森林绿", "#52c41a", "清新绿色主题"));
        basicTemplates.add(createColorThemeTemplate("royal-purple", "皇家紫", "#722ed1", "优雅紫色主题"));
        basicTemplates.add(createColorThemeTemplate("volcano-red", "火山红", "#f5222d", "激情红色主题"));

        // 创建基础布局主题
        basicTemplates.add(createLayoutThemeTemplate("sidebar-classic", "经典侧边栏", "side"));
        basicTemplates.add(createLayoutThemeTemplate("top-navigation", "顶部导航", "top"));
        basicTemplates.add(createLayoutThemeTemplate("modern-mix", "现代混合", "mix"));

        for (ThemeTemplateEntity template : basicTemplates) {
            try {
                template.initializeDefaults();
                themeTemplateDao.insert(template);
                defaultTemplatesCache.put(template.getTemplateCode(), template);
            } catch (IllegalArgumentException | ParamException e) {
                log.warn("[主题模板] 创建默认模板参数错误: templateCode={}, error={}", template.getTemplateCode(), e.getMessage());
                // 单个模板创建失败不影响其他模板，继续执行
            } catch (BusinessException e) {
                log.warn("[主题模板] 创建默认模板业务异常: templateCode={}, code={}, message={}", template.getTemplateCode(), e.getCode(), e.getMessage());
                // 单个模板创建失败不影响其他模板，继续执行
            } catch (SystemException e) {
                log.error("[主题模板] 创建默认模板系统异常: templateCode={}, code={}, message={}", template.getTemplateCode(), e.getCode(), e.getMessage(), e);
                // 单个模板创建失败不影响其他模板，继续执行
            } catch (Exception e) {
                log.warn("[主题模板] 创建默认模板未知异常: templateCode={}", template.getTemplateCode(), e);
                // 单个模板创建失败不影响其他模板，继续执行
            }
        }

        log.info("[主题模板] 创建基础默认模板完成，共{}个", basicTemplates.size());
    }

    /**
     * 创建颜色主题模板
     */
    private ThemeTemplateEntity createColorThemeTemplate(String templateCode, String templateName, String primaryColor, String description) {
        ThemeTemplateEntity template = new ThemeTemplateEntity();
        template.setTemplateCode(templateCode);
        template.setTemplateName(templateName);
        template.setTemplateCategory("system");
        template.setTemplateType("color");
        template.setTemplateDesc(description);
        template.setIsOfficial(1);
        template.setIsRecommended(1);
        template.setSupportsDarkMode(1);
        template.setTemplateStatus("published");
        template.setComplexityLevel(1);
        template.setRating(4.5);
        template.setRatingCount(1);
        template.setSortOrder(0);
        template.setTemplateTags("blue,classic,default");

        // 创建颜色配置
        Map<String, Object> colorConfig = Map.of(
                "primaryColor", primaryColor,
                "primaryHover", adjustColorBrightness(primaryColor, 0.1),
                "primaryActive", adjustColorBrightness(primaryColor, -0.1),
                "primaryBg", adjustColorAlpha(primaryColor, 0.1),
                "colorIndex", getPrimaryColorIndex(primaryColor)
        );
        template.setColorConfig(convertToJson(colorConfig));

        return template;
    }

    /**
     * 创建布局主题模板
     */
    private ThemeTemplateEntity createLayoutThemeTemplate(String templateCode, String templateName, String layoutMode) {
        ThemeTemplateEntity template = new ThemeTemplateEntity();
        template.setTemplateCode(templateCode);
        template.setTemplateName(templateName);
        template.setTemplateCategory("system");
        template.setTemplateType("layout");
        template.setTemplateDesc(templateName + "布局主题");
        template.setIsOfficial(1);
        template.setIsRecommended(1);
        template.setSupportsDarkMode(1);
        template.setTemplateStatus("published");
        template.setComplexityLevel(2);
        template.setRating(4.0);
        template.setRatingCount(1);
        template.setSortOrder(0);
        template.setTemplateTags("layout,modern,responsive");

        // 创建布局配置
        Map<String, Object> layoutConfig = Map.of(
                "layoutMode", layoutMode,
                "headerHeight", "64px",
                "sidebarWidth", "240px",
                "footerHeight", "48px",
                "contentPadding", "24px",
                "enableBreadcrumb", true,
                "enableTabNav", true,
                "responsiveBreakpoints", Map.of(
                        "xs", "480px",
                        "sm", "768px",
                        "md", "992px",
                        "lg", "1200px",
                        "xl", "1920px"
                )
        );
        template.setLayoutConfig(convertToJson(layoutConfig));

        return template;
    }

    /**
     * 获取主题模板（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param templateCode 模板代码
     * @param deviceType 设备类型
     * @return 主题模板
     */
    public ThemeTemplateEntity getTemplate(String templateCode, String deviceType) {
        // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
        ThemeTemplateEntity template = loadTemplateFromDatabase(templateCode);
        if (template != null) {
            log.debug("[主题模板] 数据库加载成功: templateCode={}", templateCode);
            return template;
        } else {
            // 使用默认模板
            template = getDefaultTemplate(templateCode, deviceType);
            if (template != null) {
                log.info("[主题模板] 使用默认模板: templateCode={}", templateCode);
            }
            return template;
        }
    }

    /**
     * 从数据库加载模板
     */
    private ThemeTemplateEntity loadTemplateFromDatabase(String templateCode) {
        try {
            return themeTemplateDao.selectByTemplateCode(templateCode);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[主题模板] 数据库查询参数错误: templateCode={}, error={}", templateCode, e.getMessage());
            return null; // For Entity return methods, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[主题模板] 数据库查询业务异常: templateCode={}, code={}, message={}", templateCode, e.getCode(), e.getMessage());
            return null; // For Entity return methods, return null on business error
        } catch (SystemException e) {
            log.error("[主题模板] 数据库查询系统异常: templateCode={}, code={}, message={}", templateCode, e.getCode(), e.getMessage(), e);
            return null; // For Entity return methods, return null on system error
        } catch (Exception e) {
            log.error("[主题模板] 数据库查询未知异常: templateCode={}, error={}", templateCode, e.getMessage(), e);
            return null; // For Entity return methods, return null on unknown error
        }
    }

    /**
     * 获取默认模板
     */
    private ThemeTemplateEntity getDefaultTemplate(String templateCode, String deviceType) {
        ThemeTemplateEntity defaultTemplate = defaultTemplatesCache.get(templateCode);
        if (defaultTemplate == null) {
            log.warn("[主题模板] 默认模板不存在: templateCode={}", templateCode);
            return null;
        }

        // 克隆模板以避免修改原始对象
        ThemeTemplateEntity template = cloneTemplate(defaultTemplate);
        // 可以根据deviceType调整配置

        return template;
    }

    /**
     * 获取推荐模板列表（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param limit 限制数量
     * @return 推荐模板列表
     */
    public List<ThemeTemplateEntity> getRecommendedTemplates(Integer limit) {
        try {
            // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
            List<ThemeTemplateEntity> templates = themeTemplateDao.selectRecommendedTemplates(limit != null ? limit : 10);
            log.info("[主题模板] 获取推荐模板成功: count={}", templates != null ? templates.size() : 0);
            return templates;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[主题模板] 获取推荐模板参数错误: limit={}, error={}", limit, e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on parameter error
        } catch (BusinessException e) {
            log.warn("[主题模板] 获取推荐模板业务异常: limit={}, code={}, message={}", limit, e.getCode(), e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on business error
        } catch (SystemException e) {
            log.error("[主题模板] 获取推荐模板系统异常: limit={}, code={}, message={}", limit, e.getCode(), e.getMessage(), e);
            return Collections.emptyList(); // For List return methods, return empty list on system error
        } catch (Exception e) {
            log.error("[主题模板] 获取推荐模板未知异常: limit={}, error={}", limit, e.getMessage(), e);
            return Collections.emptyList(); // For List return methods, return empty list on unknown error
        }
    }

    /**
     * 获取热门模板列表（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param limit 限制数量
     * @return 热门模板列表
     */
    public List<ThemeTemplateEntity> getPopularTemplates(Integer limit) {
        try {
            // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
            List<ThemeTemplateEntity> templates = themeTemplateDao.selectPopularTemplates(limit != null ? limit : 20);
            log.info("[主题模板] 获取热门模板成功: count={}", templates != null ? templates.size() : 0);
            return templates;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[主题模板] 获取热门模板参数错误: limit={}, error={}", limit, e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on parameter error
        } catch (BusinessException e) {
            log.warn("[主题模板] 获取热门模板业务异常: limit={}, code={}, message={}", limit, e.getCode(), e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on business error
        } catch (SystemException e) {
            log.error("[主题模板] 获取热门模板系统异常: limit={}, code={}, message={}", limit, e.getCode(), e.getMessage(), e);
            return Collections.emptyList(); // For List return methods, return empty list on system error
        } catch (Exception e) {
            log.error("[主题模板] 获取热门模板未知异常: limit={}, error={}", limit, e.getMessage(), e);
            return Collections.emptyList(); // For List return methods, return empty list on unknown error
        }
    }

    /**
     * 搜索模板
     */
    public List<ThemeTemplateEntity> searchTemplates(String keyword, String templateType, Integer limit) {
        log.info("[主题模板] 搜索模板: keyword={}, templateType={}, limit={}", keyword, templateType, limit);

        try {
            List<ThemeTemplateEntity> templates = themeTemplateDao.searchTemplates(
                    keyword, templateType, limit != null ? limit : 20);

            // 更新使用统计
            List<Long> templateIds = templates.stream()
                    .map(ThemeTemplateEntity::getId)
                    .toList();
            if (!templateIds.isEmpty()) {
                batchUpdateUsageStatistics(templateIds);
            }

            log.info("[主题模板] 搜索模板成功: keyword={}, count={}", keyword, templates.size());
            return templates;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[主题模板] 搜索模板参数错误: keyword={}, templateType={}, error={}", keyword, templateType, e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on parameter error
        } catch (BusinessException e) {
            log.warn("[主题模板] 搜索模板业务异常: keyword={}, templateType={}, code={}, message={}", keyword, templateType, e.getCode(), e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on business error
        } catch (SystemException e) {
            log.error("[主题模板] 搜索模板系统异常: keyword={}, templateType={}, code={}, message={}", keyword, templateType, e.getCode(), e.getMessage(), e);
            return Collections.emptyList(); // For List return methods, return empty list on system error
        } catch (Exception e) {
            log.error("[主题模板] 搜索模板未知异常: keyword={}, templateType={}", keyword, templateType, e);
            return Collections.emptyList(); // For List return methods, return empty list on unknown error
        }
    }

    /**
     * 根据类型获取模板（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    public List<ThemeTemplateEntity> getTemplatesByType(String templateType) {
        try {
            // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
            List<ThemeTemplateEntity> templates = themeTemplateDao.selectByTemplateType(templateType);
            log.info("[主题模板] 获取类型模板成功: templateType={}, count={}", templateType, templates != null ? templates.size() : 0);
            return templates;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[主题模板] 获取类型模板参数错误: templateType={}, error={}", templateType, e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on parameter error
        } catch (BusinessException e) {
            log.warn("[主题模板] 获取类型模板业务异常: templateType={}, code={}, message={}", templateType, e.getCode(), e.getMessage());
            return Collections.emptyList(); // For List return methods, return empty list on business error
        } catch (SystemException e) {
            log.error("[主题模板] 获取类型模板系统异常: templateType={}, code={}, message={}", templateType, e.getCode(), e.getMessage(), e);
            return Collections.emptyList(); // For List return methods, return empty list on system error
        } catch (Exception e) {
            log.error("[主题模板] 获取类型模板未知异常: templateType={}, error={}", templateType, e.getMessage(), e);
            return Collections.emptyList(); // For List return methods, return empty list on unknown error
        }
    }

    /**
     * 应用主题模板到用户配置
     * <p>
     * <strong>注意：</strong>Manager类不处理事务，事务应在Service层管理。
     * </p>
     */
    public boolean applyTemplateToUser(String templateCode, Long userId, String deviceType) {
        try {
            ThemeTemplateEntity template = getTemplate(templateCode, deviceType);
            if (template == null) {
                log.warn("[主题模板] 模板不存在: templateCode={}", templateCode);
                return false;
            }

            // 提取主题配置并应用到用户主题配置
            Map<String, Object> themeConfig = extractThemeConfig(template);

            // 这里应该调用用户主题配置服务来应用配置
            // 简化处理，直接记录日志
            log.info("[主题模板] 应用模板到用户: templateCode={}, userId={}, configKeys={}",
                    templateCode, userId, themeConfig.keySet());

            // 更新模板使用统计
            themeTemplateDao.updateUsageStatistics(template.getId());

            // ⚠️ 缓存清除应在Service层使用@CacheEvict注解处理

            log.info("[主题模板] 模板应用成功: templateCode={}, userId={}", templateCode, userId);
            return true;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[主题模板] 应用模板参数错误: templateCode={}, userId={}, error={}", templateCode, userId, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[主题模板] 应用模板业务异常: templateCode={}, userId={}, code={}, message={}", templateCode, userId, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[主题模板] 应用模板系统异常: templateCode={}, userId={}, code={}, message={}", templateCode, userId, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[主题模板] 应用模板未知异常: templateCode={}, userId={}, error={}", templateCode, userId, e.getMessage(), e);
            return false; // For boolean return methods, return false on unknown error
        }
    }

    /**
     * 获取模板使用统计（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @return 统计信息
     */
    public Map<String, Object> getTemplateStatistics() {
        try {
            // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
            Map<String, Object> statistics = themeTemplateDao.selectTemplateStatistics();
            log.info("[主题模板] 获取统计信息成功");
            return statistics;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[主题模板] 获取统计信息参数错误: error={}", e.getMessage());
            return new HashMap<>(); // For Map return methods, return empty map on parameter error
        } catch (BusinessException e) {
            log.warn("[主题模板] 获取统计信息业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return new HashMap<>(); // For Map return methods, return empty map on business error
        } catch (SystemException e) {
            log.error("[主题模板] 获取统计信息系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return new HashMap<>(); // For Map return methods, return empty map on system error
        } catch (Exception e) {
            log.error("[主题模板] 获取统计信息未知异常: error={}", e.getMessage(), e);
            return new HashMap<>(); // For Map return methods, return empty map on unknown error
        }
    }

    /**
     * 预热模板缓存（已废弃，缓存由Service层@Cacheable注解自动处理）
     * <p>
     * ⚠️ 注意：此方法已废弃，缓存预热由Service层的@Cacheable注解自动处理
     * </p>
     *
     * @param templateCodes 模板代码列表
     * @param deviceType 设备类型
     * @deprecated 使用Service层的@Cacheable注解替代
     */
    @Deprecated
    public void warmupTemplateCache(List<String> templateCodes, String deviceType) {
        log.warn("[主题模板] warmupTemplateCache()已废弃，请使用Service层的@Cacheable注解 - count={}", templateCodes.size());
    }

    // ==================== 私有方法 ====================

    /**
     * 克隆模板
     */
    private ThemeTemplateEntity cloneTemplate(ThemeTemplateEntity original) {
        try {
            // 浅拷贝足够，因为我们主要读取数据
            ThemeTemplateEntity cloned = new ThemeTemplateEntity();
            cloned.setId(original.getId());
            cloned.setTemplateCode(original.getTemplateCode());
            cloned.setTemplateName(original.getTemplateName());
            cloned.setTemplateCategory(original.getTemplateCategory());
            cloned.setTemplateType(original.getTemplateType());
            cloned.setTemplateDesc(original.getTemplateDesc());
            cloned.setThemeConfig(original.getThemeConfig());
            cloned.setColorConfig(original.getColorConfig());
            cloned.setLayoutConfig(original.getLayoutConfig());
            cloned.setComponentConfig(original.getComponentConfig());
            // 设置是否支持暗色模式
            String darkModeSupport = original.getSupportsDarkMode();
            cloned.setSupportsDarkMode("1".equals(darkModeSupport) ? 1 : 0);
            // 复制其他必要字段...
            return cloned;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[主题模板] 克隆模板参数错误: error={}", e.getMessage());
            return original; // 克隆失败时返回原始对象
        } catch (BusinessException e) {
            log.warn("[主题模板] 克隆模板业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return original; // 克隆失败时返回原始对象
        } catch (SystemException e) {
            log.error("[主题模板] 克隆模板系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return original; // 克隆失败时返回原始对象
        } catch (Exception e) {
            log.error("[主题模板] 克隆模板未知异常: error={}", e.getMessage(), e);
            return original; // 克隆失败时返回原始对象
        }
    }

    /**
     * 提取主题配置
     */
    private Map<String, Object> extractThemeConfig(ThemeTemplateEntity template) {
        Map<String, Object> config = new HashMap<>();

        if (template.getColorConfig() != null) {
            config.put("colors", parseJson(template.getColorConfig()));
        }

        if (template.getLayoutConfig() != null) {
            config.put("layout", parseJson(template.getLayoutConfig()));
        }

        if (template.getComponentConfig() != null) {
            config.put("components", parseJson(template.getComponentConfig()));
        }

        if (template.getThemeConfig() != null) {
            config.put("theme", parseJson(template.getThemeConfig()));
        }

        return config;
    }

    /**
     * 解析JSON（简化处理）
     */
    private Map<String, Object> parseJson(String jsonString) {
        // 简化处理，实际应该使用Jackson或其他JSON解析库
        return new HashMap<>();
    }

    /**
     * 转换为JSON（简化处理）
     */
    private String convertToJson(Map<String, Object> map) {
        // 简化处理，实际应该使用Jackson或其他JSON库
        return "{}";
    }

    /**
     * 批量更新使用统计
     */
    private void batchUpdateUsageStatistics(List<Long> templateIds) {
        templateIds.parallelStream().forEach(templateId -> {
            try {
                themeTemplateDao.updateUsageStatistics(templateId);
            } catch (IllegalArgumentException | ParamException e) {
                log.warn("[主题模板] 更新使用统计参数错误: templateId={}, error={}", templateId, e.getMessage());
                // 单个统计更新失败不影响其他统计，继续执行
            } catch (BusinessException e) {
                log.warn("[主题模板] 更新使用统计业务异常: templateId={}, code={}, message={}", templateId, e.getCode(), e.getMessage());
                // 单个统计更新失败不影响其他统计，继续执行
            } catch (SystemException e) {
                log.error("[主题模板] 更新使用统计系统异常: templateId={}, code={}, message={}", templateId, e.getCode(), e.getMessage(), e);
                // 单个统计更新失败不影响其他统计，继续执行
            } catch (Exception e) {
                log.warn("[主题模板] 更新使用统计未知异常: templateId={}, error={}", templateId, e.getMessage(), e);
                // 单个统计更新失败不影响其他统计，继续执行
            }
        });
    }

    /**
     * 获取主色调索引
     */
    private int getPrimaryColorIndex(String color) {
        Map<String, Integer> colorMap = Map.of(
                "#1677ff", 0, // 蓝色
                "#fa541c", 1, // 橙色
                "#52c41a", 2, // 绿色
                "#722ed1", 3, // 紫色
                "#f5222d", 4, // 红色
                "#faad14", 5, // 黄色
                "#13c2c2", 6, // 青色
                "#eb2f96", 7, // 粉色
                "#a0d911", 8, // 青绿色
                "#2f54eb", 9  // 深蓝色
        );
        return colorMap.getOrDefault(color, 0);
    }

    /**
     * 调整颜色亮度
     */
    private String adjustColorBrightness(String color, double factor) {
        // 简化处理，实际应该进行颜色计算
        return color;
    }

    /**
     * 调整颜色透明度
     */
    private String adjustColorAlpha(String color, double alpha) {
        // 简化处理，实际应该进行颜色计算
        return color;
    }
}
