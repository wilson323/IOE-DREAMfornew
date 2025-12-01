package net.lab1024.sa.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * IOE-DREAM 配置中心服务启动类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"net.lab1024.sa"})
@EnableConfigServer
@EnableDiscoveryClient
@EnableFeignClients
public class ConfigServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
        System.out.println("🎉 IOE-DREAM Config Service 启动成功！");
        System.out.println("📋 配置中心服务提供以下功能：");
        System.out.println("   • 统一配置管理");
        System.out.println("   • 动态配置更新");
        System.out.println("   • 环境配置隔离");
        System.out.println("   • 配置版本管理");
        System.out.println("   • 配置安全控制");
        System.out.println("   • 实时配置推送");
        System.out.println("   • 配置变更审计");
        System.out.println("🚀 服务端口: 8888");
        System.out.println("🔗 配置端点: /{application}/{profile}[/{label}]");
    }
}