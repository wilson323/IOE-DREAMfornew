package net.lab1024.sa.visitor.controller;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * 访客移动端控制器测试配置
 * <p>
 * 排除数据源和JPA自动配置，避免在@WebMvcTest测试中初始化数据库相关Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Configuration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
class TestConfig {
}
