package net.lab1024.sa.access;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 门禁管理服务启动类
 * <p>
 * 端口: 8090
 * 职责: 提供门禁控制、通行记录、权限管理、区域管理等业务API
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 正确配置包扫描路径（common包和access包）
 * - 正确配置MapperScan路径（包含common模块和access模块的DAO包）
 * </p>
 * <p>
 * 核心功能模块:
 * - 门禁控制：门禁设备管理、通行控制、多模态验证
 * - 通行记录：通行记录查询、统计分析、异常告警
 * - 权限管理：区域权限、时间权限、人员权限
 * - 区域管理：区域配置、区域关联、权限继承
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        "net.lab1024.sa.common",
        "net.lab1024.sa.access"
    },
    exclude = {
        HibernateJpaAutoConfiguration.class
    }
)
@EnableDiscoveryClient
@EnableScheduling
@MapperScan(basePackages = {
    // Common模块DAO（18个包）
    "net.lab1024.sa.common.auth.dao",
    "net.lab1024.sa.common.security.dao",
    "net.lab1024.sa.common.hr.dao",
    "net.lab1024.sa.common.system.employee.dao",
    "net.lab1024.sa.common.access.dao",
    "net.lab1024.sa.common.visitor.dao",
    "net.lab1024.sa.common.audit.dao",
    "net.lab1024.sa.common.monitor.dao",
    "net.lab1024.sa.common.config.dao",
    "net.lab1024.sa.common.document.dao",
    "net.lab1024.sa.common.file.dao",
    "net.lab1024.sa.common.menu.dao",
    "net.lab1024.sa.common.dict.dao",
    "net.lab1024.sa.common.organization.dao",
    "net.lab1024.sa.common.workflow.dao",
    "net.lab1024.sa.common.system.dao",
    "net.lab1024.sa.common.notification.dao",
    "net.lab1024.sa.common.scheduler.dao",
    // Access模块DAO
    "net.lab1024.sa.access.dao"
})
public class AccessServiceApplication {

    /**
     * 主启动方法
     * <p>
     * 启动IOE-DREAM门禁管理服务，端口8090
     * </p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AccessServiceApplication.class, args);
    }
}
