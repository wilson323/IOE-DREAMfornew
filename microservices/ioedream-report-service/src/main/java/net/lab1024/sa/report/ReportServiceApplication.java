package net.lab1024.sa.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 报表服务启动类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = { "net.lab1024.sa.report", "net.lab1024.sa.common" })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "net.lab1024.sa.report")
@EnableScheduling
@MapperScan("net.lab1024.sa.report.dao")
public class ReportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
        System.out.println("🎉 IOE-DREAM Report Service 启动成功！");
        System.out.println("📋 报表服务提供以下功能：");
        System.out.println("   • 数据统计分析");
        System.out.println("   • 图表生成");
        System.out.println("   • Excel报表导出");
        System.out.println("   • PDF报告生成");
        System.out.println("   • 实时报表");
        System.out.println("   • 自定义报表模板");
        System.out.println("   • 定时报表生成");
        System.out.println("   • 报表权限管理");
        System.out.println("🚀 服务端口: 8092");
    }
}