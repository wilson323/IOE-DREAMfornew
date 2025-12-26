# Redisson配置验证指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 待验证

---

## 📋 配置说明

### 配置位置
- **配置类**: `microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/config/RedissonConfig.java`
- **配置文件**: Nacos配置中心的 `spring.data.redis.*` 配置

---

## 🔧 配置步骤

### 1. 在Nacos配置中心添加Redis配置

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      timeout: 3000
      lettuce:
        pool:
          max-active: 20
          max-wait: -1
          max-idle: 10
          min-idle: 0
```

### 2. 验证Redisson配置类

Redisson配置类已创建在：
- `microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/config/RedissonConfig.java`

**配置说明**:
- 单节点模式
- 连接池大小: 10
- 最小空闲连接数: 5
- 连接超时: 3000ms
- 响应超时: 3000ms
- 重试次数: 3

---

## 🔍 验证步骤

### 1. 检查Redis连接

```bash
# 测试Redis连接
redis-cli -h localhost -p 6379 ping
# 预期输出: PONG
```

### 2. 启动服务并检查日志

启动服务后，查看日志中是否有：
```
Redisson客户端配置成功，地址：redis://localhost:6379，数据库：0
```

如果看到以下日志，说明配置失败：
```
Redisson客户端配置失败
```

### 3. 使用测试接口验证

创建测试接口验证Redisson是否正常工作：

```java
@RestController
@RequestMapping("/api/v1/test")
public class RedissonTestController {
    
    @Resource
    private RedissonClient redissonClient;
    
    @GetMapping("/redisson/test")
    public ResponseDTO<String> testRedisson() {
        try {
            RLock lock = redissonClient.getLock("test:lock");
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (locked) {
                try {
                    return ResponseDTO.ok("Redisson工作正常");
                } finally {
                    lock.unlock();
                }
            } else {
                return ResponseDTO.error("获取锁失败");
            }
        } catch (Exception e) {
            return ResponseDTO.error("Redisson测试失败: " + e.getMessage());
        }
    }
}
```

### 4. 检查缓存击穿防护

验证 `UnifiedCacheManager` 的缓存击穿防护功能：

```java
// 测试缓存击穿防护
String key = "test:cache:key";
String value = cacheManager.getWithRefresh(key, () -> "test-value", 3600L);
// 如果Redisson配置正常，应该使用分布式锁
// 如果Redisson未配置，会降级为直接加载数据
```

---

## ⚠️ 常见问题

### 问题1: Redisson客户端配置失败

**错误日志**:
```
Redisson客户端配置失败
java.net.ConnectException: Connection refused
```

**解决方案**:
1. 检查Redis服务是否启动
2. 检查Redis连接配置（host、port、password）
3. 检查防火墙设置

### 问题2: 缓存击穿防护不可用

**日志**:
```
RedissonClient未配置，缓存击穿防护功能将不可用
```

**解决方案**:
1. 确认 `RedissonConfig` 类已加载
2. 检查Redis连接配置
3. 确认 `RedissonClient` Bean已注册

### 问题3: 连接超时

**错误日志**:
```
Connection timeout
```

**解决方案**:
1. 增加连接超时时间
2. 检查网络连接
3. 检查Redis服务状态

---

## 📊 性能监控

### 1. 监控Redisson连接池

```java
@Resource
private RedissonClient redissonClient;

// 获取连接池统计
Config config = redissonClient.getConfig();
// 查看连接池状态
```

### 2. 监控分布式锁使用情况

在 `UnifiedCacheManager` 中已集成缓存指标收集器，可以查看：
- 缓存命中率
- 缓存响应时间
- 缓存操作计数

---

## ✅ 验证清单

- [ ] Redis服务已启动
- [ ] Redis连接配置正确
- [ ] Redisson配置类已加载
- [ ] RedissonClient Bean已注册
- [ ] 缓存击穿防护功能正常
- [ ] 分布式锁功能正常
- [ ] 性能监控正常

---

**验证完成后，请更新验证状态**

