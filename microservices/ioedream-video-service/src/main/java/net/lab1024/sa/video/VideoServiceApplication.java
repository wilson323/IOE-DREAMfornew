package net.lab1024.sa.video;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM 视频监控服务启动类
 * <p>
 * 端口: 8092
 * 职责: 提供视频监控、录像回放、智能分析、设备管理等业务API
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 正确配置包扫描路径（common包和video包）
 * - 正确配置MapperScan路径（包含common模块和video模块的DAO包）
 * </p>
 * <p>
 * 核心功能模块:
 * - 视频监控：实时监控、设备管理、通道配置
 * - 录像回放：录像查询、回放控制、下载导出
 * - 智能分析：人脸识别、行为分析、异常检测
 * - 设备管理：摄像头管理、设备状态、告警管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        // 视频服务自身包
        "net.lab1024.sa.video",
        // 核心配置（必需）
        "net.lab1024.sa.common.config",
        // 响应和异常处理
        "net.lab1024.sa.common.response",
        "net.lab1024.sa.common.exception",
        // 工具类
        "net.lab1024.sa.common.util",
        // 安全认证
        "net.lab1024.sa.common.security",
        // 设备相关（视频服务需要设备管理）
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
    // Video模块DAO
    "net.lab1024.sa.video.dao"
})
public class VideoServiceApplication {

    /**
     * 主启动方法
     * <p>
     * 启动IOE-DREAM视频监控服务，端口8092
     * </p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(VideoServiceApplication.class, args);
    }
}
