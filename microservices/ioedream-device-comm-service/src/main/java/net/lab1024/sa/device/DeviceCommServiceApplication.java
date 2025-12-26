package net.lab1024.sa.device;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM 设备通讯服务启动类
 * <p>
 * 端口: 8087
 * 职责: 提供设备协议适配、连接管理、数据采集、指令下发等设备通讯API
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 正确配置包扫描路径（common包和device包）
 * - 正确配置MapperScan路径（包含common模块和device模块的DAO包）
 * </p>
 * <p>
 * 核心功能模块:
 * - 协议适配：多种设备协议适配（TCP/IP、串口、HTTP等）
 * - 连接管理：设备连接池、连接监控、断线重连
 * - 数据采集：设备数据采集、数据解析、数据存储
 * - 指令下发：设备控制指令、参数配置、固件升级
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        // 设备通讯服务自身包
        "net.lab1024.sa.device",
        "net.lab1024.sa.devicecomm",
        // 核心配置（必需）
        "net.lab1024.sa.common.config",
        // 响应和异常处理
        "net.lab1024.sa.common.response",
        "net.lab1024.sa.common.exception",
        // 工具类
        "net.lab1024.sa.common.util",
        // 安全认证
        "net.lab1024.sa.common.security",
        // 设备相关公共模块
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
    // Device模块DAO
    "net.lab1024.sa.device.dao"
})
public class DeviceCommServiceApplication {

    /**
     * 主启动方法
     * <p>
     * 启动IOE-DREAM设备通讯服务，端口8087
     * </p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DeviceCommServiceApplication.class, args);
    }
}

