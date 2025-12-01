package net.lab1024.sa.identity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 身份权限服务启动类
 * 基于IOE-DREAM现有HR模块重构的微服务
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@SpringBootApplication(scanBasePackages = { "net.lab1024.sa.identity", "net.lab1024.sa.common" })
@EnableDiscoveryClient
@EnableCaching
@MapperScan("net.lab1024.sa.identity.mapper")
@EnableTransactionManagement
@EnableScheduling
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
