package net.lab1024.sa.consume.cache.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.cache.MultiLevelCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 多级缓存配置
 *
 * 职责：配置L1（Caffeine）+ L2（Redis）两级缓存
 *
 * 缓存配置：
 * 1. 账户缓存：5分钟L1，30分钟L2
 * 2. 区域缓存：10分钟L1，1小时L2
 * 3. 补贴缓存：5分钟L1，30分钟L2
 * 4. 配置缓存：30分钟L1，2小时L2
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
@Configuration
@EnableCaching
public class MultiLevelCacheConfiguration {

    /**
     * 配置RedisTemplate（用于多级缓存）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化value
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        serializer.setObjectMapper(mapper);

        // 使用StringRedisSerializer来序列化和反序列化key
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        log.info("[多级缓存配置] RedisTemplate配置完成");

        return template;
    }

    /**
     * 配置Spring Cache CacheManager（用于@Cacheable注解）
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(createJsonSerializer()))
                .disableCachingNullValues();

        // 针对不同缓存名的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 账户缓存：5分钟
        cacheConfigurations.put("account", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 区域缓存：10分钟
        cacheConfigurations.put("area", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 补贴缓存：5分钟
        cacheConfigurations.put("subsidy", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 配置缓存：30分钟
        cacheConfigurations.put("config", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 统计缓存：1小时
        cacheConfigurations.put("statistics", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * 创建JSON序列化器
     */
    private Jackson2JsonRedisSerializer<Object> createJsonSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        serializer.setObjectMapper(mapper);
        return serializer;
    }

    /**
     * 账户缓存管理器
     */
    @Bean(name = "accountCacheManager")
    public MultiLevelCacheManager<String, Object> accountCacheManager(
            RedisTemplate<String, Object> redisTemplate) {
        return new MultiLevelCacheManager<>(
                "account",
                redisTemplate,
                5000,   // L1最大5000条
                5,      // L1过期5分钟
                1800    // L2过期30分钟
        );
    }

    /**
     * 区域缓存管理器
     */
    @Bean(name = "areaCacheManager")
    public MultiLevelCacheManager<String, Object> areaCacheManager(
            RedisTemplate<String, Object> redisTemplate) {
        return new MultiLevelCacheManager<>(
                "area",
                redisTemplate,
                1000,   // L1最大1000条
                10,     // L1过期10分钟
                3600    // L2过期1小时
        );
    }

    /**
     * 补贴缓存管理器
     */
    @Bean(name = "subsidyCacheManager")
    public MultiLevelCacheManager<String, Object> subsidyCacheManager(
            RedisTemplate<String, Object> redisTemplate) {
        return new MultiLevelCacheManager<>(
                "subsidy",
                redisTemplate,
                3000,   // L1最大3000条
                5,      // L1过期5分钟
                1800    // L2过期30分钟
        );
    }

    /**
     * 配置缓存管理器
     */
    @Bean(name = "configCacheManager")
    public MultiLevelCacheManager<String, Object> configCacheManager(
            RedisTemplate<String, Object> redisTemplate) {
        return new MultiLevelCacheManager<>(
                "config",
                redisTemplate,
                500,    // L1最大500条
                30,     // L1过期30分钟
                7200    // L2过期2小时
        );
    }
}
