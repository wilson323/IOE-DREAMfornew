package net.lab1024.sa.base.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 门禁系统Swagger配置
 *
 * @author SmartAdmin Team
 * @date 2025/01/13
 */
@Configuration
public class AccessSwaggerConfig {

    /**
     * 门禁系统API配置
     */
    @Bean("accessOpenAPI")
    public OpenAPI accessOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartAdmin 门禁管理系统 API")
                        .description("企业级门禁管理系统接口文档，包含设备管理、权限管理、访问记录、报警处理等功能")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SmartAdmin Team")
                                .email("smartadmin@lab1024.com")
                                .url("https://smartadmin.vip"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:1024")
                                .description("开发环境"),
                        new Server()
                                .url("https://api.smartadmin.dev")
                                .description("测试环境"),
                        new Server()
                                .url("https://api.smartadmin.prod")
                                .description("生产环境")
                ));
    }
}