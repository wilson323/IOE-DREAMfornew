package net.lab1024.sa.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 身份权限服务启动类
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@SpringBootApplication(scanBasePackages = {"net.lab1024.sa", "net.lab1024.base"})
@EnableDiscoveryClient
@EnableCaching
@EnableTransactionManagement
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}