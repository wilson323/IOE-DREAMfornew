package net.lab1024.sa.oa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM OA办公服务启动类
 * <p>
 * 端口: 8089
 * 职责: 提供文档管理、工作流引擎、企业信息管理等OA业务API
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 正确配置包扫描路径（common包和oa包）
 * - 正确配置MapperScan路径（包含common模块和oa模块的DAO包）
 * </p>
 * <p>
 * 核心功能模块:
 * - 文档管理：文档上传、下载、预览、版本管理
 * - 工作流引擎：流程定义、流程实例、任务管理
 * - 企业信息：企业信息管理、组织架构维护
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        "net.lab1024.sa.common",
        "net.lab1024.sa.oa"
    },
    exclude = {
        HibernateJpaAutoConfiguration.class
    }
)
@EnableDiscoveryClient
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
    // OA模块DAO
    "net.lab1024.sa.oa.dao"
})
public class OaServiceApplication {

    /**
     * 主启动方法
     * <p>
     * 启动IOE-DREAM OA办公服务，端口8089
     * </p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OaServiceApplication.class, args);
    }
}
