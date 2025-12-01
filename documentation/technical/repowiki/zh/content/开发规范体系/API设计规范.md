# 🌐 API设计规范

> **版本**: v1.0
> **更新时间**: 2025-11-26
> **适用范围**: IOE-DREAM智慧园区一卡通管理平台所有API设计

## 📋 概述

本文档定义了IOE-DREAM平台的API设计规范，基于**RESTful架构**和**OpenAPI 3.0**标准，确保API设计的一致性、可维护性和易用性。

---

## 🎯 API设计原则

### 1. RESTful设计原则
- **资源导向**: 以资源为中心设计API
- **无状态**: 每个请求包含完整的处理信息
- **统一接口**: 使用标准的HTTP方法和状态码
- **分层系统**: 支持代理、网关等中间层

### 2. 版本管理原则
- **向后兼容**: 新版本必须兼容旧版本客户端
- **渐进迁移**: 新功能使用最新版本
- **废弃通知**: 提前6个月通知API废弃

### 3. 安全设计原则
- **最小权限**: 每个API只授予必要的权限
- **认证授权**: 所有API都需要身份认证和权限控制
- **数据保护**: 敏感数据必须加密传输和存储

---

## 🔗 URL设计规范

### 基础URL结构
```
https://api.ioe-dream.com/{version}/{service}/{resource}[/{id}][/{sub-resource}]
```

### 版本管理规范
```
版本策略:
- v0: 兼容性版本，用于过渡期
- v1: 当前稳定版本
- v2: 下一个主要版本

URL示例:
https://api.ioe-dream.com/v1/consume/accounts
https://api.ioe-dream.com/v1/access/permissions
https://api.ioe-dream.com/v0/legacy-endpoint
```

### 资源命名规范
```
资源命名规则:
- 使用名词复数形式
- 使用小写字母和连字符
- 避免缩写，使用完整单词

示例:
- /v1/consume/accounts (消费账户)
- /v1/access/permissions (门禁权限)
- /v1/attendance/records (考勤记录)
- /v1/devices/cameras (摄像头设备)
```

### HTTP方法规范
```
GET    /v1/consume/accounts           # 获取账户列表
GET    /v1/consume/accounts/{id}      # 获取单个账户
POST   /v1/consume/accounts           # 创建账户
PUT    /v1/consume/accounts/{id}      # 更新账户(完整更新)
PATCH  /v1/consume/accounts/{id}      # 更新账户(部分更新)
DELETE /v1/consume/accounts/{id}      # 删除账户
```

---

## 📨 请求设计规范

### 请求头规范
```http
# 标准请求头
Content-Type: application/json
Accept: application/json
Authorization: Bearer {jwt_token}
X-API-Version: v1
X-Trace-Id: {trace_id}
X-Client-Id: {client_id}
```

### 请求参数规范
```
# 查询参数
GET /v1/consume/accounts?page=1&size=20&sort=createTime,desc

# 路径参数
GET /v1/consume/accounts/{accountId}

# 请求体参数
POST /v1/consume/accounts
{
  "accountNumber": "CARD001",
  "userId": 12345,
  "initialBalance": 1000.00
}
```

### 请求体验证规范
```java
@RestController
@RequestMapping("/v1/consume")
@Validated
public class ConsumeControllerV1 {

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountVO>> createAccount(
            @RequestBody @Valid CreateAccountRequest request) {
        // 实现
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<PageResult<AccountVO>>> getAccounts(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Range(min = 1, max = 100) int size,
            @RequestParam(required = false) String accountNumber) {
        // 实现
    }
}
```

---

## 📬 响应设计规范

### 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 响应数据
  },
  "timestamp": "2025-11-26T12:00:00Z",
  "traceId": "trace-123456789",
  "success": true
}
```

### 成功响应示例
```json
// 单个资源响应
{
  "code": 200,
  "message": "success",
  "data": {
    "accountId": 12345,
    "accountNumber": "CARD001",
    "balance": 1000.00,
    "status": "ACTIVE",
    "createTime": "2025-11-26T12:00:00Z"
  },
  "timestamp": "2025-11-26T12:00:00Z",
  "traceId": "trace-123456789",
  "success": true
}

// 分页响应
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "accountId": 12345,
        "accountNumber": "CARD001"
      }
    ],
    "page": 1,
    "size": 20,
    "total": 100,
    "totalPages": 5
  },
  "timestamp": "2025-11-26T12:00:00Z",
  "traceId": "trace-123456789",
  "success": true
}
```

### 错误响应规范
```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": {
    "field": "accountNumber",
    "message": "账户号不能为空",
    "rejectedValue": null
  },
  "timestamp": "2025-11-26T12:00:00Z",
  "traceId": "trace-123456789",
  "success": false,
  "errors": [
    {
      "field": "balance",
      "message": "余额必须大于0",
      "rejectedValue": -100
    }
  ]
}
```

### HTTP状态码规范
```
成功状态码:
200 OK          - 请求成功
201 Created     - 资源创建成功
204 No Content  - 请求成功但无返回内容

客户端错误状态码:
400 Bad Request        - 请求参数错误
401 Unauthorized       - 未授权
403 Forbidden          - 权限不足
404 Not Found          - 资源不存在
409 Conflict           - 资源冲突
422 Unprocessable Entity - 请求格式正确但语义错误

服务器错误状态码:
500 Internal Server Error - 服务器内部错误
502 Bad Gateway           - 网关错误
503 Service Unavailable   - 服务不可用
```

---

## 🔐 安全规范

### 认证机制
```java
@Configuration
@EnableWebSecurity
public class ApiSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v0/**").permitAll()  // v0版本兼容性
                .requestMatchers("/v1/auth/**").permitAll()
                .requestMatchers("/v1/consume/**").hasRole("USER")
                .requestMatchers("/v2/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .build();
    }
}
```

### 权限控制注解
```java
@RestController
@RequestMapping("/v1/consume")
@PreAuthorize("hasRole('USER')")
public class ConsumeControllerV1 {

    @GetMapping("/accounts")
    @PreAuthorize("hasAuthority('consume:account:read')")
    public ResponseEntity<ApiResponse<List<AccountVO>>> getAccounts() {
        // 实现
    }

    @PostMapping("/consume")
    @PreAuthorize("hasAuthority('consume:create')")
    public ResponseEntity<ApiResponse<ConsumeResultVO>> consume(
            @RequestBody @Valid ConsumeRequestDTO request) {
        // 实现
    }
}
```

### 输入验证规范
```java
public class CreateAccountRequest {

    @NotBlank(message = "账户号不能为空")
    @Size(min = 4, max = 20, message = "账户号长度必须在4-20之间")
    private String accountNumber;

    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须为正数")
    private Long userId;

    @NotNull(message = "初始余额不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "初始余额必须大于0")
    @DecimalMax(value = "10000.00", message = "初始余额不能超过10000")
    private BigDecimal initialBalance;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

---

## 📚 API文档规范

### OpenAPI 3.0规范
```yaml
openapi: 3.0.3
info:
  title: IOE-DREAM 消费服务API
  description: 智慧园区一卡通消费管理API
  version: 1.0.0
  contact:
    name: IOE-DREAM开发团队
    email: dev@ioe-dream.com

servers:
  - url: https://api.ioe-dream.com/v1/consume
    description: 生产环境
  - url: https://dev-api.ioe-dream.com/v1/consume
    description: 开发环境

paths:
  /accounts:
    get:
      summary: 获取账户列表
      description: 分页获取消费账户列表
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            minimum: 1
            default: 1
        - name: size
          in: query
          schema:
            type: integer
            minimum: 1
            maximum: 100
            default: 20
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccountListResponse'

components:
  schemas:
    AccountVO:
      type: object
      properties:
        accountId:
          type: integer
          description: 账户ID
        accountNumber:
          type: string
          description: 账户号
        balance:
          type: number
          format: decimal
          description: 账户余额
        status:
          type: string
          enum: [ACTIVE, FROZEN, CLOSED]
          description: 账户状态
```

### API注释规范
```java
@RestController
@RequestMapping("/v1/consume")
@Tag(name = "消费账户管理", description = "消费账户相关的API")
public class ConsumeControllerV1 {

    @Operation(summary = "获取账户列表",
               description = "分页获取消费账户列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功",
                    content = @Content(schema = @Schema(implementation = AccountListResponse.class))),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<PageResult<AccountVO>>> getAccounts(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每页大小，最大100", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        // 实现
    }
}
```

---

## ⚡ 性能优化规范

### 分页规范
```java
@GetMapping("/accounts")
public ResponseEntity<ApiResponse<PageResult<AccountVO>>> getAccounts(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") @Max(100) int size) {

    // 使用偏移量分页
    Pageable pageable = PageRequest.of(page - 1, size,
        Sort.by(Sort.Direction.DESC, "createTime"));

    Page<AccountEntity> accountPage = accountService.findAll(pageable);

    return ResponseEntity.ok(ApiResponse.success(
        PageResult.from(accountPage)
    ));
}
```

### 缓存规范
```java
@GetMapping("/accounts/{accountId}")
@Cacheable(value = "accounts", key = "#accountId", unless = "#result == null")
public ResponseEntity<ApiResponse<AccountVO>> getAccount(@PathVariable Long accountId) {
    AccountVO account = accountService.getAccountById(accountId);
    return ResponseEntity.ok(ApiResponse.success(account));
}

@PostMapping("/accounts/{accountId}/balance")
@CacheEvict(value = "accounts", key = "#accountId")
public ResponseEntity<ApiResponse<Void>> updateBalance(
        @PathVariable Long accountId,
        @RequestBody @Valid UpdateBalanceRequest request) {
    accountService.updateBalance(accountId, request.getAmount());
    return ResponseEntity.ok(ApiResponse.success());
}
```

### 批量操作规范
```java
@PostMapping("/accounts/batch")
public ResponseEntity<ApiResponse<BatchOperationResult>> batchCreateAccounts(
        @RequestBody @Valid @Size(max = 100, message = "批量操作最多100条")
        List<@Valid CreateAccountRequest> requests) {

    BatchOperationResult result = accountService.batchCreateAccounts(requests);
    return ResponseEntity.ok(ApiResponse.success(result));
}
```

---

## 🧪 测试规范

### API测试示例
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ConsumeControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountNumber("TEST001");
        request.setUserId(12345L);
        request.setInitialBalance(new BigDecimal("1000.00"));

        mockMvc.perform(post("/v1/consume/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("TEST001"));
    }

    @Test
    void shouldReturnValidationErrorForInvalidRequest() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        // 故意不设置必填字段

        mockMvc.perform(post("/v1/consume/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
```

---

## 📋 API设计检查清单

### ✅ 设计阶段检查
- [ ] API资源定义是否清晰明确
- [ ] URL路径是否符合RESTful规范
- [ ] HTTP方法使用是否正确
- [ ] 版本管理策略是否已确定
- [ ] 安全控制措施是否已设计

### ✅ 实现阶段检查
- [ ] 请求参数验证是否完整
- [ ] 响应格式是否统一
- [ ] 错误处理是否完善
- [ ] API文档是否已生成
- [ ] 性能优化措施是否已实施

### ✅ 测试阶段检查
- [ ] 单元测试覆盖率是否达标
- [ ] 集成测试是否通过
- [ ] 安全测试是否完成
- [ ] 性能测试是否满足要求
- [ ] 文档测试是否准确

---

## 🔄 版本更新流程

### API版本升级步骤
1. **设计阶段**: 确定新版本API变更内容
2. **开发阶段**: 实现新版本API，保持旧版本兼容
3. **测试阶段**: 全面测试新旧版本API
4. **文档更新**: 更新API文档和版本说明
5. **发布通知**: 提前通知客户端版本变更
6. **废弃管理**: 逐步废弃旧版本API

### 兼容性保证
```java
// v1版本 - 稳定版本
@GetMapping("/v1/consume/accounts")
public ResponseEntity<ApiResponse<List<AccountVO>>> getAccountsV1() {
    return ResponseEntity.ok(ApiResponse.success(accountService.getAllAccounts()));
}

// v2版本 - 新功能版本
@GetMapping("/v2/consume/accounts")
public ResponseEntity<ApiResponse<PageResult<AccountVO>>> getAccountsV2(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(ApiResponse.success(
        accountService.getAccountsWithPagination(page, size)
    ));
}

// 版本兼容处理
@GetMapping("/consume/accounts")
public ResponseEntity<?> getAccounts(
        @RequestHeader(value = "X-API-Version", defaultValue = "v1") String version) {
    switch (version) {
        case "v1":
            return getAccountsV1();
        case "v2":
            return getAccountsV2(1, 20);
        default:
            throw new UnsupportedVersionException("不支持的API版本: " + version);
    }
}
```

---

**🎯 IOE-DREAM API设计规范 - 统一、安全、高性能的API设计标准**

**本规范文档是IOE-DREAM平台API设计的权威指南，所有API开发工作必须严格遵循。**