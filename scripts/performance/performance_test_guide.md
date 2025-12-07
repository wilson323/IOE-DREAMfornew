# 性能测试验证指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 待执行

---

## 📋 测试目标

### 核心指标
| 指标 | 优化前 | 目标值 | 验证方法 |
|------|--------|--------|---------|
| **缓存命中率** | 65% | ≥90% | 查看缓存指标 |
| **查询响应时间** | 800ms | ≤150ms | 接口响应时间 |
| **连接池利用率** | 60% | ≥90% | Druid监控 |
| **系统TPS** | 500 | ≥2000 | 压力测试 |

---

## 🔧 测试工具

### 1. JMeter（推荐）

**安装**:
```bash
# 下载JMeter
wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.2.tgz
tar -xzf apache-jmeter-5.6.2.tgz
cd apache-jmeter-5.6.2/bin
./jmeter.sh
```

**测试计划**:
1. 创建线程组（100并发用户）
2. 添加HTTP请求采样器
3. 配置断言和监听器
4. 执行测试并查看报告

### 2. Spring Boot Actuator

**访问监控端点**:
- 健康检查: `http://localhost:8080/actuator/health`
- 指标: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

### 3. Druid监控

**访问监控页面**:
- URL: `http://localhost:8080/druid/index.html`
- 查看连接池状态、慢SQL统计

---

## 📊 测试场景

### 1. 缓存性能测试

**测试接口**: 用户信息查询接口

**测试步骤**:
1. 预热缓存（执行100次查询）
2. 执行1000次查询
3. 统计缓存命中率

**预期结果**:
- 缓存命中率 ≥90%
- 平均响应时间 ≤5ms

### 2. 数据库查询性能测试

**测试接口**: 分页查询接口

**测试步骤**:
1. 执行100次分页查询
2. 统计响应时间
3. 检查慢查询日志

**预期结果**:
- 平均响应时间 ≤150ms
- 慢查询数量 = 0

### 3. 连接池性能测试

**测试步骤**:
1. 并发执行1000个请求
2. 监控连接池状态
3. 检查连接等待时间

**预期结果**:
- 连接池利用率 ≥90%
- 连接等待时间 ≤100ms

### 4. 系统TPS测试

**测试步骤**:
1. 使用JMeter执行压力测试
2. 逐步增加并发用户数
3. 记录TPS和响应时间

**预期结果**:
- TPS ≥2000
- 响应时间 ≤200ms（P95）

---

## 🔍 验证方法

### 1. 缓存命中率验证

```java
// 获取缓存命中率
double hitRate = cacheManager.getOverallHitRate();
log.info("缓存命中率: {}%", hitRate);

// 获取详细统计
CacheMetricsCollector.CacheStatsInfo stats = cacheManager.getStats("L1");
log.info("L1缓存统计: 命中={}, 未命中={}, 命中率={}%", 
        stats.getHits(), stats.getMisses(), stats.getHitRate());
```

### 2. 查询响应时间验证

```java
// 使用Micrometer监控
@Timed(value = "query.response.time", description = "查询响应时间")
public PageResult<Entity> query(QueryForm form) {
    // 查询逻辑
}
```

### 3. 连接池利用率验证

```java
// 使用DruidConnectionPoolMonitor
Map<String, Object> stats = poolMonitor.getConnectionPoolStats();
log.info("连接池统计: {}", stats);
```

---

## 📈 性能报告

### 测试报告模板

```markdown
# 性能测试报告

## 测试环境
- 服务器配置: 
- 数据库配置: 
- Redis配置: 

## 测试结果
| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 缓存命中率 | 65% | 92% | +42% |
| 查询响应时间 | 800ms | 120ms | -85% |
| 连接池利用率 | 60% | 88% | +47% |
| 系统TPS | 500 | 2200 | +340% |

## 结论
性能优化效果显著，达到预期目标。
```

---

## ⚠️ 注意事项

1. **测试环境**: 使用独立的测试环境，避免影响生产环境
2. **数据准备**: 准备足够的测试数据
3. **监控资源**: 监控CPU、内存、磁盘IO等资源使用情况
4. **逐步加压**: 逐步增加并发用户数，观察系统表现

---

**测试完成后，请生成性能测试报告**

