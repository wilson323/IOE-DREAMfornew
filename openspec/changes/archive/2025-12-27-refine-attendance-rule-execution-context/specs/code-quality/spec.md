# code-quality Specification (Draft)

## Purpose
定义IOE-DREAM项目的代码质量标准和测试覆盖率要求，确保企业级代码质量和可维护性。

## ADDED Requirements

### Requirement: Lombok Annotations for Model Classes
系统 MUST 使用Lombok注解减少样板代码，提升代码可维护性。

#### Scenario: Data class uses @Builder annotation
- **GIVEN** 一个模型类需要Builder模式
- **WHEN** 开发者添加 `@Builder` 注解
- **THEN** Lombok SHALL 自动生成Builder类，包含所有字段的setter方法
- **AND** 开发者不需要手动编写Builder代码

#### Scenario: Data class provides default values for collections
- **GIVEN** 一个模型类包含集合字段（如 List、Map）
- **WHEN** 开发者使用 `@Builder.Default` 注解
- **THEN** Builder SHALL 使用指定的默认值初始化集合
- **AND** 避免空指针异常

### Requirement: Type-Safe Test Contexts
系统 MUST 为测试场景提供类型安全的上下文类，避免使用Map存储测试数据。

#### Scenario: Test context uses strongly-typed fields
- **GIVEN** 测试服务需要访问测试专用字段（如 punchTime、deviceId）
- **WHEN** 测试上下文类定义这些字段为强类型（如 LocalTime、String）
- **THEN** 测试代码 SHALL 能够直接访问这些字段，无需强制类型转换
- **AND** IDE SHALL 提供自动补全和类型检查

#### Scenario: Test context converts to production context
- **GIVEN** 测试上下文包含测试专用字段
- **WHEN** 调用 `toProductionContext()` 方法
- **THEN** 系统 SHALL 将测试字段合并到 `customVariables` Map中
- **AND** 保留生产环境所需的基础字段（如 employeeId、departmentId）

### Requirement: Unit Test Coverage
系统 MUST 确保核心业务逻辑的单元测试覆盖率≥80%，关键路径达到100%。

#### Scenario: Service layer methods have unit tests
- **GIVEN** 一个Service类包含业务逻辑方法
- **WHEN** 开发者编写单元测试
- **THEN** 每个公共方法 SHALL 至少有一个测试用例
- **AND** 测试覆盖率 SHALL ≥80%
- **AND** 关键业务逻辑（如支付、授权）覆盖率 SHALL =100%

#### Scenario: Builder pattern is tested
- **GIVEN** 一个模型类使用 `@Builder` 注解
- **WHEN** 编写单元测试
- **THEN** 测试 SHALL 验证所有字段能够正确设置
- **AND** 测试 SHALL 验证默认值（`@Builder.Default`）正确应用
- **AND** 测试 SHALL 验证Builder生成的对象非null（如果使用了@NonNull）

### Requirement: Backward Compatibility
代码重构 MUST 保持向后兼容，不影响现有功能。

#### Scenario: Refactored builder maintains existing methods
- **GIVEN** 现有代码使用手动Builder的方法
- **WHEN** 重构为使用Lombok `@Builder` 注解
- **THEN** 所有公共方法签名 SHALL 保持不变
- **AND** 别名方法（如 `getUserId()`）SHALL 继续工作
- **AND** 现有测试 SHALL 无需修改即可通过

#### Scenario: Alias methods provide compatibility
- **GIVEN** 模型类字段重命名（如 employeeId → userId）
- **WHEN** 保留旧方法作为别名
- **THEN** 旧代码 SHALL 继续使用别名方法访问字段
- **AND** 新代码 SHOULD 使用标准字段名

### Requirement: Code Quality Standards
所有代码 MUST 遵循CLAUDE.md中定义的编码规范。

#### Scenario: Model class uses Jakarta EE annotations
- **GIVEN** 一个模型类需要使用注解
- **WHEN** 添加注解
- **THEN** 系统 MUST 使用 `jakarta.*` 包（如 `jakarta.validation.constraints.NotNull`）
- **AND** 系统 SHALL NOT 使用过时的 `javax.*` 包

#### Scenario: Documentation is complete
- **GIVEN** 一个公共类或方法
- **WHEN** 代码审查
- **THEN** 类 SHALL 有类级JavaDoc注释
- **AND** 公共方法 SHALL 有方法级JavaDoc注释
- **AND** 复杂逻辑 SHALL 有行内注释说明

## MODIFIED Requirements
（无 - 这是新spec）

## REMOVED Requirements
（无 - 这是新spec）

## RENAMED Requirements
（无 - 这是新spec）
