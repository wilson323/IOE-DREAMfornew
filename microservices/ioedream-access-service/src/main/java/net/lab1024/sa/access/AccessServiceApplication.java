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
 * - 精确配置扫描路径（只扫描需要的公共包和access包）
 * - 正确配置MapperScan路径
 * </p>
 * <p>
 * 核心功能模块:
 * - 门禁控制：门禁设备管理、通行控制、多模态验证
 * - 通行记录：通行记录查询、统计分析、异常告警
 * - 权限管理：区域权限、时间权限、人员权限
 * - 区域管理：区域配置、区域关联、权限继承
 * </p>
 * <p>
 * <b>内存优化说明</b>:
 * scanBasePackages精确配置，只扫描门禁服务需要的公共包，
 * 减少不必要的类加载，优化内存使用。
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
        "net.lab1024.sa.common.rbac"
    },
    exclude = {
        HibernateJpaAutoConfiguration.class
    }
)
@EnableDiscoveryClient
@EnableScheduling
@MapperScan(basePackages = {
    // Common模块DAO
    "net.lab1024.sa.common.auth.dao",
    "net.lab1024.sa.common.rbac.dao",
    "net.lab1024.sa.common.system.employee.dao",
    "net.lab1024.sa.common.access.dao",
    "net.lab1024.sa.common.organization.dao",
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

