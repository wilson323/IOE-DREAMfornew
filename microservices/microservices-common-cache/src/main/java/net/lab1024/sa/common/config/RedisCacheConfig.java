package net.lab1024.sa.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis分布式缓存配置
 *
 * 三级缓存架构 - L2层（分布式缓存）
 *
 * 特点：
 * - 分布式共享缓存（多实例共享）
 * - 数据持久化（重启后数据不丢失）
 * - 容量大（受Redis内存限制）
 * - 支持过期策略
 * - 网络开销（相比本地缓存慢）
 *
 * @author IOE-DREAM Team
 * @since 2025-01-XX
 */
@Slf4j
@EnableCaching
@Configuration
public class RedisCacheConfig {

    /**
     * RedisTemplate配置
     *
     * 配置说明：
     * - 使用Jackson2JsonRedisSerializer序列化对象
     * - 使用StringRedisSerializer序列化key
     * - 支持对象存储和读取
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("[Redis配置] 初始化RedisTemplate");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        jackson2JsonRedisSerializer.setObjectMapper(objectModel);

        // String序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);

        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();

        log.info("[Redis配置] RedisTemplate初始化完成");
        return template;
    }

    /**
     * RedisCacheManager配置
     *
     * 配置说明：
     * - 支持不同的缓存策略（用户、设备、字典等）
     * - 支持TTL过期时间
     * - 支持空值缓存（防止缓存穿透）
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        log.info("[Redis配置] 初始化RedisCacheManager");

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // 默认30分钟过期
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
            .disableCachingNullValues();  // 不缓存空值

        // 针对不同cacheName的个性化配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 用户基本信息缓存（5分钟过期）
        cacheConfigurations.put("userBasicInfo", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 设备信息缓存（10分钟过期）
        cacheConfigurations.put("deviceInfo", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 字典数据缓存（30分钟过期）
        cacheConfigurations.put("dictData", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 权限数据缓存（5分钟过期）
        cacheConfigurations.put("permission", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Token黑名单缓存（15分钟过期）
        cacheConfigurations.put("tokenBlacklist", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // 部门信息缓存（20分钟过期）
        cacheConfigurations.put("departmentInfo", defaultConfig.entryTtl(Duration.ofMinutes(20)));

        // 区域信息缓存（30分钟过期）
        cacheConfigurations.put("areaInfo", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()  // 支持事务
            .build();

        log.info("[Redis配置] RedisCacheManager初始化完成");
        return redisCacheManager;
    }
}
