package net.lab1024.sa.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Redis配置类
 * 基于现有项目Redis配置重构
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate配置（基于现有项目Redis使用模式）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                objectMapper, Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 缓存键前缀配置（基于现有项目缓存模式）
     */
    public static class CacheKeyPrefix {
        public static final String USER_SESSION = "user:session:";
        public static final String USER_INFO = "user:info:";
        public static final String USER_ROLES = "user:roles:";
        public static final String USER_PERMISSIONS = "user:permissions:";
        public static final String LOGIN_ATTEMPTS = "login:attempts:";
        public static final String TOKEN_BLACKLIST = "token:blacklist:";
    }

    /**
     * 缓存过期时间配置（基于现有项目缓存策略）
     */
    public static class CacheExpiration {
        public static final long USER_SESSION = 7200; // 2小时
        public static final long USER_INFO = 3600; // 1小时
        public static final long USER_ROLES = 1800; // 30分钟
        public static final long USER_PERMISSIONS = 1800; // 30分钟
        public static final long LOGIN_ATTEMPTS = 900; // 15分钟
        public static final long TOKEN_BLACKLIST = 604800; // 7天
    }
}
