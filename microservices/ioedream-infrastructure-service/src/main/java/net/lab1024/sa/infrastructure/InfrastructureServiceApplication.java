package net.lab1024.sa.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 基础设施服务启动类
 * 统一基础设施服务 - 整合配置中心 + 日志服务 + 调度服务 + 第三方集成 + 基础监控
 *
 * 功能模块:
 * - Config: 统一配置管理、动态更新、环境隔离
 * - Logging: 企业级日志收集分析、日志查询
 * - Scheduler: 任务调度、定时任务、作业管理
 * - Integration: 第三方系统集成、外部API调用
 * - Monitor: 基础监控、指标收集、健康检查
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-30
 */
@SpringBootApplication(scanBasePackages = {
    "net.lab1024.sa.infrastructure",
    "net.lab1024.sa.common"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
    "net.lab1024.sa.infrastructure.client"
})
@EnableScheduling
@EnableAsync
public class InfrastructureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfrastructureServiceApplication.class, args);
    }
}