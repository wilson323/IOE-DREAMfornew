package net.lab1024.sa.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 系统服务测试启动类
 * 用于接口验证测试
 */
@SpringBootApplication(scanBasePackages = "net.lab1024.sa.system")
public class SystemServiceTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemServiceTestApplication.class, args);
    }
}