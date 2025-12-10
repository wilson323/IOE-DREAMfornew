# 代码质量重构指南

**日期**: 2025-01-30
**版本**: v1.0.0
**目标**: 通过重构提升代码质量，降低复杂度，减少重复

---

## 📊 代码质量现状

### 高复杂度方法识别

基于代码审查，识别出以下高复杂度方法（需要重构）：

| 方法位置 | 方法名 | 预估复杂度 | 问题描述 | 优先级 |
|---------|--------|-----------|---------|--------|
| `ConsumeServiceImpl.executeTransaction` | executeTransaction | 8-10 | 多层嵌套if-else | P0 |
| `ConsumeServiceImpl.executeConsume` | executeConsume | 7-9 | 复杂条件判断 | P0 |
| `AccountServiceImpl.pageAccounts` | pageAccounts | 6-8 | 多个条件分支 | P1 |
| `DefaultFixedAmountCalculator.calculate` | calculate | 7-9 | 多步骤处理逻辑 | P1 |

### 重复代码模式识别

#### 1. 账户验证逻辑（重复3+次）

**位置**: `AccountServiceImpl`, `ConsumeServiceImpl`, `PaymentServiceImpl`

**重复代码**:
```java
AccountEntity account = accountDao.selectById(accountId);
if (account == null) {
    return ResponseDTO.error("ACCOUNT_NOT_FOUND", "账户不存在");
}
if (account.getStatus() != 1) {
    return ResponseDTO.error("ACCOUNT_INVALID", "账户状态无效");
}
```

**重构方案**: 提取为`AccountValidator`工具类

#### 2. 余额验证逻辑（重复5+次）

**位置**: 多个Service方法

**重复代码**:
```java
if (account.getBalance().compareTo(amount) < 0) {
    return ResponseDTO.error("INSUFFICIENT_BALANCE", "余额不足");
}
```

**重构方案**: 提取为`BalanceValidator`工具类

#### 3. Entity转VO转换逻辑（重复10+次）

**位置**: 多个Service方法

**重复代码**:
```java
AccountVO vo = new AccountVO();
vo.setAccountId(entity.getAccountId());
vo.setUserId(entity.getUserId());
// ... 20+行转换代码
```

**重构方案**: 使用MapStruct或提取为Converter工具类

---

## 🔧 重构实施方案

### 阶段1: 提取公共验证方法（优先级P0）

#### 1.1 创建AccountValidator工具类

**文件路径**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/consume/util/AccountValidator.java`

**功能**:
- 验证账户是否存在
- 验证账户状态是否有效
- 验证账户余额是否充足
- 验证账户是否被冻结

**示例代码**:
```java
public class AccountValidator {
    
    public static ResponseDTO<AccountEntity> validateAccountExists(AccountDao accountDao, Long accountId) {
        AccountEntity account = accountDao.selectById(accountId);
        if (account == null) {
            return ResponseDTO.error("ACCOUNT_NOT_FOUND", "账户不存在");
        }
        return ResponseDTO.ok(account);
    }
    
    public static ResponseDTO<Void> validateAccountStatus(AccountEntity account) {
        if (account.getStatus() != 1) {
            return ResponseDTO.error("ACCOUNT_INVALID", "账户状态无效");
        }
        return ResponseDTO.ok();
    }
    
    public static ResponseDTO<Void> validateBalanceSufficient(AccountEntity account, BigDecimal amount) {
        BigDecimal availableBalance = account.getBalance().subtract(
            account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO
        );
        if (availableBalance.compareTo(amount) < 0) {
            return ResponseDTO.error("INSUFFICIENT_BALANCE", "余额不足，可用余额: " + availableBalance);
        }
        return ResponseDTO.ok();
    }
}
```

#### 1.2 创建BalanceValidator工具类

**文件路径**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/consume/util/BalanceValidator.java`

**功能**:
- 验证余额是否充足
- 计算可用余额
- 验证金额是否有效

---

### 阶段2: 简化高复杂度方法（优先级P0）

#### 2.1 重构ConsumeServiceImpl.executeTransaction

**重构前**（复杂度: 8-10）:
```java
public ConsumeTransactionResultVO executeTransaction(ConsumeTransactionForm form) {
    if (form.getUserId() == null) {
        throw new IllegalArgumentException("用户ID不能为空");
    }
    if (form.getDeviceId() == null) {
        throw new IllegalArgumentException("设备ID不能为空");
    }
    if (form.getAreaId() == null) {
        throw new IllegalArgumentException("区域ID不能为空");
    }
    
    ResponseDTO<?> response = consumeExecutionManager.executeConsumption(form);
    if (response == null || !response.isSuccess()) {
        // 错误处理
    }
    
    ConsumeTransactionEntity transaction = null;
    if (form.getTransactionNo() != null) {
        transaction = consumeTransactionDao.selectByTransactionNo(form.getTransactionNo());
    }
    
    ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
    if (transaction != null) {
        // 构建结果
    }
    // ...
}
```

**重构后**（复杂度: ≤5）:
```java
public ConsumeTransactionResultVO executeTransaction(ConsumeTransactionForm form) {
    // 1. 参数验证（提取为独立方法）
    validateTransactionForm(form);
    
    // 2. 执行消费流程
    ResponseDTO<?> response = consumeExecutionManager.executeConsumption(form);
    if (!isResponseSuccess(response)) {
        return buildFailureResult(response);
    }
    
    // 3. 查询交易记录（提取为独立方法）
    ConsumeTransactionEntity transaction = queryTransaction(form.getTransactionNo());
    
    // 4. 构建返回结果（提取为独立方法）
    return buildTransactionResult(transaction);
}

private void validateTransactionForm(ConsumeTransactionForm form) {
    if (form.getUserId() == null) {
        throw new IllegalArgumentException("用户ID不能为空");
    }
    if (form.getDeviceId() == null) {
        throw new IllegalArgumentException("设备ID不能为空");
    }
    if (form.getAreaId() == null) {
        throw new IllegalArgumentException("区域ID不能为空");
    }
}

private boolean isResponseSuccess(ResponseDTO<?> response) {
    return response != null && response.isSuccess();
}

private ConsumeTransactionEntity queryTransaction(String transactionNo) {
    if (transactionNo == null) {
        return null;
    }
    return consumeTransactionDao.selectByTransactionNo(transactionNo);
}

private ConsumeTransactionResultVO buildTransactionResult(ConsumeTransactionEntity transaction) {
    ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
    if (transaction != null) {
        result.setTransactionNo(transaction.getTransactionNo());
        result.setAmount(transaction.getConsumeAmount());
        result.setTransactionStatus(transaction.getStatus());
    }
    return result;
}
```

---

### 阶段3: 提取公共转换方法（优先级P1）

#### 3.1 创建Converter工具类

**文件路径**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/consume/util/AccountConverter.java`

**功能**:
- Entity转VO
- Form转Entity
- 批量转换

---

## 📋 重构检查清单

### 重构前检查

- [ ] 确保所有测试通过
- [ ] 备份当前代码
- [ ] 确认重构范围

### 重构后验证

- [ ] 所有测试通过
- [ ] 代码编译无错误
- [ ] 功能验证正常
- [ ] 代码复杂度降低
- [ ] 重复代码减少

---

## 🎯 重构目标

| 指标 | 重构前 | 目标值 | 优先级 |
|------|--------|--------|--------|
| **平均圈复杂度** | 待分析 | ≤5 | P0 |
| **最大圈复杂度** | 待分析 | ≤10 | P0 |
| **代码重复度** | 待分析 | ≤3% | P1 |
| **方法平均长度** | 待分析 | ≤30行 | P1 |

---

**负责人**: IOE-DREAM架构团队
**审核状态**: 待开始
**预计完成时间**: 2025-02-06

