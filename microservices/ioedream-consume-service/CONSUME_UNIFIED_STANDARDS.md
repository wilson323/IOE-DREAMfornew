# 消费模块企业级统一规范

## 文档概述

本文档定义了IOE-DREAM智慧园区一卡通平台消费模块的企业级统一规范，包括代码架构、命名规范、异常处理、事务管理、监控日志等全方面标准。

**制定日期**: 2025-12-21
**版本**: v1.0.0
**适用范围**: 消费模块全量开发工作

---

## 🎯 核心设计原则

### 1. 四层架构强制标准
```
Controller → Service → Manager → DAO
```

**严格规范**:
- ✅ Controller: 只负责HTTP请求响应，参数验证
- ✅ Service: 业务逻辑编排，事务边界控制
- ✅ Manager: 纯Java业务组件，无Spring注解
- ✅ DAO: 数据访问，使用@Mapper注解

### 2. 包结构强制规范
```
net.lab1024.sa.consume/
├── config/           # 配置类
├── controller/       # REST控制器
├── service/          # 服务接口和实现
├── manager/          # 业务编排层（纯Java）
├── dao/              # 数据访问层
├── entity/           # 实体类
├── domain/           # 领域对象
│   ├── form/        # 请求表单
│   └── vo/          # 响应视图
├── exception/        # 异常类
├── util/             # 工具类
└── ConsumeServiceApplication.java
```

### 3. 命名规范强制标准

| 类型 | 规范 | 示例 |
|------|------|------|
| Controller | `XxxController` | `ConsumeAccountController` |
| Service接口 | `XxxService` | `ConsumeAccountService` |
| Service实现 | `XxxServiceImpl` | `ConsumeAccountServiceImpl` |
| Manager | `XxxManager` | `ConsumeAccountManager` |
| DAO | `XxxDao` + `@Mapper` | `ConsumeAccountDao` |
| Entity | `XxxEntity` | `ConsumeAccountEntity` |
| Form | `XxxAddForm`, `XxxUpdateForm` | `ConsumeAccountAddForm` |
| VO | `XxxVO`, `XxxDetailVO` | `ConsumeAccountVO` |

---

## 🏗️ 架构规范详解

### 1. Entity设计规范

**黄金法则**:
- ✅ Entity≤200行（理想标准）
- ⚠️ Entity≤400行（可接受上限）
- ❌ Entity>400行（必须拆分）

**设计原则**:
1. **纯数据模型**: Entity只包含数据字段，不包含业务逻辑
2. **合理字段数**: 建议≤30个字段，超过需考虑拆分
3. **单一职责**: 一个Entity对应一个核心业务概念
4. **表名规范**: 统一使用`t_consume_*`格式

**标准模板**:
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_transaction")
public class ConsumeTransactionEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("transaction_no")
    private String transactionNo;

    // 严格控制在30个字段以内
    // 继承BaseEntity获取审计字段
}
```

### 2. Manager层规范

**核心要求**:
- ✅ **纯Java类**: 不使用任何Spring注解
- ✅ **构造函数注入**: 通过构造函数注入依赖
- ✅ **业务逻辑编排**: 复杂业务逻辑在Manager层实现
- ✅ **异常处理**: 统一异常抛出，使用自定义异常类

**标准模板**:
```java
@Slf4j
@Component  // 仅在配置类中注册为Bean
public class ConsumeAccountManager {

    private final ConsumeAccountDao consumeAccountDao;
    private final ConsumeTransactionDao consumeTransactionDao;

    // 构造函数注入依赖
    public ConsumeAccountManager(ConsumeAccountDao consumeAccountDao,
                               ConsumeTransactionDao consumeTransactionDao) {
        this.consumeAccountDao = consumeAccountDao;
        this.consumeTransactionDao = consumeTransactionDao;
    }

    /**
     * 账户充值处理
     * 包含完整的业务逻辑：验证→计算→更新→记录
     */
    public Boolean rechargeAccount(Long accountId, ConsumeAccountRechargeForm rechargeForm) {
        // 完整的业务逻辑实现
    }
}
```

**Bean注册规范**:
```java
@Configuration
public class ConsumeManagerConfiguration {

    @Bean
    @ConditionalOnMissingBean(ConsumeAccountManager.class)
    public ConsumeAccountManager consumeAccountManager(
            ConsumeAccountDao consumeAccountDao,
            ConsumeTransactionDao consumeTransactionDao) {
        return new ConsumeAccountManager(consumeAccountDao, consumeTransactionDao);
    }
}
```

### 3. DAO层规范

**注解规范**:
- ✅ 使用`@Mapper`注解，禁止使用`@Repository`
- ✅ 继承`BaseMapper<T>`获得基础CRUD能力
- ✅ 复杂查询使用`@Param`注解

**标准模板**:
```java
@Mapper
public interface ConsumeAccountDao extends BaseMapper<ConsumeAccountVO> {

    ConsumeAccountVO selectByUserId(@Param("userId") Long userId);

    PageResult<ConsumeAccountVO> queryPage(@Param("queryForm") ConsumeAccountQueryForm queryForm);

    int updateBalance(@Param("accountId") Long accountId, @Param("newBalance") BigDecimal newBalance);
}
```

---

## 🚨 异常处理统一规范

### 1. 异常分类体系

**自定义异常类层次**:
```
ConsumeBusinessException (消费业务异常基类)
├── ConsumeAccountException (账户异常)
├── ConsumeTransactionException (交易异常)
└── ConsumeDeviceException (设备异常)
```

### 2. 错误码规范

**错误码分配**:
| 错误码范围 | 类型 | 示例 | 说明 |
|-----------|------|------|------|
| 4000-4099 | 账户相关 | 4001-账户不存在 | 精确到具体异常 |
| 4100-4199 | 交易相关 | 4101-交易不存在 | 按业务模块分配 |
| 4200-4299 | 设备相关 | 4201-设备离线 | 明确错误来源 |
| 4300-4399 | 验证相关 | 4301-参数验证失败 | 参数和验证类 |
| 500 | 系统异常 | 500-系统繁忙 | 通用系统错误 |

### 3. 统一异常处理器

**标准模板**:
```java
@RestControllerAdvice
@Slf4j
@Hidden
public class ConsumeExceptionHandler {

    @ExceptionHandler(ConsumeBusinessException.class)
    public ResponseDTO<Void> handleConsumeBusinessException(ConsumeBusinessException e) {
        log.warn("[消费业务异常] code={}, message={}, details={}",
                 e.getCode(), e.getMessage(), e.getDetails());

        return ResponseDTO.error(e.getCode(), e.getMessage())
                .put("timestamp", System.currentTimeMillis())
                .put("details", e.getDetails())
                .put("errorType", "BUSINESS_ERROR");
    }
}
```

### 4. 响应格式统一标准

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {...},
  "timestamp": 1734787200000,
  "serverTime": "2025-12-21T10:00:00"
}
```

**失败响应**:
```json
{
  "code": 4001,
  "message": "账户不存在",
  "data": null,
  "timestamp": 1734787200000,
  "errorType": "BUSINESS_ERROR",
  "details": {
    "accountId": 1001
  }
}
```

---

## 🔧 事务管理规范

### 1. 事务边界原则

**Service层负责事务**:
- ✅ Controller: 不处理事务，只负责请求响应
- ✅ Service: 使用`@Transactional`控制事务边界
- ✅ Manager: 不处理事务，专注业务逻辑
- ✅ DAO: 数据访问，不涉及事务

### 2. 事务传播机制

**标准配置**:
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeAccountServiceImpl implements ConsumeAccountService {

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Boolean rechargeAccount(Long accountId, ConsumeAccountRechargeForm rechargeForm) {
        // 事务内的业务逻辑
    }
}
```

### 3. 并发控制策略

**乐观锁机制**:
```java
// Entity中添加版本号字段
@Version
private Integer version;

// 更新时自动检查版本号
int updateCount = consumeAccountDao.updateBalance(accountId, newBalance, version);
```

**分布式锁**:
```java
// 高并发场景使用分布式锁
@Scheduled(fixedRate = 30000) // 每30秒检查一次
public void processPendingTransactions() {
    String lockKey = "consume:transaction:process";
    if (redisLockService.tryLock(lockKey, 30)) {
        try {
            // 业务处理
        } finally {
            redisLockService.unlock(lockKey);
        }
    }
}
```

---

## 📊 监控日志规范

### 1. 日志级别使用规范

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| ERROR | 系统错误、异常捕获 | 数据库连接失败、事务回滚 |
| WARN | 警告信息、潜在问题 | 账户余额不足、设备离线 |
| INFO | 业务关键节点 | 交易创建、充值完成 |
| DEBUG | 调试信息 | 方法入参、中间结果 |

### 2. 日志格式标准

**统一格式**:
```
[模块] 操作类型, 关键信息={值}, 详细信息={值}
```

**示例**:
```java
// 成功日志
log.info("[账户管理] 充值成功, accountId={}, amount={}, newBalance={}",
         accountId, rechargeForm.getAmount(), newBalance);

// 警告日志
log.warn("[交易管理] 余额不足, accountId={}, balance={}, amount={}",
         accountId, account.getBalance(), amount);

// 错误日志
log.error("[交易管理] 交易执行失败, transactionRequest={}", transactionRequest, e);
```

### 3. 监控指标规范

**关键业务指标**:
- 交易成功率: `consume.transaction.success.rate`
- 交易响应时间: `consume.transaction.response.time`
- 账户余额准确率: `consume.account.balance.accuracy`
- 设备在线率: `consume.device.online.rate`

**系统性能指标**:
- 接口QPS: `consume.api.qps`
- 接口响应时间: `consume.api.response.time`
- 数据库连接池: `consume.db.connection.pool`
- 缓存命中率: `consume.cache.hit.rate`

---

## 🎨 API设计规范

### 1. RESTful API标准

**URL设计**:
```
基础路径: /api/v1/consume

GET    /api/v1/consume/account/{id}          # 获取账户
POST   /api/v1/consume/account                 # 创建账户
PUT    /api/v1/consume/account/{id}          # 更新账户
DELETE /api/v1/consume/account/{id}          # 删除账户
POST   /api/v1/consume/transaction/execute   # 执行交易 ⭐核心API
```

### 2. 参数验证规范

**Controller层验证**:
```java
@PostMapping("/recharge")
@Operation(summary = "账户充值")
public ResponseDTO<Void> rechargeAccount(
        @Parameter(description = "账户ID", required = true) @PathVariable Long accountId,
        @Valid @RequestBody ConsumeAccountRechargeForm rechargeForm) {
    // 参数验证由框架自动完成
}
```

**Form层验证**:
```java
@Data
public class ConsumeAccountRechargeForm {

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Digits(integer = 8, fraction = 2, message = "充值金额格式不正确")
    private BigDecimal amount;

    @NotBlank(message = "充值类型不能为空")
    @Size(max = 20, message = "充值类型长度不能超过20")
    private String rechargeType;
}
```

### 3. 文档规范

**Swagger注解标准**:
```java
@RestController
@Tag(name = "消费账户管理", description = "消费账户管理、余额查询、充值等功能")
public class ConsumeAccountController {

    @PostMapping("/recharge")
    @Operation(summary = "账户充值", description = "为消费账户进行充值操作")
    public ResponseDTO<Void> rechargeAccount(
            @Parameter(description = "账户ID", required = true) @PathVariable Long accountId,
            @Valid @RequestBody ConsumeAccountRechargeForm rechargeForm) {
        // 实现
    }
}
```

---

## 🏆 质量门禁标准

### 1. 代码质量检查

**强制检查项**:
- [ ] 无@Repository注解，必须使用@Mapper
- [ ] Manager类无Spring注解，纯Java实现
- [ ] Entity行数≤200行，字段数≤30个
- [ ] 所有Controller方法有完整的Swagger注解
- [ ] 所有公共方法有JavaDoc注释
- [ ] 异常处理遵循统一规范

### 2. 性能要求标准

**响应时间要求**:
- 账户查询: < 100ms
- 交易执行: < 200ms
- 充值操作: < 300ms
- 统计查询: < 500ms

**并发能力要求**:
- 支持并发: 1000+ TPS
- 数据库连接池: 20+ 连接
- 线程池配置: 核心线程8，最大线程16

### 3. 安全要求标准

**数据安全**:
- 敏感数据加密存储
- API接口权限验证
- 交易日志完整记录
- 资金操作审计跟踪

**系统安全**:
- SQL注入防护
- XSS攻击防护
- CSRF攻击防护
- 接口访问频率限制

---

## 📚 开发规范检查清单

### 代码提交前检查

**架构合规性**:
- [ ] 遵循四层架构: Controller→Service→Manager→DAO
- [ ] Manager类为纯Java，无Spring注解
- [ ] DAO使用@Mapper注解，无@Repository
- [ ] Entity继承BaseEntity，行数≤200行
- [ ] 包结构符合规范标准

**代码质量**:
- [ ] 方法命名符合规范
- [ ] 异常处理使用自定义异常类
- [ ] 日志记录使用统一格式
- [ ] 事务边界在Service层控制
- [ ] 参数验证使用标准注解

**API设计**:
- [ ] RESTful URL设计规范
- [ ] 统一响应格式
- [ ] 完整的Swagger文档
- [ ] 错误码按规范分配
- [ ] 支持API版本控制

### 代码审查要点

**业务逻辑正确性**:
- [ ] 资金操作原子性保证
- [ ] 并发场景数据一致性
- [ ] 异常情况处理完整
- [ ] 业务规则实现正确

**系统性能优化**:
- [ ] 数据库查询优化
- [ ] 缓存策略合理
- [ ] 并发控制有效
- [ ] 资源使用合理

**安全防护措施**:
- [ ] 输入参数验证
- [ ] 权限控制完整
- [ ] 敏感信息保护
- [ ] 审计日志完整

---

## 🔄 实施保障机制

### 1. 技术保障

**代码检查工具**:
- Checkstyle: 代码规范检查
- PMD: 代码质量检查
- SonarQube: 代码安全检查
- JaCoCo: 测试覆盖率检查

**持续集成**:
- Git Pre-commit Hooks
- 自动化代码检查
- 单元测试执行
- 集成测试验证

### 2. 流程保障

**开发流程**:
1. 需求分析 → 技术设计
2. 编码实现 → 自测验证
3. 代码审查 → 集成测试
4. 部署发布 → 监控验证

**质量门禁**:
- 代码规范检查: 必须100%通过
- 单元测试覆盖率: 必须>80%
- 集成测试通过率: 必须100%
- 性能测试达标: 必须满足要求

---

## 📝 规范执行说明

### 1. 强制执行条款

**必须遵守**:
- 四层架构规范
- Manager层纯Java实现
- Entity设计规范
- 异常处理统一规范
- API设计统一标准

**严重后果**:
- ❌ 代码审查不通过
- ❌ 持续集成失败
- ❌ 无法合并到主分支
- ❌ 影响绩效考核

### 2. 推荐执行条款

**建议遵循**:
- 性能优化建议
- 监控日志规范
- 安全防护措施
- 测试覆盖要求

**优化效果**:
- ✅ 提升代码质量
- ✅ 增强系统性能
- ✅ 保障系统安全
- ✅ 提高维护效率

---

**👥 制定团队**: IOE-DREAM架构委员会
**✅ 执行监督**: 技术质量保障小组
**📅 生效日期**: 2025-12-21
**🔄 更新频率**: 每季度评审更新