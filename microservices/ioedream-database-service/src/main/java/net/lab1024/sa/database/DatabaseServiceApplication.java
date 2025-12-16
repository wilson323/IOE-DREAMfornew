package net.lab1024.sa.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 数据库初始化管理服务
 * <p>
 * 核心职责：
 * - 统一管理所有数据库表结构初始化
 * - 自动检测并同步数据库变更
 * - 管理初始化数据的导入和更新
 * - 提供数据库版本控制和回滚功能
 * </p>
 * <p>
 * 技术特性：
 * - Spring Boot 3.5.8 + Spring Cloud 2025.0.0
 * - Flyway 数据库版本管理
 * - MyBatis-Plus 代码生成
 * - Druid 连接池监控
 * - 多数据库支持
 * </p>
 * <p>
 * 架构合规性：
 * - 已移除@EnableFeignClients（架构合规化）
 * - 如需调用其他服务，使用GatewayServiceClient
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.1
 * @since 2025-12-08
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {
    "net.lab1024.sa.database",
    "net.lab1024.sa.common"
})
@EnableDiscoveryClient
@EnableScheduling
public class DatabaseServiceApplication {

    public static void main(String[] args) {
        log.info("🚀 IOE-DREAM 数据库初始化管理服务启动中...");
        log.info("📊 统一数据库表结构和初始化数据管理服务 v1.0.0");

        SpringApplication.run(DatabaseServiceApplication.class, args);

        log.info("✅ IOE-DREAM 数据库初始化管理服务启动完成");
        log.info("🔧 支持功能：数据库初始化、表结构同步、版本管理、数据导入");
    }
}
