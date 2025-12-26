# 消费性能优化 - 规格说明

**Capability**: consume-performance
**Change ID**: consume-module-complete-implementation
**类型**: 性能优化

---

## ADDED Requirements

### Requirement: 多级缓存策略

系统MUST实现多级缓存策略，提升查询性能（L1 Caffeine + L2 Redis）

**优先级**: P0

#### Scenario: 账户余额L1本地缓存（Caffeine）

**Given** 账户余额频繁查询
**When** 首次查询账户余额（accountId=12345）
**Then** 应从数据库查询
**And** 将结果缓存到Caffeine本地缓存
**And** 缓存有效期30秒
**When** 30秒内再次查询同一账户余额
**Then** 应从Caffeine缓存返回
**And** 不访问数据库
**And** 响应时间<5ms

#### Scenario: 账户余额L2 Redis缓存

**Given** Caffeine缓存过期
**When** 查询账户余额
**Then** 应从Redis缓存查询
**And** Redis缓存有效期5分钟
**And** 缓存未命中时才查询数据库
**And** 更新Redis缓存

#### Scenario: 缓存命中率目标

**Given** 多级缓存运行中
**When** 统计缓存性能
**Then** L1缓存命中率应≥80%
**And** 总缓存命中率应≥90%

#### Scenario: 缓存一致性保证

**Given** 账户余额数据在Redis和Caffeine中都有缓存
**When** 余额变更（消费或充值）
**Then** 应同时更新：数据库 → Redis → Caffeine
**And** 任一步骤失败时回滚

---

### Requirement: 批量处理优化

系统MUST支持批量处理，提升吞吐量

**优先级**: P0

#### Scenario: 异步批量处理消费请求

**Given** 高峰期有1000个并发消费请求
**When** 使用批量处理
**Then** 应每50个请求为一批
**And** 批量提交到线程池异步处理
**And** 线程池核心线程数=CPU核数*2

#### Scenario: 批量查询优化

**Given** 前端需要显示100个用户的余额
**When** 使用批量查询接口
**Then** 应使用IN查询
**And** 查询时间≤100ms
**And** 相比循环查询性能提升≥10倍

#### Scenario: 批量插入流水记录

**Given** 有100条补贴流水需要插入
**When** 使用批量插入
**Then** 应使用MyBatis-Plus批量插入
**And** 每批50条记录
**And** 插入时间≤500ms

---

### Requirement: 分布式锁

系统MUST使用分布式锁防止并发扣款问题

**优先级**: P0

#### Scenario: Redisson分布式锁防止重复扣款

**Given** 用户账户余额100元
**And** 用户同时发起2个100元的消费请求
**When** 第一个请求获取分布式锁
**Then** 第二个请求应等待
**When** 第一个请求完成，余额变为0元
**Then** 第二个请求获取锁后检查余额
**And** 返回"余额不足"错误

#### Scenario: 分布式锁超时和重入

**Given** 分布式锁配置
**Then** 应支持：
- 锁超时时间：10秒
- 等待时间：5秒
- 可重入：同一线程可多次获取锁

#### Scenario: 锁释放机制

**Given** 消费请求获取了分布式锁
**When** 消费处理完成（成功或失败）
**Then** 应在finally块中释放锁
**And** 异常情况下也应释放锁

---

### Requirement: 数据库分片策略

MUST 大用户量时采用账户分片策略

**优先级**: P1

#### Scenario: 账户表水平分片

**Given** POSID_ACCOUNT表有1000万条记录
**When** 查询单个账户
**Then** 应使用user_id进行分片
**And** 分片数量=16（可配置）
**And** 查询时间≤50ms

#### Scenario: 分片路由策略

**Given** 消费请求携带userId
**When** 路由到对应分片
**Then** 应根据userId计算分片索引
**And** 直接查询对应分片

---

### Requirement: 性能监控指标

系统MUST实时监控性能指标

**优先级**: P0

#### Scenario: TPS监控

**Given** 消费服务运行中
**When** 每秒处理请求
**Then** 应统计实时TPS
**And** 通过Micrometer上报到监控系统
**And** TPS<500时触发告警

#### Scenario: 成功率监控

**Given** 消费服务运行中
**When** 处理1000个请求（950个成功，50个失败）
**Then** 成功率应为95%
**And** 成功率<95%时触发告警

#### Scenario: 响应时间监控

**Given** 消费服务运行中
**When** 记录每个请求的响应时间
**Then** 应统计：平均响应时间、P50、P90、P99、最大响应时间
**And** P99>150ms时触发告警

#### Scenario: 数据库连接池监控

**Given** 使用Druid连接池
**When** 监控连接池状态
**Then** 应统计：活跃连接数、空闲连接数、总连接数、等待线程数
**And** 活跃连接数>80%时触发告警

#### Scenario: 缓存命中率监控

**Given** 多级缓存运行中
**When** 监控缓存性能
**Then** 应统计：L1缓存命中率、L2缓存命中率、总缓存命中率
**And** 缓存命中率<80%时触发告警

---

### Requirement: 性能压测目标

系统MUST通过性能压测验证

**优先级**: P0

#### Scenario: TPS压测验证

**Given** 使用JMeter进行压测
**And** 并发用户数=100
**And** 测试时长=10分钟
**When** 执行消费接口压测
**Then** TPS应≥1000
**And** 错误率<1%

#### Scenario: 响应时间压测验证

**Given** 使用JMeter进行压测
**And** 并发用户数=50
**When** 执行消费接口压测
**Then** 平均响应时间应≤50ms
**And** P99响应时间应≤150ms

#### Scenario: 稳定性压测验证

**Given** 执行长时间压测
**And** 并发用户数=100
**And** 测试时长=2小时
**When** 持续执行消费请求
**Then** TPS应保持稳定（波动<10%）
**And** 无内存泄漏

---

### Requirement: 代码级性能优化

代码层面MUST进行性能优化

**优先级**: P1

#### Scenario: 避免N+1查询

**Given** 查询100个用户的消费记录
**When** 未优化时（循环查询）
**Then** 会执行100次数据库查询
**When** 优化后（使用JOIN或IN查询）
**Then** 应只执行1次数据库查询
**And** 查询时间降低90%+

#### Scenario: JSON字段优化

**Given** mode_config JSON字段频繁查询
**When** 使用JSON_EXTRACT函数查询
**Then** 应在JSON字段上生成虚拟列
**And** 在虚拟列上建立索引
**And** 查询性能提升≥5倍

#### Scenario: 批量删除优化

**Given** 需要删除1000条过期流水
**When** 使用批量删除
**Then** 应使用LIMIT分批删除
**And** 每批1000条
**And** 避免长事务

---

## 验收标准

### 性能验收
- [ ] 消费TPS ≥ 1000
- [ ] 平均响应时间 ≤ 50ms
- [ ] P99响应时间 ≤ 150ms
- [ ] 缓存命中率 ≥ 90%
- [ ] 成功率 ≥ 99%

### 稳定性验收
- [ ] 2小时压测无内存泄漏
- [ ] TPS波动 <10%
- [ ] 无死锁现象

### 监控验收
- [ ] TPS监控指标正确上报
- [ ] 成功率监控指标正确上报
- [ ] 响应时间监控指标正确上报
- [ ] 数据库连接池监控指标正确上报
- [ ] 缓存命中率监控指标正确上报

---

**版本**: v1.0
**最后更新**: 2025-12-23
