# 消费模块完整企业级实现 - 技术设计

## 元数据

- **Change ID**: `consume-module-complete-implementation`
- **设计版本**: v1.0
- **创建时间**: 2025-12-23
- **设计师**: IOE-DREAM 架构团队

## 架构概览

### 当前架构（简化版）

```
┌─────────────────────────────────────┐
│   ioedream-consume-service (8094)   │
├─────────────────────────────────────┤
│  ConsumeController                  │
│    ↓                                │
│  ConsumeService                     │
│    ↓                                │
│  ConsumeManager                     │
│    ↓                                │
│  ConsumeDao                         │
│    ↓                                │
│  t_consume_account                  │
│  t_consume_record                   │
│  t_consume_account_transaction       │
└─────────────────────────────────────┘

问题：
- 简化版消费逻辑（只支持固定金额）
- 缺少消费模式支持
- 缺少补贴账户体系
- 缺少SAGA事务支持
```

### 目标架构（企业级）

```
┌─────────────────────────────────────────────────────────────┐
│              ioedream-consume-service (8094)                 │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ConsumeController (13个)                          │   │
│  │    - ConsumeAccountController                        │   │
│  │    - ConsumeTransactionController                   │   │
│  │    - ConsumeReportController (12 reports)          │   │
│  │    - ConsumeStatisticsController (14 statistics)    │   │
│  │    - ConsumeMobileController (25+ mobile APIs)     │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Service Layer                                     │   │
│  │    - ConsumeService (核心业务逻辑)                 │   │
│  │    - AccountKindService (账户类别管理)             │   │
│  │    - AreaService (区域管理)                        │   │
│  │    - MealService (餐别管理)                        │   │
│  │    - SubsidyService (补贴管理)                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Manager Layer (业务编排)                          │   │
│  │    - ConsumeModeManager (消费模式编排)              │   │
│  │    - ConsumeTransactionManager (交易编排)          │   │
│  │    - SubsidyDeductionManager (补贴扣款编排)        │   │
│  │    - SagaTransactionManager (SAGA编排)            │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  DAO Layer                                         │   │
│  │    - ConsumeAccountDao (POSID_ACCOUNT)            │   │
│  │    - ConsumeTransactionDao (POSID_TRANSACTION)     │   │
│  │    - AccountKindDao (POSID_ACCOUNTKIND)            │   │
│  │    - AreaDao (POSID_AREA)                          │   │
│  │    - MealDao (POSID_MEAL)                          │   │
│  │    - SubsidyAccountDao (POSID_SUBSIDY_ACCOUNT)     │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Database (MySQL 8.0)                              │   │
│  │    - POSID_ACCOUNT (账户表)                         │   │
│  │    - POSID_ACCOUNTKIND (账户类别表) ⭐              │   │
│  │    - POSID_AREA (区域表) ⭐                         │   │
│  │    - POSID_MEAL (餐别表) ⭐                         │   │
│  │    - POSID_MEAL_CATEGORY (餐别分类表) ⭐            │   │
│  │    - POSID_TRANSACTION (交易表，按月分区) ⭐         │   │
│  │    - POSID_SUBSIDY_TYPE (补贴类型表) ⭐             │   │
│  │    - POSID_SUBSIDY_ACCOUNT (补贴账户表) ⭐           │   │
│  │    - POSID_SUBSIDY_FLOW (补贴流水表) ⭐              │   │
│  │    - POSID_RECHARGE_ORDER (充值订单表) ⭐            │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Cache Layer (Redis)                                │   │
│  │    - account:balance:{accountId}                   │   │
│  │    - accountkind:full:{id}                        │   │
│  │    - area:info:{areaId}                            │   │
│  │    - subsidy:list:{accountId}                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## 数据库设计

### 核心表结构

#### 1. POSID_ACCOUNTKIND（账户类别表）⭐ 核心表

```sql
CREATE TABLE POSID_ACCOUNTKIND (
    id VARCHAR(50) PRIMARY KEY COMMENT '账户类别ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '类别编号',
    name VARCHAR(100) NOT NULL COMMENT '类别名称',

    -- ⭐ 消费模式配置（JSON字段）
    mode_config JSON COMMENT '各模式参数配置',

    -- ⭐ 区域权限配置（JSON字段）
    area_config JSON COMMENT '区域权限配置',

    -- 折扣配置
    discount_type INT COMMENT '折扣类型',
    discount INT COMMENT '折扣值',

    -- 限额配置
    date_max_money INT COMMENT '每日限额(分)',
    date_max_count INT COMMENT '每日限次',
    month_max_money INT COMMENT '每月限额(分)',
    month_max_count INT COMMENT '每月限次',

    -- 业务配置
    order_meal BOOLEAN DEFAULT FALSE COMMENT '必须订餐',
    is_attendance_consume BOOLEAN DEFAULT FALSE COMMENT '考勤消费',

    -- 通用字段
    available BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_code(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户类别表';
```

**mode_config JSON结构**：
```json
{
  "FIXED_AMOUNT": {
    "enabled": true,
    "subType": "SECTION",
    "values": [
      {"key": "breakfast", "amount": 8.00, "time": "06:00-09:00"},
      {"key": "lunch", "amount": 15.00, "time": "11:00-13:30"}
    ]
  },
  "FREE_AMOUNT": {
    "enabled": true,
    "minAmount": 1,
    "maxAmount": 100000
  },
  "PRODUCT": {
    "enabled": true,
    "maxItemsPerTransaction": 50
  }
}
```

**area_config JSON结构**：
```json
[
  {
    "areaId": "main_campus",
    "includeSubAreas": true
  },
  {
    "areaId": "canteen_1",
    "includeSubAreas": false
  }
]
```

#### 2. POSID_AREA（区域表）⭐ 核心表

```sql
CREATE TABLE POSID_AREA (
    id VARCHAR(50) PRIMARY KEY COMMENT '区域ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '区域编号',
    name VARCHAR(100) NOT NULL COMMENT '区域名称',
    parent_id VARCHAR(50) COMMENT '父区域ID',
    level INT DEFAULT 1 COMMENT '层级',
    type INT DEFAULT 1 COMMENT '类型(1-餐饮 2-商店 3-办公 4-医疗)',

    -- ⭐ 经营模式（核心字段）
    manage_mode INT COMMENT '经营模式(1-餐别制 2-超市制 3-混合)',

    -- ⭐ 定值配置（JSON字段）
    fixed_value_config TEXT COMMENT '定值配置JSON',

    -- 餐厅属性
    order_meal_mode INT COMMENT '订餐模式',
    order_payment_type INT COMMENT '订餐付费类型',
    inventory_flag BOOLEAN DEFAULT FALSE COMMENT '是否启用进销存',
    meal_categories TEXT COMMENT '可用餐别分类JSON',
    business_hours TEXT COMMENT '营业时间JSON',

    available BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_parent(parent_id),
    INDEX idx_type(type, available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一区域管理表';
```

**fixed_value_config JSON结构**：
```json
{
  "breakfast": {"amount": 5.00, "unit": "元"},
  "lunch": {"amount": 12.00, "unit": "元"},
  "dinner": {"amount": 10.00, "unit": "元"},
  "supper": {"amount": 8.00, "unit": "元"}
}
```

#### 3. POSID_MEAL_CATEGORY（餐别分类表）⭐ 核心表

```sql
CREATE TABLE POSID_MEAL_CATEGORY (
    id VARCHAR(50) PRIMARY KEY COMMENT '分类ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '分类编号',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    area_id VARCHAR(50) COMMENT '关联区域ID',
    sort_order INT DEFAULT 0,
    description VARCHAR(255),
    available BOOLEAN DEFAULT TRUE,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_code(code),
    INDEX idx_area(area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐别分类表';
```

#### 4. POSID_MEAL（餐别表）⭐ 核心表

```sql
CREATE TABLE POSID_MEAL (
    id VARCHAR(50) PRIMARY KEY COMMENT '餐别ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '餐别编号',
    name VARCHAR(100) NOT NULL COMMENT '餐别名称',
    category_id VARCHAR(50) COMMENT '所属分类ID',
    start_time VARCHAR(10) COMMENT '开始时间',
    end_time VARCHAR(10) COMMENT '结束时间',
    base_price INT COMMENT '基础价格(分)',
    price_rules TEXT COMMENT '价格规则JSON',
    sort_order INT DEFAULT 0,
    available BOOLEAN DEFAULT TRUE,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_category(category_id, available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐别表';
```

#### 5. POSID_TRANSACTION（交易表）⭐ 核心表（按月分区）

```sql
CREATE TABLE POSID_TRANSACTION (
    id VARCHAR(50) PRIMARY KEY,
    transaction_no VARCHAR(32) UNIQUE COMMENT '交易流水号',

    -- 人员信息
    person_id VARCHAR(50) NOT NULL,
    person_name VARCHAR(100),
    dept_id VARCHAR(50),

    -- 账户信息
    account_id VARCHAR(50) NOT NULL,
    account_kind_id VARCHAR(50),
    is_attendance_consume BOOLEAN DEFAULT FALSE COMMENT '是否考勤消费',

    -- 区域信息
    area_id VARCHAR(50) NOT NULL,
    area_name VARCHAR(100),
    area_manage_mode INT COMMENT '区域经营模式',

    -- 餐别信息
    meal_id VARCHAR(50),
    meal_category_id VARCHAR(50),
    meal_name VARCHAR(100),

    -- 设备信息
    device_id VARCHAR(50),
    device_name VARCHAR(100),

    -- 消费金额(分)
    consume_money INT NOT NULL COMMENT '消费金额',
    discount_money INT DEFAULT 0 COMMENT '折扣金额',
    final_money INT NOT NULL COMMENT '实际支付金额',

    -- 账户余额变化
    balance_before INT COMMENT '消费前余额',
    balance_after INT COMMENT '消费后余额',
    allowance_used INT DEFAULT 0 COMMENT '使用补贴金额',
    cash_used INT DEFAULT 0 COMMENT '使用现金金额',

    -- 消费模式
    consume_mode VARCHAR(20) NOT NULL COMMENT '消费模式',
    consume_type VARCHAR(20) COMMENT '消费类型',

    -- 时间信息
    consume_time DATETIME NOT NULL COMMENT '消费时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    status VARCHAR(20) DEFAULT 'SUCCESS',

    INDEX idx_account(account_id, consume_time),
    INDEX idx_person(person_id, consume_time),
    INDEX idx_area(area_id, consume_time),
    INDEX idx_time(consume_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费交易表'
PARTITION BY RANGE (TO_DAYS(consume_time)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01'))
);
```

#### 6. POSID_SUBSIDY_TYPE（补贴类型表）⭐ 核心表

```sql
CREATE TABLE POSID_SUBSIDY_TYPE (
    id VARCHAR(50) PRIMARY KEY COMMENT '补贴类型ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '类型编码',
    name VARCHAR(100) NOT NULL COMMENT '类型名称',
    priority INT DEFAULT 10 COMMENT '使用优先级',
    clear_strategy VARCHAR(20) COMMENT '清零策略',
    validity_days INT COMMENT '有效期天数',
    allow_transfer BOOLEAN DEFAULT FALSE COMMENT '允许转换',
    allow_refund BOOLEAN DEFAULT FALSE COMMENT '允许退回',
    use_scope JSON COMMENT '使用范围',
    available BOOLEAN DEFAULT TRUE,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_code(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补贴类型表';
```

#### 7. POSID_SUBSIDY_ACCOUNT（补贴账户表）⭐ 核心表

```sql
CREATE TABLE POSID_SUBSIDY_ACCOUNT (
    id VARCHAR(50) PRIMARY KEY COMMENT '补贴账户ID',
    account_id VARCHAR(50) NOT NULL COMMENT '关联账户',
    subsidy_type_id VARCHAR(50) NOT NULL COMMENT '补贴类型',
    balance INT DEFAULT 0 COMMENT '补贴余额(分)',
    expire_date DATE COMMENT '过期日期',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_account(account_id),
    INDEX idx_type(subsidy_type_id),
    INDEX idx_expire(expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补贴账户表';
```

### 表命名规范

**强制要求**：所有消费模块表使用`POSID_`前缀

**理由**：
1. 业务文档明确要求
2. 与现有表命名区分
3. 避免与其他业务模块冲突

**迁移策略**：
1. 新表使用`POSID_`前缀
2. 旧表`t_consume_*`保留作为备份
3. 提供3个月过渡期

## 业务逻辑设计

### 1. 6种消费模式实现

#### 1.1 消费模式架构

```java
/**
 * 消费模式策略接口
 */
public interface ConsumeModeStrategy {
    /**
     * 计算消费金额
     */
    ConsumeCalculationResult calculate(ConsumeContext context);

    /**
     * 验证消费条件
     */
    void validate(ConsumeContext context);

    /**
     * 获取消费模式
     */
    String getMode();
}

/**
 * 消费上下文
 */
@Data
public class ConsumeContext {
    private Long accountId;
    private Long areaId;
    private String deviceId;
    private String mealId;
    private Map<String, Object> params;
}

/**
 * 消费计算结果
 */
@Data
public class ConsumeCalculationResult {
    private BigDecimal consumeAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String mode;
    private String modeDetail;
}
```

#### 1.2 6种消费模式实现

**模式1: FIXED_AMOUNT（固定金额模式）**

```java
@Component
public class FixedAmountModeStrategy implements ConsumeModeStrategy {

    @Override
    public ConsumeCalculationResult calculate(ConsumeContext context) {
        // 1. 读取账户类别配置
        AccountKindEntity accountKind = getAccountKind(context.getAccountId());
        JSONObject modeConfig = accountKind.getModeConfig();

        // 2. 读取区域配置
        AreaEntity area = getArea(context.getAreaId());
        JSONObject fixedValueConfig = area.getFixedValueConfig();

        // 3. 确定使用的定值（优先级：账户类别 > 区域默认 > 系统全局）
        BigDecimal fixedAmount = determineFixedValue(context, modeConfig, fixedValueConfig);

        // 4. 应用折扣
        BigDecimal discountAmount = applyDiscount(accountKind, fixedAmount);
        BigDecimal finalAmount = fixedAmount.subtract(discountAmount);

        return ConsumeCalculationResult.builder()
            .consumeAmount(fixedAmount)
            .discountAmount(discountAmount)
            .finalAmount(finalAmount)
            .mode("FIXED_AMOUNT")
            .build();
    }

    private BigDecimal determineFixedValue(ConsumeContext context,
                                           JSONObject modeConfig,
                                           JSONObject fixedValueConfig) {
        // 优先级1: 账户类别mode_config中的定值配置
        if (modeConfig != null && modeConfig.containsKey("FIXED_AMOUNT")) {
            JSONObject fixedAmountConfig = modeConfig.getJSONObject("FIXED_AMOUNT");
            if (fixedAmountConfig.containsKey("values")) {
                JSONArray values = fixedAmountConfig.getJSONArray("values");
                // 根据当前餐别匹配定值
                for (int i = 0; i < values.size(); i++) {
                    JSONObject value = values.getJSONObject(i);
                    if (value.getString("key").equals(context.getMealId())) {
                        return value.getBigDecimal("amount");
                    }
                }
            }
        }

        // 优先级2: 区域fixed_value_config中的默认定值
        if (fixedValueConfig != null && fixedValueConfig.containsKey(context.getMealId())) {
            JSONObject value = fixedValueConfig.getJSONObject(context.getMealId());
            return value.getBigDecimal("amount");
        }

        // 优先级3: 系统全局默认值
        return getSystemDefaultFixedValue(context.getMealId());
    }
}
```

**模式2: FREE_AMOUNT（自由金额模式）**

```java
@Component
public class FreeAmountModeStrategy implements ConsumeModeStrategy {

    @Override
    public ConsumeCalculationResult calculate(ConsumeContext context) {
        // 1. 从上下文获取用户输入的金额
        BigDecimal inputAmount = (BigDecimal) context.getParams().get("amount");

        // 2. 验证金额范围
        AccountKindEntity accountKind = getAccountKind(context.getAccountId());
        JSONObject modeConfig = accountKind.getModeConfig();
        JSONObject freeAmountConfig = modeConfig.getJSONObject("FREE_AMOUNT");

        BigDecimal minAmount = freeAmountConfig.getBigDecimal("minAmount");
        BigDecimal maxAmount = freeAmountConfig.getBigDecimal("maxAmount");

        if (inputAmount.compareTo(minAmount) < 0 || inputAmount.compareTo(maxAmount) > 0) {
            throw new BusinessException("金额超出范围");
        }

        // 3. 应用折扣
        BigDecimal discountAmount = applyDiscount(accountKind, inputAmount);
        BigDecimal finalAmount = inputAmount.subtract(discountAmount);

        return ConsumeCalculationResult.builder()
            .consumeAmount(inputAmount)
            .discountAmount(discountAmount)
            .finalAmount(finalAmount)
            .mode("FREE_AMOUNT")
            .build();
    }
}
```

**模式3: PRODUCT（商品模式）**

```java
@Component
public class ProductModeStrategy implements ConsumeModeStrategy {

    @Override
    public ConsumeCalculationResult calculate(ConsumeContext context) {
        // 1. 获取商品列表
        List<ProductItem> products = (List<ProductItem>) context.getParams().get("products");

        // 2. 查询商品价格
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ProductItem item : products) {
            ProductEntity product = getProduct(item.getProductId());
            BigDecimal itemAmount = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);
        }

        // 3. 应用折扣
        AccountKindEntity accountKind = getAccountKind(context.getAccountId());
        BigDecimal discountAmount = applyDiscount(accountKind, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        return ConsumeCalculationResult.builder()
            .consumeAmount(totalAmount)
            .discountAmount(discountAmount)
            .finalAmount(finalAmount)
            .mode("PRODUCT")
            .build();
    }
}
```

**模式4-6**: 略（METERED, ORDER, INTELLIGENCE）

### 2. 3种区域经营模式实现

#### 2.1 经营模式判断逻辑

```java
/**
 * 消费处理服务
 */
@Service
@Slf4j
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private ConsumeModeManager consumeModeManager;

    @Resource
    private AreaService areaService;

    /**
     * 处理消费请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResult consume(ConsumeRequest request) {
        log.info("[消费服务] 开始处理消费: accountId={}, areaId={}, deviceId={}",
            request.getAccountId(), request.getAreaId(), request.getDeviceId());

        // 1. 读取区域信息
        AreaEntity area = areaService.getById(request.getAreaId());
        Integer manageMode = area.getManageMode();

        // 2. 根据经营模式选择消费方式
        ConsumeContext context = buildContext(request, area);

        if (manageMode == 1) {
            // 餐别制：定值金额消费
            return handleMealBasedConsume(context);
        } else if (manageMode == 2) {
            // 超市制：商品扫码消费
            return handleSupermarketConsume(context);
        } else if (manageMode == 3) {
            // 混合模式：用户选择
            return handleHybridConsume(context);
        } else {
            throw new BusinessException("不支持的经营模式: " + manageMode);
        }
    }

    /**
     * 餐别制消费
     */
    private ConsumeResult handleMealBasedConsume(ConsumeContext context) {
        log.info("[消费服务] 餐别制消费: areaId={}", context.getAreaId());

        // 1. 检查当前时间是否在就餐时间段
        MealEntity currentMeal = getCurrentMeal(context);

        // 2. 使用固定金额模式计算
        context.setMealId(currentMeal.getId());
        ConsumeCalculationResult result = consumeModeManager.calculate("FIXED_AMOUNT", context);

        // 3. 执行扣款
        return executeDeduction(context, result);
    }

    /**
     * 超市制消费
     */
    private ConsumeResult handleSupermarketConsume(ConsumeContext context) {
        log.info("[消费服务] 超市制消费: areaId={}", context.getAreaId());

        // 1. 使用商品模式计算
        ConsumeCalculationResult result = consumeModeManager.calculate("PRODUCT", context);

        // 2. 执行扣款
        return executeDeduction(context, result);
    }

    /**
     * 混合模式消费
     */
    private ConsumeResult handleHybridConsume(ConsumeContext context) {
        log.info("[消费服务] 混合模式消费: areaId={}", context.getAreaId());

        // 1. 根据用户选择消费方式
        String consumeType = (String) context.getParams().get("consumeType");

        if ("FIXED".equals(consumeType)) {
            return handleMealBasedConsume(context);
        } else if ("PRODUCT".equals(consumeType)) {
            return handleSupermarketConsume(context);
        } else {
            throw new BusinessException("不支持的消费类型: " + consumeType);
        }
    }
}
```

### 3. 补贴扣款优先级实现

#### 3.1 补贴扣款编排器

```java
/**
 * 补贴扣款编排器
 */
@Component
@Slf4j
public class SubsidyDeductionManager {

    /**
     * 执行扣款（含补贴优先级）
     */
    public DeductionResult executeWithSubsidy(DeductionRequest request) {
        log.info("[补贴扣款] 开始扣款: accountId={}, amount={}",
            request.getAccountId(), request.getAmount());

        // 1. 查询可用补贴
        List<SubsidyAccountEntity> availableSubsidies = queryAvailableSubsidies(
            request.getAccountId(),
            request.getAreaId(),
            request.getMealCategoryId()
        );

        // 2. 如果没有可用补贴，直接扣现金
        if (availableSubsidies.isEmpty()) {
            return deductFromCashAccount(request);
        }

        // 3. 按优先级排序补贴
        List<SubsidyAccountEntity> sortedSubsidies = sortByPriority(availableSubsidies);

        // 4. 逐个扣除补贴
        BigDecimal remainingAmount = request.getAmount();
        List<SubsidyDeductionRecord> deductionRecords = new ArrayList<>();

        for (SubsidyAccountEntity subsidy : sortedSubsidies) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break; // 已全额扣除
            }

            // 检查使用限制
            if (!checkUsageLimit(subsidy, request)) {
                continue; // 跳过此补贴
            }

            // 计算本次可扣金额
            BigDecimal deductableAmount = remainingAmount.min(subsidy.getBalance());

            // 扣除补贴
            deductSubsidy(subsidy.getId(), deductableAmount);

            // 记录扣款明细
            deductionRecords.add(SubsidyDeductionRecord.builder()
                .subsidyAccountId(subsidy.getId())
                .subsidyTypeId(subsidy.getSubsidyTypeId())
                .amount(deductableAmount)
                .balanceBefore(subsidy.getBalance())
                .balanceAfter(subsidy.getBalance().subtract(deductableAmount))
                .build());

            // 更新剩余金额
            remainingAmount = remainingAmount.subtract(deductableAmount);
        }

        // 5. 如果补贴未完全覆盖，扣现金账户
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            DeductionResult cashResult = deductFromCashAccount(request, remainingAmount);
            deductionRecords.addAll(cashResult.getDeductionRecords());
        }

        return DeductionResult.builder()
            .totalAmount(request.getAmount())
            .subsidyAmount(request.getAmount().subtract(remainingAmount))
            .cashAmount(remainingAmount)
            .deductionRecords(deductionRecords)
            .build();
    }

    /**
     * 按优先级排序补贴
     */
    private List<SubsidyAccountEntity> sortByPriority(List<SubsidyAccountEntity> subsidies) {
        return subsidies.stream()
            .sorted(Comparator
                .comparing(SubsidyAccountEntity::getExpireDate) // 1. 有效期最近的优先
                .thenComparing(SubsidyAccountEntity::getBalance)     // 2. 金额小的优先用完
            )
            .collect(Collectors.toList());
    }

    /**
     * 检查使用限制
     */
    private boolean checkUsageLimit(SubsidyAccountEntity subsidy, DeductionRequest request) {
        SubsidyTypeEntity subsidyType = getSubsidyType(subsidy.getSubsidyTypeId());
        JSONObject useScope = subsidyType.getUseScope();

        // 检查区域限制
        if (useScope.containsKey("areaRestriction")) {
            JSONArray allowedAreas = useScope.getJSONArray("areaRestriction");
            if (!allowedAreas.contains(request.getAreaId())) {
                return false; // 区域不匹配
            }
        }

        // 检查餐别限制
        if (useScope.containsKey("mealRestriction")) {
            JSONArray allowedMeals = useScope.getJSONArray("mealRestriction");
            if (!allowedMeals.contains(request.getMealCategoryId())) {
                return false; // 餐别不匹配
            }
        }

        // 检查最低消费金额
        if (useScope.containsKey("minAmount")) {
            BigDecimal minAmount = useScope.getBigDecimal("minAmount");
            if (request.getAmount().compareTo(minAmount) < 0) {
                return false; // 未达到最低消费金额
            }
        }

        return true; // 所有限制都通过
    }
}
```

### 4. SAGA分布式事务实现

#### 4.1 SAGA事务编排器

```java
/**
 * SAGA事务编排器
 */
@Component
@Slf4j
public class SagaTransactionManager {

    @Resource
    private SagaLogDao sagaLogDao;

    /**
     * 执行SAGA事务
     */
    public <T> T execute(SagaTransaction<T> saga) {
        String sagaId = generateSagaId();
        log.info("[SAGA事务] 开始事务: sagaId={}", sagaId);

        try {
            // Forward阶段：执行5个步骤
            Object step1Result = executeStep(saga, sagaId, "step1", saga::step1);
            Object step2Result = executeStep(saga, sagaId, "step2", saga::step2, step1Result);
            Object step3Result = executeStep(saga, sagaId, "step3", saga::step3, step2Result);
            Object step4Result = executeStep(saga, sagaId, "step4", saga::step4, step3Result);
            Object step5Result = executeStep(saga, sagaId, "step5", saga::step5, step4Result);

            log.info("[SAGA事务] 所有步骤成功: sagaId={}", sagaId);
            return (T) step5Result;

        } catch (Exception e) {
            log.error("[SAGA事务] 事务失败，开始补偿: sagaId={}", sagaId, e);

            // Compensate阶段：逆向补偿
            compensate(saga, sagaId);

            throw new SystemException("SAGA_TRANSACTION_FAILED", "事务执行失败", e);
        }
    }

    /**
     * 执行单个步骤
     */
    private Object executeStep(SagaTransaction saga, String sagaId, String stepName,
                                SagaStep step, Object... params) {
        String sagaLogId = generateSagaLogId();

        try {
            log.info("[SAGA事务] 执行步骤: sagaId={}, step={}", sagaId, stepName);

            // 记录步骤开始
            sagaLogDao.insert(SagaLogEntity.builder()
                .sagaId(sagaId)
                .transactionId(saga.getTransactionId())
                .stepName(stepName)
                .stepStatus("PENDING")
                .requestData(JSON.toJSONString(params))
                .executeTime(LocalDateTime.now())
                .build());

            // 执行步骤
            Object result = step.execute(params);

            // 记录步骤成功
            sagaLogDao.updateStatus(sagaLogId, "SUCCESS", JSON.toJSONString(result), null);

            return result;

        } catch (Exception e) {
            // 记录步骤失败
            sagaLogDao.updateStatus(sagaLogId, "FAILED", null, e.getMessage());

            throw e;
        }
    }

    /**
     * 补偿事务
     */
    private void compensate(SagaTransaction saga, String sagaId) {
        log.info("[SAGA事务] 开始补偿: sagaId={}", sagaId);

        // 查询已成功的步骤（倒序）
        List<SagaLogEntity> completedSteps = sagaLogDao.queryCompletedSteps(sagaId);

        // 逆向补偿
        for (int i = completedSteps.size() - 1; i >= 0; i--) {
            SagaLogEntity step = completedSteps.get(i);

            try {
                log.info("[SAGA事务] 补偿步骤: sagaId={}, step={}", sagaId, step.getStepName());

                // 执行补偿逻辑
                saga.compensate(step.getStepName(), step.getResponseData());

                // 更新状态为已补偿
                sagaLogDao.updateStatus(step.getId(), "COMPENSATED", null, null);

            } catch (Exception e) {
                log.error("[SAGA事务] 补偿失败: sagaId={}, step={}", sagaId, step.getStepName(), e);
                // 继续补偿下一步
            }
        }

        log.info("[SAGA事务] 补偿完成: sagaId={}", sagaId);
    }
}
```

#### 4.2 消费SAGA事务定义

```java
/**
 * 消费SAGA事务
 */
public class ConsumeSagaTransaction implements SagaTransaction<ConsumeResult> {

    @Override
    public String getTransactionId() {
        return "consume-" + UUID.randomUUID().toString();
    }

    /**
     * Step1: 权限验证
     */
    @Override
    public Object step1(Object... params) {
        ConsumeRequest request = (ConsumeRequest) params[0];

        // 验证账户状态
        // 验证区域权限
        // 验证餐别权限

        return "权限验证通过";
    }

    /**
     * Step2: 锁定余额
     */
    @Override
    public Object step2(Object... params) {
        ConsumeRequest request = (ConsumeRequest) params[0];
        String step1Result = (String) params[1];

        // 使用Redis分布式锁锁定账户
        // 设置锁超时时间（5秒）

        return "余额锁定成功";
    }

    /**
     * Step3: 扣除余额
     */
    @Override
    public Object step3(Object... params) {
        ConsumeRequest request = (ConsumeRequest) params[0];

        // 执行补贴扣款（优先补贴，后现金）
        DeductionResult deductionResult = subsidyDeductionManager.executeWithSubsidy(request);

        return deductionResult;
    }

    /**
     * Step4: 记录交易
     */
    @Override
    public Object step4(Object... params) {
        DeductionResult deductionResult = (DeductionResult) params[2];

        // 写入POSID_TRANSACTION表
        // 写入POSID_SUBSIDY_FLOW表

        return "交易记录成功";
    }

    /**
     * Step5: 更新统计
     */
    @Override
    public Object step5(Object... params) {
        // 更新今日消费次数
        // 更新今日消费金额
        // 更新统计数据

        return "统计更新成功";
    }

    /**
     * 补偿方法
     */
    @Override
    public void compensate(String stepName, String responseData) {
        switch (stepName) {
            case "step5":
                // 无需补偿（统计更新失败不影响主流程）
                break;
            case "step4":
                // 删除交易记录
                // 删除补贴流水记录
                break;
            case "step3":
                // 退还余额（恢复补贴和现金）
                // 释放分布式锁
                break;
            case "step2":
                // 释放分布式锁
                break;
            case "step1":
                // 无需补偿
                break;
        }
    }
}
```

### 5. 缓存策略实现

#### 5.1 多级缓存配置

```java
/**
 * 缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 账户余额缓存（实时更新）
     */
    @Bean
    public Cache accountBalanceCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }

    /**
     * 账户类别配置缓存
     */
    @Bean
    public Cache accountKindConfigCache() {
        return Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    }

    /**
     * 区域详情缓存（含餐别配置）
     */
    @Bean
    public Cache areaInfoCache() {
        return Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
    }

    /**
     * 区域权限校验结果缓存
     */
    @Bean
    public Cache areaPermissionCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
    }

    /**
     * 账户补贴列表缓存
     */
    @Bean
    public Cache subsidyListCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();
    }
}
```

#### 5.2 缓存使用示例

```java
/**
 * 账户类别服务（含缓存）
 */
@Service
@Slf4j
public class AccountKindServiceImpl implements AccountKindService {

    @Resource
    private AccountKindDao accountKindDao;

    @Resource
    private Cache accountKindConfigCache;

    /**
     * 获取账户类别配置
     */
    @Override
    @Cacheable(value = "accountKind:config", key = "#id")
    public AccountKindEntity getConfig(String id) {
        log.debug("[账户类别服务] 查询配置: accountKindId={}", id);
        return accountKindDao.selectById(id);
    }

    /**
     * 更新配置后删除缓存
     */
    @Override
    @CacheEvict(value = "accountKind:config", key = "#id")
    public void updateConfig(String id, AccountKindUpdateForm form) {
        // 更新数据库
        // 缓存自动删除（@CacheEvict）
    }
}
```

### 6. 性能优化实现

#### 6.1 批量处理优化

```java
/**
 * 批量消费处理器
 */
@Component
@Slf4j
public class BatchConsumeProcessor {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 批量处理消费请求
     */
    public List<ConsumeResult> batchProcess(List<ConsumeRequest> requests) {
        log.info("[批量消费] 开始批量处理: 数量={}", requests.size());

        // 1. 将请求加入队列
        String queueKey = "consume:batch:" + System.currentTimeMillis();
        redisTemplate.opsForList().rightPushAll(queueKey, requests.toArray());

        // 2. 后台批量处理
        return processBatch(queueKey);
    }

    /**
     * 处理一个批次
     */
    @Async
    private List<ConsumeResult> processBatch(String queueKey) {
        List<ConsumeResult> results = new ArrayList<>();

        // 每批处理50个
        while (true) {
            List<ConsumeRequest> batch = redisTemplate.opsForList().leftPop(queueKey, 50);

            if (batch == null || batch.isEmpty()) {
                break;
            }

            // 批量验证权限
            batchValidatePermission(batch);

            // 批量扣款
            List<ConsumeResult> batchResults = batchDeduct(batch);

            // 批量写入交易记录
            batchInsertTransactions(batchResults);

            results.addAll(batchResults);
        }

        return results;
    }
}
```

#### 6.2 分布式锁实现

```java
/**
 * 分布式锁服务
 */
@Component
@Slf4j
public class DistributedLockService {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 锁定账户
     */
    public <T> T lockAccount(String accountId, long leaseTime, TimeUnit unit,
                             Supplier<T> supplier) {
        String lockKey = "lock:account:" + accountId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁
            boolean locked = lock.tryLock(leaseTime, unit);
            if (!locked) {
                throw new BusinessException("ACCOUNT_LOCK_FAILED", "账户锁定失败，请稍后重试");
            }

            // 执行业务逻辑
            log.debug("[分布式锁] 获取锁成功: lockKey={}", lockKey);
            return supplier.get();

        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[分布式锁] 释放锁: lockKey={}", lockKey);
            }
        }
    }
}
```

## 数据迁移方案

### 迁移策略

#### 阶段1: 双写准备期（第1周）

```sql
-- 1. 创建新表（POSID_*）
-- 执行 Flyway 脚本创建所有新表

-- 2. 创建数据迁移视图
CREATE OR REPLACE VIEW v_consume_migration AS
SELECT
    'POSID_ACCOUNT' AS target_table,
    id AS new_id,
    account_no AS account_no,
    person_id,
    balance * 100 AS balance,  -- 元转分
    frozen_balance * 100 AS frozen_balance,
    STATUS,
    VERSION,
    CREATE_TIME,
    UPDATE_TIME
FROM t_consume_account;
```

#### 阶段2: 数据迁移（第2-3周）

```java
/**
 * 数据迁移服务
 */
@Component
@Slf4j
public class DataMigrationService {

    @Resource
    private AccountDao oldAccountDao;

    @Resource
    private ConsumeAccountDao newAccountDao;

    /**
     * 迁移账户数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void migrateAccounts() {
        log.info("[数据迁移] 开始迁移账户数据");

        int batchSize = 1000;
        int offset = 0;

        while (true) {
            // 读取旧表数据
            List<OldAccountEntity> oldAccounts = oldAccountDao.queryForMigration(offset, batchSize);

            if (oldAccounts.isEmpty()) {
                break;
            }

            // 转换并写入新表
            for (OldAccountEntity oldAccount : oldAccounts) {
                AccountEntity newAccount = convertToNew(oldAccount);
                newAccountDao.insert(newAccount);
            }

            offset += batchSize;
            log.info("[数据迁移] 已迁移: offset={}, batchSize={}", offset, batchSize);
        }

        log.info("[数据迁移] 账户数据迁移完成");
    }

    /**
     * 转换数据格式
     */
    private AccountEntity convertToNew(OldAccountEntity oldAccount) {
        AccountEntity newAccount = new AccountEntity();
        newAccount.setId(oldAccount.getId());
        newAccount.setAccountNo(oldAccount.getAccountNo());
        newAccount.setPersonId(oldAccount.getPersonId());
        // 转换金额单位（元转分）
        newAccount.setBalance(oldAccount.getBalance().multiply(new BigDecimal("100")));
        newAccount.setFrozenBalance(oldAccount.getFrozenBalance().multiply(new BigDecimal("100")));
        newAccount.setStatus(oldAccount.getStatus());
        newAccount.setVersion(oldAccount.getVersion());
        return newAccount;
    }
}
```

#### 阶段3: 双写验证期（第4周）

```java
/**
 * 双写验证器
 */
@Component
@Slf4j
public class DualWriteValidator {

    @Resource
    private AccountDao oldAccountDao;

    @Resource
    private ConsumeAccountDao newAccountDao;

    /**
     * 验证双写一致性
     */
    @Scheduled(cron = "0 */10 * * * ?")  // 每10分钟执行一次
    public void validateDualWrite() {
        log.info("[双写验证] 开始验证");

        // 查询最近1小时的变更记录
        List<AccountEntity> newAccounts = newAccountDao.queryRecentUpdates(Duration.ofHours(1));

        int inconsistentCount = 0;
        for (AccountEntity newAccount : newAccounts) {
            AccountEntity oldAccount = oldAccountDao.selectById(newAccount.getId());

            if (!compareAccounts(oldAccount, newAccount)) {
                log.error("[双写验证] 数据不一致: accountId={}", newAccount.getId());
                inconsistentCount++;
            }
        }

        log.info("[双写验证] 验证完成: 不一致数量={}", inconsistentCount);
    }

    /**
     * 比较账户数据
     */
    private boolean compareAccounts(AccountEntity oldAccount, AccountEntity newAccount) {
        return Objects.equals(oldAccount.getBalance(), newAccount.getBalance())
            && Objects.equals(oldAccount.getFrozenBalance(), newAccount.getFrozenBalance())
            && Objects.equals(oldAccount.getStatus(), newAccount.getStatus());
    }
}
```

#### 阶段4: 切换到新表（第5周）

```java
/**
 * 切换配置
 */
@Configuration
public class ConsumeServiceConfig {

    /**
     * 是否使用新表
     */
    @Value("${consume.use-new-tables:false}")
    private Boolean useNewTables;

    /**
     * 账户DAO（动态切换）
     */
    @Bean
    public AccountDao accountDao() {
        if (useNewTables) {
            return new ConsumeAccountDao();  // 新表DAO
        } else {
            return new OldAccountDao();  // 旧表DAO
        }
    }
}
```

## 测试策略

### 单元测试

**覆盖率要求**: ≥ 80%

```java
/**
 * 消费模式策略测试
 */
@SpringBootTest
class ConsumeModeStrategyTest {

    @Resource
    private FixedAmountModeStrategy fixedAmountModeStrategy;

    @Test
    void testFixedAmountCalculation() {
        // Given
        ConsumeContext context = buildTestContext();

        // When
        ConsumeCalculationResult result = fixedAmountModeStrategy.calculate(context);

        // Then
        assertThat(result.getMode()).isEqualTo("FIXED_AMOUNT");
        assertThat(result.getFinalAmount()).isEqualByComparingTo("15.00");
    }

    @Test
    void testPriorityOfFixedValue() {
        // 测试定值优先级：账户类别 > 区域默认 > 系统全局
    }
}
```

### 集成测试

**覆盖率要求**: ≥ 70%

```java
/**
 * 消费流程集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConsumeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Transactional
    void testCompleteConsumeFlow() throws Exception {
        // 测试完整消费流程
        mockMvc.perform(post("/api/v1/consume/transaction/quick")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"1001\",\"deviceId\":\"POS001\",\"amount\":25.50}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

### 性能测试

**目标指标**:
- TPS ≥ 1000
- 平均响应时间 ≤ 50ms
- P99响应时间 ≤ 150ms

```java
/**
 * 消费性能测试
 */
@Test
void testConsumePerformance() throws Exception {
    // 配置JMeter
    // 100并发用户
    // 持续5分钟

    // 验证结果
    assertThat(tps).isGreaterThanOrEqualTo(1000);
    assertThat(averageResponseTime).isLessThanOrEqualTo(50);
}
```

## 部署计划

### 分阶段部署

**阶段1: 数据库重构**（第1-4周）
- 在测试环境创建新表
- 执行数据迁移
- 双写验证

**阶段2: 业务逻辑重构**（第5-9周）
- 部署新版本消费服务
- 灰度发布（先10%流量）
- 逐步切换到新表

**阶段3: 性能优化**（第10-12周）
- 启用缓存策略
- 启用批量处理
- 性能压测验证

**阶段4: 完整切换**（第13周）
- 切换到新表
- 停止双写
- 删除旧表（1个月后）

### 回滚方案

**回滚触发条件**:
- 数据不一致率 > 1%
- 性能指标未达标
- 严重Bug影响业务

**回滚步骤**:
1. 切换配置使用旧表
2. 回滚代码版本
3. 分析问题原因
4. 修复后重新部署

## 监控告警

### 监控指标

**业务指标**:
- 消费TPS
- 消费成功率
- 消费平均耗时
- 余额不足次数
- SAGA补偿次数

**技术指标**:
- 数据库连接池使用率
- 缓存命中率
- JVM内存使用率
- GC频率和耗时

### 告警规则

```yaml
alerts:
  - name: 消费成功率告警
    condition: consume_success_rate < 0.95
    duration: 5m
    severity: critical

  - name: 消费响应时间告警
    condition: consume_p95_latency > 200ms
    duration: 5m
    severity: warning

  - name: SAGA补偿告警
    condition: saga_compensation_count > 10
    duration: 1m
    severity: critical
```

---

**设计版本**: v1.0
**最后更新**: 2025-12-23
**设计师**: IOE-DREAM 架构团队
