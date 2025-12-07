# 账户实体类统一迁移指南

> **迁移日期**: 2025-12-03  
> **目标**: 统一使用AccountEntity，废弃ConsumeAccountEntity  
> **预计完成时间**: 1-2周

---

## 📋 一、迁移概述

### 1.1 统一方案

**保留实体**: `AccountEntity`  
**废弃实体**: `ConsumeAccountEntity`  
**统一表名**: `t_consume_account`

### 1.2 迁移原因

1. ✅ AccountEntity使用范围更广（AccountServiceImpl、AccountDao、AccountManager等）
2. ✅ 主键类型Long更符合数据库设计规范（自增ID性能更好）
3. ✅ 表名`t_consume_account`符合项目命名规范（t_前缀）
4. ✅ AccountEntity已增强，包含ConsumeAccountEntity的所有优势功能

---

## 🔄 二、字段映射对照表

| ConsumeAccountEntity | AccountEntity | 说明 |
|---------------------|---------------|------|
| `id` (String) | `accountId` (Long) | 主键类型不同，需要转换 |
| `userId` (String) | `personId` (Long) | 字段名和类型都不同 |
| `userName` (String) | `personName` (String) | 字段名不同 |
| `accountType` (Integer) | `accountType` (String) | 类型不同，需要转换 |
| `accountStatus` (Integer) | `status` (String) | 字段名和类型都不同 |
| `cashBalance` (BigDecimal) | `cashBalance` (BigDecimal) | ✅ 已合并 |
| `subsidyBalance` (BigDecimal) | `subsidyBalance` (BigDecimal) | ✅ 已合并 |
| `frozenAmount` (BigDecimal) | `frozenAmount` (BigDecimal) | ✅ 相同 |
| `creditLimit` (BigDecimal) | `creditLimit` (BigDecimal) | ✅ 相同 |
| `totalRefundAmount` (BigDecimal) | `totalRefundAmount` (BigDecimal) | ✅ 已合并 |
| `consumeCount` (Integer) | `consumeCount` (Integer) | ✅ 已合并 |
| `passwordStatus` (Integer) | `passwordStatus` (Integer) | ✅ 已合并 |
| `passwordSalt` (String) | `passwordSalt` (String) | ✅ 已合并 |
| `allowOverdraft` (Boolean) | `allowOverdraft` (Boolean) | ✅ 已合并 |
| `allowSubsidyConsume` (Boolean) | `allowSubsidyConsume` (Boolean) | ✅ 已合并 |
| `allowCashConsume` (Boolean) | `allowCashConsume` (Boolean) | ✅ 已合并 |
| `singleConsumeLimit` (BigDecimal) | `singleLimit` / `singleConsumeLimit` | ✅ 已合并（别名） |
| `dailyConsumeLimit` (BigDecimal) | `dailyLimit` / `dailyConsumeLimit` | ✅ 已合并（别名） |
| `monthlyConsumeLimit` (BigDecimal) | `monthlyLimit` / `monthlyConsumeLimit` | ✅ 已合并（别名） |
| `todayConsumeAmount` (BigDecimal) | `currentDailyAmount` / `todayConsumeAmount` | ✅ 已合并（别名） |
| `monthlyConsumeAmount` (BigDecimal) | `currentMonthlyAmount` | ✅ 已合并 |
| `accountLevel` (Integer) | `accountLevel` (String) | 类型不同，需要转换 |
| `pointsBalance` (Integer) | `points` / `pointsBalance` | ✅ 已合并（别名） |
| `totalPointsEarned` (Integer) | `totalPointsEarned` (Integer) | ✅ 已合并 |
| `totalPointsUsed` (Integer) | `totalPointsUsed` (Integer) | ✅ 已合并 |
| `bindPhone` (String) | `phoneNumber` (String) | 字段名不同 |
| `bindEmail` (String) | `email` (String) | 字段名不同 |
| `bindDeviceIds` (String) | `bindDeviceIds` (String) | ✅ 已合并 |
| `validPeriodStart` (String) | `validPeriodStart` (String) | ✅ 已合并 |
| `validPeriodEnd` (String) | `validPeriodEnd` (String) | ✅ 已合并 |
| `autoRenewalStatus` (Integer) | `autoRenewalStatus` (Integer) | ✅ 已合并 |
| `riskLevel` (Integer) | `riskLevel` (Integer) | ✅ 已合并 |
| `enableTransactionNotification` (Boolean) | `enableTransactionNotification` (Boolean) | ✅ 已合并 |
| `notificationConfig` (String) | `notificationConfig` (String) | ✅ 已合并 |
| `enableAbnormalDetection` (Integer) | `enableAbnormalDetection` (Integer) | ✅ 已合并 |
| `abnormalDetectionRules` (String) | `abnormalDetectionRules` (String) | ✅ 已合并 |
| `extendAttrs` (String) | `extendData` / `extendAttrs` | ✅ 已合并（别名） |

---

## 🔧 三、类型转换工具方法

### 3.1 账户类型转换

```java
/**
 * 将ConsumeAccountEntity的Integer账户类型转换为AccountEntity的String类型
 */
public static String convertAccountType(Integer accountType) {
    if (accountType == null) {
        return "STAFF";
    }
    switch (accountType) {
        case 1: // 现金账户
            return "STAFF";
        case 2: // 补贴账户
            return "STAFF";
        case 3: // 临时账户
            return "TEMP";
        case 4: // 员工账户
            return "STAFF";
        case 5: // 访客账户
            return "VISITOR";
        default:
            return "STAFF";
    }
}

/**
 * 将AccountEntity的String账户类型转换为ConsumeAccountEntity的Integer类型
 */
public static Integer convertAccountTypeToInteger(String accountType) {
    if (accountType == null) {
        return 1;
    }
    switch (accountType) {
        case "STAFF":
            return 4; // 员工账户
        case "STUDENT":
            return 1; // 现金账户
        case "VISITOR":
            return 5; // 访客账户
        case "TEMP":
            return 3; // 临时账户
        default:
            return 1;
    }
}
```

### 3.2 账户状态转换

```java
/**
 * 将ConsumeAccountEntity的Integer账户状态转换为AccountEntity的String类型
 */
public static String convertAccountStatus(Integer accountStatus) {
    if (accountStatus == null) {
        return "ACTIVE";
    }
    switch (accountStatus) {
        case 1: // 正常
            return "ACTIVE";
        case 2: // 冻结
            return "FROZEN";
        case 3: // 注销
            return "CLOSED";
        case 4: // 挂失
            return "SUSPENDED";
        case 5: // 锁定
            return "FROZEN";
        default:
            return "ACTIVE";
    }
}

/**
 * 将AccountEntity的String账户状态转换为ConsumeAccountEntity的Integer类型
 */
public static Integer convertAccountStatusToInteger(String status) {
    if (status == null) {
        return 1;
    }
    switch (status) {
        case "ACTIVE":
            return 1; // 正常
        case "FROZEN":
            return 2; // 冻结
        case "CLOSED":
            return 3; // 注销
        case "SUSPENDED":
            return 4; // 挂起
        default:
            return 1;
    }
}
```

---

## 📝 四、迁移步骤

### 步骤1：更新ConsumeAccountManager（优先级：高）

**文件**: `ConsumeAccountManager.java`

**操作**:
1. 将`ConsumeAccountEntity`改为`AccountEntity`
2. 更新字段引用（id -> accountId, userId -> personId等）
3. 添加类型转换逻辑
4. 更新方法调用

**示例**:
```java
// 修改前
@Resource
private ConsumeAccountDao consumeAccountDao;

public ConsumeAccountEntity createAccount(ConsumeAccountEntity account) {
    account.setId(UUID.randomUUID().toString());
    // ...
}

// 修改后
@Resource
private AccountDao accountDao;

public AccountEntity createAccount(AccountEntity account) {
    // accountId由数据库自增生成，无需设置
    // ...
}
```

---

### 步骤2：更新ConsumeAccountDao（优先级：高）

**文件**: `ConsumeAccountDao.java`

**操作**:
1. 将泛型参数改为`AccountEntity`
2. 更新表名映射（consume_account -> t_consume_account）
3. 更新字段映射（id -> accountId等）
4. 更新查询条件

**示例**:
```java
// 修改前
@Mapper
public interface ConsumeAccountDao extends BaseMapper<ConsumeAccountEntity> {
    default ConsumeAccountEntity selectByAccountNo(String accountNo) {
        LambdaQueryWrapper<ConsumeAccountEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsumeAccountEntity::getAccountNo, accountNo);
        return selectOne(wrapper);
    }
}

// 修改后
@Mapper
public interface ConsumeAccountDao extends BaseMapper<AccountEntity> {
    default AccountEntity selectByAccountNo(String accountNo) {
        LambdaQueryWrapper<AccountEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountEntity::getAccountNo, accountNo);
        return selectOne(wrapper);
    }
}
```

---

### 步骤3：合并AccountDao和ConsumeAccountDao（优先级：中）

**操作**:
1. 将ConsumeAccountDao的方法迁移到AccountDao
2. 更新所有引用ConsumeAccountDao的地方改为AccountDao
3. 废弃ConsumeAccountDao

---

### 步骤4：更新其他引用（优先级：中）

**涉及文件**:
- `ConsumeSubsidyManager.java`
- `TransactionManagementManager.java`
- 其他使用ConsumeAccountEntity的地方

---

### 步骤5：数据库迁移（优先级：低）

**注意**: 如果数据库表结构不同，需要数据迁移脚本

**检查项**:
1. 表名是否一致（consume_account vs t_consume_account）
2. 主键类型是否一致（String vs Long）
3. 字段是否一致

---

## ⚠️ 五、注意事项

### 5.1 向后兼容

- ✅ AccountEntity保留了`balance`字段（标记为@Deprecated）
- ✅ AccountEntity提供了字段别名（singleConsumeLimit等）
- ✅ AccountEntity的业务方法支持向后兼容

### 5.2 数据迁移

- ⚠️ 如果数据库表不同，需要数据迁移脚本
- ⚠️ 主键类型不同（String UUID vs Long AUTO），需要特殊处理
- ⚠️ 字段类型不同（Integer vs String），需要转换逻辑

### 5.3 测试验证

- ✅ 单元测试更新
- ✅ 集成测试更新
- ✅ 功能验证

---

## 📊 六、迁移进度跟踪

| 步骤 | 文件/模块 | 状态 | 完成时间 |
|------|----------|------|----------|
| 1 | AccountEntity增强 | ✅ 已完成 | 2025-12-03 |
| 2 | ConsumeAccountEntity废弃标记 | ✅ 已完成 | 2025-12-03 |
| 3 | ConsumeAccountManager迁移 | ⏳ 待开始 | - |
| 4 | ConsumeAccountDao迁移 | ⏳ 待开始 | - |
| 5 | AccountDao和ConsumeAccountDao合并 | ⏳ 待开始 | - |
| 6 | 其他引用更新 | ⏳ 待开始 | - |
| 7 | 数据库迁移（如需要） | ⏳ 待开始 | - |
| 8 | 测试验证 | ⏳ 待开始 | - |

---

## ✅ 七、验收标准

1. ✅ AccountEntity包含ConsumeAccountEntity的所有功能
2. ✅ ConsumeAccountEntity已标记为@Deprecated
3. ✅ 所有Manager和DAO统一使用AccountEntity
4. ✅ 所有单元测试通过
5. ✅ 所有集成测试通过
6. ✅ 代码编译无错误
7. ✅ 功能验证通过

---

**迁移指南创建时间**: 2025-12-03  
**预计完成时间**: 1-2周  
**负责人**: AI Assistant

