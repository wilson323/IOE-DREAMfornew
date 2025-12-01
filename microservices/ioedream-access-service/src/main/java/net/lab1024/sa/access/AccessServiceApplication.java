package net.lab1024.sa.access;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * IOE-DREAM 访问控制微服务启动类
 * 基于现有access模块提取
 *
 * @author IOE-DREAM Team
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "net.lab1024.sa.access.feign")
public class AccessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessServiceApplication.class, args);
        System.out.println("🚀 IOE-DREAM 访问控制微服务启动成功！");
    }
}