package net.lab1024.sa.gateway;

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
 * <b>内存优化说明</b>:
 * scanBasePackages精确配置，只扫描网关服务需要的公共包，
 * 减少不必要的类加载，优化内存使用。
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        // 网关服务自身包
        "net.lab1024.sa.gateway",
        // 核心配置（必需）
        "net.lab1024.sa.common.config",
        // 网关相关组件
        "net.lab1024.sa.common.gateway",
        // 响应和异常处理
        "net.lab1024.sa.common.response",
        "net.lab1024.sa.common.exception",
        // 工具类
        "net.lab1024.sa.common.util",
        // 安全认证
        "net.lab1024.sa.common.security"
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

