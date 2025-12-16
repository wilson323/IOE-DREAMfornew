# IOE-DREAM 统一自动装配开发指南

> **版本**: 1.0.0  
> **更新日期**: 2025-12-14  
> **适用范围**: 所有微服务模块  
> **维护团队**: IOE-DREAM 架构组

---

## 📋 概述

本指南详细说明 IOE-DREAM 微服务架构中的**统一自动装配机制**，帮助开发者理解如何正确使用公共 Bean，避免重复定义和配置冲突。

### 核心原则

```
一处定义，全局可用
统一配置，按需覆盖
自动装配，零配置启动
```

---

## 🏗️ 架构设计

### 模块结构

```
microservices-common/
├── src/main/java/net/lab1024/sa/common/
│   ├── cache/
│   │   └── SpringCacheServiceImpl.java      # 统一CacheService实现
│   ├── config/
│   │   └── CommonBeanAutoConfiguration.java # 统一Bean自动装配配置
│   └── gateway/
│       └── GatewayServiceClient.java        # 网关服务客户端
└── src/main/resources/META-INF/
    ├── spring.factories                      # Spring Boot 2.x 兼容
    └── spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports  # Spring Boot 3.x
```

### 统一提供的 Bean

| Bean 名称 | 类型 | 说明 |
|-----------|------|------|
| `cacheService` | `CacheService` | 统一缓存服务，基于 Spring Cache + Redis |
| `gatewayServiceClient` | `GatewayServiceClient` | 网关服务调用客户端 |
| `restTemplate` | `RestTemplate` | HTTP 客户端 |

---

## 🚀 快速开始

### 1. 添加依赖

在微服务的 `pom.xml` 中添加 `microservices-common` 依赖：

```xml
<dependency>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>microservices-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 直接注入使用

无需任何额外配置，直接在 Service 或 Controller 中注入：

```java
@Service
public class MyBusinessService {

    @Resource
    private CacheService cacheService;
    
    @Resource
    private GatewayServiceClient gatewayServiceClient;
    
    @Resource
    private RestTemplate restTemplate;
    
    public void doSomething() {
        // 使用缓存服务
        cacheService.put("key", "value", 3600);
        String value = cacheService.get("key", String.class);
        
        // 调用其他微服务
        ResponseDTO<?> response = gatewayServiceClient.callService(
            "ioedream-common-service", 
            "/api/user/info", 
            HttpMethod.GET, 
            null
        );
    }
}
```

---

## 📖 详细说明

### CacheService 缓存服务

#### 接口定义

```java
public interface CacheService {
    // 基础操作
    <T> T get(String key, Class<T> clazz);
    void put(String key, Object value);
    void put(String key, Object value, long ttlSeconds);
    void evict(String key);
    void clear();
    
    // 命名空间操作
    <T> T get(CacheNamespace namespace, String key, Class<T> clazz);
    void put(CacheNamespace namespace, String key, Object value);
    void put(CacheNamespace namespace, String key, Object value, long ttlSeconds);
    void evict(CacheNamespace namespace, String key);
    void clearNamespace(CacheNamespace namespace);
    
    // 批量操作
    <T> Map<String, T> multiGet(Collection<String> keys, Class<T> clazz);
    void multiPut(Map<String, Object> entries);
    void multiEvict(Collection<String> keys);
}
```

#### 使用示例

```java
@Service
public class UserService {

    @Resource
    private CacheService cacheService;
    
    // 简单缓存
    public User getUserById(Long userId) {
        String cacheKey = "user:" + userId;
        
        // 先查缓存
        User user = cacheService.get(cacheKey, User.class);
        if (user != null) {
            return user;
        }
        
        // 缓存未命中，查数据库
        user = userDao.selectById(userId);
        if (user != null) {
            // 缓存1小时
            cacheService.put(cacheKey, user, 3600);
        }
        return user;
    }
    
    // 使用命名空间
    public void cacheWithNamespace() {
        cacheService.put(CacheNamespace.USER, "profile:123", userProfile, 1800);
        UserProfile profile = cacheService.get(CacheNamespace.USER, "profile:123", UserProfile.class);
    }
    
    // 批量操作
    public Map<String, User> batchGetUsers(List<Long> userIds) {
        List<String> keys = userIds.stream()
            .map(id -> "user:" + id)
            .collect(Collectors.toList());
        return cacheService.multiGet(keys, User.class);
    }
}
```

#### 推荐：使用 Spring Cache 注解

对于简单场景，推荐使用 Spring Cache 注解，更加简洁：

```java
@Service
public class UserService {

    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        return userDao.selectById(userId);
    }
    
    @CacheEvict(value = "users", key = "#user.id")
    public void updateUser(User user) {
        userDao.updateById(user);
    }
    
    @CachePut(value = "users", key = "#user.id")
    public User saveUser(User user) {
        userDao.insert(user);
        return user;
    }
}
```

### GatewayServiceClient 网关客户端

#### 核心方法

```java
public class GatewayServiceClient {
    
    // 通用服务调用
    public <T> ResponseDTO<T> callService(
        String serviceName,      // 目标服务名
        String path,             // API路径
        HttpMethod method,       // HTTP方法
        Object requestBody,      // 请求体（可选）
        Class<T> responseType    // 响应类型
    );
    
    // GET 请求
    public <T> ResponseDTO<T> get(String serviceName, String path, Class<T> responseType);
    
    // POST 请求
    public <T> ResponseDTO<T> post(String serviceName, String path, Object body, Class<T> responseType);
    
    // 带参数的 GET 请求
    public <T> ResponseDTO<T> getWithParams(
        String serviceName, 
        String path, 
        Map<String, Object> params, 
        Class<T> responseType
    );
}
```

#### 使用示例

```java
@Service
public class OrderService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;
    
    // 调用用户服务获取用户信息
    public User getUserInfo(Long userId) {
        ResponseDTO<User> response = gatewayServiceClient.get(
            "ioedream-common-service",
            "/api/user/" + userId,
            User.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        throw new BusinessException("获取用户信息失败: " + response.getMessage());
    }
    
    // 调用通知服务发送消息
    public void sendNotification(NotificationRequest request) {
        ResponseDTO<Void> response = gatewayServiceClient.post(
            "ioedream-common-service",
            "/api/notification/send",
            request,
            Void.class
        );
        
        if (!response.isSuccess()) {
            log.error("发送通知失败: {}", response.getMessage());
        }
    }
}
```

### RestTemplate HTTP 客户端

统一配置的 RestTemplate，可直接用于外部 HTTP 调用：

```java
@Service
public class ExternalApiService {

    @Resource
    private RestTemplate restTemplate;
    
    public WeatherInfo getWeather(String city) {
        String url = "https://api.weather.com/v1/current?city=" + city;
        return restTemplate.getForObject(url, WeatherInfo.class);
    }
}
```

---

## ⚙️ 配置说明

### 默认配置

统一自动装配使用以下默认配置：

```yaml
# 网关URL配置
spring:
  cloud:
    gateway:
      url: ${GATEWAY_URL:http://localhost:8080}

# 缓存配置（由 LightCacheConfiguration 提供）
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 默认1小时
```

### 自定义配置

如需自定义，在服务的 `application.yml` 中覆盖：

```yaml
spring:
  cloud:
    gateway:
      url: http://gateway.ioedream.local:8080
```

---

## 🔧 高级用法

### 自定义 Bean 覆盖

如果某个服务需要自定义实现，可以定义自己的 Bean，自动装配会跳过：

```java
@Configuration
public class CustomCacheConfiguration {

    /**
     * 自定义 CacheService 实现
     * 由于使用了 @Bean，会覆盖自动装配的默认实现
     */
    @Bean
    public CacheService cacheService(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        return new CustomCacheServiceImpl(cacheManager, redisTemplate);
    }
}
```

### 条件装配原理

`CommonBeanAutoConfiguration` 使用 `@ConditionalOnMissingBean` 注解：

```java
@AutoConfiguration
@Slf4j
public class CommonBeanAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean  // 如果容器中已存在，则不创建
    public CacheService cacheService(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
        log.info("[CacheService] 初始化统一缓存服务");
        return new SpringCacheServiceImpl(cacheManager, redisTemplate);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public GatewayServiceClient gatewayServiceClient(
            RestTemplate restTemplate, 
            ObjectMapper objectMapper,
            @Value("${spring.cloud.gateway.url:http://localhost:8080}") String gatewayUrl) {
        log.info("[GatewayServiceClient] 初始化网关客户端，URL: {}", gatewayUrl);
        return new GatewayServiceClient(restTemplate, objectMapper, gatewayUrl);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        log.info("[RestTemplate] 初始化HTTP客户端");
        return new RestTemplate();
    }
}
```

---

## ✅ 最佳实践

### DO ✓

1. **直接依赖 microservices-common**
   ```xml
   <dependency>
       <groupId>net.lab1024.sa</groupId>
       <artifactId>microservices-common</artifactId>
   </dependency>
   ```

2. **使用 @Resource 注入公共 Bean**
   ```java
   @Resource
   private CacheService cacheService;
   ```

3. **优先使用 Spring Cache 注解**
   ```java
   @Cacheable(value = "users", key = "#id")
   public User getUser(Long id) { ... }
   ```

4. **通过配置文件覆盖默认值**
   ```yaml
   spring.cloud.gateway.url: http://custom-gateway:8080
   ```

### DON'T ✗

1. **❌ 不要在各服务中重复定义 GatewayServiceClientConfiguration**
   ```java
   // 错误！已删除，不要再创建
   @Configuration
   public class GatewayServiceClientConfiguration {
       @Bean
       public GatewayServiceClient gatewayServiceClient() { ... }
   }
   ```

2. **❌ 不要重复定义 RestTemplate Bean**
   ```java
   // 错误！会导致 Bean 冲突
   @Bean
   public RestTemplate restTemplate() { ... }
   ```

3. **❌ 不要在多个地方定义 CacheService**
   ```java
   // 错误！统一使用自动装配的实现
   @Bean
   public CacheService cacheService() { ... }
   ```

4. **❌ 不要硬编码网关 URL**
   ```java
   // 错误！应使用配置
   private String gatewayUrl = "http://localhost:8080";
   ```

---

## 🔍 故障排查

### 常见问题

#### 1. Bean 找不到

**错误信息**:
```
No qualifying bean of type 'CacheService' available
```

**解决方案**:
- 确认 `pom.xml` 中已添加 `microservices-common` 依赖
- 确认 `@SpringBootApplication` 的 `scanBasePackages` 包含 `net.lab1024.sa.common`

#### 2. Bean 冲突

**错误信息**:
```
expected single matching bean but found 2: cacheService, customCacheService
```

**解决方案**:
- 如果需要自定义实现，确保 Bean 名称为 `cacheService`
- 或者使用 `@Primary` 注解标记首选 Bean

#### 3. 自动装配未生效

**检查步骤**:
1. 确认 `spring.factories` 或 `AutoConfiguration.imports` 文件存在
2. 确认文件内容正确：
   ```
   # spring.factories
   org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
   net.lab1024.sa.common.config.CommonBeanAutoConfiguration
   
   # AutoConfiguration.imports
   net.lab1024.sa.common.config.CommonBeanAutoConfiguration
   ```

#### 4. 网关调用失败

**检查步骤**:
1. 确认网关服务已启动
2. 确认 `spring.cloud.gateway.url` 配置正确
3. 检查网络连通性

---

## 📊 依赖关系图

```
┌─────────────────────────────────────────────────────────────┐
│                    业务微服务                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ access-svc  │  │ video-svc   │  │ consume-svc │  ...    │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘         │
│         │                │                │                 │
│         └────────────────┼────────────────┘                 │
│                          │                                  │
│                          ▼                                  │
│              ┌───────────────────────┐                      │
│              │  microservices-common │ ◄── 统一自动装配入口  │
│              │  ├─ CacheService      │                      │
│              │  ├─ GatewayClient     │                      │
│              │  └─ RestTemplate      │                      │
│              └───────────┬───────────┘                      │
│                          │                                  │
│         ┌────────────────┼────────────────┐                 │
│         ▼                ▼                ▼                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ common-core │  │ common-biz  │  │common-monitor│         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 变更历史

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|----------|------|
| 1.0.0 | 2025-12-14 | 初始版本，统一自动装配架构 | IOE-DREAM Team |

---

## 📚 相关文档

- [Spring Cache 使用指南](./SPRING_CACHE_USAGE_GUIDE.md)
- [微服务架构设计](../architecture/MICROSERVICES_ARCHITECTURE.md)
- [Bean 注册规范](./BEAN_REGISTRATION_STANDARDS.md)
- [CLAUDE.md 开发规范](../../CLAUDE.md)
