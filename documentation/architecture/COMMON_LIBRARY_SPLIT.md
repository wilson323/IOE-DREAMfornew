# microservices-common 拆分与依赖治理

## 目标

- 将 `microservices-common` 从“共享单体”收敛为“共享能力”，降低跨域耦合与变更半径。
- 通过“稳定性分层 + 依赖方向约束”保证全局一致性与可演进性。

## 当前落地（已启用）

**更新时间**: 2025-01-30  
**更新说明**: 基于方案C重构，所有细粒度模块已真实落地，microservices-common已重构为配置类容器

### 1) `microservices-common-core`（最小稳定内核）

- 放置内容：`ResponseDTO`、通用异常（`BusinessException/ParamException/SystemException`）、常量等。
- 设计原则：尽量不依赖 Spring，仅依赖 `slf4j-api` 与必要的基础库。
- 依赖方向：**只能被依赖**，禁止反向依赖任何 starter/业务模块。

### 2) 细粒度模块（已全部落地）

**第1层（最底层模块）**：

- `microservices-common-entity` - 所有实体类的统一管理模块（✅ 方案C已执行：所有实体类统一在此模块，共17个实体类）

**第2层（基础能力模块）**：

- `microservices-common-storage` - 文件存储
- `microservices-common-data` - 数据访问层（MyBatis-Plus、Druid、Flyway）
- `microservices-common-security` - 安全认证（JWT、Spring Security）
- `microservices-common-cache` - 缓存管理（Caffeine、Redis）
- `microservices-common-monitor` - 监控告警（Micrometer）
- `microservices-common-export` - 导出功能（EasyExcel、iText PDF）
- `microservices-common-workflow` - 工作流（Aviator、Quartz）
- `microservices-common-business` - 业务公共组件（DAO、Manager、Service接口等，不包含实体类 ✅ 方案C已执行）
  - 包含生物识别认证策略模块（`MultiModalAuthenticationStrategy`及相关实现）
  - 认证策略职责：记录认证方式用于统计和审计，不进行人员识别（设备端已完成识别）
  - 使用场景：access、attendance、visitor、video服务（consume服务不需要，设备端已识别，软件端只处理支付逻辑）
- `microservices-common-permission` - 权限验证

**依赖方向**：细粒度模块 → `microservices-common-core`（部分模块还依赖`microservices-common-entity`或`microservices-common-business`）

### 3) `microservices-common`（配置类和工具类容器）

- **新定位**：配置类和工具类容器（不再聚合细粒度模块和框架依赖）
- 放置内容：
  - 配置类：`JacksonConfiguration`、`OpenApiConfiguration`、`CommonComponentsConfiguration`
  - 配置属性类：`IoeDreamGatewayProperties`
  - 工具类：`StrategyFactory`
  - 边缘计算模型：`EdgeConfig`、`EdgeDevice`、`InferenceRequest`等
- **重要说明**：`GatewayServiceClient` 已迁移到 `microservices-common-gateway-client` 独立模块（见下文）
- 依赖方向：`microservices-common` → `microservices-common-core` + 配置类所需最小依赖（Spring Boot Starter、Jackson、Swagger、Micrometer等）
- **重要变更**：
  - ❌ 不再包含 Entity、DAO、Manager（这些已迁移到细粒度模块）
  - ❌ 不再聚合所有细粒度模块
  - ❌ 不再聚合所有框架依赖
  - ✅ 只包含配置类和工具类所需的最小依赖

## 目标形态（演进中）

建议按能力与稳定性拆为 5–8 个产物以内（避免碎片化）：

- `common-core`：最稳定共享基元（已以 `microservices-common-core` 形态落地）
- `common-spring`/`common-web`：框架横切一致性（拦截器、序列化、全局配置）
- `common-starter-*`：能力型自动装配（cache/mq/seata/security/mybatis/...）
- `*-domain-api`：跨域协作的“稳定契约”（仅模型/DTO/接口），禁止共享实现回流到公共库

## 细粒度模块落地状态（已全部落地）

**更新时间**: 2025-01-30  
**状态**: ✅ 所有细粒度模块已真实落地并纳入 Maven Reactor

**模块存在的判定标准（必须同时满足）**：

- 仓库中存在模块目录（例如 `microservices/microservices-common-xxx/`）
- 模块目录下存在 `pom.xml`
- `microservices/pom.xml` 的 `<modules>` 中包含该模块（纳入 Maven Reactor）

**已落地的细粒度模块**（全部满足上述标准）：

- ✅ `microservices-common-data` - 数据访问层
- ✅ `microservices-common-security` - 安全认证
- ✅ `microservices-common-cache` - 缓存管理
- ✅ `microservices-common-monitor` - 监控告警
- ✅ `microservices-common-business` - 业务公共组件
- ✅ `microservices-common-permission` - 权限验证
- ✅ `microservices-common-export` - 导出功能
- ✅ `microservices-common-workflow` - 工作流

**依赖规范**：

- ✅ 各服务应直接依赖需要的细粒度模块
- ✅ 需要使用网关客户端的服务应依赖 `microservices-common-gateway-client`
- ❌ 禁止服务同时依赖 `microservices-common` 和细粒度模块（网关服务除外）
- ❌ 禁止细粒度模块依赖 `microservices-common`
- ❌ 禁止 `microservices-common-gateway-client` 依赖 `microservices-common`

## 强制规则（全局一致性）

- **禁止领域实现回流**：领域逻辑应归属到具体业务服务；跨域协作优先 RPC/事件，不优先共享实现。
- **禁止环依赖**：公共库/领域 API 不得形成循环依赖。
- **稳定性优先**：越底层的模块越少变；越上层的模块越可变。
- **新增共享代码必须声明归属**：属于“稳定基元”才可进入 `microservices-common-core`，否则进入上层模块或回归领域。

## Gateway客户端模块（2025-01-30新增）

**模块名称**：`microservices-common-gateway-client`

**迁移原因**：为避免业务服务同时依赖 `microservices-common` 和细粒度模块，将 `GatewayServiceClient` 提取到独立模块。

**模块定位**：
- 提供统一的微服务间调用接口
- 支持通过API网关调用其他服务
- 独立模块，避免架构违规

**依赖关系**：
- 依赖：`microservices-common-core`（用于 ResponseDTO）
- 依赖：`spring-boot-starter-web`（用于 RestTemplate）
- 依赖：`jackson-databind`（用于 ObjectMapper）

**使用方式**：
- 业务服务直接依赖 `microservices-common-gateway-client` 模块
- 不再需要同时依赖 `microservices-common` 和细粒度模块

**相关文档**：
- 混合调用策略：`documentation/architecture/INTERNAL_CALL_STRATEGY.md`
- 依赖违规分析报告：`documentation/technical/DEPENDENCY_VIOLATION_ANALYSIS_REPORT.md`

## 方案C重构说明

**重构时间**: 2025-01-30  
**重构方案**: 方案C（混合方案）

**核心变更**：

1. ✅ 清理 `microservices-common` 聚合反模式
   - 移除所有细粒度模块的聚合依赖
   - 移除所有框架依赖（MyBatis-Plus、Redis、Druid、Flyway等）
   - 只保留配置类所需的最小依赖

2. ✅ 服务依赖优化
   - `ioedream-common-service`：直接依赖所有细粒度模块
   - `ioedream-database-service`：直接依赖 `common-core` + `common-business`
   - `ioedream-gateway-service`：保留 `microservices-common`（需要配置类），但exclusions大幅减少

3. ✅ 建立4层依赖架构
   - 第1层：最底层模块（core、entity）
   - 第2层：基础能力模块（细粒度模块）
   - 第3层：配置类容器（microservices-common）
   - 第4层：业务微服务

**详细文档**：

- 依赖优化文档：`documentation/technical/MODULE_DEPENDENCY_SYSTEMATIC_OPTIMIZATION.md`
- 执行计划：`documentation/technical/MODULE_DEPENDENCY_REFACTORING_EXECUTION_PLAN.md`
