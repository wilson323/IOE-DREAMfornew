# 消费模块核心引擎设计文档

## 🏗️ 架构设计

### 系统架构概览

基于IOE-DREAM项目严格四层架构规范，消费核心引擎采用分层设计：

```
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                         │
│  ConsumeController | AccountController | ModeController     │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer                            │
│  ConsumeEngineService | AccountService | ModeService        │
├─────────────────────────────────────────────────────────────┤
│                    Manager Layer                             │
│  ConsumeCacheManager | SecurityManager | LockManager       │
├─────────────────────────────────────────────────────────────┤
│                    DAO Layer                                │
│  ConsumeRecordDao | AccountDao | TransactionDao            │
└─────────────────────────────────────────────────────────────┘
```

### 核心组件设计

#### 1. 核心消费引擎 (ConsumeEngineService)

**职责**: 统一处理所有消费交易的核心引擎，确保原子性、安全性、高性能

```java
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ConsumeEngineService {

    @Resource
    private AccountService accountService;

    @Resource
    private ConsumeModeEngine modeEngine;

    @Resource
    private ConsumeCacheManager cacheManager;

    @Resource
    private DistributedLockManager lockManager;

    /**
     * 核心消费处理方法 - 严格确保原子性
     *
     * @param request 消费请求
     * @return 消费结果
     */
    public ConsumeResult processConsume(ConsumeRequest request) {
        // 1. 参数验证和预处理
        validateConsumeRequest(request);

        // 2. 幂等性检查（基于订单号）
        checkIdempotency(request.getOrderNo());

        // 3. 分布式锁保护（防止并发重复扣费）
        String lockKey = "consume:lock:" + request.getAccountId();
        return lockManager.executeWithLock(lockKey, 30, TimeUnit.SECONDS, () -> {
            return doProcessConsume(request);
        });
    }

    /**
     * 实际消费处理逻辑 - 单一事务内执行
     */
    @Transactional(rollbackFor = Exception.class)
    private ConsumeResult doProcessConsume(ConsumeRequest request) {
        // 1. 账户状态验证和余额检查
        AccountValidationResult validation = validateAccount(request);
        if (!validation.isValid()) {
            throw new BusinessException(validation.getErrorMessage());
        }

        // 2. 消费模式处理和金额计算
        ConsumeModeResult modeResult = modeEngine.processMode(request);

        // 3. 原子性余额扣减（关键操作）
        AccountDeductResult deductResult = accountService.deductBalance(
            request.getAccountId(),
            modeResult.getAmount(),
            request.getOrderNo()
        );

        // 4. 交易记录创建
        ConsumeRecordEntity record = createConsumeRecord(request, modeResult, deductResult);

        // 5. 缓存更新和异步处理
        cacheManager.invalidateAccountCache(request.getAccountId());
        publishConsumeEvent(record);

        return ConsumeResult.success(record, modeResult);
    }
}
```

**关键特性**:
- **原子性保证**: Spring Boot事务确保余额扣减和记录创建的一致性
- **幂等性保护**: 基于订单号的防重复处理机制
- **分布式锁**: Redis分布式锁防止并发问题
- **异常安全**: 完善的异常处理和数据补偿机制
- **性能优化**: 多级缓存和异步事件处理

#### 2. 账户管理体系 (AccountService)

**职责**: 完整的账户生命周期管理和资金安全操作

```java
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AccountService {

    @Resource
    private AccountDao accountDao;

    @Resource
    private AccountBalanceDao balanceDao;

    @Resource
    private DistributedLockManager lockManager;

    /**
     * 账户余额扣减 - 原子性操作
     *
     * @param accountId 账户ID
     * @param amount 扣减金额
     * @param orderNo 订单号
     * @return 扣减结果
     */
    @Transactional(rollbackFor = Exception.class)
    public AccountDeductResult deductBalance(Long accountId, BigDecimal amount, String orderNo) {
        // 1. 分布式锁保护
        String lockKey = "account:deduct:" + accountId;
        return lockManager.executeWithLock(lockKey, 10, TimeUnit.SECONDS, () -> {
            return doDeductBalance(accountId, amount, orderNo);
        });
    }

    /**
     * 实际扣减逻辑 - 乐观锁 + 版本控制
     */
    private AccountDeductResult doDeductBalance(Long accountId, BigDecimal amount, String orderNo) {
        // 1. 获取账户信息（乐观锁）
        AccountEntity account = accountDao.selectByIdForUpdate(accountId);
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        // 2. 账户状态验证
        validateAccountStatus(account);

        // 3. 余额充足性验证
        BigDecimal availableBalance = account.getBalance()
            .add(account.getCreditLimit())
            .subtract(account.getFrozenAmount());

        if (availableBalance.compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }

        // 4. 消费限额验证
        validateConsumeLimit(account, amount);

        // 5. 乐观锁更新余额（关键原子操作）
        int updateCount = accountDao.deductBalanceWithVersion(
            accountId, amount, account.getVersion()
        );

        if (updateCount == 0) {
            throw new BusinessException("余额扣减失败，请重试");
        }

        // 6. 记录余额变动
        AccountBalanceChangeEntity change = createBalanceChange(account, amount, orderNo);
        balanceDao.insert(change);

        // 7. 更新缓存
        cacheManager.evictAccountBalance(accountId);

        return AccountDeductResult.success(account.getBalance().subtract(amount), change.getId());
    }
}
```

#### 3. 消费模式引擎重构 (ConsumptionModeEngine)

**职责**: 真正实现6种消费模式的业务逻辑

```java
@Component
@Slf4j
public class ConsumptionModeEngine {

    private final Map<String, ConsumptionMode> modeRegistry = new ConcurrentHashMap<>();

    @Resource
    private ConsumptionModeFactory modeFactory;

    /**
     * 消费模式处理 - 真正的业务逻辑实现
     */
    public ConsumeModeResult processMode(ConsumeRequest request) {
        String modeCode = request.getConsumeMode();
        if (StringUtils.isBlank(modeCode)) {
            modeCode = "FIXED_AMOUNT"; // 默认固定金额模式
        }

        // 获取消费模式实现
        ConsumptionMode mode = modeFactory.getMode(modeCode);
        if (mode == null) {
            throw new BusinessException("不支持的消费模式: " + modeCode);
        }

        try {
            // 1. 参数验证
            if (!mode.validateParameters(request)) {
                throw new BusinessException("消费参数验证失败");
            }

            // 2. 权限和时间检查
            if (!mode.isAllowed(request)) {
                throw new BusinessException("当前不允许执行此消费操作");
            }

            // 3. 预处理（可选）
            Map<String, Object> preResult = mode.preProcess(request);

            // 4. 金额计算（核心逻辑）
            BigDecimal amount = mode.calculateAmount(request);

            // 5. 后处理（可选）
            Map<String, Object> postResult = mode.postProcess(request, amount);

            return ConsumeModeResult.success(modeCode, amount, preResult, postResult);

        } catch (Exception e) {
            log.error("消费模式处理失败: modeCode={}, request={}", modeCode, request, e);
            throw new BusinessException("消费处理失败: " + e.getMessage());
        }
    }
}
```

#### 4. 6种消费模式具体实现

##### 4.1 固定金额模式 (FixedAmountMode)

```java
@Component
@Order(1)
public class FixedAmountMode implements ConsumptionMode {

    @Override
    public String getModeId() {
        return "FIXED_AMOUNT";
    }

    @Override
    public String getModeName() {
        return "固定金额模式";
    }

    @Override
    public boolean validateParameters(ConsumeRequest request) {
        // 固定金额模式的参数验证
        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            // 支持自定义金额
            return isValidFixedAmount(request.getAmount());
        }

        // 支持预定义金额档位
        String presetAmount = request.getPresetAmount();
        return StringUtils.isBlank(presetAmount) || isValidPresetAmount(presetAmount);
    }

    @Override
    public boolean isAllowed(ConsumeRequest request) {
        // 1. 基础时间检查（工作时间）
        if (!isWorkingHours(request.getConsumeTime())) {
            return false;
        }

        // 2. 餐别检查（如果启用）
        if (request.isMealTimeRestricted()) {
            return isValidMealTime(request.getConsumeTime());
        }

        // 3. 设备权限检查
        return validateDevicePermission(request.getDeviceId());
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequest request) {
        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            // 使用预定义金额档位
            String presetAmount = request.getPresetAmount();
            amount = getPresetAmountValue(presetAmount);
        }

        // 金额有效性验证
        if (!isValidFixedAmount(amount)) {
            throw new BusinessException("无效的固定金额: " + amount);
        }

        // 应用折扣或补贴
        BigDecimal finalAmount = applyDiscountOrSubsidy(request, amount);

        log.info("固定金额模式计算完成: 原金额={}, 最终金额={}", amount, finalAmount);
        return finalAmount;
    }

    /**
     * 有效的固定金额档位
     */
    private boolean isValidFixedAmount(BigDecimal amount) {
        return FIXED_AMOUNTS.stream().anyMatch(fixed -> fixed.compareTo(amount) == 0);
    }

    private static final List<BigDecimal> FIXED_AMOUNTS = Arrays.asList(
        new BigDecimal("5.00"), new BigDecimal("8.00"), new BigDecimal("10.00"),
        new BigDecimal("12.00"), new BigDecimal("15.00"), new BigDecimal("18.00"),
        new BigDecimal("20.00"), new BigDecimal("25.00")
    );
}
```

##### 4.2 自由金额模式 (FreeAmountMode)

```java
@Component
@Order(2)
public class FreeAmountMode implements ConsumptionMode {

    @Override
    public String getModeId() {
        return "FREE_AMOUNT";
    }

    @Override
    public String getModeName() {
        return "自由金额模式";
    }

    @Override
    public boolean validateParameters(ConsumeRequest request) {
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 金额范围检查：0.01 - 9999.99
        if (amount.compareTo(new BigDecimal("0.01")) < 0 ||
            amount.compareTo(new BigDecimal("9999.99")) > 0) {
            return false;
        }

        // 小数位数检查：最多2位
        if (amount.scale() > 2) {
            return false;
        }

        return true;
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequest request) {
        BigDecimal amount = request.getAmount();

        // 1. 应用商品分类折扣
        BigDecimal discountedAmount = applyCategoryDiscount(request, amount);

        // 2. 应用会员折扣
        BigDecimal memberAmount = applyMemberDiscount(request, discountedAmount);

        // 3. 应用优惠券或积分
        BigDecimal finalAmount = applyCouponOrPoints(request, memberAmount);

        log.info("自由金额模式计算完成: 原金额={}, 最终金额={}", amount, finalAmount);
        return finalAmount;
    }
}
```

### 数据库设计

#### 核心表结构优化

**1. 账户余额表 (t_consume_account_balance)**
- 添加版本控制字段（version）支持乐观锁
- 添加冻结金额字段（frozen_amount）
- 添加信用额度字段（credit_limit）
- 添加日/月累计消费字段

**2. 消费记录表 (t_consume_record)**
- 完善索引设计，提升查询性能
- 添加消费模式详情字段（mode_detail）
- 添加退款关联字段（refund_record_id）
- 添加操作日志关联字段（operation_log_id）

**3. 余额变动记录表 (t_consume_balance_change)**
- 记录所有余额变动历史
- 支持对账和数据恢复
- 包含变动类型、金额、时间等详细信息

### 安全设计

#### 资金安全保障

1. **分布式锁机制**
```java
@Component
public class DistributedLockManager {

    @Resource
    private RedissonClient redissonClient;

    public <T> T executeWithLock(String lockKey, long waitTime, TimeUnit unit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(waitTime, unit)) {
                return supplier.get();
            } else {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("操作被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

2. **幂等性保护**
```java
@Service
public class IdempotencyService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public boolean checkAndMarkProcessed(String orderNo) {
        String key = "idempotent:consume:" + orderNo;

        // 检查是否已处理
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return true; // 已处理
        }

        // 标记为已处理，设置过期时间24小时
        redisTemplate.opsForValue().set(key, "processed", 24, TimeUnit.HOURS);
        return false; // 未处理
    }
}
```

### 性能设计

#### 多级缓存架构

```java
@Service
public class ConsumeCacheManager {

    // L1: 本地缓存
    private final Cache<String, AccountEntity> localAccountCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

    // L2: 分布式缓存
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public AccountEntity getAccount(Long accountId) {
        String key = "account:info:" + accountId;

        // 1. 检查L1缓存
        AccountEntity account = localAccountCache.getIfPresent(key);
        if (account != null) {
            return account;
        }

        // 2. 检查L2缓存
        account = (AccountEntity) redisTemplate.opsForValue().get(key);
        if (account != null) {
            localAccountCache.put(key, account);
            return account;
        }

        // 3. 查询数据库
        account = accountDao.selectById(accountId);
        if (account != null) {
            redisTemplate.opsForValue().set(key, account, 30, TimeUnit.MINUTES);
            localAccountCache.put(key, account);
        }

        return account;
    }

    public void evictAccount(Long accountId) {
        String key = "account:info:" + accountId;
        localAccountCache.invalidate(key);
        redisTemplate.delete(key);
    }
}
```

### 监控设计

#### 关键指标监控

1. **业务指标**
   - 消费交易成功率
   - 平均响应时间
   - 并发处理能力
   - 资金安全指标

2. **技术指标**
   - 数据库连接池状态
   - 缓存命中率
   - 分布式锁竞争情况
   - 事务执行时间

3. **安全指标**
   - 异常操作检测
   - 重复操作统计
   - 权限违规监控
   - 数据一致性检查

---

**设计原则**: 基于项目已有框架和严格规范，优先确保资金安全和数据一致性，通过分阶段实现和持续优化，构建真正可用的企业级消费管理系统。