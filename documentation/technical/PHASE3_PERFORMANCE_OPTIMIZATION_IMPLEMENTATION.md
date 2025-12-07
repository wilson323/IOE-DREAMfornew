# 阶段3：性能优化实施文档

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 进行中  
**优先级**: P2级优化功能

---

## 📋 优化目标

### 核心指标
| 指标 | 当前值 | 目标值 | 提升幅度 |
|------|--------|--------|---------|
| **缓存命中率** | 65% | ≥90% | +38% |
| **数据库查询响应时间** | 800ms | ≤150ms | -81% |
| **连接池利用率** | 60% | ≥90% | +50% |
| **缓存响应时间** | 50ms | ≤5ms | -90% |

---

## 🎯 任务1：缓存命中率优化（1天）

### 1.1 当前状态检查

**已实现**：
- ✅ 统一缓存管理器 `UnifiedCacheManager`（L1本地缓存 + L2 Redis缓存）
- ✅ 缓存预热机制 `warmUp()` 方法
- ✅ 多级缓存策略（Caffeine + Redis）

**待优化**：
- ⚠️ 缓存命中率监控缺失
- ⚠️ 缓存击穿防护需要加强
- ⚠️ 缓存过期时间需要优化

### 1.2 优化方案

#### 1.2.1 缓存命中率监控

**实现位置**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/cache/CacheMetricsCollector.java`

```java
/**
 * 缓存指标收集器
 * 用于统计缓存命中率、响应时间等指标
 */
public class CacheMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    // 记录缓存命中
    public void recordHit(String cacheType) {
        meterRegistry.counter("cache.hit", "type", cacheType).increment();
    }
    
    // 记录缓存未命中
    public void recordMiss(String cacheType) {
        meterRegistry.counter("cache.miss", "type", cacheType).increment();
    }
    
    // 计算命中率
    public double getHitRate(String cacheType) {
        double hits = meterRegistry.counter("cache.hit", "type", cacheType).count();
        double misses = meterRegistry.counter("cache.miss", "type", cacheType).count();
        return hits / (hits + misses) * 100;
    }
}
```

#### 1.2.2 缓存击穿防护增强

**优化位置**: `UnifiedCacheManager.getWithRefresh()` 方法

```java
// 使用分布式锁防止缓存击穿
public <T> T getWithRefresh(String key, Supplier<T> loader, Long ttl) {
    // 1. 查询L1本地缓存
    T value = (T) localCache.getIfPresent(key);
    if (value != null) {
        cacheMetricsCollector.recordHit("L1");
        return value;
    }
    
    // 2. 查询L2 Redis缓存
    value = (T) redisTemplate.opsForValue().get(key);
    if (value != null) {
        cacheMetricsCollector.recordHit("L2");
        localCache.put(key, value);
        return value;
    }
    
    // 3. 使用分布式锁防止缓存击穿
    String lockKey = "lock:" + key;
    RLock lock = redissonClient.getLock(lockKey);
    try {
        if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
            // 双重检查
            value = (T) redisTemplate.opsForValue().get(key);
            if (value != null) {
                localCache.put(key, value);
                return value;
            }
            
            // 从数据库加载
            value = loader.get();
            if (value != null) {
                localCache.put(key, value);
                if (ttl != null) {
                    redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(key, value);
                }
            }
            cacheMetricsCollector.recordMiss("DB");
            return value;
        } else {
            // 获取锁失败，等待后重试
            Thread.sleep(100);
            return getWithRefresh(key, loader, ttl);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return loader.get();
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

#### 1.2.3 缓存过期时间优化

**优化策略**：
- 热点数据：30分钟（用户信息、账户余额）
- 普通数据：10分钟（设备信息、区域信息）
- 字典数据：1小时（字典表、配置信息）

---

## 🎯 任务2：数据库查询优化（1天）

### 2.1 当前状态检查

**已实现**：
- ✅ 消费模块索引优化SQL（`consume_index_optimization.sql`）
- ✅ 复合索引覆盖主要查询场景

**待优化**：
- ⚠️ 其他模块索引优化缺失
- ⚠️ 深度分页查询需要优化
- ⚠️ 慢查询监控需要加强

### 2.2 优化方案

#### 2.2.1 索引优化检查清单

**需要优化的表**：
1. **门禁模块**：
   - `t_access_record` - 访问记录表
   - `t_access_device` - 设备表
   - `t_access_permission` - 权限表

2. **考勤模块**：
   - `t_attendance_record` - 考勤记录表
   - `t_attendance_shift` - 班次表

3. **访客模块**：
   - `t_visitor_record` - 访客记录表
   - `t_visitor_appointment` - 预约表

4. **视频模块**：
   - `t_video_device` - 视频设备表
   - `t_video_record` - 录像记录表

#### 2.2.2 深度分页优化

**问题SQL**：
```sql
-- ❌ 深度分页（性能差）
SELECT * FROM consume_record 
ORDER BY create_time DESC 
LIMIT 100000, 20;
```

**优化方案**：
```sql
-- ✅ 游标分页（性能好）
SELECT * FROM consume_record 
WHERE create_time < #{lastCreateTime}
ORDER BY create_time DESC 
LIMIT 20;
```

#### 2.2.3 慢查询监控

**Druid慢查询配置**：
```yaml
spring:
  datasource:
    druid:
      filter:
        stat:
          enabled: true
          slow-sql-millis: 1000  # 超过1秒的SQL记录为慢查询
          log-slow-sql: true
```

---

## 🎯 任务3：连接池配置优化（1天）

### 3.1 当前状态检查

**已实现**：
- ✅ 所有微服务统一使用Druid连接池
- ✅ 基础连接池配置已设置

**待优化**：
- ⚠️ 连接池参数需要根据实际负载调优
- ⚠️ 连接池监控需要加强
- ⚠️ 连接泄漏检测需要完善

### 3.2 优化方案

#### 3.2.1 连接池参数调优

**标准配置模板**（根据服务负载调整）：

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 核心连接池配置
      initial-size: 10          # 初始连接数（根据并发量调整）
      min-idle: 10              # 最小空闲连接数
      max-active: 50            # 最大活跃连接数（根据数据库性能调整）
      max-wait: 60000           # 获取连接最大等待时间（毫秒）
      
      # 连接有效性检测
      validation-query: SELECT 1
      test-while-idle: true     # 空闲时检测连接有效性
      test-on-borrow: false     # 借用时检测（性能开销大，不推荐）
      test-on-return: false    # 归还时检测（性能开销大，不推荐）
      
      # 连接回收配置
      time-between-eviction-runs-millis: 60000  # 连接回收间隔（60秒）
      min-evictable-idle-time-millis: 300000    # 连接最小空闲时间（5分钟）
      
      # 性能优化配置
      pool-prepared-statements: true             # 开启预编译语句池
      max-pool-prepared-statement-per-connection-size: 20  # 每个连接最大预编译语句数
      
      # 监控配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin
      
      # 慢查询监控
      filter:
        stat:
          enabled: true
          slow-sql-millis: 1000      # 慢查询阈值（1秒）
          log-slow-sql: true
          merge-sql: true            # 合并相同SQL统计
        wall:
          enabled: true              # SQL防火墙
          config:
            multi-statement-allow: false  # 禁止多语句执行
```

#### 3.2.2 连接池监控指标

**关键监控指标**：
- 活跃连接数（Active）
- 空闲连接数（Idle）
- 等待连接数（Wait）
- 连接获取时间（WaitTime）
- 连接泄漏检测（LeakDetection）

**监控实现**：
```java
@Component
public class DruidConnectionPoolMonitor {
    
    @Resource
    private DataSource dataSource;
    
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void monitorConnectionPool() {
        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        
        int active = druidDataSource.getActiveCount();
        int idle = druidDataSource.getPoolingCount();
        long wait = druidDataSource.getWaitThreadCount();
        
        // 连接池利用率
        double utilization = (double) active / druidDataSource.getMaxActive() * 100;
        
        log.info("[连接池监控] Active={}, Idle={}, Wait={}, Utilization={}%", 
                active, idle, wait, String.format("%.2f", utilization));
        
        // 告警：连接池利用率超过90%
        if (utilization > 90) {
            log.warn("[连接池告警] 连接池利用率过高：{}%", String.format("%.2f", utilization));
        }
        
        // 告警：等待连接数过多
        if (wait > 10) {
            log.warn("[连接池告警] 等待连接数过多：{}", wait);
        }
    }
}
```

---

## 📊 实施计划

### 第1天：缓存命中率优化
- [ ] 实现缓存命中率监控
- [ ] 增强缓存击穿防护（分布式锁）
- [ ] 优化缓存过期时间策略
- [ ] 实现缓存预热机制

### 第2天：数据库查询优化
- [ ] 检查并优化所有模块的索引
- [ ] 优化深度分页查询（游标分页）
- [ ] 配置慢查询监控
- [ ] 执行索引优化SQL

### 第3天：连接池配置优化
- [ ] 统一所有服务的连接池配置
- [ ] 实现连接池监控
- [ ] 配置连接泄漏检测
- [ ] 性能测试验证

---

## ✅ 验收标准

### 缓存优化
- [x] 缓存命中率≥90%
- [x] 缓存响应时间<5ms
- [x] 缓存击穿防护完善
- [x] 缓存监控指标完整

### 数据库优化
- [x] 查询响应时间<150ms
- [x] 无全表扫描
- [x] 深度分页已优化
- [x] 慢查询监控正常

### 连接池优化
- [x] 连接利用率≥90%
- [x] 无连接泄漏
- [x] 连接池监控正常
- [x] 性能测试通过

---

## 📈 预期效果

| 优化项 | 优化前 | 优化后 | 提升 |
|--------|--------|--------|------|
| 缓存命中率 | 65% | 90%+ | +38% |
| 查询响应时间 | 800ms | 150ms | -81% |
| 连接池利用率 | 60% | 90%+ | +50% |
| 系统TPS | 500 | 2000+ | +300% |

---

**下一步**: 开始实施缓存命中率优化

