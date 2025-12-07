package net.lab1024.sa.common.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI 3.0 统一配置类
 * <p>
 * 为所有微服务提供统一的API文档配置
 * 支持Knife4j UI界面展示
 * 严格遵循IOE-DREAM架构规范
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:ioedream-service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private Integer serverPort;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 创建OpenAPI配置
     *
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .components(createComponents())
                .addSecurityItem(createSecurityRequirement());
    }

    /**
     * 创建API信息
     *
     * @return API信息对象
     */
    private Info createApiInfo() {
        return new Info()
                .title("IOE-DREAM 智慧园区一卡通管理平台 API文档")
                .version("1.0.0")
                .description("""
                        IOE-DREAM智慧园区一卡通管理平台API文档

                        ## 核心功能模块
                        - 考勤管理：GPS打卡、离线同步、考勤统计
                        - 门禁管理：多模态验证、权限控制、通行记录
                        - 访客管理：预约管理、签到签退、访客统计
                        - 消费管理：快速消费、账户管理、消费统计
                        - 视频监控：实时监控、录像回放、智能分析

                        ## 技术栈
                        - Spring Boot 3.5.8
                        - Spring Cloud 2023.0.0
                        - Sa-Token + JWT认证
                        - MyBatis-Plus
                        - Redis缓存
                        - MySQL数据库

                        ## 安全说明
                        - 所有接口需要Bearer Token认证
                        - Token通过登录接口获取
                        - Token有效期：30天
                        """)
                .contact(new Contact()
                        .name("IOE-DREAM Team")
                        .email("support@ioe-dream.com")
                        .url("https://ioe-dream.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    /**
     * 创建服务器列表
     *
     * @return 服务器列表
     */
    private List<Server> createServers() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("开发环境 - " + applicationName);

        Server testServer = new Server()
                .url("https://test-api.ioe-dream.com")
                .description("测试环境");

        Server prodServer = new Server()
                .url("https://api.ioe-dream.com")
                .description("生产环境");

        return Arrays.asList(devServer, testServer, prodServer);
    }

    /**
     * 创建安全组件配置
     *
     * @return 安全组件对象
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Token认证，格式：Bearer {token}"));
    }

    /**
     * 创建安全要求
     *
     * @return 安全要求对象
     */
    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList("BearerAuth");
    }
}

