package net.lab1024.sa.common.cache;

import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

/**
 * 缓存模块自动配置
 * <p>
 * 提供Caffeine本地缓存、Redis分布式缓存、Redisson分布式锁等缓存相关功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Slf4j
@Configuration
public class CacheModuleAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("[模块化] microservices-common-cache 缓存模块已加载");
    }
}
