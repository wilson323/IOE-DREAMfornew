# 网关服务专家技能
## Gateway Service Specialist

**🎯 技能定位**: IOE-DREAM智慧园区API网关业务专家，精通路由管理、负载均衡、安全防护、流量控制、监控告警等核心网关功能

**⚡ 技能等级**: ★★★★★ (顶级专家)
**🎯 适用场景**: API网关开发、微服务集成、流量管理、安全防护、监控运维
**📊 技能覆盖**: 路由管理 | 负载均衡 | 安全防护 | 流量控制 | 限流熔断 | 监控告警 | 服务发现

---

## 📋 技能概述

### **核心专长**
- **智能路由管理**: 动态路由配置、路径匹配、服务发现、负载均衡
- **安全防护体系**: 身份认证、权限校验、数据加密、防攻击过滤
- **流量控制机制**: 限流、熔断、降级、流量整形、QoS保障
- **监控告警系统**: 实时监控、性能分析、告警通知、链路追踪
- **服务治理能力**: 服务发现、健康检查、故障转移、服务版本管理
- **高可用架构**: 集群部署、故障恢复、数据同步、配置管理

### **解决能力**
- **API网关建设**: 完整的企业级API网关系统实现和优化
- **微服务集成**: 统一的微服务接入和管理平台
- **安全防护体系**: 多层次的安全防护和访问控制
- **流量管理优化**: 智能的流量控制和性能优化方案
- **运维监控平台**: 全方位的网关监控和运维管理系统

---

## 🎯 业务场景覆盖

### 🛣️ 智能路由管理
```java
// 动态路由配置和管理
@Component
public class DynamicRouteManager {

    private final RouteDefinitionWriter routeDefinitionWriter;
    private final RouteDefinitionLocator routeDefinitionLocator;
    private final ApplicationEventPublisher publisher;
    private final RedisTemplate<String, Object> redisTemplate;

    public DynamicRouteManager(RouteDefinitionWriter routeDefinitionWriter,
                              RouteDefinitionLocator routeDefinitionLocator,
                              ApplicationEventPublisher publisher,
                              RedisTemplate<String, Object> redisTemplate) {
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.publisher = publisher;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 添加动态路由
     */
    public boolean addRoute(RouteDefinition definition) {
        try {
            // 1. 验证路由定义
            validateRouteDefinition(definition);

            // 2. 检查路由是否已存在
            if (routeExists(definition.getId())) {
                log.warn("路由已存在，将更新: routeId={}", definition.getId());
                return updateRoute(definition);
            }

            // 3. 保存路由到内存
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();

            // 4. 保存路由到Redis（持久化）
            saveRouteToRedis(definition);

            // 5. 发布路由变更事件
            publisher.publishEvent(new RefreshRoutesEvent(this));

            log.info("路由添加成功: routeId={}, uri={}", definition.getId(), definition.getUri());
            return true;

        } catch (Exception e) {
            log.error("路由添加失败: routeId={}", definition.getId(), e);
            return false;
        }
    }

    /**
     * 更新动态路由
     */
    public boolean updateRoute(RouteDefinition definition) {
        try {
            // 1. 先删除旧路由
            routeDefinitionWriter.delete(Mono.just(definition.getId())).subscribe();

            // 2. 添加新路由
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();

            // 3. 更新Redis中的路由配置
            saveRouteToRedis(definition);

            // 4. 发布路由变更事件
            publisher.publishEvent(new RefreshRoutesEvent(this));

            log.info("路由更新成功: routeId={}, uri={}", definition.getId(), definition.getUri());
            return true;

        } catch (Exception e) {
            log.error("路由更新失败: routeId={}", definition.getId(), e);
            return false;
        }
    }

    /**
     * 删除动态路由
     */
    public boolean deleteRoute(String routeId) {
        try {
            // 1. 从内存中删除路由
            routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();

            // 2. 从Redis中删除路由配置
            deleteRouteFromRedis(routeId);

            // 3. 发布路由变更事件
            publisher.publishEvent(new RefreshRoutesEvent(this));

            log.info("路由删除成功: routeId={}", routeId);
            return true;

        } catch (Exception e) {
            log.error("路由删除失败: routeId={}", routeId, e);
            return false;
        }
    }

    /**
     * 获取所有路由定义
     */
    public List<RouteDefinition> getAllRoutes() {
        try {
            return routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("获取路由列表失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 从Nacos服务发现自动创建路由
     */
    public void autoCreateRoutesFromNacos() {
        try {
            // 获取所有已注册的服务
            List<String> services = getNacosServices();

            for (String serviceName : services) {
                if (shouldCreateRouteForService(serviceName)) {
                    RouteDefinition routeDefinition = buildRouteFromService(serviceName);
                    addRoute(routeDefinition);
                }
            }

        } catch (Exception e) {
            log.error("从Nacos自动创建路由失败", e);
        }
    }

    private RouteDefinition buildRouteFromService(String serviceName) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(serviceName + "-route");

        // 设置路径
        definition.setPredicates(Collections.singletonList(
            new PathPredicate("/api/" + serviceName + "/**")
        ));

        // 设置目标URI（lb://表示负载均衡）
        definition.setUri(URI.create("lb://" + serviceName));

        // 设置过滤器
        definition.setFilters(Arrays.asList(
            new StripPrefixGatewayFilterFactory().apply(1),
            new AddRequestHeaderGatewayFilterFactory().apply("X-Service-Name", serviceName),
            new AddRequestHeaderGatewayFilterFactory().apply("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()))
        ));

        // 设置元数据
        definition.setMetadata(Map.of(
            "serviceName", serviceName,
            "autoCreated", "true",
            "createTime", LocalDateTime.now().toString()
        ));

        return definition;
    }

    private void validateRouteDefinition(RouteDefinition definition) {
        if (StringUtils.isBlank(definition.getId())) {
            throw new IllegalArgumentException("路由ID不能为空");
        }

        if (definition.getUri() == null) {
            throw new IllegalArgumentException("路由URI不能为空");
        }

        if (definition.getPredicates() == null || definition.getPredicates().isEmpty()) {
            throw new IllegalArgumentException("路由断言不能为空");
        }
    }

    private boolean routeExists(String routeId) {
        return getAllRoutes().stream()
            .anyMatch(route -> routeId.equals(route.getId()));
    }

    private void saveRouteToRedis(RouteDefinition definition) {
        try {
            String key = "gateway:route:" + definition.getId();
            redisTemplate.opsForValue().set(key, definition, Duration.ofHours(24));
        } catch (Exception e) {
            log.warn("保存路由到Redis失败: routeId={}", definition.getId(), e);
        }
    }

    private void deleteRouteFromRedis(String routeId) {
        try {
            String key = "gateway:route:" + routeId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("从Redis删除路由失败: routeId={}", routeId, e);
        }
    }
}
```

### 🔒 安全防护过滤器
```java
// 全局安全过滤器
@Component
@Slf4j
public class GlobalSecurityFilter implements GlobalFilter, Ordered {

    private final JwtTokenUtil jwtTokenUtil;
    private final PermissionService permissionService;
    private final RateLimiter rateLimiter;
    private final BlacklistService blacklistService;

    public GlobalSecurityFilter(JwtTokenUtil jwtTokenUtil,
                                PermissionService permissionService,
                                RateLimiter rateLimiter,
                                BlacklistService blacklistService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.permissionService = permissionService;
        this.rateLimiter = rateLimiter;
        this.blacklistService = blacklistService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        try {
            // 1. IP黑名单检查
            if (isBlacklistedIp(request)) {
                return handleBlacklistedIp(response);
            }

            // 2. 限流检查
            if (!checkRateLimit(request)) {
                return handleRateLimitExceeded(response);
            }

            // 3. 跳过不需要认证的路径
            if (isPublicPath(request)) {
                return chain.filter(exchange);
            }

            // 4. Token验证
            String token = extractToken(request);
            if (StringUtils.isBlank(token)) {
                return handleUnauthorized(response, "缺少认证令牌");
            }

            // 5. Token有效性验证
            if (!jwtTokenUtil.validateToken(token)) {
                return handleUnauthorized(response, "认证令牌无效或已过期");
            }

            // 6. 解析用户信息
            UserClaims userClaims = jwtTokenUtil.parseToken(token);
            if (userClaims == null) {
                return handleUnauthorized(response, "无法解析用户信息");
            }

            // 7. 权限验证
            if (!checkPermission(request, userClaims)) {
                return handleForbidden(response, "无权限访问该资源");
            }

            // 8. 添加用户信息到请求头
            ServerHttpRequest modifiedRequest = addUserHeaders(request, userClaims);

            // 9. 记录访问日志
            recordAccessLog(modifiedRequest, userClaims);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("安全过滤器处理异常: uri={}", request.getURI(), e);
            return handleInternalServerError(response, "系统内部错误");
        }
    }

    @Override
    public int getOrder() {
        return -100; // 确保这个过滤器在其他过滤器之前执行
    }

    private boolean isBlacklistedIp(ServerHttpRequest request) {
        String clientIp = getClientIp(request);
        return blacklistService.isIpBlacklisted(clientIp);
    }

    private boolean checkRateLimit(ServerHttpRequest request) {
        String clientIp = getClientIp(request);
        String uri = request.getURI().getPath();

        // 不同的接口有不同的限流规则
        RateLimitConfig config = getRateLimitConfig(uri);
        String key = "rate_limit:" + clientIp + ":" + uri;

        return rateLimiter.tryAcquire(key, config.getPermits(), config.getTimeWindow());
    }

    private boolean isPublicPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        // 公开的API路径
        String[] publicPaths = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/public/**",
            "/actuator/health",
            "/actuator/info"
        };

        return Arrays.stream(publicPaths)
            .anyMatch(publicPath -> path.matches(publicPath.replace("**", ".*")));
    }

    private String extractToken(ServerHttpRequest request) {
        // 从Authorization头获取token
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 从查询参数获取token（用于WebSocket等场景）
        String tokenParam = request.getQueryParams().getFirst("token");
        if (StringUtils.isNotBlank(tokenParam)) {
            return tokenParam;
        }

        return null;
    }

    private boolean checkPermission(ServerHttpRequest request, UserClaims userClaims) {
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // 获取用户权限
        Set<String> userPermissions = permissionService.getUserPermissions(userClaims.getUserId());

        // 检查是否有访问权限
        return permissionService.hasPermission(userPermissions, path, method);
    }

    private ServerHttpRequest addUserHeaders(ServerHttpRequest request, UserClaims userClaims) {
        return request.mutate()
            .header("X-User-Id", String.valueOf(userClaims.getUserId()))
            .header("X-Username", userClaims.getUsername())
            .header("X-User-Role", String.join(",", userClaims.getRoles()))
            .header("X-Request-Id", UUID.randomUUID().toString())
            .header("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()))
            .build();
    }

    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(401)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();

        DataBuffer buffer = response.bufferFactory().wrap(JsonUtils.toJson(errorResponse).getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleForbidden(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(403)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();

        DataBuffer buffer = response.bufferFactory().wrap(JsonUtils.toJson(errorResponse).getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleRateLimitExceeded(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(429)
            .message("请求过于频繁，请稍后重试")
            .timestamp(LocalDateTime.now())
            .build();

        DataBuffer buffer = response.bufferFactory().wrap(JsonUtils.toJson(errorResponse).getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleBlacklistedIp(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(403)
            .message("IP地址已被禁止访问")
            .timestamp(LocalDateTime.now())
            .build();

        DataBuffer buffer = response.bufferFactory().wrap(JsonUtils.toJson(errorResponse).getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    private void recordAccessLog(ServerHttpRequest request, UserClaims userClaims) {
        try {
            AccessLogEvent event = AccessLogEvent.builder()
                .userId(userClaims.getUserId())
                .username(userClaims.getUsername())
                .ip(getClientIp(request))
                .uri(request.getURI().toString())
                .method(request.getMethod().name())
                .userAgent(request.getHeaders().getFirst("User-Agent"))
                .timestamp(LocalDateTime.now())
                .build();

            // 异步记录访问日志
            CompletableFuture.runAsync(() -> {
                try {
                    // 发送到消息队列或直接写入数据库
                    publishAccessLogEvent(event);
                } catch (Exception e) {
                    log.error("记录访问日志失败", e);
                }
            });

        } catch (Exception e) {
            log.error("构建访问日志事件失败", e);
        }
    }
}
```

### 📊 限流熔断机制
```java
// 智能限流和熔断管理
@Service
@Slf4j
public class CircuitBreakerManager {

    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;

    public CircuitBreakerManager(MeterRegistry meterRegistry,
                                 RedisTemplate<String, Object> redisTemplate) {
        this.meterRegistry = meterRegistry;
        this.redisTemplate = redisTemplate;
        initializeDefaultCircuitBreakers();
    }

    /**
     * 执行带熔断保护的方法
     */
    public <T> T executeWithCircuitBreaker(String serviceName, Supplier<T> supplier, Supplier<T> fallback) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName);

        try {
            // 记录调用次数
            meterRegistry.counter("circuitbreaker.calls", "service", serviceName, "status", "attempt").increment();

            T result = circuitBreaker.executeSupplier(supplier);

            // 记录成功调用
            meterRegistry.counter("circuitbreaker.calls", "service", serviceName, "status", "success").increment();

            return result;

        } catch (Exception e) {
            // 记录失败调用
            meterRegistry.counter("circuitbreaker.calls", "service", serviceName, "status", "failure").increment();

            log.warn("服务调用失败，启用熔断降级: service={}, error={}", serviceName, e.getMessage());

            if (fallback != null) {
                try {
                    T fallbackResult = fallback.get();
                    meterRegistry.counter("circuitbreaker.calls", "service", serviceName, "status", "fallback").increment();
                    return fallbackResult;
                } catch (Exception fallbackException) {
                    log.error("熔断降级也失败: service={}", serviceName, fallbackException);
                    throw new CircuitBreakerOpenException("服务不可用且降级失败", fallbackException);
                }
            }

            throw new CircuitBreakerOpenException("服务不可用", e);
        }
    }

    /**
     * 检查限流
     */
    public boolean checkRateLimit(String key, int permits, Duration timeWindow) {
        RateLimiter rateLimiter = getRateLimiter(key, permits, timeWindow);
        return rateLimiter.tryAcquire();
    }

    /**
     * 获取熔断器状态
     */
    public CircuitBreakerState getCircuitBreakerState(String serviceName) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName);

        return CircuitBreakerState.builder()
            .serviceName(serviceName)
            .state(circuitBreaker.getState().toString())
            .failureRate(circuitBreaker.getMetrics().getFailureRate())
            .bufferedCalls(circuitBreaker.getMetrics().getNumberOfBufferedCalls())
            .failedCalls(circuitBreaker.getMetrics().getNumberOfFailedCalls())
            .notPermittedCalls(circuitBreaker.getMetrics().getNumberOfNotPermittedCalls())
            .build();
    }

    /**
     * 手动重置熔断器
     */
    public void resetCircuitBreaker(String serviceName) {
        CircuitBreaker circuitBreaker = circuitBreakers.get(serviceName);
        if (circuitBreaker != null) {
            circuitBreaker.transitionToClosedState();
            log.info("熔断器已重置: service={}", serviceName);
        }
    }

    private CircuitBreaker getCircuitBreaker(String serviceName) {
        return circuitBreakers.computeIfAbsent(serviceName, this::createCircuitBreaker);
    }

    private RateLimiter getRateLimiter(String key, int permits, Duration timeWindow) {
        return rateLimiters.computeIfAbsent(key, k -> createRateLimiter(permits, timeWindow));
    }

    private CircuitBreaker createCircuitBreaker(String serviceName) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)                      // 失败率阈值50%
            .waitDurationInOpenState(Duration.ofSeconds(30))  // 熔断开启后等待30秒
            .ringBufferSizeInHalfOpenState(10)             // 半开状态缓冲区大小
            .ringBufferSizeInClosedState(100)              // 闭状态缓冲区大小
            .recordExceptions(
                IOException.class,
                TimeoutException.class,
                BusinessException.class
            )
            .build();

        // 创建自定义熔断器状态监听器
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker(serviceName);

        // 添加状态变化监听器
        circuitBreaker.getEventPublisher()
            .onStateTransition(event ->
                log.info("熔断器状态变化: service={}, from={}, to={}",
                    serviceName, event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState()))
            .onCallNotPermitted(event ->
                log.warn("熔断器拒绝调用: service={}", serviceName))
            .onError(event ->
                log.error("熔断器记录错误: service={}, error={}", serviceName, event.getException().getMessage()));

        // 注册监控指标
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(5))
            .build();

        return circuitBreaker;
    }

    private RateLimiter createRateLimiter(int permits, Duration timeWindow) {
        return RateLimiter.create(permits, timeWindow);
    }

    private void initializeDefaultCircuitBreakers() {
        // 为常用服务初始化熔断器
        String[] defaultServices = {
            "common-service",
            "access-service",
            "attendance-service",
            "consume-service",
            "visitor-service",
            "video-service",
            "device-comm-service",
            "oa-service"
        };

        for (String serviceName : defaultServices) {
            getCircuitBreaker(serviceName);
            log.info("初始化服务熔断器: service={}", serviceName);
        }
    }

    /**
     * 动态调整熔断器配置
     */
    public void updateCircuitBreakerConfig(String serviceName, CircuitBreakerConfig newConfig) {
        CircuitBreaker oldCircuitBreaker = circuitBreakers.remove(serviceName);
        if (oldCircuitBreaker != null) {
            // 创建新的熔断器
            CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(newConfig);
            CircuitBreaker newCircuitBreaker = registry.circuitBreaker(serviceName);
            circuitBreakers.put(serviceName, newCircuitBreaker);

            log.info("熔断器配置已更新: service={}", serviceName);
        }
    }

    /**
     * 获取所有熔断器状态
     */
    public List<CircuitBreakerState> getAllCircuitBreakerStates() {
        return circuitBreakers.entrySet().stream()
            .map(entry -> getCircuitBreakerState(entry.getKey()))
            .collect(Collectors.toList());
    }
}
```

---

## 🏗️ 架构设计规范

### 四层架构实现

#### Controller层 - 接口控制层
```java
@RestController
@RequestMapping("/api/v1/gateway")
@Tag(name = "网关管理")
@Validated
public class GatewayController {

    @Resource
    private GatewayRouteService gatewayRouteService;

    @PostMapping("/route/add")
    @Operation(summary = "添加路由")
    public ResponseDTO<Void> addRoute(@Valid @RequestBody RouteDefinitionDTO request) {
        boolean success = gatewayRouteService.addRoute(convertToRouteDefinition(request));
        return success ? ResponseDTO.ok() : ResponseDTO.error("ROUTE_ADD_FAILED", "路由添加失败");
    }

    @DeleteMapping("/route/{routeId}")
    @Operation(summary = "删除路由")
    public ResponseDTO<Void> deleteRoute(@PathVariable String routeId) {
        boolean success = gatewayRouteService.deleteRoute(routeId);
        return success ? ResponseDTO.ok() : ResponseDTO.error("ROUTE_DELETE_FAILED", "路由删除失败");
    }

    @GetMapping("/route/list")
    @Operation(summary = "获取路由列表")
    public ResponseDTO<List<RouteDefinitionVO>> getRouteList() {
        List<RouteDefinition> routes = gatewayRouteService.getAllRoutes();
        return ResponseDTO.ok(routes.stream()
            .map(this::convertToRouteVO)
            .collect(Collectors.toList()));
    }
}
```

#### Service层 - 核心业务层
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class GatewayRouteServiceImpl implements GatewayRouteService {

    @Resource
    private DynamicRouteManager dynamicRouteManager;

    @Override
    public boolean addRoute(RouteDefinition definition) {
        // 业务规则验证
        validateRouteDefinition(definition);

        // 核心业务逻辑
        return dynamicRouteManager.addRoute(definition);
    }

    private void validateRouteDefinition(RouteDefinition definition) {
        // 验证路由ID唯一性
        if (routeExists(definition.getId())) {
            throw new BusinessException("ROUTE_ID_EXISTS", "路由ID已存在");
        }

        // 验证服务是否存在
        if (isServiceRoute(definition)) {
            String serviceName = extractServiceName(definition.getUri());
            if (!serviceExists(serviceName)) {
                throw new BusinessException("SERVICE_NOT_FOUND", "目标服务不存在");
            }
        }
    }
}
```

#### Manager层 - 复杂流程管理层
```java
// ✅ 正确：Manager类为纯Java类，不使用Spring注解
public class GatewayRouteManager {

    private final RouteDefinitionWriter routeDefinitionWriter;
    private final ServiceDiscoveryClient serviceDiscoveryClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher publisher;

    // 构造函数注入依赖
    public GatewayRouteManager(RouteDefinitionWriter routeDefinitionWriter,
                              ServiceDiscoveryClient serviceDiscoveryClient,
                              RedisTemplate<String, Object> redisTemplate,
                              ApplicationEventPublisher publisher) {
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.serviceDiscoveryClient = serviceDiscoveryClient;
        this.redisTemplate = redisTemplate;
        this.publisher = publisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public RouteManagementResult addServiceRoute(ServiceRouteRequestDTO request) {
        try {
            // 1. 验证服务是否注册
            ServiceInstance serviceInstance = validateServiceRegistration(request.getServiceName());

            // 2. 构建路由定义
            RouteDefinition routeDefinition = buildServiceRouteDefinition(request, serviceInstance);

            // 3. 保存路由定义
            saveRouteDefinition(routeDefinition);

            // 4. 配置负载均衡策略
            configureLoadBalancing(routeDefinition, request.getLoadBalancingStrategy());

            // 5. 设置健康检查
            setupHealthCheck(routeDefinition, serviceInstance);

            // 6. 发布路由变更事件
            publishRouteChangeEvent(routeDefinition, RouteChangeTypeEnum.ADDED);

            // 7. 更新路由缓存
            updateRouteCache(routeDefinition);

            return RouteManagementResult.success(routeDefinition.getId());

        } catch (Exception e) {
            log.error("添加服务路由失败: serviceName={}", request.getServiceName(), e);
            throw new BusinessException("SERVICE_ROUTE_ADD_FAILED", "添加服务路由失败", e);
        }
    }

    private ServiceInstance validateServiceRegistration(String serviceName) {
        List<ServiceInstance> instances = serviceDiscoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            throw new BusinessException("SERVICE_NOT_REGISTERED",
                "服务未注册或没有可用实例: " + serviceName);
        }

        // 选择一个健康的实例
        return instances.stream()
            .filter(instance -> isInstanceHealthy(instance))
            .findFirst()
            .orElseThrow(() -> new BusinessException("NO_HEALTHY_INSTANCE",
                "服务没有健康的实例: " + serviceName));
    }

    private RouteDefinition buildServiceRouteDefinition(ServiceRouteRequestDTO request, ServiceInstance serviceInstance) {
        RouteDefinition definition = new RouteDefinition();

        // 设置路由ID
        definition.setId(request.getServiceName() + "-route");

        // 设置路径断言
        definition.setPredicates(Arrays.asList(
            new PathPredicate(request.getPathPattern()),
            new HostPredicate(extractAllowedHosts(request))
        ));

        // 设置目标URI（负载均衡）
        definition.setUri(URI.create("lb://" + request.getServiceName()));

        // 设置过滤器
        List<GatewayFilter> filters = new ArrayList<>();

        // 路径过滤
        filters.add(new StripPrefixGatewayFilterFactory().apply(request.getStripPrefix()));

        // 重试配置
        if (request.getRetryConfig() != null) {
            filters.add(buildRetryFilter(request.getRetryConfig()));
        }

        // 超时配置
        if (request.getTimeoutConfig() != null) {
            filters.add(buildTimeoutFilter(request.getTimeoutConfig()));
        }

        // 安全配置
        filters.addAll(buildSecurityFilters(request.getSecurityConfig()));

        definition.setFilters(filters);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("serviceName", request.getServiceName());
        metadata.put("routeType", "service");
        metadata.put("createTime", LocalDateTime.now().toString());
        metadata.put("createdBy", SecurityUtils.getCurrentUsername());
        metadata.put("instanceCount", serviceDiscoveryClient.getInstances(request.getServiceName()).size());

        definition.setMetadata(metadata);

        return definition;
    }

    private void configureLoadBalancing(RouteDefinition routeDefinition, String strategy) {
        // 配置负载均衡策略
        Map<String, Object> metadata = routeDefinition.getMetadata();
        metadata.put("loadBalancingStrategy", strategy);

        // 根据策略设置不同的负载均衡器
        switch (LoadBalancingStrategyEnum.fromCode(strategy)) {
            case ROUND_ROBIN:
                metadata.put("LoadBalancer", "RoundRobinLoadBalancer");
                break;
            case WEIGHTED_RESPONSE:
                metadata.put("LoadBalancer", "WeightedResponseLoadBalancer");
                break;
            case RANDOM:
                metadata.put("LoadBalancer", "RandomLoadBalancer");
                break;
            case LEAST_CONNECTIONS:
                metadata.put("LoadBalancer", "LeastConnectionsLoadBalancer");
                break;
            default:
                metadata.put("LoadBalancer", "RoundRobinLoadBalancer");
        }
    }

    private void setupHealthCheck(RouteDefinition routeDefinition, ServiceInstance serviceInstance) {
        // 设置健康检查配置
        HealthCheckConfig healthCheck = HealthCheckConfig.builder()
            .enabled(true)
            .interval(Duration.ofSeconds(30))
            .timeout(Duration.ofSeconds(5))
            .unhealthyThreshold(3)
            .healthyThreshold(2)
            .healthCheckPath("/actuator/health")
            .build();

        Map<String, Object> metadata = routeDefinition.getMetadata();
        metadata.put("healthCheck", healthCheck);

        // 启动健康检查任务
        startHealthCheckTask(routeDefinition.getId(), serviceInstance, healthCheck);
    }

    private void startHealthCheckTask(String routeId, ServiceInstance serviceInstance, HealthCheckConfig config) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                boolean isHealthy = performHealthCheck(serviceInstance, config);
                updateServiceHealthStatus(routeId, serviceInstance.getServiceId(), isHealthy);

                if (!isHealthy) {
                    log.warn("服务实例健康检查失败: serviceId={}, instanceId={}",
                        serviceInstance.getServiceId(), serviceInstance.getInstanceId());
                }

            } catch (Exception e) {
                log.error("健康检查异常: serviceId={}", serviceInstance.getServiceId(), e);
            }
        }, 0, config.getInterval().getSeconds(), TimeUnit.SECONDS);

        // 保存调度器引用以便后续清理
        healthCheckSchedulers.put(routeId, scheduler);
    }

    private boolean performHealthCheck(ServiceInstance instance, HealthCheckConfig config) {
        try {
            URI healthCheckUri = URI.create(String.format("http://%s:%d%s",
                instance.getHost(), instance.getPort(), config.getHealthCheckPath()));

            HttpClient httpClient = HttpClient.create()
                .responseTimeout(config.getTimeout())
                .connectTimeout(config.getTimeout());

            return httpClient.get()
                .uri(healthCheckUri)
                .response()
                .map(response -> response.status().is2xxSuccessful())
                .timeout(config.getTimeout())
                .onErrorReturn(false)
                .block();

        } catch (Exception e) {
            log.debug("健康检查请求异常: instanceId={}, error={}", instance.getInstanceId(), e.getMessage());
            return false;
        }
    }

    private void publishRouteChangeEvent(RouteDefinition routeDefinition, RouteChangeTypeEnum changeType) {
        RouteChangeEvent event = RouteChangeEvent.builder()
            .routeId(routeDefinition.getId())
            .routeDefinition(routeDefinition)
            .changeType(changeType)
            .timestamp(LocalDateTime.now())
            .operator(SecurityUtils.getCurrentUsername())
            .build();

        // 异步发布事件
        CompletableFuture.runAsync(() -> {
            try {
                publisher.publishEvent(event);
            } catch (Exception e) {
                log.error("发布路由变更事件失败: routeId={}", routeDefinition.getId(), e);
            }
        });
    }
}
```

#### DAO层 - 数据访问层
```java
@Mapper
public interface GatewayRouteDao extends BaseMapper<GatewayRouteEntity> {

    @Transactional(readOnly = true)
    List<GatewayRouteEntity> selectByRouteType(@Param("routeType") String routeType);

    @Transactional(readOnly = true)
    List<GatewayRouteEntity> selectByStatus(@Param("status") Integer status);

    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("routeId") String routeId, @Param("status") Integer status);

    @Select("SELECT * FROM t_gateway_route WHERE service_name = #{serviceName} AND deleted_flag = 0")
    List<GatewayRouteEntity> selectByServiceName(@Param("serviceName") String serviceName);

    @Transactional(readOnly = true)
    List<GatewayRouteEntity> selectExpiredRoutes(@Param("expireTime") LocalDateTime expireTime);
}

@Mapper
public interface RouteAccessLogDao extends BaseMapper<RouteAccessLogEntity> {

    @Transactional(readOnly = true)
    List<RouteAccessLogEntity> selectByRouteId(@Param("routeId") String routeId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    @Transactional(readOnly = true)
    List<RouteAccessLogEntity> selectByClientIp(@Param("clientIp") String clientIp,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    @Transactional(rollback for = Exception.class)
    int batchInsert(@Param("logs") List<RouteAccessLogEntity> logs);

    @Select("SELECT COUNT(*) FROM t_route_access_log WHERE route_id = #{routeId} " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime}")
    int countByRouteIdAndTime(@Param("routeId") String routeId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);
}
```

---

## 📊 技能质量指标体系

### 核心质量指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **路由匹配准确率** | 100% | 路由规则匹配准确性 | 路由匹配测试 |
| **请求响应时间** | ≤100ms | 网关请求处理时间 | 响应时间监控 |
| **认证授权成功率** | ≥99.9% | 身份认证和权限验证成功率 | 认证成功率监控 |
| **限流熔断准确率** | ≥99% | 限流熔断机制准确性 | 限流熔断测试 |
| **系统可用性** | ≥99.95% | 网关系统可用性 | 系统可用性监控 |

### 性能指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **并发处理能力** | ≥50000 RPS | 并发请求处理能力 | 并发性能测试 |
| **路由配置更新时间** | ≤5s | 路由配置更新生效时间 | 配置更新测试 |
| **健康检查响应时间** | ≤2s | 服务健康检查响应时间 | 健康检查测试 |
| **限流精度** | 99% | 限流机制精确度 | 限流精度测试 |

### 安全指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **攻击拦截率** | ≥99.9% | 恶意请求拦截成功率 | 安全攻击测试 |
| **数据加密覆盖率** | 100% | 敏感数据传输加密比例 | 数据安全检查 |
| **访问日志完整性** | 100% | 访问日志记录完整性 | 日志完整性检查 |
| **权限控制准确率** | 100% | 权限验证准确度 | 权限控制测试 |

### 版本管理
- **主版本**: v1.0.0 - 初始版本
- **文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
- **创建时间**: 2025-12-08
- **最后更新**: 2025-12-08
- **变更类型**: MAJOR - 新技能创建

---

## 🛠️ 开发规范和最佳实践

### 路由配置最佳实践
```java
// ✅ 正确的路由配置
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("common-service", r -> r.path("/api/v1/common/**")
            .filters(f -> f.stripPrefix(2)
                .addRequestHeader("X-Source", "gateway")
                .retry(retryConfig -> retryConfig.setRetries(3).setBackoff(BackoffConfig.exponential(100, 2, 1000))))
            .uri("lb://common-service"))
        .route("access-service", r -> r.path("/api/v1/access/**")
            .filters(f -> f.stripPrefix(2)
                .circuitBreaker(config -> config.setName("access-service").setFallbackUri("forward:/fallback/access")))
            .uri("lb://access-service"))
        .build();
}
```

### 安全过滤器最佳实践
```java
// ✅ 正确的安全过滤顺序
@Configuration
public class GatewayConfig {

    @Bean
    public GlobalFilter securityFilter() {
        return new SecurityGlobalFilter(); // 认证过滤器
    }

    @Bean
    public GlobalFilter rateLimitFilter() {
        return new RateLimitGlobalFilter(); // 限流过滤器
    }

    @Bean
    public GlobalFilter loggingFilter() {
        return new LoggingGlobalFilter(); // 日志过滤器
    }
}
```

---

## 🔗 相关文档参考

### 核心架构文档
- **📋 CLAUDE.md**: 全局架构规范 (强制遵循)
- **🏗️ 四层架构详解**: Controller→Service→Manager→DAO架构模式
- **🔧 依赖注入规范**: 统一使用@Resource注解
- **📦 DAO层规范**: 统一使用Dao后缀和@Mapper注解

### 技术栈文档
- **Spring Cloud Gateway**: API网关框架文档
- **Nacos**: 服务注册发现和配置中心
- **Redis**: 分布式缓存和限流存储
- **Resilience4j**: 熔断限流库文档

### 安全规范文档
- **🔒 API安全规范**: API接口安全要求
- **🛡️ 网关安全架构**: 网关安全防护体系
- **📊 监控告警规范**: 系统监控和告警配置

---

**📋 重要提醒**:
1. 本技能严格遵循IOE-DREAM四层架构规范
2. 所有代码示例使用Jakarta EE 3.0+包名规范
3. 统一使用@Resource依赖注入，禁止使用@Autowired
4. 统一使用@Mapper注解和Dao后缀命名
5. 重点关注高并发、高可用的网关架构设计
6. 必须支持动态路由配置和服务发现
7. 严格遵循安全防护和访问控制要求

**让我们一起建设稳定、高效的API网关体系！** 🚀

---
**文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
**创建时间**: 2025-12-08
**最后更新**: 2025-12-08
**技能等级**: ★★★★★ (顶级专家)
**适用架构**: Spring Cloud Gateway + Nacos + Redis + Resilience4j