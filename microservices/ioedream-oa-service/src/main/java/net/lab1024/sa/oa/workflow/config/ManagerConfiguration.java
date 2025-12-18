package net.lab1024.sa.oa.workflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.oa.workflow.cache.WorkflowCacheManager;

/**
 * Manager配置类
 * <p>
 * 用于将OA模块特有的Manager实现类注册为Spring Bean
 * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过配置类注册
 * </p>
 * <p>
 * 注意：公共Manager（NotificationManager、WorkflowApprovalManager、ApprovalConfigManager等）
 * 已由CommonBeanAutoConfiguration统一装配，无需在此重复定义
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-14 移除重复的公共Bean定义，改用统一自动装配
 * @updated 2025-01-30 添加WorkflowCacheManager Bean注册
 */
@Slf4j
@Configuration("oaManagerConfiguration")
public class ManagerConfiguration {

    /**
     * 注册WorkflowCacheManager（cache包）为Spring Bean
     * <p>
     * 工作流缓存管理器，实现三级缓存架构
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过配置类注册
     * </p>
     *
     * @param redisTemplate Redis模板
     * @return 工作流缓存管理器实例
     */
    @Bean("workflowCacheManager")
    @ConditionalOnMissingBean(name = "workflowCacheManager")
    public WorkflowCacheManager workflowCacheManager(RedisTemplate<String, Object> redisTemplate) {
        log.info("[WorkflowCacheManager] 初始化工作流缓存管理器（cache包）");
        return new WorkflowCacheManager(redisTemplate);
    }

    /**
     * 注册WorkflowCacheManager（performance包）为Spring Bean
     * <p>
     * 工作流高级缓存管理器，提供企业级多级缓存策略和性能监控
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过配置类注册
     * </p>
     * <p>
     * 注意：使用完整类名避免与cache包的WorkflowCacheManager冲突
     * </p>
     *
     * @param redisTemplate Redis模板
     * @param meterRegistry 指标注册表
     * @return 工作流高级缓存管理器实例
     */
    @Bean("workflowPerformanceCacheManager")
    @ConditionalOnMissingBean(name = "workflowPerformanceCacheManager")
    public net.lab1024.sa.oa.workflow.performance.WorkflowCacheManager workflowPerformanceCacheManager(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry) {
        log.info("[WorkflowPerformanceCacheManager] 初始化工作流高级缓存管理器（performance包）");
        return new net.lab1024.sa.oa.workflow.performance.WorkflowCacheManager(redisTemplate, meterRegistry);
    }
}





