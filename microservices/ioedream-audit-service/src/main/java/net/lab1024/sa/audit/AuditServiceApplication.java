package net.lab1024.sa.audit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 审计服务启动类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"net.lab1024.sa"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@MapperScan("net.lab1024.sa.audit.dao")
public class AuditServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
        System.out.println("🎉 IOE-DREAM Audit Service 启动成功！");
        System.out.println("📋 审计服务提供以下功能：");
        System.out.println("   • 操作审计记录");
        System.out.println("   • 数据变更追踪");
        System.out.println("   • 用户行为分析");
        System.out.println("   • 合规报告生成");
        System.out.println("   • 安全事件记录");
        System.out.println("   • 审计日志管理");
        System.out.println("   • 数据完整性检查");
        System.out.println("🚀 服务端口: 8096");
        System.out.println("🔗 审计端点: /api/audit/**");
    }
}