# API规范（权威文档）

> **📋 文档版本**: v4.0.0 (整合版)
> **📋 文档职责**: SmartAdmin项目的唯一API设计规范权威来源，基于Knife4j 4.6.0，贴合项目实际情况。

## ⚠️ 核心约束

### 🚫 绝对禁止
```markdown
❌ 禁止使用GET方法进行数据修改操作
❌ 禁止在URL中传递敏感信息
❌ 禁止返回未经包装的原始数据
❌ 禁止使用不安全的HTTP方法
❌ 禁止缺少必要的权限验证
❌ 禁止API版本混乱管理
❌ 禁止缺少错误处理机制
❌ 禁止返回过多的数据字段
❌ 禁止绕过Knife4j注解直接返回数据
❌ 禁止Controller中直接操作权限逻辑
```

### ✅ 必须执行
```markdown
✅ 必须使用RESTful设计风格
✅ 必须统一ResponseDTO响应格式
✅ 必须进行@Valid参数验证
✅ 必须使用Sa-Token权限控制
✅ 必须使用Knife4j注解编写API文档
✅ 必须处理所有异常情况
✅ 必须遵循命名规范
✅ 必须考虑性能和安全
✅ 必须使用Knife4j的分组和标签功能
```

## 🛠️ Knife4j配置规范

### Knife4j配置类
```java
@Configuration
@EnableOpenApi
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartAdmin API文档")
                        .version("1.0.0")
                        .description("SmartAdmin微服务系统API文档")
                        .contact(new Contact()
                                .name("SmartAdmin Team")
                                .email("smartadmin@example.com")
                                .url("https://smartadmin.vip")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目文档")
                        .url("https://docs.smartadmin.com"))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("开发环境"),
                        new Server()
                                .url("https://api.smartadmin.com")
                                .description("生产环境")
                ))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityRequirement(new SecurityRequirement()
                        .addList("BearerAuth"));
    }

    @Bean
    public GroupedOpenApiCustomizer defaultGroupedOpenApiCustomizer() {
        return new GroupedOpenApiCustomizer() {
            @Override
            public void customize(GroupedOpenApiBuilder builder) {
                builder.title("SmartAdmin API文档");
                builder.description("基于Knife4j的API文档，自动生成，实时更新");
            }
        };
    }

    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return new GlobalOpenApiCustomizer() {
            @Override
            public void customize(OpenAPI openApi) {
                // 全局通用响应示例
                Map<String, Object> successExample = new HashMap<>();
                successExample.put("code", 200);
                successExample.put("message", "操作成功");
                successExample("data", "示例数据");
                successExample("success", true);

                Map<String, Object> errorExample = new HashMap<>();
                errorExample.put("code", 500);
                errorExample.put("message", "系统异常");
                errorExample("data", null);
                errorExample("success", false);

                // 全局通用响应类型
                Components schemas
                    .add("SuccessResult", new Schema<>()
                            .implementation(SuccessResult.class))
                    .add("ErrorResult", new Schema<>()
                            .implementation(ErrorResult.class));

                // 全局响应头
                Headers headers = new Headers();
                headers.add("Cache-Control", "no-cache");
                headers.add("Pragma", "no-cache");

                // 全局API响应示例
                ApiResponse successResponse = new ApiResponse()
                        .description("成功响应")
                        .content(new Content()
                                .mediaType("application/json")
                                .schema(getSchema("SuccessResult")))
                        .headers(headers);

                ApiResponse errorResponse = new ApiResponse()
                        .description("错误响应")
                        .content(new Content()
                                .mediaType("application/json")
                                .schema(getSchema("ErrorResult")))
                        .headers(headers);

                openApi.getGlobalResponses()
                        .add("200", successResponse)
                        .add("500", errorResponse);
            }
        };
    }

    private Schema<?> getSchema(String name) {
        return new Schema<>()
                .type("object")
                .name(name)
                .exampleMap(exampleMap)
                .properties(getPropertyMap(name));
    }

    private Map<String, Property> getPropertyMap(String name) {
        Map<String, Property> properties = new LinkedHashMap<>();

        if ("SuccessResult".equals(name)) {
            properties.put("code", new Property().type("integer").description("状态码"));
            properties.put("message", new Property().type("string").description("消息"));
            properties.put("data", new Property().type("object").description("数据"));
            properties.put("success", new Property().type("boolean").description("是否成功"));
        } else if ("ErrorResult".equals(name)) {
            properties.put("code", new Property().type("integer").description("错误码"));
            properties.put("message", new Property().type("string").description("错误消息"));
            properties.put("data", new Property().type("object").description("数据"));
            properties.put("success", new Property().type("boolean").description("是否成功"));
        }

        return properties;
    }
}
```

## 🏗️ RESTful设计规范

### URL设计规范
```markdown
✅ 使用名词复数形式：/api/users, /api/orders
✅ 使用小写字母和连字符：/api/user-profiles
✅ 层级关系清晰：/api/users/{userId}/orders
✅ 版本控制：/api/v1/users, /api/v2/users
✅ 分页参数：/api/users?page=1&size=20
✅ 过滤参数：/api/users?status=active&type=vip
✅ 排序参数：/api/users?sort=createTime:desc,userName:asc
❌ 禁止使用动词：/api/getUser, /api/createOrder
❌ 禁止深层嵌套：/api/users/{userId}/orders/{orderId}/items/{itemId}
```

### HTTP方法规范
```markdown
✅ GET: 查询资源（安全，幂等）
✅ POST: 创建资源（不安全，不幂等）
✅ POST: 更新资源（不安全，幂等）
✅ POST: 删除资源（不安全，幂等）
❌ 禁止GET方法修改数据
❌ 禁止使用PUT、PATCH方法（SmartAdmin统一使用POST）
```

### Controller层实现模板
```java
// 标准Controller模板
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户信息的增删改查操作")
@SaCheckLogin
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "新增用户", description = "新增用户信息")
    @PostMapping("/add")
    @SaCheckPermission("user:add")
    public ResponseDTO<String> add(@RequestBody @Valid UserAddForm addForm) {
        log.info("新增用户, param: {}", addForm);
        return ResponseDTO.ok(userService.addUser(addForm));
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PostMapping("/update")
    @SaCheckPermission("user:update")
    public ResponseDTO<String> update(@RequestBody @Valid UserUpdateForm updateForm) {
        log.info("更新用户, param: {}", updateForm);
        return ResponseDTO.ok(userService.updateUser(updateForm));
    }

    @Operation(summary = "删除用户", description = "删除用户信息")
    @PostMapping("/delete")
    @SaCheckPermission("user:delete")
    public ResponseDTO<String> delete(@RequestBody @Valid IdForm idForm) {
        log.info("删除用户, id: {}", idForm.getId());
        return ResponseDTO.ok(userService.deleteUser(idForm.getId()));
    }

    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    @PostMapping("/page")
    @SaCheckPermission("user:query")
    public ResponseDTO<PageResult<UserVO>> page(@RequestBody @Valid UserQueryForm queryForm) {
        log.info("分页查询用户, param: {}", queryForm);
        return ResponseDTO.ok(userService.getUserList(queryForm));
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @GetMapping("/detail/{id}")
    @SaCheckPermission("user:query")
    public ResponseDTO<UserVO> detail(@PathVariable Long id) {
        log.info("获取用户详情, id: {}", id);
        return ResponseDTO.ok(userService.getUserDetail(id));
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current")
    @SaCheckLogin
    public ResponseDTO<UserVO> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("获取当前用户信息, userId: {}", userId);
        return ResponseDTO.ok(userService.getUserDetail(userId));
    }

    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PostMapping("/status")
    @SaCheckPermission("user:update")
    public ResponseDTO<String> updateStatus(@RequestBody @Valid UserStatusForm statusForm) {
        log.info("更新用户状态, param: {}", statusForm);
        return ResponseDTO.ok(userService.updateUserStatus(statusForm));
    }

    @Operation(summary = "重置用户密码", description = "重置用户密码")
    @PostMapping("/reset-password")
    @SaCheckPermission("user:update")
    public ResponseDTO<String> resetPassword(@RequestBody @Valid UserResetPasswordForm form) {
        log.info("重置用户密码, userId: {}", form.getUserId());
        return ResponseDTO.ok(userService.resetPassword(form));
    }
}
```

## 📋 统一响应格式

### ResponseDTO通用类
```java
// 成功响应
@Data
@AllArgsConstructor
public class ResponseDTO<T> implements Serializable {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 请求ID
     */
    private String traceId;

    public ResponseDTO() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    public ResponseDTO(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.success = false;
        this.timestamp = LocalDateTime.now();
    }

    public ResponseDTO(T data) {
        this.code = ResponseCode.SUCCESS.getCode();
        this.message = ResponseStringConst.SUCCESS;
        this.data = data;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ResponseDTO<T> ok() {
        return new ResponseDTO<>(ResponseCode.SUCCESS.getCode(), ResponseStringConst.SUCCESS);
    }

    public static <T> ResponseDTO<T> ok(T data) {
        return new ResponseDTO<>(data);
    }

    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>(ResponseCode.ERROR.getCode(), message);
    }

    public static <T> ResponseDTO<T> error(Integer code, String message) {
        response.setCode(code);
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }
}

// 分页响应
@Data
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    /**
     * 记录列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;
}

// 状态码枚举
public enum ResponseCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),
    PARAM_ERROR(400, "参数错误"),
    AUTH_ERROR(401, "认证失败"),
    PERMISSION_ERROR(403, "权限不足"),
    DATA_NOT_FOUND(404, "数据不存在");

    private final Integer code;
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
```

## 🔒 参数验证规范

### 验证注解使用
```java
// Form参数验证
@Data
public class UserAddForm {

    @NotBlank(message = "用户名不能为空")
    @Length(min = 2, max = 50, message = "用户名长度必须在2-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String userName;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer gender;

    @NotBlank(message = "部门不能为空")
    private Long deptId;
}

// 查询参数验证
@Data
public class UserQueryForm extends PageForm {

    @Length(max = 50, message = "用户名长度不能超过50")
    private String userName;

    @Length(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 2, message = "状态值不能大于2")
    private Integer status;

    @Min(value = 1, message = "部门ID不能小于1")
    private Long deptId;

    private String createTimeStart;
    private String createTimeEnd;
}
```

## 🔍 异常处理规范

### 全局异常处理器
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Resource
    private ResponseUtil responseUtil;

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数验证异常: {}", message);
        return ResponseDTO.error(ResponseCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(SaTokenException.class)
    public ResponseDTO<Void> handleSaTokenException(SaTokenException e) {
        log.warn("认证异常: {}", e.getMessage());
        return ResponseDTO.error(ResponseCode.AUTH_ERROR.getCode(), "认证失败: " + e.getMessage());
    }

    @ExceptionHandler(SaTokenNotLoginException.class)
    public ResponseDTO<Void> handleNotLoginException(SaTokenNotLoginException e) {
        log.warn("未登录异常: {}", e.getMessage());
        return ResponseDTO.error(ResponseCode.AUTH_ERROR.getCode(), "请先登录");
    }

    @ExceptionHandler(SaTokenPermissionException.class)
    public ResponseDTO<Void> handlePermissionException(SaTokenPermissionException e) {
        log.warn("权限异常: {}", e.getMessage());
        return ResponseDTO.error(ResponseCode.PERMISSION_ERROR.getCode(), "权限不足: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseDTO.error(ResponseCode.ERROR.getCode(), "系统异常，请联系管理员");
    }
}
```
❌ 禁止POST方法查询数据
❌ 禁止PUT方法创建资源
```

### 状态码规范
```markdown
✅ 200 OK: 请求成功
✅ 201 Created: 资源创建成功
✅ 204 No Content: 请求成功但无返回内容
✅ 400 Bad Request: 请求参数错误
✅ 401 Unauthorized: 未认证
✅ 403 Forbidden: 无权限
✅ 404 Not Found: 资源不存在
✅ 409 Conflict: 资源冲突
✅ 422 Unprocessable Entity: 参数验证失败
✅ 500 Internal Server Error: 服务器内部错误
❌ 禁止返回不合适的状态码
```

## 📝 统一响应格式

### 成功响应格式
```json
{
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {
    "userId": 12345,
    "userName": "张三",
    "email": "zhangsan@example.com"
  },
  "timestamp": "2024-11-09T10:30:00Z",
  "traceId": "abc123def456"
}
```

### 分页响应格式
```json
{
  "code": "SUCCESS",
  "message": "查询成功",
  "data": {
    "records": [
      {
        "userId": 12345,
        "userName": "张三",
        "email": "zhangsan@example.com"
      }
    ],
    "total": 100,
    "size": 20,
    "current": 1,
    "pages": 5
  },
  "timestamp": "2024-11-09T10:30:00Z",
  "traceId": "abc123def456"
}
```

### 错误响应格式
```json
{
  "code": "USER_NOT_FOUND",
  "message": "用户不存在",
  "data": null,
  "errors": [
    {
      "field": "userId",
      "message": "用户ID不存在",
      "code": "INVALID_USER_ID"
    }
  ],
  "timestamp": "2024-11-09T10:30:00Z",
  "traceId": "abc123def456"
}
```

## 🔐 安全规范

### 认证授权
```markdown
✅ 使用JWT或Session进行认证
✅ 实现RBAC权限控制
✅ 敏感操作需要二次验证
✅ 接口访问频率限制
✅ 参数签名验证
✅ SQL注入防护
✅ XSS攻击防护
✅ CSRF攻击防护
```

### 敏感信息处理
```markdown
✅ 密码字段加密传输
✅ 敏感数据脱敏显示
✅ 避免在URL中传递敏感信息
✅ 使用HTTPS协议传输
✅ 定期更新密钥和Token
✅ 实现请求签名验证
❌ 禁止明文传输密码
❌ 禁止在日志中记录敏感信息
```

## 📚 API文档规范

### Swagger注解规范
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户信息的增删改查操作")
public class UserController {

    @Operation(
        summary = "获取用户详情",
        description = "根据用户ID获取用户的详细信息，包括基本信息、角色权限等"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取用户信息",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UserVO.class))),
        @ApiResponse(responseCode = "404", description = "用户不存在",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorVO.class)))
    })
    @GetMapping("/{id}")
    public ResponseDTO<UserVO> getUser(
            @Parameter(description = "用户ID", required = true, example = "12345")
            @PathVariable Long id) {
        return ResponseDTO.ok(userService.getDetail(id));
    }
}
```

## 📊 性能规范

### 响应时间要求
```markdown
✅ 简单查询API: P95 ≤ 100ms
✅ 复杂查询API: P95 ≤ 500ms
✅ 数据修改API: P95 ≤ 200ms
✅ 文件上传API: P95 ≤ 5s
✅ 批量操作API: P95 ≤ 2s
❌ 禁止响应时间超过要求的API上线
```

### 数据传输优化
```markdown
✅ 使用分页查询避免大量数据传输
✅ 实现字段选择器减少不必要字段
✅ 使用数据压缩减少传输量
✅ 实现缓存机制提升响应速度
✅ 优化SQL查询提升数据库性能
❌ 禁止一次性传输大量数据
❌ 禁止返回不必要的字段
```

---

**🎯 核心原则**：
1. **RESTful设计** - 遵循REST架构风格
2. **统一格式** - 响应格式保持一致
3. **安全第一** - 实现完善的认证授权
4. **性能优先** - 优化响应时间和传输效率
5. **文档完善** - 提供清晰准确的API文档

**📖 相关文档**：
- [架构规范](./架构规范.md) - 系统架构设计
- [编码规范](./编码规范.md) - 代码编写规范
- [安全规范](./安全规范.md) - 安全相关规范