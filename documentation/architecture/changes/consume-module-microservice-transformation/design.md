# 消费模块微服务化架构设计

## 设计概述

本文档详细描述了消费模块微服务化的技术架构设计，包括微服务拆分、API设计、数据架构、安全设计等关键技术决策。

**设计原则**:
- 单一职责原则：每个微服务负责明确的业务边界
- 高内聚低耦合：服务内部高度内聚，服务之间低度耦合
- 渐进式改造：确保改造过程中的业务连续性
- 技术兼容性：保持与现有SmartAdmin v3框架的兼容

## 🏗️ 微服务架构设计

### 服务拆分架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (Spring Cloud Gateway)           │
├─────────────────────────────────────────────────────────────────┤
│                          Service Mesh                           │
│  ┌───────────────┐ ┌───────────────┐ ┌─────────────────────────┐ │
│  │  Account      │ │   Payment     │ │      Device             │ │
│  │  Service      │ │   Service     │ │      Service            │ │
│  │               │ │               │ │                         │ │
│  │ • 账户管理     │ │ • 消费支付     │ │ • 设备管理              │ │
│  │ • 余额管理     │ │ • 退款处理     │ │ • 设备监控              │ │
│  │ • 安全验证     │ │ • 批量支付     │ │ • 参数下发              │ │
│  └───────────────┘ └───────────────┘ └─────────────────────────┘ │
│                                                                  │
│  ┌───────────────┐ ┌─────────────────────────────────────────────┐ │
│  │   Config      │ │            Reconciliation                  │ │
│  │   Service     │ │            Service                         │ │
│  │               │ │                                             │ │
│  │ • 配置管理     │ │ • 数据对账                                    │ │
│  │ • 业务规则     │ │ • 统计分析                                    │ │
│  │ • 限额费率     │ │ • 异常处理                                    │ │
│  └───────────────┘ └─────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                      Infrastructure Layer                       │
│  ┌───────────────┐ ┌───────────────┐ ┌─────────────────────────┐ │
│  │     Nacos     │ │    RocketMQ   │ │      Redis Cluster      │ │
│  │  注册+配置中心  │ │   消息队列     │ │      缓存集群            │ │
│  └───────────────┘ └───────────────┘ └─────────────────────────┘ │
│                                                                  │
│  ┌───────────────┐ ┌───────────────┐ ┌─────────────────────────┐ │
│  │  MySQL Cluster│ │  Elasticsearch │ │      Prometheus         │ │
│  │  数据库集群     │ │   日志搜索     │ │      监控收集            │ │
│  └───────────────┘ └───────────────┘ └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 核心微服务设计

#### 1. 账户管理服务 (consume-account-service)

**服务职责**:
- 消费账户的全生命周期管理
- 账户余额和信用额度管理
- 账户安全验证和权限控制
- 账户交易记录和审计

**技术架构**:
```
┌─────────────────────────────────────────────┐
│                Controller Layer             │
│  • AccountController                        │
│  • BalanceController                        │
│  • SecurityController                       │
├─────────────────────────────────────────────┤
│                Service Layer                │
│  • AccountService                           │
│  • BalanceService                           │
│  • SecurityService                          │
│  • TransactionService                       │
├─────────────────────────────────────────────┤
│                Manager Layer                │
│  • AccountManager                           │
│  • BalanceManager                           │
│  • RiskControlManager                       │
├─────────────────────────────────────────────┤
│                DAO Layer                    │
│  • AccountDao                               │
│  • BalanceLogDao                            │
│  • TransactionDao                           │
├─────────────────────────────────────────────┤
│              Database Layer                 │
│  • consume_account_db                       │
│  • Tables: t_consume_account               │
│           t_account_transaction            │
│           t_account_balance_log            │
└─────────────────────────────────────────────┘
```

#### 2. 支付处理服务 (consume-payment-service)

**服务职责**:
- 六种消费模式的支付处理
- 支付授权和资金冻结
- 退款撤销处理
- 批量支付和清算

**支付引擎设计**:
```
PaymentEngine (策略模式)
├── FixedAmountPaymentEngine      # 固定金额支付
├── FreeAmountPaymentEngine       # 自由金额支付
├── MeteringPaymentEngine         # 计量支付
├── ProductPaymentEngine          # 商品支付
├── SmartPaymentEngine           # 智能支付
└── OrderingPaymentEngine        # 订餐支付

支付流程:
1. 支付请求 → 参数验证 → 账户验证
2. 余额检查 → 风险控制 → 资金冻结
3. 支付处理 → 状态更新 → 通知发送
4. 异步清算 → 记账处理 → 对账准备
```

#### 3. 设备管理服务 (consume-device-service)

**服务职责**:
- 消费终端设备的注册和配置
- 设备状态实时监控和故障处理
- 设备参数远程下发和控制
- 设备权限管理和访问控制

**设备管理架构**:
```
Device Management
├── Device Registration     # 设备注册
├── Configuration Management # 配置管理
├── Status Monitoring       # 状态监控
├── Fault Handling         # 故障处理
└── Permission Control     # 权限控制

设备通信协议:
├── HTTP/HTTPS             # 标准协议
├── WebSocket             # 实时通信
├── TCP/IP                # 设备直连
└── MQTT                  # IoT设备
```

## 🌐 API设计规范

### RESTful API设计

#### 统一响应格式
```java
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": "2025-11-27T10:30:00Z",
  "traceId": "trace-123456789"
}

// 失败响应
{
  "code": 400,
  "message": "参数验证失败",
  "data": null,
  "timestamp": "2025-11-27T10:30:00Z",
  "traceId": "trace-123456789"
}
```

#### API版本管理
- **URL版本控制**: `/api/v1/account`, `/api/v2/account`
- **向后兼容**: 保持至少一个版本的向后兼容
- **废弃通知**: 提前3个月通知API废弃

### 核心API设计

#### 1. 账户管理API

```yaml
# 账户创建
POST /api/v1/account
Content-Type: application/json
Request:
  personId: Long
  accountType: String
  initialBalance: BigDecimal
  securityConfig: AccountSecurityConfig

Response:
  accountId: Long
  accountNo: String
  status: String
  createTime: DateTime

# 余额查询
GET /api/v1/account/{accountId}/balance
Response:
  accountId: Long
  balance: BigDecimal
  frozenAmount: BigDecimal
  availableAmount: BigDecimal
  creditLimit: BigDecimal
  updateTime: DateTime

# 账户充值
POST /api/v1/account/{accountId}/recharge
Content-Type: application/json
Request:
  amount: BigDecimal
  paymentMethod: String
  remark: String

Response:
  transactionId: String
  newBalance: BigDecimal
  status: String
```

#### 2. 支付处理API

```yaml
# 消费支付
POST /api/v1/payment/consume
Content-Type: application/json
Request:
  personId: Long
  deviceId: Long
  consumeMode: String
  amount: BigDecimal
  products: ProductInfo[]  # 商品模式使用

Response:
  paymentId: String
  status: String
  paidAmount: BigDecimal
  remainingBalance: BigDecimal
  consumeTime: DateTime

# 支付查询
GET /api/v1/payment/{paymentId}
Response:
  paymentId: String
  personId: Long
  personName: String
  amount: BigDecimal
  status: String
  deviceId: Long
  consumeTime: DateTime
  refundStatus: String

# 批量支付
POST /api/v1/payment/batch
Content-Type: application/json
Request:
  payments: PaymentRequest[]

Response:
  batchId: String
  totalCount: Integer
  successCount: Integer
  failureCount: Integer
  results: PaymentResult[]
```

#### 3. 设备管理API

```yaml
# 设备注册
POST /api/v1/device/register
Content-Type: application/json
Request:
  deviceNo: String
  deviceType: String
  deviceModel: String
  location: String
  configParams: Map<String, Object>

Response:
  deviceId: Long
  deviceToken: String
  status: String
  registerTime: DateTime

# 设备状态上报
POST /api/v1/device/{deviceId}/status
Content-Type: application/json
Request:
  status: String
  lastOnlineTime: DateTime
  errorCodes: String[]
  metrics: DeviceMetrics

Response:
  success: Boolean
  message: String

# 设备配置下发
PUT /api/v1/device/{deviceId}/config
Content-Type: application/json
Request:
  configVersion: String
  configParams: Map<String, Object>
  effectiveTime: DateTime

Response:
  configId: String
  status: String
  updateTime: DateTime
```

### API安全设计

#### 认证机制
```java
// JWT Token认证
@Component
public class JwtAuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                        FilterChain chain) {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("Authorization");

        // 验证JWT Token
        if (jwtUtil.validateToken(token)) {
            // 设置用户信息到上下文
            UserContext.setUserId(jwtUtil.getUserId(token));
            UserContext.setPermissions(jwtUtil.getPermissions(token));
        }

        chain.doFilter(request, response);
    }
}
```

#### 权限控制
```java
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    @PostMapping("/create")
    @SaCheckPermission("consume:account:create")
    public ResponseDTO<AccountVO> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        // 创建账户逻辑
    }

    @GetMapping("/{accountId}/balance")
    @SaCheckPermission("consume:account:read")
    public ResponseDTO<BalanceVO> getBalance(@PathVariable Long accountId) {
        // 查询余额逻辑
    }
}
```

## 🗄️ 数据架构设计

### 数据库分库策略

#### 分库原则
1. **业务边界**: 按微服务业务边界分库
2. **数据关联**: 强关联数据放在同一库
3. **查询性能**: 高频查询数据本地化
4. **扩展性**: 支持后续水平分片

#### 数据库设计

##### 1. consume_account_db (账户数据库)

```sql
-- 消费账户主表
CREATE TABLE t_consume_account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    person_id BIGINT NOT NULL COMMENT '人员ID',
    account_no VARCHAR(32) UNIQUE NOT NULL COMMENT '账户编号',
    account_type VARCHAR(20) NOT NULL COMMENT '账户类型',
    balance DECIMAL(15,2) DEFAULT 0.00 COMMENT '账户余额',
    frozen_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '冻结金额',
    credit_limit DECIMAL(15,2) DEFAULT 0.00 COMMENT '信用额度',
    status VARCHAR(20) NOT NULL COMMENT '账户状态',
    security_level VARCHAR(20) DEFAULT 'NORMAL' COMMENT '安全等级',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0,
    version INT DEFAULT 1,
    INDEX idx_person_id (person_id),
    INDEX idx_account_no (account_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费账户表';

-- 账户交易流水表
CREATE TABLE t_account_transaction (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL COMMENT '账户ID',
    transaction_no VARCHAR(32) UNIQUE NOT NULL COMMENT '交易流水号',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易类型',
    amount DECIMAL(15,2) NOT NULL COMMENT '交易金额',
    balance_before DECIMAL(15,2) NOT NULL COMMENT '交易前余额',
    balance_after DECIMAL(15,2) NOT NULL COMMENT '交易后余额',
    related_order_no VARCHAR(32) COMMENT '关联订单号',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account_id (account_id),
    INDEX idx_transaction_no (transaction_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户交易流水表';
```

##### 2. consume_payment_db (支付数据库)

```sql
-- 消费记录主表
CREATE TABLE t_consume_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id VARCHAR(32) UNIQUE NOT NULL COMMENT '支付ID',
    person_id BIGINT NOT NULL COMMENT '人员ID',
    person_name VARCHAR(50) NOT NULL COMMENT '人员姓名',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    consume_mode VARCHAR(20) NOT NULL COMMENT '消费模式',
    amount DECIMAL(15,2) NOT NULL COMMENT '消费金额',
    paid_amount DECIMAL(15,2) NOT NULL COMMENT '实付金额',
    discount_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '优惠金额',
    status VARCHAR(20) NOT NULL COMMENT '支付状态',
    refund_status VARCHAR(20) DEFAULT 'NONE' COMMENT '退款状态',
    consume_time DATETIME NOT NULL COMMENT '消费时间',
    extend_data JSON COMMENT '扩展数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_person_id (person_id),
    INDEX idx_device_id (device_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_consume_time (consume_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录表';

-- 支付订单表
CREATE TABLE t_payment_order (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) UNIQUE NOT NULL COMMENT '订单号',
    payment_id VARCHAR(32) NOT NULL COMMENT '支付ID',
    person_id BIGINT NOT NULL COMMENT '人员ID',
    order_type VARCHAR(20) NOT NULL COMMENT '订单类型',
    total_amount DECIMAL(15,2) NOT NULL COMMENT '订单总金额',
    paid_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '已付金额',
    status VARCHAR(20) NOT NULL COMMENT '订单状态',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_payment_id (payment_id),
    INDEX idx_person_id (person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';
```

##### 3. consume_device_db (设备数据库)

```sql
-- 设备配置主表
CREATE TABLE t_consume_device_config (
    device_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_no VARCHAR(32) UNIQUE NOT NULL COMMENT '设备编号',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型',
    device_model VARCHAR(50) COMMENT '设备型号',
    location VARCHAR(200) COMMENT '设备位置',
    region_id BIGINT COMMENT '区域ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    mac_address VARCHAR(50) COMMENT 'MAC地址',
    supported_modes VARCHAR(200) COMMENT '支持的模式',
    default_mode VARCHAR(20) COMMENT '默认模式',
    status VARCHAR(20) NOT NULL COMMENT '设备状态',
    last_online_time DATETIME COMMENT '最后在线时间',
    config_params JSON COMMENT '设备配置参数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_device_no (device_no),
    INDEX idx_device_type (device_type),
    INDEX idx_region_id (region_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费设备配置表';

-- 设备状态日志表
CREATE TABLE t_device_status_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT NOT NULL COMMENT '设备ID',
    status VARCHAR(20) NOT NULL COMMENT '设备状态',
    error_codes VARCHAR(200) COMMENT '错误代码',
    cpu_usage DECIMAL(5,2) COMMENT 'CPU使用率',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率',
    disk_usage DECIMAL(5,2) COMMENT '磁盘使用率',
    network_status VARCHAR(20) COMMENT '网络状态',
    log_time DATETIME NOT NULL COMMENT '日志时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_log_time (log_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备状态日志表';
```

### 数据一致性设计

#### 分布式事务
```java
@Service
@Transactional
public class PaymentService {

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Autowired
    private PaymentOrderService paymentOrderService;

    /**
     * 消费支付处理 - 分布式事务
     */
    @GlobalTransactional  // Seata分布式事务
    public PaymentResult processPayment(PaymentRequest request) {
        try {
            // 1. 创建支付订单
            PaymentOrder order = paymentOrderService.createOrder(request);

            // 2. 调用账户服务扣款
            AccountDebitRequest debitRequest = new AccountDebitRequest();
            debitRequest.setAccountId(request.getAccountId());
            debitRequest.setAmount(request.getAmount());
            debitRequest.setOrderNo(order.getOrderNo());

            AccountDebitResponse debitResponse = accountServiceClient.debit(debitRequest);

            // 3. 更新支付订单状态
            paymentOrderService.updateOrderStatus(order.getOrderId(),
                                                debitResponse.isSuccess() ?
                                                "SUCCESS" : "FAILED");

            return new PaymentResult(order, debitResponse.isSuccess());

        } catch (Exception e) {
            // Seata自动回滚所有分支事务
            throw new PaymentException("支付处理失败", e);
        }
    }
}
```

#### 幂等性控制
```java
@Component
public class IdempotencyService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查并设置幂等标识
     */
    public boolean checkAndSetIdempotent(String businessKey, long expireTime) {
        String key = "idempotent:" + businessKey;

        // 使用Redis原子操作保证幂等
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, "1", expireTime, TimeUnit.SECONDS);

        return Boolean.TRUE.equals(success);
    }

    /**
     * 删除幂等标识
     */
    public void deleteIdempotent(String businessKey) {
        String key = "idempotent:" + businessKey;
        redisTemplate.delete(key);
    }
}
```

## 🔄 服务间通信设计

### 同步通信

#### OpenFeign客户端
```java
@FeignClient(name = "consume-account-service",
             path = "/api/v1/account",
             configuration = FeignConfiguration.class)
public interface AccountServiceClient {

    @GetMapping("/{accountId}/balance")
    ResponseDTO<BalanceVO> getBalance(@PathVariable("accountId") Long accountId);

    @PostMapping("/{accountId}/debit")
    ResponseDTO<AccountDebitResponse> debit(@PathVariable("accountId") Long accountId,
                                           @RequestBody AccountDebitRequest request);

    @PostMapping("/{accountId}/freeze")
    ResponseDTO<Boolean> freezeAmount(@PathVariable("accountId") Long accountId,
                                      @RequestBody FreezeAmountRequest request);
}
```

#### 服务调用配置
```yaml
# application.yml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: basic
      consume-account-service:
        connectTimeout: 3000
        readTimeout: 8000

  httpclient:
    enabled: true
    max-connections: 200
    max-connections-per-route: 50

  compression:
    request:
      enabled: true
      mime-types: text/xml,application/json
      min-request-size: 2048
    response:
      enabled: true
```

### 异步通信

#### 消息队列配置
```java
@Configuration
@EnableRabbitMQ
public class MQConfig {

    // 支付结果通知队列
    @Bean
    public Queue paymentResultQueue() {
        return QueueBuilder
            .durable("consume.payment.result.queue")
            .build();
    }

    // 设备状态变更队列
    @Bean
    public TopicExchange deviceStatusExchange() {
        return new TopicExchange("consume.device.status.exchange");
    }

    // 绑定关系
    @Bean
    public Binding deviceStatusBinding() {
        return BindingBuilder
            .bind(deviceStatusQueue())
            .to(deviceStatusExchange())
            .with("device.status.*");
    }
}
```

#### 消息发送
```java
@Component
public class PaymentMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送支付结果通知
     */
    public void sendPaymentResult(PaymentResultMessage message) {
        rabbitTemplate.convertAndSend(
            "consume.payment.result.exchange",
            "payment.result.success",
            message,
            correlationData -> {
                correlationData.setId(message.getPaymentId());
            }
        );
    }

    /**
     * 发送设备状态变更通知
     */
    public void sendDeviceStatusChange(DeviceStatusMessage message) {
        rabbitTemplate.convertAndSend(
            "consume.device.status.exchange",
            "device.status." + message.getStatus().toLowerCase(),
            message
        );
    }
}
```

#### 消息消费
```java
@Component
@RabbitListener(queues = "consume.payment.result.queue")
public class PaymentMessageConsumer {

    /**
     * 处理支付结果消息
     */
    @RabbitHandler
    public void handlePaymentResult(PaymentResultMessage message) {
        try {
            // 更新支付记录状态
            paymentRecordService.updatePaymentStatus(
                message.getPaymentId(),
                message.getStatus()
            );

            // 发送用户通知
            if (message.isSuccess()) {
                notificationService.sendPaymentSuccessNotification(message);
            } else {
                notificationService.sendPaymentFailureNotification(message);
            }

        } catch (Exception e) {
            log.error("处理支付结果消息失败: {}", message, e);
            throw new MessageRejectedException(e);
        }
    }
}
```

## 🔐 安全架构设计

### 认证授权

#### OAuth2配置
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/device/**").hasRole("DEVICE")
                .requestMatchers("/api/v1/payment/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri()).build();
    }
}
```

#### API权限控制
```java
@Aspect
@Component
public class PermissionCheckAspect {

    @Around("@annotation(permissionCheck)")
    public Object checkPermission(ProceedingJoinPoint joinPoint,
                                 PermissionCheck permissionCheck) throws Throwable {

        // 获取当前用户权限
        Set<String> userPermissions = SecurityContextHolder.getContext()
            .getAuthentication().getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        // 检查权限
        String requiredPermission = permissionCheck.value();
        if (!userPermissions.contains(requiredPermission)) {
            throw new AccessDeniedException("权限不足: " + requiredPermission);
        }

        return joinPoint.proceed();
    }
}
```

### 数据加密

#### 敏感数据加密
```java
@Component
public class DataEncryptionService {

    @Value("${encryption.key}")
    private String encryptionKey;

    /**
     * 加密敏感数据
     */
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128,
                                                          generateIV().getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new EncryptionException("数据加密失败", e);
        }
    }

    /**
     * 解密敏感数据
     */
    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128,
                                                          generateIV().getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));

            return new String(decrypted);

        } catch (Exception e) {
            throw new EncryptionException("数据解密失败", e);
        }
    }
}
```

## 📊 监控和运维设计

### 健康检查
```java
@Component
public class HealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            // 检查数据库连接
            try (Connection conn = dataSource.getConnection()) {
                if (!conn.isValid(5)) {
                    return Health.down().withDetail("database", "连接失败").build();
                }
            }

            // 检查Redis连接
            redisTemplate.opsForValue().set("health:check", "ok", 10, TimeUnit.SECONDS);

            return Health.up()
                .withDetail("database", "正常")
                .withDetail("redis", "正常")
                .build();

        } catch (Exception e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
```

### 指标监控
```java
@Component
public class MetricsCollector {

    private final Counter paymentCounter;
    private final Timer paymentTimer;
    private final Gauge accountBalanceGauge;

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.paymentCounter = Counter.builder("consume.payment.count")
            .description("支付次数")
            .register(meterRegistry);

        this.paymentTimer = Timer.builder("consume.payment.duration")
            .description("支付耗时")
            .register(meterRegistry);

        this.accountBalanceGauge = Gauge.builder("consume.account.balance")
            .description("账户余额")
            .register(meterRegistry, this, MetricsCollector::getTotalBalance);
    }

    public void recordPayment() {
        paymentCounter.increment();
    }

    public Timer.Sample startPaymentTimer() {
        return Timer.start();
    }

    private double getTotalBalance() {
        // 获取所有账户总余额的逻辑
        return accountService.getTotalBalance();
    }
}
```

---

**设计版本**: v1.0
**创建日期**: 2025-11-27
**最后更新**: 2025-11-27
**设计状态**: 待评审