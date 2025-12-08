package net.lab1024.sa.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM API网关服务启动类 (企业级实现)
 * <p>
 * 端口: 8080
 * 职责: API网关、路由转发、负载均衡、限流熔断、统一认证等
 * </p>
 * <p>
 * <b>技术架构</b>:
 * <ul>
 *   <li>Spring Cloud Gateway + WebFlux (Reactive Stack)</li>
 *   <li>Netty作为底层HTTP服务器 (非Servlet)</li>
 *   <li>Spring Security WebFlux (Reactive Security)</li>
 * </ul>
 * </p>
 * <p>
 * <b>重要说明</b>:
 * Spring Cloud Gateway必须使用WebFlux(Reactive)模式，
 * 与Servlet(Spring MVC)模式不兼容。已通过以下方式排除Servlet组件:
 * <ul>
 *   <li>pom.xml中排除spring-boot-starter-web</li>
 *   <li>pom.xml中排除spring-security-web</li>
 *   <li>应用类中排除Servlet自动配置</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        "net.lab1024.sa.common",
        "net.lab1024.sa.gateway"
    },
    exclude = {
        // 排除JPA自动配置 (使用MyBatis-Plus)
        HibernateJpaAutoConfiguration.class,
        // 排除所有Servlet Security自动配置 (网关使用WebFlux Reactive Security)
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        // 排除Spring MVC自动配置 (网关使用WebFlux)
        WebMvcAutoConfiguration.class
    }
)
@EnableDiscoveryClient
@MapperScan(basePackages = {
    // Common模块所有DAO包（17个，按字母排序）
    "net.lab1024.sa.common.attendance.dao",
    "net.lab1024.sa.common.audit.dao",
    "net.lab1024.sa.common.auth.dao",
    "net.lab1024.sa.common.consume.dao",
    "net.lab1024.sa.common.device.dao",
    "net.lab1024.sa.common.menu.dao",
    "net.lab1024.sa.common.monitor.dao",
    "net.lab1024.sa.common.notification.dao",
    "net.lab1024.sa.common.oa.dao",
    "net.lab1024.sa.common.organization.dao",
    "net.lab1024.sa.common.rbac.dao",
    "net.lab1024.sa.common.scheduler.dao",
    "net.lab1024.sa.common.system.dao",
    "net.lab1024.sa.common.system.employee.dao",
    "net.lab1024.sa.common.video.dao",
    "net.lab1024.sa.common.visitor.dao",
    "net.lab1024.sa.common.workflow.dao"
})
public class GatewayServiceApplication {

    /**
     * 主启动方法
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
