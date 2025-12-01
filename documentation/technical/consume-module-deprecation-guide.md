# 消费模块架构废弃指南

## ⚠️ 重要提示

**ConsumeModeEngine体系已废弃，请勿使用！**

## 废弃的架构体系

### ConsumeModeEngine体系（已废弃）

以下接口和类已标记为`@Deprecated`，**禁止在新代码中使用**：

#### 接口
- `net.lab1024.sa.admin.module.consume.engine.ConsumeModeEngine` ❌

#### 实现类（全部废弃）
- `OrderingConsumeEngine` ❌ → 使用 `OrderingMode` ✅
- `SmartConsumeEngine` ❌ → 使用 `SmartMode` ✅
- `ProductConsumeEngine` ❌ → 使用 `ProductMode` ✅
- `MeteringConsumeEngine` ❌ → 使用 `MeteringMode` ✅
- `FreeAmountConsumeEngine` ❌ → 使用 `FreeAmountMode` ✅
- `FixedAmountConsumeEngine` ❌ → 使用 `FixedAmountMode` ✅

#### 数据模型（已废弃）
- `ConsumeRequestDTO` ❌ → 使用 `ConsumeRequest` ✅
- `ConsumeResultDTO` ❌ → 使用 `ConsumeResult` ✅
- `ConsumeValidationResult` ❌ → 使用 `ConsumptionMode.ValidationResult` ✅
- `ConsumeModeConfig` ❌ → 使用 `ConsumptionModeConfig` ✅

## 推荐使用的架构体系

### ConsumptionModeEngine体系（推荐使用）✅

#### 核心接口
- `net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionMode` ✅
- `net.lab1024.sa.admin.module.consume.engine.mode.strategy.ConsumptionModeStrategy` ✅

#### 引擎管理器
- `net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionModeEngine` ✅

#### 实现类（推荐使用）
- `OrderingMode` (extends AbstractConsumptionMode) ✅
- `SmartMode` ✅
- `ProductMode` ✅
- `MeteringMode` ✅
- `FreeAmountMode` ✅
- `FixedAmountMode` ✅
- `StandardConsumptionMode` ✅

#### 数据模型（推荐使用）
- `ConsumeRequest` ✅
- `ConsumeResult` ✅
- `ConsumptionMode.ValidationResult` ✅
- `ConsumptionModeConfig` ✅

## 迁移指南

### 1. 接口迁移

```java
// ❌ 废弃方式
@Resource
private ConsumeModeEngine consumeModeEngine;

// ✅ 推荐方式
@Resource
private ConsumptionModeEngine modeEngine;
```

### 2. 实现类迁移

```java
// ❌ 废弃方式
@Component
public class MyConsumeEngine implements ConsumeModeEngine {
    // ...
}

// ✅ 推荐方式
@Component
public class MyConsumeMode extends AbstractConsumptionMode {
    public MyConsumeMode() {
        super("MY_MODE", "我的消费模式", "描述");
    }
    // ...
}
```

### 3. 数据模型迁移

```java
// ❌ 废弃方式
ConsumeRequestDTO request = new ConsumeRequestDTO();
ConsumeResultDTO result = engine.processConsume(request);

// ✅ 推荐方式
ConsumeRequest request = new ConsumeRequest();
ConsumeResult result = modeEngine.executeModeProcessing(request);
```

## 废弃原因

1. **实际使用情况**：项目实际使用的是ConsumptionModeEngine体系（见`ConsumptionModeController`）
2. **架构完整性**：ConsumptionModeEngine体系更完整，支持策略模式和配置管理
3. **代码冗余**：两套架构功能重复，造成维护困难
4. **规范遵循**：ConsumptionModeEngine体系更符合repowiki规范

## 检查清单

在编写新代码前，请确认：

- [ ] 未使用`ConsumeModeEngine`接口
- [ ] 未使用`*ConsumeEngine`实现类
- [ ] 未使用`ConsumeRequestDTO`和`ConsumeResultDTO`
- [ ] 使用`ConsumptionModeEngine`体系
- [ ] 使用`ConsumeRequest`和`ConsumeResult`
- [ ] 使用`AbstractConsumptionMode`作为基类

## 违规后果

- ❌ 使用废弃接口/类将导致代码审查不通过
- ❌ 新功能必须使用推荐架构
- ❌ 旧代码迁移时需同步更新

## 相关文档

- [消费模块架构分析](consume-module-architecture-analysis.md)
- [开发规范文档](../repowiki/zh/content/开发规范体系/Java编码规范.md)

