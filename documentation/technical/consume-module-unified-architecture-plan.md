# 消费模块统一架构方案

**创建时间**: 2025-11-18  
**目标**: 统一消费模式架构，消除冗余，完成TODO事项  
**状态**: 🟡 执行中

## 📋 执行摘要

### 架构冗余问题分析

#### 🔴 问题1：两套架构体系并存

**ConsumeModeEngine体系（已废弃）**：
- 接口：`ConsumeModeEngine`
- 实现类（6个）：`OrderingConsumeEngine`、`SmartConsumeEngine`、`MeteringConsumeEngine`、`ProductConsumeEngine`、`FixedAmountConsumeEngine`、`FreeAmountConsumeEngine`
- 数据模型：`ConsumeRequestDTO`、`ConsumeResultDTO`
- 状态：已标记`@Deprecated`，但仍有引用

**ConsumptionMode体系（正在使用）**：
- 接口：`ConsumptionMode`（`engine/mode/ConsumptionMode.java`）
- 引擎：`ConsumptionModeEngine`（`engine/mode/ConsumptionModeEngine.java`）
- 实现类（7个）：继承`AbstractConsumptionMode`
  - `OrderingMode`、`ProductMode`、`SmartMode`、`MeteringMode`、`FixedAmountMode`、`FreeAmountMode`
- 数据模型：`ConsumeRequest`、`ConsumeResult`
- 状态：✅ 正在使用（`ConsumptionModeController`）

#### 🟡 问题2：OrderingMode vs OrderingConsumeEngine功能重复

- `OrderingConsumeEngine`：788行代码，功能完整但已废弃
- `OrderingMode`：670行代码，基于新架构，部分TODO未完成
- 功能重叠度：约80%

#### 🟢 问题3：TODO事项统计

**全局TODO统计**：169个TODO标记（39个文件）

**消费模块TODO分布**：
- `OrderingConsumeEngine`：3个TODO
- `SmartConsumeEngine`：12个TODO
- `ProductConsumeEngine`：4个TODO
- `MeteringConsumeEngine`：3个TODO
- `PaymentPasswordServiceImpl`：3个TODO
- `ConsumeLimitConfigServiceImpl`：6个TODO
- `AccountSecurityServiceImpl`：22个TODO
- 其他服务：42个TODO

## 🎯 统一架构方案

### 方案：保留ConsumptionMode体系

**理由**：
1. ✅ 已被`ConsumptionModeController`实际使用
2. ✅ 架构更完整（策略模式、配置管理、工厂模式）
3. ✅ 代码更规范（遵循repowiki规范）
4. ✅ 支持扩展性更好（抽象基类、模板方法）

**行动**：
1. ✅ 确认所有`ConsumeModeEngine`实现类已标记`@Deprecated`
2. 🔄 迁移`OrderingConsumeEngine`中的业务逻辑到`OrderingMode`
3. ⏳ 完成`OrderingMode`中的TODO事项
4. ⏳ 标记所有引用`ConsumeModeEngine`的地方
5. ⏳ 逐步移除`ConsumeModeEngine`体系的使用

## 📝 执行计划

### 阶段1：架构确认和标记（进行中）

- [x] 分析两套架构的使用情况
- [x] 确认保留`ConsumptionModeEngine`体系
- [ ] 检查所有`ConsumeModeEngine`实现类是否已标记`@Deprecated`
- [ ] 在`ConsumptionModeEngineManager`中添加废弃警告

### 阶段2：OrderingMode TODO完成（待执行）

#### TODO 1：设备配置获取
**位置**：`OrderingMode.doValidateParameters()`  
**任务**：从设备配置中获取订餐模式配置  
**方案**：注入`ConsumeCacheService`或`DeviceConfigService`

#### TODO 2：商品分类支持
**位置**：`OrderingMode.isOrderingProduct()`相关  
**任务**：完善商品分类判断逻辑  
**方案**：使用`ProductDao`查询商品分类信息

#### TODO 3：每日限制验证
**位置**：`OrderingMode.doIsAllowed()`  
**任务**：实现每日订餐限额检查  
**方案**：使用`ConsumeLimitConfigService`获取限额配置

#### TODO 4：订单取消功能
**位置**：`OrderingConsumeEngine.canCancelOrder()`  
**任务**：迁移到`OrderingMode`并实现完整逻辑  
**方案**：注入`ConsumeRecordDao`实现订单查询和验证

#### TODO 5：推荐套餐功能
**位置**：`OrderingConsumeEngine.getRecommendedCombos()`  
**任务**：迁移到`OrderingMode`并完善实现  
**方案**：基于商品数据和用户偏好生成推荐

### 阶段3：其他消费模式TODO完成（待执行）

#### SmartConsumeEngine → SmartMode迁移
- [ ] 迁移智能推荐逻辑
- [ ] 实现用户偏好分析
- [ ] 实现机器学习模型训练

#### ProductConsumeEngine → ProductMode迁移
- [ ] 实现商品库存更新
- [ ] 实现商品搜索功能
- [ ] 实现分类商品查询

#### MeteringConsumeEngine → MeteringMode迁移
- [ ] 实现计量历史记录查询
- [ ] 完善计量类型单价配置

### 阶段4：服务层TODO完成（待执行）

#### PaymentPasswordServiceImpl
- [ ] 实现验证码验证逻辑
- [ ] 实现生物特征验证
- [ ] 实现设备信息查询

#### ConsumeLimitConfigServiceImpl
- [ ] 实现限额变更历史查询
- [ ] 实现全局限额配置管理
- [ ] 实现基于规则引擎的限额计算

#### AccountSecurityServiceImpl
- [ ] 实现批量冻结/解冻
- [ ] 实现自动冻结规则
- [ ] 实现安全评分系统

### 阶段5：代码清理和优化（待执行）

- [ ] 移除废弃的`ConsumeModeEngine`实现类引用
- [ ] 统一代码风格和命名规范
- [ ] 添加完整的JavaDoc注释
- [ ] 优化异常处理逻辑
- [ ] 补充单元测试

## 🔍 代码一致性检查清单

### 命名规范
- [ ] 所有类名遵循驼峰命名
- [ ] 所有方法名遵循驼峰命名
- [ ] 所有常量使用大写+下划线
- [ ] 所有包名遵循小写+点分隔

### 架构规范
- [ ] 所有消费模式继承`AbstractConsumptionMode`
- [ ] 所有服务实现接口定义的方法
- [ ] Controller只调用Service层
- [ ] Service层不直接访问DAO层

### 编码规范
- [ ] 使用`@Resource`而非`@Autowired`
- [ ] 使用`jakarta.*`而非`javax.*`
- [ ] 使用SLF4J而非`System.out`
- [ ] Entity继承`BaseEntity`

### 文档规范
- [ ] 所有公共方法有JavaDoc注释
- [ ] 所有类有类级别注释
- [ ] 所有复杂逻辑有行内注释

## 📊 进度跟踪

| 阶段 | 状态 | 完成度 | 备注 |
|------|------|--------|------|
| 阶段1：架构确认 | 🟡 进行中 | 60% | 分析完成，标记待完成 |
| 阶段2：OrderingMode TODO | ⏳ 待开始 | 0% | 5个TODO待完成 |
| 阶段3：其他模式迁移 | ⏳ 待开始 | 0% | 3个模式待迁移 |
| 阶段4：服务层TODO | ⏳ 待开始 | 0% | 22+6+3个TODO |
| 阶段5：代码清理 | ⏳ 待开始 | 0% | 全局一致性检查 |

## 🚨 风险与注意事项

### 风险1：废弃代码仍被引用
**影响**：可能导致编译错误或运行时异常  
**缓解**：先标记为废弃，再逐步迁移

### 风险2：业务逻辑迁移遗漏
**影响**：功能缺失或行为不一致  
**缓解**：详细对比新旧实现，确保功能完整迁移

### 风险3：TODO数量庞大
**影响**：完成时间较长  
**缓解**：按优先级分批完成，先完成核心功能

## 📚 相关文档

- [消费模块架构分析](./consume-module-architecture-analysis.md)
- [消费模块废弃指南](./consume-module-deprecation-guide.md)
- [开发规范](./DEV_STANDARDS.md)

