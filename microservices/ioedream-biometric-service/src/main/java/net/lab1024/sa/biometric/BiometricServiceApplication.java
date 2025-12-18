package net.lab1024.sa.biometric;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IOE-DREAM 生物模板管理服务启动类
 * <p>
 * 端口: 8096
 * 职责: 提供生物特征模板管理、特征提取、设备同步等功能
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 精确配置扫描路径(只扫描需要的公共包和biometric包)
 * - 正确配置MapperScan路径
 * </p>
 * <p>
 * 核心功能模块:
 * - 模板管理: 生物特征模板CRUD、版本管理
 * - 特征提取: 从用户上传照片提取512维特征向量
 * - 设备同步: 智能下发模板到相关门禁设备
 * - 权限联动: 根据用户权限自动同步模板
 * </p>
 * <p>
 * <b>重要说明</b>:
 * ⚠️ 该服务只管理模板数据，不负责实时识别
 * ⚠️ 实时识别由设备端完成，软件端只处理模板存储和下发
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@SpringBootApplication(
    scanBasePackages = {
        // 生物识别服务自身包
        "net.lab1024.sa.biometric",
        // 核心配置(必需)
        "net.lab1024.sa.common.config",
        // 响应和异常处理
        "net.lab1024.sa.common.response",
        "net.lab1024.sa.common.exception",
        // 工具类
        "net.lab1024.sa.common.util",
        // 安全认证
        "net.lab1024.sa.common.security",
        // 组织架构
        "net.lab1024.sa.common.organization"
    },
    exclude = {
        HibernateJpaAutoConfiguration.class
    }
)
@EnableDiscoveryClient
@EnableScheduling
@EnableAsync
@MapperScan(basePackages = {
    // Common模块DAO
    "net.lab1024.sa.common.auth.dao",
    "net.lab1024.sa.common.organization.dao",
    "net.lab1024.sa.common.system.employee.dao",
    // Biometric模块DAO
    "net.lab1024.sa.biometric.dao"
})
public class BiometricServiceApplication {

    /**
     * 主启动方法
     * <p>
     * 启动IOE-DREAM生物模板管理服务,端口8096
     * </p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BiometricServiceApplication.class, args);
    }
}
