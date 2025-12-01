package net.lab1024.sa.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 统一报表分析服务启动类
 * 提供跨服务数据聚合分析、报表生成、业务洞察功能
 *
 * @author IOE-DREAM Team
 */
@SpringBootApplication(scanBasePackages = {"net.lab1024.sa.analytics", "net.lab1024.sa.base"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "net.lab1024.sa.analytics")
@EnableScheduling
public class AnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
        System.out.println("🚀 IOE-DREAM 统一报表分析服务启动成功！");
        System.out.println("📊 提供跨服务数据聚合分析、报表生成、业务洞察功能");
        System.out.println("📈 支持实时统计分析、历史数据查询、自定义报表");
        System.out.println("🎯 集成门禁、消费、考勤、视频、HR等业务数据");
    }
}