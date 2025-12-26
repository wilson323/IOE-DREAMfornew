package net.lab1024.sa.common.security.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.security.filter.CsrfFilter;
import net.lab1024.sa.common.security.filter.SqlInjectionFilter;
import net.lab1024.sa.common.security.filter.XssFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import java.util.Arrays;

/**
 * 安全防护过滤器配置
 * <p>
 * 功能：注册安全防护过滤器
 * 过滤器顺序：
 * 1. SQL注入防护过滤器（最高优先级）
 * 2. XSS防护过滤器（第二优先级）
 * 3. CSRF防护过滤器（第三优先级）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityFilterConfiguration {

    @Resource
    private SqlInjectionFilter sqlInjectionFilter;

    @Resource
    private XssFilter xssFilter;

    @Resource
    private CsrfFilter csrfFilter;

    /**
     * 注册SQL注入防护过滤器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.sql-injection", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<SqlInjectionFilter> sqlInjectionFilterRegistration() {
        log.info("[安全防护] 注册SQL注入防护过滤器");

        FilterRegistrationBean<SqlInjectionFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(sqlInjectionFilter);
        registration.addUrlPatterns("/*");
        registration.setName("sqlInjectionFilter");
        registration.setOrder(1); // 最高优先级

        log.info("[安全防护] SQL注入防护过滤器注册成功");
        return registration;
    }

    /**
     * 注册XSS防护过滤器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.xss", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        log.info("[安全防护] 注册XSS防护过滤器");

        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(xssFilter);
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(2); // 第二优先级

        log.info("[安全防护] XSS防护过滤器注册成功");
        return registration;
    }

    /**
     * 注册CSRF防护过滤器
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.csrf", name = "enabled", havingValue = "true", matchIfMissing = false)
    public FilterRegistrationBean<CsrfFilter> csrfFilterRegistration() {
        log.info("[安全防护] 注册CSRF防护过滤器");

        FilterRegistrationBean<CsrfFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(csrfFilter);
        registration.addUrlPatterns("/*");
        registration.setName("csrfFilter");
        registration.setOrder(3); // 第三优先级

        log.info("[安全防护] CSRF防护过滤器注册成功");
        return registration;
    }
}
