# GatewayServiceClient 完整实现说明

## 📋 功能概述

`GatewayServiceClient` 是统一的服务调用工具类，用于通过网关调用其他微服务，替代直接使用 FeignClient。

## ✅ 已实现功能

### 1. 基础服务调用
- ✅ 通过网关调用设备服务 (`callDeviceService`)
- ✅ 通过网关调用认证服务 (`callAuthService`)
- ✅ 统一的错误处理和日志记录

### 2. 认证Token传递 ✅
- ✅ 从当前请求上下文获取认证token
- ✅ 支持 `Authorization` 请求头（Bearer格式）
- ✅ 支持 `X-Access-Token` 请求头
- ✅ 自动添加Bearer前缀（如果需要）
- ✅ 完善的日志记录

### 3. 配置管理
- ✅ 网关URL配置（`ioedream.gateway.url`）
- ✅ RestTemplate超时配置
- ✅ 连接和读取超时设置

## 🔧 技术实现

### Token获取逻辑

```java
private String getAuthTokenFromRequest() {
    // 1. 优先从Authorization头获取
    String authorization = SmartRequestUtil.getHeader("Authorization");
    if (authorization != null && !authorization.isEmpty()) {
        // 如果已经是Bearer格式，直接返回；否则添加Bearer前缀
        if (authorization.startsWith("Bearer ") || authorization.startsWith("bearer ")) {
            return authorization;
        } else {
            return "Bearer " + authorization;
        }
    }

    // 2. 从X-Access-Token头获取
    String accessToken = SmartRequestUtil.getHeader("X-Access-Token");
    if (accessToken != null && !accessToken.isEmpty()) {
        return "Bearer " + accessToken;
    }

    return null;
}
```

### 使用示例

```java
@Autowired
private GatewayServiceClient gatewayServiceClient;

// 调用设备服务（自动传递认证token）
ResponseDTO<DeviceInfoVO> result = gatewayServiceClient.callDeviceService(
    "/info/" + deviceId, 
    HttpMethod.GET, 
    null, 
    DeviceInfoVO.class
);

// 调用认证服务（自动传递认证token）
ResponseDTO<UserInfoVO> result = gatewayServiceClient.callAuthService(
    "/userinfo", 
    HttpMethod.GET, 
    null, 
    UserInfoVO.class
);
```

## 📝 配置说明

### application.yml

```yaml
ioedream:
  gateway:
    url: http://localhost:8080  # 网关地址
```

### RestTemplate配置

```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 连接超时5秒
        factory.setReadTimeout(30000);    // 读取超时30秒
        return factory;
    }
}
```

## ⚠️ 注意事项

### 1. Token传递
- Token会自动从当前请求上下文获取
- 如果未找到token，会记录警告日志，但不会阻止请求
- 支持Bearer格式和普通token格式

### 2. 错误处理
- 所有异常都会被捕获并记录日志
- 返回统一的错误响应格式
- 不会抛出未处理的异常

### 3. 性能考虑
- 使用RestTemplate连接池
- 配置了合理的超时时间
- 通过网关调用会增加一次转发，但带来统一治理的好处

## 🔄 与FeignClient对比

| 特性 | FeignClient | GatewayServiceClient |
|------|-------------|---------------------|
| 服务治理 | 分散 | 统一（通过网关） |
| 限流熔断 | 需要单独配置 | 网关统一管理 |
| 监控追踪 | 困难 | 网关统一监控 |
| Token传递 | 需要手动处理 | 自动传递 |
| 依赖关系 | 直接依赖 | 通过网关解耦 |

## 📊 优势

1. **统一服务治理**: 所有服务调用通过网关，统一限流、熔断、监控
2. **自动Token传递**: 自动从请求上下文获取并传递认证token
3. **更好的可维护性**: 集中管理服务调用逻辑
4. **避免循环依赖**: 通过网关解耦服务间直接依赖
5. **统一错误处理**: 统一的错误处理和日志记录

## 🎯 后续优化建议

1. **重试机制**: 可以添加自动重试功能
2. **缓存支持**: 对于查询类请求可以添加缓存
3. **性能监控**: 添加调用耗时统计
4. **熔断降级**: 与网关的熔断机制配合使用

---

**实现完成**: 2025-01-30  
**维护团队**: IOE-DREAM Team

