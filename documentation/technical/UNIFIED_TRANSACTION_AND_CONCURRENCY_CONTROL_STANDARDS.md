# IOE-DREAM 统一事务管理和并发控制规范

**版本**: v1.0.0
**生效日期**: 2025-12-21
**适用范围**: IOE-DREAM项目所有业务微服务
**规范优先级**: 企业级强制标准，所有事务相关代码必须严格遵循

---

## 📋 核心架构原则

### 1. 分层事务管理原则

```yaml
四层架构事务职责:
  Controller:     禁止事务管理（接口层，只负责参数验证）
  Service:        事务边界（@Transactional，业务编排）
  Manager:        事务传播（事务内业务逻辑编排，可使用事务传播）
  DAO:            只读事务（查询事务，或依赖Service层事务）
```

### 2. 分布式事务原则

```yaml
分布式事务场景:
  跨服务数据一致性:   使用Seata AT模式
  高并发支付场景:     本地事务 + 幂等性设计
  数据同步场景:      使用事件驱动 + 最终一致性
  批量处理场景:      使用SAGA模式 + 补偿机制
```

### 3. 并发控制原则

```yaml
并发控制策略:
  数据库层面:        乐观锁（version） + 悲观锁（SELECT FOR UPDATE）
  缓存层面:        Redisson分布式锁
  应用层面:        Resilience4j限流 + 舱壁隔离
  业务层面:        幂等性设计 + 状态机控制
```

---

## 🏗️ 技术架构规范

### 1. 事务管理技术栈

```yaml
# Spring事务管理
spring:
  transaction:
    default-transaction-manager: DataSourceTransactionManager
    rollback-on-commit-failure: true

# Seata分布式事务
seata:
  enabled: true
  application-id: ${spring.application.name}
  tx-service-group: default_tx_group
  config:
    type: nacos
  registry:
    type: nacos

# 事务传播配置
business:
  transaction:
    timeout: 30000        # 事务超时时间（毫秒）
    retry-times: 3       # 重试次数
    retry-interval: 1000  # 重试间隔（毫秒）
```

### 2. 并发控制技术栈

```yaml
# 分布式锁（Redisson）
redisson:
  codec: org.redisson.codec.JsonJacksonCodec
  threads: 16
  nettyThreads: 32
  transportMode: "NIO"

# 缓存防护（UnifiedCacheManager）
cache:
  protection:
    enable-penetration-protection: true   # 缓存穿透防护
    enable-breakdown-protection: true      # 缓存击穿防护
    enable-avalanche-protection: true      # 缓存雪崩防护
    distributed-lock-timeout: 5000        # 分布式锁超时（毫秒）

# 容错限流（Resilience4j）
resilience4j:
  circuitbreaker:
    failure-rate-threshold: 50%
    wait-duration-in-open-state: 60s
  ratelimiter:
    limit-for-period: 100
    limit-refresh-period: 1s
  bulkhead:
    max-concurrent-calls: 100
```

---

## 📝 事务管理实施规范

### 1. Service层事务管理

#### ✅ 正确的事务管理

```java
@Service
@Transactional(rollbackFor = Exception.class)  // 类级别事务
public class ConsumeAccountServiceImpl implements ConsumeAccountService {

    @Resource
    private ConsumeAccountManager consumeAccountManager;

    /**
     * 账户充值（事务边界）
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)  // 方法级别事务覆盖
    public ResponseDTO<Void> rechargeAccount(Long accountId, ConsumeAccountRechargeForm rechargeForm) {
        log.info("[账户充值] 开始处理充值请求: accountId={}, amount={}", accountId, rechargeForm.getAmount());

        // 1. 参数验证
        validateRechargeForm(accountId, rechargeForm);

        // 2. 业务逻辑（在Manager层处理）
        Boolean result = consumeAccountManager.rechargeAccount(accountId, rechargeForm);

        if (result) {
            log.info("[账户充值] 充值成功: accountId={}, amount={}", accountId, rechargeForm.getAmount());
            return ResponseDTO.ok();
        } else {
            log.error("[账户充值] 充值失败: accountId={}, amount={}", accountId, rechargeForm.getAmount());
            throw new ConsumeBusinessException("RECHARGE_FAILED", "账户充值失败");
        }
    }

    /**
     * 只读查询事务
     */
    @Override
    @Transactional(readOnly = true)  // 只读事务
    public ConsumeAccountVO getAccountDetail(Long accountId) {
        return consumeAccountManager.getAccountDetail(accountId);
    }
}
```

#### ❌ 错误的事务管理

```java
// ❌ 错误：Controller中管理事务
@RestController
public class ConsumeAccountController {

    @Transactional  // 禁止在Controller中使用事务注解
    public ResponseDTO<Void> rechargeAccount(...) {
        // 错误：事务边界不清晰
    }
}

// ❌ 错误：事务粒度过细
@Service
public class ConsumeAccountServiceImpl {

    public void rechargeAccount(Long accountId, BigDecimal amount) {
        // 第一个事务
        updateAccountBalance(accountId, amount);

        // 第二个事务（应该合并为一个事务）
        insertTransactionRecord(accountId, amount);
    }
}

// ❌ 错误：异常处理不当
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeAccountServiceImpl {

    public void rechargeAccount(Long accountId, BigDecimal amount) {
        try {
            updateAccountBalance(accountId, amount);
        } catch (Exception e) {
            // 错误：吞掉异常会导致事务不回滚
            log.error("充值失败", e);
        }
    }
}
```

### 2. 分布式事务管理

#### ✅ Seata分布式事务

```java
@Service
public class ConsumeTransactionServiceImpl implements ConsumeTransactionService {

    @Resource
    private ConsumeAccountService consumeAccountService;

    @Resource
    private DeviceCommService deviceCommService;

    /**
     * 跨服务分布式事务（Seata AT模式）
     */
    @GlobalTransactional(name = "consume-transaction-execute", rollbackFor = Exception.class)
    public ResponseDTO<ConsumeTransactionVO> executeDistributedTransaction(ConsumeTransactionExecuteForm form) {
        log.info("[分布式事务] 开始执行消费交易: {}", form);

        try {
            // 1. 检查账户余额（本地事务）
            ConsumeAccountVO account = consumeAccountService.getAccountDetail(form.getUserId());
            if (account.getBalance().compareTo(form.getAmount()) < 0) {
                throw new ConsumeTransactionException("INSUFFICIENT_BALANCE", "余额不足", form.getDeviceId());
            }

            // 2. 扣减账户余额（本地事务）
            consumeAccountService.deductBalance(account.getAccountId(), form.getAmount());

            // 3. 调用设备服务验证（跨服务调用）
            ResponseDTO<Boolean> deviceResult = deviceCommService.validateDeviceTransaction(form);
            if (!deviceResult.isSuccess() || !deviceResult.getData()) {
                throw new ConsumeTransactionException("DEVICE_VALIDATION_FAILED", "设备验证失败", form.getDeviceId());
            }

            // 4. 创建交易记录（本地事务）
            ConsumeTransactionVO transaction = createTransactionRecord(form);

            log.info("[分布式事务] 消费交易执行成功: transactionId={}", transaction.getTransactionId());
            return ResponseDTO.ok(transaction);

        } catch (Exception e) {
            log.error("[分布式事务] 消费交易执行失败，开始回滚: {}", form, e);
            throw new ConsumeTransactionException("TRANSACTION_FAILED", "交易执行失败: " + e.getMessage(), form.getDeviceId());
        }
    }
}
```

#### ✅ 本地事务 + 幂等性设计

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeTransactionServiceImpl implements ConsumeTransactionService {

    /**
     * 高并发交易处理（本地事务 + 幂等性）
     */
    @Transactional(rollbackFor = Exception.class, timeout = 15)
    public ConsumeTransactionVO executeTransaction(ConsumeTransactionExecuteForm form) {
        log.info("[交易执行] 开始处理交易: {}", form);

        // 1. 幂等性检查（防止重复提交）
        String idempotentKey = generateIdempotentKey(form);
        if (isDuplicateTransaction(idempotentKey)) {
            log.warn("[交易执行] 检测到重复交易: {}", form);
            throw new ConsumeTransactionException.duplicate(idempotentKey);
        }

        // 2. 乐观锁并发控制
        ConsumeAccountEntity account = consumeAccountDao.selectForUpdate(form.getAccountId());
        if (account == null) {
            throw new ConsumeTransactionException.notFound(form.getAccountId().toString());
        }

        // 3. 余额验证
        if (account.getBalance().compareTo(form.getAmount()) < 0) {
            throw new ConsumeTransactionException.invalidAmount(form.getDeviceId(), form.getAmount().toString());
        }

        // 4. 扣减余额（乐观锁更新）
        BigDecimal newBalance = account.getBalance().subtract(form.getAmount());
        int updateCount = consumeAccountDao.updateBalanceWithVersion(
            account.getAccountId(),
            newBalance,
            account.getVersion()
        );

        if (updateCount == 0) {
            log.warn("[交易执行] 乐观锁冲突，账户已被其他事务修改: accountId={}", account.getAccountId());
            throw new ConsumeTransactionException.duplicate("optimistic_lock_conflict");
        }

        // 5. 创建交易记录
        ConsumeTransactionEntity transaction = createTransactionEntity(form, newBalance);
        consumeTransactionDao.insert(transaction);

        // 6. 记录幂等性
        recordIdempotent(idempotentKey, transaction.getId());

        log.info("[交易执行] 交易执行成功: transactionId={}", transaction.getId());
        return convertToVO(transaction);
    }

    /**
     * 生成幂等性键
     */
    private String generateIdempotentKey(ConsumeTransactionExecuteForm form) {
        return String.format("consume_tx:%s:%s:%s:%s",
            form.getUserId(), form.getDeviceId(), form.getAmount(), System.currentTimeMillis() / 60000); // 1分钟窗口
    }
}
```

---

## 🔒 并发控制实施规范

### 1. 分布式锁控制

#### ✅ Redisson分布式锁

```java
@Component
public class ConsumeDistributedLockManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 账户操作分布式锁
     */
    public <T> T executeWithAccountLock(Long accountId, Supplier<T> operation) {
        String lockKey = "lock:account:" + accountId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待5秒，锁定30秒
            boolean acquired = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!acquired) {
                throw new ConsumeBusinessException("LOCK_ACQUIRE_FAILED", "获取账户锁失败，请稍后重试");
            }

            log.debug("[分布式锁] 获取账户锁成功: accountId={}", accountId);
            return operation.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[分布式锁] 获取锁被中断: accountId={}", accountId, e);
            throw new ConsumeBusinessException("LOCK_INTERRUPTED", "锁获取被中断");

        } finally {
            // 释放锁（只释放当前线程持有的锁）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[分布式锁] 释放账户锁: accountId={}", accountId);
            }
        }
    }

    /**
     * 设备操作分布式锁
     */
    public <T> T executeWithDeviceLock(String deviceId, Supplier<T> operation) {
        String lockKey = "lock:device:" + deviceId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(3, 15, TimeUnit.SECONDS);
            if (!acquired) {
                throw new ConsumeBusinessException("DEVICE_LOCK_FAILED", "设备正忙，请稍后重试");
            }

            log.debug("[分布式锁] 获取设备锁成功: deviceId={}", deviceId);
            return operation.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConsumeBusinessException("DEVICE_LOCK_INTERRUPTED", "设备锁获取被中断");

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[分布式锁] 释放设备锁: deviceId={}", deviceId);
            }
        }
    }
}
```

#### ✅ Manager层分布式锁使用

```java
@Service
public class ConsumeAccountManagerImpl implements ConsumeAccountManager {

    @Resource
    private ConsumeDistributedLockManager lockManager;

    @Resource
    private ConsumeAccountDao consumeAccountDao;

    /**
     * 扣减账户余额（使用分布式锁）
     */
    @Override
    public Boolean deductBalance(Long accountId, BigDecimal amount, String description) {
        return lockManager.executeWithAccountLock(accountId, () -> {
            log.info("[账户管理] 开始扣减余额: accountId={}, amount={}", accountId, amount);

            // 1. 获取账户信息
            ConsumeAccountEntity account = consumeAccountDao.selectById(accountId);
            if (account == null) {
                throw new ConsumeAccountException.notFound(accountId);
            }

            // 2. 验证余额
            if (account.getBalance().compareTo(amount) < 0) {
                throw new ConsumeAccountException.insufficientBalance(accountId, account.getBalance(), amount);
            }

            // 3. 扣减余额（乐观锁）
            BigDecimal newBalance = account.getBalance().subtract(amount);
            int updateCount = consumeAccountDao.updateBalanceWithVersion(accountId, newBalance, account.getVersion());

            if (updateCount == 0) {
                throw new ConsumeAccountException.concurrentModification(accountId);
            }

            log.info("[账户管理] 余额扣减成功: accountId={}, amount={}, newBalance={}",
                accountId, amount, newBalance);
            return true;
        });
    }
}
```

### 2. 数据库层面并发控制

#### ✅ 乐观锁控制

```java
// Entity实体设计
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_account")
public class ConsumeAccountEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long accountId;

    @TableField("balance")
    @DecimalMin(value = "0.00", message = "余额不能为负数")
    @Digits(integer = 10, fraction = 2, message = "金额格式错误")
    private BigDecimal balance;

    @Version  // 乐观锁版本字段
    @TableField("version")
    private Integer version;

    // 其他字段...
}

// DAO层乐观锁更新
@Mapper
public interface ConsumeAccountDao extends BaseMapper<ConsumeAccountEntity> {

    /**
     * 乐观锁更新账户余额
     */
    @Update("UPDATE t_consume_account " +
            "SET balance = #{balance}, version = version + 1, update_time = NOW() " +
            "WHERE account_id = #{accountId} AND version = #{version}")
    int updateBalanceWithVersion(@Param("accountId") Long accountId,
                                @Param("balance") BigDecimal balance,
                                @Param("version") Integer version);

    /**
     * 悲观锁查询账户（SELECT FOR UPDATE）
     */
    @Select("SELECT * FROM t_consume_account " +
            "WHERE account_id = #{accountId} AND deleted_flag = 0 " +
            "FOR UPDATE")
    ConsumeAccountEntity selectForUpdate(@Param("accountId") Long accountId);
}
```

#### ✅ 悲观锁控制

```java
@Service
public class ConsumeAccountManagerImpl implements ConsumeAccountManager {

    /**
     * 高风险账户操作（使用悲观锁）
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public Boolean高风险操作(Long accountId, BigDecimal amount) {
        log.info("[账户管理] 开始高风险操作: accountId={}, amount={}", accountId, amount);

        // 1. 悲观锁锁定账户记录
        ConsumeAccountEntity account = consumeAccountDao.selectForUpdate(accountId);
        if (account == null) {
            throw new ConsumeAccountException.notFound(accountId);
        }

        // 2. 业务处理（账户已被锁定，其他事务需要等待）
        // ... 业务逻辑处理

        log.info("[账户管理] 高风险操作完成: accountId={}", accountId);
        return true;
    }
}
```

---

## 🛡️ 容错和降级规范

### 1. Resilience4j容错配置

#### ✅ 熔断器配置

```java
@Service
public class ConsumeTransactionServiceImpl implements ConsumeTransactionService {

    @Resource
    private ConsumeAccountService consumeAccountService;

    /**
     * 支付处理（熔断器保护）
     */
    @CircuitBreaker(name = "payment-processing", fallbackMethod = "fallbackPaymentProcessing")
    @TimeLimiter(name = "payment-processing")
    @Bulkhead(name = "payment-processing", type = Bulkhead.Type.THREADPOOL)
    public ResponseDTO<ConsumeTransactionVO> processPayment(ConsumeTransactionExecuteForm form) {
        log.info("[支付处理] 开始处理支付: {}", form);

        // 支付处理逻辑
        return executePaymentTransaction(form);
    }

    /**
     * 支付处理降级方法
     */
    public ResponseDTO<ConsumeTransactionVO> fallbackPaymentProcessing(ConsumeTransactionExecuteForm form, Exception e) {
        log.error("[支付处理] 熔断降级: {}, error={}", form, e.getMessage());

        // 降级逻辑：返回友好的错误信息，记录降级日志
        ConsumeTransactionVO fallbackVO = new ConsumeTransactionVO();
        fallbackVO.setStatus("DEGRADED");
        fallbackVO.setErrorMsg("系统繁忙，请稍后重试");

        return ResponseDTO.businessError("PAYMENT_DEGRADED", "支付服务暂时不可用，请稍后重试");
    }
}
```

#### ✅ 限流配置

```java
@Service
public class ConsumeAccountServiceImpl implements ConsumeAccountService {

    /**
     * 账户查询（限流保护）
     */
    @RateLimiter(name = "query-api", fallbackMethod = "fallbackQueryAccount")
    @TimeLimiter(name = "query-api")
    public ResponseDTO<ConsumeAccountVO> getAccountDetail(Long accountId) {
        log.debug("[账户查询] 查询账户详情: accountId={}", accountId);

        // 查询逻辑
        return ResponseDTO.ok(consumeAccountManager.getAccountDetail(accountId));
    }

    /**
     * 账户查询限流降级
     */
    public ResponseDTO<ConsumeAccountVO> fallbackQueryAccount(Long accountId, Exception e) {
        log.warn("[账户查询] 限流降级: accountId={}, error={}", accountId, e.getMessage());

        // 限流降级：返回缓存数据或友好提示
        return ResponseDTO.businessError("RATE_LIMIT_EXCEEDED", "查询过于频繁，请稍后重试");
    }
}
```

### 2. 缓存防护机制

#### ✅ 三级缓存防护

```java
@Service
public class ConsumeAccountServiceImpl implements ConsumeAccountService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 获取账户信息（缓存防护）
     */
    @Override
    @Transactional(readOnly = true)
    public ConsumeAccountVO getAccountDetail(Long accountId) {
        log.debug("[账户查询] 获取账户详情: accountId={}", accountId);

        String cacheKey = "consume:account:info:" + accountId;

        // 使用统一缓存管理器（包含穿透、击穿、雪崩防护）
        return cacheManager.get(
            cacheKey,
            ConsumeAccountVO.class,
            () -> {
                // 数据加载器：缓存未命中时从数据库加载
                ConsumeAccountVO account = consumeAccountManager.getAccountDetail(accountId);
                if (account == null) {
                    log.debug("[账户查询] 账户不存在: accountId={}", accountId);
                }
                return account;
            },
            Duration.ofMinutes(30)  // TTL: 30分钟
        );
    }

    /**
     * 清除账户缓存
     */
    @Override
    public void evictAccountCache(Long accountId) {
        String cacheKey = "consume:account:info:" + accountId;
        cacheManager.evict(cacheKey);
        log.debug("[账户查询] 清除账户缓存: accountId={}", accountId);
    }
}
```

---

## 📊 监控和告警规范

### 1. 事务监控指标

```java
@Component
public class TransactionMonitorMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter transactionSuccessCounter;
    private final Counter transactionFailureCounter;
    private final Timer transactionDurationTimer;

    public TransactionMonitorMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.transactionSuccessCounter = Counter.builder("consume_transaction_success_total")
            .description("消费交易成功总数")
            .register(meterRegistry);
        this.transactionFailureCounter = Counter.builder("consume_transaction_failure_total")
            .description("消费交易失败总数")
            .tag("error_type", "unknown")
            .register(meterRegistry);
        this.transactionDurationTimer = Timer.builder("consume_transaction_duration_seconds")
            .description("消费交易处理时间")
            .register(meterRegistry);
    }

    /**
     * 记录交易成功
     */
    public void recordTransactionSuccess(String transactionType, BigDecimal amount) {
        transactionSuccessCounter.increment(
            Tags.of("type", transactionType, "amount_range", getAmountRange(amount))
        );
    }

    /**
     * 记录交易失败
     */
    public void recordTransactionFailure(String transactionType, String errorType, BigDecimal amount) {
        transactionFailureCounter.increment(
            Tags.of("type", transactionType, "error_type", errorType, "amount_range", getAmountRange(amount))
        );
    }

    /**
     * 记录交易处理时间
     */
    public void recordTransactionDuration(Duration duration, String transactionType) {
        transactionDurationTimer.record(duration, Tags.of("type", transactionType));
    }

    private String getAmountRange(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("10")) <= 0) return "0-10";
        if (amount.compareTo(new BigDecimal("100")) <= 0) return "10-100";
        if (amount.compareTo(new BigDecimal("1000")) <= 0) return "100-1000";
        return "1000+";
    }
}
```

### 2. 分布式锁监控

```java
@Component
public class DistributedLockMonitor {

    private final MeterRegistry meterRegistry;
    private final Counter lockAcquireSuccessCounter;
    private final Counter lockAcquireFailureCounter;
    private final Timer lockWaitTimeTimer;

    public DistributedLockMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.lockAcquireSuccessCounter = Counter.builder("distributed_lock_acquire_success_total")
            .description("分布式锁获取成功总数")
            .register(meterRegistry);
        this.lockAcquireFailureCounter = Counter.builder("distributed_lock_acquire_failure_total")
            .description("分布式锁获取失败总数")
            .register(meterRegistry);
        this.lockWaitTimeTimer = Timer.builder("distributed_lock_wait_time_seconds")
            .description("分布式锁等待时间")
            .register(meterRegistry);
    }

    /**
     * 记录锁获取成功
     */
    public void recordLockAcquireSuccess(String lockType, String resourceId) {
        lockAcquireSuccessCounter.increment(
            Tags.of("type", lockType, "resource_id", resourceId)
        );
    }

    /**
     * 记录锁获取失败
     */
    public void recordLockAcquireFailure(String lockType, String resourceId, String reason) {
        lockAcquireFailureCounter.increment(
            Tags.of("type", lockType, "resource_id", resourceId, "reason", reason)
        );
    }

    /**
     * 记录锁等待时间
     */
    public void recordLockWaitTime(Duration waitTime, String lockType) {
        lockWaitTimeTimer.record(waitTime, Tags.of("type", lockType));
    }
}
```

---

## 📋 实施检查清单

### 1. 代码实施检查

- [ ] Service层使用@Transactional注解，异常回滚配置正确
- [ ] 跨服务调用使用@GlobalTransactional注解
- [ ] 高并发操作使用分布式锁保护
- [ ] 数据库更新使用乐观锁或悲观锁
- [ ] 幂等性设计完善，防止重复操作
- [ ] 缓存防护机制生效（穿透、击穿、雪崩）
- [ ] 容错降级机制配置正确
- [ ] 监控指标埋点完整

### 2. 配置检查

- [ ] Seata配置正确（application-seata.yml）
- [ ] Redisson配置正确（分布式锁）
- [ ] Resilience4j配置正确（容错限流）
- [ ] 缓存配置正确（UnifiedCacheManager）
- [ ] 监控配置正确（Micrometer）

### 3. 测试验证

- [ ] 并发测试验证锁机制有效
- [ ] 事务回滚测试验证
- [ ] 幂等性测试验证
- [ ] 熔断降级测试验证
- [ ] 缓存防护测试验证
- [ ] 监控指标测试验证

---

## 🔗 相关文档参考

### 📋 核心规范文档

- **[CLAUDE.md - 全局架构标准](../../CLAUDE.md)** - **最高架构规范**
- **[UNIFIED_DEVELOPMENT_STANDARDS.md](./UNIFIED_DEVELOPMENT_STANDARDS.md)** - 统一开发标准
- **[UNIFIED_CACHE_MANAGER_MIGRATION_GUIDE.md](./UNIFIED_CACHE_MANAGER_MIGRATION_GUIDE.md)** - 缓存管理规范
- **[application-seata.yml](../common-config/seata/application-seata.yml)** - Seata配置
- **[resilience4j-application.yml](../common-config/resilience4j-application.yml)** - 容错配置

### 🏗️ 技术实施指导

- **[Seata官方文档](https://seata.io/zh-cn/)** - 分布式事务框架
- **[Redisson官方文档](https://github.com/redisson/redisson)** - 分布式锁框架
- **[Resilience4j官方文档](https://resilience4j.readme.io/)** - 容错框架
- **[Spring事务管理](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)** - Spring事务

---

**重要提醒**: 本文档是IOE-DREAM项目的统一事务管理和并发控制规范，所有开发人员必须严格遵循。违反事务和并发控制规范可能导致数据不一致、性能问题、系统故障等严重后果。

**👥 制定人**: IOE-DREAM架构委员会
**🏗️ 技术架构师**: SmartAdmin核心团队
**✅ 最终解释权**: IOE-DREAM项目架构委员会
**📅 版本**: v1.0.0 - 企业级增强版