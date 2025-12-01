package net.lab1024.sa.attendance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 考勤微服务启动类
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@SpringBootApplication(scanBasePackages = {
    "net.lab1024.sa.attendance",
    "net.lab1024.sa.common"
})
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("net.lab1024.sa.attendance.dao")
@ComponentScan(basePackages = {
    "net.lab1024.sa.attendance",
    "net.lab1024.sa.common"
})
public class AttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceApplication.class, args);
        System.out.println("=================================");
        System.out.println("🚀 IOE-DREAM 考勤管理微服务启动成功！");
        System.out.println("Attendance Service Started Successfully!");
        System.out.println("=================================");
    }
}