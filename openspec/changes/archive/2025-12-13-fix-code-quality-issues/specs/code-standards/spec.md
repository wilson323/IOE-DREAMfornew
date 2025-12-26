## MODIFIED Requirements

### Requirement: DAO 层注解规范

DAO 层接口 MUST 使用 `@Mapper` 注解，禁止使用 `@Repository` 注解。

#### Scenario: DAO 接口定义

- **WHEN** 定义 DAO 接口
- **THEN** 必须使用 `@Mapper` 注解
- **AND** 禁止使用 `@Repository` 注解

### Requirement: 依赖注入规范

依赖注入 MUST 使用 `@Resource` 注解，禁止使用 `@Autowired` 注解。

#### Scenario: 依赖注入

- **WHEN** 注入依赖
- **THEN** 必须使用 `@Resource` 注解
- **AND** 禁止使用 `@Autowired` 注解

### Requirement: 代码清洁规范

代码 MUST 不包含未使用的 import 和字段。

#### Scenario: 代码清洁

- **WHEN** 提交代码
- **THEN** 不得包含未使用的 import
- **AND** 不得包含未使用的字段
