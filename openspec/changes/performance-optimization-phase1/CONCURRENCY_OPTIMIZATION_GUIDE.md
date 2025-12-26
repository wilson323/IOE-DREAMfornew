# 并发优化实施指南

> **任务编号**: P1-7.8
> **任务名称**: 并发优化（支持≥1000用户并发）
> **预计工时**: 3人天
> **优先级**: P1（高优先级）
> **创建日期**: 2025-12-26

---

## 📋 任务概述

### 问题描述

当前系统在高并发场景下存在以下瓶颈：

- **并发能力不足**: 仅支持300并发用户，超过则响应时间急剧增加
- **线程池配置不当**: 默认线程池配置未优化，资源利用率低
- **同步阻塞调用**: 大量同步调用导致线程阻塞
- **无限流保护**: 缺乏限流机制，高并发时系统雪崩
- **数据库连接池耗尽**: 高并发时连接池耗尽，请求排队等待
- **锁竞争严重**: 分布式锁使用不当，性能瓶颈明显

### 优化目标

- ✅ **并发用户数**: 从300→≥1000（**233%↑**）
- ✅ **响应时间**: P95响应时间<500ms（在1000并发下）
- ✅ **吞吐量**: TPS从800→≥2000（**150%↑**）
- ✅ **错误率**: <0.1%（在高并发下）
- ✅ **资源利用率**: CPU利用率>70%，内存利用率<80%
- ✅ **限流保护**: 实现多层限流保护机制
- ✅ **熔断降级**: 自动熔断降级，防止雪崩

### 预期效果

| 指标 | 优化前 | 优化后 | 提升幅度 |
|------|--------|--------|----------|
| **并发用户数** | 300 | ≥1000 | **233%↑** |
| **TPS** | 800 | ≥2000 | **150%↑** |
| **P95响应时间** | 2000ms | <500ms | **75%↑** |
| **P99响应时间** | 5000ms | <1000ms | **80%↑** |
| **错误率** | 5% | <0.1% | **98%↓** |
| **CPU利用率** | 50% | >70% | **40%↑** |
| **内存利用率** | 60% | <80% | 稳定 |

---

## 🎯 优化策略

### 1. 异步处理优化

#### 1.1 异步化Service方法

**优化前（同步阻塞）**:

```java
@Service
public class AccessServiceImpl implements AccessService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;
    @Resource
    private DeviceDao deviceDao;
    @Resource
    private AccessRecordDao accessRecordDao;

    @Override
    public ResponseDTO<AccessResultVO> verifyAccess(Long userId, String deviceId) {
        // 1. 同步查询设备信息（阻塞100ms）
        DeviceEntity device = deviceDao.selectById(deviceId);

        // 2. 同步调用设备服务（阻塞300ms）
        ResponseDTO<Boolean> deviceResponse = gatewayServiceClient.callDeviceService(
            "/api/device/verify",
            HttpMethod.POST,
            verifyRequest,
            Boolean.class
        );

        // 3. 同步保存通行记录（阻塞50ms）
        AccessRecordEntity record = new AccessRecordEntity();
        accessRecordDao.insert(record);

        // 总耗时: 100 + 300 + 50 = 450ms
        return ResponseDTO.ok(result);
    }
}
```

**优化后（异步非阻塞）**:

```java
@Service
public class AccessServiceImpl implements AccessService {

    @Resource
    private DeviceDao deviceDao;
    @Resource
    private GatewayServiceClient gatewayServiceClient;
    @Resource
    private AsyncServiceExecutor asyncServiceExecutor;
    @Resource
    private AccessRecordDao accessRecordDao;

    @Override
    public CompletableFuture<ResponseDTO<AccessResultVO>> verifyAccessAsync(Long userId, String deviceId) {
        // 1. 异步查询设备信息
        CompletableFuture<DeviceEntity> deviceFuture = CompletableFuture.supplyAsync(() -> {
            return deviceDao.selectById(deviceId);
        }, asyncServiceExecutor.getExecutor());

        // 2. 异步调用设备服务
        CompletableFuture<ResponseDTO<Boolean>> deviceVerifyFuture = deviceFuture.thenComposeAsync(device -> {
            VerifyRequest request = buildVerifyRequest(userId, device);
            return gatewayServiceClient.callDeviceServiceAsync(
                "/api/device/verify",
                HttpMethod.POST,
                request,
                Boolean.class
            );
        }, asyncServiceExecutor.getExecutor());

        // 3. 异步保存通行记录（不阻塞响应）
        deviceVerifyFuture.thenAcceptAsync(deviceResponse -> {
            if (deviceResponse.getData()) {
                AccessRecordEntity record = buildAccessRecord(userId, deviceId);
                accessRecordDao.insertAsync(record);
            }
        }, asyncServiceExecutor.getExecutor());

        // 4. 快速返回结果（不等待保存完成）
        return deviceVerifyFuture.thenApply(deviceResponse -> {
            AccessResultVO result = buildResult(deviceResponse.getData());
            return ResponseDTO.ok(result);
        });
        // 总耗时: max(100, 300) = 300ms（节省33%）
    }
}
```

**配置异步线程池**:

```java
@Configuration
public class AsyncServiceExecutor {

    @Bean("asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数（根据CPU核心数配置）
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);

        // 最大线程数
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 4);

        // 队列容量
        executor.setQueueCapacity(500);

        // 线程名前缀
        executor.setThreadNamePrefix("async-");

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}
```

**预期效果**: 接口响应时间减少30-50%

#### 1.2 WebFlux响应式编程（适用于IO密集型场景）

**引入依赖**:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

**响应式Controller**:

```java
@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    @Resource
    private AccessService accessService;

    /**
     * 响应式门禁验证接口
     * 使用WebFlux非阻塞IO，支持更高并发
     */
    @PostMapping("/verify")
    public Mono<ResponseDTO<AccessResultVO>> verifyAccess(@RequestBody VerifyRequest request) {
        return Mono.fromCallable(() -> {
                return accessService.verifyAccessAsync(request.getUserId(), request.getDeviceId());
            })
            .flatMap(future -> Mono.fromFuture(future))
            .onErrorResume(e -> {
                log.error("[门禁验证] 异步验证失败", e);
                return Mono.just(ResponseDTO.error("ACCESS_VERIFY_ERROR", "验证失败"));
            });
    }
}
```

**预期效果**: IO密集型接口并发能力提升200%

### 2. 线程池优化

#### 2.1 Tomcat线程池配置

**配置文件**: `application.yml`

```yaml
server:
  tomcat:
    # 最大工作线程数（处理请求的线程数）
    threads:
      max: 800  # 最大800个工作线程
      min-spare: 100  # 最小空闲100个线程

    # 连接队列大小
    accept-count: 500  # 等待队列500个请求

    # 最大连接数
    max-connections: 10000  # 最大10000个连接

    # 连接超时时间
    connection-timeout: 20000  # 20秒超时

    # 启用访问日志
    accesslog:
      enabled: true
      directory: logs
      pattern: '%h %l %u %t "%r" %s %b %D %{User-Agent}i'
```

**参数说明**:

| 参数 | 说明 | 推荐值 | 计算公式 |
|------|------|--------|----------|
| `threads.max` | 最大工作线程数 | 800 | CPU核心数 * 200 |
| `threads.min-spare` | 最小空闲线程数 | 100 | threads.max * 0.125 |
| `accept-count` | 等待队列大小 | 500 | threads.max * 0.625 |
| `max-connections` | 最大连接数 | 10000 | 根据服务器性能调整 |
| `connection-timeout` | 连接超时时间 | 20000ms | 20秒 |

**调优建议**:

```java
/**
 * Tomcat线程池自定义配置
 */
@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            TomcatWebServer server = (TomcatWebServer) factory.getWebServer();
            Tomcat tomcat = server.getTomcat();
            Connector connector = tomcat.getConnector();

            // 自定义Executor
            ProtocolHandler protocolHandler = connector.getProtocolHandler();
            Executor executor = new StandardThreadExecutor();
            ((StandardThreadExecutor) executor).setMaxThreads(800);
            ((StandardThreadExecutor) executor).setMinSpareThreads(100);
            ((StandardThreadExecutor) executor).setAcceptCount(500);
            ((StandardThreadExecutor) executor).setNamePrefix("tomcat-worker-");

            protocolHandler.setExecutor(executor);
        };
    }
}
```

#### 2.2 业务线程池配置

**场景1: IO密集型任务（数据库查询、RPC调用）**

```java
@Configuration
public class IoIntensiveExecutorConfig {

    @Bean("ioIntensiveExecutor")
    public ThreadPoolTaskExecutor ioIntensiveExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // IO密集型：线程数 = CPU核心数 / (1 - 阻塞系数)
        // 阻塞系数 = 0.8（IO等待占比）
        int cpuCore = Runtime.getRuntime().availableProcessors();
        int threadCount = (int) (cpuCore / (1 - 0.8));

        executor.setCorePoolSize(threadCount);
        executor.setMaxPoolSize(threadCount * 2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("io-intensive-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        return executor;
    }
}
```

**场景2: CPU密集型任务（计算、加密、图像处理）**

```java
@Configuration
public class CpuIntensiveExecutorConfig {

    @Bean("cpuIntensiveExecutor")
    public ThreadPoolTaskExecutor cpuIntensiveExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // CPU密集型：线程数 = CPU核心数 + 1
        int cpuCore = Runtime.getRuntime().availableProcessors();
        int threadCount = cpuCore + 1;

        executor.setCorePoolSize(threadCount);
        executor.setMaxPoolSize(threadCount);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("cpu-intensive-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();

        return executor;
    }
}
```

**使用示例**:

```java
@Service
public class UserServiceImpl implements UserService {

    @Resource(name = "ioIntensiveExecutor")
    private ThreadPoolTaskExecutor ioIntensiveExecutor;

    @Resource(name = "cpuIntensiveExecutor")
    private ThreadPoolTaskExecutor cpuIntensiveExecutor;

    /**
     * IO密集型任务：查询数据库
     */
    @Override
    public CompletableFuture<UserVO> getUserAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            return userDao.selectById(userId);
        }, ioIntensiveExecutor.getExecutor())
        .thenApplyAsync(entity -> convertToVO(entity), ioIntensiveExecutor.getExecutor());
    }

    /**
     * CPU密集型任务：密码加密
     */
    @Override
    public String encryptPassword(String rawPassword) {
        return cpuIntensiveExecutor.submit(() -> {
            return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        }).join();
    }
}
```

### 3. 限流保护

#### 3.1 接口级别限流（Sentinel）

**引入依赖**:

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

**配置限流规则**:

```java
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 规则1: 门禁验证接口限流
        FlowRule rule1 = new FlowRule();
        rule1.setResource("access:verify");
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);  // QPS限流
        rule1.setCount(1000);  // 限制1000 QPS
        rule1.setLimitApp("default");
        rule1.setStrategy(RuleConstant.STRATEGY_DIRECT);  // 直接拒绝
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);  // 快速失败
        rules.add(rule1);

        // 规则2: 用户查询接口限流（Warm-Up预热）
        FlowRule rule2 = new FlowRule();
        rule2.setResource("user:query");
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setCount(500);
        rule2.setLimitApp("default");
        rule2.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rule2.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);  // Warm-Up
        rule2.setWarmUpPeriodSec(10);  // 预热10秒
        rules.add(rule2);

        // 规则3: 消费接口限流（匀速排队）
        FlowRule rule3 = new FlowRule();
        rule3.setResource("consume:pay");
        rule3.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule3.setCount(200);
        rule3.setLimitApp("default");
        rule3.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rule3.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);  // 匀速排队
        rule3.setMaxQueueingTimeMs(500);  // 最大排队时间500ms
        rules.add(rule3);

        FlowRuleManager.loadRules(rules);
    }
}
```

**使用Sentinel注解**:

```java
@Service
public class AccessServiceImpl implements AccessService {

    /**
     * 门禁验证接口限流
     */
    @Override
    @SentinelResource(
        value = "access:verify",
        blockHandler = "verifyBlockHandler",
        fallback = "verifyFallback"
    )
    public ResponseDTO<AccessResultVO> verifyAccess(Long userId, String deviceId) {
        // 业务逻辑...
        return ResponseDTO.ok(result);
    }

    /**
     * 限流阻塞处理
     */
    public ResponseDTO<AccessResultVO> verifyBlockHandler(Long userId, String deviceId, BlockException ex) {
        log.warn("[门禁验证] 限流触发: userId={}, deviceId={}", userId, deviceId);
        return ResponseDTO.error("TOO_MANY_REQUESTS", "系统繁忙，请稍后重试");
    }

    /**
     * 降级处理
     */
    public ResponseDTO<AccessResultVO> verifyFallback(Long userId, String deviceId, Throwable ex) {
        log.error("[门禁验证] 服务降级: userId={}, deviceId={}", userId, deviceId, ex);
        return ResponseDTO.error("SERVICE_DEGRADED", "服务暂时不可用");
    }
}
```

**预期效果**: 接口QPS限制在系统承载能力内，防止雪崩

#### 3.2 用户级别限流

**实现**: 基于Redis + Lua脚本实现用户级限流

```java
@Component
public class UserRateLimiter {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 检查用户是否超过限流
     *
     * @param userId 用户ID
     * @param limit 限流次数（每分钟）
     * @return true：允许访问，false：限流
     */
    public boolean allowRequest(Long userId, int limit) {
        String key = "rate_limit:user:" + userId;

        // Lua脚本：原子性操作
        String script =
            "local current = redis.call('GET', KEYS[1]) " +
            "if current == false then " +
            "    redis.call('SET', KEYS[1], 1) " +
            "    redis.call('EXPIRE', KEYS[1], 60) " +
            "    return 1 " +
            "else " +
            "    if tonumber(current) < tonumber(ARGV[1]) then " +
            "        return redis.call('INCR', KEYS[1]) " +
            "    else " +
            "        return 0 " +
            "    end " +
            "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(
            redisScript,
            Collections.singletonList(key),
            String.valueOf(limit)
        );

        return result != null && result == 1;
    }
}
```

**使用拦截器**:

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Resource
    private UserRateLimiter userRateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从JWT Token中提取用户ID
        Long userId = JwtUtil.getUserId(request);

        // 检查是否限流（每分钟100次）
        boolean allowed = userRateLimiter.allowRequest(userId, 100);

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\": 429, \"message\": \"请求过于频繁，请稍后重试\"}");
            } catch (IOException e) {
                log.error("[限流拦截] 响应写入失败", e);
            }
            return false;
        }

        return true;
    }
}
```

**预期效果**: 防止单个用户恶意请求，保护系统资源

#### 3.3 系统级别限流

**基于系统指标自适应限流**:

```java
@Component
public class AdaptiveRateLimiter {

    @Resource
    private SystemMetricsCollector metricsCollector;

    private volatile double currentLimit = 1000;  // 当前限流值
    private volatile double lastLimit = 1000;

    /**
     * 动态调整限流阈值
     */
    @Scheduled(fixedRate = 5000)  // 每5秒调整一次
    public void adjustRateLimit() {
        SystemMetrics metrics = metricsCollector.getMetrics();

        // CPU使用率
        double cpuUsage = metrics.getCpuUsage();
        // 内存使用率
        double memoryUsage = metrics.getMemoryUsage();
        // 平均响应时间
        double avgResponseTime = metrics.getAvgResponseTime();
        // 错误率
        double errorRate = metrics.getErrorRate();

        // 计算系统负载分数（0-100）
        double loadScore = (
            cpuUsage * 0.3 +
            memoryUsage * 0.3 +
            (avgResponseTime / 1000) * 0.2 +
            errorRate * 100 * 0.2
        );

        // 动态调整限流值
        double newLimit;
        if (loadScore < 50) {
            // 系统负载低，提高限流值
            newLimit = Math.min(currentLimit * 1.1, 2000);
        } else if (loadScore > 80) {
            // 系统负载高，降低限流值
            newLimit = Math.max(currentLimit * 0.9, 100);
        } else {
            // 系统负载正常，保持不变
            newLimit = currentLimit;
        }

        if (Math.abs(newLimit - lastLimit) > 50) {
            log.info("[自适应限流] 调整限流值: {} -> {}", lastLimit, newLimit);
            currentLimit = newLimit;
            lastLimit = newLimit;
        }
    }

    /**
     * 检查是否允许请求
     */
    public boolean allowRequest() {
        // 使用令牌桶算法
        // ...实现令牌桶算法
        return true;
    }
}
```

**预期效果**: 根据系统负载自动调整限流阈值，最大化系统吞吐量

### 4. 熔断降级

#### 4.1 Sentinel熔断规则

**配置熔断规则**:

```java
@Configuration
public class SentinelCircuitBreakerConfig {

    @PostConstruct
    public void initCircuitBreakerRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 规则1: 慢调用比例熔断
        DegradeRule rule1 = new DegradeRule();
        rule1.setResource("access:verify");
        rule1.setGrade(RuleConstant.DEGRADE_GRADE_RT);  // 慢调用比例
        rule1.setCount(500);  // 慢调用阈值：500ms
        rule1.setTimeWindow(10);  // 熔断时长：10秒
        rule1.setMinRequestAmount(10);  // 最小请求数：10
        rule1.setSlowRatioThreshold(0.5);  // 慢调用比例阈值：50%
        rule1.setStatIntervalMs(10000);  // 统计时长：10秒
        rules.add(rule1);

        // 规则2: 异常比例熔断
        DegradeRule rule2 = new DegradeRule();
        rule2.setResource("consume:pay");
        rule2.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);  // 异常比例
        rule2.setCount(0.5);  // 异常比例阈值：50%
        rule2.setTimeWindow(10);  // 熔断时长：10秒
        rule2.setMinRequestAmount(10);  // 最小请求数：10
        rule2.setStatIntervalMs(10000);  // 统计时长：10秒
        rules.add(rule2);

        // 规则3: 异常数熔断
        DegradeRule rule3 = new DegradeRule();
        rule3.setResource("video:stream");
        rule3.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);  // 异常数
        rule3.setCount(50);  // 异常数阈值：50
        rule3.setTimeWindow(10);  // 熔断时长：10秒
        rule3.setMinRequestAmount(10);  // 最小请求数：10
        rule3.setStatIntervalMs(10000);  // 统计时长：10秒
        rules.add(rule3);

        DegradeRuleManager.loadRules(rules);
    }
}
```

**使用熔断降级**:

```java
@Service
public class ConsumeServiceImpl implements ConsumeService {

    /**
     * 消费支付接口（支持熔断降级）
     */
    @Override
    @SentinelResource(
        value = "consume:pay",
        blockHandler = "payBlockHandler",
        fallback = "payFallback"
    )
    public ResponseDTO<ConsumeResultVO> pay(ConsumePayRequest request) {
        // 业务逻辑...

        // 调用第三方支付接口（可能失败）
        return paymentService.charge(request);
    }

    /**
     * 熔断降级处理
     */
    public ResponseDTO<ConsumeResultVO> payFallback(ConsumePayRequest request, Throwable ex) {
        log.error("[消费支付] 服务降级: {}", request, ex);

        // 降级逻辑：返回失败，但不影响用户继续操作
        ConsumeResultVO result = new ConsumeResultVO();
        result.setSuccess(false);
        result.setMessage("支付服务暂时不可用，请稍后重试");

        return ResponseDTO.error("SERVICE_DEGRADED", "支付服务降级", result);
    }
}
```

**预期效果**: 服务异常时自动熔断，防止级联故障

#### 4.2 Resilience4j熔断器

**引入依赖**:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

**配置熔断器**:

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      accessService:
        # 滑动窗口类型：COUNT_BASED（基于计数）或 TIME_BASED（基于时间）
        sliding-window-type: COUNT_BASED
        # 滑动窗口大小
        sliding-window-size: 100
        # 失败率阈值（50%）
        failure-rate-threshold: 50
        # 慢调用阈值（500ms）
        slow-call-duration-threshold: 500ms
        # 慢调用率阈值（50%）
        slow-call-rate-threshold: 50
        # 最小调用次数（10次）
        minimum-number-of-calls: 10
        # 熔断器打开后的等待时间（10秒）
        wait-duration-in-open-state: 10s
        # 半开状态允许的调用次数（5次）
        permitted-number-of-calls-in-half-open-state: 5
        # 自动从打开转换到半开
        automatic-transition-from-open-to-half-open-enabled: true
```

**使用熔断器**:

```java
@Service
public class AccessServiceImpl implements AccessService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 使用Resilience4j熔断器
     */
    @Override
    @CircuitBreaker(name = "accessService", fallbackMethod = "verifyFallback")
    public ResponseDTO<AccessResultVO> verifyAccess(Long userId, String deviceId) {
        // 调用远程服务
        return gatewayServiceClient.callDeviceService(...);
    }

    /**
     * 降级方法
     */
    private ResponseDTO<AccessResultVO> verifyFallback(Long userId, String deviceId, Exception ex) {
        log.error("[门禁验证] 服务降级", ex);

        // 降级逻辑：使用本地缓存数据
        AccessResultVO result = getFromLocalCache(userId, deviceId);

        return ResponseDTO.ok(result);
    }
}
```

**预期效果**: 服务异常时自动降级，保证基本可用性

### 5. 分布式锁优化

#### 5.1 Redisson分布式锁

**引入依赖**:

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
</dependency>
```

**配置Redisson**:

```java
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 单节点模式
        config.useSingleServer()
            .setAddress("redis://localhost:6379")
            .setPassword("your_password")
            .setDatabase(0)
            .setConnectionPoolSize(64)
            .setConnectionMinimumIdleSize(10)
            .setIdleConnectionTimeout(10000)
            .setConnectTimeout(10000)
            .setTimeout(3000)
            .setRetryAttempts(3)
            .setRetryInterval(1500);

        return Redisson.create(config);
    }
}
```

**使用分布式锁**:

```java
@Service
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 消费支付（使用分布式锁防止重复支付）
     */
    @Override
    public ResponseDTO<ConsumeResultVO> pay(ConsumePayRequest request) {
        String lockKey = "consume:pay:userId:" + request.getUserId();

        // 获取分布式锁
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁，最多等待5秒，锁超时时间10秒
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!locked) {
                return ResponseDTO.error("REQUEST_IN_PROGRESS", "支付处理中，请勿重复提交");
            }

            // 执行支付逻辑
            return doPay(request);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseDTO.error("LOCK_INTERRUPTED", "加锁被中断");
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private ResponseDTO<ConsumeResultVO> doPay(ConsumePayRequest request) {
        // 支付逻辑...
        return ResponseDTO.ok(result);
    }
}
```

**优化点**:

1. **锁粒度优化**: 锁的粒度尽可能小（如锁用户ID而非全局锁）
2. **锁超时设置**: 根据业务执行时间设置合理的超时时间
3. **锁等待时间**: 避免无限等待，设置合理的等待时间
4. **锁释放**: 确保在finally块中释放锁，避免死锁

**预期效果**: 防止并发重复操作，保证数据一致性

---

## 📊 性能测试验证

### 1. JMeter压测脚本

**测试计划**: `jmeter-tests/concurrency-test.jmx`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="并发性能测试">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:8090</stringProp>
          </elementProp>
          <elementProp name="USER_TOKEN" elementType="Argument">
            <stringProp name="Argument.name">USER_TOKEN</stringProp>
            <stringProp name="Argument.value">your_test_token</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <!-- 线程组: 1000并发用户 -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="1000并发用户">
        <stringProp name="ThreadGroup.num_threads">1000</stringProp>
        <stringProp name="ThreadGroup.ramp_time">60</stringProp>  <!-- 60秒内逐步增加 -->
        <longProp name="ThreadGroup.duration">600</longProp>  <!-- 持续10分钟 -->
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">10</stringProp>  <!-- 循环10次 -->
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <!-- HTTP请求: 门禁验证 -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="门禁验证">
          <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
          <stringProp name="HTTPSampler.port"></stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/access/verify</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <stringProp name="Argument.value">{&quot;userId&quot;:1,&quot;deviceId&quot;:&quot;DEV001&quot;}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
              </elementProp>
            </collectionProp>
          </elementProp>
          <elementProp name="HTTPSampler.header_manager" elementType="HeaderManager">
            <collectionProp name="HeaderManager.headers">
              <elementProp name="" elementType="Header">
                <stringProp name="Header.name">Authorization</stringProp>
                <stringProp name="Header.value">Bearer ${USER_TOKEN}</stringProp>
              </elementProp>
              <elementProp name="" elementType="Header">
                <stringProp name="Header.name">Content-Type</stringProp>
                <stringProp name="Header.value">application/json</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
        </HTTPSamplerProxy>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

**执行测试**:

```bash
# 使用JMeter命令行执行测试
jmeter -n -t jmeter-tests/concurrency-test.jmx \
  -l results/concurrency-test.jtl \
  -e -o results/html-report/

# 查看测试报告
open results/html-report/index.html
```

### 2. 性能指标验证

**关键指标**:

| 指标 | 目标值 | 验证方法 |
|------|--------|----------|
| **并发用户数** | ≥1000 | JMeter压测报告 |
| **TPS** | ≥2000 | JMeter压测报告 |
| **P95响应时间** | <500ms | JMeter压测报告 |
| **P99响应时间** | <1000ms | JMeter压测报告 |
| **错误率** | <0.1% | JMeter压测报告 |
| **CPU利用率** | >70% | Prometheus监控 |
| **内存利用率** | <80% | Prometheus监控 |
| **数据库连接池** | <80%占用 | Druid监控 |

**Prometheus监控查询**:

```promql
# CPU使用率
rate(process_cpu_usage{service="ioedream-access-service"}[5m]) * 100

# 内存使用率
jvm_memory_used_bytes{service="ioedream-access-service",area="heap"} / jvm_memory_max_bytes * 100

# TPS（每秒请求数）
rate(http_server_requests_seconds_count{service="ioedream-access-service"}[1m])

# P95响应时间
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{service="ioedream-access-service"}[5m]))

# 错误率
rate(http_server_requests_seconds_count{service="ioedream-access-service",status!~"2.."}[5m]) / rate(http_server_requests_seconds_count{service="ioedream-access-service"}[5m]) * 100
```

---

## 📋 实施检查清单

### 阶段1: 异步化改造

- [ ] **异步Service方法**
  - [ ] 识别可异步化的Service方法
  - [ ] 使用CompletableFuture改造
  - [ ] 配置异步线程池
  - [ ] 验证功能正常

- [ ] **响应式编程**
  - [ ] 引入WebFlux依赖
  - [ ] 改造IO密集型接口
  - [ ] 验证并发性能提升

### 阶段2: 线程池优化

- [ ] **Tomcat线程池配置**
  - [ ] 配置server.tomcat参数
  - [ ] 自定义Executor
  - [ ] 验证连接数和线程数

- [ ] **业务线程池配置**
  - [ ] 配置IO密集型线程池
  - [ ] 配置CPU密集型线程池
  - [ ] 使用线程池执行异步任务

### 阶段3: 限流保护

- [ ] **接口级别限流**
  - [ ] 引入Sentinel依赖
  - [ ] 配置限流规则
  - [ ] 使用@SentinelResource注解
  - [ ] 验证限流生效

- [ ] **用户级别限流**
  - [ ] 实现Redis + Lua限流
  - [ ] 配置限流拦截器
  - [ ] 验证用户限流生效

- [ ] **系统级别限流**
  - [ ] 实现自适应限流
  - [ ] 配置系统指标收集
  - [ ] 验证动态调整生效

### 阶段4: 熔断降级

- [ ] **Sentinel熔断配置**
  - [ ] 配置熔断规则
  - [ ] 实现降级方法
  - [ ] 验证熔断生效

- [ ] **Resilience4j熔断器**
  - [ ] 引入Resilience4j依赖
  - [ ] 配置熔断器
  - [ ] 使用@CircuitBreaker注解

### 阶段5: 分布式锁优化

- [ ] **Redisson分布式锁**
  - [ ] 引入Redisson依赖
  - [ ] 配置RedissonClient
  - [ ] 使用分布式锁保护关键操作
  - [ ] 验证锁的正确性

### 阶段6: 性能测试

- [ ] **JMeter压测**
  - [ ] 编写压测脚本
  - [ ] 执行并发测试（1000用户）
  - [ ] 收集性能数据
  - [ ] 分析瓶颈点

- [ ] **监控验证**
  - [ ] 配置Prometheus监控
  - [ ] 配置Grafana面板
  - [ ] 实时监控性能指标
  - [ ] 配置性能告警

---

## 📚 附录

### A. 常见性能瓶颈排查

**问题1: 接口响应慢**

```
症状: 接口响应时间>2秒

排查步骤:
1. 查看应用日志，确认是否有慢查询
2. 使用Arthas诊断，查看线程状态
3. 检查数据库连接池是否耗尽
4. 检查是否有锁竞争
5. 检查GC是否频繁

解决方案:
- 优化慢SQL（添加索引）
- 增加数据库连接池大小
- 使用异步处理
- 优化锁粒度
```

**问题2: 并发能力不足**

```
症状: 并发用户数<300时正常，超过则响应时间急剧增加

排查步骤:
1. 检查Tomcat线程池配置
2. 检查数据库连接池配置
3. 检查是否有同步阻塞调用
4. 检查是否有锁竞争
5. 检查系统资源（CPU/内存/网络）

解决方案:
- 增加Tomcat线程池大小
- 增加数据库连接池大小
- 异步化处理
- 优化锁粒度
- 升级服务器配置
```

**问题3: 错误率高**

```
症状: 错误率>1%

排查步骤:
1. 查看错误日志，确认错误类型
2. 检查是否有服务超时
3. 检查是否有连接泄露
4. 检查是否有OOM
5. 检查是否有限流/熔断触发

解决方案:
- 优化代码逻辑
- 增加超时时间
- 修复连接泄露
- 增加内存配置
- 调整限流/熔断阈值
```

### B. 性能优化工具推荐

| 工具 | 用途 | 地址 |
|------|------|------|
| **JMeter** | 性能压测 | https://jmeter.apache.org/ |
| **Arthas** | Java诊断 | https://arthas.aliyun.com/ |
| **VisualVM** | JVM监控 | https://visualvm.github.io/ |
| **Prometheus** | 监控系统 | https://prometheus.io/ |
| **Grafana** | 监控面板 | https://grafana.com/ |
| **Sentinel** | 限流熔断 | https://github.com/alibaba/Sentinel |

---

**文档版本**: v1.0
**最后更新**: 2025-12-26
**作者**: IOE-DREAM 性能优化小组
**状态**: ✅ 文档完成，待实施验证
