package net.lab1024.sa.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * IOE-DREAM 企业服务启动类
 * 统一企业服务平台 - 整合OA办公自动化 + HR人力资源管理 + 文档管理
 *
 * 功能模块:
 * - OA: 流程审批、文档管理、会议管理、工作流
 * - HR: 员工管理、组织架构、薪酬福利、绩效考核
 * - File: 文件存储、上传下载、权限控制
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-30
 */
@SpringBootApplication(scanBasePackages = {
    "net.lab1024.sa.enterprise",
    "net.lab1024.sa.common"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
    "net.lab1024.sa.enterprise.client"
})
@EnableTransactionManagement
public class EnterpriseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseServiceApplication.class, args);
    }
}