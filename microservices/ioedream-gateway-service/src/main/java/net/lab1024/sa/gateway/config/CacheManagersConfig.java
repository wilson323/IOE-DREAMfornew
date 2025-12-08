package net.lab1024.sa.gateway.config;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.UnifiedCacheManager;

/**
 * 缓存Manager配置类
 * <p>
 * 符合CLAUDE.md规范 - Manager类通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 注册UnifiedCacheManager为Spring Bean
 * - 统一管理缓存模块的依赖注入
 * </p>
 * <p>
 * 企业级特性：
 * - 多级缓存架构（L1本地缓存 + L2 Redis缓存）
 * - Caffeine高性能本地缓存
 * - Redis分布式缓存
 * - Redisson分布式锁（缓存击穿防护）
 * - 缓存命中率监控
 * - 缓存预热支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Configuration
public class CacheManagersConfig {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 注册企业级UnifiedCacheManager
     * <p>
     * 符合CLAUDE.md规范：
     * - Manager类是纯Java类，通过构造函数注入依赖
     * - 在微服务中通过配置类将Manager注册为Spring Bean
     * - 使用@Resource注解进行依赖注入（禁止@Autowired）
     * </p>
     * <p>
     * 企业级功能：
     * - L1本地缓存：Caffeine，10000容量，5分钟过期
     * - L2分布式缓存：Redis
     * - 分布式锁：Redisson（防止缓存击穿）
     * - 缓存命中率监控：Micrometer
     * - 缓存穿透防护
     * - 缓存雪崩防护
     * </p>
     * <p>
     * 缓存策略：
     * - 读取顺序：L1本地缓存 → L2 Redis缓存 → 数据源
     * - 写入策略：同时写入L1和L2
     * - 失效策略：L2失效时自动清除L1
     * </p>
     *
     * @return UnifiedCacheManager实例
     */
    @Bean
    public UnifiedCacheManager unifiedCacheManager() {
        log.info("[UnifiedCacheManager] 初始化企业级统一缓存管理器");
        log.info("[UnifiedCacheManager] RedisTemplate: {}", redisTemplate != null ? "已注入" : "未注入");
        log.info("[UnifiedCacheManager] RedissonClient: {}", redissonClient != null ? "已注入" : "未注入");
        log.info("[UnifiedCacheManager] MeterRegistry: {}", meterRegistry != null ? "已注入" : "未注入");

        UnifiedCacheManager cacheManager = new UnifiedCacheManager(
                redisTemplate,
                redissonClient,
                meterRegistry
        );

        log.info("[UnifiedCacheManager] 企业级统一缓存管理器初始化完成");
        log.info("[UnifiedCacheManager] L1本地缓存：Caffeine（10000容量，5分钟过期）");
        log.info("[UnifiedCacheManager] L2分布式缓存：Redis");
        log.info("[UnifiedCacheManager] 分布式锁：Redisson（缓存击穿防护）");
        log.info("[UnifiedCacheManager] 监控：Micrometer（缓存命中率）");
        
        return cacheManager;
    }
}
