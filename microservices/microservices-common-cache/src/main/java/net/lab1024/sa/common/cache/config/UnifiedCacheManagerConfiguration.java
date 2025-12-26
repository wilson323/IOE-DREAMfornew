package net.lab1024.sa.common.cache.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import net.lab1024.sa.common.cache.UnifiedCacheManager;

/**
 * UnifiedCacheManager配置类
 * <p>
 * 将UnifiedCacheManager注册为Spring Bean，供所有微服务使用
 * 严格遵循CLAUDE.md规范：
 * - 使用@Configuration注解
 * - 使用@ConditionalOnMissingBean避免重复注册
 * - 通过构造函数注入依赖
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class UnifiedCacheManagerConfiguration {

    /**
     * 注册UnifiedCacheManager为Spring Bean
     * <p>
     * 三级缓存架构：
     * - L1: Caffeine本地缓存 (毫秒级响应)
     * - L2: Redis分布式缓存 (数据一致性)
     * - L3: 网关缓存 (服务间调用优化)
     * </p>
     *
     * @param redisTemplate  Redis模板
     * @param redissonClient Redisson客户端（用于分布式锁和L3缓存）
     * @return UnifiedCacheManager实例
     */
    @Bean
    @ConditionalOnMissingBean(UnifiedCacheManager.class)
    public UnifiedCacheManager unifiedCacheManager(
            RedisTemplate<String, Object> redisTemplate,
            RedissonClient redissonClient) {
        return new UnifiedCacheManager(redisTemplate, redissonClient);
    }
}
