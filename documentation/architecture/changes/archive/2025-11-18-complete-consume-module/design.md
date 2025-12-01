# 消费模块设计文档

## 🏗️ 架构设计

### 系统架构概览

基于repowiki四层架构规范，消费模块采用严格的分层设计：

```
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                         │
│  ConsumeController | AccountController | ReportController  │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer                            │
│  ConsumeEngineService | AccountService | RefundService     │
├─────────────────────────────────────────────────────────────┤
│                    Manager Layer                             │
│  ConsumeCacheManager | SecurityManager | LimitManager      │
├─────────────────────────────────────────────────────────────┤
│                    DAO Layer                                │
│  ConsumeRecordDao | AccountDao | TransactionDao            │
└─────────────────────────────────────────────────────────────┘
```

### 核心组件设计

#### 1. 核心消费引擎 (ConsumeEngineService)

**职责**: 统一处理所有消费交易的核心引擎

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeEngineService {

    /**
     * 核心消费处理方法
     * 确保原子性、一致性、隔离性、持久性
     */
    public ConsumeResult processConsume(ConsumeRequest request) {
        // 1. 参数验证和预处理
        // 2. 幂等性检查
        // 3. 账户状态验证
        // 4. 权限和限额检查
        // 5. 消费模式处理
        // 6. 原子性余额扣减
        // 7. 交易记录创建
        // 8. 缓存更新
        // 9. 通知和事件发布
        // 10. 异常处理和补偿
    }
}
```

**关键特性**:
- **原子性保证**: 使用数据库事务确保余额扣减和记录创建的一致性
- **幂等性保护**: 基于订单号的防重复处理机制
- **异常安全**: 完善的异常处理和数据补偿机制
- **性能优化**: 多级缓存和异步处理

#### 2. 账户管理体系 (AccountService)

**职责**: 完整的账户生命周期管理

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountService {

    /**
     * 账户余额扣减 - 原子性操作
     */
    public AccountDeductResult deductBalance(Long accountId, BigDecimal amount, String orderNo) {
        // 1. 账户状态检查
        // 2. 余额验证
        // 3. 消费限额检查
        // 4. 乐观锁更新余额
        // 5. 记录余额变动
        // 6. 缓存同步更新
    }

    /**
     * 账户充值处理
     */
    public AccountRechargeResult rechargeBalance(AccountRechargeRequest request) {
        // 1. 充值参数验证
        // 2. 支付方式验证
        // 3. 余额增加操作
        // 4. 充值记录创建
        // 5. 通知发送
    }
}
```

#### 3. 消费模式引擎 (ConsumptionModeEngine)

**职责**: 支持6种消费模式的插件化引擎

```java
@Component
public class ConsumptionModeEngine {

    private final Map<String, ConsumptionMode> modeRegistry = new ConcurrentHashMap<>();

    /**
     * 模式注册机制
     */
    public void registerMode(String modeCode, ConsumptionMode mode) {
        modeRegistry.put(modeCode, mode);
    }

    /**
     * 消费模式处理
     */
    public ConsumeModeResult processMode(ConsumeModeRequest request) {
        ConsumptionMode mode = modeRegistry.get(request.getMode());
        if (mode == null) {
            throw new BusinessException("不支持的消费模式: " + request.getMode());
        }
        return mode.process(request);
    }
}
```

**6种消费模式**:

1. **固定金额模式 (FixedAmountMode)**
   - 场景: 餐厅、食堂固定价格消费
   - 实现: 简单金额验证和扣费

2. **自由金额模式 (FreeAmountMode)**
   - 场景: 超市、商店自由购物
   - 实现: 金额范围检查和灵活扣费

3. **计量计费模式 (MeteringMode)**
   - 场景: 按重量、体积、时间计费
   - 实现: 计量验证和动态计费

4. **商品消费模式 (ProductMode)**
   - 场景: 扫码商品消费
   - 实现: 商品验证、库存扣减、价格计算

5. **订餐消费模式 (OrderingMode)**
   - 场景: 预订餐食消费
   - 实现: 订餐验证、餐别处理、补贴计算

6. **智能消费模式 (SmartMode)**
   - 场景: AI推荐消费
   - 实现: 智能推荐、个性化定价

### 数据库设计

#### 核心表结构

**1. 消费账户表 (t_consume_account)**
- 设计完整度: 95%
- 字段数量: 75个核心字段
- 关键特性: 支持多种账户类型、信用额度、消费限额

**2. 消费记录表 (t_consume_record)**
- 设计完整度: 90%
- 字段数量: 35个业务字段
- 关键特性: 完整的交易记录和审计字段

**3. 消费设备配置表 (t_consume_device_config)**
- 设计完整度: 95%
- 字段数量: 50+ 配置字段
- 关键特性: 设备管理、离线支持、远程控制

### 安全设计

#### 资金安全保障

1. **原子性事务**
```java
@Transactional(rollbackFor = Exception.class)
public ConsumeResult processConsume(ConsumeRequest request) {
    // 余额扣减和记录创建在同一事务中
    accountService.deductBalance(accountId, amount);
    consumeRecordDao.create(record);
    // 事务自动提交或回滚
}
```

2. **幂等性保护**
```java
public ConsumeResult processConsume(ConsumeRequest request) {
    // 基于订单号的幂等性检查
    if (consumeRecordDao.existsByOrderNo(request.getOrderNo())) {
        return getExistingResult(request.getOrderNo());
    }
    // 执行实际的消费处理
}
```

3. **余额验证**
```java
public void validateBalance(Long accountId, BigDecimal amount) {
    AccountEntity account = accountService.getById(accountId);
    BigDecimal availableBalance = account.getBalance().add(account.getCreditLimit())
                                           .subtract(account.getFrozenAmount());
    if (availableBalance.compareTo(amount) < 0) {
        throw new BusinessException("余额不足");
    }
}
```

### 性能设计

#### 多级缓存架构

```java
@Service
public class ConsumeCacheManager {

    // L1: 本地缓存 (Caffeine)
    private final Cache<String, AccountEntity> localAccountCache;

    // L2: 分布式缓存 (Redis)
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 账户余额缓存 - 读写策略
     */
    public AccountEntity getAccountWithCache(Long accountId) {
        String key = "account:balance:" + accountId;

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

        // 3. 查询数据库并更新缓存
        account = accountDao.selectById(accountId);
        if (account != null) {
            redisTemplate.opsForValue().set(key, account, Duration.ofMinutes(30));
            localAccountCache.put(key, account);
        }

        return account;
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
   - 内存使用情况
   - GC性能指标

3. **安全指标**
   - 异常操作检测
   - 重复操作统计
   - 权限违规监控
   - 数据一致性检查

### 集成设计

#### 外部系统集成

1. **门禁系统集成**
   - 无感通行消费
   - 权限联动验证
   - 设备状态同步

2. **考勤系统集成**
   - 考勤补贴发放
   - 异常考勤处理
   - 数据统计分析

3. **支付系统集成**
   - 第三方支付对接
   - 资金清算处理
   - 对账 reconciliation

### 部署设计

#### 环境配置

1. **开发环境**
   - 单机部署
   - H2内存数据库
   - 本地Redis

2. **测试环境**
   - 容器化部署
   - MySQL主从
   - Redis集群

3. **生产环境**
   - Kubernetes集群
   - MySQL集群
   - Redis集群
   - 监控告警

---

**设计原则**: 基于已有的优秀数据库设计，严格遵循repowiki四层架构规范，优先确保资金安全，通过分阶段实施实现完整的企业级消费管理系统。