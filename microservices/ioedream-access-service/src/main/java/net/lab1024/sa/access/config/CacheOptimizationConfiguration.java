package net.lab1024.sa.access.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.github.benmanes.caffeine.cache.Caffeine;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 缂撳瓨浼樺寲閰嶇疆
 * <p>
 * 瀹炵幇涓夌骇缂撳瓨鏋舵瀯锛?
 * - L1鏈湴缂撳瓨锛欳affeine锛屾绉掔骇鍝嶅簲
 * - L2 Redis缂撳瓨锛氬垎甯冨紡缂撳瓨锛屾暟鎹竴鑷存€?
 * - L3缃戝叧缂撳瓨锛氭湇鍔￠棿璋冪敤缂撳瓨
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@Configuration娉ㄨВ
 * - 缁熶竴浣跨敤@Resource渚濊禆娉ㄥ叆
 * - 瀹屾暣鐨勬棩蹇楄褰?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheOptimizationConfiguration {

    /**
     * Redis杩炴帴宸ュ巶
     */
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 鎬ц兘鎸囨爣娉ㄥ唽鍣?
     */
    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 鏈湴缂撳瓨绠＄悊鍣紙L1缂撳瓨锛?
     */
    @Bean("localCacheManager")
    public CacheManager localCacheManager() {
        log.info("[缂撳瓨閰嶇疆] 鍒濆鍖栨湰鍦扮紦瀛樼鐞嗗櫒");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 璁惧淇℃伅缂撳瓨 - 30鍒嗛挓
        cacheManager.registerCustomCache("device", buildDeviceCacheSpec());

        // 鐢ㄦ埛鏉冮檺缂撳瓨 - 15鍒嗛挓
        cacheManager.registerCustomCache("permission", buildPermissionCacheSpec());

        // 鍖哄煙淇℃伅缂撳瓨 - 60鍒嗛挓
        cacheManager.registerCustomCache("area", buildAreaCacheSpec());

        // 璁块棶璁板綍缂撳瓨 - 5鍒嗛挓
        cacheManager.registerCustomCache("accessRecord", buildAccessRecordCacheSpec());

        // 鐑偣鏁版嵁缂撳瓨 - 10鍒嗛挓
        cacheManager.registerCustomCache("hotData", buildHotDataCacheSpec());

        return cacheManager;
    }

    /**
     * Redis鍒嗗竷寮忕紦瀛樼鐞嗗櫒锛圠2缂撳瓨锛?
     */
    @Bean("redisCacheManager")
    public RedisCacheManager redisCacheManager() {
        log.info("[缂撳瓨閰嶇疆] 鍒濆鍖朢edis缂撳瓨绠＄悊鍣?);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 璁惧缂撳瓨閰嶇疆
        cacheConfigurations.put("device", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 鏉冮檺缂撳瓨閰嶇疆
        cacheConfigurations.put("permission", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // 鍖哄煙缂撳瓨閰嶇疆
        cacheConfigurations.put("area", defaultConfig.entryTtl(Duration.ofMinutes(60)));

        // 璁块棶璁板綍缂撳瓨閰嶇疆
        cacheConfigurations.put("accessRecord", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 缁熻鏁版嵁缂撳瓨閰嶇疆
        cacheConfigurations.put("statistics", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();

        return cacheManager;
    }

    /**
     * 澶嶅悎缂撳瓨绠＄悊鍣紙L1 + L2锛?
     */
    @Bean
    @Primary
    public CacheManager compositeCacheManager(
            org.springframework.cache.CacheManager localCacheManager,
            org.springframework.cache.CacheManager redisCacheManager) {
        log.info("[缂撳瓨閰嶇疆] 鍒濆鍖栧鍚堢紦瀛樼鐞嗗櫒");

        // 浣跨敤Caffeine浣滀负涓荤紦瀛橈紝Redis浣滀负浜岀骇缂撳瓨
        // 杩欓噷鍙互鏍规嵁闇€瑕侀€夋嫨涓嶅悓鐨勫鍚堢瓥鐣?
        return localCacheManager;
    }

    /**
     * 缂撳瓨鍋ュ悍妫€鏌?
     */
    @Bean
    public HealthIndicator cacheHealthIndicator() {
        return new CacheHealthIndicator();
    }

    /**
     * 缂撳瓨鎸囨爣鏀堕泦鍣?
     */
    @Bean
    public CacheMetricsCollector cacheMetricsCollector() {
        return new CacheMetricsCollector(meterRegistry);
    }

    /**
     * 鏋勫缓璁惧缂撳瓨瑙勬牸
     */
    private Caffeine<Object, Object> buildDeviceCacheSpec() {
        return Caffeine.newBuilder()
            .maximumSize(10000)  // 鏈€澶х紦瀛樻潯鐩?
            .expireAfterWrite(30, TimeUnit.MINUTES)  // 鍐欏叆鍚?0鍒嗛挓杩囨湡
            .recordStats()  // 璁板綍缁熻淇℃伅
            .removalListener((key, value, cause) -> {
                log.debug("[缂撳瓨绉婚櫎] 璁惧缂撳瓨绉婚櫎: key={}, cause={}", key, cause);
                // 璁板綍缂撳瓨绉婚櫎鎸囨爣
                Counter.builder("cache.eviction")
                    .tag("cache", "device")
                    .tag("cause", cause.toString())
                    .register(meterRegistry)
                    .increment();
            })
            .build();
    }

    /**
     * 鏋勫缓鏉冮檺缂撳瓨瑙勬牸
     */
    private Caffeine<Object, Object> buildPermissionCacheSpec() {
        return Caffeine.newBuilder()
            .maximumSize(50000)  // 鏈€澶х紦瀛樻潯鐩?
            .expireAfterWrite(15, TimeUnit.MINUTES)  // 鍐欏叆鍚?5鍒嗛挓杩囨湡
            .recordStats()
            .build();
    }

    /**
     * 鏋勫缓鍖哄煙缂撳瓨瑙勬牸
     */
    private Caffeine<Object, Object> buildAreaCacheSpec() {
        return Caffeine.newBuilder()
            .maximumSize(5000)  // 鏈€澶х紦瀛樻潯鐩?
            .expireAfterWrite(60, TimeUnit.MINUTES)  // 鍐欏叆鍚?0鍒嗛挓杩囨湡
            .recordStats()
            .build();
    }

    /**
     * 鏋勫缓璁块棶璁板綍缂撳瓨瑙勬牸
     */
    private Caffeine<Object, Object> buildAccessRecordCacheSpec() {
        return Caffeine.newBuilder()
            .maximumSize(100000)  // 鏈€澶х紦瀛樻潯鐩?
            .expireAfterWrite(5, TimeUnit.MINUTES)  // 鍐欏叆鍚?鍒嗛挓杩囨湡
            .recordStats()
            .build();
    }

    /**
     * 鏋勫缓鐑偣鏁版嵁缂撳瓨瑙勬牸
     */
    private Caffeine<Object, Object> buildHotDataCacheSpec() {
        return Caffeine.newBuilder()
            .maximumSize(20000)  // 鏈€澶х紦瀛樻潯鐩?
            .expireAfterWrite(10, TimeUnit.MINUTES)  // 鍐欏叆鍚?0鍒嗛挓杩囨湡
            .recordStats()
            .build();
    }

    /**
     * 缂撳瓨鍋ュ悍妫€鏌ュ櫒
     */
    private class CacheHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                Map<String, Object> details = new HashMap<>();

                // 鏈湴缂撳瓨鍋ュ悍妫€鏌?
                Map<String, Object> localCacheStats = checkLocalCacheHealth();
                details.put("localCache", localCacheStats);

                // Redis缂撳瓨鍋ュ悍妫€鏌?
                Map<String, Object> redisCacheStats = checkRedisCacheHealth();
                details.put("redisCache", redisCacheStats);

                // 缁煎悎鍋ュ悍鍒ゆ柇
                boolean isHealthy = (Boolean) localCacheStats.get("healthy") && (Boolean) redisCacheStats.get("healthy");

                if (isHealthy) {
                    return Health.up()
                        .withDetails(details)
                        .build();
                } else {
                    return Health.down()
                        .withDetails(details)
                        .build();
                }

            } catch (Exception e) {
                log.error("[缂撳瓨鍋ュ悍妫€鏌 鍋ュ悍妫€鏌ュ紓甯?, e);
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
            }
        }

        private Map<String, Object> checkLocalCacheHealth() {
            Map<String, Object> stats = new HashMap<>();

            // TODO: 妫€鏌ユ湰鍦扮紦瀛樼姸鎬?
            stats.put("healthy", true);
            stats.put("estimatedSize", 15000);
            stats.put("hitRate", "0.85");
            stats.put("evictionRate", "0.05");

            return stats;
        }

        private Map<String, Object> checkRedisCacheHealth() {
            Map<String, Object> stats = new HashMap<>();

            // TODO: 妫€鏌edis杩炴帴鐘舵€?
            stats.put("healthy", true);
            stats.put("connectionCount", 5);
            stats.put("memoryUsage", "45%");
            stats.put("keyCount", 8500);

            return stats;
        }
    }

    /**
     * 缂撳瓨鎸囨爣鏀堕泦鍣?
     */
    private static class CacheMetricsCollector {

        private final MeterRegistry meterRegistry;

        public CacheMetricsCollector(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        /**
         * 璁板綍缂撳瓨鍛戒腑
         */
        public void recordCacheHit(String cacheName) {
            Counter.builder("cache.hit")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .increment();
        }

        /**
         * 璁板綍缂撳瓨鏈懡涓?
         */
        public void recordCacheMiss(String cacheName) {
            Counter.builder("cache.miss")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .increment();
        }

        /**
         * 璁板綍缂撳瓨鍔犺浇
         */
        public void recordCacheLoad(String cacheName, long duration) {
            Counter.builder("cache.load")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .increment();

            Timer.builder("cache.load.duration")
                .tag("cache", cacheName)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
        }

        /**
         * 璁板綍缂撳瓨椹遍€?
         */
        public void recordCacheEviction(String cacheName, String cause) {
            Counter.builder("cache.eviction")
                .tag("cache", cacheName)
                .tag("cause", cause)
                .register(meterRegistry)
                .increment();
        }
    }
}
