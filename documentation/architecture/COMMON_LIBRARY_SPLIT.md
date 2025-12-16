# microservices-common 拆分与依赖治理

## 目标
- 将 `microservices-common` 从“共享单体”收敛为“共享能力”，降低跨域耦合与变更半径。
- 通过“稳定性分层 + 依赖方向约束”保证全局一致性与可演进性。

## 当前落地（已启用）
### 1) `microservices-common-core`（最小稳定内核）
- 放置内容：`ResponseDTO`、通用异常（`BusinessException/ParamException/SystemException`）、常量等。
- 设计原则：尽量不依赖 Spring，仅依赖 `slf4j-api` 与必要的基础库。
- 依赖方向：**只能被依赖**，禁止反向依赖任何 starter/业务模块。

### 2) `microservices-common`（公共库聚合）
- 放置内容：框架横切配置（例如 Jackson/Tracing）、通用 Manager/DAO/Entity、统一调用客户端等。
- 依赖方向：`microservices-common` → `microservices-common-core`。

## 目标形态（演进中）
建议按能力与稳定性拆为 5–8 个产物以内（避免碎片化）：
- `common-core`：最稳定共享基元（已以 `microservices-common-core` 形态落地）
- `common-spring`/`common-web`：框架横切一致性（拦截器、序列化、全局配置）
- `common-starter-*`：能力型自动装配（cache/mq/seata/security/mybatis/...）
- `*-domain-api`：跨域协作的“稳定契约”（仅模型/DTO/接口），禁止共享实现回流到公共库

## 强制规则（全局一致性）
- **禁止领域实现回流**：领域逻辑应归属到具体业务服务；跨域协作优先 RPC/事件，不优先共享实现。
- **禁止环依赖**：公共库/领域 API 不得形成循环依赖。
- **稳定性优先**：越底层的模块越少变；越上层的模块越可变。
- **新增共享代码必须声明归属**：属于“稳定基元”才可进入 `microservices-common-core`，否则进入上层模块或回归领域。

## 与内部调用策略的关系
直连能力属于“平台护栏”能力，建议放在公共库聚合层：
- 直连客户端：`microservices/microservices-common/src/main/java/net/lab1024/sa/common/gateway/DirectServiceClient.java`
- 混合调用策略：`documentation/architecture/INTERNAL_CALL_STRATEGY.md`

