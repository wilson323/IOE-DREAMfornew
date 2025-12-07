# ConsumeAccountManager迁移计划

> **迁移日期**: 2025-12-03  
> **目标**: 将ConsumeAccountManager迁移到使用AccountEntity和AccountDao  
> **预计完成时间**: 2-3天

---

## 📋 一、迁移策略

### 策略选择：直接修改ConsumeAccountManager

**原因**:
1. ConsumeAccountManager功能完整，直接修改比新建更高效
2. 使用AccountEntityConverter进行类型转换
3. 逐步迁移，保持向后兼容

---

## 🔄 二、关键变更点

### 2.1 主键类型变更

| 原类型 | 新类型 | 处理方式 |
|--------|--------|----------|
| `String accountId` | `Long accountId` | 使用AccountEntityConverter转换 |
| `String userId` | `Long personId` | 使用AccountEntityConverter转换 |

### 2.2 字段类型变更

| 原字段 | 新字段 | 处理方式 |
|--------|--------|----------|
| `Integer accountType` | `String accountType` | 使用转换方法 |
| `Integer accountStatus` | `String status` | 使用转换方法 |

### 2.3 DAO方法变更

| 原方法 | 新方法 | 处理方式 |
|--------|--------|----------|
| `ConsumeAccountDao.selectById(String)` | `AccountDao.selectById(Long)` | 类型转换 |
| `ConsumeAccountDao.selectByUserId(String)` | `AccountDao.selectByUserId(Long)` | 类型转换 |

---

## 📝 三、迁移步骤

### 步骤1：更新依赖注入（已完成）

- ✅ 创建AccountEntityConverter工具类
- ⏳ 更新ConsumeAccountManager的DAO依赖
- ⏳ 更新方法签名

### 步骤2：迁移核心方法（进行中）

**优先级：高**
- ⏳ `createAccount()` - 创建账户
- ⏳ `updateBalance()` - 更新余额
- ⏳ `freezeAmount()` / `unfreezeAmount()` - 冻结/解冻金额
- ⏳ `freezeAccount()` / `unfreezeAccount()` - 冻结/解冻账户

### 步骤3：迁移查询方法

**优先级：中**
- ⏳ `getAccountByNo()` - 根据账户号查询
- ⏳ `getUserAccounts()` - 获取用户账户列表
- ⏳ `getUserAccountByType()` - 获取指定类型账户
- ⏳ `getUserMainAccount()` - 获取主账户

### 步骤4：迁移统计方法

**优先级：中**
- ⏳ `getAccountStats()` - 账户统计
- ⏳ `getUserAccountSummary()` - 用户账户汇总

### 步骤5：迁移验证方法

**优先级：低**
- ⏳ `validateAccountForConsume()` - 验证账户可消费性
- ⏳ `validateConsumeLimits()` - 验证消费限制

### 步骤6：迁移其他方法

**优先级：低**
- ⏳ `getLowBalanceAccounts()` - 低余额账户
- ⏳ `getDormantAccounts()` - 休眠账户
- ⏳ `getHighRiskAccounts()` - 高风险账户
- ⏳ `updateAccountPoints()` - 更新积分
- ⏳ `setPaymentPassword()` - 设置支付密码

---

## ⚠️ 四、注意事项

### 4.1 主键处理

- ⚠️ AccountEntity使用Long AUTO，创建时不需要设置accountId
- ⚠️ ConsumeAccountEntity使用String UUID，需要生成UUID
- ✅ 使用AccountEntityConverter处理转换

### 4.2 缓存键处理

- ⚠️ 缓存键可能需要调整（userId类型变化）
- ✅ 保持缓存键格式一致，使用String类型

### 4.3 向后兼容

- ✅ 保留ConsumeAccountEntity的转换方法
- ✅ 逐步迁移，不一次性替换

---

## 📊 五、迁移进度

| 步骤 | 方法 | 状态 | 完成时间 |
|------|------|------|----------|
| 1 | AccountEntityConverter创建 | ✅ 已完成 | 2025-12-03 |
| 2 | createAccount() | ⏳ 进行中 | - |
| 2 | updateBalance() | ⏳ 待开始 | - |
| 2 | freezeAmount() | ⏳ 待开始 | - |
| 3 | getAccountByNo() | ⏳ 待开始 | - |
| 3 | getUserAccounts() | ⏳ 待开始 | - |
| 4 | getAccountStats() | ⏳ 待开始 | - |
| 5 | validateAccountForConsume() | ⏳ 待开始 | - |
| 6 | 其他方法 | ⏳ 待开始 | - |

**总体进度**: 10% (1/10步骤完成)

---

**迁移计划创建时间**: 2025-12-03  
**预计完成时间**: 2-3天

