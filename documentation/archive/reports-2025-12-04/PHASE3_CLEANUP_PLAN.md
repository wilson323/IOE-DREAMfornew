# Phase 3 清理执行计划

**执行日期**: 2025-12-04
**策略**: 快速路径 - 直接删除旧代码

---

## 🎯 删除清单

### 第一批：旧策略实现类（5个）✅
理由：已有适配器提供相同功能，且存在语法错误

1. ✅ FixedValueConsumeStrategy.java
2. ✅ ProductConsumeStrategy.java (有语法错误)
3. ✅ MeteringConsumeStrategy.java
4. ✅ IntelligentConsumeStrategy.java
5. ✅ HybridConsumeStrategy.java

### 第二批：适配器类（5个）
理由：旧实现类删除后，适配器无用

1. ⏳ FixedValueConsumeStrategyAdapter.java
2. ⏳ ProductConsumeStrategyAdapter.java
3. ⏳ MeteringConsumeStrategyAdapter.java
4. ⏳ IntelligentConsumeStrategyAdapter.java
5. ⏳ HybridConsumeStrategyAdapter.java

### 第三批：废弃类型定义
1. ⏳ ConsumeRequestVO.java
2. ⏳ enumtype/ConsumeModeEnum.java
3. ⏳ ConsumeStrategy.java

### 第四批：冗余管理器
1. ⏳ ConsumeStrategyManager.java

---

## ⚠️ 注意事项

1. **检查引用**: 删除前确认无其他引用
2. **编译验证**: 每批删除后验证编译
3. **功能测试**: 确保 ConsumptionModeEngineManager 功能正常

---

## ✅ 保留的代码

### 核心组件（保留）
- ✅ ConsumptionModeStrategy 接口
- ✅ ConsumptionModeEngineManager
- ✅ ConsumeRequestDTO
- ✅ domain.enums.ConsumeModeEnum
- ✅ ConsumeRequestConverter（可能在未来删除）
- ✅ ConsumeModeEnumConverter（可能在未来删除）

---

**执行顺序**: 按批次逐步删除，每批后验证编译

