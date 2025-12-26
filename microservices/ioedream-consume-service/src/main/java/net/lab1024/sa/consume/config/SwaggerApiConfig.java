package net.lab1024.sa.consume.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Swagger API文档配置
 *
 * 功能说明：
 * 1. 自动生成REST API文档
 * 2. 支持在线测试API
 * 3. 提供完整的接口说明
 *
 * 访问地址：
 * - Swagger UI: http://localhost:8094/swagger-ui.html
 * - API Docs: http://localhost:8094/api-docs
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Configuration
public class SwaggerApiConfig {

    /**
     * OpenAPI基础信息配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IOE-DREAM 消费服务 API文档")
                        .description("智慧园区消费管理系统RESTful API文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("IOE-DREAM架构团队")
                                .email("support@ioe-dream.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new io.swagger.v3.oas.models.Components()
                        .schemas(getSchemas())
                        .responses(getCommonResponses()));
    }

    /**
     * 消费服务API分组
     */
    @Bean
    public GroupedOpenApi consumeApi() {
        return GroupedOpenApi.builder()
                .group("消费服务")
                .pathsToMatch("/api/v1/consume/**")
                .build();
    }

    /**
     * 公共Schema定义
     */
    private Map<String, Schema> getSchemas() {
        Map<String, Schema> schemas = new HashMap<>();

        // ResponseDTO
        schemas.put("ResponseDTO", new Schema<>()
                .type("object")
                .description("统一响应对象")
                .addProperty("code", new Schema<>().type("integer").description("业务状态码"))
                .addProperty("message", new Schema<>().type("string").description("提示信息"))
                .addProperty("data", new Schema<>().description("响应数据"))
                .addProperty("timestamp", new Schema<>().type("integer").format("int64").description("时间戳")));

        // PageResult
        schemas.put("PageResult", new Schema<>()
                .type("object")
                .description("分页结果对象")
                .addProperty("list", new Schema<>().type("array").description("数据列表"))
                .addProperty("total", new Schema<>().type("integer").format("int64").description("总记录数"))
                .addProperty("pageNum", new Schema<>().type("integer").description("当前页码"))
                .addProperty("pageSize", new Schema<>().type("integer").description("每页大小"))
                .addProperty("pages", new Schema<>().type("integer").description("总页数")));

        return schemas;
    }

    /**
     * 通用响应定义
     */
    private Map<String, ApiResponse> getCommonResponses() {
        Map<String, ApiResponse> responses = new HashMap<>();

        // 成功响应
        responses.put("200", new ApiResponse()
                .description("操作成功")
                .content(new Content<>()
                        .addMediaType("application/json",
                                new MediaType<>().schema(new Schema<>().$ref("#/components/schemas/ResponseDTO")))));

        // 参数错误
        responses.put("400", new ApiResponse()
                .description("参数错误")
                .content(new Content<>()
                        .addMediaType("application/json",
                                new MediaType<>().schema(new Schema<>().$ref("#/components/schemas/ResponseDTO")))));

        // 未授权
        responses.put("401", new ApiResponse()
                .description("未授权")
                .content(new Content<>()
                        .addMediaType("application/json",
                                new MediaType<>().schema(new Schema<>().$ref("#/components/schemas/ResponseDTO")))));

        // 禁止访问
        responses.put("403", new ApiResponse()
                .description("禁止访问")
                .content(new Content<>()
                        .addMediaType("application/json",
                                new MediaType<>().schema(new Schema<>().$ref("#/components/schemas/ResponseDTO")))));

        // 资源不存在
        responses.put("404", new ApiResponse()
                .description("资源不存在")
                .content(new Content<>()
                        .addMediaType("application/json",
                                new MediaType<>().schema(new Schema<>().$ref("#/components/schemas/ResponseDTO")))));

        // 服务器错误
        responses.put("500", new ApiResponse()
                .description("服务器错误")
                .content(new Content<>()
                        .addMediaType("application/json",
                                new MediaType<>().schema(new Schema<>().$ref("#/components/schemas/ResponseDTO")))));

        return responses;
    }
}
