# Swagger/OpenAPI 配置指南

## 📋 配置概述

IOE-DREAM项目使用Swagger 3.0 (OpenAPI 3)自动生成API文档，提供交互式API测试界面。

---

## 🚀 快速开始

### 访问Swagger UI

**本地环境**:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API文档: http://localhost:8080/v3/api-docs

**测试环境**:
- Swagger UI: https://test.ioe-dream.com/swagger-ui.html
- API文档: https://test.ioe-dream.com/v3/api-docs

**生产环境**:
- Swagger UI: https://api.ioe-dream.com/swagger-ui.html
- API文档: https://api.ioe-dream.com/v3/api-docs

---

## ⚙️ 核心配置

### 1. Maven依赖配置

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 2. 基础配置类

```java
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "IOE-DREAM API文档",
        version = "2.0.0",
        description = "IOE-DREAM智能管理系统API接口文档",
        contact = @Contact(
            name = "IOE-DREAM技术团队",
            email = "support@ioe-dream.com",
            url = "https://www.ioe-dream.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "开发环境"),
        @Server(url = "https://test.ioe-dream.com", description = "测试环境"),
        @Server(url = "https://api.ioe-dream.com", description = "生产环境")
    },
    security = {
        @SecurityRequirement(name = "bearerAuth")
    }
)
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(new Info()
                        .title("IOE-DREAM API文档")
                        .version("2.0.0")
                        .description("智能门禁、考勤、消费、视频监控一体化管理平台"));
    }
}
```

### 3. 应用配置

```yaml
# application.yml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    try-it-out-enabled: true
    filter: true
    tags-sorter: alpha
    operations-sorter: method
  show-actuator: true
  default-consumes-media-type: application/json
  default-produces-media-type: application/json

# 禁用环境（生产环境可选择性禁用）
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

---

## 📚 标注使用指南

### Controller层注解

#### 1. 基本注解

```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户信息的增删改查操作")
@PermissionCheck(value = "USER_MANAGE", description = "用户管理权限")
public class UserController {

    @Operation(summary = "获取用户列表", description = "分页查询用户信息列表")
    @GetMapping("/query")
    public ResponseDTO<PageResult<UserVO>> queryUsers(UserQueryForm queryForm) {
        // 业务逻辑
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID获取详细信息")
    @Parameter(name = "userId", description = "用户ID", required = true, example = "1001")
    @GetMapping("/{userId}")
    public ResponseDTO<UserVO> getUserDetail(@PathVariable Long userId) {
        // 业务逻辑
    }

    @Operation(summary = "创建用户", description = "创建新的用户信息")
    @PostMapping("/add")
    public ResponseDTO<Long> addUser(@Valid @RequestBody UserAddForm addForm) {
        // 业务逻辑
    }
}
```

#### 2. 响应模型注解

```java
@Data
@Schema(description = "用户信息视图对象")
public class UserVO {

    @Schema(description = "用户ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "邮箱地址", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号码", example = "13800138000", pattern = "^1[3-9]\\d{9}$")
    private String phone;

    @Schema(description = "用户状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "LOCKED"})
    private String status;

    @Schema(description = "创建时间", example = "2025-12-21T14:30:00")
    private LocalDateTime createTime;
}
```

#### 3. 请求参数注解

```java
@Data
@Schema(description = "用户查询表单")
public class UserQueryForm extends BaseQueryForm {

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "邮箱地址", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号码", example = "13800138000")
    private String phone;

    @Schema(description = "用户状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "LOCKED"})
    private String status;

    @Schema(description = "部门ID", example = "100")
    private Long deptId;
}
```

---

## 🔧 高级配置

### 1. 多环境配置

```java
@Profile("dev")
@Configuration
public class SwaggerDevConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("公共接口")
                .pathsToMatch("/api/v1/public/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理接口")
                .pathsToMatch("/api/v1/admin/**")
                .build();
    }
}

@Profile("prod")
@Configuration
public class SwaggerProdConfig {
    // 生产环境可以配置更严格的API文档
}
```

### 2. 安全配置

```java
@Configuration
public class SwaggerSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }
}
```

### 3. 自定义文档生成

```java
@Component
public class CustomOpenApiBuilder {

    public OpenAPI buildOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IOE-DREAM API")
                        .version("2.0.0")
                        .description("智能门禁、考勤、消费、视频监控一体化平台"))
                .externalDocs(new ExternalDocumentation()
                        .description("IOE-DREAM 文档中心")
                        .url("https://docs.ioe-dream.com"))
                .components(new Components()
                        .addParameters("userId",
                                new Parameter()
                                        .in("path")
                                        .name("userId")
                                        .description("用户ID")
                                        .required(true)
                                        .schema(new Schema()
                                                .type("integer")
                                                .format("int64"))));
    }
}
```

---

## 📝 注解详细说明

### 核心注解列表

#### 1. 类级别注解

| 注解 | 说明 | 使用位置 |
|------|------|---------|
| `@Tag` | API标签，用于分组 | Controller类 |
| `@RestController` | REST控制器 | Controller类 |
| `@RequestMapping` | 请求路径映射 | Controller类 |
| `@PermissionCheck` | 权限检查注解 | Controller方法/类 |

#### 2. 方法级别注解

| 注解 | 说明 | 使用位置 |
|------|------|---------|
| `@Operation` | API操作描述 | Controller方法 |
| `@Parameter` | 参数描述 | 方法参数 |
| `@Parameters` | 多参数描述 | 方法 |
| `@ApiResponse` | 响应描述 | 方法 |
| `@ApiResponses` | 多响应描述 | 方法 |

#### 3. 模型注解

| 注解 | 说明 | 使用位置 |
|------|------|---------|
| `@Schema` | 模型描述 | 类、字段 |
| `@ArraySchema` | 数组模型描述 | 字段 |
| `@Hidden` | 隐藏字段 | 字段、方法 |

### 注解属性详解

#### @Operation 属性

```java
@Operation(
    summary = "操作简短描述",
    description = "操作的详细描述",
    operationId = "uniqueOperationId",  // 唯一操作ID
    tags = {"用户管理"},               // 标签分组
    parameters = {                      // 参数列表
        @Parameter(name = "id", description = "用户ID", required = true)
    },
    responses = {                       // 响应列表
        @ApiResponse(responseCode = "200", description = "成功",
                     content = @Content(schema = @Schema(implementation = UserVO.class))),
        @ApiResponse(responseCode = "400", description = "参数错误")
    },
    deprecated = false,                 // 是否已废弃
    hidden = false                      // 是否隐藏
)
```

#### @Schema 属性

```java
@Schema(
    description = "字段描述",
    example = "示例值",
    required = true,                     // 是否必填
    nullable = false,                    // 是否可为空
    readOnly = false,                    // 是否只读
    writeOnly = false,                   // 是否只写
    minLength = 1,                       // 最小长度
    maxLength = 100,                     // 最大长度
    pattern = "^[a-zA-Z0-9_]+$",        // 正则表达式
    allowableValues = {"ACTIVE", "INACTIVE"}, // 允许的值
    implementation = UserEntity.class,    // 实现类
    type = "string",                     // 数据类型
    format = "date-time"                 // 数据格式
)
```

---

## 🎨 界面定制

### 1. 自定义UI配置

```javascript
// 在swagger-ui.html中添加自定义CSS和JS
window.onload = function() {
    // 自定义样式
    const style = document.createElement('style');
    style.innerHTML = `
        .swagger-ui .topbar {
            background-color: #2c3e50;
        }
        .swagger-ui .info .title {
            color: #3498db;
        }
    `;
    document.head.appendChild(style);

    // 自动设置认证
    const ui = SwaggerUIBundle({
        url: '/v3/api-docs',
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        plugins: [
            SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout",
        defaultModelsExpandDepth: 2,
        defaultModelExpandDepth: 2,
        tryItOutEnabled: true
    });
};
```

### 2. 认证配置

```java
@Configuration
public class SwaggerAuthConfig {

    @Bean
    public SecurityScheme apiKeyAuth() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT Token认证");
    }

    @Bean
    public SecurityRequirement apiKeyRequirement() {
        return new SecurityRequirement().addList("apiKeyAuth");
    }
}
```

---

## 📊 文档导出

### 1. 导出为PDF

```bash
# 使用swagger2pdf工具
npx swagger2pdf generate \
  --input http://localhost:8080/v3/api-docs \
  --output api-documentation.pdf

# 或使用puppeteer
npx puppeteer pdf \
  --url http://localhost:8080/swagger-ui.html \
  --output api-documentation.pdf
```

### 2. 导出为Markdown

```java
@Component
public class SwaggerMarkdownGenerator {

    public String generateMarkdown(OpenAPI openAPI) {
        StringBuilder markdown = new StringBuilder();

        // 标题
        markdown.append("# ").append(openAPI.getInfo().getTitle()).append("\n\n");

        // 版本信息
        markdown.append("**版本**: ").append(openAPI.getInfo().getVersion()).append("\n");
        markdown.append("**描述**: ").append(openAPI.getInfo().getDescription()).append("\n\n");

        // API列表
        markdown.append("## API列表\n\n");

        // 生成各API的Markdown
        openAPI.getPaths().forEach((path, pathItem) -> {
            markdown.append("### ").append(path).append("\n");
            // 详细的API文档生成逻辑
        });

        return markdown.toString();
    }
}
```

---

## 🔍 调试和测试

### 1. 接口测试

Swagger UI提供了直接的接口测试功能：

1. **打开接口详情**: 点击API展开详情
2. **填写参数**: 根据参数要求填写测试数据
3. **设置认证**: 在右上角设置Bearer Token
4. **执行请求**: 点击"Try it out"执行测试
5. **查看结果**: 查看响应数据和状态码

### 2. 批量测试

```bash
# 使用curl测试API
curl -X GET "http://localhost:8080/api/v1/users/1001" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"

# 使用Postman导入API
# 从Swagger UI导出OpenAPI JSON文件，然后导入Postman
```

---

## 📱 移动端适配

### 响应式设计

Swagger UI默认支持移动端，可以通过以下方式优化：

```yaml
springdoc:
  swagger-ui:
    # 启用移动端适配
    displayRequestDuration: true
    filter: true
    tags-sorter: alpha
    operations-sorter: alpha
    docExpansion: none
    disable-swagger-default-url: true
    displayOperationId: false
    showExtensions: false
    showCommonExtensions: false
```

---

## 🔒 安全最佳实践

### 1. 生产环境配置

```yaml
# 生产环境建议
springdoc:
  api-docs:
    enabled: true           # 根据需要开启
  swagger-ui:
    enabled: false          # 生产环境关闭UI界面
  group-configs:
    - group: 'public'
      display-name: '公共接口'
      paths-to-match: '/api/v1/public/**'
```

### 2. 敏感信息隐藏

```java
@Schema(
    description = "用户密码",
    accessMode = Schema.AccessMode.READ_ONLY,  // 只读
    hidden = true,                              // 隐藏敏感字段
    example = "******"
)
private String password;
```

---

## 🚀 性能优化

### 1. 缓存配置

```yaml
springdoc:
  api-docs:
    resolve-schema-properties: true
    writer-with-default-pretty-printer: true
  cache:
    disabled: false
  show-actuator: false
  default-consumes-media-type: application/json
```

### 2. 延迟加载

```java
@Profile({"dev", "test"})
@Configuration
public class SwaggerConfig {
    // 只在开发和测试环境启用详细配置
}
```

---

**配置维护**: IOE-DREAM技术团队
**最后更新**: 2025-12-21
**版本**: v2.0.0

🎉 **完整的Swagger/OpenAPI配置指南，为IOE-DREAM项目提供专业级的API文档支持！**