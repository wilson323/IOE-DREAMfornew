package net.lab1024.sa.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 调度服务启动类
 * <p>
 * 任务调度服务，提供：
 * - 定时任务管理
 * - 作业调度
 * - 任务监控
 * - 分布式调度
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class SchedulerServiceApplication {

    public static void main(String[] args) {
        System.setProperty("spring.application.name", "ioedream-scheduler-service");
        SpringApplication.run(SchedulerServiceApplication.class, args);

        System.out.println("===============================================");
        System.out.println("⏰ IOE-DREAM 调度服务启动成功！");
        System.out.println("📋 服务功能: 定时任务、作业调度、任务监控");
        System.out.println("🎯 服务端口: 8026");
        System.out.println("🔄 企业级任务调度平台");
        System.out.println("===============================================");
    }
}