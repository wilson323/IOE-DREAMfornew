# 🔌 API设计专家技能

## 技能信息

**技能名称**: API设计专家 (API Design Expert)
**技能等级**: ★★★ 高级
**适用角色**: API架构师、后端开发工程师、系统设计师
**前置技能**: HTTP协议基础、RESTful API设计、JSON/XML格式、微服务架构
**预计学时**: 40小时
**技能认证**: IOE-DREAM API设计专家认证

---

## 🎯 技能概述

API设计专家专注于设计和实现高质量、易用、可维护的API接口。该技能涵盖了RESTful API设计、GraphQL API设计、API文档编写、版本管理、安全性、性能优化等核心能力，确保API接口符合企业级标准。

## 📚 知识要求

### 理论知识

#### 🌐 HTTP协议深度理解
- **HTTP/1.1 vs HTTP/2** 特性和差异
- **HTTP方法语义**: GET, POST, PUT, PATCH, DELETE的正确使用
- **状态码设计**: 2xx, 3xx, 4xx, 5xx的准确含义
- **请求头和响应头**: Cache-Control, Content-Type, Authorization等
- **HTTPS和安全传输**: TLS/SSL证书配置和最佳实践

#### 📋 RESTful API设计原则
- **Richardson成熟度模型**: RESTful API的成熟度评估
- **资源导向设计**: 资源建模和URI设计原则
- **无状态性**: 会话状态管理和无状态设计
- **统一接口**: 统一的接口规范和约定
- **分层系统**: 清晰的API架构层次

#### 🔧 API架构模式
- **API版本控制策略**: URL版本、Header版本、内容协商
- **分页和过滤**: 分页参数、过滤条件、排序规则
- **批量操作**: 批量创建、更新、删除的设计模式
- **异步API**: 异步任务处理和状态查询
- **缓存策略**: HTTP缓存、API缓存、CDN缓存

### 业务理解

#### 💼 IOE-DREAM业务API
- **消费服务API**: 账户管理、余额查询、消费记录、统计分析
- **门禁服务API**: 权限管理、设备控制、通行记录、区域管理
- **考勤服务API**: 考勤规则、排班管理、统计报表、请假申请
- **监控系统API**: 设备管理、视频分析、告警通知、数据采集
- **系统管理API**: 用户管理、角色权限、系统配置、日志管理

#### 🔐 安全与权限API
- **认证授权API**: JWT令牌、OAuth2.0、RBAC权限控制
- **数据安全API**: 加密解密、脱敏处理、审计日志
- **网络安全API**: 限流控制、防护机制、DDoS防护

### 技术背景

#### 🛠️ 技术栈掌握
- **API框架**: Spring Boot, Spring MVC, Spring WebFlux
- **文档工具**: Swagger/OpenAPI 3.0, Postman, Insomnia
- **测试工具**: JUnit, Mockito, REST Assured, Newman
- **监控工具**: Prometheus, Grafana, ELK Stack
- **版本管理**: Git, Maven, Semantic Versioning

#### 📐 数据格式标准
- **JSON**: JSON Schema, 数据验证、错误处理
- **XML**: XML Schema, XSLT转换、命名空间
- **Protocol Buffers**: gRPC接口定义、二进制序列化
- **Form Data**: 文件上传、表单数据处理

---

## 🛠️ 操作技能

### 1. RESTful API设计

#### 资源建模
```java
/**
 * RESTful资源模型示例
 */
@Entity
@Table(name = "t_consume_account")
public class AccountEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(name = "account_number", unique = true, length = 32)
    private String accountNumber;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "balance", precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 关联查询
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", insertable = false, updatable = false)
    private PersonEntity person;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConsumeRecordEntity> consumeRecords;
}

/**
 * RESTful API控制器设计
 */
@RestController
@RequestMapping("/api/v1/consume")
@Api(tags = "消费管理API")
@Validated
public class ConsumeControllerV1 {

    @Autowired
    private AccountService accountService;

    /**
     * 获取账户列表
     * 支持分页、过滤、排序
     */
    @GetMapping("/accounts")
    @ApiOperation(value = "获取账户列表", notes = "支持分页查询和多条件过滤")
    public ResponseEntity<ApiResponse<PageResult<AccountVO>>> getAccounts(
            @Valid @ModelAttribute AccountQueryForm queryForm,
            @Valid @PageableDefault(size = 20) Pageable pageable) {

        PageResult<AccountVO> result = accountService.getAccounts(queryForm, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取账户详情
     */
    @GetMapping("/accounts/{accountId}")
    @ApiOperation(value = "获取账户详情", notes = "根据账户ID获取详细信息")
    public ResponseEntity<ApiResponse<AccountDetailVO>> getAccountDetail(
            @PathVariable @NotNull Long accountId) {

        AccountDetailVO account = accountService.getAccountDetail(accountId);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    /**
     * 创建账户
     */
    @PostMapping("/accounts")
    @ApiOperation(value = "创建账户", notes = "创建新的消费账户")
    public ResponseEntity<ApiResponse<AccountVO>> createAccount(
            @RequestBody @Valid AccountCreateForm createForm) {

        AccountVO account = accountService.createAccount(createForm);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account));
    }

    /**
     * 更新账户
     */
    @PutMapping("/accounts/{accountId}")
    @ApiOperation(value = "更新账户", notes = "更新账户信息")
    public ResponseEntity<ApiResponse<AccountVO>> updateAccount(
            @PathVariable @NotNull Long accountId,
            @RequestBody @Valid AccountUpdateForm updateForm) {

        AccountVO account = accountService.updateAccount(accountId, updateForm);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    /**
     * 删除账户
     */
    @DeleteMapping("/accounts/{accountId}")
    @ApiOperation(value = "删除账户", notes = "软删除指定账户")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable @NotNull Long accountId) {

        accountService.deleteAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 充值账户
     */
    @PostMapping("/accounts/{accountId}/recharge")
    @ApiOperation(value = "账户充值", notes = "向指定账户充值金额")
    public ResponseEntity<ApiResponse<RechargeResultVO>> rechargeAccount(
            @PathVariable @NotNull Long accountId,
            @RequestBody @Valid RechargeForm rechargeForm) {

        RechargeResultVO result = accountService.rechargeAccount(accountId, rechargeForm);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取账户余额
     */
    @GetMapping("/accounts/{accountId}/balance")
    @ApiOperation(value = "获取账户余额", notes = "查询账户当前余额")
    public ResponseEntity<ApiResponse<AccountBalanceVO>> getAccountBalance(
            @PathVariable @NotNull Long accountId) {

        AccountBalanceVO balance = accountService.getAccountBalance(accountId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    /**
     * 获取账户交易记录
     */
    @GetMapping("/accounts/{accountId}/transactions")
    @ApiOperation(value = "获取交易记录", notes = "分页查询账户交易历史记录")
    public ResponseEntity<ApiResponse<PageResult<TransactionVO>>> getAccountTransactions(
            @PathVariable @NotNull Long accountId,
            @Valid @ModelAttribute TransactionQueryForm queryForm,
            @Valid @PageableDefault(size = 20) Pageable pageable) {

        PageResult<TransactionVO> result = accountService.getAccountTransactions(
                accountId, queryForm, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
```

#### API版本控制
```java
/**
 * API版本控制配置
 */
@Configuration
public class ApiVersionConfig {

    /**
     * 版本路由映射
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addRequestHandlers(RequestHandlerMapping registry) {
                // 注册不同版本的处理器
                registry.registerHandler(new V1ConsumeRequestHandler());
                registry.registerHandler(new V2ConsumeRequestHandler());
                registry.registerHandler(new V0ConsumeRequestHandler());
            }
        };
    }
}

/**
 * 版本请求处理器接口
 */
public interface ConsumeRequestHandler {
    Object handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;
}

/**
 * V1版本处理器实现
 */
@Component("v1ConsumeHandler")
public class V1ConsumeRequestHandler implements ConsumeRequestHandler {

    @Autowired
    private AccountServiceV1 accountServiceV1;

    @Override
    public Object handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getRequestURI();

        if (path.startsWith("/api/v1/consume/accounts")) {
            if ("GET".equals(request.getMethod())) {
                return handleGetAccounts(request, response);
            } else if ("POST".equals(request.getMethod())) {
                return handleCreateAccount(request, response);
            }
        }

        // 404 Not Found
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return null;
    }

    private Object handleGetAccounts(HttpServletRequest request, HttpServletResponse response) {
        // V1版本获取账户列表实现
        return accountServiceV1.getAccountsV1(request);
    }

    private Object handleCreateAccount(HttpServletRequest request, HttpServletResponse response) {
        // V1版本创建账户实现
        return accountServiceV1.createAccountV1(request);
    }
}
```

### 2. OpenAPI文档设计

#### OpenAPI规范定义
```yaml
# consume-service-api-v1.yaml
openapi: 3.0.3
info:
  title: IOE-DREAM 消费服务API
  description: 智慧园区一卡通消费管理服务API接口文档
  version: 1.0.0
  contact:
    name: IOE-DREAM开发团队
    email: dev@ioe-dream.com
    url: https://www.ioe-dream.com
  license:
    name: MIT License
    url: https://opensource.org/licenses/MIT

servers:
  - url: https://api.ioe-dream.com/v1/consume
    description: 生产环境
  - url: https://api-test.ioe-dream.com/v1/consume
    description: 测试环境
  - url: https://api-dev.ioe-dream.com/v1/consume
    description: 开发环境

tags:
  - name: 账户管理
    description: 账户创建、查询、更新、删除
  - name: 充值管理
    description: 账户充值、余额查询、充值记录
  - name: 消费管理
    description: 消费记录、统计分析、异常处理
  - name: 权限管理
    description: 账户权限、访问控制、安全验证

paths:
  /accounts:
    get:
      tags:
        - 账户管理
      summary: 获取账户列表
      description: 分页获取账户列表，支持多条件过滤和排序
      parameters:
        - name: page
          in: query
          description: 页码，从1开始
          required: false
          schema:
            type: integer
            minimum: 1
            default: 1
        - name: size
          in: query
          description: 每页记录数，最大100
          required: false
          schema:
            type: integer
            minimum: 1
            maximum: 100
            default: 20
        - name: accountNumber
          in: query
          description: 账户编号模糊查询
          required: false
          schema:
            type: string
            example: "CARD001"
        - name: personName
          in: query
          description: 持有人姓名模糊查询
          required: false
          schema:
            type: string
            example: "张三"
        - name: accountType
          in: query
          description: 账户类型过滤
          required: false
          schema:
            type: string
            enum: [STAFF, STUDENT, VISITOR]
            example: "STAFF"
        - name: status
          in: query
          description: 账户状态过滤
          required: false
          schema:
            type: string
            enum: [ACTIVE, FROZEN, CLOSED]
            example: "ACTIVE"
        - name: sortBy
          in: query
          description: 排序字段
          required: false
          schema:
            type: string
            enum: [createTime, updateTime, balance]
            default: "createTime"
        - name: sortDirection
          in: query
          description: 排序方向
          required: false
          schema:
            type: string
            enum: [asc, desc]
            default: "desc"
      responses:
        '200':
          description: 查询成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccountListResponse'
        '400':
          description: 请求参数错误
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: 未授权
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: 服务器内部错误
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
      security:
        - BearerAuth: []

  /accounts/{accountId}:
    get:
      tags:
        - 账户管理
      summary: 获取账户详情
      description: 根据账户ID获取账户详细信息
      parameters:
        - name: accountId
          in: path
          description: 账户ID
          required: true
          schema:
            type: integer
            format: int64
          example: 1001
      responses:
        '200':
          description: 查询成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccountDetailResponse'
        '404':
          description: 账户不存在
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: 服务器内部错误
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
      security:
        - BearerAuth: []

components:
  schemas:
    AccountListResponse:
      type: object
      properties:
        code:
          type: integer
          format: int32
          example: 200
        message:
          type: string
          example: "查询成功"
        data:
          $ref: '#/components/schemas/AccountListData'
        timestamp:
          type: string
          format: date-time
          example: "2025-01-01T12:00:00Z"

    AccountListData:
      type: object
      properties:
        accounts:
          type: array
          items:
            $ref: '#/components/schemas/AccountVO'
          description: 账户列表
        pagination:
          $ref: '#/components/schemas/PaginationInfo'
          description: 分页信息

    AccountVO:
      type: object
      properties:
        accountId:
          type: integer
          format: int64
          description: 账户ID
          example: 1001
        accountNumber:
          type: string
          description: 账户编号
          example: "CARD001"
        personId:
          type: integer
          format: int64
          description: 人员ID
          example: 10001
        personName:
          type: string
          description: 人员姓名
          example: "张三"
        accountType:
          $ref: '#/components/schemas/AccountType'
          description: 账户类型
        status:
          $ref: '#/components/schemas/AccountStatus'
          description: 账户状态
        balance:
          type: number
          format: decimal
          description: 账户余额
          example: 1000.50
        dailyLimit:
          type: number
          format: decimal
          description: 日消费限额
          example: 500.00
        monthlyLimit:
          type: number
          format: decimal
          description: 月消费限额
          example: 10000.00
        createTime:
          type: string
          format: date-time
          description: 创建时间
          example: "2025-01-01T10:00:00Z"
        updateTime:
          type: string
          format: date-time
          description: 更新时间
          example: "2025-01-01T12:00:00Z"
        tags:
          type: array
          items:
            type: string
          description: 账户标签
          example: ["VIP", "员工"]
      required:
        - accountId
        - accountNumber
        - personId
        - accountType
        - status
        - balance

    AccountType:
      type: string
      enum:
        - STAFF
        - STUDENT
        - VISITOR
        - TEMPORARY
      description: 账户类型枚举
      example: "STAFF"

    AccountStatus:
      type: string
      enum:
        - ACTIVE
        - FROZEN
        - CLOSED
        - EXPIRED
        - SUSPENDED
      description: 账户状态枚举
      example: "ACTIVE"

    PaginationInfo:
      type: object
      properties:
        page:
          type: integer
          description: 当前页码
          example: 1
        size:
          type: integer
          description: 每页大小
          example: 20
        total:
          type: integer
          description: 总记录数
          example: 100
        totalPages:
          type: integer
          description: 总页数
          example: 5
        hasNext:
          type: boolean
          description: 是否有下一页
          example: true
        hasPrevious:
          type: boolean
          description: 是否有上一页
          example: false
      required:
        - page
        - size
        - total
        - totalPages

    ErrorResponse:
      type: object
      properties:
        code:
          type: integer
          format: int32
          description: 错误码
          example: 400
        message:
          type: string
          description: 错误信息
          example: "请求参数错误"
        details:
          type: array
          items:
            type: string
          description: 错误详情
          example: ["账户ID不能为空"]
        timestamp:
          type: string
          format: date-time
          description: 错误时间
          example: "2025-01-01T12:00:00Z"
        traceId:
          type: string
          description: 追踪ID
          example: "abc123def456"
      required:
        - code
        - message

  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT认证令牌
```

### 3. API性能优化

#### 缓存策略实现
```java
/**
 * API响应缓存配置
 */
@Configuration
@EnableCaching
public class ApiCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(buildCaffeineCache());
        return cacheManager;
    }

    private Caffeine<Object, Object> buildCaffeineCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}

/**
 * 账户服务缓存实现
 */
@Service
public class AccountServiceV1 {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存账户详情
     */
    @Cacheable(value = "account:detail", key = "#accountId", unless = "#result == null")
    public AccountDetailVO getAccountDetail(Long accountId) {
        // 先从Redis缓存查询
        String cacheKey = "account:detail:" + accountId;
        AccountDetailVO cached = (AccountDetailVO) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return cached;
        }

        // 数据库查询
        AccountDetailVO account = queryAccountDetailFromDatabase(accountId);

        // 写入缓存
        if (account != null) {
            redisTemplate.opsForValue().set(cacheKey, account, 5, TimeUnit.MINUTES);
        }

        return account;
    }

    /**
     * 清除账户缓存
     */
    @CacheEvict(value = "account:detail", key = "#accountId")
    public void evictAccountCache(Long accountId) {
        String cacheKey = "account:detail:" + accountId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 批量预热缓存
     */
    @Async
    public void warmupAccountCache() {
        List<Long> accountIds = accountRepository.findAllActiveAccountIds();

        for (Long accountId : accountIds) {
            try {
                getAccountDetail(accountId);
            } catch (Exception e) {
                log.warn("预热账户缓存失败: accountId={}, error={}", accountId, e.getMessage());
            }
        }
    }
}

/**
 * HTTP响应压缩配置
 */
@Configuration
public class HttpCompressionConfig {

    @Bean
    public FilterRegistrationBean<GzipFilter> gzipFilterRegistration() {
        FilterRegistrationBean<GzipFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GzipFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("gzipFilter");
        registration.setOrder(1);
        return registration;
    }
}
```

#### 数据库查询优化
```java
/**
 * 账户Repository查询优化
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    /**
     * 优化的账户列表查询
     */
    @Query("SELECT a FROM AccountEntity a " +
           "WHERE (:accountNumber IS NULL OR a.accountNumber LIKE %:accountNumber%) " +
           "AND (:personName IS NULL OR a.person.name LIKE %:personName%) " +
           "AND (:accountType IS NULL OR a.accountType = :accountType) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "ORDER BY a.#{#sortBy} #{#sortDirection}")
    Page<AccountEntity> findAccountsWithConditions(
            @Param("accountNumber") String accountNumber,
            @Param("personName") String personName,
            @Param("accountType") String accountType,
            @Param("status") String status,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection,
            Pageable pageable);

    /**
     * 使用DTO投影避免N+1问题
     */
    @Query("SELECT new net.lab1024.sa.admin.module.consume.domain.projection.AccountProjection(" +
           "a.accountId, a.accountNumber, a.balance, a.status, " +
           "p.name, p.phone, " +
           "a.createTime, a.updateTime) " +
           "FROM AccountEntity a " +
           "LEFT JOIN PersonEntity p ON a.personId = p.id " +
           "WHERE a.accountId IN :accountIds")
    List<AccountProjection> findAccountProjections(@Param("accountIds") List<Long>);

    /**
     * 批量查询优化
     */
    @Query("SELECT a FROM AccountEntity a WHERE a.accountId IN :accountIds")
    List<AccountEntity> findByAccountIdsIn(@Param("accountIds") List<Long>);

    /**
     * 统计查询优化
     */
    @Query("SELECT COUNT(a) FROM AccountEntity a WHERE a.status = :status")
    Long countByStatus(@Param("status") String status);

    /**
     * 聚合查询优化
     */
    @Query("SELECT new net.lab1024.sa.admin.module.consume.domain.dto.AccountStatisticsDTO(" +
           "COUNT(a) as totalCount, " +
           "SUM(a.balance) as totalBalance, " +
           "AVG(a.balance) as avgBalance, " +
           "MAX(a.balance) as maxBalance, " +
           "MIN(a.balance) as minBalance) " +
           "FROM AccountEntity a " +
           "WHERE a.status = 'ACTIVE' " +
           "GROUP BY a.accountType")
    List<AccountStatisticsDTO> getStatisticsByAccountType();
}
```

### 4. API安全性设计

#### 认证授权实现
```java
/**
 * API安全配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ApiSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/consume/health").permitAll()
                .requestMatchers("/api/v1/consume/auth/**").permitAll()
                .requestMatchers("/api/v1/consume/accounts/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/consume/accounts/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/consume/accounts").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/consume/accounts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/consume/accounts/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/consume/privilege/**").hasRole("PRIVILEGE_ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(apiKeyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptions -> authenticationEntryPoint)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter() {
        return new ApiKeyAuthenticationFilter();
    }
}

/**
 * JWT认证过滤器
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

/**
 * API Key认证过滤器
 */
@Component
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String apiKey = getApiKeyFromRequest(request);

            if (StringUtils.hasText(apiKey)) {
                ApiKeyInfo apiKeyInfo = apiKeyService.getApiKeyInfo(apiKey);

                if (apiKeyInfo != null && apiKeyInfo.isActive()) {
                    UserDetails userDetails = createUserDetailsFromApiKey(apiKeyInfo);
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("API Key认证失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getApiKeyFromRequest(HttpServletRequest request) {
        return request.getHeader("X-API-Key");
    }

    private UserDetails createUserDetailsFromApiKey(ApiKeyInfo apiKeyInfo) {
        // 根据API Key信息创建UserDetails
        List<GrantedAuthority> authorities = apiKeyInfo.getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission))
                .collect(Collectors.toList());

        return new User(apiKeyInfo.getApiKey(), "", authorities);
    }
}
```

---

## ⚠️ 注意事项

### 🔒 安全注意事项
- **输入验证**: 所有API输入必须进行严格验证
- **输出过滤**: 敏感数据必须进行脱敏处理
- **访问控制**: 实现细粒度的权限控制
- **审计日志**: 记录所有API访问和操作日志
- **防护措施**: 实现防SQL注入、XSS、CSRF等攻击

### 📊 性能注意事项
- **缓存策略**: 合理使用缓存提高性能
- **数据库优化**: 优化SQL查询，避免N+1问题
- **响应压缩**: 对大体积响应进行压缩
- **连接池**: 合理配置数据库连接池
- **限流控制**: 实现API限流保护系统

### 🔄 版本管理注意事项
- **向后兼容**: 确保API版本向后兼容
- **废弃通知**: 提前通知API废弃计划
- **文档同步**: 保持文档与实现同步更新
- **测试覆盖**: 确保所有版本都有测试覆盖
- **监控告警**: 监控API版本使用情况

---

## 📊 评估标准

### 技术指标
- **API设计**: 符合RESTful原则，OpenAPI文档完整性≥95%
- **响应性能**: API响应时间P95≤200ms，P99≤500ms
- **可用性**: API可用性≥99.9%
- **安全性**: 安全漏洞扫描通过率100%
- **文档质量**: API文档准确性和完整性≥95%

### 用户体验指标
- **易用性**: API接口易用性评分≥8.0
- **一致性**: API接口行为一致性评分≥9.0
- **错误处理**: 错误信息清晰度和指导性评分≥8.0
- **开发者体验**: 开发者使用体验评分≥8.5

### 质量标准
- **代码规范**: 代码规范符合度100%
- **测试覆盖**: API测试覆盖率≥95%
- **监控覆盖**: 关键指标监控覆盖率100%
- **文档维护**: 文档及时更新率≥90%

---

## 🚀 最佳实践

### 设计阶段最佳实践
1. **资源建模**: 基于业务领域进行资源建模
2. **URI设计**: 遵循RESTful URI设计原则
3. **版本策略**: 制定清晰的API版本管理策略
4. **安全设计**: 从设计阶段就考虑安全因素
5. **性能考虑**: 预先考虑性能和扩展性问题

### 开发阶段最佳实践
1. **代码生成**: 使用工具生成API代码和文档
2. **测试驱动**: 编写充分的API测试用例
3. **文档同步**: 保持代码与文档同步更新
4. **错误处理**: 实现统一的错误处理机制
5. **日志记录**: 记录详细的API访问和操作日志

### 维护阶段最佳实践
1. **监控告警**: 建立完善的API监控和告警体系
2. **性能优化**: 持续进行API性能调优
3. **版本升级**: 谨慎进行API版本升级
4. **用户反馈**: 收集和处理用户反馈
5. **文档更新**: 及时更新API文档和示例

---

## 📚 学习资源

### 推荐书籍
- 《RESTful Web APIs》- Leonard Richardson
- 《API Design Patterns》- Arnaud Lauret
- 《Designing Data-Intensive Applications》- Martin Kleppmann
- 《Building Microservices》- Sam Newman

### 在线资源
- [OpenAPI规范](https://swagger.io/specification/)
- [REST API设计指南](https://restfulapi.net/)
- [HTTP状态码参考](https://httpstatuses.io/)
- [API安全最佳实践](https://owasp.org/www-project-api-security/)

### 工具和框架
- [Swagger/OpenAPI](https://swagger.io/tools/)
- [Postman](https://www.postman.com/)
- [Insomnia](https://insomnia.rest/)
- [REST Assured](https://rest-assured.io/)

---

*此技能文档是IOE-DREAM API设计专家的权威指南，提供完整的API设计、实现和优化能力。*