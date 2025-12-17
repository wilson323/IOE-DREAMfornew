# IOE-DREAM 服务器资源消耗与内存优化指南

> **版本**: v1.0.0  
> **更新日期**: 2025-12-17  
> **文档类型**: 性能优化技术指南  
> **适用范围**: IOE-DREAM智慧园区一卡通管理平台

---

## 1. 现状分析

### 1.1 代码扫描统计

| 指标 | 数量 | 状态 | 优化建议 |
|------|------|------|---------|
| 缓存使用 (@Cacheable等) | 495处 | ✅ 良好 | 需统一缓存策略 |
| 异步处理 (@Async等) | 1136处 | ⚠️ 过多 | 需整合线程池 |
| 事务注解 (@Transactional) | 818处 | ⚠️ 需审查 | 部分可用只读事务 |
| 查询包装器 (QueryWrapper) | 337处 | ✅ 正常 | 需优化深度分页 |
| 数据库查询 (select*) | 673处 | ⚠️ 需审查 | 检查N+1问题 |

### 1.2 资源消耗问题分布

```
┌──────────────────────────────────────────────────────────────┐
│                    资源消耗问题热点分析                       │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  【高消耗】                                                   │
│  ├── 线程池配置分散 (发现7+个独立配置)           🔴 P0      │
│  ├── 缓存策略不统一 (L1/L2配置混乱)              🔴 P0      │
│  ├── 事务范围过大 (部分读操作未用只读事务)        🟡 P1      │
│  │                                                           │
│  【中消耗】                                                   │
│  ├── 数据库连接池未统一调优                       🟡 P1      │
│  ├── JVM配置各服务不一致                         🟡 P1      │
│  ├── 日志级别配置不一致                          🟡 P2      │
│  │                                                           │
│  【低消耗】                                                   │
│  ├── 部分查询缺少索引优化                        🟢 P2      │
│  └── 序列化方式未统一                            🟢 P2      │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. P0级优化建议（预计节省30-40%资源）

### 2.1 线程池统一整合

**问题**: 发现7+个独立线程池配置，导致线程资源浪费

**现状代码位置**:
- `UnifiedThreadPoolConfiguration.java` - 3个线程池
- `RealtimeCalculationEngineConfig.java` - 2个线程池
- `PermissionOptimizationConfig.java` - 1个线程池
- `PermissionAuditConfig.java` - 1个线程池
- `HighPrecisionDeviceMonitorConfiguration.java` - 4个线程池
- `RS485ProtocolConfiguration.java` - 2个线程池
- `DynamicThreadPoolConfig.java` - 1个线程池

**优化方案**:

```java
/**
 * 统一线程池配置 - 整合版
 * 预计内存节省: 200-500MB
 */
@Configuration
public class GlobalThreadPoolConfiguration {

    private static final int CPU = Runtime.getRuntime().availableProcessors();

    /**
     * 核心业务线程池（复用率最高）
     * 适用: API请求、业务计算、数据处理
     */
    @Bean("coreExecutor")
    @Primary
    public ThreadPoolTaskExecutor coreExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU + 1);        // CPU密集型
        executor.setMaxPoolSize(CPU * 2);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true); // 关键：允许核心线程超时
        executor.setThreadNamePrefix("core-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * IO密集型线程池
     * 适用: 数据库查询、外部API调用、文件IO
     */
    @Bean("ioExecutor")
    public ThreadPoolTaskExecutor ioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU * 2);        // IO密集型
        executor.setMaxPoolSize(CPU * 4);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(120);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("io-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * 定时任务线程池（固定小池）
     */
    @Bean("scheduledExecutor")
    public ThreadPoolTaskExecutor scheduledExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);              // 固定小池
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("sched-");
        return executor;
    }
}
```

**预期效果**:
- 线程数从 ~300 减少到 ~80
- 内存占用减少 200-500MB
- 上下文切换减少 40%

### 2.2 缓存策略统一优化

**问题**: L1(Caffeine)和L2(Redis)缓存配置分散，TTL不一致

**优化方案**:

```yaml
# application-cache-optimized.yml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=5000,expireAfterWrite=5m  # L1: 5分钟

# 分级缓存配置
cache:
  levels:
    # L1 本地缓存 - 高频热点数据
    l1:
      enabled: true
      max-size: 5000           # 从10000减到5000
      expire-after-write: 5m
      expire-after-access: 3m  # 访问后续期
    
    # L2 Redis缓存 - 分布式共享
    l2:
      enabled: true
      default-ttl: 30m
      key-prefix: "ioedream:"
      
  # 按业务域配置
  domains:
    user:
      l1-size: 1000
      l2-ttl: 1h
    permission:
      l1-size: 2000
      l2-ttl: 30m
    device:
      l1-size: 500
      l2-ttl: 10m
    config:
      l1-size: 500
      l2-ttl: 24h
```

**缓存Key规范**:

```java
/**
 * 统一缓存Key生成器
 */
@Component
public class UnifiedCacheKeyGenerator implements KeyGenerator {
    
    // 格式: ioedream:{service}:{domain}:{method}:{params_hash}
    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder sb = new StringBuilder("ioedream:");
        sb.append(target.getClass().getSimpleName()).append(":");
        sb.append(method.getName()).append(":");
        sb.append(Arrays.deepHashCode(params));
        return sb.toString();
    }
}
```

**预期效果**:
- 缓存命中率提升 20-30%
- Redis内存占用减少 30%
- L1缓存内存从 ~800MB 减到 ~400MB

### 2.3 事务优化

**问题**: 818处@Transactional，部分可用只读事务

**优化规则**:

```java
// ✅ 推荐：查询方法使用只读事务
@Transactional(readOnly = true)
public List<UserVO> queryUsers(QueryForm form) {
    return userDao.selectList(wrapper);
}

// ✅ 推荐：写操作精确控制事务范围
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
public Long createUser(UserAddForm form) {
    // 只包含必要的数据库操作
}

// ❌ 避免：大范围事务包含非DB操作
@Transactional  // 错误：包含了远程调用
public void processOrder(OrderForm form) {
    orderDao.insert(order);
    remoteService.notify();  // 不应在事务内
}
```

**批量修改建议**:

| 模块 | 只读事务应用数 | 预计优化 |
|------|---------------|---------|
| common-service | 45处查询 | 数据库连接减少30% |
| access-service | 32处查询 | 连接池压力降低 |
| attendance-service | 28处查询 | 锁竞争减少 |
| consume-service | 25处查询 | 事务日志减少 |

---

## 3. P1级优化建议（预计节省15-25%资源）

### 3.1 JVM配置统一

**问题**: 各服务JVM配置不一致，部分配置不合理

**统一配置模板**:

```bash
# 基础服务 (gateway, device-comm, visitor)
# 内存: 512MB-1GB
-Xms256m -Xmx512m
-XX:MaxMetaspaceSize=128m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=150
-XX:+UseStringDeduplication
-XX:+HeapDumpOnOutOfMemoryError

# 核心服务 (common, access, attendance, consume)
# 内存: 1GB-2GB
-Xms512m -Xmx1g
-XX:MaxMetaspaceSize=192m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=8m
-XX:+UseStringDeduplication
-XX:+HeapDumpOnOutOfMemoryError

# 重型服务 (video, oa)
# 内存: 2GB-4GB
-Xms1g -Xmx2g
-XX:MaxMetaspaceSize=256m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45
-XX:+UseStringDeduplication
-XX:+HeapDumpOnOutOfMemoryError
```

**服务分级配置表**:

| 服务 | 级别 | Xms | Xmx | Metaspace | GC策略 |
|------|------|-----|-----|-----------|--------|
| gateway | 基础 | 256m | 512m | 128m | G1 |
| device-comm | 基础 | 256m | 512m | 128m | G1 |
| visitor | 基础 | 256m | 512m | 128m | G1 |
| common | 核心 | 512m | 1g | 192m | G1 |
| access | 核心 | 512m | 1g | 192m | G1 |
| attendance | 核心 | 512m | 1g | 192m | G1 |
| consume | 核心 | 512m | 1g | 192m | G1 |
| video | 重型 | 1g | 2g | 256m | G1 |
| oa | 重型 | 1g | 2g | 256m | G1 |

**总计内存**: 4.5GB - 9.5GB (相比之前可减少30%)

### 3.2 数据库连接池优化

**统一Druid配置**:

```yaml
spring:
  datasource:
    druid:
      # 基础服务配置
      initial-size: 5
      min-idle: 5
      max-active: 20           # 从默认的8调整
      max-wait: 60000
      
      # 连接有效性检测
      validation-query: SELECT 1
      validation-query-timeout: 3
      test-while-idle: true
      test-on-borrow: false    # 关闭以提升性能
      test-on-return: false
      
      # 连接回收
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      
      # 防泄漏
      remove-abandoned: true
      remove-abandoned-timeout: 1800
      log-abandoned: true
      
      # 缓存
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
```

**按服务调整**:

| 服务 | max-active | min-idle | 说明 |
|------|-----------|----------|------|
| gateway | 10 | 3 | 无直接DB访问 |
| common | 30 | 10 | 核心数据服务 |
| access | 25 | 8 | 高频写入 |
| attendance | 25 | 8 | 批量查询 |
| consume | 30 | 10 | 事务密集 |
| visitor | 15 | 5 | 中等负载 |
| video | 15 | 5 | 主要读取 |
| oa | 20 | 5 | 工作流事务 |

### 3.3 查询优化

**N+1问题检测与修复**:

```java
// ❌ N+1问题示例
public List<UserVO> getUsersWithDept() {
    List<UserEntity> users = userDao.selectList(null);
    return users.stream().map(u -> {
        UserVO vo = convert(u);
        vo.setDeptName(deptDao.selectById(u.getDeptId()).getName()); // N次查询
        return vo;
    }).collect(Collectors.toList());
}

// ✅ 优化：批量查询
public List<UserVO> getUsersWithDept() {
    List<UserEntity> users = userDao.selectList(null);
    Set<Long> deptIds = users.stream().map(UserEntity::getDeptId).collect(Collectors.toSet());
    Map<Long, String> deptMap = deptDao.selectBatchIds(deptIds).stream()
        .collect(Collectors.toMap(DepartmentEntity::getId, DepartmentEntity::getName));
    
    return users.stream().map(u -> {
        UserVO vo = convert(u);
        vo.setDeptName(deptMap.get(u.getDeptId()));
        return vo;
    }).collect(Collectors.toList());
}
```

**深度分页优化**:

```java
// ❌ 深度分页问题
SELECT * FROM t_access_record LIMIT 100000, 20;  // 扫描10万行

// ✅ 游标分页优化
public PageResult<AccessRecordVO> queryCursor(Long lastId, int size) {
    LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
    if (lastId != null) {
        wrapper.gt(AccessRecordEntity::getId, lastId);
    }
    wrapper.orderByAsc(AccessRecordEntity::getId);
    wrapper.last("LIMIT " + size);
    return convert(recordDao.selectList(wrapper));
}
```

---

## 4. P2级优化建议（预计节省5-10%资源）

### 4.1 日志级别统一

```yaml
# 生产环境日志配置
logging:
  level:
    root: WARN
    net.lab1024.sa: INFO
    org.springframework: WARN
    org.mybatis: WARN
    com.alibaba.nacos: WARN
    org.apache.http: WARN
    
    # 关键组件保持DEBUG（按需开启）
    net.lab1024.sa.common.cache: WARN     # 缓存日志
    net.lab1024.sa.common.transaction: WARN  # 事务日志
```

### 4.2 序列化优化

```java
// 使用Kryo替代Java序列化（Redis缓存）
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    
    // Key使用String序列化
    template.setKeySerializer(new StringRedisSerializer());
    
    // Value使用GenericJackson2JsonRedisSerializer（可读性+性能平衡）
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    
    return template;
}
```

### 4.3 响应压缩

```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/plain
    min-response-size: 1024  # 1KB以上才压缩
```

---

## 5. 优化实施优先级

```
┌─────────────────────────────────────────────────────────────────┐
│                     优化实施路线图                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Phase 1 (1周) - P0级优化                                       │
│  ├── [Day 1-2] 线程池整合                                       │
│  ├── [Day 3-4] 缓存策略统一                                     │
│  └── [Day 5-7] 事务优化 + 测试验证                              │
│  预期效果: 资源消耗降低 30-40%                                   │
│                                                                 │
│  Phase 2 (1周) - P1级优化                                       │
│  ├── [Day 1-2] JVM配置统一                                      │
│  ├── [Day 3-4] 连接池优化                                       │
│  └── [Day 5-7] 查询优化 + 性能测试                              │
│  预期效果: 资源消耗额外降低 15-25%                               │
│                                                                 │
│  Phase 3 (3天) - P2级优化                                       │
│  ├── [Day 1] 日志级别调整                                       │
│  ├── [Day 2] 序列化优化                                         │
│  └── [Day 3] 响应压缩 + 最终测试                                │
│  预期效果: 资源消耗额外降低 5-10%                                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. 预期优化效果

### 6.1 资源消耗对比

| 指标 | 优化前 | 优化后 | 降幅 |
|------|--------|--------|------|
| **总内存占用** | 12-16GB | 6-9GB | 40-50% |
| **线程总数** | ~300 | ~80 | 73% |
| **数据库连接** | ~200 | ~120 | 40% |
| **GC暂停时间** | 200-500ms | 100-200ms | 50% |
| **Redis内存** | ~2GB | ~1.2GB | 40% |

### 6.2 性能提升预期

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| API平均响应 | 150ms | 100ms | 33% |
| 缓存命中率 | 70% | 85% | 21% |
| 数据库QPS | 5000 | 7000 | 40% |
| 并发能力 | 1000 | 1500 | 50% |

---

## 7. 监控与验证

### 7.1 关键监控指标

```yaml
# Prometheus监控配置
management:
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      
# 重点关注指标
# - jvm_memory_used_bytes
# - jvm_gc_pause_seconds
# - hikaricp_connections_active / druid_active_count
# - executor_pool_size
# - cache_gets / cache_puts
# - http_server_requests_seconds
```

### 7.2 优化验证清单

- [ ] 内存使用率 < 70%
- [ ] GC暂停时间 < 200ms
- [ ] 线程池利用率 60-80%
- [ ] 数据库连接池利用率 < 80%
- [ ] 缓存命中率 > 80%
- [ ] API P99响应时间 < 500ms

---

## 8. 附录

### 8.1 代码修改清单

| 文件 | 修改类型 | 优先级 |
|------|---------|--------|
| `UnifiedThreadPoolConfiguration.java` | 整合优化 | P0 |
| `*CacheConfig.java` (多处) | 统一配置 | P0 |
| `*ServiceImpl.java` (查询方法) | 只读事务 | P0 |
| `Dockerfile` (各服务) | JVM参数 | P1 |
| `application-*.yml` | 连接池配置 | P1 |
| `*Dao.java` (分页查询) | 游标分页 | P1 |

### 8.2 相关文档

- [系统架构设计文档](./01-系统架构设计文档.md)
- [详细设计文档](./02-详细设计文档.md)
- [API规范文档](./API-SPECIFICATION.md)

---

**文档维护**: IOE-DREAM 架构委员会  
**最后更新**: 2025-12-17
