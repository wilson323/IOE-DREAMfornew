# 服务发现专家

> **版本**: v1.0.0
> **更新时间**: 2025-11-21
> **分类**: 微服务技能 > 服务治理
> **标签**: ["服务发现", "Nacos", "负载均衡", "健康检查", "服务治理"]
> **技能等级**: ★★★ 专家级
> **适用角色**: 微服务架构师、DevOps工程师、高级开发工程师
> **前置技能**: microservices-architecture-specialist, spring-boot-jakarta-guardian
> **预计学时**: 40-60小时

---

## 📋 技能概述

本技能专注于微服务架构中的服务发现、注册与管理，基于Nacos构建高可用的服务治理体系。提供从服务注册、发现、负载均衡到健康检查的完整解决方案。

**核心组件**: Nacos Server + Nacos Client + Spring Cloud LoadBalancer
**核心目标**: 构建稳定、高效、可扩展的服务发现机制

---

## 🏗️ 服务发现架构设计

### 1. Nacos服务注册中心

#### 服务端部署配置
```yaml
# nacos/application.yml
server:
  port: 8848

spring:
  application:
    name: nacos-server
  datasource:
    platform: mysql
    url: jdbc:mysql://mysql-server:3306/nacos_config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
    username: ${MYSQL_USERNAME:nacos}
    password: ${MYSQL_PASSWORD:nacos}
    driver-class-name: com.mysql.cj.jdbc.Driver

nacos:
  core:
    auth:
      enabled: true
      default.token.secret.key: ${NACOS_AUTH_TOKEN:SecretKey012345678901234567890123456789012345678901234567890123456789}
      plugin.nacos.token.cache.enable: false

management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true
```

#### 客户端注册配置
```yaml
# bootstrap.yml (优先级高于application.yml)
spring:
  application:
    name: user-service
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:IOE-DREAM}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        enabled: true
        register-enabled: true
        ephemeral: true  # 临时实例
        weight: 1.0      # 权重
        metadata:
          version: ${SERVICE_VERSION:1.0.0}
          region: ${SERVICE_REGION:default}
          zone: ${SERVICE_ZONE:default}
          instance-id: ${INSTANCE_ID:${spring.application.name}:${spring.cloud.client.ip-address}:${server.port}}
        heart-beat:
          interval: 5000    # 心跳间隔
          timeout: 15000    # 心跳超时
        ip-delete-timeout: 30000  # IP删除超时
      config:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:IOE-DREAM}
        file-extension: yaml
        shared-configs:
          - data-id: common-config.yaml
            group: ${NACOS_GROUP:IOE-DREAM}
            refresh: true
```

### 2. 服务注册与注销

#### 自动服务注册
```java
@SpringBootApplication
@EnableNacosDiscovery
@EnableNacosConfig
@Slf4j
public class UserServiceApplication implements ApplicationRunner {

    @Value("${server.port}")
    private Integer serverPort;

    @Value("${spring.cloud.client.ip-address}")
    private String ipAddress;

    @Resource
    private NacosServiceRegistry nacosServiceRegistry;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Service starting on {}:{}", ipAddress, serverPort);

        // 手动注册服务（可选，自动注册已启用）
        registerService();
    }

    private void registerService() {
        try {
            Instance instance = new Instance();
            instance.setIp(ipAddress);
            instance.setPort(serverPort);
            instance.setWeight(1.0);
            instance.setHealthy(true);
            instance.setEphemeral(true);

            // 设置元数据
            Map<String, String> metadata = new HashMap<>();
            metadata.put("version", "1.0.0");
            metadata.put("startTime", Instant.now().toString());
            instance.setMetadata(metadata);

            nacosServiceRegistry.register(
                nacosDiscoveryProperties.getNacosRegistration());

            log.info("Service registered successfully: {}:{}", ipAddress, serverPort);
        } catch (Exception e) {
            log.error("Failed to register service", e);
        }
    }

    @PreDestroy
    public void unregisterService() {
        try {
            nacosServiceRegistry.deregister(
                nacosDiscoveryProperties.getNacosRegistration());
            log.info("Service unregistered successfully");
        } catch (Exception e) {
            log.error("Failed to unregister service", e);
        }
    }
}
```

#### 服务注销钩子
```java
@Component
@Slf4j
public class ServiceDeregistrationHook {

    @Resource
    private NacosServiceRegistry nacosServiceRegistry;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @EventListener
    public void handleContextClosedEvent(ContextClosedEvent event) {
        log.info("Application context is closing, deregistering from Nacos...");
        try {
            nacosServiceRegistry.deregister(
                nacosDiscoveryProperties.getNacosRegistration());
            log.info("Service deregistered successfully");
        } catch (Exception e) {
            log.error("Failed to deregister service", e);
        }
    }
}
```

---

## 🔍 服务发现与负载均衡

### 1. 服务发现客户端

#### Feign客户端配置
```java
@FeignClient(
    name = "user-service",
    path = "/api/v1/users",
    configuration = UserFeignConfiguration.class
)
public interface UserServiceClient {

    @GetMapping("/{userId}")
    ResponseDTO<UserVO> getUserById(@PathVariable("userId") Long userId);

    @PostMapping("/batch")
    ResponseDTO<List<UserVO>> getUsersByIds(@RequestBody List<Long> userIds);

    @GetMapping("/search")
    ResponseDTO<PageResult<UserVO>> searchUsers(@RequestParam UserQueryDTO query);

    @PutMapping("/{userId}/status")
    ResponseDTO<Boolean> updateUserStatus(@PathVariable("userId") Long userId,
                                        @RequestParam Integer status);
}
```

#### Feign配置类
```java
@Configuration
public class UserFeignConfiguration {

    @Bean
    public Logger.Level loggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            5000,  // 连接超时
            10000  // 读取超时
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 1000, 3);
    }
}
```

### 2. 负载均衡策略

#### 自定义负载均衡规则
```java
@Configuration
public class LoadBalancerConfiguration {

    @Bean
    @Primary
    public ReactorLoadBalancer<ServiceInstance> userServiceLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory factory) {

        String serviceName = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        return new RoundRobinLoadBalancer(
            factory.getLazyProvider(serviceName, ServiceInstanceListSupplier.class),
            serviceName
        );
    }

    @Bean
    public ReactorServiceInstanceLoadBalancer weightBasedLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory factory) {

        String serviceName = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        return new WeightBasedLoadBalancer(
            factory.getLazyProvider(serviceName, ServiceInstanceListSupplier.class),
            serviceName
        );
    }
}
```

#### 基于权重的负载均衡
```java
public class WeightBasedLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
            .getIfAvailable(NoopServiceInstanceListSupplier::new);

        return supplier.get(request).next()
            .map(serviceInstances -> getInstanceByWeight(serviceInstances));
    }

    private ServiceInstance getInstanceByWeight(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return null;
        }

        // 计算总权重
        int totalWeight = instances.stream()
            .mapToInt(instance -> getWeight(instance))
            .sum();

        if (totalWeight <= 0) {
            // 如果没有权重配置，使用轮询
            return instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
        }

        // 基于权重选择
        int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;

        for (ServiceInstance instance : instances) {
            currentWeight += getWeight(instance);
            if (randomWeight < currentWeight) {
                return instance;
            }
        }

        return instances.get(0);
    }

    private int getWeight(ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();
        String weightStr = metadata.get("weight");
        return weightStr != null ? Integer.parseInt(weightStr) : 1;
    }
}
```

---

## ❤️ 健康检查机制

### 1. 服务健康检查配置

#### 健康检查端点
```java
@RestController
@RequestMapping("/actuator")
public class HealthController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseDTO<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();

        // 应用状态
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", System.currentTimeMillis());

        // 检查数据库连接
        try {
            dataSource.getConnection().close();
            healthInfo.put("database", "UP");
        } catch (Exception e) {
            healthInfo.put("database", "DOWN");
            healthInfo.put("database_error", e.getMessage());
        }

        // 检查Redis连接
        try {
            redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(10));
            healthInfo.put("redis", "UP");
        } catch (Exception e) {
            healthInfo.put("redis", "DOWN");
            healthInfo.put("redis_error", e.getMessage());
        }

        // 检查业务关键功能
        try {
            userService.healthCheck();
            healthInfo.put("business", "UP");
        } catch (Exception e) {
            healthInfo.put("business", "DOWN");
            healthInfo.put("business_error", e.getMessage());
        }

        return ResponseDTO.ok(healthInfo);
    }

    @GetMapping("/health/readiness")
    public ResponseEntity<Map<String, String>> readiness() {
        // 准备就绪检查 - 服务是否准备好接收流量
        try {
            // 检查关键依赖
            dataSource.getConnection().close();
            redisTemplate.opsForValue().get("test");

            return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", String.valueOf(System.currentTimeMillis())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/health/liveness")
    public ResponseEntity<Map<String, String>> liveness() {
        // 存活检查 - 服务是否仍在运行
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}
```

### 2. 健康检查配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "health,info,metrics,prometheus"
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
      group:
        liveness:
          include: "livenessState"
          show-details: always
        readiness:
          include: "readinessState,db,redis"
          show-details: always
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
    defaults:
      enabled: false
    db:
      enabled: true
    redis:
      enabled: true
```

---

## 🔧 服务治理高级特性

### 1. 服务元数据管理

#### 动态元数据更新
```java
@Component
@Slf4j
public class ServiceMetadataManager {

    @Resource
    private NacosServiceRegistry nacosServiceRegistry;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Scheduled(fixedRate = 30000)  // 每30秒更新一次
    public void updateMetadata() {
        try {
            Instance instance = getCurrentInstance();
            if (instance != null) {
                Map<String, String> metadata = instance.getMetadata();

                // 更新动态元数据
                metadata.put("lastUpdateTime", Instant.now().toString());
                metadata.put("memoryUsage", getMemoryUsage());
                metadata.put("cpuUsage", getCpuUsage());
                metadata.put("requestCount", String.valueOf(getRequestCount()));

                nacosServiceRegistry.updateMetadata(
                    nacosDiscoveryProperties.getNacosRegistration(), instance);

                log.debug("Service metadata updated: {}", metadata);
            }
        } catch (Exception e) {
            log.error("Failed to update service metadata", e);
        }
    }

    private Instance getCurrentInstance() {
        try {
            return nacosDiscoveryProperties.getNacosRegistration().getInstance();
        } catch (Exception e) {
            log.error("Failed to get current instance", e);
            return null;
        }
    }

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        return String.format("%.2f%%", (double) usedMemory / totalMemory * 100);
    }

    private String getCpuUsage() {
        // 获取CPU使用率的实现
        return "0.0%";  // 简化实现
    }

    private long getRequestCount() {
        // 获取请求计数的实现
        return 0L;  // 简化实现
    }
}
```

### 2. 服务分组管理

#### 多环境服务隔离
```java
@Configuration
public class ServiceGroupConfiguration {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${service.region:default}")
    private String region;

    @Value("${service.zone:default}")
    private String zone;

    @Bean
    @Primary
    public NacosDiscoveryProperties nacosDiscoveryProperties() {
        NacosDiscoveryProperties properties = new NacosDiscoveryProperties();

        // 根据环境配置不同的命名空间和分组
        switch (activeProfile) {
            case "dev":
                properties.setNamespace("dev");
                properties.setGroup("IOE-DREAM-DEV");
                break;
            case "test":
                properties.setNamespace("test");
                properties.setGroup("IOE-DREAM-TEST");
                break;
            case "prod":
                properties.setNamespace("prod");
                properties.setGroup("IOE-DREAM-PROD");
                break;
            default:
                properties.setNamespace("dev");
                properties.setGroup("IOE-DREAM-DEV");
        }

        // 设置地理位置信息
        Map<String, String> metadata = new HashMap<>();
        metadata.put("region", region);
        metadata.put("zone", zone);
        metadata.put("environment", activeProfile);
        properties.setMetadata(metadata);

        return properties;
    }
}
```

### 3. 服务版本管理

#### 版本化服务发现
```java
@Component
public class ServiceVersionManager {

    @Resource
    private NacosServiceManager nacosServiceManager;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    public List<ServiceInstance> getInstancesByVersion(String serviceName, String version) {
        try {
            NamingService namingService = nacosServiceManager.getNamingService();

            List<Instance> instances = namingService.selectInstances(
                serviceName,
                nacosDiscoveryProperties.getGroup(),
                true
            );

            return instances.stream()
                .filter(instance -> version.equals(instance.getMetadata().get("version")))
                .map(this::convertToServiceInstance)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to get instances by version: {} {}", serviceName, version, e);
            return Collections.emptyList();
        }
    }

    public ServiceInstance getInstanceByVersion(String serviceName, String version) {
        List<ServiceInstance> instances = getInstancesByVersion(serviceName, version);
        if (instances.isEmpty()) {
            return null;
        }
        return instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
    }

    private ServiceInstance convertToServiceInstance(Instance instance) {
        DefaultServiceInstance serviceInstance = new DefaultServiceInstance();
        serviceInstance.setInstanceId(instance.getInstanceId());
        serviceInstance.setHost(instance.getIp());
        serviceInstance.setPort(instance.getPort());
        serviceInstance.setMetadata(instance.getMetadata());
        serviceInstance.setHealthy(instance.isHealthy());
        serviceInstance.setServiceId(instance.getServiceName());
        return serviceInstance;
    }
}
```

---

## 🔒 安全与权限控制

### 1. 服务间认证

#### JWT Token认证
```java
@Configuration
@EnableWebSecurity
public class ServiceSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtDecoder(jwtDecoder())
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}
```

### 2. 服务访问控制

#### 基于角色的服务访问
```java
@Component
public class ServiceAccessController {

    @Resource
    private UserRoleService userRoleService;

    public boolean canAccessService(String serviceName, String userId) {
        try {
            // 检查用户是否有权限访问特定服务
            List<String> userRoles = userRoleService.getUserRoles(userId);
            List<String> requiredRoles = getServiceRequiredRoles(serviceName);

            return userRoles.stream().anyMatch(requiredRoles::contains);
        } catch (Exception e) {
            log.error("Failed to check service access permission", e);
            return false;
        }
    }

    private List<String> getServiceRequiredRoles(String serviceName) {
        // 根据服务名称获取所需角色
        switch (serviceName) {
            case "user-service":
                return Arrays.asList("USER_MANAGER", "ADMIN");
            case "access-control-service":
                return Arrays.asList("ACCESS_CONTROL_MANAGER", "ADMIN");
            case "consume-service":
                return Arrays.asList("CONSUME_MANAGER", "ADMIN");
            default:
                return Collections.singletonList("ADMIN");
        }
    }
}
```

---

## 📊 监控与告警

### 1. 服务发现监控

#### 服务状态监控
```java
@Component
@Slf4j
public class ServiceDiscoveryMonitor {

    @Resource
    private NacosServiceManager nacosServiceManager;

    @Resource
    private AlertService alertService;

    @Scheduled(fixedRate = 60000)  // 每分钟检查一次
    public void monitorServiceStatus() {
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            List<String> serviceNames = namingService.getServicesOfServer(1, 100).getData();

            for (String serviceName : serviceNames) {
                monitorSingleService(serviceName, namingService);
            }
        } catch (Exception e) {
            log.error("Failed to monitor service status", e);
        }
    }

    private void monitorSingleService(String serviceName, NamingService namingService) {
        try {
            List<Instance> instances = namingService.selectInstances(
                serviceName, "IOE-DREAM", true);

            int healthyInstances = (int) instances.stream()
                .filter(Instance::isHealthy)
                .count();

            int totalInstances = instances.size();
            double healthRatio = totalInstances > 0 ? (double) healthyInstances / totalInstances : 0;

            // 健康实例比例低于50%时告警
            if (healthRatio < 0.5) {
                alertService.sendAlert(AlertLevel.WARNING,
                    String.format("Service %s health ratio is low: %.2f%%", serviceName, healthRatio * 100));
            }

            log.debug("Service {} status: {}/{} healthy ({:.2f}%)",
                serviceName, healthyInstances, totalInstances, healthRatio * 100);

        } catch (Exception e) {
            log.error("Failed to monitor service: {}", serviceName, e);
        }
    }
}
```

### 2. 性能指标收集

#### 服务发现指标
```java
@Component
public class ServiceDiscoveryMetrics {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger serviceRegistrationCount;
    private final AtomicInteger serviceDiscoveryCount;
    private final Timer discoveryLatencyTimer;

    public ServiceDiscoveryMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.serviceRegistrationCount = meterRegistry.gauge("service.registration.count", new AtomicInteger(0));
        this.serviceDiscoveryCount = meterRegistry.gauge("service.discovery.count", new AtomicInteger(0));
        this.discoveryLatencyTimer = Timer.builder("service.discovery.latency")
            .description("Service discovery latency")
            .register(meterRegistry);
    }

    public void recordServiceRegistration() {
        serviceRegistrationCount.incrementAndGet();
    }

    public void recordServiceDiscovery() {
        serviceDiscoveryCount.incrementAndGet();
    }

    public void recordDiscoveryLatency(Duration duration) {
        discoveryLatencyTimer.record(duration);
    }
}
```

---

## ⚠️ 最佳实践与注意事项

### ✅ 推荐实践

1. **服务注册设计**
   - 使用有意义的实例ID，便于识别和管理
   - 合理设置心跳间隔和超时时间
   - 为服务添加有意义的元数据

2. **负载均衡策略**
   - 根据业务特点选择合适的负载均衡算法
   - 考虑服务实例的处理能力和地理位置
   - 实现优雅的流量切换机制

3. **健康检查**
   - 实现多层次的健康检查（应用、数据库、缓存、业务）
   - 区分存活检查和就绪检查
   - 提供详细的健康状态信息

4. **监控告警**
   - 监控关键指标：服务可用性、响应时间、错误率
   - 设置合理的告警阈值
   - 建立完善的故障处理流程

### ❌ 避免的陷阱

1. **服务发现问题**
   - 不要硬编码服务地址
   - 避免缓存服务实例信息过久
   - 不要忽视服务健康状态

2. **负载均衡问题**
   - 避免所有服务使用相同的负载均衡策略
   - 不要忽视服务实例的实际负载情况
   - 避免频繁的负载均衡策略切换

3. **健康检查问题**
   - 健康检查逻辑不要太复杂
   - 避免健康检查本身成为性能瓶颈
   - 不要设置过短的超时时间

---

## 📊 评估标准

### 🎯 技能掌握评估

#### 理论知识 (30%)
- [ ] 服务发现原理和架构
- [ ] Nacos核心功能和配置
- [ ] 负载均衡算法和策略
- [ ] 健康检查机制和实现

#### 实践能力 (50%)
- [ ] 能够配置和部署Nacos集群
- [ ] 熟练实现服务注册和发现
- [ ] 能够实现自定义负载均衡策略
- [ ] 掌握健康检查的最佳实践

#### 问题解决 (20%)
- [ ] 服务发现故障排查
- [ ] 负载均衡性能优化
- [ ] 健康检查异常处理
- [ ] 服务可用性保障

### 📈 质量标准

- **服务可用性**: > 99.9%
- **发现延迟**: < 100ms (P95)
- **健康检查频率**: 30秒
- **监控覆盖度**: 100%

---

## 🔗 相关技能

- **前置技能**: microservices-architecture-specialist, spring-boot-jakarta-guardian
- **相关技能**: distributed-transaction-specialist, kubernetes-deployment-specialist
- **进阶技能**: system-optimization-specialist, intelligent-operations-expert

---

## 💡 持续学习方向

1. **服务网格**: Istio服务发现集成
2. **边缘计算**: 边缘服务发现
3. **混合云**: 跨云服务发现
4. **智能调度**: AI驱动的负载均衡

---

**⚠️ 重要提醒**: 服务发现是微服务架构的核心组件，需要根据IOE-DREAM项目的具体需求进行定制化配置和优化。确保遵循repowiki规范和项目的安全要求。