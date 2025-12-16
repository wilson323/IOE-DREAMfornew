package net.lab1024.sa.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM 公共业务服务启动类
 * <p>
 * 端口: 8088
 * 职责: 提供用户认证、权限管理、组织架构、字典管理、审计日志、通知管理等公共业务API
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 正确配置包扫描路径（common包和admin包）
 * - 正确配置MapperScan路径（包含所有common模块的DAO包）
 * </p>
 * <p>
 * 核心功能模块:
 * - 用户认证与授权 (auth)
 * - 组织架构管理 (organization)
 * - 权限管理 (security/rbac)
 * - 字典管理 (dict)
 * - 审计日志 (audit)
 * - 系统配置 (config)
 * - 通知管理 (notification)
 * - 任务调度 (scheduler)
 * - 监控告警 (monitor)
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        "net.lab1024.sa.common",
        "net.lab1024.sa.admin"
    },
    exclude = {
        HibernateJpaAutoConfiguration.class
    }
)
@EnableDiscoveryClient
@MapperScan(basePackages = {
    // microservices-common模块DAO
    "net.lab1024.sa.common.auth.dao",

    // microservices-common-business模块DAO
    "net.lab1024.sa.common.dict.dao",
    "net.lab1024.sa.common.menu.dao",
    "net.lab1024.sa.common.notification.dao",
    "net.lab1024.sa.common.organization.dao",
    "net.lab1024.sa.common.preference.dao",
    "net.lab1024.sa.common.scheduler.dao",
    "net.lab1024.sa.common.system.dao",
    "net.lab1024.sa.common.system.area.dao",
    "net.lab1024.sa.common.system.employee.dao",
    "net.lab1024.sa.common.theme.dao",
    "net.lab1024.sa.common.workflow.dao",

    // microservices-common-monitor模块DAO
    "net.lab1024.sa.common.monitor.dao",

    // microservices-common-security模块DAO
    "net.lab1024.sa.common.audit.dao",
    "net.lab1024.sa.common.security.auth.dao"
})
public class CommonServiceApplication {

    /**
     * 主启动方法
     * <p>
     * 启动IOE-DREAM公共业务服务，端口8088
     * </p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CommonServiceApplication.class, args);
    }
}
