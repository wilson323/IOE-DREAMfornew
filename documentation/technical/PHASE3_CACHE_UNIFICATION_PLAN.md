# 阶段三：缓存服务统一方案

> **创建时间**: 2025-11-20  
> **目标**: 统一ConsumeCacheService和ConsumeCacheManager，消除重复功能

---

## 📊 功能重叠分析

### ConsumeCacheService (Service层)
**使用场景**: 被4个类使用
- ReportServiceImpl
- OrderingMode
- ConsumeServiceImpl
- AccountSecurityManager

**功能范围**:
- 账户信息缓存（AccountEntity）
- 账户余额缓存（BigDecimal）
- 设备配置缓存（Map<String, Object>）
- 消费统计缓存（Map<String, Object>）
- 消费配置缓存（Object）
- 通用缓存操作（getOrLoad, setCacheAsync, batchEvict等）

**技术实现**: 直接使用CacheService和RedisUtil

### ConsumeCacheManager (Manager层)
**使用场景**: 目前未被使用（只有定义）

**功能范围**:
- 员工账户余额缓存（BigDecimal）
- 员工消费记录缓存（List<ConsumeRecordEntity>）
- 消费统计缓存（Map<String, Object>）
- 消费限额管理缓存（Map<String, Object>）
- 消费限额检查（checkConsumeLimit）

**技术实现**: 继承BaseCacheManager，直接访问ConsumeRecordDao

---

## 🔍 功能重叠识别

### 完全重叠的功能
1. **账户余额缓存** - 两个类都提供余额缓存功能
2. **消费统计缓存** - 两个类都提供统计缓存功能

### 部分重叠的功能
1. **缓存管理** - ConsumeCacheService提供通用缓存操作，ConsumeCacheManager提供特定业务缓存操作

### 独特功能
**ConsumeCacheService独有**:
- 账户信息缓存（AccountEntity）
- 设备配置缓存
- 消费配置缓存
- 通用缓存操作（getOrLoad, setCacheAsync, batchEvict等）

**ConsumeCacheManager独有**:
- 员工消费记录缓存
- 消费限额管理
- 消费限额检查
- 消费统计计算（calculateMonthlyStats）

---

## 🎯 统一方案

### 方案：Service层调用Manager层（符合repowiki规范）

**原则**:
1. **ConsumeCacheManager** (Manager层) - 负责所有缓存管理的具体实现
2. **ConsumeCacheService** (Service层) - 作为Service层接口，调用ConsumeCacheManager

**实施步骤**:
1. **扩展ConsumeCacheManager功能**
   - 添加ConsumeCacheService中的独特功能（账户信息、设备配置、消费配置等）
   - 保留ConsumeCacheManager的独特功能（消费记录、限额管理等）

2. **重构ConsumeCacheService**
   - 移除直接使用CacheService和RedisUtil的代码
   - 改为注入ConsumeCacheManager
   - 所有方法改为调用ConsumeCacheManager的对应方法

3. **统一余额缓存功能**
   - 保留ConsumeCacheManager的余额缓存实现（更完整，包含数据库查询）
   - ConsumeCacheService的余额缓存方法调用ConsumeCacheManager

4. **统一统计缓存功能**
   - 保留ConsumeCacheManager的统计缓存实现（包含计算逻辑）
   - ConsumeCacheService的统计缓存方法调用ConsumeCacheManager

---

## 📋 详细实施计划

### 步骤1: 扩展ConsumeCacheManager
- [ ] 添加账户信息缓存方法（cacheAccountInfo, getCachedAccountInfo）
- [ ] 添加设备配置缓存方法（cacheDeviceConfig, getCachedDeviceConfig）
- [ ] 添加消费配置缓存方法（cacheConsumeConfig, getCachedConsumeConfig）
- [ ] 添加通用缓存操作方法（getOrLoad, setCacheAsync, batchEvict等）

### 步骤2: 重构ConsumeCacheService
- [ ] 注入ConsumeCacheManager
- [ ] 移除CacheService和RedisUtil的直接使用
- [ ] 所有方法改为调用ConsumeCacheManager

### 步骤3: 验证和测试
- [ ] 编译验证
- [ ] 功能验证
- [ ] 性能验证

---

## ⚠️ 注意事项

1. **保持向后兼容**: ConsumeCacheService的公共方法签名不变
2. **性能考虑**: Manager层的缓存实现可能更高效（继承BaseCacheManager）
3. **功能完整性**: 确保所有功能都迁移到Manager层

---

**状态**: 待执行  
**优先级**: P1

