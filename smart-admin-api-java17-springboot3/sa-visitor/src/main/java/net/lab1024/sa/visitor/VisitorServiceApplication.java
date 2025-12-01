package net.lab1024.sa.visitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * IOE-DREAM 访客管理微服务启动类
 *
 * 基于现有项目架构的微服务化改造
 * 从access模块中提取访客管理功能，建立独立的微服务
 *
 * @author IOE-DREAM Team
 * @version 3.0.0
 * @since 2025-11-27
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "net.lab1024.sa.visitor")
@EnableDiscoveryClient
@EnableFeignClients
@EnableTransactionManagement(proxyTargetClass = true)
public class VisitorServiceApplication {

    public static void main(String[] args) {
        log.info("Starting IOE-DREAM Visitor Service...");
        SpringApplication.run(VisitorServiceApplication.class, args);
        log.info("Visitor Service started successfully!");
    }
}