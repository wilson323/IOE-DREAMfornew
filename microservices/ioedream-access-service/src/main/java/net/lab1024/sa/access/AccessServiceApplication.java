package net.lab1024.sa.access;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM 门禁管理服务启动类
 * <p>
 * 端口: 8090
 * 职责: 提供门禁控制、通行记录、权限管理等业务API
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 正确配置包扫描路径（common包和access包）
 * - 正确配置MapperScan路径（包含common模块和access模块的DAO包）
 * </p>
 * <p>
 * 核心功能模块:
 * - 门禁验证：双模式验证（设备端验证+后台验证）
 * - 通行记录：记录管理、批量上传、离线补录
 * - 权限管理：权限同步、权限变更监听
 * - 区域管理：区域信息、人员权限、区域监控
 * - 设备管理：设备状态、设备控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        // 门禁服务自身包
        "net.lab1024.sa.access",
        // 核心配置（必需）
        "net.lab1024.sa.common.config",
        // 响应和异常处理
        "net.lab1024.sa.common.response",
        "net.lab1024.sa.common.exception",
        // 工具类
        "net.lab1024.sa.common.util",
        // 安全认证
        "net.lab1024.sa.common.security",
        // 门禁相关公共模块
        "net.lab1024.sa.common.access",
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
    // Common模块DAO
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
