# Resilience4j 使用示例和最佳实践

> **创建时间**: 2025-12-09
> **适用范围**: IOE-DREAM 所有微服务
> **版本**: v1.0.0

## 📋 目录

1. [基本概念](#基本概念)
2. [Service层使用示例](#service层使用示例)
3. [Controller层使用示例](#controller层使用示例)
4. [Manager层使用示例](#manager层使用示例)
5. [最佳实践](#最佳实践)
6. [监控和告警](#监控和告警)
7. [故障排查](#故障排查)

---

## 基本概念

Resilience4j 提供了以下核心容错机制：

| 机制 | 用途 | 适用场景 |
|------|------|----------|
| **Circuit Breaker** | 熔断器 | 防止级联故障，快速失败 |
| **Retry** | 重试机制 | 临时性错误自动重试 |
| **Rate Limiter** | 限流器 | 控制访问频率，保护服务 |
| **Bulkhead** | 舱壁隔离 | 资源隔离，防止资源耗尽 |
| **Time Limiter** | 时间限制器 | 防止长时间阻塞 |

---

## Service层使用示例

### 1. 数据库访问容错

```java
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private UserManager userManager;

    /**
     * 用户查询 - 基本容错配置
     */
    @CircuitBreaker(name = "databaseCircuitBreaker", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "databaseRetry")
    @TimeLimiter(name = "databaseTimeLimiter")
    @RateLimiter(name = "databaseRateLimiter")
    @Bulkhead(name = "databaseBulkhead")
    public ResponseDTO<UserVO> getUserById(Long userId) {
        log.info("[用户查询] 开始查询用户: userId={}", userId);

        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        UserVO userVO = userManager.convertToVO(user);
        log.info("[用户查询] 查询成功: userId={}", userId);

        return ResponseDTO.ok(userVO);
    }

    /**
     * 用户查询熔断降级方法
     */
    public ResponseDTO<UserVO> getUserByIdFallback(Long userId, Exception ex) {
        log.error("[用户查询] 熔断降级: userId={}, error={}", userId, ex.getMessage());

        // 返回缓存中的用户信息或默认值
        UserVO fallbackUser = userManager.getUserFromCache(userId);
        if (fallbackUser != null) {
            return ResponseDTO.ok(fallbackUser);
        }

        return ResponseDTO.error("SERVICE_UNAVAILABLE", "用户服务暂时不可用，请稍后重试");
    }

    /**
     * 批量用户查询 - 使用不同的容错策略
     */
    @CircuitBreaker(name = "databaseCircuitBreaker", fallbackMethod = "getUsersBatchFallback")
    @Retry(name = "databaseRetry")
    @TimeLimiter(name = "databaseTimeLimiter")
    @Bulkhead(name = "databaseBulkhead", type = Bulkhead.Type.THREADPOOL)  // 线程池隔离
    public ResponseDTO<List<UserVO>> getUsersBatch(List<Long> userIds) {
        log.info("[批量用户查询] 开始查询: userIds={}", userIds);

        List<UserEntity> users = userDao.selectBatchIds(userIds);
        List<UserVO> userVOs = users.stream()
                .map(userManager::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(userVOs);
    }

    /**
     * 批量查询降级方法
     */
    public ResponseDTO<List<UserVO>> getUsersBatchFallback(List<Long> userIds, Exception ex) {
        log.error("[批量用户查询] 熔断降级: userIds={}, error={}", userIds, ex.getMessage());

        // 分批处理，避免同时触发熔断
        List<UserVO> result = new ArrayList<>();
        int batchSize = 10;

        for (int i = 0; i < userIds.size(); i += batchSize) {
            List<Long> batch = userIds.subList(i, Math.min(i + batchSize, userIds.size()));
            try {
                List<UserEntity> batchUsers = userDao.selectBatchIds(batch);
                result.addAll(batchUsers.stream()
                        .map(userManager::convertToVO)
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                log.warn("[批量用户查询] 分批查询失败: batch={}", batch, e);
            }
        }

        return ResponseDTO.ok(result);
    }

    /**
     * 用户更新 - 写操作容错
     */
    @CircuitBreaker(name = "databaseCircuitBreaker", fallbackMethod = "updateUserFallback")
    @Retry(name = "databaseRetry")  // 写操作也可以重试，但需要幂等性保证
    @RateLimiter(name = "writeOperationRateLimiter")  // 使用写操作专用限流器
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> updateUser(UserUpdateForm updateForm) {
        log.info("[用户更新] 开始更新: userId={}", updateForm.getUserId());

        UserEntity user = userDao.selectById(updateForm.getUserId());
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 使用乐观锁防止并发更新
        user.setVersion(updateForm.getVersion());
        user.setUsername(updateForm.getUsername());
        user.setEmail(updateForm.getEmail());

        int updateCount = userDao.updateById(user);
        if (updateCount == 0) {
            throw new BusinessException("UPDATE_FAILED", "更新失败，可能数据已被修改");
        }

        // 清除缓存
        userManager.clearUserCache(updateForm.getUserId());

        log.info("[用户更新] 更新成功: userId={}", updateForm.getUserId());
        return ResponseDTO.ok();
    }

    /**
     * 用户更新降级方法
     */
    public ResponseDTO<Void> updateUserFallback(UserUpdateForm updateForm, Exception ex) {
        log.error("[用户更新] 熔断降级: userId={}, error={}", updateForm.getUserId(), ex.getMessage());

        // 写操作降级通常返回错误，而不是默认值
        return ResponseDTO.error("SERVICE_UNAVAILABLE", "用户服务暂时不可用，请稍后重试");
    }
}
```

### 2. 外部服务调用容错

```java
@Service
@Slf4j
public class ExternalApiServiceImpl implements ExternalApiService {

    @Resource
    private RestTemplate restTemplate;

    /**
     * 调用外部API - 完整容错配置
     */
    @CircuitBreaker(name = "externalServiceCircuitBreaker", fallbackMethod = "callExternalApiFallback")
    @Retry(name = "externalServiceRetry")
    @TimeLimiter(name = "externalServiceTimeLimiter")
    @Bulkhead(name = "externalServiceBulkhead")
    @RateLimiter(name = "externalServiceRateLimiter")
    public ResponseDTO<String> callExternalApi(String apiUrl, Object request) {
        log.info("[外部API调用] 开始调用: url={}", apiUrl);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[外部API调用] 调用成功: url={}, status={}", apiUrl, response.getStatusCode());
                return ResponseDTO.ok(response.getBody());
            } else {
                throw new BusinessException("EXTERNAL_API_ERROR", "外部API调用失败: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("[外部API调用] 调用异常: url={}, error={}", apiUrl, e.getMessage());
            throw e;
        }
    }

    /**
     * 外部API调用降级方法
     */
    public ResponseDTO<String> callExternalApiFallback(String apiUrl, Object request, Exception ex) {
        log.error("[外部API调用] 熔断降级: url={}, error={}", apiUrl, ex.getMessage());

        // 尝试使用备用数据源
        String fallbackData = getFallbackData(apiUrl, request);
        if (fallbackData != null) {
            log.info("[外部API调用] 使用备用数据: url={}", apiUrl);
            return ResponseDTO.ok(fallbackData);
        }

        return ResponseDTO.error("EXTERNAL_SERVICE_UNAVAILABLE", "外部服务暂时不可用");
    }

    private String getFallbackData(String apiUrl, Object request) {
        // 实现备用数据获取逻辑
        // 例如：使用缓存数据、默认数据等
        return null;
    }
}
```

---

## Controller层使用示例

```java
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理API")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 查询用户 - API级别容错
     */
    @GetMapping("/{userId}")
    @CircuitBreaker(name = "apiCircuitBreaker", fallbackMethod = "getUserApiFallback")
    @RateLimiter(name = "apiRateLimiter")  # API级别限流
    @Operation(summary = "查询用户详情")
    public ResponseDTO<UserVO> getUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {

        log.info("[用户API] 查询用户: userId={}", userId);
        return userService.getUserById(userId);
    }

    /**
     * API查询降级方法
     */
    public ResponseDTO<UserVO> getUserApiFallback(Long userId, Exception ex) {
        log.error("[用户API] 查询降级: userId={}, error={}", userId, ex.getMessage());

        // API降级通常返回简单错误信息
        return ResponseDTO.error("API_UNAVAILABLE", "服务暂时不可用，请稍后重试");
    }

    /**
     * 批量查询用户 - 支持分页
     */
    @PostMapping("/batch")
    @CircuitBreaker(name = "batchApiCircuitBreaker", fallbackMethod = "getUsersBatchApiFallback")
    @RateLimiter(name = "batchOperationRateLimiter")  # 批量操作限流
    @Operation(summary = "批量查询用户")
    public ResponseDTO<PageResult<UserVO>> getUsersBatch(
            @Valid @RequestBody UserBatchQueryForm queryForm) {

        log.info("[用户API] 批量查询: queryForm={}", queryForm);
        return userService.getUsersBatch(queryForm);
    }

    /**
     * 批量查询API降级方法
     */
    public ResponseDTO<PageResult<UserVO>> getUsersBatchApiFallback(UserBatchQueryForm queryForm, Exception ex) {
        log.error("[用户API] 批量查询降级: queryForm={}, error={}", queryForm, ex.getMessage());

        // 返回空结果而不是错误，避免影响前端体验
        PageResult<UserVO> emptyResult = new PageResult<>();
        emptyResult.setList(Collections.emptyList());
        emptyResult.setTotal(0L);
        emptyResult.setPageNum(queryForm.getPageNum());
        emptyResult.setPageSize(queryForm.getPageSize());

        return ResponseDTO.ok(emptyResult);
    }
}
```

---

## Manager层使用示例

```java
@Component
@Slf4j
public class UserManager {

    @Resource
    private UserDao userDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 复杂业务操作 - 多层容错
     */
    @CircuitBreaker(name = "businessCircuitBreaker", fallbackMethod = "complexBusinessOperationFallback")
    @Retry(name = "businessRetry")
    @TimeLimiter(name = "businessTimeLimiter")
    @Bulkhead(name = "businessBulkhead")
    public BusinessResultDTO complexBusinessOperation(Long userId, BusinessRequestDTO request) {
        log.info("[复杂业务操作] 开始: userId={}, request={}", userId, request);

        try {
            // 1. 数据验证
            validateBusinessRequest(request);

            // 2. 数据库操作（带容错）
            UserEntity user = getUserWithCircuitBreaker(userId);

            // 3. 缓存操作（带容错）
            updateCacheWithCircuitBreaker(userId, request);

            // 4. 外部服务调用（带容错）
            callExternalServiceWithCircuitBreaker(request);

            // 5. 业务逻辑处理
            BusinessResultDTO result = processBusinessLogic(user, request);

            log.info("[复杂业务操作] 完成: userId={}, result={}", userId, result);
            return result;

        } catch (Exception e) {
            log.error("[复杂业务操作] 失败: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 数据库查询容错方法
     */
    @CircuitBreaker(name = "databaseCircuitBreaker")
    private UserEntity getUserWithCircuitBreaker(Long userId) {
        return userDao.selectById(userId);
    }

    /**
     * 缓存更新容错方法
     */
    @CircuitBreaker(name = "redisCircuitBreaker")
    private void updateCacheWithCircuitBreaker(Long userId, BusinessRequestDTO request) {
        String cacheKey = "user:business:" + userId;
        redisTemplate.opsForValue().set(cacheKey, request, Duration.ofMinutes(30));
    }

    /**
     * 外部服务调用容错方法
     */
    @CircuitBreaker(name = "externalServiceCircuitBreaker")
    private void callExternalServiceWithCircuitBreaker(BusinessRequestDTO request) {
        // 调用外部服务逻辑
    }

    /**
     * 复杂业务操作降级方法
     */
    public BusinessResultDTO complexBusinessOperationFallback(Long userId, BusinessRequestDTO request, Exception ex) {
        log.error("[复杂业务操作] 降级: userId={}, error={}", userId, ex.getMessage());

        // 实现降级逻辑
        return createFallbackResult(userId, request);
    }

    private BusinessResultDTO createFallbackResult(Long userId, BusinessRequestDTO request) {
        // 创建降级结果的逻辑
        BusinessResultDTO fallbackResult = new BusinessResultDTO();
        fallbackResult.setSuccess(false);
        fallbackResult.setMessage("服务暂时不可用，已使用降级处理");
        fallbackResult.setData(createDefaultData(userId));
        return fallbackResult;
    }
}
```

---

## 最佳实践

### 1. 容错策略选择原则

| 场景 | 推荐容错机制 | 配置建议 |
|------|-------------|----------|
| **数据库查询** | CircuitBreaker + Retry + TimeLimiter | 失败率40%，重试2次，超时30秒 |
| **数据库写入** | CircuitBreaker + TimeLimiter | 失败率30%，不重试，超时10秒 |
| **Redis操作** | CircuitBreaker + Retry | 失败率60%，重试5次，快速失败 |
| **外部API调用** | CircuitBreaker + Retry + TimeLimiter | 失败率55%，重试3次，超时15秒 |
| **微服务间调用** | CircuitBreaker + Bulkhead | 失败率45%，并发限制20 |
| **文件上传下载** | TimeLimiter + Bulkhead | 超时60秒，并发限制5 |

### 2. 注解组合最佳实践

```java
// 1. 读操作 - 完整容错
@CircuitBreaker(name = "databaseCircuitBreaker", fallbackMethod = "fallbackMethod")
@Retry(name = "databaseRetry")
@TimeLimiter(name = "databaseTimeLimiter")
@RateLimiter(name = "queryOperationRateLimiter")
public Result readOperation(String param) { ... }

// 2. 写操作 - 简化容错（不重试）
@CircuitBreaker(name = "databaseCircuitBreaker", fallbackMethod = "fallbackMethod")
@TimeLimiter(name = "databaseTimeLimiter")
@RateLimiter(name = "writeOperationRateLimiter")
public Result writeOperation(String param) { ... }

// 3. 批量操作 - 线程池隔离
@CircuitBreaker(name = "batchCircuitBreaker", fallbackMethod = "fallbackMethod")
@Bulkhead(name = "batchBulkhead", type = Bulkhead.Type.THREADPOOL)
public Result batchOperation(List<String> params) { ... }

// 4. 外部服务调用 - 完整容错
@CircuitBreaker(name = "externalServiceCircuitBreaker", fallbackMethod = "fallbackMethod")
@Retry(name = "externalServiceRetry")
@TimeLimiter(name = "externalServiceTimeLimiter")
@RateLimiter(name = "externalServiceRateLimiter")
public Result externalServiceCall(String param) { ... }
```

### 3. 降级方法设计原则

```java
/**
 * 降级方法设计最佳实践
 */
public Result fallbackMethod(Parameter param, Exception ex) {
    // 1. 记录详细的错误日志
    log.error("[服务降级] 方法={}, 参数={}, 异常={}",
             "methodName", param, ex.getMessage(), ex);

    // 2. 分析异常类型，选择降级策略
    if (ex instanceof CircuitBreakerOpenException) {
        // 熔断器打开 - 返回降级数据
        return getDegradedResult(param);
    } else if (ex instanceof TimeoutException) {
        // 超时 - 返回简单结果或缓存数据
        return getCachedResult(param);
    } else {
        // 其他异常 - 返回错误信息
        return Result.error("SERVICE_UNAVAILABLE", "服务暂时不可用");
    }
}
```

### 4. 配置调优建议

```yaml
# 生产环境推荐配置
resilience4j:
  circuitbreaker:
    configs:
      production:
        failure-rate-threshold: 40%           # 生产环境更保守
        minimum-number-of-calls: 50           # 更多样本
        wait-duration-in-open-state: 60s      # 更长恢复时间
        sliding-window-size: 200             # 更大窗口

  retry:
    configs:
      production:
        max-attempts: 2                      # 减少重试次数
        wait-duration: 1s                    # 增加重试间隔

  ratelimiter:
    configs:
      production:
        limit-for-period: 100                # 适中限流
        timeout-duration: 100ms             # 短等待时间
```

### 5. 监控和告警配置

```java
// 自定义监控指标
@Component
public class Resilience4jMetrics {

    private final MeterRegistry meterRegistry;

    @EventListener
    public void handleCircuitBreakerEvent(CircuitBreakerOnStateTransitionEvent event) {
        meterRegistry.counter("resilience4j.circuitbreaker.transition",
                "circuitbreaker", event.getCircuitBreakerName(),
                "from", event.getStateTransition().getFromState().name(),
                "to", event.getStateTransition().getToState().name())
                .increment();
    }

    @EventListener
    public void handleRetryEvent(RetryOnRetryEvent event) {
        meterRegistry.counter("resilience4j.retry.attempt",
                "retry", event.getRetry().getName(),
                "attempt", String.valueOf(event.getNumberOfRetryAttempts()))
                .increment();
    }
}
```

---

## 监控和告警

### 1. Actuator端点

访问以下端点获取容错状态：

```
# 熔断器状态
GET /actuator/circuitbreakers

# 重试状态
GET /actuator/retries

# 限流器状态
GET /actuator/ratelimiters

# 舱壁状态
GET /actuator/bulkheads

# 健康检查
GET /actuator/health
```

### 2. Prometheus指标

关键监控指标：

- `resilience4j_circuitbreaker_calls_total` - 熔断器调用总数
- `resilience4j_circuitbreaker_state` - 熔断器状态
- `resilience4j_retry_calls_total` - 重试调用总数
- `resilience4j_ratelimiter_available_permissions` - 限流器可用许可
- `resilience4j_bulkhead_available_concurrent_calls` - 舱壁可用并发

### 3. Grafana仪表盘

推荐监控面板：

1. **熔断器状态面板**
   - 显示所有熔断器的当前状态
   - 成功率/失败率趋势
   - 状态变化事件

2. **限流器性能面板**
   - 各限流器的允许/拒绝请求数
   - 平均等待时间
   - 限流触发频率

3. **重试效果面板**
   - 重试成功率
   - 重试延迟分布
   - 重试次数分布

---

## 故障排查

### 1. 常见问题和解决方案

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| **熔断器频繁打开** | 失败率阈值过低 | 调整failure-rate-threshold |
| **重试次数过多** | 重试策略不当 | 减少max-attempts，增加等待时间 |
| **限流过于严格** | limit-for-period过低 | 调整限流阈值 |
| **舱壁频繁拒绝** | max-concurrent-calls过小 | 增加并发限制 |
| **降级方法未触发** | fallbackMethod签名不匹配 | 检查参数类型和数量 |

### 2. 日志配置

```yaml
logging:
  level:
    io.github.resilience4j: DEBUG
    org.springframework.retry: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
```

### 3. 调试技巧

```java
// 添加调试日志
@CircuitBreaker(name = "exampleCircuitBreaker")
public Result exampleMethod(String param) {
    log.debug("[容错调试] 开始执行: param={}, circuitbreaker状态={}",
             param, circuitBreaker.getState());

    try {
        // 业务逻辑
        return businessLogic(param);
    } finally {
        log.debug("[容错调试] 执行完成: param={}, 结果={}", param, result);
    }
}
```

---

**总结**: Resilience4j提供了完整的容错机制，合理配置和使用可以显著提升系统的可用性和稳定性。关键是根据具体业务场景选择合适的容错策略，并做好监控和故障排查。