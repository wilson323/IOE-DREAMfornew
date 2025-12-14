# 微服务整合规格

## ADDED Requirements

### Requirement: 七微服务架构约束
系统 SHALL 严格限制为8个核心微服务（含网关），禁止创建额外的微服务。

#### Scenario: 微服务数量验证
- **WHEN** 检查项目微服务数量
- **THEN** 有效微服务数量 SHALL 等于 8
- **AND** 微服务列表 SHALL 包含：
  - ioedream-gateway-service (8080)
  - ioedream-common-service (8088)
  - ioedream-device-comm-service (8087)
  - ioedream-access-service (8090)
  - ioedream-attendance-service (8091)
  - ioedream-consume-service (8094)
  - ioedream-visitor-service (8095)
  - ioedream-video-service (8092)

#### Scenario: 新微服务创建限制
- **WHEN** 尝试创建新的微服务
- **THEN** 系统 SHALL 拒绝创建
- **AND** 功能 SHALL 整合到现有微服务中

---

### Requirement: microservices-common 职责边界
microservices-common 作为公共 JAR 库，SHALL 仅包含可复用的非 Spring Bean 组件。

#### Scenario: 允许的组件类型
- **WHEN** 向 microservices-common 添加组件
- **THEN** 允许的组件类型 SHALL 包含：
  - Entity（数据实体）
  - DAO（数据访问接口，使用 @Mapper）
  - Manager（业务编排，使用 @Component）
  - Form（表单对象）
  - VO（视图对象）
  - Config（配置类）
  - Constant（常量）
  - Enum（枚举）
  - Exception（异常）
  - Util（工具类）

#### Scenario: 禁止的组件类型
- **WHEN** 尝试向 microservices-common 添加 @Service 实现类
- **THEN** 系统 SHALL 在代码审查时拒绝
- **WHEN** 尝试向 microservices-common 添加 @RestController
- **THEN** 系统 SHALL 在代码审查时拒绝
- **WHEN** 尝试向 microservices-common 添加 spring-boot-starter-web 依赖
- **THEN** 系统 SHALL 在代码审查时拒绝

---

### Requirement: ioedream-common-service 功能整合
ioedream-common-service SHALL 整合所有公共业务功能，提供统一的 API 接口。

#### Scenario: 整合的功能模块
- **WHEN** 部署 ioedream-common-service
- **THEN** 服务 SHALL 提供以下功能模块：
  - 认证授权（auth）
  - 用户管理（identity）
  - 权限管理（security）
  - 字典管理（dict）
  - 菜单管理（menu）
  - 部门管理（department）
  - 审计日志（audit）
  - 系统配置（config）
  - 通知管理（notification）
  - 任务调度（scheduler）
  - 监控告警（monitor）
  - 文件管理（file）
  - 工作流管理（workflow）

#### Scenario: API 路径兼容性
- **WHEN** 前端调用原有 API 路径
- **THEN** 请求 SHALL 被正确路由到 ioedream-common-service
- **AND** 响应格式 SHALL 保持向后兼容

---

### Requirement: 服务间调用规范
所有微服务间调用 SHALL 通过 GatewayServiceClient 进行，禁止 FeignClient 直连。

#### Scenario: 正确的服务调用方式
- **WHEN** 微服务 A 需要调用微服务 B 的功能
- **THEN** 微服务 A SHALL 使用 GatewayServiceClient
- **AND** 请求 SHALL 通过 API 网关路由

#### Scenario: 禁止的服务调用方式
- **WHEN** 代码中使用 @FeignClient 注解直连其他服务
- **THEN** 代码审查 SHALL 拒绝该提交
- **WHEN** 代码中直接访问其他服务的数据库
- **THEN** 代码审查 SHALL 拒绝该提交

---

### Requirement: 依赖注入规范
所有 Spring Bean 注入 SHALL 使用 @Resource 注解，禁止 @Autowired。

#### Scenario: 正确的依赖注入
- **WHEN** 需要注入 Spring Bean
- **THEN** 代码 SHALL 使用 @Resource 注解
- **AND** 注解 SHALL 来自 jakarta.annotation.Resource

#### Scenario: 禁止的依赖注入
- **WHEN** 代码中使用 @Autowired 注解
- **THEN** 代码审查 SHALL 拒绝该提交
- **WHEN** 代码中使用构造函数注入
- **THEN** 代码审查 SHALL 拒绝该提交

---

### Requirement: DAO 层命名规范
数据访问层 SHALL 使用 @Mapper 注解和 Dao 后缀，禁止 @Repository 和 Repository 后缀。

#### Scenario: 正确的 DAO 命名
- **WHEN** 创建数据访问接口
- **THEN** 接口名 SHALL 以 Dao 后缀结尾（如 UserDao）
- **AND** 接口 SHALL 使用 @Mapper 注解
- **AND** 接口 SHALL 继承 BaseMapper<Entity>

#### Scenario: 禁止的 DAO 命名
- **WHEN** 代码中使用 @Repository 注解
- **THEN** 代码审查 SHALL 拒绝该提交
- **WHEN** 接口名以 Repository 后缀结尾
- **THEN** 代码审查 SHALL 拒绝该提交

---

### Requirement: Jakarta EE 包名规范
所有 Java EE 相关包 SHALL 使用 jakarta.* 命名空间，禁止 javax.*。

#### Scenario: 正确的包引用
- **WHEN** 代码中需要引用 Java EE 类
- **THEN** import 语句 SHALL 使用 jakarta.* 包名
- **AND** 包含但不限于：
  - jakarta.annotation.Resource
  - jakarta.validation.Valid
  - jakarta.persistence.Entity
  - jakarta.servlet.http.HttpServletRequest

#### Scenario: 禁止的包引用
- **WHEN** 代码中使用 javax.* 包名
- **THEN** 代码审查 SHALL 拒绝该提交

---

### Requirement: 四层架构规范
所有业务代码 SHALL 遵循四层架构：Controller → Service → Manager → DAO。

#### Scenario: 正确的层级调用
- **WHEN** Controller 处理请求
- **THEN** Controller SHALL 仅调用 Service 层
- **WHEN** Service 处理业务逻辑
- **THEN** Service SHALL 调用 Manager 或 DAO 层
- **WHEN** Manager 编排复杂流程
- **THEN** Manager SHALL 调用 DAO 层

#### Scenario: 禁止的跨层调用
- **WHEN** Controller 直接调用 DAO
- **THEN** 代码审查 SHALL 拒绝该提交
- **WHEN** Controller 直接调用 Manager
- **THEN** 代码审查 SHALL 拒绝该提交

---

### Requirement: 连接池规范
所有数据库连接 SHALL 使用 Druid 连接池，禁止 HikariCP。

#### Scenario: 正确的连接池配置
- **WHEN** 配置数据库连接
- **THEN** datasource.type SHALL 为 com.alibaba.druid.pool.DruidDataSource
- **AND** 配置 SHALL 包含 Druid 监控参数

#### Scenario: 禁止的连接池配置
- **WHEN** 配置中使用 HikariCP
- **THEN** 配置审查 SHALL 拒绝该配置

---

### Requirement: 功能迁移完整性
被整合服务的所有功能 SHALL 100% 迁移到目标服务，不允许功能遗漏。

#### Scenario: 功能迁移验证
- **WHEN** 完成服务整合
- **THEN** 原服务所有 API 端点 SHALL 在目标服务中可用
- **AND** 功能对比矩阵 SHALL 显示 100% 迁移完成

#### Scenario: 功能遗漏处理
- **WHEN** 发现功能遗漏
- **THEN** 系统 SHALL 阻止服务归档
- **AND** 遗漏功能 SHALL 被补充完成

---

### Requirement: 前端兼容性
后端重构 SHALL 保持 API 接口 100% 向后兼容，前端无需修改。

#### Scenario: API 路径兼容
- **WHEN** 前端调用 API
- **THEN** 原有 API 路径 SHALL 继续可用
- **AND** 响应格式 SHALL 保持不变

#### Scenario: 网关路由配置
- **WHEN** 服务整合完成
- **THEN** 网关 SHALL 配置路由规则
- **AND** 旧路径 SHALL 透明转发到新服务

---

### Requirement: 代码质量标准
所有迁移代码 SHALL 满足企业级代码质量标准。

#### Scenario: 测试覆盖率要求
- **WHEN** 提交迁移代码
- **THEN** 单元测试覆盖率 SHALL ≥ 80%
- **AND** 核心业务逻辑覆盖率 SHALL = 100%

#### Scenario: 编译质量要求
- **WHEN** 构建项目
- **THEN** 编译错误数量 SHALL = 0
- **AND** 编译警告数量 SHALL = 0

#### Scenario: 代码规范要求
- **WHEN** 提交代码
- **THEN** 代码 SHALL 通过 checkstyle 检查
- **AND** 代码 SHALL 通过 spotbugs 检查

---

### Requirement: 服务归档规范
被整合服务 SHALL 在验证完成后归档，保留30天后可删除。

#### Scenario: 归档前验证
- **WHEN** 准备归档服务
- **THEN** 功能迁移 SHALL 100% 完成
- **AND** 测试 SHALL 100% 通过
- **AND** 文档 SHALL 100% 更新

#### Scenario: 归档操作
- **WHEN** 执行服务归档
- **THEN** 服务代码 SHALL 移动到 archive/deprecated-services/
- **AND** Git 标签 SHALL 创建备份
- **AND** 迁移说明文档 SHALL 创建

#### Scenario: 保留期限
- **WHEN** 服务归档完成
- **THEN** 归档代码 SHALL 保留30天
- **AND** 30天后 MAY 删除归档代码

