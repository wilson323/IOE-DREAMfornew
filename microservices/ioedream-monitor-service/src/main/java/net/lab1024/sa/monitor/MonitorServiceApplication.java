package net.lab1024.sa.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 监控服务启动类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"net.lab1024.sa"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@MapperScan("net.lab1024.sa.monitor.dao")
public class MonitorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorServiceApplication.class, args);
        System.out.println("🎉 IOE-DREAM Monitor Service 启动成功！");
        System.out.println("📋 监控服务提供以下功能：");
        System.out.println("   • 实时访问监控");
        System.out.println("   • WebSocket实时通信");
        System.out.println("   • 系统性能监控");
        System.out.println("   • 历史数据清理");
        System.out.println("   • 监控数据统计");
        System.out.println("   • 告警通知管理");
        System.out.println("   • 监控报表生成");
        System.out.println("🚀 服务端口: 8097");
        System.out.println("🔗 WebSocket端口: 8099");
        System.out.println("📊 管理端点: 8098");
    }
}