package net.lab1024.sa.oa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * OA办公服务启动类
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@SpringBootApplication(scanBasePackages = {"net.lab1024.sa.base", "net.lab1024.sa.oa"})
@EnableFeignClients
public class OaApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaApplication.class, args);
    }
}