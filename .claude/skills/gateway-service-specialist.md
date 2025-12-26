# 网关服务专家

## 🎯 核心职责

负责统一微服务间调用的GatewayServiceClient开发与优化，确保服务间通信的高效、稳定和安全。

## 🔧 技术栈

- **框架**: Spring Boot 3.5.8 + Microservices Gateway Client
- **服务发现**: Nacos
- **熔断限流**: Resilience4j
- **安全认证**: Spring Security + JWT
- **序列化**: Jackson
- **HTTP客户端**: RestTemplate
- **监控**: Micrometer + Prometheus

## 💡 核心专长

### 1. GatewayServiceClient设计与优化
- 统一服务调用接口设计
- RestTemplate配置优化
- 泛型类型处理
- 异常处理与重试机制

### 2. 服务间通信模式
- 同步调用模式
- 超时与重试策略
- 熔断降级机制
- 负载均衡配置

### 3. 安全与认证
- JWT Token传递
- 服务间鉴权
- 安全头配置
- 敏感数据保护

### 4. 性能与监控
- 调用链路追踪
- 性能指标收集
- 监控告警配置
- 问题诊断定位

## 🚀 最佳实践

### GatewayServiceClient核心实现
```java
@Slf4j
@Component
public class GatewayServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String gatewayBaseUrl;

    public GatewayServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper, String gatewayBaseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.gatewayBaseUrl = gatewayBaseUrl;
    }

    /**
     * 将Jackson TypeReference转换为Spring ParameterizedTypeReference
     */
    private <T> ParameterizedTypeReference<T> toParameterizedTypeReference(TypeReference<T> typeReference) {
        return new ParameterizedTypeReference<T>() {
            @Override
            public java.lang.reflect.Type getType() {
                return typeReference.getType();
            }
        };
    }

    /**
     * 调用公共服务
     */
    public <T> T callCommonService(String apiPath, HttpMethod method, Object request,
                                     Class<T> responseType) {
        log.info("[网关调用] 调用公共服务: {} {} with request: {}", method, apiPath, request);
        try {
            String url = gatewayBaseUrl + apiPath;
            if (request != null) {
                return restTemplate.postForObject(url, request, responseType);
            } else {
                return restTemplate.getForObject(url, responseType);
            }
        } catch (Exception e) {
            log.error("[网关调用] 调用公共服务失败: {} {}", apiPath, e.getMessage(), e);
            throw new RuntimeException("调用公共服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用公共服务（Map参数）
     */
    public <T> T callCommonService(String apiPath, HttpMethod method, Map<String, Object> request,
                                     Class<T> responseType) {
        log.info("[网关调用] 调用公共服务: {} with map request: {}", method, apiPath, request);
        try {
            String url = gatewayBaseUrl + apiPath;
            if (request != null) {
                return restTemplate.postForObject(url, request, responseType);
            } else {
                return restTemplate.getForObject(url, responseType);
            }
        } catch (Exception e) {
            log.error("[网关调用] 调用公共服务失败: {} {}", apiPath, e.getMessage(), e);
            throw new RuntimeException("调用公共服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用OA服务
     */
    public <T> T callOAService(String apiPath, HttpMethod method, Map<String, Object> request,
                                 Class<T> responseType) {
        log.info("[网关调用] 调用OA服务: {} {} with request: {}", method, apiPath, request);
        String url = gatewayBaseUrl + "/oa" + apiPath;
        return restTemplate.postForObject(url, request, responseType);
    }

    /**
     * 调用设备通讯服务
     */
    public <T> T callDeviceCommService(String apiPath, HttpMethod method, Map<String, Object> request,
                                       Class<T> responseType) {
        log.info("[网关调用] 调用设备通讯服务: {} {} with request: {}", method, apiPath, request);
        String url = gatewayBaseUrl + "/device-comm" + apiPath;
        return restTemplate.postForObject(url, request, responseType);
    }

    /**
     * 调用消费服务
     */
    public <T> T callConsumeService(String apiPath, HttpMethod method, Map<String, Object> request,
                                   Class<T> responseType) {
        log.info("[网关调用] 调用消费服务: {} {} with request: {}", method, apiPath, request);
        String url = gatewayBaseUrl + "/consume" + apiPath;
        return restTemplate.postForObject(url, request, responseType);
    }

    /**
     * 调用考勤服务
     */
    public <T> T callAttendanceService(String apiPath, HttpMethod method, Map<String, Object> request,
                                       Class<T> responseType) {
        log.info("[网关调用] 调用考勤服务: {} {} with request: {}", method, apiPath, request);
        String url = gatewayBaseUrl + "/attendance" + apiPath;
        return restTemplate.postForObject(url, request, responseType);
    }

    /**
     * 调用访客服务
     */
    public <T> T callVisitorService(String apiPath, HttpMethod method, Map<String, Object> request,
                                   Class<T> responseType) {
        log.info("[网关调用] 调用访客服务: {} {} with request: {}", method, apiPath, request);
        return callCommonService(apiPath, method, request, responseType);
    }

    /**
     * 调用门禁服务
     */
    public <T> T callAccessService(String apiPath, HttpMethod method, Map<String, Object> request,
                                  Class<T> responseType) {
        log.info("[网关调用] 调用门禁服务: {} {} with request: {}", method, apiPath, request);
        return callCommonService(apiPath, method, request, responseType);
    }

    /**
     * 调用公共服务（TypeReference）
     */
    public <T> T callCommonService(String apiPath, HttpMethod method, Object request,
                                  TypeReference<T> responseType) {
        log.info("[网关调用] TypeReference调用公共服务: {} {} with request: {}", method, apiPath, request);
        try {
            String url = gatewayBaseUrl + apiPath;
            T result;
            ParameterizedTypeReference<T> paramTypeRef = toParameterizedTypeReference(responseType);
            if (request != null && method == HttpMethod.POST) {
                HttpEntity<Object> entity = new HttpEntity<>(request);
                result = restTemplate.exchange(url, method, entity, paramTypeRef).getBody();
            } else {
                result = restTemplate.exchange(url, method, null, paramTypeRef).getBody();
            }

            if (result != null) {
                return result;
            }
            throw new RuntimeException("服务调用返回空响应");
        } catch (Exception e) {
            log.error("[网关调用] TypeReference调用公共服务失败: {} {}", apiPath, e.getMessage(), e);
            throw new RuntimeException("调用公共服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用公共服务（Map参数，TypeReference）
     */
    public <T> T callCommonService(String apiPath, HttpMethod method, Map<String, Object> request,
                                  TypeReference<T> responseType) {
        log.info("[网关调用] TypeReference调用公共服务: {} {} with map request: {}", method, apiPath, request);
        try {
            String url = gatewayBaseUrl + apiPath;
            T result;
            ParameterizedTypeReference<T> paramTypeRef = toParameterizedTypeReference(responseType);
            if (request != null && method == HttpMethod.POST) {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request);
                result = restTemplate.exchange(url, method, entity, paramTypeRef).getBody();
            } else {
                result = restTemplate.exchange(url, method, null, paramTypeRef).getBody();
            }

            if (result != null) {
                return result;
            }
            throw new RuntimeException("服务调用返回空响应");
        } catch (Exception e) {
            log.error("[网关调用] TypeReference调用公共服务失败: {} {}", apiPath, e.getMessage(), e);
            throw new RuntimeException("调用公共服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 简单的服务调用
     */
    public void invokeService(String serviceName, String apiPath) {
        log.info("[网关调用] 调用服务: {} at path: {}", serviceName, apiPath);
    }
}
```

### RestTemplate配置最佳实践
```java
@Configuration
public class GatewayClientConfiguration {

    @Bean
    public RestTemplate gatewayRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 设置超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 连接超时5秒
        factory.setReadTimeout(30000);    // 读取超时30秒

        restTemplate.setRequestFactory(factory);

        // 配置消息转换器
        restTemplate.setMessageConverters(createMessageConverters());

        return restTemplate;
    }

    private List<HttpMessageConverter<?>> createMessageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        // String转换器
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // JSON转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(createObjectMapper());
        converters.add(jsonConverter);

        return converters;
    }

    @Bean
    public ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 时间格式化
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 序列化配置
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return objectMapper;
    }
}
```

### 服务调用熔断配置
```java
@Configuration
public class Resilience4jConfiguration {

    @Bean
    public CircuitBreaker gatewayCircuitBreaker() {
        return CircuitBreaker.ofDefaults("gateway-service");
    }

    @Bean
    public TimeLimiter gatewayTimeLimiter() {
        return TimeLimiter.of(Duration.ofSeconds(30));
    }
}
```

### 业务服务中的使用示例
```java
@Service
@Slf4j
public class ConsumeReportServiceImpl implements ConsumeReportService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Override
    public Map<String, Object> getDeviceOperationReport(String deviceId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("deviceId", deviceId);
            request.put("startDate", startDate);
            request.put("endDate", endDate);

            // 调用设备通讯服务获取设备数据
            Map<String, Object> deviceData = gatewayServiceClient.callDeviceCommService(
                "/api/v1/device/statistics/operation",
                HttpMethod.POST,
                request,
                Map.class
            );

            // 处理返回数据
            return processDeviceData(deviceData);

        } catch (Exception e) {
            log.error("[消费报表] 获取设备运营报表失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            throw new BusinessException("DEVICE_DATA_ERROR", "获取设备数据失败");
        }
    }

    @Override
    public List<Map<String, Object>> getConsumptionRanking(String type, Integer limit, String startDate, String endDate) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("type", type);
            request.put("limit", limit);
            request.put("startDate", startDate);
            request.put("endDate", endDate);

            // 使用TypeReference处理复杂泛型返回
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};

            return gatewayServiceClient.callCommonService(
                "/api/v1/statistics/consumption/ranking",
                HttpMethod.POST,
                request,
                typeRef
            );

        } catch (Exception e) {
            log.error("[消费报表] 获取消费排行榜失败: type={}, error={}", type, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> processDeviceData(Map<String, Object> deviceData) {
        // 处理设备数据的业务逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsage", deviceData.get("usage"));
        result.put("activeHours", deviceData.get("activeTime"));
        result.put("errorCount", deviceData.get("errors"));
        return result;
    }
}
```

## 🔍 监控与诊断

### 调用链路追踪配置
```java
@Configuration
public class TracingConfiguration {

    @Bean
    public RestTemplateCustomizer restTemplateCustomizer(Tracer tracer) {
        return restTemplate -> {
            restTemplate.setInterceptors(List.of(new TracingClientHttpRequestInterceptor(tracer)));
        };
    }
}
```

### 性能监控指标
```java
@Component
@Slf4j
public class GatewayServiceMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter serviceCallCounter;
    private final Timer serviceCallTimer;

    public GatewayServiceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.serviceCallCounter = Counter.builder("gateway.service.calls")
            .description("Gateway service call count")
            .register(meterRegistry);
        this.serviceCallTimer = Timer.builder("gateway.service.duration")
            .description("Gateway service call duration")
            .register(meterRegistry);
    }

    public <T> T recordServiceCall(String serviceName, String apiPath, Supplier<T> supplier) {
        return Timer.Sample.start(meterRegistry)
            .stop(serviceCallTimer, () -> {
                serviceCallCounter.increment(Tags.of("service", serviceName, "api", apiPath));
                return supplier.get();
            });
    }
}
```

## 📋 常见问题解决

### 1. RestTemplate泛型类型问题
```java
// 问题：RestTemplate.exchange()泛型类型推导错误
// 解决：使用ParameterizedTypeReference包装复杂泛型类型

// ✅ 正确做法
ParameterizedTypeReference<List<UserVO>> typeRef = new ParameterizedTypeReference<List<UserVO>>() {};
ResponseEntity<List<UserVO>> response = restTemplate.exchange(url, HttpMethod.GET, null, typeRef);
```

### 2. 序列化问题
```java
// 问题：Jackson序列化LocalDateTime异常
// 解决：配置ObjectMapper的时间处理

ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```

### 3. 连接超时配置
```java
// 问题：服务调用超时
// 解决：合理配置连接和读取超时

SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
factory.setConnectTimeout(5000);  // 连接超时
factory.setReadTimeout(30000);     // 读取超时
```

## 🎯 性能优化建议

1. **连接池配置**: 使用HttpClient连接池提升并发性能
2. **缓存策略**: 对不变数据实施本地缓存
3. **批量调用**: 支持批量API调用减少网络开销
4. **异步调用**: 对非关键路径使用异步调用
5. **熔断降级**: 配置合理的熔断策略避免级联故障

## 📚 相关文档

- [Microservices架构设计规范](../CLAUDE.md)
- [服务间通信标准](documentation/technical/SERVICE_COMMUNICATION_STANDARD.md)
- [熔断降级指南](documentation/technical/RESILIENCE4J_GUIDE.md)
- [监控告警配置](documentation/technical/MONITORING_SETUP_GUIDE.md)