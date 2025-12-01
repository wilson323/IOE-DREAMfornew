package net.lab1024.sa.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * IOE-DREAM 网关服务启动类
 * <p>
 * 统一API网关服务，提供：
 * - 统一入口管理
 * - 路由转发
 * - 负载均衡
 * - 限流熔断
 * - 安全认证
 * - 监控日志
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    /**
     * 主方法 - 启动网关服务
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 设置Spring Boot应用名称
        System.setProperty("spring.application.name", "ioedream-gateway-service");

        SpringApplication.run(GatewayServiceApplication.class, args);

        System.out.println("===============================================");
        System.out.println("🚪 IOE-DREAM 网关服务启动成功！");
        System.out.println("🌐 服务功能: API网关、路由转发、负载均衡、安全认证");
        System.out.println("🎯 服务端口: 8080");
        System.out.println("📊 Actuator: http://localhost:8080/actuator");
        System.out.println("🔍 Routes: http://localhost:8080/actuator/gateway/routes");
        System.out.println("🛡️ 企业级统一API网关");
        System.out.println("===============================================");
    }

    /**
     * 自定义路由配置 - 编程式配置
     * 在application.yml基础上提供额外的动态路由能力
     *
     * @param builder 路由构建器
     * @return 路由定位器
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 健康检查路由
                .route("health-check", r -> r
                        .path("/health")
                        .filters(f -> f
                                .setPath("/actuator/health")
                                .rewritePath("/health", "/actuator/health"))
                        .uri("http://localhost:8080"))

                // 网关信息路由
                .route("gateway-info", r -> r
                        .path("/gateway")
                        .filters(f -> f
                                .setPath("/actuator/gateway")
                                .rewritePath("/gateway", "/actuator/gateway"))
                        .uri("http://localhost:8080"))

                // API版本路由 - v1版本
                .route("api-v1", r -> r
                        .path("/v1/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("API-Version", "v1"))
                        .uri("lb://ioedream-auth-service"))

                .build();
    }
}
