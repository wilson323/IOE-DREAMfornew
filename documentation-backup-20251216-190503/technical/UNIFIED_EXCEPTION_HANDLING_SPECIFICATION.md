# IOE-DREAM 统一异常处理架构规范

**版本**: v1.0.0
**生效日期**: 2025-12-16
**适用范围**: IOE-DREAM所有微服务
**规范级别**: 强制执行
**制定依据**: Spring Boot 3.5 + 企业级微服务架构最佳实践

---

## 📋 规范概述

### 1. 背景与目标

**现状分析**:
- IOE-DREAM项目已存在完善的GlobalExceptionHandler（位于common-service）
- 各微服务异常处理使用不一致，存在重复实现
- 容错配置分散，缺乏统一管理
- 缺少TraceId追踪和审计日志

**规范目标**:
- ✅ **统一异常处理**: 全局使用common-service中的GlobalExceptionHandler
- ✅ **容错集成**: 异常处理与Resilience4j深度集成
- ✅ **配置中心化**: 通过Nacos统一管理异常和容错配置
- ✅ **可观测性**: 完整的TraceId追踪和异常监控
- ✅ **企业级特性**: 错误码标准化、审计日志、国际化支持

### 2. 架构设计原则

**核心原则**:
1. **集中统一**: 所有微服务统一使用common-service的GlobalExceptionHandler
2. **分类明确**: 业务异常、系统异常、参数异常分类处理
3. **容错集成**: 与Resilience4j、降级机制无缝集成
4. **可观测性**: TraceId追踪、指标监控、审计日志
5. **用户友好**: 对外提供友好的错误信息，对内记录详细错误

**技术架构**:
```
┌─────────────────────────────────────────────────────────────┐
│                    各业务微服务                                │
├─────────────────────────────────────────────────────────────┤
│  Controller → Service → Manager → DAO                      │
│     ↓           ↓          ↓          ↓                      │
│  业务逻辑    业务异常    系统异常    数据异常                      │
│     ↓           ↓          ↓          ↓                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              GlobalExceptionHandler                      │   │
│  │                 (common-service)                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│     ↓           ↓          ↓          ↓                      │
│  Resilience4j  TraceId    审计日志     监控指标                     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 实施规范

### 1. 依赖关系管理

**强制依赖配置**:
```xml
<!-- 在每个业务微服务的pom.xml中必须添加 -->
<dependency>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>ioedream-common-service</artifactId>
    <version>1.0.0</version>
</dependency>
```

**检查清单**:
- [ ] 每个微服务都依赖common-service
- [ ] 版本号统一为1.0.0
- [ ] 排除重复的异常处理实现

### 2. 异常分类标准

**2.1 BusinessException（业务异常）**
```java
/**
 * 业务异常（可预期，用户可理解）
 * 用于处理业务逻辑中的预期错误，如参数验证失败、业务规则冲突等
 */
public class BusinessException extends RuntimeException {
    private String code;      // 业务错误码：USER_NOT_FOUND, INVALID_AMOUNT
    private String message;   // 用户友好的错误信息

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
```

**2.2 SystemException（系统异常）**
```java
/**
 * 系统异常（不可预期，需要运维介入）
 * 用于处理系统级错误，如数据库连接失败、网络超时、第三方服务错误等
 */
public class SystemException extends RuntimeException {
    private String code;      // 系统错误码：DATABASE_ERROR, NETWORK_ERROR
    private String message;   // 错误信息
    private Throwable cause;  // 原始异常

    public SystemException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.cause = cause;
    }
}
```

**2.3 ParamException（参数异常）**
```java
/**
 * 参数异常（请求参数问题）
 * 用于处理HTTP请求参数相关的错误
 */
public class ParamException extends RuntimeException {
    private String code;      // 参数错误码：PARAM_INVALID, PARAM_MISSING
    private String message;   // 错误信息

    public ParamException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
```

### 3. 统一异常处理使用规范

**3.1 Controller层异常处理**
```java
@RestController
@RequestMapping("/api/v1/consume")
@Tag(name = "消费管理")
public class ConsumeController {

    @Resource
    private ConsumeService consumeService;

    // ✅ 正确示例：Controller层不处理异常，直接抛出
    @PostMapping("/consume")
    @Observed(name = "consume-api")
    @CircuitBreaker(name = "consume-service")
    @Retry(name = "consume-service")
    @RateLimiter(name = "consume-service")
    public ResponseDTO<ConsumeResultDTO> consume(@Valid @RequestBody ConsumeRequestDTO request) {
        // 不使用try-catch吞噬异常
        return consumeService.consume(request);
    }

    // ❌ 错误示例：Controller层捕获异常
    @PostMapping("/consume-error")
    public ResponseDTO<ConsumeResultDTO> consumeError(@RequestBody ConsumeRequestDTO request) {
        try {
            return consumeService.consume(request);
        } catch (Exception e) {  // 禁止！异常应向上传播到GlobalExceptionHandler
            log.error("消费失败", e);
            return ResponseDTO.error("CONSUME_ERROR", "消费失败");
        }
    }
}
```

**3.2 Service层异常处理**
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private ConsumeManager consumeManager;

    @Override
    public ResponseDTO<ConsumeResultDTO> consume(ConsumeRequestDTO request) {
        // ✅ 业务验证失败，抛出业务异常
        if (request.getAccountId() == null) {
            throw new BusinessException("ACCOUNT_REQUIRED", "账户ID不能为空");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "消费金额必须大于0");
        }

        // ✅ 系统错误，抛出系统异常
        try {
            ConsumeResultDTO result = consumeManager.executeConsume(request);
            return ResponseDTO.ok(result);
        } catch (DatabaseException e) {
            throw new SystemException("DATABASE_ERROR", "数据库操作失败", e);
        } catch (ExternalServiceException e) {
            throw new SystemException("EXTERNAL_SERVICE_ERROR", "外部服务调用失败", e);
        }
    }
}
```

**3.3 Manager层异常处理**
```java
@Component
public class ConsumeManagerImpl implements ConsumeManager {

    @Resource
    private AccountDao accountDao;

    @Override
    public ConsumeResultDTO executeConsume(ConsumeRequestDTO request) {
        // ✅ 数据访问异常直接抛出，由上层包装
        AccountEntity account = accountDao.selectById(request.getAccountId());
        if (account == null) {
            throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        }

        // ✅ 业务逻辑验证失败，抛出业务异常
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "账户余额不足");
        }

        // 执行消费逻辑
        return processConsume(account, request);
    }
}
```

### 4. 统一异常响应格式

**4.1 错误响应DTO**
```java
@Data
@Builder
public class ErrorResponseDTO {
    private String traceId;       // 追踪ID（用于日志关联）
    private String code;          // 业务错误码
    private String message;       // 用户友好的错误描述
    private String module;        // 错误模块
    private Long timestamp;       // 错误时间戳
    private Object details;       // 详细信息（可选，仅内部使用）
}
```

**4.2 统一响应格式示例**
```json
// 业务异常响应
{
  "code": 400,
  "message": "ACCOUNT_NOT_FOUND",
  "data": {
    "traceId": "550e8400-e29b-41d4-a716-446655440000",
    "code": "ACCOUNT_NOT_FOUND",
    "message": "账户不存在",
    "module": "consume",
    "timestamp": 1701234567890
  }
}

// 系统异常响应
{
  "code": 500,
  "message": "系统繁忙，请稍后重试",
  "data": {
    "traceId": "550e8400-e29b-41d4-a716-446655440001",
    "code": "DATABASE_ERROR",
    "message": "系统繁忙，请稍后重试",
    "module": "consume",
    "timestamp": 1701234567890,
    "details": "Connection timeout:数据库连接超时"
  }
}
```

### 5. 容错机制集成

**5.1 Resilience4j与异常处理集成**
```java
@Service
public class ExternalServiceClient {

    @Resource
    private ExternalService externalService;

    @Retry(name = "external-service", fallbackMethod = "fallback")
    @CircuitBreaker(name = "external-service")
    @RateLimiter(name = "external-service")
    @Bulkhead(name = "external-service")
    public ResponseDTO<String> callExternal(RequestDTO request) {
        try {
            return externalService.call(request);
        } catch (BusinessException e) {
            // ✅ 业务异常直接向上抛，由GlobalExceptionHandler处理
            log.warn("[外部服务] 业务异常, traceId={}, code={}", MDC.get("traceId"), e.getCode());
            throw e;
        } catch (Exception e) {
            // ✅ 系统异常包装后向上抛
            log.error("[外部服务] 系统异常, traceId={}", MDC.get("traceId"), e);
            throw new SystemException("EXTERNAL_CALL_FAILED", "外部服务调用失败", e);
        }
    }

    public ResponseDTO<String> fallback(RequestDTO request, Exception e) {
        String traceId = MDC.get("traceId");
        log.warn("[降级] 外部服务调用失败，使用降级方案, traceId={}", traceId, e);

        // 降级响应也可以抛出业务异常
        if (isBusinessCritical(request)) {
            throw new BusinessException("SERVICE_UNAVAILABLE", "关键服务不可用");
        }

        return ResponseDTO.ok("降级响应");
    }

    private boolean isBusinessCritical(RequestDTO request) {
        // 判断是否为关键业务请求
        return "critical".equals(request.getType());
    }
}
```

**5.2 Resilience4j配置中心化**
```yaml
# Nacos配置中心：resilience4j-common.yml
resilience4j:
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1000ms
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.net.SocketTimeoutException
          - java.io.IOException
          - org.springframework.web.client.HttpServerErrorException
        ignoreExceptions:
          - net.lab1024.sa.common.exception.BusinessException
    instances:
      external-service:
        baseConfig: default
        maxAttempts: 5
        waitDuration: 2000ms
      consume-service:
        baseConfig: default
        maxAttempts: 2

  circuitbreaker:
    configs:
      default:
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        slidingWindowSize: 100
        minimumNumberOfCalls: 10
        recordExceptionPredicate: com.example.exception.BusinessExceptionPredicate
    instances:
      external-service:
        baseConfig: default
        failureRateThreshold: 30
        waitDurationInOpenState: 30s

  ratelimiter:
    configs:
      default:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
    instances:
      consume-api:
        baseConfig: default
        limitForPeriod: 5
      external-service:
        baseConfig: default
        limitForPeriod: 2

  bulkhead:
    configs:
      default:
        maxConcurrentCalls: 10
        maxWaitDuration: 1000ms
```

### 6. GlobalExceptionHandler实现

**核心实现位于**: `ioedream-common-service/src/main/java/net/lab1024/sa/common/exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Resource
    private AuditLogService auditLogService;

    // 6.1 业务异常处理
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        String traceId = MDC.get("traceId");

        // 记录业务异常日志
        log.warn("[业务异常] traceId={}, code={}, message={}",
                 traceId, e.getCode(), e.getMessage());

        // 构建错误响应
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .traceId(traceId)
            .code(e.getCode())
            .message(e.getMessage())
            .module(getCurrentModule())
            .timestamp(System.currentTimeMillis())
            .build();

        // 记录业务异常审计
        auditLogService.recordBusinessError(traceId, e.getCode(), e.getMessage());

        return ResponseDTO.error(error.getCode(), error.getMessage(), error);
    }

    // 6.2 系统异常处理
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleSystemException(SystemException e) {
        String traceId = MDC.get("traceId");

        // 记录系统异常日志（包含完整栈信息）
        log.error("[系统异常] traceId={}, code={}, message={}",
                 traceId, e.getCode(), e.getMessage(), e);

        // 记录系统异常审计
        auditLogService.recordSystemError(traceId, e.getCode(), e.getMessage(), e.getCause());

        // 对用户提供友好信息，详细信息仅内部使用
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .traceId(traceId)
            .code(e.getCode())
            .message("系统繁忙，请稍后重试")
            .module(getCurrentModule())
            .timestamp(System.currentTimeMillis())
            .details(e.getMessage())  // 详细错误信息仅用于内部排查
            .build();

        // 触发告警（关键系统异常）
        if (isCriticalSystemError(e.getCode())) {
            alertService.sendAlert(traceId, e.getCode(), e.getMessage());
        }

        return ResponseDTO.error(error.getCode(), error.getMessage(), error);
    }

    // 6.3 参数验证异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException e) {
        String traceId = MDC.get("traceId");

        // 提取验证错误信息
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing
            ));

        log.warn("[参数验证异常] traceId={}, errors={}", traceId, errors);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .traceId(traceId)
            .code("VALIDATION_ERROR")
            .message("参数验证失败")
            .module(getCurrentModule())
            .timestamp(System.currentTimeMillis())
            .details(errors)
            .build();

        return ResponseDTO.error(error.getCode(), error.getMessage(), error);
    }

    // 6.4 HTTP方法不支持异常
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseDTO<Void> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        String traceId = MDC.get("traceId");

        log.warn("[HTTP方法不支持] traceId={}, method={}", traceId, e.getMethod());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .traceId(traceId)
            .code("METHOD_NOT_ALLOWED")
            .message("不支持的HTTP方法: " + e.getMethod())
            .module(getCurrentModule())
            .timestamp(System.currentTimeMillis())
            .build();

        return ResponseDTO.error(error.getCode(), error.getMessage(), error);
    }

    // 6.5 缺少请求参数异常
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleMissingParameterException(
            MissingServletRequestParameterException e) {
        String traceId = MDC.get("traceId");

        log.warn("[缺少参数] traceId={}, parameter={}", traceId, e.getParameterName());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .traceId(traceId)
            .code("MISSING_PARAMETER")
            .message("缺少必需参数: " + e.getParameterName())
            .module(getCurrentModule())
            .timestamp(System.currentTimeMillis())
            .build();

        return ResponseDTO.error(error.getCode(), error.getMessage(), error);
    }

    // 6.6 数据库异常处理
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleDataAccessException(DataAccessException e) {
        String traceId = MDC.get("traceId");

        log.error("[数据库异常] traceId={}, error={}", traceId, e.getMessage(), e);

        // 记录数据库异常审计
        auditLogService.recordDatabaseError(traceId, e.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .traceId(traceId)
            .code("DATABASE_ERROR")
            .message("数据操作失败，请稍后重试")
            .module(getCurrentModule())
            .timestamp(System.currentTimeMillis())
            .details(e.getMessage())
            .build();

        return ResponseDTO.error(error.getCode(), error.getMessage(), error);
    }

    // 6.7 通用异常处理（兜底）
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleException(Exception e) {
        String traceId = MDC.get("traceId");

        log.error("[未知异常] traceId={}, error={}", traceId, e.getMessage(), e);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .traceId(traceId)
            .code("UNKNOWN_ERROR")
            .message("系统异常，请联系管理员")
            .module(getCurrentModule())
            .timestamp(System.currentTimeMillis())
            .details(e.getClass().getSimpleName() + ": " + e.getMessage())
            .build();

        return ResponseDTO.error(error.getCode(), error.getMessage(), error);
    }

    // 辅助方法：获取当前模块名
    private String getCurrentModule() {
        // 从Spring应用上下文或包名获取当前模块名
        return "unknown";
    }

    // 辅助方法：判断是否为关键系统错误
    private boolean isCriticalSystemError(String errorCode) {
        return List.of("DATABASE_CONNECTION_ERROR", "FILE_SYSTEM_ERROR",
                      "MEMORY_ERROR", "NETWORK_ERROR").contains(errorCode);
    }
}
```

---

## 🔍 质量保障

### 1. 自动化检查脚本

**检查脚本**: `scripts/exception-handling-check.ps1`

```powershell
# 异常处理合规性检查脚本
Write-Host "开始检查异常处理合规性..." -ForegroundColor Green

# 检查1: 扫描重复的@ControllerAdvice
Write-Host "检查1: 扫描重复的@ControllerAdvice..." -ForegroundColor Yellow
$duplicateHandlers = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "@RestControllerAdvice" |
    Group-Object -Property Path |
    Where-Object { $_.Count -gt 0 }

if ($duplicateHandlers.Count -gt 1) {
    Write-Host "发现重复的异常处理器:" -ForegroundColor Red
    $duplicateHandlers | ForEach-Object {
        Write-Host "  - $($_.Name): $($_.Group.File)" -ForegroundColor Red
    }
    exit 1
} else {
    Write-Host "✅ 无重复的异常处理器" -ForegroundColor Green
}

# 检查2: 扫描try-catch吞噬异常
Write-Host "检查2: 扫描try-catch吞噬异常..." -ForegroundColor Yellow
$suppressedExceptions = Get-ChildItem -Path "microservices/*/src/main/java" -Recurse -Filter "*.java" |
    Select-String -Pattern "catch.*Exception.*\{[\s\S]*?\}" |
    Where-Object { $_.Line -notmatch "throw.*Exception" }

if ($suppressedExceptions.Count -gt 0) {
    Write-Host "发现可能吞噬异常的代码:" -ForegroundColor Yellow
    $suppressedExceptions | Select-Object -First 5 | ForEach-Object {
        Write-Host "  - $($_.Path):$($_.LineNumber): $($_.Line.Trim())" -ForegroundColor Yellow
    }
} else {
    Write-Host "✅ 未发现明显的异常吞噬" -ForegroundColor Green
}

# 检查3: 验证common-service依赖
Write-Host "检查3: 验证common-service依赖..." -ForegroundColor Yellow
$services = @("consume", "access", "attendance", "visitor", "video", "device-comm", "oa")
foreach ($service in $services) {
    $pomPath = "microservices/ioedream-$service-service/pom.xml"
    if (Test-Path $pomPath) {
        $content = Get-Content $pomPath -Raw
        if ($content -match "ioedream-common-service") {
            Write-Host "✅ $service-service 依赖 common-service" -ForegroundColor Green
        } else {
            Write-Host "❌ $service-service 缺少 common-service 依赖" -ForegroundColor Red
        }
    }
}

Write-Host "异常处理合规性检查完成！" -ForegroundColor Green
```

### 2. 单元测试覆盖

**异常处理单元测试**:
```java
@SpringBootTest
@AutoConfigureMockMvc
class ExceptionHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testBusinessException() throws Exception {
        mockMvc.perform(post("/api/v1/consume/account/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": -100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
    }

    @Test
    void testSystemException() throws Exception {
        // 模拟系统异常
        mockMvc.perform(get("/api/v1/consume/force-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("SYSTEM_ERROR"))
                .andExpect(jsonPath("$.data.traceId").exists());
    }

    @Test
    void testValidationException() throws Exception {
        mockMvc.perform(post("/api/v1/consume/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
```

### 3. 集成测试验证

**异常处理集成测试**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "resilience4j.circuitbreaker.instances.consume-service.failureRateThreshold=0"
})
class ExceptionHandlingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCircuitBreakerWithException() {
        // 模拟服务故障
        ResponseEntity<ResponseDTO> response = restTemplate.getForEntity(
            "/api/v1/consume/trigger-circuit-breaker", ResponseDTO.class);

        // 验证熔断器开启后的异常处理
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SERVICE_UNAVAILABLE", response.getBody().getCode());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testTraceIdPropagation() {
        ResponseEntity<ResponseDTO> response = restTemplate.getForEntity(
            "/api/v1/consume/account/test-trace-id", ResponseDTO.class);

        // 验证TraceId在异常响应中存在
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().toString().contains("traceId"));
    }
}
```

---

## 📊 监控与运维

### 1. 异常指标监控

**Micrometer异常指标配置**:
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5,0.9,0.95,0.99
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
```

**异常指标收集**:
```java
@Component
public class ExceptionMetrics {

    private final Counter businessExceptionCounter;
    private final Counter systemExceptionCounter;
    private final Counter validationExceptionCounter;

    public ExceptionMetrics(MeterRegistry meterRegistry) {
        this.businessExceptionCounter = Counter.builder("business.exception.count")
            .description("业务异常计数")
            .register(meterRegistry);
        this.systemExceptionCounter = Counter.builder("system.exception.count")
            .description("系统异常计数")
            .register(meterRegistry);
        this.validationExceptionCounter = Counter.builder("validation.exception.count")
            .description("验证异常计数")
            .register(meterRegistry);
    }

    @EventListener
    public void handleBusinessException(BusinessExceptionEvent event) {
        businessExceptionCounter.increment(
            Tags.of("code", event.getCode(), "module", event.getModule())
        );
    }

    @EventListener
    public void handleSystemException(SystemExceptionEvent event) {
        systemExceptionCounter.increment(
            Tags.of("code", event.getCode(), "module", event.getModule())
        );
    }
}
```

### 2. 告警规则配置

**Prometheus告警规则**:
```yaml
# exception-alerts.yml
groups:
  - name: exception.alerts
    rules:
      - alert: BusinessExceptionRate
        expr: rate(business_exception_count_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "业务异常率过高"
          description: "服务 {{ $labels.application }} 业务异常率超过10%"

      - alert: SystemExceptionSpike
        expr: rate(system_exception_count_total[1m]) > 0.05
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "系统异常激增"
          description: "服务 {{ $labels.application }} 系统异常激增"

      - alert: ValidationExceptionHigh
        expr: rate(validation_exception_count_total[5m]) > 0.2
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "参数验证异常过高"
          description: "服务 {{ $labels.application }} 参数验证异常率超过20%"
```

### 3. 日志聚合与分析

**ELK Stack日志配置**:
```yaml
# logback-spring.xml
<configuration>
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5000</destination>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <mdc/>
                <message/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>

    <logger name="net.lab1024.sa" level="INFO" additivity="false">
        <appender-ref ref="LOGSTASH"/>
    </logger>
</configuration>
```

---

## 📚 最佳实践总结

### 1. 异常处理最佳实践

**Do's（推荐做法）**:
- ✅ 使用统一的GlobalExceptionHandler
- ✅ 按异常类型分类处理（业务/系统/参数）
- ✅ 所有异常包含TraceId追踪
- ✅ 对用户提供友好错误信息
- ✅ 记录完整的异常日志和审计信息
- ✅ 与Resilience4j集成实现容错
- ✅ 配置中心化管理异常和容错规则

**Don'ts（禁止做法）**:
- ❌ 各微服务重复实现异常处理器
- ❌ 在Controller层吞噬异常
- ❌ 直接暴露系统内部错误信息
- ❌ 缺少Traceid的异常处理
- ❌ 硬编码异常配置
- ❌ 混淆业务异常和系统异常

### 2. 错误码设计规范

**错误码命名规则**:
- **业务异常**: {模块}_{具体错误} (如: CONSUME_INVALID_AMOUNT)
- **系统异常**: {系统}_{错误类型} (如: DATABASE_CONNECTION_ERROR)
- **参数异常**: PARAM_{错误类型} (如: PARAM_MISSING, PARAM_INVALID)

**常用错误码**:
```java
// 通用业务错误
USER_NOT_FOUND, USER_DISABLED, INVALID_TOKEN, PERMISSION_DENIED

// 消费模块错误
ACCOUNT_NOT_FOUND, INSUFFICIENT_BALANCE, INVALID_AMOUNT, CONSUME_LIMIT_EXCEEDED

// 系统错误
DATABASE_ERROR, NETWORK_ERROR, FILE_SYSTEM_ERROR, EXTERNAL_SERVICE_ERROR

// 参数错误
PARAM_MISSING, PARAM_INVALID, PARAM_TYPE_MISMATCH, VALIDATION_ERROR
```

### 3. 国际化支持

**异常信息国际化**:
```java
@Component
public class ErrorMessageResolver {

    @Resource
    private MessageSource messageSource;

    public String resolveMessage(String code, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return code; // 降级返回错误码
        }
    }
}

# messages_zh_CN.properties
ACCOUNT_NOT_FOUND=账户不存在
INSUFFICIENT_BALANCE=账户余额不足
INVALID_AMOUNT=无效的消费金额

# messages_en_US.properties
ACCOUNT_NOT_FOUND=Account not found
INSUFFICIENT_BALANCE=Insufficient balance
INVALID_AMOUNT=Invalid amount
```

---

## 🔄 实施路线图

### 阶段1: 依赖关系修复（1天）
- [ ] 检查各微服务common-service依赖
- [ ] 修复缺失依赖
- [ ] 验证GlobalExceptionHandler生效

### 阶段2: 异常处理标准化（2天）
- [ ] 移除重复的@ControllerAdvice实现
- [ ] 统一使用BusinessException、SystemException
- [ ] 更新Controller和Service层异常处理

### 阶段3: 容错配置中心化（2天）
- [ ] 迁移Resilience4j配置到Nacos
- [ ] 实现配置热更新
- [ ] 集成异常处理与容错机制

### 阶段4: 监控和告警（2天）
- [ ] 配置异常指标收集
- [ ] 设置告警规则
- [ ] 实现日志聚合和分析

### 阶段5: 测试和验证（2天）
- [ ] 单元测试覆盖
- [ ] 集成测试验证
- [ ] 性能测试验证

### 阶段6: 文档和培训（1天）
- [ ] 更新开发规范文档
- [ ] 制作异常处理指南
- [ ] 团队培训

---

## 📞 支持与维护

### 1. 故障排查指南

**异常处理故障排查步骤**:
1. **检查TraceId**: 通过TraceId关联完整调用链
2. **查看异常日志**: 检查异常分类和错误码
3. **验证配置**: 确认GlobalExceptionHandler正确加载
4. **检查依赖**: 验证common-service依赖关系
5. **监控指标**: 查看异常统计和告警

### 2. 常见问题FAQ

**Q1: GlobalExceptionHandler不生效？**
- 检查是否依赖common-service
- 确认无重复的@ControllerAdvice
- 验证包扫描路径

**Q2: 异常响应格式不统一？**
- 检查是否使用标准异常类
- 验证ResponseDTO构建逻辑
- 确认无异常吞噬

**Q3: TraceId丢失？**
- 检查MDC配置
- 验证日志框架配置
- 确认异步任务TraceId传递

---

**📋 文档版本**:
- **版本**: v1.0.0
- **创建日期**: 2025-12-16
- **最后更新**: 2025-12-16
- **维护责任人**: IOE-DREAM架构团队
- **审核人**: 老王（企业级架构专家）

**🔗 相关文档**:
- [CLAUDE.md 全局架构规范](../CLAUDE.md)
- [Resilience4j配置指南](./RESILIENCE4J_CONFIGURATION_GUIDE.md)
- [微服务容错设计指南](./MICROSERVICES_FAULT_TOLERANCE_GUIDE.md)
- [监控体系建设指南](./MONITORING_SYSTEM_GUIDE.md)