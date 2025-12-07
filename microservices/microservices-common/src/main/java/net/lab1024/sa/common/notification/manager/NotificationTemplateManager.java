package net.lab1024.sa.common.notification.manager;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.cache.UnifiedCacheManager;
import net.lab1024.sa.common.notification.dao.NotificationTemplateDao;
import net.lab1024.sa.common.notification.domain.entity.NotificationTemplateEntity;

/**
 * 通知模板管理器
 * <p>
 * 统一管理通知模板的获取、渲染、缓存等操作
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 企业级特性：
 * - 模板缓存（多级缓存）
 * - 变量替换（支持{{variable}}格式）
 * - 模板验证（格式校验）
 * - 模板热更新（清除缓存）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class NotificationTemplateManager {

    private final NotificationTemplateDao notificationTemplateDao;
    private final UnifiedCacheManager cacheManager;
    private final ObjectMapper objectMapper;

    /**
     * 缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "notification:template:";

    /**
     * 缓存过期时间：30分钟
     */
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * 变量替换正则表达式：{{variable}}
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * 构造函数
     *
     * @param notificationTemplateDao 模板DAO
     * @param cacheManager           统一缓存管理器
     * @param objectMapper           JSON对象映射器
     */
    public NotificationTemplateManager(NotificationTemplateDao notificationTemplateDao,
                                       UnifiedCacheManager cacheManager,
                                       ObjectMapper objectMapper) {
        this.notificationTemplateDao = notificationTemplateDao;
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据模板编码获取模板
     * <p>
     * 支持多级缓存
     * </p>
     *
     * @param templateCode 模板编码
     * @return 通知模板实体
     */
    public NotificationTemplateEntity getTemplateByCode(String templateCode) {
        if (templateCode == null || templateCode.isEmpty()) {
            return null;
        }

        String cacheKey = CACHE_KEY_PREFIX + templateCode;
        return cacheManager.getWithRefresh(
                cacheKey,
                () -> notificationTemplateDao.selectByTemplateCode(templateCode),
                CACHE_TTL
        );
    }

    /**
     * 根据模板类型获取模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    public List<NotificationTemplateEntity> getTemplatesByType(Integer templateType) {
        if (templateType == null) {
            return List.of();
        }

        return notificationTemplateDao.selectByTemplateType(templateType);
    }

    /**
     * 根据模板类型和状态获取模板列表
     *
     * @param templateType 模板类型
     * @param status       状态（1-启用 2-禁用）
     * @return 模板列表
     */
    public List<NotificationTemplateEntity> getTemplatesByTypeAndStatus(Integer templateType, Integer status) {
        if (templateType == null || status == null) {
            return List.of();
        }

        return notificationTemplateDao.selectByTemplateTypeAndStatus(templateType, status);
    }

    /**
     * 渲染模板
     * <p>
     * 使用变量替换模板中的占位符
     * 支持{{variable}}格式的变量
     * </p>
     *
     * @param templateCode 模板编码
     * @param variables     变量Map（key为变量名，value为变量值）
     * @return 渲染后的内容
     */
    public String renderTemplate(String templateCode, Map<String, Object> variables) {
        NotificationTemplateEntity template = getTemplateByCode(templateCode);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + templateCode);
        }

        if (template.getStatus() == null || template.getStatus() != 1) {
            throw new RuntimeException("模板已禁用: " + templateCode);
        }

        return renderContent(template.getContent(), variables);
    }

    /**
     * 渲染模板内容
     * <p>
     * 使用变量替换内容中的占位符
     * </p>
     *
     * @param content   模板内容
     * @param variables 变量Map
     * @return 渲染后的内容
     */
    public String renderContent(String content, Map<String, Object> variables) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        if (variables == null || variables.isEmpty()) {
            return content;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 渲染模板主题
     * <p>
     * 渲染邮件主题（如果模板有主题）
     * </p>
     *
     * @param templateCode 模板编码
     * @param variables    变量Map
     * @return 渲染后的主题
     */
    public String renderSubject(String templateCode, Map<String, Object> variables) {
        NotificationTemplateEntity template = getTemplateByCode(templateCode);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + templateCode);
        }

        String subject = template.getSubject();
        if (subject == null || subject.isEmpty()) {
            return null;
        }

        return renderContent(subject, variables);
    }

    /**
     * 获取模板变量列表
     * <p>
     * 从模板的variables字段解析变量列表
     * </p>
     *
     * @param templateCode 模板编码
     * @return 变量列表
     */
    public List<String> getTemplateVariables(String templateCode) {
        NotificationTemplateEntity template = getTemplateByCode(templateCode);
        if (template == null) {
            return List.of();
        }

        String variablesJson = template.getVariables();
        if (variablesJson == null || variablesJson.isEmpty()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(variablesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 清除模板缓存
     * <p>
     * 用于模板更新后清除缓存
     * </p>
     *
     * @param templateCode 模板编码
     */
    public void evictCache(String templateCode) {
        if (templateCode == null || templateCode.isEmpty()) {
            return;
        }

        String cacheKey = CACHE_KEY_PREFIX + templateCode;
        cacheManager.evict(cacheKey);
    }
}
