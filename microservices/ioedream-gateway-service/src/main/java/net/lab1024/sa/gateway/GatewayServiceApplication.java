package net.lab1024.sa.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM API网关服务启动类
 * <p>
 * 端口: 8080
 * 职责: API网关、路由转发、负载均衡、限流熔断、统一认证等
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 正确配置包扫描路径
 * </p>
 * <p>
 * 核心功能模块:
 * - API网关：统一入口、路由转发
 * - 负载均衡：服务负载均衡、健康检查
 * - 限流熔断：接口限流、服务熔断
 * - 统一认证：JWT验证、权限校验
 * - 监控日志：请求日志、监控指标
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
        HibernateJpaAutoConfiguration.class
    }
)
@EnableDiscoveryClient
public class GatewayServiceApplication {

    /**
     * 主启动方法
     * <p>
     * 启动IOE-DREAM API网关服务，端口8080
     * </p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
