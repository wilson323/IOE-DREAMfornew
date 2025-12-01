package net.lab1024.sa.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 设备管理服务启动类
 * 基于现有设备管理模块重构的微服务
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@SpringBootApplication(scanBasePackages = { "net.lab1024.sa.device", "net.lab1024.sa.common" })
@EnableDiscoveryClient
@EnableCaching
@EnableJpaRepositories
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class DeviceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceServiceApplication.class, args);
    }
}
