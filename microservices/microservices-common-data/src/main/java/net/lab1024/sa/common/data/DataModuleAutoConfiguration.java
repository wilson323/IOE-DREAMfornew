package net.lab1024.sa.common.data;

import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

/**
 * 数据层模块自动配置
 * <p>
 * 提供MyBatis-Plus、Druid连接池、事务管理、Flyway迁移等数据访问相关功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Slf4j
@Configuration
public class DataModuleAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("[模块化] microservices-common-data 数据层模块已加载");
    }
}
