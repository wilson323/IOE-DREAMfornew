package net.lab1024.sa.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson配置类
 * <p>
 * 统一配置ObjectMapper Bean，确保与JsonUtil配置一致
 * 严格遵循CLAUDE.md规范：
 * - 使用@Configuration配置类
 * - 使用@Bean注册ObjectMapper
 * - 确保配置与JsonUtil一致（JavaTimeModule、日期格式）
 * </p>
 * <p>
 * 配置内容：
 * - 注册JavaTimeModule（支持LocalDateTime等Java 8时间类型）
 * - 禁用将日期写入为时间戳（使用ISO-8601格式）
 * - 确保与JsonUtil的静态ObjectMapper配置一致
 * </p>
 * <p>
 * 使用方式：
 * - 在需要ObjectMapper的地方，通过@Resource注入
 * - JsonUtil内部可以使用此Bean，或继续使用静态实例（向后兼容）
 * </p>
 * <p>
 * 迁移说明：从microservices-common-core迁移到microservices-common
 * 原因：common-core应保持最小稳定内核，不包含Spring配置类
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class JacksonConfiguration {

    /**
     * 配置ObjectMapper Bean
     * <p>
     * 使用@Primary确保这是默认的ObjectMapper Bean
     * 使用@ConditionalOnMissingBean避免与其他配置冲突
     * 配置与JsonUtil的静态ObjectMapper完全一致
     * </p>
     *
     * @return 配置好的ObjectMapper实例
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}

