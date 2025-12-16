package net.lab1024.sa.visitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM 访客管理服务启动类
 * <p>
 * 端口: 8095
 * 职责: 提供访客预约、访客登记、访客通行、访客统计等业务API
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 正确配置包扫描路径（common包和visitor包）
 * - 正确配置MapperScan路径（包含common模块和visitor模块的DAO包）
 * </p>
 * <p>
 * 核心功能模块:
 * - 访客预约：在线预约、审批流程、预约管理
 * - 访客登记：现场登记、身份验证、人脸识别
 * - 访客通行：临时授权、门禁联动、轨迹追踪
 * - 访客统计：访问统计、数据分析、报表查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        // 访客服务自身包
        "net.lab1024.sa.visitor",
        // 核心配置（必需）
        "net.lab1024.sa.common.config",
        // 响应和异常处理
        "net.lab1024.sa.common.response",
        "net.lab1024.sa.common.exception",
        // 工具类
        "net.lab1024.sa.common.util",
        // 安全认证
        "net.lab1024.sa.common.security",
        // 访客相关公共模块
        "net.lab1024.sa.common.visitor",
        // 组织机构
        "net.lab1024.sa.common.organization",
        // RBAC权限
        "net.lab1024.sa.common.rbac",
        // 系统配置
        "net.lab1024.sa.common.system"
    },
    exclude = {
        HibernateJpaAutoConfiguration.class
    }
)
@EnableDiscoveryClient
@MapperScan(basePackages = {
    // Common模块DAO（18个包）
    "net.lab1024.sa.common.auth.dao",
    "net.lab1024.sa.common.rbac.dao",
    "net.lab1024.sa.common.system.employee.dao",
    "net.lab1024.sa.common.access.dao",
    "net.lab1024.sa.common.visitor.dao",
    "net.lab1024.sa.common.audit.dao",
    "net.lab1024.sa.common.monitor.dao",
    "net.lab1024.sa.common.config.dao",
    "net.lab1024.sa.common.menu.dao",
    "net.lab1024.sa.common.dict.dao",
    "net.lab1024.sa.common.organization.dao",
    "net.lab1024.sa.common.workflow.dao",
    "net.lab1024.sa.common.system.dao",
    "net.lab1024.sa.common.notification.dao",
    "net.lab1024.sa.common.scheduler.dao",
    // Visitor模块DAO
    "net.lab1024.sa.visitor.dao",
    "net.lab1024.sa.visitor.domain.dao"
})
public class VisitorServiceApplication {

    /**
     * 主启动方法
     * <p>
     * 启动IOE-DREAM访客管理服务，端口8095
     * </p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(VisitorServiceApplication.class, args);
    }
}

