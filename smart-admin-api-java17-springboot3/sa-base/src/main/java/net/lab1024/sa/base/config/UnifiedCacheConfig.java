package net.lab1024.sa.base.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.cache.CacheMetricsCollector;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import net.lab1024.sa.base.common.cache.UnifiedCacheManager;

/**
 * 统一缓存配置
 * <p>
 * 严格遵循repowiki配置规范：
 * - 统一缓存策略配置
 * - 多级缓存支持
 * - 性能优化配置
 * - 监控和统计配置
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Configuration
public class UnifiedCacheConfig {

    /**
     * Redis缓存管理器配置
     */
    @Bean
    @Primary
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        log.info("配置Redis缓存管理器");

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认30分钟过期
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        // 针对不同命名空间的缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 用户缓存 - 30分钟
        cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 设备缓存 - 10分钟
        cacheConfigurations.put("device", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 门禁缓存 - 5分钟
        cacheConfigurations.put("access", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 考勤缓存 - 15分钟
        cacheConfigurations.put("attendance", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // 消费缓存 - 10分钟
        cacheConfigurations.put("consume", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 视频缓存 - 5分钟
        cacheConfigurations.put("video", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 文档缓存 - 30分钟
        cacheConfigurations.put("document", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 系统缓存 - 60分钟
        cacheConfigurations.put("system", defaultConfig.entryTtl(Duration.ofMinutes(60)));

        // 临时缓存 - 5分钟
        cacheConfigurations.put("temp", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 配置缓存 - 120分钟
        cacheConfigurations.put("config", defaultConfig.entryTtl(Duration.ofMinutes(120)));

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware() // 支持事务
                .build();

        log.info("Redis缓存管理器配置完成，缓存配置数量: {}", cacheConfigurations.size());
        return cacheManager;
    }

    /**
     * RedisTemplate配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("配置RedisTemplate");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用String序列化器序列化key
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 使用Jackson序列化器序列化value
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        log.info("RedisTemplate配置完成");
        return template;
    }

    /**
     * 本地Caffeine缓存配置
     */
    @Bean
    @ConditionalOnProperty(name = "cache.local.enabled", havingValue = "true", matchIfMissing = true)
    public Cache<String, Object> localCaffeineCache() {
        log.info("配置本地Caffeine缓存");

        Cache<String, Object> cache = Caffeine.newBuilder()
                .maximumSize(10_000) // 最大缓存条数
                .expireAfterWrite(5, TimeUnit.MINUTES) // 写入后5分钟过期
                .expireAfterAccess(3, TimeUnit.MINUTES) // 访问后3分钟过期
                .recordStats() // 启用统计
                .build();

        log.info("本地Caffeine缓存配置完成");
        return cache;
    }

    /**
     * 缓存监控配置
     */
    @Bean
    @ConditionalOnProperty(name = "cache.monitoring.enabled", havingValue = "true", matchIfMissing = true)
    public CacheMonitoringConfig cacheMonitoringConfig() {
        log.info("配置缓存监控");

        return new CacheMonitoringConfig();
    }

    /**
     * 缓存预热配置
     */
    @Bean
    @ConditionalOnProperty(name = "cache.warmup.enabled", havingValue = "false", matchIfMissing = true)
    public CacheWarmupConfig cacheWarmupConfig() {
        log.info("配置缓存预热");

        return new CacheWarmupConfig();
    }

    /**
     * 缓存监控配置类
     */
    public static class CacheMonitoringConfig {
        private boolean enabled = true;
        private long metricsReportInterval = 60; // 秒
        private long healthCheckInterval = 300; // 秒
        private double healthScoreThreshold = 70.0; // 健康度阈值
        private boolean alertEnabled = true;

        // getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getMetricsReportInterval() {
            return metricsReportInterval;
        }

        public void setMetricsReportInterval(long metricsReportInterval) {
            this.metricsReportInterval = metricsReportInterval;
        }

        public long getHealthCheckInterval() {
            return healthCheckInterval;
        }

        public void setHealthCheckInterval(long healthCheckInterval) {
            this.healthCheckInterval = healthCheckInterval;
        }

        public double getHealthScoreThreshold() {
            return healthScoreThreshold;
        }

        public void setHealthScoreThreshold(double healthScoreThreshold) {
            this.healthScoreThreshold = healthScoreThreshold;
        }

        public boolean isAlertEnabled() {
            return alertEnabled;
        }

        public void setAlertEnabled(boolean alertEnabled) {
            this.alertEnabled = alertEnabled;
        }
    }

    /**
     * 缓存预热配置类
     */
    public static class CacheWarmupConfig {
        private boolean enabled = false;
        private boolean autoWarmup = false;
        private String warmupSchedule = "0 0 2 * * ?"; // 每天凌晨2点
        private int batchSize = 100; // 批量大小
        private long batchInterval = 100; // 批次间隔（毫秒）

        // getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAutoWarmup() {
            return autoWarmup;
        }

        public void setAutoWarmup(boolean autoWarmup) {
            this.autoWarmup = autoWarmup;
        }

        public String getWarmupSchedule() {
            return warmupSchedule;
        }

        public void setWarmupSchedule(String warmupSchedule) {
            this.warmupSchedule = warmupSchedule;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getBatchInterval() {
            return batchInterval;
        }

        public void setBatchInterval(long batchInterval) {
            this.batchInterval = batchInterval;
        }
    }

    /**
     * 缓存性能配置类
     */
    @lombok.Data
    @lombok.Builder
    public static class CachePerformanceConfig {
        @lombok.Builder.Default
        private boolean compressionEnabled = false;
        @lombok.Builder.Default
        private int compressionThreshold = 1024; // 压缩阈值（字节）
        @lombok.Builder.Default
        private boolean serializationOptimized = true;
        @lombok.Builder.Default
        private boolean asyncEnabled = true;
        @lombok.Builder.Default
        private int asyncThreadPoolSize = 5;
        @lombok.Builder.Default
        private long asyncTimeout = 5000; // 毫秒

        // 缓存连接池配置
        @lombok.Builder.Default
        private int maxTotal = 20;
        @lombok.Builder.Default
        private int maxIdle = 10;
        @lombok.Builder.Default
        private int minIdle = 2;
        @lombok.Builder.Default
        private long maxWaitMillis = 3000;

        // 批量操作配置
        @lombok.Builder.Default
        private int batchSize = 1000;
        @lombok.Builder.Default
        private long batchTimeout = 10000; // 毫秒

        // 重试配置
        @lombok.Builder.Default
        private int maxAttempts = 3;
        @lombok.Builder.Default
        private long retryDelay = 100; // 毫秒
        @lombok.Builder.Default
        private double retryMultiplier = 2.0;
    }

    /**
     * 缓存安全配置类
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheSecurityConfig {
        @lombok.Builder.Default
        private boolean encryptionEnabled = false;
        @lombok.Builder.Default
        private String encryptionAlgorithm = "AES";
        @lombok.Builder.Default
        private String encryptionKey = "";
        @lombok.Builder.Default
        private boolean accessLogEnabled = true;
        @lombok.Builder.Default
        private boolean sensitiveDataMasking = true;
        @lombok.Builder.Default
        private long accessLogRetention = 30; // 天

        // 权限控制
        @lombok.Builder.Default
        private boolean rbacEnabled = false;
        @lombok.Builder.Default
        private String defaultPermission = "cache:read";
        @lombok.Builder.Default
        private Set<String> adminRoles = Set.of("ADMIN", "CACHE_ADMIN");
    }

    /**
     * 自定义ObjectMapper配置（用于Redis序列化）
     */
    @Bean
    public ObjectMapper cacheObjectMapper() {
        log.info("配置缓存ObjectMapper");

        ObjectMapper mapper = new ObjectMapper();

        // 配置序列化特性
        mapper.findAndRegisterModules();

        log.info("缓存ObjectMapper配置完成");
        return mapper;
    }

    /**
     * 缓存健康检查配置
     */
    @Bean
    public CacheHealthChecker cacheHealthChecker(UnifiedCacheManager unifiedCacheManager,
            CacheMetricsCollector metricsCollector) {
        log.info("配置缓存健康检查器");

        return new CacheHealthChecker(unifiedCacheManager, metricsCollector);
    }

    /**
     * 缓存健康检查器
     */
    public static class CacheHealthChecker {
        private final UnifiedCacheManager cacheManager;
        private final CacheMetricsCollector metricsCollector;

        public CacheHealthChecker(UnifiedCacheManager cacheManager, CacheMetricsCollector metricsCollector) {
            this.cacheManager = cacheManager;
            this.metricsCollector = metricsCollector;
        }

        /**
         * 执行健康检查
         */
        public Map<String, Object> performHealthCheck() {
            Map<String, Object> healthResult = new HashMap<>();

            try {
                // 获取缓存健康度评估
                Map<String, Object> assessment = metricsCollector.getHealthAssessment();
                healthResult.put("assessment", assessment);

                // 检查各命名空间状态
                Map<String, Map<String, Object>> allStats = metricsCollector.getAllStatistics();
                healthResult.put("statistics", allStats);

                // 执行基础连通性测试
                boolean connectivityTest = testCacheConnectivity();
                healthResult.put("connectivity", connectivityTest ? "正常" : "异常");

                // 计算总体健康状态
                double globalHealthScore = (Double) assessment.get("globalHealthScore");
                String healthStatus = globalHealthScore >= 80 ? "健康" : globalHealthScore >= 60 ? "警告" : "异常";

                healthResult.put("status", healthStatus);
                healthResult.put("score", globalHealthScore);
                healthResult.put("checkTime", System.currentTimeMillis());

                return healthResult;

            } catch (Exception e) {
                log.error("缓存健康检查失败", e);
                healthResult.put("status", "异常");
                healthResult.put("error", e.getMessage());
                healthResult.put("checkTime", System.currentTimeMillis());
                return healthResult;
            }
        }

        /**
         * 测试缓存连通性
         */
        private boolean testCacheConnectivity() {
            try {
                // 简单的连通性测试
                CacheNamespace testNamespace = CacheNamespace.TEMP;
                String testKey = "health-check-" + System.currentTimeMillis();
                String testValue = "ping";

                // 设置测试值
                UnifiedCacheManager.CacheResult<String> setResult = cacheManager.set(testNamespace, testKey, testValue);
                if (!setResult.isSuccess()) {
                    return false;
                }

                // 获取测试值
                UnifiedCacheManager.CacheResult<String> getResult = cacheManager.get(testNamespace, testKey,
                        String.class);
                if (!getResult.isSuccess()) {
                    return false;
                }

                // 删除测试值
                cacheManager.delete(testNamespace, testKey);

                return testValue.equals(getResult.getData());

            } catch (Exception e) {
                log.error("缓存连通性测试失败", e);
                return false;
            }
        }
    }
}
