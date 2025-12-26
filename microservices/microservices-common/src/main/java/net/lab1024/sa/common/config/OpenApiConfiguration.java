package net.lab1024.sa.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 配置类
 * <p>
 * 统一配置API文档生成，包含：
 * - API基本信息
 * - 服务器地址配置
 * - 联系方式和许可证信息
 * - 外部文档链接
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${spring.application.name:ioedream-service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 配置OpenAPI 3.0文档
     *
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .components(createComponents())
                .security(List.of(new SecurityRequirement().addList("JWT")))
                .externalDocs(externalDocumentation());
    }

    /**
     * 创建组件配置
     *
     * @return 组件配置对象
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("JWT", createJWTScheme())
                .addSecuritySchemes("RefreshToken", createRefreshTokenScheme());
    }

    /**
     * 创建JWT认证方案
     *
     * @return JWT安全方案
     */
    private SecurityScheme createJWTScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT访问令牌，格式：Bearer {token}\n\n" +
                        "获取方式：\n" +
                        "1. 调用 /api/v1/auth/login 登录获取token\n" +
                        "2. 在请求头中添加：Authorization: Bearer {token}");
    }

    /**
     * 创建刷新令牌方案
     *
     * @return 刷新令牌安全方案
     */
    private SecurityScheme createRefreshTokenScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Refresh-Token")
                .description("刷新令牌，用于延长会话有效期\n\n" +
                        "使用场景：\n" +
                        "1. 当访问令牌即将过期时使用\n" +
                        "2. 在请求头中添加：Refresh-Token: {refresh_token}");
    }

    /**
     * API基本信息配置
     *
     * @return API信息对象
     */
    private Info apiInfo() {
        return new Info()
                .title("IOE-DREAM 智慧园区一卡通管理平台 API")
                .description("企业级智慧安防管理平台，集成多模态生物识别、一卡通管理、智能门禁等核心功能。"
                        + "\n\n## 核心功能"
                        + "\n- 🔐 **多模态生物识别**: 人脸、指纹、掌纹、虹膜识别"
                        + "\n- 🚪 **智能门禁控制**: 边缘验证模式，支持多种认证方式"
                        + "\n- ⏰ **考勤管理**: 边缘识别+中心计算，支持复杂排班规则"
                        + "\n- 💳 **消费管理**: 中心实时验证，支持离线降级模式"
                        + "\n- 👥 **访客管理**: 混合验证模式，完整访问轨迹记录"
                        + "\n- 📹 **视频监控**: 边缘AI计算，智能分析和告警"
                        + "\n- 📊 **数据分析**: 实时监控、统计分析、报表生成"
                        + "\n\n## 技术特性"
                        + "\n- **微服务架构**: Spring Boot 3.5.8 + Spring Cloud 2025.0.0"
                        + "\n- **高并发支持**: 多级缓存 + 异步处理"
                        + "\n- **安全合规**: 三级等保标准，金融级安全防护"
                        + "\n- **边缘计算**: 设备端智能处理，降低服务器压力"
                        + "\n- **实时监控**: 分布式链路追踪 + 性能监控")
                .version(getApiVersion())
                .contact(new Contact()
                        .name("IOE-DREAM架构团队")
                        .email("support@ioedream.com")
                        .url("https://github.com/IOE-DREAM"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 服务器地址配置
     *
     * @return 服务器列表
     */
    private List<Server> serverList() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("开发环境服务器");

        Server testServer = new Server()
                .url("http://test.ioedream.com")
                .description("测试环境服务器");

        Server prodServer = new Server()
                .url("https://api.ioedream.com")
                .description("生产环境服务器");

        return List.of(devServer, testServer, prodServer);
    }

    /**
     * 外部文档配置
     *
     * @return 外部文档对象
     */
    private ExternalDocumentation externalDocumentation() {
        return new ExternalDocumentation()
                .description("IOE-DREAM 项目文档")
                .url("https://docs.ioedream.com");
    }

    /**
     * 获取API版本号
     *
     * @return API版本
     */
    private String getApiVersion() {
        // 根据环境动态返回版本号
        return switch (activeProfile) {
            case "prod" -> "v1.0.0";
            case "test" -> "v1.0.0-rc.1";
            default -> "v1.0.0-snapshot";
        };
    }
}