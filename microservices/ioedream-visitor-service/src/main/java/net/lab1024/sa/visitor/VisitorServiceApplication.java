package net.lab1024.sa.visitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 访客服务启动类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@SpringBootApplication(scanBasePackages = {
    "net.lab1024.sa.visitor",
    "net.lab1024.sa.common"
})
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("net.lab1024.sa.visitor.dao")
@ComponentScan(basePackages = {
    "net.lab1024.sa.visitor",
    "net.lab1024.sa.common"
})
public class VisitorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitorServiceApplication.class, args);
        System.out.println("=================================");
        System.out.println("🚀 IOE-DREAM 访客管理微服务启动成功！");
        System.out.println("Visitor Service Started Successfully!");
        System.out.println("=================================");
    }
}