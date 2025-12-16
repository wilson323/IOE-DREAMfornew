package net.lab1024.sa.attendance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IOE-DREAM 考勤管理服务启动类
 * <p>
 * 端口: 8091
 * 职责: 提供考勤打卡、排班管理、考勤统计、异常处理等业务API
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@SpringBootApplication注解
 * - 启用Nacos服务发现(@EnableDiscoveryClient)
 * - 正确配置包扫描路径（common包和attendance包）
 * - 正确配置MapperScan路径（包含common模块和attendance模块的DAO包）
 * </p>
 * <p>
 * 核心功能模块:
 * - 考勤打卡：GPS打卡、离线同步、生物识别打卡
 * - 排班管理：排班配置、班次管理、弹性时间
 * - 考勤统计：出勤统计、迟到早退、加班统计
 * - 异常处理：异常考勤、补签申请、审批流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootApplication(
    scanBasePackages = {
        // 考勤服务自身包
        "net.lab1024.sa.attendance",
        // 核心配置（必需）
        "net.lab1024.sa.common.config",
        // 响应和异常处理
        "net.lab1024.sa.common.response",
        "net.lab1024.sa.common.exception",
        // 工具类
        "net.lab1024.sa.common.util",
        // 安全认证
        "net.lab1024.sa.common.security",
        // 考勤相关公共模块
        "net.lab1024.sa.common.attendance",
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
    // Attendance模块DAO
    "net.lab1024.sa.attendance.dao"
})
public class AttendanceServiceApplication {

    /**
     * 主启动方法
     * <p>
     * 启动IOE-DREAM考勤管理服务，端口8091
     * </p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AttendanceServiceApplication.class, args);
    }
}


