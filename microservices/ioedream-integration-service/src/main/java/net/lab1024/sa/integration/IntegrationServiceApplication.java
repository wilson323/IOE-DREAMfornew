package net.lab1024.sa.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// 使用Nacos作为注册中心，移除Eureka依赖
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * IOE-DREAM 集成服务启动类
 * <p>
 * 第三方系统集成服务，提供：
 * - 第三方系统集成
 * - API接口适配
 * - 数据格式转换
 * - 协议转换
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@SpringBootApplication
// @EnableEurekaClient - 使用Nacos替代Eureka
@EnableDiscoveryClient
@EnableAsync
public class IntegrationServiceApplication {

    public static void main(String[] args) {
        System.setProperty("spring.application.name", "ioedream-integration-service");
        SpringApplication.run(IntegrationServiceApplication.class, args);

        System.out.println("===============================================");
        System.out.println("🔗 IOE-DREAM 集成服务启动成功！");
        System.out.println("🌐 服务功能: 第三方集成、API适配、数据转换");
        System.out.println("🎯 服务端口: 8027");
        System.out.println("🔌 企业级系统集成平台");
        System.out.println("===============================================");
    }
}