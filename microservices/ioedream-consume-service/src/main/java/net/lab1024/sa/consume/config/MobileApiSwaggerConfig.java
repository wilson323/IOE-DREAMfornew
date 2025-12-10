package net.lab1024.sa.consume.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 移动端API Swagger配置
 * 移动端专用API文档配置
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class MobileApiSwaggerConfig {

    @Bean
    public OpenAPI mobileOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IOE-DREAM 移动端消费服务API")
                        .description("智慧园区一卡通管理平台 - 移动端消费服务接口文档<br/>" +
                                "专为移动端优化的消费相关API接口")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IOE-DREAM架构团队")
                                .email("architect@ioe-dream.com")
                                .url("https://www.ioe-dream.com"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8094")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.ioe-dream.com/consume")
                                .description("生产环境"),
                        new Server()
                                .url("https://test-api.ioe-dream.com/consume")
                                .description("测试环境")
                ));
    }
}