# Task 3.3: API合约管理策略规划

## 📊 执行摘要

**设计日期**: 2025-11-27
**设计目标**: 建立完整的API合约管理体系，确保微服务间接口的一致性、可维护性和向后兼容性
**核心发现**: 基于OpenAPI 3.0规范，结合Spring Cloud Contract和API版本管理，建立企业级API治理体系
**技术选型**: OpenAPI 3.0 + Swagger UI + Spring Cloud Contract + API Gateway + 版本管理

### 🔍 关键设计决策
- **API规范**: 统一使用OpenAPI 3.0标准
- **合约管理**: Spring Cloud Contract自动化测试
- **版本策略**: 语义化版本控制 + 向后兼容性保证
- **文档管理**: Swagger UI自动化文档生成
- **API网关**: 统一入口管理和版本路由
- **监控治理**: API使用监控和性能分析

---

## 📋 API合约管理体系架构

### 1. 合约生命周期管理

#### 1.1 API合约开发流程

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   需求分析阶段   │───▶│   合约设计阶段   │───▶│   开发实施阶段   │
│                 │    │                 │    │                 │
│ • 业务需求分析   │    │ • OpenAPI设计    │    │ • 代码生成      │
│ • 接口需求梳理   │    │ • 数据模型定义   │    │ • 单元测试      │
│ • 使用场景确认   │    │ • 错误码定义     │    │ • 集成测试      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                        │                        │
        ▼                        ▼                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   测试验证阶段   │───▶│   部署发布阶段   │───▶│   监控维护阶段   │
│                 │    │                 │    │                 │
│ • 契约测试      │    │ • 版本发布      │    │ • 性能监控      │
│ • 兼容性测试    │    │ • 文档更新      │    │ • 使用分析      │
│ • 回归测试      │    │ • 灰度发布      │    │ • 异常告警      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

#### 1.2 合约版本管理策略

**版本命名规范**:
```
主版本号.次版本号.修订号 (Major.Minor.Patch)

示例:
v1.0.0 - 初始版本
v1.1.0 - 新增功能，向后兼容
v1.1.1 - 修复bug，向后兼容
v2.0.0 - 重大变更，不向后兼容
```

**版本兼容性矩阵**:
| 客户端版本 | 服务端v1.0 | 服务端v1.1 | 服务端v2.0 |
|-----------|-----------|-----------|-----------|
| **v1.0** | ✅ 兼容 | ✅ 兼容 | ❌ 不兼容 |
| **v1.1** | ⚠️ 部分兼容 | ✅ 兼容 | ❌ 不兼容 |
| **v2.0** | ❌ 不兼容 | ❌ 不兼容 | ✅ 兼容 |

### 2. OpenAPI 3.0规范设计

#### 2.1 API文档结构模板

```yaml
# api-template.yml - OpenAPI 3.0模板
openapi: 3.0.3
info:
  title: IOE-DREAM Smart Platform API
  description: |
    IOE-DREAM智能校园平台API文档

    ## 认证方式
    所有API都需要通过JWT Token进行认证，在请求头中携带：
    ```
    Authorization: Bearer <JWT_TOKEN>
    ```

    ## 错误码说明
    | 错误码 | 说明 |
    |--------|------|
    | 200 | 请求成功 |
    | 400 | 请求参数错误 |
    | 401 | 未授权访问 |
    | 403 | 权限不足 |
    | 404 | 资源不存在 |
    | 500 | 服务器内部错误 |

  version: 1.0.0
  contact:
    name: IOE-DREAM开发团队
    email: dev-support@ioedream.com
    url: https://docs.ioedream.com/api
  license:
    name: MIT
    url: https://opensource.org/licenses/MIT

servers:
  - url: https://api.ioedream.com/v1
    description: 生产环境
  - url: https://api-staging.ioedream.com/v1
    description: 测试环境
  - url: http://localhost:8080/v1
    description: 开发环境

# 安全认证配置
security:
  - bearerAuth: []

components:
  # 安全方案定义
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT Token认证

  # 通用数据模型
  schemas:
    # 通用响应格式
    ApiResponse:
      type: object
      properties:
        code:
          type: integer
          description: 响应状态码
          example: 200
        message:
          type: string
          description: 响应消息
          example: "操作成功"
        data:
          description: 响应数据
        timestamp:
          type: integer
          description: 时间戳
          example: 1638000000000
        traceId:
          type: string
          description: 链路追踪ID
          example: "abc123def456"
      required:
        - code
        - message
        - timestamp

    # 分页响应格式
    PageResponse:
      type: object
      properties:
        total:
          type: integer
          description: 总记录数
          example: 100
        page:
          type: integer
          description: 当前页码
          example: 1
        size:
          type: integer
          description: 每页大小
          example: 20
        pages:
          type: integer
          description: 总页数
          example: 5
        records:
          type: array
          description: 数据列表
          items:
            $ref: '#/components/schemas/AnyType'

    # 分页请求参数
    PageRequest:
      type: object
      properties:
        page:
          type: integer
          description: 页码(从1开始)
          minimum: 1
          default: 1
        size:
          type: integer
          description: 每页大小
          minimum: 1
          maximum: 100
          default: 20
        sort:
          type: string
          description: 排序字段
          example: "createTime,desc"

  # 通用参数定义
  parameters:
    PageParam:
      in: query
      name: page
      schema:
        type: integer
        minimum: 1
        default: 1
      description: 页码

    SizeParam:
      in: query
      name: size
      schema:
        type: integer
        minimum: 1
        maximum: 100
        default: 20
      description: 每页大小

    SortParam:
      in: query
      name: sort
      schema:
        type: string
        pattern: "^[a-zA-Z]+,(asc|desc)$"
      description: 排序字段和方向

  # 通用响应定义
  responses:
    SuccessResponse:
      description: 操作成功
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ApiResponse'

    UnauthorizedResponse:
      description: 未授权
      content:
        application/json:
          schema:
            allOf:
              - $ref: '#/components/schemas/ApiResponse'
              - type: object
                properties:
                  code:
                    example: 401
                  message:
                    example: "未授权访问"

    ForbiddenResponse:
      description: 权限不足
      content:
        application/json:
          schema:
            allOf:
              - $ref: '#/components/schemas/ApiResponse'
              - type: object
                properties:
                  code:
                    example: 403
                  message:
                    example: "权限不足"

    NotFoundResponse:
      description: 资源不存在
      content:
        application/json:
          schema:
            allOf:
              - $ref: '#/components/schemas/ApiResponse'
              - type: object
                properties:
                  code:
                    example: 404
                  message:
                    example: "资源不存在"

    ErrorResponse:
      description: 服务器错误
      content:
        application/json:
          schema:
            allOf:
              - $ref: '#/components/schemas/ApiResponse'
              - type: object
                properties:
                  code:
                    example: 500
                  message:
                    example: "服务器内部错误"
```

#### 2.2 具体服务API示例

**用户权限服务API**:
```yaml
# identity-service-api.yml
openapi: 3.0.3
info:
  title: Identity Service API
  description: 用户身份与权限管理服务API
  version: 1.2.0

paths:
  /users:
    get:
      summary: 获取用户列表
      description: 分页获取用户列表，支持搜索和筛选
      parameters:
        - $ref: '#/components/parameters/PageParam'
        - $ref: '#/components/parameters/SizeParam'
        - $ref: '#/components/parameters/SortParam'
        - name: keyword
          in: query
          description: 搜索关键词(用户名、姓名)
          schema:
            type: string
        - name: status
          in: query
          description: 用户状态
          schema:
            type: string
            enum: [active, inactive, locked]
        - name: roleId
          in: query
          description: 角色ID
          schema:
            type: integer
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/ApiResponse'
                  - type: object
                    properties:
                      data:
                        $ref: '#/components/schemas/UserPageResponse'

    post:
      summary: 创建用户
      description: 创建新用户账号
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateUserRequest'
      responses:
        '201':
          description: 创建成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/ApiResponse'
                  - type: object
                    properties:
                      data:
                        $ref: '#/components/schemas/UserVO'
        '400':
          $ref: '#/components/responses/ErrorResponse'

  /users/{userId}:
    get:
      summary: 获取用户详情
      parameters:
        - name: userId
          in: path
          required: true
          description: 用户ID
          schema:
            type: integer
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/ApiResponse'
                  - type: object
                    properties:
                      data:
                        $ref: '#/components/schemas/UserDetailVO'
        '404':
          $ref: '#/components/responses/NotFoundResponse'

    put:
      summary: 更新用户信息
      parameters:
        - name: userId
          in: path
          required: true
          description: 用户ID
          schema:
            type: integer
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateUserRequest'
      responses:
        '200':
          description: 更新成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/ApiResponse'
                  - type: object
                    properties:
                      data:
                        $ref: '#/components/schemas/UserVO'

  /users/{userId}/permissions:
    get:
      summary: 获取用户权限
      description: 获取指定用户在指定区域的权限信息
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: integer
        - name: areaId
          in: query
          description: 区域ID(可选，不传则获取所有区域权限)
          schema:
            type: integer
      responses:
        '200':
          description: 获取成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/ApiResponse'
                  - type: object
                    properties:
                      data:
                        $ref: '#/components/schemas/UserPermissionVO'

components:
  schemas:
    # 用户视图对象
    UserVO:
      type: object
      properties:
        userId:
          type: integer
          description: 用户ID
          example: 1001
        username:
          type: string
          description: 用户名
          example: "zhangsan"
        realName:
          type: string
          description: 真实姓名
          example: "张三"
        email:
          type: string
          format: email
          description: 邮箱
          example: "zhangsan@example.com"
        phone:
          type: string
          description: 手机号
          example: "13800138000"
        status:
          type: string
          enum: [active, inactive, locked]
          description: 用户状态
          example: "active"
        createTime:
          type: string
          format: date-time
          description: 创建时间
          example: "2023-01-01T00:00:00Z"
        updateTime:
          type: string
          format: date-time
          description: 更新时间
          example: "2023-01-01T00:00:00Z"

    # 用户详情视图
    UserDetailVO:
      allOf:
        - $ref: '#/components/schemas/UserVO'
        - type: object
          properties:
            roles:
              type: array
              description: 用户角色列表
              items:
                $ref: '#/components/schemas/RoleVO'
            permissions:
              type: array
              description: 权限列表
              items:
                type: string
            lastLoginTime:
              type: string
              format: date-time
              description: 最后登录时间
            loginCount:
              type: integer
              description: 登录次数
              example: 100

    # 分页用户响应
    UserPageResponse:
      allOf:
        - $ref: '#/components/schemas/PageResponse'
        - type: object
          properties:
            records:
              type: array
              description: 用户列表
              items:
                $ref: '#/components/schemas/UserVO'

    # 创建用户请求
    CreateUserRequest:
      type: object
      required:
        - username
        - realName
        - password
      properties:
        username:
          type: string
          description: 用户名
          minLength: 3
          maxLength: 50
          pattern: "^[a-zA-Z0-9_]+$"
          example: "zhangsan"
        realName:
          type: string
          description: 真实姓名
          minLength: 2
          maxLength: 50
          example: "张三"
        password:
          type: string
          description: 密码
          minLength: 6
          maxLength: 128
          format: password
          example: "password123"
        email:
          type: string
          format: email
          description: 邮箱
          example: "zhangsan@example.com"
        phone:
          type: string
          description: 手机号
          pattern: "^1[3-9]\\d{9}$"
          example: "13800138000"
        roleIds:
          type: array
          description: 角色ID列表
          items:
            type: integer
          example: [1, 2]
        areaIds:
          type: array
          description: 可访问区域ID列表
          items:
            type: integer
          example: [100, 200]

    # 更新用户请求
    UpdateUserRequest:
      type: object
      properties:
        realName:
          type: string
          description: 真实姓名
          minLength: 2
          maxLength: 50
          example: "张三"
        email:
          type: string
          format: email
          description: 邮箱
          example: "zhangsan@example.com"
        phone:
          type: string
          description: 手机号
          pattern: "^1[3-9]\\d{9}$"
          example: "13800138000"
        status:
          type: string
          enum: [active, inactive, locked]
          description: 用户状态
          example: "active"
        roleIds:
          type: array
          description: 角色ID列表
          items:
            type: integer
          example: [1, 2]
        areaIds:
          type: array
          description: 可访问区域ID列表
          items:
            type: integer
          example: [100, 200]

    # 用户权限信息
    UserPermissionVO:
      type: object
      properties:
        userId:
          type: integer
          description: 用户ID
          example: 1001
        username:
          type: string
          description: 用户名
          example: "zhangsan"
        roles:
          type: array
          description: 角色列表
          items:
            $ref: '#/components/schemas/RoleVO'
        permissions:
          type: array
          description: 权限标识列表
          items:
            type: string
          example: ["user:view", "user:edit", "access:verify"]
        accessibleAreas:
          type: array
          description: 可访问区域列表
          items:
            $ref: '#/components/schemas/AreaVO'
```

### 3. Spring Cloud Contract集成

#### 3.1 契约定义

**生产者契约定义**:
```groovy
// contracts/base/user_contract.groovy
package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "获取用户信息API合约"
    name "getUserById"
    request {
        method GET()
        url "/api/v1/users/1001"
        headers {
            header "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
            header "Content-Type": "application/json"
        }
    }
    response {
        status 200
        headers {
            header "Content-Type": "application/json"
        }
        body(
            code: 200,
            message: "获取成功",
            data: [
                userId: 1001,
                username: "zhangsan",
                realName: "张三",
                email: "zhangsan@example.com",
                phone: "13800138000",
                status: "active",
                createTime: "2023-01-01T00:00:00Z",
                updateTime: "2023-01-01T00:00:00Z"
            ],
            timestamp: 1638000000000,
            traceId: "abc123def456"
        )
    }
}

Contract.make {
    description "创建用户API合约"
    name "createUser"
    request {
        method POST()
        url "/api/v1/users"
        headers {
            header "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
            header "Content-Type": "application/json"
        }
        body(
            username: "lisi",
            realName: "李四",
            password: "password123",
            email: "lisi@example.com",
            phone: "13900139000",
            roleIds: [1, 2],
            areaIds: [100, 200]
        )
    }
    response {
        status 201
        headers {
            header "Content-Type": "application/json"
        }
        body(
            code: 201,
            message: "创建成功",
            data: [
                userId: 1002,
                username: "lisi",
                realName: "李四",
                email: "lisi@example.com",
                phone: "13900139000",
                status: "active",
                createTime: "2023-01-02T00:00:00Z",
                updateTime: "2023-01-02T00:00:00Z"
            ],
            timestamp: 1638086400000,
            traceId: "def456ghi789"
        )
    }
}
```

#### 3.2 消费者测试

**消费者单元测试**:
```java
@SpringBootTest
@AutoConfigureStubRunner(
    stubsMode = StubRunnerProperties.StubsMode.LOCAL,
    ids = "com.ioedream:identity-service:+:stubs:8080"
)
class IdentityServiceClientTest {

    @Resource
    private IdentityServiceClient identityServiceClient;

    @Test
    void shouldGetUserById() {
        // Given
        Long userId = 1001L;

        // When
        UserVO user = identityServiceClient.getUserById(userId);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isEqualTo(1001L);
        assertThat(user.getUsername()).isEqualTo("zhangsan");
        assertThat(user.getRealName()).isEqualTo("张三");
    }

    @Test
    void shouldCreateUser() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
            .username("lisi")
            .realName("李四")
            .password("password123")
            .email("lisi@example.com")
            .phone("13900139000")
            .roleIds(Arrays.asList(1L, 2L))
            .areaIds(Arrays.asList(100L, 200L))
            .build();

        // When
        UserVO user = identityServiceClient.createUser(request);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isEqualTo(1002L);
        assertThat(user.getUsername()).isEqualTo("lisi");
        assertThat(user.getRealName()).isEqualTo("李四");
    }
}
```

### 4. API版本管理策略

#### 4.1 版本路由配置

**API Gateway版本路由**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        # V1版本路由
        - id: identity-service-v1
          uri: lb://identity-service
          predicates:
            - Path=/api/v1/identity/**
            - Method=GET,POST,PUT,DELETE
          filters:
            - StripPrefix=2
            - AddRequestHeader=API-Version, v1

        # V2版本路由
        - id: identity-service-v2
          uri: lb://identity-service
          predicates:
            - Path=/api/v2/identity/**
            - Method=GET,POST,PUT,DELETE
          filters:
            - StripPrefix=2
            - AddRequestHeader=API-Version, v2

        # 最新版本路由(无版本号指向最新版本)
        - id: identity-service-latest
          uri: lb://identity-service
          predicates:
            - Path=/api/identity/**
            - Method=GET,POST,PUT,DELETE
          filters:
            - StripPrefix=1
            - AddRequestHeader=API-Version, v2
```

#### 4.2 版本兼容性处理

**版本适配器**:
```java
@Component
public class ApiVersionAdapter {

    @Resource
    private IdentityServiceV1 identityServiceV1;
    @Resource
    private IdentityServiceV2 identityServiceV2;

    public UserVO getUserByVersion(String version, Long userId) {
        switch (version) {
            case "v1":
                return adaptV1ToV2(identityServiceV1.getUser(userId));
            case "v2":
            default:
                return identityServiceV2.getUser(userId);
        }
    }

    private UserVO adaptV1ToV2(UserVOV1 v1User) {
        return UserVO.builder()
            .userId(v1User.getUserId())
            .username(v1User.getUsername())
            .realName(v1User.getRealName())
            .email(v1User.getEmail())
            .phone(v1User.getPhone())
            .status(convertStatus(v1User.getStatus()))
            .createTime(v1User.getCreateTime())
            .updateTime(v1User.getUpdateTime())
            // 新增字段默认值
            .avatarUrl(null)
            .lastLoginTime(null)
            .build();
    }
}
```

### 5. API文档生成与管理

#### 5.1 Swagger UI配置

**Swagger配置类**:
```java
@Configuration
@EnableOpenApi
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("IOE-DREAM API Documentation")
                .description("IOE-DREAM智能校园平台API文档")
                .version("1.0.0")
                .contact(new Contact()
                    .name("IOE-DREAM开发团队")
                    .email("dev-support@ioedream.com")
                    .url("https://www.ioedream.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .externalDocs(new ExternalDocumentation()
                .description("IOE-DREAM平台完整文档")
                .url("https://docs.ioedream.com"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/v1/**")
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/api/v1/admin/**")
            .build();
    }
}
```

#### 5.2 文档注解使用

**Controller文档注解**:
```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户信息的增删改查操作")
@Validated
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping
    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持搜索和筛选")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(schema = @Schema(implementation = UserPageResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权访问")
    })
    public ApiResponse<PageResponse<UserVO>> getUsers(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户状态") @RequestParam(required = false) UserStatus status) {

        PageRequest pageRequest = PageRequest.builder()
            .page(page)
            .size(size)
            .keyword(keyword)
            .status(status)
            .build();

        PageResponse<UserVO> response = userService.getUsers(pageRequest);

        return ApiResponse.success(response);
    }

    @PostMapping
    @Operation(summary = "创建用户", description = "创建新的用户账号")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = UserVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "409", description = "用户名已存在")
    })
    public ApiResponse<UserVO> createUser(
            @Parameter(description = "用户创建请求", required = true)
            @Valid @RequestBody CreateUserRequest request) {

        UserVO user = userService.createUser(request);

        return ApiResponse.success(user);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(schema = @Schema(implementation = UserDetailVO.class))),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ApiResponse<UserDetailVO> getUserDetail(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId) {

        UserDetailVO user = userService.getUserDetail(userId);

        return ApiResponse.success(user);
    }
}
```

---

## 📊 API监控与治理

### 1. API使用监控

#### 1.1 API调用指标收集

```java
@Component
@Slf4j
public class ApiMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final Counter apiCallCounter;
    private final Timer apiCallTimer;
    private final Gauge apiActiveConnections;

    public ApiMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.apiCallCounter = Counter.builder("api.calls.total")
            .description("Total API calls")
            .register(meterRegistry);
        this.apiCallTimer = Timer.builder("api.calls.duration")
            .description("API call duration")
            .register(meterRegistry);
        this.apiActiveConnections = Gauge.builder("api.connections.active")
            .description("Active API connections")
            .register(meterRegistry, this, ApiMetricsCollector::getActiveConnections);
    }

    public <T> T recordApiCall(String api, String method, Supplier<T> supplier) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            T result = supplier.get();

            // 记录成功调用
            apiCallCounter.increment(
                Tags.of("api", api, "method", method, "status", "success")
            );

            return result;
        } catch (Exception e) {
            // 记录失败调用
            apiCallCounter.increment(
                Tags.of("api", api, "method", method, "status", "error")
            );
            throw e;
        } finally {
            sample.stop(Timer.builder("api.calls.duration")
                .tag("api", api)
                .tag("method", method)
                .register(meterRegistry));
        }
    }

    private double getActiveConnections() {
        // 返回当前活跃连接数
        return activeConnectionCount.get();
    }
}
```

#### 1.2 API性能告警

```java
@Component
@Slf4j
public class ApiPerformanceMonitor {

    @Resource
    private AlertService alertService;

    @EventListener
    @Async
    public void handleSlowApiCall(SlowApiCallEvent event) {
        log.warn("检测到慢API调用: {} - {}ms", event.getApi(), event.getDuration());

        // 发送告警
        AlertRequest alert = AlertRequest.builder()
            .title("API性能告警")
            .message(String.format("API %s 调用耗时 %d ms，超过阈值 %d ms",
                event.getApi(), event.getDuration(), event.getThreshold()))
            .level(AlertLevel.WARNING)
            .source("API监控")
            .build();

        alertService.sendAlert(alert);
    }

    @EventListener
    @Async
    public void handleHighErrorRate(HighErrorRateEvent event) {
        log.error("检测到高错误率: {} - {}%", event.getApi(), event.getErrorRate());

        AlertRequest alert = AlertRequest.builder()
            .title("API错误率告警")
            .message(String.format("API %s 错误率 %.2f%%，超过阈值 %.2f%%",
                event.getApi(), event.getErrorRate(), event.getThreshold()))
            .level(AlertLevel.CRITICAL)
            .source("API监控")
            .build();

        alertService.sendAlert(alert);
    }
}
```

### 2. API安全管理

#### 2.1 访问频率限制

```java
@Component
public class ApiRateLimiter {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public boolean isAllowed(String api, String clientId, int limit, int windowSeconds) {
        String key = String.format("rate_limit:%s:%s", api, clientId);

        return redisTemplate.execute(new SessionCallback<Boolean>() {
            @Override
            public Boolean execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                // 清理过期记录
                long currentTime = System.currentTimeMillis();
                long windowStart = currentTime - windowSeconds * 1000L;
                operations.opsForZSet().removeRangeByScore(key, 0, windowStart);

                // 获取当前窗口内请求数
                Long count = operations.opsForZSet().count(key, windowStart, currentTime);

                if (count < limit) {
                    // 记录当前请求
                    operations.opsForZSet().add(key, String.valueOf(currentTime), currentTime);
                    operations.expire(key, windowSeconds, TimeUnit.SECONDS);
                    operations.exec();
                    return true;
                } else {
                    operations.discard();
                    return false;
                }
            }
        });
    }
}
```

#### 2.2 API访问审计

```java
@Component
@Slf4j
public class ApiAuditLogger {

    @Resource
    private AuditLogService auditLogService;

    @EventListener
    @Async
    public void logApiCall(ApiCallEvent event) {
        AuditLog auditLog = AuditLog.builder()
            .userId(event.getUserId())
            .username(event.getUsername())
            .api(event.getApi())
            .method(event.getMethod())
            .requestParams(JsonUtils.toJson(event.getParams()))
            .responseCode(event.getResponseCode())
            .responseTime(event.getResponseTime())
            .clientIp(event.getClientIp())
            .userAgent(event.getUserAgent())
            .createTime(LocalDateTime.now())
            .build();

        auditLogService.save(auditLog);
    }
}
```

---

## 🔮 API治理演进路线图

### Phase 1: 基础合约管理 (2个月)
- [ ] OpenAPI 3.0规范标准化
- [ ] Swagger UI文档系统建设
- [ ] API版本管理机制实施
- [ ] 基础监控指标收集

### Phase 2: 契约测试实施 (1个月)
- [ ] Spring Cloud Contract集成
- [ ] 自动化测试流水线
- [ ] 消费者驱动契约测试
- [ ] CI/CD集成

### Phase 3: 高级治理功能 (2个月)
- [ ] API使用监控 dashboard
- [ ] 性能告警和自动降级
- [ ] 访问控制和安全审计
- [ ] API生命周期管理

### Phase 4: 智能化治理 (1个月)
- [ ] API使用分析和推荐
- [ ] 自动化兼容性检查
- [ ] 智能版本升级建议
- [ ] API质量评分系统

---

**报告生成时间**: 2025-11-27T23:45:00+08:00
**设计完成度**: Phase 3 Task 3.3 - 100%完成
**下一任务**: Task 3.4 - 创建技术栈现代化计划

这个API合约管理策略为IOE-DREAM微服务架构提供了完整的API治理框架，确保API的一致性、可维护性和向后兼容性，支持微服务间的可靠通信和协作。